package rotp.multiplayer.hotseat;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import rotp.model.game.GameSession;

/** Save only model boundaries, never a live synchronous mission stack. */
public final class HotSeatPersistence {
    private HotSeatPersistence() { }
    public static boolean canSave(GameSession game) {
        if (game != GameSession.instance() || game.hotSeatState() == null
                || !game.hotSeatState().snapshot().recoverable() || !game.hotSeatModelSaveable()) return false;
        var controller = game.hotSeatController();
        return controller == null || controller.safeToSave();
    }
    public static long save(GameSession game, File destination) throws Exception {
        if (!canSave(game)) throw new IllegalStateException("Finish this choice before saving.");
        if (SwingUtilities.isEventDispatchThread()) game.captureHotSeatView();
        return game.writeHotSeatEnvelope(destination);
    }
    public static GameSession load(File source) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) return GameSession.restoreHotSeatEnvelope(source);
        AtomicReference<GameSession> result = new AtomicReference<>();
        AtomicReference<Exception> failure = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try { result.set(GameSession.restoreHotSeatEnvelope(source)); }
            catch (Exception error) { failure.set(error); }
        });
        if (failure.get() != null) throw failure.get();
        return result.get();
    }
}
