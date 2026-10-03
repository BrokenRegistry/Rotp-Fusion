package rotp.multiplayer.turn;

import java.io.Serializable;
import java.util.Objects;

/** The owner and target of a colony ship choice. */
public record PendingColonizationDecision(String id, String ownerPlayerId,
        int empireId, int systemId, int designId) implements Serializable {
    private static final long serialVersionUID = 1L;

    public PendingColonizationDecision {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
    }
}
