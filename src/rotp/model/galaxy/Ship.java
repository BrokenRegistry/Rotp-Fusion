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

import java.util.Comparator;

import rotp.model.Sprite;
import rotp.model.empires.Empire;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.sprites.FlightPathSprite;
import rotp.util.Base;

public interface Ship extends IMappedObject, Base, Sprite {
    static final int NOT_LAUNCHED = -1;
    default float hullPoints()          { return 0; }
    default boolean isRallied()         { return false; }
    default boolean passesThroughNebula(IMappedObject to) { return passesThroughNebula(this, to); }
    default boolean validDestination(int sysId) { return canSendTo(sysId);  }
    default boolean nullDest()          { return destSysId() == StarSystem.NULL_ID; }
    default boolean retreating()        { return false; }
    default boolean isTransport()       { return false; }

    @Override
    default int displayPriority() { return 8; }
    @Override
    default boolean hasDisplayPanel() { return true; }

    public boolean canSendTo(int sysId);
    public float travelSpeed();
    public float launchTime();
    public float arrivalTimeAdjusted();
    public boolean visibleTo(int empId);

    // empId() and empire() provide the same information and either could be defined in terms of the other.
    // ShipFleet directly stores the empId and defines empire() using the empId.
    // Transport directly stores the empire and defines empId() using the empire.
    // Either works fine. Which to provide a default implementation for is essentially arbitrary.
    // We just can't provide a default implementation for *both* empId() and empire().
    // Unfortunately, there is no Java construct for "any implementation must override at least one of these two functions."
    // https://softwareengineering.stackexchange.com/questions/287234/is-it-good-practice-to-implement-two-java-8-default-methods-in-terms-of-each-oth
    public int empId();
    default Empire empire() { return galaxy().empire(empId()); }
    public int destSysId();
    default StarSystem destination() { return galaxy().system(destSysId()); }
    // Rally systems are treated differently from other destinations, but sometimes we just want either one.
    default StarSystem destinationOrRallySystem() { return destination(); }
    default float destX() { return destination().x(); }
    default float destY() { return destination().y(); }
    // destination is always a star system, but origin point need not be?
    public float fromX();
    public float fromY();
    default float transitX() {
        return fromX() + travelPct()*(destX() - fromX());
    }
    default float transitY() {
        return fromY() + travelPct()*(destY() - fromY());
    }
    default float travelPct(float currTime) {
        if ((launchTime() == NOT_LAUNCHED) || (launchTime() == currTime))
            return 0;
        else 
        	return (float) ((currTime-launchTime()) / Math.ceil(arrivalTimeAdjusted()-launchTime()));
//            return (currTime-launchTime()) / (arrivalTimeAdjusted()-launchTime());
    }
    default float travelPct() {
        return travelPct(galaxy().currentTime());
    }
    default float transitXlastTurn() {
        return fromX() + travelPct(galaxy().currentTime() - Galaxy.TIME_PER_TURN)*(destX() - fromX());
    }
    default float transitYlastTurn() {
        return fromY() + travelPct(galaxy().currentTime() - Galaxy.TIME_PER_TURN)*(destY() - fromY());
    }

    public boolean inTransit();
    // This default travelTurnsRemaining() assumes that arrivalTime is always well-defined even if the Ship is not in transit (as it is for ShipFleet).
    default int travelTurnsRemainingAdjusted() { return (int)Math.ceil(arrivalTimeAdjusted() - galaxy().currentTime()); }
    @Override // from IMappedObject
    default float x() { return inTransit() ? transitX() : fromX(); }
    @Override // from IMappedObject
    default float y() { return inTransit() ? transitY() : fromY(); }

	public boolean isDeployed();
    public FlightPathSprite pathSprite();
// BR: To fix dHannash resulting pathSprite bug
//    default FlightPathSprite pathSpriteTo(StarSystem sys) {
//        return new FlightPathSprite(this, sys);
//    }
    public int maxMapScale();
    public void setDisplayed(GalaxyMapPanel map);
    public boolean displayed();

    public boolean isPotentiallyArmed(Empire e);
    public static Comparator<Ship> ARRIVAL_TIME = (Ship sh1, Ship sh2) -> Base.compare(sh1==null? -99 : sh1.arrivalTimeAdjusted(), sh2==null? -99 : sh2.arrivalTimeAdjusted());
    public static Comparator<Ship> EMPIRE_ID = (Ship sh1, Ship sh2) -> Base.compare(sh1==null? -1 : sh1.empId(), sh2==null? -1 : sh2.empId());
}
