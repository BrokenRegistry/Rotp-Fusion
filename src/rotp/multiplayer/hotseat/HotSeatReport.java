package rotp.multiplayer.hotseat;

import java.io.Serializable;
import java.util.List;

/** Recipient-formatted data, with no mutable model or UI references. */
public record HotSeatReport(long id, int turn, int recipientEmpireId,
        String kind, String title, List<String> lines) implements Serializable {
    private static final long serialVersionUID = 1L;
    public HotSeatReport { lines = List.copyOf(lines); }
}
