package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.*;
import rotp.ui.RotPUI;
import rotp.ui.multiplayer.HotSeatDesktop;
import rotp.ui.tech.AllocateTechUI;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatOrdersTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @Test void realResearchActionsRejectFinishedAndStalePanels() throws Exception {
        var game = HotSeatTestFixture.start(1, 0);
        game.startHotSeatGame(game.options(), new HotSeatSetup(List.of(
                new HotSeatSetup.Assignment("a", "Alice", 0), new HotSeatSetup.Assignment("b", "Bob", 1))));
        HotSeatController[] controller = new HotSeatController[1];
        HotSeatTestFixture.onEdt(() -> {
            controller[0] = new HotSeatController(game, new HotSeatDesktop(), new HotSeatTurnTest.ImmediateChoices());
            try { HotSeatTestFixture.set(GameSession.class, game, "hotSeatController", controller[0]); }
            catch (Exception ex) { throw new AssertionError(ex); }
            controller[0].start();
        });
        try {
            HotSeatTurnTest.awaitPlanning(game);
            AllocateTechUI[] oldPanel = new AllocateTechUI[1];
            var oldMap = new rotp.ui.main.overlay.MapOverlayNone[1];
            boolean beforeA = game.galaxy().empire(0).tech().category(0).locked();
            boolean beforeB = game.galaxy().empire(1).tech().category(0).locked();
            HotSeatTestFixture.onEdt(() -> {
                oldPanel[0] = RotPUI.instance().techUI();
                oldMap[0] = new rotp.ui.main.overlay.MapOverlayNone(RotPUI.instance().mainUI());
                checkDesignSpiesAndRally(game, 0, 1);
                var own = game.galaxy().empire(0).allColonizedSystems().get(0);
                var other = game.galaxy().empire(1).allColonizedSystems().get(0);
                own.transportSprite().amt(3);
                assertEquals(3, own.transportAmt());
                other.transportAmt(4);
                other.transportSprite().clear();
                assertEquals(4, other.transportAmt(), "Enemy transport order must remain private");
                boolean policy = rotp.model.game.IGovOptions.governorByDefault.get();
                rotp.model.game.IGovOptions.governorByDefault.set(!policy);
                assertEquals(policy, rotp.model.game.IGovOptions.governorByDefault.get(), "Shared governor policy is frozen");
                own.colony().setGovernor(false);
                other.colony().setGovernor(false);
                var mixed = bulkPane(List.of(own, other));
                mixed.setGovernor(true);
                assertFalse(own.colony().isGovernor(), "Reject mixed-owner bulk changes atomically");
                assertFalse(other.colony().isGovernor());
                var legal = bulkPane(List.of(own));
                legal.setGovernor(true);
                assertTrue(own.colony().isGovernor());
                oldPanel[0].toggleCategoryLock(0);
                assertEquals(!beforeA, game.galaxy().empire(0).tech().category(0).locked());
                var snap = game.hotSeatState().snapshot();
                controller[0].finishPlayerTurn(snap.ownerPlayerId(), snap.revision());
                oldPanel[0].toggleCategoryLock(0);
                assertEquals(!beforeA, game.galaxy().empire(0).tech().category(0).locked(), "Finished seat is read-only");
            });
            HotSeatTurnTest.awaitPlanning(game);
            HotSeatTestFixture.onEdt(() -> {
                checkDesignSpiesAndRally(game, 1, 0);
                var beforeKey = game.hotSeatState().snapshot();
                oldMap[0].handleKeyPress(new java.awt.event.KeyEvent(RotPUI.instance().mainUI(),
                        java.awt.event.KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
                        java.awt.event.KeyEvent.VK_N, 'n'));
                assertEquals(beforeKey, game.hotSeatState().snapshot(), "Old N callback cannot finish Bob's turn");
                oldPanel[0].toggleCategoryLock(0);
                assertEquals(beforeB, game.galaxy().empire(1).tech().category(0).locked(), "Old callback cannot edit next owner");
                RotPUI.instance().techUI().toggleCategoryLock(0);
                assertEquals(!beforeB, game.galaxy().empire(1).tech().category(0).locked());
            });
        } finally { HotSeatTestFixture.onEdt(controller[0]::close); }
    }
    private static void checkDesignSpiesAndRally(GameSession game, int owner, int other) {
        try {
            var own = game.galaxy().empire(owner);
            var foreign = game.galaxy().empire(other);
            var constructor = Class.forName("rotp.ui.design.ConfirmCreateUI").getDeclaredConstructor();
            constructor.setAccessible(true);
            var panel = constructor.newInstance();
            var design = foreign.shipLab().scoutDesign();
            String original = design.name();
            HotSeatTestFixture.set(panel.getClass(), panel, "targetDesign", design);
            invoke(panel, "createAction");
            assertEquals(original, design.name(), "Foreign design cannot be renamed");
            design = own.shipLab().scoutDesign();
            HotSeatTestFixture.set(panel.getClass(), panel, "targetDesign", design);
            var nameField = panel.getClass().getDeclaredField("nameField");
            nameField.setAccessible(true);
            ((javax.swing.JTextField) nameField.get(panel)).setText("Scout " + owner);
            invoke(panel, "createAction");
            assertEquals("Scout " + owner, design.name());
            var spies = new rotp.ui.races.ManageSpiesUI(RotPUI.instance().racesUI());
            var enemyView = foreign.viewForEmpire(owner);
            int previous = enemyView.spies().allocation();
            HotSeatTestFixture.set(spies.getClass(), spies, "empireViews", List.of(enemyView));
            invoke(spies, "increaseSliderValue", 0);
            assertEquals(previous, enemyView.spies().allocation(), "Foreign spy budget cannot change");
            var ownView = own.viewForEmpire(other);
            previous = ownView.spies().allocation();
            HotSeatTestFixture.set(spies.getClass(), spies, "empireViews", List.of(ownView));
            invoke(spies, "increaseSliderValue", 0);
            assertEquals(previous + 1, ownView.spies().allocation());
            var home = own.allColonizedSystems().get(0);
            boolean forward = own.sv.forwardRallies(home.id);
            home.rallySprite().toggleForwardRallies();
            assertEquals(!forward, own.sv.forwardRallies(home.id));
            var enemyHome = foreign.allColonizedSystems().get(0);
            forward = own.sv.forwardRallies(enemyHome.id);
            enemyHome.rallySprite().toggleForwardRallies();
            assertEquals(forward, own.sv.forwardRallies(enemyHome.id));
        } catch (Exception failure) { throw new AssertionError(failure); }
    }
    private static Object invoke(Object target, String name, Object... arguments) throws Exception {
        Class<?>[] types = java.util.Arrays.stream(arguments).map(a -> a instanceof Integer ? int.class : a.getClass()).toArray(Class<?>[]::new);
        var method = target.getClass().getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(target, arguments);
    }
    private static rotp.ui.planets.MultiColonySpendingPane bulkPane(List<rotp.model.galaxy.StarSystem> systems) {
        try {
            var constructor = rotp.ui.planets.MultiColonySpendingPane.class.getDeclaredConstructor(
                    rotp.ui.SystemViewer.class, java.awt.Color.class, java.awt.Color.class,
                    java.awt.Color.class, java.awt.Color.class);
            constructor.setAccessible(true);
            rotp.ui.SystemViewer viewer = new rotp.ui.SystemViewer() {
                public rotp.model.galaxy.StarSystem systemViewToDisplay() { return systems.get(0); }
                public List<rotp.model.galaxy.StarSystem> systemsToDisplay() { return systems; }
                public void repaint() { }
            };
            return constructor.newInstance(viewer, java.awt.Color.BLACK, java.awt.Color.WHITE,
                    java.awt.Color.GRAY, java.awt.Color.DARK_GRAY);
        } catch (Exception failure) { throw new AssertionError(failure); }
    }
}
