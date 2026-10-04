package rotp.multiplayer.hotseat;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import javax.swing.*;
import rotp.model.game.GameSession;
import rotp.multiplayer.session.PlayerSeat;
import rotp.multiplayer.turn.*;
import rotp.ui.BasePanel;
import rotp.ui.multiplayer.*;
import rotp.util.Base;

/** One worker owns simulation; the event thread owns the covered desktop. */
public final class HotSeatController implements AutoCloseable, Base {
    private final GameSession game;
    private final HotSeatDesktop desktop;
    private final HotSeatDecisions decisions;
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "hot-seat-turn");
        thread.setDaemon(true);
        return thread;
    });
    private final AtomicBoolean running = new AtomicBoolean();
    private volatile boolean closed;
    private volatile CompletableFuture<?> pending;
    private volatile boolean boundarySave;
    // The player who acknowledged the last decision handoff and is still at the screen.
    private volatile String seatedOwner;
    // Reports are playing on the revealed map; planning stays locked until they finish.
    private volatile boolean replayingReports;
    // Play by email: who last unlocked this computer with their PIN since the match was opened.
    private volatile String localPlayer;
    private long missionSequence;

    public HotSeatController(GameSession session, HotSeatDesktop desktop, HotSeatDecisions decisions) {
        game = Objects.requireNonNull(session);
        this.desktop = Objects.requireNonNull(desktop);
        this.decisions = Objects.requireNonNull(decisions);
        if (session.hotSeatSetup() == null) throw new IllegalArgumentException("Hot-seat setup required");
    }

    public void start() {
        requireEdt();
        attachProviders();
        execute(true);
    }

    public void resume() {
        requireEdt();
        seatedOwner = null;
        attachProviders();
        if (game.hotSeatState().snapshot().pendingDecisionId() != null
                || game.hotSeatState().snapshot().stage() == HotSeatState.Stage.RESOLVING)
            execute(game.turnCoordinator().turn() < 0);
        else presentPlanning();
    }

    public boolean canEdit(int empireId) {
        var snapshot = game.hotSeatState().snapshot();
        var seat = game.controllerRegistry().seatForEmpire(empireId);
        return !closed && !running.get() && !replayingReports && game == GameSession.instance()
                && !desktop.isCovered() && snapshot.stage() == HotSeatState.Stage.PLANNING
                && seat != null && seat.playerId().equals(snapshot.ownerPlayerId());
    }
    public boolean safeToSave() { return !running.get() || boundarySave || pending != null && game.hotSeatState().snapshot().recoverable(); }
    // A closed controller's worker can stay marked running after its wait was cancelled.
    public boolean safeToReplace() { return closed || !running.get() || pending != null; }
    public void captureView() { desktop.captureView(game); }

    public boolean finishPlayerTurn(String owner, long revision) {
        requireEdt();
        var before = game.hotSeatState().snapshot();
        if (closed || running.get() || replayingReports || desktop.isCovered() || before.revision() != revision
                || before.stage() != HotSeatState.Stage.PLANNING || !Objects.equals(before.ownerPlayerId(), owner)) return false;
        var byEmail = game.playByEmail();
        if (byEmail == null) return completePlayerTurn(owner, revision);
        desktop.showPrivatePanel(new PlayByEmailFinishPanel(game.hotSeatSetup().displayName(owner),
                byEmail.orders(owner), orders -> {
                    if (closed) return;
                    byEmail.orders(owner, orders);
                    if (!completePlayerTurn(owner, revision)) desktop.revealPlanning();
                }, desktop::revealPlanning));
        return true;
    }

    private boolean completePlayerTurn(String owner, long revision) {
        var before = game.hotSeatState().snapshot();
        if (closed || running.get() || before.revision() != revision
                || before.stage() != HotSeatState.Stage.PLANNING || !Objects.equals(before.ownerPlayerId(), owner)) return false;
        if (!saveLastSafe()) return false;
        if (!game.inProgress()) {
            game.hotSeatState().finishMatch();
            presentPlanning();
            return true;
        }
        if (!game.hotSeatState().finishPlanning(owner, revision, living())) return false;
        var snapshot = game.hotSeatState().snapshot();
        if (snapshot.stage() == HotSeatState.Stage.RESOLVING) execute(false);
        else presentPlanning();
        return true;
    }

    private void execute(boolean startup) {
        if (!running.compareAndSet(false, true)) return;
        showStatus("HOTSEAT_RESOLVING");
        worker.execute(() -> {
            try {
                if (startup) {
                    while (answer(game.deliverInProcessNotifications())) { }
                } else {
                    TurnCheckpoint checkpoint;
                    do {
                        // A save at the final phase must finish this round, not start another one.
                        checkpoint = game.turnCoordinator().turn() >= 0 && game.turnCoordinator().nextPhase() == null
                                && game.hotSeatState().snapshot().turn() < game.galaxy().currentTurn()
                                ? game.deliverInProcessNotifications() : game.advanceInProcessPhase();
                        while (answer(checkpoint)) checkpoint = game.deliverInProcessNotifications();
                        game.hotSeatState().resolutionBoundary(true);
                        boundarySave = true;
                        try {
                            if (!saveLastSafe()) return;
                        } finally {
                            boundarySave = false;
                            if (game.hotSeatState().snapshot().stage() == HotSeatState.Stage.RESOLVING)
                                game.hotSeatState().resolutionBoundary(false);
                        }
                    } while (game.inProgress() && game.turnCoordinator().nextPhase() != null);
                }
                if (closed) return;
                if (!game.inProgress()) game.hotSeatState().finishMatch();
                else if (game.hotSeatState().snapshot().stage() == HotSeatState.Stage.RESOLVING)
                    game.hotSeatState().completeResolution(game.galaxy().currentTurn(), living());
                running.set(false);
                onEdt(this::presentPlanning);
            } catch (Throwable failure) {
                if (!closed) {
                    failure.printStackTrace();
                    game.hotSeatState().failResolution();
                    running.set(false);
                    onEdt(() -> showStatus("HOTSEAT_RESOLUTION_FAILED"));
                }
            }
        });
    }

    private boolean answer(TurnCheckpoint checkpoint) throws Exception {
        if (checkpoint.councilVote() != null) {
            var d = checkpoint.councilVote();
            Integer value = request(d.ownerPlayerId(), d.id(), true, () -> decisions.councilVote(d));
            require(new DecisionRouter(game).submitCouncilVote(d.ownerPlayerId(), d.id(), value)
                    == DecisionRouter.Result.ACCEPTED);
            accepted(d.ownerPlayerId(), d.id());
        } else if (checkpoint.councilRuling() != null) {
            var d = checkpoint.councilRuling();
            boolean value = request(d.ownerPlayerId(), d.id(), true, () -> decisions.councilRuling(d));
            require(new DecisionRouter(game).submitCouncilRuling(d.ownerPlayerId(), d.id(), value)
                    == DecisionRouter.Result.ACCEPTED);
            accepted(d.ownerPlayerId(), d.id());
        } else if (!checkpoint.researchChoices().isEmpty()) {
            var d = seatedFirst(checkpoint.researchChoices(), PendingResearchDecision::ownerPlayerId);
            String value = request(d.ownerPlayerId(), d.id(), true, () -> decisions.research(d));
            require(new ResearchDecisionRouter(game).submit(d.ownerPlayerId(), d.id(), d.empireId(),
                    d.categoryIndex(), value) == ResearchDecisionRouter.Result.ACCEPTED);
            accepted(d.ownerPlayerId(), d.id());
        } else if (!checkpoint.colonizationChoices().isEmpty()) {
            var d = seatedFirst(checkpoint.colonizationChoices(), PendingColonizationDecision::ownerPlayerId);
            boolean value = request(d.ownerPlayerId(), d.id(), true, () -> decisions.colonize(d));
            require(game.answerColonizationDecision(d.ownerPlayerId(), d.id(), value));
            accepted(d.ownerPlayerId(), d.id());
        } else if (!checkpoint.diplomacyChoices().isEmpty()) {
            var d = seatedFirst(checkpoint.diplomacyChoices(), c -> c.notice().ownerPlayerId());
            String owner = d.notice().ownerPlayerId();
            boolean value = request(owner, d.id(), true, () -> decisions.diplomacy(d));
            require(game.answerDiplomacyDecision(owner, d.id(), value));
            accepted(owner, d.id());
        } else return false;
        return true;
    }

    /** Keeps the seated player's remaining choices together before handing off. */
    private <T> T seatedFirst(List<T> choices, java.util.function.Function<T, String> owner) {
        String seated = seatedOwner;
        for (T choice : choices) if (Objects.equals(seated, owner.apply(choice))) return choice;
        return choices.get(0);
    }

    private <T> T request(String owner, String id, boolean safe,
            Supplier<CompletableFuture<T>> display) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Worker required");
        if (closed) throw new CancellationException();
        var existing = game.hotSeatState().snapshot();
        if (existing.pendingDecisionId() == null) game.hotSeatState().beginDecision(owner, id, safe);
        else if (!id.equals(existing.pendingDecisionId()) || !owner.equals(existing.ownerPlayerId()))
            throw new IllegalStateException("Saved decision does not match pending model choice");
        CompletableFuture<T> response = new CompletableFuture<>();
        pending = response;
        if (safe && !saveLastSafe()) throw new IllegalStateException("Unable to save the decision boundary");
        onEdt(() -> {
            var snapshot = game.hotSeatState().snapshot();
            Runnable show = () -> {
                try {
                    display.get().whenComplete((value, error) -> {
                        if (closed) return;
                        if (error == null) response.complete(value);
                        else response.completeExceptionally(error);
                    });
                } catch (Throwable error) { response.completeExceptionally(error); }
            };
            // Same player still seated: no privacy boundary to cross.
            if (owner.equals(seatedOwner) && desktop.isCovered()) {
                if (!closed && game.hotSeatState().confirmHandoff(snapshot.revision())) show.run();
                return;
            }
            seatedOwner = null;
            handoff(snapshot, () -> {
                if (closed || !game.hotSeatState().confirmHandoff(snapshot.revision())) return;
                desktop.activateViewer(game, owner);
                seatedOwner = owner;
                show.run();
            });
        });
        try { return response.get(); }
        finally { pending = null; }
    }

    private void accepted(String owner, String id) {
        require(game.hotSeatState().completeDecision(owner, id, game.hotSeatState().snapshot().revision()));
        onEdt(() -> showStatus("HOTSEAT_RESOLVING"));
    }

    private <T> T mission(String owner, Supplier<CompletableFuture<T>> display) {
        String id = "mission:" + (++missionSequence);
        try {
            T result = request(owner, id, false, display);
            accepted(owner, id);
            return result;
        } catch (Exception failure) { throw new IllegalStateException("Hot-seat mission interrupted", failure); }
    }

    private void presentPlanning() {
        requireEdt();
        if (closed) return;
        seatedOwner = null;
        if (!game.inProgress() && game.hotSeatState().snapshot().stage() != HotSeatState.Stage.FINISHED)
            game.hotSeatState().finishMatch();
        if (!saveLastSafe()) return;
        var snapshot = game.hotSeatState().snapshot();
        boolean finished = snapshot.stage() == HotSeatState.Stage.FINISHED;
        for (var human : game.hotSeatSetup().humans()) {
            if (finished || game.galaxy().empire(human.empireId()).extinct()) {
                var reports = game.hotSeatInbox().unread(human.empireId());
                if (reports.isEmpty()) continue;
                if (game.playByEmail() != null) {
                    // Never wait on a player who is out of the game for a file round trip.
                    for (var report : reports) game.hotSeatInbox().acknowledge(human.empireId(), report.id());
                    continue;
                }
                var privateHandoff = new HotSeatSnapshot(snapshot.turn(), snapshot.revision(), snapshot.stage(),
                        human.playerId(), snapshot.finishedPlayers(), null, true);
                desktop.cover(privateHandoff, () -> desktop.showPrivatePanel(new HotSeatReportsPanel(
                        human.displayName(), reports, () -> {
                            if (closed) return;
                            for (var report : reports) game.hotSeatInbox().acknowledge(human.empireId(), report.id());
                            presentPlanning();
                        })));
                return;
            }
        }
        if (finished) {
            Runnable results = () -> desktop.showPrivatePanel(new HotSeatResultPanel(game.matchOutcome(),
                    game.hotSeatSetup(), () -> { close(); rotp.ui.RotPUI.instance().selectGamePanel(); }));
            // The match ended here: everyone else needs the final file to see the result.
            if (game.playByEmail() != null && !game.playByEmail().finalResultExported())
                sendFinalResult(snapshot, results);
            else results.run();
            return;
        }
        handoff(snapshot, () -> {
            if (closed || !game.hotSeatState().confirmHandoff(snapshot.revision())) return;
            String owner = snapshot.ownerPlayerId();
            desktop.activateViewer(game, owner);
            int empire = game.galaxy().player().id;
            var reports = game.hotSeatInbox().unread(empire);
            Runnable summary = () -> {
                if (closed || !Objects.equals(owner, game.hotSeatState().snapshot().ownerPlayerId())) return;
                var rest = reports.stream().filter(r -> !HotSeatReportReplay.replayable(r)).toList();
                for (var report : reports)
                    if (HotSeatReportReplay.replayable(report)) game.hotSeatInbox().acknowledge(empire, report.id());
                if (rest.isEmpty()) { desktop.revealPlanning(); return; }
                desktop.showPrivatePanel(new HotSeatReportsPanel(game.hotSeatSetup().displayName(owner),
                        rest, () -> {
                            if (closed || !Objects.equals(owner, game.hotSeatState().snapshot().ownerPlayerId())) return;
                            for (var report : rest) game.hotSeatInbox().acknowledge(empire, report.id());
                            desktop.revealPlanning();
                        }));
            };
            var replay = reports.stream().filter(HotSeatReportReplay::replayable).toList();
            if (replay.isEmpty()) { summary.run(); return; }
            // The game's own report screens, shown over this player's map.
            replayingReports = true;
            desktop.revealPlanning();
            HotSeatReportReplay.play(game, replay, () -> !closed, () -> {
                replayingReports = false;
                summary.run();
            });
        });
    }

    /**
     * Hands the computer to the snapshot's owner. In play by email a different person means
     * saving a turn file for them; nobody sees the next empire without its PIN.
     */
    private void handoff(HotSeatSnapshot snapshot, Runnable confirm) {
        if (game.playByEmail() == null) { desktop.cover(snapshot, confirm); return; }
        String owner = snapshot.ownerPlayerId();
        Runnable unlocked = () -> { localPlayer = owner; confirm.run(); };
        if (owner.equals(localPlayer) && desktop.isCovered()) { confirm.run(); return; }
        if (localPlayer == null) { desktop.cover(snapshot, unlocked); return; }
        String name = game.hotSeatSetup().displayName(owner);
        java.io.File file = GameSession.saveFileNamed(rotp.multiplayer.pbem.TurnFiles.fileName(
                game.playByEmail().matchLabel(), snapshot.turn(), snapshot.revision(), name));
        try { HotSeatPersistence.save(game, file); }
        catch (Exception failure) {
            failure.printStackTrace();
            game.hotSeatState().failResolution();
            showStatus("HOTSEAT_RESOLUTION_FAILED");
            return;
        }
        game.playByEmailSentFile(file);
        localPlayer = null;
        desktop.clearPrivateUi();
        desktop.showPrivatePanel(PlayByEmailSendPanel.forPlayer(name, file.getName(),
                () -> { close(); rotp.ui.RotPUI.instance().selectGamePanel(); },
                () -> { if (!closed) desktop.cover(snapshot, unlocked); }));
    }

    private void sendFinalResult(HotSeatSnapshot snapshot, Runnable results) {
        java.io.File file = GameSession.saveFileNamed(rotp.multiplayer.pbem.TurnFiles.finalFileName(
                game.playByEmail().matchLabel(), snapshot.turn()));
        // Persist this in the final file so recipients go directly to the results.
        // A recovery checkpoint still needs an export even without a local PIN unlock.
        game.playByEmail().finalResultExported(true);
        try { HotSeatPersistence.save(game, file); }
        catch (Exception failure) {
            game.playByEmail().finalResultExported(false);
            failure.printStackTrace();
            results.run();
            return;
        }
        game.playByEmailSentFile(file);
        localPlayer = null;
        desktop.clearPrivateUi();
        desktop.showPrivatePanel(PlayByEmailSendPanel.finalResult(file.getName(), results));
    }

    private Set<String> living() {
        Set<String> result = new LinkedHashSet<>();
        for (var seat : game.controllerRegistry().seats())
            if (seat.controllerType() == PlayerSeat.ControllerType.HUMAN
                    && !game.galaxy().empire(seat.empireId()).extinct()) result.add(seat.playerId());
        return result;
    }

    private void attachProviders() {
        var scouting = new HashMap<String, StrictInProcessNotificationSink.ScoutingNoticeProvider>();
        var technology = new HashMap<String, StrictInProcessNotificationSink.TechnologyNoticeProvider>();
        var ships = new HashMap<String, StrictInProcessNotificationSink.ShipConstructionNoticeProvider>();
        var diplomacy = new HashMap<String, StrictInProcessNotificationSink.DiplomacyNoticeProvider>();
        var bombingReports = new HashMap<String, StrictInProcessNotificationSink.BombardmentNoticeProvider>();
        var sabotageReports = new HashMap<String, StrictInProcessNotificationSink.SabotageNoticeProvider>();
        var bombing = new HashMap<String, InProcessBombardmentDecisionAdapter.Provider>();
        var espionage = new HashMap<String, InProcessEspionageDecisionAdapter.Provider>();
        var sabotage = new HashMap<String, InProcessSabotageDecisionAdapter.Provider>();
        for (var human : game.hotSeatSetup().humans()) {
            String owner = human.playerId();
            scouting.put(owner, (empire, systems) -> report(empire, "SCOUT", text("HOTSEAT_SCOUTING"),
                    systems.values().stream().flatMap(List::stream).distinct().map(id -> systemName(empire, id)).toList(),
                    scoutedPayload(systems)));
            technology.put(owner, n -> report(n.recipientEmpireId(), "TECH", text("HOTSEAT_RESEARCH"),
                    List.of(tech(n.techId()).name(), tech(n.techId()).detail()), n));
            ships.put(owner, (empire, counts) -> report(empire, "SHIPS", text("HOTSEAT_CONSTRUCTION"),
                    counts.entrySet().stream().map(e -> game.galaxy().empire(empire).shipLab()
                            .design(e.getKey()).name() + ": " + e.getValue()).toList(), new HashMap<>(counts)));
            diplomacy.put(owner, n -> {
                report(n.recipientEmpireId(), "DIPLOMACY", text("HOTSEAT_DIPLOMACY"),
                        List.of(game.galaxy().empire(n.talkerEmpireId()).name(),
                                n.messageType().replace('-', ':').replaceAll("([a-z])([A-Z])", "$1 $2")), n);
                return false;
            });
            bombingReports.put(owner, n -> report(n.recipientEmpireId(), "BOMBARDMENT", text("HOTSEAT_BOMBARDMENT"),
                    List.of(systemName(n.recipientEmpireId(), n.systemId()),
                            text("HOTSEAT_POPULATION", String.valueOf(n.populationBefore()), String.valueOf(n.populationAfter())),
                            text("HOTSEAT_BASES", String.valueOf(n.basesBefore()), String.valueOf(n.basesAfter())),
                            text("HOTSEAT_FACTORIES", String.valueOf(n.factoriesBefore()), String.valueOf(n.factoriesAfter()))), n));
            sabotageReports.put(owner, n -> report(n.recipientEmpireId(), "SABOTAGE", text("HOTSEAT_SABOTAGE"),
                    List.of(systemName(n.recipientEmpireId(), n.systemId()),
                            text("HOTSEAT_SABOTAGE_" + n.action()) + ": " + n.amount()), n));
            if (game.playByEmail() != null) {
                // Nobody waits at the keyboard mid-phase: standing orders answer at once.
                bombing.put(owner, d -> StandingOrderProviders.bombard(game, d));
                espionage.put(owner, d -> StandingOrderProviders.espionage(game, d));
                sabotage.put(owner, d -> StandingOrderProviders.sabotage(game, d));
                continue;
            }
            bombing.put(owner, d -> mission(owner, () -> decisions.bombardment(d)));
            espionage.put(owner, d -> mission(owner, () -> decisions.espionage(d)));
            sabotage.put(owner, d -> mission(owner, () -> decisions.sabotage(d)));
        }
        game.turnNotificationSink(StrictInProcessNotificationSink.deferredDecisions(scouting, technology,
                ships, diplomacy, bombingReports, sabotageReports));
        game.bombardmentDecisionAdapter(new InProcessBombardmentDecisionAdapter(bombing));
        game.espionageDecisionAdapter(new InProcessEspionageDecisionAdapter(espionage));
        game.sabotageDecisionAdapter(new InProcessSabotageDecisionAdapter(sabotage));
    }

    private String systemName(int owner, int system) { return game.galaxy().empire(owner).sv.name(system); }
    private void report(int owner, String kind, String title, List<String> lines, java.io.Serializable payload) {
        game.hotSeatInbox().append(game.galaxy().currentTurn(), owner, kind, title, lines, payload);
    }
    private static HashMap<String, ArrayList<Integer>> scoutedPayload(Map<String, List<Integer>> systems) {
        var copy = new HashMap<String, ArrayList<Integer>>();
        systems.forEach((source, ids) -> copy.put(source, new ArrayList<>(ids)));
        return copy;
    }
    private void showStatus(String key) {
        requireEdt();
        BasePanel panel = new BasePanel() {
            private static final long serialVersionUID = 1L;
            @Override public void paintComponent(java.awt.Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
        };
        panel.setLayout(new java.awt.GridBagLayout());
        JLabel label = HotSeatStyle.title(text(key));
        panel.add(label);
        if (key.equals("HOTSEAT_RESOLUTION_FAILED")) {
            JPanel actions = new JPanel();
            actions.setOpaque(false);
            JButton restore = HotSeatStyle.button(new JButton(text("HOTSEAT_RESTORE_SAFE")), 22);
            restore.setEnabled(lastSafeFile().isFile());
            restore.addActionListener(e -> {
                try { HotSeatPersistence.load(lastSafeFile()); }
                catch (Exception failure) { label.setText(text("HOTSEAT_LOAD_FAILED")); }
            });
            JButton menu = HotSeatStyle.button(new JButton(text("HOTSEAT_MENU")), 22);
            menu.addActionListener(e -> { close(); rotp.ui.RotPUI.instance().selectGamePanel(); });
            actions.add(restore); actions.add(menu);
            panel.setLayout(new java.awt.BorderLayout(20, 20));
            panel.add(label, java.awt.BorderLayout.CENTER);
            panel.add(actions, java.awt.BorderLayout.SOUTH);
        }
        desktop.showPrivatePanel(panel);
    }
    private java.io.File lastSafeFile() { return GameSession.saveFileNamed("HotSeat-LastSafe.rotp"); }
    private boolean saveLastSafe() {
        try {
            HotSeatPersistence.save(game, lastSafeFile());
            game.saveRecentSession();
            return true;
        } catch (Exception failure) {
            failure.printStackTrace();
            game.hotSeatState().failResolution();
            running.set(false);
            onEdt(() -> showStatus("HOTSEAT_RESOLUTION_FAILED"));
            return false;
        }
    }
    private void onEdt(Runnable action) {
        SwingUtilities.invokeLater(() -> { if (!closed) action.run(); });
    }
    private static void require(boolean accepted) {
        if (!accepted) throw new IllegalStateException("Hot-seat answer rejected");
    }
    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Event thread required");
    }
    @Override public void close() {
        requireEdt();
        closed = true;
        if (pending != null) pending.cancel(true);
        if (replayingReports) {
            // Release the report screen still waiting, and the turn flag it holds.
            game.resumeNextTurnProcessing();
            GameSession.performingTurn(false);
        }
        worker.shutdownNow();
        desktop.close();
    }
}
