package rotp.ui;

import rotp.model.game.GameSession;
import rotp.model.tech.TechCategory;
import rotp.multiplayer.turn.ResearchDecisionAdapter;

/** Keeps the current research panel as the default adapter. */
public final class DesktopResearchDecisionAdapter implements ResearchDecisionAdapter {
    @Override
    public void presentSelection(GameSession session, TechCategory category) {
        RotPUI.instance().selectSelectNewTechPanel(category);
    }
}
