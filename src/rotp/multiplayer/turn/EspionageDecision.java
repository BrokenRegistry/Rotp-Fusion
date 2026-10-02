package rotp.multiplayer.turn;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Choices visible to the human who ran an espionage mission. */
public record EspionageDecision(String ownerPlayerId, int ownerEmpireId,
        int victimEmpireId, int targetSystemId, Map<String, String> categoryTechIds,
        List<Integer> frameableEmpireIds) {
    public EspionageDecision {
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
        categoryTechIds = Map.copyOf(categoryTechIds);
        frameableEmpireIds = List.copyOf(frameableEmpireIds);
    }
}
