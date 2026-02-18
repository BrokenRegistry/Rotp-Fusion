package rotp.model.game;

import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;

public interface IMapOptions extends IBaseOptsTools {

	// ========================================================================
	// Galaxy Map options
	// Parameters not available in specific option panel.

	ParamInteger squareGridSize	= new ParamInteger( MOD_UI, "SQUARE_GRID_SIZE", 5)
			.setLimits(5, 100)
			.setIncrements(5, 10, 20);
	default int	squareGridSize() { return squareGridSize.get(); }

	ParamBoolean showSquareGrid	= new ParamBoolean(MOD_UI, "SHOW_SQUARE_GRID", false);
	default void toggleSquareGridDisplay()		{ showSquareGrid.toggle();}
	default boolean showSquareGrid()			{ return showSquareGrid.get();}

	ParamBoolean showGridCircular	= new ParamBoolean(MOD_UI, "SHOW_GRID_CIRCULAR", false);
	default void toggleGridCircularDisplay()	{ showGridCircular.toggle();}
	default boolean showGridCircular()			{ return showGridCircular.get();}

	ParamList flightPathDisplay	= new ParamList (MOD_UI, "FLIGHT_PATH_DISPLAY", "Important")
			.showFullGuide(true)
			.put("All",		  MOD_UI + "SHOW_ALL_FLIGHTPATHS")
			.put("Important", MOD_UI + "SHOW_IMPORTANT_FLIGHTPATHS")
			.put("None",	  MOD_UI + "SHOW_NO_FLIGHTPATHS");
	default void toggleFlightPathDisplay(boolean reverse)	{ flightPathDisplay.toggle(reverse);}
	default boolean showFleetsOnly()			{ return flightPathDisplay.get().equals("None"); }
	default boolean showImportantFlightPaths() 	{ return !flightPathDisplay.get().equals("None"); }
	default boolean showAllFlightPaths()		{ return flightPathDisplay.get().equals("All"); }

	ParamList systemNameDisplay	= new ParamList (MOD_UI, "SYSTEM_NAME_DISPLAY", "Data")
			.showFullGuide(true)
			.put("Hide",	MOD_UI + "HIDE_SYSTEM_NAME")
			.put("Name",	MOD_UI + "SHOW_SYSTEM_NAME")
			.put("Data",	MOD_UI + "SHOW_SYSTEM_DATA");
	default void toggleSystemNameDisplay(boolean reverse)	{ systemNameDisplay.toggle(reverse);}
	default boolean hideSystemNames()		{ return systemNameDisplay.get().equals("Hide"); }
	default boolean showSystemNames()		{ return systemNameDisplay.get().equals("Name"); }
	default boolean showSystemData() 		{ return systemNameDisplay.get().equals("Data"); }

	ParamList shipDisplay	= new ParamList (MOD_UI, "SHIP_DISPLAY", "All")
			.showFullGuide(true)
			.put("All",		 MOD_UI + "SHOW_ALL_SHIPS")
			.put("NoUnarmed", MOD_UI + "SHOW_NO_UNARMED_SHIPS")
			.put("Armed",	 MOD_UI + "SHOW_ONLY_ARMED_SHIPS");
	default void resetShipDisplay()					{ shipDisplay.set("All");}
	default void toggleShipDisplay(boolean reverse)	{ shipDisplay.toggle(reverse);}
	default boolean showFriendlyTransports()		{ return !shipDisplay.get().equals("Armed"); }
	default boolean showUnarmedShips()				{ return shipDisplay.get().equals("All"); }

	ParamList showShipRanges	= new ParamList (MOD_UI, "SHOW_SHIP_RANGES", "SR")
			.showFullGuide(true)
			.put("R",	MOD_UI + "SHOW_RANGES")
			.put("SR",	MOD_UI + "SHOW_STARS_AND_RANGES")
			.put("S",	MOD_UI + "SHOW_STARS")
			.put("-",	MOD_UI + "SHOW_NO_STARS_AND_RANGES");
	default void toggleShipRangesDisplay(boolean reverse)	{ showShipRanges.toggle(reverse);}
	default boolean showShipRanges()	{
		return (showShipRanges.get().equals("R"))
				|| (showShipRanges.get().equals("SR"));
	}
	default boolean showStars()	{
		return (showShipRanges.get().equals("S"))
				|| (showShipRanges.get().equals("SR"));
	}

	ParamInteger defaultMaxBases	= new ParamInteger( GAME_UI, "DEFAULT_MAX_BASES", 0)
			.setDefaultValue(ROTP_DEFAULT, 1)
			.setLimits(0, 5000)
			.setIncrements(1, 5, 20);
	default int	defaultMaxBases() { return defaultMaxBases.get(); }

	ParamBoolean divertExcessToResearch	= new DivertExcessToResearch();
	default boolean	divertColonyExcessToResearch()				{ return divertExcessToResearch.get(); }
	default boolean	toggleDivertColonyExcessToResearch()		{
		divertExcessToResearch.next();
		return divertExcessToResearch.get();
	}
	final class DivertExcessToResearch extends ParamBoolean {
		DivertExcessToResearch() { super(GAME_UI, "DIVERT_EXCESS_TO_RESEARCH", true); }
		@Override public Boolean set(Boolean b)	{
			Boolean val = super.set(b);
			if (GameSession.instance().isReady())
				if (IGameOptions.reserveFromRich.get())
					GameSession.instance().galaxy().player().redoGovTurnDecisionsRich();
			return val;
		}
	}

	ParamBoolean displayYear	= new ParamBoolean( // Duplicate Do not add the list
			GAME_UI, "DISPLAY_YEAR", false);
	default boolean displayYear()		{ return displayYear.get(); }
	default void toggleYearDisplay()	{ displayYear.toggle(); }
}
