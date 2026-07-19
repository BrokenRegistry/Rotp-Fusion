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
package rotp.model.ai;

import static rotp.model.game.IGameOptions.BASE;
import static rotp.model.game.IGameOptions.FUN;
import static rotp.model.game.IGameOptions.FUSION;
import static rotp.model.game.IGameOptions.GOVERNOR;
import static rotp.model.game.IGameOptions.HYBRID;
import static rotp.model.game.IGameOptions.MODNAR;
import static rotp.model.game.IGameOptions.PERSONALITY;
import static rotp.model.game.IGameOptions.RANDOM;
import static rotp.model.game.IGameOptions.RANDOM_ADVANCED;
import static rotp.model.game.IGameOptions.RANDOM_BASIC;
import static rotp.model.game.IGameOptions.RANDOM_NO_RELATIONBAR;
import static rotp.model.game.IGameOptions.ROOKIE;
import static rotp.model.game.IGameOptions.XILMI;
import static rotp.model.game.IGameOptions.advancedAIset;
import static rotp.model.game.IGameOptions.allAIset;
import static rotp.model.game.IGameOptions.baseAIset;
import static rotp.model.game.IGameOptions.noRelationBarAIset;

import java.util.ArrayList;

import rotp.model.ai.interfaces.Diplomat;
import rotp.model.ai.interfaces.FleetCommander;
import rotp.model.ai.interfaces.General;
import rotp.model.ai.interfaces.Governor;
import rotp.model.ai.interfaces.Scientist;
import rotp.model.ai.interfaces.ShipCaptain;
import rotp.model.ai.interfaces.ShipDesigner;
import rotp.model.ai.interfaces.SpyMaster;
import rotp.model.ai.interfaces.Treasurer;
import rotp.model.colony.Colony;
import rotp.model.empires.Empire;
import rotp.model.galaxy.IMappedObject;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GovernorOptions;
import rotp.model.ships.ShipDesign;
import rotp.ui.notifications.BombardSystemNotification;
import rotp.ui.notifications.ColonizeSystemNotification;
import rotp.util.Base;

public final class AI implements Base {

    private final Empire empire;

    private final Diplomat diplomat;
    private final General general;
    private final FleetCommander fleetCommander;
    private final Governor governor;
    private final Scientist scientist;
    private final ShipCaptain captain;
    private final ShipDesigner shipDesigner;
    private final SpyMaster spyMaster;
    private final Treasurer treasurer;


    public AI (Empire e, int aiType) {
        empire = e;

        switch (aiType) {
            case RANDOM:
                aiType = allAIset().random();
                break;
            case RANDOM_BASIC:
                aiType = baseAIset().random();
                break;
            case RANDOM_ADVANCED:
                aiType = advancedAIset().random();
                break;
            case RANDOM_NO_RELATIONBAR:
                aiType = noRelationBarAIset().random();
                break;
            default:
                break;
        }
        if(empire.selectedAI < 0 && aiType != GOVERNOR)
            empire.selectedAI = aiType;
        switch(aiType) {
	        case GOVERNOR:	// TODO BR: progressively move to governor
	            general =        new rotp.model.ai.xilmi.AIGeneral(empire);
	            captain =        new rotp.model.ai.xilmi.AIShipCaptain(empire);
	            governor =       new rotp.model.ai.xilmi.AIGovernor(empire);
	            diplomat =       new rotp.model.ai.fusion.AIDiplomat(empire);
	            shipDesigner =   new rotp.model.ai.xilmi.AIShipDesigner(empire);
	            scientist =      new rotp.model.ai.xilmi.AIScientist(empire);
	            fleetCommander = new rotp.model.ai.governor.AIFleetCommander(empire);
	            spyMaster =      new rotp.model.ai.governor.AISpyMaster(empire);
	            treasurer =      new rotp.model.ai.governor.AITreasurer(empire);
	            break;
            case BASE:
                general =        new rotp.model.ai.base.AIGeneral(empire);
                captain =        new rotp.model.ai.base.AIShipCaptain(empire);
                governor =       new rotp.model.ai.base.AIGovernor(empire);
                diplomat =       new rotp.model.ai.base.AIDiplomat(empire);
                shipDesigner =   new rotp.model.ai.base.AIShipDesigner(empire);
                scientist =      new rotp.model.ai.base.AIScientist(empire);
                fleetCommander = new rotp.model.ai.base.AIFleetCommander(empire);
                spyMaster =      new rotp.model.ai.base.AISpyMaster(empire);
                treasurer =      new rotp.model.ai.base.AITreasurer(empire);
                break;
            case MODNAR:
                general =        new rotp.model.ai.modnar.AIGeneral(empire);
                captain =        new rotp.model.ai.modnar.AIShipCaptain(empire);
                governor =       new rotp.model.ai.modnar.AIGovernor(empire);
                diplomat =       new rotp.model.ai.modnar.AIDiplomat(empire);
                shipDesigner =   new rotp.model.ai.modnar.AIShipDesigner(empire);
                scientist =      new rotp.model.ai.modnar.AIScientist(empire);
                fleetCommander = new rotp.model.ai.modnar.AIFleetCommander(empire);
                spyMaster =      new rotp.model.ai.modnar.AISpyMaster(empire);
                treasurer =      new rotp.model.ai.modnar.AITreasurer(empire);
                break;
            case ROOKIE:
                general =        new rotp.model.ai.rookie.AIGeneral(empire);
                captain =        new rotp.model.ai.rookie.AIShipCaptain(empire);
                governor =       new rotp.model.ai.rookie.AIGovernor(empire);
                diplomat =       new rotp.model.ai.rookie.AIDiplomat(empire);
                shipDesigner =   new rotp.model.ai.rookie.AIShipDesigner(empire);
                scientist =      new rotp.model.ai.rookie.AIScientist(empire);
                fleetCommander = new rotp.model.ai.rookie.AIFleetCommander(empire);
                spyMaster =      new rotp.model.ai.rookie.AISpyMaster(empire);
                treasurer =      new rotp.model.ai.rookie.AITreasurer(empire);
                break;
            case XILMI:
                general =        new rotp.model.ai.xilmi.AIGeneral(empire);
                captain =        new rotp.model.ai.xilmi.AIShipCaptain(empire);
                governor =       new rotp.model.ai.xilmi.AIGovernor(empire);
                diplomat =       new rotp.model.ai.xilmi.AIDiplomat(empire);
                shipDesigner =   new rotp.model.ai.xilmi.AIShipDesigner(empire);
                scientist =      new rotp.model.ai.xilmi.AIScientist(empire);
                fleetCommander = new rotp.model.ai.xilmi.AIFleetCommander(empire);
                spyMaster =      new rotp.model.ai.xilmi.AISpyMaster(empire);
                treasurer =      new rotp.model.ai.xilmi.AITreasurer(empire);
                break;
            case HYBRID:
                general =        new rotp.model.ai.xilmi.AIGeneral(empire);
                captain =        new rotp.model.ai.xilmi.AIShipCaptain(empire);
                governor =       new rotp.model.ai.xilmi.AIGovernor(empire);
                diplomat =       new rotp.model.ai.rookie.AIDiplomat(empire);
                shipDesigner =   new rotp.model.ai.xilmi.AIShipDesigner(empire);
                scientist =      new rotp.model.ai.xilmi.AIScientist(empire);
                fleetCommander = new rotp.model.ai.xilmi.AIFleetCommander(empire);
                spyMaster =      new rotp.model.ai.rookie.AISpyMaster(empire);
                treasurer =      new rotp.model.ai.xilmi.AITreasurer(empire);
                break;
            case FUN:
                general =        new rotp.model.ai.xilmi.AIGeneral(empire);
                captain =        new rotp.model.ai.xilmi.AIShipCaptain(empire);
                governor =       new rotp.model.ai.xilmi.AIGovernor(empire);
                diplomat =       new rotp.model.ai.fun.AIDiplomat(empire);
                shipDesigner =   new rotp.model.ai.xilmi.AIShipDesigner(empire);
                scientist =      new rotp.model.ai.xilmi.AIScientist(empire);
                fleetCommander = new rotp.model.ai.xilmi.AIFleetCommander(empire);
                spyMaster =      new rotp.model.ai.xilmi.AISpyMaster(empire);
                treasurer =      new rotp.model.ai.xilmi.AITreasurer(empire);
                break;
            case PERSONALITY:
                general =        new rotp.model.ai.xilmi.AIGeneral(empire);
                captain =        new rotp.model.ai.xilmi.AIShipCaptain(empire);
                governor =       new rotp.model.ai.xilmi.AIGovernor(empire);
                diplomat =       new rotp.model.ai.fusion.AIDiplomat(empire, 1);
                shipDesigner =   new rotp.model.ai.xilmi.AIShipDesigner(empire);
                scientist =      new rotp.model.ai.xilmi.AIScientist(empire);
                fleetCommander = new rotp.model.ai.xilmi.AIFleetCommander(empire);
                spyMaster =      new rotp.model.ai.xilmi.AISpyMaster(empire);
                treasurer =      new rotp.model.ai.xilmi.AITreasurer(empire);
                break;
            case FUSION:
            default:
                general =        new rotp.model.ai.xilmi.AIGeneral(empire);
                captain =        new rotp.model.ai.xilmi.AIShipCaptain(empire);
                governor =       new rotp.model.ai.xilmi.AIGovernor(empire);
                diplomat =       new rotp.model.ai.fusion.AIDiplomat(empire);
                shipDesigner =   new rotp.model.ai.xilmi.AIShipDesigner(empire);
                scientist =      new rotp.model.ai.xilmi.AIScientist(empire);
                fleetCommander = new rotp.model.ai.xilmi.AIFleetCommander(empire);
                spyMaster =      new rotp.model.ai.xilmi.AISpyMaster(empire);
                treasurer =      new rotp.model.ai.xilmi.AITreasurer(empire);
                break;
        }
    }

    // MISC INTERFACE

    // direct
    public ShipCaptain shipCaptain()                   { return captain; }
    public General general()                           { return general; }
    public Diplomat diplomat()                         { return diplomat; }
    public FleetCommander fleetCommander()             { return fleetCommander; }
    public Governor governor()                         { return governor; }
    public Treasurer treasurer()                       { return treasurer; }
    public Scientist scientist()                       { return scientist; }
    public ShipDesigner shipDesigner()                 { return shipDesigner; }
    public SpyMaster spyMaster()                       { return spyMaster; }

    // uncategorized
    private ColonyTransporter createColony(int sysId, Colony colony, int minTransports, boolean excludeUnderSiege) {
        float targetPct = empire.governorAI().targetPopPct(empire.sv.view(sysId));
		int popNeeded = colony.calcPopNeeded(targetPct, excludeUnderSiege);
        int maxPopToGive = (int) empire.sv.maxPopToGive(sysId, targetPct);
        if ((popNeeded < minTransports) && (maxPopToGive < minTransports))
            return null;

        return new ColonyTransporter(colony, popNeeded, maxPopToGive, minTransports);
    }
    public void sendTransports() {
        long tm0 = System.currentTimeMillis();
        int minTransportSize = empire.generalAI().minTransportSize();
		GovernorOptions govOptions = session().getGovernorOptions();
		boolean isPlayerControlled = empire.isPlayerControlled();
		boolean excludeBesieged = isPlayerControlled? govOptions.excludeTransportToBesieged() : false;
		boolean forceGivey = !isPlayerControlled || govOptions.isAutotransportUngoverned();
		boolean richDisabled = isPlayerControlled && govOptions.isTransportRichDisabled();

		ColonyTransporterList needy = new ColonyTransporterList();
		ColonyTransporterList givey = new ColonyTransporterList();
        for (StarSystem sys: empire.allColonizedSystems()) {
			Colony colony = sys.colony();
			if (colony == null || (isPlayerControlled && colony.noGovAutoTransport()))
				continue;
            ColonyTransporter col = createColony(id(sys), colony, minTransportSize, excludeBesieged);
            if (col != null) {
                if((col.popNeeded >= minTransportSize) && (col.popNeeded >= col.maxPopToGive)
                   && (empire.estimatedFleetDamagePerRoundToArrivingTransports(sys.orbitingFleets()) < empire.tech().topArmorTech().transportHP))
                    needy.add(col);
                else if ((col.maxPopToGive >= minTransportSize) && (col.maxPopToGive > col.popNeeded))
                {
                    if(richDisabled && (sys.planet().hasResource() || sys.planet().isArtifact()))
                        continue;
                    if(forceGivey || colony.isGovernor())
                        givey.add(col);
                }
            }
        }

        if (needy.isEmpty() || givey.isEmpty()) {
            log("sendTransports (NONE): "+empire.raceName()+"   "+(System.currentTimeMillis()-tm0)+"ms");
            return;
        }

        needy.sortByTransportPriority();

        float allowableTurns = (float) (1 + Math.min(7, Math.floor(22 / empire.tech().topSpeed())));
        if(isPlayerControlled)
            allowableTurns = Math.min(govOptions.getTransportMaxTurns(), allowableTurns);
        for(ColonyTransporter needer : needy) {
			givey.sortByTarget(needer);
            boolean allGiversBusy = true;
            for(ColonyTransporter giver : givey)
            {
                if(giver.colony.transport().size() > 0)
                    continue;
                allGiversBusy = false;
                float travelTime = giver.colony.transport().travelTimeAdjusted(needer.colony.starSystem());
                if ((giver.maxPopToGive >= minTransportSize) && (giver.transportPriority < needer.transportPriority)
                        && travelTime <= allowableTurns) {
                    float needed = needer.popNeeded - ((int) (Math.ceil(giver.transportTimeTo(needer))) * needer.growth);
                    int trPop = (int) min(needed, giver.maxPopToGive);
                    if (trPop >= minTransportSize) {
                        giver.sendTransportsTo(needer, trPop);
                    }
                }
            }
            if(allGiversBusy)
                break;
        }
        long tm1 = System.currentTimeMillis();
        log("sendTransports: "+empire.raceName()+"   "+(tm1-tm0)+"ms");
    }
    public void checkColonize(StarSystem sys, ShipFleet fl) {
        if (fl.retreating())
            return;
		// At this point Conflict have been resolved
		// if (sys.orbitingShipsInConflict())
		// 	return;
        if (sys.colony() != null)
            return;
        if (!empire.canColonize(sys.planet().type()))
            return;
        if (sys.orbitingShipsBarColony(fl))
        	return;

        ShipDesign bestDesign = shipDesigner().bestDesignToColonize(fl, sys);
        // if no useable colony design, exit
        if (bestDesign == null)
            return;

        // AT THIS POINT, the fleet can definitely colonize the planet
        // confirm if player controlled & if colonize prompt is disabled
        if (empire.isAIControlled() || (options().autoColonize() && !sys.hasPlague()))
            fl.colonizeSystem(sys, bestDesign);
        else
            ColonizeSystemNotification.create(sys.id, fl, bestDesign);
    }
    //Xilmi: return value of 1 means yes 2 means yes, but target-bombing
    public int promptForBombardment(StarSystem sys, ShipFleet fl) {
        // if player, prompt for decision to bomb instead of deciding here
        if (empire.isPlayerControlled()) {
            if (options().autoBombardNever())
                return 0;
            boolean autoBomb = false;
            // user preference auto-bombard set to always?
            if (options().autoBombardYes())
                autoBomb = true;
            // auto-bombard set to whenever at war?
            boolean atWar = empire.atWarWith(sys.empId());
            if (options().autoBombardWar() && atWar) 
                autoBomb = true;
            // auto-bombard set to whenever at war and not invading?
            int transports = empire.transportsInTransit(sys);
            if (options().autoBombardInvading() && atWar && (transports == 0))
                autoBomb = true;
            int bombTarget = 0;
            if(options().targetBombardAllowedForPlayer() && empire.transportsInTransit(sys) > 0)
                bombTarget = options().selectedBombingTarget();
            BombardSystemNotification.create(id(sys), fl, autoBomb, bombTarget);
            return 0;
        }

        // ail: asking our general for permission
        if(!empire.generalAI().allowedToBomb(sys))
            return 0;

        // estimate bombardment damage and resulting population loss
        float damage = fl.expectedBombardDamage(false);
        float popLoss = damage / 200;
        float sysPop = empire.sv.population(id(sys));

        // if colony will NOT be destroyed, then bombs away!
        if (popLoss < (sysPop * .9))
            return 1;

        // determine number of troops in transit
        int transports = empire.transportsInTransit(sys);

        // if none in transit, then bombs away!
        if (transports < 1)
            return 1;

        // else don't bomb
        //Xilmi: Not a nice way, but a way to tell Xilmi-AIs apart from base-AIs:
        if(empire.generalAI().absolution() != 0)
        {
            if(options().targetBombardAllowedForAI() == true)
            {
                return 2;
            }
        }
        return 0;
    }
	private final class ColonyTransporter implements IMappedObject {
		private final Colony colony;
		private final float x, y;
		private float transportPriority;
		private float growth;
		private int popNeeded;
		private int maxPopToGive;
		private float squareDist;
		private ColonyTransporter(Colony c, int needs, int gives, int min) {
            colony = c;
            StarSystem sys = c.starSystem();
            x = sys.x();
            y = sys.y();
            popNeeded = needs;
            maxPopToGive = gives;

            // calc these values only for needy colonies
            if ((popNeeded >= min) && (popNeeded >= maxPopToGive)) {
                transportPriority = c.empire().fleetCommanderAI().transportPriority(sys);
                growth = c.normalPopGrowth();
            }
        }
        @Override
        public float x() { return x; }
        @Override
        public float y() { return y; }
        private float transportTimeTo(ColonyTransporter dest) {
            return colony.starSystem().transportTimeTo(dest.colony.starSystem());
        }
        private void sendTransportsTo(ColonyTransporter dest, int trPop) {
            colony.scheduleTransportsToSystem(dest.colony.starSystem(), trPop);
            maxPopToGive = 0;
            dest.popNeeded -= trPop;
        }
		private void setSquareDistance(ColonyTransporter target) {
			float dx = x-target.x;
			float dy = y-target.y;
			squareDist = dx*dx + dy*dy;
		}
    }
	private final class ColonyTransporterList extends ArrayList<ColonyTransporter> {
		private static final long serialVersionUID = 1L;
		private void sortByTransportPriority()	{
			sort((ColonyTransporter col1, ColonyTransporter col2)
					-> Float.compare(col1.transportPriority, col2.transportPriority));
		}
		private void sortByTarget(ColonyTransporter target)	{
			// BR: To allow quicker sorting, distances are computed only once.
			for (ColonyTransporter colTr : this)
				colTr.setSquareDistance(target);
			sort( (ColonyTransporter col1, ColonyTransporter col2)
					-> Float.compare(col1.squareDist, col2.squareDist));
		}
	}
}
