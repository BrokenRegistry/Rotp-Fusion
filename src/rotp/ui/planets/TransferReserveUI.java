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
package rotp.ui.planets;

import static rotp.ui.game.AdvisorPanel.isAdvising;
import static rotp.ui.game.IAdvisor.ADVISOR;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

import rotp.model.colony.Colony;
import rotp.model.colony.Colony.ColonyBudget;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireBudget;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.IGovOptions;
import rotp.model.game.IMapOptions;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.game.AdvisorPanel;
import rotp.ui.game.HelpUI;
import rotp.ui.game.IAdvisor;
import rotp.ui.main.SystemPanel;
import rotp.util.AdviceBox;

final class TransferReserveUI extends BasePanel implements MouseListener, MouseWheelListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private static final Color backgroundHaze = new Color(0,0,0,160);
    private static final Color yellowText = new Color(255,240,78);
    private static final Color backC = new Color(112,85,68);
    private static final Color borderC = new Color(112,85,68,128);
    private static final Color okButtonBdrC = new Color(158,165,156);
    private static final Color cancelButtonBdrC = new Color(184,165,143);
    private static final Color sliderButtonColor = Color.black;
	private static final Color sliderBoxBlueC	= new Color(34, 140, 142);
	private static final int MAX_TICKS	= 50;
	private static final int LEFT		= 0;
	private static final int CENTER		= 1;
	private static final int RIGHT		= 2;
	private static final String ALL		= "PLANETS_BUTTON_ALL_";
	private static final String LIST	= "PLANETS_BUTTON_LIST_";
	private static final String CLEAR	= "CLEAR_";
	private static final String DO		= "DO_";
	private static final String GRANT	= "GRANT";
	private static final String BOTH	= "BOTH";
	private static final String RAISE	= "RAISE";

	private final Paint[] clearPaint	= new Paint[3];
	private final Paint[] doPaint		= new Paint[3];
	private final Paint[] bottomPaint	= new Paint[3];
	private final Paint[] widePaint		= new Paint[2];

	private List<StarSystem> targetSystems;

	private Shape hoverBox, prevHover;
	private boolean initted = false;

	private final ActionButton optimalBudgetButton, optimalShareButton, convertButton, governorButton;
	private final ActionButton transfertButton, cancelButton, budgetButton;
	private final ActionButton redoAllGrantButton, redoAllBudgetButton, redoAllRaiseButton;
	private final ActionButton clearAllGrantButton, clearAllBudgetButton, clearAllRaiseButton;
	private final ActionButton listGrantButton, listBudgetButton, listRaiseButton;
	private final ActionButton listClearGrantButton, listClearBudgetButton, listClearRaiseButton;

	private final AdviceCheckbox toResearchCheckbox		= new AdviceCheckbox();
	private final AdviceCheckbox governorGrantCheckbox	= new AdviceCheckbox();
	private final AdviceCheckbox governorRaiseCheckbox	= new AdviceCheckbox();
	private final AdviceCheckbox canUpdateGrantCheckbox	= new AdviceCheckbox();
	private final AdviceCheckbox canUpdateRaiseCheckbox	= new AdviceCheckbox();
	private final AdviceCheckbox oldWayCheckbox			= new AdviceCheckbox();
	private final AdviceBox reserveSlider	= new AdviceBox();
	private final Polygon leftArrow		= new Polygon();
	private final Polygon rightArrow	= new Polygon();
	private final int checkW = s12;

	private int boxAreaL, boxAreaW;
	private int amt = 0;

    // polygon coordinates for left & right increment buttons
    private final int leftButtonX[] = new int[3];
    private final int leftButtonY[] = new int[3];
    private final int rightButtonX[] = new int[3];
    private final int rightButtonY[] = new int[3];
	private Shape textureClip;

	private Empire empire;
	private Colony colony;
	private ColonyBudget budget;
	private boolean emptyList, isPlayer, isGovernor, isMultiple;
	private int lineH, vSep = s20;
	private int button3W, button3H;
	private int[] button3X = new int[3];
	private int[] button2X = new int[2];
	private int button2W, button2H;
	private int boxWidth, boxHeight, boxX, boxY, margins;
	private int infoWidth, infoLeft, infoTop;	// Popup Box
	private float neededRsv, playerRsv, subsidies;
	private boolean showHelp = false;
	private HelpUI helpUI;
	private PlanetsUI parent;

	public TransferReserveUI(PlanetsUI p) {
		parent = p;
		optimalBudgetButton	= new ActionButton(this, "PLANETS_BUTTON_BUDGET_WISH", widePaint, 0, okButtonBdrC, false);
		optimalShareButton	= new ActionButton(this, "PLANETS_BUTTON_SHARE_WISH", widePaint, 1, okButtonBdrC, false);
		convertButton		= new ActionButton(this, "PLANETS_BUTTON_CONVERT", widePaint, 0, okButtonBdrC, false);
		governorButton		= new ActionButton(this, "PLANETS_BUTTON_REMOVE", widePaint, 1, okButtonBdrC, true);

		transfertButton		= new ActionButton(this, "PLANETS_TRANSFER_ACCEPT", bottomPaint, LEFT, okButtonBdrC, false);
		cancelButton		= new ActionButton(this, "PLANETS_TRANSFER_CANCEL", bottomPaint, CENTER, cancelButtonBdrC, false);
		budgetButton		= new ActionButton(this, "PLANETS_BUTTON_BUDGET", bottomPaint, RIGHT, okButtonBdrC, false);

		redoAllGrantButton	= new ActionButton(this, ALL+DO+GRANT, doPaint, LEFT, okButtonBdrC, false);
		redoAllBudgetButton	= new ActionButton(this, ALL+DO+BOTH, doPaint, CENTER, okButtonBdrC, true);
		redoAllRaiseButton	= new ActionButton(this, ALL+DO+RAISE, doPaint, RIGHT, okButtonBdrC, true);
		clearAllGrantButton	= new ActionButton(this, ALL+CLEAR+GRANT, clearPaint, LEFT, okButtonBdrC, false);
		clearAllBudgetButton= new ActionButton(this, ALL+CLEAR+BOTH, clearPaint, CENTER, okButtonBdrC, false);
		clearAllRaiseButton	= new ActionButton(this, ALL+CLEAR+RAISE, clearPaint, RIGHT, okButtonBdrC, false);

		listGrantButton		= new ActionButton(this, LIST+DO+GRANT, doPaint, LEFT, okButtonBdrC, false);
		listBudgetButton	= new ActionButton(this, LIST+DO+BOTH, doPaint, CENTER, okButtonBdrC, true);
		listRaiseButton		= new ActionButton(this, LIST+DO+RAISE, doPaint, RIGHT, okButtonBdrC, true);
		listClearGrantButton= new ActionButton(this, LIST+CLEAR+GRANT, clearPaint, LEFT, okButtonBdrC, false);
		listClearBudgetButton= new ActionButton(this, LIST+CLEAR+BOTH, clearPaint, CENTER, okButtonBdrC, false);
		listClearRaiseButton= new ActionButton(this, LIST+CLEAR+RAISE, clearPaint, RIGHT, okButtonBdrC, false);
		initModel();
	}
    private void initModel() {
        setOpaque(false);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
	}
	void targetSystems(List<StarSystem> syslist) {
		initted = false;	// in case of language change
		targetSystems = syslist;
		emptyList = syslist.isEmpty();
		if(!emptyList) {
			colony = syslist.get(0).colony();
			setDefaultAmt();
			isMultiple = syslist.size() > 1;
		}
		initAdvisor();
	}
	private Font descriptionFont()	{ return narrowFont(20); }
	@Override protected void initAdvisor()	{
		ADVISOR.init(this, IAdvisor.BUDGET_ADVISOR, empire);
		ADVISOR.setMargins(s3, s3, 0, s50, s50);
	}
	private void init()	{
		// Horizontal parameters
		int w = getWidth();
		int buttonsHorSep = s20;
		margins		= s30;
		boxWidth	= scaled(560);
		boxX		= (w - boxWidth)/2;
		infoLeft	= boxX + margins;
		infoWidth	= boxWidth - margins - margins;
		button3W	= (infoWidth - buttonsHorSep - buttonsHorSep)/3;
		button2W	= (infoWidth - buttonsHorSep)/2;

		button3X[0] = infoLeft;
		button3X[1] = infoLeft + button3W + buttonsHorSep;
		button3X[2] = button3X[1] + button3W + buttonsHorSep;
		button2X[0] = infoLeft;
		button2X[1] = infoLeft + button2W + buttonsHorSep;

		// colors
		Color greenEdgeC	= new Color(44, 59, 30);
		Color greenMidC		= new Color(71, 93, 48);
		Color[] greenColors	= {greenEdgeC, greenEdgeC, greenMidC, greenEdgeC, greenEdgeC };

		Color clearEdgeC	= new Color(70, 14, 14);
		Color clearMidC		= new Color(110, 22, 22);
		Color[] clearColors	= {clearEdgeC, clearEdgeC, clearMidC, clearEdgeC, clearEdgeC };

		Color redEdgeC		= new Color(100,70,50);
		Color redMidC		= new Color(161,110,76);
		Color[] redColors	= {redEdgeC, redEdgeC, redMidC, redEdgeC, redEdgeC };
		float[] fract		= {0.0f, 0.2f, 0.5f, 0.8f, 1.0f};

		int id = LEFT;
		Point2D start = new Point2D.Float(button3X[id], 0);
		Point2D end   = new Point2D.Float(button3X[id] + button3W, 0);
		Paint greenBackLeftGrad = new LinearGradientPaint(start, end, fract, greenColors);
		Paint clearBackLeftGrad = new LinearGradientPaint(start, end, fract, clearColors);

		id = CENTER;
		start = new Point2D.Float(button3X[id], 0);
		end   = new Point2D.Float(button3X[id] + button3W, 0);
		Paint largeRedBackMidGrad = new LinearGradientPaint(start, end, fract, redColors);
		Paint greenBackMidGrad = new LinearGradientPaint(start, end, fract, greenColors);
		Paint clearBackMidGrad = new LinearGradientPaint(start, end, fract, clearColors);

		id = RIGHT;
		start = new Point2D.Float(button3X[id], 0);
		end   = new Point2D.Float(button3X[id] + button3W, 0);
		Paint greenBackRightGrad = new LinearGradientPaint(start, end, fract, greenColors);
		Paint clearBackRightGrad = new LinearGradientPaint(start, end, fract, clearColors);

		doPaint[LEFT]	= greenBackLeftGrad;
		doPaint[CENTER]	= greenBackMidGrad;
		doPaint[RIGHT]	= greenBackRightGrad;
		clearPaint[LEFT]	= clearBackLeftGrad;
		clearPaint[CENTER]	= clearBackMidGrad;
		clearPaint[RIGHT]	= clearBackRightGrad;
		bottomPaint[LEFT]	= greenBackLeftGrad;
		bottomPaint[CENTER]	= largeRedBackMidGrad;
		bottomPaint[RIGHT]	= greenBackRightGrad;
		widePaint[0] = new Color(0, 0, 0, 0);
		widePaint[1] = new Color(0, 0, 0, 0);

		// Vertical Parameters
		int h = getHeight();
		button3H = s32;
		button2H = s27;
		lineH	= s20;

		toResearchCheckbox.init(checkW, button2W, vSep, IMapOptions.divertExcessToResearch);
		governorGrantCheckbox.init(checkW, button2W, vSep, IGovOptions.governorGrantFunds);
		governorRaiseCheckbox.init(checkW, button2W, vSep, IGovOptions.governorRaiseFunds);
		canUpdateGrantCheckbox.init(checkW, button2W, vSep, IGovOptions.redoBudgetGrantAllowed);
		canUpdateRaiseCheckbox.init(checkW, button2W, vSep, IGovOptions.redoBudgetRaiseAllowed);
		oldWayCheckbox.init(checkW, button2W, vSep, IGovOptions.autospendImmediateTransfer);

		toResearchCheckbox.setPane(this);
		governorGrantCheckbox.setPane(this);
		governorRaiseCheckbox.setPane(this);
		canUpdateGrantCheckbox.setPane(this);
		canUpdateRaiseCheckbox.setPane(this);
		oldWayCheckbox.setPane(this);

		reserveSlider.init(this, null, null, "PLANETS_TRANSFER_DESC_HELP");

		// Draw in a fake image to get the real height.
		BufferedImage img = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) img.getGraphics();

		infoTop = 0;
		Point pos = new Point(infoLeft, infoTop + lineH/2);
		pos = drawBox(g, pos);
		g.dispose();

		boxHeight = pos.y + s30;
		boxY = (h - boxHeight)/2;
		infoTop = boxY + s25;

		initted = true;
	}
	private void setDefaultAmt() {
		empire = player();
		budget = colony.budget();
		isPlayer = budget.isPlayerBudget();
		isGovernor = budget.governorBudgetBC() != null;
		neededRsv = budget.reserveNeededBC();
		subsidies = budget.budgetSubsidiesBC();
		playerRsv = empire.budget().unusedPlayerReserves();
		if (isPlayer)
			playerRsv += subsidies;
		int ticks = (int) Math.ceil(MAX_TICKS * neededRsv/playerRsv);
		setAmt(ticks);
	}
	void clear()	{
		targetSystems = null;
		empire = null;
		colony = null;
		budget = null;
	}
	@Override public String textureName()	{ return TEXTURE_BROWN; }
	@Override public Shape textureClip()	{ return textureClip; }
	@Override public void paintComponent(Graphics g0)	{
		if (emptyList) {
			exit();
			return;
		}
		super.paintComponent(g0);
		if (!initted)
			init();

		Graphics2D g = (Graphics2D) g0;
		drawBackGroundAndBox(g);

		Point pos = new Point(infoLeft, infoTop + lineH/2);
		pos = drawBox(g, pos);

		if (showHelp)
			helpUI().paintComponent(g, false);
		else
			helpUI = null; // Safe to clear there.
	}
	@Override protected boolean isAdvised()	{ return true; }
	private Point drawBox(Graphics2D g, Point pos) {
		String description = text("PLANETS_BUTTON_DO_CLEAR_DESC");
		g.setFont(descriptionFont());
		List<String> lines = wrappedLines(g, description, infoWidth);
		for (String line : lines) {
			int sw = g.getFontMetrics().stringWidth(line);
			int infoX = boxX + margins + (infoWidth - sw)/2;
			drawShadowedString(g, line, 3, infoX, pos.y, SystemPanel.textShadowC, SystemPanel.whiteText);
			pos.y += lineH;
		}

		boolean disabled = player().divertColonyExcessToResearch();
		pos = drawButtonLine(g, pos, ALL, disabled, clearAllRaiseButton, clearAllBudgetButton, clearAllGrantButton);
		pos = drawButtonLine(g, pos, null, disabled, redoAllRaiseButton, redoAllBudgetButton, redoAllGrantButton);
		pos = drawButtonLine(g, pos, LIST, disabled, listClearRaiseButton, listClearBudgetButton, listClearGrantButton);
		pos = drawButtonLine(g, pos, null, disabled, listRaiseButton, listBudgetButton, listGrantButton);

		pos = drawCheckboxes(g, pos);	

		pos = drawTitle(g, pos);

		pos = drawSliderAndArrowButtons(g, pos);

		pos = drawInfo(g, pos);

		disabled = !(isPlayer || isMultiple);
		pos = drawButtonLine(g, pos, null, disabled, optimalBudgetButton, optimalShareButton);
		pos = drawButtonLine(g, pos, null, disabled, convertButton, governorButton);
		pos.y += s5;

		pos = drawButtonLine(g, pos, null, false, transfertButton, cancelButton, budgetButton);

		return pos;
	}
	private void drawBackGroundAndBox(Graphics2D g)	{
		// draw background "haze"
		g.setColor(backgroundHaze);
		int w = getWidth();
		int h = getHeight();
		g.fillRect(0, 0, w, h);

		// draw box
		g.setColor(borderC);
		g.fillRect(boxX, boxY, boxWidth, boxHeight);
		g.setColor(backC);
		g.fillRect(boxX+s15, boxY+s15, boxWidth-s30, boxHeight-s30);
		textureClip = new Rectangle(boxX+s15, boxY+s15, boxWidth-s30, boxHeight-s30);
	}
	private Point drawButtonLine(Graphics2D g, Point pos, String label, boolean disabled, ActionButton... buttons)	{
		boolean hasDescription = label != null;
		int buttonH = button3H;
		int buttonW = button3W;
		int dy = s10;
		int arc = s2;
		int fontSize = 20;
		int[] buttonsX = button3X;
		if (buttons.length == 2) {
			buttonH = button2H;
			buttonW = button2W;
			dy = s8;
			arc = s15;
			fontSize = 18;
			buttonsX = button2X;
		}

		// Descriptions
		if (hasDescription) {
			pos.y += lineH/2;
			String description = text(label + "DESC");
			g.setFont(descriptionFont());
			List<String> lines = wrappedLines(g, description, infoWidth);
			for (String line : lines) {
				drawShadowedString(g, line, 3, infoLeft, pos.y, SystemPanel.textShadowC, SystemPanel.whiteText);
				pos.y += lineH;
			}
		}
		pos.y -= lineH/2;

		g.setFont(narrowFont(fontSize));
		for (ActionButton button : buttons) {
			int id = button.posId;
			int buttonX = buttonsX[id];
			button.setBounds(buttonX, pos.y, buttonW, buttonH);
			g.setColor(SystemPanel.textShadowC);
			g.fillRoundRect(buttonX+s4, pos.y+s4, buttonW, buttonH, arc, arc);
			g.setPaint(button.paint[id]);
			g.fillRoundRect(buttonX, pos.y, buttonW, buttonH, arc, arc);
			Stroke prev = g.getStroke();
			g.setStroke(stroke1);
			if (hoverBox == button)
				g.setColor(Color.YELLOW);
			else
				g.setColor(button.borderColor);
			g.drawRoundRect(buttonX, pos.y, buttonW, buttonH, arc, arc);
			g.setStroke(prev);
			Color c2 = hoverBox == button ? Color.YELLOW : SystemPanel.whiteText;
			c2 = disabled && button.canBeDisabled? Color.GRAY : c2;
			String str = text(button.getLabelKey());
			int sw = g.getFontMetrics().stringWidth(str);
			int strX = buttonX + ((buttonW - sw) / 2);
			drawShadowedString(g, str, 3, strX, pos.y+buttonH-dy, SystemPanel.textShadowC, c2);
		}
		pos.y = pos.y + buttonH + vSep;
		return pos;
	}
	private Point drawCheckboxes(Graphics2D g, Point pos)	{
		// Draw line
		g.setColor(SystemPanel.blackText);
		g.drawLine(infoLeft, pos.y, infoLeft + infoWidth, pos.y);
		pos.y += vSep;
		Point posRight = new Point(pos.x + infoWidth/2, pos.y);

		// Divert excess to research
		toResearchCheckbox.setLocation(pos.x, pos.y-checkW);
		toResearchCheckbox.drawCheckbox(g);
		// Old way of immediate transfer
		oldWayCheckbox.setLocation(posRight.x, posRight.y-checkW);
		oldWayCheckbox.drawCheckbox(g);

		pos.y += vSep;
		posRight.y = pos.y;
		// governor Grant Funds
		governorGrantCheckbox.setLocation(pos.x, pos.y-checkW);
		governorGrantCheckbox.drawCheckbox(g);
		// governor Raise Funds
		governorRaiseCheckbox.setLocation(posRight.x, posRight.y-checkW);
		governorRaiseCheckbox.drawCheckbox(g);

		pos.y += vSep;
		posRight.y = pos.y;
		// redo Budget Grant Allowed
		canUpdateGrantCheckbox.setLocation(pos.x, pos.y-checkW);
		canUpdateGrantCheckbox.drawCheckbox(g);
		// redo Budget Raise Allowed
		canUpdateRaiseCheckbox.setLocation(posRight.x, posRight.y-checkW);
		canUpdateRaiseCheckbox.drawCheckbox(g);

		// Draw line
		pos.y += vSep/2;
		g.setColor(SystemPanel.blackText);
		g.drawLine(infoLeft, pos.y, infoLeft + infoWidth, pos.y);

		pos.y += vSep;
		return pos;
	}
	private Point drawTitle(Graphics2D g, Point pos)	{
		String title;	
		if(isMultiple)
			title = text("PLANETS_TRANSFER_DESC", targetSystems.size()) + " " + text("SYSTEMS_TITLE");
		else
			title = text("PLANETS_TRANSFER_DESC", colony.name());

		g.setFont(narrowFont(24));
		int titleSW = g.getFontMetrics().stringWidth(title);

		int xTitle = boxX + (boxWidth - titleSW)/2;
		g.setColor(yellowText);
		drawShadowedString(g, title, 3, xTitle, pos.y + vSep, SystemPanel.textShadowC, SystemPanel.whiteText);
		pos.y += vSep + vSep;
		return pos;
	}
	private Point drawSliderAndArrowButtons(Graphics2D g, Point pos)	{
		int arrowLeftM = infoLeft;
		int arrowRightM = boxX + boxWidth - s100;
		int arrowW = s8;
		int arrowH = s18;

		leftButtonX[0] = arrowLeftM;
		leftButtonX[1] = arrowLeftM + arrowW;
		leftButtonX[2] = arrowLeftM + arrowW;

		leftButtonY[0] = pos.y + arrowH/2;
		leftButtonY[1] = pos.y;
		leftButtonY[2] = pos.y + arrowH;

		rightButtonX[0] = arrowRightM;
		rightButtonX[1] = arrowRightM - arrowW;
		rightButtonX[2] = arrowRightM - arrowW;

		rightButtonY[0] = pos.y + arrowH/2;
		rightButtonY[1] = pos.y;
		rightButtonY[2] = pos.y + arrowH;

		leftArrow.reset();
		rightArrow.reset();
		for (int i=0;i<3;i++) {
			leftArrow.addPoint(leftButtonX[i], leftButtonY[i]);
			rightArrow.addPoint(rightButtonX[i], rightButtonY[i]);
		}

		if (hoverBox == leftArrow)
			g.setColor(Color.yellow);
		else
			g.setColor(sliderButtonColor);
		g.fillPolygon(leftButtonX, leftButtonY, 3);
		if (hoverBox == rightArrow)
			g.setColor(Color.yellow);
		else
			g.setColor(sliderButtonColor);
		g.fillPolygon(rightButtonX, rightButtonY, 3);

		// slider box
		int boxL = arrowLeftM+arrowW+s4;
		int boxR = arrowRightM-arrowW-s4;
		int boxW = boxR - boxL;
		int boxH = arrowH;
		int boxBorderW = s2;

		g.setColor(Color.black);
		g.fillRect(boxL, pos.y, boxW, boxH);
		g.setColor(sliderBoxBlueC);
		g.fillRect(boxL, pos.y+s1, boxW * amt / MAX_TICKS, boxH-s2);

		if (hoverBox == reserveSlider) {
			g.setColor(Color.yellow);
			Stroke prev = g.getStroke();
			g.setStroke(stroke2);
			g.drawRect(boxL, pos.y, boxW, boxH);
			g.setStroke(prev);
		}

		boxAreaL = boxL+boxBorderW;
		boxAreaW = boxW-boxBorderW-boxBorderW;
		reserveSlider.setBounds(boxAreaL, pos.y, boxAreaW, boxH);

		// amount string
		g.setFont(narrowFont(20));
		int transferAmt = (int) (playerRsv * amt / MAX_TICKS);
		String amtString = text("PLANETS_AMT_BC", transferAmt);
		int sw = g.getFontMetrics().stringWidth(amtString);
		int amtX = boxX+boxWidth-s40-sw;
		g.setColor(SystemPanel.blackText);
		drawString(g, amtString, amtX, pos.y + boxH - s3);

		pos.y += arrowH + vSep;
		return pos;
	}
	private Point drawInfo(Graphics2D g, Point pos)	{
		// Info about current choice
		String currentChoice;
		if (isPlayer)
			currentChoice = text("PLANETS_BUDGET_CURRENT_PLAYER", fmt(ceil(budget.playerBudgetBC())), "");
		else if (isGovernor)
			currentChoice = text("PLANETS_BUDGET_GOVERNOR_ALONE", fmt(budget.governorBudgetBC()), "");
		else
			currentChoice = text("PLANETS_BUDGET_CURRENT_NONE");

		g.setFont(narrowFont(20));
		int sw = g.getFontMetrics().stringWidth(currentChoice);
		if (sw > infoWidth) {
			scaledFont(g, currentChoice, infoWidth, 19, 15);
			sw = g.getFontMetrics().stringWidth(currentChoice);
		}
		int infoX = boxX + (infoWidth - sw)/2;
		g.setColor(SystemPanel.blackText);
		drawString(g, currentChoice, infoX, pos.y);
		pos.y += lineH;

		// Info about potential governor choice
		if (isPlayer && !isMultiple) {
			String govChoice;
			if (isGovernor)
					govChoice = text("PLANETS_BUDGET_GOVERNOR_SUGGEST", fmt(budget.governorBudgetBC()), "");
				else
					govChoice = text("PLANETS_BUDGET_NO_GOVERNOR");
			g.setFont(narrowFont(16));
			sw = g.getFontMetrics().stringWidth(govChoice);
			if (sw > infoWidth) {
				scaledFont(g, govChoice, infoWidth, 15, 12);
				sw = g.getFontMetrics().stringWidth(govChoice);
			}
			infoX = boxX + (infoWidth - sw)/2;
			g.setColor(SystemPanel.blackText);
			drawString(g, govChoice, infoX, pos.y);
			pos.y += lineH;
		}
		pos.y += vSep;
		return pos;
	}
	private HelpUI helpUI()	{
		if (helpUI == null)
			buildHelp();
		return helpUI;
	}
	private void buildHelp()	{
		helpUI = RotPUI.helpUI();
		helpUI.clear();
		int height = getHeight();
		int bottom = height - s25;
		int bw = scaled(300);
		int xSep = s50;

		int tx = cancelButton.xc();
		int ty = cancelButton.ye()-s2;
		int w = bw;
		int x = tx - bw/2;
		int y = -bottom;
		HelpUI.HelpSpec spC1 = helpUI.addBrownHelpText(x, y, w, 0, cancelButton.getDescriptionText());
		spC1.setLine(spC1.xc(), spC1.y(), tx, ty);

		// LEFT SIDE
		int ySep = s10;
		tx = transfertButton.x+s5;
		ty = transfertButton.yc();
		w = bw;
		x = tx - bw - xSep;
		y = -bottom;
		HelpUI.HelpSpec spL1 = helpUI.addBrownHelpText(x, y, w, 0, transfertButton.getDescriptionText());
		spL1.setLine(spL1.xe(), spL1.yc(), tx, ty);

		ty = convertButton.yc();
		w = bw;
		y = ySep-spL1.y();
		HelpUI.HelpSpec spL2 = helpUI.addBrownHelpText(x, y, w, 0, convertButton.getDescriptionText());
		spL2.setLine(spL2.xe(), spL2.yc(), tx, ty);

		ty = optimalBudgetButton.yc();
		w = bw;
		y = ySep-spL2.y();
		HelpUI.HelpSpec spL3 = helpUI.addBrownHelpText(x, y, w, 0, optimalBudgetButton.getDescriptionText());
		spL3.setLine(spL3.xe(), spL3.yc(), tx, ty);

		tx = canUpdateGrantCheckbox.x - s5;
		ty = canUpdateGrantCheckbox.yc() + s3;
		w = bw;
		y = ySep-spL3.y();
		HelpUI.HelpSpec spL4 = helpUI.addBrownHelpText(x, y, w, 0, IGovOptions.redoBudgetGrantAllowed.getDescription());
		spL4.setLine(spL4.xe(), spL4.yc(), tx, ty);

		tx = governorGrantCheckbox.x - s5;
		ty = governorGrantCheckbox.yc();
		w = bw;
		y = ySep-spL4.y();
		HelpUI.HelpSpec spL5 = helpUI.addBrownHelpText(x, y, w, 0, IGovOptions.governorGrantFunds.getDescription());
		spL5.setLine(spL5.xe(), spL5.yc(), tx, ty);

		tx = toResearchCheckbox.x - s5;
		ty = toResearchCheckbox.yc() - s3;
		w = bw;
		y = ySep-spL5.y();
		HelpUI.HelpSpec spL6 = helpUI.addBrownHelpText(x, y, w, 0, IMapOptions.divertExcessToResearch.getDescription());
		spL6.setLine(spL6.xe(), spL6.yc(), tx, ty);

		tx = oldWayCheckbox.x - s5;
		ty = oldWayCheckbox.yc() - s3;
		y = ySep-spL6.y();
		HelpUI.HelpSpec spL7 = helpUI.addBrownHelpText(x, y, w, 0, IGovOptions.autospendImmediateTransfer.getDescription());
		spL7.setLine(spL7.xe(), spL7.yc(), tx, ty);

		// RIGHT SIDE
		ySep = s10;
		tx = budgetButton.xe() - s5;
		ty = budgetButton.yc();
		x = tx + xSep;
		y = -bottom;
		HelpUI.HelpSpec spR1 = helpUI.addBrownHelpText(x, y, w, 0, budgetButton.getDescriptionText());
		spR1.setLine(spR1.x(), spR1.yc(), tx, ty);

		ty = governorButton.yc();
		y = ySep-spR1.y();
		HelpUI.HelpSpec spR2 = helpUI.addBrownHelpText(x, y, w, 0, governorButton.getDescriptionText());
		spR2.setLine(spR2.x(), spR2.yc(), tx, ty);

		ty = optimalShareButton.yc();
		y = ySep-spR2.y();
		HelpUI.HelpSpec spR3 = helpUI.addBrownHelpText(x, y, w, 0, optimalShareButton.getDescriptionText());
		spR3.setLine(spR3.x(), spR3.yc(), tx, ty);

		tx = canUpdateRaiseCheckbox.getTextRightX() + s5;
		ty = canUpdateRaiseCheckbox.yc() + s3;
		y = ySep-spR3.y();
		HelpUI.HelpSpec spR4 = helpUI.addBrownHelpText(x, y, w, 0, canUpdateRaiseCheckbox.getParam().getDescription());
		spR4.setLine(spR4.x(), spR4.yc(), tx, ty);

		tx = governorRaiseCheckbox.getTextRightX() + s5;
		ty = governorRaiseCheckbox.yc() - s2;
		y = ySep-spR4.y();
		HelpUI.HelpSpec spR5 = helpUI.addBrownHelpText(x, y, w, 0, governorRaiseCheckbox.getParam().headerHelp(false));
		spR5.setLine(spR5.x(), spR5.yce(), tx, ty);
	}
    private void increment()   { setAmt(amt+1); }
    private void decrement()   { setAmt(amt-1); }
    private void setAmt(int i) { amt = bounds(0, i, MAX_TICKS); }
	private void setHoverSprite(int x, int y) {
		if (isAdvising() && ADVISOR.getExitBox().isSelectableAt(null, x, y))
			hoverBox = hoverBox(AdvisorPanel.getButtonBox(), hoverBox);
		else if (cancelButton.isSelectableAt(x, y))
			hoverBox = hoverBox(cancelButton, hoverBox);
		else if (transfertButton.isSelectableAt(x, y))
			hoverBox = hoverBox(transfertButton, hoverBox);
		else if (leftArrow.contains(x,y))
			hoverBox = hoverBox(leftArrow, hoverBox);
		else if (rightArrow.contains(x,y))
			hoverBox = hoverBox(rightArrow, hoverBox);
		else if (reserveSlider.isSelectableAt(x,y))
			hoverBox = hoverBox(reserveSlider, hoverBox);
		else if (budgetButton.isSelectableAt(x,y))
			hoverBox = hoverBox(budgetButton, hoverBox);
		else if (governorButton.isSelectableAt(x,y) && (isPlayer || isMultiple))
			hoverBox = hoverBox(governorButton, hoverBox);
		else if (optimalShareButton.isSelectableAt(x,y))
			hoverBox = hoverBox(optimalShareButton, hoverBox);
		else if (optimalBudgetButton.isSelectableAt(x,y))
			hoverBox = hoverBox(optimalBudgetButton, hoverBox);
		else if (convertButton.isSelectableAt(x,y))
			hoverBox = hoverBox(convertButton, hoverBox);
		else if (toResearchCheckbox.isSelectableAt(x,y))
			hoverBox = hoverBox(toResearchCheckbox, hoverBox);
		else if (oldWayCheckbox.isSelectableAt(x,y))
			hoverBox = hoverBox(oldWayCheckbox, hoverBox);
		else if (governorGrantCheckbox.isSelectableAt(x,y))
			hoverBox = hoverBox(governorGrantCheckbox, hoverBox);
		else if (governorRaiseCheckbox.isSelectableAt(x,y))
			hoverBox = hoverBox(governorRaiseCheckbox, hoverBox);
		else if (canUpdateGrantCheckbox.isSelectableAt(x,y))
			hoverBox = hoverBox(canUpdateGrantCheckbox, hoverBox);
		else if (canUpdateRaiseCheckbox.isSelectableAt(x,y))
			hoverBox = hoverBox(canUpdateRaiseCheckbox, hoverBox);
		else if (redoAllGrantButton.isSelectableAt(x,y))
			hoverBox = hoverBox(redoAllGrantButton, hoverBox);
		else if (redoAllBudgetButton.isSelectableAt(x,y) && !options().divertColonyExcessToResearch())
			hoverBox = hoverBox(redoAllBudgetButton, hoverBox);
		else if (redoAllRaiseButton.isSelectableAt(x,y) && !options().divertColonyExcessToResearch())
			hoverBox = hoverBox(redoAllRaiseButton, hoverBox);
		else if (clearAllGrantButton.isSelectableAt(x,y))
			hoverBox = hoverBox(clearAllGrantButton, hoverBox);
		else if (clearAllBudgetButton.isSelectableAt(x,y))
			hoverBox = hoverBox(clearAllBudgetButton, hoverBox);
		else if (clearAllRaiseButton.isSelectableAt(x,y))
			hoverBox = hoverBox(clearAllRaiseButton, hoverBox);
		else if (listClearGrantButton.isSelectableAt(x,y))
			hoverBox = hoverBox(listClearGrantButton, hoverBox);
		else if (listClearBudgetButton.isSelectableAt(x,y))
			hoverBox = hoverBox(listClearBudgetButton, hoverBox);
		else if (listClearRaiseButton.isSelectableAt(x,y))
			hoverBox = hoverBox(listClearRaiseButton, hoverBox);
		else if (listGrantButton.isSelectableAt(x,y))
			hoverBox = hoverBox(listGrantButton, hoverBox);
		else if (listBudgetButton.isSelectableAt(x,y) && !options().divertColonyExcessToResearch())
			hoverBox = hoverBox(listBudgetButton, hoverBox);
		else if (listRaiseButton.isSelectableAt(x,y) && !options().divertColonyExcessToResearch())
			hoverBox = hoverBox(listRaiseButton, hoverBox);
		else
			hoverBox = hoverBox(null, hoverBox);
	}

	private void exit() {
		if (isAdvising())
			ADVISOR.onHold();
		parent.initAdvisor();
		hoverBox = null;
		showHelp = false;
		amt = 0;
		softClick();
		disableGlassPane();
	}
	private void redoGrantFundsButtonAction()	{
		softClick();
		player().budget().redoBudget(player().allColonizedSystems(), true, false, EmpireBudget.REDO, true);
		exit();
	}
	private void redoRaiseFundsButtonAction()	{
		softClick();
		player().budget().redoBudget(player().allColonizedSystems(), false, true, EmpireBudget.REDO, true);
		exit();
	}
	private void redoAllBudgetButtonAction()	{
		softClick();
		player().budget().redoBudget(player().allColonizedSystems(), true, true, EmpireBudget.REDO, true);
		exit();
	}
	private void clearGrantFundsButtonAction()	{
		softClick();
		player().budget().redoBudget(player().allColonizedSystems(), true, false, EmpireBudget.CLEAR, true);
		exit();
	}
	private void clearRaiseFundsButtonAction()	{
		softClick();
		player().budget().redoBudget(player().allColonizedSystems(), false, true, EmpireBudget.CLEAR, true);
		exit();
	}
	private void clearAllBudgetButtonAction()	{
		softClick();
		player().budget().redoBudget(player().allColonizedSystems(), true, true, EmpireBudget.CLEAR, true);
		exit();
	}
	private void listGrantFundsButtonAction()	{
		softClick();
		player().budget().redoBudget(targetSystems, true, false, EmpireBudget.LIST, true);
		exit();
	}
	private void listRaiseFundsButtonAction()	{
		softClick();
		player().budget().redoBudget(targetSystems, false, true, EmpireBudget.LIST, true);
		exit();
	}
	private void listAllBudgetButtonAction()	{
		softClick();
		player().budget().redoBudget(targetSystems, true, true, EmpireBudget.LIST, true);
		exit();
	}
	private void clearListGrantFundsButtonAction()	{
		softClick();
		player().budget().redoBudget(targetSystems, true, false, EmpireBudget.CLEAR, true);
		exit();
	}
	private void clearListRaiseFundsButtonAction()	{
		softClick();
		player().budget().redoBudget(targetSystems, false, true, EmpireBudget.CLEAR, true);
		exit();
	}
	private void clearListAllBudgetButtonAction()	{
		softClick();
		player().budget().redoBudget(targetSystems, true, true, EmpireBudget.CLEAR, true);
		exit();
	}
	private void transfertButtonAction()	{ // No changes
		softClick();
		float pct = (float) amt / MAX_TICKS;
		int amount = (int) (pct*player().totalReserve());
		for(StarSystem sys : targetSystems) {
			Colony col = sys.colony();
			empire.allocateReserve(col, amount);
			col.governIfNeeded();
			if(empire.totalReserve() == 0)
				break;
		}
		exit();
	}
	private void transfertBudgetButtonAction()	{
		softClick();
		boolean carryUnfunded = govOptions().autospendCarryUnfunded();
		for(StarSystem sys : targetSystems)
			sys.colony().budget().transfertBudget(carryUnfunded);
		empire.budget().makeBudgetColonyObsolete();
		empire.budget().refreshBudget();
		exit();
	}
	private void budgetButtonAction()	{
		softClick();
		int amount = (int) (playerRsv * amt / MAX_TICKS);
		for(StarSystem sys : targetSystems) {
			ColonyBudget cb = sys.colony().budget();
			if (cb.isPlayerBudget())
				playerRsv += cb.playerBudgetBC();
			int amt = min(amount, (int)playerRsv);
			cb.playerBudgetBC(amt);
			playerRsv -= amt;
			if(playerRsv < 1)
				break;
		}
		empire.budget().makeBudgetColonyObsolete();
		empire.budget().redoBudget(targetSystems, true, false, EmpireBudget.REDO, false);
		exit();
	}
	private void govButtonAction()	{
		softClick();
		for(StarSystem sys : targetSystems)
			sys.colony().budget().playerBudgetBC(null);
		empire.budget().makeBudgetColonyObsolete();
		empire.budget().redoBudget(targetSystems, true, false, EmpireBudget.REDO, false);
		exit();
	}
	private void wishShareButtonAction()	{
		softClick();
		int wishSum = 0;
		int onBudget = 0;
		// clear player budget
		for(StarSystem sys : targetSystems)
			sys.colony().budget().playerBudgetBC(null);
		empire.budget().makeBudgetColonyObsolete();
		empire.budget().redoBudget(targetSystems, true, false, EmpireBudget.LIST, false);
		setDefaultAmt();

		// Do as asked
		for(StarSystem sys : targetSystems) {
			ColonyBudget cb = sys.colony().budget();
			wishSum += cb.reserveNeededBC();
			if (cb.isPlayerBudget())
				onBudget += cb.playerBudgetBC();
		}
		float ratio = min(1, (playerRsv+onBudget)/wishSum);
		for(StarSystem sys : targetSystems)
			sys.colony().budget().budgetizeRatio(ratio);
		empire.budget().makeBudgetColonyObsolete();
		empire.budget().refreshBudget();

		// Grant funding
		player().budget().redoBudget(targetSystems, true, false, EmpireBudget.LIST, true);
		exit();
	}
	private void wishBudgetButtonAction()	{
		softClick();
		// clear player budget
		for(StarSystem sys : targetSystems)
			sys.colony().budget().playerBudgetBC(null);
		empire.budget().makeBudgetColonyObsolete();
		player().budget().redoBudget(targetSystems, true, false, EmpireBudget.LIST, true);
		setDefaultAmt();

		// Do as asked
		for(StarSystem sys : targetSystems)
			sys.colony().budget().budgetizeNeeded();
		empire.budget().makeBudgetColonyObsolete();
		empire.budget().refreshBudget();

		// Grant funding
		player().budget().redoBudget(targetSystems, true, false, EmpireBudget.LIST, true);
		exit();
	}
	@Override public Shape getHoverBox()	{ return hoverBox; }
	@Override public void cancelHelp()		{ if (isAdvising()) ADVISOR.advanceMap(); }
	@Override public void keyPressed(KeyEvent e)	{
		setModifierKeysState(e);
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				exit();
				return;
			case KeyEvent.VK_SPACE:
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_F1:
				if (e.isControlDown()) {
					showHelp = !showHelp;
					helpUI = null;
					repaint();
					return;
				}
				toggleOnDemandAdvisor();
				return;
			case KeyEvent.VK_E:
				if (e.isAltDown() && e.isControlDown()) {
					debugReloadLabels("en");
					repaint();
				}
				return;
			case KeyEvent.VK_F:
				if (e.isAltDown() && e.isControlDown()) {
					debugReloadLabels("fr");
					repaint();
				}
				return;
			case KeyEvent.VK_L:
				if (e.isAltDown()) {
					debugReloadLabels("");
					repaint();
				}
				break;
			}
	}
	@Override public void keyReleased(KeyEvent e)		{ setModifierKeysState(e); }
    @Override public void mouseClicked(MouseEvent arg0)	{ }
    @Override public void mouseEntered(MouseEvent arg0)	{ }
    @Override public void mouseExited(MouseEvent arg0)	{
        if (hoverBox != null) {
			if (hoverBox instanceof AdviceBox)
				((AdviceBox) hoverBox).hovering(false);
            hoverBox = null;
            repaint();
        }
    }
    @Override public void mousePressed(MouseEvent arg0)	{ }
    @Override public void mouseReleased(MouseEvent e)	{
        if (e.getButton() > 3)
            return;
        int prevAmt = amt;
        if (hoverBox == cancelButton) {
            exit();
            return;
        }
		else if (hoverBox == transfertButton) {
			transfertButtonAction();
			return;
		} else {
			if (isAdvising() && hoverBox == AdvisorPanel.getButtonBox()) {
				ADVISOR.advanceMap();
				repaint();
				return;
			}
			else if (hoverBox == leftArrow)
				decrement();
			else if (hoverBox == rightArrow)
				increment();
			else if (hoverBox == reserveSlider) 
				setAmt(MAX_TICKS*(e.getX()-boxAreaL)/boxAreaW);
			else if (hoverBox == budgetButton) {
				budgetButtonAction();
				return;
			}
			else if (hoverBox == governorButton) {
				govButtonAction();
				return;
			}
			else if (hoverBox == optimalShareButton) {
				wishShareButtonAction();
				return;
			}
			else if (hoverBox == optimalBudgetButton) {
				wishBudgetButtonAction();
				return;
			}
			else if (hoverBox == convertButton) {
				transfertBudgetButtonAction();
				return;
			}
			else if (hoverBox == toResearchCheckbox) {
				toResearchCheckbox.mouseReleased(e, true, true);
				return;
			}
			else if (hoverBox == oldWayCheckbox) {
				oldWayCheckbox.mouseReleased(e, true, true);
				return;
			}
			else if (hoverBox == governorGrantCheckbox) {
				governorGrantCheckbox.mouseReleased(e, true, true);
				return;
			}
			else if (hoverBox == governorRaiseCheckbox) {
				governorRaiseCheckbox.mouseReleased(e, true, true);
				return;
			}
			else if (hoverBox == canUpdateGrantCheckbox) {
				canUpdateGrantCheckbox.mouseReleased(e, true, true);
				return;
			}
			else if (hoverBox == canUpdateRaiseCheckbox) {
				canUpdateRaiseCheckbox.mouseReleased(e, true, true);
				return;
			}
			else if (hoverBox == redoAllGrantButton) {
				redoGrantFundsButtonAction();
				return;
			}
			else if (hoverBox == redoAllBudgetButton) {
				redoAllBudgetButtonAction();
				return;
			}
			else if (hoverBox == redoAllRaiseButton) {
				redoRaiseFundsButtonAction();
				return;
			}
			else if (hoverBox == clearAllGrantButton) {
				clearGrantFundsButtonAction();
				return;
			}
			else if (hoverBox == clearAllBudgetButton) {
				clearAllBudgetButtonAction();
				return;
			}
			else if (hoverBox == clearAllRaiseButton) {
				clearRaiseFundsButtonAction();
				return;
			}
			else if (hoverBox == listGrantButton) {
				listGrantFundsButtonAction();
				return;
			}
			else if (hoverBox == listBudgetButton) {
				listAllBudgetButtonAction();
				return;
			}
			else if (hoverBox == listRaiseButton) {
				listRaiseFundsButtonAction();
				return;
			}
			else if (hoverBox == listClearGrantButton) {
				clearListGrantFundsButtonAction();
				return;
			}
			else if (hoverBox == listClearBudgetButton) {
				clearListAllBudgetButtonAction();
				return;
			}
			else if (hoverBox == listClearRaiseButton) {
				clearListRaiseFundsButtonAction();
				return;
			}
		}

		if (amt != prevAmt) {
			softClick();
			repaint();
		}
		else if(showHelp) {
			softClick();
			showHelp = false;
			helpUI = null;
			repaint();
		}
		else
			misClick();
	}
	@Override  public void mouseDragged(MouseEvent e)	{
		prevHover = hoverBox;
        setHoverSprite(e.getX(),e.getY());

        if (prevHover != hoverBox)
            repaint();
    }
	@Override public void mouseMoved(MouseEvent e)	{
		prevHover = hoverBox;
        setHoverSprite(e.getX(),e.getY());

        if (prevHover != hoverBox)
            repaint();
    }
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int count = e.getUnitsToScroll();
        if (hoverBox == reserveSlider) {
            int prevAmt = amt;
            if (count < 0)
                increment();
            else if (count > 0)
                decrement();
            if (amt != prevAmt) 
                repaint();   
        }
		else if (hoverBox == toResearchCheckbox)
			toResearchCheckbox.mouseWheelMoved(e, true, true);
		else if (hoverBox == oldWayCheckbox)
			oldWayCheckbox.mouseWheelMoved(e, true, true);
		else if (hoverBox == governorGrantCheckbox)
			governorGrantCheckbox.mouseWheelMoved(e, true, true);
		else if (hoverBox == governorRaiseCheckbox)
			governorRaiseCheckbox.mouseWheelMoved(e, true, true);
		else if (hoverBox == canUpdateGrantCheckbox)
			canUpdateGrantCheckbox.mouseWheelMoved(e, true, true);
		else if (hoverBox == canUpdateRaiseCheckbox)
			canUpdateRaiseCheckbox.mouseWheelMoved(e, true, true);
	}
	private final class ActionButton extends AdviceBox {
		private static final long serialVersionUID = 1L;
		private final Paint[] paint;
		private final int posId;
		private final Color borderColor;
		private final boolean canBeDisabled;
		private ActionButton(BasePanel p, String label, Paint[] bg, int pos, Color border, boolean divert)	{
			super(p, label, true);
			this.paint = bg;
			this.posId = pos;
			borderColor = border;
			canBeDisabled = divert;
		}
	}
}
