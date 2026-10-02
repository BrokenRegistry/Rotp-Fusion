package rotp.multiplayer.turn;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rotp.model.empires.SabotageMission;
import rotp.model.empires.SpyNetwork;
import rotp.model.empires.SpyNetwork.Sabotage;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GameSession;
import rotp.multiplayer.session.PlayerSeat;

/** Synchronous sabotage choice with a result delivered after the model phase. */
public final class InProcessSabotageDecisionAdapter implements SabotageDecisionAdapter {
    public record Choice(Sabotage action, int systemId) { }

    @FunctionalInterface
    public interface Provider {
        /** Return null to cancel the mission. */
        Choice choose(SabotageDecision decision);
    }

    private final Map<String, Provider> providers;

    public InProcessSabotageDecisionAdapter(Map<String, Provider> providers) {
        this.providers = Map.copyOf(Objects.requireNonNull(providers, "providers"));
    }

    @Override
    public void resolve(GameSession session, SabotageMission mission, int suggestedSystemId) {
        if (session.controllerRegistry() == null)
            throw new IllegalStateException("Sabotage requires a controller roster");
        SpyNetwork spies = mission.spies();
        int ownerId = spies.owner().id;
        PlayerSeat seat = session.controllerRegistry().seatForEmpire(ownerId);
        if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
            throw new IllegalStateException("Sabotage mission has no human owner");
        Provider provider = providers.get(seat.playerId());
        if (provider == null)
            throw new IllegalStateException("No sabotage provider for " + seat.playerId());

        SpyNetwork.SabotageTargets targets = spies.sabotageTargets();
        Map<Sabotage, List<Integer>> legal = new EnumMap<>(Sabotage.class);
        legal.put(Sabotage.FACTORIES, systemIds(targets.factoryTargets));
        legal.put(Sabotage.MISSILES, systemIds(targets.baseTargets));
        legal.put(Sabotage.REBELS, systemIds(targets.rebellionTargets));
        SabotageDecision decision = new SabotageDecision(seat.playerId(), ownerId,
                mission.target().id, suggestedSystemId, legal, mission);
        Choice choice = provider.choose(decision);
        if (choice == null) {
            mission.cancelMission();
            return;
        }
        if (choice.action() == null || !legal.get(choice.action()).contains(choice.systemId()))
            throw new IllegalArgumentException("Illegal sabotage target");
        StarSystem system = session.galaxy().system(choice.systemId());
        if (system == null || system.empire() != mission.target())
            throw new IllegalArgumentException("Sabotage target changed owner");
        switch (choice.action()) {
            case FACTORIES -> mission.destroyFactories(system);
            case MISSILES -> mission.destroyMissileBases(system);
            case REBELS -> mission.inciteRebellion(system);
        }
        session.enableSpyReport(ownerId);
        int amount = switch (choice.action()) {
            case FACTORIES -> mission.factoriesDestroyed();
            case MISSILES -> mission.missileBasesDestroyed();
            case REBELS -> mission.rebelsIncited();
        };
        GameSession.addTurnNotificationForEmpire(ownerId, new SabotageResultNotification(
                new SabotageNotice(ownerId, mission.target().id,
                        system.id, choice.action(), amount)));
    }

    private static List<Integer> systemIds(List<StarSystem> systems) {
        List<Integer> ids = new ArrayList<>();
        for (StarSystem system : systems)
            ids.add(system.id);
        return List.copyOf(ids);
    }
}
