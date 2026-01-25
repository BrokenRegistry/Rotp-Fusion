package rotp.ui.components;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;

import rotp.model.empires.species.ICRSettings;
import rotp.ui.game.GameUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.races.RacesUI;
import rotp.ui.util.IParam;
import rotp.util.FontManager;

public class RComboBox<T> extends JComboBox<T> implements RotPComponents {
	private static final long serialVersionUID = 1L;

	protected List<T> selectionList;
//	private RaceList raceList;
	protected int comboFontSize	= 12;
	protected int textIndent	= s5;
	protected int textBaseline	= s4;
	protected Font comboFont	= FontManager.current().narrowFont(comboFontSize);
	protected boolean showArrow = true;
	protected int boxLocation	= SwingConstants.SOUTH;
	protected IParam<T> param;

	public void enableArrow(boolean flag)	{
		if (flag == showArrow)
			return;
		showArrow = flag;
		if (showArrow)
			setUI(new RComboBoxUI());
	}
	public void setPopupLocation(Integer popupLocation)	{
		boxLocation = popupLocation;
		setUI(new RComboBoxUI());
	}
	protected void showTipFor(int index, String text)	{
		if (param == null)
			return;
		String tip = param.getToolTip(index);
		setDescription(tip);
	}

	public RComboBox(List<T> list)	{
		super();
		updateList(list);
		init();
	}
	public RComboBox(IParam<T> param)	{
		super();
		this.param = param;
		if (param != null) {
			List<T> list = (List<T>) param.getListForUI();
			if (list != null)
				updateList(list);
		}
		init();
	}

	public RComboBox()	{
		super();
		init();
	}
	private void init()	{
		setOpaque(false);
		setLightWeightPopupEnabled(false);
		setBackground(RacesUI.brown);
		setForeground(SystemPanel.blackText);
		setRenderer(new RListRenderer());
		setFont(comboFont);
		setUI(new RComboBoxUI());
	}
	protected Font getSpecialFont(String value)	{ return comboFont; }
	protected Font lastFont = comboFont;
	@Override public JComponent getComponent()	{ return this; } // TODO BR: Add TOOL TIP
	@Override public void paintChildren(Graphics g)	{
		if (showArrow)
			super.paintChildren(g);
	}
	@Override public void paintComponent(Graphics g)	{
		super.paintComponent(g);
		int w = getWidth();
		int h = getHeight();
		if ((w <= 0) || (h <= 0))
			return;
		setRenderingHints(g);
		g.setColor(GameUI.buttonBackgroundColor());
		g.fillRect(0,0, w, h);
		g.setColor(ICRSettings.settingBlandC);
		setFont(comboFont);
		g.drawString("Load GMO File", textIndent, h-textBaseline);
	}
	public void updateList(List<T> newList)	{
		removeAllItems();
		selectionList = newList;
		for(T item : selectionList)
			addItem(item);
	}
	private class RListRenderer extends DefaultListCellRenderer	{
		private static final long serialVersionUID = 1L;
		@Override public void paintComponent(Graphics g)	{
			super.paintComponent(g);
			int w = getWidth();
			int h = getHeight();
			if ((w <= 0) || (h <= 0))
				return;

			setHiRenderingHints(g);
			g.setColor(getBackground());
			g.fillRect(0,0, w, h);

			g.setColor(getForeground());
//			setFont(comboFont);
			setFont(lastFont);
			g.drawString(getText(), textIndent, h-textBaseline);
		}
		@Override public Component getListCellRendererComponent(JList<?> list, 
				Object value, int index, boolean isSelected, boolean cellHasFocus)	{
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			lastFont = getSpecialFont(value.toString());
//			System.out.println("index: " + index + " value: " + value);
			setFont(lastFont);
			if (isSelected)
				showTipFor(index, value.toString());
			return this;
		}
	}
	private class RComboBoxUI extends BasicComboBoxUI	{
		RComboBoxUI()	{ super(); }
		@Override protected RComboPopup createPopup()	{ return new RComboPopup(comboBox); }
		@Override protected JButton createArrowButton()	{ return createArrowButton(boxLocation); }
		private JButton createArrowButton(int boxLocation)	{
			JButton button = new BasicArrowButton( 
					boxLocation,
					GameUI.borderMidColor(), 
					GameUI.borderBrightColor(), 
					GameUI.borderDarkColor(), 
					GameUI.borderBrightColor());
			button.setBorder(BorderFactory.createLineBorder(GameUI.borderDarkColor()));
			return button;
		}
	}
	class RComboPopup extends BasicComboPopup	{
		private static final long serialVersionUID = 1L;

		public RComboPopup(JComboBox<Object> combo) {
			super(combo);
			list.setSelectionBackground(GameUI.raceCenterColor());
			list.setSelectionForeground(highlightColor());
			scroller.getVerticalScrollBar().setUI(darkScrollBarUI);
			scroller.getVerticalScrollBar().setBackground(GameUI.raceCenterColor());
			scroller.getVerticalScrollBar().setForeground(RacesUI.scrollBarC);
		}
		// Copied from BasicComboPopup and added option to give preferred direction
		@Override protected Rectangle computePopupBounds(int px, int py, int pw, int ph)	{
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Rectangle screenBounds;

			// Calculate the desktop dimensions relative to the combo box.
			GraphicsConfiguration gc = comboBox.getGraphicsConfiguration();
			Point p = new Point();
			SwingUtilities.convertPointFromScreen(p, comboBox);
			if (gc != null) {
				Insets screenInsets = toolkit.getScreenInsets(gc);
				screenBounds = gc.getBounds();
				screenBounds.width -= (screenInsets.left + screenInsets.right);
				screenBounds.height -= (screenInsets.top + screenInsets.bottom);
				screenBounds.x += (p.x + screenInsets.left);
				screenBounds.y += (p.y + screenInsets.top);
			}
			else {
				screenBounds = new Rectangle(p, toolkit.getScreenSize());
			}
			int borderHeight = 0;
			Border popupBorder = getBorder();
			if (popupBorder != null) {
				Insets borderInsets = popupBorder.getBorderInsets(this);
				borderHeight = borderInsets.top + borderInsets.bottom;
				screenBounds.width -= (borderInsets.left + borderInsets.right);
				screenBounds.height -= borderHeight;
			}
			boolean canBeBellow = (py + ph) <= (screenBounds.y + screenBounds.height);
			boolean canBeAbove = ph <= (-screenBounds.y - borderHeight);
			if (!canBeAbove && !canBeBellow) {
				// then a full screen height popup
				int y = screenBounds.y + Math.max(0, (screenBounds.height - ph) / 2 );
				int h = Math.min(screenBounds.height, ph);
				return new Rectangle(px, y, pw, h);
			}
			switch (boxLocation) {
				case SwingConstants.NORTH:
					if (canBeAbove)
						return new Rectangle(px, -ph - borderHeight, pw, ph);
					else
						return new Rectangle(px, py, pw, ph);
				case SwingConstants.SOUTH:
				default:
					if (canBeBellow)
						return new Rectangle(px, py, pw, ph);
					else
						return new Rectangle(px, -ph - borderHeight, pw, ph);
			}
		}
	}
	private static RScrollBarUI darkScrollBarUI = new RScrollBarUI();
	private static class RScrollBarUI extends BasicScrollBarUI	{
		@Override protected void configureScrollBarColors()	{ thumbColor = RacesUI.scrollBarC; }
	}
}

