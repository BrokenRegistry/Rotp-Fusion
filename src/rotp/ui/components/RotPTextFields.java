package rotp.ui.components;

import static java.awt.GridBagConstraints.NONE;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import rotp.model.empires.species.SettingString;
import rotp.ui.BasePanel;
import rotp.ui.game.GameUI;
import rotp.ui.util.IParam;
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
//		@Override public String getToolTipText(MouseEvent e){ return null; }
		@Override public JComponent getComponent()			{ return this; }
		@Override protected void paintComponent(Graphics g)	{
			Graphics2D g2 = (Graphics2D) g;
			int w = getWidth();
			int h = getHeight();
			g2.setColor(GameUI.setupFrame());
			g2.fillRect(0, s2, w-1, h-1-s2-s2);
			if (showBorder) {
				g2.setStroke(BasePanel.stroke1);
				g2.setColor(highlightColor());
				g2.drawRect(0, s2, w-1, h-1-s2-s2);
			}
			super.paintComponent(g);
		}
		private class TextFieldMouseAdapter extends MouseAdapter	{
			@Override public void mouseEntered(MouseEvent evt)	{
				showBorder = true;
				setForeground(textFieldTextHLColor());
				if (!isEditable()) {
					popGuide();
//					String tip = getToolTipText();
//					if (tip!=null && !tip.isEmpty())
//						popGuide(tip);
				}
			}
			@Override public void mouseExited(MouseEvent evt)	{
				showBorder = false;
				setForeground(textFieldTextColor());
				hideGuide();
			}
			@Override public void mousePressed(MouseEvent evt)	{}
			@Override public void mouseReleased(MouseEvent evt)	{}
		}
	}

	public static class RFieldAndLabel extends RTextField	{
		private static final long serialVersionUID = 1L;
		protected RLabel rotPLabel;
		protected IParam<String> param;

		public RFieldAndLabel(String label, String txt, int colomns)	{
			super(txt, colomns); // Create RotPTextField
			rotPLabel = new RLabel(label);
		}
		public RFieldAndLabel(Container pane, String label, String text, int colomns, int x, int y)	{
			this(label, text, colomns);
			pane.add(rotPLabel, newGbc(x,y, 1,1, 0,0, 6, NONE, ZERO_INSETS, 0,0));
			pane.add(this, newGbc(x+1,y, 1,1, 0,0, 4, NONE, ZERO_INSETS, 0,0));
		}
		public void setToolTipText(String labelTT, String fieldTT)	{
			if (labelTT != null && !labelTT.isEmpty())
				rotPLabel.setToolTipText(labelTT);
			if (fieldTT != null && !fieldTT.isEmpty())
				setToolTipText(fieldTT);
		}
		public void setParam(IParam<String> param)		{
			this.param = param;
			rotPLabel.setParam(param);
		}
		@Override public IParam<?> getParam()		{ return param; }
		@Override public JComponent getComponent()	{ return rotPLabel; }
	}
	public static class SettingField extends RFieldAndLabel	{
		private static final long serialVersionUID = 1L;
//		private final SettingString param;
		private int currentId;

		public SettingField(SettingString setting, int colomns)	{
			super(setting.getLabel(), setting.settingValue(), colomns);
			setParam(setting);
//			this.param = setting;
			init();
		}
		public SettingField(Container pane, SettingString setting, int colomns, int x, int y)	{
			super(pane, setting.getLabel(), setting.settingValue(), colomns, x, y);
			setParam(setting);
//			this.param = setting;
			init();
		}
		public SettingField(Container pane, SettingString setting, int colomns, int x, int y, String lang, int itemId)	{
			super(pane, setting.getLabel(lang), setting.settingValue(itemId), colomns, x, y);
			setParam(setting);
//			this.param = setting;
			String tooltips = setting.htmlTooltips();
			setToolTipText(tooltips, tooltips);
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
				SettingField field = (SettingField) evt.getSource(); // should be this
				String text = field.getText();
				param.selectedValue(currentId, text);
			}
		}
		private void textChangedAction()	{
			if (param!= null)
				param.selectedValue(currentId, getText());
			else
				System.out.println("Failed textChangedAction(): " + getText() + " Id = " + currentId); // TODO BR: REMOVE
		}
	}
}
