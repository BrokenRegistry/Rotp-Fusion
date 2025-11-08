/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.ui.game;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static rotp.model.empires.species.CustomRaceDefinitions.getAllowedAlienRaces;
import static rotp.model.empires.species.CustomRaceDefinitions.getBaseRaceList;
import static rotp.model.game.DefaultValues.MOO1_DEFAULT;
import static rotp.model.game.DefaultValues.ROTP_DEFAULT;
import static rotp.model.game.IBaseOptsTools.BASE_UI;
import static rotp.model.game.IBaseOptsTools.LIVE_OPTIONS_FILE;
import static rotp.model.game.IBaseOptsTools.MOD_UI;
import static rotp.model.game.IGalaxyOptions.SIZE_DYNAMIC;
import static rotp.model.game.IGalaxyOptions.SIZE_RANDOM;
import static rotp.model.game.IGalaxyOptions.aliensNumber;
import static rotp.model.game.IGalaxyOptions.bitmapGalaxyLastFolder;
import static rotp.model.game.IGalaxyOptions.difficultySelection;
import static rotp.model.game.IGalaxyOptions.galaxyRandSource;
import static rotp.model.game.IGalaxyOptions.globalCROptions;
import static rotp.model.game.IGalaxyOptions.previewNebula;
import static rotp.model.game.IGalaxyOptions.randomNumAliens;
import static rotp.model.game.IGalaxyOptions.randomNumAliensLim1;
import static rotp.model.game.IGalaxyOptions.randomNumAliensLim2;
import static rotp.model.game.IGalaxyOptions.randomNumStarsLim1;
import static rotp.model.game.IGalaxyOptions.randomNumStarsLim2;
import static rotp.model.game.IGalaxyOptions.shapeSelection;
import static rotp.model.game.IGalaxyOptions.showNewRaces;
import static rotp.model.game.IGalaxyOptions.sizeSelection;
import static rotp.model.game.IGalaxyOptions.useSelectableAbilities;
import static rotp.model.game.IGameOptions.baseAI;
import static rotp.model.game.IGameOptions.defaultAI;
import static rotp.model.game.IMainOptions.noFogOnIcons;
import static rotp.ui.game.BaseModPanel.PolyBox.DOWN_ARROW;
import static rotp.ui.game.BaseModPanel.PolyBox.LEFT_ARROW;
import static rotp.ui.game.BaseModPanel.PolyBox.RIGHT_ARROW;
import static rotp.ui.game.BaseModPanel.PolyBox.UP_ARROW;
import static rotp.ui.options.ISubUiKeys.ADVANCED_SYSTEMS_UI_KEY;
import static rotp.ui.options.ISubUiKeys.GALAXY_SHAPES_UI_KEY;
import static rotp.ui.util.IParam.LABEL_DESCRIPTION;
import static rotp.ui.util.IParam.labelFormat;
import static rotp.ui.util.IParam.langLabel;
import static rotp.ui.util.IParam.tableFormat;

// modnar: needed for adding RenderingHints
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import rotp.Rotp;
import rotp.model.ai.AIList;
import rotp.model.empires.Empire;
import rotp.model.empires.species.CustomRaceDefinitions;
import rotp.model.empires.species.ISpecies;
import rotp.model.empires.species.Species;
import rotp.model.empires.species.SpecificCROption;
import rotp.model.galaxy.AllShapes;
import rotp.model.galaxy.GalaxyFactory.GalaxyCopy;
import rotp.model.galaxy.GalaxyShape;
import rotp.model.galaxy.GalaxyShape.EmpireSystem;
import rotp.model.galaxy.Nebula;
import rotp.model.game.DynOptions;
import rotp.model.game.GameSession;
import rotp.model.game.IGalaxyOptions.ListShapeParam;
import rotp.model.game.IGameOptions;
import rotp.model.game.IInGameOptions;
import rotp.model.game.RulesetManager;
import rotp.model.game.SafeListParam;
import rotp.ui.NoticeMessage;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.game.HelpUI.HelpSpec;
import rotp.ui.main.SystemPanel;
import rotp.ui.options.AllSubUI;
import rotp.ui.util.IParam;
import rotp.ui.util.ListDialogUI;
import rotp.ui.util.ParamButtonHelp;
import rotp.ui.util.ParamList;
import rotp.ui.util.ParamSubUI;
import rotp.ui.util.RotpFileChooser;
import rotp.ui.util.StringList;
import rotp.util.FontManager;
import rotp.util.ModifierKeysState;

public final class SetupGalaxyUI  extends BaseModPanel implements ISpecies, MouseWheelListener {
	private static final long serialVersionUID = 1L;
    // public  static final String guiTitleID	= "SETUP_GALAXY";
	private	static final String GUI_ID       = "START_GALAXY";
	private static final String BACK_KEY	 = "SETUP_BUTTON_BACK";
	private static final String RESTART_KEY	 = "SETUP_BUTTON_RESTART";
	private static final String START_KEY	 = "SETUP_BUTTON_START";
	private static final String SIZE_OPT_KEY = "SETUP_GALAXY_SIZE_STAR_PER_EMPIRE";
	private static final String SIZE_MAX_KEY = "SETUP_RANDOM_NUM_STARS_LIM_MAX";
	private static final String SIZE_MIN_KEY = "SETUP_RANDOM_NUM_STARS_LIM_MIN";
	private static final String NO_SELECTION = "SETUP_BITMAP_NO_SELECTION";
	private static final String SPECIFIC_AI  = "SETUP_SPECIFIC_AI";
	private static final String GLOBAL_AI    = "SETUP_GLOBAL_AI";
	private static final String OPPONENT_RANDOM	 = "SETUP_OPPONENT_RANDOM";
 	private static final int    buttonFont		= 30;
	private	static final Font   bigButtonFont	= FontManager.current().narrowFont(buttonFont);
	public  static final int	MAX_DISPLAY_OPPS = 49;
	private static String opponentRandom = "Randomized";
	private static SetupGalaxyUI instance;
    private final Color darkBrownC = new Color(112,85,68);
	private final Color darkYellow = new Color(223, 223, 0);

	@Override protected ParamButtonHelp newExitButton() {
		return new ParamButtonHelp(
				"SETUP_START",
				START_KEY,	 true,
				START_KEY,	 false,
				RESTART_KEY, true,
				RESTART_KEY, false);
	}
	public final  ParamListOpponentAI opponentAI	= new ParamListOpponentAI( // For Guide
			BASE_UI, "OPPONENT_AI",
			IGameOptions.globalAIset().getAliens(),
			defaultAI.aliensKey);
	private final ParamListSpecificAI specificAI	= new ParamListSpecificAI( // For Guide
			BASE_UI, "SPECIFIC_AI",
			IGameOptions.specificAIset().getAliens(),
			defaultAI.aliensKey);
	private final ParamListSpecificOpponent specificOpponent	= new ParamListSpecificOpponent( // For Guide
			BASE_UI, "SPECIFIC_OPPONENT", guiOptions().allRaceOptions(), opponentRandom);
    public  final ParamListGlobalAbilities  globalAbilities		= new ParamListGlobalAbilities(  // For Guide
			BASE_UI, "GLOBAL_ABILITY", SpecificCROption.BASE_RACE.value);
	private final ParamListSpecificAbilities specificAbilities	= new ParamListSpecificAbilities( // For Guide
			BASE_UI, "SPECIFIC_ABILITY", SpecificCROption.defaultSpecificValue().value);
	private boolean popupPositionsInitialised = false;

	private Box tuneGalaxyBox		= new Box("SETUP_TUNE_GALAXY_OPTIONS"); // BR add UI panel for MOD game options
	private Box compactSetupBox		= new Box("SETUP_GALAXY_COMPACT_OPTIONS"); // BR add UI panel for MOD game options
	private Box compactOptionBox	= new Box("SETUP_GALAXY_COMPACT_OPTIONS"); // BR add UI panel for MOD game options
	private Box	settingsBox			= new Box("SETUP_GALAXY_CLASSIC_OPTIONS");
	private Box	backBox				= new Box("SETUP_GALAXY_BACK");
	private Box	galaxyBox			= new Box("SETUP_GALAXY_PREVIEW");
	private Box	newRacesBox			= new Box(showNewRaces); // BR:
	private Box	showAbilitiesBox; // BR:
	private Box		shapeBox;
	private PolyBox	shapeBoxL		= new PolyBox(LEFT_ARROW);
	private PolyBox	shapeBoxR		= new PolyBox(RIGHT_ARROW);
	private Box		mapOption1Box;
	private PolyBox	mapOption1BoxL	= new PolyBox(LEFT_ARROW);
	private PolyBox	mapOption1BoxR	= new PolyBox(RIGHT_ARROW);			 
	private Box		mapOption2Box;
	private PolyBox	mapOption2BoxL	= new PolyBox(LEFT_ARROW);
	private PolyBox	mapOption2BoxR	= new PolyBox(RIGHT_ARROW);			 
	private Box		mapOption3Box;
	private PolyBox	mapOption3BoxL	= new PolyBox(LEFT_ARROW);
	private PolyBox	mapOption3BoxR	= new PolyBox(RIGHT_ARROW);			 
	private Box		mapOption4Box;
	private PolyBox	mapOption4BoxL	= new PolyBox(LEFT_ARROW);
	private PolyBox	mapOption4BoxR	= new PolyBox(RIGHT_ARROW);			 
	private Box		sizeOptionBox; // BR:
	private PolyBox	sizeOptionBoxL	= new PolyBox(LEFT_ARROW);
	private PolyBox	sizeOptionBoxR	= new PolyBox(RIGHT_ARROW);
	private Box		sizeBox;
	private PolyBox	sizeBoxL		= new PolyBox(LEFT_ARROW);
	private PolyBox	sizeBoxR		= new PolyBox(RIGHT_ARROW);
	private Box		sizeMinBox;
	private PolyBox	sizeMinBoxL		= new PolyBox(LEFT_ARROW);
	private PolyBox	sizeMinBoxR		= new PolyBox(RIGHT_ARROW);
	private Box		sizeMaxBox;
	private PolyBox	sizeMaxBoxL		= new PolyBox(LEFT_ARROW);
	private PolyBox	sizeMaxBoxR		= new PolyBox(RIGHT_ARROW);
	private Box		diffBox;
	private PolyBox	diffBoxL		= new PolyBox(LEFT_ARROW);
	private PolyBox	diffBoxR		= new PolyBox(RIGHT_ARROW);
	private Box		wysiwygBox;
	private PolyBox	wysiwygBoxL		= new PolyBox(LEFT_ARROW);
	private PolyBox	wysiwygBoxR		= new PolyBox(RIGHT_ARROW);
	private Box		oppBox;
	private PolyBox	oppBoxU			= new PolyBox(UP_ARROW);
	private PolyBox	oppBoxD			= new PolyBox(DOWN_ARROW);
	private Box		oppRandomBox;
	private Box		oppMinBox;
	private PolyBox	oppMinBoxU		= new PolyBox(UP_ARROW);
	private PolyBox	oppMinBoxD		= new PolyBox(DOWN_ARROW);
	private Box		oppMaxBox;
	private PolyBox	oppMaxBoxU		= new PolyBox(UP_ARROW);
	private PolyBox	oppMaxBoxD		= new PolyBox(DOWN_ARROW);
	private Box		aiBox			= new Box(opponentAI);
	private PolyBox	aiBoxL			= new PolyBox(LEFT_ARROW);
	private PolyBox	aiBoxR			= new PolyBox(RIGHT_ARROW);
	private Box		abilitiesBox	= new Box(globalAbilities); // dataRace selection
	private PolyBox	abilitiesBoxL	= new PolyBox(LEFT_ARROW);
	private PolyBox	abilitiesBoxR	= new PolyBox(RIGHT_ARROW);

	private Box[]	oppSet			= new Box[MAX_DISPLAY_OPPS];
	private Box[]	oppAI			= new Box[MAX_DISPLAY_OPPS];
	private Box[]	oppAbilities	= new Box[MAX_DISPLAY_OPPS]; // BR: dataRace selection

	private boolean starting = false;
	private int leftBoxX, rightBoxX, boxW, boxY, leftBoxH, rightBoxH;
	private int galaxyX, galaxyY, galaxyW, galaxyH;

    private int  galaxyGrid  = 10;
    private boolean showGrid = false;
    private boolean prevShowGrid = false;

    private int wBigMug		= s76;
    private int hBigMug		= s82;
    private int rShBigMug	= s78;

	private BufferedImage playerMug;
	private BufferedImage bigBackMug;
	private BufferedImage nullMug;
	private BufferedImage backMug;
	private BufferedImage[] rivalMugs;

	private IGameOptions opts;
	private boolean forceUpdate = true;
	private List<Nebula> nebulas;
	private ListShapeParam shapeOptionsList;

	private int lastMugSize;
    // Buttons Parameters
    private int buttonSep	= s15;
    private Box	helpBox		= new Box("SETTINGS_BUTTON_HELP");
	@Override public boolean drawMemory()		{ return true; }
	@Override protected Box newExitBox()		{ return new Box(newExitButton()); }
	@Override protected Font bigButtonFont()	{ return bigButtonFont; }
	@Override protected Font bigButtonFont(boolean retina)		{
		if (retina)
			return narrowFont(retina(buttonFont));
		else
			return bigButtonFont;
	}
	@Override protected void setBigButtonGraphics(Graphics2D g)	{
		g.setFont(bigButtonFont());
		g.setPaint(GameUI.buttonRightBackground());
	}
	@Override protected void setSmallButtonGraphics(Graphics2D g) {
		g.setFont(smallButtonFont());
		g.setPaint(GameUI.buttonLeftBackground());
	}

    public static ParamList specificAI() { return instance.specificAI; }
    public static ParamList opponentAI() { return instance.opponentAI; }
	public static int mouseBoxIndex()	 {
		if (instance.hoverBox == null)
			return 0;
		return instance.hoverBox.mouseBoxIndex();
	}

	public SetupGalaxyUI() {
		instance = this;
		init0();
	}
	private void init0() {
		isSubMenu = false;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	public void initOpponentGuide() {
		opponentRandom = text(OPPONENT_RANDOM);
		LinkedList<String> list = new LinkedList<>();
		list.addAll(opts.getInternalSpeciesList());
		list.add(opponentRandom); // For Random (Randomized)
		specificOpponent.reInit(list);
		specificOpponent.defaultValue(opponentRandom);
	}
	private void initAIandAbilitiesList() {
		initOpponentGuide();
		StringList allowedAlien = getAllowedAlienRaces();
		StringList baseRaceList = getBaseRaceList();
		StringList list = new StringList();

		// specific Abilities
		list.addAll(SpecificCROption.options());
		list.removeLast(); // The blank one (USER_CHOICE)
		list.addAll(allowedAlien);
		list.addAll(baseRaceList);
		specificAbilities.reInit(list);

		// global Abilities
		list.clear();
		list.addAll(globalCROptions.getBaseOptions());
		list.addAll(allowedAlien);
		list.addAll(baseRaceList);
		globalAbilities.reInit(list);
	}
	private ListShapeParam shapeOptionsList()	{
		if (shapeOptionsList == null)
			shapeOptionsList = opts.galaxyShape().paramList();
		return shapeOptionsList;
	}
	public	void refreshShapeOptions(ListShapeParam optionsList)	{
		if (notActive() && opts == null)
			return;
		shapeOptionsList = null;
		switch (shapeOptionsList().size()) {
			case 4:
				if (mapOption4Box != null)
					mapOption4Box.initGuide(shapeOptionsList.get(3));
			case 3:
				if (mapOption3Box != null)
					mapOption3Box.initGuide(shapeOptionsList.get(2));
			case 2:
				if (mapOption2Box != null)
					mapOption2Box.initGuide(shapeOptionsList.get(1));
			case 1:
				if (mapOption1Box != null)
					mapOption1Box.initGuide(shapeOptionsList.get(0));
				break;
			default:
				System.err.println("Wrong Size: GalaxyShape.updateShapeOptions " + shapeOptionsList().size());
		}
	}
	@Override protected void singleInit()	{
		showAbilitiesBox	= new Box(useSelectableAbilities);
		shapeBox		= new Box(shapeSelection);
		mapOption1Box	= new Box((IParam) null);
		mapOption2Box	= new Box((IParam) null);
		mapOption3Box	= new Box((IParam) null);
		mapOption4Box	= new Box((IParam) null);
		sizeOptionBox	= new Box(opts.dynStarsPerEmpire());
		sizeBox			= new Box(sizeSelection);
		sizeMinBox		= new Box(randomNumStarsLim1);
		sizeMaxBox		= new Box(randomNumStarsLim2);
		diffBox			= new Box(difficultySelection);
		wysiwygBox		= new Box(galaxyRandSource);
		oppBox			= new Box(aliensNumber);
		oppMinBox		= new Box(randomNumAliensLim1);
		oppMaxBox		= new Box(randomNumAliensLim2);
		oppRandomBox	= new Box(randomNumAliens);

		paramList = AllSubUI.optionsGalaxy();
		paramList.addAll(AllSubUI.getHandle(GALAXY_SHAPES_UI_KEY).getUiAll(false));

		for (int i=0;i<oppSet.length;i++)
			oppSet[i] = new Box(specificOpponent, i);
		for (int i=0;i<oppAbilities.length;i++)
			oppAbilities[i] = new Box(specificAbilities, i);
		for (int i=0;i<oppAI.length;i++)
			oppAI[i] = new Box(specificAI, i);
		duplicateList = new SafeListParam("GALAXY_DUPLICATE");
		duplicateList.add(difficultySelection);
		duplicateList.add(shapeSelection);
		duplicateList.add(sizeSelection);
		duplicateList.add(aliensNumber);

		for (IParam param : duplicateList)
			for (int i=0; i<2; i++)
				param.initDependencies(i);
	}
	@Override public void init() {
		showGrid = prevShowGrid;
		opts = guiOptions();
		isOnTop = true;
		super.init();
		playerMug  = null;
		initAIandAbilitiesList();
		opts.saveOptionsToFile(LIVE_OPTIONS_FILE);
		refreshGui(0);
	}
	@Override protected String GUI_ID() { return GUI_ID; }
	@Override public void refreshGui(int level) {
		refreshShapeOptions(opts.galaxyShape().paramList());
		opts.setAndGenerateGalaxy();
		clearMugs();
		backImg = null;
		nebulas = null;
		repaint();
	}
	@Override public void clearImages()	{
		super.clearImages();
		nebulas = null;
		clearMugs();
	}
	private void initPopupPositions()	{
		if (popupPositionsInitialised)
			return;
		SafeListParam list = AllSubUI.getHandle(ADVANCED_SYSTEMS_UI_KEY).optionsMap().getListNoTitle();
		int boxX = galaxyBox.x - scaled(360);
		for ( IParam param : list) {
			if (param instanceof ParamList) {
				ParamList p = (ParamList) param;
				p.setPosition(boxX, -1);
			}
		}
		popupPositionsInitialised = true;
	}
	private int mugWidth(int size)	{ return scaled(380)/size; }
	private int mugHeight(int size)	{ return scaled(410)/size; }
	private int mugRSh(int size)	{ return scaled(390)/size; }
	private void resetMugs()		{
		lastMugSize	= 0;
		rivalMugs	= null;
		nullMug		= null;
		backMug		= null;
	}
	private void clearMugs()		{
		playerMug	= null;
		bigBackMug	= null;
		resetMugs();
	}
	private void noFogChanged()		{
		noFogOnIcons.toggle();
		clearMugs();
		repaint();
	}
	private BufferedImage getMug(BufferedImage diplo, BufferedImage back)	{
		int bw = back.getWidth();
		int bh = back.getHeight();
		int dw = diplo.getWidth();
		int dh = diplo.getHeight();
		BufferedImage mug = new BufferedImage(bw, bh, TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) mug.getGraphics();
	
		float fog = opts.noFogOnIcons()? 1.0f : 0.5f;
		Composite raceComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER , fog);
		g.setComposite(raceComp);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(back, 0, 0, bw, bh, null);
		g.drawImage(diplo, 0, 0, bw, bh, 0, 0, dw, dh, null);
		g.dispose();
		return mug;
	}
	private BufferedImage rivalMug(int num, int size)	{
		if (size != lastMugSize) {
			resetMugs();
			lastMugSize = size;
		}
		if (rivalMugs == null)
			initRivalMugs();
		return rivalMugs[num]; 
	}
	private void updateRivalMugs(int i)	{
		if (rivalMugs == null) {
			initRivalMugs();
			return;
		}
		String selOpp = opts.selectedOpponentRace(i);
		if (selOpp == null)
			rivalMugs[i] = nullMug();
		else {
			BufferedImage diplo = new Species(selOpp).diploMugshotQuiet();
			rivalMugs[i] = getMug(diplo, backMug());
		}
	}
	private void initRivalMugs()		{
		rivalMugs	=  new BufferedImage[MAX_DISPLAY_OPPS];
		for (int i=0; i<MAX_DISPLAY_OPPS; i++)
			updateRivalMugs(i);
	}
	private BufferedImage backMug()		{
		if (backMug == null)
			initRivalBackMugs();
		return backMug;
	}
	private BufferedImage bigBackMug()	{
		if (bigBackMug == null)
			initBackMugs();
		return bigBackMug;
	}
	private BufferedImage nullMug()		{
		if (nullMug == null)
			initRivalBackMugs();
		return nullMug;
	}
	private void initRivalBackMugs()	{
		backMug	= opts.getMugBackImg(mugWidth(lastMugSize), mugHeight(lastMugSize), mugRSh(lastMugSize));
		String label = text("SETUP_OPPONENT_RANDOM");
		nullMug = getNullMug(label);
	}
	private void initBackMugs()	{ bigBackMug = opts.getMugBackImg(wBigMug, hBigMug, rShBigMug); }
	private BufferedImage getNullMug(String label) {
		BufferedImage back = backMug();
		int bw = back.getWidth();
		int bh = back.getHeight();
		BufferedImage mug = new BufferedImage(bw, bh, TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) mug.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(back, 0, 0, null);
		g.setFont(narrowFont(30));
		g.setColor(Color.black);
		int randSW = g.getFontMetrics().stringWidth(label);
		int x = (bw - randSW)/2;
		int y = bh - scaled(150) / lastMugSize;
		drawString(g, label, x, y);
		g.dispose();
		return mug;
	}
	@Override public void showHelp() {
		loadHelpUI();
		repaint();   
	}
	@Override public void showHotKeys() {
		loadHotKeysUI();
		repaint();   
	}
	@Override protected void loadHotKeysUI() {
    	HelpUI helpUI = RotPUI.helpUI();
        helpUI.clear();
        int xHK = scaled(100);
        int yHK = scaled(70);
        int wHK = scaled(360);
        helpUI.addBrownHelpText(xHK, yHK, wHK, 18, text("SETUP_GALAXY_HELP_HK"));
        helpUI.open(this);
	}
	private void loadHelpUI() {
		int xBox, yBox, wBox;
		int x1, y1, x2, y2;
		int xb, xe, yb, ye;
		int nL, hBox;
		String txt;
		HelpSpec sp;
		Box dest;
		int w = getWidth();
		HelpUI helpUI = RotPUI.helpUI();
		helpUI.clear();
		int lineH= HelpUI.lineH();

		// Overview = Top, Center
		txt  = text("SETUP_GALAXY_MAIN_DESC");
		nL   = 4;
		wBox = scaled(400);
		xBox = w/2 - wBox/2;
		xBox = rightBoxX;
		yBox = s10;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
		int hShift = s15;
		int xTab   = s15;

		// Small Buttons at the bottom
		wBox = scaled(200);

		// Default button: Touch Galaxy
		dest = defaultBox;
		txt  = dest.getDescription();
		nL   = 5;
		hBox = HelpUI.height(nL);
		x1	 = rightBoxX - wBox;
		y1	 = dest.y - hBox - hShift;
		xBox = x1;
		yBox = y1 - 0*lineH/2;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = xBox + wBox*3/4;
		yb   = sp.ye();
		xe   = dest.x + dest.width/2;
		ye   = dest.y;
		sp.setLine(xb, yb, xe, ye);

		// Back button; left Galaxy
		dest = backBox;
		txt  = dest.getDescription();
		nL   = 2;
		hBox = HelpUI.height(nL);
		y2	 = y1 - hBox - hShift;
		xBox = x1;
		yBox = y2;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = xBox + wBox;
		yb   = sp.yc();
		xe   = dest.x - s25;
		ye   = dest.y;
		sp.setLineArr(new int[] {xb, yb, xb+s15, yb, xe, ye, dest.x, dest.y+s10});

		// User button: Left of Last button
		dest = userBox;
		txt  = dest.getDescription();
		nL   = 5;
		xBox = x1 - (wBox+s20)*3/2 - xTab;
		yBox = y1 + s10;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox+s20, -nL, txt);
		xb   = sp.xce();
		yb   = sp.ye();
		xe   = dest.x + dest.width/2;
		ye   = dest.y;
		sp.setLine(xb, yb, xe, ye);

		// Last button: Left of Default button
		dest = lastBox;
		txt  = dest.getDescription();
		nL   = 3;
		x2	 = x1 - wBox - xTab;
		xBox = x2;
		yBox = y2;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = xBox + wBox*3/4;
		yb   = sp.ye();
		xe   = dest.x + dest.width/4;
		ye   = dest.y;
		sp.setLine(xb, yb, xe, ye);

		// Guide button: Left of User button
		dest = guideBox;
		txt  = dest.getDescription();
		nL   = 3;
		hBox = HelpUI.height(nL);
		xBox = x2 - wBox - xTab;
		yBox = y2;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = xBox + wBox*1/4;
		yb   = sp.ye();
		xe   = dest.x + dest.width*1/2;
		ye   = dest.y;
		sp.setLine(xb, yb, xe, ye);

		// Big buttons, bottom up

		// Start button; right Galaxy
		dest = exitBox;
		txt  = dest.getHelp();
		nL   = 11;
		wBox = scaled(300);
		hBox = HelpUI.height(nL);
		xBox = rightBoxX + boxW + s50 - wBox;
		yBox = dest.y - hBox - scaled(150);
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = xBox + wBox*5/6;
		yb   = sp.ye();
		xe   = dest.x + dest.width - s5;
		ye   = dest.y;
		sp.setLine(xb, yb, xe, ye);

		// Tune Galaxy button;
		int margin = s3;
		dest = tuneGalaxyBox;
		txt  = dest.getHelp();
		nL   = 3;
		wBox = scaled(400);
		hBox = HelpUI.height(nL);
		xBox = rightBoxX - scaled(270);
		yBox = y2 - hBox - hShift;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = xBox + wBox*6/7;
		yb   = sp.ye();
		xe   = dest.x + dest.width/2;
		ye   = dest.y;
		sp.setLine(xb, yb, xe, ye);
		// int y3 = yBox;

		// Opponents parameters
		wBox   = scaled(250);
		hShift = s60;  

		txt  = text("SETUP_GALAXY_AGAINST_DESC");
		dest = aiBox;
//		txt  = dest.getGuide();
		nL   = 5;
		hBox = HelpUI.height(nL);
		xBox = dest.x + dest.width/2 - wBox*3/4;
		yBox = dest.y + hShift;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = xBox + wBox*3/4;
		yb   = yBox;
		xe   = dest.x + dest.width/2;
		ye   = dest.y + dest.height;
		sp.setLine(xb, yb, xe, ye);

		dest = oppRandomBox;
		txt  = dest.getDescription();
		nL   = 3;
		hBox = HelpUI.height(nL);
		xBox = dest.x - wBox - hShift/2;
		yBox = dest.y;
		HelpSpec spOR = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = spOR.xe();
		yb   = spOR.yc();
		xe   = dest.x + dest.width/2;
		ye   = dest.y + dest.height/2;
		spOR.setLine(xb, yb, xe, ye);

		dest = newRacesBox;
		txt  = dest.getDescription();
		nL   = 3;
		hBox = HelpUI.height(nL);
		xBox = dest.x + dest.width/2 - wBox/4;
		yBox = dest.y + hShift/2;
		HelpSpec sp2 = helpUI.addBrownHelpText(xBox, yBox, 2*wBox, -nL, txt);
		xb   = xBox + wBox/4;
		yb   = yBox;
		xe   = dest.x + dest.width/2;
		ye   = dest.y + dest.height;
		sp2.setLine(xb, yb, xe, ye);


		dest = showAbilitiesBox;
		txt  = dest.getDescription();
		nL   = 3;
		hBox = HelpUI.height(nL);
		xBox = dest.x + dest.width + s40;
		yBox = dest.y + dest.height/2 - hBox/2 - s10;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = xBox;
		yb   = sp.yc() + s10;
		xe   = dest.x + dest.width;
		ye   = dest.y + dest.height/2;
		sp.setLine(xb, yb, xe, ye);

		wBox = scaled(450);
		dest = abilitiesBox;
		txt  = dest.getDescription();
		nL   = 5;
		hBox = HelpUI.height(nL);
		xBox = leftBoxX;
		yBox = dest.y - hBox - s40;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = sp.xce();
		yb   = sp.ye();
		xe   = dest.x + dest.width*3/4;
		ye   = dest.y;
		sp.setLine(xb, yb, xe, ye);

		// Options Buttons
		txt  = text("");
		switch (opts.compactOptionOnly().get().toUpperCase()) {
			case "NO": {
				hBox = settingsBox.height + 2*margin;
				wBox = settingsBox.width + 2*margin;
				xBox = settingsBox.x - margin;
				yBox = settingsBox.y - margin;
				nL   = 2;
				txt  = text("SETUP_GALAXY_VANILLA_OPTIONS_HELP");
				break;
			}
			case "YES":
			default: {
				hBox = compactSetupBox.height + 2*margin;
				wBox = compactSetupBox.x + compactOptionBox.width - compactOptionBox.x + 2*margin;
				xBox = compactOptionBox.x - margin;
				yBox = compactSetupBox.y - margin;
				nL   = 7;
				txt  = text("SETUP_GALAXY_COMPACT_OPTIONS_HELP");
				break;
			}
		}

		int[] lineArr = sp.rect(xBox, yBox, wBox, hBox);
		xe = xBox;
		ye = yBox;
		yb = yBox - s80;

		hBox = HelpUI.height(nL);
		wBox = scaled(440);
		xBox = rightBoxX - scaled(210);
		yBox = sp2.ye() + s10;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, -nL, txt);
		xb   = xBox + wBox*6/7;
		yb   = yBox + sp.height();

		switch (opts.compactOptionOnly().get().toUpperCase()) {
			case "NO": {
				xe = settingsBox.x;
				ye = settingsBox.y;
				break;
			}
			case "YES":
			default: {
				sp.setLineArr(lineArr);
				break;
			}
		}
		sp.setLine(xb, yb, xe, ye);

		helpUI.open(this);
	}
    @Override protected void doExitBoxAction() { doStartBoxAction(); }
    private void doStartBoxAction() {
		buttonClick();
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT:
			restartGame();
			return;
		default: // Save
			startGame();
			return; 
		}
 	}
	private void doBackBoxAction() {
		buttonClick();
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT: // Restore
			// loadAndUpdateFromFileName(opts, LIVE_OPTIONS_FILE, ALL_GUI_ID);
			// break;
		default: // Save
			opts.saveOptionsToFile(LIVE_OPTIONS_FILE);
			break; 
		}
		// Go back to Race Panel
		close();
		String name = opts.selectedLeaderName();
		RotPUI.instance().selectSetupRacePanel(name);
 	}
	private static String backButtonKey() {
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT:
			// return restoreKey;
		default:
			return BACK_KEY;
		}
	}
	private String getBitmapFile() {
		String dirPath = bitmapGalaxyLastFolder.get();
		File selectedFile = new File(opts.galaxyShape().getOption3());
		if (selectedFile.exists()) {
			dirPath = selectedFile.getParentFile().getAbsolutePath();
			bitmapGalaxyLastFolder.set(dirPath);
			UserPreferences.save();
		}
		JFileChooser fileChooser = new RotpFileChooser(300, 200, 420,470);
		fileChooser.setCurrentDirectory(new File(dirPath));
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"Images (bmp, gif, jpg, png, webp)", "bmp", "gif", "jpg", "jpeg", "png", "webp"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"Windows Bitmap (bmp)", "bmp"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"Compuserve GIF (gif)", "gif"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"JPG / JPEG Format (jpg, jpeg)", "jpg", "jpeg"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"Portable Network Graphics Files (png)", "png"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"Wippy File Format (webp)", "webp"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
				"All files (*.*", "*"));
		// Add listener to file picking
		fileChooser.addPropertyChangeListener(new BMPropertyChangeListener(fileChooser));
		int result = fileChooser.showOpenDialog(getParent());
		if (result == JFileChooser.APPROVE_OPTION) {
			// user selects a file
			selectedFile = fileChooser.getSelectedFile();
			dirPath = selectedFile.getParentFile().getAbsolutePath();
			bitmapGalaxyLastFolder.set(dirPath);
			UserPreferences.save();
			return selectedFile.getPath();
		}
		return opts.galaxyShape().paramOption3().defaulttoString();
	}
	public  void selectBitmapFromList() {
		if (notActive())
			return;
		String filePath = getBitmapFile();
		opts.galaxyShape().paramOption3().setString(filePath);
		opts.galaxyShape().quickGenerate();
		repaint();
	}
	@Override public void preview(String s, IParam param) {
		if (s == null)
			return;
		if (!s.equalsIgnoreCase("quickGenerate")) {
			if (shapeSelection == param) {
				shapeSelection.set(s);
				return;
			}
			else if (sizeSelection == param) {
				sizeSelection.set(s);
				return;
			}
			else if (opts.galaxyShape().paramOption1() == param) {
				opts.galaxyShape().paramOption1().setString(s);
				postSelectionMedium(false);
				return;
			}
			else if (opts.galaxyShape().paramOption2() == param) {
				opts.galaxyShape().paramOption2().setString(s);
				postSelectionMedium(false);
				return;
			}
			else if (isShapeBitmapGalaxy() &&
					opts.galaxyShape().paramOption3() == param){
				opts.galaxyShape().paramOption3().setString(s);
				postSelectionMedium(false);
				return;
			}			
		}
		postSelectionMedium(false);
	}	
	private String selectSpecificAIFromList(int i) {
		String title			= text(SPECIFIC_AI);
		String message			= text(SPECIFIC_AI + LABEL_DESCRIPTION);
		String initialChoice	= text(opts.specificOpponentAIOption(i+1));
		AIList list				= IGameOptions.specificAIset();
		List<String> returnList = list.getAliens();
		String[] choiceArray	= list.getNames().toArray(new String[list.size()]);;
		ListDialogUI dialog = RotPUI.instance().listDialog();
		dialog.init(
			this, getParent(),			// Frame & Location component
			message, title,				// Message, Title
			choiceArray,				// List
			initialChoice, 				// Initial choice
			"XX_AI: Character_XX",		// long Dialogue
			true,						// isVerticalWrap
			-1, -1,						// Position
			scaled(350), scaled(250),	// size Width, Height
			null, null,					// Font, Preview
			returnList,					// Alternate return
			specificAI);				// help parameter
		String input = (String) dialog.showDialog(0);
		ModifierKeysState.reset();
		repaint();
		if (input == null)
			return initialChoice;
		opts.specificOpponentAIOption(input, i+1);
		return input;
	}
	private String selectGlobalAIFromList() {
		String title			= text(GLOBAL_AI);
		String message			= text(GLOBAL_AI + LABEL_DESCRIPTION);
		String initialChoice	= text(opts.selectedOpponentAIOption());
		AIList list				= IGameOptions.globalAIset();
		List<String> returnList = list.getAliens();
		String[] choiceArray	= list.getNames().toArray(new String[list.size()]);;
		ListDialogUI dialog = RotPUI.instance().listDialog();
		dialog.init(
			this, getParent(),			// Frame & Location component
			message, title,				// Message, Title
			choiceArray,				// List
			initialChoice, 				// Initial choice
			"XX_AI:Character_XX",		// long Dialogue
			true,						// isVerticalWrap
			-1, -1,						// Position
			scaled(350), scaled(270),	// size Width, Height
			null, null,					// Font, Preview
			returnList,					// Alternate return
			opponentAI);				// help parameter

		String input = (String) dialog.showDialog(0);
		ModifierKeysState.reset();
		repaint();
		if (input == null)
			return initialChoice;
		opts.selectedOpponentAIOption(input);
		return input;
	}
	// ==============================================================
	// Paint components sub sections
	// ==============================================================
	private Graphics2D paintInit(Graphics g0) {
		//showTiming = true; // TO DO BR: COMMENTS
		if (showTiming)
			System.out.println("===== Galaxy PaintComponents =====");
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		// modnar: use (slightly) better upsampling
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		int w = getWidth();
		int h = getHeight();

		for (Box box: oppSet)
			box.setBounds(0,0,0,0);
		for (Box box: oppAI)
			box.setBounds(0,0,0,0);
		for (Box box: oppAbilities)
			box.setBounds(0,0,0,0);
		// background image
		// this.backImg = null;			// TO DO BR: Remove
		g.drawImage(backImg(), 0, 0, w, h, this);
		// drawFixButtons(g, false);	// TO DO BR: Remove
		drawFixButtons(g, forceUpdate);
		forceUpdate = false;
		drawButtons(g);
		return g;
	}
	private void drawOpponentCount(Graphics2D g) {
		// draw number of opponents
		int numOpp = opts.selectedNumberOpponents();
		g.setFont(narrowFont(30));
		g.setColor(Color.black);
		if (isRandomNumAlien()) {
			g.setFont(narrowFont(26));
			int minOpp = opts.randomNumAliensLim1();
			String str = str(minOpp);
			int numSW = g.getFontMetrics().stringWidth(str);
			int x0 = oppMinBox.x + ((oppMinBox.width-numSW)/2);
			int y0 = oppMinBox.y + oppMinBox.height -s6;
			drawString(g, str, x0, y0);

			int maxOpp = opts.randomNumAliensLim2();
			str = str(maxOpp);
			numSW = g.getFontMetrics().stringWidth(str);
			x0 = oppMaxBox.x + ((oppMaxBox.width-numSW)/2);
			y0 = oppMaxBox.y + oppMaxBox.height -s6;
			drawString(g, str, x0, y0);
		}
		else {
			g.setFont(narrowFont(30));
			String oppStr = str(numOpp);
			int numSW = g.getFontMetrics().stringWidth(oppStr);
			int x0 = oppBox.x + ((oppBox.width-numSW)/2);
			int y0 = oppBox.y + oppBox.height -s8;
			drawString(g, oppStr, x0, y0);
		}
		drawOpponentBoxes(g);
	}
	private void drawOpponentBoxes(Graphics2D g) {
		int numOpp	= opts.selectedNumberOpponents();
		int numCols	= max (3, ceil(sqrt(numOpp)));
		int mugW	= mugWidth(numCols);
		int mugH	= mugHeight(numCols);
		int fSize	= 108 / (numCols+2);
		int offset1	= s36 / (numCols+2);
		int offset2	= scaled(108) / (numCols+2);
		int boundH	= scaled(144) / (numCols+2);
		int spaceW	= mugW + ((boxW-s60-numCols*mugW) / (numCols-1));
		int spaceH	= mugH + s60 / (numCols-1);
		int y0		= scaled(214);

		Stroke prevStroke = g.getStroke();
		Color borderC = GameUI.setupFrame();
		boolean selectableAI = opts.selectableAI();
		boolean selectableCR = useSelectableAbilities.get();
		int maxDraw = min((numCols*numCols), numOpp, MAX_DISPLAY_OPPS);
		for (int i=0; i<maxDraw; i++) {
			int row	= i/numCols;
			int col	= i%numCols;
			int y2	= y0 + row*spaceH;
			int x2	= leftBoxX + s30 + col*spaceW;
			int y2o	= y2;
			int mugHo = mugH;
			if (selectableCR) {
				y2o   += boundH;
				mugHo -= boundH;
			}
			if (selectableAI)
				mugHo -= boundH;

			oppSet[i].setBounds(x2, y2o, mugW, mugHo);
			oppAI[i].setBounds(x2, y2+mugH-boundH, mugW, boundH);
			oppAbilities[i].setBounds(x2, y2, mugW, boundH);
			g.drawImage(rivalMug(i, numCols), x2, y2, this);

			if (selectableAI) {
				g.setColor(SystemPanel.whiteText);
				String aiText = text(opts.specificOpponentAIOption(i+1));
				g.setFont(narrowFont(fSize)); // BR: Adjusted to fit the box
				scaledFont(g, aiText, mugW-s2, fSize, fSize-5);
				int aiSW = g.getFontMetrics().stringWidth(aiText);
				int x2b = x2+(mugW-aiSW)/2;
				drawBorderedString(g, aiText, x2b, y2+mugH-offset1, SystemPanel.blackText, SystemPanel.whiteText);
			}
			if (selectableCR) {
				g.setColor(SystemPanel.whiteText);
				String crText = specificAbilities.guideValue(i+1);
				g.setFont(narrowFont(fSize));
				int fontSize = scaledFont(g, crText, mugW-s2, fSize, fSize-6);
				int dy = scaled(fSize-fontSize)/2;
				int crSW = g.getFontMetrics().stringWidth(crText);
				int x2b = x2+(mugW-crSW)/2;
				drawBorderedString(g, crText, x2b, y2+offset2-dy, SystemPanel.blackText, SystemPanel.whiteText);
			}
			g.setStroke(stroke1);
			g.setColor(borderC);
			g.drawRect(x2, y2, mugW, mugH);
			g.setStroke(prevStroke);
		}
	}
	private void drawGalaxy(Graphics2D g) {
		// draw galaxy
		try {
			if (!GalaxyShape.generating)
				drawGalaxyShape(g, opts.galaxyShape(), galaxyX, galaxyY, galaxyW, galaxyH);
		}
		catch(NullPointerException | IndexOutOfBoundsException e) {
			System.out.println("drawGalaxyShape failed");
		}

		// draw info under galaxy map
		g.setColor(Color.black);
		g.setFont(narrowFont(16));
		int galaxyBoxW = boxW-s40;
		int y3 = galaxyY+galaxyH+s16;
		int numSystem = opts.galaxyShape().finalNumberStarSystems();
		String systemsLbl = text("SETUP_GALAXY_NUMBER_SYSTEMS", numSystem);
		int sw3 = g.getFontMetrics().stringWidth(systemsLbl);
		int x3 = rightBoxX+s20+((galaxyBoxW/2)-sw3)/2;
		drawString(g,systemsLbl, x3,y3);

		String maxOppsLbl = text("SETUP_GALAXY_MAX_OPPONENTS", opts.maximumOpponentsOptions());
		int sw4 = g.getFontMetrics().stringWidth(maxOppsLbl);
		int x4 = rightBoxX+s20+(galaxyBoxW/2)+((galaxyBoxW/2)-sw4)/2;
		drawString(g,maxOppsLbl, x4,y3);
	}
	private void highlightHovered(Graphics2D g) {
		// highlight any controls that are hovered
		if ((	 hoverPolyBox == shapeBoxL)		 || (hoverPolyBox == shapeBoxR)
			||  (hoverPolyBox == sizeBoxL)		 || (hoverPolyBox == sizeBoxR)
			||  (hoverPolyBox == sizeMinBoxL)	 || (hoverPolyBox == sizeMinBoxR)
			||  (hoverPolyBox == sizeMaxBoxL)	 || (hoverPolyBox == sizeMaxBoxR)
			||  (hoverPolyBox == diffBoxL)		 || (hoverPolyBox == diffBoxR)
			||  (hoverPolyBox == wysiwygBoxL)	 || (hoverPolyBox == wysiwygBoxR)
			||  (hoverPolyBox == aiBoxL)		 || (hoverPolyBox == aiBoxR)
			||  (hoverPolyBox == abilitiesBoxL)	 || (hoverPolyBox == abilitiesBoxR)
			||  (hoverPolyBox == mapOption1BoxL) || (hoverPolyBox == mapOption1BoxR)
			||  (hoverPolyBox == mapOption2BoxL) || (hoverPolyBox == mapOption2BoxR)
			||  (hoverPolyBox == mapOption3BoxL) || (hoverPolyBox == mapOption3BoxR)
			||  (hoverPolyBox == mapOption4BoxL) || (hoverPolyBox == mapOption4BoxR)
			||  (hoverPolyBox == sizeOptionBoxL) || (hoverPolyBox == sizeOptionBoxR)
			||  (hoverPolyBox == oppBoxU)		 || (hoverPolyBox == oppBoxD)
			||  (hoverPolyBox == oppMinBoxU)	 || (hoverPolyBox == oppMinBoxD)
			||  (hoverPolyBox == oppMaxBoxU)	 || (hoverPolyBox == oppMaxBoxD)) {
			g.setColor(Color.yellow);
			g.fill(hoverPolyBox);
		}
		else if ((hoverBox == shapeBox)		|| (hoverBox == sizeBox)
			|| (hoverBox == sizeMinBox)		|| (hoverBox == sizeMaxBox)
			|| (hoverBox == mapOption1Box)	|| (hoverBox == mapOption2Box)
			|| (hoverBox == mapOption3Box)	|| (hoverBox == mapOption4Box)
			|| (hoverBox == sizeOptionBox)	|| (hoverBox == abilitiesBox)
			|| (hoverBox == aiBox)			|| (hoverBox == newRacesBox)
			|| (hoverBox == oppBox)			|| (hoverBox == oppRandomBox)
			|| (hoverBox == oppMinBox)		|| (hoverBox == oppMaxBox)
			|| (hoverBox == diffBox)		|| (hoverBox == showAbilitiesBox)		
			|| (hoverBox == wysiwygBox)) {
			Stroke prev = g.getStroke();
			g.setStroke(stroke2);
			g.setColor(Color.yellow);
			g.draw(hoverBox);
			g.setStroke(prev);
		}
		else if (hoverBox == galaxyBox) {
			// Standard borders are ugly when applied to galaxy preview
			Stroke prev = g.getStroke();
			g.setStroke(stroke1);
			g.setColor(darkYellow);
			g.draw(hoverBox);
			g.setStroke(prev);
		}
		else {
			if (opts.selectableAI()) {
				for (int i=0;i<oppAI.length;i++) {
					if (hoverBox == oppAI[i]) {
						Stroke prev = g.getStroke();
						g.setStroke(stroke2);
						g.setColor(Color.yellow);
						g.draw(hoverBox);
						g.setStroke(prev);
						break;
					}
				}
			}
			if (useSelectableAbilities.get()) {
				for (int i=0;i<oppAbilities.length;i++) {
					if (hoverBox == oppAbilities[i]) {
						Stroke prev = g.getStroke();
						g.setStroke(stroke2);
						g.setColor(Color.yellow);
						g.draw(hoverBox);
						g.setStroke(prev);
						break;
					}
				}
			}
			for (int i=0;i<oppSet.length;i++) {
				if (hoverBox == oppSet[i]) {
					Stroke prev = g.getStroke();
					g.setStroke(stroke2);
					g.setColor(Color.yellow);
					g.draw(hoverBox);
					g.setStroke(prev);
					break;
				}
			}
		}
	}
	private void drawOpponentTopSection(Graphics2D g) {
		// draw top opponents selections options
		g.setColor(Color.black);
		int fSizeMax = 15;
		int fSizeMin = 10;

		// draw Opponent CR text
		if (globalCROptions.isBaseRace()) // for backward compatibility
			globalCROptions.setFromDefault(false, true);
		abilitiesBox.drawString(g, fSizeMax, fSizeMin);

		// draw Show Abilities Yes/No text
		showAbilitiesBox.drawString(g, fSizeMax, fSizeMin);

		// draw Opponent AI text
		aiBox.drawString(g, fSizeMax, fSizeMin);

		// draw New Races ON/OFF text
		newRacesBox.drawString(g, fSizeMax, fSizeMin);
	}
	private int  drawGalaxyTopOptions(Graphics2D g) {
		// draw galaxy options text
		g.setFont(narrowFont(15));
		int y = shapeBox.y+shapeBox.height-s4;
		String shapeLbl = text(opts.selectedGalaxyShape());
		scaledFont(g, shapeLbl, mapOption1Box.width-s20, 15, 10);
		int shapeSW = g.getFontMetrics().stringWidth(shapeLbl);
		int x =shapeBox.x+((shapeBox.width-shapeSW)/2);
		drawString(g,shapeLbl, x, y);

		if (shapeOptionsList().size() > 0) {
			if (isShapeTextGalaxy()) {
				String label = opts.galaxyShape().getOption1();
				Font prevFont = g.getFont();
				scaledGalaxyFont(g, label, mapOption1Box.width-s10, s16, s15, s6);
				int sw = g.getFontMetrics().stringWidth(label);
				int x1 = mapOption1Box.x+((mapOption1Box.width-sw)/2);
				drawString(g, label, x1, y+s20);
				g.setFont(prevFont);
			}
			else {
				String label = text(opts.galaxyShape().getOption1());
				scaledFont(g, label, mapOption1Box.width-s6, 15, 10);
				int sw = g.getFontMetrics().stringWidth(label);
				int x1 = mapOption1Box.x+((mapOption1Box.width-sw)/2);
				drawString(g, label, x1, y+s20);
			}
		}
		if (shapeOptionsList().size() > 1) {
			String label = text(opts.galaxyShape().getOption2());
			scaledFont(g, label, mapOption2Box.width-s6, 15, 10);
			int sw = g.getFontMetrics().stringWidth(label);
			int x1 =mapOption2Box.x+((mapOption2Box.width-sw)/2);
			drawString(g, label, x1, y+s40);	
		}
		if (shapeOptionsList().size() > 2) {
			if (isShapeTextGalaxy()) {
				String label = opts.galaxyShape().getOption3();
				scaledFont(g, label, mapOption3Box.width-s6, 15, 10);
				int sw = g.getFontMetrics().stringWidth(label);
				int x1 =mapOption3Box.x+((mapOption3Box.width-sw)/2);
				drawString(g, label, x1, y+s60);
			}
			else if (isShapeBitmapGalaxy()) {
				String label = opts.galaxyShape().getOption3();
				if (label.isEmpty())
					label = text(NO_SELECTION);
				else
					label = getNameFromPath(label);
				scaledFont(g, label, mapOption3Box.width-s6, 15, 10);
				int sw = g.getFontMetrics().stringWidth(label);
				int x1 =mapOption3Box.x+((mapOption3Box.width-sw)/2);
				drawString(g, label, x1, y+s60);
			}
		}
		if (shapeOptionsList().size() > 3) {
				String label = opts.galaxyShape().getOption4();
				scaledFont(g, shapeLbl, mapOption4Box.width-s6, 15, 10);
				int sw = g.getFontMetrics().stringWidth(label);
				int x1 =mapOption4Box.x+((mapOption4Box.width-sw)/2);
				drawString(g, label, x1, y+s80);
		}
		g.setFont(narrowFont(15));
		return y;
	}
	private void drawGalaxySizeAndDiff(Graphics2D g, int y5) {
		String sizeLbl = text(opts.selectedGalaxySize());
		int sizeSW = g.getFontMetrics().stringWidth(sizeLbl);
		int x5b =sizeBox.x+((sizeBox.width-sizeSW)/2);
		drawString(g,sizeLbl, x5b, y5);

		if (isDynamicSize()) { // BR:
			String label = text(SIZE_OPT_KEY, opts.dynStarsPerEmpire().guideValue());
			int sw2 = g.getFontMetrics().stringWidth(label);
			int x5b1 =sizeOptionBox.x+((sizeOptionBox.width-sw2)/2);
			drawString(g,label, x5b1, y5+s20);		   
		}
		else if (isRandomSize()) {
			int lim1 = opts.randomNumStarsLim1();
			int lim2 = opts.randomNumStarsLim2();
			String label;
			if (lim1>lim2)
				label = text(SIZE_MAX_KEY, lim1);
			else
				label = text(SIZE_MIN_KEY, lim1);
			int sw2 = g.getFontMetrics().stringWidth(label);
			int xm =sizeMinBox.x+((sizeMinBox.width-sw2)/2);
			drawString(g, label, xm, y5+s20);		   

			if (lim1>lim2)
				label = text(SIZE_MIN_KEY, lim2);
			else
				label = text(SIZE_MAX_KEY, lim2);
			sw2 = g.getFontMetrics().stringWidth(label);
			xm =sizeMaxBox.x+((sizeMaxBox.width-sw2)/2);
			drawString(g, label, xm, y5+s40);		   
		}
		String diffLbl = text(opts.selectedGameDifficulty());
		// modnar: add custom difficulty level option, set in Remnants.cfg
		// append this custom difficulty percentage to diffLbl if selected
		if (diffLbl.equals("Custom")) {
			diffLbl = diffLbl + " (" + Integer.toString(IInGameOptions.customDifficulty.get()) + "%)";
		} else {
			diffLbl = diffLbl + " (" + Integer.toString(Math.round(100 * opts.aiProductionModifier())) + "%)";
		}
		int diffSW = g.getFontMetrics().stringWidth(diffLbl);
		int x5c =diffBox.x+((diffBox.width-diffSW)/2);
		drawString(g,diffLbl, x5c, y5);

		String wysiwygLbl;
		if (galaxyRandSource.get() == 0)
			wysiwygLbl = text("SETTINGS_MOD_GALAXY_RAND_RANDOM");
		else
			wysiwygLbl = text("SETTINGS_MOD_GALAXY_RAND_WYSIWYG", galaxyRandSource.guideValue());
		int wysiwygSW = g.getFontMetrics().stringWidth(wysiwygLbl);
		if (wysiwygSW > wysiwygBox.width) {
			wysiwygLbl = galaxyRandSource.guideValue();
			wysiwygSW  = g.getFontMetrics().stringWidth(wysiwygLbl);
		}
		int x5d =wysiwygBox.x+((wysiwygBox.width-wysiwygSW)/2);
		drawString(g,wysiwygLbl, x5d, y5+s20);
	}
	private void drawAutoplayWarning(Graphics2D g, int y5) {
		// draw autoplay warning
		if (opts.isAutoPlay()) {
			int shift = 0;
			if (shapeOptionsList().size()>3)
				shift = scaled(150);
			g.setFont(narrowFont(16));
			String warning = text("SETTINGS_AUTOPLAY_WARNING");
			List<String> warnLines = wrappedLines(g, warning, galaxyW-shift);
			g.setColor(Color.white);
			int warnY = y5+s60;
			switch (opts.compactOptionOnly().get().toUpperCase()) {
				case "NO":
				case "YES":
				default:
					warnY += s25;
			}
			warnY -= s18 * (warnLines.size()-2);
			for (String line: warnLines) {
				drawString(g,line, galaxyX+shift, warnY);
				warnY += s18;
			}
		}
	}
	@Override public void paintComponent(Graphics g0) {
		//showTiming = true; // TO DO BR: COMMENTS
		if (!isOnTop)
			return;
		long timeStart = System.currentTimeMillis();
		Graphics2D g = paintInit(g0);

		drawOpponentCount(g);
		drawGalaxy(g);
		highlightHovered(g);
		drawOpponentTopSection(g);

		int y5 = drawGalaxyTopOptions(g);
		drawGalaxySizeAndDiff(g, y5);
		drawAutoplayWarning(g, y5);

		drawHelpButton(g);
		showGuide(g);

		if (starting) {
			NoticeMessage.setStatus(text("SETUP_CREATING_GALAXY"));
			drawNotice(g, 30);
		}
		if (showTiming)
			System.out.println("Galaxy paintComponent() Time = " + (System.currentTimeMillis()-timeStart) + " ms");
	}
	private void drawHelpButton(Graphics2D g) {
		helpBox.setBounds(s20,s20,s20,s25);
		g.setColor(darkBrownC);
		g.fillOval(s20, s20, s20, s25);
		g.setFont(narrowFont(25));
		if (helpBox == hoverBox)
			g.setColor(Color.yellow);
		else
			g.setColor(Color.white);

		drawString(g,"?", s26, s40);
	}
	private void drawFixButtons(Graphics2D g, boolean all) {
		Stroke prev;
		switch (opts.compactOptionOnly().get().toUpperCase()) {
			case "YES": {
				g.setFont(narrowFont(20)); // 18 for 3 buttons// 20 for 2 buttons
				// BR: second UI panel for MOD game options
				// MOD settings button
				if (hoverBox == compactSetupBox || all) {
					String textMOD = text("SETUP_BUTTON_MERGED_STATIC_SETTINGS");
					int swMOD = g.getFontMetrics().stringWidth(textMOD);
					int xMOD = compactSetupBox.x+((compactSetupBox.width-swMOD)/2);
					int yMOD = compactSetupBox.y+compactSetupBox.height-s8;
					Color color = all ? GameUI.borderBrightColor() : Color.yellow;
					drawShadowedString(g, textMOD, 2, xMOD, yMOD, GameUI.borderDarkColor(), color);
					prev = g.getStroke();
					g.setStroke(stroke1);
					g.drawRoundRect(compactSetupBox.x, compactSetupBox.y, compactSetupBox.width, compactSetupBox.height, cnr, cnr);
					g.setStroke(prev);
				}
				// BR: second UI panel for MOD game options
				// MOD settings button
				if (hoverBox == compactOptionBox || all) {
					String textMOD = text("SETUP_BUTTON_MERGED_DYNAMIC_SETTINGS");
					int swMOD = g.getFontMetrics().stringWidth(textMOD);
					int xMOD = compactOptionBox.x+((compactOptionBox.width-swMOD)/2);
					int yMOD = compactOptionBox.y+compactOptionBox.height-s8;
					Color color = all ? GameUI.borderBrightColor() : Color.yellow;
					drawShadowedString(g, textMOD, 2, xMOD, yMOD, GameUI.borderDarkColor(), color);
					prev = g.getStroke();
					g.setStroke(stroke1);
					g.drawRoundRect(compactOptionBox.x, compactOptionBox.y, compactOptionBox.width, compactOptionBox.height, cnr, cnr);
					g.setStroke(prev);
				}
				break;
			}
			case "NO":
			default: {
				g.setFont(narrowFont(20)); // 18 for 3 buttons// 20 for 2 buttons
				// Advanced settings button
				if (hoverBox == settingsBox || all) {
					String text6 = text("SETUP_BUTTON_SETTINGS");
					int sw6 = g.getFontMetrics().stringWidth(text6);
					int x6 = settingsBox.x+((settingsBox.width-sw6)/2);
					int y6 = settingsBox.y+settingsBox.height-s8;
					Color color = all ? GameUI.borderBrightColor() : Color.yellow;
					drawShadowedString(g, text6, 2, x6, y6, GameUI.borderDarkColor(), color);
					prev = g.getStroke();
					g.setStroke(stroke1);
					g.drawRoundRect(settingsBox.x, settingsBox.y, settingsBox.width, settingsBox.height, cnr, cnr);
					g.setStroke(prev);
				}
				break;
			}
		}

		// Tune Galaxy button
		g.setFont(narrowFont(20)); // 18 for 3 buttons// 20 for 2 buttons
		if (hoverBox == tuneGalaxyBox || all) {
			String text = text("SETUP_BUTTON_TUNE_GALAXY");
			int sw = g.getFontMetrics().stringWidth(text);
			int x = tuneGalaxyBox.x+((tuneGalaxyBox.width-sw)/2);
			int y = tuneGalaxyBox.y+tuneGalaxyBox.height*75/100;
			Color color = all ? GameUI.borderBrightColor() : Color.yellow;
			drawShadowedString(g, text, 2, x, y, GameUI.borderDarkColor(), color);
			prev = g.getStroke();
			g.setStroke(stroke1);
			g.drawRoundRect(tuneGalaxyBox.x, tuneGalaxyBox.y, tuneGalaxyBox.width, tuneGalaxyBox.height, cnr, cnr);
			g.setStroke(prev);
		}

		g.setFont(bigButtonFont());
		// left button
		if (hoverBox == backBox || all) {
			String text = text(backButtonKey());
			int sw = g.getFontMetrics().stringWidth(text);
			int x = backBox.x+((backBox.width-sw)/2);
			int y = backBox.y+backBox.height*75/100;
			Color c = hoverBox == backBox ? Color.yellow : GameUI.borderBrightColor();
			drawShadowedString(g, text, 2, x, y, GameUI.borderDarkColor(), c);
			prev = g.getStroke();
			g.setStroke(stroke1);
			g.drawRoundRect(backBox.x, backBox.y, backBox.width, backBox.height, cnr, cnr);
			g.setStroke(prev);
		}
	}
	private void drawGalaxyShape(Graphics2D g, GalaxyShape sh, int x, int y, int w, int h) {
		// Value in ly
		if (!sh.isValid())
			return;
		float radius = 0;
			if (opts.looseNeighborhood())
				radius = opts.secondRingRadius();
		float fullGw = sh.width()  + 2*radius;
		float fullGh = sh.height() + 2*radius;
		float factor = min(w/fullGw, h/fullGh);

		// Conversion in pixels
		int dispNomW = Math.round(sh.width() * factor);
		int dispNomH = Math.round(sh.height() * factor);
		int xOff = x + Math.round((w - sh.width()*factor) / 2f);
		int yOff = y + Math.round((h - sh.height()*factor) / 2f);
		int grid = max(1, Math.round(galaxyGrid * factor));

		// Work in pixel
		int starSize    = s2;
		int worldsSize  = 0;
		int nearSize    = 0;
		int compSize    = 0;
		int starShift   = s1;
		int worldsShift = 0;
		int nearShift   = 0;
		int compShift   = 0;
		boolean colored = opts.galaxyPreviewColorStarsSize().get() != 0;
		boolean showPlayer = opts.galaxyPreviewPlayer();
		boolean showAI     = opts.galaxyPreviewAI();
		boolean showOrion  = opts.galaxyPreviewOrion();
		if (colored) {
			xOff += starShift;
			yOff += starShift;
			worldsSize  = scaled(opts.galaxyPreviewColorStarsSize().get());
			nearSize    = worldsSize * 3/4;
			compSize    = worldsSize/2;
			worldsShift = worldsSize/2;
			nearShift   = nearSize/2;
			compShift   = compSize/2;
		}
		// Start with grid
		if (showGrid) {
			int xEnd = xOff + dispNomW;
			int yEnd = yOff + dispNomH;
			int lim = dispNomW/(2*grid);
			int ctr = (xOff+xEnd)/2;
			g.setColor(Color.darkGray);
			for (int i=-lim; i<=lim; i++) {
				int xG = ctr + Math.round(i*grid);
				g.drawLine(xG, yOff, xG, yEnd);
			}
			lim = dispNomH/(2*grid);
			ctr = (yOff+yEnd)/2;			
			for (int i=-lim; i<=lim; i++) {
				int yG = ctr + Math.round(i*grid);
				g.drawLine(xOff, yG, xEnd, yG);
			}
			if (showPlayer) {
				// Neighbors circle around player home world
				EmpireSystem player = sh.empireSystems().get(0);
				int x0 = xOff + (int) (player.x(0)*factor);
				int y0 = yOff + (int) (player.y(0)*factor);
				int r1 = Math.round(opts.firstRingRadius() * factor);
				int d1 = r1+r1;
				g.drawRoundRect(x0-r1, y0-r1, d1, d1, d1, d1);
				int r2 = Math.round(opts.secondRingRadius() * factor);
				int d2 = r2+r2;
				g.drawRoundRect(x0-r2, y0-r2, d2, d2, d2, d2);
			}
		}
		// BR: Add Nebulae
		if (colored && previewNebula.get())
			for (Nebula neb : nebulas())
				neb.drawNebulaPreview(g, xOff, yOff, factor);
		// Add with lone stars
		Point.Float pt = new Point.Float();
		for (int i=0; i<sh.numberStarSystems();i++) {
			sh.coords(i, pt);
			int x0 = xOff + (int) (pt.x*factor);
			int y0 = yOff + (int) (pt.y*factor);
			g.setColor(starColor(i));
			if (colored)
				g.fillRoundRect(x0-starShift, y0-starShift, starSize, starSize, starSize, starSize);
			else
				g.fillRect(x0, y0, starSize, starSize);
		}
		// Add Orion over the other stars
		if (colored && showOrion) {
			g.setColor(new Color(64, 64, 255)); // Start with Orion
			sh.coords(0, pt);
			int x0 = xOff + (int) (pt.x*factor);
			int y0 = yOff + (int) (pt.y*factor);
			g.fillRoundRect(x0-worldsShift, y0-worldsShift, worldsSize, worldsSize, worldsSize, worldsSize);
		} 

		// BR: add empires stars to avoid lonely Orion star
		int numCompWorlds = sh.numCompanionWorld();
		int iColor = 0;
		int iEmp   = 0;
		boolean showHomeworld = false;
		if (colored) {
			showHomeworld = showPlayer || showAI;
			if (showPlayer)
				g.setColor(Color.green); // Start with Player
			else
				g.setColor(Color.red); // Player will be red too
		}

		for (EmpireSystem emp : sh.empireSystems()) {
			// Home worlds
			int x0 = xOff + (int) (emp.x(0)*factor);
			int y0 = yOff + (int) (emp.y(0)*factor);
			if (showHomeworld)
				g.fillRoundRect(x0-worldsShift, y0-worldsShift, worldsSize, worldsSize, worldsSize, worldsSize);
			else {
				g.setColor(starColor(iColor));
				iColor++;					
				g.fillRect(x0, y0, starSize, starSize);
			}
			// Near Stars
			for (int iSys=1; iSys<emp.numSystems();iSys++) {
				x0 = xOff + (int) (emp.x(iSys)*factor);
				y0 = yOff + (int) (emp.y(iSys)*factor);
				if (showHomeworld)
					g.fillRoundRect(x0-nearShift, y0-nearShift, nearSize, nearSize, nearSize, nearSize);
				else {
					g.setColor(starColor(iColor));
					iColor++;					
					g.fillRect(x0, y0, starSize, starSize);
				}
			}
			// Companions Worlds
			if (numCompWorlds > 0) {
				for (int iCW=0; iCW<numCompWorlds; iCW++) {
					pt = sh.getCompanion(iEmp, iCW);
					x0 = xOff + (int) (pt.x*factor);
					y0 = yOff + (int) (pt.y*factor);
					if (showHomeworld)
						g.fillRoundRect(x0-compShift, y0-compShift, compSize, compSize, compSize, compSize);
					else {
						g.setColor(starColor(iColor));
						iColor++;					
						g.fillRect(x0, y0, starSize, starSize);
					}
				}
			}
			if (colored) {
				g.setColor(Color.red); // Start with Player, continue with aliens
				showHomeworld = showAI;
			}
			iEmp++;
		}
		// BR: Add Empire distance
		if (showGrid || (hoverBox == galaxyBox)) {
			float  spacing    = opts.galaxyShape().empireBuffer();
			String spacingStr = String.format("%.01f", spacing);
			int    spacingPct = opts.selectedEmpireSpreadingPct();
			String spacingKey = opts.empireSpreadingFactorMapKey();
			String spStr = text(spacingKey, str(spacingPct), spacingStr);
			g.setColor(darkYellow);
			g.setFont(narrowFont(15));
			int xt = galaxyBox.x+s5;
			int yt = galaxyBox.y+galaxyBox.height-s5;
			drawString(g, spStr, xt, yt);
			// Add line reference
			int x1 = galaxyBox.x+galaxyBox.width-s5;
			int x2 = (int) (x1-factor*spacing);
			yt -= s2;
			Stroke prev = g.getStroke();
			g.setStroke(stroke2);
			g.drawLine(x1, yt, x2, yt);
			g.drawLine(x1, yt-s2, x1, yt+s2);
			g.drawLine(x2, yt-s2, x2, yt+s2);
			g.setStroke(prev);
		}
		// BR: Add Galaxy Size
		if (showGrid || (hoverBox == galaxyBox)) {
			String key   = "SETTINGS_MOD_GALAXY_SIZE";
			String gwStr = str(opts.galaxyShape().width());
			String ghStr = str(opts.galaxyShape().height());
			String spStr = text(key, gwStr, ghStr);
			g.setColor(darkYellow);
			g.setFont(narrowFont(15));
			int xt = galaxyBox.x + s5;
			int yt = galaxyBox.y + s15;
			drawString(g, spStr, xt, yt);
		}
	}
	private List<Nebula> nebulas() {
		if (nebulas == null) {
			nebulas = new ArrayList<>(opts.numberNebula());
			opts.galaxyShape().createNebulas(nebulas);
		}
		return nebulas;
	}
	private Color starColor(int i) {
		switch(i % 4) {
			case 0:
			case 1: return Color.lightGray;
			case 2: return Color.gray;
			case 3: return Color.white;
		}
		return Color.gray;
	}
	private BufferedImage playerRaceImg()	{
		if (playerMug == null) {
			String selRace = opts.selectedPlayerRace();
			playerMug = newBufferedImage(new Species(selRace).diploMug());
		}
		return playerMug;
	}
	private boolean isDynamicSize()			{ return opts.selectedGalaxySize().equals(SIZE_DYNAMIC); }
	private boolean isRandomSize()			{ return opts.selectedGalaxySize().equals(SIZE_RANDOM); }
	private boolean isRandomNumAlien()		{ return opts.randomNumAliens(); }
	public	boolean isShapeTextMulti()		{ return opts.galaxyShape().getOption2().contains("MULTI"); }
	public	boolean isShapeTextGalaxy()		{ return AllShapes.isTextShape(opts.selectedGalaxyShape()); }
	public	boolean isShapeBitmapGalaxy()	{ return AllShapes.isBitMapShape(opts.selectedGalaxyShape()); }
	private	String getNameFromPath(String path) {
		File file = new File(path);
		if (file.exists())
			return file.getName();
		return path;
	}
	// ==============================================================
	// Post selection tasks
	// ==============================================================
	private boolean notActive()	{
		if (!Rotp.initialized())
			return true;
		if (loadingOptions())
			return true;
		if (RulesetManager.current().isGameMode())
			return true;
		return !isVisible();
	}
	public	void postGalaxySizeSelection(boolean click) {
		if (notActive())
			return;
		opts = guiOptions();
		if (click) softClick();
		int numOpps = opts.selectedNumberOpponents();
		if(numOpps<0) {
			opts.selectedNumberOpponents(0);
			numOpps = 0;
		}
		int maxOpps = opts.maximumOpponentsOptions();
		if (maxOpps < numOpps)
			opts.selectedNumberOpponents(maxOpps);
		opts.galaxyShape().quickGenerate(); // modnar: do a quickgen to get correct map preview
		backImg = null; // BR: to show/hide system per empire
		nebulas = null;
		postSelectionLight(false);
	}
	public	void postSelectionFull(boolean click) {
		if (notActive())
			return;
		opts = guiOptions();
		if (click)
			softClick();
		if(!GalaxyShape.generating)
			opts.galaxyShape().quickGenerate();
		refreshShapeOptions(opts.galaxyShape().paramList());
		backImg = null;
		nebulas = null;
		postSelectionLight(false);
	}
	public	void postSelectionMedium(boolean click) {
		if (notActive())
			return;
		opts = guiOptions();
		if (click) softClick();
		opts.galaxyShape().quickGenerate();
		refreshShapeOptions(opts.galaxyShape().paramList());
		nebulas = null;
		postSelectionLight(false);
	}
	public	void postSelectionLight(boolean click) {
		if (click)
			softClick();
		loadGuide();
		repaint();
	}
	private void nextOpponentAI(boolean click) {
		opts.selectedOpponentAIOption(opts.nextOpponentAI());
		postSelectionLight(click);
	}
	private void prevOpponentAI(boolean click) {
		opts.selectedOpponentAIOption(opts.prevOpponentAI());
		postSelectionLight(click);
	}
	private void toggleOpponentAI(MouseEvent e) {
		softClick();
		boolean up  = !SwingUtilities.isRightMouseButton(e);
		boolean mid = SwingUtilities.isMiddleMouseButton(e);
		if (mid)
			opts.selectedOpponentAIOption(opponentAI.defaultValue());
		else if (opts.opponentAIOptions().size() >= opts.minListSizePopUp().get()
					|| isCtrlDown())
			selectGlobalAIFromList();
		else if (up)
			opts.selectedOpponentAIOption(opts.nextOpponentAI());
		else
			opts.selectedOpponentAIOption(opts.prevOpponentAI());
		postSelectionLight(false);
	}
	private void toggleNewRaces(boolean click) {
		if (click) softClick();
		showNewRaces.toggle();
		initOpponentGuide();
		postSelectionLight(false);
	}
	private void nextSpecificOpponentAI(int i, boolean click) {
		if (click) softClick();
		if (click || isCtrlDown())
			selectSpecificAIFromList(i);
		else
			opts.nextSpecificOpponentAI(i+1);
	}
	private void prevSpecificOpponentAI(int i, boolean click) {
		if (click) softClick();
		if (click || isCtrlDown())
			selectSpecificAIFromList(i);
		else
			opts.prevSpecificOpponentAI(i+1);
	}
	private void toggleSpecificOpponentAI(int i, boolean click, boolean up, boolean mid) {
		if (click) softClick();
		if (mid)
			opts.specificOpponentAIOption(specificAI.defaultValue(), i+1);
		else if (up)
			nextSpecificOpponentAI(i, false);
		else
			prevSpecificOpponentAI(i, false);
		postSelectionLight(false);
	}
	private void toggleShowAbility(boolean click) {
		if (click) softClick();
		if (click && isCtrlDown()) {
			String defVal = SpecificCROption.defaultSpecificValue().value;
			for (int i=0;i<oppAbilities.length;i++)
				opts.specificOpponentCROption(defVal,i+1);
		}
		else
			useSelectableAbilities.toggle();
		postSelectionLight(false);
	}
	private void toggleOpponent(int i, boolean click, boolean up, boolean mid) {
		if (click) softClick();
		if (mid)
			opts.selectedOpponentRace(i, null);
		else if (up)
			opts.nextOpponent(i);
		else
			opts.prevOpponent(i);
		updateRivalMugs(i);
		postSelectionLight(false);
	}
	private void toggleGalaxyGrid(MouseEvent e) {
		if (SwingUtilities.isMiddleMouseButton(e)) {
			opts.resetEmpireSpreadingFactor();
			backImg = null;
			nebulas = null;
			postSelectionMedium(false);
			return;
		}
		showGrid = !showGrid;
		repaint(galaxyBox);
	}
	private void toggleEmpireSpacing(MouseWheelEvent e) {
		opts.toggleEmpireSpreadingFactor(e);
		postSelectionMedium(false);
	}

	// ==============================================================
	// Actions Methods
	// ==============================================================
	private void goToOptions() {
		buttonClick();
		isOnTop = false;
		AdvancedOptionsUI optionsUI = RotPUI.advancedOptionsUI();
		close();
		optionsUI.init();
	}
	// BR: add UI panel for MOD game options
	@Override protected void close() { 
		super.close();
		forceUpdate = true;
	}
	private void goToMergedStatic() {
		buttonClick();
		isOnTop = false;
		BaseCompactOptionsUI modOptionsUI = RotPUI.setupUI();
		close();
		modOptionsUI.start(GUI_ID, this);
	}
	private void goToMergedDynamic() {
		buttonClick();
		isOnTop = false;
		BaseCompactOptionsUI modOptionsUI = RotPUI.rulesUI();
		close();
		modOptionsUI.start(GUI_ID, this);
	}
	// BR: Add option to return to the main menu
	private void goToMainMenu() {
		buttonClick();
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT: // Restore
			opts.updateAllNonCfgFromFile(LIVE_OPTIONS_FILE);	
			break;
		default:
			opts.saveOptionsToFile(LIVE_OPTIONS_FILE);
			break;
		}
		close();
		RotPUI.instance().selectGamePanel();
	}
	// BR: For restarting with new options
	private void restartGame() {
		Empire.resetPlayerId();
		opts.saveOptionsToFile(LIVE_OPTIONS_FILE);
		starting = true;
		buttonClick();
		repaint();
		GalaxyCopy oldGalaxy = new GalaxyCopy(opts);
		UserPreferences.setForNewGame();
		// Get the old galaxy parameters
		close();
        RotPUI.instance().selectRestartGamePanel(oldGalaxy);
		starting = false;
	}
	public	void startGame() {
		Empire.resetPlayerId();
		opts.saveOptionsToFile(LIVE_OPTIONS_FILE);
		starting = true;
		buttonClick();
		GameUI.gameName = generateGameName(opts);
		UserPreferences.setForNewGame();
		close();
		final Runnable save = () -> {
			long start = System.currentTimeMillis();
			GameSession.instance().startGame(opts);
			RotPUI.instance().mainUI().checkMapInitialized();
			RotPUI.instance().selectIntroPanel();
			log("TOTAL GAME START TIME:" +(System.currentTimeMillis()-start));
			log("Game Name; "+GameUI.gameName);
			starting = false;
			backImg = null;
			nebulas = null;
			playerMug  = null;
		};
		SwingUtilities.invokeLater(save);
	}
	private void goToTuneGalaxyOptions() {
		prevShowGrid = showGrid;
		showGrid = true;
		ParamSubUI subUI = new ParamSubUI( MOD_UI, ADVANCED_SYSTEMS_UI_KEY);
		int x = scaled(20);
		int y = scaled(50);
		int w = scaled(660);
		int h = scaled(530);
		Rectangle rec = new Rectangle(x, y, w, h);
		subUI.hovering(this, rec);
	}

	@Override protected void initBackImg() {
		int w = getWidth();
		int h = getHeight();
		backImg = newOpaqueImage(w, h);
		Graphics2D g = (Graphics2D) backImg.getGraphics();
		setRenderingHints(g);
		// modnar: use (slightly) better upsampling
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		Species species = new Species(opts.selectedPlayerRace());
		if (opts.selectedPlayerIsCustom())
			species.setSpeciesSkills(CustomRaceDefinitions.CUSTOM_RACE_KEY, (DynOptions)null);
		String setupName = species.setupName();

		// background image
		Image back = GameUI.defaultBackground;
		int imgW = back.getWidth(null);
		int imgH = back.getHeight(null);
		g.drawImage(back, 0, 0, w, h, 0, 0, imgW, imgH, this);

		// shade Box Dimensions
		leftBoxX = s80;
		rightBoxX = scaled(665);
		boxW = scaled(505);
		boxY = s95;
		leftBoxH = scaled(615);
		rightBoxH = scaled(605); // BR: was 575
		// draw opponents title
		String title1 = text("SETUP_SELECT_OPPONENTS");
		g.setFont(narrowFont(50));
		int sw1 = g.getFontMetrics().stringWidth(title1);
		int x1 = leftBoxX+((boxW-sw1)/2);
		int y0 = s80;
		drawBorderedString(g, title1, 2, x1, y0, Color.darkGray, Color.white);

		// draw galaxy title
		String title2 = text("SETUP_SELECT_GALAXY");
		g.setFont(narrowFont(50));
		int sw1b = g.getFontMetrics().stringWidth(title2);
		int x1b = rightBoxX+((boxW-sw1b)/2);
		drawBorderedString(g, title2, 2, x1b, y0, Color.darkGray, Color.white);

		// draw opponents shading
		g.setColor(GameUI.setupShade());
		g.fillRect(leftBoxX, boxY, boxW, leftBoxH);

		// draw opponents back gradient
		g.setPaint(GameUI.opponentsSetupBackground());
		g.fillRect(leftBoxX+s20, boxY+s20, boxW-s40, s92);

		// draw race box for player
		BufferedImage backimg = bigBackMug(); 
		int mugW = backimg.getWidth();
		int mugH = backimg.getHeight();
		g.drawImage(backimg, leftBoxX+s25, boxY+s25, this);
		g.drawImage(playerRaceImg(), leftBoxX+s25, boxY+s25, mugW, mugH, this);

		boolean isRandomNumAlien = isRandomNumAlien();
		// draw player vs opponent text
		int x2 = leftBoxX+s25+mugW+s15;
		int y2 = boxY+s25+mugH-s52;
		int yho = isRandomNumAlien? s7 : s5;
		if (isRandomNumAlien)
			g.setFont(narrowFont(24));
		else
			g.setFont(narrowFont(28));
		String header1 = text("SETUP_OPPONENTS_HEADER_1", setupName);
		String header2 = text("SETUP_OPPONENTS_HEADER_2");
		int swHdr = g.getFontMetrics().stringWidth(header1);
		drawBorderedString(g, header1, 1, x2, y2-yho, Color.black, Color.white);

		// draw opponent count box and arrows
		int x2b = x2+swHdr+s5;
		int x2c = x2b;
		
		g.setColor(GameUI.setupFrame());
		if (isRandomNumAlien) {
			int y2c = y2-s13;
			int ySep = 0;
			oppBoxD.reset();
			oppBoxU.reset();
			oppBox.setBounds(0,0,0,0);

			g.setColor(GameUI.setupFrame());
			oppMinBoxD.setAndFill(g, x2b, y2c, ySep);
			oppMinBoxU.setAndFill(g, x2b, y2c, ySep);			

			x2c += s15;
			oppMinBox.setBounds(x2c, y2-s27, s25, s29);
			g.fill(oppMinBox);

			x2c += s27;
			g.setColor(GameUI.buttonBackgroundColor());
			oppRandomBox.setBounds(x2c, y2c-s2, s13, s4);
			g.fill(oppRandomBox);

			x2c += s15;
			g.setColor(GameUI.setupFrame());
			oppMaxBox.setBounds(x2c, y2-s27, s25, s29);
			g.fill(oppMaxBox);

			x2c += s28;
			oppMaxBoxD.setAndFill(g, x2c, y2c, ySep);
			oppMaxBoxU.setAndFill(g, x2c, y2c, ySep);			
		}
		else {
			int y2c = y2-s13;
			int ySep = s6;
			oppMinBoxD.reset();
			oppMinBoxU.reset();
			oppMaxBoxD.reset();
			oppMaxBoxD.reset();
			oppMinBox.setBounds(0,0,0,0);
			oppMaxBox.setBounds(0,0,0,0);
			x2c += s33;
			g.setColor(GameUI.buttonBackgroundColor());
			oppRandomBox.setBounds(x2c, y2c-s2, s14, s4);
			g.fill(oppRandomBox);
			g.setColor(GameUI.setupFrame());
			oppBox.setBounds(x2b, y2-s30, s30, s35);
			g.fill(oppBox);
			oppBoxD.setAndFill(g, x2c, y2c, ySep);
			oppBoxU.setAndFill(g, x2c, y2c, ySep);			
		}

		int x2d = x2c+s20;
		drawBorderedString(g, header2, 1, x2d, y2-yho, Color.black, Color.white);
		
		// align AI and CR selection
		String header3   = text("SETUP_OPPONENT_AI");
		String header3cr = text("SETUP_OPPONENT_CR");
		g.setFont(narrowFont(16));
		int swHdr3   = g.getFontMetrics().stringWidth(header3);
		int swHdr3cr = g.getFontMetrics().stringWidth(header3cr);
		swHdr3 = max(swHdr3, swHdr3cr);

		// draw AI selection
		g.setColor(SystemPanel.blackText);
		int y3 = y2+s47; // BR: up a little
		int x3 = x2;
		drawString(g,header3, x3, y3);
		// int sliderW = s100+s20;
		int sliderW	= s100;
		int sliderH	= s16;
		int sliderX	= x3+swHdr3+s20;
		int sliderYAI = y3-sliderH+s3;
		int dy	= 0;
		g.setColor(GameUI.setupFrame());

		aiBoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
		aiBoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
		aiBox.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
		g.fill(aiBox);

		// draw CR selection
		g.setColor(SystemPanel.blackText);
		int y3cr = y2+s27;
		int x3cr = x2;
		drawString(g,header3cr, x3cr, y3cr);

		int sliderYCR = y3cr-sliderH+s3;
		g.setColor(GameUI.setupFrame());

		abilitiesBoxL.setAndFill(g, sliderX, sliderYCR+dy, sliderH);
		abilitiesBoxR.setAndFill(g, sliderX+sliderW, sliderYCR+dy, sliderH);
		abilitiesBox.setBounds(sliderX, sliderYCR+dy, sliderW, sliderH);
		g.fill(abilitiesBox);

		// Align "New Races selection" and "Show Selectable Abilities"
		int margin	= s40;
		int sep		= s10;
		int side	= leftBoxX + boxW - margin;
		int widthNR = s60;
		int widthSA = s40;
		String headerNR = text("SETUP_NEW_RACES_HEADER");
		String headerSA = text("SETUP_SHOW_ABILITIES_HEADER");
		g.setFont(narrowFont(16));
		int swHdrNR = g.getFontMetrics().stringWidth(headerNR);
		int swHdrSA = g.getFontMetrics().stringWidth(headerSA);
		int x4 = side-sep - max(widthNR+swHdrNR, widthSA+swHdrSA);
		
		// draw New Races selection
		g.setColor(SystemPanel.blackText);
		int yNR = y3;
		drawString(g, headerNR, x4, yNR);
		g.setColor(GameUI.setupFrame());
		int bxNR = side-widthNR;
		newRacesBox.setBounds(bxNR , sliderYAI, widthNR, sliderH);
		g.fill(newRacesBox);

		// draw Show Selectable Abilities
		g.setFont(narrowFont(16));
		g.setColor(SystemPanel.blackText);
		int ySA = y3cr;
		drawString(g, headerSA, x4, ySA);
		g.setColor(GameUI.setupFrame());
		int bxSA = side-widthSA;
		showAbilitiesBox.setBounds(bxSA , sliderYCR, widthSA, sliderH);
		g.fill(showAbilitiesBox);

		// draw galaxy shading
		g.setColor(GameUI.setupShade());
		g.fillRect(rightBoxX, boxY, boxW, rightBoxH);

		// draw galaxy background gradient
		g.setPaint(GameUI.galaxySetupBackground());
		g.fillRect(rightBoxX+s20, boxY+s20, boxW-s40, rightBoxH-s40);
		g.setColor(Color.black);
		galaxyX = rightBoxX+s40;
		galaxyY = boxY+s40;
		galaxyW = boxW-s80;
		galaxyH = scaled(325);
		galaxyBox.setBounds(galaxyX, galaxyY, galaxyW, galaxyH);
		g.fill(galaxyBox);

		initPopupPositions();

		// draw 3 galaxy option labels
		int sectionW = (boxW-s40) / 3;
		int y5 = galaxyY+galaxyH+s45;
		g.setFont(narrowFont(24));
		String shapeLbl = text("SETUP_GALAXY_SHAPE_LABEL");
		int shapeSW = g.getFontMetrics().stringWidth(shapeLbl);
		int x5a = rightBoxX+s20+((sectionW-shapeSW)/2);
		drawBorderedString(g, shapeLbl, 1, x5a, y5, Color.black, Color.white);

		String sizeLbl = text("SETUP_GALAXY_SIZE_LABEL");
		int sizeSW = g.getFontMetrics().stringWidth(sizeLbl);
		int x5b = rightBoxX+s20+sectionW+((sectionW-sizeSW)/2);
		drawBorderedString(g, sizeLbl, 1, x5b, y5, Color.black, Color.white);

		String diffLbl = text("SETUP_GAME_DIFFICULTY_LABEL");
		int diffSW = g.getFontMetrics().stringWidth(diffLbl);
		int x5c = rightBoxX+s20+sectionW+sectionW+((sectionW-diffSW)/2);
		drawBorderedString(g, diffLbl, 1, x5c, y5, Color.black, Color.white);

		sliderW = sectionW*2/3;
		sliderH = s18;
		sliderYAI = y5+s10;
		sliderX = rightBoxX+s20+(sectionW/6);
		dy = 0;
		g.setColor(GameUI.setupFrame());

		shapeBoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
		shapeBoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
		shapeBox.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
		g.fill(shapeBox);

		mapOption1BoxL.reset();
		mapOption1BoxR.reset();
		mapOption1Box.setBounds(0,0,0,0);
		if (shapeOptionsList().size() > 0) {
			dy = s20;
			mapOption1BoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
			mapOption1BoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
			mapOption1Box.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
			g.fill(mapOption1Box);
		}

		mapOption2BoxL.reset();
		mapOption2BoxR.reset();
		mapOption2Box.setBounds(0,0,0,0);
		if (shapeOptionsList().size() > 1) {
			dy = s40;
			mapOption2BoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
			mapOption2BoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
			mapOption2Box.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
			g.fill(mapOption2Box);
		}
		mapOption3BoxL.reset();
		mapOption3BoxR.reset();
		mapOption3Box.setBounds(0,0,0,0);
		if (shapeOptionsList().size() > 2) {
			dy = s60;
			if (isShapeBitmapGalaxy()) {
				mapOption3Box.setBounds(sliderX, sliderYAI+dy, sliderW+2*sectionW, sliderH);
				g.fill(mapOption3Box);
			}
			else {
				mapOption3BoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
				mapOption3BoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
				mapOption3Box.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
				g.fill(mapOption3Box);
			}
		}
		mapOption4BoxL.reset();
		mapOption4BoxR.reset();
		mapOption4Box.setBounds(0,0,0,0);
		if (shapeOptionsList().size() > 3) {
			dy = s80;
			mapOption4BoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
			mapOption4BoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
			mapOption4Box.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
			g.fill(mapOption4Box);
		}

		sliderX += sectionW;
		dy = 0;
		sizeBoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
		sizeBoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
		sizeBox.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
		g.fill(sizeBox);

		sizeMinBoxL.reset();
		sizeMinBoxR.reset();
		sizeMaxBoxL.reset();
		sizeMaxBoxR.reset();
		sizeOptionBoxL.reset();
		sizeOptionBoxR.reset();
		sizeMinBox.setBounds(0,0,0,0);
		sizeMaxBox.setBounds(0,0,0,0);
		sizeOptionBox.setBounds(0,0,0,0);
		if (isDynamicSize()) {
			dy = s20;
			sizeOptionBoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
			sizeOptionBoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
			sizeOptionBox.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
			g.fill(sizeOptionBox);
		}
		else if (isRandomSize()) {
			dy = s20;
			sizeMinBoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
			sizeMinBoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
			sizeMinBox.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
			g.fill(sizeMinBox);
			dy = s40;
			sizeMaxBoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
			sizeMaxBoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
			sizeMaxBox.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
			g.fill(sizeMaxBox);
		}
		sliderX += sectionW;
		dy = 0;
		diffBoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
		diffBoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
		diffBox.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
		g.fill(diffBox);

		dy = s20;
		wysiwygBoxL.setAndFill(g, sliderX, sliderYAI+dy, sliderH);
		wysiwygBoxR.setAndFill(g, sliderX+sliderW, sliderYAI+dy, sliderH);
		wysiwygBox.setBounds(sliderX, sliderYAI+dy, sliderW, sliderH);
		g.fill(wysiwygBox);

		// draw settings button
		int smallButtonH = s27; // 27 for 3 buttons // 30 for 2 buttons
		int smallButtonW = scaled(160); // 150 for 3 buttons // 180 for 2 buttons
		// BR: buttons positioning
		// Tune galaxy button
		int yb = 610;
		int xb = 700;
		int dx = 145;
		dy = 30;
		//smallButtonW = scaled(180);
		smallButtonH = s30;
		g.setPaint(GameUI.buttonBackgroundColor());
		tuneGalaxyBox.setBounds(scaled(xb), scaled(yb+dy), smallButtonW, smallButtonH);
		tuneGalaxyBox.fillButtonFullImg(g);

		switch (opts.compactOptionOnly().get().toUpperCase()) {
			case "YES": {
				int bw = 110;
				yb = 610; // 615 for 3 buttons (1 row) // 610 for 2 buttons
				xb = 1140 - bw;
				dx = bw+10; // 145 for 3 buttons // 200 for 2 buttons 1 row // 241 for centered
				dy = 30; // for 1 row
				smallButtonW = scaled(bw); // 150 for 3 buttons // 180 for 2 buttons
				smallButtonH = s30; // 27 for 3 buttons // 30 for 2 buttons
				// draw Merged settings button
				g.setPaint(GameUI.buttonBackgroundColor());
				compactSetupBox.setBounds(scaled(xb), scaled(yb+dy), smallButtonW, smallButtonH);
				compactSetupBox.fillButtonFullImg(g);
				// draw Merged settings button
				compactOptionBox.setBounds(scaled(xb-dx), scaled(yb+dy), smallButtonW, smallButtonH);
				compactOptionBox.fillButtonFullImg(g);
				settingsBox.setBounds(0, 0, 0, 0);
				break;
				}
			case "NO":
			default: {
				yb = 610; // 615 for 3 buttons (1 row) // 610 for 2 buttons
				xb = 960; // 984 for 3 buttons // 960 for 2 buttons 1 row // 948 for centered
				dx = 200; // 145 for 3 buttons // 200 for 2 buttons 1 row // 241 for centered
				dy = 30; // for 1 row
				smallButtonW = scaled(180); // 150 for 3 buttons // 180 for 2 buttons
				smallButtonH = s30; // 27 for 3 buttons // 30 for 2 buttons
				g.setPaint(GameUI.buttonLeftBackground());
				settingsBox.setBounds(scaled(xb), scaled(yb+dy), smallButtonW, smallButtonH);
				settingsBox.fillButtonFullImg(g);
				compactSetupBox.setBounds(0, 0, 0, 0);
				compactOptionBox.setBounds(0, 0, 0, 0);
				break;
				}
		}

		int buttonH = s45;
		int buttonW = scaled(220);
		int yB = 685+10; // 2 Button's Rows Offset, was 685
		xb = 950; // was 950
		dx = 241;
		// draw START button
		exitBox.setBounds(scaled(xb), scaled(yB), buttonW, buttonH);

		// draw BACK button
		xb -= dx;
		g.setPaint(GameUI.buttonLeftBackground());
		backBox.setBounds(scaled(xb), scaled(yB), buttonW, buttonH);
		backBox.fillButtonFullImg(g);

		// draw DEFAULT button
		buttonH = s30;
		buttonW = defaultButtonWidth(g);
		yb = scaled(yB+15);
		xb = scaled(xb)-buttonW-buttonSep;
		g.setPaint(GameUI.buttonLeftBackground());
		defaultBox.setBounds(xb, yb, buttonW, buttonH);
		defaultBox.fillButtonFullImg(g);

		// draw LAST button
		buttonW = lastButtonWidth(g);
		xb -= (buttonW + buttonSep);
		lastBox.setBounds(xb, yb, buttonW, buttonH);

		// draw USER button
		buttonW = userButtonWidth(g);
		xb -= (buttonW + buttonSep);
		userBox.setBounds(xb, yb, buttonW, buttonH);

		// draw GUIDE button
		buttonW = guideButtonWidth(g);
		xb = s20;
		guideBox.setBounds(xb, yb, buttonW, buttonH);

		drawFixButtons(g, true);
		initButtonBackImg();
		g.dispose();
	}
	@Override public String ambienceSoundKey()		{ return GameUI.AMBIENCE_KEY; }
	@Override public void keyPressed(KeyEvent e)	{
		super.keyPressed(e);
		switch(e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
			doBackBoxAction();
			return;
		case KeyEvent.VK_ENTER:
			doStartBoxAction();
			return;
    	case KeyEvent.VK_B:
    		opts.compactOptionOnly().next();
    		clearImages();
    		initBackImg();
    		repaint();
            return;
		case KeyEvent.VK_F:
			if (!e.isAltDown())
				noFogChanged();
			return;
		case KeyEvent.VK_M: // BR: "M" = Go to Main Menu
			if (!e.isAltDown())
				goToMainMenu();
			return;
		case KeyEvent.VK_T:
			goToTuneGalaxyOptions();
			return;
		default:
			return;
		}
	}
	@Override public void mouseReleased(MouseEvent e) { // BR: added full mouse control
		if (e.getButton() > 3)
			return;
		if (hoverBox == null && hoverPolyBox == null)
			return;
		if (hoverBox == helpBox) {
			if (SwingUtilities.isRightMouseButton(e))
				showHotKeys();
			else
				showHelp();
			return;
		}
		boolean up  = !SwingUtilities.isRightMouseButton(e);
		boolean mid = SwingUtilities.isMiddleMouseButton(e);
		if (hoverBox == backBox)
			doBackBoxAction();
		else if (hoverBox == defaultBox)
			doDefaultBoxAction();
		else if (hoverBox == guideBox)
			doGuideBoxAction();
		else if (hoverBox == userBox)
			doUserBoxAction();
		else if (hoverBox == lastBox)
			doLastBoxAction();
		else if (hoverBox == settingsBox)
			goToOptions();
		else if (hoverBox == tuneGalaxyBox)
			goToTuneGalaxyOptions();
		else if (hoverBox == compactSetupBox)
			goToMergedStatic();
		else if (hoverBox == compactOptionBox)
			goToMergedDynamic();
		else if (hoverBox == galaxyBox)
			toggleGalaxyGrid(e);
		else if (hoverBox == exitBox)
			doStartBoxAction();
		else if (hoverPolyBox == shapeBoxL)
			shapeSelection.prev();
		else if (hoverBox == shapeBox)
			shapeSelection.toggle(e, this);
		else if (hoverPolyBox == shapeBoxR)
			shapeSelection.next();
		else if (hoverPolyBox == mapOption1BoxL)
			opts.galaxyShape().paramOption1().prev();
		else if (hoverBox == mapOption1Box)
			opts.galaxyShape().paramOption1().toggle(e, this);
		else if (hoverPolyBox == mapOption1BoxR)
			opts.galaxyShape().paramOption1().next();
		else if (hoverPolyBox == mapOption2BoxL)
			opts.galaxyShape().paramOption2().prev();
		else if (hoverBox == mapOption2Box)
			opts.galaxyShape().paramOption2().toggle(e, this);
		else if (hoverPolyBox == mapOption2BoxR)
			opts.galaxyShape().paramOption2().next();
		else if (hoverPolyBox == mapOption3BoxL)
			opts.galaxyShape().paramOption3().prev();
		else if (hoverBox == mapOption3Box)
			opts.galaxyShape().paramOption3().toggle(e, this);
		else if (hoverPolyBox == mapOption3BoxR)
			opts.galaxyShape().paramOption3().next();
		else if (hoverPolyBox == mapOption4BoxL)
			opts.galaxyShape().paramOption4().prev();
		else if (hoverBox == mapOption4Box)
			opts.galaxyShape().paramOption4().toggle(e, this);
		else if (hoverPolyBox == mapOption4BoxR)
			opts.galaxyShape().paramOption4().next();
		else if (hoverPolyBox == sizeBoxL)
			sizeSelection.prev();
		else if (hoverBox == sizeBox)
			sizeSelection.toggle(e, this);
		else if (hoverPolyBox == sizeBoxR)
			sizeSelection.next();
		else if (hoverPolyBox == sizeMinBoxL) {
			randomNumStarsLim1.prev();
			postSelectionMedium(true);
		}
		else if (hoverBox == sizeMinBox) {
			randomNumStarsLim1.toggle(e, this);
			postSelectionMedium(true);
		}
		else if (hoverPolyBox == sizeMinBoxR) {
			randomNumStarsLim1.next();
			postSelectionMedium(true);
		}
		else if (hoverPolyBox == sizeMaxBoxL) {
			randomNumStarsLim2.prev();
			postSelectionMedium(true);
		}
		else if (hoverBox == sizeMaxBox) {
			randomNumStarsLim2.toggle(e, this);
			postSelectionMedium(true);
		}
		else if (hoverPolyBox == sizeMaxBoxR) {
			randomNumStarsLim2.next();
			postSelectionMedium(true);
		}
		else if (hoverPolyBox == sizeOptionBoxL) {
			opts.dynStarsPerEmpire().prev(e);
			opts.dynStarsPerEmpire().set(opts.dynStarsPerEmpire().getValidValue());
		}
		else if (hoverBox == sizeOptionBox) {
			opts.dynStarsPerEmpire().toggle(e, (MouseWheelEvent)null, this);
			postSelectionMedium(true);
		}
		else if (hoverPolyBox == sizeOptionBoxR) {
			opts.dynStarsPerEmpire().next(e);
			opts.dynStarsPerEmpire().set(opts.dynStarsPerEmpire().getValidValue());
		}
		else if (hoverPolyBox == aiBoxL)
			prevOpponentAI(true);
		else if (hoverBox == aiBox)
			toggleOpponentAI(e);
		else if (hoverPolyBox == aiBoxR)
			nextOpponentAI(true);
		else if (hoverPolyBox == abilitiesBoxL)
			globalAbilities.prev();
		else if (hoverBox == abilitiesBox)
			globalAbilities.toggle(e, this);
		else if (hoverPolyBox == abilitiesBoxR)
			globalAbilities.next();
		else if (hoverBox == newRacesBox)
			toggleNewRaces(true);
		else if (hoverBox == showAbilitiesBox)
			toggleShowAbility(true);
		else if (hoverPolyBox == diffBoxL)
			difficultySelection.prev();
		else if (hoverBox == diffBox)
			difficultySelection.toggle(e, this);
		else if (hoverPolyBox == diffBoxR)
			difficultySelection.next();
		else if (hoverPolyBox == wysiwygBoxL)
			galaxyRandSource.prev();
		else if (hoverBox == wysiwygBox)
			galaxyRandSource.toggle(e, this);
		else if (hoverPolyBox == wysiwygBoxR)
			galaxyRandSource.next();
		else if (hoverPolyBox == oppBoxU)
			aliensNumber.next();
		else if (hoverBox == oppBox)
			aliensNumber.toggle(e, this);
		else if (hoverPolyBox == oppBoxD)
			aliensNumber.prev();
		else if (hoverPolyBox == oppMinBoxU) {
			randomNumAliensLim1.next();
			postSelectionFull(true);
		}
		else if (hoverBox == oppMinBox) {
			randomNumAliensLim1.toggle(e, this);
			postSelectionFull(true);
		}
		else if (hoverPolyBox == oppMinBoxD) {
			randomNumAliensLim1.prev();
			postSelectionFull(true);
		}
		else if (hoverPolyBox == oppMaxBoxU) {
			randomNumAliensLim2.next();
			postSelectionFull(true);
		}
		else if (hoverBox == oppMaxBox) {
			randomNumAliensLim2.toggle(e, this);
			postSelectionFull(true);
		}
		else if (hoverPolyBox == oppMaxBoxD) {
			randomNumAliensLim2.prev();
			postSelectionFull(true);
		}
		else if (hoverBox == oppRandomBox) {
			randomNumAliens.toggle(e, this);
			postSelectionFull(true);
		}
		else {
			for (int i=0;i<oppSet.length;i++) {
				if (hoverBox == oppAI[i]) {
					toggleSpecificOpponentAI(i, true, up, mid);
					break;
				}
				else if (hoverBox == oppAbilities[i]) {
					specificAbilities.toggle(e, this);
					break;
				}
				else if (hoverBox == oppSet[i]) {
					if (mid && e.isControlDown()) {
						for (int k=i; k<MAX_DISPLAY_OPPS; k++)
							toggleOpponent(k, true, up, mid);
					}
					else if (up && e.isShiftDown()) {
						for (int k=i; k<MAX_DISPLAY_OPPS; k++)
							if (opts.selectedOpponentRace(k) == null)
								toggleOpponent(k, true, up, mid);
					}
					else
						toggleOpponent(i, true, up, mid);
					break;
				}
			}
		}
	}
	@Override public void mouseWheelMoved(MouseWheelEvent e) {
		boolean up = e.getWheelRotation() > 0;
		if (hoverBox == shapeBox)
			shapeSelection.toggle(e);
		else if (hoverBox == galaxyBox)
			toggleEmpireSpacing(e);
		else if (hoverBox == mapOption1Box)
			opts.galaxyShape().paramOption1().toggle(e);
		else if (hoverBox == mapOption2Box)
			opts.galaxyShape().paramOption2().toggle(e);
		else if (hoverBox == mapOption3Box)
			opts.galaxyShape().paramOption3().toggle(e);
		else if (hoverBox == mapOption4Box)
			opts.galaxyShape().paramOption4().toggle(e);
		else if (hoverBox == sizeBox)
			sizeSelection.toggle(e);
		else if (hoverBox == sizeMinBox) {
			randomNumStarsLim1.toggle(e);
			postSelectionMedium(false);
		}
		else if (hoverBox == sizeMaxBox) {
			randomNumStarsLim2.toggle(e);
			postSelectionMedium(false);
		}
		else if (hoverBox == sizeOptionBox)
			opts.dynStarsPerEmpire().toggle((MouseEvent)null, e, this);
		else if (hoverBox == aiBox)
			if (up)
				prevOpponentAI(false);
			else
				nextOpponentAI(false);
		else if (hoverBox == abilitiesBox)
			if (up)
				globalAbilities.prev();
			else
				globalAbilities.next();
		else if (hoverBox == newRacesBox)
			toggleNewRaces(false);
		else if (hoverBox == showAbilitiesBox)
			toggleShowAbility(false);
		else if (hoverBox == diffBox)
			difficultySelection.toggle(e);
		else if (hoverBox == wysiwygBox)
			galaxyRandSource.toggle(e);
		else if (hoverBox == oppBox)
			aliensNumber.toggle(e);
		else if (hoverBox == oppMinBox) {
			randomNumAliensLim1.toggle(e);
			postSelectionFull(false);
		}
		else if (hoverBox == oppMaxBox) {
			randomNumAliensLim2.toggle(e);
			postSelectionFull(false);
		}
		else if (hoverBox == oppRandomBox) {
			randomNumAliens.toggle(e);
			postSelectionFull(false);
		}
		else {
			for (int i=0;i<oppAI.length;i++) {
				if (hoverBox == oppAI[i]) {
					toggleSpecificOpponentAI(i, false, up, false);
					return;
				}
			}
			for (int i=0;i<oppAbilities.length;i++) {
				if (hoverBox == oppAbilities[i]) {
					specificAbilities.toggle(e);
					return;
				}
			}
			for (int i=0;i<oppSet.length;i++) {
				if (hoverBox == oppSet[i]) {
					toggleOpponent(i, false, up, false);
					return;
				}
			}
		}
	}

	private class ParamListOpponentAI extends ParamList { // For Guide
		private ParamListOpponentAI(String gui, String name, List<String> list, String defaultValue) {
			super(gui, name, list, defaultValue);
			setDefaultValue(MOO1_DEFAULT, baseAI.aliensKey);
			setDefaultValue(ROTP_DEFAULT, baseAI.aliensKey);
			isDuplicate(true);
		}
		@Override public String	getOptionValue(IGameOptions options)	{
			return options.selectedOpponentAIOption();
		}
		@Override public String	guideValue()	{ return langLabel(get()); }
		@Override public void reInit(List<String> list) {
			if (list == null)
				super.reInit(IGameOptions.globalAIset().getAliens());
			else
				super.reInit(list);
		}
		@Override public String setFromIndex(int index) {
			String value = super.setFromIndex(index);
			opts.selectedOpponentAIOption(value);
			return value;
		}
		@Override public String set(String value) {
			super.set(value);
			opts.selectedOpponentAIOption(value);
			return value;
		}
		@Override public boolean prev() {
			prevOpponentAI(true);
			return false;
		}
		@Override public boolean next() {
			nextOpponentAI(true);
			return false;
		}
	};
	private class ParamListSpecificAI extends ParamList { // For Guide
		ParamListSpecificAI(String gui, String name, List<String> list, String defaultValue) {
			super(gui, name, list, defaultValue);
			isDuplicate(true);
		}
		@Override public String	getOptionValue(IGameOptions options)	{
			return options.specificOpponentAIOption(mouseBoxIndex()+1);
		}
		@Override public String	guideValue()	{ return langLabel(get()); }
		@Override public void reInit(List<String> list) {
			if (list == null)
				super.reInit(IGameOptions.specificAIset().getAliens());
			else
				super.reInit(list);
		}
	};
	private class ParamListSpecificOpponent extends ParamList { // For Guide
		ParamListSpecificOpponent(String gui, String name, List<String> list, String defaultValue) {
			super(gui, name, list, defaultValue);
			isDuplicate(true);
		}
		@Override public void reInit(List<String> list) {
			if (list == null)
				super.reInit(guiOptions().allRaceOptions());
			else
				super.reInit(list);
		}
		@Override public String	getOptionValue(IGameOptions options)	{
			String val = options.selectedOpponentRace(mouseBoxIndex());
			if (val == null)
				return opponentRandom;
			return val;
		}
		@Override public String	guideValue()	{
			String key = get();
			if (key == null || key.equals(opponentRandom))
				return "Random";
			return Species.getSpeciesName(key); 
		}
		@Override public String getRowGuide(int id)	{
			// System.out.println("id = " + id + " " + this.getGuiValue(id));
			String key, help;
			key = getGuiValue(id);
			if (key == null || key.equals(opponentRandom))
				help = labelFormat(opponentRandom) + "Surprise me!";
			else {
				Species species	= new Species(key);
				String raceName = species.setupName();
				help = labelFormat(raceName) + species.getDescription(1)
						+ "<br>" + species.getDescription(2)
						+ "&ensp /&ensp " + species.getDescription(3)
						+ "&ensp /&ensp " + species.getDescription(4);
			}
			return help;
		}
		@Override public String valueGuide(int id)	{
			// System.out.println("id = " + id + " " + this.getGuiValue(id));
			String key, help;
			key = getGuiValue(id);
			if (key == null || key.equals(opponentRandom))
				help = "Surprise me!";
			else {
				Species species	= new Species(key);
				String raceName = species.setupName();
				help = labelFormat(raceName) + species.getDescription(1)
						+ "<br>" + species.getDescription(2)
						+ "<br>" + species.getDescription(3)
						+ "<br>" + species.getDescription(4);
			}
			return tableFormat(help);
		}
	};
	// Bitmap shape Properties Change Listener
	private class BMPropertyChangeListener implements PropertyChangeListener {
		private final JFileChooser bmFileChooser;
		private BMPropertyChangeListener(JFileChooser fileChooser) {
			bmFileChooser = fileChooser;
		}
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (bmFileChooser.getSelectedFile() != null) {
				preview(bmFileChooser.getSelectedFile().getPath(), opts.galaxyShape().paramOption3());
			}
		}
	}
}
