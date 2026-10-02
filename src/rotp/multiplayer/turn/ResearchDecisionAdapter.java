package rotp.multiplayer.turn;

import rotp.model.game.GameSession;
import rotp.model.tech.TechCategory;

/** Presents a new research choice to its owning controller. */
@FunctionalInterface
public interface ResearchDecisionAdapter {
    void presentSelection(GameSession session, TechCategory category);
}
