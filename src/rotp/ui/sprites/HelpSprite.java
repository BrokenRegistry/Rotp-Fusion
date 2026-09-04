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
import java.awt.event.MouseEvent;

import rotp.ui.BasePanel;
import rotp.ui.game.AdvisorPanel;
import rotp.ui.main.GalaxyMapPanel;

public class HelpSprite extends MapSprite {
    private final BasePanel parent;

    public HelpSprite(BasePanel p)  { 
        parent = p; 
		setBounds(s10, s8, s20, s20);
		box.setAdviceHelpKey("MAIN_PANEL_HELP_ICON_HELP");
    }

	@Override public void draw(GalaxyMapPanel map, Graphics2D g) {
		int x1 = s16;
		int y1 = s26;
		g.setColor(new Color(100, 100, 255, 100));
		box.fillOval(g);
        g.setFont(narrowFont(20));
        if (hovering)
            g.setColor(Color.yellow);
        else
            g.setColor(Color.white);

        drawString(g,"?", x1, y1);
    }
    @Override
    public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
        if (click)
            softClick();
        hovering = true;
        if (rightClick)
        	parent.showHotKeys();
		else if (AdvisorPanel.helpShowAdvisor.get())
			parent.toggleOnDemandAdvisor(parent, AdvisorPanel.MAP_ADVISOR, player());
		else
			parent.showHelp();
    }
}
