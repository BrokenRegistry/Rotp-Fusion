package rotp.multiplayer.turn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rotp.model.empires.Empire;
import rotp.model.empires.EspionageMission;
import rotp.model.game.GameSession;
import rotp.model.tech.Tech;
import rotp.model.tech.TechCategory;
import rotp.model.tech.TechTree;
import rotp.multiplayer.session.PlayerSeat;

/** Synchronous owned decision provider for the existing espionage resolution. */
public final class InProcessEspionageDecisionAdapter implements EspionageDecisionAdapter {
    public record Choice(String technologyId, Integer framedEmpireId) { }

    @FunctionalInterface
    public interface Provider {
        Choice choose(EspionageDecision decision);
    }

    private final Map<String, Provider> providers;

    public InProcessEspionageDecisionAdapter(Map<String, Provider> providers) {
        this.providers = Map.copyOf(Objects.requireNonNull(providers, "providers"));
    }

    @Override
    public void present(GameSession session, EspionageMission mission, int victimEmpireId) {
        if (session.controllerRegistry() == null)
            throw new IllegalStateException("An espionage provider requires a controller roster");
        int empireId = mission.spyEmpire().id;
        PlayerSeat seat = session.controllerRegistry().seatForEmpire(empireId);
        if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
            throw new IllegalStateException("Espionage mission has no human owner");
        Map<String, String> categoryTechIds = new HashMap<>();
        Map<String, Tech> legalTechs = new HashMap<>();
        for (int i = 0; i < TechTree.NUM_CATEGORIES; i++) {
            String categoryId = TechCategory.id(i);
            Tech tech = mission.techChoice(categoryId);
            if (tech != null) {
                categoryTechIds.put(categoryId, tech.id);
                legalTechs.put(tech.id, tech);
            }
        }
        List<Integer> frameableIds = new ArrayList<>();
        if (mission.canFrame()) {
            for (Empire empire : mission.empiresToFrame())
                frameableIds.add(empire.id);
        }
        EspionageDecision decision = new EspionageDecision(seat.playerId(), empireId,
                victimEmpireId, mission.targetSystem().id, categoryTechIds, frameableIds, mission);
        Provider provider = providers.get(seat.playerId());
        if (provider == null)
            throw new IllegalStateException("No espionage provider for " + seat.playerId());
        Choice choice = Objects.requireNonNull(provider.choose(decision), "choice");
        Tech chosenTech = legalTechs.get(choice.technologyId());
        if (chosenTech == null)
            throw new IllegalArgumentException("Illegal technology for espionage decision");
        if (choice.framedEmpireId() != null
                && !frameableIds.contains(choice.framedEmpireId()))
            throw new IllegalArgumentException("Illegal empire to frame");
        mission.stealTech(chosenTech);
        if (choice.framedEmpireId() != null)
            mission.frameEmpire(session.galaxy().empire(choice.framedEmpireId()));
    }
}
