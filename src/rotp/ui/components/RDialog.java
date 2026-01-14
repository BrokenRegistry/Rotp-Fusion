package rotp.ui.components;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import rotp.ui.BasePanel;

public class RDialog extends JDialog implements RotPComponents {
	private static final long serialVersionUID = 1L;

	protected final BasePanel parent;
	protected final Frame frame;
	protected int titleFontSize	= 30;

	protected RDialog(BasePanel parent2)	{
		this.parent = parent2;
		if (parent2 != null)
			frame = JOptionPane.getFrameForComponent(parent2.getParent());
		else frame = null;
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
