package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

final class GovAspectOptions extends AbstractOptionsSubUI {
	static final String OPTION_ID = GOVERNOR_ASPECT_UI_KEY;
	static final String HEAD_ID	  = GOV_UI;

	@Override public String optionId()	{ return OPTION_ID; }
	@Override public String headId()	{ return HEAD_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(new SafeListParam(Arrays.asList(
				originalPanel,
				customSize,
				animatedImage
				)));
		map.add(new SafeListParam(Arrays.asList(
				brightnessPct,
				sizeFactorPct
				)));
		map.add(new SafeListParam(Arrays.asList(
				horizontalPosition,
				verticalPosition
				)));
		return map;
	};
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey(),
				Arrays.asList(
						originalPanel,
						customSize,
						animatedImage,
						brightnessPct,
						sizeFactorPct,
						horizontalPosition,
						verticalPosition
						));
		return minorList;
	}
}
