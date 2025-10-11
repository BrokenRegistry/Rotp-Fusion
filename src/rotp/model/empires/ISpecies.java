package rotp.model.empires;

import static rotp.model.empires.CustomRaceDefinitions.BASE_RACE_MARKER;
import static rotp.model.empires.CustomRaceDefinitions.fileToAlienRace;
import static rotp.model.game.IGalaxyOptions.globalCROptions;
import static rotp.ui.util.IParam.labelFormat;
import static rotp.ui.util.IParam.realLangLabel;
import static rotp.ui.util.IParam.rowFormat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.model.game.RulesetManager;
import rotp.ui.RotPUI;
import rotp.ui.game.SetupGalaxyUI;
import rotp.ui.util.ParamList;
import rotp.ui.util.StringList;
import rotp.util.Base;
import rotp.util.LabelManager;

public interface ISpecies {
	String CUSTOM_SPECIES_FOLDER	= "CustomSpecies/";
	String INTRO_FILE_EXTENSION		= ".intro.txt";
	String CR_EMPIRE_NAME_RANDOM	= "Randomized";

	SpeciesManager R_M = new SpeciesManager();
	
	// Exclusions keys
	List<String> notTalking	= new ArrayList<>(Arrays.asList("Mouth"));
	List<String> closed		= new ArrayList<>(Arrays.asList("Open"));
	List<String> open		= new ArrayList<>(Arrays.asList("Closed"));
	List<String> notFiring	= new ArrayList<>(Arrays.asList("Firing"));

	class SpeciesList extends ArrayList<Species>	{
		private static final long serialVersionUID = 1L;

		public SpeciesList(int initialCapacity)	{ super(initialCapacity); }

		public Species add(String animKey, String skillKey, boolean isPlayer)	{
			Species species	= new Species(animKey, skillKey, isPlayer);
			boolean valid	= add(species);
			Race animSpecies	= R_M.keyed(animKey);
			Race skillSpecies	= R_M.keyed(skillKey);

			return species;
		}
		public Species add(String animKey, String skillKey, DynOptions skillOpts, boolean isPlayer)	{
			Species species	= new Species(animKey, skillKey, skillOpts, isPlayer);
			boolean valid	= add(species);
			Race animSpecies	= R_M.keyed(animKey);
			Race skillSpecies	= R_M.keyed(skillKey);

			return species;
		}
		@Override public boolean add(Species species)	{
			
			return super.add(species);
		}
		
	}
	class Species implements ISpecies, Serializable {
		private static final long serialVersionUID = 1L;

		Species(String animKey, String skillKey, boolean isPlayer)	{
			speciesAnimKey(skillKey);
			speciesSkillKey(skillKey);
			isPlayer(isPlayer);
		}
		Species(String animKey, String skillKey, DynOptions skillOpt, boolean isPlayer)	{
			speciesAnimKey	= skillKey;
			speciesSkillKey	= skillKey;
		}
		String	speciesAnimKey;		// Internal Species (jar description)
		String	speciesSkillKey;
		DynOptions skillOpts;

		boolean	isOriginalSkill;	// Same Animation and Skill
		boolean	isInternalSkill;	// (jar description) but can be different than Animations
		boolean	isScrambled;		// Internal Skill but scrambled
		boolean	isRandomized;
		boolean	isPlayer;
		
		SpeciesNames speciesNames = new SpeciesNames();
		
		Race getSpeciesAnim()	{ return R_M.keyed(speciesAnimKey); }
		Race getSpeciesSkill()	{ return R_M.keyed(speciesSkillKey, skillOpts); }

		String	speciesAnimKey()			{ return speciesAnimKey; };		// Internal Species (jar description)
		void	speciesAnimKey(String s)	{ speciesAnimKey = s; };		// Internal Species (jar description)
		String	speciesSkillKey()			{ return speciesSkillKey; };
		void	speciesSkillKey(String s)	{ speciesSkillKey = s; };
		DynOptions	skillOpts()				{ return skillOpts; };
		void	skillOpts(DynOptions s)		{ skillOpts = s; };

		boolean	isOriginalSkill()			{ return isOriginalSkill; };	// Same Animation and Skill
		void	isOriginalSkill(boolean b)	{ isOriginalSkill = b; };		// Same Animation and Skill
		boolean	isInternalSkill()			{ return isInternalSkill; };	// (jar description) but can be different than Animations
		void	isInternalSkill(boolean b)	{ isInternalSkill = b; };		// (jar description) but can be different than Animations
		boolean	isScrambled()				{ return isScrambled; };		// Internal Skill but scrambled
		void	isScrambled(boolean b)		{ isScrambled = b; };			// Internal Skill but scrambled
		boolean	isRandomized()				{ return isRandomized; };
		void	isRandomized(boolean b)		{ isRandomized = b; };
		boolean	isPlayer()					{ return isPlayer; };
		void	isPlayer(boolean b)			{ isPlayer = b; };

		
		SpeciesNames	speciesNames()			{ return speciesNames(); };		// Must be unique
		void	speciesNames(SpeciesNames s)	{ speciesNames(s); };			// Must be unique
		String	speciesName()			{ return speciesNames.speciesName(); };	// Must be unique
		void	speciesName(String s)	{ speciesNames.speciesName(s); };		// Must be unique
		String	leaderName()			{ return speciesNames.leaderName(); };	// Must be unique
		void	leaderName(String s)	{ speciesNames.leaderName(s); };		// Must be unique
		String	homeworld()				{ return speciesNames.homeworld(); };	// Must be unique
		void	homeworld(String s)		{ speciesNames.homeworld(s); };			// Must be unique
		String	title()					{ return speciesNames.title(); };
		void	title(String s)			{ speciesNames.title(s); };
		String	fullTitle()				{ return speciesNames.fullTitle(); };
		void	fullTitle(String s)		{ speciesNames.fullTitle(s); };

		String	descriptions(int i)				{ return speciesNames.descriptions(i); };
		void	descriptions(int i, String s)	{ speciesNames.descriptions(i, s); };
		StringList	shipNames(int i)			{ return speciesNames.shipNames(i); };
		void	shipNames(int i, StringList s)	{ speciesNames.shipNames(i, s); };
	}
	class SpeciesNames implements Serializable	{
		private static final long serialVersionUID = 1L;
		// Mandatory
		private String speciesName;
		private String homeworld;
		private String leaderName;
		// Optional
		private String[] descriptions;
		private String title;
		private String fullTitle;
		private StringList[] shipNames;

		boolean isComplete()	{
			return speciesName != null
					&& homeworld != null
					&& leaderName != null;
		}
		String	speciesName()			{ return speciesName; };	// Must be unique
		void	speciesName(String s)	{ speciesName = s; };		// Must be unique
		String	leaderName()			{ return leaderName; };		// Must be unique
		void	leaderName(String s)	{ leaderName = s; };		// Must be unique
		String	homeworld()				{ return homeworld; };		// Must be unique
		void	homeworld(String s)		{ homeworld = s; };			// Must be unique
		String	title()					{ return title; };
		void	title(String s)			{ title = s; };
		String	fullTitle()				{ return fullTitle; };
		void	fullTitle(String s)		{ fullTitle = s; };

		String	descriptions(int i)				{ return descriptions[i]; };
		void	descriptions(int i, String s)	{ descriptions[i] = s; };
		StringList	shipNames(int i)			{ return shipNames[i]; };
		void	shipNames(int i, StringList s)	{ shipNames[i] = s; };


	}
	class Utils {
		private	static final String LABEL_CONVERTER_KEY	= "ABILITIES_";

		static int currentIndex()			{ return SetupGalaxyUI.mouseBoxIndex()+1; }
		static String labelKey(String key)	{ return LABEL_CONVERTER_KEY + key.toUpperCase().replaceAll("'", "").replaceAll(" ", "_"); }
		static String convert(String key)	{
			String labelKey	= labelKey(key);
			String labelTxt	= LabelManager.current().label(labelKey);
			if (labelTxt.equals(labelKey))
				return key;
			return labelTxt;
		}
		static IGameOptions options()	{ return RulesetManager.current().currentOptions(); }
		static void postSelect(boolean click)	{ RotPUI.setupGalaxyUI().postSelectionLight(click); }
		
	}
	// For Guide, do not add to panels
	class ParamListGlobalAbilities extends ParamList { // For Guide
		public ParamListGlobalAbilities(String gui, String name, String defaultValue) {
			super(gui, name, null, defaultValue);
			isDuplicate(true); // To activate get and set optionValue
		}
		@Override public String getOptionValue(IGameOptions options) {
			return globalCROptions.get();
		}
		@Override public void setOptionValue(IGameOptions options, String str) {
			globalCROptions.set(str);
			Utils.postSelect(false);
		}
		@Override public String	guideValue()	{ return Utils.convert(get()); }
		@Override public String getRowGuide(int id)	{
			String key		= getLangLabel(id);
			String labelKey	= Utils.labelKey(key);
			String help		= realLangLabel(labelKey + LABEL_DESCRIPTION);
			if (help != null)
				return rowFormat(labelFormat(realLangLabel(labelKey)), help);

			key  = getGuiValue(id);
			help = realLangLabel(key + LABEL_DESCRIPTION);
			Race   race		= fileToAlienRace(key);
			String raceName = race.setupName;
			if (key.startsWith(BASE_RACE_MARKER))
				help = labelFormat(name(id)) + "<i>(Original species)</i>&nbsp " + race.getDescription1();
			else
				help = labelFormat(raceName) + race.getDescription1();
			help += "<br>" + race.getDescription2()
					+ "&ensp /&ensp " + race.getDescription3()
					+ "&ensp /&ensp " + race.getDescription4();
			return help;
		}
		@Override protected int dialogWidth()	{ return RotPUI.scaledSize(500); }
		@Override protected int dialogHeight()	{ return RotPUI.scaledSize(450);}
		@Override public void reInit(List<String> list)	 {
			valueLabelMap.clear();
			for (String element : list)
				put(element, Utils.convert(element));
			if (!list.contains(get()))
				set(list.get(0));
		}
	};
	// For Guide, do not add to panels
	class ParamListSpecificAbilities extends ParamList {
		public ParamListSpecificAbilities(String gui, String name, String defaultValue)	{
			super(gui, name, null, defaultValue);
			isDuplicate(true); // To activate get and set optionValue
		}
		@Override public String getOptionValue(IGameOptions opts)	{
			return opts.specificOpponentCROption(Utils.currentIndex());
		}
		@Override public void setOptionValue(IGameOptions opts, String str)	{
			opts.specificOpponentCROption(str, Utils.currentIndex());
			Utils.postSelect(false);
		}
		@Override public String	guideValue()		{ return Utils.convert(get()); }
		@Override public String getRowGuide(int id)	{
			String key		= getLangLabel(id);
			String labelKey	= Utils.labelKey(key);
			String help		= realLangLabel(labelKey + LABEL_DESCRIPTION);
			if (help != null)
				return rowFormat(labelFormat(realLangLabel(labelKey)), help);

			key  = getGuiValue(id);
			help = realLangLabel(key + LABEL_DESCRIPTION);
			Race   race		= fileToAlienRace(key);
			String raceName	= race.setupName;
			if (key.startsWith(BASE_RACE_MARKER))
				help = labelFormat(name(id)) + "<i>(Original species)</i>&nbsp " + race.getDescription1();
			else
				help = labelFormat(raceName) + race.getDescription1();
			help += "<br>" + race.getDescription2()
					+ "&ensp /&ensp " + race.getDescription3()
					+ "&ensp /&ensp " + race.getDescription4();
			return help;
		}
		@Override protected int dialogWidth()	{ return RotPUI.scaledSize(500); }
		@Override protected int dialogHeight()	{ return RotPUI.scaledSize(450);}
		@Override public void reInit(List<String> list)	{
			valueLabelMap.clear();
			for (String element : list)
				put(element, Utils.convert(element));
			if (!list.contains(get()))
				set(list.get(0));
		}
		public  String	guideValue(int id)	{ return Utils.convert(Utils.options().specificOpponentCROption(id)); }
	};
}
