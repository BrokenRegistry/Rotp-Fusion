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

import static rotp.model.colony.Colony.DEFENSE;
import static rotp.model.colony.Colony.ECOLOGY;
import static rotp.model.colony.Colony.INDUSTRY;
import static rotp.model.colony.Colony.RESEARCH;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList; // modnar: change to cleaner icon set
import java.util.List; // modnar: change to cleaner icon set

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import rotp.model.colony.Colony;
import rotp.model.colony.ColonyIndustry;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GovernorOptions;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.SystemViewer;
import rotp.ui.UserPreferences;
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

    Color borderHi, borderLo, textC, backC;

    private EmpireSliderPane shipSlider, defSlider, indSlider, ecoSlider, researchSlider;

    private final SystemViewer parent;
    private GalaxyMapPanel mapListener;
    public EmpireColonySpendingPane(SystemViewer p, Color c0, Color text, Color hi, Color lo) {
        parent = p;
        textC = text;
        backC = c0;
        borderHi = hi;
        borderLo = lo;
        init();
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
    @Override
    public void keyPressed(KeyEvent e) {
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
        @Override
        public void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g);

            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if  (colony == null)
                return;

            int w = getWidth();

            if (category < 0) {
                Color color;
                if (colony.isGovernor()) {
                    color = Color.green;
                } else {
                    color = MainUI.shadeBorderC();
                }

                g.setFont(narrowFont(20));
                String titleText = text("MAIN_COLONY_ALLOCATE_SPENDING");
                int titleY = getHeight() - s6;
                drawShadowedString(g, titleText, 2, s5, titleY, color, textC);

                // crappy ASCII art. Should be something else.
                // TODO: for future use
//                if (1 == 0) {
//                    if (colony.isAutoShips()) {
//                        color = Color.green;
//                    } else {
//                        color = MainUI.shadeBorderC();
//                    }
//                    String shipAutomateText = "]=>";
//                    drawShadowedString(g, shipAutomateText, 2, w - s95, titleY, color, textC);
//                }
                String governorOptionsText = text("GOVERNOR_OPTIONS");
                drawShadowedString(g, governorOptionsText, 2, w-s60, titleY, MainUI.shadeBorderC(), textC);
                return;
            }
            String text = text(Colony.categoryName(category));

            // label
            Color textC;
            if (hoverBox == labelBox)
                textC = SystemPanel.yellowText;
            else if (colony.canAdjust(category))
            	if (colony.hasOrder(category) && options().showPendingOrders())
            		textC = sliderTextHasOrder;
            	else
            		textC = sliderTextEnabled;
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
            	String indStr = "";
		boolean rightAmount = (balance == 0);
            	boolean warning = !rightAmount && !colony.isGovernor();
            	if (warning) {
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
				indStr = text("MAIN_COLONY_SPENDING_REFIT") + " " + indStr;
	            	g.setFont(narrowFont(14));
	            	int sw1 = g.getFontMetrics().stringWidth(indStr);
	            	int x1 = (boxW-sw1)/2;
	            	drawString(g, indStr, boxL+x1, boxTopY+boxH-s4);
            	}
            }
            
            if (category == Colony.ECOLOGY)  {
                int popGrowth = colony.ecology().upcomingPopGrowth();
                g.setFont(narrowFont(14));
                String popStr = text("MAIN_COLONY_SPENDING_ECO_GROWTH",strFormat("%+3d", popGrowth));
                int sw1 = g.getFontMetrics().stringWidth(popStr);
                int x1 = (boxW-sw1)/2;
                
                if (popGrowth < 0)
                    g.setColor(SystemPanel.darkOrangeText);
                else
                    g.setColor(Color.gray);
                 drawString(g,popStr, boxL+x1, boxTopY+boxH-s4);
                
                if (popGrowth < 0)
                    g.setColor(SystemPanel.orangeText);
                else
                    g.setColor(Color.lightGray);
                Shape prevClip = g.getClip();
                g.setClip(fillRect);
                drawString(g,popStr, boxL+x1, boxTopY+boxH-s4);
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
/*        private void leftClickResultBox(boolean click, MouseEvent e) {
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;
            if(!colony.locked(category)) {
                colony.smoothMaxSlider(category);
                if (e!=null && e.isControlDown())
           		 colony.toggleOrder(category);
            }
            if (click)
                softClick();
            if(category != ECOLOGY)
            	colony.checkEcoAtClean();
            if(category != RESEARCH && !colony.locked(RESEARCH))
            	colony.allocation(RESEARCH, 0);
            colony.redistributeReducedEcoSpending();

            parent.repaint();
        } */
/*        private void maxSlider(boolean click, MouseEvent e) {
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;
            if(!colony.locked(category)) {
                colony.clearUnlockedSpending();
            	if (e.isControlDown())
            		 colony.toggleOrder(category);
                int allocation = colony.allocationRemaining();
                if(e.getButton() == 1)
                {
                    switch(category)
                    {
                        case SHIP:
                            if(colony.shipyard().buildLimit() > 0)
                                allocation = min(allocation, colony.shipyard().maxAllocationNeeded());
                            break;
                        case DEFENSE:
                            allocation = min(allocation, colony.defense().maxAllocationNeeded());
                            break;
                        case INDUSTRY:
                            allocation = min(allocation, colony.industry().maxAllocationNeeded());
                            break;
                        case ECOLOGY:
                            allocation = min(allocation, colony.ecology().maxAllocationNeeded());
                            break;
                        default:
                            break;
                    }
                    if(allocation == 0)
                        allocation = colony.allocationRemaining();
                }
                colony.setAllocation(this.category, allocation);
                if(category != ECOLOGY)
                    colony.checkEcoAtClean();
                else
                    colony.redistributeReducedEcoSpending();
            }
            if (click)
                softClick();
            parent.repaint();
        } */
/*        private void rightClickResultBox(boolean click, MouseEvent e) {
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;
            if(!colony.locked(category)) {
            	if (e.isControlDown())
            		colony.toggleOrder(category);
                float prevTech = mapListener == null ? 0 : colony.totalPlanetaryResearch();
                switch(category) {
                    case ECOLOGY:
                    	colony.checkEcoAtTerraform();
                    	colony.keepEcoLockedToClean = false;
                        break;
                    default:
                    	maxSlider(click, e);
                    	return;
                }
                if (mapListener == null)
                    RotPUI.instance().techUI().resetPlanetaryResearch();
                else {
                    float techAdj = colony.totalPlanetaryResearch() - prevTech;
                    RotPUI.instance().techUI().adjustPlanetaryResearch(techAdj);
                    mapListener.repaintTechStatus();
                }
            } 
            if (click)
                softClick();
            parent.repaint();
        } */
/*        private void middleClickResultBox(boolean click, MouseEvent e) {
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;
            Colony colony = sys.colony();
            if (colony == null)
                return;
            if(!colony.locked(category)) {
            	if (e.isControlDown())
            		colony.toggleOrder(category);
                float prevTech = mapListener == null ? 0 : colony.totalPlanetaryResearch();
                switch(category) {
                    case SHIP:		break;
                    case DEFENSE:	break;
                    case INDUSTRY:	break;
                    case ECOLOGY:
                    	colony.keepEcoLockedToClean = true;
                    	colony.checkEcoAtClean();
                        break;
                    default:		break;
                }
                if (mapListener == null)
                    RotPUI.instance().techUI().resetPlanetaryResearch();
                else {
                    float techAdj = colony.totalPlanetaryResearch() - prevTech;
                    RotPUI.instance().techUI().adjustPlanetaryResearch(techAdj);
                    mapListener.repaintTechStatus();
                }
            }
            if (click)
                softClick();
            parent.repaint();
        } */
/*        private void clearMaxClick(boolean click, MouseEvent e) {
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

            colony.clearUnlockedSpending();
            int allocation = colony.allocationRemaining();
            int allocationNeeded =  colony.category(category).smartAllocationNeeded(e);
            allocation = min(allocation, allocationNeeded);
            System.out.println("allocation = "+allocation);
            if(allocation == 0 && category != RESEARCH)
                allocation = colony.allocationRemaining();
            colony.setAllocation(category, allocation);

        	if (category == ECOLOGY) {
        		colony.redistributeReducedEcoSpending();
        		colony.keepEcoLockedToClean = SwingUtilities.isMiddleMouseButton(e);
        	}
        	else 
        		colony.checkEcoAtClean();
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
        } */
/*        private void clearBaseAllocClick(boolean click, MouseEvent e) {
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

            colony.clearUnlockedSpending();
            int allocation = colony.allocationRemaining();
            int allocationNeeded =  colony.category(category).smartAllocationNeeded(e);
            allocation = min(allocation, allocationNeeded);
            colony.hasNewOrders(true);
        	player().governorAI().setInitialAllocations(colony);
        	colony.validate();
            
            if (click)
                softClick();
            parent.repaint();
        }*/
        private void smoothMaxClick(boolean click, MouseEvent e) { // TODO BR:
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
            colony.verifiedSmoothMaxSlider(category, e);

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
        	
            // Specific optimizations
            
            // Smart Max
            if (e.isShiftDown()) {
                colony.clearUnlockedSpending();
                if (e.isControlDown()) {
                	colony.redistributeReducedEcoSpending();
                }
                else {
                    int allocation = colony.allocationRemaining();
                    int allocationNeeded =  colony.category(category).smartAllocationNeeded(e);
                    
                    allocation = min(allocation, allocationNeeded);
                    if(allocation == 0 && category != RESEARCH)
                        allocation = colony.allocationRemaining();
                    colony.setAllocation(category, allocation);

                	if (category == ECOLOGY) {
                		colony.redistributeReducedEcoSpending();
                		colony.keepEcoLockedToClean = SwingUtilities.isMiddleMouseButton(e);
                	}
                	else 
                		colony.checkEcoAtClean();
                }
        	}
            // Reset to AI Setting
            else if (e.isControlDown()) {
           		colony.hasNewOrders(true); 
            	try {
            		player().governorAI().setInitialAllocations(colony);
            	}
            	catch (Exception exception) {
            		System.err.println("setInitialAllocations Error");
            		System.err.println(exception.toString());
            	}
            	player().governorAI().setInitialAllocations(colony);
            	colony.validate();
            	colony.checkEcoAtClean();
            }

            // Smooth Max
            else
        		colony.verifiedSmoothMaxSlider(category, e);

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
	            switch (category) {
		    	case DEFENSE:
		    	case INDUSTRY:
		    	case ECOLOGY:
		    		colony.toggleOrder(category);
		            repaint();
		    		return;
	            }
        }
        @Override
        public void mouseClicked(MouseEvent arg0) {}
        @Override
        public void mouseEntered(MouseEvent arg0) {}
        @Override
        public void mouseExited(MouseEvent arg0) {
            if (hoverBox != null) {
                hoverBox = null;
                repaint();
            }
        }
        @Override
        public void mousePressed(MouseEvent ev) { }
        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() > 3)
                return;
            int x = e.getX();
            int y = e.getY();
            if (labelBox.contains(x,y))
            	if (e.isControlDown())
           		 	toggleOrder();
            	else
            		toggleLock();
            else if (leftArrow.contains(x,y))
                decrement(true);
            else if (rightArrow.contains(x,y))
                increment(true);
            else if (resultBox.contains(x,y))
            	commonResultBox(true, e);
//            	if (e.isControlDown())
//            		clearBaseAllocClick(true, e);
//            	else if (e.isShiftDown())
//            		clearMaxClick(true, e);
//            	else
//            		smoothMaxClick(true, e);
            		
//            	if (SwingUtilities.isRightMouseButton(e))
//            		rightClickResultBox(true, e);
//            	else if (SwingUtilities.isMiddleMouseButton(e))
//            		middleClickResultBox(true, e);
//            	else
//            		leftClickResultBox(true, e);
//            		maxSlider(true, e);
            else {
                if (this.category < 0) {
// TODO: for future use
//                    if (x < EmpireColonySpendingPane.this.getWidth() - s95) {
//                        toggleGovernor();
//                    } else if (x < EmpireColonySpendingPane.this.getWidth() - s60) {
//                        toggleAutoShips();
                    if (x < EmpireColonySpendingPane.this.getWidth() - s60) {
                        toggleGovernor();
                    } else {
                        governorOptions();
                    }
                }
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
        @Override
        public void mouseDragged(MouseEvent arg0) { }
        @Override
        public void mouseMoved(MouseEvent e) {
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

            if (newHover != hoverBox) {
                hoverBox = newHover;
                repaint();
            }
        }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
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

    JFrame governorOptionsFrame = null;
    private class GovernorComponentAdapter extends ComponentAdapter {
    	@Override public void componentMoved(java.awt.event.ComponentEvent evt) {
        	GovernorOptions options = govOptions();
			options.setPosition(getLocation());
        }
    }
    private void governorOptions() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                if (governorOptionsFrame == null) {
                    governorOptionsFrame = new JFrame("GovernorOptions")
//                    {
//                    	{
//                    		addComponentListener(new GovernorComponentAdapter());
//                    	}
//                    }
                    ;
                    governorOptionsFrame.addComponentListener(new GovernorComponentAdapter());
                    // make this window have an icon, same as main window
                    // modnar: change to cleaner icon set
                    List<Image> iconImages = new ArrayList<Image>();
                    iconImages.add(ImageManager.current().image("ROTP_MOD_ICON3"));
                    iconImages.add(ImageManager.current().image("ROTP_MOD_ICON2"));
                    iconImages.add(ImageManager.current().image("ROTP_MOD_ICON1"));
                    governorOptionsFrame.setIconImages(iconImages);
                    governorOptionsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

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
                governorOptionsFrame.setLocation(govOptions().getPosition());
                ((GovernorOptionsPanel) governorOptionsFrame.getContentPane()).reOpen();
            }
        });
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
}
