package rotp.multiplayer.hotseat;

import rotp.model.game.GameSession;

/** UI-only order entry guard. Simulation code retains its existing model paths. */
public final class HotSeatOrders {
    private HotSeatOrders() { }
    public static boolean canEdit(int owner) {
        var game = GameSession.instance();
        return game.hotSeatState() == null || game.hotSeatController() != null
                && game.hotSeatController().canEdit(owner);
    }
}
