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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

import rotp.Rotp;
import rotp.model.Sprite;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.sprites.RoundButtonSprite;
import rotp.util.OSUtil;

public final class MapOverlayMemoryLow implements IMapOverlay {
    final Color edgeC = new Color(44,59,30);
    final Color midC = new Color(70,93,48);
    final String osTxt = " OS = " + OSUtil.getOS(); 
    MainUI parent;
    RestartButtonSprite restartButton = new RestartButtonSprite();
    SkipButtonSprite skipButton = new SkipButtonSprite();
    public MapOverlayMemoryLow(MainUI p) {
        parent = p;
    }
    public void init() {
        restartButton.reset();
        skipButton.reset();
    }
    public void releaseObjects() { }

    public void restart() {
        Rotp.restartFromLowMemory();
    }
    public void skip() {
        parent.clearOverlay();
    }
    @Override
    public boolean masksMouseOver(int x, int y)   { return true; }
    @Override
    public boolean hoveringOverSprite(Sprite o) { return false; }
    @Override
    public void advanceMap() { }
    @Override
    public void paintOverMap(MainUI parent, GalaxyMapPanel ui, Graphics2D g) {
		int x0 = s100;
		int y0 = s30;
        int w0 = scaled(850);
        int h0 = scaled(585);
        g.setColor(MainUI.paneShadeC2);
        g.fillRect(x0, y0, w0, h0);

        int x1 = x0 + s7;
        int y1 = y0 + s7;
        int w1 = w0 - s7 - s7;
		int h1 = s65;
        g.setColor(MainUI.paneBackground);
        g.fillRect(x1, y1, w1, h1);

        int x2 = x1;
        int y2 = y1+h1+s3;
        int w2 = w1;
        int h2 = scaled(462);
        g.setColor(MainUI.paneBackground);
        g.fillRect(x2, y2, w2, h2);

        // draw title
        String titleStr = text("MAIN_MEMORY_LOW_TITLE");
        g.setFont(narrowFont(22));
        int sw1 = g.getFontMetrics().stringWidth(titleStr);
        int x1a = x1+(w1-sw1)/2;
        drawShadowedString(g, titleStr, 3, x1a, y1+h1-s35, SystemPanel.textShadowC, SystemPanel.whiteText);

		int lineH = s18;
        int x2a = x2+s10;
        int y2a = y2+s20;

        int textW = w2+x2-x2a-s10;
        String desc1 = text("MAIN_MEMORY_LOW_DESC");
        g.setFont(narrowFont(16));
        List<String> lines = wrappedLines(g, desc1, textW);
        for (String line: lines) {
            drawString(g,line, x2a, y2a);
            y2a += lineH;
        }

        y2a += s10;
        String desc2 = text("MAIN_MEMORY_LOW_DESC_2");
        g.setFont(narrowFont(16));
        lines = wrappedLines(g, desc2, textW);
        for (String line: lines) {
            drawString(g,line, x2a, y2a);
            y2a += lineH;
        }

        y2a += s10;
        String desc3 = text("MAIN_MEMORY_LOW_DESC_3");
        g.setFont(narrowFont(16));
        lines = wrappedLines(g, desc3, textW);
        for (String line: lines) {
            drawString(g,line, x2a, y2a);
            y2a += lineH;
        }

        y2a += s10;
        String desc4 = memHeap();
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, s15));
        drawString(g, desc4, x2a, y2a);
        y2a += lineH;

        desc4 = memNonHeap();
        drawString(g, desc4, x2a, y2a);
        y2a += lineH;

        y2a += s10;
        drawString(g,"Memory Pool: ", x2a, y2a);
        y2a += lineH;
        List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean: beans) {
            desc4 = memoryPoolToString(bean);
            drawString(g, desc4, x2a, y2a);
            y2a += lineH;
        }

        y2a += s10;
        drawString(g,"Garbage Collector: ", x2a, y2a);
        y2a += lineH;
        for (GarbageCollectorMXBean bean: ManagementFactory.getGarbageCollectorMXBeans()) {
            desc4 = memoryGCToString(bean);
            drawString(g, desc4, x2a, y2a);
            y2a += lineH;
            lines = wrappedLines(g, desc4, textW);
        }
        y2a += s10;
        drawString(g,"Last " + Rotp.getMemoryInfo(true) + osTxt, x2a, y2a);

        // init and draw continue button sprite
        parent.addNextTurnControl(restartButton);
        restartButton.init(this,g);
        restartButton.mapX(x0+s10);
        restartButton.mapY(y0+h0-restartButton.height()-s10);
        restartButton.draw(parent.map(), g);

        parent.addNextTurnControl(skipButton);
        skipButton.init(this,g);
        skipButton.mapX(x0+s25+restartButton.width());
        skipButton.mapY(restartButton.mapY());
        skipButton.draw(parent.map(), g);
    }
    @Override
    public boolean handleKeyPress(KeyEvent e) {
		if (baseHandleKeyPress(e))
			return true;
        switch(e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                //softClick();
                skip();
                break;
            case KeyEvent.VK_L:
            	if (e.isAltDown()) {
            		debugReloadLabels(parent);
            		break;
            	}
                misClick();
                break;
            default:
                misClick();
                break;
        }
        return true;
    }
}
class RestartButtonSprite extends RoundButtonSprite {
	private MapOverlayMemoryLow parent;

	public void init(MapOverlayMemoryLow p, Graphics2D g)  {
		parent = p;
		init(p, g, s20, s30, "MAIN_MEMORY_LOW_RESTART", 2);
	}
	@Override protected Color[] colors()	{ return new Color[] {parent.edgeC, parent.midC, parent.edgeC}; }
	@Override public void draw(GalaxyMapPanel map, Graphics2D g)	{ directDraw(map, g); }
	@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
		parent.restart();
	};
}
class SkipButtonSprite extends RoundButtonSprite {
	private MapOverlayMemoryLow parent;

	public void init(MapOverlayMemoryLow p, Graphics2D g)  {
		parent = p;
		init(p, g, s20, s30, "MAIN_MEMORY_LOW_SKIP", 2);
	}
	@Override protected Color[] colors()	{ return new Color[] {parent.edgeC, parent.midC, parent.edgeC}; }
	@Override public void draw(GalaxyMapPanel map, Graphics2D g) { directDraw(map, g); }
	@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
		parent.skip();
	};
}
