/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *	 https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.model.events;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;

import rotp.model.colony.Colony;
import rotp.model.empires.Empire;
import rotp.model.galaxy.SpaceMonster;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.notifications.GNNNotification;

abstract class RandomEventMonsters extends AbstractRandomEvent implements IMonsterPos {
	// BR: I forgot to set one... So here is the auto-generated one!
	private static final long serialVersionUID = 7564985189828624716L;
	private static final int NOTIFY_TURN_COUNT = 3;
	private	  int targetEmpId;
	protected int targetSysId;
	private	  int targetTurnCount = 0;
	protected SpaceMonster monster;
	protected float level = 1.0f;
	private	  float realSpeed = 1.0f;

	private	  HashMap<Integer, Point.Float> path;
	protected long monsterId;
    // BR: Dynamic options for future backward compatibility
	private DynOptions dynamicOptions = new DynOptions();
	private Integer notifyTurnCount;
	private Boolean notified;
	protected int nextEmpId(boolean clear)		{ return Empire.NULL_ID; }
	@Override public DynOptions dynamicOpts()	{
		if (dynamicOptions == null)
			dynamicOptions = new DynOptions();
		return dynamicOptions;
	}
	@Override public boolean notified()			{
		// Boolean notified = getNotified();
		if (notified == null)
			return targetTurnCount <= notifyTurnCount();
		return notified;
	}
	@Override public int targetTurnCount()		{ return targetTurnCount; }
	@Override public Point.Float pos()			{
		int id = bounds(0, targetTurnCount, wanderPath().size()-1);
		return wanderPath().get(id);
	}
	@Override public HashMap<Integer, Point.Float> wanderPath()	{
		if (path == null)
			path = new HashMap<>();
		return path;
	};
	@Override public boolean goodEvent()		{ return false; }
	@Override public boolean monsterEvent() 	{ return true; }
	@Override public SpaceMonster monster(boolean track)	{ return monster; }
	@Override int nextAllowedTurn()				{ // for backward compatibility
		String nextAllowedTurnKey = name() + "_NEXT_ALLOWED_TURN";
		return (Integer) galaxy().dynamicOptions().getInteger(nextAllowedTurnKey, -1);
	}
	@Override public String statusMessage()		{ return text(statusMessageKey()); }
	@Override public String systemKey()			{ return "MAIN_PLANET_EVENT_" + name() + monsterId(); }
	@Override public String notificationText()	{
		String s1 = text(eventName());
		Empire emp = galaxy().empire(targetEmpId);
		s1 = s1.replace("[system]", emp.sv.name(targetSysId));
		s1 = s1.replace("[race]", emp.raceName());
		s1 = emp.replaceTokens(s1, "victim");
		s1 = emp.replaceTokens(s1, "");
		return s1;
	}
	@Override public void trigger(Empire emp)	{
		Empire empire = galaxy().empire(nextEmpId(true));
		if (empire == null)
			empire = emp;
		if (empire == null)
			return;

		log("Starting Space " + dispName() + " event against: "+empire.raceName());
		// System.out.println("Starting Space " + dispName() + " event against: "+emp.raceName());
		if (realSpeed==0)
			realSpeed=1; // backward compatibility

		if (empire.extinct()) {
			targetEmpId = empire.id;
			targetSysId = empire.homeSysId(); // Former home of extinct empire
		}
		else {
			StarSystem targetSystem = random(empire.allColonizedSystems());
			targetSystem.eventKey(systemKey());
			targetEmpId = empire.id;
			targetSysId = targetSystem.id;
		}
		targetTurnCount = newNotifyTurnCount();
		// System.out.println(galaxy().currentTurn() + " Trigger() Pre-init "+ name()+ " targetTurnCount = " + targetTurnCount);
		initMonster();
		// System.out.println(galaxy().currentTurn() + " Trigger: Post-init "+ name() + " targetTurnCount = " + targetTurnCount);

		galaxy().events().addActiveEvent(this);
		if (mustNotify()) 
			approachSystem();	 
	}
	@Override public void nextTurn()			{
		if (isEventDisabled()) {
			StarSystem sys = galaxy().system(targetSysId);
			if (sys != null)
				sys.clearEvent();
			terminateEvent(this);
			return;
		}
		targetTurnCount--;
		// System.out.println(galaxy().currentTurn() + " Next Turn " + monster.name() + " " + targetTurnCount);
		if (monster == null) {
			initMonster();
			System.err.println(galaxy().currentTurn() + " Next Turn InitMonster() " + monster.name() );
		}
		monster.event = this; // TO DO BR: for debug: Comment

		Point.Float pos = pos();
		if (pos != null)
			monster.setXY(pos.x, pos.y);
		else {
			System.err.println(galaxy().currentTurn() + " Next Turn: pos == null " + targetTurnCount + " --> " + monster.name());
			System.out.println(galaxy().currentTurn() + " Next Turn: pos == null " + (int)(minimumTurn()-returnTurn().get()-galaxy().currentTurn()) );
			// System.out.println("Next Turn:"+ name() + " targetTurnCount = " + targetTurnCount);
		}

		if (mustNotify())
			approachSystem();
		else if (targetTurnCount <= 0)
			enterSystem();
	}
	@Override public int  startTurn()			{
		if (techDiscovered() && options().techRandomEvents())
			return 1;
		return super.startTurn();
	}
	@Override public void validateOnLoad()		{
		super.validateOnLoad();
		if (monster == null)
			trackLostMonster();
		monster.event = this;
	};

	// Mandatory and occasional override
	abstract protected String name();
	abstract protected SpaceMonster newMonster(Float speed, Float level);
	abstract protected Integer lootMonster(boolean lootMode);
	protected int getNextSystem()				{
		StarSystem targetSystem = galaxy().system(targetSysId);
		// next system is one of the 10 nearest systems
		// more likely to go to new system (25%) than visited system (5%)
		IGameOptions opts = options();
		float maxDist = opts.monsterMaxDistance();
		float minDist = opts.monsterMinDistance();
		if (opts.isMoO1Monster()) {
			maxDist = 6;
			minDist = 0;
		}
		List<StarSystem> near = targetSystem.ringSystems(minDist, maxDist);
		shuffle(near);
		boolean stopLooking = false;		
		int nextSysId = -1;
		int loops = 0;
		if (near.size() > 0) {
			while (!stopLooking) {
				loops++;
				for (StarSystem sys : near) {
					float chance = monster.vistedSystems().contains(sys.id) ? 0.05f : 0.25f;
					if (random() < chance) {
						nextSysId = sys.id;
						stopLooking = true;
						break;
					}
				}
				if (loops > 10) 
					stopLooking = true;
			}
		}
		return nextSysId;
	}
	// Don't use! For backward compatibility only, when a monster was already launched
	protected int oldEmpId()					{ return targetEmpId; }
	protected int oldSysId()					{ return targetSysId; }
	protected int oldTurnCount()				{ return targetTurnCount; }

	protected float bcLootProbability()			{
		IGameOptions opts = options();
		if (!opts.monstersGiveLoot())
			return 0;
		return opts.monstersLevel();
	}
	protected int bcLootAmount()				{
		IGameOptions opts = options();
		if (!opts.monstersGiveLoot())
			return 0;
		float level	= opts.monstersLevel();
		int turn	= galaxy().currentTurn();
		return (int) (turn*level*level*10);
	}
	protected float researchLootProbability()	{
		IGameOptions opts = options();
		if (!opts.monstersGiveLoot())
			return 0;
		float rcb	  = opts.researchCostBase(100);
		float rFactor = (float) Math.sqrt(rcb);
		return opts.monstersLevel()/rFactor;
	}
	protected int researchLootAmount()			{
		IGameOptions opts = options();
		if (!opts.monstersGiveLoot())
			return 0;
		float level	= opts.monstersLevel();
		int	  turn	= galaxy().currentTurn();
		float rcb	= opts.researchCostBase(10);
		float rFactor = (float) Math.sqrt(rcb);
		return (int) (turn*level*level*10/rFactor);
	}
	private String monsterId()					{ return monsterId==0? "": (""+monsterId); }
	private void monsterDestroyed()				{
		// System.out.println(galaxy().currentTurn() + " monsterDestroyed() " + monster.name());
		//galaxy().events().removeActiveEvent(this);
		monster.plunder();
		Empire emp = monster.lastAttacker();
		String event = eventName();
		String notifKey = event + "_3";
		Integer saleAmount = null;
		if (options().monstersGiveLoot()) {
			notifKey = event + "_PLUNDER";
			saleAmount = lootMonster(true);
			emp.addToTreasury(saleAmount);
		}
		else {
			saleAmount = lootMonster(false);
			if (saleAmount != null)
				emp.addToTreasury(saleAmount);
		}

		if (player().knowsOf(targetEmpId) || !player().sv.name(targetSysId).isEmpty())
			if (updateGNNAllowed(GNN_END)) {
				String txt = notificationText(notifKey, emp, saleAmount);
				GNNNotification.notifyRandomEvent(txt, gnnEvent());
			}
		monster = null;
		terminateEvent(this);
	}
	private void trackLostMonster()				{ // backward compatibility
		System.out.println(galaxy().currentTurn() + " trackLostMonster() ");
		targetEmpId		= oldEmpId();
		targetSysId		= oldSysId();
		targetTurnCount	= oldTurnCount()+1;
		realSpeed		= 1.0f;
		initMonster();
	}
	private String eventName()					{ return "EVENT_SPACE_" + name(); }
	private String dispName()					{ return capitalize(name()); }
	private String statusMessageKey()			{ return "SYSTEMS_STATUS_SPACE" + name(); }
	private String capitalize(String s)			{
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}
	private String gnnEvent()					{ return "GNN_Event_" + dispName(); }	
	private void approachSystem()				{
		setNotified(true);
		StarSystem targetSystem = galaxy().system(targetSysId);
		targetSystem.eventKey(systemKey());
		Empire pl = player();
		if (targetSystem.isColonized()) { 
			if (pl.knowsOf(targetSystem.empire()) || !pl.sv.name(targetSysId).isEmpty())
				if (updateGNNAllowed(GNN_TARGET)) {
					String txt = notificationText(eventName(), targetSystem.empire(), null);
					GNNNotification.notifyRandomEvent(txt, gnnEvent());
				}
		}
		else if (pl.sv.isScouted(targetSysId))
			if (updateGNNAllowed(GNN_TARGET)) {
				String txt = notificationText(eventName()+"_1", null, null);
				GNNNotification.notifyRandomEvent(txt, gnnEvent());
			}
	}
	private boolean nextSystemAllowed()			{ // BR: To allow disappearance
		int maxSystem = options().selectedCrystalMaxSystems();
		return maxSystem == 0 || maxSystem > monster.vistedSystemsCount();
	}
	private void initMonster()					{
		setNotified(false);
		level = options().monstersLevel(); // To avoid uninitialized level!
		monster = newMonster(realSpeed, level);
		monster.event = this; // TO DO BR: for debug: Comment
		StarSystem targetSystem = galaxy().system(targetSysId);
		float distanceToTarget	= realSpeed * (notifyTurnCount()+1);
		Point.Float pos = randomPos(targetSystem.x(), targetSystem.y(), distanceToTarget);
		monster.setXY(pos.x, pos.y);
		monster.destSysId(targetSysId);
		targetTurnCount = travelTurnsRemaining(); // computed here, as the speed may change.
		monster.launch(pos.x, pos.y);
		// System.out.println(galaxy().currentTurn() + " initMonster() " + monster.name() + " " + travelTurnsRemaining());
		buildPath();
		// System.out.println(galaxy().currentTurn() + " initMonster() travelTurnsRemaining " + travelTurnsRemaining());
	}
 	private void enterSystem()					{
		//System.out.println("Monster enter system");
		monster.visitSystem(targetSysId);
		monster.initCombat();
		StarSystem targetSystem = galaxy().system(targetSysId);
		targetSystem.clearEvent();
		Colony col = targetSystem.colony();
		if (!targetSystem.orbitingFleets().isEmpty())
			startCombat();
		else if ((col != null) && col.defense().isArmed())
			startCombat();

		if (monster.alive()) {
			degradePlanet(targetSystem);
			if (nextSystemAllowed())
				moveToNextSystem();
			else
				monsterVanished();
		}
		else 
			monsterDestroyed();
	}
	private void startCombat()					{
		StarSystem targetSystem = galaxy().system(targetSysId);
		galaxy().shipCombat().battle(targetSystem, monster);
	}
	private void monsterVanished()				{ // BR: To allow disappearance
		// System.out.println(galaxy().currentTurn() + " monsterVanished() " + monster.name());
		if (player().knowsOf(galaxy().empire(targetEmpId)) || !player().sv.name(targetSysId).isEmpty())
			if (updateGNNAllowed(GNN_END)) {
				String txt = notificationText(eventName()+"_4", monster.lastAttacker(), null);
				GNNNotification.notifyRandomEvent(txt, gnnEvent());
			}
		monster = null;
		terminateEvent(this);
	}
	private void moveToNextSystem()				{
		// System.out.println(galaxy().currentTurn() + " moveToNextSystem() " + monster.name());
		newNotifyTurnCount();
		setNotified(false);
		monster.travelSpeed = realSpeed; // could have been slowed!
		monster.sysId(targetSysId); // be sure the monster is at the planet
		int nextSysId = getNextSystem();
		if (nextSysId < 0) {
			log("ERR: Could not find next system. Space " + dispName() + " removed.");
			//System.out.println(galaxy().currentTurn() + " ERR: Could not find next system. Space " + dispName() + " removed.");
			terminateEvent(this);
			return;
		}
		log("Space " + dispName() + " moving to system: "+nextSysId);

		targetSysId = nextSysId;	
		monster.destSysId(targetSysId);
		targetTurnCount = travelTurnsRemaining(); // computed here, as the speed may change.
//		if (targetTurnCount <=1) {
//			System.out.println(galaxy().currentTurn() + " moveToNextSystem(): targetTurnCount <=1 " + targetTurnCount + " --> " + monster.name());
//		}
		monster.launch();
		buildPath();
		if (mustNotify()) {
			approachSystem();
		}
	}
	private void setGNNState(int state)			{ eventGNNState().put(eventName(), state); }
	private Integer getGNNState()				{
		Integer state = eventGNNState().get(eventName());
		if (state == null)
			return 0;
		return state;
	}
	private boolean updateGNNAllowed(int state)	{
		Integer currentGNNState = getGNNState();
		// First Time?
		if (state > currentGNNState)
			// Set the new state
			setGNNState(state);

		//System.out.println("options().monstersGNNNotification(): " + options().monstersGNNNotification());
		switch (options().monstersGNNNotification()) {
			case "First": // GNN will only notify the first attack of each monster type.
				// Conditional notification
				return state > currentGNNState;

			case "New": // GNN will notify the first attack of monsters, when they appear.
				// Conditional notification
				return state <= GNN_TARGET;

			case "All":
			default:
				return true;
		}
	}
	private HashMap<String, Integer> eventGNNState()	{ return galaxy().eventGNNState(); }
	private void degradePlanet(StarSystem targetSystem)	{
		Empire emp = targetSystem.empire();
		// colony may have already been destroyed in combat
		if (targetSystem.isColonized() || targetSystem.abandoned())
			monster.degradePlanet(targetSystem);

		if (emp == null)
			return;
		Empire pl = player();
		if (pl.knowsOf(emp) || !pl.sv.name(targetSysId).isEmpty())
			if (updateGNNAllowed(GNN_REDIR)) {
				String txt = notificationText(eventName()+"_2", emp, null);
				GNNNotification.notifyRandomEvent(txt, gnnEvent());
			}
	}
	private String notificationText(String key, Empire emp, Integer amount)	{
		String s1 = text(key);
		if (emp != null) {
			s1 = s1.replace("[system]", emp.sv.name(targetSysId));
			s1 = s1.replace("[race]", emp.raceName());
			s1 = emp.replaceTokens(s1, "victim");
			s1 = emp.replaceTokens(s1, "");
		}
		else 
			s1 = s1.replace("[system]", player().sv.name(targetSysId));
		if (amount != null)
			s1 = s1.replace("[amt]", amount.toString());
		return s1;
	}
	private float avgSpeed()			{
		if (options().isMoO1Monster())
			return 1f;
		float speed = options().monsterSpeed();
		if (speed == 0)
			return 1f / (1.5f * max(1, 100.0f/galaxy().maxNumStarSystems()));
		return speed;
	}
	private int travelTurnsRemaining()	{ return (int) Math.ceil(distanceToTarget()/avgSpeed()); }
	private float distanceToTarget()	{ return galaxy().system(targetSysId).distanceTo(monster); }
	private Point.Float randomPos(float xSrc, float ySrc, double dist) {
		double alpha = random()*Math.PI*2;
		double dx	= dist*Math.cos(alpha);
		double dy	= dist*Math.sin(alpha);
		float x		= (float) (xSrc + dx);
		float y		= (float) (ySrc + dy);
		return new Point.Float(x, y);
	}
	private void lineTo (float xSrc, float ySrc,
			float xDest, float yDest,
			int turnSrc, int turnDest) { // turnDest<turnSrc (Remaining turn)
		if (Float.isNaN(xDest) || Float.isNaN(yDest) || Float.isNaN(xSrc) || Float.isNaN(ySrc)) {
			System.err.println("lineTo has NaN " + xSrc + " " + ySrc + " " + xDest + " " + yDest);
		}
		wanderPath().put(turnSrc, new Point.Float(xSrc, ySrc));
		wanderPath().put(turnDest, new Point.Float(xDest, yDest));
		int n = turnSrc-turnDest;
		if (n<2)
			return;
		float dx = (xSrc-xDest)/n; // because turnDest<turnSrc
		float dy = (ySrc-yDest)/n;

		for (int i = 1; i<n; i++) {
			wanderPath().put((turnDest+i), new Point.Float(xDest+i*dx, yDest+i*dy));
		}
	}
	private Point.Float randomPos(float xSrc, float ySrc,
			float xDest, float yDest,
			double distanceToTarget, double maxDistanceToTravel) {
		Point.Float pos	= null;
		double distance	= Double.MAX_VALUE;
		int maxLoop = 100;
		while(distance > maxDistanceToTravel) {
			if (maxLoop <= 0)
				return null;
			pos = randomPos(xDest, yDest, distanceToTarget);
			distance = pos.distance(xSrc, ySrc);
			maxLoop--;
		}
		return pos;
	}
	private void buildPath()			{
		int approachTurn = notifyTurnCount()+1;
		StarSystem targetSystem = galaxy().system(targetSysId);
		float xTarget	= targetSystem.x();
		float yTarget	= targetSystem.y();
		float xMonster	= monster.fromX();
		float yMonster	= monster.fromY();
		monster.travelSpeed = realSpeed;
		wanderPath().clear();
		// Close enough for straight line?
		if (targetTurnCount <= approachTurn) {
			monster.travelSpeed = distanceToTarget()/targetTurnCount;
			lineTo(xMonster, yMonster, xTarget, yTarget, targetTurnCount, 0);
			return;
		}

		// Build the approach... (Last straight line)
		// Find a point at distance 4 turns of Target
		// and less than targetTurnCount-4 from monster
		double distanceToTarget		= approachTurn * realSpeed;
		double maxDistanceToTravel	= (targetTurnCount - approachTurn) * realSpeed;
		Point.Float approachPos	= randomPos(xMonster, yMonster, xTarget, yTarget,
				distanceToTarget, maxDistanceToTravel);

		// if nothing found, then straight line
		if (approachPos == null) {
			monster.travelSpeed = distanceToTarget()/targetTurnCount;
			lineTo(xMonster, yMonster, xTarget, yTarget, targetTurnCount, 0);
			return;
		}
		// add approach path
		lineTo(approachPos.x, approachPos.y, xTarget, yTarget, approachTurn, 0);

		// Build Monster system departure
		float remainingDistance	= monster.distanceTo(approachPos.x, approachPos.y);
		int remainingTurns		= targetTurnCount - approachTurn;
		int maxTravelTurns		= (int) (Math.ceil(remainingDistance / realSpeed));
		int wanderTurns			= remainingTurns-maxTravelTurns;
		if (wanderTurns <= 1) {
			// then No departure... Direct wandering
//			wanderPath().put(targetTurnCount, new Point.Float(xMonster, yMonster));
			wanderTurns			= remainingTurns;
//			int wanderTurnsAway = wanderTurns/2;
			int wanderTurnsAway = 1;
			int wanderTurnsBack = 1;
			float wanderDistanceAway = wanderTurnsAway * realSpeed;
			float wanderDistanceBack = wanderTurnsBack * realSpeed;
			Point.Float monsterPos	 = new Point.Float(xMonster, yMonster);
			Point.Float wanderPos = wanderPoint(monsterPos, approachPos, wanderDistanceAway, wanderDistanceBack);
			// add wandering path
			int wanderPointTurn = targetTurnCount - wanderTurnsAway;
			lineTo(xMonster, yMonster, wanderPos.x, wanderPos.y, targetTurnCount, wanderPointTurn);
			lineTo(wanderPos.x, wanderPos.y, approachPos.x, approachPos.y, wanderPointTurn, approachTurn);

			return;
		}
		int departureTurns		= NOTIFY_TURN_COUNT;
		int departureTurn		= targetTurnCount-departureTurns;
		float departureDistance	= departureTurns * realSpeed;
		maxDistanceToTravel		= remainingTurns * realSpeed;
		Point.Float departurePos = randomPos(approachPos.x, approachPos.y, xMonster, yMonster,
				departureDistance, maxDistanceToTravel);
		if (departurePos == null) {
			// then Straight line
			lineTo(xMonster, yMonster, approachPos.x, approachPos.y, targetTurnCount, approachTurn);
			return;
		}
		// add departure path
		lineTo(xMonster, yMonster, departurePos.x, departurePos.y, targetTurnCount, departureTurn);

		// Build Monster wandering
		remainingTurns		= departureTurn - approachTurn;
		remainingDistance	= (float) departurePos.distance(approachPos);
		maxTravelTurns		= (int) (Math.ceil(remainingDistance / realSpeed));
		wanderTurns			= remainingTurns;

		if (wanderTurns <= 1)
			return;
		int wanderTurnsAway = wanderTurns/2;
		int wanderTurnsBack = wanderTurns-wanderTurnsAway;
		float wanderDistanceAway = wanderTurnsAway * realSpeed;
		float wanderDistanceBack = wanderTurnsBack * realSpeed;
		Point.Float wanderPos = wanderPoint(departurePos, approachPos, wanderDistanceAway, wanderDistanceBack);

		// add wandering path
		int wanderPointTurn = departureTurn - wanderTurnsAway;
		lineTo(departurePos.x, departurePos.y, wanderPos.x, wanderPos.y, departureTurn, wanderPointTurn);
		lineTo(wanderPos.x, wanderPos.y, approachPos.x, approachPos.y, wanderPointTurn, approachTurn);
	}
	private Point.Float wanderPoint(Point.Float src, Point.Float dest, float distSrc, float distDest) {
		// Shift referential to src
		float xDestShift = dest.x-src.x;
		float yDestShift = dest.y-src.y;
		// Rotate so yDest = 0
		double xDSR2 = yDestShift*yDestShift + xDestShift*xDestShift;
		double xDSR  = Math.sqrt(Math.abs(xDSR2));
		// Squared distances
		double dS2 = distSrc*distSrc;
		double dD2 = distDest*distDest;
		// x an y Wander Shifted and Rotated
		double xWSR = (dS2-dD2+xDSR2)/(2*xDSR);
		double yWSR = Math.sqrt(Math.abs(dS2 - xWSR*xWSR));
		// randomize sign of Y
		if (rng().nextBoolean())
			yWSR = -yWSR;
		// Rotate back
		double cosE = xDestShift/xDSR;
		double sinE = yDestShift/xDSR;
		double xWS = xWSR * cosE - yWSR * sinE;
		double yWS = xWSR * sinE + yWSR * cosE;
		// Shift back
		float xW = (float) (xWS + src.x);
		float yW = (float) (yWS + src.y);

		return new Point.Float(xW, yW);
	}
	private void setNotified(Boolean b)	{ notified = b; }
	private boolean mustNotify()		{
		if (notified == null)
			return targetTurnCount == notifyTurnCount(); // for backward compatibility
		return !notified && targetTurnCount <= notifyTurnCount();
	}
	private int notifyTurnCount()		{
		if (notifyTurnCount == null)
			return NOTIFY_TURN_COUNT;
		return notifyTurnCount;
	}
	private int newNotifyTurnCount()	{
		notifyTurnCount = NOTIFY_TURN_COUNT + roll(-1, 2);
		return notifyTurnCount;
	}
}
