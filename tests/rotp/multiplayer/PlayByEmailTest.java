package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.model.empires.Empire;
import rotp.model.empires.EspionageMission;
import rotp.model.empires.SabotageMission;
import rotp.model.empires.Spy;
import rotp.model.game.GameSession;
import rotp.model.game.IConvenienceOptions;
import rotp.model.game.IGameOptions;
import rotp.model.galaxy.StarSystem;
import rotp.model.tech.TechLibrary;
import rotp.multiplayer.hotseat.*;
import rotp.multiplayer.pbem.StandingOrders;
import rotp.multiplayer.pbem.StandingOrders.*;
import rotp.multiplayer.turn.*;
import rotp.ui.multiplayer.HotSeatDesktop;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class PlayByEmailTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    /** Fails the test if a mid-phase choice ever reaches a screen in play by email. */
    static class NoMissionPrompts extends HotSeatTurnTest.ImmediateChoices {
        @Override public CompletableFuture<InProcessBombardmentDecisionAdapter.Choice> bombardment(BombardmentDecision d) { throw new AssertionError("bombard prompt"); }
        @Override public CompletableFuture<InProcessEspionageDecisionAdapter.Choice> espionage(EspionageDecision d) { throw new AssertionError("espionage prompt"); }
        @Override public CompletableFuture<InProcessSabotageDecisionAdapter.Choice> sabotage(SabotageDecision d) { throw new AssertionError("sabotage prompt"); }
    }

    static GameSession startMatch() {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0),
                new HotSeatSetup.Assignment("b", "Bob", 1))), true);
        return game;
    }

    static HotSeatController attach(GameSession game) {
        HotSeatController[] controller = new HotSeatController[1];
        HotSeatTestFixture.onEdt(() -> {
            controller[0] = new HotSeatController(game, new HotSeatDesktop(), new NoMissionPrompts());
            try { HotSeatTestFixture.set(GameSession.class, game, "hotSeatController", controller[0]); }
            catch (Exception ex) { throw new AssertionError(ex); }
            controller[0].start();
        });
        return controller[0];
    }

    @Test void standingOrdersBombardWithoutPromptingAndIgnoreTheSharedSetting() throws Exception {
        var game = startMatch();
        assertNotNull(game.playByEmail());
        var controller = attach(game);
        String shared = IConvenienceOptions.autoBombard_.get();
        try {
            HotSeatTurnTest.awaitPlanning(game);
            IConvenienceOptions.autoBombard_.set(IConvenienceOptions.AUTOBOMBARD_NEVER);
            Empire attacker = game.galaxy().empire(0);
            Empire defender = game.galaxy().empire(1);
            StarSystem target = defender.allColonizedSystems().get(0);
            attacker.viewForEmpire(defender).embassy().declareWar();
            game.galaxy().ships.buildShips(0, target.id, attacker.shipLab().bomberDesign().id(), 10);
            attacker.sv.refreshFullScan(target.id);
            var fleet = target.orbitingFleetForEmpire(attacker);

            game.playByEmail().orders("a", new StandingOrders(Bombard.NEVER, Frame.NEVER, SabotageTarget.FACTORIES));
            float before = target.colony().population();
            attacker.ai().promptForBombardment(target, fleet);
            assertEquals(before, target.colony().population(), "NEVER must not bomb");

            game.playByEmail().orders("a", new StandingOrders(Bombard.AT_WAR, Frame.NEVER, SabotageTarget.FACTORIES));
            attacker.ai().promptForBombardment(target, fleet);
            assertTrue(!target.isColonized() || target.colony().population() < before,
                    "AT_WAR must bomb even though the shared setting says never");
        } finally {
            IConvenienceOptions.autoBombard_.set(shared);
            HotSeatTestFixture.onEdt(controller::close);
        }
    }

    @Test void standingOrdersStealFrameAndSabotageWithoutPrompting() throws Exception {
        var game = startMatch();
        var controller = attach(game);
        try {
            HotSeatTurnTest.awaitPlanning(game);
            Empire owner = game.galaxy().empire(0);
            Empire victim = game.galaxy().empire(1);
            StarSystem target = victim.allColonizedSystems().get(0);
            owner.viewForEmpire(victim).embassy().contact(true);
            victim.viewForEmpire(owner).embassy().contact(true);
            owner.sv.refreshFullScan(target.id);
            var spies = owner.viewForEmpire(victim).spies();
            String techId = owner.tech().computer().allTechs().stream()
                    .filter(id -> !owner.tech().allKnownTechs().contains(id)).findFirst().orElseThrow();
            var tech = TechLibrary.current().tech(techId);
            var espionage = new EspionageMission(spies, new Spy(spies).makeSuper(), List.of(tech), target, List.of(tech));
            game.espionageDecisionAdapter().present(game, espionage, victim.id);
            assertTrue(owner.tech().allKnownTechs().contains(techId), "The game picks the only legal tech");

            game.playByEmail().orders("a", new StandingOrders(Bombard.NEVER, Frame.NEVER, SabotageTarget.FACTORIES));
            float factories = target.colony().industry().factories();
            game.sabotageDecisionAdapter().resolve(game, new SabotageMission(spies, new Spy(spies).makeSuper()), target.id);
            assertTrue(target.colony().industry().factories() < factories, "Standing sabotage destroys factories");
        } finally { HotSeatTestFixture.onEdt(controller::close); }
    }

    @Test void simulationSettingsStoredPerComputerAreFrozenForTheMatch() throws Exception {
        String aggression = rotp.model.game.IInGameOptions.gameAgressiveness.get();
        startMatch();
        rotp.model.game.IInGameOptions.gameAgressiveness.set(IGameOptions.AGGRESSIV_ALWAYS_WAR);
        assertEquals(aggression, rotp.model.game.IInGameOptions.gameAgressiveness.get(),
                "A computer's own setting must not change a running match");
    }
}
