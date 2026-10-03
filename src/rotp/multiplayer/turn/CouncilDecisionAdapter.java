package rotp.multiplayer.turn;

import rotp.model.empires.GalacticCouncil;
import rotp.model.game.GameSession;

/** Presents a council convention and waits until its voting flow is released. */
@FunctionalInterface
public interface CouncilDecisionAdapter {
    void presentConvention(GameSession session, GalacticCouncil council);
}
