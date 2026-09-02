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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.List;

import rotp.model.empires.SystemInfo;
import rotp.model.empires.SystemView;
import rotp.model.galaxy.StarSystem;
import rotp.ui.BasePanel;
import rotp.ui.map.IMapHandler;

final class UnexploredSystemPanel extends SystemPanel {
    private static final long serialVersionUID = 1L;

	UnexploredSystemPanel(SpriteDisplayPanel p) {
		spritePanel(p);
		initModel();
	}
	void releaseObjects() { }

	@Override public IMapHandler mapHandler()	{ return parentSpritePanel().parent; }
	@Override protected BasePanel topPane()		{ return null; }
	@Override protected BasePanel bottomPane()	{ return new SystemRangePane(this); }
	@Override protected BasePanel detailPane()	{ return new UnexploredDetailPane(this); }

	private final class UnexploredDetailPane extends DetailBasePane {
		private static final long serialVersionUID = 1L;

		private UnexploredDetailPane(SystemPanel p) {
			super(p);
			parent = p;
			init();
		}
		private void init() {
			setOpaque(true);
			setBackground(Color.black);
		}
		@Override public String textureName()			{ return null; }
		@Override public Color starBackgroundC()		{ return SystemPanel.starBackgroundC; }
		@Override public boolean hasStarBackground()	{ return true; }
		@Override public void paintComponent(Graphics g0) {
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;

            Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();

            if (sys.inNebula()) {
                g.setColor(SystemPanel.nebulaC);
                g.fillRect(0,0,w,h);
            }

            drawStar(g, sys.starType(), s40, getWidth()/2, getHeight()/2);

            int sz = s60;
            int shX = (options().selectedFlagColorCount() == 1)? 0 : s4; // BR: flagColorCount
            String label = text("MAIN_UNEXPLORED_SYSTEM");
            scaledFont(g, label, w-sz, 36, 24);
            drawBorderedString(g, label, 2, s10, s40, Color.black, SystemPanel.orangeText);

            // draw system banner
            SystemInfo sv = player().sv;
            if (hoverBox == flagBox) {
                Image hoverImage = sv.flagHover(sys.id);
                g.drawImage(hoverImage, w-sz+s5-shX, 0, sz, sz, null);
            }
            else if (sv.flagColorId(sys.id) == SystemView.FLAG_NONE){
                Image hoverImage = sv.flagHover(sys.id);
                g.drawImage(hoverImage, w-sz+s5-shX, 0, sz, sz, null);
                Composite prevC = g.getComposite();
                Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
                g.setComposite(comp);
                g.setColor(Color.black);
                g.fillRect(w-sz+s5-shX, 0, sz, sz-s10);
                g.setComposite(prevC);
            }

            Image flagImage = sv.mapFlagImage(sys.id);
            g.drawImage(flagImage, w-sz+s5-shX, 0, sz, sz, null);
            flagBox.setBounds(w-sz+s5-shX,0,sz-s20,sz-s10);         

            if (sys.inNebula()) {
                g.setFont(narrowFont(16));
                g.setColor(grayText);
                List<String> nebLines =  wrappedLines(g, text("MAIN_NEBULA_DESC"), getWidth()-s12);
                int ydelta = s18;
                int y0=s70;
                for (String line: nebLines) {
                    drawString(g,line, s8, y0);
                    y0 += ydelta;
                }
            } 

            g.setFont(narrowFont(16));
            g.setColor(grayText);
            List<String> descLines =  wrappedLines(g, text(sys.starType().description()), getWidth()-s12);

            int ydelta = s18;
            int y0=h-s8-(ydelta*(descLines.size()-1));
            for (String line: descLines) {
                drawString(g,line, s8, y0);
                y0 += ydelta;
            }

			// Guarded by a monster ?
			SystemView sysView = sv.view(sys.id);
			boolean isGuarded = sysView.isGuarded();
			if (isGuarded) {
				int cx = getWidth()/2;
				int cy = getHeight()/2;
				label = text("SYSTEMS_UNSCOUTED_GUARDED");
				scaledFont(g, label, w-s10, 24, 12);
				drawBorderedString(g, label, 2, s10, cy+s60, Color.black, redText);
				Image monsterImage = sysView.monsterImage();
				if (monsterImage != null) {
					sz = s50;
					g.drawImage(monsterImage, cx-s60, cy-s60, sz, sz, null);
				}
			}
		}
	}
}
