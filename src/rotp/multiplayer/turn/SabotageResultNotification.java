package rotp.multiplayer.turn;

import java.util.Objects;
import rotp.ui.notifications.TurnNotification;

/** In-process-only wrapper for a recipient-specific sabotage result. */
public final class SabotageResultNotification implements TurnNotification {
    private final SabotageNotice notice;

    public SabotageResultNotification(SabotageNotice notice) {
        this.notice = Objects.requireNonNull(notice, "notice");
    }

    public SabotageNotice notice() { return notice; }

    @Override public String displayOrder() { return SABOTAGE_RESULT; }
    @Override public void notifyPlayer() {
        throw new IllegalStateException("Sabotage result requires an in-process sink");
    }
}
