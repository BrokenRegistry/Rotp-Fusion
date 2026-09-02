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
package rotp.ui.main;

import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import rotp.model.empires.Empire;
import rotp.model.galaxy.StarSystem;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.util.AdviceBox;

class DetailBasePane extends BasePanel implements MouseMotionListener, MouseListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;
	protected SystemPanel parent;
	protected Shape textureClip;
	protected AdviceBox flagBox = new AdviceBox();
	protected AdviceBox nameBox = new AdviceBox();
	protected Shape hoverBox;
	protected int displayEmpId = Empire.NULL_ID;

	protected DetailBasePane(SystemPanel p) {
		parent = p;
		init();
	}
	private void init() {
		setOpaque(false);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		flagBox.init(this, null, null, "DETAIL_PANEL_FLAGS_HELP");
//		nameBox.init(this, null, null, "DETAIL_PANEL_FLAGS_HELP");
	}
	@Override public String textureName()	{ return TEXTURE_GRAY; }
	@Override public Shape textureClip()	{ return textureClip; }
	@Override public void mouseDragged(MouseEvent e)	{ }
	@Override public void mouseMoved(MouseEvent e)	{
		setModifierKeysState(e);
		int x = e.getX();
		int y = e.getY();
		hoverBox = null;
		if (flagBox.contains(x,y))
			hoverBox = hoverBox(flagBox, hoverBox);
		else if (nameBox.contains(x,y))
			hoverBox = hoverBox(nameBox, hoverBox);
		else
			hoverBox = hoverBox(null, hoverBox);
	}
	@Override public void mouseClicked(MouseEvent e)	{ }
	@Override public void mousePressed(MouseEvent e)	{ }
	@Override public void mouseReleased(MouseEvent e)	{
		setModifierKeysState(e);
		boolean rightClick  = SwingUtilities.isRightMouseButton(e);
		boolean middleClick = SwingUtilities.isMiddleMouseButton(e);
		if (hoverBox == flagBox) {
			StarSystem sys = parent.parentSpritePanel().systemViewToDisplay();
			// BR: if 3 buttons:
			//	- Middle click = Reset
			//	- Right click = Reverse
			if (middleClick)
				player().sv.resetFlagColor(sys.id);
			else if (rightClick)
				if (has3Buttons())
					player().sv.toggleFlagColor(sys.id, true);
				else
					player().sv.resetFlagColor(sys.id);
			else
				player().sv.toggleFlagColor(sys.id, false);
			parent.mapHandler().repaint();
		}
		else if (hoverBox == nameBox) {
			System.out.println("displayEmpId " + displayEmpId);
			RotPUI.instance().selectRacesPanel();
			RotPUI.instance().racesUI().selectDiplomacyTab();
			RotPUI.instance().racesUI().selectedEmpire(galaxy().empire(displayEmpId));
		}
	}
	@Override public void mouseEntered(MouseEvent e)	{ clearHoverSprite(e, parent.mapHandler()); }
	@Override public void mouseExited(MouseEvent e)		{ 
		if (hoverBox != null) {
			hoverBox = null;
			repaint();
		}
	}
	@Override public void mouseWheelMoved(MouseWheelEvent e) {
		setModifierKeysState(e);
		if (hoverBox == flagBox) {
			StarSystem sys = parent.parentSpritePanel().systemViewToDisplay();
			if (e.getWheelRotation() < 0)
				player().sv.toggleFlagColor(sys.id, true);
			else
				player().sv.toggleFlagColor(sys.id, false);
			parent.parentSpritePanel().repaint();
		}
	}
}