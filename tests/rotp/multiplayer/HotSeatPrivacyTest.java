package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JButton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.Rotp;
import rotp.model.empires.Empire;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.HotSeatState;
import rotp.ui.multiplayer.HotSeatDesktop;
import rotp.util.Rand;

@EnabledIfSystemProperty(named="rotp.integration", matches="true")
class HotSeatPrivacyTest {
    @TempDir static Path directory;
    @BeforeAll static void init() throws Exception { HotSeatTestFixture.initialize(directory); }

    @Test void activationChangesOnlyViewerNotOwnershipAiOrRandomStream() {
        GameSession game = HotSeatTestFixture.start(2, 0, 1);
        int ai0 = game.galaxy().empire(0).selectedAI;
        int ai1 = game.galaxy().empire(1).selectedAI;
        long expected = new Rand(941).nextLong();
        Rotp.rand(new Rand(941));
        game.galaxy().activateHotSeatViewer(1);
        assertEquals(1, game.galaxy().player().id);
        assertEquals(1, Empire.PLAYER_ID);
        assertEquals(ai0, game.galaxy().empire(0).selectedAI);
        assertEquals(ai1, game.galaxy().empire(1).selectedAI);
        assertFalse(game.galaxy().empire(0).isAIControlled());
        assertFalse(game.galaxy().empire(1).isAIControlled());
        assertTrue(game.galaxy().empire(2).isAIControlled());
        assertEquals(expected, Rotp.rand().nextLong());
        assertThrows(IllegalArgumentException.class, () -> game.galaxy().activateHotSeatViewer(2));
        assertEquals(1, game.galaxy().player().id);
    }

    @Test void handoffConsumesShortcutsAndRequiresAFreshKeyPress() {
        HotSeatTestFixture.start(2, 0, 1);
        HotSeatTestFixture.onEdt(() -> {
            var desktop = new HotSeatDesktop();
            try {
                var state = new HotSeatState(List.of("human-0", "human-1"), 1);
                var accepted = new AtomicInteger();
                // A key held from a preceding action cannot acknowledge a new screen.
                desktop.dispatchKeyEvent(key(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER));
                desktop.cover(state.snapshot(), accepted::incrementAndGet);
                assertTrue(desktop.isCovered());
                assertTrue(desktop.dispatchKeyEvent(key(KeyEvent.KEY_PRESSED, KeyEvent.VK_ESCAPE)));
                assertTrue(desktop.dispatchKeyEvent(key(KeyEvent.KEY_PRESSED, KeyEvent.VK_F1)));
                desktop.dispatchKeyEvent(key(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER));
                assertEquals(0, accepted.get());
                desktop.dispatchKeyEvent(key(KeyEvent.KEY_RELEASED, KeyEvent.VK_ENTER));
                desktop.dispatchKeyEvent(key(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER));
                assertEquals(1, accepted.get());
                desktop.dispatchKeyEvent(key(KeyEvent.KEY_PRESSED, KeyEvent.VK_ENTER));
                assertEquals(1, accepted.get());
            }
            finally { desktop.close(); }
        });
    }

    @Test void oldHandoffButtonCannotAcknowledgeNewOwnerAndUiSelectionIsCleared() {
        GameSession game = HotSeatTestFixture.start(2, 0, 1);
        HotSeatTestFixture.onEdt(() -> {
            var desktop = new HotSeatDesktop();
            try {
                var state = new HotSeatState(List.of("human-0", "human-1"), 1);
                var accepted = new AtomicInteger();
                desktop.cover(state.snapshot(), accepted::incrementAndGet);
                JButton previous = button((Container) Rotp.getFrame().getGlassPane());
                desktop.cover(state.snapshot(), accepted::incrementAndGet);
                previous.doClick();
                assertEquals(0, accepted.get());
                GameSession.var("MAINUI_SELECTED_SYSTEM", game.galaxy().empire(0).allColonizedSystems().get(0));
                GameSession.var("MAINUI_LAST_HOVERING_SPRITE", "private");
                desktop.activateViewer(game, "human-1");
                assertNotEquals("private", GameSession.var("MAINUI_LAST_HOVERING_SPRITE"));
                Object selected = GameSession.var("MAINUI_SELECTED_SYSTEM");
                assertTrue(selected == null || selected == game.galaxy().empire(1).allColonizedSystems().get(0));
                assertTrue(desktop.isCovered(), "Activating a viewer does not acknowledge the cover");
            }
            finally { desktop.close(); }
        });
    }

    private static KeyEvent key(int type, int code) {
        return new KeyEvent(Rotp.getFrame(), type, System.currentTimeMillis(), 0, code, KeyEvent.CHAR_UNDEFINED);
    }
    @Test void blankCoverCapturesMouseDragAndWheelBeforeTheUnderlyingMap() {
        HotSeatTestFixture.onEdt(() -> {
            var frame = Rotp.getFrame();
            var original = frame.getContentPane();
            var underneath = new javax.swing.JPanel();
            AtomicInteger received = new AtomicInteger();
            var listener = new java.awt.event.MouseAdapter() {
                @Override public void mousePressed(java.awt.event.MouseEvent e) { received.incrementAndGet(); }
                @Override public void mouseDragged(java.awt.event.MouseEvent e) { received.incrementAndGet(); }
                @Override public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) { received.incrementAndGet(); }
            };
            underneath.addMouseListener(listener);
            underneath.addMouseMotionListener(listener);
            underneath.addMouseWheelListener(listener);
            frame.setContentPane(underneath);
            frame.setVisible(true);
            frame.validate();
            sendMouse(frame);
            assertTrue(received.get() > 0, "Exercise actual lightweight mouse dispatch");
            try (var desktop = new HotSeatDesktop()) {
                desktop.cover(new HotSeatState(List.of("human-0", "human-1"), 1).snapshot(), () -> {});
                frame.validate();
                received.set(0);
                sendMouse(frame);
                assertEquals(0, received.get(), "Blank privacy area must capture all mouse input");
            } finally {
                frame.setVisible(false);
                frame.setContentPane(original);
            }
        });
    }
    private static void sendMouse(javax.swing.JFrame frame) {
        long now = System.currentTimeMillis();
        for (int id : new int[] {java.awt.event.MouseEvent.MOUSE_PRESSED,
                java.awt.event.MouseEvent.MOUSE_DRAGGED, java.awt.event.MouseEvent.MOUSE_RELEASED})
            frame.dispatchEvent(new java.awt.event.MouseEvent(frame, id, now,
                    java.awt.event.InputEvent.BUTTON1_DOWN_MASK, 15, 80, 1, false, java.awt.event.MouseEvent.BUTTON1));
        frame.dispatchEvent(new java.awt.event.MouseWheelEvent(frame, java.awt.event.MouseEvent.MOUSE_WHEEL,
                now, 0, 15, 80, 0, false, java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, 1));
    }
    @Test void eachPlayerKeepsTheirOwnMapPositionAcrossHandoffs() {
        GameSession game = HotSeatTestFixture.start(2, 0, 1);
        HotSeatTestFixture.onEdt(() -> {
            var desktop = new HotSeatDesktop();
            try {
                var state = new HotSeatState(List.of("human-0", "human-1"), 1);
                desktop.cover(state.snapshot(), () -> {});
                desktop.activateViewer(game, "human-0");
                var map = rotp.ui.RotPUI.instance().mainUI().map();
                map.centerX(7); map.centerY(9); map.setScale(12);
                float scale = map.scaleY();
                desktop.activateViewer(game, "human-1");
                map = rotp.ui.RotPUI.instance().mainUI().map();
                map.centerX(3); map.centerY(4);
                desktop.activateViewer(game, "human-0");
                map = rotp.ui.RotPUI.instance().mainUI().map();
                assertEquals(7, coordinate(map, "centerX"));
                assertEquals(9, coordinate(map, "centerY"));
                assertEquals(scale, map.scaleY());
                desktop.activateViewer(game, "human-1");
                assertEquals(3, coordinate(rotp.ui.RotPUI.instance().mainUI().map(), "centerX"));
                assertEquals(4, coordinate(rotp.ui.RotPUI.instance().mainUI().map(), "centerY"));
            }
            finally { desktop.close(); }
        });
    }
    private static JButton button(Container root) {
        for (Component child : root.getComponents()) {
            if (child instanceof JButton b) return b;
            if (child instanceof Container nested) {
                JButton found = button(nested);
                if (found != null) return found;
            }
        }
        return null;
    }
    private static float coordinate(rotp.ui.main.GalaxyMapPanel map, String name) {
        try {
            var method = map.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            return (float) method.invoke(map);
        }
        catch (Exception failure) { throw new AssertionError(failure); }
    }
}
