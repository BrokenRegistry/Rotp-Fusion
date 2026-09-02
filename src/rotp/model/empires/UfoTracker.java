package rotp.model.empires;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import rotp.model.galaxy.Ship;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.galaxy.Transport;
import rotp.model.ships.ShipDesign;

public final class UfoTracker {
	private final Empire empire;

	UfoTracker (Empire e)	{ empire = e; }

	Map<Ship, Ship> matchShipsSeenThisTurnToShipsSeenLastTurn(List<Ship> visibleShips, Set<Ship> shipsVisibleLastTurnDestroyed) {
		// This function attempts to match ships seen last turn with ships seen this turn (to determine trajectories).
		// Obviously, we have the object references in hand, so we could just compare their identities.
		// But the point is to find out whether *the empire* can do that using only the information available.
		// The Map<Ship, Ship> will be a bit funny-looking because for any Ship in the keySet, the value will always be itself.
		// To save an allocation, the set of *last* turn's ships is destroyed as we go.
		Map<Ship, Ship> ret = new HashMap<>();
		for (Ship ufo : visibleShips) {
			if (ufo == null)
				continue;
			if (!knowsShipNotBuiltThisTurn(ufo))
				continue;
			if (!knowsShipCouldNotHaveFlownInFromOutsideScanRange(ufo))
				continue;
			// If it definitely wasn't built this turn, and it definitely wasn't outside scan range last turn, then
			// it must have been seen last turn. The question then becomes uniquely identifying it.
			boolean foundMatch = false;
			for (Ship ufoLastTurn : shipsVisibleLastTurnDestroyed)
				if (ufoLastTurn != null && !knowsCouldNotHaveBeenSameShipLastTurn(ufo, ufoLastTurn))
					if (foundMatch) {
						foundMatch = false;
						ret.remove(ufo);
						break;
					}
					else {
						foundMatch = true;
						ret.put(ufo, ufoLastTurn);
					}
			if (foundMatch)
				shipsVisibleLastTurnDestroyed.remove(ret.get(ufo));
		}
		return ret;
	}
	Map<Ship, StarSystem> suspectedDestinationsOfShipsSeenLastTurn(Set<Ship> shipsKnowLastTurnLocationOf) {
		Map<Ship, StarSystem> suspectedDestinations = new HashMap<>();
		for (Ship sh : shipsKnowLastTurnLocationOf) {
			if (sh != null) {
				StarSystem suspectedDestination = starSystemInLineIfAny(sh.transitXlastTurn(), sh.transitYlastTurn(), sh.transitX(), sh.transitY());
				if (suspectedDestination != null)
					// If the Ship arrived at a system this turn and is currently in orbit, then it can be useful to retain that system as its destination.
					// However, if the Ship arrived at the system this turn and immediately retreated or deployed from the system,
					// in terms of xy coordinates that looks exactly like arriving at the system and staying in orbit,
					// but in that case it is impossible to know its new destination (without Improved Space Scanner).
					if (!knowsShipIsLeavingSystemThisTurn(sh, suspectedDestination))
						suspectedDestinations.put(sh, suspectedDestination);
			}
		}
		return suspectedDestinations;
	}

	private boolean knowsLastTurnLocationOf(Ship sh) { return empire.suspectedDestinationsOfVisibleShips().containsKey(sh); }
	private boolean knowsCouldNotHaveBeenSameShipLastTurn(Ship ufoNow, Ship ufoLastTurn) {
		if (ufoNow.empire().ownershipColor() != ufoLastTurn.empire().ownershipColor())
			return true;
		if (ufoNow.isTransport() != ufoLastTurn.isTransport())
			return true;
		if (ufoNow instanceof ShipFleet && ufoLastTurn instanceof ShipFleet)
			return knowsCouldNotHaveBeenSameFleetLastTurn((ShipFleet)ufoNow, (ShipFleet)ufoLastTurn);
		if (ufoNow instanceof Transport && ufoLastTurn instanceof Transport)
			return knowsCouldNotHaveBeenSameTransportLastTurn((Transport)ufoNow, (Transport)ufoLastTurn);
		return false;
	}
	private boolean knowsTransportCouldNotHaveReachedThisLocation(Transport ufoNow, Transport ufoLastTurn) {
		float maxPossibleTravelSpeed = maxSpeedTransportMightHave(ufoNow);
		float deltaX = ufoNow.x() - ufoLastTurn.transitXlastTurn();
		float deltaY = ufoNow.y() - ufoLastTurn.transitYlastTurn();
		float squaredDistanceMoved = deltaX*deltaX + deltaY*deltaY;
		return squaredDistanceMoved > maxPossibleTravelSpeed*maxPossibleTravelSpeed*1.125; // allow a fudge factor
	}
	private boolean knowsFleetCouldNotHaveReachedThisLocation(ShipFleet ufoNow, ShipFleet ufoLastTurn) {
		if (!ufoLastTurn.inTransit())
			// If the Ship seen last turn arrive()d this turn, then we immediately give up.
			// Note that the Empire does not know whether the Ship seen last turn is now in orbit,
			// (matching ships seen last turn to ships seen this turn is exactly what the Empire is trying to do),
			// but also note that this is entirely 100% about not wanting to store the xy coordinates of ship-sightings.
			// The squinting-at-screenshots method absolutely *can* match up ships under this condition;
			// whether the Ship seen last turn is still in transit would be 100% irrelevant *except* that we don't want to store more data.
			return false;
		float maxPossibleTravelSpeed = maxSpeedFleetMightHave(ufoNow);
		// If we wanted this function to have broader uses,
		// we could call maxSpeedFleetMightHave() on both Ships and take the minimum of the two,
		// but in the context of trying to distinguish ships from each other,
		// if the two UFOs are so obviously different that different amounts are known about their possible speeds,
		// then they'll immediately be known distinct and this function will never be called.
		float deltaX = ufoNow.x() - ufoLastTurn.transitXlastTurn();
		float deltaY = ufoNow.y() - ufoLastTurn.transitYlastTurn();
		float squaredDistanceMoved = deltaX*deltaX + deltaY*deltaY;
		for (float speed = minSpeedFleetMightHave(ufoNow); speed < maxPossibleTravelSpeed + 1; speed += 1) {
			float difference = squaredDistanceMoved - speed*speed;
			if (difference*difference < 1.125*squaredDistanceMoved)
				// If this is a possible speed the fleet could be moving at, and that speed would fit this location,
				// then this empire cannot rule out this fleet reaching this location.
				return false;
		}
		return true;
	}
	private boolean knowsCouldNotHaveBeenSameFleetLastTurn(ShipFleet fleetNow, ShipFleet fleetLastTurn) {
		if (!visibleShipViews(fleetNow).equals(visibleShipViews(fleetLastTurn)))
			// This defers to ShipView.equals() for the ships that have ShipViews,
			// and compares the count of each as well as the count of all ships that lack ShipViews.
			// Strictly speaking, we could implement ShipView.equals(), but letting ShipView inherit .equals from Object works,
			// because two ShipViews are always distinguishable.
			return true;
		if (knowsFleetCouldNotHaveReachedThisLocation(fleetNow, fleetLastTurn))
			return true;
		return false;
	}
	private boolean knowsCouldNotHaveBeenSameTransportLastTurn(Transport transportNow, Transport transportLastTurn) {
		// There is no concept of a ShipView for transports, but size is always visible.
		if (transportNow.size() != transportLastTurn.size())
			return true;
		if (knowsTransportCouldNotHaveReachedThisLocation(transportNow, transportLastTurn))
			return true;
		return false;
	}
	private Map<ShipView, Integer> visibleShipViews(ShipFleet fleet) {
		// shipViewFor(design) might be null because design is null, or it might be null when there is a design but we have no ShipView for that design.
		Map<ShipDesign, Integer> designs = fleet.visibleShipDesigns(empire.id);
		int numberOfShipsWithMissingDesign = 0;
		if (designs.containsKey(null))
			numberOfShipsWithMissingDesign = designs.get(null);
		Map<ShipView, Integer> ret = designs.entrySet().stream().filter(Objects::nonNull)
											.collect(Collectors.toMap(
			entry -> empire.shipViewFor(entry.getKey()),
			Map.Entry::getValue,
			// Although we separately dealt with null designs, we can still have multiple null ShipViews.
			// We agglomerate those all together and save the count in case we want it.
			// (Collectors.toMap will complain of "Duplicate key null" if we don't include some kind of mergeFunction)
			Integer::sum
		));
		if (numberOfShipsWithMissingDesign > 0)
			ret.put(null, ret.get(null) + numberOfShipsWithMissingDesign);
		return ret;
	}
	private static boolean knowsShipNotBuiltThisTurn(Ship ufo) {
		// If the ship does not have the same coordinates as any star --- that is,
		// if it's in deep space, not deploying from a star system,
		// then it could not have been built this turn.
		// Ship already stores that information, so we can just reference it.
		return ufo.inTransit() || ufo.retreating();
		// The fact that a ship is in a star system owned by someone else does not necessarily prove that it was not built that turn,
		// because ships are built before invasions are resolved, and incoming transports could have run the gauntlet of the ships's weapons
		// and conquered the planet out from under it.
		// BR: if already tagged as retreating, then they are known by the player.
	}
	private float maxSpeedShipMightHave(Ship ufo) {
		if (knowsLastTurnLocationOf(ufo))
			// To save on compute, we don't actually store the location of each UFO last turn.
			// We only store the list of which UFOs the empire knows last turn's location of,
			// and query the actual location (hence the actual speed).
			return ufo.travelSpeed();
		if (ufo instanceof ShipFleet)
			return maxSpeedFleetMightHave((ShipFleet)ufo);
		if (ufo instanceof Transport)
			return maxSpeedTransportMightHave((Transport)ufo);
		return 9;
	}
/*	public float minSpeedShipMightHave(Ship ufo) {
		if (knowsLastTurnLocationOf(ufo))
			// To save on compute, we don't actually store the location of each UFO last turn.
			// We only store the list of which UFOs the empire knows last turn's location of,
			// and query the actual location (hence the actual speed).
			return ufo.travelSpeed();
		if (ufo instanceof ShipFleet)
			return minSpeedFleetMightHave((ShipFleet)ufo);
		if (ufo instanceof Transport)
			return minSpeedTransportMightHave((Transport)ufo);
		return 0.125f;
	} */
	private float maxSpeedFleetMightHave(ShipFleet fleet) {
		return fleet.visibleShipDesigns(empire.id).keySet().stream()
					.map(design -> empire.shipViewFor(design))
					.map(view -> (view == null) ? 9 : view.maxPossibleWarpSpeed())
					.min(Comparator.<Float>naturalOrder()).get();
	}
	private float minSpeedFleetMightHave(ShipFleet fleet) {
		return fleet.visibleShipDesigns(empire.id).keySet().stream()
					.map(design -> empire.shipViewFor(design))
					.map(view -> (view == null) ? 1 : view.minPossibleWarpSpeed())
					.min(Comparator.<Float>naturalOrder()).get();
	}
	private float maxSpeedTransportMightHave(Transport transport) {
		EmpireView empireView = empire.viewForEmpire(transport.empire());
		if (empireView == null)
			return 8;
		SpyNetwork spies = empireView.spies();
		// No matter how fast you research, it is impossible to discover more than one propulsion tech per turn.
		// But nevertheless someone could have researched Hyper Drives without even having researched Nuclear Engines first.
		// propulsion().maxKnownQuintile() cannot increase more than one per turn.
		// But nevertheless there might be an unencountered empire which researched Hyper Drives and traded it to the encountered empire.
		// So the only way to be *sure* is if the report is current.
		if (spies.reportAge() == 0)
			return spies.tech().transportTravelSpeed();
		return 8;
	}
/*	private float minSpeedTransportMightHave(Transport transport) {
		// We can never be sure a transport wasn't launched a long, long time ago.
		// Actually, due to transport syncing, can we ever have any lower bound on transport speed?
		return 0.125f;
	}*/
	private boolean knowsShipCouldNotHaveFlownInFromOutsideScanRange(Ship ufo) {
		// For simplicity, we completely ignore scan coverage from ships.
		return empire.distanceTo(ufo) + maxSpeedShipMightHave(ufo) < empire.planetScanningRange();
	}
	/**
	 * This function uses only information available to the empire.
	 * This function should never be used to for game purposes, because when systems are collinear the answer it returns can be,
	 * and sometimes will be, wrong.
	 */
	private StarSystem starSystemInLineIfAny(float firstX, float firstY, float secondX, float secondY) {
		// BR: fixed wrong destination when retreating.
		// Fixed crash on vertical or horizontal travels
		// Fixed transport without destinations
		final float strideMin = 0.05f; // To avoid division by something to small to be significant
		final float maxYears = 8f;
		final float offsetMax = 0.05f;
		float strideX = secondX - firstX;
		float strideY = secondY - firstY;
		StarSystem target = null;
		float minDistance = Float.MAX_VALUE;
		
		if (Math.abs(strideX) > strideMin) {
			for (StarSystem sys: empire.allColonizedSystems()) {
				float years = (sys.x()-secondX)/strideX;
				// Negative years = wrong direction
				// years > maxYears means to far away
				if (years < 0 || years > maxYears)
					continue;
				float dist = Math.abs(sys.y() - (secondY + years * strideY));
				if (dist < offsetMax)
					if (dist < minDistance) {
						target = sys;
						minDistance = dist;
					}
			}
		}
		else if (Math.abs(strideY) >  strideMin) {
			for (StarSystem sys: empire.allColonizedSystems()) {
				float years = (sys.y()-secondY)/strideY;
				// Negative years = wrong direction
				// years > maxYears means to far away
				if (years < 0 || years > maxYears)
					continue;
				float dist = Math.abs(sys.x() - (secondX + years * strideX));
				if (dist < offsetMax)
					if (dist < minDistance) {
						target = sys;
						minDistance = dist;
					}
			}
		}
		return target;
		
		//final int MAX_LOOKAHEAD = 8; // just to ensure we don't somehow infinite-loop
		// Typically, minLookahead is 0.
		// We start at 0 just so that we store the "destination" of a stationary ship in orbit as its location, for completeness.
		// This allows e.g. knowing whether we've been tracking a ship across turns just by checking whether it's in suspectedDestinationsOfVisibleShips.
		// However, a ship might have arrived at a system this turn *and retreated*.
		// In that case, its coordinates will be the coordinates of the system, and its previous-turn coordinates will be (obviously) 1 turn away from that system ---
		// but it's not in orbit at that system! It's headed *away* from that system!
		// But we don't check for that in this function, because in that case, in fact, it is *impossible* to guess its destination (using only Deep Space Scanner).
		// So instead, at a higher level, those cases are dropped.
//		for (int i=0; i<MAX_LOOKAHEAD; i++)
//			for (StarSystem sys: empire.allColonizedSystems())
//				// Strictly speaking, there's no reason the suspected-destination system needs to be colonized;
//				// it doesn't even need to be in scanner range. But this saves time and it's going to be rare for
//				// a ship to be observed far outside the empire when Improved Space Scanner is not researched.
//				// BR: fixed retreating fleets wrong destination
//				// BR: it worth nothing to start from the previous location as it could be the origin System
//				//	if (sys != null && 
//				//	Math.abs((sys.x() - firstX - i*strideX)/strideX) < 0.125 &&
//				//	Math.abs((sys.y() - firstY - i*strideY)/strideY) < 0.125)
//				if (sys != null && 
//						Math.abs((sys.x() - secondX - i*strideX)/strideX) < 0.125f &&
//						Math.abs((sys.y() - secondY - i*strideY)/strideY) < 0.125f)
//					return sys;
//		return null;
	}
	private static boolean knowsShipIsLeavingSystemThisTurn(Ship sh, StarSystem sys) {
		// The fact of whether a ship is retreating or not is always visible to the player if the ship itself is visible.
		// Similarly, the player can always see that a ship has been deployed (that is, is no longer in orbit).
		return sh.x() == sys.x() && sh.y() == sys.y() && (sh.retreating() || sh.isDeployed());
	}
}
