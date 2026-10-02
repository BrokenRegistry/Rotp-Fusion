package rotp.multiplayer.turn;

import java.util.Objects;

import rotp.ui.notifications.TurnNotification;

/** A turn notice and the empire entitled to receive it, when known. */
public record QueuedTurnNotification(TurnNotification notification, Integer recipientEmpireId) {
    public QueuedTurnNotification {
        Objects.requireNonNull(notification, "notification");
    }
}
