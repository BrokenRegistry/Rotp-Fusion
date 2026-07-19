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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
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
import java.util.List;

import javax.swing.JEditorPane;

import rotp.model.colony.Colony;
import rotp.model.colony.Colony.ColonyBudget;
import rotp.model.empires.Empire;
import rotp.model.galaxy.StarSystem;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.game.HelpUI;
import rotp.ui.main.SystemPanel;

final class TransferReserveUI extends BasePanel implements MouseListener, MouseWheelListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private static final Color backgroundHaze = new Color(0,0,0,160);
    private static final Color yellowText = new Color(255,240,78);
    private static final Color backC = new Color(112,85,68);
    private static final Color borderC = new Color(112,85,68,128);
    private static final Color okButtonBdrC = new Color(158,165,156);
    private static final Color cancelButtonBdrC = new Color(184,165,143);

    private LinearGradientPaint largeRedBackMidGrad;
    private LinearGradientPaint largeGreenBackLeftGrad;
    private LinearGradientPaint largeGreenBackRightGrad;

    private static final Color sliderButtonColor = Color.black;
    //private static final Color sliderHighlightColor = new Color(255,255,255);
    private static final Color sliderBoxBlue = new Color(34,140,142);
	private static final int MAX_TICKS = 50;

    private List<StarSystem> targetSystems;

    private Shape hoverBox;
    private boolean initted = false;

	private final Rect optimalBudgetButton	= new Rect();
	private final Rect optimalShareButton	= new Rect();
	private final Rect cancelButton		= new Rect();
	private final Rect transfertButton	= new Rect();
	private final Rect governorButton	= new Rect();
	private final Rect budgetButton		= new Rect();
	private final Rect convertButton	= new Rect();
	private final Rect reserveSlider	= new Rect();
    private final Polygon leftArrow = new Polygon();
    private final Polygon rightArrow = new Polygon();

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
	private int h, w, xTitle, yTitle;
	private int boxWidth, boxHeight, xBox, yBox;	// Popup Box
	private float neededRsv, playerRsv, subsidies;
	private boolean showHelp = false;
	private JEditorPane helpEditor;
	private HelpUI helpUI;

    public TransferReserveUI() {
        initModel();
    }
    private void initModel() {
        setOpaque(false);
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
		helpEditor = new JEditorPane();
		helpEditor.setEditable(false);
		helpEditor.setContentType("text/html");
		add(helpEditor);
    }
	void targetSystems(List<StarSystem> syslist) {
		targetSystems = syslist;
		emptyList = syslist.isEmpty();
		if(!emptyList) {
			colony = syslist.get(0).colony();
			setDefaultAmt();
			isMultiple = syslist.size() > 1;
		}
	}
	private void setDefaultAmt() {
		empire = player();
		budget = colony.budget();
		isPlayer = budget.isPlayerBudget();
//		isGovernor = budget.isGovernorBudget();
		isGovernor = budget.budgetGovernorBC() != null;
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
    @Override
    public String textureName()     { return TEXTURE_BROWN; }
    @Override
    public Shape textureClip()     { return textureClip; }
	@Override public void paintComponent(Graphics g0)	{
		if (emptyList) {
			exit();
			return;
		}
		Graphics2D g = (Graphics2D) g0;
		super.paintComponent(g);
		w = getWidth();
		h = getHeight();

		drawTitleAndBackGround(g);
		drawSliderAndArrowButtons(g);
		drawInfo(g);
		drawBottomButtons(g);
		//System.out.println("showHelp: " + showHelp);
		if (showHelp)
			helpUI().paintComponent(g, false);
		else
			helpUI = null; // Safe to clear there.
	}
	private HelpUI helpUI()	{
		if (helpUI == null)
			buildHelp();
		return helpUI;
	}
	private void buildHelp()	{
		helpUI = RotPUI.helpUI();
		helpUI.clear();
		int bw = scaled(250);
		int xSep = s60;

		if (cancelButton.width > 0) {
			int tx = cancelButton.xc();
			int ty = cancelButton.ye();
			int w = bw;
			int x = tx - bw/2;
			int y = ty + s50;
			HelpUI.HelpSpec sp = helpUI.addBrownHelpText(x, y, w, 4, text("PLANETS_HELP_T_CANCEL"));
			sp.setLine(sp.xc(), sp.y(), tx, ty);
		}

		if (budgetButton.width > 0) {
			int tx = budgetButton.xe();
			int ty = budgetButton.yc();
			int w = bw;
			int x = tx + xSep;
			int y = ty + s20;
			HelpUI.HelpSpec sp = helpUI.addBrownHelpText(x, y, w, 4, text("PLANETS_HELP_T_BUDGET"));
			sp.setLine(sp.x(), sp.yc(), tx, ty);
		}
		if (governorButton.width > 0) {
			int tx = governorButton.xe();
			int ty = governorButton.yc();
			int w = bw;
			int x = tx + xSep;
			int y = ty - s45;
			HelpUI.HelpSpec sp = helpUI.addBrownHelpText(x, y, w, 4, text("PLANETS_HELP_T_REMOVE_PLAYER"));
			sp.setLine(sp.x(), sp.yc(), tx, ty);
		}
		if (optimalShareButton.width > 0) {
			int tx = optimalShareButton.xe();
			int ty = optimalShareButton.yc();
			int w = bw;
			int x = tx + xSep;
			int y = ty - s120;
			HelpUI.HelpSpec sp = helpUI.addBrownHelpText(x, y, w, 4, text("PLANETS_HELP_T_SHARE_OPTIMAL"));
			sp.setLine(sp.x(), sp.yc(), tx, ty);
		}

		if (transfertButton.width > 0) {
			int tx = transfertButton.x;
			int ty = transfertButton.yc();
			int w = bw;
			int x = tx - bw - xSep;
			int y = ty + s20;
			HelpUI.HelpSpec sp = helpUI.addBrownHelpText(x, y, w, 4, text("PLANETS_HELP_T_TRANSFERT"));
			sp.setLine(sp.xe(), sp.yc(), tx, ty);
		}
		if (convertButton.width > 0) {
			int tx = convertButton.x;
			int ty = convertButton.yc();
			int w = bw;
			int x = tx - bw - xSep;
			int y = ty - s45;
			HelpUI.HelpSpec sp = helpUI.addBrownHelpText(x, y, w, 4, text("PLANETS_HELP_T_CONVERT"));
			sp.setLine(sp.xe(), sp.yc(), tx, ty);
		}
		if (optimalBudgetButton.width > 0) {
			int tx = optimalBudgetButton.x;
			int ty = optimalBudgetButton.yc();
			int w = bw;
			int x = tx - bw - xSep;
			int y = ty - s120;
			HelpUI.HelpSpec sp = helpUI.addBrownHelpText(x, y, w, 4, text("PLANETS_HELP_T_BUDGET_OPTIMAL"));
			sp.setLine(sp.xe(), sp.yc(), tx, ty);
		}

	}
	private void drawTitleAndBackGround(Graphics2D g)	{
		// draw background "haze"
		g.setColor(backgroundHaze);
		g.fillRect(0, 0, w, h);

		// draw box
		boxWidth  = scaled(500);
		boxHeight = scaled(300);
		xBox = (w - boxWidth)/2;
		yBox = h/3;

		g.setColor(borderC);
		g.fillRect(xBox, yBox, boxWidth, boxHeight);
		g.setColor(backC);
		g.fillRect(xBox+s15, yBox+s15, boxWidth-s30, boxHeight-s30);

		textureClip = new Rectangle(xBox+s15, yBox+s15, boxWidth-s30, boxHeight-s30);

		// draw title
		String title;	
		if(isMultiple)
			title = text("PLANETS_TRANSFER_DESC", targetSystems.size()) + " " + text("SYSTEMS_TITLE");
		else
			title = text("PLANETS_TRANSFER_DESC", colony.name());

		g.setFont(narrowFont(24));
		int titleSW = g.getFontMetrics().stringWidth(title);

		xTitle = xBox + (boxWidth - titleSW)/2;
		yTitle = yBox + s45;
		g.setColor(yellowText);
		drawShadowedString(g, title, 3, xTitle, yTitle, SystemPanel.textShadowC, SystemPanel.whiteText);
	}
	private void drawSliderAndArrowButtons(Graphics2D g)	{
		int arrowLeftM = xBox+s30;
		int arrowRightM = xBox+boxWidth-s100;
		int arrowW = s8;
		int arrowTopY = yTitle+s15;
		int arrowH = s18;
		leftButtonX[0] = arrowLeftM; leftButtonX[1] = arrowLeftM+arrowW; leftButtonX[2] = arrowLeftM+arrowW;
		leftButtonY[0] = arrowTopY+(arrowH/2); leftButtonY[1] = arrowTopY; leftButtonY[2] = arrowTopY+arrowH;
		rightButtonX[0] = arrowRightM; rightButtonX[1] = arrowRightM-arrowW; rightButtonX[2] = arrowRightM-arrowW;
		rightButtonY[0] = arrowTopY+(arrowH/2); rightButtonY[1] = arrowTopY; rightButtonY[2] = arrowTopY+arrowH;
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
		int boxTopY = arrowTopY;
		int boxH = arrowH;
		int boxBorderW = s2;

		g.setColor(Color.black);
		g.fillRect(boxL, boxTopY, boxW, boxH);
		g.setColor(sliderBoxBlue);
		g.fillRect(boxL, boxTopY+s1, boxW * amt / MAX_TICKS, boxH-s2);

		if (hoverBox == reserveSlider) {
			g.setColor(Color.yellow);
			Stroke prev = g.getStroke();
			g.setStroke(stroke2);
			g.drawRect(boxL, boxTopY, boxW, boxH);
			g.setStroke(prev);
		}

		boxAreaL = boxL+boxBorderW;
		boxAreaW = boxW-boxBorderW-boxBorderW;
		reserveSlider.setBounds(boxAreaL, boxTopY, boxAreaW, boxH);

		// amount string
		g.setFont(narrowFont(20));
		int transferAmt = (int) (playerRsv * amt / MAX_TICKS);
		String amtString = text("PLANETS_AMT_BC", transferAmt);
		int sw = g.getFontMetrics().stringWidth(amtString);
		int amtX = xBox+boxWidth-s40-sw;
		g.setColor(SystemPanel.blackText);
		drawString(g, amtString, amtX, boxTopY+boxH-s3);
	}
	private void drawInfo(Graphics2D g)	{
		int infoM = s30;
		int infoW = boxWidth - infoM - infoM;
		int lineSep = s10;

		// Info about current choice
		String currentChoice;
		if (isPlayer)
			currentChoice = text("PLANETS_BUDGET_CURRENT_PLAYER", ceil(budget.budgetPlayerBC()));
		else if (isGovernor)
			currentChoice = text("PLANETS_BUDGET_GOVERNOR_ALONE", budget.budgetGovernorBC());
		else
			currentChoice = text("PLANETS_BUDGET_CURRENT_NONE");

		g.setFont(narrowFont(20));
		int sw = g.getFontMetrics().stringWidth(currentChoice);
		if (sw > infoM) {
			scaledFont(g, currentChoice, infoW, 19, 15);
			sw = g.getFontMetrics().stringWidth(currentChoice);
		}
		int infoX = xBox + (infoW - sw)/2;
		int infoY = yTitle + s60;
		g.setColor(SystemPanel.blackText);
		drawString(g, currentChoice, infoX, infoY);
		infoY += lineSep;

		// Info about potential governor choice
		if (isPlayer && !isMultiple) {
			String govChoice;
			if (isGovernor)
					govChoice = text("PLANETS_BUDGET_GOVERNOR_SUGGEST", budget.budgetGovernorBC());
				else
					govChoice = text("PLANETS_BUDGET_NO_GOVERNOR");
			g.setFont(narrowFont(20));
			sw = g.getFontMetrics().stringWidth(govChoice);
			if (sw > infoM) {
				scaledFont(g, govChoice, infoW, 19, 15);
				sw = g.getFontMetrics().stringWidth(govChoice);
				infoX = xBox + (infoW - sw)/2;
				infoY += lineSep;
				g.setColor(SystemPanel.blackText);
				drawString(g, govChoice, infoX, infoY);
			}
		}
	}
	private void drawBottomButtons(Graphics2D g)	{
		// button vars
		int buttonM  = s30;  // L/R margin
		int buttonM2 = s20; // space between buttons
		int buttonW  = (boxWidth - buttonM - buttonM - buttonM2 - buttonM2)/3;
		int buttonH  = s32;
		int buttonY  = yBox + boxHeight - buttonH - s30;
		int button1X = xBox + buttonM;
		int button2X = button1X + buttonW + buttonM2;
		int button3X = button2X + buttonW + buttonM2;
		int cnr = s2;
		g.setFont(narrowFont(20));
		int dy = s10;

		// set up background gradients
		if (!initted)
			init(button1X, button1X+buttonW, button2X, button2X+buttonW, button3X, button3X+buttonW);

		//Bottom Line
		// transfer button
		int buttonX = button1X;
		transfertButton.setBounds(buttonX, buttonY, buttonW, buttonH);
		g.setColor(SystemPanel.textShadowC);
		g.fillRoundRect(buttonX+s4, buttonY+s4, buttonW, buttonH, cnr, cnr);
		g.setPaint(largeGreenBackLeftGrad);
		g.fillRoundRect(buttonX, buttonY, buttonW, buttonH, cnr, cnr);
		Stroke prev = g.getStroke();
		g.setStroke(stroke1);
		if (hoverBox == transfertButton)
			g.setColor(Color.yellow);
		else
			g.setColor(okButtonBdrC);
		g.drawRoundRect(buttonX, buttonY, buttonW, buttonH, cnr, cnr);
		g.setStroke(prev);
		Color c1 = hoverBox == transfertButton ? Color.yellow : SystemPanel.whiteText;
		String str = text("PLANETS_TRANSFER_ACCEPT");
		int sw = g.getFontMetrics().stringWidth(str);
		int strX = buttonX+ ((buttonW - sw) / 2);
		drawShadowedString(g, str, 3, strX, buttonY+buttonH-dy, SystemPanel.textShadowC, c1);

		// cancel button
		buttonX = button2X;
		cancelButton.setBounds(buttonX, buttonY, buttonW, buttonH);
		g.setColor(SystemPanel.textShadowC);
		g.fillRoundRect(buttonX+s4, buttonY+s4, buttonW, buttonH, cnr, cnr);
		g.setPaint(largeRedBackMidGrad);
		g.fillRoundRect(buttonX, buttonY, buttonW, buttonH, cnr, cnr);
		prev = g.getStroke();
		g.setStroke(stroke1);
		if (hoverBox == cancelButton)
			g.setColor(Color.YELLOW);
		else
			g.setColor(cancelButtonBdrC);
		g.drawRoundRect(buttonX, buttonY, buttonW, buttonH, cnr, cnr);
		g.setStroke(prev);
		Color c2 = hoverBox == cancelButton ? Color.YELLOW : SystemPanel.whiteText;
		str = text("PLANETS_TRANSFER_CANCEL");
		sw = g.getFontMetrics().stringWidth(str);
		strX = buttonX+ ((buttonW - sw) / 2);
		drawShadowedString(g, str, 3, strX, buttonY+buttonH-dy, SystemPanel.textShadowC, c2);

		// Budget button
		buttonX = button3X;
		budgetButton.setBounds(buttonX, buttonY, buttonW, buttonH);
		g.setColor(SystemPanel.textShadowC);
		g.fillRoundRect(buttonX+s4, buttonY+s4, buttonW, buttonH, cnr, cnr);
		g.setPaint(largeGreenBackRightGrad);
		g.fillRoundRect(buttonX, buttonY, buttonW, buttonH, cnr, cnr);
		prev = g.getStroke();
		g.setStroke(stroke1);
		if (hoverBox == budgetButton)
			g.setColor(Color.YELLOW);
		else
			g.setColor(okButtonBdrC);
		g.drawRoundRect(buttonX, buttonY, buttonW, buttonH, cnr, cnr);
		g.setStroke(prev);
		c1 = hoverBox == budgetButton ? Color.YELLOW : SystemPanel.whiteText;
		str = text("PLANETS_BUTTON_BUDGET");
		sw = g.getFontMetrics().stringWidth(str);
		strX = buttonX+ ((buttonW - sw) / 2);
		drawShadowedString(g, str, 3, strX, buttonY+buttonH-dy, SystemPanel.textShadowC, c1);

		// ----------------------------------------
		// Minor buttons
		g.setFont(narrowFont(18));
		dy = s8;
		int arc = s15;
		int buttonH1 = s27;
		int buttonY1 = buttonY - s45;
		buttonX = button1X;
		int buttonW1 = buttonW * 3/2 + buttonM2/2;

		// Mid line
		// Transfer Budget button
		convertButton.setBounds(buttonX, buttonY1, buttonW1, buttonH1);
		prev = g.getStroke();
		g.setStroke(stroke1);
		if (hoverBox == convertButton)
			g.setColor(Color.YELLOW);
		else
			g.setColor(okButtonBdrC);
		g.drawRoundRect(buttonX, buttonY1, buttonW1, buttonH1, arc, arc);
		g.setStroke(prev);
		c2 = hoverBox == convertButton ? Color.YELLOW : SystemPanel.whiteText;
		str = text("PLANETS_BUTTON_CONVERT");
		sw = g.getFontMetrics().stringWidth(str);
		strX = buttonX+ ((buttonW1 - sw) / 2);
		drawShadowedString(g, str, 3, strX, buttonY1+buttonH1-dy, SystemPanel.textShadowC, c2);

		// Remove Player budget button
		if (isPlayer || isMultiple) {
			buttonX += buttonW1 + buttonM2;
			governorButton.setBounds(buttonX, buttonY1, buttonW1, buttonH1);
			prev = g.getStroke();
			g.setStroke(stroke1);
			if (hoverBox == governorButton)
				g.setColor(Color.YELLOW);
			else
				g.setColor(okButtonBdrC);
			g.drawRoundRect(buttonX, buttonY1, buttonW1, buttonH1, arc, arc);
			g.setStroke(prev);
			c2 = hoverBox == governorButton ? Color.YELLOW : SystemPanel.whiteText;
			str = text("PLANETS_BUTTON_REMOVE");
			sw = g.getFontMetrics().stringWidth(str);
			strX = buttonX+ ((buttonW1 - sw) / 2);
			drawShadowedString(g, str, 3, strX, buttonY1+buttonH1-dy, SystemPanel.textShadowC, c2);
		}

		// -------------------------------------------------
		// Top Line
		buttonX = button1X;
		buttonY1 -= s40;
		// budgetize wish button
		optimalBudgetButton.setBounds(buttonX, buttonY1, buttonW1, buttonH1);
		prev = g.getStroke();
		g.setStroke(stroke1);
		if (hoverBox == optimalBudgetButton)
			g.setColor(Color.YELLOW);
		else
			g.setColor(okButtonBdrC);
		g.drawRoundRect(buttonX, buttonY1, buttonW1, buttonH1, arc, arc);
		g.setStroke(prev);
		c2 = hoverBox == optimalBudgetButton ? Color.YELLOW : SystemPanel.whiteText;
		str = text("PLANETS_BUTTON_BUDGET_WISH");
		sw = g.getFontMetrics().stringWidth(str);
		strX = buttonX+ ((buttonW1 - sw) / 2);
		drawShadowedString(g, str, 3, strX, buttonY1+buttonH1-dy, SystemPanel.textShadowC, c2);

		// Share wish button
		buttonX += buttonW1 + buttonM2;
		optimalShareButton.setBounds(buttonX, buttonY1, buttonW1, buttonH1);
		prev = g.getStroke();
		g.setStroke(stroke1);
		if (hoverBox == optimalShareButton)
			g.setColor(Color.YELLOW);
		else
			g.setColor(okButtonBdrC);
		g.drawRoundRect(buttonX, buttonY1, buttonW1, buttonH1, arc, arc);
		g.setStroke(prev);
		c2 = hoverBox == optimalShareButton ? Color.YELLOW : SystemPanel.whiteText;
		str = text("PLANETS_BUTTON_SHARE_WISH");
		sw = g.getFontMetrics().stringWidth(str);
		strX = buttonX+ ((buttonW1 - sw) / 2);
		drawShadowedString(g, str, 3, strX, buttonY1+buttonH1-dy, SystemPanel.textShadowC, c2);
	}
    private void increment()   { setAmt(amt+1); }
    private void decrement()   { setAmt(amt-1); }
    private void setAmt(int i) { amt = bounds(0, i, MAX_TICKS); }
    private void setHoverSprite(int x, int y) {
        hoverBox = null;

        if (cancelButton.contains(x, y))
            hoverBox = cancelButton;
        else if (transfertButton.contains(x, y))
            hoverBox = transfertButton;
        else if (leftArrow.contains(x,y))
            hoverBox = leftArrow;
        else if (rightArrow.contains(x,y))
            hoverBox = rightArrow;
        else if (reserveSlider.contains(x,y))
            hoverBox = reserveSlider;
		else if (budgetButton.contains(x,y))
			hoverBox = budgetButton;
		else if (governorButton.contains(x,y))
			hoverBox = governorButton;
		else if (optimalShareButton.contains(x,y))
			hoverBox = optimalShareButton;
		else if (optimalBudgetButton.contains(x,y))
			hoverBox = optimalBudgetButton;
		else if (convertButton.contains(x,y))
			hoverBox = convertButton;
	}
	private void init(int leftX0, int leftX1, int midX0, int midX1, int rightX0, int rightX1)	{
		Point2D start1	= new Point2D.Float(leftX0, 0);
		Point2D end1	= new Point2D.Float(leftX1, 0);
		Point2D start2	= new Point2D.Float(midX0, 0);
		Point2D end2	= new Point2D.Float(midX1, 0);
		Point2D start3	= new Point2D.Float(rightX0, 0);
		Point2D end3	= new Point2D.Float(rightX1, 0);
		float[] fract	= {0.0f, 0.2f, 0.5f, 0.8f, 1.0f};

        Color greenEdgeC = new Color(44,59,30);
        Color greenMidC = new Color(71,93,48);
        Color[] greenColors = {greenEdgeC, greenEdgeC, greenMidC, greenEdgeC, greenEdgeC };

        Color redEdgeC = new Color(100,70,50);
        Color redMidC = new Color(161,110,76);
        Color[] redColors = {redEdgeC, redEdgeC, redMidC, redEdgeC, redEdgeC };

		largeGreenBackLeftGrad	= new LinearGradientPaint(start1, end1, fract, greenColors);
		largeRedBackMidGrad		= new LinearGradientPaint(start2, end2, fract, redColors);
		largeGreenBackRightGrad	= new LinearGradientPaint(start3, end3, fract, greenColors);

		initted = true;
	}
    private void exit() {
        hoverBox = null;
		showHelp = false;
        amt = 0;
        softClick();
        disableGlassPane();
        repaint();
    }
	private void transfertButtonAction()	{ // No changes
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
		boolean carryUnfunded = govOptions().autospendCarryUnfunded();
		for(StarSystem sys : targetSystems)
			sys.colony().budget().transfertBudget(carryUnfunded);
		empire.budget().computeIfNeeded(true);
		exit();
	}
	private void budgetButtonAction()	{
		int amount = (int) (playerRsv * amt / MAX_TICKS);
		for(StarSystem sys : targetSystems) {
			ColonyBudget cb = sys.colony().budget();
			if (cb.isPlayerBudget())
				playerRsv += cb.budgetPlayerBC();
			int amt = min(amount, (int)playerRsv);
			cb.budgetPlayerBC(amt);
			playerRsv -= amt;
			if(playerRsv < 1)
				break;
		}
		empire.budget().computeIfNeeded(true);
		for(StarSystem sys : targetSystems)
			sys.colony().governIfNeeded();
		exit();
	}
	private void govButtonAction()	{
		for(StarSystem sys : targetSystems)
			sys.colony().budget().budgetPlayerBC(null);
		empire.budget().computeIfNeeded(true);
		for(StarSystem sys : targetSystems)
			sys.colony().governIfNeeded();
		exit();
	}
	private void wishShareButtonAction()	{
		int wishSum = 0;
		int onBudget = 0;
		for(StarSystem sys : targetSystems) {
			ColonyBudget cb = sys.colony().budget();
			wishSum += cb.reserveNeededBC();
			if (cb.isPlayerBudget())
				onBudget += cb.budgetPlayerBC();
		}
		float ratio = min(1, (playerRsv+onBudget)/wishSum);
		for(StarSystem sys : targetSystems)
			sys.colony().budget().budgetizeRatio(ratio);

		empire.budget().computeIfNeeded(true);
		for(StarSystem sys : targetSystems)
			sys.colony().governIfNeeded();
		exit();
	}
	private void wishBudgetButtonAction()	{
		for(StarSystem sys : targetSystems)
			sys.colony().budget().budgetizeNeeded();

		empire.budget().computeIfNeeded(true);
		for(StarSystem sys : targetSystems)
			sys.colony().governIfNeeded();
		exit();
	}
	@Override public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				exit();
				return;
			case KeyEvent.VK_SPACE:
			case KeyEvent.VK_ENTER:
			case KeyEvent.VK_F1:
				showHelp = !showHelp;
				helpUI = null;
				repaint();
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
    @Override
    public void mouseClicked(MouseEvent arg0) { }
    @Override
    public void mouseEntered(MouseEvent arg0) { }
    @Override
    public void mouseExited(MouseEvent arg0) {
        if (hoverBox != null) {
            hoverBox = null;
            repaint();
        }
    }
    @Override
    public void mousePressed(MouseEvent arg0) { }
    @Override
    public void mouseReleased(MouseEvent e) {
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

        if (amt != prevAmt) {
            softClick();
            repaint();
        }
        else
            misClick();
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        Shape prevHover = hoverBox;
        setHoverSprite(e.getX(),e.getY());

        if (prevHover != hoverBox)
            repaint();
    }
    @Override
    public void mouseMoved(MouseEvent e) {
        Shape prevHover = hoverBox;
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
    }
}
