package rotp.multiplayer.pbem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Names that tell players whose turn a file holds. */
public final class TurnFiles {
    private TurnFiles() { }

    public static String matchLabel(LocalDateTime created) {
        return "PBEM-" + created.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
    }

    public static String fileName(String matchLabel, int turn, String playerName) {
        String name = playerName.trim().replaceAll("[^A-Za-z0-9_-]+", "_").replaceAll("^_+|_+$", "");
        if (name.isEmpty()) name = "player";
        return String.format("%s-T%03d-for-%s.rotp", matchLabel, turn, name);
    }
}
