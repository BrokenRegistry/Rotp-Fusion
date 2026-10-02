package rotp.multiplayer.turn;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import rotp.model.game.GameSession;
import rotp.multiplayer.session.PlayerSeat;

/** Drives one rostered turn through its recoverable, owned decision barriers. */
public final class InProcessMatchDriver {
    public interface SeatDecisions {
        /** Return null to abstain. */
        Integer councilVote(PendingDecision decision);
        boolean councilRuling(PendingDecision decision);
        String research(PendingResearchDecision decision);
        boolean colonize(PendingColonizationDecision decision);
        boolean diplomacy(PendingDiplomacyDecision decision);
    }

    private final GameSession session;
    private final Map<String, SeatDecisions> providers;
    private final BiConsumer<GameSession, TurnCheckpoint> boundaryObserver;

    public InProcessMatchDriver(GameSession session, Map<String, SeatDecisions> providers) {
        this(session, providers, (ignoredSession, ignoredCheckpoint) -> { });
    }

    /** Observer may save the completed boundary before the next model phase. */
    public InProcessMatchDriver(GameSession session, Map<String, SeatDecisions> providers,
            BiConsumer<GameSession, TurnCheckpoint> boundaryObserver) {
        this.session = Objects.requireNonNull(session, "session");
        this.providers = Map.copyOf(Objects.requireNonNull(providers, "providers"));
        this.boundaryObserver = Objects.requireNonNull(boundaryObserver, "boundaryObserver");
        if (session.controllerRegistry() == null)
            throw new IllegalArgumentException("A controller roster is required");
        for (PlayerSeat seat : session.controllerRegistry().seats()) {
            if (seat.controllerType() == PlayerSeat.ControllerType.HUMAN
                    && !this.providers.containsKey(seat.playerId()))
                throw new IllegalArgumentException("Missing decision provider for " + seat.playerId());
        }
    }

    /** Returns at the end of one turn or when the match finishes. */
    public TurnCheckpoint advanceOneTurn() {
        int turnInProgress = -1;
        while (true) {
            TurnCheckpoint checkpoint = turnInProgress >= 0
                    && session.turnCoordinator().turn() == turnInProgress
                    && session.turnCoordinator().nextPhase() == null
                    ? session.deliverInProcessNotifications()
                    : session.advanceInProcessPhase();
            turnInProgress = checkpoint.turn();
            boundaryObserver.accept(session, checkpoint);
            if (checkpoint.councilVote() != null) {
                PendingDecision decision = checkpoint.councilVote();
                Integer choice = provider(decision.ownerPlayerId()).councilVote(decision);
                requireAccepted(new DecisionRouter(session).submitCouncilVote(
                        decision.ownerPlayerId(), decision.id(), choice));
            }
            else if (checkpoint.councilRuling() != null) {
                PendingDecision decision = checkpoint.councilRuling();
                boolean accept = provider(decision.ownerPlayerId()).councilRuling(decision);
                requireAccepted(new DecisionRouter(session).submitCouncilRuling(
                        decision.ownerPlayerId(), decision.id(), accept));
            }
            else if (!checkpoint.researchChoices().isEmpty()) {
                PendingResearchDecision decision = checkpoint.researchChoices().get(0);
                String techId = provider(decision.ownerPlayerId()).research(decision);
                ResearchDecisionRouter.Result result = new ResearchDecisionRouter(session).submit(
                        decision.ownerPlayerId(), decision.id(), decision.empireId(),
                        decision.categoryIndex(), techId);
                if (result != ResearchDecisionRouter.Result.ACCEPTED)
                    throw new IllegalStateException("Research reply rejected: " + result);
            }
            else if (!checkpoint.colonizationChoices().isEmpty()) {
                PendingColonizationDecision decision = checkpoint.colonizationChoices().get(0);
                boolean colonize = provider(decision.ownerPlayerId()).colonize(decision);
                if (!session.answerColonizationDecision(decision.ownerPlayerId(),
                        decision.id(), colonize))
                    throw new IllegalStateException("Colonization reply rejected");
            }
            else if (!checkpoint.diplomacyChoices().isEmpty()) {
                PendingDiplomacyDecision decision = checkpoint.diplomacyChoices().get(0);
                String owner = decision.notice().ownerPlayerId();
                boolean accept = provider(owner).diplomacy(decision);
                if (!session.answerDiplomacyDecision(owner, decision.id(), accept))
                    throw new IllegalStateException("Diplomacy reply rejected");
            }
            else if (!session.inProgress() || session.turnCoordinator().nextPhase() == null)
                return checkpoint;
        }
    }

    private SeatDecisions provider(String playerId) {
        SeatDecisions provider = providers.get(playerId);
        if (provider == null)
            throw new IllegalStateException("No provider for " + playerId);
        return provider;
    }

    private static void requireAccepted(DecisionRouter.Result result) {
        if (result != DecisionRouter.Result.ACCEPTED)
            throw new IllegalStateException("Council reply rejected: " + result);
    }
}
