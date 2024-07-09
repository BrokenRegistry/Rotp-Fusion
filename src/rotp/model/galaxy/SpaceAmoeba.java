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
package rotp.model.galaxy;

import rotp.model.colony.Colony;
import rotp.model.combat.CombatStackSpaceAmoeba;
//import static rotp.model.events.RandomEventSpaceAmoeba.monster;

import java.awt.Image;

import rotp.model.planet.PlanetType;
import rotp.ui.main.GalaxyMapPanel;

public class SpaceAmoeba extends SpaceMonster {
    private static final long serialVersionUID = 1L;
    public SpaceAmoeba(Float speed, Float level) {
        super("SPACE_AMOEBA", -4, speed, level);
    }
    @Override
    public void initCombat() {
    	super.initCombat();
        addCombatStack(new CombatStackSpaceAmoeba(this, travelSpeed(), stackLevel()));       
    }
    @Override public void degradePlanet(StarSystem sys) {
        Colony col = sys.colony();
        if (col != null) {
            float prevFact = col.industry().factories();
            col.industry().removeFactories(prevFact*0.9f);
            sys.empire().lastAttacker(this);
            col.destroy();
        }
        sys.planet().degradeToType(PlanetType.BARREN);
        sys.planet().resetWaste();     
        sys.abandoned(false);
    }
    // ShipMonster overriders
	@Override public int maxMapScale()	{ return GalaxyMapPanel.MAX_FLEET_HUGE_SCALE; }
	@Override public Image shipImage()	{ return image("SPACE_AMOEBA"); }
}