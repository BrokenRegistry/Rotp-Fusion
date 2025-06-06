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
package rotp.model.ai.xilmi;

import static rotp.model.tech.Tech.ARMOR;
import static rotp.model.tech.Tech.BIOLOGICAL_WEAPON;
import static rotp.model.tech.Tech.CLOAKING;
import static rotp.model.tech.TechCategory.PROPULSION;
import static rotp.model.tech.TechCategory.WEAPON;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rotp.model.ai.interfaces.Scientist;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.galaxy.StarSystem;
import rotp.model.ships.ShipDesign;
import rotp.model.tech.Tech;
import rotp.model.tech.TechAmoebaEatShips;
import rotp.model.tech.TechArmor;
import rotp.model.tech.TechAtmosphereEnrichment;
import rotp.model.tech.TechAutomatedRepair;
import rotp.model.tech.TechBattleComputer;
import rotp.model.tech.TechBattleSuit;
import rotp.model.tech.TechBeamFocus;
import rotp.model.tech.TechBiologicalAntidote;
import rotp.model.tech.TechBiologicalWeapon;
import rotp.model.tech.TechBlackHole;
import rotp.model.tech.TechBombWeapon;
import rotp.model.tech.TechCategory;
import rotp.model.tech.TechCloaking;
import rotp.model.tech.TechCloning;
import rotp.model.tech.TechCombatTransporter;
import rotp.model.tech.TechControlEnvironment;
import rotp.model.tech.TechDeflectorShield;
import rotp.model.tech.TechDisplacement;
import rotp.model.tech.TechECMJammer;
import rotp.model.tech.TechEcoRestoration;
import rotp.model.tech.TechEnergyPulsar;
import rotp.model.tech.TechEngineWarp;
import rotp.model.tech.TechFuelRange;
import rotp.model.tech.TechFutureComputer;
import rotp.model.tech.TechFutureConstruction;
import rotp.model.tech.TechFutureForceField;
import rotp.model.tech.TechFuturePlanetology;
import rotp.model.tech.TechFuturePropulsion;
import rotp.model.tech.TechFutureWeapon;
import rotp.model.tech.TechHandWeapon;
import rotp.model.tech.TechHyperspaceComm;
import rotp.model.tech.TechImprovedIndustrial;
import rotp.model.tech.TechImprovedTerraforming;
import rotp.model.tech.TechIndustrialWaste;
import rotp.model.tech.TechMissileShield;
import rotp.model.tech.TechMissileWeapon;
import rotp.model.tech.TechPersonalShield;
import rotp.model.tech.TechPlanetaryShield;
import rotp.model.tech.TechRepulsor;
import rotp.model.tech.TechResistSpecial;
import rotp.model.tech.TechRoboticControls;
import rotp.model.tech.TechScanner;
import rotp.model.tech.TechShipInertial;
import rotp.model.tech.TechShipNullifier;
import rotp.model.tech.TechShipWeapon;
import rotp.model.tech.TechSoilEnrichment;
import rotp.model.tech.TechSquidInk;
import rotp.model.tech.TechStargate;
import rotp.model.tech.TechStasisField;
import rotp.model.tech.TechStreamProjector;
import rotp.model.tech.TechSubspaceInterdictor;
import rotp.model.tech.TechTeleporter;
import rotp.model.tech.TechTorpedoWeapon;
import rotp.model.tech.TechTree;
import rotp.ui.notifications.SelectTechNotification;
import rotp.util.Base;

public class AIScientist implements Base, Scientist {
    private static final float NEW_QUINTILE_BONUS = 0.10f;
    private final Empire empire;

    public AIScientist (Empire c) {
        empire = c;
    }
//-----------------------------------
// PUBLIC INTERFACE
//-----------------------------------
    @Override
    public Tech mostDesirableTech(List<Tech> techs) {
        Tech.comparatorCiv = empire;
        Collections.sort(techs, Tech.RESEARCH_VALUE);
        return techs.get(0);
    }
    @Override
    public void setTechTreeAllocations() {
        if(empire.tech().researchCompleted())
            return;
        // invoked after nextTurn() processing is complete on each civ's turn
        setDefaultTechTreeAllocations();
        //ail: This happens at the beginning before we see whether we want to switch this off. But we want accidental excess to go to research so to not waste it.
        if(!empire.divertColonyExcessToResearch() && !empire.tech().researchCompleted())
            empire.toggleColonyExcessToResearch();
        if(empire.divertColonyExcessToResearch() && empire.tech().researchCompleted())
            empire.toggleColonyExcessToResearch();
        //ail: first I stop researching where there's no techs left
        int leftOverAlloc = 0;
        for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
            //System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" "+empire.tech().category(j).id()+" alloc before adjust: "+empire.tech().category(j).allocation());
            if (empire.tech().category(j).possibleTechs().isEmpty())
            {
                leftOverAlloc+=empire.tech().category(j).allocation();
                empire.tech().category(j).allocation(0);
            }
        }
        /*if(leftOverAlloc >= 60)
        {
            setDefaultTechTreeAllocations();
            return;
        }*/
        while(leftOverAlloc > 0)
        {
            for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
                if (!empire.tech().category(j).possibleTechs().isEmpty())
                {
                    empire.tech().category(j).adjustAllocation(1);
                    leftOverAlloc--;
                }
                if(leftOverAlloc <= 0)
                    break;
            }
        }
        int futureTechs = 0;
        for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
            if (empire.tech().category(j).studyingFutureTech())
                futureTechs++;
        }
        //second I stop researching techs with too high of a discovery-chance
        for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
            Tech currentTechResearching = empire.tech().category(j).tech(empire.tech().category(j).currentTech());
            boolean researchingSomethingWeDontReallyWant = false;
            if(currentTechResearching != null)
            {
                //System.out.print("\n"+empire.name()+" "+empire.tech().category(j).id()+" "+discoveryChanceOfCategoryIfAllocationWasZero(j)+" > "+empire.tech().category(j).allocation()+" Prio: "+researchPriority(currentTechResearching)+" warmode: "+warMode());
                if(researchValueAllocation(currentTechResearching) == 0 && futureTechs < 6)
                {
                    researchingSomethingWeDontReallyWant = true;
                    //System.out.print("\n"+empire.name()+" "+empire.tech().category(j).id()+" reduced because "+currentTechResearching.name()+" is either owned by someone else or not something we want.");
                }
            }
            if (discoveryChanceOfCategoryIfAllocationWasZero(j) > min(empire.tech().category(j).allocation(), 50f/3f) || researchingSomethingWeDontReallyWant)
            {
                leftOverAlloc+=empire.tech().category(j).allocation();
                empire.tech().category(j).allocation(0);
            }
        }
        while(leftOverAlloc > 0)
        {
            boolean couldSpend = false;
            for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
                Tech currentTechResearching = empire.tech().category(j).tech(empire.tech().category(j).currentTech());
                boolean researchingSomethingWeDontReallyWant = false;
                if(currentTechResearching != null)
                    if(researchValueAllocation(currentTechResearching) == 0 && futureTechs < 6)
                        researchingSomethingWeDontReallyWant = true;
                if (!empire.tech().category(j).possibleTechs().isEmpty()
                        && discoveryChanceOfCategoryIfAllocationWasZero(j) <= min(empire.tech().category(j).allocation(), 50f/3f)
                        && !researchingSomethingWeDontReallyWant)
                {
                    empire.tech().category(j).adjustAllocation(1);
                    leftOverAlloc--;
                    couldSpend = true;
                    //System.out.print("\n"+empire.name()+" "+empire.tech().category(j).id()+" put leftover into "+currentTechResearching.name());
                }
                if(leftOverAlloc <= 0)
                    break;
            }
            if(!couldSpend)
            {
                for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
                    if (!empire.tech().category(j).possibleTechs().isEmpty())
                    {
                        empire.tech().category(j).adjustAllocation(1);
                        leftOverAlloc--;
                    }
                    if(leftOverAlloc <= 0)
                        break;
                }
            }
        }
        //and lastly i stop researching future techs when there's still others
        if(futureTechs == 6)
            return;
        for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
            if (empire.tech().category(j).studyingFutureTech())
            {
                leftOverAlloc+=empire.tech().category(j).allocation();
                empire.tech().category(j).allocation(0);
            }
        }
        while(leftOverAlloc > 0)
        {
            boolean couldSpend = false;
            for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
                if (!empire.tech().category(j).possibleTechs().isEmpty()
                        && !empire.tech().category(j).studyingFutureTech()
                        && discoveryChanceOfCategoryIfAllocationWasZero(j) <= min(empire.tech().category(j).allocation(), 50f/3f))
                {
                    empire.tech().category(j).adjustAllocation(1);
                    leftOverAlloc--;
                    couldSpend = true;
                }
                if(leftOverAlloc <= 0)
                    break;
            }
            if(!couldSpend)
            {
                for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
                    if (!empire.tech().category(j).possibleTechs().isEmpty()
                            && discoveryChanceOfCategoryIfAllocationWasZero(j) <= min(empire.tech().category(j).allocation(), 50f/3f))
                    {
                        empire.tech().category(j).adjustAllocation(1);
                        leftOverAlloc--;
                        couldSpend = true;
                    }
                    if(leftOverAlloc <= 0)
                        break;
                }
            }
            if(!couldSpend)
            {
                for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
                    if (!empire.tech().category(j).possibleTechs().isEmpty())
                    {
                        empire.tech().category(j).adjustAllocation(1);
                        leftOverAlloc--;
                    }
                    if(leftOverAlloc <= 0)
                        break;
                }
            }
        }
        /*for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
            if(empire.tech().category(j).currentTech() != null)
                System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" "+empire.tech().category(j).key()+": "+empire.tech().category(j).currentTechName()+": "+empire.tech().category(j).allocation()+" of "+empire.totalPlanetaryResearch()+" "+discoveryChanceOfCategoryIfAllocationWasZero(j)+"%");
        }*/
    }
    @Override
    public void setDefaultTechTreeAllocations() {
        // invoked directly when the TechTree is first created
        if (empire.isPlayerControlled()) {
            empire.tech().computer().allocation(10);
            empire.tech().construction().allocation(10);
            empire.tech().forceField().allocation(10);
            empire.tech().planetology().allocation(10);
            empire.tech().propulsion().allocation(10);
            empire.tech().weapon().allocation(10);
            return;
        }
        
        if (empire.tech().topFuelRangeTech().range() < 4 && empire.tech().propulsion().techLevel() < 5) {
            empire.tech().computer().allocation(0);
            empire.tech().construction().allocation(0);
            empire.tech().forceField().allocation(0);
            empire.tech().planetology().allocation(0);
            empire.tech().propulsion().allocation(60);
            empire.tech().weapon().allocation(0);
            return;
        }

        float totalTechCost = 0;
        
        float computerMod = 1.0f;
        float constructionMod = 1.0f;
        float forcefieldMod = 1.0f;
        float planetologyMod = 1.0f;
        float propulsionMod = 1.0f;
        float weaponMod = 1.0f;
        
        if(empire.diplomatAI().getVariant() == 1) {
            if(empire.leader().isDiplomat())
                forcefieldMod = 0.5f;
            if(empire.leader().isEcologist())
                planetologyMod = 0.5f;
            if(empire.leader().isExpansionist())
                propulsionMod = 0.5f;
            if(empire.leader().isIndustrialist())
                constructionMod = 0.5f;
            if(empire.leader().isMilitarist())
                weaponMod = 0.5f;
            if(empire.leader().isTechnologist())
                computerMod = 0.5f;
        }
        
        for(int i = 0; i < 6; ++i) {
            float currentMod = 1.0f;
            if(empire.tech().category(i).id().equals("TECH_COMPUTERS"))
                currentMod = computerMod;
            if(empire.tech().category(i).id().equals("TECH_CONSTRUCTION"))
                currentMod = constructionMod;
            if(empire.tech().category(i).id().equals("TECH_FORCE_FIELDS"))
                currentMod = forcefieldMod;
            if(empire.tech().category(i).id().equals("TECH_PLANETOLOGY"))
                currentMod = planetologyMod;
            if(empire.tech().category(i).id().equals("TECH_PROPULSION"))
                currentMod = propulsionMod;
            if(empire.tech().category(i).id().equals("TECH_WEAPONS"))
                currentMod = weaponMod;
            if(tech(empire.tech().category(i).currentTech()) != null)
                totalTechCost += tech(empire.tech().category(i).currentTech()).researchCost() * currentMod;
            else
                totalTechCost += empire.tech().category(i).baseResearchCost(Math.round(empire.tech().category(i).techLevel()));
        }
        
        float totalInverse = 0;
        
        for(int i = 0; i < 6; ++i) {
            float currentMod = 1.0f;
            if(empire.tech().category(i).id().equals("TECH_COMPUTERS"))
                currentMod = computerMod;
            if(empire.tech().category(i).id().equals("TECH_CONSTRUCTION"))
                currentMod = constructionMod;
            if(empire.tech().category(i).id().equals("TECH_FORCE_FIELDS"))
                currentMod = forcefieldMod;
            if(empire.tech().category(i).id().equals("TECH_PLANETOLOGY"))
                currentMod = planetologyMod;
            if(empire.tech().category(i).id().equals("TECH_PROPULSION"))
                currentMod = propulsionMod;
            if(empire.tech().category(i).id().equals("TECH_WEAPONS"))
                currentMod = weaponMod;
            if(tech(empire.tech().category(i).currentTech()) != null)
                totalInverse += totalTechCost / (tech(empire.tech().category(i).currentTech()).researchCost() * currentMod);
            else
                totalInverse += totalTechCost / empire.tech().category(i).baseResearchCost(Math.round(empire.tech().category(i).techLevel()));
        }
        
        for(int i = 0; i < 6; ++i) {
            float currentMod = 1.0f;
            if(empire.tech().category(i).id().equals("TECH_COMPUTERS"))
                currentMod = computerMod;
            if(empire.tech().category(i).id().equals("TECH_CONSTRUCTION"))
                currentMod = constructionMod;
            if(empire.tech().category(i).id().equals("TECH_FORCE_FIELDS"))
                currentMod = forcefieldMod;
            if(empire.tech().category(i).id().equals("TECH_PLANETOLOGY"))
                currentMod = planetologyMod;
            if(empire.tech().category(i).id().equals("TECH_PROPULSION"))
                currentMod = propulsionMod;
            if(empire.tech().category(i).id().equals("TECH_WEAPONS"))
                currentMod = weaponMod;
            if(tech(empire.tech().category(i).currentTech()) != null)
                empire.tech().category(i).allocationPct((totalTechCost / (tech(empire.tech().category(i).currentTech()).researchCost() * currentMod)) / totalInverse);
            else
                empire.tech().category(i).allocationPct((totalTechCost / empire.tech().category(i).baseResearchCost(Math.round(empire.tech().category(i).techLevel()))) / totalInverse);
        }
        
        if (empire.fleetCommanderAI().inExpansionMode()) {
            Tech currentPlanetology = empire.tech().tech(empire.tech().planetology().currentTech());
            Tech currentPropulsion = empire.tech().tech(empire.tech().propulsion().currentTech());
            boolean needPlanetology = false;
            boolean needPropulsion = false;
            if(currentPlanetology != null && currentPlanetology.isControlEnvironmentTech() && baseValue((TechControlEnvironment)currentPlanetology) > 1)
                needPlanetology = true;
            if(currentPropulsion != null && currentPropulsion.isFuelRangeTech() && baseValue((TechFuelRange)currentPropulsion) > 2)
                needPropulsion = true;
            empire.tech().computer().allocation(0);
            empire.tech().construction().allocation(0);
            empire.tech().forceField().allocation(0);
            empire.tech().planetology().allocation(0);
            empire.tech().propulsion().allocation(0);
            empire.tech().weapon().allocation(0);

            if(needPlanetology || needPropulsion)
            {
                if(needPlanetology && needPropulsion) {
                    empire.tech().planetology().allocation(30);
                    empire.tech().propulsion().allocation(30);
                } else if (needPlanetology) {
                    empire.tech().planetology().allocation(60);
                } else {
                    empire.tech().propulsion().allocation(60);
                }
            }
            else
            {
                //we don't directly benefit from range or planetology... we want to get a mix of construction, planetology and propulsion to get to extended-fuel-range-designs quicker
                empire.tech().construction().allocation(20);
                empire.tech().planetology().allocation(20);
                empire.tech().propulsion().allocation(20);
            }
        }
        else if(stealableTechs() > 0 && minimalTechForRush())
        {
            empire.tech().computer().adjustAllocation(stealableTechs()*5);
            empire.tech().construction().adjustAllocation(stealableTechs()*-1);
            empire.tech().forceField().adjustAllocation(stealableTechs()*-1);
            empire.tech().planetology().adjustAllocation(stealableTechs()*-1);
            empire.tech().propulsion().adjustAllocation(stealableTechs()*-1);
            empire.tech().weapon().adjustAllocation(stealableTechs()*-1);
        }
        
        int futureTechs = 0;
        for (int j=0; j<TechTree.NUM_CATEGORIES; j++) {
            if (empire.tech().category(j).studyingFutureTech()
                    || empire.tech().category(j).possibleTechs().isEmpty())
                futureTechs++;
        }
        //When we are researching future-techs, weapons and propulsion are much more valuable than the rest
        if(futureTechs == 6)
        {
            empire.tech().computer().adjustAllocation(-5);
            empire.tech().construction().adjustAllocation(-5);
            empire.tech().forceField().adjustAllocation(-5);
            empire.tech().planetology().adjustAllocation(-5);
            empire.tech().propulsion().adjustAllocation(+10);
            empire.tech().weapon().adjustAllocation(+10);
        }
        
        int totalAlloc = empire.tech().computer().allocation() + empire.tech().construction().allocation() 
        + empire.tech().forceField().allocation() + empire.tech().planetology().allocation() 
        + empire.tech().propulsion().allocation() + empire.tech().weapon().allocation();
        
        int roundingFix = 60 - totalAlloc;
        while(roundingFix < 0)
        {
            if(empire.tech().computer().allocation() > 0)
            {
                empire.tech().computer().adjustAllocation(-1);
                roundingFix++;
                if(roundingFix >= 0)
                    break;
            }
            if(empire.tech().construction().allocation() > 0)
            {
                empire.tech().construction().adjustAllocation(-1);
                roundingFix++;
                if(roundingFix >= 0)
                    break;
            }
            if(empire.tech().forceField().allocation() > 0)
            {
                empire.tech().forceField().adjustAllocation(-1);
                roundingFix++;
                if(roundingFix >= 0)
                    break;
            }
            if(empire.tech().planetology().allocation() > 0)
            {
                empire.tech().planetology().adjustAllocation(-1);
                roundingFix++;
                if(roundingFix >= 0)
                    break;
            }
            if(empire.tech().propulsion().allocation() > 0)
            {
                empire.tech().propulsion().adjustAllocation(-1);
                roundingFix++;
                if(roundingFix >= 0)
                    break;
            }
            if(empire.tech().weapon().allocation() > 0)
            {            empire.tech().weapon().adjustAllocation(-1);
                roundingFix++;
                if(roundingFix >= 0)
                    break;
            }
        }
        
        while(roundingFix > 0)
        {
            empire.tech().computer().adjustAllocation(1);
            roundingFix--;
            if(roundingFix <= 0)
                break;
            empire.tech().construction().adjustAllocation(1);
            roundingFix--;
            if(roundingFix <= 0)
                break;
            empire.tech().forceField().adjustAllocation(1);
            roundingFix--;
            if(roundingFix <= 0)
                break;
            empire.tech().planetology().adjustAllocation(1);
            roundingFix--;
            if(roundingFix <= 0)
                break;
            empire.tech().propulsion().adjustAllocation(1);
            roundingFix--;
            if(roundingFix <= 0)
                break;
            empire.tech().weapon().adjustAllocation(1);
            roundingFix--;
            if(roundingFix <= 0)
                break;
        }  
    }
    @Override
    public void setTechToResearch(TechCategory cat) {
        // invoked for AI after a tech is learned
        // also invoked for AI & Player when Research BC are allocated during nextTurn() and no
        // Tech has yet been chosen to research

        List<Tech> techs = cat.techsAvailableForResearch();

        // no more techs to research in this category
        if (techs.isEmpty())
            return;

        if (empire.isPlayerControlled() ) {
            Tech firstTech = techs.get(0);
            // we stop asking for user selection once we finished Future Tech 1
            if (firstTech.futureTechLevel() < 2) {
                session().addTurnNotification(new SelectTechNotification(cat));
                return;
            }
        }
        List<Tech> techsOnlyBest = new ArrayList<>();
        
        for(Tech t : techs)
        {
            int type = t.techType;
            Tech highestOfType = t;
            int highestLevel = 0;
            int highestQuintile = 0;
            float lowestResearchCostInQuintile = Float.MAX_VALUE;
            float bestScore = 0;
            for(Tech inner : techs)
            {
                if(inner.techType == type)
                {
                    if(t.cat.index() == WEAPON)
                    {
                        //special case for weapons, here we want the cheapest within the same quintile
                        float currentPrio = researchPriority(inner);
                        if(currentPrio > bestScore)
                        {
                            bestScore = currentPrio;
                            highestQuintile = inner.quintile();
                            highestOfType = inner;
                            lowestResearchCostInQuintile = inner.researchCost();
                        }
                        if(currentPrio < bestScore)
                            continue;
                        if(inner.quintile() > highestQuintile)
                        {
                            highestQuintile = inner.quintile();
                            highestOfType = inner;
                            bestScore = currentPrio;
                            lowestResearchCostInQuintile = inner.researchCost();
                        }
                        if(inner.quintile() < t.quintile())
                            continue;
                        if(inner.researchCost() < lowestResearchCostInQuintile)
                        {
                            lowestResearchCostInQuintile = inner.researchCost();
                            highestOfType = inner;
                            bestScore = currentPrio;
                            highestQuintile = inner.quintile();
                        }
                    }
                    else if(inner.level > highestLevel)
                    {
                        highestOfType = inner;
                        highestLevel = inner.level;
                    }
                }
            }
            if(!techsOnlyBest.contains(highestOfType))
                techsOnlyBest.add(highestOfType);
        }
        
        float bestScore = 0;
        float lowestCost = Float.MAX_VALUE;
        Tech cheapestTech = null;
        for(Tech current: techsOnlyBest)
        {
            float currentPrio = researchPriority(current);
            float currentCost = current.researchCost();
            if(currentPrio > bestScore)
            {
                bestScore = currentPrio;
                lowestCost = currentCost;
                cheapestTech = current;
                continue;
            }
            if(currentPrio < bestScore)
                continue;
            if(currentCost < lowestCost)
            {
                bestScore = currentPrio;
                lowestCost = currentCost;
                cheapestTech = current;
            }
        }
        
        // return highest priority
        cat.currentTech(cheapestTech);
        /*
        for(Tech t : techs)
        {
            System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" "+cat.id()+" option: "+t.name()+" "+researchPriority(t));
        }
        for(Tech t : techsOnlyBest)
        {
            System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" "+cat.id()+" option (only best): "+t.name()+" "+researchPriority(t)+" bestScore: "+bestScore);
        }
        System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" "+cat.id()+" picked: "+cat.currentTechName()+" "+researchPriority(cheapestTech));
        */
    }
    //
    //  RESEARCH VALUES for various types of tech
    //
    @Override
    public float researchPriority(Tech t) {
        float ownerFactor = 1.0f;
        for(EmpireView ev : empire.contacts())
        {
            if(!ev.inEconomicRange())
                continue;
            if(!galaxy().options().canTradeTechs(empire, ev.empireUncut()))
                continue;
            if(ev.internalSecurityAdj() > empire.spyInfiltrationAdj())
                continue;
            if(isImportant(t))
                continue;
            //If others, who we are not at war with, have it, we value it lower because in that case we can try and trade for it
            if(!empire.atWarWith(ev.empId()) && ev.diplomatAI().techsAvailableForRequest(empire).contains(t))
                ownerFactor /= 2;
            else if(ev.spies().possibleTechs().contains(t.id()) && ev.spies().isEspionage() && ev.spies().hasSpies())
                ownerFactor /= 2;
        }
        return researchValue(t) * ownerFactor;
    }
    @Override
    public float researchValue(Tech t) {
        //ail: for something that has 0 base-value, we also don't add random
        //System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" researchValue of "+t.name()+" t.warModeFactor(): "+t.warModeFactor()+" warMode(): "+warMode()+" is Weapon: "+(t.cat.index() == WEAPON)+" is obsolete: "+t.isObsolete(empire));
        if (t.isObsolete(empire))
            return 0;
        return t.baseValue(empire);
    }
    public float researchValueAllocation(Tech t) {
        //ail: for something that has 0 base-value, we also don't add random
        //System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" researchValue of "+t.name()+" t.warModeFactor(): "+t.warModeFactor()+" warMode(): "+warMode()+" is Weapon: "+(t.cat.index() == WEAPON)+" is obsolete: "+t.isObsolete(empire));
        if (t.isObsolete(empire))
            return 0;
        if(!empire.fleetCommanderAI().inExpansionMode() && (t.quintile() > 1 || t.baseValue(empire) < 3))
        {
            boolean needWeapon = false;
            boolean needWarp = false;
            if(empire.tech().topShipWeaponTech().quintile() < 2 && empire.tech().topBaseMissileTech().quintile() < 2 && empire.tech().topBaseScatterPackTech() == null)
                needWeapon = true;
            if(empire.tech().topEngineWarpTech().quintile() < 2)
                needWarp = true;
            float prelim = t.baseValue(empire);
            if(needWeapon && needWarp)
            {
                if(t.cat.index() != WEAPON && t.cat.index() != PROPULSION)
                   prelim = 0;
            }
            else if(needWeapon)
            {
                if(t.cat.index() != WEAPON)
                    prelim = 0;
            }
            else if(needWarp)
            {
                if(t.cat.index() != PROPULSION)
                    prelim = 0;
            }
            //certain exceptions get their old value back
            if((t.isType(BIOLOGICAL_WEAPON) || t.isType(ARMOR)) && t.quintile() < 3)
                prelim = t.baseValue(empire);
            return prelim;
        }
        return t.baseValue(empire);
    }
    @Override
    public float researchBCValue(Tech t) {
        if (t.isObsolete(empire))
            return 0;

        if (empire.generalAI().inWarMode())
            return warTradeBCValue(t);

        if (empire.fleetCommanderAI().inExpansionMode())
            return t.expansionModeFactor() * t.researchCost();

        return t.researchCost();
    }
    @Override
    public float warTradeValue(Tech t) {
        if (t.isObsolete(empire))
            return 0;
        return t.warModeFactor() * (researchValueBonus(t) + t.baseValue(empire));
    }
    @Override
    public float warTradeBCValue(Tech t) {
        return t.warModeFactor() * t.researchCost(); 
    }
    private float researchValueBonus(Tech t) {
        TechCategory cat = empire.tech().category(t.cat.index());
        // if we have not researched a tech in this quintile yet
        // and we are not researching a tech in this quintile,
        // then the perceived value is 10% of the tech level
        Tech currentTech = tech(cat.currentTech());
        if ((cat.maxKnownQuintile() < t.quintile())
        && (currentTech != null)
        && (currentTech.quintile() < t.quintile()))
            return t.level * NEW_QUINTILE_BONUS;
        else
            return 0;
    }
    @Override
    public float baseValue(TechArmor t) {
        return 3;
    }
    @Override
    public float baseValue(TechAtmosphereEnrichment t) {
        if (empire.tech().canTerraformHostile())
            return 0;
        return 3;
    }
    @Override
    public float baseValue(TechAutomatedRepair t) {
        return 3;
    }
    @Override
    public float baseValue(TechBattleComputer t) {
        return 3;
    }
    @Override
    public float baseValue(TechBattleSuit t) {
        return 3;
    }
    @Override
    public float baseValue(TechBeamFocus t) {
        return 2;
    }
    @Override
    public float baseValue(TechBiologicalAntidote t) {
        float bioWeapon = 0;
        for(EmpireView ev : empire.contacts())
            if(ev.spies().tech().biologicalAttackLevel() > bioWeapon)
                bioWeapon = ev.spies().tech().antidoteLevel();
        if(empire.tech().topBiologicalAntidoteTech() == null || bioWeapon >= empire.tech().topBiologicalAntidoteTech().attackReduction)
            return 3;
        else
            return 1;
    }
    @Override
    public float baseValue(TechBiologicalWeapon t) {
        float antiDote = 0;
        for(EmpireView ev : empire.contacts())
            if(ev.spies().tech().antidoteLevel() > antiDote)
                antiDote = ev.spies().tech().antidoteLevel();
        if(antiDote >= t.maxDamage)
            return 1;
        else
            return 3.0f / (antiDote + 1.0f);
    }
    @Override
    public float baseValue(TechBlackHole t) {
        return 3;
    }
    @Override
    public float baseValue(TechBombWeapon t) {
        return 3;
    }
    @Override
    public float baseValue(TechCloaking t) {
        return 3;
    }
    @Override
    public float baseValue(TechCloning t) {
        return 3;
    }
    @Override
    public float baseValue(TechCombatTransporter t) {
        return 3;
    }
    @Override
    public float baseValue(TechControlEnvironment t) {
        if (empire.ignoresPlanetEnvironment())
            return 0;
        float range = empire.shipRange();
        if(empire.shipDesignerAI().BestDesignToColonize() != null)
            range = empire.shipDesignerAI().BestDesignToColonize().range();
        List<StarSystem> possible = empire.uncolonizedPlanetsInRange(range);
        List<StarSystem> newPossible;
        if(range > empire.shipRange())
            newPossible = empire.uncolonizedPlanetsInExtendedShipRange(t.environment());
        else 
            newPossible = empire.uncolonizedPlanetsInShipRange(t.environment());
        int newPlanets = newPossible.size() - possible.size();
        if (newPlanets < 1)
            return 1;
        float val = 3;
        if(empire.fleetCommanderAI().inExpansionMode())
        {
            val += 1;
        }
        return val;
    }
    @Override
    public float baseValue(TechDeflectorShield t) {
        return 3;
    }
    @Override
    public float baseValue(TechDisplacement t) {
        return 1;
    }
    @Override
    public float baseValue(TechECMJammer t) {
        return 1;
    }
    @Override
    public float baseValue(TechEcoRestoration t) {
        return 3;
    }
    @Override
    public float baseValue(TechEngineWarp t) {
        return 3;
    }
    @Override
    public float baseValue(TechEnergyPulsar t) {
        return 1;
    }
    @Override
    public float baseValue(TechFuelRange t) {
        float val = 2;
        List<StarSystem> possible = empire.uncolonizedPlanetsInRange(empire.shipRange());
        List<StarSystem> newPossible = empire.uncolonizedPlanetsInRange((int)t.range());
        float newPlanets = newPossible.size() - possible.size();
        if (newPlanets > 0 && empire.fleetCommanderAI().inExpansionMode())
        {
            val += 1;
            if(possible.isEmpty())
                val += 1;
        }
        return val;
    }
    @Override
    public float baseValue(TechFutureComputer t) {
        return 1;
    }
    @Override
    public float baseValue(TechFutureConstruction t) {
        return 1;
    }
    @Override
    public float baseValue(TechFutureForceField t) {
        return 1;
    }
    @Override
    public float baseValue(TechFuturePlanetology t) {
        return 1;
    }
    @Override
    public float baseValue(TechFuturePropulsion t) {
        return 1;
    }
    @Override
    public float baseValue(TechFutureWeapon t) {
        return 1;
    }
    @Override
    public float baseValue(TechHandWeapon t) {
        return 3;
    }
    @Override
    public float baseValue(TechHyperspaceComm t) {
        return 4;
    }
    @Override
    public float baseValue(TechImprovedIndustrial t) {
        return 3;
    }
    @Override
    public float baseValue(TechImprovedTerraforming t) {
        return 3;
    }
    @Override
    public float baseValue(TechIndustrialWaste t) {
        if (empire.ignoresPlanetEnvironment())
            return 0;
        return 3;
    }
    @Override
    public float baseValue(TechMissileShield t) {
        return 1;
    }
    @Override
    public float baseValue(TechMissileWeapon t) {
    	float baseWeight = 1;
    	float shipWeight = 4;
    	float baseVal = baseWeight / options().selectedMissileBaseModifier();
    	float shipVal = shipWeight / options().selectedMissileShipModifier();
        float val = 2 * (baseVal + shipVal) / (baseWeight + shipWeight) ;
        if(empire.tech().topShipWeaponTech().quintile() < 2 && empire.tech().topBaseMissileTech().quintile() < 2 && empire.tech().topBaseScatterPackTech() == null)
            val += 1;
        return val;
    }
    @Override
    public float baseValue(TechPersonalShield t) {
        return 3;
    }
    @Override
    public float baseValue(TechPlanetaryShield t) {
        return 3;
    }
    @Override
    public float baseValue(TechRepulsor t) {
        return 4;
    }
    @Override
    public float baseValue(TechRoboticControls t) {
        return 4;
    }
    @Override
    public float baseValue(TechScanner t) {
        return 2; //more important than ECM for proper attack-fleet-sizing and avoiding having to retreat
    }
    @Override
    public float baseValue(TechShipInertial t) {
        return 2;
    }
    @Override
    public float baseValue(TechShipNullifier t) {
        if(t.isWarpDissipator())
            return 2;
        else
            return 1;
    }
    @Override
    public float baseValue(TechShipWeapon t) {
        float val = 3;
        if(empire.tech().topShipWeaponTech().quintile() < 2 && empire.tech().topBaseMissileTech().quintile() < 2 && empire.tech().topBaseScatterPackTech() == null)
            val += 1;
        if((t.range > 1 || t.heavyAllowed) && needRange() && !empire.tech().knowsTechOfType(CLOAKING))
            val += 1;
        //Gatling Lasers never worth it until you got nothing else available
        if(t.damageHigh() <= 4)
            val = 1;
        return val;
    }
    @Override
    public float baseValue(TechSoilEnrichment t) {
        if (empire.ignoresPlanetEnvironment())
            return 0;
        return 4;
    }
    @Override
    public float baseValue(TechStargate t) {
        return 1;
    }
    @Override
    public float baseValue(TechStasisField t) {
        return 3;
    }
    @Override
    public float baseValue(TechStreamProjector t) {
        return 3;
    }
    @Override
    public float baseValue(TechSubspaceInterdictor t) {
        boolean anyEnemiesHaveTeleporter = false;
        for (EmpireView v: empire.empireViews()) {
            if ((v != null) && v.embassy().anyWar()) {
                if (v.spies().tech().knowsTechOfType(Tech.TELEPORTER))
                        anyEnemiesHaveTeleporter = true;
                if (v.spies().tech().knowsTechOfType(Tech.COMBAT_TRANSPORTER))
                        anyEnemiesHaveTeleporter = true;
            }
        }
        if(!anyEnemiesHaveTeleporter)
            return 1;
        return 3;
    }
    @Override
    public float baseValue(TechTeleporter t) {
        boolean allEnemiesHaveInterdiction = true;
        for (EmpireView v: empire.empireViews()) {
            if ((v != null) && v.embassy().anyWar()) {
                if (v.spies().tech().knowsTechOfType(Tech.SUBSPACE_INTERDICTOR))
                        allEnemiesHaveInterdiction = false;
            }
        }
        if (allEnemiesHaveInterdiction)
            return 1;
        return 3;
    }
    @Override
    public float baseValue(TechTorpedoWeapon t) {
        return 1;
    }
    // Monster tech
    @Override public float baseValue(TechResistSpecial t)  { return 3; }
    @Override public float baseValue(TechAmoebaEatShips t) { return 3; }
    @Override public float baseValue(TechSquidInk t)       { return 3; }
    
    private float discoveryChanceOfCategoryIfAllocationWasZero(int category)
    {
        int allocationBefore = empire.tech().category(category).allocation();
        empire.tech().category(category).allocation(0);
        float chance = (empire.tech().category(category).upcomingDiscoveryChance() - 1) * 60;
        empire.tech().category(category).allocation(allocationBefore);
        return chance;
    }
    @Override
    public boolean isImportant(Tech t)
    {
        if(t.techType == Tech.ENGINE_WARP
            || t.techType == Tech.ROBOTIC_CONTROLS
            || t.techType == Tech.CLOAKING
            || t.techType == Tech.SOIL_ENRICHMENT)
            return true;
        return false;
    }
    @Override
    public boolean isOptional(Tech t)
    {
        if(t.techType == Tech.BEAM_FOCUS
            || t.techType == Tech.DISPLACEMENT
            || t.techType == Tech.ECM_JAMMER
            || t.techType == Tech.ENERGY_PULSAR
            || t.techType == Tech.MISSILE_SHIELD
            || t.techType == Tech.SHIP_NULLIFIER
            || t.techType == Tech.STARGATE
            || t.techType == Tech.TORPEDO_WEAPON)
            return true;
        return false;
    }
    public int stealableTechs()
    {
        int stealables = 0;
        for(EmpireView ev : empire.contacts())
        {
            if(ev.spies().hasSpies() && ev.spies().isEspionage())
            {
                for(Tech tech : ev.spies().unknownTechs())
                {
                    //System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" could steal "+tech.name()+" from "+ev.empire().name());
                    if(tech.isFutureTech())
                        continue;
                    if(tech.cat.index() == TechCategory.COMPUTER)
                        continue;
                    if(tech.isObsolete(empire))
                        continue;
                    //System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" want to steal "+tech.name()+" from "+ev.empire().name());
                    stealables++;
                }
            }
        }
        //System.out.print("\n"+galaxy().currentTurn()+" "+empire.name()+" stealables: "+stealables);
        return stealables;
    }
    public boolean needRange()
    {
        for(EmpireView ev : empire.contacts())
            for(ShipDesign enemyDesign : ev.designsUncut())
                if(enemyDesign.repulsorRange() > 0)
                    return true;
        return false;
    }
    @Override
    public boolean minimalTechForRush()
    {
        return (empire.tech().topShipWeaponTech().quintile() > 1 
                || empire.tech().topBaseMissileTech().quintile() > 1 
                || empire.tech().topBaseScatterPackTech() != null) 
                && empire.tech().topBaseSpeed() > 1;
    }
}
