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
package rotp.ui.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import rotp.model.empires.Empire;
import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.Ship;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.galaxy.Transport;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.ui.BasePanel;
import rotp.ui.map.IMapHandler;

class WarViewPanel extends SystemPanel {
	private static final long serialVersionUID = 1L;
	private SpriteDisplayPanel parent;
	private BasePanel topPane;

	WarViewPanel(SpriteDisplayPanel p) {
		parent = p;
		init();
	}
	private void init() { initModel(); }

	@Override public IMapHandler mapHandler()	{ return parent.parent; }
	@Override public void animate() {
		topPane.animate();
		detailPane.animate();
	}
	@Override protected BasePanel topPane() {
		if (topPane == null)
			topPane = new SystemViewInfoPane(this);
		return topPane;	
	}
	@Override protected BasePanel detailPane() {
		if (detailPane == null)
			detailPane = new IncomingFleetsPane();
		return detailPane;
	}
	@Override protected BasePanel bottomPane() {
		if (bottomPane == null)
			bottomPane = new ExitWarViewButton(getWidth(), s40);
		return bottomPane;
	}
	@Override public StarSystem systemViewToDisplay()	{ return parent.systemViewToDisplay(); }

	private final class FleetRecord {
		private YearRecordMap playerYearMap = new YearRecordMap();
		private YearRecordMap alienYearMap  = new YearRecordMap();
		private String[] playerReport, alienReport;

		private String[] getPlayerReport()	{
			if (playerReport == null)
				playerReport = buildReport(playerYearMap);
			return playerReport;
		}
		private String[] getAlienReport()	{
			if (alienReport == null)
				alienReport = buildReport(alienYearMap);
			return alienReport;
		}
		private void add(Ship sh)	{
			Empire empire = sh.empire();
			boolean isPlayer = isPlayer(empire);
			YearRecordMap yearMap = isPlayer ? playerYearMap : alienYearMap;
			String name;
			Integer num;
			if (sh instanceof Transport) {
				Transport tranport = (Transport) sh;
				num	 = tranport.size();
				name = isPlayer ? text("WAR_VIEW_TRANSPORT") : text("WAR_VIEW_TRANSPORT", empire.raceName());
				add(yearMap, name, num);
			}
			else {
				ShipFleet fleet = (ShipFleet) sh;
				for (int id=0; id<ShipDesignLab.MAX_DESIGNS; id++) {
					num = fleet.num(id);
					if (num > 0) {
						ShipDesign design = fleet.design(id);
						if (!design.allowsCloaking()) {
							name = design.name();
							add(yearMap, name, num);
						}
					}
				}
			}
		}
		private void add(YearRecordMap yearMap, String name, Integer num)	{
			Integer oldVal = yearMap.get(name);
			if (oldVal == null)
				oldVal = 0;
			yearMap.put(name, oldVal + num);
		}
		private String[] buildReport(YearRecordMap yearMap)	{
			int reportSize = yearMap.size();
			String[] report = new String[reportSize];
			int idx = 0;
			for (Entry<String, Integer> entry : yearMap.entrySet()) {
				report[idx] = entry.getValue() + " " + entry.getKey();
				idx++;
			}
			return report;
		}
	}
	private final class FleetRecordMap extends TreeMap<Integer, FleetRecord> {
		private static final long serialVersionUID = 1L;
	}
	private final class YearRecordMap extends HashMap<String, Integer> {
		private static final long serialVersionUID = 1L;
	}
	private final class IncomingFleetsPane extends BasePanel implements MouseListener, MouseWheelListener {
		private static final long serialVersionUID = 1L;
		private static final int fontSize = 16;
		private final int lineH = scaled(fontSize);
		private final int scrollH = 3 * lineH;
		private Integer sysId;
		private int offsetY = 0;

		private Rectangle eventsBox = new Rectangle();
		private IncomingFleetsPane() {
			setBackground(Color.DARK_GRAY);
			addMouseListener(this);
			addMouseWheelListener(this);
		}
		private FleetRecordMap buildLists()	{
			Galaxy gal = galaxy();
			Empire pl = player();
			FleetRecordMap fleetsMap = new FleetRecordMap();
			if (sysId == null)
				return fleetsMap;

			// Process orbiting fleet
			FleetRecord orbitingRecord = new FleetRecord();
			fleetsMap.put(0, orbitingRecord);
			List<ShipFleet> orbitingFleet = pl.visibleOrbitingFleet(galaxy().system(sysId));
			for (Ship fleet : orbitingFleet) {
				orbitingRecord.add(fleet);
			}

			// Process moving fleets
			List<Ship> incomingFleet = pl.incomingKnownETAFleets(sysId);
			for (Ship fleet : incomingFleet) {
				Integer turn = fleet.travelTurnsRemainingAdjusted();
				FleetRecord incomingRecord = fleetsMap.get(turn);
				if (incomingRecord == null) {
					incomingRecord = new FleetRecord();
					fleetsMap.put(turn, incomingRecord);
				}
				incomingRecord.add(fleet);
			}
			// Process Ready to launch transports
			for (StarSystem sys: pl.allColonizedSystems()) {
				if (sys != null && sys.transportAmt > 0) {
					StarSystem dest = gal.system(sys.transportDestId);
					if (dest != null && dest.id == sysId) {
						Integer turn = ceil(sys.colony().transport().travelTimeAdjusted(dest));
						FleetRecord incomingRecord = fleetsMap.get(turn);
						if (incomingRecord == null) {
							incomingRecord = new FleetRecord();
							fleetsMap.put(turn, incomingRecord);
						}
						incomingRecord.add(incomingRecord.playerYearMap, text("WAR_VIEW_TRANSPORT"), sys.transportAmt);
					}
				}
			} 
			return fleetsMap;
		}
		@Override
		public void paintComponent(Graphics g0) {
			int w = getWidth();
			int h = getHeight();
			Graphics2D g = (Graphics2D) g0;
			super.paintComponent(g);

			StarSystem sys = systemViewToDisplay();
			if (sys == null) {
				sysId = null;
				return;
			}
			sysId = sys.id;
			offsetY = 0;
			FleetRecordMap fleetsMap = buildLists();

			String title = text("WAR_VIEW_TITLE");
			g.setFont(narrowFont(20));
			drawShadowedString(g, title, 2, s10, s23, MainUI.shadeBorderC(), SystemPanel.whiteLabelText);
			eventsBox.setBounds(s5, s30, w-s10, h-s35);

			g.setClip(eventsBox);
			g.setFont(narrowFont(fontSize));
			g.setColor(SystemPanel.blackText);
			int y0 = eventsBox.y+s20-offsetY;
			int x0 = eventsBox.x+s10;
			int xE = x0 + eventsBox.width-s20;

			for (Entry<Integer, FleetRecord> entry : fleetsMap.entrySet()) {
				String year = entry.getKey().toString();
				FleetRecord record = entry.getValue();
				String[] report = record.getPlayerReport();
				if (report != null && report.length > 0) {
					g.setColor(Color.GREEN);
					int yearOffset = ((report.length - 1) * lineH) /2;
					drawString(g, year, x0, y0 + yearOffset);
					for (String line: report) {
						drawString(g,line, x0+s40, y0);
						y0 += lineH;
					}
					y0 += s3;
					g.setColor(Color.GRAY);
					int yl = y0 - lineH;
					g.drawLine(x0, yl, xE, yl);
				}
				report = record.getAlienReport();
				if (report != null && report.length > 0) {
					g.setColor(Color.RED);
					int yearOffset = ((report.length - 1) * lineH) /2;
					drawString(g, year, x0, y0 + yearOffset);
					for (String line: report) {
						drawString(g,line, x0+s40, y0);
						y0 += lineH;
					}
					y0 += s3;
					g.setColor(Color.GRAY);
					int yl = y0 - lineH;
					g.drawLine(x0, yl, xE, yl);
				}
			}
			g.setClip(null);
		}
		@Override public void mouseClicked(MouseEvent e) { 
			if (SwingUtilities.isMiddleMouseButton(e))
				offsetY = 0;
			if (SwingUtilities.isRightMouseButton(e))
				offsetY += scrollH;
			if (SwingUtilities.isLeftMouseButton(e))
				offsetY = max(0, offsetY-scrollH);
		}
		@Override public void mousePressed(MouseEvent e) { }
		@Override public void mouseReleased(MouseEvent e) { }
		@Override public void mouseEntered(MouseEvent e)	{ clearHoverSprite(e, parent.parent); }
		@Override public void mouseExited(MouseEvent e)		{ setModifierKeysState(e); }
		@Override public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() > 0)
				offsetY += scrollH;
			else
				offsetY = max(0, offsetY-scrollH);
		}
	}
	private final class ExitWarViewButton extends BasePanel implements MouseListener, MouseMotionListener {
		private static final long serialVersionUID = 1L;
		private final Color redEdgeC	= new Color(92, 20, 20);
		private final Color redMidC		= new Color(117, 42, 42);
		private final Rectangle exitBox	= new Rectangle();
		private LinearGradientPaint buttonBack;
		private Rectangle hoverBox;
		private Shape textureClip;

		private ExitWarViewButton(int w, int h)	{ init(w,h); }
		private void init(int w, int h)	{
			Point2D start = new Point2D.Float(s2, 0);
			Point2D end = new Point2D.Float(w-s2, 0);
			float[] dist = {0.0f, 0.5f, 1.0f};
			Color[] redColors = {redEdgeC, redMidC, redEdgeC };
			buttonBack = new LinearGradientPaint(start, end, dist, redColors);
			setPreferredSize(new Dimension(w, h));
			setOpaque(false);
			addMouseListener(this);
			addMouseMotionListener(this);
		}
		private void clickAction(int numClicks)	{ GalaxyMapPanel.warView(false); }
		private void drawButton(Graphics2D g, LinearGradientPaint gradient, String label, Rectangle actionBox, int x1, int x2)	{
			int y = s4;
			int h = getHeight()-s7;
			int w = x2 - x1;
			//if (actionBox != null)
				actionBox.setBounds(x1, y, w, h);
			g.setColor(buttonShadowC);
			Stroke prev = g.getStroke();
			g.setStroke(stroke2);
			g.drawRoundRect(x1+s3, y+s2, w-s2, h, s10, s10);
			g.setStroke(prev);

			g.setPaint(gradient);
			g.fillRoundRect(x1,y,w,h,s10,s10);

			boolean hovering = (actionBox != null) && (actionBox == hoverBox);
			Color c0 = hovering ? SystemPanel.yellowText : SystemPanel.whiteText;

			g.setFont(narrowFont(22));
			int sw = g.getFontMetrics().stringWidth(label);
			int x0 = x1+((w-sw)/2);
			drawShadowedString(g, label, 3, x0, y+h-s9, SystemPanel.textShadowC, c0);

			g.setColor(c0);
			Stroke prev2 = g.getStroke();
			g.setStroke(stroke2);
			g.drawRoundRect(x1+s1,y,w-s2,h,s10,s10);
			g.setStroke(prev2);
		}
		@Override public String textureName()				{ return TEXTURE_BROWN; }
		@Override public Shape textureClip()				{ return textureClip; }
		@Override public void paintComponent(Graphics g0)	{
			super.paintComponent(g0);
			Graphics2D g = (Graphics2D) g0;
			drawButton(g,buttonBack, text("WAR_VIEW_EXIT"), exitBox, s2, getWidth()-s2);
		}
		@Override public void mouseClicked(MouseEvent e)	{ }
		@Override public void mousePressed(MouseEvent e)	{ }
		@Override public void mouseReleased(MouseEvent e)	{
			if (e.getButton() > 3)
				return;
			int x = e.getX();
			int y = e.getY();
			if (exitBox.contains(x,y)) {
				clickAction(e.getClickCount());
				return;
			}
		}
		@Override public void mouseEntered(MouseEvent e)	{ }
		@Override public void mouseExited(MouseEvent e)		{
			if (hoverBox != null) {
				hoverBox = null;
				repaint();
			}
		}
		@Override public void mouseDragged(MouseEvent e)	{ }
		@Override public void mouseMoved(MouseEvent e)		{
			int x = e.getX();
			int y = e.getY();
			Rectangle prevHover = hoverBox;
			hoverBox = null;
			if (exitBox.contains(x,y))
				hoverBox = exitBox;

			if (hoverBox != prevHover)
				repaint();
		}
	}
}
