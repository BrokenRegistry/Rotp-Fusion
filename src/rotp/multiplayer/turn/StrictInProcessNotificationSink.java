package rotp.multiplayer.turn;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import rotp.model.game.GameSession;
import rotp.multiplayer.session.PlayerSeat;
import rotp.ui.diplomacy.DialogueManager;
import rotp.ui.notifications.ColonizeSystemNotification;
import rotp.ui.notifications.DiscoverTechNotification;
import rotp.ui.notifications.DiplomaticNotification;
import rotp.ui.notifications.PlunderShipTechNotification;
import rotp.ui.notifications.PlunderTechNotification;
import rotp.ui.notifications.SelectTechNotification;
import rotp.ui.notifications.ShipConstructionNotification;
import rotp.ui.notifications.StealTechNotification;
import rotp.ui.notifications.SystemsScoutedNotification;
import rotp.ui.notifications.TradeTechNotification;
import rotp.ui.notifications.TurnNotification;

/** Supports extracted in-process prompts and rejects all unadapted UI notices. */
public final class StrictInProcessNotificationSink implements TurnNotificationSink {
    @FunctionalInterface
    public interface ScoutingNoticeProvider {
        void present(int empireId, Map<String, List<Integer>> systemIdsBySource);
    }
    @FunctionalInterface
    public interface TechnologyNoticeProvider {
        void present(TechnologyNotice notice);
    }
    @FunctionalInterface
    public interface ShipConstructionNoticeProvider {
        void present(int empireId, Map<Integer, Integer> designCounts);
    }
    @FunctionalInterface
    public interface DiplomacyNoticeProvider {
        /** Return true to accept an offer; the value is ignored for information messages. */
        boolean present(DiplomacyNotice notice);
    }
    @FunctionalInterface
    public interface BombardmentNoticeProvider {
        void present(BombardmentNotice notice);
    }
    @FunctionalInterface
    public interface SabotageNoticeProvider {
        void present(SabotageNotice notice);
    }

    private final InProcessResearchDecisionAdapter researchAdapter;
    private final InProcessColonizationDecisionAdapter colonizationAdapter;
    private final Map<String, ScoutingNoticeProvider> scoutingProviders;
    private final Map<String, TechnologyNoticeProvider> technologyProviders;
    private final Map<String, ShipConstructionNoticeProvider> shipConstructionProviders;
    private final Map<String, DiplomacyNoticeProvider> diplomacyProviders;
    private final Map<String, BombardmentNoticeProvider> bombardmentProviders;
    private final Map<String, SabotageNoticeProvider> sabotageProviders;
    private final boolean deferResearch;
    private final boolean deferColonization;
    private final boolean deferDiplomacy;

    public StrictInProcessNotificationSink(InProcessResearchDecisionAdapter researchAdapter) {
        this(researchAdapter, null);
    }

    public StrictInProcessNotificationSink(InProcessResearchDecisionAdapter researchAdapter,
            InProcessColonizationDecisionAdapter colonizationAdapter) {
        this(researchAdapter, colonizationAdapter, Map.of());
    }

    public StrictInProcessNotificationSink(InProcessResearchDecisionAdapter researchAdapter,
            InProcessColonizationDecisionAdapter colonizationAdapter,
            Map<String, ScoutingNoticeProvider> scoutingProviders) {
        this(researchAdapter, colonizationAdapter, scoutingProviders, Map.of());
    }

    public StrictInProcessNotificationSink(InProcessResearchDecisionAdapter researchAdapter,
            InProcessColonizationDecisionAdapter colonizationAdapter,
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders) {
        this(researchAdapter, colonizationAdapter, scoutingProviders, technologyProviders,
                Map.of());
    }

    public StrictInProcessNotificationSink(InProcessResearchDecisionAdapter researchAdapter,
            InProcessColonizationDecisionAdapter colonizationAdapter,
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders) {
        this(researchAdapter, colonizationAdapter, scoutingProviders, technologyProviders,
                shipConstructionProviders, Map.of());
    }

    public StrictInProcessNotificationSink(InProcessResearchDecisionAdapter researchAdapter,
            InProcessColonizationDecisionAdapter colonizationAdapter,
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders,
            Map<String, DiplomacyNoticeProvider> diplomacyProviders) {
        this(researchAdapter, colonizationAdapter, scoutingProviders, technologyProviders,
                shipConstructionProviders, diplomacyProviders, Map.of());
    }

    public StrictInProcessNotificationSink(InProcessResearchDecisionAdapter researchAdapter,
            InProcessColonizationDecisionAdapter colonizationAdapter,
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders,
            Map<String, DiplomacyNoticeProvider> diplomacyProviders,
            Map<String, BombardmentNoticeProvider> bombardmentProviders) {
        this(researchAdapter, colonizationAdapter, scoutingProviders, technologyProviders,
                shipConstructionProviders, diplomacyProviders, bombardmentProviders, Map.of());
    }

    public StrictInProcessNotificationSink(InProcessResearchDecisionAdapter researchAdapter,
            InProcessColonizationDecisionAdapter colonizationAdapter,
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders,
            Map<String, DiplomacyNoticeProvider> diplomacyProviders,
            Map<String, BombardmentNoticeProvider> bombardmentProviders,
            Map<String, SabotageNoticeProvider> sabotageProviders) {
        this(researchAdapter, colonizationAdapter, scoutingProviders, technologyProviders,
                shipConstructionProviders, diplomacyProviders, bombardmentProviders,
                sabotageProviders, false, false, false);
    }

    public static StrictInProcessNotificationSink deferredResearch(
            InProcessColonizationDecisionAdapter colonizationAdapter,
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders,
            Map<String, DiplomacyNoticeProvider> diplomacyProviders) {
        return new StrictInProcessNotificationSink(null, colonizationAdapter,
                scoutingProviders, technologyProviders, shipConstructionProviders,
                diplomacyProviders, Map.of(), Map.of(), true, false, false);
    }
    public static StrictInProcessNotificationSink deferredResearchAndColonization(
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders,
            Map<String, DiplomacyNoticeProvider> diplomacyProviders) {
        return new StrictInProcessNotificationSink(null, null, scoutingProviders,
                technologyProviders, shipConstructionProviders, diplomacyProviders,
                Map.of(), Map.of(), true, true, false);
    }
    public static StrictInProcessNotificationSink deferredDecisions(
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders,
            Map<String, DiplomacyNoticeProvider> diplomacyInformationProviders) {
        return deferredDecisions(scoutingProviders, technologyProviders,
                shipConstructionProviders, diplomacyInformationProviders, Map.of());
    }
    public static StrictInProcessNotificationSink deferredDecisions(
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders,
            Map<String, DiplomacyNoticeProvider> diplomacyInformationProviders,
            Map<String, BombardmentNoticeProvider> bombardmentProviders) {
        return deferredDecisions(scoutingProviders, technologyProviders,
                shipConstructionProviders, diplomacyInformationProviders,
                bombardmentProviders, Map.of());
    }
    public static StrictInProcessNotificationSink deferredDecisions(
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders,
            Map<String, DiplomacyNoticeProvider> diplomacyInformationProviders,
            Map<String, BombardmentNoticeProvider> bombardmentProviders,
            Map<String, SabotageNoticeProvider> sabotageProviders) {
        return new StrictInProcessNotificationSink(null, null, scoutingProviders,
                technologyProviders, shipConstructionProviders, diplomacyInformationProviders,
                bombardmentProviders, sabotageProviders, true, true, true);
    }

    private StrictInProcessNotificationSink(InProcessResearchDecisionAdapter researchAdapter,
            InProcessColonizationDecisionAdapter colonizationAdapter,
            Map<String, ScoutingNoticeProvider> scoutingProviders,
            Map<String, TechnologyNoticeProvider> technologyProviders,
            Map<String, ShipConstructionNoticeProvider> shipConstructionProviders,
            Map<String, DiplomacyNoticeProvider> diplomacyProviders,
            Map<String, BombardmentNoticeProvider> bombardmentProviders,
            Map<String, SabotageNoticeProvider> sabotageProviders, boolean deferResearch,
            boolean deferColonization, boolean deferDiplomacy) {
        this.researchAdapter = deferResearch ? researchAdapter
                : Objects.requireNonNull(researchAdapter, "researchAdapter");
        this.colonizationAdapter = colonizationAdapter;
        this.scoutingProviders = Map.copyOf(Objects.requireNonNull(scoutingProviders,
                "scoutingProviders"));
        this.technologyProviders = Map.copyOf(Objects.requireNonNull(technologyProviders,
                "technologyProviders"));
        this.shipConstructionProviders = Map.copyOf(Objects.requireNonNull(
                shipConstructionProviders, "shipConstructionProviders"));
        this.diplomacyProviders = Map.copyOf(Objects.requireNonNull(
                diplomacyProviders, "diplomacyProviders"));
        this.bombardmentProviders = Map.copyOf(Objects.requireNonNull(
                bombardmentProviders, "bombardmentProviders"));
        this.sabotageProviders = Map.copyOf(Objects.requireNonNull(
                sabotageProviders, "sabotageProviders"));
        this.deferResearch = deferResearch;
        this.deferColonization = deferColonization;
        this.deferDiplomacy = deferDiplomacy;
    }
    public boolean defersResearch() { return deferResearch; }
    public boolean defersColonization() { return deferColonization; }
    public boolean defersDiplomacy() { return deferDiplomacy; }

    @Override
    public void deliver(GameSession session, List<QueuedTurnNotification> notifications) {
        for (QueuedTurnNotification entry : notifications) {
            if (entry.notification() instanceof SelectTechNotification research) {
                if (entry.recipientEmpireId() == null
                        || entry.recipientEmpireId() != research.category().empire().id)
                    throw new IllegalStateException("Research notification has no matching empire recipient");
                if (deferResearch && new ResearchDecisionRouter(session).pending(
                        entry.recipientEmpireId(), research.category().index()) == null)
                    throw new IllegalStateException("Research notice has no pending owned choice");
            }
            else if (entry.notification() instanceof ColonizeSystemNotification colonization) {
                if (!deferColonization && colonizationAdapter == null)
                    throw new UnsupportedOperationException("No in-process colonization adapter");
                if (entry.recipientEmpireId() == null
                        || entry.recipientEmpireId() != colonization.fleet().empId())
                    throw new IllegalStateException("Colonization notification has no matching empire recipient");
            }
            else if (entry.notification() instanceof SystemsScoutedNotification) {
                if (entry.recipientEmpireId() == null || session.controllerRegistry() == null)
                    throw new IllegalStateException("Scouting notification has no empire recipient");
                var seat = session.controllerRegistry().seatForEmpire(entry.recipientEmpireId());
                if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN
                        || !scoutingProviders.containsKey(seat.playerId()))
                    throw new UnsupportedOperationException("No scouting provider for empire "
                            + entry.recipientEmpireId());
            }
            else if (technologyNotice(entry.notification(), entry.recipientEmpireId()) != null) {
                if (entry.recipientEmpireId() == null || session.controllerRegistry() == null)
                    throw new IllegalStateException("Technology notice has no empire recipient");
                var seat = session.controllerRegistry().seatForEmpire(entry.recipientEmpireId());
                if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN
                        || !technologyProviders.containsKey(seat.playerId()))
                    throw new UnsupportedOperationException("No technology provider for empire "
                            + entry.recipientEmpireId());
            }
            else if (entry.notification() instanceof ShipConstructionNotification) {
                if (entry.recipientEmpireId() == null || session.controllerRegistry() == null)
                    throw new IllegalStateException("Ship construction notice has no empire recipient");
                var seat = session.controllerRegistry().seatForEmpire(entry.recipientEmpireId());
                if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN
                        || !shipConstructionProviders.containsKey(seat.playerId()))
                    throw new UnsupportedOperationException("No construction provider for empire "
                            + entry.recipientEmpireId());
            }
            else if (entry.notification() instanceof DiplomaticNotification diplomacy) {
                if (entry.recipientEmpireId() == null || session.controllerRegistry() == null
                        || diplomacy.view() == null
                        || entry.recipientEmpireId() != diplomacy.view().empId())
                    throw new IllegalStateException("Diplomacy notice has no matching empire recipient");
                if (DialogueManager.OFFER_JOINT_WAR.equals(diplomacy.type())
                        && diplomacy.otherEmpire() == null)
                    throw new IllegalStateException("Joint-war offer has no target empire");
                var seat = session.controllerRegistry().seatForEmpire(entry.recipientEmpireId());
                boolean deferredOffer = deferDiplomacy
                        && DiplomacyDecisionActions.isOffer(diplomacy.type());
                if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN
                        || (!deferredOffer && !diplomacyProviders.containsKey(seat.playerId())))
                    throw new UnsupportedOperationException("No diplomacy provider for empire "
                            + entry.recipientEmpireId());
            }
            else if (entry.notification() instanceof BombardmentResultNotification bombardment) {
                BombardmentNotice notice = bombardment.notice();
                if (entry.recipientEmpireId() == null || session.controllerRegistry() == null
                        || entry.recipientEmpireId() != notice.recipientEmpireId())
                    throw new IllegalStateException("Bombardment result has no matching recipient");
                var seat = session.controllerRegistry().seatForEmpire(entry.recipientEmpireId());
                if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN
                        || !bombardmentProviders.containsKey(seat.playerId()))
                    throw new UnsupportedOperationException("No bombardment provider for empire "
                            + entry.recipientEmpireId());
            }
            else if (entry.notification() instanceof SabotageResultNotification sabotage) {
                SabotageNotice notice = sabotage.notice();
                if (entry.recipientEmpireId() == null || session.controllerRegistry() == null
                        || entry.recipientEmpireId() != notice.recipientEmpireId())
                    throw new IllegalStateException("Sabotage result has no matching recipient");
                var seat = session.controllerRegistry().seatForEmpire(entry.recipientEmpireId());
                if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN
                        || !sabotageProviders.containsKey(seat.playerId()))
                    throw new UnsupportedOperationException("No sabotage provider for empire "
                            + entry.recipientEmpireId());
            }
            else
                throw new UnsupportedOperationException(
                        "Turn notification needs an in-process adapter: "
                                + entry.notification().getClass().getName());
        }
        for (QueuedTurnNotification entry : notifications) {
            if (entry.notification() instanceof SelectTechNotification research) {
                if (!deferResearch)
                    researchAdapter.presentSelection(session, research.category());
            }
            else if (entry.notification() instanceof ColonizeSystemNotification colonization) {
                if (deferColonization)
                    session.deferColonizationDecision(colonization.systemId(),
                            colonization.fleet(), colonization.design());
                else
                    colonizationAdapter.present(session, colonization.systemId(),
                            colonization.fleet(), colonization.design());
            }
            else if (entry.notification() instanceof SystemsScoutedNotification) {
                int empireId = entry.recipientEmpireId();
                String playerId = session.controllerRegistry().seatForEmpire(empireId).playerId();
                scoutingProviders.get(playerId).present(empireId,
                        session.scoutedSystemIdsForEmpire(empireId));
            }
            else if (entry.notification() instanceof ShipConstructionNotification) {
                int empireId = entry.recipientEmpireId();
                String playerId = session.controllerRegistry().seatForEmpire(empireId).playerId();
                shipConstructionProviders.get(playerId).present(empireId,
                        Map.copyOf(session.shipConstructionCountsForEmpire(empireId)));
            }
            else if (entry.notification() instanceof DiplomaticNotification diplomacy)
                deliverDiplomacy(session, entry.recipientEmpireId(), diplomacy);
            else if (entry.notification() instanceof BombardmentResultNotification bombardment) {
                String playerId = session.controllerRegistry()
                        .seatForEmpire(entry.recipientEmpireId()).playerId();
                bombardmentProviders.get(playerId).present(bombardment.notice());
            }
            else if (entry.notification() instanceof SabotageResultNotification sabotage) {
                String playerId = session.controllerRegistry()
                        .seatForEmpire(entry.recipientEmpireId()).playerId();
                sabotageProviders.get(playerId).present(sabotage.notice());
            }
            else {
                int empireId = entry.recipientEmpireId();
                String playerId = session.controllerRegistry().seatForEmpire(empireId).playerId();
                technologyProviders.get(playerId).present(
                        technologyNotice(entry.notification(), empireId));
            }
        }
    }

    private TechnologyNotice technologyNotice(TurnNotification notification, Integer recipientEmpireId) {
        if (recipientEmpireId == null)
            return null;
        if (notification instanceof DiscoverTechNotification discovered)
            return new TechnologyNotice(TechnologyNotice.Kind.DISCOVERED,
                    recipientEmpireId, discovered.techId(), null, null);
        if (notification instanceof PlunderTechNotification plunder)
            return new TechnologyNotice(TechnologyNotice.Kind.PLANET_PLUNDER,
                    recipientEmpireId, plunder.techId(), plunder.systemId(),
                    plunder.sourceEmpireId() < 0 ? null : plunder.sourceEmpireId());
        if (notification instanceof PlunderShipTechNotification plunder)
            return new TechnologyNotice(TechnologyNotice.Kind.SHIP_PLUNDER,
                    recipientEmpireId, plunder.techId(), null, plunder.sourceEmpireId());
        if (notification instanceof TradeTechNotification trade)
            return new TechnologyNotice(TechnologyNotice.Kind.TRADED,
                    recipientEmpireId, trade.techId, null, trade.empId);
        if (notification instanceof StealTechNotification stolen
                && stolen.stolenTechId() != null)
            return new TechnologyNotice(TechnologyNotice.Kind.STOLEN,
                    recipientEmpireId, stolen.stolenTechId(), stolen.systemId(),
                    stolen.sourceEmpireId());
        return null;
    }

    private void deliverDiplomacy(GameSession session, int recipientEmpireId,
            DiplomaticNotification notification) {
        String type = notification.type();
        boolean offer = DiplomacyDecisionActions.isOffer(type);
        int tradeAmount = type.equals(DialogueManager.OFFER_TRADE)
                ? notification.view().trade().maxLevel() : 0;
        String playerId = session.controllerRegistry().seatForEmpire(recipientEmpireId).playerId();
        DiplomacyNotice notice = new DiplomacyNotice(playerId, recipientEmpireId,
                notification.talker().id, notification.otherEmpire() == null ? null
                        : notification.otherEmpire().id, type,
                type.equals(DialogueManager.OFFER_TRADE) ? tradeAmount : null, offer,
                notification.incident());
        if (offer && deferDiplomacy) {
            session.deferDiplomacyDecision(notice);
            return;
        }
        boolean accept = diplomacyProviders.get(playerId).present(notice);
        if (offer)
            DiplomacyDecisionActions.apply(session, notice, accept);
    }
}
