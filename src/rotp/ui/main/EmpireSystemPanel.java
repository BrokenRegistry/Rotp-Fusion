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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints; // modnar: needed for adding RenderingHints
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import rotp.model.colony.Colony;
import rotp.model.colony.ColonyShipyard;
import rotp.model.empires.SystemView;
import rotp.model.galaxy.StarSystem;
import rotp.model.ships.Design;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipLibrary;
import rotp.ui.BasePanel;

public class EmpireSystemPanel extends SystemPanel {
    private static final long serialVersionUID = 1L;
    static final Color sliderButtonColor = new Color(153,0,11);
    static final Color sliderHighlightColor = new Color(255,255,255);
    static final Color spendingPaneHighlightColor = new Color(96,96,224);
    static final Color productionGreenColor = new Color(89, 240, 46);
    static final Color enabledArrowColor = Color.black;
    static final Color disabledArrowColor = new Color(65,65,65);

    static final Color darkBrown = new Color(45,14,5);
    static final Color brown = new Color(64,24,13);
    static final Color sliderBoxBlue = new Color(34,140,142);
    private static final Color gray20C  = new Color(20,20,20);
    private static final Color buttonC  = new Color(110,110,110);
    private static final Color gray70C  = new Color(70,70,70);
    private static final Color gray90C  = new Color(90,90,90);
    private static final Color gray115C = new Color(115,115,115);
    // private static final Color gray150C = new Color(150,150,150);
    private static final Color gray175C = new Color(175,175,175);
    private static final Color gray190C = new Color(190,190,190);

    private SystemViewInfoPane topPane;
    private EmpireColonySpendingPane spendingPane;
    private EmpireShipPane shipPane;
    private EmpireColonyFoundedPane foundedPane;
    private EmpireColonyInfoPane infoPane;
    private boolean lastAltIsDown = false;
	private BasePanel currentPane = null;

    public EmpireSystemPanel(SpriteDisplayPanel p) {
        parentSpritePanel = p;
        init();
    }
	private void init()	{ initModel(); }
	@Override public void enterCurrentPane(BasePanel pane)	{ currentPane = pane; }
	@Override public void exitCurrentPane(BasePanel pane)	{
		if (currentPane == pane)
			currentPane = null;
	}
	private void repaintOnAltChange() {
		if (lastAltIsDown == isAltDown())
			return;
		lastAltIsDown = !lastAltIsDown;
		if (currentPane != null)
			currentPane.repaint();
		else
			repaint();
	}
    public void releaseObjects() { }

    @Override
    public String subPanelTextureName()    { return TEXTURE_GRAY; }
    @Override
    protected void showDefaultDetail()  { detailLayout.show(detailCardPane, EMPIRE_DETAIL);  }
    @Override
    protected void showStarDetail()     { detailLayout.show(detailCardPane, STAR_DETAIL); }
    @Override
    protected void showPlanetDetail()   { detailLayout.show(detailCardPane, PLANET_DETAIL); }

    @Override
    public void animate() {
        topPane.animate();
    }
    @Override
    public void keyPressed(KeyEvent e) {
    	setModifierKeysState(e); // BR: For the Flag color selection
    	repaintOnAltChange();
        int k = e.getKeyCode();
        boolean shift = e.isShiftDown();
        boolean isAltDown = e.isAltDown();
        switch (k) {
            case KeyEvent.VK_B:
                if (shift)
                	infoPane.basesPane.incrBases(-1, false, false);
                else
                	infoPane.basesPane.incrBases(1, false, false);
                return;
            case KeyEvent.VK_F:
                foundedPane.toggleFlagColor(shift);
                return;
            case KeyEvent.VK_S:
                nextShipDesign();
                return;
            case KeyEvent.VK_Q:
            case KeyEvent.VK_1:
            case KeyEvent.VK_2:
            case KeyEvent.VK_3:
            case KeyEvent.VK_4:
            case KeyEvent.VK_5:
                spendingPane.keyPressed(e);
                return;
            case KeyEvent.VK_L:
            	if (isAltDown) {
            		debugReloadLabels(this);
            		return;
            	}
            	misClick();
            	return;
        }
    }
	@Override public void keyReleased(KeyEvent e) {
		setModifierKeysState(e);
		repaintOnAltChange();
	}
    public void nextShipDesign() {
        StarSystem sys = parentSpritePanel.systemViewToDisplay();
        if (sys == null)
            return;
        Colony c = sys.colony();
        if (c == null)
            return;
        if (!c.shipyard().canCycleDesign()) 
            misClick();
        else {
            softClick();
            c.shipyard().goToNextDesign();
            if (c.shipyard().buildLimit()>0)
            	c.governIfNeeded();
            parentSpritePanel.repaint();
        }
    }
    public void prevShipDesign() {
        StarSystem sys = parentSpritePanel.systemViewToDisplay();
        if (sys == null)
            return;
        Colony c = sys.colony();
        if (c == null)
            return;
        if (!c.shipyard().canCycleDesign()) {
            misClick();
        }
        else {
            softClick();
            c.shipyard().goToPrevDesign();
            if (c.shipyard().buildLimit()>0)
            	c.governIfNeeded();
            parentSpritePanel.repaint();
        }
    }
    @Override
    protected BasePanel topPane() {
        if (topPane == null)
            topPane = new SystemViewInfoPane(this);
        return topPane;
    }
    @Override
    protected BasePanel detailPane() {
        BasePanel detailTopPane = new BasePanel();
        detailTopPane.setOpaque(true);
        detailTopPane.setBackground(dataBorders);

        foundedPane = new EmpireColonyFoundedPane(this, parentSpritePanel.parent, MainUI.paneBackground());
        infoPane = new EmpireColonyInfoPane(this, MainUI.paneBackground(), dataBorders, SystemPanel.yellowText, SystemPanel.blackText);
        BorderLayout layout = new BorderLayout();
        layout.setVgap(s1);
        detailTopPane.setLayout(layout);
        detailTopPane.setPreferredSize(new Dimension(getWidth(),scaled(110)));
        detailTopPane.add(foundedPane, BorderLayout.NORTH);
        detailTopPane.add(infoPane, BorderLayout.CENTER);
        Color textC = new Color(204,204,204);
        spendingPane = new EmpireColonySpendingPane(this,
        		MainUI.paneBackground(), textC, labelBorderHi, labelBorderLo,
        		gray20C, gray90C, gray175C, gray115C, gray175C, true);
        if (parentSpritePanel.parent != null)  
            spendingPane.mapListener(parentSpritePanel.parent.map());
        shipPane = new EmpireShipPane(this);

        BasePanel empireDetailPane = new BasePanel();
        empireDetailPane.setOpaque(false);
        BorderLayout layout0 = new BorderLayout();
        empireDetailPane.setLayout(layout0);
        empireDetailPane.add(detailTopPane, BorderLayout.NORTH);
        empireDetailPane.add(spendingPane, BorderLayout.CENTER);
        empireDetailPane.add(shipPane, BorderLayout.SOUTH);

        detailCardPane = new JPanel();
        detailCardPane.setOpaque(false);
        detailCardPane.setLayout(detailLayout);
        detailCardPane.add(empireDetailPane, EMPIRE_DETAIL);
        detailLayout.show(detailCardPane, EMPIRE_DETAIL);

        BasePanel pane = new BasePanel();
        pane.setLayout(new BorderLayout());
        pane.setBackground(MainUI.paneBackground());
        pane.add(detailCardPane, BorderLayout.CENTER);

        return pane;
    }
    class EmpireShipPane extends BasePanel implements MouseListener, MouseMotionListener, MouseWheelListener {
        private static final long serialVersionUID = 1L;
        private final EmpireSystemPanel parent;
        // polygon coordinates for left & right increment buttons
        private final int leftButtonX[] = new int[3];
        private final int leftButtonY[] = new int[3];
        private final int rightButtonX[] = new int[3];
        private final int rightButtonY[] = new int[3];

        private final Rectangle shipDesignBox = new Rectangle();
        private final Rectangle rallyPointBox = new Rectangle();
        private final Rectangle rallyToSGBox  = new Rectangle();
        private final Rectangle transportBox  = new Rectangle();
        private final Rectangle abandonBox  = new Rectangle();
        private final Rectangle shipNameBox = new Rectangle();
        private final Polygon prevDesign = new Polygon();
        private final Polygon nextDesign = new Polygon();
        private Shape hoverBox;

        private final Polygon upArrow = new Polygon();
        private final Polygon downArrow = new Polygon();
        private final int upButtonX[] = new int[3];
        private final int upButtonY[] = new int[3];
        private final int downButtonX[] = new int[3];
        private final int downButtonY[] = new int[3];
        private boolean inGovLimitBox = false;
        private Rectangle limitBox = new Rectangle();
        private boolean hasPreview = false;

        Color textColor = newColor(204,204,204);
        Color darkShadingC = newColor(50,50,50);
        EmpireShipPane(EmpireSystemPanel p) {
            parent = p;
            init();
        }
        private void init() {
            setBackground(MainUI.paneBackground());
            setPreferredSize(new Dimension(getWidth(), scaled(150)));
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }
        @Override
        public String textureName()            { return TEXTURE_GRAY; }
        @Override
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            int w = getWidth();
            int h = getHeight();

            int midMargin = scaled(105);

            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            Colony col = sys == null ? null : sys.colony();
            if (col == null)
                return;

            drawTitle(g);
            drawShipIcon(g, col, s5,s30,midMargin-s15,s75);
            drawShipCompletion(g, col, midMargin,h-scaled(116),w-s10-midMargin,s30);
            drawNameSelector(g, col, midMargin,h-scaled(103),w-s10-midMargin,s30);
           	int wR = w-s10-midMargin;
           	int yR = h-s70;

            if (rallyToSGEnabled()) {
            	wR -= s25;
             	int x2 = midMargin + wR + s5;
            	drawRallyToSG(g, x2, yR, s25 ,s25);            	
            }
            else
            	rallyToSGBox.setBounds(0, 0, 0, 0);
           	drawRallyPointButton(g, midMargin, yR, wR, s25);

            // 60:40 width ratio for "Send Transports" & "Abandon" buttons
            int w0 = (w-s5)*12/20;
            int w1 = (w-s5)*8/20;
            g.setColor(MainUI.shadeBorderC());
            g.fillRect(0, h-s35, w, s35);
            int maxSize = 20;
            int transportSize = drawTransportButton(g, 0, h-s35, w0, s35, maxSize);
            int abandonSize   = drawAbandonButton(g, w0+s5, h-s35, w1, s35, transportSize);
            if (abandonSize < transportSize) // Same text size for the two buttons
            	drawTransportButton(g,0,h-s35,w0,s35, maxSize);
        }
        private boolean hasStargate()	{ return player().stargateCostPerBC()>0; }
        private void drawTitle(Graphics g) {
            g.setColor(MainUI.shadeBorderC());
            g.fillRect(0, 0, getWidth(), s3);
            g.setFont(narrowFont(20));
            g.setColor(Color.black);
            String str = text("MAIN_COLONY_SHIPYARD_CONSTRUCTION");
            scaledFont(g, str, getWidth()-s10, 20, 16);
            drawShadowedString(g, str, 2, s5, s22, MainUI.shadeBorderC(), textColor);
        }
        private void drawShipIcon(Graphics2D g,  Colony c, int x, int y, int w, int h) {
			// modnar: draw ship design icons in System information panel on main map screen
            g.setColor(Color.black);
            g.fillRect(x, y, w, h);
            shipDesignBox.setBounds(x,y,w,h);
            g.drawImage(initializedBackgroundImage(w, h), x,y, null);
            if (c == null)
                return;

            // draw design image
            Design d = c.shipyard().design();
            Image img = d.image();
            if (img == null)
                return;
            int w0 = img.getWidth(null);
            int h0 = img.getHeight(null);
            float scale = min((float)w/w0, (float)h/h0);

            int w1 = (int)(scale*w0);
            int h1 = (int)(scale*h0);
            int x1 = x+(w - w1) / 2;
            int y1 = y+(h - h1) / 2;
			// modnar: one-step progressive image downscaling, slightly better
			// there should be better methods
			if (scale < 0.5) {
				BufferedImage tmp = new BufferedImage(w0/2, h0/2, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2D = tmp.createGraphics();
				g2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2D.drawImage(img, 0, 0, w0/2, h0/2, 0, 0, w0, h0, this);
				g2D.dispose();
				img = tmp;
				w0 = img.getWidth(null);
				h0 = img.getHeight(null);
				scale = scale*2;
			}
			// modnar: use (slightly) better downsampling
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g.drawImage(img, x1, y1, x1+w1, y1+h1, 0, 0, w0, h0, this);
			// Draw default design
			if (d.isDefaultDesign()) {
				img = globalDefaultDesignIcon(s10, Color.green);
				g.drawImage(img, x+s2, y+s2, null);
			}

            if (hoverBox == shipDesignBox) {
                Stroke prev = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(SystemPanel.yellowText);
                g.drawRect(x, y, w, h);
                g.setStroke(prev);
            }

            // draw expected build count
            String count;
            if (d.scrapped()) {
                count = text("MAIN_COLONY_SHIP_SCRAPPED");
                g.setColor(Color.red);
                g.setFont(narrowFont(20));
            }
            else {
                int i = c.shipyard().upcomingShipCount();
                if (i == 0)
                    return;
                count = Integer.toString(i);
                g.setColor(SystemPanel.yellowText);
                g.setFont(narrowFont(20));
            }

            int sw = g.getFontMetrics().stringWidth(count);
            drawString(g,count, x+w-s5-sw, y+h-s5);
        }
        private void drawShipCompletion(Graphics2D g, Colony c, int x, int y, int w, int h) {
            if (c == null)
                return;
            //govLimitBox.setBounds(x, y, w, h);
            int buildLimit = c.shipyard().buildLimit();
            int buildSparePct = c.govShipBuildSparePct();
            int buildUsePct   = 100 - buildSparePct;
            boolean noSparePct = buildSparePct != 0;
            boolean showBuildSparePct = isAltDown() && inGovLimitBox;

            String label = showBuildSparePct ?
            		text("MAIN_COLONY_SHIPYARD_GOV_PCT") :
            		text("MAIN_COLONY_SHIPYARD_LIMIT");
            String none = text("MAIN_COLONY_SHIPYARD_LIMIT_NONE");
            String amt = showBuildSparePct ?
            		text("MAIN_COLONY_SHIPYARD_GOV_PCT_VAL", buildUsePct) :
            		c.shipyard().buildLimitStr();

			// Dynamic font are required for some languages
			// First determine longest text
			g.setFont(narrowFont(16));
			int sw2 = g.getFontMetrics().stringWidth(none);
			int sw3 = g.getFontMetrics().stringWidth(amt);
			String str = label + (sw2>sw3? none : amt);
			// get the font size
			int dx = s12;
			int maxWidth = w - s24;
			int fontSize = scaledFont(g, str, maxWidth, 16, 8);
			// Shift the left position for small font
			if (fontSize < 16) {
				dx = 0;
				maxWidth = w - s12;
				fontSize = scaledFont(g, str, maxWidth, 16, 8);
			}
			//System.out.println("scaled font size = " + fontSize); // TO DO BR: Remove
			int sw1 = g.getFontMetrics().stringWidth(label);
			sw2 = g.getFontMetrics().stringWidth(none);
			sw3 = g.getFontMetrics().stringWidth(amt);

			int x1 = x + dx;
			int y1 = y + s8;
			int x2 = x1 + sw1 + s5;
			int x3 = x1 + sw1 + s5 + max(sw2,sw3) + s5;
			int y3 = y1 + s2;
            if (noSparePct && (c.isGovernor() || showBuildSparePct)) {
            	int r = 100 + (120*buildSparePct)/100;
            	g.setColor(new Color(r, r/3, 0));
            }
            else
            	g.setColor(Color.black);
            drawString(g,label, x1, y1);
            g.setColor(Color.black);
            drawString(g,amt, x2, y1);  
            
            limitBox.setBounds(x2-s3,y1-s15,x3-x2,s18);
            if (hoverBox == limitBox) {
                Stroke prevStroke = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(SystemPanel.yellowText);
                g.draw(limitBox);
                g.setStroke(prevStroke);
            }

            upButtonX[0] = x3+s6; upButtonX[1] = x3; upButtonX[2] = x3+s12;
            upButtonY[0] = y3-s17; upButtonY[1] = y3-s9; upButtonY[2] = y3-s9;

            downButtonX[0] = x3+s6; downButtonX[1] = x3; downButtonX[2] = x3+s12;
            downButtonY[0] = y3; downButtonY[1] = y3-s8; downButtonY[2] = y3-s8;

            g.setColor(enabledArrowColor);
            g.fillPolygon(upButtonX, upButtonY, 3);

            if (!showBuildSparePct && buildLimit == 0)
                g.setColor(disabledArrowColor);
            else
                g.setColor(enabledArrowColor);
            g.fillPolygon(downButtonX, downButtonY, 3);

            upArrow.reset();
            downArrow.reset();
            for (int i=0;i<upButtonX.length;i++) {
                upArrow.addPoint(upButtonX[i], upButtonY[i]);
                downArrow.addPoint(downButtonX[i], downButtonY[i]);
            }
            Stroke prevStroke = g.getStroke();
            g.setStroke(stroke2);
            if (hoverBox == upArrow) {
                g.setColor(SystemPanel.yellowText);
                g.drawPolygon(upArrow);
            }
            else if ((hoverBox == downArrow)
                && (buildLimit > 0 || showBuildSparePct)) {
                g.setColor(SystemPanel.yellowText);
                g.drawPolygon(downArrow);
            }
            g.setStroke(prevStroke);
        }
        private void drawNameSelector(Graphics2D g, Colony c, int x, int y, int w, int h) {
            int leftM = x;
            int rightM = x+w;
            int buttonW = s10;
            int buttonTopY = y+s5;
            int buttonMidY = y+s15;
            int buttonBotY = y+s25;
            leftButtonX[0] = leftM; leftButtonX[1] = leftM+buttonW; leftButtonX[2] = leftM+buttonW;
            leftButtonY[0] = buttonMidY; leftButtonY[1] = buttonTopY; leftButtonY[2] = buttonBotY;

            rightButtonX[0] = rightM; rightButtonX[1] = rightM-buttonW; rightButtonX[2] = rightM-buttonW;
            rightButtonY[0] = buttonMidY; rightButtonY[1] = buttonTopY; rightButtonY[2] = buttonBotY;
            prevDesign.reset();
            nextDesign.reset();
            for (int i=0;i<leftButtonX.length;i++) {
                prevDesign.addPoint(leftButtonX[i], leftButtonY[i]);
                nextDesign.addPoint(rightButtonX[i], rightButtonY[i]);
            }

            if (hoverBox == prevDesign)
                g.setColor(SystemPanel.yellowText);
            else
                g.setColor(Color.black);
            g.fillPolygon(leftButtonX, leftButtonY, 3);

            if (hoverBox == nextDesign)
                g.setColor(SystemPanel.yellowText);
            else
                g.setColor(Color.black);
            g.fillPolygon(rightButtonX, rightButtonY, 3);

            int barX = x+s12;
            int barW = w-s24;
            int barY = y+s5;
            int barH = h-s10;
            g.setColor(Color.black);
            g.fillRect(barX, barY, barW, barH);
            shipNameBox.setBounds(barX, barY, barW, barH);

            if (c != null) {
                g.setColor(sliderBoxBlue);
                String name = c.shipyard().design().name();
                if (name == null) {
                    // int i = 0;
                    name = "";
                }
                scaledFont(g, name, barW-s5, 18, 8);
                int sw = g.getFontMetrics().stringWidth(name);
                int x0 = barX+((barW-sw)/2);
                drawString(g,name, x0, barY+s16);
            }

            if (hoverBox == shipNameBox) {
                Stroke prev = g.getStroke();
                g.setColor(SystemPanel.yellowText);
                g.setStroke(stroke2);
                g.draw(shipNameBox);
                g.setStroke(prev);
            }
        }
        private void drawRallyPointButton(Graphics2D g, int x, int y, int w, int h) {
            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            if (sys == null)
                return;

            boolean enabled = rallyPointEnabled();
            rallyPointBox.setBounds(x, y, w, h);
            GradientPaint pt = new GradientPaint(x, y, gray175C, x+w, y, gray115C);
            Paint prevPaint = g.getPaint();
            g.setPaint(pt);
            g.fillRoundRect(x, y, w, h, s10, s10);
            g.setPaint(prevPaint);

            Stroke prevStroke = g.getStroke();
            if ((hoverBox == rallyPointBox)
            && enabled)
                g.setColor(SystemPanel.yellowText);
            else
                g.setColor(gray175C);

            g.setStroke(stroke2);
            g.drawRoundRect(x,y,w,h,s15,s15);
            g.setStroke(prevStroke);

            String s = text("MAIN_COLONY_RELOCATE_LABEL");
            scaledFont(g, s, w-s5, 17, 10);
            if (!enabled)
                g.setColor(gray90C);
            else if (hoverBox == rallyPointBox)	
                g.setColor(SystemPanel.yellowText);
            else
                g.setColor(gray20C);
            int sw = g.getFontMetrics().stringWidth(s);
            int x0 = x+((w-sw)/2);
            drawString(g,s, x0, y+h-s7);
        }
        private void drawRallyToSG(Graphics2D g, int x, int y, int w, int h) {
            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            if (sys == null)
                return;

            boolean enabled = rallyToSGEnabled();
            rallyToSGBox.setBounds(x, y, w, h);
            Paint prevPaint = g.getPaint();
            g.setPaint(Color.BLACK);
            g.fillRoundRect(x, y, w, h, s10, s10);
            g.setPaint(prevPaint);

            Stroke prevStroke = g.getStroke();
            if ((hoverBox == rallyToSGBox) && enabled) {
            	previewRallyPath();
            	g.setColor(SystemPanel.yellowText);
            }
            else {
            	clearRallyPathPreview();
            	g.setColor(gray175C);
            }

            g.setStroke(stroke2);
            g.drawRoundRect(x,y,w,h,s15,s15);
            g.setStroke(prevStroke);

            int sgOfs = s3;
            int sgW = s25 - 2*sgOfs;
            int x1 = x + sgOfs;
            int y1 = y + sgOfs;
            int x2 = x1 + sgW;
            int y2 = y1 + sgW;
            Image img = ShipLibrary.current().stargate.getImage();
            int imgW = img.getWidth(null);
            int imgH = img.getHeight(null);
            g.drawImage(img, x1, y1, x2, y2, 0, 0, imgW, imgH, null);
        }
        private int drawTransportButton(Graphics2D g, int x, int y, int w, int h, int maxSize) {
            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            if (sys == null || sys.colony() == null)
                return 20;

            boolean enabled = transportEnabled();
            boolean noGovAutoTransport = sys.colony().noGovAutoTransport();
            transportBox.setBounds(x, y, w, h);
            g.setColor(darkShadingC);
            g.fillRect(x+s2,y+s5,w-s2,h-s5);
            g.setColor(gray190C);
            g.fillRect(x+s2,y+s5, w-s3, h-s7);
            g.setColor(buttonC);
            g.fillRect(x+s3,y+s6, w-s5, h-s9);
            int iconW = 0;

            if (noGovAutoTransport) {
            	BufferedImage img = spendingPane.governorImage();
            	int imgW = img.getWidth();
            	int imgH = img.getHeight();
            	int margin = (h-imgH)/2;
            	int govX = x + margin;
            	int govY = y + margin + s2;
            	iconW = imgW + margin;
            	g.drawImage(img, govX, govY, imgW, imgH, null);
            	g.setColor(new Color(210, 0, 0));
            	Stroke prevStroke = g.getStroke();
            	g.setStroke(stroke2);
            	g.drawOval(govX-s1, govY-s1, imgW+s2, imgH+s2);
            	g.drawLine(govX-s1, govY+imgH+s2, govX+imgW+s2, govY-s1);
            	g.setStroke(prevStroke);
            }

            if (!enabled)
                g.setColor(gray70C);
            else if (hoverBox == transportBox)
                g.setColor(SystemPanel.yellowText);
            else
                g.setColor(textColor);
            String s = text("MAIN_COLONY_TRANSPORTS_LABEL");
            int w2 = w-iconW;
            int fontSize = scaledFont(g, s, w2-s10, maxSize, 14);
            g.setFont(narrowFont(fontSize));
            int sw = g.getFontMetrics().stringWidth(s);
            drawString(g,s, x+iconW+((w2-sw)/2),y+s25);

            if ((hoverBox == transportBox)
            && enabled) {
                Stroke prevStroke = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(SystemPanel.yellowText);
                g.drawRect(x+s2,y+s5,w-s2,h-s7);
                g.setStroke(prevStroke);
            }
            return fontSize;
        }
        private int drawAbandonButton(Graphics2D g, int x, int y, int w, int h, int maxSize) {
            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            if (sys == null)
                return 20;

            boolean enabled = transportEnabled();
            abandonBox.setBounds(x, y, w, h);
            g.setColor(MainUI.shadeBorderC());
            g.fillRect(x, y, w, h);
            g.setColor(darkShadingC);
            g.fillRect(x+s2,y+s5,w-s2,h-s5);
            g.setColor(gray190C);
            g.fillRect(x+s2,y+s5, w-s3, h-s7);
            g.setColor(buttonC);
            g.fillRect(x+s3,y+s6, w-s5, h-s9);
            if (!enabled)
                g.setColor(gray70C);
            else if (hoverBox == abandonBox)
                g.setColor(SystemPanel.yellowText);
            else
                g.setColor(textColor);

            String s = text("MAIN_COLONY_ABANDON_LABEL");
            int fontSize = scaledFont(g, s, w-s10, maxSize, 14);
            
            g.setFont(narrowFont(fontSize));
            int sw = g.getFontMetrics().stringWidth(s);
            drawString(g,s, x+((w-sw)/2),y+s25);
            if ((hoverBox == abandonBox)
            && enabled) {
                Stroke prevStroke = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(SystemPanel.yellowText);
                g.drawRect(x+s2,y+s5,w-s2,h-s7);
                g.setStroke(prevStroke);
            }
            return fontSize;
        }
        private void incrementGovBuildPct(int amt) {
            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            Colony col = sys == null ? null : sys.colony();
            if (col == null)
                return;
            col.incrShipBuildSparePct(-amt); // negative because Use is displayed
            col.governIfNeeded();
            softClick();
            parent.repaint();
        }
        private void resetGovBuildPct() {
            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            Colony col = sys == null ? null : sys.colony();
            if (col == null)
                return;
            col.resetShipBuildSparePct();
        	col.governIfNeeded(true);
            softClick();
            repaint();
        }
        private void incrementBuildLimit(int amt) {
            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            Colony col = sys == null ? null : sys.colony();
            if (col == null)
                return;
            boolean updated = col.shipyard().incrementBuildLimit(amt);
            if (updated) {
                col.governIfNeeded();
                softClick();
                parent.repaint();
            }
            else
                misClick();
        }
        private void decrementBuildLimit(int amt) {
            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            Colony col = sys == null ? null : sys.colony();
            if (col == null)
                return;
            boolean updated = col.shipyard().decrementBuildLimit(amt);
            if (updated) {
                col.governIfNeeded(true);
                softClick();
                parent.repaint();
            }
            else
                misClick();
        }
        private void resetBuildLimit() {
            StarSystem sys = parentSpritePanel.systemViewToDisplay();
            Colony col = sys == null ? null : sys.colony();
            if (col == null)
                return;
            boolean updated = col.shipyard().resetBuildLimit();
            if (updated) {
            	col.governIfNeeded(true);
                softClick();
                repaint();
            }
            else
                misClick();
        }
        private Image initializedBackgroundImage(int w, int h) {
            if ((starBackground == null)
            || (starBackground.getWidth() != w)
            || (starBackground.getHeight() != h)) {
                starBackground = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                Graphics g = starBackground.getGraphics();
                drawBackgroundStars(g,w,h);
                g.dispose();
            }
            return starBackground;
        }
        private void rallyToNearestSG(int action, Float speed) {
        	player().sv.rallyNearestStarGate(parentSpritePanel.systemViewToDisplay().id, action, speed);
        }
        private void rallyToNearestSG(int action) {
        	StarSystem sys =parentSpritePanel.systemViewToDisplay();
        	Float speed = null;
        	if (sys != null) {
        		ColonyShipyard shipyard = sys.colony().shipyard();
        		if (shipyard.design() instanceof ShipDesign) {
        			ShipDesign shipDesign = (ShipDesign) shipyard.design();
        			speed = options().chainRallySpeed(player(), shipDesign);
        		}
        	}
        	rallyToNearestSG(action, speed);        		
        }
        private void previewRallyPath() {
        	hasPreview = true;
        	rallyToNearestSG (SystemView.PREVIEW_RALLY);
        }
        private void clearRallyPathPreview() {
        	if (hasPreview) {
        		rallyToNearestSG (SystemView.CLEAR_PREVIEW, null);
        		hasPreview = false;
        	}
        }
        private boolean rallyToSGEnabled()	{
        	StarSystem sys = parentSpritePanel.systemViewToDisplay();
        	return !sys.hasStargate(player()) && rallyPointEnabled() && hasStargate(); }
        private boolean rallyPointEnabled() { return !session().performingTurn() && player().canRallyFleetsFrom(id(parentSpritePanel.systemViewToDisplay())); }
        private boolean transportEnabled() { return !session().performingTurn() && player().canSendTransportsFrom(parentSpritePanel.systemViewToDisplay()); }
		@Override public void mouseClicked(MouseEvent arg0)	{ enterCurrentPane(this); }
		@Override public void mouseEntered(MouseEvent arg0)	{
			enterCurrentPane(this);
			inGovLimitBox = true;
			repaint();
		}
		@Override  public void mouseExited(MouseEvent arg0)	{
			exitCurrentPane(this);
			inGovLimitBox = false;
			if (hoverBox != null) {
				hoverBox = null;
				repaint();
			}
		}
		@Override public void mousePressed(MouseEvent arg0)	{ enterCurrentPane(this); }
		@Override public void mouseReleased(MouseEvent e)	{
			enterCurrentPane(this);
            if (e.getButton() > 3)
                return;
            int x = e.getX();
            int y = e.getY();
            boolean rightClick = SwingUtilities.isRightMouseButton(e);
            boolean shiftPressed = e.isShiftDown();
            boolean ctrlPressed = e.isControlDown();
            if (isAltDown()) {
                int adjAmt = 5;
                if (shiftPressed)
                    adjAmt = 20;
                else if (ctrlPressed)
                    adjAmt = 50;
                if (upArrow.contains(x,y))
                	incrementGovBuildPct(adjAmt);
                else if (downArrow.contains(x,y)) 
                	incrementGovBuildPct(-adjAmt);
                else if (limitBox.contains(x,y))
                	resetGovBuildPct();
                return;
            }
            int adjAmt = 1;
            if (shiftPressed)
                adjAmt *= 5;
            if (ctrlPressed)
                adjAmt *= 20;

            if (upArrow.contains(x,y))
                incrementBuildLimit(adjAmt);
            else if (downArrow.contains(x,y)) 
                decrementBuildLimit(adjAmt);
            else if (limitBox.contains(x,y))
                resetBuildLimit();
            else if (shipDesignBox.contains(x,y)){
                if (rightClick)
                    prevShipDesign();
                else
                    nextShipDesign();
                parent.repaint();
            }
            else if (shipNameBox.contains(x,y)){
                if (rightClick)
                    prevShipDesign();
                else
                    nextShipDesign();
                parent.repaint();
            }
            else if (nextDesign.contains(x,y)){
                nextShipDesign();
                parent.repaint();
            }
            else if (prevDesign.contains(x,y)){
                prevShipDesign();
                parent.repaint();
            }
            else if (rallyPointBox.contains(x,y)){
                if (rallyPointEnabled()) {
                    StarSystem sys =  parentSpritePanel.systemViewToDisplay();
                    if (sys != null)
                        parentSpritePanel.parent.clickedSprite(sys.rallySprite());
                    parentSpritePanel.repaint();
                }
            }
            else if (rallyToSGBox.contains(x,y)){
                if (rallyToSGEnabled()) {
                	rallyToNearestSG(SystemView.SET_RALLY);
                    parentSpritePanel.repaint();
                }
            }
            else if (transportBox.contains(x,y)){
                if (transportEnabled()) {
                    StarSystem sys =  parentSpritePanel.systemViewToDisplay();
                    if (sys != null) {
                    	if (e.isControlDown())
                    		sys.colony().toggleGovAutoTransport();
                    	else {
	                        TransportDeploymentPanel.enableAbandon = false; 
	                        parentSpritePanel.parent.clickedSprite(sys.transportSprite());
                    	}
                    }
                    parentSpritePanel.repaint();
                }
            }
            else if (abandonBox.contains(x,y)){
                if (transportEnabled()) {
                    StarSystem sys =  parentSpritePanel.systemViewToDisplay();
                    if (sys != null)  {
                        TransportDeploymentPanel.enableAbandon = true; 
                        parentSpritePanel.parent.clickedSprite(sys.transportSprite());
                    }
                    parentSpritePanel.repaint();
                }
            }
        }
		@Override public void mouseDragged(MouseEvent arg0) { enterCurrentPane(this); }
		@Override public void mouseMoved(MouseEvent e)		{
			enterCurrentPane(this);
            int x = e.getX();
            int y = e.getY();
            Shape prevHover = hoverBox;
            hoverBox = null;

            if (upArrow.contains(x,y))
                hoverBox = upArrow;
            else if (downArrow.contains(x,y))
                hoverBox = downArrow;
            else if (limitBox.contains(x,y))
                hoverBox = limitBox;
            else if (shipDesignBox.contains(x,y))
                hoverBox = shipDesignBox;
            else if (shipNameBox.contains(x,y))
                hoverBox = shipNameBox;
            else if (nextDesign.contains(x,y))
                hoverBox = nextDesign;
            else if (prevDesign.contains(x,y))
                hoverBox = prevDesign;
            else if (rallyPointBox.contains(x,y))
                hoverBox = rallyPointBox;
            else if (rallyToSGBox.contains(x,y))
                hoverBox = rallyToSGBox;
            else if (transportBox.contains(x,y))
                hoverBox = transportBox;
            else if (abandonBox.contains(x,y))
                hoverBox = abandonBox;

            if (prevHover != hoverBox)
                repaint();
        }
		@Override public void mouseWheelMoved(MouseWheelEvent e) {
			enterCurrentPane(this);
            int x = e.getX();
            int y = e.getY();
            
            if (limitBox.contains(x,y)) {
                boolean shiftPressed = e.isShiftDown();
                boolean ctrlPressed = e.isControlDown();
                boolean altPressed = e.isAltDown();
                if (altPressed) {
                    int adjAmt = 5;
                    if (shiftPressed)
                        adjAmt = 20;
                    else if (ctrlPressed)
                        adjAmt = 50;
                    if (e.getWheelRotation() < 0)
                    	incrementGovBuildPct(adjAmt);
                    else
                    	incrementGovBuildPct(-adjAmt);
                    return;
                }
                int adjAmt = 1;
                if (shiftPressed)
                    adjAmt = 5;
                else if (ctrlPressed)
                    adjAmt = 20;
                if (e.getWheelRotation() < 0)
                    incrementBuildLimit(adjAmt);
                else
                    decrementBuildLimit(adjAmt);
                return;
            }
            if (shipDesignBox.contains(x,y) 
            || shipNameBox.contains(x,y)) {
                if (e.getWheelRotation() < 0)
                    nextShipDesign();
                else
                    prevShipDesign();
                return;
            }
        }
    }
}
