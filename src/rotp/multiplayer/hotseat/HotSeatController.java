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
        attachProviders();
        if (game.hotSeatState().snapshot().pendingDecisionId() != null
                || game.hotSeatState().snapshot().stage() == HotSeatState.Stage.RESOLVING)
            execute(game.turnCoordinator().turn() < 0);
        else presentPlanning();
    }

    public boolean canEdit(int empireId) {
        var snapshot = game.hotSeatState().snapshot();
        var seat = game.controllerRegistry().seatForEmpire(empireId);
        return !closed && !running.get() && game == GameSession.instance()
                && !desktop.isCovered() && snapshot.stage() == HotSeatState.Stage.PLANNING
                && seat != null && seat.playerId().equals(snapshot.ownerPlayerId());
    }
    public boolean safeToSave() { return !running.get() || boundarySave || pending != null && game.hotSeatState().snapshot().recoverable(); }
    public boolean safeToReplace() { return !running.get() || pending != null; }
    public void captureView() { desktop.captureView(game); }

    public boolean finishPlayerTurn(String owner, long revision) {
        requireEdt();
        var before = game.hotSeatState().snapshot();
        if (closed || running.get() || desktop.isCovered() || before.revision() != revision
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
            var d = checkpoint.researchChoices().get(0);
            String value = request(d.ownerPlayerId(), d.id(), true, () -> decisions.research(d));
            require(new ResearchDecisionRouter(game).submit(d.ownerPlayerId(), d.id(), d.empireId(),
                    d.categoryIndex(), value) == ResearchDecisionRouter.Result.ACCEPTED);
            accepted(d.ownerPlayerId(), d.id());
        } else if (!checkpoint.colonizationChoices().isEmpty()) {
            var d = checkpoint.colonizationChoices().get(0);
            boolean value = request(d.ownerPlayerId(), d.id(), true, () -> decisions.colonize(d));
            require(game.answerColonizationDecision(d.ownerPlayerId(), d.id(), value));
            accepted(d.ownerPlayerId(), d.id());
        } else if (!checkpoint.diplomacyChoices().isEmpty()) {
            var d = checkpoint.diplomacyChoices().get(0);
            String owner = d.notice().ownerPlayerId();
            boolean value = request(owner, d.id(), true, () -> decisions.diplomacy(d));
            require(game.answerDiplomacyDecision(owner, d.id(), value));
            accepted(owner, d.id());
        } else return false;
        return true;
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
            desktop.cover(snapshot, () -> {
                if (closed || !game.hotSeatState().confirmHandoff(snapshot.revision())) return;
                desktop.activateViewer(game, owner);
                try {
                    display.get().whenComplete((value, error) -> {
                        if (closed) return;
                        if (error == null) response.complete(value);
                        else response.completeExceptionally(error);
                    });
                } catch (Throwable error) { response.completeExceptionally(error); }
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
        if (!game.inProgress() && game.hotSeatState().snapshot().stage() != HotSeatState.Stage.FINISHED)
            game.hotSeatState().finishMatch();
        if (!saveLastSafe()) return;
        var snapshot = game.hotSeatState().snapshot();
        boolean finished = snapshot.stage() == HotSeatState.Stage.FINISHED;
        for (var human : game.hotSeatSetup().humans()) {
            if (finished || game.galaxy().empire(human.empireId()).extinct()) {
                var reports = game.hotSeatInbox().unread(human.empireId());
                if (reports.isEmpty()) continue;
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
            desktop.showPrivatePanel(new HotSeatResultPanel(game.matchOutcome(), game.hotSeatSetup(),
                    () -> { close(); rotp.ui.RotPUI.instance().selectGamePanel(); }));
            return;
        }
        desktop.cover(snapshot, () -> {
            if (closed || !game.hotSeatState().confirmHandoff(snapshot.revision())) return;
            String owner = snapshot.ownerPlayerId();
            desktop.activateViewer(game, owner);
            int empire = game.galaxy().player().id;
            var reports = game.hotSeatInbox().unread(empire);
            if (reports.isEmpty()) desktop.revealPlanning();
            else desktop.showPrivatePanel(new HotSeatReportsPanel(game.hotSeatSetup().displayName(owner),
                    reports, () -> {
                        if (closed || !Objects.equals(owner, game.hotSeatState().snapshot().ownerPlayerId())) return;
                        for (var report : reports) game.hotSeatInbox().acknowledge(empire, report.id());
                        desktop.revealPlanning();
                    }));
        });
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
                    systems.values().stream().flatMap(List::stream).distinct().map(id -> systemName(empire, id)).toList()));
            technology.put(owner, n -> report(n.recipientEmpireId(), "TECH", text("HOTSEAT_RESEARCH"),
                    List.of(tech(n.techId()).name(), tech(n.techId()).detail())));
            ships.put(owner, (empire, counts) -> report(empire, "SHIPS", text("HOTSEAT_CONSTRUCTION"),
                    counts.entrySet().stream().map(e -> game.galaxy().empire(empire).shipLab()
                            .design(e.getKey()).name() + ": " + e.getValue()).toList()));
            diplomacy.put(owner, n -> {
                report(n.recipientEmpireId(), "DIPLOMACY", text("HOTSEAT_DIPLOMACY"),
                        List.of(game.galaxy().empire(n.talkerEmpireId()).name(),
                                n.messageType().replace('-', ':').replaceAll("([a-z])([A-Z])", "$1 $2")));
                return false;
            });
            bombingReports.put(owner, n -> report(n.recipientEmpireId(), "BOMBARDMENT", text("HOTSEAT_BOMBARDMENT"),
                    List.of(systemName(n.recipientEmpireId(), n.systemId()),
                            text("HOTSEAT_POPULATION", String.valueOf(n.populationBefore()), String.valueOf(n.populationAfter())),
                            text("HOTSEAT_BASES", String.valueOf(n.basesBefore()), String.valueOf(n.basesAfter())),
                            text("HOTSEAT_FACTORIES", String.valueOf(n.factoriesBefore()), String.valueOf(n.factoriesAfter())))));
            sabotageReports.put(owner, n -> report(n.recipientEmpireId(), "SABOTAGE", text("HOTSEAT_SABOTAGE"),
                    List.of(systemName(n.recipientEmpireId(), n.systemId()),
                            text("HOTSEAT_SABOTAGE_" + n.action()) + ": " + n.amount())));
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
    private void report(int owner, String kind, String title, List<String> lines) {
        game.hotSeatInbox().append(game.galaxy().currentTurn(), owner, kind, title, lines);
    }
    private void showStatus(String key) {
        requireEdt();
        BasePanel panel = new BasePanel();
        panel.setLayout(new java.awt.GridBagLayout());
        panel.setBackground(new java.awt.Color(20, 24, 32));
        JLabel label = new JLabel(text(key));
        label.setForeground(java.awt.Color.WHITE);
        panel.add(label);
        if (key.equals("HOTSEAT_RESOLUTION_FAILED")) {
            JPanel actions = new JPanel();
            JButton restore = new JButton(text("HOTSEAT_RESTORE_SAFE"));
            restore.setEnabled(lastSafeFile().isFile());
            restore.addActionListener(e -> {
                try { HotSeatPersistence.load(lastSafeFile()); }
                catch (Exception failure) { label.setText(text("HOTSEAT_LOAD_FAILED")); }
            });
            JButton menu = new JButton(text("HOTSEAT_MENU"));
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
        worker.shutdownNow();
        desktop.close();
    }
}
