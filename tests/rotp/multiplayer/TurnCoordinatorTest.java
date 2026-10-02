package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import rotp.multiplayer.turn.TurnCoordinator;
import rotp.multiplayer.turn.TurnCoordinator.Phase;

class TurnCoordinatorTest {
    @Test
    void restoredBoundaryContinuesWithoutRepeatingMovement() throws Exception {
        TurnCoordinator original = new TurnCoordinator();
        original.startTurn(7);
        List<String> effects = new ArrayList<>();
        original.run(Phase.PREPARE, () -> effects.add("prepare"));
        original.run(Phase.LAUNCH, () -> effects.add("launch"));
        original.run(Phase.MOVEMENT, () -> effects.add("move"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        TurnCoordinator restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (TurnCoordinator) in.readObject();
        }
        assertEquals(7, restored.turn());
        assertTrue(restored.atSafeBoundary());
        assertEquals(Phase.COUNCIL, restored.nextPhase());
        assertThrows(IllegalStateException.class,
                () -> restored.run(Phase.MOVEMENT, () -> effects.add("move again")));
        restored.run(Phase.COUNCIL, () -> effects.add("vote"));
        assertEquals(List.of("prepare", "launch", "move", "vote"), effects);
        assertEquals(Phase.EMPIRE_TURNS, restored.nextPhase());
    }

    @Test
    void failedModelWorkCannotBeRetriedAsASafeBoundary() {
        TurnCoordinator coordinator = new TurnCoordinator();
        coordinator.startTurn(1);
        assertThrows(IllegalArgumentException.class, () -> coordinator.run(Phase.PREPARE, () -> {
            throw new IllegalArgumentException("model failure");
        }));
        assertFalse(coordinator.atSafeBoundary());
        assertEquals(Phase.PREPARE, coordinator.activePhase());
        assertNull(coordinator.lastCompletedPhase());
        assertThrows(IllegalStateException.class, () -> coordinator.run(Phase.PREPARE, () -> fail("replayed")));
        assertThrows(IllegalStateException.class, () -> coordinator.startTurn(2));
    }

    @Test
    void incompleteTurnsAndOutOfOrderPhasesCannotSkipWork() {
        TurnCoordinator coordinator = new TurnCoordinator();
        assertThrows(IllegalStateException.class, () -> coordinator.run(Phase.PREPARE, () -> fail("unstarted")));
        coordinator.startTurn(1);
        assertThrows(IllegalStateException.class, () -> coordinator.startTurn(2));
        assertThrows(IllegalStateException.class, () -> coordinator.run(Phase.MOVEMENT, () -> fail("skipped")));
        for (Phase phase : Phase.values())
            coordinator.run(phase, () -> {});
        assertNull(coordinator.nextPhase());
        assertThrows(IllegalArgumentException.class, () -> coordinator.startTurn(1));
        coordinator.startTurn(2);
        assertEquals(Phase.PREPARE, coordinator.nextPhase());
    }
}
