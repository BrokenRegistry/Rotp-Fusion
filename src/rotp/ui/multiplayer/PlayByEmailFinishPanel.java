package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.*;
import rotp.multiplayer.pbem.StandingOrders;
import rotp.multiplayer.pbem.StandingOrders.*;
import rotp.ui.BasePanel;

/** Review standing orders, then send the turn. */
public final class PlayByEmailFinishPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final JComboBox<Choice<Bombard>> bombard;
    private final JComboBox<Choice<Frame>> frame;
    private final JComboBox<Choice<SabotageTarget>> sabotage;
    private final JButton send;

    /** Shows the translated label while keeping the enum value. */
    private record Choice<E extends Enum<E>>(E value, String label) {
        @Override public String toString() { return label; }
    }

    public PlayByEmailFinishPanel(String playerName, StandingOrders current,
            Consumer<StandingOrders> onSend, Runnable onBack) {
        setLayout(new BorderLayout(15, 25));
        setBorder(BorderFactory.createEmptyBorder(60, 120, 50, 120));
        add(HotSeatStyle.title(text("PBEM_FINISH_TITLE", playerName)), BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(10, 20));
        center.setOpaque(false);
        JTextArea detail = new JTextArea(text("PBEM_FINISH_DETAIL"));
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        HotSeatStyle.body(detail, 20);
        center.add(detail, BorderLayout.NORTH);
        JPanel rows = new JPanel(new GridLayout(0, 2, 16, 12));
        rows.setOpaque(false);
        bombard = combo(Bombard.values(), "PBEM_BOMBARD_", current.bombard());
        frame = combo(Frame.values(), "PBEM_FRAME_", current.frame());
        sabotage = combo(SabotageTarget.values(), "PBEM_SABOTAGE_", current.sabotage());
        rows.add(HotSeatStyle.body(text("PBEM_BOMBARD"), 20));
        rows.add(bombard);
        rows.add(HotSeatStyle.body(text("PBEM_FRAME"), 20));
        rows.add(frame);
        rows.add(HotSeatStyle.body(text("PBEM_SABOTAGE"), 20));
        rows.add(sabotage);
        center.add(rows, BorderLayout.CENTER);
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

    private <E extends Enum<E>> JComboBox<Choice<E>> combo(E[] values, String prefix, E selected) {
        JComboBox<Choice<E>> box = new JComboBox<>();
        for (E value : values) {
            var choice = new Choice<>(value, text(prefix + value.name()));
            box.addItem(choice);
            if (value == selected) box.setSelectedItem(choice);
        }
        return box;
    }
    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E value(JComboBox<Choice<E>> box) {
        return ((Choice<E>) box.getSelectedItem()).value();
    }
    public StandingOrders orders() { return new StandingOrders(value(bombard), value(frame), value(sabotage)); }
    @Override public void paintComponent(Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
    /** Selects orders and sends, as a player would. */
    public void send(StandingOrders orders) {
        select(bombard, orders.bombard());
        select(frame, orders.frame());
        select(sabotage, orders.sabotage());
        if (send.isEnabled()) send.doClick(0);
    }
    private static <E extends Enum<E>> void select(JComboBox<Choice<E>> box, E value) {
        for (int i = 0; i < box.getItemCount(); i++)
            if (box.getItemAt(i).value() == value) box.setSelectedIndex(i);
    }
}
