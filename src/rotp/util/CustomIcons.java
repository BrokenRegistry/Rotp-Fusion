package rotp.util;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import rotp.ui.game.GameUI;

public class CustomIcons implements Base {
	public static final CustomIcons instance = new CustomIcons();
	private CustomIcons() {}

	@Override public BufferedImage rulesIcon(int width, int height, int cnr) {
		// Create image and fill background color
		BufferedImage rulesIcon = new BufferedImage(width, height, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(rulesIcon);
		g.setColor(multColor(GameUI.raceCenterColor(), 0.8f));
		g.fillRoundRect(0, 0, width, height, cnr, cnr);

		// Add background image
		BufferedImage img = optionsIconBG();
		int x1 = cnr/4;
		int y1 = x1;
		int x2 = width - x1;
		int y2 = height - y1;
		int imgW = img.getWidth();
		int imgH = img.getHeight();
		g.drawImage(img, x1, y1, x2, y2, 0, 0, imgW, imgH, null);

		// Add specific graphics
		x1 = width/6;
		y1 = height/5;
		x2 = width - x1;
		y2 = height - height/3;
		img  = rulesImage(GameUI.saveGameBackgroundColor());
		img  = resizeImage(img, x2-x1, y2-y1);
		imgW = img.getWidth();
		imgH = img.getHeight();
		g.drawImage(img, x1, y1, x2, y2, 0, 0, imgW, imgH, null);
		
		g.dispose();
		return rulesIcon;
	}
	@Override public BufferedImage settingsIcon(int width, int height, int cnr) {
		// Create image and fill background color
		BufferedImage settingsIcon = new BufferedImage(width, height, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(settingsIcon);
		g.setColor(multColor(GameUI.raceCenterColor(), 0.8f));
		g.fillRoundRect(0, 0, width, height, cnr, cnr);

		// Add background image
		BufferedImage img = optionsIconBG();
		int x1 = cnr/4;
		int y1 = x1;
		int x2 = width - x1;
		int y2 = height - y1;
		int imgW = img.getWidth();
		int imgH = img.getHeight();
		g.drawImage(img, x1, y1, x2, y2, 0, 0, imgW, imgH, null);
		
		// Add specific graphics
		x1 = width/6;
		y1 = height/4;
		x2 = width - x1;
		y2 = height *2/3;
		int w1	= x2-x1;
		int h1	= y2-y1;
		int side = min(w1, h1);
		int dx	= (w1-side)/2;
		int dy	= (h1-side)/2;
		x1 += dx;
		x2 = x1 + side;
		y1 += dy;
		y2 = y1 + side;

		img  = settingsImage(GameUI.saveGameBackgroundColor());
		img	 = resizeImage(img, side, side);
		g.drawImage(img, x1, y1, x2, y2, 0, 0, side, side, null);

		g.dispose();
		return settingsIcon;
	}
	@Override public BufferedImage subMenuIcon(int wi, int hi, Color rectCol, Color barCol, Color lineCol) {
		BufferedImage img = subMenuImage(rectCol, barCol, lineCol);
		return resizeImage(img, wi, hi);
	}
	@Override public BufferedImage subMenuMoreIcon(int wi, int hi, Color rectCol, Color barCol, Color lineCol) {
		BufferedImage img = subMenuMoreImage(rectCol, barCol, lineCol);
		return resizeImage(img, wi, hi);
	}
	@Override public BufferedImage eyeIcon(int wi, int hi, Color lineCol, boolean fillIris) {
		BufferedImage img = eyeImage(96, 96, lineCol, fillIris);
		return resizeImage(img, wi, hi);
	}
	@Override public BufferedImage targetIcon(int wi, int hi, Color lineCol, int CircleNum) {
		BufferedImage img = targetImage(96, 96, lineCol, CircleNum);
		return resizeImage(img, wi, hi);
	}
	@Override public BufferedImage globalDefaultDesignIcon(int side, Color lineCol) {
		BufferedImage img = globalDefaultDesignImage(96, lineCol);
		return resizeImage(img, side, side);
	}
	@Override public BufferedImage localDefaultDesignIcon(int side, Color lineCol) {
		BufferedImage img = localDefaultDesignImage(96, 96, lineCol);
		return resizeImage(img, side, side);
	}
	@Override public BufferedImage magnifierIcon(int side, Color lineCol) {
		BufferedImage img = magnifierImage(96, 96, lineCol);
		return resizeImage(img, side, side);
	}

	private BufferedImage optionsIconBG() {
		int cnr = 3;
	 	int bh  = 5;
	 	int bw1 = 8;
		int sp1 = 12;
		int bw2 = 12;
		int sp2 = 5;
		int descH   = 8;
		int descSep = 3;
		
		int w = bw1 + sp1 + 3*sp2 + 4*bw2;
		int h = w;

		BufferedImage img = new BufferedImage(w, h, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(img);
		
		// button row
		g.setColor(GameUI.buttonBackgroundColor());
		int x = 0;
		int y = h - bh;
		g.fillRoundRect(x, y, bw1, bh, cnr, cnr);
		x += bw1 + sp1;
		g.fillRoundRect(x, y, bw2, bh, cnr, cnr);
		x += bw2 + sp2;
		g.fillRoundRect(x, y, bw2, bh, cnr, cnr);
		x += bw2 + sp2;
		g.fillRoundRect(x, y, bw2, bh, cnr, cnr);
		x += bw2 + sp2;
		g.fillRoundRect(x, y, bw2, bh, cnr, cnr);
		
		// Description bar
		g.setColor(GameUI.setupFrame());
		x = 0;
		y -= descSep + descH;
		g.fillRect(x, y, w, descH);

		// Title bar
		g.setColor(Color.lightGray);
		int th = 4;
		x = w/4;
		int tw = w-x-x;
		y = 0;
		g.fillRect(x, y, tw, th);

		g.dispose();
		return img;
	}
	private BufferedImage subMenuImage(Color rectCol, Color barCol, Color lineCol) {
		int lineThick = 4;
		int barThick  = 6;
		int rectThick = 18;
		int rectGap   = 21;
		int arcD  = 12;
		int leftW = 32;
		int yBar  = 6;
		int yLine = 7;
		int xLine = 7;
		int barVL = barThick;
		int barVR = barVL + barThick;
		int barVC = (barVL + barVR)/2;
		int barHR = leftW - barThick;
		
		int h = rectThick*3 + rectGap*2;
		int w = h;
		BufferedImage img = new BufferedImage(w, h, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(img);

		// top bar
		int rectW = w;
		int x = 0;
		int y = 0;
		g.setColor(rectCol);
		g.fillRoundRect(x, y, rectW, rectThick, arcD, arcD);
		g.setColor(lineCol);
		g.fillRect(x+xLine+leftW, y+yLine, rectW-2*xLine-leftW, lineThick);

		// center bar
		y += rectThick+rectGap;
		x = leftW;
		rectW = w - x;
		g.setColor(rectCol);
		g.fillRoundRect(x, y, rectW, rectThick, arcD, arcD);
		g.setColor(lineCol);
		g.fillRect(x+xLine, y+yLine, rectW-2*xLine, lineThick);
		int xl = barVR;
		int lineW = barHR - xl;
		int lineH = barThick;
		g.setColor(barCol);
		g.fillRect(xl, y+yBar, lineW, lineH);
		
		// bottom bar
		y += rectThick+rectGap;
		x = leftW;
		rectW = w - x;
		g.setColor(rectCol);
		g.fillRoundRect(x, y, rectW, rectThick, arcD, arcD);
		g.setColor(lineCol);
		g.fillRect(x+xLine, y+yLine, rectW-2*xLine, lineThick);
		xl = barVC;
		lineW = barHR - xl;
		lineH = barThick;
		g.setColor(barCol);
		g.fillRect(xl, y+yBar, lineW, lineH);
		
		// vertical Bar
		x = barVL;
		y = rectThick + barThick;
		lineW = barThick;
		lineH = h - y - barThick;
		g.fillRoundRect(x, y, lineW, lineH, arcD, arcD);
		g.fillRect(x, y, lineW, lineW);

		g.dispose();
		return img;
	}
	private BufferedImage rulesImage(Color color) {
		Graphics2D g;
		int anvilW1	 = 35;
		int anvilH1	 = 10;
		int anvilW2	 = 50;
		int anvilH2	 = 10;
		int anvilGap = -3;
		int anvilCnr = 5;

		int armW	= 60;
		int armH	= 10;
		int armGap	= 2;
		int armCnr	= 10;

		int bodyH	= 30;
		int bodyW	= 25;
		int bodyCnr	= 5;

		int sideW	= bodyW + 10;
		int sideH	= 7;
		int sideGap	= 2;
		int sideCnr = 5;
		int sideDX	= (sideW-bodyW)/2;

	   	double armRot	 = Math.toRadians(2);
	   	double armSin	 = Math.sin(armRot);
	   	float  armShift	 = (float) (armSin*armW);
	   	int	armShiftCeil = max (1, ceil(armShift));
	   	int	armShiftRnd	 = max (1, round(armShift));

	   	// create arm
		int armHeight = armH + 2 * armShiftCeil;
		int armWidth  = armW + armGap;
		BufferedImage arm = new BufferedImage(armWidth, armHeight, TYPE_INT_ARGB);
		g = getGraphicsRH(arm);
		g.setColor(color);
		AffineTransform at = new AffineTransform();
		at.rotate(armRot, 0, armShiftRnd);
		RoundRectangle2D armHalf = new RoundRectangle2D.Float(0, 0, armW, armH, armCnr, armCnr);
		Shape armHalfRot = at.createTransformedShape(armHalf);
		g.fill(armHalfRot);
		g.drawImage(arm, 0, armHeight, armWidth, 0, 0, 0, armWidth, armHeight, null);
		g.dispose();

		// create mallet
		int malletH = bodyH + 2*(sideGap+sideH);
		int malletW = armW + armGap + bodyW + sideDX;
		BufferedImage mallet = new BufferedImage(malletW, malletH, TYPE_INT_ARGB);
		g = getGraphicsRH(mallet);
		g.setColor(color);
		// add arm
		int x = 0;
		int y = (malletH - armHeight)/2;
		g.drawImage(arm, x, y, null);
		// add body
		x = armW + armGap;
		y = (malletH - bodyH)/2;
		g.fillRoundRect(x, y, bodyW, bodyH, bodyCnr, bodyCnr);
		// add Top Side
		x -= sideDX;
		y -= sideGap + sideH;
		g.fillRoundRect(x, y, sideW, sideH, sideCnr, sideCnr);
		// add Bottom Side
		y = (malletH + bodyH)/2 + sideGap;
		g.fillRoundRect(x, y, sideW, sideH, sideCnr, sideCnr);
		g.dispose();
		
		// Create image
		double malletRot = Math.toRadians(-140);
		int malletDX = 20;
		int	malletDY = (anvilH1 + anvilGap + anvilH2);
		at = new AffineTransform();
		at.rotate(malletRot, malletW, malletH);
		
		int w  = malletW + anvilW2 - 2*malletDX;
		int h  = malletH + 3*malletDY;
		BufferedImage rulesImage = new BufferedImage(w, h, TYPE_INT_ARGB);
		g = getGraphicsRH(rulesImage);
		g.setColor(color);
		// add mallet
		at = new AffineTransform();
		at.translate(malletDX*4/5, malletDY*6/4);
		at.rotate(malletRot, malletW/2, malletH/2);
		g.drawRenderedImage(mallet, at);
		// add anvil bottom
		x = 0;
		y = h - anvilH2;
		g.fillRoundRect(x, y, anvilW2, anvilH2, anvilCnr, anvilCnr);
		x += (anvilW2 - anvilW1)/2;
		y -= anvilH1 + anvilGap;
		g.fillRoundRect(x, y, anvilW1, anvilH1, anvilCnr, anvilCnr);

		g.dispose();
		return rulesImage;
	}
	private BufferedImage settingsImage(Color color)	{
		int lineNum = 3;
		int lineH	= 4;
		int circR	= 2 * lineH;
		int circD	= 2 * circR;
		int lineSp	= 4 * circR;
		int height	= lineSp * (lineNum-1) + circD;
		int width	= height;

		int circX	= width/5;
		int circY	= 0;
		int lineX1	= 0;
		int lineX4	= width;
		int dy		= circR;
		int lineY	= circY + dy;

		BufferedImage settingsImage = new BufferedImage(width, height, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(settingsImage);
		g.setColor(color);
		g.setStroke(new BasicStroke(lineH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		for (int i=0; i<lineNum; i++) {
			int lineX2 = circX;
			int lineX3 = circX + circD;
			g.draw(new Ellipse2D.Float(circX, circY, circD, circD));
			g.drawLine(lineX1, lineY, lineX2, lineY);
			g.drawLine(lineX3, lineY, lineX4, lineY);
			circX = width - circX - circD;
			circY += lineSp;
			lineY	= circY + dy;
		}
		g.dispose();
		return settingsImage;
	}
	private BufferedImage subMenuMoreImage(Color rectCol, Color barCol, Color lineCol) {
		BufferedImage folderImg = subMenuImage(rectCol, barCol, lineCol);
		int folderW	= folderImg.getWidth();
		int folderH	= folderImg.getHeight();
		int incDiv	= 3;
		int incrH	= folderH/incDiv;
		int width	= folderW;
		int height	= folderH + incrH;
		int xFolder	= 0;
		int yFolder	= 0;
		BufferedImage img = new BufferedImage(width, height, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(img);
		g.drawImage(folderImg, xFolder, yFolder, null);

		int dotNum  = 3;
		int dotRayW	= incrH / 4;
		int dotDiaW	= 2 * dotRayW;
		int dotDiaH	= dotDiaW * (incDiv+1)/incDiv;
		
		int dotSep	= 3 * dotRayW;
		int xDotExt	= width - dotDiaW;
		int yDotExt	= height - dotDiaH;

		int ptRayW	= dotRayW * 2/3;
		int ptDiaW	= 2 * ptRayW;
		int ptDiaH	= ptDiaW * (incDiv+1)/incDiv;
		int ptRayH	= ptDiaH / 2;
		int xPoint	= xDotExt + ptRayW -2;
		int yPoint	= yDotExt + ptRayH -2;

		for (int i=0; i<dotNum; i++) {
			g.setColor(barCol);
			g.fillOval(xDotExt, yDotExt, dotDiaW, dotDiaH);
			g.setColor(lineCol);
			g.fillOval(xPoint, yPoint, ptDiaW, ptDiaH);
			xDotExt -= dotSep;
			xPoint -= dotSep;
		}

		g.dispose();
		return img;
	}
	private BufferedImage eyeImage(int width, int height, Color lineCol, boolean fillIris) {
		int strokeW	= fillIris? width/24 : width/12;
		int xCtr = width/2 -1;
		int yCtr = height/2 -1;
		width	 = (xCtr+1)*2 + 1;
		height	 = (yCtr+1)*2 + 1;
		int xRay = xCtr;
		int yRay = xCtr *7/10;
		int rayExt = round(Math.sqrt(yRay*yRay + xRay*xRay))+1;
		int diaExt	= rayExt+rayExt;
		int yCtrUp	= yCtr - yRay -1;
		int yCtrLow	= yCtr + yRay +1;
		int rayIris	= 20;
		int diaIris	= rayIris+rayIris;
		int rayPup	= 6;
		int diaPup	= rayPup+rayPup;

		BufferedImage img = new BufferedImage(width, height, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(img);
		g.setColor(lineCol);
		g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawOval(xCtr-rayExt, yCtrUp-rayExt, diaExt, diaExt);
		g.drawOval(xCtr-rayExt, yCtrLow-rayExt, diaExt, diaExt);

		strokeW	= width/24;
		g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (fillIris)
			g.fillOval(xCtr-rayIris+1, yCtr-rayIris, diaIris, diaIris);
		else
			g.drawOval(xCtr-rayIris+1, yCtr-rayIris, diaIris, diaIris);
		g.setColor(Color.black);
		g.fillOval(xCtr-rayPup+1, yCtr-rayPup, diaPup, diaPup);

		g.dispose();
		return img;
	}
	private BufferedImage targetImage(int width, int height, Color lineCol, int CircleNum) {
		int strokeW	= width/24;
		int xCtr = width/2 -1;
		int yCtr = height/2 -1;
		width	 = (xCtr+1)*2 + 1;
		height	 = (yCtr+1)*2 + 1;
//		int start = strokeW;
//		int stop  = width - strokeW - 1;
		int start = 0;
		int stop  = width - 1;

		BufferedImage img = new BufferedImage(width, height, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(img);
		g.setColor(lineCol);
		g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		int step = width / (2*CircleNum + 1);
		int ray = step;
		g.drawLine(start, yCtr, xCtr-ray, yCtr);
		g.drawLine(xCtr+ray, yCtr, stop, yCtr);
		g.drawLine(xCtr, start, xCtr, yCtr-ray);
		g.drawLine(xCtr, yCtr+ray, xCtr, stop);
		
		for (int i = 0; i < CircleNum; i++) {
			g.drawOval(xCtr-ray, yCtr-ray, 2*ray, 2*ray);
			ray += step;
		}

		g.dispose();
		return img;
	}
	private BufferedImage globalDefaultDesignImage(int side, Color lineCol) {
		int strokeW	= side/12;
		int ctr = side/2 -1;
		side	= (ctr+1)*2 + 1;
		int tlArrow	= strokeW/2;
		int dArrow	= side - strokeW;
		int start	= -135;
		int length	= 270;
		int xArrow	= tlArrow;
		int yArrow	= (int) (tlArrow + dArrow * (2-sqrt(2))/2);
		int lArrow	= side/4;
		int rayDot	= side/8;
		int diaDot	= 2 * rayDot;
		int tlDot	= ctr - rayDot;

		BufferedImage img = new BufferedImage(side, side, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(img);
		g.setColor(lineCol);
		g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawArc(tlArrow, tlArrow, dArrow, dArrow, start, length);
		g.drawLine(xArrow, yArrow, xArrow+lArrow,yArrow);
		g.drawLine(xArrow, yArrow, xArrow, yArrow-lArrow);
		g.drawRoundRect(tlDot, tlDot, diaDot, diaDot, diaDot, diaDot);
		
		g.dispose();
		return img;
	}
	private BufferedImage localDefaultDesignImage(int width, int height, Color lineCol) {
		int strokeW	= width/12;
		int xCtr = width/2 -1;
		int yCtr = height/2 -1;
		width	 = (xCtr+1)*2 + 1;
		height	 = (yCtr+1)*2 + 1;
		int xRay = xCtr;
		int yRay = xCtr *7/10;
		int rayExt = round(Math.sqrt(yRay*yRay + xRay*xRay))+1;
		int diaExt	= rayExt+rayExt;
		int yCtrUp	= yCtr - yRay -1;
		int yCtrLow	= yCtr + yRay +1;
		int rayIris	= 20;
		int diaIris	= rayIris+rayIris;
		int rayPup	= 6;
		int diaPup	= rayPup+rayPup;

		BufferedImage img = new BufferedImage(width, height, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(img);
		g.setColor(lineCol);
		g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawOval(xCtr-rayExt, yCtrUp-rayExt, diaExt, diaExt);
		g.drawOval(xCtr-rayExt, yCtrLow-rayExt, diaExt, diaExt);

		strokeW	= width/24;
		g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawOval(xCtr-rayIris+1, yCtr-rayIris, diaIris, diaIris);
		g.setColor(Color.black);
		g.fillOval(xCtr-rayPup+1, yCtr-rayPup, diaPup, diaPup);

		g.dispose();
		return img;
	}
	private BufferedImage magnifierImage(int width, int height, Color lineCol) {
		int stroke_	= width/24;
		int strokeW	= stroke_+stroke_;
		int ray = width*5/16 + 1;
		int dia	= ray+ray;
		int ctr = ray + stroke_;
		int junction = ctr + stroke_ + round(ray / Math.sqrt(2))+1;

		BufferedImage img = new BufferedImage(width, height, TYPE_INT_ARGB);
		Graphics2D g = getGraphicsRH(img);
		g.setColor(lineCol);
		g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawOval(width-ctr-ray, ctr-ray, dia, dia);

		strokeW	= width/6;
		g.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.drawLine(width-junction, junction, 0, height);

		g.dispose();
		return img;
	}
}
