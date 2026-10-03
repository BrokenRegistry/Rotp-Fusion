package rotp.multiplayer.turn;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/** Research choices available to one human-controlled empire. */
public record PendingResearchDecision(String id, String ownerPlayerId, int empireId,
        int categoryIndex, List<String> legalTechIds) implements Serializable {
    private static final long serialVersionUID = 1L;

    public PendingResearchDecision {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
        legalTechIds = List.copyOf(legalTechIds);
    }
}
