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
package rotp.ui;

import static rotp.model.game.IDebugOptions.AUTORUN_BENCHMARK;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import rotp.Rotp;
import rotp.model.colony.Colony;
import rotp.model.combat.ShipCombatManager;
import rotp.model.empires.Empire;
import rotp.model.empires.EspionageMission;
import rotp.model.empires.SabotageMission;
import rotp.model.empires.species.DNAWorkshop;
import rotp.model.galaxy.GalaxyFactory.GalaxyCopy;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.Transport;
import rotp.model.game.GovernorOptions;
import rotp.model.game.IDebugOptions;
import rotp.model.game.IGalaxyOptions.ListShapeParam;
import rotp.model.game.IGameOptions;
import rotp.model.game.MOO1GameOptions;
import rotp.model.game.SafeListParam;
import rotp.model.ships.ShipDesign;
import rotp.model.tech.TechCategory;
import rotp.ui.combat.ShipBattleUI;
import rotp.ui.design.DesignUI;
import rotp.ui.diplomacy.DiplomacyRequestReply;
import rotp.ui.fleets.FleetUI;
import rotp.ui.game.AdvancedOptionsUI;
import rotp.ui.game.BaseCompactOptionsUI;
import rotp.ui.game.GameOverUI;
import rotp.ui.game.GameUI;
import rotp.ui.game.HelpUI;
import rotp.ui.game.LoadGameUI;
import rotp.ui.game.MainOptionsUI;
import rotp.ui.game.RaceIntroUI;
import rotp.ui.game.SaveGameUI;
import rotp.ui.game.SetupGalaxyUI;
import rotp.ui.game.SetupRaceUI;
import rotp.ui.history.HistoryUI;
import rotp.ui.main.MainUI;
import rotp.ui.map.SystemsUI;
import rotp.ui.notifications.DiplomaticNotification;
import rotp.ui.notifications.TurnNotification;
import rotp.ui.options.AllSubUI;
import rotp.ui.options.ISubUiKeys;
import rotp.ui.planets.ColonizePlanetUI;
import rotp.ui.planets.ColonyViewUI;
import rotp.ui.planets.GroundBattleUI;
import rotp.ui.planets.PlanetsUI;
import rotp.ui.races.RacesUI;
import rotp.ui.races.SabotageUI;
import rotp.ui.tech.AllocateTechUI;
import rotp.ui.tech.DiplomaticMessageUI;
import rotp.ui.tech.DiscoverTechUI;
import rotp.ui.tech.SelectNewTechUI;
import rotp.ui.util.IParam;
import rotp.ui.util.ListDialogUI;
import rotp.ui.util.StringDialogUI;
import rotp.ui.vipconsole.VIPConsole;
import rotp.util.AnimationManager;
import rotp.util.sound.SoundManager;

public final class RotPUI extends BasePanel implements ActionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private static int FPS = 10;
    private static int ANIMATION_TIMER = 100;
    private boolean drawNextTurnNotice = true;

    public static boolean isVIPConsole = false; // BR: to avoid complex call on error!
    public static boolean useDebugFile = false;

	public static final List<ActionListener> animationListeners = new ArrayList<>();
    private static final String SETUP_RACE_PANEL = "SetupRace";
    private static final String SETUP_GALAXY_PANEL = "SetupGalaxy";
    private static final String LOAD_PANEL = "Load";
    private static final String SAVE_PANEL = "Save";
    private static final String INTRO_PANEL = "Intro";
    private static final String MAIN_PANEL = "Main";
    private static final String GAME_PANEL = "Game";
    private static final String DESIGN_PANEL = "Design";
    private static final String FLEET_PANEL = "Fleet";
    private static final String SYSTEMS_PANEL = "Systems";
    private static final String RACES_PANEL = "Races";
    private static final String PLANETS_PANEL = "Planets";
    private static final String TECH_PANEL = "Tech";
    private static final String SELECT_NEW_TECH_PANEL = "NewTech";
    private static final String DISCOVER_TECH_PANEL = "DiscoverTech";
    private static final String COLONIZE_PROMPT_PANEL = "PromptColonize";
    private static final String COLONY_VIEW_PANEL = "ColonyView";
    private static final String DIPLOMATIC_MESSAGE_PANEL = "DiplomaticMessage";
    private static final String SHIP_BATTLE_PANEL = "ShipBattle";
    private static final String GROUND_BATTLE_PANEL = "GroundBattle";
    private static final String SABOTAGE_PANEL = "Sabotage";
    private static final String GNN_PANEL = "GNN";
    private static final String COUNCIL_PANEL = "GalacticCouncil";
    private static final String GAME_OVER_PANEL = "GameOver,Man,GameOver";
    private static final String DNA_WORKSHOP_PANEL = "DNAWorkshop";
	// private static final String CREDITS_PANEL = "Credits";
    private static final String ERROR_PANEL = "Error";
    private static final String DIALOG_PANEL = "Dialog";
	private static RotPUI instance;
    private static PrintWriter debugFile = null;

    static void fps(int fps) {
        // bound arg between 10 & 60
        int actualFPS = Math.min(60, Math.max(10,fps));
        if (FPS == actualFPS)
            return;

        FPS = actualFPS;
        ANIMATION_TIMER = 1000/FPS;
        instance.resetTimer();
    }
    public static int scaledSize(int i) {
        if (i < 1)
            return (int) Math.ceil(Rotp.resizeAmt()*i);
        else if (i > 1)
            return (int) Math.floor(Rotp.resizeAmt()*i);
        else
            return i;
    }
    public static int unscaledSize(int i) {
        return (int) Math.max(0, Math.ceil(i/Rotp.resizeAmt()));
    }
    public static PrintWriter debugFile() {
        if (!useDebugFile)
            return null;

        if (debugFile == null) {
            try {
                FileOutputStream fout = new FileOutputStream(new File("rotp_log.txt"));
                debugFile = new PrintWriter(fout, true);
            }
            catch (FileNotFoundException e) {
                System.err.println("RotpUI.static<> -- Unable to open debug file:  FileNotFoundException: " + e);
            }
        }
        return debugFile;
    }

    private final GameUI gameUI = new GameUI();
    private final LoadGameUI loadGameUI = new LoadGameUI();
    private final SaveGameUI saveGameUI = new SaveGameUI();
    private final SetupRaceUI setupRaceUI = new SetupRaceUI();
    private final SetupGalaxyUI setupGalaxyUI = new SetupGalaxyUI();
    private final RaceIntroUI raceIntroUI = new RaceIntroUI();
    private MainUI mainUI;
    private final DesignUI designUI = new DesignUI();
    private final FleetUI fleetUI = new FleetUI();
    private final SystemsUI systemsUI = new SystemsUI();
    private final RacesUI racesUI = new RacesUI();
    private final PlanetsUI planetsUI = new PlanetsUI();
    private final AllocateTechUI allocateTechUI = new AllocateTechUI();
    private final SelectNewTechUI selectNewTechUI = new SelectNewTechUI();
    private final DiscoverTechUI discoverTechUI = new DiscoverTechUI();
    private final ShipBattleUI shipBattleUI = new ShipBattleUI();
    private final GroundBattleUI groundBattleUI = new GroundBattleUI();
    private final SabotageUI sabotageUI = new SabotageUI();
    private final HistoryUI historyUI = new HistoryUI();
    private final GNNUI gnnUI = new GNNUI();
    private final ColonizePlanetUI colonizePlanetUI = new ColonizePlanetUI();
    private final ColonyViewUI colonyViewUI = new ColonyViewUI();
    private final DiplomaticMessageUI diplomaticMessageUI = new DiplomaticMessageUI();
    private final GalacticCouncilUI galacticCouncilUI = new GalacticCouncilUI();
    private final GameOverUI gameOverUI = new GameOverUI();
    private final ErrorUI errorUI = new ErrorUI();
    private final HelpUI helpUI = new HelpUI();
    private final MainOptionsUI mainOptionsUI = new MainOptionsUI();
	private StringDialogUI	stringDialog;
	private ListDialogUI	listDialog;
	private final List<BaseCompactOptionsUI> optionsPanels = new ArrayList<>();
	private final DNAWorkshop dnaWorkshopUI = new DNAWorkshop();

    private final AdvancedOptionsUI advancedOptionsUI = new AdvancedOptionsUI();
    private final LargeDialogPane  dialogPane       = new LargeDialogPane();

    private final CardLayout layout = new CardLayout();
    private String currentPane = GAME_PANEL;
    private BasePanel selectedPanel;

    private int optionPanelId = -1;
    private Timer timer;
    private int animationCount = 0;
    private long animationMs = 0;
    public MainUI mainUI() {
        if (mainUI == null)
            mainUI = new MainUI();
        return mainUI;
    }
    public static RaceIntroUI raceIntroUI()  { return instance.raceIntroUI; }
    public static GameUI gameUI()	  { return instance.gameUI; }
    public AllocateTechUI techUI()    { return allocateTechUI; }
    public RacesUI racesUI()          { return racesUI; }
    @Override
    public int animationCount()     { return animationCount; }
    @Override
    public long animationMs()       { return animationMs; }

    public RotPUI() {
		// Frame Name can not be changed later...
		// Children UI will not recognize their parent
		String newFrameTitle = text("GAME_TITLE_FRAME");
		Rotp.getFrame().setTitle(newFrameTitle);
        timer = new Timer(ANIMATION_TIMER, this);
        instance = this;
    }
	// public void clearAdvice()	{ RotPUI.this.mainUI().clearAdvice(); }
    public void processNotifications(List<TurnNotification> notifications) {
    	if (IDebugOptions.debugAutoRun()) {
    		if (IDebugOptions.debugLogNotif()) {
    			for (TurnNotification tn: notifications) {
    				writeToFile( IGameOptions.NOTIF_LOGFILE,
    						concat(getTurn(), " | ", tn.toString()),
    						true, galaxy().currentTurn() > 1);
    			}
        	}
    		return;
    	}
    	else
    		for (TurnNotification tn: notifications)
    			processNotification(tn);
    }
    public void processNotification(TurnNotification tn) {
        try {
            drawNextTurnNotice = false;
            tn.notifyPlayer();
        } finally {
            drawNextTurnNotice = true;
        }
    }

    private void resetTimer() {
        if (timer != null)
            timer.stop();
        timer = new Timer(ANIMATION_TIMER, this);
        timer.start();
    }
    public void init() {
		selectGamePanel();
        addKeyListener(this);
        if (Rotp.startupException != null)
            selectErrorPanel(Rotp.startupException);
        else
            selectCurrentPanel();

        timer.start();
		//toggleAnimations();
		stringDialog = new StringDialogUI(Rotp.getFrame());
		listDialog	 = new ListDialogUI(Rotp.getFrame());
        repaint();
    }
    public void toggleAnimations() { // Keep for debug
        if (playAnimations())
            timer.start();
        else
            timer.stop();
    }
	public static RotPUI instance() { return instance; }
    public static HelpUI helpUI()   { return instance.helpUI; }

	public static BaseCompactOptionsUI getOptionPanel()	{ return instance.nextOptionPanel(); }
	public static void releaseOptionPanel()				{ instance.optionPanelId--; }
	public static BaseCompactOptionsUI	setupUI()		{
		BaseCompactOptionsUI ui = getOptionPanel();
		ui.initUI("SETTINGS_MOD_STATIC_TITLE", ISubUiKeys.SETUP_PARAMETERS_UI_KEY);
		return ui;
	}
	public static BaseCompactOptionsUI	rulesUI()		{
		BaseCompactOptionsUI ui = getOptionPanel();
		ui.initUI("SETTINGS_MOD_DYNAMIC_TITLE", ISubUiKeys.RULES_OPTIONS_UI_KEY);
		return ui;
	}
	public static AdvancedOptionsUI	advancedOptionsUI()	{ return instance.advancedOptionsUI; }
	public static MainOptionsUI		mainOptionsUI()		{ return instance.mainOptionsUI; }
	public static SetupRaceUI		setupRaceUI()		{ return instance.setupRaceUI; }
	public static SetupGalaxyUI		setupGalaxyUI()		{ return instance.setupGalaxyUI; }
	public ListDialogUI	listDialog()					{
		if (listDialog == null)
			listDialog = new ListDialogUI(Rotp.getFrame());
		return listDialog;
	}
	public StringDialogUI	stringDialog()				{
		if (stringDialog == null)
			stringDialog = new StringDialogUI(Rotp.getFrame());
		return stringDialog;
	}
	public void resetStringDialog()						{ stringDialog = null; }
	public void resetListDialog()						{ listDialog = null; }
	private BaseCompactOptionsUI nextOptionPanel()		{
		optionPanelId ++;
		if (optionsPanels.size() == optionPanelId)
			optionsPanels.add(new BaseCompactOptionsUI());
		return optionsPanels.get(optionPanelId);
	}

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (drawNextTurnNotice && session().performingTurn()) {
            drawNotice(g, 28, -s100);
        }
        requestFocusInWindow();
    }
    /* public void repaintNotice() {
        int w0 = scaled(500);
        int h0 = s100;
        int x0 = (getWidth()-w0)/2;
        int y0 = (getHeight()-h0)/2;
        repaint(x0,y0,w0,h0);
    } */
    private void selectCurrentPanel() { selectPanel(currentPane, selectedPanel); }

	// PLAYER-TRIGGERED ACTIONS
	public	void refreshShapeOptions(ListShapeParam optionsList)	{
		setupGalaxyUI.refreshShapeOptions(optionsList);
		if (optionPanelId < 0)
			return;
		optionsPanels.get(optionPanelId).reloadUI();
	}

	public void selectSetupRacePanel(String leaderName)	{
		setupRaceUI.init(leaderName);
		selectPanel(SETUP_RACE_PANEL, setupGalaxyUI);
	}
    public void selectSetupRacePanel()	 {
    	setupRaceUI.init();
    	selectPanel(SETUP_RACE_PANEL, setupRaceUI); 
    }
	 public void selectDNAWorkshopPanel(BasePanel parent, boolean allowEdit) {
		dnaWorkshopUI.init(parent, allowEdit);
		selectPanel(DNA_WORKSHOP_PANEL, dnaWorkshopUI); 
	}
   public void selectSetupGalaxyPanel() {
    	setupGalaxyUI.init();
    	selectPanel(SETUP_GALAXY_PANEL, setupGalaxyUI);
    }
    public void selectLoadGamePanel() {
		if (IDebugOptions.selectedShowVIPPanel() && !Rotp.isIDE()) {
			VIPConsole.loadMenu.open("");
		}
		else {
			loadGameUI.init();
			selectPanel(LOAD_PANEL, loadGameUI);
		}
    }
    // BR: for restarting with new options
    public void selectRestartGamePanel(GalaxyCopy oldGalaxy) {
    	loadGameUI.init(oldGalaxy);
    	selectPanel(LOAD_PANEL, loadGameUI);
    }
    public void selectSaveGamePanel()	{ saveGameUI.init(); selectPanel(SAVE_PANEL, saveGameUI);  }
    public void selectIntroPanel() {
        mainUI.init(false);
        selectPanel(MAIN_PANEL, mainUI());
        enableGlassPane(raceIntroUI);
        repaint();
        GovernorOptions.callForReset();
		if (IDebugOptions.selectedShowVIPPanel())
			VIPConsole.introMenu.open("");

		else if (IDebugOptions.debugBenchmark()) {
    		raceIntroUI.finish();
    		repaint();
    		return;
    	}
    }
    public void selectMainPanel()      { selectMainPanel(false); }
    public void selectMainPanel(boolean pauseNextTurn)      {
        disableGlassPane();
        if (!session().status().inProgress()) {
            selectGameOverPanel();
            return;
        }
        mainUI.init(pauseNextTurn);
        selectPanel(MAIN_PANEL, mainUI());
        repaint();
    }
    public void selectMainPanelLoadGame() {
        disableGlassPane();
        if (!session().status().inProgress()) {
            selectGameOverPanel();
            return;
        }
        mainUI.clearOverlay();
        mainUI.init(false);
        mainUI.showDisplayPanel();
        selectPanel(MAIN_PANEL, mainUI());
        repaint();
        GovernorOptions.callForReset();
    }
    public void selectMainPanelNewGame()      {
        disableGlassPane();
        mainUI.clearOverlay();
        mainUI.init(true);
        // modnar: don't show help on new game // BR: Conditional
        if (!options().isAutoPlay() && !options().isAutoHelpDisabled()) {
            mainUI.showHelp();
            options().autoHelpHasBeenShown();
        }
        mainUI.showDisplayPanel();
        selectPanel(MAIN_PANEL, mainUI());
        repaint();
        GovernorOptions.callForReset();
        if (IDebugOptions.debugBenchmark()) {
        	mainUI.map().initBenchmark();
        	mainUI.repaintAllImmediately();
        	final Runnable save = () -> {
        		handleNextTurn();
            	session().nextTurn();
        	};
        	SwingUtilities.invokeLater(save);
        }
    }
    public void selectGamePanel()      {
        gameUI.init();
        if (!UserPreferences.windowed())
            selectDialogPanel(GAME_PANEL, gameUI);
        else
            selectPanel(GAME_PANEL, gameUI);
    }
    public void selectDesignPanel()    { designUI.init(); selectPanel(DESIGN_PANEL, designUI); }
    public void selectFleetPanel()     { fleetUI.init(); selectPanel(FLEET_PANEL, fleetUI); }
    public void selectSystemsPanel()   { systemsUI.init(); selectPanel(SYSTEMS_PANEL, systemsUI); }
    public void selectRacesPanel()     { racesUI.init(); selectPanel(RACES_PANEL, racesUI); }
    public void selectPlanetsPanel()   { planetsUI.init(); selectPanel(PLANETS_PANEL, planetsUI); }
    public void selectTechPanel()      { allocateTechUI.init(); selectPanel(TECH_PANEL, allocateTechUI); }
    public void selectTechPanel(int r) { allocateTechUI.init(r); selectPanel(TECH_PANEL, allocateTechUI); }
    public void selectCouncilPanel()   {
    	if (IDebugOptions.debugAutoRun()) {
    		galacticCouncilUI.autoRun();
    		return;
    	}  	
        session().pauseNextTurnProcessing("Show Council");
        galacticCouncilUI.init();
        if (!UserPreferences.windowed())
            selectDialogPanel(COUNCIL_PANEL, galacticCouncilUI);
        else
            selectPanel(COUNCIL_PANEL, galacticCouncilUI);
        session().waitUntilNextTurnCanProceed();
    }
	public void paintCouncilNotice(boolean shading)  {
		//galacticCouncilUI.repaint();
		Graphics g = galacticCouncilUI.getGraphics();
		drawNotice(g, 28, -s100, shading);
		g.dispose();
	}
    private void benchmarkGameOver()  {
    	List<Empire> sortedEmpires = new ArrayList<>();
    	sortedEmpires.addAll(Arrays.asList(galaxy().empires()));
        for (Empire emp: sortedEmpires)
            emp.setBenchmark();
        Collections.sort(sortedEmpires, Empire.BENCHMARK);
        Empire allyLeader  = galaxy().council().leader();
        Empire rebelLeader = galaxy().council().rebelLeader();
        boolean hasLeader  = allyLeader != null;
        String out = "";
        // In case of Diplomatic win, the winner may not be the most powerful!
        // Then move it back on the top of the list!
        if (hasLeader) {
        	if (!allyLeader.extinct()) {
            	sortedEmpires.remove(allyLeader);
            	sortedEmpires.add(0, allyLeader);
            	out = "Diplomatic:";
            	// System.out.println("Diplomatic win Ally");
            }
        	else if (!rebelLeader.extinct()) {
            	sortedEmpires.remove(rebelLeader);
            	sortedEmpires.add(0, rebelLeader);
            	out = "Rebelion:";
            	// System.out.println("Diplomatic win Rebel");
            }
        }
        else {
        	out = "Military:";
        	// System.out.println("Military win");
        }

        String sep   = "	"; // Tab
        String aiKey = options().selectedAutoplayOption();
        out += galaxy().currentTurn() + sep + "Player " + text(aiKey);
        // out += "Turn:" + galaxy().currentTurn() + sep + "Player " + text(aiKey);
        for (Empire emp: sortedEmpires) {
        	out   += sep + emp.raceType() + ": " + emp.benchmark();
        }
        for (Empire emp: sortedEmpires) {
        	aiKey = emp.getAiKey();
        	String aiName = text(aiKey).replace("AI: ", "");
        	out   += sep + aiName;
        }
		IGameOptions.showGuide.set(false);
    	selectGamePanel();
        writeToFile(AUTORUN_BENCHMARK, out, true, true);        	
		selectSetupRacePanel();
		selectSetupGalaxyPanel();
		setupGalaxyUI().startGame();
    }
    public void selectGameOverPanel()  {
    	if (IDebugOptions.debugBenchmark()) {
    		benchmarkGameOver();
    		return;
    	}  	
        gameOverUI.init();
        if (!UserPreferences.windowed())
            selectDialogPanel(GAME_OVER_PANEL, gameOverUI);
        else
            selectPanel(GAME_OVER_PANEL, gameOverUI);
    }
    public void selectErrorPanel(Throwable e)  {
        // ignore low-level mp3 errors from the Java FX library
        for (StackTraceElement line : e.getStackTrace()) {
            if (line.toString().contains("com.sun.media.jfxmediaimpl.NativeMediaPlayer.sendWarning")) {
                err("IGNORED JAVA MEDIA WARNING: ");
                e.printStackTrace();
                return;
            }
            if (line.toString().contains("com.sun.media.sound.DirectAudioDevice")) {
                err("IGNORED JAVA MEDIA WARNING: ");
                e.printStackTrace();
                SoundManager.loadSounds();
                return;
            }
        }
        //e.printStackTrace();
        if (isVIPConsole) {
        	VIPConsole.throwError(e);
        }
        else {
        	errorUI.init(e);
            selectPanel(ERROR_PANEL, errorUI);
        }
    }
    public void selectGNNPanel(String title, String id, List<Empire> empires) {
        session().pauseNextTurnProcessing("Show GNN");
        gnnUI.init(title, id, empires);
        if (!UserPreferences.windowed())
            selectDialogPanel(GNN_PANEL, gnnUI);
        else
            selectPanel(GNN_PANEL, gnnUI);
        session().waitUntilNextTurnCanProceed();
    }
    public void selectSabotagePanel(SabotageMission m, int sysId) {
        session().pauseNextTurnProcessing("Show Sabotage");
        sabotageUI.init(m, sysId);
        selectPanel(SABOTAGE_PANEL, sabotageUI);
        session().waitUntilNextTurnCanProceed();
    }
    public void selectHistoryPanel(int empId, boolean showAll) {
        historyUI.init(empId, showAll);
        enableGlassPane(historyUI);
    }
    public void selectGroundBattlePanel(Colony c, Transport tr) {
        session().pauseNextTurnProcessing("Show Ground Battle");
        groundBattleUI.init(c, tr);
        if (!UserPreferences.windowed())
            selectDialogPanel(GROUND_BATTLE_PANEL, groundBattleUI);
        else
            selectPanel(GROUND_BATTLE_PANEL, groundBattleUI);
        session().waitUntilNextTurnCanProceed();
    }
    public void showAdvice(String key, Empire emp1, String var1, String var2, String var3) {
        if(!UserPreferences.disableAdvisor() && !isVIPConsole)
        {
            mainUI().showAdvice(key, emp1, var1, var2, var3);
            selectMainPanel();
        }
    }
    public void showBombardmentNotice(int sysId, ShipFleet fl) {
        try {
            drawNextTurnNotice = false;
            session().pauseNextTurnProcessing("Show Bombard Notice");
            mainUI().showBombardmentNotice(sysId, fl);
            selectMainPanel();
            session().waitUntilNextTurnCanProceed();
        } finally {
            drawNextTurnNotice = true;
        }
    }
    public void promptForBombardment(int sysId, ShipFleet fl) {
        try {
            drawNextTurnNotice = false;
            session().pauseNextTurnProcessing("Show Bombard Prompt");
            mainUI().showBombardmentPrompt(sysId, fl);
            selectMainPanel();
            session().waitUntilNextTurnCanProceed();
        } finally {
            drawNextTurnNotice = true;
        }
    }
    public void promptForShipCombat(ShipCombatManager mgr) {
        try {
            drawNextTurnNotice = false;
            session().pauseNextTurnProcessing("Show Ship Combat Prompt");
            mainUI().showShipCombatPrompt(mgr);
            selectMainPanel();
            session().waitUntilNextTurnCanProceed();
        } finally {
            drawNextTurnNotice = true;
        }
    }
    public void selectShipBattlePanel(ShipCombatManager mgr, int combatFlag) {
        boolean showBattle = shipBattleUI.init(mgr, combatFlag);
        if (showBattle)
            selectPanel(SHIP_BATTLE_PANEL, shipBattleUI);
        else
            session().resumeNextTurnProcessing();
    }
    public void promptForColonization(int sysId, ShipFleet fl, ShipDesign d) {
        try {
            drawNextTurnNotice = false;
            session().pauseNextTurnProcessing("Show Colonize Prompt");
            mainUI().showColonizationPrompt(sysId, fl, d);
            selectMainPanel();
            session().waitUntilNextTurnCanProceed();
        } finally {
            drawNextTurnNotice = true;
        }
    }
    public void selectColonizationPanel(int sysId, ShipFleet fl, ShipDesign d) {
        colonizePlanetUI.init(sysId, fl, d);
        if (!UserPreferences.windowed())
            selectDialogPanel(COLONIZE_PROMPT_PANEL, colonizePlanetUI);
        else
            selectPanel(COLONIZE_PROMPT_PANEL, colonizePlanetUI);
    }
    public void selectColonyViewPanel(int sysId) {
    	colonyViewUI.init(sysId);
        if (!UserPreferences.windowed())
            selectDialogPanel(COLONY_VIEW_PANEL, colonyViewUI);
        else
            selectPanel(COLONY_VIEW_PANEL, colonyViewUI);
    }
    public void selectSelectNewTechPanel(TechCategory cat) {
        session().pauseNextTurnProcessing("Show Select Tech");
        selectNewTechUI.category(cat);
        if (!UserPreferences.windowed())
            selectDialogPanel(SELECT_NEW_TECH_PANEL, selectNewTechUI);
        else
            selectPanel(SELECT_NEW_TECH_PANEL, selectNewTechUI);
        session().waitUntilNextTurnCanProceed();
    }
    public void selectPlunderShipTechPanel(String techId, int empId) {
        session().pauseNextTurnProcessing("Show Plunder Tech");
        discoverTechUI.plunderShipTech(techId, empId);
        if (!UserPreferences.windowed())
            selectDialogPanel(DISCOVER_TECH_PANEL, discoverTechUI);
        else
            selectPanel(DISCOVER_TECH_PANEL, discoverTechUI);
        session().waitUntilNextTurnCanProceed();
    }
    public void selectPlunderTechPanel(String techId, int sysId, int empId) {
        session().pauseNextTurnProcessing("Show Plunder Tech");
        discoverTechUI.plunderTech(techId, sysId, empId);
        if (!UserPreferences.windowed())
            selectDialogPanel(DISCOVER_TECH_PANEL, discoverTechUI);
        else
            selectPanel(DISCOVER_TECH_PANEL, discoverTechUI);
        session().waitUntilNextTurnCanProceed();
    }
    public void selectDiscoverTechPanel(String techId) {
        session().pauseNextTurnProcessing("Show Discover Tech");
        discoverTechUI.discoverTech(techId);
        if (!UserPreferences.windowed())
            selectDialogPanel(DISCOVER_TECH_PANEL, discoverTechUI);
        else
            selectPanel(DISCOVER_TECH_PANEL, discoverTechUI);
        session().waitUntilNextTurnCanProceed();
    }
    public void selectTradeTechPanel(String techId, int empId) {
        session().pauseNextTurnProcessing("Show Trade Tech");
        discoverTechUI.tradeTech(techId, empId);
        if (!UserPreferences.windowed())
            selectDialogPanel(DISCOVER_TECH_PANEL, discoverTechUI);
        else
            selectPanel(DISCOVER_TECH_PANEL, discoverTechUI);
        session().waitUntilNextTurnCanProceed();
    }
    public void selectStealTechPanel(EspionageMission mission, int empId) {
        discoverTechUI.stealTech(mission, empId);
        if (!UserPreferences.windowed())
            selectDialogPanel(DISCOVER_TECH_PANEL, discoverTechUI);
        else
            selectPanel(DISCOVER_TECH_PANEL, discoverTechUI);
    }
    public void selectEspionageMissionPanel(EspionageMission mission, int empId) {
       try {
            drawNextTurnNotice = false;
            session().pauseNextTurnProcessing("Show Espionage");
            log("==MAIN UI==   espionage mission");
            mainUI().showEspionageMission(mission, empId);
            selectMainPanel();
            session().waitUntilNextTurnCanProceed();
        } finally {
            drawNextTurnNotice = true;
        }
    }

    public void selectDiplomaticMessagePanel(DiplomaticNotification notif) {
        session().pauseNextTurnProcessing("Show Diplomatic Message");
        log("==MAIN UI==   selectDiplomaticMessagePanel");
        if (!diplomaticMessageUI.init(notif)) { // BR: to skip Offer Trade by Empire now at war
        	// System.out.println("Skipped selectDiplomaticMessagePanel");
        	return;
        }
        if (!UserPreferences.windowed())
            selectDialogPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
        else
            selectPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
        session().waitUntilNextTurnCanProceed();
    }
    public void selectDiplomaticDialoguePanel(DiplomaticNotification notif) {
        log("==MAIN UI==   selectDiplomaticDialoguePanel");
        if (!diplomaticMessageUI.init(notif)) {
        	// System.out.println("Skipped selectDiplomaticDialoguePanel");
        	return;
        };
        diplomaticMessageUI.endFade();
        if (!UserPreferences.windowed())
            selectDialogPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
        else
            selectPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
    }
    public void selectDiplomaticReplyPanel(DiplomacyRequestReply reply) {
        log("==MAIN UI==   selectDiplomaticReplyPanel");
        diplomaticMessageUI.initReply(reply);
        diplomaticMessageUI.endFade();
        if (!UserPreferences.windowed())
            selectDialogPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
        else
            selectPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
    }
    public void selectDiplomaticReplyModalPanel(DiplomacyRequestReply reply) {
        session().pauseNextTurnProcessing("Show Diplomatic Reply");
        log("==MAIN UI==   selectDiplomaticReplyModalPanel");
        diplomaticMessageUI.initReply(reply);
        diplomaticMessageUI.endFade();
        if (!UserPreferences.windowed())
            selectDialogPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
        else
            selectPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
        session().waitUntilNextTurnCanProceed();
    }
    // BR: After using the Research panel as help!
    public void continueDiplomaticReplyModalPanel() {
        if (!UserPreferences.windowed())
            selectDialogPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
        else
            selectPanel(DIPLOMATIC_MESSAGE_PANEL, diplomaticMessageUI);
    }

    public void allocateSystems() {
        try {
            drawNextTurnNotice = false;
            session().pauseNextTurnProcessing("Show Allocate Systems");
            log("==MAIN UI==   allocate systems");
            mainUI().allocateSystems(session().systemsToAllocate());
            selectMainPanel();
            session().waitUntilNextTurnCanProceed();
        } finally {
            drawNextTurnNotice = true;
        }
    }
    public void showSystemsScouted() {
        session().pauseNextTurnProcessing("Show Systems Scouted");
        log("==MAIN UI==   show systems scouted");
   		mainUI().showSystemsScouted(session().systemsScouted());
        selectMainPanel();
        session().waitUntilNextTurnCanProceed();
    }
    public void showSpyReport() {
        session().pauseNextTurnProcessing("Show SpyReport");
        log("==MAIN UI==   show spy report");
        mainUI().showSpyReport();
        selectMainPanel();
        session().waitUntilNextTurnCanProceed();
    }
    public void showShipConstruction() {
        session().pauseNextTurnProcessing("Show Ship Construction");
        log("==MAIN UI==   show ship construction");
        mainUI().showShipsConstructed(session().shipsConstructed());
        selectMainPanel();
        session().waitUntilNextTurnCanProceed();
    }
    public void initModel() {
        setFocusTraversalKeysEnabled(false);
        setBackground(Color.CYAN);
        setLayout(layout);

        if (!UserPreferences.windowed())
            dialogPane.addToLayout(gameUI, GAME_PANEL);
        else
            add(gameUI, GAME_PANEL);

        add(setupRaceUI, SETUP_RACE_PANEL);
        add(setupGalaxyUI, SETUP_GALAXY_PANEL);
        add(loadGameUI, LOAD_PANEL);
        add(saveGameUI, SAVE_PANEL);
        add(raceIntroUI, INTRO_PANEL);
        add(mainUI(), MAIN_PANEL);
        add(designUI, DESIGN_PANEL);
        add(fleetUI, FLEET_PANEL);
        add(systemsUI, SYSTEMS_PANEL);
        add(racesUI, RACES_PANEL);
        add(planetsUI, PLANETS_PANEL);
        add(allocateTechUI, TECH_PANEL);
        add(sabotageUI, SABOTAGE_PANEL);
        add(shipBattleUI, SHIP_BATTLE_PANEL);
        add(errorUI, ERROR_PANEL);
        add(dialogPane, DIALOG_PANEL);
        add(dnaWorkshopUI, DNA_WORKSHOP_PANEL);

        if (!UserPreferences.windowed()) {
            dialogPane.addToLayout(diplomaticMessageUI, DIPLOMATIC_MESSAGE_PANEL);
            dialogPane.addToLayout(selectNewTechUI, SELECT_NEW_TECH_PANEL);
            dialogPane.addToLayout(colonizePlanetUI, COLONIZE_PROMPT_PANEL);
            dialogPane.addToLayout(colonyViewUI, COLONY_VIEW_PANEL);
            dialogPane.addToLayout(discoverTechUI, DISCOVER_TECH_PANEL);
            dialogPane.addToLayout(groundBattleUI, GROUND_BATTLE_PANEL);
            dialogPane.addToLayout(gnnUI, GNN_PANEL);
            dialogPane.addToLayout(galacticCouncilUI, COUNCIL_PANEL);
            dialogPane.addToLayout(gameOverUI, GAME_OVER_PANEL);
        }
        else {
            add(diplomaticMessageUI, DIPLOMATIC_MESSAGE_PANEL);
            add(selectNewTechUI, SELECT_NEW_TECH_PANEL);
            add(colonizePlanetUI, COLONIZE_PROMPT_PANEL);
            add(colonyViewUI, COLONY_VIEW_PANEL);
            add(discoverTechUI, DISCOVER_TECH_PANEL);
            add(groundBattleUI, GROUND_BATTLE_PANEL);
            add(gnnUI, GNN_PANEL);
            add(galacticCouncilUI, COUNCIL_PANEL);
            add(gameOverUI, GAME_OVER_PANEL);
        }
		// Copy the former "Live.Option" to new "Last.Option"
		MOO1GameOptions.copyOptionsFromLiveToLast();
		rulesetManager().newOptions().loadStartupOptions();
		for (int level=0; level<2; level++) {
			SafeListParam allModOptions = AllSubUI.allModOptions(true);
			for (IParam param : allModOptions)
				param.initDependencies(level);
		}
    }
    private void selectDialogPanel(String panelName, BasePanel panel)   {
        currentPane = panelName;
        selectedPanel = panel;
        selectedPanel.playAmbience();
        dialogPane.selectPanel(panelName, panel);
        layout.show(this, DIALOG_PANEL);
    }
    private void selectPanel(String panelName, BasePanel panel)   {
        currentPane = panelName;
        selectedPanel = panel;
        selectedPanel.playAmbience();
        log("showing panel: ", panelName);
        layout.show(this, panelName);
    }
    @Override
    public void enableGlassPane(BasePanel panel)   {
        super.enableGlassPane(panel);
        panel.playAmbience();
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        long newTime  = timeMs();
        animationMs = newTime;
        animationCount++;
		for (ActionListener listener : animationListeners)
			listener.actionPerformed(e);
        animate();
    }
    @Override
    public void animate() {
        try {
            AnimationManager.reclaimImages();
            if (playAnimations()) {
                if (glassPane() != null)
                    glassPane().animate();
                if (selectedPanel != null)
                    selectedPanel.animate();
            }
            else {
                if (glassPane() != null)
                    glassPane().animateForLowGraphic();
                if (selectedPanel != null)
                    selectedPanel.animateForLowGraphic();
            }
        }
        catch (Exception e) {
            // we have to catch all errors or else the
            // animation timer stops completely
            e.printStackTrace();
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
    	setModifierKeysState(e); // BR: For the Flag color selection
        if (glassPane() != null)
            glassPane().keyPressed(e);
        else if (selectedPanel != null)
            selectedPanel.keyPressed(e);
    }
    @Override
    public void keyReleased(KeyEvent e) {
    	setModifierKeysState(e); // BR: For the Flag color selection
        if (glassPane() != null)
            glassPane().keyReleased(e);
        else if (selectedPanel != null)
            selectedPanel.keyReleased(e);
		e.consume(); // BR: To fix some "Alt"-Key problems 
    }
    @Override
    public void keyTyped(KeyEvent e) {
        if (glassPane() != null)
            glassPane().keyTyped(e);
        else if (selectedPanel != null)
            selectedPanel.keyTyped(e);
    }
    private class LargeDialogPane extends BasePanel {
        private static final long serialVersionUID = 1L;
        private final CardLayout dialogLayout = new CardLayout();
        private final BasePanel dialogHolder = new BasePanel();
        public LargeDialogPane() {
            initModel();
        }
        @Override
        public boolean hasStarBackground()   { return true; }
        private void initModel() {
            setOpaque(true);
            setBackground(Color.black);
            dialogHolder.setLayout(dialogLayout);

            Dimension size = Rotp.getSize();
            int border = s10;
            int w = size.width-border-border;
            int h = size.height-border-border;

            BasePanel barTop    = new BasePanel();
            BasePanel barBottom = new BasePanel();
            BasePanel barLeft   = new BasePanel();
            BasePanel barRight  = new BasePanel();
            barTop.setOpaque(false);
            barBottom.setOpaque(false);
            barLeft.setOpaque(false);
            barRight.setOpaque(false);

            int barW = 0;
            int barH = 0;
            boolean sideBars = w > (h*8/5);

            if (sideBars) {
                barW = (w-(h*8/5))/2;
                barH = h;
                barTop.setPreferredSize(new Dimension(w+border+border, border));
                barBottom.setPreferredSize(new Dimension(w+border+border, border));
                barLeft.setPreferredSize(new Dimension(barW+border, barH));
                barRight.setPreferredSize(new Dimension(barW+border, barH));
            }
            else {
                barH = (h-(w*5/8))/2;
                barW = w;
                barTop.setPreferredSize(new Dimension(w+border+border, barH));
                barBottom.setPreferredSize(new Dimension(w+border+border, barH));
                barLeft.setPreferredSize(new Dimension(border, barH));
                barRight.setPreferredSize(new Dimension(border, barH));
            }

            setLayout(new BorderLayout());
            add(barTop, BorderLayout.NORTH);
            add(barBottom, BorderLayout.SOUTH);
            add(barLeft, BorderLayout.WEST);
            add(barRight, BorderLayout.EAST);
            add(dialogHolder, BorderLayout.CENTER);
        }
        private void addToLayout(BasePanel panel, String key) {
            dialogHolder.add(panel, key);
        }
        public void selectPanel(String panelName, BasePanel panel)   {
            log("showing dialog panel: ", panelName);
            dialogLayout.show(dialogHolder, panelName);
        }
    }
    /* public class SideBarPane extends BasePanel {
        private static final long serialVersionUID = 1L;
        // private final Color hazeC = new Color(0,0,0,64);
        public SideBarPane() {
            setOpaque(false);
            setBackground(Color.black);
        }
    } */
}
