package rotp.model.empires.species;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import rotp.ui.components.RLabel;
import rotp.ui.components.RLineBorder;
import rotp.ui.components.RotPComponents;

class RSettingPanel extends JPanel implements RotPComponents {
	private static final long serialVersionUID = 1L;
	private final RLineBorder border;
	private final TitledBorder titleBorder;
	private final int lineLength;
	private final List<RLabel> valueLabels = new ArrayList<>();
	private final MouseAdapter settingListener;
	final ICRSettings setting;
	private boolean highLighted;

	RSettingPanel(Container host, ICRSettings setting, int width, MouseAdapter listener)	{
		setOpaque(false);
		setAlignmentX(0);
		setAlignmentY(0);
		setFocusable(true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setting	= setting;
		this.lineLength	= width;
		settingListener	= listener;
		//setToolTipText(setting.htmlFullHelp());
//		setToolTipText(setting.getFullHelp());
//		setToolTipText(setting.getGuide());
		if (setting.isBullet())
			border	= new RLineBorder(Color.BLACK, false);
		else
			border	= new RLineBorder(Color.BLACK, true);
		int titleJustif	= TitledBorder.LEFT;
		int titplePos	= TitledBorder.TOP;
		titleBorder		= BorderFactory.createTitledBorder(border, "", titleJustif, titplePos, settingLabelFont());
		highLighted		= false;
		setBorder(titleBorder);
		setName(setting.getCfgLabel());

		addMouseListener(settingListener);
		addMouseMotionListener(settingListener);
		addMouseWheelListener(settingListener);

		setting.updateGui(this);
		updateDisplay();
	}
	void updateDisplay()	{
		if (highLighted) {
			border.setLineColor(highlightColor());
			border.setThickness(2);
		}
		else {
			border.setLineColor(Color.BLACK);
			border.setThickness(1);
		}
		setLabelColor(setting.getCostColor());
		setLabelText(setting.guiSettingDisplayStr());
		repaint();
	}
	void highLighted(boolean b)	{
		highLighted = b;
		updateDisplay();
	}
	void setValueString(String str, Color color)	{
		boolean refresh = false;
		List<String> lines	= wrappedLines(settingValueFont(), str, lineLength);
		for (int i=0; i<lines.size(); i++)
			refresh |= setOptionString(lines.get(i), i, color, refresh);
		if (lines.size() < valueLabels.size()) {
			refresh = true;
			for (int i=lines.size(); i<valueLabels.size(); i++)
				valueLabels.get(i).setText("");
		}
		if (refresh)
			revalidate();
	}
	boolean setOptionString(String str, int idx, Color color, boolean refresh)	{
		if (valueLabels.size() <= idx) {
			refresh = true;
			RLabel valueLabel = new RLabel(str);
			valueLabel.setAlignmentX(0);
			valueLabel.setBorder(emptyBorder());
			valueLabel.setForeground(color);
			//valueLabel.addMouseListener(settingListener);
			//valueLabel.setFocusable(true);
			valueLabels.add(valueLabel);
			add(valueLabel);
		}
		else {
			RLabel valueLabel = valueLabels.get(idx);
			valueLabel.setText(str);
			valueLabel.setForeground(color);
		}
		if (refresh)
			revalidate();
		return refresh;
	}
	public void setLabelText(String title)	{ titleBorder.setTitle(title); }
	public void setLabelColor(Color color)	{ titleBorder.setTitleColor(color); }
	private EmptyBorder emptyBorder()		{ return new EmptyBorder(0, s10, 0, s4); }
//	@Override public String generateToolTip(XYDataset arg0, int arg1, int arg2) {
//		return "";
//	}
//	@Override public JToolTip createToolTip() {
//		Point p = getMousePosition();
//		// Locate the renderer under the event location
//		System.out.println("Position = " + p);
//		BufferedImage eyeIcon = this.eyeIcon(scaled(300), scaled(300), Color.RED, true);
//		//return new ImageToolTip();
//		return super.createToolTip();
//		//keep default behaviour 
//		//JToolTip toReturn=super.createToolTip();
//		//toReturn.addComponentListener(customTooltipListener);
//		//return toReturn;
//	}
}
