package rotp.multiplayer.session;

import java.io.Serializable;
import java.util.Objects;

/** A stable controller assignment for one empire in a match. */
public record PlayerSeat(String playerId, int empireId, ControllerType controllerType,
        ConnectionStatus connectionStatus) implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ControllerType { HUMAN, AI }
    public enum ConnectionStatus { CONNECTED, DISCONNECTED, NOT_APPLICABLE }

    public PlayerSeat {
        if (playerId == null || playerId.isBlank())
            throw new IllegalArgumentException("A player ID is required");
        if (empireId < 0)
            throw new IllegalArgumentException("An empire ID must be nonnegative");
        Objects.requireNonNull(controllerType, "controllerType");
        Objects.requireNonNull(connectionStatus, "connectionStatus");
        if (controllerType == ControllerType.AI && connectionStatus != ConnectionStatus.NOT_APPLICABLE)
            throw new IllegalArgumentException("AI seats have no connection");
        if (controllerType == ControllerType.HUMAN && connectionStatus == ConnectionStatus.NOT_APPLICABLE)
            throw new IllegalArgumentException("Human seats require a connection status");
    }

    public PlayerSeat withConnectionStatus(ConnectionStatus status) {
        return new PlayerSeat(playerId, empireId, controllerType, status);
    }
}
