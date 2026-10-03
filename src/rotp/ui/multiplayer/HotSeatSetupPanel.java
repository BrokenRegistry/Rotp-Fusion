package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
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
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        setBackground(new Color(30, 34, 42));
        JLabel heading = new JLabel(text("HOTSEAT_SETUP_TITLE"));
        heading.setForeground(Color.WHITE);
        heading.setFont(heading.getFont().deriveFont(24f));
        add(heading, BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(0, 3, 12, 8));
        rows.add(new JLabel(text("HOTSEAT_EMPIRE")));
        rows.add(new JLabel(text("HOTSEAT_HUMAN")));
        rows.add(new JLabel(text("HOTSEAT_PLAYER_NAME")));
        for (int i = 0; i < empireNames.size(); i++) {
            final int empire = i;
            Assignment assignment = previous == null ? null : previous.humans().stream()
                    .filter(h -> h.empireId() == empire).findFirst().orElse(null);
            JCheckBox human = new JCheckBox(text("HOTSEAT_HUMAN"),
                    assignment != null || previous == null && i < 2);
            JTextField name = new JTextField(assignment == null
                    ? text("HOTSEAT_DEFAULT_NAME", i + 1) : assignment.displayName());
            name.setEnabled(human.isSelected());
            human.addActionListener(e -> name.setEnabled(human.isSelected()));
            humans.add(human);
            names.add(name);
            rows.add(new JLabel(empireNames.get(i)));
            rows.add(human);
            rows.add(name);
        }
        JPanel list = new JPanel(new BorderLayout());
        list.add(rows, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        JLabel error = new JLabel(" ");
        error.setForeground(new Color(170, 20, 20));
        footer.add(error, BorderLayout.NORTH);
        JPanel buttons = new JPanel();
        JButton use = new JButton(text("HOTSEAT_USE"));
        JButton byEmail = new JButton(text("PBEM_USE"));
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
        JButton single = new JButton(text("HOTSEAT_SINGLE_PLAYER"));
        single.addActionListener(e -> accept.accept(null, false));
        JButton back = new JButton(text("HOTSEAT_CANCEL"));
        back.addActionListener(e -> cancel.run());
        buttons.add(use);
        buttons.add(byEmail);
        buttons.add(single);
        buttons.add(back);
        footer.add(buttons, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
    }
}
