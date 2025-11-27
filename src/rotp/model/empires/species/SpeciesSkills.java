/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General License, Version 3 (the "License");
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

import java.awt.Image;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import rotp.model.empires.Empire;
import rotp.model.empires.Leader.Objective;
import rotp.model.empires.Leader.Personality;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.DynOptions;
import rotp.model.game.IMainOptions;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipDesign;
import rotp.ui.util.StringList;
import rotp.util.Base;
import rotp.util.LabelManager;
import rotp.util.LanguageManager;

class SpeciesSkills implements Base, Serializable {
	private static final long serialVersionUID = 1L;
	private static final String INTRO_FILE_EXTENSION = ".intro.txt";

	String id;
	String setupName; // BR: was never used
	private String empireTitle; // BR: for custom Species
	private String speciesPrefix = ""; 
	private String speciesSuffix = "";
	private String leaderPrefix = "";
	private String leaderSuffix = "";
	private String worldsPrefix = "";
	private String worldsSuffix = "";
	private String langKey;
	private String description1, description2, description3, description4; // modnar: add desc4
	private String directoryName;
	private final SpeciesUniqueIdentifiers uniqueNames = new SpeciesUniqueIdentifiers();
	private final StringList systemNames = new StringList();

	private final StringList shipNamesSmall	 = new StringList();
	private final StringList shipNamesMedium = new StringList();
	private final StringList shipNamesLarge	 = new StringList();
	private final StringList shipNamesHuge	 = new StringList();

	private float defaultRaceRelations = 0;
	private final HashMap<String, Integer> raceRelations = new HashMap<>();
	private LabelManager labels;

	// BR: Settings that where encoded in "HomeworldKey"
	private float bCBonus = 0f;
	private float hPFactor = 1f;
	private float maintenanceFactor	= 1f;
	private float shipSpaceFactor	= 1f;
	private String planetArtifacts	= "None";
	private String planetRessource	= "Normal";
	private String planetEnvironment	= "Normal";
	// Custom Races:
	private boolean isCustomSpecies		= false;
	private boolean isCopy				= false; // Security prevent modification of original 
	private boolean isAnimAutonomous	= false; // Security prevent modification of original 
	private DynOptions speciesOptions	= null;
	// \BR:
	private int startingYear;
	private int speciesType;
	private String homeworldStarType;
	private String homeworldPlanetType;
	private int homeworldSize;
	private String preferredShipSet;
	private int preferredShipSize	= 2;
	private int shipAttackBonus		= 0;
	private int shipDefenseBonus	= 0;
	private int shipInitiativeBonus	= 0;
	private int groundAttackBonus	= 0;
	private boolean telepathic	= false;
	private float spyCostMod	= 1;
	private float internalSecurityAdj	= 0;
	private float spyInfiltrationAdj	= 0;
	private float workerProductivityMod	= 1;
	private int robotControlsAdj	= 0;
	private float techDiscoveryPct	= 0.5f;
	private float researchBonusPct	= 1.0f;
	private float growthRateMod		= 1;
	private float tradePctBonus		= 0;
	private float positiveDPMod		= 1;
	private int diplomacyBonus		= 0;
	private float councilBonus		= 0;
	private float[] techMod			= new float[] { 1, 1, 1, 1, 1, 1 };
	private float[] discoveryMod	= new float[] { 0, 0, 0, 0, 0, 0 }; // BR:
	private boolean ignoresPlanetEnvironment = false;
	private String acceptedPlanetEnvironment = "No";
	private boolean ignoresFactoryRefit = false;
	private boolean availablePlayer	= true;	// BR: never used!
	private boolean availableAI		= true;	// BR: Never used!
	private boolean masksDiplomacy	= false;
	private float labFlagX			= 0;
	private int spyFactoryFrames	= 0;
	private int spyMissileFrames	= 0;
	private int spyRebellionFrames	= 0;
	private String title;
	private String fullTitle;
	private int homeworldKey, introTextX;
	private String reworkableSpeciesKey, languageList;
	private int reworkableId = -1; // -1 if none was used while editing
	private int preferredAI;

	private static final int PERSONALITY_COUNT	= Personality.values().length;
	private static final int OBJECTIVE_COUNT	= Objective.values().length;
	private static final int DESIGN_MODS_COUNT	= 28;
	private float[] personalityPct	= new float[PERSONALITY_COUNT];
	private float[] objectivePct	= new float[OBJECTIVE_COUNT];
	private float[] shipDesignMods	= new float[DESIGN_MODS_COUNT];

	String defaultHomeworldName()   {
		if (homeSystemNames().isEmpty())
			return "Empty";
		return homeSystemNames().get(0);
	}
	String homeworldPlanetType()		{ return homeworldPlanetType; }
	void homeworldPlanetType(String s)	{ homeworldPlanetType = s; }

	String id()							{ return id; }
	void id(String s)					{ id = s; }
	int introTextX()					{ return introTextX; }
	void introTextX(int i)				{ introTextX = i; }
	int preferredAI()					{ return preferredAI; }
	void preferredAI(int i)				{ preferredAI = i; }
	int speciesType()					{ return speciesType; }
	void speciesType(int i)				{ speciesType = i; }

	String languageList()				{ return languageList; }
	void languageList(String s)			{ languageList = s; }
	String reworkableSpeciesKey()		{ return reworkableSpeciesKey; }
	void reworkableSpeciesKey(String s)	{ reworkableSpeciesKey = s; }
	int reworkableId()					{ return reworkableId; }
	void reworkableId(int i)			{ reworkableId = i; }
	int startingYear()					{ return startingYear; }
	void startingYear(int i)			{ startingYear = i; }
	void empireTitle(String s)			{ empireTitle = s; }
	String speciesPrefix()				{ return speciesPrefix; }
	void speciesPrefix(String s)		{ speciesPrefix = s; }
	String speciesSuffix()				{ return speciesSuffix; }
	void speciesSuffix(String s)		{ speciesSuffix = s; }
	String leaderPrefix()				{ return leaderPrefix; }
	void leaderPrefix(String s)			{ leaderPrefix = s; }
	String leaderSuffix()				{ return leaderSuffix; }
	void leaderSuffix(String s)			{ leaderSuffix = s; }
	String worldsPrefix()				{ return worldsPrefix; }
	void worldsPrefix(String s)			{ worldsPrefix = s; }
	String worldsSuffix()				{ return worldsSuffix; }
	void worldsSuffix(String s)			{ worldsSuffix = s; }
	String preferredShipSet()			{ return preferredShipSet; }
	void preferredShipSet(String s)		{ preferredShipSet = s; }

	String homeworldStarType()			{ return homeworldStarType; }
	void homeworldStarType(String s)	{ homeworldStarType = s; }
	int homeworldSize()					{ return homeworldSize; }
	void homeworldSize(int i)			{ homeworldSize = i; }
	String directoryName()				{ return directoryName; }
	String langKey()					{ return langKey; }
	void langKey(String s)				{ langKey = s; }
	List<String> systemNames()			{ return systemNames; }
	void systemNames(List<String> s)	{ systemNames.resetFrom(s); }
	StringList speciesNames()			{ return uniqueNames.speciesNames; }
	StringList homeSystemNames()		{ return uniqueNames.homeSystemNames; }
	StringList leaderNames()			{ return uniqueNames.leaderNames; }
	StringList shipNamesSmall()			{ return shipNamesSmall; }
	StringList shipNamesMedium()		{ return shipNamesMedium; }
	StringList shipNamesLarge()			{ return shipNamesLarge; }
	StringList shipNamesHuge()			{ return shipNamesHuge; }
	private StringList remainingSpeciesNames()	 { return uniqueNames.remainingSpeciesNames; }
	private StringList remainingHomeworldNames() { return uniqueNames.remainingHomeworldNames; }
	private StringList remainingLeaderNames()	 { return uniqueNames.remainingLeaderNames; }

	SpeciesSkills ()	{
		leaderNames().add("Leader");
		for (int i=0; i<PERSONALITY_COUNT; i++)
			personalityPct(i, 1);
		for (int i=0; i<OBJECTIVE_COUNT; i++)
			objectivePct(i, 1);
	}
	SpeciesSkills(String dirPath)	{
		directoryName = dirPath;
		labels = new LabelManager();
	}
	// TODO BR: For species customization to be completed
	String empireTitle() {
		String s = "[this_empire]";
		String key = "this";
		List<String> tokens = varTokens(s, key);
		String s1 = s;
		for (String token: tokens) {
			String replString = concat("[",key, token,"]");
			List<String> values = substrings(text(token), ',');
			s1 = s1.replace(replString, values.get(0));
		}
		if (s.equalsIgnoreCase(s1))
			return this.empireTitle();
		return s1;
	}
	protected boolean isCopy()			{ return isCopy; }
	protected void isCopy( boolean is)	{ isCopy = is; }
	// BR: for species customization
	// Get a Copy the current species
	protected SpeciesSkills copy()		{
		if (speciesOptions() == null)
			return copy(new DynOptions());
		else
			return copy(speciesOptions().copy());
	}
	protected SpeciesSkills copy(DynOptions srcOptions) {
		SpeciesSkills copy	= RaceFactory.current().reloadRaceDataFile(directoryName);
		labels.copy(labels, copy.labels);
		copy.setupName		= setupName();
		copy.empireTitle	= empireTitle();
		copy.description1	= description1;
		copy.description2	= description2;
		copy.description3	= description3;
		copy.description4	= description4;
		copy.title			= title;
		copy.fullTitle		= fullTitle;
		copy.shipNamesSmall.addAll(shipNamesSmall);
		copy.shipNamesMedium.addAll(shipNamesMedium);
	 	copy.shipNamesLarge.addAll(shipNamesLarge);
		copy.shipNamesHuge.addAll(shipNamesHuge);
		copy.uniqueNames.speciesNames.addAll(uniqueNames.speciesNames);
		copy.uniqueNames.homeSystemNames.addAll(uniqueNames.homeSystemNames);
		copy.uniqueNames.leaderNames.addAll(uniqueNames.leaderNames);
		copy.speciesOptions(srcOptions); // TODO BR: COMMENT MAYBE
		copy.isCopy = true;
		return copy;
	}
	void loadNameList()		{
		StringList secondaryNames = new StringList(speciesNames());
		uniqueNames.remainingSpeciesNames = new StringList();
		remainingSpeciesNames().add(secondaryNames.remove(0));
		remainingSpeciesNames().addAll(secondaryNames);
	}
	void loadLeaderList()	{
		StringList secondaryNames = new StringList(leaderNames());
		uniqueNames.remainingLeaderNames = new StringList();
		remainingLeaderNames().add(secondaryNames.remove(0));
		Collections.shuffle(secondaryNames);
		remainingLeaderNames().addAll(secondaryNames);
	}
	void loadHomeworldList()	{
		StringList homeNames = new StringList(homeSystemNames());
		uniqueNames.remainingHomeworldNames = new StringList();
		remainingHomeworldNames().add(homeNames.remove(0));
		Collections.shuffle(homeNames);
		remainingHomeworldNames().addAll(homeNames);
	}
	String nextAvailableName()	{
		if (remainingSpeciesNames()==null) 
			loadNameList();
		String name = remainingSpeciesNames().remove(0);
		return name;
	}
	int nameIndex(String n)			{ return speciesNames().indexOf(n); }
	String nameVariant(int i)		{
		StringList names = speciesNames();
		return i<names.size()? names.get(i): names.get(0);
	}
	String nextAvailableLeader()	{
		if (remainingLeaderNames()==null)
			loadLeaderList();
		return remainingLeaderNames().remove(0);
	}
	String nextAvailableHomeworld()	{
		if (remainingHomeworldNames()==null)
			loadHomeworldList();
		return remainingHomeworldNames().remove(0);
	}
	LabelManager raceLabels()			{ return labels; }
	@Override public String toString()	{ return concat("Skills:", id); }
	@Override public String text(String key)	{
		if (raceLabels().hasLabel(key))
			return raceLabels().label(key);

		String altKey = LanguageManager.swapToken(key);
		if (altKey != null && raceLabels().hasLabel(altKey))
			return raceLabels().label(altKey);

		return labels().label(key);
	}
	@Override public String text(String key, String... vals)	{
		String str = text(key);
		for (int i=0;i<vals.length;i++)
			str = str.replace(textSubs[i], vals[i]);
		return str;
	}

	List<String> customIntroduction()	{
		List<String> introLines = new ArrayList<>();
		if (isCustomSpecies) {
			log("loading Custom Species Intro");
			Path path = Paths.get(IMainOptions.speciesDirectoryPath(), id + INTRO_FILE_EXTENSION);
			BufferedReader in = directReader(path.toString());
			if (in != null) {
				try {
					String input;
					while ((input = in.readLine()) != null) {
						if (!isComment(input)) {
							introLines.add(input);
						}
					}
				}
				catch (IOException e) {}
				finally {
					try {
						in.close();
					} catch (IOException e) {}
				}
			}
		}
		if (!introLines.isEmpty())
			return introLines;

		// return race-specific dialogue if present
		// else return default dialog
		if (raceLabels().hasIntroduction())
			return raceLabels().introduction();
		return null;
	}
	List<String> introduction()	{
		// return race-specific dialogue if present
		// else return default dialog
		if (raceLabels().hasIntroduction())
			return raceLabels().introduction();
		return labels().introduction();
	}
	private List<String> varTokens(String s)	{ // BR: for debug
		String startKey = "[";
		int keySize = startKey.length();
		List<String> tokens = new ArrayList<>();
		int prevIndex = -1;
		int nextIndex = s.indexOf(startKey, prevIndex);
		while (nextIndex >= 0) {
		int endIndex = s.indexOf(']', nextIndex);
		if (endIndex <= nextIndex)
			return tokens;
		String var = s.substring(nextIndex+keySize, endIndex);
		tokens.add(var);
		prevIndex = nextIndex;
		nextIndex = s.indexOf(startKey, endIndex);
		}
		return tokens;
	}
	boolean validateDialogueTokens()	{ // BR: for debug
		boolean valid = true;
		for (Entry<String, List<String>> entry : raceLabels().dialogueMapEntrySet()) {
			List<String> val = entry.getValue();
			if (val == null || val.isEmpty()) {
				valid = false;
				String key = entry.getKey();
				System.err.println("Keyword with empty text: " + key + " / " + id);
			}
			else {
				for (String txt : val) {
					List<String> tokens = varTokens(txt);
					if (!tokens.isEmpty()) {
						for (String token : tokens) {
							String src = token;
							token = token.replace("your_", "_");
							token = token.replace("my_", "_");
							token = token.replace("other_", "_");
							token = token.replace("alien_", "_");
							token = token.replace("player_", "_");
							token = token.replace("spy_", "_");
							token = token.replace("leader_", "_");
							token = token.replace("defender_", "_");
							token = token.replace("attacker_", "_");
							token = token.replace("voter_", "_");
							token = token.replace("candidate_", "_");
							token = token.replace("victim_", "_");
							token = token.replace("rebel_", "_");
							token = token.replace("rival_", "_");

							switch (token) {
								case "_name":
								case "_home":
								case "system":
								case "amt":
								case "year":
								case "tech":
								case "techGiven":
								case "techReceived":
								case "framed":
								case "spiesCaught":
								case "forced":
								case "target":
									break;
								default:
									if (!raceLabels().hasLabel(token)) {
										if (!labels().hasLabel(token)) {
											valid = false;
											System.err.println("Missing token: " + token + " / " + id + " / " + src);
										}
									}
							}
						}
					}
				}
			}
		}
		return valid;
	}
	String dialogue(String key)	{
		// return race-specific dialogue if present
		// else return default dialog
		if (raceLabels().hasDialogue(key))
			return raceLabels().dialogue(key);
		return labels().dialogue(key);
	}
	String name()						{ return text(id); }
	void setDescription1(String desc)	{ description1 = desc; }
	void setDescription2(String desc)	{ description2 = desc; }
	void setDescription3(String desc)	{ description3 = desc; }
	void setDescription4(String desc)	{ description4 = desc; }
	void setDescription(int i, String desc)	{
		switch (i) {
			case 1:	description1 = desc; return;
			case 2:	description2 = desc; return;
			case 3:	description3 = desc; return;
			case 4:	description4 = desc; return;
		}
	}
	String getDescription(int i)		{
		switch (i) {
			case 1:	return getDescription1();
			case 2:	return getDescription2();
			case 3:	return getDescription3();
			case 4:	return getDescription4();
		}
		return getDescription1();
	}
	String getDescription1()			{ return description1; }
	String getDescription2()			{ return description2; }
	String getDescription3()			{
		String name = isCustomSpecies()? setupName : setupName();
		String desc = description3.replace("[empire]", empireTitle());
		return desc.replace("[race]", name);
	}
	String getDescription4()			{ return description4; }
	String setupName()			{
		if (speciesNames().isEmpty())
			return "";
		return text(substrings(speciesNames().get(0), '|').get(0));
	}
	int shipAttackBonus()				{ return shipAttackBonus; }
	void shipAttackBonus(int i)			{ shipAttackBonus = i; }
	int shipDefenseBonus()				{ return shipDefenseBonus; }
	void shipDefenseBonus(int i)		{ shipDefenseBonus = i; }
	int shipInitiativeBonus()			{ return shipInitiativeBonus; }
	void shipInitiativeBonus(int i)		{ shipInitiativeBonus = i; }
	int groundAttackBonus()				{ return groundAttackBonus; }
	void groundAttackBonus(int i)		{ groundAttackBonus = i; }
	float spyCostMod()					{ return spyCostMod; }
	void spyCostMod(float f)			{ spyCostMod = f; }
	float internalSecurityAdj()			{ return internalSecurityAdj; }
	void internalSecurityAdj(float f)	{ internalSecurityAdj = f; }
	float spyInfiltrationAdj()			{ return spyInfiltrationAdj; }
	void spyInfiltrationAdj(float f)	{ spyInfiltrationAdj = f; }
	float workerProductivityMod()		{ return workerProductivityMod; }
	void workerProductivityMod(float f)	{ workerProductivityMod = f; }
	int robotControlsAdj()				{ return robotControlsAdj; }
	void robotControlsAdj(int i)		{ robotControlsAdj = i; }
	float techDiscoveryPct()			{ return techDiscoveryPct; }
	void techDiscoveryPct(float f)		{ techDiscoveryPct = f; }
	float techDiscoveryPct(int i)		{
		return min(1, max(0,
				techDiscoveryPct() + discoveryMod[i]));
	}
	float researchBonusPct()			{ return researchBonusPct; }
	void researchBonusPct(float f)		{ researchBonusPct = f; }
	float researchNoSpyBonusPct()		{
		if (options().forbidTechStealing())
			return 1f + max(0f, spyInfiltrationAdj/2);
		return 1f;
	}
	float growthRateMod()			{ return growthRateMod; }
	void growthRateMod(float f)		{ growthRateMod = f; }
	float tradePctBonus()			{ return tradePctBonus; }
	void tradePctBonus(float f)		{ tradePctBonus = f; }
	float positiveDPMod()			{ return positiveDPMod; }
	void positiveDPMod(float f)		{ positiveDPMod = f; }
	int diplomacyBonus()			{ return diplomacyBonus; }
	void diplomacyBonus(int i)		{ diplomacyBonus = i; }
	float councilBonus()			{ return councilBonus; }
	void councilBonus(float f)		{ councilBonus = f; }
	float techMod(int i)			{ return techMod[i]; }
	void techMod(int i, float f)	{ techMod[i] = f; }

	boolean ignoresPlanetEnvironment()		 { return ignoresPlanetEnvironment; }
	void ignoresPlanetEnvironment(boolean b) { ignoresPlanetEnvironment = b; }
	String acceptedPlanetEnvironment()		 { return acceptedPlanetEnvironment; }
	void acceptedPlanetEnvironment(String s) { acceptedPlanetEnvironment = s; }

	float[] personalityPct()			{ return personalityPct; }
	void personalityPct(float[] f)		{ personalityPct = f; }
	private float personalityPct(int i)	{ return personalityPct[i]; }
	void personalityPct(int i, float f)	{ personalityPct[i] = f; }
	float[] objectivePct()				{ return objectivePct; }
	void objectivePct(float[] f)		{ objectivePct = f; }
	private float objectivePct(int i)	{ return objectivePct[i]; }
	void objectivePct(int i, float f)	{ objectivePct[i] = f; }
	float discoveryMod(int i)			{ return discoveryMod[i]; }
	void discoveryMod(int i, float f)	{ discoveryMod[i] = f; }
	float[] shipDesignMods()			{ return shipDesignMods; }
	float shipDesignMods(int i)			{ return shipDesignMods[i]; }
	void shipDesignMods(int i, float f)	{ shipDesignMods[i] = f; }
	int shipDesignModsSize()			{ return DESIGN_MODS_COUNT; }
	boolean availablePlayer()			{ return availablePlayer; }
	void availablePlayer(boolean b)		{ availablePlayer = b; }
	boolean availableAI()				{ return availableAI; }
	void availableAI(boolean b)			{ availableAI = b; }
	boolean ignoresFactoryRefit()		{ return ignoresFactoryRefit; }
	void ignoresFactoryRefit(boolean b)	{ ignoresFactoryRefit = b; }
	boolean telepathic()				{ return telepathic; }
	void telepathic(boolean b)			{ telepathic = b; }
	boolean masksDiplomacy()			{ return masksDiplomacy; }
	void masksDiplomacy(boolean b)		{ masksDiplomacy = b; }
	int homeworldKey()					{ return homeworldKey; }
	void homeworldKey(int i)			{ homeworldKey = i; }
	String title()						{ return title; }
	void title(String s)				{ title = s; }
	String fullTitle()					{ return fullTitle; }
	void fullTitle(String s)			{ fullTitle = s; }
	// BR: Custom Species
	boolean isCustomSpecies()			{ return isCustomSpecies; }
	SpeciesSkills isCustomSpecies(boolean is)	{ isCustomSpecies = is; return this; }
	boolean isAnimAutonomous()			{ return isAnimAutonomous; }
	void isAnimAutonomous(boolean is)	{ isAnimAutonomous = is; }
	boolean isRandomized()				{ return SkillsFactory.CR_EMPIRE_NAME_RANDOM.equalsIgnoreCase(empireTitle); }
	DynOptions speciesOptions()			{ return speciesOptions; }
	void speciesOptions(DynOptions val)	{ speciesOptions = val; }
	// BR: Get the values encoded in HomeworldKey
	float bCBonus()						{ return bCBonus; }
	void  bCBonus(float val)			{ bCBonus = val; }
	float hPFactor()					{ return hPFactor;  }
	void  hPFactor(float val)			{ hPFactor = val; }
	float maintenanceFactor()			{ return maintenanceFactor; }
	void  maintenanceFactor(float val)	{ maintenanceFactor = val; }
	float shipSpaceFactor()				{ return shipSpaceFactor; }
	void  shipSpaceFactor(float val)	{ shipSpaceFactor = val; }
	String planetArtifacts()			{
		if (planetRessource.equalsIgnoreCase("Artifact")) { // for backward compatibility
			planetRessource = "Normal";
			planetArtifacts = "Artifact";
		}
		return planetArtifacts;
	}
	void   planetArtifacts(String s)	{
		planetArtifacts = s;
		if (planetRessource.equalsIgnoreCase("Artifact")) { // for backward compatibility
			planetRessource = "Normal";
			planetArtifacts = "Artifact";
		}
	}
	String planetRessource()			{
		if (planetRessource.equalsIgnoreCase("Artifact")) { // for backward compatibility
			planetRessource = "Normal";
			planetArtifacts = "Artifact";
		}
		return planetRessource;
	}
	void   planetRessource(String s)	{
		planetRessource = s;
		if (s.equalsIgnoreCase("Artifact")) { // for backward compatibility
			planetArtifacts = s;
			planetRessource = "Normal";
		}
	}
	String planetEnvironment()			{ return planetEnvironment; }
	void   planetEnvironment(String s)	{ planetEnvironment = s; }

	boolean raceWithUltraPoorHomeworld()	{ return planetRessource.equalsIgnoreCase("UltraPoor"); }
	boolean raceWithPoorHomeworld()			{ return planetRessource.equalsIgnoreCase("Poor"); }
	boolean raceWithRichHomeworld()			{ return planetRessource.equalsIgnoreCase("Rich"); }
	boolean raceWithUltraRichHomeworld()	{ return planetRessource.equalsIgnoreCase("UltraRich"); }
	boolean raceWithOrionLikeHomeworld()	{ return planetArtifacts.equalsIgnoreCase("OrionLike"); }
	boolean raceWithArtifactsHomeworld()	{
		return planetArtifacts.equalsIgnoreCase("Artifacts")
				|| planetRessource.equalsIgnoreCase("Artifacts"); // for backward compatibility
	}
	boolean raceWithHostileHomeworld()	{ return planetEnvironment.equalsIgnoreCase("Hostile"); }
	boolean raceWithFertileHomeworld()	{  return planetEnvironment.equalsIgnoreCase("Fertile"); }
	boolean raceWithGaiaHomeworld()		{ return planetEnvironment.equalsIgnoreCase("Gaia"); }

	float defaultRaceRelations()		{ return defaultRaceRelations; }
	void defaultRaceRelations(int d)	{ defaultRaceRelations = d; }
	float baseRelations(Species s)		{
		String id = s.skillKey();
		float definedRelations = raceRelations.containsKey(id) ? raceRelations.get(id) : defaultRaceRelations();
		return definedRelations + options().baseAIRelationsAdj();
	}
	void baseRelations(String key, int d)	{ raceRelations.put(key, d); }
	float labFlagX()					{ return labFlagX; }
	void labFlagX(float d)				{ labFlagX = d; }
	int spyFactoryFrames()				{ return spyFactoryFrames; }
	void spyFactoryFrames(int d)		{ spyFactoryFrames = d; }
	int spyMissileFrames()				{ return spyMissileFrames; }
	void spyMissileFrames(int d)		{ spyMissileFrames = d; }
	int spyRebellionFrames()			{ return spyRebellionFrames; }
	void spyRebellionFrames(int d)		{ spyRebellionFrames = d; }

	List<Image> sabotageFactoryFrames()	{
		List<Image> images = new ArrayList<>();
		for (int i=1;i<=spyFactoryFrames;i++) {
			String fileName = directoryName+"/SabotageFactories/Frame"+String.format("%03d.jpg", i);
			Image img = icon(fileName).getImage();
			images.add(img);
		}
		return images;
	}
	List<Image> sabotageMissileFrames()	{
		List<Image> images = new ArrayList<>();
		for (int i=1;i<=spyMissileFrames;i++) {
			String fileName = directoryName+"/SabotageMissiles/Frame"+String.format("%03d.jpg", i);
			Image img = icon(fileName).getImage();
			images.add(img);
		}
		return images;
	}
	List<Image> sabotageRebellionFrames()	{
		List<Image> images = new ArrayList<>();
		for (int i=1;i<=spyRebellionFrames;i++) {
			String fileName = directoryName+"/SabotageRebellion/Frame"+String.format("%03d.jpg", i);
			Image img = icon(fileName).getImage();
			images.add(img);
		}
		return images;
	}
	boolean isHostile(PlanetType pt)	{ return ignoresPlanetEnvironment() ? false : pt.hostileToTerrans(); }
	int preferredShipSize()				{ return preferredShipSize; }
	void preferredShipSize(int i)		{ preferredShipSize = i; }
	int randomShipSize()				{
		float r = random();
		if (r <= .5)
			return preferredShipSize;
		if (r <= .75)
			return Math.min(preferredShipSize+1, ShipDesign.MAX_SIZE);

		return max(preferredShipSize-1, 0);
	}
	int randomLeaderAttitude()	{
		float r = random();
		float modAccum = 0;
		for (int i=0;i<PERSONALITY_COUNT;i++) {
			modAccum += personalityPct(i);
			if (r < modAccum)
				return i;
		};
		return 0;
	}
	int mostCommonLeaderAttitude()	{
		float maxPct = 0;
		int maxAttitude = 0;
		for (int i=0;i<PERSONALITY_COUNT;i++) {
			if (personalityPct(i) > maxPct) {
				maxPct = personalityPct(i);
				maxAttitude = i;
			}
		};
		return maxAttitude;
	}
	int randomLeaderObjective()	{
		float r = random();
		float modAccum = 0;
		for (int i=0;i<OBJECTIVE_COUNT;i++) {
			modAccum += objectivePct(i);
			if (r < modAccum)
				return i;
		};
		return 0;
	}
	String randomSystemName(Empire emp)	{
		// this is only called when a new system is scouted
		// the name is stored on the empire's system view for this system
		// and transferred to the system when it is colonized
		List<String> allPossibleNames = masterNameList(emp);
		for (StarSystem sys : galaxy().starSystems()) {
			String name = sys.name().trim(); // custom species may add confusing spaces
			allPossibleNames.remove(name);
		}
		// Multiple and Custom species may share the same list... We have to looks thru all systems view
		// looking at the galaxy().starSystems() is not good enough. Named only when colonized.
		int n = galaxy().numStarSystems();
		for (Empire e : galaxy().empires())
			if (!e.extinct())
				for (int i=0;i<n;i++)
					if (e.sv.isScouted(i))
						allPossibleNames.remove(emp.sv.name(i));
		String systemName = allPossibleNames.isEmpty() ? galaxy().nextSystemName(id) : allPossibleNames.get(0);
		log("Naming system:", systemName);
		return systemName;
	}
	StringList masterSystemList(Species emp)	{ // to extend Homeworld list
		Collections.shuffle(systemNames);
		// BR: add custom species prefix and suffix
		StringList complexNames = new StringList();
		String prefix = emp.worldsPrefix();
		String suffix = emp.worldsSuffix();
		for (String s: systemNames)
			complexNames.add(prefix + s + suffix);
		return complexNames;
	}
	private List<String> masterNameList(Species emp)	{
		List<String> complexNames = masterSystemList(emp);
		List<String> names = new ArrayList<>(complexNames);
		for (String s: complexNames)
			names.add(text("COLONY_NAME_2", s));
		for (String s: complexNames)
			names.add(text("COLONY_NAME_3", s));
		for (String s: complexNames)
			names.add(text("COLONY_NAME_4", s));
		for (String s: complexNames)
			names.add(text("COLONY_NAME_5", s));
		return names;
	}
	String randomLeaderName()			{ return random(leaderNames()); }
	List<String> shipNames(int size)	{
		switch(size) {
			case ShipDesign.SMALL:	return shipNamesSmall();
			case ShipDesign.MEDIUM:	return shipNamesMedium();
			case ShipDesign.LARGE:	return shipNamesLarge();
			case ShipDesign.HUGE:	return shipNamesHuge();
		}
		return null;
	}
	void parseSpeciesNames(String names)	{ speciesNames().resetFrom(substrings(names, ',')); }
	void parseHomeWorlds(String names)		{ homeSystemNames().resetFrom(substrings(names, ',')); }
	void parseLeaderNames(String names)		{ leaderNames().resetFrom(substrings(names, ',')); }
	void parseShipNamesSmall(String names)	{ shipNamesSmall().resetFrom(substrings(names, ',')); }
	void parseShipNamesMedium(String names)	{ shipNamesMedium().resetFrom(substrings(names, ',')); }
	void parseShipNamesLarge(String names)	{ shipNamesLarge().resetFrom(substrings(names, ',')); }
	void parseShipNamesHuge(String names)	{ shipNamesHuge().resetFrom(substrings(names, ',')); }
	void parseDialogLabel(String label, String names)	{ labels().addLabel(label, names); }
	
	private class SpeciesUniqueIdentifiers	{
		private static final StringList enLabels = new StringList("_empire,_race,_race_plural,_title,_nameTitle", ",");
		private static final StringList frLabels = new StringList("_empireof,_raceadjec,_raceadjecF"
				+ ",_race_pluralnoun,_race_pluralnounof,_race_pluralnounto,_race_pluraladjec,_race_pluraladjecF"
				+ ",_title,_nameTitle", ",");
		private final StringList speciesNames	 = new StringList();
		private final StringList homeSystemNames = new StringList();
		private final StringList leaderNames	 = new StringList();
		private StringList remainingSpeciesNames;
		private StringList remainingHomeworldNames;
		private StringList remainingLeaderNames;
	}
}
