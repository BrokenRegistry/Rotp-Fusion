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
import static rotp.model.empires.CustomRaceDefinitions.ROOT;
import static rotp.model.game.IBaseOptsTools.LIVE_OPTIONS_FILE;
import static rotp.ui.util.IParam.langLabel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import rotp.Rotp;
import rotp.model.empires.CustomRaceDefinitions;
import rotp.model.empires.CustomRaceDefinitions.RaceList;
import rotp.model.empires.Race;
import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.model.game.IMainOptions;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.util.ICRSettings;
import rotp.ui.util.InterfaceOptions;
import rotp.ui.util.ParamButtonHelp;
import rotp.ui.util.ParamCR;
import rotp.util.LabelManager;
import rotp.util.ModifierKeysState;

public class EditCustomRaceUI extends ShowCustomRaceUI implements MouseWheelListener {
	private static final long serialVersionUID	= 1L;
	private static final String GUI_ID			= "CUSTOM_RACE";
	private static final String selectKey		= ROOT + "GUI_SELECT";
	private static final String randomKey		= ROOT + "GUI_RANDOM";
	private static final String randomGetKey	= ROOT + "GUI_RANDOM_GET";
	private static final String randomPushKey	= ROOT + "GUI_RANDOM_PUSH";
	private static final String saveCurrentKey	= ROOT + "GUI_SAVE";
	private static final String loadCurrentKey	= ROOT + "GUI_LOAD";
	private static final int	raceListW		= RotPUI.scaledSize(180);
	private static final int	speciesDirH		= s16;
	
	private static final ParamButtonHelp loadButtonHelp = new ParamButtonHelp( // For Help Do not add the list
			"CUSTOM_RACE_BUTTON_LOAD",
			saveCurrentKey,
			"",
			loadCurrentKey,
			"");
	private	static final EditCustomRaceUI instance		= new EditCustomRaceUI();
	
	private final Box selectBox		= new Box(selectKey);
	private final Box randomBox		= new Box(randomKey);
	private final Box randomGetBox	= new Box(randomGetKey);
	private final Box randomPushBox	= new Box(randomPushKey);
	private final Box loadBox		= new Box(loadButtonHelp);

	private LinkedList<ICRSettings> guiList;
	private RaceList raceList;
	private int yRandB, yRandGetB, xRandPushB;
	
	private	  final Box speciesDirBox	= new Box(ROOT + "SPECIES_DIR");
	
	// ========== Constructors and initializers ==========
	//
	private EditCustomRaceUI() {}
	public static EditCustomRaceUI instance() { return instance.init0(); }
	public EditCustomRaceUI init0() {
		if (initialized)
			return this;
		initialized = true;
		cr(new CustomRaceDefinitions());		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		maxLeftM	= scaled(999);
		guiTitleID	= ROOT + "GUI_TITLE";
	    initGUI();		

		guiList = cr().guiList();
	    for(ICRSettings setting : guiList)
	    	setting.settingText(new ModText(this, labelFontSize,
					labelC, labelC, hoverC, depressedC, textC, false));
	    raceList = cr().initRaceList();
	    initSetting(raceList);

	    commonList = new LinkedList<>();
	    commonList.addAll(settingList);
	    commonList.addAll(guiList);
	    
	    mouseList = new LinkedList<>();
	    mouseList.addAll(commonList);
	    mouseList.add(raceList);
		return this;
	}
	private void reloadRaceList(boolean foldersRework) {
		raceList.reload(foldersRework);
		int paramIdx	= raceList.index();
		int bulletStart	= raceList.bulletStart();
		int bulletSize	= raceList.bulletBoxSize();
		for (int bulletIdx=0; bulletIdx < bulletSize; bulletIdx++) {
			int optionIdx = bulletStart + bulletIdx;
			raceList.optionText(optionBT(), bulletIdx);
			raceList.optionText(bulletIdx).disabled(optionIdx == paramIdx);
		}
		reInitFields();
	}
	// ========== Other Methods ==========
	//
	static void updatePlayerCustomRace() {
		if (instance == null)
			return;
		if (instance.cr() == null)
			return;
		instance.guiOptions().selectedPlayerCustomRace(instance.cr().getAsOptions());
	}
	private void saveCurrentRace() {
		IGameOptions opts = guiOptions();
		if (opts.selectedPlayerIsCustom()) {
			ParamCR pl	 =  opts.playerCustomRace();
			Race plRace	 = pl.getRace();
			String plKey = plRace.id();
			String crKey = cr().getRaceKey();
			if (crKey.equals(plKey))
				opts.selectedPlayerCustomRace(cr().getAsOptions());
		}
		cr().saveRace();
	}
	private void loadCurrentRace() { cr().loadRace(); }
	private void doLoadBoxAction() { // Local to panel
		buttonClick();
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT: // Save
			loadCurrentRace();
			break;
		default: // Load
			saveCurrentRace();
			break; 
		}
		reloadRaceList(false);
		repaint();
	}
	private void doSelectBoxAction() {
		buttonClick();
		guiOptions().selectedPlayerCustomRace(cr().getAsOptions());
		guiOptions().selectedPlayerIsCustom(true);
		guiOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
		close();
	}
	public void updateCRGui(IGameOptions source) {
		for (InterfaceOptions param : commonList)
			param.updateOptionTool(source.dynOpts());
		writeLocalOptions(guiOptions());
	}
	private void writeLocalOptions(IGameOptions destination) {
		for (InterfaceOptions param : commonList)
			param.updateOption(destination.dynOpts());
	}
	private void setToLocalDefault() {
		for (InterfaceOptions param : commonList)
			param.setFromDefault(false, true);
		writeLocalOptions(guiOptions());
		init(); // Validate Init
	}
	private void randomizeRace() {
		cr().randomizeRace(true);
		totalCostText.repaint(totalCostStr());
	}
	private void getRandomParam() {
		IGameOptions opts = guiOptions();
		cr().randomTargetMax.set(opts.randomAlienRacesTargetMax());
		cr().randomTargetMin.set(opts.randomAlienRacesTargetMin());
		cr().randomMax.set(opts.randomAlienRacesMax());
		cr().randomMin.set(opts.randomAlienRacesMin());
		cr().randomUseTarget.set(opts.randomAlienRacesUseTarget());
		cr().randomSmoothEdges.set(opts.randomAlienRacesSmoothEdges());
		repaint();
	}
	private void pushRandomParam() {
		IGameOptions opts = guiOptions();
		opts.randomAlienRacesTargetMax(cr().randomTargetMax.settingValue());
		opts.randomAlienRacesTargetMin(cr().randomTargetMin.settingValue());
		opts.randomAlienRacesMax(cr().randomMax.settingValue());
		opts.randomAlienRacesMin(cr().randomMin.settingValue());
		opts.randomAlienRacesUseTarget(cr().randomUseTarget.settingValue());
		opts.randomAlienRacesSmoothEdges(cr().randomSmoothEdges.settingValue());
	}
	private String loadButtonKey() {
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT:
			return loadCurrentKey;
		default:
			return saveCurrentKey;
		}
	}
	private int loadButtonWidth(Graphics2D g) {
		return Math.max(g.getFontMetrics().stringWidth(LabelManager.current().label(saveCurrentKey)),
						g.getFontMetrics().stringWidth(LabelManager.current().label(loadCurrentKey)))
				+ smallButtonMargin;
	}
	private void mouseCommon(MouseEvent e, MouseWheelEvent w) {
		if (hoverBox == speciesDirBox && e != null) {
			if (e.isControlDown())
				reloadRaceList(true);
			else {
				IMainOptions.speciesDirectory.toggle(e, w, this);
				reloadRaceList(false);
			}
			cr().loadRace();
			repaint();
			return;
		}
		for (int settingIdx=0; settingIdx < mouseList.size(); settingIdx++) {
			ICRSettings setting = mouseList.get(settingIdx);
			if (setting.isBullet()) {
				if (hoverBox == setting.settingText().box()) { // Check Setting
					setting.toggle(e, w, this);
					setting.guiSelect();
					if (showGuide()) {
						loadGuide();
						repaint();
					}
					else if (raceList.newValue())
						repaint();
					else
						totalCostText.repaint(totalCostStr());
					return;
				}
				else { // Check options
					int bulletStart	= setting.bulletStart();
					int bulletSize	= setting.bulletBoxSize();
					for (int bulletIdx=0; bulletIdx < bulletSize; bulletIdx++) {
						int optionIdx = bulletStart + bulletIdx;
						if (hoverBox == setting.optionText(bulletIdx).box()) {
							if (setting.toggle(e, w, optionIdx) || raceList.newValue()) {
								repaint();
							}
							else
								totalCostText.repaint(totalCostStr());
							return;
						}
					}
				}
			}
			else if (hoverBox == setting.settingText().box()) {
				setting.toggle(e, w, this);
				setting.settingText().repaint();
				totalCostText.repaint(totalCostStr());
				if (showGuide()) {
					loadGuide();
					repaint();
				}
				return;
			} 
		}
	}
	private String speciesDirButtonTxt()	{
		String dir = IMainOptions.speciesDirectory.guideValue();
		String jar = Rotp.jarPath();
		if (jar.length() <= dir.length()) {
			String sub = dir.substring(0, jar.length());
			boolean same = sub.equals(jar);
			if(same) {
				if (jar.length() == dir.length()) {
					String label = IMainOptions.speciesDirectory.getLangLabel() + "_DEFAULT";
					dir = langLabel(label);
				}
				else
					dir = dir.substring(jar.length()+1, dir.length());
			}
		}
		return dir;
	}
	private void drawSpeciesDirButton(Graphics2D g)	{
		// Button location and drawing
		int imageW	= s20;
		String text	= speciesDirButtonTxt();
		int sw		= g.getFontMetrics().stringWidth(text);
		int textW	= sw + miniButtonMargin;
		int buttonW	= imageW + textW;
		int xBox	= leftM + wGist() - columnPad - buttonW;
		int yBox	= yCost - speciesDirH;// - s10;
		speciesDirBox.setBounds(xBox, yBox, buttonW, miniButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(speciesDirBox.x, speciesDirBox.y, buttonW, miniButtonH, cnr, cnr);

		// FolderText location and drawing
		int xTxt = speciesDirBox.x + imageW + miniButtonMargin/2;
		int yTxt = speciesDirBox.y + speciesDirBox.height * 75/100;
		Color c = hoverBox == speciesDirBox ? Color.yellow : GameUI.borderBrightColor();
		g.setColor(c);
		g.drawString(text, xTxt, yTxt);

		// Icon location and Drawing
		xTxt -= imageW;
		g.setFont(fusionFont(13));
		drawBoldString(g,"📁", xTxt, yTxt);
	}
	// ========== Overriders ==========
	//
	@Override protected void drawFixButtons(Graphics2D g, boolean all) {
		Stroke prev;
		// left button
		if (hoverBox == randomBox || all) {
			g.setFont(smallButtonFont());
			String text = text(randomKey);
			int sw = g.getFontMetrics().stringWidth(text);
			int x = randomBox.x+((randomBox.width-sw)/2);
			int y = randomBox.y+randomBox.height*75/100;
			Color c = hoverBox == randomBox ? Color.yellow : GameUI.borderBrightColor();
			drawShadowedString(g, text, 2, x, y, GameUI.borderDarkColor(), c);
			prev = g.getStroke();
			g.setStroke(stroke1);
			g.drawRoundRect(randomBox.x, randomBox.y, randomBox.width, randomBox.height, cnr, cnr);
			g.setStroke(prev);
		}
		if (hoverBox == randomGetBox || all) {
			g.setFont(miniButtonFont());
			String text = text(randomGetKey);
			int sw = g.getFontMetrics().stringWidth(text);
			int x = randomGetBox.x+((randomGetBox.width-sw)/2);
			int y = randomGetBox.y+randomGetBox.height*75/100;
			Color c = hoverBox == randomGetBox ? Color.yellow : GameUI.borderBrightColor();
			drawShadowedString(g, text, 2, x, y, GameUI.borderDarkColor(), c);
			prev = g.getStroke();
			g.setStroke(stroke1);
			g.drawRoundRect(randomGetBox.x, randomGetBox.y, randomGetBox.width, randomGetBox.height, cnr, cnr);
			g.setStroke(prev);
		}
		if (hoverBox == randomPushBox || all) {
			g.setFont(miniButtonFont());
			String text = text(randomPushKey);
			int sw = g.getFontMetrics().stringWidth(text);
			int x = randomPushBox.x+((randomPushBox.width-sw)/2);
			int y = randomPushBox.y+randomPushBox.height*75/100;
			Color c = hoverBox == randomPushBox ? Color.yellow : GameUI.borderBrightColor();
			drawShadowedString(g, text, 2, x, y, GameUI.borderDarkColor(), c);
			prev = g.getStroke();
			g.setStroke(stroke1);
			g.drawRoundRect(randomPushBox.x, randomPushBox.y, randomPushBox.width, randomPushBox.height, cnr, cnr);
			g.setStroke(prev);
		}
	}
	@Override protected void initButtonsBounds(Graphics2D g) {
		g.setFont(smallButtonFont());

		// Exit Button
		int buttonW	= exitButtonWidth(g);
		xButton = leftM + wGist() - buttonW - buttonPad;
		exitBox.setBounds(xButton, yButton+s2, buttonW, smallButtonH);

		// Select Button
		String text	 = text(selectKey);
		int sw = g.getFontMetrics().stringWidth(text);
		buttonW	 = sw + smallButtonMargin;
		xButton -= (buttonW + buttonPad);
		selectBox.setBounds(xButton, yButton, buttonW, smallButtonH);

		// Default Button
		buttonW	 = defaultButtonWidth(g);
		xButton -= (buttonW + buttonPad);
		defaultBox.setBounds(xButton, yButton, buttonW, smallButtonH);

		// Last Button
		buttonW  = lastButtonWidth(g);
		xButton -= (buttonW + buttonPad);
		lastBox.setBounds(xButton, yButton, buttonW, smallButtonH);

		// User Button
		buttonW	 = userButtonWidth(g);
		xButton -= (buttonW + buttonPad);
		userBox.setBounds(xButton, yButton, buttonW, smallButtonH);

		// Load / Save Button
		buttonW	 = loadButtonWidth(g);
		xButton -= (buttonW + buttonPad);
		loadBox.setBounds(xButton, yButton, buttonW, smallButtonH);

		// Guide Button
		buttonW = g.getFontMetrics().stringWidth(text) + smallButtonMargin;
		xButton	= leftM + buttonPad;
		guideBox.setBounds(xButton, yButton, buttonW, smallButtonH);

		// Randomize Button
		text	= text(randomKey);
		xButton = leftM + buttonPad;
		yRandB  = yDesc - buttonPadV - smallButtonH;
		buttonW = g.getFontMetrics().stringWidth(text) + smallButtonMargin;
		randomBox.setBounds(xButton, yRandB, buttonW, smallButtonH);

		// Randomize Get Button
		text	  = text(randomGetKey);
		xButton	  = leftM + buttonPad;
		yRandGetB = yRandB - miniButtonH - 1*buttonPadV - 6*settingH - 0*settingHPad;
		buttonW	  = g.getFontMetrics().stringWidth(text) + miniButtonMargin;
		randomGetBox.setBounds(xButton, yRandGetB, buttonW, miniButtonH);

		// Randomize Push Button
		text	   = text(randomGetKey);
		xRandPushB = xButton + buttonW + buttonPad;
		buttonW	   = g.getFontMetrics().stringWidth(text) + miniButtonMargin;
		randomPushBox.setBounds(xRandPushB, yRandGetB, buttonW, miniButtonH);

		// Species Directory Button
		int xSD = leftM + wGist() - columnPad - buttonW;
		int ySD	= yCost - speciesDirH - s10;
		speciesDirBox.setBounds(xSD, ySD, buttonW, smallButtonH);
	}
	@Override protected void initFixButtons(Graphics2D g) {
		// System.out.println("EDIT: initFixButtons(Graphics2D g) " + (randomBox.y-yButton));
		// Randomize Button
		Stroke prev = g.getStroke();
		setSmallButtonGraphics(g);
		g.fillRoundRect(randomBox.x, randomBox.y, randomBox.width, randomBox.height, cnr, cnr);
		g.fillRoundRect(randomGetBox.x, randomGetBox.y, randomGetBox.width, randomGetBox.height, cnr, cnr);
		g.fillRoundRect(randomPushBox.x, randomPushBox.y, randomPushBox.width, randomPushBox.height, cnr, cnr);
		drawFixButtons(g, true);
		g.setStroke(prev);
	}
	@Override protected void drawButtons(Graphics2D g, boolean init) {
        Stroke prev = g.getStroke();
        if (init)
            g.setFont(bigButtonFont(retina));
        else
        	g.setFont(bigButtonFont(false));
        drawButton(g, init, exitBox,	text(exitButtonKey()));

        //drawSpeciesDirButton(g);

        if (init)
        	g.setFont(smallButtonFont(retina));
        else
        	g.setFont(smallButtonFont());
        drawButton(g, init, defaultBox,	text(defaultButtonKey()));
        drawButton(g, init, lastBox,	text(lastButtonKey()));
        drawButton(g, init, userBox,	text(userButtonKey()));
        drawButton(g, init, loadBox,	text(loadButtonKey()));
        drawButton(g, init, selectBox,	text(selectKey));
        drawButton(g, init, guideBox,	text(guideButtonKey()));
        g.setStroke(prev);
	}
    @Override public BufferedImage initButtonBackImg() {
    	initButtonPosition();
		buttonBackImg = new BufferedImage(retina(wButton), retina(hButton), TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) buttonBackImg.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		setBigButtonGraphics(g);
		// draw EXIT button
		exitBox.fillButtonBackImg(g);

		setSmallButtonGraphics(g);
		// Select Button
		selectBox.fillButtonBackImg(g);
		// Default Button
		defaultBox.fillButtonBackImg(g);
		// Last Button
		lastBox.fillButtonBackImg(g);
		// User Button
		userBox.fillButtonBackImg(g);
		// Load / Save Button
		loadBox.fillButtonBackImg(g);
		// draw GUIDE button
		guideBox.fillButtonBackImg(g);
		
		drawButtons(g, true); // init = true; local = true
		return buttonBackImg;
    }

	@Override public void refreshGui(int level) {
		if (level == 0)
			cr().setSettingTools((DynOptions) guiOptions().selectedPlayerCustomRace());
		repaint();
	}
	@Override protected void doDefaultBoxAction() {
		buttonClick();
		switch (ModifierKeysState.get()) {
		case CTRL: // restoreGlobalKey
			guiOptions().updateAllNonCfgFromFile(LIVE_OPTIONS_FILE);		
			break;
		case CTRL_SHIFT: // restoreLocalKey
			guiOptions().updateFromFile(LIVE_OPTIONS_FILE, localOptions());		
			break;
		case SHIFT: // setLocalDefaultKey
			setToLocalDefault();
			break; 
		default: // setGlobalDefaultKey
			guiOptions().resetAllNonCfgSettingsToDefault();
			setToLocalDefault();
			break; 
		}
		refreshGui(0);
	}
	@Override public void open(BasePanel p) {
		enableGlassPane(this);
		ModifierKeysState.reset();
		parent = p;

		cr().setSettingTools((DynOptions) guiOptions().selectedPlayerCustomRace());
		guiOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
		init();
		reloadRaceList(false);
		repaint();
	}
	@Override protected String GUI_ID() { return GUI_ID; }
	@Override protected void close() {
		ModifierKeysState.reset();
		super.close();
		((SetupRaceUI) parent).raceChanged();		
		RotPUI.instance().selectSetupRacePanel();
	}
	@Override protected int getBackGroundWidth() {
		return super.getBackGroundWidth() + raceListW + columnPad;
	}
	@Override protected String raceAIButtonTxt() { return ""; }
	@Override protected void paintButtons(Graphics2D g) {
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

		// Select Button
		text	 = text(selectKey);
		sw = g.getFontMetrics().stringWidth(text);
		buttonW	 = sw + smallButtonMargin;
		xButton -= (buttonW + buttonPad);
		selectBox.setBounds(xButton, yButton, buttonW, smallButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(selectBox.x, selectBox.y, buttonW, smallButtonH, cnr, cnr);
		xT = selectBox.x + ((selectBox.width-sw)/2);
		yT = selectBox.y + selectBox.height-s8;
		cB = hoverBox == selectBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(selectBox.x, selectBox.y, selectBox.width, selectBox.height, cnr, cnr);
		g.setStroke(prev);

		// Default Button
		text	 = text(defaultButtonKey());
		sw		 = g.getFontMetrics().stringWidth(text);
		buttonW	 = defaultButtonWidth(g);
		xButton -= (buttonW + buttonPad);
		defaultBox.setBounds(xButton, yButton, buttonW, smallButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(defaultBox.x, defaultBox.y, buttonW, smallButtonH, cnr, cnr);
		xT = defaultBox.x + ((defaultBox.width-sw)/2);
		yT = defaultBox.y + defaultBox.height-s8;
		cB = hoverBox == defaultBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(defaultBox.x, defaultBox.y, defaultBox.width, defaultBox.height, cnr, cnr);
		g.setStroke(prev);

		// Last Button
		text	 = text(lastButtonKey());
		sw		 = g.getFontMetrics().stringWidth(text);
		buttonW  = lastButtonWidth(g);
		xButton -= (buttonW + buttonPad);
		lastBox.setBounds(xButton, yButton, buttonW, smallButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(lastBox.x, lastBox.y, buttonW, smallButtonH, cnr, cnr);
		xT = lastBox.x + ((lastBox.width-sw)/2);
		yT = lastBox.y + lastBox.height-s8;
		cB = hoverBox == lastBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(lastBox.x, lastBox.y, lastBox.width, lastBox.height, cnr, cnr);
		g.setStroke(prev);

		// User preference Button
		text	 = text(userButtonKey());
		sw		 = g.getFontMetrics().stringWidth(text);
		buttonW	 = userButtonWidth(g);
		xButton -= (buttonW + buttonPad);
		userBox.setBounds(xButton, yButton, buttonW, smallButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(userBox.x, userBox.y, buttonW, smallButtonH, cnr, cnr);
		xT = userBox.x + ((userBox.width-sw)/2);
		yT = userBox.y + userBox.height-s8;
		cB = hoverBox == userBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(userBox.x, userBox.y, userBox.width, userBox.height, cnr, cnr);
		g.setStroke(prev);

		// Load / Save Button
		text	 = text(loadButtonKey());
		sw		 = g.getFontMetrics().stringWidth(text);
		buttonW	 = loadButtonWidth(g);
		xButton -= (buttonW + buttonPad);
		loadBox.setBounds(xButton, yButton, buttonW, smallButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(loadBox.x, loadBox.y, buttonW, smallButtonH, cnr, cnr);
		xT = loadBox.x + ((loadBox.width-sw)/2);
		yT = loadBox.y + loadBox.height-s8;
		cB = hoverBox == loadBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(loadBox.x, loadBox.y, loadBox.width, loadBox.height, cnr, cnr);
		g.setStroke(prev);

		// Guide Button
		text	= text(guideButtonKey());
		sw		= g.getFontMetrics().stringWidth(text);
		buttonW = g.getFontMetrics().stringWidth(text) + smallButtonMargin;
		xButton	= leftM + buttonPad;
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

		// Randomize Button
		text	= text(randomKey);
		xButton = leftM + buttonPad;
		yRandB  = yDesc - buttonPadV - smallButtonH;
		sw		= g.getFontMetrics().stringWidth(text);
		buttonW = g.getFontMetrics().stringWidth(text) + smallButtonMargin;
		randomBox.setBounds(xButton, yRandB, buttonW, smallButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(randomBox.x, randomBox.y, buttonW, smallButtonH, cnr, cnr);
		xT = randomBox.x+((randomBox.width-sw)/2);
		yT = randomBox.y+randomBox.height-s8;
		cB = hoverBox == randomBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(randomBox.x, randomBox.y, randomBox.width, randomBox.height, cnr, cnr);
		g.setStroke(prev);

		g.setFont(miniButtonFont());
		// Random Get Button
		text	= text(randomGetKey);
		xButton = leftM + buttonPad;
		yRandGetB = yRandB - miniButtonH - 1*buttonPadV - 6*settingH - 0*settingHPad;
		sw		= g.getFontMetrics().stringWidth(text);
		buttonW = g.getFontMetrics().stringWidth(text) + miniButtonMargin;
		randomGetBox.setBounds(xButton, yRandGetB, buttonW, smallButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(randomGetBox.x, randomGetBox.y, buttonW, miniButtonH, cnr, cnr);
		xT = randomGetBox.x+((randomGetBox.width-sw)/2);
		yT = randomGetBox.y+randomGetBox.height-s8;
		cB = hoverBox == randomGetBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(randomGetBox.x, randomGetBox.y, randomGetBox.width, randomGetBox.height, cnr, cnr);
		g.setStroke(prev);

		// Random Push Button
		text	= text(randomPushKey);
		xButton = leftM + buttonPad;
		xRandPushB = xButton + buttonW + buttonPad;
		sw		= g.getFontMetrics().stringWidth(text);
		buttonW = g.getFontMetrics().stringWidth(text) + miniButtonMargin;
		randomPushBox.setBounds(xRandPushB, yRandGetB, buttonW, miniButtonH);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRoundRect(randomPushBox.x, randomPushBox.y, buttonW, smallButtonH, cnr, cnr);
		xT = randomPushBox.x+((randomPushBox.width-sw)/2);
		yT = randomPushBox.y+randomPushBox.height-s8;
		cB = hoverBox == randomPushBox ? Color.yellow : GameUI.borderBrightColor();
		drawShadowedString(g, text, 2, xT, yT, GameUI.borderDarkColor(), cB);
		prev = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(randomPushBox.x, randomPushBox.y, randomPushBox.width, randomPushBox.height, cnr, cnr);
		g.setStroke(prev);
	}
	@Override public void paintComponent(Graphics g0) {
		// showTiming = true;
		if (showTiming)
			System.out.println("===== EditCustomRace PaintComponents =====");
		long timeStart = System.currentTimeMillis();
		super.paintComponent(g0); // call ShowUI
		Graphics2D g = (Graphics2D) g0;
		// Custom Race List
		currentWidth = raceListW;
		Stroke prev = g.getStroke();
		g.setStroke(stroke2);
		paintSetting(g, raceList);
		g.setStroke(prev);
	
		// Randomize Options
		xLine = xDesc  + labelPad;
		yLine = yRandB - labelPad;
		ModText bt;
		for(ICRSettings setting : guiList) {
			bt = setting.settingText();
			bt.displayText(setting.guiSettingDisplayStr());
			bt.setScaledXY(xLine, yLine);
			bt.draw(g);
			yLine -= labelH;
		}
		drawSpeciesDirButton(g);
		//drawButtons(g);
		if (showTiming)
			System.out.println("EditCustomRace paintComponent() Time = " + (System.currentTimeMillis()-timeStart));	
	}
	@Override public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				doExitBoxAction();
				return;
		}
		super.keyPressed(e);
	}
	@Override public void mouseReleased(MouseEvent e) {
		if (e.getButton() > 3)
			return;
		if (hoverBox == null)
			return;
		if (hoverBox == exitBox) {
			doExitBoxAction();
			return;
		}
		if (hoverBox == selectBox) {
			doSelectBoxAction();
			return;
		}
		if (hoverBox == guideBox) {
			doGuideBoxAction();
			return;
		}
		if (hoverBox == userBox) {
			doUserBoxAction();
			return;
		}
		if (hoverBox == lastBox) {
			doLastBoxAction();
			return;
		}
		if (hoverBox == defaultBox) {
			doDefaultBoxAction();			
			return;
		}
		if (hoverBox == loadBox) {
			doLoadBoxAction();			
			return;
		}
		if (hoverBox == randomBox) {
			randomizeRace();			
			return;
		}
		if (hoverBox == randomGetBox) {
			getRandomParam();			
			return;
		}
		if (hoverBox == randomPushBox) {
			pushRandomParam();			
			return;
		}
		mouseCommon(e, null);
	}
	@Override public void mouseWheelMoved(MouseWheelEvent e) {
		mouseCommon(null, e);
	}
}
