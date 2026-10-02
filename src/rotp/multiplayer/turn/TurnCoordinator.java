package rotp.multiplayer.turn;

import java.io.Serializable;
import java.util.Objects;

/**
 * Records the ordered simulation phases of one turn. An active phase is
 * diagnostic state, not a safe restart point until its decisions are extracted.
 */
public final class TurnCoordinator implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Phase {
        PREPARE,
        LAUNCH,
        MOVEMENT,
        COUNCIL,
        EMPIRE_TURNS,
        SPACE_COMBAT,
        INVASIONS,
        POST_COLONIZATION,
        DIPLOMACY,
        DECISIONS,
        REFRESH
    }

    private int turn = -1;
    private Phase activePhase;
    private Phase lastCompletedPhase;

    public int turn() { return turn; }
    public Phase activePhase() { return activePhase; }
    public Phase lastCompletedPhase() { return lastCompletedPhase; }
    public Phase nextPhase() {
        int ordinal = nextPhaseOrdinal();
        return ordinal < Phase.values().length ? Phase.values()[ordinal] : null;
    }
    public boolean atSafeBoundary() { return turn >= 0 && activePhase == null; }

    public void startTurn(int turn) {
        if (activePhase != null)
            throw new IllegalStateException("Turn phase is still active: " + activePhase);
        if (this.turn >= 0 && nextPhase() != null)
            throw new IllegalStateException("Previous turn is incomplete at " + nextPhase());
        if (this.turn >= 0 && turn <= this.turn)
            throw new IllegalArgumentException("Turn must advance beyond " + this.turn);
        this.turn = turn;
        lastCompletedPhase = null;
    }

    /** Legacy desktop continuation after a game-over exit, never for rostered play. */
    public void abandonTerminalTurn() {
        if (activePhase != null)
            throw new IllegalStateException("Cannot abandon active phase " + activePhase);
        turn = -1;
        lastCompletedPhase = null;
    }

    public void run(Phase phase, Runnable work) {
        Objects.requireNonNull(phase, "phase");
        Objects.requireNonNull(work, "work");
        if (turn < 0 || activePhase != null || phase.ordinal() != nextPhaseOrdinal())
            throw new IllegalStateException("Unexpected turn phase: " + phase);
        activePhase = phase;
        work.run();
        lastCompletedPhase = phase;
        activePhase = null;
    }

    private int nextPhaseOrdinal() {
        return lastCompletedPhase == null ? 0 : lastCompletedPhase.ordinal() + 1;
    }
}
