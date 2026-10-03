package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.*;
import rotp.multiplayer.hotseat.HotSeatSetup;
import rotp.multiplayer.hotseat.HotSeatSetup.Assignment;
import rotp.ui.BasePanel;

/** Edits a draft roster; Use Hot Seat, Use Play by Email or Single Player commits it. */
public final class HotSeatSetupPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final List<JCheckBox> humans = new ArrayList<>();
    private final List<JTextField> names = new ArrayList<>();

    public HotSeatSetupPanel(List<String> empireNames, HotSeatSetup previous,
            BiConsumer<HotSeatSetup, Boolean> accept, Runnable cancel) {
        setLayout(new BorderLayout(15, 25));
        setBorder(BorderFactory.createEmptyBorder(50, 80, 40, 80));
        add(HotSeatStyle.title(text("HOTSEAT_SETUP_TITLE")), BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridBagLayout());
        rows.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 12, 4, 12);
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        for (String key : List.of("HOTSEAT_EMPIRE", "HOTSEAT_HUMAN", "HOTSEAT_PLAYER_NAME"))
            rows.add(heading(text(key)), c);
        for (int i = 0; i < empireNames.size(); i++) {
            final int empire = i;
            Assignment assignment = previous == null ? null : previous.humans().stream()
                    .filter(h -> h.empireId() == empire).findFirst().orElse(null);
            JCheckBox human = new JCheckBox(text("HOTSEAT_HUMAN"),
                    assignment != null || previous == null && i < 2);
            human.setOpaque(false);
            human.setFont(narrowFont(20));
            human.setForeground(HotSeatStyle.BODY);
            JTextField name = new JTextField(assignment == null
                    ? text("HOTSEAT_DEFAULT_NAME", i + 1) : assignment.displayName(), 16);
            name.setFont(narrowFont(20));
            name.setEnabled(human.isSelected());
            human.addActionListener(e -> name.setEnabled(human.isSelected()));
            humans.add(human);
            names.add(name);
            c.gridy = i + 1;
            rows.add(HotSeatStyle.body(empireNames.get(i), 20), c);
            rows.add(human, c);
            rows.add(name, c);
        }
        JScrollPane scroll = new JScrollPane(rows);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        // Centered at natural size instead of stretched to fill the window.
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(scroll);
        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(0, 10));
        footer.setOpaque(false);
        JLabel error = HotSeatStyle.body(" ", 18);
        error.setForeground(new Color(255, 120, 120));
        footer.add(error, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttons.setOpaque(false);
        JButton use = HotSeatStyle.button(new JButton(text("HOTSEAT_USE")), 20);
        JButton byEmail = HotSeatStyle.button(new JButton(text("PBEM_USE")), 20);
        java.awt.event.ActionListener commit = e -> {
            try {
                var assignments = new ArrayList<Assignment>();
                for (int i = 0; i < humans.size(); i++) {
                    final int empire = i;
                    if (humans.get(i).isSelected()) {
                        String id = previous == null ? "human-" + i : previous.humans().stream()
                                .filter(h -> h.empireId() == empire).map(Assignment::playerId)
                                .findFirst().orElse("human-" + i);
                        assignments.add(new Assignment(id, names.get(i).getText(), i));
                    }
                }
                accept.accept(new HotSeatSetup(assignments), e.getSource() == byEmail);
            } catch (IllegalArgumentException invalid) {
                error.setText(text("HOTSEAT_INVALID_NAMES"));
            }
        };
        use.addActionListener(commit);
        byEmail.addActionListener(commit);
        JButton single = HotSeatStyle.button(new JButton(text("HOTSEAT_SINGLE_PLAYER")), 20);
        single.addActionListener(e -> accept.accept(null, false));
        JButton back = HotSeatStyle.button(new JButton(text("HOTSEAT_CANCEL")), 20);
        back.addActionListener(e -> cancel.run());
        buttons.add(use);
        buttons.add(byEmail);
        buttons.add(single);
        buttons.add(back);
        footer.add(buttons, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
    }

    private JLabel heading(String text) {
        JLabel label = HotSeatStyle.body(text, 22);
        label.setForeground(rotp.ui.game.GameUI.titleColor());
        return label;
    }
    @Override public void paintComponent(Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
}
