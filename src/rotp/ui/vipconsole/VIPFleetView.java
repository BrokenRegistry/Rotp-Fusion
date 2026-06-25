package rotp.ui.vipconsole;

import java.util.ArrayList;
import java.util.List;

import rotp.model.empires.Empire;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.ui.RotPUI;
import rotp.ui.main.FleetPanel;
import rotp.ui.vipconsole.VIPConsole.Command;

public class VIPFleetView implements IVIPConsole {
	//private final CommandConsole console;
	private Empire empire;
	private StarSystem dest;
	private ShipFleet  fleet;
	private int fleetId;
	private String nebulaText;
	// private boolean isPlayer, contact;
	private int selectedFleet;
	
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
		if (fleet.launched() || (fleet.isDeployed() && !player().knowETA(fleet))) {
			if (player().knowETA(fleet) && (fleet.hasDestination())) {
				String dest =  planetName(fleet.destination().altId);
				String str2 = dest.isEmpty() ? text("MAIN_FLEET_DEST_UNSCOUTED") : text("MAIN_FLEET_DESTINATION", dest);
				out += str2;
			}
			String str3 = fleet.retreating() ? text("MAIN_FLEET_RETREATING") : text("MAIN_FLEET_IN_TRANSIT");
			out += NEWLINE + str3;
			if (!fleet.empire().isPlayer()) {
				if (player().alliedWith(fleet.empId())) {
					String str4 = text("MAIN_FLEET_ALLY");
					out += str4;
				} else if (player().atWarWith(fleet.empId())) {
					String str4 = text("MAIN_FLEET_ENEMY");
					out += "!" + str4;
				}
			}
		}
		else if (fleet.isDeployed()) {
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
						String destName = player().sv.knownName(currDest.id);
						if (destName.isEmpty())
							text = text("MAIN_FLEET_ETA_UNNAMED", dist);
						else
							text = text("MAIN_FLEET_ETA_NAMED", destName, dist);  
					}
				}
				else {
					String name = player().sv.knownName(dest.id);
					if (name.isEmpty())
						text = text("MAIN_FLEET_INVALID_DESTINATION2");
					else 
						text = text("MAIN_FLEET_INVALID_DESTINATION", name);
				}
			}
			else if (fleet.isDeployed() || fleet.inTransit()) {
				if (fleet.empire().isPlayer()) {
					retreatText = text("MAIN_FLEET_AUTO_RETREAT");
					rallyText = text("MAIN_FLEET_SET_RALLY");
				}
				dest = dest == null ? fleet.destination() : dest;
				int dist = fleet.travelTurnsAdjusted(dest);
				String destName = player().sv.knownName(dest.id);
				if (destName.isEmpty())
					text = text("MAIN_FLEET_ETA_UNNAMED", dist);
				else
					text = text("MAIN_FLEET_ETA_NAMED", destName, dist);
				if ((dist > 1) && fleet.passesThroughNebula(dest))
					nebulaText = text("MAIN_FLEET_THROUGH_NEBULA", fleet.speedInNebulaeStr());
			}
			else if (fleet.canSendTo(id(dest))) {
				int dist = 0;
				if (fleet.canReach(dest)) {
					dist = fleet.travelTurnsAdjusted(dest);
					String destName = player().sv.knownName(dest.id);
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
					nebulaText = text("MAIN_FLEET_THROUGH_NEBULA", fleet.speedInNebulaeStr());
			}
			else if (fleet.isOrbiting()) {
				text = text("MAIN_FLEET_CHOOSE_DEST");
			}
		}
		else if (fleet.inTransit() || fleet.isDeployed()) {
			if (fleet.empire().isPlayer()) {
				retreatText = text("MAIN_FLEET_AUTO_RETREAT");
				rallyText = text("MAIN_FLEET_SET_RALLY");
			}
			if (player().knowETA(fleet)) {
				int dist = fleet.travelTurnsRemainingAdjusted();
				if (fleet.hasDestination()) {
					String destName = player().sv.knownName(fleet.destSysId());
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
	String sendFleet(Entries param, String out)	{
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
			if (!panel.newAdjustedFleet(counts))
				return out + NEWLINE + "Error: Fleet can not be selected.";
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

	// ##### FleetView Command
	Command initSelectFleet()	{
		return new CmdSelectFleet("select Fleet from index and gives fleet info", FLEET_KEY);		
	}

	// ################### SUB COMMAND CLASSES ######################
	private class CmdSelectFleet extends Command {
		CmdSelectFleet  (String descr, String... keys) {
		super(descr, keys);
		cmdParam(" Index " + optional(FLEET_UNDEPLOY) + OR_SEP
				+ optional(FLEET_SEND + " " + SYSTEM_KEY + " destId [n] [n] [n] [n] [n]"));
		cmdHelp("Additionnal requests:"
				 + NEWLINE + "Optional "+ optional(FLEET_UNDEPLOY) + " to Undeploy fleet"
				 + NEWLINE + "Optional "+ optional(FLEET_SEND) + " to Star System " + SYSTEM_KEY + "x"
				 + NEWLINE + "Optional select sub fleet by adding the number of each listed design");
		}
		@Override protected String execute(Entries param) {
			if (param.isEmpty())
				return cmdHelp();

			String  str  = param.remove(0);
			Integer flId = getInteger(str);
			ShipFleet fleet;
			if (flId == null) { // select a new fleet
				return "??? parameter " + str + NEWLINE + cmdHelp();
			}

			FleetPanel panel = RotPUI.instance().mainUI().displayPanel().fleetPane();
			String out = getShortGuide() + NEWLINE;
			// select a new fleet
			selectedFleet = console().validFleet(flId);
			if (selectedFleet == flId)
				out = "";
			else
				return "Invalid Fleet selection";
			fleet = console().getFleet(selectedFleet);
			mainUI().selectSprite(fleet, 1, false, true, false, null);
			mainUI().map().recenterMapOn(fleet);
			mainUI().repaint();
			init(selectedFleet);
			out = getInfo(out);

			if (!param.isEmpty()) { // Do something with selected fleet
				str = param.remove(0);
				if (str.equalsIgnoreCase(FLEET_SEND)) { // Send Fleet
					if (fleet.empire().isPlayer())
						out = sendFleet(param, out);
					else
						out = "Error: You do not own this fleet. Only player fleets can be sent"; 
				}
				else if (str.equalsIgnoreCase(FLEET_UNDEPLOY)) {
					if (fleet.empire().isPlayer())
						panel.undeployFleet();
					else
						out = "Error: You do not own this fleet. Only player fleets can be undeployed"; 
				}
				else
					out += NEWLINE + "Wrong parameter " + str;
			}
			return out;
		}
	}
}
