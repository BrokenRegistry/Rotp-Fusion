package rotp.multiplayer.hotseat;

import java.util.Objects;
import rotp.model.game.GameSession;
import rotp.multiplayer.turn.DiplomacyNotice;
import rotp.ui.diplomacy.DialogueManager;

/** Queues human offers without invoking an AI's receive-offer decision. */
public final class HotSeatDiplomacy {
    public enum Offer { TRADE, PEACE, PACT, ALLIANCE, JOINT_WAR }
    public enum Result { QUEUED, WRONG_OWNER, ILLEGAL, DUPLICATE }
    private final GameSession game;
    public HotSeatDiplomacy(GameSession game) { this.game = Objects.requireNonNull(game); }

    public synchronized Result submit(String playerId, long revision, Offer offer,
            int recipientEmpireId, int tradeLevel, Integer targetId) {
        if (game.hotSeatState() == null) return Result.WRONG_OWNER;
        var snapshot = game.hotSeatState().snapshot();
        if (snapshot.stage() != HotSeatState.Stage.PLANNING || snapshot.revision() != revision
                || !Objects.equals(snapshot.ownerPlayerId(), playerId)) return Result.WRONG_OWNER;
        var actorSeat = game.controllerRegistry().seats().stream()
                .filter(s -> s.playerId().equals(playerId)).findFirst().orElse(null);
        var recipientSeat = game.controllerRegistry().seatForEmpire(recipientEmpireId);
        if (actorSeat == null || recipientSeat == null || offer == null
                || !game.controllerRegistry().isHumanControlled(recipientEmpireId)
                || actorSeat.empireId() == recipientEmpireId) return Result.ILLEGAL;
        var actor = game.galaxy().empire(actorSeat.empireId());
        var recipient = game.galaxy().empire(recipientEmpireId);
        if (actor.extinct() || recipient.extinct() || !actor.hasContact(recipient)
                || !recipient.hasContact(actor)) return Result.ILLEGAL;
        String type = switch (offer) {
            case TRADE -> DialogueManager.OFFER_TRADE;
            case PEACE -> DialogueManager.OFFER_PEACE;
            case PACT -> DialogueManager.OFFER_PACT;
            case ALLIANCE -> DialogueManager.OFFER_ALLIANCE;
            case JOINT_WAR -> DialogueManager.OFFER_JOINT_WAR;
        };
        for (var pending : game.pendingDiplomacyDecisions()) {
            var n = pending.notice();
            if (n.talkerEmpireId() == actor.id && n.recipientEmpireId() == recipient.id
                    && n.messageType().equals(type)) return Result.DUPLICATE;
        }
        var notice = new DiplomacyNotice(recipientSeat.playerId(), recipient.id,
                actor.id, offer == Offer.JOINT_WAR ? targetId : null, type,
                offer == Offer.TRADE ? tradeLevel : null, true);
        if (!rotp.multiplayer.turn.DiplomacyDecisionActions.eligible(game, notice)) return Result.ILLEGAL;
        game.deferDiplomacyDecision(notice);
        return Result.QUEUED;
    }
}
