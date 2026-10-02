package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import rotp.multiplayer.hotseat.HotSeatSetup;
import rotp.multiplayer.hotseat.HotSeatSetup.Assignment;

class HotSeatSetupTest {
    @Test void namedNonadjacentHumansLeaveOtherEmpiresWithAi() {
        var setup = new HotSeatSetup(List.of(new Assignment("a", " Alice ", 0),
                new Assignment("b", "Bob", 2)));
        assertEquals("Alice", setup.humans().get(0).displayName());
        assertTrue(setup.registry(3).isHumanControlled(2));
        assertTrue(setup.registry(3).isAIControlled(1));
        assertThrows(IllegalArgumentException.class, () -> setup.registry(2));
        assertThrows(UnsupportedOperationException.class, () -> setup.humans().clear());
    }

    @Test void invalidOrAmbiguousAssignmentsAreRejected() {
        Assignment alice = new Assignment("a", "Alice", 0);
        assertThrows(IllegalArgumentException.class, () -> new Assignment(" ", "A", 0));
        assertThrows(IllegalArgumentException.class, () -> new Assignment("a", " ", 0));
        assertThrows(IllegalArgumentException.class, () -> new Assignment("a", "A", -1));
        assertThrows(IllegalArgumentException.class, () -> new HotSeatSetup(List.of(alice)));
        for (Assignment duplicate : List.of(new Assignment("a", "Bob", 1),
                new Assignment("b", " alice ", 1), new Assignment("b", "Bob", 0)))
            assertThrows(IllegalArgumentException.class,
                    () -> new HotSeatSetup(List.of(alice, duplicate)));
    }

    @Test void twoOrThreeHumansAreSupported() {
        var two = new HotSeatSetup(List.of(new Assignment("a", "Alice", 0),
                new Assignment("b", "Bob", 1)));
        assertEquals(2, two.registry(2).seats().size());
        var three = new HotSeatSetup(List.of(new Assignment("a", "Alice", 0),
                new Assignment("c", "Charlie", 2), new Assignment("d", "Dana", 3)));
        assertEquals(3, three.registry(4).seats().size());
        assertTrue(three.registry(4).isAIControlled(1));
    }
}
