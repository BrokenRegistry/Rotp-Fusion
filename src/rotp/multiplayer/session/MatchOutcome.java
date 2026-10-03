package rotp.multiplayer.session;

import java.io.Serializable;
import java.util.Map;

/** Match result and each human seat's standing at the current model state. */
public record MatchOutcome(boolean finished, Cause cause,
        Map<String, SeatResult> resultsByPlayer) implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Cause { ONGOING, MILITARY, MILITARY_ALLIANCE, COUNCIL, NO_HUMANS_REMAIN }
    public enum SeatResult { ACTIVE, ELIMINATED, WON, LOST }

    public MatchOutcome {
        resultsByPlayer = Map.copyOf(resultsByPlayer);
    }
}
