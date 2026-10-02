package rotp.ui;

import rotp.model.empires.GalacticCouncil;
import rotp.model.game.GameSession;
import rotp.multiplayer.turn.CouncilDecisionAdapter;
import rotp.ui.notifications.CouncilVoteNotification;

/** Keeps the current single-player Council panel as the default adapter. */
public final class DesktopCouncilDecisionAdapter implements CouncilDecisionAdapter {
    @Override
    public void presentConvention(GameSession session, GalacticCouncil council) {
        CouncilVoteNotification.create();
    }
}
