package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import org.junit.jupiter.api.Test;
import rotp.multiplayer.session.ControllerRegistry;
import rotp.multiplayer.session.PlayerSeat;
import rotp.multiplayer.session.PlayerSeat.ConnectionStatus;
import rotp.multiplayer.session.PlayerSeat.ControllerType;

class ControllerRegistryTest {
    @Test
    void disconnectedHumanRemainsHumanAcrossSerialization() throws Exception {
        ControllerRegistry registry = new ControllerRegistry();
        registry.add(new PlayerSeat("alice", 0, ControllerType.HUMAN, ConnectionStatus.CONNECTED));
        registry.add(new PlayerSeat("bob", 1, ControllerType.HUMAN, ConnectionStatus.DISCONNECTED));
        registry.add(new PlayerSeat("computer", 2, ControllerType.AI, ConnectionStatus.NOT_APPLICABLE));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(registry);
        }
        ControllerRegistry restored;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (ControllerRegistry) in.readObject();
        }
        assertTrue(restored.isHumanControlled(0));
        assertTrue(restored.isHumanControlled(1));
        assertFalse(restored.isAIControlled(1));
        assertEquals("bob", restored.seatForEmpire(1).playerId());
        assertTrue(restored.isAIControlled(2));
        assertTrue(restored.isAIControlled(3));
    }

    @Test
    void duplicateAssignmentsCannotReplaceAnExistingOwner() {
        ControllerRegistry registry = new ControllerRegistry();
        PlayerSeat alice = new PlayerSeat("alice", 0, ControllerType.HUMAN, ConnectionStatus.CONNECTED);
        registry.add(alice);
        assertThrows(IllegalArgumentException.class, () -> registry.add(
                new PlayerSeat("bob", 0, ControllerType.HUMAN, ConnectionStatus.CONNECTED)));
        assertThrows(IllegalArgumentException.class, () -> registry.add(
                new PlayerSeat("alice", 1, ControllerType.HUMAN, ConnectionStatus.CONNECTED)));
        assertEquals(alice, registry.seatForEmpire(0));
        assertNull(registry.seatForEmpire(1));
        assertEquals(1, registry.seats().size());
        assertThrows(UnsupportedOperationException.class, () -> registry.seats().clear());
    }
}
