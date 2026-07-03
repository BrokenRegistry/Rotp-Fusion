package rotp.ui.util.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.SwingConstants;

class RotpJSpinnerIcon implements Icon {

	private int dir;
	/**
	 * 
	 * @param direction SwingConstants: EST | WEST
	 */
	RotpJSpinnerIcon (int direction) {  dir = direction; }
	private Polygon arrow(int width, int height, int border) {
		int dh = Math.max(1, height/19);
		Polygon p = new Polygon();
		p.reset();
		int b2 = 3*border/2;
		int b  = border;
		int m  = width/5;
		int w  = width - m;
		if (dir == SwingConstants.EAST) {
			p.addPoint(m+b,		b+b + dh);
			p.addPoint(m-b2+w,	height/2);
			p.addPoint(m+b,		height -dh - b-b);
			return p;
		}
		else if (dir == SwingConstants.WEST) {
			p.addPoint(w-b,	b+b + dh);
			p.addPoint(b2,	height/2);
			p.addPoint(w-b,	height - dh - b-b);
			return p;
		}
		else if (dir == SwingConstants.NORTH) {
			p.addPoint(width/2, border);
			p.addPoint(width-border, height - border);
			p.addPoint(border, height - border);
			return p;
		}
		p.addPoint(border, border);
		p.addPoint(width-border, border);
		p.addPoint(width/2, height - border);
		return p;
	}
	@Override public int getIconHeight() { return 20; }
	@Override public int getIconWidth()	 { return 20; }
	@Override public void paintIcon(Component component, Graphics g0, int xi, int yi) {
		Graphics2D g = (Graphics2D) g0;
		RotpJSpinnerButton button = (RotpJSpinnerButton) component;
		RotpJSpinner spinner = (RotpJSpinner) button.getParent();
		
		ButtonModel buttonModel = button.getModel();
		Color borderC = spinner.borderColor();
		Color centerC = spinner.valueBgColor();
		int border = 1;
		int x = 0;
		int y = 0;
		int w = button.getWidth();
		int h = button.getHeight();
		
		if (!buttonModel.isEnabled()) {
			borderC = spinner.disabledColor();
			centerC = spinner.hiddenColor();
		}
		else if (buttonModel.isRollover()) {
			borderC = spinner.hoverColor();
			centerC = spinner.hoverColor();
		}
		// Fill background to go over OS choices...
		g.setColor(spinner.panelBgColor());
		g.fillRect(x, y, w, h);
		
		// Draw borders
		g.setColor(borderC);
		g.fill(arrow(w, h, 0));

		// Fill the buttons
		g.setColor(centerC);
		g.fill(arrow(w, h, border));		
	}
}
