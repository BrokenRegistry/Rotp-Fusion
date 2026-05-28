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

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static rotp.model.colony.Colony.ECOLOGY;
import static rotp.model.colony.Colony.RESEARCH;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.SwingUtilities;

import rotp.model.colony.Colony;
import rotp.model.galaxy.StarSystem;
import rotp.ui.BasePanel;
import rotp.ui.SystemViewer;
import rotp.ui.main.EmpireColonySpendingPane;
import rotp.ui.main.SystemPanel;
import rotp.ui.options.AllSubUI;
import rotp.ui.util.ParamSubUI;

public class MultiColonySpendingPane extends BasePanel implements MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private static final Color buttonTextHasOrder	= new Color(0, 0, 255);
    private static final Color buttonTextHasOrderD	= new Color(65, 65, 192);
    private static final Color buttonTextUrged		= new Color(255, 0, 255);
    private static final Color buttonTextUrgedD		= new Color(142, 65, 192);
    private static final Color buttonTextMixed		= new Color(128, 0, 255);
    private static final Color buttonTextMixedD		= new Color(65, 32, 96);
	private static final Color textColorOn			= new Color(209, 209, 209);
	private static final Color textColorOff			= new Color(178, 124, 87);
	private static final Color buttonColor			= new Color(209, 209, 209);

	private static BufferedImage governorImage, noGovernorImage, mixedGovernorImage;
	private static int governorImageEmpireId;
	private static int govMaxW, optMaxW;
	private static int govButtonW, optButtonW;
	private static int govButtonX, optButtonX;
	private static int maxButtonFontSize = 17;
	private static int minButtonFontSize = 12;
	private static int govFontSize;
	private static int iconWidth	= s25;
	private static int buttonH		= s25;
	private static int buttonY;
	private static int buttonMargin	= s20;
	private static int buttonOffset	= s10;
	private static boolean needInitialization = true;
	private final Rectangle governorBox	= new Rectangle();
	private final Rectangle optionsBox	= new Rectangle();

	public static void resetPanel()	{ needInitialization = true; }

    private LinearGradientPaint greenBackC;
    private Color backC;
    private Rectangle spending0Box = new Rectangle();
    private Rectangle spending25Box = new Rectangle();
    private Rectangle spending50Box = new Rectangle();
    private Rectangle spending75Box = new Rectangle();
    private Rectangle spendingMaxBox = new Rectangle();
    private Rectangle[] catBox = new Rectangle[5];
    private Rectangle hoverBox;
    private int selectedCat = 0;

    private final SystemViewer parent;
    MultiColonySpendingPane(SystemViewer p, Color c0, Color text, Color hi, Color lo) {
        parent = p;
        backC = c0;
        init();
    }
    @Override
    public String textureName()            { return parent.subPanelTextureName(); }
    private void init() {
        for (int i=0;i<catBox.length;i++)
            catBox[i] = new Rectangle();

        setOpaque(true);
        addMouseListener(this);
        addMouseMotionListener(this);
        setBackground(backC);
    }
	private void reinitPanel(Graphics2D g)	{
		buttonY = getHeight() - buttonH - s1;
		int w = getWidth();
		initGovernorImage();
		initNoGovernorImage();
		initMixedGovernorImage();
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
		x2 = w - buttonOffset;
		optButtonX = x2 - optButtonW;
		needInitialization = false;
	}
	private BufferedImage governorImage()	{
		if (governorImage == null || governorImageEmpireId != player().id) {
			initGovernorImage();
			initMixedGovernorImage();
		}
		return governorImage;
	}
	private BufferedImage mixedGovernorImage()	{
		if (mixedGovernorImage == null || governorImageEmpireId != player().id) {
			initGovernorImage();
			initMixedGovernorImage();
		}
		return mixedGovernorImage;
	}
	private void initMixedGovernorImage()	{
		governorImageEmpireId = player().id;
		int mugH = s20;
		int mugW = mugH;
		mixedGovernorImage = new BufferedImage(mugW, mugH, TYPE_INT_ARGB);
		Graphics2D g = mixedGovernorImage.createGraphics();
		g.setComposite(AlphaComposite.SrcOver);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		Image img = player().flagNorm();
		int imgW = img.getWidth(null);
		int imgH = img.getHeight(null);
		g.setColor(Color.darkGray);
		g.fillRoundRect(0, 0, mugW, mugH, s8, s8);
		g.drawImage(img, 0, 0, mugW, mugH, 0, 0, imgW, imgH, null);
		g.dispose();
	}
	private void initGovernorImage()		{
		governorImageEmpireId = player().id;
		int mugH = s20; // s82
		int mugW = mugH * 76 / 82; // s76
		governorImage = new BufferedImage(mugW, mugH, TYPE_INT_ARGB);
		Graphics2D g = governorImage.createGraphics();
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
	private void initNoGovernorImage()		{
		int mugH = s22;
		int mugW = mugH;
		noGovernorImage = new BufferedImage(mugW, mugH, TYPE_INT_ARGB);
		Graphics2D g = noGovernorImage.createGraphics();
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
	private int initButtons(Graphics2D g, int maxFont)	{
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
	private int initOptionsButtonW(Graphics2D g, int maxWidth, int maxFont)	{
		int maxTitleWidth = maxWidth - buttonMargin;
		String title = text("GOVERNOR_OPTIONS");
		int fontSize = scaledFont(g, title, maxTitleWidth, maxFont, minButtonFontSize);
		int titleW   = g.getFontMetrics().stringWidth(title);
		optButtonW = titleW + buttonMargin;
		return fontSize;
	}
	private void drawGovernorButton(Graphics2D g, boolean hovered)	{
		Color borderC, titleC;
		Boolean isGovernor = isGovernor(null);
		String title;
		if (isGovernor == null)
			title = text("GOVERNOR_IS_ON_AND_OFF_BUTTON");
		else if (isGovernor)
			title = text("GOVERNOR_IS_ON_BUTTON");
		else
			title = text("GOVERNOR_IS_OFF_BUTTON");
		int titleDx = iconWidth/2;
		g.setFont(narrowFont(govFontSize));
		Rectangle2D bound = g.getFontMetrics().getStringBounds(title, g);

		int titleW     = (int) Math.ceil(bound.getWidth());
		double titleH  = bound.getHeight();
		double descent = g.getFontMetrics().getDescent();
		double titleDy = (buttonH-titleH)/2;
		int titleX = round(govButtonX + (govButtonW-titleW)/2) - titleDx;
		int titleY = round(buttonY + buttonH - descent-titleDy);

		if (hovered) {
			borderC = SystemPanel.yellowText;
			titleC  = SystemPanel.yellowText;
		}
		else {
			borderC = buttonColor;
			if (isGovernor == null)
				titleC = textColorOff;
			else if (isGovernor)
				titleC = textColorOn;
			else
				titleC = textColorOff;
		}
		governorBox.setBounds(govButtonX, buttonY, govButtonW, buttonH);

		Stroke prevStroke = g.getStroke();
		g.setStroke(stroke2);
		g.setColor(borderC);
		g.drawRect(govButtonX, buttonY, govButtonW, buttonH);
		g.setStroke(prevStroke);
		g.setColor(titleC);
		g.drawString(title, titleX, titleY);

		if (isGovernor == null)
			g.drawImage(mixedGovernorImage(), govButtonX+govButtonW-iconWidth, buttonY+s3, null);
		else if (isGovernor)
			g.drawImage(governorImage(), govButtonX+govButtonW-iconWidth, buttonY+s3, null);
		else
			g.drawImage(noGovernorImage, govButtonX+govButtonW-iconWidth, buttonY+s2, null);
	}
	private void drawOptionsButton(Graphics2D g, boolean hovered)	{
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
		g.drawRect(optButtonX, buttonY, optButtonW, buttonH);
		g.setStroke(prevStroke);
		
		g.setColor(titleC);
		g.drawString(title, titleX, titleY);
	}
	@Override public void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        super.paintComponent(g);
		if (needInitialization)
			reinitPanel(g);
        int w = getWidth();
        
        if (greenBackC == null)
            initGradients();

    	
        g.setFont(narrowFont(20));
//        String titleText = text("MAIN_COLONY_ALLOCATE_SPENDING");
//        drawShadowedString(g, titleText, 2, s5, y0, MainUI.shadeBorderC(), textC);

		buttonY = s8;
		drawGovernorButton(g, hoverBox == governorBox);
		drawOptionsButton(g, hoverBox == optionsBox);
		int y0 = 2 * buttonY + buttonH;

        int n = catBox.length;
        int gap = s5;
        int w0 = (w-s20-((n-1)*gap))/n;

        int x0 = s10;
        int h0 = s20;
        //y0 += s7;
        
        boolean showPendingOrders = options().showPendingOrders();
        for (int i=0;i<n;i++) {
        	drawSpendingButton(g, i, showPendingOrders, x0, y0, w0, h0);
            x0 = x0+w0+gap;
        }

		int buttonSep = s29;
        String catName = text(Colony.categoryName(selectedCat));
        y0 += buttonSep;
        drawGreenButton(g,text("PLANETS_SPENDING_0",catName), spending0Box, hoverBox, y0);
        y0 += buttonSep;
        drawGreenButton(g,text("PLANETS_SPENDING_25",catName), spending25Box, hoverBox, y0);
        y0 += buttonSep;
        drawGreenButton(g,text("PLANETS_SPENDING_50",catName), spending50Box, hoverBox, y0);
        y0 += buttonSep;
        drawGreenButton(g,text("PLANETS_SPENDING_75",catName), spending75Box, hoverBox, y0);
        y0 += buttonSep;
        drawGreenButton(g,text("PLANETS_SPENDING_MAX",catName), spendingMaxBox, hoverBox, y0);
        y0 += buttonSep;

        y0 += s12;
        g.setColor(SystemPanel.blackText);            
        String desc = text("FLEETS_ADJUST_SPENDING_DESC2");
        if (!player().ignoresPlanetEnvironment())
            desc = desc + " " + text("FLEETS_ADJUST_SPENDING_DESC3");
        g.setFont(narrowFont(14));
        List<String> descLines = wrappedLines(g, desc, w-s20);
        for (String line: descLines) {
            drawString(g,line, s10, y0);
            y0 += s15;
        }
    }
	private Boolean isGovernor(Boolean yesAndNo)	{
		List<StarSystem> systems = parent.systemsToDisplay();
		boolean yes = false;
		boolean no  = false;
		for (StarSystem sys: systems) {
			if (sys.colony().isGovernor())
				yes = true;
			else
				no = true;
			if (yes && no)
				return yesAndNo;
		}
		return yes;
	}
	private Boolean isUrged(List<StarSystem> systems, int cat)	{
		boolean yes = false;
		boolean no  = false;
		for (StarSystem sys: systems) {
			if (sys.colony().isUrged(cat))
				yes = true;
			else
				no = true;
			if (yes && no)
				return null;
		}
		return yes;
	}
	private Boolean hasOrder(List<StarSystem> systems, int cat)	{
		boolean yes = false;
		boolean no  = false;
		for (StarSystem sys: systems) {
			if (sys.colony().hasOrder(cat))
				yes = true;
			else
				no = true;
			if (yes && no)
				return null;
		}
		return yes;
	}
    private void drawSpendingButton(Graphics2D g, int cat, boolean showPendingOrders, int x, int y, int w, int h) {
		String label = text(Colony.categoryName(cat));
		Rectangle actionBox = catBox[cat];
		boolean selected = cat == selectedCat;
		Boolean hasOrder = false;
		Boolean isUrged  = false;
		boolean isMixed  = false;
		List<StarSystem> systems = parent.systemsToDisplay();
		if (showPendingOrders) {
			hasOrder = hasOrder(systems, cat);
			isUrged  = isUrged(systems, cat);
			isMixed  = (hasOrder == null) || (isUrged == null) || (isUrged && hasOrder);
			if (isMixed) {
				hasOrder = false;
				isUrged  = false;
			}
		}

		actionBox.setBounds(x, y, w, h);
		g.setColor(SystemPanel.buttonShadowC);
		g.fillRoundRect(x+s1, y+s3, w, h, s8, s8);
		g.fillRoundRect(x+s2, y+s4, w, h, s8, s8);

		g.setPaint(greenBackC);
		g.fillRoundRect(x, y, w, h, s5, s5);

		boolean hovering = actionBox == hoverBox;
		Color c0, c1;
		int shadow = 1;
		if (hovering) {
			c0 = Color.yellow;
			c1 = c0;
		}

		else if (selected) {
			c1 = Color.white;
			if (isMixed)
				c0 = buttonTextMixed;
			else if (isUrged)
				c0 = buttonTextUrged;
			else if (hasOrder)
				c0 = buttonTextHasOrder;
			else {
				c0 = Color.white;
				c1 = SystemPanel.textShadowC;
				shadow = 3;
			}
		}
		else {
			c1 = SystemPanel.grayText;
			if (isMixed)
				c0 = buttonTextMixedD;
			else if (isUrged)
				c0 = buttonTextUrgedD;
			else if (hasOrder)
				c0 = buttonTextHasOrderD;
			else {
				c0 = SystemPanel.grayText;
				c1 = SystemPanel.textShadowC;
				shadow = 3;
			}
		}

		g.setFont(narrowFont(16));
		int sw = g.getFontMetrics().stringWidth(label);
		int x0 = x+((w-sw)/2);
		drawShadowedString(g, label, shadow, x0, y+h-s5, c1, c0);

		g.setColor(c0);
		Stroke prev2 = g.getStroke();
		g.setStroke(stroke1);
		g.drawRoundRect(x+s1, y, w-s2, h, s5, s5);
		g.setStroke(prev2);
	}
    private void drawGreenButton(Graphics2D g, String label, Rectangle actionBox, Shape hoverBox, int y) {
        int buttonH = s24;
        int x1 = s10;
        int w1 = getWidth()-s20;
        if (actionBox != null)
            actionBox.setBounds(x1,y,w1,buttonH);
        g.setColor(SystemPanel.buttonShadowC);
        g.fillRoundRect(x1+s1,y+s3,w1,buttonH,s8,s8);
        g.fillRoundRect(x1+s2,y+s4,w1,buttonH,s8,s8);

        g.setPaint(greenBackC);
        g.fillRoundRect(x1,y,w1,buttonH,s5,s5);

        boolean hovering = (actionBox != null) && (actionBox == hoverBox);
        Color c0 = (actionBox == null) ? SystemPanel.grayText : hovering ? Color.yellow : SystemPanel.whiteText;

        g.setFont(narrowFont(16));
        int sw = g.getFontMetrics().stringWidth(label);
        int x0 = x1+((w1-sw)/2);
        drawShadowedString(g, label, 3, x0, y+buttonH-s7, SystemPanel.textShadowC, c0);

        g.setColor(c0);
        Stroke prev2 = g.getStroke();
        g.setStroke(stroke1);
        g.drawRoundRect(x1+s1,y,w1-s2,buttonH,s5,s5);
        g.setStroke(prev2);
    }
    private void initGradients() {
        int w = getWidth();
        int leftM = s2;
        int rightM = w-s2;
        Point2D start = new Point2D.Float(leftM, 0);
        Point2D end = new Point2D.Float(rightM, 0);
        float[] dist = {0.0f, 0.5f, 1.0f};

        Color greenEdgeC = new Color(44,59,30);
        Color greenMidC = new Color(71,93,48);
        Color[] greenColors = {greenEdgeC, greenMidC, greenEdgeC };

        greenBackC = new LinearGradientPaint(start, end, dist, greenColors);
    }
    void selectCat(int i) {
        if (i != selectedCat) {
            selectedCat = i;
            repaint();
        }
    }
	private void toggleCatOrders(int cat) {
		selectedCat = cat;
		List<StarSystem> systems = parent.systemsToDisplay();
		Boolean hasOrder = hasOrder(systems, cat);
		Boolean isUrged  = isUrged(systems, cat);
		boolean isMixed  = (hasOrder == null) || (isUrged == null) || (isUrged && hasOrder);
		if (isMixed)
			for (StarSystem sys: systems) {
				Colony colony = sys.colony();
				colony.removeOrder(cat);
				colony.governIfNeeded();
			}
		else
			for (StarSystem sys: systems) {
				Colony colony = sys.colony();
				colony.toggleOrder(cat);
				colony.governIfNeeded();
			}
		repaint();
	}

	void toggleGovernor()	{ setGovernor(!isGovernor(false)); }
    public void setGovernor(boolean gov) {
        List<StarSystem> systems = parent.systemsToDisplay();
        for (StarSystem sys: systems) {
            Colony c = sys.colony();
            if (c != null) {
                c.setGovernor(gov);
                if (c.isGovernor()) {
                    c.govern();
                }
            }
        }
        parent.repaintAll();
    }
    void increaseBase(InputEvent e) {
        List<StarSystem> systems = parent.systemsToDisplay();
        for (StarSystem sys: systems) {
            Colony c = sys.colony();
            if (c != null) {
            	c.defense().incrMaxBases(1, false, false);
            	c.governIfNeeded();
            	//c.defense().incrMaxBases(1, e.isShiftDown(), e.isControlDown());
            }
        }
        parent.repaintAll();
    }
    void decreaseBase(InputEvent e) {
        List<StarSystem> systems = parent.systemsToDisplay();
        for (StarSystem sys: systems) {
            Colony c = sys.colony();
            if (c != null) {
            	c.defense().incrMaxBases(-1, false, false);
            	c.governIfNeeded();
            	//c.defense().incrMaxBases(-1, e.isShiftDown(), e.isControlDown());
            }
        }
        parent.repaintAll();
    }
    public void setSpendingLevel(float pct) {
        List<StarSystem> systems = parent.systemsToDisplay();
        for (StarSystem sys: systems) {
            Colony c = sys.colony();
            if (c != null) {
                c.forcePct(selectedCat, pct);
                c.ensureMinimumCleanup();
            }
        }
        parent.repaintAll();
    }
    void smoothSmartMax() {
        List<StarSystem> systems = parent.systemsToDisplay();
        for (StarSystem sys: systems) {
            Colony colony = sys.colony();
            if (colony != null) {
            	if(!colony.locked(selectedCat))
                    colony.smoothMaxSlider(selectedCat);
                if(selectedCat != ECOLOGY)
                	colony.checkEcoAtClean();
                if(selectedCat != RESEARCH && !colony.locked(RESEARCH))
                	colony.allocation(RESEARCH, 0);
                colony.redistributeReducedEcoSpending();
            }
        }
        parent.repaintAll();
    }

    void setLock(int cat, boolean lock) {
        List<StarSystem> systems = parent.systemsToDisplay();
        for (StarSystem sys: systems) {
            Colony c = sys.colony();
            c.locked(cat, lock);
        }
        parent.repaintAll();
    }
	@Override public void mouseClicked(MouseEvent arg0) { parent.enterCurrentPane(this); }
	@Override public void mouseEntered(MouseEvent arg0) { parent.enterCurrentPane(this); }
	@Override public void mouseExited(MouseEvent arg0) {
		parent.exitCurrentPane(this);
        if (hoverBox != null) {
            hoverBox = null;
            repaint();
        }
    }
	@Override public void mousePressed(MouseEvent ev) { parent.enterCurrentPane(this); }
	@Override public void mouseReleased(MouseEvent e) {
		parent.enterCurrentPane(this);
        if (e.getButton() > 3)
            return;

		for (int i=0;i<catBox.length;i++) {
			if (hoverBox == catBox[i]) {
				if (SwingUtilities.isRightMouseButton(e))
					toggleCatOrders(i);
				else
					selectCat(i);
				return;
			}
		}
        if (hoverBox == spending0Box) 
            setSpendingLevel(0);
        else if (hoverBox == spending25Box) 
            setSpendingLevel(0.25f);
        else if (hoverBox == spending50Box) 
            setSpendingLevel(0.5f);
        else if (hoverBox == spending75Box) 
            setSpendingLevel(0.75f);
        else if (hoverBox == spendingMaxBox) 
            setSpendingLevel(1);
		else if (hoverBox == governorBox)
			toggleGovernor();
		else if (hoverBox == optionsBox)
			if (e.isShiftDown()) {
				ParamSubUI optionsUI = AllSubUI.governorSubUI();
				optionsUI.start(null);
			}
			else
				EmpireColonySpendingPane.governorOptions();
    }
	@Override public void mouseDragged(MouseEvent arg0) { parent.enterCurrentPane(this); }
	@Override public void mouseMoved(MouseEvent e) {
		parent.enterCurrentPane(this);
        int x = e.getX();
        int y = e.getY();

        Rectangle newHover = null;
		if (governorBox.contains(x,y))
			newHover = governorBox;
		else if (optionsBox.contains(x,y))
			newHover = optionsBox;
		else if (spending0Box.contains(x,y))
            newHover = spending0Box;
        else if (spending25Box.contains(x,y))
            newHover = spending25Box;
        else if (spending50Box.contains(x,y))
            newHover = spending50Box;
        else if (spending75Box.contains(x,y))
            newHover = spending75Box;
        else if (spendingMaxBox.contains(x,y))
            newHover = spendingMaxBox;
        else {
            for (int i=0;i<catBox.length;i++) {
                if (catBox[i].contains(x,y)) {
                    newHover = catBox[i];
                    break;
                }
            }
        }

        if (newHover != hoverBox) {
            hoverBox = newHover;
            repaint();
        }
    }
}