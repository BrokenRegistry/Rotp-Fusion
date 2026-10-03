package rotp.multiplayer.turn;

import java.util.Map;
import java.util.Objects;

import rotp.model.empires.GalacticCouncil;
import rotp.model.game.GameSession;

/** Resolves a convention with independent human vote providers in one process. */
public final class InProcessCouncilDecisionAdapter implements CouncilDecisionAdapter {
    @FunctionalInterface
    public interface HumanVoteProvider {
        /** Returns a candidate empire ID, or null to abstain. */
        Integer choose(PendingDecision decision);
    }
    @FunctionalInterface
    public interface HumanRulingProvider {
        boolean accept(PendingDecision decision);
    }

    private final Map<String, HumanVoteProvider> providers;
    private final Map<String, HumanRulingProvider> rulingProviders;

    public InProcessCouncilDecisionAdapter(Map<String, HumanVoteProvider> providers) {
        this(providers, Map.of());
    }

    public InProcessCouncilDecisionAdapter(Map<String, HumanVoteProvider> providers,
            Map<String, HumanRulingProvider> rulingProviders) {
        this.providers = Map.copyOf(Objects.requireNonNull(providers, "providers"));
        this.rulingProviders = Map.copyOf(Objects.requireNonNull(rulingProviders, "rulingProviders"));
    }

    @Override
    public void presentConvention(GameSession session, GalacticCouncil council) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(council, "council");
        DecisionRouter router = new DecisionRouter(session);
        council.continueNonPlayerVoting();
        while (council.votingInProgress()) {
            PendingDecision decision = router.pendingCouncilVote();
            if (decision == null)
                throw new IllegalStateException("Council vote has no owning human seat");
            HumanVoteProvider provider = providers.get(decision.ownerPlayerId());
            if (provider == null)
                throw new IllegalStateException("No vote provider for " + decision.ownerPlayerId());
            Integer choice = provider.choose(decision);
            DecisionRouter.Result result = router.submitCouncilVote(
                    decision.ownerPlayerId(), decision.id(), choice);
            if (result != DecisionRouter.Result.ACCEPTED)
                throw new IllegalStateException("Council vote rejected: " + result);
        }
        PendingDecision ruling;
        while ((ruling = router.pendingCouncilRuling()) != null) {
            HumanRulingProvider provider = rulingProviders.get(ruling.ownerPlayerId());
            if (provider == null)
                throw new IllegalStateException("No ruling provider for " + ruling.ownerPlayerId());
            DecisionRouter.Result result = router.submitCouncilRuling(
                    ruling.ownerPlayerId(), ruling.id(), provider.accept(ruling));
            if (result != DecisionRouter.Result.ACCEPTED)
                throw new IllegalStateException("Council ruling rejected: " + result);
        }
    }
}
