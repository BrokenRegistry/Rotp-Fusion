package rotp.model.game;

import rotp.model.colony.Colony;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.planet.Planet;
import rotp.model.ships.ShipDesign;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamFloat;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;

public interface IInGameOptions extends IRandomEvents, IConvenienceOptions, ICombatOptions, IShipDesignOption {

	// ========================================================================
	// GamePlay options
	ParamList popGrowthFactor				= new ParamList(MOD_UI, "POP_GROWTH", "Normal")
			.showFullGuide(true)
			.put("Normal",		MOD_UI + "POP_GROWTH_NORMAL")
			.put("Reduced",		MOD_UI + "POP_GROWTH_REDUCED");
	default String selectedPopGrowthFactor()	{ return popGrowthFactor.get(); }

	ParamList targetBombard					= new ParamList(MOD_UI, "TARGET_BOMBARD", "None")
			.showFullGuide(true)
			.put("None",	MOD_UI + "TARGET_BOMBARD_NONE")
			.put("AI",		MOD_UI + "TARGET_BOMBARD_AI")
			.put("Player",	MOD_UI + "TARGET_BOMBARD_PLAYER")
			.put("Both",	MOD_UI + "TARGET_BOMBARD_BOTH");
	default boolean targetBombardAllowedForAI() {
		switch (targetBombard.get().toUpperCase()) {
			case  "BOTH":
			case  "AI":
				return true;
			default:
				return false;
		}
	}
	default boolean targetBombardAllowedForPlayer() {
		switch (targetBombard.get().toUpperCase()) {
			case  "BOTH":
			case  "PLAYER":
				return true;
			default:
				return false;
		}
	}

	ParamInteger customDifficulty	= new ParamInteger(MOD_UI, "CUSTOM_DIFFICULTY", 100)
			.setLimits(20, 500)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default int selectedCustomDifficulty()		{ return customDifficulty.get(); }

	ParamBoolean dynamicDifficulty	= new ParamBoolean(MOD_UI, "DYNAMIC_DIFFICULTY", false);
	default boolean selectedDynamicDifficulty()	{ return dynamicDifficulty.get(); }

	ParamList scrapRefundOption		= new ParamList(MOD_UI, "SCRAP_REFUND", "All")
			.showFullGuide(true)
			.put("All",		MOD_UI + "SCRAP_REFUND_ALL")
			.put("Empire",	MOD_UI + "SCRAP_REFUND_EMPIRE")
			.put("Ally",	MOD_UI + "SCRAP_REFUND_ALLY")
			.put("Never",	MOD_UI + "SCRAP_REFUND_NEVER");
	default String selectedScrapRefundOption()	{ return scrapRefundOption.get(); }

	ParamFloat scrapRefundFactor	= new ParamFloat(MOD_UI, "SCRAP_REFUND_FACTOR", 0.25f)
			.setLimits(0f, 1f)
			.setIncrements(0.01f, 0.05f, 0.2f)
			.cfgFormat( "0.##")
			.guiFormat("%");
	default float selectedScrapRefundFactor()	{ return scrapRefundFactor.get(); }

	ParamInteger shipSpaceFactor	= new ParamInteger(MOD_UI, "SHIP_SPACE_FACTOR", 100)
			.setLimits(10, 1000)
			.setIncrements(1, 10, 50)
			.pctValue(true);
	default float selectedShipSpaceFactor()	{ return shipSpaceFactor.get()/100f; }

	ParamFloat missileBaseModifier	= new ParamFloat(MOD_UI, "MISSILE_BASE_MODIFIER", 2f/3f)
			.setDefaultValue(MOO1_DEFAULT, 1f)
			.setDefaultValue(ROTP_DEFAULT, 1f)
			.setLimits(0.1f, 2f)
			.setIncrements(0.01f, 0.05f, 0.2f)
			.cfgFormat("0.##")
			.guiFormat("%")
			.formerName(MOD_UI+"MISSILE_SIZE_MODIFIER");
	default float selectedMissileBaseModifier()	{ return missileBaseModifier.get(); }

	ParamFloat missileShipModifier	= new ParamFloat(MOD_UI, "MISSILE_SHIP_MODIFIER", 2f/3f)
			.setDefaultValue(MOO1_DEFAULT, 1f)
			.setDefaultValue(ROTP_DEFAULT, 1f)
			.setLimits(0.1f, 2f)
			.setIncrements(0.01f, 0.05f, 0.2f)
			.cfgFormat("0.##")
			.guiFormat("%")
			.formerName(MOD_UI+"MISSILE_SIZE_MODIFIER");
	default float selectedMissileShipModifier()	{ return missileShipModifier.get(); }

	ParamBoolean challengeMode		= new ParamBoolean(MOD_UI, "CHALLENGE_MODE", false);
	default boolean selectedChallengeMode()		{ return challengeMode.get(); }
	
	ParamFloat counciRequiredPct	= new ParamFloat(MOD_UI, "COUNCIL_REQUIRED_PCT", 2f/3f)
			.setLimits(0f, 0.99f)
			.setIncrements(0.01f/3f, 0.02f, 0.1f)
			.cfgFormat("0.##")
			.guiFormat("‰");

	ParamInteger bombingTarget		= new ParamInteger(MOD_UI, "BOMBING_TARGET", 2)
			.setLimits(0, Planet.finalSizeMax)
			.setIncrements(1, 5, 20);
	default int selectedBombingTarget()			{ return bombingTarget.get(); }

	ParamList autoTerraformEnding	= new ParamList( MOD_UI, "AUTO_TERRAFORM_ENDING", "Populated")
			.showFullGuide(true)
			.put("Populated",	MOD_UI + "TERRAFORM_POPULATED")
			.put("Terraformed",	MOD_UI + "TERRAFORM_TERRAFORMED")
			.put("Cleaned",		MOD_UI + "TERRAFORM_CLEANED");
	default String selectedAutoTerraformEnding()	{ return autoTerraformEnding.get(); }

	ParamBoolean trackUFOsAcrossTurns = new ParamBoolean(MOD_UI, "TRACK_UFOS_ACROSS_TURNS", false);
	default boolean selectedTrackUFOsAcrossTurns()	{ return trackUFOsAcrossTurns.get(); }

	ParamBoolean allowTechStealing	= new ParamBoolean(MOD_UI, "ALLOW_TECH_STEALING", true);
	default boolean forbidTechStealing()	 	{ return !allowTechStealing.get(); }

	ParamInteger maxTechsCaptured	= new MaxTechsCaptured()
			.setLimits(0, 200)
			.setIncrements(1, 5, 20);
	default int maxTechsCaptured()				{ return maxTechsCaptured.get(); }
	final class MaxTechsCaptured extends ParamInteger {
		MaxTechsCaptured() {
			super(MOD_UI, "MAX_TECH_CAPTURED", 6);
			setLimits(0, 10);
			setIncrements(1, 2, 5);
			loop(true);
		}
		// For backward compatibility
		@Override protected Integer getOptionValue(IGameOptions options) {
			Integer value = options.dynOpts().getInteger(getLangLabel());
			if (value == null) {
				Boolean allowTechStealing = options.dynOpts().getBoolean(MOD_UI + "ALLOW_TECH_STEALING");
				if (allowTechStealing == null)
					allowTechStealing = true;
				if (allowTechStealing)
					value = defaultValue();
				else
					value = 0;
			}
			return value;
		}
	}

	ParamInteger maxSecurityPct		= new ParamInteger(MOD_UI, "MAX_SECURITY_PCT", 10)
			.setLimits(10, 90)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default int selectedMaxSecurityPct()		{ return maxSecurityPct.get(); }

	ParamList darkGalaxy			= new ParamList( MOD_UI, "DARK_GALAXY", "No")
			.showFullGuide(true)
			.isValueInit(false)
			.put("No",		MOD_UI + "DARK_GALAXY_NO")
			.put("Shrink",	MOD_UI + "DARK_GALAXY_SHRINK")
			.put("NoSpy",	MOD_UI + "DARK_GALAXY_NO_SPY")
			.put("Spy",		MOD_UI + "DARK_GALAXY_SPY");
	default boolean selectedDarkGalaxy()	{
		return !darkGalaxy.get().equalsIgnoreCase("No") 
				&& GameSession.instance().inProgress(); // for the final replay
	}
	default boolean darkGalaxySpy()			{ return darkGalaxy.get().equalsIgnoreCase("Spy"); }
	default boolean darkGalaxyNoSpy()		{ return darkGalaxy.get().equalsIgnoreCase("NoSpy"); }
	default boolean darkGalaxyDark()		{ return darkGalaxy.get().equalsIgnoreCase("Shrink"); }
	
	ParamList transportAutoEco			= new ParamList( MOD_UI, "TRANSPORT_AUTO_ECO", "No")
			.showFullGuide(true)
			.isValueInit(false)
			.put("No",	 MOD_UI + "TRANSPORT_AUTO_ECO_NO")
			.put("Yes",	 MOD_UI + "TRANSPORT_AUTO_ECO_YES")
			.put("Last", MOD_UI + "TRANSPORT_AUTO_ECO_LAST");
	default boolean transportAutoEcoDefaultNo()	{ return transportAutoEco.get().equals("No"); }
	default boolean transportAutoEcoDefaultYes(){ return transportAutoEco.get().equals("Yes"); }
	default boolean transportAutoEcoLast()		{ return transportAutoEco.get().equals("Last"); }

	ParamBoolean spyOverSpend			= new ParamBoolean(MOD_UI, "SPY_OVERSPEND", true);
	default boolean spyOverSpend()				{ return spyOverSpend.get(); }

	ParamList councilPlayerVote			= new ParamList( MOD_UI, "COUNCIL_PLAYER_VOTE", "By Size")
			.showFullGuide(true)
			.put("First",	MOD_UI + "COUNCIL_PLAYER_VOTE_FIRST")
			.put("By Size",	MOD_UI + "COUNCIL_PLAYER_VOTE_SIZE")
			.put("Last",	MOD_UI + "COUNCIL_PLAYER_VOTE_LAST");
	default boolean playerVotesFirst()	{ return councilPlayerVote.get().equalsIgnoreCase("First"); }
	default boolean playerVotesLast()	{ return councilPlayerVote.get().equalsIgnoreCase("Last"); }

	ParamBoolean defaultForwardRally	= new ParamBoolean(MOD_UI, "DEFAULT_FORWARD_RALLY", true);
	default boolean defaultForwardRally()	{ return defaultForwardRally.get(); }

	ParamBoolean defaultChainRally		= new ParamBoolean(MOD_UI, "DEFAULT_CHAIN_RALLY", true);
	default boolean defaultChainRally()		{ return defaultChainRally.get(); }
	
	String CHAIN_RALLY_SPEED_FLEET	= "FLEET";
	String CHAIN_RALLY_SPEED_MIN	= "MIN";
	String CHAIN_RALLY_SPEED_TOP	= "TOP";
	ParamList chainRallySpeed		= new ParamList( MOD_UI, "CHAIN_RALLY_SPEED", CHAIN_RALLY_SPEED_FLEET)
			.showFullGuide(true)
			.put(CHAIN_RALLY_SPEED_FLEET,	MOD_UI + "CHAIN_RALLY_SPEED_FLEET")
			.put(CHAIN_RALLY_SPEED_MIN,		MOD_UI + "CHAIN_RALLY_SPEED_MIN")
			.put(CHAIN_RALLY_SPEED_TOP,		MOD_UI + "CHAIN_RALLY_SPEED_TOP");
	default String chainRallySpeed()		{ return chainRallySpeed.get(); }
	default Float chainRallySpeed(Empire player, ShipFleet fleet)	{
		if (fleet == null)
			return chainRallySpeed(player);
		switch (chainRallySpeed.get().toUpperCase()) {
			case CHAIN_RALLY_SPEED_FLEET :
				return fleet.slowestStackSpeed();
			case CHAIN_RALLY_SPEED_MIN :
				return fleet.empire().minActiveDesignSpeed();
			case CHAIN_RALLY_SPEED_TOP :
				return fleet.empire().tech().topSpeed();
		}
		return null;
	}
	default Float chainRallySpeed(Empire player, ShipDesign design)	{
		if (design == null)
			return chainRallySpeed(player);
		switch (chainRallySpeed.get().toUpperCase()) {
			case CHAIN_RALLY_SPEED_FLEET :
				return (float) design.warpSpeed();
			case CHAIN_RALLY_SPEED_MIN :
				return player.minActiveDesignSpeed();
			case CHAIN_RALLY_SPEED_TOP :
				return player.tech().topSpeed();
		}
		return null;
	}
	default Float chainRallySpeed(Empire player)	{
		switch (chainRallySpeed.get().toUpperCase()) {
			case CHAIN_RALLY_SPEED_FLEET :
			case CHAIN_RALLY_SPEED_MIN :
				return player.minActiveDesignSpeed();
			case CHAIN_RALLY_SPEED_TOP :
				return player.tech().topSpeed();
		}
		return null;
	}

	String PEACE_TREATY_NORMAL		= "NORMAL";
	String PEACE_TREATY_ARMISTICE	= "NOWAR";
	String PEACE_TREATY_COLD_WAR	= "TRUCE";
	ParamList specialPeaceTreaty	= new ParamList( MOD_UI, "SPECIAL_PEACE_TREATY", PEACE_TREATY_NORMAL)
			.showFullGuide(true)
			.put(PEACE_TREATY_NORMAL,	 MOD_UI + "SPECIAL_PEACE_TREATY_NORMAL")
			.put(PEACE_TREATY_ARMISTICE, MOD_UI + "SPECIAL_PEACE_TREATY_NOWAR")
			.put(PEACE_TREATY_COLD_WAR,	 MOD_UI + "SPECIAL_PEACE_TREATY_TRUCE");
	default boolean allowPeaceTreaty()	{ return !specialPeaceTreaty.get().equalsIgnoreCase(PEACE_TREATY_ARMISTICE) ;}
	default boolean isColdWarMode()		{ return specialPeaceTreaty.get().equalsIgnoreCase(PEACE_TREATY_COLD_WAR) ;}

	String DEVELOPED_ALL		= "ALL";
	String DEVELOPED_NO_BASE	= "NO_BASES";
	String DEVELOPED_INDUSTRY	= "INDUSTRY";
	ParamList developedDefinition	= new ParamList(MOD_UI, "DEVELOPED_DEFINITION", DEVELOPED_ALL)
			.showFullGuide(true)
			.put(DEVELOPED_ALL, 	 MOD_UI + "DEVELOPED_ALL")
			.put(DEVELOPED_NO_BASE,  MOD_UI + "DEVELOPED_NO_BASE")
			.put(DEVELOPED_INDUSTRY, MOD_UI + "DEVELOPED_INDUSTRY");
	default boolean isDeveloped(Colony col)	{
		switch (developedDefinition.get()) {
		case DEVELOPED_NO_BASE:
			return col.industry().isCompleted(maxMissingFactories())
					&& col.ecology().isCompleted(maxMissingPopulation());
		case DEVELOPED_INDUSTRY:
			return col.industry().isCompleted(maxMissingFactories());
		case DEVELOPED_ALL:
		default:
			return col.defense().isCompleted(0)
					&& col.industry().isCompleted(maxMissingFactories())
					&& col.ecology().isCompleted(maxMissingPopulation());
		}
	}
	ParamInteger maxMissingPopulation	= new ParamInteger(MOD_UI, "DEV_MAX_MISSING_POP", 3)
			.setDefaultValue(MOO1_DEFAULT, 0)
			.setDefaultValue(ROTP_DEFAULT, 0)
			.setLimits(0, 50)
			.setIncrements(1, 5, 20);
	default int maxMissingPopulation()	{ return maxMissingPopulation.get(); }	

	ParamInteger maxMissingFactories	= new ParamInteger(MOD_UI, "DEV_MAX_MISSING_FACT", 3)
			.setDefaultValue(MOO1_DEFAULT, 0)
			.setDefaultValue(ROTP_DEFAULT, 0)
			.setLimits(0, 50)
			.setIncrements(1, 5, 20);
	default int maxMissingFactories()	{ return maxMissingFactories.get(); }	

	String RALLY_COMBAT_NEVER	= "RALLY_COMBAT_NEVER";
	String RALLY_COMBAT_BUILT	= "RALLY_COMBAT_BUILT";
	String RALLY_COMBAT_ALL		= "RALLY_COMBAT_ALL";
	String RALLY_COMBAT_PASS_BY	= "RALLY_COMBAT_PASS_BY";
	ParamList rallyCombat			= new ParamList( MOD_UI, "RALLY_COMBAT", RALLY_COMBAT_ALL)
			.isCfgFile(true)
			.showFullGuide(true)
			.put(RALLY_COMBAT_NEVER,	MOD_UI + RALLY_COMBAT_NEVER)
			.put(RALLY_COMBAT_BUILT,	MOD_UI + RALLY_COMBAT_BUILT)
			.put(RALLY_COMBAT_PASS_BY,	MOD_UI + RALLY_COMBAT_PASS_BY)
			.put(RALLY_COMBAT_ALL,		MOD_UI + RALLY_COMBAT_ALL)
			.setDefaultValue(FUSION_DEFAULT, RALLY_COMBAT_ALL)
			.setDefaultValue(MOO1_DEFAULT,   RALLY_COMBAT_BUILT)
			.setDefaultValue(ROTP_DEFAULT,   RALLY_COMBAT_BUILT);
	default boolean rallyJoinCombat()		{ return !rallyCombat.get().equals(RALLY_COMBAT_NEVER); }
	default boolean rallyBuiltJoinCombat()	{ return rallyCombat.get().equals(RALLY_COMBAT_BUILT) || rallyAllJoinCombat(); }
	default boolean rallyTransitJoinCombat(){ return rallyCombat.get().equals(RALLY_COMBAT_PASS_BY) || rallyAllJoinCombat(); }
	default boolean rallyAllJoinCombat()	{ return rallyCombat.get().equals(RALLY_COMBAT_ALL); }

	String COMBAT_LOSS_DEFENSES	= "COMBAT_LOSS_DEFENSE";
	String COMBAT_LOSS_RALLY	= "COMBAT_LOSS_RALLY";
	String COMBAT_LOSS_SHARED	= "COMBAT_LOSS_SHARED";
	ParamList rallyCombatLoss	= new ParamList( MOD_UI, "RALLY_COMBAT_LOSS", COMBAT_LOSS_DEFENSES)
			.isCfgFile(true)
			.showFullGuide(true)
			.put(COMBAT_LOSS_DEFENSES,	MOD_UI + COMBAT_LOSS_DEFENSES)
			.put(COMBAT_LOSS_RALLY,		MOD_UI + COMBAT_LOSS_RALLY)
			.put(COMBAT_LOSS_SHARED,	MOD_UI + COMBAT_LOSS_SHARED);
	default String rallyLosses()		{ return rallyCombatLoss.get(); }
	default boolean rallyLossDefense()	{ return rallyCombatLoss.get().equals(COMBAT_LOSS_DEFENSES); }
	default boolean rallyLossRally()	{ return rallyCombatLoss.get().equals(COMBAT_LOSS_RALLY); }
	default boolean rallyLossShared()	{ return rallyCombatLoss.get().equals(COMBAT_LOSS_SHARED); }

	ParamBoolean useSmartRefit		= new ParamBoolean(MOD_UI, "USE_SMART_REFIT", true);
	default boolean useSmartRefit()		{ return useSmartRefit.get(); }

	String MAX_LANDING_UNLIMITED	= "MAX_LANDING_UNLIMITED";
	String MAX_LANDING_MULTIPLER	= "MAX_LANDING_MULTIPLER";
	String MAX_LANDING_FIXED		= "MAX_LANDING_FIXED";
	ParamList maxLandingTroops	= new ParamList( MOD_UI, "MAX_LANDING_TROOPS", MAX_LANDING_UNLIMITED)
			.showFullGuide(true)
			.setDefaultValue(MOO1_DEFAULT, MAX_LANDING_FIXED)
			.isValueInit(false)
			.put(MAX_LANDING_UNLIMITED,	MOD_UI + MAX_LANDING_UNLIMITED)
			.put(MAX_LANDING_FIXED,		MOD_UI + MAX_LANDING_FIXED)
			.put(MAX_LANDING_MULTIPLER,	MOD_UI + MAX_LANDING_MULTIPLER);
	ParamInteger maxLandingTroopsAmount	= new ParamInteger(MOD_UI, "MAX_LANDING_AMOUNT", 300)
			.setDefaultValue(MOO1_DEFAULT, 300)
			.setLimits(0, 10000)
			.setIncrements(10, 50, 200);
	ParamInteger maxLandingTroopsFactor	= new ParamInteger(MOD_UI, "MAX_LANDING_FACTOR", 200)
			.setLimits(0, 10000)
			.setIncrements(10, 50, 200);
	ParamInteger maxLandingTroopsIAFactor	= new ParamInteger(MOD_UI, "MAX_LANDING_IA_FACTOR", 100)
			.setLimits(0, 1000)
			.setIncrements(5, 20, 100);
	default float maxLandingTroops(StarSystem sys, boolean isPlayer)	{
		float playerLimit = 0;
		switch (maxLandingTroops.get()) {
			case MAX_LANDING_FIXED:
				playerLimit = maxLandingTroopsAmount.get();
			case MAX_LANDING_MULTIPLER:
				playerLimit = maxLandingTroopsFactor.get() * sys.planet().currentSize() / 100;
			case MAX_LANDING_UNLIMITED:
			default:
				playerLimit = Integer.MAX_VALUE;
		}
		if (isPlayer)
			return playerLimit;
		else
			return playerLimit * maxLandingTroopsIAFactor.get() / 100;
	}
	String AGGRESSIV_NORMAL		= "AGGRESSIV_NORMAL";
	String AGGRESSIV_AI_WAR_OK	= "AGGRESSIV_AI_WAR_OK";	// AI can declare war to AI but not to player
	String AGGRESSIV_AI_NO_WAR	= "AGGRESSIV_AI_NO_WAR";	// No war between AI
	String AGGRESSIV_NEVER_WAR	= "AGGRESSIV_NEVER_WAR";	// Player can't declare war neither
	String AGGRESSIV_ALWAYS_WAR	= "AGGRESSIV_ALWAYS_WAR";	// All empire are permanently at war
	String AGGRESSIV_ALLIANCE	= "AGGRESSIV_ALLIANCE"; 	// All empire are permanently Allied
	ParamList gameAgressiveness	= new ParamList( MOD_UI, "GAME_AGGRESSIVENESS", AGGRESSIV_NORMAL)
			.isCfgFile(true)
			.showFullGuide(true)
			.put(AGGRESSIV_NORMAL,		MOD_UI + AGGRESSIV_NORMAL)
			.put(AGGRESSIV_AI_WAR_OK,	MOD_UI + AGGRESSIV_AI_WAR_OK)
			.put(AGGRESSIV_AI_NO_WAR,	MOD_UI + AGGRESSIV_AI_NO_WAR)
			.put(AGGRESSIV_NEVER_WAR,	MOD_UI + AGGRESSIV_NEVER_WAR)
			.put(AGGRESSIV_ALLIANCE,	MOD_UI + AGGRESSIV_ALLIANCE)
			.put(AGGRESSIV_ALWAYS_WAR,	MOD_UI + AGGRESSIV_ALWAYS_WAR);
	
	default boolean alwaysAlly()	{ return gameAgressiveness.get().equals(AGGRESSIV_ALLIANCE); }
	default boolean alwaysAtWar()	{ return gameAgressiveness.get().equals(AGGRESSIV_ALWAYS_WAR); }
	default boolean canStopWar()	{ return !alwaysAtWar(); }
	default boolean canStartWar(Empire ask, Empire target)	{ return canStartWar(ask.isPlayer(), target.isPlayer()); }
	default boolean canStartWar(boolean askIsPlayer, boolean targetIsPlayer)	{
		// Player vs AI
		if (askIsPlayer)
			switch (gameAgressiveness.get()) {
				case AGGRESSIV_NEVER_WAR:
					return false;
				default:
					return true;
			}
		// AI vs Player
		if (targetIsPlayer)
			switch (gameAgressiveness.get()) {
				case AGGRESSIV_NEVER_WAR:
				case AGGRESSIV_AI_WAR_OK:
				case AGGRESSIV_AI_NO_WAR:
					return false;
				default:
					return true;
			}
		// AI vs AI
		switch (gameAgressiveness.get()) {
			case AGGRESSIV_NEVER_WAR:
				return false;
			default:
				return true;
		}
	}
	ParamBoolean skirmishesAllowed		= new ParamBoolean(MOD_UI, "SKIRMISHES_ALLOWED", true);
	default boolean skirmishesAllowed()	{ return skirmishesAllowed.get(); }
	default boolean skirmishesAllowed(Empire ask, Empire target)	{
		if (skirmishesAllowed.get())
			return true;
		EmpireView view = ask.viewForEmpire(target);
		if (view != null && view.embassy().war())
			return true;

		boolean askIsPlayer		= ask.isPlayer();
		boolean targetIsPlayer	= target.isPlayer();
		if (askIsPlayer)
			switch (gameAgressiveness.get()) {
				case AGGRESSIV_NEVER_WAR:
					return false;
				case AGGRESSIV_AI_WAR_OK:
				case AGGRESSIV_AI_NO_WAR:
					// if AI can't attack player, and skirmish are not allowed:
					// Player can't start skirmish either. Player should declare war first!
					return skirmishesAllowed.get();
				default:
					return true;
			}
		// AI vs Player
		if (targetIsPlayer)
			switch (gameAgressiveness.get()) {
				case AGGRESSIV_NEVER_WAR:
				case AGGRESSIV_AI_WAR_OK:
				case AGGRESSIV_AI_NO_WAR:
					return skirmishesAllowed.get();
				default:
					return true;
			}
		// AI vs AI
		switch (gameAgressiveness.get()) {
			case AGGRESSIV_NEVER_WAR:
				return skirmishesAllowed.get();
			default:
				return true;
		}
	}

	String CLOSEST_COLONY	= "CLOSEST_COLONY";
	String CLOSEST_ALLY		= "CLOSEST_ALLY";
	String ANY_ALLY			= "ANY_ALLY";
	String ANY_STAR_SYSTEM	= "ANY_STAR_SYSTEM";
	ParamList retreatDestination	= new ParamList( MOD_UI, "RETREAT_DESTINATION", ANY_ALLY)
			.showFullGuide(true)
			.put(CLOSEST_COLONY,	MOD_UI + CLOSEST_COLONY)
			.put(CLOSEST_ALLY,		MOD_UI + CLOSEST_ALLY)
			.put(ANY_ALLY,			MOD_UI + ANY_ALLY)
			.put(ANY_STAR_SYSTEM,	MOD_UI + ANY_STAR_SYSTEM)
			.setDefaultValue(FUSION_DEFAULT, ANY_ALLY)
			.setDefaultValue(MOO1_DEFAULT, CLOSEST_COLONY)
			.setDefaultValue(ROTP_DEFAULT, ANY_ALLY);
	default boolean retreatToAnyPlanet()	{ return retreatDestination.get().equals(ANY_STAR_SYSTEM); }
	default boolean retreatOnlyToAlly()		{ 
		switch (retreatDestination.get()) {
			case CLOSEST_ALLY:
			case ANY_ALLY:
				return true;
			default:
				return false;
		}
	}
	default boolean retreatClosestOnly()	{ 
		switch (retreatDestination.get()) {
			case CLOSEST_COLONY:
			case CLOSEST_ALLY:
				return true;
			default:
				return false;
		}
	}

	ParamBoolean hyperComRetreatExtended	= new ParamBoolean(MOD_UI, "HYPER_COM_RETREAT_EXT", true)
			.setDefaultValue(FUSION_DEFAULT, false)
			.setDefaultValue(MOO1_DEFAULT, false)
			.setDefaultValue(ROTP_DEFAULT, false);
	default boolean hyperComRetreatExtended()			{ return hyperComRetreatExtended.get(); }
	ParamBoolean noEnemyOnRetreatDestination	= new ParamBoolean(MOD_UI, "NO_RETREAT_ENEMY_DESTINATION", false);
	default boolean noEnemyOnRetreatDestination()		{ return noEnemyOnRetreatDestination.get(); }

//	ParamBoolean markRetreatOnArrivalAsRetreating	= new ParamBoolean(MOD_UI, "RETREAT_ON_ARRIVAL_RETREATING", true);
//	default boolean markRetreatOnArrivalAsRetreating()	{ return markRetreatOnArrivalAsRetreating.get(); }
//	ParamBoolean markDiplomaticRetreatAsRetreating	= new ParamBoolean(MOD_UI, "DIPLOMATIC_RETREAT_RETREATING", true);
//	default boolean markDiplomaticRetreatAsRetreating()	{ return markDiplomaticRetreatAsRetreating.get(); }
}
