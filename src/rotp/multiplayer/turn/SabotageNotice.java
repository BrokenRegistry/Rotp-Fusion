package rotp.multiplayer.turn;

import java.io.Serializable;

import rotp.model.empires.SpyNetwork.Sabotage;

/** Result of one sabotage mission, addressed to its human owner. */
public record SabotageNotice(int recipientEmpireId, int victimEmpireId,
        int systemId, Sabotage action, int amount) implements Serializable {
    private static final long serialVersionUID = 1L;
}
