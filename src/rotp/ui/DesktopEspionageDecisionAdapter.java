package rotp.ui;

import rotp.model.empires.EspionageMission;
import rotp.model.game.GameSession;
import rotp.multiplayer.turn.EspionageDecisionAdapter;

/** Keeps the existing single-player espionage selection panel. */
public final class DesktopEspionageDecisionAdapter implements EspionageDecisionAdapter {
    @Override
    public void present(GameSession session, EspionageMission mission, int victimEmpireId) {
        RotPUI.instance().selectEspionageMissionPanel(mission, victimEmpireId);
    }
}
