package rotp.multiplayer.pbem;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Match state for save-passing play; its presence on a session is the mode. */
public final class PlayByEmail implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String matchLabel;
    private final List<String> players;
    private final HashMap<String, StandingOrders> orders = new HashMap<>();
    private final HashMap<String, PinHash> pins = new HashMap<>();

    public PlayByEmail(List<String> playerIds, LocalDateTime created) {
        players = List.copyOf(playerIds);
        matchLabel = TurnFiles.matchLabel(created);
        for (String id : players) orders.put(id, StandingOrders.DEFAULT);
    }

    public String matchLabel() { return matchLabel; }

    public synchronized StandingOrders orders(String playerId) {
        return orders.getOrDefault(require(playerId), StandingOrders.DEFAULT);
    }
    public synchronized void orders(String playerId, StandingOrders value) {
        orders.put(require(playerId), Objects.requireNonNull(value));
    }

    public synchronized boolean hasPin(String playerId) { return pins.containsKey(require(playerId)); }
    public synchronized boolean pinMatches(String playerId, String pin) {
        PinHash hash = pins.get(require(playerId));
        return hash != null && hash.matches(pin);
    }
    /** Only the first unlock sets a PIN; it cannot be replaced from a turn file. */
    public synchronized void setPin(String playerId, String pin) {
        if (pins.containsKey(require(playerId))) throw new IllegalStateException("PIN already set");
        pins.put(playerId, PinHash.create(pin));
    }

    /** Snapshot of all standing orders, for tests and the finish screen. */
    public synchronized Map<String, StandingOrders> allOrders() { return Map.copyOf(orders); }

    private String require(String playerId) {
        if (!players.contains(playerId)) throw new IllegalArgumentException("Unknown player " + playerId);
        return playerId;
    }
}
