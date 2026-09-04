package rotp.ui.sprites;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import rotp.ui.BasePanel;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.SystemPanel;
import rotp.ui.main.overlay.IMapOverlay;

public class RoundButtonSprite extends MapSprite {
	private Paint background;
	private final Color edgeC = new Color(59,59,59);
	private final Color midC = new Color(93,93,93);
	protected IMapOverlay parent;
	protected int arc = s5;
	protected int baseLine = s10;

	public void reset()			{ background = null; }
	protected String label()	{ return text(box.getLabelKey()); }
	protected Font font()		{ return narrowFont(18); }
	protected Color[] colors()	{ return new Color[] {edgeC, midC, edgeC}; }

	protected void init(IMapOverlay p, Graphics2D g, int w, int h, String labelKey, int forcedLoc) {
		parent = p;
		box.setLabelKey(labelKey);
		if (g==null)
			box.setSize(w + new Canvas().getFontMetrics(font()).stringWidth(label()), h);
		else
			box.setSize(w + g.getFontMetrics(font()).stringWidth(label()), h);
		box.setForcedLocation(forcedLoc);
	}
	protected Paint background() {
		if (background == null) {
			float[] dist = {0.0f, 0.5f, 1.0f};
			Point2D start = new Point2D.Float(box.x, 0);
			Point2D end = new Point2D.Float(box.xe(), 0);
			background = new LinearGradientPaint(start, end, dist, colors());
		}
		return background;
	}
	@Override public void setBounds(int x, int y, int w, int h) {
		// if w changes due to language change, then recreate gradient background
		if (w != box.width)
			background = null;
		super.setBounds(x, y, w, h);
	}
	@Override public void draw(GalaxyMapPanel map, Graphics2D g) {
		if (!parent.drawSprites())
			return;
		directDraw(map, g);
	}
	protected void directDraw(GalaxyMapPanel map, Graphics2D g) {
		g.setColor(SystemPanel.blackText);
		box.fillShiftRoundRect(g, arc+s5, s3);
		g.setPaint(background());
		box.fillRoundRect(g, s5);
		Color c0 = hovering ? SystemPanel.yellowText : SystemPanel.whiteText;
		g.setColor(c0);
		Stroke prevStr = g.getStroke();
		g.setStroke(BasePanel.stroke2);
		box.drawRoundRect(g, arc);
		g.setStroke(prevStr);
		g.setFont(font());

		String str = label();
		int sw = g.getFontMetrics().stringWidth(str);
		int x2a = box.xText(sw);
		drawBorderedString(g, str, x2a, box.ye()-baseLine, SystemPanel.textShadowC, c0);
	}
}
