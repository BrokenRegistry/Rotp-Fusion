package rotp.ui.util.swing;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSpinnerUI;

//Inspired by https://coderanch.com/t/522480/java/change-jspinner-arrows-left

class RotpJSpinnerUI extends BasicSpinnerUI {
	private int arrowSize = 16;
	RotpJSpinnerUI(int iconSize) { arrowSize = iconSize; }
	RotpJSpinnerUI() {}
	public static ComponentUI createUI(JComponent c) { // BR: Unused, but who knows?
		return new RotpJSpinnerUI();
	}
	private Component createArrowButton(int direction) {
		RotpJSpinnerButton b = new RotpJSpinnerButton(direction, arrowSize);
		b.setInheritsPopupMenu(true);
		return b;
	}
	@Override protected Component createNextButton() {
		Component c = createArrowButton(SwingConstants.EAST);
		c.setName("Spinner.nextButton");
		installNextButtonListeners(c);
		return c;
	}
	@Override protected Component createPreviousButton() {
		Component c = createArrowButton(SwingConstants.WEST);
		c.setName("Spinner.previousButton");
		installPreviousButtonListeners(c);
		return c;
	}
	@Override public void installUI(JComponent c) {
		super.installUI(c);
		spinner.removeAll();
		spinner.setLayout(new RotpSpinnerBorderLayout());
		spinner.add(createNextButton(), BorderLayout.EAST);
		spinner.add(createPreviousButton(), BorderLayout.WEST);
//		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "#");
//		spinner.add(editor, BorderLayout.CENTER);
		spinner.add(createEditor(), BorderLayout.CENTER);
	}
}
class RotpSpinnerBorderLayout extends BorderLayout {
	private static final long serialVersionUID = 1L;
	@Override public void addLayoutComponent(Component comp, Object constraints) {
		if (constraints.equals("Editor"))
			constraints = CENTER;
		super.addLayoutComponent(comp, constraints);
	}
}
