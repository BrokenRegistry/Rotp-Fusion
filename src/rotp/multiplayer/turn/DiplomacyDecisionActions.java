package rotp.multiplayer.turn;

import rotp.model.empires.Empire;
import rotp.model.game.GameSession;
import rotp.ui.diplomacy.DialogueManager;

/** Applies the same diplomacy actions used by the desktop offer buttons. */
public final class DiplomacyDecisionActions {
    private DiplomacyDecisionActions() { }

    public static boolean isOffer(String type) {
        return DialogueManager.OFFER_TRADE.equals(type)
                || DialogueManager.OFFER_PEACE.equals(type)
                || DialogueManager.OFFER_PACT.equals(type)
                || DialogueManager.OFFER_ALLIANCE.equals(type)
                || DialogueManager.OFFER_JOINT_WAR.equals(type);
    }

    /** Shared queue/reply eligibility: planning may invalidate an earlier offer. */
    public static boolean eligible(GameSession session, DiplomacyNotice notice) {
        var galaxy = session.galaxy();
        int sender = notice.talkerEmpireId(), receiver = notice.recipientEmpireId();
        if (sender < 0 || receiver < 0 || sender >= galaxy.numEmpires()
                || receiver >= galaxy.numEmpires() || sender == receiver) return false;
        Empire actor = galaxy.empire(sender), recipient = galaxy.empire(receiver);
        if (actor.extinct() || recipient.extinct() || !actor.hasContact(recipient)
                || !recipient.hasContact(actor)) return false;
        boolean war = actor.atWarWith(receiver);
        return switch (notice.messageType()) {
            case DialogueManager.OFFER_TRADE -> !war && notice.tradeAmount() != null
                    && notice.tradeAmount() > actor.viewForEmpire(recipient).trade().level()
                    && notice.tradeAmount() <= actor.viewForEmpire(recipient).trade().maxLevel()
                    && actor.viewForEmpire(recipient).nominalTradeLevels().contains(notice.tradeAmount());
            case DialogueManager.OFFER_PEACE -> war;
            case DialogueManager.OFFER_PACT -> !war && actor.tradingWith(recipient)
                    && !actor.pactWith(receiver) && !actor.alliedWith(receiver);
            case DialogueManager.OFFER_ALLIANCE -> !war && actor.pactWith(receiver)
                    && !actor.alliedWith(receiver);
            case DialogueManager.OFFER_JOINT_WAR -> !war && notice.targetEmpireId() != null
                    && notice.targetEmpireId() >= 0 && notice.targetEmpireId() < galaxy.numEmpires()
                    && notice.targetEmpireId() != sender && notice.targetEmpireId() != receiver
                    && !galaxy.empire(notice.targetEmpireId()).extinct()
                    && actor.hasContact(galaxy.empire(notice.targetEmpireId()))
                    && recipient.hasContact(galaxy.empire(notice.targetEmpireId()))
                    && !recipient.alliedWith(notice.targetEmpireId()) && !recipient.atWarWith(notice.targetEmpireId());
            default -> false;
        };
    }

    public static boolean apply(GameSession session, DiplomacyNotice notice, boolean accept) {
        if (!notice.responseRequired() || !isOffer(notice.messageType()))
            throw new IllegalArgumentException("Diplomatic notice has no offer response");
        Empire recipient = session.galaxy().empire(notice.recipientEmpireId());
        Empire talker = session.galaxy().empire(notice.talkerEmpireId());
        if (recipient == null || talker == null)
            throw new IllegalStateException("Diplomatic offer refers to a missing empire");
        if (session.hotSeatState() != null && !eligible(session, notice)) return false;
        // Earlier replies at the same boundary can invalidate a later offer.
        // Consume that reply without creating a treaty for extinct or estranged empires.
        if (recipient.extinct() || talker.extinct() || !recipient.hasContact(talker))
            return false;
        String type = notice.messageType();
        if (DialogueManager.OFFER_TRADE.equals(type)) {
            if (notice.tradeAmount() == null)
                throw new IllegalStateException("Trade offer has no amount");
            if (accept && (notice.tradeAmount() <= 0
                    || notice.tradeAmount() > recipient.viewForEmpire(talker).trade().maxLevel()
                    || notice.tradeAmount() <= recipient.viewForEmpire(talker).trade().level()))
                return false;
            if (accept && !recipient.atWarWith(talker.id))
                recipient.diplomatAI().acceptOfferTrade(talker, notice.tradeAmount());
            else
                recipient.diplomatAI().refuseOfferTrade(talker, notice.tradeAmount());
        }
        else if (DialogueManager.OFFER_PEACE.equals(type)) {
            if (!recipient.atWarWith(talker.id))
                return false;
            if (accept)
                recipient.diplomatAI().acceptOfferPeace(talker);
            else
                recipient.diplomatAI().refuseOfferPeace(talker);
        }
        else if (DialogueManager.OFFER_PACT.equals(type)) {
            if (recipient.atWarWith(talker.id) || recipient.pactWith(talker.id)
                    || recipient.alliedWith(talker.id))
                return false;
            if (accept)
                recipient.diplomatAI().acceptOfferPact(talker);
            else
                recipient.diplomatAI().refuseOfferPact(talker);
        }
        else if (DialogueManager.OFFER_ALLIANCE.equals(type)) {
            if (recipient.atWarWith(talker.id) || recipient.alliedWith(talker.id))
                return false;
            if (accept)
                recipient.diplomatAI().acceptOfferAlliance(talker);
            else
                recipient.diplomatAI().refuseOfferAlliance(talker);
        }
        else {
            Empire target = notice.targetEmpireId() == null ? null
                    : session.galaxy().empire(notice.targetEmpireId());
            if (target == null)
                throw new IllegalStateException("Joint-war offer has no target empire");
            if (target.extinct() || target == recipient || target == talker
                    || recipient.atWarWith(talker.id) || recipient.alliedWith(target.id))
                return false;
            if (accept)
                recipient.diplomatAI().acceptOfferJointWar(talker, target);
            else
                recipient.diplomatAI().refuseOfferJointWar(talker, target);
        }
        return true;
    }
}
