package rotp.multiplayer.hotseat;

import java.io.Serializable;

/** Per-viewer presentation state; no shared sprite/model references. */
public record HotSeatViewState(float centerX, float centerY, float scale, Integer selectedSystemId)
        implements Serializable {
    private static final long serialVersionUID = 1L;
}
