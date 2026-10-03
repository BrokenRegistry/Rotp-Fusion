package rotp.multiplayer.turn;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import rotp.model.empires.EspionageMission;

/** Choices visible to the human who ran an espionage mission. */
public record EspionageDecision(String ownerPlayerId, int ownerEmpireId,
        int victimEmpireId, int targetSystemId, Map<String, String> categoryTechIds,
        List<Integer> frameableEmpireIds, EspionageMission mission) {
    public EspionageDecision {
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
        categoryTechIds = Map.copyOf(categoryTechIds);
        frameableEmpireIds = List.copyOf(frameableEmpireIds);
    }
    /** Without the live mission, which only the game's own screens need. */
    public EspionageDecision(String ownerPlayerId, int ownerEmpireId, int victimEmpireId,
            int targetSystemId, Map<String, String> categoryTechIds, List<Integer> frameableEmpireIds) {
        this(ownerPlayerId, ownerEmpireId, victimEmpireId, targetSystemId, categoryTechIds,
                frameableEmpireIds, null);
    }
}
