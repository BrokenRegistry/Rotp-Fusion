package rotp.util;

import static rotp.ui.game.AdvisorPanel.isAdvising;
import static rotp.ui.game.IAdvisor.ADVISOR;
import static rotp.ui.game.IAdvisor.HELP_KEY;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import rotp.model.IAdvice;
import rotp.model.Sprite;
import rotp.ui.util.IParam;
import rotp.util.Base.Rect;
import rotp.util.sound.SoundManager;

public class AdviceBox extends Rect implements IAdvice {
	private static final long serialVersionUID = 1L;
	private String labelKey, adviceHelpKey, topLeftHelpKey, advisorImageKey;
	private String adviceHelpTxt;
	private IParam<?> param;
	private Point offset = new Point();		// to offset the arrow tip
	private Point targetLoc = new Point();	// to offset the box
	private Rectangle selectionBox = new Rectangle();
	private JComponent panel;
	private boolean hovering;
	private int forcedLocation = 0;	// For box, following num-pad position
	private Sprite spriteToDraw;	// to not be hidden by the avatar 

	private Supplier<BufferedImage> getHelpImg; // Either Draw, choosing a location or return an image to be resized and positioned 

	public AdviceBox()				{ super(); }
	public AdviceBox(String key)	{ labelKey = key; }
	public AdviceBox(IParam<?> p)	{ param = p; }
	public AdviceBox(Point p)		{ super(p); }
	public AdviceBox(Dimension d)	{ super(d); }
	public AdviceBox(Rectangle r)	{ super(r); }
	public AdviceBox(Point p, Dimension d)	{ super(p, d); }
	public AdviceBox(int width, int height)	{ super(width, height); }
	public AdviceBox(JComponent parent)		{ panel = parent; }
	public AdviceBox(int x, int y, int width, int height)	{ super(x, y, width, height); }
	public AdviceBox(JComponent p, String key, boolean aHK)	{
		panel = p;
		labelKey = key;
		if (aHK)
			adviceHelpKey = key + HELP_KEY;
	}

	public int xOffset()		{ return x + offset.x; }
	public int yOffset()		{ return y + offset.y; }
	public Point getTargetLoc()	{ return new Point(targetLoc.x + offset.x, targetLoc.y + offset.y); }
	public IParam<?> getParam()	{ return param; }
	public JComponent getPane()	{ return panel; }
	public int getForcedLocation()	{ return forcedLocation; }
	public Sprite getSpriteToDraw()	{ return spriteToDraw; }
	public Supplier<BufferedImage> getHelpImg()	{ return getHelpImg; }

	public void convertLocation(JComponent dest)	{ // do not call when hidden
		if (panel == null)
			targetLoc = getLocation();
		else if (!panel.isVisible())
			return;
		else
			targetLoc = SwingUtilities.convertPoint(panel, getLocation(), dest);
	}
	public void setParam(IParam<?> p)	{ param = p; }
	public void setPane(JComponent c)	{ panel = c; }
	public void setOffset(Point pt)		{ offset = pt; }
	public void setOffset(int x, int y)	{ offset = new Point(x, y); }
	public void setLabelKey(String key)	{ labelKey = key; }
	public void setForcedLocation(int loc)		{ forcedLocation = loc; }
	public void setAdviceHelpKey(String key)	{ adviceHelpKey = key; }
	public void setAdviceHelpText(String text)	{ adviceHelpTxt = text; }
	public void setTopLeftHelpKey(String key)	{ topLeftHelpKey = key; }
	public void setAdvisorImageKey(String key)	{ advisorImageKey = key; }
	public void setSpriteToDraw(Sprite sprite)	{ spriteToDraw = sprite; }
	public void setGetHelpImg(Supplier<BufferedImage> fct)	{ getHelpImg = fct; }

	public void init(JComponent c, IParam<?> p, String labKey, String advKey)	{
		panel = c;
		param = p;
		labelKey = labKey;
		adviceHelpKey = advKey;
	}

	public void fillEnvRoundRect(Graphics2D g, int r, int ext)		{ g.fillRoundRect(x+ext, y+ext, width+ext+ext, height+ext+ext, r, r); }
	public void fillShiftRoundRect(Graphics2D g, int r, int shift)	{ g.fillRoundRect(x+shift, y+shift, width, height, r, r); }
	public void fillRoundRect(Graphics2D g, int r)	{ g.fillRoundRect(x, y, width, height, r, r); }
	public void drawRoundRect(Graphics2D g, int r)	{ g.drawRoundRect(x, y, width, height, r, r); }
	public void fillOval(Graphics2D g)				{ g.fillOval(x, y, width, height); }
	public void drawOval(Graphics2D g)				{ g.drawOval(x, y, width, height); }
	public Rectangle getSelectionBox()	{ return selectionBox; }
	public String getLabelKey()			{ return labelKey; }
	public String getAdviceHelpKey()	{ return adviceHelpKey; }
	public String getTopLefteHelpKey()	{ return topLeftHelpKey; }
	public String getAdvisorImageKey()	{ return advisorImageKey; }
	public String getAdviceHelpText()	{
		if (adviceHelpTxt != null)
			return adviceHelpTxt;

		if (param != null)
			return param.getFullHelp();

		if (adviceHelpKey != null)
			return LabelManager.current().realLabel(adviceHelpKey);

		if (labelKey != null) {
			String s = LabelManager.current().realLabel(labelKey + HELP_KEY);
			if (s != null)
				return s;
		}
		return null;
	}
	public String getDescriptionText()	{
		if (param != null)
			return param.getDescription();

		if (adviceHelpKey != null) {
			String s = LabelManager.current().realLabel(adviceHelpKey.replace(HELP_KEY, IParam.LABEL_DESCRIPTION));
			if (s != null)
				return s;
		}

		if (labelKey != null) {
			String s = LabelManager.current().realLabel(labelKey + IParam.LABEL_DESCRIPTION);
			if (s != null)
				return s;
		}
		return null;
	}
	public void mapX(int i)	{ x = i; selectionBox.x = i;}
	public void mapY(int i)	{ y = i; selectionBox.y = i;}
	public void hovering(boolean b)	{ hovering = b; }

	public void setSelectionBounds(int x, int y, int w, int h)	{ selectionBox.setBounds(x, y, w, h); }
	public void setSelectionSize(int w, int h)		{ selectionBox.setSize(w, h); }
	public void setSelectionLocation(int x, int y)	{ selectionBox.setLocation(x, y); }

	public void mouseReleased(MouseEvent e, boolean click, boolean repaint)	{
		if (getParam() != null) {
			if (click)
				SoundManager.current().playAudioClip("SoftClick");
			getParam().toggle(e, null);
			if (repaint && panel!=null)
				panel.repaint();
		}
		else if (click)
			SoundManager.current().playAudioClip("MisClick");
	}
	public void mouseWheelMoved(MouseWheelEvent e, boolean click, boolean repaint) {
		if (getParam() != null) {
			if (click)
				SoundManager.current().playAudioClip("SoftClick");
			getParam().toggle(e);
			if (repaint && panel!=null)
				panel.repaint();
		}
		else if (click)
			SoundManager.current().playAudioClip("MisClick");
	}

	public boolean isSelectableAt(int x, int y)	{
		boolean wasHovering = hovering;
		hovering = selectionBox.contains(x, y);
		if (hovering && !wasHovering && isAdvising())
			ADVISOR.hoveringOverElement(this);
		else if(wasHovering && !hovering && isAdvising())
			ADVISOR.leavedElement(this);
		return hovering;
	}
	@Override public boolean contains(int x, int y)	{
		boolean wasHovering = hovering;
		hovering = selectionBox.contains(x, y);
		if (hovering && !wasHovering && isAdvising())
			ADVISOR.hoveringOverElement(this);
		else if(wasHovering && !hovering && isAdvising())
			ADVISOR.leavedElement(this);
		return hovering;
	}
	@Override public void setSize(int w, int h)		{
		super.setSize(w, h);
		selectionBox.setSize(w, h);
	}
	@Override public void setLocation(int x, int y)	{
		super.setLocation(x, y);
		selectionBox.setLocation(x, y);
	}
	@Override public void setBounds(int x, int y, int w, int h)	{
		super.setBounds(x, y, w, h);
		setSelectionBounds(x, y, w, h);
	}
	@Override public AdviceBox getBox()	{ return this; }
	@Override public boolean hovering()	{ return hovering; }
}