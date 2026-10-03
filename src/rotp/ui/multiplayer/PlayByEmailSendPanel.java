package rotp.ui.multiplayer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import javax.swing.*;
import rotp.ui.BasePanel;

/** Shown after a turn file is written for someone else; never reveals the map. */
public final class PlayByEmailSendPanel extends BasePanel {
    private static final long serialVersionUID = 1L;
    private final JButton menu;
    private final JButton here;
    public PlayByEmailSendPanel(String playerName, String fileName, Runnable toMenu, Runnable playerHere) {
        setLayout(new BorderLayout(15, 25));
        setBorder(BorderFactory.createEmptyBorder(80, 120, 60, 120));
        add(HotSeatStyle.title(text("PBEM_SEND_TITLE", playerName)), BorderLayout.NORTH);
        JTextArea detail = new JTextArea(text("PBEM_SEND_DETAIL", fileName, playerName));
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);
        HotSeatStyle.body(detail, 22);
        add(detail, BorderLayout.CENTER);
        menu = HotSeatStyle.button(new JButton(text("PBEM_RETURN_MENU")), 22);
        here = HotSeatStyle.button(new JButton(text("PBEM_HERE", playerName)), 22);
        menu.addActionListener(e -> { lockButtons(); toMenu.run(); });
        here.addActionListener(e -> { lockButtons(); playerHere.run(); });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        footer.setOpaque(false);
        footer.add(menu);
        footer.add(here);
        add(footer, BorderLayout.SOUTH);
    }
    private void lockButtons() { menu.setEnabled(false); here.setEnabled(false); }
    @Override public void paintComponent(Graphics g) { HotSeatStyle.paintBackdrop(g, this); }
    public void returnToMenu() { if (menu.isEnabled()) menu.doClick(0); }
    public void playerIsHere() { if (here.isEnabled()) here.doClick(0); }
}
