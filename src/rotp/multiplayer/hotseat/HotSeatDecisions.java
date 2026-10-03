package rotp.multiplayer.hotseat;

import java.util.concurrent.CompletableFuture;
import rotp.multiplayer.turn.*;

/** UI requests return immediately; only the simulation worker waits for answers. */
public interface HotSeatDecisions {
    CompletableFuture<Integer> councilVote(PendingDecision decision);
    CompletableFuture<Boolean> councilRuling(PendingDecision decision);
    CompletableFuture<String> research(PendingResearchDecision decision);
    CompletableFuture<Boolean> colonize(PendingColonizationDecision decision);
    CompletableFuture<Boolean> diplomacy(PendingDiplomacyDecision decision);
    CompletableFuture<InProcessBombardmentDecisionAdapter.Choice> bombardment(BombardmentDecision decision);
    CompletableFuture<InProcessEspionageDecisionAdapter.Choice> espionage(EspionageDecision decision);
    CompletableFuture<InProcessSabotageDecisionAdapter.Choice> sabotage(SabotageDecision decision);
}
