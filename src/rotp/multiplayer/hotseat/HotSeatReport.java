package rotp.multiplayer.hotseat;

import java.io.Serializable;
import java.util.List;

/**
 * Recipient-formatted data. The optional payload lets the game's own report
 * screen replay it: a turn notice record, or scouted systems / built ships maps.
 */
public record HotSeatReport(long id, int turn, int recipientEmpireId,
        String kind, String title, List<String> lines, Serializable payload) implements Serializable {
    private static final long serialVersionUID = 1L;
    public HotSeatReport { lines = List.copyOf(lines); }
    public HotSeatReport(long id, int turn, int recipientEmpireId, String kind, String title, List<String> lines) {
        this(id, turn, recipientEmpireId, kind, title, lines, null);
    }
}
