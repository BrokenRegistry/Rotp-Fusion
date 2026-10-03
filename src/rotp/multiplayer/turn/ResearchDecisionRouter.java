package rotp.multiplayer.turn;

import java.util.Objects;

import rotp.model.empires.Empire;
import rotp.model.game.GameSession;
import rotp.model.tech.TechCategory;
import rotp.model.tech.TechTree;
import rotp.multiplayer.session.ControllerRegistry;
import rotp.multiplayer.session.PlayerSeat;

/** Validates research selections against the owning seat and current category. */
public final class ResearchDecisionRouter {
    public enum Result { ACCEPTED, NO_PENDING_DECISION, WRONG_OWNER, STALE_DECISION, ILLEGAL_CHOICE }

    private final GameSession session;

    public ResearchDecisionRouter(GameSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    public PendingResearchDecision pending(int empireId, int categoryIndex) {
        ControllerRegistry controllers = session.controllerRegistry();
        if (controllers == null || session.galaxy() == null ||
                categoryIndex < 0 || categoryIndex >= TechTree.NUM_CATEGORIES)
            return null;
        Empire empire = session.galaxy().empire(empireId);
        PlayerSeat seat = controllers.seatForEmpire(empireId);
        if (empire == null || seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
            return null;
        TechCategory category = empire.tech().category(categoryIndex);
        if (!category.selectionPending())
            return null;
        String id = "research:" + session.id() + ":" + empireId + ":" + categoryIndex
                + ":" + category.selectionSequence();
        return new PendingResearchDecision(id, seat.playerId(), empireId, categoryIndex,
                category.techIdsAvailableForResearch());
    }

    public Result submit(String playerId, String decisionId, int empireId,
            int categoryIndex, String techId) {
        PendingResearchDecision decision = pending(empireId, categoryIndex);
        if (decision == null)
            return Result.NO_PENDING_DECISION;
        if (!decision.id().equals(decisionId))
            return Result.STALE_DECISION;
        if (!decision.ownerPlayerId().equals(playerId))
            return Result.WRONG_OWNER;
        if (!decision.legalTechIds().contains(techId))
            return Result.ILLEGAL_CHOICE;
        TechCategory category = session.galaxy().empire(empireId).tech().category(categoryIndex);
        return category.selectPendingTech(techId) ? Result.ACCEPTED : Result.STALE_DECISION;
    }
}
