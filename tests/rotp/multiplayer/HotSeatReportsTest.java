package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.*;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatReportsTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }
    @Test void automaticBattleReportsReachOnlyParticipantsOnce() {
        var game = HotSeatTestFixture.start(2, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1),
                new HotSeatSetup.Assignment("c", "Charlie", 2))));
        var b = game.galaxy().empire(1);
        var c = game.galaxy().empire(2);
        b.viewForEmpire(c).embassy().declareWar();
        var arena = Arrays.stream(game.galaxy().starSystems()).filter(s -> s != null
                && !s.isColonized() && !s.hasMonster()).findFirst().orElseThrow();
        game.galaxy().ships.buildShips(1, arena.id, b.shipLab().fighterDesign().id(), 40);
        game.galaxy().ships.buildShips(2, arena.id, c.shipLab().fighterDesign().id(), 3);
        var combat = game.galaxy().shipCombat();
        combat.battle(arena);
        assertTrue(game.hotSeatInbox().unread(0).stream().noneMatch(r -> r.kind().equals("COMBAT")));
        for (int owner : new int[] {1, 2})
            assertEquals(1, game.hotSeatInbox().unread(owner).stream().filter(r -> r.kind().equals("COMBAT")).count());
        combat.endOfCombat(true);
        assertEquals(1, game.hotSeatInbox().unread(1).stream().filter(r -> r.kind().equals("COMBAT")).count());
    }
}
