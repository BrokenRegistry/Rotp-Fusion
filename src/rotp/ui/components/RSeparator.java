package rotp.ui.components;

import static java.awt.GridBagConstraints.REMAINDER;

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;

import rotp.ui.game.GameUI;

public class RSeparator extends RLabel	{
	private static final long serialVersionUID = 1L;
	protected static int separatorWidth	= s2;
	protected static int separatorGap	= s2;

	public RSeparator(boolean horizontal, int lineWidth, String text)	{
		super(text);
		if (horizontal)
			setBorder(BorderFactory.createMatteBorder(0, 0, lineWidth, 0, separatorColor()));
		else
			setBorder(BorderFactory.createMatteBorder(0, lineWidth, 0, 0, separatorColor()));
		setBackground(separatorColor());
	}
	public RSeparator(Container pane, boolean horizontal, int x, int y)	{
		this(pane, horizontal, separatorWidth, null, x, y, separatorGap);
	}
	public RSeparator(Container pane, boolean horizontal, int x, int y, int gap)	{
		this(pane, horizontal, separatorWidth, null, x, y, gap);
	}
	public RSeparator(Container pane, boolean horizontal, int lineWidth, String text, int x, int y)	{
		this(pane, horizontal, lineWidth, text, x, y, separatorGap);
	}
	public RSeparator(Container pane, boolean horizontal, int lineWidth, String text, int x, int y, int gap)	{
		this(horizontal, lineWidth, text);
		GridBagConstraints gbc;
		if (horizontal)
			gbc = newGbc(x,y, REMAINDER,1, 0,0, 2, GridBagConstraints.HORIZONTAL, new Insets(gap, 0, gap, 0), 0,0);
		else
			gbc = newGbc(x,y, 1,REMAINDER, 0,0, 6, GridBagConstraints.VERTICAL, new Insets(0, gap, 0, gap), 0,0);
		pane.add(this, gbc);
	}
	public RSeparator(Container pane, boolean horizontal, String text, int x, int y, int gap)	{
		super(text);
		GridBagConstraints gbc;
		if (horizontal)
			gbc = newGbc(x,y, REMAINDER,1, 0,0, 2, GridBagConstraints.HORIZONTAL, new Insets(gap, 0, gap, 0), 0,0);
		else
			gbc = newGbc(x,y, 1,REMAINDER, 0,0, 6, GridBagConstraints.VERTICAL, new Insets(0, gap, 0, gap), 0,0);
		pane.add(this, gbc);
	}

	protected Color separatorColor()	{ return GameUI.raceEdgeColor(); }
}
