package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import rotp.Rotp;
import rotp.model.game.GameSession;
import rotp.model.game.IGalaxyOptions;
import rotp.model.game.IGameOptions;
import rotp.model.game.IGovOptions;
import rotp.model.game.IMainOptions;
import rotp.model.game.RulesetManager;
import rotp.model.colony.Colony;
import rotp.model.empires.Empire;
import rotp.model.empires.EspionageMission;
import rotp.model.empires.SabotageMission;
import rotp.model.empires.Spy;
import rotp.model.empires.SpyNetwork;
import rotp.model.combat.ShipCombatManager;
import rotp.model.events.RandomEventPlague;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.galaxy.Transport;
import rotp.model.tech.TechCategory;
import rotp.model.tech.TechTree;
import rotp.model.tech.TechLibrary;
import rotp.multiplayer.session.ControllerRegistry;
import rotp.multiplayer.session.PlayerSeat;
import rotp.multiplayer.session.MatchOutcome;
import rotp.multiplayer.turn.*;
import rotp.ui.RotPUI;
import rotp.ui.diplomacy.DialogueManager;
import rotp.ui.notifications.DiplomaticNotification;
import rotp.ui.notifications.StealTechNotification;
import rotp.ui.notifications.TechStolenAlert;
import rotp.ui.notifications.DiscoverTechNotification;
import rotp.util.Rand;

/** Requires desktop graphics and the game assets; never uses personal saves. */
@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class InProcessRecoveryTest {
    @TempDir static Path directory;
    private static JFrame frame;

    @BeforeAll
    static void initializeEngine() throws Exception {
        Rotp.rand(new Rand(136L));
        Files.writeString(directory.resolve("Remnants.cfg"), "GRAPHICS: Low\n");
        setStatic(Rotp.class, "startupDir", directory + File.separator);
        setStatic(Rotp.class, "isIDE", false);
        frame = new JFrame();
        setStatic(Rotp.class, "frame", frame);
        Method init = Rotp.class.getDeclaredMethod("initUtils");
        init.setAccessible(true);
        init.invoke(null);
        assertNull(Rotp.startupException, "Engine asset initialization failed");
        RulesetManager.current();
        RotPUI ui = new RotPUI();
        frame.add(ui);
        ui.initModel();
        IGameOptions.galaxyRandSource.set(136);
        setStatic(Rotp.class, "initialized", true);
        Field timerField = RotPUI.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            ui.init();
            try {
                ((Timer) timerField.get(ui)).stop();
            }
            catch (IllegalAccessException failure) {
                throw new IllegalStateException(failure);
            }
        });
        IMainOptions.saveDirectory.set(directory.toString());
    }

    // The forked test JVM owns the engine lifetime. No window is displayed.
    // Disposing the parent invokes legacy dialog cleanup before those dialogs
    // have ever been opened, so JVM exit performs native resource cleanup.

    @Test
    void completedMovementCheckpointResumesTheSameTurn() throws Exception {
        GameSession session = startMatch();
        Empire human = session.galaxy().empire(0);
        ShipFleet homeFleet = session.galaxy().system(human.homeSysId()).orbitingFleetForEmpire(human);
        StarSystem destination = null;
        for (int i = 0; i < session.galaxy().numStarSystems(); i++) {
            StarSystem candidate = session.galaxy().system(i);
            if (!candidate.isColonized() && !candidate.hasMonster() && homeFleet.canReach(i)) {
                destination = candidate;
                break;
            }
        }
        assertNotNull(destination);
        int[] scouts = new int[homeFleet.numCopy().length];
        scouts[human.shipLab().scoutDesign().id()] = 1;
        assertTrue(session.galaxy().ships.deploySubfleet(homeFleet, scouts, destination.id));
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        assertTrue(human.allFleets().stream().anyMatch(fleet -> fleet.inTransit()
                || fleet.sysId() != human.homeSysId()), "The fixture must actually move a fleet");
        // Reproduce pending cleanup work that used to be lost by serialization.
        Colony aiColony = session.galaxy().empire(2).allColonizedSystems().get(0).colony();
        for (int category = 0; category < 5; category++)
            aiColony.allocation(category, category == Colony.INDUSTRY ? 50 : 0);
        aiColony.toggleRecalcSpending();
        List<String> boundaryState = semanticState(session);
        File saved = directory.resolve("movement.rotp").toFile();
        session.saveInProcessCheckpoint(saved);
        int turn = session.galaxy().currentTurn();
        driver(session).advanceOneTurn();
        int expectedTurn = session.galaxy().currentTurn();
        List<String> expectedState = semanticState(session);
        long expectedRandom = Rotp.rand().nextLong();

        GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
        installProviders(restored);
        assertEquals(boundaryState, semanticState(restored));
        assertEquals(turn, restored.galaxy().currentTurn());
        assertEquals(TurnCoordinator.Phase.COUNCIL, restored.turnCoordinator().nextPhase());
        driver(restored).advanceOneTurn();
        assertEquals(expectedTurn, restored.galaxy().currentTurn());
        assertEquals(expectedState, semanticState(restored));
        assertEquals(expectedRandom, Rotp.rand().nextLong());
        assertTrue(restored.galaxy().empire(0).isPlayerControlled());
        assertTrue(restored.galaxy().empire(1).isPlayerControlled());
        assertTrue(restored.galaxy().empire(2).isAIControlled());
    }

    @Test
    void researchBarrierPreservesOwnershipAndContinuation() throws Exception {
        GameSession session = startMatch();
        for (int empire = 0; empire < 2; empire++)
            GameSession.requestTechSelection(session.galaxy().empire(empire).tech().computer());
        TurnCheckpoint pending = session.advanceInProcessPhase();
        assertTrue(pending.researchChoices().stream().anyMatch(d -> d.empireId() == 0));
        assertTrue(pending.researchChoices().stream().anyMatch(d -> d.empireId() == 1));
        PendingResearchDecision choice = pending.researchChoices().get(0);
        ResearchDecisionRouter router = new ResearchDecisionRouter(session);
        String wrongOwner = choice.empireId() == 0 ? "human-1" : "human-0";
        assertEquals(ResearchDecisionRouter.Result.WRONG_OWNER, router.submit(wrongOwner,
                choice.id(), choice.empireId(), choice.categoryIndex(), choice.legalTechIds().get(0)));
        assertEquals(pending, session.turnCheckpoint());
        File saved = directory.resolve("research.rotp").toFile();
        session.saveDecisionBarrier(saved);
        driver(session).advanceOneTurn();
        List<String> expected = semanticState(session);
        long expectedRandom = Rotp.rand().nextLong();
        assertEquals(ResearchDecisionRouter.Result.NO_PENDING_DECISION,
                router.submit(choice.ownerPlayerId(), choice.id(), choice.empireId(),
                        choice.categoryIndex(), choice.legalTechIds().get(0)));
        assertEquals(expected, semanticState(session));

        GameSession restored = GameSession.restoreDecisionBarrier(saved);
        installProviders(restored);
        assertEquals(pending, restored.turnCheckpoint());
        driver(restored).advanceOneTurn();
        assertEquals(expected, semanticState(restored));
        assertEquals(expectedRandom, Rotp.rand().nextLong());
    }

    @Test
    void twoColonizationChoicesSurviveReloadWithoutRepeatingProduction() throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.SPACE_COMBAT);
        List<Integer> targets = new ArrayList<>();
        for (int empireId = 0; empireId < 2; empireId++) {
            Empire empire = session.galaxy().empire(empireId);
            StarSystem target = null;
            for (int i = 0; i < session.galaxy().numStarSystems(); i++) {
                StarSystem candidate = session.galaxy().system(i);
                if (!targets.contains(i) && !candidate.isColonized() && !candidate.hasMonster()
                        && empire.canColonize(candidate.planet().type())) {
                    target = candidate;
                    break;
                }
            }
            assertNotNull(target, "Fixture needs a habitable unoccupied system");
            targets.add(target.id);
            session.galaxy().ships.buildShips(empireId, target.id, empire.shipLab().colonyDesign().id(), 1);
        }
        TurnCheckpoint pending = session.advanceInProcessPhase();
        assertEquals(TurnCoordinator.Phase.INVASIONS, pending.lastCompletedPhase());
        assertEquals(2, pending.colonizationChoices().size());
        for (PendingColonizationDecision choice : pending.colonizationChoices()) {
            assertFalse(session.answerColonizationDecision("human-" + (1 - choice.empireId()), choice.id(), true));
            assertFalse(session.galaxy().system(choice.systemId()).isColonized());
        }
        File saved = directory.resolve("colonization.rotp").toFile();
        session.saveDecisionBarrier(saved);
        driver(session).advanceOneTurn();
        for (int empireId = 0; empireId < 2; empireId++)
            assertEquals(empireId, session.galaxy().system(targets.get(empireId)).empId());
        List<String> expected = semanticState(session);
        long expectedRandom = Rotp.rand().nextLong();
        for (PendingColonizationDecision choice : pending.colonizationChoices())
            assertFalse(session.answerColonizationDecision(choice.ownerPlayerId(), choice.id(), true));
        assertEquals(expected, semanticState(session));
        GameSession restored = GameSession.restoreDecisionBarrier(saved);
        installProviders(restored);
        assertEquals(pending, restored.turnCheckpoint());
        driver(restored).advanceOneTurn();
        assertEquals(expected, semanticState(restored));
        assertEquals(expectedRandom, Rotp.rand().nextLong());
    }

    @Test
    void diplomaticOfferReloadAppliesTreatyOnlyForItsOwner() throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.DIPLOMACY);
        Empire first = session.galaxy().empire(0);
        Empire second = session.galaxy().empire(1);
        first.viewForEmpire(second).embassy().contact(true);
        second.viewForEmpire(first).embassy().contact(true);
        DiplomaticNotification.create(first.viewForEmpire(second), DialogueManager.OFFER_PACT);
        TurnCheckpoint pending = session.deliverInProcessNotifications();
        assertEquals(1, pending.diplomacyChoices().size());
        PendingDiplomacyDecision choice = pending.diplomacyChoices().get(0);
        assertEquals("human-1", choice.notice().ownerPlayerId());
        assertFalse(session.answerDiplomacyDecision("human-0", choice.id(), true));
        assertFalse(first.pactWith(1));
        File saved = directory.resolve("diplomacy.rotp").toFile();
        session.saveDecisionBarrier(saved);
        assertTrue(session.answerDiplomacyDecision("human-1", choice.id(), true));
        assertTrue(first.pactWith(1));
        assertTrue(second.pactWith(0));
        assertFalse(session.answerDiplomacyDecision("human-1", choice.id(), true));
        driver(session).advanceOneTurn();
        List<String> expected = semanticState(session);
        long expectedRandom = Rotp.rand().nextLong();

        GameSession restored = GameSession.restoreDecisionBarrier(saved);
        installProviders(restored);
        assertEquals(pending, restored.turnCheckpoint());
        assertTrue(restored.answerDiplomacyDecision("human-1", choice.id(), true));
        driver(restored).advanceOneTurn();
        assertEquals(expected, semanticState(restored));
        assertEquals(expectedRandom, Rotp.rand().nextLong());
    }

    @Test
    void automaticShipCombatMatchesAfterCheckpointRecovery() throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.EMPIRE_TURNS);
        Empire first = session.galaxy().empire(0);
        Empire second = session.galaxy().empire(1);
        first.viewForEmpire(second).embassy().declareWar();
        StarSystem arena = null;
        for (int i = 0; i < session.galaxy().numStarSystems(); i++) {
            StarSystem candidate = session.galaxy().system(i);
            if (!candidate.isColonized() && !candidate.hasMonster()) {
                arena = candidate;
                break;
            }
        }
        assertNotNull(arena);
        int arenaId = arena.id;
        session.galaxy().ships.buildShips(0, arenaId, first.shipLab().fighterDesign().id(), 40);
        session.galaxy().ships.buildShips(1, arenaId, second.shipLab().fighterDesign().id(), 3);
        session.deliverInProcessNotifications();
        File saved = directory.resolve("before-combat.rotp").toFile();
        session.saveInProcessCheckpoint(saved);
        TurnCheckpoint after = session.advanceInProcessPhase();
        assertEquals(TurnCoordinator.Phase.SPACE_COMBAT, after.lastCompletedPhase());
        assertTrue(arena.orbitingFleetsNoMonster().stream().map(ShipFleet::empId).distinct().count() <= 1,
                "Automatic combat must resolve the opposing fleets");
        List<String> expected = semanticState(session);
        long expectedRandom = Rotp.rand().nextLong();

        GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
        installProviders(restored);
        restored.advanceInProcessPhase();
        assertEquals(expected, semanticState(restored));
        assertEquals(expectedRandom, Rotp.rand().nextLong());
    }

    @Test
    void councilVoteBarrierRestoresStableOwnedDecisions() throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        // Arrange the Council scheduler; convention setup and voting remain real model work.
        Object council = session.galaxy().council();
        setField(council, "nextAction", 2); // scheduled CONVENE action
        setField(council, "actionCountdown", 0);
        TurnCheckpoint pending = session.advanceInProcessPhase();
        assertEquals(TurnCoordinator.Phase.COUNCIL, pending.lastCompletedPhase());
        PendingDecision choice = pending.councilVote();
        assertNotNull(choice);
        String wrongOwner = choice.empireId() == 0 ? "human-1" : "human-0";
        DecisionRouter router = new DecisionRouter(session);
        assertEquals(DecisionRouter.Result.WRONG_OWNER,
                router.submitCouncilVote(wrongOwner, choice.id(), null));
        assertEquals(DecisionRouter.Result.ILLEGAL_CHOICE,
                router.submitCouncilVote(choice.ownerPlayerId(), choice.id(), 9999));
        assertEquals(pending, session.turnCheckpoint());
        File saved = directory.resolve("council.rotp").toFile();
        session.saveDecisionBarrier(saved);
        driver(session).advanceOneTurn();
        List<String> expected = semanticState(session);
        long expectedRandom = Rotp.rand().nextLong();
        assertNotEquals(DecisionRouter.Result.ACCEPTED,
                router.submitCouncilVote(choice.ownerPlayerId(), choice.id(), null));
        assertEquals(expected, semanticState(session));

        GameSession restored = GameSession.restoreDecisionBarrier(saved);
        installProviders(restored);
        assertEquals(pending, restored.turnCheckpoint());
        driver(restored).advanceOneTurn();
        assertEquals(expected, semanticState(restored));
        assertEquals(expectedRandom, Rotp.rand().nextLong());
    }

    @Test
    void losingTheViewingEmpireDoesNotEndTheOtherHumansMatch() {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        for (StarSystem colony : new ArrayList<>(session.galaxy().empire(0).allColonizedSystems()))
            colony.colony().destroy();
        assertTrue(session.inProgress());
        assertEquals(MatchOutcome.SeatResult.ELIMINATED, session.matchOutcome().resultsByPlayer().get("human-0"));
        assertEquals(MatchOutcome.SeatResult.ACTIVE, session.matchOutcome().resultsByPlayer().get("human-1"));
        for (StarSystem colony : new ArrayList<>(session.galaxy().empire(2).allColonizedSystems()))
            colony.colony().destroy();
        assertFalse(session.inProgress());
        assertEquals(MatchOutcome.Cause.MILITARY, session.matchOutcome().cause());
        assertEquals(MatchOutcome.SeatResult.LOST, session.matchOutcome().resultsByPlayer().get("human-0"));
        assertEquals(MatchOutcome.SeatResult.WON, session.matchOutcome().resultsByPlayer().get("human-1"));
    }

    @Test
    void groundCombatCheckpointReplaysCaptureAndMatchEndingExactly() throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.SPACE_COMBAT);
        Empire attacker = session.galaxy().empire(0);
        Empire defender = session.galaxy().empire(1);
        for (StarSystem colony : new ArrayList<>(session.galaxy().empire(2).allColonizedSystems()))
            colony.colony().destroy();
        attacker.viewForEmpire(defender).embassy().declareWar();
        StarSystem target = session.galaxy().system(defender.homeSysId());
        target.colony().setPopulation(1);
        target.colony().defense().bases(0);
        Transport invasion = new Transport(session.galaxy().system(attacker.homeSysId()));
        invasion.size(40);
        invasion.setDest(target);
        invasion.launch();
        // Arrange arrival at the invasion boundary, using the same transport entry point as movement.
        session.galaxy().removeTransport(invasion);
        invasion.arrive();
        session.deliverInProcessNotifications();
        File saved = directory.resolve("before-ground-combat.rotp").toFile();
        session.saveInProcessCheckpoint(saved);
        session.advanceInProcessPhase();
        assertEquals(0, target.empId());
        assertFalse(session.inProgress());
        assertEquals(MatchOutcome.Cause.MILITARY, session.matchOutcome().cause());
        assertEquals(MatchOutcome.SeatResult.WON, session.matchOutcome().resultsByPlayer().get("human-0"));
        assertEquals(MatchOutcome.SeatResult.LOST, session.matchOutcome().resultsByPlayer().get("human-1"));
        List<String> expected = semanticState(session);
        long expectedRandom = Rotp.rand().nextLong();

        GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
        installProviders(restored);
        restored.advanceInProcessPhase();
        assertEquals(expected, semanticState(restored));
        assertEquals(expectedRandom, Rotp.rand().nextLong());
    }

    @ParameterizedTest(name = "Council {0}, second human accepts={1}")
    @CsvSource({
        "SETUP_COUNCIL_REBELS, true",
        "SETUP_COUNCIL_REBELS, false",
        "SETUP_COUNCIL_IMMEDIATE, true",
        "SETUP_COUNCIL_IMMEDIATE, false",
        "SETUP_COUNCIL_REALMS_BEYOND, false",
        "SETUP_COUNCIL_NO_ALLIANCES, false"
    })
    void councilRulingsResumeWithTheSameCoalitions(String mode, boolean secondAccepts) throws Exception {
        GameSession session = startMatch();
        session.options().selectedCouncilWinOption(mode);
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        // Elect the AI so its own ruling is deterministic; both human ballots and rulings are routed.
        Empire candidate = session.galaxy().empire(2);
        candidate.allColonizedSystems().get(0).colony().setPopulation(1000);
        var council = session.galaxy().council();
        setField(council, "nextAction", 2);
        setField(council, "actionCountdown", 0);
        session.advanceInProcessPhase();
        DecisionRouter router = new DecisionRouter(session);
        Set<String> voters = new HashSet<>();
        for (int attempt = 0; attempt < 3 && router.pendingCouncilVote() != null; attempt++) {
            PendingDecision vote = router.pendingCouncilVote();
            assertTrue(voters.add(vote.ownerPlayerId()), "A human must not vote twice");
            assertEquals(DecisionRouter.Result.ACCEPTED,
                    router.submitCouncilVote(vote.ownerPlayerId(), vote.id(), 2));
        }
        assertEquals(Set.of("human-0", "human-1"), voters);
        assertEquals(2, council.leader().id);
        TurnCheckpoint pending = session.deliverInProcessNotifications();
        PendingDecision ruling = pending.councilRuling();
        assertNotNull(ruling);
        String otherOwner = ruling.empireId() == 0 ? "human-1" : "human-0";
        assertEquals(DecisionRouter.Result.WRONG_OWNER,
                router.submitCouncilRuling(otherOwner, ruling.id(), true));
        assertEquals(DecisionRouter.Result.STALE_DECISION,
                router.submitCouncilRuling(ruling.ownerPlayerId(), ruling.id() + ":old", true));
        assertEquals(pending, session.turnCheckpoint());
        File saved = directory.resolve("ruling-" + mode + "-" + secondAccepts + ".rotp").toFile();
        session.saveDecisionBarrier(saved);
        answerCouncilRulings(session, secondAccepts);
        session.deliverInProcessNotifications();
        boolean finalWar = !secondAccepts && !session.options().immediateCouncilWin()
                && !session.options().realmsBeyondCouncil();
        assertEquals(finalWar, session.inProgress());
        assertTrue(council.allies().contains(session.galaxy().empire(0)));
        assertEquals(secondAccepts, council.allies().contains(session.galaxy().empire(1)));
        assertEquals(!secondAccepts, council.rebels().contains(session.galaxy().empire(1)));
        if (finalWar) {
            assertTrue(session.galaxy().empire(0).viewForEmpire(1).embassy().finalWar());
            assertTrue(session.galaxy().empire(1).viewForEmpire(2).embassy().finalWar());
        }
        else {
            assertEquals(MatchOutcome.Cause.COUNCIL, session.matchOutcome().cause());
            // Accepting an unrelated AI leader is a diplomatic loss in the original rules.
            assertEquals(MatchOutcome.SeatResult.LOST, session.matchOutcome().resultsByPlayer().get("human-0"));
            assertEquals(MatchOutcome.SeatResult.LOST,
                    session.matchOutcome().resultsByPlayer().get("human-1"));
        }
        List<String> expected = semanticState(session);
        long expectedRandom = Rotp.rand().nextLong();
        GameSession restored = GameSession.restoreDecisionBarrier(saved);
        installProviders(restored);
        assertEquals(pending, restored.turnCheckpoint());
        answerCouncilRulings(restored, secondAccepts);
        restored.deliverInProcessNotifications();
        assertEquals(expected, semanticState(restored));
        assertEquals(expectedRandom, Rotp.rand().nextLong());
    }

    private static void answerCouncilRulings(GameSession session, boolean secondAccepts) {
        DecisionRouter router = new DecisionRouter(session);
        Set<String> owners = new HashSet<>();
        for (int attempt = 0; attempt < 3 && router.pendingCouncilRuling() != null; attempt++) {
            PendingDecision ruling = router.pendingCouncilRuling();
            assertTrue(owners.add(ruling.ownerPlayerId()));
            boolean accept = ruling.empireId() == 0 || secondAccepts;
            assertEquals(DecisionRouter.Result.ACCEPTED,
                    router.submitCouncilRuling(ruling.ownerPlayerId(), ruling.id(), accept));
            assertNotEquals(DecisionRouter.Result.ACCEPTED,
                    router.submitCouncilRuling(ruling.ownerPlayerId(), ruling.id(), accept));
        }
        assertEquals(Set.of("human-0", "human-1"), owners);
        assertNull(router.pendingCouncilRuling());
    }

    @ParameterizedTest(name = "Election {0}, leader={1}, prior ally={2}")
    @CsvSource({
        "SETUP_COUNCIL_REBELS,0,false", "SETUP_COUNCIL_REBELS,0,true",
        "SETUP_COUNCIL_REBELS,2,false", "SETUP_COUNCIL_REBELS,2,true",
        "SETUP_COUNCIL_IMMEDIATE,0,false", "SETUP_COUNCIL_IMMEDIATE,0,true",
        "SETUP_COUNCIL_IMMEDIATE,2,false", "SETUP_COUNCIL_IMMEDIATE,2,true",
        "SETUP_COUNCIL_REALMS_BEYOND,0,false", "SETUP_COUNCIL_REALMS_BEYOND,0,true",
        "SETUP_COUNCIL_REALMS_BEYOND,2,false", "SETUP_COUNCIL_REALMS_BEYOND,2,true",
        "SETUP_COUNCIL_NO_ALLIANCES,0,false", "SETUP_COUNCIL_NO_ALLIANCES,0,true",
        "SETUP_COUNCIL_NO_ALLIANCES,2,false", "SETUP_COUNCIL_NO_ALLIANCES,2,true"
    })
    void settledCouncilPreservesLeaderAndExistingAllianceVictory(String mode, int leaderId,
            boolean priorAlliance) throws Exception {
        GameSession session = startMatch();
        session.options().selectedCouncilWinOption(mode);
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        Empire leader = session.galaxy().empire(leaderId);
        Empire potentialAlly = session.galaxy().empire(leaderId == 0 ? 1 : 0);
        if (priorAlliance) leader.viewForEmpire(potentialAlly).embassy().signAlliance();
        leader.allColonizedSystems().get(0).colony().setPopulation(1000);
        var council = session.galaxy().council();
        setField(council, "nextAction", 2);
        setField(council, "actionCountdown", 0);
        session.advanceInProcessPhase();
        DecisionRouter router = new DecisionRouter(session);
        while (router.pendingCouncilVote() != null) {
            var vote = router.pendingCouncilVote();
            assertEquals(DecisionRouter.Result.ACCEPTED,
                    router.submitCouncilVote(vote.ownerPlayerId(), vote.id(), leaderId));
        }
        assertEquals(leaderId, council.leader().id);
        session.deliverInProcessNotifications();
        File saved = directory.resolve("election-" + mode + "-" + leaderId + "-" + priorAlliance + ".rotp").toFile();
        session.saveDecisionBarrier(saved);
        // The AI may resist a human leader: arrange its acceptance before the human replies.
        for (Empire rebel : new ArrayList<>(council.rebels()))
            if (rebel.isAIControlled()) council.acceptRuling(rebel);
        answerCouncilRulings(session, true);
        assertTrue(session.matchOutcome().finished());
        for (int id = 0; id < 2; id++) {
            boolean won = id == leaderId || (id == potentialAlly.id && priorAlliance
                    && !session.options().noAllianceCouncil());
            assertEquals(won ? MatchOutcome.SeatResult.WON : MatchOutcome.SeatResult.LOST,
                    session.matchOutcome().resultsByPlayer().get("human-" + id));
        }
        MatchOutcome expected = session.matchOutcome();
        GameSession restored = GameSession.restoreDecisionBarrier(saved);
        installProviders(restored);
        for (Empire rebel : new ArrayList<>(restored.galaxy().council().rebels()))
            if (rebel.isAIControlled()) restored.galaxy().council().acceptRuling(rebel);
        answerCouncilRulings(restored, true);
        assertEquals(expected, restored.matchOutcome());
    }

    @Test
    void realmsBeyondDefyingPriorAllyCannotShareCouncilVictory() throws Exception {
        GameSession session = startMatch();
        session.options().selectedCouncilWinOption(IGameOptions.COUNCIL_REALMS_BEYOND);
        assertTrue(session.options().noAllianceCouncil(), "Realms Beyond also disables alliance victory");
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        Empire leader = session.galaxy().empire(2);
        leader.viewForEmpire(1).embassy().signAlliance();
        leader.allColonizedSystems().get(0).colony().setPopulation(1000);
        var council = session.galaxy().council();
        setField(council, "nextAction", 2);
        setField(council, "actionCountdown", 0);
        session.advanceInProcessPhase();
        DecisionRouter router = new DecisionRouter(session);
        while (router.pendingCouncilVote() != null) {
            var vote = router.pendingCouncilVote();
            assertEquals(DecisionRouter.Result.ACCEPTED,
                    router.submitCouncilVote(vote.ownerPlayerId(), vote.id(), 2));
        }
        session.deliverInProcessNotifications();
        File saved = directory.resolve("realms-defying-ally.rotp").toFile();
        session.saveDecisionBarrier(saved);
        for (boolean restore : new boolean[]{false, true}) {
            if (restore) session = GameSession.restoreDecisionBarrier(saved);
            router = new DecisionRouter(session);
            while (router.pendingCouncilRuling() != null) {
                var ruling = router.pendingCouncilRuling();
                assertEquals(DecisionRouter.Result.ACCEPTED, router.submitCouncilRuling(
                        ruling.ownerPlayerId(), ruling.id(), ruling.ownerPlayerId().equals("human-0")));
            }
            assertTrue(session.matchOutcome().finished());
            assertEquals(Map.of("human-0", MatchOutcome.SeatResult.LOST,
                    "human-1", MatchOutcome.SeatResult.LOST), session.matchOutcome().resultsByPlayer());
        }
    }

    private static void advanceTo(GameSession session, TurnCoordinator.Phase phase) {
        for (int attempt = 0; attempt < 30; attempt++) {
            TurnCheckpoint checkpoint = session.advanceInProcessPhase();
            answerResearch(session, checkpoint);
            if (checkpoint.lastCompletedPhase() == phase)
                return;
        }
        fail("Fixture did not reach " + phase);
    }

    @Test
    void partiallyAnsweredResearchContinuesIdenticallyForFiveTurns() throws Exception {
        GameSession session = startMatch();
        for (int empire = 0; empire < 2; empire++)
            GameSession.requestTechSelection(session.galaxy().empire(empire).tech().computer());
        TurnCheckpoint pending = session.advanceInProcessPhase();
        PendingResearchDecision first = pending.researchChoices().stream()
                .filter(d -> d.empireId() == 0).findFirst().orElseThrow();
        ResearchDecisionRouter router = new ResearchDecisionRouter(session);
        assertEquals(ResearchDecisionRouter.Result.ACCEPTED, router.submit(first.ownerPlayerId(),
                first.id(), first.empireId(), first.categoryIndex(), first.legalTechIds().get(0)));
        pending = session.deliverInProcessNotifications();
        assertTrue(pending.researchChoices().stream().anyMatch(d -> d.empireId() == 1));
        assertFalse(pending.researchChoices().contains(first));
        File saved = directory.resolve("partial-research.rotp").toFile();
        session.saveDecisionBarrier(saved);
        List<List<String>> expected = new ArrayList<>();
        List<Long> random = new ArrayList<>();
        int before = session.galaxy().currentTurn();
        for (int turn = 0; turn < 5; turn++) {
            driver(session).advanceOneTurn();
            expected.add(semanticState(session));
            random.add(Rotp.rand().nextLong());
        }
        assertTrue(session.galaxy().currentTurn() >= before + 4);
        GameSession restored = GameSession.restoreDecisionBarrier(saved);
        installProviders(restored);
        assertEquals(pending, restored.turnCheckpoint());
        assertEquals(ResearchDecisionRouter.Result.NO_PENDING_DECISION,
                new ResearchDecisionRouter(restored).submit(first.ownerPlayerId(), first.id(),
                        first.empireId(), first.categoryIndex(), first.legalTechIds().get(0)));
        for (int turn = 0; turn < 5; turn++) {
            driver(restored).advanceOneTurn();
            assertEquals(expected.get(turn), semanticState(restored), "Continuation turn " + turn);
            assertEquals(random.get(turn).longValue(), Rotp.rand().nextLong());
        }
    }

    @ParameterizedTest(name = "{0}: {1}")
    @CsvSource({"TRADE,accept", "TRADE,refuse", "TRADE,stale",
        "PEACE,accept", "PEACE,refuse", "PEACE,stale",
        "PACT,accept", "PACT,refuse", "PACT,stale",
        "ALLIANCE,accept", "ALLIANCE,refuse", "ALLIANCE,stale",
        "JOINT_WAR,accept", "JOINT_WAR,refuse", "JOINT_WAR,stale"})
    void diplomaticOfferMatrixSurvivesReload(String type, String response) throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.DIPLOMACY);
        Empire talker = session.galaxy().empire(0);
        Empire recipient = session.galaxy().empire(1);
        if (type.equals("TRADE")) {
            for (Empire empire : List.of(talker, recipient)) {
                Colony colony = empire.allColonizedSystems().get(0).colony();
                colony.setPopulation(200);
                colony.industry().factories(200);
                empire.recalcPlanetaryProduction();
            }
            talker.viewForEmpire(recipient).trade().setContact();
            recipient.viewForEmpire(talker).trade().setContact();
        }
        for (Empire a : session.galaxy().empires())
            for (Empire b : session.galaxy().empires())
                if (a != b) a.viewForEmpire(b).embassy().contact(true);
        if (type.equals("PEACE"))
            talker.viewForEmpire(recipient).embassy().declareWar();
        String message = switch (type) {
            case "TRADE" -> DialogueManager.OFFER_TRADE;
            case "PEACE" -> DialogueManager.OFFER_PEACE;
            case "PACT" -> DialogueManager.OFFER_PACT;
            case "ALLIANCE" -> DialogueManager.OFFER_ALLIANCE;
            default -> DialogueManager.OFFER_JOINT_WAR;
        };
        DiplomaticNotification.create(talker.viewForEmpire(recipient), message,
                type.equals("JOINT_WAR") ? session.galaxy().empire(2) : null);
        TurnCheckpoint pending = session.deliverInProcessNotifications();
        PendingDiplomacyDecision choice = pending.diplomacyChoices().stream()
                .filter(d -> d.notice().messageType().equals(message)).findFirst().orElseThrow();
        assertEquals("human-1", choice.notice().ownerPlayerId());
        assertEquals(1, choice.notice().recipientEmpireId());
        if (type.equals("TRADE")) assertTrue(choice.notice().tradeAmount() > 0);
        assertFalse(session.answerDiplomacyDecision("human-0", choice.id(), true));
        assertFalse(session.answerDiplomacyDecision("human-1", choice.id() + ":stale", true));
        if (response.equals("stale")) {
            if (type.equals("PEACE")) talker.viewForEmpire(recipient).embassy().signPeace();
            else if (type.equals("JOINT_WAR"))
                recipient.viewForEmpire(2).embassy().signAlliance();
            else talker.viewForEmpire(recipient).embassy().declareWar();
            session.deliverInProcessNotifications();
        }
        List<String> before = diplomaticState(session);
        File saved = directory.resolve("offer-" + type + "-" + response + ".rotp").toFile();
        session.saveDecisionBarrier(saved);
        boolean accept = !response.equals("refuse");
        assertTrue(session.answerDiplomacyDecision("human-1", choice.id(), accept));
        assertFalse(session.answerDiplomacyDecision("human-1", choice.id(), accept));
        if (!response.equals("accept")) assertEquals(before, diplomaticState(session));
        else switch (type) {
            case "TRADE" -> assertEquals(choice.notice().tradeAmount().intValue(), recipient.viewForEmpire(0).trade().level());
            case "PEACE" -> assertFalse(recipient.atWarWith(0));
            case "PACT" -> assertTrue(recipient.pactWith(0));
            case "ALLIANCE" -> assertTrue(recipient.alliedWith(0));
            default -> assertTrue(recipient.atWarWith(2));
        }
        session.deliverInProcessNotifications();
        List<String> expected = semanticState(session);
        List<String> expectedDiplomacy = diplomaticState(session);
        long rng = Rotp.rand().nextLong();
        GameSession restored = GameSession.restoreDecisionBarrier(saved);
        installProviders(restored);
        assertTrue(restored.answerDiplomacyDecision("human-1", choice.id(), accept));
        restored.deliverInProcessNotifications();
        assertEquals(expected, semanticState(restored));
        assertEquals(expectedDiplomacy, diplomaticState(restored));
        assertEquals(rng, Rotp.rand().nextLong());
    }

    private static List<String> diplomaticState(GameSession session) {
        List<String> result = new ArrayList<>();
        for (Empire a : session.galaxy().empires())
            for (Empire b : session.galaxy().empires())
                if (a != b) result.add(a.id + ":" + b.id + ":" + a.atWarWith(b.id)
                        + ":" + a.pactWith(b.id) + ":" + a.alliedWith(b.id)
                        + ":" + a.viewForEmpire(b).trade().level());
        return result;
    }

    @Test
    void informationDeliveryAndSavedAlertsRemainPrivate() throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        List<List<String>> events = eventLog(session);
        for (int id = 0; id < 2; id++) {
            Empire empire = session.galaxy().empire(id);
            GameSession.addSystemScouted(id, session.galaxy().system(empire.homeSysId()));
            GameSession.addShipsConstructed(id, empire.shipLab().fighterDesign(), id + 2);
            String technology = unknownComputerTech(empire);
            empire.learnTech(technology);
            TechStolenAlert.create(id, 2, technology);
        }
        session.deliverInProcessNotifications();
        for (int id = 0; id < 2; id++) {
            final int owner = id;
            assertTrue(events.get(id).stream().anyMatch(e -> e.equals("scout:" + session.galaxy().empire(owner).homeSysId())));
            assertTrue(events.get(id).contains("ships:" + (id + 2)));
            assertEquals(1, events.get(id).stream().filter(e -> e.startsWith("tech:")).count());
            assertFalse(session.alertRecordsForEmpire(id).isEmpty());
            assertTrue(session.alertRecordsForEmpire(id).stream().allMatch(a -> a.recipientEmpireId() == owner));
        }
        List<List<String>> delivered = events.stream().map(List::copyOf).toList();
        session.deliverInProcessNotifications();
        assertEquals(delivered, events);
        File saved = directory.resolve("private-alerts.rotp").toFile();
        // Newly learned research may leave a decision, both barrier forms are supported.
        session.saveInProcessCheckpoint(saved);
        List<AlertRecord> firstAlerts = session.alertRecordsForEmpire(0);
        List<AlertRecord> secondAlerts = session.alertRecordsForEmpire(1);
        GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
        assertEquals(firstAlerts, restored.alertRecordsForEmpire(0));
        assertEquals(secondAlerts, restored.alertRecordsForEmpire(1));
    }

    @Test
    void missingRecipientProviderCanRetryWithoutReplayingDeliveredNotices() throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        String firstTech = unknownComputerTech(session.galaxy().empire(0));
        String secondTech = unknownComputerTech(session.galaxy().empire(1));
        DiscoverTechNotification.create(0, firstTech);
        DiscoverTechNotification.create(1, secondTech);
        session.turnNotificationSink(StrictInProcessNotificationSink.deferredDecisions(Map.of(),
                Map.of("human-0", notice -> { assertEquals(0, notice.recipientEmpireId()); first.add(notice.techId()); }),
                Map.of(), Map.of()));
        List<String> before = semanticState(session);
        assertThrows(UnsupportedOperationException.class, session::deliverInProcessNotifications);
        assertEquals(List.of(firstTech), first);
        assertTrue(second.isEmpty());
        assertEquals(before, semanticState(session));
        assertThrows(IllegalStateException.class,
                () -> session.saveInProcessCheckpoint(directory.resolve("unsafe-delivery.rotp").toFile()));
        session.turnNotificationSink(StrictInProcessNotificationSink.deferredDecisions(Map.of(),
                Map.of("human-0", notice -> first.add(notice.techId()),
                       "human-1", notice -> { assertEquals(1, notice.recipientEmpireId()); second.add(notice.techId()); }),
                Map.of(), Map.of()));
        TurnCheckpoint after = session.deliverInProcessNotifications();
        assertEquals(TurnCoordinator.Phase.MOVEMENT, after.lastCompletedPhase());
        assertEquals(List.of(firstTech), first);
        assertEquals(List.of(secondTech), second);
        assertEquals(before, semanticState(session));
        session.deliverInProcessNotifications();
        assertEquals(1, first.size());
        assertEquals(1, second.size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void espionageAndSabotageUseOnlyTheirOwningProvider(int ownerId) {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        Empire owner = session.galaxy().empire(ownerId);
        Empire victim = session.galaxy().empire(1 - ownerId);
        StarSystem target = victim.allColonizedSystems().get(0);
        owner.viewForEmpire(victim).embassy().contact(true);
        victim.viewForEmpire(owner).embassy().contact(true);
        owner.sv.refreshFullScan(target.id);
        List<List<String>> events = eventLog(session);
        var spies = owner.viewForEmpire(victim).spies();
        String techId = unknownComputerTech(owner);
        var tech = TechLibrary.current().tech(techId);
        EspionageMission espionage = new EspionageMission(spies, new Spy(spies).makeSuper(),
                List.of(tech), target, List.of(tech));
        List<String> before = semanticState(session);
        assertThrows(IllegalStateException.class, () -> new InProcessEspionageDecisionAdapter(Map.of())
                .present(session, espionage, victim.id));
        assertThrows(IllegalArgumentException.class, () -> new InProcessEspionageDecisionAdapter(Map.of(
                "human-" + ownerId, decision -> new InProcessEspionageDecisionAdapter.Choice("invalid-tech", null)))
                .present(session, espionage, victim.id));
        assertThrows(IllegalArgumentException.class, () -> new InProcessEspionageDecisionAdapter(Map.of(
                "human-" + ownerId, decision -> new InProcessEspionageDecisionAdapter.Choice(techId, 9999)))
                .present(session, espionage, victim.id));
        assertFalse(espionage.hasStolenTech());
        assertEquals(before, semanticState(session));
        List<String> decisions = new ArrayList<>();
        session.espionageDecisionAdapter(new InProcessEspionageDecisionAdapter(Map.of(
                "human-" + ownerId, decision -> {
                    assertEquals(ownerId, decision.ownerEmpireId());
                    assertEquals("human-" + ownerId, decision.ownerPlayerId());
                    assertEquals(victim.id, decision.victimEmpireId());
                    decisions.add("espionage");
                    return new InProcessEspionageDecisionAdapter.Choice(techId, null);
                })));
        session.espionageDecisionAdapter().present(session, espionage, victim.id);
        assertTrue(owner.tech().allKnownTechs().contains(techId));
        assertFalse(victim.tech().allKnownTechs().contains(techId));
        StealTechNotification.create(ownerId, espionage, victim.id);
        SabotageMission sabotage = new SabotageMission(spies, new Spy(spies).makeSuper());
        float factories = target.colony().industry().factories();
        session.sabotageDecisionAdapter(new InProcessSabotageDecisionAdapter(Map.of(
                "human-" + ownerId, decision -> {
                    assertEquals(ownerId, decision.ownerEmpireId());
                    assertEquals("human-" + ownerId, decision.ownerPlayerId());
                    decisions.add("sabotage");
                    return new InProcessSabotageDecisionAdapter.Choice(SpyNetwork.Sabotage.FACTORIES, target.id);
                })));
        session.sabotageDecisionAdapter().resolve(session, sabotage, target.id);
        assertTrue(target.colony().industry().factories() < factories);
        session.deliverInProcessNotifications();
        assertEquals(List.of("espionage", "sabotage"), decisions);
        assertTrue(events.get(ownerId).contains("tech:" + techId));
        assertTrue(events.get(ownerId).contains("sabotage:" + target.id));
        assertTrue(events.get(1 - ownerId).isEmpty(), "The victim must not receive the spy's result payload");
    }

    @ParameterizedTest
    @CsvSource({"0,false", "1,false", "0,true", "1,true"})
    void bombardmentChoiceAndResultsReachTheParticipants(int attackerId, boolean skip) {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.SPACE_COMBAT);
        Empire attacker = session.galaxy().empire(attackerId);
        Empire defender = session.galaxy().empire(1 - attackerId);
        StarSystem target = defender.allColonizedSystems().get(0);
        attacker.viewForEmpire(defender).embassy().declareWar();
        session.galaxy().ships.buildShips(attackerId, target.id, attacker.shipLab().bomberDesign().id(), 10);
        session.deliverInProcessNotifications();
        List<List<String>> events = eventLog(session);
        List<String> decisions = new ArrayList<>();
        session.bombardmentDecisionAdapter(new InProcessBombardmentDecisionAdapter(Map.of(
                "human-" + attackerId, decision -> {
                    assertEquals(attackerId, decision.attackerEmpireId());
                    assertEquals(defender.id, decision.defenderEmpireId());
                    decisions.add(decision.ownerPlayerId());
                    return skip ? InProcessBombardmentDecisionAdapter.Choice.SKIP
                            : InProcessBombardmentDecisionAdapter.Choice.BOMBARD;
                })));
        float before = target.colony().population();
        session.bombardmentDecisionAdapter().resolve(session, target.id,
                target.orbitingFleetForEmpire(attacker), false, 0);
        session.deliverInProcessNotifications();
        assertEquals(List.of("human-" + attackerId), decisions);
        if (skip) {
            assertEquals(before, target.colony().population());
            assertTrue(events.stream().allMatch(List::isEmpty));
        }
        else {
            assertTrue(!target.isColonized() || target.colony().population() < before);
            assertEquals(List.of("scout:" + target.id, "bomb:" + target.id), events.get(attackerId));
            assertEquals(List.of("bomb:" + target.id), events.get(defender.id));
        }
    }

    @Test
    void combatBetweenSecondHumanAndAiDoesNotAddTheLocalViewer() {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.EMPIRE_TURNS);
        Empire human = session.galaxy().empire(1);
        Empire ai = session.galaxy().empire(2);
        StarSystem arena = Arrays.stream(session.galaxy().starSystems())
                .filter(s -> s != null && !s.isColonized() && !s.hasMonster()).findFirst().orElseThrow();
        human.viewForEmpire(ai).embassy().declareWar();
        session.galaxy().ships.buildShips(1, arena.id, human.shipLab().fighterDesign().id(), 40);
        session.galaxy().ships.buildShips(2, arena.id, ai.shipLab().fighterDesign().id(), 3);
        ShipCombatManager combat = session.galaxy().shipCombat();
        combat.battle(arena);
        assertEquals(Set.of(1, 2), new HashSet<>(combat.results().empires().stream().map(e -> e.id).toList()));
        assertNotEquals(0, combat.results().victor().id);
        assertTrue(arena.orbitingFleetsNoMonster().stream().map(ShipFleet::empId).distinct().count() <= 1);
    }

    @ParameterizedTest
    @CsvSource({"0,false", "3,false", "0,true", "3,true"})
    void aiBombardmentRunsThroughTheInvasionPhase(int victimId, boolean explicitSeat) throws Exception {
        GameSession session = startMatch(3);
        if (explicitSeat)
            session.controllerRegistry().add(new PlayerSeat("ai-2", 2,
                    PlayerSeat.ControllerType.AI, PlayerSeat.ConnectionStatus.NOT_APPLICABLE));
        advanceTo(session, TurnCoordinator.Phase.SPACE_COMBAT);
        Empire attacker = session.galaxy().empire(2);
        Empire defender = session.galaxy().empire(victimId);
        StarSystem target = defender.allColonizedSystems().get(0);
        attacker.viewForEmpire(defender).embassy().declareWar();
        session.galaxy().ships.buildShips(2, target.id, attacker.shipLab().bomberDesign().id(), 10);
        attacker.sv.refreshFullScan(target.id);
        ShipFleet bombers = target.orbitingFleetForEmpire(attacker);
        assertTrue(bombers.canAttackPlanets());
        assertTrue(attacker.ai().promptForBombardment(target, bombers) > 0,
                "AI fixture must actually choose bombardment");
        session.deliverInProcessNotifications();
        List<List<String>> events = eventLog(session);
        File saved = directory.resolve("ai-bomb-" + victimId + "-" + explicitSeat + ".rotp").toFile();
        session.saveInProcessCheckpoint(saved);
        float before = target.colony().population();
        TurnCheckpoint after = session.advanceInProcessPhase();
        assertEquals(TurnCoordinator.Phase.INVASIONS, after.lastCompletedPhase());
        assertTrue(!target.isColonized() || target.colony().population() < before);
        assertEquals(victimId == 0 ? List.of("bomb:" + target.id) : List.of(), events.get(0));
        assertTrue(events.get(1).isEmpty());
        List<String> expected = semanticState(session);
        long rng = Rotp.rand().nextLong();
        GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
        installProviders(restored);
        List<List<String>> restoredEvents = eventLog(restored);
        restored.advanceInProcessPhase();
        assertEquals(events, restoredEvents);
        assertEquals(expected, semanticState(restored));
        assertEquals(rng, Rotp.rand().nextLong());
    }

    private static String unknownComputerTech(Empire empire) {
        return empire.tech().computer().allTechs().stream()
                .filter(id -> !empire.tech().allKnownTechs().contains(id)).findFirst().orElseThrow();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void completedPlagueProjectKeepsItsSpendingRequestAfterRecovery(int ownerId) throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.COUNCIL);
        Empire owner = session.galaxy().empire(ownerId);
        StarSystem system = owner.allColonizedSystems().get(0);
        Colony colony = system.colony();
        colony.setGovernor(false);
        RandomEventPlague plague = new RandomEventPlague();
        plague.trigger(owner);
        assertTrue(colony.research().hasProject());
        // Arrange one research point remaining; completion runs through real production.
        setField(plague, "researchRemaining", 1f);
        for (int category = 0; category < 5; category++)
            colony.allocation(category, category == Colony.RESEARCH ? 50 : 0);
        session.advanceInProcessPhase();
        assertTrue(colony.research().hasCompletedProject());
        assertFalse(colony.research().hasProject());
        File saved = directory.resolve("plague-" + ownerId + ".rotp").toFile();
        session.saveInProcessCheckpoint(saved);
        driver(session).advanceOneTurn();
        var expectedRequests = session.allocationReasonsForEmpire(ownerId);
        assertTrue(expectedRequests.containsKey(system.id), "Project completion must request allocation");
        List<String> expected = semanticState(session);
        long rng = Rotp.rand().nextLong();
        GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
        installProviders(restored);
        driver(restored).advanceOneTurn();
        assertEquals(expectedRequests, restored.allocationReasonsForEmpire(ownerId));
        assertEquals(expected, semanticState(restored));
        assertEquals(rng, Rotp.rand().nextLong());
    }

    @Test
    void pendingColonyOrderKeepsItsReallocationAfterRecovery() throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.EMPIRE_TURNS);
        Colony colony = session.galaxy().empire(1).allColonizedSystems().get(0).colony();
        colony.setGovernor(false);
        for (int category = 0; category < 5; category++)
            colony.allocation(category, category == Colony.RESEARCH ? 50 : 0);
        colony.addColonyOrder(Colony.Orders.FACTORIES, 1f);
        File saved = directory.resolve("pending-order.rotp").toFile();
        session.saveInProcessCheckpoint(saved);
        driver(session).advanceOneTurn();
        assertTrue(colony.allocation(Colony.INDUSTRY) > 0);
        List<String> expected = semanticState(session);
        GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
        installProviders(restored);
        driver(restored).advanceOneTurn();
        assertEquals(expected, semanticState(restored));
    }

    @Test
    void aiShipBuildEstimateSurvivesPostProductionCheckpoint() throws Exception {
        GameSession session = startMatch();
        advanceTo(session, TurnCoordinator.Phase.EMPIRE_TURNS);
        Empire ai = session.galaxy().empire(2);
        Colony colony = ai.allColonizedSystems().get(0).colony();
        colony.shipyard().resetQueueData();
        float estimate = colony.shipyard().turnsToBuild(ai.shipLab().fighterDesign());
        assertTrue(Float.isFinite(estimate) && estimate > 0 && estimate < Integer.MAX_VALUE);
        File saved = directory.resolve("ship-estimate.rotp").toFile();
        session.saveInProcessCheckpoint(saved);
        GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
        Empire restoredAi = restored.galaxy().empire(2);
        assertEquals(estimate, restoredAi.allColonizedSystems().get(0).colony().shipyard()
                .turnsToBuild(restoredAi.shipLab().fighterDesign()));
    }

    @Test
    void preProductionSpyBudgetRemainsDeferredAfterRecovery() throws Exception {
        boolean previous = IGovOptions.trainSpiesASAP.get();
        try {
            GameSession session = startMatch();
            IGovOptions.trainSpiesASAP.set(false);
            assertFalse(session.getGovernorOptions().trainSpiesASAP());
            advanceTo(session, TurnCoordinator.Phase.PREPARE);
            var view = session.galaxy().empire(1).viewForEmpire(2);
            view.embassy().contact(false);
            view.spies().allocation(5);
            File saved = directory.resolve("before-spy-budget.rotp").toFile();
            session.saveInProcessCheckpoint(saved);
            view.owner().spyMasterAI().setSpyingAllocation(view);
            assertEquals(5, view.spies().allocation(), "Do not change spy budget before production");
            GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
            var restoredView = restored.galaxy().empire(1).viewForEmpire(2);
            restoredView.owner().spyMasterAI().setSpyingAllocation(restoredView);
            assertEquals(5, restoredView.spies().allocation());
        }
        finally { IGovOptions.trainSpiesASAP.set(previous); }
    }

    @Test
    void persistentRandomStreamIsNotConsumedByLoadNormalization() throws Exception {
        boolean previous = IGameOptions.persistentRNG.get();
        try {
            GameSession session = startMatch();
            IGameOptions.persistentRNG.set(true);
            assertTrue(session.options().persistentRNG());
            session.options().selectedRandomizeAIOption(IGameOptions.RANDOMIZE_AI_PERSONALITY);
            advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
            File saved = directory.resolve("persistent-rng.rotp").toFile();
            session.saveInProcessCheckpoint(saved);
            List<Long> expected = new ArrayList<>();
            for (int i = 0; i < 16; i++) expected.add(Rotp.rand().nextLong());
            GameSession.restoreInProcessCheckpoint(saved);
            List<Long> actual = new ArrayList<>();
            for (int i = 0; i < 16; i++) actual.add(Rotp.rand().nextLong());
            assertEquals(expected, actual);
        }
        finally { IGameOptions.persistentRNG.set(previous); }
    }

    @Test
    void incomeLossBeforeProductionStillUpdatesGovernorAfterRecovery() throws Exception {
        boolean previous = IGovOptions.contactUpdateSpending.get();
        try {
            GameSession session = startMatch();
            IGovOptions.contactUpdateSpending.set(true);
            assertTrue(session.getGovernorOptions().contactUpdateSpending());
            Empire owner = session.galaxy().empire(1);
            Colony colony = owner.allColonizedSystems().get(0).colony();
            colony.setGovernor(true);
            advanceTo(session, TurnCoordinator.Phase.PREPARE);
            float beforeLoss = owner.netIncome();
            advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
            // Model a movement-phase economic shock, before the production safety recalculation.
            colony.setPopulation(10);
            colony.industry().factories(0);
            owner.recalcPlanetaryProduction();
            for (int category = 0; category < 5; category++)
                colony.allocation(category, category == Colony.RESEARCH ? 50 : 0);
            assertTrue(owner.netIncome() < beforeLoss);
            var incomeField = Empire.class.getDeclaredField("lastNetIncome");
            incomeField.setAccessible(true);
            float preparedIncome = incomeField.getFloat(owner);
            assertTrue(owner.netIncome() < preparedIncome);
            File saved = directory.resolve("income-shock.rotp").toFile();
            session.saveInProcessCheckpoint(saved);
            advanceTo(session, TurnCoordinator.Phase.EMPIRE_TURNS);
            List<String> expected = semanticState(session);
            long rng = Rotp.rand().nextLong();
            GameSession restored = GameSession.restoreInProcessCheckpoint(saved);
            installProviders(restored);
            assertEquals(preparedIncome, incomeField.getFloat(restored.galaxy().empire(1)),
                    "The pre-production income comparison must survive the checkpoint");
            advanceTo(restored, TurnCoordinator.Phase.EMPIRE_TURNS);
            assertEquals(expected, semanticState(restored));
            assertEquals(rng, Rotp.rand().nextLong());
        }
        finally { IGovOptions.contactUpdateSpending.set(previous); }
    }

    private static List<List<String>> eventLog(GameSession session) {
        List<List<String>> log = List.of(new ArrayList<>(), new ArrayList<>());
        Map<String, StrictInProcessNotificationSink.ScoutingNoticeProvider> scouting = new java.util.HashMap<>();
        Map<String, StrictInProcessNotificationSink.TechnologyNoticeProvider> tech = new java.util.HashMap<>();
        Map<String, StrictInProcessNotificationSink.ShipConstructionNoticeProvider> ships = new java.util.HashMap<>();
        Map<String, StrictInProcessNotificationSink.DiplomacyNoticeProvider> diplomacy = new java.util.HashMap<>();
        Map<String, StrictInProcessNotificationSink.BombardmentNoticeProvider> bombs = new java.util.HashMap<>();
        Map<String, StrictInProcessNotificationSink.SabotageNoticeProvider> sabotage = new java.util.HashMap<>();
        for (int id = 0; id < 2; id++) {
            final int owner = id;
            String player = "human-" + id;
            scouting.put(player, (empire, systems) -> {
                assertEquals(owner, empire);
                systems.values().forEach(ids -> ids.forEach(system -> log.get(owner).add("scout:" + system)));
            });
            tech.put(player, notice -> { assertEquals(owner, notice.recipientEmpireId()); log.get(owner).add("tech:" + notice.techId()); });
            ships.put(player, (empire, designs) -> { assertEquals(owner, empire); designs.values().forEach(n -> log.get(owner).add("ships:" + n)); });
            diplomacy.put(player, notice -> { assertEquals(owner, notice.recipientEmpireId()); return false; });
            bombs.put(player, notice -> { assertEquals(owner, notice.recipientEmpireId()); log.get(owner).add("bomb:" + notice.systemId()); });
            sabotage.put(player, notice -> { assertEquals(owner, notice.recipientEmpireId()); log.get(owner).add("sabotage:" + notice.systemId()); });
        }
        session.turnNotificationSink(StrictInProcessNotificationSink.deferredDecisions(scouting, tech, ships, diplomacy, bombs, sabotage));
        return log;
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void partiallyAnsweredCouncilRestoresAndFinalWarEnds(boolean rebelsWin) throws Exception {
        GameSession session = startMatch();
        session.options().selectedCouncilWinOption(IGameOptions.COUNCIL_REBELS);
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        var council = session.galaxy().council();
        session.galaxy().empire(2).allColonizedSystems().get(0).colony().setPopulation(1000);
        setField(council, "nextAction", 2);
        setField(council, "actionCountdown", 0);
        session.advanceInProcessPhase();
        DecisionRouter router = new DecisionRouter(session);
        while (router.pendingCouncilVote() != null) {
            var vote = router.pendingCouncilVote();
            assertEquals(DecisionRouter.Result.ACCEPTED,
                    router.submitCouncilVote(vote.ownerPlayerId(), vote.id(), 2));
        }
        var first = router.pendingCouncilRuling();
        assertNotNull(first);
        assertEquals("human-" + first.empireId(), first.ownerPlayerId());
        assertEquals(DecisionRouter.Result.ACCEPTED, router.submitCouncilRuling(
                first.ownerPlayerId(), first.id(), first.empireId() == 0));
        TurnCheckpoint partial = session.deliverInProcessNotifications();
        assertNotNull(partial.councilRuling());
        assertNotEquals(first.ownerPlayerId(), partial.councilRuling().ownerPlayerId());
        File saved = directory.resolve("partial-council-" + rebelsWin + ".rotp").toFile();
        session.saveDecisionBarrier(saved);
        finishFinalWar(session, rebelsWin);
        List<String> expected = semanticState(session);
        long rng = Rotp.rand().nextLong();
        GameSession restored = GameSession.restoreDecisionBarrier(saved);
        installProviders(restored);
        assertEquals(partial, restored.turnCheckpoint());
        assertNotEquals(DecisionRouter.Result.ACCEPTED, new DecisionRouter(restored)
                .submitCouncilRuling(first.ownerPlayerId(), first.id(), true));
        finishFinalWar(restored, rebelsWin);
        assertEquals(expected, semanticState(restored));
        assertEquals(rng, Rotp.rand().nextLong());
    }

    private static void finishFinalWar(GameSession session, boolean rebelsWin) {
        DecisionRouter router = new DecisionRouter(session);
        var ruling = router.pendingCouncilRuling();
        assertEquals("human-" + ruling.empireId(), ruling.ownerPlayerId());
        assertEquals(DecisionRouter.Result.ACCEPTED, router.submitCouncilRuling(
                ruling.ownerPlayerId(), ruling.id(), ruling.empireId() == 0));
        assertEquals(MatchOutcome.Cause.ONGOING, session.matchOutcome().cause());
        assertEquals(Map.of("human-0", MatchOutcome.SeatResult.ACTIVE,
                "human-1", MatchOutcome.SeatResult.ACTIVE), session.matchOutcome().resultsByPlayer());
        // Real colony destruction drives extinction and Council coalition removal.
        for (int id : rebelsWin ? new int[]{0, 2} : new int[]{1})
            for (StarSystem system : new ArrayList<>(session.galaxy().empire(id).allColonizedSystems()))
                system.colony().destroy();
        session.deliverInProcessNotifications();
        assertFalse(session.inProgress());
        assertEquals(rebelsWin ? MatchOutcome.Cause.MILITARY : MatchOutcome.Cause.COUNCIL,
                session.matchOutcome().cause());
        assertEquals(rebelsWin ? MatchOutcome.SeatResult.LOST : MatchOutcome.SeatResult.WON,
                session.matchOutcome().resultsByPlayer().get("human-0"));
        assertEquals(rebelsWin ? MatchOutcome.SeatResult.WON : MatchOutcome.SeatResult.LOST,
                session.matchOutcome().resultsByPlayer().get("human-1"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void allianceVictoryHonorsTheNoAllianceOption(boolean disabled) {
        GameSession session = startMatch();
        session.options().selectedCouncilWinOption(disabled ? IGameOptions.COUNCIL_NO_ALLIANCES : IGameOptions.COUNCIL_REBELS);
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        session.galaxy().empire(0).viewForEmpire(1).embassy().signAlliance();
        session.galaxy().empire(0).viewForEmpire(2).embassy().signAlliance();
        assertEquals(disabled, session.inProgress());
        assertEquals(disabled ? MatchOutcome.Cause.ONGOING : MatchOutcome.Cause.MILITARY_ALLIANCE,
                session.matchOutcome().cause());
        assertEquals(Map.of("human-0", disabled ? MatchOutcome.SeatResult.ACTIVE : MatchOutcome.SeatResult.WON,
                "human-1", disabled ? MatchOutcome.SeatResult.ACTIVE : MatchOutcome.SeatResult.WON),
                session.matchOutcome().resultsByPlayer());
    }

    @Test
    void allHumanSeatsLostEndsMatchEvenWhenMultipleAisSurvive() {
        GameSession session = startMatch(3);
        advanceTo(session, TurnCoordinator.Phase.MOVEMENT);
        for (int id : new int[]{0, 1})
            for (StarSystem system : new ArrayList<>(session.galaxy().empire(id).allColonizedSystems()))
                system.colony().destroy();
        assertEquals(2, session.galaxy().activeEmpires().size());
        assertFalse(session.inProgress());
        assertEquals(MatchOutcome.Cause.NO_HUMANS_REMAIN, session.matchOutcome().cause());
        assertEquals(Map.of("human-0", MatchOutcome.SeatResult.LOST,
                "human-1", MatchOutcome.SeatResult.LOST), session.matchOutcome().resultsByPlayer());
    }

    /** Explicit gameplay projection: no timestamps, artwork, caches, or object identities. */
    private static List<String> semanticState(GameSession session) {
        List<String> state = new ArrayList<>();
        state.add("turn=" + session.galaxy().currentTurn());
        state.add("outcome=" + session.matchOutcome());
        state.add("council=" + session.galaxy().council().voteIndex() + ":"
                + session.galaxy().council().votes1() + ":" + session.galaxy().council().votes2());
        state.add("council-allies=" + session.galaxy().council().allies().stream().map(e -> e.id).sorted().toList());
        state.add("council-rebels=" + session.galaxy().council().rebels().stream().map(e -> e.id).sorted().toList());
        for (Empire empire : session.galaxy().empires()) {
            state.add("empire=" + empire.id + ":" + empire.totalReserve() + ":"
                    + empire.tech().allKnownTechs().stream().sorted().toList());
            for (int i = 0; i < TechTree.NUM_CATEGORIES; i++) {
                TechCategory category = empire.tech().category(i);
                state.add("research=" + empire.id + ":" + i + ":" + category.currentTech()
                        + ":" + category.totalBC() + ":" + category.allocation());
            }
            for (Empire other : session.galaxy().empires()) {
                if (other == empire)
                    continue;
                var embassy = empire.viewForEmpire(other).embassy();
                state.add("relations=" + empire.id + ":" + other.id + ":" + embassy.contact()
                        + ":" + embassy.treaty().getClass().getSimpleName());
            }
            for (ShipFleet fleet : empire.allFleets())
                state.add("fleet=" + empire.id + ":" + fleet.sysId() + ":" + fleet.destSysId()
                        + ":" + fleet.x() + ":" + fleet.y() + ":" + fleet.inTransit()
                        + ":" + Arrays.toString(fleet.numCopy()));
        }
        for (int i = 0; i < session.galaxy().numStarSystems(); i++) {
            StarSystem system = session.galaxy().system(i);
            if (!system.isColonized())
                continue;
            var colony = system.colony();
            state.add("colony=" + i + ":" + colony.empire().id + ":" + colony.population()
                    + ":" + colony.industry().factories() + ":" + colony.defense().rawBases());
            for (int category = 0; category < 5; category++)
                state.add("spending=" + i + ":" + category + ":" + colony.allocation(category));
        }
        state.sort(String::compareTo);
        return state;
    }

    private static GameSession startMatch() {
        return startMatch(2);
    }

    private static GameSession startMatch(int opponents) {
        Rotp.rand(new Rand(136L));
        IGameOptions options = RulesetManager.current().newOptions().copyAllOptions();
        options.selectedGalaxySize(IGalaxyOptions.SIZE_TINY);
        options.selectedNumberOpponents(opponents);
        options.selectedCouncilWinOption(IGameOptions.COUNCIL_REBELS);
        ControllerRegistry roster = new ControllerRegistry();
        for (int empire = 0; empire < 2; empire++)
            roster.add(new PlayerSeat("human-" + empire, empire,
                    PlayerSeat.ControllerType.HUMAN, PlayerSeat.ConnectionStatus.CONNECTED));
        Rotp.rand(new Rand(136L));
        GameSession session = GameSession.instance();
        session.startGame(options, roster);
        installProviders(session);
        return session;
    }

    private static void installProviders(GameSession session) {
        session.turnNotificationSink(StrictInProcessNotificationSink.deferredDecisions(
                Map.of("human-0", (empire, systems) -> assertEquals(0, empire),
                       "human-1", (empire, systems) -> assertEquals(1, empire)),
                Map.of("human-0", notice -> assertEquals(0, notice.recipientEmpireId()),
                       "human-1", notice -> assertEquals(1, notice.recipientEmpireId())),
                Map.of("human-0", (empire, designs) -> assertEquals(0, empire),
                       "human-1", (empire, designs) -> assertEquals(1, empire)),
                Map.of("human-0", notice -> { assertEquals(0, notice.recipientEmpireId()); return false; },
                       "human-1", notice -> { assertEquals(1, notice.recipientEmpireId()); return false; })));
    }

    private static InProcessMatchDriver driver(GameSession session) {
        return new InProcessMatchDriver(session, Map.of(
                "human-0", new Decisions("human-0"), "human-1", new Decisions("human-1")));
    }

    private static void answerResearch(GameSession session, TurnCheckpoint checkpoint) {
        for (PendingResearchDecision decision : checkpoint.researchChoices())
            assertEquals(ResearchDecisionRouter.Result.ACCEPTED,
                    new ResearchDecisionRouter(session).submit(decision.ownerPlayerId(), decision.id(),
                            decision.empireId(), decision.categoryIndex(), decision.legalTechIds().get(0)));
    }

    private record Decisions(String owner) implements InProcessMatchDriver.SeatDecisions {
        @Override public Integer councilVote(PendingDecision decision) {
            assertEquals(owner, decision.ownerPlayerId());
            return null;
        }
        @Override public boolean councilRuling(PendingDecision decision) {
            assertEquals(owner, decision.ownerPlayerId());
            return true;
        }
        @Override public String research(PendingResearchDecision decision) {
            assertEquals(owner, decision.ownerPlayerId());
            return decision.legalTechIds().get(0);
        }
        @Override public boolean colonize(PendingColonizationDecision decision) {
            assertEquals(owner, decision.ownerPlayerId());
            return true;
        }
        @Override public boolean diplomacy(PendingDiplomacyDecision decision) {
            assertEquals(owner, decision.notice().ownerPlayerId());
            return false;
        }
    }

    private static void setStatic(Class<?> type, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
