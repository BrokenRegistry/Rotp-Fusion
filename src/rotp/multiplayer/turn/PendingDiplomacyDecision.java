package rotp.multiplayer.turn;

import java.io.Serializable;
import java.util.Objects;

/** An owned diplomatic offer awaiting an accept or refuse response. */
public record PendingDiplomacyDecision(String id, DiplomacyNotice notice)
        implements Serializable {
    private static final long serialVersionUID = 1L;

    public PendingDiplomacyDecision {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(notice, "notice");
        if (!notice.responseRequired())
            throw new IllegalArgumentException("Diplomatic information is not a decision");
    }
}
