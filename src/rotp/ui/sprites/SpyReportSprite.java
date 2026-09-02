

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
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import rotp.ui.RotPUI;
import rotp.ui.main.GalaxyMapPanel;


public final class SpyReportSprite extends MapControlSprite {
    static LinearGradientPaint alertBack;
    private int yOrigin, yShift;
    public SpyReportSprite(int xOff, int yOff, int w, int h, int shift) {
		super(xOff, yOff, w, h, "MAIN_HELP_1I");
        yShift  = scaled(yOff-shift);
        yOrigin = yOffset;
    }
	public void updateLocation()	{ yOffset = session().aFewMoreTurns() ? yOrigin : yShift; }
	@Override public boolean isSelectableAt(GalaxyMapPanel map, int mapX, int mapY) {
		hovering = map.parent().showSpyReportIcon() && super.isSelectableAt(map, mapX, mapY);
		return hovering;
	}
    @Override
    public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
        RotPUI.instance().showSpyReport();
        map.parent().hoveringOverSprite(null);
    }
    @Override
    public void draw(GalaxyMapPanel map, Graphics2D g2) {
        if (!map.parent().showSpyReportIcon())
            return;

        Image img = image("SPY_REPORT");

        drawBackground(map,g2,width);
        g2.drawImage(img, startX, startY, width, height,null);

        drawBorder(map, g2, width, Color.black, false);
    }
    @Override
	public void drawBackground(GalaxyMapPanel map, Graphics2D g2, int w) {
        startX = xOffset >= 0 ? xOffset : map.getWidth()+xOffset;
        startY = yOffset >= 0 ? yOffset : map.getHeight()+yOffset;
		box.setLocation(startX, startY);
		box.setSelectionBounds(startX-s3, startY-s3, width+s3+s3, height+s3+s3);
        if (alertBack == null) {
            float[] dist = {0.0f, 1.0f};
            Color topC = new Color(176,108,6);
            Color botC = new Color(203,139,36);
            Point2D start = new Point2D.Float(0, startY);
            Point2D end = new Point2D.Float(0, startY+height);
            Color[] colors = {topC, botC };
            alertBack = new LinearGradientPaint(start, end, dist, colors);
        }
        int cnr = s12;
        g2.setPaint(alertBack);
        g2.fillRoundRect(startX, startY, w, height, cnr, cnr);
    }
}
