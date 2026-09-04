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
import java.awt.event.MouseEvent;

import rotp.model.game.GameSession;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.notifications.GameAlert;
import rotp.ui.notifications.SpyReportAlert;

public final class AlertDismissSprite extends MapSprite {
    private final MainUI parent;

	public AlertDismissSprite(MainUI p)  {
		parent = p;
		box.setAdviceHelpKey("MAIN_ALERT_NOTIF_SPRITE_HELP");
	}

    @Override
    public boolean isSelectableAt(GalaxyMapPanel map, int x, int y) {
        if (!parent.showAlerts())
            return false;
		hovering = box.contains(x, y);
        return hovering;
    }
    @Override
    public void draw(GalaxyMapPanel map, Graphics2D g) {
        if (!parent.showAlerts())
            return;

        int w1 = s10;

        Stroke prev = g.getStroke();
        if (hovering)
            g.setStroke(BasePanel.stroke3);
        else
            g.setStroke(BasePanel.stroke2);

		int x1 = parent.getWidth() - s24;
        int y1 = parent.getHeight() - scaled(162);

        int x2 = x1+w1;
        int y2 = y1+w1;

        g.setColor(Color.black);
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, y1, x1, y2);

        g.setStroke(prev);
    }
    @Override
    public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
        if (!parent.showAlerts())
            return;
        if (click)
            softClick();
        if (rightClick) { // BR: Move to system
			GameAlert alert = GameSession.currentAlert();
			if (alert instanceof SpyReportAlert) {
				RotPUI.instance().showSpyReport();
				box.hovering(false);
			}
			else {
				map.recenterMapOn(alert.system());
				map.repaint();
			}
			return;
        }
        hovering = true;

        GameSession.dismissAlert();
        map.repaint();
    }
}