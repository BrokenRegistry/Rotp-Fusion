package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.HotSeatDecisions;
import rotp.multiplayer.turn.*;
import rotp.ui.BasePanel;
import rotp.ui.diplomacy.DialogueManager;
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

    @Override public CompletableFuture<Integer> councilVote(PendingDecision d) {
        var options = new ArrayList<Option<Integer>>();
        for (int empire : d.legalEmpireIds()) options.add(new Option<>(empireName(empire), empire));
        if (d.abstainAllowed()) options.add(new Option<>(text("HOTSEAT_ABSTAIN"), null));
        return choose(text("HOTSEAT_COUNCIL_VOTE"), options);
    }
    @Override public CompletableFuture<Boolean> councilRuling(PendingDecision d) {
        return yesNo(text("HOTSEAT_COUNCIL_RULING_DETAIL", game.galaxy().council().leader().name()),
                "HOTSEAT_ACCEPT", "HOTSEAT_REJECT");
    }
    @Override public CompletableFuture<String> research(PendingResearchDecision d) {
        var options = new ArrayList<Option<String>>();
        for (String id : d.legalTechIds()) options.add(new Option<>(tech(id).name() + " - " + tech(id).detail(), id));
        return choose(text("HOTSEAT_RESEARCH"), options);
    }
    @Override public CompletableFuture<Boolean> colonize(PendingColonizationDecision d) {
        return yesNo(text("HOTSEAT_COLONIZE", systemName(d.empireId(), d.systemId())),
                "HOTSEAT_COLONIZE_YES", "HOTSEAT_SKIP");
    }
    @Override public CompletableFuture<Boolean> diplomacy(PendingDiplomacyDecision d) {
        return yesNo(diplomacyDescription(game, d.notice()), "HOTSEAT_ACCEPT", "HOTSEAT_REJECT");
    }
    @Override public CompletableFuture<InProcessBombardmentDecisionAdapter.Choice> bombardment(BombardmentDecision d) {
        var options = new ArrayList<Option<InProcessBombardmentDecisionAdapter.Choice>>();
        options.add(new Option<>(text("HOTSEAT_SKIP"), InProcessBombardmentDecisionAdapter.Choice.SKIP));
        options.add(new Option<>(text("HOTSEAT_BOMBARD"), InProcessBombardmentDecisionAdapter.Choice.BOMBARD));
        if (d.targetAllowed()) options.add(new Option<>(text("HOTSEAT_TARGET_BOMBARD"), InProcessBombardmentDecisionAdapter.Choice.TARGET_BOMBARD));
        return choose(text("HOTSEAT_BOMBARDMENT") + ": " + systemName(d.attackerEmpireId(), d.systemId()), options);
    }
    @Override public CompletableFuture<InProcessEspionageDecisionAdapter.Choice> espionage(EspionageDecision d) {
        requireEdt();
        var future = new CompletableFuture<InProcessEspionageDecisionAdapter.Choice>();
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
        if (game.hotSeatState() != null) {
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
            panel.add(footer, BorderLayout.SOUTH);
        }
        return panel;
    }
    private String empireName(int id) { return game.galaxy().empire(id).name(); }
    private String systemName(int empire, int id) { return game.galaxy().empire(empire).sv.name(id); }
    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Event thread required");
    }
}
