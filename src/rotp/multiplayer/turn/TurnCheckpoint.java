package rotp.multiplayer.turn;

import java.io.Serializable;
import java.util.List;

/** Completed phase cursor and decisions recoverable from the serialized model. */
public record TurnCheckpoint(int turn, TurnCoordinator.Phase lastCompletedPhase,
        PendingDecision councilVote, PendingDecision councilRuling,
        List<PendingResearchDecision> researchChoices,
        List<PendingColonizationDecision> colonizationChoices,
        List<PendingDiplomacyDecision> diplomacyChoices)
        implements Serializable {
    private static final long serialVersionUID = 1L;

    public TurnCheckpoint {
        if (turn < 0)
            throw new IllegalArgumentException("No turn has started");
        researchChoices = List.copyOf(researchChoices);
        colonizationChoices = List.copyOf(colonizationChoices);
        diplomacyChoices = List.copyOf(diplomacyChoices);
    }
}
