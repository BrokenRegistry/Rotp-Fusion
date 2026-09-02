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
package rotp.ui.sprites;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.SystemPanel;

public abstract class MapControlSprite extends MapSprite {
    protected static Color background = new Color(0,0,0);
    protected int xOffset, yOffset, width, height;
    protected int startX, startY;

	protected MapControlSprite(int xOff, int yOff, int w, int h, String adviceKey) {
		xOffset	= scaled(xOff);
		yOffset	= scaled(yOff);
		width	= scaled(w);
		height	= scaled(h);
		box.setSize(width, height);
		box.setLocation(xOffset, yOffset);
		box.setSelectionBounds(startX-s3, startY-s3, width+s3+s3, height+s3+s3);
		box.setAdviceHelpKey(adviceKey);
	}
	protected void drawBackground(GalaxyMapPanel map, Graphics2D g2) {
        startX = xOffset >= 0 ? xOffset : map.getWidth()+xOffset;
        startY = yOffset >= 0 ? yOffset : map.getHeight()+yOffset;
		box.setLocation(startX, startY);
		box.setSelectionBounds(startX-s3, startY-s3, width+s3+s3, height+s3+s3);
        g2.setColor(map.parent().shadeC());
        g2.fillRect(startX-s5, startY-s5, width+s5+s5, height+s5+s5);
    }
    protected void drawBorder(GalaxyMapPanel map, Graphics2D g2) {
        Stroke str0 = g2.getStroke();

        if (hovering) {
            g2.setStroke(stroke2);
            g2.setColor(SystemPanel.yellowText);
        }
        else {
            g2.setStroke(stroke1);
            g2.setColor(map.parent().backC());
        }
		int cnr = s12;
        g2.drawRoundRect(startX, startY, width, height, cnr, cnr);
        g2.setStroke(str0);
    }
    public void drawBackground(GalaxyMapPanel map, Graphics2D g2, int w) {
        startX = xOffset >= 0 ? xOffset : map.getWidth()+xOffset;
        startY = yOffset >= 0 ? yOffset : map.getHeight()+yOffset;
		box.setLocation(startX, startY);
		box.setSelectionBounds(startX-s3, startY-s3, width+s3+s3, height+s3+s3);
        g2.setColor(map.parent().shadeC());
        g2.fillRect(startX-s5, startY-s5, w+s5+s5, height+s5+s5);
    }
    protected void drawBorder(GalaxyMapPanel map, Graphics2D g2, int w, Color c, boolean show) {
        Stroke str0 = g2.getStroke();
		int cnr = s12;

        g2.setStroke(stroke1);
        g2.setColor(c);
        g2.drawRoundRect(startX, startY, width, height, cnr, cnr);

        if (hovering || show) {
			g2.setStroke(stroke2);
            g2.setColor(SystemPanel.yellowText);
            g2.drawRoundRect(startX, startY, w, height, cnr, cnr);
            g2.setStroke(str0);
        }
    }
}