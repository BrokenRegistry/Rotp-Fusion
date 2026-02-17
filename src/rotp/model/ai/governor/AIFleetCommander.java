package rotp.model.ai.governor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import rotp.model.ai.governor.ParamFleetAuto.SubFleet;
import rotp.model.ai.governor.ParamFleetAuto.SubFleetList;
import rotp.model.ai.interfaces.FleetCommander;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.empires.SystemInfo;
import rotp.model.empires.SystemView;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GovernorOptions;
import rotp.model.game.IGovOptions;
import rotp.model.planet.PlanetType;
import rotp.util.Base;

// BR: Moved Governor Ship automation from empire to here
public class AIFleetCommander implements Base, FleetCommander {
	private static final float MAX_ALLOWED_SHIP_MAINT = 0.35f;
	private final Empire empire;
	public AIFleetCommander(Empire e)	{ empire = e; }
	@Override public boolean inExpansionMode()	{
		for(EmpireView contact : empire.contacts())
			if(empire.inShipRange(contact.empId()))
				return false;
		if((empire.tech().planetology().techLevel() > 19 || empire.ignoresPlanetEnvironment())
			&& empire.shipLab().needScouts == false)
			return false;
		return true;
	}
	@Override public void nextTurn()	{
		autocolonize();
		autoattack();
		autoscout();
		if (session().getGovernorOptions().isAutoScout())
			// To avoid damaging your relationship with that empire
			moveScoutAwayFromAlienColonies();
	}
	@Override public float transportPriority(StarSystem sv){
			int id = sv.id;
			float pr = sv.colony().transportPriority();

			if (empire.sv.colony(id).inRebellion())
				return pr * 5;
			else if (empire.sv.isBorderSystem(id))
				return pr * 2;
			else if (empire.sv.isInnerSystem(id))
				return pr / 2;
			else
				return pr;
	}
	@Override public float maxShipMaintainance()	{ return MAX_ALLOWED_SHIP_MAINT; }

	/**
	 * Sort targets to by value and to be as close to source system as possible
	 */
	private interface SystemsSorter	{
		void sort(Integer sourceSystem, List<Integer> targets, float warpSpeed);
	}
	private interface TargetsSorter	{
		void sort(Integer sourceSystem, List<Target> targets, float warpSpeed);
	}
	/**
	 * Sort fleets by value and to be as close to target system as possible
	 */
	private class ColonyPriority implements TargetsSorter	{
		private final float secondColonyWeight;
		private final float distanceWeight;
		private final boolean autoColonyMultiple;
		public ColonyPriority()	{
			GovernorOptions gov	= govOptions();
			secondColonyWeight	= gov.secondColonyWeightPct();
			distanceWeight		= gov.colonyDistanceWeight();
			autoColonyMultiple	= gov.autoColonizeTuned() && gov.autoColonizeMultiple();
		}
		@Override public void sort(Integer sourceSystem, List<Target> targets, float warpSpeed)	{
			// ok, let's use both distance and value of planet to prioritize colonization, 50% and 50%
			StarSystem source = empire.sv.system(sourceSystem);

			float maxTravelTime = -1;
			float maxValue = -1;
			for (Target target : targets) {
				float value = planetValue(target.sysId);
				if (maxValue < 0 || maxValue < value)
					maxValue = value;

				float travelTime = source.travelTimeTo(empire.sv.system(target.sysId), warpSpeed);
				if (maxTravelTime < travelTime)
					maxTravelTime = travelTime;
			}
			// could happen if we only have 1 colony ship already orbiting the only remaining colonizable planet
			if (Math.abs(maxTravelTime) <= 0.1)
				maxTravelTime = 1;
			//System.out.println(message+" maxDistance =" + maxDistance + " maxValue=" + maxValue);

			float maxDistance1 = maxTravelTime;
			float maxValue1 = maxValue;
			// planets with lowest weight are most desirable (closest/best)
			targets.sort((s1, s2) -> (int) Math.signum(
					autoColonizeWeight(source, s1, maxDistance1, maxValue1, warpSpeed, autoColonyMultiple, secondColonyWeight, distanceWeight) -
					autoColonizeWeight(source, s2, maxDistance1, maxValue1, warpSpeed, autoColonyMultiple, secondColonyWeight, distanceWeight)));
			//for (Target target : targets) {
			//	double weight = autocolonizeWeight(source, target, maxDistance, maxValue, warpSpeed);
			//	System.out.format(message+" System %d %s travel=%.1f value=%.1f weight=%.2f%n",
			//			target.sysId, empire.sv.name(target.sysId), source.travelTimeTo(empire.sv.system(target.sysId), warpSpeed),
			//			planetValue(target.sysId), weight);
			//}
		}
	}
	private double autoColonizeWeight(StarSystem source, Target target, float maxDistance, float maxValue, float warpSpeed, boolean autoColonyMultiple, float secondColonyWeight, float distanceWeight) {
		// let's flip value percent and sort by descending order. That's because in rare cases distancePercent could be
		// greater than 1
		float valuePercent = 1 - planetValue(target.sysId) / maxValue;
		float distancePercent = source.travelTimeTo(empire.sv.system(target.sysId), warpSpeed) / maxDistance;
		// default distance is worth 50% of weight, value 50%
		float valueWeight = 1 - distanceWeight;
		float weight = valuePercent * valueWeight + distancePercent * distanceWeight;

		if (autoColonyMultiple && target.alreadyTargeted)
			weight /= (secondColonyWeight + 0.001f);
		return weight;
		// return valuePercent * 0.5 + distancePercent * 0.5;
	}
	private class ScoutPriority implements TargetsSorter	{
		private final float secondScoutWeight;
		private final boolean autoScoutMultiple;
		public ScoutPriority()	{
			GovernorOptions gov = govOptions();
			secondScoutWeight = gov.secondScoutWeightPct();
			autoScoutMultiple = gov.autoScoutSmart() && gov.autoScoutMultiple();
		}
		@Override public void sort(Integer sourceSystem, List<Target> targets, float warpSpeed)	{
			// ok, let's use both distance and value of planet to prioritize colonization, 50% and 50%
			StarSystem source = empire.sv.system(sourceSystem);

			float maxTravelTime = -1;
			for (Target target : targets) {
				float travelTime = source.travelTimeTo(empire.sv.system(target.sysId), warpSpeed);
				if (maxTravelTime < travelTime)
					maxTravelTime = travelTime;
			}
			// could happen if we only have 1 colony ship already orbiting the only remaining colonizable planet
			if (Math.abs(maxTravelTime) <= 0.1)
				maxTravelTime = 1;
			//System.out.println(message+" maxDistance =" + maxDistance + " maxValue=" + maxValue);

			float maxDistance1 = maxTravelTime;
			// planets with lowest weight are most desirable (closest/best)
			targets.sort((s1, s2) -> (int) Math.signum(
					autoScoutWeight(source, s1, maxDistance1, warpSpeed, secondScoutWeight, autoScoutMultiple) -
					autoScoutWeight(source, s2, maxDistance1, warpSpeed, secondScoutWeight, autoScoutMultiple)));
		}
	}
	private double autoScoutWeight(StarSystem source, Target target, float maxDistance, float warpSpeed, float secondScoutWeight, boolean autoScoutMultiple) {
		float weight = source.travelTimeTo(empire.sv.system(target.sysId), warpSpeed) / maxDistance;
		if (autoScoutMultiple && target.alreadyTargeted)
			weight /= (secondScoutWeight + 0.001f);
		return weight;
	}

	/**
	 * Sort fleets by value and to be as close to target system as possible
	 */
	private class ColonizePriority implements SystemsSorter	{
		@SuppressWarnings("unused")
		private final String message;
		public ColonizePriority(String message)	{
			this.message = message;
		}
		@Override public void sort(Integer sourceSystem, List<Integer> targets, float warpSpeed)	{
			// ok, let's use both distance and value of planet to prioritize colonization, 50% and 50%
			StarSystem source = empire.sv.system(sourceSystem);

			float maxTravelTime = -1;
			float maxValue = -1;
			for (int sid : targets) {
				float value = planetValue(sid);
				if (maxValue < 0 || maxValue < value)
					maxValue = value;

				float travelTime = source.travelTimeTo(empire.sv.system(sid), warpSpeed);
				if (maxTravelTime < travelTime)
					maxTravelTime = travelTime;
			}
			// could happen if we only have 1 colony ship already orbiting the only remaining colonizable planet
			if (Math.abs(maxTravelTime) <= 0.1)
				maxTravelTime = 1;
			//System.out.println(message+" maxDistance =" + maxDistance + " maxValue=" + maxValue);

			float maxDistance1 = maxTravelTime;
			float maxValue1 = maxValue;
			// planets with lowest weight are most desirable (closest/best)
			targets.sort((s1, s2) -> (int) Math.signum(
					autocolonizeWeight(source, s1, maxDistance1, maxValue1, warpSpeed) -
					autocolonizeWeight(source, s2, maxDistance1, maxValue1, warpSpeed)));
			//for (int si : targets) {
			//	double weight = autocolonizeWeight(source, si, maxDistance, maxValue, warpSpeed);
			//	System.out.format(message+" System %d %s travel=%.1f value=%.1f weight=%.2f%n",
			//			si, empire.sv.name(si), source.travelTimeTo(empire.sv.system(si), warpSpeed),
			//			planetValue(si), weight);
			//}
		}
	}
	private double autocolonizeWeight(StarSystem source, int targetId, float maxDistance, float maxValue, float warpSpeed) {
		// let's flip value percent and sort by descending order. That's because in rare cases distancePercent could be
		// greater than 1
		float valuePercent = 1 - planetValue(targetId) / maxValue;
		float distancePercent = source.travelTimeTo(empire.sv.system(targetId), warpSpeed) / maxDistance;
		// so distance is worth 50% of weight, value 50%
		float distanceWeight = govOptions().colonyDistanceWeight();
		float valueWeight = 1 - distanceWeight;
		return valuePercent * valueWeight + distancePercent * distanceWeight;
		// return valuePercent * 0.5 + distancePercent * 0.5;
	}
	// taken from AIFleetCommander.setColonyFleetPlan()
	private float planetValue(int sid) {
		float value = empire.sv.currentSize(sid);

		//increase value by 5 for each of our systems it is near, and
		//decrease by 2 for alien systems. This is an attempt to encourage
		//colonization of inner colonies (easier to defend) even if they
		//are not as good as outer colonies
		int[] nearbySysIds = empire.sv.galaxy().system(sid).nearbySystems();
		for (int nearSysId : nearbySysIds) {
			int nearEmpId = empire.sv.empId(nearSysId);
			if (nearEmpId != Empire.NULL_ID) {
				if (nearEmpId == empire.id)
					value += 5;
				else
					value -= 2;
			}
		}
		// assume that we will terraform  the planet
		value += empire.tech().terraformAdj();
		//multiply *2 for artifacts, *3 for super-artifacts
		value *= (1 + empire.sv.artifactLevel(sid));
		if (empire.sv.isUltraRich(sid))
			value *= 3;
		else if (empire.sv.isRich(sid))
			value *= 2;
		else if (empire.sv.isPoor(sid))
			value /= 2;
		else if (empire.sv.isUltraPoor(sid))
			value /= 3;
		return value;
	}

	private void moveScoutAwayFromAlienColonies() {
		List<ShipFleet> allFleets = galaxy().ships.notInTransitFleets(empire.id);
		for (ShipFleet fleet : allFleets) {
			if (fleet == null)
				continue;
			if (!fleet.isOrbiting() || !fleet.canSend())	// we only use idle (orbiting) fleets
				continue;
			StarSystem sys = fleet.system();
			if (!sys.isColonized())
				continue;
			Empire sysEmp = sys.empire();
			if (empire != sysEmp && fleet.isAutoScoutOnly() && !empire.atWarWith(sysEmp.id)) {
				StarSystem destSys = empire.withDrawFromSystem(sys);
				if (destSys != null)
					galaxy().ships.deployFleet(fleet, destSys.id);
			}
		}
	}
	private List<Integer> filterTargets(Predicate<Integer> filterFunction)	{
		List<Integer> targets = new LinkedList<>();
		for (int i = 0; i < empire.sv.count(); ++i) {
			if (filterFunction.test(i)) {
				targets.add(i);
			}
		}
		return targets;
	}
	private List<Target> getScoutTargets(SubFleetList subFleetList, boolean hasExtendedRange, int defaultTimeLimit)	{
		int timeToBest = 0;
		GovernorOptions gov = govOptions();;
		if (gov.autoScoutSmart() && gov.autoScoutMultiple())
			timeToBest = gov.autoScoutSaveTime()+1; // +1 because it will be a >=
		List<Target> targets = new ArrayList<>();
		SystemInfo sv = empire.sv;
		for (int i = 0; i < sv.count(); ++i) {
			Integer timeLimit = testScoutTarget(i, subFleetList, hasExtendedRange, timeToBest, defaultTimeLimit);
			if (timeLimit != null)
				targets.add(new Target (i, timeLimit, timeLimit<defaultTimeLimit));
		}
		return targets;
	}
	private Integer testScoutTarget(int sysId, SubFleetList subFleetList, boolean extendedRange, int timeToBest, int timeLimit)	{
		Integer bestTime = timeLimit; // Best time to reach this planet
		// scout time only gets set for scouted systems, not ones we were forced to retreat from, don't use scout time
		SystemInfo ev = empire.sv;
		SystemView sysView = ev.view(sysId);
		if (sysView.empire() != null			// already owned by another empire
				|| sysView.scouted()			// known system
				|| ev.isGuarded(sysId))	// already known to be owned
			return null; 

		boolean inRange = extendedRange? ev.inScoutRange(sysId) : ev.inShipRange(sysId);
		if (!inRange)
			return null;

		// ships already on route- no need to send more?
		StarSystem sys = ev.system(sysId);
		for (ShipFleet fl : empire.ownFleetsTargetingSystem(sys))
			if (fl != null) {
				int time = fl.travelTurnsRemainingAdjusted();
				if (time <= timeToBest)
					return null; // No need to try to best this one
				if (time < bestTime)
					bestTime = time;
			}

		// ships already deployed to - no need to send more // BR: For redeployment
		for (ShipFleet sf : empire.ownFleetsDeployedToSystem(sys))
			if (sf != null) {
				int time = sf.travelTurnsRemainingAdjusted();
				if (time <= timeToBest)
					return null;  // No need to try to best this one
				if (time < bestTime)
					bestTime = time;
			}
		return bestTime;
	}
	private void autoscout()	{
		GovernorOptions gov = session().getGovernorOptions();
		if (!gov.isAutoScout())
			return;
		ParamFleetAuto rules = IGovOptions.fleetAutoScoutMode;
		boolean smart = gov.autoScoutSmart();

		if (!smart) {
			SubFleetList subFleetList = filterFleets(rules, false);
			if (subFleetList.isEmpty())
				return;
			subFleetList.sortByWarpSpeed();
			autoscout(rules, subFleetList, 9999, 9999);
			return;
		}

		int maxTime = gov.autoScoutMaxTime()+1; // +1 because it will be a >=
		if (maxTime == 1) // No limit
			maxTime = 9999;

		// Send From all Systems with limits
		SubFleetList subFleetList = filterFleets(rules, false);
		if (subFleetList.isEmpty())
			return;
		subFleetList.sortByWarpSpeed();
		autoscout(rules, subFleetList, maxTime, maxTime);

		// Send From colonized world without limit
		subFleetList = filterFleets(rules, true);
		if (subFleetList.isEmpty())
			return;
		subFleetList.sortByWarpSpeed();
		autoscout(rules, subFleetList, 9999, 9999);
	}
	private void autoscout(ParamFleetAuto rules, SubFleetList subFleetList, int inTurn, float goTurn)	{
		boolean hasExtendedRange = subFleetList.hasExtendedRange();
		List<Target> targets = getScoutTargets(subFleetList, hasExtendedRange, inTurn);
		// No systems to scout
		if (targets.isEmpty())
			return;

		// shuffle toScout list. empire is to prevent colony ship with auto-scouting on from going directly to the habitable planet.
		// That's because AFAIK map generation sets one of the 2 nearby planets to be habitable, and it's always first one in the list
		// So if we keep default order, that's cheating
		Collections.shuffle(targets);

		// don't send out armed scout ships when enemy fleet is incoming, hence the need for defend predicate
		autoSendFleets(targets, new ScoutPriority(), subFleetList, rules, goTurn);
	}
	record Target(int sysId, int timeToBest, boolean alreadyTargeted) {}
	/**
	 * Search for the worthy targets
	 * @param subFleetList available fleets
	 * @param hasExtendedRange Best fleet range in the list
	 * @param sendToGuarded Allow sending colony to guarded planets
	 * @param defaultTimeLimit default time to best
	 * @return List of worthy targets
	 */
	private List<Target> getColonyTargets(SubFleetList subFleetList, boolean hasExtendedRange, boolean sendToGuarded, int defaultTimeLimit)	{
		int timeToBest = 0;
		GovernorOptions gov = govOptions();;
		if (gov.autoColonizeTuned() && gov.autoColonizeMultiple())
			timeToBest = gov.autoColonizeSaveTime();
		List<Target> targets = new ArrayList<>();
		SystemInfo sv = empire.sv;
		for (int i = 0; i < sv.count(); ++i) {
			Integer timeLimit = testColonyTarget(i, subFleetList, hasExtendedRange, sendToGuarded, timeToBest, defaultTimeLimit);
			if (timeLimit != null)
				targets.add(new Target (i, timeLimit, timeLimit<defaultTimeLimit));
		}
		return targets;
	}
	/**
	 * Apply all the filters to validate if the planet meat the criteria
	 * 
	 * @param sysId Target system Id
	 * @param subFleetList Colony ships tha can be sent
	 * @param extendedRange Best fleet range in the list
	 * @param sendToGuarded Allow sending colony to guarded planets
	 * @param timeToBest For already sent fleets
	 * @param timeLimit default time to best
	 * @return best arrival time, required by a fleet to be worthy.
	 */
	private Integer testColonyTarget(int sysId, SubFleetList subFleetList, boolean extendedRange, boolean sendToGuarded, int timeToBest, int timeLimit)	{
		Integer bestTime = timeLimit; // Best time to reach this planet
		// only colonize scouted systems, systems with planets
		// attempt to colonize systems already owned by someone if allowed
		SystemInfo ev = empire.sv;
		SystemView sysView = ev.view(sysId);
		if (sysView.empire() != null			// already owned by another empire
				|| !sysView.scouted()			// Unknown system
				|| ev.isGuarded(sysId)			// There is an Monster
				|| !empire.canColonize(sysId))	// Empire don't have the technology
			return null; 

		StarSystem sys = ev.system(sysId);
		PlanetType type = sys.planet().type();
		// if we don't have tech or ships to colonize empire planet, ignore it.
		// Since 2.15, for a game with restricted colonization option, we have to check each design if it can colonize
		if ((!empire.ignoresPlanetEnvironment()				// Do not ignores Environment
				|| !empire.acceptedPlanetEnvironment(type))	// nor limited to this type
				&& !subFleetList.canColonize(type))			// nor have an colony ship of this kind
			return null;

		boolean inRange = extendedRange? ev.inScoutRange(sysId) : ev.inShipRange(sysId);
		if (!inRange)
			return null;

		// colony ship already on route- no need to send more?
		for (ShipFleet sf : empire.ownFleetsTargetingSystem(sys))
			if (sf != null && sf.canColonizeSystem(sys)) {
				int time = sf.travelTurnsRemainingAdjusted();
				if (time <= timeToBest)
					return null; // No need to try to best this one
				if (time < bestTime)
					bestTime = time;
			}

		// ships already deployed to - no need to send more // BR: For redeployment
		for (ShipFleet sf : empire.ownFleetsDeployedToSystem(sys))
			if (sf != null && sf.canColonizeSystem(sys)) {
				int time = sf.travelTurnsRemainingAdjusted();
				if (time <= timeToBest)
					return null;  // No need to try to best this one
				if (time < bestTime)
					bestTime = time;
			}

		// colony ship already on orbiting
		List<ShipFleet> orbitingFleets = sysView.orbitingFleets();
		if (orbitingFleets != null && !orbitingFleets.isEmpty()) {
			//	System.out.println("System "+i+" "+empire.sv.descriptiveName(i)+" has ships in orbit");
			for (ShipFleet sf: orbitingFleets)
				if (sf != null) {
					if (sf.empId() == empire.id && sf.canColonizeSystem(sys))
						return null;
					// if fleet isn't armed- ignore it
					if (sendToGuarded)
						continue;
					if (!sf.isArmed())
						continue;
					// if fleet belongs to allied/non-aggression pact empire- ignore it
					if (empire.pactWith(sf.empId()) || empire.alliedWith(sf.empId()))
						continue;
					// don't send colony to systems guarded by by armed enemy.
					// System.out.println("System "+i+" "+empire.sv.descriptiveName(i)+" has armed enemy ships in orbit");
					return null;
				}
		}
		return bestTime;
	}
	private void autocolonize()	{
		GovernorOptions gov = session().getGovernorOptions();
		if (!gov.isAutoColonize())
			return;
		ParamFleetAuto rules = IGovOptions.fleetAutoColonizeMode;
		boolean tuned = gov.autoColonizeTuned();
		boolean fight = gov.armedColonizerFight();

		if (!tuned) {
			SubFleetList subFleetList = filterFleets(rules, false);
			if (subFleetList.isEmpty())
				return;
			subFleetList.sortForColonize();
			autocolonize(rules, subFleetList, fight, 9999, 9999);
			return;
		}

		int maxTime = gov.autoColonizeMaxTime()+1; // +1 because it will be a >=
		if (maxTime == 1) // No limit
			maxTime = 9999;

		SubFleetList subFleetList = filterFleets(rules, false);
		if (subFleetList.isEmpty())
			return;
		subFleetList.sortForColonize();
		autocolonize(rules, subFleetList, fight, maxTime, maxTime);
	}
	/**
	 * Find the best planet and dispatch the fleets
	 * @param rules Fleets options
	 * @param subFleetList List of fleets to be dispatched
	 * @param sendToGuarded Allow sending colony to guarded planets
	 * @param inTurn Distance filter for target
	 * @param goTurn Distance filter for fleets
	 */
	private void autocolonize(ParamFleetAuto rules, SubFleetList subFleetList, boolean sendToGuarded, int inTurn, float goTurn)	{
		boolean hasExtendedRange = subFleetList.hasExtendedRange();
		List<Target> targets = getColonyTargets(subFleetList, hasExtendedRange, sendToGuarded, inTurn);
		// No systems to colonize
		if (targets.isEmpty())
			return;
		autoSendFleets(targets, new ColonyPriority(), subFleetList, rules, goTurn);
	}

	// similar to autocolonize. Send ships to enemy planets and systems with enemy ships in orbit
	private void autoattack()	{
		GovernorOptions options = session().getGovernorOptions();
		if (!options.isAutoAttack())
			return;

		ParamFleetAuto rules = IGovOptions.fleetAutoAttackMode;
		SubFleetList subFleetList = filterFleets(rules, false);
		if (subFleetList.isEmpty())
			return;
		subFleetList.sortForAttack();

		boolean extendedRange = subFleetList.hasExtendedRange();
		List<Integer> hostileEmpires = IGovOptions.autoAttackEmpire.targetEmpires(empire);

		List<Integer> targets = filterTargets(sysId -> {
			// consider both scouted and unscouted systems if they belong to the enemy
			boolean inRange;
			if (extendedRange)
				inRange = empire.sv.inScoutRange(sysId);
			else
				inRange = empire.sv.inShipRange(sysId);
			if (!inRange)
				return false;

			List<ShipFleet> fleets = empire.sv.orbitingFleets(sysId);
			if (fleets != null) {
				for (ShipFleet sf: fleets) {
					if (sf != null && sf.empire() == empire && sf.isArmed()) {
						// don't target planets which already have own armed fleets in orbit
						return false;
					}
				}
			}
			// armed ships already on route- no need to send more
			for (ShipFleet sf: empire.ownFleetsTargetingSystem(empire.sv.system(sysId)))
				if (rules.alreadyHasDesignsOnRoute(sf, sysId))
					return false; // attack fleet already on its way, don't send more

			if (empire.sv.empire(sysId) != null && hostileEmpires.contains(empire.sv.empire(sysId).id)) {
				//System.out.println("System "+empire.sv.name(i)+" belongs to enemy empire, targeting");
				return true;
			}
			// empire will send ships to own colonies that have enemy ships in orbit. I guess that's OK
			if (fleets != null) {
				for (ShipFleet sf: fleets) {
					if (sf != null && hostileEmpires.contains(sf.empId()) && !sf.retreating()) {
						//System.out.println("System "+empire.sv.name(i)+" has enemy ships, targeting");
						return true;
					}
				}
			}
			return false;
		});

		// No systems to colonize
		if (targets.isEmpty())
			return;

		//for (Integer i: targets)
		//	System.out.println("ToAttack "+empire.sv.name(i) + " scouted="+empire.sv.view(i).scouted()+" extrange=" + empire.sv.inScoutRange(i) + " range=" + empire.sv.inShipRange(i));

		autoSendShips(targets, new ColonizePriority("toAttack"), subFleetList, rules, 999);
	}
	/**
	 * Identify the list of available fleets
	 * @param rules To identify the kind of fleets
	 * @param fromColonyOnly To limit the origin of the fleet
	 * @return The list of available fleets.
	 */
	private SubFleetList filterFleets(ParamFleetAuto rules, boolean fromColonyOnly)	{
		SubFleetList subFleetList = rules.newSubFleetList(empire);
		List<ShipFleet> allFleets = galaxy().ships.notInTransitFleets(empire.id);
		for (ShipFleet fleet : allFleets) {
			if (fleet == null)
				continue;
			if (!fleet.isOrbiting() || !fleet.canSend())	// we only use idle (orbiting) fleets
				continue;
			if (fromColonyOnly && !fleet.system().isColonized())
				continue;
			subFleetList.splitAndAdd(fleet);
		}
		return subFleetList;
	}
	private boolean alreadyHasDesignsOrbiting(int sysId, ParamFleetAuto rules)	{
		// if target system already has a fitting ship in orbit, don't send new ships there
		ShipFleet orbitingFleet = empire.sv.system(sysId).orbitingFleetForEmpire(empire);
		if (orbitingFleet != null)
			return rules.alreadyHasDesignsOrbiting(orbitingFleet, sysId);
		return false;
	}
	private void autoSendShips(List<Integer> targets,
							  SystemsSorter systemsSorter,
							  SubFleetList subFleetList,
							  ParamFleetAuto rules,
							  float maxTravelTime)	{

		if (subFleetList.isEmpty())
			return;

		if (targets.size() > subFleetList.size()) {
			// System.out.println("MORE TARGET SYSTEMS THAN SHIPS");
			// we have more stars to explore than we have ships, so
			// we take ships and send them to closest systems.
			for (Iterator<SubFleet> iFleet = subFleetList.iterator(); iFleet.hasNext(); ) {
				SubFleet subFleet = iFleet.next();
				if (targets.isEmpty())
					break;

				if (subFleet != null) {
					// don't send same fleet to multiple destinations by mistake
					if (!subFleet.fleet().isOrbiting())
						continue;
					// System.out.println("Deploying ships from Fleet " + fleet + " " + fleet.system().name());
					float warpSpeed = subFleet.warpSpeed();
					systemsSorter.sort(subFleet.fleet().sysId(), targets, warpSpeed);

					for (Iterator<Integer> iTarget = targets.iterator(); iTarget.hasNext(); ) {
						int sysId = iTarget.next();
						if (alreadyHasDesignsOrbiting(sysId, rules) || !rules.fitForSystem(subFleet, sysId))
							continue;
						float travelTime = subFleet.fleet().travelTimeTo(empire.sv.system(sysId), warpSpeed);
						if (travelTime > maxTravelTime)
							continue;
						boolean deployed = false;
						if (!subFleet.isExtendedRange() && empire.sv.inShipRange(sysId))
							deployed = deploy(subFleet, sysId);	// deploy
						else if (subFleet.isExtendedRange() && empire.sv.inScoutRange(sysId)) 
							deployed = deploy(subFleet, sysId);	// deploy
						if (deployed) {
							iTarget.remove();	// remove empire system as it has a ship assigned already
							iFleet.remove();
							break; // Go to next fleet
						}
					}
				}
			}
		}
		else {
			// System.out.println("MORE SHIPS THAN TARGET SYSTEMS");
			// We sort target systems by distance from home as the starting point
			float warpSpeed = subFleetList.minWarpSpeed();
			// System.out.println("Warp Speed "+warpSpeed);
			systemsSorter.sort(empire.homeSysId(), targets, warpSpeed);

			for (Iterator<Integer> iTarget = targets.iterator(); iTarget.hasNext(); ) {
				int sysId = iTarget.next();
				if (alreadyHasDesignsOrbiting(sysId, rules))
					continue;
				// System.out.println("Finding fleets for system " + empire.sv.name(si)+" "+empire.sv.descriptiveName(si));
				subFleetList.sortByTimeToSys(sysId);

				for (Iterator<SubFleet> iFleet = subFleetList.iterator(); iFleet.hasNext(); ) {
					SubFleet subFleet = iFleet.next();
					// if fleet was sent elsewhere during previous iteration, don't redirect it again
					if (!rules.fitForSystem(subFleet, sysId))
						continue;
					float travelTime = subFleet.fleet().travelTimeTo(empire.sv.system(sysId), warpSpeed);
					if (travelTime > maxTravelTime)
						continue;

					boolean deployed = false;
					if (!subFleet.isExtendedRange() && empire.sv.inShipRange(sysId))
						deployed = deploy(subFleet, sysId);	// deploy
					else if (subFleet.isExtendedRange() && empire.sv.inScoutRange(sysId)) 
						deployed = deploy(subFleet, sysId);	// deploy
					if (deployed) {
						iTarget.remove();	// remove empire system as it has a ship assigned already
						iFleet.remove();
						break; // go to next target
					}
				}
			}
		}
	}

	private void autoSendFleets(List<Target> targets,
							  TargetsSorter targetsSorter,
							  SubFleetList subFleetList,
							  ParamFleetAuto rules,
							  float maxTravelTime)	{

		if (subFleetList.isEmpty())
			return;

		if (targets.size() > subFleetList.size()) {
			// System.out.println("MORE TARGET SYSTEMS THAN SHIPS");
			// we have more stars to explore than we have ships, so
			// we take ships and send them to closest systems.
			for (Iterator<SubFleet> iFleet = subFleetList.iterator(); iFleet.hasNext(); ) {
				SubFleet subFleet = iFleet.next();
				if (targets.isEmpty())
					break;

				if (subFleet != null) {
					// don't send same fleet to multiple destinations by mistake
					if (!subFleet.fleet().isOrbiting())
						continue;
					// System.out.println("Deploying ships from Fleet " + fleet + " " + fleet.system().name());
					float warpSpeed = subFleet.warpSpeed();
					targetsSorter.sort(subFleet.fleet().sysId(), targets, warpSpeed);

					for (Iterator<Target> iTarget = targets.iterator(); iTarget.hasNext(); ) {
						Target target = iTarget.next();
						int sysId = target.sysId;
						if (alreadyHasDesignsOrbiting(sysId, rules) || !rules.fitForSystem(subFleet, sysId))
							continue;
						int travelTime = ceil(subFleet.fleet().travelTimeTo(empire.sv.system(sysId), warpSpeed));
						if (travelTime >= target.timeToBest)
							continue;
						boolean deployed = false;
						if (!subFleet.isExtendedRange() && empire.sv.inShipRange(sysId))
							deployed = deploy(subFleet, sysId);	// deploy
						else if (subFleet.isExtendedRange() && empire.sv.inScoutRange(sysId)) 
							deployed = deploy(subFleet, sysId);	// deploy
						if (deployed) {
							iTarget.remove();	// remove empire system as it has a ship assigned already
							iFleet.remove();
							break; // Go to next fleet
						}
					}
				}
			}
		}
		else {
			// System.out.println("MORE SHIPS THAN TARGET SYSTEMS");
			// We sort target systems by distance from home as the starting point
			float warpSpeed = subFleetList.minWarpSpeed();
			// System.out.println("Warp Speed "+warpSpeed);
			targetsSorter.sort(empire.homeSysId(), targets, warpSpeed);

			for (Iterator<Target> iTarget = targets.iterator(); iTarget.hasNext(); ) {
				Target target = iTarget.next();
				int sysId = target.sysId;
				if (alreadyHasDesignsOrbiting(sysId, rules))
					continue;
				// System.out.println("Finding fleets for system " + empire.sv.name(si)+" "+empire.sv.descriptiveName(si));
				subFleetList.sortByTimeToSys(sysId);

				for (Iterator<SubFleet> iFleet = subFleetList.iterator(); iFleet.hasNext(); ) {
					SubFleet subFleet = iFleet.next();
					// if fleet was sent elsewhere during previous iteration, don't redirect it again
					if (!rules.fitForSystem(subFleet, sysId))
						continue;
					int travelTime = ceil(subFleet.fleet().travelTimeTo(empire.sv.system(sysId), warpSpeed));
					if (travelTime >= target.timeToBest)
						continue;

					boolean deployed = false;
					if (!subFleet.isExtendedRange() && empire.sv.inShipRange(sysId))
						deployed = deploy(subFleet, sysId);	// deploy
					else if (subFleet.isExtendedRange() && empire.sv.inScoutRange(sysId)) 
						deployed = deploy(subFleet, sysId);	// deploy
					if (deployed) {
						iTarget.remove();	// remove empire system as it has a ship assigned already
						iFleet.remove();
						break; // go to next target
					}
				}
			}
		}
	}
	private boolean deploy(SubFleet subFleet, int target)	{
		if (subFleet.entireFleet())
			galaxy().ships.deployFleet(subFleet.fleet(), empire.sv.system(target).id);
		else
			return galaxy().ships.deploySubfleet(subFleet.fleet(), subFleet.shipCounts(), empire.sv.system(target).id);
		return true;
	}
}
