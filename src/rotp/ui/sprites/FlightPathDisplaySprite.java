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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import rotp.model.game.IGameOptions;
import rotp.model.game.IMapOptions;
import rotp.ui.main.GalaxyMapPanel;

public final class FlightPathDisplaySprite extends MapControlSprite  {
	static Color yellowC = Color.yellow;
	static Color redC = Color.red;
	static Color greenC = Color.green;
	static Color darkGreenC = new Color(0,128,0);
	static FlightPathSprite sprite = new FlightPathSprite();
	Color extColor, normColor;

	public FlightPathDisplaySprite(int xOff, int yOff, int w, int h) {
		super(xOff, yOff, w, h, "FLIGHTPATH_SPRITE");
		box.setParam(IMapOptions.flightPathDisplay);
		extColor =  newColor(0,0,192,64);
		normColor = newColor(32,32,192,128);
	}
	@Override
	public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
		IMapOptions.flightPathDisplay.toggle(e, null);
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
			detail = IGameOptions.flightPathDisplay.getLangLabel("_EXT");
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

		int x1a = startX+s5;
		int x1b = startX+width-s5;
		int y1 = startY+height-s5;
		int x2 = startX+s15;
		int y2 = startY+s15;

		int cnr = s12;
		g2.setColor(background);
		g2.fillRoundRect(startX, startY, width, height, cnr, cnr);

		g2.setClip(startX, startY, width, height);
		galaxy().system(player().capitalSysId()).drawStar(map, g2, x2, y2);

		if (map.showImportantFlightPaths())
			sprite.draw(g2, 0, 20, false, x1b,y1,x2,y2, redC);
		if (map.showAllFlightPaths())
			sprite.draw(g2, 0, 20, false, x1a,y1,x2,y2, yellowC);

		g2.setClip(null);

		if (hovering) {
			g2.setColor(Color.lightGray);
			g2.setFont(narrowFont(fontSize));
			y1 = startY + height - s17;
			int x1 = startX + width + s10;
			if (detailLines.size() == 1)
				y1 += s8;
			for (String line: detailLines) {
				drawString(g2, line, x1, y1);
				y1 += lineH;
			}
		}
		drawBorder(map, g2, w, map.parent().shadeC(), false);
	}
}
