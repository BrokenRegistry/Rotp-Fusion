package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import rotp.Rotp;
import rotp.model.game.*;
import rotp.multiplayer.session.*;
import rotp.ui.RotPUI;
import rotp.util.Rand;

/** Test-owned engine lifetime; settings and saves never use personal folders. */
final class HotSeatTestFixture {
    static void initialize(Path directory) throws Exception {
        Rotp.rand(new Rand(136));
        Files.writeString(directory.resolve("Remnants.cfg"), "GRAPHICS: Low\n");
        set(Rotp.class, null, "startupDir", directory + File.separator);
        set(Rotp.class, null, "isIDE", false);
        JFrame frame = new JFrame();
        frame.setSize(1200, 800);
        set(Rotp.class, null, "frame", frame);
        var init = Rotp.class.getDeclaredMethod("initUtils");
        init.setAccessible(true);
        init.invoke(null);
        assertNull(Rotp.startupException);
        RulesetManager.current();
        RotPUI ui = new RotPUI();
        frame.add(ui);
        ui.initModel();
        IGameOptions.galaxyRandSource.set(136);
        set(Rotp.class, null, "initialized", true);
        Field timer = RotPUI.class.getDeclaredField("timer");
        timer.setAccessible(true);
        onEdt(() -> {
            ui.init();
            try { ((Timer) timer.get(ui)).stop(); }
            catch (IllegalAccessException ex) { throw new IllegalStateException(ex); }
        });
        IMainOptions.saveDirectory.set(directory.toString());
    }

    static GameSession start(int opponents, int... humans) {
        Rotp.rand(new Rand(136));
        IGameOptions options = RulesetManager.current().newOptions().copyAllOptions();
        options.selectedGalaxySize(IGameOptions.SIZE_TINY);
        options.selectedNumberOpponents(opponents);
        options.selectedCouncilWinOption(IGameOptions.COUNCIL_REBELS);
        ControllerRegistry registry = new ControllerRegistry();
        for (int empire : humans)
            registry.add(new PlayerSeat("human-" + empire, empire,
                    PlayerSeat.ControllerType.HUMAN, PlayerSeat.ConnectionStatus.CONNECTED));
        Rotp.rand(new Rand(136));
        GameSession session = GameSession.instance();
        session.startGame(options, registry);
        return session;
    }

    static void onEdt(Runnable action) {
        try {
            if (SwingUtilities.isEventDispatchThread()) action.run();
            else SwingUtilities.invokeAndWait(action);
        }
        catch (Exception failure) { throw new AssertionError(failure); }
    }

    static void set(Class<?> type, Object object, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
    }
}
