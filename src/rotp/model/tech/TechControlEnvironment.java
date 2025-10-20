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
package rotp.model.tech;

import rotp.model.empires.Empire;
import rotp.model.empires.species.Species;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipSpecialColony;

public final class TechControlEnvironment extends Tech {
    private int hostilityAllowed;
    public String specialName;

    public TechControlEnvironment (String typeId, int lv, int seq, boolean b, TechCategory c) {
        id(typeId, seq);
        typeSeq = seq;
        level = lv;
        cat = c;
        free = b;
        init();
    }
    @Override
    public boolean canBeMiniaturized()      { return true; }
    @Override
    public void init() {
        super.init();
        techType = Tech.CONTROL_ENVIRONMENT;
        switch(typeSeq) {
            case 0: hostilityAllowed = PlanetType.HOSTILITY_MINIMAL; return;
            case 1: hostilityAllowed = PlanetType.HOSTILITY_BARREN; return;
            case 2: hostilityAllowed = PlanetType.HOSTILITY_TUNDRA; return;
            case 3: hostilityAllowed = PlanetType.HOSTILITY_DEAD; return;
            case 4: hostilityAllowed = PlanetType.HOSTILITY_INFERNO; return;
            case 5: hostilityAllowed = PlanetType.HOSTILITY_TOXIC; return;
            case 6: hostilityAllowed = PlanetType.HOSTILITY_RADIATED; return;
        }
    }
    @Override
    public boolean isControlEnvironmentTech()    { return true; }
    public int environment()                     { return hostilityAllowed; }
    public int hostilityAllowed()                { return hostilityAllowed; }
    public boolean canColonize(PlanetType pt)    { return pt.hostility() <= hostilityAllowed(); }
    @Override
    public boolean canBeResearched(Species s)	{
        // If Ignores Eco is not No, only environments that are not colonizable are researchable
        switch (s.acceptedPlanetEnvironment()) {
            case "All":
                return false;
            case "Limited":
                switch (hostilityAllowed) {
                    case PlanetType.HOSTILITY_BARREN:
                    case PlanetType.HOSTILITY_TUNDRA:
                    case PlanetType.HOSTILITY_DEAD:
                        return false;
                    default:
                        return true;
                }
            default:
                return true;
        }
    }
    @Override
    public float expansionModeFactor()  { return 3; }
    @Override
    public float baseCost(ShipDesign d) { return (350 + (typeSeq *25)); }
    @Override
    public float baseSize(ShipDesign d) { return 700; }
    @Override
    public boolean isObsolete(Empire c) {
        if (options().restrictedColonization())
            return c.tech().knowsTechForHostility(hostilityAllowed);
        
        TechControlEnvironment topTech = c.tech().topControlEnvironmentTech();        
        return (topTech != null) && (topTech.environment() > environment()) ;
    }
    @Override
    public float baseValue(Empire c) { return c.ai().scientist().baseValue(this); }
    @Override
    public void provideBenefits(Empire c) {
        super.provideBenefits(c);
        if (!isObsolete(c)) {
            c.tech().topControlEnvironmentTech(this);
            c.tech().learnToColonizeHostility(hostilityAllowed);
        }

        ShipSpecialColony sh = new ShipSpecialColony(this);
        c.shipLab().addSpecial(sh);
    }
}
