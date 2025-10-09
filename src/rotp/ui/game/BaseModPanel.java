/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	   https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.ui.game;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static rotp.model.game.DefaultValues.FUSION_DEFAULT;
import static rotp.model.game.DefaultValues.MOO1_DEFAULT;
import static rotp.model.game.DefaultValues.ROTP_DEFAULT;
import static rotp.model.game.IBaseOptsTools.GAME_OPTIONS_FILE;
import static rotp.model.game.IBaseOptsTools.LAST_OPTIONS_FILE;
import static rotp.model.game.IBaseOptsTools.LIVE_OPTIONS_FILE;
import static rotp.model.game.IBaseOptsTools.USER_OPTIONS_FILE;
import static rotp.model.game.IMainOptions.showGuide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;

import rotp.Rotp;
import rotp.model.game.IMainOptions;
import rotp.model.game.SafeListParam;
import rotp.ui.BasePanel;
import rotp.ui.BaseText;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.util.IParam;
import rotp.ui.util.ParamButtonHelp;
import rotp.ui.util.ParamDefaultButtonHelp;
import rotp.util.LabelManager;
import rotp.util.ModifierKeysState;

public abstract class BaseModPanel extends BasePanel
		implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static final String setGlobalDefaultKey	= "SETTINGS_GLOBAL_DEFAULT";
	private static final String setLocalDefaultKey	= "SETTINGS_LOCAL_DEFAULT";
	private static final String setGlobalGameKey	= "SETTINGS_GLOBAL_LAST_GAME";
	private static final String setLocalGameKey		= "SETTINGS_LOCAL_LAST_GAME";
	private static final String setGlobalLastKey	= "SETTINGS_GLOBAL_LAST_SET";
	private static final String setLocalLastKey		= "SETTINGS_LOCAL_LAST_SET";
	private static final String setGlobalUserKey	= "SETTINGS_GLOBAL_USER_SET";
	private static final String setLocalUserKey		= "SETTINGS_LOCAL_USER_SET";
	private static final String saveGlobalUserKey	= "SETTINGS_GLOBAL_USER_SAVE";
	private static final String saveLocalUserKey	= "SETTINGS_LOCAL_USER_SAVE";
	private static final String restoreGlobalKey	= "SETTINGS_GLOBAL_RESTORE";
	private static final String restoreLocalKey		= "SETTINGS_LOCAL_RESTORE";
	private static final String guideKey			= "SETTINGS_GUIDE";
	protected static final String exitKey		 	= "SETTINGS_EXIT";
	protected static final String cancelKey		 	= "SETTINGS_CANCEL";
	protected static final String applyKey		 	= "SETTINGS_APPLY";
	
	private	  static int	 exitButtonWidth, guideButtonWidth,
							 userButtonWidth, defaultButtonWidth, lastButtonWidth;
	protected static int	 mX, mY;
	protected static int	 smallButtonMargin, smallButtonH;
	protected static int	 miniButtonMargin, miniButtonH;
	protected static int	 cnr;

	private	static int	guideFontSize;
	public	static int	guideFontSize()			{ return guideFontSize; }
	public	static void	guideFontSize(int size)	{ guideFontSize = size; }
	public	static void	showGuide(boolean b)	{ showGuide.set(b); }
	public	static boolean showGuide() 			{ return showGuide.get(); }
	public	static boolean dialGuide	= false; // To disable automated Guide on dialog list
	private	static boolean contextHlp	= false; // The time to show  the contextual help

	private	  final LinkedList<PolyBox>	polyBoxList	= new LinkedList<>();
	protected final LinkedList<Box>		boxBaseList	= new LinkedList<>();
	protected final LinkedList<Box>		boxHelpList	= new LinkedList<>();
	protected Box	  hoverBox;
	protected Box	  prevHover;
	protected PolyBox hoverPolyBox;
	private	  PolyBox prevPolyBox;
	protected boolean hoverChanged;
	protected boolean isSubMenu = true; // Not (Race or Galaxy)
	protected boolean isOnTop	= true;
	protected boolean retina	= false;
	protected boolean hovering	= false;
	protected int retinaFactor	= 1;
	private   int baseRF		= 2;
	protected int cnrR			= cnr;

	// Debug Parameter
	protected boolean showTiming = false;

	private   boolean loadingOptions = false;
	private	  boolean initialised = false;
	SafeListParam paramList;
	SafeListParam duplicateList;
	SafeListParam activeList;

	protected int xButton, yButton, wButton, hButton; // absolute button position.
	protected int xFull, yFull, wFull, hFull, rFull, bFull; // absolute panel window size and position (right, bottom)
	protected int xGist, yGist, hGist, rGist, bGist; // relative Content size and position (right, bottom)
	private int wGist; // relative Content size and position (right, bottom)
	protected BufferedImage buttonBackImg;
	private	  LinearGradientPaint bg;
	protected LinearGradientPaint bg() {
		if (bg == null)
			bg = GameUI.modBackground(xGist, rGist);
		return bg;
	}
	protected BufferedImage backImg; // the full background
	protected void initBackImg()		{  }
	protected boolean loadingOptions()	{ return loadingOptions; }
	protected BufferedImage backImg()	{
        if (backImg == null) {
     		retina		 = UserPreferences.graphicRetina();
     		retinaFactor = retina ? baseRF : 1;
     		cnrR		 = retina(cnr);
        	initBackImg();
        }
        return backImg;
    }
	protected int wGist()				{ return wGist; }
	protected void wGist(int w)			{ wGist = w; }
//	protected int retina(int val)		{ return (int) (val*retinaFactor); }
//	protected int invRetina(int val)	{ return (int) (val/retinaFactor); }
	protected int retina(int val)		{ return val*retinaFactor; }
	protected int invRetina(int val)	{ return val/retinaFactor; }

	protected void singleInit() {} // To avoid call to options during class creation
	public SafeListParam activeList() { return activeList; }
	
	public final GuidePopUp guidePopUp;
	
	private Font miniButtonFont		= narrowFont(15);
	private Font smallButtonFont	= narrowFont(20);
	private Font smallButtonFontR	= narrowFont((int)(20*baseRF));
	protected Font smallButtonFont(boolean retina)	{
		if (retina)
			return smallButtonFontR;
		else
			return smallButtonFont;
	}
	protected Font bigButtonFont(boolean retina)	{
		if (retina)
			return smallButtonFontR;
		else
			return smallButtonFont;
	}
	protected Font bigButtonFont()				{ return smallButtonFont; }
	protected Font smallButtonFont()			{ return smallButtonFont; }
	protected Font miniButtonFont()				{ return miniButtonFont; }
	protected Box newExitBox()					{ return new Box(exitButton); }

	protected Box exitBox		= newExitBox();
	protected Box defaultBox	= new Box(defaultButton);
	protected Box lastBox		= new Box(lastButton);
	protected Box userBox		= new Box(userButton);
	protected Box guideBox		= new Box(guideKey);

	protected boolean globalOptions	= false; // No preferred button and Saved to remnant.cfg

	protected BaseModPanel ()	{
		guidePopUp = new GuidePopUp();
		guidePopUp.init();
	}
	protected void reInit(boolean hover)		{
		boxBaseList.clear();
		exitBox		= newExitBox();
		defaultBox	= new Box(defaultButton);
		userBox		= new Box(userButton);
		guideBox	= new Box(guideKey);
		paramList	= null;
		duplicateList	= null;
		activeList	= null;
		isSubMenu	= true;
		isOnTop		= true;
		retina		= false;
		hovering	= hover;
		cnrR		= cnr;
		initialised	= false;
		backImg		= null;
		buttonBackImg	= null;
		bg	= null;
	}
	protected void terminate()	{ reInit(false); }
	protected abstract String GUI_ID();
	protected void forceUpdate(boolean b)	{}
	protected SafeListParam localOptions()	{ return activeList; };
	private void localInit(Graphics2D g)	{
		Font prevFont = g.getFont();
		
		g.setFont(bigButtonFont());
		initExitButtonWidth(g);
		
		g.setFont(smallButtonFont());
		initGuideButtonWidth(g);
		initUserButtonWidth(g);
		initDefaultButtonWidth(g);
		initLastButtonWidth(g);

		g.setFont(prevFont);
		guidePopUp.init();
	}
	private int stringWidth(Graphics2D g, String key) {
		return g.getFontMetrics().stringWidth(LabelManager.current().label(key));
	}
	private int buttonWidth(Graphics2D g, String[] keys) {
		int result = 0;
		for (String key : keys)
			result = max(result, stringWidth(g, key));
		return smallButtonMargin + result;
	}
	
	public void refreshGui(int level) {}

	protected void init() {
		//ModifierKeysState.reset();
		if (!hovering) {
			xFull = 0;
			yFull = 0;
			wFull = RotPUI.setupRaceUI().getWidth();
			hFull = RotPUI.setupRaceUI().getHeight();
			rFull = xFull + wFull;
			bFull = yFull + hFull;
			xGist = 0;
			yGist = 0;
			wGist(wFull);
			hGist = hFull;
			rGist = xGist + wGist();
			bGist = yGist + hGist;
		}
		smallButtonMargin = s30;
		smallButtonH	  = s30;
		miniButtonMargin  = s22;
		miniButtonH		  = s22;
		cnr				  = s5;
		if (!initialised) {
			initBoxes();
		}
		isOnTop = true;
	}
	private void initBoxes() {
		if (isSubMenu) {
			defaultBox	= new Box(defaultSubButton);
			lastBox		= new Box(lastSubButton);
			userBox		= new Box(userSubButton);
		}
		else {
			defaultBox	= new Box(defaultButton);
			lastBox		= new Box(lastButton);
			userBox		= new Box(userButton);
		}
		guideBox	= new Box(guideKey);
		singleInit();
		initialised = true;
	}
	public void clearImages() {
		buttonBackImg = null;
		backImg = null;
		bg = null;
	}
	protected void close() { 
		disableGlassPane();
		clearImages();
		isOnTop = false;
		//ModifierKeysState.reset();
	}
	private BufferedImage buttonBackImg() {
        if (buttonBackImg == null)
            initButtonBackImg();
        return buttonBackImg;
    }
	@Override public void repaintButtons() {
		initButtonBackImg();
		Graphics2D g = (Graphics2D) getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		drawButtons(g);
		g.dispose();
	}
	protected void drawButtons(Graphics2D g) {
		if (retina) {
			BufferedImage img = buttonBackImg();
			int w = img.getWidth();
			int h = img.getHeight();
			g.drawImage(img, xButton, yButton, xButton+invRetina(w), yButton+invRetina(h), 0, 0, w, h, null);
		}
		else
			g.drawImage(buttonBackImg(), xButton, yButton, null);
		drawButtons(g, false); // init = false; local = false
	}
    protected void drawButton(Graphics2D g, boolean init, Box box, String str) {
    	if (init) {
	        int sw	 = g.getFontMetrics().stringWidth(str);
	        int x    = retina(box.x) + ((retina(box.width)-sw)/2);
	        int y    = retina(box.y) + retina(box.height)*75/100;
        	x -= retina(xButton);
        	y -= retina(yButton);
	        Color c1 = GameUI.borderBrightColor();
	        drawShadowedString(g, str, 2, x, y, GameUI.borderDarkColor(), c1);
	        if (retina) {
	        	g.setStroke(stroke2);
	        	g.drawRoundRect(retina(box.x), retina(box.y), retina(box.width), retina(box.height), cnrR, cnrR);    		
	        }
	        else {
	        	g.setStroke(stroke1);
		        g.drawRoundRect(box.x, box.y, box.width, box.height, cnr, cnr);    		
	        }
    	}
    	else if (hoverBox == box) {
	        int sw	 = g.getFontMetrics().stringWidth(str);
	        int x    = box.x + ((box.width-sw)/2);
	        int y    = box.y + box.height*75/100;
	        if (init) {
	        	x -= xButton;
	        	y -= yButton;
	        }
	        Color c1 = init? GameUI.borderBrightColor() : Color.yellow;
	        drawShadowedString(g, str, 2, x, y, GameUI.borderDarkColor(), c1);
	        g.setStroke(stroke1);
	        g.drawRoundRect(box.x, box.y, box.width, box.height, cnr, cnr);
        }
    }
	protected void drawButtons(Graphics2D g, boolean init) {
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
        drawButton(g, init, defaultBox,	text(defaultButtonKey()));
        drawButton(g, init, lastBox,	text(lastButtonKey()));
        drawButton(g, init, userBox,	text(userButtonKey()));
        drawButton(g, init, guideBox,	text(guideButtonKey()));
        g.setStroke(prev);
	}
    protected void initButtonPosition() {
		int xMin = guideBox.x;		
		int yMin = exitBox.y;
		if (hovering)
			yMin = min(yMin, defaultBox.y);
		int xMax = exitBox.x + exitBox.width;
		int yMax = exitBox.y + exitBox.height;
		xButton = xMin-retina(s2);
		yButton = yMin-retina(s2);
		wButton = xMax - xMin + retina(s4);
		hButton = yMax - yMin + retina(s4);
    }
	protected void setBigButtonGraphics(Graphics2D g)	{
		g.setFont(bigButtonFont(retina));
		g.setPaint(GameUI.buttonBackgroundColor());
	}
	protected void setSmallButtonGraphics(Graphics2D g) {
		g.setFont(smallButtonFont(retina));
		g.setPaint(GameUI.buttonBackgroundColor());
	}
	public BufferedImage initButtonBackImg() {
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
		// draw DEFAULT button
		defaultBox.fillButtonBackImg(g);
		// draw LAST button
		lastBox.fillButtonBackImg(g);
		// draw USER button
		userBox.fillButtonBackImg(g);
		// draw GUIDE button
		guideBox.fillButtonBackImg(g);

		drawButtons(g, true); // init = true; local = true
		return buttonBackImg;
	}

	// ==================== Guide Button ====================
	//
	protected String guideButtonKey() { return guideKey; }
	private void initGuideButtonWidth(Graphics2D g) {
		guideButtonWidth = buttonWidth(g, new String[] {guideKey});
	}
	protected int guideButtonWidth(Graphics2D g) {
		if (guideButtonWidth == 0) 
			localInit(g);
		return guideButtonWidth;
	}
	protected void doGuideBoxAction() {
		buttonClick();
		showGuide.toggle();
		dialGuide &= showGuide.get();
		if (showGuide.get())
			loadGuide();
		else
			clearGuide();
		paintComponent(getGraphics());
	}	

	// ==================== Exit Button ====================
	//
	protected ParamButtonHelp newExitButton() {
		return new ParamButtonHelp(
				"SETTINGS_BUTTON_EXIT", // For Help Do not add the list
				exitKey,	true,
				exitKey,	false,
				cancelKey,	true,
				cancelKey,	false);
	}
	private final ParamButtonHelp exitButton = newExitButton();
	private final ParamButtonHelp exitSubButton = new ParamButtonHelp(
			"SETTINGS_BUTTON_EXIT", // For Help Do not add the list
			exitKey,	true,
			applyKey,	true,
			cancelKey,	true,
			cancelKey,	false);
	protected String exitButtonKey() {
		if (isSubMenu)
			return exitSubButton.getKey();
		else
			return exitButton.getKey();
	}
	private void initExitButtonWidth(Graphics2D g) { // Same size for panels and sub panels
		exitButtonWidth = buttonWidth(g, exitButton.getKeys());
	}
	protected int exitButtonWidth(Graphics2D g) {
		if (exitButtonWidth == 0)
			localInit(g);
		return exitButtonWidth;
	}
	protected void doExitBoxAction() {
		buttonClick();
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT: // Restore
			guiOptions().updateAllNonCfgFromFile(LIVE_OPTIONS_FILE);
			break;
		default: // Save
			guiOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
			break; 
		}
		close();
	}

	// ==================== User Button ====================
	//
	private static final ParamButtonHelp userButton = new ParamButtonHelp(
			"SETTINGS_BUTTON_USER", // For Help Do not add the list
			setGlobalUserKey,
			setLocalUserKey,
			saveGlobalUserKey,
			saveLocalUserKey);
	private static final ParamButtonHelp userSubButton = new ParamButtonHelp(
			"SETTINGS_BUTTON_USER", // For Help Do not add the list
			setLocalUserKey,	true,
			setLocalUserKey,	false,
			saveLocalUserKey,	true,
			saveLocalUserKey,	false);
	protected String userButtonKey() {
		if (isSubMenu)
			return userSubButton.getKey();
		else
			return userButton.getKey();
	}
	private void initUserButtonWidth(Graphics2D g) { // Same size for panels and sub panels
		userButtonWidth = buttonWidth(g, userButton.getKeys());
	}
	protected int userButtonWidth(Graphics2D g) {
		if (userButtonWidth == 0) 
			localInit(g);
		return userButtonWidth;
	}
	protected void doUserBoxAction() {
		buttonClick();
		if (isSubMenu)
			switch (ModifierKeysState.get()) {
			case CTRL:
			case CTRL_SHIFT: // saveLocalUserKey
				guiOptions().saveOptionsToFile(USER_OPTIONS_FILE, localOptions());
				return;
			case SHIFT: // setLocalUserKey
			default:
				loadingOptions = true;
				guiOptions().updateFromFile(USER_OPTIONS_FILE, localOptions());
				loadingOptions = false;
				refreshGui(0);
				return;
			}
		else
			switch (ModifierKeysState.get()) {
			case CTRL: // saveGlobalUserKey
				guiOptions().saveOptionsToFile(USER_OPTIONS_FILE);
				return;
			case CTRL_SHIFT: // saveLocalUserKey
				guiOptions().saveOptionsToFile(USER_OPTIONS_FILE, localOptions());
				return;
			case SHIFT: // setLocalUserKey
				loadingOptions = true;
				guiOptions().updateFromFile(USER_OPTIONS_FILE, localOptions());
				loadingOptions = false;
				refreshGui(0);
				return;
			default: // setGlobalUserKey
				loadingOptions = true;
				if (globalOptions) {
					guiOptions().updateFromFile(USER_OPTIONS_FILE, localOptions());
				}
				else
					guiOptions().updateAllNonCfgFromFile(USER_OPTIONS_FILE);
				loadingOptions = false;
				refreshGui(0);
				return;
			}
	}	

	// ==================== Default Button ====================
	//
	private static final ParamButtonHelp defaultButton = new ParamDefaultButtonHelp(
			"SETTINGS_BUTTON_DEFAULT", // For Help Do not add the list
			setGlobalDefaultKey,
			setLocalDefaultKey,
			restoreGlobalKey,
			restoreLocalKey);
	private static final ParamButtonHelp defaultSubButton = new ParamDefaultButtonHelp(
			"SETTINGS_BUTTON_DEFAULT", // For Help Do not add the list
			setLocalDefaultKey,	true,
			setLocalDefaultKey,	false,
			restoreLocalKey,	true,
			restoreLocalKey,	false);
	protected String defaultButtonKey() {
		if (globalOptions)  // The old ways
			switch (ModifierKeysState.get()) {
			case CTRL:
			case CTRL_SHIFT: return restoreLocalKey;
			default:		 return setLocalDefaultKey;
			}
		else if (isSubMenu)
			return defaultSubButton.getKey();
		else
			return defaultButton.getKey();
	}
	private void initDefaultButtonWidth(Graphics2D g) { // Same size for panels and sub panels
		defaultButtonWidth = buttonWidth(g, defaultButton.getKeys());
	}
	protected int defaultButtonWidth(Graphics2D g) {
		if (defaultButtonWidth == 0) 
			localInit(g);
		return defaultButtonWidth;
	}
	protected void doDefaultBoxAction() {
		buttonClick();
		if (isSubMenu)
			switch (ModifierKeysState.get()) {
			case CTRL: // restoreGlobalKey
			case CTRL_SHIFT: // restoreLocalKey
				loadingOptions = true;
				guiOptions().updateFromFile(LIVE_OPTIONS_FILE, localOptions());
				loadingOptions = false;
				refreshGui(0);
				return;
			case SHIFT:
			default: // setLocalDefaultKey
				loadingOptions = true;
				guiOptions().resetPanelSettingsToDefault(localOptions(), !globalOptions, isSubMenu);		
				loadingOptions = false;
				refreshGui(0);
				return;
			}
		else
			switch (ModifierKeysState.get()) {
			case CTRL: // restoreGlobalKey
				loadingOptions = true;
				guiOptions().updateAllNonCfgFromFile(LIVE_OPTIONS_FILE);		
				loadingOptions = false;
				refreshGui(0);
				return;
			case CTRL_SHIFT: // restoreLocalKey
				loadingOptions = true;
				guiOptions().updateFromFile(LIVE_OPTIONS_FILE, localOptions());		
				loadingOptions = false;
				refreshGui(0);
				return;
			case SHIFT: // setLocalDefaultKey
				loadingOptions = true;
				guiOptions().resetPanelSettingsToDefault(localOptions(), !globalOptions, isSubMenu);		
				loadingOptions = false;
				refreshGui(0);
				return;
			default: // setGlobalDefaultKey
				loadingOptions = true;
				guiOptions().resetAllNonCfgSettingsToDefault();		
				loadingOptions = false;
				refreshGui(0);
				return;
			}
	}

	// ==================== Last Button ====================
	//
	private static final ParamButtonHelp lastButton = new ParamButtonHelp(
			"SETTINGS_BUTTON_LAST", // For Help Do not add the list
			setGlobalGameKey,
			setLocalGameKey,
			setGlobalLastKey,
			setLocalLastKey);
	private static final ParamButtonHelp lastSubButton = new ParamButtonHelp(
			"SETTINGS_BUTTON_LAST", // For Help Do not add the list
			setLocalGameKey,	true,
			setLocalGameKey,	false,
			setLocalLastKey,	true,
			setLocalLastKey,	false);
	protected String lastButtonKey() {
		 if (isSubMenu)
				return lastSubButton.getKey();
			else
				return lastButton.getKey();
	}
	private void initLastButtonWidth(Graphics2D g) { // Same size for panels and sub panels
		lastButtonWidth = buttonWidth(g, lastButton.getKeys());
	}
	protected int lastButtonWidth(Graphics2D g) {
		if (lastButtonWidth == 0) 
			localInit(g);
		return lastButtonWidth;
	}
	protected void doLastBoxAction() {
		buttonClick();
		 if (isSubMenu)
			 switch (ModifierKeysState.get()) {
				case CTRL:
				case CTRL_SHIFT: // setLocalGameKey
					loadingOptions = true;
					guiOptions().updateFromFile(LAST_OPTIONS_FILE, localOptions());
					loadingOptions = false;
					break;
				case SHIFT:
				default: // setLocalLastKey
					loadingOptions = true;
					guiOptions().updateFromFile(GAME_OPTIONS_FILE, localOptions());
					loadingOptions = false;
					break;
				}
		else
			switch (ModifierKeysState.get()) {
			case CTRL: // setGlobalGameKey
				loadingOptions = true;
				guiOptions().updateAllNonCfgFromFile(LAST_OPTIONS_FILE);
				loadingOptions = false;
				break;
			case CTRL_SHIFT: // setLocalGameKey
				loadingOptions = true;
				guiOptions().updateFromFile(LAST_OPTIONS_FILE, localOptions());
				loadingOptions = false;
				break;
			case SHIFT: // setLocalLastKey
				guiOptions().updateFromFile(GAME_OPTIONS_FILE, localOptions());
				break;
			default: // setGlobalLastKey
				loadingOptions = true;
				guiOptions().updateAllNonCfgFromFile(GAME_OPTIONS_FILE);
				loadingOptions = false;
			}
		refreshGui(0);
	}

	// ---------- Events management
	@Override public void advanceHelp() { cancelHelp(); }
	@Override public void cancelHelp()  { RotPUI.helpUI().close(); }
	@Override public void showHelp()    { showHotKeys(); }
	@Override public void showHotKeys() {
		Rectangle hotKeysBox  = new Rectangle(mX, mY, 0, 0);
		String    hotKeysText = text("MOD_OPTIONS_HELP_HK");
		guidePopUp.setDest(hotKeysBox, hotKeysText, getGraphics());
		contextHlp = true;
	}
	@Override public void mouseClicked(MouseEvent e) {  }
	@Override public void mousePressed(MouseEvent e) {  }
	@Override public void mouseEntered(MouseEvent e) { setModifierKeysState(e); repaint(); }
	@Override public void mouseExited(MouseEvent e)	 { clearGuide(); }
	@Override public void mouseDragged(MouseEvent e) {  }
	@Override public void mouseMoved(MouseEvent e)	 {
		mX = e.getX();
		mY = e.getY();
		if (hoverBox != null && hoverBox.contains(mX,mY)) {
			hoverChanged = false;
			return;
		}
		hoverChanged = true;
		prevHover	 = hoverBox;
		prevPolyBox	 = hoverPolyBox;
		hoverPolyBox = null;
		hoverBox	 = null;

		for (Box box : boxBaseList)
			if (box.contains(mX,mY)) {
				hoverBox = box;
				break;
			}
		if (hoverBox != prevHover) {
			loadGuide();
			repaint();
			return;
		}
		for (PolyBox box : polyBoxList)
				if (box.contains(mX,mY)) {
					hoverPolyBox = box;
					break;
				}
		if (hoverPolyBox != prevPolyBox) {
			repaint();
		}
	}
	@Override public void keyReleased(KeyEvent e)	 { checkModifierKey(e); }
	@Override public void keyPressed(KeyEvent e)	 {
		checkModifierKey(e);
		switch(e.getKeyCode()) {
			case KeyEvent.VK_F1:
				if (e.isShiftDown()) {
					showHotKeys();
					return;
				}
				if (showContextualHelp())
					return;
				showHelp(); // Panel Help
				return;
			case KeyEvent.VK_E:
				if (e.isAltDown() && e.isControlDown()) {
					debugReloadLabels("en");
					forceUpdate(true);
				}
				return;
			case KeyEvent.VK_G:
				doGuideBoxAction();
				return;
			case KeyEvent.VK_L:
				if (e.isAltDown()) {
					debugReloadLabels("");
					forceUpdate(true);
				}
				return;
			case KeyEvent.VK_F:
				if (e.isAltDown() && e.isControlDown()) {
					debugReloadLabels("fr");
					forceUpdate(true);
				}
				else if (e.isAltDown())
					IMainOptions.defaultSettings.set(FUSION_DEFAULT);
				return;
			case KeyEvent.VK_M:
				if (e.isAltDown())
					IMainOptions.defaultSettings.set(MOO1_DEFAULT);
				return;
			case KeyEvent.VK_R:
				if (e.isAltDown())
					IMainOptions.defaultSettings.set(ROTP_DEFAULT);
				return;
		}
	}
	// ---------- Help management
	protected void loadHotKeysUI()			{}
	protected void loadGuide()				{
		if (hoverBox == null) {
			clearGuide();
			return;
		}
		if (!(showGuide.get() || dialGuide))
			return;
		guidePopUp.setDest(hoverBox, false, getGraphics());
	}
	private boolean showContextualHelp()	{ // Following "F1!
		if (hoverBox == null)
			return false; // ==> panel help
		
		if (!guidePopUp.setDest(hoverBox, true, getGraphics()))
			return false; // ==> panel help
		contextHlp = true;
		return true;
	}
	protected void showGuide(Graphics g)	{
		if (!(showGuide.get() || dialGuide || contextHlp))
			return;
		guidePopUp.paintGuide(g);
	}
	private void clearGuide()				{
		guidePopUp.clear();
		contextHlp = false;
	}
	// ========== Sub Classes ==========
	//
	final class Box extends Rectangle {
		private static final long serialVersionUID = 1L;
		private IParam	param;
		private String	label;
		private ModText modText;
		private int 	mouseBoxIndex;

		// ========== Constructors ==========
		//
		public Box()				{ addToList(); }
		private Box(boolean add)	{ if (add) addToList(); }
		private Box(ModText modText, boolean addToList) {
			this(addToList);
			this.modText = modText;
		}
		Box(String label)			{
			this();
			boxHelpList.add(this);
			this.label = label;
		}
		Box(IParam param)	{
			this();
			boxHelpList.add(this);
			this.param = param;
		}
		Box(IParam param, int mouseBoxIndex) {
			this(param);
			mouseBoxIndex(mouseBoxIndex);
		}
		private	void removeFromList()		 {
			boxBaseList.remove(this);
			//System.out.println("Removed " + boxBaseList.size() + " " + getDescription());
		}
		private void addToList() 			 {
			boxBaseList.add(this);
			//System.out.println("added " + boxBaseList.size() + " " + getDescription());
		}
		private void initGuide(String label) { this.label = label; }
		public  void initGuide(IParam param) { this.param = param; }
		private void mouseBoxIndex(int idx)	 { mouseBoxIndex = idx; }
		public String getLabel()			 { return label; }
		public IParam getParam()			 { return param; }

		// ========== Doers ==========
		//
		boolean checkIfHovered() { return checkIfHovered(null); }
		boolean checkIfHovered(JTextPane descBox) {
			if (contains(mX,mY)) {
				hoverBox = this;
				if (descBox != null)
					descBox.setText(getDescription());
				hoverChanged = (hoverBox != prevHover);
				if (hoverChanged) {
					mouseEnter();
					if (descBox != null)
						descBox.setText(getDescription());
					loadGuide();
					if (prevHover != null) {
						prevHover.mouseExit();
						repaint(prevHover);
					}
					repaint();					
				}
				return true;
			}
			return false;
		}
		private void mouseEnter() {
			if (modText != null) {
				if (param != null)
					param.updated(true);
				modText.mouseEnter();
			}
		}
		void mouseExit() {
			if (modText != null) {
				if (param != null)
					param.updated(true);
				modText.mouseExit();
			}
		}
		void fillButtonBackImg(Graphics2D g) {
			if (retina)
				g.fillRoundRect(retina(x-xButton), retina(y-yButton), retina(width), retina(height), cnrR, cnrR);
			else
				g.fillRoundRect(x-xButton, y-yButton, width, height, cnr, cnr);
		}
		void fillButtonFullImg(Graphics2D g) {
			if (retina)
				g.fillRoundRect(retina(x), retina(y), retina(width), retina(height), cnrR, cnrR);
			else
				g.fillRoundRect(x, y, width, height, cnr, cnr);
		}
		public void drawString (Graphics2D g, int fSizeMax, int fSizeMin)	{
			String str	= replaceDigits(param.guideValue());
			int fSize	= scaledFont(g, str, width-s2, fSizeMax, fSizeMin);
			int offset	= (scaled(fSizeMax) - scaled(fSize))/3 + s3;
			int sw = g.getFontMetrics().stringWidth(str);
			int sx = x + (width - sw)/2;
			int sy = y + height - offset;
			g.drawString(str, sx, sy);
		}
		// ========== Getters ==========
		//
		public String getDescription()		 {
			String desc = getParamDescription();
			if (desc == null || desc.isEmpty()) {
				desc = getLabelDescription();
				if (desc == null)
					return "";
			}
			return desc;
		}
		private String getFullHelp()		 {
			String help = getParamFullHelp();
			if (help == null || help.isEmpty()) {
				help = getLabelHelp();
				if (help == null)
					return "";
			}
			return help;
		}
		String getHelp()					 {
			String help = getParamHelp();
			if (help == null || help.isEmpty()) {
				help = getLabelHelp();
				if (help == null)
					return "";
			}
			return help;
		}
		private String getGuide()			 {
			String guide = getParamGuide();
			if (guide == null || guide.isEmpty()) {
				guide = getLabelHelp();
				if (guide == null)
					return "";
			}
			return guide;
		}
		int	    mouseBoxIndex()				 { return mouseBoxIndex; }
		private String getLabelDescription() { return IParam.langDesc(label); }
		private String getLabelHelp()		 { return IParam.langHelp(label); }
		private String getParamDescription() {
			if (param == null)
				return "";
			String desc = param.getGuiDescription();
			if (desc == null || desc.isEmpty())
				return param.getToolTip();
			return desc;
		}
		private String getParamHelp()	 	 {
			if (param == null)
				return "";
			return param.getHelp();
		}
		private String getParamFullHelp()	 {
			if (param == null)
				return "";
			return param.getFullHelp();
		}
		private String getParamGuide()		 {
			if (param == null)
				return "";
			return param.getGuide();
		}
	}

	final class PolyBox extends Polygon {
		private static final long serialVersionUID = 1L;
		static final int LEFT_ARROW		= 0;
		static final int RIGHT_ARROW	= 1;
		static final int UP_ARROW		= 2;
		static final int DOWN_ARROW		= 3;
		private final int dir;
		private int dy	= s8;
		private int dx	= s3;
		// ========== Constructors ==========
		//
		PolyBox(int direction) {
			super();
			polyBoxList.add(this);
			dir = direction;
		}
		void setPos(int x, int y)	{
			reset();
			switch (dir) {
			case LEFT_ARROW:
				addPoint(x-s4,	y+s1);
				addPoint(x-s4,	y+dy+dy-s2);
				addPoint(x-s13,	y+dy);
				return;
			case RIGHT_ARROW:
				addPoint(x+s4,	y+s1);
				addPoint(x+s4,	y+dy+dy-s2);
				addPoint(x+s13,	y+dy);
				return;
			case UP_ARROW:
				addPoint(x,				y-dy-s1);
				addPoint(x+s13+dx+dx,	y-dy-s1);
				addPoint(x+s7+dx,		y-dy-s13);
				return;
			case DOWN_ARROW:
				addPoint(x,				y+dy+s2);
				addPoint(x+s13+dx+dx,	y+dy+s2);
				addPoint(x+s7+dx,		y+dy+s14);
				return;
			}
		}
		void setAndFill(Graphics2D g, int x, int y, int h)	{
			dy = h/2;
			dx = h/4;
			setPos(x, y);
			g.fill(this);
		}
	}
	public final class ModText extends BaseText {
		
		private final Box box;
		private final int baseFontsize;
		boolean forceHover = false;
		@Override protected Color textColor() {
			if (forceHover)
				return hoverC;
			else
				return super.textColor();
		}
		/**
		* @param p		BasePanel
		* @param fSize	fontSize
		* @param c1		enabledC
		* @param c2		disabledC
		* @param c3		hoverC
		* @param c4		depressedC
		* @param c5		shadeC
		* @param add	add to box list
		*/
		ModText(BasePanel p, int fSize, Color c1, Color c2, Color c3, Color c4, Color c5, boolean add) {
			super(p, false, fSize, 0, 0, c1, c2, c3, c4, c5, 0, 0, 0);
			box = new Box(this, add);
			baseFontsize = fSize;
		}
		public void    fontMult(float fMult)	{ super.newFontSize((int) (baseFontsize * fMult)); }
		public void	   removeBoxFromList()		{ box.removeFromList(); }
		public ModText initGuide(IParam param)	{ box.initGuide(param); return this; }
		public ModText initGuide(String label)	{ box.initGuide(label); return this; }
		Box box() {
			box.setBounds(bounds());
			return box;
		}
	}
	// ===============================================================================
	public final class GuidePopUp {
		private static final int FONT_SIZE	= 16;
		private final int maxWidth      = scaled(400);
		private final Color helpColor	= new Color(240,240,240);
		private final JTextPane border	= new JTextPane();
		private final JTextPane	margin	= new JTextPane();
		private final JTextPane pane	= new JTextPane();
		private Rectangle dest			= new Rectangle(0,0,0,0);
		private String text;
		private int x, y, w, h;
		private int[] lineArr;
		private boolean fullHelp;
		private Color bgC		= GameUI.setupFrame();;
		private Color bdrC		= new Color(bgC.getRed(), bgC.getGreen(), bgC.getBlue(), 160);
		private Color lineColor	= bgC;
		private boolean initialised = false;

		// ========== Constructors and initializers ==========
		//	
		GuidePopUp()		{ }
		private void init() {
			if (initialised)
				return;
			add(border, 0);
			add(margin, 0);
			add(pane, 0);
			border.setOpaque(true);
			margin.setOpaque(true);
			pane.setOpaque(true);
			pane.setContentType("text/html");
			pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			hide();
			guideFontSize(FONT_SIZE);
			initialised = true;
		}
		private void setText(String newText)	{
			text = newText;
			text = text.replace("<=", "&lt;=");
			text = text.replace(">=", "&gt;=");
			text = text.replace("<>", "&lt;&gt;");
		}
		private void setFullHelp(boolean full)	{ fullHelp = full; }
		private void setDest(Rectangle newDest)	{
			dest = newDest;
			setVisible();
			init(dest);
		}
		public  void setDest(Rectangle dest, String text, Graphics g0)	{
			guideFontSize(FONT_SIZE);
			lineArr = null;
			setFullHelp(dest.width == 0);
			setText(text);
			setDest(dest);
		}
		private boolean setDest(Box dest, boolean fullHelp, Graphics g0){
			if (dest == null)
				return false;
			guideFontSize(FONT_SIZE);
			String txt;
			if (fullHelp)
				txt = dest.getFullHelp();
			else
				txt = dest.getGuide();
			if (txt == null || txt.isEmpty())
				return false;
			setFullHelp(fullHelp);
			setText(txt);
			setDest(dest);
			if (guideFontSize() < FONT_SIZE) { // To update header size
				if (fullHelp)
					txt = dest.getFullHelp();
				else
					txt = dest.getGuide();
				if (txt == null || txt.isEmpty())
					return false;
				setFullHelp(fullHelp);
				setText(txt);
				setDest(dest);
			}
			return true;
		}
		// ========== Shared Methods ==========
		//
		private void setVisible()		{
			if(pane.isVisible())
				return;
			border.setVisible(true);
			margin.setVisible(true);
			pane.setVisible(true);
		}
		private void hide()				{
			border.setVisible(false);
			margin.setVisible(false);
			pane.setVisible(false);
		}
		public void clear()		{ hide(); }
		// ========== Private Methods ==========
		//
		private void paintGuide(Graphics g0)	{
			if (dest == null)
				return;
			if (!pane.isVisible())
				return;
			Graphics2D g = (Graphics2D) g0;
			setVisible();
			border.setBackground(bdrC);
			border.setBounds(x-s8, y-s8, w+s16, h+s16);
			margin.setBackground(bgC);
			margin.setBounds(x-s3, y-s3, w+s6, h+s6);
			pane.setFont(plainFont(guideFontSize()));
			pane.setBackground(bgC);
			pane.setBounds(x, y, w, h);
			drawLines(g);
		}
		private void setLineArr(int... arr)		{ lineArr = arr; }
		private void drawLines(Graphics2D g)	{
			if (lineArr != null) {
				Stroke prev = g.getStroke();
				g.setStroke(stroke2);
				g.setColor(lineColor);
				int size = lineArr.length/2 - 1;
				for (int i=0; i<size; i++) {
					int k = 2*i;
					g.drawLine(lineArr[k], lineArr[k+1], lineArr[k+2], lineArr[k+3]);
				}
				g.setStroke(prev);
			}			
		}
		private void autoSize(int width)		{
			int iW = scaled(Rotp.IMG_W - 20);
			int iH = scaled(Rotp.IMG_H - 20);
			int testW, preTest;
			bgC  = fullHelp ? helpColor : GameUI.setupFrame();
			bdrC = new Color(bgC.getRed(), bgC.getGreen(), bgC.getBlue(), 160);
			w = Short.MAX_VALUE;
			boolean go = true;
			while (go) {
				pane.setFont(plainFont(guideFontSize()));
				h = Short.MAX_VALUE;
				preTest = -1;
				testW = width - 1; // to prevent rounding errors
				while (h > iH && preTest != testW && testW < iW) {
					preTest = testW;
					pane.setSize(new Dimension(testW, Short.MAX_VALUE));
					pane.setText(text);
					Dimension paneSize = pane.getPreferredSize();
					w = min(testW, paneSize.width);
					h = paneSize.height;
					testW *= (float) h /iH;
				}
				go = (w > iW || h > iH);
				if (go) {
					guideFontSize (max (1, min(guideFontSize()-1, (int)(guideFontSize() * (float)iH/h -1))));
					go = guideFontSize() > 1;
				}
			}
			w += 1;
			margin.setSize(new Dimension(w+s6, h+s6));
			border.setSize(new Dimension(w+s16, h+s16));
			pane.setSize(new Dimension(w, h));
		}
		private void init(Rectangle dest)		{ init(dest, s20, s20); }
		private void init(Rectangle dest, int xShift, int yShift) {
			init(dest, xShift, yShift, s10, s10); }
		private void init(Rectangle dest, int xShift, int yShift, int xCover, int yCover) {
			init(dest, xShift, yShift, xCover, yCover, s10, s10); }
		private void init(Rectangle dest,
				int xShift, int yShift, int xCover, int yCover, int xMargin, int yMargin) {
			int xb, xd, yb, yd;
			int iW = scaled(Rotp.IMG_W);
			int iH = scaled(Rotp.IMG_H);
			xCover = min(xCover, dest.width/2);
			yCover = min(yCover, dest.height/2);
			
			autoSize(maxWidth);
			// relative position
			// find X location
			if (2*dest.x + dest.width  > iW) { // put box to the left
				x = dest.x - w - xShift;
				if (x < xMargin)
					x = xMargin;
				xb = x + w;
				xd = dest.x + xCover;
				if (xd < xb)
					xd = xb + s10;
			}
			else { // put box to the right
				x = dest.x + dest.width + xShift;
				if (x+w > iW-xMargin)
					x = iW-xMargin - w;
				xb = x;
				xd = dest.x + dest.width - xCover;
				if (xd > xb)
					xd = xb - s10;
			}
			// find Y location
			if (2*dest.y + dest.width  > iH) { // put box to the top
				y = dest.y - h - yShift;
				if (y < yMargin)
					y = yMargin;
				yb = y + h;
				yd = dest.y + yCover;
				if (yd < yb)
					yb = yd + s10;
			}
			else { // put box to the bottom
				y = dest.y + dest.height + yShift;
				if (y+h > iH-yMargin)
					y = iH-yMargin - h;
				yb = y;
				yd = dest.y + dest.height - yCover;
				if (yd > yb)
					yb = yd - s10;
			}
			if (dest.width>0) // no line for Hotkeys help
				setLineArr(xb, yb, xd, yd);
		}
	}
}
