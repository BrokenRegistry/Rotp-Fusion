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
import static rotp.model.game.IBaseOptsTools.LIVE_OPTIONS_FILE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.main.SystemPanel;
import rotp.ui.util.IParam;
import rotp.util.ModifierKeysState;

// modnar: add UI panel for modnar MOD game options, based on StartOptionsUI.java
abstract class AbstractOptionsUI extends BaseModPanel implements MouseWheelListener {
	private static final long serialVersionUID = 1L;
	private final String guiTitleID;
	private final String GUI_ID;
	
	private static final int settingFontSize = 20;
	private static final int descFontSize	 = 15;
	private Font descFont;
	private static int columnPad	= s20;
	private static int smallButtonH = s30;
	private static int hSetting	    = s90;
	private static int lineH		= s17;
	private static int rowPad		= s20;
	private static int hDistSetting = hSetting + rowPad; // distance between two setting top corner
	private int leftM, rightM, topM, yTop;
	private int numColumns, numRows;
	private int yTitle, xDesc, yDesc;
	private int xSetting, ySetting, wSetting; // settings var
	private int index, column;
	
	private Color textC = SystemPanel.whiteText;
	private LinkedList<Integer>	lastRowList = new LinkedList<>();
	private LinkedList<ModText> btList		= new LinkedList<>();

	private final LinkedHashMap<Integer, BufferedImage>	imgList	= new LinkedHashMap<>();
	private boolean forceUpdate = true;

	// ========== Constructors and initializers ==========
	//
	AbstractOptionsUI(String guiTitle_ID, String guiId) {
		guiTitleID = guiTitle_ID;
		GUI_ID = guiId;
	}
	@Override protected void singleInit() {
		System.out.println("AbstractOptionsUI SingleInit()");
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		reset();
	}
	private void elementsPositioning()	{
		// Elements positioning
		int shiftTitle	= s40;
		int shiftButton	= s15;
		int topPad		= hSetting;
		int hSettings	= hDistSetting * numRows;

		// ----- With Management
		if (numColumns == 4)
			columnPad = s12;

		leftM	= max(columnPad, scaled(100 + (3-numColumns) * 150));
		rightM	= leftM;
		xDesc	= leftM + columnPad/2;

		// ----- Height Management
		if (numRows == 6) {
			topPad		-= s40; // Push the settings up			
			shiftTitle	-= s10; // Shift the title a little
		}	
		hGist	= topPad + hSettings + smallButtonH + s10;
		topM	= (hFull - hGist)/2;
		yTop	= topM + topPad; // First setting top position
		yButton	= yTop + hSettings - shiftButton + s13;
		yTitle	= topM + shiftTitle;
		yDesc	= yTitle + s20;
		
		// Special positioning for 6 rows
		if (numRows == 6) {
			// Move the description to the Title level
			yDesc	= yTitle;
		}		
	}
	private void buildRowCountList() {
		numColumns = 1;
		Integer numParam = 0;
		for (IParam param : activeList) {
			if (param == null) {
				numColumns++;
				lastRowList.add(numParam);
			}
			else
				numParam++;
		}
		lastRowList.add(numParam);
		while (activeList.remove(null));
	}
	void reset() {
		setOpaque(false);
		textC = SystemPanel.whiteText;
		numColumns = 0;
		// Call for filling the settings
		init0();
		
		if (paramList == null) 
			activeList = duplicateList;
		else
			activeList = paramList;
		
		buildRowCountList();
		
		for (int i=0; i<activeList.size(); i++)
			btList.add(newBT().initGuide(activeList.get(i)));

		// numRows = Max column length
		numRows	 = lastRowList.getFirst();
		for (int i=1; i<lastRowList.size(); i++)
			numRows = max(numRows, lastRowList.get(i)-lastRowList.get(i-1));
	}
	// ========== Abstract Methods Request ==========
	//
	protected abstract void init0();
	// ========== Optimization Methods ==========
	//
	@Override protected void initBackImg() {
		long timeStart = System.currentTimeMillis();
		elementsPositioning();
		forceUpdate = true;
		backImg = newOpaqueImage(wFull, hFull);
		Graphics2D g = (Graphics2D) backImg.getGraphics();
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
		g.drawImage(back, 0, 0, wFull, hFull, 0, 0, imgW, imgH, this);

		g.setPaint(bg());
		g.fillRect(leftM, topM, wGist, hGist);

		// Title
		g.setFont(narrowFont(30));
		String title = text(guiTitleID);
		int sw = g.getFontMetrics().stringWidth(title);
		int xTitle = (wFull-sw)/2;
		if (numRows == 6) {
			xTitle = wFull -rightM - sw - 2*columnPad;
		}
		drawBorderedString(g, title, 1, xTitle, yTitle, Color.black, Color.white);

		// Description
		g.setFont(narrowFont(18));
		String expl = text("SETTINGS_DESCRIPTION");
		g.setColor(SystemPanel.blackText);
		drawString(g, expl, xDesc, yDesc);
		
		// buttons location
		int smallButtonW = scaled(180);
//		exitBox.setBounds(wFull-scaled(189)-rightM, yButton+s2, smallButtonW, smallButtonH);
		exitBox.setBounds(wFull-scaled(189)-rightM, yButton, smallButtonW, smallButtonH);
		smallButtonW = defaultButtonWidth(g);
		defaultBox.setBounds(exitBox.x-smallButtonW-s30, yButton, smallButtonW, smallButtonH);
		smallButtonW = defaultButtonWidth(g);
		lastBox.setBounds(defaultBox.x-smallButtonW-s30, yButton, smallButtonW, smallButtonH);
		smallButtonW = userButtonWidth(g);
		userBox.setBounds(lastBox.x-smallButtonW-s30, yButton, smallButtonW, smallButtonH);
		smallButtonW = guideButtonWidth(g);
		guideBox.setBounds(leftM+s9, yButton, smallButtonW, smallButtonH);
        initButtonBackImg();

		g.dispose();
		if (showTiming) 
			System.out.println("initBackImg() Time = " + (System.currentTimeMillis()-timeStart));
    }
	// ========== Other Methods ==========
	//
	private  ModText newBT() { 
		return new ModText(this, settingFontSize,  textC, textC, hoverC, depressedC, textC, true);
	}
	private Font descFont() {
		if (descFont == null)
			descFont = narrowFont(retina(descFontSize));
		return descFont;
	}
	private void paintSetting(Graphics2D g, ModText txt, String desc) {
		int margin = s2;
		int xNew = margin;
		int yNew = margin + lineH;
		int xNewR = retina(xNew);
		int yNewR = retina(yNew);
		IParam param = activeList.get(index);
		boolean refresh = forceUpdate || param.updated();
		refresh = true;
		if (refresh) {
			int wSettingR	= retina(wSetting);
			int hSettingR	= retina(hSetting);
			int columnPadR	= retina(columnPad);
			int lineHR	= retina(lineH);
			int imgW	= wSetting + xNew + margin;
			int imgH	= hSetting + yNew + margin;
			int imgWR	= retina(imgW);
			int imgHR	= retina(imgH);

			BufferedImage img = new BufferedImage(imgWR, imgHR, TYPE_INT_ARGB);
			Graphics2D gi = (Graphics2D) img.getGraphics();
			gi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			gi.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
			
			if (retina) {
				gi.setStroke(stroke6);
				txt.fontMult(retinaFactor);
			}
			else
				gi.setStroke(stroke3);

			if (param.isSubMenu()) {
				gi.setColor(GameUI.textColor());
				txt.enabledC(GameUI.textColor());
			}
			else
				gi.setColor(SystemPanel.blackText);

			int s10R = retina(s10);
			int s7R  = retina(s7);
			int s25R = retina(s25);
			int blankW = txt.stringWidth(gi) + s10R;
			param.drawBox(gi, xNewR, yNewR, wSettingR, hSettingR, s10R/2, blankW);
			txt.setScaledXY(xNewR + columnPadR, yNewR+s7R);
			txt.draw(gi);
			if (param.isSubMenu())
				gi.setColor(GameUI.textColor());
			else
				gi.setColor(SystemPanel.blackText);
			gi.setFont(descFont());
			List<String> lines = scaledNarrowWrappedLines(gi, desc, wSettingR-s25R, 4, retina(15), retina(12));
			int y3 = xNewR + lineHR + s10R;
			for (String line: lines) {
				y3 += lineHR;
				drawString(gi, line, xNewR+columnPadR, y3);
			}		
			gi.dispose();
			if (retina) {
				int x = xSetting-xNew;
				int y = ySetting-yNew;
				g.drawImage(img, x, y, x+imgW, y+imgH, 0, 0, imgWR, imgHR, null);
				txt.fontMult(1);
			}
			else
				g.drawImage(img, xSetting-xNew, ySetting-yNew, null);

			param.updated(false);
			imgList.put(index, img);

			txt.setScaledXY(xSetting+columnPad, ySetting+s7);
			txt.setBounds(xSetting, ySetting-s7, wSetting, hSetting+s10);
		}
		else if (retina) {
			BufferedImage img = imgList.get(index);
			int x = xSetting-xNew;
			int y = ySetting-yNew;
			int w = img.getWidth();
			int h = img.getHeight();
			g.drawImage(imgList.get(index), x, y, x+invRetina(w), y+invRetina(h), 0, 0, w, h, null);				
		}
		else
			g.drawImage(imgList.get(index), xSetting-xNew, ySetting-yNew, null);
	}
	private void goToNextSetting() {
		index++;
		if (index >= lastRowList.get(column)) {
			column++;
			if (column == numColumns) {
				// System.err.println(GUI_ID + ": column > numColumns");
				column--;
			}
			xSetting = xSetting + wSetting + columnPad;
			ySetting = yTop;
		} else
			ySetting += hDistSetting;
	}
	private void mouseCommon(boolean up, boolean mid, boolean shiftPressed, boolean ctrlPressed
			, MouseEvent e, MouseWheelEvent w) {
		for (int i=0; i<activeList.size(); i++) {
			if (hoverBox == btList.get(i).box()) {
				IParam param = activeList.get(i);
				if (param.isSubMenu()) {
					if (e == null)
						return;
					super.close();
			        disableGlassPane();
			        param.toggle(e, GUI_ID, this);
					return;
				}			
				param.toggle(e, w, this);
				param.updated(true);
				btList.get(i).repaint(activeList.get(i).getGuiDisplay());
				if (showGuide())
					loadGuide();
				repaint();
				return;
			}			
		}
	}
	private void setLocalToDefault(boolean excludeCfg, boolean excludeSubMenu) {
		for (IParam param : activeList)
			if (!(excludeCfg && param.isCfgFile())
					&& !(excludeSubMenu && param.isSubMenu()))
			param.setFromDefault(excludeCfg, excludeSubMenu);
	}
	// ========== Overriders ==========
	//
	@Override public void init()	 {
		reset();
		super.init();
		wFull	= RotPUI.setupRaceUI().getWidth();
		hFull	= RotPUI.setupRaceUI().getHeight();
		wGist	= wFull - (leftM + rightM);
		wSetting = (wGist/numColumns)-columnPad;
		if (!globalOptions) // The new ways
			guiOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
		forceUpdate = true;
		enableGlassPane(this);
		refreshGui(0);
		forceUpdate = true;
	}
	@Override protected void close() {
		super.close();
		descFont = null;
        disableGlassPane();
		RotPUI.setupGalaxyUI().init();
	}
	@Override protected void doExitBoxAction()		{
		if (globalOptions) { // The old ways
			//buttonClick();
			UserPreferences.save();
			close();			
		}
		super.doExitBoxAction();
	}
	@Override protected void doDefaultBoxAction()	{
		if (globalOptions) { // The old ways
			buttonClick();
			switch (ModifierKeysState.get()) {
			case CTRL:
			case CTRL_SHIFT: // cancelKey
				UserPreferences.reload();
				break;
			default: // setLocalDefaultKey
				setLocalToDefault(false, isSubMenu); // Main panels (Race & Galaxy) reset their sub menus
				break; 
			}
			refreshGui(0);
		}
		else
			super.doDefaultBoxAction();
	}
	@Override public void refreshGui(int level)	{
		super.refreshGui(level);
		for (int i=0; i<activeList.size(); i++) {
			IParam param = activeList.get(i);
			if (param != null) {
				ModText modText = btList.get(i);
				if (modText != null)
					modText.displayText(param.getGuiDisplay());
			}
		}
		repaint();
	}
	@Override protected String GUI_ID()		{ return GUI_ID; }
	@Override public void paintComponent(Graphics g0)	{
		// showTiming = true;
		if (showTiming)
			System.out.println("===== Classic Menu PaintComponents =====");
		long timeStart = System.currentTimeMillis();
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 

        // background image
        g.drawImage(backImg(), 0, 0, this);

		// Buttons background image
        drawButtons(g);

		Stroke prev = g.getStroke();
		g.setStroke(stroke3);
		
		// Loop thru the parameters
		index	 = 0;
		column	 = 0;
		xSetting = leftM + columnPad/2;
		ySetting = yTop;
		// First column (left)
		while (index < activeList.size()) {
			paintSetting(g, btList.get(index), activeList.get(index).getGuiDescription());
			goToNextSetting();
		}
		forceUpdate = false;
		g.setStroke(prev);
		showGuide(g);
		if (showTiming)
			System.out.println("Classic Menu paintComponent() Time = " + (System.currentTimeMillis()-timeStart));
	}
	@Override public void keyPressed(KeyEvent e)		{
		super.keyPressed(e);
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				doExitBoxAction();
				return;
			case KeyEvent.VK_SPACE:
			default: // BR:
		}
	}
	@Override public void mouseMoved(MouseEvent e)		{
		mX = e.getX();
		mY = e.getY();
		if (hoverBox != null && hoverBox.contains(mX,mY)) {
			hoverChanged = false;
			return;
		}
		prevHover	 = hoverBox;
		hoverBox	 = null;
		hoverChanged = true;
		for (ModText bt : btList)
			if (bt.box() == prevHover) {
				repaint();
				break;
			}
		for (Box box : boxBaseList)
			if (box.checkIfHovered())
				break;
		if (prevHover != null) {
			prevHover.mouseExit();
			loadGuide();
			repaint();
		}
	}
	@Override public void mouseReleased(MouseEvent e)	{
		checkModifierKey(e);
		if (e.getButton() > 3)
			return;
		if (hoverBox == null)
			return;
		boolean up	= !SwingUtilities.isRightMouseButton(e); // BR: added bidirectional
		boolean mid	= !SwingUtilities.isMiddleMouseButton(e); // BR: added reset click
		boolean shiftPressed = e.isShiftDown();
		boolean ctrlPressed  = e.isControlDown();
		mouseCommon(up, mid, shiftPressed, ctrlPressed, e, null);
		if (hoverBox == exitBox)
			doExitBoxAction();
		else if (hoverBox == defaultBox)
			doDefaultBoxAction();
		else if (hoverBox == userBox)
			doUserBoxAction();
		else if (hoverBox == guideBox)
			doGuideBoxAction();
		else if (hoverBox == lastBox)
			doLastBoxAction();
	}
	@Override public void mouseEntered(MouseEvent e)	{
		for (int i=0; i<activeList.size(); i++) {
			if (hoverBox == btList.get(i).box()) {	
				btList.get(i).repaint(activeList.get(i).getGuiDisplay());
				if (showGuide()) {
					loadGuide();
					repaint();
				}
				return;
			}			
		}
	}
	@Override public void mouseWheelMoved(MouseWheelEvent e) {
		checkModifierKey(e);
		boolean shiftPressed = e.isShiftDown();
		boolean ctrlPressed  = e.isControlDown();
		boolean up = e.getWheelRotation() < 0;
		mouseCommon(up, false, shiftPressed, ctrlPressed, null, e);
	}
}
