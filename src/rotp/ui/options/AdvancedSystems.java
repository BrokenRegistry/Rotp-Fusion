package rotp.ui.options;

import java.util.Arrays;

import rotp.model.galaxy.AllShapes;
import rotp.model.galaxy.GalaxyShape;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamTitle;

public final class AdvancedSystems extends AbstractOptionsSubUI {
	static final String OPTION_ID = ADVANCED_SYSTEMS_UI_KEY;
	
	@Override public String optionId()			{ return OPTION_ID; }

	@Override public SafeListPanel optionsMap()	{ return advancedSystemMap(); }

	public static SafeListPanel advancedSystemMap()	{
		SafeListPanel map = new SafeListPanel(OPTION_ID);

		GalaxyShape shape = AllShapes.getShape();
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("HOMEWORLD_NEIGHBORHOOD"),
				firstRingSystemNumber,
				firstRingHabitable,
				firstRingRadius,
				secondRingSystemNumber,
				secondRingHabitable,
				secondRingRadius,

				HEADER_SPACER_100,
				new ParamTitle("GAME_OTHER"),
				looseNeighborhood,
				orionToEmpireModifier,
				empiresSpreadingFactor,
				galaxyRandSource,

				HEADER_SPACER_100,
				new ParamTitle("LINKED_OPTIONS"),
				starDensity,
				sizeSelection,
				dynStarsPerEmpire,
				aliensNumber
				)));
		map.add(new SafeListParam(Arrays.asList(
				new ParamTitle("GALAXY_PREVIEW"),
				galaxyPreviewColorStarsSize,
				galaxyPreviewAI,
				galaxyPreviewPlayer,
				galaxyPreviewOrion,
				
				HEADER_SPACER_50,
				new ParamTitle("NEBULAE_OPTION"),
				nebulae,
				nebulaPlacing,
				realNebulaSize,
				realNebulaShape,
				previewNebula,

				HEADER_SPACER_50,
				new ParamTitle("GALAXY_SHAPE"),
				shapeSelection,
				shape.paramOption1(),
				shape.paramOption2(),
				shape.paramOption3(),
				shape.paramOption4()
//				shapeOption1,
//				shapeOption2,
//				shapeOption3,
//				shapeOption4,
//				shapeLineSpacing
				)));
		return map;
	}
}
