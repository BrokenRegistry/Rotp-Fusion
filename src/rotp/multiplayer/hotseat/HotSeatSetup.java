package rotp.multiplayer.hotseat;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import rotp.multiplayer.session.ControllerRegistry;
import rotp.multiplayer.session.PlayerSeat;

/** Stable human identities, separate from the empire races and AI settings. */
public record HotSeatSetup(List<Assignment> humans) implements Serializable {
    private static final long serialVersionUID = 1L;

    public record Assignment(String playerId, String displayName, int empireId)
            implements Serializable {
        private static final long serialVersionUID = 1L;
        public Assignment {
            if (playerId == null || playerId.isBlank() || displayName == null
                    || displayName.isBlank() || empireId < 0)
                throw new IllegalArgumentException("Each human needs an ID, name and empire");
            playerId = playerId.trim();
            displayName = displayName.trim();
        }
    }

    public HotSeatSetup {
        if (humans == null || humans.size() < 2)
            throw new IllegalArgumentException("Hot seat requires at least two humans");
        humans = List.copyOf(humans);
        var ids = new HashSet<String>();
        var names = new HashSet<String>();
        var empires = new HashSet<Integer>();
        for (Assignment human : humans)
            if (!ids.add(human.playerId())
                    || !names.add(human.displayName().toLowerCase(Locale.ROOT))
                    || !empires.add(human.empireId()))
                throw new IllegalArgumentException("Human IDs, names and empires must be unique");
    }

    public ControllerRegistry registry(int empireCount) {
        var registry = new ControllerRegistry();
        for (Assignment human : humans) {
            if (human.empireId() >= empireCount)
                throw new IllegalArgumentException("A human assignment exceeds the empire count");
            registry.add(new PlayerSeat(human.playerId(), human.empireId(),
                    PlayerSeat.ControllerType.HUMAN, PlayerSeat.ConnectionStatus.CONNECTED));
        }
        return registry;
    }

    public String displayName(String playerId) {
        return humans.stream().filter(h -> h.playerId().equals(playerId))
                .map(Assignment::displayName).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown human player"));
    }
}
