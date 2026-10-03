package rotp.multiplayer.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Authoritative empire ownership; an unassigned empire remains AI controlled. */
public final class ControllerRegistry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Integer, PlayerSeat> seatsByEmpire = new LinkedHashMap<>();

    public void add(PlayerSeat seat) {
        Objects.requireNonNull(seat, "seat");
        if (seatsByEmpire.containsKey(seat.empireId()))
            throw new IllegalArgumentException("Empire already has a controller: " + seat.empireId());
        if (seatsByEmpire.values().stream().anyMatch(existing -> existing.playerId().equals(seat.playerId())))
            throw new IllegalArgumentException("Player ID already assigned: " + seat.playerId());
        seatsByEmpire.put(seat.empireId(), seat);
    }

    public PlayerSeat seatForEmpire(int empireId) {
        return seatsByEmpire.get(empireId);
    }

    public boolean isHumanControlled(int empireId) {
        PlayerSeat seat = seatForEmpire(empireId);
        return seat != null && seat.controllerType() == PlayerSeat.ControllerType.HUMAN;
    }

    public boolean isAIControlled(int empireId) {
        return !isHumanControlled(empireId);
    }

    public Collection<PlayerSeat> seats() {
        return Collections.unmodifiableCollection(seatsByEmpire.values());
    }
}
