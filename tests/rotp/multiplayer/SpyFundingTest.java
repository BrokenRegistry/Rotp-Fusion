package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.model.empires.SpyNetwork;
import rotp.model.game.IInGameOptions;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class SpyFundingTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @Test void humanOwnersKeepExcessForFutureSpiesRegardlessOfTargetController() throws Exception {
        var game = HotSeatTestFixture.start(2, 0, 1);
        boolean previous = IInGameOptions.spyOverSpend.get();
        try {
            IInGameOptions.spyOverSpend.set(false);
            for (int ownerId : new int[] {0, 1}) {
                for (int targetId : new int[] {ownerId == 0 ? 1 : 0, 2}) {
                    var owner = game.galaxy().empire(ownerId);
                    var target = game.galaxy().empire(targetId);
                    var spies = owner.viewForEmpire(targetId).spies();
                    spies.maxSpies(1);
                    float reserve = owner.totalReserve();
                    float targetReserve = target.totalReserve();
                    float excess = owner.baseSpyCost() / 2;
                    fund(spies, owner.baseSpyCost() + excess);
                    assertEquals(1, spies.numActiveSpies(), "Recruit only the requested spy");
                    assertEquals(reserve, owner.totalReserve(), "Unused spy funds stay in the network");
                    assertEquals(targetReserve, target.totalReserve(), "Do not credit the target");
                    fund(spies, 0);
                    assertEquals(reserve, owner.totalReserve(), "Retained funds must not create treasury money");
                    assertEquals(1, spies.numActiveSpies(), "Do not exceed the human owner's target");
                    spies.maxSpies(2);
                    float cost = spies.realCostForNextSpy();
                    assertEquals(owner.baseSpyCost() - excess, cost, "Retained funds reduce the next spy's cost");
                    fund(spies, cost - 1);
                    assertEquals(1, spies.numActiveSpies(), "Partial funding cannot recruit another spy");
                    fund(spies, 1);
                    assertEquals(2, spies.numActiveSpies());
                }
            }
        } finally { IInGameOptions.spyOverSpend.set(previous); }
    }

    @Test void unfinishedRecruitmentKeepsItsFunding() throws Exception {
        var game = HotSeatTestFixture.start(1, 0, 1);
        boolean previous = IInGameOptions.spyOverSpend.get();
        try {
            IInGameOptions.spyOverSpend.set(false);
            var owner = game.galaxy().empire(0);
            var spies = owner.viewForEmpire(1).spies();
            spies.maxSpies(1);
            float reserve = owner.totalReserve();
            float targetReserve = game.galaxy().empire(1).totalReserve();
            float cost = owner.baseSpyCost();
            fund(spies, cost - 1);
            assertEquals(0, spies.numActiveSpies());
            assertEquals(reserve, owner.totalReserve(), "Keep partial funding until the team is complete");
            assertEquals(targetReserve, game.galaxy().empire(1).totalReserve());
            fund(spies, 1);
            assertEquals(1, spies.numActiveSpies());
            assertEquals(reserve, owner.totalReserve());
        } finally { IInGameOptions.spyOverSpend.set(previous); }
    }

    @Test void aiOwnersAndEnabledOverspendStillUseTheFullBudget() throws Exception {
        var game = HotSeatTestFixture.start(2, 0, 1);
        boolean previous = IInGameOptions.spyOverSpend.get();
        try {
            for (boolean overspend : new boolean[] {false, true}) {
                IInGameOptions.spyOverSpend.set(overspend);
                var owner = game.galaxy().empire(overspend ? 0 : 2);
                var spies = owner.viewForEmpire(1).spies();
                spies.maxSpies(1);
                float reserve = owner.totalReserve();
                fund(spies, owner.baseSpyCost() * 2);
                assertEquals(2, spies.numActiveSpies());
                assertEquals(reserve, owner.totalReserve());
            }
        } finally { IInGameOptions.spyOverSpend.set(previous); }
    }

    private static void fund(SpyNetwork spies, float amount) throws Exception {
        var method = SpyNetwork.class.getDeclaredMethod("allocateSpyBC", float.class);
        method.setAccessible(true);
        method.invoke(spies, amount);
    }
}
