package rotp.multiplayer.turn;

import java.io.Serializable;
import java.util.Objects;

/** Recipient-specific alert text captured when the alert is created. */
public record AlertRecord(int recipientEmpireId, String type,
        String description, int systemId) implements Serializable {
    private static final long serialVersionUID = 1L;

    public AlertRecord {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(description, "description");
    }
}
