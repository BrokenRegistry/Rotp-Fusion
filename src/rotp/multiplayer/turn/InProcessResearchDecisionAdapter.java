package rotp.multiplayer.turn;

import java.util.Map;
import java.util.Objects;

import rotp.model.game.GameSession;
import rotp.model.tech.TechCategory;

/** Resolves research choices using one provider per human seat. */
public final class InProcessResearchDecisionAdapter implements ResearchDecisionAdapter {
    @FunctionalInterface
    public interface HumanResearchProvider {
        String choose(PendingResearchDecision decision);
    }

    private final Map<String, HumanResearchProvider> providers;

    public InProcessResearchDecisionAdapter(Map<String, HumanResearchProvider> providers) {
        this.providers = Map.copyOf(Objects.requireNonNull(providers, "providers"));
    }

    @Override
    public void presentSelection(GameSession session, TechCategory category) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(category, "category");
        ResearchDecisionRouter router = new ResearchDecisionRouter(session);
        PendingResearchDecision decision = router.pending(category.empire().id, category.index());
        if (decision == null)
            throw new IllegalStateException("Research choice has no owning human seat");
        HumanResearchProvider provider = providers.get(decision.ownerPlayerId());
        if (provider == null)
            throw new IllegalStateException("No research provider for " + decision.ownerPlayerId());
        ResearchDecisionRouter.Result result = router.submit(decision.ownerPlayerId(),
                decision.id(), decision.empireId(), decision.categoryIndex(), provider.choose(decision));
        if (result != ResearchDecisionRouter.Result.ACCEPTED)
            throw new IllegalStateException("Research choice rejected: " + result);
    }
}
