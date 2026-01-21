package rotp.model.empires.species;

import static rotp.model.empires.species.SettingBase.CostFormula.DIFFERENCE;
import static rotp.model.empires.species.SettingBase.CostFormula.NORMALIZED;
import static rotp.model.game.IMainOptions.speciesDirectoryPath;
import static rotp.model.game.IRaceOptions.defaultRaceKey;
import static rotp.ui.util.IParam.langLabel;
import static rotp.ui.util.PlayerShipSet.DISPLAY_RACE_SET;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import rotp.Rotp;
import rotp.model.empires.Leader.Personality;
import rotp.model.game.DynOptions;
import rotp.model.game.DynamicOptions;
import rotp.model.game.IGameOptions;
import rotp.model.game.IRaceOptions;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipLibrary;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.util.PlayerShipSet;
import rotp.ui.util.StringList;
import rotp.util.LabelManager;
import rotp.util.LanguageManager;

public abstract class SpeciesSettings {
	protected static final String DEFAULT_LANGUAGE	= "en";
	private	static final String PLANET				= "PLANET_";
	public	static final String ROOT				= "CUSTOM_RACE_";
			static final String EXT					= ".race";
	private	static final String RANDOMIZED_RACE_KEY	= "RANDOMIZED_RACE";
	public	static final String RANDOM_RACE_KEY		= "RANDOM_RACE_KEY";
	public	static final String CUSTOM_RACE_KEY		= "CUSTOM_RACE_KEY";
	public	static final String BASE_RACE_MARKER	= "*";
	private	static final boolean booleansAreBullet	= true;
	static	final String CR_EMPIRE_NAME_RANDOM		= "Randomized";
	static	final FilenameFilter SPECIES_FILTER		= (File dir, String name1) -> name1.toLowerCase().endsWith(EXT);

	// Label list taken from the definitions files
	protected String workingLanguageCode; 
	private static final HashMap<String, StringList> ONE_PER_EMPIRE_MAP	= new HashMap<>();
	private static final HashMap<String, StringList> ONE_PER_SPECIES_MAP = new HashMap<>();
	protected void updateEmpireSpeciesMap() {
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
			workingLanguageCode = selectedLanguageDir();
		}
	}
	private HashMap<String, StringList> getOnePerEmpireMap()	{
		if (ONE_PER_EMPIRE_MAP.isEmpty() || !selectedLanguageDir().equals(workingLanguageCode))
			updateEmpireSpeciesMap();
		return ONE_PER_EMPIRE_MAP;
	}
	private HashMap<String, StringList> getOnePerSpeciesMap(){
		if (ONE_PER_SPECIES_MAP.isEmpty() || !selectedLanguageDir().equals(workingLanguageCode))
			updateEmpireSpeciesMap();
		return ONE_PER_SPECIES_MAP;
	}

	private static String toLanguageKey(String s)	{ return DEFAULT_LANGUAGE.equals(s)? "" : s; }
	protected static String selectedLanguageDir()	{ return LanguageManager.selectedLanguageDir(); }

	// ========================================================================
	// #=== Non Static variable and methods
	//
	protected final SettingMap settingMap = new SettingMap(); // !!! To be kept up to date !!!
	private SpeciesSkills race; // !!! To be kept up to date !!!
	private DynOptions animOptions;
	protected SpeciesSkills animSkills;
	private boolean isReference	= false;
	private boolean isForShow	= false;
	BasePanel parent;

	private boolean isReference()			{ return isReference; };
	protected void isReference(boolean b)	{ isReference = b; };
	private boolean isForShow()				{ return isForShow; };
	protected void isForShow(boolean b)		{ isForShow = b; };
	protected SpeciesSkills race()			{ return race; };		// !!! To be kept up to date !!!
	protected void race(SpeciesSkills race)	{ this.race = race; };	// !!! To be kept up to date !!!
	protected void autoUpdate(KeyEvent e)	{ // For developers only
		if(!(Rotp.isIDE() && e.isShiftDown() && e.isControlDown()))
			return;
		settingMap.cleanLanguages();
		AllSpeciesAttributes settings = new AllSpeciesAttributes();
		settings.fillFromAnim(true);
	}

	private boolean callUI()	{
		settingMap.cleanLanguages();
		SettingMap backupMap = new SettingMap();
		backupMap.copyFrom(settingMap);
		race().speciesOptions().backupStringMap();

		AllSpeciesAttributes settings = new AllSpeciesAttributes();
		CustomNameUI ui = new CustomNameUI(parent, settings);
		RotPUI.animationListeners.add(ui);
		boolean canceled = ui.showPanel();
		RotPUI.animationListeners.remove(ui);

		if (canceled) {
			settingMap.copyFrom(backupMap);
			race().speciesOptions().restoreStringMap();
			for (ICRSettings setting : settingMap.getAll())
				setting.settingToSkill(race());
		}
		else {
			// Update language setting
			List<String> codes = LanguageManager.current().languageCodes();
			List<String> names = LanguageManager.current().languageNames();
			LanguageList languageSetting = (LanguageList) settingMap.get(ROOT + LanguageList.KEY);
			StringList languageDir = new StringList();
			for (String name : settings.getLanguageNames()) {
				int idx = names.indexOf(name);
				String dir = codes.get(idx);
				languageDir.add(dir);
			}

			DynOptions destOptions = race().speciesOptions();
			languageSetting.set(languageDir.asString());
			languageSetting.settingToSkill(race());
			languageSetting.updateOption(destOptions);

			// clean languages if needed
			settingMap.cleanLanguages();

			// update the skills from the source option
			List<ICRSettings> icrSettings = settingMap.getAll();
			for (ICRSettings setting : icrSettings) {
				if (setting instanceof SettingStringLanguage) {
					setting.settingToSkill(race());
					setting.updateOption(destOptions);
				}
			}
		}
		return true;
	}
	// -#-
	// ========================================================================
	// Sub Classes
	//
	// #======================== Setting Map ========================
	final class SettingMap {
		private final List<ICRSettings> settingList	= new ArrayList<>(); // !!! To be kept up to date !!!
		private final List<ICRSettings> guiList	 	= new ArrayList<>();
		private final List<ICRSettings> attributeList	= new ArrayList<>();
		private final HashMap <String, ICRSettings> settingMap = new HashMap<>();
		boolean filled = false;

		private void put(String name, ICRSettings setting)	{
			if (name.startsWith("_"))
				System.out.println("name.startsWith(_)");
			settingMap.put(name, setting);
		}
		void add(ICRSettings setting) {
			if (settingList.contains(setting)) {
				System.err.println("DUPLICATE settingList " + setting.getLangLabel());
			}
			settingList.add(setting);
			put(setting.getLangLabel(), setting);
		}
		void addGui(ICRSettings setting) {
			if (guiList.contains(setting)) {
				System.err.println("DUPLICATE guilist " + setting.getLangLabel());
			}
			guiList.add(setting);
			put(setting.getLangLabel(), setting);
		}
		void addAttribute(ICRSettings setting) {
			if (attributeList.contains(setting)) {
				System.err.println("DUPLICATE attributeList " + setting.getLangLabel());
			}
			attributeList.add(setting);
			put(setting.getLangLabel(), setting);
		}
		List<ICRSettings> getGuis()		{ return guiList; }
		List<ICRSettings> getSettings()	{ return settingList; }
		List<ICRSettings> getAll()		{
			List<ICRSettings> list = new ArrayList<>(settingList);
			list.addAll(attributeList);
			list.addAll(guiList);
			return list;
		}
		ICRSettings get(String key)		{ return settingMap.get(key); }
		private StringList getList(String key)	{
			ICRSettings setting = settingMap.get(key);
			if (setting == null) {
				setting = settingMap.get(key.substring(0, key.length()-2));
				if (setting == null) {
					setting = settingMap.get(key + selectedLanguageDir());
					if (setting == null)
						return new StringList("new");
					else
						return ((SettingStringList)setting).getList();
				}
				StringList list = ((SettingStringList)setting).getList();
				return new StringList(list.getFirst());
			}
			else
				return ((SettingStringList)setting).getList();
		}
		private void copyFrom(SettingMap src)	{
			settingList.clear();
			settingList.addAll(src.settingList);
			guiList.clear();
			guiList.addAll(src.guiList);
			attributeList.clear();
			attributeList.addAll(src.attributeList);
			settingMap.clear();
			settingMap.putAll(src.settingMap);
			filled = src.filled;
		}
		void cleanLanguages()	{
			LanguageList languageList = (LanguageList) get(ROOT + LanguageList.KEY);
			StringList toKeep = new StringList(languageList.settingValue());
			String selectedLanguage = selectedLanguageDir();
			if (!toKeep.contains(selectedLanguage))
				toKeep.add(selectedLanguage);
			HashMap <String, ICRSettings> mapCopy = new HashMap<>(settingMap);
			for(Entry<String, ICRSettings> entry : mapCopy.entrySet()) {
				if (entry.getValue() instanceof SettingStringLanguage) {
					SettingStringLanguage setting = (SettingStringLanguage) entry.getValue();
					if (!toKeep.contains(setting.langDir)) {
						settingMap.remove(entry.getKey());
						attributeList.remove(setting);
					}
				}
			}
		}
		void languageChanged(String oldDir, String newDir)	{
			// first update the settings
			if (parent != null && parent.isVisible()) {
				for(Entry<String, ICRSettings> entry : settingMap.entrySet()) {
					if (entry.getValue() instanceof SettingStringLanguage) {
						SettingStringLanguage setting = (SettingStringLanguage) entry.getValue();
						if (setting.languageChanged(oldDir, newDir)) {
						}
					}
				}
			}
			// Then update settingMap indexes
			HashMap <String, ICRSettings> mapCopy = new HashMap<>(settingMap);
			settingMap.clear();
			for(ICRSettings setting : mapCopy.values())
				settingMap.put(setting.getLangLabel(), setting);
		}
	}
	// -#-
	// #==================== ReworkedRaceKey ====================
	//
	class AvatarKey extends SettingBase<String> {
		private static final String AVATAR_KEY = "REWORKED_RACE_KEY";
		static final String DEFAULT_VALUE = "NONE";

		private static void setAvatarKey(DynOptions opts, String key)	{ opts.setString(ROOT + AVATAR_KEY, key); }
		private static String getAvatarKey(DynOptions opts)			{ return opts.getString(ROOT + AVATAR_KEY, DEFAULT_VALUE); }
		static String getRawAvatarKey(DynOptions opts)	{
			String key = opts.getString(ROOT + AVATAR_KEY, DEFAULT_VALUE);
			return DEFAULT_VALUE.equals(key)? null : key;
		}
		/**
		 * Get an avatar key from the filename, or the folder name
		 * @param file source file
		 * @param foldersAvatar true -> Get a reworked key from the Folder.
		 * @return the key, "" if none
		 */
		private static String fileToAvatar (File file, boolean foldersAvatar)	{
			// Test for reworked old Ways
			String name = file.getName();
			name = name.substring(0, name.length() - EXT.length());
			if (IRaceOptions.allRaceKeyList.contains(name))
				return name;
			if (foldersAvatar) {
				Path path = file.toPath();
				int count = path.getNameCount();
				String dir = "RACE_" + path.getName(count-2).toString().toUpperCase();
				if (IRaceOptions.allRaceKeyList.contains(dir))
					return dir;
			}
			return "";
		}
		/**
		 * validate the key and return it
		 * @param opt options files
		 * @param file File to be checked for old reworked way
		 * @param foldersAvatar true -> Get a reworked key from the Folder.
		 * @return true if the reworked file is not empty
		 */
		static String validRedesign(DynOptions opt, File file, boolean foldersAvatar)	{
			String optKey	= getAvatarKey(opt); // current key
			String fileKey	= fileToAvatar(file, foldersAvatar); // potential candidate

			if (fileKey.isEmpty()) // no candidate
				return optKey;

			if (!fileKey.equals(optKey)) { // update the key
				setAvatarKey(opt, fileKey);
				DynOptions.saveOptions(opt, file);
				return fileKey;
			}
			return optKey;
		}
		AvatarKey() {
			super(ROOT, AVATAR_KEY);
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
			String defaultValue = LabelManager.current().label(ROOT + AVATAR_KEY + "_" + DEFAULT_VALUE);
			put(defaultValue, DEFAULT_VALUE, 0f, DEFAULT_VALUE);
			defaultCfgValue(defaultValue);
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills)	{ skills.avatarSpeciesKey(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills)	{ set(skills.avatarSpeciesKey()); }
		@Override protected StringList altReturnList()	{ return new StringList(getValues()); }
		@Override protected StringList guiTextsList()	{ return getOptions(); }
		@Override public String guideDefaultValue()		{ return getDefaultCfgValue(); }
		@Override public String guideValue()			{ return getCfgValue(); }
		@Override public void updateOption(DynamicOptions destOptions)	{
			if (!isSpacer() && destOptions != null)
				destOptions.setString(dynOptionIndex(), settingValue());
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions)	{
			if (!isSpacer() && srcOptions != null) {
				String reworkKey = srcOptions.getString(dynOptionIndex());
				if (reworkKey == null || DEFAULT_VALUE.equals(reworkKey)) {
					animSkills = null;
					animOptions = null;
					set(DEFAULT_VALUE);
				}
				else {
					isReference(true);
					set(reworkKey);
					animSkills = SkillsFactory.getMasterSkillsForReworked(reworkKey);
					animOptions = animSkills.speciesOptions();
					isReference(false);
				}
				set(srcOptions.getString(dynOptionIndex(), DEFAULT_VALUE));
			}
		}
		@Override public void copyOption(IGameOptions src, IGameOptions dest, boolean updateTool, int cascadeSubMenu)	{
			if (!isSpacer() && src != null && dest != null)
				dest.dynOpts().setString(dynOptionIndex(), settingValue());
			dest.dynOpts().setString(dynOptionIndex(), src.dynOpts().getString(dynOptionIndex(), DEFAULT_VALUE));
		}
		@Override public boolean isSettingString()	{ return true; }
	}
	// ==================== Animation ID ====================
	//
	class AnimationId extends SettingInteger {
		private static final String KEY = "ANIMATION_ID";
		// big = good
		AnimationId() {
			super(ROOT, KEY, -1, 0, null);
			hasNoCost(true);
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.reworkableId(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.reworkableId()); }
	}

	// -#-
	// #==================== AllSpeciesAttributes ====================
	//
	class AllSpeciesAttributes {
		SettingString raceKey;
		private StringList civilizationNames, languageNames;
		private HashMap<String, SpeciesAttributes> attributesMap = new HashMap<>();

		AllSpeciesAttributes()	{
			raceKey = (RaceKey) settingMap.get(ROOT + RaceKey.RACE_KEY);
			LanguageList languageList = (LanguageList) settingMap.get(ROOT + LanguageList.KEY);
			StringList languages = new StringList(languageList.settingValue());
			for (String lg : languages) {
				attributesMap.put(lg, new SpeciesAttributes(lg));
			}
		}
		StringList getLanguageNames()	{
			if (languageNames == null) {
				LanguageList languageList = (LanguageList) settingMap.get(ROOT + LanguageList.KEY);
				StringList languageDir =  new StringList(languageList.settingValue());
				languageNames = new StringList();
				List<String> codes = LanguageManager.current().languageCodes();
				List<String> names = LanguageManager.current().languageNames();
				for (String dir : languageDir) {
					int idx = codes.indexOf(dir);
					String name = names.get(idx);
					languageNames.add(name);
				}
				//languageNames = new StringList(languageList.settingValue());
				int idx = languageDir.indexOf(selectedLanguageDir());
				languageNames.setSelectedIndex(idx);
			}
			return languageNames;
		}
		StringList getCivilizationsNames()	{
			if (civilizationNames == null) {
				String currentLanguage = selectedLanguageDir();
				if (DEFAULT_LANGUAGE.equals(currentLanguage))
					civilizationNames = settingMap.getList(ROOT + RaceName.KEY);
				else
					civilizationNames = settingMap.getList(ROOT + RaceName.KEY + currentLanguage);
				civilizationNames.setSelectedIndex(0);
			}
			return civilizationNames;
		}
		StringList getLeadersNames(String currentLanguage)	{
			StringList leadersNames;
			if (DEFAULT_LANGUAGE.equals(currentLanguage))
				leadersNames = settingMap.getList(ROOT + LeaderName.KEY);
			else
				leadersNames = settingMap.getList(ROOT + LeaderName.KEY + currentLanguage);
			leadersNames.setSelectedIndex(0);
			return leadersNames;
		}
		StringList getHomeWorldNames(String currentLanguage)	{
			StringList homeWorldNames;
			if (DEFAULT_LANGUAGE.equals(currentLanguage))
				homeWorldNames = settingMap.getList(ROOT + HomeWorld.KEY);
			else
				homeWorldNames = settingMap.getList(ROOT + HomeWorld.KEY + currentLanguage);
			homeWorldNames.setSelectedIndex(0);
			return homeWorldNames;
		}
		StringList getFullCivNames(String currentLanguage, StringList namedCiv)	{
			StringList fullCivNames = new StringList();
			for (int i=0; i<namedCiv.size(); i++)
				if (isCivAutonomous(currentLanguage, i))
					fullCivNames.add(namedCiv.get(i));
			return fullCivNames;
		}

		SpeciesAttributes getAttributes(String dir)	{
			SpeciesAttributes speciesAttributes = attributesMap.get(dir);
			if (speciesAttributes == null)
				attributesMap.put(dir, new SpeciesAttributes(dir));
			return attributesMap.get(dir);
		}
		boolean hasAnim()	{ return animSkills != null; }
		void insert(int idx, String name)	{
			// add new civilization to every language
			for (SpeciesAttributes attributes : attributesMap.values())
				attributes.insert(idx, name);
		}
		void delete(int idx)	{ // Remove an civilization
			for (SpeciesAttributes attributes : attributesMap.values())
				attributes.delete(idx);
		}
		private void fillFromAnim(boolean forced) {
			int idx = 0;
			for (String name : getCivilizationsNames()) {
				fillFromAnim(forced, idx, name);
				idx++;
			}
		}
		void fillFromAnim(boolean forced, int civIdx, String civName)	{
			if (animSkills == null)
				return;
			String currentLanguage = selectedLanguageDir();
			SpeciesAttributes dest = attributesMap.get(currentLanguage);
			if (dest == null)
				return;

			// Look if Animation has same civilization name
			AnimationId animationId = (AnimationId)settingMap.get(ROOT + AnimationId.KEY);
			StringList animNames = animSkills.civilizationNames();
			int animIdx = animNames.indexOfIgnoreCase(civName);
			if (animIdx < 0) {
				animIdx =  animationId.settingValue();
				if (animIdx < 0)
					if (animNames.size() >= civIdx)
						animIdx = civIdx;
					else
						animIdx = 0;
			}
			else
				animationId.set(animIdx);
			dest.fillFromAnim(forced, civIdx, animIdx);
		}
		void fillLabelsFromNames(String langDir, int civIdx, boolean forced)	{
			SpeciesAttributes dest = attributesMap.get(langDir);
			if (dest == null)
				return;
			switch (langDir) {
			case DEFAULT_LANGUAGE:
				fillLabelsFromNamesEN(civIdx, forced);
				return;
			case "fr":
				fillLabelsFromNamesFR(civIdx, forced);
				return;
			}
		}
		void copyFromLanguage(String langSrc, String langDest, int civIdx, boolean forced)	{
			SpeciesAttributes src = attributesMap.get(langSrc);
			SpeciesAttributes dest = attributesMap.get(langDest);
			dest.copyFromLanguage(src, civIdx, forced);
		}
		boolean isAnimAutonomous(String dir)	{
			SpeciesAttributes attributes = attributesMap.get(dir);
			if (attributes == null)
				return false;
			return attributes.isAnimAutonomous();
		}
		boolean isAnimAutonomous(String dir, int idx)	{
			SpeciesAttributes attributes = attributesMap.get(dir);
			if (attributes == null)
				return false;
			return attributes.isAnimAutonomous(idx);
		}
		private boolean isCivAutonomous(String dir, int idx)	{
			SpeciesAttributes attributes = attributesMap.get(dir);
			if (attributes == null)
				return false;
			return attributes.isCivAutonomous(idx);
		}
		private void fillLabelsFromNamesEN(int civIdx, boolean forced)	{
			String langKey = "";
			RaceName nameSetting = (RaceName)settingMap.get(ROOT + RaceName.KEY + langKey);
			StringList nameList = nameSetting.getList();
			String name = nameList.get(civIdx, "");

			LeaderTitle titleSetting = (LeaderTitle)settingMap.get(ROOT + LeaderTitle.KEY + langKey);
			String title = titleSetting.settingValue();

			LeaderFullTitle fulltitleSetting = (LeaderFullTitle)settingMap.get(ROOT + LeaderFullTitle.KEY + langKey);
			String fulltitle = fulltitleSetting.settingValue();
			if (fulltitle.isEmpty() || !fulltitleSetting.isAnimAutonomous(civIdx)) {
				fulltitle = title;
				fulltitleSetting.set(fulltitle);
			}

			// _empire
			CivilizationDialogLabel dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_empire" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, name + " " + title);
				}
			}

			// _race
			dialogLabel = (CivilizationDialogLabel)settingMap.get(ROOT + "_race" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, name);
				}
			}

			// _race_plural
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_race_plural" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, name + "s");
				}
			}

			// _title 
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_title " + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, title);
				}
			}

			// _nameTitle 
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_nameTitle " + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, fulltitle);
				}
			}
		}
		private void fillLabelsFromNamesFR(int civIdx, boolean forced)	{
			final String langKey = "fr";
			RaceName nameSetting = (RaceName)settingMap.get(ROOT + RaceName.KEY + langKey);
			StringList nameList = nameSetting.getList();
			String name = nameList.get(civIdx, "");

			LeaderTitle titleSetting = (LeaderTitle)settingMap.get(ROOT + LeaderTitle.KEY + langKey);
			String title = titleSetting.settingValue();

			LeaderFullTitle fulltitleSetting = (LeaderFullTitle)settingMap.get(ROOT + LeaderFullTitle.KEY + langKey);
			String fulltitle = fulltitleSetting.settingValue();
			if (fulltitle.isEmpty() || !fulltitleSetting.isAnimAutonomous(civIdx)) {
				fulltitle = title;
				fulltitleSetting.set(fulltitle);
			}

			String government = "L'empire";
			if (animSkills != null) {
				String allGov = animSkills.raceLabels().label("_empire");
				StringList govList = new StringList(allGov);
				String fullName = govList.getFirst();
				StringList words = new StringList(fullName, " ");
				words.removeLast();
				government = words.asString(" ");
			}
			String governmentOf = "de l'empire";
			if (animSkills != null) {
				String allGov = animSkills.raceLabels().label("_empireof");
				StringList govList = new StringList(allGov);
				String fullName = govList.getFirst();
				StringList words = new StringList(fullName, " ");
				words.removeLast();
				governmentOf = words.asString(" ");
			}

			// _empire
			CivilizationDialogLabel dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_empire" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, government + " " + name);
				}
			}

			// _empireof
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_empireof" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, governmentOf + " " + name);
				}
			}

			// _raceadjec
			dialogLabel = (CivilizationDialogLabel)settingMap.get(ROOT + "_raceadjec" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, name);
				}
			}

			// _raceadjecF
			dialogLabel = (CivilizationDialogLabel)settingMap.get(ROOT + "_raceadjecF" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, name + "e");
				}
			}

			// _race_pluralnoun
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_race_pluralnoun" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, "les " + name + "s");
				}
			}

			// _race_pluralnounof
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_race_pluralnounof" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, "des " + name + "s");
				}
			}

			// _race_pluralnounto
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_race_pluralnounto" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, "aux " + name + "s");
				}
			}

			// _race_pluraladjecF
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_race_pluraladjecF" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, name + "es");
				}
			}

			// _race_pluraladjec
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_race_pluraladjec" + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, name + "s");
				}
			}

			// _title 
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_title " + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, title);
				}
			}

			// _nameTitle 
			dialogLabel = (CivilizationDialogLabel) settingMap.get(ROOT + "_nameTitle " + langKey);
			if (dialogLabel != null) {
				String value = dialogLabel.settingValue(civIdx);
				if (forced || value.isEmpty() || !dialogLabel.isAnimAutonomous(civIdx)) {
					dialogLabel.selectedValue(civIdx, fulltitle);
				}
			}
		}
	}
	// -#-
	// #==================== RaceKey ====================
	//
	protected String raceKey()	{ return ((RaceKey) settingMap.get(ROOT + RaceKey.RACE_KEY)).settingValue(); }
	protected String fileName()	{ return raceKey() + EXT; }
	class RaceKey extends SettingString	{
		private static final String RACE_KEY = "RACE_KEY";
		private static void setKey(DynOptions opts, String key)	{ opts.setString(ROOT + RACE_KEY, key); }
		private static String getKey(DynOptions opts)			{ return opts.getString(ROOT + RACE_KEY, ""); }
		static String fileToKey(File file)				{
			String key = file.getPath();
			key = key.substring(0, key.length() - EXT.length());
			String dir = speciesDirectoryPath();
			if (dir.length() <= key.length()) {
				String parent = key.substring(0, dir.length());
				boolean same = parent.equals(dir);
				if(same) {
					if (dir.length() == key.length())
						key = "";
					else
						key = key.substring(dir.length()+1, key.length());
				}
			}
			return key;
		}
		static String valid(DynOptions opt, File file)	{
			String optKey	= getKey(opt);
			String fileKey	= fileToKey(file);
			if (!optKey.equalsIgnoreCase(fileKey)) {
				setKey(opt, fileKey);
				DynOptions.saveOptions(opt, file);
			}
			return fileKey;
		}

		RaceKey() {
			super(ROOT, RACE_KEY, defaultRaceKey, 1);
			randomStr(RANDOMIZED_RACE_KEY);
		}
		@Override public boolean toggle(MouseEvent e, MouseWheelEvent w, int idx)	{ return callUI(); }
		@Override public void settingToSkill(SpeciesSkills skills) { skills.id = settingValue(); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.id); }
	}
	// -#-
	// #==================== RaceName ====================
	//
	class RaceName extends SettingStringList {
		static final String KEY = "RACE_NAME";
		RaceName(String langDir) {
			super(ROOT, KEY, "", langDir);
			randomStr("Random Race");
		}
		@Override public void pushToSkills(SpeciesSkills skills)	{
			skills.parseCivilizationNames(settingValue());
			skills.setupName = skills.setupName();
		}
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(String.join(",", skills.civilizationNames())); }
	}
	// ==================== RaceDescription ====================
	//
	class RaceDescription extends SettingStringLanguage {
		private static String KEY(int i)	{ return "RACE_DESC_" + i; }
		private final int id;
		RaceDescription(int i, String langDir) {
			super(ROOT, KEY(i), "Description "+i, i==3? 4:2, langDir);
			id = i;
			randomStr("Randomized");
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
			for (String s : IGameOptions.specificAIset().getAliens())
				put(s, s.toUpperCase(), 0f, s);
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
		private void pushSetting(SpeciesSkills skills) {
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
		private void pullSetting(SpeciesSkills skills) {
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

		private void pushSetting(SpeciesSkills skills) {
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
		private void pullSetting(SpeciesSkills skills) {
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
			super(ROOT, "CREDIT", 0, 0, 35, 1, 5, 20, DIFFERENCE, new float[]{0f, .8f}, new float[]{0f, .8f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.bCBonus(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.bCBonus() * 100)); }
	}
	// ==================== HitPointsBonus ====================
	//
	class HitPointsBonus extends SettingInteger {
		// big = good
		HitPointsBonus() {
			super(ROOT, "HIT_POINTS", 100, 50, 200, 1, 5, 20, DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .6f});
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
			super(ROOT, "SHIP_SPACE", 100, 80, 175, 1, 5, 20, DIFFERENCE, new float[]{0f, 1f}, new float[]{0f, 2f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.shipSpaceFactor(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.shipSpaceFactor() * 100)); }
	}
	// ==================== MaintenanceBonus ====================
	//
	class MaintenanceBonus extends SettingInteger {
		// Big = bad
		public MaintenanceBonus() {
			super(ROOT, "MAINTENANCE", 100, 50, 200, 1, 5, 20, DIFFERENCE, new float[]{0f, -.2f}, new float[]{0f, -.4f});
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
	// ==================== PopulationBonusPct ====================
	//
	@SuppressWarnings("unused")
	class PopulationBonusPct extends SettingInteger { // BR: May be implemented later... !High risk of bugs!
		PopulationBonusPct() {
			super(ROOT, "POPULATION_BONUS", 100, 70, 150, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .8f}, new float[]{0f, 1.4f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.populationBonusPct(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.populationBonusPct()); }
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
				boolean oldBooleanValue = srcOptions.getBoolean(dynOptionIndex(), false);
				String defaultValue = oldBooleanValue? "All" : "No";
				setFromCfgValue(srcOptions.getString(dynOptionIndex(), defaultValue));
			}
		}
	}
	// ==================== PopGrowRate ====================
	//
	class PopGrowRate extends SettingInteger {
		PopGrowRate() {
			super(ROOT, "POP_GROW_RATE", 100, 50, 200, 1, 5, 20, DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .3f});
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.growthRateMod(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round (skills.growthRateMod() * 100)); }
	}
	// ==================== ShipAttack ====================
	//
	class ShipAttack extends SettingInteger {
		ShipAttack() {
			super(ROOT, "SHIP_ATTACK", 0, -1, 5, 1, 1, 1, DIFFERENCE, new float[]{0f, 3f}, new float[]{0f, 5f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.shipAttackBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.shipAttackBonus()); }
	}
	// ==================== ShipDefense ====================
	//
	class ShipDefense extends SettingInteger {
		ShipDefense() {
			super(ROOT, "SHIP_DEFENSE", 0, -1, 5, 1, 1, 1, DIFFERENCE, new float[]{0f, 1.5f, 1.5f}, new float[]{0f, 6f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.shipDefenseBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.shipDefenseBonus()); }
	}
	// ==================== ShipInitiative ====================
	//
	class ShipInitiative extends SettingInteger {
		ShipInitiative() {
			super(ROOT, "SHIP_INITIATIVE", 0, -1, 5, 1, 1, 1, DIFFERENCE, new float[]{5f, 1f}, new float[]{0f, 6f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.shipInitiativeBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.shipInitiativeBonus()); }
	}
	// ==================== GroundAttack ====================
	//
	class GroundAttack extends SettingInteger {
		GroundAttack() {
			super(ROOT, "GROUND_ATTACK", 0, -20, 30, 1, 5, 20, DIFFERENCE, new float[]{0f, 1.25f}, new float[]{0f, 0.75f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.groundAttackBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.groundAttackBonus()); }
	}
	// ==================== SpyCost ====================
	//
	class SpyCost extends SettingInteger {
		SpyCost() {
			super(ROOT, "SPY_COST", 100, 50, 200, 1, 5, 20, DIFFERENCE, new float[]{0f, -.1f}, new float[]{0f, -.2f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.spyCostMod(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.spyCostMod() * 100)); }
	}
	// ==================== SpySecurity ====================
	//
	class SpySecurity extends SettingInteger {
		SpySecurity() {
			super(ROOT, "SPY_SECURITY", 0, -20, 40, 1, 5, 20, DIFFERENCE, new float[]{0f, 1f}, new float[]{0f, 2f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.internalSecurityAdj(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.internalSecurityAdj() * 100)); }
	}
	// ==================== SpyInfiltration ====================
	//
	class SpyInfiltration extends SettingInteger {
		SpyInfiltration() {
			super(ROOT, "SPY_INFILTRATION", 0, -20, 40, 1, 5, 20, DIFFERENCE, new float[]{0f, 1.25f}, new float[]{0f, 2.5f});
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
			super(ROOT, "DIPLOMACY_TRADE", 0, -30, 30, 1, 5, 20, DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .3f});
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
			super(ROOT, "DIPLO_POS_DP", 100, 70, 200, 1, 5, 20, DIFFERENCE, new float[]{0f, .3f}, new float[]{0f, .8f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.positiveDPMod(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.positiveDPMod() * 100)); }
	}
	// ==================== DiplomacyBonus ====================
	//
	class DiplomacyBonus extends SettingInteger {
		DiplomacyBonus() {
			super(ROOT, "DIPLOMACY_BONUS", 0, -50, 100, 1, 5, 20, DIFFERENCE, new float[]{0f, .1f}, new float[]{0f, .2f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.diplomacyBonus(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.diplomacyBonus()); }
	}
	// ==================== DiplomacyCouncil ====================
	//
	class DiplomacyCouncil extends SettingInteger {
		DiplomacyCouncil() {
			super(ROOT, "DIPLOMACY_COUNCIL", 0, -25, 25, 1, 5, 20, DIFFERENCE, new float[]{0f, .2f}, new float[]{0f, .2f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.councilBonus(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.councilBonus() * 100)); }
	}
	// ==================== RelationDefault ====================
	//
	class RelationDefault extends SettingInteger {
		RelationDefault() {
			super(ROOT, "RELATION_DEFAULT", 0, -10, 10, 1, 2, 4, DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .4f});
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
			super(ROOT, "PROD_WORKER", 100, 70, 300, 1, 5, 20, DIFFERENCE, new float[]{0f, .8f, 0f}, new float[]{0f, 0.8f, 0.01f});
			initOptionsText();
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.workerProductivityMod(settingValue()/100f); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.workerProductivityMod() * 100)); }
	}
	// ==================== ProdControl ====================
	//
	class ProdControl extends SettingInteger {
		ProdControl() {
			super(ROOT, "PROD_CONTROL", 0, -1, 4, 1, 1, 1, DIFFERENCE, new float[]{0f, 15f, 0f}, new float[]{0f, 30f, 0f});
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
	// -#-
	// #==================== TechResearch  And Discovery====================
	//
	public class Technologies {
		TechResearch	techResearch	= new TechResearch();
		TechDiscovery	techDiscovery	= new TechDiscovery();

		// ==================== TechResearch ====================
		//
		public class TechResearch extends SettingInteger {

			ResearchComputer		computer	= new ResearchComputer();
			ResearchConstruction	construction= new ResearchConstruction();
			ResearchForceField		forceField	= new ResearchForceField();
			ResearchPlanet			planet		= new ResearchPlanet();
			ResearchPropulsion		propulsion	= new ResearchPropulsion();
			ResearchWeapon			weapon		= new ResearchWeapon();

			TechResearch() {
				super(ROOT, "TECH_RESEARCH", 100, 60, 200, 1, 5, 20, DIFFERENCE, new float[]{0f, 0.7f, 0.004f}, new float[]{0f, 1.0f, 0.006f});
				hasNoCost(false);
				initOptionsText();
			}
			@Override public void settingToSkill(SpeciesSkills skills) { skills.researchBonusPct(settingValue()/100f); }
			@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.researchBonusPct() * 100)); }
			@Override public String guiSettingDisplayStr() {
				return getLabel() + ": " + guideSelectedValue() + " " + costString(cost());
			}
			@Override protected boolean next(Integer i) {
				super.next(i);
				if (settingText() == null) { // TODO BR: remove once EditCustomSpecies is obsolete
					// To force refresh the display for those parameters 
					computer.updated(true);
					construction.updated(true);
					forceField.updated(true);
					planet.updated(true);
					propulsion.updated(true);
					weapon.updated(true);
					return true;
				}
				computer.settingText().repaint(computer.guiSettingDisplayStr());
				construction.settingText().repaint(construction.guiSettingDisplayStr());
				forceField.settingText().repaint(forceField.guiSettingDisplayStr());
				planet.settingText().repaint(planet.guiSettingDisplayStr());
				propulsion.settingText().repaint(propulsion.guiSettingDisplayStr());
				weapon.settingText().repaint(weapon.guiSettingDisplayStr());
				return false;
			}
			@Override public boolean toggle(MouseWheelEvent e) {
				super.toggle(e);
				return true;
			}
			@Override public void enabledColor(float cost) { super.enabledColor(cost()); }

			String costString(float cost) {
				String str = "(<";
				str +=  new DecimalFormat("0.0").format(cost);
				return str + ">)";
			}
			public float cost() {
				return computer.settingCost()
						+ construction.settingCost()
						+ forceField.settingCost()
						+ planet.settingCost()
						+ propulsion.settingCost()
						+ weapon.settingCost();
			}
			private void markTechResearchForRefresh()	{ updated(true); }

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
			public class SettingResearch extends SettingInteger {
				// Cost: smaller = better
				private static final float	c0 = 0;
				private static final float	c1 = -18.02331959f;
				private static final float	c2 = 9.56463523f;
				private static final float	c3 = -4.365405984f;
				private static final float	c4 = 0.824090347f;
				private static final float	baseCostDefault = 100f;
				private static final float	norm = 100f;

				private SettingResearch(String nameLangLabel) {
					super(ROOT, nameLangLabel, 100, 50, 200, 1, 5, 20, NORMALIZED, new float[]{c0, c1, c2, c3, c4}, null);
				}
				@Override public float settingCost() { return settingCost(combinedValue()); }
				@Override public float settingCost(Integer value) {
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
				@Override protected void selectedValue(Integer newValue) {
					super.selectedValue(newValue);
					markTechResearchForRefresh();
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
		public class TechDiscovery extends SettingInteger {

			DiscoveryComputer	  computer		= new DiscoveryComputer();
			DiscoveryConstruction construction	= new DiscoveryConstruction();
			DiscoveryForceField	  forceField	= new DiscoveryForceField();
			DiscoveryPlanet		  planet		= new DiscoveryPlanet();
			DiscoveryPropulsion	  propulsion	= new DiscoveryPropulsion();
			DiscoveryWeapon		  weapon		= new DiscoveryWeapon();

			TechDiscovery() {
				super(ROOT, "TECH_DISCOVERY", 50, 0, 100, 1, 5, 20, DIFFERENCE, new float[]{0f, .5f}, new float[]{0f, 0.5f});
				hasNoCost(false);
				initOptionsText();
			}
			@Override public void settingToSkill(SpeciesSkills skills) { skills.techDiscoveryPct(settingValue()/100f); }
			@Override public void skillToSetting(SpeciesSkills skills) { set(Math.round(skills.techDiscoveryPct() * 100)); }
			@Override public String guiSettingDisplayStr() {
				return getLabel() + ": " + guideSelectedValue() + " " + costString(cost());
			}
			@Override protected boolean next(Integer i) {
				super.next(i);
				if (settingText() == null) { // TODO BR: remove once EditCustomSpecies is obsolete
					// To force refresh the display for those parameters 
					computer.updated(true);
					construction.updated(true);
					forceField.updated(true);
					planet.updated(true);
					propulsion.updated(true);
					weapon.updated(true);
					return true;
				}
				computer.settingText().repaint(computer.guiSettingDisplayStr());
				construction.settingText().repaint(construction.guiSettingDisplayStr());
				forceField.settingText().repaint(forceField.guiSettingDisplayStr());
				planet.settingText().repaint(planet.guiSettingDisplayStr());
				propulsion.settingText().repaint(propulsion.guiSettingDisplayStr());
				weapon.settingText().repaint(weapon.guiSettingDisplayStr());
				return false;
			}
			@Override public boolean toggle(MouseWheelEvent e) {
				super.toggle(e);
				return true;
			}
			@Override public void enabledColor(float cost) { super.enabledColor(cost()); }

			String costString(float cost) {
				String str = "(<";
				str +=  new DecimalFormat("0.0").format(cost);
				return str + ">)";
			}
			public float cost() {
				return computer.settingCost()
						+ construction.settingCost()
						+ forceField.settingCost()
						+ planet.settingCost()
						+ propulsion.settingCost()
						+ weapon.settingCost();
			}
			private void markTechDiscoveryForRefresh()	{ updated(true); }

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
			public class SettingDiscovery extends SettingInteger {
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
				@Override public float settingCost(Integer value) {
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
				@Override protected void selectedValue(Integer newValue) {
					super.selectedValue(newValue);
					markTechDiscoveryForRefresh();
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
	// -#-
	// #==================== Names and Labels ====================
	//
	// ==================== Language list ====================
	//
	class LanguageList extends SettingString {
		static final String KEY = "LANGUAGE_LIST";
		private static String DEFAULT_VALUE() { return LanguageManager.selectedLanguageDir(); }
		LanguageList()	{ super(ROOT, KEY, DEFAULT_VALUE()); }
		@Override public void settingToSkill(SpeciesSkills skills) { skills.languageList(settingValue()); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.languageList()); }
	}
	// ==================== AnimReady ====================
	//
	class AnimReady extends SettingStringList {
		private static final String KEY = "ANIM_READY";
		private static final String DEFAULT_VALUE = "";
		AnimReady(String langDir)					{ super(ROOT, KEY, DEFAULT_VALUE, langDir); }
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.isAnimAutonomous(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.isAnimAutonomous()); }
	}
	// ==================== HomeWorld ====================
	//
	private class HomeWorld extends SettingStringList {
		private static final String KEY = "HOME_WORLD";
		private static final String DEFAULT_VALUE = "";
		private HomeWorld(String langDir)				{ super(ROOT, KEY, DEFAULT_VALUE, langDir); }
		@Override public boolean isCivAutonomous(int idx)	{ return true; }
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.parseHomeWorlds(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.homeSystemNames().asString()); }
	}
	// ==================== LeaderName ====================
	//
	private class LeaderName extends SettingStringList {
		private static final String KEY = "LEADER_NAME";
		private static final String DEFAULT_VALUE = "";
		private LeaderName(String langDir)				{ super(ROOT, KEY, DEFAULT_VALUE, langDir); }
		@Override public boolean isCivAutonomous(int idx)	{ return true; }
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.parseLeaderNames(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.leaderNames().asString()); }
	}
	// ==================== EmpireName ====================
	//
	class EmpireName extends CivilizationDialogLabel {
		private static final String PREVIOUS_KEY = "RACE_EMPIRE_NAME";
		private static final String KEY = "_empire";
		EmpireName(String langDir) {
			super(KEY, langDir);
			randomStr(CR_EMPIRE_NAME_RANDOM);
		}
		private String validate(DynamicOptions srcOptions)	{
			String value = getFromOption(srcOptions);
			if (value != null) // UpToDate
				return value;
			//Update the options
			value = srcOptions.getString(ROOT+PREVIOUS_KEY, defaultValue());
			setOption(srcOptions, value);
			return value;
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions) { // For backward compatibility
			if (srcOptions != null && !isSpacer())
				extendedSet(validate(srcOptions));
		}
	}
	// ==================== LeaderTitle ====================
	//
	class LeaderTitle extends SettingStringLanguage {
		private static final String KEY = "LEADER_TITLE";
		LeaderTitle(String langDir)		{ super(ROOT, KEY, "", langDir); }
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.title(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.title()); }
	}
	// ==================== LeaderFullTitle ====================
	//
	class LeaderFullTitle extends SettingStringLanguage {
		private static final String KEY = "LEADER_FULLTITLE";
		LeaderFullTitle(String langDir)		{ super(ROOT, KEY, "", langDir); }
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.fullTitle(settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.fullTitle()); }
	}
	// ==================== ShipNames ====================
	//
	class ShipNames extends SettingStringLanguage {
		private static final String BASE_KEY = "SHIP_NAMES";
		private static final String KEY(int size)	{ return BASE_KEY + "_" + size;}
		final int shipSize;
		ShipNames(int size, String langDir)	{
			super(ROOT, KEY(size), "", langDir);
			shipSize = size;
		}
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.parseShipName(shipSize, settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.getShipNames(shipSize)); }
	}
	// -#-
	// #====================
	// List of unique Names
	//
	class SpeciesAttributes implements ICRSettings { // for one Language
		private final String labelKey;
		SpeciesNameItems		speciesNameItems;
		SpeciesDescriptionItems	speciesDescriptionItems;
		SpeciesShipNamesItems	speciesShipNamesItems;
		SpeciesLabelItems		speciesLabelItems;
		CivilizationNameItems	civilizationNameItems;
		CivilizationLabelItems	civilizationLabelItems;

		SpeciesAttributes(String langDir)	{
			labelKey = langDir;
			speciesNameItems		= new SpeciesNameItems(labelKey);
			speciesDescriptionItems	= new SpeciesDescriptionItems(labelKey);
			speciesShipNamesItems	= new SpeciesShipNamesItems(labelKey);
			speciesLabelItems		= new SpeciesLabelItems(labelKey);
			civilizationNameItems	= new CivilizationNameItems(labelKey);
			civilizationLabelItems	= new CivilizationLabelItems(labelKey);
		}
		@Override public String  getLabel()	{ return ""; }
		@Override public void updateOption(DynamicOptions destOptions) {
			speciesNameItems.updateOption(destOptions);
			speciesDescriptionItems.updateOption(destOptions);
			speciesShipNamesItems.updateOption(destOptions);
			speciesLabelItems.updateOption(destOptions);
			civilizationNameItems.updateOption(destOptions);
			civilizationLabelItems.updateOption(destOptions);
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions) {
			speciesNameItems.updateOptionTool(srcOptions);
			speciesDescriptionItems.updateOptionTool(srcOptions);
			speciesShipNamesItems.updateOptionTool(srcOptions);
			speciesLabelItems.updateOptionTool(srcOptions);
			civilizationNameItems.updateOptionTool(srcOptions);
			civilizationLabelItems.updateOptionTool(srcOptions);
		}
		private void insert(int idx, String name)	{
			civilizationNameItems.insert(idx, name);
			civilizationLabelItems.insert(idx);
		}
		private void delete(int idx)	{
			civilizationNameItems.delete(idx);
			civilizationLabelItems.delete(idx);
		}
		private boolean isCivAutonomous(int idx)	{ return civilizationNameItems.isCivAutonomous(idx) && civilizationLabelItems.isCivAutonomous(idx); }
		private boolean isAnimAutonomous(int idx)	{ return civilizationNameItems.isAnimAutonomous(idx) && civilizationLabelItems.isAnimAutonomous(idx); }
		private boolean isAnimAutonomous()			{ return civilizationNameItems.isAnimAutonomous() && civilizationLabelItems.isAnimAutonomous(); }
		public boolean isFilled()	{
			boolean filled = speciesNameItems.isFilled();
			filled &= speciesDescriptionItems.isFilled();
			filled &= civilizationLabelItems.isFilled();
			filled &= civilizationNameItems.isFilled();
			filled &= speciesLabelItems.isFilled();
			return filled;
		}
		private void copyFromLanguage(SpeciesAttributes langSrc, int civIdx, boolean forced)	{
			speciesNameItems.copyFromLanguage(langSrc.speciesNameItems, civIdx, forced);
			speciesDescriptionItems.copyFromLanguage(langSrc.speciesDescriptionItems, civIdx, forced);
			speciesShipNamesItems.copyFromLanguage(langSrc.speciesShipNamesItems, civIdx, forced);
			speciesLabelItems.copyFromLanguage(langSrc.speciesLabelItems, civIdx, forced);
			civilizationNameItems.copyFromLanguage(langSrc.civilizationNameItems, civIdx, forced);
			civilizationLabelItems.copyFromLanguage(langSrc.civilizationLabelItems, civIdx, forced);			
		}
		private void fillFromAnim(boolean forced, int civIdx, int animIdx)	{
			speciesNameItems.fillFromAnim(forced, civIdx, animIdx);
			speciesDescriptionItems.fillFromAnim(forced, civIdx, animIdx);
			speciesShipNamesItems.fillFromAnim(forced, civIdx, animIdx);
			speciesLabelItems.fillFromAnim(forced, civIdx, animIdx);
			civilizationNameItems.fillFromAnim(forced, civIdx, animIdx);
			civilizationLabelItems.fillFromAnim(forced, civIdx, animIdx);
		}
	}
	// ====================
	// Species List
	//
	class SpeciesNameItems extends ICSSettingsStringList {
		private static final long serialVersionUID = 1L;
		private SpeciesNameItems(String langDir)	{
			super(langDir);
			SettingStringLanguage setting = (SettingStringLanguage) settingMap.get(ROOT + LeaderTitle.KEY + langKey);
			if (setting == null) {
				setting	= new LeaderTitle(langDir);
				settingMap.addAttribute(setting);
			}
			setting.skillToSetting(race());
			add(setting);

			setting = (SettingStringLanguage) settingMap.get(ROOT + LeaderFullTitle.KEY + langKey);
			if (setting == null) {
				setting	= new LeaderFullTitle(langDir);
				settingMap.addAttribute(setting);
			}
			setting.skillToSetting(race());
			add(setting);
		}
	}
	class SpeciesDescriptionItems extends ICSSettingsStringList {
		private static final long serialVersionUID = 1L;
		private static final int[] SEQ = new int[] {1, 2, 4, 3};
		private SpeciesDescriptionItems(String langDir)	{
			super(langDir);
			String langKey = toLanguageKey(langDir);
			for (int i : SEQ) {
				if (isForShow()) {
					SettingStringLanguage setting = (SettingStringLanguage) settingMap.get(ROOT + RaceDescription.KEY(i));
					if (setting == null) {
						setting	= new RaceDescription(i, selectedLanguageDir());
						settingMap.addAttribute(setting);
					}
					setting.skillToSetting(race());
					add(setting);
				}
				else {
					SettingStringLanguage setting = (SettingStringLanguage) settingMap.get(ROOT + RaceDescription.KEY(i) + langKey);
					if (setting == null) {
						setting	= new RaceDescription(i, langDir);
						settingMap.addAttribute(setting);
					}
					setting.skillToSetting(race());
					add(setting);
				}
			}
		}
	}
	class SpeciesShipNamesItems extends ICSSettingsStringList {
		private static final long serialVersionUID = 1L;
		private static final int[] SEQ = new int[] {0, 1, 2, 3};
		private SpeciesShipNamesItems(String langDir)	{
			super(langDir);
			String langKey = toLanguageKey(langDir);
			for (int i : SEQ) {
				if (isForShow()) {
					SettingStringLanguage setting = (SettingStringLanguage) settingMap.get(ROOT + ShipNames.KEY(i));
					if (setting == null) {
						setting	= new ShipNames(i, selectedLanguageDir());
						settingMap.addAttribute(setting);
					}
					setting.skillToSetting(race());
					add(setting);
				}
				else {
					SettingStringLanguage setting = (SettingStringLanguage) settingMap.get(ROOT + ShipNames.KEY(i) + langKey);
					if (setting == null) {
						setting	= new ShipNames(i, langDir);
						settingMap.addAttribute(setting);
					}
					setting.skillToSetting(race());
					add(setting);
				}
			}
		}
	}
	class SpeciesLabelItems extends ICSSettingsStringList {
		private static final long serialVersionUID = 1L;
		private SpeciesLabelItems(String langDir)	{
			super(langDir);
			StringList list = getOnePerSpeciesMap().get(langDir);
			for (String label : list) {
				SpeciesDialogLabel setting = (SpeciesDialogLabel) settingMap.get(ROOT + label + langKey);
				if (setting == null) {
					setting	= new SpeciesDialogLabel(label, langDir);
					settingMap.addAttribute(setting);
				}
				setting.skillToSetting(race());
				add(setting);
			}
		}
		@Override public void copyFromLanguage(ICSSettingsStringList langSrc, int civIdx, boolean forced)	{
			for (SettingStringLanguage dest : this) {
				String destValue = dest.settingValue(civIdx);
				if (!forced && (destValue != null && !destValue.isEmpty()))
					continue;
				for (SettingStringLanguage src : langSrc) {
					if (!dest.nameLangLabel.equals(src.nameLangLabel))
						continue;
					dest.selectedValue(civIdx, src.settingValue(civIdx));
				}
			}
		}
	}
	private class ICSSettingsStringList extends ArrayList<SettingStringLanguage> implements ICRSettings {
		protected final String langKey;
		private ICSSettingsStringList(String langDir)	{
			langKey = toLanguageKey(langDir);
		}
		static final long serialVersionUID = 1L;
		@Override public String  getLabel()	{ return ""; }
		@Override public void updateOption(DynamicOptions destOptions) {
			for (ICRSettings item : this)
				item.updateOption(destOptions);
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions) {
			for (ICRSettings item : this)
				item.updateOptionTool(srcOptions);
		}
		void insert(int idx)	{
			for (SettingStringLanguage setting : this)
				setting.addValue(idx, "");
		}
		void delete(int idx)	{
			for (SettingStringLanguage setting : this)
				setting.removeValue(idx);
		}
		public boolean isFilled()	{
			for (SettingStringLanguage setting : this)
				if (!setting.isFilled())
					return false;
			return true;
		}
		public boolean isAnimAutonomous()	{
			for (SettingStringLanguage setting : this)
				if (!setting.isAnimAutonomous())
					return false;
			return true;
		}
		public boolean isAnimAutonomous(int idx)	{
			for (SettingStringLanguage setting : this)
				if (!setting.isAnimAutonomous(idx))
					return false;
			return true;
		}
		public boolean isCivAutonomous(int idx)	{
			for (SettingStringLanguage setting : this)
				if (!setting.isCivAutonomous(idx))
					return false;
			return true;
		}
	public void copyFromLanguage(ICSSettingsStringList langSrc, int civIdx, boolean forced)	{
			int size = Math.min(size(), langSrc.size());
			for (int i=0; i<size; i++) {
				SettingString dest = get(i);
				String destValue = dest.settingValue(civIdx);
				if (forced || destValue == null || destValue.isEmpty()) { // do not override
					SettingString src = langSrc.get(i);
					dest.selectedValue(civIdx, src.settingValue(civIdx));
				}
			}
		}
		void fillFromAnim(boolean forced, int civIdx, int animIdx) {
			for (SettingStringLanguage setting : this)
				setting.fillFromAnim(forced, civIdx, animIdx);;
		}
	}
	// ==================== Multi Language Settings ====================
	//
	// ====================
	// Empire List
	//
	class CivilizationNameItems extends ICSSettingsStringList {
		private static final long serialVersionUID = 1L;
		private CivilizationNameItems(String langDir)	{
			super(langDir);
			SettingStringList setting = (SettingStringList) settingMap.get(ROOT + RaceName.KEY + langKey);
			if (setting == null) {
				setting	= new RaceName(langDir);
				settingMap.addAttribute(setting);
			}
			setting.skillToSetting(race());
			add(setting);
			setting = (SettingStringList) settingMap.get(ROOT + HomeWorld.KEY + langKey);
			if (setting == null) {
				setting	= new HomeWorld(langDir);
				settingMap.addAttribute(setting);
			}
			setting.skillToSetting(race());
			add(setting);
			setting = (SettingStringList) settingMap.get(ROOT + LeaderName.KEY + langKey);
			if (setting == null) {
				setting	= new LeaderName(langDir);
				settingMap.addAttribute(setting);
			}
				setting.skillToSetting(race());
			add(setting);
		}
		private void insert(int idx, String raceName)	{
			String value = raceName;
			for (SettingStringLanguage setting : this) {
				setting.addValue(idx, value);
				value = "";
			}
		}
	}
	class CivilizationLabelItems extends ICSSettingsStringList { // DialogLabels
		private static final long serialVersionUID = 1L;
		private CivilizationLabelItems(String langDir)	{
			super(langDir);
			StringList list = getOnePerEmpireMap().get(langDir);
			for (String label : list) {
				CivilizationDialogLabel setting = (CivilizationDialogLabel) settingMap.get(ROOT + label + langKey);
				if (setting == null) {
					setting	= new CivilizationDialogLabel(label, langDir);
					settingMap.addAttribute(setting);
				}
				setting.skillToSetting(race());
				add(setting);
			}
		}
		@Override public void copyFromLanguage(ICSSettingsStringList langSrc, int civIdx, boolean forced)	{
			for (SettingStringLanguage dest : this) {
				String destValue = dest.settingValue(civIdx);
				if (!forced && (destValue != null && !destValue.isEmpty()))
					continue;
				for (SettingStringLanguage src : langSrc) {
					if (!dest.nameLangLabel.equals(src.nameLangLabel))
						continue;
					dest.selectedValue(civIdx, src.settingValue(civIdx));
				}
			}
		}
	}
	// ==================== DialogLabel ====================
	//
	private class CivilizationDialogLabel extends SettingStringList {
		private CivilizationDialogLabel(String labelKey, String langDir)	{
			super(ROOT, labelKey, "", langDir);
			key = nameLangLabel + langKey;
		}
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.parseDialogLabel(nameLangLabel, settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.raceLabels().label(nameLangLabel)); }
	}
	private class SpeciesDialogLabel extends SettingStringLanguage {
		private SpeciesDialogLabel(String labelKey, String langDir)	{
			super(ROOT, labelKey, "", langDir);
			key = nameLangLabel + langKey;
		}
		@Override public void pushToSkills(SpeciesSkills skills)	{ skills.parseDialogLabel(nameLangLabel, settingValue()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ set(skills.raceLabels().label(nameLangLabel)); }
	}
	private abstract class SettingStringList extends SettingStringLanguage {
		private int lastSelectedIndex;
		private SettingStringList(String guiLangLabel, String nameLangLabel, String defaultValue, String langDir) {
			super(guiLangLabel, nameLangLabel, defaultValue, langDir);
		}
		@Override public String settingValue(int item)	{
			lastSelectedIndex = item;
			StringList list = getList();
			if (item >= list.size()) {
				// update the list content
				String val = list.get(item, "");
				setList(list);
				return val;
			}
			return getList().get(item, "");
		}
		@Override public void selectedValue(int id, String val)	{
			lastSelectedIndex = id;
			StringList list = getList();
			list.set(id, val);
			setList(list);
		}
		@Override public void addValue(int item, String val)	{
			lastSelectedIndex = item;
			StringList list = getList();
			list.add(item, val);
			setList(list);
		}
		@Override public String removeValue(int item)	{
			StringList list = getList();
			String str = list.remove(item);
			setList(list);
			lastSelectedIndex = min(item, list.size()-1);
			return str;
		}
		@Override public int getIndex()		{ return lastSelectedIndex; }
		@Override public void fillFromAnim(boolean forced, int civIdx, int animIdx)	{
			String value = settingValue(civIdx);
			if (!forced && (value != null && !value.isEmpty()))
				return;

			String animValues = animOptions.getString(dynOptionIndex());
			if (animValues == null)
				return;
			StringList animList = new StringList(animValues);
			if (animList.size() <= animIdx)
				animIdx = 0;
			String animValue = animList.get(animIdx, "");
			if (!animValue.isEmpty())
				selectedValue(civIdx, animValue);
		}
		@Override boolean isFilled()	{
			String value = settingValue();
			if (value == null || value.isEmpty())
				return false;
			StringList values = new StringList(value);
			for (String str : values)
				if (!isFilled(str))
					return false;
			return true;
		}
		@Override boolean isFilled(int idx)	{
			String value = settingValue();
			if (value == null || value.isEmpty())
				return false;
			StringList values = new StringList(value);
			if (values.size() <= idx)
				return false;
			return isFilled(values.get(idx));
		}
		private void setList(StringList list)	{ super.set(list.asString()); }
		StringList getList()					{ return new StringList(settingValue()); }
	}
	private abstract class SettingStringLanguage extends SettingString {
		// Selected language will be saved both the standard way and with the language ID
		// Other languages will be saved with their language Id.
		// Selected language will be loaded normally from the skills
		// Other languages will be loaded from their language Id.
		protected String langDir;
		protected String langKey;
		protected String nameLabel; // to override the base setting one  // also = getCfgLabel()
		protected String key; // also = getLangLabel
		protected String nameLangLabel;
		private SettingStringLanguage(String guiLangLabel, String nameLangLabel, String defaultValue, String langDir) {
			super(guiLangLabel, nameLangLabel + toLanguageKey(langDir), defaultValue);
			this.nameLangLabel = nameLangLabel;
			this.langDir = langDir;
			langKey = toLanguageKey(langDir);
			nameLabel = nameLangLabel + langKey;
			key = ROOT + nameLabel;
		}
		private SettingStringLanguage(String guiLangLabel, String nameLangLabel, String defaultValue, int lineNum ,String langDir) {
			super(guiLangLabel, nameLangLabel + toLanguageKey(langDir), defaultValue, lineNum);
			this.nameLangLabel = nameLangLabel;
			this.langDir = langDir;
			set(defaultValue);
			langKey = toLanguageKey(langDir);
			nameLabel = nameLangLabel + langKey;
			key = ROOT + nameLabel;
		}
		abstract void pushToSkills(SpeciesSkills skills);
		abstract void pullFromSkills(SpeciesSkills skills);

		void extendedSet(String value)	{ set(value); }
		private void fillFromAnim(boolean forced)	{
			// Animations are always in the selected language
			String value = settingValue();
			if (!forced && isFilled(value))
				return;
			String animValue = animOptions.getString(dynOptionIndex()); // Try to get linked animations value
			if (animValue != null)
				extendedSet(animValue);
		}
		public void fillFromAnim(boolean forced, int civIdx, int animIdx)	{ fillFromAnim(forced); }

		@Override public boolean toggle(MouseEvent e, MouseWheelEvent w, int idx)	{ return callUI(); }
		@Override public void settingToSkill(SpeciesSkills skills)	{
			String gameLang = selectedLanguageDir();
			if (langDir.equals(gameLang))
				pushToSkills(skills);	// Current language
			else
				updateOption(skills.speciesOptions());	// Other Language
		}
		@Override public void skillToSetting(SpeciesSkills skills)	{
			String gameLang = selectedLanguageDir();
			gameLang = selectedLanguageDir();
			if (isReference())
				pullFromSkills(skills);	// Current language
			else if (langDir.equals(gameLang))
				pullFromSkills(skills);	// Current language
			else
				updateOptionTool(skills.speciesOptions());	// Other Language
		}
		@Override public void updateOption(DynamicOptions destOptions)	{ // setting to options
			if (!isSpacer() && destOptions != null) 
				setOption(destOptions, settingValue());
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions)	{ // Option to settings
			if (srcOptions != null && !isSpacer()) {
				String value = getFromOption(srcOptions);
				if (value == null)
					value = defaultValue();
				extendedSet(value);
			}
		}
		@Override public String dynOptionIndex()	{
			if (langDir.equals(selectedLanguageDir()))
				return baseOptionIndex();
			return langOptionIndex();
		}
		@Override public String getCfgLabel()	{ return nameLabel; }
		@Override public String getLangLabel()	{ return ROOT + nameLabel; }
		protected String getFromOption(DynamicOptions srcOptions)	{
			String value = null;
			if (isForShow())
				value = srcOptions.getString(baseOptionIndex() + selectedLanguageDir());
			if (value == null)
				value = srcOptions.getString(langOptionIndex());
			if (value == null)
				value = srcOptions.getString(baseOptionIndex());
			if (value == null)
				value = srcOptions.getString(defaultOptionIndex());
			return value;
		}
		protected void setOption(DynamicOptions destOptions, String value)	{
			String gameLang = selectedLanguageDir();
			if (isReference() || langDir.equals(gameLang)) {	// Current language
				// System.out.println("==> " + baseOptionIndex() + " / Value: " + value); // TO DO BR: REMOVE
				destOptions.setString(baseOptionIndex(), value);
			}
			// System.out.println("==> " + langOptionIndex() + " / Value: " + value); // TO DO BR: REMOVE
			destOptions.setString(langOptionIndex(), value);	// Current and Other language
		}
		protected String baseOptionIndex()		{ return ROOT + nameLangLabel; }
		protected String langOptionIndex()		{ return ROOT + nameLangLabel + langDir; }
		protected String defaultOptionIndex()	{ return ROOT + nameLangLabel + DEFAULT_LANGUAGE; }

		public void addValue(int item, String val)	{ selectedValue(val); }
		public String removeValue(int item)			{ return null; }
		boolean isFilled(String value)		{ return value != null && !value.isEmpty() && !value.startsWith("_"); }
		boolean isFilled()					{ return isFilled(settingValue()); }
		boolean isFilled(int idx)			{ return isFilled(); }
		boolean isAnimAutonomous()			{ return isFilled(); }
		boolean isAnimAutonomous(int idx)	{ return isFilled(idx); }
		boolean isCivAutonomous(int idx)	{ return isFilled(idx); }
		boolean languageChanged(String oldDir, String newDir)	{
			langDir = newDir;
			langKey = toLanguageKey(langDir);
			key = ROOT + nameLangLabel + langKey;
			getFromOption(race().speciesOptions());
			return false;
		}
	}
	// -#-
}