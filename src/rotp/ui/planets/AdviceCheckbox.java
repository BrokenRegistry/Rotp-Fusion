package rotp.ui.planets;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;

import rotp.ui.main.SystemPanel;
import rotp.ui.util.ParamBoolean;
import rotp.util.AdviceBox;
import rotp.util.Base;

class AdviceCheckbox extends AdviceBox implements Base	{
	private static final long serialVersionUID = 1L;
	private static final Color CHECKBOX_COLOR = new Color(178, 124, 87);

	private int textRightX;

	@Override public void setSize(int w, int h)		{ setSelectionSize(w, h); }
	@Override public void setLocation(int x, int y)	{ // Top left selection box
		setSelectionLocation(x, y);
		this.x = x;
		this.y = y + (getSelectionBox().height-height)/2;
	}

	public void init(int boxSide, int width, int height, ParamBoolean param)	{
		this.height	= boxSide;
		this.width	= boxSide;
		setParam(param);
		setSelectionSize(width, height);
	}
	public int getTextRightX()				{ return textRightX; }
	public void setTextRightX(int i)		{ textRightX = i; }
	public void setCheckboxSide(int side)	{ width = side; height = side; }
	public String setOptimalSize(Graphics2D g, Font font) {
		final String description = getParam().govLabelTxt();
		final int sw = g.getFontMetrics().stringWidth(description);
		return description;
	}

	public void drawCheckbox(Graphics2D g)	{ drawCheckbox(g, 18, CHECKBOX_COLOR); }
	public void drawCheckbox(Graphics2D g, int fontSize, Color checkboxC)	{
		drawCheckbox(g, plainFont(fontSize), CHECKBOX_COLOR, width * 2/3);
	}
	public void drawCheckbox(Graphics2D g, Font font, Color checkboxC, int sep)	{
		final int xe = xe();
		final int ye = ye();
		final int descrX = x + width + sep;

		g.setColor(Color.black);
		g.setFont(font);
		final String description = getParam().govLabelTxt();
		final int sw = g.getFontMetrics().stringWidth(description);
		setTextRightX(descrX + sw);
		drawString(g, description, descrX, ye);

		final Stroke prev = g.getStroke();
		g.setColor(CHECKBOX_COLOR);
		g.fill(this);

		if (hovering()) {
			g.setColor(Color.yellow);
			g.setStroke(stroke1);
			g.draw(this);
			g.setStroke(stroke2);
			g.draw(getSelectionBox());
		}
		if (((ParamBoolean)getParam()).get()) {
			final int hs = height/2;
			final int qs = height/4;
			g.setStroke(stroke2);
			g.setColor(SystemPanel.whiteText);
			g.drawLine(x-s1, ye-hs, x+qs, ye-qs);
			g.drawLine(x+qs, ye-qs, xe, y);
		}
		g.setStroke(prev);
	}
}
