package rotp.ui.vipconsole;

import static rotp.ui.vipconsole.VIPConsole.instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import rotp.model.empires.DiplomaticTreaty;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.empires.SystemView;
import rotp.model.empires.TreatyAlliance;
import rotp.model.galaxy.IMappedObject;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.galaxy.Transport;
import rotp.model.ships.ShipDesign;
import rotp.ui.RotPUI;
import rotp.ui.main.MainUI;
import rotp.util.Base;
import rotp.util.Basket;

public interface IVIPConsole extends Base {
	int NULL_ID		= -1;
	int OPTION_ID	= 0;
	int SETTING_ID	= 1;
	int MENU_ID		= 2;
	String SPACER	= ", ";
	String OR_SEP	= "|";	// " or " may be better?
	String HELP		= "?";
	String EQUAL_SEP	= " = ";

	String AIMED_KEY		= "A";
	String DESIGN_KEY		= "D";
	String EMPIRE_KEY		= "E";
	String FLEET_KEY		= "F";
	String SYSTEM_KEY		= "P";
	String TRANSPORT_KEY	= "T";
	String MENU_KEY			= "M";
	String OPTION_KEY		= "O";
	String SETTING_KEY		= "S";
	String TECHNOLOGY_KEY	= "TECH";
	String DESIGN_MENU_KEY	= "DESIGN";

	String COL_TOGGLE_GOV		= "TG";
	String COL_SHIP_SPENDING	= "S";
	String COL_DEF_SPENDING		= "D";
	String COL_IND_SPENDING		= "I";
	String COL_ECO_SPENDING		= "E";
	String COL_TECH_SPENDING	= "R";
	String COL_SHIP_BUILDING	= "SB";
	String COL_SHIP_LIMIT		= "SL";
	String COL_BASE_LIMIT		= "BL";
	String COL_GET_FUND			= "FUND";

	String COL_TOGGLE_LOCK		= "TL";
	String COL_SMART_ECO_MAX	= "EM";
	String COL_SMOOTH_MAX		= "SM";
	String COL_ECO_CLEAN		= "C";
	String COL_ECO_GROWTH		= "G";
	String COL_ECO_TERRAFORM	= "T";

	String COL_TROOP_SEND		= "SEND";
	String COL_ABANDON			= "ABANDON";
	String COL_CANCEL_SEND		= "CANCEL";

	String FLEET_SEND		= "SEND";
	String FLEET_UNDEPLOY	= "U";

	// EMPIRE
	String EMP_DIPLOMACY	= "D";
	String EMP_INTELLIGENCE	= "I";
	String EMP_MILITARY		= "M";
	String EMP_STATUS		= "S";
	String EMP_REPORT		= "R";
	String EMP_DEF_BASES	= "B";
	String EMP_INTEL_TAXES	= "T"; // Both security and Spying
	String EMP_SPY_NETWORK	= "N";
	String EMP_SPY_ORDER	= "O";
	String EMP_AUDIENCE		= "A";
	String EMP_SPY_HIDE		= "H";
	String EMP_SPY_ESPION	= "E";
	String EMP_SPY_SABOTAGE	= "S";
	String EMP_FINANCES		= "F";
	String EMP_DEV_COLONIES	= "D";
	String EMP_ALL_COLONIES	= "A";
	// TECH
	String TECH_COMPUTER	= "CPU";
	String TECH_CONSTRUCTION= "CONST";
	String TECH_FORCE_FIELDS= "FF";
	String TECH_PLANETOLOGY	= "ECO";
	String TECH_PROPULSION	= "PROP";
	String TECH_WEAPON		= "W";
	String PRESS_ANY_KEY	= "Enter any command to continue";
	
	Boolean verboseGlobal	= null;
	default Boolean verboseLocal()	{ return null; }
	default boolean verbose()	{
		Boolean verboseLocal = verboseLocal();
		if (verboseLocal == null)	// No local then global or default
			if (verboseGlobal == null)
				return true;
			else
				return verboseGlobal;
		else
			return verboseLocal;
	}

//	##### TOOLS #####
	default MainUI mainUI()	  			{ return RotPUI.instance().mainUI(); }
	default VIPConsole console()		{ return instance(); }
	default Empire empire(int empId)	{ return galaxy().empire(empId); }
	default int validPlanet(int p)		{ return bounds(0, p, galaxy().systemCount-1); }
	default String cLn(String s)		{ return s.isEmpty() ? "" : (NEWLINE + s); }
	default String ly(float dist)		{ return text("SYSTEMS_RANGE", df1.format(Math.ceil(10*dist)/10)); }
	default String bracketed(String key, int index)	{ return "(" + key + " " + index + ")"; }
	default String bracketed(String key, String s)	{ return "(" + key + " " + s + ")"; }
	default String optional(String... keys)			{ return "[" + either(keys) + "]"; }
	default String either(String... keys)			{
		String sep = "";
		String out = "";
		for (String key : keys) {
			out += sep + key;
			sep = OR_SEP;
		}
		return out;
	}
	default String setDest(List<String> param, String out)	{
		String s = param.get(0);
		Integer f;
		if (s.equalsIgnoreCase(AIMED_KEY))
			param.remove(0); // Parameter processed... Implicit target, Nothing to do
		else if (s.startsWith(SYSTEM_KEY)) { // New destination
			if (s.length() > 1) { // Parameter linked
				s = s.substring(1);
				param.remove(0); // Parameter processed
			}
			else { // Planet number in the following parameter
				param.remove(0); // Parameter processed
				if (param.isEmpty()) {
					out += NEWLINE + "Wrong Destination Parameter";
					return out;
				}
				else { // get planet Number
					s = param.remove(0);
				}
			}
			// Process the planet index
			f = getInteger(s);
			if (f != null) { // select a new Destination
				console().aimedStar(f);
			}
			else {
				out += NEWLINE + "Wrong Destination Parameter";
				return out;
			}
		}
		return out;
	}
	default String viewSystemInfo(StarSystem sys, boolean local)	{
		Empire emp		= sys.empire();
		Empire pl		= player();
		SystemView view	= pl.sv.view(sys.id);
		String out = bracketed(SYSTEM_KEY, sys.altId) + " ";
		// Star Color
		out += sys.starColor() + " star";
		// Planet Name
		String s = view.name();
		if (!s.isEmpty())
			out += SPACER + view.name();
		out += SPACER + shortSystemInfo(view);
		// Planet Distance
		if (local) {
			StarSystem ref = instance().getSys(instance().selectedStar());
			out +=  SPACER + "Distance " + bracketed(SYSTEM_KEY, instance().selectedStar()) + "s = " + ly(ref.distanceTo(sys));
		}
		else if (pl != emp){
			out += SPACER + "Distance to player = " + ly(pl.distanceTo(sys));
		}
		return out;
	}
	default String viewTargetSystemInfo(StarSystem sys, boolean local)	{
		return "Aimed System = " + viewSystemInfo(sys, local);
	}
	default Basket getIndex(List<String> param, String str, String key)	{
		Basket res = new Basket("");
		if (str.equals(key))
			str = param.remove(0).toUpperCase();
		else
			str = str.replace(key, "");
		res.integerValue = getInteger(str);
		if (res.integerValue == null)
			res.stringValue = "Error: Missing Integer value " + str + "?";
		return res;
	}
	//	##### FLEETS #####
	default String fleetDesignInfo(ShipFleet fl, String sep)	{
		String out = "";
		int[] visible = fl.visibleShips(player().id);
		// count how many of those visible designs have ships
		int num = 0;
		String separator = "";
		for (int cnt: visible)
			if (cnt > 0) {
				out += separator + shipDesignInfo(fl, num);
				separator = sep;
				num++;
			}
		return out;
	}
	default String shipDesignInfo(ShipFleet fl, int i)	{
		Empire pl = player();
		boolean isPlayer = isPlayer(fl.empire());
		boolean contact	 = isPlayer || pl.hasContacted(fl.empId());
		String out = "";
		ShipDesign d = fl.visibleDesign(pl.id, i);
		// draw design ID
		if (isPlayer)
			out += bracketed(DESIGN_KEY, d.id()) + " ";
		// draw ship count
		int count = fl.num(d.id());
		out += count + " ";
		// draw ship name
		if (contact)
			out += d.name();
		else
			out += "Unknown ";
		return out;
	}
	default String viewFleetInfo(ShipFleet fleet)	{
		if (fleet.isEmpty())
			return "Empty Fleet";
		Empire pl  = player();
		Empire emp = fleet.empire();
		// Empire
		String out = "Owner = " + shortEmpireInfo(emp);
		// Location
		if (fleet.isOrbiting())
			out += SPACER + "Orbit " + planetName(fleet.system().altId);
		else if (pl.knowETA(fleet)) {
			int destination = fleet.destination().altId;
			int eta = fleet.travelTurnsRemainingAdjusted();
			out += SPACER + "ETA " + planetName(destination) + " = " + eta + " year";
			if (eta>1)
				out += "s";
		}
		else {
			out += SPACER + "Closest System = ";
			StarSystem sys = pl.closestSystem(fleet);
			out += planetName(sys.altId) + SPACER + "Distance = " + ly(sys.distanceTo(fleet));
		}
		out += SPACER + fleetDesignInfo(fleet, SPACER);
		return out;
	}
//	##### TRANSPORTS #####
	default String transportInfo(Transport transport, String sep)	{
		Empire pl  = player();
		Empire emp = transport.empire();
		String out = bracketed(TRANSPORT_KEY, instance().getTransportIndex(transport));
		out += " Size = " + transport.launchSize();
		out += sep + "Owner = " + longEmpireInfo(emp);
		if (pl.knowETA(transport)) {
			SystemView sv = instance().getView(transport.from().altId);
			out += sep + "From " + planetName(sv, sep);
			sv = instance().getView(transport.destination().altId);
			out += sep + "To " + planetName(sv, sep);
			int eta = transport.travelTurnsRemainingAdjusted();
			out += sep + "ETA = " + eta + " year";
			if (eta>1)
				out += "s";
		}
		else
			out += sep + closestSystem(transport, sep);
		return out;
	}
//	##### SYSTEMS #####
	default String planetName(int altId)	{ return planetName(instance().getView(altId), SPACER); }
	default String planetNameCR(int altId)	{ return planetName(instance().getView(altId), NEWLINE); }
	default String planetName(int altId, String sep)	{ return planetName(instance().getView(altId), sep); }
	default String planetName(SystemView sv, String sp)	{
		String out = bracketed(SYSTEM_KEY, sv.system().altId) + " ";
		String name = sv.name();
		out += name;
		if (!sv.scouted()) {
			if (!name.isEmpty())
				out += sp;
			out += "Unexplored";
			return out;
		}
		return out;
	}
	default String shortSystemInfo(SystemView sv)		{
		if (!sv.isColonized()) {
			if (!sv.scouted()) 
				return text("MAIN_UNSCOUTED");
			else if (sv.system().planet().isEnvironmentNone())
				return text("MAIN_NO_PLANETS");
			else if (sv.abandoned())
				return text("MAIN_ABANDONED");
			else
				return text("MAIN_NO_COLONIES");
		}
		String out;
		Empire emp	= sv.empire();
		String name	= emp.raceName();
		String id	= bracketed(EMPIRE_KEY, emp.id) + " ";
		if (!sv.scouted())
			out = text("MAIN_UNSCOUTED") + " " +  id + text("PLANET_WORLD", name);
		else if (emp.isHomeworld(sv.system()))
			out = id + text("PLANET_HOMEWORLD", name);
		else if (emp.isColony(sv.system()))
			out = id + text("PLANET_COLONY", name);
		else
			out = id + text("PLANET_WORLD", name);
		out = emp.replaceTokens(out, "alien");
		return out;
	}
	default String closestSystem(IMappedObject mapObj, String sep)	{
		StarSystem sys = player().closestSystem(mapObj);
		String out = "Closest System = ";
		out += planetName(sys.altId, sep);
		out += sep + "Distance = " + ly(sys.distanceTo(mapObj));
		return out;
	}
//	##### EMPIRES
	default String shortEmpireInfo(Empire emp)		{
		if (emp.isPlayer() || player().hasContacted(emp.id))
			return bracketed(EMPIRE_KEY, emp.id);
		else
			return "Unknown";
	}
	default String longEmpireInfo(Empire emp)		{
		String out = shortEmpireInfo(emp);
		if (emp.isPlayer() || player().hasContacted(emp.id))
			out += " " + emp.name();
		return out;
	}
	default String empireContactInfo(Empire emp, String sep){
		String out;
		boolean inRange = true;
		if (emp.isPlayer()) {
			List<EmpireView> views = player().contacts();
			int n = views.size();
			out = bracketed(EMPIRE_KEY, emp.id) +  " " + emp.name();
			out += sep + text("RACES_KNOWN_EMPIRES", n);
			if (n > 0) {
				int recalled = 0;
				for (EmpireView v : views)
					if (!v.diplomats())
						recalled++;
				out += sep + text("RACES_RECALLED_DIPLOMATS", recalled);
			}
		}
		else if (!player().hasContacted(emp.id)) {
			return "Unknown Empire";
		}
		else {
			out = longEmpireInfo(emp);
			EmpireView view = player().viewForEmpire(emp);
			inRange = view.inEconomicRange();
			if (inRange) {
				out += sep + empireTreaty(emp);
				out += sep + empireTrade(emp);
				if (!view.diplomats())
					out += sep + text("RACES_DIPLOMATS_RECALLED");
			}
			else
				out += sep + text("RACES_OUT_OF_RANGE");
			if (!emp.masksDiplomacy())
				out += sep + empireRelationPct(emp);
		}
		return out;
	}
	default String empireRelationPct(Empire emp)	{
		EmpireView view = emp.viewForEmpire(player());
		int relation = (int) (0.5f + view.embassy().relations());
		String str = text("RACES_DIPLOMACY_RELATIONS_METER");
		return str + EQUAL_SEP + relation + "%";
	}
	default String empireTreaty(Empire emp)			{
		EmpireView view = emp.viewForEmpire(player());
		DiplomaticTreaty treaty = view.embassy().treaty();
		String out = treaty.status(player());
		boolean isAlly = treaty.isAlliance();
		if (isAlly) {
			TreatyAlliance alliance = (TreatyAlliance) treaty;
			int standing = alliance.standing(player());
			out += " level " + standing;
		}
        if (treaty.isPeace() && options().isColdWarMode())
        	out += " " + text("RACES_COLD_WAR");

		return out;
	}
	default String empireTrade(Empire emp)			{
		EmpireView view = emp.viewForEmpire(player());
		int level = view.trade().level();
		if (level == 0)
			return text("RACES_TRADE_NONE");
		else
			return text("RACES_TRADE_LEVEL", level);
	}
	default String viewEmpiresContactInfo(List<Empire>	empires)	{
		String out = "";
		if (empires.isEmpty())
			return out + "Empty Empire List" + NEWLINE;
		for (Empire empire : empires) {
			out += empireContactInfo(empire, NEWLINE);
			out += NEWLINE;
		}
		return out;
	}
	default String viewEmpiresContactInfo()	{ return viewEmpiresContactInfo(player().contactedEmpires()); }

	public class Entries extends ArrayList<String> {
		private static final long serialVersionUID = 1L;
		private final String entry;
		private int	index;
		Entries(String str)			{
			entry = str.trim();
			String[] text = entry.split("\\s+");
			for (int i=0; i<text.length; i++)
				add(text[i]);
			index = 0;
		}
		@Override public String get(int idx)		{ return super.get(idx).toUpperCase(); }
		@Override public String remove(int idx)		{ return rawRemove(idx).toUpperCase(); }
		@Override public boolean remove(Object o)	{
			if (super.remove(o)) {
				index ++;
				return true;
			}
			return false;
		}
		@Override public void add(int id, String s)	{
			index --;
			super.add(id, s);
		}
		@Override public boolean add(String s)		{
			index --;
			return super.add(s);
		}
		@Override public Iterator<String> iterator()				{ return ucL().iterator(); }
		@Override public ListIterator<String> listIterator()		{ return ucL().listIterator(); }
		@Override public ListIterator<String> listIterator(int idx)	{ return ucL().listIterator(idx); }

		private ArrayList<String> ucL()	{
			ArrayList<String> src = new ArrayList<>(this);
			ArrayList<String> ucL = new ArrayList<>();
			for (String s : src)
				ucL.add(s.toUpperCase());
			return ucL;
		}
		public boolean rawContains(String s)	{ return super.contains(s); }
		public String rawRemove(int idx)		{
			if (idx >= size())
				return "";
			index++;
			return super.remove(idx);
		}
		public String remove()					{ return remove(0); }
		public String next()					{ return remove(0); }
		public String get()						{ return get(0); }
		public String rawRemove()				{ return rawRemove(0); }
		public String rawGet(int idx)			{ return super.get(idx); }
		public String rawEntry()				{ return entry; }
		public String entry()					{ return rawEntry().toUpperCase(); }
		public String remains()					{
			if (isEmpty())
				return "";
			String[] par = entry.split("\\s+", index+1);
			clear();
			return par[index];
		}
		public boolean hasDigit()	{ return get().matches(".*\\d.*"); }
	}
}
