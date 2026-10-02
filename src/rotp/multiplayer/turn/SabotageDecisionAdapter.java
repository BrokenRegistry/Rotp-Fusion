package rotp.multiplayer.turn;

import rotp.model.empires.SabotageMission;
import rotp.model.game.GameSession;

/** Resolves an owned sabotage choice during the invasion phase. */
public interface SabotageDecisionAdapter {
    void resolve(GameSession session, SabotageMission mission, int suggestedSystemId);
}
