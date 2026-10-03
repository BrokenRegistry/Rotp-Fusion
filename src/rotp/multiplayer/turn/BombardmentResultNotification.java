package rotp.multiplayer.turn;

import java.util.Objects;
import rotp.ui.notifications.TurnNotification;

/** In-process-only wrapper for a recipient-specific bombing result. */
public final class BombardmentResultNotification implements TurnNotification {
    private final BombardmentNotice notice;

    public BombardmentResultNotification(BombardmentNotice notice) {
        this.notice = Objects.requireNonNull(notice, "notice");
    }

    public BombardmentNotice notice() { return notice; }

    @Override public String displayOrder() { return PROMPT_BOMBARD; }
    @Override public void notifyPlayer() {
        throw new IllegalStateException("Bombardment result requires an in-process sink");
    }
}
