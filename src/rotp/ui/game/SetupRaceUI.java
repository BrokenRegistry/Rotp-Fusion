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
package rotp.ui.game;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static rotp.model.empires.CustomRaceDefinitions.BASE_RACE_MARKER;
import static rotp.ui.util.IParam.labelFormat;
import static rotp.ui.util.IParam.realLangLabel;
import static rotp.ui.util.IParam.rowFormat;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints; // modnar: needed for adding RenderingHints
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import rotp.model.empires.Race;
import rotp.model.game.IGameOptions;
import rotp.model.game.IRaceOptions;
import rotp.model.ships.ShipImage;
import rotp.model.ships.ShipLibrary;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.game.HelpUI.HelpSpec;
import rotp.ui.main.SystemPanel;
import rotp.ui.options.AllSubUI;
import rotp.ui.util.ParamList;
import rotp.ui.util.ParamString;
import rotp.util.FontManager;
import rotp.util.LanguageManager;
import rotp.util.ModifierKeysState;

public final class SetupRaceUI extends BaseModPanel implements MouseWheelListener, IRaceOptions {
    private static final long serialVersionUID	= 1L;
	private static final String guiTitleID		= "SETUP_SELECT_RACE";
	private static final String GUI_ID          = "START_RACE";
	private static final String cancelKey		= "SETUP_BUTTON_CANCEL";
	private static final String customRaceKey	= "SETUP_BUTTON_CUSTOM_PLAYER_RACE";
    private static final int    MAX_SHIP   		= ShipLibrary.designsPerSize; // BR:
	private static final int    settingFont		= 20;
	private	static final Font   labelFont		= FontManager.current().narrowFont(settingFont);
 	private static final int    buttonFont		= 30;
    private final int    MAX_RACES  		= 16; // modnar: increase MAX_RACES to add new Races
    private final int    MAX_COLORS 		= 16; // modnar: add new colors

    private final Color darkBrownC = new Color(112,85,68);

    private int w10Or = scaled(200);
    private int w6Ext = scaled(80);
    private int w6Ext() { return isOS()? 0 : w6Ext; }

    // Left Frame
    private int xLeftFrame() { return scaled(220) - w6Ext; }
    private int yLeftFrame = scaled(110);
    private int wLeftFrame() { return w10Or + w6Ext(); }
    private int hLeftFrame = scaled(485);

    // Center Frame
    private int xCtrFrame() { return xLeftFrame() + wLeftFrame() -s3; };
    private int yCtrFrame = scaled(103);
    private int wCtrFrame = scaled(395);
    private int hCtrFrame = scaled(499);

    // Right Frame
    private int xRightFrame() { return xCtrFrame() + wCtrFrame; }
    private int yRightFrame = yLeftFrame;
    private int wRightFrame = scaled(335);
    private int hRightFrame = hLeftFrame;

    // Shading BG
    private int wShEdge = s15;
    private int xShading() { return xLeftFrame() - wShEdge; } 
    private int yShading = yLeftFrame - wShEdge;
    private int wShading() { return xRightFrame() + wRightFrame + wShEdge - xShading(); }
    private int hShading = hLeftFrame + 2 * wShEdge;

    // Colors
    private int xColors() { return xRightFrame() + wShEdge; };
    private int yColors = yRightFrame + hRightFrame - scaled(40);
    private int wColors = s21;  // modnar: add new colors, change color box sizes
    private int hColors = s15;

    // Species Parameters
    private int iconSize	= s95;
    private int yIcon		= yRightFrame + s10;
    private int xIcon() { return xRightFrame() + s6 + iconSize/2; }
    private int wIcon		= s76;
    private int hIcon		= s82;
    private int rShadeIcon	= s78;
    private Box	playerRaceSettingBox = new Box("SETUP_RACE_CUSTOM"); // BR: Player Race Customization
    private Box	checkBox			 = new Box("SETUP_RACE_CHECK_BOX"); // BR: Player Race Customization
    private Box[] raceBox			 = new Box[MAX_RACES];
    private BufferedImage raceImg;
    private BufferedImage raceIconImg; // For the little icon
    private BufferedImage raceBackImg; // For race Mug
    private BufferedImage[] raceMugs = new BufferedImage[MAX_RACES];
    private Race dataRace;

    // Other Parameters
    private final int FIELD_W		= scaled(160);
    private final int FIELD_H		= s24;
    private JTextField leaderName	= new JTextField("");
    private Box	leaderBox			= new Box("SETUP_RACE_LEADER");
    private JTextField homeWorld 	= new JTextField("");
    private Box	homeWorldBox		= new Box("SETUP_RACE_HOMEWORLD");
    private Box[] colorBox			= new Box[MAX_COLORS];

    // Fleet Parameters
    private final int shipNum     = 6;
    private final int shipSize    = 2; // Large
    private final int shipDist    = s80;
    private final int shipHeight  = s65;
    private final int shipWidth   = (int) (shipHeight * 1.3314f);
    private final int fleetWidth  = shipWidth;
    private final int fleetHeight = shipHeight + shipDist * (shipNum-1);
    private int xFleet() { return xRightFrame() + scaled(235); }
    private final int yFleet      = yRightFrame + s10;
    private Box		  shipSetBox  = new Box("SETUP_RACE_SHIPSET"); // BR: ShipSet Selection
    private Box[]	  shipBox	  = new Box[MAX_SHIP]; // BR: ShipSet Selection
    private JTextField shipSetTxt = new JTextField(""); // BR: ShipSet Selection
    private int shipStyle		  = 0; // The index that define the shape
    private BufferedImage shipBackImg, fleetBackImg;
	private BufferedImage[] fleetImages = new BufferedImage[MAX_SHIP];

    // Buttons Parameters
    private int buttonSep	= s15;
    private Box	helpBox		= new Box("SETTINGS_BUTTON_HELP");
    private Box	cancelBox	= new Box("SETUP_RACE_CANCEL");
//    private boolean drawAllButtons = true;
	public ParamList playerSpecies() {
		return new ParamListPlayerSpecies( // For Guide
				BASE_UI, "PLAYER_SPECIES", guiOptions().getNewRacesOnOffList(), IRaceOptions.defaultRace);
	}
	public ParamString playerHomeWorld() {
		return new ParamStringPlayerHomeWorld(BASE_UI, "PLAYER_HOMEWORLD", "");
	}
	public ParamStringPlayerLeader playerLeader() {
		return new ParamStringPlayerLeader(BASE_UI, "PLAYER_LEADER", "");
	}

	@Override protected Box newExitBox()		{ return new Box("SETUP_RACE_NEXT"); }
	@Override protected String exitButtonKey()	{ return "SETUP_BUTTON_NEXT" ; }
	@Override protected Font bigButtonFont(boolean retina)	{
		if (retina)
			return narrowFont(retina(buttonFont));
		else
			return narrowFont(buttonFont);
	}
	@Override protected void setBigButtonGraphics(Graphics2D g)	{
		g.setFont(bigButtonFont(retina));
		g.setPaint(GameUI.buttonRightBackground());
	}
	@Override protected void setSmallButtonGraphics(Graphics2D g) {
		g.setFont(smallButtonFont());
		g.setPaint(GameUI.buttonLeftBackground());
	}

	private boolean isOS() { return newGameOptions().originalSpeciesOnly(); }
    public SetupRaceUI() {
        init0();
    }
    private void init0() {
		isSubMenu = false;
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        for (int i=0;i<raceBox.length;i++)
            raceBox[i] = new Box("SETUP_RACE_RACES");
        for (int i=0;i<colorBox.length;i++)
            colorBox[i] = new Box();
        for (int i=0; i<shipBox.length; i++)
        	shipBox[i] = new Box("SETUP_RACE_SHIPSET");
        initTextField(homeWorld);
        initTextField(leaderName);
        initTextField(shipSetTxt); // BR:
    }
    private void resetRaceMug() {
    	for (int i=0;i<raceMugs.length;i++)
    		raceMugs[i] = null;
    }
    private void resetFleetImages() {
    	for (int i=0; i<fleetImages.length; i++)
    		fleetImages[i] = null;
    }
    private void initShipBoxBounds() {
    	int xFleet = xFleet(); 
        for (int i=0; i<shipBox.length; i++)
        	shipBox[i].setBounds(xFleet, yFleet + i * shipDist, shipWidth, shipHeight);
    }
	@Override protected void singleInit()	{ paramList = AllSubUI.optionsRace(); }
    @Override public void init() {
    	super.init();
    	leaderName.setBackground(GameUI.setupFrame());
    	shipSetTxt.setBackground(GameUI.setupFrame());
    	homeWorld.setBackground(GameUI.setupFrame());
    	EditCustomRaceUI.updatePlayerCustomRace();
        leaderName.setFont(labelFont);
        setHomeWorldFont(); // BR: MonoSpaced font for Galaxy
        shipSetTxt.setFont(labelFont); // BR:
        initShipBoxBounds();
        refreshGui(0);
        // Save initial options
        newGameOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
    }
	@Override public void showHelp() {
		loadHelpUI();
		repaint();   
	}
	@Override public void showHotKeys() {
		loadHotKeysUI();
		repaint();   
	}
	@Override protected void loadHotKeysUI() {
    	HelpUI helpUI = RotPUI.helpUI();
        helpUI.clear();
        int xHK = scaled(100);
        int yHK = scaled(70);
        int wHK = scaled(360);
        helpUI.addBrownHelpText(xHK, yHK, wHK, 17, text("SETUP_RACE_HELP_HK"));
        helpUI.open(this);
	}
	private void loadHelpUI() {
		int xBox, yBox, wBox;
		int xb, xe, yb, ye;
		int nL, hBox, lH;
		String txt;
		HelpSpec sp;
		Box dest;
		HelpUI helpUI = RotPUI.helpUI();
		helpUI.clear();

		txt  = text("SETUP_RACE_MAIN_DESC");
		nL   = 6;
		xBox = s50;
		wBox = scaled(350);
		yBox = s20;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
		lH   = HelpUI.lineH();

		int yShift = s40;
		int xShift = s40;
		dest = exitBox;
		txt  = dest.getDescription();
        nL   = 2;
        wBox = dest.width - s20;
        hBox = nL*lH;
        xBox = dest.x+s10;
        yBox = dest.y-hBox-yShift;
        sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
        xb   = xBox+wBox/2;
        yb   = yBox+sp.height();
        xe   = dest.x + dest.width/2;
        ye   = dest.y;
        sp.setLine(xb, yb, xe, ye);

		dest = cancelBox;
		txt  = dest.getDescription();
        nL   = 2;
        hBox = nL*lH;
        xBox = dest.x+s10;
        yBox = dest.y-hBox-yShift;
        sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
        xb   = xBox+wBox/2;
        yb   = yBox+sp.height();
        xe   = dest.x + dest.width/2;
        ye   = dest.y;
        sp.setLine(xb, yb, xe, ye);

		dest = defaultBox;
		txt  = dest.getDescription();
        nL   = 3;
        hBox = nL*lH;
        xBox -= 2*xShift;
        yBox -= hBox+yShift;
        sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
        xb   = xBox+xShift;
        yb   = yBox+sp.height();
        xe   = dest.x + dest.width - xShift;
        ye   = dest.y;
        sp.setLine(xb, yb, xe, ye);

		dest = lastBox;
		txt  = dest.getDescription();
        nL   = 3;
        hBox = nL*lH;
        xBox -= wBox;
        yBox = dest.y-hBox-yShift;
        sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
        xb   = xBox+wBox/4;
        yb   = yBox+sp.height();
        xe   = dest.x + dest.width/2;
        ye   = dest.y;
        sp.setLine(xb, yb, xe, ye);

		dest = userBox;
		txt  = dest.getDescription();
        nL   = 2;
        hBox = nL*lH;
        xBox -= xShift;
        yBox -= hBox+yShift;
        sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
        xb   = xBox+xShift/2;
        yb   = yBox+sp.height();
        xe   = dest.x + dest.width*3/4;;
        ye   = dest.y;
        sp.setLine(xb, yb, xe, ye);

		dest = checkBox;
		txt  = dest.getDescription();
        nL   = 2;
        hBox = nL*lH;
        xBox -= xShift;
        yBox -= hBox+yShift;
        sp   = helpUI.addBrownHelpText(xBox, yBox, scaled(170), nL, txt);
        xb   = xBox+xShift/2;
        yb   = yBox+sp.height();
        xe   = dest.x + dest.width/2;
        ye   = dest.y;
        sp.setLine(xb, yb, xe, ye);

		dest = guideBox;
		txt  = dest.getDescription();
		nL   = 3;
		hBox = HelpUI.height(nL);
		xBox = dest.x;
		yBox = playerRaceSettingBox.y - hBox - yShift/2;
		sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
		xb   = xBox + wBox*1/4;
		yb   = yBox + sp.height();
		xe   = dest.x + dest.width*1/2;
		ye   = dest.y;
		sp.setLine(xb, yb, xe, ye);

		dest = playerRaceSettingBox;
		txt  = dest.getDescription();
        nL   = 3;
        hBox = nL*lH;
		xBox += xShift*3;
        yBox -= hBox+yShift;
        sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
        xb   = xBox+wBox*3/4;
        yb   = yBox+sp.height();
        xe   = dest.x + dest.width*3/4;
        ye   = dest.y;
        sp.setLine(xb, yb, xe, ye);

		dest = shipSetBox;
		txt  = dest.getDescription();
        nL   = 3;
        wBox = scaled(225);
        hBox = nL*lH;
        xBox = dest.x - s15;
        yBox = dest.y - hBox - s60;
        sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
        xb   = xBox+wBox/2;
        yb   = yBox+sp.height();
        xe   = dest.x + dest.width/2;
        ye   = dest.y;
        sp.setLine(xb, yb, xe, ye);
        int margin = s3;
		dest = shipBox[0];
		int lx = dest.x - margin;
		int ty = dest.y - margin;
		int rx = lx + dest.width + margin;
		int by = shipBox[MAX_SHIP-1].y + dest.height + margin;

        sp.setLineArr(xb+s30, yBox,
        		lx, ty + scaled(120),
        		lx, ty,
        		rx, ty,
        		rx, by,
        		lx, by,
        		lx, ty + s100
           	    );

		txt  = text("SETUP_RACE_RACES_DESC");
		Rectangle dst = new Rectangle(scaled(425), scaled(108), scaled(385), scaled(489));
        nL   = 4;
        wBox = scaled(300);
        hBox = nL*lH;
        xBox = dst.x - wBox - s25;
        yBox = dst.y + dst.height/4;
        sp   = helpUI.addBrownHelpText(xBox, yBox, wBox, nL, txt);
        xb   = xBox+wBox;
        yb   = yBox+sp.height()/2;
        xe   = dst.x + s20;
        ye   = dst.y + dest.height/2;
        sp.setLine(xb, yb, xe, ye);

        helpUI.open(this);
    }
    private void setHomeWorldFont() { // BR: MonoSpaced font for Galaxy
   		homeWorld.setFont(narrowFont(20));
    }
    private void doCancelBoxAction() {
		buttonClick();
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT: 
		default: // Save
			newGameOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
			break; 
		}
    	goToMainMenu();
 	}
    private void doNextBoxAction() { // save and continue
		buttonClick();
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT:
		default: // Save
			newGameOptions().saveOptionsToFile(LIVE_OPTIONS_FILE);
			break; 
		}
 		goToGalaxySetup();
 	}
    @Override protected void doExitBoxAction() { doNextBoxAction(); }
	@Override protected String GUI_ID() { return GUI_ID; }
	@Override public void refreshGui(int level) {
		raceChanged();
		repaint();
	}
	private static String cancelButtonKey() {
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT:
			// return restoreKey;
		default:
			return cancelKey;
		}
	}
	private void drawFixButtons(Graphics2D g, boolean all) {
        Stroke prev = g.getStroke();

        // Help button
        if (hoverBox == helpBox || all) {
	        g.setFont(narrowFont(25));
	        if (helpBox == hoverBox)
	            g.setColor(Color.yellow);
	        else
	            g.setColor(Color.white);
	        drawString(g,"?", s26, s40);
	        g.setStroke(stroke1);
	        g.drawOval(helpBox.x, helpBox.y, helpBox.width, helpBox.height);
    	}

        g.setFont(bigButtonFont(false));
		// left button
        if (hoverBox == cancelBox || all) {
	        String text1 = text(cancelButtonKey());
	        int sw1  = g.getFontMetrics().stringWidth(text1);
	        int x1   = cancelBox.x+((cancelBox.width-sw1)/2);
	        int y1   = cancelBox.y+cancelBox.height*75/100;
	        Color c1 = hoverBox == cancelBox ? Color.yellow : GameUI.borderBrightColor();
	        drawShadowedString(g, text1, 2, x1, y1, GameUI.borderDarkColor(), c1);
	        g.setStroke(stroke1);
	        g.drawRoundRect(cancelBox.x, cancelBox.y, cancelBox.width, cancelBox.height, cnr, cnr);
        }

        // BR: Player Race Customization
        // far left button
        if (hoverBox == playerRaceSettingBox || all) {
	        g.setFont(smallButtonFont());
	        String text4 = text(customRaceKey);
	        int sw4  = g.getFontMetrics().stringWidth(text4);
	        int x4   = playerRaceSettingBox.x + ((playerRaceSettingBox.width-sw4)/2);
	        int y4   = playerRaceSettingBox.y + playerRaceSettingBox.height*75/100;
	        Color c4 = hoverBox == playerRaceSettingBox ? Color.yellow : GameUI.borderBrightColor();
	        drawShadowedString(g, text4, 2, x4, y4, GameUI.borderDarkColor(), c4);
	        g.setStroke(stroke1);
	        g.drawRoundRect(playerRaceSettingBox.x, playerRaceSettingBox.y, playerRaceSettingBox.width, playerRaceSettingBox.height, cnr, cnr);
        }

        // BR: Race customization check box
        if (hoverBox == checkBox || all) {
	        int checkW = s16;
	        int checkX = playerRaceSettingBox.x + playerRaceSettingBox.width + s10;    
	        int checkY = playerRaceSettingBox.y + playerRaceSettingBox.height - s7;
	        checkBox.setBounds(checkX, checkY-checkW, checkW, checkW);
	        g.setStroke(stroke3);
	        // g.setColor(checkBoxC);
			g.setColor(GameUI.setupFrame());
	        g.fill(checkBox);
	        if (hoverBox == checkBox) {
	            g.setColor(Color.yellow);
	            g.draw(checkBox);
	        }
        }
        if (!all & playerIsCustom.get()) {
	        int checkW = s16;
	        int checkX = playerRaceSettingBox.x + playerRaceSettingBox.width + s10;    
	        int checkY = playerRaceSettingBox.y + playerRaceSettingBox.height - s7;
	        g.setStroke(stroke3);
            g.setColor(SystemPanel.blackText);
            g.drawLine(checkX-s1, checkY-s8, checkX+s4, checkY-s4);
            g.drawLine(checkX+s4, checkY-s4, checkX+checkW, checkY-s16);
        }
        g.setStroke(prev);
	}
	@Override public void paintComponent(Graphics g0) {
		// showTiming = true;
		if (showTiming)
			System.out.println("===== SetupRaceUI PaintComponents =====");
		if (!isOnTop)
			return;
		long timeStart = System.currentTimeMillis();
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
 
        int x = colorBox[0].x;
        int y = colorBox[0].y;

        shipSetTxt.setCaretPosition(shipSetTxt.getText().length());
        shipSetTxt.getCaret().setVisible(false);
        shipSetTxt.setFocusable(false);
        shipSetTxt.setLocation(x, y-s100-s44);
        shipSetBox.setBounds(x-s1, y-s100-s44, FIELD_W+s2, FIELD_H+s2);
        leaderName.setCaretPosition(leaderName.getText().length());
        leaderName.setLocation(x, y-s50); // BR: squeezed
        leaderBox.setBounds(x-s1, y-s50, FIELD_W+s2, FIELD_H+s2); // BR: squeezed
        homeWorld.setCaretPosition(homeWorld.getText().length());
        homeWorld.setLocation(x, y-s97); // BR: squeezed
		// modnar: test hover text
        // homeWorld.setToolTipText("<html> Homeworld Name is used as <br> the Galaxy Map when selecting <br> Map Shape [Text]. <br><br> (Unicode characters allowed)");
        homeWorldBox.setBounds(x-s1, y-s97, FIELD_W+s2, FIELD_H+s2); // BR: squeezed

		// modnar: use (slightly) better upsampling
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // background image
		if (retina)
			g.drawImage(backImg(), 0, 0, wFull, hFull, 0, 0, wFull, hFull, this);
		else
			g.drawImage(backImg(), 0, 0, this);

		if (showTiming)
			System.out.println("background image Time = " + (System.currentTimeMillis()-timeStart));

		drawButtons(g);
        drawFixButtons(g, false);
//      drawAllButtons = false;

        // selected race center img
		if (retina) {
			g.drawImage(raceImg(),  xCtrFrame(), yCtrFrame, xCtrFrame()+wCtrFrame, yCtrFrame+hCtrFrame,
					0, 0, retina(wCtrFrame), retina(hCtrFrame), this);
	        // draw Ship frames on the right
	        g.drawImage(fleetBackImg(), xFleet(), yFleet, xFleet()+fleetWidth, yFleet+fleetHeight,
	        		0, 0, retina(fleetWidth), retina(fleetHeight), null);
		}
		else {
			g.drawImage(raceImg(), xCtrFrame(), yCtrFrame, null);
	        // draw Ship frames on the right
	        g.drawImage(fleetBackImg(), xFleet(), yFleet, null);
		}

        // selected race box
        List<String> races = newGameOptions().startingRaceOptions();
        String selRace = newGameOptions().selectedPlayerRace();
        for (int i=0;i<races.size();i++) {
            if (races.get(i).equals(selRace)) {
                Rectangle box = raceBox[i];
               	drawRaceBox(g, i, box.x, box.y, null, false);
                Stroke prev = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(GameUI.setupFrame());
                g.draw(raceBox[i]);
                g.setStroke(prev);
                break;
            }
        }

        // hovering race box outline
        for (int i=0;i<raceBox.length;i++) {
            if (raceBox[i] == hoverBox) {
                Stroke prev = g.getStroke();
                g.setStroke(stroke2);
                g.setColor(Color.yellow);
                g.draw(raceBox[i]);
                g.setStroke(prev);
                break;
            }
        }

        // race icon
        //BufferedImage icon = newBufferedImage(race.flagNorm());
        if (retina)
        	g.drawImage(raceIcon(), xIcon(), yIcon, xIcon()+iconSize, yIcon+iconSize,
        			0, 0, retina(iconSize), retina(iconSize), null);
        else
        	g.drawImage(raceIcon(), xIcon(), yIcon, null);

        // draw race name
        Race race = Race.keyed(newGameOptions().selectedPlayerRace());
        int x0 = colorBox[0].x;
        int y0 = scaled(240); // BR: squeezed
        // BR: show custom race name and descriptions
        String raceName, desc1, desc2, desc3, desc4;
        if (playerIsCustom.get()) {
        	raceName = dataRace.setupName;
        	desc1 = dataRace.getDescription1();
        	desc2 = dataRace.getDescription2();
        	desc3 = dataRace.getDescription3(raceName);
        	desc4 = dataRace.getDescription4();
        }
        else {
        	raceName = race.setupName();
        	desc1 = race.getDescription1();
        	desc2 = race.getDescription2();
        	desc3 = race.getDescription3();
        	desc4 = race.getDescription4();
        }
        // \BR:
        int fs = scaledFontSize(g, raceName, scaled(200), 30, 10);
        g.setFont(font(fs));
        drawBorderedString(g0, raceName, 1, x0, y0, Color.black, Color.white);

        int dy = s16;
        String language = LanguageManager.current().selectedLanguageName();
        if (language.equals("Français")) {
        	dy = s15;
        	g.setFont(narrowFont(15));
        }
        else
        	g.setFont(narrowFont(16));
        // draw race desc #1
        int maxLineW = scaled(185); // modnar: right side extended, increase maxLineW
        y0 += s20; // BR: squeezed
        g.setColor(Color.black);
        List<String> desc1Lines = wrappedLines(g, desc1, maxLineW); // BR:
        g.fillOval(x0, y0-s8, s5, s5);
        for (String line: desc1Lines) {
            drawString(g,line, x0+s8, y0);
            y0 += dy;
        }

        // draw race desc #2
        y0 += s3;
        List<String> desc2Lines = wrappedLines(g, desc2, maxLineW); // BR:
        g.fillOval(x0, y0-s8, s5, s5);
        for (String line: desc2Lines) {
            drawString(g,line, x0+s8, y0);
            y0 += dy;
        }

        // modnar: draw race desc #4, with 'if' check
        if (desc4 != null) {
            y0 += s3;
            List<String> desc4Lines = wrappedLines(g, desc4, maxLineW); // BR:
            g.fillOval(x0, y0-s8, s5, s5);
            for (String line: desc4Lines) {
                drawString(g,line, x0+s8, y0);
                y0 += dy;
            }
        }
        
        // draw race desc #3
        y0 += s3;  // BR: squeezed
        // String desc3 = race.description3.replace("[race]", race.setupName());
        List<String> desc3Lines = scaledNarrowWrappedLines(g0, desc3, maxLineW+s8, 5, 16, 13);
        for (String line: desc3Lines) {
            drawString(g,line, x0, y0);
            y0 += dy;
        }

        // BR: draw Ship Set label
        String shipSetLbl = text("SETUP_SHIP_SET_LABEL");
        int x3 = colorBox[0].x;
        int y3 = colorBox[0].y-scaled(148);
        g.setFont(narrowFont(20));
        g.setColor(Color.black);
        drawString(g,shipSetLbl, x, y3);

        if (hoverBox == shipSetBox) {
            Stroke prev = g.getStroke();
            g.setStroke(stroke4);
            g.setColor(Color.yellow);
            g.draw(hoverBox);
            g.setStroke(prev);
        }

        // draw homeworld label
        String homeLbl = text("SETUP_HOMEWORLD_NAME_LABEL");
        x3 = colorBox[0].x;
        // y3 = colorBox[0].y-s100-s14;
        y3 = colorBox[0].y-scaled(101); // BR: squeezed
        g.setFont(narrowFont(20));
        g.setColor(Color.black);
        drawString(g,homeLbl, x3, y3);

        if (hoverBox == homeWorldBox) {
            Stroke prev = g.getStroke();
            g.setStroke(stroke4);
            g.setColor(Color.yellow);
            g.draw(hoverBox);
            g.setStroke(prev);
        }

        // draw leader name label
        String nameLbl = text("SETUP_LEADER_NAME_LABEL");
        x3 = colorBox[0].x;
        // y3 = colorBox[0].y-s60;
        y3 = colorBox[0].y-s54; // BR: squeezed
        g.setFont(narrowFont(20));
        g.setColor(Color.black);
        drawString(g,nameLbl, x3, y3);

        if (hoverBox == leaderBox) {
            Stroke prev = g.getStroke();
            g.setStroke(stroke4);
            g.setColor(Color.yellow);
            g.draw(hoverBox);
            g.setStroke(prev);
        }

        // draw empire color label
        String colorLbl = text("SETUP_RACE_COLOR");
        x3 = colorBox[0].x;
        y3 = colorBox[0].y-s7;
        g.setFont(narrowFont(20));
        g.setColor(Color.black);
        drawString(g,colorLbl, x3, y3);

        // draw selected & hovering colors
        for (int i=0;i<colorBox.length;i++) {
            int xC = colorBox[i].x;
            int yC = colorBox[i].y;
            int wC = colorBox[i].width;
            int hC = colorBox[i].height;
            Color c = newGameOptions().color(i);
            if (hoverBox == colorBox[i]) {
                Stroke prev = g.getStroke();
                g.setStroke(BasePanel.stroke2);
                g.setColor(Color.yellow);
                g.drawRect(xC, yC, wC, hC);
                g.setStroke(prev);
            }
            if (newGameOptions().selectedPlayerColor() == i) {
                g.setColor(c);
                g.fillRect(xC, yC, wC, hC);
                Stroke prev = g.getStroke();
                g.setStroke(BasePanel.stroke2);
                g.setColor(GameUI.setupFrame());
                g.drawRect(xC, yC, wC, hC);
                g.setStroke(prev);
            }
        }

		showGuide(g);

		if (showTiming)
			System.out.println("paintComponent() Time = " + (System.currentTimeMillis()-timeStart));
	}
    private void goToMainMenu() {
        buttonClick();
        RotPUI.instance().selectGamePanel();
        close();
    }
    private void goToGalaxySetup() {
        buttonClick();
        RotPUI.instance().selectSetupGalaxyPanel();
        close();
    }
	@Override public void clearImages() {
		super.clearImages();
//        backImg			= null;
        raceImg			= null;
        raceIconImg		= null;
        raceBackImg		= null;
        shipBackImg		= null;
        fleetBackImg	= null;
        resetFleetImages();
        resetRaceMug();
    }
    private void selectRace(int i) {
        String selRace = newGameOptions().selectedPlayerRace();
        List<String> races = newGameOptions().startingRaceOptions();
        if (i <= races.size()) {
            if (!selRace.equals(races.get(i))) {
                newGameOptions().selectedPlayerRace(races.get(i));
                raceChanged();
                repaint();
            }
        }
    }
    private void shipSetChanged() {
    	shipSetTxt.setText(playerShipSet.displaySet());
    	shipStyle = playerShipSet.realShipSetId();
        fleetBackImg = null;
        resetFleetImages();
    }
    private void noFogChanged() {
    	IGameOptions.noFogOnIcons.toggle();
    	resetRaceMug();
    	backImg = null;
        repaint();
   }
    private void checkBoxChanged() {
    	shipSetChanged();
    	repaint();
    }
    void raceChanged() {
        Race r   =  Race.keyed(newGameOptions().selectedPlayerRace());
      	dataRace = playerCustomRace.getRace(); // BR:
        r.resetSetupImage();
        r.resetMugshot();
        shipSetChanged();
        leaderName.setText(r.randomLeaderName());
        newGameOptions().selectedLeaderName(leaderName.getText());
        homeWorld.setText(r.defaultHomeworldName());
        newGameOptions().selectedHomeWorldName(homeWorld.getText());
        raceImg = null;
        raceIconImg = null;
    }
    private void selectColor(int i) {
        int selColor = newGameOptions().selectedPlayerColor();
        if (selColor != i) {
            newGameOptions().selectedPlayerColor(i);
            repaint();
        }
    }
    private void initTextField(JTextField value) {
        value.setBackground(GameUI.setupFrame());
        value.setBorder(newEmptyBorder(3,3,0,0));
        value.setPreferredSize(new Dimension(FIELD_W, FIELD_H));
        value.setFont(narrowFont(20));
        value.setForeground(Color.black);
        value.setCaretColor(Color.black);
        value.putClientProperty("caretWidth", s3);
        value.setVisible(true);
        value.addMouseListener(this);
        add(value);
    }
    private BufferedImage raceImg() {
        if (raceImg == null) {
            int newW = retina(wCtrFrame);
            int newH = retina(hCtrFrame);
            raceImg = new BufferedImage(newW, newH, TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) raceImg.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            String selRace = newGameOptions().selectedPlayerRace();
    		BufferedImage img = newBufferedImage(Race.keyed(selRace).setupImage());
            int imgW = img.getWidth(null);
            int imgH = img.getHeight(null);
    		g.drawImage(img, 0, 0, newW, newH, 0, 0, imgW, imgH, null);
    		g.dispose();
        }
        return raceImg;
    }
    private BufferedImage raceIcon() {
    	// raceIconImg = null;
        if (raceIconImg == null) {
            int newW = retina(iconSize);
            int newH = retina(iconSize);
            String selRace = newGameOptions().selectedPlayerRace();
    		Image image = Race.keyed(selRace).flagNorm();
            raceIconImg = resizeImage(image, newW, newH);
//            raceIconImg = new BufferedImage(newW, newH, TYPE_INT_ARGB);
//            Graphics2D g = (Graphics2D) raceIconImg.getGraphics();
//            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
//            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//    		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
//    		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//    		
//            String selRace    = newGameOptions().selectedPlayerRace();
//    		BufferedImage img = newBufferedImage(Race.keyed(selRace).flagNorm());
//            int imgW = img.getWidth(null);
//            int imgH = img.getHeight(null);
//    		g.drawImage(img, 0, 0, newW, newH, 0, 0, imgW, imgH, null);
//    		g.dispose();
        }
        return raceIconImg;
    }
    private BufferedImage fleetBackImg() {
        if (fleetBackImg == null)
            initFleetBackImg();
        return fleetBackImg;
    }
    private BufferedImage shipBackImg() {
        if (shipBackImg == null)
        	shipBackImg = getShipBackImg();
        return shipBackImg;
    }
    private BufferedImage raceBackImg() {
        if (raceBackImg == null)
    		raceBackImg = newGameOptions().getMugBackImg(retina(wIcon),
    				retina(hIcon), retina(rShadeIcon));
        return raceBackImg;
    }
    private void initFleetBackImg() {
		long timeStart = System.currentTimeMillis();
		fleetBackImg = new BufferedImage(retina(fleetWidth), retina(fleetHeight), TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) fleetBackImg.getGraphics();
        g.setComposite(AlphaComposite.SrcOver);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		BufferedImage shipBack = shipBackImg();
        for(int i=0; i<shipNum; i++) {
            int x = 0;
            int y = i * retina(shipDist);
            g.drawImage(shipBack, x, y, retina(shipWidth), retina(shipHeight), null);
            g.drawImage(getShipImage(i), x, y, retina(shipWidth), retina(shipHeight), null);
        }
        g.dispose();
		if (showTiming)
			System.out.println("initFleetBackImg() Time = " + (System.currentTimeMillis()-timeStart));
    }
    private BufferedImage getShipBackImg() {
		long timeStart = System.currentTimeMillis();
		int shipW = retina(shipWidth);
		int shipH = retina(shipHeight);
		BufferedImage shipBackImg = new BufferedImage(shipW, shipH, TYPE_INT_ARGB);
        Point2D center = new Point2D.Float(shipW/2f, shipH/2f);
        float radius = retina(s60);
        float[] dist = {0.0f, 0.55f, 0.85f, 1.0f};
        Color[] colors = {GameUI.raceCenterColor(), GameUI.raceCenterColor(), GameUI.raceEdgeColor(), GameUI.raceEdgeColor()};
        RadialGradientPaint p = new RadialGradientPaint(center, radius, dist, colors);
        Graphics2D g = (Graphics2D) shipBackImg.getGraphics();
		g.setPaint(p);
        g.fillRect(0, 0, shipW, shipH);
        g.dispose();
		if (showTiming)
			System.out.println("initShipBackImg() Time = " + (System.currentTimeMillis()-timeStart));
		return shipBackImg;
    }
    @Override protected void initBackImg() {
 		long timeStart	= System.currentTimeMillis();
        int w = getWidth();
        int h = getHeight();
        backImg = newOpaqueImage(w, h);
        Graphics2D g = (Graphics2D) backImg.getGraphics();
        setFontHints(g);

		// modnar: use (slightly) better upsampling
        // BR: Even better for unique rendering
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // background image
        Image back = GameUI.defaultBackground;
        int imgW = back.getWidth(null);
        int imgH = back.getHeight(null);
        g.drawImage(back, 0, 0, w, h, 0, 0, imgW, imgH, this);

        // draw title
        String title = text(guiTitleID);
        g.setFont(narrowFont(50));
        int sw = g.getFontMetrics().stringWidth(title);
        int x0 = (w - sw) / 2;
        int y0 = s80;
        drawBorderedString(g, title, 2, x0, y0, Color.darkGray, Color.white);

        // draw shading, modnar: extend right side
		// modnar: extend out for new Races
        g.setColor(GameUI.setupShade());
        g.fillRect(xShading(), yShading, wShading(), hShading); // BR: adjusted Shading right position

        // draw race frame
        g.setColor(GameUI.setupFrame());
        g.fillRect(xCtrFrame(), yCtrFrame, wCtrFrame, hCtrFrame);

        // draw race left gradient
		// modnar: extend out for new Races
        g.setPaint(GameUI.raceLeftBackground());
        g.fillRect(xLeftFrame(), yLeftFrame, wLeftFrame(), hLeftFrame);

        // draw race right gradient, modnar: extend right side
        g.setPaint(GameUI.raceRightBackground(xRightFrame()));
        g.fillRect(xRightFrame(), yRightFrame, wRightFrame, hRightFrame);

        int buttonH = s45;
        int buttonW = scaled(220);

        int xL = (xLeftFrame() + s15);
		int xM = xL + s90;
        int xR = xM + s95; // modnar: set column for new Races

        float fog = newGameOptions().noFogOnIcons()? 1.0f : 0.3f;
        Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER , fog);

        for (int i=0; i<5; i++) {
        	int y = (yLeftFrame + scaled(12 + i * 95));
        	int x = xL;
        	int num = 2*i;
        	BufferedImage mug = drawRaceBox(g, num, x, y, comp, true);
            raceBox[num].setBounds(x, y, invRetina(mug.getWidth()), invRetina(mug.getHeight()));
            num++;
            x = xM;
        	mug = drawRaceBox(g, num, x, y, comp, true);
            raceBox[num].setBounds(x, y, invRetina(mug.getWidth()), invRetina(mug.getHeight()));
        }
        for (int i=0; i<6; i++) { // For modnar new Races
        	int y = yLeftFrame + scaled(10 + i * 80);
        	int num = i+10;
        	BufferedImage mug = drawRaceBox(g, num,   xR, y, comp, true);
            raceBox[num].setBounds(xR, y, invRetina(mug.getWidth()), invRetina(mug.getHeight()));
        }

        // draw color buttons on right panel
        int xC = xColors();
        int yC = yColors;
        int wC = wColors;
        int hC = hColors;
        for (int i=0;i<MAX_COLORS;i++) {
            int yC1 = i%2 == 0 ? yC : yC+hC+s5;
            Color c = guiOptions().color(i);
            Color c0 = new Color(c.getRed(), c.getGreen(), c.getBlue(), 160); // modnar: less transparent unselected color
            g.setColor(c0);
            g.fillRect(xC, yC1, wC, hC);
            colorBox[i].setBounds(xC, yC1, wC, hC);
            if (i%2 == 1)
                xC += (wC+s5); // modnar: add new colors, less separation between color boxes
        }

        // draw Help Button
        helpBox.setBounds(s20,s20,s20,s25);
        g.setColor(darkBrownC);
        g.fillOval(helpBox.x, helpBox.y, helpBox.width, helpBox.height);
        
        // draw left button
        g.setPaint(GameUI.buttonLeftBackground());
        cancelBox.setBounds(scaled(710), scaled(685+10), buttonW, buttonH);
        cancelBox.fillButtonFullImg(g);

        // draw right button
        g.setPaint(GameUI.buttonRightBackground());
        exitBox.setBounds(scaled(950), scaled(685+10), buttonW, buttonH);
        exitBox.fillButtonFullImg(g);

		// setBounds DEFAULT button
		buttonH = s30;
		buttonW = defaultButtonWidth(g);
		int xB	= cancelBox.x - (buttonW + buttonSep);
		int yB	= cancelBox.y + s15;
		defaultBox.setBounds(xB, yB, buttonW, buttonH);

		// setBounds LAST button
		buttonH = s30;
		buttonW = lastButtonWidth(g);
		xB -= (buttonW + buttonSep);
		lastBox.setBounds(xB, yB, buttonW, buttonH);

		// setBounds USER button
		buttonW = userButtonWidth(g);
		xB -= (buttonW + buttonSep);
		userBox.setBounds(xB, yB, buttonW, buttonH);

		// setBounds GUIDE button
		buttonW = guideButtonWidth(g);
		xB = s20;
		guideBox.setBounds(xB, yB, buttonW, buttonH);

		// BR: Player Race Customization
        // far left button
        g.setFont(smallButtonFont());
        int smallButtonH = s30;
        int smallButtonW = g.getFontMetrics().stringWidth(text(customRaceKey)) + smallButtonMargin;
        xB = xLeftFrame();;
        yB = yCtrFrame + hCtrFrame + s10;
        g.setPaint(GameUI.buttonLeftBackground());
        playerRaceSettingBox.setBounds(xB, yB, smallButtonW, smallButtonH);
		playerRaceSettingBox.fillButtonFullImg(g);

        drawFixButtons(g, true);
        initButtonBackImg();
        
        g.dispose();
		if (showTiming) 
			System.out.println("initBackImg() Time = " + (System.currentTimeMillis()-timeStart));
    }
    private void initRaceMugImg() {
		long timeStart = System.currentTimeMillis();

        List<String> races = newGameOptions().startingRaceOptions();
		BufferedImage back = raceBackImg();
		int bW = back.getWidth();
		int bH = back.getHeight();
		for (int num=0; num<MAX_RACES; num++) {
			// modnar: 80% size box for newRaces
	        float raceBoxSize = num >= 10? 0.8f : 1.0f;
	        int  rbW = (int)(raceBoxSize * bW);
	        int  rbH = (int)(raceBoxSize * bH);

	        raceMugs[num] = new BufferedImage(rbW, rbH, TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) raceMugs[num].getGraphics();
            g.setComposite(AlphaComposite.SrcOver);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

	        g.drawImage(back, 0, 0, rbW, rbH, null); // modnar: 80% size box for newRaces

	        BufferedImage img = newBufferedImage(Race.keyed(races.get(num)).diploMugshotQuiet());
            int imgW = img.getWidth();
            int imgH = img.getHeight();
    		g.drawImage(img, 0, 0, rbW, rbH, 0, 0, imgW, imgH, null);
    		g.dispose();
		}
		if (showTiming)
			System.out.println("initFleetBackImg() Time = " + (System.currentTimeMillis()-timeStart));
    }
    private BufferedImage drawRaceBox(Graphics2D g, int num, int x, int y, Composite comp, boolean bg) {
        if (raceMugs[num] == null)
        	initRaceMugImg();
        BufferedImage mug = raceMugs[num];
        Composite prevC	  = g.getComposite();
        if (comp != null)
            g.setComposite(comp);
        if (retina)
        	g.drawImage(mug, x, y, x+invRetina(mug.getWidth()), y+invRetina(mug.getHeight()),
        			0, 0, mug.getWidth(), mug.getHeight(), null);
        else
        	g.drawImage(mug, x, y, null);
        g.setComposite(prevC);
        return mug;
    }
    private BufferedImage getShipImage(int shapeId) {
    	if (fleetImages[shapeId] != null)
    		return fleetImages[shapeId];
    	fleetImages = new BufferedImage[MAX_SHIP];
        ShipImage images = ShipLibrary.current().shipImage(shipStyle, shipSize, shapeId);
        Image img = icon(images.baseIcon()).getImage();
        int w0 = img.getWidth(null);
        int h0 = img.getHeight(null);
        float scale = min(shipWidth*2f/w0, shipHeight*2f/h0);
        int w1 = (int)(scale*w0);
        int h1 = (int)(scale*h0);
        BufferedImage resizedImg = new BufferedImage(w1,h1, TYPE_INT_ARGB);
        Graphics2D g = resizedImg.createGraphics();
		// modnar: one-step progressive image downscaling, mostly for Sakkra ships (higher-res image files)
		// there should be better methods
		if (scale < 0.5) {
			BufferedImage tmp = new BufferedImage(w0/2, h0/2, TYPE_INT_ARGB);
			Graphics2D g2D = tmp.createGraphics();
			// BR: Maximized Quality
	        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
	        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2D.drawImage(img, 0, 0, w0/2, h0/2, 0, 0, w0, h0, this);
			g2D.dispose();
			img = tmp;
			w0 = img.getWidth(null);
			h0 = img.getHeight(null);
			scale = scale*2;
		}
		// modnar: use (slightly) better downsampling
		// NOTE: drawing current ship design on upper-left of Design screen
		// https://docs.oracle.com/javase/tutorial/2d/advanced/quality.html
		// BR: Set to the best using modnar recommendations
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(img, 0, 0, w1, h1, 0, 0, w0, h0, null);
        g.dispose();
        fleetImages[shapeId] = resizedImg;
		return fleetImages[shapeId];
    }
    // BR: Display UI panel for Player Race Customization
    private void goToPlayerRaceCustomization() {
        buttonClick();
        EditCustomRaceUI.instance().open(this);
		setVisible(false);
    }
    private void goToRenameSpecies() {
        buttonClick();
		String langId = LanguageManager.selectedLanguageDir();
		switch (langId) {
			case "en":
				buttonClick();
//				IMainOptions.specieNameOptionsUI().toggle(null, GUI_ID, this);
				AllSubUI.nameSubUI().toggle(null, GUI_ID, this);
				setVisible(false);
				return;
			case "fr":
				buttonClick();
//				IMainOptions.specieNameOptionsFrUI().toggle(null, GUI_ID, this);
				AllSubUI.nameFrSubUI().toggle(null, GUI_ID, this);
				setVisible(false);
				return;
			default:
				misClick();
				return;
		}
    }
    @Override
    public String ambienceSoundKey() { 
        return GameUI.AMBIENCE_KEY;
    }
    @Override
    public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
    	checkModifierKey(e);
    	int k = e.getKeyCode();
        switch(k) {
	    	case KeyEvent.VK_F:
	        	noFogChanged();
	            return;
	    	case KeyEvent.VK_N:
	    		goToRenameSpecies();
				return;
	    	case KeyEvent.VK_R:
	    		playerIsCustom.set(false);
	        	newGameOptions().setRandomPlayerRace();
	        	raceChanged();
	        	repaint();
				return;
        	case KeyEvent.VK_ESCAPE:
            	doCancelBoxAction();
                return;
            case KeyEvent.VK_ENTER:
            	doNextBoxAction();
                return;
            default:
                return;
        }
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() > 3)
            return;
        if (hoverBox == null)
            return;
        if (hoverBox == helpBox) {
			if (SwingUtilities.isRightMouseButton(e))
				showHotKeys();
			else
				showHelp();
            return;
        }
        search:
        if (hoverBox == cancelBox)
        	doCancelBoxAction();
        else if (hoverBox == exitBox)
        	doNextBoxAction();
        else if (hoverBox == defaultBox)
        	doDefaultBoxAction();
        else if (hoverBox == guideBox)
			doGuideBoxAction();
        else if (hoverBox == userBox)
			doUserBoxAction();
        else if (hoverBox == lastBox)
			doLastBoxAction();
        // BR: Player Race customization
        else if (hoverBox == playerRaceSettingBox)
            goToPlayerRaceCustomization();
        else if (hoverBox == checkBox) {
            playerIsCustom.toggle(e, this);
            checkBoxChanged();
        }
        // BR: Player Ship Set Selection
        else if (hoverBox == shipSetBox) {
        	playerShipSet.toggle(e, this);
        	shipSetChanged();
        	repaint();
        }
        else {
            for (int i=0;i<raceBox.length;i++) {
                if (hoverBox == raceBox[i]) {
                    selectRace(i);
                    shipSetChanged();
                    break search;
                }
            }
            for (int i=0;i<colorBox.length;i++) {
                if (hoverBox == colorBox[i]) {
                    selectColor(i);
                    break search;
                }
            }
        }
    }
    @Override
    public void mouseEntered(MouseEvent e) {
        if (e.getComponent() == leaderName) {
            leaderName.requestFocus();
            hoverBox = leaderBox;
            repaint();
        }
        else if (e.getComponent() == homeWorld) {
            homeWorld.requestFocus();
            hoverBox = homeWorldBox;
            repaint();
        }
        else if (e.getComponent() == shipSetTxt) {
            hoverBox = shipSetBox;
            repaint();
        }
    }
    @Override
    public void mouseExited(MouseEvent e) {
    	super.mouseExited(e);
        if (e.getComponent() == leaderName) {
            newGameOptions().selectedLeaderName(leaderName.getText());
            RotPUI.instance().requestFocus();
        }
        else if (e.getComponent() == homeWorld) {
            newGameOptions().selectedHomeWorldName(homeWorld.getText());
            RotPUI.instance().requestFocus();
        }
        if (hoverBox != null) {
            hoverBox = null;
            repaint();
        }
    }
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (hoverBox == shipSetBox) {
        	playerShipSet.toggle(e);
        	shipSetChanged();
        	repaint();
        }
        else if (hoverBox == checkBox) {
            playerIsCustom.toggle(e);
            checkBoxChanged();
        }
    }
	private class ParamListPlayerSpecies extends ParamList {
		private ParamListPlayerSpecies(String gui, String name, List<String> list, String defaultValue) {
			super(gui, name, list, defaultValue);
			isDuplicate(true);
		}
		@Override public String	getOptionValue(IGameOptions options)	{
			return options.selectedPlayerRace();
		}
		@Override protected void setOptionValue(IGameOptions options, String value)	{
	        String selRace = newGameOptions().selectedPlayerRace();
            if (!selRace.equals(value)) {
                newGameOptions().selectedPlayerRace(value);
                raceChanged();
                repaint();
            }
		}
		@Override public String guideDefaultValue()	{
			return getRowGuide(defaultValueIndex());
		}
		@Override public String getRowGuide(int id)	{
			String key  = getGuiValue(id);
			String help = realLangLabel(key+LABEL_DESCRIPTION);
			if (help != null)
				return rowFormat(labelFormat(name(id)), help);

			Race   race		= Race.keyed(key);
			String raceName = race.setupName();
			if (key.startsWith(BASE_RACE_MARKER))
				help = labelFormat(name(id)) + "<i>(Original species)</i>&nbsp " + race.getDescription1();
			else
				help = labelFormat(raceName) + race.getDescription1();
			help += "<br>" + race.getDescription2()
				 +  "<br>" + race.getDescription3()
				 +  "<br>" + race.getDescription4();
			return help;
		}
		@Override public String	guideValue()	{ return text(get()); }
		@Override public void reInit(List<String> list) {
			if (list == null)
				super.reInit(guiOptions().getNewRacesOnOffList());
			else
				super.reInit(list);
		}
	}
	private class ParamStringPlayerHomeWorld extends ParamString {
		private ParamStringPlayerHomeWorld(String gui, String name, String defaultValue) {
			super(gui, name, defaultValue);
			isDuplicate(true);
		}
		@Override public String	getOptionValue(IGameOptions options)	{
			return options.selectedHomeWorldName();
		}
		@Override protected void setOptionValue(IGameOptions options, String value)	{
			options.selectedHomeWorldName(value);
            repaint();
		}
		@Override public String	guideValue()	{ return text(get()); }
		@Override public String	defaultValue()	{
	        Race r = Race.keyed(newGameOptions().selectedPlayerRace());
			return r.defaultHomeworldName();
		}
	}
	private class ParamStringPlayerLeader extends ParamString {
		private ParamStringPlayerLeader(String gui, String name, String defaultValue) {
			super(gui, name, defaultValue);
			isDuplicate(true);
		}
		@Override public String	getOptionValue(IGameOptions options)	{
			return options.selectedLeaderName();
		}
		@Override protected void setOptionValue(IGameOptions options, String value)	{
			options.selectedLeaderName(value);
            repaint();
		}
		@Override public String	guideValue()	{ return text(get()); }
		@Override public String	defaultValue()	{
	        Race r = Race.keyed(newGameOptions().selectedPlayerRace());
			return r.randomLeaderName();
		}
	}
}
