package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.model.game.*;
import rotp.multiplayer.hotseat.*;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatStartupTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @Test void invalidSetupCannotReplaceTheCurrentGalaxy() {
        var game = HotSeatTestFixture.start(1, 0);
        var original = game.galaxy();
        var options = game.options().copyAllOptions();
        var setup = new HotSeatSetup(List.of(new HotSeatSetup.Assignment("a", "Alice", 0),
                new HotSeatSetup.Assignment("b", "Bob", 2)));
        assertThrows(IllegalArgumentException.class, () -> game.startHotSeatGame(options, setup));
        assertSame(original, game.galaxy());
        assertNull(game.hotSeatSetup());
    }

    @Test void startupPersistsNamedOwnershipAndNormalStartupClearsIt() {
        var game = HotSeatTestFixture.start(1, 0);
        var options = game.options().copyAllOptions();
        var setup = new HotSeatSetup(List.of(new HotSeatSetup.Assignment("a", "Alice", 0),
                new HotSeatSetup.Assignment("b", "Bob", 1)));
        game.startHotSeatGame(options, setup);
        assertEquals(setup, game.hotSeatSetup());
        assertEquals(HotSeatState.Stage.HANDOFF, game.hotSeatState().snapshot().stage());
        assertEquals(game.galaxy().currentTurn(), game.hotSeatState().snapshot().turn());
        assertTrue(game.controllerRegistry().isHumanControlled(1));
        game.startGame(options);
        assertNull(game.hotSeatSetup());
        assertNull(game.hotSeatState());
        assertNull(game.controllerRegistry());
    }
}
