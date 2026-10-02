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
package rotp.ui.notifications;

import rotp.model.galaxy.ShipFleet;
import rotp.model.game.GameSession;
import rotp.model.ships.ShipDesign;
import rotp.util.Base;

public class ColonizeSystemNotification implements TurnNotification, Base {
    private final int sysId;
    private final ShipFleet fleet;
    private final ShipDesign design;

    public static void create(int systemId, ShipFleet fl, ShipDesign d) {
        GameSession.addTurnNotificationForEmpire(fl.empId(),
                new ColonizeSystemNotification(systemId, fl, d));
    }
    private ColonizeSystemNotification(int systemId, ShipFleet fl, ShipDesign d) {
        sysId = systemId;
        fleet = fl;
        design = d;
    }
    public int systemId() { return sysId; }
    public ShipFleet fleet() { return fleet; }
    public ShipDesign design() { return design; }
    @Override
    public String displayOrder() { return PROMPT_COLONIZE; }
    @Override
    public void notifyPlayer() {
        // last minute check to ensure fleet is valid and system is uncolonized
        if (fleet.hasShip(design) && !galaxy().system(sysId).isColonized()) {
            GameSession session = GameSession.instance();
            session.colonizationDecisionAdapter().present(session, sysId, fleet, design);
        }
    }
}
