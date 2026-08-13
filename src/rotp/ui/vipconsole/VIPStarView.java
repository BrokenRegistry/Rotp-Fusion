package rotp.ui.vipconsole;

import rotp.model.colony.Colony;
import rotp.model.empires.Empire;
import rotp.model.empires.SystemInfo;
import rotp.model.empires.SystemView;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.planet.Planet;
import rotp.model.planet.PlanetType;
import rotp.model.ships.Design;
import rotp.model.ships.ShipDesignLab;
import rotp.ui.RotPUI;
import rotp.ui.sprites.SystemTransportSprite;
import rotp.ui.vipconsole.VIPConsole.Command;

public class VIPStarView implements IVIPConsole {
	private Empire empire;
	private StarSystem sys;
	private SystemView sv;
	private Colony colony;
	private int id;
	private boolean isPlayer, isScouted, isColony;
	int selectedStar, aimedStar;
	
	void initId(int sysId)		{
		sv	= player().sv.view(sysId);
		sys	= galaxy().system(sysId);
		id	= sysId;
		empire 		= sv.empire();
		isPlayer	= isPlayer(empire);
		isScouted	= sv.scouted();
		isColony	= sv.isColonized();
		if (isPlayer)
			colony = sv.colony();
		else
			colony	= null;
	}
	void initAltId(int altId)	{ initId(console().getSysId(altId)); }
	// ##### Systems Report
	String systemInfo(int id)	{
		initId(id);
		String out = systemBox("");
		if (isColony)
			out += NEWLINE + shortSystemInfo(sv);
		out = terrainBox(out);
		out = distanceBox(out);
		return out;
	}
	String getInfo(String out)	{
		if (player().hiddenSystem(sys)) // Dark Galaxy
			return out + " !!! Hidden";
		out = systemBox(out);
		out += fleetInfo();
		out = empireBox(out);
		out = terrainBox(out);
		out = distanceBox(out);
		//out = colonyControls(out);
		return out;
	}
	// ##### SUB BOXES
	private String systemBox(String out)	{
		out += planetName(sv, NEWLINE);
		out += cLn(planetEnvironment());
		out += cLn(transportInfo());
		out += cLn(researchProject());
		if (sv.isAlert())
			out += NEWLINE + "! Under Attack";
		return out;
	}
	private String empireBox(String out)	{
		if (sv.flagColorId() > 0)
		   	out += NEWLINE + systemFlag("Flag colors = ");
		if (!isColony)
			return out;
		out += NEWLINE + shortSystemInfo(sv);
		if (isPlayer)
			out += NEWLINE + playerColonyData();
		else {
			out += cLn(treatyStatus());
			out += NEWLINE + alienColonyData();
		}
		return out;
	}
	private String terrainBox(String out)	{
		if (isPlayer)
			return colonyControls(out);
		out += NEWLINE + text(sys.starType().description());		
		if (isScouted)
			out += NEWLINE + planetColonizable();
		return out;
	}
	private String distanceBox(String out)	{
		if (isPlayer)
			return out;
		out += cLn(systemRange());
		return out;
	}
	// ##### PLAYER COLONY CONTROLS
	private String colonyControls(String out)	{
		if (!isPlayer)
			return out;

		// Income
		int income = (int) colony.totalIncome();
		int prod   = (int) colony.production();
        String str1 = text("MAIN_COLONY_PRODUCTION");
        String str2 = str(income);
        String str3 = concat(str1, " ", str2, " ", "(", str(prod), ")");
		out += NEWLINE + str3;

        // Governor
		if (colony.isGovernor())
			out += NEWLINE + "Governor is On";
		else
			out += NEWLINE + "Governor is Off";
		
		// Categories
		for (int category=0; category<Colony.NUM_CATS; category++) {
			out += NEWLINE;
			int pct = 2 * colony.allocation(category);
			String labelText  = text(Colony.categoryName(category));
			String resultText = text(colony.category(category).upcomingResult());
			out += labelText + " = " + pct + "%, out = " + resultText;
			if (!colony.canAdjust(category))
				out += " (locked)";
		}
		
		// Ship Build Queue
		String str = text("MAIN_COLONY_SHIPYARD_CONSTRUCTION");
		String name = colony.shipyard().design().name();
		out += NEWLINE + str + " = " + name;
		Design d = colony.shipyard().design();
        if (d.scrapped())
        	out += " " + text("MAIN_COLONY_SHIP_SCRAPPED");
        else {
            int i = colony.shipyard().upcomingShipCount();
        	out += ", out = " + i;
        }
		String label = text("MAIN_COLONY_SHIPYARD_LIMIT");
		String amt   = colony.shipyard().buildLimitStr();
		out += NEWLINE + label + " " + amt;
		
		return out;
	}
	// ##### SUB ELEMENTS
	private String systemFlag(String out)	{
		int numFlag = options().selectedFlagColorCount();
		for (int i=0; i<numFlag; i++) {
			if (i>0)
				out += SPACER;
			out += sv.getFlagColorName(i);
		}
		return out;
	}
	private String planetColonizable()	{
		Empire pl = player();
		PlanetType pt = sv.planet().type();
		if (pl.canColonize(pt))
			return "Player can colonize";
		else if (pl.isLearningToColonize(pt))
			return "Player is learning tech to colonize";
		else if (pl.canLearnToColonize(pt))
			return "Player can learn tech to colonize";
		else if (pt.isAsteroids())
			return "Not colonizable";
		else
			return "Player does not have tech to colonize";
	}
	private String planetEnvironment()	{
		PlanetType planetType = sv.planetType();
		if (planetType == null)
			return "";
		String out = "Planet Type = " + planetType.name();
		String ecology = text(sv.ecologyType());
		if (!ecology.isEmpty())
			out += SPACER + ecology;
		String resource = text(sv.resourceType());
		if (!resource.isEmpty())
			out += SPACER + resource;
		if (sv.currentSize() > 0)
			out += NEWLINE + "Current Size = " + sv.currentSize();
		return out;		
	}
	private String playerColonyData()	{
		int pop	= sv.population();
		if (pop == 0)
			return text("MAIN_SYSTEM_DETAIL_NO_DATA");
		String out = "Population = " + pop;
		Planet planet = sv.planet();
		if (planet != null) {
			out += " / ECOmax = " + (int) planet.sizeAfterWaste();
		}
		SystemInfo si	= player().sv;
		if (si.isColonized(id) && si.colony(id).inRebellion())
			out += " ! " + text("MAIN_PLANET_REBELLION");
		// Test for Transport
		int trId = sys.transportDestId;
		if (trId != StarSystem.NULL_ID) {
			StarSystem destination = galaxy().system(trId);
			int amount = sys.transportAmt();
			String dest = bracketed(SYSTEM_KEY, destination.altId);
			if (amount == 1)
				out += NEWLINE + "Planned "+ amount + " transport to " + dest;
			else if (amount >= (int)sys.population())
				out += NEWLINE + text("MAIN_TRANSPORT_ABANDON_TITLE") + " to " + dest;
			else
				out += NEWLINE + "Planned "+ amount + " transports to " + dest;
			int turns = (int) Math.ceil(sys.transportTimeTo(destination));
			out += NEWLINE + text("MAIN_TRANSPORT_ETA", dest, turns);
			int maxAllowed = player().maxTransportsAllowed(destination);
			if (amount > maxAllowed) {
                if (maxAllowed == 0)
                    out += NEWLINE + text("MAIN_TRANSPORT_NO_ROOM");
                else
                	out += NEWLINE + text("MAIN_TRANSPORT_SIZE_WARNING", str(maxAllowed));
			}
		}
		out += NEWLINE + "Factories = " + sv.factories();
		Colony colony = sv.colony();
		if (colony != null)
			out += " / max = " + colony.industry().maxBuildableFactories();
		if (sv.shieldLevel() > 0)
		   	out += NEWLINE + "Shield Level = " + sv.shieldLevel();
		int bases = sv.bases();
		out += NEWLINE + "Bases = " + bases;
		if (colony != null) {
			int maxBase = colony.defense().maxBases();
			out += "/" + maxBase;
		}
		return out;
	}
	private String alienColonyData()	{
		int pop	= sv.population();
		if (pop == 0)
			return text("MAIN_SYSTEM_DETAIL_NO_DATA");
		String out = cLn(systemReportAge());
		out += "Population = " + pop;
		SystemInfo si	= player().sv;
		if (si.isColonized(id) && si.colony(id).inRebellion())
			out += " ! " + text("MAIN_PLANET_REBELLION");
		out += NEWLINE + "Factories = " + sv.factories();
		if (sv.shieldLevel() > 0)
		   	out += NEWLINE + "Shield Level = " + sv.shieldLevel();
		int bases = sv.bases();
		if (bases > 0)
			out += NEWLINE + "Bases = " + bases;
		return out;
	}
	private String transportInfo()		{
		if (sys.canShowIncomingTransports()) {
			if (isPlayer) {
				int friendPop = sys.colony().playerPopApproachingSystem();;
				int enemyPop  = sys.colony().enemyPopApproachingPlayerSystem();
				String str = "";
				if (friendPop > 0)
					str += text("Incoming population = ", friendPop);
				if (enemyPop > 0)
					return text("! Incoming enemy troop = ", enemyPop) + cLn(str);
			}
			else {
				int playerPop = sys.colony().playerPopApproachingSystem();;
				return text("Incoming player troop = ", playerPop);
			}
		}
		return "";
	}
	private String fleetInfo()			{
		String out = "";
		for (ShipFleet fl: sys.orbitingFleets()) {
			if (fl.visibleTo(player())) {
				out += NEWLINE + "In Orbit " + longEmpireInfo(fl.empire()) + " fleet";
				out += NEWLINE + fleetDesignInfo(fl, NEWLINE);
			}
		}
		for (ShipFleet fl: player().getEtaFleets(sys)) {
			out += NEWLINE + "Incoming " + longEmpireInfo(fl.empire()) + " fleet";
			out += NEWLINE + fleetDesignInfo(fl, NEWLINE);
			out += NEWLINE + "ETA = " + (int) Math.ceil(fl.travelTimeAdjusted(sys)) + " Years";
		}
		return out;
	}
	private String treatyStatus()		{
		int empId = player().sv.empId(id);
		if (player() == empire)
			return "";
		if (player().alliedWith(empId))
			return text("MAIN_FLEET_ALLY");
		else if (player().atWarWith(empId))
			return text("MAIN_FLEET_ENEMY");
		else
			return "";
	}	
	private String researchProject()	{
		if (sys.hasEvent())
			return text(sys.eventKey());
		return "";
	}	
	private String systemReportAge()	{
		int age = player().sv.spyReportAge(sys.id);
		if (age > 0)
			return text("RACES_REPORT_AGE", age);
		else
			return "";
	}	
	private String systemRange()		{
		SystemInfo si = player().sv;
		float range	= (float) Math.ceil(si.distance(id)*10)/10;
		String out  = "Distance = ";
		if (player().alliedWith(id(sys.empire())))
			out += text("MAIN_ALLIED_COLONY");
		else
			out += text("MAIN_SYSTEM_RANGE", df1.format(range));
		out += SPACER;
		if (si.inShipRange(id)) {
			out += text("MAIN_IN_RANGE_DESC");
		}
		else if (si.inScoutRange(id)) {
			out += text("MAIN_SCOUT_RANGE_DESC");
		}
		else {
			out += text("MAIN_OUT_OF_RANGE_DESC");
		}
		return out;		
	}
	private String spending(int category, Entries param, String out)	{
		if (param.isEmpty()) {
			return out + "Error: Missing parameter ";
		}

		String s  = param.remove(0);
		if (COL_TOGGLE_LOCK.equalsIgnoreCase(s)) {
			colony.toggleLock(category);
			if (colony.canAdjust(category))
				return out + "Unlocked";
			else
				return out + "Locked";
		}
		
		if (!colony.canAdjust(category))
			return out + "Error: Category is locked";
		
		switch (s.toUpperCase()) {
			case COL_SMART_ECO_MAX:
				colony.forcePct(category, 1);
				colony.keepEcoLockedToClean = true;
				colony.checkEcoAtClean();
				return out + "Maxed keeping ECO clean";
			case COL_SMOOTH_MAX:
				colony.keepEcoLockedToClean = true;
				colony.smoothMaxSlider(category);
				colony.checkEcoAtClean();
				return out + "Smart Maxed";
			default:
				Integer val = getInteger(s);
				if (val == null)
					return out + "Unexpected parameter " + s;
				double pct = val/100.0;
				colony.forcePct(category, (float) pct);
				if (category != Colony.RESEARCH)
					RotPUI.instance().techUI().resetPlanetaryResearch();
				pct = colony.pct(category);
				return out + "Set to " + (int)(pct*100) + "%";
		}
	}
	// ##### Spending
	String toggleGovernor(String out)	{
		colony.setGovernor(!colony.isGovernor());
        if (colony.isGovernor()) {
            colony.govern();
            out += "Governor is now active";
        }
        else
        	out += "Governor is disabled";
		return out;
	}
	String shipSpending(Entries param, String out)	{
		out += "Ship spending: ";
		return spending(Colony.SHIP, param, out);
	}
	String defSpending(Entries param, String out)	{
		out += "Defense spending: ";
		return spending(Colony.DEFENSE, param, out);
	}
	String indSpending(Entries param, String out)	{
		out += "Industry spending: ";
		return spending(Colony.INDUSTRY, param, out);
	}
	String ecoSpending(Entries param, String out)	{
		int category = Colony.ECOLOGY;
		out += "Ecology spending: ";
		if (param.isEmpty()) {
			return out + "Error: Missing parameter ";
		}

		String s  = param.remove(0);
		if (COL_TOGGLE_LOCK.equalsIgnoreCase(s)) {
			colony.toggleLock(category);
			if (colony.canAdjust(category))
				return out + "Unlocked";
			else
				return out + "Locked";
		}
		
		if (!colony.canAdjust(category))
			return out + "Error: Category is locked";
		
		switch (s.toUpperCase()) {
			case COL_ECO_CLEAN:
				colony.keepEcoLockedToClean = true;
				colony.checkEcoAtClean();
				return out + "Set ECO to clean";
			case COL_ECO_GROWTH:
				colony.smoothMaxSlider(category);
				return out + "Smart Maxed";
			case COL_ECO_TERRAFORM:
				colony.checkEcoAtTerraform();
            	colony.keepEcoLockedToClean = false;
				return out + "Set ECO to Terraform";
			default:
				Integer val = getInteger(s);
				if (val == null)
					return out + "Unexpected parameter " + s;
				double pct = val/100.0;
				colony.forcePct(category, (float) pct);
				if (category != Colony.RESEARCH)
					RotPUI.instance().techUI().resetPlanetaryResearch();
				pct = colony.pct(category);
				return out + "Set to " + (int)(pct*100) + "%";
		}
	}
	String techSpending(Entries param, String out)	{
		out += "Technology spending: ";
		return spending(Colony.RESEARCH, param, out);
	}
	String shipBuilding(Entries param, String out)	{
		out += "Ship Building ";
		if (param.isEmpty()) {
			return out + "Error: Missing parameter ";
		}
		String s  = param.remove(0);
		Integer val = getInteger(s);
		if (val == null)
			return out + "Unexpected parameter " + s;
		int id = bounds(0, val, ShipDesignLab.MAX_DESIGNS-1);
		Design d;
		if (id == val)
			d = player().shipLab().design(val);
		else if (!player().tech().canBuildStargate())
			return out + "Error: Stargate Tech not yet available";
		else if (colony.shipyard().hasStargate())
			return out + "Error: Already has a Stargate";
		else
			d = player().shipLab().stargateDesign();
		
		if (d.active())
			out += "Set to " + d.name();
		else
			return out + "Error Invalid design";
			
		colony.shipyard().design(d);
		return out;
	}
	String shipLimit(Entries param, String out)	{
		out += "Ship Limit ";
		if (param.isEmpty()) {
			return out + "Error: Missing parameter ";
		}
		String s  = param.remove(0);
		Integer val = getInteger(s);
		if (val == null)
			return out + "Unexpected parameter " + s;
		colony.shipyard().buildLimit(val);
		out += "Set to " + colony.shipyard().buildLimitStr();
		return out;
	}
	String missBuilding(Entries param, String out)	{
		out += "Missiles Limit ";
		if (param.isEmpty()) {
			return out + "Error: Missing parameter ";
		}
		String s  = param.remove(0);
		Integer val = getInteger(s);
		if (val == null)
			return out + "Unexpected parameter " + s;
		val = max(0, val);
		colony.defense().maxBases(val);
		out += "Set to " + colony.defense().maxBases();
		return out;
	}
	String getFunds(Entries param, String out)	{
		if (param.isEmpty())
			return out + "Error: Amount parameter is missing";
		String str = param.get(0);
		Integer amount = getInteger(str);
		if (amount == null)
			return out + "Error: Wrong amount parameter";

		param.remove(0);
		float realAmount = player().allocateReserve(sys.colony(), amount);
		out += text("PLANETS_TRANSFER_DESC", sys.name());
		out += NEWLINE + "Amount requested" + EQUAL_SEP + amount;
		out += NEWLINE + "Amount transfered" + EQUAL_SEP + df1.format(realAmount);
		return out;
	}
	// ##### Population
	private String validTransportDestination(StarSystem dest) {
		if (dest == null) {
			return "Error: Invalid destination";
		}
		SystemInfo si	= player().sv;
		if (!si.isScouted(dest.id)) {
			return "Error: " + text("MAIN_TRANSPORT_UNSCOUTED");
		}
		if (!si.isColonized(dest.id) && !si.isAbandoned(dest.id)) {
			return "Error: Destination neither colonized nor abandoned";
		}
		if (!si.inShipRange(dest.id)) {
			return "Error: " + text("MAIN_TRANSPORT_OUT_OF_RANGE");
		}
		if (!player().canColonize(dest.planet().type())
				&& !((dest.empire() == player()) && dest.colony().inRebellion())) {
			return "Error: " + text("MAIN_TRANSPORT_HOSTILE");
		}
		return "";
	}
	String sendPopulation(Entries param, String out)	{
		// System.out.println("Send Transport " + param);
		// Check for destination
		out = setDest(param, out);
		// Check for amount
		if (param.isEmpty()) {
			out += NEWLINE + "Error: Amount to send is missing";
			return out;
		}
		String s = param.remove(0);
		Integer amount = getInteger(s);
		if (amount == null || amount<=0) {
			out += NEWLINE + "Error: Wrong amount Parameter " + s;
			return out;
		}
		amount = min(amount, (int)(sys.population()/2));
		StarSystem dest = aimedSystem();
		String destError = validTransportDestination(dest);
		if (!destError.isEmpty()) {
			out += NEWLINE + destError;
			return out;
		}
		if (amount == 1)
			out += NEWLINE + "Send"+ amount + " transport to " + dest;
		else
			out += NEWLINE + "Send "+ amount + " transports to " + dest;

		SystemTransportSprite transportSprite = sys.transportSprite();
		transportSprite.clickedDest(dest);
		transportSprite.amt(amount);

		player().budget().makeTransortObsolete();
		player().deployTransport(sys);
		RotPUI.instance().mainUI().clickedSprite(sys);

		return out;
	}
	String abandonColony(Entries param, String out)	{
		// System.out.println("Abandon Planet" + param);
		// Check for destination
		out = setDest(param, out);
		StarSystem dest = aimedSystem();
		String destError = validTransportDestination(dest);
		if (!destError.isEmpty()) {
			out += NEWLINE + destError;
			return out;
		}
		int amount = (int) sys.population();
		out += NEWLINE + "Abandon Planet " + amount + " transports sent to " + dest;
		SystemTransportSprite transportSprite = sys.transportSprite();
		transportSprite.clickedDest(dest);
		transportSprite.amt(amount);

		player().budget().makeTransortObsolete();
		player().deployTransport(sys);
		RotPUI.instance().mainUI().clickedSprite(sys);

		return out;
	}
	String cancelSend(Entries param, String out)	{
		System.out.println("Cancel Transport" + param);

		player().budget().makeTransortObsolete();
		SystemTransportSprite transportSprite = sys.transportSprite();
		transportSprite.clear();
		player().budget().makeTransortObsolete();
		player().deployTransport(sys);
		RotPUI.instance().mainUI().clickedSprite(sys);

		return out;
	}

	// ##### StarView Command
	StarSystem aimedSystem()		{
		if (aimedStar < 0 || aimedStar >= galaxy().systemCount)
			return null;
		return console().getSys(aimedStar);
	}
	Command initSelectPlanet()		{
		return new CmdSelectPlanet("Select Planet from index and gives Info", SYSTEM_KEY);		
	}
	Command initAimedPlanet()		{
		return new CmdAimedPlanet("select Aimed planet from index, or Selected planet", AIMED_KEY);		
	}
	// ################### SUB COMMAND CLASSES ######################
	private class CmdSelectPlanet extends Command {
		CmdSelectPlanet  (String descr, String... keys) {
		super(descr, keys);
		cmdParam(" Index " + optional(COL_TOGGLE_GOV)
				+ optional(COL_SHIP_SPENDING + " %")
				+ optional(COL_DEF_SPENDING + " %")
				+ optional(COL_IND_SPENDING + " %")
				+ optional(COL_ECO_SPENDING + " %")
				+ optional(COL_TECH_SPENDING + " %")
				+ optional(COL_SHIP_BUILDING + " val")
				+ optional(COL_SHIP_LIMIT + " max")
				+ optional(COL_BASE_LIMIT + " max")
				);
		cmdHelp("Additionnal sequentially processed requests:"
				+ NEWLINE + optional(COL_TOGGLE_GOV) + " To Toggle Governo on/off"
				+ NEWLINE + optional(COL_SHIP_SPENDING + " " + COL_TOGGLE_LOCK) + " To lock/unlock Ship spending"
				+ NEWLINE + optional(COL_SHIP_SPENDING + " %") + " To set Ship spending percentage"
				+ NEWLINE + optional(COL_SHIP_SPENDING + " " + COL_SMART_ECO_MAX) + " To maximize Ship spending, while keeping ECO clean"
				+ NEWLINE + optional(COL_SHIP_SPENDING + " " + COL_SMOOTH_MAX) + " To smart maximize Ship spending to reach target, while keeping ECO clean"
				+ NEWLINE + optional(COL_DEF_SPENDING + " " + COL_TOGGLE_LOCK) + " To lock/unlock Defense spending"
				+ NEWLINE + optional(COL_DEF_SPENDING + " %") + " To set Defense spending percentage"
				+ NEWLINE + optional(COL_DEF_SPENDING + " " + COL_SMART_ECO_MAX) + " To maximize Defense spending, while keeping ECO clean"
				+ NEWLINE + optional(COL_DEF_SPENDING + " " + COL_SMOOTH_MAX) + " To smart maximize Defense spending to reach target, while keeping ECO clean"
				+ NEWLINE + optional(COL_IND_SPENDING + " " + COL_TOGGLE_LOCK) + " To lock/unlock Industry spending"
				+ NEWLINE + optional(COL_IND_SPENDING + " %") + " To set Industry spending percentage"
				+ NEWLINE + optional(COL_IND_SPENDING + " " + COL_SMART_ECO_MAX) + " To maximize Industry spending, while keeping ECO clean"
				+ NEWLINE + optional(COL_ECO_SPENDING + " " + COL_TOGGLE_LOCK) + " To lock/unlock Ecology spending"
				+ NEWLINE + optional(COL_ECO_SPENDING + " %") + " To set Ecology spending percentage"
				+ NEWLINE + optional(COL_ECO_SPENDING + " " + COL_ECO_CLEAN) + " To set Ecology spending to clean"
				+ NEWLINE + optional(COL_ECO_SPENDING + " " + COL_ECO_GROWTH) + " To set Ecology spending to grow population"
				+ NEWLINE + optional(COL_ECO_SPENDING + " " + COL_ECO_TERRAFORM) + " To set Ecology spending to terraform planet"
				+ NEWLINE + optional(COL_TECH_SPENDING + " " + COL_TOGGLE_LOCK) + " To lock/unlock Research spending"
				+ NEWLINE + optional(COL_TECH_SPENDING + " %") + " To set Research spending percentage"
				+ NEWLINE + optional(COL_TECH_SPENDING + " " + COL_SMART_ECO_MAX) + " To maximize Research spending, while keeping ECO clean"
				+ NEWLINE + optional(COL_GET_FUND + " amount") + " To transfert funds from the empire to this colony"
				+ NEWLINE + optional(COL_TROOP_SEND + " " + SYSTEM_KEY) + " destId amount : To send transport to another planet"
				+ NEWLINE + optional(COL_ABANDON + " " + SYSTEM_KEY) + " destId amount : To abandon the planet"
				+ NEWLINE + optional(COL_CANCEL_SEND) + " To cancel all transports from this planet"
				);
		}
		@Override protected String execute(Entries param) {
			if (param.isEmpty())
				return getShortGuide();
			String s = param.remove(0);
			Integer p = getInteger(s);
			if (p == null)
				return getShortGuide();

			String out	= "";
			selectedStar	= validPlanet(p);
			StarSystem sys	= console().getSys(selectedStar);
			mainUI().selectSystem(sys);
			initAltId(selectedStar);

			// Action are reserved to player colonies
			if (!isPlayer(sys.empire()))
				return getInfo(out);

			while (!param.isEmpty()) {
				s = param.remove(0);
				switch (s.toUpperCase()) {
				case COL_TOGGLE_GOV:
					out = toggleGovernor(out) + NEWLINE;
					break;
				case COL_SHIP_SPENDING:
					out = shipSpending(param, out) + NEWLINE;
					break;
				case COL_IND_SPENDING:
					out = indSpending(param, out) + NEWLINE;
					break;
				case COL_DEF_SPENDING:
					out = defSpending(param, out) + NEWLINE;
					break;
				case COL_ECO_SPENDING:
					out = ecoSpending(param, out) + NEWLINE;
					break;
				case COL_TECH_SPENDING:
					out = techSpending(param, out) + NEWLINE;
					break;
				case COL_SHIP_BUILDING:
					out = shipBuilding(param, out) + NEWLINE;
					break;
				case COL_SHIP_LIMIT:
					out = shipLimit(param, out) + NEWLINE;
					break;
				case COL_BASE_LIMIT:
					out = missBuilding(param, out) + NEWLINE;
					break;
				case COL_TROOP_SEND:
					out = sendPopulation(param, out) + NEWLINE;
					break;
				case COL_ABANDON:
					out = abandonColony(param, out) + NEWLINE;
					break;
				case COL_CANCEL_SEND:
					out = cancelSend(param, out) + NEWLINE;
					break;
				case COL_GET_FUND:
					out = getFunds(param, out) + NEWLINE;
					break;
				default:
					out += "Don't understand parameter " + s;
					break;
				}
			}
			return getInfo(out);
		}
	}
	private class CmdAimedPlanet extends Command {
		CmdAimedPlanet  (String descr, String... keys) {
		super(descr, keys);
		cmdParam(" " + optional("Index", "S"));
		cmdHelp("select Aimed planet from planet index, or from Selected planet if \"S\", and gives Destination info");
		}
		@Override protected String execute(Entries param) {
			String out = getShortGuide() + NEWLINE;
			if (!param.isEmpty()) {
				String s = param.get(0);
				if (s.equalsIgnoreCase("S"))
					aimedStar = selectedStar;
				else {
					Integer p = getInteger(s);
					if (p != null) {
						aimedStar = p;
						out = "";
					}
				}
			}
			StarSystem sys	= console().getSys(aimedStar);
			out += viewTargetSystemInfo(sys, true);
			return out;
		}
	}
}
