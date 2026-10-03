package rotp.multiplayer.pbem;

import java.io.Serializable;
import java.util.Objects;

/** One player's answers for choices that arrive while they are away. */
public record StandingOrders(Bombard bombard, Frame frame, SabotageTarget sabotage)
        implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Bombard { NEVER, ALWAYS, AT_WAR, AT_WAR_NOT_INVADING }
    public enum Frame { NEVER, WHEN_POSSIBLE }
    public enum SabotageTarget { FACTORIES, MISSILES, REBELS }

    public static final StandingOrders DEFAULT =
            new StandingOrders(Bombard.AT_WAR, Frame.NEVER, SabotageTarget.FACTORIES);

    public StandingOrders {
        Objects.requireNonNull(bombard, "bombard");
        Objects.requireNonNull(frame, "frame");
        Objects.requireNonNull(sabotage, "sabotage");
    }
}
