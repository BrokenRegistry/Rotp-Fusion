package rotp.model.empires.species;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import rotp.model.empires.species.SpeciesSettings.Technologies.TechDiscovery;
import rotp.model.empires.species.SpeciesSettings.Technologies.TechResearch;
import rotp.ui.components.RLabel;
import rotp.ui.components.RLineBorder;
import rotp.ui.components.RotPComponents;
import rotp.ui.util.IParam;

public class RSettingPanel extends JPanel implements RotPComponents {
	private static final long serialVersionUID = 1L;
	private final RLineBorder border;
	private final TitledBorder titleBorder;
	private final int lineLength;
	private final List<RLabel> valueLabels = new ArrayList<>();
	private final MouseAdapter settingListener;
	final ICRSettings<?> setting;
	private boolean highLighted;

	RSettingPanel(Container host, ICRSettings<?> setting, int width, MouseAdapter listener)	{
		setOpaque(false);
		setAlignmentX(0);
		setAlignmentY(0);
		setFocusable(true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setting	= setting;
		this.lineLength	= width;
		settingListener	= listener;
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
		updateDisplay(true);
	}

	@Override public IParam<?> getParam()		{ return setting; }
	@Override public JComponent getComponent()	{ return this; }
	@Override public void paintImmediately()	{ paintImmediately(0, 0, getWidth(), getHeight()); }

	private Color getCostColor(float cost)	{
		if (cost == 0) 
			return ICRSettings.settingC;
		else if (cost > 0)
			return ICRSettings.settingPosC;
		else
			return ICRSettings.settingNegC;	
	}
	private Color getCostColor()	{
		if (setting.hasNoCost())
			return ICRSettings.settingBlandC;

		if (setting instanceof TechDiscovery) {
			TechDiscovery td = (TechDiscovery)setting;
			float cost = td.cost();
			return getCostColor(cost);
		}
		else if (setting instanceof TechResearch) {
			TechResearch tr = (TechResearch)setting;
			float cost = tr.cost();
			return getCostColor(cost);
		}
		else
			return getCostColor(setting.settingCost());
	}
	void updateDisplay(boolean forced)	{
		if (!forced && !setting.updated())
			return;
		setting.updated(false);
		if (highLighted) {
			border.setLineColor(highlightColor());
			border.setThickness(2);
		}
		else {
			border.setLineColor(Color.BLACK);
			border.setThickness(1);
		}
		setLabelColor(getCostColor());
		setLabelText(setting.guiSettingDisplayStr());
		repaint();
	}
	void highLighted(boolean b)	{
		highLighted = b;
		updateDisplay(true);
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
			valueLabel.setBorder(new EmptyBorder(0, s10, 0, s4));
			valueLabel.setForeground(color);
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
	public void setBullet(MouseEvent e)	{
		int y = e.getY();
		int bulletStart	= setting.bulletStart();
		int bulletSize	= setting.bulletBoxSize();
		for (int bulletIdx=0; bulletIdx < bulletSize; bulletIdx++) {
			int optionIdx = bulletStart + bulletIdx;
			RLabel label = valueLabels.get(optionIdx);
			int relY = y-label.getY();
			if (relY>=0 && relY<label.getHeight()) {
				SettingBase<?> sb = (SettingBase<?>)setting;
				sb.index(optionIdx);
			}
		}
	}
}
