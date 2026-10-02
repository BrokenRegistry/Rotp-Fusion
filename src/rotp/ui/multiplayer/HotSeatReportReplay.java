package rotp.ui.multiplayer;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import javax.swing.SwingUtilities;
import rotp.model.empires.Empire;
import rotp.model.game.GameSession;
import rotp.multiplayer.hotseat.HotSeatReport;
import rotp.multiplayer.turn.BombardmentNotice;
import rotp.multiplayer.turn.DiplomacyNotice;
import rotp.multiplayer.turn.SabotageNotice;
import rotp.multiplayer.turn.TechnologyNotice;
import rotp.ui.RotPUI;
import rotp.ui.notifications.DiplomaticNotification;

/**
 * Plays a seated player's private reports on the game's own notification
 * screens, the way single player shows them at the end of a turn. Each
 * screen pauses this thread until the player clicks through it.
 */
public final class HotSeatReportReplay {
    private HotSeatReportReplay() { }

    /** True when the game has its own screen for this report. */
    public static boolean replayable(HotSeatReport report) {
        Object p = report.payload();
        return p instanceof TechnologyNotice || p instanceof BombardmentNotice
                || p instanceof SabotageNotice || p instanceof Map
                || p instanceof DiplomacyNotice notice && !notice.responseRequired();
    }

    /** Runs off the event thread; {@code done} runs on the event thread afterwards if still active. */
    public static void play(GameSession game, List<HotSeatReport> reports, BooleanSupplier active, Runnable done) {
        if (!SwingUtilities.isEventDispatchThread()) throw new IllegalStateException("Event thread required");
        // Set before the reveal is painted, so planning is never briefly unlocked.
        GameSession.performingTurn(true);
        Thread thread = new Thread(() -> {
            try {
                for (HotSeatReport report : reports) {
                    if (game != GameSession.instance() || !active.getAsBoolean()) break;
                    try { show(game, report); }
                    catch (Throwable failure) { failure.printStackTrace(); }
                    finally { game.resumeNextTurnProcessing(); }
                }
            } finally {
                // Planning unlocks only once the controller has taken back over.
                SwingUtilities.invokeLater(() -> {
                    if (game != GameSession.instance()) return;
                    GameSession.performingTurn(false);
                    if (!active.getAsBoolean()) return;
                    game.hotSeatScoutedReport(null);
                    RotPUI.instance().selectMainPanel();
                    done.run();
                });
            }
        }, "hot-seat-reports");
        thread.setDaemon(true);
        thread.start();
    }

    @SuppressWarnings("unchecked")
    private static void show(GameSession game, HotSeatReport report) throws Exception {
        RotPUI ui = RotPUI.instance();
        Object payload = report.payload();
        if (payload instanceof TechnologyNotice n) {
            int source = n.sourceEmpireId() == null ? Empire.NULL_ID : n.sourceEmpireId();
            switch (n.kind()) {
                case DISCOVERED -> ui.selectDiscoverTechPanel(n.techId());
                case PLANET_PLUNDER -> ui.selectPlunderTechPanel(n.techId(), n.systemId(), source);
                case SHIP_PLUNDER -> ui.selectPlunderShipTechPanel(n.techId(), source);
                case TRADED -> ui.selectTradeTechPanel(n.techId(), source);
                case STOLEN -> ui.selectStolenTechReport(n.techId(), n.systemId(), source);
            }
        }
        else if (payload instanceof BombardmentNotice n)
            ui.showBombardmentResult(n.systemId(), game.galaxy().empire(n.attackerEmpireId()),
                    n.populationBefore(), n.populationAfter(), n.basesBefore(), n.basesAfter(),
                    n.factoriesBefore(), n.factoriesAfter());
        else if (payload instanceof SabotageNotice n)
            ui.selectSabotageResultReport(game.galaxy().empire(n.victimEmpireId()), n.systemId(),
                    n.action(), n.amount());
        else if (payload instanceof DiplomacyNotice n) {
            Empire talker = game.galaxy().empire(n.talkerEmpireId());
            Empire other = n.targetEmpireId() == null ? null : game.galaxy().empire(n.targetEmpireId());
            ui.selectDiplomaticMessagePanel(DiplomaticNotification.report(
                    talker.viewForEmpire(n.recipientEmpireId()), n.messageType(), other, n.incident()));
        }
        else if ("SCOUT".equals(report.kind())) {
            onEdt(() -> game.hotSeatScoutedReport((Map<String, List<Integer>>) payload));
            ui.showSystemsScouted();
        }
        else if ("SHIPS".equals(report.kind())) {
            onEdt(() -> game.hotSeatShipsReport((Map<Integer, Integer>) payload));
            ui.showShipConstruction();
        }
    }

    private static void onEdt(Runnable action) throws Exception {
        SwingUtilities.invokeAndWait(action);
    }
}
