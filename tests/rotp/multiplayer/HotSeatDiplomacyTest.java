package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import rotp.multiplayer.hotseat.*;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatDiplomacyTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @ParameterizedTest @CsvSource({"PACT", "ALLIANCE"})
    void queuedOfferIsInvalidatedWhenItsPrerequisiteIsBroken(String kind) {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1))));
        var a = game.galaxy().empire(0); var b = game.galaxy().empire(1);
        a.viewForEmpire(b).embassy().contact(true); b.viewForEmpire(a).embassy().contact(true);
        a.viewForEmpire(b).trade().startRoute(25);
        if (kind.equals("ALLIANCE")) a.viewForEmpire(b).embassy().signPact();
        game.hotSeatState().confirmHandoff(game.hotSeatState().snapshot().revision());
        assertEquals(HotSeatDiplomacy.Result.QUEUED, new HotSeatDiplomacy(game).submit("a",
                game.hotSeatState().snapshot().revision(), HotSeatDiplomacy.Offer.valueOf(kind), 1, 0, null));
        var pending = game.pendingDiplomacyDecisions().get(0);
        if (kind.equals("ALLIANCE")) b.viewForEmpire(a).embassy().breakPact();
        else b.viewForEmpire(a).trade().stopRoute();
        game.turnCoordinator().startTurn(game.galaxy().currentTurn());
        assertTrue(game.answerDiplomacyDecision("b", pending.id(), true));
        assertFalse(b.pactWith(a.id));
        assertFalse(b.alliedWith(a.id));
        assertTrue(game.hotSeatInbox().unread(1).stream().flatMap(r -> r.lines().stream())
                .anyMatch(line -> line.contains("no longer eligible")), "Explain the invalidated offer");
    }

    @ParameterizedTest @CsvSource({"TRADE,true", "TRADE,false", "PEACE,true", "PEACE,false",
        "PACT,true", "PACT,false", "ALLIANCE,true", "ALLIANCE,false", "JOINT_WAR,true", "JOINT_WAR,false"})
    void humanOffersWaitForRecipientAndApplyOnce(String kind, boolean accept) {
        var game = HotSeatTestFixture.start(2, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1))));
        var a = game.galaxy().empire(0);
        var b = game.galaxy().empire(1);
        for (var x : game.galaxy().empires()) for (var y : game.galaxy().empires())
            if (x != y) x.viewForEmpire(y).embassy().contact(true);
        for (var empire : List.of(a, b)) {
            var colony = empire.allColonizedSystems().get(0).colony();
            colony.setPopulation(200); colony.industry().factories(200); empire.recalcPlanetaryProduction();
        }
        a.viewForEmpire(b).trade().setContact(); b.viewForEmpire(a).trade().setContact();
        if (kind.equals("PEACE")) a.viewForEmpire(b).embassy().declareWar();
        if (kind.equals("PACT") || kind.equals("ALLIANCE")) a.viewForEmpire(b).trade().startRoute(25);
        if (kind.equals("ALLIANCE")) a.viewForEmpire(b).embassy().signPact();
        game.hotSeatState().confirmHandoff(game.hotSeatState().snapshot().revision());
        var snapshot = game.hotSeatState().snapshot();
        var offers = new HotSeatDiplomacy(game);
        var offer = HotSeatDiplomacy.Offer.valueOf(kind);
        int level = 25;
        Integer target = kind.equals("JOINT_WAR") ? 2 : null;
        assertEquals(HotSeatDiplomacy.Result.WRONG_OWNER,
                offers.submit("b", snapshot.revision(), offer, 1, level, target));
        assertEquals(HotSeatDiplomacy.Result.QUEUED,
                offers.submit("a", snapshot.revision(), offer, 1, level, target));
        assertEquals(HotSeatDiplomacy.Result.DUPLICATE,
                offers.submit("a", snapshot.revision(), offer, 1, level, target));
        var pending = game.pendingDiplomacyDecisions().get(0);
        assertEquals("b", pending.notice().ownerPlayerId());
        assertFalse(b.alliedWith(a.id));
        game.turnCoordinator().startTurn(game.galaxy().currentTurn());
        assertFalse(game.answerDiplomacyDecision("a", pending.id(), accept));
        assertTrue(game.answerDiplomacyDecision("b", pending.id(), accept));
        assertFalse(game.answerDiplomacyDecision("b", pending.id(), accept));
        switch (kind) {
            case "TRADE" -> assertEquals(accept ? level : 0, b.viewForEmpire(a).trade().level());
            case "PEACE" -> assertEquals(!accept, b.atWarWith(a.id));
            case "PACT" -> assertEquals(accept, b.pactWith(a.id));
            case "ALLIANCE" -> assertEquals(accept, b.alliedWith(a.id));
            case "JOINT_WAR" -> assertEquals(accept, b.atWarWith(2));
        }
        assertFalse(game.hotSeatInbox().unread(0).isEmpty());
        assertFalse(game.hotSeatInbox().unread(1).isEmpty());
    }
}
