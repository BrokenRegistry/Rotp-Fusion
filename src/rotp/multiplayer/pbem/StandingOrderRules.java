package rotp.multiplayer.pbem;

import java.util.List;
import java.util.Map;
import rotp.model.empires.SpyNetwork.Sabotage;
import rotp.multiplayer.turn.InProcessBombardmentDecisionAdapter;
import rotp.multiplayer.turn.InProcessSabotageDecisionAdapter;

/** Pure answers to absent players' mid-turn choices. */
public final class StandingOrderRules {
    private static final List<Sabotage> FALLBACK =
            List.of(Sabotage.FACTORIES, Sabotage.MISSILES, Sabotage.REBELS);

    private StandingOrderRules() { }

    public static InProcessBombardmentDecisionAdapter.Choice bombard(StandingOrders orders,
            boolean atWar, int transportsInTransit, boolean targetAllowed) {
        boolean bomb = switch (orders.bombard()) {
            case NEVER -> false;
            case ALWAYS -> true;
            case AT_WAR -> atWar;
            case AT_WAR_NOT_INVADING -> atWar && transportsInTransit == 0;
        };
        if (!bomb) return InProcessBombardmentDecisionAdapter.Choice.SKIP;
        // Spare population for arriving troops, as the single-player auto path does.
        return targetAllowed && transportsInTransit > 0
                ? InProcessBombardmentDecisionAdapter.Choice.TARGET_BOMBARD
                : InProcessBombardmentDecisionAdapter.Choice.BOMBARD;
    }

    /** Returns null when no sabotage target is legal, which cancels the mission. */
    public static InProcessSabotageDecisionAdapter.Choice sabotage(StandingOrders orders,
            int suggestedSystemId, Map<Sabotage, List<Integer>> legal) {
        Sabotage preferred = switch (orders.sabotage()) {
            case FACTORIES -> Sabotage.FACTORIES;
            case MISSILES -> Sabotage.MISSILES;
            case REBELS -> Sabotage.REBELS;
        };
        var order = new java.util.ArrayList<Sabotage>();
        order.add(preferred);
        for (Sabotage action : FALLBACK) if (action != preferred) order.add(action);
        for (Sabotage action : order) {
            List<Integer> systems = legal.getOrDefault(action, List.of());
            if (systems.isEmpty()) continue;
            int system = systems.contains(suggestedSystemId) ? suggestedSystemId : systems.get(0);
            return new InProcessSabotageDecisionAdapter.Choice(action, system);
        }
        return null;
    }
}
