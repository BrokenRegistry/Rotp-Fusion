package rotp.multiplayer.hotseat;

import java.io.Serializable;

public record HotSeatSaveEnvelope(int version, Kind kind, HotSeatSnapshot snapshot)
        implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Kind { PLANNING, DECISION, RESOLUTION_BOUNDARY, FINISHED }
}
