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
import rotp.ui.multiplayer.*;
import rotp.ui.tech.SelectNewTechUI;
import rotp.model.empires.SpyNetwork.Sabotage;

@EnabledIfSystemProperty(named = "rotp.integration", matches = "true")
class HotSeatDecisionTest {
    @TempDir static Path directory;
    @BeforeAll static void initialize() throws Exception { HotSeatTestFixture.initialize(directory); }

    @Test void everyDecisionFamilyHasExplicitGraphicalLegalChoices() {
        var game = HotSeatTestFixture.start(1, 0, 1);
        String tech = game.galaxy().empire(0).tech().allKnownTechs().get(0);
        HotSeatTestFixture.onEdt(() -> {
            try (var desktop = new HotSeatDesktop()) {
                var panels = new HotSeatDecisionPanels(game, desktop);
                var vote = panels.councilVote(new PendingDecision("v", "human-0", 1, 0,
                        PendingDecision.Kind.COUNCIL_VOTE, List.of(0, 1), true));
                assertFalse(vote.isDone());
                clickFirst();
                assertEquals(0, vote.join());
                try { HotSeatTestFixture.set(game.galaxy().council().getClass(), game.galaxy().council(), "leader", game.galaxy().empire(1)); }
                catch (Exception ex) { throw new AssertionError(ex); }
                var ruling = panels.councilRuling(new PendingDecision("r", "human-0", 1, 0,
                        PendingDecision.Kind.COUNCIL_RULING, List.of(), false));
                assertTrue(visibleText(Rotp.getFrame().getGlassPane()).contains(game.galaxy().empire(1).name()),
                        "Ruling must identify the elected empire");
                clickFirst(); assertTrue(ruling.join());
                var category = game.galaxy().empire(0).tech().category(0);
                var legal = category.techIdsAvailableForResearch();
                String current = category.currentTech();
                var research = panels.research(new PendingResearchDecision("t", "human-0", 0, 0, legal));
                var screen = find(Rotp.getFrame().getGlassPane(), SelectNewTechUI.class);
                assertNotNull(screen, "Research must use the game's own selection screen");
                assertFalse(research.isDone());
                screen.consoleEntry("1");
                assertTrue(legal.contains(research.join()));
                assertEquals(current, category.currentTech(), "The turn engine, not the screen, applies the choice");
                var colonize = panels.colonize(new PendingColonizationDecision("c", "human-0", 0, 0, 0));
                clickFirst(); assertTrue(colonize.join());
                var diplomacy = panels.diplomacy(new PendingDiplomacyDecision("d", new DiplomacyNotice(
                        "human-0", 0, 1, null, rotp.ui.diplomacy.DialogueManager.OFFER_PACT, null, true)));
                clickFirst(); assertTrue(diplomacy.join());
                var bomb = panels.bombardment(new BombardmentDecision("human-0", 0, 1, 0, false, 0));
                assertEquals(2, buttons(Rotp.getFrame().getGlassPane()).size());
                clickFirst(); assertEquals(InProcessBombardmentDecisionAdapter.Choice.SKIP, bomb.join());
                var spy = panels.espionage(new EspionageDecision("human-0", 0, 1, 0, Map.of("COMPUTERS", tech), List.of(1)));
                clickFirst(); assertEquals(tech, spy.join().technologyId()); assertNull(spy.join().framedEmpireId());
                var sabotage = panels.sabotage(new SabotageDecision("human-0", 0, 1, 0,
                        Map.of(Sabotage.FACTORIES, List.of(0))));
                clickFirst(); assertEquals(new InProcessSabotageDecisionAdapter.Choice(Sabotage.FACTORIES, 0), sabotage.join());
            }
        });
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
