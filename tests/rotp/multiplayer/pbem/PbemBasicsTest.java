package rotp.multiplayer.pbem;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PbemBasicsTest {
    @Test void pinMatchesOnlyItselfAndSurvivesSerialization() throws Exception {
        PinHash pin = PinHash.create("4271");
        assertTrue(pin.matches("4271"));
        assertFalse(pin.matches("4272"));
        assertFalse(pin.matches(null));
        var bytes = new ByteArrayOutputStream();
        try (var out = new ObjectOutputStream(bytes)) { out.writeObject(pin); }
        try (var in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            assertTrue(((PinHash) in.readObject()).matches("4271"));
        }
    }

    @Test void pinLengthIsBounded() {
        assertFalse(PinHash.acceptable("123"));
        assertFalse(PinHash.acceptable("12 45"));
        assertFalse(PinHash.acceptable("1234567890123"));
        assertTrue(PinHash.acceptable("1234"));
        assertThrows(IllegalArgumentException.class, () -> PinHash.create("12"));
    }

    @Test void turnFileNamesAreSafeAndSortable() {
        String label = TurnFiles.matchLabel(LocalDateTime.of(2026, 10, 3, 14, 5));
        assertEquals("PBEM-20261003-1405", label);
        assertEquals("PBEM-20261003-1405-T012-r0007-for-Bob.rotp", TurnFiles.fileName(label, 12, 7, "Bob"));
        assertEquals("PBEM-20261003-1405-T003-r0012-for-Mary Ann_.._.rotp", TurnFiles.fileName(label, 3, 12, " Mary Ann/../ "));
        assertEquals("PBEM-20261003-1405-T003-r0001-for-player.rotp", TurnFiles.fileName(label, 3, 1, " ... "));
        assertEquals("PBEM-20261003-1405-T009-final.rotp", TurnFiles.finalFileName(label, 9));
    }

    @Test void turnFileNamesNeverCollide() {
        String label = "PBEM-20261003-1405";
        assertNotEquals(TurnFiles.fileName(label, 3, 5, "Mary Ann"), TurnFiles.fileName(label, 3, 5, "Mary_Ann"),
                "Different players stay distinct");
        assertNotEquals(TurnFiles.fileName(label, 3, 5, "Alice"), TurnFiles.fileName(label, 3, 9, "Alice"),
                "A planning file and a later choice in the same turn stay distinct");
    }

    @Test void buildMismatchIsReportedWithBothBuilds() {
        assertDoesNotThrow(() -> BuildStamp.requireMatch("v1 2026/10/03", "v1 2026/10/03"));
        assertDoesNotThrow(() -> BuildStamp.requireMatch(null, "v1"));
        var mismatch = assertThrows(BuildStamp.MismatchException.class,
                () -> BuildStamp.requireMatch("v1 old", "v1 new"));
        assertEquals("v1 old", mismatch.fileBuild);
        assertEquals("v1 new", mismatch.localBuild);
    }
}
