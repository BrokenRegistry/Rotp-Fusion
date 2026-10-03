package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.HotSeatReport;
import rotp.ui.RotPUI;
import rotp.ui.multiplayer.HotSeatReportReplay;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatReportReplayTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = {false, true})
    void diplomaticContinueDismissesReportWhilePlanningIsLocked(boolean mouse) throws Exception {
        var game = HotSeatTestFixture.start(1, 0, 1);
        game.startHotSeatGame(game.options(), new rotp.multiplayer.hotseat.HotSeatSetup(List.of(
                new rotp.multiplayer.hotseat.HotSeatSetup.Assignment("a", "Alice", 0),
                new rotp.multiplayer.hotseat.HotSeatSetup.Assignment("b", "Bob", 1))));
        var snapshot = game.hotSeatState().snapshot();
        game.hotSeatState().confirmHandoff(snapshot.revision());
        var notice = new rotp.multiplayer.turn.DiplomacyNotice("a", 0, 1, null,
                rotp.ui.diplomacy.DialogueManager.CONTACT_RUTHLESS, null, false);
        var report = new HotSeatReport(1, 1, 0, "DIPLOMACY", "Contact", List.of(), notice);
        var active = new AtomicBoolean(true);
        var done = new CountDownLatch(1);
        try {
            HotSeatTestFixture.onEdt(() -> HotSeatReportReplay.play(game, List.of(report), active::get, done::countDown));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            var ready = new AtomicBoolean();
            while (!ready.get() && System.nanoTime() < deadline) {
                HotSeatTestFixture.onEdt(() -> ready.set(GameSession.isSuspended()
                        && RotPUI.instance().selectedPanel() instanceof rotp.ui.tech.DiplomaticMessageUI));
                if (!ready.get()) Thread.sleep(10);
            }
            assertTrue(ready.get(), "Diplomatic report did not open");
            HotSeatTestFixture.onEdt(() -> {
                assertFalse(rotp.multiplayer.hotseat.HotSeatOrders.canEdit(0));
                var panel = (rotp.ui.tech.DiplomaticMessageUI) RotPUI.instance().selectedPanel();
                panel.endFade();
                try { HotSeatTestFixture.set(panel.getClass(), panel, "startTimeMs", 0L); }
                catch (Exception e) { throw new AssertionError(e); }
                if (mouse) {
                    panel.setSize(panel.scaled(1200), panel.scaled(800));
                    var image = new java.awt.image.BufferedImage(panel.getWidth(), panel.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
                    var graphics = image.createGraphics();
                    try { panel.paint(graphics); } finally { graphics.dispose(); }
                    try {
                        var boxes = panel.getClass().getDeclaredField("selectBoxes");
                        boxes.setAccessible(true);
                        var box = ((java.awt.Rectangle[]) boxes.get(panel))[0];
                        assertTrue(box.width > 0 && box.height > 0, "Continue must be painted");
                        int x = box.x + box.width / 2, y = box.y + box.height / 2;
                        panel.mouseMoved(new java.awt.event.MouseEvent(panel, java.awt.event.MouseEvent.MOUSE_MOVED,
                                System.currentTimeMillis(), 0, x, y, 0, false));
                        panel.mouseReleased(new java.awt.event.MouseEvent(panel, java.awt.event.MouseEvent.MOUSE_RELEASED,
                                System.currentTimeMillis(), 0, x, y, 1, false, java.awt.event.MouseEvent.BUTTON1));
                    } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
                } else panel.keyPressed(new KeyEvent(panel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                        0, KeyEvent.VK_1, '1'));
            });
            assertTrue(done.await(3, TimeUnit.SECONDS), "Continue must finish diplomatic report playback");
            assertFalse(GameSession.performingTurn());
            // An old notification must not release a different seat's pending turn.
            var stale = new rotp.ui.diplomacy.TurnNotificationMessage(
                    rotp.ui.diplomacy.DialogueManager.CONTACT_RUTHLESS);
            HotSeatTestFixture.onEdt(() -> {
                stale.init();
                var before = game.hotSeatState().snapshot();
                assertTrue(game.hotSeatState().finishPlanning(before.ownerPlayerId(), before.revision(),
                        java.util.Set.of("a", "b")));
                GameSession.performingTurn(true);
                game.pauseNextTurnProcessing("Test stale diplomatic acknowledgement");
                stale.select(0);
                assertTrue(GameSession.isSuspended(), "Stale Continue must not dismiss another seat's report");
                stale.escape();
                assertTrue(GameSession.isSuspended(), "Stale Escape must not dismiss another seat's report");
            });
        } finally {
            active.set(false);
            game.resumeNextTurnProcessing();
            done.await(2, TimeUnit.SECONDS);
            HotSeatTestFixture.onEdt(() -> {
                GameSession.performingTurn(false);
                RotPUI.instance().selectMainPanel();
            });
        }
    }

    @Test void nativeReportsHideProgressUntilDismissedAndRestorePlanning() throws Exception {
        var game = HotSeatTestFixture.start(1, 0, 1);
        var scouting = new HashMap<String, List<Integer>>();
        scouting.put("Scouts", List.of(game.galaxy().player().homeSysId()));
        scouting.put("Allies", List.of());
        scouting.put("Astronomers", List.of());
        var ships = new HashMap<Integer, Integer>();
        ships.put(game.galaxy().player().shipLab().scoutDesign().id(), 1);
        var reports = List.of(
                new HotSeatReport(1, 1, 0, "SCOUT", "Scouting", List.of(), scouting),
                new HotSeatReport(2, 1, 0, "SHIPS", "Construction", List.of(), ships));
        var active = new AtomicBoolean(true);
        var done = new CountDownLatch(1);
        RotPUI.drawNextTurnNotice = true;
        try {
            HotSeatTestFixture.onEdt(() -> HotSeatReportReplay.play(game, reports, active::get, done::countDown));
            for (String overlay : List.of("MapOverlaySystemsScouted", "MapOverlayShipsConstructed")) {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                var ready = new AtomicBoolean();
                while (!ready.get() && System.nanoTime() < deadline) {
                    HotSeatTestFixture.onEdt(() -> ready.set(GameSession.isSuspended()
                            && currentOverlay().equals(overlay)));
                    if (!ready.get()) Thread.sleep(10);
                }
                assertTrue(ready.get(), "Report did not open: " + overlay);
                HotSeatTestFixture.onEdt(() -> {
                    assertTrue(GameSession.performingTurn(), "Planning must stay locked during reports");
                    assertFalse(RotPUI.drawNextTurnNotice, "Turn progress must not cover " + overlay);
                    var main = RotPUI.instance().mainUI();
                    main.keyPressed(new KeyEvent(main, KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                            0, KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED));
                });
            }
            assertTrue(done.await(10, TimeUnit.SECONDS), "Dismissing reports must return to planning");
            assertFalse(GameSession.performingTurn());
            assertTrue(RotPUI.drawNextTurnNotice, "Later turn processing must retain its progress notice");
        } finally {
            active.set(false);
            game.resumeNextTurnProcessing();
            done.await(2, TimeUnit.SECONDS);
            HotSeatTestFixture.onEdt(() -> {
                GameSession.performingTurn(false);
                RotPUI.drawNextTurnNotice = true;
                RotPUI.instance().mainUI().clearOverlay();
            });
        }
    }

    private static String currentOverlay() {
        try {
            var field = rotp.ui.main.MainUI.class.getDeclaredField("overlay");
            field.setAccessible(true);
            return field.get(RotPUI.instance().mainUI()).getClass().getSimpleName();
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
}
