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

import rotp.model.game.GameSession;
import rotp.model.game.IConvenienceOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.game.AdvisorPanel;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.main.SystemPanel;

public final class YearDisplaySprite extends MapSprite {
    private int minMapX, maxButtonW;
    private final MainUI parent;

    public YearDisplaySprite(MainUI p)  { parent = p; }

	@Override public boolean isSelectableAt(GalaxyMapPanel map, int x, int y) {
		if (GameSession.currentAlert() != null)
			return false;
		super.isSelectableAt(map, x, y);
		if (hovering)
			if (options().displayYear())
				box.setAdviceHelpKey("MAIN_YEAR_DISPLAY" + AdvisorPanel.HELP_KEY);
			else
				box.setAdviceHelpKey("MAIN_TURN_DISPLAY" + AdvisorPanel.HELP_KEY);
		return hovering;
	}
    @Override
    public void draw(GalaxyMapPanel map, Graphics2D g) {
        if (!parent.showYear())
            return;
        if (GameSession.currentAlert() != null)
            return;

        String s = displayYearOrTurn();
		if (IConvenienceOptions.showNextCouncil.get() 
				&& !options().selectedCouncilWinOption().equals(IGameOptions.COUNCIL_NONE)) {
        	int nextC = galaxy().council().nextCouncil();
        	 if (nextC > 0)
        		 s += " (" + nextC + ")";
        }
        g.setFont(narrowFont(24));

        int sw = g.getFontMetrics().stringWidth(s);
		box.setSize(sw+s5+s5, s20);
		box.setLocation(map.getWidth()-box.width-s25, map.getHeight()-box.height-s80);

		Color textC = hovering ? SystemPanel.yellowText : Color.gray;
		drawShadowedString(g, s, 2, box.x, box.ye(), Color.black, textC);
    }
    @Override
    public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
        if (GameSession.currentAlert() != null)
            return;
        if (click)
            softClick();
        minMapX = min(box.x, minMapX);
        maxButtonW = max(box.width, maxButtonW);
        hovering = true;

        options().toggleYearDisplay();
		if (options().displayYear())
			box.setAdviceHelpKey("MAIN_YEAR_DISPLAY" + AdvisorPanel.HELP_KEY);
		else
			box.setAdviceHelpKey("MAIN_TURN_DISPLAY" + AdvisorPanel.HELP_KEY);
    }
	@Override public void repaint(GalaxyMapPanel map)	{ map.repaint(minMapX, box.y, maxButtonW, box.height); }
}
