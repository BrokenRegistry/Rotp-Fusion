package rotp.ui.options;

import java.util.Arrays;

import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;

public final class GalaxyMenuOptions extends AbstractOptionsSubUI {
	static final 		String OPTION_ID = "GALAXY_SUBUI";
	public static final String GALAXY_ID = OPTION_ID;
	
	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);
		map.add(optionsGalaxy());
		return map;
	}
	static SafeListParam optionsGalaxy()	{
		return new SafeListParam(GALAXY_ID,
			Arrays.asList(
					showNewRaces,
					globalCROptions,
					useSelectableAbilities,
					galaxyRandSource,
					previewNebula,
					empiresSpreadingFactor,
					dynStarsPerEmpire // This one is a duplicate, but it helps readability
					));
	}
}
