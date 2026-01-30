package rotp.ui.util;

import static rotp.ui.game.BaseModPanel.dialGuide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import rotp.ui.components.RotPButtons;
import rotp.ui.components.RotPButtons.RButton;
import rotp.ui.components.RotPComponents;
import rotp.ui.components.RotPTextFields.RTextField;
import rotp.ui.game.BaseModPanel;
import rotp.ui.game.GameUI;
import rotp.util.LabelManager;
import rotp.util.ModifierKeysState;

public class StringDialogUI extends JDialog implements RotPComponents, ActionListener {
	private static final long serialVersionUID = 1L;

	private final int s5	= scaled(5);
	private final int s10	= scaled(10);

	private String initialValue;
	private String value = null;
	private Frame frame;
	private BaseModPanel baseModPanel;
	private IParam<?> param;
	private RButton cancelButton, setButton;
	private JPanel listPane, buttonPane;
	private JTextPane requestText;
	private RTextField input;

	public StringDialogUI(JFrame frame)	{
		super(frame, true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(500, 100));

		//Create and initialize the buttons.
		String cancel = LabelManager.current().label("BUTTON_TEXT_CANCEL");
		cancelButton = RotPButtons.newButton(cancel);
		cancelButton.setVerticalAlignment(SwingConstants.TOP);
		cancelButton.addActionListener(this);

		String set = LabelManager.current().label("BUTTON_TEXT_SET");
		setButton = RotPButtons.newButton(set);
		setButton.setVerticalAlignment(SwingConstants.TOP);
		setButton.addActionListener(this);

		input = new RTextField();
		input.addMouseListener(new DialMouseAdapter(setButton));
		input.addActionListener(this);

		//Create a container so that we can add a title around
		//the scroll pane.  Can't add a title directly to the
		//scroll pane because its background would be white.
		//Lay out the label and scroll pane from top to bottom.
		requestText = new JTextPane();
		requestText.setOpaque(true);
		requestText.setFont(narrowFont(14));
		requestText.setForeground(Color.BLACK);
		requestText.setBackground(GameUI.borderMidColor());
		requestText.setContentType("text/html");
		requestText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		requestText.setEditable(false);

		listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
		listPane.setBackground(GameUI.borderMidColor());
		listPane.add(requestText);
		listPane.add(Box.createRigidArea(new Dimension(0, s5)));
		listPane.add(input);
		listPane.setBorder(BorderFactory.createEmptyBorder(s10, s10, s10, s10));

		//Lay out the buttons from left to right.
		buttonPane = new JPanel();
		buttonPane.setFont(narrowFont(14));
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(s10, s10, s10, s10));
		buttonPane.setBackground(GameUI.borderMidColor());
		buttonPane.setForeground(Color.WHITE);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createRigidArea(new Dimension(s10, s10)));
		buttonPane.add(setButton);
		buttonPane.add(Box.createRigidArea(new Dimension(s10, s10)));

		//Put everything together, using the content pane's BorderLayout.
		Container contentPane = getContentPane();
		contentPane.setBackground(GameUI.borderMidColor());
		contentPane.add(listPane, BorderLayout.NORTH);
		contentPane.add(buttonPane, BorderLayout.PAGE_END);
	}

	public void init(BaseModPanel frameComp,
			Component locationComp,
			String labelText,
			String title,
			String initialValue,
			String longValue,
			int x, int y,
			int width, int height,
			Font listFont,
			InterfacePreview panel,
			IParam<?> param)
	{
		setTitle(title);
		baseModPanel		= frameComp;
		frame				= JOptionPane.getFrameForComponent(locationComp);
		this.param 			= param;
		this.initialValue	= initialValue;
		dialGuide			= BaseModPanel.showGuide(); // Always reinitialize.

		if (listFont == null)
			input.setFont(narrowFont(14));
		else
			input.setFont(listFont);

		if (width<=0)
			width = scaled(300);
		if (height<=0)
			height = scaled(25);
		input.setPreferredSize(new Dimension(width, height));
		input.setSize(width, height);
		setMinimumSize(new Dimension(width, height+scaled(100)));

		//Initialize values.
		requestText.setText(labelText);
		requestText.setEditable(false);
		input.setText(initialValue);
		pack();

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
		if (dialGuide && param != null) // For Help
			showHelp();
	}
	public String showDialog(int refreshLevel)	{ // Can only be called once.
		setValue(null);
		input.requestFocusInWindow();
		setVisible(true);
		ModifierKeysState.reset();
		return value;
	}
	private void setValue(String newValue)	{ value = newValue; }
	@Override public void dispose()	{
		clearHelp();
		dialGuide = false;
		super.dispose();
		ModifierKeysState.reset();
		frame.repaint();
	}
	//Handle clicks on the Set and Cancel buttons.
	@Override public void actionPerformed(ActionEvent e)	{
		if (e.getSource().equals(input)) {
			setValue(input.getText());
			dispose();
			return;
		}
		String cmd = e.getActionCommand();
		if ("Guide".equals(cmd)) {
			dialGuide = !dialGuide;
			if (dialGuide) {
				showHelp();
			}
			else {
				clearHelp();
				Graphics g = frame.getGraphics();
				frame.paintComponents(g);
				g.dispose();
			}
			return;
		}		
		String set = LabelManager.current().label("BUTTON_TEXT_SET");
		if (set.equals(cmd)) {
			setValue(input.getText());
			dispose();
			return;
		}
		String cancel = LabelManager.current().label("BUTTON_TEXT_CANCEL");
		if (cancel.equals(cmd)) {
			setValue(initialValue);
			dispose();
			return;
		}
	}
	private void clearHelp()	{ if (baseModPanel != null) baseModPanel.guidePopUp.clear(); }
	private void showHelp()		{
		if (baseModPanel == null)
			return;
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
			text = param.getGuide();
		baseModPanel.guidePopUp.setDest(dest, text);
	}

	@Override public String getName()	{ return input.getText(); }

	private class DialMouseAdapter extends MouseAdapter	{
		private final JButton setButton;
		private DialMouseAdapter (JButton button)	{ setButton = button; }
		@Override public void mouseClicked(MouseEvent e)	{
			if (e.getClickCount() == 2) {
				setButton.doClick(); //emulate button click
			}
		}
		@Override public void mouseEntered(MouseEvent evt)	{
			if (showGuide())
				popGuide();
		}
		@Override public void mouseExited(MouseEvent evt)	{
			hideGuide();
		}
	}

	@Override public JComponent getComponent()	{ return (JComponent) getContentPane(); }
	@Override public IParam<?> getParam()		{ return param; }
}
