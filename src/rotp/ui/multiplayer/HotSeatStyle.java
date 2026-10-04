package rotp.ui.multiplayer;

import rotp.ui.BasePanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.text.JTextComponent;
import rotp.ui.game.GameUI;
import rotp.ui.main.GalaxyMapPanel;
import rotp.util.Base;

/** The game's look for hot-seat screens that have no single-player equivalent. */
public final class HotSeatStyle implements Base {
    private static final HotSeatStyle BASE = new HotSeatStyle();
    static final Color BODY = new Color(225, 225, 255);
    private HotSeatStyle() { }

    /** Star field under a dark wash, as behind the game's menus. */
    public static void paintBackdrop(Graphics g0, JComponent c) {
        Graphics2D g = (Graphics2D) g0;
        int w = c.getWidth(), h = c.getHeight();
        g.setColor(Color.black);
        g.fillRect(0, 0, w, h);
        if (GalaxyMapPanel.sharedStarBackground != null)
            g.drawImage(GalaxyMapPanel.sharedStarBackground, 0, 0, w, h, null);
        g.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 90), 0, h, new Color(0, 0, 0, 200)));
        g.fillRect(0, 0, w, h);
    }
    public static JLabel title(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(BASE.narrowFont(36));
        label.setForeground(GameUI.titleColor());
        return label;
    }
    public static JLabel body(String text, int size) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(BASE.narrowFont(size));
        label.setForeground(BODY);
        return label;
    }
    public static void body(JTextComponent text, int size) {
        text.setFont(BASE.narrowFont(size));
        text.setForeground(BODY);
        text.setOpaque(false);
        text.setBorder(BorderFactory.createEmptyBorder());
    }
    /** Client property: an option button drawn as the current choice. */
    public static final String SELECTED = "rotp.hotseat.selected";

    /** A JButton drawn like the game's own buttons (see BaseModPanel.drawButton). */
    public static <B extends AbstractButton> B button(B button, int size) {
        button.setUI(new GameButtonUI());
        button.setFont(BASE.narrowFont(size));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 22, 6, 22));
        return button;
    }
    public static void selected(AbstractButton button, boolean on) {
        button.putClientProperty(SELECTED, on);
        button.repaint();
    }

    /**
     * Drawn as SetupGalaxyUI's player button: gradient fill, rounded bright outline and
     * shadowed label, yellow on hover. A selected option keeps a thicker outline.
     */
    private static final class GameButtonUI extends BasicButtonUI {
        @Override public void paint(Graphics g0, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g = (Graphics2D) g0.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                boolean on = Boolean.TRUE.equals(b.getClientProperty(SELECTED));
                boolean hover = b.isEnabled() && b.getModel().isRollover();
                int cnr = BasePanel.s5;
                int w = c.getWidth(), h = c.getHeight();
                g.setPaint(GameUI.buttonBackground(0, w));
                g.fillRoundRect(0, 0, w - 1, h - 1, cnr, cnr);
                Color fore = !b.isEnabled() ? GameUI.borderDarkColor()
                        : hover ? Color.yellow : GameUI.borderBrightColor();
                g.setColor(fore);
                if (on) {
                    g.setStroke(BasePanel.stroke3);
                    g.drawRoundRect(1, 1, w - 3, h - 3, cnr, cnr);
                } else {
                    g.setStroke(BasePanel.stroke1);
                    g.drawRoundRect(0, 0, w - 1, h - 1, cnr, cnr);
                }
                g.setFont(b.getFont());
                FontMetrics fm = g.getFontMetrics();
                String label = b.getText();
                java.awt.Insets margin = b.getInsets();
                int x = Math.max((w - fm.stringWidth(label)) / 2, margin.left);
                int y = (h - margin.top - margin.bottom) * 75 / 100 + margin.top;
                BASE.drawShadowedString(g, label, 2, x, y, GameUI.borderDarkColor(), fore);
            } finally { g.dispose(); }
        }
    }
}
