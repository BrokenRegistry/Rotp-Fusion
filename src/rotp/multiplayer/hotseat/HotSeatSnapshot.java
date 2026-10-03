package rotp.multiplayer.hotseat;

import java.io.Serializable;
import java.util.Set;

/** Immutable scheduler view. A revision belongs to exactly one screen/action. */
public record HotSeatSnapshot(int turn, long revision, HotSeatState.Stage stage,
        String ownerPlayerId, Set<String> finishedPlayers, String pendingDecisionId,
        boolean recoverable) implements Serializable {
    private static final long serialVersionUID = 1L;
    public HotSeatSnapshot { finishedPlayers = Set.copyOf(finishedPlayers); }
}
