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
package rotp.ui.game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import javax.swing.JEditorPane;

import rotp.Rotp;
import rotp.model.IAdvice;
import rotp.model.Sprite;
import rotp.model.empires.species.Species;
import rotp.ui.BasePanel;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.MainUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.main.overlay.IMapOverlay;
import rotp.ui.map.IMapHandler;
import rotp.ui.sprites.MapControlSprite;
import rotp.ui.sprites.RoundButtonSprite;
import rotp.util.AdviceBox;

public final class AdvisorPanel extends BasePanel implements IAdvisor, IMapOverlay {
	private static final long serialVersionUID = 1L;

	AdvisorPanel()	{
		setOpaque(false);
		EXIT_BOX.init(this, null);
		initguideBox();
	}
	public static boolean isAdvising()				{ return A.isAdvising; }
	private static void isAdvising(boolean is)		{ A.isAdvising = is; }

	public void init(IMapHandler handler, BasePanel p, String key, Species sp)	{
		A.mapHandler = handler;
		init(p, key, sp);
	}
	public void init(BasePanel p, String key, Species sp)	{
		A.parent		= p;
		A.isOnHold		= false;
		A.advisorKey	= key;
		A.species		= sp;
		A.avatarImg	= getAdvisorImage(sp, key);
	}
	public void setMargins(int left, int floor, int right, int tlx, int tly)	{
		setMargins(left, floor, right, tlx, tly, s25, s25);
	}
	public void setMargins(int left, int floor, int right, int tlx, int tly, int lineX, int lineY)	{
		A.leftMargin	= left;
		A.rightMargin	= right;
		A.floorMargin	= floor;
		A.boxBottom	= ADVISOR.scaled(Rotp.IMG_H) - A.floorMargin;
		A.topLeftBoxLocation.x = tlx;
		A.topLeftBoxLocation.y = tly;
		A.lineLengthX	= lineX;
		A.lineLengthY	= lineY;

		// initAvatar
		setTopLeftAdvice(null);
		setAvatarSize(A.defaultAvatarWidth, A.defaultAvatarHeight);
	}
	public void setAvatarSize(int w, int h)	{
		A.nominalAvatarHeight = h;
		A.nominalAvatarWidth  = w;
		updateAvatarSize(advisorIconSize.get());
	}
	public void switchOn()	{ }
	public void onHold()	{	// leave a panel
		A.isOnHold = true;
		A.target = null;
		GUIDE_BOX.setText(null);
//		releaseObjects();
	}
	public boolean toggle()	{
		isAdvising(!A.isAdvising);
		if (A.isAdvising)
			switchOn();
		else
			onHold();
		return A.isAdvising;
	}
	public static AdviceBox getButtonBox()	{ return EXIT_BOX.getBox(); }
	public static void releaseObjects()		{
		A.target = null;
		A.species = null;
		GUIDE_BOX.setText(null);
		A.parent.repaint();
	}
	public void setTopLeftAdvice(String key)	{ EXIT_BOX.getBox().setTopLeftHelpKey(key); }
	public RoundButtonSprite getExitBox()	{ return EXIT_BOX; }
	public void leavedElement(IAdvice prevTarget)	{
		if (!A.isAdvising || prevTarget != A.target)
			return;
		A.target = null;
		A.parent.repaint();
	}
	public boolean hoveringOverElement(IAdvice newTarget)	{
		if (!A.isAdvising || newTarget == A.target)
			return false;

		A.target = newTarget;

		if (newTarget != null) {
			AdviceBox newTargetBox = newTarget.getBox();
			if (newTargetBox != null) {
				newTargetBox.convertLocation(ADVISOR);
				if (newTargetBox.getAdvisorImageKey() != null) {
					String key = newTargetBox.getAdvisorImageKey();
					if (key == null)
						A.avatarImg = ADVISOR.newBufferedImage(getAdvisorImage(A.species, A.advisorKey));
					else
						A.avatarImg = ADVISOR.newBufferedImage(getAdvisorImage(A.species, key));
				}
			}
		}
		A.parent.repaint();
		return false;
	}
	@Override public void advanceMap()	{
		onHold();
		isAdvising(false);
	}
	@Override public boolean hideNextTurnNotice()			{ return false; }
	@Override public boolean canChangeMapScale()			{ return true; }
	@Override public boolean consumesClicks(Sprite spr)		{ return spr == EXIT_BOX; }
	@Override public boolean masksMouseOver(int x, int y)	{ return ADVISOR_BOX.contains(x,y); }
	@Override public boolean hoveringOverSprite(Sprite o)	{ return ADVISOR.hoveringOverElement(o); }
	@Override public void paintOverMap(MainUI mainUI, GalaxyMapPanel mapPanel, Graphics2D g)	{
		A.mapHandler = mainUI;
		paintOverMap(g); }
	@Override public void paintComponent(Graphics g)	{
		if (A.isOnHold)
			return;
		paintOverMap((Graphics2D) g);
	}
	@Override public boolean handleKeyPress(KeyEvent e)	{
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				advanceMap();
				return true;
			case KeyEvent.VK_L:
				if (e.isAltDown()) {
					debugReloadLabels(A.parent);
					return true;
				}
				return false;
			default:
				return false;
		}
	}
	public void paintOverMap(Graphics2D g)	{
		if (A.avatarImg == null || !A.isAdvising || (ADVISOR.isAltDown() && ADVISOR.isCtrlDown()))
			return;
		A.dx = 0;
		A.dy = 0;
		drawAvatar(g);
		drawButton(g);
		if (A.target != null && !A.target.hovering()) {
			A.target = null;
			A.parent.repaint();
			return;
		}

		// for the case target is set to null while drawing!
		final IAdvice drawnTarget = A.target;

		// getBox() may have to build a new box, So it will be called only once.
		final AdviceBox box = drawnTarget == null? null : drawnTarget.getBox();

		drawAdvice(g, drawnTarget, box);
		if (drawnTarget == null)
			return;

		// could be hidden by the avatar
		Sprite toDraw = drawnTarget.getBox().getSpriteToDraw();
		if (toDraw != null && toDraw instanceof MapControlSprite)
			toDraw.draw(map(), g);
	}
	private void drawAvatar(Graphics2D g)	{
		float[] dist	= {0.0f, 1.0f};
		Color[] colors	= {CENTER_COLOR, BACK_DARK_COLOR};
		Point2D center	= AVATAR_BOX.getCenter();
		float radius	= (float)AVATAR_BOX.getRadius();
		g.setPaint(new RadialGradientPaint(center, radius, dist, colors));
		g.fill(AVATAR_BOX);

		int imgW = A.avatarImg.getWidth();
		int imgH = A.avatarImg.getHeight();
		int maxH = AVATAR_BOX.height * 12/10;
		int maxW = AVATAR_BOX.width * 12/10;
		int dispW, dispH;
		if (imgW*maxH < imgH*maxW){
			dispH = maxH;
			dispW = imgW * maxH / imgH;
		}
		else {
			dispW = maxW;
			dispH = imgH * maxW / imgW;
		}

		int x1 = AVATAR_BOX.x;
		int y1 = AVATAR_BOX.ye()-dispH;
		g.drawImage(A.avatarImg, x1, y1, x1+dispW, y1+dispH, 0, 0, imgW, imgH, null);

		if (EXIT_BOX.hovering()) {
			Stroke prevStr = g.getStroke();
			g.setStroke(BasePanel.stroke2);
			g.setColor(Color.yellow);
			g.draw(EXIT_BOX.getBox().getSelectionBox());
			g.setStroke(prevStr);
		}
	}
	private void drawButton(Graphics2D g)	{
		if (A.mapHandler != null)
			A.mapHandler.nextTurnSprites().add(EXIT_BOX);
		ADVISOR.setRenderingHints(g);
		EXIT_BOX.draw(map(), g); //	exitBox.draw(null, g); could works too
	}
	private void drawAdvice(Graphics2D g, IAdvice target, AdviceBox targetBox)	{
		if (target == null || targetBox == EXIT_BOX.getBox()) {
			drawWelcome(g, target, targetBox);
			if (target == EXIT_BOX || target == EXIT_BOX.getBox())
				drawTopLeftAdvice(g, EXIT_BOX, EXIT_BOX.getBox());
			return;
		}

		targetBox = target.getBox();
		if (targetBox == null) {
			drawWelcome(g, target, targetBox);
			return;
		}

		// if there is separate box, draw it
		drawTopLeftAdvice(g, target, targetBox);

		String str = helpText(target, targetBox);
		if (str == null) {
			Supplier<BufferedImage> fct = targetBox.getHelpImg();
			if (fct != null) {
				BufferedImage img = fct.get();
				TEXT_BOX.width = img.getWidth() + TEXT_MARGIN + TEXT_MARGIN;
				TEXT_BOX.height = img.getHeight() + TEXT_MARGIN;
				setBoxLocation(targetBox);
				drawGuideBox(g, TEXT_BOX.x, TEXT_BOX.y, BORDER_WIDTH, img);
				drawLines(g);
				return;

			}
			else
			str = "null";
		}

		initGuideBox(g, str, s400, 0);

		setBoxLocation(targetBox);

		drawGuideBox(g, TEXT_BOX.x, TEXT_BOX.y, BORDER_WIDTH, null);

		drawLines(g);
	}
	private void drawWelcome(Graphics2D g, IAdvice target, AdviceBox targetBox)	{
		String str = helpText(target, targetBox);
		initGuideBox(g, str, s400, 0);
		drawGuideBox(g, AVATAR_BOX.xe() + s2, AVATAR_BOX.ye() - TEXT_BOX.height, 0, null);
	}
	private void drawTopLeftAdvice(Graphics2D g, IAdvice target, AdviceBox targetBox)	{
		String str = topLeftText(target, targetBox);
		if (str == null || str.isBlank())
			return;

		initGuideBox(g, str, s400, ADVISOR.getHeight()-A.topLeftBoxLocation.y);
		drawGuideBox(g, A.topLeftBoxLocation.x, A.topLeftBoxLocation.y, 0, null);
	}
	private String avatarTopLeftText()	{
		String key = EXIT_BOX.getBox().getTopLefteHelpKey();
		if (key == null)
			return null;
		return ADVISOR.text(key);
	}
	private String topLeftText(IAdvice target, AdviceBox targetBox)	{
		if (target == null || targetBox == null)
			return avatarTopLeftText();

		String key = targetBox.getTopLefteHelpKey();
		if (key == null)
			return null;
		return ADVISOR.text(key);
	}

	private void initguideBox()	{
		GUIDE_BOX.getEditorKit().createDefaultDocument();
		GUIDE_BOX.setText("");
		GUIDE_BOX.setOpaque(false);
		GUIDE_BOX.setContentType("text/html");
		GUIDE_BOX.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		GUIDE_BOX.setBackground(new Color(0, 0, 0, 0));
		GUIDE_BOX.setForeground(SystemPanel.whiteText);
	}
}
