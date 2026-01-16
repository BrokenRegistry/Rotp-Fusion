package rotp.ui.components;

import static java.awt.GridBagConstraints.NONE;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import rotp.ui.components.RButtonBar.ButtonBarListener.BarEvents;
import rotp.ui.components.RotPButtons.RButton;
import rotp.ui.components.RotPButtons.RMiniButton;
import rotp.ui.components.RotPButtons.RMiniToggleButton;
import rotp.ui.util.StringList;

public class RButtonBar extends JPanel implements ActionListener, ItemSelectable, SwingConstants, RotPComponents	{
	private static final long serialVersionUID = 1L;

	public interface GetColor	{ Color color(int idx, String label); }
	public interface ButtonBarListener	{
		enum BarEvents	{ BUTTON_SELECTED, BUTTON_REMOVED, BUTTON_ADDED, BUTTON_RENAMED }
		public void actionPerformed(BarEvent e);
	}
	public class BarEvent {
		public final BarEvents event;
		public final String prevLabel;
		public final String newLabel;
		public final int index;
		BarEvent(BarEvents event, String newLabel, String prevLabel, int index) {
			this.event = event;
			this.prevLabel = prevLabel;
			this.newLabel = newLabel;
			this.index = index;
		}
		@Override public String toString()	{
			String s = event.toString();
			s += " prevLabel: " + prevLabel;
			s += " newLabel: " + newLabel;
			s += " index: " + index;
			return s;
		}
	}
	public static String INSERT_NAME = "Insert";
	public static String REMOVE_NAME = "Remove";
	public static String ADD_NAME	 = "Add After";

	private RButton insertButton;
	private RButton removeButton;
	private RButton addButton;
	private ArrayList<MiniToggle> buttonList;
	private StringList labelList;

	private String barName;
	private boolean westSide;
	private boolean keepFirst;
	private boolean keepOne;

	private ButtonBarListener barListener;
	private BarEvent pendingEvent;
	private GetColor gtc;
	private boolean updating;
	
	private List<ActionListener> sizeListenerList = new ArrayList<>();		// For resize purpose
	private List<ItemListener> itemListenerList	 = new ArrayList<>();		// List selection
	private Function<String, String> newTextRequest;

	public RButtonBar(StringList list, String itemName)	{ this(list, itemName, true, true, false); }

	public RButtonBar(StringList list, String itemName, boolean westSide, boolean keepOne, boolean keepFirst)	{
		setName("RotPButtonBar");
		if (list == null)
			labelList = new StringList();
		else
			labelList = list;
		barName = itemName;
		this.westSide	= westSide;
		this.keepOne	= keepOne;
		this.keepFirst	= keepFirst;
		buttonList		= new ArrayList<>();
		for (String s : labelList) {
			MiniToggle button = new MiniToggle(s);
			buttonList.add(button);
		}

		insertButton = new InsertItemButton(true);
		removeButton = new RemoveItemButton();
		addButton	 = new InsertItemButton(false);

		setOpaque(false);
		setLayout(new GridBagLayout());
		addControlButtons();
		updating = true;
		addToggleButtons();
		updating = false;
		validate();

		int idx = getSelectedIndex();
		buttonList.get(idx).setSelected(true);
	}
	private void addControlButtons()	{
		int incr = westSide? 1 : -1;
		int xPos = westSide? 0 : buttonList.size()+2;
		GridBagConstraints gbc = newGbc(xPos,0, 1,1, 0,0, 5, NONE, ZERO_INSETS, 0,0);
		add(insertButton, gbc);
		gbc.gridx += incr;
		add(removeButton, gbc);
		gbc.gridx += incr;
		gbc.insets = westSide? new Insets(0, 0, 0, s5) : new Insets(0, s5, 0, 0);
		add(addButton, gbc);

		gbc.insets = ZERO_INSETS;

		int idx = getSelectedIndex();
		int i = 0;
		for (MiniToggle b : buttonList) {
			b.setSelected(i == idx);
			gbc.gridx += incr;
			add(b, gbc);
			i++;
		}
	}
	private void addToggleButtons()	{
		updating = true;
		int incr = westSide? 1 : -1;
		int xPos = westSide? 2 : buttonList.size();
		GridBagConstraints gbc = newGbc(xPos,0, 1,1, 0,0, 5, NONE, ZERO_INSETS, 0,0);
		for (MiniToggle b : buttonList) {
			gbc.gridx += incr;
			add(b, gbc);
		}
		updating = false;
	}
	private void removeToggleButtons()	{
		updating = true;
		for (MiniToggle b : buttonList)
			remove(b);
		updating = false;
	}
	private void callEvent(BarEvent event)	{
		// System.out.println("callEvent: " + event.toString());
		if (updating)
			return;

		if (barListener != null) {
			switch(event.event) {
				case BUTTON_ADDED:
				case BUTTON_REMOVED:
					pendingEvent = event;
					return;
				case BUTTON_RENAMED:
				case BUTTON_SELECTED:
					if (pendingEvent == null)
						barListener.actionPerformed(event);
					else
						barListener.actionPerformed(pendingEvent);
					break;
			}
		}

		pendingEvent = null;
	}
	private Color getTextColor(String name)	{
		if (gtc == null)
			return buttonTextColor();
		else {
			int idx = labelList.indexOf(name);
			return gtc.color(idx, name);
		}
	}
	@Override public JComponent getComponent()		{ return this; }
	@Override public Object[] getSelectedObjects()	{
		System.out.println("RSelectionButtons getSelectedObjects()");
		return null;
	}
	@Override public void actionPerformed(ActionEvent e)	{
		System.out.println("RSelectionButtons actionPerformed(ActionEvent e)");
		System.out.println(e.toString());
	}
	@Override public void addItemListener(ItemListener l)	{
		itemListenerList.add(l);
		for(MiniToggle b : buttonList)
			b.addItemListener(l);
	}
	@Override public void removeItemListener(ItemListener l)	{
		itemListenerList.remove(l);
		for(MiniToggle b : buttonList)
			b.removeItemListener(l);
	}
	public void setTextColorGetter(GetColor gtc)				{ this.gtc = gtc; }
	public void setButtonBarListener(ButtonBarListener l)		{ barListener = l; }
	public void setNewTextRequest(Function<String, String> f)	{ newTextRequest = f; }
	public void addSizeListener(ActionListener listener)		{ sizeListenerList.add(listener); }
	public void removeSizeListener(ActionListener listener)		{ sizeListenerList.remove(listener); }
	public void repaintButton(int idx)	{ buttonList.get(idx).repaint(); }
	public void setSelectedIndex(int i)	{
		if (i == labelList.getSelectedIndex())
			return;
		buttonList.get(labelList.getSelectedIndex()).setSelected(false);
		labelList.setSelectedIndex(i);
		MiniToggle button = buttonList.get(i);
		if (!button.isSelected())
			buttonList.get(i).setSelected(true);
	}
	public int getSelectedIndex()		{ return labelList.ValidSelectedIndex(); }
	public int getIndex(MiniToggle b)	{ return buttonList.indexOf(b); }
	public StringList getList()			{ return labelList; }
	public void renameSelected(String newLabel)	{
		int idx = getSelectedIndex();
		String prevLabel = labelList.set(idx, newLabel);
		buttonList.get(idx).setText(newLabel);
		// System.out.println("button name changed: " + prevLabel + " --> " + newLabel);
		callEvent(new BarEvent(BarEvents.BUTTON_RENAMED, newLabel, prevLabel, idx));
	}
	private String getValidtext(String str)	{
		if (newTextRequest != null)
			str = newTextRequest.apply(barName);
		if (str == null)
			str = "New Item";
		if (!labelList.contains(str))
			return str;
		for (int i=1; i<=labelList.size(); i++) {
			if(!labelList.contains(str + " " + i))
				return str + " " + i;
		}
		return str + " " + (labelList.size() + 1);
	}
	// ==========================================
	// Insert Button
	private class InsertItemAction implements ActionListener	{
		@Override public void actionPerformed(ActionEvent evt)	{
			InsertItemButton iB = (InsertItemButton) evt.getSource();
			int idx = getSelectedIndex();
			String prevLabel = labelList.get(idx);
			String newLabel = getValidtext(null);

			if (!iB.insert)
				idx++;
			if (keepFirst && idx==0)
				idx = 1;
			labelList.add(idx, newLabel);
			labelList.setSelectedIndex(idx);
			callEvent(new BarEvent(BarEvents.BUTTON_ADDED, newLabel, prevLabel, idx));

			removeToggleButtons();
			MiniToggle b = new MiniToggle(newLabel);
			buttonList.add(idx, b);
			addToggleButtons();
			revalidate();

			b.setSelected(true);
		}
	}
	private class InsertItemButton extends RMiniButton	{
		private static final long serialVersionUID = 1L;
		private final boolean insert;
		private InsertItemButton(boolean insert)	{
			super((insert ^ westSide)? "BUTTON_BAR_INSERT_RIGHT" : "BUTTON_BAR_INSERT_LEFT");
			setLabelKey();
			this.insert	= insert;
			setName(insert? INSERT_NAME : ADD_NAME);
			addActionListener(new InsertItemAction());
		}
	}
	// ==========================================
	// Remove Button
	private class RemoveItemAction implements ActionListener	{
		@Override public void actionPerformed(ActionEvent evt)	{
			int prevIdx = getSelectedIndex();
			if (keepFirst && prevIdx==0) {
				misClick();
				return;
			}
			if (keepOne && labelList.size()<=1) {
				misClick();
				return;
			}
			String prevLabel = labelList.remove(prevIdx);
			int newIdx = prevIdx;
			if(prevIdx >= labelList.size())
				newIdx--;
			String newLabel = labelList.setSelectedIndex(newIdx);
			callEvent(new BarEvent(BarEvents.BUTTON_REMOVED, newLabel, prevLabel, prevIdx));

			removeToggleButtons();
			buttonList.remove(prevIdx);
			addToggleButtons();
			revalidate();

			buttonList.get(newIdx).setSelected(true);
		}
	}
	private class RemoveItemButton extends RMiniButton	{
		private static final long serialVersionUID = 1L;
		private RemoveItemButton()	{
			super("BUTTON_BAR_REMOVE");
			setLabelKey();
			setName(REMOVE_NAME);
			addActionListener(new RemoveItemAction());
		}
	}
	// ==========================================
	// Mini Buttons
	private class toggleItemAction implements ItemListener	{
		@Override public void itemStateChanged(ItemEvent evt)	{
			// System.out.println("toggleItemAction " + evt.getStateChange() + " / Updating = " + updating);
			if (updating)
				return;
			MiniToggle b = (MiniToggle) evt.getSource();
			int idx = buttonList.indexOf(b);
			int prevIdx = labelList.getSelectedIndex();
			String newLabel = labelList.setSelectedIndex(idx);
			String prevLabel = labelList.setSelectedIndex(prevIdx);
			if (pendingEvent == null) {
				if (b.isSelected()) { // Toggled On
					if (prevIdx == idx)
						return;
					else {
						MiniToggle prevB = buttonList.get(prevIdx);
						// update the list Index
						labelList.setSelectedIndex(idx);
						// Change the state of the previously selected button
						updating = true;
						prevB.setSelected(false);
						updating = false;
					}
				}
				else if (prevIdx == idx) {
					b.setSelected(true); // Can not unselect the last
					// System.out.println("Can not unselect the last");
					misClick();
				}
			}
			else
				labelList.setSelectedIndex(pendingEvent.newLabel);

			callEvent (new BarEvent(BarEvents.BUTTON_SELECTED, newLabel, prevLabel, idx));
		}
	}
	private class MiniToggle extends RMiniToggleButton	{
		private static final long serialVersionUID = 1L;
		private MiniToggle(String txt)	{
			super(txt);
			setName(barName);
			addItemListener(new toggleItemAction());
		}
		@Override public Color buttonTextColor()	{ return getTextColor(getText()); }
	}
}
