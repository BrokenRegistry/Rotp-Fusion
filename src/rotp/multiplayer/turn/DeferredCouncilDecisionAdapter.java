package rotp.multiplayer.turn;

import rotp.model.empires.GalacticCouncil;
import rotp.model.game.GameSession;

/** Stops a convention at the next owned vote without a live UI wait. */
public final class DeferredCouncilDecisionAdapter implements CouncilDecisionAdapter {
    @Override
    public void presentConvention(GameSession session, GalacticCouncil council) {
        if (session.controllerRegistry() == null)
            throw new IllegalStateException("Deferred voting requires a controller roster");
        council.continueNonPlayerVoting();
    }
}
