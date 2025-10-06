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

package rotp.model.empires;

import static rotp.Rotp.rand;
import static rotp.model.game.IMainOptions.speciesDirectoryPath;
import static rotp.model.game.IPreGameOptions.randomAlienRaces;
import static rotp.model.game.IPreGameOptions.randomAlienRacesMax;
import static rotp.model.game.IPreGameOptions.randomAlienRacesMin;
import static rotp.model.game.IPreGameOptions.randomAlienRacesSmoothEdges;
import static rotp.model.game.IPreGameOptions.randomAlienRacesTargetMax;
import static rotp.model.game.IPreGameOptions.randomAlienRacesTargetMin;
import static rotp.model.game.IRaceOptions.defaultRace;
import static rotp.ui.util.PlayerShipSet.DISPLAY_RACE_SET;
import static rotp.ui.util.SettingBase.CostFormula.DIFFERENCE;
import static rotp.ui.util.SettingBase.CostFormula.NORMALIZED;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import rotp.model.empires.Leader.Personality;
import rotp.model.game.DynOptions;
import rotp.model.game.DynamicOptions;
import rotp.model.game.IGameOptions;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipLibrary;
import rotp.ui.util.PlayerShipSet;
import rotp.ui.util.SettingBase;
import rotp.ui.util.SettingBoolean;
import rotp.ui.util.SettingFloat;
import rotp.ui.util.SettingInteger;
import rotp.ui.util.SettingString;

public class CustomRaceDefinitions implements ISpecies {
	
	public	static final String ROOT	= "CUSTOM_RACE_";
	private	static final String PLANET	= "PLANET_";
	private	static final String EXT		= ".race";
	private static final String baseRace = defaultRace;
	private	static final String RANDOMIZED_RACE_KEY	= "RANDOMIZED_RACE";
	public	static final String RANDOM_RACE_KEY		= "RANDOM_RACE_KEY";
	public	static final String CUSTOM_RACE_KEY		= "CUSTOM_RACE_KEY";
	public	static final String BASE_RACE_MARKER	= "*";
	private static final boolean booleansAreBullet	= true;
	private static final FilenameFilter SPECIES_FILTER = (File dir, String name1) -> name1.toLowerCase().endsWith(EXT);

	private Race race; // !!! To be kept up to date !!!
	private final LinkedList<SettingBase<?>> settingList = new LinkedList<>(); // !!! To be kept up to date !!!
	private final LinkedList<SettingBase<?>> guiList	 = new LinkedList<>();

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

	private LinkedList<Integer> spacerList; // For UI
	private LinkedList<Integer> columnList; // For UI
	private RaceList raceList;
	private AvailableAI		availableAI		= new AvailableAI();
	private CRPersonality	personality		= new CRPersonality();
	private CRObjective		objective		= new CRObjective();
	private RaceKey	  		raceKey			= new RaceKey();
	private TechDiscovery	techDiscovery	= new TechDiscovery();
	private TechResearch	techResearch	= new TechResearch();

	// ========== Constructors and Initializers ==========
	//
	public CustomRaceDefinitions() {
		newSettingList();
		pushSettings();
	}
	public CustomRaceDefinitions(Race race) {
		newSettingList();
		setRace(race.name());
	}
	public CustomRaceDefinitions(DynOptions srcOptions) {
		newSettingList();
		setSettingTools(srcOptions);
		pushSettings();
	}
	private CustomRaceDefinitions(String fileName) {
		this(loadOptions(speciesDirectoryPath(), fileName + EXT));
	}

	// -------------------- Static Methods --------------------
	// 
	private static void backwardComp(DynOptions opts) {
		if (opts.getString(ROOT + "HOME_RESOURCES", "").equalsIgnoreCase("Artifacts")) {
			opts.setString(ROOT + "HOME_RESOURCES", "Normal");
			opts.setString(ROOT + "HOME_ARTIFACTS", "Artifacts");
		}
	}
	private static DynOptions loadOptions(String path, String fileName) {
		DynOptions opts = DynOptions.loadOptions(path, fileName);
		return opts;
	}
	private static DynOptions loadOptions(File saveFile) {
		DynOptions opts =  DynOptions.loadOptions(saveFile);
		backwardComp(opts);
		return opts;
	}

	public static boolean raceFileExist(String fileName) {
		File f = new File(speciesDirectoryPath(), fileName + EXT);
		return (f.exists() && !f.isDirectory());
	}
	public static Race fileToAlienRace(String fileName) {
		if(fileName.startsWith(BASE_RACE_MARKER))
			return getBaseRace(fileName);
		return new CustomRaceDefinitions(fileName).getRace();
	}
	public static Race optionToAlienRace(DynOptions options) {
		return new CustomRaceDefinitions(options).getRace();
	}
	private static Race getRandomAlienRace() {
		CustomRaceDefinitions cr = new CustomRaceDefinitions();
		cr.randomizeRace(randomAlienRacesMin.get(), randomAlienRacesMax.get(),
				randomAlienRacesTargetMin.get(), randomAlienRacesTargetMax.get(),
				randomAlienRaces.isTarget(), randomAlienRacesSmoothEdges.get(), false);
		return cr.getRace().isCustomRace(true);
	}
	public static Race getAlienRace(String key, DynOptions options) {
		if (key.equalsIgnoreCase(RANDOM_RACE_KEY)) { // Generate random
			return getRandomAlienRace();
		}
		if (key.equalsIgnoreCase(CUSTOM_RACE_KEY)) { // Player Custom
			DynOptions opt = (DynOptions) IGameOptions.playerCustomRace.get();
			return new CustomRaceDefinitions(opt).getRace();
		}
		if (options == null) // load from file
			return new CustomRaceDefinitions(key).getRace();
		else
			return new CustomRaceDefinitions(options).getRace();
	}
	static Race keyToRace(String raceKey) {
		if (raceKey.equalsIgnoreCase(RANDOM_RACE_KEY)) {
			return getRandomAlienRace();
		}
		if (raceKey.equalsIgnoreCase(CUSTOM_RACE_KEY)) {
			DynOptions opt = (DynOptions) IGameOptions.playerCustomRace.get();
			return new CustomRaceDefinitions(opt).getRace();
		}
		// load from file
		return new CustomRaceDefinitions(raceKey).getRace();
	}
	public static DynOptions getDefaultOptions() {
		return new CustomRaceDefinitions(baseRace).getAsOptions();
	}
	public static LinkedList<String> getBaseRaceList() {
		return getRaceFileList()
				.stream()
				.filter(c -> c.startsWith(BASE_RACE_MARKER))
				.collect(Collectors.toCollection(LinkedList::new));
	}
	private static Race getBaseRace(String key) {
		CustomRaceDefinitions cr = new CustomRaceDefinitions();
		cr.setRace(cr.new RaceList().getBaseRace(key));
		return cr.getRace();
	}
	private static LinkedList<String> getRaceFileList() {
		return new CustomRaceDefinitions().new RaceList().getLabels();
	}
	public static LinkedList<String> getAllowedAlienRaces() {
		return new CustomRaceDefinitions().new RaceList().getAllowedAlienRaces();
	}
	public static LinkedList<String> getAllAlienRaces() {
		return new CustomRaceDefinitions().new RaceList().getAllAlienRaces();
	}
	// ========== Options Management ==========
	//
	/**
	 * Settings to DynOptions
	 * @return DynOptions
	 */
	public DynOptions getAsOptions() {
		DynOptions destOptions = new DynOptions();
		for (SettingBase<?> setting : settingList)
			setting.updateOption(destOptions);
		for (SettingBase<?> setting : guiList)
			setting.updateOption(destOptions);
		return destOptions;
	}
	/**
	 * DynOptions to settings and race
	 * @param srcOptions
	 */
	public void setSettingTools(DynOptions srcOptions) {
		for (SettingBase<?> setting : settingList)
			setting.updateOptionTool(srcOptions);
		for (SettingBase<?> setting : guiList)
			setting.updateOptionTool(srcOptions);
		pushSettings();
	}
	/**
	 * race to settings
	 */
	public void setFromRaceToShow(Race race) {
		this.race = race;
		pullSettings();
	}
	private void saveSettingList(String path, String fileName) {
		getAsOptions().save(path, fileName);
	}
	/**
	 * DynOptions to settings and race
	 */
	private void loadSettingList(String path, String fileName) {
		setSettingTools(loadOptions(path, fileName));
		
	}
	private String fileName() { return race.id + EXT; }
	public void saveRace() { saveSettingList(speciesDirectoryPath(), fileName()); }
	public void loadRace() {
		if (R_M.isValidKey(race.id))
			setRace(race.id);
		else
			loadSettingList(speciesDirectoryPath(), fileName());
	}
	// ========== Main Getters ==========
	//
	public String getRaceKey()						{ return race.id; }
	public LinkedList<SettingBase<?>> settingList()	{ return settingList; }
	public LinkedList<SettingBase<?>> guiList()		{ return guiList; }
	public LinkedList<Integer>		  spacerList()	{ return spacerList; }
	public LinkedList<Integer>		  columnList()	{ return columnList; }
	public RaceList initRaceList()		{ 
		raceList = new RaceList();
		return raceList;
	}
	public Race getRace() {
		pushSettings();
		race.raceOptions(getAsOptions());
		race.isCustomRace(true);
		return race;
	}
	// ========== Other Methods ==========
	//
	/**
	 * @param raceKey the new race
	 */
	public void setRace(String raceKey) {
		race = R_M.keyed(raceKey).copy();
		pullSettings();
	}
	public int getCount() {
		int count = 0;
		for (SettingBase<?> setting : settingList) {
			if (!setting.isSpacer()
					&& !setting.hasNoCost())
				count++;
		}
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

		List<SettingBase<?>> shuffledSettingList = new ArrayList<>(settingList);
		// first pass full random
		randomizeRace(min, max, gaussian, updateGui);
		float cost = getTotalCost();

		// second pass going smoothly to the target
		for (int i=0; i<10; i++) {
			Collections.shuffle(shuffledSettingList);
			for (SettingBase<?> setting : shuffledSettingList) {
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
			for (SettingBase<?> setting : shuffledSettingList) {
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
		for (SettingBase<?> setting : settingList) {
			if (!setting.isSpacer()) {
				setting.setRandom(min, max, gaussian);
				if (updateGui)
					setting.guiSelect();
			}
		}
	}
	public void randomizeRace(boolean updateGui) {
		randomizeRace(randomMin.settingValue(), randomMax.settingValue(),
			randomTargetMin.settingValue(), randomTargetMax.settingValue(),
			randomUseTarget.settingValue(), randomSmoothEdges.settingValue(), updateGui);
		pushSettings();
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
		for (SettingBase<?> setting : settingList) {
			if (setting.isSpacer())
				continue;
			totalCost += setting.settingCost();
		}
		return totalCost;
	}
	public  float getMalusCost() {
		float malus = 0;
		for (SettingBase<?> setting : settingList) {
			if (setting.isSpacer())
				continue;
			float cost = setting.settingCost();
			if (cost < 0)
				malus += setting.settingCost();
		}
		return -malus;
	}
	private void pushSettings() {
		race = R_M.keyed(baseRace).copy();
		for (SettingBase<?> setting : settingList) {
			setting.pushSetting();
		}
	}
	private void pullSettings() {
		for (SettingBase<?> setting : settingList) {
			setting.pullSetting();
		}
	}
	private float updateSettings() {
		for (SettingBase<?> setting : settingList) {
			setting.updateGui();
		}
		return getTotalCost();
	}
	private void newSettingList() {
		spacerList  = new LinkedList<>();
		columnList  = new LinkedList<>();

		// ====================
		// First column (left)
		settingList.add(raceKey);
		settingList.add(new RaceName());
		settingList.add(new EmpireName());
		settingList.add(new RaceDescription1());
		settingList.add(new RaceDescription2());
		settingList.add(new RaceDescription4());
		settingList.add(new RaceDescription3());
		endOfColumn();

		// ====================
		// Second column
		settingList.add(personality.erratic);
		settingList.add(personality.pacifist);
		settingList.add(personality.honorable);
		settingList.add(personality.ruthless);
		settingList.add(personality.aggressive);
		settingList.add(personality.xenophobic);
		spacer();
		settingList.add(objective.militarist);
		settingList.add(objective.ecologist);
		settingList.add(objective.diplomat);
		settingList.add(objective.industrialist);
		settingList.add(objective.expansionist);
		settingList.add(objective.technologist);
		spacer();
		settingList.add(availableAI);
		settingList.add(new PreferredShipSize());
		settingList.add(new PreferredShipSet());
		spacer();
		settingList.add(new RacePrefix());
		settingList.add(new RaceSuffix());
		settingList.add(new LeaderPrefix());
		settingList.add(new LeaderSuffix());
		settingList.add(new WorldsPrefix());
		settingList.add(new WorldsSuffix());
		endOfColumn();

		// ====================
		// Third column
		settingList.add(techDiscovery);
		settingList.add(techDiscovery.computer);
		settingList.add(techDiscovery.construction);
		settingList.add(techDiscovery.forceField);
		settingList.add(techDiscovery.planet);
		settingList.add(techDiscovery.propulsion);
		settingList.add(techDiscovery.weapon);
		spacer();
//		settingList.add(new RacePlanetType()); // not yet differentiated
		settingList.add(new HomeworldSize());
//		settingList.add(new SpeciesType()); // Not used in Game
		settingList.add(new PopGrowRate());
		settingList.add(new IgnoresEco());
		spacer();
		settingList.add(new ProdWorker());
		settingList.add(new ProdControl());
		settingList.add(new IgnoresFactoryRefit());
		spacer();
		settingList.add(new ShipAttack());
		settingList.add(new ShipDefense());
		settingList.add(new ShipInitiative());
		settingList.add(new GroundAttack());
		spacer();
		settingList.add(new SpyCost());
		settingList.add(new SpySecurity());
		settingList.add(new SpyInfiltration());
//		settingList.add(new SpyTelepathy()); // Not used in Game
		endOfColumn();

		// ====================
		// Fourth column
		settingList.add(techResearch);
		settingList.add(techResearch.computer);
		settingList.add(techResearch.construction);
		settingList.add(techResearch.forceField);
		settingList.add(techResearch.planet);
		settingList.add(techResearch.propulsion);
		settingList.add(techResearch.weapon);
		spacer();
		settingList.add(new PlanetArtifacts()); // Backward compatibility: stay above PlanetRessources()
		settingList.add(new PlanetRessources());
		settingList.add(new PlanetEnvironment());
		spacer();
		settingList.add(new CreditsBonus());
		settingList.add(new HitPointsBonus());
		settingList.add(new MaintenanceBonus());
		settingList.add(new ShipSpaceBonus());
		spacer();
		settingList.add(new DiplomacyTrade());
// 		settingList.add(new DiploPosDP()); // Not used in Game
		settingList.add(new DiplomacyBonus());
		settingList.add(new DiplomacyCouncil());
		settingList.add(new RelationDefault());	// BR: Maybe All the races
		endOfColumn();

		// ====================
		// Fifth column
		// endOfColumn();
		// ====================
		guiList.add(randomSmoothEdges);
	    guiList.add(randomMin);
	    guiList.add(randomMax);
	    guiList.add(randomTargetMin);
	    guiList.add(randomTargetMax);
	    guiList.add(randomUseTarget);	    
	    for(SettingBase<?> setting : guiList)
	    	setting.hasNoCost(true);
	}
	private void endOfColumn()	{ columnList.add(settingList.size()); }
	private void spacer()		{ spacerList.add(settingList.size()); }
	private String fileToKey (File file)	{
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
	// ==================== Nested Classes ====================
	//
	// ==================== RaceList ====================
	//
	public class RaceList extends SettingBase<String> {
		private boolean newValue = false;
		private boolean reload	 = false;

		public RaceList() {
			super(ROOT, "RACE_LIST");
			isBullet(true);
			maxBullet(32);
			labelsAreFinals(true);
			hasNoCost(true);
			showFullGuide(false);
			reload();
		}
		// ---------- Initializers ----------
		//
		public void reload() {
			String currentValue = settingValue();
			clearLists();
			clearOptionsText();
			// Add Current race
			add((DynOptions) IGameOptions.playerCustomRace.get());
			defaultIndex(0);
			// Add existing files
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) {
					DynOptions opt = loadOptions(file);
					if (opt.size() == 0)
						System.err.println("Empty race file: " + file.getName());
					else {
						String key = RaceKey.getKey(opt);
						String fileKey = fileToKey(file);
						if (!key.equalsIgnoreCase(fileKey)) {
							RaceKey.setKey(opt, fileKey);
							DynOptions.saveOptions(opt, file);
						}
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
			speciesList.addAll(Arrays.asList(speciesDir.listFiles(SPECIES_FILTER)));
			// Sub Dir files
			File[] subDirectories = speciesDir.listFiles(File::isDirectory);
			for (File subDir : subDirectories)
				scanSubDir(speciesList, subDir);
		}
		private void add(DynOptions opt) {
			CustomRaceDefinitions cr = new CustomRaceDefinitions(opt);
			Race dr = cr.getRace();
			String cfgValue	 = dr.setupName;
			String langLabel = dr.id;
			String tooltipKey = dr.getDescription3();
			float cost = cr.getTotalCost();
			put(cfgValue, langLabel, cost, langLabel, tooltipKey);
		}
		private void add(String raceKey) {
			Race dr = R_M.keyed(raceKey);	    	
			String cfgValue	  = dr.id;
			String langLabel  = BASE_RACE_MARKER + dr.setupName();
			String tooltipKey = dr.getDescription3();
			CustomRaceDefinitions cr = new CustomRaceDefinitions(dr);
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
		public LinkedList<String> getAllowedAlienRaces() {
			LinkedList<String> list = new LinkedList<>();
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) {
					CustomRaceDefinitions cr = new CustomRaceDefinitions(loadOptions(file));
					if (cr.availableAI.settingValue())
						list.add(cr.raceKey.settingValue());
				}
			return list;
		}
		public LinkedList<String> getAllAlienRaces() {
			LinkedList<String> list = new LinkedList<>();
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) {
					CustomRaceDefinitions cr = new CustomRaceDefinitions(loadOptions(file));
					list.add(cr.raceKey.settingValue());
				}
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
				race = R_M.keyed(getCfgValue(settingValue())).copy();
				pullSettings();
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
		private static final String RACE_KEY = "RACE_KEY";
		static String getKey (DynOptions opts)				{ return opts.getString(ROOT + RACE_KEY); }
		static void setKey (DynOptions opts, String key)	{ opts.setString(ROOT + RACE_KEY, key); }

		private RaceKey() {
			super(ROOT, RACE_KEY, baseRace, 1);
			inputMessage("Enter the Race File Name");
			randomStr(RANDOMIZED_RACE_KEY);
		}
		@Override public void pushSetting() { race.id = settingValue(); }
		@Override public void pullSetting() { set(race.id); }
	}
	// ==================== RaceName ====================
	//
	private class RaceName extends SettingString {
		private RaceName() {
			super(ROOT, "RACE_NAME", "Custom Race", 1);
			inputMessage("Enter the Race Name");
			randomStr("Random Race");
		}
		@Override public void pushSetting() {
			race.parseRaceNames(settingValue());
			race.setupName = race.setupName();
		}
		@Override public void pullSetting() {
			String raceNames = String.join(",", race.raceNames());
			set(raceNames);
		}
	}
	// ==================== EmpireName ====================
	//
	private class EmpireName extends SettingString {
		private EmpireName() {
			super(ROOT, "RACE_EMPIRE_NAME", "Custom Empire", 1);
			inputMessage("Enter the Empire Designation");
			randomStr(CR_EMPIRE_NAME_RANDOM);
		}
		@Override public void pushSetting() { race.empireTitle(settingValue()); }
		@Override public void pullSetting() {
			if (race.empireTitle() == null)
				set(race.empireTitle());
			else
				set(race.empireTitle());				
		}
	}
	// ==================== RaceDescription1 ====================
	//
	private class RaceDescription1 extends SettingString {
		private RaceDescription1() {
			super(ROOT, "RACE_DESC_1", "Description 1", 2);
			inputMessage("Enter the Description");
			randomStr("Randomized");
		}
		@Override public void pushSetting() {
			race.setDescription1(settingValue());
		}
		@Override public void pullSetting() {
			set(race.getDescription1());		
		}
	}
	// ==================== RaceDescription2 ====================
	//
	private class RaceDescription2 extends SettingString {
		private RaceDescription2() {
			super(ROOT, "RACE_DESC_2", "Description 2", 2);
			inputMessage("Enter the Description");
			randomStr("Randomized");
		}
		@Override public void pushSetting() { race.setDescription2(settingValue()); }
		@Override public void pullSetting() { set(race.getDescription2()); }
	}
	// ==================== RaceDescription3 ====================
	//
	private class RaceDescription3 extends SettingString {
		private RaceDescription3() {
			super(ROOT, "RACE_DESC_3", "Description 3", 3);
			inputMessage("Enter the Description");
			randomStr("Randomized");
		}
		@Override public void pushSetting() { race.setDescription3(settingValue()); }
		@Override public void pullSetting() { set(race.getDescription3()); }
	}
	// ==================== RaceDescription4 ====================
	//
	private class RaceDescription4 extends SettingString {
		private RaceDescription4() {
			super(ROOT, "RACE_DESC_4", "Description 4", 3);
			inputMessage("Enter the Description");
			randomStr("Randomized");
		}
		@Override public void pushSetting() { race.setDescription4(settingValue()); }
		@Override public void pullSetting() { set(race.getDescription4()); }
	}
	// ==================== RacePrefix ====================
	//
	private class RacePrefix extends SettingString {
		private RacePrefix() {
			super(ROOT, "RACE_PREFIX", "@", 1);
			inputMessage("Enter the race Prefix");
			randomStr("#");
			isBullet(false);
		}
		@Override public void pushSetting() { race.racePrefix(settingValue()); }
		@Override public void pullSetting() { set(race.racePrefix()); }
	}
	// ==================== RaceSuffix ====================
	//
	private class RaceSuffix extends SettingString {
		private RaceSuffix() {
			super(ROOT, "RACE_SUFFIX", "", 1);
			inputMessage("Enter the race Suffix");
			randomStr("#");
			isBullet(false);
		}
		@Override public void pushSetting() { race.raceSuffix(settingValue()); }
		@Override public void pullSetting() { set(race.raceSuffix()); }
	}
	// ==================== LeaderPrefix ====================
	//
	private class LeaderPrefix extends SettingString {
		private LeaderPrefix() {
			super(ROOT, "LEADER_PREFIX", "@", 1);
			inputMessage("Enter the leader Prefix");
			randomStr("#");
			isBullet(false);
		}
		@Override public void pushSetting() { race.leaderPrefix(settingValue()); }
		@Override public void pullSetting() { set(race.leaderPrefix()); }
	}
	// ==================== LeaderSuffix ====================
	//
	private class LeaderSuffix extends SettingString {
		private LeaderSuffix() {
			super(ROOT, "LEADER_SUFFIX", "", 1);
			inputMessage("Enter the leader Suffix");
			randomStr("#");
			isBullet(false);
		}
		@Override public void pushSetting() { race.leaderSuffix(settingValue()); }
		@Override public void pullSetting() { set(race.leaderSuffix()); }
	}
	// ==================== WorldsPrefix ====================
	//
	private class WorldsPrefix extends SettingString {
		private WorldsPrefix() {
			super(ROOT, "WORLDS_PREFIX", "@", 1);
			inputMessage("Enter the worlds Prefix");
			randomStr("#");
			isBullet(false);
		}
		@Override public void pushSetting() { race.worldsPrefix(settingValue()); }
		@Override public void pullSetting() { set(race.worldsPrefix()); }
	}
	// ==================== WorldsSuffix ====================
	//
	private class WorldsSuffix extends SettingString {
		private WorldsSuffix() {
			super(ROOT, "WORLDS_SUFFIX", "", 1);
			inputMessage("Enter the worlds Suffix");
			randomStr("#");
			isBullet(false);
		}
		@Override public void pushSetting() { race.worldsSuffix(settingValue()); }
		@Override public void pullSetting() { set(race.worldsSuffix()); }
	}
	// ==================== AvailablePlayer ====================
	//
	@SuppressWarnings("unused")
	private class AvailablePlayer extends SettingBoolean {
		private static final boolean defaultValue = true;

		private AvailablePlayer() {
			super(ROOT, "AVAILABLE_PLAYER", defaultValue);
			isBullet(false);
			hasNoCost(true);
			initOptionsText();
		}
		@Override public void pushSetting() { race.availablePlayer(settingValue()); }
		@Override public void pullSetting() { set(race.availablePlayer()); }
	}
	// ==================== AvailableAI ====================
	//
	private class AvailableAI extends SettingBoolean {
		private static final boolean defaultValue = true;

		private AvailableAI() {
			super(ROOT, "AVAILABLE_AI", defaultValue);
			isBullet(true);
			hasNoCost(true);
			getToolTip();
			initOptionsText();
		}
		@Override public void pushSetting() { race.availableAI(settingValue()); }
		@Override public void pullSetting() { set(race.availableAI()); }
	}
	// ==================== PreferredShipSize ====================
	//
	private class PreferredShipSize extends SettingBase<String> {
		private static final String defaultValue = "Large";

		private PreferredShipSize() {
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
		@Override public void pushSetting() { race.preferredShipSize(index()); }
		@Override public void pullSetting() { index(race.preferredShipSize()); }
	}
	// ==================== PreferredShipSet ====================
	//
	private class PreferredShipSet extends SettingBase<String> {
		private static final String defaultValue = DISPLAY_RACE_SET;

		private PreferredShipSet() {
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
		@Override public void pushSetting() 		{ race.preferredShipSet(settingValue()); }
		@Override public void pullSetting() 		{ set(race.preferredShipSet()); }
	}
	// ==================== CRObjective ====================
	//
	private class CRObjective {
		float[] objectivePct	= new float[Personality.values().length];
		Militarist		militarist		= new Militarist();
		Ecologist		ecologist		= new Ecologist();
		Diplomat		diplomat		= new Diplomat();
		Industrialist	industrialist	= new Industrialist();
		Expansionist	expansionist	= new Expansionist();
		Technologist	technologist	= new Technologist();

		private CRObjective() {}
		void pushSetting() {
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

			race.objectivePct(objectivePct);
		}
		void pullSetting() {
			objectivePct = race.objectivePct();
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
			private Technologist() {
				super(ROOT, "TECHNOLOGIST", 0f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
			@Override public void pushSetting() { objective.pushSetting(); }
			@Override public void pullSetting() { objective.pullSetting(); }
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
			private Industrialist() {
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
			private Ecologist() {
				super(ROOT, "ECOLOGIST", 0.2f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Militarist ====================
		//
		private class Militarist extends SettingFloat {
			private Militarist() {
				super(ROOT, "MILITARIST", 0.1f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
	}
	// ==================== CRPersonality ====================
	//
	private class CRPersonality {
		float[] personalityPct	= new float[Personality.values().length];
		Erratic		erratic		= new Erratic();
		Pacifist	pacifist	= new Pacifist();
		Honorable	honorable	= new Honorable();
		Ruthless	ruthless	= new Ruthless();
		Aggressive	aggressive	= new Aggressive();
		Xenophobic	xenophobic	= new Xenophobic();

		private CRPersonality() {}
		void pushSetting() {
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

			race.personalityPct(personalityPct);
		}
		void pullSetting() {
			personalityPct = race.personalityPct();
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
			private Xenophobic() {
				super(ROOT, "XENOPHOBIC", 0f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
			@Override public void pushSetting() { personality.pushSetting(); }
			@Override public void pullSetting() { personality.pullSetting(); }
		}
		// ==================== Aggressive ====================
		//
		private class Aggressive extends SettingFloat {
			private Aggressive() {
				super(ROOT, "AGGRESSIVE", 0f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Ruthless ====================
		//
		private class Ruthless extends SettingFloat {
			private Ruthless() {
				super(ROOT, "RUTHLESS", 0.2f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Honorable ====================
		//
		private class Honorable extends SettingFloat {
			private Honorable() {
				super(ROOT, "HONORABLE", 0.5f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Pacifist ====================
		//
		private class Pacifist extends SettingFloat {
			private Pacifist() {
				super(ROOT, "PACIFIST", 0.2f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
		// ==================== Erratic ====================
		//
		private class Erratic extends SettingFloat {
			private Erratic() {
				super(ROOT, "ERRATIC", 0.1f, 0f, 1f, .01f, .05f, .20f);
				cfgFormat("%");
				hasNoCost(true);
			}
		}
	}
	// ==================== CreditsBonus ====================
	//
	private class CreditsBonus extends SettingInteger {
		// big = good
		private CreditsBonus() {
			super(ROOT, "CREDIT", 0, 0, 35, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .8f}, new float[]{0f, .8f});
		}
		@Override public void pushSetting() { race.bCBonus(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.bCBonus() * 100)); }
	}
	// ==================== HitPointsBonus ====================
	//
	private class HitPointsBonus extends SettingInteger {
		// big = good
		private HitPointsBonus() {
			super(ROOT, "HIT_POINTS", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .6f});
		}
		@Override public void pushSetting() { race.hPFactor(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.hPFactor() * 100)); }
	}
	// ==================== ShipSpaceBonus ====================
	//
	// Absolute min = ? .75 not OK for colony building!
	private class ShipSpaceBonus extends SettingInteger {
		// big = good
		private ShipSpaceBonus() {
			super(ROOT, "SHIP_SPACE", 100, 80, 175, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1f}, new float[]{0f, 2f});
		}
		@Override public void pushSetting() { race.shipSpaceFactor(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.shipSpaceFactor() * 100)); }
	}
	// ==================== MaintenanceBonus ====================
	//
	private class MaintenanceBonus extends SettingInteger {
		// Big = bad
		public MaintenanceBonus() {
			super(ROOT, "MAINTENANCE", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, -.2f}, new float[]{0f, -.4f});
		}
		@Override public void pushSetting() { race.maintenanceFactor(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.maintenanceFactor() * 100f)); }
	}
	// ==================== PlanetRessources ====================
	//
	private class PlanetRessources extends SettingBase<String> {
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

		@Override public void pushSetting() { race.planetRessource(settingValue()); }
		@Override public void pullSetting() { set(race.planetRessource()); }
	}
	// ==================== PlanetArtifacts ====================
	//
	private class PlanetArtifacts extends SettingBase<String> {
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
		@Override public void pushSetting() { race.planetArtifacts(settingValue()); }
		@Override public void pullSetting() { set(race.planetArtifacts()); }
	}
	// ==================== PlanetEnvironment ====================
	//
	private class PlanetEnvironment extends SettingBase<String> {
		private static final String defaultValue = "Normal";

		private PlanetEnvironment() {
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
		@Override public void pushSetting() { race.planetEnvironment(settingValue()); }
		@Override public void pullSetting() { set(race.planetEnvironment()); }
	}
	// ==================== PlanetType ====================
	//
	@SuppressWarnings("unused")
	private class RacePlanetType extends SettingBase<String> {
		private static final String defaultValue = "Terran";

		private RacePlanetType() {
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
		@Override public void pushSetting() { race.homeworldPlanetType(settingValue()); }
		@Override public void pullSetting() { set(race.homeworldPlanetType()); }
	}
	// ==================== HomeworldSize ====================
	//
	private class HomeworldSize extends SettingInteger {
		private HomeworldSize() {
			super(ROOT, "HOME_SIZE", 100, 70, 150, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .7f});
		}
		@Override public void pushSetting() { race.homeworldSize(settingValue()); }
		@Override public void pullSetting() { set(race.homeworldSize()); }
	}
	// ==================== SpeciesType ====================
	//
	@SuppressWarnings("unused")
	private class SpeciesType extends SettingBase<Integer> {
		private static final String defaultValue = "Terran";

		private SpeciesType() {
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
		@Override public void pushSetting() { race.speciesType(settingValue()); }
		@Override public void pullSetting() { set(race.speciesType()); }
	}
	// ==================== IgnoreEco ====================
	//
	private class IgnoresEco extends SettingBase<String> {
		private static final String defaultValue = "No";

		private IgnoresEco() {
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
		@Override public void pushSetting() {
			race.acceptedPlanetEnvironment(settingValue());
			race.ignoresPlanetEnvironment(!settingValue().equalsIgnoreCase("No"));
		}
		@Override public void pullSetting() { set(race.acceptedPlanetEnvironment()); }
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
	private class PopGrowRate extends SettingInteger {
		private PopGrowRate() {
			super(ROOT, "POP_GROW_RATE", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .3f});
		}
		@Override public void pushSetting() { race.growthRateMod(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round (race.growthRateMod() * 100)); }
	}
	// ==================== ShipAttack ====================
	//
	private class ShipAttack extends SettingInteger {
		private ShipAttack() {
			super(ROOT, "SHIP_ATTACK", 0, -1, 5, 1, 1, 1,
					DIFFERENCE, new float[]{0f, 3f}, new float[]{0f, 5f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.shipAttackBonus(settingValue()); }
		@Override public void pullSetting() { set(race.shipAttackBonus()); }
	}
	// ==================== ShipDefense ====================
	//
	private class ShipDefense extends SettingInteger {
		private ShipDefense() {
			super(ROOT, "SHIP_DEFENSE", 0, -1, 5, 1, 1, 1,
					DIFFERENCE, new float[]{0f, 1.5f, 1.5f}, new float[]{0f, 6f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.shipDefenseBonus(settingValue()); }
		@Override public void pullSetting() { set(race.shipDefenseBonus()); }
	}
	// ==================== ShipInitiative ====================
	//
	private class ShipInitiative extends SettingInteger {
		private ShipInitiative() {
			super(ROOT, "SHIP_INITIATIVE", 0, -1, 5, 1, 1, 1,
					DIFFERENCE, new float[]{5f, 1f}, new float[]{0f, 6f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.shipInitiativeBonus(settingValue()); }
		@Override public void pullSetting() { set(race.shipInitiativeBonus()); }
	}
	// ==================== GroundAttack ====================
	//
	private class GroundAttack extends SettingInteger {
		private GroundAttack() {
			super(ROOT, "GROUND_ATTACK", 0, -20, 30, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1.25f}, new float[]{0f, 0.75f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.groundAttackBonus(settingValue()); }
		@Override public void pullSetting() { set(race.groundAttackBonus()); }
	}
	// ==================== SpyCost ====================
	//
	private class SpyCost extends SettingInteger {
		private SpyCost() {
			super(ROOT, "SPY_COST", 100, 50, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, -.1f}, new float[]{0f, -.2f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.spyCostMod(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.spyCostMod() * 100)); }
	}
	// ==================== SpySecurity ====================
	//
	private class SpySecurity extends SettingInteger {
		private SpySecurity() {
			super(ROOT, "SPY_SECURITY", 0, -20, 40, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1f}, new float[]{0f, 2f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.internalSecurityAdj(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.internalSecurityAdj() * 100)); }
	}
	// ==================== SpyInfiltration ====================
	//
	private class SpyInfiltration extends SettingInteger {
		private SpyInfiltration() {
			super(ROOT, "SPY_INFILTRATION", 0, -20, 40, 1, 5, 20,
					DIFFERENCE, new float[]{0f, 1.25f}, new float[]{0f, 2.5f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.spyInfiltrationAdj(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.spyInfiltrationAdj() * 100)); }
	}
	// ==================== SpyTelepathy ====================
	//
	@SuppressWarnings("unused")
	private class SpyTelepathy extends SettingBoolean {
		private static final boolean defaultValue = false;

		private SpyTelepathy() {
			super(ROOT, "SPY_TELEPATHY", defaultValue, 20f, 0f);
			isBullet(booleansAreBullet);
			initOptionsText();
		}
		@Override public void pushSetting() { race.telepathic(settingValue()); }
		@Override public void pullSetting() { set(race.telepathic()); }
	}
	// ==================== DiplomacyTrade ====================
	//
	private class DiplomacyTrade extends SettingInteger {
		private DiplomacyTrade() {
			super(ROOT, "DIPLOMACY_TRADE", 0, -30, 30, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .3f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.tradePctBonus(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.tradePctBonus() * 100)); }
	}
	// ==================== DiploPosDP ====================
	//
	@SuppressWarnings("unused")
	private class DiploPosDP extends SettingInteger {
		private DiploPosDP() {
			super(ROOT, "DIPLO_POS_DP", 100, 70, 200, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .3f}, new float[]{0f, .8f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.positiveDPMod(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.positiveDPMod() * 100)); }
	}
	// ==================== DiplomacyBonus ====================
	//
	private class DiplomacyBonus extends SettingInteger {
		private DiplomacyBonus() {
			super(ROOT, "DIPLOMACY_BONUS", 0, -50, 100, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .1f}, new float[]{0f, .2f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.diplomacyBonus(settingValue()); }
		@Override public void pullSetting() { set(race.diplomacyBonus()); }
	}
	// ==================== DiplomacyCouncil ====================
	//
	private class DiplomacyCouncil extends SettingInteger {
		private DiplomacyCouncil() {
			super(ROOT, "DIPLOMACY_COUNCIL", 0, -25, 25, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .2f}, new float[]{0f, .2f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.councilBonus(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.councilBonus() * 100)); }
	}
	// ==================== RelationDefault ====================
	//
	private class RelationDefault extends SettingInteger {
		private RelationDefault() {
			super(ROOT, "RELATION_DEFAULT", 0, -10, 10, 1, 2, 4,
					DIFFERENCE, new float[]{0f, .4f}, new float[]{0f, .4f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.defaultRaceRelations(settingValue()); }
		@Override public void pullSetting() { set((int)race.defaultRaceRelations()); }
	}
	// ==================== ProdWorker ====================
	//
	private class ProdWorker extends SettingInteger {
		// bigger = better
		private ProdWorker() {
			super(ROOT, "PROD_WORKER", 100, 70, 300, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .8f, 0f}, new float[]{0f, 0.8f, 0.01f});
			initOptionsText();
		}
		@Override public void pushSetting() { race.workerProductivityMod(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.workerProductivityMod() * 100)); }
	}
	// ==================== ProdControl ====================
	//
	private class ProdControl extends SettingInteger {
		private ProdControl() {
			super(ROOT, "PROD_CONTROL", 0, -1, 4, 1, 1, 1,
					DIFFERENCE, new float[]{0f, 15f, 0f}, new float[]{0f, 30f, 0f});
			pctValue(false);
			initOptionsText();
		}
		@Override public void pushSetting() { race.robotControlsAdj(settingValue()); }
		@Override public void pullSetting() { set(race.robotControlsAdj()); }
	}
	// ==================== IgnoresFactoryRefit ====================
	//
	private class IgnoresFactoryRefit extends SettingBoolean {
		private static final boolean defaultValue = false;

		private IgnoresFactoryRefit() {
			super(ROOT, "PROD_REFIT_COST", defaultValue, 40f, 0f);
			isBullet(booleansAreBullet);
			initOptionsText();
		}
		@Override public void pushSetting() { race.ignoresFactoryRefit(settingValue()); }
		@Override public void pullSetting() { set(race.ignoresFactoryRefit()); }
	}
	// ==================== TechResearch ====================
	//
	private class TechResearch extends SettingInteger {

		ResearchComputer		computer	= new ResearchComputer();
		ResearchConstruction	construction= new ResearchConstruction();
		ResearchForceField		forceField	= new ResearchForceField();
		ResearchPlanet			planet		= new ResearchPlanet();
		ResearchPropulsion		propulsion	= new ResearchPropulsion();
		ResearchWeapon			weapon		= new ResearchWeapon();

		private TechResearch() {
			super(ROOT, "TECH_RESEARCH", 100, 60, 200, 1, 5, 20, DIFFERENCE,
					new float[]{0f, 0.7f, 0.004f},
					new float[]{0f, 1.0f, 0.006f});
			hasNoCost(true);
			initOptionsText();
		}
		@Override public void pushSetting() { race.researchBonusPct(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.researchBonusPct() * 100)); }
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

		private String costString(float cost) {
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
			private ResearchComputer() {
				super("RESEARCH_COMPUTER");
				initOptionsText();
			}
			@Override public void pushSetting() { race.techMod(0, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.techMod(0) * 100)); }
		}
		// ==================== ResearchConstruction ====================
		//
		private class ResearchConstruction extends SettingResearch {
			private ResearchConstruction() {
				super("RESEARCH_CONSTRUCTION");
				initOptionsText();
			}
			@Override public void pushSetting() { race.techMod(1, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.techMod(1) * 100)); }
		}
		// ==================== ResearchForceField ====================
		//
		private class ResearchForceField extends SettingResearch {
			private ResearchForceField() {
				super("RESEARCH_FORCEFIELD");
				initOptionsText();
			}
			@Override public void pushSetting() { race.techMod(2, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.techMod(2) * 100)); }
		}
		// ==================== ResearchPlanet ====================
		//
		private class ResearchPlanet extends SettingResearch {
			private ResearchPlanet() {
				super("RESEARCH_PLANET");
				initOptionsText();
			}
			@Override public void pushSetting() { race.techMod(3, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.techMod(3) * 100)); }
		}
		// ==================== ResearchPropulsion ====================
		//
		private class ResearchPropulsion extends SettingResearch {
			private ResearchPropulsion() {
				super("RESEARCH_PROPULSION");
				initOptionsText();
			}
			@Override public void pushSetting() { race.techMod(4, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.techMod(4) * 100)); }
		}
		// ==================== ResearchWeapon ====================
		//
		private class ResearchWeapon extends SettingResearch {
			private ResearchWeapon() {
				super("RESEARCH_WEAPON");
				initOptionsText();
			}
			@Override public void pushSetting() { race.techMod(5, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.techMod(5) * 100)); }
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

			private String costString(float cost) {
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
	private class TechDiscovery extends SettingInteger {

		DiscoveryComputer	  computer		= new DiscoveryComputer();
		DiscoveryConstruction construction	= new DiscoveryConstruction();
		DiscoveryForceField	  forceField	= new DiscoveryForceField();
		DiscoveryPlanet		  planet		= new DiscoveryPlanet();
		DiscoveryPropulsion	  propulsion	= new DiscoveryPropulsion();
		DiscoveryWeapon		  weapon		= new DiscoveryWeapon();

		private TechDiscovery() {
			super(ROOT, "TECH_DISCOVERY", 50, 0, 100, 1, 5, 20,
					DIFFERENCE, new float[]{0f, .5f}, new float[]{0f, 0.5f});
			hasNoCost(true);
			initOptionsText();
		}
		@Override public void pushSetting() { race.techDiscoveryPct(settingValue()/100f); }
		@Override public void pullSetting() { set(Math.round(race.techDiscoveryPct() * 100)); }
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

		private String costString(float cost) {
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
			private DiscoveryComputer() {
				super("DISCOVERY_COMPUTER");
				initOptionsText();
			}
			@Override public void pushSetting() { race.discoveryMod(0, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.discoveryMod(0) * 100)); }
		}
		// ==================== DiscoveryConstruction ====================
		//
		private class DiscoveryConstruction extends SettingDiscovery {
			private DiscoveryConstruction() {
				super("DISCOVERY_CONSTRUCTION");
				initOptionsText();
			}
			@Override public void pushSetting() { race.discoveryMod(1, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.discoveryMod(1) * 100)); }
		}
		// ==================== DiscoveryForceField ====================
		//
		private class DiscoveryForceField extends SettingDiscovery {
			private DiscoveryForceField() {
				super("DISCOVERY_FORCEFIELD");
				initOptionsText();
			}
			@Override public void pushSetting() { race.discoveryMod(2, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.discoveryMod(2) * 100)); }
		}
		// ==================== DiscoveryPlanet ====================
		//
		private class DiscoveryPlanet extends SettingDiscovery {
			private DiscoveryPlanet() {
				super("DISCOVERY_PLANET");
				initOptionsText();
			}
			@Override public void pushSetting() { race.discoveryMod(3, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.discoveryMod(3) * 100)); }
		}
		// ==================== DiscoveryPropulsion ====================
		//
		private class DiscoveryPropulsion extends SettingDiscovery {
			private DiscoveryPropulsion() {
				super("DISCOVERY_PROPULSION");
				initOptionsText();
			}
			@Override public void pushSetting() { race.discoveryMod(4, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.discoveryMod(4) * 100)); }
		}
		// ==================== DiscoveryWeapon ====================
		//
		private class DiscoveryWeapon extends SettingDiscovery {
			private DiscoveryWeapon() {
				super("DISCOVERY_WEAPON");
				initOptionsText();
			}
			@Override public void pushSetting() { race.discoveryMod(5, settingValue()/100f); }
			@Override public void pullSetting() { set(Math.round(race.discoveryMod(5) * 100)); }
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
