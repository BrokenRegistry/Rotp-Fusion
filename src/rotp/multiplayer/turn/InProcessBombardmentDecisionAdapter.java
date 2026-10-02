package rotp.multiplayer.turn;

import java.util.Map;
import java.util.Objects;

import rotp.model.empires.Empire;
import rotp.model.galaxy.ShipFleet;
import rotp.model.game.GameSession;
import rotp.multiplayer.session.PlayerSeat;

/** Synchronous bombing choice, followed by retryable recipient result delivery. */
public final class InProcessBombardmentDecisionAdapter implements BombardmentDecisionAdapter {
    public enum Choice { SKIP, BOMBARD, TARGET_BOMBARD }

    @FunctionalInterface
    public interface Provider {
        Choice choose(BombardmentDecision decision);
    }

    private final Map<String, Provider> providers;

    public InProcessBombardmentDecisionAdapter(Map<String, Provider> providers) {
        this.providers = Map.copyOf(Objects.requireNonNull(providers, "providers"));
    }

    @Override
    public void resolve(GameSession session, int systemId, ShipFleet fleet,
            boolean autoBomb, int bombingTarget) {
        if (session.controllerRegistry() == null)
            throw new IllegalStateException("Bombardment requires a controller roster");
        Empire attacker = fleet.empire();
        Empire defender = session.galaxy().system(systemId).empire();
        if (defender == null)
            return;
        PlayerSeat attackerSeat = session.controllerRegistry().seatForEmpire(attacker.id);
        // Unassigned empires are AI controlled, just as in ControllerRegistry.
        boolean humanAttacker = session.controllerRegistry().isHumanControlled(attacker.id);

        Choice choice;
        if (humanAttacker && !autoBomb) {
            Provider provider = providers.get(attackerSeat.playerId());
            if (provider == null)
                throw new IllegalStateException("No bombardment provider for "
                        + attackerSeat.playerId());
            boolean targetAllowed = session.options().targetBombardAllowedForPlayer();
            float limit = 0.5f + session.options().selectedBombingTarget();
            BombardmentDecision decision = new BombardmentDecision(attackerSeat.playerId(),
                    attacker.id, defender.id, systemId, targetAllowed, limit, fleet);
            choice = Objects.requireNonNull(provider.choose(decision), "bombardment choice");
            if (choice == Choice.TARGET_BOMBARD && !targetAllowed)
                throw new IllegalArgumentException("Target bombardment is disabled");
        }
        else
            choice = bombingTarget == 0 ? Choice.BOMBARD : Choice.TARGET_BOMBARD;
        if (choice == Choice.SKIP)
            return;

        defender.sv.refreshFullScan(systemId);
        float populationBefore = defender.sv.population(systemId);
        float basesBefore = defender.sv.bases(systemId);
        float factoriesBefore = defender.sv.factories(systemId);
        if (choice == Choice.TARGET_BOMBARD) {
            float limit = autoBomb || !humanAttacker
                    ? bombingTarget : 0.5f + session.options().selectedBombingTarget();
            fleet.targetBombard(limit);
        }
        else
            fleet.bombard();
        attacker.sv.refreshFullScan(systemId);
        defender.sv.refreshFullScan(systemId);
        for (PlayerSeat seat : session.controllerRegistry().seats()) {
            if (seat.controllerType() != PlayerSeat.ControllerType.HUMAN
                    || (seat.empireId() != attacker.id && seat.empireId() != defender.id))
                continue;
            BombardmentNotice notice = new BombardmentNotice(seat.empireId(), attacker.id,
                    defender.id, systemId, choice == Choice.TARGET_BOMBARD,
                    populationBefore, defender.sv.population(systemId),
                    basesBefore, defender.sv.bases(systemId),
                    factoriesBefore, defender.sv.factories(systemId));
            GameSession.addTurnNotificationForEmpire(seat.empireId(),
                    new BombardmentResultNotification(notice));
        }
    }
}
