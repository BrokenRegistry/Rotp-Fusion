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
package rotp.ui.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import rotp.model.galaxy.StarSystem;
import rotp.model.game.IGameOptions;
import rotp.model.game.IMapOptions;
import rotp.ui.BasePanel;
import rotp.ui.main.GalaxyMapPanel;

public final class SystemNameDisplaySprite  extends MapControlSprite  {
	static Color greenC = Color.green;
	static Color darkGreenC = new Color(0,128,0);
	static FlightPathSprite sprite = new FlightPathSprite();

	Color extColor, normColor;

	public SystemNameDisplaySprite(int xOff, int yOff, int w, int h) {
		super(xOff, yOff, w, h, "SYSTEM_NAME_SPRITE");
		box.setParam(IMapOptions.systemNameDisplay);
		extColor =  newColor(0,0,192,64);
		normColor = newColor(32,32,192,128);
	}
	@Override
	public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
		IMapOptions.systemNameDisplay.toggle(e, null);
//		map.toggleSystemNameDisplay(rightClick);
	}
	@Override
	public void draw(GalaxyMapPanel map, Graphics2D g2) {
		int w = width;
		String detail ="";
		int fontSize = 13;
		int lineH	 = s14;
		List<String> detailLines = new ArrayList<>();

		if (hovering) {
			g2.setFont(narrowFont(fontSize));
			detail = IGameOptions.systemNameDisplay.getLangLabel("_EXT");
			int detailW = g2.getFontMetrics().stringWidth(detail);
			int w0 = detailW * 3/5;
			detailLines = wrappedLines(g2, detail, w0);
			while (detailLines.size() > 2) {
				w0 += detailW/10;
				detailLines = wrappedLines(g2, detail, w0);
			}
			w = width + s15 + w0;
		}
		drawBackground(map, g2, w);

		StarSystem home = galaxy().system(player().capitalSysId());
		int x2 = startX+width/2;
		int y2 = startY+scaled(2);

		Color c0 = g2.getColor();
		int cnr = s12;		
		g2.setColor(background);
		
		g2.fillRoundRect(startX, startY, width, height, cnr, cnr);

		Shape clipArea = new RoundRectangle2D.Float(startX, startY, width, height, cnr, cnr);
		g2.setClip(clipArea);

		home.drawStar(map, g2, x2, y2);

		if (map.showSystemData()) {
			g2.setColor(StarSystem.systemNameBackC);
			g2.fillRect(startX, y2+scaled(12), width, height);
			g2.setColor(StarSystem.systemDataBackC);
			g2.fillRect(startX, y2+scaled(12), width, s1);
		}
		g2.setFont(narrowFont(12));

		Color textC = map.hideSystemNames() ? darkGreenC : greenC;

		g2.setColor(textC);
		String name = map.parent().systemLabel(home);
		int sw = g2.getFontMetrics().stringWidth(name);

		drawString(g2,name, x2-(sw/2), y2+scaled(24));

		if (map.showSystemData()) {
			g2.setColor(StarSystem.systemDataBackC);
			g2.fillRect(startX, y2+scaled(26), width, height);
		}
		g2.setClip(null);
		g2.setColor(c0);

		if (hovering) {
			g2.setColor(Color.lightGray);
			g2.setFont(narrowFont(fontSize));
			int y1 = startY + height - BasePanel.s17;
			int x1 = startX + width + BasePanel.s10;
			if (detailLines.size() == 1)
				y1 += BasePanel.s8;
			for (String line: detailLines) {
				drawString(g2, line, x1, y1);
				y1 += lineH;
			}
		}
		drawBorder(map, g2, w, map.parent().shadeC(), false);
	}
}
