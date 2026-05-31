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
package rotp.model.ships;

import rotp.model.combat.CombatStack;
import rotp.model.combat.ShipCombatManager;
import rotp.model.tech.TechShipNullifier;

public final class ShipSpecialShipNullifier extends ShipSpecial {
    private static final long serialVersionUID = 1L;
    public ShipSpecialShipNullifier(TechShipNullifier t) {
        tech(t);
        sequence(t.level + .05f);
    }
    @Override
    public TechShipNullifier tech() { return (TechShipNullifier) super.tech(); }
    @Override
    public String name()            { return tech().name();  }
    @Override
    public String desc()            { return tech().brief(); }
    @Override
    public boolean isWeapon()       { return true; }
    @Override
    public int range()              { return tech().range; }
    @Override
    public void fireUpon(CombatStack source, CombatStack target, int count, ShipCombatManager mgr) {
        if (random() < tech().hitChance)
            makeSuccessfulAttack(source, target);
        else
            makeUnsuccessfulAttack(source, target);            
    }
    private void makeSuccessfulAttack(CombatStack source, CombatStack target) {
        int compRed = roll(tech().minComputerRed, tech().maxComputerRed);
        int ecmRed = roll(tech().minECMRed, tech().maxECMRed);

		if (target.isShip()) {
			target.decComputerLevel(compRed);
			target.decMaxMove(tech().speedRed);
			target.decMissileDefense(ecmRed + tech().manvRed);
			target.decBeamDefense(tech().manvRed);
			target.decManeuverability(tech().manvRed);
		}
		else if (target.isColony()) {
			target.decComputerLevel(compRed);
			target.decMissileDefense(ecmRed);
		}
        tech().drawSpecialAttack(source, target, 1, 0);
    }
    private void makeUnsuccessfulAttack(CombatStack source, CombatStack target) {
        tech().drawSpecialAttack(source, target, 1, -1);
    }
}
