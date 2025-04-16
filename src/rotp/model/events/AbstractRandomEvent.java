/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gnu.org/licenses/gpl-3.0.html
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
import java.util.List;

import rotp.model.empires.Empire;
import rotp.model.game.IDebugOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.util.ParamInteger;
import rotp.util.Base;

abstract class AbstractRandomEvent implements RandomEvent, Base, Serializable {
	// BR: I forgot to set one... So here is the auto-generated one!
	private static final long serialVersionUID = -6808434634169955261L;
	private List<Empire>  pendingEvents	= new ArrayList<>(); // Do not use: For backward compatibility
	private List<Integer> empireIdPendingEvents	= new ArrayList<>();
	private Integer lastEndedTurn; // BR: new parameter, could be null on reload

	abstract ParamInteger delayTurn();
	abstract ParamInteger returnTurn();

	int nextAllowedTurn()						{ return -1; } // for backward compatibility
	@Override public void	 nextTurn()			{ }
	@Override public boolean repeatable()		{ return returnTurn().get() != 0; }
	@Override public String	 systemKey()		{ return ""; }
	@Override public String	 statusMessage()	{ return ""; }
	@Override public boolean monsterEvent()		{ return false; }
	@Override public void	 validateOnLoad()	{
		if (pendingEvents == null)
			return;
		empireIdPendingEvents = new ArrayList<>();
		for (Empire e : pendingEvents)
			empireIdPendingEvents.add(e.id);
	};
	@Override public int startTurn()			{
		return eventsStartTurn() + delayTurn().get() * difficultyFactor();
	}
	@Override public boolean hasPendingEvents()	{
		if (empireIdPendingEvents == null)
			return false;
		return empireIdPendingEvents.size() > 0; 
	}
	@Override public Empire getPendingEmpire()	{
		if (empireIdPendingEvents == null)
			return null;
		if (empireIdPendingEvents.size()==0)
			return null;
		return galaxy().empire(empireIdPendingEvents.remove(0)); 
	}
	@Override public int minimumTurn()			{
		if (isEventDisabled())
			return Integer.MAX_VALUE;

		int returnTurn = returnTurn().get();
		// Multiple Monster?
		if (returnTurn == -1 && monsterEvent())
			return startTurn();
			
		if (!alreadyOccurred())
			return startTurn();

		// repeatable?
		if (returnTurn != 0)
			return lastEndedTurn + returnTurn;
		else
			return Integer.MAX_VALUE;
	}	
	@Override public void addPendingEvents(Empire emp) {
		empireIdPendingEvents.add(emp.id);
		if (IDebugOptions.debugAutoRun() && IDebugOptions.debugLogEvents())
			turnLog(IGameOptions.AUTORUN_EVENTS, "Pending: " + emp.name() + " # " + notificationText());
	}
	boolean	isEventDisabled()					{
		// Specific Event disabled ?
		if (delayTurn().get() < 0)
			return true;
		String reo = guiOptions().selectedRandomEventOption();
		switch (reo) {
		case IGameOptions.RANDOM_EVENTS_OFF:
			return true;
		case IGameOptions.RANDOM_EVENTS_ON:
			return false;
		case IGameOptions.RANDOM_EVENTS_NO_MONSTERS:
			return monsterEvent();
		case IGameOptions.RANDOM_EVENTS_ONLY_MONSTERS:
			return !(monsterEvent() && techDiscovered());
		case IGameOptions.RANDOM_EVENTS_TECH_MONSTERS:
			if (monsterEvent())
				return !techDiscovered();
		}
		return false;
	}
	void terminateEvent(RandomEvent event)		{
   		setLastEndedTurn();
   		galaxy().events().removeActiveEvent(event);
	}
	private void	setLastEndedTurn() 			{ lastEndedTurn = galaxy().currentTurn(); }
	private int		eventsStartTurn()			{ return IGameOptions.eventsStartTurn.get(); }
	private boolean alreadyOccurred()			{
		if (lastEndedTurn != null)
			return true;
		// Test for backward compatibility
		int nextAllowedTurn = nextAllowedTurn(); // overridable call
		if (nextAllowedTurn < 0) // Never have occurred
			return false;
		// old save: backward compatibility
		lastEndedTurn = nextAllowedTurn - returnTurn().get();
		return true;
	}
    private int	 difficultyFactor()				{
		switch (options().selectedGameDifficulty()) {
	        case IGameOptions.DIFFICULTY_EASIEST:	return 4;
	        case IGameOptions.DIFFICULTY_EASIER:	return 3;
	        case IGameOptions.DIFFICULTY_EASY:		return 2;
	        default:								return 1;
		}
    }
}
