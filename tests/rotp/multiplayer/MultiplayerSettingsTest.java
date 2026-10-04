package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.model.game.IConvenienceOptions;
import rotp.multiplayer.hotseat.HotSeatSetup;
import rotp.ui.options.AllSubUI;
import rotp.ui.options.ISubUiKeys;
import rotp.ui.options.MultiplayerDisplayOptions;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class MultiplayerSettingsTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @Test void onlyDisplayScreensOpenDuringAMatch() {
        for (String display : List.of(MultiplayerDisplayOptions.OPTION_ID, ISubUiKeys.VISUAL_OPTIONS_UI_KEY,
                ISubUiKeys.ZOOM_OPTIONS_UI_KEY, ISubUiKeys.FLAG_OPTIONS_UI_KEY, ISubUiKeys.HELP_AND_ADVICE_UI_KEY))
            assertTrue(MultiplayerDisplayOptions.displayOnly(display), display);
        for (String gameplay : List.of(ISubUiKeys.GOVERNOR_UI_KEY, ISubUiKeys.DEBUG_OPTIONS_UI_KEY,
                ISubUiKeys.GAME_AUTOMATION_UI_KEY, ISubUiKeys.SETTINGS_OPTIONS_UI_KEY))
            assertFalse(MultiplayerDisplayOptions.displayOnly(gameplay), gameplay);
        assertTrue(MultiplayerDisplayOptions.replacesDuringMatch(ISubUiKeys.SETTINGS_OPTIONS_UI_KEY));
        var map = AllSubUI.getHandle(MultiplayerDisplayOptions.OPTION_ID).optionsMap();
        assertTrue(map.listSizeNoSpacer() > 10, "The display screen lists the display groups");
    }

    @Test void gameplaySwitchesOnOpenScreensAreFixedForTheMatch() {
        boolean colonize = IConvenienceOptions.autoColonize_.get();
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1))));
        IConvenienceOptions.autoColonize_.set(!colonize);
        assertEquals(colonize, IConvenienceOptions.autoColonize_.get(), "Auto-colonize is shared by every empire");
    }
}
