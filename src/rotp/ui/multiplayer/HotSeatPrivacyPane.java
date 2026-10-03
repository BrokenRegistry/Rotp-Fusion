package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import rotp.multiplayer.pbem.PinHash;
import rotp.ui.BasePanel;

/** Opaque screen with a single-use acknowledgement, never a transparent overlay. */
public final class HotSeatPrivacyPane extends BasePanel {
    private static final long serialVersionUID = 1L;

    /** A player's PIN in a play-by-email match. */
    public interface PinGate {
        boolean hasPin();
        boolean matches(String pin);
        void set(String pin);
    }

    private final JButton proceed;
    private final JPasswordField pin;
    private final JPasswordField repeat;
    private final JLabel error = new JLabel(" ");
    private boolean accepted;

    public HotSeatPrivacyPane(String playerName, Runnable confirm) {
        this(playerName, null, confirm);
    }

    public HotSeatPrivacyPane(String playerName, PinGate gate, Runnable confirm) {
        setOpaque(true);
        setLayout(new GridBagLayout());
        JPanel content = new JPanel(new BorderLayout(20, 28));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        boolean choosing = gate != null && !gate.hasPin();
        String title = gate == null ? text("HOTSEAT_HANDOFF", playerName)
                : text(choosing ? "PBEM_CHOOSE_PIN" : "PBEM_ENTER_PIN", playerName);
        content.add(HotSeatStyle.title(title), BorderLayout.NORTH);
        proceed = new JButton(text("HOTSEAT_CONTINUE")) {
            private static final long serialVersionUID = 1L;
            @Override protected void processMouseEvent(MouseEvent event) {
                if (event.getClickCount() > 1) { event.consume(); return; }
                super.processMouseEvent(event);
            }
        };
        HotSeatStyle.button(proceed, 24);
        if (gate == null) {
            pin = repeat = null;
            content.add(HotSeatStyle.body(text("HOTSEAT_HANDOFF_DETAIL"), 20), BorderLayout.CENTER);
        } else {
            pin = pinField();
            repeat = choosing ? pinField() : null;
            JPanel fields = new JPanel(new GridLayout(0, 1, 8, 8));
            fields.setOpaque(false);
            if (choosing) fields.add(HotSeatStyle.body(text("PBEM_CHOOSE_PIN_DETAIL"), 20));
            fields.add(HotSeatStyle.body(text("PBEM_PIN"), 20));
            fields.add(pin);
            if (repeat != null) {
                fields.add(HotSeatStyle.body(text("PBEM_CONFIRM_PIN"), 20));
                fields.add(repeat);
            }
            error.setForeground(new Color(255, 120, 120));
            error.setFont(narrowFont(20));
            error.setHorizontalAlignment(JLabel.CENTER);
            fields.add(error);
            // Natural width, not the title's: a PIN is at most 12 characters.
            JPanel center = new JPanel(new GridBagLayout());
            center.setOpaque(false);
            center.add(fields);
            content.add(center, BorderLayout.CENTER);
            pin.addActionListener(e -> proceed.doClick(0));
            if (repeat != null) repeat.addActionListener(e -> proceed.doClick(0));
        }
        proceed.addActionListener(event -> {
            if (accepted || !unlocked(gate)) return;
            accepted = true;
            proceed.setEnabled(false);
            confirm.run();
        });
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.add(proceed);
        content.add(footer, BorderLayout.SOUTH);
        add(content);
    }

    private JPasswordField pinField() {
        JPasswordField field = new JPasswordField(10);
        field.setFont(narrowFont(26));
        field.setHorizontalAlignment(JPasswordField.CENTER);
        return field;
    }

    private boolean unlocked(PinGate gate) {
        if (gate == null) return true;
        String entered = new String(pin.getPassword());
        if (repeat != null) {
            if (!PinHash.acceptable(entered)) return fail("PBEM_PIN_INVALID");
            if (!entered.equals(new String(repeat.getPassword()))) return fail("PBEM_PIN_MISMATCH");
            gate.set(entered);
            return true;
        }
        if (gate.matches(entered)) return true;
        pin.setText("");
        return fail("PBEM_WRONG_PIN");
    }
    private boolean fail(String key) {
        error.setText(text(key));
        return false;
    }

    /** True when this handoff needs typed input, so keys must reach its fields. */
    public boolean asksForPin() { return pin != null; }
    @Override public boolean requestFocusInWindow() {
        return pin != null ? pin.requestFocusInWindow() : super.requestFocusInWindow();
    }
    @Override public void paintComponent(java.awt.Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
    public void acknowledge() { proceed.doClick(0); }
    /** Types a PIN (twice when choosing one) and continues. */
    public void enterPin(String value) {
        pin.setText(value);
        if (repeat != null) repeat.setText(value);
        proceed.doClick(0);
    }
    public String errorText() { return error.getText(); }
}
