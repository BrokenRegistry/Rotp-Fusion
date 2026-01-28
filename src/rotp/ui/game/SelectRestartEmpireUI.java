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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
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
import java.util.LinkedList;

import javax.swing.SwingUtilities;

import rotp.model.empires.Empire.EmpireBaseData;
import rotp.model.empires.species.DNAFactory;
import rotp.model.galaxy.GalaxyFactory.GalaxyCopy;
import rotp.model.game.GameSession;
import rotp.model.game.IGameOptions;
import rotp.ui.BasePanel;
import rotp.ui.NoticeMessage;
import rotp.ui.RotPUI;
import rotp.ui.main.SystemPanel;

final class SelectRestartEmpireUI  extends BasePanel implements MouseListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;
	private static final int MAX_LENGTH  = 20;
	
	private static final SelectRestartEmpireUI instance = new SelectRestartEmpireUI();

	private LoadListingPanel listingPanel;
	private LinkedList<Integer> valueList	 = new LinkedList<>();
	private LinkedList<String> empireList	 = new LinkedList<>();
	private LinkedList<String> raceList	  	 = new LinkedList<>();
	private LinkedList<String> abilitiesList = new LinkedList<>();
	private LinkedList<String> homeworldList = new LinkedList<>();
	private Integer selectedOpponent = -1;
	private Shape hoverBox;
	private Shape selectBox;
	private int selectIndex;
	private int start = 0;
	private int end   = 0;
	private GalaxyCopy oldGalaxy;
	
	private int buttonW, button1X, button2X;
    private boolean loading = false;

	private final Rectangle cancelBox	 = new Rectangle();
	private final Rectangle loadBox		 = new Rectangle();
	private final Rectangle empireBox	 = new Rectangle();
	private final Rectangle homeworldBox = new Rectangle();
	private final Rectangle raceBox		 = new Rectangle();
	private final Rectangle abilitiesBox = new Rectangle();
	private final Rectangle valueBox	 = new Rectangle();
	private LinearGradientPaint[] loadBackC;
	private LinearGradientPaint[] cancelBackC;
	private GameSession newSession;
	
	private final int wE = scaled(250);
	private final int wH = scaled(150);
	private final int wR = scaled(150);
	private final int wA = scaled(150);
	private final int wV = scaled(50);
	private final int wM = s30; // margin


	public SelectRestartEmpireUI()			{ initModel(); }
	static SelectRestartEmpireUI instance()	{ return instance; }
	// BR: for restarting with new options
	void init(GalaxyCopy oG, GameSession newSession) {
		this.newSession = newSession;
		oldGalaxy = oG;
		empireList.clear();
		valueList.clear();
		raceList.clear();
		empireList.clear();
		abilitiesList.clear();
		homeworldList.clear();

		hoverBox = null;
		selectBox = null;
		selectIndex = -1;
		start = 0;
		end = 0;
		selectedOpponent = -1;

		loadListing();
	}
	@Override public void open() {
		super.open();
		enableGlassPane(this);
		repaint();
	}

	private void loadListing() {
		empireList.clear();
		valueList.clear();
		raceList.clear();
		empireList.clear();
		abilitiesList.clear();
		homeworldList.clear();
		DNAFactory cr;

		for (EmpireBaseData e : oldGalaxy.empires()){
			String name = e.empireName;
			String home = e.homeSys.starName;
			String race = e.raceName;
			String abilities = e.dataName;
			if (e.isCustomRace)
				cr = new DNAFactory(e.raceOptions);
			else
				cr = new DNAFactory(e.dataRaceKey);

			empireList.add(name);
			valueList.add(Math.round(cr.getTotalCost()));
			raceList.add(race);
			abilitiesList.add(abilities);
			homeworldList.add(home);
		}
		if (!homeworldList.isEmpty()) {
			selectIndex = 0;
			selectedOpponent = start+selectIndex;
		}
	}
	private int selectedEmpire(int index) {
		return start+index;
	}
	private void initGradients() {
		int w	 = getWidth();
		buttonW	 = s100+s100;
		button1X = (w/2)-s10-buttonW;
		button2X = (w/2)+s10;
		Point2D start1 = new Point2D.Float(button1X, 0);
		Point2D end1   = new Point2D.Float(button1X+buttonW, 0);
		Point2D start2 = new Point2D.Float(button2X, 0);
		Point2D end2   = new Point2D.Float(button2X+buttonW, 0);
		float[] dist   = {0.0f, 0.5f, 1.0f};

		Color brownEdgeC = new Color(100,70,50);
		Color brownMidC  = new Color(161,110,76);
		Color[] brownColors = {brownEdgeC, brownMidC, brownEdgeC };

		Color grayEdgeC = new Color(59,66,65);
		Color grayMidC  = new Color(107,118,117);
		Color[] grayColors = {grayEdgeC, grayMidC, grayEdgeC };

		loadBackC   = new LinearGradientPaint[2];
		cancelBackC = new LinearGradientPaint[2];

		loadBackC[0]   = new LinearGradientPaint(start1, end1, dist, brownColors);
		cancelBackC[0] = new LinearGradientPaint(start2, end2, dist, brownColors);
		loadBackC[1]   = new LinearGradientPaint(start1, end1, dist, grayColors);
		cancelBackC[1] = new LinearGradientPaint(start2, end2, dist, grayColors);
	}
	@Override public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (loadBackC == null)
			initGradients();
		Image back = GameUI.defaultBackground;
		int imgW = back.getWidth(null);
		int imgH = back.getHeight(null);
		g.drawImage(back, 0, 0, getWidth(), getHeight(), 0, 0, imgW, imgH, this);

		// if loading, draw notice
        if (loading) {
            NoticeMessage.setStatus(text("LOAD_GAME_LOADING"));
            drawNotice(g, 30);
        }		
	}
	private void initModel() {
		addMouseWheelListener(this);
		listingPanel = new LoadListingPanel();
		setLayout(new BorderLayout());
		add(listingPanel, BorderLayout.CENTER);
	}
	private void scrollDown() {
		int prevStart  = start;
		int prevSelect = selectIndex;
		start = max(0, min(start+1, empireList.size()-MAX_LENGTH));
		if ((start == prevStart) && (selectIndex >= 0))
			selectIndex = min(selectIndex+1, empireList.size()-1, MAX_LENGTH-1);
		selectedOpponent = selectedEmpire(selectIndex);
		if ((prevStart != start) || (prevSelect != selectIndex))
			repaint();
	}
	private void scrollUp() {
		int prevStart = start;
		int prevSelect = selectIndex;
		start = max(start-1, 0);
		if ((start == prevStart) && (selectIndex >= 0))
			selectIndex = max(selectIndex-1, 0);
		selectedOpponent = selectedEmpire(selectIndex);
		if ((prevStart != start) || (prevSelect != selectIndex))
			repaint();
	}
	@Override public void mouseWheelMoved(MouseWheelEvent e) {
		int count = e.getUnitsToScroll();
		if (count < 0)
			scrollUp();
		else
			scrollDown();
	}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {
		if (e.getButton() > 3)
			return;
        if (e.getButton() > 3)
            return;
        RotPUI.instance().selectMainPanel(false);
	}
	@Override public void keyPressed(KeyEvent e) {
		int k = e.getKeyCode();
		switch(k) {
			case KeyEvent.VK_DOWN:	scrollDown(); return;
			case KeyEvent.VK_UP:	scrollUp();   return;
			case KeyEvent.VK_L:
				if (e.isAltDown()) {
					debugReloadLabels(this);
					return;
				}
				if (canSelect())
					setOpponent(selectedOpponent);
				return;
			case KeyEvent.VK_ENTER:
				if (canSelect())
					setOpponent(selectedOpponent);
				return;
			case KeyEvent.VK_ESCAPE:
			case KeyEvent.VK_C:	cancelSelect();	return;
		}
	}
	private boolean canSelect()	{ return selectIndex >= 0; }
	private boolean canLoad()	  { return selectedOpponent>=0; }
	public void setOpponent(int idx) {
		buttonClick();
		oldGalaxy.selectEmpire(idx);
		GameSession.instance().loadSession(newSession);
		loading = true;
		repaint();

		final Runnable load = () -> {
			long start = System.currentTimeMillis();
			IGameOptions opts = newGameOptions();
			GameUI.gameName = generateGameName(opts);
			GameSession.instance().restartGame(opts, oldGalaxy);
			RotPUI.instance().mainUI().checkMapInitialized();
			RotPUI.instance().selectIntroPanel();
			log("TOTAL GAME START TIME:" +(System.currentTimeMillis()-start));
			log("Game Name; "+GameUI.gameName);
	     	oldGalaxy  = null;
	     	newSession = null;
		};
		SwingUtilities.invokeLater(load);
	}
	private void cancelSelect() {
		buttonClick();
		disableGlassPane();
		RotPUI.instance().selectSetupGalaxyPanel();
	}
	// ==================== Nested Class ====================
	//
	private class LoadListingPanel extends BasePanel implements MouseListener, MouseMotionListener {
		private static final long serialVersionUID = 1L;
		private final Rectangle[] gameBox = new Rectangle[MAX_LENGTH];
		private final Rectangle listBox = new Rectangle();
		private boolean dragging = false;
		private int lastMouseY;
		private int yOffset = 0;
		private int lineH = s50;
		public LoadListingPanel() {
			init();
		}
		private void init() {
			setOpaque(false);
			for (int i=0;i<gameBox.length;i++)
				gameBox[i] = new Rectangle();
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		@Override
		public void paintComponent(Graphics g0) {
			super.paintComponent(g0);
			Graphics2D g = (Graphics2D) g0;

			for (int i=0;i<gameBox.length;i++)
				gameBox[i].setBounds(0,0,0,0);

			int w = getWidth();
			lineH = s22;

			String title = text("SETUP_SELECT_EMPIRE_TITLE");
			g.setFont(font(60));
			int sw = g.getFontMetrics().stringWidth(title);
			drawShadowedString(g, title, 1, 3, (w-sw)/2, scaled(120), GameUI.titleShade(), GameUI.titleColor());

			end = min(empireList.size(), start+MAX_LENGTH);

			int w0 = wE+wH+wR+wA+wV+wM+wM;
			int x0 = (w-w0)/2;
			int h0 = s5+(MAX_LENGTH*lineH);
			int y0 = scaled(180);

			// draw back mask
			int wTop  = s10;
			int wSide = s40;
			int wBottom = s80;
			g.setColor(GameUI.loadListMask());
			g.fillRect(x0-wSide, y0-wTop, w0+wSide+wSide, h0+lineH+wTop+wBottom);

			g.setColor(GameUI.raceCenterColor());
			g.fillRect(x0, y0, w0, h0+lineH);

			g.setColor(GameUI.sortLabelBackColor());
			g.fillRect(x0, y0, w0, lineH);

			int bX = x0+wM;
			int bY = y0+lineH;
			drawEmpireButton	(g, bX, bY);
			bX+=wE;
			drawHomeworldButton	(g, bX, bY);
			bX+=wH;
			drawRaceButton		(g, bX, bY);
			bX+=wR;
			drawAbilitiesButton	(g, bX, bY);
			bX+=wA;
			drawValueButton		(g, bX, bY);
			// draw list of games to load
			int lineY = y0+s5+lineH;
			listBox.setBounds(x0, y0, w0, h0);
			for (int i=start;i<start+MAX_LENGTH;i++) {
				int boxIndex = i-start;
				if (boxIndex == selectIndex) {
					g.setPaint(GameUI.loadHoverBackground());
					g.fillRect(x0+s20, lineY-s4, w0-s40, lineH);
				}
				else if (i % 2 == 1) {
					g.setPaint(GameUI.loadHiBackground());
					g.fillRect(x0+s20, lineY-s4, w0-s40, lineH);
				}
				if (i<end) {
					drawSelectEmpire(g, boxIndex, 
							empireList.get(i),
							homeworldList.get(i),
							raceList.get(i),
							abilitiesList.get(i),
							valueList.get(i),
							x0, lineY, w0, lineH);
					gameBox[boxIndex].setBounds(x0,lineY,w0,lineH);
				}
				lineY += lineH;
			}
			// draw load button
			int buttonY = lineY+s20;
			int buttonH = s40;
			loadBox.setBounds(button1X,buttonY,buttonW,buttonH);
			g.setColor(SystemPanel.buttonShadowC);
			g.fillRoundRect(button1X+s1,buttonY+s3,buttonW,buttonH,s8,s8);
			g.fillRoundRect(button1X+s2,buttonY+s4,buttonW,buttonH,s8,s8);
			g.setPaint(loadBackC[GameUI.opt()]);
			g.fillRoundRect(button1X,buttonY,buttonW,buttonH,s5,s5);

			String text1 = text("SETUP_SELECT_EMPIRE_SELECT_BUTTON");
			g.setFont(narrowFont(30));
			int sw1 = g.getFontMetrics().stringWidth(text1);
			int x1 = button1X + ((buttonW-sw1)/2);

			boolean hoveringLoad = (loadBox == hoverBox) && canLoad();
			Color textC = hoveringLoad ? GameUI.textHoverColor() : GameUI.textColor();
			drawShadowedString(g, text1, 0, 2, x1, buttonY+buttonH-s10, GameUI.textShade(), textC);

			if (hoveringLoad) {
				Stroke prev2 = g.getStroke();
				g.setStroke(stroke1);
				g.drawRoundRect(button1X,buttonY,buttonW,buttonH,s5,s5);
				g.setStroke(prev2);
			}

			// draw cancel button
			cancelBox.setBounds(button2X,buttonY,buttonW,buttonH);
			g.setColor(SystemPanel.buttonShadowC);
			g.fillRoundRect(button2X+s1,buttonY+s3,buttonW,buttonH,s8,s8);
			g.fillRoundRect(button2X+s2,buttonY+s4,buttonW,buttonH,s8,s8);
			g.setPaint(cancelBackC[GameUI.opt()]);
			g.fillRoundRect(button2X,buttonY,buttonW,buttonH,s5,s5);

			String text2 = text("SETUP_SELECT_EMPIRE_CANCEL_BUTTON");
			g.setFont(narrowFont(30));
			int sw2 = g.getFontMetrics().stringWidth(text2);
			int x2 = button2X + ((buttonW-sw2)/2);

			textC = (cancelBox == hoverBox) ? GameUI.textHoverColor() : GameUI.textColor();
			drawShadowedString(g, text2, 0, 2, x2, buttonY+buttonH-s10, GameUI.textShade(), textC);

			if (cancelBox == hoverBox) {
				Stroke prev2 = g.getStroke();
				g.setStroke(stroke1);
				g.drawRoundRect(button2X,buttonY,buttonW,buttonH,s5,s5);
				g.setStroke(prev2);
			}
		}
		private void drawEmpireButton(Graphics g, int x, int y) {
			Color textC = empireBox == hoverBox ? GameUI.textHoverColor() : GameUI.textColor();
			g.setFont(narrowFont(20));
			String title = text("SETUP_SELECT_EMPIRE_EMPIRE");
			int sw = g.getFontMetrics().stringWidth(title);
			empireBox.setBounds(x, y-lineH,sw,lineH);
			g.setColor(textC);
			drawString(g,title, x, y-(lineH/5));
		}
		private void drawHomeworldButton(Graphics g, int x, int y) {
			Color textC = homeworldBox == hoverBox ? GameUI.textHoverColor() : GameUI.textColor();
			g.setFont(narrowFont(20));
			String title = text("SETUP_SELECT_EMPIRE_HOME");
			int sw = g.getFontMetrics().stringWidth(title);
			homeworldBox.setBounds(x, y-lineH, sw, lineH);
			g.setColor(textC);
			drawString(g,title, x, y-(lineH/5));
		}
		private void drawRaceButton(Graphics g, int x, int y) {
			Color textC = raceBox == hoverBox ? GameUI.textHoverColor() : GameUI.textColor();
			g.setFont(narrowFont(20));
			String title = text("SETUP_SELECT_EMPIRE_RACE");
			int sw = g.getFontMetrics().stringWidth(title);
			raceBox.setBounds(x, y-lineH, sw, lineH);
			g.setColor(textC);
			drawString(g,title, x, y-(lineH/5));
		}
		private void drawAbilitiesButton(Graphics g, int x, int y) {
			Color textC = abilitiesBox == hoverBox ? GameUI.textHoverColor() : GameUI.textColor();
			g.setFont(narrowFont(20));
			String title = text("SETUP_SELECT_EMPIRE_ABILITIES");
			int sw = g.getFontMetrics().stringWidth(title);
			abilitiesBox.setBounds(x, y-lineH, sw, lineH);
			g.setColor(textC);
			drawString(g,title, x, y-(lineH/5));
		}
		private void drawValueButton(Graphics g, int x, int y) {
			Color textC = valueBox == hoverBox ? GameUI.textHoverColor() : GameUI.textColor();
			g.setFont(narrowFont(20));
			String title = text("SETUP_SELECT_EMPIRE_VALUE");
			int sw = g.getFontMetrics().stringWidth(title);
			valueBox.setBounds(x, y-lineH, wV, lineH);
			g.setColor(textC);
			drawString(g,title, x+wV-sw, y-(lineH/5));
		}
		private void scrollY(int deltaY) {
			yOffset += deltaY;
			if (yOffset > lineH) {
				scrollUp();
				yOffset -= lineH;
			}
			else if (yOffset < -lineH) {
				scrollDown();
				yOffset += lineH;
			}
		}
		private void drawSelectEmpire(Graphics2D g, int index,
				String empireT,
				String homeT,
				String raceT,
				String abilitiesT,
				int valueT, 
				int x, int y, int w, int h) {
			Color c0 = (index != selectIndex) && (hoverBox == gameBox[index]) ? GameUI.loadHoverBackground() : Color.black;
			g.setColor(c0);
			g.setFont(narrowFont(20));
			int sw = g.getFontMetrics().stringWidth(empireT);
			int maxW = w-scaled(250);
			g.setClip(x+s25, y+h-s30, maxW, s30);

			int tX = x+wM;
			int tY = y+h-s8;
			drawString(g, empireT, tX, tY);
			g.setClip(null);
			if (sw > maxW)
				drawString(g,text("LOAD_GAME_TOO_LONG"), x+s25+maxW, tY);

			tX+=wE;
			sw = g.getFontMetrics().stringWidth(homeT);
			drawString(g, homeT, tX, tY);

			tX+=wH;
			sw = g.getFontMetrics().stringWidth(raceT);
			drawString(g, raceT, tX, tY);

			tX+=wR;
			sw = g.getFontMetrics().stringWidth(abilitiesT);
			drawString(g, abilitiesT, tX, tY);

			tX+=wA+wV;
			String valueStr = shortFmt(valueT);
			sw = g.getFontMetrics().stringWidth(valueStr);
			drawString(g, valueStr, tX-sw, tY);
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			mouseMoved(e);
		}
		@Override
		public void mouseMoved(MouseEvent e) {
			int x = e.getX();
			int y = e.getY();
			int deltaY = y - lastMouseY;
			lastMouseY = y;

			if (dragging && listBox.contains(x,y))
				scrollY(deltaY);

			Shape oldHover = hoverBox;
			hoverBox = null;

			if (loadBox.contains(x,y))
				hoverBox = loadBox;
			else if (cancelBox.contains(x,y))
				hoverBox = cancelBox;
			else {
				for (int i=0;i<gameBox.length;i++) {
					if (gameBox[i].contains(x,y))
						hoverBox = gameBox[i];
				}
			}

			if (hoverBox != oldHover)
				repaint();
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {  }
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
		public void mousePressed(MouseEvent arg0) {
			dragging = true;
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			dragging = false;
			if (e.getButton() > 3)
				return;
			int count = e.getClickCount();
			if (hoverBox == null)
				return;

			if (hoverBox == loadBox) {
				setOpponent(selectedOpponent);
				return;
			}
			if (hoverBox == cancelBox) {
				cancelSelect();
				return;
			}
			if (count == 2)
				setOpponent(selectIndex);
			if (hoverBox != selectBox) {
				softClick();
				selectBox = hoverBox;
				for (int i=0;i<gameBox.length;i++) {
					if (gameBox[i] == hoverBox)
						selectIndex = i;
				}
				if (!empireList.isEmpty()) {
					selectedOpponent = selectedEmpire(selectIndex);
				}
				instance.repaint();
			}
		}
	}
}
