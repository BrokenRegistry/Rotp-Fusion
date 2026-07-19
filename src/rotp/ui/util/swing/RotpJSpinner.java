package rotp.ui.util.swing;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;

public class RotpJSpinner extends JSpinner {
	private static final long serialVersionUID = 1L;
	protected RotpJSpinner() {
		//super();
		if (isNewFormat()) {
			setUI(new RotpJSpinnerUI(arrowSize()));
			//setUI(new LeftRightSpinnerUI());
//			JSpinner.NumberEditor editor = new JSpinner.NumberEditor(this, "#");
//			setEditor(editor);
		}
	}
//	@Override public void setLayout(LayoutManager mgr) {
//		if (isNewFormat())
//			super.setLayout(new LeftRightSpinnerBorderLayout());
//			//super.setLayout(new RotpJSpinnerLayout());
//		else
//			super.setLayout(mgr);
//	}

	protected boolean	isNewFormat()	{ return true; }
	protected int		arrowSize()		{ return 16; }
	protected Color		borderColor()	{ return Color.BLACK; }
	protected Color		valueBgColor()	{ return Color.WHITE; }
	protected Color		disabledColor()	{ return Color.GRAY; }
	protected Color		hiddenColor()	{ return Color.GRAY; }
	protected Color		hoverColor()	{ return Color.YELLOW; }
	protected Color		panelBgColor()	{ return Color.LIGHT_GRAY; }
	
	public void	centerText() {
		JComponent c = getEditor();
		if (c instanceof DefaultEditor) {
			DefaultEditor ed = (DefaultEditor)c;
			ed.getTextField().setHorizontalAlignment(JTextField.CENTER);
		}
	}
}