package rotp.ui.components;

import static java.awt.GridBagConstraints.NONE;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolTipUI;

import rotp.model.empires.species.SettingString;
import rotp.ui.BasePanel;
import rotp.ui.game.GameUI;
import rotp.util.FontManager;

public class RotPTextFields { //extends RotPComponents {

	public static class RTextField extends JTextField implements RotPComponents	{
		private static final long serialVersionUID = 1L;
		private static final EmptyBorder border = new EmptyBorder(s4, s4, 0, s4);
		protected int textFieldFontSize	= 12;
		protected Font textFieldFont	= FontManager.current().narrowFont(textFieldFontSize);
		protected Color textFieldTextHLColor()	{ return Color.BLACK; }
		protected Color textFieldTextColor()	{ return Color.DARK_GRAY; }

		private boolean showBorder;

		public RTextField(String text, int columns)	{
			super(text, columns);
			setBackground(GameUI.setupFrame());
			setForeground(textFieldTextColor());
			setFont(textFieldFont);
			setBorder(border);
			setOpaque(false);
			addMouseListener(new TextFieldMouseAdapter());
		}
		public RTextField(Container pane, String text, int columns, GridBagConstraints gbc)	{
			this(text, columns);
			pane.add(this, gbc);
		}
		@Override protected void paintComponent(Graphics g)	{
			Graphics2D g2 = (Graphics2D) g;
			Rectangle bounds = getBounds();
			g2.setColor(GameUI.setupFrame());
			g2.fillRect(0, s2, bounds.width-1, bounds.height-1-s2-s2);
			if (showBorder) {
				g2.setStroke(BasePanel.stroke1);
				g2.setColor(highlightColor());
				g2.drawRect(0, s2, bounds.width-1, bounds.height-1-s2-s2);
			}
			super.paintComponent(g);
		}
		private class TextFieldMouseAdapter extends MouseAdapter	{
			@Override public void mouseEntered(MouseEvent evt)	{
				showBorder = true;
				setForeground(textFieldTextHLColor());
			}
			@Override public void mouseExited(MouseEvent evt)	{
				showBorder = false;
				setForeground(textFieldTextColor());
			}
			@Override public void mousePressed(MouseEvent evt)	{}
			@Override public void mouseReleased(MouseEvent evt)	{}
		}
	}

	public static class RFieldAndLabel extends RTextField	{
		private static final long serialVersionUID = 1L;
		protected RLabel rotPLabel;

		public RFieldAndLabel(String label, String txt, int colomns)	{
			super(txt, colomns); // Create RotPTextField
			rotPLabel = new RLabel(label);
		}
		public RFieldAndLabel(Container pane, String label, String text, int colomns, int x, int y)	{
			this(label, text, colomns);
			pane.add(rotPLabel, newGbc(x,y, 1,1, 0,0, 6, NONE, ZERO_INSETS, 0,0));
			pane.add(this, newGbc(x+1,y, 1,1, 0,0, 4, NONE, ZERO_INSETS, 0,0));
		}
	}

	public static class SettingField extends RFieldAndLabel	{
		private static final long serialVersionUID = 1L;
		private final SettingString setting;
		private int currentId;

		public SettingField(SettingString setting, int colomns)	{
			super(setting.getLabel(), setting.settingValue(), colomns);
			this.setting = setting;
			init();
		}
		public SettingField(Container pane, SettingString setting, int colomns, int x, int y)	{
			super(pane, setting.getLabel(), setting.settingValue(), colomns, x, y);
			this.setting = setting;
			init();
		}
		public SettingField(Container pane, SettingString setting, int colomns, int x, int y, String lang, int itemId)	{
			super(pane, setting.getLabel(lang), setting.settingValue(itemId), colomns, x, y);
			this.setting = setting;
			String tooltips = setting.htmlTooltips();
			if (!tooltips.isEmpty()) {
				setToolTipText(tooltips);
				rotPLabel.setToolTipText(tooltips);
			}
			currentId = itemId;
			init();
		}
		private void init()	{
			addActionListener(new TextFieldAction());
			setUI(ui);
			addChangeListener(this, e -> textChangedAction());
		}
		private class TextFieldAction implements ActionListener	{
			@Override public void actionPerformed(ActionEvent evt)	{
				//System.out.println("TextFieldAction " + evt.getActionCommand());
				SettingField field = (SettingField) evt.getSource(); // should be this
				//String actionCommand = evt.getActionCommand();
				//String label = field.setting.getLabel();
				String text = field.getText();
				//System.out.println("TextFieldAction Label = " + label + " Text = " + text + " Command = " + actionCommand);
				setting.selectedValue(currentId, text);
			}
		}
		private void textChangedAction()	{ setting.selectedValue(currentId, getText()); }
	}
	public static class RotPToolTipUI extends ToolTipUI {
		public static ComponentUI createUI(JComponent c)	{
			return new RotPToolTipUI();
		}
		RotPToolTipUI() {
			
		}
		@Override public void paint(Graphics g, JComponent c)	{
			super.paint(g, c);
		}
		@Override public void update(Graphics g, JComponent c)	{
			super.update(g, c);
		}
		@Override public Dimension getMinimumSize(JComponent c)	{
			return c.getLayout().minimumLayoutSize(c);
		}
		@Override public Dimension getPreferredSize(JComponent c)	{
			return c.getLayout().preferredLayoutSize(c);
		}
		@Override public Dimension getMaximumSize(JComponent c)	{
			return getPreferredSize(c);
		}
	}
}
