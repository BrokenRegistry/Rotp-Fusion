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
package rotp.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import rotp.Rotp;
import rotp.util.OSUtil;

public final class ErrorUI extends BasePanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static final String BASE_TITLE	= "An Error has occurred  :(  - ";
	private static final String DESC_1		="If you would like to help fix this problem, please send a screen shot of this UI plus the ";
	private static final String DESC_2		=" save game file to BrokenRegistry, or bring it to his attention on the ROTP subreddit.";
	private static final int LOADING_MODE	= 0;
	private static final int SETUP_MODE		= 1;
	private static final int PLAYER_MODE	= 2;
	private static final int TURN_MODE		= 3;
	public static int currentMode = LOADING_MODE;
	public static int errorMode = LOADING_MODE;
	public static void inLoadingMode()	{ currentMode = LOADING_MODE; }
	public static void inSetupMode()	{ currentMode = SETUP_MODE; }
	public static void inPlayerMode()	{ currentMode = PLAYER_MODE; }
	public static void inTurnMode()		{ currentMode = TURN_MODE; }

	private final String[] mode	= new String[4];
	private final String[] file	= new String[4];
	private final String osTxt = "OS = " + OSUtil.getOS();
	private Throwable exception;
	public ErrorUI()	{ init(); }
	private void init()	{
		setBackground(Color.black);
		addMouseListener(this);
		addMouseMotionListener(this);
		mode[LOADING_MODE]	= "When Loading";
		mode[SETUP_MODE]	= "In Setup";
		mode[PLAYER_MODE]	= "During Player Turn";
		mode[TURN_MODE]		= "During AI Turn";
		file[LOADING_MODE]	= "'recent.rotp'";
		file[SETUP_MODE]	= "'recent.rotp'";
		file[PLAYER_MODE]	= "'recent.rotp'";
		file[TURN_MODE]		= "!!! To Replay Last Turn !!!.rotp";
	}
    public void init(Throwable e) {
        exception = e;
        e.printStackTrace();
		errorMode = currentMode;
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int w = getWidth();
        int h = getHeight();

        g.setColor(Color.lightGray);

		int x0 = w/16;
		int w0 = w*7/8;
		int y0 = s50;
		g.setFont(narrowFont(40));
		String title = BASE_TITLE + mode[errorMode];
		drawString(g, title, x0, y0);

		g.setFont(narrowFont(30));
		int sw = g.getFontMetrics().stringWidth(osTxt);
		drawString(g, osTxt, (w-x0-sw), y0);

		y0 = s80;
		g.setFont(narrowFont(30));
		String desc = DESC_1 + file[errorMode] + DESC_2;
		y0 = drawString(g, desc, x0, y0, w0, s35);

		g.setFont(narrowFont(24));
		y0 += s50;
		String str = "Email: Broken.Registry@protonmail.com";
		//String str = "Email: ail.st@gmx.de";
		sw = g.getFontMetrics().stringWidth(str);
		drawString(g, str, x0, y0);
		//y0 += BasePanel.s30;
		str = "Reddit: www.Reddit.com/r/rotp";
		drawString(g, str, x0+sw+BasePanel.s50, y0);

		g.setFont(narrowFont(24));
		y0 = drawString(g, exception.toString(), x0, y0+s23, w0, s27);
		int maxY = h-s60;
		for (StackTraceElement line : exception.getStackTrace()) {
			y0 = drawString(g, line.toString(), x0, y0, w0, s27);
			if (y0>maxY)
				break;
		}

        g.setFont(narrowFont(20));
        String ver = "Version:"+ Rotp.releaseId;
        sw = g.getFontMetrics().stringWidth(ver);
        drawString(g, ver, w-sw-s20, h-s30);

        drawMemory(g);
        drawSkipText(g, true);
    }
	int drawString(Graphics g, String str, int x0, int y0, int w0, int step) {
		List<String> lines = wrappedLines(g, str, w0);
		int lineCount = 0;
		for (String line : lines) {
			y0 += step;
			if (lineCount < 10)
				drawString(g,line, x0, y0);
			lineCount++;
		}
		return y0;
	}
    @Override
    public boolean displayMemory() {
        return true;
    }
    private void advanceMode() {
        RotPUI.instance().selectGamePanel();
    }
    @Override
    public void mouseDragged(MouseEvent e) { }
    @Override
    public void mouseMoved(MouseEvent e) { }
    @Override
    public void mouseClicked(MouseEvent arg0) { }
    @Override
    public void mouseEntered(MouseEvent arg0) { }
    @Override
    public void mouseExited(MouseEvent arg0) { }
    @Override
    public void mousePressed(MouseEvent arg0) {}
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() > 3)
            return;
        advanceMode();
    }
    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                advanceMode();
                return;
            case KeyEvent.VK_L:
            	if (e.isAltDown()) {
            		debugReloadLabels(this);
            		break;
            	}
            	misClick();
            	break;
        }
    }
}