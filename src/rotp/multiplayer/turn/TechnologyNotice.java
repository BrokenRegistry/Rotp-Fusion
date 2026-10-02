package rotp.multiplayer.turn;

import java.util.Objects;

/** Technology result visible to one empire after turn resolution. */
public record TechnologyNotice(Kind kind, int recipientEmpireId, String techId,
        Integer systemId, Integer sourceEmpireId) {
    public enum Kind { DISCOVERED, PLANET_PLUNDER, SHIP_PLUNDER, TRADED, STOLEN }

    public TechnologyNotice {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(techId, "techId");
    }
}
