package rotp.ui.components;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.NORTHEAST;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.SOUTH;
import static java.awt.GridBagConstraints.SOUTHEAST;
import static java.awt.GridBagConstraints.SOUTHWEST;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import rotp.Rotp;
import rotp.ui.ScaledInteger;
import rotp.ui.game.GameUI;
import rotp.ui.game.GuideUI.IGuide;
import rotp.ui.main.SystemPanel;
import rotp.ui.util.IParam;
import rotp.util.Base;
import rotp.util.FontManager;

public interface RotPComponents extends IGuide, Base, ScaledInteger {
	Insets ZERO_INSETS	= new Insets(0, 0, 0, 0);
	String LABEL_DESCRIPTION	= IParam.LABEL_DESCRIPTION;
//	DescriptionPane descriptionPane = new DescriptionPane(); // not cap, because not really a constant!

	default Color buttonBackgroundColor()	{ return GameUI.buttonBackgroundColor(); }
	default Color buttonTextColor()			{ return GameUI.borderBrightColor(); }
	default Color highlightColor()			{ return Color.YELLOW; }

	default Color tooltipBackgroundColor()	{ return GameUI.paneBackgroundColor(); }
	default Color tooltipTxtColor()			{ return SystemPanel.blackText; }
	default Font tooltipFont()				{ return FontManager.getNarrowFont(scaled(tooltipFontSize())); }

	default Font settingLabelFont()			{ return FontManager.getNarrowFont(scaled(settingsFontSize())); }
	default Font settingValueFont()			{ return FontManager.getNarrowFont(scaled(settingsFontSize())); }
	default int baseFontSize()				{ return 14; }
	default int tooltipFontSize()			{ return 12; }
	default int settingsFontSize()			{ return 12; }
	default int baseDismissDelay()			{ return 4000; }	// in ms (default = 4000 ms)
	default int baseInitialDelay()			{ return 750; }		// in ms (default = 750 ms)

	// #=== 
	/**
	 * Creates a {@code GridBagConstraints} object with
	 * all of its fields set to the passed-in arguments.
	 *
	 * Note: Because the use of this constructor hinders readability
	 * of source code, this constructor should only be used by
	 * automatic source code generation tools.
	 *
	 * @param gridx     The initial gridx value (= Column).
	 * @param gridy     The initial gridy value (= Row).
	 * @param gridwidth The initial gridwidth value (= Number of Columns).
	 * @param gridheight The initial gridheight value (= Number of Rows).
	 * @param weightx   The initial weightx value (shifts rows to side of set anchor).
	 * @param weighty   The initial weighty value (shifts columns to side of set anchor).
	 * @param anchor    The initial anchor value (Can be based on NumPad key location).
	 * @param fill      The initial fill value (For resize purpose).
	 * @param insets    The initial insets value (= placement inside cell).
	 * @param ipadx     The initial ipadx value (= to be added to the width of object).
	 * @param ipady     The initial ipady value (= to be added to the height of object)..
	 *
	 * @see java.awt.GridBagConstraints#gridx
	 * @see java.awt.GridBagConstraints#gridy
	 * @see java.awt.GridBagConstraints#gridwidth
	 * @see java.awt.GridBagConstraints#gridheight
	 * @see java.awt.GridBagConstraints#weightx
	 * @see java.awt.GridBagConstraints#weighty
	 * @see java.awt.GridBagConstraints#anchor
	 * @see java.awt.GridBagConstraints#fill
	 * @see java.awt.GridBagConstraints#insets
	 * @see java.awt.GridBagConstraints#ipadx
	 * @see java.awt.GridBagConstraints#ipady
	 */
	default GridBagConstraints newGbc(int gridx, int gridy,
									int gridwidth, int gridheight,
									double weightx, double weighty,
									int anchor, int fill,
									Insets insets, int ipadx, int ipady) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx		= gridx;		// column
		gbc.gridy		= gridy;		// row
		gbc.gridwidth	= gridwidth;	// number of columns
		gbc.gridheight	= gridheight;	// number of rows
		gbcAnchor(gbc, anchor);			// Location inside the cell
		gbc.fill		= fill;			// For resize purpose
		gbc.ipadx		= ipadx;		// width of object
		gbc.ipady		= ipady;		// height of object
		gbc.insets		= insets;		// placement inside cell
		gbc.weightx		= weightx;		// shifts rows to side of set anchor
		gbc.weighty		= weighty;		// shifts columns to side of set anchor
		return gbc;
	}
	/**
	 * Change some elements of {@code GridBagConstraints} 
	 *
	 * @param gbc       The initial {@code GridBagConstraints} object, it will be modified.
	 * @param gridx     The initial gridx value (= Column).
	 * @param gridy     The initial gridy value (= Row).
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcMove(GridBagConstraints gbc, int dx, int dy) {
		gbc.gridx += dx;	// column
		gbc.gridy += dy;	// row
		return gbc;
	}
	/**
	 * Change some elements of {@code GridBagConstraints}
	 *
	 * @param gbc       The initial {@code GridBagConstraints} object, it will be modified.
	 * @param gridwidth The initial gridwidth value (= Number of Columns).
	 * @param gridheight The initial gridheight value (= Number of Rows).
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcSize (GridBagConstraints gbc, int gridwidth, int gridheight) {
		gbc.gridwidth	= gridwidth;	// number of columns
		gbc.gridheight	= gridheight;	// number of rows
		return gbc;
	}
	/**
	 * Change some elements of {@code GridBagConstraints}
	 *
	 * @param gbc       The initial {@code GridBagConstraints} object., it will be modified.
	 * @param weightx   The initial weightx value (shifts rows to side of set anchor).
	 * @param weighty   The initial weighty value (shifts columns to side of set anchor).
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcWeight (GridBagConstraints gbc, double weightx, double weighty) {
		gbc.weightx		= weightx;		// shifts rows to side of set anchor
		gbc.weighty		= weighty;		// shifts columns to side of set anchor
		return gbc;
	}
	/**
	 * Change some elements of {@code GridBagConstraints}
	 *
	 * @param gbc       The initial {@code GridBagConstraints} object., it will be modified.
	 * @param ipadx     The initial ipadx value (= to be added to the width of object).
	 * @param ipady     The initial ipady value (= to be added to the height of object).
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcPad (GridBagConstraints gbc, int ipadx, int ipady) {
		gbc.ipadx		= ipadx;		// width of object
		gbc.ipady		= ipady;		// height of object
		return gbc;
	}
	/**
	 * Change the anchor of {@code GridBagConstraints} 
	 *
	 * @param gbc    The initial {@code GridBagConstraints} object, it will be modified.
	 * @param anchor The initial anchor value (Can be based on NumPad key location).
	 * @return the interpreted anchor value.
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcAnchor(GridBagConstraints gbc, int anchor) {
		if (anchor == 0) {
			System.out.println("GridBagConstraints gbcAnchor(GridBagConstraints gbc, int anchor = 0)");
		}
		switch (anchor) {
			// Based on numPad
			case 1: gbc.anchor = SOUTHWEST;	break;
			case 2: gbc.anchor = SOUTH;		break;
			case 3: gbc.anchor = SOUTHEAST;	break;
			case 4: gbc.anchor = WEST;		break;
			case 5: gbc.anchor = CENTER;	break;
			case 6: gbc.anchor = EAST;		break;
			case 7: gbc.anchor = NORTHWEST;	break;
			case 8: gbc.anchor = NORTH;		break;
			case 9: gbc.anchor = NORTHEAST;	break;
			// Based on original constant
			default: gbc.anchor = anchor;
		}
		return gbc;
	}
	/**
	 * Credit to https://stackoverflow.com/users/964243/boann
	 * 
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document},
	 * and a {@link PropertyChangeListener} on the text component to detect
	 * if the {@code Document} itself is replaced.
	 * 
	 * @param text any text component, such as a {@link JTextField}
	 *        or {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s
	 *        when the text is changed; the source object for the events
	 *        will be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	default void addChangeListener(JTextComponent text, ChangeListener changeListener) {
		Objects.requireNonNull(text);
		Objects.requireNonNull(changeListener);
		DocumentListener dl = new DocumentListener() {
			private int lastChange = 0;
			private int lastNotifiedChange = 0;

			@Override public void insertUpdate(DocumentEvent e) { changedUpdate(e); }
			@Override public void removeUpdate(DocumentEvent e) { changedUpdate(e); }
			@Override public void changedUpdate(DocumentEvent e) {
				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastNotifiedChange != lastChange) {
						lastNotifiedChange = lastChange;
						changeListener.stateChanged(new ChangeEvent(text));
					}
				});
			}
		};
		text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
			Document d1 = (Document)e.getOldValue();
			Document d2 = (Document)e.getNewValue();
			if (d1 != null) d1.removeDocumentListener(dl);
			if (d2 != null) d2.addDocumentListener(dl);
			dl.changedUpdate(null);
		});
		Document d = text.getDocument();
		if (d != null) d.addDocumentListener(dl);
	}
	default void initTooltips()	{
		UIManager.put("ToolTip.font", tooltipFont());
		UIManager.put("ToolTip.background", tooltipBackgroundColor());
		UIManager.put("ToolTip.foreground", tooltipTxtColor());
		setTooltipDismissDelay(baseDismissDelay());
		setTooltipInitialDelay(baseInitialDelay());
	}
	default void setTooltipInitialDelay(int ms)	{ToolTipManager.sharedInstance().setInitialDelay(ms);} // (4000 ms)
	default void setTooltipDismissDelay(int ms)	{ToolTipManager.sharedInstance().setDismissDelay(ms);} // (750 ms)
	default void setTooltipEnabled(boolean is)	{ToolTipManager.sharedInstance().setEnabled(is);}
	default boolean isTooltipEnabled()			{return ToolTipManager.sharedInstance().isEnabled();}
	default int getTooltipInitialDelay()		{return ToolTipManager.sharedInstance().getInitialDelay();}
	default int getTooltipDismissDelay()		{return ToolTipManager.sharedInstance().getDismissDelay();}
	// -#-


	class GuidePopUp extends JTextPane implements Base, ScaledInteger {
		private static final int FONT_SIZE	= 16;
		private final int maxWidth		= scaled(400);
		private final Color helpColor	= new Color(240,240,240);
		private final JTextPane border	= new JTextPane();
		private final JTextPane	margin	= new JTextPane();

		private Color bgC		= GameUI.setupFrame();;
		private Color bdrC		= new Color(bgC.getRed(), bgC.getGreen(), bgC.getBlue(), 160);
		private Color lineColor	= bgC;

		private Rectangle dest	= new Rectangle(0, 0, 0, 0);
		private String text;
		private int x, y, w, h;
		private int[] lineArr;
		private boolean fullHelp;
		private boolean initialised = false;
		private	int	guideFontSize;

		// From box
		private IParam	param;
		private String	label;
//		private ModText modText;
		private int 	mouseBoxIndex;

		// ========== Constructors and initializers ==========
		//	
		GuidePopUp(IParam param)		{
			this.param = param;
			init();
		}
		private void init() {
			if (initialised)
				return;
			add(border, 0);
			add(margin, 0);
			border.setOpaque(true);
			margin.setOpaque(true);
			this.setOpaque(true);
			this.setContentType("text/html");
			this.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			hideGuide();
			guideFontSize(FONT_SIZE);
			initialised = true;
		}
		private	int	guideFontSize()					{ return guideFontSize; }
		private	void guideFontSize(int size)		{ guideFontSize = size; }
		private void setGuideText(String newText)	{
			text = newText;
			text = text.replace("<=", "&lt;=");
			text = text.replace(">=", "&gt;=");
			text = text.replace("<>", "&lt;&gt;");
		}
		private void setFullHelp(boolean full)	{ fullHelp = full; }
		private void setDest(Rectangle newDest)	{
			dest = newDest;
			makeGuideVisible();
			init(dest);
		}
		public  void setDest(Rectangle dest, String text, Graphics g0)	{
			guideFontSize(FONT_SIZE);
			lineArr = null;
			setFullHelp(dest.width == 0);
			setGuideText(text);
			setDest(dest);
		}
//		private boolean setDest(Box dest, boolean fullHelp){
//			if (dest == null)
//				return false;
//			guideFontSize(FONT_SIZE);
//			String txt;
//			if (fullHelp)
//				txt = dest.getFullHelp();
//			else
//				txt = dest.getGuide();
//			if (txt == null || txt.isEmpty())
//				return false;
//			setFullHelp(fullHelp);
//			setGuideText(txt);
//			setDest(dest);
//			if (guideFontSize() < FONT_SIZE) { // To update header size
//				if (fullHelp)
//					txt = dest.getFullHelp();
//				else
//					txt = dest.getGuide();
//				if (txt == null || txt.isEmpty())
//					return false;
//				setFullHelp(fullHelp);
//				setGuideText(txt);
//				setDest(dest);
//			}
//			return true;
//		}
		// ========== Shared Methods ==========
		//
		private void makeGuideVisible()		{
			if(this.isVisible())
				return;
			border.setVisible(true);
			margin.setVisible(true);
			this.setVisible(true);
		}
		private void hideGuide()				{
			border.setVisible(false);
			margin.setVisible(false);
			this.setVisible(false);
		}
		public void clear()		{ hideGuide(); }
		// ========== Private Methods ==========
		//
		private void paintGuide(Graphics g0)	{
			if (dest == null)
				return;
			if (!this.isVisible())
				return;
			Graphics2D g = (Graphics2D) g0;
			makeGuideVisible();
			border.setBackground(bdrC);
			border.setBounds(x-s8, y-s8, w+s16, h+s16);
			margin.setBackground(bgC);
			margin.setBounds(x-s3, y-s3, w+s6, h+s6);
			this.setFont(plainFont(guideFontSize()));
			this.setBackground(bgC);
			this.setBounds(x, y, w, h);
			drawLines(g);
		}
		private void setLineArr(int... arr)		{ lineArr = arr; }
		private void drawLines(Graphics2D g)	{
			if (lineArr != null) {
				Stroke prev = g.getStroke();
				g.setStroke(stroke2);
				g.setColor(lineColor);
				int size = lineArr.length/2 - 1;
				for (int i=0; i<size; i++) {
					int k = 2*i;
					g.drawLine(lineArr[k], lineArr[k+1], lineArr[k+2], lineArr[k+3]);
				}
				g.setStroke(prev);
			}			
		}
		private void autoSize(int width)		{
			int iW = scaled(Rotp.IMG_W - 20);
			int iH = scaled(Rotp.IMG_H - 20);
			int testW, preTest;
			bgC  = fullHelp ? helpColor : GameUI.setupFrame();
			bdrC = new Color(bgC.getRed(), bgC.getGreen(), bgC.getBlue(), 160);
			w = Short.MAX_VALUE;
			boolean go = true;
			while (go) {
				this.setFont(plainFont(guideFontSize()));
				h = Short.MAX_VALUE;
				preTest = -1;
				testW = width - 1; // to prevent rounding errors
				while (h > iH && preTest != testW && testW < iW) {
					preTest = testW;
					this.setSize(new Dimension(testW, Short.MAX_VALUE));
					this.setText(text);
					Dimension paneSize = this.getPreferredSize();
					w = min(testW, paneSize.width);
					h = paneSize.height;
					testW *= (float) h /iH;
				}
				go = (w > iW || h > iH);
				if (go) {
					guideFontSize (max (1, min(guideFontSize()-1, (int)(guideFontSize() * (float)iH/h -1))));
					go = guideFontSize() > 1;
				}
			}
			w += 1;
			margin.setSize(new Dimension(w+s6, h+s6));
			border.setSize(new Dimension(w+s16, h+s16));
			this.setSize(new Dimension(w, h));
		}
		private void init(Rectangle dest)		{ init(dest, s20, s20); }
		private void init(Rectangle dest, int xShift, int yShift) {
			init(dest, xShift, yShift, s10, s10); }
		private void init(Rectangle dest, int xShift, int yShift, int xCover, int yCover) {
			init(dest, xShift, yShift, xCover, yCover, s10, s10); }
		private void init(Rectangle dest,
				int xShift, int yShift, int xCover, int yCover, int xMargin, int yMargin) {
			int xb, xd, yb, yd;
			int iW = scaled(Rotp.IMG_W);
			int iH = scaled(Rotp.IMG_H);
			xCover = min(xCover, dest.width/2);
			yCover = min(yCover, dest.height/2);
			
			autoSize(maxWidth);
			// relative position
			// find X location
			if (2*dest.x + dest.width  > iW) { // put box to the left
				x = dest.x - w - xShift;
				if (x < xMargin)
					x = xMargin;
				xb = x + w;
				xd = dest.x + xCover;
				if (xd < xb)
					xd = xb + s10;
			}
			else { // put box to the right
				x = dest.x + dest.width + xShift;
				if (x+w > iW-xMargin)
					x = iW-xMargin - w;
				xb = x;
				xd = dest.x + dest.width - xCover;
				if (xd > xb)
					xd = xb - s10;
			}
			// find Y location
			if (2*dest.y + dest.width  > iH) { // put box to the top
				y = dest.y - h - yShift;
				if (y < yMargin)
					y = yMargin;
				yb = y + h;
				yd = dest.y + yCover;
				if (yd < yb)
					yb = yd + s10;
			}
			else { // put box to the bottom
				y = dest.y + dest.height + yShift;
				if (y+h > iH-yMargin)
					y = iH-yMargin - h;
				yb = y;
				yd = dest.y + dest.height - yCover;
				if (yd > yb)
					yb = yd - s10;
			}
			if (dest.width>0) // no line for Hotkeys help
				setLineArr(xb, yb, xd, yd);
		}
		// From Box
		public String getDescription()		 {
			String desc = getParamDescription();
			if (desc == null || desc.isEmpty()) {
				desc = getLabelDescription();
				if (desc == null)
					return "";
			}
			return desc;
		}
		private String getFullHelp()		 {
			String help = getParamFullHelp();
			if (help == null || help.isEmpty()) {
				help = getLabelHelp();
				if (help == null)
					return "";
			}
			return help;
		}
		String getHelp()					 {
			String help = getParamHelp();
			if (help == null || help.isEmpty()) {
				help = getLabelHelp();
				if (help == null)
					return "";
			}
			return help;
		}
		private String getGuide()			 {
			String guide = getParamGuide();
			if (guide == null || guide.isEmpty()) {
				guide = getLabelHelp();
				if (guide == null)
					return "";
			}
			return guide;
		}
		int	    mouseBoxIndex()				 { return mouseBoxIndex; }
		private String getLabelDescription() { return IParam.langDesc(label); }
		private String getLabelHelp()		 { return IParam.langHelp(label); }
		private String getParamDescription() {
			if (param == null)
				return "";
			String desc = param.getGuiDescription();
			if (desc == null || desc.isEmpty())
				return param.getToolTip();
			return desc;
		}
		private String getParamHelp()	 	 {
			if (param == null)
				return "";
			return param.getHelp();
		}
		private String getParamFullHelp()	 {
			if (param == null)
				return "";
			return param.getFullHelp();
		}
		private String getParamGuide()		 {
			if (param == null)
				return "";
			return param.getGuide();
		}
	}


	class GuidePopUpText implements Base, ScaledInteger {
		private static final int FONT_SIZE	= 16;
		private final int maxWidth		= scaled(400);
		private final Color helpColor	= new Color(240,240,240);

		private Color bgC		= GameUI.setupFrame();;
		private Color bdrC		= new Color(bgC.getRed(), bgC.getGreen(), bgC.getBlue(), 160);
		private Color lineColor	= bgC;

		private Rectangle dest	= new Rectangle(0, 0, 0, 0);
		private String text;
		private int x, y, w, h;
		private int[] lineArr;
		private boolean fullHelp;
		private boolean initialised = false;
		private	int	guideFontSize;

		// From box
		private IParam	param;
		private String	label;
//		private ModText modText;
		private int 	mouseBoxIndex;

		// ========== Constructors and initializers ==========
		//	
		GuidePopUpText(IParam param)		{
			this.param = param;
//			init();
		}
		private void init(JTextPane pane) {
			if (initialised)
				return;
			pane.setOpaque(true);
			pane.setContentType("text/html");
			pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			hideGuide(pane);
			guideFontSize(FONT_SIZE);
			initialised = true;
		}
		private	int	guideFontSize()					{ return guideFontSize; }
		private	void guideFontSize(int size)		{ guideFontSize = size; }
		private void setGuideText(String newText)	{
			text = newText;
			text = text.replace("<=", "&lt;=");
			text = text.replace(">=", "&gt;=");
			text = text.replace("<>", "&lt;&gt;");
		}
		private void setFullHelp(boolean full)	{ fullHelp = full; }
		private void setDest(JTextPane pane, Rectangle newDest)	{
			dest = newDest;
			makeGuideVisible(pane);
			init(pane, dest);
		}
		public void setDest(JTextPane pane, Rectangle dest, String text, Graphics g0)	{
			guideFontSize(FONT_SIZE);
			lineArr = null;
			setFullHelp(dest.width == 0);
			setGuideText(text);
			setDest(pane, dest);
		}
//		private boolean setDest(Box dest, boolean fullHelp, Graphics g0){
//			if (dest == null)
//				return false;
//			guideFontSize(FONT_SIZE);
//			String txt;
//			if (fullHelp)
//				txt = dest.getFullHelp();
//			else
//				txt = dest.getGuide();
//			if (txt == null || txt.isEmpty())
//				return false;
//			setFullHelp(fullHelp);
//			setGuideText(txt);
//			setDest(dest);
//			if (guideFontSize() < FONT_SIZE) { // To update header size
//				if (fullHelp)
//					txt = dest.getFullHelp();
//				else
//					txt = dest.getGuide();
//				if (txt == null || txt.isEmpty())
//					return false;
//				setFullHelp(fullHelp);
//				setGuideText(txt);
//				setDest(dest);
//			}
//			return true;
//		}
//		// ========== Shared Methods ==========
//		//
		private void makeGuideVisible(JTextPane pane)	{
			if(pane.isVisible())
				return;
			pane.setVisible(true);
		}
		private void hideGuide(JTextPane pane)	{ pane.setVisible(false); }
		public void clear(JTextPane pane)		{ hideGuide(pane); }
		// ========== Private Methods ==========
		//
		private void paintGuide(Graphics g0, JTextPane pane)	{
			if (dest == null)
				return;
			if (!pane.isVisible())
				return;
			Graphics2D g = (Graphics2D) g0;
			makeGuideVisible(pane);
			pane.setFont(plainFont(guideFontSize()));
			pane.setBackground(bgC);
			pane.setBounds(x, y, w, h);
			drawLines(g);
		}
		private void setLineArr(int... arr)		{ lineArr = arr; }
		private void drawLines(Graphics2D g)	{
			if (lineArr != null) {
				Stroke prev = g.getStroke();
				g.setStroke(stroke2);
				g.setColor(lineColor);
				int size = lineArr.length/2 - 1;
				for (int i=0; i<size; i++) {
					int k = 2*i;
					g.drawLine(lineArr[k], lineArr[k+1], lineArr[k+2], lineArr[k+3]);
				}
				g.setStroke(prev);
			}			
		}
		private void autoSize(JTextPane pane, int width)	{
			int iW = scaled(Rotp.IMG_W - 20);
			int iH = scaled(Rotp.IMG_H - 20);
			int testW, preTest;
			bgC  = fullHelp ? helpColor : GameUI.setupFrame();
			bdrC = new Color(bgC.getRed(), bgC.getGreen(), bgC.getBlue(), 160);
			w = Short.MAX_VALUE;
			boolean go = true;
			while (go) {
				pane.setFont(plainFont(guideFontSize()));
				h = Short.MAX_VALUE;
				preTest = -1;
				testW = width - 1; // to prevent rounding errors
				while (h > iH && preTest != testW && testW < iW) {
					preTest = testW;
					pane.setSize(new Dimension(testW, Short.MAX_VALUE));
					pane.setText(text);
					Dimension paneSize = pane.getPreferredSize();
					w = min(testW, paneSize.width);
					h = paneSize.height;
					testW *= (float) h /iH;
				}
				go = (w > iW || h > iH);
				if (go) {
					guideFontSize (max (1, min(guideFontSize()-1, (int)(guideFontSize() * (float)iH/h -1))));
					go = guideFontSize() > 1;
				}
			}
			w += 1;
			pane.setSize(new Dimension(w, h));
		}
		private void init(JTextPane pane, Rectangle dest)	{ init(pane, dest, s20, s20); }
		private void init(JTextPane pane, Rectangle dest, int xShift, int yShift)	{ init(pane, dest, xShift, yShift, s10, s10); }
		private void init(JTextPane pane, Rectangle dest, int xShift, int yShift, int xCover, int yCover)	{
			init(pane, dest, xShift, yShift, xCover, yCover, s10, s10);
		}
		private void init(JTextPane pane, Rectangle dest, int xShift, int yShift, int xCover, int yCover, int xMargin, int yMargin)	{
			int xb, xd, yb, yd;
			int iW = scaled(Rotp.IMG_W);
			int iH = scaled(Rotp.IMG_H);
			xCover = min(xCover, dest.width/2);
			yCover = min(yCover, dest.height/2);
			
			autoSize(pane, maxWidth);
			// relative position
			// find X location
			if (2*dest.x + dest.width  > iW) { // put box to the left
				x = dest.x - w - xShift;
				if (x < xMargin)
					x = xMargin;
				xb = x + w;
				xd = dest.x + xCover;
				if (xd < xb)
					xd = xb + s10;
			}
			else { // put box to the right
				x = dest.x + dest.width + xShift;
				if (x+w > iW-xMargin)
					x = iW-xMargin - w;
				xb = x;
				xd = dest.x + dest.width - xCover;
				if (xd > xb)
					xd = xb - s10;
			}
			// find Y location
			if (2*dest.y + dest.width  > iH) { // put box to the top
				y = dest.y - h - yShift;
				if (y < yMargin)
					y = yMargin;
				yb = y + h;
				yd = dest.y + yCover;
				if (yd < yb)
					yb = yd + s10;
			}
			else { // put box to the bottom
				y = dest.y + dest.height + yShift;
				if (y+h > iH-yMargin)
					y = iH-yMargin - h;
				yb = y;
				yd = dest.y + dest.height - yCover;
				if (yd > yb)
					yb = yd - s10;
			}
			if (dest.width>0) // no line for Hotkeys help
				setLineArr(xb, yb, xd, yd);
		}
		// From Box
		public String getDescription()		 {
			String desc = getParamDescription();
			if (desc == null || desc.isEmpty()) {
				desc = getLabelDescription();
				if (desc == null)
					return "";
			}
			return desc;
		}
		private String getFullHelp()		 {
			String help = getParamFullHelp();
			if (help == null || help.isEmpty()) {
				help = getLabelHelp();
				if (help == null)
					return "";
			}
			return help;
		}
		String getHelp()					 {
			String help = getParamHelp();
			if (help == null || help.isEmpty()) {
				help = getLabelHelp();
				if (help == null)
					return "";
			}
			return help;
		}
		private String getGuide()			 {
			String guide = getParamGuide();
			if (guide == null || guide.isEmpty()) {
				guide = getLabelHelp();
				if (guide == null)
					return "";
			}
			return guide;
		}
		int		mouseBoxIndex()					{ return mouseBoxIndex; }
		private String getLabelDescription()	{ return IParam.langDesc(label); }
		private String getLabelHelp()			{ return IParam.langHelp(label); }
		private String getParamDescription()	{
			if (param == null)
				return "";
			String desc = param.getGuiDescription();
			if (desc == null || desc.isEmpty())
				return param.getToolTip();
			return desc;
		}
		private String getParamHelp()		{ return param == null? "" : param.getHelp(); }
		private String getParamFullHelp()	{ return param == null? "" : param.getFullHelp(); }
		private String getParamGuide()		{ return param == null? "" : param.getGuide(); }
	}
	default void addVariableSpace(Container pane, int x, int y)	{
		GridBagConstraints gbc = newGbc(x,y, 1,1, 1.0,1.0, CENTER, BOTH, ZERO_INSETS, 0,0);
		pane.add(new RLabel(""), gbc);
	}
//	final class DescriptionPane extends RPanel	{
//		private static final long serialVersionUID = 1L;
//		public final JTextPane descriptionBox = new JTextPane();
//		public final JLabel minHeightLabel	= new JLabel();
//		public final JLabel minWidthLabel	= new JLabel();
//		private DescriptionPane()	{
//			setOpaque(false);
//			setLayout(new GridBagLayout());
//
//			add(minHeightLabel, newGbc(0,0, 1,1, 0,0, CENTER, NONE, ZERO_INSETS, 0,0));
//			add(minWidthLabel, newGbc(0,0, 1,1, 0,0, CENTER, NONE, ZERO_INSETS, 0,0));
//			setMinHeight(s41); // two lines
//			setMinWidth(s20);
//
//			descriptionBox.setForeground(Color.BLACK);
//			descriptionBox.setOpaque(false);
//			descriptionBox.setContentType("text/html");
//			descriptionBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
//		}
//		public void init(Color bgC, Color fgC, Font font, String tip, int minW, int minH)	{
//			setBackground(bgC);
//			setForeground(fgC);
//			descriptionBox.setText(tip);
//			setTipFont(font);
//			setMinWidth(minW);
//			setMinHeight(minH);
//		}
//		public void setText(String text)	{
//			descriptionBox.setText(text);
//			repaint();
//		}
//		public void setTipFont(Font font)	{ descriptionBox.setFont(font); }
//		public void setMinHeight(int h)		{ minHeightLabel.setPreferredSize(new Dimension(1, h)); }
//		public void setMinWidth(int w)		{ minWidthLabel.setPreferredSize(new Dimension(w, 1)); }
//
//		@Override protected void paintComponent(Graphics g)	{
//			super.paintComponent(g);
//			int w = getWidth();
//			int h = getHeight();
//			g.setColor(getBackground());
//			g.fillRect(0, 0, w-1, h-1);
//
//			Dimension dim =  descriptionBox.getPreferredSize();
//			descriptionBox.setSize(dim);
//			g.translate(s10, 0);
//			descriptionBox.paint(g);
//			g.translate(-s10, 0);
//		}
//	}
}
