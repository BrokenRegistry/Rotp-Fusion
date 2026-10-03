package rotp.multiplayer.turn;

import java.io.Serializable;
import java.util.Objects;

import rotp.model.incidents.DiplomaticIncident;

/** A diplomatic message and its legal in-process response. */
public record DiplomacyNotice(String ownerPlayerId, int recipientEmpireId,
        int talkerEmpireId, Integer targetEmpireId, String messageType,
        Integer tradeAmount, boolean responseRequired,
        DiplomaticIncident incident) implements Serializable {
    private static final long serialVersionUID = 1L;
    public DiplomacyNotice {
        Objects.requireNonNull(ownerPlayerId, "ownerPlayerId");
        Objects.requireNonNull(messageType, "messageType");
    }
    public DiplomacyNotice(String ownerPlayerId, int recipientEmpireId, int talkerEmpireId,
            Integer targetEmpireId, String messageType, Integer tradeAmount, boolean responseRequired) {
        this(ownerPlayerId, recipientEmpireId, talkerEmpireId, targetEmpireId, messageType,
                tradeAmount, responseRequired, null);
    }
}
