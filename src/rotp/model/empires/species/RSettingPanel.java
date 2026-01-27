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
import rotp.ui.game.GameUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.util.IParam;

public class RSettingPanel extends JPanel implements RotPComponents {
	private static final long serialVersionUID = 1L;

	private static final Color OPTION_COLOR		= SystemPanel.blackText;	// Unselected option Color
	private static final Color SELECT_COLOR		= SystemPanel.whiteText;	// Selected option color
	private static final Color POSITIVE_COLOR	= SystemPanel.limeText;
	private static final Color NEGATIVE_COLOR	= SystemPanel.redText;
	private static final Color SETTING_COLOR	= SystemPanel.whiteText;
	private static final Color BLAND_COLOR		= GameUI.borderBrightColor();
	private static final Color LINE_COLOR		= Color.BLACK;

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
			border	= new RLineBorder(LINE_COLOR, false);
		else
			border	= new RLineBorder(LINE_COLOR, true);
		int titleJustif	= TitledBorder.LEFT;
		int titplePos	= TitledBorder.TOP;
		titleBorder		= BorderFactory.createTitledBorder(border, "", titleJustif, titplePos, settingLabelFont());
		highLighted		= false;
		setBorder(titleBorder);
		setName(setting.getCfgLabel());

		addMouseListener(settingListener);
		addMouseMotionListener(settingListener);
		addMouseWheelListener(settingListener);

		updateGui(true);
//		updateDisplay(true);
	}

	@Override public IParam<?> getParam()		{ return setting; }
	@Override public JComponent getComponent()	{ return this; }
	@Override public void paintImmediately()	{ paintImmediately(0, 0, getWidth(), getHeight()); }

	private Color getCostColor(float cost)	{
		if (cost == 0) 
			return SETTING_COLOR;
		else if (cost > 0)
			return POSITIVE_COLOR;
		else
			return NEGATIVE_COLOR;	
	}
	private Color getCostColor()	{
		if (setting.hasNoCost())
			return BLAND_COLOR;

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
	private void lightUpdateDisplay()	{
		setting.updated(false);
		if (highLighted) {
			border.setLineColor(highlightColor());
			border.setThickness(2);
		}
		else {
			border.setLineColor(LINE_COLOR);
			border.setThickness(1);
		}
		setLabelColor(getCostColor());
		setLabelText(setting.guiSettingDisplayStr());
		repaint();
	}
	void highLighted(boolean b)	{
		highLighted = b;
		lightUpdateDisplay();
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
			repaint();
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
			repaint();
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
	public void updateGui(boolean forced)	{
		if (!forced) {
			if (setting.updated())
				lightUpdateDisplay();
			return;
		}
		int paramId	= setting.index();
		int bulletStart	= setting.bulletStart();
		int bulletSize	= setting.bulletBoxSize();

		setLabelColor(getCostColor());
		setLabelText(setting.guiSettingDisplayStr());
		if (bulletSize == 0) {
			paintImmediately();
			return;
		}
		if (setting.isSettingString()) {
			setValueString(setting.guideSelectedValue(), OPTION_COLOR);
			paintImmediately();
			return;
		}
		if (bulletSize == 1) {
			setValueString(setting.guideSelectedValue(), OPTION_COLOR);
			paintImmediately();
			return;
		}
		boolean refresh = false;
		for (int bulletIdx=0; bulletIdx < bulletSize; bulletIdx++) {
			int optionIdx = bulletStart + bulletIdx;
			boolean disabled = optionIdx == paramId;
			Color color = disabled? OPTION_COLOR : SELECT_COLOR;
			String text = setting.guiCostOptionStr(optionIdx);
			refresh |= setOptionString(text, bulletIdx, color, refresh && ((bulletIdx+1) >= bulletSize));
		}
		paintImmediately();
	}
}
