package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import javax.swing.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.Rotp;
import rotp.model.game.*;
import rotp.multiplayer.hotseat.*;
import rotp.multiplayer.turn.*;
import rotp.ui.RotPUI;
import rotp.ui.multiplayer.*;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatTurnTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @Test void onlyTheLastHumanAdvancesExactlyOneSharedTurn() throws Exception {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0),
                new HotSeatSetup.Assignment("b", "Bob", 1))));
        int initial = game.galaxy().currentTurn();
        HotSeatController[] controller = new HotSeatController[1];
        HotSeatTestFixture.onEdt(() -> {
            controller[0] = new HotSeatController(game, new HotSeatDesktop(), new ImmediateChoices());
            controller[0].start();
        });
        try {
            awaitPlanning(game);
            assertEquals(initial, game.galaxy().currentTurn(), "Startup must not simulate a turn");
            var first = game.hotSeatState().snapshot();
            assertEquals("a", first.ownerPlayerId());
            assertTrue(controller[0].canEdit(0));
            assertFalse(controller[0].canEdit(1));
            HotSeatTestFixture.onEdt(() -> {
                assertTrue(controller[0].finishPlayerTurn("a", first.revision()));
                assertFalse(controller[0].finishPlayerTurn("a", first.revision()));
            });
            assertEquals(initial, game.galaxy().currentTurn());
            awaitPlanning(game);
            var second = game.hotSeatState().snapshot();
            assertEquals("b", second.ownerPlayerId());
            HotSeatTestFixture.onEdt(() -> {
                assertTrue(controller[0].finishPlayerTurn("b", second.revision()));
                assertFalse(controller[0].finishPlayerTurn("b", second.revision()));
            });
            awaitPlanning(game);
            assertEquals(initial + 1, game.galaxy().currentTurn());
            assertEquals("a", game.hotSeatState().snapshot().ownerPlayerId());
        } finally { HotSeatTestFixture.onEdt(controller[0]::close); }
    }

    @Test void aSeatedPlayerAnswersAllTheirChoicesBehindOneHandoff() throws Exception {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0),
                new HotSeatSetup.Assignment("b", "Bob", 1))));
        var researched = new java.util.ArrayList<String>();
        var handoffs = new java.util.ArrayList<String>();
        HotSeatController[] controller = new HotSeatController[1];
        HotSeatTestFixture.onEdt(() -> {
            controller[0] = new HotSeatController(game, new HotSeatDesktop(), new ImmediateChoices() {
                @Override public CompletableFuture<String> research(PendingResearchDecision d) {
                    researched.add(d.ownerPlayerId());
                    return super.research(d);
                }
            });
            controller[0].start();
        });
        try {
            awaitPlanning(game, handoffs);
            // Several open research categories per empire, as after a productive turn.
            for (int empire : List.of(0, 1))
                for (int category = 0; category < 3; category++)
                    game.galaxy().empire(empire).tech().category(category).requestSelection();
            for (String owner : List.of("a", "b")) {
                var snapshot = game.hotSeatState().snapshot();
                HotSeatTestFixture.onEdt(() -> assertTrue(controller[0].finishPlayerTurn(owner, snapshot.revision())));
                awaitPlanning(game, handoffs);
            }
            assertTrue(researched.size() > 2, "Fixture should produce several research choices: " + researched);
            for (int i = 1; i < handoffs.size(); i++)
                assertNotEquals(handoffs.get(i - 1), handoffs.get(i), "Repeated decision handoff: " + handoffs);
            for (int i = 2; i < researched.size(); i++)
                assertFalse(researched.get(i).equals(researched.get(i - 2))
                        && !researched.get(i).equals(researched.get(i - 1)), "Choices interleaved: " + researched);
        } finally { HotSeatTestFixture.onEdt(controller[0]::close); }
    }

    @Test void reportsPlayOnTheGamesOwnScreensBeforePlanningUnlocks() throws Exception {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0),
                new HotSeatSetup.Assignment("b", "Bob", 1))));
        HotSeatController[] controller = new HotSeatController[1];
        HotSeatTestFixture.onEdt(() -> {
            controller[0] = new HotSeatController(game, new HotSeatDesktop(), new ImmediateChoices());
            controller[0].start();
        });
        try {
            awaitPlanning(game);
            for (var earlier : game.hotSeatInbox().unread(1)) game.hotSeatInbox().acknowledge(1, earlier.id());
            String tech = game.galaxy().empire(1).tech().allKnownTechs().get(0);
            game.hotSeatInbox().append(game.galaxy().currentTurn(), 1, "TECH", "Research", List.of(tech),
                    new TechnologyNotice(TechnologyNotice.Kind.DISCOVERED, 1, tech, null, null));
            game.hotSeatInbox().append(game.galaxy().currentTurn(), 1, "COMBAT", "Combat", List.of("Won"));
            var first = game.hotSeatState().snapshot();
            HotSeatTestFixture.onEdt(() -> assertTrue(controller[0].finishPlayerTurn("a", first.revision())));
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
            while (!(RotPUI.instance().selectedPanel() instanceof rotp.ui.tech.DiscoverTechUI)) {
                assertTrue(System.nanoTime() < deadline, "Discovery screen did not appear");
                HotSeatTestFixture.onEdt(() -> {
                    if (Rotp.getFrame().getGlassPane() instanceof HotSeatPrivacyPane handoff) handoff.acknowledge();
                });
                Thread.sleep(20);
            }
            HotSeatTestFixture.onEdt(() -> assertFalse(controller[0].canEdit(1), "Planning waits for the reports"));
            game.resumeNextTurnProcessing();
            deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
            while (!(Rotp.getFrame().getGlassPane() instanceof HotSeatReportsPanel)) {
                assertTrue(System.nanoTime() < deadline, "Battle summary did not follow");
                Thread.sleep(20);
            }
            HotSeatTestFixture.onEdt(() -> ((HotSeatReportsPanel) Rotp.getFrame().getGlassPane()).acknowledge());
            awaitPlanning(game);
            HotSeatTestFixture.onEdt(() -> assertTrue(controller[0].canEdit(1)));
            assertTrue(game.hotSeatInbox().unread(1).isEmpty());
        } finally { HotSeatTestFixture.onEdt(controller[0]::close); }
    }

    static void awaitPlanning(GameSession game) throws Exception { awaitPlanning(game, null); }
    /** Records the owner of each acknowledged decision handoff when asked. */
    static void awaitPlanning(GameSession game, List<String> decisionHandoffs) throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(30);
        while (System.nanoTime() < deadline) {
            HotSeatTestFixture.onEdt(() -> {
                if (Rotp.getFrame().getGlassPane() instanceof HotSeatPrivacyPane handoff) {
                    var snapshot = game.hotSeatState().snapshot();
                    if (decisionHandoffs != null && snapshot.pendingDecisionId() != null)
                        decisionHandoffs.add(snapshot.ownerPlayerId());
                    handoff.acknowledge();
                }
                else if (Rotp.getFrame().getGlassPane() instanceof HotSeatReportsPanel reports)
                    reports.acknowledge();
            });
            boolean planning = game.hotSeatState().snapshot().stage() == HotSeatState.Stage.PLANNING;
            // Reports play on the game's own screens over the map: click through each one.
            if (planning && GameSession.performingTurn()) game.resumeNextTurnProcessing();
            else if (planning && !HotSeatDesktop.blocksNavigation()) return;
            assertNotEquals(HotSeatState.Stage.ERROR, game.hotSeatState().snapshot().stage());
            Thread.sleep(20);
        }
        fail("Timed out: " + game.hotSeatState().snapshot());
    }

    static class ImmediateChoices implements HotSeatDecisions {
        public CompletableFuture<Integer> councilVote(PendingDecision d) { return CompletableFuture.completedFuture(null); }
        public CompletableFuture<Boolean> councilRuling(PendingDecision d) { return CompletableFuture.completedFuture(true); }
        public CompletableFuture<String> research(PendingResearchDecision d) { return CompletableFuture.completedFuture(d.legalTechIds().get(0)); }
        public CompletableFuture<Boolean> colonize(PendingColonizationDecision d) { return CompletableFuture.completedFuture(true); }
        public CompletableFuture<Boolean> diplomacy(PendingDiplomacyDecision d) { return CompletableFuture.completedFuture(false); }
        public CompletableFuture<InProcessBombardmentDecisionAdapter.Choice> bombardment(BombardmentDecision d) { return CompletableFuture.completedFuture(InProcessBombardmentDecisionAdapter.Choice.SKIP); }
        public CompletableFuture<InProcessEspionageDecisionAdapter.Choice> espionage(EspionageDecision d) { return CompletableFuture.completedFuture(new InProcessEspionageDecisionAdapter.Choice(d.categoryTechIds().values().iterator().next(), null)); }
        public CompletableFuture<InProcessSabotageDecisionAdapter.Choice> sabotage(SabotageDecision d) { return CompletableFuture.completedFuture(null); }
    }
}
