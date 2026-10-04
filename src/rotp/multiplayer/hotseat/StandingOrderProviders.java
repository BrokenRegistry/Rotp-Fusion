package rotp.multiplayer.hotseat;

import java.util.List;
import rotp.model.empires.Empire;
import rotp.model.game.GameSession;
import rotp.model.tech.Tech;
import rotp.multiplayer.pbem.StandingOrderRules;
import rotp.multiplayer.pbem.StandingOrders;
import rotp.multiplayer.turn.*;

/** Applies a player's standing orders to the live model's mid-phase choices. */
public final class StandingOrderProviders {
    private StandingOrderProviders() { }

    public static InProcessBombardmentDecisionAdapter.Choice bombard(GameSession game, BombardmentDecision d) {
        Empire attacker = game.galaxy().empire(d.attackerEmpireId());
        return StandingOrderRules.bombard(orders(game, d.ownerPlayerId()), attacker.atWarWith(d.defenderEmpireId()),
                attacker.transportsInTransit(game.galaxy().system(d.systemId())), d.targetAllowed());
    }

    public static InProcessEspionageDecisionAdapter.Choice espionage(GameSession game, EspionageDecision d) {
        Empire owner = game.galaxy().empire(d.ownerEmpireId());
        List<Tech> candidates = d.categoryTechIds().values().stream().sorted().map(game::tech).toList();
        // The AI sorts the list it is given.
        Tech tech = owner.scientistAI().mostDesirableTech(new java.util.ArrayList<>(candidates));
        if (tech == null || !candidates.contains(tech)) tech = candidates.get(0);
        Integer framed = null;
        if (orders(game, d.ownerPlayerId()).frame() == StandingOrders.Frame.WHEN_POSSIBLE
                && !d.frameableEmpireIds().isEmpty()) {
            List<Empire> frameable = d.frameableEmpireIds().stream().map(game.galaxy()::empire).toList();
            Empire suggested = owner.spyMasterAI().suggestToFrame(frameable);
            framed = suggested != null && d.frameableEmpireIds().contains(suggested.id)
                    ? suggested.id : d.frameableEmpireIds().get(0);
        }
        return new InProcessEspionageDecisionAdapter.Choice(tech.id, framed);
    }

    public static InProcessSabotageDecisionAdapter.Choice sabotage(GameSession game, SabotageDecision d) {
        return StandingOrderRules.sabotage(orders(game, d.ownerPlayerId()), d.suggestedSystemId(), d.targetSystemIds());
    }

    private static StandingOrders orders(GameSession game, String playerId) {
        return game.playByEmail().orders(playerId);
    }
}
