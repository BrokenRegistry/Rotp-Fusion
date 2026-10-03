package rotp.ui;

import rotp.model.galaxy.ShipFleet;
import rotp.model.game.GameSession;
import rotp.model.ships.ShipDesign;
import rotp.multiplayer.turn.ColonizationDecisionAdapter;

/** Keeps the established local colonization panel. */
public final class DesktopColonizationDecisionAdapter implements ColonizationDecisionAdapter {
    @Override
    public void present(GameSession session, int systemId, ShipFleet fleet, ShipDesign design) {
        RotPUI.instance().promptForColonization(systemId, fleet, design);
    }
}
