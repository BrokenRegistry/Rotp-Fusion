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

import static rotp.model.colony.ColonyDefense.MAX_BASES;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import rotp.model.colony.Colony;
import rotp.model.empires.Empire;
import rotp.model.galaxy.StarSystem;
import rotp.model.planet.Planet;
import rotp.ui.BasePanel;
import rotp.ui.SystemViewer;

public class EmpireColonyInfoPane extends BasePanel {
    private static final long serialVersionUID = 1L;
    private static final Color enabledArrowColor = Color.black;
//    static final Color disabledArrowColor = new Color(65,65,65);
//    static final Color sliderHighlightColor = new Color(255,255,255);
//    static final Color productionGreenColor = new Color(89, 240, 46);
//    static final Color dataBorders	= new Color(160, 160, 160);
    private static final Color urgedColor	= new Color(0, 0, 142);
    private static final Color mixedColor	= new Color(64, 64, 64);

//    Color borderC;
    private Color darkC;
    private Color textC;
    private Color backC;
    private SystemViewer parentUI;
    EmpireBasesPane basesPane;
    public EmpireColonyInfoPane(SystemViewer p, Color backColor, Color borderColor, Color textColor, Color darkTextColor) {
        parentUI = p;
        // borderC = borderColor;
        darkC = darkTextColor;
        textC = textColor;
        backC = backColor;
        init(borderColor);
    }
    private void init(Color c0) {
        setBackground(c0);

        setOpaque(true);
        JPanel popFactoriesPane = new JPanel();
        popFactoriesPane.setOpaque(false);

        GridLayout layout1 = new GridLayout(0,2);
        layout1.setHgap(s1);
        popFactoriesPane.setLayout(layout1);
        popFactoriesPane.add(new EmpirePopPane());
        popFactoriesPane.add(new EmpireFactoriesPane());

        JPanel shieldBasesPane = new JPanel();
        shieldBasesPane.setOpaque(false);

        basesPane = new EmpireBasesPane();
        GridLayout layout2 = new GridLayout(0,2);
        layout2.setHgap(s1);
        shieldBasesPane.setLayout(layout2);
        shieldBasesPane.add(new EmpireShieldPane());
        shieldBasesPane.add(basesPane);

        GridLayout layout0 = new GridLayout(3,0);
        layout0.setVgap(s1);
        setLayout(layout0);
        add(popFactoriesPane);
        add(shieldBasesPane);
        add(new EmpireProductionPane());
    }
    private void incrementBases(InputEvent e) {
        basesPane.incrBases(1, e.isShiftDown(), e.isControlDown());
    }
    private void decrementBases(InputEvent e) {
        basesPane.incrBases(-1, e.isShiftDown(), e.isControlDown());
    }
    private abstract class EmpireDataPane extends BasePanel implements MouseListener, MouseMotionListener, MouseWheelListener {
        private static final long serialVersionUID = 1L;
        protected Shape hoverBox;
        protected Rectangle basesBox = new Rectangle();
        protected Rectangle titleBox = new Rectangle();
        EmpireDataPane()	{ init(); }
        private void init()	{
            setOpaque(true);
            setBackground(backC);
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }
        protected List<Colony> colonies()	{
        	List<StarSystem> systems = parentUI.systemsToDisplay();
        	List<Colony> colonies = new ArrayList<>();
            if (systems == null) {
                systems = new ArrayList<>();
                StarSystem sys = parentUI.systemViewToDisplay();
                if (sys != null)
                    systems.add(sys);
            }
            if (systems.isEmpty())
                return colonies;
            for (StarSystem sys1: systems) {
                Colony c = sys1.colony();
                if (c != null)
                    colonies.add(c);
            }
            return colonies;
        }
        protected int rightMargin()			{ return s5; }
        private void urge(boolean b)		{
        	List<Colony> colonies = colonies();
        	if (colonies.isEmpty())
        		return;
        	for (Colony c : colonies) {
        		if (urged(c) != b) {
        			urge(c, b);
        			c.governIfNeeded();
        		}
        	}
        }
        protected void urgeToggle()			{
        	List<Colony> colonies = colonies();
        	if (colonies.isEmpty())
        		return;
        	Boolean urged = urged(colonies);
        	if (urged == null)
        		urge(true);
        	else
        		urge(!urged);
        }
        protected String valueString(List<Colony> c)		{ return str(value(c)); }
        protected String maxValueString(List<Colony> c)		{ return str(maxValue(c)); }
        protected String resultString(List<Colony> c)		{
        	if (value(c) == maxValue(c))
        		return "";
        	return concat("/", maxValueString(c));
        }
        protected String dataLabelString(List<Colony> c)	{ return null; }
        protected Color data2Color(List<Colony> c)			{ return textC; }
        private boolean governed(List<Colony> colonies)	{
        	for (Colony c: colonies)
        		if (c.isGovernor())
        			return true;
            return false;
        }
        private Boolean urged(List<Colony> colonies)		{
        	boolean yes = false;
        	boolean no  = false;
        	for (Colony c: colonies) {
        		if (urged(c))
        			yes = true;
        		else
        			no = true;
        		if (yes && no)
        			return null;
        	}
            return yes;
        }
        abstract protected boolean urged(Colony c);
        abstract protected void urge(Colony c, boolean b);
        abstract protected String titleString();
        abstract protected int value(List<Colony> c);
        abstract protected int maxValue(List<Colony> c);
		@Override public void mouseClicked(MouseEvent e)	{ parentUI.enterCurrentPane(this); }
		@Override public void mouseEntered(MouseEvent e)	{
			parentUI.enterCurrentPane(this);
			clearHoverSprite(e, parentUI.mapHandler());
		}
		@Override public void mouseExited(MouseEvent e)		{
			setModifierKeysState(e);
			parentUI.exitCurrentPane(this);
            if (hoverBox != null) {
                hoverBox = null;
                repaint();
            }
        }
		@Override public void mousePressed(MouseEvent e)	{ parentUI.enterCurrentPane(this); }
        @Override public void mouseReleased(MouseEvent e)	{
            if (e.getButton() > 3)
                return;
            int x = e.getX();
            int y = e.getY();
            if (titleBox.contains(x,y)) {
            	urgeToggle();
            	parentUI.repaint();
            }
        }
		@Override public void mouseDragged(MouseEvent e)	{ parentUI.enterCurrentPane(this); }
		@Override public void mouseMoved(MouseEvent e)		{
			setModifierKeysState(e);
			parentUI.enterCurrentPane(this);
            int x = e.getX();
            int y = e.getY();
            Shape newHover = null;
            if (titleBox.contains(x,y))
                newHover = titleBox;
            if (newHover != hoverBox) {
                hoverBox = newHover;
                repaint();
            }
        }
		@Override public void mouseWheelMoved(MouseWheelEvent e)	{ parentUI.enterCurrentPane(this); }
        @Override public void paintComponent(Graphics g0)	{
            Graphics2D g = (Graphics2D) g0;

            List<Colony> colonies = colonies();
            if (colonies.isEmpty())
                return;
            super.paintComponent(g);

            String strTitle = titleString();
            String strDataLabel = dataLabelString(colonies);
            String strData1 = valueString(colonies);
            String strData2 = resultString(colonies);
            Color data2Color = data2Color(colonies);
            Boolean urged = urged(colonies);
            boolean governed = governed(colonies);

            int x0 = s5;
            int y0 = getHeight()-s6;

            // calc max width for label and try to get largest font (from 13-16) in it            
            int x1 = 0;
            int x2 = 0;
            int sw1 = 0;
            int sw2 = 0;
            int fontSize = 17;
            boolean textFits = false;
            int titleMaxW = 0;
            while (!textFits && (fontSize >12)) {
                fontSize--;
                g.setFont(narrowFont(fontSize));
                int sw0 = strDataLabel == null ? 0 : g.getFontMetrics().stringWidth(strDataLabel);
                if (sw0 > 0)
                    x1 = getWidth()-rightMargin()-sw0;
                else {
                    sw1 = g.getFontMetrics().stringWidth(strData1)+s1;
                    sw2 = g.getFontMetrics().stringWidth(strData2);
                    x2 = getWidth()-rightMargin()-sw2;
                    x1 = x2-sw1;
                }
                titleMaxW = x1-x0-s2;
                textFits = g.getFontMetrics().stringWidth(strTitle) <= titleMaxW;
            }
            titleBox.setBounds(x0, y0-s17, titleMaxW, s20);

            if (governed) {
	            if (hoverBox == titleBox)
	            	g.setColor(Color.yellow);
	            else if (urged == null)
	            	g.setColor(mixedColor);
	            else if (urged)
	            	g.setColor(urgedColor);
	            else
	            	g.setColor(SystemPanel.blackText);
            }
            else
            	g.setColor(SystemPanel.blackText);
            drawString(g, strTitle, x0, y0);

            if (strDataLabel != null) {
                drawShadowedString(g, strDataLabel, 1, x1, y0, darkC, textC);
            }
            else {
                drawShadowedString(g, strData1, 1, x1, y0, darkC, data2Color);
                g.setColor(darkC);
                drawString(g,strData2, x2, y0);
                basesBox.setBounds(x1-s3,y0-s12,(x2-x1)+sw2+s6,s15);
                if (hoverBox == basesBox) {
                    Stroke prevStroke = g.getStroke();
                    g.setStroke(stroke2);
                    g.setColor(SystemPanel.yellowText);
                    g.drawRect(x1-s3,y0-s12,(x2-x1)+sw2+s4,s15);
                    g.setStroke(prevStroke);
                }
            }
        }
    }
    private class EmpirePopPane extends EmpireDataPane {
        private static final long serialVersionUID = 1L;
        @Override public String textureName()		{ return parentUI.subPanelTextureName(); }
        @Override protected String titleString()	{
            if (isShiftDown() || isAltDown())
            	return text("MAIN_COLONY_WORKING_POPULATION");
            else
            	return text("MAIN_COLONY_POPULATION");
        }
        @Override protected boolean urged(Colony c)	{ return c.govUrgePop(); }
        @Override protected void urge(Colony c, boolean b)		{ c.govUrgePop(b); }
        @Override protected int value(List<Colony> colonies)	{
            float val = 0;
            if (isShiftDown() || isAltDown())
            	for (Colony c: colonies)
                    val += c.workingPopulation(); 
            else
	            for (Colony c: colonies)
	                val += c.displayPopulation(); 
            return (int) (val + 0.01f);
        }
        @Override protected int maxValue(List<Colony> colonies)	{ 
        	float val = 0;
            for (Colony c: colonies)
                val += c.maxSize(); 
            return (int) (val + 0.01f);
        }
    }
    private class EmpireFactoriesPane extends EmpireDataPane {
        private static final long serialVersionUID = 1L;
        @Override public String textureName()		{ return parentUI.subPanelTextureName(); }
        @Override protected String titleString()	{ return text("MAIN_COLONY_FACTORIES"); }
        @Override protected boolean urged(Colony c)	{ return c.govUrgeFactories(); }
        @Override protected void urge(Colony c, boolean b)		{ c.govUrgeFactories(b); }
        @Override protected int value(List<Colony> colonies)	{ 
            float val = 0;
            for (Colony c: colonies)
                val += c.industry().factories(); 
            return (int) (val + 0.01f);
        }
        @Override protected int maxValue(List<Colony> colonies)	{ 
            int val = 0;
            for (Colony c: colonies)
                val += c.industry().maxBuildableFactories(); 
            return val;
        }
        @Override protected String maxValueString(List<Colony> c)	{ 
            if (c.size()> 1)
                return str(maxValue(c));
            Planet p = c.get(0).planet();
            if (p.currentSize() < p.maxSize())
                return text("MAIN_COLONY_VALUE+", str(maxValue(c))); 
            else
                return str(maxValue(c));
        }
    }
    private class EmpireShieldPane extends EmpireDataPane {
        private static final long serialVersionUID = 1L;
        @Override public String textureName()		{ return parentUI.subPanelTextureName(); }
        @Override protected String titleString()	{ return text("MAIN_COLONY_SHIELD"); }
        @Override protected boolean urged(Colony c)	{ return c.govUrgeShield(); }
        @Override protected void urge(Colony c, boolean b)		{ c.govUrgeShield(b); }
        @Override protected int value(List<Colony> colonies)	{ 
            int val = 0;
            for (Colony c: colonies)
                val += (int) c.defense().shieldLevel(); 
            return val;
        }
        @Override protected int maxValue(List<Colony> colonies)	{ 
            int val = 0;
            for (Colony c: colonies)
                val += c.defense().maxShieldLevel(); 
            return val;
        }
        @Override protected String dataLabelString(List<Colony> colonies)	{ 
            for (Colony c: colonies) {
                if (!c.starSystem().inNebula())
                    return null;
            }
            return text("MAIN_COLONY_NO_SHIELD");
        }
    }
    class EmpireBasesPane extends EmpireDataPane {
        private static final long serialVersionUID = 1L;
        private final Polygon upArrow = new Polygon();
        private final Polygon downArrow = new Polygon();
        private final int upButtonX[] = new int[3];
        private final int upButtonY[] = new int[3];
        private final int downButtonX[] = new int[3];
        private final int downButtonY[] = new int[3];
        private boolean allowAdjust = true;
        private List<Colony> colonies = new ArrayList<>();
        private int maxBasesValue = 0;
        public EmpireBasesPane() { super(); }
        void incrBases(int inc, boolean shiftDown, boolean ctrlDown)  {
            StarSystem sys = parentUI.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if  (colony == null)
                return;

			if (inc == 0) {
				maxBasesValue = (int) colony.defense().bases();
			}
			else {
				if (shiftDown)
					inc *= 5;
				if (ctrlDown)
					inc *= 20;
				maxBasesValue += inc;
				// BR: don't loop anymore
				maxBasesValue = bounds(0, maxBasesValue, MAX_BASES);
//				if (maxBasesValue > MAX_BASES)
//					maxBasesValue = 0;
//				else if (maxBasesValue < 0) 
//					maxBasesValue = MAX_BASES;
			}

            for (Colony c: colonies) {
            	c.defense().maxBases(maxBasesValue);
            	c.governIfNeeded();
            }
            softClick();
            repaint();            
        }
        @Override public String textureName()		{ return parentUI.subPanelTextureName(); }
        @Override protected int rightMargin()		{ return allowAdjust ? s20 : s5; }
        @Override protected String titleString()	{ return text("MAIN_COLONY_BASES"); }
        @Override protected boolean urged(Colony c)	{ return c.govUrgeBases(); }
        @Override protected void urge(Colony c, boolean b)		{ c.govUrgeBases(b); }
        @Override protected int value(List<Colony> colonies)	{ 
            float val = 0;
            for (Colony c: colonies)
                val += c.defense().bases(); 
            return (int) (val + 0.01f);
        }
        @Override protected int maxValue(List<Colony> colonies)	{ 
            int val = 0;
            for (Colony c: colonies)
                val = max(val,c.defense().maxBases()); 
            maxBasesValue = val;
            return val;
        }
        @Override public void paintComponent(Graphics g0)	{
            Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g);

            colonies = colonies();
            if (colonies.isEmpty())
                return;

            allowAdjust = true;
            int w = getWidth();
            int h = getHeight();

            upButtonX[0] = w-s11; upButtonX[1] = w-s17; upButtonX[2] = w-s5;
            upButtonY[0] = s3; upButtonY[1] = s11; upButtonY[2] = s11;

            downButtonX[0] = w-s11; downButtonX[1] = w-s17; downButtonX[2] = w-s5;
            downButtonY[0] = h-s3; downButtonY[1] = h-s11; downButtonY[2] = h-s11;

            g.setColor(enabledArrowColor);
            g.fillPolygon(upButtonX, upButtonY, 3);

//            if (maxBasesValue == 0)
//                g.setColor(disabledArrowColor);
//            else
//                g.setColor(enabledArrowColor);
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
            else if (hoverBox == downArrow) {
                g.setColor(SystemPanel.yellowText);
                g.drawPolygon(downArrow);
            }
            g.setStroke(prevStroke);
        }
        @Override public void mouseExited(MouseEvent e)		{
        	parentUI.exitCurrentPane(this);
            if (hoverBox != null) {
                hoverBox = null;
                repaint();
            }
            colonies.clear();
        }
        @Override public void mouseReleased(MouseEvent e)	{
        	parentUI.enterCurrentPane(this);
            if (e.getButton() > 3)
                return;
            int x = e.getX();
            int y = e.getY();
            if (upArrow.contains(x,y))
                incrementBases(e);
            else if (downArrow.contains(x,y)) 
                decrementBases(e);
            else if (basesBox.contains(x,y) && SwingUtilities.isRightMouseButton(e)) 
            	incrBases(0, e.isShiftDown(), e.isControlDown());
            else if (titleBox.contains(x,y))
                urgeToggle();
            else
            	return;
            parentUI.repaint();
        }
        @Override public void mouseMoved(MouseEvent e)		{
        	parentUI.enterCurrentPane(this);
            int x = e.getX();
            int y = e.getY();

            Shape newHover = null;
            if (titleBox.contains(x,y))
                newHover = titleBox;
            else if (upArrow.contains(x,y))
                newHover = upArrow;
            else if (downArrow.contains(x,y))
                newHover = downArrow;
            else if (basesBox.contains(x,y))
                newHover = basesBox;

            if (newHover != hoverBox) {
                hoverBox = newHover;
                repaint();
            }
        }
        @Override public void mouseWheelMoved(MouseWheelEvent e)	{
        	parentUI.enterCurrentPane(this);
            if (e.getWheelRotation() < 0)
                incrementBases(e);
            else
                decrementBases(e);
        }
    }
    private class EmpireProductionPane extends EmpireDataPane {
        private static final long serialVersionUID = 1L;
        EmpireProductionPane()	{ init(); }
        private void init()		{
            setBackground(backC);
            setOpaque(true);
        }
        @Override public String textureName()		{ return parentUI.subPanelTextureName(); }
		@Override protected String titleString()	{
			if (isShiftDown() || isAltDown())
				return text("MAIN_COLONY_PRODUCTION_ALT");
			else
				return text("MAIN_COLONY_PRODUCTION");
		}
		@Override protected boolean urged(Colony c) { return c.govUrgeBuildUp(); }
		@Override protected String valueString(List<Colony> c)	{
			if (isShiftDown() || isAltDown()) {
				if (c.isEmpty())
					return "?";
				Colony col = c.get(0);
				Empire e =col.empire();
				String workerProd = fmt(e.workerProductivity(), 2);
				String factoryProd = fmt(col.factoryNetProductivity(), 1);
				//return str(value(c));
				return concat(workerProd, "  (", factoryProd, ")");
			}
			String income = str(value(c));
			String prod   = str(maxValue(c));
			//return str(value(c));
			return concat(income, "  (", prod, ")");
		}
		@Override protected String resultString(List<Colony> c)	{ return ""; }
		@Override protected void urge(Colony c, boolean b)		{ c.govUrgeBuildUp(b); }
		@Override protected int value(List<Colony> cols)		{
            float val = 0;
            for (Colony c: cols)
                val += c.totalIncome();
            return (int) (val + 0.01f);
		}
		@Override protected int maxValue(List<Colony> cols)		{
            float val = 0;
            for (Colony c: cols)
                val += c.production();
            return (int) (val + 0.01f);
		}
		@Override protected Color data2Color(List<Colony> cols)	{
			boolean yes = false;
			boolean no  = false;
			for (Colony c: cols) {
				if (c.showTriggeredROI())
					yes = true;
				else
					no = true;
				if (yes && no)
					return new Color(44, 120, 23);
			}
			if (yes)
				return new Color(89, 240, 46);
			return textC;
		}
	}
}
