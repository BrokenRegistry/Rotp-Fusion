/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.model.empires;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import rotp.model.ai.interfaces.ShipTemplate;
import rotp.model.empires.Leader.Objective;
import rotp.model.empires.Leader.Personality;
import rotp.model.galaxy.StarType;
import rotp.model.game.DynOptions;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipDesign;
import rotp.model.tech.TechCategory;
import rotp.util.Base;
import rotp.util.LabelManager;
import rotp.util.LanguageManager;

public class SpeciesRoot implements ISpecies, Base {
	private static final int PERSONALITY_COUNT	= Personality.values().length;
	private static final int OBJECTIVE_COUNT	= Objective.values().length;
	private static final int DESIGN_MODS_COUNT	= 28;
	static final String ECO_LIMITED				= "Limited";
	static final String ECO_ALL					= "All";
	static final String ECO_NONE				= "No";
	static final String PLANET_RESOURCE_U_POOR	= "UltraPoor";
	static final String PLANET_RESOURCE_POOR	= "Poor";
	static final String PLANET_RESOURCE_NORMAL	= "Normal";
	static final String PLANET_RESOURCE_RICH	= "Rich";
	static final String PLANET_RESOURCE_U_RICH	= "UltraRich";
	static final String PLANET_ARTIFACT_NONE	= "None";
	static final String PLANET_ARTIFACT_ANTARAN	= "Artifacts";
	static final String PLANET_ARTIFACT_ORION	= "OrionLike";
	static final String PLANET_ENV_HOSTILE		= "Hostile";
	static final String PLANET_ENV_NORMAL		= "Normal";
	static final String PLANET_ENV_FERTILE		= "Fertile";
	static final String PLANET_ENV_GAIA			= "Gaia";

	private String id;
	public String setupName; // BR: was never used
	private String empireTitle; // BR: for custom Races
	private String racePrefix = ""; // BR: for custom Races
	private String raceSuffix = ""; // BR: for custom Races
	private String leaderPrefix = ""; // BR: for custom Races
	private String leaderSuffix = ""; // BR: for custom Races
	private String worldsPrefix = ""; // BR: for custom Races
	private String worldsSuffix = ""; // BR: for custom Races
	private String langKey;
	private String description1, description2, description3, description4; // modnar: add desc4
	private String directoryName;

	private String laboratoryKey, embassyKey, councilKey;
	private String holographKey;
	private String diplomatKey;
	private String scientistKey;
	private String soldierKey;
	private String spyFaceKey;
	private String leaderKey;
	private String soldierFaceKey;
	private String mugshotKey;
	private String wideMugshotKey;
	private String setupImageKey;
	private String advisorFaceKey;
	private String advisorScoutKey;
	private String advisorTransportKey;
	private String advisorDiplomacyKey;
	private String advisorShipKey;
	private String advisorRallyKey;
	private String advisorMissileKey;
	private String advisorWeaponKey;
	private String advisorCouncilKey;
	private String advisorRebellionKey;
	private String advisorResistCouncilKey;
	private String advisorCouncilResistedKey;
	private String diplomacyTheme;
	private String spyKey;
	private String gnnKey;
	private String gnnHostKey;
	private String gnnColor;
	private Color gnnTextColor;
	private String transportKey;
	private String transportDescKey;
	private String transportOpenKey;
	private int transportDescFrames, transportOpenFrames;
	private String shipAudioKey;
	private RaceCombatAnimation troopNormal;
	private RaceCombatAnimation troopHostile;
	private RaceCombatAnimation troopDeath1;
	private RaceCombatAnimation troopDeath2;
	private RaceCombatAnimation troopDeath3;
	private RaceCombatAnimation troopDeath4;
	private RaceCombatAnimation troopDeath1H;
	private RaceCombatAnimation troopDeath2H;
	private RaceCombatAnimation troopDeath3H;
	private RaceCombatAnimation troopDeath4H;
	private List<String> fortressKeys = new ArrayList<>();
	private String shieldKey;
	private String voiceKey;
	private String ambienceKey;
	private String flagWarKey, flagNormKey, flagPactKey;
	private String dlgWarKey, dlgNormKey,dlgPactKey;
	private String winSplashKey, lossSplashKey;
	private Color winTextC, lossTextC;
	private List<String> soundKeys;

	private SpeciesLabels speciesLabels = new SpeciesLabels();
	private float defaultRaceRelations = 0;
	private HashMap<String, Integer> raceRelations = new HashMap<>();
	private LabelManager labels;

	// BR: Settings that where encoded in "HomeworldKey"
	private float bCBonus = 0f;
	private float hPFactor = 1f;
	private float maintenanceFactor = 1f;
	private float shipSpaceFactor = 1f;
	private String planetArtifacts = PLANET_ARTIFACT_NONE;
	private String planetRessource = PLANET_RESOURCE_NORMAL;
	private String planetEnvironment = PLANET_ENV_NORMAL;
	// Custom Races:
	private boolean isCustomRace = false;
	private DynOptions raceOptions	= null;
	// \BR:
	private int startingYear;
	private int speciesType;
	private String homeworldStarType; // for player
	private String homeworldStarTypeAI = StarType.RANDOM;
	private String homeworldPlanetType = StarType.RANDOM;
	private int homeworldSize;
	private String preferredShipSet;
	private int preferredShipSize = 2;
	private int shipAttackBonus = 0;
	private int shipDefenseBonus = 0;
	private int shipInitiativeBonus = 0;
	private int groundAttackBonus = 0;
	private boolean telepathic = false;
	private float spyCostMod = 1;
	private float internalSecurityAdj = 0;
	private float spyInfiltrationAdj = 0;
	private float workerProductivityMod = 1;
	private int robotControlsAdj = 0;
	private float techDiscoveryPct = 0.5f;
	private float researchBonusPct = 1.0f;
	private float growthRateMod = 1;
	private float tradePctBonus = 0;
	private float positiveDPMod = 1;
	private int diplomacyBonus = 0;
	private float councilBonus = 0;
	private float[] techMod = new float[] { 1, 1, 1, 1, 1, 1 };
	private float[] discoveryMod = new float[] { 0, 0, 0, 0, 0, 0 }; // BR:
	private boolean ignoresPlanetEnvironment = false;
	private String acceptedPlanetEnvironment = ECO_NONE;
	private boolean ignoresFactoryRefit = false;
	private boolean availablePlayer = true;
	private boolean availableAI = true;
	private boolean masksDiplomacy = false;
	private String title;
	private String fullTitle;
	private int homeworldKey;

	private float[] personalityPct	= new float[PERSONALITY_COUNT];
	private float[] objectivePct	= new float[OBJECTIVE_COUNT];
	private float[] shipDesignMods	= new float[DESIGN_MODS_COUNT];

	private float labFlagX = 0;
	private int espionageX, espionageY;
	private int spyFactoryFrames = 0;
	private int spyMissileFrames = 0;
	private int spyRebellionFrames = 0;
	private int introTextX;
	private int transportW, transportYOffset, transportLandingFrames, colonistWalkingFrames;
	private int colonistDelay, colonistX1, colonistX2, colonistY1, colonistY2;
	private int dialogLeftMargin, dialogRightMargin,  dialogTopY;
	private float diploScale, diploOpacity;
	private int diploXOffset, diploYOffset;
	private int flagW, flagH;

	private transient BufferedImage transportClosedImg;
	private transient Image transportImg;
	private transient BufferedImage diploMug, wideDiploMug;


	SpeciesRoot () {
		speciesLabels.leaderNames.add("Leader");
		for (int i=0; i<PERSONALITY_COUNT; i++)
			personalityPct(i, 1);
		for (int i=0; i<OBJECTIVE_COUNT; i++)
			objectivePct(i, 1);
	}

	SpeciesRoot(String dirPath) {
		directoryName = dirPath;
		labels = new LabelManager();
	}

	public String homeworldPlanetType()	{ return homeworldPlanetType; }
	void homeworldPlanetType(String s)	{ homeworldPlanetType = s; }

	String id()							{ return id; }
	void id(String s)					{ id = s; }
	int startingYear()					{ return startingYear; }
	void startingYear(int i)			{ startingYear = i; }
	int speciesType()					{ return speciesType; }
	void speciesType(int i)				{ speciesType = i; }

	void empireTitle(String s)			{ empireTitle = s; }
	String racePrefix()					{ return racePrefix; }
	void racePrefix(String s)			{ racePrefix = s; }
	String raceSuffix()					{ return raceSuffix; }
	void raceSuffix(String s)			{ raceSuffix = s; }
	String leaderPrefix()				{ return leaderPrefix; }
	void leaderPrefix(String s)			{ leaderPrefix = s; }
	String leaderSuffix()				{ return leaderSuffix; }
	void leaderSuffix(String s)			{ leaderSuffix = s; }
	public String worldsPrefix()		{ return worldsPrefix; }
	void worldsPrefix(String s)			{ worldsPrefix = s; }
	public String worldsSuffix()		{ return worldsSuffix; }
	void worldsSuffix(String s)			{ worldsSuffix = s; }
	public String preferredShipSet()	{ return preferredShipSet; }
	void preferredShipSet(String s)		{ preferredShipSet = s; }
	String homeworldStarType()			{ return homeworldStarType; }
	void homeworldStarType(String s)	{ homeworldStarType = s; }
	String homeworldStarTypeAI()		{ return homeworldStarTypeAI; }
	void homeworldStarTypeAI(String s)	{ homeworldStarTypeAI = s; }
	public int homeworldSize()			{ return homeworldSize; }
	void homeworldSize(int i)			{ homeworldSize = i; }

	String directoryName()				{ return directoryName; }
	String lalelsFileName(String s)		{ return isCustomRace()? s : langKey + "." + s; }
	String langKey()					{ return langKey; }
	void langKey(String s)				{ langKey = s; }
//	List<String> systemNames()			{ return speciesLabels.systemNames; }
	void systemNames(List<String> s)	{ speciesLabels.systemNames(s); }
	List<String> raceNames()			{ return speciesLabels.raceNames; }
	List<String> homeSystemNames()		{ return speciesLabels.homeSystemNames; }
	List<String> leaderNames()			{ return speciesLabels.leaderNames; }
	List<String> shipNamesSmall()		{ return speciesLabels.shipNamesSmall; }
	List<String> shipNamesMedium()		{ return speciesLabels.shipNamesMedium; }
	List<String> shipNamesLarge()		{ return speciesLabels.shipNamesLarge; }
	List<String> shipNamesHuge()		{ return speciesLabels.shipNamesHuge; }

	int colonistDelay()					{ return colonistDelay; }
	int colonistStartX()				{ return colonistX1; }
	int colonistStartY()				{ return colonistY1; }
	int colonistStopX()					{ return colonistX2; }
	int colonistStopY()					{ return colonistY2; }
	int dialogLeftMargin()				{ return dialogLeftMargin; }
	void dialogLeftMargin(int i)		{ dialogLeftMargin = i; }
	int dialogRightMargin()				{ return dialogRightMargin; }
	void dialogRightMargin(int i)		{ dialogRightMargin = i; }
	int dialogTopY()					{ return dialogTopY; }
	void dialogTopY(int i)				{ dialogTopY = i; }
	int colonistWalkingFrames()			{ return colonistWalkingFrames; }
	void colonistWalkingFrames(int i)	{ colonistWalkingFrames = i; }
	int transportLandingFrames()		{ return transportLandingFrames; }
	void transportLandingFrames(int i)	{ transportLandingFrames = i; }
	int transportDescFrames()			{ return transportDescFrames; }
	void transportDescFrames(int i)		{ transportDescFrames = i; }
	int transportOpenFrames()			{ return transportOpenFrames; }
	void transportOpenFrames(int i)		{ transportOpenFrames = i; }
	int transportYOffset()				{ return transportYOffset; }
	void transportYOffset(int i)		{ transportYOffset = i; }
	int transportW()					{ return transportW; }
	void transportW(int i)				{ transportW = i; }
	int flagW()							{ return flagW; }
	void flagW(int i)					{ flagW = i; }
	int flagH()							{ return flagH; }
	void flagH(int i)					{ flagH = i; }
	int introTextX()					{ return introTextX; }
	void introTextX(int i)				{ introTextX = i; }
	int diploXOffset()					{ return diploXOffset; }
	void diploXOffset(int i)			{ diploXOffset = i; }
	int diploYOffset()					{ return diploYOffset; }
	void diploYOffset(int i)			{ diploYOffset = i; }

	float diploScale()					{ return diploScale; }
	void diploScale(float f)			{ diploScale = f; }
	float diploOpacity()				{ return diploOpacity; }
	void diploOpacity(float f)			{ diploOpacity = f; }

	Color gnnTextColor()				{ return gnnTextColor; }
	void gnnTextColor(Color c)			{ gnnTextColor = c; }

	String lossSplashKey()				{ return lossSplashKey; }
	void lossSplashKey(String s)		{ lossSplashKey = s; }
	String winSplashKey()				{ return winSplashKey; }
	void winSplashKey(String s)			{ winSplashKey = s; }
	String shipAudioKey()				{ return shipAudioKey; }
	void shipAudioKey(String s)			{ shipAudioKey = s; }
	String ambienceKey()				{ return ambienceKey; }
	void ambienceKey(String s)			{ ambienceKey = s; }
	String transportDescKey()			{ return transportDescKey; }
	void transportDescKey(String s)		{ transportDescKey = s; }
	String transportOpenKey()			{ return transportOpenKey; }
	void transportOpenKey(String s)		{ transportOpenKey = s; }
	String mugshotKey()					{ return mugshotKey; }
	void mugshotKey(String s)			{ mugshotKey = s; }
	String wideMugshotKey()				{ return wideMugshotKey; }
	void wideMugshotKey(String s)		{ wideMugshotKey = s; }
	String setupImageKey()				{ return setupImageKey; }
	void setupImageKey(String s)		{ setupImageKey = s; }
	String spyFaceKey()					{ return spyFaceKey; }
	void spyFaceKey(String s)			{ spyFaceKey = s; }
	String soldierFaceKey()				{ return soldierFaceKey; }
	void soldierFaceKey(String s)		{ soldierFaceKey = s; }
	String advisorFaceKey()				{ return advisorFaceKey; }
	void advisorFaceKey(String s)		{ advisorFaceKey = s; }
	String advisorScoutKey()			{ return advisorScoutKey; }
	void advisorScoutKey(String s)		{ advisorScoutKey = s; }
	String advisorTransportKey()		{ return advisorTransportKey; }
	void advisorTransportKey(String s)	{ advisorTransportKey = s; }
	String advisorDiplomacyKey()		{ return advisorDiplomacyKey; }
	void advisorDiplomacyKey(String s)	{ advisorDiplomacyKey = s; }
	String advisorShipKey()				{ return advisorShipKey; }
	void advisorShipKey(String s)		{ advisorShipKey = s; }
	String advisorRallyKey()			{ return advisorRallyKey; }
	void advisorRallyKey(String s)		{ advisorRallyKey = s; }
	String advisorMissileKey()			{ return advisorMissileKey; }
	void advisorMissileKey(String s)	{ advisorMissileKey = s; }
	String advisorWeaponKey()			{ return advisorWeaponKey; }
	void advisorWeaponKey(String s)		{ advisorWeaponKey = s; }
	String advisorCouncilKey()			{ return advisorCouncilKey; }
	void advisorCouncilKey(String s)	{ advisorCouncilKey = s; }
	String advisorRebellionKey()		{ return advisorRebellionKey; }
	void advisorRebellionKey(String s)	{ advisorRebellionKey = s; }
	String advisorResistCouncilKey()	{ return advisorResistCouncilKey; }
	void advisorResistCouncilKey(String s)	{ advisorResistCouncilKey = s; }
	String advisorCouncilResistedKey()	{ return advisorCouncilResistedKey; }
	void advisorCouncilResistedKey(String s)	{ advisorCouncilResistedKey = s; }
	String councilKey()					{ return councilKey; }
	void councilKey(String s)			{ councilKey = s; }
	String laboratoryKey()				{ return laboratoryKey; }
	void laboratoryKey(String s)		{ laboratoryKey = s; }
	String embassyKey()					{ return embassyKey; }
	void embassyKey(String s)			{ embassyKey = s; }
	String holographKey()				{ return holographKey; }
	void holographKey(String s)			{ holographKey = s; }
	String diplomatKey()				{ return diplomatKey; }
	void diplomatKey(String s)			{ diplomatKey = s; }
	String scientistKey()				{ return scientistKey; }
	void scientistKey(String s)			{ scientistKey = s; }
	String soldierKey()					{ return soldierKey; }
	void soldierKey(String s)			{ soldierKey = s; }
	String spyKey()						{ return spyKey; }
	void spyKey(String s)				{ spyKey = s; }
	String leaderKey()					{ return leaderKey; }
	void leaderKey(String s)			{ leaderKey = s; }
	String gnnKey()						{ return gnnKey; }
	void gnnKey(String s)				{ gnnKey = s; }
	String gnnHostKey()					{ return gnnHostKey; }
	void gnnHostKey(String s)			{ gnnHostKey = s; }
	String gnnColor()					{ return gnnColor; }
	void gnnColor(String s)				{ gnnColor = s; }
	String flagWarKey()					{ return flagWarKey; }
	void flagWarKey(String s)			{ flagWarKey = s; }
	String flagNormKey()				{ return flagNormKey; }
	void flagNormKey(String s)			{ flagNormKey = s; }
	String flagPactKey()				{ return flagPactKey; }
	void flagPactKey(String s)			{ flagPactKey = s; }
	String dlgWarKey()					{ return dlgWarKey; }
	void dlgWarKey(String s)			{ dlgWarKey = s; }
	String dlgNormKey()					{ return dlgNormKey; }
	void dlgNormKey(String s)			{ dlgNormKey = s; }
	String dlgPactKey()					{ return dlgPactKey; }
	void dlgPactKey(String s)			{ dlgPactKey = s; }
	String transportKey()				{ return transportKey; }
	void transportKey(String s)			{ transportKey = s; }
	String shieldKey()					{ return shieldKey; }
	void shieldKey(String s)			{ shieldKey = s; }
	String voiceKey()					{ return voiceKey; }
	void voiceKey(String s)				{ voiceKey = s; }

	RaceCombatAnimation troopNormal()	{ return troopNormal; }
	RaceCombatAnimation troopHostile()	{ return troopHostile; }
	RaceCombatAnimation troopDeath1()	{ return troopDeath1; }
	RaceCombatAnimation troopDeath2()	{ return troopDeath2; }
	RaceCombatAnimation troopDeath3()	{ return troopDeath3; }
	RaceCombatAnimation troopDeath4()	{ return troopDeath4; }
	RaceCombatAnimation troopDeath1H()	{ return troopDeath1H; }
	RaceCombatAnimation troopDeath2H()	{ return troopDeath2H; }
	RaceCombatAnimation troopDeath3H()	{ return troopDeath3H; }
	RaceCombatAnimation troopDeath4H()	{ return troopDeath4H; }

	void troopNormal(RaceCombatAnimation a)		{ troopNormal = a; }
	void troopHostile(RaceCombatAnimation a)	{ troopHostile = a; }
	void troopDeath1(RaceCombatAnimation a)		{ troopDeath1 = a; }
	void troopDeath2(RaceCombatAnimation a)		{ troopDeath2 = a; }
	void troopDeath3(RaceCombatAnimation a)		{ troopDeath3 = a; }
	void troopDeath4(RaceCombatAnimation a)		{ troopDeath4 = a; }
	void troopDeath1H(RaceCombatAnimation a)	{ troopDeath1H = a; }
	void troopDeath2H(RaceCombatAnimation a)	{ troopDeath2H = a; }
	void troopDeath3H(RaceCombatAnimation a)	{ troopDeath3H = a; }
	void troopDeath4H(RaceCombatAnimation a)	{ troopDeath4H = a; }


	// TODO BR: For race customization to be completed
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
	public void loadNameList()		{ speciesLabels.loadNameList(); }
	public void loadLeaderList()	{ speciesLabels.loadLeaderList(); }
	public void loadHomeworldList()	{ speciesLabels.loadHomeworldList(); }

	// =========================================================
	// for Species only
	//
	String nextAvailableName()		{ return speciesLabels.nextAvailableName(); }
	int nameIndex(String n)			{ return speciesLabels.raceNames.indexOf(n); }
	String nameVariant(int i)		{ return speciesLabels.nameVariant(i); }
	String nextAvailableLeader()	{ return speciesLabels.nextAvailableName(); }
	public String nextAvailableHomeworld()	{ return speciesLabels.nextAvailableHomeworld(); }
	// =========================================================

	LabelManager raceLabels()			{ return labels; }
	@Override public String toString()	{ return concat("Race:", id); }

	@Override public String text(String key) {
		if (raceLabels().hasLabel(key))
			return raceLabels().label(key);

		String altKey = LanguageManager.swapToken(key);
		if (altKey != null && raceLabels().hasLabel(altKey))
			return raceLabels().label(altKey);

		return labels().label(key);
	}
	@Override public String text(String key, String... vals) {
		String str = text(key);
		for (int i=0;i<vals.length;i++)
			str = str.replace(textSubs[i], vals[i]);
		return str;
	}

	String name()				{ return text(id); }
	void setDescription(String str, int id)	{
		switch (id) {
		case 1: description1 = str;
		case 2: description2 = str;
		case 3: description3 = str;
		case 4: description4 = str;
		}
	}
	public String getDescription1()		{ return description1; }
	public String getDescription2()		{ return description2; }
	public String getDescription3()		{ return getDescription3(setupName()); }
	public String getDescription3(String name)	{
		 String desc = description3.replace("[empire]", empireTitle());
		return desc.replace("[race]", name);
	}
	public String getDescription4()		{ return description4; }

	public String setupName()			{
		if (speciesLabels.raceNames.isEmpty())
			return "";
		return text(substrings(speciesLabels.raceNames.get(0), '|').get(0));
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
	float techDiscoveryPct(int i)		{ return min(1, max(0, techDiscoveryPct() + discoveryMod[i])); }
	float researchBonusPct()			{ return researchBonusPct; }
	void researchBonusPct(float f)		{ researchBonusPct = f; }
	float researchNoSpyBonusPct()		{
		if (options().forbidTechStealing())
			return 1f + max(0f, spyInfiltrationAdj/2);
		return 1f;
	}
	float growthRateMod()				{ return growthRateMod; }
	void growthRateMod(float f)			{ growthRateMod = f; }
	float tradePctBonus()				{ return tradePctBonus; }
	void tradePctBonus(float f)			{ tradePctBonus = f; }
	float positiveDPMod()				{ return positiveDPMod; }
	void positiveDPMod(float f)			{ positiveDPMod = f; }
	int diplomacyBonus()				{ return diplomacyBonus; }
	void diplomacyBonus(int i)			{ diplomacyBonus = i; }
	float councilBonus()				{ return councilBonus; }
	void councilBonus(float f)			{ councilBonus = f; }
	float techMod(int i)				{ return techMod[i]; }
	int techModPct(int i)				{ return toPct(techMod[i]); }
	void techMod(int i, float f)		{ techMod[i] = f; }
	
	public boolean ignoresPlanetEnvironment()	{ return ignoresPlanetEnvironment; }
	void ignoresPlanetEnvironment(boolean b)	{ ignoresPlanetEnvironment = b; }
	public String acceptedPlanetEnvironment()	{ return acceptedPlanetEnvironment; }
	void acceptedPlanetEnvironment(String s)	{ acceptedPlanetEnvironment = s; }
	public int ecoTolerance()			{
		if (ignoresPlanetEnvironment)
			switch (acceptedPlanetEnvironment()) {
				case "Limited":
					return 2;
				case "All":
				default:
					return 1;
			}
		else
			return 0;
	}
	void ecoTolerance(int i)			{
		switch (i) {
		case 1:
			ignoresPlanetEnvironment(true);
			acceptedPlanetEnvironment(ECO_ALL);
			return;
		case 2:
			ignoresPlanetEnvironment(true);
			acceptedPlanetEnvironment(ECO_LIMITED);
			return;
		case 0:
		default:
			ignoresPlanetEnvironment(false);
			return;
		}
	}

	float[] personalityPct()			{ return personalityPct; }
	void personalityPct(float[] f)		{ personalityPct = f; }
	float personalityPct(int i)			{ return personalityPct[i]; }
	void personalityPct(int i, float f)	{ personalityPct[i] = f; }
	float[] objectivePct()				{ return objectivePct; }
	void objectivePct(float[] f)		{ objectivePct = f; }
	float objectivePct(int i)			{ return objectivePct[i]; }
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
	public int homeworldKey()			{ return homeworldKey; }
	void homeworldKey(int i)			{ homeworldKey = i; }
	String title()						{ return title; }
	void title(String s)				{ title = s; }
	String fullTitle()					{ return fullTitle; }
	void fullTitle(String s)			{ fullTitle = s; }
	// BR: Custom Races
	boolean isCustomRace()				{ return isCustomRace; }
	SpeciesRoot isCustomRace(boolean val)	{ isCustomRace = val; return this;}
	boolean isRandomized()				{ return CR_EMPIRE_NAME_RANDOM.equalsIgnoreCase(empireTitle); }
	DynOptions raceOptions()			{ return raceOptions; }
	void raceOptions(DynOptions val)	{ raceOptions = val; }
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

	public boolean raceWithUltraPoorHomeworld()	{ return planetRessource.equalsIgnoreCase(PLANET_RESOURCE_U_POOR);	}
	public boolean raceWithPoorHomeworld()		{ return planetRessource.equalsIgnoreCase(PLANET_RESOURCE_POOR); }
	public boolean raceWithRichHomeworld()		{ return planetRessource.equalsIgnoreCase(PLANET_RESOURCE_RICH); }
	public boolean raceWithUltraRichHomeworld()	{ return planetRessource.equalsIgnoreCase(PLANET_RESOURCE_U_RICH); }
	public boolean raceWithOrionLikeHomeworld()	{ return planetArtifacts.equalsIgnoreCase(PLANET_ARTIFACT_ORION); }
	public boolean raceWithArtifactsHomeworld()	{
		return planetArtifacts.equalsIgnoreCase(PLANET_ARTIFACT_ANTARAN)
				|| planetRessource.equalsIgnoreCase(PLANET_ARTIFACT_ANTARAN); // for backward compatibility
	}
	public boolean raceWithHostileHomeworld()	{ return planetEnvironment.equalsIgnoreCase(PLANET_ENV_HOSTILE); }
	public boolean raceWithFertileHomeworld()	{ return planetEnvironment.equalsIgnoreCase(PLANET_ENV_FERTILE); }
	public boolean raceWithGaiaHomeworld()		{ return planetEnvironment.equalsIgnoreCase(PLANET_ENV_GAIA); }

	float defaultRaceRelations()			{ return defaultRaceRelations; }
	void defaultRaceRelations(int d)		{ defaultRaceRelations = d; }
	float baseRelations(SpeciesRoot r)	{
		float definedRelations = raceRelations.containsKey(r.id) ? raceRelations.get(r.id) : defaultRaceRelations();
		return definedRelations + options().baseAIRelationsAdj();
	}
	void baseRelations(String key, int d)	{ raceRelations.put(key, d); }
	int preferredShipSize()					{ return preferredShipSize; }
	void preferredShipSize(int i)			{ preferredShipSize = i; }
	int randomShipSize()					{
		float r = random();
		if (r <= .5)
			return preferredShipSize;
		if (r <= .75)
			return Math.min(preferredShipSize+1, ShipDesign.MAX_SIZE);
		return max(preferredShipSize-1, 0);
	}
	int randomLeaderAttitude()				{
		float r = random();
		float modAccum = 0;
		for (int i=0;i<PERSONALITY_COUNT;i++) {
			modAccum += personalityPct(i);
			if (r < modAccum)
				return i;
		};
		return 0;
	}
	int mostCommonLeaderAttitude()			{
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
	int randomLeaderObjective()				{
		float r = random();
		float modAccum = 0;
		for (int i=0;i<OBJECTIVE_COUNT;i++) {
			modAccum += objectivePct(i);
			if (r < modAccum)
				return i;
		};
		return 0;
	}
	
	private void parseDataLine(String input, SpeciesRoot sr, Race r)	{
		if (isComment(input))
			return;
		List<String> vals = substrings(input, ':');
		if (vals.size() < 2)
			return;
		String key = StringUtils.capitalize(vals.get(0).strip().toLowerCase());
		String value = vals.get(1).strip();
		if (value.isEmpty())
			return;

		switch (key) {
		case SPECIES_KEY:			{ sr.parseSpeciesKey(value);		return; }
		case LANG_KEY:				{ sr.parseSpeciesLangKey(value);	return; }
		case STARTING_YEAR:			{ sr.parseStartingYear(value);		return; }
		case AVAILABLE_TO_PLAYER:	{ sr.parseAvailableToPlayer(value);	return; }
		case AVAILABLE_TO_AI:		{ sr.parseAvailableToAI(value);		return; }

		case HOMESTAR_TYPE:
		case HOMESTAR_PLAYER:		{ sr.parseHomeStarType(value);		return; }
		case HOMESTAR_AI:			{ sr.parseHomeStarTypeAI(value);	return; }
		case HOMEWORLD_TYPE:		{ sr.parseHomeWorldType(value);		return; }
		case HOMEWORLD_KEY:			{ sr.parseHomeWorldKey(value);		return; }
		case HOMEWORLD_SIZE:		{ sr.parseHomeWorldSize(value);		return; }
//		case "mugshot":				{ r.mugshotKey(value); return; }
//		case "diploProfile":		{ r.wideMugshotKey(value); return; }
//		case "setupImage":			{ r.setupImageKey(value); return; }
//		case "spyMug":				{ r.spyFaceKey(value); return; }
//		case "soldierMug":			{ r.soldierFaceKey(value); return; }
//		case "advisorMug":			{ r.advisorFaceKey(value); return; }
//		case "advisorScout":		{ r.advisorScoutKey(value); return; }
//		case "advisorTransport":	{ r.advisorTransportKey(value); return; }
//		case "advisorDiplomacy":	{ r.advisorDiplomacyKey(value); return; }
//		case "advisorShip":			{ r.advisorShipKey(value); return; }
//		case "advisorRally":		{ r.advisorRallyKey(value); return; }
//		case "advisorMissile":		{ r.advisorMissileKey(value); return; }
//		case "advisorWeapon":		{ r.advisorWeaponKey(value); return; }
//		case "advisorCouncil":		{ r.advisorCouncilKey(value); return; }
//		case "advisorRebellion":	{ r.advisorRebellionKey(value); return; }
//		case "advisorResistCouncil":	{ r.advisorResistCouncilKey(value); return; }
//		case "advisorCouncilResisted":	{ r.advisorCouncilResistedKey(value); return; }
//		case "council": 		{ r.councilKey(value); return; }
//		case "lab":			{ r.laboratoryKey(value); return; }
//		case "embassy":		{ r.embassyKey(value); return; }
//		case "holograph":		{ r.holographKey(value); return; }
//		case "diplomat":		{ r.diplomatKey(value); return; }
//		case "scientist":		{ r.scientistKey(value); return; }
//		case "trooper":		{ r.soldierKey(value); return; }
//		case "spy":			{ r.spyKey(value); return; }
//		case "leader":			{ r.leaderKey(value); return; }
//		case "diploTheme":		{ r.diplomacyTheme(value); return; }
//		case "gnn":			{ r.gnnKey(value); return; }
//		case "gnnHost":		{ r.gnnHostKey(value); return; }
//		case "gnnColor":		{ r.gnnColor(value); return; }
//		case "gnnTextColor":	{ r.gnnTextColor(parseColor(value:; return; }
//		case "diplomatXform":	{ r.diplomacyTransformer(PixelShifter.createFrom(value:; }
//		case "winSplash":		{ r.parseWinSplash(value); return; }
//		case "lossSplash":		{ r.parseLossSplash(value); return; }
//		case "flagSize":		{ r.flagSize(value); return; }
//		case "flagWar":		{ r.flagWarKey(value); return; }
//		case "flagNormal":		{ r.flagNormKey(value); return; }
//		case "flagPact":		{ r.flagPactKey(value); return; }
//		case "dialogWar":		{ r.dlgWarKey(value); return; }
//		case "dialogNormal":	{ r.dlgNormKey(value); return; }
//		case "dialogPact":		{ r.dlgPactKey(value); return; }
//		case "troopIcon":		{ r.troopNormal().iconSpec(value); return; }
//		case "troopFireXY":	{ r.troopNormal().fireXY(value); return; }
//		case "troopScale":		{
//			r.troopNormal().scaling(value);
//			r.troopDeath1().scaling(value);
//			r.troopDeath2().scaling(value);
//			r.troopDeath3().scaling(value);
//			r.troopDeath4().scaling(value);
//			r.troopHostile().scaling(value);
//			r.troopDeath1H().scaling(value);
//			r.troopDeath2H().scaling(value);
//			r.troopDeath3H().scaling(value);
//			r.troopDeath4H().scaling(value);
//			return;
//		}
//		case "troopHIcon":		{ r.troopHostile().iconSpec(value); return; }
//		case "troopHFireXY":	{ r.troopHostile().fireXY(value); return; }
//		case "troopDeath1":	{ r.troopDeath1().iconSpec(value); return; }
//		case "troopDeath2":	{ r.troopDeath2().iconSpec(value); return; }
//		case "troopDeath3":	{ r.troopDeath3().iconSpec(value); return; }
//		case "troopDeath4":	{ r.troopDeath4().iconSpec(value); return; }
//		case "troopDeath1H":	{ r.troopDeath1H().iconSpec(value); return; }
//		case "troopDeath2H":	{ r.troopDeath2H().iconSpec(value); return; }
//		case "troopDeath3H":	{ r.troopDeath3H().iconSpec(value); return; }
//		case "troopDeath4H":	{ r.troopDeath4H().iconSpec(value); return; }
//		case "landingAudio":	{ r.shipAudioKey(value); return; }
//		case "transport":		{ r.transportKey(value); return; }
//		case "transportDesc":	{ r.parseTransportDesc(value); return; }
//		case "transportOpen":	{ r.parseTransportOpen(value); return; }
//		case "transportW":		{ r.transportW(parseInt(value)); return; }
//		case "transportYOff":	{ r.transportYOffset(parseInt(value)); return; }
//		case "transportLandingFrames":	{ r.transportLandingFrames(parseInt(value)); return; }
//		case "colonistWalk":	{ r.colonistWalk(value); return; }
//		case "labFlagX":		{ r.labFlagX(parseFloat(value)); return; }
//		case "spyFactories":	{ r.spyFactoryFrames(parseInt(value)); return; }
//		case "spyMissiles":	{ r.spyMissileFrames(parseInt(value)); return; }
//		case "spyRebellion":	{ r.spyRebellionFrames(parseInt(value)); return; }
//		case "espionageXY":	{ r.espionageXY(substrings(value, '@')); return; }
//		case "dialogTextX":	{ parseDialogTextMargins(r, substrings(value, ',')); return; }
//		case "dialogTextY":	{ r.dialogTopY(parseInt(value)); return; }
//		case "fortress":		{ r.parseFortress(value); return; }
//		case "shield":			{ r.shieldKey(value); return; }
//		case "introTextX":		{ r.introTextX(parseInt(value)); return; }
//		case "councilDiplo":	{ r.parseCouncilDiplomatLocation(value); return; }
//		case "homeworld":		{ r.homeworldKey(parseInt(value)); return; }
//		case "voice":			{ r.voiceKey(value); return; }
//		case "ambience":		{ r.ambienceKey(value); return; }

		case SPECIES:			{ sr.parseSpeciesMods(value);		return; }
		case SPECIES_TYPE:		{ sr.parseSpeciesType(value);		return; }
		case SPECIES_ECO:		{ sr.parseSpeciesIgnoreEco(value);	return; }

		case PERSONALITY:		{ sr.parseLeaderPersonalityMods(value);	return; }
		case ERRATIC:			{ sr.parseLeaderPersonality(value, Personality.ERRATIC);	return; }
		case PACIFIST:			{ sr.parseLeaderPersonality(value, Personality.PACIFIST);	return; }
		case HONORABLE:			{ sr.parseLeaderPersonality(value, Personality.HONORABLE);	return; }
		case RUTHLESS:			{ sr.parseLeaderPersonality(value, Personality.RUTHLESS);	return; }
		case AGGRESSIVE:		{ sr.parseLeaderPersonality(value, Personality.AGGRESSIVE);	return; }
		case XENOPHOBIC:		{ sr.parseLeaderPersonality(value, Personality.XENOPHOBIC);	return; }

		case OBJECTIVE:			{ sr.parseLeaderObjectiveMods(value);	return; }
		case MILITARIST:		{ sr.parseLeaderObjective(value, Objective.MILITARIST);		return; }
		case ECOLOGIST:			{ sr.parseLeaderObjective(value, Objective.ECOLOGIST);		return; }
		case DIPLOMAT:			{ sr.parseLeaderObjective(value, Objective.DIPLOMAT);		return; }
		case INDUSTRIALIST:		{ sr.parseLeaderObjective(value, Objective.INDUSTRIALIST);	return; }
		case EXPANSIONIST:		{ sr.parseLeaderObjective(value, Objective.EXPANSIONIST);	return; }
		case TECHNOLOGIST:		{ sr.parseLeaderObjective(value, Objective.TECHNOLOGIST);	return; }

		case PREF_SHIP_MOD:		{ sr.parseSpeciesShipSizeMods(value);	return; }
		case SHIP_SET:			{ sr.parseSpeciesShipSet(value);		return; }
		case SHIP_SIZE:			{ sr.parseSpeciesShipSize(value);		return; }

		case SHIP_MOD:			{ sr.parseSpeciesShipMods(value);		return; }
		case SHIP_ATTACK:		{ sr.parseSpeciesShipAttack(value);		return; }
		case SHIP_DEFENSE:		{ sr.parseSpeciesShipDefense(value);	return; }
		case SHIP_INITIATIVE:	{ sr.parseSpeciesShipInitiative(value);	return; }

		case GROUND_MOD:
		case GROUND_ATTACK_MOD:	{ sr.parseSpeciesGroundMods(value);		return; }

		case SPY_MOD:			{ sr.parseSpeciesSpyMods(value);			return; }
		case SPY_COST:			{ sr.parseSpeciesSpyCost(value);			return; }
		case SECURITY_ADJ:		{ sr.parseSpeciesInternalSecurity(value);	return; }
		case INFILTRATION_ADJ:	{ sr.parseSpeciesSpyInfiltration(value);	return; }
		case TELEPATHIC:		{ sr.parseSpeciesTelepathic(value);			return; }
		case MASK_RELATIONS:	{ sr.parseSpeciesMasksDiplomacy(value);		return; }

		case PROD_MOD:			{ sr.parseSpeciesProdMods(value);			return; }
		case WORKER_PROD:		{ sr.parseSpeciesWorkerProd(value);			return; }
		case FACTORY_CONTROLS:	{ sr.parseSpeciesRobotControl(value);		return; }
		case IGNORE_REFIT_COST:	{ sr.parseSpeciesIgnoreRefitCost(value);	return; }

		case TECH_MOD:			{ sr.parseSpeciesTechMods(value);		return; }
		case TECH_DISCOVERY:	{ sr.parseSpeciesTechDiscovery(value);	return; }
		case RESEARCH_BONUS:	{ sr.parseSpeciesResearchBonus(value);	return; }

		case POPULATION_MOD:
		case POPULATION_GROWTH:	{ sr.parseSpeciesPopMods(value);	return; }

		case DIPLO_MOD:			{ sr.parseSpeciesDiploMods(value);			return; }
		case TRADE_BONUS:		{ sr.parseSpeciesTradeBonusMods(value);		return; }
		case POSITIVE_DP_MOD:	{ sr.parseSpeciesPositiveDPMods(value);		return; }
		case DIPLOMACY_BONUS:	{ sr.parseSpeciesDiplomacyBonusMods(value);	return; }
		case COUNCIL_BONUS:		{ sr.parseSpeciesCouncilBonusMods(value);	return; }

		case RESEARCH:			{ sr.parseSpeciesResearchMods(value);	return; }
		case COMPUTER:			{ sr.parseSpeciesResearch(value, TechCategory.COMPUTER);		return; }
		case CONSTRUCTION:		{ sr.parseSpeciesResearch(value, TechCategory.CONSTRUCTION);	return; }
		case FORCE_FIELD:		{ sr.parseSpeciesResearch(value, TechCategory.FORCE_FIELD);		return; }
		case PLANETOLOGY:		{ sr.parseSpeciesResearch(value, TechCategory.PLANETOLOGY);		return; }
		case PROPULSION:		{ sr.parseSpeciesResearch(value, TechCategory.PROPULSION);		return; }
		case WEAPON:			{ sr.parseSpeciesResearch(value, TechCategory.WEAPON);			return; }

		case RELATIONS:			{ sr.parseSpeciesRelationsMods(value);	return; }

		case SHIP_DESIGN:		{ sr.parseShipDesignMods(value);		return; }
		case COST_MULT_S:		{ sr.parseShipDesign(value, ShipTemplate.COST_MULT_S);			return; }
		case COST_MULT_M:		{ sr.parseShipDesign(value, ShipTemplate.COST_MULT_M);			return; }
		case COST_MULT_L:		{ sr.parseShipDesign(value, ShipTemplate.COST_MULT_L);			return; }
		case COST_MULT_H:		{ sr.parseShipDesign(value, ShipTemplate.COST_MULT_H);			return; }
		case MODULE_SPACE:		{ sr.parseShipDesign(value, ShipTemplate.MODULE_SPACE);			return; }
		case SHIELD_WGHT_FB:	{ sr.parseShipDesign(value, ShipTemplate.SHIELD_WEIGHT_FB);		return; }
		case SHIELD_WGHT_D:		{ sr.parseShipDesign(value, ShipTemplate.SHIELD_WEIGHT_D);		return; }
		case ECM_WGHT_FD:		{ sr.parseShipDesign(value, ShipTemplate.ECM_WEIGHT_FD);		return; }
		case ECM_WGHT_B:		{ sr.parseShipDesign(value, ShipTemplate.ECM_WEIGHT_B);			return; }
		case MANEUVER_WGHT_BD:	{ sr.parseShipDesign(value, ShipTemplate.MANEUVER_WEIGHT_BD);	return; }
		case MANEUVER_WGHT_F:	{ sr.parseShipDesign(value, ShipTemplate.MANEUVER_WEIGHT_F);	return; }
		case ARMOR_WGHT_FB:		{ sr.parseShipDesign(value, ShipTemplate.ARMOR_WEIGHT_FB);		return; }
		case ARMOR_WGHT_D:		{ sr.parseShipDesign(value, ShipTemplate.ARMOR_WEIGHT_D);		return; }
		case SPECIALS_WGHT:		{ sr.parseShipDesign(value, ShipTemplate.SPECIALS_WEIGHT);		return; }
		case SPEED_MATCHING:	{ sr.parseShipDesign(value, ShipTemplate.SPEED_MATCHING);		return; }
		case REINFORCED_ARMOR:	{ sr.parseShipDesign(value, ShipTemplate.REINFORCED_ARMOR);		return; }
		case BIO_WEAPONS:		{ sr.parseShipDesign(value, ShipTemplate.BIO_WEAPONS);			return; }
		case PREF_PULSARS:		{ sr.parseShipDesign(value, ShipTemplate.PREF_PULSARS);			return; }
		case PREF_CLOAK:		{ sr.parseShipDesign(value, ShipTemplate.PREF_CLOAK);			return; }
		case PREF_REPAIR:		{ sr.parseShipDesign(value, ShipTemplate.PREF_REPAIR);			return; }
		case PREF_INERTIAL:		{ sr.parseShipDesign(value, ShipTemplate.PREF_INERTIAL);		return; }
		case PREF_MISS_SHIELD:	{ sr.parseShipDesign(value, ShipTemplate.PREF_MISS_SHIELD);		return; }
		case PREF_REPULSOR:		{ sr.parseShipDesign(value, ShipTemplate.PREF_REPULSOR);		return; }
		case PREF_STASIS:		{ sr.parseShipDesign(value, ShipTemplate.PREF_STASIS);			return; }
		case PREF_STREAM_PROJ:	{ sr.parseShipDesign(value, ShipTemplate.PREF_STREAM_PROJECTOR); return; }
		case PREF_WARP_DISSIP:	{ sr.parseShipDesign(value, ShipTemplate.PREF_WARP_DISSIPATOR);	return; }
		case PREF_TECH_NULLIF:	{ sr.parseShipDesign(value, ShipTemplate.PREF_TECH_NULLIFIER);	return; }
		case PREF_BEAM_FOCUS:	{ sr.parseShipDesign(value, ShipTemplate.PREF_BEAM_FOCUS);		return; }
		default:
			err("unknown key->", input);
		}
	}

	// ========================================================================
	// SPECIES KEYS AND AVAILABILITY
	//
	static final String SPECIES_KEY		= "Key";
	static final String LANG_KEY		= "LangKey";
	static final String STARTING_YEAR	= "Year";
	static final String AVAILABLE_TO_PLAYER	= "Available to player";
	static final String AVAILABLE_TO_AI		= "Available to ai";

	private void parseSpeciesKey(String val)		{ id(val); }
	private void parseSpeciesLangKey(String val)	{ langKey(val); }
	private void parseStartingYear(String val)		{ startingYear(parseInt(val)); }
	private void parseAvailableToPlayer(String val)	{ availablePlayer(parseBoolean(val)); }
	private void parseAvailableToAI(String val)		{ availableAI(parseBoolean(val)); }

	private List<String> listSpeciesKeysMods()	{
		List<String> strList = new ArrayList<>();
		strList.add(labelLine(SPECIES_KEY,			id()));
		strList.add(labelLine(LANG_KEY,				langKey()));
		strList.add(labelLine(STARTING_YEAR,		startingYear()));
		strList.add(labelLine(AVAILABLE_TO_PLAYER,	availablePlayer()));
		strList.add(labelLine(AVAILABLE_TO_AI,		availableAI()));
		return strList;
	}

	// ========================================================================
	// SPECIES HOME WORLD
	//
	static final String HOMESTAR_TYPE	= "Homestartype";
	static final String HOMESTAR_PLAYER	= "Home star type player";
	static final String HOMESTAR_AI		= "Home star type ai";
	static final String HOMEWORLD_TYPE	= "Home world type";
	static final String HOMEWORLD_SIZE	= "Home world size";
	static final String HOMEWORLD_KEY	= "Home world key";
	private void parseHomeStarType(String value)	{
		value = StarType.validType(value);
		homeworldStarType(value);
	}
	private void parseHomeStarTypeAI(String value)	{
		value = StarType.validType(value);
		homeworldStarTypeAI(value);
	}
	private void parseHomeWorldType(String value)	{ homeworldPlanetType(value); }
	private void parseHomeWorldSize(String value)	{ homeworldSize(parseInt(value)); }
	private void parseHomeWorldKey(String value)	{
		int key = parseInt(value);
		homeworldKey(key);
		switch (key) {
		case 888:
			hPFactor(0.666f);
			shipSpaceFactor(1.4f);
			planetRessource(PLANET_RESOURCE_RICH);
			return;
		case 1337:
			planetArtifacts(PLANET_ARTIFACT_ANTARAN);
			planetEnvironment(PLANET_ENV_FERTILE);
			return;
		case 8888:
			planetRessource(PLANET_RESOURCE_U_RICH);
			return;
		case 10101:
			bCBonus(0.25f);
			maintenanceFactor(0.5f);
			return;
		}
	}

	private List<String> listSpeciesHomeMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// Species home info");
		strList.add("// Species star types - " + join(StarType.empireStarTypeList()));
		strList.add(labelLine(HOMESTAR_PLAYER,	homeworldStarType()));
		strList.add(labelLine(HOMESTAR_AI,		homeworldStarTypeAI()));
		strList.add("// Species planet types - " + join(PlanetType.empirePlanetTypeList()));
		strList.add(labelLine(HOMEWORLD_TYPE,	homeworldPlanetType()));
		strList.add(labelLine(HOMEWORLD_SIZE,	homeworldSize()));
		strList.add(labelLine(HOMEWORLD_KEY,	homeworldKey()));
		return strList;
	}

	// ========================================================================
	// SPECIES ECOLOGY INFO
	//
	static final String SPECIES			= "Species";
	static final String SPECIES_TYPE	= "Species type";
	static final String SPECIES_ECO		= "Species ecology";

	private void parseSpeciesMods(String input)	{
		List<String> vals = substrings(input, ',');
		if (vals.size() < 2)
			err("Race ", name(), " is missing some species vars");
		
		// field #1 is species type (carbon, silicate, robotic) -
		speciesType(parseInt(vals.get(0)));
		// deprecated - will be overwritten by ignoreEco param
		ignoresPlanetEnvironment(parseInt(vals.get(1)) == 1);
		if (parseInt(vals.get(1)) == 1)
			acceptedPlanetEnvironment(ECO_ALL);
		else
			acceptedPlanetEnvironment(ECO_NONE);
	}
	private void parseSpeciesType(String val)		{ speciesType(parseInt(val)); }
	private void parseSpeciesIgnoreEco(String val)	{ ecoTolerance(parseInt(val)); }

	private List<String> listSpeciesTypeMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// Species environment info");
		strList.add("// Species type - terran(1), aquatic(2), silicate(3) or robotic(4)");
		strList.add("// Species ecology - eco sensitive(0), ignore eco(1), limited tolerance(2)");
		strList.add(labelLine(SPECIES_TYPE,	speciesType()));
		strList.add(labelLine(SPECIES_ECO,	ecoTolerance()));
		return strList;
	}

	// ========================================================================
	// LEADERS PERSONALITY MODIFIERS
	//
	static final String PERSONALITY	= "Personality";
	static final String ERRATIC		= "Erratic %";
	static final String PACIFIST	= "Pacifist %";
	static final String HONORABLE	= "Honorable %";
	static final String RUTHLESS	= "Ruthless %";
	static final String AGGRESSIVE	= "Aggressive %";
	static final String XENOPHOBIC	= "Xenophobic %";

	private void parseLeaderPersonalityMods(String input)	{
		List<String> vals = substrings(input, ',');
		for (int i=0; i<vals.size(); i++)
			personalityPct(i, parsePct(vals.get(i)));
	}
	private void parseLeaderPersonality(String input, Personality pers)	{
		int idx = pers.ordinal();
		float value = parsePct(input);
		personalityPct(idx, value);
	}

	private List<String> listLeaderPersonalityMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// Species Leader Personality Probability %");
		strList.add(labelLine(ERRATIC,		objectivePct(Personality.ERRATIC.ordinal())));
		strList.add(labelLine(PACIFIST,		objectivePct(Personality.PACIFIST.ordinal())));
		strList.add(labelLine(HONORABLE,	objectivePct(Personality.HONORABLE.ordinal())));
		strList.add(labelLine(RUTHLESS, 	objectivePct(Personality.RUTHLESS.ordinal())));
		strList.add(labelLine(AGGRESSIVE,	objectivePct(Personality.AGGRESSIVE.ordinal())));
		strList.add(labelLine(XENOPHOBIC,	objectivePct(Personality.XENOPHOBIC.ordinal())));
		return strList;
	}

	// ========================================================================
	// LEADERS OBJECTIVE MODIFIERS
	//
	static final String OBJECTIVE		= "Objective";
	static final String MILITARIST		= "Militarist %";
	static final String ECOLOGIST		= "Ecologist %";
	static final String DIPLOMAT		= "Diplomat %";
	static final String INDUSTRIALIST	= "Industrialist %";
	static final String EXPANSIONIST	= "Expansionist %";
	static final String TECHNOLOGIST	= "Technologist %";

	private void parseLeaderObjectiveMods(String input)	{
		List<String> vals = substrings(input, ',');
		for (int i=0; i<vals.size(); i++)
			objectivePct(i, parsePct(vals.get(i)));
	}
	private void parseLeaderObjective(String input, Objective obj )	{
		int idx = obj.ordinal();
		float value = parsePct(input);
		objectivePct(idx, value);
	}

	private List<String> listLeaderObjectiveMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// Species Leader Objectives Probability %");
		strList.add(labelLine(MILITARIST,	 objectivePct(Objective.MILITARIST.ordinal())));
		strList.add(labelLine(ECOLOGIST,	 objectivePct(Objective.ECOLOGIST.ordinal())));
		strList.add(labelLine(DIPLOMAT,		 objectivePct(Objective.DIPLOMAT.ordinal())));
		strList.add(labelLine(INDUSTRIALIST, objectivePct(Objective.INDUSTRIALIST.ordinal())));
		strList.add(labelLine(EXPANSIONIST,	 objectivePct(Objective.EXPANSIONIST.ordinal())));
		strList.add(labelLine(TECHNOLOGIST,	 objectivePct(Objective.TECHNOLOGIST.ordinal())));
		return strList;
	}

	// ========================================================================
	// SPECIES SHIPSET MODIFIERS
	//
	static final String PREF_SHIP_MOD	= "Preferredship";
	static final String SHIP_SET		= "Preferred ship set";
	static final String SHIP_SIZE		= "Preferred ship size";

	private void parseSpeciesShipSizeMods(String input)	{
		List<String> vals = substrings(input, ',');
		parseSpeciesShipSet(vals.get(0));
		parseSpeciesShipSize(vals.get(1));
	}
	private void parseSpeciesShipSet(String val)	{ preferredShipSet(val); }
	private void parseSpeciesShipSize(String val)	{ preferredShipSize(parseInt(val)); }

	private List<String> listSpeciesPreferedShipMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// Preferred ship set & ship size  (0-small to 3-huge)");
		strList.add(labelLine(SHIP_SET,		preferredShipSet()));
		strList.add(labelLine(SHIP_SIZE,	preferredShipSize()));
		return strList;
	}

	// ========================================================================
	// SPECIES SHIP BONUS MODIFIERS
	//
	static final String SHIP_MOD		= "Shipmod";
	static final String SHIP_ATTACK		= "Ship attack bonus";
	static final String SHIP_DEFENSE	= "Ship defense bonus";
	static final String SHIP_INITIATIVE	= "Ship initiative bonus";

	private void parseSpeciesShipMods(String input)	{
		List<String> vals = substrings(input, ',');
		parseSpeciesShipAttack(vals.get(0));
		parseSpeciesShipDefense(vals.get(1));
		parseSpeciesShipInitiative(vals.get(2));
	}
	private void parseSpeciesShipAttack(String val)		{ shipAttackBonus(parseInt(val)); }
	private void parseSpeciesShipDefense(String val)	{ shipDefenseBonus(parseInt(val)); }
	private void parseSpeciesShipInitiative(String val)	{ shipInitiativeBonus(parseInt(val)); }

	private List<String> listSpeciesShipMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// Ship bonus modifiers");
		strList.add(labelLine(SHIP_ATTACK,		shipAttackBonus()));
		strList.add(labelLine(SHIP_DEFENSE,		shipDefenseBonus()));
		strList.add(labelLine(SHIP_INITIATIVE,	shipInitiativeBonus()));
		return strList;
	}

	// ========================================================================
	// SPECIES GROUND MODIFIERS
	//
	static final String GROUND_MOD			= "Groundmod";
	static final String GROUND_ATTACK_MOD	= "Ground attack bonus";

	private void parseSpeciesGroundMods(String value)	{ groundAttackBonus(parseInt(value)); }

	private List<String> listSpeciesGroundMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// Ground attack modifiers %");
		strList.add(labelLine(GROUND_ATTACK_MOD, groundAttackBonus()));
		return strList;
	}

	// ========================================================================
	// SPECIES SPY MODIFIERS
	//
	static final String SPY_MOD				= "Spymod";
	static final String SPY_COST			= "Spy cost %";
	static final String SECURITY_ADJ		= "Security adj %";
	static final String INFILTRATION_ADJ	= "Infiltration adj %";
	static final String TELEPATHIC			= "Telepathic";
	static final String MASK_RELATIONS		= "Mask relations";

	private void parseSpeciesSpyMods(String input)	{
		List<String> vals = substrings(input, ',');
		parseSpeciesSpyCost(vals.get(0));
		parseSpeciesInternalSecurity(vals.get(1));
		parseSpeciesSpyInfiltration(vals.get(2));
		parseSpeciesTelepathic(vals.get(3));
		parseSpeciesMasksDiplomacy(vals.get(4));
	}
	private void parseSpeciesSpyCost(String val)			{ spyCostMod(parsePct(val)); }
	private void parseSpeciesInternalSecurity(String val)	{ internalSecurityAdj(parsePct(val)); }
	private void parseSpeciesSpyInfiltration(String val)	{ spyInfiltrationAdj(parsePct(val)); }
	private void parseSpeciesTelepathic(String val)			{ telepathic(parseBoolean(val)); }
	private void parseSpeciesMasksDiplomacy(String val)		{ masksDiplomacy(parseBoolean(val)); }

	private List<String> listSpeciesSpyMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// production modifiers");
		strList.add(labelLine(SPY_COST,			toPct(spyCostMod())));
		strList.add(labelLine(SECURITY_ADJ,		toPct(internalSecurityAdj())));
		strList.add(labelLine(INFILTRATION_ADJ,	toPct(spyInfiltrationAdj())));
		strList.add(labelLine(TELEPATHIC,		telepathic()));
		strList.add(labelLine(MASK_RELATIONS,	masksDiplomacy()));
		return strList;
	}

	// ========================================================================
	// SPECIES PRODUCTION MODIFIERS
	//
	static final String PROD_MOD			= "Prodmod";
	static final String WORKER_PROD			= "Worker productivity %";
	static final String FACTORY_CONTROLS	= "Factory controls bonus %";
	static final String IGNORE_REFIT_COST	= "Ignore refit cost";

	private void parseSpeciesProdMods(String input)	{
		List<String> vals = substrings(input, ',');
		if (vals.size() < 3)
			err("Race ", name(), " is missing some prod vars. ");
		parseSpeciesWorkerProd(vals.get(0));
		parseSpeciesRobotControl(vals.get(1));
		parseSpeciesIgnoreRefitCost(vals.get(2));
	}
	private void parseSpeciesWorkerProd(String val)		 { workerProductivityMod(parsePct(val)); }
	private void parseSpeciesRobotControl(String val)	 { robotControlsAdj(parseInt(val)); }
	private void parseSpeciesIgnoreRefitCost(String val) { ignoresFactoryRefit(parseBoolean(val)); }

	private List<String> listSpeciesProdMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// production modifiers");
		strList.add(labelLine(WORKER_PROD,		 toPct(workerProductivityMod())));
		strList.add(labelLine(FACTORY_CONTROLS,	 robotControlsAdj()));
		strList.add(labelLine(IGNORE_REFIT_COST, ignoresFactoryRefit()));
		return strList;
	}

	// ========================================================================
	// SPECIES TECHNOLOGY MODIFIERS
	//
	static final String TECH_MOD		= "Techmod";
	static final String TECH_DISCOVERY	= "Tech discovery %";
	static final String RESEARCH_BONUS	= "Research bonus %";

	private void parseSpeciesTechMods(String input)	{
		List<String> vals = substrings(input, ',');
		if (vals.size() < 2)
			err("Race ", name(), " is missing some research vars. ");
		// field #1 is tech discovery pct (only field for now)
		techDiscoveryPct(parsePct(vals.get(0)));
		researchBonusPct(parsePct(vals.get(1)));
	}
	private void parseSpeciesTechDiscovery(String val)	{ techDiscoveryPct(parsePct(val)); }
	private void parseSpeciesResearchBonus(String val)	{ researchBonusPct(parsePct(val)); }

	private List<String> listSpeciesTechMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// Technology Research modifiers");
		strList.add(labelLine(TECH_DISCOVERY, toPct(techDiscoveryPct())));
		strList.add(labelLine(RESEARCH_BONUS, toPct(researchBonusPct())));
		return strList;
	}

	// ========================================================================
	// SPECIES POPULATION MODIFIERS
	//
	static final String POPULATION_MOD	  = "Popmod";
	static final String POPULATION_GROWTH = "Population growth bonus %";

	private void parseSpeciesPopMods(String input)	{ growthRateMod(parsePct(input)); }

	private List<String> listSpeciesPopMods()		{
		List<String> strList = new ArrayList<>();
		strList.add("// population modifiers - growth rate");
		strList.add(labelLine(POPULATION_MOD, toPct(growthRateMod())));
		return strList;
	}

	// ========================================================================
	// SPECIES DIPLOMACY MODIFIERS
	//
	static final String DIPLO_MOD		= "Diplomod %";
	static final String TRADE_BONUS		= "Trade bonus %";
	static final String POSITIVE_DP_MOD	= "Positive dp mod %";
	static final String DIPLOMACY_BONUS	= "Diplomacy bonus %";
	static final String COUNCIL_BONUS	= "Council bonus %";

	private void parseSpeciesDiploMods(String input)	{
		List<String> vals = substrings(input, ',');
		// field #1 is trade bonus modifier (as %)
		parseSpeciesTradeBonusMods(vals.get(0));
		// field #2 is positive DP modifier (as %)
		parseSpeciesPositiveDPMods(vals.get(1));
		// field #3 is diplomatic relations modifier (as %)
		parseSpeciesDiplomacyBonusMods(vals.get(2));
		// field #4 is council vote modifier (as %)
		parseSpeciesCouncilBonusMods(vals.get(3));
	}
	private void parseSpeciesTradeBonusMods(String val)		{ tradePctBonus(parsePct(val)); }
	private void parseSpeciesPositiveDPMods(String val)		{ positiveDPMod(parsePct(val)); }
	private void parseSpeciesDiplomacyBonusMods(String val)	{ diplomacyBonus(parseInt(val)); }
	private void parseSpeciesCouncilBonusMods(String val)	{ councilBonus(parsePct(val)); }

	private List<String> listSpeciesDiploMods()	{
		String unusedModifier = labelLine(POSITIVE_DP_MOD,	toPct(positiveDPMod()));
		List<String> strList = new ArrayList<>();
		strList.add("// diplomacy modifiers %");
		strList.add(labelLine(TRADE_BONUS,		toPct(tradePctBonus())));
		strList.add(labelLine(unusedModifier,	" (Unused modifier)"));
		strList.add(labelLine(DIPLOMACY_BONUS,	diplomacyBonus()));
		strList.add(labelLine(COUNCIL_BONUS,	toPct(councilBonus())));
		return strList;
	}

	// ========================================================================
	// SPECIES RESEARCH MODIFIERS
	//
	static final String RESEARCH		= "Research";
	static final String COMPUTER		= "Computer %";
	static final String CONSTRUCTION	= "Construction %";
	static final String FORCE_FIELD		= "Force field %";
	static final String PLANETOLOGY		= "Planetology %";
	static final String PROPULSION		= "Propulsion %";
	static final String WEAPON			= "Weapon %";

	private void parseSpeciesResearchMods(String input)	{
		List<String> vals = substrings(input, ',');
		for (int i = 0; i<6; i++)
			parseSpeciesResearch(vals.get(i), i);
	}
	private void parseSpeciesResearch(String val, int cat)	{ techMod(cat, parsePct(val)); }

	private List<String> listSpeciesResearchMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// research category modifiers %");
		strList.add(labelLine(COMPUTER,		techModPct(TechCategory.COMPUTER)));
		strList.add(labelLine(CONSTRUCTION,	techModPct(TechCategory.CONSTRUCTION)));
		strList.add(labelLine(FORCE_FIELD,	techModPct(TechCategory.FORCE_FIELD)));
		strList.add(labelLine(PLANETOLOGY,	techModPct(TechCategory.PLANETOLOGY)));
		strList.add(labelLine(PROPULSION,	techModPct(TechCategory.PROPULSION)));
		strList.add(labelLine(WEAPON,		techModPct(TechCategory.WEAPON)));
		return strList;
	}

	// ========================================================================
	// SPECIES RELATIONS MODIFIERS
	//
	static final String RELATIONS_DEFAULT	= "DEFAULT";
	static final char	RELATIONS_SEP		= '=';
	static final String RELATIONS			= "Relations";

	private void parseSpeciesRelationsMods(String input)	{
		List<String> relations = substrings(input, ',');
		for (String s : relations) {
			List<String> vals = substrings(s, RELATIONS_SEP);
			if (vals.get(0).equalsIgnoreCase(RELATIONS_DEFAULT))
				defaultRaceRelations(parseInt(vals.get(1)));
			else
				baseRelations(vals.get(0), parseInt(vals.get(1)));
		}
	}

	private List<String> listSpeciesRelationsMods()	{
		List<String> list = new ArrayList<>();
		list.add(RELATIONS_DEFAULT + RELATIONS_SEP + defaultRaceRelations());
		for (Entry<String, Integer> entry : raceRelations.entrySet())
			list.add(entry.getKey() + RELATIONS_SEP + entry.getValue());

		List<String> strList = new ArrayList<>();
		strList.add("// race relations modifiers");
		strList.add(labelLine(RELATIONS, list));

		return strList;
	}

	// ========================================================================
	// SHIP DESIGN MODIFIERS
	//
	static final String SHIP_DESIGN			= "Shipdesign";
	static final String COST_MULT_S			= "Cost mult small";
	static final String COST_MULT_M			= "Cost mult medium";
	static final String COST_MULT_L			= "Cost mult large";
	static final String COST_MULT_H			= "Cost mult huge";
	static final String MODULE_SPACE		= "Module space";
	static final String SHIELD_WGHT_FB		= "Shield weight fighter bomber";
	static final String SHIELD_WGHT_D		= "Shield weight destroyer";
	static final String ECM_WGHT_FD			= "Ecm weight fighter destroyer";
	static final String ECM_WGHT_B			= "Ecm weight bomber";
	static final String MANEUVER_WGHT_BD	= "Manoeuver weight bomber destroyer";
	static final String MANEUVER_WGHT_F		= "Manoeuver weight fighter";
	static final String ARMOR_WGHT_FB		= "Armor weight fighter bomber";
	static final String ARMOR_WGHT_D		= "Armor weight destroyer";
	static final String SPECIALS_WGHT		= "Special weight";
	static final String SPEED_MATCHING		= "Speed matching";
	static final String REINFORCED_ARMOR	= "Reinforced armor";
	static final String BIO_WEAPONS			= "Bio weapons";
	static final String PREF_PULSARS		= "Prefers pulsars";
	static final String PREF_CLOAK			= "Prefers cloak";
	static final String PREF_REPAIR			= "Prefers repair";
	static final String PREF_INERTIAL		= "Prefers inertial";
	static final String PREF_MISS_SHIELD	= "Prefers missile shield";
	static final String PREF_REPULSOR		= "Prefers repulsor";
	static final String PREF_STASIS			= "Prefers stasis";
	static final String PREF_STREAM_PROJ	= "Prefers stream projector";
	static final String PREF_WARP_DISSIP	= "Prefers warp dissipator";
	static final String PREF_TECH_NULLIF	= "Prefers tech nullifier";
	static final String PREF_BEAM_FOCUS		= "Prefers beam focus";

	private void parseShipDesignMods(String input)	{
		List<String> mods = substrings(input, ',');
		int maxMods = shipDesignModsSize();

		if (mods.size() > maxMods)
			err("Too many ship design modifiers specified for " + name());
		for (int i=0; i<mods.size(); i++) {
			float val = parseFloat(mods.get(i));
			if (i < maxMods)
				shipDesignMods(i, val);
		}
	}
	private void parseShipDesign(String mod, int id)	{ shipDesignMods(id, parseFloat(mod)); }

	private List<String> listShipDesignMods()	{
		List<String> strList = new ArrayList<>();
		strList.add("// ship design modifiers");
		strList.add(labelLine(COST_MULT_S,		shipDesignMods(ShipTemplate.COST_MULT_S), 2));
		strList.add(labelLine(COST_MULT_M,		shipDesignMods(ShipTemplate.COST_MULT_M), 2));
		strList.add(labelLine(COST_MULT_L,		shipDesignMods(ShipTemplate.COST_MULT_L), 2));
		strList.add(labelLine(COST_MULT_H,		shipDesignMods(ShipTemplate.COST_MULT_H), 2));
		strList.add(labelLine(MODULE_SPACE,		shipDesignMods(ShipTemplate.MODULE_SPACE), 2));
		strList.add(labelLine(SHIELD_WGHT_FB,	shipDesignMods(ShipTemplate.SHIELD_WEIGHT_FB), 0));
		strList.add(labelLine(SHIELD_WGHT_D,	shipDesignMods(ShipTemplate.SHIELD_WEIGHT_D), 0));
		strList.add(labelLine(ECM_WGHT_FD,		shipDesignMods(ShipTemplate.ECM_WEIGHT_FD), 0));
		strList.add(labelLine(ECM_WGHT_B,		shipDesignMods(ShipTemplate.ECM_WEIGHT_B), 0));
		strList.add(labelLine(MANEUVER_WGHT_BD,	shipDesignMods(ShipTemplate.MANEUVER_WEIGHT_BD), 0));
		strList.add(labelLine(MANEUVER_WGHT_F,	shipDesignMods(ShipTemplate.MANEUVER_WEIGHT_F), 0));
		strList.add(labelLine(ARMOR_WGHT_FB,	shipDesignMods(ShipTemplate.ARMOR_WEIGHT_FB), 0));
		strList.add(labelLine(ARMOR_WGHT_D,		shipDesignMods(ShipTemplate.ARMOR_WEIGHT_D), 0));
		strList.add(labelLine(SPECIALS_WGHT,	shipDesignMods(ShipTemplate.SPECIALS_WEIGHT), 0));
		strList.add(labelLine(SPEED_MATCHING,	shipDesignMods(ShipTemplate.SPEED_MATCHING), 0));
		strList.add(labelLine(REINFORCED_ARMOR,	shipDesignMods(ShipTemplate.REINFORCED_ARMOR), 0));
		strList.add(labelLine(BIO_WEAPONS,		shipDesignMods(ShipTemplate.BIO_WEAPONS), 0));
		strList.add(labelLine(PREF_PULSARS,		shipDesignMods(ShipTemplate.PREF_PULSARS), 0));
		strList.add(labelLine(PREF_CLOAK,		shipDesignMods(ShipTemplate.PREF_CLOAK), 0));
		strList.add(labelLine(PREF_REPAIR,		shipDesignMods(ShipTemplate.PREF_REPAIR), 0));
		strList.add(labelLine(PREF_INERTIAL,	shipDesignMods(ShipTemplate.PREF_INERTIAL), 0));
		strList.add(labelLine(PREF_MISS_SHIELD,	shipDesignMods(ShipTemplate.PREF_MISS_SHIELD), 0));
		strList.add(labelLine(PREF_REPULSOR,	shipDesignMods(ShipTemplate.PREF_REPULSOR), 0));
		strList.add(labelLine(PREF_STASIS,		shipDesignMods(ShipTemplate.PREF_STASIS), 0));
		strList.add(labelLine(PREF_STREAM_PROJ,	shipDesignMods(ShipTemplate.PREF_STREAM_PROJECTOR), 0));
		strList.add(labelLine(PREF_WARP_DISSIP,	shipDesignMods(ShipTemplate.PREF_WARP_DISSIPATOR), 0));
		strList.add(labelLine(PREF_TECH_NULLIF,	shipDesignMods(ShipTemplate.PREF_TECH_NULLIFIER), 0));
		strList.add(labelLine(PREF_BEAM_FOCUS,	shipDesignMods(ShipTemplate.PREF_BEAM_FOCUS), 0));
		return strList;
	}

	private List<String> listSpeciesAbilities()	{ // TODO BR: listSpeciesAbilities
		List<String> strList = new ArrayList<>();
		strList.addAll(listSpeciesKeysMods());
		strList.add("");
		strList.addAll(listSpeciesHomeMods());

		strList.add("");
		strList.addAll(listSpeciesTypeMods());
		strList.add("");
		strList.addAll(listLeaderPersonalityMods());
		strList.add("");
		strList.addAll(listLeaderObjectiveMods());
		strList.add("");
		strList.addAll(listSpeciesPreferedShipMods());
		strList.add("");
		strList.addAll(listSpeciesShipMods());
		strList.add("");
		strList.addAll(listSpeciesGroundMods());
		strList.add("");
		strList.addAll(listSpeciesSpyMods());
		strList.add("");
		strList.addAll(listSpeciesProdMods());
		strList.add("");
		strList.addAll(listSpeciesTechMods());
		strList.add("");
		strList.addAll(listSpeciesPopMods());
		strList.add("");
		strList.addAll(listSpeciesDiploMods());
		strList.add("");
		strList.addAll(listSpeciesResearchMods());
		strList.add("");
		strList.addAll(listSpeciesRelationsMods());
		strList.add("");
		strList.addAll(listShipDesignMods());
		return strList;
	}
	private void writeSpeciesAbilities()	{ // TODO BR: writeSpeciesAbilities
		List<String> strList = listSpeciesAbilities();
		
	}
}

