package rotp.multiplayer.turn;

import rotp.model.galaxy.ShipFleet;
import rotp.model.game.GameSession;

/** Resolves a bombing action without relying on the local desktop viewer. */
public interface BombardmentDecisionAdapter {
    void resolve(GameSession session, int systemId, ShipFleet fleet,
            boolean autoBomb, int bombingTarget);
}
