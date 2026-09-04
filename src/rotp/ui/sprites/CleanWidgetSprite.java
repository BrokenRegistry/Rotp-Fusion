/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 https://www.gnu.org/licenses/gpl-3.0.html
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
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import rotp.model.colony.Colony;
import rotp.ui.BasePanel;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.SystemPanel;

public final class CleanWidgetSprite extends MapControlSprite {
	private Image image;
	private int yOrigin, yShift;
	private boolean clean		= true;
	private int dirtyCount		= 0;
	private int lockedCount		= 0;
	private int unlockedCount	= 0;
	private int govLockCount	= 0;
	private int govUnlockCount	= 0;
	private int numLines		= 0;
	
	public CleanWidgetSprite(int xOff, int yOff, int w, int h, int shift) {
		super(xOff, yOff, w, h, "MAIN_HELP_MOD_1K");
		yShift  = scaled(yOff+shift);
		yOrigin = yOffset;
	}
	private Image image()	{
		if (image == null)
			image = scaledImageW("CLEAN_BUTTON", width-BasePanel.s4);
		return image;
	}
	public void checkForEcoClean ()	{
		int[] needCleaning = player().needCleaning();
		govLockCount	= needCleaning[Colony.GOV_LOCKED_DIRTY];
		govUnlockCount	= needCleaning[Colony.GOV_UNLOCKED_DIRTY];
		lockedCount		= needCleaning[Colony.LOCKED_DIRTY];
		unlockedCount	= needCleaning[Colony.UNLOCKED_DIRTY];
		dirtyCount		= govLockCount + govUnlockCount + lockedCount + unlockedCount;
		clean			= dirtyCount == 0;
	}
	public void updateLocation()	{
		if (session().aFewMoreTurns())
			yOffset = yOrigin;
		else
			yOffset = yShift;
		checkForEcoClean ();
	}
	public void setCleanValue(boolean b) { clean = b; }
	@Override
	public boolean acceptDoubleClicks()	 { return false; }
	@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
		player().checkEcoAtClean(e);
	}
	private String toLabel(int count, String key, boolean shiftDown, boolean ctrlDown) {
		if (count > 0) {
			numLines += 1;
			if (count == 1)
				if (ctrlDown)
					return text(key + "_C_1");
				else if (shiftDown)
					return text(key + "_S_1");
				else
					return text(key + "_1");
			else
				if (ctrlDown)
					return text(key + "_C", count);
				else if (shiftDown)
					return text(key + "_S", count);
				else
					return text(key, count);
		}
		return "";
	}
	@Override public void draw(GalaxyMapPanel map, Graphics2D g2) {
		int fontSize = 13;
		int lineH	 = BasePanel.s14;
		int border	 = BasePanel.s2;
		int displayW = width;
		int displayH = height;
		int labelW;
		String label;
		List<String> detailLines = new ArrayList<>();
		boolean shiftDown = isShiftDown();
		boolean ctrlDown  = isCtrlDown();
		numLines = 1;

		if (hovering) {
			checkForEcoClean ();
			g2.setFont(narrowFont(fontSize));
			int textW;
			if (clean) {
				label	= text("MOD_MAIN_COLONIES_ARE_CLEAN");
				labelW	= g2.getFontMetrics().stringWidth(label);
				textW	= labelW*3/5;
			}
			else {
				if (dirtyCount == 1)
					label = text("MOD_MAIN_COLONIES_ARE_DIRTY_ALL_1");
				else
					label = text("MOD_MAIN_COLONIES_ARE_DIRTY_ALL", dirtyCount);
				labelW = g2.getFontMetrics().stringWidth(label);
				textW  = labelW*3/5;
				label += toLabel(unlockedCount,	 "MOD_MAIN_COL_DIRTY_UNLOCK",	  shiftDown, ctrlDown);
				label += toLabel(govLockCount,	 "MOD_MAIN_COL_DIRTY_GOV_LOCK",	  shiftDown, ctrlDown);
				label += toLabel(lockedCount,	 "MOD_MAIN_COL_DIRTY_LOCKED",	  shiftDown, ctrlDown);
				label += toLabel(govUnlockCount, "MOD_MAIN_COL_DIRTY_GOV_UNLOCK", shiftDown, ctrlDown);
			}
			if (numLines == 1)
				textW = labelW * 3/5;
			else
				textW = labelW;
			numLines = max(2, numLines);
			displayH += lineH * (numLines-2);
			detailLines = wrappedLines(g2, label, textW);
			while (detailLines.size() > numLines) {
				textW += labelW/10;
				detailLines = wrappedLines(g2, label, textW);
			}
			displayW = width + BasePanel.s15 + textW;
		}
		drawBackground(map, g2, displayW, displayH);
		if (!clean)
			drawRedBackground(map, g2);

		g2.drawImage(image(), startX+border, startY+border, map);

		if (hovering) {
			g2.setColor(Color.lightGray);
			g2.setFont(narrowFont(fontSize));
			int y1 = startY + height - BasePanel.s17;
			int x1 = startX + width + BasePanel.s10;
			if (detailLines.size() == 1)
				y1 += BasePanel.s8;
			for (String line: detailLines) {
				drawString(g2, line, x1, y1);
				y1 += lineH;
			}
		}
		drawBorder(map, g2, displayW, displayH, map.parent().shadeC(), false);
	}
	public void drawRedBackground(GalaxyMapPanel map, Graphics2D g2) {
		int cnr = BasePanel.s12;
		startX = xOffset >= 0 ? xOffset : map.getWidth()+xOffset;
		startY = yOffset >= 0 ? yOffset : map.getHeight()+yOffset;
		box.setLocation(startX, startY);
		box.setSelectionBounds(startX-s3, startY-s3, width+s3+s3, height+s3+s3);
		g2.setColor(Color.RED);
		g2.fillRoundRect(startX, startY, width, height, cnr, cnr);
	}
	public void drawBackground(GalaxyMapPanel map, Graphics2D g2, int w, int h) {
		int cnr = BasePanel.s12;
		int brdr = BasePanel.s1;
		startX = xOffset >= 0 ? xOffset : map.getWidth()+xOffset;
		startY = yOffset >= 0 ? yOffset : map.getHeight()+yOffset;
		box.setLocation(startX, startY);
		box.setSelectionBounds(startX-s3, startY-s3, width+s3+s3, height+s3+s3);
		g2.setColor(map.parent().shadeC());
		g2.fillRoundRect(startX-brdr, startY-brdr, w+brdr+brdr, h+brdr+brdr, cnr, cnr);
	}
	public void drawBorder(GalaxyMapPanel map, Graphics2D g2, int w, int h, Color c, boolean show) {
		Stroke str0 = g2.getStroke();

		int cnr = BasePanel.s12;

		g2.setStroke(BasePanel.stroke1);
		g2.setColor(c);
		g2.drawRoundRect(startX, startY, width, height, cnr, cnr);

		if (hovering || show) {
			g2.setStroke(BasePanel.stroke2);
			g2.setColor(SystemPanel.yellowText);
			g2.drawRoundRect(startX, startY, w, h, cnr, cnr);
			g2.setStroke(str0);
		}
	}
}
