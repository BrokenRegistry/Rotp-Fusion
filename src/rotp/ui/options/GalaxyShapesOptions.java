package rotp.ui.options;

import rotp.model.galaxy.AllShapes;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

public final class GalaxyShapesOptions extends AbstractOptionsSubUI {
	static final 		String OPTION_ID = GALAXY_SHAPES_UI_KEY;
	
	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(AllShapes.optionList());
		return map;
	}
	@Override public SafeListParam majorList()	{
		SafeListParam majorList = new SafeListParam(uiMajorKey());
		return majorList;
	}
	@Override public SafeListParam minorList()	{
		SafeListParam minorList = new SafeListParam(uiMinorKey());
		return minorList;
	}
}
