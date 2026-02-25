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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import rotp.model.Sprite;
import rotp.model.empires.Empire;
import rotp.model.galaxy.Ship;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.galaxy.Transport;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipDesignLab;
import rotp.ui.BasePanel;
import rotp.ui.map.IMapHandler;

public class WarViewPanel extends SystemPanel {
	private static final long serialVersionUID = 1L;
	SpriteDisplayPanel parent;
	BasePanel topPane;

	public WarViewPanel(SpriteDisplayPanel p) {
		parent = p;
		init();
	}

	private void init() { initModel(); }
    public void releaseObjects() { }

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
	@Override
    public StarSystem systemViewToDisplay() {
        // return galaxy().system(0);
		Sprite clickedSprite = parent.parent.clickedSprite();
		if (clickedSprite instanceof StarSystem)
			return (StarSystem) clickedSprite;
		return galaxy().system(0);
	}
	final class FleetRecord {
		private YearRecordMap playerYearMap = new YearRecordMap();
		private YearRecordMap alienYearMap  = new YearRecordMap();
		private String[] playerReport, alienReport;

		String[] getPlayerReport()	{
			if (playerReport == null)
				playerReport = buildReport(playerYearMap);
			return playerReport;
		}
		String[] getAlienReport()	{
			if (alienReport == null)
				alienReport = buildReport(alienYearMap);
			return alienReport;
		}
		void add(Ship sh)	{
			Empire empire = sh.empire();
			boolean isPlayer = isPlayer(empire);
			YearRecordMap yearMap = isPlayer ? playerYearMap : alienYearMap;
			String name;
			Integer num;
			if (sh instanceof Transport) {
				Transport tranport = (Transport) sh;
				num	 = tranport.size();
				name = isPlayer ? "Transport" : (empire.raceName() + " Troop");
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
		private void add (YearRecordMap yearMap, String name, Integer num)	{
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
	final class FleetRecordMap extends TreeMap<Integer, FleetRecord> {
		private static final long serialVersionUID = 1L;
	}
	final class YearRecordMap extends HashMap<String, Integer> {
		private static final long serialVersionUID = 1L;
	}
	
	final class IncomingFleetsPane extends BasePanel implements MouseListener, MouseWheelListener {
		private static final long serialVersionUID = 1L;
		private static final int fontSize = 16;
		private final int lineH = scaled(fontSize);
		private final int scrollH = 3 * lineH;
		// private StarSystem sys, lastSys;
		private Integer sysId;
		//private FleetRecordMap fleetsMap = new FleetRecordMap();
		int offsetY = 0;
		
		Rectangle eventsBox = new Rectangle();
		IncomingFleetsPane() {
			setBackground(Color.DARK_GRAY);
			addMouseListener(this);
			addMouseWheelListener(this);
		}
		private FleetRecordMap buildLists() {
			FleetRecordMap fleetsMap = new FleetRecordMap();
			if (sysId == null)
				return fleetsMap;
			//fleetsMap.clear();
			// Process orbiting fleet
			FleetRecord orbitingRecord = new FleetRecord();
			fleetsMap.put(0, orbitingRecord);
			List<ShipFleet> orbitingFleet = player().visibleOrbitingFleet(galaxy().system(sysId));
			for (Ship fleet : orbitingFleet) {
				orbitingRecord.add(fleet);
			}
			List<Ship> incomingFleet = player().incomingKnownETAFleets(sysId);
			for (Ship fleet : incomingFleet) {
				Integer turn = fleet.travelTurnsRemainingAdjusted();
				FleetRecord incomingRecord = fleetsMap.get(turn);
				if (incomingRecord == null) {
					incomingRecord = new FleetRecord();
					fleetsMap.put(turn, incomingRecord);
				}
				incomingRecord.add(fleet);
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
		@Override public void mouseEntered(MouseEvent e)	{
			IMapHandler mapHandler = parent.parent;
			if (mapHandler.hoveringSprite() != null)
				mapHandler.hoveringSprite().mouseExit(null);
			mapHandler.hoveringOverSprite(null, true);
		}
		@Override public void mouseExited(MouseEvent e) { }
		@Override public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.getWheelRotation() > 0)
				offsetY += scrollH;
			else
				offsetY = max(0, offsetY-scrollH);
		}
	}
}
