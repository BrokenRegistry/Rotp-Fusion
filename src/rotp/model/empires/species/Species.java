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
import rotp.model.empires.Leader;
import rotp.model.empires.RaceCombatAnimation;
import rotp.model.empires.SystemInfo;
import rotp.model.empires.species.SkillsFactory.CivilizationRecord;
import rotp.model.game.DynOptions;
import rotp.model.planet.PlanetType;
import rotp.model.tech.Tech;
import rotp.ui.util.StringList;
import rotp.util.Base;

public class Species implements ISpecies, Base, Serializable {
	private static final long serialVersionUID = 1L;

	// ====================================================================
	// #=== Species Management
	// ====================================================================
	private static final Map<String, Race> INTERNAL_SPECIES_MAP = new HashMap<>();
	private static final Map<String, String> INTERNAL_NAMES_MAP = new HashMap<>();
	private static final Map<String, String> LANGUAGE_NAMES_MAP = new HashMap<>(); // contains English and loaded languages

	static Race getAnim(String key)			{ return INTERNAL_SPECIES_MAP.get(key); }
	static boolean isValidKey(String s)		{ return INTERNAL_SPECIES_MAP.get(s) != null; }
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
	public static String languageToKey (String s)	{ return LANGUAGE_NAMES_MAP.get(s); }
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
	public static void cleanUsedNames()	{
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
	private static final String CUSTOM_RACE_DESCRIPTION	= "CUSTOM_RACE_DESCRIPTION";

	private transient Race anim;
	private transient SpeciesSkills skills;
	transient String initialHomeWorld;
	transient String initialLeaderName;
	transient int colorId = -1;
//	private transient Integer civilizationIndex;
//	private transient String civilizationName; // for debug
//	private Boolean isCustomNames;
//	private Boolean isReworked;
	private CivilizationId civilizationId;
//	private CivilizationName leaderName;

	private CivilizationId civilizationId()	{
		if (civilizationId == null)
			civilizationId = new CivilizationId();
		return civilizationId;
	}
	protected String getLeaderName()	{ return initialLeaderName; }
	protected String getHomeWorldName()	{ return initialHomeWorld; }
	protected int colorId()				{ return colorId; }
//	String nextAvailableCivilization()	{ // TODO BR: nextAvailableCivilization
//		if (this.isCustomSpecies()) {
//			
//		}
//		if (remainingCivilizationNames()==null) 
//			loadNameList();
//		String name = remainingCivilizationNames().remove(0);
//		return name;
//	}
	@Override public String toString()	{
		String s = "Anim: " + animKey();
		s += " Skills: " + skillKey();
		s += " Custom: " + isCustomSpecies();
		if (civilizationId != null) {
			s += " Civ Name: " + civilizationId.getName() + " Civ idx: " ;
			s += " Civ idx: " + civilizationId.getIndex();
			if (civilizationId.isFromAnimation)
				s += " from Anim ";
			else
				s += " from Skills ";
		}
		s += " Leader: " + getLeaderName();
		s += " Home: " + getHomeWorldName();
		s += " ColorId: " + colorId();
		return s;
	}
	// ====================================================================
	// Constructors
	//
	protected Species(Species src)	{
		setSpecies(src);
		civilizationIndex();
	}
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
		anim = getAnim(key);
		if (anim == null) { // Add custom race if missing
			skills = SkillsFactory.keyToCustomSpecies(key);
			skills.isCustomSpecies(true);
			skills.setDescription4(skills.text(CUSTOM_RACE_DESCRIPTION));
		}
		else
			skills = anim;
	}
	// ====================================================================
	// Initializers
	//
	protected void setSpecies(Species src)	{
		anim = src.anim;
		skills = src.skills;
	}
	public void setNewSpeciesAnim(String animKey)	{ anim = getAnim(animKey); }
	public void setAllCustomNames(CivilizationRecord civ, String langDir)	{
		civilizationId = new CivilizationId(civ, langDir, true);
		initialHomeWorld = civ.homeWorld;
		initialLeaderName = civ.leaderName;
	}
	public void lockUsedNames()	{
		usedCivilizationNames().add(civilizationId.civName);
		usedLeaderNames().add(initialLeaderName);
		usedCivilizationNames().add(initialHomeWorld);
	}
	public void setAllAnimNames(CivilizationRecord civ, String langDir)	{
		civilizationId = new CivilizationId(civ, langDir, false);
		initialHomeWorld = civ.homeWorld;
		initialLeaderName = civ.leaderName;
	}
	public SpeciesSkills setSpeciesSkills(String skillsKey)	{
		skills = getAnim(skillsKey);
		if (skills == null) {
			skills = SkillsFactory.keyToCustomSpecies(skillsKey);
			skills.isCustomSpecies(true);
			skills.setDescription4(skills.text(CUSTOM_RACE_DESCRIPTION));
		}
		return skills;
	}
	public SpeciesSkills setSpeciesSkills(String skillsKey, DynOptions options)	{
		if (options == null)
			return setSpeciesSkills(skillsKey);
		skills = SkillsFactory.optionToSkills(options);
		skills.isCustomSpecies(true);
		return skills;
	}
	public void setSpeciesSkills(SpeciesSkills speciesSkills)	{ skills = speciesSkills; }
	public void setOldSpeciesIndex(int id)		{ civilizationId().setIndex(id); }
//	public void setOldSpeciesIndex(int id)		{ civilizationIndex = id; }
	SpeciesSkills getSkillCopy()			{ return skills.copy(); }

	// ====================================================================
	// To be overridden
	//
	protected boolean isPlayer()			{ return true; } // TODO BR: Check if used
	protected int civilizationNameIndex()	{ return civilizationIndex(); } // TODO BR: Check if used
	protected int capitalSysId()			{ return 0; }
	protected SystemInfo sv()				{ return null; }
	protected Leader leader()				{ return null; }

	// ====================================================================
	// class tools
	//
	private String raceName(int i)		{ // TODO BR: update... Civilization Name
		String rn;
		List<String> names = substrings(unparsedRaceName(), '|');
		if (i >= names.size() || names.get(i).isEmpty())
			rn = names.get(0);
		else
			rn = names.get(i);
		return speciesPrefix() + rn + speciesSuffix(); // BR: for custom Races
	}
	private String unparsedRaceName()	{ // TODO BR: wrong: update
		if (isCustomPlayer())
			return skills.setupName;
		return anim.nameVariant(civilizationNameIndex());
	}
	protected String civilizationName()	{ return civilizationId().getName(); }
	protected int civilizationIndex()	{ return civilizationId().getIndex(); }
	public String label(String token) {
		List<String> values = substrings(anim.text(token), ',');
		return civilizationNameIndex() < values.size() ? values.get(civilizationNameIndex()) : values.get(0);      
	}
	// Species Methods
	public String replaceTokens(String s, String key)	{ // TODO BR: update
		if (key.equals("player")) // BR: many confusion in translations
			s = replaceTokens(s, "my");
		List<String> tokens = this.varTokens(s, key);
		String s1 = s;
		for (String token: tokens) {
			String replString = concat("[",key, token,"]");
			// leader name is special case, not in dictionary
			if (token.equals("_name")) 
				s1 = s1.replace(replString, leader().name());
			else if (token.equals("_home"))
				s1 = s1.replace(replString, sv().name(capitalSysId()));
			else if (isCustomPlayer() && !isRandomized() && token.equals("_race"))
				s1 = s1.replace(replString, setupName());
			else if (isCustomPlayer() && !isRandomized() && token.equals("_empire"))
				s1 = s1.replace(replString, empireTitle());
			else {
				List<String> values = substrings(anim.text(token), ',');
				String value = civilizationNameIndex() < values.size() ? values.get(civilizationNameIndex()) : values.get(0);
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
		if (isCustomPlayer())
			return skills.setupName;
		if (sv()==null)
			return "Orion";
		return raceName(0);
	}

	public boolean isValid()	{ return anim != null && skills != null; }
	public String id()			{ return anim.id() + " / " + skills.id(); }

	public String title()		{ return title(isPlayer()); }
	public String fullTitle()	{ return fullTitle(isPlayer()); }
	public List<String> introduction()	{ return introduction(isPlayer()); }
	public boolean isCustomPlayer()		{ return skills.isCustomSpecies() && isPlayer(); }
	public String isAnimAutonomous()	{ return skills.isAnimAutonomous(); }
	public void initCRToShow(SkillsFactory cr)	{ cr.setFromRaceToShow(skills);}
	// ====================================================================
	// Purely Animations
	//
	protected String animKey()		{ return anim.id(); }
	public String speciesName()		{return anim.name(); }
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
	public String randomSystemName(Empire e)	{ return anim.randomSystemName(e); } // TODO BR: add option for custom systems
	public String raceText(String key, String... s)	{ return anim.text(key, s); }

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
	private String speciesPrefix()		{ return skills.speciesPrefix(); }
	private String speciesSuffix()		{ return skills.speciesSuffix(); }
	private String leaderPrefix()		{ return skills.leaderPrefix(); }
	private String leaderSuffix()		{ return skills.leaderSuffix(); }
	protected String empireTitle()		{ return skills.empireTitle(); }
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
	public String planetRessource()			{ return skills.planetRessource(); }
	public String planetEnvironment()		{ return skills.planetEnvironment(); }
	public int preferredShipSize()			{ return skills.preferredShipSize(); }
	public int diplomacyBonus()				{ return skills.diplomacyBonus(); }
	public int robotControlsAdj()			{ return skills.robotControlsAdj(); }
	public float councilBonus()				{ return skills.councilBonus(); }
	public float baseRelations(Species s)	{ return skills.baseRelations(s); }
	public float tradePctBonus()			{ return skills.tradePctBonus(); }
	public float researchBonusPct()			{ return skills.researchBonusPct(); }
	public float researchNoSpyBonusPct()	{ return skills.researchNoSpyBonusPct(); }
	public float techDiscoveryPct()			{ return skills.techDiscoveryPct(); }
	public float techDiscoveryPct(int i)	{ return skills.techDiscoveryPct(i); }
	public float growthRateMod()			{ return skills.growthRateMod(); }
	protected float workerProductivityMod()	{ return skills.workerProductivityMod(); }
	public float internalSecurityAdj()		{ return skills.internalSecurityAdj(); }
	float baseRelations(Empire e)			{ return skills.baseRelations(e); }
	public float spyInfiltrationAdj()		{ return skills.spyInfiltrationAdj(); }
	public float techMod(int cat)			{ return skills.techMod(cat); }
	public int groundAttackBonus()			{ return skills.groundAttackBonus(); }
	public int shipAttackBonus()			{ return skills.shipAttackBonus(); }
	public int shipDefenseBonus()			{ return skills.shipDefenseBonus(); }
	public int shipInitiativeBonus()		{ return skills.shipInitiativeBonus(); }
	public boolean ignoresPlanetEnvironment()	{ return skills.ignoresPlanetEnvironment(); }
	
	// ====================================================================
	// Depend on custom Species
	//
	public String setupName()	{
		if (isCustomSpecies())
			return skills.setupName;
		return anim.setupName();
	}
	public String raceType()	{
		if (isCustomSpecies())
			return skills.setupName;
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
	public String nextAvailableHomeworldExt()	{
		String name;
		do {
			name = nextAvailableHomeworld();
			if (name.isEmpty()) {
				// Then last chance, get one from the star System List
				StringList systemNames = anim.masterSystemList(this);
				systemNames.removeAll(usedHomeNames());
				return systemNames.getFirst();
			}
			else
				name = skills.worldsPrefix() + name + skills.worldsSuffix();
			// System.out.println("Next home world name = " + name);
		}
		while(usedHomeNames().contains(name));
		usedHomeNames().add(name);
		return name;
	}
	public String nextAvailableHomeworld()		{ // TODO BR: make private
		if (!skills.isCustomSpecies())
			return anim.nextAvailableHomeworld();
		String s = skills.nextAvailableHomeworld();
		if (s==null || s.isEmpty())
			return anim.nextAvailableHomeworld();
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
	private String nextAvailableNameExt()	{
		String name;
		do {
			name = nextAvailableName();
			if (name.isEmpty()) {
				// Should never happen!
				name = anim.civilizationNames().getFirst();
				name = name + name;
				System.err.println("Error: species list is Empty -> " + name);
				return name;
			}
		}
		while(usedCivilizationNames().contains(name));
		return name;

	}
	private String nextAvailableName()		{
		// To be implemented later
		// 50 species and player Anim != Skill => one opponent will have to be mixed too !!!
//		if (skills.isCustomSpecies()) {
//			speciesName = skills.nextAvailableName();
//			if (speciesName==null || speciesName.isEmpty()) {
//				speciesName  = anim.nextAvailableName();
//				speciesIndex = anim.nameIndex(speciesName);
//			}
//			else
//				speciesIndex = skills.nameIndex(speciesName);
//		}
//		else {
//		speciesName = anim.nextAvailableName();
//		speciesIndex = anim.nameIndex(speciesName);
		civilizationId().setName(anim.nextAvailableName()); // TODO BR: select the right source
		civilizationId().setIndex(anim.nameIndex(civilizationId.getName()));
//			civilizationIndex = anim.nameIndex(civilizationName);
//		}
		return civilizationId().getName();
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
		String s = skills.randomLeaderName();
		if (s==null || s.isEmpty())
			return anim.randomLeaderName();
		return s;
	}
	public List<String> shipNames(int size)	{
		if (!skills.isCustomSpecies())
			return anim.shipNames(size);
		List<String> list = skills.shipNames(size);
		if (list==null || list.isEmpty())
			return anim.shipNames(size);
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
	class CivilizationId implements Serializable	{
		private static final long serialVersionUID = 1L;
		private String civName;
		private String languageDir;
		private Integer index;
		private boolean isFromAnimation = false;
		private boolean isCustom = false;
		CivilizationId(CivilizationRecord civ, String langDir, boolean custom)	{ // TODO BR:
			isCustom = custom;
			isFromAnimation = !isCustom;
			languageDir = langDir;
			index = civ.civIndex;
			civName = civ.civName;
		}
		CivilizationId(String name, Integer index)	{
			civName = name;
			this.index = index;
		}
		CivilizationId()	{ init(); }
		private void init()	{
			do {
				getAvailableName();
			}
			while(!civName.isEmpty() && usedCivilizationNames().contains(civName));
		}
		private void getAvailableName()	{
			index = null;
			if (skills.isCustomSpecies()) {
				civName = skills.nextAvailableName();
				if (civName!=null && !civName.isEmpty()) {
					index = anim.nameIndex(civName);
					isCustom = true;
					isFromAnimation = false;
					return;
				}
			}
			// Then get from animations
			civName = anim.nextAvailableName();
			if (civName!=null && !civName.isEmpty()) {
				index = anim.nameIndex(civName);
				isCustom = false;
				isFromAnimation = true;
			}
			else {
				isCustom = false;
				isFromAnimation = false;
			}
			return;
		}
		void setName(String str)			{ civName = str; }
		String getName()					{ return civName; }
		void setLangDir(String dir)			{ languageDir = dir; }
		String getLangDir()					{ return languageDir; }
		void setIndex(Integer id)			{ index = id; }
		Integer getIndex()					{ return index; }
		void isFromAnimation(boolean is)	{ isFromAnimation = is; }
		boolean isFromAnimation()			{ return isFromAnimation; }
		void isCustom(boolean is)			{ isCustom = is; }
		boolean isCustom()					{ return isCustom; }
		boolean isEmpty()					{ return index == null; }

//		void changeCivilizationName(BaseModPanel frameComp, Component locationComp)	{
//			// Not a good Idea to use this, as all the dialogs will be wrong
//			softClick();
//			String dlgTitle = text("RACES_DIPLOMACY_CIVILIZATION_TITLE", name);
//			String dlgLabel = text("RACES_DIPLOMACY_CIVILIZATION_LABEL", name);
//			StringDialogUI dlg = RotPUI.instance().stringDialog();
//			dlg.init(frameComp, locationComp, dlgLabel, dlgTitle, name, name,
//					-1, -1, scaled(350), -1, null, null, null);
//			String newName = dlg.showDialog(0);
//			if (!newName.equals(name)) {
//				isAutoUpgradable(false);
//				name = newName;
//			}
//		}
	}
}

