package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.Rotp;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.*;
import rotp.ui.multiplayer.HotSeatDesktop;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatRecoveryTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @Test void planningSavePreservesPartialRoundReportsOrdersAndRandomStream() throws Exception {
        var game = HotSeatTestFixture.start(2, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1),
                new HotSeatSetup.Assignment("c", "Charlie", 2))));
        HotSeatController[] controller = new HotSeatController[1];
        HotSeatTestFixture.onEdt(() -> {
            controller[0] = new HotSeatController(game, new HotSeatDesktop(), new HotSeatTurnTest.ImmediateChoices());
            try { HotSeatTestFixture.set(GameSession.class, game, "hotSeatController", controller[0]); }
            catch (Exception ex) { throw new AssertionError(ex); }
            controller[0].start();
        });
        HotSeatTurnTest.awaitPlanning(game);
        HotSeatTestFixture.onEdt(() -> {
            var snapshot = game.hotSeatState().snapshot();
            controller[0].finishPlayerTurn(snapshot.ownerPlayerId(), snapshot.revision());
        });
        HotSeatTurnTest.awaitPlanning(game);
        game.hotSeatInbox().append(1, 0, "TEST", "Private", List.of("Alice only"));
        game.galaxy().empire(0).allColonizedSystems().get(0).colony().setGovernor(false);
        game.galaxy().empire(1).allColonizedSystems().get(0).colony().setGovernor(true);
        var snapshot = game.hotSeatState().snapshot();
        float[] maintenance = new float[3];
        float[] income = new float[3];
        for (int id = 0; id < 3; id++) {
            var empire = game.galaxy().empire(id);
            empire.recalcPlanetaryProduction();
            maintenance[id] = empire.shipMaintCostPerBC();
            income[id] = empire.netIncome();
            assertTrue(maintenance[id] > 0, "Starting ships must incur maintenance");
        }
        var file = directory.resolve("planning.rotp").toFile();
        HotSeatPersistence.save(game, file);
        long expectedRandom = Rotp.rand().nextLong();
        var restored = HotSeatPersistence.load(file);
        assertEquals(expectedRandom, Rotp.rand().nextLong());
        for (int id = 0; id < 3; id++) {
            var empire = restored.galaxy().empire(id);
            assertEquals(maintenance[id], empire.shipMaintCostPerBC(), 0.0001f,
                    "Loaded planning preview must include ship maintenance for seat " + id);
            assertEquals(income[id], empire.netIncome(), 0.0001f);
        }
        assertEquals(snapshot.finishedPlayers(), restored.hotSeatState().snapshot().finishedPlayers());
        assertEquals("b", restored.hotSeatState().snapshot().ownerPlayerId());
        assertEquals(HotSeatState.Stage.HANDOFF, restored.hotSeatState().snapshot().stage());
        assertTrue(restored.hotSeatState().snapshot().revision() > snapshot.revision());
        assertEquals(List.of("Alice only"), restored.hotSeatInbox().unread(0).get(0).lines());
        assertTrue(restored.hotSeatInbox().unread(1).isEmpty());
        assertFalse(restored.galaxy().empire(0).allColonizedSystems().get(0).colony().isGovernor());
        assertTrue(restored.galaxy().empire(1).allColonizedSystems().get(0).colony().isGovernor());
        assertTrue(HotSeatDesktop.blocksNavigation());
        HotSeatTestFixture.onEdt(() -> restored.hotSeatController().close());
    }

    @Test void malformedLoadDoesNotReplaceCurrentGameOrConsumeRandomness() throws Exception {
        var game = HotSeatTestFixture.start(1, 0);
        var invalid = directory.resolve("invalid.rotp");
        Files.writeString(invalid, "Not a saved game");
        Rotp.rand(new rotp.util.Rand(741));
        long expected = new rotp.util.Rand(741).nextLong();
        assertThrows(Exception.class, () -> HotSeatPersistence.load(invalid.toFile()));
        assertSame(game, GameSession.instance());
        assertEquals(expected, Rotp.rand().nextLong());
    }

    @Test void failedWritePreservesOriginalAndUnsupportedVersionPreservesLiveSession() throws Exception {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1))));
        HotSeatTestFixture.onEdt(() -> game.openHotSeatDesktop(false));
        HotSeatTurnTest.awaitPlanning(game);
        var file = directory.resolve("atomic-hotseat.rotp").toFile();
        HotSeatPersistence.save(game, file);
        byte[] original = Files.readAllBytes(file.toPath());
        var views = GameSession.class.getDeclaredField("hotSeatViews");
        views.setAccessible(true);
        Object previous = views.get(game);
        views.set(game, java.util.Map.of(0, new Object()));
        try { assertThrows(Exception.class, () -> HotSeatPersistence.save(game, file)); }
        finally { views.set(game, previous); }
        assertArrayEquals(original, Files.readAllBytes(file.toPath()));
        HotSeatTestFixture.set(GameSession.class, game, "hotSeatSaveEnvelope",
                new HotSeatSaveEnvelope(999, HotSeatSaveEnvelope.Kind.PLANNING, game.hotSeatState().snapshot()));
        var unsupported = directory.resolve("unsupported.rotp").toFile();
        var write = GameSession.class.getDeclaredMethod("writeSessionAtomically", java.io.File.class);
        write.setAccessible(true);
        write.invoke(game, unsupported);
        var rng = Rotp.rand();
        assertThrows(Exception.class, () -> HotSeatPersistence.load(unsupported));
        assertSame(game, GameSession.instance());
        assertSame(rng, Rotp.rand());
        var restored = HotSeatPersistence.load(file);
        assertTrue(HotSeatDesktop.blocksNavigation());
        HotSeatTestFixture.onEdt(restored.hotSeatController()::close);
    }

    @Test void failedDecisionCoversTheGameAndRestoresTheLastSafeChoice() throws Exception {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1))));
        GameSession.requestTechSelection(game.galaxy().empire(1).tech().computer());
        HotSeatTestFixture.onEdt(() -> {
            var controller = new HotSeatController(game, new HotSeatDesktop(), new HotSeatTurnTest.ImmediateChoices() {
                @Override public java.util.concurrent.CompletableFuture<String> research(rotp.multiplayer.turn.PendingResearchDecision d) {
                    throw new IllegalStateException("Injected decision failure");
                }
            });
            try { HotSeatTestFixture.set(GameSession.class, game, "hotSeatController", controller); }
            catch (Exception ex) { throw new AssertionError(ex); }
            controller.start();
        });
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(20);
        while (game.hotSeatState().snapshot().stage() != HotSeatState.Stage.ERROR && System.nanoTime() < deadline) {
            HotSeatTestFixture.onEdt(() -> {
                if (Rotp.getFrame().getGlassPane() instanceof rotp.ui.multiplayer.HotSeatPrivacyPane pane) pane.acknowledge();
            });
            Thread.sleep(20);
        }
        assertEquals(HotSeatState.Stage.ERROR, game.hotSeatState().snapshot().stage());
        assertTrue(HotSeatDesktop.blocksNavigation());
        assertFalse(HotSeatPersistence.canSave(game));
        var restored = HotSeatPersistence.load(GameSession.saveFileNamed("HotSeat-LastSafe.rotp"));
        assertEquals(1, restored.galaxy().currentTurn());
        assertNotNull(restored.hotSeatState().snapshot().pendingDecisionId());
        assertTrue(HotSeatDesktop.blocksNavigation());
        HotSeatTestFixture.onEdt(restored.hotSeatController()::close);
    }

    @Test void savedResearchChoiceReopensForItsOwnerAndAcceptsARealButtonReply() throws Exception {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1))));
        GameSession.requestTechSelection(game.galaxy().empire(1).tech().computer());
        HotSeatTestFixture.onEdt(() -> {
            var controller = new HotSeatController(game, new HotSeatDesktop(), new HotSeatTurnTest.ImmediateChoices() {
                @Override public java.util.concurrent.CompletableFuture<String> research(rotp.multiplayer.turn.PendingResearchDecision d) {
                    return new java.util.concurrent.CompletableFuture<>();
                }
            });
            try { HotSeatTestFixture.set(GameSession.class, game, "hotSeatController", controller); }
            catch (Exception ex) { throw new AssertionError(ex); }
            controller.start();
        });
        awaitDecision(game);
        var before = game.hotSeatState().snapshot();
        assertEquals("b", before.ownerPlayerId());
        var file = directory.resolve("research-hotseat.rotp").toFile();
        HotSeatPersistence.save(game, file);
        var restored = HotSeatPersistence.load(file);
        assertEquals(before.pendingDecisionId(), restored.hotSeatState().snapshot().pendingDecisionId());
        awaitDecision(restored);
        var decision = new rotp.multiplayer.turn.ResearchDecisionRouter(restored).pending(1, 0);
        String chosen = decision.legalTechIds().get(0);
        HotSeatTestFixture.onEdt(() -> assertTrue(clickTechnology(Rotp.getFrame().getGlassPane(), chosen)));
        HotSeatTurnTest.awaitPlanning(restored);
        assertEquals(chosen, restored.galaxy().empire(1).tech().computer().currentTech());
        assertEquals(1, restored.galaxy().currentTurn());
        HotSeatTestFixture.onEdt(restored.hotSeatController()::close);
    }

    private static void awaitDecision(GameSession game) throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            HotSeatTestFixture.onEdt(() -> {
                if (Rotp.getFrame().getGlassPane() instanceof rotp.ui.multiplayer.HotSeatPrivacyPane pane) pane.acknowledge();
            });
            if (game.hotSeatState().snapshot().stage() == HotSeatState.Stage.DECISION) return;
            assertNotEquals(HotSeatState.Stage.ERROR, game.hotSeatState().snapshot().stage());
            Thread.sleep(20);
        }
        fail("Decision did not appear");
    }
    /** Picks a technology on the game's own research screen, which lists choices by level. */
    private static boolean clickTechnology(java.awt.Component component, String techId) {
        if (component instanceof rotp.ui.tech.SelectNewTechUI screen) {
            var listed = new java.util.ArrayList<>(screen.category().techIdsAvailableForResearch());
            listed.sort(rotp.model.tech.Tech.LEVEL);
            return screen.consoleEntry(String.valueOf(listed.indexOf(techId) + 1)) == rotp.ui.vipconsole.IVIPListener.VALID_ENTRY;
        }
        if (component instanceof java.awt.Container container)
            for (var child : container.getComponents()) if (clickTechnology(child, techId)) return true;
        return false;
    }
}
