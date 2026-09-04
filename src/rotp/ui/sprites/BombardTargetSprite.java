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
import java.awt.LinearGradientPaint;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import rotp.ui.BasePanel;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.SystemPanel;
import rotp.ui.main.overlay.MapOverlayBombardPrompt;

public final class BombardTargetSprite extends MapSprite {
    private LinearGradientPaint background;
    private final Color edgeC = new Color(44,59,30);
    private final Color midC = new Color(70,93,48);
    private MapOverlayBombardPrompt parent;

    public void reset()       { background = null; }

	public void parent(MapOverlayBombardPrompt p)  {
		parent = p;
		box.setAdviceHelpKey("MAIN_BOMBARD_TARGET_HELP");
		box.setForcedLocation(8);
	}

    @Override
    public void draw(GalaxyMapPanel map, Graphics2D g) {
        if (!parent.drawSprites())
            return;
        if (background == null) {
            float[] dist = {0.0f, 0.5f, 1.0f};
			Point2D yesStart = new Point2D.Float(box.x, 0);
			Point2D yesEnd = new Point2D.Float(box.xe(), 0);
            Color[] yesColors = {edgeC, midC, edgeC };
            background = new LinearGradientPaint(yesStart, yesEnd, dist, yesColors);
        }
        g.setFont(narrowFont(20));
        String str = text("MAIN_BOMBARD_TARGET", options().selectedBombingTarget());
        int sw = g.getFontMetrics().stringWidth(str);
        g.setPaint(background);
		box.fillRoundRect(g, s5);
        Color c0 = hovering ? SystemPanel.yellowText : Color.white;
        g.setColor(c0);
        Stroke prevStr = g.getStroke();
        g.setStroke(BasePanel.stroke2);
		box.drawRoundRect(g, s5);
        g.setStroke(prevStr);
		int x2a = box.xText(sw);
		drawBorderedString(g, str, x2a, box.ye()-s10, SystemPanel.textShadowC, c0);
    }
    @Override
    public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
        if (click)
            softClick();
		box.hovering(false);
        parent.bombardTarget();
    }
}
