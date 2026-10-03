/*
 * Copyright 2015-2020 Ray Fowler
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.model.game;

import static rotp.model.game.IBaseOptsTools.GAME_OPTIONS_FILE;
import static rotp.model.game.IDebugOptions.AUTORUN_LOGFILE;
import static rotp.model.game.IDebugOptions.MEMORY_LOGFILE;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;

import rotp.Rotp;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.empires.EspionageMission;
import rotp.model.empires.GalacticCouncil;
import rotp.model.empires.Leader;
import rotp.model.empires.SabotageMission;
import rotp.model.empires.Spy;
import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.GalaxyFactory;
import rotp.model.galaxy.GalaxyFactory.GalaxyCopy;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.Ships;
import rotp.model.galaxy.StarSystem;
import rotp.model.galaxy.Transport;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipManeuver;
import rotp.model.ships.ShipSpecial;
import rotp.model.ships.ShipWeapon;
import rotp.model.tech.Tech;
import rotp.model.tech.TechCategory;
import rotp.model.tech.TechTree;
import rotp.multiplayer.session.ControllerRegistry;
import rotp.multiplayer.turn.CouncilDecisionAdapter;
import rotp.multiplayer.turn.ResearchDecisionAdapter;
import rotp.multiplayer.turn.ColonizationDecisionAdapter;
import rotp.multiplayer.turn.EspionageDecisionAdapter;
import rotp.multiplayer.turn.BombardmentDecisionAdapter;
import rotp.multiplayer.turn.InProcessBombardmentDecisionAdapter;
import rotp.multiplayer.turn.SabotageDecisionAdapter;
import rotp.multiplayer.turn.TurnNotificationSink;
import rotp.multiplayer.turn.QueuedTurnNotification;
import rotp.multiplayer.turn.QueuedGameAlert;
import rotp.multiplayer.turn.AlertRecord;
import rotp.multiplayer.turn.RecordedGameAlert;
import rotp.multiplayer.turn.TurnCheckpoint;
import rotp.multiplayer.turn.PendingDecision;
import rotp.multiplayer.turn.PendingResearchDecision;
import rotp.multiplayer.turn.PendingColonizationDecision;
import rotp.multiplayer.turn.PendingDiplomacyDecision;
import rotp.multiplayer.turn.DiplomacyNotice;
import rotp.multiplayer.turn.DiplomacyDecisionActions;
import rotp.multiplayer.turn.InProcessColonizationDecisionAdapter;
import rotp.multiplayer.turn.DecisionRouter;
import rotp.multiplayer.turn.DeferredCouncilDecisionAdapter;
import rotp.multiplayer.turn.ResearchDecisionRouter;
import rotp.multiplayer.turn.StrictInProcessNotificationSink;
import rotp.multiplayer.session.PlayerSeat;
import rotp.multiplayer.session.MatchOutcome;
import rotp.multiplayer.session.MatchOutcomeEvaluator;
import rotp.multiplayer.turn.TurnCoordinator;
import rotp.multiplayer.turn.TurnCoordinator.Phase;
import rotp.ui.ErrorUI;
import rotp.ui.DesktopCouncilDecisionAdapter;
import rotp.ui.DesktopColonizationDecisionAdapter;
import rotp.ui.DesktopResearchDecisionAdapter;
import rotp.ui.DesktopEspionageDecisionAdapter;
import rotp.ui.DesktopTurnNotificationSink;
import rotp.ui.NoticeMessage;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.game.AdvisorPanel;
import rotp.ui.game.GameOverUI;
import rotp.ui.game.GameUI;
import rotp.ui.game.LoadGameUI;
import rotp.ui.main.EmpireColonySpendingPane;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.notifications.DiplomaticNotification;
import rotp.ui.notifications.GNNExpansionEvent;
import rotp.ui.notifications.GNNRankingNoticeCheck;
import rotp.ui.notifications.GameAlert;
import rotp.ui.notifications.SabotageNotification;
import rotp.ui.notifications.SelectTechNotification;
import rotp.ui.notifications.ShipConstructionNotification;
import rotp.ui.notifications.SpyReportAlert;
import rotp.ui.notifications.StealTechNotification;
import rotp.ui.notifications.SystemsScoutedNotification;
import rotp.ui.notifications.TradeTechNotification;
import rotp.ui.notifications.TurnNotification;
import rotp.ui.planets.MultiColonySpendingPane;
import rotp.ui.races.RacesUI;
import rotp.ui.sprites.FlightPathSprite;
import rotp.ui.vipconsole.VIPConsole;
import rotp.util.Base;
import rotp.util.LabelManager;
import rotp.util.MoveToTrash;
import rotp.util.Rand;

public final class GameSession implements Base, Serializable {
    private static final long serialVersionUID = 1L;
    public static final int CURRENT_SAVE_VERSION = 1;
    public static final String SAVEFILE_DIRECTORY = ".";
    public static final String BACKUP_DIRECTORY   = "backup";
    public static final String SAVEFILE_EXTENSION = ".rotp";
    public static final String RECENT_SAVEFILE    = "recent"+SAVEFILE_EXTENSION;
    public static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final Object ONE_GAME_AT_A_TIME = new Object();
    private static GameSession instance = new GameSession();
	private static void instance(GameSession newInstance) { instance = newInstance; }
    public static GameSession instance()  { return instance; }

	private static final boolean showInfo = false; // BR: for debug
    private static final int MINIMUM_NEXT_TURN_TIME = 500;
    private static Thread nextTurnThread;
    private static volatile boolean suspendNextTurn = false;
    private static final ThreadFactory minThreadFactory = GameSession.minThreadFactory();
    private static ExecutorService smallSphereService = Executors.newSingleThreadExecutor(minThreadFactory);

    private static HashMap<String,Object> vars;
    private static boolean performingTurn;
    private transient volatile boolean turnFailed;
    private HashMap<Integer, HashMap<StarSystem, List<String>>> systemsToAllocateByEmpire;
    private HashMap<Integer, HashMap<String, List<StarSystem>>> systemsScoutedByEmpire;
    private HashMap<Integer, HashMap<ShipDesign, Integer>> shipsConstructedByEmpire;
    private transient List<QueuedGameAlert> alerts = new ArrayList<>();
    private List<AlertRecord> alertRecords = new ArrayList<>();
    private transient HashMap<Integer, Integer> viewedAlertsByEmpire = new HashMap<>();
    private static boolean ironmanLocked = false;
    private static boolean autoRunning = false;

    private IGameOptions options;
    private Galaxy galaxy;
    private ControllerRegistry controllerRegistry;
    private rotp.multiplayer.hotseat.HotSeatState hotSeatState;
    private rotp.multiplayer.hotseat.HotSeatSetup hotSeatSetup;
    private rotp.multiplayer.hotseat.HotSeatInbox hotSeatInbox;
    private transient rotp.multiplayer.hotseat.HotSeatController hotSeatController;
    private rotp.multiplayer.hotseat.HotSeatPolicies hotSeatPolicies;
    private rotp.multiplayer.hotseat.HotSeatSaveEnvelope hotSeatSaveEnvelope;
    private rotp.multiplayer.pbem.PlayByEmail playByEmail;
    public rotp.multiplayer.pbem.PlayByEmail playByEmail() { return playByEmail; }
    public static boolean isPlayByEmail() { return instance != null && instance.playByEmail != null; }
    public static boolean hotSeatPolicyLocked(String key) {
        return instance != null && instance.hotSeatPolicies != null && instance.hotSeatPolicies.locks(key);
    }
    public void restoreHotSeatPolicies() { if (hotSeatPolicies != null) hotSeatPolicies.restore(); }
    public void captureHotSeatView() {
        if (hotSeatController != null) hotSeatController.captureView();
    }
    public boolean hotSeatModelSaveable() {
        return !performingTurn && galaxy != null && turnCoordinator().activePhase() == null
                && inProcessPendingPostPhase == null && notifications().isEmpty() && alerts().isEmpty();
    }
    public long writeHotSeatEnvelope(File destination) throws Exception {
        if (!rotp.multiplayer.hotseat.HotSeatPersistence.canSave(this))
            throw new IllegalStateException("Finish this choice before saving.");
        var oldEnvelope = hotSeatSaveEnvelope;
        var oldRandom = inProcessCheckpointRandom;
        var oldBarrier = inProcessSavedBarrier;
        var snapshot = hotSeatState.snapshot();
        var kind = snapshot.pendingDecisionId() != null
                ? rotp.multiplayer.hotseat.HotSeatSaveEnvelope.Kind.DECISION
                : snapshot.stage() == rotp.multiplayer.hotseat.HotSeatState.Stage.RESOLVING
                    ? rotp.multiplayer.hotseat.HotSeatSaveEnvelope.Kind.RESOLUTION_BOUNDARY
                    : snapshot.stage() == rotp.multiplayer.hotseat.HotSeatState.Stage.FINISHED
                        ? rotp.multiplayer.hotseat.HotSeatSaveEnvelope.Kind.FINISHED
                        : rotp.multiplayer.hotseat.HotSeatSaveEnvelope.Kind.PLANNING;
        hotSeatSaveEnvelope = new rotp.multiplayer.hotseat.HotSeatSaveEnvelope(1, kind, snapshot);
        inProcessCheckpointRandom = Rotp.rand();
        inProcessSavedBarrier = turnCheckpoint();
        try { return writeSessionAtomically(destination); }
        catch (Exception | Error failure) {
            hotSeatSaveEnvelope = oldEnvelope;
            inProcessCheckpointRandom = oldRandom;
            inProcessSavedBarrier = oldBarrier;
            throw failure;
        }
    }
    public static GameSession restoreHotSeatEnvelope(File source) throws Exception {
        if (!javax.swing.SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("Load hot-seat games on the event thread");
        rotp.multiplayer.pbem.BuildStamp.requireMatch(source);
        GameSession restored;
        try (ZipFile zip = new ZipFile(source)) {
            ZipEntry entry = zip.getEntry("GameSession.dat");
            if (entry == null) throw new IOException("Missing hot-seat session");
            restored = loadObjectData(zip.getInputStream(entry));
        }
        if (restored == null || restored.hotSeatSetup == null || restored.hotSeatState == null
                || restored.hotSeatInbox == null || restored.hotSeatSaveEnvelope == null
                || restored.hotSeatSaveEnvelope.version() != 1 || restored.inProcessCheckpointRandom == null
                || !restored.hotSeatSaveEnvelope.snapshot().equals(restored.hotSeatState.snapshot())
                || !restored.hotSeatState.snapshot().recoverable())
            throw new IOException("Unsupported hot-seat save");
        GameSession previous = instance();
        if (previous.hotSeatController != null && !previous.hotSeatController.safeToReplace())
            throw new IllegalStateException("Finish the current turn operation before loading");
        Rand previousRandom = Rotp.rand();
        int previousPlayer = Empire.PLAYER_ID;
        try {
            instance(restored);
            restored.options().setAsGame();
            restored.restoreHotSeatPolicies();
            Rotp.rand(copyCheckpointRandom(restored.inProcessCheckpointRandom));
            restored.galaxy.validateOnLoad(false);
            // Transient maintenance caches deserialize as zero, which is a valid
            // cached cost. Invalidate them before exposing planning previews.
            for (Empire empire : restored.galaxy.empires())
                empire.recalcPlanetaryProduction();
            Rotp.rand(restored.inProcessCheckpointRandom);
            if (!restored.turnCheckpoint().equals(restored.inProcessSavedBarrier))
                throw new IOException("Hot-seat model does not match its save boundary");
            if (previous.hotSeatController != null) previous.hotSeatController.close();
            performingTurn = false;
            autoRunning = false;
            clearViewingState();
            restored.startExecutors();
            restored.hotSeatState.coverForLoad();
            restored.openHotSeatDesktop(true);
            return restored;
        } catch (Exception | Error failure) {
            instance(previous);
            Rotp.rand(previousRandom);
            Empire.updatePlayerId(previousPlayer);
            if (previous.options() != null) previous.options().setAsGame();
            previous.restoreHotSeatPolicies();
            throw failure;
        }
    }
    private Map<Integer, rotp.multiplayer.hotseat.HotSeatViewState> hotSeatViews;
    private TurnCoordinator turnCoordinator;
    private Phase inProcessPendingPostPhase;
    private long colonizationDecisionSequence;
    private List<ColonizationChoice> pendingColonizationChoices = new ArrayList<>();
    private long diplomacyDecisionSequence;
    private List<PendingDiplomacyDecision> pendingDiplomacyChoices = new ArrayList<>();
    private Rand inProcessCheckpointRandom;
    private TurnCheckpoint inProcessSavedBarrier;
    private static final class ColonizationChoice implements Serializable {
        private static final long serialVersionUID = 1L;
        private final PendingColonizationDecision decision;
        private final ShipFleet fleet;
        private final ShipDesign design;
        private ColonizationChoice(PendingColonizationDecision decision,
                ShipFleet fleet, ShipDesign design) {
            this.decision = decision;
            this.fleet = fleet;
            this.design = design;
        }
    }
    private transient CouncilDecisionAdapter councilDecisionAdapter;
    private transient ResearchDecisionAdapter researchDecisionAdapter;
    private transient ColonizationDecisionAdapter colonizationDecisionAdapter;
    private transient EspionageDecisionAdapter espionageDecisionAdapter;
    private transient BombardmentDecisionAdapter bombardmentDecisionAdapter;
    private transient SabotageDecisionAdapter sabotageDecisionAdapter;
    private transient TurnNotificationSink turnNotificationSink;
    private transient List<QueuedTurnNotification> notifications = new ArrayList<>();
    private final GameStatus status = new GameStatus();
    private long id;
    private Long achievementId;
    private boolean spyActivity = false;
    private HashSet<Integer> spyActivityByEmpire;
    private Integer lastTurnAlive;
    private boolean aFewMoreTurns = false;
	private boolean lastAlwaysAtWar = false;
	private boolean lastAlwaysAlly  = false;
	private transient boolean loading = false;

	public boolean loading()					{ return loading; }
	boolean isReady()							{ return galaxy()!=null && !loading(); }
    public GameStatus status()                   { return status; }
    public long id()                             { return id; }
    public long achievementId()                  {
    	if (achievementId == null)
    		achievementId = newAchievementId();
    	return achievementId;
    }
	private static long newAchievementId()				{ return System.currentTimeMillis(); }
	public static ExecutorService smallSphereService()	{ return smallSphereService; }

    public static boolean ironmanLocked() 		 { return ironmanLocked; }
    public static boolean isSuspended() 		 { return suspendNextTurn; }
    // BR: to save the beginning of the turn
    public static final String recentStartSaveFile() {
    	return LabelManager.current().label("LOAD_GAME_RECENT_START_SAVEFILE") + SAVEFILE_EXTENSION;
    }
	public static boolean isGameMode()	{ return instance().isReady() && RulesetManager.current().isGameMode(); }

    public void pauseNextTurnProcessing(String s)   {
        if (performingTurn) {
            log("Pausing Next Turn: ", s);
            suspendNextTurn = true;
        }
    }
    public void resumeNextTurnProcessing()  {
        log("Resuming Next Turn");
        suspendNextTurn = false;
    }
	public static HashMap<ShipDesign, Integer> shipsConstructed()	{
        return instance().shipConstructionForEmpire(Empire.PLAYER_ID);
    }
    private HashMap<ShipDesign, Integer> shipConstructionForEmpire(int empireId) {
        if (shipsConstructedByEmpire == null)
            shipsConstructedByEmpire = new HashMap<>();
        return shipsConstructedByEmpire.computeIfAbsent(empireId, ignored -> new HashMap<>());
    }
    public HashMap<Integer, Integer> shipConstructionCountsForEmpire(int empireId) {
        HashMap<Integer, Integer> counts = new HashMap<>();
        for (var entry : shipConstructionForEmpire(empireId).entrySet())
            counts.put(entry.getKey().id(), entry.getValue());
        return counts;
    }
    private void clearShipsConstructed() {
        if (shipsConstructedByEmpire == null)
            return;
        for (var counts : shipsConstructedByEmpire.values())
            counts.clear();
        shipsConstructedByEmpire.clear();
    }
	public static HashMap<StarSystem, List<String>> systemsToAllocate()	{
        return instance().allocationRequestsForEmpire(Empire.PLAYER_ID);
    }
    private HashMap<Integer, HashMap<StarSystem, List<String>>> allocationRequests() {
        if (systemsToAllocateByEmpire == null)
            systemsToAllocateByEmpire = new HashMap<>();
        return systemsToAllocateByEmpire;
    }
    private HashMap<StarSystem, List<String>> allocationRequestsForEmpire(int empireId) {
        return allocationRequests().computeIfAbsent(empireId, ignored -> new HashMap<>());
    }
    public HashMap<Integer, List<String>> allocationReasonsForEmpire(int empireId) {
        HashMap<Integer, List<String>> reasons = new HashMap<>();
        for (var entry : allocationRequestsForEmpire(empireId).entrySet())
            reasons.put(entry.getKey().id, List.copyOf(entry.getValue()));
        return reasons;
    }
    private void clearAllocationRequests() {
        for (HashMap<StarSystem, List<String>> requests : allocationRequests().values())
            requests.clear();
        allocationRequests().clear();
    }
	public static HashMap<String, List<StarSystem>> systemsScouted()	{
        return instance().scoutedSystemsForEmpire(Empire.PLAYER_ID);
    }
    private HashMap<String, List<StarSystem>> scoutedSystemsForEmpire(int empireId) {
        if (systemsScoutedByEmpire == null)
            systemsScoutedByEmpire = new HashMap<>();
        return systemsScoutedByEmpire.computeIfAbsent(empireId, ignored -> {
            HashMap<String, List<StarSystem>> systems = new HashMap<>();
            systems.put("Scouts", new ArrayList<>());
            systems.put("Allies", new ArrayList<>());
            systems.put("Astronomers", new ArrayList<>());
            return systems;
        });
    }
    public HashMap<String, List<Integer>> scoutedSystemIdsForEmpire(int empireId) {
        HashMap<String, List<Integer>> ids = new HashMap<>();
        for (var entry : scoutedSystemsForEmpire(empireId).entrySet()) {
            List<Integer> systemIds = new ArrayList<>();
            for (StarSystem system : entry.getValue())
                systemIds.add(system.id);
            ids.put(entry.getKey(), List.copyOf(systemIds));
        }
        return ids;
    }
	private List<QueuedTurnNotification> notifications() {
        if (notifications == null)
            notifications = new ArrayList<>();
        return notifications;
    }
	private static HashMap<String,Object> vars() {
        if (vars == null)
            vars = new HashMap<>();
        return vars;
    }
	private List<QueuedGameAlert> alerts() {
        if (alerts == null)
            alerts = new ArrayList<>();
        return alerts;
    }
    private List<AlertRecord> alertRecords() {
        if (alertRecords == null)
            alertRecords = new ArrayList<>();
        return alertRecords;
    }
    public List<AlertRecord> alertRecordsForEmpire(int empireId) {
        List<AlertRecord> result = new ArrayList<>();
        for (AlertRecord record : alertRecords())
            if (record.recipientEmpireId() == empireId)
                result.add(record);
        return List.copyOf(result);
    }
    private HashMap<Integer, Integer> viewedAlertsByEmpire() {
        if (viewedAlertsByEmpire == null)
            viewedAlertsByEmpire = new HashMap<>();
        return viewedAlertsByEmpire;
    }
    public List<GameAlert> alertsForEmpire(int empireId) {
        List<GameAlert> result = new ArrayList<>();
        if (controllerRegistry != null) {
            for (AlertRecord record : alertRecordsForEmpire(empireId))
                result.add(new RecordedGameAlert(record));
            return List.copyOf(result);
        }
        for (QueuedGameAlert entry : alerts()) {
            if (controllerRegistry == null || Integer.valueOf(empireId).equals(entry.recipientEmpireId()))
                result.add(entry.alert());
        }
        return List.copyOf(result);
    }
	public static GameAlert currentAlert()	{
        GameSession session = instance();
        List<GameAlert> localAlerts = session.alertsForEmpire(Empire.PLAYER_ID);
        int viewed = viewedAlerts();
        if (viewed >= localAlerts.size())
            return null;
        return localAlerts.get(viewed);
    }
	public static int viewedAlerts()	{
        return instance().viewedAlertsByEmpire().getOrDefault(Empire.PLAYER_ID, 0);
    }
	public static int numAlerts()		{ return instance().alertsForEmpire(Empire.PLAYER_ID).size(); }
	public static void addAlert(GameAlert a)	{
        GameSession session = instance();
        if (session.controllerRegistry != null)
            throw new IllegalStateException("A rostered match requires an alert recipient");
        session.alerts().add(new QueuedGameAlert(a, null));
    }
    public static void addAlertForEmpire(int empireId, GameAlert alert) {
        GameSession session = instance();
        if (session.controllerRegistry == null) {
            session.alerts().add(new QueuedGameAlert(alert, empireId));
            return;
        }
        PlayerSeat seat = session.controllerRegistry.seatForEmpire(empireId);
        if (session.galaxy == null || session.galaxy.empire(empireId) == null)
            throw new IllegalArgumentException("Alert has no empire: " + empireId);
        if (seat == null || seat.controllerType() == PlayerSeat.ControllerType.AI)
            return;
        if (session.hotSeatInbox != null) {
            session.hotSeatInbox.append(session.galaxy.currentTurn(), empireId, "ALERT",
                    new rotp.util.Base() { }.text("HOTSEAT_ALERT"), List.of(alert.descriptionForEmpire(empireId)));
            return;
        }
        session.alertRecords().add(new AlertRecord(empireId,
                alert.getClass().getSimpleName(), alert.descriptionForEmpire(empireId),
                alert.sysId()));
    }
	private static void clearAlerts()	{
        instance().alerts().clear();
        instance().alertRecords().clear();
        instance().viewedAlertsByEmpire().clear();
    }
	public static void dismissAlert()	{
        GameSession session = instance();
        session.viewedAlertsByEmpire().merge(Empire.PLAYER_ID, 1, Integer::sum);
    }

    public void aFewMoreTurns(boolean b) { aFewMoreTurns = b; }
    public boolean aFewMoreTurns() 		 { return aFewMoreTurns; }
	public static boolean performingTurn()			{ return performingTurn; }
	public static void performingTurn(boolean b)	{ performingTurn = b; }
    @Override
    public IGameOptions options()        { return options; }
    public void options(IGameOptions o)  { options = o; o.setAsGame(); }
    @Override
    public Galaxy galaxy()               { return galaxy; }
    public ControllerRegistry controllerRegistry() { return controllerRegistry; }
    public rotp.multiplayer.hotseat.HotSeatState hotSeatState() { return hotSeatState; }
    public rotp.multiplayer.hotseat.HotSeatSetup hotSeatSetup() { return hotSeatSetup; }
    public rotp.multiplayer.hotseat.HotSeatInbox hotSeatInbox() { return hotSeatInbox; }
    public rotp.multiplayer.hotseat.HotSeatController hotSeatController() { return hotSeatController; }
    public void openHotSeatDesktop(boolean resume) {
        if (!javax.swing.SwingUtilities.isEventDispatchThread())
            throw new IllegalStateException("Event thread required");
        if (hotSeatController != null) hotSeatController.close();
        var desktop = new rotp.ui.multiplayer.HotSeatDesktop();
        hotSeatController = new rotp.multiplayer.hotseat.HotSeatController(this, desktop,
                new rotp.ui.multiplayer.HotSeatDecisionPanels(this, desktop));
        if (resume) hotSeatController.resume();
        else hotSeatController.start();
    }
    public rotp.multiplayer.hotseat.HotSeatViewState hotSeatView(int empireId) {
        return hotSeatViews == null ? null : hotSeatViews.get(empireId);
    }
    public void hotSeatView(int empireId, rotp.multiplayer.hotseat.HotSeatViewState view) {
        if (hotSeatViews == null) hotSeatViews = new HashMap<>();
        hotSeatViews.put(empireId, view);
    }
    public void controllerRegistry(ControllerRegistry registry) { controllerRegistry = registry; }
    public TurnCoordinator turnCoordinator() {
        if (turnCoordinator == null)
            turnCoordinator = new TurnCoordinator();
        return turnCoordinator;
    }
    public TurnCheckpoint turnCheckpoint() {
        TurnCoordinator coordinator = turnCoordinator();
        if (!coordinator.atSafeBoundary() && !hotSeatStartupBoundary())
            throw new IllegalStateException("A turn checkpoint requires a completed phase boundary");
        if (inProcessPendingPostPhase != null)
            throw new IllegalStateException("Phase delivery is still pending: "
                    + inProcessPendingPostPhase);
        if (controllerRegistry != null && !notifications().isEmpty())
            throw new IllegalStateException("Turn notifications are still pending delivery");
        PendingDecision councilVote = new DecisionRouter(this).pendingCouncilVote();
        PendingDecision councilRuling = new DecisionRouter(this).pendingCouncilRuling();
        List<PendingResearchDecision> researchChoices = new ArrayList<>();
        if (controllerRegistry != null && galaxy != null) {
            ResearchDecisionRouter router = new ResearchDecisionRouter(this);
            for (PlayerSeat seat : controllerRegistry.seats()) {
                if (seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
                    continue;
                for (int category = 0; category < TechTree.NUM_CATEGORIES; category++) {
                    PendingResearchDecision decision = router.pending(seat.empireId(), category);
                    if (decision != null)
                        researchChoices.add(decision);
                }
            }
        }
        return new TurnCheckpoint(coordinator.turn() < 0 ? galaxy.currentTurn() : coordinator.turn(), coordinator.lastCompletedPhase(),
                councilVote, councilRuling, researchChoices, pendingColonizationDecisions(),
                pendingDiplomacyDecisions());
    }
    public List<PendingDiplomacyDecision> pendingDiplomacyDecisions() {
        return pendingDiplomacyChoices == null ? List.of() : List.copyOf(pendingDiplomacyChoices);
    }
    public void deferDiplomacyDecision(DiplomacyNotice notice) {
        if (controllerRegistry == null || !notice.responseRequired()
                || !DiplomacyDecisionActions.isOffer(notice.messageType()))
            throw new IllegalStateException("No diplomatic offer to defer");
        PlayerSeat seat = controllerRegistry.seatForEmpire(notice.recipientEmpireId());
        if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN
                || !seat.playerId().equals(notice.ownerPlayerId()))
            throw new IllegalStateException("Diplomatic offer has no matching human owner");
        if (pendingDiplomacyChoices == null)
            pendingDiplomacyChoices = new ArrayList<>();
        String decisionId = "diplomacy:" + id + ":" + (++diplomacyDecisionSequence);
        pendingDiplomacyChoices.add(new PendingDiplomacyDecision(decisionId, notice));
    }
    public boolean answerDiplomacyDecision(String playerId, String decisionId, boolean accept) {
        if (pendingDiplomacyChoices == null || performingTurn
                || !turnCoordinator().atSafeBoundary() || inProcessPendingPostPhase != null)
            return false;
        for (PendingDiplomacyDecision decision : new ArrayList<>(pendingDiplomacyChoices)) {
            if (!decision.id().equals(decisionId))
                continue;
            if (!decision.notice().ownerPlayerId().equals(playerId))
                return false;
            boolean applied = DiplomacyDecisionActions.apply(this, decision.notice(), accept);
            pendingDiplomacyChoices.remove(decision);
            if (hotSeatInbox != null) {
                var notice = decision.notice();
                String description = rotp.ui.multiplayer.HotSeatDecisionPanels.diplomacyDescription(this, notice);
                for (int recipient : new int[] {notice.recipientEmpireId(), notice.talkerEmpireId()})
                    if (controllerRegistry.isHumanControlled(recipient))
                        hotSeatInbox.append(galaxy.currentTurn(), recipient, "DIPLOMACY",
                                text("HOTSEAT_DIPLOMACY"), List.of(description,
                                        text(!applied ? "HOTSEAT_REPLY_INVALID"
                                                : accept ? "HOTSEAT_REPLY_ACCEPT" : "HOTSEAT_REPLY_REJECT")));
            }
            return true;
        }
        return false;
    }
    public List<PendingColonizationDecision> pendingColonizationDecisions() {
        if (pendingColonizationChoices == null)
            return List.of();
        return pendingColonizationChoices.stream().map(choice -> choice.decision).toList();
    }
    public void deferColonizationDecision(int systemId, ShipFleet fleet, ShipDesign design) {
        if (controllerRegistry == null)
            throw new IllegalStateException("Colonization requires a controller roster");
        if (!InProcessColonizationDecisionAdapter.eligible(galaxy.system(systemId), fleet, design))
            return;
        PlayerSeat seat = controllerRegistry.seatForEmpire(fleet.empId());
        if (seat == null || seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
            throw new IllegalStateException("Colony ship has no owning human seat");
        String decisionId = "colonize:" + id + ":" + (++colonizationDecisionSequence);
        PendingColonizationDecision decision = new PendingColonizationDecision(decisionId,
                seat.playerId(), fleet.empId(), systemId, design.id());
        if (pendingColonizationChoices == null)
            pendingColonizationChoices = new ArrayList<>();
        pendingColonizationChoices.add(new ColonizationChoice(decision, fleet, design));
    }
    /** Hot seat: the waiting colony ship, for the game's own colonize prompt. */
    public ShipFleet colonizationFleet(String decisionId) {
        ColonizationChoice choice = colonizationChoice(decisionId);
        return choice == null ? null : choice.fleet;
    }
    public ShipDesign colonizationDesign(String decisionId) {
        ColonizationChoice choice = colonizationChoice(decisionId);
        return choice == null ? null : choice.design;
    }
    private ColonizationChoice colonizationChoice(String decisionId) {
        if (pendingColonizationChoices != null)
            for (ColonizationChoice choice : pendingColonizationChoices)
                if (choice.decision.id().equals(decisionId))
                    return choice;
        return null;
    }
    public boolean answerColonizationDecision(String playerId, String decisionId,
            boolean colonize) {
        if (pendingColonizationChoices == null || performingTurn
                || !turnCoordinator().atSafeBoundary() || inProcessPendingPostPhase != null)
            return false;
        for (ColonizationChoice choice : new ArrayList<>(pendingColonizationChoices)) {
            PendingColonizationDecision decision = choice.decision;
            if (!decision.id().equals(decisionId))
                continue;
            if (!decision.ownerPlayerId().equals(playerId))
                return false;
            if (colonize && colonizationReferenceIsCanonical(choice)
                    && InProcessColonizationDecisionAdapter.eligible(
                    galaxy.system(decision.systemId()), choice.fleet, choice.design))
                choice.fleet.colonizeSystem(galaxy.system(decision.systemId()), choice.design);
            pendingColonizationChoices.remove(choice);
            return true;
        }
        return false;
    }
    private boolean colonizationReferenceIsCanonical(ColonizationChoice choice) {
        PendingColonizationDecision decision = choice.decision;
        Empire empire = galaxy.empire(decision.empireId());
        return empire != null && choice.fleet != null && choice.design != null
                && choice.fleet.empId() == decision.empireId()
                && choice.design.id() == decision.designId()
                && galaxy.ships.allFleets().contains(choice.fleet)
                && empire.shipLab().design(decision.designId()) == choice.design;
    }
    /**
     * Advances a rostered game through complete phase boundaries. Council
     * decisions return to the caller; a later call continues after their reply.
     * Research, colonization, and diplomacy can also use deferred decisions.
     * Unsupported UI notifications fail during delivery.
     */
    public TurnCheckpoint advanceInProcessTurn() {
        return advanceInProcessTurn(false);
    }
    /** Advances through one completed model phase and its notification delivery. */
    public TurnCheckpoint advanceInProcessPhase() {
        return advanceInProcessTurn(true);
    }
    /** Delivers replies' notices at an idle boundary without starting another turn. */
    public TurnCheckpoint deliverInProcessNotifications() {
        if (this != instance() || controllerRegistry == null || galaxy == null
                || performingTurn || (!turnCoordinator().atSafeBoundary() && !hotSeatStartupBoundary())
                || inProcessPendingPostPhase != null
                || !(turnNotificationSink instanceof StrictInProcessNotificationSink))
            throw new IllegalStateException("An idle in-process boundary is required");
        while (!notifications().isEmpty())
            processNotifications();
        return turnCheckpoint();
    }
    private boolean hotSeatStartupBoundary() {
        return hotSeatSetup != null && turnCoordinator().turn() < 0
                && turnCoordinator().activePhase() == null;
    }
    private TurnCheckpoint advanceInProcessTurn(boolean stopAtPhaseBoundary) {
        if (this != instance() || controllerRegistry == null || galaxy == null)
            throw new IllegalStateException("An active rostered session is required");
        if (!(turnNotificationSink instanceof StrictInProcessNotificationSink))
            throw new IllegalStateException("An in-process notification sink is required");
        if (performingTurn)
            throw new IllegalStateException("Another turn is already running");
        councilDecisionAdapter(new DeferredCouncilDecisionAdapter());
        TurnCoordinator coordinator = turnCoordinator();
        if (coordinator.activePhase() != null)
            throw new IllegalStateException("Cannot restart active phase "
                    + coordinator.activePhase());
        if (!inProgress()) {
            if (coordinator.turn() < 0)
                throw new IllegalStateException("The match has finished before a turn started");
            if (inProcessPendingPostPhase != null) {
                finishInProcessPostPhase(inProcessPendingPostPhase);
                inProcessPendingPostPhase = null;
            }
            return deliverInProcessNotifications();
        }
        if (coordinator.turn() >= 0 && coordinator.nextPhase() == null
                && inProcessPendingPostPhase == null) {
            TurnCheckpoint checkpoint = deliverInProcessNotifications();
            if (!checkpoint.researchChoices().isEmpty()
                    || !checkpoint.colonizationChoices().isEmpty()
                    || !checkpoint.diplomacyChoices().isEmpty())
                return checkpoint;
        }
        if (coordinator.turn() < 0
                || (coordinator.nextPhase() == null && inProcessPendingPostPhase == null))
            coordinator.startTurn(galaxy.currentTurn());
        performingTurn = true;
        boolean phaseExecuted = inProcessPendingPostPhase != null;
        try {
            while (inProcessPendingPostPhase != null || !notifications().isEmpty()
                    || (coordinator.nextPhase() != null && inProgress())) {
                if (inProcessPendingPostPhase != null) {
                    finishInProcessPostPhase(inProcessPendingPostPhase);
                    inProcessPendingPostPhase = null;
                    continue;
                }
                if (!notifications().isEmpty()) {
                    processNotifications();
                    continue;
                }
                if (stopAtPhaseBoundary && phaseExecuted)
                    return turnCheckpoint();
                if (new DecisionRouter(this).pendingCouncilVote() != null
                        || new DecisionRouter(this).pendingCouncilRuling() != null) {
                    if (coordinator.lastCompletedPhase() != Phase.COUNCIL)
                        throw new IllegalStateException("Council decision is outside its phase boundary");
                    return turnCheckpoint();
                }
                TurnCheckpoint checkpoint = turnCheckpoint();
                if (!checkpoint.researchChoices().isEmpty()
                        || !checkpoint.colonizationChoices().isEmpty()
                        || !checkpoint.diplomacyChoices().isEmpty())
                    return checkpoint;
                Phase phase = coordinator.nextPhase();
                coordinator.run(phase, () -> runInProcessPhase(phase));
                phaseExecuted = true;
                if (phase == Phase.PREPARE || phase == Phase.COUNCIL
                        || phase == Phase.EMPIRE_TURNS
                        || phase == Phase.SPACE_COMBAT
                        || phase == Phase.INVASIONS || phase == Phase.POST_COLONIZATION
                        || phase == Phase.DIPLOMACY || phase == Phase.DECISIONS
                        || phase == Phase.REFRESH)
                    inProcessPendingPostPhase = phase;
            }
            return turnCheckpoint();
        }
        finally {
            performingTurn = false;
        }
    }
    /** Saves any completed in-process phase boundary without transient messages. */
    public long saveInProcessCheckpoint(File destination) throws Exception {
        return saveInProcessCheckpoint(destination, false);
    }
    /** Saves an idle owned decision barrier with no transient messages. */
    public long saveDecisionBarrier(File destination) throws Exception {
        return saveInProcessCheckpoint(destination, true);
    }
    private long saveInProcessCheckpoint(File destination, boolean decisionOnly)
            throws Exception {
        if (this != instance() || controllerRegistry == null || galaxy == null || performingTurn)
            throw new IllegalStateException("An idle active rostered session is required");
        TurnCheckpoint checkpoint = turnCheckpoint();
        if (decisionOnly && !hasPendingDecision(checkpoint))
            throw new IllegalStateException("No owned decision barrier to save");
        if (!notifications().isEmpty() || !alerts().isEmpty())
            throw new IllegalStateException("Transient turn messages must be delivered before saving");
        if (pendingColonizationChoices != null)
            for (ColonizationChoice choice : pendingColonizationChoices)
                if (!colonizationReferenceIsCanonical(choice))
                    throw new IllegalStateException("Colonization fleet or design is detached from the galaxy");
        Rand previousRandom = inProcessCheckpointRandom;
        TurnCheckpoint previousBarrier = inProcessSavedBarrier;
        inProcessCheckpointRandom = Rotp.rand();
        inProcessSavedBarrier = checkpoint;
        try {
            return saveSession(destination);
        }
        catch (Exception | Error failure) {
            inProcessCheckpointRandom = previousRandom;
            inProcessSavedBarrier = previousBarrier;
            throw failure;
        }
    }
    /** Compatibility entry point restricted to Council decisions. */
    public long saveCouncilBarrier(File destination) throws Exception {
        TurnCheckpoint checkpoint = turnCheckpoint();
        if (!hasPendingCouncilDecision(checkpoint))
            throw new IllegalStateException("No Council decision barrier to save");
        return saveDecisionBarrier(destination);
    }
    private static boolean hasPendingCouncilDecision(TurnCheckpoint checkpoint) {
        return checkpoint.lastCompletedPhase() == Phase.COUNCIL
                && (checkpoint.councilVote() != null || checkpoint.councilRuling() != null);
    }
    private static boolean hasPendingDecision(TurnCheckpoint checkpoint) {
        return hasPendingCouncilDecision(checkpoint)
                || !checkpoint.researchChoices().isEmpty()
                || !checkpoint.colonizationChoices().isEmpty()
                || !checkpoint.diplomacyChoices().isEmpty();
    }
    /** Restores a trusted completed phase checkpoint; providers must be reattached. */
    public static GameSession restoreInProcessCheckpoint(File source) throws Exception {
        return restoreInProcessCheckpoint(source, false, false);
    }
    /** Restores a trusted local decision barrier; providers must be reattached. */
    public static GameSession restoreDecisionBarrier(File source) throws Exception {
        return restoreInProcessCheckpoint(source, true, false);
    }
    /** Compatibility entry point restricted to Council decisions. */
    public static GameSession restoreCouncilBarrier(File source) throws Exception {
        return restoreInProcessCheckpoint(source, true, true);
    }
    private static GameSession restoreInProcessCheckpoint(File source,
            boolean decisionOnly, boolean councilOnly)
            throws Exception {
        GameSession restored;
        try (ZipFile zip = new ZipFile(source)) {
            ZipEntry entry = zip.getEntry("GameSession.dat");
            if (entry == null)
                throw new IOException("In-process checkpoint has no session entry");
            restored = loadObjectData(zip.getInputStream(entry));
        }
        if (restored == null || restored.controllerRegistry == null
                || restored.inProcessCheckpointRandom == null
                || restored.inProcessSavedBarrier == null)
            throw new IOException("Invalid in-process checkpoint");
        GameSession previous = instance();
        Rand previousRandom = Rotp.rand();
        try {
            instance(restored);
            restored.options().setAsGame();
            // Load normalization creates derived objects that can draw randomness.
            // Keep both the saved stream and the previous session's stream intact.
            Rotp.rand(copyCheckpointRandom(restored.inProcessCheckpointRandom));
            restored.galaxy().validateOnLoad(false);
            Rotp.rand(restored.inProcessCheckpointRandom);
            TurnCheckpoint checkpoint = restored.turnCheckpoint();
            if (!checkpoint.equals(restored.inProcessSavedBarrier)
                    || (decisionOnly && !hasPendingDecision(checkpoint))
                    || (councilOnly && !hasPendingCouncilDecision(checkpoint))
                    || !restored.alerts().isEmpty())
                throw new IOException("Checkpoint is not a supported phase boundary");
            if (restored.pendingColonizationChoices != null)
                for (ColonizationChoice choice : restored.pendingColonizationChoices)
                    if (!restored.colonizationReferenceIsCanonical(choice))
                        throw new IOException("Restored colonization reference is detached");
            performingTurn = false;
            autoRunning = false;
            return restored;
        }
        catch (Exception | Error failure) {
            instance(previous);
            Rotp.rand(previousRandom);
            if (previous.options() != null)
                previous.options().setAsGame();
            throw failure;
        }
    }
    private static Rand copyCheckpointRandom(Rand random) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(random);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (Rand) input.readObject();
        }
    }
    private void runInProcessPhase(Phase phase) {
        Galaxy gal = galaxy();
        switch (phase) {
            case PREPARE -> {
                TradeTechNotification.resetSkipButton();
                validateAlwaysAtWar();
                validateAlwaysAlly();
                forEachHumanEmpire(Empire::startingNextTurnProcess);
                clearAllocationRequests();
                clearScoutedSystems();
                clearShipsConstructed();
                clearSpyActivity();
                clearAlerts();
                clearNotificationLimits();
                Ships.rallyTransitJoinCombat = options.rallyTransitJoinCombat();
            }
            case LAUNCH -> gal.preNextTurn();
            case MOVEMENT -> {
                gal.advanceTime();
                Empire.updateDynValues();
                gal.moveShipsInTransit();
                gal.events().nextTurn();
            }
            case COUNCIL -> gal.council().nextTurn();
            case EMPIRE_TURNS -> {
                if (!IDebugOptions.debugAutoRun()) {
                    GNNRankingNoticeCheck.nextTurn();
                    GNNExpansionEvent.nextTurn();
                }
                gal.nextEmpireTurns();
                gal.clearSpaceMonsters();
                forEachHumanEmpire(empire -> {
                    empire.setVisibleShips(true);
                    empire.setVisibleMonsters();
                });
            }
            case SPACE_COMBAT -> {
                gal.postNextTurn1();
                forEachHumanEmpire(Empire::updateScoutMessages);
            }
            case INVASIONS -> {
                gal.refreshAllEmpireViews();
                gal.postNextTurn2();
            }
            case POST_COLONIZATION -> gal.postNextTurn3();
            case DIPLOMACY -> gal.assessTurn();
            case DECISIONS -> gal.makeNextTurnDecisions();
            case REFRESH -> {
                gal.clearSpaceMonsters();
                forEachHumanEmpire(Empire::setVisibleMonsters);
                validate();
                gal.refreshAllEmpireViews();
                gal.refreshEmpireStatus();
            }
        }
    }
    private void finishInProcessPostPhase(Phase phase) {
        processNotifications();
        if (phase == Phase.DECISIONS) {
            forEachHumanEmpire(Empire::redoGovTurnDecisions);
            for (int recipientEmpireId : spyActivityByEmpire())
                SpyReportAlert.create(recipientEmpireId);
        }
    }
    private void forEachHumanEmpire(Consumer<Empire> action) {
        if (controllerRegistry == null) {
            action.accept(player());
            return;
        }
        for (PlayerSeat seat : controllerRegistry.seats()) {
            if (seat.controllerType() != PlayerSeat.ControllerType.HUMAN)
                continue;
            Empire empire = galaxy().empire(seat.empireId());
            if (empire != null && !empire.extinct())
                action.accept(empire);
        }
    }
    public CouncilDecisionAdapter councilDecisionAdapter() {
        if (councilDecisionAdapter == null)
            councilDecisionAdapter = new DesktopCouncilDecisionAdapter();
        return councilDecisionAdapter;
    }
    public void councilDecisionAdapter(CouncilDecisionAdapter adapter) {
        councilDecisionAdapter = java.util.Objects.requireNonNull(adapter, "adapter");
    }
    public ResearchDecisionAdapter researchDecisionAdapter() {
        if (researchDecisionAdapter == null)
            researchDecisionAdapter = new DesktopResearchDecisionAdapter();
        return researchDecisionAdapter;
    }
    public void researchDecisionAdapter(ResearchDecisionAdapter adapter) {
        researchDecisionAdapter = java.util.Objects.requireNonNull(adapter, "adapter");
    }
    public ColonizationDecisionAdapter colonizationDecisionAdapter() {
        if (colonizationDecisionAdapter == null)
            colonizationDecisionAdapter = new DesktopColonizationDecisionAdapter();
        return colonizationDecisionAdapter;
    }
    public void colonizationDecisionAdapter(ColonizationDecisionAdapter adapter) {
        colonizationDecisionAdapter = java.util.Objects.requireNonNull(adapter, "adapter");
    }
    public EspionageDecisionAdapter espionageDecisionAdapter() {
        if (espionageDecisionAdapter == null && controllerRegistry != null)
            throw new IllegalStateException("A rostered espionage decision needs an adapter");
        if (espionageDecisionAdapter == null)
            espionageDecisionAdapter = new DesktopEspionageDecisionAdapter();
        return espionageDecisionAdapter;
    }
    public void espionageDecisionAdapter(EspionageDecisionAdapter adapter) {
        espionageDecisionAdapter = java.util.Objects.requireNonNull(adapter, "adapter");
    }
    public BombardmentDecisionAdapter bombardmentDecisionAdapter() {
        if (bombardmentDecisionAdapter == null)
            bombardmentDecisionAdapter = new InProcessBombardmentDecisionAdapter(java.util.Map.of());
        return bombardmentDecisionAdapter;
    }
    public void bombardmentDecisionAdapter(BombardmentDecisionAdapter adapter) {
        bombardmentDecisionAdapter = java.util.Objects.requireNonNull(adapter, "adapter");
    }
    public SabotageDecisionAdapter sabotageDecisionAdapter() {
        if (sabotageDecisionAdapter == null)
            throw new IllegalStateException("A rostered sabotage mission needs an adapter");
        return sabotageDecisionAdapter;
    }
    public void sabotageDecisionAdapter(SabotageDecisionAdapter adapter) {
        sabotageDecisionAdapter = java.util.Objects.requireNonNull(adapter, "adapter");
    }
    public TurnNotificationSink turnNotificationSink() {
        if (turnNotificationSink == null)
            turnNotificationSink = new DesktopTurnNotificationSink();
        return turnNotificationSink;
    }
    public void turnNotificationSink(TurnNotificationSink sink) {
        turnNotificationSink = java.util.Objects.requireNonNull(sink, "sink");
    }
    public void galaxy(Galaxy g)         { galaxy = g; }

	public float populationBonus()		{ return options().planetSizeMultiplier(); }
	public static float damageBonus()	{ return 1.0f; }
	public static float researchBonus()	{ return 1.0f; }
    public float researchMapSizeAdjustment() {
        float stars = galaxy().numStarSystems();
        int races = galaxy().numOpponents()+2;
        float targetRatio = 12.0f;
        return sqrt(stars/races/targetRatio);
    }
    public static void addShipsConstructed(int empireId, ShipDesign design, int newCount) {
        GameSession session = instance();
        if (!design.active())
            throw new RuntimeException("Constructed an inactive ship design");

        HashMap<ShipDesign, Integer> counts = session.shipConstructionForEmpire(empireId);
        if (counts.isEmpty())
            addTurnNotificationForEmpire(empireId, new ShipConstructionNotification());

        counts.merge(design, newCount, Integer::sum);
    }
    private HashSet<Integer> spyActivityByEmpire() {
        if (spyActivityByEmpire == null)
            spyActivityByEmpire = new HashSet<>();
        return spyActivityByEmpire;
    }
    public void enableSpyReport() { enableSpyReport(Empire.PLAYER_ID); }
    public void enableSpyReport(int empireId) {
        if (controllerRegistry == null)
            spyActivity = true;
        else if (controllerRegistry.isHumanControlled(empireId))
            spyActivityByEmpire().add(empireId);
    }
    public boolean spyActivity() {
        return controllerRegistry == null ? spyActivity
                : spyActivityByEmpire().contains(Empire.PLAYER_ID);
    }
    private void clearSpyActivity() {
        spyActivity = false;
        spyActivityByEmpire().clear();
    }
	public static void addSystemScouted(StarSystem sys)					{ addSystemScouted(Empire.PLAYER_ID, sys); }
	public static void addSystemScoutedByAllies(StarSystem sys)			{ addSystemScoutedByAllies(Empire.PLAYER_ID, sys); }
	public static void addSystemScoutedByAstronomers(StarSystem sys)	{ addSystemScoutedByAstronomers(Empire.PLAYER_ID, sys); }
    public static void addSystemScouted(int empireId, StarSystem sys) {
        instance().scoutedSystemsForEmpire(empireId).get("Scouts").add(sys);
    }
    public static void addSystemScoutedByAllies(int empireId, StarSystem sys) {
        instance().scoutedSystemsForEmpire(empireId).get("Allies").add(sys);
    }
    public static void addSystemScoutedByAstronomers(int empireId, StarSystem sys) {
        instance().scoutedSystemsForEmpire(empireId).get("Astronomers").add(sys);
    }
    private static void clearScoutedSystems() {
        GameSession session = instance();
        if (session.systemsScoutedByEmpire == null)
            return;
        for (var bySource : session.systemsScoutedByEmpire.values())
            for (List<StarSystem> systems : bySource.values())
                systems.clear();
    }
    private void clearScoutedSystemsForEmpire(int empireId) {
        for (List<StarSystem> systems : scoutedSystemsForEmpire(empireId).values())
            systems.clear();
    }
    /** Hot seat: load a stored report into the viewer's scouting overlay data; null clears it. */
    public void hotSeatScoutedReport(Map<String, ? extends List<Integer>> systemIds) {
        clearScoutedSystemsForEmpire(Empire.PLAYER_ID);
        if (systemIds == null)
            return;
        var bySource = scoutedSystemsForEmpire(Empire.PLAYER_ID);
        systemIds.forEach((source, ids) -> {
            List<StarSystem> systems = bySource.computeIfAbsent(source, ignored -> new ArrayList<>());
            for (int id : ids)
                systems.add(galaxy.system(id));
        });
    }
    /** Hot seat: load a stored report into the viewer's ship construction overlay data. */
    public void hotSeatShipsReport(Map<Integer, Integer> designCounts) {
        var ships = shipConstructionForEmpire(Empire.PLAYER_ID);
        ships.clear();
        var lab = galaxy.empire(Empire.PLAYER_ID).shipLab();
        designCounts.forEach((designId, count) -> {
            if (lab.design(designId) != null)
                ships.put(lab.design(designId), count);
        });
    }
	public static boolean haveScoutedSystems()	{
        return instance().haveScoutedSystemsForEmpire(Empire.PLAYER_ID);
    }
    private boolean haveScoutedSystemsForEmpire(int empireId) {
        for (Collection<StarSystem> systems : scoutedSystemsForEmpire(empireId).values()) {
            if (!systems.isEmpty())
                return true;
        }
        return false;
    }
    public void addSystemToAllocate(StarSystem sys, String reason) {
        if (sys.empire() == null)
            return;
        addSystemToAllocateForEmpire(sys.empire().id, sys, reason);
    }
    public void addSystemToAllocateForEmpire(int empireId, StarSystem sys, String reason) {
        // don't prompt to allocate systems that are in rebellion
        if (sys.isColonized() && sys.colony().inRebellion())
            return;

        int recipientEmpireId = controllerRegistry == null ? Empire.PLAYER_ID : empireId;
        if (controllerRegistry != null && !controllerRegistry.isHumanControlled(recipientEmpireId))
            return;

        log("Re-allocate: ", sys.name(), " :", reason);
        HashMap<StarSystem, List<String>> requests = allocationRequestsForEmpire(recipientEmpireId);
        if (!requests.containsKey(sys))
            requests.put(sys, new ArrayList<>());

        if (!requests.get(sys).contains(reason))
            requests.get(sys).add(reason);
    }
	public static boolean awaitingAllocation(StarSystem sys)		{
        GameSession session = instance();
        int ownerId = session.controllerRegistry == null ? Empire.PLAYER_ID : sys.empId();
        return session.allocationRequestsForEmpire(ownerId).containsKey(sys);
    }
	public static void addTurnNotification(TurnNotification notif)	{
        instance().notifications().add(new QueuedTurnNotification(notif, null));
    }
    public static void addTurnNotificationForEmpire(int empireId, TurnNotification notif) {
        instance().notifications().add(new QueuedTurnNotification(notif, empireId));
    }
    public static void requestTechSelection(TechCategory category) {
        if (category.requestSelection())
            addTurnNotificationForEmpire(category.empire().id, new SelectTechNotification(category));
    }
	public static void removePendingNotification(String key)	{
		List<QueuedTurnNotification> notifs = new ArrayList<>(instance().notifications());
        for (QueuedTurnNotification notif: notifs) {
            if (notif.notification().key().equals(key))
				instance().notifications().remove(notif);
        }

    }
	private GameSession() {
		Rotp.ifIDE("==================== Create GameSession =====================");
		//options(Rotp.rulesetManager().defaultRuleset());
	}
    public void startGame(IGameOptions newGameOptions) {
        startGame(newGameOptions, null);
    }
    public void startGame(IGameOptions newGameOptions, ControllerRegistry registry) {
        startGame(newGameOptions, registry, null);
    }
    public void startHotSeatGame(IGameOptions newGameOptions,
            rotp.multiplayer.hotseat.HotSeatSetup setup) {
        startHotSeatGame(newGameOptions, setup, false);
    }
    public void startHotSeatGame(IGameOptions newGameOptions,
            rotp.multiplayer.hotseat.HotSeatSetup setup, boolean byEmail) {
        java.util.Objects.requireNonNull(setup, "setup");
        if (newGameOptions.isAutoPlay() || newGameOptions.randomNumAliens()
                || IDebugOptions.debugAutoRun() || newGameOptions.selectedIronmanLoad()
                || !newGameOptions.isGameOptionsAllowed())
            throw new IllegalArgumentException(
                    "Hot seat requires fixed opponents, autoplay off, debug autorun off and ironman off");
        ControllerRegistry registry = setup.registry(newGameOptions.selectedNumberOpponents() + 1);
        startGame(newGameOptions, registry, setup, !byEmail ? null : new rotp.multiplayer.pbem.PlayByEmail(
                setup.humans().stream().map(rotp.multiplayer.hotseat.HotSeatSetup.Assignment::playerId).toList(),
                java.time.LocalDateTime.now()));
    }
    private void startGame(IGameOptions newGameOptions, ControllerRegistry registry,
            rotp.multiplayer.hotseat.HotSeatSetup setup) {
        startGame(newGameOptions, registry, setup, null);
    }
    private void startGame(IGameOptions newGameOptions, ControllerRegistry registry,
            rotp.multiplayer.hotseat.HotSeatSetup setup, rotp.multiplayer.pbem.PlayByEmail byEmail) {
        stopCurrentGame();
        hotSeatSetup = setup;
        playByEmail = byEmail;
        hotSeatSaveEnvelope = null;
        hotSeatPolicies = null;
        hotSeatInbox = setup == null ? null : new rotp.multiplayer.hotseat.HotSeatInbox();
        hotSeatState = setup == null ? null : new rotp.multiplayer.hotseat.HotSeatState(
                setup.humans().stream().map(rotp.multiplayer.hotseat.HotSeatSetup.Assignment::playerId)
                        .toList(), 1);
        hotSeatViews = null;
        controllerRegistry = registry;
        turnCoordinator = null;
        inProcessPendingPostPhase = null;
        inProcessCheckpointRandom = null;
        inProcessSavedBarrier = null;
        pendingColonizationChoices = new ArrayList<>();
        colonizationDecisionSequence = 0;
        pendingDiplomacyChoices = new ArrayList<>();
        diplomacyDecisionSequence = 0;
        councilDecisionAdapter = null;
        researchDecisionAdapter = null;
        colonizationDecisionAdapter = null;
        espionageDecisionAdapter = null;
        bombardmentDecisionAdapter = null;
        sabotageDecisionAdapter = null;
        turnNotificationSink = null;

        options(newGameOptions.copyAllOptions());
		rulesetManager().setAsGameMode();
    	instance().getGovernorOptions().gameStarted();
        if (setup != null) hotSeatPolicies = new rotp.multiplayer.hotseat.HotSeatPolicies();
        startExecutors();

        synchronized(ONE_GAME_AT_A_TIME) {
            id = (long) (Long.MAX_VALUE*random());
            achievementId = newAchievementId();
            GalaxyFactory.current().newGalaxy();
            if (registry != null) {
                for (PlayerSeat seat : registry.seats()) {
                    if (galaxy.empire(seat.empireId()) == null)
                        throw new IllegalArgumentException("Controller seat has no empire: "
                                + seat.empireId());
                }
            }
            log("Galaxy complete");
            status().startGame();
            clearScoutedSystems();
            clearAllocationRequests();
            clearShipsConstructed();
            clearSpyActivity();
            galaxy().startGame();
            saveRecentSession();
            saveBackupSession(1);
        }
    }
    // BR: For Restart with new options
    public void restartGame(IGameOptions newGameOptions, GalaxyCopy src) {
        stopCurrentGame();
        hotSeatSetup = null;
        playByEmail = null;
        hotSeatSaveEnvelope = null;
        hotSeatPolicies = null;
        hotSeatInbox = null;
        hotSeatState = null;
        hotSeatViews = null;
        controllerRegistry = null;
        turnCoordinator = null;
        inProcessPendingPostPhase = null;
        inProcessCheckpointRandom = null;
        inProcessSavedBarrier = null;
        pendingColonizationChoices = new ArrayList<>();
        colonizationDecisionSequence = 0;
        pendingDiplomacyChoices = new ArrayList<>();
        diplomacyDecisionSequence = 0;
        councilDecisionAdapter = null;
        researchDecisionAdapter = null;
        colonizationDecisionAdapter = null;
        espionageDecisionAdapter = null;
        bombardmentDecisionAdapter = null;
        sabotageDecisionAdapter = null;
        turnNotificationSink = null;
        options(src.options().copyAllOptions());
		rulesetManager().setAsGameMode();
    	instance().getGovernorOptions().gameStarted();
        startExecutors();

        synchronized(ONE_GAME_AT_A_TIME) {
            id = (long) (Long.MAX_VALUE*random());
            achievementId = newAchievementId();
            GalaxyFactory.current().newGalaxy(src);
            log("Galaxy complete");
            status().startGame();
            clearScoutedSystems();
            clearAllocationRequests();
            clearShipsConstructed();
            clearSpyActivity();
            galaxy().startGame();
    		GameUI.gameName = generateGameName(newGameOptions);
            saveRecentSession();
            saveBackupSession(1);
        }
    }
	private static void startExecutors()	{ smallSphereService = Executors.newSingleThreadExecutor(); }
    private void resetStaticVars() {
		vars.clear();
		performingTurn	= false;

		notifications().clear();

		clearAllocationRequests();
		clearScoutedSystems();

        clearShipsConstructed();

		alerts().clear();
		alertRecords().clear();
		viewedAlertsByEmpire().clear();
		autoRunning		= false;
		ironmanLocked	= false;
		aFewMoreTurns	= false;
		RacesUI.instance.resetFinalVars();
		EmpireColonySpendingPane.resetPanel();
		MultiColonySpendingPane.resetPanel();
		AdvisorPanel.ADVISOR.onHold();

		MainUI mainUI = RotPUI.instance().mainUI();
		if (mainUI != null) {
			mainUI.nextTurnSprites().clear();
			GalaxyMapPanel map = mainUI.map();
			if (map != null) {
				map.clearHoverSprite();
				map.resetRangeAreas();
			}
		}
    }
    private void stopCurrentGame() {
        if (hotSeatController != null) {
            var closing = hotSeatController;
            hotSeatController = null;
            if (javax.swing.SwingUtilities.isEventDispatchThread()) closing.close();
            else {
                try { javax.swing.SwingUtilities.invokeAndWait(closing::close); }
                catch (Exception failure) { throw new IllegalStateException("Unable to close hot-seat desktop", failure); }
            }
        }
        RotPUI.instance().mainUI().clearAdvice();
        resetStaticVars(); // BR: better twice than never!
        //vars().clear();
        //clearAlerts();
        // shut down any threads running from previous game
        smallSphereService().shutdownNow();
    }
	public static void exit()		{ System.exit(0); }
	public static Object var(String key)				{ return vars().get(key); }
	public static void var(String key, Object value)	{ vars().put(key, value); }
	public static void removeVar(String key)			{ vars().remove(key); }
    /** Session variables hold desktop selections/caches, never simulation ownership. */
    public static void clearViewingState() { vars().clear(); }
    public void replaceVarValue(Object prevValue, Object newValue) {
        List<String> keys = new ArrayList<>();
        keys.addAll(vars().keySet());
        for (String key: keys) {
            if (var(key) == prevValue) {
                log("replacing value for session var: ", key);
                var(key, newValue);
            }
        }
    }
	public static void removeVarValue(Object value)	{
        List<String> keys = new ArrayList<>();
        keys.addAll(vars().keySet());
        for (String key: keys) {
            if (var(key) == value)
                vars().remove(key);
        }
    }
    public void nextTurn() {
        if (hotSeatState != null) {
            if (hotSeatController == null) throw new IllegalStateException("Hot-seat desktop is not attached");
            var snapshot = hotSeatState.snapshot();
            hotSeatController.finishPlayerTurn(snapshot.ownerPlayerId(), snapshot.revision());
            return;
        }
        if (controllerRegistry != null)
            throw new IllegalStateException("Rostered turns require advanceInProcessTurn()");
    	if (IDebugOptions.debugAutoRun())
    		autoRunning = true;
    	nextTurnLoop();
    }
	public static void pauseAutoRun()	{ autoRunning = false; }
	public static boolean autoRunning()	{ return autoRunning; }
    private void nextTurnLoop() {
        if (turnFailed || performingTurn())
            return;
        
        performingTurn = true;
        nextTurnThread = new Thread(nextTurnProcess(), "NextTurnProcess");
        nextTurnThread.start();
    }
    public void waitUntilNextTurnCanProceed() {
        while(suspendNextTurn)
            sleep(200);
    }
	public static boolean sufficientHeapSpace() {
        long maxHeap = Rotp.maxHeapMemory;
        long reqHeap = 200;
        return maxHeap > reqHeap;
    }
    public MatchOutcome matchOutcome() { return MatchOutcomeEvaluator.evaluate(this); }
    public boolean inProgress()  {
        return controllerRegistry == null || galaxy == null
                ? status().inProgress() : !matchOutcome().finished();
    }
    private void debugMonitor(long fileSize, long dt) {
    	boolean append = galaxy().currentTurn() > 1;
    	String turn;
    	String duration;
    	String state = status().key();
    	if (dt == 0) {
    		turn = getTurn(".5");
    		duration = " ";
    	}
    	else {
    		turn = getTurn(".0");
    		duration = msToHMS(dt);
    	}
    	String time = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        String memS = concat(turn,
        		          " | ", Rotp.getMemoryInfo(false),
        		          " | File size:", String.format("%10d", fileSize),
        		          " | ", time);   	
		if (IDebugOptions.debugConsoleMemory())
			System.out.println(memS);
		if (IDebugOptions.debugFileMemory())
			writeToFile(MEMORY_LOGFILE, memS, true, append);        
		if (IDebugOptions.debugAutoRun()) {
			String s = concat(turn,
					" | Col:", String.format("%5d", player().numColonies()),
					"/", StringUtils.rightPad(String.valueOf(galaxy().numColonizedSystems()), 5),
					" | Aliens:", String.format("%3d", player().numContacts()),
					"/", StringUtils.rightPad(String.valueOf(galaxy().numActiveEmpires()-1), 2),
					" | War:", String.format("%3d", player().numEnemies()),
					" | Status: ", state,
					" | ", new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()),
					" | ", duration
					);
			writeToFile(AUTORUN_LOGFILE, s, true, append);
			if (IDebugOptions.consoleAutoRun())
				System.out.println(s);
        }
        if (IDebugOptions.debugShowMoreMemory()) {
            memLog();
            RotPUI.instance().mainUI().showMemoryLowPrompt(); // TO DO BR: Comment
        }

    }
	public void debugAddOn() {
		// BR: easy to track temporary test code.
//		StarSystem sys = galaxy().system("Koch");
//		if (sys == null)
//			return;
//		sys.eventKey(RandomEventPlague.eventKey);
	}
	@SuppressWarnings("unused") private void ModnarPrivateLogging() {
		String LogPath = Rotp.jarPath();
		File TestLogFile = new File(LogPath, "TestLogFile.txt");
		if (galaxy.currentTurn() % 5 == 0) { // log every 5 turns
			PrintWriter out = null;
			try {
				out = new PrintWriter(new BufferedWriter(new FileWriter(TestLogFile, true)));
				out.println("Turn: "+ str(galaxy.currentTurn()));
				for (Empire e: galaxy().empires()) {
					StarSystem sys1 = e.mostPopulousSystemForCiv(e);
					// float relationToPlayer = 0.0f;
					// if (!(e==player())) {
					// 	EmpireView pl = e.viewForEmpire(player());
					// 	relationToPlayer = pl.embassy().relations();
					// }
					out.println(String.format("%10s", e.raceName())
					+ String.format("%6d", e.numColonizedSystems())
					+ String.format("%12.2f", e.totalPlanetaryPopulation())
					+ String.format("%12.2f", e.totalPlanetaryProduction())
					+ String.format("%12.0f", e.totalFleetSize())
					+ String.format("%10.2f", 100*e.shipMaintCostPerBC()) + "%"
					+ String.format("%10.2f", 100*e.missileBaseCostPerBC()) + "%"
					+ String.format("%10.2f", 100*e.totalSecurityCostPct()) + "%"
					+ String.format("%12.2f", e.totalPlanetaryResearch())
					+ String.format("%8.2f", e.tech().avgTechLevel())
					+ String.format("%6d", e.numEnemies())
					+ String.format("reserve %12.2f", e.totalReserve())
					+ String.format("trade %12.2f", e.netTradeIncome())
					+ String.format("%10s", sys1.name())
					+ String.format("%8.2f", sys1.colony().industry().factories())
					+ String.format("%8.2f", sys1.colony().reserveIncome())
					+ String.format("%8.2f", sys1.colony().totalIncome())
					+ String.format("%8.2f", sys1.colony().production())
					+ String.format("%8.2f", sys1.colony().defense().rawBases())
					
					);
				}
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void validateAlwaysAtWar()	{
		boolean alwaysAtWar = options().alwaysAtWar();
		if(alwaysAtWar == lastAlwaysAtWar)
			return;
		lastAlwaysAtWar = alwaysAtWar;
		if (alwaysAtWar)
			galaxy().startAlwaysAtWar();
	}
	private void validateAlwaysAlly()	{
		boolean alwaysAlly  = options().alwaysAlly();
		if(alwaysAlly == lastAlwaysAlly)
			return;
		lastAlwaysAlly = alwaysAlly;
		if (alwaysAlly)
			galaxy().startAlwaysAlly();
	}

    private Runnable nextTurnProcess() {
        return () -> {
			try {
				ErrorUI.inTurnMode();
                performingTurn = true;
                Galaxy gal = galaxy();
                String turnTitle = nextTurnTitle();
                NoticeMessage.setStatus(turnTitle, text("TURN_SAVING") + " a");
                FlightPathSprite.clearWorkingPaths();
                RotPUI.instance().mainUI().saveMapState();
                log("Next Turn - BEGIN: ", str(galaxy.currentYear()));
                log("Autosaving pre-turn");
                long ufs = instance().saveRecentSession(false);
                debugMonitor(ufs, 0);
				// ModnarPrivateLogging();

                long startMs = timeMs();
                if (controllerRegistry == null && turnCoordinator().turn() >= 0
                        && turnCoordinator().nextPhase() != null)
                    turnCoordinator().abandonTerminalTurn();
                turnCoordinator().startTurn(gal.currentTurn());
                turnCoordinator().run(Phase.PREPARE, () -> {
					TradeTechNotification.resetSkipButton();
					validateAlwaysAtWar();
					validateAlwaysAlly();
					forEachHumanEmpire(Empire::startingNextTurnProcess);
                    clearAllocationRequests();
                    clearScoutedSystems();
                    clearShipsConstructed();
                    clearSpyActivity();
                    clearAlerts();
                    clearNotificationLimits();
                    RotPUI.instance().repaint();
                    // BR: This could be called quite often, better make it quick.
                    Ships.rallyTransitJoinCombat = options.rallyTransitJoinCombat();
                    processNotifications();
                });
                turnCoordinator().run(Phase.LAUNCH,
                        () -> gal.preNextTurn()); // Launching deployed fleets

				if (!inProgress()) {
					ErrorUI.inPlayerMode();
					return;
				}
                // REMOVE THIS CODE
                // playerViewAllSystems();
                // playerViewAllHomeSystems();

                // all intra-empire events: civ turns, ship movement, etc
                turnCoordinator().run(Phase.MOVEMENT, () -> {
                    gal.advanceTime();	// Clock only
					Empire.updateDynValues();
                    gal.moveShipsInTransit(); // Move and arrival
                    gal.events().nextTurn();
                });
                RotPUI.instance().selectMainPanel();

                turnCoordinator().run(Phase.COUNCIL, () -> gal.council().nextTurn());
                turnCoordinator().run(Phase.EMPIRE_TURNS, () -> {
                    if (!IDebugOptions.debugAutoRun()) {
                        GNNRankingNoticeCheck.nextTurn();
                        GNNExpansionEvent.nextTurn();
                    }
                    gal.nextEmpireTurns();
                    gal.clearSpaceMonsters();
                    forEachHumanEmpire(empire -> {
                        empire.setVisibleShips(true); // BR: To make the call to ufo tracking unique.
                        empire.setVisibleMonsters();
                    });
                });

                // test game over conditions
                // randomlyEndGame(); // TO DO BR: Comment
				if (!inProgress()) {
					ErrorUI.inPlayerMode();
					return;
				}

                if (processNotifications()) {
                    log("Notifications processed 1 - back to MainPanel");
                    RotPUI.instance().selectMainPanel();
                }
                turnCoordinator().run(Phase.SPACE_COMBAT,
                        () -> gal.postNextTurn1()); // ship combat & invasions at each system
				if (!inProgress()) {
					ErrorUI.inPlayerMode();
					return;
				}

                forEachHumanEmpire(Empire::updateScoutMessages);
                if (processNotifications()) {
                    log("Notifications processed 2 - back to MainPanel");
                    RotPUI.instance().selectMainPanel();
                }
                turnCoordinator().run(Phase.INVASIONS, () -> {
                    gal.refreshAllEmpireViews();
                    gal.postNextTurn2(); // Ship and colonies interaction => Troop Invasion
                });

				if (!inProgress()) {
					ErrorUI.inPlayerMode();
					return;
				}
                if (processNotifications()) {
                    log("Notifications processed 3 - back to MainPanel");
                    RotPUI.instance().selectMainPanel();
                }
                turnCoordinator().run(Phase.POST_COLONIZATION,
                        () -> gal.postNextTurn3()); // BR: post colonization scouting
                if (processNotifications()) {
                    log("Notifications processed 3a - back to MainPanel");
                    RotPUI.instance().selectMainPanel();
                }

                // all diplomatic fallout: praise, warnings, treaty offers, war declarations + Research
                turnCoordinator().run(Phase.DIPLOMACY,
                        () -> gal.assessTurn()); // Start rallying

                if (processNotifications()){
                    log("Notifications processed 4 - back to MainPanel");
                    RotPUI.instance().selectMainPanel();
                }
                turnCoordinator().run(Phase.DECISIONS,
                        () -> gal.makeNextTurnDecisions());

                if (processNotifications()){
                    log("Notifications processed 5 - back to MainPanel");
                    RotPUI.instance().selectMainPanel();
                }

				// Previous Governor call was too early for "isFollowingColonyRequests"
				// So it's redone there so the player doesn't have to loop through
				// the colonies to refresh them.
				// Only this specific governor is called, as I don't want to
				// revalidates the possible impacts on the other one.
				forEachHumanEmpire(Empire::redoGovTurnDecisions);

                if (controllerRegistry == null) {
                    if (spyActivity)
                        SpyReportAlert.create();
                }
                else {
                    for (int recipientEmpireId : spyActivityByEmpire())
                        SpyReportAlert.create(recipientEmpireId);
                }

                turnCoordinator().run(Phase.REFRESH, () -> {
                    log("Refreshing Player Views");
                    gal.clearSpaceMonsters();
                    forEachHumanEmpire(Empire::setVisibleMonsters);
                    NoticeMessage.resetSubstatus(text("TURN_REFRESHING"));
                    validate();
                    //BR: Tentative to fix range area errors
                    if (!IDebugOptions.debugAutoRun()) {
                        RotPUI.instance().mainUI().map().resetRangeAreas();
                        player().setEmpireMapAvgCoordinates();
                    }
                    gal.refreshAllEmpireViews();
                    gal.refreshEmpireStatus(); // BR: was not up to date at the beginning of turns
                });

                log("Autosaving post-turn");
                log("NEXT TURN PROCESSING TIME: ", str(timeMs()-startMs));
                NoticeMessage.resetSubstatus(text("TURN_SAVING") + " b");
                ufs = instance().saveRecentSession(true);

                if (processNotifications()) { // BR: to display scouted Stars after diplomacy
                	log("Notifications processed 6 - back to MainPanel");
                	RotPUI.instance().selectMainPanel();
                }

				if (!systemsToAllocate().isEmpty())
					if (options.showAllocatePopUp())
						RotPUI.instance().allocateSystems();
					else
						systemsToAllocate().clear();

                log("Reselecting main panel");
                RotPUI.instance().mainUI().showDisplayPanel();
                RotPUI.instance().selectMainPanel();
                notifications().clear();
                // ensure Next Turn takes at least a minimum time
                long spentMs = timeMs() - startMs;
                if (spentMs < MINIMUM_NEXT_TURN_TIME && !IDebugOptions.debugAutoRun()) {
                    try { Thread.sleep(MINIMUM_NEXT_TURN_TIME - spentMs);
                    } catch (InterruptedException e) { }
                }
                else if (spentMs < 100) { // To give time to thread to synchronize.
                    try { Thread.sleep(100 - spentMs);
                    } catch (InterruptedException e) { }
                }
                RotPUI.instance().repaint();
                log("Next Turn - END: ", str(galaxy.currentYear()));
            	debugMonitor(ufs, spentMs);
            }
            catch(Exception e) {
                turnFailed = true;
                autoRunning = false;
                err("Unexpected error during Next Turn:", e.toString());
                exception(e);
            }
            finally {
                if (turnFailed) {
                    performingTurn = false;
                    suspendNextTurn = false;
                    return;
                }
                RotPUI.instance().mainUI().restoreMapState();
				Empire.updateDynValues();
                if (Rotp.memoryLow())
                    RotPUI.instance().mainUI().showMemoryLowPrompt();
                // handle game over possibility
                // Follow turn limit request
                if(benchmarkBreakAndContinue()) {
                	IDebugOptions.debugBMContinue();
                	RotPUI.instance().selectGameOverPanel();
                	performingTurn = false;
					ErrorUI.inPlayerMode();
					return;
				}
				if (autoRunning && IDebugOptions.debugAutoRun()) {
					if (aFewMoreTurns()) {
						performingTurn = false;
						nextTurnLoop();
						return;
					}
                	// Auto Run Mode Stop if:
                   	// 1) Easy case: the player won
                   	// 2) The player lost with option StopOnLoss
                	// 3) Military win: Only one empire remaining
                	// 4) Diplomatic win, no rebels
                	// 5) Final war: one side win
                 	if(status().won()) {
                		RotPUI.instance().selectGameOverPanel();
                		performingTurn = false;
						ErrorUI.inPlayerMode();
						return;
                	}
                 	if(status().lost() && IDebugOptions.debugARStopOnLoss()) {
                		RotPUI.instance().selectGameOverPanel();
                		performingTurn = false;
						ErrorUI.inPlayerMode();
						return;
                	}
                	// Stop if only one empire remaining and player started with opponent(s)
                	if (galaxy().numActiveEmpires() == 1 
                			&& options.selectedOpponentRaces()[0]!=null) {
                		RotPUI.instance().selectGameOverPanel();
                		performingTurn = false;
						ErrorUI.inPlayerMode();
						return;
                	}
                	GalacticCouncil council = galaxy().council();
                	boolean wonByAlly = !council.finalWar(); // No rebellion or win by ally.
                    boolean wonByRebels = council.allies().isEmpty();
                	if (council.rebelion() && (wonByAlly || wonByRebels) ) {
            			// System.out.println("wonByAlly Or wonByRebels");
                		RotPUI.instance().selectGameOverPanel();
                		performingTurn = false;
						ErrorUI.inPlayerMode();
						return;
                	}
               		performingTurn = false;
               		nextTurnLoop();
                }
                else { // Normal mode
                    if (!inProgress())
                        RotPUI.instance().selectGameOverPanel();
                }
                performingTurn = false;
                if (IDebugOptions.selectedShowVIPPanel() && status().inProgress())
                	VIPConsole.turnCompleted(galaxy().currentTurn());
				ErrorUI.inPlayerMode();
            }
        };
    }
	private boolean benchmarkBreakAndContinue() {
		if (IDebugOptions.debugBMBreak())
			return true;
		if (!IDebugOptions.debugBenchmark())
			return false;
		int turn = galaxy().currentTurn();
		int maxTurns = IDebugOptions.debugBMMaxTurns();
		if (maxTurns > 0 && turn > maxTurns) {
			System.err.println("maxTurns > 0 && turn > maxTurns");
			return true;
		}
		int maxLostTurns = IDebugOptions.debugBMLostTurns();
		if (maxLostTurns > 0 && status().lost()) {
			if (lastTurnAlive == null)
				lastTurnAlive = player().status().lastTurnAlive();
			if (turn - lastTurnAlive > maxLostTurns) {
				System.err.println("turn - lastTurnAlive > maxLostTurns");
				return true;
			}
		}
    	return false;
    }
	private static void clearNotificationLimits()	{ DiplomaticNotification.clearNotificationLimits(); }
    public boolean processNotifications() {
        log("Processing player notifications: ", str(notifications().size()));
        if (controllerRegistry != null || !options().isAutoPlay()) {
            if (controllerRegistry == null) {
                if (haveScoutedSystems() && notifications().stream().noneMatch(entry ->
                        entry.notification() instanceof SystemsScoutedNotification))
                    addTurnNotification(new SystemsScoutedNotification());
            }
            else {
                for (PlayerSeat seat : controllerRegistry.seats()) {
                    int empireId = seat.empireId();
                    if (seat.controllerType() != PlayerSeat.ControllerType.HUMAN
                            || !haveScoutedSystemsForEmpire(empireId))
                        continue;
                    if (notifications().stream().noneMatch(entry ->
                            entry.notification() instanceof SystemsScoutedNotification
                                    && Integer.valueOf(empireId).equals(entry.recipientEmpireId())))
                        addTurnNotificationForEmpire(empireId, new SystemsScoutedNotification());
                }
            }
        }


        if (notifications().isEmpty())
            return false;
        // received a concurrent modification here... iterate over temp array
        List<QueuedTurnNotification> notifs = new ArrayList<>(notifications());
        notifs.sort((a, b) -> a.notification().compareTo(b.notification()));
        TurnNotificationSink sink = turnNotificationSink();
        if (sink instanceof StrictInProcessNotificationSink) {
            for (QueuedTurnNotification entry : notifs) {
                sink.deliver(this, List.of(entry));
                notifications().remove(entry);
                if (entry.notification() instanceof SystemsScoutedNotification
                        && entry.recipientEmpireId() != null)
                    clearScoutedSystemsForEmpire(entry.recipientEmpireId());
            }
        }
        else {
            sink.deliver(this, notifs);
            notifications().removeAll(notifs);
        }
        clearScoutedSystems();
        return true;
    }
    public void startGroundCombat() { // For test and debug only
        for (EmpireView v : player().empireViews()) {
            if ((v!= null) && !v.embassy().contact()) {
                v.embassy().makeFirstContact();
                v.embassy().declareWar();
                break;
            }
        }

        if (galaxy().currentTurn() > 2)
            return;
        Empire pl = player();
        if (pl.hostiles().isEmpty())
            return;
        EmpireView ev = random(pl.hostiles());
        StarSystem sys = galaxy().system(pl.homeSysId());

        Empire emp = galaxy().empire(ev.empId());
        Transport tr = emp.allColonizedSystems().get(0).colony().transport();
        tr.setDest(sys);
        tr.size(30); // better hope you're playing the Bulrathi
        tr.launch();
        tr.arrive();
    }
    public void startShipCombat() {
        if (galaxy().currentTurn() > 2)
            return;
        Empire pl = player();
        if (pl.hostiles().isEmpty())
            return;
        EmpireView ev = random(pl.hostiles());
        StarSystem sys = galaxy().system(pl.homeSysId());
        sys.colony().defense().bases(3);

        // ShipDesign plSc = pl.shipLab().scoutDesign();
        ShipDesign plSh = pl.shipLab().fighterDesign();
        ShipFleet plFl = sys.orbitingFleetForEmpire(pl);
        if (plFl != null) {
            plFl.addShips(plSh.id(), 2);
        }
        else {
            plFl = new ShipFleet(pl.id, sys);
            plFl.addShips(plSh.id(), 2);
            sys.acceptFleet(plFl);
        }

        ShipDesign enSh = ev.shipLabUncut().fighterDesign();
        ShipDesign enSh2 = ev.shipLabUncut().bomberDesign();
        ShipFleet enFl = new ShipFleet(ev.empId(), sys);
        enFl.addShips(enSh.id(), 5);
        enFl.addShips(enSh2.id(), 3);
        sys.acceptFleet(enFl);
    }
    public void startShipCombat2() {
        if (galaxy().currentTurn() > 2)
            return;
        Empire pl = player();
        if (pl.hostiles().isEmpty())
            return;
        EmpireView ev = random(pl.hostiles());
        StarSystem sys = galaxy().system(pl.homeSysId());
        sys.colony().defense().bases(1);
        ShipDesign plCo= pl.shipLab().colonyDesign();
        ShipDesign plSc = pl.shipLab().scoutDesign();
        ShipDesign plSh = pl.shipLab().fighterDesign();
        ShipFleet plFl = sys.orbitingFleetForEmpire(pl);
        if (plFl != null) {
            plFl.removeShips(plSc.id(), 2, false);
            plFl.removeShips(plCo.id(), 1, false);
            //plFl.addShips(plSh, 2);
        }
        else {
            plFl = new ShipFleet(pl.id, sys);
            plFl.addShips(plSh.id(), 2);
            sys.acceptFleet(plFl);
        }

        ShipDesign enSh = ev.shipLabUncut().fighterDesign();
        ShipWeapon miss = ev.shipLabUncut().missileWeapon(0, 5);
        enSh.addWeapon(miss, 20);
        ShipFleet enFl = new ShipFleet(ev.empId(), sys);
        enFl.addShips(enSh.id(), 5);
        //enFl.addShips(enSh2, 3);
        sys.acceptFleet(enFl);
    }
    public void startGalacticCouncil() {
        if (galaxy().currentTurn() == 2) {
            for (Empire emp: galaxy().empires())
                emp.makeFullContact();
        }
    }
	public static void startGNNNotification()	{ (new GNNRankingNoticeCheck()).showRanking(); }
    public void randomlyEndGame() {
        if (galaxy().numberTurns() < 2)
            return;
        galaxy().council().leader(random(galaxy().empires()));
        player().lastAttacker(random(galaxy().empires()));

        int r = roll(0,11);
        switch(r) {
            case 0: session().status().loseOverthrown(); break;
            case 1: session().status().loseMilitary(); break;
            case 2: session().status().loseDiplomatic(); break;
            case 3: session().status().loseNewRepublic(); break;
            case 4: session().status().loseRebellion(); break;
            case 5: session().status().winDiplomatic(); break;
            case 6: session().status().winMilitary(); break;
            case 7: session().status().winMilitaryAlliance(); break;
            case 8: session().status().winNewRepublic(); break;
            case 9: session().status().winRebellion(); break;
            case 10: session().status().winRebellionAlliance(); break;
            case 11: session().status().wonCouncilAlliance(); break;
        }
    }
    public void formAllianceWithRandomContact() {
        int empId = random(player().contactedEmpires()).id;
        if (!player().alliedWith(empId)) {
            EmpireView v = player().viewForEmpire(empId);
            v.embassy().signAlliance();
        }
    }
    public void formAlliancesWithContacts() {
        for (Empire e: player().contactedEmpires()) {
            if (!player().alliedWith(e.id)) {
                EmpireView v = player().viewForEmpire(e.id);
                v.embassy().signAlliance();
            }
        }
    }
    public void randomlyLearnTechs() {
        if (galaxy().numberTurns() == 2) {
            err("Each empire randomly learning 10 unknown techs to facilitate TechExchange testing");
            for (Empire emp: galaxy().empires()) {
                for (int i=0;i<10;i++)
                    emp.tech().learnTech(emp.tech().randomUnknownTech(1,20, emp.isHumanEmpire(), null, null).id()); // BR: always add in some Technologies
            }
            err("Each empire spying on each other");
            for (Empire emp1: galaxy().empires()) {
                for (Empire emp2: galaxy().empires()) {
                    if (emp1 != emp2) {
                        EmpireView v = emp1.viewForEmpire(emp2);
                        v.spies().updateTechList();
                    }
                }
            }
        }
    }
    public void startHighTechShipCombat() { // For Test and debug
        if (galaxy().numberTurns() > 2)
            return;

        // make enemies
        for (EmpireView v : player().empireViews()) {
            if ((v!= null) && !v.embassy().contact()) {
                v.embassy().makeFirstContact();
                v.embassy().declareWar();
                break;
            }
        }

        // learn everything
        for (Empire emp: galaxy().empires())
            emp.tech().learnAll();

        Empire pl = player();
        if (pl.hostiles().isEmpty())
            return;
        EmpireView ev = random(pl.hostiles());
        StarSystem sys = galaxy().system(pl.homeSysId());
        sys.colony().defense().bases(3);

        ShipDesign plSc = pl.shipLab().scoutDesign();
        ShipDesign plSh = pl.shipLab().fighterDesign();
        ShipDesign plBo = pl.shipLab().bomberDesign();
        ShipDesign plCo = pl.shipLab().colonyDesign();
        ShipFleet plFl = sys.orbitingFleetForEmpire(pl);
        if (plFl == null) {
            plFl = new ShipFleet(pl.id, sys);
            sys.acceptFleet(plFl);
        }

        plFl.addShips(plSc.id(), 20);
        plFl.addShips(plSh.id(), 2);
        plFl.addShips(plBo.id(), 2);
        plFl.addShips(plCo.id(), 2);

        ShipSpecial sp1  = pl.shipLab().specialTeleporter();
        ShipSpecial sp2  = pl.shipLab().specialCloak();
        ShipSpecial sp3  = pl.shipLab().specialNamed("Stasis Field");
        ShipSpecial sp4  = pl.shipLab().specialNamed("High Energy Focus");
        ShipSpecial sp5  = pl.shipLab().specialNamed("Ionic Pulsar");
        ShipSpecial sp6  = pl.shipLab().specialNamed("Black Hole Generator");
        ShipSpecial sp7  = pl.shipLab().specialNamed("Warp Dissipator");
        ShipSpecial sp8  = pl.shipLab().specialNamed("Technology Nullifier");
        ShipSpecial sp9  = pl.shipLab().specialNamed("Displacement Device");

        ShipManeuver manv = pl.shipLab().maneuvers().get(pl.shipLab().maneuvers().size()-1);
        ShipWeapon wpn1 = pl.shipLab().beamWeapon(0, true);

        plSc.maneuver(manv);
        plSc.special(0, sp1);
        plSc.special(1, sp7);
        plSc.special(2, sp5);
        pl.shipViewFor(plSc).scan();

        plSh.maneuver(manv);
        plSh.weapon(0,wpn1);
        plSh.special(0,sp2);
        plSh.special(1,sp6);
        plSh.special(2,sp4);
        pl.shipViewFor(plSh).scan();

        plBo.maneuver(manv);
        plBo.special(0,sp3);
        plBo.special(1, sp8);
        plBo.special(2, sp9);
        pl.shipViewFor(plBo).scan();


        ShipDesign enSh = ev.shipLabUncut().fighterDesign();
        ShipDesign enSh2 = ev.shipLabUncut().bomberDesign();
        ShipFleet enFl = new ShipFleet(ev.empId(), sys);
        enFl.addShips(enSh.id(), 100);
        enFl.addShips(enSh2.id(), 30);
        sys.acceptFleet(enFl);
    }
    public void randomlyStealATech() {
        EmpireView view = random(player().empireViews());
        if (view != null) {
            StarSystem espionageSystem = galaxy().system(view.homeSysId());

            List<Tech> techs = new ArrayList<>();
            for (int i=0;i<TechTree.NUM_CATEGORIES;i++)
                techs.add(tech(random(view.techUncut().category(i).allTechs())));
            techs.remove(random(techs)); // one blank category
            Spy spy = (new Spy(view.spies())).makeSuper();
            EspionageMission mission = new EspionageMission(view.spies(), spy, techs,espionageSystem, techs);
            StealTechNotification.create(player().id, mission, view.empId());
            for (EmpireView v: player().empireViews())
                if ((v != null) && (v.empId() != view.empId()))
                    mission.empiresToFrame().add(v.empireUncut());
        }
    }
    public void randomlyCommitSabotage() {
        EmpireView view = random(player().empireViews());
        if (view != null) {
            view.embassy().makeFirstContact();
            Empire emp = galaxy().empire(view.empId());
            StarSystem sys = random(emp.allColonizedSystems());
            player().sv.refreshFullScan(sys.id);
            Spy spy = (new Spy(view.spies())).makeSuper();
            SabotageMission mission = new SabotageMission(view.spies(), spy);
            SabotageNotification.addMission(mission, sys.id);
        }
    }
    public void playerViewAllHomeSystems() {
        // for testing the minimum empire distance code
        for (Empire emp: galaxy().empires()) {
            player().sv.refreshFullScan(emp.homeSysId());
        }
        for (StarSystem sys: galaxy().starSystems()) {
            if (sys.hasMonster())
                player().sv.refreshFullScan(sys.id);
        }
    }
    public void playerViewAllSystems() {
        // for testing the minimum empire distance code
        for (StarSystem sys: galaxy().starSystems()) {
                player().sv.refreshFullScan(sys.id);
        }
    }
    private String nextTurnTitle() {
        if (options().displayYear())
            return text("MAIN_ADVANCING_YEAR", galaxy().currentYear()+1);
        else
            return text("MAIN_ADVANCING_TURN", galaxy().currentTurn()+1);
    }
    private boolean deleteBackupFiles(int keep)	{
        log("Deleting backup files, keep " + keep + " most recent backup");
    	File backupDir	= new File(backupDir());
        boolean hasBackupDir = backupDir.exists() && backupDir.isDirectory();
    	if (!hasBackupDir)
    		return false;
        String ext	= GameSession.SAVEFILE_EXTENSION;
        FilenameFilter filter = (File dir, String name1) -> name1.toLowerCase().endsWith(ext);
        File[] fileList = backupDir.listFiles(filter);
        if (fileList == null || fileList.length <= keep)
        	return false;
        Arrays.sort(fileList, LoadGameUI.FILE_DATE);
        
        File[] toRecycle = Arrays.copyOfRange(fileList, keep, fileList.length);
    	return MoveToTrash.moveToTrash(toRecycle);
    }
    public long saveSession(String filename, boolean backup) throws Exception {
        log("Saving game as file: ", filename, "  backup: "+backup);
    	File theDir = backup ? new File(backupDir()) : new File(saveDir());
        File saveFile = backup ? backupFileNamed(filename) : saveFileNamed(filename);
        Files.createDirectories(theDir.toPath());
        return saveSession(saveFile);
    }
    public long saveSession(File saveFile) throws Exception {
        log("Saving game as file: ", saveFile.getName());
        if (hotSeatState != null) return rotp.multiplayer.hotseat.HotSeatPersistence.save(this, saveFile);
        return writeSessionAtomically(saveFile);
    }
    private long writeSessionAtomically(File saveFile) throws Exception {
        GameSession currSession = GameSession.instance();
		((MOO1GameOptions)currSession.options()).updateVersion();
        Path destination = saveFile.toPath().toAbsolutePath();
        Path temporary = Files.createTempFile(destination.getParent(), "." + saveFile.getName(), ".tmp");
        try {
            byte[] data;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream objOut = new ObjectOutputStream(bos)) {
                objOut.writeObject(currSession);
                objOut.flush();
                data = bos.toByteArray();
            }
            try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(temporary))) {
                out.putNextEntry(new ZipEntry("GameSession.dat"));
                out.write(data);
                out.closeEntry();
                // After the session: the generic loader reads the first entry.
                if (currSession.playByEmail != null) rotp.multiplayer.pbem.BuildStamp.write(out);
            }
            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return data.length;
        }
        finally {
            Files.deleteIfExists(temporary);
        }
    }
	private static void resolveOptionsDiscrepansies(GameSession gs) {
		// resolving AutoPlay potential issues
		String autoPlaySetting = gs.options().selectedAutoplayOption();
		if (!autoPlaySetting.equals(IGameOptions.AUTOPLAY_OFF))
			gs.galaxy.player().changePlayerAI(autoPlaySetting);
	}
    private void loadPreviousSession(GameSession gs, boolean startUp) {
        stopCurrentGame();
        instance(gs);
		// BR: save the last loaded game initial parameters
		instance().options().saveOptionsToFile(GAME_OPTIONS_FILE);

		if (showInfo) 
			showInfo(gs.galaxy());
        startExecutors();
        RotPUI.instance().mainUI().checkMapInitialized();
        if (!startUp) {
            RotPUI.instance().selectMainPanelLoadGame();
        }
        instance().getGovernorOptions().gameLoaded();

        // BR: To fix a previous bug.
        if (instance().aFewMoreTurns() && GameOverUI.gameOverTitleBaseKey().isEmpty())
        	instance().aFewMoreTurns(false);

        if (IDebugOptions.selectedShowVIPPanel())
        	VIPConsole.updateConsole();
    }
	private static void showInfo(Galaxy g)	{ // BR: for debug
		System.out.println("GameSession.showInfo = true ===========================================");
		System.out.println();
		for (Empire emp : g.empires()) {
			int id = emp.homeSysId();
			StarSystem sys = g.system(id);
			Leader boss = emp.leader();
			System.out.println(
					String.format("%-16s", emp.civilizationName())
					+ String.format("%-12s", sys.name())
					+ String.format("%-16s", emp.speciesSkillsName())
					+ String.format("%-12s", boss.personality())
					+ String.format("%-15s", boss.objective())
					+ String.format("%-22s", emp.diplomatAI())
//					+ String.format("ID=" + "%-4s", id)
//					+ String.format("x=" + "%-11s", sys.x())
//					+ String.format("y=" + "%-11s", sys.y())
					+ String.format("AI=" + "%-4s", emp.selectedAI)
					+ emp.getAiName()
					);
		}
		System.out.println();
	}
	public static String saveDir()		{ return IMainOptions.saveDirectoryPath(); }
	public static String backupDir()	{ return IMainOptions.backupDirectoryPath(); }
	public static File recentSaveFile()	{ return new File(saveDir(), GameSession.RECENT_SAVEFILE); }
	public static File saveFileNamed(String fileName)	{ return new File(saveDir(), fileName); }
	public static File backupFileNamed(String fileName)	{ return new File(backupDir(), fileName); }
    private String backupFileName(int num) {
        Empire pl = player();
        String leader = pl.leader().name().replaceAll("\\s", "");
        String race = pl.raceName();
        String gShape = text(options().selectedGalaxyShape()).replaceAll("\\s", "");
        String gSize = text(options().selectedGalaxySize());
        String diff = text(options().selectedGameDifficulty());
        // modnar: add custom difficulty level option, set in Remnants.cfg
        // append this custom difficulty percentage to backup save file name if selected
        if (diff.equals("Custom")) {
            diff = diff + " (" + Integer.toString(options().selectedCustomDifficulty()) + "%)";
        }
        String turn = "T"+pad4.format(num);
        String opp  = "vs"+galaxy().numOpponents();
        String dash = "-";
        return concat(leader,dash,race,dash,gShape,dash,gSize,dash,diff,dash,opp,dash,turn,SAVEFILE_EXTENSION);
    }
    public long saveRecentSession(boolean playerTurn) {
        if (hotSeatState != null && !rotp.multiplayer.hotseat.HotSeatPersistence.canSave(this)) return -1;
    	boolean allowAutoSave = !IDebugOptions.debugNoAutoSave();
    	long ufs = -1;
    	if (allowAutoSave && !playerTurn) // BR: Always keep a copy of starting turn
    		saveRecentStartSession();
        String filename = RECENT_SAVEFILE;
        try {
        	if (allowAutoSave)
        		ufs = saveSession(filename, false);
            if (playerTurn)
               saveBackupSession(galaxy().currentTurn());
        }
        catch(Exception e) {
            err("Error saving: ", filename, " - ", e.getMessage());
            if (playerTurn)
                RotPUI.instance().mainUI().showAutosaveFailedPrompt(e.getMessage());
        }
		return ufs;
    }
    public void saveRecentSession() {
        if (hotSeatState != null && !rotp.multiplayer.hotseat.HotSeatPersistence.canSave(this)) return;
        String filename = RECENT_SAVEFILE;
        try {
            saveSession(filename, false);
        }
        catch(Exception e) {
            err("Error saving: ", filename, " - ", e.getMessage());
        }
    }
    public void saveRecentStartSession() {
        if (hotSeatState != null && !rotp.multiplayer.hotseat.HotSeatPersistence.canSave(this)) return;
        String filename = recentStartSaveFile();
        try {
            saveSession(filename, false);
        }
        catch(Exception e) {
            err("Error saving: ", filename, " - ", e.getMessage());
        }
    }
    public void saveBackupSession(int turn) {
        if (hotSeatState != null && !rotp.multiplayer.hotseat.HotSeatPersistence.canSave(this)) return;
        String filename = "nofile";
        try {
            int backupTurns = UserPreferences.backupTurns();
            if (backupTurns > 0) {
                if ((turn == 1) || (turn % backupTurns == 0)) {
                    filename = backupFileName(turn);
                    saveSession(filename, true);
                    if (options.deleteBackup()) {
                    	int keep = options.backupKeep();
                    	deleteBackupFiles(keep);
                    }
                }
            }
        }
        catch(Exception e) {
            err("Error saving: ", filename, " - ", e.getMessage());
            RotPUI.instance().mainUI().showAutosaveFailedPrompt(e.getMessage());
        }
    }
	public static boolean hasRecentSession()	{
		File f = new File(saveDir(), RECENT_SAVEFILE); // BR: To work on debug too...
        try {
            // InputStream file = new FileInputStream(RECENT_SAVEFILE);
            InputStream file = new FileInputStream(f);
            file.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
	public static boolean hasRecentStartSession()	{
		File f = new File(saveDir(), recentStartSaveFile());
        try {
            InputStream file = new FileInputStream(f);
            file.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
    public void loadLastSavedGame(boolean startUp) {
    	String ext = GameSession.SAVEFILE_EXTENSION;
    	File saveDir = new File(saveDir());
    	FilenameFilter filter = (File dir, String name1) -> name1.toLowerCase().endsWith(ext);
        File[] fileList = saveDir.listFiles(filter);
        String lastSave = "";
        long lastModifiedTime = Long.MIN_VALUE;
        if (fileList != null) {
            for (File file : fileList) {
                if (file.lastModified() > lastModifiedTime) {
                    lastSave = file.getName();
                    lastModifiedTime = file.lastModified();
                }
            }
        }
        loadSession(saveDir(), lastSave, startUp);
    }
    public void loadRecentStartGame(boolean startUp) {
       loadSession(saveDir(), recentStartSaveFile(), startUp);
    }
    public void loadRecentSession(boolean startUp) {
        loadSession(saveDir(), RECENT_SAVEFILE, startUp);
    }
    // BR: added option to restart with new options
    public void loadSession(String dir, String filename, boolean startUp) {
        GameSession previousSession = instance();
        boolean previousAutoRunning = autoRunning;
        boolean previousIronmanLocked = ironmanLocked;
        int previousPlayerId = Empire.PLAYER_ID;
        Rand previousRandom = Rotp.rand();
        GameSession newSession = null;
        try {
            log("Loading game from file: ", filename);
            File saveFile = dir.isEmpty() ? new File(filename) : new File(dir, filename);
            try { rotp.multiplayer.pbem.BuildStamp.requireMatch(saveFile); }
            catch (rotp.multiplayer.pbem.BuildStamp.MismatchException mismatch) {
                throw new RuntimeException(text("PBEM_BUILD_MISMATCH", mismatch.fileBuild, mismatch.localBuild));
            }
            // assume the file is not zipped, load it directly
            try (InputStream file = new FileInputStream(saveFile)) {
                newSession = loadObjectData(file);
            }

            // if newSession is null, see if it is zipped
            if (newSession == null) {
                try (ZipFile zipFile = new ZipFile(saveFile)) {
                    ZipEntry ze = zipFile.entries().nextElement();
                    InputStream zis = zipFile.getInputStream(ze);
                    newSession = loadObjectData(zis);
					if (newSession == null) {
						zis.close();
						throw new RuntimeException(text("LOAD_GAME_BAD_VERSION", filename));
					}
					zis.close();
                }
            }

            if (newSession.hotSeatState != null || newSession.hotSeatSetup != null) {
                try { rotp.multiplayer.hotseat.HotSeatPersistence.load(saveFile); }
                catch (Exception failure) { throw new IOException("Unable to restore hot-seat game", failure); }
                return;
            }
			GameSession.instance(newSession);
			instance().loading = true;
			int oldChanges = newSession.galaxy().player().budget().getChanges();

			if (Rotp.isIDE()) {
				if (newSession.governorOptions == null)
					System.err.println("@ newSession.governorOptions == null ==> Not RotP-Fusion");
				DynOptions dynOpts = newSession.options().dynOpts();
				if (dynOpts == null)
					System.err.println("@  newSession.options.dynOpts() == null ==> Not RotP-Fusion");
				else {
					String mouseSens = dynOpts.getString("GAME_SETTINGS_SENSITIVITY", "Not available");
					System.out.println("@ mouse Sensitivity = " + mouseSens);
					String graphics = dynOpts.getString("GAME_SETTINGS_GRAPHICS", "Not available");
					System.out.println("@ Graphics Level = " + graphics);
				}
				String version = ((MOO1GameOptions)newSession.options).getVersion();
				System.out.println("@ Version = " + version);
			}
			((MOO1GameOptions)newSession.options()).validateOnLoad(); // In case of non Fusion version
			rulesetManager().setAsGameMode();

			// BR: save the last loaded game initial parameters
			instance().options().setAsGame();
			resolveOptionsDiscrepansies(newSession);
			rulesetManager().setAsGameMode();
//			instance().loading = false;

			if (instance().galaxy.playerSwapRequest())
				instance().galaxy.swapPlayerEmpire();
            newSession.validateOnLoadOnly();
            newSession.validate();

            newSession.ironmanValidation();
            loadPreviousSession(newSession, startUp);
			newSession.galaxy().player().budget().setChanges(oldChanges);
			instance().loading = false;

        	if (!IDebugOptions.debugNoAutoSave()) {
                // do not autosave the current session if that is the file we are trying to reload            
                if (!filename.equalsIgnoreCase(RECENT_SAVEFILE))
                    saveRecentSession();
                else
                	saveRecentStartSession(); // BR: to keep a copy of the beginning of the turn
        	}
        }
        catch(IOException e) {
            throw new RuntimeException(text("LOAD_GAME_BAD_VERSION", filename));
        }
        catch (RuntimeException | Error e) {
            instance(previousSession);
            autoRunning = previousAutoRunning;
            ironmanLocked = previousIronmanLocked;
            Empire.updatePlayerId(previousPlayerId);
            Rotp.rand(previousRandom);
            if (newSession != null)
                newSession.loading = false;
            throw e;
        }

    }
    // BR: For restarting with new options
	public static void loadSession(GameSession newSession)	{
        GameSession previousSession = instance();
        boolean previousAutoRunning = autoRunning;
        GameSession.instance(newSession);
        try {
            newSession.validate();
            newSession.validateOnLoadOnly();
        }
        catch (RuntimeException | Error e) {
            GameSession.instance(previousSession);
            autoRunning = previousAutoRunning;
            throw e;
        }
    }
	private static GameSession loadObjectData(InputStream is)	{
        try {
            GameSession newSession;
            try (InputStream buffer = new BufferedInputStream(is)) {
                ObjectInput input = new ObjectInputStream(buffer);
                newSession = (GameSession) input.readObject();
            }
            return newSession;
        }
        catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
    private void validate() {
        galaxy().validate();
    }
    private void ironmanValidation() {
    	ironmanLocked = false;
        if (options().selectedIronmanLoad()) {
        	int turn = galaxy().currentTurn();
	        int modulo = Math.floorMod(turn, options.selectedIronmanLoadDelay());
	        ironmanLocked = (modulo != 0) && (turn > 1);
        }
    }
    private void validateOnLoadOnly() {
    	autoRunning = false;
        GNNExpansionEvent.instance().validate(galaxy());

        // check for invalid colonies with too much waste & negative pop
        for (StarSystem sys: galaxy().starSystems()) {
            if (sys.isColonized())
                sys.colony().validateOnLoad();
        }
        // check for council last-vote init issue
        boolean allVotedOnlyForPlayer = true;
        for (Empire emp: galaxy().empires()) {
        	emp.validateOnLoad();
            if (emp.lastCouncilVoteEmpId() != 0)
                allVotedOnlyForPlayer = false;
        }

        if (allVotedOnlyForPlayer) {
            for (Empire emp: galaxy().empires())
                emp.lastCouncilVoteEmpId(Empire.NULL_ID);
        }

        Galaxy gal = this.galaxy();
        Empire pl = player();
        pl.setEmpireMapAvgCoordinates();

        float minX = gal.width();
        float minY = gal.height();
        float maxX = 0;
        float maxY = 0;

        List<StarSystem> alliedSystems = pl.allColonizedSystems();
        for (StarSystem sys : alliedSystems) {
            minX = min(minX,sys.x());
            maxX = max(maxX,sys.x());
            minY = min(minY,sys.y());
            maxY = max(maxY,sys.y());
        }
        float r = pl.scoutReach(6);
        minX = max(0,minX-r);
        maxX = min(gal.width(), maxX+r);
        minY = max(0,minY-r);
        maxY = min(gal.height(), maxY+r);
        pl.setBounds(minX, maxX, minY, maxY);
        pl.setVisibleShips(true);
        // BR: Backward compatibility tentative
        galaxy().validateOnLoad();
        ((MOO1GameOptions) options).validateOnLoad();
        if (IDebugOptions.selectedShowVIPPanel())
        	VIPConsole.updateConsole();
        if (IDebugOptions.debugShowMoreMemory()) {
            memLog();
            // RotPUI.instance().mainUI().showMemoryLowPrompt(); // TO DO BR: Comment
        }
        EmpireColonySpendingPane.resetPanel();
        MultiColonySpendingPane.resetPanel();

        //debugAddOn(); // TO DO BR: Comment
    }
    static ThreadFactory minThreadFactory() {
        return (Runnable r) -> {
            Thread t = new Thread(r, "minThreadFactory");
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        };
    }

    private GovernorOptions governorOptions = new GovernorOptions();

    public GovernorOptions getGovernorOptions() {
        // can happen on deserialized stock save game
        if (governorOptions == null) {
            governorOptions = new GovernorOptions();
        }
        return governorOptions;
    }
}
