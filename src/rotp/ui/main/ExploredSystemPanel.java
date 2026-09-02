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


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import rotp.model.galaxy.StarSystem;
import rotp.model.planet.PlanetType;
import rotp.ui.BasePanel;
import rotp.ui.map.IMapHandler;

final class ExploredSystemPanel extends SystemPanel {
    private static final long serialVersionUID = 1L;


	ExploredSystemPanel(SpriteDisplayPanel p)	{
		spritePanel(p);
		initModel();
	}
	void releaseObjects() { }

	@Override protected void showDefaultDetail(){ }
	@Override protected void showStarDetail()	{ }
	@Override protected void showPlanetDetail()	{ }
	@Override public IMapHandler mapHandler()	{ return parentSpritePanel().parent; }
	@Override public void animate()				{ overviewPane.animate(); }
	@Override protected BasePanel topPane()		{ return new SystemViewInfoPane(this); }
	@Override protected BasePanel bottomPane()	{ return new SystemRangePane(this); }
	@Override protected BasePanel detailPane()	{ return new ExploredDetailPane(this); }

	private final class ExploredDetailPane extends DetailBasePane {
		private static final long serialVersionUID = 1L;

		ExploredDetailPane(SystemPanel p)	{ super(p); }

		@Override public void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;

            super.paintComponent(g);
            int h = getHeight();
            int w = getWidth();

            int topH = s45;
            // draw "UNCOLONIZED" box
            g.setColor(MainUI.paneBackground());
            g.fillRect(0, 0, w, topH-s5);
            g.setColor(MainUI.shadeBorderC());
            g.fillRect(0, topH-s5, w, s6);

            textureClip = new Rectangle2D.Float(0,0,w,topH-s5);

            String label;
            if (sys.planet().isEnvironmentNone())
                label = text("MAIN_NO_PLANETS");
            else if (sys.abandoned())
                label = text("MAIN_ABANDONED");
            else
                label = text("MAIN_NO_COLONIES");
            scaledFont(g, label, w-s60, 24, 12);
            drawShadowedString(g, label, 2, s10, topH-s15, MainUI.shadeBorderC(), SystemPanel.whiteLabelText);

            // draw system banner
            int sz = s70;
            int shX = (options().selectedFlagColorCount() == 1)? 0 : s8; // BR: flagColorCount
            if (hoverBox == flagBox) { // BR: swapped hover and Flag
                Image hoverImage = mapHandler().flagHover(sys);
                g.drawImage(hoverImage, w-sz+s15-shX, topH-sz+s10, sz, sz, null);
            }
            Image flagImage = mapHandler().flagImage(sys);
            g.drawImage(flagImage, w-sz+s15-shX, topH-sz+s10, sz, sz, null);
            flagBox.setBounds(w-sz+s25-shX,topH-sz+s10,sz-s20,sz-s10);

            // draw planet terrain background
            PlanetType pt = sys.planet().type();

            if (pt != null) {
                BufferedImage img = pt.terrainImage();
                if (img != null)
                    g.drawImage(img, 0, topH, w, h, 0, 0, img.getWidth(), img.getHeight(), null);
                g.setFont(narrowFont(16));
                g.setColor(grayText);
                String desc = pt.description(player());
                List<String> descLines =  wrappedLines(g, text(desc), getWidth()-s12);

                int ydelta = s18;
                int y0=h-s8-(ydelta*(descLines.size()-1));
                for (String line: descLines) {
                    drawBorderedString(g, line, s8, y0, Color.black, whiteText);
                    y0 += ydelta;
                }
            }
        }
    }
}
