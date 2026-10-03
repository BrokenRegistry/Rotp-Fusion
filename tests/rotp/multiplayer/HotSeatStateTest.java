package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import rotp.multiplayer.hotseat.HotSeatState;
import rotp.multiplayer.hotseat.HotSeatState.Stage;

class HotSeatStateTest {
    @Test void aRoundRequiresEveryHumanAndRejectsRepeatedFinish() {
        var state = new HotSeatState(List.of("alice", "bob"), 1);
        assertEquals(Stage.HANDOFF, state.snapshot().stage());
        assertTrue(state.confirmHandoff(state.snapshot().revision()));
        long click = state.snapshot().revision();
        assertFalse(state.finishPlanning("bob", click, Set.of("alice", "bob")));
        assertTrue(state.finishPlanning("alice", click, Set.of("alice", "bob")));
        assertEquals(Stage.HANDOFF, state.snapshot().stage());
        assertEquals("bob", state.snapshot().ownerPlayerId());
        assertFalse(state.finishPlanning("alice", click, Set.of("alice", "bob")));
        assertFalse(state.confirmHandoff(click));
        assertTrue(state.confirmHandoff(state.snapshot().revision()));
        assertTrue(state.finishPlanning("bob", state.snapshot().revision(), Set.of("alice", "bob")));
        assertEquals(Stage.RESOLVING, state.snapshot().stage());
        assertFalse(state.finishPlanning("bob", state.snapshot().revision(), Set.of("alice", "bob")));
        state.completeResolution(2, Set.of("alice", "bob"));
        assertEquals(2, state.snapshot().turn());
        assertEquals("alice", state.snapshot().ownerPlayerId());
        assertTrue(state.snapshot().finishedPlayers().isEmpty());
    }

    @Test void eliminatedSeatsAreSkippedWithoutReorderingSurvivors() {
        var state = new HotSeatState(List.of("a", "c", "d"), 4);
        state.confirmHandoff(state.snapshot().revision());
        assertTrue(state.finishPlanning("a", state.snapshot().revision(), Set.of("a", "d")));
        assertEquals("d", state.snapshot().ownerPlayerId());
        state.confirmHandoff(state.snapshot().revision());
        state.finishPlanning("d", state.snapshot().revision(), Set.of("d"));
        state.completeResolution(5, Set.of("d"));
        assertEquals("d", state.snapshot().ownerPlayerId());
        state.confirmHandoff(state.snapshot().revision());
        state.finishPlanning("d", state.snapshot().revision(), Set.of());
        assertEquals(Stage.FINISHED, state.snapshot().stage());
    }

    @Test void restoringPartialRoundCoversScreenAndKeepsFinishedSeats() throws Exception {
        var state = new HotSeatState(List.of("a", "b"), 3);
        state.confirmHandoff(state.snapshot().revision());
        state.finishPlanning("a", state.snapshot().revision(), Set.of("a", "b"));
        state.confirmHandoff(state.snapshot().revision());
        var restored = copy(state);
        assertEquals(state.snapshot(), restored.snapshot());
        long oldCallback = restored.snapshot().revision();
        restored.coverForLoad();
        assertFalse(restored.confirmHandoff(oldCallback));
        assertEquals(Set.of("a"), restored.snapshot().finishedPlayers());
        assertTrue(restored.confirmHandoff(restored.snapshot().revision()));
        restored.finishPlanning("b", restored.snapshot().revision(), Set.of("a", "b"));
        assertEquals(Stage.RESOLVING, restored.snapshot().stage());
    }

    @Test void aDecisionRequiresItsOwnerAndIdAndRestoresBehindAHandoff() throws Exception {
        var state = new HotSeatState(List.of("a", "b"), 1);
        state.beginDecision("b", "research:7", true);
        assertEquals(Stage.HANDOFF, state.snapshot().stage());
        state.confirmHandoff(state.snapshot().revision());
        assertEquals(Stage.DECISION, state.snapshot().stage());
        assertFalse(state.completeDecision("a", "research:7", state.snapshot().revision()));
        assertFalse(state.completeDecision("b", "research:6", state.snapshot().revision()));
        var restored = copy(state);
        restored.coverForLoad();
        assertEquals("research:7", restored.snapshot().pendingDecisionId());
        restored.confirmHandoff(restored.snapshot().revision());
        long reply = restored.snapshot().revision();
        assertTrue(restored.completeDecision("b", "research:7", reply));
        assertFalse(restored.completeDecision("b", "research:7", reply));
        assertEquals(Stage.RESOLVING, restored.snapshot().stage());
    }

    @Test void invalidRosterAndUnsafeRecoveryAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new HotSeatState(List.of(), 1));
        assertThrows(IllegalArgumentException.class, () -> new HotSeatState(List.of("a", "a"), 1));
        var state = new HotSeatState(List.of("a", "b"), 1);
        assertThrows(IllegalArgumentException.class, () -> state.beginDecision("ai", "x", true));
        state.beginDecision("a", "mission", false);
        assertFalse(state.snapshot().recoverable());
        state.failResolution();
        assertEquals(Stage.ERROR, state.snapshot().stage());
        assertFalse(state.confirmHandoff(state.snapshot().revision()));
        state.finishMatch();
        assertEquals(Stage.FINISHED, state.snapshot().stage());
    }

    private static HotSeatState copy(HotSeatState state) throws Exception {
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) { out.writeObject(state); }
        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (HotSeatState) in.readObject();
        }
    }
}
