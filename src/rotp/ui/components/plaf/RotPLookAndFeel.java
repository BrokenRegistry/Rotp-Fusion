package rotp.ui.components.plaf;

import javax.swing.UIDefaults;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class RotPLookAndFeel extends NimbusLookAndFeel { // TODO BR: Try to customize Look and Feel
	private static final long serialVersionUID = 1L;

	@Override protected void initClassDefaults(UIDefaults table) {
		super.initClassDefaults(table);
		String rotPPackageName = "rotp.ui.components.plaf.";

		Object[] uiDefaults = {
			      "ButtonUI", rotPPackageName + "RotPButtonUI",
			        "MenuUI", rotPPackageName + "RotPMenuUI",
			 "ProgressBarUI", rotPPackageName + "RotPProgressBarUI",
			   "ScrollBarUI", rotPPackageName + "RotPScrollBarUI",
			   "SplitPaneUI", rotPPackageName + "RotPSplitPaneUI",
			  "TabbedPaneUI", rotPPackageName + "RotPTabbedPaneUI",
			 "TableHeaderUI", rotPPackageName + "RotPTableHeaderUI",
			"ToggleButtonUI", rotPPackageName + "RotPToggleButtonUI",
			     "ToolBarUI", rotPPackageName + "RotPToolBarUI",
		};
		table.putDefaults(uiDefaults);
	}
}
