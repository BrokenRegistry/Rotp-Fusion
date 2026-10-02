package rotp.multiplayer.hotseat;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Persisted turn ownership; never stores a UI callback or running thread. */
public final class HotSeatState implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Stage { HANDOFF, PLANNING, RESOLVING, DECISION, FINISHED, ERROR }

    private final List<String> order;
    private final Set<String> finished = new HashSet<>();
    private int turn;
    private long revision;
    private Stage stage = Stage.HANDOFF;
    private Stage afterHandoff = Stage.PLANNING;
    private String owner;
    private String decisionId;
    private boolean recoverable = true;

    public HotSeatState(List<String> orderedPlayerIds, int turn) {
        Objects.requireNonNull(orderedPlayerIds, "players");
        if (orderedPlayerIds.size() < 2 || turn < 0
                || orderedPlayerIds.stream().anyMatch(id -> id == null || id.isBlank())
                || new HashSet<>(orderedPlayerIds).size() != orderedPlayerIds.size())
            throw new IllegalArgumentException("Two or more distinct human players are required");
        order = List.copyOf(orderedPlayerIds);
        this.turn = turn;
        owner = order.get(0);
    }

    public synchronized HotSeatSnapshot snapshot() {
        return new HotSeatSnapshot(turn, revision, stage, owner, finished, decisionId, recoverable);
    }

    public synchronized boolean confirmHandoff(long expectedRevision) {
        if (stage != Stage.HANDOFF || revision != expectedRevision) return false;
        stage = afterHandoff;
        revision++;
        return true;
    }

    public synchronized boolean finishPlanning(String playerId, long expectedRevision,
            Set<String> livingPlayers) {
        if (stage != Stage.PLANNING || revision != expectedRevision || !Objects.equals(owner, playerId))
            return false;
        validateLiving(livingPlayers);
        finished.add(playerId);
        String next = order.stream().filter(livingPlayers::contains)
                .filter(id -> !finished.contains(id)).findFirst().orElse(null);
        if (livingPlayers.isEmpty()) finishMatch();
        else if (next == null) {
            stage = Stage.RESOLVING;
            owner = null;
            recoverable = false;
            revision++;
        }
        else handoff(next, Stage.PLANNING);
        return true;
    }

    public synchronized void beginDecision(String ownerPlayerId, String id, boolean safe) {
        if (!order.contains(ownerPlayerId) || id == null || id.isBlank())
            throw new IllegalArgumentException("A decision requires its human owner and ID");
        if (decisionId != null || stage == Stage.FINISHED || stage == Stage.ERROR)
            throw new IllegalStateException("Cannot replace pending or terminal work");
        decisionId = id;
        recoverable = safe;
        handoff(ownerPlayerId, Stage.DECISION);
    }

    public synchronized boolean completeDecision(String playerId, String id, long expectedRevision) {
        if (stage != Stage.DECISION || revision != expectedRevision
                || !Objects.equals(owner, playerId) || !Objects.equals(decisionId, id))
            return false;
        decisionId = null;
        owner = null;
        stage = Stage.RESOLVING;
        recoverable = false;
        revision++;
        return true;
    }

    public synchronized void completeResolution(int nextTurn, Set<String> livingPlayers) {
        validateLiving(livingPlayers);
        if (nextTurn < turn || decisionId != null || stage != Stage.RESOLVING)
            throw new IllegalStateException("Resolution is not complete");
        turn = nextTurn;
        finished.clear();
        recoverable = true;
        if (livingPlayers.isEmpty()) finishMatch();
        else handoff(order.stream().filter(livingPlayers::contains).findFirst().orElseThrow(), Stage.PLANNING);
    }

    public synchronized void finishMatch() {
        stage = Stage.FINISHED;
        owner = null;
        decisionId = null;
        recoverable = true;
        revision++;
    }
    public synchronized void resolutionBoundary(boolean safe) {
        if (stage != Stage.RESOLVING || decisionId != null)
            throw new IllegalStateException("Only resolution can enter a phase boundary");
        recoverable = safe;
    }

    public synchronized void failResolution() {
        stage = Stage.ERROR;
        recoverable = false;
        revision++;
    }

    public synchronized void coverForLoad() {
        if (stage == Stage.PLANNING || stage == Stage.DECISION) {
            afterHandoff = stage;
            stage = Stage.HANDOFF;
        }
        revision++;
    }

    private void handoff(String playerId, Stage destination) {
        owner = playerId;
        stage = Stage.HANDOFF;
        afterHandoff = destination;
        revision++;
    }

    private void validateLiving(Set<String> living) {
        if (living == null || !order.containsAll(living))
            throw new IllegalArgumentException("Living seats must belong to this match");
    }
}
