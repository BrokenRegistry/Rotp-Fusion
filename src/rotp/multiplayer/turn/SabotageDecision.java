package rotp.multiplayer.turn;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import rotp.model.empires.SabotageMission;
import rotp.model.empires.SpyNetwork.Sabotage;

/** The systems currently legal for each sabotage action. */
public record SabotageDecision(String ownerPlayerId, int ownerEmpireId,
        int victimEmpireId, int suggestedSystemId,
        Map<Sabotage, List<Integer>> targetSystemIds, SabotageMission mission) {
    public SabotageDecision {
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
        targetSystemIds = Map.copyOf(targetSystemIds);
    }
    /** Without the live mission, which only the game's own screen needs. */
    public SabotageDecision(String ownerPlayerId, int ownerEmpireId, int victimEmpireId,
            int suggestedSystemId, Map<Sabotage, List<Integer>> targetSystemIds) {
        this(ownerPlayerId, ownerEmpireId, victimEmpireId, suggestedSystemId, targetSystemIds, null);
    }
}
