package rotp.model.empires.species;

import static rotp.model.empires.species.SettingBase.CostFormula.DIFFERENCE;
import static rotp.model.empires.species.SettingBase.CostFormula.NORMALIZED;
import static rotp.ui.util.IParam.langLabel;
import static rotp.ui.util.PlayerShipSet.DISPLAY_RACE_SET;
import static rotp.util.LanguageManager.selectedLanguageDir;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import rotp.model.empires.Leader.Personality;
import rotp.model.game.DynOptions;
import rotp.model.game.DynamicOptions;
import rotp.model.game.IGameOptions;
import rotp.model.game.IRaceOptions;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipLibrary;
import rotp.ui.util.PlayerShipSet;
import rotp.ui.util.StringList;
import rotp.util.LabelManager;
import rotp.util.LanguageManager;

public abstract class SpeciesSettings {
//	public static final String LANG_SEP	= "§";
	public static final String ROOT		= "CUSTOM_RACE_";
	public static final String PLANET	= "PLANET_";
	public static final String EXT		= ".race";
	public static final String RANDOMIZED_RACE_KEY		= "RANDOMIZED_RACE";
	public static final String RANDOM_RACE_KEY			= "RANDOM_RACE_KEY";
	public static final String CUSTOM_RACE_KEY			= "CUSTOM_RACE_KEY";
	public static final String BASE_RACE_MARKER			= "*";
	public static final String TO_EDIT_TAG				= "*";
	public static final String TO_EDIT_DEFAULT			= "Edit" + TO_EDIT_TAG;
	public static final String CR_EMPIRE_NAME_RANDOM	= "Randomized";
	public static final boolean booleansAreBullet		= true;
	public static final FilenameFilter SPECIES_FILTER	= (File dir, String name1) -> name1.toLowerCase().endsWith(EXT);

	// Label list taken from the definitions files
	public static final HashMap<String, StringList> ONE_PER_EMPIRE_MAP	= new HashMap<>();
	public static final HashMap<String, StringList> ONE_PER_SPECIES_MAP = new HashMap<>();
	private static void updateEmpireSpeciesMap() {
		List<String> languageCodes = LanguageManager.current().languageCodes();
		for (String lang : languageCodes) {
			String dir = "lang/" + lang + "/races/Human.";
			StringList multiple	= new StringList();
			StringList common	= new StringList();
			LabelManager label	= new LabelManager();
			label.loadLabelFile(dir);
			label.filterLabelsTo(common, multiple);
			ONE_PER_EMPIRE_MAP.put(lang, multiple);
			ONE_PER_SPECIES_MAP.put(lang, common);
		}
	}
	static HashMap<String, StringList> getOnePerEmpireMap()	{
		if (ONE_PER_EMPIRE_MAP.isEmpty())
			updateEmpireSpeciesMap();
		return ONE_PER_EMPIRE_MAP;
	}
	static HashMap<String, StringList> getOnePerSpeciesMap()	{
		if (ONE_PER_SPECIES_MAP.isEmpty())
			updateEmpireSpeciesMap();
		return ONE_PER_SPECIES_MAP;
	}

	static String toLanguageKey(String s)		{ return "en".equals(s)? "" : s; }
	static boolean isCurrentLanguage(String s)	{ return selectedLanguageDir().equals(s); }

	// ========================================================================
	// Non Static variable and methods
	//
	protected final SettingMap settingMap = new SettingMap(); // !!! To be kept up to date !!!
	private SpeciesSkills race; // !!! To be kept up to date !!!
	protected DynOptions reworkedRace;
	boolean isReference = false; // For the rework sources, not to be updated.
	boolean loadingInternalRace = false; // For the rework sources, not to be updated.

	protected SpeciesSkills race()					{ return race; }; // !!! To be kept up to date !!!
	protected void race(SpeciesSkills race)			{ this.race = race; }; // !!! To be kept up to date !!!
	protected void setDialogueSettings(String dir)	{
		StringList list = getOnePerSpeciesMap().get(dir);
		String langKey = toLanguageKey(dir);
		for (String label : list) {
			String settingKey = ROOT + label + langKey;
			SpeciesDialogLabel setting = (SpeciesDialogLabel) settingMap.get(settingKey);
			if (setting == null) {
				setting = new SpeciesDialogLabel(label, dir);
				settingMap.addAttribute(setting);
			}
//			System.out.println("settingKey: " + settingKey + " value: " + race().raceLabels().label(label));
			setting.set(race().raceLabels().label(label)); //  + TO_EDIT_TAG // TODO BR: VALIDATE
			setting.skillToSetting(race());
		}
		list = getOnePerEmpireMap().get(dir);
		for (String label : list) {
			String settingKey = ROOT + label + langKey;
			EthnicDialogLabel setting = (EthnicDialogLabel) settingMap.get(settingKey);
			if (setting == null) {
				setting = new EthnicDialogLabel(label, dir);
				settingMap.addAttribute(setting);
			}
//			System.out.println("settingKey: " + settingKey + " value: " + race().raceLabels().label(label));
			setting.set(race().raceLabels().label(label)); //  + TO_EDIT_TAG
			setting.skillToSetting(race());
		}
	}
	void report(ICRSettings setting, String text)	{
//		if (setting instanceof SettingStringLanguage)
//			System.out.println(text + " " + setting.toString());
	}
	// ========================================================================
	// Sub Classes
	//
	class SettingMap {
		private final List<ICRSettings> settingList	= new ArrayList<>(); // !!! To be kept up to date !!!
		private final List<ICRSettings> guiList	 	= new ArrayList<>();
		private final List<ICRSettings> attributeList	= new ArrayList<>();
		private final HashMap <String, ICRSettings> settingMap = new HashMap<>();
		boolean filled = false;

		private void put(String name, ICRSettings setting)	{
			if (name.startsWith("_"))
				System.out.println("name.startsWith(_)");
			if (filled)
				System.out.println("settingMap is not ready for new entry");
			settingMap.put(name, setting);
		}
		void add(ICRSettings setting) {
			if (settingList.contains(setting)) {
				System.err.println("DUPLICATE settingList");
			}
			settingList.add(setting);
			put(setting.getLangLabel(), setting);
		}
		void addGui(ICRSettings setting) {
			if (guiList.contains(setting)) {
				System.err.println("DUPLICATE guilist");
			}
			guiList.add(setting);
			put(setting.getLangLabel(), setting);
		}
		void addAttribute(ICRSettings setting) {
			if (attributeList.contains(setting)) {
				System.err.println("DUPLICATE attributeList");
			}
			attributeList.add(setting);
			put(setting.getLangLabel(), setting);
		}
//		void addAttribute(ICRSettings setting, String Key) {
//			if (attributeList.contains(setting)) {
//				System.err.println("DUPLICATE attributeList");
//			}
//			attributeList.add(setting);
//			put(Key, setting);
//		}
		List<ICRSettings> getGuis()			{ return guiList; }
		List<ICRSettings> getSettings()		{ return settingList; }
		List<ICRSettings> getAttributes()	{ return attributeList; }
		List<ICRSettings> getAll()			{
			List<ICRSettings> list = new ArrayList<>(settingList);
			list.addAll(attributeList);
			list.addAll(guiList);
			return list;
		}
		ICRSettings get(String key)			{ return settingMap.get(key); }
		StringList getList(String key)		{ return ((SettingStringList)settingMap.get(ROOT + key)).getList(); }
	}

	// ==================== RaceName ====================
	//
	class RaceName extends SettingStringList { // TODO BR: update to standard list selection
		static final String KEY = "RACE_NAME";
		static final String EN_SINGULAR	= "_race";
		static final String EN_PLURAL	= "_race_plural";
		static final String EN_EMPIRE	= "_empire";
		RaceName(String langDir) {
			super(ROOT, KEY, TO_EDIT_DEFAULT, langDir);
			randomStr("Random Race");
		}
		private void forward(DynamicOptions src)	{ // TODO BR: Other Languages
			String singular = src.getString(ROOT + EN_SINGULAR + langKey);
			if (singular != null)
				return;
			String value = settingValue();
			if (value == null || value.endsWith(TO_EDIT_TAG))
				return;
			System.out.println("forward changed a value: " + value + " singular: " + singular);
			src.setString(ROOT + EN_SINGULAR + langKey, value);
			src.setString(ROOT + EN_PLURAL + langKey, value + "s" + TO_EDIT_TAG);
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions) { // For backward compatibility
			super.updateOptionTool(srcOptions);
			forward(srcOptions);
		}
		@Override public void pushToSkills(SpeciesSkills skills) {
			skills.parseSpeciesNames(settingValue());
			skills.setupName = skills.setupName();
		}
		@Override public void pullFromSkills(SpeciesSkills skills) {
			String raceNames = String.join(",", skills.speciesNames());
			set(raceNames);
		}
	}
	// ==================== RaceDescription ====================
	//
	class RaceDescription extends SettingStringLanguage {
		private static String KEY(int i)	{ return "RACE_DESC_" + i; }
		private final int id;
		RaceDescription(int i, String langDir) {
			super(ROOT, KEY(i), "Description "+i, 2, langDir);
			id = i;
			randomStr("Randomized");
		}
		@Override void validateLingoDefault(DynamicOptions speciesOptions, String currentLingo)	{
			if (isReference || reworkedRace == null)
				return;
			super.validateLingoDefault(speciesOptions, currentLingo);
		}
		@Override void validateReworkedDefault(String value)	{
			if (isReference || reworkedRace == null)
				return;
			super.validateReworkedDefault(value);
		}
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.setDescription(id, settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.getDescription(id)); }
	}
	// ==================== RacePrefix ====================
	//
	class RacePrefix extends SettingString {
		RacePrefix() {
			super(ROOT, "RACE_PREFIX", "@", 1);
			randomStr("#");
			isBullet(false);
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.speciesPrefix(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.speciesPrefix()); }
	}
	// ==================== RaceSuffix ====================
	//
	class RaceSuffix extends SettingString {
		RaceSuffix() {
			super(ROOT, "RACE_SUFFIX", "", 1);
			randomStr("#");
			isBullet(false);
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.speciesSuffix(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.speciesSuffix()); }
	}
	// ==================== LeaderPrefix ====================
	//
	class LeaderPrefix extends SettingString {
		LeaderPrefix() {
			super(ROOT, "LEADER_PREFIX", "@", 1);
			randomStr("#");
			isBullet(false);
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.leaderPrefix(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.leaderPrefix()); }
	}
	// ==================== LeaderSuffix ====================
	//
	class LeaderSuffix extends SettingString {
		LeaderSuffix() {
			super(ROOT, "LEADER_SUFFIX", "", 1);
			randomStr("#");
			isBullet(false);
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.leaderSuffix(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.leaderSuffix()); }
	}
	// ==================== WorldsPrefix ====================
	//
	class WorldsPrefix extends SettingString {
		WorldsPrefix() {
			super(ROOT, "WORLDS_PREFIX", "@", 1);
			randomStr("#");
			isBullet(false);
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.worldsPrefix(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.worldsPrefix()); }
	}
	// ==================== WorldsSuffix ====================
	//
	class WorldsSuffix extends SettingString {
		WorldsSuffix() {
			super(ROOT, "WORLDS_SUFFIX", "", 1);
			randomStr("#");
			isBullet(false);
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.worldsSuffix(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.worldsSuffix()); }
	}
	// ==================== AvailablePlayer ====================
	//
	@SuppressWarnings("unused")
	class AvailablePlayer extends SettingBoolean {
		private static final boolean defaultValue = true;

		AvailablePlayer() {
			super(ROOT, "AVAILABLE_PLAYER", defaultValue);
			isBullet(false);
			hasNoCost(true);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.availablePlayer(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.availablePlayer()); }
	}
	// ==================== AvailableAI ====================
	//
	class AvailableAI extends SettingBoolean {
		private static final boolean defaultValue = true;

		AvailableAI() {
			super(ROOT, "AVAILABLE_AI", defaultValue);
			isBullet(false);
			hasNoCost(true);
			getToolTip();
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.availableAI(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.availableAI()); }
	}
	// ==================== BoundAI ====================
	//
	class BoundAI extends SettingBase<String> {
		private static final String BOUND_AI = "BOUND_AI";
		private static final String DEFAULT_VALUE = ROOT + BOUND_AI + "_NONE";

		BoundAI() {
			super(ROOT, BOUND_AI);
			isBullet(false);
			hasNoCost(true);
			showFullGuide(false);
			getToolTip();
			initOptionsText();
			labelsAreFinals(true);
			allowListSelect(true);
			refreshLevel(1);
			for (String s : IGameOptions.specificAIset().getAliens()) {
				put(s, s.toUpperCase(), 0f, s);
			}
			put(DEFAULT_VALUE, DEFAULT_VALUE, 0f, DEFAULT_VALUE);
			defaultCfgValue(DEFAULT_VALUE);
			initOptionsText();
		}
		@Override public String guideDefaultValue()	{ return defaultLangLabel(); }
		@Override public String guideValue()		{ return langLabel(settingValue()); }
		@Override public void settingToSkill(SpeciesSkills skills) 		{ skills.preferredShipSet(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) 		{ set(skills.preferredShipSet()); }
	}

	// ==================== PreferredShipSize ====================
	//
	class PreferredShipSize extends SettingBase<String> {
		private static final String defaultValue = "Large";

		PreferredShipSize() {
			super(ROOT, "FAVORED_SHIP_SIZE");
			isBullet(true);
			hasNoCost(true);
			showFullGuide(true);
			getToolTip();
			initOptionsText();
			labelsAreFinals(true);
			put("Small",	ROOT + "SHIP_SIZE_SMALL",	0f, "Small");
			put("Medium",	ROOT + "SHIP_SIZE_MEDIUM",	0f, "Medium");
			put("Large",	ROOT + "SHIP_SIZE_LARGE",	0f, "Large");
			put("Huge",		ROOT + "SHIP_SIZE_HUGE",	0f, "Huge");
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.preferredShipSize(index()); }
		@Override public void skillToSetting(SpeciesSkills skills) { index(skills.preferredShipSize()); }
	}
	// ==================== PreferredShipSet ====================
	//
	class PreferredShipSet extends SettingBase<String> {
		private static final String defaultValue = DISPLAY_RACE_SET;

		PreferredShipSet() {
			super(ROOT, "FAVORED_SHIPSET");
			isBullet(false);
			hasNoCost(true);
			showFullGuide(false);
			getToolTip();
			initOptionsText();
			labelsAreFinals(true);
			allowListSelect(true);
			refreshLevel(1);
			String root = PlayerShipSet.rootLabelKey();
			for (String s : ShipLibrary.current().styles) {
				put(s, root + s.toUpperCase(), 0f, s);
			}
			put(DISPLAY_RACE_SET, PlayerShipSet.displayLabelKey(), 0f, DISPLAY_RACE_SET);
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public String guideDefaultValue()	{ return defaultLangLabel(); }
		@Override public String guideValue()		{ return getSelLangLabel(); }
		@Override public void settingToSkill(SpeciesSkills skills) 		{ skills.preferredShipSet(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) 		{ set(skills.preferredShipSet()); }
	}
	// ==================== ReworkedRaceKey ====================
	//
	class ReworkedRaceKey extends SettingBase<String> {
		static final String REWORKED_RACE_KEY = "REWORKED_RACE_KEY";
		static final String DEFAULT_VALUE = "NONE";

		private static void setReworkedKey(DynOptions opts, String key)	{ opts.setString(ROOT + REWORKED_RACE_KEY, key); }
		private static String getReworkedKey(DynOptions opts)			{ return opts.getString(ROOT + REWORKED_RACE_KEY, DEFAULT_VALUE); }
		static String getRawReworkedKey(DynOptions opts)		{
			String key = opts.getString(ROOT + REWORKED_RACE_KEY, DEFAULT_VALUE);
			return DEFAULT_VALUE.equals(key)? null : key;
		}
		/**
		 * Get a reworked key from the filename, or the folder name
		 * @param file source file
		 * @param foldersRework true -> Get a reworked key from the Folder.
		 * @return the key, "" if none
		 */
		private static String fileToReworked (File file, boolean foldersRework)	{
			// Test for reworked old Ways
			String name = file.getName();
			name = name.substring(0, name.length() - EXT.length());
			if (IRaceOptions.allRaceOptions.contains(name))
				return name;
			if (foldersRework) {
				Path path = file.toPath();
				int count = path.getNameCount();
				String dir = "RACE_" + path.getName(count-2).toString().toUpperCase();
				if (IRaceOptions.allRaceOptions.contains(dir))
					return dir;
			}
			return "";
		}
		/**
		 * validate the key and return it
		 * @param opt options files
		 * @param file File to be checked for old reworked way
		 * @param foldersRework true -> Get a reworked key from the Folder.
		 * @return true if the reworked file is not empty
		 */
		static String validReworked(DynOptions opt, File file, boolean foldersRework)	{
			String optKey	= getReworkedKey(opt); // current key
			String fileKey	= fileToReworked(file, foldersRework); // potential candidate

			if (fileKey.isEmpty()) // no candidate
				return optKey;

			if (!fileKey.equals(optKey)) { // update the key
				setReworkedKey(opt, fileKey);
				DynOptions.saveOptions(opt, file);
				return fileKey;
			}
			return optKey;
		}
		ReworkedRaceKey() {
			super(ROOT, REWORKED_RACE_KEY);
			isBullet(false);
			hasNoCost(true);
			showFullGuide(false);
			getToolTip();
			initOptionsText();
			labelsAreFinals(true);
			allowListSelect(true);
			refreshLevel(1);
			for (Entry<String, String> s : Species.namesMap().entrySet())
				put(s.getValue(), s.getKey(), 0f, s.getKey());
			String defaultValue = LabelManager.current().label(ROOT + REWORKED_RACE_KEY + "_" + DEFAULT_VALUE);
			put(defaultValue, DEFAULT_VALUE, 0f, DEFAULT_VALUE);
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills)	{ skills.reworkableSpeciesKey(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills)	{ set(skills.reworkableSpeciesKey()); }
		@Override protected StringList altReturnList()	{ return new StringList(getValues()); }
		@Override protected StringList guiTextsList()	{ return getOptions(); }
		@Override public String guideDefaultValue()		{ return getDefaultCfgValue(); }
		@Override public String guideValue()			{ return getCfgValue(); }
		@Override public void updateOptionTool()		{
			if (!isSpacer() && dynOpts() != null)
				set(dynOpts().getString(getLangLabel(), DEFAULT_VALUE));
		}
		@Override public void updateOption(DynamicOptions destOptions)	{
			if (!isSpacer() && destOptions != null)
				destOptions.setString(getLangLabel(), settingValue());
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions)	{
			if (!isSpacer() && srcOptions != null) {
				String reworkKey = srcOptions.getString(getLangLabel());
				if (reworkKey == null || DEFAULT_VALUE.equals(reworkKey)) {
					reworkedRace = null;
					set(DEFAULT_VALUE);
				}
				else {
					set(reworkKey);
					Race refSkill = Species.getAnim(reworkKey);
					CustomRaceDefinitions cr = new CustomRaceDefinitions(refSkill, true, false);
					
					reworkedRace = cr.getAsOptions();
				}
				set(srcOptions.getString(getLangLabel(), DEFAULT_VALUE));
			}
		}
		@Override public void copyOption(IGameOptions src, IGameOptions dest, boolean updateTool, int cascadeSubMenu)	{
			if (!isSpacer() && src != null && dest != null)
				dest.dynOpts().setString(getLangLabel(), settingValue());
			dest.dynOpts().setString(getLangLabel(), src.dynOpts().getString(getLangLabel(), DEFAULT_VALUE));
		}
	}
	// ==================== CRObjective ====================
	//
	class CRObjective {
		private float[] objectivePct	= new float[Personality.values().length];
		Militarist		militarist		= new Militarist();
		Ecologist		ecologist		= new Ecologist();
		Diplomat		diplomat		= new Diplomat();
		Industrialist	industrialist	= new Industrialist();
		Expansionist	expansionist	= new Expansionist();
		Technologist	technologist	= new Technologist();

		CRObjective() {}
		private CRObjective objective()	{ return this; }
		void pushSetting(SpeciesSkills skills) {
			objectivePct[0] = militarist.settingValue();
			objectivePct[1] = ecologist.settingValue();
			objectivePct[2] = diplomat.settingValue();
			objectivePct[3] = industrialist.settingValue();
			objectivePct[4] = expansionist.settingValue();
			objectivePct[5] = technologist.settingValue();

			// Normalization
			float sum = 0;
			for (float f : objectivePct)
				sum += f;
			if (sum == 0f) // User entry! anything is possible
				objectivePct[0] = 1f;
			else
				for (int i=0; i<objectivePct.length; i++)
					objectivePct[i] /= sum;

			skills.objectivePct(objectivePct);
		}
		void pullSetting(SpeciesSkills skills) {
			objectivePct = skills.objectivePct();
			militarist   .set(objectivePct[0]);
			ecologist    .set(objectivePct[1]);
			diplomat     .set(objectivePct[2]);
			industrialist.set(objectivePct[3]);
			expansionist .set(objectivePct[4]);
			technologist .set(objectivePct[5]);
		}
		// ==================== Technologist ====================
		//
		private class Technologist extends SettingFloat {
			// big = good
			Technologist() {
				super(ROOT, "TECHNOLOGIST", 0f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
			@Override public void settingToSkill(SpeciesSkills skills) { objective().pushSetting(skills); }
			@Override public void skillToSetting(SpeciesSkills skills) { objective().pullSetting(skills); }
		}
		// ==================== Expansionist ====================
		//
		private class Expansionist extends SettingFloat {
			private Expansionist() {
				super(ROOT, "EXPANSIONIST", 0f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Industrialist ====================
		//
		private class Industrialist extends SettingFloat {
			Industrialist() {
				super(ROOT, "INDUSTRIALIST", 0.2f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Diplomat ====================
		//
		private class Diplomat extends SettingFloat {
			private Diplomat() {
				super(ROOT, "DIPLOMAT", 0.5f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Ecologist ====================
		//
		private class Ecologist extends SettingFloat {
			Ecologist() {
				super(ROOT, "ECOLOGIST", 0.2f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Militarist ====================
		//
		private class Militarist extends SettingFloat {
			Militarist() {
				super(ROOT, "MILITARIST", 0.1f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
	}
	// ==================== CRPersonality ====================
	//
	class CRPersonality {
		private float[] personalityPct	= new float[Personality.values().length];
		Erratic		erratic		= new Erratic();
		Pacifist	pacifist	= new Pacifist();
		Honorable	honorable	= new Honorable();
		Ruthless	ruthless	= new Ruthless();
		Aggressive	aggressive	= new Aggressive();
		Xenophobic	xenophobic	= new Xenophobic();

		CRPersonality() {}
		private CRPersonality personality() { return this; }
		
		void pushSetting(SpeciesSkills skills) {
			personalityPct[0] = erratic.settingValue();
			personalityPct[1] = pacifist.settingValue();
			personalityPct[2] = honorable.settingValue();
			personalityPct[3] = ruthless.settingValue();
			personalityPct[4] = aggressive.settingValue();
			personalityPct[5] = xenophobic.settingValue();

			// Normalization
			float sum = 0;
			for (float f : personalityPct)
				sum += f;
			if (sum == 0f) // User entry! anything is possible
				personalityPct[0] = 1f;
			else
				for (int i=0; i<personalityPct.length; i++)
					personalityPct[i] /= sum;

			skills.personalityPct(personalityPct);
		}
		void pullSetting(SpeciesSkills skills) {
			personalityPct = skills.personalityPct();
			erratic   .set(personalityPct[0]);
			pacifist  .set(personalityPct[1]);
			honorable .set(personalityPct[2]);
			ruthless  .set(personalityPct[3]);
			aggressive.set(personalityPct[4]);
			xenophobic.set(personalityPct[5]);
		}
		// ==================== Xenophobic ====================
		//
		private class Xenophobic extends SettingFloat {
			// big = good
			Xenophobic() {
				super(ROOT, "XENOPHOBIC", 0f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
			@Override public void settingToSkill(SpeciesSkills skills) { personality().pushSetting(skills); }
			@Override public void skillToSetting(SpeciesSkills skills) { personality().pullSetting(skills); }
		}
		// ==================== Aggressive ====================
		//
		private class Aggressive extends SettingFloat {
			Aggressive() {
				super(ROOT, "AGGRESSIVE", 0f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Ruthless ====================
		//
		private class Ruthless extends SettingFloat {
			Ruthless() {
				super(ROOT, "RUTHLESS", 0.2f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Honorable ====================
		//
		private class Honorable extends SettingFloat {
			Honorable() {
				super(ROOT, "HONORABLE", 0.5f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Pacifist ====================
		//
		private class Pacifist extends SettingFloat {
			Pacifist() {
				super(ROOT, "PACIFIST", 0.2f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Erratic ====================
		//
		private class Erratic extends SettingFloat {
			Erratic() {
				super(ROOT, "ERRATIC", 0.1f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
	}
	// ==================== CreditsBonus ====================
	//
	class CreditsBonus extends SettingInteger {
		// big = good
		CreditsBonus() {
			super(ROOT, "CREDIT", 0, 0, 35, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .8f}, new float[]{0f, .8f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.bCBonus(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.bCBonus() * 100)); }
	}
	// ==================== HitPointsBonus ====================
	//
	class HitPointsBonus extends SettingInteger {
		// big = good
		HitPointsBonus() {
			super(ROOT, "HIT_POINTS", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .6f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.hPFactor(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.hPFactor() * 100)); }
	}
	// ==================== ShipSpaceBonus ====================
	//
	// Absolute min = ? .75 not OK for colony building!
	class ShipSpaceBonus extends SettingInteger {
		// big = good
		ShipSpaceBonus() {
			super(ROOT, "SHIP_SPACE", 100, 80, 175, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1f}, new float[]{0f, 2f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.shipSpaceFactor(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.shipSpaceFactor() * 100)); }
	}
	// ==================== MaintenanceBonus ====================
	//
	class MaintenanceBonus extends SettingInteger {
		// Big = bad
		public MaintenanceBonus() {
			super(ROOT, "MAINTENANCE", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, -.2f}, new float[]{0f, -.4f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.maintenanceFactor(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.maintenanceFactor() * 100f)); }
	}
	// ==================== PlanetRessources ====================
	//
	class PlanetRessources extends SettingBase<String> {
		private static final String defaultValue = "Normal";

		public PlanetRessources() {
			super(ROOT, "HOME_RESOURCES");
			isBullet(true);
			labelsAreFinals(true);
			showFullGuide(true);
			put("UltraPoor",	PLANET + "ULTRA_POOR",		-50f, "UltraPoor");
			put("Poor",			PLANET + "POOR",			-25f, "Poor");
			put("Normal",		ROOT   + "RESOURCES_NORMAL",  0f, "Normal");
			put("Rich",			PLANET + "RICH",			 30f, "Rich");
			put("UltraRich",	PLANET + "ULTRA_RICH",		 50f, "UltraRich");
//			put("Artifacts",	PLANET + "ARTIFACTS",		 40f, "Artifacts");
			defaultCfgValue(defaultValue);
			initOptionsText();
		}

		@Override public void settingToSkill(SpeciesSkills skills) { skills.planetRessource(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.planetRessource()); }
	}
	// ==================== PlanetArtifacts ====================
	//
	class PlanetArtifacts extends SettingBase<String> {
		private static final String defaultValue = "None";

		public PlanetArtifacts() {
			super(ROOT, "HOME_ARTIFACTS");
			isBullet(true);
			labelsAreFinals(true);
			showFullGuide(true);
			put("None",			ROOT + "ARTIFACTS_NONE",	  0f, "None");
			put("Artifacts",	PLANET + "ARTIFACTS",		 40f, "Artifacts");
			put("OrionLike",	ROOT   + "ARTIFACTS_ORION",  80f, "OrionLike");
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.planetArtifacts(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.planetArtifacts()); }
	}
	// ==================== PlanetEnvironment ====================
	//
	class PlanetEnvironment extends SettingBase<String> {
		private static final String defaultValue = "Normal";

		PlanetEnvironment() {
			super(ROOT, "HOME_ENVIRONMENT");
			isBullet(true);
			labelsAreFinals(true);
			showFullGuide(true);
			put("Hostile", PLANET + "HOSTILE",			  -20f, "Hostile");
			put("Normal",  ROOT   + "ENVIRONMENT_NORMAL",	0f, "Normal");
			put("Fertile", PLANET + "FERTILE",			   15f, "Fertile");
			put("Gaia",	   PLANET + "GAIA",				   30f, "Gaia");
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.planetEnvironment(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.planetEnvironment()); }
	}
	// ==================== PlanetType ====================
	//
	@SuppressWarnings("unused")
	class RacePlanetType extends SettingBase<String> {
		private static final String defaultValue = "Terran";

		RacePlanetType() {
			super(ROOT, "HOME_TYPE");
			isBullet(true);
			labelsAreFinals(true);
			showFullGuide(true);
			put("Ocean",	PlanetType.OCEAN,	0f, PlanetType.OCEAN);
			put("Jungle",	PlanetType.JUNGLE,	0f, PlanetType.JUNGLE);
			put("Terran",	PlanetType.TERRAN,	0f, PlanetType.TERRAN);
			defaultCfgValue(defaultValue);
			initOptionsText();
			hasNoCost(true); // to be removed
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.homeworldPlanetType(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.homeworldPlanetType()); }
	}
	// ==================== HomeworldSize ====================
	//
	class HomeworldSize extends SettingInteger {
		HomeworldSize() {
			super(ROOT, "HOME_SIZE", 100, 70, 150, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .7f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.homeworldSize(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.homeworldSize()); }
	}
	// ==================== SpeciesType ====================
	//
	@SuppressWarnings("unused")
	class SpeciesType extends SettingBase<Integer> {
		private static final String defaultValue = "Terran";

		SpeciesType() {
			super(ROOT, "RACE_TYPE");
			isBullet(true);
			labelsAreFinals(true);
			put("Terran",	"RACE_TERRAN",   0f, 1);
			put("Aquatic",	"RACE_AQUATIC",  2f, 2);
			put("Silicate",	"RACE_SILICATE", 4f, 3);
			put("Robotic",	"RACE_ROBOTIC",	 4f, 4);
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.speciesType(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.speciesType()); }
	}
	// ==================== IgnoreEco ====================
	//
	class IgnoresEco extends SettingBase<String> {
		private static final String defaultValue = "No";

		IgnoresEco() {
			super(ROOT, "IGNORES_ECO");
			isBullet(true);
			labelsAreFinals(true);
			showFullGuide(true);
			put("None",		ROOT+"IGNORES_ECO_NO",			0f, "No");
			put("Limited",	ROOT+"IGNORES_ECO_LIMITED",	30f, "Limited");
			put("All",		ROOT+"IGNORES_ECO_ALL",		50f, "All");
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) {
			skills.acceptedPlanetEnvironment(settingValue());
			skills.ignoresPlanetEnvironment(!settingValue().equalsIgnoreCase("No"));
		}
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.acceptedPlanetEnvironment()); }
		@Override public void updateOptionTool(DynamicOptions srcOptions) {
			if (srcOptions != null) {
				// get the old boolean value (if there is one)
				boolean oldBooleanValue = srcOptions.getBoolean(getLangLabel(), false);
				String defaultValue = oldBooleanValue? "All" : "No";
				setFromCfgValue(srcOptions.getString(getLangLabel(), defaultValue));
			}
		}
	}
	// ==================== PopGrowRate ====================
	//
	class PopGrowRate extends SettingInteger {
		PopGrowRate() {
			super(ROOT, "POP_GROW_RATE", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .3f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.growthRateMod(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round (skills.growthRateMod() * 100)); }
	}
	// ==================== ShipAttack ====================
	//
	class ShipAttack extends SettingInteger {
		ShipAttack() {
			super(ROOT, "SHIP_ATTACK", 0, -1, 5, 1, 1, 1,
					DIFFERENCE, new float[]{0f, 3f}, new float[]{0f, 5f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.shipAttackBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.shipAttackBonus()); }
	}
	// ==================== ShipDefense ====================
	//
	class ShipDefense extends SettingInteger {
		ShipDefense() {
			super(ROOT, "SHIP_DEFENSE", 0, -1, 5, 1, 1, 1,
					DIFFERENCE, new float[]{0f, 1.5f, 1.5f}, new float[]{0f, 6f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.shipDefenseBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.shipDefenseBonus()); }
	}
	// ==================== ShipInitiative ====================
	//
	class ShipInitiative extends SettingInteger {
		ShipInitiative() {
			super(ROOT, "SHIP_INITIATIVE", 0, -1, 5, 1, 1, 1,
					DIFFERENCE, new float[]{5f, 1f}, new float[]{0f, 6f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.shipInitiativeBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.shipInitiativeBonus()); }
	}
	// ==================== GroundAttack ====================
	//
	class GroundAttack extends SettingInteger {
		GroundAttack() {
			super(ROOT, "GROUND_ATTACK", 0, -20, 30, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1.25f}, new float[]{0f, 0.75f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.groundAttackBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.groundAttackBonus()); }
	}
	// ==================== SpyCost ====================
	//
	class SpyCost extends SettingInteger {
		SpyCost() {
			super(ROOT, "SPY_COST", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, -.1f}, new float[]{0f, -.2f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.spyCostMod(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.spyCostMod() * 100)); }
	}
	// ==================== SpySecurity ====================
	//
	class SpySecurity extends SettingInteger {
		SpySecurity() {
			super(ROOT, "SPY_SECURITY", 0, -20, 40, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1f}, new float[]{0f, 2f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.internalSecurityAdj(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.internalSecurityAdj() * 100)); }
	}
	// ==================== SpyInfiltration ====================
	//
	class SpyInfiltration extends SettingInteger {
		SpyInfiltration() {
			super(ROOT, "SPY_INFILTRATION", 0, -20, 40, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1.25f}, new float[]{0f, 2.5f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.spyInfiltrationAdj(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.spyInfiltrationAdj() * 100)); }
	}
	// ==================== SpyTelepathy ====================
	//
	@SuppressWarnings("unused")
	class SpyTelepathy extends SettingBoolean {
		private static final boolean defaultValue = false;

		SpyTelepathy() {
			super(ROOT, "SPY_TELEPATHY", defaultValue, 20f, 0f);
			isBullet(booleansAreBullet);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.telepathic(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.telepathic()); }
	}
	// ==================== DiplomacyTrade ====================
	//
	class DiplomacyTrade extends SettingInteger {
		DiplomacyTrade() {
			super(ROOT, "DIPLOMACY_TRADE", 0, -30, 30, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .3f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.tradePctBonus(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.tradePctBonus() * 100)); }
	}
	// ==================== DiploPosDP ====================
	//
	@SuppressWarnings("unused")
	class DiploPosDP extends SettingInteger {
		DiploPosDP() {
			super(ROOT, "DIPLO_POS_DP", 100, 70, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .3f}, new float[]{0f, .8f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.positiveDPMod(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.positiveDPMod() * 100)); }
	}
	// ==================== DiplomacyBonus ====================
	//
	class DiplomacyBonus extends SettingInteger {
		DiplomacyBonus() {
			super(ROOT, "DIPLOMACY_BONUS", 0, -50, 100, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .1f}, new float[]{0f, .2f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.diplomacyBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.diplomacyBonus()); }
	}
	// ==================== DiplomacyCouncil ====================
	//
	class DiplomacyCouncil extends SettingInteger {
		DiplomacyCouncil() {
			super(ROOT, "DIPLOMACY_COUNCIL", 0, -25, 25, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .2f}, new float[]{0f, .2f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.councilBonus(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.councilBonus() * 100)); }
	}
	// ==================== RelationDefault ====================
	//
	class RelationDefault extends SettingInteger {
		RelationDefault() {
			super(ROOT, "RELATION_DEFAULT", 0, -10, 10, 1, 2, 4,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .4f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.defaultRaceRelations(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set((int)skills.defaultRaceRelations()); }
	}
	// ==================== ProdWorker ====================
	//
	class ProdWorker extends SettingInteger {
		// bigger = better
		ProdWorker() {
			super(ROOT, "PROD_WORKER", 100, 70, 300, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .8f, 0f}, new float[]{0f, 0.8f, 0.01f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.workerProductivityMod(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.workerProductivityMod() * 100)); }
	}
	// ==================== ProdControl ====================
	//
	class ProdControl extends SettingInteger {
		ProdControl() {
			super(ROOT, "PROD_CONTROL", 0, -1, 4, 1, 1, 1,
					DIFFERENCE, new float[]{0f, 15f, 0f}, new float[]{0f, 30f, 0f});
			pctValue(false);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.robotControlsAdj(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.robotControlsAdj()); }
	}
	// ==================== IgnoresFactoryRefit ====================
	//
	class IgnoresFactoryRefit extends SettingBoolean {
		private static final boolean defaultValue = false;

		IgnoresFactoryRefit() {
			super(ROOT, "PROD_REFIT_COST", defaultValue, 40f, 0f);
			isBullet(booleansAreBullet);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.ignoresFactoryRefit(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.ignoresFactoryRefit()); }
	}
	// ==================== TechResearch  And Discovery====================
	//
	class Technologies {
		TechResearch	techResearch	= new TechResearch();
		TechDiscovery	techDiscovery	= new TechDiscovery();

		// ==================== TechResearch ====================
		//
		class TechResearch extends SettingInteger {

			ResearchComputer		computer	= new ResearchComputer();
			ResearchConstruction	construction= new ResearchConstruction();
			ResearchForceField		forceField	= new ResearchForceField();
			ResearchPlanet			planet		= new ResearchPlanet();
			ResearchPropulsion		propulsion	= new ResearchPropulsion();
			ResearchWeapon			weapon		= new ResearchWeapon();

			TechResearch() {
				super(ROOT, "TECH_RESEARCH", 100, 60, 200, 1, 5, 20, DIFFERENCE,
						new float[]{0f, 0.7f, 0.004f},
						new float[]{0f, 1.0f, 0.006f});
				hasNoCost(true);
				initOptionsText();
			}
			@Override public void settingToSkill(SpeciesSkills skills) { skills.researchBonusPct(settingValue()/100f); }
			@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.researchBonusPct() * 100)); }
			@Override public String guiSettingDisplayStr() {
				return getLabel() + ": " + guideSelectedValue() + " " + costString(cost());
			}
			@Override protected boolean next(Integer i) {
				super.next(i);
				computer.settingText().repaint(computer.guiSettingDisplayStr());
				construction.settingText().repaint(construction.guiSettingDisplayStr());
				forceField.settingText().repaint(forceField.guiSettingDisplayStr());
				planet.settingText().repaint(planet.guiSettingDisplayStr());
				propulsion.settingText().repaint(propulsion.guiSettingDisplayStr());
				weapon.settingText().repaint(weapon.guiSettingDisplayStr());
				return false;
			}
			@Override public void enabledColor(float cost) { super.enabledColor(cost()); }

			String costString(float cost) {
				String str = "(<";
				str +=  new DecimalFormat("0.0").format(cost);
				return str + ">)";
			}
			private float cost() {
				return computer.settingCost()
						+ construction.settingCost()
						+ forceField.settingCost()
						+ planet.settingCost()
						+ propulsion.settingCost()
						+ weapon.settingCost();
			}

			// ==================== ResearchComputer ====================
			//
			private class ResearchComputer extends SettingResearch {
				ResearchComputer() {
					super("RESEARCH_COMPUTER");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.techMod(0, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.techMod(0) * 100)); }
			}
			// ==================== ResearchConstruction ====================
			//
			private class ResearchConstruction extends SettingResearch {
				ResearchConstruction() {
					super("RESEARCH_CONSTRUCTION");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.techMod(1, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.techMod(1) * 100)); }
			}
			// ==================== ResearchForceField ====================
			//
			private class ResearchForceField extends SettingResearch {
				ResearchForceField() {
					super("RESEARCH_FORCEFIELD");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.techMod(2, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.techMod(2) * 100)); }
			}
			// ==================== ResearchPlanet ====================
			//
			private class ResearchPlanet extends SettingResearch {
				ResearchPlanet() {
					super("RESEARCH_PLANET");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.techMod(3, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.techMod(3) * 100)); }
			}
			// ==================== ResearchPropulsion ====================
			//
			private class ResearchPropulsion extends SettingResearch {
				ResearchPropulsion() {
					super("RESEARCH_PROPULSION");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.techMod(4, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.techMod(4) * 100)); }
			}
			// ==================== ResearchWeapon ====================
			//
			private class ResearchWeapon extends SettingResearch {
				ResearchWeapon() {
					super("RESEARCH_WEAPON");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.techMod(5, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.techMod(5) * 100)); }
			}
			//
			// ==================== Research ====================
			//
			private class SettingResearch extends SettingInteger {
				// Cost: smaller = better
				private static final float	c0 = 0;
				private static final float	c1 = -18.02331959f;
				private static final float	c2 = 9.56463523f;
				private static final float	c3 = -4.365405984f;
				private static final float	c4 = 0.824090347f;
				private static final float	baseCostDefault = 100f;
				private static final float	norm = 100f;

				private SettingResearch(String nameLangLabel) {
					super(ROOT, nameLangLabel, 100, 50, 200, 1, 5, 20, NORMALIZED,
							new float[]{c0, c1, c2, c3, c4}, null);
				}

				@Override public float settingCost() { return settingCost(combinedValue()); }
				@Override protected float settingCost(Integer value) {
					float baseCost = (value - baseCostDefault)/norm;
					float cost = 0;
					for (int i=0; i<posCostFactor.length; i++) {
						cost += posCostFactor[i] * Math.pow(baseCost, i);			
					}
					return cost;
				}
				@Override public String guiSettingDisplayStr() {
					return getLabel() + ": " + guideValue() + " " + costString(this.settingCost());
				}
				@Override public String guideValue() {
					//String str = settingValue().toString();
					String str = guideSelectedValue();
					str += " -> ";
					str += String.valueOf(combinedValue()) + "%";
					return str;
				}

				String costString(float cost) {
					String str = "(";
					str +=  new DecimalFormat("0.0").format(cost);
					return str + ")";
				}
				private Integer combinedValue() { return combinedValue(settingValue()); }
				private Integer combinedValue(Integer value) {
					return Math.round(100f * value / techResearch.settingValue());
				}
			}
		}
		// ==================== TechDiscovery ====================
		//
		class TechDiscovery extends SettingInteger {

			DiscoveryComputer	  computer		= new DiscoveryComputer();
			DiscoveryConstruction construction	= new DiscoveryConstruction();
			DiscoveryForceField	  forceField	= new DiscoveryForceField();
			DiscoveryPlanet		  planet		= new DiscoveryPlanet();
			DiscoveryPropulsion	  propulsion	= new DiscoveryPropulsion();
			DiscoveryWeapon		  weapon		= new DiscoveryWeapon();

			TechDiscovery() {
				super(ROOT, "TECH_DISCOVERY", 50, 0, 100, 1, 5, 20,
						DIFFERENCE, new float[]{0f, .5f}, new float[]{0f, 0.5f});
				hasNoCost(true);
				initOptionsText();
			}
			@Override public void settingToSkill(SpeciesSkills skills) { skills.techDiscoveryPct(settingValue()/100f); }
			@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.techDiscoveryPct() * 100)); }
			@Override public String guiSettingDisplayStr() {
				return getLabel() + ": " + guideSelectedValue() + " " + costString(cost());
			}
			@Override protected boolean next(Integer i) {
				super.next(i);
				computer.settingText().repaint(computer.guiSettingDisplayStr());
				construction.settingText().repaint(construction.guiSettingDisplayStr());
				forceField.settingText().repaint(forceField.guiSettingDisplayStr());
				planet.settingText().repaint(planet.guiSettingDisplayStr());
				propulsion.settingText().repaint(propulsion.guiSettingDisplayStr());
				weapon.settingText().repaint(weapon.guiSettingDisplayStr());
				return false;
			}
			@Override public void enabledColor(float cost) { super.enabledColor(cost()); }

			String costString(float cost) {
				String str = "(<";
				str +=  new DecimalFormat("0.0").format(cost);
				return str + ">)";
			}
			private float cost() {
				return computer.settingCost()
						+ construction.settingCost()
						+ forceField.settingCost()
						+ planet.settingCost()
						+ propulsion.settingCost()
						+ weapon.settingCost();
			}

			// ==================== DiscoveryComputer ====================
			//
			private class DiscoveryComputer extends SettingDiscovery {
				// smaller = better
				DiscoveryComputer() {
					super("DISCOVERY_COMPUTER");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.discoveryMod(0, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.discoveryMod(0) * 100)); }
			}
			// ==================== DiscoveryConstruction ====================
			//
			private class DiscoveryConstruction extends SettingDiscovery {
				DiscoveryConstruction() {
					super("DISCOVERY_CONSTRUCTION");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.discoveryMod(1, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.discoveryMod(1) * 100)); }
			}
			// ==================== DiscoveryForceField ====================
			//
			private class DiscoveryForceField extends SettingDiscovery {
				DiscoveryForceField() {
					super("DISCOVERY_FORCEFIELD");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.discoveryMod(2, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.discoveryMod(2) * 100)); }
			}
			// ==================== DiscoveryPlanet ====================
			//
			private class DiscoveryPlanet extends SettingDiscovery {
				DiscoveryPlanet() {
					super("DISCOVERY_PLANET");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.discoveryMod(3, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.discoveryMod(3) * 100)); }
			}
			// ==================== DiscoveryPropulsion ====================
			//
			private class DiscoveryPropulsion extends SettingDiscovery {
				DiscoveryPropulsion() {
					super("DISCOVERY_PROPULSION");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.discoveryMod(4, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.discoveryMod(4) * 100)); }
			}
			// ==================== DiscoveryWeapon ====================
			//
			private class DiscoveryWeapon extends SettingDiscovery {
				DiscoveryWeapon() {
					super("DISCOVERY_WEAPON");
					initOptionsText();
				}
				@Override public void settingToSkill(SpeciesSkills skills) { skills.discoveryMod(5, settingValue()/100f); }
				@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.discoveryMod(5) * 100)); }
			}
			//
			// ==================== Discovery ====================
			//
			private class SettingDiscovery extends SettingInteger {
	
				private static final float	c0 = 0f;
				private static final float	c1 = 4.9221976f;
				private static final float	c2 = 1.25604100f;
				private static final float	c3 = -2.37443919f;
				private static final float	c4 = -0.62901149f;
				private static final float	c5 = 0.576847553f;
				private static final float	baseCostDefault = 50f;
				private static final float	norm = 50f;

				private SettingDiscovery(String nameLangLabel) {
					super(ROOT, nameLangLabel, 0, -100, 100, 1, 5, 20, NORMALIZED,
							new float[]{c0, c1, c2, c3, c4, c5}, null);
				}

				@Override public float settingCost() {
					return settingCost(combinedValue());
				}
				@Override protected float settingCost(Integer value) {
					float baseCost = (value - baseCostDefault)/norm;
					float cost = 0;
					for (int i=0; i<posCostFactor.length; i++) {
						cost += posCostFactor[i] * Math.pow(baseCost, i);			
					}
					return cost;
				}
				@Override public String guiSettingDisplayStr() {
					return getLabel() + ": " + guideValue() + " " + costString(this.settingCost());
				}
				@Override public String guideValue() {
					String str = guideSelectedValue();
					str += " -> ";
					str += String.valueOf(combinedValue()) + "%";
					return str;
				}

				private String costString(float cost) {
					String str = "(";
					str +=  new DecimalFormat("0.0").format(cost);
					return str + ")";
				}
				private Integer combinedValue() { return combinedValue(settingValue()); }
				private Integer combinedValue(Integer value) {
					return Math.max(0, Math.min(100, 
							Math.round(value + techDiscovery.settingValue())));
				}
			}
		}
	}

	// ==================== Names and Labels ====================
	//
	// ==================== HomeWorld ====================
	//
	class HomeWorld extends SettingStringList {
		private static final String KEY = "HOME_WORLD";
		HomeWorld(String langDir)	{ super(ROOT, KEY, TO_EDIT_DEFAULT, langDir); }
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.parseHomeWorlds(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.homeSystemNames().asString()); }
	}
	// ==================== LeaderName ====================
	//
	class LeaderName extends SettingStringList {
		private static final String KEY = "LEADER_NAME";
		LeaderName(String langDir)	{ super(ROOT, KEY, TO_EDIT_DEFAULT, langDir); }
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.parseLeaderNames(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.leaderNames().asString()); }
	}
	// ==================== EmpireName ====================
	//
	class EmpireName extends EthnicDialogLabel {
		private static final String PREVIOUS_KEY = "RACE_EMPIRE_NAME";
		private static final String KEY = "_empire";
		EmpireName(String langDir) {
			super(KEY, langDir);
			randomStr(CR_EMPIRE_NAME_RANDOM);
		}
		private String validate(DynamicOptions src)	{
			String value = src.getString(getLangLabel());
			if (value != null) // UpToDate
				return value;
			//Update the options
			value = src.getString(ROOT+PREVIOUS_KEY, defaultValue());
			src.setString(getLangLabel(), value);
			return value;
		}
		@Override public void updateOptionTool()	{ // For backward compatibility
			if (!isSpacer() && dynOpts() != null)
				set(validate(dynOpts()));
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions) { // For backward compatibility
			if (srcOptions != null && !isSpacer())
				set(validate(srcOptions));
		}
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.empireTitle(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.empireTitle()); }
	}
	// ==================== LeaderTitle ====================
	//
	class LeaderTitle extends SettingStringLanguage {
		private static final String KEY = "LEADER_TITLE";
		LeaderTitle(String langDir)		{ super(ROOT, KEY, TO_EDIT_DEFAULT, langDir); }

//		@Override void validateReworkedDefault(String value)	{ // TODO BR: REMOVE
//			super.validateReworkedDefault(value);
//		}
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.title(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.title()); }
	}
	// ==================== LeaderFullTitle ====================
	//
	class LeaderFullTitle extends SettingStringLanguage {
		private static final String KEY = "LEADER_FULLTITLE";
		LeaderFullTitle(String langDir)		{ super(ROOT, KEY, TO_EDIT_DEFAULT, langDir); }
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.fullTitle(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.fullTitle()); }
	}

	class SpeciesAttributes implements ICRSettings { // for one Language
		SpeciesNameItems		speciesNameItems;
		SpeciesDescriptionItems	speciesDescriptionItems;
		SpeciesLabelItems		speciesLabelItems;
		EthnicNameItems			empireNameItems;
		EthnicLabelItems		empireLabelItems;

		SpeciesAttributes(String langDir)	{
			String labelKey = langDir;
			speciesNameItems		= new SpeciesNameItems(labelKey);
			speciesDescriptionItems	= new SpeciesDescriptionItems(labelKey);
			speciesLabelItems		= new SpeciesLabelItems(labelKey);
			empireNameItems			= new EthnicNameItems(labelKey);
			empireLabelItems		= new EthnicLabelItems(labelKey);
			// TODO BR:
		}
		@Override public void updateOptionTool() {
			speciesNameItems.updateOptionTool();
			speciesDescriptionItems.updateOptionTool();
			speciesLabelItems.updateOptionTool();
			empireNameItems.updateOptionTool();
			empireLabelItems.updateOptionTool();
		}
		@Override public void updateOption(DynamicOptions destOptions) {
			speciesNameItems.updateOption(destOptions);
			speciesDescriptionItems.updateOption(destOptions);
			speciesLabelItems.updateOption(destOptions);
			empireNameItems.updateOption(destOptions);
			empireLabelItems.updateOption(destOptions);
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions) {
			speciesNameItems.updateOptionTool(srcOptions);
			speciesDescriptionItems.updateOptionTool(srcOptions);
			speciesLabelItems.updateOptionTool(srcOptions);
			empireNameItems.updateOptionTool(srcOptions);
			empireLabelItems.updateOptionTool(srcOptions);
		}
	}
	// ====================
	// List of unique Names
	//
	// ====================
	// Species List
	//
	class SpeciesNameItems extends ICSSettingsStringList {
		private static final long serialVersionUID = 1L;
		SpeciesNameItems(String langDir)	{
			String langKey = toLanguageKey(langDir);

			SettingString setting = (SettingString) settingMap.get(ROOT + LeaderTitle.KEY + langKey);
			if (setting == null) {
				setting	= new LeaderTitle(langDir);
				setting.skillToSetting(race());
				settingMap.addAttribute(setting);
			}
			add(setting);

			setting = (SettingString) settingMap.get(ROOT + LeaderFullTitle.KEY + langKey);
			if (setting == null) {
				setting	= new LeaderFullTitle(langDir);
				setting.skillToSetting(race());
				settingMap.addAttribute(setting);
			}
			add(setting);
		}
	}
	class SpeciesDescriptionItems extends ICSSettingsStringList {
		private static final long serialVersionUID = 1L;
		private static final int[] SEQ = new int[] {1, 2, 4, 3};
		private final String languageDir;
		SpeciesDescriptionItems(String langDir)	{
			languageDir = langDir;
			String langKey = toLanguageKey(languageDir);
			for (int i : SEQ) {
				SettingString setting = (SettingString) settingMap.get(ROOT + RaceDescription.KEY(i) + langKey);
				if (setting == null) {
					setting	= new RaceDescription(i, langDir);
					setting.skillToSetting(race());
					settingMap.addAttribute(setting);
				}
				add(setting);
			}
		}
	}
	class SpeciesLabelItems extends ICSSettingsStringList {
		private static final long serialVersionUID = 1L;
		private final String languageDir;
		SpeciesLabelItems(String langDir)	{
			languageDir = langDir;
			String langKey = toLanguageKey(languageDir);
			StringList list = getOnePerSpeciesMap().get(languageDir);
			for (String label : list) {
				SpeciesDialogLabel setting = (SpeciesDialogLabel) settingMap.get(ROOT + label + langKey);
				if (setting == null) {
					setting	= new SpeciesDialogLabel(label, langDir);
					setting.skillToSetting(race());
					settingMap.addAttribute(setting);
				}
				add(setting);
			}
		}
	}
	class ICSSettingsStringList extends ArrayList<SettingString> implements ICRSettings {
		static final long serialVersionUID = 1L;
		@Override public void updateOptionTool() {
			for (ICRSettings item : this)
				item.updateOptionTool();
		}
		@Override public void updateOption(DynamicOptions destOptions) {
			for (ICRSettings item : this)
				item.updateOption(destOptions);
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions) {
			for (ICRSettings item : this)
				item.updateOptionTool(srcOptions);
		}
	}
	// ==================== Multi Language Settings ====================
	//
	// ====================
	// Empire List
	//
	class EthnicNameItems extends ICSSettingsStringList {
		private static final long serialVersionUID = 1L;
		EthnicNameItems(String langDir)	{
			String langKey = toLanguageKey(langDir);
			SettingStringList setting = (SettingStringList) settingMap.get(ROOT + RaceName.KEY + langKey);
			if (setting == null) {
				setting	= new RaceName(langDir);
				setting.skillToSetting(race());
				settingMap.addAttribute(setting);
			}
			add(setting);
			setting = (SettingStringList) settingMap.get(ROOT + HomeWorld.KEY + langKey);
			if (setting == null) {
				setting	= new HomeWorld(langDir);
				setting.skillToSetting(race());
				settingMap.addAttribute(setting);
			}
			add(setting);
			setting = (SettingStringList) settingMap.get(ROOT + LeaderName.KEY + langKey);
			if (setting == null) {
				setting	= new LeaderName(langDir);
				setting.skillToSetting(race());
				settingMap.addAttribute(setting);
			}
			add(setting);
		}
	}
	class EthnicLabelItems extends ICSSettingsStringList { // DialogLabels
		private static final long serialVersionUID = 1L;
		EthnicLabelItems(String langDir)	{
			String langKey = toLanguageKey(langDir);
			StringList list = getOnePerEmpireMap().get(langDir);
			for (String label : list) {
				EthnicDialogLabel setting = (EthnicDialogLabel) settingMap.get(ROOT + label + langKey);
				if (setting == null) {
					setting	= new EthnicDialogLabel(label, langDir);
					setting.skillToSetting(race());
					settingMap.addAttribute(setting);
				}
				add(setting);
			}
		}
	}
	// ==================== DialogLabel ====================
	//
	class EthnicDialogLabel extends SettingStringList { // TODO BR: Push Pull (Validate)
		EthnicDialogLabel(String labelKey, String langDir)	{
			super(ROOT, labelKey, TO_EDIT_DEFAULT, langDir);
			key = nameLangLabel + langKey;
		}
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.parseDialogLabel(key, settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{
//			System.out.println("pullFromSkills key = " + key + " / value = " + skills.raceLabels().label(key)); // TODO BR: REMOVE
			set(skills.raceLabels().label(key)); }
	}
	class SpeciesDialogLabel extends SettingStringLanguage {
		SpeciesDialogLabel(String labelKey, String langDir)	{
			super(ROOT, labelKey, TO_EDIT_DEFAULT, langDir);
			key = nameLangLabel + langKey;
		}
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.parseDialogLabel(key, settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.raceLabels().label(key)); }
	}
	abstract class SettingStringList extends SettingStringLanguage {
		int lastIndex;
		SettingStringList(String guiLangLabel, String nameLangLabel, String defaultValue, String langDir) {
			super(guiLangLabel, nameLangLabel, defaultValue, langDir);
		}
		SettingStringList(String guiLangLabel, String nameLangLabel, String defaultValue, int lineNum ,String langDir) {
			super(guiLangLabel, nameLangLabel, defaultValue, lineNum, langDir);
			set(defaultValue);
		}
		@Override public String settingValue(int item)	{
			lastIndex = item;
			return getList().get(item, TO_EDIT_DEFAULT);
		}
		@Override public void selectedValue(int id, String val)	{
			lastIndex = id;
			StringList list = getList();
			list.set(id, val);
			setList(list);
		}
		@Override public void addValue(int item, String val)	{
			lastIndex = item;
			StringList list = getList();
			list.add(item, val);
			setList(list);
		}
		@Override public int getIndex()		{ return lastIndex; }
		@Override String getSelectedItem()	{ return getList().get(lastIndex, TO_EDIT_DEFAULT); }
		@Override void validateReworkedDefault(String value)	{
			if (isReference || reworkedRace == null)
				return;
			if (TO_EDIT_DEFAULT.equals(value)) { // Try to get reworked value
				String reworkedList = reworkedRace.getString(getLangLabel());
				if (reworkedList == null)
					return;
				String reworkedValue = new StringList(reworkedList).get(lastIndex, TO_EDIT_DEFAULT);
				if (!reworkedValue.isEmpty())
					setAsLastIndex(tagValue(reworkedValue));
				else {
					System.out.println("validateReworkedDefault reworkedValue = null");
				}
			}
		}
		@Override void validateLingoDefault(DynamicOptions speciesOptions, String currentLingo)	{
			if (isReference)
				return;
			String value = getSelectedItem();
			if (TO_EDIT_DEFAULT.equals(value)) { // Try to get reworked value
				String masterKey = ROOT + nameLangLabel + toLanguageKey(currentLingo);
				String masterList = speciesOptions.getString(masterKey);
				if (masterList == null)
					return;
				String masterValue = new StringList(masterList).get(lastIndex, TO_EDIT_DEFAULT);
				if (!masterValue.isEmpty())
					setAsLastIndex(tagValue(masterValue));
				else {
					System.out.println("validateLingoDefault masterValue = null");
				}
			}
		}
		void setAsLastIndex(String s)	{
			StringList list = getList();
			list.set(lastIndex, s);
			setList(list);
		}		
		void setList(StringList list)	{ super.set(list.asString()); }
		StringList getList()			{ return new StringList(settingValue()); }
		boolean remove(String s)		{
			StringList list = getList();
			boolean removed = list.remove(s);
			setList(list);
			return removed;
		}
		boolean add(String s)			{
			StringList list = getList();
			boolean added = list.add(s);
			setList(list);
			return added;
		}
	}
	abstract class SettingStringLanguage extends SettingString {
		String langDir, langKey, key, nameLangLabel;
		SettingStringLanguage(String guiLangLabel, String nameLangLabel, String defaultValue, String langDir) {
			super(guiLangLabel, nameLangLabel + toLanguageKey(langDir), defaultValue);
			this.nameLangLabel = nameLangLabel;
			if (langDir.isEmpty())
				System.out.println("langDir.isEmpty()"); // TODO BR: REMOVE
			this.langDir = langDir;
			langKey = toLanguageKey(langDir);
			key = ROOT + nameLangLabel + langKey;
			
		}
		SettingStringLanguage(String guiLangLabel, String nameLangLabel, String defaultValue, int lineNum ,String langDir) {
			super(guiLangLabel, nameLangLabel + toLanguageKey(langDir), defaultValue, lineNum);
			this.nameLangLabel = nameLangLabel;
			if (langDir.isEmpty())
				System.out.println("langDir.isEmpty()"); // TODO BR: REMOVE
			this.langDir = langDir;
			set(defaultValue);
			langKey = toLanguageKey(langDir);
			key = ROOT + nameLangLabel + langKey;
		}
		abstract void pushToSkills(SpeciesSkills skills);
		abstract void pullFromSkills(SpeciesSkills skills);
		String tagValue(String value)	{ return value.endsWith(TO_EDIT_TAG)? value : value + TO_EDIT_TAG; }
		String getLiveValue()			{ return settingValue(); }
		void setLiveValue(String str)	{ set(str); }
		String getSelectedItem()		{ return settingValue(); }
		void validateReworkedDefault(String value)	{
			if (isReference || reworkedRace == null)
				return;
			if (TO_EDIT_DEFAULT.equals(value)) {
				String reworkedValue = reworkedRace.getString(getLangLabel()); // Try to get reworked value
				if (reworkedValue != null)
					set(tagValue(reworkedValue));
				else {
					System.out.println("validateReworkedDefault reworkedValue = null");
				}
			}
		}
		void validateLingoDefault(DynamicOptions speciesOptions, String currentLingo)	{
			if (isReference)
				return;
			String value = settingValue();
			if (TO_EDIT_DEFAULT.equals(value)) {
				String masterKey = ROOT + nameLangLabel + toLanguageKey(currentLingo);
				String masterValue = speciesOptions.getString(masterKey);
				if (masterValue != null)
					set(tagValue(masterValue));
				else {
					System.out.println("validateLingoDefault masterValue = null");
				}
			}
		}
		@Override public void settingToSkill(SpeciesSkills skills) {
			String currentLingo = selectedLanguageDir();
			if (langDir.equals(currentLingo)) {
				validateReworkedDefault(getSelectedItem());
				pushToSkills(skills);	// Current language
			}
			else {
				validateLingoDefault(skills.speciesOptions(), currentLingo);
				super.settingToSkill(skills);	// Other Language
			}
		}
		@Override public void skillToSetting(SpeciesSkills skills) {
			String currentLingo = selectedLanguageDir();
			if (langDir.equals(currentLingo)) {
				pullFromSkills(skills);	// Current language
				validateReworkedDefault(getSelectedItem());
			}
			else {
				super.skillToSetting(skills);	// Other Language
				validateLingoDefault(skills.speciesOptions(), currentLingo);
			}
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions) {
			if (srcOptions != null && !isSpacer()) {
				String val = srcOptions.getString(getLangLabel());
				if (val == null || nameLangLabel.equals(key)) {
					val = srcOptions.getString(key);
				}
				String currentLingo = selectedLanguageDir();
				if (langDir.equals(currentLingo)) {
					set(srcOptions.getString(getLangLabel(), defaultValue()));
					validateReworkedDefault(getSelectedItem());
				}
				else {
//					set(srcOptions.getString(getLangLabel(), defaultValue())); // TODO convert to options
//					validateLingoDefault(reworkedRace, currentLingo);
				}
			}
		}
	}

}