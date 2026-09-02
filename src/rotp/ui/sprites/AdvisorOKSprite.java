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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.overlay.IMapOverlay;

public class AdvisorOKSprite extends RoundButtonSprite {
	private final Color edgeC = new Color(59,59,59);
	private final Color midC  = new Color(93,93,93);
	private IMapOverlay parent;
	private boolean draw;

	public AdvisorOKSprite()				{ box.setLabelKey("MAIN_ADVISOR_BUTTON_OK"); }
	public void draw(boolean b)				{ draw = b; }
	public void parent(IMapOverlay p)		{ parent = p; }

	@Override protected Color[] colors()	{ return new Color[] {edgeC, midC, edgeC}; }
	@Override protected Font font()			{ return narrowFont(20); }
	@Override public void draw(GalaxyMapPanel map, Graphics2D g)	{ draw(g); }
	public void draw(Graphics2D g)	{
		if (draw)
			directDraw(null, g);
	}
	@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
		if (click)
			softClick();
		parent.advanceMap();
	}
}
