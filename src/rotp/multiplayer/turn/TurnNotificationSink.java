package rotp.multiplayer.turn;

import java.util.List;

import rotp.model.game.GameSession;

/** Delivers the ordered notifications produced during one turn segment. */
@FunctionalInterface
public interface TurnNotificationSink {
    void deliver(GameSession session, List<QueuedTurnNotification> notifications);
}
