package rotp.model.game;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.Rotp;
import rotp.model.planet.Planet;
import rotp.ui.RotPUI;
import rotp.ui.game.BaseModPanel;
import rotp.ui.util.LinkData;
import rotp.ui.util.LinkValue;
import rotp.ui.util.ParamAAN2;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamFloat;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;
import rotp.ui.util.ParamTech;
import rotp.ui.util.RandomAlienRaces;

public interface IPreGameOptions extends IAdvOptions, IIronmanOptions, ISystemsOptions {

	// ========================================================================
	// Factory options
	ParamAAN2 orionLikeHomeworld		= new ParamAAN2("HOME_ORION");
	default ParamAAN2 selectedOrionLikeHomeworld()	{ return orionLikeHomeworld; }
	ParamAAN2 artifactsHomeworld		= new ParamAAN2("HOME_ARTIFACT");
	default ParamAAN2 selectedArtifactsHomeworld()	{ return artifactsHomeworld; }
	ParamAAN2 fertileHomeworld			= new ParamAAN2("HOME_FERTILE");
	default ParamAAN2 selectedFertileHomeworld()	{  return fertileHomeworld; }
	ParamAAN2 gaiaHomeworld				= new ParamAAN2("HOME_GAIA");
	default ParamAAN2 selectedGaiaHomeworld()		{  return gaiaHomeworld; }
	ParamAAN2 richHomeworld				= new ParamAAN2("HOME_RICH");
	default ParamAAN2 selectedRichHomeworld()		{ return richHomeworld; }
	ParamAAN2 ultraRichHomeworld		= new ParamAAN2("HOME_ULTRA_RICH");
	default ParamAAN2 selectedUltraRichHomeworld()	{ return ultraRichHomeworld; }

	ParamFloat minDistArtifactPlanet	= new ParamFloat(MOD_UI, "DIST_ARTIFACT_PLANET", 0.0f)
			.setLimits(0.0f, null)
			.setIncrements(0.2f, 1f, 5f)
			.cfgFormat("0.##")
			.guiFormat("0.0");
	default float selectedMinDistArtifactPlanet() { return minDistArtifactPlanet.get(); }

	ParamBoolean battleScout		= new ParamBoolean(MOD_UI, "BATTLE_SCOUT", false);
	default boolean selectedBattleScout()		{ return battleScout.get(); }

	ParamBoolean randomTechStart	= new ParamBoolean(MOD_UI, "RANDOM_TECH_START", false);
	default boolean selectedRandomTechStart()	{ return randomTechStart.get(); }

	ParamInteger companionWorlds	= new ParamInteger(MOD_UI, "COMPANION_WORLDS" , 0)
			.setLimits(-4, 6)
			.setIncrements(1, 1, 1)
			.loop(true);
	default int selectedCompanionWorlds() 		{ return Math.abs(companionWorlds.get()); }
	default int signedCompanionWorlds() 		{ return companionWorlds.get(); }

	ParamInteger empiresSpreadingFactor	= new ParamInteger(MOD_UI, "EMPIRES_SPREADING_FACTOR", 100)
			.setDefaultValue(MOO1_DEFAULT, 125)
			.setLimits(10, 1000)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default int		selectedEmpireSpreadingPct()	{ return empiresSpreadingFactor.get(); }
	default float	selectedEmpireSpreadingFactor()	{ return 0.01f * empiresSpreadingFactor.get(); }
	default boolean	isCustomEmpireSpreadingFactor()	{ return !empiresSpreadingFactor.isDefaultValue(); }
	default void	resetEmpireSpreadingFactor()	{ empiresSpreadingFactor.setFromDefault(false, false); }
	default void	toggleEmpireSpreadingFactor(MouseWheelEvent e)	{ empiresSpreadingFactor.toggle(e); }
	default String	empireSpreadingFactorMapKey()	{ return MOD_UI + "EMPIRES_SPREADING_FACTOR_MAP"; }
	default ParamInteger getEmpiresSpreadingFactor()	{ return empiresSpreadingFactor; }

	ParamInteger randomNumStarsLim1		= new ParamInteger (MOD_UI, "RANDOM_NUM_STARS_LIM1", 50)
			.setLimits(10, Rotp.maximumSystems-1)
			.setIncrements(1, 5, 20);
	ParamInteger randomNumStarsLim2		= new ParamInteger (MOD_UI, "RANDOM_NUM_STARS_LIM2", 250)
			.setLimits(10, Rotp.maximumSystems-1)
			.setIncrements(1, 5, 20);
	ParamInteger randomNumAliensLim1	= new ParamInteger (MOD_UI, "RANDOM_NUM_ALIENS_LIM1", 4)
			.setLimits(0, Rotp.maximumSystems-1)
			.setIncrements(1, 5, 20);
	ParamInteger randomNumAliensLim2	= new ParamInteger (MOD_UI, "RANDOM_NUM_ALIENS_LIM2", 20)
			.setLimits(0, Rotp.maximumSystems-1)
			.setIncrements(1, 5, 20);

	ParamInteger minStarsPerEmpire	= new MinStarsPerEmpire();
	class MinStarsPerEmpire extends ParamInteger {
		MinStarsPerEmpire() {
			super(MOD_UI, "MIN_STARS_PER_EMPIRE", 3);
			setDefaultValue(MOO1_DEFAULT, 4);
			setDefaultValue(ROTP_DEFAULT, 8);
			setLimits(1, Rotp.maximumSystems-1);
			setIncrements(1, 5, 20);
		}
		@Override public Integer dynMinValue()	{
			int min = minValue();
			int otherMin = secondRingSystemNumber.getValidMin()+1;
			min = Math.max(min, otherMin);
			return min;
		}
	}
	default int selectedMinStarsPerEmpire()		{ return minStarsPerEmpire.getValidValue(); }

	ParamInteger prefStarsPerEmpire	= new PrefStarsPerEmpire();
	class PrefStarsPerEmpire extends ParamInteger {
		PrefStarsPerEmpire() {
			super(MOD_UI, "PREF_STARS_PER_EMPIRE", 10);
			setDefaultValue(MOO1_DEFAULT, 5);
			setDefaultValue(ROTP_DEFAULT, 16);
			setLimits(3, Rotp.maximumSystems-1);
			setIncrements(1, 10, 100);
		}
		@Override public Integer dynMinValue()	{
			int min = minValue();
			int otherMin = secondRingSystemNumber.getValidMin()+1;
			min = Math.max(min, otherMin);
			return min;
		}
	}
	default int selectedPrefStarsPerEmpire()	{ return prefStarsPerEmpire.getValidValue(); }

	ParamInteger dynStarsPerEmpire	= new DynStarsPerEmpire();
	class DynStarsPerEmpire extends ParamInteger {
		DynStarsPerEmpire() {
			super(MOD_UI, "DYN_STARS_PER_EMPIRE", 10);
			setLimits(1, Rotp.maximumSystems-1);
			setIncrements(1, 10, 100);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				resetLinks();
				//addLink(secondRingSystemNumber, DO_FOLLOW, GO_DOWN, GO_DOWN, "Ring 2");
				addLink(secondRingSystemNumber, DO_LOCK, GO_DOWN, GO_DOWN, "Ring 2");
			}
			else
				super.initDependencies(level);
		}
		@Override protected void convertValueToLink(LinkData rec)	{
			// Convert the current state
			switch (rec.key) {
				case "Ring 2":
					rec.aimValue = new LinkValue((rec.srcValue.intValue()-1));
					return;
				default:
					super.convertValueToLink(rec);
			}
		}
		@Override public Integer defaultValue() {
			return prefStarsPerEmpire.get();
		}
		@Override public Integer set(Integer value)	{
			super.set(value);
			if (Rotp.initialized())
				RotPUI.setupGalaxyUI().postGalaxySizeSelection(false);
			return value;
		}
		@Override public boolean prev() {
			super.prev();
			RotPUI.setupGalaxyUI().postGalaxySizeSelection(true);
			return false;
		}
		@Override public boolean next() {
			super.next();
			RotPUI.setupGalaxyUI().postGalaxySizeSelection(true);
			return false;
		}
		@Override public Integer dynMinValue()	{
			int min = minValue();
			int otherMin = secondRingSystemNumber.getValidMin()+1;
			min = Math.max(min, otherMin);
			return min;
		}
		
		@Override public boolean isValidValue()	{ return isValidDoubleCheck(); }
		@Override public boolean toggle(MouseEvent e, MouseWheelEvent w, BaseModPanel frame) {
			boolean forceUpdate = super.toggle(e, w, frame);
			Integer value = get();
			int min	= dynMinValue();
			if (value < min)
				set(min);
			return forceUpdate;
		}
		@Override public String getGuiDisplay(int idx)	{
			if (!IGalaxyOptions.sizeSelection.get().equals(IGalaxyOptions.SIZE_DYNAMIC))
				return "---";
			return super.getGuiDisplay(idx);
		}
	}
	default int selectedDynStarsPerEmpire()	 { return Math.abs(dynStarsPerEmpire.getValidValue()); }
	default ParamInteger dynStarsPerEmpire() { return dynStarsPerEmpire; }

	// Restart Always looks for setup options!
	ParamBoolean restartChangesAliensAI		= new ParamBoolean( MOD_UI, "RESTART_CHANGES_ALIENS_AI", false);
	default boolean selectedRestartChangesAliensAI()	{ return restartChangesAliensAI.get(); }
	ParamBoolean restartChangesPlayerAI		= new ParamBoolean( MOD_UI, "RESTART_CHANGES_PLAYER_AI", false);
	default boolean selectedRestartChangesPlayerAI()	{ return restartChangesPlayerAI.get(); }
	ParamBoolean restartAppliesSettings		= new ParamBoolean( MOD_UI, "RESTART_APPLY_SETTINGS",false);
	default boolean selectedRestartAppliesSettings()	{ return restartAppliesSettings.get(); }
	ParamList    restartChangesPlayerRace	= new ParamList( MOD_UI, "RESTART_PLAYER_RACE", "Swap")
		.showFullGuide(true)
		.put("Last", 	MOD_UI + "RESTART_PLAYER_RACE_LAST")
		.put("Swap",	MOD_UI + "RESTART_PLAYER_RACE_SWAP")
		.put("GuiSwap",	MOD_UI + "RESTART_PLAYER_RACE_GUI_SWAP")
		.put("GuiLast",	MOD_UI + "RESTART_PLAYER_RACE_GUI_LAST");
	default String selectedRestartChangesPlayerRace()	{ return restartChangesPlayerRace.get(); }

	ParamTech techIrradiated	= new ParamTech("TECH_IRRADIATED",	3, "ControlEnvironment",6); // level 18
	ParamTech techCloaking		= new ParamTech("TECH_CLOAKING",	2, "Cloaking",			0); // level 27
	ParamTech techStargate		= new ParamTech("TECH_STARGATES",	4, "Stargate", 			0); // level 27
	ParamTech techHyperspace	= new ParamTech("TECH_HYPERSPACE",	0, "HyperspaceComm",	0); // level 34
	ParamTech techIndustry2		= new ParamTech("TECH_INDUSTRY_2",	1, "ImprovedIndustrial",7); // level 38
	ParamTech techThorium		= new ParamTech("TECH_THORIUM",		4, "FuelRange",			8); // level 41
	ParamTech techTransport		= new ParamTech("TECH_TRANSPORTERS",4, "CombatTransporter",	0); // level 45
	ParamTech techCloning		= new ParamTech("TECH_CLONING",		3, "Cloning",			0); // level 21
	ParamTech techAtmospheric	= new ParamTech("TECH_ATMOSPHERIC",	3, "AtmosphereEnrichment",	0); // level 22
	ParamTech techGaia			= new ParamTech("TECH_GAIA",		3, "SoilEnrichment",	1); // level 30
	List<ParamTech> techModList		= new ArrayList<>(Arrays.asList(
			techIrradiated, techCloning, techAtmospheric,
			techCloaking, techStargate, techGaia, techHyperspace,
			techIndustry2, techThorium, techTransport
			));
	default List<ParamTech> techModList()	{ return techModList; }
	default List<String> forbiddenTechList(boolean isPlayer)	{
		List<String> list = new ArrayList<>();
		for (ParamTech  tech : techModList) {
			if (tech.isNever(isPlayer)) {
				list.add(tech.techId());
			}
		}
		return list;
	}
	default boolean isForbiddenTech(String id, boolean isPlayer)	{
		boolean forbidden = false;
		for (ParamTech  tech : techModList) {
			if (tech.isNever(isPlayer) && tech.techId().equalsIgnoreCase(id)) {
				return true;
			}
		}
		return forbidden;
	}

	ParamInteger randomAlienRacesMin		 = new ParamInteger(MOD_UI, "RACES_RAND_MIN", -25)
			.setLimits(-100, 100)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default int randomAlienRacesMin()			{ return randomAlienRacesMin.get(); }
	default void randomAlienRacesMin(int val)	{ randomAlienRacesMin.set(val); }

	ParamInteger randomAlienRacesMax		 = new ParamInteger(MOD_UI, "RACES_RAND_MAX", 25)
			.setLimits(-100, 100)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default int randomAlienRacesMax()			{ return randomAlienRacesMax.get(); }
	default void randomAlienRacesMax(int val)	{ randomAlienRacesMax.set(val); }

	ParamInteger randomAlienRacesTargetMax	 = new ParamInteger(MOD_UI, "RACES_RAND_TARGET_MAX", 75)
			.setLimits(null, null)
			.setIncrements(1, 10, 100);
	default int randomAlienRacesTargetMax()			{ return randomAlienRacesTargetMax.get(); }
	default void randomAlienRacesTargetMax(int val)	{ randomAlienRacesTargetMax.set(val); }

	ParamInteger randomAlienRacesTargetMin	 = new ParamInteger(MOD_UI, "RACES_RAND_TARGET_MIN", 0)
			.setLimits(null, null)
			.setIncrements(1, 10, 100);
	default int randomAlienRacesTargetMin()			{ return randomAlienRacesTargetMin.get(); }
	default void randomAlienRacesTargetMin(int val)	{ randomAlienRacesTargetMin.set(val); }

	ParamBoolean randomAlienRacesSmoothEdges = new ParamBoolean(MOD_UI, "RACES_RAND_EDGES", true);
	default boolean randomAlienRacesSmoothEdges()		{ return randomAlienRacesSmoothEdges.get(); }
	default void randomAlienRacesSmoothEdges(boolean b)	{ randomAlienRacesSmoothEdges.set(b); }

	RandomAlienRaces randomAlienRaces		 = new RandomAlienRaces(MOD_UI, "RACES_ARE_RANDOM", RandomAlienRaces.TARGET);
	default String selectedRandomAlienRaces()			{ return randomAlienRaces.get(); }
	default boolean randomAlienRacesUseTarget()			{ return randomAlienRaces.isTarget(); }
	default void randomAlienRacesUseTarget(boolean b)	{ randomAlienRaces.isTarget(b); }

	ParamList    guardianMonsters			 = new ParamList( MOD_UI, "GUARDIAN_MONSTERS", "None")
		.showFullGuide(true)
		.put("None", 	MOD_UI + "GUARDIAN_MONSTERS_NONE")
//		.put("Rich",		MOD_UI + "GUARDIAN_MONSTERS_RICH")
//		.put("Artefact",	MOD_UI + "GUARDIAN_MONSTERS_RUIN")
		.put("All",		MOD_UI + "GUARDIAN_MONSTERS_ALL");
	default boolean noPlanetHaveMonster()		{ return guardianMonsters.get().equals("None"); }
	default boolean richPlanetHaveMonster()		{
		return guardianMonsters.get().equals("All") || guardianMonsters.get().equals("Rich");
	}
	default boolean artefactPlanetHaveMonster()	{
		return guardianMonsters.get().equals("All") || guardianMonsters.get().equals("Artefact");
	}

	ParamInteger guardianMonstersProbability = new ParamInteger(MOD_UI, "GUARDIAN_MONSTERS_PCT", 50)
			.setLimits(0, 500)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default float guardianMonstersProbability()	{
		if (noPlanetHaveMonster())
			return 0;
		return guardianMonstersProbability.get()/100f;
	}
	ParamInteger guardianMonstersLevel = new ParamInteger(MOD_UI, "GUARDIAN_MONSTERS_LEVEL", 100)
			.setLimits(10, 1000)
			.setIncrements(5, 20, 100)
			.pctValue(true);
	default float guardianMonstersLevel()		{ return guardianMonstersLevel.get()/100f; }
	
	default float guardianMonstersProbability(Planet planet) {
		if (noPlanetHaveMonster())
			return 0;
		if (artefactPlanetHaveMonster() && planet.isArtifact()) {
			float basePct	= guardianMonstersProbability.get()/100f;
			float planetPct	= planet.baseSize()/100f;
			return basePct * planetPct;
		}
		if (richPlanetHaveMonster() && planet.isResourceRich()) {
			float basePct	= guardianMonstersProbability.get()/100f;
			float planetPct	= planet.baseSize()/100f;
			return basePct * planetPct;
		}
		if (richPlanetHaveMonster() && planet.isResourceUltraRich()) {
			float basePct	= guardianMonstersProbability.get()/100f;
			float planetPct	= planet.baseSize()/100f;
			return 1.5f * basePct * planetPct;
		}
		return 0;
	}

	String NEBULA_POS_NORMAL	= "NORMAL";
	String NEBULA_POS_INSIST	= "INSIST";
	String NEBULA_POS_EXTEND	= "EXTEND";
	ParamList nebulaPlacing		= new ParamList( MOD_UI, "NEBULA_POS", NEBULA_POS_NORMAL)
		.isDuplicate(false)
		.isCfgFile(true)
		.showFullGuide(true)
		.put(NEBULA_POS_NORMAL,	MOD_UI + "NEBULA_POS_NORMAL")
		.put(NEBULA_POS_INSIST,	MOD_UI + "NEBULA_POS_INSIST")
		.put(NEBULA_POS_EXTEND,	MOD_UI + "NEBULA_POS_EXTEND");
	default ParamList getNebulaPlacing()	{ return nebulaPlacing; }

	default boolean looseNebula()	{ return nebulaPlacing.get().equalsIgnoreCase(NEBULA_POS_EXTEND); }
	default int nebulaCallsBeforeShrink()	{
		switch (nebulaPlacing.get().toUpperCase()) {
		case NEBULA_POS_NORMAL:	return 1;
		case NEBULA_POS_INSIST:	return 5;
		case NEBULA_POS_EXTEND:	return 10;
		}
		return 1;
	}

	String NEBULA_ENRICHMENT_NONE	= "NONE";
	String NEBULA_ENRICHMENT_LESS	= "LESS";
	String NEBULA_ENRICHMENT_NORMAL	= "NORMAL";
	String NEBULA_ENRICHMENT_MORE	= "MORE";
	String NEBULA_ENRICHMENT_ALWAYS	= "ALWAYS";
	ParamList nebulaEnrichment		= new ParamList( MOD_UI, "NEBULA_ENRICHMENT", NEBULA_ENRICHMENT_NORMAL)
		.isDuplicate(false)
		.isCfgFile(true)
		.showFullGuide(true)
		.put(NEBULA_ENRICHMENT_NONE,	MOD_UI + "NEBULA_ENRICHMENT_NONE")
		.put(NEBULA_ENRICHMENT_LESS,	MOD_UI + "NEBULA_ENRICHMENT_LESS")
		.put(NEBULA_ENRICHMENT_NORMAL,	MOD_UI + "NEBULA_ENRICHMENT_NORMAL")
		.put(NEBULA_ENRICHMENT_MORE,	MOD_UI + "NEBULA_ENRICHMENT_MORE")
		.put(NEBULA_ENRICHMENT_ALWAYS,	MOD_UI + "NEBULA_ENRICHMENT_ALWAYS");
	default boolean noNebulaEnrichment()		{ return nebulaEnrichment.get().equalsIgnoreCase(NEBULA_ENRICHMENT_NONE); }
	default boolean alwaysNebulaEnrichment()	{ return nebulaEnrichment.get().equalsIgnoreCase(NEBULA_ENRICHMENT_ALWAYS); }
	default int nebulaEnrichmentInsideStar()	{
		switch (nebulaEnrichment.get().toUpperCase()) {
		case NEBULA_ENRICHMENT_NONE:	return Integer.MAX_VALUE;
		case NEBULA_ENRICHMENT_LESS:	return 4;
		case NEBULA_ENRICHMENT_NORMAL:	return 3;
		case NEBULA_ENRICHMENT_MORE:	return 2;
		case NEBULA_ENRICHMENT_ALWAYS:	return 1;
		}
		return 3;
	}
	default int nebulaEnrichmentGalaxySize()	{
		switch (nebulaEnrichment.get().toUpperCase()) {
		case NEBULA_ENRICHMENT_NONE:	return Integer.MAX_VALUE;
		case NEBULA_ENRICHMENT_LESS:	return 200;
		case NEBULA_ENRICHMENT_NORMAL:	return 100;
		case NEBULA_ENRICHMENT_MORE:	return 50;
		case NEBULA_ENRICHMENT_ALWAYS:	return 1;
		}
		return 100;
	}
	String NEBULA_HOMEWORLD_ALLOW	= "ALLOW";
	String NEBULA_HOMEWORLD_NEVER	= "NEVER";
	ParamList nebulaHomeworld		= new ParamList( MOD_UI, "NEBULA_HOMEWORLD", NEBULA_HOMEWORLD_ALLOW)
		.isDuplicate(false)
		.isCfgFile(true)
		.showFullGuide(true)
		.put(NEBULA_HOMEWORLD_ALLOW,	MOD_UI + "NEBULA_HOMEWORLD_ALLOW")
		.put(NEBULA_HOMEWORLD_NEVER,	MOD_UI + "NEBULA_HOMEWORLD_NEVER");
	default boolean neverNebulaHomeworld()	{ return nebulaHomeworld.get().equalsIgnoreCase(NEBULA_HOMEWORLD_NEVER); }

	ParamBoolean looseNeighborhood	= new ParamBoolean( MOD_UI, "LOOSE_NEIGHBORHOOD", false);
	default boolean looseNeighborhood()			{ return looseNeighborhood.get(); }
	default ParamBoolean getLooseNeighborhood()	{ return looseNeighborhood; }
}
