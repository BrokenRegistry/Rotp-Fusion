package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.HotSeatDecisions;
import rotp.multiplayer.turn.*;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.diplomacy.DialogueManager;
import rotp.ui.notifications.DiplomaticNotification;
import rotp.ui.tech.SelectNewTechUI;
import rotp.util.Base;

/** Legal choices only, presented on the owner's opaque desktop. */
public final class HotSeatDecisionPanels implements HotSeatDecisions, Base {
    private final GameSession game;
    private final HotSeatDesktop desktop;
    private record Option<T>(String label, T value) { @Override public String toString() { return label; } }
    public HotSeatDecisionPanels(GameSession game, HotSeatDesktop desktop) {
        this.game = game;
        this.desktop = desktop;
    }

    // The game's own screens, shown on the decider's uncovered desktop. Each reports the
    // choice back to the turn worker, which applies it. Without the live game objects a
    // screen needs, a decision falls back to a plain list of the legal choices.

    @Override public CompletableFuture<Integer> councilVote(PendingDecision d) {
        var council = game.galaxy().council();
        // The council screen needs a convened vote waiting on this seat.
        if (council.candidate1() != null && council.votingInProgress()
                && council.nextVoter().id == d.empireId()) {
            var future = new CompletableFuture<Integer>();
            nativeCouncilVote(d, future);
            return future;
        }
        var options = new ArrayList<Option<Integer>>();
        for (int empire : d.legalEmpireIds()) options.add(new Option<>(empireName(empire), empire));
        if (d.abstainAllowed()) options.add(new Option<>(text("HOTSEAT_ABSTAIN"), null));
        return choose(text("HOTSEAT_COUNCIL_VOTE"), options);
    }
    private void nativeCouncilVote(PendingDecision d, CompletableFuture<Integer> future) {
        desktop.revealPlanning();
        RotPUI.instance().selectHotSeatCouncilVote(chosen -> {
            Integer id = chosen == null ? null : chosen.id;
            boolean legal = id == null ? d.abstainAllowed() : d.legalEmpireIds().contains(id);
            if (legal) future.complete(id);
            else nativeCouncilVote(d, future);
        });
    }
    @Override public CompletableFuture<Boolean> councilRuling(PendingDecision d) {
        if (game.galaxy().council().hasLeader()) {
            var future = new CompletableFuture<Boolean>();
            desktop.revealPlanning();
            RotPUI.instance().selectHotSeatCouncilRuling(future::complete);
            return future;
        }
        return yesNo(text("HOTSEAT_COUNCIL_RULING_DETAIL", game.galaxy().council().leader().name()),
                "HOTSEAT_ACCEPT", "HOTSEAT_REJECT");
    }
    /** The game's own research screen; the choice returns to the turn worker. */
    @Override public CompletableFuture<String> research(PendingResearchDecision d) {
        requireEdt();
        var future = new CompletableFuture<String>();
        var screen = new SelectNewTechUI();
        screen.hotSeatCategory(game.galaxy().empire(d.empireId()).tech().category(d.categoryIndex()), id -> {
            if (d.legalTechIds().contains(id)) future.complete(id);
        });
        screen.setFocusable(true);
        screen.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) { screen.keyPressed(e); }
        });
        // The game's animation timer pauses while the desktop is covered.
        Timer repaint = new Timer(100, null);
        repaint.addActionListener(e -> {
            if (future.isDone() || !screen.isDisplayable()) repaint.stop();
            else screen.repaint();
        });
        BasePanel panel = new BasePanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.add(screen, BorderLayout.CENTER);
        JPanel footer = saveFooter();
        if (footer != null) panel.add(footer, BorderLayout.SOUTH);
        desktop.showPrivatePanel(panel);
        screen.requestFocusInWindow();
        repaint.start();
        return future;
    }
    @Override public CompletableFuture<Boolean> colonize(PendingColonizationDecision d) {
        var fleet = game.colonizationFleet(d.id());
        var design = game.colonizationDesign(d.id());
        if (fleet != null && design != null) {
            var future = new CompletableFuture<Boolean>();
            desktop.revealPlanning();
            RotPUI.instance().selectHotSeatColonize(d.systemId(), fleet, design, future::complete);
            return future;
        }
        return yesNo(text("HOTSEAT_COLONIZE", systemName(d.empireId(), d.systemId())),
                "HOTSEAT_COLONIZE_YES", "HOTSEAT_SKIP");
    }
    @Override public CompletableFuture<Boolean> diplomacy(PendingDiplomacyDecision d) {
        var n = d.notice();
        var talker = game.galaxy().empire(n.talkerEmpireId());
        var view = talker == null ? null : talker.viewForEmpire(n.recipientEmpireId());
        if (view != null) {
            var future = new CompletableFuture<Boolean>();
            var other = n.targetEmpireId() == null ? null : game.galaxy().empire(n.targetEmpireId());
            desktop.revealPlanning();
            if (RotPUI.instance().selectHotSeatDiplomaticOffer(
                    DiplomaticNotification.report(view, n.messageType(), other, n.incident()), future::complete))
                return future;
        }
        return yesNo(diplomacyDescription(game, d.notice()), "HOTSEAT_ACCEPT", "HOTSEAT_REJECT");
    }
    @Override public CompletableFuture<InProcessBombardmentDecisionAdapter.Choice> bombardment(BombardmentDecision d) {
        if (d.fleet() != null) {
            var future = new CompletableFuture<InProcessBombardmentDecisionAdapter.Choice>();
            desktop.revealPlanning();
            RotPUI.instance().selectHotSeatBombard(d.systemId(), d.fleet(), choice -> future.complete(
                    choice == 2 && d.targetAllowed() ? InProcessBombardmentDecisionAdapter.Choice.TARGET_BOMBARD
                    : choice == 0 ? InProcessBombardmentDecisionAdapter.Choice.SKIP
                    : InProcessBombardmentDecisionAdapter.Choice.BOMBARD));
            return future;
        }
        var options = new ArrayList<Option<InProcessBombardmentDecisionAdapter.Choice>>();
        options.add(new Option<>(text("HOTSEAT_SKIP"), InProcessBombardmentDecisionAdapter.Choice.SKIP));
        options.add(new Option<>(text("HOTSEAT_BOMBARD"), InProcessBombardmentDecisionAdapter.Choice.BOMBARD));
        if (d.targetAllowed()) options.add(new Option<>(text("HOTSEAT_TARGET_BOMBARD"), InProcessBombardmentDecisionAdapter.Choice.TARGET_BOMBARD));
        return choose(text("HOTSEAT_BOMBARDMENT") + ": " + systemName(d.attackerEmpireId(), d.systemId()), options);
    }
    @Override public CompletableFuture<InProcessEspionageDecisionAdapter.Choice> espionage(EspionageDecision d) {
        requireEdt();
        var future = new CompletableFuture<InProcessEspionageDecisionAdapter.Choice>();
        if (d.mission() != null) {
            desktop.revealPlanning();
            RotPUI.instance().selectHotSeatEspionage(d.mission(), d.victimEmpireId(), category -> {
                String techId = d.categoryTechIds().get(category);
                if (techId == null) return;
                // As in single player, a frameable theft must name one of the two suspects.
                if (d.frameableEmpireIds().size() < 2)
                    future.complete(new InProcessEspionageDecisionAdapter.Choice(techId, null));
                else RotPUI.instance().selectHotSeatFrame(d.mission(), techId, d.victimEmpireId(), framed ->
                        future.complete(new InProcessEspionageDecisionAdapter.Choice(techId,
                                d.frameableEmpireIds().contains(framed) ? framed : null)));
            });
            return future;
        }
        BasePanel panel = panel(text("HOTSEAT_ESPIONAGE"));
        JComboBox<Option<String>> technologies = new JComboBox<>();
        d.categoryTechIds().values().stream().distinct().sorted().forEach(id ->
                technologies.addItem(new Option<>(tech(id).name() + " - " + tech(id).detail(), id)));
        JComboBox<Option<Integer>> frame = new JComboBox<>();
        frame.addItem(new Option<>(text("HOTSEAT_NO_FRAME"), null));
        for (int empire : d.frameableEmpireIds()) frame.addItem(new Option<>(text("HOTSEAT_FRAME", empireName(empire)), empire));
        JPanel choices = new JPanel(new GridLayout(0, 1, 10, 10));
        choices.add(technologies);
        choices.add(frame);
        panel.add(choices, BorderLayout.CENTER);
        JButton confirm = new JButton(text("HOTSEAT_CONFIRM"));
        confirm.addActionListener(e -> {
            if (future.isDone() || technologies.getSelectedIndex() < 0) return;
            confirm.setEnabled(false);
            future.complete(new InProcessEspionageDecisionAdapter.Choice(
                    technologies.getItemAt(technologies.getSelectedIndex()).value(),
                    frame.getItemAt(frame.getSelectedIndex()).value()));
        });
        choices.add(confirm);
        desktop.showPrivatePanel(panel);
        return future;
    }
    @Override public CompletableFuture<InProcessSabotageDecisionAdapter.Choice> sabotage(SabotageDecision d) {
        if (d.mission() != null) {
            var future = new CompletableFuture<InProcessSabotageDecisionAdapter.Choice>();
            desktop.revealPlanning();
            RotPUI.instance().selectHotSeatSabotage(d.mission(), d.suggestedSystemId(), (action, system) -> {
                if (!d.targetSystemIds().getOrDefault(action, List.of()).contains(system)) return false;
                future.complete(new InProcessSabotageDecisionAdapter.Choice(action, system));
                return true;
            }, () -> future.complete(null));
            return future;
        }
        var options = new ArrayList<Option<InProcessSabotageDecisionAdapter.Choice>>();
        for (var action : rotp.model.empires.SpyNetwork.Sabotage.values())
            for (int system : d.targetSystemIds().getOrDefault(action, List.of()))
                options.add(new Option<>(text("HOTSEAT_SABOTAGE_" + action.name()) + " - "
                        + systemName(d.ownerEmpireId(), system),
                        new InProcessSabotageDecisionAdapter.Choice(action, system)));
        options.add(new Option<>(text("HOTSEAT_CANCEL"), null));
        return choose(text("HOTSEAT_SABOTAGE"), options);
    }

    public static String diplomacyDescription(GameSession game, DiplomacyNotice notice) {
        String kind = switch (notice.messageType()) {
            case DialogueManager.OFFER_TRADE -> "HOTSEAT_OFFER_TRADE";
            case DialogueManager.OFFER_PEACE -> "HOTSEAT_OFFER_PEACE";
            case DialogueManager.OFFER_PACT -> "HOTSEAT_OFFER_PACT";
            case DialogueManager.OFFER_ALLIANCE -> "HOTSEAT_OFFER_ALLIANCE";
            case DialogueManager.OFFER_JOINT_WAR -> "HOTSEAT_OFFER_JOINT_WAR";
            default -> "HOTSEAT_DIPLOMACY";
        };
        Base labels = new Base() { };
        String description = game.galaxy().empire(notice.talkerEmpireId()).name() + ": " + labels.text(kind);
        if (notice.tradeAmount() != null) description += " " + notice.tradeAmount();
        if (notice.targetEmpireId() != null) description += " " + game.galaxy().empire(notice.targetEmpireId()).name();
        return description;
    }

    private CompletableFuture<Boolean> yesNo(String title, String yes, String no) {
        return choose(title, List.of(new Option<>(text(yes), true), new Option<>(text(no), false)));
    }
    private <T> CompletableFuture<T> choose(String title, List<Option<T>> options) {
        requireEdt();
        CompletableFuture<T> future = new CompletableFuture<>();
        BasePanel panel = panel(title);
        JPanel buttons = new JPanel(new GridLayout(0, 1, 10, 10));
        for (var option : options) {
            JButton button = new JButton(option.label());
            button.addActionListener(e -> {
                if (future.isDone()) return;
                for (var component : buttons.getComponents()) component.setEnabled(false);
                future.complete(option.value());
            });
            buttons.add(button);
        }
        JPanel list = new JPanel(new BorderLayout());
        list.add(buttons, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        desktop.showPrivatePanel(panel);
        return future;
    }
    private BasePanel panel(String title) {
        BasePanel panel = new BasePanel();
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        panel.setBackground(new Color(25, 30, 40));
        JTextArea heading = new JTextArea(title);
        heading.setEditable(false);
        heading.setLineWrap(true);
        heading.setWrapStyleWord(true);
        heading.setOpaque(false);
        heading.setForeground(Color.WHITE);
        heading.setFont(heading.getFont().deriveFont(24f));
        panel.add(heading, BorderLayout.NORTH);
        JPanel footer = saveFooter();
        if (footer != null) panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }
    private JPanel saveFooter() {
        if (game.hotSeatState() == null) return null;
        JPanel footer = new JPanel();
        JButton save = new JButton(text("HOTSEAT_SAVE_CHECKPOINT"));
        save.setEnabled(rotp.multiplayer.hotseat.HotSeatPersistence.canSave(game));
        save.setToolTipText(text(save.isEnabled() ? "HOTSEAT_SAVE_CHECKPOINT_DETAIL" : "HOTSEAT_FINISH_CHOICE"));
        JLabel status = new JLabel(" ");
        save.addActionListener(e -> {
            try {
                game.saveSession("HotSeat-Decision.rotp", false);
                status.setText(text("HOTSEAT_SAVED"));
            } catch (Exception failure) { status.setText(text("HOTSEAT_FINISH_CHOICE")); }
        });
        footer.add(save); footer.add(status);
        return footer;
    }
    private String empireName(int id) { return game.galaxy().empire(id).name(); }
    private String systemName(int empire, int id) { return game.galaxy().empire(empire).sv.name(id); }
    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Event thread required");
    }
}
