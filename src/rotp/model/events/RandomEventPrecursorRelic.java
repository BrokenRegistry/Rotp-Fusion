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

import java.util.ArrayList;
import java.util.List;

import rotp.model.empires.Empire;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.IGameOptions;
import rotp.model.planet.PlanetFactory;
import rotp.model.tech.TechTree;
import rotp.ui.notifications.GNNNotification;
import rotp.ui.util.ParamInteger;

// modnar: add Precursor Relic random event
// give free Techs in Construction category
// generates colonizable planets in some systems with "No Colonizable Planets"
public class RandomEventPrecursorRelic extends AbstractRandomEvent {
    private static final long serialVersionUID = 1L;
    private static final int MAX_TECHS_DISCOVERED = 5; // 5 free techs
    private int empId;
    @Override ParamInteger delayTurn()		{ return IGameOptions.relicDelayTurn; }
    @Override ParamInteger returnTurn()		{ return IGameOptions.relicReturnTurn; }
    @Override public boolean goodEvent()	{ return true; } // classify as positive event, has galactic effects
    @Override
    public String notificationText()    {
        String s1 = text("EVENT_RELIC_PLANETS");
        s1 = galaxy().empire(empId).replaceTokens(s1, "target");
        return s1;
    }
    // BR: Fix for "Precursor Relic" event; Now every one has the same chance
    private String notificationText(boolean hasContact) {
    	if (hasContact)
    		return notificationText();
    	else
    		return text("EVENT_RELIC_PLANETS_NO_CONTACT");
    }
    @Override
    public void trigger(Empire emp) {
        
        // find all star systems that have no planets
        List<StarSystem> systems = new ArrayList<>();
        for (StarSystem sys : galaxy().starSystems())
            if (sys.planet().type().isAsteroids())
            	systems.add(sys);

        if (!systems.isEmpty()) {
            float richArtifactProb = 0.02f; // BR: to replace the nebulae fixed bug
            // go through planet generation again for those empty star systems
            for (StarSystem sys : systems) {
				do {
					sys.planet(PlanetFactory.createPlanet(sys, session().populationBonus()));
				}
				while(sys.hasAsteroid());
                sys.addEvent(new SystemRandomEvent("SYSEVENT_PRECURSOR_RELIC"));
                if(rng().nextFloat()<richArtifactProb) {
                	richArtifactProb = 0;
                	sys.planet().setArtifact();
                	if (rng().nextBoolean())
                		sys.planet().setResourceUltraRich();
                	else
                		sys.planet().setResourceRich();
                }
            }
            if (emp == null || emp.extinct()) {
            	GNNNotification.notifyRandomEvent(notificationText(emp.isPlayer() 
            				|| player().hasContact(emp)), "GNN_Event_Relic_Planets");
            	return;
            }
        }
        // find unknown Techs in Construction category
        List<String> availableTechs = new ArrayList<>();

        TechTree empTech = emp.tech();
        int maxConsLevel = (int) empTech.construction().techLevel()+10;
        
        // BR: fixed tech Library deletion
        // allTechs() is the static tech library list!
        // List<String> unkConstructionTechs =  empTech.construction().allTechs();
		List<String> unkConstructionTechs = empTech.construction().allowedTechs();
        unkConstructionTechs.removeAll(empTech.construction().knownTechs());
        for (String techId: unkConstructionTechs) {
            if (tech(techId).level() <= maxConsLevel && !tech(techId).restricted)
                availableTechs.add(techId);
        }

        if (availableTechs.isEmpty()) {
        	GNNNotification.notifyRandomEvent(notificationText(emp.isPlayer() 
        				|| player().hasContact(emp)), "GNN_Event_Relic_Planets");
        	return;
        }

        List<String> discoveredTechs = new ArrayList<>();
        if (availableTechs.size() <= MAX_TECHS_DISCOVERED)
            discoveredTechs.addAll(availableTechs);
        else {
            while (discoveredTechs.size() < MAX_TECHS_DISCOVERED) {
                String techId = random(availableTechs);
                availableTechs.remove(techId);
                discoveredTechs.add(techId);
            }
        }

        empId = emp.id;
        // BR: Fix for "Precursor Relic" event; Now every one has the same chance
		// if (emp.isPlayer() || player().hasContact(emp))
		//     GNNNotification.notifyRandomEvent(notificationText(), "GNN_Event_Relic_Planets");
        GNNNotification.notifyRandomEvent(notificationText(emp.isPlayer() 
        				|| player().hasContact(emp)), "GNN_Event_Relic_Planets");

        for (String techId: discoveredTechs)
            emp.plunderShipTech(tech(techId), -1);
    }
}
