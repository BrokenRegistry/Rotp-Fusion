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
import static rotp.model.game.IRaceOptions.defaultRaceKey;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import rotp.Rotp;
import rotp.model.empires.species.SpeciesSettings.Technologies.TechDiscovery;
import rotp.model.empires.species.SpeciesSettings.Technologies.TechResearch;
import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.game.BaseModPanel;
import rotp.ui.util.StringList;
import rotp.util.Base;

public class SkillsFactory extends SpeciesSettings {

	private static SkillsFactory reworkFactory, raceListFactory;
	public void cleanFactories()	{
		reworkFactory	= null;
		raceListFactory	= null;
	}
	SkillsFactory()	{}

	public final SettingInteger randomTargetMax	= new SettingInteger(ROOT, "RANDOM_TARGET_MAX", 75, null, null, 1, 5, 20).pctValue(false);
	public final SettingInteger randomTargetMin	= new SettingInteger(ROOT, "RANDOM_TARGET_MIN", 0, null, null, 1, 5, 20).pctValue(false);
	public final SettingInteger randomMax		= new SettingInteger(ROOT, "RANDOM_MAX", 25, -100, 100, 1, 5, 20);
	public final SettingInteger randomMin		= new SettingInteger(ROOT, "RANDOM_MIN", -25, -100, 100, 1, 5, 20);
	public final SettingBoolean randomUseTarget	= new SettingBoolean(ROOT, "RANDOM_USE_TARGET", false, true);
	public final SettingBoolean randomSmoothEdges = new SettingBoolean(ROOT, "RANDOM_EDGES", true, true);

	private List<Integer> spacerList; // For UI
	private List<Integer> columnList; // For UI
	private RaceList raceList;
	private AvailableAI	  availableAI	= new AvailableAI();
	private CRPersonality personality	= new CRPersonality();
	private CRObjective	  objective		= new CRObjective();
	private RaceKey		  raceKey		= new RaceKey();
	private Technologies  technologies	= new Technologies();
	private TechDiscovery techDiscovery	= technologies.techDiscovery;
	private TechResearch  techResearch	= technologies.techResearch;

	// #========== Constructors For Custom Skills Editors and Viewer ==========
	//
	public static SkillsFactory getSkillsFactoryForEditor(BaseModPanel parent)	{
		SkillsFactory factory = new SkillsFactory();
		factory.parent = parent;
		factory.initFactoryForEdit();
		return factory;
	}
	public void languageChanged()	{
		cleanFactories();
		String previousLanguage = workingLanguageCode;
		String newLanguage = selectedLanguageDir();
		if (newLanguage.equals(previousLanguage))
			return;
		updateEmpireSpeciesMap();
		settingMap.languageChanged(previousLanguage, newLanguage);
	}
	SpeciesSkills getRawRace()	{ return race(); }
	public String getRaceKey()	{ return raceKey(); }
	public List<ICRSettings> settingList()	{ return settingMap.getSettings(); }
	public List<ICRSettings> guiList()		{ return settingMap.getGuis(); }
	public List<Integer>	 spacerList()	{ return spacerList; }
	public List<Integer>	 columnList()	{ return columnList; }
	public RaceList initRaceList()	{ 
		raceList = new RaceList();
		return raceList;
	}
	public float getTotalCost() {
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
	/**
	 * Settings to DynOptions
	 * @return DynOptions
	 */
	public DynOptions getAsOptions() {
		DynOptions destOptions = new DynOptions();
		for (ICRSettings setting : settingMap.getAll()) {
			// System.out.println(setting.toString()); // TO DO BR: REMOVE
			setting.updateOption(destOptions);
		}
		return destOptions;
	}
	public String getPlayerAnim()	{ return animSkills==null? null : animSkills.id; }
	private void initSkillsForEditor(DynOptions srcOptions)	{
		String skillKey = AnimationRaceKey.getRawReworkedKey(srcOptions);
		initWithInternalSkillsForGalaxy(null, skillKey, false);

		isReference(false);
		// Create missing language setting attributes
		String languageKey = ROOT + LanguageList.KEY;
		// current languages
		LanguageList languageSetting = (LanguageList) settingMap.get(languageKey);
		StringList languages = new StringList(languageSetting.settingValue());
		// src options languages
		String srcLanguages = srcOptions.getString(languageKey, DEFAULT_LANGUAGE);
		StringList srcLanguageList = new StringList(srcLanguages);
		// Create missing
		for (String lg : srcLanguageList) {
			if (!languages.contains(lg))
				languages.add(lg);
		}
		for (String lg : languages) {
			new SpeciesAttributes(lg);
		}
		// update language setting
		languageSetting.set(languages.asString());

		// update the skills from the source option
		List<ICRSettings> settings = settingMap.getAll();
		settings.remove(languageSetting);
		for (ICRSettings setting : settings)
			setting.updateOptionTool(srcOptions);

		// The previous options could offer more languages.
		settingMap.cleanLanguages();
		for (ICRSettings setting : settings)
			setting.settingToSkill(race());
		languageSetting.settingToSkill(race());

		// Fills the skills options from the settings
		DynOptions destOptions = race().speciesOptions();
		for (ICRSettings setting : settings)
			setting.updateOption(destOptions);
		languageSetting.updateOption(destOptions);
	}
	/**
	 * DynOptions to settings and race
	 * @param srcOptions
	 */
	public void setSettingTools(DynOptions srcOptions)	{ initSkillsForEditor(srcOptions); }
	/**
	 * race to settings
	 */
	void setFromRaceToShow(SpeciesSkills skills, DynOptions srcOptions) {
		race(skills);
		isForShow(true);
		if (skills.isCustomSpecies() && srcOptions != null)
			for (ICRSettings setting : settingMap.getSettings())
				setting.updateOptionTool(srcOptions);
		else
			for (ICRSettings setting : settingMap.getSettings())
				setting.skillToSetting(race());
		// The previous options could offer more languages.
		isForShow(false);
	}
	private void saveSettings(String path, String fileName)	{ getAsOptions().save(path, fileName); }
	public void saveRace()	{ saveSettings(speciesDirectoryPath(), fileName()); }
	public void loadRace()	{
		String raceKey = raceKey();
		if (raceKey.isEmpty())
			raceKey = defaultRaceKey;

		if (Species.isValidKey(raceKey)) {
			race(Species.getAnim(raceKey).copy(true));
			for (ICRSettings setting : settingMap.getAll())
				setting.skillToSetting(race());
		}
		else
			setSettingTools(loadOptions(speciesDirectoryPath(), fileName()));
		// The previous options could offer more languages.
		settingMap.cleanLanguages();
	}
	private void initFactoryForEdit()	{
		race(Species.getAnim(defaultRaceKey).copy(true));
		// Create the settings if necessary
		newSettingList(true);
	}
	// -#-
	// #========== Constructors For Restart UI Factory ==========
	//
	public SkillsFactory (DynOptions srcOptions)	{
		initSkillsForGalaxy(null, srcOptions);
	}
	public SkillsFactory (String internalRaceKey)	{
		initWithInternalSkillsForGalaxy(null, internalRaceKey, false);
	}
	// -#-
	// #========== Constructors For Galaxy Factory ==========
	//
	private SkillsFactory (SpeciesSkills anim, DynOptions srcOptions)	{
		initSkillsForGalaxy(anim, srcOptions);
	}
//	public SkillsFactory (SpeciesSkills anim, String internalRaceKey, boolean fullCopy)	{
//		initWithInternalSkillsForGalaxy(anim, internalRaceKey, fullCopy);
//	}

	static SpeciesSkills optionToSkills(SpeciesSkills anim, DynOptions srcOptions)	{
		return new SkillsFactory(anim, srcOptions).race();
	}
//	public static SpeciesSkills fileToSkills(SpeciesSkills anim, String fileName)	{
//		SkillsFactory factory = new SkillsFactory();
//		factory.initSkillsForGalaxy(anim, loadOptions(speciesDirectoryPath(), fileName + EXT));
//		return factory.race();
//	}
//	public static HashMap<String, StringList> getAnimationMap()	{ return newRaceList().animationMap(); }
//	public static StringList getAllAlienSkills()		{ return newRaceList().getAllAlienRaces(); }
	public static StringList getAllowedAlienSkills()	{ return newRaceList().getAllowedAlienRaces(); }
	static SkillsFactory getSkillsFactoryForGalaxy()	{
		SkillsFactory factory = new SkillsFactory();
		factory.initWithDefaultSkillsForGalaxy(true);
		return factory;
	}
	private static RaceList newRaceList()	{
		SkillsFactory factory = new SkillsFactory();
		factory.initWithDefaultSkillsForGalaxy(false);
		return factory.new RaceList();
	}

	private void initSkillsForGalaxy(SpeciesSkills anim, DynOptions srcOptions)	{
		String skillKey = AnimationRaceKey.getRawReworkedKey(srcOptions);
		initWithInternalSkillsForGalaxy(anim, skillKey, false);

		isReference(false);
		// update the skills from the source option
		List<ICRSettings> settings = settingMap.getAll();
		for (ICRSettings setting : settings)
			setting.updateOptionTool(srcOptions);

		// Fills the skills options from the settings
		DynOptions destOptions = race().speciesOptions();
		for (ICRSettings setting : settings)
			setting.updateOption(destOptions);

		// Fills with selected settings
		for (ICRSettings setting : settings)
			setting.settingToSkill(race());

		// The previous options could offer more languages.
		settingMap.cleanLanguages();
	}
	private void initWithInternalSkillsForGalaxy(SpeciesSkills anim, String SkillsKey, boolean fullCopy)	{
		if (SkillsKey == null) {
			if (anim == null)
				initWithDefaultSkillsForGalaxy(fullCopy);
			else
				initWithAnimSkillsForGalaxy(anim, fullCopy);
			return;
		}
		race(Species.getAnim(SkillsKey).copy(fullCopy));
		// Create the settings if necessary
		newSettingList(true);

		// Fills the basic settings with skills
		List<ICRSettings> settings = settingMap.getSettings();
		for (ICRSettings setting : settings)
			setting.skillToSetting(race());
	}
	private void initWithDefaultSkillsForGalaxy(boolean fullCopy)	{
		race(Species.getAnim(defaultRaceKey).copy(fullCopy));
		// Create the settings if necessary
		newSettingList(true);
		// Fills with default settings (instead of default Species settings)
		List<ICRSettings> settings = settingMap.getAll();
		for (ICRSettings setting : settings)
			setting.settingToSkill(race());
	}
	private void initWithAnimSkillsForGalaxy(SpeciesSkills anim, boolean fullCopy)	{
		race(anim.copy(fullCopy));
		// Create the settings if necessary
		newSettingList(true);
		// Fills the basic settings with skills
		List<ICRSettings> settings = settingMap.getSettings();
		for (ICRSettings setting : settings)
			setting.skillToSetting(race());
	}
	// -#-
	// #========== Constructors For Species ==========
	//
	static SpeciesSkills keyToCustomSpecies(SpeciesSkills anim, String skillsKey)	{
		SkillsFactory factory = new SkillsFactory();
		factory.initSkillsForSpecies(anim, skillsKey);
		return factory.race();
	}
	static SpeciesSkills fileToAlienRaceInfo(SpeciesSkills anim, String skillsKey)	{
		SkillsFactory factory = new SkillsFactory();
		factory.initSkillsForSpecies(anim, skillsKey);
		return factory.race();
	}
	private void initSkillsForSpecies(SpeciesSkills anim, String skillsKey)	{
		switch (skillsKey) {
			case RANDOM_RACE_KEY:
				initWithInternalSkillsForGalaxy(anim, null, false);
//				race(Species.getAnim(defaultRaceKey).copy(false));
				race().isCustomSpecies(true);
				race().isRandomizedSpecies(true);
				randomizeRace(randomAlienRacesMin.get(), randomAlienRacesMax.get(),
						randomAlienRacesTargetMin.get(), randomAlienRacesTargetMax.get(),
						randomAlienRaces.isTarget(), randomAlienRacesSmoothEdges.get(), false);
				return;
			case CUSTOM_RACE_KEY:
				initWithInternalSkillsForGalaxy(anim, null, false);
				initSkillsForEditor((DynOptions) IGameOptions.playerCustomRace.get());
				race().isCustomSpecies(true);
//				initSkillsForGalaxy(anim, (DynOptions) IGameOptions.playerCustomRace.get());
				return;
			default:
				if(skillsKey.startsWith(BASE_RACE_MARKER)) {
					String key = Species.languageToKey(skillsKey.substring(1));
					if (key == null)
						key = defaultRaceKey;
					race(Species.getAnim(key).copy(true));
					return;
				}
				else {
					initSkillsForGalaxy(anim, loadOptions(speciesDirectoryPath(), skillsKey + EXT));
					return;
				}
		}
	}
	// -#-
	// #========== Constructors For Param CR ==========
	// Called during Startup, will be reinitialized later
	//
	public static DynOptions getDefaultOptions()	{
		SkillsFactory factory = new SkillsFactory();
		factory.initFactoryForParamCR();
		return factory.race().speciesOptions();
	}
	private void initFactoryForParamCR()	{
		race(Species.getAnim(defaultRaceKey).copy(true));
		newSettingList(true);
	}
	// -#-
	// #========== Constructors For RaceList ==========
	// RaceList will only use data from SpeciesSkills
	//
	private static SkillsFactory getFactoryForRaceList(DynOptions srcOptions)	{
		if (raceListFactory == null)
			raceListFactory = new SkillsFactory();
		raceListFactory.initSkillsForRaceList(srcOptions);
		return raceListFactory;
	}
	private static SkillsFactory getFactoryForRaceList(Species species)	{
		if (raceListFactory == null)
			raceListFactory = new SkillsFactory();
		raceListFactory.initSkillsForRaceList(species);
		return raceListFactory;
	}
	private void initSkillsForRaceList(DynOptions srcOptions)	{
		race(Species.getAnim(defaultRaceKey).copy(false));
		newSettingList(false);

		// update default setting value from source options
		List<ICRSettings> settings = settingMap.getSettings();
		for (ICRSettings setting : settings)
			setting.updateOptionTool(srcOptions);

		// Then push the settings to the SpeciesSkills
		for (ICRSettings setting : settings)
			setting.settingToSkill(race());

		race().isCustomSpecies(true);
	}
	private void initSkillsForRaceList(Species species)	{
		race(species.getSkillCopy(false));
		newSettingList(false);

		List<ICRSettings> settings = settingMap.getSettings();
		for (ICRSettings setting : settings)
			setting.skillToSetting(race());

		race().isCustomSpecies(true);
	}
	// -#-
	// #========== Constructors For Reworked ==========
	//
	static SpeciesSkills getMasterSkillsForReworked(String key)	{
		if (reworkFactory == null)
			reworkFactory = new SkillsFactory();
		reworkFactory.initForReworked(key);
		return reworkFactory.race();
	}
	private void initForReworked(String key)	{
		isReference(true);
		// Get a copy of the Master Skills
		if(Species.isValidKey(key))
			race(Species.getAnim(key).copy(true));
		else
			race(Species.getAnim(defaultRaceKey).copy(true));

		// Create the settings if necessary
		newSettingList(true);

		// Fills the settings from the Master skills
		List<ICRSettings> settings = settingMap.getAll();
		for (ICRSettings setting : settings)
			setting.skillToSetting(race());

		// Fills the skills options from the settings
		DynOptions destOptions = race().speciesOptions();
		for (ICRSettings setting : settings)
			setting.updateOption(destOptions);
		isReference(false);
	}
	// -#-
	// #========== Constructors For Other Gui ==========
	//
	public static StringList getBaseRaceList()	{
		return getRaceFileList()
				.stream()
				.filter(c -> c.startsWith(BASE_RACE_MARKER))
				.collect(Collectors.toCollection(StringList::new));
	}
	private static StringList getRaceFileList()	{ return newRaceList().getLabels(); }
	// -#-
	// #========== Tools ==========
	//
	private static void backwardComp(DynOptions opts) {
		if (opts.getString(ROOT + "HOME_RESOURCES", "").equalsIgnoreCase("Artifacts")) {
			opts.setString(ROOT + "HOME_RESOURCES", "Normal");
			opts.setString(ROOT + "HOME_ARTIFACTS", "Artifacts");
		}
	}
	private static DynOptions loadOptions(File saveFile) {
		DynOptions opts =  DynOptions.loadOptions(saveFile);
		backwardComp(opts);
		return opts;
	}
	private static DynOptions loadOptions(String path, String fileName) {
		DynOptions opts =  DynOptions.loadOptions(path, fileName);
		backwardComp(opts);
		return opts;
	}
	// -#-
	// #========== Randomized Skills ==========
	//
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
		randomizeRace(randomMin.settingValue(), randomMax.settingValue(),
			randomTargetMin.settingValue(), randomTargetMax.settingValue(),
			randomUseTarget.settingValue(), randomSmoothEdges.settingValue(), updateGui);
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

		DynOptions destOptions = race().speciesOptions();
		for (ICRSettings setting : settingMap.getSettings()) {
			setting.settingToSkill(race());
			setting.updateOption(destOptions);
		}
	}	
	// -#-
	// #========== Setting initialization ==========
	//
	private void newSettingList(boolean clear) {
		if(settingMap.filled) {
			if (clear)
				for (ICRSettings setting : settingMap.getAll())
					setting.setFromDefault(false, true);
			return;
		}
		String dir = selectedLanguageDir();
		spacerList  = new LinkedList<>();
		columnList  = new LinkedList<>();

		// ====================
		// First column (left)
		settingMap.add(new AnimationRaceKey());
		settingMap.add(raceKey);
		settingMap.addAttribute(new RaceName(dir));
		settingMap.add(new LeaderTitle(dir));
		settingMap.add(new LeaderFullTitle(dir));
		settingMap.addAttribute(new EmpireName(dir));
		for (int i : new int[]{ 1, 2, 4, 3 })
			settingMap.add(new RaceDescription(i, dir));
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
//		settingMap.add(new RacePlanetType()); // not yet differentiated
		settingMap.add(new HomeworldSize());
//		settingMap.add(new SpeciesType()); // Not used in Game
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
//		settingMap.add(new SpyTelepathy()); // Not used in Game
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
// 		settingMap.add(new DiploPosDP()); // Not used in Game
		settingMap.add(new DiplomacyBonus());
		settingMap.add(new DiplomacyCouncil());
		settingMap.add(new RelationDefault());	// BR: Maybe All the races
		endOfColumn();

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

		// ====================
		// Other non displayed attributes
		// ====================
		settingMap.addAttribute(new LanguageList());
		settingMap.addAttribute(new AnimationId());
		settingMap.addAttribute(new AnimReady(dir));
		new SpeciesAttributes(dir);

		settingMap.filled = true;
	}
	private void endOfColumn()	{ columnList.add(settingMap.getSettings().size()); }
	private void spacer()		{ spacerList.add(settingMap.getSettings().size()); }
	
	public boolean autoUpdate(KeyEvent e, RaceList raceList)	{ // For developers only
		if(!(Rotp.isIDE() && e.isShiftDown() && e.isControlDown()))
			return false;
		LinkedList<String> values = raceList.getValues();
		values.removeFirst(); // custom Player
		for (int i=0; i<16; i++)
			values.removeLast(); // default species

		List<String> before	= new ArrayList<>();
		List<String> dual_b	= new ArrayList<>();
		List<String> after	= new ArrayList<>();
		List<String> dual_a	= new ArrayList<>();
		for (String raceKey : values) {
			File file = new File(speciesDirectoryPath(), raceKey+EXT);
			if (file.exists()) {
				initSkillsForEditor(loadOptions(file));
				RaceName raceName = (RaceName) settingMap.get(ROOT + RaceName.KEY);

				String civ = String.join(",", race().civilizationNames());
				civ = raceName.settingValue();
				if (before.contains(civ))
					dual_b.add(civ);
				else
					before.add(civ);
				System.out.println("Load file: " + raceKey + " / Civ = " + civ);

				autoUpdate(e);

				civ = String.join(",", race().civilizationNames());
				civ = raceName.settingValue();
				if (after.contains(civ))
					dual_a.add(civ);
				else
					after.add(civ);
				System.out.println("Save file: " + raceKey + " / Civ = " + civ);
				saveRace();
			}
		}
		System.out.println();
		System.out.println(dual_b.size() + " Multiple Entry in Original: " + dual_b.toString());
		System.out.println(dual_a.size() + " Multiple Entry in Updated: " + dual_a.toString());
		return true;
	}
	// -#-
	// #==================== RaceList ====================
	//
	
	private boolean isFilled(String value)	{ return value != null && !value.isEmpty() && !value.startsWith("_"); }
	private File[] loadListing()	{
		File speciesDir = new File(speciesDirectoryPath());
		List<File> speciesList = new ArrayList<>();
		scanSubDir(speciesList, speciesDir);
		return speciesList.toArray(new File[0]);
	}
	private void scanSubDir(List<File> speciesList, File speciesDir)	{
		if (speciesDir == null || !speciesDir.exists() || !speciesDir.isDirectory())
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
	final class CivilizationRecord {
		final String skillsKey;
		final String fileKey;
		final String prefAnimKey;
		final String civName;
		final String speciesName;
		final String leaderName;
		final String homeWorld;
		final boolean fullCivName;
		private final boolean fullAnim;
		private final boolean availableAI;
		final boolean isCustom;
		final DynOptions speciesOptions;
		final int civIndex;
		int useCount = 0;
		private CivilizationRecord(DynOptions speciesOptions,
				int civIndex,
				String skillsKey,
				String fileKey,
				String prefAnimKey,
				String civName,
				String speciesName,
				String leaderName,
				String homeWorld,
				boolean fullCivName,
				boolean fullAnim,
				boolean availableAI,
				boolean isCustom)	{
			this.speciesOptions	= speciesOptions;
			this.skillsKey		= skillsKey;
			this.fileKey		= fileKey;
			this.prefAnimKey	= prefAnimKey;
			this.civName		= civName;
			this.speciesName	= speciesName;
			this.leaderName		= leaderName;
			this.homeWorld		= homeWorld;
			this.fullCivName	= fullCivName;
			this.fullAnim		= fullAnim;
			this.availableAI	= availableAI;
			this.civIndex		= civIndex;
			this.isCustom		= isCustom;
		}
		void markAsUsed()	{ useCount++; }
		boolean hasPreferedAnim()	{
			return prefAnimKey!=null && prefAnimKey!=AnimationRaceKey.DEFAULT_VALUE && !prefAnimKey.isEmpty();
		}
		boolean isFullAnim()	{
			if (!fullAnim)
				return false;
			if (Species.usedCivilizationNames().contains(civName))
				return false;
			if (Species.usedLeaderNames().contains(leaderName))
				return false;
			return !Species.usedHomeNames().contains(homeWorld);
		}
		@Override public String toString()	{
			String s = skillsKey + " " + civName + " useCount: " + useCount;
			return s;
		}
		private int useCount(boolean onlyIfAvailableAI, boolean onlyfullAnim)	{
			if (availableAI || !onlyIfAvailableAI)
				if (onlyfullAnim && !isFullAnim())
						return 1;
				else
					return useCount;
			else
				return IGameOptions.MAX_OPPONENTS;
		}
	}
	private final class SpeciesRecord {
		private final String skillsKey;
		private final String fileKey;
		private final String prefAnimKey;
		private final StringList namedCiv;
		private final StringList namedLeader;
		private final StringList namedHome;
		private final StringList fullCivName;
		private final StringList fullLeader;
		private final StringList fullHome;
		private final StringList fullAnim;
		private final boolean availableAI;
		private final boolean isCustom;
		private final DynOptions speciesOptions;
		private SpeciesRecord(File file)	{
			speciesOptions	 = loadOptions(file);
			SkillsFactory sf = new SkillsFactory(null, speciesOptions);
			skillsKey  = sf.raceKey.settingValue();
			fileKey = RaceKey.fileToKey(file);
			if (sf.animSkills == null)
				prefAnimKey = AnimationRaceKey.DEFAULT_VALUE;
			else
				prefAnimKey = sf.animSkills.id;
			String dir = selectedLanguageDir();
			AllSpeciesAttributes settings = sf.new AllSpeciesAttributes();

			namedCiv	= settings.getCivilizationsNames();
			namedLeader	= settings.getLeadersNames(dir);
			namedHome	= settings.getHomeWorldNames(dir);
			fullCivName	= settings.getFullCivNames(dir, namedCiv);
			fullLeader	= new StringList();
			fullHome	= new StringList();
			fullAnim	= new StringList();
			for (int i=0; i<namedCiv.size(); i++) {
				if (isFilled(namedLeader.get(i)))
					fullLeader.add(namedCiv.get(i));
				if (isFilled(namedHome.get(i)))
					fullHome.add(namedCiv.get(i));
			}
			fullAnim.addAll(fullCivName);
			fullAnim.retainAll(fullLeader);
			fullAnim.retainAll(fullHome);
			availableAI = sf.race().availableAI();
			isCustom	= true;
		}
		private SpeciesRecord(String key, Race race)	{
			speciesOptions	= race.speciesOptions();
			skillsKey	= key;
			fileKey		= key;
			prefAnimKey	= AnimationRaceKey.DEFAULT_VALUE;
			namedCiv	= race.civilizationNames();
			namedLeader	= race.leaderNames();
			namedHome	= race.homeSystemNames();
			fullCivName	= namedCiv;
			fullLeader	= namedCiv;
			fullHome	= namedCiv;
			fullAnim	= namedCiv;
			availableAI = race.availableAI();
			isCustom	= false;
		}
		private CivRecordList getList()	{
			CivRecordList list = new CivRecordList();
			for (int i=0; i<namedCiv.size(); i++) {
				String civName = namedCiv.get(i);
				list.add(new CivilizationRecord (speciesOptions, i,
						skillsKey, fileKey, prefAnimKey,
						civName, namedCiv.get(0), namedLeader.get(i), namedHome.get(i),
						fullCivName.contains(civName), fullAnim.contains(civName),
						availableAI, isCustom));
			}
//			if (list.isEmpty()) {
//				System.out.println("Empty getList() for " + fileKey); // TO DO BR: REMOVE
//			}
			return list;
		}
	}
	final class AnimationListMap extends HashMap<String, CivRecordList> implements Base {
		private static final long serialVersionUID = 1L;
		public AnimationListMap()	{
			for (String raceKey : IGameOptions.allRaceKeyList)
				put(raceKey, new CivRecordList());
			put(AnimationRaceKey.DEFAULT_VALUE, new CivRecordList());
		}
		AnimationListMap loadCustomSpecies()	{
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) {
					if (file == null)
						continue;
					SpeciesRecord speciesRec = new SpeciesRecord(file);
					CivRecordList civRecList = get(speciesRec.prefAnimKey);
					civRecList.addAll(speciesRec.getList());
				}
			return this;
		}
		AnimationListMap loadInternalSpecies()	{
			for (Entry<String, Race> entry : Species.internalMap()) {
				SpeciesRecord speciesRec = new SpeciesRecord(entry.getKey(), entry.getValue());
				CivRecordList civRecList = get(speciesRec.fileKey);
				civRecList.addAll(speciesRec.getList());
			}
			return this;
		}
		public CivRecordList getAllCustomCiv()	{ return getFullAnim(getAllCiv()); }
		public AnimationListMap getFullAnimMap()	{
			AnimationListMap map = new AnimationListMap();
			for (Entry<String, CivRecordList> entry : entrySet()) {
				map.put(entry.getKey(), getFullAnimCiv(entry.getKey()));
			}
			return map;
		}
		public CivRecordList getAllCiv()	{
			CivRecordList list = new CivRecordList();
			for (CivRecordList civs : values())
				list.addAll(civs);
			return list;
		}
//		public CivRecordList getSelectedCiv(StringList keys)	{
//			CivRecordList list = new CivRecordList();
//			for ( String key : keys)
//				list.addAll(get(key));
//			return list;
//		}
		AnimationListMap getSelectedMap(StringList keys)	{
			AnimationListMap map = new AnimationListMap();
			for ( String key : keys)
				map.put(key, get(key));
			return map;
		}
		private CivRecordList getFullAnimCiv(String key)	{ return getFullAnim(get(key)); }
		private CivRecordList getFullAnim(CivRecordList src)	{
			CivRecordList list = new CivRecordList();
			for (CivilizationRecord civ : src)
				if (civ.isFullAnim())
					list.add(civ);
			return list;
		}
		CivRecordList getWithPrefAnim(CivRecordList src)	{
			CivRecordList list = new CivRecordList();
			for (CivilizationRecord civ : src)
				if (civ.hasPreferedAnim())
					list.add(civ);
			return list;
		}
		CivRecordList nextRandom(boolean onlyIfAvailableAI, boolean forAnim)	{
			int minUse = IGameOptions.MAX_OPPONENTS;
			if (isEmpty())
				return null;
			for (CivRecordList civList : values())
				minUse = min(minUse, civList.totalUseCount(onlyIfAvailableAI, forAnim));

			List<CivRecordList> list = new ArrayList<>();
			for (CivRecordList civList : values())
				if (civList.totalUseCount(onlyIfAvailableAI, forAnim) == minUse)
					list.add(civList);
			return random(list);
		}
	}
	final class CivRecordList extends ArrayList<CivilizationRecord> implements Base {
		private static final long serialVersionUID = 1L;
		CivRecordList getFileKey(String fileKey)	{
			CivRecordList fileKeyList =  new CivRecordList();
			for (CivilizationRecord civRec : this)
				if (civRec.fileKey.equals(fileKey))
					fileKeyList.add(civRec);
			return fileKeyList;
		}
		CivRecordList getKey(String key)	{
			CivRecordList fileKeyList =  new CivRecordList();
			for (CivilizationRecord civRec : this)
				if (civRec.fileKey.equals(key) || civRec.skillsKey.equals(key))
					fileKeyList.add(civRec);
			return fileKeyList;
		}
		CivilizationRecord nextRandom(boolean onlyIfAvailableAI, boolean forAnim)	{
			int minUse = Integer.MAX_VALUE;
			if (isEmpty())
				return null;
			for (CivilizationRecord civRec : this)
				minUse = min(minUse, civRec.useCount(onlyIfAvailableAI, forAnim));

			CivRecordList list = new CivRecordList();
			for (CivilizationRecord civRec : this)
				if (civRec.useCount(onlyIfAvailableAI, forAnim) == minUse)
					list.add(civRec);
			// Do not update use here, it may still be rejected
			return random(list);
		}
		private int totalUseCount(boolean onlyIfAvailableAI, boolean forAnim)	{
			if (isEmpty())
				return Integer.MAX_VALUE;
			int usecount = 0;
			for ( CivilizationRecord civRec : this)
				if (civRec.availableAI || !onlyIfAvailableAI)
					usecount += civRec.useCount(onlyIfAvailableAI, forAnim);
				else
					return IGameOptions.MAX_OPPONENTS;
			if (forAnim && usecount == size())
				return IGameOptions.MAX_OPPONENTS;
			return usecount;
		}
	}
	public final class RaceList extends SettingBase<String> {
		private boolean newValue = false;
		private boolean reload	 = false;
		private HashMap<String, StringList> animationMap = new HashMap<>();

		public RaceList()	{
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
		public void reload(boolean foldersRedesign)	{
			String currentValue = settingValue();
			clearLists();
			animationMap.clear();
			clearOptionsText();
			for (String raceKey : IGameOptions.allRaceKeyList)
				animationMap.put(raceKey, new StringList());
			animationMap.put(AnimationRaceKey.DEFAULT_VALUE, new StringList());

			// Add Current race
			add((DynOptions) IGameOptions.playerCustomRace.get());
			defaultIndex(0);
			// Add existing files
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) {
					if (file == null)
						continue;
					DynOptions opt = loadOptions(file);
					if (opt.size() == 0)
						System.err.println("Empty race file: " + file.getName());
					else {
						// Check to Update filename
						String skillKey = RaceKey.valid(opt, file);
						// Test for reworked old Ways
						String animKey  = AnimationRaceKey.validRedesign(opt, file, foldersRedesign);
						animationMap.get(animKey).add(skillKey); // The map has a "None" key
						add(opt);
					}
				}
			// Add Game races
			for (String raceKey : IGameOptions.allRaceKeyList)
				add(raceKey);

			initOptionsText();
			reload = true;
			set(currentValue);
		}
//		HashMap<String, StringList> animationMap()		{ return animationMap; }
		private void add(DynOptions opt)	{
			SkillsFactory cr = SkillsFactory.getFactoryForRaceList(opt);
			SpeciesSkills dr = cr.race();
			String cfgValue	 = dr.setupName;
			String langLabel = dr.id;
			String tooltipKey = dr.getDescription3();
			float cost = cr.getTotalCost();
			put(cfgValue, langLabel, cost, langLabel, tooltipKey);
		}
		private void add(String raceKey)	{
			Species species	  = new Species(raceKey);	    	
			String cfgValue	  = species.skillKey();
			String langLabel  = BASE_RACE_MARKER + species.setupName();
			String tooltipKey = species.getDescription(3);
			SkillsFactory cr  = getFactoryForRaceList(species);
			float cost = cr.getTotalCost();
			put(cfgValue, langLabel, cost, langLabel, tooltipKey);
		}
		public boolean newValue()	{
			if (newValue) {
				newValue = false;
				return true;
			}
			return false;
		}
		public StringList getAllowedAlienRaces()	{
			StringList list = new StringList();
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) {
					SkillsFactory cr = SkillsFactory.getFactoryForRaceList(loadOptions(file));
					if (cr.availableAI.settingValue())
						list.add(cr.raceKey.settingValue());
				}
			list.removeNullAndEmpty();
			return list;
		}
		public StringList getAllAlienRaces()	{
			StringList list = new StringList();
			File[] fileList = loadListing();
			if (fileList != null)
				for (File file : fileList) {
					SkillsFactory cr = SkillsFactory.getFactoryForRaceList(loadOptions(file));
					list.add(cr.raceKey.settingValue());
				}
			list.removeNullAndEmpty();
			return list;
		}
		// ---------- Overriders ----------
		//
		@Override public String guideValue()					{ return guiOptionLabel(); }
		@Override public String guiOptionValue(int index)		{ return guiOptionLabel(); }
		@Override protected void selectedValue(String value)	{
			super.selectedValue(value);
			if (reload) { // No need to load options on reload
				reload = false;
				return;
			}
			if (index() == 0) {
				initSkillsForEditor((DynOptions) IGameOptions.playerCustomRace.get());
				newValue = true;
				return;
			}
			if (index()>=listSize()-16) { // Base Race
				isReference(true);
				String key = Species.languageToKey(value.substring(1));
				race(Species.getAnim(key).copy(true));

				// Create missing language setting attributes
				String languageKey = ROOT + LanguageList.KEY;
				// current languages
				LanguageList languageSetting = (LanguageList) settingMap.get(languageKey);
				StringList languages = new StringList(languageSetting.settingValue());
				// src options languages
				String srcLanguages = selectedLanguageDir();
				StringList srcLanguageList = new StringList(srcLanguages);
				// Create missing
				for (String lg : srcLanguageList) {
					if (!languages.contains(lg))
						languages.add(lg);
				}
				for (String lg : languages) {
					new SpeciesAttributes(lg);
				}
				// update language setting
				languageSetting.set(languages.asString());

				DynOptions destOptions = race().speciesOptions();
				List<ICRSettings> settings = settingMap.getAll();
				for (ICRSettings setting : settings) {
					setting.skillToSetting(race());
					setting.updateOption(destOptions);
				}

				for (ICRSettings setting : settingMap.getSettings())
					setting.updateGui();
				isReference(false);
				return;
			}
			File file = new File(speciesDirectoryPath(), settingValue() + EXT);
			if (file.exists()) {
				initSkillsForEditor(loadOptions(file));
				newValue = true;
				return;
			}
		}
	}
	// -#-
}
