package rotp.multiplayer.turn;

/** A completed bombardment result addressed to one human empire. */
public record BombardmentNotice(int recipientEmpireId, int attackerEmpireId,
        int defenderEmpireId, int systemId, boolean targeted,
        float populationBefore, float populationAfter,
        float basesBefore, float basesAfter,
        float factoriesBefore, float factoriesAfter) { }
