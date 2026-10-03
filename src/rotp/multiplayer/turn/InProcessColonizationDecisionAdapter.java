package rotp.multiplayer.turn;

import java.util.Map;
import java.util.Objects;

import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GameSession;
import rotp.model.ships.ShipDesign;
import rotp.multiplayer.session.PlayerSeat;

/** Resolves colonization with a separate choice provider for each human seat. */
public final class InProcessColonizationDecisionAdapter implements ColonizationDecisionAdapter {
    @FunctionalInterface
    public interface HumanColonizationProvider {
        boolean colonize(PendingColonizationDecision decision);
    }

    private final Map<String, HumanColonizationProvider> providers;

    public InProcessColonizationDecisionAdapter(Map<String, HumanColonizationProvider> providers) {
        this.providers = Map.copyOf(Objects.requireNonNull(providers, "providers"));
    }

    @Override
    public void present(GameSession session, int systemId, ShipFleet fleet, ShipDesign design) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(fleet, "fleet");
        Objects.requireNonNull(design, "design");
        if (session.controllerRegistry() == null)
            throw new IllegalStateException("Colonization requires a controller roster");
        PlayerSeat seat = session.controllerRegistry().seatForEmpire(fleet.empId());
        if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
            throw new IllegalStateException("Colony ship has no owning human seat");
        StarSystem system = session.galaxy().system(systemId);
        if (!eligible(system, fleet, design))
            return;
        HumanColonizationProvider provider = providers.get(seat.playerId());
        if (provider == null)
            throw new IllegalStateException("No colonization provider for " + seat.playerId());
        String id = "colonize:" + session.id() + ":" + session.galaxy().currentTurn()
                + ":" + systemId + ":" + fleet.empId() + ":" + design.id();
        PendingColonizationDecision decision = new PendingColonizationDecision(id,
                seat.playerId(), fleet.empId(), systemId, design.id());
        if (provider.colonize(decision) && eligible(system, fleet, design))
            fleet.colonizeSystem(system, design);
    }

    public static boolean eligible(StarSystem system, ShipFleet fleet, ShipDesign design) {
        return system != null && !system.isColonized() && fleet.isOrbiting()
                && fleet.sysId() == system.id && fleet.hasShip(design)
                && fleet.empire().canColonize(system.planet().type())
                && !system.orbitingShipsBarColony(fleet);
    }
}
