package rotp.multiplayer.turn;

import java.util.Objects;

import rotp.model.galaxy.ShipFleet;

/** Bombing choices available to the fleet's human owner. */
public record BombardmentDecision(String ownerPlayerId, int attackerEmpireId,
        int defenderEmpireId, int systemId, boolean targetAllowed,
        float targetPopulationLimit, ShipFleet fleet) {
    public BombardmentDecision {
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
    }
    /** Without the live fleet, which only the game's own prompt needs. */
    public BombardmentDecision(String ownerPlayerId, int attackerEmpireId, int defenderEmpireId,
            int systemId, boolean targetAllowed, float targetPopulationLimit) {
        this(ownerPlayerId, attackerEmpireId, defenderEmpireId, systemId, targetAllowed,
                targetPopulationLimit, null);
    }
}
