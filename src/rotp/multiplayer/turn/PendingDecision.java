package rotp.multiplayer.turn;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/** Public choices and the seat authorized to answer a turn decision. */
public record PendingDecision(String id, String ownerPlayerId, int turn, int empireId,
        Kind kind, List<Integer> legalEmpireIds, boolean abstainAllowed) implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Kind { COUNCIL_VOTE, COUNCIL_RULING }

    public PendingDecision {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
        Objects.requireNonNull(kind, "kind");
        legalEmpireIds = List.copyOf(legalEmpireIds);
    }
}
