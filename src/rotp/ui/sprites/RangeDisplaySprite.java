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

public final class RangeDisplaySprite extends MapControlSprite  {
	public RangeDisplaySprite(int xOff, int yOff, int w, int h)	{
		super(xOff, yOff, w, h, "RANGE_DISPLAY_SPRITE");
		box.setParam(IMapOptions.showShipRanges);
	}
	@Override
	public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
		IMapOptions.showShipRanges.toggle(e, null);
		//map.toggleShipRangesDisplay(rightClick);
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
			detail = IGameOptions.showShipRanges.getLangLabel("_EXT");
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

		int cnr = s12;
		g2.setColor(background);
		g2.fillRoundRect(startX, startY, width, height, cnr, cnr);

		g2.setColor(player().scoutBorderColor());
		g2.fillOval(startX+width/6, startY+height/6, width*2/3, height*2/3);

		g2.setColor(player().shipBorderColor());
		g2.fillOval(startX+width*3/10, startY+height*3/10, width*2/5, height*2/5);

		if (hovering) {
			g2.setColor(Color.lightGray);
			g2.setFont(narrowFont(fontSize));
			int y1 = startY + height - s17;
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
