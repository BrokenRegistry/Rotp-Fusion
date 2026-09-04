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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

import rotp.model.empires.Empire;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.overlay.IMapOverlay;

public final class SystemFlagSprite extends MapSprite {
	private IMapOverlay parent;
	private int sysId;

	public void init(IMapOverlay p, Graphics2D g, int sysId)	{
		parent = p;
		this.sysId = sysId;
		box.setSize(s70, s70);
		box.setAdviceHelpKey("DETAIL_PANEL_FLAGS_HELP");
	}
	public void reset()	{}
	private Image flagImage()	{ return Empire.thePlayer().sv.flagImage(sysId); }
	private Image flagHaze()	{ return Empire.thePlayer().sv.view(sysId).flagBackGround("Flag_Haze"); }
	private Image flagHover()	{ return Empire.thePlayer().sv.view(sysId).flagBackGround("Flag_Hover"); }
	private void resetFlagColor()	{
		player().sv.resetFlagColor(sysId);
		parent.getMapHandler().repaint();
	}
	private void toggleFlagColor(boolean reverse)	{
		player().sv.toggleFlagColor(sysId, reverse);
		parent.getMapHandler().repaint();
	}
	@Override public boolean acceptDoubleClicks()	{ return true; }
	@Override public boolean acceptWheel()			{ return true; }
	@Override public void draw(GalaxyMapPanel map, Graphics2D g)	{
		if (!parent.drawSprites())
			return;
		Image flagImage = flagImage();
		Image flagHaze = flagHaze();
		g.drawImage(flagHaze, box.x, box.y, box.width, box.height, null);
		if (hovering) {
			Image flagHover = flagHover();
			g.drawImage(flagHover, box.x, box.y, box.width, box.height, null);
		}
		g.drawImage(flagImage, box.x, box.y, box.width, box.height, null);
	}
	@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
		// BR: if 3 buttons:
		//	- Middle click = Reset
		//	- Right click = Reverse
		if (middleClick)
			resetFlagColor();
		else if (rightClick)
			if (has3Buttons())
				toggleFlagColor(true);
			else
				resetFlagColor();
		else
			toggleFlagColor(false);
	}
	@Override public void wheel(GalaxyMapPanel map, int rotation, boolean click) {
		if (rotation < 0)
			toggleFlagColor(true);
		else
			toggleFlagColor(false);
	}
}
