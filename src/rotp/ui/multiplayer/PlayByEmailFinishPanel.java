package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import rotp.multiplayer.pbem.StandingOrders;
import rotp.multiplayer.pbem.StandingOrders.*;
import rotp.ui.BasePanel;
import rotp.ui.game.GameUI;

/**
 * Review standing orders, then send the turn. Options are buttons, not dropdowns:
 * a dropdown's list opens beneath the cover this screen is drawn on.
 */
public final class PlayByEmailFinishPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final Options<Bombard> bombard;
    private final Options<Frame> frame;
    private final Options<SabotageTarget> sabotage;
    private final JButton send;

    /** One question's options; exactly one is selected and drawn highlighted. */
    private final class Options<E extends Enum<E>> {
        private final List<JButton> buttons = new ArrayList<>();
        private final E[] values;
        private E selected;
        Options(E[] values, String prefix, E initial) {
            this.values = values;
            for (E value : values) {
                JButton button = HotSeatStyle.button(new JButton(text(prefix + value.name())), 18);
                button.addActionListener(e -> select(value));
                buttons.add(button);
            }
            select(initial);
        }
        void select(E value) {
            selected = value;
            for (int i = 0; i < values.length; i++)
                HotSeatStyle.selected(buttons.get(i), values[i] == value);
        }
        JPanel row() {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            row.setOpaque(false);
            buttons.forEach(row::add);
            return row;
        }
    }

    public PlayByEmailFinishPanel(String playerName, StandingOrders current,
            Consumer<StandingOrders> onSend, Runnable onBack) {
        setLayout(new BorderLayout(15, 25));
        setBorder(BorderFactory.createEmptyBorder(50, 80, 40, 80));
        add(HotSeatStyle.title(text("PBEM_FINISH_TITLE", playerName)), BorderLayout.NORTH);

        bombard = new Options<>(Bombard.values(), "PBEM_BOMBARD_", current.bombard());
        frame = new Options<>(Frame.values(), "PBEM_FRAME_", current.frame());
        sabotage = new Options<>(SabotageTarget.values(), "PBEM_SABOTAGE_", current.sabotage());
        JPanel questions = new JPanel(new GridBagLayout());
        questions.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 8, 0);
        JTextArea detail = new JTextArea(text("PBEM_FINISH_DETAIL"), 0, 50);
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        HotSeatStyle.body(detail, 20);
        c.insets = new Insets(0, 0, 24, 0);
        questions.add(detail, c);
        addQuestion(questions, c, "PBEM_BOMBARD", bombard.row());
        addQuestion(questions, c, "PBEM_FRAME", frame.row());
        addQuestion(questions, c, "PBEM_SABOTAGE", sabotage.row());
        // Centered at natural size instead of stretched to fill the screen.
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(questions);
        add(center, BorderLayout.CENTER);

        send = HotSeatStyle.button(new JButton(text("PBEM_SEND")), 22);
        JButton back = HotSeatStyle.button(new JButton(text("PBEM_BACK")), 22);
        send.addActionListener(e -> {
            send.setEnabled(false);
            back.setEnabled(false);
            onSend.accept(orders());
        });
        back.addActionListener(e -> {
            send.setEnabled(false);
            back.setEnabled(false);
            onBack.run();
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        footer.setOpaque(false);
        footer.add(back);
        footer.add(send);
        add(footer, BorderLayout.SOUTH);
    }

    private void addQuestion(JPanel questions, GridBagConstraints c, String key, Component row) {
        JLabel label = HotSeatStyle.body(text(key), 22);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        c.insets = new Insets(0, 0, 6, 0);
        questions.add(label, c);
        c.insets = new Insets(0, 0, 22, 0);
        questions.add(row, c);
    }

    public StandingOrders orders() {
        return new StandingOrders(bombard.selected, frame.selected, sabotage.selected);
    }
    @Override public void paintComponent(Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
    /** Selects orders and sends, as a player would by clicking. */
    public void send(StandingOrders orders) {
        bombard.select(orders.bombard());
        frame.select(orders.frame());
        sabotage.select(orders.sabotage());
        if (send.isEnabled()) send.doClick(0);
    }
}
