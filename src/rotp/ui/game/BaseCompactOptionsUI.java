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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;

import rotp.model.game.IGameOptions;
import rotp.model.game.IMainOptions;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.design.DesignUI;
import rotp.ui.main.EmpireColonySpendingPane;
import rotp.ui.main.GovernorOptionsPanel;
import rotp.ui.main.SystemPanel;
import rotp.ui.options.AllSubUI;
import rotp.ui.util.IParam;
import rotp.ui.util.ParamList;
import rotp.util.FontManager;
import rotp.util.ModifierKeysState;

public final class BaseCompactOptionsUI extends BaseModPanel implements MouseWheelListener {
	private static final long serialVersionUID = 1L;
	private String guiTitleID;
	private String GUI_ID;
	
	private static final Color	disabledColor		= GameUI.textColor();
	private static final Color	enabledColor		= GameUI.labelColor();
	private static final Color	defaultValuesColor	= SystemPanel.whiteText;
	private static final Color	customValuesColor	= Color.orange;
	private static final Color	subMenuIconColor	= disabledColor;
	private static final Color	subMenuIconColor2	= Color.YELLOW;
	private static final Color	subMenuIconColor3	= Color.BLACK;
	private static final int	rowPad			= s10;
	private	static final int	descPadM		= s5;
	private static final int	buttonPadV		= rowPad;
	private static final int	descFontSize	= 16;
	private static final int	titleFontSize	= 30;
	private static final int	descLineH		= RotPUI.scaledSize(descFontSize+2);
	private static final int	titleOffset		= s40; // Offset from Margin
	private static final int	titlePad		= s70; // Offset of first setting
	private static final int	minFontSize		= 8;
	private static final int	settingFontSize	= 19;
	private static final int	settingH		= RotPUI.scaledSize(settingFontSize);
	private static final int	settingpadH		= s5;
	private static final int	columnPad		= s12;
	private static final int	tooltipLines	= 2;
	private static final int	descHeigh		= tooltipLines * descLineH + descPadM;
	private static final int	bottomPad		= rowPad;
	private static final int	textBoxH		= settingH + s3;
	private static final int	hDistSetting	= settingH + settingpadH; // distance between two setting top corner
	private static final int	minWidth		= 710;
	private static final int	subMenuIconH	= RotPUI.scaledSize(settingFontSize-4);
	private static final int	subMenuIconW	= subMenuIconH;
	private static final int	subMenuIconPad	= s3;
	private static final int	maxColumnWidth	= RotPUI.scaledSize(300);
	private	static final Font	descFont		= FontManager.current().narrowFont(descFontSize);
	private	static final Font	titleFont		= FontManager.current().narrowFont(titleFontSize);
	
	private	final	JTextPane	descBox			= new JTextPane();
	private int yTop;
	private int numColumns, numRows, hSettingsTotal;
	private int yTitle;
	private int settingLeft, xSetting, ySetting, columnWidth, extraSep; // settings var
	private int index, column;
	private int xDesc, yDesc, descWidth;
	
	private final LinkedList<Integer>	lastRowList	= new LinkedList<>();
	private final LinkedList<ModText>	btListLeft	= new LinkedList<>(); // left part
	private final LinkedList<ModText>	btListRight	= new LinkedList<>(); // right part
	private final LinkedList<ModText>	btListBoth	= new LinkedList<>();
	private final LinkedHashMap<Integer, BufferedImage>	imgList	= new LinkedHashMap<>();
	private	BufferedImage subMenuIcon, subMenuMoreIcon, eyeIcon;
	private SafeListPanel optionsList;
	private BasePanel parentUI;
	private boolean forceUpdate	 = true;
	private boolean callPreview	 = false;
	private IParam  callParam	 = null;
	private boolean isCentered	 = true;
	private boolean isLeftAlign	 = false;
	//private boolean isRightAlign = false;
	private boolean isJustified	 = false;
	private boolean closing	 = false;
	
	// ========== Constructors and initializers ==========
	//
	public BaseCompactOptionsUI() {
		setOpaque(false);
		add(descBox);
		descBox.setOpaque(true);
		descBox.setContentType("text/html");
		descBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	public void initUI(String guiTitle_ID, String guiId) {
		init_0();
		guiTitleID = guiTitle_ID;
		this.GUI_ID = guiId;
		clearOptionsList();
		reInit(false);
	}
	public void initUI(String guiTitle_ID, String guiId, boolean hovering, Rectangle location) {
		init_0();
		guiTitleID = guiTitle_ID;
		this.GUI_ID = guiId;
		xFull = location.x;
		yFull = location.y;
		wFull = location.width;
		hFull = location.height;
		rFull = xFull + wFull;
		bFull = yFull + hFull;
		xGist = 0;
		yGist = 0;
		wGist = wFull;
		hGist = hFull;
		rGist = xGist + wGist;
		bGist = yGist + hGist;
		clearOptionsList();
		reInit(hovering);
	}
	public void reloadUI()	{
		if (closing)
			return;

		for (ModText bt : btListLeft)
			bt.removeBoxFromList();
		for (ModText bt : btListRight)
			bt.removeBoxFromList();
		hoverBox = null;
		clearOptionsList();
		singleInit();
		forceUpdate(true);

		repaint();
	}
	private void clearOptionsList()	{ optionsList	= null; }
	private void vadidOptionsList()	{
		if (optionsList == null)
			optionsList = AllSubUI.getHandle(GUI_ID).optionsMap();
	}
	@Override protected void singleInit()	{
		vadidOptionsList();
		lastRowList.clear();
		activeList		= new SafeListParam(optionsList.name);
		duplicateList	= new SafeListParam(optionsList.name);
		paramList		= new SafeListParam(optionsList.name);
		int totalRows   = 0;
		hSettingsTotal	= 0;
		numColumns = optionsList.size();
		numRows    = 0;
		btListLeft.clear();
		btListRight.clear();
		btListBoth.clear();
		for (SafeListParam list : optionsList) {
			int hSettings = 0;
			totalRows += list.size();
			lastRowList.add(totalRows);
			numRows = max(numRows, list.size());
			for (IParam param : list) {
				if (param != null) {
					hSettings += hDistSetting * param.heightFactor();
					activeList.add(param);
					btListLeft.add(newBT(param.isTitle()).initGuide(param));
					btListRight.add(newBT2(param.isDefaultValue()).initGuide(param));
					if (param.isDuplicate())
						duplicateList.add(param);
					else
						paramList.add(param);
				}
			}
			hSettingsTotal = max(hSettingsTotal, hSettings);
		}
		btListBoth.addAll(btListLeft);
		btListBoth.addAll(btListRight);
	}
	private void init_0() {
		closing = false;
		clearOptionsList();
		lastRowList.clear();
		imgList.clear();
		forceUpdate(true);
		numColumns	= 0;
		numRows		= 0;
		hSettingsTotal	= 0;
		settingLeft	= 0;
		xSetting	= 0;
		ySetting	= 0;
		columnWidth	= 0;
		extraSep	= 0;
		index		= 0;
		column		= 0;
		xDesc		= 0;
		yDesc		= 0;
		descWidth	= 0;
		callPreview	= false;
		callParam	= null;
	}
	@Override protected void terminate()	{
		parentUI = null;
		RotPUI.releaseOptionPanel();
	}

	// ========== Optimization Methods ==========
	//
	@Override protected void initBackImg() {
		// Background image is FullWindow width
		long timeStart = System.currentTimeMillis();
		backImg = newOpaqueImage(wFull, hFull);
		Graphics2D g = (Graphics2D) backImg.getGraphics();
		// modnar: use (slightly) better upsampling
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// background image
		if (!hovering) {
			Image back = GameUI.defaultBackground;
			int imgW = back.getWidth(null);
			int imgH = back.getHeight(null);
			g.drawImage(back, 0, 0, wFull, hFull, 0, 0, imgW, imgH, this);
		}
		g.setPaint(bg());
		g.fillRect(xGist, yGist, wGist, hGist);

		// Title
		g.setFont(titleFont);
		String title = text(guiTitleID);
		int sw = g.getFontMetrics().stringWidth(title);
		int xTitle = xGist + (wGist-sw)/2;
		drawBorderedString(g, title, 1, xTitle, yTitle, Color.black, Color.white);

		// buttons location
		// buttons are referenced to absolute location
		int sep = s9;
		int sep2 = s30;
		int exitWidth = scaled(180);
		int optButtonWidth = 2*defaultButtonWidth(g) + userButtonWidth(g);
		int limWith = 4*sep + exitWidth + optButtonWidth + guideButtonWidth(g);
		int smallButtonW = exitWidth;
		int xPos = xFull+rGist-smallButtonW-sep;
		int ypos;
		exitBox.setBounds(xPos, yButton, smallButtonW, smallButtonH);
		smallButtonW = guideButtonWidth(g);
		guideBox.setBounds(xFull+xGist+sep, yButton, smallButtonW, smallButtonH);
		smallButtonW = defaultButtonWidth(g);
		if (hovering && wGist<limWith) {
			sep2 = (wGist-optButtonWidth-2*sep)/2;
			xPos = xFull+rGist-smallButtonW-sep;
			ypos = yButton-smallButtonH-sep;
		}
		else {
			xPos = exitBox.x-smallButtonW-sep2;
			ypos = yButton;
		}
		defaultBox.setBounds(xPos, ypos, smallButtonW, smallButtonH);
		smallButtonW = defaultButtonWidth(g);
		lastBox.setBounds(defaultBox.x-smallButtonW-sep2, defaultBox.y, smallButtonW, smallButtonH);
		smallButtonW = userButtonWidth(g);
		userBox.setBounds(lastBox.x-smallButtonW-sep2, defaultBox.y, smallButtonW, smallButtonH);

		yDesc = defaultBox.y - ( descHeigh + buttonPadV);
		
		initButtonBackImg();
		g.dispose();
		if (showTiming) 
			System.out.println("initBackImg() Time = " + (System.currentTimeMillis()-timeStart));
	}
	// ========== Other Methods ==========
	//
	private boolean forceUpdate()		{ return forceUpdate; }
	private void clearIcons()			{
		subMenuIcon		= null;
		subMenuMoreIcon	= null;
		eyeIcon			= null;
	}
	private BufferedImage subMenuIcon() {
		// subMenuIcon = null; // TO DO BR: Comment
		if (subMenuIcon == null)
			subMenuIcon = subMenuIcon(retina(subMenuIconW), retina(subMenuIconH),
					subMenuIconColor, subMenuIconColor2, subMenuIconColor3);
		return subMenuIcon;
	}
	private BufferedImage subMenuMoreIcon() {
		// subMenuMoreIcon = null; // TO DO BR: Comment
		if (subMenuMoreIcon == null)
			subMenuMoreIcon = subMenuMoreIcon(retina(subMenuIconW), retina(subMenuIconH),
					subMenuIconColor, subMenuIconColor2, subMenuIconColor3);
		return subMenuMoreIcon;
	}
	private BufferedImage eyeIcon() {
		// eyeIcon = null; // TO DO BR: Comment
		if (eyeIcon == null)
			eyeIcon = eyeIcon(retina(subMenuIconW), retina(subMenuIconH), subMenuIconColor2, true);
		return eyeIcon;
	}

	private ModText newBT(boolean disabled) {
		ModText bt = new ModText(this, settingFontSize, enabledColor,
				disabledColor, hoverC, depressedC, enabledColor, true);
		bt.disabled(disabled);
		return bt;
	}
	private ModText newBT2(boolean isDefault) {
		ModText bt;
		if (isDefault)
			bt = new ModText(this, settingFontSize, defaultValuesColor, 
					disabledColor, hoverC, depressedC, disabledColor, true);
		else
			bt = new ModText(this, settingFontSize, customValuesColor,
					disabledColor, hoverC, depressedC, disabledColor, true);
		return bt;
	}
	private void paintDescriptions(Graphics2D g) {
		descBox.setFont(descFont);
		descBox.setBackground(GameUI.setupFrame());
		descBox.setBounds(xDesc, yDesc, descWidth, descHeigh);
	}
	private void setValueColor(int index) {
		ModText txt2 = btListRight.get(index);
		if (activeList.get(index).isDefaultValue())
			txt2.enabledC(defaultValuesColor);
		else
			txt2.enabledC(customValuesColor);
	}
	private boolean setFontSize(Graphics g, int width, int minSize, ModText... txtArr) {
		for (ModText txt : txtArr)
			txt.fontMult(1);

		while (true) {
			int txtW = 0;
			for (ModText txt : txtArr)
				txtW += txt.stringWidth(g);

			if (txtW <= width)
				return true;
			
			for (ModText txt : txtArr)
				if (!txt.decrFontSize(minSize))
					return false;
		}
	}
	private void paintSetting(Graphics2D g, IParam param) {
		boolean refresh = forceUpdate() || param.updated();
		if (refresh) { // Update imgList
			int xRight	= 0;
			int xLeft	= 0;
			int bRight	= 0;
			int bLeft	= 0;
			int bMargin = s7;
			int margin	= subMenuIconW + subMenuIconPad;
			int width	= retina(columnWidth-margin-margin);
			if(hovering && param.trueChange()) {
				callPreview = true; // To refresh visible parent panel
				callParam = param;
			}
			float hFactor = param.heightFactor();
			int boxH = (int) (textBoxH * hFactor);
			BufferedImage img = new BufferedImage(retina(columnWidth), retina(boxH), TYPE_INT_ARGB);
			Graphics2D gi = (Graphics2D) img.getGraphics();
			gi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			gi.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
			ModText txtLeft	 = btListLeft.get(index);
			txtLeft.repaint(activeList.get(index).getGuiDisplay(0));
			ModText txtRight = btListRight.get(index);
			txtRight.repaint(activeList.get(index).getGuiDisplay(1));
			boolean isHovered = (txtLeft.box() == hoverBox) || (txtRight.box() == hoverBox);
			if (isHovered) {
				txtLeft.forceHover  = true;
				txtRight.forceHover = true;
			}
			if (param.isDefaultValue())
				txtRight.enabledC(defaultValuesColor);
			else
				txtRight.enabledC(customValuesColor);
			if (!param.isValidValue())
				txtRight.enabledC(Color.red);
			
			if (retina) { // Adapt font size to draw image
				txtLeft.fontMult(retinaFactor);
				txtRight.fontMult(retinaFactor);
			}
			if (param.isSubMenu()) {
				boolean hasMore = param.getUnseen() > 0; 
				txtLeft.enabledC(GameUI.textColor());
				txtRight.forceHover = false;
				setFontSize(gi, width, minFontSize, txtLeft);
				int sw	= txtLeft.stringWidth(gi);
				int dxIconFolder = 0;
				if (isCentered || isJustified) {
					xLeft	= max(0, retina(margin) + (width + - sw)/2);
					dxIconFolder	= xLeft - retina(margin);
					bLeft	= dxIconFolder;
					bRight	= xLeft + sw + bMargin;
				}
				else if (isLeftAlign) {
					xLeft	= retina(margin);
					dxIconFolder = 0;
					bLeft	= 0;
					bRight	= xLeft + sw + bMargin;
				}
				else { // if (isRightAlign)
					dxIconFolder	= retina(columnWidth - margin);
					xLeft	= dxIconFolder - sw;
					bLeft	= xLeft;
					bRight	= retina(columnWidth);
				}
				if (hasMore)
					if (isHovered)
						gi.drawImage(eyeIcon(), (dxIconFolder), retina(s3), null);
					else
						gi.drawImage(subMenuMoreIcon(), (dxIconFolder), retina(s3), null);
				else
					gi.drawImage(subMenuIcon(), (dxIconFolder), retina(s3), null);

				txtLeft.setScaledXY(xLeft, retina(rowPad+s7));
				txtLeft.draw(gi);
				gi.dispose();
				if (retina) {
					int y = ySetting-rowPad;
					g.drawImage(img, xSetting, y, xSetting+columnWidth, y+boxH,
							0, 0, retina(columnWidth), retina(boxH), null);
					// restore font size for mouse hovering detection
					txtLeft.fontMult(1);
					txtRight.fontMult(1);
				} 
				else
					g.drawImage(img, xSetting, ySetting-rowPad, null);

				param.updated(false);
				imgList.put(index, img);
				txtLeft.setLeftMargin(0);
				txtLeft.setFixedWidth(true,  invRetina(bRight-bLeft));
				txtLeft.setScaledXY(xSetting + invRetina(bLeft), ySetting+s7);
				txtLeft.updateBounds(g);
				txtLeft.forceHover  = false;
			}
			else { // Not sub-menu
				setFontSize(gi, width, minFontSize, txtLeft, txtRight);
				int swLeft	= txtLeft.stringWidth(gi);
				int swRight	= txtRight.stringWidth(gi);
				int sw		= swLeft + swRight;
				if (isCentered || param.isTitle()) {
					xLeft	= max(retina(margin), (retina(columnWidth-margin) - sw)/2);
					xRight	= xLeft + swLeft;
				}
				else  if (isLeftAlign) {
					xLeft	= retina(margin);
					xRight	= xLeft + swLeft;
				}
				else if (isJustified) {
					xRight	= retina(columnWidth-margin) - swRight;
					xLeft	= retina(margin);
				}
				else { // if (isRightAlign)
					xRight	= retina(columnWidth-margin) - swRight;
					xLeft	= xRight - swLeft;
				}
				xLeft  = max(0, xLeft);
				xRight = max(swLeft, xRight);
				txtLeft.setScaledXY(xLeft, retina(rowPad+s7));
				txtRight.setScaledXY(xRight, retina(rowPad+s7));
				txtLeft.draw(gi);
				txtRight.draw(gi);
				gi.dispose();
				if (retina) {
					int y = ySetting-rowPad;
					g.drawImage(img, xSetting, y, xSetting+columnWidth, y+boxH,
							0, 0, retina(columnWidth), retina(boxH), null);
					// restore font size for mouse hovering detection
					txtLeft.fontMult(1);
					txtRight.fontMult(1);
				} 
				else
					g.drawImage(img, xSetting, ySetting-rowPad, null);

				param.updated(false);
				imgList.put(index, img);
				int mid	= invRetina((xRight+xLeft+swLeft)/2);
				bLeft	= invRetina(xLeft - bMargin);
				bRight	= mid;
				txtLeft.setFixedWidth(true,  bRight-bLeft);
				txtLeft.setScaledXY(xSetting+bLeft, ySetting+s7);
				txtLeft.updateBounds(g);
				txtLeft.forceHover  = false;
				bLeft	= mid;
				bRight	= invRetina(xRight + swRight + bMargin);
				txtRight.setFixedWidth(true,  bRight-bLeft);
				txtRight.setScaledXY(xSetting+bLeft, ySetting+s7);
				txtRight.updateBounds(g);			
				txtRight.forceHover = false;				
			}
		}
		else if (retina) {
			BufferedImage img = imgList.get(index);
			int y = ySetting-rowPad;
			int w = img.getWidth();
			int h = img.getHeight();
			g.drawImage(img, xSetting, y, xSetting+invRetina(w), y+invRetina(h), 0, 0, w, h, null);
		}
		else
			g.drawImage(imgList.get(index), xSetting, ySetting-rowPad, null);
	}
	private void goToNextSetting(IParam param) {
		int hSetting = (int) (hDistSetting * param.heightFactor());
		index++;
		if (index >= lastRowList.get(column)) {
			column++;
			xSetting = xSetting + columnWidth + extraSep;
			ySetting = yFull+yTop;
		} else
			ySetting += hSetting;
	}
	private void mouseCommon(MouseEvent e, MouseWheelEvent w) {
		//System.out.println("BaseCompactOptionUI.mouseCommon()");
		for (int i=0; i<activeList.size(); i++) {
			if (hoverBox == btListLeft.get(i).box()
					|| hoverBox == btListRight.get(i).box() ) {
				IParam param = activeList.get(i);
				if (param.isSubMenu()) {
					if (e == null)
						return;
					super.close();
					param.updated(true);
					btListLeft.get(i).mouseExit();
					btListRight.get(i).mouseExit();
					disableGlassPane();
					param.toggle(e, GUI_ID, this);
					return;
				}
				// ! toggle may change forceUpdate value... Do not swap ! 
				forceUpdate(param.toggle(e, w, this) || forceUpdate());
				param.updated(true);
				setValueColor(i);
				btListLeft.get(i).repaint(activeList.get(i).getGuiDisplay(0));
				btListRight.get(i).repaint(activeList.get(i).getGuiDisplay(1));
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
	public  void start(String p, BasePanel ui) { // Called from subUI
		parentUI = ui;
		start();
	}
	private void start() { // Called from subUI
		super.init();
		hoverBox = null;
		prevHover = null;
		descBox.setVisible(true);
		extraSep = 0;
		int minH = titlePad + hSettingsTotal + descHeigh + buttonPadV + smallButtonH + bottomPad;
		if (hovering)
			if (scaled(minWidth) > wGist)
				minH = minH + buttonPadV + smallButtonH;
		if (minH > hGist) {
			hGist = max(hGist, minH);
			hFull = hGist;
			bGist = yGist + hGist;
			bFull = bGist;
		}
		else {
			xGist = columnPad;
			rGist = wFull - columnPad;
			wGist = rGist - xGist;
			hGist = minH;
			yGist = (hFull - hGist) / 2;
			bGist = yGist + hGist;

			columnWidth = ((wGist-columnPad)/4); // Max Width allowed
			if (numColumns < 4) { // to adjust the panel width
				wGist = (columnWidth + columnPad) * 3; // below 3 the buttons will be squeezed!
				xGist = (wFull - wGist)/2;
				rGist = wFull - xGist;
				if (numColumns == 2)
					extraSep = 6 * columnPad;
			}
		}
		columnWidth	= ((wGist-columnPad)/numColumns);
		int wCorr	= max(0, columnWidth - maxColumnWidth);
		settingLeft	= xFull+xGist + columnPad/2 + wCorr*numColumns/2 - extraSep/2;
		columnWidth	= min(columnWidth, maxColumnWidth);
		yTitle		= yGist + titleOffset;
		yTop		= yGist + titlePad; // First setting top position
		descWidth	= wGist - 2 * columnPad;
		xDesc		= xFull + xGist + columnPad;		
		yButton		= yFull + bGist - (smallButtonH + bottomPad);

		guiOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
		enableGlassPane(this);
		forceUpdate(true);
		refreshGui(0);
		forceUpdate(true);
	}
	// ========== Overriders ==========
	//
	@Override public void preview(String s, IParam param) {
		if (parentUI == null)
			return;
		if (s != null && !s.equalsIgnoreCase("quickGenerate"))
			if (param instanceof ParamList)
				((ParamList) param).set(s);
		parentUI.preview(s, param);
	}
	@Override protected String exitButtonKey() {
		switch (ModifierKeysState.get()) {
		case CTRL:		 return cancelKey;
		case CTRL_SHIFT: return cancelKey;
		case SHIFT:		 return applyKey;
		default:		 return exitKey;
		}
	}
	@Override protected void close() {
		closing = true;
		super.close();
		hoverBox = null;
		prevHover = null;

		if (parentUI != null) {
			if (parentUI instanceof BaseCompactOptionsUI)
				((BaseCompactOptionsUI) parentUI).start();
			else if (parentUI instanceof BaseModPanel)
				((BaseModPanel) parentUI).init();
			else  if (parentUI instanceof DesignUI)
				RotPUI.instance().selectDesignPanel();
		}
		else {
			if (session().galaxy().playerSwapRequest()) {
				session().galaxy().swapPlayerEmpire();
				RotPUI.instance().selectMainPanel();
				RotPUI.instance().mainUI().showDisplayPanel();
			}
			else
				RotPUI.instance().mainUI().map().resetRangeAreas();
		}
		terminate();
	}
	@Override protected void doExitBoxAction()		{
		buttonClick();
		switch (ModifierKeysState.get()) {
		case CTRL:			// Cancel and exit
		case CTRL_SHIFT:	// Cancel and exit
			guiOptions().updateAllNonCfgFromFile(LIVE_OPTIONS_FILE);
			UserPreferences.reload();
			break;
		case SHIFT:			// Apply
			guiOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
			repaintButtons();
			return; 
		default:			// Exit
			guiOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
			break; 
		}
		close();
	}
	@Override protected void doUserBoxAction()		{
		switch (ModifierKeysState.get()) {
		case CTRL: // saveGlobalUserKey
		case CTRL_SHIFT: // saveLocalUserKey
			break;
		case SHIFT: // setLocalUserKey
		default: // setGlobalUserKey
			UserPreferences.reload();
			break; 
		}
		super.doUserBoxAction();
	}
	@Override protected void doDefaultBoxAction()	{
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT: // cancelKey
			UserPreferences.reload();
			break;
		default: // setLocalDefaultKey
			setLocalToDefault(false, true);
			break; 
		}
		super.doDefaultBoxAction();
	}
	@Override protected void doLastBoxAction()		{
		buttonClick();
		switch (ModifierKeysState.get()) {
		case SHIFT: // setLocalLastKey
		default: // setGlobalLastKey
			UserPreferences.reload();
		}
		super.doLastBoxAction();
	}
	@Override protected void forceUpdate(boolean b)	{
		forceUpdate = b;
		if (forceUpdate)
			clearIcons();
	}
	@Override public void refreshGui(int level)		{
		super.refreshGui(level);
		for (int i=0; i<activeList.size(); i++) {
			setValueColor(i);
			btListLeft.get(i).displayText(activeList.get(i).getGuiDisplay(0));
			btListRight.get(i).displayText(activeList.get(i).getGuiDisplay(1));
		}
		repaint();
	}
	@Override protected String GUI_ID()		{ return GUI_ID; }
	@Override public void paintComponent(Graphics g0)	{
		// showTiming = true;
		if (showTiming)
			System.out.println("===== Compact PaintComponents =====");
		long timeStart = System.currentTimeMillis();
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		IGameOptions opts = guiOptions();
		isCentered	 = opts.optionPanelIsCentered();
		isLeftAlign	 = opts.optionPanelIsLeftAlign();
		//isRightAlign = opts.optionPanelIsRightAlign();
		isJustified	 = opts.optionPanelIsJustified();
		
        // background image
        g.drawImage(backImg(), xFull, yFull, this);
		// Buttons background image
        drawButtons(g);
		// Tool tip
		paintDescriptions(g);

		Stroke prev = g.getStroke();
		g.setStroke(stroke3);
		// Loop thru the parameters
		index	 = 0;
		column	 = 0;
		xSetting = settingLeft;
		ySetting = yFull+yTop;
		while (index<activeList.size()) {
			IParam param = activeList.get(index);
			paintSetting(g, param);
			goToNextSetting(param);
		}
			mouseMoved(null); // to select again a box that have been changed by reloadUI
		if (callPreview) {
			parentUI.preview("quickGenerate", callParam);
			callPreview = false;
			callParam = null;
		}
		forceUpdate(false);
		g.setStroke(prev);
		showGuide(g);
		if (showTiming)
			System.out.println("Compact paintComponent() Time = " + (System.currentTimeMillis()-timeStart));	
	}
	@Override public void keyReleased(KeyEvent e)		{
		if(checkModifierKey(e))
			if(hoverBox != null)
				descBox.setText(hoverBox.getDescription());
	}
	@Override public void keyPressed(KeyEvent e)		{
		if(checkModifierKey(e))
			if(hoverBox != null)
				descBox.setText(hoverBox.getDescription());
		super.keyPressed(e);
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				doExitBoxAction();
				return;
			case KeyEvent.VK_A:
				if (e.isShiftDown())
					IMainOptions.optionPanelAlignment.prev();
				else
					IMainOptions.optionPanelAlignment.next();
				//refreshGui(0);
				forceUpdate(true);
				repaint();
				return;
			case KeyEvent.VK_C:
				if (e.isControlDown()) {
					IGameOptions.optionPanelAlignment.set(IGameOptions.CENTERED);
					forceUpdate(true);
				}
				return;
			case KeyEvent.VK_J:
				if (e.isControlDown()) {
					IGameOptions.optionPanelAlignment.set(IGameOptions.JUSTIFIED);
					forceUpdate(true);
				}
				return;
			case KeyEvent.VK_L:
				if (e.isControlDown()) {
					IGameOptions.optionPanelAlignment.set(IGameOptions.LEFT_ALIGN);
					forceUpdate(true);
				}
				return;
			case KeyEvent.VK_R:
				if (e.isControlDown()) {
					IGameOptions.optionPanelAlignment.set(IGameOptions.RIGHT_ALIGN);
					forceUpdate(true);
				}
				return;
		}
	}
	@Override public void mouseEntered(MouseEvent e)	{
		//super.mouseEntered(e);
		setModifierKeysState(e);
		repaint();
	}
	@Override public void mouseMoved(MouseEvent e)		{
		if (e != null) { // to select again a box that have been changed by reloadUI
			mX = e.getX();
			mY = e.getY();
		}
		if (hoverBox != null && hoverBox.contains(mX,mY)) {
			hoverChanged = false;
			return;
		}
		prevHover = hoverBox;
		hoverBox = null;
		hoverChanged = true;
		for (ModText bt : btListBoth)
			if (bt.box() == prevHover) {
				repaint();
				break;
			}
		for (Box box : boxBaseList) {
			if (box.checkIfHovered(descBox))
				break;
		}
		if (prevHover != null) {
			prevHover.mouseExit();
			if (hoverBox == null)
				descBox.setText("");
			loadGuide();
			repaint();
		}
	}
	@Override public void mouseReleased(MouseEvent e)	{
		if (e.getButton() > 3)
			return;
		if (hoverBox == null)
			return;
		mouseCommon(e, null);
		if (hoverBox == exitBox) {
			doExitBoxAction();
			if (parentUI!=null)
				if (parentUI instanceof GovernorOptionsPanel)
					EmpireColonySpendingPane.governorOptions();
				else if (parentUI instanceof BaseModPanel)
					((BaseModPanel) parentUI).mouseMoved(e);
			return;
		}
		else if (hoverBox == defaultBox)
			doDefaultBoxAction();
		else if (hoverBox == userBox)
			doUserBoxAction();
		else if (hoverBox == guideBox)
			doGuideBoxAction();
		else if (hoverBox == lastBox)
			doLastBoxAction();
	}
	@Override public void mouseWheelMoved(MouseWheelEvent e) { mouseCommon(null, e); }
}
