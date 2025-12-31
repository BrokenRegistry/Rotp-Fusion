package rotp.ui.components;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import rotp.ui.game.BaseModPanel;

public class RDialog extends JDialog implements RotPComponents {
	private static final long serialVersionUID = 1L;

	protected BaseModPanel parent;
	protected Frame frame;
	protected int titleFontSize	= 30;

	protected RDialog(BaseModPanel parent)	{
		this.parent = parent;
		if (parent != null)
			frame = JOptionPane.getFrameForComponent(parent.getParent());
	}
	
	protected void addVariableSpace(Container pane, int x, int y)	{
		GridBagConstraints gbc = newGbc(x,y, 1,1, 1.0,1.0, CENTER, BOTH, ZERO_INSETS, 0,0);
		pane.add(new RLabel(""), gbc);
	}
	// ========================================================================
	// List toggled by buttons
	//
//	public class RotPCardPane extends JPanel {
//		frame = JOptionPane.getFrameForComponent(frameComp.getParent());
//	}
	public class refreshAction implements ActionListener	{
		@Override public void actionPerformed(ActionEvent evt)	{
			refresh();
		}
	}
	protected void refresh() {
		pack();
	}


	// ========================================================================
	// Book panel, will include several pages
	//
	
}
