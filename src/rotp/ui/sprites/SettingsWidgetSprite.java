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
import java.awt.image.BufferedImage;
import java.util.List;

import rotp.ui.BasePanel;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.options.AllSubUI;
import rotp.ui.util.ParamSubUI;

public class SettingsWidgetSprite extends MapControlSprite {
	private BufferedImage image;
	private BufferedImage image() {
		if (image == null)
			image = settingsIcon(width, height, BasePanel.s12);
		return image;
	}
	public SettingsWidgetSprite(int xOff, int yOff, int w, int h) {
		xOffset = scaled(xOff);
		yOffset = scaled(yOff);
		width = scaled(w);
		height = scaled(h);
	}
	@Override
	public boolean acceptDoubleClicks()		 { return false; }
	@Override
	public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
		checkForChange(e);
		ParamSubUI optionsUI = AllSubUI.settingsSubUI();
		optionsUI.start(null);
	}
	@Override
	public void draw(GalaxyMapPanel map, Graphics2D g2) {
		int w = width;
		String label;
		int fontSize = 13;
		int labelW;
		String detail;
		List<String> detailLines = null;

		if (hovering) {
			g2.setFont(narrowFont(fontSize));
			label  = text("SETTINGS_MOD_SETTINGS_OPTIONS_BUTTON");
			detail = text("SETTINGS_MOD_SETTINGS_OPTIONS_DETAIL");
			labelW = g2.getFontMetrics().stringWidth(label);
			int w0 = labelW*3/5;
			detailLines = wrappedLines(g2, detail, w0);
			while (detailLines.size() > 2) {
				w0 += labelW/10;
				detailLines = wrappedLines(g2, detail, w0);
			}
			w = width + BasePanel.s15 + w0;
		}
		drawBackground(map, g2, w);

		g2.drawImage(image(), startX, startY, map);

		if (hovering) {
			g2.setColor(Color.lightGray);
			g2.setFont(narrowFont(fontSize));
			int y1 = startY + height - BasePanel.s17;
			int x1 = startX + width + BasePanel.s10;
			if (detailLines.size() == 1)
				y1 += BasePanel.s8;
			for (String line: detailLines) {
				drawString(g2, line, x1, y1);
				y1 += BasePanel.s14;
			}			
		}
		drawBorder(map, g2, w, map.parent().shadeC(), false);
	}
}
