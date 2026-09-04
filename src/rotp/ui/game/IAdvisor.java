package rotp.ui.game;

import static rotp.model.game.IBaseOptsTools.BASE_UI;
import static rotp.ui.game.GuideUI.cleanHtmlText;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JEditorPane;

import rotp.model.IAdvice;
import rotp.model.Sprite;
import rotp.model.empires.species.Species;
import rotp.ui.BasePanel;
import rotp.ui.ScaledInteger;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.main.overlay.IMapOverlay;
import rotp.ui.map.IMapHandler;
import rotp.ui.sprites.RoundButtonSprite;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamInteger;
import rotp.util.AdviceBox;
import rotp.util.Base;
import rotp.util.Base.Rect;
import rotp.util.FontManager;

public interface IAdvisor extends ScaledInteger {
	String HELP_KEY				= "_HELP";;
	String DIPLOMAT_ADVISOR		= "MAIN_ADVISOR_DIPLOMACY";
	String SCIENTIST_ADVISOR	= "MAIN_SCIENTIST_ADVISOR";
	String MILITARY_ADVISOR		= "MAIN_MILITARY_ADVISOR";
	String COUNCIL_ADVISOR		= "MAIN_COUNCIL_ADVISOR";
	String DESIGN_ADVISOR		= "MAIN_DESIGN_ADVISOR";
	String BUDGET_ADVISOR		= "MAIN_BUDGET_ADVISOR";
	String SPY_ADVISOR			= "MAIN_SPY_ADVISOR";
	String MAP_ADVISOR			= "MAIN_MAP_ADVISOR";
	String SCOUT_ADVISOR		= "MAIN_SCOUT_ADVISOR";
	Color BACK_TRANS_COLOR	= new Color(34, 53, 102, 127);
	Color BACK_DARK_COLOR	= new Color(34, 53, 102);
	Color CENTER_COLOR		= new Color(79, 102, 156);
	Color EXIT_EDGE_COLOR	= new Color(59, 59, 59);
	Color EXIT_MID_COLOR	= new Color(93, 93, 93);
	Color LINE_COLOR		= BACK_DARK_COLOR;

	JEditorPane GUIDE_BOX	= new JEditorPane();
	Rect ADVISOR_BOX	= new Rect();
	Rect AVATAR_BOX		= new Rect();
	Rect TEXT_BOX		= new Rect();
	ExitBox EXIT_BOX 	= new ExitBox();
	int TEXT_MARGIN		= s10;
	int BORDER_WIDTH	= s5;
	AdvisorVar A		= new AdvisorVar();

	AdvisorPanel ADVISOR = new AdvisorPanel();

	static final ParamBoolean helpShowAdvisor = new ParamBoolean(BASE_UI, "HELP_SHOW_ADVISOR", true).isCfgFile(true);
	static final ParamInteger advisorFontSize = new ParamInteger(BASE_UI, "ADVISOR_FONT_SIZE", 16)
			.setLimits(8, 20)
			.setIncrements(1, 2, 5)
			.isCfgFile(true);
	static final ParamInteger advisorIconSize = new ParamInteger(BASE_UI, "ADVISOR_ICON_SIZE", 100)
			.setLimits(25, 200)
			.setIncrements(1, 5, 20)
			.isCfgFile(true)
			.pctValue(true)
			.setNewValueMethod(IAdvisor.ADVISOR::updateAvatarSize);

	default BufferedImage getAdvisorImage(Species species, String key)	{
		species.resetScientist();
		species.resetSpy();
		species.resetDiplomat();
		species.resetSoldier();
		switch(key) {
			case MAP_ADVISOR:		return species.advisorDiplomacy();
			case DIPLOMAT_ADVISOR:	return species.advisorDiplomacy();
			case SCIENTIST_ADVISOR:	return species.scientistQuiet();
			case MILITARY_ADVISOR:	return species.advisorWeapon();
			case COUNCIL_ADVISOR:	return species.advisorCouncil();
			case DESIGN_ADVISOR:	return species.advisorShip();
			case BUDGET_ADVISOR:	return species.advisorDiplomacy();
			case SCOUT_ADVISOR:		return species.advisorScout();
			case SPY_ADVISOR:		return species.spyQuiet();
			default: return species.advisorDiplomacy();
		}
	}
	default void updateAvatarSize(Integer pct)	{
		AVATAR_BOX.setSize(A.nominalAvatarWidth*pct/100, A.nominalAvatarHeight*pct/100);
		AVATAR_BOX.setLocation(A.leftMargin, A.boxBottom - AVATAR_BOX.height);

		// init exitBox
		EXIT_BOX.reset(); // Background paint
		EXIT_BOX.mapX(AVATAR_BOX.xe() - EXIT_BOX.width() - BORDER_WIDTH);
		EXIT_BOX.mapY(AVATAR_BOX.y + BORDER_WIDTH);
		EXIT_BOX.setSelectionBounds(AVATAR_BOX.x, AVATAR_BOX.y, AVATAR_BOX.width, AVATAR_BOX.height);
	}
	default GalaxyMapPanel map()	{ return A.mapHandler == null ? null : A.mapHandler.map(); }
	default String defaultHelp()	{ return ADVISOR.text("ON_DEMAND_ADVISOR_DEFAULT_HELP"); }
	default String defaultNoHelp(IAdvice target, AdviceBox targetBox)	{
		String key = targetBox.getAdviceHelpKey();
		if (key == null) {
			if (target instanceof Sprite) {
				key = target.toString();
				return ADVISOR.text("ON_DEMAND_ADVISOR_KNOWN_NO_HELP", key);
			}
			else {
				key = targetBox.getLabelKey();
				if (key == null)
					return ADVISOR.text("ON_DEMAND_ADVISOR_NO_HELP_NOKEY");
				else
					return ADVISOR.text("ON_DEMAND_ADVISOR_KNOWN_NO_HELP", key);
			}
		}
		return ADVISOR.text("ON_DEMAND_ADVISOR_NO_HELP", key);
	}
	default String helpText(IAdvice target, AdviceBox targetBox)	{
		if (target == null || targetBox == null)
			return defaultHelp();
		if (targetBox == EXIT_BOX.getBox())
			return defaultHelp();

		String str = targetBox.getAdviceHelpText();
		if (str == null && targetBox.getHelpImg() == null)
			return defaultNoHelp(target, targetBox);
		return str;
	}
	default void setLineArr(int... arr)		{ A.lineArr = arr; }
	default void drawLines(Graphics2D g)	{
		if (A.lineArr == null)
			return;
		Stroke prev = g.getStroke();
		g.setStroke(stroke2);
		g.setColor(LINE_COLOR);
		int size = A.lineArr.length/2 - 1;
		int xe = 0, ye = 0;
		for (int i=0; i<size; i++) {
			int k = 2*i;
			xe = A.lineArr[k+2] + A.dx;
			ye = A.lineArr[k+3] + A.dy;
			g.drawLine(A.lineArr[k], A.lineArr[k+1], xe, ye);
		}
		g.setStroke(prev);
		int r = s3;
		g.fillOval(xe-r, ye-r, r+r, r+r);
	}
	default void drawGuideBox(Graphics2D g, int x0, int y0, int transpMargin, BufferedImage img)	{
		TEXT_BOX.x = x0;
		TEXT_BOX.y = y0;

		if (transpMargin > 0) {
			int x = TEXT_BOX.x - transpMargin;
			int y = TEXT_BOX.y - transpMargin;
			int h = TEXT_BOX.height + transpMargin + transpMargin;
			int w = TEXT_BOX.width  + transpMargin + transpMargin;
			g.setColor(BACK_TRANS_COLOR);
			g.fillRect(x, y, w, h);
		}

		float[] dist	= {0.0f, 1.0f};
		Color[] colors	= {CENTER_COLOR, BACK_DARK_COLOR};
		Point2D center	= TEXT_BOX.getCenter();
		float radius	= (float)TEXT_BOX.getRadius();
		g.setPaint(new RadialGradientPaint(center, radius, dist, colors, MultipleGradientPaint.CycleMethod.REFLECT));
		g.fill(TEXT_BOX);

		if (img == null) {
			img = new BufferedImage(TEXT_BOX.width, TEXT_BOX.height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D gi = Base.getGraphicsRH(img);
			GUIDE_BOX.paint(gi);
			gi.dispose();
		}
		g.drawImage(img, TEXT_BOX.x + TEXT_MARGIN, TEXT_BOX.y + TEXT_MARGIN/2, null);
	}
	default void initGuideBox(Graphics2D g, String tipText, int w, int h)	{
		GUIDE_BOX.setText(cleanHtmlText(tipText));

		autoSizeBox(w, h);
		TEXT_BOX.width = GUIDE_BOX.getWidth() + TEXT_MARGIN + TEXT_MARGIN;
		TEXT_BOX.height = GUIDE_BOX.getHeight() + TEXT_MARGIN;
	}
	private static boolean forcedLeft(int i)	{ return i==7 || i==4 || i==1; }
	private static boolean forcedRight(int i)	{ return i==9 || i==6 || i==3; }
	private static boolean forcedTop(int i)		{ return i>=7; }
	private static boolean forcedUnder(int i)	{ return i==3 || i==2 || i==1; }
	default void setBoxLocation(AdviceBox targetBox)	{
		if (A.parent == null)
			return;
		int iW = A.parent.getWidth() - A.rightMargin;
		int iH = A.parent.getHeight();
		int forcedLocation = targetBox.getForcedLocation();
		Point loc = targetBox.getTargetLoc();
		int x = loc.x;
		int y = loc.y;
		int cover		= s5;
		int xTarCover	= s10;
		int yTarCover	= s10;
		int xBoxMargin	= s10;
		int yBoxMargin	= s10;
		int xLineCover	= cover;
		int yLineCover	= cover;
		int xb, xd, yb, yd;
		xTarCover =Math. min(xTarCover, targetBox.width/2);
		yTarCover = Math.min(yTarCover, targetBox.height/2);

		// relative position
		// find X location
		boolean forcedLeft	= forcedLeft(forcedLocation);
		boolean forcedRight	= forcedRight(forcedLocation);
		boolean atLeft = forcedLeft || !forcedRight && (2*x + targetBox.width > iW);
		if (atLeft) { // put box to the left textBox.x
			TEXT_BOX.x = Math.min(x - TEXT_BOX.width - A.lineLengthX, iW - TEXT_BOX.width);
			if (TEXT_BOX.x < xBoxMargin)
				TEXT_BOX.x = xBoxMargin;
			TEXT_BOX.x += A.dx;
			xb = TEXT_BOX.x + TEXT_BOX.width;
			xd = x + xTarCover;
			if (xd < xb)
				xd = xb + xLineCover;
		}
		else { // put box to the right
			TEXT_BOX.x = x + targetBox.width + A.lineLengthX;
			if (TEXT_BOX.x+TEXT_BOX.width > iW-xBoxMargin)
				TEXT_BOX.x = iW-xBoxMargin - TEXT_BOX.width;
			TEXT_BOX.x += A.dx;
			xb = TEXT_BOX.x;
			xd = x + targetBox.width - xTarCover;
			if (xd > xb)
				xd = xb - xLineCover;
		}

		// find Y location
		boolean forcedTop	= forcedTop(forcedLocation);
		boolean forcedUnder	= forcedUnder(forcedLocation);
		boolean atTop = forcedTop || !forcedUnder && (2*y + targetBox.height > iH);
		if (atTop) { // put box to the top textBox.y
			TEXT_BOX.y = y - TEXT_BOX.height - A.lineLengthY;
			if (TEXT_BOX.y < yBoxMargin)
				TEXT_BOX.y = yBoxMargin;
			TEXT_BOX.y += A.dy;
			yb = TEXT_BOX.y + TEXT_BOX.height;
			yd = y + yTarCover;
			if (yd < yb)
				yb = yd + yLineCover;
		}
		else { // put box to the bottom
			TEXT_BOX.y = y + targetBox.height + A.lineLengthY;
			if (TEXT_BOX.y+TEXT_BOX.height > iH-yBoxMargin)
				TEXT_BOX.y = iH-yBoxMargin - TEXT_BOX.height;
			TEXT_BOX.y += A.dy;
			yb = TEXT_BOX.y;
			yd = y + targetBox.height - yTarCover;
			if (yd > yb)
				yb = yd - yLineCover;
		}
		setLineArr(xb, yb, xd, yd);
	}
	default void autoSizeBox(int maxWidth, int maxHeight)	{
		int iW = maxWidth == 0? A.parent.getWidth() : maxWidth;
		int iH = maxHeight == 0? A.parent.getHeight() : maxHeight;
		int testW, preTest;
		TEXT_BOX.width = Short.MAX_VALUE;
		boolean go = true;
		int guideFontSize = advisorFontSize.get();

		while (go) {
			GUIDE_BOX.setFont(FontManager.current().plainFont(guideFontSize));
			TEXT_BOX.height = Short.MAX_VALUE;
			preTest = -1;
			testW = maxWidth - 1; // to prevent rounding errors
			while (TEXT_BOX.height > iH && preTest != testW && testW < iW) {
				preTest = testW;
				GUIDE_BOX.setSize(new Dimension(testW, iH));
				Dimension paneSize = GUIDE_BOX.getPreferredSize();
				TEXT_BOX.width = Math.min(testW, paneSize.width);
				TEXT_BOX.height = paneSize.height;
				testW *= (float) TEXT_BOX.height /iH;
			}
			go = (TEXT_BOX.width > iW || TEXT_BOX.height > iH);
			if (go) {
				guideFontSize = Math.max(1, Math.min(guideFontSize-1, (int)(guideFontSize * (float)iH/TEXT_BOX.height -1)));
				go = guideFontSize > 1;
			}
		}
		// Try to reduce the with
		Dimension oldSize = TEXT_BOX.getSize();
		Dimension newSize = TEXT_BOX.getSize();
		while (newSize.height == TEXT_BOX.height && oldSize.width > s200) {
			oldSize.width -= s5;
			GUIDE_BOX.setSize(oldSize);
			newSize = GUIDE_BOX.getPreferredSize();
		}
		TEXT_BOX.width = oldSize.width + s5;
		TEXT_BOX.width += 1;
		GUIDE_BOX.setSize(TEXT_BOX.getSize());
	}

	// ========================================================================
	// SUB CLASSES
	//
	default Species getSpecies()	{ return A.species; }
	final class AdvisorVar {
		boolean isAdvising;
		boolean isOnHold;
		IMapHandler mapHandler;
		BasePanel parent;
		BufferedImage avatarImg;
		String advisorKey;
		Species species;
		IAdvice target;
		Point topLeftBoxLocation	= new Point(s50, s50);
		int leftMargin	= s3;
		int rightMargin	= 0;
		int floorMargin	= s3;
		int dx = 0;
		int dy = 0;
		int defaultAvatarHeight	= s200;
		int defaultAvatarWidth	= s160;
		int nominalAvatarHeight	= defaultAvatarHeight;
		int nominalAvatarWidth	= defaultAvatarWidth;
		int boxBottom;
		int lineLengthX	= s25;
		int lineLengthY	= s25;
		int[] lineArr;
	}

	final class ExitBox extends RoundButtonSprite {
		void init(IMapOverlay p, Graphics2D g)	{
			parent = p;
			arc = s18;
			baseLine = s3;
			box.setLabelKey("ON_DEMAND_ADVISOR_BUTTON_OK");
			box.setSize(arc, arc);
		}

		@Override public void draw(GalaxyMapPanel map, Graphics2D g)	{
			if (A.isAdvising)
				directDraw(map, g);
		}
		@Override protected Color[] colors()	{ return new Color[] {EXIT_EDGE_COLOR, EXIT_MID_COLOR, EXIT_EDGE_COLOR}; }
		@Override public void click(GalaxyMapPanel map, int count, boolean rightClick, boolean click, boolean middleClick, MouseEvent e) {
			ADVISOR.advanceMap();
		}
	}
}
