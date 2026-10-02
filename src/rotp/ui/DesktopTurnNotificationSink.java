package rotp.ui;

import java.util.ArrayList;
import java.util.List;

import rotp.model.game.GameSession;
import rotp.model.empires.Empire;
import rotp.multiplayer.turn.QueuedTurnNotification;
import rotp.multiplayer.turn.TurnNotificationSink;
import rotp.ui.notifications.TurnNotification;

/** Preserves the existing desktop notification sequence. */
public final class DesktopTurnNotificationSink implements TurnNotificationSink {
    @Override
    public void deliver(GameSession session, List<QueuedTurnNotification> notifications) {
        List<TurnNotification> localNotifications = new ArrayList<>(notifications.size());
        for (QueuedTurnNotification entry : notifications) {
            if (session.controllerRegistry() != null
                    && (entry.recipientEmpireId() == null
                            || entry.recipientEmpireId() != Empire.PLAYER_ID))
                throw new UnsupportedOperationException(
                        "Desktop notification has no local recipient: "
                                + entry.notification().getClass().getName());
            localNotifications.add(entry.notification());
        }
        RotPUI.instance().processNotifications(localNotifications);
    }
}
