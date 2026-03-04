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
package rotp.ui.main;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static rotp.model.colony.Colony.ECOLOGY;
import static rotp.model.colony.Colony.RESEARCH;
import static rotp.model.colony.Colony.SHIP;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList; // modnar: change to cleaner icon set
import java.util.List; // modnar: change to cleaner icon set

import javax.swing.SwingUtilities;

import rotp.model.colony.Colony;
import rotp.model.colony.ColonyIndustry;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GameSession;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.SystemViewer;
import rotp.ui.UserPreferences;
import rotp.ui.options.AllSubUI;
import rotp.ui.util.ParamSubUI;
import rotp.util.ImageManager;

public class EmpireColonySpendingPane extends BasePanel {
    private static final long serialVersionUID = 1L;
    static final Color sliderHighlightColor	= new Color(255,255,255);
    static final Color sliderBoxEnabled		= new Color(34,140,142);
    static final Color sliderBoxDisabled	= new Color(102,137,137);
    static final Color sliderErrEnabled		= new Color(140,34,34);
    static final Color sliderErrDisabled	= new Color(137,102,102);
    static final Color sliderBackEnabled	= Color.black;
    static final Color sliderBackDisabled	= new Color(65,65,65);
    static final Color sliderTextEnabled	= Color.black;
    static final Color sliderTextDisabled	= new Color(65,65,65);
    static final Color sliderTextHasOrder	= new Color(0,0,142);
    static final Color sliderTextHasOrderD	= new Color(65,65,142);
    static final Color sliderTextUrged		= new Color(142,0,142);
    static final Color sliderTextUrgedD		= new Color(96,65,128);

    private static BufferedImage governorImage;
    private static BufferedImage noGovernorImage;
    private static int governorImageEmpireId;
    private static int govMaxW, optMaxW;
    private static int govButtonW, optButtonW;
    private static int govButtonX, optButtonX;
    private static int maxButtonFontSize = 17;
    private static int minButtonFontSize = 12;
    private static int govFontSize;
    private static int iconWidth    = s25;
    private static int buttonH      = s25;
    private static int buttonY;
    private static int buttonMargin = s20;
    private static int buttonOffset = s10;
    private static boolean needInitializationRound = true;
    private static boolean needInitializationSquare = true;

    private final Rectangle governorBox	= new Rectangle();
    private final Rectangle optionsBox	= new Rectangle();
    private GradientPaint govPaint, optPaint;
    private final SystemViewer parent;
    private final boolean isRound;
    private final Color textColorOn, textColorOff;
    private final Color paintColorExt, paintColorInt;
    private final Color buttonColor;
    final Color borderHi, borderLo, textC, backC;

    private EmpireSliderPane shipSlider, defSlider, indSlider, ecoSlider, researchSlider;

    private GalaxyMapPanel mapListener;

    public static void resetPanel() {
    	needInitializationRound = true;
    	needInitializationSquare = true;
    }

    public EmpireColonySpendingPane(SystemViewer p, Color c0, Color text, Color hi, Color lo,
    		Color tOn, Color tOff, Color pExt, Color pInt, Color bC, boolean round) {
        parent = p;
        textC  = text;
        backC  = c0;
        borderHi = hi;
        borderLo = lo;
        textColorOn  = tOn;
        textColorOff = tOff;
        paintColorExt = pExt;
        paintColorInt = pInt;
        buttonColor   = bC;

        isRound  = round;
        init();
    }
    BufferedImage governorImage()	{
    	if (governorImage == null || governorImageEmpireId != player().id)
    		initGovernorImage();
    	return governorImage;
    }
    public void mapListener(GalaxyMapPanel map)  { mapListener = map; }
    @Override
    public String textureName()            { return parent.subPanelTextureName(); }
    private void init() {
        shipSlider     = new EmpireSliderPane(this, Colony.SHIP);
        defSlider      = new EmpireSliderPane(this, Colony.DEFENSE);
        indSlider      = new EmpireSliderPane(this, Colony.INDUSTRY);
        ecoSlider      = new EmpireSliderPane(this, Colony.ECOLOGY);
        researchSlider = new EmpireSliderPane(this, Colony.RESEARCH);

        GridLayout layout = new GridLayout(6,0);
        layout.setVgap(s1);
        setOpaque(true);

        setBackground(backC);
        setLayout(layout);
        add(new EmpireSliderPane(this, -1));  // this is the "Colony Spending" label
        add(shipSlider);
        add(defSlider);
        add(indSlider);
        add(ecoSlider);
        add(researchSlider);
    }
    private boolean needInitialization() {
    	return isRound? needInitializationRound : needInitializationSquare;
    }
    private void reinitPanel(Graphics2D g) {
    	buttonY = getHeight() - buttonH - s1;
    	int w = getWidth();
    	initGovernorImage();
    	initNoGovernorImage();
    	govFontSize = maxButtonFontSize;
    	int maxFont;
    	govMaxW = scaled(150);
    	optMaxW = scaled(75);
    	do {
    		maxFont = govFontSize;
    		govFontSize = initButtons(g, maxFont);
    	}
    	while(maxFont != govFontSize);
    	govButtonX = buttonOffset;
    	int x2 = govButtonX + govButtonW;
    	govPaint = new GradientPaint(govButtonX, buttonY, paintColorExt, x2, buttonY, paintColorInt);
    	x2 = w - buttonOffset;
    	optButtonX = x2 - optButtonW;
    	optPaint = new GradientPaint(optButtonX, buttonY, paintColorInt, x2, buttonY, paintColorExt);
    	if (isRound)
    		needInitializationRound = false;
    	else
    		needInitializationSquare = false;
    }
    private void initGovernorImage()	{
    	governorImageEmpireId = player().id;
		int mugH = s20; // s82
		int mugW = mugH * 76 / 82; // s76
		governorImage = new BufferedImage(mugW, mugH, TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) governorImage.getGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		Image img = player().diploMugshotQuiet();
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
		g.drawImage(img, 0, 0, mugW, mugH, 0, 0, imgW, imgH, null);
    	g.dispose();
    }
    private void initNoGovernorImage()	{
		int mugH = s22;
		int mugW = mugH;
		noGovernorImage = new BufferedImage(mugW, mugH, TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) noGovernorImage.getGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		Image img = image("NO_GOVERNOR");
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
		g.drawImage(img, 0, 0, mugW, mugH, 0, 0, imgW, imgH, null);
    	g.dispose();
    }
    private int initButtons(Graphics2D g, int maxFont) {
    	int font1 = initGovernorButton(g, govMaxW, maxFont);
    	int font2 = initOptionsButtonW(g, optMaxW, font1);
    	return min(font1, font2);
    }
    private int initGovernorButton(Graphics2D g, int maxWidth, int maxFont)	{
		int margin = iconWidth + buttonMargin/2;
		int maxTitleWidth = maxWidth - margin;
		String title1 = text("GOVERNOR_IS_ON_BUTTON");
		int fontSize1 = scaledFont(g, title1, maxTitleWidth, maxFont, minButtonFontSize);
		int titleW1   = g.getFontMetrics().stringWidth(title1);

		String title2 = text("GOVERNOR_IS_OFF_BUTTON");
		int fontSize2 = scaledFont(g, title2, maxTitleWidth, maxFont, minButtonFontSize);
		int titleW2   = g.getFontMetrics().stringWidth(title2);

		String title3 = text("GOVERNOR_IS_ON_AND_OFF_BUTTON");
		int fontSize3 = scaledFont(g, title3, maxTitleWidth, maxFont, minButtonFontSize);
		int titleW3   = g.getFontMetrics().stringWidth(title3);

		int titleW = max(titleW1, titleW2, titleW3);
		govButtonW = titleW + margin;
		return min(fontSize1, fontSize2, fontSize3);
    }
    private int initOptionsButtonW(Graphics2D g, int maxWidth, int maxFont) {
		int maxTitleWidth = maxWidth - buttonMargin;
		String title = text("GOVERNOR_OPTIONS");
		int fontSize = scaledFont(g, title, maxTitleWidth, maxFont, minButtonFontSize);
		int titleW   = g.getFontMetrics().stringWidth(title);
		optButtonW = titleW + buttonMargin;
		return fontSize;
    }
    private void drawGovernorButton(Graphics2D g, boolean hovered, boolean isGovernor) {
        Color borderC, titleC;
        String title = isGovernor? text("GOVERNOR_IS_ON_BUTTON") : text("GOVERNOR_IS_OFF_BUTTON");
        int titleDx = iconWidth/2;
        g.setFont(narrowFont(govFontSize));
        Rectangle2D bound = g.getFontMetrics().getStringBounds(title, g);
        
        int titleW     = (int) Math.ceil(bound.getWidth());
        double titleH  = bound.getHeight();
        double descent = g.getFontMetrics().getDescent();
        double titleDy = (buttonH-titleH)/2;
        int titleX = round(govButtonX + (govButtonW-titleW)/2) - titleDx;
        int titleY = round(buttonY + buttonH - descent-titleDy);

        if (isRound) {
            Paint prevPaint = g.getPaint();
            g.setPaint(govPaint);
            g.fillRoundRect(govButtonX, buttonY, govButtonW, buttonH, s10, s10);
            g.setPaint(prevPaint);
        }
        if (hovered) {
        	borderC = SystemPanel.yellowText;
        	titleC  = SystemPanel.yellowText;
        }
        else {
        	borderC = buttonColor;
        	if (isGovernor)
        		titleC  = textColorOn;
        	else
        		titleC  = textColorOff;
        }
        governorBox.setBounds(govButtonX, buttonY, govButtonW, buttonH);

        Stroke prevStroke = g.getStroke();
        g.setStroke(stroke2);
        g.setColor(borderC);
        if (isRound)
        	g.drawRoundRect(govButtonX, buttonY, govButtonW, buttonH, s10, s10);
        else
        	g.drawRect(govButtonX, buttonY, govButtonW, buttonH);
        g.setStroke(prevStroke);
        g.setColor(titleC);
        g.drawString(title, titleX, titleY);

        if (isGovernor)
        	g.drawImage(governorImage(), govButtonX+govButtonW-iconWidth, buttonY+s3, null);
        else
        	g.drawImage(noGovernorImage, govButtonX+govButtonW-iconWidth, buttonY+s2, null);
    }
    private void drawOptionsButton(Graphics2D g, boolean hovered) {
        Color borderC, titleC;
        String title = text("GOVERNOR_OPTIONS");
        g.setFont(narrowFont(govFontSize));
        Rectangle2D bound = g.getFontMetrics().getStringBounds(title, g);
        int titleW  = (int) Math.ceil(bound.getWidth());
        double titleH  = bound.getHeight();
        double descent = g.getFontMetrics().getDescent();
        double titleDy = (buttonH-titleH)/2;
        int titleX = round(optButtonX + (optButtonW-titleW)/2);
        int titleY = round(buttonY + buttonH - descent-titleDy);

        if (isRound) {
            Paint prevPaint = g.getPaint();
            g.setPaint(optPaint);
            g.fillRoundRect(optButtonX, buttonY, optButtonW, buttonH, s10, s10);
            g.setPaint(prevPaint);
        }
        if (hovered) {
        	borderC = SystemPanel.yellowText;
        	titleC  = SystemPanel.yellowText;
        }
        else {
        	borderC = buttonColor;
           	titleC  = textColorOn;
        }
    	optionsBox.setBounds(optButtonX, buttonY, optButtonW, buttonH);

    	Stroke prevStroke = g.getStroke();
        g.setStroke(stroke2);
        g.setColor(borderC);
        if (isRound)
        	g.drawRoundRect(optButtonX, buttonY, optButtonW, buttonH, s10, s10);
        else
        	g.drawRect(optButtonX, buttonY, optButtonW, buttonH);
        g.setStroke(prevStroke);

        g.setColor(titleC);
        g.drawString(title, titleX, titleY);
    }

    @Override public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        int mods = e.getModifiersEx();
        switch (k) {
            case KeyEvent.VK_1:
            	if (mods == 0) // No modifiers
            		shipSlider.increment(true);
            	else if (e.isShiftDown())
            		shipSlider.decrement(true);
            	else if (e.isControlDown())
            		shipSlider.toggleLock();
            	else if (e.isAltDown())
            		shipSlider.smoothMaxClick(true, null);
                return;
            case KeyEvent.VK_2:
            	if (mods == 0) // No modifiers
            		defSlider.increment(true);
            	else if (e.isShiftDown())
            		defSlider.decrement(true);
            	else if (e.isControlDown())
            		defSlider.toggleLock();
            	else if (e.isAltDown())
            		defSlider.smoothMaxClick(true, null);
                return;
            case KeyEvent.VK_3:
            	if (mods == 0) // No modifiers
            		indSlider.increment(true);
            	else if (e.isShiftDown())
            		indSlider.decrement(true);
            	else if (e.isControlDown())
            		indSlider.toggleLock();
            	else if (e.isAltDown())
            		indSlider.smoothMaxClick(true, null);
                return;
            case KeyEvent.VK_4:
            	if (mods == 0) // No modifiers
            		ecoSlider.increment(true);
            	else if (e.isShiftDown())
            		ecoSlider.decrement(true);
            	else if (e.isControlDown())
            		ecoSlider.toggleLock();
            	else if (e.isAltDown())
            		ecoSlider.smoothMaxClick(true, null);
                return;
            case KeyEvent.VK_5:
            	if (mods == 0) // No modifiers
            		researchSlider.increment(true);
            	else if (e.isShiftDown())
            		researchSlider.decrement(true);
            	else if (e.isControlDown())
            		researchSlider.toggleLock();
            	else if (e.isAltDown())
            		researchSlider.smoothMaxClick(true, null);
                return;
            case KeyEvent.VK_Q:
            {
                toggleGovernor();
                break;
            }
            case KeyEvent.VK_W:
            {
                toggleAutoShips();
                break;
            }
        }
    }

    private class EmpireSliderPane extends BasePanel implements MouseListener, MouseMotionListener, MouseWheelListener {
        private static final long serialVersionUID = 1L;
        //EmpireColonySpendingPane mgmtPane;
        private final Polygon leftArrow = new Polygon();
        private final Polygon rightArrow = new Polygon();
        private final Rectangle labelBox = new Rectangle();
        private final Rectangle sliderBox = new Rectangle();
        private final Rectangle resultBox = new Rectangle();
        private Shape hoverBox;
        // polygon coordinates for left & right increment buttons
        private final int leftButtonX[] = new int[3];
        private final int leftButtonY[] = new int[3];
        private final int rightButtonX[] = new int[3];
        private final int rightButtonY[] = new int[3];
        private final int category;
        private EmpireSliderPane(EmpireColonySpendingPane ui, int cat) {
            //mgmtPane = ui;
            category = cat;
            init();
        }
        private void init() {
            setOpaque(false);
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }

        @Override public void paintComponent(Graphics g0) {
        	Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g);
        	if (needInitialization())
        		reinitPanel(g);

            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if  (colony == null)
                return;

            int w = getWidth();

            if (category < 0) {
            	buttonY = getHeight() - buttonH - s1;
            	drawGovernorButton(g, hoverBox == governorBox, colony.isGovernor());
            	drawOptionsButton(g, hoverBox == optionsBox);

            	//paintGovernor(g, colony);
//                Color color;
//                if (colony.isGovernor()) {
//                    color = Color.green;
//                } else {
//                    color = MainUI.shadeBorderC();
//                }
//
//                g.setFont(narrowFont(20));
//                String titleText = text("MAIN_COLONY_ALLOCATE_SPENDING");
//                int titleY = getHeight() - s6;
//                drawShadowedString(g, titleText, 2, s5, titleY, color, textC);
//
//                // crappy ASCII art. Should be something else.
//                // TODO: for future use
////                if (1 == 0) {
////                    if (colony.isAutoShips()) {
////                        color = Color.green;
////                    } else {
////                        color = MainUI.shadeBorderC();
////                    }
////                    String shipAutomateText = "]=>";
////                    drawShadowedString(g, shipAutomateText, 2, w - s95, titleY, color, textC);
////                }
//                String governorOptionsText = text("GOVERNOR_OPTIONS");
//                drawShadowedString(g, governorOptionsText, 2, w-s60, titleY, MainUI.shadeBorderC(), textC);
                return;
            }
            String text = text(Colony.categoryName(category));

            // label
            Color textC;
            if (hoverBox == labelBox)
                textC = SystemPanel.yellowText;
            else if (colony.canAdjust(category))
            	if (colony.isUrged(category))
            		textC = sliderTextUrged;
            	else if (colony.hasOrder(category) && options().showPendingOrders())
            		textC = sliderTextHasOrder;
            	else
            		textC = sliderTextEnabled;
            else if (colony.isUrged(category))
        		textC = sliderTextUrgedD;
        	else if (colony.hasOrder(category) && options().showPendingOrders())
        		textC = sliderTextHasOrderD;
        	else
                textC = sliderTextDisabled;
            
            String labelText = text(text);
            g.setColor(textC);
            g.setFont(narrowFont(18));
            drawString(g,labelText, s10, getHeight()-s10);
            labelBox.setBounds(s5, 0, leftMargin()-s15, getHeight());

            int boxL = boxLeftX();
            int boxW = boxRightX() - boxL;
            int boxTopY = boxTopY();
            int boxBottomY = boxBottomY();
            int boxH = boxBottomY - boxTopY;

            // slider
            float pct = colony.pct(category);
            leftButtonX[0] = leftMargin(); leftButtonX[1] = leftMargin()+buttonWidth(); leftButtonX[2] = leftMargin()+buttonWidth();
            leftButtonY[0] = buttonMidY(); leftButtonY[1] = buttonTopY(); leftButtonY[2] = buttonBottomY();

            rightButtonX[0] = w-rightMargin(); rightButtonX[1] = w-rightMargin()-buttonWidth(); rightButtonX[2] = w-rightMargin()-buttonWidth();
            rightButtonY[0] = buttonMidY(); rightButtonY[1] = buttonTopY(); rightButtonY[2] = buttonBottomY();

            Color c1  = colony.canAdjust(category) ? sliderBoxEnabled : sliderBoxDisabled;
            Color c1a = colony.canAdjust(category) ? sliderErrEnabled : sliderErrDisabled;
            Color c2  = colony.canAdjust(category) ? sliderBackEnabled : sliderBackDisabled;

            Color c3 = hoverBox == leftArrow ? SystemPanel.yellowText : c2;
            g.setColor(c3);
            g.fillPolygon(leftButtonX, leftButtonY, 3);

            c3 = hoverBox == rightArrow ? SystemPanel.yellowText : c2;
            g.setColor(c3);
            g.fillPolygon(rightButtonX, rightButtonY, 3);

            leftArrow.reset();
            rightArrow.reset();
            for (int i=0;i<leftButtonX.length;i++) {
                leftArrow.addPoint(leftButtonX[i], leftButtonY[i]);
                rightArrow.addPoint(rightButtonX[i], rightButtonY[i]);
            }

            sliderBox.x = boxL;
            sliderBox.y = boxTopY;
            sliderBox.width = boxW;
            sliderBox.height = boxH;

            g.setColor(c2);
            g.fillRect(boxL+boxBorderW(), boxTopY, boxW-(2*boxBorderW()), boxH);

            if (colony.warning(category))
           		g.setColor(c1a);
            else
                g.setColor(c1);
            
            Rectangle fillRect;
            
            if (pct == 1)           
                fillRect = new Rectangle(boxL+boxBorderW(), boxTopY+s2, boxW-(2*boxBorderW()), boxH-s3);
            else
                fillRect = new Rectangle(boxL+boxBorderW(), boxTopY+s2, (int) (pct*(boxW-(2*boxBorderW()))), boxH-s3);
                
            g.fill(fillRect);

            if (category == Colony.INDUSTRY)  {
            	ColonyIndustry industry = colony.industry();
            	Float[] factoryBalance = industry.factoryBalance();
            	float balance   = factoryBalance[0];
            	Float refitFlag = factoryBalance[1];
            	boolean rightAmount = (balance == 0);
            	boolean warning = !rightAmount;// && !colony.isGovernor();
            	if (warning) {
            		String indStr = "";
            		if (balance > 0) {
						indStr = text("MAIN_COLONY_SPENDING_UNUSED_FACT", df1.format(balance));
						g.setColor(Color.ORANGE);
						g.fill(fillRect);
						g.setColor(Color.GRAY);
                	}
                	else {
						indStr = text("MAIN_COLONY_SPENDING_NEEDED_FACT", df1.format(-balance));
						g.setColor(Color.LIGHT_GRAY);
	                }
					if (refitFlag == null)
						indStr = text("MAIN_COLONY_SPENDING_REFIT") + ", " + indStr;
	            	//g.setFont(narrowFont(14));
	            	int fontSize = scaledFont(g, indStr, boxW-s5, 14, 10);
	            	int yOff = scaled(4 + (14-fontSize)/2);
	            	int sw1 = g.getFontMetrics().stringWidth(indStr);
	            	int x1 = (boxW-sw1)/2;
	            	drawString(g, indStr, boxL+x1, boxTopY+boxH-yOff);
            	}
            }
            
            if (category == Colony.ECOLOGY)  {
                float popGrowth = colony.ecology().upcomingPopGrowthFloat();
                String valStr = String.format("%+3.1f", popGrowth);
                String popStr = text("MAIN_COLONY_SPENDING_ECO_GROWTH", valStr);
                if (popGrowth < 0)
                    g.setColor(SystemPanel.darkOrangeText);
                else
                    g.setColor(Color.gray);
                //g.setFont(narrowFont(14));
                int fontSize = scaledFont(g, popStr, boxW-s5, 14, 10);
                int yOff = scaled(4 + (14-fontSize)/2);
                int sw1 = g.getFontMetrics().stringWidth(popStr);
                int x1 = (boxW-sw1)/2;
                drawString(g,popStr, boxL+x1, boxTopY+boxH-yOff);
                
                if (popGrowth < 0)
                    g.setColor(SystemPanel.orangeText);
                else
                    g.setColor(Color.lightGray);
                Shape prevClip = g.getClip();
                g.setClip(fillRect);
                drawString(g,popStr, boxL+x1, boxTopY+boxH-yOff);
                g.setClip(prevClip);
            }

            if (hoverBox == sliderBox) {
                g.setColor(SystemPanel.yellowText);
                Stroke prev = g.getStroke();
                g.setStroke(stroke2);
                g.drawRect(boxL+s3, boxTopY+s1, boxW-s6, boxH-s2);
                g.setStroke(prev);
            }

            // result
            textC = SystemPanel.blackText;
            if (hoverBox == resultBox)
                textC = SystemPanel.yellowText;
            String resultText = text(colony.category(category).upcomingResult());

            g.setColor(textC);
            scaledFont(g, resultText, rightMargin()-s10, 18, 14);
            g.setFont(narrowFont(18));
            int sw = g.getFontMetrics().stringWidth(resultText);
            drawString(g,resultText, getWidth()-sw-s10, getHeight()-s10);
            resultBox.setBounds(getWidth()-rightMargin(), 0, rightMargin(), getHeight());
        }
        private int leftMargin()        { return s58; }
        private int rightMargin()       { return s70; }
        private int buttonTopY()        { return s6; }
        private int buttonWidth()       { return s10; }
        private int buttonBottomY()     { return getHeight()-s7; }
        private int buttonMidY()        { return (buttonTopY()+buttonBottomY())/2; }
        private int boxLeftX()          { return leftMargin()+s10; }
        private int boxRightX()         { return getWidth()-rightMargin()-s10; }
        private int boxTopY()           { return s6; }
        private int boxBottomY()        { return getHeight()-s6; }
        private int boxBorderW()        { return s3; }

        private void decrement(boolean click) {
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;
            
            float prevTech = mapListener == null ? 0 : colony.totalPlanetaryResearch();
            if (colony.increment(category, -1)) {
                if (mapListener == null)
                    RotPUI.instance().techUI().resetPlanetaryResearch();
                else {
                    float techAdj = colony.totalPlanetaryResearch() - prevTech;
                    RotPUI.instance().techUI().adjustPlanetaryResearch(techAdj);
                    mapListener.repaintTechStatus();
                }
                if (click)
                    softClick();
                parent.repaint();
            }
            else if (click)
                misClick();
        }
        private void smoothMaxClick(boolean click, MouseEvent e) {
        	// Common start
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;
            if (colony.locked(category)) {
            	misClick();
            	return;
            }
            float prevTech = mapListener == null ? 0 : colony.totalPlanetaryResearch();

            // Specific optimizations
            colony.verifiedSmoothMaxSlider(category, e, true);

            // Common End
        	if (mapListener == null)
                RotPUI.instance().techUI().resetPlanetaryResearch();
            else {
                float techAdj = colony.totalPlanetaryResearch() - prevTech;
                RotPUI.instance().techUI().adjustPlanetaryResearch(techAdj);
                mapListener.repaintTechStatus();
            }
            	
            if (click)
                softClick();
            parent.repaint();
        }
        private void commonResultBox(boolean click, MouseEvent e) {
        	// Common start
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;
            if (colony.locked(category)) {
            	misClick();
            	return;
            }
            float prevTech = mapListener == null ? 0 : colony.totalPlanetaryResearch();
           	boolean hadsShipSpending = colony.allocation(Colony.SHIP) > 0;
        	
            // Specific optimizations
           	if (e.isShiftDown() && e.isControlDown()) { // Smooth Max Under test
            	boolean v2 = !options().useSmartRefit();
           		colony.verifiedSmoothMaxSlider(category, e, v2);
           	}
           	else if (e.isShiftDown()) { // Smart Max, clear the free spending
                colony.clearUnlockedSpending();
                if (e.isControlDown()) {
                	colony.redistributeReducedEcoSpending();
                }
                else {
                    int allocation = colony.allocationRemaining();
                    int allocationNeeded = colony.category(category).smartAllocationNeeded(e);
                    allocation = min(allocation, allocationNeeded);
                    if(allocation == 0 && category != RESEARCH)
                        allocation = colony.allocationRemaining();
                    colony.setAllocation(category, allocation);

                	if (category == ECOLOGY) {
                		colony.redistributeReducedEcoSpending();
                		colony.keepEcoLockedToClean = SwingUtilities.isMiddleMouseButton(e);
                	}
                	else {
                		colony.checkEcoAtClean();
                		colony.redistributeReducedEcoSpending();
                	}
                }
        	}
            // Refresh all
            else if (e.isControlDown()) {
            	colony.clearUnlockedSpending();
            	if (!colony.locked(ECOLOGY))
            		colony.allocation(ECOLOGY, colony.ecology().cleanupAllocationNeeded());
            	colony.redistributeSpending(-1, hadsShipSpending, true);
            }

            // Smooth Max
            else {
            	boolean v2 = options().useSmartRefit();
            	colony.verifiedSmoothMaxSlider(category, e, v2);
            	//colony.checkEcoAtClean();
            }

            // Common End
        	if (mapListener == null)
                RotPUI.instance().techUI().resetPlanetaryResearch();
            else {
                float techAdj = colony.totalPlanetaryResearch() - prevTech;
                RotPUI.instance().techUI().adjustPlanetaryResearch(techAdj);
                mapListener.repaintTechStatus();
            }
            	
            if (click)
                softClick();
            parent.repaint();
        }
        private void increment(boolean click) {
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;

            float prevTech = mapListener == null ? 0 : colony.totalPlanetaryResearch();
            if (colony.increment(category, 1)) {
                if (mapListener == null)
                    RotPUI.instance().techUI().resetPlanetaryResearch();
                else {
                    float techAdj = colony.totalPlanetaryResearch() - prevTech;
                    RotPUI.instance().techUI().adjustPlanetaryResearch(techAdj);
                    mapListener.repaintTechStatus();
                }
                if (click)
                    softClick();
                parent.repaint();
            }
            else if (click)
                misClick();
        }
        private void toggleLock() {
            softClick();
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;
            colony.toggleLock(category);
            repaint();
        }
        private void toggleOrder() {
            softClick();
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;	
    		colony.toggleOrder(category);
    		colony.governIfNeeded(category == SHIP);
            parent.repaint();
//	        switch (category) {
//		    	case DEFENSE:
//		    	case INDUSTRY:
//		    	case ECOLOGY:
//		    	case SHIP:
//		    		colony.toggleOrder(category);
//		            repaint();
//		    		return;
//	        }
        }
        @Override public void mouseClicked(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e)	{ clearHoverSprite(e, parent.mapHandler()); }
        @Override public void mouseExited(MouseEvent e)  {
			setModifierKeysState(e);
            if (hoverBox != null) {
                hoverBox = null;
                repaint();
            }
            if (!getVisibleRect().contains(e.getPoint()) ) {
            	GalaxyMapPanel.checkForEcoClean();
            }
        }
        @Override public void mousePressed(MouseEvent ev) { }
        @Override public void mouseReleased(MouseEvent e) {
            if (e.getButton() > 3)
                return;
            int x = e.getX();
            int y = e.getY();
            if (labelBox.contains(x,y))
            	if (e.isControlDown() || SwingUtilities.isRightMouseButton(e))
           		 	toggleOrder();
            	else
            		toggleLock();
            else if (leftArrow.contains(x,y))
                decrement(true);
            else if (rightArrow.contains(x,y))
                increment(true);
            else if (resultBox.contains(x,y))
            	commonResultBox(true, e);
            else if (this.category < 0) {
                if (governorBox.contains(x,y))
                	toggleGovernor();
                else if (optionsBox.contains(x,y))
                	if (e.isShiftDown()) {
                		ParamSubUI optionsUI = AllSubUI.governorSubUI();
                		optionsUI.start(null);
                	}
                	else
                		governorOptions();
            } else {
//                if (this.category < 0) {
//// TODO: for future use
////                    if (x < EmpireColonySpendingPane.this.getWidth() - s95) {
////                        toggleGovernor();
////                    } else if (x < EmpireColonySpendingPane.this.getWidth() - s60) {
////                        toggleAutoShips();
//                    if (x < EmpireColonySpendingPane.this.getWidth() - s60) {
//                        toggleGovernor();
//                    } else {
//                        governorOptions();
//                    }
//                }
                float pct = pctBoxSelected(x,y);
                if (pct >= 0) {
                    Colony colony = parent.systemViewToDisplay().colony();
                    if (!colony.canAdjust(category))
                        misClick();
                    else {
                        softClick();
                        // clicks near the edge of the box are typically trying
                        // to zero or max them out. Assume that.
                        if (pct < .05)
                            pct = 0;
                        else if (pct > .95)
                            pct = 1;
                        float prevTech = mapListener == null ? 0 : colony.totalPlanetaryResearch();
                        colony.forcePct(category, pct);
                        if (category == Colony.ECOLOGY)
                            colony.keepEcoLockedToClean = false;
                        if (mapListener == null)
                            RotPUI.instance().techUI().resetPlanetaryResearch();
                        else {
                            float techAdj = colony.totalPlanetaryResearch() - prevTech;
                            RotPUI.instance().techUI().adjustPlanetaryResearch(techAdj);
                            mapListener.repaintTechStatus();
                        }
                        parent.repaint();
                    }
                }
            }
        }
        @Override public void mouseDragged(MouseEvent e) { }
        @Override public void mouseMoved(MouseEvent e) {
			setModifierKeysState(e);
            int x = e.getX();
            int y = e.getY();

            Shape newHover = null;
            if (labelBox.contains(x,y))
                newHover = labelBox;
            else if (sliderBox.contains(x,y))
                newHover = sliderBox;
            else if (leftArrow.contains(x,y))
                newHover = leftArrow;
            else if (rightArrow.contains(x,y))
                newHover = rightArrow;
            else if (resultBox.contains(x,y))
                newHover = resultBox;
            else if (governorBox.contains(x,y))
                newHover = governorBox;
            else if (optionsBox.contains(x,y))
                newHover = optionsBox;

            if (newHover != hoverBox) {
                hoverBox = newHover;
                repaint();
            }
        }
        @Override public void mouseWheelMoved(MouseWheelEvent e) {
			setModifierKeysState(e);
            int rot = e.getWheelRotation();
            if (hoverBox == sliderBox) {
                if (rot > 0)
                    decrement(false);
                else if (rot  < 0)
                    increment(false);
            }
        }
        private float pctBoxSelected(int x, int y) {
            int bw = boxBorderW();
            int minX = sliderBox.x+bw;
            int maxX = sliderBox.x+sliderBox.width-bw;

            if ((x < minX)
            || (x > maxX)
            || (y < (boxTopY()-bw))
            || (y > (boxBottomY()+bw)))
                return -1;

            float num = x - minX;
            float den = maxX-minX;
            return num/den;
        }
    }
    // BR: made static because there is two instances of EmpireColonySpendingPane
    static GovernorFrame governorOptionsFrame = null;
    static void openGovernorOptions() {
    	
    }
    private void toggleGovernor() {
        if (parent.systemViewToDisplay() != null && parent.systemViewToDisplay().colony() != null) {
            Colony colony = parent.systemViewToDisplay().colony();
            colony.setGovernor(!colony.isGovernor());
            if (colony.isGovernor()) {
                colony.govern();
            }
            parent.repaint();
        }
    }
    private void toggleAutoShips() {
        if (parent.systemViewToDisplay() != null && parent.systemViewToDisplay().colony() != null) {
            Colony colony = parent.systemViewToDisplay().colony();
            colony.setAutoShips(!colony.isAutoShips());
            parent.repaint();
        }
    }
    public static void governorOptions() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                if (governorOptionsFrame == null) {
                    governorOptionsFrame = new GovernorFrame("GovernorOptions");
//                    {
//                    	{
//                    		addComponentListener(new GovernorComponentAdapter());
//                    	}
//                    };
                    governorOptionsFrame.addComponentListener(governorOptionsFrame.new GovernorComponentAdapter());
                    // make this window have an icon, same as main window
                    // modnar: change to cleaner icon set
                    List<Image> iconImages = new ArrayList<Image>();
                    iconImages.add(ImageManager.current().image("ROTP_MOD_ICON3"));
                    iconImages.add(ImageManager.current().image("ROTP_MOD_ICON2"));
                    iconImages.add(ImageManager.current().image("ROTP_MOD_ICON1"));
                    governorOptionsFrame.setIconImages(iconImages);
                    governorOptionsFrame.setDefaultCloseOperation(GovernorFrame.HIDE_ON_CLOSE);

                    //Create and set up the content pane.
                    GovernorOptionsPanel newContentPane = new GovernorOptionsPanel(governorOptionsFrame);
                    newContentPane.setOpaque(true); //content panes must be opaque
                    governorOptionsFrame.setContentPane(newContentPane);
                    newContentPane.applyStyle(); // Keep after set to the frame
                    if (UserPreferences.fullScreen() &&
                    		governorOptionsFrame.isAlwaysOnTopSupported())
                    	governorOptionsFrame.setAlwaysOnTop(true);
                }
                //Display the window.
                governorOptionsFrame.pack();
                governorOptionsFrame.setVisible(true);
                governorOptionsFrame.setLocation(GameSession.instance().getGovernorOptions().getPosition());
                ((GovernorOptionsPanel) governorOptionsFrame.getContentPane()).reOpen();
            }
        });
    }
}
