package rotp.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;

import rotp.model.empires.species.ICRSettings;
import rotp.ui.game.GameUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.races.RacesUI;
import rotp.util.FontManager;

public class RComboBox<E> extends JComboBox<E> implements RotPComponents {
	private static final long serialVersionUID = 1L;

	private List<E> selectionList;
//	private RaceList raceList;
	protected int comboFontSize	= 12;
	protected int textIndent	= s5;
	protected int textBaseline	= s4;
	protected Font comboFont	= FontManager.current().narrowFont(comboFontSize);
	protected boolean showArrow = true;

	public void enableArrow(boolean flag)	{ showArrow = flag; }
	protected void showTipFor(int index, String text)	{ }

	
	public RComboBox(List<E> list)	{
		this();
		updateList(list);
	}
	public RComboBox()	{
		super();
		setOpaque(false);
		setLightWeightPopupEnabled(false);
		setBackground(RacesUI.brown);
		setForeground(SystemPanel.blackText);
		getEditor().getEditorComponent().setBackground(GameUI.borderMidColor());
		getEditor().getEditorComponent().setForeground(SystemPanel.blackText);
		setRenderer(new ListRenderer());
		setFont(comboFont);
		setSelectedIndex(0);
		setMaximumRowCount(30);
		setUI(new MyUI());
		addActionListener(e -> selectionAction(e));
	}
	private void selectionAction(ActionEvent evt)	{ // TODO BR: ACTIONS
		String selection = (String) getSelectedItem();
//		raceList.selectedValue(selection);
//		refreshAll();
	}
	@Override public JComponent getComponent()	{ return this; } // TODO BR: Add TOOL TIP
//	@Override public void paintBorder(Graphics g)	{
//		super.paintBorder(g);
//	}
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
	public void updateList(List<E> newList)	{
		removeAllItems();
		selectionList = newList;
		for(E item : selectionList)
			addItem(item);
	}
	private class ListRenderer extends DefaultListCellRenderer	{
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
			setFont(comboFont);
			g.drawString(getText(), textIndent, h-textBaseline);
		}
		@Override public Component getListCellRendererComponent(JList<?> list, 
				Object value, int index, boolean isSelected, boolean cellHasFocus)	{
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (isSelected)
				showTipFor(index, value.toString());
			return this;
		}
	}
	static class MyUI extends BasicComboBoxUI	{
		MyUI()	{
			super();
		}
		@Override protected ComboPopup createPopup() {
			return new MyCP( comboBox );
		}
		@Override protected JButton createArrowButton() {
			JButton arrowButton = new BasicArrowButton( 
					BasicArrowButton.NORTH,
//					BasicArrowButton.SOUTH,
					GameUI.borderMidColor(), 
					GameUI.borderBrightColor(), 
					GameUI.borderDarkColor(), 
					GameUI.borderBrightColor());
			arrowButton.setBorder(BorderFactory.createLineBorder(GameUI.borderDarkColor()));
			return arrowButton;
		}
	}
	static class MyCP extends BasicComboPopup	{
		public MyCP(JComboBox<Object> combo) {
			super(combo);
			scroller.getVerticalScrollBar().setUI(new DarkScrollBarUI());
			scroller.getVerticalScrollBar().setBackground(RacesUI.brown);
			scroller.getVerticalScrollBar().setForeground(RacesUI.scrollBarC);
			
//			scroller.getVerticalScrollBar().setBackground(GameUI.borderBrightColor());
//			scroller.getVerticalScrollBar().setUI(new DarkScrollBarUI());
		}
	}
	static class ColorArrowUI extends BasicComboBoxUI {

		public static ComboBoxUI createUI(JComponent c) {
			return new ColorArrowUI();
		}

		@Override protected JButton createArrowButton() {
			return new BasicArrowButton(
					BasicArrowButton.SOUTH,
					Color.cyan, Color.magenta,
					Color.yellow, Color.blue);
		}
	}
	private static class DarkScrollBarUI extends BasicScrollBarUI {
		@Override protected void configureScrollBarColors() {
//			super.configureScrollBarColors();
			thumbColor = RacesUI.scrollBarC;
//			thumbHighlightColor = RacesUI.scrollBarC;
//			thumbLightShadowColor = RacesUI.scrollBarC;
//			thumbDarkShadowColor = RacesUI.scrollBarC;
//			trackColor = RacesUI.scrollBarC;
//			trackHighlightColor = Color.YELLOW;

//			thumbColor = RacesUI.scrollBarC;
//			thumbColor = GameUI.borderDarkColor();
		}
	}
}

