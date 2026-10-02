package rotp.multiplayer.turn;

import java.util.Objects;

/** Bombing choices available to the fleet's human owner. */
public record BombardmentDecision(String ownerPlayerId, int attackerEmpireId,
        int defenderEmpireId, int systemId, boolean targetAllowed,
        float targetPopulationLimit) {
    public BombardmentDecision {
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
    }
}
