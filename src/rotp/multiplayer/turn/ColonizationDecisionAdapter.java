package rotp.multiplayer.turn;

import rotp.model.galaxy.ShipFleet;
import rotp.model.game.GameSession;
import rotp.model.ships.ShipDesign;

/** Presents a human colony ship choice to its controller. */
@FunctionalInterface
public interface ColonizationDecisionAdapter {
    void present(GameSession session, int systemId, ShipFleet fleet, ShipDesign design);
}
