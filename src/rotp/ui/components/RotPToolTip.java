package rotp.ui.components;

import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.plaf.ToolTipUI;

public class RotPToolTip extends JToolTip {
	private static final long serialVersionUID = 1L;
//	String tipText;

	@Override public void updateUI() { setUI((ToolTipUI)UIManager.getUI(this)); }
}
