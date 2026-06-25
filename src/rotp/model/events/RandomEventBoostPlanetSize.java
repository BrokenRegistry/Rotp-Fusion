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
import rotp.ui.notifications.GNNNotification;
import rotp.ui.util.ParamInteger;

// modnar: add Boost Planet baseSize random event
// increase planet baseSize for a small planet
public class RandomEventBoostPlanetSize extends AbstractRandomEvent {
    private static final long serialVersionUID = 1L;
    private int empId;
    private int sysId;
    @Override ParamInteger delayTurn()		{ return IGameOptions.sizeBoostDelayTurn; }
    @Override ParamInteger returnTurn()		{ return IGameOptions.sizeBoostReturnTurn; }
    @Override public boolean goodEvent()	{ return true; }
    @Override
    public String notificationText()    {
        String s1 = text("EVENT_SIZEBOOST");
        s1 = s1.replace("[system]", galaxy().empire(empId).sv.name(sysId));
        s1 = galaxy().empire(empId).replaceTokens(s1, "target");
        return s1;
    }
    @Override
    public void trigger(Empire emp) {
    	if (emp == null || emp.extinct())
    		return;
        // find a random colony that is less than base size 50
        List<StarSystem> systems = new ArrayList<>();
        for (StarSystem sys : emp.allColonizedSystems()) {
            Planet pl = sys.planet();
            if (pl.baseSize() <= 50.0f) 
                systems.add(sys);
        }
        if (systems.isEmpty())
            return;

        StarSystem targetSystem = random(systems);
        // increase that planet's base size by 50
        targetSystem.planet().baseSize(targetSystem.planet().baseSize() + 50.0f);
        targetSystem.addEvent(new SystemRandomEvent("SYSEVENT_SIZEBOOST"));

        empId = emp.id;
        sysId = targetSystem.id;
        if (player().knowsOf(empId) && player().sv.hasName(sysId))
            GNNNotification.notifyRandomEvent(notificationText(), "GNN_Event_SizeBoost");
    }
}
