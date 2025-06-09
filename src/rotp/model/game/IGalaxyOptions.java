package rotp.model.game;

import static rotp.model.game.DefaultValues.DEF_VAL;
import static rotp.model.game.IGameOptions.DIFFICULTY_NORMAL;
import static rotp.model.game.IGameOptions.getGameDifficultyOptions;
import static rotp.ui.util.IParam.langLabel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import rotp.Rotp;
import rotp.model.galaxy.AllShapes;
import rotp.ui.RotPUI;
import rotp.ui.game.SetupGalaxyUI;
import rotp.ui.util.GlobalCROptions;
import rotp.ui.util.IParam;
import rotp.ui.util.LinkData;
import rotp.ui.util.LinkValue;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;
import rotp.ui.util.ParamListMultiple;
import rotp.ui.util.ParamString;
import rotp.ui.util.SpecificCROption;
import rotp.util.Rand;

public interface IGalaxyOptions extends IBaseOptsTools {
	String SIZE_RANDOM		= "SETUP_GALAXY_SIZE_RANDOM";
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
	String SIZE_DEFAULT		= SIZE_SMALL;

	static LinkedHashMap<String, Integer> galaxySizeMap(boolean dynamic, IGameOptions opts) {
		LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
		if (dynamic)
			if (opts== null) {
				map.put(SIZE_RANDOM,	Rotp.maximumSystems);
				map.put(SIZE_DYNAMIC,	Rotp.maximumSystems);
			}
			else {
				Rand randRnd = new Rand(galaxyRandSource.get());
				int sysLim1 = randomNumStarsLim1.getValidValue();
				int sysLim2 = randomNumStarsLim2.getValidValue();
				int rndNum = randRnd.nextIntInclusive(sysLim1, sysLim2);
				map.put(SIZE_RANDOM, rndNum);
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
	static List<String> getGalaxySizeOptions(int minSize)	{
		LinkedHashMap<String, Integer> map = galaxySizeMap(true, null);
		int max = map.get(SIZE_MAXIMUM);
		List<String> list = new ArrayList<>();
		for (Entry<String, Integer> entry : map.entrySet()) {
			int size = entry.getValue();
			if (size <= max && size >= minSize)
				list.add(entry.getKey());
		}
		return list;
	}
	static int getNumberStarSystems(String size, IGameOptions opts)	{ return galaxySizeMap(true, opts).get(size); }
	static List<String> getGalaxyShapeOptions()	{ return AllShapes.getNames(); }
	interface IShapeOption extends IParam	{
		IParam getParam();
		default String	defaulttoString()	{ return getAsInt().toString(); }
		default String	gettoString()		{ return getAsInt().toString(); }
		default Integer	getAsInt()			{ return 0; }
		void setString(String str);
	}
	class ListShapeParam extends ArrayList<IShapeOption>	{
		private static final long serialVersionUID = 1L;
		private static final IShapeOption NONE = new ShapeOptionList("NONE");
		public ListShapeParam(Collection<IShapeOption> c)	{
			addAll(c);
		}
		public ListShapeParam()			{}
		@Override public IShapeOption get(int id)	{
			if (id<0 || size() == 0 || id >= size())
				return NONE;
			return super.get(id);
		}
		@Override public boolean add(IShapeOption so)	{
			if (so == null)
				return false;
			return super.add(so);
		}
		@Override public boolean addAll(Collection<? extends IShapeOption> c)	{
			if (c == null)
				return false;
			return super.addAll(c);
		}
	}
	class ShapeOptionInteger extends ParamInteger implements IShapeOption	{
		private ShapeOptionInteger(String name)	{
			super(BASE_UI, name + "_O" + 0, 0);
			isDuplicate(false);
		}
		public ShapeOptionInteger(String name, int option, Integer defaultValue)	{
			super(BASE_UI, name + "_O" + option, defaultValue);
			isDuplicate(false);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				//resetLinks();
			}
			else
				updated(true);
		}
		@Override public IParam getParam()			{ return this; }
		@Override public String defaulttoString()	{ return defaultValue().toString(); }
		@Override public String gettoString()		{
			Integer i = get();
			return i == null? "" : i.toString();
		}
		@Override public void setString(String str)	{
			Integer val = stringToInteger(str);
			if (val == null)
				val = defaultValue();
			set(val);
		}
		@Override public Integer set(Integer val)	{
			Integer str = super.set(val);
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionFull(false);
			return str;
		}
		@Override public Integer getAsInt()			{ return get(); }
	}
	class ShapeOptionString extends ParamString implements IShapeOption	{
		private ShapeOptionString(String name)	{
			super(BASE_UI, name + "_O" + 0, "");
			isDuplicate(false);
		}
		public ShapeOptionString(String name, int option, String defaultValue)	{
			super(BASE_UI, name + "_O" + option, defaultValue);
			isDuplicate(false);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				//resetLinks();
			}
			else
				updated(true);
		}
		@Override public IParam getParam()			{ return this; }
		@Override public String defaulttoString()	{ return defaultValue(); }
		@Override public String gettoString()		{
			String str = get();
			return str == null? "" : str;
		}
		@Override public void setString(String str)	{ set(str); }
		@Override public String set(String val)		{
			String str = super.set(val);
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionFull(false);
			return str;
		}
	}
	class ShapeOptionList extends ParamList implements IShapeOption	{
		private ShapeOptionList(String name)	{
			super(BASE_UI, name + "_O" + 0, new ArrayList<>(), "");
			isDuplicate(false);
			showFullGuide(true);
		}
		public ShapeOptionList(String name, int option, List<String> list, int defaultId)	{
			super(BASE_UI, name + "_O" + option, list, list.get(defaultId));
			isDuplicate(false);
			showFullGuide(true);
		}
		public ShapeOptionList(String name, int option, List<String> list, String defaultValue)	{
			super(BASE_UI, name + "_O" + option, list, defaultValue);
			isDuplicate(false);
			showFullGuide(true);
		}

		@Override public IParam getParam()			{ return this; }
		@Override public String defaulttoString()	{ return defaultValue(); }
		@Override public String gettoString()		{
			String str = get();
			return str == null? "" : str;
		}
		@Override public void setString(String str)	{ set(str); }
		@Override public String set(String val)		{
			String str = super.set(val);
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionFull(false);
			return str;
		}
	}
	class ShapeOptionListMult extends ParamListMultiple implements IShapeOption	{
		public ShapeOptionListMult(String name, int option, List<String> list, int defaultId)	{
			super(BASE_UI, name + "_O" + option, list, list.get(defaultId));
			showFullGuide(true);
		}

		@Override public IParam getParam()			{ return this; }
		@Override public String defaulttoString()	{ return defaultValue(); }
		@Override public String gettoString()		{ return get(); }
		@Override public void setString(String str)	{ set(str); }
		@Override public String set(String val)		{
			String str = super.set(val);
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionFull(false);
			return str;
		}
	}

	// ==================== Galaxy Menu addition ====================
	//
	default String getGalaxyKey(int size) {
		LinkedHashMap<String, Integer> map = galaxySizeMap(false, null);
		for (Entry<String, Integer> entry : map.entrySet())
			if (size <= entry.getValue())
				return entry.getKey();
		return null;
	}

	ParamBoolean previewNebula			= new ParamBoolean(MOD_UI, "PREVIEW_NEBULA", true);
	ParamInteger galaxyRandSource		= new GalaxyRandSource() ;
	final class GalaxyRandSource extends ParamInteger {
		GalaxyRandSource() {
			super(MOD_UI, "GALAXY_RAND_SOURCE", 0);
			setLimits(0, Integer.MAX_VALUE);
			setIncrements(1, 100, 10000);
			loop(true);
		}
		@Override public Integer	set(Integer value)	{
			super.set(value);
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionMedium(false);
			return value;
		}
	}
	ParamBoolean showNewRaces 			= new ShowNewRaces();
	final class ShowNewRaces extends ParamBoolean {
		ShowNewRaces() {
			super(MOD_UI, "SHOW_NEW_RACES", false);
		}
		@Override public void	setFromCfgValue(String value)	{
			super.setFromCfgValue(value);
			if (Rotp.initialized()) {
				RotPUI.setupGalaxyUI().initOpponentGuide();
				RotPUI.setupGalaxyUI().postSelectionLight(true);
			}
		}
	}
	GlobalCROptions globalCROptions		= new GlobalCROptions (BASE_UI, "OPP_CR_OPTIONS",
			SpecificCROption.BASE_RACE.value);
	default String globalCROptions()				{ return globalCROptions.get(); }
	default SpecificCROption globalCROptionsEnu()	{ return globalCROptions.getEnu(); }
	
	ParamBoolean useSelectableAbilities	= new ParamBoolean(BASE_UI, "SELECT_CR_OPTIONS", false);
	default boolean useSelectableAbilities()	{ return useSelectableAbilities.get(); }

	ParamList	shapeSelection			= new ShapeSelection();
	final class ShapeSelection extends ParamList {
		ShapeSelection() {
			super(BASE_UI, "GALAXY_SHAPE", getGalaxyShapeOptions(), AllShapes.getDefault());
			isDuplicate(true);
			showFullGuide(false);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0)
				resetLinks();
			else
				super.initDependencies(level);
		}
		@Override public String getOptionValue(IGameOptions options) {
			String val = options.selectedGalaxyShape();
			if (isInvalidLocalValue(val)) {
				val = AllShapes.replaceWithValid(val);
			}
			return val;
		}
		@Override public void setOptionValue(IGameOptions options, String newValue) {
			// System.out.println("shapeSelection setOptionValue " + newValue);
			options.selectedGalaxyShape(newValue);
			RotPUI.instance().refreshShapeOptions(null);
		}
		@Override public String	setFromIndex(int value)	{
			super.setFromIndex(value);
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionFull(true);
			return get();
		}
		@Override public String	set(String value)	{
			super.set(value);
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionFull(true);
			return get();
		}
	}

	ParamInteger randomNumStarsLim1		= new ParamInteger(BASE_UI, "RANDOM_NUM_STARS_LIM1", 50)
			.setLimits(10, Rotp.maximumSystems-1)
			.setIncrements(1, 5, 20);
	ParamInteger randomNumStarsLim2		= new ParamInteger(BASE_UI, "RANDOM_NUM_STARS_LIM2", 250)
			.setLimits(10, Rotp.maximumSystems-1)
			.setIncrements(1, 5, 20);
	ParamBoolean randomNumAliens		= new ParamBoolean(BASE_UI, "RANDOM_NUM_ALIENS", false);
	ParamInteger randomNumAliensLim1	= new ParamInteger(BASE_UI, "RANDOM_NUM_ALIENS_LIM1", 4)
			.setLimits(0, SetupGalaxyUI.MAX_DISPLAY_OPPS)
			.setIncrements(1, 5, 20);
	ParamInteger randomNumAliensLim2	= new ParamInteger (BASE_UI, "RANDOM_NUM_ALIENS_LIM2", 20)
			.setLimits(0, SetupGalaxyUI.MAX_DISPLAY_OPPS)
			.setIncrements(1, 5, 20);
	default int randomNumStarsLim1()	{ return randomNumStarsLim1.getValidValue(); }
	default int randomNumStarsLim2()	{ return randomNumStarsLim2.getValidValue(); }
	default int randomNumStarsMax()		{ return Math.max(randomNumStarsLim1(), randomNumStarsLim2()); }
	default int randomNumStarsMin()		{ return Math.min(randomNumStarsLim1(), randomNumStarsLim2()); }
	default int randomNumAliensLim1()	{ return randomNumAliensLim1.getValidValue(); }
	default int randomNumAliensLim2()	{ return randomNumAliensLim2.getValidValue(); }
	default int randomNumAliensMax()	{ return Math.max(randomNumAliensLim1(), randomNumAliensLim2()); }
	default int randomNumAliensMin()	{ return Math.min(randomNumAliensLim1(), randomNumAliensLim2()); }
	default boolean randomNumAliens()	{ return randomNumAliens.get(); }

	ParamList sizeSelection 			= new SizeSelection();
	final class SizeSelection extends ParamList {
		private static final int MIN_SIZE = 4;
		private boolean allowRefresh = true;
		SizeSelection() {
			super(BASE_UI, "GALAXY_SIZE", getGalaxySizeOptions(MIN_SIZE), SIZE_SMALL);
			isDuplicate(true);
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
			if (label.equals(SIZE_DYNAMIC))
				diffLbl += " " + langLabel("SETUP_GALAXY_SIZE_VALUE_DYN", ""+size);
			else
				diffLbl += " " + langLabel("SETUP_GALAXY_SIZE_VALUE", ""+size);
			return diffLbl;
		}
		@Override public String realHelp(int id) {
			String label   = getLangLabel(id);
			if (label.equals(SIZE_DYNAMIC))
				return super.realHelp(id);
			if (label.equals(SIZE_MAXIMUM))
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
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postGalaxySizeSelection(true);
			return get();
		}
		@Override public String	set(String value)	{
			super.set(value);
			if (Rotp.initialized())
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

	ParamList difficultySelection		= new DifficultySelection();
	final class DifficultySelection extends ParamList {
		DifficultySelection() {
			super(BASE_UI, "GAME_DIFFICULTY", getGameDifficultyOptions(), DIFFICULTY_NORMAL);
			isDuplicate(true);
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
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionLight(true);
			return get();
		}
		@Override public String	set(String value)	{
			super.set(value);
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionLight(false);
			return get();
		}
	}

	ParamInteger aliensNumber			= new AliensNumber();	
	final class AliensNumber extends ParamInteger {
		AliensNumber() {
			super(BASE_UI, "ALIENS_NUMBER", 1);
			setLimits(0, SetupGalaxyUI.MAX_DISPLAY_OPPS);
			setIncrements(1, 5, 20);
			isDuplicate(true);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				//resetLinks();
			}
			else
				updated(true);
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
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postSelectionMedium(true);
			return value;
		}
	}

	ParamString bitmapGalaxyLastFolder	= new ParamString(BASE_UI, "BITMAP_LAST_FOLDER", Rotp.jarPath())
				.isCfgFile(true);
}
