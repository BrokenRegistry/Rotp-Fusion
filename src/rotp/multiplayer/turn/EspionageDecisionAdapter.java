package rotp.multiplayer.turn;

import rotp.model.empires.EspionageMission;
import rotp.model.game.GameSession;

/** Selects a stolen technology and optional empire to frame. */
public interface EspionageDecisionAdapter {
    void present(GameSession session, EspionageMission mission, int victimEmpireId);
}
