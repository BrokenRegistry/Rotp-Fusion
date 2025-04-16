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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import rotp.model.empires.Empire;
import rotp.model.galaxy.SpaceMonster;
import rotp.model.game.IDebugOptions;
import rotp.model.game.IGameOptions;
import rotp.util.Base;

public class RandomEvents implements Base, Serializable {
	private static final long serialVersionUID = 1L;
	private static final float START_CHANCE = 0.0f;
	private static final float CHANCE_INCR = 0.01f;
	private static final float MAX_CHANCE_INCR = 0.05f;
	private List<RandomEvent> events;
	private List<RandomEvent> activeEvents;
	private RandomEvent lastEvent; // modnar: keep track of last event
	private float eventChance = START_CHANCE;
	// BR: Added option for fixed random (reloading wont change the issue)
	private Long turnSeed   = null;
	private Long listSeed   = null;
	private Long targetSeed = null;
	private long monsterId  = 0;
	private HashMap<String, Integer> eventGNNState;
	private Integer empireIdTechTriggerSpaceAmoeba  = Empire.NULL_ID;
	private Integer empireIdTechTriggerSpaceCrystal = Empire.NULL_ID;
	private Integer empireIdTechTriggerSpacePirates = Empire.NULL_ID;

	public int startTurn()				{ // BR:Made it adjustable
		return IGameOptions.eventsStartTurn.get();
	}
	public RandomEvents()				{
		activeEvents = new ArrayList<>();
		events = new ArrayList<>();
		turnSeed();
		listSeed();
		targetSeed();
		loadEvents();
	}
	private float chanceIncr()			{ return CHANCE_INCR * options().selectedEventsPace(); }
	private float maxChanceIncr()		{ return MAX_CHANCE_INCR * options().selectedEventsPace(); }
	// BR: Added option for fixed random (reloading wont change the issue)
	private Long turnSeed()				{
		if (turnSeed == null)
			turnSeed = rng().nextLong();
		return turnSeed;
	}
	private Long listSeed()				{
		if (listSeed == null)
			listSeed = rng().nextLong();
		return listSeed;
	}
	private Long targetSeed()			{
		if (targetSeed == null)
			targetSeed = rng().nextLong();
		return targetSeed;
	}
	private float turnRnd()				{
		if (options().selectedFixedEventsMode()) {
			turnSeed = new Random(turnSeed()).nextLong();
			return new Random(turnSeed).nextFloat();
		}
		else
			return random();
	}
	private int listRnd(int max)		{
		if (options().selectedFixedEventsMode()) {
			listSeed = new Random(listSeed()).nextLong();
			return new Random(listSeed).nextInt(max);
		}
		else
			return rng().nextInt(max);
	}
	private <T> T listRnd(List<T> list)	{
		if (list == null || list.isEmpty())
			return null;
		return list.get(listRnd(list.size()));
	}
	private int targetRnd()				{
		int numEmp = options().selectedNumberOpponents()+1;
		targetSeed = new Random(targetSeed()).nextLong();
		return new Random(targetSeed).nextInt(numEmp);
	}

	public int empireIdTechTriggerSpaceAmoeba()	{
		if (empireIdTechTriggerSpaceAmoeba == null)
			empireIdTechTriggerSpaceAmoeba = galaxy().techDiscoveryEmpireId(RandomEventSpaceAmoeba.TRIGGER_TECH);
		return empireIdTechTriggerSpaceAmoeba;
	}
	public int empireIdTechTriggerSpaceCrystal()	{
		if (empireIdTechTriggerSpaceCrystal == null)
			empireIdTechTriggerSpaceCrystal = galaxy().techDiscoveryEmpireId(RandomEventSpaceCrystal.TRIGGER_TECH);
		return empireIdTechTriggerSpaceCrystal;
	}
	public int empireIdTechTriggerSpacePirates()	{
		if (empireIdTechTriggerSpacePirates == null)
			empireIdTechTriggerSpacePirates = galaxy().techDiscoveryEmpireId(RandomEventSpacePirates.TRIGGER_TECH);
		return empireIdTechTriggerSpacePirates;
	}
	public void empireIdTechTriggerSpaceAmoeba(int id)	{ empireIdTechTriggerSpaceAmoeba  = id; }
	public void empireIdTechTriggerSpaceCrystal(int id)	{ empireIdTechTriggerSpaceCrystal = id; }
	public void empireIdTechTriggerSpacePirates(int id)	{ empireIdTechTriggerSpacePirates = id; }
	public boolean spaceAmoebaNotTriggered()	{ return empireIdTechTriggerSpaceAmoeba() == Empire.NULL_ID; }
	public boolean spaceCrystalNotTriggered()	{ return empireIdTechTriggerSpaceCrystal() == Empire.NULL_ID; }
	public boolean spacePiratesNotTriggered()	{ return empireIdTechTriggerSpacePirates() == Empire.NULL_ID; }

	public void addActiveEvent(RandomEvent ev)		{ activeEvents.add(ev); }
	public boolean isActiveEvent(RandomEvent ev)	{ return activeEvents.contains(ev); }
	public void removeActiveEvent(RandomEvent ev)	{
		activeEvents.remove(ev);
		if (IDebugOptions.debugAutoRun() && IDebugOptions.debugLogEvents()) {
			turnLog(IGameOptions.AUTORUN_EVENTS, "Remove Event: " + ev.notificationText());
		}
		if (ev.hasPendingEvents()) { // BR: May only happen with "fixed Event mode"
			Empire emp = ev.getPendingEmpire();
			ev.trigger(emp);
			if (IDebugOptions.debugAutoRun() && IDebugOptions.debugLogEvents()) {
				turnLog(IGameOptions.AUTORUN_EVENTS, "Get Pending Event for: " + emp.name());
				turnLog(IGameOptions.AUTORUN_EVENTS, ev.notificationText());
			}
		}
	}
	public void nextTurn() {
		 // BR: To allow RandomEventOption dynamic changes
		IGameOptions opts = options();
		if (opts.disabledRandomEvents()) 
			return;

		// possible that next-turn logic may remove an active event
		List<RandomEvent> tempEvents = new ArrayList<>(activeEvents);
		for (RandomEvent ev: tempEvents)
			ev.nextTurn();

		int turnNum = galaxy().currentTurn();
		if (turnNum < startTurn() && !options().selectedRandomEventOption().equals(IGameOptions.RANDOM_EVENTS_ONLY_MONSTERS))
			return;

		eventChance = min(maxChanceIncr(), eventChance + chanceIncr());
		// eventChance = 1; // TO DO BR: Comment
		// System.out.println("eventChance = " + eventChance);
		if (turnRnd() > eventChance)
			return;
		// System.out.println("Random event to be selected");

		List<RandomEvent> subList = eventSubList();
		if (subList.isEmpty())
			return;
 
		RandomEvent triggeredEvent = listRnd(subList);
		// RandomEvent triggeredEvent = random(events);
		// if (turnNum < triggeredEvent.minimumTurn())
		//	 return;
		
		// modnar: make random events repeatable
		// No removal, dynamically built
		//	if (!triggeredEvent.repeatable()) {
		//		events.remove(triggeredEvent);
		//	}
		eventChance = START_CHANCE; // Reset the probability counter
		
		Empire affectedEmpire;
		if (opts.selectedFixedEventsMode()) {
			affectedEmpire = empireForFixedEvent();
			if (activeEvents.contains(triggeredEvent) && !allowConcurrence(triggeredEvent)) {
				triggeredEvent.addPendingEvents(affectedEmpire);
				return;
			}
		}
		if (!opts.selectedEventsFavorWeak())
			affectedEmpire = randomEmpire();
		else if (triggeredEvent.goodEvent())
			affectedEmpire = empireForGoodEvent();
		else
			affectedEmpire = empireForBadEvent();
		// If Monster & concurrent: create a copy of the event
		if (allowConcurrence(triggeredEvent)) {
			RandomEventMonsters rem = (RandomEventMonsters) triggeredEvent;
			// System.out.println("rem.name() = " + rem.name()); // TO DO BR: Comment
			switch (rem.name()) {
			case "PIRATES":
				rem = new RandomEventSpacePirates();
				monsterId++;
				rem.monsterId = monsterId;
				triggeredEvent = rem;
				break;
			case "CRYSTAL":
				rem = new RandomEventSpaceCrystal();
				monsterId++;
				rem.monsterId = monsterId;
				triggeredEvent = rem;
				break;
			case "AMOEBA":
				rem = new RandomEventSpaceAmoeba();
				monsterId++;
				rem.monsterId = monsterId;
				triggeredEvent = rem;
				break;
			}
			triggeredEvent.trigger(affectedEmpire);
			// Not needed yet... This to allow more customization later
			rem.level = options().monstersLevel();
		}
		else
			triggeredEvent.trigger(affectedEmpire);
		
		lastEvent = triggeredEvent; // modnar: keep track of last event
	   
		if (IDebugOptions.debugAutoRun() && IDebugOptions.debugLogEvents())
			turnLog(IGameOptions.AUTORUN_EVENTS, triggeredEvent.notificationText());
	}
	public RandomEvent activeEventForKey(String key) {
		for (RandomEvent ev: activeEvents) {
			if (ev.systemKey().equals(key))
				return ev;
		}
		return null;
	}
	private boolean allowConcurrence(RandomEvent event) {
		if (event.monsterEvent()) {
			RandomEventMonsters mEv = (RandomEventMonsters) event;
			if (mEv.returnTurn().get() == -1)
				return true;
		}
		return false;
	}
	private List<RandomEvent> eventSubList() { // BR: To allow RandomEventOption dynamic changes
		List<RandomEvent> subList = new ArrayList<>();
		for (RandomEvent ev: events)
			if (isValidEvent (ev))
				subList.add(ev);
		return subList;	
	}
	private boolean isValidEvent (RandomEvent event) {
		IGameOptions opts = options();
		// No Random Events?
		if (!opts.allowRandomEvent(event))
			return false;
		// To Early?
		if (galaxy().currentTurn() < event.minimumTurn())
			return false;
		// Concurrent Monsters?
		if (allowConcurrence(event))
				return true;
		// don't trigger the same event twice in a row
		if (event == lastEvent)
			return false;
		// don't trigger when a duplicate event is still in effect
		if (opts.selectedFixedEventsMode())
			return true; // FixedEvents exception
		for (RandomEvent ev: activeEvents)
			if (event == ev)
				return false;
		return true;
	}
	private void loadEvents()				{  // BR: To allow RandomEventOption dynamic changes
		addEvent(new RandomEventDonation());
		addEvent(new RandomEventDepletedPlanet());
		addEvent(new RandomEventEnrichedPlanet());
		addEvent(new RandomEventFertilePlanet());
		addEvent(new RandomEventComputerVirus());
		addEvent(new RandomEventEarthquake());
		addEvent(new RandomEventIndustrialAccident());
		addEvent(new RandomEventRebellion());
		addEvent(new RandomEventAncientDerelict());
		addEvent(new RandomEventAssassination());
		addEvent(new RandomEventPlague());
		addEvent(new RandomEventSupernova());
		addEvent(new RandomEventPiracy());
		addEvent(new RandomEventComet());
		addEvent(new RandomEventSpaceAmoeba());
		addEvent(new RandomEventSpaceCrystal());
		// modnar: add space pirate random event
		addEvent(new RandomEventSpacePirates());
		// modnar: add Precursor Relic random event
		addEvent(new RandomEventPrecursorRelic());
		// modnar: add Boost Planet baseSize random event
		addEvent(new RandomEventBoostPlanetSize());
		// modnar: add Gauntlet Relic random event
		addEvent(new RandomEventGauntletRelic());
		// addEvent(new RandomEventGenric("EventKey1"));
	}
	private void addEvent(RandomEvent ev)	{
		// if (options().allowRandomEvent(ev)) // BR: To allow RandomEventOption dynamic changes
			events.add(ev);
	}
	private Empire empireForFixedEvent()	{ return galaxy().empire(targetRnd()); }
	private Empire randomEmpire()			{ return listRnd(galaxy().activeEmpires()); }
	private Empire empireForBadEvent()		{
					  // chance of empires for bad events is based power for each empire
		Empire[] emps = galaxy().empires();
		float[] vals = new float[emps.length];
		float total = 0.0f;
		for (int i=0;i<emps.length;i++) {
			Empire emp = emps[i];
			float power = emp.extinct() ? 0 : emp.industrialPowerLevel(emp);
			vals[i] = power;
			total += power;
		}

		float r = total * random();
		for (int i=0;i<emps.length;i++) {
			if (r <= vals[i])
				return emps[i];
			r -= vals[i];
		}

		// should never get here... if we do, have event affect the player
		return player();
	}
	private Empire empireForGoodEvent()		{
		// chance of empires for good events is based 1/power for each empire
		Empire[] emps = galaxy().empires();
		float[] vals = new float[emps.length];
		float total = 0.0f;
		for (int i=0;i<emps.length;i++) {
			Empire emp = emps[i];
			float power = emp.extinct() ? 0 : emp.industrialPowerLevel(emp);
			if (power > 0)
				power = 1/power;
			vals[i] = power;
			total += power;
		}

		float r = total * random();
		for (int i=0;i<emps.length;i++) {
			if (r <= vals[i])
				return emps[i];
			r -= vals[i];
		}

		// should never get here... if we do, have event affect the player
		return player();
	}
	public List<SpaceMonster> monsters()	{
		List<SpaceMonster> monsters = new ArrayList<>();
    	// possible that call may remove an active event
    	List<RandomEvent> tempEvents = new ArrayList<>(activeEvents);
		for(RandomEvent ev: tempEvents) {
			if (ev.monsterEvent()) {
				SpaceMonster monster = ev.monster(false); // do not track
				if (monster != null)
					monsters.add(monster);
			}
		}
		return monsters;
	}
    public void validateOnLoad()			{
    	// possible that call may remove an active event
    	List<RandomEvent> tempEvents = new ArrayList<>(activeEvents);
		for(RandomEvent ev: tempEvents) {
			ev.validateOnLoad();
//			if (ev.monsterEvent()) {
//				SpaceMonster monster = ev.monster(true); // Track lost Monsters
//				monster.event = (IMonsterPos) ev;
//			}
		}
    }
	public HashMap<String, Integer> eventGNNState()	{
		if (eventGNNState == null)
			eventGNNState = new HashMap<>();
		return eventGNNState;
	}
}
