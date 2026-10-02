package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import rotp.multiplayer.hotseat.HotSeatInbox;

class HotSeatInboxTest {
    @Test void reportsArePrivateAndAcknowledgedOnlyOnce() {
        var inbox = new HotSeatInbox();
        long id = inbox.append(1, 0, "SCOUT", "Scouting", List.of("Alice's discovery"));
        assertTrue(inbox.unread(1).isEmpty());
        assertFalse(inbox.acknowledge(1, id));
        assertEquals(1, inbox.unread(0).size());
        assertTrue(inbox.acknowledge(0, id));
        assertFalse(inbox.acknowledge(0, id));
        assertTrue(inbox.unread(0).isEmpty());
    }

    @Test void savedReportsKeepRecipientsAcknowledgementsAndMonotonicIds() throws Exception {
        var inbox = new HotSeatInbox();
        long first = inbox.append(1, 0, "TECH", "Research", List.of("Secret A"));
        long second = inbox.append(1, 1, "TECH", "Research", List.of("Secret B"));
        inbox.acknowledge(0, first);
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) { out.writeObject(inbox); }
        HotSeatInbox restored;
        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (HotSeatInbox) in.readObject();
        }
        assertTrue(restored.unread(0).isEmpty());
        assertEquals(List.of("Secret B"), restored.unread(1).get(0).lines());
        assertThrows(UnsupportedOperationException.class, () -> restored.unread(1).clear());
        assertTrue(restored.append(2, 0, "SCOUT", "Scouting", List.of()) > second);
        assertFalse(restored.acknowledge(0, second));
    }
}
