package rotp.multiplayer.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rotp.model.empires.Empire;
import rotp.model.empires.GalacticCouncil;
import rotp.model.game.GameSession;

/** Derives a rostered match result without changing the desktop player status. */
public final class MatchOutcomeEvaluator {
    private MatchOutcomeEvaluator() { }

    public static MatchOutcome evaluate(GameSession session) {
        if (session.controllerRegistry() == null || session.galaxy() == null)
            throw new IllegalStateException("A rostered galaxy is required");

        List<Empire> active = session.galaxy().activeEmpires();
        Set<Integer> winnerIds = new HashSet<>();
        MatchOutcome.Cause cause = MatchOutcome.Cause.ONGOING;
        if (active.size() == 1) {
            winnerIds.add(active.get(0).id);
            cause = MatchOutcome.Cause.MILITARY;
        }
        else {
            GalacticCouncil council = session.galaxy().council();
            boolean immediateRuling = session.options().immediateCouncilWin()
                    || session.options().realmsBeyondCouncil() || council.isForcedEndOfGame();
            if (council.disbanded() && council.leader() != null
                    && (council.rebels().isEmpty() || council.allies().isEmpty()
                            || immediateRuling)) {
                for (Empire winner : council.allies().isEmpty() && !immediateRuling
                        ? council.rebels() : council.allies()) {
                    // Accepting an election is not itself a shared victory. Preserve
                    // the desktop leader/prior-alliance rule until a final war has
                    // actually begun; a resolved final war rewards its winning side.
                    if (council.finalWarStarted() || winner == council.leader()
                            || (!session.options().noAllianceCouncil()
                                    && winner.alliedWith(council.leader().id)))
                        winnerIds.add(winner.id);
                }
                cause = MatchOutcome.Cause.COUNCIL;
            }
            if (cause == MatchOutcome.Cause.ONGOING
                    && !session.options().noAllianceCouncil()) {
                for (PlayerSeat seat : session.controllerRegistry().seats()) {
                    if (seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
                        continue;
                    Empire candidate = session.galaxy().empire(seat.empireId());
                    if (candidate == null || candidate.extinct())
                        continue;
                    boolean alliedWithEveryone = true;
                    for (Empire other : active) {
                        if (other != candidate && !candidate.alliedWith(other.id)) {
                            alliedWithEveryone = false;
                            break;
                        }
                    }
                    if (alliedWithEveryone) {
                        for (Empire winner : active)
                            winnerIds.add(winner.id);
                        cause = MatchOutcome.Cause.MILITARY_ALLIANCE;
                        break;
                    }
                }
            }
        }

        boolean anyHumanAlive = false;
        for (PlayerSeat seat : session.controllerRegistry().seats()) {
            if (seat.controllerType() == PlayerSeat.ControllerType.HUMAN) {
                Empire empire = session.galaxy().empire(seat.empireId());
                anyHumanAlive |= empire != null && !empire.extinct();
            }
        }
        if (!anyHumanAlive && cause == MatchOutcome.Cause.ONGOING)
            cause = MatchOutcome.Cause.NO_HUMANS_REMAIN;

        boolean finished = cause != MatchOutcome.Cause.ONGOING;
        Map<String, MatchOutcome.SeatResult> results = new HashMap<>();
        for (PlayerSeat seat : session.controllerRegistry().seats()) {
            if (seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
                continue;
            Empire empire = session.galaxy().empire(seat.empireId());
            MatchOutcome.SeatResult result;
            if (finished)
                result = winnerIds.contains(seat.empireId())
                        ? MatchOutcome.SeatResult.WON : MatchOutcome.SeatResult.LOST;
            else if (empire == null || empire.extinct())
                result = MatchOutcome.SeatResult.ELIMINATED;
            else
                result = MatchOutcome.SeatResult.ACTIVE;
            results.put(seat.playerId(), result);
        }
        return new MatchOutcome(finished, cause, results);
    }
}
