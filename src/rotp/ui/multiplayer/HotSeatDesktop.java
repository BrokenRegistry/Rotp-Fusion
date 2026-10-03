package rotp.ui.multiplayer;

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import rotp.Rotp;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.HotSeatSnapshot;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;

/** Owns the shared desktop's privacy boundary. All operations run on the EDT. */
public final class HotSeatDesktop implements KeyEventDispatcher, AutoCloseable {
    private static HotSeatDesktop current;
    private static long viewerGeneration;
    public static long viewerGeneration() { return viewerGeneration; }
    private final Component previousGlass;
    private final Set<Integer> pressedKeys = new HashSet<>();
    private BasePanel pane;
    private boolean covered;
    private boolean closed;
    private long generation;
    private GameSession viewedSession;
    private Integer displayedEmpire;

    public HotSeatDesktop() {
        requireEdt();
        if (current != null) current.close();
        previousGlass = Rotp.getFrame().getGlassPane();
        current = this;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    }

    public static boolean blocksNavigation() { return current != null && current.isCovered(); }
    public boolean isCovered() { return covered && !closed; }

    public void cover(HotSeatSnapshot snapshot, Runnable confirm) {
        requireEdt();
        long ticket = ++generation;
        hideSecondaryWindows();
        var setup = GameSession.instance().hotSeatSetup();
        String owner = snapshot.ownerPlayerId();
        String name = setup == null ? owner : setup.displayName(owner);
        var byEmail = GameSession.instance().playByEmail();
        HotSeatPrivacyPane.PinGate gate = byEmail == null ? null : new HotSeatPrivacyPane.PinGate() {
            @Override public boolean hasPin() { return byEmail.hasPin(owner); }
            @Override public boolean matches(String pin) { return byEmail.pinMatches(owner, pin); }
            @Override public void set(String pin) { byEmail.setPin(owner, pin); }
        };
        pane = new HotSeatPrivacyPane(name, gate, () -> {
            if (!closed && generation == ticket) confirm.run();
        });
        installPane(pane);
    }

    /** Decisions and reports use the same full-window privacy boundary. */
    public void showPrivatePanel(BasePanel panel) {
        requireEdt();
        ++generation;
        pane = panel;
        installPane(panel);
    }

    private void installPane(BasePanel panel) {
        if (closed) throw new IllegalStateException("Desktop session closed");
        covered = true;
        panel.setOpaque(true);
        // Opacity only covers pixels. Register all mouse masks so Swing does not
        // retarget events in blank glass areas to the underlying game controls.
        var blockMouse = new java.awt.event.MouseAdapter() {
            @Override public void mousePressed(java.awt.event.MouseEvent e) { e.consume(); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { e.consume(); }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { e.consume(); }
            @Override public void mouseMoved(java.awt.event.MouseEvent e) { e.consume(); }
            @Override public void mouseDragged(java.awt.event.MouseEvent e) { e.consume(); }
            @Override public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) { e.consume(); }
        };
        panel.addMouseListener(blockMouse);
        panel.addMouseMotionListener(blockMouse);
        panel.addMouseWheelListener(blockMouse);
        Rotp.getFrame().setGlassPane(panel);
        panel.setVisible(true);
        panel.requestFocusInWindow();
        Rotp.getFrame().revalidate();
        Rotp.getFrame().repaint();
    }

    public void activateViewer(GameSession session, String playerId) {
        requireEdt();
        if (!isCovered()) throw new IllegalStateException("Cover the display before changing viewers");
        var seat = session.controllerRegistry().seats().stream()
                .filter(s -> s.playerId().equals(playerId)).findFirst().orElseThrow();
        if (viewedSession == session && displayedEmpire != null)
            session.hotSeatView(displayedEmpire, RotPUI.instance().mainUI().captureHotSeatView());
        session.galaxy().activateHotSeatViewer(seat.empireId());
        ++viewerGeneration;
        clearPrivateUi();
        RotPUI.instance().resetHotSeatPanels();
        RotPUI.instance().mainUI().restoreHotSeatView(session.hotSeatView(seat.empireId()));
        viewedSession = session;
        displayedEmpire = seat.empireId();
    }
    public void captureView(GameSession session) {
        requireEdt();
        if (session == viewedSession && displayedEmpire != null)
            session.hotSeatView(displayedEmpire, RotPUI.instance().mainUI().captureHotSeatView());
    }

    public void clearPrivateUi() {
        requireEdt();
        hideSecondaryWindows();
        GameSession.clearViewingState();
    }

    public void revealPlanning() {
        requireEdt();
        ++generation;
        RotPUI.instance().selectMainPanel();
        covered = false;
        if (pane != null) pane.setVisible(false);
        RotPUI.instance().requestFocusInWindow();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (closed) return false;
        boolean fresh = false;
        if (event.getID() == KeyEvent.KEY_PRESSED) fresh = pressedKeys.add(event.getKeyCode());
        if (event.getID() == KeyEvent.KEY_RELEASED) pressedKeys.remove(event.getKeyCode());
        if (!covered) return false;
        if (pane instanceof HotSeatPrivacyPane handoff && !handoff.asksForPin()) {
            if (fresh && event.getID() == KeyEvent.KEY_PRESSED && event.getKeyCode() == KeyEvent.VK_ENTER)
                handoff.acknowledge();
            event.consume();
            return true;
        }
        // A covered dialog may edit its own controls, never dispatch to the old map.
        if (event.getSource() instanceof Component source && SwingUtilities.isDescendingFrom(source, pane))
            return false;
        event.consume();
        return true;
    }

    private static void hideSecondaryWindows() {
        for (Window window : Window.getWindows())
            if (window != Rotp.getFrame() && window.isVisible()) window.setVisible(false);
    }

    @Override public void close() {
        requireEdt();
        if (closed) return;
        closed = true;
        covered = false;
        generation++;
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(this);
        if (Rotp.getFrame().getGlassPane() == pane) {
            pane.setVisible(false);
            Rotp.getFrame().setGlassPane(previousGlass);
            previousGlass.setVisible(false);
        }
        if (current == this) current = null;
    }

    private static void requireEdt() {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Swing operation outside EDT");
    }
}
