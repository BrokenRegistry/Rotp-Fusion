package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rotp.Rotp;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.*;
import rotp.multiplayer.session.MatchOutcome;
import rotp.ui.RotPUI;
import rotp.ui.multiplayer.*;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatAcceptanceTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @org.junit.jupiter.api.Test void allHumansLostEndsWhileTwoAiEmpiresSurvive() throws Exception {
        var game = HotSeatTestFixture.start(3, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 2))));
        for (int id : new int[] {0, 2})
            for (var system : new ArrayList<>(game.galaxy().empire(id).allColonizedSystems())) system.colony().destroy();
        HotSeatTestFixture.onEdt(() -> game.openHotSeatDesktop(false));
        awaitResult(game);
        assertEquals(MatchOutcome.Cause.NO_HUMANS_REMAIN, game.matchOutcome().cause());
        assertTrue(game.matchOutcome().resultsByPlayer().values().stream().allMatch(r -> r == MatchOutcome.SeatResult.LOST));
        HotSeatTestFixture.onEdt(game.hotSeatController()::close);
    }

    @org.junit.jupiter.api.Test void councilPromptsAllHumansAndPublishesTheMatchResult() throws Exception {
        var game = HotSeatTestFixture.start(2, 0);
        var options = game.options().copyAllOptions();
        options.selectedCouncilWinOption(rotp.model.game.IGameOptions.COUNCIL_IMMEDIATE);
        game.startHotSeatGame(options, new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1))));
        game.galaxy().empire(0).allColonizedSystems().get(0).colony().setPopulation(1000);
        HotSeatTestFixture.set(game.galaxy().council().getClass(), game.galaxy().council(), "nextAction", 2);
        HotSeatTestFixture.set(game.galaxy().council().getClass(), game.galaxy().council(), "actionCountdown", 0);
        var voters = java.util.concurrent.ConcurrentHashMap.<String>newKeySet();
        HotSeatTestFixture.onEdt(() -> {
            var controller = new HotSeatController(game, new HotSeatDesktop(), new HotSeatTurnTest.ImmediateChoices() {
                @Override public java.util.concurrent.CompletableFuture<Integer> councilVote(rotp.multiplayer.turn.PendingDecision d) {
                    voters.add(d.ownerPlayerId()); return java.util.concurrent.CompletableFuture.completedFuture(0);
                }
            });
            try { HotSeatTestFixture.set(GameSession.class, game, "hotSeatController", controller); }
            catch (Exception failure) { throw new AssertionError(failure); }
            controller.start();
        });
        for (int i = 0; i < 2; i++) {
            HotSeatTurnTest.awaitPlanning(game);
            HotSeatTestFixture.onEdt(() -> {
                var snapshot = game.hotSeatState().snapshot();
                game.hotSeatController().finishPlayerTurn(snapshot.ownerPlayerId(), snapshot.revision());
            });
        }
        awaitResult(game);
        assertEquals(Set.of("a", "b"), voters);
        assertEquals(MatchOutcome.Cause.COUNCIL, game.matchOutcome().cause());
        assertEquals(MatchOutcome.SeatResult.WON, game.matchOutcome().resultsByPlayer().get("a"));
        HotSeatTestFixture.onEdt(game.hotSeatController()::close);
    }

    @org.junit.jupiter.api.Test void humansVoteOnTheGalacticCouncilScreen() throws Exception {
        var game = HotSeatTestFixture.start(2, 0);
        var options = game.options().copyAllOptions();
        options.selectedCouncilWinOption(rotp.model.game.IGameOptions.COUNCIL_IMMEDIATE);
        game.startHotSeatGame(options, new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1))));
        game.galaxy().empire(0).allColonizedSystems().get(0).colony().setPopulation(1000);
        HotSeatTestFixture.set(game.galaxy().council().getClass(), game.galaxy().council(), "nextAction", 2);
        HotSeatTestFixture.set(game.galaxy().council().getClass(), game.galaxy().council(), "actionCountdown", 0);
        var councilViewers = new java.util.ArrayList<Integer>();
        HotSeatTestFixture.onEdt(() -> {
            var desktop = new HotSeatDesktop();
            var controller = new HotSeatController(game, desktop, new HotSeatDecisionPanels(game, desktop));
            try { HotSeatTestFixture.set(GameSession.class, game, "hotSeatController", controller); }
            catch (Exception failure) { throw new AssertionError(failure); }
            controller.start();
        });
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(60);
        while (!(Rotp.getFrame().getGlassPane() instanceof HotSeatResultPanel) && System.nanoTime() < deadline) {
            HotSeatTestFixture.onEdt(() -> {
                var glass = Rotp.getFrame().getGlassPane();
                var snapshot = game.hotSeatState().snapshot();
                boolean covered = HotSeatDesktop.blocksNavigation();
                if (glass instanceof HotSeatPrivacyPane pane && covered) pane.acknowledge();
                else if (glass instanceof HotSeatReportsPanel pane && covered) pane.acknowledge();
                else if (covered && glass instanceof java.awt.Container c && findTech(c) != null) findTech(c).consoleEntry("1");
                else if (!covered && RotPUI.instance().selectedPanel() instanceof rotp.ui.GalacticCouncilUI council
                        && snapshot.stage() == HotSeatState.Stage.DECISION) {
                    if (councilViewers.isEmpty() || councilViewers.get(councilViewers.size() - 1) != game.galaxy().player().id)
                        councilViewers.add(game.galaxy().player().id);
                    council.keyPressed(new java.awt.event.KeyEvent(council, java.awt.event.KeyEvent.KEY_PRESSED,
                            System.currentTimeMillis(), 0, java.awt.event.KeyEvent.VK_1, java.awt.event.KeyEvent.CHAR_UNDEFINED));
                }
                else if (snapshot.stage() == HotSeatState.Stage.PLANNING && GameSession.performingTurn())
                    game.resumeNextTurnProcessing();
                else if (snapshot.stage() == HotSeatState.Stage.PLANNING && !covered)
                    game.hotSeatController().finishPlayerTurn(snapshot.ownerPlayerId(), snapshot.revision());
            });
            assertNotEquals(HotSeatState.Stage.ERROR, game.hotSeatState().snapshot().stage());
            Thread.sleep(20);
        }
        assertInstanceOf(HotSeatResultPanel.class, Rotp.getFrame().getGlassPane());
        assertEquals(MatchOutcome.Cause.COUNCIL, game.matchOutcome().cause());
        assertTrue(councilViewers.containsAll(List.of(0, 1)), "Each human votes on the council screen: " + councilViewers);
        HotSeatTestFixture.onEdt(game.hotSeatController()::close);
    }
    private static rotp.ui.tech.SelectNewTechUI findTech(java.awt.Container container) {
        for (var child : container.getComponents()) {
            if (child instanceof rotp.ui.tech.SelectNewTechUI screen) return screen;
            if (child instanceof java.awt.Container inner && findTech(inner) != null) return findTech(inner);
        }
        return null;
    }

    private static void awaitResult(GameSession game) throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
        while (!(Rotp.getFrame().getGlassPane() instanceof HotSeatResultPanel) && System.nanoTime() < deadline) {
            HotSeatTestFixture.onEdt(() -> {
                if (Rotp.getFrame().getGlassPane() instanceof HotSeatPrivacyPane pane) pane.acknowledge();
                else if (Rotp.getFrame().getGlassPane() instanceof HotSeatReportsPanel pane) pane.acknowledge();
            });
            assertNotEquals(HotSeatState.Stage.ERROR, game.hotSeatState().snapshot().stage());
            Thread.sleep(20);
        }
        assertInstanceOf(HotSeatResultPanel.class, Rotp.getFrame().getGlassPane());
    }

    @ParameterizedTest @ValueSource(booleans = {false, true})
    void fiveRoundsThenAnEliminatedViewerAndMilitaryResult(boolean includeAi) throws Exception {
        int opponents = includeAi ? 3 : 1;
        int[] humans = includeAi ? new int[] {0, 2, 3} : new int[] {0, 1};
        var game = HotSeatTestFixture.start(opponents, 0);
        var assignments = new ArrayList<HotSeatSetup.Assignment>();
        for (int id : humans) assignments.add(new HotSeatSetup.Assignment("p" + id, "Player " + id, id));
        game.startHotSeatGame(game.options(), new HotSeatSetup(assignments));
        HotSeatController[] controller = new HotSeatController[1];
        HotSeatTestFixture.onEdt(() -> {
            controller[0] = new HotSeatController(game, new HotSeatDesktop(), new HotSeatTurnTest.ImmediateChoices());
            try { HotSeatTestFixture.set(GameSession.class, game, "hotSeatController", controller[0]); }
            catch (Exception failure) { throw new AssertionError(failure); }
            controller[0].start();
        });
        try {
            int initialTurn = game.galaxy().currentTurn();
            float aiPopulation = includeAi ? game.galaxy().empire(1).totalPlanetaryPopulation() : 0;
            float aiFactories = includeAi ? game.galaxy().empire(1).allColonizedSystems().get(0).colony().industry().factories() : 0;
            for (int round = 0; round < 5; round++) {
                for (int id : humans) {
                    HotSeatTurnTest.awaitPlanning(game);
                    assertEquals(initialTurn + round, game.galaxy().currentTurn());
                    assertEquals("p" + id, game.hotSeatState().snapshot().ownerPlayerId());
                    HotSeatTestFixture.onEdt(() -> {
                        boolean lock = game.galaxy().empire(id).tech().computer().locked();
                        RotPUI.instance().techUI().toggleCategoryLock(0);
                        assertEquals(!lock, game.galaxy().empire(id).tech().computer().locked());
                        var snapshot = game.hotSeatState().snapshot();
                        assertTrue(controller[0].finishPlayerTurn(snapshot.ownerPlayerId(), snapshot.revision()));
                    });
                }
            }
            HotSeatTurnTest.awaitPlanning(game);
            assertEquals(initialTurn + 5, game.galaxy().currentTurn());
            if (includeAi) assertTrue(game.galaxy().empire(1).totalPlanetaryPopulation() != aiPopulation
                    || game.galaxy().empire(1).allColonizedSystems().get(0).colony().industry().factories() != aiFactories,
                    "AI production must change population or factories during five rounds");
            int winner = humans[humans.length - 1];
            for (var system : new ArrayList<>(game.galaxy().empire(0).allColonizedSystems())) system.colony().destroy();
            game.hotSeatInbox().append(game.galaxy().currentTurn(), 0, "LOSS", "Private loss", List.of("Only player zero sees this"));
            HotSeatTestFixture.onEdt(() -> {
                var snapshot = game.hotSeatState().snapshot();
                assertTrue(controller[0].finishPlayerTurn(snapshot.ownerPlayerId(), snapshot.revision()));
            });
            if (includeAi) {
                HotSeatTurnTest.awaitPlanning(game);
                assertEquals("p2", game.hotSeatState().snapshot().ownerPlayerId());
                assertTrue(game.inProgress(), "Viewer loss must not end the rostered match");
                for (var empire : game.galaxy().empires())
                    if (empire.id != winner)
                        for (var system : new ArrayList<>(empire.allColonizedSystems())) system.colony().destroy();
                HotSeatTestFixture.onEdt(() -> {
                    var snapshot = game.hotSeatState().snapshot();
                    controller[0].finishPlayerTurn(snapshot.ownerPlayerId(), snapshot.revision());
                });
            }
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
            while (!(Rotp.getFrame().getGlassPane() instanceof HotSeatResultPanel) && System.nanoTime() < deadline) {
                HotSeatTestFixture.onEdt(() -> {
                    if (Rotp.getFrame().getGlassPane() instanceof HotSeatPrivacyPane pane) pane.acknowledge();
                    else if (Rotp.getFrame().getGlassPane() instanceof HotSeatReportsPanel pane) pane.acknowledge();
                });
                assertNotEquals(HotSeatState.Stage.ERROR, game.hotSeatState().snapshot().stage());
                Thread.sleep(20);
            }
            assertInstanceOf(HotSeatResultPanel.class, Rotp.getFrame().getGlassPane());
            assertEquals(MatchOutcome.SeatResult.WON, game.matchOutcome().resultsByPlayer().get("p" + winner));
            assertEquals(MatchOutcome.SeatResult.LOST, game.matchOutcome().resultsByPlayer().get("p0"));
            assertTrue(game.hotSeatInbox().unread(0).isEmpty(), "Final private reports must be acknowledged");
        } finally { HotSeatTestFixture.onEdt(controller[0]::close); }
    }
}
