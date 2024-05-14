package rotp.ui.console;

import java.util.ArrayList;
import java.util.List;

import rotp.model.empires.Empire;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.ui.RotPUI;
import rotp.ui.main.FleetPanel;

public class ConsoleFleetView implements IConsole {
	//private final CommandConsole console;
	private Empire empire;
	private StarSystem dest;
	private ShipFleet  fleet;
	private int fleetId;
	private String nebulaText;
	// private boolean isPlayer, contact;
	
	void init(int flId)	{
		fleet	= console().getFleet(flId);
		fleetId	= flId;
	}

	// ##### Systems Report
	String getInfo(String out)	{
		if (fleet == null || fleet.isEmpty())
			return out;

		if (fleet.isOrbiting()) {
			dest = null;
		}
		else {
			dest = fleet.destination();
		}
		empire 	= fleet.empire();
		
		// Top Box
		out += longEmpireInfo(empire) + " Fleet" + NEWLINE;
		out += bracketed(FLEET_KEY, fleetId) + " ";
		out = topBox(out);
		out = infoBox(out + NEWLINE);
		return out;
	}
	// ##### SUB BOXES
	private String topBox(String out)	{
		// draw orbiting data, bottom up
		if (fleet.launched() || (fleet.deployed() && !player().knowETA(fleet))) {
			if (player().knowETA(fleet) && (fleet.hasDestination())) {
				String dest =  planetName(fleet.destination().altId);
				String str2 = dest.isEmpty() ? text("MAIN_FLEET_DEST_UNSCOUTED") : text("MAIN_FLEET_DESTINATION", dest);
				out += str2;
			}
			String str3 = fleet.retreating() ? text("MAIN_FLEET_RETREATING") : text("MAIN_FLEET_IN_TRANSIT");
			out += NEWLINE + str3;
			if (!fleet.empire().isPlayer()) {
				if (player().alliedWith(fleet.empId)) {
					String str4 = text("MAIN_FLEET_ALLY");
					out += str4;
				} else if (player().atWarWith(fleet.empId)) {
					String str4 = text("MAIN_FLEET_ENEMY");
					out += "!" + str4;
				}
			}
		}
		else if (fleet.deployed()) {
			String dest =  planetName(fleet.destination().altId);
			String str2 = dest.isEmpty() ? text("MAIN_FLEET_DEST_UNSCOUTED") : text("MAIN_FLEET_DESTINATION", dest);
			out += str2;
			StarSystem sys1 = fleet.system();
			String str3 = text("MAIN_FLEET_ORIGIN", planetName(sys1.altId));
			out += str3;
			String str4 = fleet.retreating() && fleet.empire().isPlayer() ? text("MAIN_FLEET_RETREATING") :text("MAIN_FLEET_DEPLOYED");
			out += str4;
		}
		else {
			StarSystem sys1 = fleet.system();
			if (sys1 == null)
				out += NEWLINE + "ERROR: No system assigned to fleet ";
			else {
				String str2 = text("MAIN_FLEET_LOCATION", planetName(sys1.altId));
				String str3 = text("MAIN_FLEET_IN_ORBIT");
				out += str3 + " ";
				out += str2;
			}
		}
		return out;
	}
	private String infoBox(String out)	{
		if (fleet.canBeSentBy(player()))
			out += "Deployable ship list" + NEWLINE;
		else
			out += "Ship list" + NEWLINE;
		String text	= null;
		nebulaText	= null;
		String retreatText	= null;
		String rallyText	= null;
		if (fleet.canBeSentBy(player())) {
			if (!fleet.canSendTo(id(dest))) {
				if (dest == null) {
					StarSystem currDest = fleet.destination();
					if (currDest == null)
						text = "";
					else {
						if (fleet.empire().isPlayer()) {
							retreatText = text("MAIN_FLEET_AUTO_RETREAT");
							rallyText = text("MAIN_FLEET_SET_RALLY");
						}
						int dist = fleet.travelTurnsAdjusted(currDest);
						String destName = player().sv.name(currDest.id);
						if (destName.isEmpty())
							text = text("MAIN_FLEET_ETA_UNNAMED", dist);
						else
							text = text("MAIN_FLEET_ETA_NAMED", destName, dist);  
					}
				}
				else {
					String name = player().sv.name(dest.id);
					if (name.isEmpty())
						text = text("MAIN_FLEET_INVALID_DESTINATION2");
					else 
						text = text("MAIN_FLEET_INVALID_DESTINATION", name);
				}
			}
			else if (fleet.isDeployed() || fleet.isInTransit()) {
				if (fleet.empire().isPlayer()) {
					retreatText = text("MAIN_FLEET_AUTO_RETREAT");
					rallyText = text("MAIN_FLEET_SET_RALLY");
				}
				dest = dest == null ? fleet.destination() : dest;
				int dist = fleet.travelTurnsAdjusted(dest);
				String destName = player().sv.name(dest.id);
				if (destName.isEmpty())
					text = text("MAIN_FLEET_ETA_UNNAMED", dist);
				else
					text = text("MAIN_FLEET_ETA_NAMED", destName, dist);
				if ((dist > 1) && fleet.passesThroughNebula(dest))
					nebulaText = text("MAIN_FLEET_THROUGH_NEBULA");
			}
			else if (fleet.canSendTo(id(dest))) {
				int dist = 0;
				if (fleet.canReach(dest)) {
					dist = fleet.travelTurnsAdjusted(dest);
					String destName = player().sv.name(dest.id);
					if (destName.isEmpty())
						text = text("MAIN_FLEET_ETA_UNNAMED", dist);
					else
						text = text("MAIN_FLEET_ETA_NAMED", destName, dist);
				}
				else {
					dist = player().rangeTo(dest);
					text = text("MAIN_FLEET_OUT_OF_RANGE_DESC", dist);
				}
				if ((dist > 1) && fleet.passesThroughNebula(dest))
					nebulaText = text("MAIN_FLEET_THROUGH_NEBULA");
			}
			else if (fleet.isOrbiting()) {
				text = text("MAIN_FLEET_CHOOSE_DEST");
			}
		}
		else if (fleet.isInTransit() || fleet.isDeployed()) {
			if (fleet.empire().isPlayer()) {
				retreatText = text("MAIN_FLEET_AUTO_RETREAT");
				rallyText = text("MAIN_FLEET_SET_RALLY");
			}
			if (player().knowETA(fleet)) {
				int dist = fleet.travelTurnsRemainingAdjusted();
				if (fleet.hasDestination()) {
					String destName = player().sv.name(fleet.destSysId());
					if (destName.isEmpty())
						text = text("MAIN_FLEET_ETA_UNNAMED", dist);
					else
						text = text("MAIN_FLEET_ETA_NAMED", destName, dist);
				}
			}
			else {
				text = text("MAIN_FLEET_ETA_UNKNOWN");
			}
		}
		out = fleetBox(out);
		if (text != null)
			out += NEWLINE + text;

		if (rallyText != null)
			if (fleet.isRallied())
				out += NEWLINE + rallyText;
		if (retreatText != null)
			if (fleet.retreatOnArrival())
				out += NEWLINE + retreatText;
		return out;
	}
	private String fleetBox(String out)	{
		out += fleetDesignInfo(fleet, NEWLINE);
		if (nebulaText != null)
			out += NEWLINE + nebulaText;
		return out;
	}

	// ##### SEND FLEET
	String sendFleet(List<String> param, String out)	{
		// System.out.println("Select Fleet Send " + param);
		FleetPanel panel = RotPUI.instance().mainUI().displayPanel().fleetPane();
		// Check for destination
		out = setDest(param, out);
		Integer amt;
		// process adjusted fleet selection
		if (!param.isEmpty()) {
			// Convert to Integer
			List<Integer> counts = new ArrayList<>();
			for (String str : param) {
				amt = getInteger(str);
				if (amt == null || amt<0) {
					out += NEWLINE + "Wrong Sub Fleet Parameter " + str;
					return out;
				}
				counts.add(amt);
			}
			// activate adjustment
			panel.newAdjustedFleet(counts);
		}

		// All parameters are set: Process command
		int validDest = validPlanet(console().aimedStar());
		if (validDest != console().aimedStar()) {
			out += NEWLINE + "Invalid Destination star system " + console().aimedStar();
			panel.cancel();
			return out;
		}
		StarSystem dest = console().getSys(console().aimedStar());
		String status = panel.sendFleet(dest);
		out += NEWLINE + status;

		return out;
	}
}