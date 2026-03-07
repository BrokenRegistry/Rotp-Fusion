package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;
import rotp.util.LanguageManager;

final class SettingMenuPreferences extends AbstractOptionsSubUI {
	static final String OPTION_ID = SETTING_MENU_PREF_UI_KEY;

	@Override public String optionId()			{ return OPTION_ID; }
	@Override public boolean isCfgFile()		{ return true; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("PANEL_CONTENTS"),
				useFusionFont,
				galaxyPreviewColorStarsSize,
				galaxyPreviewAI,
				galaxyPreviewPlayer,
				galaxyPreviewOrion,
				noFogOnIcons,
				showAlternateAnimation
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("PANEL_FORMAT"),
				minListSizePopUp,
				compactOptionOnly,
				loadSaveWidth,
				optionPanelAlignment,

				HEADER_SPACER_50,
				LanguageManager.changeLanguageFormat
				)));
		return map;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						optionPanelAlignment,
						LanguageManager.changeLanguageFormat
						));
		return minorList;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey(),
				Arrays.asList(
						galaxyPreviewColorStarsSize,
						minListSizePopUp,
						showAlternateAnimation,
						noFogOnIcons,
						compactOptionOnly,
						useFusionFont,
						loadSaveWidth
						));
		return majorList;
	}
}
