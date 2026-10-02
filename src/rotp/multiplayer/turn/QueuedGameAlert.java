package rotp.multiplayer.turn;

import java.util.Objects;

import rotp.ui.notifications.GameAlert;

/** Session-local desktop alert and its known recipient empire. */
public record QueuedGameAlert(GameAlert alert, Integer recipientEmpireId) {
    public QueuedGameAlert {
        Objects.requireNonNull(alert, "alert");
    }
}
