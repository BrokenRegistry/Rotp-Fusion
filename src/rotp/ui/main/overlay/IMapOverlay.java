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
package rotp.ui.main.overlay;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import rotp.model.Sprite;
import rotp.model.galaxy.StarSystem;
import rotp.ui.RotPUI;
import rotp.ui.ScaledInteger;
import rotp.ui.game.AdvisorPanel;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.util.Base;

public interface IMapOverlay extends Base, ScaledInteger {
	boolean hoveringOverSprite(Sprite o);
	void advanceMap();
	void paintOverMap(MainUI parent, GalaxyMapPanel ui, Graphics2D g2);

	boolean handleKeyPress(KeyEvent e); // return true if an action has been taken
	default boolean baseHandleKeyPress(KeyEvent e)	{ // return true if an action has been taken
		switch(e.getKeyCode()) {
			case KeyEvent.VK_F1:
				if (e.isShiftDown())
					getMapHandler().showHotKeys();
				else if (AdvisorPanel.helpShowAdvisor.get()) {
					if (e.isControlDown())
						getMapHandler().showHelp();
					else
						getMapHandler().toggleOnDemandAdvisor(getMapHandler(), AdvisorPanel.MAP_ADVISOR, player());
				}
				else if (e.isControlDown())
					getMapHandler().toggleOnDemandAdvisor(getMapHandler(), AdvisorPanel.MAP_ADVISOR, player());
				else
					getMapHandler().showHelp();
				return true;
			case KeyEvent.VK_HOME:
				Sprite sp = getMapHandler().displayPanel().spriteToDisplay();
				if (sp != null && sp instanceof StarSystem) {
					StarSystem sys = (StarSystem) sp;
					player().sv.forceAutoFlagColor(sys.id, e.isAltDown());
				}
				break;
		}
		return false;
	}
	default boolean handleKeyReleased(KeyEvent e)	{ return false; }
	default boolean consumesClicks(Sprite spr)		{ return true; }
	default boolean masksMouseOver(int x, int y)	{ return false; }
	default boolean canChangeMapScale()				{ return false; }
	default boolean hideNextTurnNotice()			{ return true; }
	default boolean drawSprites()					{ return true; }
	default MainUI getMapHandler()					{ return RotPUI.instance().mainUI(); }
}
