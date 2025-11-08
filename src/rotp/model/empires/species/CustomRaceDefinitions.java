/*
 * Copyright 2015-2020 Ray Fowler
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rotp.model.empires.species;

import static rotp.Rotp.rand;
import static rotp.model.game.IMainOptions.speciesDirectoryPath;
import static rotp.model.game.IPreGameOptions.randomAlienRaces;
import static rotp.model.game.IPreGameOptions.randomAlienRacesMax;
import static rotp.model.game.IPreGameOptions.randomAlienRacesMin;
import static rotp.model.game.IPreGameOptions.randomAlienRacesSmoothEdges;
import static rotp.model.game.IPreGameOptions.randomAlienRacesTargetMax;
import static rotp.model.game.IPreGameOptions.randomAlienRacesTargetMin;
import static rotp.model.game.IRaceOptions.defaultRace;
import static rotp.util.LanguageManager.selectedLanguageDir;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import rotp.model.empires.species.SpeciesSettings.Technologies.TechDiscovery;
import rotp.model.empires.species.SpeciesSettings.Technologies.TechResearch;
import rotp.model.game.DynOptions;
import rotp.model.game.DynamicOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.game.BaseModPanel;
import rotp.ui.util.StringList;

public class CustomRaceDefinitions extends SpeciesSettings {

	public final SettingInteger randomTargetMax = new SettingInteger(
			ROOT, "RANDOM_TARGET_MAX", 75, null, null, 1, 5, 20).pctValue(false);
	public final SettingInteger randomTargetMin = new SettingInteger(
			ROOT, "RANDOM_TARGET_MIN", 0, null, null, 1, 5, 20).pctValue(false);
	public final SettingInteger randomMax = new SettingInteger(
			ROOT, "RANDOM_MAX", 25, -100, 100, 1, 5, 20);
	public final SettingInteger randomMin = new SettingInteger(
			ROOT, "RANDOM_MIN", -25, -100, 100, 1, 5, 20);
	public final SettingBoolean randomUseTarget = new SettingBoolean(
			ROOT, "RANDOM_USE_TARGET", false, true);
	public final SettingBoolean randomSmoothEdges = new SettingBoolean(
			ROOT, "RANDOM_EDGES", true, true);

	private List<Integer> spacerList; // For UI
	private List<Integer> columnList; // For UI
	private RaceList raceList;
	private AvailableAI		availableAI		= new AvailableAI();
	private CRPersonality	personality		= new CRPersonality();
	private CRObjective		objective		= new CRObjective();
	private RaceKey	  		raceKey			= new RaceKey();
	private Technologies	technologies	= new Technologies();
	private TechDiscovery	techDiscovery	= technologies.techDiscovery;
	private TechResearch	techResearch	= technologies.techResearch;

	// ========== Constructors and Initializers ==========
	//
	public CustomRaceDefinitions(boolean minimal)	{
		isReference = false;
		loadInternalRace(defaultRace, minimal);
	}
	public CustomRaceDefinitions(Species species, boolean minimal)	{
		isReference = true;
		loadSkills(species.getSkillCopy(), minimal);
	}
	CustomRaceDefinitions(SpeciesSkills skills, boolean isReference, boolean minimal)	{
		this.isReference = isReference;
		loadInternalRace(skills, minimal);
	}
	public CustomRaceDefinitions(DynOptions srcOptions, boolean minimal)	{
		createFromOptions(srcOptions, minimal);
	}
	CustomRaceDefinitions(String fileName, boolean minimal)	{
		this(loadOptions(speciesDirectoryPath(), fileName + EXT), minimal);
	}
	// ========== Constructors Helpers ==========
	//
	/**
	 * Load internal race, update the setting, and update the options.
	 * @param skills Internal race Skill
	 */
	private void loadSkills(SpeciesSkills skills, boolean minimal)	{
		// work on a copy of the skills
		if(skills.isCopy())
			race(skills);
		else
			race(skills.copy());
		// Create the setting list
		newSettingList();
		// Copy the values from the skills to the settings
		updateSettingFromSkill(minimal);
	}
	/**
	 * Reload current best internal race, update the setting, and update the options.
	 */
	private void reloadInternalRace(boolean minimal)	{
		String reworkedKey = ((ReworkedRaceKey)settingMap.get(ROOT + ReworkedRaceKey.REWORKED_RACE_KEY)).settingValue();
		if (ReworkedRaceKey.DEFAULT_VALUE.equals(reworkedKey))
			reworkedKey = defaultRace;
		loadInternalRace(reworkedKey, minimal);
	}
	/**
	 * Load internal race, update the setting, and update the options.
	 * @param raceKey Internal Race key
	 */
	private void loadInternalRace(String raceKey, boolean minimal)	{ loadInternalRace(Species.getAnim(raceKey), minimal); }
	/**
	 * Load internal race, update the setting, and update the options.
	 * @param skills Internal race Skill
	 */
	private void loadInternalRace(SpeciesSkills skills, boolean minimal)	{
		loadingInternalRace = true;
		loadSkills(skills, minimal);
		// Copy the values from the settings to the options
		updateOptionsFromSettings(minimal);
		loadingInternalRace = false;
	}
	/**
	 * Fully load the best internal race, as preparation for custom species.
	 * @param srcOptions Internal Race key
	 */
	private void preloadInternalRace(DynOptions srcOptions)	{
		String reworkedKey = ReworkedRaceKey.getRawReworkedKey(srcOptions);
		if (reworkedKey == null)
			reworkedKey = defaultRace;
		loadInternalRace(reworkedKey, false);
	}
	/**
	 * Fully load the best Internal internal race, update the settings from the options, and update the options.
	 * @param srcOptions
	 */
	private void createFromOptions(DynOptions srcOptions, boolean minimal)	{
		// Load best internal Race
		reworkedRace = null;
		preloadInternalRace(srcOptions);
		// Update settings from options
		isReference = false;
		updateSettingsFromOptions(srcOptions, minimal);
		addMissingOptionsFromOptions(srcOptions, minimal);
		// update Skills from settings
		updateSkillsFromSettings(minimal);
		// update Options from settings (To push eventual updates)
		updateOptionsFromSettings(minimal);
	}
	private void updateSettingsFromOptions(DynOptions srcOptions, boolean minimal)	{
		List<ICRSettings> settings = minimal? settingMap.getSettings() : settingMap.getAll();
		for (ICRSettings setting : settings) {
			setting.updateOptionTool(srcOptions);
			report(setting, "updateSettingsFromOptions"); // TODO BR: REMOVE
		}
	}
	private void addMissingOptionsFromOptions(DynOptions srcOptions, boolean minimal)	{
		if (minimal)
			return;
		// Add any missing options if necessary.
		LinkedHashMap<String, String> destOptions = race().speciesOptions().stringList();
		for (Entry<String, String> entry : srcOptions.stringList().entrySet()) {
			if (destOptions.containsKey(entry.getKey()))
				continue;
			destOptions.put(entry.getKey(), entry.getValue());
			System.out.println("updateSettingsFromOptions " + entry.getKey() + " " + entry.getValue()); // TODO BR: REMOVE
		}
	}
	private void updateOptionsFromSettings(boolean minimal)	{
		DynOptions destOptions = race().speciesOptions();
		List<ICRSettings> settings = minimal? settingMap.getSettings() : settingMap.getAll();
		for (ICRSettings setting : settings)
			setting.updateOption(destOptions);
	}
	private void updateSkillsFromSettings(boolean minimal)	{
		List<ICRSettings> settings = minimal? settingMap.getSettings() : settingMap.getAll();
		for (ICRSettings setting : settings)
			setting.settingToSkill(race());
	}
	private void updateSettingFromSkill(boolean minimal)	{
		List<ICRSettings> settings = minimal? settingMap.getSettings() : settingMap.getAll();
		for (ICRSettings setting : settings)
			setting.skillToSetting(race());
	}
	// -------------------- Static Methods --------------------
	// 
	private static void backwardComp(DynOptions opts) {
		if (opts.getString(ROOT + "HOME_RESOURCES", "").equalsIgnoreCase("Artifacts")) {
			opts.setString(ROOT + "HOME_RESOURCES", "Normal");
			opts.setString(ROOT + "HOME_ARTIFACTS", "Artifacts");
		}
	}
	private static DynOptions loadOptions(String path, String fileName)	{
		DynOptions opts = DynOptions.loadOptions(path, fileName);
		return opts;
	}
	private static DynOptions loadOptions(File fileName) {
		DynOptions opts =  DynOptions.loadOptions(fileName);
		backwardComp(opts);
		return opts;
	}
	public static SpeciesSkills fileToAlienRace(String fileName)		{
		if(fileName.startsWith(BASE_RACE_MARKER))
			return getBaseRace(fileName, true);
		return new CustomRaceDefinitions(fileName, false).getRace();
	}
	public static SpeciesSkills fileToAlienRaceInfo(String fileName)	{
		if(fileName.startsWith(BASE_RACE_MARKER))
			return getBaseRace(fileName, true);
		return new CustomRaceDefinitions(fileName, true).getRace();
	}
	public static SpeciesSkills optionToAlienRace(DynOptions options)	{
		return new CustomRaceDefinitions(options, false).getRace();
	}
	static SpeciesSkills getRandomAlienRace() {
		CustomRaceDefinitions cr = new CustomRaceDefinitions(false);
		cr.randomizeRace(randomAlienRacesMin.get(), randomAlienRacesMax.get(),
				randomAlienRacesTargetMin.get(), randomAlienRacesTargetMax.get(),
				randomAlienRaces.isTarget(), randomAlienRacesSmoothEdges.get(), false);
		return cr.getRace().isCustomSpecies(true);
	}
	public static DynOptions getDefaultOptions() {
//		return new CustomRaceDefinitions(defaultRace, true).getAsOptions();
		return new CustomRaceDefinitions(true).getAsOptions();
	}
	public static StringList getBaseRaceList()	{
		return getRaceFileList()
				.stream()
				.filter(c -> c.startsWith(BASE_RACE_MARKER))
				.collect(Collectors.toCollection(StringList::new));
	}
	private static SpeciesSkills getBaseRace(String key, boolean minimal) {
		CustomRaceDefinitions cr = new CustomRaceDefinitions(minimal);
		cr.setRace(cr.new RaceList().getBaseRace(key));
		return cr.getRace();
	}
	private static RaceList newRaceList()			{ return new CustomRaceDefinitions(false).new RaceList(); } // TODO BR: Optimize
	private static StringList getRaceFileList()		{ return newRaceList().getLabels(); }
	public static StringList getAllowedAlienRaces()	{ return newRaceList().getAllowedAlienRaces(); }
	public static StringList getAllAlienRaces()		{ return newRaceList().getAllAlienRaces(); }
	public static HashMap<String, StringList> getReworkMap()	{ return newRaceList().reworkMap(); }

	// ========== Options Management ==========
	//
	/**
	 * Settings to DynOptions
	 * @return DynOptions
	 */
	public DynOptions getAsOptions()	{
		updateOptionsFromSettings(false);
		return race().speciesOptions();
	}
	/**
	 * DynOptions to settings and race
	 * @param srcOptions
	 */
	public void setSettingTools(DynOptions srcOptions)	{ createFromOptions(srcOptions, true); }
	/**
	 * race to settings
	 */
	public void setFromRaceToShow(SpeciesSkills skills)	{
		race(skills);
		for (ICRSettings setting : settingMap.getSettings())
			setting.skillToSetting(skills);
	}
	private void saveSettingList(String path, String fileName)	{ getAsOptions().save(path, fileName); }
	/**
	 * DynOptions to settings and race
	 */
	private String fileName()	{ return race().id + EXT; }
	public void saveRace()		{ saveSettingList(speciesDirectoryPath(), fileName()); }
	public void loadRace()		{
		if (Species.isValidKey(race().id))
			setRace(race().id);
		else
			createFromOptions(loadOptions(speciesDirectoryPath(), fileName()), true);
	}
	// ========== Main Getters ==========
	//
	public String getRaceKey()				{ return race().id; }
	public List<ICRSettings> settingList()	{ return settingMap.getSettings(); }
	public List<ICRSettings> guiList()		{ return settingMap.getGuis(); }
	public List<Integer>	 spacerList()	{ return spacerList; }
	public List<Integer>	 columnList()	{ return columnList; }
	public RaceList initRaceList()			{ 
		raceList = new RaceList();
		return raceList;
	}
	SpeciesSkills getRace()					{
		race().isCustomSpecies(true);
		return race();
	}
	public SpeciesSkills getRawRace()		{ return race(); }
	// ========== Other Methods ==========
	//
	/**
	 * @param raceKey of the new race
	 */
	private void setRace(String raceKey) {
		race(Species.getAnim(raceKey).copy());
		for (ICRSettings setting : settingMap.getAll())
			setting.skillToSetting(race());
	}
	public int getCount()	{
		int count = 0;
		for (ICRSettings setting : settingMap.getSettings())
			if (!setting.isSpacer() && !setting.hasNoCost())
				count++;
		return count;
	}
	/**
	 * race is not up to date
	 */
	private void randomizeRace(float min, float max,
			float targetMin, float targetMax, boolean gaussian, boolean updateGui) {
		float target	= (targetMax + targetMin)/2;
		float maxDiff	= Math.max(0.04f, Math.abs(targetMax-targetMin)/2);
		float maxChange	= Math.max(0.1f, Math.abs(max-min));

		List<ICRSettings> shuffledSettingList = new ArrayList<>(settingMap.getSettings());
		// first pass full random
		randomizeRace(min, max, gaussian, updateGui);
		float cost = getTotalCost();

		// second pass going smoothly to the target
		for (int i=0; i<10; i++) {
			Collections.shuffle(shuffledSettingList);
			for (ICRSettings setting : shuffledSettingList) {
				if (!setting.isSpacer() &&!setting.hasNoCost()) {
					float difference = target - cost;
					if (Math.abs(difference) <= maxDiff)
						return;
					float costFactor = setting.costFactor();
					float changeRequest = difference/costFactor;
					cost -= setting.settingCost();
					if (Math.abs(changeRequest) <= maxChange) {
						float maxRequest = changeRequest + maxDiff/costFactor;
						if (maxRequest > 0)
							maxRequest = Math.min(maxChange, maxRequest);
						else
							maxRequest =  Math.max(-maxChange, maxRequest);
						float minRequest = changeRequest - maxDiff/costFactor;
						if (maxRequest > 0)
							minRequest = Math.min(maxChange, minRequest);
						else
							minRequest =  Math.max(-maxChange, minRequest);
						float request = minRequest + rand().nextFloat() * (maxRequest-minRequest);

						setting.setValueFromCost(setting.settingCost()+request*costFactor);
					} else {
						setting.setRandom(setting.lastRandomSource()
								+ maxChange*Math.signum(changeRequest));
					}
					cost += setting.settingCost();
					if (updateGui)
						setting.guiSelect();
				}
			}
		}
		// third pass forcing the target
		for (int i=0; i<5; i++) {
			Collections.shuffle(shuffledSettingList);
			for (ICRSettings setting : shuffledSettingList) {
				if (!setting.isSpacer() &&!setting.hasNoCost()) {
					cost -= setting.settingCost();
					setting.setValueFromCost(target - cost);
					cost += setting.settingCost();
					if (updateGui)
						setting.guiSelect();
					if (Math.abs(cost-target) <= maxDiff)
						return;
				}
			}
		}
	}
	/**
	 * race is not up to date
	 */
	private void randomizeRace(float min, float max, boolean gaussian, boolean updateGui) {
		for (ICRSettings setting : settingMap.getSettings()) {
			if (!setting.isSpacer()) {
				setting.setRandom(min, max, gaussian);
				if (updateGui)
					setting.guiSelect();
			}
		}
	}
	public void randomizeRace(boolean updateGui) {
		reloadInternalRace(true);
		randomizeRace(randomMin.settingValue(), randomMax.settingValue(),
			randomTargetMin.settingValue(), randomTargetMax.settingValue(),
			randomUseTarget.settingValue(), randomSmoothEdges.settingValue(), updateGui);
		updateSkillsFromSettings(true);
	}
	/**
	 * race is not up to date
	 */
	private void randomizeRace(float min, float max, float targetMin, float targetMax, 
			boolean useTarget, boolean gaussian, boolean updateGui) {
		if (useTarget)
			randomizeRace(min, max, targetMin, targetMax, gaussian, updateGui);
		else
			randomizeRace(min, max, gaussian, updateGui);
	}
	public  float getTotalCost() {
		float totalCost = 0;
		for (ICRSettings setting : settingMap.getSettings()) {
			if (setting.isSpacer())
				continue;
			totalCost += setting.settingCost();
		}
		return totalCost;
	}
	public  float getMalusCost() {
		float malus = 0;
		for (ICRSettings setting : settingMap.getSettings()) {
			if (setting.isSpacer())
				continue;
			float cost = setting.settingCost();
			if (cost < 0)
				malus += setting.settingCost();
		}
		return -malus;
	}
	private float updateSettings()	{
		for (ICRSettings setting : settingMap.getSettings())
			setting.updateGui();
		return getTotalCost();
	}
	private void newSettingList()	{
		if (settingMap.filled)
			return;
		spacerList  = new LinkedList<>();
		columnList  = new LinkedList<>();
		String dir = "en";
		dir = selectedLanguageDir();

		// ====================
		// First column (left)
		settingMap.add(new ReworkedRaceKey()); // Keep First
		settingMap.add(raceKey);
		settingMap.add(new RaceName(dir));
		settingMap.add(new EmpireName(dir));
		settingMap.add(new HomeWorld(dir));
		settingMap.add(new LeaderName(dir));
		settingMap.add(new RaceDescription(1, dir));
		settingMap.add(new RaceDescription(2, dir));
		settingMap.add(new RaceDescription(4, dir));
		settingMap.add(new RaceDescription(3, dir));
		endOfColumn();

		// ====================
		// Second column
		settingMap.add(personality.erratic);
		settingMap.add(personality.pacifist);
		settingMap.add(personality.honorable);
		settingMap.add(personality.ruthless);
		settingMap.add(personality.aggressive);
		settingMap.add(personality.xenophobic);
		spacer();
		settingMap.add(objective.militarist);
		settingMap.add(objective.ecologist);
		settingMap.add(objective.diplomat);
		settingMap.add(objective.industrialist);
		settingMap.add(objective.expansionist);
		settingMap.add(objective.technologist);
		spacer();
		settingMap.add(availableAI);
		settingMap.add(new BoundAI());
		settingMap.add(new PreferredShipSize());
		settingMap.add(new PreferredShipSet());
		spacer();
		settingMap.add(new RacePrefix());
		settingMap.add(new RaceSuffix());
		settingMap.add(new LeaderPrefix());
		settingMap.add(new LeaderSuffix());
		settingMap.add(new WorldsPrefix());
		settingMap.add(new WorldsSuffix());
		endOfColumn();

		// ====================
		// Third column
		settingMap.add(techDiscovery);
		settingMap.add(techDiscovery.computer);
		settingMap.add(techDiscovery.construction);
		settingMap.add(techDiscovery.forceField);
		settingMap.add(techDiscovery.planet);
		settingMap.add(techDiscovery.propulsion);
		settingMap.add(techDiscovery.weapon);
		spacer();
//		settingList.add(new RacePlanetType()); // not yet differentiated
		settingMap.add(new HomeworldSize());
//		settingList.add(new SpeciesType()); // Not used in Game
		settingMap.add(new PopGrowRate());
		settingMap.add(new IgnoresEco());
		spacer();
		settingMap.add(new ProdWorker());
		settingMap.add(new ProdControl());
		settingMap.add(new IgnoresFactoryRefit());
		spacer();
		settingMap.add(new ShipAttack());
		settingMap.add(new ShipDefense());
		settingMap.add(new ShipInitiative());
		settingMap.add(new GroundAttack());
		spacer();
		settingMap.add(new SpyCost());
		settingMap.add(new SpySecurity());
		settingMap.add(new SpyInfiltration());
//		settingList.add(new SpyTelepathy()); // Not used in Game
		endOfColumn();

		// ====================
		// Fourth column
		settingMap.add(techResearch);
		settingMap.add(techResearch.computer);
		settingMap.add(techResearch.construction);
		settingMap.add(techResearch.forceField);
		settingMap.add(techResearch.planet);
		settingMap.add(techResearch.propulsion);
		settingMap.add(techResearch.weapon);
		spacer();
		settingMap.add(new PlanetArtifacts()); // Backward compatibility: stay above PlanetRessources()
		settingMap.add(new PlanetRessources());
		settingMap.add(new PlanetEnvironment());
		spacer();
		settingMap.add(new CreditsBonus());
		settingMap.add(new HitPointsBonus());
		settingMap.add(new MaintenanceBonus());
		settingMap.add(new ShipSpaceBonus());
		spacer();
		settingMap.add(new DiplomacyTrade());
// 		settingList.add(new DiploPosDP()); // Not used in Game
		settingMap.add(new DiplomacyBonus());
		settingMap.add(new DiplomacyCouncil());
		settingMap.add(new RelationDefault());	// BR: Maybe All the races
		endOfColumn();

		// ====================
		// Hidden settings
		settingMap.addAttribute(new LanguagesList());	// TODO Complete
		settingMap.addAttribute(new LeaderTitle(dir));	// TODO Complete
		settingMap.addAttribute(new LeaderFullTitle(dir));	// TODO Complete
		setDialogueSettings(dir);

		// ====================
		// Fifth column
		// endOfColumn();
		// ====================
		settingMap.addGui(randomSmoothEdges);
		settingMap.addGui(randomMin);
		settingMap.addGui(randomMax);
		settingMap.addGui(randomTargetMin);
		settingMap.addGui(randomTargetMax);
		settingMap.addGui(randomUseTarget);	    
		for(ICRSettings setting : settingMap.getGuis())
			setting.hasNoCost(true);
		settingMap.filled = true;
	}
	private void endOfColumn()	{ columnList.add(settingMap.getSettings().size()); }
	private void spacer()		{ spacerList.add(settingMap.getSettings().size()); }
	// ==================== Nested Classes ====================
	//
	// ==================== RaceList ====================
	//
	public class RaceList extends SettingBase<String> {
		private boolean newValue = false;
		private boolean reload	 = false;
		private HashMap<String, StringList> reworkedMap = new HashMap<>();

		public RaceList() {
			super(ROOT, "RACE_LIST");
			isBullet(true);
			maxBullet(32);
			labelsAreFinals(true);
			hasNoCost(true);
			showFullGuide(false);
			reload(false);
		}
		// ---------- Initializers ----------
		//
		public void reload(boolean foldersRework) {
			String currentValue = settingValue();
			clearLists();
			reworkedMap.clear();
			clearOptionsText();
			for (String raceKey : IGameOptions.allRaceOptions)
				reworkedMap.put(raceKey, new StringList());
			reworkedMap.put(ReworkedRaceKey.DEFAULT_VALUE, new StringList());

			// Add Current race
			add((DynOptions) IGameOptions.playerCustomRace.get());
			defaultIndex(0);
			// Add existing files
			File[] fileList = loadListing();
//			fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) {
					DynOptions opt = loadOptions(file);
					if (opt.size() == 0)
						System.err.println("Empty race file: " + file.getName());
					else {
						// Check to Update filename
						String skillKey = RaceKey.valid(opt, file);
						// Test for reworked old Ways
						String animKey  = ReworkedRaceKey.validReworked(opt, file, foldersRework);
						reworkedMap.get(animKey).add(skillKey); // The map has a "None" key
						add(opt);
					}
				}
			// Add Game races
			for (String raceKey : IGameOptions.allRaceOptions)
				add(raceKey);

			initOptionsText();
			reload = true;
			set(currentValue);
		}
		private HashMap<String, StringList> reworkMap() { return reworkedMap; }
		private File[] loadListing()	{
			File speciesDir = new File(speciesDirectoryPath());
			List<File> speciesList = new ArrayList<>();
			scanSubDir(speciesList, speciesDir);
			return speciesList.toArray(new File[0]);
		}
		private void scanSubDir(List<File> speciesList, File speciesDir)	{
			if (!speciesDir.exists() || !speciesDir.isDirectory())
				return;
			// Local files
			File[] array = speciesDir.listFiles(SPECIES_FILTER);
			if (array != null && array.length > 0)
				speciesList.addAll(Arrays.asList(array));
			// Sub Dir files
			File[] subDirectories = speciesDir.listFiles(File::isDirectory);
			if(subDirectories != null && subDirectories.length > 0)
				for (File subDir : subDirectories)
					scanSubDir(speciesList, subDir);
		}
		private void add(DynOptions opt) {
			CustomRaceDefinitions cr = new CustomRaceDefinitions(opt, true);
			SpeciesSkills dr = cr.getRace();
			String cfgValue	 = dr.setupName;
			String langLabel = dr.id;
			String tooltipKey = dr.getDescription3();
			float cost = cr.getTotalCost();
			put(cfgValue, langLabel, cost, langLabel, tooltipKey);
		}
		private void add(String raceKey) {
			Species species	  = new Species(raceKey);
			String cfgValue	  = species.skillKey();
			String langLabel  = BASE_RACE_MARKER + species.setupName();
			String tooltipKey = species.getDescription(3);
			CustomRaceDefinitions cr = new CustomRaceDefinitions(species, true);
			float cost = cr.getTotalCost();
			put(cfgValue, langLabel, cost, langLabel, tooltipKey);
		}
		private String getBaseRace(String key) {
			return getCfgValue(key);
		}
		public boolean newValue() {
			if (newValue) {
				newValue = false;
				return true;
			}
			return false;
		}
		public StringList getAllowedAlienRaces() {
			StringList list = new StringList();
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) { // TODO BR: optimize by reading from options
					CustomRaceDefinitions cr = new CustomRaceDefinitions(loadOptions(file), true);
					if (cr.availableAI.settingValue())
						list.add(cr.raceKey.settingValue());
				}
			list.removeNullAndEmpty();
			return list;
		}
		public StringList getAllAlienRaces() { // TODO BR: optimize by reading from options
			StringList list = new StringList();
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) {
					CustomRaceDefinitions cr = new CustomRaceDefinitions(loadOptions(file), true);
					list.add(cr.raceKey.settingValue());
				}
			list.removeNullAndEmpty();
			return list;
	    }
		// ---------- Overriders ----------
		//
		@Override public String guideValue() {
			return guiOptionLabel();
		}
		@Override public String guiOptionValue(int index) {
			return guiOptionLabel();
		}
		@Override protected void selectedValue(String value) {
			super.selectedValue(value);
			if (reload) { // No need to load options on reload
				reload = false;
				return;
			}
			if (index() == 0) {
				setSettingTools((DynOptions) IGameOptions.playerCustomRace.get());
				newValue = true;
				return;
			}
			if (index()>=listSize()-16) { // Base Race
				race(Species.getAnim(getCfgValue(settingValue())).copy());
				for (ICRSettings setting : settingMap.getAll()) // TODO BR: validate if GetSettings
					setting.skillToSetting(race());
				updateSettings();
				return;
			}
			File file = new File(speciesDirectoryPath(), settingValue()+EXT);
			if (file.exists()) {
				setSettingTools(loadOptions(file));
				newValue = true;
				return;
			}
		}
	}
	// ==================== RaceKey ====================
	//
	private class RaceKey extends SettingString	{
		private static final String KEY = "RACE_KEY";

		private static void setKey(DynOptions opts, String key)	{ opts.setString(ROOT + KEY, key); }
		private static String getKey(DynOptions opts)			{ return opts.getString(ROOT + KEY, ""); }
		private static String fileToKey (File file)				{
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
		private static String valid(DynOptions opt, File file)	{
			String optKey	= getKey(opt);
			String fileKey	= fileToKey(file);
			if (!optKey.equalsIgnoreCase(fileKey)) {
				setKey(opt, fileKey);
				DynOptions.saveOptions(opt, file);
			}
			return fileKey;
		}
		private RaceKey() {
			super(ROOT, KEY, defaultRace, 1);
			randomStr(RANDOMIZED_RACE_KEY);
		}
		@Override public boolean toggle(MouseEvent e, MouseWheelEvent w, BaseModPanel frame) {
			if (e == null)
				return toggle(w);

			AllSpeciesAttributes settings =  new AllSpeciesAttributes(this, frame);
			CustomNameUI ui = new CustomNameUI(frame, settings);
			return false;
		}
		@Override public void settingToSkill(SpeciesSkills skills) { skills.id = settingValue(); }
		@Override public void skillToSetting(SpeciesSkills skills) { set(skills.id); }
	}



	// ==================== All Empire and Species Identifications ====================
	//
		private class LanguagesList extends SettingStringList {
		private static final String KEY = "LANGUAGES";
		private static String get(DynOptions opts)	{ return opts.getString(ROOT + KEY, "en"); }
		private LanguagesList()	{
			super(ROOT, KEY, "en", "en");
			String lang = selectedLanguageDir();
			if (lang.equals("en"))
				return;
			set("en," + lang);
		}
		@Override protected void selectedValue(String newValue) {
			if(newValue.isBlank())
				super.selectedValue(defaultValue());
			else
				super.selectedValue(newValue);
		}
		@Override public LanguagesList set(String newValue) {
			if(newValue.isBlank())
				super.set(defaultValue());
			else
				super.set(newValue);
			return this;
		}
		@Override public void pushToSkills(SpeciesSkills skills)	{ updateOption(skills.speciesOptions()); }
		@Override public void pullFromSkills(SpeciesSkills skills)	{ updateOptionTool(skills.speciesOptions()); }
	}

	class AllSpeciesAttributes implements ICRSettings { // For all languages
		RaceKey raceKey;
		
		private SettingStringList languagesDir;
		private HashMap<String, SpeciesAttributes> attributesLanguageMap; // Language Map
		
		private AllSpeciesAttributes(RaceKey key, BaseModPanel frame)	{
			DynOptions options = getAsOptions();
			raceKey = key;
			attributesLanguageMap = new HashMap<>();
			StringList languageList = settingMap.getList(LanguagesList.KEY);
			for (String langDir: languageList) {
				attributesLanguageMap.put(langDir, new SpeciesAttributes(langDir));
			}
		}
		SpeciesAttributes getAttributes(String langDir)	{
			SpeciesAttributes sa = attributesLanguageMap.get(langDir);
			if (sa == null) {
				sa = new SpeciesAttributes(langDir);
				attributesLanguageMap.put(langDir, sa);
			}
			return sa;
		}

		public StringList getEthnicNames()			{ return settingMap.getList(RaceName.KEY); }
		public void addLanguage(String dir)			{ languagesDir.add(dir); }	// TODO BR: More?
		public void removeLanguage(String dir)		{ languagesDir.remove(dir); }	// TODO BR: More?
		public StringList getLanguages()			{ return languagesDir.getList(); }	// TODO BR: More?
		public void setLanguages(StringList list)	{ languagesDir.setList(list); }	// TODO BR: More?
	
		@Override public boolean next()	{
			// TODO BR: Call GUI
			return false;
		}
		@Override public void updateOptionTool() {
			raceKey.updateOptionTool();
			languagesDir.updateOptionTool();
			for (SpeciesAttributes language : attributesLanguageMap.values())
				language.updateOptionTool();
		}
		@Override public void updateOption(DynamicOptions destOptions) {
			raceKey.updateOption(destOptions);
			languagesDir.updateOption(destOptions);
			for (SpeciesAttributes language : attributesLanguageMap.values())
				language.updateOption(destOptions);
		}
		@Override public void updateOptionTool(DynamicOptions srcOptions) {
			raceKey.updateOptionTool(srcOptions);
			languagesDir.updateOptionTool(srcOptions);
			for (SpeciesAttributes language : attributesLanguageMap.values())
				language.updateOptionTool(srcOptions);
		}
	}
}
