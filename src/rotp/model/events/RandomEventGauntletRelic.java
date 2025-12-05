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
import rotp.model.planet.Planet;
import rotp.model.tech.TechTree;
import rotp.ui.notifications.GNNNotification;
import rotp.ui.util.ParamInteger;

// modnar: add Gauntlet Relic random event
// give free Techs in Planetology category
// cut population on all colonies in half
// baseSize +10 for all planets (less than size-90) in target empire
public class RandomEventGauntletRelic extends AbstractRandomEvent {
    private static final long serialVersionUID = 1L;
    private static final int MAX_TECHS_DISCOVERED = 5; // 5 free techs
    private int empId;
    @Override ParamInteger delayTurn()		{ return IGameOptions.gauntletDelayTurn; }
    @Override ParamInteger returnTurn()		{ return IGameOptions.gauntletReturnTurn; }
    @Override public boolean goodEvent()	{ return true; } // classify as positive event, has galactic effects
    @Override
    public String notificationText()    {
        String s1 = text("EVENT_RELIC_GAUNTLET");
        s1 = galaxy().empire(empId).replaceTokens(s1, "target");
        return s1;
    }
    private String notificationText(boolean hasContact) {
    	if (hasContact)
    		return notificationText();
    	else
    		return text("EVENT_RELIC_GAUNTLET_NO_CONTACT");
    }
    @Override
    public void trigger(Empire emp) {
        
        // step through all colonized systems
        // cut population on all colonies in half
        for (Empire e: galaxy().empires()) {
            for (StarSystem sys: e.allColonizedSystems()) {
                sys.planet().colony().setPopulation( 0.5f * sys.population() );
            }
        }

    	if (emp == null || emp.extinct()) {
        	GNNNotification.notifyRandomEvent(notificationText(emp.isPlayer() 
    				|| player().hasContact(emp)), "GNN_Event_Relic_Gauntlet");
            return;
    	}

        // find all colonies in target empire that are less than base size 90
        // increase base size of those planets 10
        for (StarSystem sys : emp.allColonizedSystems()) {
            Planet pl = sys.planet();
            if (pl.baseSize() <= 90.0f) {
                pl.baseSize(pl.baseSize() + 10.0f);
            }
        }
        
        // find unknown Techs in Planetology category
        List<String> availableTechs = new ArrayList<>();

        TechTree empTech = emp.tech();
        int maxConsLevel = (int) empTech.planetology().techLevel()+10;

        // BR: fixed tech Library deletion
        // allTechs() is the static tech library list!
        // List<String> unkPlanetologyTechs =  empTech.planetology().allTechs();
		List<String> unkPlanetologyTechs = empTech.planetology().allowedTechs();
        unkPlanetologyTechs.removeAll(empTech.planetology().knownTechs());
        for (String techId: unkPlanetologyTechs) {
            if (tech(techId).level() <= maxConsLevel && !tech(techId).restricted)
                availableTechs.add(techId);
        }

        if (availableTechs.isEmpty()) {
        	GNNNotification.notifyRandomEvent(notificationText(emp.isPlayer() 
    				|| player().hasContact(emp)), "GNN_Event_Relic_Gauntlet");
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
    	GNNNotification.notifyRandomEvent(notificationText(emp.isPlayer() 
				|| player().hasContact(emp)), "GNN_Event_Relic_Gauntlet");

        for (String techId: discoveredTechs)
            emp.plunderShipTech(tech(techId), -1);
    }
}
