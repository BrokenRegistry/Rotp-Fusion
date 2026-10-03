package rotp.multiplayer.turn;

import java.util.List;
import java.util.Objects;

import rotp.model.empires.Empire;
import rotp.model.empires.GalacticCouncil;
import rotp.model.game.GameSession;
import rotp.multiplayer.session.ControllerRegistry;
import rotp.multiplayer.session.PlayerSeat;

/** Routes council votes to their owning seat without exposing the full session. */
public final class DecisionRouter {
    public enum Result { ACCEPTED, NO_PENDING_DECISION, WRONG_OWNER, STALE_DECISION, ILLEGAL_CHOICE }

    private final GameSession session;

    public DecisionRouter(GameSession session) {
        this.session = Objects.requireNonNull(session, "session");
    }

    public PendingDecision pendingCouncilVote() {
        ControllerRegistry controllers = session.controllerRegistry();
        if (controllers == null || session.galaxy() == null)
            return null;
        GalacticCouncil council = session.galaxy().council();
        if (!council.hasPendingHumanVote())
            return null;
        Empire voter = council.nextVoter();
        PlayerSeat seat = controllers.seatForEmpire(voter.id);
        if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
            return null;
        String id = "council:" + session.id() + ":" + council.conventionSequence()
                + ":" + council.voteIndex();
        return new PendingDecision(id, seat.playerId(), session.galaxy().currentTurn(),
                voter.id, PendingDecision.Kind.COUNCIL_VOTE,
                List.of(council.candidate1().id, council.candidate2().id), true);
    }

    public PendingDecision pendingCouncilRuling() {
        ControllerRegistry controllers = session.controllerRegistry();
        if (controllers == null || session.galaxy() == null)
            return null;
        GalacticCouncil council = session.galaxy().council();
        Empire empire = council.nextHumanRulingEmpire();
        if (empire == null)
            return null;
        PlayerSeat seat = controllers.seatForEmpire(empire.id);
        if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
            return null;
        String id = "council-ruling:" + session.id() + ":"
                + council.conventionSequence() + ":" + empire.id;
        return new PendingDecision(id, seat.playerId(), session.galaxy().currentTurn(),
                empire.id, PendingDecision.Kind.COUNCIL_RULING, List.of(), false);
    }

    public Result submitCouncilRuling(String playerId, String decisionId, boolean accept) {
        PendingDecision pending = pendingCouncilRuling();
        if (pending == null)
            return Result.NO_PENDING_DECISION;
        if (!pending.id().equals(decisionId))
            return Result.STALE_DECISION;
        if (!pending.ownerPlayerId().equals(playerId))
            return Result.WRONG_OWNER;
        GalacticCouncil council = session.galaxy().council();
        if (!council.castHumanRuling(pending.empireId(), accept))
            return Result.STALE_DECISION;
        council.continueNonPlayerRulings();
        return Result.ACCEPTED;
    }

    public Result submitCouncilVote(String playerId, String decisionId, Integer chosenEmpireId) {
        PendingDecision pending = pendingCouncilVote();
        if (pending == null)
            return Result.NO_PENDING_DECISION;
        if (!pending.id().equals(decisionId))
            return Result.STALE_DECISION;
        if (!pending.ownerPlayerId().equals(playerId))
            return Result.WRONG_OWNER;
        if (chosenEmpireId != null && !pending.legalEmpireIds().contains(chosenEmpireId))
            return Result.ILLEGAL_CHOICE;
        GalacticCouncil council = session.galaxy().council();
        Empire chosen = chosenEmpireId == null ? null
                : council.candidate1().id == chosenEmpireId ? council.candidate1() : council.candidate2();
        if (!council.castHumanVote(pending.empireId(), chosen))
            return Result.STALE_DECISION;
        council.continueNonPlayerVoting();
        return Result.ACCEPTED;
    }
}
