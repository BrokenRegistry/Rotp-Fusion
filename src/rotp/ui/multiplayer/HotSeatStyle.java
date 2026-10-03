package rotp.ui.multiplayer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    /** A plain JButton drawn like the game's buttons. */
    public static <B extends AbstractButton> B button(B button, int size) {
        button.setUI(new BasicButtonUI());
        button.setFont(BASE.narrowFont(size));
        button.setForeground(GameUI.buttonTextColor());
        button.setBackground(GameUI.buttonBackgroundColor());
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GameUI.borderBrightColor(), 2),
                BorderFactory.createEmptyBorder(8, 28, 8, 28)));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (button.isEnabled()) button.setForeground(GameUI.textHoverColor()); }
            @Override public void mouseExited(MouseEvent e) { button.setForeground(GameUI.buttonTextColor()); }
        });
        return button;
    }
}
