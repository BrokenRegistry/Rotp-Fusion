package rotp.model.empires.species;

import static rotp.ui.util.PlayerShipSet.DISPLAY_RACE_SET;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import rotp.model.empires.Empire;
import rotp.model.empires.Empire.EmpireBaseData;
import rotp.model.empires.Leader;
import rotp.model.empires.RaceCombatAnimation;
import rotp.model.empires.SystemInfo;
import rotp.model.empires.species.DNAFactory.CivilizationRecord;
import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.model.planet.PlanetType;
import rotp.model.tech.Tech;
import rotp.ui.util.StringList;
import rotp.util.Base;
import rotp.util.LabelManager;

public class Species implements ISpecies, Base, Serializable {
	private static final long serialVersionUID = 1L;

	// ====================================================================
	// #=== Species Management
	// ====================================================================
	private static final Map<String, Race> INTERNAL_SPECIES_MAP = new HashMap<>();
	private static final Map<String, String> INTERNAL_NAMES_MAP = new HashMap<>();
	private static final Map<String, String> LANGUAGE_NAMES_MAP = new HashMap<>(); // contains English and loaded languages

	static Race getAnim(String key)			{ return key==null? null : INTERNAL_SPECIES_MAP.get(key); }
	static boolean isValidKey(String key)	{ return key==null? false :INTERNAL_SPECIES_MAP.get(key) != null; }
	static List<Race> races()				{ return new ArrayList<>(INTERNAL_SPECIES_MAP.values()); }
	static Set<Entry<String, Race>> internalMap()	{ return INTERNAL_SPECIES_MAP.entrySet(); }
	static Map<String, String> namesMap()	{ return INTERNAL_NAMES_MAP; }
	static void addRace(Race speciesAnim)	{ INTERNAL_SPECIES_MAP.put(speciesAnim.id(), speciesAnim); }
	static void addName(Race speciesAnim)	{
		INTERNAL_NAMES_MAP.put(speciesAnim.id(), speciesAnim.setupName());
		LANGUAGE_NAMES_MAP.put(speciesAnim.setupName(), speciesAnim.id());
	}
	public static List<String> notFiring()	{ return Race.notFiring; }
	public static void loadAllList()		{
		for (Race skills: Species.races()) {
			skills.loadNameList();
			skills.loadLeaderList();
			skills.loadHomeworldList();
		}
	}
	static String languageToKey (String s)	{ return LANGUAGE_NAMES_MAP.get(s); }
	public static boolean validateDialogueTokens()	{
		boolean valid = true;
		for (Race skills: Species.races())
			valid &= skills.validateDialogueTokens();
		return valid;
	}
	public static String getSpeciesName(String key)				{ return new Species(key).setupName(); }
	public static void loadRaceLangFiles(Species s, String dir)	{ RaceFactory.current().loadRaceLangFiles(s.anim, dir); }
	// ====================================================================
	// Names validations
	//
	private static StringList usedHomeNames, usedLeaderNames, usedSpeciesNames;
	static void cleanUsedNames()	{
		usedHomeNames	 = null;
		usedLeaderNames	 = null;
		usedSpeciesNames = null;
	}
	protected static StringList usedHomeNames()	{
		if (usedHomeNames == null)
			usedHomeNames = new StringList();
		return usedHomeNames;
	}
	protected static StringList usedLeaderNames()	{
		if (usedLeaderNames == null)
			usedLeaderNames = new StringList();
		return usedLeaderNames;
	}
	protected static StringList usedCivilizationNames()	{
		if (usedSpeciesNames == null)
			usedSpeciesNames = new StringList();
		return usedSpeciesNames;
	}
	// -#-
	// ====================================================================
	// Species
	// ====================================================================
//	private static final String CUSTOM_RACE_DESCRIPTION	= "CUSTOM_RACE_DESCRIPTION";

	private transient Race anim;
	private transient SpeciesSkills skills;
	private transient String initialHomeWorld;
	private transient String initialLeaderName;
	transient String fileKey; // for debug purpose
	transient int colorId = -1;
	private boolean isOrion = false;
	private CivilizationId civilizationId;
	private boolean trySkillsForNames = false;
	public CivilizationId getRawCivilizationId(int id)	{ // For restart
		if (civilizationId == null) {
			// backward compatibility
			setOldSpeciesIndex(id);
		}
		return civilizationId;
	}
	private CivilizationId civilizationId()	{ return civilizationId; }
	SpeciesSkills getSkillsForEdit()	{ return skills; }
	protected String empireTitle()		{ return replaceTokens("[this_empire]", "this"); }
	protected String getLeaderName()	{ return initialLeaderName; }
	public String getHomeWorldName()	{ return initialHomeWorld; }
	protected int colorId()				{ return colorId; }
	@Override public String toString()	{
		String s = "";
		s += String.format("%-22s", "Anim: "	+ animKey());
		s += String.format("%-24s", "Skills: "	+ skillKey());
		s += String.format("%-14S", "Custom: "	+ isCustomSpecies());
		if (civilizationId != null) {
			s += String.format("%-25s", "Species: "	 + raceName());
			s += String.format("%-26s", "Civ Name: " + civilizationName());
			s += String.format("%-11s", "Civ idx: "	 + civilizationIndex());
			if (civilizationId.isSkillsNames())
				s += "custom Names ";
			else
				s += "anim Names   ";
		}
		s += String.format("%-28s", "Leader: " + getLeaderName());
		s += String.format("%-22s", "Home: " + getHomeWorldName());
		s += String.format("%-12s", "ColorId: " + colorId());
		s += String.format("%-24s", "file: " + fileKey);
		return s;
	}
	// ====================================================================
	// Constructors
	//
	public Species(String animKey, String skillsKey, DynOptions options)	{
		anim	= getAnim(animKey);
		skills	= anim;
		if (anim == null) { // Add custom race if missing
			setSpeciesSkills(skillsKey, options);
		}
		else if (animKey.equals(skillsKey)) {
			if (options != null)
				setSpeciesSkills(skillsKey, options);
			return;
		}
		else if (skillsKey != null || options != null)
			setSpeciesSkills(skillsKey, options);
	}
	public Species(String key)	{
		if (key.equals(ORION_KEY)) {
			isOrion = true;
			key = "RACE_PSILON";
		}
		anim = getAnim(key);
		if (anim == null) { // Add custom race if missing
			skills = DNAFactory.keyToCustomSpecies(null, key);
			skills.isCustomSpecies(true);
//			skills.setDescription4(skills.text(CUSTOM_RACE_DESCRIPTION));
		}
		else
			skills = anim;
	}
	Species(EmpireBaseData eSrc)	{
		this(eSrc.raceKey, eSrc.dataRaceKey, eSrc.raceOptions);
		civilizationId = eSrc.civilizationId;
		initialHomeWorld = eSrc.homeSys.starName;
		initialLeaderName = eSrc.leaderName;
	}
	protected Species(Species src)	{ // At Empire Creation
		anim	= src.anim;
		skills	= src.skills;
		colorId	= src.colorId;
		initialHomeWorld	= src.initialHomeWorld;
		initialLeaderName	= src.initialLeaderName;
		civilizationId		= src.civilizationId;
		trySkillsForNames	= trySkillsForNames();
	}
	// ====================================================================
	// Initializers
	//
	private void setOldSpeciesIndex(int id)	{
		if (civilizationId == null) {
			String name;
			if (trySkillsForNames) {
				name = skills.nameVariant(id);
				if (!name.isEmpty()) {
					civilizationId = new CivilizationId(name, id);
					return;
				}
			}
			name = anim.nameVariant(id);
			civilizationId = new CivilizationId(name, id);
		}
		else
			civilizationId.setIndex(id);
	}
	public void validateOnLoad(String animKey, String skillsKey, DynOptions options, int raceNameIndex)	{
		Species species = new Species(animKey, skillsKey, options);
		anim	= species.anim;
		skills	= species.skills;
		if (civilizationId == null) {
			setOldSpeciesIndex(raceNameIndex);
		}
	}
	public void setNewSpeciesAnim(String animKey)	{ anim = getAnim(animKey); }
	void setAllCustomNames(CivilizationRecord civ, String langDir)	{
		civilizationId = new CivilizationId(civ, langDir, true);
		initialHomeWorld = civ.homeWorld;
		initialLeaderName = civ.leaderName;
	}
	boolean lockUsedNames()	{
		boolean used = usedCivilizationNames().contains(civilizationName());
		used &= usedLeaderNames().contains(initialLeaderName);
		used &= usedHomeNames().contains(initialHomeWorld);
		usedCivilizationNames().add(civilizationName());
		usedLeaderNames().add(initialLeaderName);
		usedHomeNames().add(initialHomeWorld);
		return used;
	}
	void setAllAnimNames(CivilizationRecord civ, String langDir)	{
		civilizationId = new CivilizationId(civ, langDir, false);
		initialHomeWorld = civ.homeWorld;
		initialLeaderName = civ.leaderName;
	}
	void setPlayerAnimNames(IGameOptions options, String langDir)	{
		initialLeaderName = options.selectedLeaderName();
		initialHomeWorld = options.selectedHomeWorldName();
		String civName = setupName();
		civilizationId = new CivilizationId(civName, civName, 0, langDir, isCustomSpecies());
	}
	SpeciesSkills setSpeciesSkills(String skillsKey)	{
		skills = getAnim(skillsKey);
		if (skills == null) {
			skills = DNAFactory.keyToCustomSpecies(anim, skillsKey);
			skills.isCustomSpecies(true);
//			skills.setDescription4(skills.text(CUSTOM_RACE_DESCRIPTION)); // TO DO BR: May be not!!!
		}
		return skills;
	}
	public SpeciesSkills setSpeciesSkills(String skillsKey, DynOptions options)	{
		if (options == null)
			return setSpeciesSkills(skillsKey);
		skills = DNAFactory.optionToSkills(anim, options);
		skills.isCustomSpecies(true);
		return skills;
	}
	SpeciesSkills setSpeciesSkills(DynOptions options)	{
		skills = DNAFactory.optionToSkills(anim, options);
		skills.isCustomSpecies(true);
		return skills;
	}
	public void setSpeciesSkills(SpeciesSkills speciesSkills)	{ skills = speciesSkills; }
	SpeciesSkills getSkillCopy(boolean full)	{ return skills.copy(full); }

	// ====================================================================
	// To be overridden
	//
	protected boolean isPlayer()			{ return true; }
	protected int civilizationNameIndex()	{ return civilizationIndex(); }
	protected int capitalSysId()			{ return 0; }
	protected SystemInfo sv()				{ return null; }
	protected Leader leader()				{ return null; }

	// ====================================================================
	// class tools
	//
	protected int civilizationIndex()	{ return civilizationId().getIndex(); }
	public String label(String token)	{
		List<String> values = substrings(anim.text(token), ',');
		return civilizationNameIndex() < values.size() ? values.get(civilizationNameIndex()) : values.get(0);
	}
	private boolean isSkillsNames()		{ return civilizationId == null? false : civilizationId.isSkillsNames(); }
	// Species Methods
	private boolean trySkillsForNames()	{
		if (!isCustomSpecies()) // only available for customized species
			return false;
		if (skills.isRandomized())
			return false;
		if (options().randomizeAIAbility()) // => this means hidden abilities
			return false;
		return true;
	}
	private String tokenText(String key, boolean trySkills)	{
		if (trySkills) {
			if (skills.raceLabels().hasLabel(key))
				return skills.raceLabels().label(key);
		}
		return anim.text(key);
	}
	public String replaceTokens(String s, String key)	{
		if (key.equals("player")) // BR: many confusion in translations
			s = replaceTokens(s, "my");
		boolean trySkills = trySkillsForNames && isSkillsNames();
		List<String> tokens = this.varTokens(s, key);
		String s1 = s;
		for (String token: tokens) {
			String replString = concat("[",key, token,"]");
			// leader name is special case, not in dictionary
			if (token.equals("_name")) 
				s1 = s1.replace(replString, leader().name());
			else if (token.equals("_home"))
				s1 = s1.replace(replString, sv().name(capitalSysId()));
			else {
				List<String> animValues = substrings(anim.text(token), ',');
				int idx = civilizationNameIndex();
				if (trySkills) {
					String text = tokenText(token, trySkills);
					if (!token.equals(text)) {
						List<String> skillsValues = substrings(text, ',');
						if (animValues.size() == 1) { // "_title or equivalent, could be empty
							if (skillsValues.size() > 0) {
								s1 = s1.replace(replString, skillsValues.get(0));
								return s1;
							}
						}
						else { // Must not be empty string
							if (idx < skillsValues.size()) {
								String value = skillsValues.get(idx);
								if (value != null && !value.isEmpty()) {
									s1 = s1.replace(replString, value);
									return s1;
								}
							}
						}
					}
				}
				String value = idx < animValues.size() ? animValues.get(idx) : animValues.get(0);
				s1 = s1.replace(replString, value);
			}
		}
		return s1;
	}
	public boolean acceptedPlanetEnvironment(PlanetType pt)	{
		switch (acceptedPlanetEnvironment()) {
			case "Limited":
				switch (pt.key()) {
					case PlanetType.INFERNO:
					case PlanetType.TOXIC:
					case PlanetType.RADIATED:
						return false;
					default:
						return true;
				}
			case "All":
			default:
				return true;
		}
	}
	public String raceName()	{
		if (isOrion)
			return "Orion";
//		String name = civilizationId().getSpeciesName();
		String name = civilizationName();
		if (name == null || name.isEmpty())
			return animSetupName(); // BR: for backward compatibility
		return name;
	}
	public boolean isValid()	{ return anim != null && skills != null; }
	public String id()			{ return anim.id() + " / " + skills.id(); }

	public String title()		{ return title(isPlayer()); }
	public String fullTitle()	{ return fullTitle(isPlayer()); }
	public List<String> introduction()	{ return introduction(isPlayer()); }
	public boolean isCustomPlayer()		{ return skills.isCustomSpecies() && isPlayer(); }
//	public String isAnimAutonomous()	{ return skills.isAnimAutonomous(); }
	public void initCRToShow(DNAFactory dnaFactory)	{ dnaFactory.setFromRaceToShow(skills, speciesOptions());}
	// ====================================================================
	// Purely Animations
	//
	protected String animKey()	{ return anim.id(); }
	public String civilizationName()	{
		String name;
		if (trySkillsForNames && civilizationId().isSkillsNames())
			name = skills.nameVariant(civilizationIndex());
		else
			name = anim.nameVariant(civilizationIndex());
		if (name == null || name.isEmpty())
			name = civilizationId().getCivName();
		if (name == null || name.isEmpty())
			return anim.name(); // BR: for backward compatibility
		return name;
	}
	public String animSetupName()	{return anim.setupName(); }

	public boolean masksDiplomacy()	{ return anim.masksDiplomacy(); }

	public int mostCommonLeaderAttitude()	{ return anim.mostCommonLeaderAttitude(); }
	public int homeworldKey()		{ return anim.homeworldKey(); }
	public int randomFortress()		{ return anim.randomFortress(); }
	public int startingYear()		{ return anim.startingYear(); }
	public int dialogTopY()			{ return anim.dialogTopY(); }
	public int dialogRightMargin()	{ return anim.dialogRightMargin(); }
	public int dialogLeftMargin()	{ return anim.dialogLeftMargin(); }
	public int introTextX()			{ return anim.introTextX(); }
	public int diploXOffset()		{ return anim.diploXOffset(); }
	public int diploYOffset()		{ return anim.diploYOffset(); }
	public int flagW()				{ return anim.flagW(); }
	public int flagH()				{ return anim.flagH(); }
	public float diploOpacity()		{ return anim.diploOpacity(); }
	public float diploScale()		{ return anim.diploScale(); }
	public float labFlagX()			{ return anim.labFlagX(); }

	public List<Image> sabotageMissileFrames()		{ return anim.sabotageMissileFrames(); }
	public List<Image> sabotageFactoryFrames()		{ return anim.sabotageFactoryFrames(); }
	public List<Image> sabotageRebellionFrames()	{ return anim.sabotageRebellionFrames(); }
	public RaceCombatAnimation troopNormal()		{ return anim.troopNormal(); }
	public RaceCombatAnimation troopHostile()		{ return anim.troopHostile(); }
	public RaceCombatAnimation troopDeath1()		{ return anim.troopDeath1(); }
	public RaceCombatAnimation troopDeath2()		{ return anim.troopDeath2(); }
	public RaceCombatAnimation troopDeath3()		{ return anim.troopDeath3(); }
	public RaceCombatAnimation troopDeath4()		{ return anim.troopDeath4(); }
	public RaceCombatAnimation troopDeath1H()		{ return anim.troopDeath1H(); }
	public RaceCombatAnimation troopDeath2H()		{ return anim.troopDeath2H(); }
	public RaceCombatAnimation troopDeath3H()		{ return anim.troopDeath3H(); }
	public RaceCombatAnimation troopDeath4H()		{ return anim.troopDeath4H(); }
	public String transportDescKey()				{ return anim.transportDescKey(); }
	public String transportOpenKey()				{ return anim.transportOpenKey(); }
	public int transportW()							{ return anim.transportW(); }
	public int transportYOffset()					{ return anim.transportYOffset(); }
	public int transportDescFrames()				{ return anim.transportDescFrames(); }
	public int transportOpenFrames()				{ return anim.transportOpenFrames(); }
	public int transportLandingFrames()				{ return anim.transportLandingFrames(); }
	public int colonistWalkingFrames()				{ return anim.colonistWalkingFrames(); }
	public int colonistDelay()						{ return anim.colonistDelay(); }
	public int colonistStartX()						{ return anim.colonistStartX(); }
	public int colonistStartY()						{ return anim.colonistStartY(); }
	public int colonistStopX()						{ return anim.colonistStopX(); }
	public int colonistStopY()						{ return anim.colonistStopY(); }
	public BufferedImage transportDescending()		{ return anim.transportDescending(); }
	public BufferedImage advisorScout()				{ return anim.advisorScout(); }
	public BufferedImage advisorTransport()			{ return anim.advisorTransport(); }
	public BufferedImage advisorDiplomacy()			{ return anim.advisorDiplomacy(); }
	public BufferedImage advisorShip()				{ return anim.advisorShip(); }
	public BufferedImage advisorRally()				{ return anim.advisorRally(); }
	public BufferedImage advisorMissile()			{ return anim.advisorMissile(); }
	public BufferedImage advisorWeapon()			{ return anim.advisorWeapon(); }
	public BufferedImage advisorCouncil()			{ return anim.advisorCouncil(); }
	public BufferedImage advisorRebellion()			{ return anim.advisorRebellion(); }
	public BufferedImage advisorCouncilResisted()	{ return anim.advisorCouncilResisted(); }
	public BufferedImage advisorResistCouncil()		{ return anim.advisorResistCouncil(); }
	public BufferedImage soldierQuiet()				{ return anim.soldierQuiet(); }
	public BufferedImage soldierTalking()			{ return anim.soldierTalking(); }
	public BufferedImage spyQuiet()					{ return anim.spyQuiet(); }
	public BufferedImage spyTalking()				{ return anim.spyTalking(); }
	public BufferedImage scientistQuiet()			{ return anim.scientistQuiet(); }
	public BufferedImage scientistTalking()			{ return anim.scientistTalking(); }
	public BufferedImage diplomatQuiet()			{ return anim.diplomatQuiet(); }
	public BufferedImage diplomatTalking()			{ return anim.diplomatTalking(); }
	public BufferedImage diploMugshotQuiet()		{ return anim.diploMugshotQuiet(); }
	public BufferedImage diploMug()			{ return anim.diploMug(); }
	public BufferedImage councilLeader()	{ return anim.councilLeader(); }
	public BufferedImage setupImage()		{ return anim.setupImage(); }
	public BufferedImage shield()			{ return anim.shield(); }
	public BufferedImage laboratory()		{ return anim.laboratory(); }
	public BufferedImage embassy()			{ return anim.embassy(); }
	public BufferedImage holograph()		{ return anim.holograph(); }
	public BufferedImage gnn()				{ return anim.gnn(); }
	public BufferedImage gnnHost()			{ return anim.gnnHost(); }
	public BufferedImage fortress(int i)	{ return anim.fortress(i); }
	public Image flagNorm()					{ return anim.flagNorm(); }
	public Image flagWar()					{ return anim.flagWar(); }
	public Image flagPact()					{ return anim.flagPact(); }
	public Image dialogNorm()				{ return anim.dialogNorm(); }
	public Image dialogWar()				{ return anim.dialogWar(); }
	public Image dialogPact()				{ return anim.dialogPact(); }
	public Image council()					{ return anim.council(); }
	public Image transport()				{ return anim.transport(); }
	public Image gnnEvent(String s)			{ return anim.gnnEvent(s); }
	public Color gnnTextColor()				{ return anim.gnnTextColor(); }

	public void resetScientist()			{ anim.resetScientist(); }
	public void resetSpy()					{ anim.resetSpy(); }
	public void resetDiplomat()				{ anim.resetDiplomat(); }
	public void resetSoldier()				{ anim.resetSoldier(); }
	public void resetGNN(String s)			{ anim.resetGNN(s); }
	public void resetSetupImage()			{ anim.resetSetupImage(); }
	public void resetMugshot()				{ anim.resetMugshot(); }
	public boolean isSpeciesAnim(Species s)	{ return anim == s.anim; }
	public boolean isHostile(PlanetType pt)	{ return anim.isHostile(pt); }
	
	public String diplomacyTheme()			{ return anim.diplomacyTheme(); }
	public String shipAudioKey()			{ return anim.shipAudioKey(); }
	public String ambienceKey()				{ return anim.ambienceKey(); }
	public String dialogue(String key)		{ return anim.dialogue(key); }
	public String raceId()					{ return anim.id; }
	public String lossSplashKey()			{ return anim.lossSplashKey(); }
	public String winSplashKey()			{ return anim.winSplashKey(); }
	public String randomSystemName(Empire e)		{ return anim.randomSystemName(e); } // TODO BR: add option for custom systems
	public String raceText(String key, String... s)	{
		if (anim == null)
			return LabelManager.current().label(key);
		return anim.text(key, s);
	}

	// ====================================================================
	// Purely Skills
	//
	public int homeworldSize()			{ return skills.homeworldSize(); }
	public int randomLeaderAttitude()	{ return skills.randomLeaderAttitude(); }
	public int randomLeaderObjective()	{ return skills.randomLeaderObjective(); }

	public String speciesSkillsName()	{return skills.name(); } // For debug only
	public String skillKey()			{ return skills.id(); }
	public String homeworldPlanetType()	{ return skills.homeworldPlanetType(); }
	String worldsPrefix()				{ return skills.worldsPrefix(); }
	String worldsSuffix()				{ return skills.worldsSuffix(); }
	private String leaderPrefix()		{ return skills.leaderPrefix(); }
	private String leaderSuffix()		{ return skills.leaderSuffix(); }
	public String acceptedPlanetEnvironment()	{ return skills.acceptedPlanetEnvironment(); }

	protected float spyCostMod()		{ return skills.spyCostMod(); }
	public float shipDesignMods(int i)	{ return skills.shipDesignMods(i); }
	public float[] shipDesignMods()		{ return skills.shipDesignMods(); }

	public boolean raceWithUltraPoorHomeworld()	{ return skills.raceWithUltraPoorHomeworld(); }
	public boolean raceWithPoorHomeworld()		{ return skills.raceWithPoorHomeworld(); }
	public boolean raceWithRichHomeworld()		{ return skills.raceWithRichHomeworld(); }
	public boolean raceWithUltraRichHomeworld()	{ return skills.raceWithUltraRichHomeworld(); }
	public boolean raceWithArtifactsHomeworld()	{ return skills.raceWithArtifactsHomeworld(); }
	public boolean raceWithOrionLikeHomeworld()	{ return skills.raceWithOrionLikeHomeworld(); }
	public boolean raceWithHostileHomeworld()	{ return skills.raceWithHostileHomeworld(); }
	public boolean raceWithFertileHomeworld()	{ return skills.raceWithFertileHomeworld(); }
	public boolean raceWithGaiaHomeworld()		{ return skills.raceWithGaiaHomeworld(); }
	public boolean isCustomSpecies()			{ return skills.isCustomSpecies(); }
	public boolean ignoresFactoryRefit()		{ return skills.ignoresFactoryRefit(); }
	public boolean isRandomized()				{ return skills.isRandomized(); }
	public boolean canResearch(Tech t)			{ return t.canBeResearched(this); }
	protected DynOptions speciesOptions()		{ return skills.speciesOptions(); }

	// Modnar added features
	protected float bCBonus()				{ return skills.bCBonus(); }
	public float hPFactor()					{ return skills.hPFactor();  }
	public float maintenanceFactor()		{ return skills.maintenanceFactor(); }
	public float shipSpaceFactor()			{ return skills.shipSpaceFactor(); }
//	public String planetRessource()			{ return skills.planetRessource(); }
//	public String planetEnvironment()		{ return skills.planetEnvironment(); }
	public int preferredShipSize()			{ return skills.preferredShipSize(); }
	public int diplomacyBonus()				{ return skills.diplomacyBonus(); }
	public int robotControlsAdj()			{ return skills.robotControlsAdj(); }
	public float councilBonus()				{ return skills.councilBonus(); }
	public float baseRelations(Species s)	{ return skills.baseRelations(s); }
	public float tradePctBonus()			{ return skills.tradePctBonus(); }
	public float researchBonusPct()			{ return skills.researchBonusPct(); }
	public float researchNoSpyBonusPct()	{ return skills.researchNoSpyBonusPct(); }
//	public float techDiscoveryPct()			{ return skills.techDiscoveryPct(); }
	public float techDiscoveryPct(int i)	{ return skills.techDiscoveryPct(i); }
	public float growthRateMod()			{ return skills.growthRateMod(); }
	protected float workerProductivityMod()	{ return skills.workerProductivityMod(); }
	public float internalSecurityAdj()		{ return skills.internalSecurityAdj(); }
//	float baseRelations(Empire e)			{ return skills.baseRelations(e); }
	public float spyInfiltrationAdj()		{ return skills.spyInfiltrationAdj(); }
	public float techMod(int cat)			{ return skills.techMod(cat); }
	public int groundAttackBonus()			{ return skills.groundAttackBonus(); }
	public int shipAttackBonus()			{ return skills.shipAttackBonus(); }
	public int shipDefenseBonus()			{ return skills.shipDefenseBonus(); }
	public int shipInitiativeBonus()		{ return skills.shipInitiativeBonus(); }
	public boolean ignoresPlanetEnvironment()	{ return skills.ignoresPlanetEnvironment(); }

	public float shipDesignModCost(int size)	{ return skills.shipDesignModCost(size); }
	public float shipDesignModCostMultSmall()	{ return skills.shipDesignModCostMultSmall(); }
	public float shipDesignModCostMultMedium()	{ return skills.shipDesignModCostMultMedium(); }
	public float shipDesignModCostMultLarge()	{ return skills.shipDesignModCostMultLarge(); }
	public float shipDesignModCostMultHuge()	{ return skills.shipDesignModCostMultHuge(); }
	public float shipDesignModModuleSpace()		{ return skills.shipDesignModModuleSpace(); }
	public int shipDesignModShieldWeightFB()	{ return skills.shipDesignModShieldWeightFB(); }
	public int shipDesignModShieldWeightD()		{ return skills.shipDesignModShieldWeightD(); }
	public int shipDesignModEcmWeightFD()		{ return skills.shipDesignModEcmWeightFD(); }
	public int shipDesignModEcmWeightB()		{ return skills.shipDesignModEcmWeightB(); }
	public int shipDesignModManeuverWeightBD()	{ return skills.shipDesignModManeuverWeightBD(); }
	public int shipDesignModManeuverWeightF()	{ return skills.shipDesignModManeuverWeightF(); }
	public int shipDesignModArmorWeightFB()		{ return skills.shipDesignModArmorWeightFB(); }
	public int shipDesignModArmorWeightD()		{ return skills.shipDesignModArmorWeightD(); }
	public int shipDesignModSpecialsWeight()	{ return skills.shipDesignModSpecialsWeight(); }
	public boolean shipDesignModSpeedMatching()		{ return skills.shipDesignModSpeedMatching(); }
	public boolean shipDesignModReinforcedArmor()	{ return skills.shipDesignModReinforcedArmor(); }
	public boolean shipDesignModBioWeapons()		{ return skills.shipDesignModBioWeapons(); }
	public boolean shipDesignModPrefPulsars()		{ return skills.shipDesignModPrefPulsars(); }
	public boolean shipDesignModPrefCloak()			{ return skills.shipDesignModPrefCloak(); }
	public boolean shipDesignModPrefRepair()		{ return skills.shipDesignModPrefRepair(); }
	public boolean shipDesignModPrefInertial()		{ return skills.shipDesignModPrefInertial(); }
	public boolean shipDesignModPrefMissShield()	{ return skills.shipDesignModPrefMissShield(); }
	public boolean shipDesignModPrefRepulsor()		{ return skills.shipDesignModPrefRepulsor(); }
	public boolean shipDesignModPrefStasis()		{ return skills.shipDesignModPrefStasis(); }
	public boolean shipDesignModPrefStreamProj()	{ return skills.shipDesignModPrefStreamProj(); }
	public boolean shipDesignModPrefWarpDissip()	{ return skills.shipDesignModPrefWarpDissip(); }
	public boolean shipDesignModPrefTechNullif()	{ return skills.shipDesignModPrefTechNullif(); }
	public boolean shipDesignModPrefBeamFocus()		{ return skills.shipDesignModPrefBeamFocus(); }

	// ====================================================================
	// Depend on custom Species
	//
	public String setupName()	{
		if (isCustomSpecies()) {
			String name = skills.setupName();
			if (name.isEmpty())
				return skills.setupName;
			return name;
		}
		return anim.setupName();
	}
	public String raceType()	{
		if (isCustomSpecies()) {
			String name = skills.setupName();
			if (name.isEmpty())
				return skills.setupName;
			return name;
		}
		else
			return skills.nameVariant(0);
	}
	private String title(boolean isPlayer)	{
		if (isPlayer && isCustomSpecies()) {
			String title = skills.title();
			if (title != null && !title.isEmpty())
				return title;
		}
		return anim.title();
	}
	private String fullTitle(boolean isPlayer)	{
		if (isPlayer && isCustomSpecies()) {
			String title = skills.fullTitle();
			if (title != null && !title.isEmpty())
				return title;
		}
		return anim.fullTitle();
	}
	private List<String> introduction(boolean isPlayer)	{
		if (isPlayer && isCustomSpecies()) {
			List<String> intro = skills.customIntroduction();
			if (intro != null && !intro.isEmpty())
				return intro;
		}
		return anim.introduction();
	}
	public String defaultHomeworldName()		{
		if (!skills.isCustomSpecies())
			return anim.defaultHomeworldName();
		String s = skills.defaultHomeworldName();
		if (s==null || s.isEmpty())
			return anim.defaultHomeworldName();
		return s;
	}
	protected String nextAvailableLeaderExt()	{
		String name;
		do {
			name = nextAvailableLeader();
			if (name.isEmpty()) {
				name = anim.leaderNames().getFirst();
				name = leaderPrefix() + name + name + leaderSuffix();
				System.err.println("Error: leader list is Empty -> " + name);
				return name;
			}
			else
				name = leaderPrefix() + name + leaderSuffix();
			// System.out.println("Next leader name = " + name);
		}
		while(usedLeaderNames().contains(name));
		return name;
	}
	private String nextAvailableLeader()	{
		if (!skills.isCustomSpecies())
			return anim.nextAvailableLeader();
		String s = skills.nextAvailableLeader();
		if (s==null || s.isEmpty())
			return anim.nextAvailableLeader();
		return s;
	}
	public String preferredShipSet()		{
		String ShipSet = skills.preferredShipSet();
		if (ShipSet.equalsIgnoreCase(DISPLAY_RACE_SET)) {
			if (!skills.isCustomSpecies())
				return anim.preferredShipSet();
			String s = skills.preferredShipSet();
			if (s==null || s.isEmpty())
				return anim.preferredShipSet();
			return s;
		}
		return ShipSet;
	}
	public String getDescription(int i)		{
		if (!skills.isCustomSpecies())
			return anim.getDescription(i);
		String s = skills.getDescription(i);
		if (s==null || s.isEmpty())
			return anim.getDescription(i);
		return s;
	}
	public String randomLeaderName()		{
		if (!skills.isCustomSpecies())
			return anim.randomLeaderName();
		// Random Species check if has names
		String s = skills.randomLeaderName();
		if (s==null || s.isEmpty())
			return anim.randomLeaderName();
		return s;
	}
	public List<String> shipNames(int size)	{
		if (!skills.isCustomSpecies())
			return anim.shipNames(size);
		List<String> list = new ArrayList<>(skills.shipNames(size));
		// The lab takes the first unused name
		// ... OK for anim, but customs ones will be randomized
		shuffle(list);
		// In case there is not enough customized names
		list.addAll(anim.shipNames(size));
		return list;
	}
	public List<String> systemNames()		{
		if (!skills.isCustomSpecies())
			return anim.systemNames();
		List<String> list = skills.systemNames();
		if (list==null || list.isEmpty())
			return anim.systemNames();
		return list;
	}
	public class CivilizationId implements Serializable	{
		private static final long serialVersionUID = 1L;
		private String civName;
		private String speciesName;
//		private String languageDir;
		private Integer index;
		private boolean isSkillsNames = false;

		private CivilizationId(String civName, String speciesName, Integer civIndex, String langDir, boolean isSkillsNames)	{
			isSkillsNames(isSkillsNames);
//			setLangDir(langDir);
			setIndex(civIndex);
			setCivName(civName);
			setSpeciesName(speciesName);
		}
		private CivilizationId(CivilizationRecord civ, String langDir, boolean isSkillsNames)	{
			isSkillsNames(isSkillsNames);
//			setLangDir(langDir);
			setIndex(civ.civIndex);
			setCivName(civ.civName);
			setSpeciesName(civ.speciesName);
		}
		private CivilizationId(String name, Integer index)	{
			setCivName(name);
			setIndex(index);
		}
		@Override public String toString()			{
			String s = "";
			s += String.format("%-26s", "civName: "	+ civName);
			s += String.format("%-31s", " speciesName: " + speciesName);
			s += String.format("%-10s", " index: " + index);
			s += String.format("%-21s", " isSkillsNames: " + isSkillsNames);
			return s;
		}
		private void setCivName(String str)			{ civName = str; }
		private String getCivName()					{ return civName; }
		private void setSpeciesName(String str)		{ speciesName = str; }
//		private String getSpeciesName()				{ return speciesName; }
//		private void setLangDir(String dir)			{ languageDir = dir; }
//		private String getLangDir()					{ return languageDir; }
		private void setIndex(Integer id)			{ index = id; }
		private Integer getIndex()					{ return index; }
		private void isSkillsNames(boolean is)		{ isSkillsNames = is; }
		private boolean isSkillsNames()				{ return isSkillsNames; }
	}
}

