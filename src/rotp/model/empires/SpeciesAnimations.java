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
package rotp.model.empires;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import rotp.model.empires.Leader.Objective;
import rotp.model.empires.Leader.Personality;
import rotp.model.game.DynOptions;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipDesign;
import rotp.util.Base;
import rotp.util.ImageTransformer;
import rotp.util.LabelManager;
import rotp.util.LanguageManager;

public class SpeciesAnimations implements ISpecies, Base {
	private static final int PERSONALITY_COUNT	= Personality.values().length;
	private static final int OBJECTIVE_COUNT	= Objective.values().length;
	private static final int DESIGN_MODS_COUNT	= 28;

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
	private RaceCombatAnimation troopNormal = new RaceCombatAnimation();
	private RaceCombatAnimation troopHostile = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath1 = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath2 = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath3 = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath4 = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath1H = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath2H = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath3H = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath4H = new RaceCombatAnimation();
	private List<String> fortressKeys = new ArrayList<>();
	private String shieldKey;
	private String voiceKey;
	private String ambienceKey;
	private String flagWarKey, flagNormKey, flagPactKey;
	private String dlgWarKey, dlgNormKey,dlgPactKey;
	private String winSplashKey, lossSplashKey;
	private Color winTextC, lossTextC;
	private ImageTransformer diplomacyTransformer; // BR: Never Used!
	private List<String> soundKeys = new ArrayList<>();
	private SpeciesLabels speciesLabels = new SpeciesLabels();
//	private List<String> raceNames = new ArrayList<>();
//	private List<String> homeSystemNames = new ArrayList<>();
//	private List<String> leaderNames = new ArrayList<>();
//	private List<String> systemNames = new ArrayList<>();
//
//	private List<String> shipNamesSmall = new ArrayList<>();
//	private List<String> shipNamesMedium = new ArrayList<>();
//	private List<String> shipNamesLarge = new ArrayList<>();
//	private List<String> shipNamesHuge = new ArrayList<>();
//
//	private final List<String> remainingRaceNames = new ArrayList<>();
//	private final List<String> remainingHomeworldNames = new ArrayList<>();
//	private final List<String> remainingLeaderNames = new ArrayList<>();
	private float defaultRaceRelations = 0;
	private HashMap<String, Integer> raceRelations = new HashMap<>();
	private LabelManager labels;

	// BR: Settings that where encoded in "HomeworldKey"
	private float bCBonus = 0f;
	private float hPFactor = 1f;
	private float maintenanceFactor = 1f;
	private float shipSpaceFactor = 1f;
	private String planetArtifacts = "None";
	private String planetRessource = "Normal";
	private String planetEnvironment = "Normal";
	// Custom Races:
	private boolean isCustomRace = false;
	private DynOptions raceOptions	= null;
	// \BR:
	private int startingYear;
	private int speciesType;
	private String homeworldStarType;
	private String homeworldPlanetType;
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
	private String acceptedPlanetEnvironment = "No";
	private boolean ignoresFactoryRefit = false;
	private boolean availablePlayer = true;
	private boolean availableAI = true;
	private boolean masksDiplomacy = false;
	private float labFlagX = 0;
	private int espionageX, espionageY;
	private int spyFactoryFrames = 0;
	private int spyMissileFrames = 0;
	private int spyRebellionFrames = 0;
	private String title;
	private String fullTitle;
	private int homeworldKey, introTextX;
	private int transportW, transportYOffset, transportLandingFrames, colonistWalkingFrames;
	private int colonistDelay, colonistX1, colonistX2, colonistY1, colonistY2;
	private int dialogLeftMargin, dialogRightMargin,  dialogTopY;
	private float diploScale, diploOpacity;
	private int diploXOffset, diploYOffset;
	private int flagW, flagH;

	private float[] personalityPct	= new float[PERSONALITY_COUNT];
	private float[] objectivePct	= new float[OBJECTIVE_COUNT];
	private float[] shipDesignMods	= new float[DESIGN_MODS_COUNT];

	private transient BufferedImage transportClosedImg;
	private transient Image transportImg;
	private transient BufferedImage diploMug, wideDiploMug;

	SpeciesAnimations () {
		speciesLabels.leaderNames.add("Leader");
		for (int i=0; i<PERSONALITY_COUNT; i++)
			personalityPct(i, 1);
		for (int i=0; i<OBJECTIVE_COUNT; i++)
			objectivePct(i, 1);
	}

	SpeciesAnimations(String dirPath) {
		directoryName = dirPath;
		labels = new LabelManager();
	}

	protected SpeciesAnimations ( SpeciesAnimations src) {
		id	= src.id;
		setupName		= src.setupName;
		empireTitle		= src.empireTitle;;
		racePrefix		= src.racePrefix;
		raceSuffix		= src.raceSuffix;
		leaderPrefix	= src.leaderPrefix;
		leaderSuffix	= src.leaderSuffix;
		worldsPrefix	= src.worldsPrefix;
		worldsSuffix	= src.worldsSuffix;
		langKey			= src.langKey;
		description1	= src.description1;
		description2	= src.description2;
		description3	= src.description3;
		description4	= src.description4;
		directoryName	= src.directoryName;
		laboratoryKey	= src.laboratoryKey;
		embassyKey		= src.embassyKey;
		councilKey		= src.councilKey;
		holographKey	= src.holographKey;
		diplomatKey		= src.diplomatKey;
		scientistKey	= src.scientistKey;
		soldierKey		= src.soldierKey;
		spyFaceKey		= src.spyFaceKey;
		leaderKey		= src.leaderKey;
		soldierFaceKey	= src.soldierFaceKey;
		mugshotKey		= src.mugshotKey;
		wideMugshotKey	= src.wideMugshotKey;
		setupImageKey	= src.setupImageKey;
		advisorFaceKey	= src.advisorFaceKey;
		advisorScoutKey	= src.advisorScoutKey;
		advisorTransportKey	= src.advisorTransportKey;
		advisorDiplomacyKey	= src.advisorDiplomacyKey;
		advisorShipKey		= src.advisorShipKey;
		advisorRallyKey		= src.advisorRallyKey;
		advisorMissileKey	= src.advisorMissileKey;
		advisorWeaponKey	= src.advisorWeaponKey;
		advisorCouncilKey	= src.advisorCouncilKey;
		advisorRebellionKey	= src.advisorRebellionKey;
		advisorResistCouncilKey	= src.advisorResistCouncilKey;
		advisorCouncilResistedKey	= src.advisorCouncilResistedKey;
		diplomacyTheme	= src.diplomacyTheme;
		spyKey			= src.spyKey;
		gnnKey			= src.gnnKey;
		gnnHostKey		= src.gnnHostKey;
		gnnColor		= src.gnnColor;
		gnnTextColor	= src.gnnTextColor;
		transportKey	= src.transportKey;
		transportDescKey	= src.transportDescKey;
		transportOpenKey	= src.transportOpenKey;
		transportDescFrames	= src.transportDescFrames;
		transportOpenFrames	= src.transportOpenFrames;
		shipAudioKey	= src.shipAudioKey;
		troopNormal		= src.troopNormal;
		troopHostile	= src.troopHostile;
		troopDeath1		= src.troopDeath1;
		troopDeath2		= src.troopDeath2;
		troopDeath3		= src.troopDeath3;
		troopDeath4		= src.troopDeath4;
		troopDeath1H	= src.troopDeath1H;
		troopDeath2H	= src.troopDeath2H;
		troopDeath3H	= src.troopDeath3H;
		troopDeath4H	= src.troopDeath4H;
		fortressKeys	= new ArrayList<>(src.fortressKeys);
		shieldKey		= src.shieldKey;
		voiceKey		= src.voiceKey;
		ambienceKey		= src.ambienceKey;
		flagWarKey		= src.flagWarKey;
		flagNormKey		= src.flagNormKey;
		flagPactKey		= src.flagPactKey;
		dlgWarKey		= src.dlgWarKey;
		dlgNormKey		= src.dlgNormKey;
		dlgPactKey		= src.dlgPactKey;
		winSplashKey	= src.winSplashKey;
		lossSplashKey	= src.lossSplashKey;
		winTextC		= src.winTextC;
		lossTextC		= src.lossTextC;
		soundKeys		= new ArrayList<>(src.soundKeys);
		diplomacyTransformer	= src.diplomacyTransformer;
		// SpeciesLabels speciesLabels;	// NOT COPIED
		defaultRaceRelations	= src.defaultRaceRelations;
		raceRelations	= new HashMap<>(src.raceRelations);
		// LabelManager labels;	// NOT COPIED
		labels = new LabelManager();
		src.labels.copy(src.labels, labels);

		// Settings that where encoded in "HomeworldKey"
		bCBonus		= src.bCBonus;
		hPFactor	= src.hPFactor;
		maintenanceFactor	= src.maintenanceFactor;
		shipSpaceFactor		= src.shipSpaceFactor;
		planetArtifacts		= src.planetArtifacts;
		planetRessource		= src.planetRessource;
		planetEnvironment	= src.planetEnvironment;
		// Custom Races:
		isCustomRace		= src.isCustomRace;
		// DynOptions raceOptions;	// NOT COPIED
		startingYear		= src.startingYear;
		speciesType			= src.speciesType;
		homeworldStarType	= src.homeworldStarType;
		homeworldPlanetType	= src.homeworldPlanetType;
		homeworldSize		= src.homeworldSize;
		preferredShipSet	= src.preferredShipSet;
		preferredShipSize	= src.preferredShipSize;
		shipAttackBonus		= src.shipAttackBonus;
		shipDefenseBonus	= src.shipDefenseBonus;
		shipInitiativeBonus	= src.shipInitiativeBonus;
		groundAttackBonus	= src.groundAttackBonus;
		telepathic			= src.telepathic;
		spyCostMod			= src.spyCostMod;
		internalSecurityAdj	= src.internalSecurityAdj;
		spyInfiltrationAdj	= src.spyInfiltrationAdj;
		workerProductivityMod	= src.workerProductivityMod;
		robotControlsAdj	= src.robotControlsAdj;
		techDiscoveryPct	= src.techDiscoveryPct;
		researchBonusPct	= src.researchBonusPct;
		growthRateMod		= src.growthRateMod;
		tradePctBonus		= src.tradePctBonus;
		positiveDPMod		= src.positiveDPMod;
		diplomacyBonus		= src.diplomacyBonus;
		councilBonus		= src.councilBonus;
		techMod				= src.techMod.clone();
		discoveryMod		= src.discoveryMod.clone();
		ignoresPlanetEnvironment	= src.ignoresPlanetEnvironment;
		acceptedPlanetEnvironment	= src.acceptedPlanetEnvironment;
		availablePlayer		= src.availablePlayer;
		availableAI			= src.availableAI;
		ignoresFactoryRefit	= src.ignoresFactoryRefit;
		masksDiplomacy		= src.masksDiplomacy;
		labFlagX			= src.labFlagX;
		espionageX			= src.espionageX;
		espionageY			= src.espionageY;
		spyFactoryFrames	= src.spyFactoryFrames;
		spyMissileFrames	= src.spyMissileFrames;
		spyRebellionFrames	= src.spyRebellionFrames;
		title				= src.title;
		fullTitle			= src.fullTitle;
		homeworldKey		= src.homeworldKey;
		introTextX			= src.introTextX;
		transportW			= src.transportW;
		transportYOffset	= src.transportYOffset;
		transportLandingFrames	= src.transportLandingFrames;
		colonistWalkingFrames	= src.colonistWalkingFrames;
		colonistDelay		= src.colonistDelay;
		colonistX1			= src.colonistX1;
		colonistX2			= src.colonistX2;
		colonistY1			= src.colonistY1;
		colonistY2			= src.colonistY2;
		dialogLeftMargin	= src.dialogLeftMargin;
		dialogRightMargin	= src.dialogRightMargin;
		dialogTopY		= src.dialogTopY;
		diploScale		= src.diploScale;
		diploOpacity	= src.diploOpacity;
		diploXOffset	= src.diploXOffset;
		diploYOffset	= src.diploYOffset;
		flagW			= src.flagW;
		flagH			= src.flagH;
		personalityPct	= src.personalityPct.clone();
		objectivePct	= src.objectivePct.clone();
		shipDesignMods	= src.shipDesignMods.clone();
	}

	protected SpeciesAnimations copy()	{ return new SpeciesAnimations(this); }

	public String homeworldPlanetType()	{ return homeworldPlanetType; }
	void homeworldPlanetType(String s)	{ homeworldPlanetType = s; }

	String id()							{ return id; }
	void id(String s)					{ id = s; }
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
	int startingYear()					{ return startingYear; }
	void startingYear(int i)			{ startingYear = i; }
	int speciesType()					{ return speciesType; }
	void speciesType(int i)				{ speciesType = i; }

	float diploScale()					{ return diploScale; }
	void diploScale(float f)			{ diploScale = f; }
	float diploOpacity()				{ return diploOpacity; }
	void diploOpacity(float f)			{ diploOpacity = f; }

	Color gnnTextColor()				{ return gnnTextColor; }
	void gnnTextColor(Color c)			{ gnnTextColor = c; }

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
	String homeworldStarType()			{ return homeworldStarType; }
	void homeworldStarType(String s)	{ homeworldStarType = s; }
	public int homeworldSize()					{ return homeworldSize; }
	void homeworldSize(int i)			{ homeworldSize = i; }
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
	ImageTransformer diplomacyTransformer()			{ return diplomacyTransformer; }
	void diplomacyTransformer(ImageTransformer s)	{ diplomacyTransformer = s; }

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
    // BR: for race customization
    // Get a Copy the current race
//    protected Race copy() {
//    	Race copy = SpeciesManager.current().reloadRaceDataFile(directoryName);
//    	copy.id = id;
//    	labels.copy(labels, copy.labels);
//    	copy.setupName	  = setupName();
//    	copy.empireTitle  = empireTitle();
//    	copy.description1 = description1;
//    	copy.description2 = description2;
//    	copy.description3 = description3;
//    	copy.description4 = description4;
////		copy.raceNames.addAll(raceNames);
////		copy.homeSystemNames.addAll(homeSystemNames);
////		copy.leaderNames.addAll(leaderNames);
////		copy.shipNamesSmall.addAll(shipNamesSmall);
////		copy.shipNamesMedium.addAll(shipNamesMedium);
////	 	copy.shipNamesLarge.addAll(shipNamesLarge);
////		copy.shipNamesHuge.addAll(shipNamesHuge);
//
//		// useless for abilities
//		copy.troopNormal(null);
//		copy.troopHostile(null);
//		copy.troopDeath1(null);
//		copy.troopDeath2(null);
//		copy.troopDeath3(null);
//		copy.troopDeath4(null);
//		copy.troopDeath1H(null);
//		copy.troopDeath2H(null);
//		copy.troopDeath3H(null);
//		copy.troopDeath4H(null);
//		return copy;
//	}
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
	String diplomacyTheme()				{ return diplomacyTheme; }
	void diplomacyTheme(String str)		{ diplomacyTheme = str; }

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

	List<String> customIntroduction() {
		if (isCustomRace) {
			List<String> introLines = new ArrayList<>();
			log("loading Custom Species Intro");
			String filename = CUSTOM_SPECIES_FOLDER +  id + INTRO_FILE_EXTENSION;
			BufferedReader in = reader(filename, false);
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
			if (!introLines.isEmpty())
				return introLines;
		}
		// return race-specific dialogue if present
		// else return default dialog
		if (raceLabels().hasIntroduction())
			return raceLabels().introduction();
		return null;
	}
    List<String> introduction() {
        // return race-specific dialogue if present
        // else return default dialog
        if (raceLabels().hasIntroduction())
            return raceLabels().introduction();
        return labels().introduction();
    }

    private List<String> varTokens(String s) { // BR: for debug
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
	public boolean validateDialogueTokens()	{ // BR: for debug
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
											System.err.println("Missing token: " + token
													+ " / " + id + " / " + src);
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
    String dialogue(String key) {
        // return race-specific dialogue if present
        // else return default dialog
        if (raceLabels().hasDialogue(key))
            return raceLabels().dialogue(key);
        return labels().dialogue(key);
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
//	void setDescription1(String desc)	{ description1 = desc; } // TODO BR: REMOVE
//	void setDescription2(String desc)	{ description2 = desc; }
//	void setDescription3(String desc)	{ description3 = desc; }
//	void setDescription4(String desc)	{ description4 = desc; }
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
	
	public boolean ignoresPlanetEnvironment()	{ return ignoresPlanetEnvironment; }
	void ignoresPlanetEnvironment(boolean b)	{ ignoresPlanetEnvironment = b; }
	public String acceptedPlanetEnvironment()	{ return acceptedPlanetEnvironment; }
	void acceptedPlanetEnvironment(String s)	{ acceptedPlanetEnvironment = s; }

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
	SpeciesAnimations isCustomRace(boolean val)		{ isCustomRace = val; return this;}
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
    void   planetArtifacts(String s)   {
    	planetArtifacts = s;
    	if (planetRessource.equalsIgnoreCase("Artifact")) { // for backward compatibility
    		planetRessource = "Normal";
    		planetArtifacts = "Artifact";
    	}
    }
    String planetRessource()           {
    	if (planetRessource.equalsIgnoreCase("Artifact")) { // for backward compatibility
    		planetRessource = "Normal";
    		planetArtifacts = "Artifact";
    	}
    	return planetRessource;
    }
    void   planetRessource(String s)   {
   		planetRessource = s;
    	if (s.equalsIgnoreCase("Artifact")) { // for backward compatibility
    		planetArtifacts = s;
    		planetRessource = "Normal";
    	}
    }
    String planetEnvironment()         { return planetEnvironment; }
    void   planetEnvironment(String s) { planetEnvironment = s; }

    public boolean raceWithUltraPoorHomeworld() {
        return planetRessource.equalsIgnoreCase("UltraPoor");
    }
    public boolean raceWithPoorHomeworld() {
        return planetRessource.equalsIgnoreCase("Poor");
    }
    public boolean raceWithRichHomeworld() {
        return planetRessource.equalsIgnoreCase("Rich");
    }
    public boolean raceWithUltraRichHomeworld() {
        return planetRessource.equalsIgnoreCase("UltraRich");
    }
    public boolean raceWithOrionLikeHomeworld() {
        return planetArtifacts.equalsIgnoreCase("OrionLike");
    }
    public boolean raceWithArtifactsHomeworld() {
        return planetArtifacts.equalsIgnoreCase("Artifacts")
        		|| planetRessource.equalsIgnoreCase("Artifacts"); // for backward compatibility
    }
    public boolean raceWithHostileHomeworld() {
        return planetEnvironment.equalsIgnoreCase("Hostile");
    }
    public boolean raceWithFertileHomeworld() {
        return planetEnvironment.equalsIgnoreCase("Fertile");
    }
    public boolean raceWithGaiaHomeworld() {
        return planetEnvironment.equalsIgnoreCase("Gaia");
    }

    float defaultRaceRelations()       { return defaultRaceRelations; }
    void defaultRaceRelations(int d)   { defaultRaceRelations = d; }
    float baseRelations(SpeciesAnimations r) {
        float definedRelations = raceRelations.containsKey(r.id) ? raceRelations.get(r.id) : defaultRaceRelations();
        return definedRelations + options().baseAIRelationsAdj();
    }
	void baseRelations(String key, int d) { raceRelations.put(key, d); }
	float labFlagX()                   { return labFlagX; }
	void labFlagX(float d)             { labFlagX = d; }
	int spyFactoryFrames()             { return spyFactoryFrames; }
	void spyFactoryFrames(int d)       { spyFactoryFrames = d; }
	int spyMissileFrames()             { return spyMissileFrames; }
	void spyMissileFrames(int d)       { spyMissileFrames = d; }
	int spyRebellionFrames()           { return spyRebellionFrames; }
	void spyRebellionFrames(int d)     { spyRebellionFrames = d; }
	void espionageXY(List<String> vals) {
        espionageX = parseInt(vals.get(0));
        if (vals.size() > 1)
            espionageY = parseInt(vals.get(1));
    }
	Image flagWar()                    { return image(flagWarKey); }
	public Image flagNorm()                   { return image(flagNormKey); }
	Image flagPact()                   { return image(flagPactKey); }
	Image dialogWar()                  { return image(dlgWarKey); }
	Image dialogNorm()                 { return image(dlgNormKey); }
	Image dialogPact()                 { return image(dlgPactKey); }
	Image council()                    { return image(councilKey);  }
	Image gnnEvent(String id)          { return image(gnnEventKey(id)); }
	private String gnnEventKey(String id)     { return concat(gnnColor,"_",id); }
	BufferedImage gnn()                { return currentFrame(gnnKey); }
	BufferedImage gnnHost()            { return currentFrame(gnnHostKey); }
	BufferedImage laboratory()         { return currentFrame(laboratoryKey);  }
	BufferedImage embassy()            { return currentFrame(embassyKey);  }
	BufferedImage holograph()          { return currentFrame(holographKey);  }
	BufferedImage mugshot()            { return currentFrame(mugshotKey);  }
	public BufferedImage setupImage()         { return currentFrame(setupImageKey);  }
	BufferedImage spyMugshotQuiet()    { return currentFrame(spyFaceKey, notTalking);  }
	BufferedImage soldierMugshot()     { return currentFrame(soldierFaceKey, notTalking);  }
	BufferedImage advisorMugshot()     { return currentFrame(advisorFaceKey, notTalking); }
	BufferedImage advisorScout()       { return currentFrame(advisorScoutKey, notTalking); }
	BufferedImage advisorTransport()   { return currentFrame(advisorTransportKey, notTalking); }
	BufferedImage advisorDiplomacy()   { return currentFrame(advisorDiplomacyKey, notTalking); }
	BufferedImage advisorShip()        { return currentFrame(advisorShipKey, notTalking); }
	BufferedImage advisorRally()       { return currentFrame(advisorRallyKey, notTalking); }
	BufferedImage advisorMissile()     { return currentFrame(advisorMissileKey, notTalking); }
	BufferedImage advisorWeapon()      { return currentFrame(advisorWeaponKey, notTalking); }
	BufferedImage advisorCouncil()     { return currentFrame(advisorCouncilKey, notTalking); }
	BufferedImage advisorRebellion()   { return currentFrame(advisorRebellionKey, notTalking); }
	BufferedImage advisorResistCouncil()   { return currentFrame(advisorResistCouncilKey, notTalking); }
	BufferedImage advisorCouncilResisted()  { return currentFrame(advisorCouncilResistedKey, notTalking); }
	BufferedImage diplomatTalking()    { return currentFrame(diplomatKey);  }
	BufferedImage scientistTalking()   { return currentFrame(scientistKey);  }
	BufferedImage soldierTalking()     { return currentFrame(soldierKey);  }
	BufferedImage spyTalking()         { return currentFrame(spyKey);  }
	public BufferedImage diploMugshotQuiet()  { return currentFrame(mugshotKey, notTalking);  }
	BufferedImage diploWideMugshot()   { return currentFrame(wideMugshotKey, notTalking);  }
	BufferedImage diplomatQuiet()      { return currentFrame(diplomatKey, notTalking);  }
	BufferedImage scientistQuiet()     { return currentFrame(scientistKey, notTalking);  }
	BufferedImage soldierQuiet()       { return currentFrame(soldierKey, notTalking);  }
	BufferedImage spyQuiet()           { return currentFrame(spyKey, notTalking);  }
	BufferedImage councilLeader()      { return asBufferedImage(image(leaderKey));  }
	public BufferedImage diploMug()    {
        if (diploMug == null)
            diploMug = newBufferedImage(diploMugshotQuiet());
        return diploMug;
    }
    BufferedImage wideDiploMug()    {
        if (wideDiploMug == null)
            wideDiploMug = newBufferedImage(diploWideMugshot());
        return wideDiploMug;
    }
    Image transport()          {
        if (transportImg == null)
            transportImg = image(transportKey);
        return transportImg;
    }
    BufferedImage transportDescending()    {
        if (transportClosedImg == null)
            transportClosedImg = currentFrame(transportDescKey, closed);
        return transportClosedImg;
    }
    BufferedImage transportOpening()   { return currentFrame(transportDescKey, open); }
    
    List<Image> sabotageFactoryFrames() {
        List<Image> images = new ArrayList<>();
        for (int i=1;i<=spyFactoryFrames;i++) {
            String fileName = directoryName+"/SabotageFactories/Frame"+String.format("%03d.jpg", i);
            Image img = icon(fileName).getImage();
            images.add(img);
        }
        return images;
    }
    List<Image> sabotageMissileFrames() {
        List<Image> images = new ArrayList<>();
        for (int i=1;i<=spyMissileFrames;i++) {
            String fileName = directoryName+"/SabotageMissiles/Frame"+String.format("%03d.jpg", i);
            Image img = icon(fileName).getImage();
            images.add(img);
        }
        return images;
    }
    List<Image> sabotageRebellionFrames() {
        List<Image> images = new ArrayList<>();
        for (int i=1;i<=spyRebellionFrames;i++) {
            String fileName = directoryName+"/SabotageRebellion/Frame"+String.format("%03d.jpg", i);
            Image img = icon(fileName).getImage();
            images.add(img);
        }
        return images;
    }
	BufferedImage fortress(int i)		{ return currentFrame(fortressKeys.get(i)); }
	int randomFortress()				{ return roll(1,fortressKeys.size())-1; }
	BufferedImage shield()				{ return currentFrame(shieldKey); }
	public void resetMugshot()			{ resetAnimation(mugshotKey); }
	public void resetSetupImage()		{ resetAnimation(setupImageKey); }
	void resetDiplomat()				{ resetAnimation(diplomatKey); }
	void resetScientist()				{ resetAnimation(scientistKey); }
	void resetSoldier()					{ resetAnimation(soldierKey); }
	void resetSpy()						{ resetAnimation(spyKey); }
	void resetGNN(String id)			{
        resetAnimation(gnnKey);
        resetAnimation(gnnHostKey);
        resetAnimation(gnnEventKey(id));
    }
    boolean isHostile(PlanetType pt)	{
        return ignoresPlanetEnvironment() ? false : pt.hostileToTerrans();
    }
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
    int randomLeaderAttitude() {
        float r = random();
        float modAccum = 0;
        for (int i=0;i<PERSONALITY_COUNT;i++) {
            modAccum += personalityPct(i);
            if (r < modAccum)
                return i;
        };
        return 0;
    }
    int mostCommonLeaderAttitude() {
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
    int randomLeaderObjective() {
        float r = random();
        float modAccum = 0;
        for (int i=0;i<OBJECTIVE_COUNT;i++) {
            modAccum += objectivePct(i);
            if (r < modAccum)
                return i;
        };
        return 0;
    }
//    String randomSystemName(Empire emp) {
//        // this is only called when a new system is scouted
//        // the name is stored on the empire's system view for this system
//        // and transferred to the system when it is colonized
//        List<String> allPossibleNames = masterNameList(emp);
//        for (StarSystem sys : galaxy().starSystems()) {
//        	String name = sys.name().trim(); // custom species may add confusing spaces
//        	allPossibleNames.remove(name);
//        }
//        // Multiple and Custom species may share the same list... We have to looks thru all systems view
//        // looking at the galaxy().starSystems() is not good enough. Named only when colonized.
//        int n = galaxy().numStarSystems();
//        for (Empire e : galaxy().empires())
//        	if (!e.extinct())
//                for (int i=0;i<n;i++)
//                    if (e.sv.isScouted(i))
//                        allPossibleNames.remove(emp.sv.name(i));
//        // Custom species may share the same list... We have to looks thru all systems
//        // int n = galaxy().numStarSystems();
//        // for (int i=0;i<n;i++) {
//        //     if (emp.sv.isScouted(i))
//        //         allPossibleNames.remove(emp.sv.name(i));
//        // }
//        String systemName = allPossibleNames.isEmpty() ? galaxy().nextSystemName(id) : allPossibleNames.get(0);
//        log("Naming system:", systemName);
//        return systemName;
//    }
//    private List<String> masterNameList(Empire emp) {
//        Collections.shuffle(systemNames);
//        // BR: add custom species prefix and suffix
//        List<String> complexNames = new ArrayList<>();
//		String prefix = emp.worldsPrefix();
//		String suffix = emp.worldsSuffix();
//        for (String s: systemNames)
//        	complexNames.add(prefix + s + suffix);
//
//        List<String> names = new ArrayList<>(complexNames);
//
//        for (String s: complexNames)
//            names.add(text("COLONY_NAME_2", s));
//        for (String s: complexNames)
//            names.add(text("COLONY_NAME_3", s));
//        for (String s: complexNames)
//            names.add(text("COLONY_NAME_4", s));
//        for (String s: complexNames)
//            names.add(text("COLONY_NAME_5", s));
//        return names;
//    }
    public String randomLeaderName() { return random(leaderNames()); }
    List<String> shipNames(int size) {
        switch(size) {
            case ShipDesign.SMALL:   return shipNamesSmall();
            case ShipDesign.MEDIUM:  return shipNamesMedium();
            case ShipDesign.LARGE:   return shipNamesLarge();
            case ShipDesign.HUGE:    return shipNamesHuge();
        }
        return null;
    }
    void addSoundKey(String s)   { soundKeys.add(s); }
    void colonistWalk(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 3)
            err("Invalid colonistWalk string: ", s);

        // The string argument represents the pixel offset from the
        // top-left of the transport ship for the colonist to walk
        // from and then to before planting his flag
        List<String> points = substrings(vals.get(2), '>');
        if (points.size() != 2)
            err("Invalid colonistWalk string: ", s);

        List<String> fromXY = substrings(points.get(0),'@');
        if (fromXY.size() != 2)
            err("Invalid from point in colonistWalk string:", s);

        List<String> toXY = substrings(points.get(1),'@');
        if (toXY.size() != 2)
            err("Invalid to point in colonistWalk string:", s);

        colonistDelay = parseInt(vals.get(0));
		colonistWalkingFrames(parseInt(vals.get(1)));
        colonistX1 = parseInt(fromXY.get(0));
        colonistY1 = parseInt(fromXY.get(1));
        colonistX2 = parseInt(toXY.get(0));
        colonistY2 = parseInt(toXY.get(1));
    }
    void parseFortress(String s) {
        //  f1|f2|f3|f4, spec
        //   reconstructs as this list:
        //  f1,spec
        //  f2,spec
        //  f3,spec
        //  f4,spec
        List<String> vals = substrings(s, ',');
        if (vals.size() != 2)
            err("Invalid fortress string: ", s);

        List<String> forts = substrings(vals.get(0), '|');
        for (String fort: forts) {
            String fortKey = concat(fort, ",", vals.get(1));
            fortressKeys.add(fortKey);
        }
    }
    void flagSize(String s) {
        List<String> vals = substrings(s, 'x');
        if (vals.size() != 2)
            err("Invalid FlagSize string: ", s);

        flagW(parseInt(vals.get(0)));
        flagH(parseInt(vals.get(1)));
    }
    void parseTransportDesc(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 3)
            err("Invalid TransportDesc string: ", s);

        transportDescKey(concat(vals.get(0), ",", vals.get(2)));
        transportDescFrames(parseInt(vals.get(1)));
    }
    void parseTransportOpen(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 3)
            err("Invalid Transport Open string: ", s);

        transportOpenKey(concat(vals.get(0), ",", vals.get(2)));
        transportOpenFrames (parseInt(vals.get(1)));
    }
    void parseWinSplash(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 4)
            err("Invalid Win Splash string: ", s);

		winSplashKey(vals.get(0));
        int r = parseInt(vals.get(1));
        int g = parseInt(vals.get(2));
        int b = parseInt(vals.get(3));
        winTextC = new Color(r,g,b);
    }
    void parseLossSplash(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 4)
            err("Invalid Loss Splash string: ", s);

		lossSplashKey(vals.get(0).trim());
        int r = parseInt(vals.get(1).trim());
        int g = parseInt(vals.get(2).trim());
        int b = parseInt(vals.get(3).trim());
        lossTextC = new Color(r,g,b);
    }
    void parseCouncilDiplomatLocation(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 4)
            err("Invalid Council Diplomat location string: ", s);

		diploScale(parseFloat(vals.get(0).trim()));
		diploXOffset(parseInt(vals.get(1).trim()));
		diploYOffset(parseInt(vals.get(2).trim()));
		diploOpacity(parseFloat(vals.get(3).trim()));
	}
	public String defaultHomeworldName()	{ return speciesLabels.defaultHomeworldName(); }
	public boolean hasHomeworldNames()		{ return speciesLabels.hasHomeworldNames(); }
	void parseRaceNames(String names)		{ speciesLabels.parseRaceNames(names); }
	SpeciesLabels speciesLabels()			{ return speciesLabels; }
}

