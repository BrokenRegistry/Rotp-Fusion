/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *	 notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *	 notice, this list of conditions and the following disclaimer in the
 *	 documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *	 contributors may be used to endorse or promote products derived
 *	 from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

package rotp.ui.util;

import static rotp.ui.game.BaseModPanel.dialGuide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

import rotp.ui.game.BaseModPanel;
import rotp.ui.game.GameUI;
import rotp.util.Base;
import rotp.util.LabelManager;
import rotp.util.ModifierKeysState;

/*
 * ListDialog.java is meant to be used by programs such as
 * ListDialogRunner.  It requires no additional files.
 */

/**
 * Use this modal dialog to let the user choose one string from a long
 * list.  See ListDialogRunner.java for an example of using ListDialog.
 * The basics:
 * <pre>
	String[] choices = {"A", "long", "array", "of", "strings"};
	String selectedName = ListDialog.showDialog(
								componentInControllingFrame,
								locatorComponent,
								"A description of the list:",
								"Dialog Title",
								choices,
								choices[0]);
 * </pre>
 */
public class ListDialogUI extends JDialog implements ActionListener, Base {
	private static final long serialVersionUID = 1L;
	private String initialValue;
	private String value = null;
	private int index    = -1;
	private int initialIndex;
	private int indexMax;
	private JList<Object> list;
	private List<String> alternateReturn;
	private Frame frame;
	private BaseModPanel baseModPanel;
	private IParam param;
	private JButton helpButton, cancelButton, setButton;
	private JScrollPane listScroller;
	private JPanel listPane, buttonPane;
	private JLabel label;
	private final int s5 = scaled(5);
	private final int s10 = scaled(10);
	private final int s15 = scaled(15);
	private final int topInset  = scaled(6);

	public ListDialogUI(JFrame frame)	{
		super(frame, true);
		int sideInset = s10;
		//Create and initialize the buttons.
		helpButton = new JButton("?");
		helpButton.setMargin(new Insets(topInset, sideInset, 0, sideInset));
		helpButton.setFont(narrowFont(15));
		helpButton.setVerticalAlignment(SwingConstants.TOP);
		helpButton.setBackground(GameUI.buttonBackgroundColor());
		helpButton.setForeground(GameUI.buttonTextColor());
		helpButton.setActionCommand("Guide");
		helpButton.addActionListener(this);
		
		sideInset = s15;
		String cancel = LabelManager.current().label("BUTTON_TEXT_CANCEL");
		cancelButton = new JButton(cancel);
		cancelButton.setMargin(new Insets(topInset, sideInset, 0, sideInset));
		cancelButton.setFont(narrowFont(15));
		cancelButton.setVerticalAlignment(SwingConstants.TOP);
		cancelButton.setBackground(GameUI.buttonBackgroundColor());
		cancelButton.setForeground(GameUI.buttonTextColor());
		cancelButton.addActionListener(this);

		String set = LabelManager.current().label("BUTTON_TEXT_SET");
		setButton = new JButton(set);
		setButton.setMargin(new Insets(topInset, sideInset, 0, sideInset));
		setButton.setFont(narrowFont(15));
		setButton.setVerticalAlignment(SwingConstants.TOP);
		setButton.setBackground(GameUI.buttonBackgroundColor());
		setButton.setForeground(GameUI.buttonTextColor());
		setButton.addActionListener(this);

		list = new DialJList();
		list.addMouseListener(new DialMouseAdapter(setButton));
		list.setVisibleRowCount(-1);
		list.setBackground(GameUI.setupFrame());
		list.setForeground(Color.BLACK);
		list.setSelectionBackground(GameUI.borderMidColor());
		list.setSelectionForeground(Color.WHITE);

		listScroller = new JScrollPane(list);
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		listScroller.getVerticalScrollBar().setBackground(GameUI.borderMidColor());
		listScroller.getVerticalScrollBar().setUI(new DarkScrollBarUI());
		listScroller.getHorizontalScrollBar().setBackground(GameUI.borderMidColor());
		listScroller.getHorizontalScrollBar().setUI(new DarkScrollBarUI());

		//Create a container so that we can add a title around
		//the scroll pane.  Can't add a title directly to the
		//scroll pane because its background would be white.
		//Lay out the label and scroll pane from top to bottom.
		label = new JLabel();
		label.setFont(narrowFont(15));
		label.setLabelFor(list);
		label.setForeground(Color.BLACK);

		listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.setBackground(GameUI.borderMidColor());
		listPane.add(label);
		listPane.add(Box.createRigidArea(new Dimension(0,s5)));
		listPane.add(listScroller);
		listPane.setBorder(BorderFactory.createEmptyBorder(s10,s10,s10,s10));

		//Lay out the buttons from left to right.
		buttonPane = new JPanel();
		buttonPane.setFont(narrowFont(15));
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(0, s10, s10, s10));
		buttonPane.setBackground(GameUI.borderMidColor());
		buttonPane.setForeground(Color.WHITE);

		//Lay out the buttons from left to right.
		buttonPane.add(helpButton);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createRigidArea(new Dimension(s10, 0)));
		buttonPane.add(setButton);

		//Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.add(listPane, BorderLayout.CENTER);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);

	}
	/**
	 * Set up the dialog.  The first Component argument
	 * determines which frame the dialog depends on; it should be
	 * a component in the dialog's controlling frame. The second
	 * Component argument should be null if you want the dialog
	 * to come up with its left corner in the center of the screen;
	 * otherwise, it should be the component on top of which the
	 * dialog should appear.
	 */	
	public void init(BaseModPanel frameComp,
			   Component locationComp,
			   String labelText,
			   String title,
			   Object[] data,
			   String initialValue,
			   String longValue,
			   boolean isVerticalWrap,
			   int x, int y,
			   int width, int height,
			   Font listFont,
			   InterfacePreview panel,
			   List<String> alternateReturn,
			   IParam param) {
		setTitle(title);
		baseModPanel		 = frameComp;
		frame				 = JOptionPane.getFrameForComponent(frameComp.getParent());
		this.alternateReturn = alternateReturn;
		this.param 			 = param;
		this.initialValue	 = initialValue;
		indexMax			 = data.length -1;
		dialGuide			 = BaseModPanel.showGuide(); // Always reinitialize.
		getRootPane().setDefaultButton(setButton);

		//main part of the dialog
		list.setListData(data);
		if (listFont == null)
			list.setFont(narrowFont(14));
		else {
			list.setFont(listFont);
			DefaultListCellRenderer renderer = (DefaultListCellRenderer) list.getCellRenderer();
			renderer.setHorizontalAlignment(SwingConstants.CENTER);
		}
		list.addListSelectionListener(new DialListSelectionListener(panel));
		//list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		if (longValue != null)
			list.setPrototypeCellValue(longValue); //get extra space
		if (isVerticalWrap)
			list.setLayoutOrientation(JList.VERTICAL_WRAP);
		else
			list.setLayoutOrientation(JList.HORIZONTAL_WRAP);

		if (width<=0)
			width = scaled(300);
		if (height<=0)
			height = scaled(150);
		listScroller.setPreferredSize(new Dimension(width, height));
		label.setText("<html>" + labelText + "</html>");

		//Initialize values.
		setValue(initialValue);
		pack();
		initialIndex = list.getSelectedIndex();

		setSize(width, height);
		setLocationRelativeTo(locationComp);
		if (x>0 || y>0) {
			Point location = getLocation();
			if (x>0)
				location.x = x;
			if (y>0)
				location.y = y;
			setLocation(location);
		}
		if (dialGuide && param != null)// For Help
			showHelp(index);
	}
	//public String getValue()		{ return value; }
	public String showDialog(int refreshLevel)	{ // Can only be called once.
		value = null;
		index = -1;
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setVisible(true);
		if (alternateReturn != null && index >= 0) {
			index = Math.max(0, index);
			value = alternateReturn.get(index);
		}
		ModifierKeysState.reset();
		baseModPanel.initButtonBackImg();
		baseModPanel.refreshGui(refreshLevel);
		return value;
	}
	public int showDialogGetId(int refreshLevel, int idx)	{ // Can only be called once.
		initialIndex = Math.min(Math.max(0, idx), indexMax);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
//		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setSelectedIndex(initialIndex);
		initialValue = (String) list.getSelectedValue();
		value = null;
		index = -1;
		setVisible(true);
		ModifierKeysState.reset();
		baseModPanel.initButtonBackImg();
		baseModPanel.refreshGui(refreshLevel);
		return index;
	}
	private void setValue(String newValue)	{
		value = newValue;
		list.setSelectedValue(value, true);
		index = Math.max(0, list.getSelectedIndex());
	}

	@Override public void dispose() {
		clearHelp();
		dialGuide = false;
		super.dispose();
		ModifierKeysState.reset();
		frame.repaint();
	}
	//Handle clicks on the Set and Cancel buttons.
	@Override public void actionPerformed(ActionEvent e) {
		if ("Guide".equals(e.getActionCommand())) {
			dialGuide = !dialGuide;
			if (dialGuide) {
				showHelp(list.getSelectedIndex());
			}
			else {
				clearHelp();
				frame.paintComponents(frame.getGraphics());
			}
			return;
		}		
        String set = LabelManager.current().label("BUTTON_TEXT_SET");
		if (set.equals(e.getActionCommand())) {
			index = list.getSelectedIndex();
			value = (String)(list.getSelectedValue());
			dispose();
			return;
		}
		String cancel = LabelManager.current().label("BUTTON_TEXT_CANCEL");
		if (cancel.equals(e.getActionCommand())) {
			index = initialIndex;
			value = initialValue;
			dispose();
			return;
		}
	}
	private void clearHelp() {
		baseModPanel.guidePopUp.clear();
	}
	private void showHelp(int idx) {
		Rectangle dest = getBounds();
		if (dest.x == 0)
			return;
		Point pt = frame.getLocationOnScreen();
		dest.x -= pt.x;
		dest.y -= pt.y;
		dest.y += scaled(80);
		clearHelp();
		String text	= "No Help Yet";
		if (param != null)
			text = param.getGuide(idx);
		baseModPanel.guidePopUp.setDest(dest, text, frame.getGraphics());
	}
	private class DialJList extends JList<Object> {
		//Subclass JList to workaround bug 4832765, which can cause the
		//scroll pane to not let the user easily scroll up to the beginning
		//of the list.  An alternative would be to set the unitIncrement
		//of the JScrollBar to a fixed value. You wouldn't get the nice
		//aligned scrolling, but it should work.
		private static final long serialVersionUID = 1L;
		private DialJList () { super(); }
		private DialJList (Object[] data ) {
			super(data);
		}
		@Override public int getScrollableUnitIncrement(Rectangle visibleRect,
											  int orientation,
											  int direction) {
			int row;
			if (orientation == SwingConstants.VERTICAL &&
				  direction < 0 && (row = getFirstVisibleIndex()) != -1) {
				Rectangle r = getCellBounds(row, row);
				if ((r.y == visibleRect.y) && (row != 0))  {
					Point loc = r.getLocation();
					loc.y--;
					int prevIndex = locationToIndex(loc);
					Rectangle prevR = getCellBounds(prevIndex, prevIndex);

					if (prevR == null || prevR.y >= r.y) {
						return 0;
					}
					return prevR.height;
				}
			}
			return super.getScrollableUnitIncrement(
							visibleRect, orientation, direction);
		}
	};
	private class DialListSelectionListener implements ListSelectionListener {
		private final InterfacePreview panel;
		private DialListSelectionListener(InterfacePreview panel) { this.panel = panel; }
		@Override public void valueChanged(ListSelectionEvent e) {
			value = (String) list.getSelectedValue();
			if (value != null) {
				index = Math.max(0, list.getSelectedIndex());
				if (panel != null) { // For Preview
					if (alternateReturn != null)
						value = alternateReturn.get(index);

					if (param != null)
						param.setFromCfgValue(value);

					panel.preview(value, param);
				}
				if (dialGuide && param != null) { // For Help
					showHelp(index);
				}
			}
		}		
	}
	private class DialMouseAdapter extends MouseAdapter {
		private final JButton setButton;
		private DialMouseAdapter (JButton button) { setButton = button; }
		@Override public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				setButton.doClick(); //emulate button click
			}
		}
	}
	private class DarkScrollBarUI extends BasicScrollBarUI {
		@Override protected void configureScrollBarColors() {
	        this.thumbColor = GameUI.borderDarkColor();
	    }
	}
}