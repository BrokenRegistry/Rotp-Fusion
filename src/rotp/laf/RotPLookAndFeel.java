package rotp.laf;

import javax.swing.UIDefaults;
import javax.swing.plaf.metal.MetalLookAndFeel;

import rotp.ui.components.RotPToolTipUI;

public class RotPLookAndFeel extends MetalLookAndFeel {
	private static final long serialVersionUID = 1L;

	@Override public String getName()			{ return "RotPLookAndFeel"; }
	@Override public String getID()				{ return "RotPLookAndFeel"; }
	@Override public String getDescription()	{ return "A custom LookAndFeel that changes ToolTip"; }
	@Override public boolean isNativeLookAndFeel()		{ return false; }
	@Override public boolean isSupportedLookAndFeel()	{ return true; }
	@Override protected void initClassDefaults(UIDefaults table) {
		super.initClassDefaults(table);
		// Step 3: Register the custom ButtonUI class for JButton
//		table.put("ButtonUI", RotPButtonUI.class.getName());
//		table.put("TextFieldUI", RotPTextFieldUI.class.getName());
		table.put("ToolTipUI", RotPToolTipUI.class.getName());
	}
	@Override protected void initComponentDefaults(UIDefaults table) {
		super.initComponentDefaults(table);
		// Set custom colors for JLabel
//		table.put("Label.foreground", new Color(0, 68, 204));
	}
}
