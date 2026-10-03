package rotp.multiplayer.pbem;

import static org.junit.jupiter.api.Assertions.*;
import static rotp.multiplayer.turn.InProcessBombardmentDecisionAdapter.Choice.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import rotp.model.empires.SpyNetwork.Sabotage;
import rotp.multiplayer.pbem.StandingOrders.*;
import rotp.multiplayer.turn.InProcessSabotageDecisionAdapter.Choice;

class StandingOrderRulesTest {
    private static StandingOrders bombard(Bombard rule) {
        return new StandingOrders(rule, Frame.NEVER, SabotageTarget.FACTORIES);
    }
    private static StandingOrders sabotage(SabotageTarget target) {
        return new StandingOrders(Bombard.NEVER, Frame.NEVER, target);
    }

    @Test void bombardRulesFollowWarAndInvasion() {
        assertEquals(SKIP, StandingOrderRules.bombard(bombard(Bombard.NEVER), true, 0, true));
        assertEquals(BOMBARD, StandingOrderRules.bombard(bombard(Bombard.ALWAYS), false, 0, true));
        assertEquals(SKIP, StandingOrderRules.bombard(bombard(Bombard.AT_WAR), false, 0, true));
        assertEquals(BOMBARD, StandingOrderRules.bombard(bombard(Bombard.AT_WAR), true, 0, true));
        assertEquals(BOMBARD, StandingOrderRules.bombard(bombard(Bombard.AT_WAR_NOT_INVADING), true, 0, true));
        assertEquals(SKIP, StandingOrderRules.bombard(bombard(Bombard.AT_WAR_NOT_INVADING), true, 3, true));
    }

    @Test void invadingBombardmentSparesPopulationOnlyWhenAllowed() {
        assertEquals(TARGET_BOMBARD, StandingOrderRules.bombard(bombard(Bombard.AT_WAR), true, 2, true));
        assertEquals(BOMBARD, StandingOrderRules.bombard(bombard(Bombard.AT_WAR), true, 2, false));
    }

    @Test void sabotagePrefersTheSuggestedSystemForTheChosenAction() {
        var legal = Map.of(Sabotage.FACTORIES, List.of(4, 7), Sabotage.MISSILES, List.of(7),
                Sabotage.REBELS, List.<Integer>of());
        assertEquals(new Choice(Sabotage.FACTORIES, 7), StandingOrderRules.sabotage(sabotage(SabotageTarget.FACTORIES), 7, legal));
        assertEquals(new Choice(Sabotage.FACTORIES, 4), StandingOrderRules.sabotage(sabotage(SabotageTarget.FACTORIES), 9, legal));
        assertEquals(new Choice(Sabotage.MISSILES, 7), StandingOrderRules.sabotage(sabotage(SabotageTarget.MISSILES), 4, legal));
    }

    @Test void sabotageFallsBackThenCancels() {
        var onlyMissiles = Map.of(Sabotage.FACTORIES, List.<Integer>of(), Sabotage.MISSILES, List.of(3),
                Sabotage.REBELS, List.<Integer>of());
        assertEquals(new Choice(Sabotage.MISSILES, 3), StandingOrderRules.sabotage(sabotage(SabotageTarget.REBELS), 3, onlyMissiles));
        var none = Map.of(Sabotage.FACTORIES, List.<Integer>of(), Sabotage.MISSILES, List.<Integer>of(),
                Sabotage.REBELS, List.<Integer>of());
        assertNull(StandingOrderRules.sabotage(sabotage(SabotageTarget.FACTORIES), 3, none));
    }
}
