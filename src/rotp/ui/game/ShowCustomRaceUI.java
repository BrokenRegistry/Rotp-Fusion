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
import static rotp.model.empires.species.SpeciesSettings.ROOT;
import static rotp.ui.game.SetupGalaxyUI.specificAI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;

import rotp.model.ai.AIList;
import rotp.model.empires.Empire;
import rotp.model.empires.species.ICRSettings;
import rotp.model.empires.species.SettingBase;
import rotp.model.empires.species.SkillsFactory;
import rotp.model.game.IGameOptions;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.races.RacesUI;
import rotp.ui.util.IParam;
import rotp.ui.util.ListDialogUI;
import rotp.util.FontManager;
import rotp.util.ModifierKeysState;

public class ShowCustomRaceUI extends BaseModPanel {
	private static final long serialVersionUID	= 1L;
	private static final String	 playerAIOffKey	= "SETUP_OPPONENT_AI_PLAYER";
	private static final String	 totalCostKey	= ROOT + "GUI_COST";
	private static final String	 malusCostKey	= ROOT + "GUI_MALUS";

	private	static final int	 tooltipPadV	= s10;
	private	static final int	 descPadM		= s5;
	private static final int	 descLineH		= s18;
	private	static final Font	 descFont		= FontManager.current().narrowFont(14);

	protected static final Color textC			= SystemPanel.whiteText;
	protected static final int	 buttonPad		= s15;
	protected static final int	 buttonPadV		= tooltipPadV;
	protected static final int	 xButtonOffset	= s30;
	protected static final Color labelC			= SystemPanel.orangeText;
	protected static final int	 labelFontSize	= 14;
	protected static final int	 labelH			= s16;
	protected static final int	 labelPad		= s8;
	protected static final int	 columnPad		= s12;

	private static final Color	costC			= SystemPanel.blackText;
	private static final Color	malusC			= SystemPanel.redText;
	private static final int	costFontSize	= 18;
	private	static final Font	titleFont		= FontManager.current().narrowFont(30);
	private static final int	titleOffset		= s30; // Offset from Margin
	private static final int	costOffset		= s25; // Offset from title
	private static final int	titlePad		= s80; // Offset of first setting
	private static final int	raceAIH			= s18;

	private static final Color	frameC			= SystemPanel.blackText; // Setting frame color
	private static final Color	settingNegC		= SettingBase.settingNegC; // Setting name color
	private static final Color	settingC		= SettingBase.settingC; // Setting name color
	private static final int	settingFont		= 14;
	protected static final int	settingH		= s16;
	private	  static final int	spacerH			= s10;
	protected static final int	settingHPad		= s4;
	private static final int	frameShift		= s5;
	private static final int	frameTopPad		= 0;
	private static final int	frameSizePad	= s10;
	private static final int	frameEndPad		= s4;
	private static final int	settingIndent	= s10;
	private static final int	wFirstColumn	= RotPUI.scaledSize(200);
	private static final int	wSetting		= RotPUI.scaledSize(200);
	protected int currentWidth = wFirstColumn;

	private static final Color optionC		= SystemPanel.blackText; // Unselected option Color
	private static final Color selectC		= SystemPanel.whiteText;  // Selected option color
	private static final int   optionFont	= 13;
	private static final int   optionH		= s15;
	private static final int   optionIndent	= s15;
	private static SkillsFactory cr;

	// This should be the last static to be initialized
	private static final ShowCustomRaceUI instance = new ShowCustomRaceUI();
	public static void languageChanged()	{
		if (instance != null)
			instance.cr().languageChanged();
	}

	private List<Integer> colSettingsCount;
	private	List<Integer> spacerList;
	private List<Integer> columnList;
	List<ICRSettings>  commonList;
	protected List<ICRSettings> settingList;
	protected List<ICRSettings> mouseList;
	
	protected String guiTitleID;

	private int numColumns	= 0;
	private int columnsMaxH	= 0;
	private int columnH		= RotPUI.scaledSize(60); // For the Random options
	private int numSettings	= 0;
	private	int descLines	= 2;
	private	int descHeigh	= descLines * descLineH + descPadM;

	private int yTitle;
	private int xCost;
	protected int yCost;
	private int w;
	private int h;
	private int settingSize;
	private int topM;
	private int yTop;
	private int descWidth;
	protected int xDesc, yDesc;
	protected int leftM;
	protected int xLine, yLine; // settings var

	protected BasePanel parent;
	@Override protected Box newExitBox() { return new Box("SETTINGS_EXIT"); }
	private	  final Box raceAIBox		= new Box(ROOT + "RACE_AI");
	private	  final JTextPane descBox	= new JTextPane();
	protected ModText totalCostText;
	private   ModText malusCostText;
	private	  RacesUI  raceUI; // Parent panel
	protected int maxLeftM;
	protected boolean initialized = false;
	private   boolean forceUpdate = true;

	// ========== Constructors and initializers ==========
	//
	protected ShowCustomRaceUI() {
		setOpaque(false);
		add(descBox);
		descBox.setOpaque(true);
		descBox.setContentType("text/html");
		descBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
	    totalCostText = new ModText(this, costFontSize, 
	    		costC, costC, hoverC, depressedC, costC, false);
	    malusCostText = new ModText(this, costFontSize, 
	    		malusC, malusC, hoverC, depressedC, malusC, false);
	}
	public static ShowCustomRaceUI instance() 				{ return instance.init0(); }
	public static SkillsFactory displayedSpecies()	{ return cr; }

	private ShowCustomRaceUI init0() {
		if (initialized)
			return this;
		initialized = true;
		cr(SkillsFactory.getSkillsFactoryForEditor(this));		
		maxLeftM	= scaled(80);
		guiTitleID	= ROOT + "SHOW_TITLE";
	    commonList	= settingList;
	    mouseList	= settingList;
		addMouseListener(this);
		addMouseMotionListener(this);
	    initGUI();		
		return this;
	}
	public void loadRace(IGameOptions options)		{ // For Race Diplomatic UI Panel
		forceUpdate = true;
		Empire emp = raceUI.selectedEmpire();
		emp.initCRToShow(cr);
		if (emp.isCustomSpecies())
			guiTitleID	= ROOT + "SHOW_TITLE_CUSTOM";
		else
			guiTitleID	= ROOT + "SHOW_TITLE";
	}
	public void init(RacesUI p)	{ // For Race Diplomatic UI Panel
		forceUpdate = true;
		super.init();
		raceUI = p;
	}
	protected void initGUI()	{
		colSettingsCount = new LinkedList<>();
		columnList	= cr().columnList();
		spacerList	= cr().spacerList();
		settingList	= cr().settingList();
		settingSize	= settingList.size();
	    mouseList	= settingList;
	    forceUpdate = true;

		for (int i=0; i<settingSize; i++) {
			if (spacerList.contains(i))
				columnH += spacerH;
			if (columnList.contains(i))
				endOfColumn();
			initSetting(settingList.get(i));
			numSettings++;
		}
		endOfColumn();
	}
	protected void initSetting(ICRSettings setting) {
		if (setting.isBullet()) {
			setting.settingText(settingBT());
			columnH += settingH + frameSizePad;
			columnH += frameTopPad;
			int paramIdx	= setting.index();
			int bulletStart	= setting.bulletStart();
			int bulletSize	= setting.bulletBoxSize();
			for (int bulletIdx=0; bulletIdx < bulletSize; bulletIdx++) {
				int optionIdx = bulletStart + bulletIdx;
				setting.optionText(optionBT(), bulletIdx);
				setting.optionText(bulletIdx).disabled(optionIdx == paramIdx);
				columnH	+= optionH;
			}
			columnH += frameEndPad;
		} else {
			setting.settingText(settingBT());			
			columnH += settingH;
		}
		columnH += settingHPad;
	}
	public void open(BasePanel p) {
		forceUpdate = true;
		enableGlassPane(this);
		ModifierKeysState.reset();
		parent = p;
		init();
		repaint();
	}
	// ========== Other Methods ==========
	//
	SkillsFactory cr()			{ return cr; }
	void cr(SkillsFactory cr)	{ ShowCustomRaceUI.cr = cr; }
	private void setDesc(String tt)		{
		descBox.setText(tt);
		loadGuide();
		if (hoverBox != null && hoverBox != prevHover)
			repaint(hoverBox);
	}
	private	  ModText settingBT()		{
		return new ModText(this, settingFont, settingC, settingNegC, hoverC, depressedC, textC, false);
	}
	protected ModText optionBT()		{
		return new ModText(this, optionFont, optionC, selectC, hoverC, depressedC, textC, false);
	}
	private void endOfColumn()			{
		columnH -= 2 * settingH; // to fit the reality... Debug needed
		columnsMaxH = max(columnsMaxH, columnH);
		colSettingsCount.add(numSettings);
		numColumns++;
		numSettings	= 0;
		columnH		= 0;
	}
	private boolean isPlayer()			{ return raceUI.selectedEmpire().isPlayer(); }

	private String selectAIFromList(String[] choiceArray, List<String> returnList,
									String initialLabel, IParam param) {
		String message = "Make your choice";
		String initialChoice = text(initialLabel);
		ListDialogUI dialog = RotPUI.instance().listDialog();
		dialog.init(
		    	this, getParent(),			// Frame & Location component
		    	message,					// Message
		        "Empire AI selection",		// Title
		        choiceArray,				// List
		        initialChoice, 				// Initial choice
		        "XXXXXXXXXXXXXXXX",			// long Dialogue
		        true,						// isVertical
		        -1, -1,						// Position
		        scaled(220), scaled(220),	// size
				null, null,					// Font, Preview
				returnList,					// Alternate return
				param); 					// help parameter
		String input = (String) dialog.showDialog(0);
	    if (input == null)
	    	return initialChoice;
	    return input;
	}
	private void playerAIBoxAction()	{
		AIList list				= IGameOptions.changePlayAIset();
		List<String> returnList = list.getAutoPlay();
		String[] choiceArray	= list.getNames().toArray(new String[list.size()]);

		IGameOptions opts = raceUI.options();
		String aiNewKey = selectAIFromList(choiceArray, returnList, opts.selectedAutoplayOption(), opts.autoplay());
		opts.selectedAutoplayOption(aiNewKey);
		raceUI.selectedEmpire().changePlayerAI(aiNewKey);
	}
	private void alienAIBoxAction()		{
		AIList list				= IGameOptions.changeAlienAIset();
		List<String> returnList = list.getAliens();
		String[] choiceArray	= list.getNames().toArray(new String[list.size()]);
		Empire emp		= raceUI.selectedEmpire();
		String aiNewKey = selectAIFromList(choiceArray, returnList, emp.getAiName(), specificAI());
		emp.changeOpponentAI(aiNewKey);
	}
	private void raceAIBoxAction()		{
		if (isPlayer())
			playerAIBoxAction();
		else
			alienAIBoxAction();
		repaint();
	}
	protected  String totalCostStr()	{ return text(totalCostKey, Math.round(cr().getTotalCost())); }
	private  String malusCostStr()		{ return text(malusCostKey, Math.round(cr().getMalusCost())); }

	private boolean checkForHoveredSettings(List<ICRSettings> settings) {
		for (ICRSettings setting : settings) {
			if (setting.settingText().contains(mX,mY)) {
				hoverBox = setting.settingText().box();
				setDesc(setting.getToolTip());
				if (hoverBox != prevHover) {
					setting.settingText().mouseEnter();
					setting.updated(true);
					repaint(hoverBox);
				}
				return true;
			}
			if (setting.isBullet()) {
				int idx = 0;
				for (ModText txt : setting.optionsText()) {
					if (txt.contains(mX,mY)) {
						hoverBox = txt.box();
						setDesc(setting.getToolTip(idx));
						if (hoverBox != prevHover) {
							txt.mouseEnter();
							setting.updated(true);
							repaint(hoverBox);
						}
						return true;
					}
					idx++;
				}
			}
		}
		return false;
	}
	protected String raceAIButtonTxt()	{
		if (isPlayer())
			if (raceUI.selectedEmpire().isAIControlled())
				return raceUI.selectedEmpire().getAiName();
			else
				return text(playerAIOffKey);
		else
			return raceUI.selectedEmpire().getAiName();
	}
	protected int getBackGroundWidth()	{
		return columnPad+wFirstColumn+columnPad + (wSetting+columnPad) * (numColumns-1);
	}
	protected void paintSetting(Graphics2D g, ICRSettings setting) {
		boolean refresh = forceUpdate || setting.updated();
		if (refresh)
			setting.drawSetting(frameSizePad, frameEndPad, optionH, currentWidth,
					frameC, frameShift, xLine, yLine, settingIndent,
					s12, settingH, frameTopPad, wSetting, optionIndent, retina, retinaFactor);
		if (retina) {
			BufferedImage img = setting.getImage();
			int y = yLine -s12;
			int w = img.getWidth();
			int h = img.getHeight();
			g.drawImage(img, xLine, y, xLine+invRetina(w), y+invRetina(h), 0, 0, w, h, null);
		}
		else
			g.drawImage(setting.getImage(),xLine, yLine -s12, null);
		yLine += setting.deltaYLines();
	}
	private void paintDescriptions(Graphics2D g) {
		descBox.setFont(descFont);
		descBox.setBackground(GameUI.setupFrame());
		descBox.setBounds(xDesc, yDesc, descWidth, descHeigh);
		descBox.setVisible(true);
	}
	protected void paintButtons(Graphics2D g) {
		g.setFont(smallButtonFont());

		// Exit Button
		String text = text(exitButtonKey());
		int sw = g.getFontMetrics().stringWidth(text);
		int buttonW	= exitButtonWidth(g);
		xButton = leftM + wGist() - buttonW - buttonPad;
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(exitBox.x, exitBox.y, buttonW, smallButtonH, cnr, cnr);
		int xT = exitBox.x+((exitBox.width-sw)/2);
		int yT = exitBox.y+exitBox.height-s8;
		Color cB = hoverBox == exitBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		Stroke prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(exitBox.x, exitBox.y, exitBox.width, exitBox.height, cnr, cnr);
		g.setStroke(prev);

		// Guide Button
		text	= text(guideButtonKey());
		xButton = leftM + buttonPad;
		sw		= g.getFontMetrics().stringWidth(text);
		buttonW = g.getFontMetrics().stringWidth(text) + smallButtonMargin;
		guideBox.setBounds(xButton+s2, yButton, buttonW, smallButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(guideBox.x, guideBox.y, buttonW, smallButtonH, cnr, cnr);
		xT = guideBox.x+((guideBox.width-sw)/2);
		yT = guideBox.y+guideBox.height-s8;
		cB = hoverBox == guideBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(guideBox.x, guideBox.y, guideBox.width, guideBox.height, cnr, cnr);
		g.setStroke(prev);
	}
	protected void initButtonsBounds(Graphics2D g) {
		g.setFont(smallButtonFont());

		// Exit Button
		String text = text(exitButtonKey());
		int buttonW	= exitButtonWidth(g);
		xButton = leftM + wGist() - buttonW - buttonPad;
		exitBox.setBounds(xButton, yButton+s2, buttonW, smallButtonH);

		// Guide Button
		text	= text(guideButtonKey());
		xButton = leftM + buttonPad;
		buttonW = g.getFontMetrics().stringWidth(text) + smallButtonMargin;
		guideBox.setBounds(xButton+s2, yButton, buttonW, smallButtonH);

		// RaceUI Button
		text = "AI: Character";
		int sw = g.getFontMetrics().stringWidth(text);
		buttonW	= sw + smallButtonMargin;
		int xAI = leftM + wGist() - columnPad - buttonW;
		int yAI	= yCost - raceAIH - s10;
		raceAIBox.setBounds(xAI, yAI, buttonW, smallButtonH);
	}
	protected void initFixButtons(Graphics2D g) {
		// RaceUI Button
        Stroke prev = g.getStroke();
		setSmallButtonGraphics(g);
		g.fillRoundRect(raceAIBox.x, raceAIBox.y, raceAIBox.width, raceAIBox.height, cnr, cnr);
        drawButton(g, true, raceAIBox,	raceAIButtonTxt());
        g.setStroke(prev);
	}
	protected void drawFixButtons(Graphics2D g, boolean all) {
		initFixButtons(g);
		Stroke prev;
		g.setFont(smallButtonFont());
		// left button
		String text = raceAIButtonTxt();
		int sw = g.getFontMetrics().stringWidth(text);
		int x = raceAIBox.x+((raceAIBox.width-sw)/2);
		int y = raceAIBox.y+raceAIBox.height*75/100;
		Color c = hoverBox == raceAIBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, x, y, GameUI.borderDarkColor(), c);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(raceAIBox.x, raceAIBox.y, raceAIBox.width, raceAIBox.height, cnr, cnr);
		g.setStroke(prev);
	}
	// ========== Overriders ==========
	//
	@Override protected void close() {
		super.close();
		for (ICRSettings setting : settingList)
			setting.clearImage();
	}
	@Override protected void drawButtons(Graphics2D g, boolean init) {
        Stroke prev = g.getStroke();
        
        if (init)
            g.setFont(bigButtonFont(retina));
        else
        	g.setFont(bigButtonFont(false));
        drawButton(g, init, exitBox,	text(exitButtonKey()));

        if (init)
        	g.setFont(smallButtonFont(retina));
        else
        	g.setFont(smallButtonFont());
        drawButton(g, init, guideBox,	text(guideButtonKey()));
        g.setStroke(prev);
	}
    @Override public BufferedImage initButtonBackImg() {
    	initButtonPosition();
		buttonBackImg = new BufferedImage(retina(wButton), retina(hButton), TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) buttonBackImg.getGraphics();
		setRenderingHints(g);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// draw EXIT button
		setBigButtonGraphics(g);
		exitBox.fillButtonBackImg(g);

		// draw GUIDE button
		setSmallButtonGraphics(g);
		guideBox.fillButtonBackImg(g);
		
		drawButtons(g, true); // init = true; local = true
		return buttonBackImg;
    }
	@Override protected void initBackImg() {
		long timeStart = System.currentTimeMillis();
		w	= getWidth();
		h	= getHeight();
		wGist(getBackGroundWidth());
		hGist	= titlePad + columnsMaxH + tooltipPadV + descHeigh + buttonPadV + smallButtonH + buttonPadV;

		// Set the base top Margin
		// Set the final High
		topM	= (h - hGist)/2;
		yButton	= topM + hGist - buttonPadV - smallButtonH;

		yTop	= topM + titlePad; // First setting top position
		leftM	= Math.min((w - wGist())/2, maxLeftM);
		yTitle	= topM + titleOffset;
		yDesc	= yButton - buttonPadV - descHeigh;
		yCost 	= yTitle + costOffset;
		xCost	= leftM + columnPad/2;
		xDesc	= leftM + columnPad;

		backImg = newOpaqueImage(w, h);
		Graphics2D g = (Graphics2D) backImg.getGraphics();
		g.setFont(descFont);
		// modnar: use (slightly) better upsampling
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// background image
		Image back = GameUI.defaultBackground;
		int imgW = back.getWidth(null);
		int imgH = back.getHeight(null);
		g.drawImage(back, 0, 0, w, h, 0, 0, imgW, imgH, this);

		g.setPaint(bg());
		g.fillRect(leftM, topM, wGist(), hGist);

		// Title
		g.setFont(titleFont);
		String title = text(guiTitleID);
		int sw = g.getFontMetrics().stringWidth(title);
		int xTitle = leftM + (wGist() - sw)/2;
		drawBorderedString(g, title, 1, xTitle, yTitle, Color.black, Color.white);

		initButtonsBounds(g);
		initFixButtons(g);
		drawFixButtons(g, true);

		initButtonBackImg();
		g.dispose();
		if (showTiming) 
			System.out.println("initBackImg() Time = " + (System.currentTimeMillis()-timeStart));
	}
	protected void reInitFields()	{
		forceUpdate = true;
		for (ICRSettings setting : commonList) {
			if (setting.isBullet()) {
				setting.settingText().displayText(setting.guiSettingDisplayStr()); // The setting
				int bulletStart	= setting.bulletStart();
				int bulletSize	= setting.bulletBoxSize();
				for (int bulletIdx=0; bulletIdx < bulletSize; bulletIdx++) {
					int optionIdx = bulletStart + bulletIdx;
					setting.optionText(bulletIdx).displayText(setting.guiCostOptionStr(optionIdx));
				}
			}
			else {
				setting.settingText().displayText(setting.guiSettingDisplayStr());			
			}
		}
		totalCostText.displayText(totalCostStr());
		totalCostText.disabled(true);
		malusCostText.displayText(malusCostStr());
		malusCostText.disabled(true);
		descBox.setText("<b>Shift</b>&nbsp and <b>Ctrl</b>&nbsp can be used to change buttons, click and scroll functions");
	}
	@Override protected void init()	{
		super.init();
		reInitFields();
	}
	@Override public boolean checkModifierKey(InputEvent e) {
		boolean change = checkForChange(e);
		return change;
	}
	@Override protected String GUI_ID()				  { return ""; }
	@Override public void paintComponent(Graphics g0) {
		//showTiming = true;
		if (showTiming)
			System.out.println("===== ShowCustomRace PaintComponents =====");
		long timeStart = System.currentTimeMillis();
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;

		// background image
		g.drawImage(backImg(), 0, 0, this);
		// Buttons background image
		drawButtons(g);
		drawFixButtons(g, false);

		// Tool tip
		paintDescriptions(g);

		// Total cost
		totalCostText.displayText(totalCostStr());
		totalCostText.setScaledXY(xCost, yCost);
		totalCostText.draw(g);
		// Penalty cost
		int sw = totalCostText.w();
		malusCostText.displayText(malusCostStr());
		malusCostText.setScaledXY(xCost + sw + s2, yCost);
		malusCostText.draw(g);

		// Loop thru the parameters
		xLine = leftM+s10;
		yLine = yTop;
		currentWidth	= wFirstColumn;
		descWidth	= wGist() - 2 * columnPad;

		// First column (left)
		// Loop thru parameters
		Stroke prev = g.getStroke();
		g.setStroke(stroke2);
		for (int i=0; i<settingSize; i++) {
			if (spacerList.contains(i))
				yLine += spacerH;
			if (columnList.contains(i)) {
				xLine = xLine + currentWidth + columnPad;
				currentWidth = wSetting;
				yLine = yTop;
			}
			paintSetting(g, settingList.get(i));
			yLine += settingHPad;
		}
		g.setStroke(prev);

		showGuide(g);

		// ready for extension
		xLine = xLine + currentWidth + columnPad;
		yLine = yTop;
		forceUpdate = false;
		if (showTiming)
			System.out.println("ShowCustomRace paintComponent() Time = " + (System.currentTimeMillis()-timeStart));	
	}
	@Override public void keyReleased(KeyEvent e)	  {
		if(checkModifierKey(e)) {
			if(hoverBox != null)
				descBox.setText(hoverBox.getDescription());
			repaintButtons();
		}
	}
	@Override public void keyPressed(KeyEvent e)	  {
		if(checkModifierKey(e)) {
			if(hoverBox != null)
				descBox.setText(hoverBox.getDescription());
			repaintButtons();
		}
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				close();
				return;
			default:
				break;
		}
		super.keyPressed(e);
	}
	@Override public void mouseMoved(MouseEvent e)	  {
		mX = e.getX();
		mY = e.getY();
		if (hoverBox != null && hoverBox.contains(mX,mY)) {
			hoverChanged = false;
			return;
		}
		descBox.setText("");
		hoverChanged = true;
		prevHover	 = hoverBox;
		hoverBox	 = null;
		// Go thru the buttons and restore the boxes
		for (Box box : boxHelpList)
			if (box.checkIfHovered(descBox))
				break;
		if (prevHover != null) {
			prevHover.mouseExit();
			if (hoverBox == null)
				descBox.setText("");
			loadGuide();
			repaint();
		}
		// Check if cursor is in a box
		checkForHoveredSettings(mouseList);
	}
	@Override public void mouseReleased(MouseEvent e) {
		if (e.getButton() > 3)
			return;
		if (hoverBox == null)
			return;
		if (hoverBox == exitBox) {
			close();
			return;
		}
		if (hoverBox == guideBox) {
			doGuideBoxAction();
			return;
		}
		if (hoverBox == raceAIBox) {
			raceAIBoxAction();
			return;
		}
	}
}
