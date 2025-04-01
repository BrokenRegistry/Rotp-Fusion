package rotp.model.game;

import static rotp.model.game.DefaultValues.DEF_VAL;
import static rotp.model.game.IGalaxyOptions.GalaxyOption.instance;
import static rotp.model.game.IGameOptions.DIFFICULTY_NORMAL;
import static rotp.model.game.IGameOptions.getGameDifficultyOptions;
import static rotp.ui.util.IParam.langLabel;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import rotp.Rotp;
import rotp.ui.RotPUI;
import rotp.ui.game.BaseModPanel;
import rotp.ui.util.GlobalCROptions;
import rotp.ui.util.LinkData;
import rotp.ui.util.LinkValue;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;
import rotp.ui.util.ParamString;
import rotp.ui.util.SpecificCROption;

public interface IGalaxyOptions extends IBaseOptsTools {
	String SIZE_DYNAMIC		= "SETUP_GALAXY_SIZE_DYNAMIC";
	String SIZE_MICRO		= "SETUP_GALAXY_SIZE_MICRO";
	String SIZE_TINY		= "SETUP_GALAXY_SIZE_TINY";
	String SIZE_SMALL		= "SETUP_GALAXY_SIZE_SMALL";
	String SIZE_SMALL2		= "SETUP_GALAXY_SIZE_SMALL2";
	String SIZE_MEDIUM		= "SETUP_GALAXY_SIZE_AVERAGE";
	String SIZE_MEDIUM2		= "SETUP_GALAXY_SIZE_AVERAGE2";
	String SIZE_LARGE		= "SETUP_GALAXY_SIZE_LARGE";
	String SIZE_LARGE2		= "SETUP_GALAXY_SIZE_LARGE2";
	String SIZE_HUGE		= "SETUP_GALAXY_SIZE_HUGE";
	String SIZE_HUGE2		= "SETUP_GALAXY_SIZE_HUGE2";
	String SIZE_MASSIVE		= "SETUP_GALAXY_SIZE_MASSIVE";
	String SIZE_MASSIVE2	= "SETUP_GALAXY_SIZE_MASSIVE2";
	String SIZE_MASSIVE3	= "SETUP_GALAXY_SIZE_MASSIVE3";
	String SIZE_MASSIVE4	= "SETUP_GALAXY_SIZE_MASSIVE4";
	String SIZE_MASSIVE5	= "SETUP_GALAXY_SIZE_MASSIVE5";
	String SIZE_INSANE		= "SETUP_GALAXY_SIZE_INSANE";
	String SIZE_LUDICROUS	= "SETUP_GALAXY_SIZE_LUDICROUS";
	String SIZE_MAXIMUM		= "SETUP_GALAXY_SIZE_MAXIMUM";

	String SHAPE_RECTANGLE	= "SETUP_GALAXY_SHAPE_RECTANGLE";
	String SHAPE_ELLIPTICAL	= "SETUP_GALAXY_SHAPE_ELLIPSE";
	String SHAPE_SPIRAL		= "SETUP_GALAXY_SHAPE_SPIRAL";
	// modnar: add new map shapes
	String SHAPE_TEXT		= "SETUP_GALAXY_SHAPE_TEXT";
	String SHAPE_LORENZ		= "SETUP_GALAXY_SHAPE_LORENZ";
	String SHAPE_FRACTAL	= "SETUP_GALAXY_SHAPE_FRACTAL";
	String SHAPE_MAZE		= "SETUP_GALAXY_SHAPE_MAZE";
	String SHAPE_SHURIKEN	= "SETUP_GALAXY_SHAPE_SHURIKEN";
	String SHAPE_BULLSEYE	= "SETUP_GALAXY_SHAPE_BULLSEYE";
	String SHAPE_GRID		= "SETUP_GALAXY_SHAPE_GRID";
	String SHAPE_CLUSTER	= "SETUP_GALAXY_SHAPE_CLUSTER";
	String SHAPE_SWIRLCLUSTERS = "SETUP_GALAXY_SHAPE_SWIRLCLUSTERS";
	String SHAPE_SPIRALARMS	= "SETUP_GALAXY_SHAPE_SPIRALARMS";
	// BR: add new map shapes
	String SHAPE_BITMAP		= "SETUP_GALAXY_SHAPE_BITMAP";
	String SHAPE_RANDOM		= "SETUP_GALAXY_SHAPE_RANDOM";
	String SHAPE_RANDOM_2	= "SETUP_GALAXY_SHAPE_RANDOM_2";

	// ==================== Galaxy Menu addition ====================
	//
	default int		selectedGalaxyRandSource()			{ return instance().galaxyRandSource.get(); }
	default boolean	selectedShowNewRaces()				{ return instance().showNewRaces.get(); }
	default String	selectedUseGlobalCROptions()		{ return instance().globalCROptions.get(); }
	default boolean	selectedUseSelectableAbilities()	{ return instance().useSelectableAbilities.get(); }
	default String	selectedGalaxyShapeOption3()		{ return instance().shapeOption3.get(); }
	default int		selectedGalaxyTextOption4()			{ return instance().shapeOption4.get(); }
	default int		selectedShapeLineSpacing()			{ return instance().shapeLineSpacing.get(); }
	default String	selectedShapeSelection()			{ return instance().shapeSelection.get(); }
	default	String	getGalaxyKey(int size)				{ return instance().getGalaxyKey(size); }

	default ParamBoolean	previewNebula()				{ return instance().previewNebula; }
	default ParamBoolean	showNewRaces()				{ return instance().showNewRaces; }
	default List<String>	galaxyShapeOptions()		{ return instance().getGalaxyShapeOptions(); }
	default GlobalCROptions	globalCROptions()			{ return instance().globalCROptions; }
	default ParamInteger	galaxyRandSource()			{ return instance().galaxyRandSource; }
	default ParamBoolean	useSelectableAbilities()	{ return instance().useSelectableAbilities; }
	default ParamInteger	shapeLineSpacing()			{ return instance().shapeLineSpacing; }
	default ParamInteger	shapeOption4()				{ return instance().shapeOption4; }
	default ParamString		shapeOption3()				{ return instance().shapeOption3; }
	default ParamList		shapeOption2()				{ return instance().shapeOption2; }
	default ParamList		shapeOption1()				{ return instance().shapeOption1; }
	default ParamList		shapeSelection()			{ return instance().shapeSelection; }
	default ParamList		sizeSelection()				{ return instance().sizeSelection; }
	default ParamList		difficultySelection()		{ return instance().difficultySelection; }
	default ParamInteger	aliensNumber()				{ return instance().aliensNumber; }
	default ParamString 	bitmapGalaxyLastFolder()	{ return instance().bitmapGalaxyLastFolder; }

	default LinkedHashMap<String, Integer> galaxySizeMap(boolean dynamic, IGameOptions opts) {
		return instance().sizeMap(dynamic, opts);
	}
	default int numberStarSystems(String size, IGameOptions opts) {
		return instance().getNumberStarSystems(size, opts);
	}
	static ParamList	getSizeSelection()			{ return instance().sizeSelection; }
	static ParamList	getDifficultySelection()	{ return instance().difficultySelection; }
	static ParamInteger	getAliensNumber()			{ return instance().aliensNumber; }
	static ParamInteger	getGalaxyRandSource()		{ return instance().galaxyRandSource; }
	static ParamInteger	getGalaxyShapeOption4()		{ return instance().shapeOption4; }
	static ParamInteger	getGalaxyShapeLineSpacing()	{ return instance().shapeLineSpacing; }

	class GalaxyOption {
		private static GalaxyOption instance;

		public static final GalaxyOption instance() {
			if (instance == null)
				instance = new GalaxyOption();
			return instance;
		}
		private final LinkedHashMap<String, Integer> sizeMap(boolean dynamic, IGameOptions opts) {
			LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
			if (dynamic) 
				if (opts== null)
				    map.put(SIZE_DYNAMIC,	Rotp.maximumSystems);
				else {
					map.put(SIZE_DYNAMIC,
							1 + (opts.selectedDynStarsPerEmpire() // +1 for Orion
								* (opts.selectedNumberOpponents()+1))); // +1 for player);
				}
		    map.put(SIZE_MICRO,		24); // BR: added original moo small size
		    map.put(SIZE_TINY,		33);
		    if (DEF_VAL.isMoo1())
			    map.put(SIZE_SMALL,	48);
		    else
		    	map.put(SIZE_SMALL,	50);
		    map.put(SIZE_SMALL2,	70);
		    if (DEF_VAL.isMoo1())
		    	map.put(SIZE_MEDIUM, 108);
		    else
		    	map.put(SIZE_MEDIUM, 100);
		    map.put(SIZE_MEDIUM2,	150);
		    map.put(SIZE_LARGE,		225);
		    map.put(SIZE_LARGE2,	333);
		    map.put(SIZE_HUGE,		500);
		    map.put(SIZE_HUGE2,		700);
		    map.put(SIZE_MASSIVE,	1000);
		    map.put(SIZE_MASSIVE2,	1500);
		    map.put(SIZE_MASSIVE3,	2250);
		    map.put(SIZE_MASSIVE4,	3333);
		    map.put(SIZE_MASSIVE5,	5000);
		    map.put(SIZE_INSANE,	10000);
		    map.put(SIZE_LUDICROUS,	100000);
		    map.put(SIZE_MAXIMUM,	Rotp.maximumSystems);
			return map;
		}
		private final List<String> getGalaxySizeOptions(int minSize) {
			LinkedHashMap<String, Integer> map = sizeMap(true, null);
			int max = map.get(SIZE_MAXIMUM);
			List<String> list = new ArrayList<>();
			for (Entry<String, Integer> entry : map.entrySet()) {
				int size = entry.getValue();
				if (size <= max && size >= minSize)
					list.add(entry.getKey());
			}
			return list;
		}
		private final String getGalaxyKey(int size) {
			LinkedHashMap<String, Integer> map = sizeMap(false, null);
			for (Entry<String, Integer> entry : map.entrySet())
				if (size <= entry.getValue())
					return entry.getKey();
			return null;
		}
		private final int getNumberStarSystems(String size, IGameOptions opts) { return sizeMap(true, opts).get(size); }

		private final List<String> getGalaxyShapeOptions() {
			List<String> list = new ArrayList<>();
			list.add(SHAPE_RECTANGLE);
			list.add(SHAPE_ELLIPTICAL);
			list.add(SHAPE_SPIRAL);
			// modnar: add new map shapes
			list.add(SHAPE_TEXT);
			list.add(SHAPE_CLUSTER);
			list.add(SHAPE_SWIRLCLUSTERS);
			list.add(SHAPE_GRID);
			list.add(SHAPE_SPIRALARMS);
			list.add(SHAPE_MAZE);
			list.add(SHAPE_SHURIKEN);
			list.add(SHAPE_BULLSEYE);
			list.add(SHAPE_LORENZ);
			list.add(SHAPE_FRACTAL);
			// BR: add new map shapes
			list.add(SHAPE_BITMAP);
			list.add(SHAPE_RANDOM);
			list.add(SHAPE_RANDOM_2);
			return list;
		}	
		// ==================== Galaxy Menu addition ====================
		//
		public final ParamBoolean previewNebula = new ParamBoolean(MOD_UI, "PREVIEW_NEBULA", true);
		public final ParamInteger galaxyRandSource	= new GalaxyRandSource() ;
		private final class GalaxyRandSource extends ParamInteger {
			GalaxyRandSource() {
				super(MOD_UI, "GALAXY_RAND_SOURCE", 0);
				setLimits(0, Integer.MAX_VALUE);
				setIncrements(1, 100, 10000);
				loop(true);
			}
			@Override public Integer	set(Integer value)	{
				super.set(value);
				if (RotPUI.instance() != null)
					RotPUI.setupGalaxyUI().postSelectionMedium(false);
				return value;
			}
		}
		public final ParamBoolean showNewRaces 			= new ShowNewRaces();
		private final class ShowNewRaces extends ParamBoolean {
			ShowNewRaces() {
				super(MOD_UI, "SHOW_NEW_RACES", false);
			}
			@Override public void	setFromCfgValue(String value)	{
				super.setFromCfgValue(value);
				if (RotPUI.instance() != null) {
					RotPUI.setupGalaxyUI().initOpponentGuide();
					RotPUI.setupGalaxyUI().postSelectionLight(true);
				}
			}
		}
		public final GlobalCROptions globalCROptions 	= new GlobalCROptions (BASE_UI, "OPP_CR_OPTIONS",
				SpecificCROption.BASE_RACE.value);
		public final ParamBoolean useSelectableAbilities	= new ParamBoolean(BASE_UI, "SELECT_CR_OPTIONS", false);
		public final ParamInteger shapeLineSpacing   		= new ShapeLineSpacing();
		private final class ShapeLineSpacing extends ParamInteger {
			ShapeLineSpacing() {
				super(BASE_UI, "SHAPE_LINE_SPACING", 5);
				setLimits(-100, 100);
				setIncrements(1, 5, 20);
				loop(true);
			}
			@Override public Integer	setFromIndex(int value)	{
				super.setFromIndex(value);
				if (RotPUI.instance() != null && RotPUI.setupGalaxyUI().isShapeTextGalaxy()) {
					RotPUI.setupGalaxyUI().updateGalaxyText();
					RotPUI.setupGalaxyUI().postSelectionMedium(true);
				}
				return get();
			}
			@Override public Integer	set(Integer value)	{
				super.set(value);
				if (RotPUI.instance() != null && RotPUI.setupGalaxyUI().isShapeTextGalaxy()) {
					RotPUI.setupGalaxyUI().updateGalaxyText();
					RotPUI.setupGalaxyUI().postSelectionMedium(true);
				}
				return get();
			}
			@Override public String getGuiDisplay(int idx)	{
				if (RotPUI.setupGalaxyUI().isShapeTextGalaxy())
					return super.getGuiDisplay(idx);
				return "---";
			}
			@Override public String getGuiDisplay()	{
				if (RotPUI.setupGalaxyUI().isShapeTextGalaxy())
					return super.getGuiDisplay();
				return "---";
			}
		}

		public final ParamInteger shapeOption4   		= new ShapeOption4();
		private final class ShapeOption4 extends ParamInteger {
			ShapeOption4() {
				super(BASE_UI, "SHAPE_OPTION_4", 2);
				setLimits(2, 10);
				setIncrements(1, 2, 5);
				loop(true);
			}
			@Override public Integer	setFromIndex(int value)	{
				super.setFromIndex(value);
				if (RotPUI.instance() != null) {
					RotPUI.setupGalaxyUI().updateGalaxyText();
					RotPUI.setupGalaxyUI().postSelectionMedium(true);
				}
				return get();
			}
			@Override public Integer	set(Integer value)	{
				super.set(value);
				if (RotPUI.instance() != null) {
					RotPUI.setupGalaxyUI().updateGalaxyText();
					RotPUI.setupGalaxyUI().postSelectionMedium(true);
				}
				return get();
			}
			@Override public String getGuiDisplay(int idx)	{
				if (RotPUI.setupGalaxyUI().isShapeTextGalaxy() && RotPUI.setupGalaxyUI().isShapeTextMulti())
					return super.getGuiDisplay(idx);
				return "---";
			}
			@Override public String getGuiDisplay()	{
				if (RotPUI.setupGalaxyUI().isShapeTextGalaxy() && RotPUI.setupGalaxyUI().isShapeTextMulti())
					return super.getGuiDisplay();
				return "---";
			}
		}
		public final ParamString  shapeOption3   		= new ShapeOption3();
		private final class ShapeOption3 extends ParamString {
			ShapeOption3() { super(BASE_UI, "SHAPE_OPTION_3", ""); }
			@Override public void initDependencies(int level)	{
				if (level == 0) {
					//resetLinks();
				}
				else {
					updated(true);
				}
			}
			@Override public boolean toggle(MouseEvent e, BaseModPanel frame)	{
				RotPUI.setupGalaxyUI().selectBitmapFromList();
				return false;
			}
			@Override public String getGuiDisplay(int idx)	{
				if (shapeSelection.get().equals(SHAPE_BITMAP))
					return super.getGuiDisplay(idx);
				return "---";
			}
		}
		private final ParamList	shapeOption2   		= new ShapeOption2(); // Duplicate Do not add the list
		private final class ShapeOption2 extends ParamList {
			ShapeOption2() {
				super(BASE_UI, "SHAPE_OPTION_2");
				showFullGuide(true);
			}
			@Override public void initDependencies(int level)	{
				if (level == 0) {
					//resetLinks();
				}
				else {
					updated(true);
				}
			}
			@Override public String	getOptionValue(IGameOptions options)	{
				return options.selectedGalaxyShapeOption2();
			}
			@Override public void setOptionValue(IGameOptions options, String newValue) {
				options.selectedGalaxyShapeOption2(newValue);
			}
			@Override public String	headerHelp(boolean sep) {
				if (listSize() == 0)
					return ("This shape do not have a secondary option<br>");
				return headerHelp(shapeSelection.get() + "_O2", sep); }
			@Override public String getLangLabel(int id) {
				if (id<0)
					return "";
				String label = super.getLangLabel(id);
				if (label != null && label.startsWith("SETUP_GALAXY_MAP_OPTION_")) {
					if (shapeOption1.get().endsWith("0"))
						label += "0";
					else
						label += "1";
				}
				return label;
			}
			@Override public String	setFromIndex(int value)	{
				super.setFromIndex(value);
				if (RotPUI.instance() != null)
					if (RotPUI.setupGalaxyUI().isShapeTextGalaxy()) {
						RotPUI.setupGalaxyUI().updateGalaxyText();
						RotPUI.setupGalaxyUI().postSelectionFull(true);
					}
					else
						RotPUI.setupGalaxyUI().postSelectionMedium(true);
				return get();
			}
			@Override public String	set(String value)	{
				super.set(value);
				if (RotPUI.instance() != null)
					if (RotPUI.setupGalaxyUI().isShapeTextGalaxy()) {
						RotPUI.setupGalaxyUI().updateGalaxyText();
						RotPUI.setupGalaxyUI().postSelectionFull(true);
					}
					else
						RotPUI.setupGalaxyUI().postSelectionMedium(true);
				return get();
			}
			@Override public String getGuiDisplay(int idx)	{
				if (listSize()==0)
					return "---";
				return super.getGuiDisplay(idx);
			}
		}
		private final ParamList	shapeOption1   		= new ShapeOption1(); // Duplicate Do not add the list
		private final class ShapeOption1 extends ParamList {
			ShapeOption1() {
				super(BASE_UI, "SHAPE_OPTION_1");
				showFullGuide(true);
			}
			@Override public void initDependencies(int level)	{
				if (level == 0) {
					//resetLinks();
				}
				else {
					updated(true);
				}
			}
			@Override public String	getOptionValue(IGameOptions options)	{
				return options.selectedGalaxyShapeOption1();
			}
			@Override public void setOptionValue(IGameOptions options, String newValue) {
				options.selectedGalaxyShapeOption1(newValue);
			}
			@Override public String	headerHelp(boolean sep) {
				if (listSize() == 0)
					return ("This shape do not have options<br>");
				return headerHelp(shapeSelection.get() + "_O1", sep); }
			@Override public String	setFromIndex(int value)	{
				super.setFromIndex(value);
				if (RotPUI.instance() != null)
					RotPUI.setupGalaxyUI().postSelectionMedium(true);
				return get();
			}
			@Override public String	set(String value)	{
				super.set(value);
				if (RotPUI.instance() != null)
					RotPUI.setupGalaxyUI().postSelectionMedium(true);
				return get();
			}
			@Override public boolean next()	{
				if (shapeSelection.get().equals(SHAPE_TEXT))
					RotPUI.setupGalaxyUI().nextMapOption1(true);
				else {
					super.next();
					RotPUI.setupGalaxyUI().postSelectionMedium(true);
				}
				return false;
			}
			@Override public boolean prev()	{
				if (shapeSelection.get().equals(SHAPE_TEXT))
					RotPUI.setupGalaxyUI().prevMapOption1(true);
				else {
					super.prev();
					RotPUI.setupGalaxyUI().postSelectionMedium(true);
				}
				return false;
			}
			@Override public boolean toggle(MouseEvent e, BaseModPanel frame)	{
				if (shapeSelection.get().equals(SHAPE_TEXT))
					RotPUI.setupGalaxyUI().selectGalaxyTextFromList();
				else
					super.toggle(e, frame);
				return false;
			}
			@Override public String getGuiDisplay(int idx)	{
				if (listSize()==0)
					return "---";
				if (idx == 1 && shapeSelection.get().equals(SHAPE_TEXT)) {
					return get();
				}
				return super.getGuiDisplay(idx);
			}
		}

		private final ParamList	shapeSelection			= new ShapeSelection(); // Duplicate Do not add the list
		private final class ShapeSelection extends ParamList {
			ShapeSelection() {
				super(BASE_UI, "GALAXY_SHAPE", getGalaxyShapeOptions(),  SHAPE_RECTANGLE);
				showFullGuide(false);
			}
			@Override public void initDependencies(int level)	{
				if (level == 0) {
					resetLinks();
					addLink(shapeOption1, DO_REFRESH);
					addLink(shapeOption2, DO_REFRESH);
					addLink(shapeOption3, DO_REFRESH);
				}
				else
					super.initDependencies(level);
			}
			@Override public String getOptionValue(IGameOptions options) {
				return options.selectedGalaxyShape();
			}
			@Override public void setOptionValue(IGameOptions options, String newValue) {
				options.selectedGalaxyShape(newValue);
			}
			@Override public String	setFromIndex(int value)	{
				super.setFromIndex(value);
				if (RotPUI.instance() != null)
					RotPUI.setupGalaxyUI().postSelectionFull(true);
				return get();
			}
			@Override public String	set(String value)	{
				super.set(value);
				if (RotPUI.instance() != null)
					RotPUI.setupGalaxyUI().postSelectionFull(true);
				return get();
			}
		}

		private final ParamList sizeSelection 			= new SizeSelection(); // Duplicate Do not add the list
		private final class SizeSelection extends ParamList {
			private static final int MIN_SIZE = 4;
			private boolean allowRefresh = true;
			SizeSelection() {
				super(BASE_UI, "GALAXY_SIZE", getGalaxySizeOptions(MIN_SIZE), SIZE_SMALL);
				setDefaultValue(MOO1_DEFAULT, SIZE_MICRO);
				showFullGuide(false);
			}
			@Override public void initDependencies(int level)	{
				if (level == 0) {
					resetLinks();
					addLink(aliensNumber, DO_REFRESH);
				}
				else {
					IGameOptions opts = opts();
					int minSize = Math.max(opts.selectedMinStarsPerEmpire()+1,
											opts.secondRingSystemNumber()+2);
					reInit(getGalaxySizeOptions(minSize));
					boolean invalid = isInvalidLocalValue(get());
					if (invalid) {
						invalid = isInvalidLocalValue(get());
						if (invalid) {
							set(SIZE_DYNAMIC);
						}
					}
					super.initDependencies(level);
				}
			}
			@Override public String getOptionValue(IGameOptions options) {
				return options.selectedGalaxySize();
			}
			@Override public void setOptionValue(IGameOptions options, String newValue) {
				options.selectedGalaxySize(newValue);
			}
			@Override public String name(int id) {
				String diffLbl = super.name(id);
				String label   = getLangLabel(id);
				int size = opts().numberStarSystems(label);
				if (label.equals("SETUP_GALAXY_SIZE_DYNAMIC"))
					diffLbl += " (now " + size + ")";
				else
					diffLbl += " (" + size + ")";
				return diffLbl;
			}
			@Override public String realHelp(int id) {
				String label   = getLangLabel(id);
				if (label.equals("SETUP_GALAXY_SIZE_DYNAMIC"))
					return super.realHelp(id);
				if (label.equals("SETUP_GALAXY_SIZE_MAXIMUM"))
					return super.realHelp(id);
				int size = opts().numberStarSystems(label);
				if (size < 101)
					return langLabel("SETUP_GALAXY_SIZE_MOO1_DESC");
				if (size < 1001)
					return langLabel("SETUP_GALAXY_SIZE_UP1000_DESC");
				return langLabel("SETUP_GALAXY_SIZE_OVER1000_DESC");
			}
			@Override public String	setFromIndex(int value)	{
				super.setFromIndex(value);
				if (RotPUI.instance() != null)
					RotPUI.setupGalaxyUI().postGalaxySizeSelection(true);
				return get();
			}
			@Override public String	set(String value)	{
				super.set(value);
				if (RotPUI.instance() != null)
					postGalaxySizeSelection();
				return get();
			}
			@Override public boolean isValidValue()	{ return !isInvalidLocalValue(get()); }
			@Override public boolean isInvalidLocalValue(LinkData rec)	{ return super.isInvalidLocalValue(rec); }
			@Override public boolean isInvalidLocalValue(String value)	{ return super.isInvalidLocalValue(value); }
			@Override protected boolean isInvalidLocalMax(String value)	{ return false; }
			@Override protected boolean isInvalidLocalMin(String value)	{ return false; }
			@Override protected boolean updateNeeded(LinkValue value, boolean up) {
				if (get().equalsIgnoreCase(SIZE_DYNAMIC))
					return false;
				String testValue = value.stringValue();
				int testSize	 = getNumberStarSystems(testValue, opts());
				int currentSize  = getNumberStarSystems(get(), opts());
				if (up)
					return testSize > currentSize;
				else
					return false;
			}
			@Override public boolean followValue(LinkData rec)	{
				// called by secondRingSystemNumber
				int size = rec.srcValue.intValue();
				boolean up = rec.aimUp;
				if (up) {
					int minSize = size+2;
					reInit(getGalaxySizeOptions(minSize));
					boolean updateNeeded = updateNeeded(rec.aimValue, up);
					//System.out.println("followValue size "+size+"->"+minSize+" updateNeeded="+updateNeeded);
					if (updateNeeded)
						set(rec.aimValue.stringValue());
					return true; // To allow standard follow
				}
				else { // update list, don't follow
					int minSize = size+2;
					reInit(getGalaxySizeOptions(minSize));
					// uncomment to follow
					// super.followValue(value, up);
				}
				return true;
			}
			private void postGalaxySizeSelection() {
				if (allowRefresh)
					RotPUI.setupGalaxyUI().postGalaxySizeSelection(false);
			}
		}

		private final ParamList difficultySelection	= new DifficultySelection(); // Duplicate Do not add the list
		private final class DifficultySelection extends ParamList {
			DifficultySelection() {
				super(BASE_UI, "GAME_DIFFICULTY", getGameDifficultyOptions(), DIFFICULTY_NORMAL);
				showFullGuide(true);
			}
			@Override public String getOptionValue(IGameOptions options) {
				return options.selectedGameDifficulty();
			}
			@Override public void setOptionValue(IGameOptions options, String newValue) {
				options.selectedGameDifficulty(newValue);
			}
			@Override public String name(int id) {
				String diffLbl = super.name(id);
				String label   = getLangLabel(id);
				if (label.equals("SETUP_DIFFICULTY_CUSTOM"))
					diffLbl += " (" + Integer.toString(opts().selectedCustomDifficulty()) + "%)";
				else {
					float modifier = opts().aiProductionModifier(label);
					diffLbl += " (" + Integer.toString(Math.round(100 * modifier)) + "%)";
				}
				return diffLbl;
			}
			@Override public String	setFromIndex(int value)	{
				super.setFromIndex(value);
				if (RotPUI.instance() != null)
					RotPUI.setupGalaxyUI().postSelectionLight(true);
				return get();
			}
			@Override public String	set(String value)	{
				super.set(value);
				if (RotPUI.instance() != null)
					RotPUI.setupGalaxyUI().postSelectionLight(false);
				return get();
			}
		}

		private final ParamInteger aliensNumber = new AliensNumber(); // Duplicate Do not add the list
		private final class AliensNumber extends ParamInteger {
			AliensNumber() {
				super(BASE_UI, "ALIENS_NUMBER", 1);
				setLimits(0, 49);
				setIncrements(1, 5, 20);
				isDuplicate(true);
			}
			@Override public void initDependencies(int level)	{
				if (level == 0) {
					//resetLinks();
				}
				else {
					updated(true);
				}
			}
			@Override public Integer getOptionValue(IGameOptions options) {
				maxValue(options.maximumOpponentsOptions());
				return options.selectedNumberOpponents();
			}
			@Override public void setOptionValue(IGameOptions options, Integer newValue) {
				options.selectedOpponentRace(newValue, null);
				options.selectedNumberOpponents(newValue);
			}
			@Override public Integer defaultValue() {
				return opts().defaultOpponentsOptions();
			}
			@Override public Integer set(Integer value)	{
				super.set(value);
				if (RotPUI.instance() != null)
					RotPUI.setupGalaxyUI().postSelectionMedium(true);
				return value;
			}
		}

		private final ParamString bitmapGalaxyLastFolder = new ParamString(BASE_UI, "BITMAP_LAST_FOLDER", Rotp.jarPath())
					.isCfgFile(true);
	}
}
