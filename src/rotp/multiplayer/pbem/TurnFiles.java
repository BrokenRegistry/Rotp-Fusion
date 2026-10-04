package rotp.multiplayer.pbem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** Names that tell players whose turn a file holds; each handoff gets its own file. */
public final class TurnFiles {
    private TurnFiles() { }

    public static String matchLabel(LocalDateTime created) {
        return "PBEM-" + created.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
                + "-" + UUID.randomUUID();
    }

    /** The handoff revision keeps a turn's planning and decision files apart. */
    public static String fileName(String matchLabel, int turn, long revision, String playerName) {
        return String.format("%s-T%03d-r%04d-for-%s.rotp", matchLabel, turn, revision, safe(playerName));
    }

    /** The finished match, for every player to open and see the result. */
    public static String finalFileName(String matchLabel, int turn) {
        return String.format("%s-T%03d-final.rotp", matchLabel, turn);
    }

    /** Keeps the name as typed, replacing only characters Windows forbids in file names. */
    static String safe(String playerName) {
        String name = playerName.replaceAll("[<>:\"/\\\\|?*\\p{Cntrl}]", "_").trim()
                .replaceAll("[. ]+$", "");
        return name.isEmpty() ? "player" : name;
    }
}
