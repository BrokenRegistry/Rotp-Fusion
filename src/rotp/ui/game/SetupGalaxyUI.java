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
import static rotp.model.empires.CustomRaceDefinitions.BASE_RACE_MARKER;
import static rotp.model.empires.CustomRaceDefinitions.fileToAlienRace;
import static rotp.model.empires.CustomRaceDefinitions.getAllowedAlienRaces;
import static rotp.model.empires.CustomRaceDefinitions.getBaseRaceList;
import static rotp.model.game.DefaultValues.MOO1_DEFAULT;
import static rotp.model.game.DefaultValues.ROTP_DEFAULT;
import static rotp.model.game.IBaseOptsTools.BASE_UI;
import static rotp.model.game.IBaseOptsTools.LIVE_OPTIONS_FILE;
import static rotp.model.game.IBaseOptsTools.MOD_UI;
import static rotp.ui.UserPreferences.GALAXY_TEXT_FILE;
import static rotp.ui.util.IParam.LABEL_DESCRIPTION;
import static rotp.ui.util.IParam.labelFormat;
import static rotp.ui.util.IParam.langLabel;
import static rotp.ui.util.IParam.realLangLabel;
import static rotp.ui.util.IParam.rowFormat;
import static rotp.ui.util.IParam.tableFormat;

// modnar: needed for adding RenderingHints
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import rotp.Rotp;
import rotp.model.ai.AIList;
import rotp.model.empires.CustomRaceDefinitions;
import rotp.model.empires.Empire;
import rotp.model.empires.Race;
import rotp.model.galaxy.GalaxyFactory.GalaxyCopy;
import rotp.model.galaxy.GalaxyShape;
import rotp.model.galaxy.GalaxyShape.EmpireSystem;
import rotp.model.galaxy.Nebula;
import rotp.model.game.GameSession;
import rotp.model.game.IGameOptions;
import rotp.model.game.IInGameOptions;
import rotp.model.game.SafeListPanel;
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
import rotp.ui.util.ParamTitle;
import rotp.ui.util.SpecificCROption;
import rotp.util.FontManager;
import rotp.util.LabelManager;
import rotp.util.ModifierKeysState;

public final class SetupGalaxyUI  extends BaseModPanel implements MouseWheelListener {
	private static final long serialVersionUID = 1L;
    // public  static final String guiTitleID	= "SETUP_GALAXY";
	private	static final String GUI_ID       = "START_GALAXY";
	private static final String BACK_KEY	 = "SETUP_BUTTON_BACK";
	private static final String RESTART_KEY	 = "SETUP_BUTTON_RESTART";
	private static final String START_KEY	 = "SETUP_BUTTON_START";
	private static final String SIZE_OPT_KEY = "SETUP_GALAXY_SIZE_STAR_PER_EMPIRE";
	private static final String NO_SELECTION = "SETUP_BITMAP_NO_SELECTION";
	private static final String SPECIFIC_AI  = "SETUP_SPECIFIC_AI";
	private static final String GLOBAL_AI    = "SETUP_GLOBAL_AI";
	private static final String SPECIFIC_ABILITY = "SETUP_SPECIFIC_ABILITY";
	private static final String GLOBAL_ABILITIES = "SETUP_GLOBAL_ABILITY";
	private static final String OPPONENT_RANDOM	 = "SETUP_OPPONENT_RANDOM";
	private	static final String LANG_LIST_KEY    = "LIST_DIALOG_";
	private static final String ADVANCED_SYSTEMS_GUI_ID	= "ADVANCED_SYSTEMS_OPTIONS";
 	private static final int    buttonFont		= 30;
	private	static final Font   bigButtonFont	= FontManager.current().narrowFont(buttonFont);
	public  static final int	MAX_DISPLAY_OPPS = 49;
	private static String opponentRandom = "???";
	private static SetupGalaxyUI instance;
    private final Color darkBrownC = new Color(112,85,68);
	private final Color darkYellow = new Color(223, 223, 0);

	@Override protected ParamButtonHelp newExitButton() {
		return new ParamButtonHelp(
			"SETUP_START",
			START_KEY,
			"",
			RESTART_KEY,
			"");
	}
	public final  ParamListOpponentAI opponentAI	= new ParamListOpponentAI( // For Guide
			BASE_UI, "OPPONENT_AI",
			IGameOptions.globalAIset().getAliens(),
			IGameOptions.defaultAI.aliensKey);
	private final ParamListSpecificAI specificAI	= new ParamListSpecificAI( // For Guide
			BASE_UI, "SPECIFIC_AI",
			IGameOptions.specificAIset().getAliens(),
			IGameOptions.defaultAI.aliensKey);
	private final ParamListSpecificOpponent specificOpponent	= new ParamListSpecificOpponent( // For Guide
			BASE_UI, "SPECIFIC_OPPONENT", guiOptions().allRaceOptions(), opponentRandom);
    public  final ParamListGlobalAbilities  globalAbilities		= new ParamListGlobalAbilities(  // For Guide
			BASE_UI, "GLOBAL_ABILITY", globalAbilitiesList,
			SpecificCROption.BASE_RACE.value);
	private final ParamListSpecificAbilities specificAbilities	= new ParamListSpecificAbilities( // For Guide
			BASE_UI, "SPECIFIC_ABILITY", specificAbilitiesList,
			SpecificCROption.defaultSpecificValue().value);
	private boolean popupPositionsInitialised = false;

	private Box tuneGalaxyBox		= new Box("SETUP_TUNE_GALAXY_OPTIONS"); // BR add UI panel for MOD game options
	private Box compactSetupBox		= new Box("SETUP_GALAXY_COMPACT_OPTIONS"); // BR add UI panel for MOD game options
	private Box compactOptionBox	= new Box("SETUP_GALAXY_COMPACT_OPTIONS"); // BR add UI panel for MOD game options
	private Box	settingsBox			= new Box("SETUP_GALAXY_CLASSIC_OPTIONS");
	private Box	backBox				= new Box("SETUP_GALAXY_BACK");
	private Box	galaxyBox			= new Box("SETUP_GALAXY_PREVIEW");
	private Box	newRacesBox			= new Box("SETUP_GALAXY_RACE_LIST"); // BR:
	private Box	showAbilitiesBox; // BR:
	private Box		shapeBox;
	private Polygon	shapeBoxL		= new PolyBox();
	private Polygon	shapeBoxR		= new PolyBox();
	private Box		mapOption1Box;
	private Polygon	mapOption1BoxL	= new PolyBox();
	private Polygon	mapOption1BoxR	= new PolyBox();			 
	private Box		mapOption2Box;
	private Polygon	mapOption2BoxL	= new PolyBox();
	private Polygon	mapOption2BoxR	= new PolyBox();			 
	private Box		mapOption3Box; // BR:
	private Box		mapOption4Box;
	private Polygon	mapOption4BoxL	= new PolyBox();
	private Polygon	mapOption4BoxR	= new PolyBox();			 
	private Box		sizeOptionBox; // BR:
	private Polygon	sizeOptionBoxL	= new PolyBox();   // BR:
	private Polygon	sizeOptionBoxR	= new PolyBox();   // BR:
	private Box		sizeBox;
	private Polygon	sizeBoxL		= new PolyBox();
	private Polygon	sizeBoxR		= new PolyBox();
	private Box		diffBox;
	private Polygon	diffBoxL		= new PolyBox();
	private Polygon	diffBoxR		= new PolyBox();
	private Box		wysiwygBox; // BR:
	private Polygon	wysiwygBoxL		= new PolyBox();   // BR:
	private Polygon	wysiwygBoxR		= new PolyBox();   // BR:
	private Box		oppBox;
	private Polygon	oppBoxU			= new PolyBox();
	private Polygon	oppBoxD			= new PolyBox();
	private Box		aiBox			= new Box(opponentAI);
	private Polygon	aiBoxL			= new PolyBox();
	private Polygon	aiBoxR			= new PolyBox();
	private Box		abilitiesBox	= new Box(globalAbilities); // dataRace selection
	private Polygon	abilitiesBoxL	= new PolyBox(); // BR:
	private Polygon	abilitiesBoxR	= new PolyBox(); // BR:

	private Box[]	oppSet			= new Box[MAX_DISPLAY_OPPS];
	private Box[]	oppAI			= new Box[MAX_DISPLAY_OPPS];
	private Box[]	oppAbilities	= new Box[MAX_DISPLAY_OPPS]; // BR: dataRace selection

	private boolean starting = false;
	private int leftBoxX, rightBoxX, boxW, boxY, leftBoxH, rightBoxH;
	private int galaxyX, galaxyY, galaxyW, galaxyH;
	private static final LinkedList<String> specificAbilitiesList = new LinkedList<>();; 
	private static final LinkedList<String> globalAbilitiesList   = new LinkedList<>();; 
	private String[] specificAbilitiesArray; 
	private String[] globalAbilitiesArray; 
	private String[] galaxyTextArray;
    private Font dialogMonoFont;
    private int  dialogMonoFontSize = 20;

    private int  galaxyGrid  = 10;
    private boolean showGrid = false;
    private boolean prevShowGrid = false;

    private int wSmallMug	= s54;
    private int hSmallMug 	= s58;
    private int rShSmallMug	= s56;

    private int wBigMug		= s76;
    private int hBigMug		= s82;
    private int rShBigMug	= s78;

	private BufferedImage playerMug;
	private BufferedImage smallBackMug;
	private BufferedImage bigBackMug;
	private BufferedImage smallNullMug;
	private BufferedImage bigNullMug;

	private IGameOptions opts;
	private boolean forceUpdate = true;
	private List<Nebula> nebulas;

    // Local copy of the good sized race Mug, to avoid depending SetupRaceUI
    private BufferedImage[] bigOppMugs;
    private BufferedImage[] smallOppMugs;

    // Buttons Parameters
    private int buttonSep	= s15;
    private Box	helpBox		= new Box("SETTINGS_BUTTON_HELP");
	@Override protected Box newExitBox()		{ return new Box(newExitButton()); }
	@Override protected Font bigButtonFont()					{ return bigButtonFont; }
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
    private static int mouseBoxIndex() { return instance.hoverBox.mouseBoxIndex(); }

	private Font dialogMonoFont() {
    	if (dialogMonoFont == null)
    		dialogMonoFont = galaxyFont(scaled(dialogMonoFontSize));
    	return dialogMonoFont;
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
		list.addAll(opts.getNewRacesOnOffList());
		list.add(opponentRandom); // For Random (???)
		specificOpponent.reInit(list);
		specificOpponent.defaultValue(opponentRandom);
	}
	private void initAIandAbilitiesList() {
		initOpponentGuide();
		// specific Abilities
		specificAbilitiesList.clear();
		specificAbilitiesList.addAll(SpecificCROption.options());
		specificAbilitiesList.removeLast(); // The blank one (USER_CHOICE)
		specificAbilitiesList.addAll(getAllowedAlienRaces());
		specificAbilitiesList.addAll(getBaseRaceList());
		specificAbilities.reInit(specificAbilitiesList);
		specificAbilitiesArray = specificAbilitiesList.toArray(new String[specificAbilitiesList.size()]);
		// global Abilities
		globalAbilitiesList.clear();
		globalAbilitiesList.addAll(opts.globalCROptions().getBaseOptions());
		globalAbilitiesList.addAll(getAllowedAlienRaces());
		globalAbilitiesList.addAll(getBaseRaceList());
		globalAbilities.reInit(globalAbilitiesList);
		globalAbilitiesArray = globalAbilitiesList.toArray(new String[globalAbilitiesList.size()]);
	}
	private SafeListPanel advancedSystemMap()	{
		SafeListPanel map = new SafeListPanel("ADVANCED_SYSTEM");
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("NEBULAE_OPTION"),
				opts.getNebula(),
				opts.getNebulaPlacing(),
				opts.getRealNebulaSize(),
				opts.getRealNebulaShape(),
				opts.previewNebula(),

				opts.headerSpacer(),
				new ParamTitle("GALAXY_SHAPE"),
				opts.shapeSelection(),
				opts.shapeOption1(),
				opts.shapeOption2(),
				opts.shapeOption3(),
				opts.shapeOption4(),
				opts.shapeLineSpacing(),

				opts.headerSpacer(),
				new ParamTitle("LINKED_OPTIONS"),
				opts.starDensity(),
				opts.sizeSelection(),
				opts.dynStarsPerEmpire(),
				opts.aliensNumber()
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("HOMEWORLD_NEIGHBORHOOD"),
				opts.getFirstRingSystemNumber(),
				opts.getFirstRingHabitable(),
				opts.getFirstRingRadius(),
				opts.getSecondRingSystemNumber(),
				opts.getSecondRingHabitable(),
				opts.getSecondRingRadius(),

				opts.headerSpacer(),
				new ParamTitle("GAME_OTHER"),
				opts.getLooseNeighborhood(),
				opts.getOrionToEmpireModifier(),
				opts.getEmpiresSpreadingFactor(),
				opts.galaxyRandSource(),

				opts.headerSpacer(),
				new ParamTitle("GALAXY_PREVIEW"),
				IGameOptions.galaxyPreviewColorStarsSize,
				IGameOptions.galaxyPreviewAI,
				IGameOptions.galaxyPreviewPlayer,
				IGameOptions.galaxyPreviewOrion
				)));
		return map;
	}
	@Override protected void singleInit() {
		//startBox			= new Box(startButtonHelp);
		showAbilitiesBox	= new Box(opts.useSelectableAbilities());
		shapeBox			= new Box(opts.shapeSelection());
		mapOption1Box		= new Box(opts.shapeOption1());
		mapOption2Box		= new Box(opts.shapeOption2());
		mapOption3Box		= new Box(opts.shapeOption3());
		mapOption4Box		= new Box(opts.shapeOption4());
		sizeOptionBox		= new Box(opts.dynStarsPerEmpire());
		sizeBox				= new Box(opts.sizeSelection());
		diffBox				= new Box(opts.difficultySelection());
		wysiwygBox			= new Box(opts.galaxyRandSource());
		oppBox				= new Box(opts.aliensNumber());

		paramList = AllSubUI.optionsGalaxy();
		for (int i=0;i<oppSet.length;i++)
			oppSet[i] = new Box(specificOpponent, i);
		for (int i=0;i<oppAbilities.length;i++)
			oppAbilities[i] = new Box(specificAbilities, i);
		for (int i=0;i<oppAI.length;i++)
			oppAI[i] = new Box(specificAI, i);
		duplicateList = new SafeListParam("GALAXY_DUPLICATE");
		duplicateList.add(opts.difficultySelection());
		duplicateList.add(opts.shapeSelection());
		duplicateList.add(opts.sizeSelection());
		duplicateList.add(opts.shapeOption1());
		duplicateList.add(opts.shapeOption2());
		duplicateList.add(opts.aliensNumber());

		for (IParam param : duplicateList)
			for (int i=0; i<2; i++)
				param.initDependencies(i);
	}
	@Override public void init() {
		showGrid = prevShowGrid;
		opts = guiOptions();
		isOnTop = true;
		super.init();
		dialogMonoFont = null;
		playerMug  = null;
        initAIandAbilitiesList();
        opts.saveOptionsToFile(LIVE_OPTIONS_FILE);
		refreshGui(0);
	}
	@Override protected String GUI_ID() { return GUI_ID; }
	@Override public void refreshGui(int level) {
        opts.setAndGenerateGalaxy();
        clearMugs();
        backImg = null;
        nebulas = null;
        repaint();
	}
	@Override public void clearImages() {
		super.clearImages();
		dialogMonoFont	= null;
		galaxyTextArray	= null;
      	nebulas 		= null;
      	clearMugs();
    }
	private void initPopupPositions()	{
		if (popupPositionsInitialised)
			return;
		SafeListParam list = advancedSystemMap().getListNoTitle();
		int boxX = galaxyBox.x - scaled(360);
		for ( IParam param : list) {
			if (param instanceof ParamList) {
				ParamList p = (ParamList) param;
				p.setPosition(boxX, -1);
			}
		}
		popupPositionsInitialised = true;
	}
    private void clearMugs() {
    	playerMug		= null;
    	smallBackMug	= null;
		bigBackMug		= null;
		smallNullMug	= null;
		bigNullMug		= null;
		smallOppMugs	= null;
		bigOppMugs		= null;
   }
    private void noFogChanged() {
    	IGameOptions.noFogOnIcons.toggle();
    	clearMugs();
        repaint();
    }
    private BufferedImage getMug(BufferedImage diplo, BufferedImage back) {
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
    private BufferedImage smallOppMug(int num) {
        if (smallOppMugs == null)
        	initOppMugs();
        return smallOppMugs[num];
    }
    private BufferedImage bigOppMug(int num) {
        if (bigOppMugs == null)
        	initOppMugs();
        return bigOppMugs[num];
    }
    private BufferedImage oppMug(int num, boolean small) { return small? smallOppMug(num) : bigOppMug(num); }
    private void updateOppMugs(int i) {
    	if (smallOppMugs == null) {
    		initOppMugs();
    		return;
    	}
		String selOpp = opts.selectedOpponentRace(i);
		if (selOpp == null) {
    		smallOppMugs[i] = smallNullMug();
    		bigOppMugs[i]   = bigNullMug();
		} else {
    		BufferedImage diplo = Race.keyed(selOpp).diploMugshotQuiet();
    		smallOppMugs[i] = getMug(diplo, smallBackMug());
    		bigOppMugs[i]   = getMug(diplo, bigBackMug());
		}
    }
    private void initOppMugs() {
    	smallOppMugs = new BufferedImage[MAX_DISPLAY_OPPS];
    	bigOppMugs   = new BufferedImage[MAX_DISPLAY_OPPS];
    	for (int i=0; i<MAX_DISPLAY_OPPS; i++)
    		updateOppMugs(i);
    }
	private BufferedImage smallBackMug() {
		if (smallBackMug == null)
			initBackMugs();
		return smallBackMug;
	}
	private BufferedImage bigBackMug() {
		if (bigBackMug == null)
			initBackMugs();
		return bigBackMug;
	}
    private BufferedImage backMug(boolean small) { return small? smallBackMug() : bigBackMug(); }
	private BufferedImage smallNullMug() {
		if (smallNullMug == null)
			initBackMugs();
		return smallNullMug;
	}
	private BufferedImage bigNullMug() {
		if (bigNullMug == null)
			initBackMugs();
		return bigNullMug;
	}
	private void initBackMugs() {
		smallBackMug = opts.getMugBackImg(wSmallMug, hSmallMug, rShSmallMug);
		bigBackMug   = opts.getMugBackImg(wBigMug,   hBigMug,   rShBigMug);
		String label = text("SETUP_OPPONENT_RANDOM");
		smallNullMug = getNullMug(label, true);
		bigNullMug   = getNullMug(label, false);
	}
    private BufferedImage getNullMug(String label, boolean smallImages) {
    	BufferedImage back = backMug(smallImages);
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
    	int x = ((bw - randSW)/2);
		int y = smallImages ? bh-s20 : bh-s31;
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
		RotPUI.instance().selectSetupRacePanel();
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
	private int currentSpecificAbilityIndex(String s) {
		for (int i=0; i<specificAbilitiesArray.length; i++) {
			if (s.equalsIgnoreCase((String) specificAbilitiesArray[i]))
				return i;
		}
		return -1;
	}
	private int currentGlobalAbilityIndex(String s) {
		for (int i=0; i<globalAbilitiesArray.length; i++) {
			if (s.equalsIgnoreCase((String) globalAbilitiesArray[i]))
				return i;
		}
		return -1;
	}
	@SuppressWarnings("rawtypes")
	private void setFileChooserFont(Component[] comp) {
		int topInset  = scaled(6);
		int sideInset = scaled(15);
	    for(int i=0; i<comp.length; i++)  {
	    	if(comp[i] instanceof JPanel){
	            ((JPanel)comp[i]).setBackground(GameUI.borderMidColor());
	            if(((JPanel)comp[i]).getComponentCount() !=0){
	            	setFileChooserFont(((JPanel)comp[i]).getComponents());
	            }
	        }
	        if(comp[i] instanceof JTextField){
	            ((JTextField)comp[i]).setBackground(GameUI.setupFrame());
	        }
	        if(comp[i] instanceof JToggleButton){
	            ((JToggleButton)comp[i]).setBackground(GameUI.setupFrame());
	        }
	        if(comp[i] instanceof JButton){
	            String txt = ((JButton)comp[i]).getText();
	            String cancel = LabelManager.current().label("BUTTON_TEXT_CANCEL");
	            String open = LabelManager.current().label("BUTTON_TEXT_OPEN");
	            if (txt!=null && (cancel.equals(txt) || open.equals(txt))) {
		            ((JButton)comp[i]).setMargin(new Insets(topInset, sideInset, 0, sideInset));
		            ((JButton)comp[i]).setBackground(GameUI.buttonBackgroundColor());
		            ((JButton)comp[i]).setForeground(GameUI.buttonTextColor());
		            ((JButton)comp[i]).setVerticalAlignment(SwingConstants.TOP);
	            }
	        }
	        if(comp[i] instanceof JScrollPane){
	            ((JScrollPane)comp[i]).setBackground(GameUI.borderMidColor());
	        }
	        if(comp[i] instanceof JList){
	            ((JList)comp[i]).setBackground(GameUI.setupFrame());
	            ((JList)comp[i]).setSelectionBackground(GameUI.borderMidColor());
	        }
	        if(comp[i] instanceof JComboBox){
	            ((JComboBox)comp[i]).setBackground(GameUI.setupFrame());
	        }
	        if(comp[i] instanceof Container)
	        	setFileChooserFont(((Container)comp[i]).getComponents());
	        try{comp[i].setFont(narrowFont(15));}
	        catch(Exception e){}//do nothing
	    }
	}

	private String getBitmapFile() {
        String dirPath = opts.bitmapGalaxyLastFolder().get();
        File selectedFile = new File(opts.selectedGalaxyShapeOption3());
        if (selectedFile.exists()) {
        	dirPath = selectedFile.getParentFile().getAbsolutePath();
        	opts.bitmapGalaxyLastFolder().set(dirPath);
        	UserPreferences.save();
        }
        BitmapFileChooser fileChooser = new BitmapFileChooser();
	    setFileChooserFont(fileChooser.getComponents());
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
        	opts.bitmapGalaxyLastFolder().set(dirPath);
        	UserPreferences.save();
			return selectedFile.getPath();
		}
		return opts.shapeOption3().defaultValue();
	}
	public  void selectBitmapFromList() {
		String filePath = getBitmapFile();
		opts.shapeOption3().set(filePath);
		opts.galaxyShape().quickGenerate();
		repaint();
	}
	private int currentGalaxyTextIndex(String s) {
		String[] textList = getGalaxyTextList(false);
		for (int i=0; i<textList.length; i++) {
			if (s.equals((String) textList[i]))
				return i;
		}
		// Second chance by ignoring case
		for (int i=0; i<textList.length; i++) {
			if (s.equalsIgnoreCase((String) textList[i]))
				return i;
		}
		return -1;
	}
	private void initGalaxyTextFile(File file) {
		try (FileOutputStream fout = new FileOutputStream(file);
			// modnar: change to OutputStreamWriter, force UTF-8
			PrintWriter out = new PrintWriter(new OutputStreamWriter(fout, "UTF-8")); ) {
			out.println( "	List of customized Text Galaxies");
			out.println( "	Use a \"tab\" as separator to add comments");
			out.println();
			out.println( "ROTP	// Initial options");
			out.println( "ℳo○𝟏	// The precursor!");
			out.println();
			out.println("	A nice selection by U/dweller_below");
			out.println();
			out.println( "∞	Infinity feels good, but gameplay is the same as 8");
			out.println( "☸	The wheel of Dharma also feels appropriate");
			out.println( "༜	The Tibetan Sign Rdel Dkar Gsum gives 3 close rings. And it stacks well in multiple lines");
			out.println( "༶	The Tibetan Mark Caret gives 4 widely spaced star fields");
			out.println( "❖	This one gives 4 star fields with 8 to 13 light year spacing.");
			out.println( "ⵘ	Tifinagh Letter Ayer Yagh gives 5 star fields");
			out.println( "∴∵	You can stack or repeat these 2 characters for multiples of 3 or 6.");
			out.println( "፨	The Ethiopic Paragraph Separator is a nice 7 star fields.");
			out.println( "⁂");
			out.println( "🂓");
			out.println( "░");
			out.println( "▒");
			out.println( "⨌");
			out.println( "🦌");
			out.println( "⛄");
			out.println( "🎅");
			out.println( "🎄");
			out.println();
			out.println("	And more ...");
			out.println();
			out.println( "☃");
			out.println( "👽");
			out.println( "⌨");
			out.println( "⸎");
			out.println( "ꔘ");
			out.println( "꙰");
			out.println( "҈");
			out.println( "҉");
			out.println( "۞");
			out.println( "ꙮ");
			out.println( "𐩕");
			out.println( "֍");
			out.println( "֎");
			out.println( "☷");
			out.println( "❉");
			out.println( "⛆");
			out.println( "⣿");
			out.println( "𓃑");
			out.println( "𖡼");
			out.println( "𖥚");
			out.println( "፠");
			out.println( "⁂");
			out.println( "፤፤");
			out.println( "𐄳");
			out.println( "𐧾");
			out.println( "𐮜");
			out.println( "𑗗");
			out.println( "𝅂");
			out.println( "𞡜");
			out.println();
		}
		catch (IOException e) {
			System.err.println("GalaxyTextFile.save -- IOException: "+ e.toString());
		}
	}
	private String[] getGalaxyTextList(boolean reload) {
		if (!reload && galaxyTextArray != null)
			return galaxyTextArray;
		LinkedList<String> list = new LinkedList<>();
		// list.add(opts.selectedHomeWorldName());
		String path = Rotp.jarPath();
		String galaxyfile = GALAXY_TEXT_FILE;
		File file = new File(path, galaxyfile);
		if (!file.exists())
			initGalaxyTextFile(file);
			
		try ( BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream(file), "UTF-8"));) {
			String input;
			while ((input = in.readLine()) != null) {
				String[] args = input.split("\t");
				String val = args[0].trim();
				if (!val.isEmpty())
					list.add(val);
			}				
		}
		catch (FileNotFoundException e) {
			System.err.println(path+galaxyfile+" not found.");
		}
		catch (IOException e) {
			System.err.println("GalaxyTextFile.load -- IOException: "+ e.toString());
		}

		galaxyTextArray = list.toArray(new String[list.size()]);
		return galaxyTextArray;
	}
	public  void selectGalaxyTextFromList() {
		String initialChoice = baseGalaxyText();
		String message = text("SETUP_SHAPE_MESSAGE");
		String title  = text("SETUP_SHAPE_TITLE");
		int boxWidth  = scaled(430);
		int boxHeight = scaled(360);
		int boxX = galaxyBox.x - boxWidth - s10;
		int boxY = (getHeight() - boxHeight)/2;
		ListDialogUI dialog = RotPUI.listDialog();
		dialog.init(
				this,					// Frame component
				getParent(),			// Location component
				message,				// Message
				title,					// Title
				(String[]) getGalaxyTextList(true),	// List
				initialChoice, 			// Initial choice
				null,					// long Dialogue
				false,					// isVerticalWrap
				boxX, boxY,				// Position
				boxWidth, boxHeight,	// size
				dialogMonoFont(),		// Font
				this,					// for listener
				null,					// Alternate return
				opts.shapeOption1()); 	// Help parameter

		String input = (String) dialog.showDialog(0);
		if (input == null)
			return;
		opts.selectedGalaxyShapeOption1(input);
		updateGalaxyText();
		postSelectionMedium(false);
	}
	private String baseGalaxyText()	{
		return opts.selectedGalaxyShapeOption1().split(System.lineSeparator())[0];
	}
	public  void updateGalaxyText()	{
		String str = baseGalaxyText();
		if (isShapeTextMulti()) {
			String[] textList = getGalaxyTextList(false);
			int id = max(0, currentGalaxyTextIndex(str));
			int num = opts.selectedGalaxyTextOption4();
			while (num > 1) {
				//str += NEWLINE;
				str += System.lineSeparator();
				id++;
				if (id >= textList.length)
					id = 0;
				str += textList[id];
				num--;
			}
		}
		opts.selectedGalaxyShapeOption1(str);
	}
	@Override public void preview(String s, IParam param) {
		if (s == null)
			return;
		if (!s.equalsIgnoreCase("quickGenerate")) {
			if (opts.shapeSelection() == param) {
				opts.shapeSelection().set(s);
				return;
			}
			else if (opts.sizeSelection() == param) {
				opts.sizeSelection().set(s);
				return;
			}
			else if (isShapeTextGalaxy()) {
				if (opts.shapeOption1() == param) {
					opts.selectedGalaxyShapeOption1(s);
					updateGalaxyText();
					postSelectionMedium(false);
					return;
				}
				else if (opts.shapeOption2() == param) {
					opts.selectedGalaxyShapeOption2(s);
					updateGalaxyText();
					postSelectionFull(false);
					return;
				}
			}
			else if (opts.shapeOption1() == param) {
				opts.selectedGalaxyShapeOption1(s);
				postSelectionMedium(false);
				return;
			}
			else if (opts.shapeOption2() == param) {
				opts.selectedGalaxyShapeOption2(s);
				postSelectionMedium(false);
				return;
			}
			else if (isShapeBitmapGalaxy() && opts.shapeOption3() == param){
				opts.shapeOption3().set(s);
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
		ListDialogUI dialog = RotPUI.listDialog();
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
		ListDialogUI dialog = RotPUI.listDialog();
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
	private String selectSpecificAbilityFromList(int i) {
		String title   = text(SPECIFIC_ABILITY);
		String message = text(SPECIFIC_ABILITY + LABEL_DESCRIPTION);
		String initialChoice = opts.specificOpponentCROption(i);
		ListDialogUI dialog = RotPUI.listDialog();
		dialog.init(
		    	this, getParent(),	// Frame & Location component
		    	message, title,				// Message, Title
		        specificAbilitiesArray,		// List
		        initialChoice, 				// Initial choice
		        "XX_RACE_JACKTRADES_XX",	// long Dialogue
		        false,						// isVerticalWrap
				-1, -1,						// Position
		        scaled(500), scaled(450),	// size
				null, null, null,			// Font, Preview, Alternate return
				specificAbilities); // help parameter

		String input = (String) dialog.showDialog(0);
		ModifierKeysState.reset();
		repaint();
	    if (input == null)
	    	return initialChoice;
	    opts.specificOpponentCROption(input, i);
	    return input;
	}
	private String dialogLang(String src)	{
		String langKey = LANG_LIST_KEY + src.toUpperCase();
		String langTxt = text(langKey);
		if (langTxt.equals(langKey))
			return src;
		return langTxt;
	}
	private String[] selectionList(String[] srcList) {
		List<String> langList = new ArrayList<>();
		for (String src : srcList)
			langList.add(dialogLang(src));
		String[] langArr = new String[langList.size()];
		return langList.toArray(langArr);
	}
	private String selectAlienAbilityFromList() {
		String title   = text(GLOBAL_ABILITIES);
		String message = text(GLOBAL_ABILITIES + LABEL_DESCRIPTION);
		String initialChoice = opts.selectedUseGlobalCROptions();
		String[] srcList  = selectionList(globalAbilitiesArray);
		ListDialogUI dialog = RotPUI.listDialog();
		dialog.init(
			    this, getParent(),			// Frame & Location component
		    	message, title,				// Message, Title
		    	srcList,					// List
		        initialChoice, 				// Initial choice
		        "XX_RACE_JACKTRADES_XX",	// long Dialogue
		        false,						// isVerticalWrap
				-1, -1,						// Position
		        scaled(500), scaled(450),	// size
				null, null,					// Font, Preview
				Arrays.asList(globalAbilitiesArray),	//Alternate return
				globalAbilities); // help parameter

		String input = (String) dialog.showDialog(0);
		ModifierKeysState.reset();
		ModifierKeysState.reset();
		repaint();
	    if (input == null)
	    	return initialChoice;
	    opts.globalCROptions().set(input);
	    return input;
	}
	// ==============================================================
	// Paint components sub sections
	// ==============================================================
	Graphics2D paintInit(Graphics g0) {
		// showTiming = true;
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
		int maxOpp = opts.maximumOpponentsOptions();
		int numOpp = opts.selectedNumberOpponents();
		
		boolean smallImages = maxOpp > 25;
		int mugW = smallImages? wSmallMug : wBigMug;
		int mugH = smallImages? hSmallMug : hBigMug;
		g.setFont(narrowFont(30));
		g.setColor(Color.black);
		String oppStr =str(numOpp);
		int numSW = g.getFontMetrics().stringWidth(oppStr);
		int x0 = oppBox.x + ((oppBox.width-numSW)/2);
		int y0 = oppBox.y + oppBox.height -s5;
		drawString(g,oppStr, x0, y0);

		int numRows = smallImages ? 7 : 5;
		int numCols = smallImages ? 7 : 5;
		int fSize	= smallImages ? 12 : 15;
		int offset1	= smallImages ? s4 : s5;
		int offset2	= smallImages ? s12 : s15;
		int boundH	= smallImages ? s17 : s20;
		int spaceW = mugW+(((boxW-s60)-(numCols*mugW))/(numCols-1));
		int spaceH = smallImages ? mugH+s10 : mugH+s15;

		// draw opponent boxes
		Stroke prevStroke = g.getStroke();
		Color borderC = GameUI.setupFrame();
		boolean selectableAI = opts.selectableAI();
		boolean selectableCR = opts.selectedUseSelectableAbilities();
		int maxDraw = min((numRows*numCols), numOpp, MAX_DISPLAY_OPPS);
		for (int i=0;i<maxDraw;i++) {
			int row = i/numCols;
			int col = i%numCols;
			// int y2 = y0+s50+(row*spaceH);
			int y2 = y0+s67+(row*spaceH); // BR: Adjusted for dataRace selection
			int x2 = leftBoxX+s30+(col*spaceW);
			int y2o = y2;
			int mugHo = mugH;
			if (selectableCR) {
				y2o   += boundH;
				mugHo -= boundH;
			}
			if (selectableAI) {
				mugHo -= boundH;
			}
			oppSet[i].setBounds(x2, y2o, mugW, mugHo);
			oppAI[i].setBounds(x2, y2+mugH-boundH, mugW, boundH); // BR: Adjusted
			oppAbilities[i].setBounds(x2, y2, mugW, boundH);
			g.drawImage(oppMug(i, smallImages), x2, y2, this);

			if (selectableAI) {
				g.setColor(SystemPanel.whiteText);
				String aiText = text(opts.specificOpponentAIOption(i+1));
				g.setFont(narrowFont(fSize)); // BR: Adjusted to fit the box
				scaledFont(g, aiText, mugW-s2, fSize, fSize-4);
				int aiSW = g.getFontMetrics().stringWidth(aiText);
				int x2b = x2+(mugW-aiSW)/2;
				drawString(g,aiText, x2b, y2+mugH-offset1);
			}
			if (selectableCR) {
				g.setColor(SystemPanel.whiteText);
				//String crText = text(opts.specificOpponentCROption(i+1));
				String crText = dialogLang(opts.specificOpponentCROption(i+1));
				g.setFont(narrowFont(fSize));
				int fontSize = scaledFont(g, crText, mugW-s2, fSize, fSize-4);
				int dy = scaled(fSize-fontSize)/2;
				int crSW = g.getFontMetrics().stringWidth(crText);
				int x2b = x2+(mugW-crSW)/2;
				drawString(g,crText, x2b, y2+offset2-dy);
			}
			g.setStroke(stroke1);
			g.setColor(borderC);
			g.drawRect(x2, y2, mugW, mugH);
			g.setStroke(prevStroke);
		}
	}
	private void drawGalaxy(Graphics2D g) {
		// draw galaxy
		drawGalaxyShape(g, opts.galaxyShape(), galaxyX, galaxyY, galaxyW, galaxyH);

		// draw info under galaxy map
		g.setColor(Color.black);
		g.setFont(narrowFont(16));
		int galaxyBoxW = boxW-s40;
		int y3 = galaxyY+galaxyH+s16;
		String systemsLbl = text("SETUP_GALAXY_NUMBER_SYSTEMS", opts.numberStarSystems());
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
			||  (hoverPolyBox == diffBoxL)		 || (hoverPolyBox == diffBoxR)
			||  (hoverPolyBox == wysiwygBoxL)	 || (hoverPolyBox == wysiwygBoxR)
			||  (hoverPolyBox == aiBoxL)		 || (hoverPolyBox == aiBoxR)
			||  (hoverPolyBox == abilitiesBoxL)	 || (hoverPolyBox == abilitiesBoxR)
			||  (hoverPolyBox == mapOption1BoxL) || (hoverPolyBox == mapOption1BoxR)
			||  (hoverPolyBox == mapOption2BoxL) || (hoverPolyBox == mapOption2BoxR)
			||  (hoverPolyBox == mapOption4BoxL) || (hoverPolyBox == mapOption4BoxR)
			||  (hoverPolyBox == sizeOptionBoxL) || (hoverPolyBox == sizeOptionBoxR)
			||  (hoverPolyBox == oppBoxU)		 || (hoverPolyBox == oppBoxD)) {
			g.setColor(Color.yellow);
			g.fill(hoverPolyBox);
		}
		else if ((hoverBox == shapeBox)		|| (hoverBox == sizeBox)
			|| (hoverBox == mapOption1Box)	|| (hoverBox == mapOption2Box)
			|| (hoverBox == mapOption3Box)	|| (hoverBox == mapOption4Box)
			|| (hoverBox == sizeOptionBox)	|| (hoverBox == abilitiesBox)
			|| (hoverBox == aiBox)			|| (hoverBox == newRacesBox)
			|| (hoverBox == oppBox)			|| (hoverBox == showAbilitiesBox)
			|| (hoverBox == diffBox)		|| (hoverBox == wysiwygBox)) {
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
			if (opts.selectedUseSelectableAbilities()) {
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
		g.setFont(narrowFont(15));

		// draw Opponent CR text
		if (opts.globalCROptions().isBaseRace()) // for backward compatibility
			opts.globalCROptions().setFromDefault(false, true);
		// String crLbl = text(opts.selectedUseGlobalCROptions());
		String crLbl = dialogLang(opts.selectedUseGlobalCROptions());
		int crSW = g.getFontMetrics().stringWidth(crLbl);
		int x4cr = abilitiesBox.x+((aiBox.width-crSW)/2);
		int y4cr = abilitiesBox.y+abilitiesBox.height-s3;
		drawString(g,crLbl, x4cr, y4cr);
		
		// draw Show Abilities Yes/No text
		String showAbilityLbl = showAbilityStr();
		int showAbilitySW = g.getFontMetrics().stringWidth(showAbilityLbl);
		int x4d = showAbilitiesBox.x+((showAbilitiesBox.width-showAbilitySW)/2);
		int y4d = showAbilitiesBox.y+showAbilitiesBox.height-s3;
		drawString(g, showAbilityLbl, x4d, y4d);

		// draw Opponent AI text
		String aiLbl = text(opts.selectedOpponentAIOption());
		int aiSW = g.getFontMetrics().stringWidth(aiLbl);
		int x4b = aiBox.x+((aiBox.width-aiSW)/2);
		int y4b = aiBox.y+aiBox.height-s3;
		drawString(g,aiLbl, x4b, y4b);

		// draw New Races ON/OFF text
		String newRacesLbl = newRacesOnStr();
		int newRacesSW = g.getFontMetrics().stringWidth(newRacesLbl);
		int x4c = newRacesBox.x+((newRacesBox.width-newRacesSW)/2);
		int y4c = newRacesBox.y+newRacesBox.height-s3;
		drawString(g, newRacesLbl, x4c, y4c);
	}
	private int  drawGalaxyTopOptions(Graphics2D g) {
		// draw galaxy options text
		int y5 = shapeBox.y+shapeBox.height-s4;
		String shapeLbl = text(opts.selectedGalaxyShape());
		int shapeSW = g.getFontMetrics().stringWidth(shapeLbl);
		int x5a =shapeBox.x+((shapeBox.width-shapeSW)/2);
		drawString(g,shapeLbl, x5a, y5);

		if (opts.numGalaxyShapeOption1() > 0) {
			if (isShapeTextGalaxy()) {
				if (isShapeTextMulti()) {
					int lineCount = opts.selectedGalaxyTextOption4();
					String label1 = text("SETUP_GALAXY_SHAPE_OPTION_4", lineCount);
					int sw1 = g.getFontMetrics().stringWidth(label1);
					int x5d = mapOption4Box.x+((mapOption4Box.width-sw1)/2);
					drawString(g,label1, x5d, y5+s60);
				}
				String label1 = opts.selectedGalaxyShapeOption1();
				Font prevFont = g.getFont();
				scaledGalaxyFont(g, label1, mapOption1Box.width-s20, s16, s15, s6);
				int sw1 = g.getFontMetrics().stringWidth(label1);
				int x5d = mapOption1Box.x+((mapOption1Box.width-sw1)/2);
				drawString(g,label1, x5d, y5+s20);
				g.setFont(prevFont);
			}
			else {
				String label1 = text(opts.selectedGalaxyShapeOption1());
				int sw1 = g.getFontMetrics().stringWidth(label1);
				int x5d = mapOption1Box.x+((mapOption1Box.width-sw1)/2);
				drawString(g,label1, x5d, y5+s20);
			}
			if (opts.numGalaxyShapeOption2() > 0) {
				if (isShapeBitmapGalaxy()) {
					Font prevFont = g.getFont();
					String label3 = getNameFromPath(opts.selectedGalaxyShapeOption3());
					if (label3.equals(opts.shapeOption3().defaultValue()))
						label3 = text(NO_SELECTION);
			        int fs  = scaledFont(g, label3, mapOption3Box.width, 15, 10);
					int sw2 = g.getFontMetrics().stringWidth(label3);
					g.setFont(narrowFont(fs));
					int x5e =mapOption3Box.x+((mapOption3Box.width-sw2)/2);
					switch (opts.compactOptionOnly().get().toUpperCase()) {
						case "NO":
						case "YES":
						default:
							drawString(g,label3, x5e, y5+s60);
							break;
					}
					g.setFont(prevFont);
				}
				String label2 = text(opts.selectedGalaxyShapeOption2());
				int sw2 = g.getFontMetrics().stringWidth(label2);
				int x5e =mapOption2Box.x+((mapOption2Box.width-sw2)/2);
				drawString(g,label2, x5e, y5+s40);	
			}		 
		}
		return y5;
	}
	private void drawGalaxySizeAndDiff(Graphics2D g, int y5) {
		String sizeLbl = text(opts.selectedGalaxySize());
		int sizeSW = g.getFontMetrics().stringWidth(sizeLbl);
		int x5b =sizeBox.x+((sizeBox.width-sizeSW)/2);
		drawString(g,sizeLbl, x5b, y5);

		if (isDynamic()) { // BR:
			String label = text(SIZE_OPT_KEY, opts.dynStarsPerEmpire().guideValue());
			int sw2 = g.getFontMetrics().stringWidth(label);
			int x5b1 =sizeOptionBox.x+((sizeOptionBox.width-sw2)/2);
			drawString(g,label, x5b1, y5+s20);		   
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
		if (opts.selectedGalaxyRandSource() == 0)
			wysiwygLbl = text("SETTINGS_MOD_GALAXY_RAND_RANDOM");
		else
			wysiwygLbl = text("SETTINGS_MOD_GALAXY_RAND_WYSIWYG", opts.galaxyRandSource().guideValue());
		int wysiwygSW = g.getFontMetrics().stringWidth(wysiwygLbl);
		if (wysiwygSW > wysiwygBox.width) {
			wysiwygLbl = opts.galaxyRandSource().guideValue();
			wysiwygSW  = g.getFontMetrics().stringWidth(wysiwygLbl);
		}
		int x5d =wysiwygBox.x+((wysiwygBox.width-wysiwygSW)/2);
		drawString(g,wysiwygLbl, x5d, y5+s20);
	}
	private void drawAutoplayWarning(Graphics2D g, int y5) {
		// draw autoplay warning
		if (opts.isAutoPlay()) {
			g.setFont(narrowFont(16));
			String warning = text("SETTINGS_AUTOPLAY_WARNING");
			List<String> warnLines = this.wrappedLines(g, warning, galaxyW);
			g.setColor(Color.white);
			int warnY = y5+s60;
			switch (opts.compactOptionOnly().get().toUpperCase()) {
				case "NO":
				case "YES":
				default:
					warnY += s25;
			}
			for (String line: warnLines) {
				drawString(g,line, galaxyX, warnY);
				warnY += s18;
			}
		}
	}
	@Override public void paintComponent(Graphics g0) {
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
			System.out.println("Galaxy paintComponent() Time = " + (System.currentTimeMillis()-timeStart));
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
	private String newRacesOnStr() {
		if (opts.selectedShowNewRaces()) return text("SETUP_NEW_RACES_ON");
		else return text("SETUP_NEW_RACES_OFF");
	}
	private String showAbilityStr() {
		return opts.useSelectableAbilities().guideValue();
	}
	private void drawGalaxyShape(Graphics2D g, GalaxyShape sh, int x, int y, int w, int h) {
		// Value in ly
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
		int grid = Math.round(galaxyGrid * factor);

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
//		System.out.println("Galaxy size x=" + fmt(sh.width(), 1) + " y=" + fmt(sh.height(), 1)
//											+ " x+=" + fmt(fullGw, 1) + " y+=" + fmt(fullGh, 1));
		if (colored && opts.previewNebula().get())
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
	private BufferedImage playerRaceImg() {
		if (playerMug == null) {
			String selRace = opts.selectedPlayerRace();
			playerMug = newBufferedImage(Race.keyed(selRace).diploMug());
		}
		return playerMug;
	}
	private boolean isDynamic() {
		return opts.selectedGalaxySize().equals(IGameOptions.SIZE_DYNAMIC);
	}
	public  boolean isShapeTextGalaxy()	{ return opts.selectedGalaxyShape().equals(IGameOptions.SHAPE_TEXT); }
	public  boolean isShapeTextMulti()	{ return opts.selectedGalaxyShapeOption2().contains("MULTI"); }
	private boolean isShapeBitmapGalaxy() { return opts.selectedGalaxyShape().equals(IGameOptions.SHAPE_BITMAP); }
	private String getNameFromPath(String path) {
		File file = new File(path);
		if (file.exists())
			return file.getName();
		return path;
	}
	private boolean isRandomGalaxy(String galaxyShape) {
		return galaxyShape.equals(IGameOptions.SHAPE_RANDOM)
				|| galaxyShape.equals(IGameOptions.SHAPE_RANDOM_2);
	}

	// ==============================================================
	// Post selection tasks
	// ==============================================================
	public	void postGalaxySizeSelection(boolean click) {
		opts = guiOptions();
		if (click) softClick();
		int numOpps = opts.selectedNumberOpponents();
		if(numOpps<0) {
			opts.selectedNumberOpponents(0);
			numOpps = 0;
		}
		int maxOpps = opts.maximumOpponentsOptions();
		if (maxOpps < numOpps) {
			for (int i=maxOpps;i<numOpps;i++)
				opts.selectedOpponentRace(i,null);
			opts.selectedNumberOpponents(maxOpps);
		}
		String sss = opts.selectedShapeSelection();
		if (isRandomGalaxy(sss))
			opts.selectedGalaxyShape(sss);
		opts.galaxyShape().quickGenerate(); // modnar: do a quickgen to get correct map preview
		backImg = null; // BR: to show/hide system per empire
		nebulas = null;
		postSelectionLight(false);
	}
	public	void postSelectionFull(boolean click) {
		opts = guiOptions();
		if (click)
			softClick();
		opts.galaxyShape().quickGenerate();
		backImg = null;
		nebulas = null;
		postSelectionLight(false);
	}
	public	void postSelectionMedium(boolean click) {
		opts = guiOptions();
		if (click) softClick();
		opts.galaxyShape().quickGenerate();
		nebulas = null;
		postSelectionLight(false);
	}
	public	void postSelectionLight(boolean click) {
		if (click)
			softClick();
		loadGuide();
		repaint();
	}
	public  void nextMapOption1(boolean click) {
		if (isShapeTextGalaxy()) {
			String currText = baseGalaxyText();
			int nextIndex = 0;
			if (currText != null)
				nextIndex = currentGalaxyTextIndex(currText)+1;
			if (nextIndex >= getGalaxyTextList(false).length)
				nextIndex = 0;
			String nextText = (String) getGalaxyTextList(false)[nextIndex];
			opts.selectedGalaxyShapeOption1(nextText);
			updateGalaxyText();
		}
		else
			opts.shapeOption1().next();
		postSelectionMedium(click);
	}
	public  void prevMapOption1(boolean click) {
		if (isShapeTextGalaxy()) {
			String currText = baseGalaxyText();
			int prevIndex = 0;
			if (currText != null)
				prevIndex = currentGalaxyTextIndex(currText)-1;
			if (prevIndex < 0)
				prevIndex = getGalaxyTextList(false).length-1;
			String prevText = (String) getGalaxyTextList(false)[prevIndex];
			opts.selectedGalaxyShapeOption1(prevText);
			updateGalaxyText();
		}
		else
			opts.shapeOption1().prev();
		postSelectionMedium(click);
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
	private void nextGlobalAbilities(boolean click) {
		if (click) softClick();
			String currCR = opts.selectedUseGlobalCROptions();
		int nextIndex = 0;
		if (currCR != null)
			nextIndex = currentGlobalAbilityIndex(currCR)+1;
		if (nextIndex >= globalAbilitiesArray.length)
			nextIndex = 0;
		String nextCR = (String) globalAbilitiesArray[nextIndex];
		opts.globalCROptions().set(nextCR);
		postSelectionLight(false);
	}
	private void prevGlobalAbilities(boolean click) {
		if (click) softClick();
			String currCR = opts.selectedUseGlobalCROptions();
		int prevIndex = 0;
		if (currCR != null)
			prevIndex = currentGlobalAbilityIndex(currCR)-1;
		if (prevIndex < 0)
			prevIndex = globalAbilitiesArray.length-1;
		String prevCR = (String) globalAbilitiesArray[prevIndex];
		opts.globalCROptions().set(prevCR);
		postSelectionLight(false);
	}
	private void toggleGlobalAbilities(MouseEvent e) {
		softClick();
		boolean up  = !SwingUtilities.isRightMouseButton(e);
		boolean mid = SwingUtilities.isMiddleMouseButton(e);
		if (mid)
			opts.globalCROptions().setFromDefault(false, true);
		else if (globalAbilitiesArray.length >= opts.minListSizePopUp().get()
				|| isCtrlDown())
			selectAlienAbilityFromList();
		else if (up)
			nextGlobalAbilities(false);
		else
			prevGlobalAbilities(false);
		postSelectionLight(false);
	}
	private void toggleNewRaces(boolean click) {
		if (click) softClick();
		opts.showNewRaces().toggle();
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
			opts.useSelectableAbilities().toggle();
		postSelectionLight(false);
	}
	private void nextSpecificOpponentAbilities(int i, boolean click) {
		if (click) softClick();
		if (click || isCtrlDown())
			selectSpecificAbilityFromList(i+1);
		else {
			String currCR = opts.specificOpponentCROption(i+1);
			int nextIndex = 0;
			if (currCR != null)
				nextIndex = currentSpecificAbilityIndex(currCR)+1;
			if (nextIndex >= specificAbilitiesArray.length)
				nextIndex = 0;
			String nextCR = (String) specificAbilitiesArray[nextIndex];
			opts.specificOpponentCROption(nextCR, i+1);
		}
	}
	private void prevSpecificOpponentAbilities(int i, boolean click) {
		if (click) softClick();
		if (click || isCtrlDown())
			selectSpecificAbilityFromList(i+1);
		else {
			String currCR = opts.specificOpponentCROption(i+1);
			int prevIndex = 0;
			if (currCR != null)
				prevIndex = currentSpecificAbilityIndex(currCR)-1;
	        if (prevIndex < 0)
	        	prevIndex = specificAbilitiesArray.length-1;
	        String prevCR = (String) specificAbilitiesArray[prevIndex];
	        opts.specificOpponentCROption(prevCR, i+1);
		}
	}
	private void toggleSpecificOpponentAbilities(int i, boolean click, boolean up, boolean mid) {
		if (click) softClick();
		if (mid)
			opts.specificOpponentCROption(specificAbilities.defaultValue(), i+1);
		else if (up)
			nextSpecificOpponentAbilities(i, false);
		else
			prevSpecificOpponentAbilities(i, false);
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
		updateOppMugs(i);
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
		GameUI.gameName = generateGameName();
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
			dialogMonoFont = null;
			galaxyTextArray = null;
		};
		SwingUtilities.invokeLater(save);
	}
	private void goToTuneGalaxyOptions() {
		prevShowGrid = showGrid;
		showGrid = true;
		ParamSubUI subUI = new ParamSubUI( MOD_UI, ADVANCED_SYSTEMS_GUI_ID, advancedSystemMap());
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
		setFontHints(g);
		// modnar: use (slightly) better upsampling
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		Race race;
		String setupName;
		if (opts.selectedPlayerIsCustom()) {
			race = Race.keyed(CustomRaceDefinitions.CUSTOM_RACE_KEY, null);
			setupName = race.setupName;
		}
		else {
			race = Race.keyed(opts.selectedPlayerRace());
			setupName = race.setupName();
		}

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

		// draw player vs opponent text
		int x2 = leftBoxX+s25+mugW+s15;
		// int y2 = boxY+s25+mugH-s42;
		int y2 = boxY+s25+mugH-s52; // BR: up a little
		int yho = s5; // BR: a little space for new races on/off 
		g.setFont(narrowFont(28));
		String header1 = text("SETUP_OPPONENTS_HEADER_1", setupName);
		String header2 = text("SETUP_OPPONENTS_HEADER_2");
		int swHdr = g.getFontMetrics().stringWidth(header1);
		drawBorderedString(g, header1, 1, x2, y2-yho, Color.black, Color.white);

		// draw opponent count box and arrows
		int x2b = x2+swHdr+s5;
		g.setColor(GameUI.setupFrame());
		oppBox.setBounds(x2b,y2-s30,s30,s35);
		g.fill(oppBox);
		int x2c = x2b+s33;
		int y2c = (int)(oppBox.getY()+(oppBox.getHeight()/2));
		oppBoxD.reset();
		oppBoxD.addPoint(x2c,y2c+s2);
		oppBoxD.addPoint(x2c+s13,y2c+s2);
		oppBoxD.addPoint(x2c+s7,y2c+s17);
		g.fill(oppBoxD);
		oppBoxU.reset();
		oppBoxU.addPoint(x2c,y2c-s1);
		oppBoxU.addPoint(x2c+s13,y2c-s1);
		oppBoxU.addPoint(x2c+s7,y2c-s16);
		g.fill(oppBoxU);

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
		int sliderW = s100;
		int sliderH = s16;
		int sliderYAI = y3-sliderH+s3;
		int sliderX = x3+swHdr3+s20;
		g.setColor(GameUI.setupFrame());

		aiBoxL.reset();
		aiBoxL.addPoint(sliderX-s4,sliderYAI+s1);
		aiBoxL.addPoint(sliderX-s4,sliderYAI+sliderH-s2);
		aiBoxL.addPoint(sliderX-s13,sliderYAI+(sliderH/2));
		g.fill(aiBoxL);
		aiBoxR.reset();
		aiBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+s1);
		aiBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+sliderH-s2);
		aiBoxR.addPoint(sliderX+sliderW+s13,sliderYAI+(sliderH/2));
		g.fill(aiBoxR);
		aiBox.setBounds(sliderX, sliderYAI, sliderW, sliderH);
		g.fill(aiBox);

		// draw CR selection
		g.setColor(SystemPanel.blackText);
		int y3cr = y2+s27;
		int x3cr = x2;
		drawString(g,header3cr, x3cr, y3cr);

		int sliderYCR = y3cr-sliderH+s3;
		g.setColor(GameUI.setupFrame());

		abilitiesBoxL.reset();
		abilitiesBoxL.addPoint(sliderX-s4,sliderYCR+s1);
		abilitiesBoxL.addPoint(sliderX-s4,sliderYCR+sliderH-s2);
		abilitiesBoxL.addPoint(sliderX-s13,sliderYCR+(sliderH/2));
		g.fill(abilitiesBoxL);
		abilitiesBoxR.reset();
		abilitiesBoxR.addPoint(sliderX+sliderW+s4,sliderYCR+s1);
		abilitiesBoxR.addPoint(sliderX+sliderW+s4,sliderYCR+sliderH-s2);
		abilitiesBoxR.addPoint(sliderX+sliderW+s13,sliderYCR+(sliderH/2));
		g.fill(abilitiesBoxR);
		abilitiesBox.setBounds(sliderX, sliderYCR, sliderW, sliderH);
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
		g.setColor(GameUI.setupFrame());

		shapeBoxL.reset();
		shapeBoxL.addPoint(sliderX-s4,sliderYAI+s1);
		shapeBoxL.addPoint(sliderX-s4,sliderYAI+sliderH-s2);
		shapeBoxL.addPoint(sliderX-s13,sliderYAI+(sliderH/2));
		g.fill(shapeBoxL);
		shapeBoxR.reset();
		shapeBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+s1);
		shapeBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+sliderH-s2);
		shapeBoxR.addPoint(sliderX+sliderW+s13,sliderYAI+(sliderH/2));
		g.fill(shapeBoxR);
		shapeBox.setBounds(sliderX, sliderYAI, sliderW, sliderH);
		g.fill(shapeBox);

		mapOption1BoxL.reset();
		mapOption1BoxR.reset();
		mapOption1Box.setBounds(0,0,0,0);
		if (opts.numGalaxyShapeOption1() > 0) {
			mapOption1BoxL.addPoint(sliderX-s4,sliderYAI+s1+s20);
			mapOption1BoxL.addPoint(sliderX-s4,sliderYAI+sliderH-s2+s20);
			mapOption1BoxL.addPoint(sliderX-s13,sliderYAI+(sliderH/2)+s20);
			g.fill(mapOption1BoxL);
			mapOption1BoxR.addPoint(sliderX+sliderW+s4,sliderYAI+s1+s20);
			mapOption1BoxR.addPoint(sliderX+sliderW+s4,sliderYAI+sliderH-s2+s20);
			mapOption1BoxR.addPoint(sliderX+sliderW+s13,sliderYAI+(sliderH/2)+s20);
			g.fill(mapOption1BoxR);
			mapOption1Box.setBounds(sliderX, sliderYAI+s20, sliderW, sliderH);
			g.fill(mapOption1Box);
		}

		mapOption2BoxL.reset();
		mapOption2BoxR.reset();
		mapOption2Box.setBounds(0,0,0,0);
		mapOption3Box.setBounds(0,0,0,0);
		mapOption4BoxL.reset();
		mapOption4BoxR.reset();
		mapOption4Box.setBounds(0,0,0,0);
		if (opts.numGalaxyShapeOption2() > 0) {
			if (isShapeBitmapGalaxy()) {
				switch (opts.compactOptionOnly().get().toUpperCase()) {
					case "NO":
					case "YES": {
						mapOption3Box.setBounds(sliderX, sliderYAI+s60, sliderW+2*sectionW, sliderH);
						break;
					}
					case "6":
					default: {
						mapOption3Box.setBounds(sliderX+sectionW-s13, sliderYAI+s40, sliderW+sectionW+s26, sliderH);
						break;
					}
				}
				g.fill(mapOption3Box);
			}
			else if (isShapeTextGalaxy() && isShapeTextMulti()) {
				int o4X, o4Y;
				switch (opts.compactOptionOnly().get().toUpperCase()) {
					case "NO":
					case "YES":
						o4X = sliderX;
						o4Y = sliderYAI+s60;
						break;
					case "6":
					default:
						o4X = sliderX+sectionW;
						o4Y = sliderYAI+s40;
						break;
				}
				mapOption4BoxL.addPoint(o4X-s4, o4Y+s1);
				mapOption4BoxL.addPoint(o4X-s4, o4Y+sliderH-s2);
				mapOption4BoxL.addPoint(o4X-s13, o4Y+(sliderH/2));
				g.fill(mapOption4BoxL);
				mapOption4BoxR.addPoint(o4X+sliderW+s4, o4Y+s1);
				mapOption4BoxR.addPoint(o4X+sliderW+s4, o4Y+sliderH-s2);
				mapOption4BoxR.addPoint(o4X+sliderW+s13, o4Y+(sliderH/2));
				g.fill(mapOption4BoxR);
				mapOption4Box.setBounds(o4X, o4Y, sliderW, sliderH);
				g.fill(mapOption4Box);
			}
			mapOption2BoxL.addPoint(sliderX-s4, sliderYAI+s1+s40);
			mapOption2BoxL.addPoint(sliderX-s4, sliderYAI+sliderH-s2+s40);
			mapOption2BoxL.addPoint(sliderX-s13, sliderYAI+(sliderH/2)+s40);
			g.fill(mapOption2BoxL);
			mapOption2BoxR.addPoint(sliderX+sliderW+s4, sliderYAI+s1+s40);
			mapOption2BoxR.addPoint(sliderX+sliderW+s4, sliderYAI+sliderH-s2+s40);
			mapOption2BoxR.addPoint(sliderX+sliderW+s13, sliderYAI+(sliderH/2)+s40);
			g.fill(mapOption2BoxR);
			mapOption2Box.setBounds(sliderX, sliderYAI+s40, sliderW, sliderH);
			g.fill(mapOption2Box);
		}

		sliderX += sectionW;
		sizeBoxL.reset();
		sizeBoxL.addPoint(sliderX-s4,sliderYAI+s1);
		sizeBoxL.addPoint(sliderX-s4,sliderYAI+sliderH-s2);
		sizeBoxL.addPoint(sliderX-s13,sliderYAI+(sliderH/2));
		g.fill(sizeBoxL);
		sizeBoxR.reset();
		sizeBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+s1);
		sizeBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+sliderH-s2);
		sizeBoxR.addPoint(sliderX+sliderW+s13,sliderYAI+(sliderH/2));
		g.fill(sizeBoxR);
		sizeBox.setBounds(sliderX, sliderYAI, sliderW, sliderH);
		g.fill(sizeBox);

	sizeOptionBoxL.reset();
		sizeOptionBoxR.reset();
		sizeOptionBox.setBounds(0,0,0,0);
		if (isDynamic()) {
			sizeOptionBoxL.addPoint(sliderX-s4,sliderYAI+s1+s20);
			sizeOptionBoxL.addPoint(sliderX-s4,sliderYAI+sliderH-s2+s20);
			sizeOptionBoxL.addPoint(sliderX-s13,sliderYAI+(sliderH/2)+s20);
			g.fill(sizeOptionBoxL);
			sizeOptionBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+s1+s20);
			sizeOptionBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+sliderH-s2+s20);
			sizeOptionBoxR.addPoint(sliderX+sliderW+s13,sliderYAI+(sliderH/2)+s20);
			g.fill(sizeOptionBoxR);
			sizeOptionBox.setBounds(sliderX, sliderYAI+s20, sliderW, sliderH);
			g.fill(sizeOptionBox);
		}
		sliderX += sectionW;
		diffBoxL.reset();
		diffBoxL.addPoint(sliderX-s4,sliderYAI+s1);
		diffBoxL.addPoint(sliderX-s4,sliderYAI+sliderH-s2);
		diffBoxL.addPoint(sliderX-s13,sliderYAI+(sliderH/2));
		g.fill(diffBoxL);
		diffBoxR.reset();
		diffBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+s1);
		diffBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+sliderH-s2);
		diffBoxR.addPoint(sliderX+sliderW+s13,sliderYAI+(sliderH/2));
		g.fill(diffBoxR);
		diffBox.setBounds(sliderX, sliderYAI, sliderW, sliderH);
		g.fill(diffBox);

		wysiwygBoxL.reset();
		wysiwygBoxL.addPoint(sliderX-s4,sliderYAI+s1+s20);
		wysiwygBoxL.addPoint(sliderX-s4,sliderYAI+sliderH-s2+s20);
		wysiwygBoxL.addPoint(sliderX-s13,sliderYAI+(sliderH/2)+s20);
		g.fill(wysiwygBoxL);
		wysiwygBoxR.reset();
		wysiwygBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+s1+s20);
		wysiwygBoxR.addPoint(sliderX+sliderW+s4,sliderYAI+sliderH-s2+s20);
		wysiwygBoxR.addPoint(sliderX+sliderW+s13,sliderYAI+(sliderH/2+s20));
		g.fill(wysiwygBoxR);
		wysiwygBox.setBounds(sliderX, sliderYAI+s20, sliderW, sliderH);
		g.fill(wysiwygBox);

		// draw settings button
		int smallButtonH = s27; // 27 for 3 buttons // 30 for 2 buttons
		int smallButtonW = scaled(160); // 150 for 3 buttons // 180 for 2 buttons
		// BR: buttons positioning
		// Tune galaxy button
		int yb = 610;
		int xb = 700;
		int dx = 145;
		int dy = 30;
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
            noFogChanged();
            return;
		case KeyEvent.VK_M: // BR: "M" = Go to Main Menu
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
		// BR: Merged UI panel for MOD game options
		else if (hoverBox == compactSetupBox)
			goToMergedStatic();
		else if (hoverBox == compactOptionBox)
			goToMergedDynamic();
		else if (hoverBox == galaxyBox)
			toggleGalaxyGrid(e);
		else if (hoverBox == exitBox)
			doStartBoxAction();
		else if (hoverPolyBox == shapeBoxL)
			opts.shapeSelection().prev();
		else if (hoverBox == shapeBox)
			opts.shapeSelection().toggle(e, this);
		else if (hoverPolyBox == shapeBoxR)
			opts.shapeSelection().next();
		else if (hoverPolyBox == mapOption1BoxL)
			prevMapOption1(true);
		else if (hoverBox == mapOption1Box)
			opts.shapeOption1().toggle(e, this);
		else if (hoverPolyBox == mapOption1BoxR)
			nextMapOption1(true);
		else if (hoverPolyBox == mapOption2BoxL)
			opts.shapeOption2().prev();
		else if (hoverBox == mapOption2Box)
			opts.shapeOption2().toggle(e, this);
		else if (hoverPolyBox == mapOption2BoxR)
			opts.shapeOption2().next();
		else if (hoverBox == mapOption3Box)
			opts.shapeOption3().toggle(e, this);
		else if (hoverPolyBox == mapOption4BoxL)
			opts.shapeOption4().prev();
		else if (hoverBox == mapOption4Box)
			opts.shapeOption4().toggle(e, this);
		else if (hoverPolyBox == mapOption4BoxR)
			opts.shapeOption4().next();
		else if (hoverPolyBox == sizeBoxL)
			opts.sizeSelection().prev();
		else if (hoverBox == sizeBox)
			opts.sizeSelection().toggle(e, this);
		else if (hoverPolyBox == sizeBoxR)
			opts.sizeSelection().next();
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
			prevGlobalAbilities(true);
		else if (hoverBox == abilitiesBox)
			toggleGlobalAbilities(e);
		else if (hoverPolyBox == abilitiesBoxR)
			nextGlobalAbilities(true);
		else if (hoverBox == newRacesBox)
			toggleNewRaces(true);
		else if (hoverBox == showAbilitiesBox)
			toggleShowAbility(true);
		else if (hoverPolyBox == diffBoxL)
			opts.difficultySelection().prev();
		else if (hoverBox == diffBox)
			opts.difficultySelection().toggle(e, this);
		else if (hoverPolyBox == diffBoxR)
			opts.difficultySelection().next();
		else if (hoverPolyBox == wysiwygBoxL)
			opts.galaxyRandSource().prev();
		else if (hoverBox == wysiwygBox)
			opts.galaxyRandSource().toggle(e, this);
		else if (hoverPolyBox == wysiwygBoxR)
			opts.galaxyRandSource().next();
		else if (hoverPolyBox == oppBoxU)
			opts.aliensNumber().next();
		else if (hoverBox == oppBox)
			opts.aliensNumber().toggle(e, this);
		else if (hoverPolyBox == oppBoxD)
			opts.aliensNumber().prev();
		else {
			for (int i=0;i<oppSet.length;i++) {
				if (hoverBox == oppAI[i]) {
					toggleSpecificOpponentAI(i, true, up, mid);
					break;
				}
				else if (hoverBox == oppAbilities[i]) {
					toggleSpecificOpponentAbilities(i, true, up, mid);
					break;
				}
				else if (hoverBox == oppSet[i]) {
					toggleOpponent(i, true, up, mid);
					break;
				}
			}
		}
	}
	@Override public void mouseWheelMoved(MouseWheelEvent e) {
		boolean up = e.getWheelRotation() > 0;
		if (hoverBox == shapeBox)  {
			opts.shapeSelection().toggle(e);
		}
		else if (hoverBox == galaxyBox) {
			toggleEmpireSpacing(e);
		}
		else if (hoverBox == mapOption1Box) {
			opts.shapeOption1().toggle(e);
		}
		else if (hoverBox == mapOption2Box) {
			opts.shapeOption2().toggle(e);
		}
		else if (hoverBox == mapOption4Box) {
			opts.shapeOption4().toggle(e);
		}
		else if (hoverBox == sizeBox) {
			opts.sizeSelection().toggle(e);
		}
		else if (hoverBox == sizeOptionBox) {
			opts.dynStarsPerEmpire().toggle((MouseEvent)null, e, this);
		}
		else if (hoverBox == aiBox) {
			if (up)
				prevOpponentAI(false);
			else
				nextOpponentAI(false);
		}
		else if (hoverBox == abilitiesBox) {
			if (up)
				prevGlobalAbilities(false);
			else
				nextGlobalAbilities(false);
		}
		else if (hoverBox == newRacesBox) {
			toggleNewRaces(false);
		}
		else if (hoverBox == showAbilitiesBox) {
			toggleShowAbility(false);
		}
		else if (hoverBox == diffBox)
		 {
			opts.difficultySelection().toggle(e);
		}
		else if (hoverBox == wysiwygBox)
		 {
			opts.galaxyRandSource().toggle(e);
		}
		else if (hoverBox == oppBox) {
			opts.aliensNumber().toggle(e);
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
					toggleSpecificOpponentAbilities(i, false, up, false);
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

	// ########## SUB-CLASSES ##########
	private class ParamListOpponentAI extends ParamList { // For Guide
		private ParamListOpponentAI(String gui, String name, List<String> list, String defaultValue) {
			super(gui, name, list, defaultValue);
			this.setDefaultValue(MOO1_DEFAULT, IGameOptions.baseAI.aliensKey);
			this.setDefaultValue(ROTP_DEFAULT, IGameOptions.baseAI.aliensKey);
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
			Race race = Race.keyed(key);
			String name = race.setupName();
			return name; 
		}
		@Override public String getRowGuide(int id)	{
			// System.out.println("id = " + id + " " + this.getGuiValue(id));
			String key, help;
			key = getGuiValue(id);
			if (key == null || key.equals(opponentRandom))
				help = labelFormat(opponentRandom) + "Surprise me!";
			else {
				Race   race		= Race.keyed(key);
				String raceName = race.setupName();
				help = labelFormat(raceName) + race.getDescription1()
						+ "<br>" + race.getDescription2()
						+ "&ensp /&ensp " + race.getDescription3()
						+ "&ensp /&ensp " + race.getDescription4();
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
				Race   race		= Race.keyed(key);
				String raceName = race.setupName();
				help = labelFormat(raceName) + race.getDescription1()
						+ "<br>" + race.getDescription2()
						+ "<br>" + race.getDescription3()
						+ "<br>" + race.getDescription4();
			}
			return tableFormat(help);
		}
	};
    private class ParamListGlobalAbilities extends ParamList { // For Guide
    	private ParamListGlobalAbilities(String gui, String name, List<String> list, String defaultValue) {
			super(gui, name, list, defaultValue);
		}
		@Override public String	getOptionValue(IGameOptions options) {
			return guiOptions().selectedUseGlobalCROptions();
		}
		@Override public String	guideValue()	{ return text(get()); }
		@Override public String getRowGuide(int id)	{
			String key  = getGuiValue(id);
			String help = realLangLabel(key+LABEL_DESCRIPTION);
			if (help != null)
				return rowFormat(labelFormat(name(id)), help);

			Race   race		= fileToAlienRace(key);
			String raceName = race.setupName;
			if (key.startsWith(BASE_RACE_MARKER))
				help = labelFormat(name(id)) + "<i>(Original species)</i>&nbsp " + race.getDescription1();
			else
				help = labelFormat(raceName) + race.getDescription1();
			help += "<br>" + race.getDescription2()
					+ "&ensp /&ensp " + race.getDescription3()
					+ "&ensp /&ensp " + race.getDescription4();
			return help;
		}
		@Override public String setFromIndex(int index) {
			String value = super.setFromIndex(index);
			guiOptions().globalCROptions().set(value);
			return value;
		}
		@Override public String set(String value) {
			super.set(value);
			guiOptions().globalCROptions().set(value);
			return value;
		}
		@Override public boolean prev() {
			prevGlobalAbilities(true);
			return false;
		}
		@Override public boolean next() {
			nextGlobalAbilities(true);
			return false;
		}
	};
	private class ParamListSpecificAbilities extends ParamList { // For Guide
		ParamListSpecificAbilities(String gui, String name, List<String> list, String defaultValue) {
			super(gui, name, list, defaultValue);
		}
		@Override public String	getOptionValue(IGameOptions options)	{
			return options.specificOpponentCROption(mouseBoxIndex()+1);
		}
		@Override public String	guideValue()	{ return text(get()); }
		@Override public String getRowGuide(int id)	{
			String key  = getGuiValue(id);
			String help = realLangLabel(key+LABEL_DESCRIPTION);
			if (help != null)
				return rowFormat(labelFormat(name(id)), help);

			Race   race		= fileToAlienRace(key);
			String raceName = race.setupName;
			if (key.startsWith(BASE_RACE_MARKER))
				help = labelFormat(name(id)) + "<i>(Original species)</i>&nbsp " + race.getDescription1();
			else
				help = labelFormat(raceName) + race.getDescription1();
			help += "<br>" + race.getDescription2()
					+ "&ensp /&ensp " + race.getDescription3()
					+ "&ensp /&ensp " + race.getDescription4();
			return help;
		}
	};
	private class BitmapFileChooser extends JFileChooser {
		private static final long serialVersionUID = 1L;
		@Override protected JDialog createDialog(Component parent)
                throws HeadlessException {
            JDialog dlg = super.createDialog(parent);
            dlg.setLocation(scaled(300), scaled(200));
            dlg.setSize(scaled(420), scaled(470));
            dlg.getContentPane().setBackground(GameUI.borderMidColor());
            return dlg;
        }
	}
	private class BMPropertyChangeListener implements PropertyChangeListener {
		private final JFileChooser bmFileChooser;
		private BMPropertyChangeListener(JFileChooser fileChooser) {
			bmFileChooser = fileChooser;
		}
		@Override
        public void propertyChange(PropertyChangeEvent evt) {
			if (bmFileChooser.getSelectedFile() != null) {
				preview(bmFileChooser.getSelectedFile().getPath(), opts.shapeOption3());
			}
        }
	}
}
