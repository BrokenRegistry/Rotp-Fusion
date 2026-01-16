package rotp.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.border.LineBorder;

// Extended this class to allow line size and color selection
public class RLineBorder extends LineBorder implements RotPComponents {
	private static final long serialVersionUID = 1L;
	private boolean topOnly = false;

	public RLineBorder(Color color, boolean topOnly) {
		super(color, 1, false);
		this.topOnly = topOnly;
	}
	public void setLineColor(Color color)	{ lineColor = color; }
	public void setThickness(int thickness)	{ this.thickness = thickness; }
	/*
	 * Copied from sun.java2d.pipe.Region.clipRound(double)
	 * because it can not be imported
	 */
	public static int clipRound(final double coordinate) {
        final double newv = coordinate - 0.5;
        if (newv < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        if (newv > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.ceil(newv);
    }
	@Override public JComponent getComponent()	{ return null; }
	/*
	 * Copy from original LineBorder and modified for "top only" 
	 */
	@Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		if ((this.thickness > 0) && (g instanceof Graphics2D)) {
			Graphics2D g2d = (Graphics2D) g;

			AffineTransform at = g2d.getTransform();

			// if m01 or m10 is non-zero, then there is a rotation or shear
			// or if no Scaling enabled,
			// skip resetting the transform
			boolean resetTransform = ((at.getShearX() == 0) && (at.getShearY() == 0)) &&
					((at.getScaleX() > 1) || (at.getScaleY() > 1));

			int xtranslation;
			int ytranslation;
			int w;
			int h;
			int offs;

			if (resetTransform) {
				/* Deactivate the HiDPI scaling transform,
				 * so we can do paint operations in the device
				 * pixel coordinate system instead of the logical coordinate system.
				 */
				g2d.setTransform(new AffineTransform());
				double xx = at.getScaleX() * x + at.getTranslateX();
				double yy = at.getScaleY() * y + at.getTranslateY();
				xtranslation = clipRound(xx);
				ytranslation = clipRound(yy);
				w = clipRound(at.getScaleX() * width + xx) - xtranslation;
				h = clipRound(at.getScaleY() * height + yy) - ytranslation;
				offs = this.thickness * (int) at.getScaleX();
			}
			else {
				w = width;
				h = height;
				xtranslation = x;
				ytranslation = y;
				offs = this.thickness;
			}

			g2d.translate(xtranslation, ytranslation);

			Color oldColor = g2d.getColor();
			g2d.setColor(this.lineColor);

			Shape outer;
			Shape inner;

			int size = offs + offs;
			Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
			if (this.roundedCorners) {
				float arc = .2f * offs;
				outer = new RoundRectangle2D.Float(0, 0, w, h, offs, offs);
				inner = new RoundRectangle2D.Float(offs, offs, w - size, h - size, arc, arc);
				path.append(outer, false);
				path.append(inner, false);
			}
			else if (topOnly) {
				outer = new Rectangle2D.Float(0, 0, w, offs);
				path.append(outer, false);
			}
			else {
				outer = new Rectangle2D.Float(0, 0, w, h);
				inner = new Rectangle2D.Float(offs, offs, w - size, h - size);
				path.append(outer, false);
				path.append(inner, false);
			}
			g2d.fill(path);
			g2d.setColor(oldColor);

			g2d.translate(-xtranslation, -ytranslation);

			if (resetTransform)
				g2d.setTransform(at);
		}
	}
}
