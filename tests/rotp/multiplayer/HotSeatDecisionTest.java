package rotp.multiplayer;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import rotp.Rotp;
import rotp.multiplayer.turn.*;
import java.awt.event.KeyEvent;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.multiplayer.*;
import rotp.ui.tech.SelectNewTechUI;
import rotp.model.empires.SpyNetwork.Sabotage;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatDecisionTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @Test void everyDecisionFamilyHasExplicitGraphicalLegalChoices() throws Exception {
        var game = HotSeatTestFixture.start(1, 0, 1);
        String tech = game.galaxy().empire(0).tech().allKnownTechs().get(0);
        HotSeatDesktop[] desktop = new HotSeatDesktop[1];
        HotSeatDecisionPanels[] panels = new HotSeatDecisionPanels[1];
        @SuppressWarnings("unchecked") CompletableFuture<Boolean>[] answer = new CompletableFuture[1];
        HotSeatTestFixture.onEdt(() -> {
            desktop[0] = new HotSeatDesktop();
            panels[0] = new HotSeatDecisionPanels(game, desktop[0]);
            // No vote in progress: the plain list of legal choices.
            var vote = panels[0].councilVote(new PendingDecision("v", "human-0", 1, 0,
                    PendingDecision.Kind.COUNCIL_VOTE, List.of(0, 1), true));
            assertFalse(vote.isDone());
            clickFirst();
            assertEquals(0, vote.join());
            try { HotSeatTestFixture.set(game.galaxy().council().getClass(), game.galaxy().council(), "leader", game.galaxy().empire(1)); }
            catch (Exception ex) { throw new AssertionError(ex); }
            answer[0] = panels[0].councilRuling(new PendingDecision("r", "human-0", 1, 0,
                    PendingDecision.Kind.COUNCIL_RULING, List.of(), false));
            assertInstanceOf(rotp.ui.GalacticCouncilUI.class, RotPUI.instance().selectedPanel(),
                    "The ruling uses the Galactic Council screen");
            var leader = game.galaxy().council().leader();
            press(RotPUI.instance().selectedPanel(), KeyEvent.VK_1);
            assertTrue(answer[0].join());
            assertSame(leader, game.galaxy().council().leader(), "The turn engine, not the screen, applies the ruling");
            var category = game.galaxy().empire(0).tech().category(0);
            var legal = category.techIdsAvailableForResearch();
            String current = category.currentTech();
            var research = panels[0].research(new PendingResearchDecision("t", "human-0", 0, 0, legal));
            var screen = find(Rotp.getFrame().getGlassPane(), SelectNewTechUI.class);
            assertNotNull(screen, "Research must use the game's own selection screen");
            assertFalse(research.isDone());
            screen.consoleEntry("1");
            assertTrue(legal.contains(research.join()));
            assertEquals(current, category.currentTech(), "The turn engine, not the screen, applies the choice");
            var colonize = panels[0].colonize(new PendingColonizationDecision("c", "human-0", 0, 0, 0));
            clickFirst(); assertTrue(colonize.join());
            answer[0] = panels[0].diplomacy(new PendingDiplomacyDecision("d", new DiplomacyNotice(
                    "human-0", 0, 1, null, rotp.ui.diplomacy.DialogueManager.OFFER_PACT, null, true)));
            assertInstanceOf(rotp.ui.tech.DiplomaticMessageUI.class, RotPUI.instance().selectedPanel(),
                    "Offers use the diplomacy screen");
        });
        // The envoy finishes speaking before replies are accepted, as in single player.
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
        while (!answer[0].isDone() && System.nanoTime() < deadline) {
            HotSeatTestFixture.onEdt(() -> press(RotPUI.instance().selectedPanel(), KeyEvent.VK_1));
            Thread.sleep(100);
        }
        assertTrue(answer[0].join(), "The first reply accepts the offer");
        assertFalse(game.galaxy().empire(0).pactWith(1), "The turn engine, not the screen, forms the pact");
        HotSeatTestFixture.onEdt(() -> {
            try {
                var bomb = panels[0].bombardment(new BombardmentDecision("human-0", 0, 1, 0, false, 0));
                assertEquals(2, buttons(Rotp.getFrame().getGlassPane()).size());
                clickFirst(); assertEquals(InProcessBombardmentDecisionAdapter.Choice.SKIP, bomb.join());
                var spy = panels[0].espionage(new EspionageDecision("human-0", 0, 1, 0, Map.of("COMPUTERS", tech), List.of(1)));
                clickFirst(); assertEquals(tech, spy.join().technologyId()); assertNull(spy.join().framedEmpireId());
                var sabotage = panels[0].sabotage(new SabotageDecision("human-0", 0, 1, 0,
                        Map.of(Sabotage.FACTORIES, List.of(0))));
                clickFirst(); assertEquals(new InProcessSabotageDecisionAdapter.Choice(Sabotage.FACTORIES, 0), sabotage.join());
            } finally { desktop[0].close(); }
        });
    }

    private static void press(Component target, int key) {
        ((BasePanel) target).keyPressed(new KeyEvent(target, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED));
    }
    private static <T> T find(Component component, Class<T> type) {
        if (type.isInstance(component)) return type.cast(component);
        if (component instanceof Container container)
            for (Component child : container.getComponents()) {
                T found = find(child, type);
                if (found != null) return found;
            }
        return null;
    }
    private static void clickFirst() { buttons(Rotp.getFrame().getGlassPane()).get(0).doClick(0); }
    private static String visibleText(Component component) {
        String value = component instanceof JTextArea area ? area.getText() : "";
        if (component instanceof Container container)
            for (Component child : container.getComponents()) value += visibleText(child);
        return value;
    }
    private static List<JButton> buttons(Component component) {
        List<JButton> result = new ArrayList<>();
        if (component instanceof JButton button && button.getClass() == JButton.class) result.add(button);
        else if (component instanceof Container container)
            for (Component child : container.getComponents()) result.addAll(buttons(child));
        return result;
    }
}
