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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import rotp.model.colony.MissileBase;
import rotp.model.empires.Empire;
import rotp.model.game.GameSession;
import rotp.model.game.IGameOptions;
import rotp.model.planet.PlanetType;
import rotp.ui.notifications.TradeTechNotification;
import rotp.util.Base;

public final class TechTree implements Base, Serializable {
    private static final long serialVersionUID = 1L;
    public static final int NUM_CATEGORIES = 6;

    private Empire empire;
    private TechCategory[] category;
    private MissileBase bestMissileBase;
    private boolean spy = false;
    private boolean canBuildStargate = false;
    private boolean hyperspaceCommunications = false;
	private boolean hasRepulsorTech;
	private boolean hasEnergyFocusTech;
	private boolean hasCloakingTech;
	private boolean hasBlackHoleTech;

    private String topArmorTech;
    private String topAtmoEnrichmentTech;
    private String topAutomatedRepairTech;
    private String topBattleComputerTech;
    private String topBattleSuitTech;
    private String topBiologicalAntidoteTech;
    private String topBiologicalWeaponTech;
    private String topBombWeaponTech;
    private String topCloningTech;
    private String topControlEnvironmentTech;
    private String topDeflectorShieldTech;
    private String topECMJammerTech;
    private String topEcoRestorationTech;
    private String topEnergyPulsarTech;
    private String topEngineWarpTech;
    private String topFuelRangeTech;
    private String topHandWeaponTech;
    private String topImprovedIndustrialTech;
    private String topTerraformingTech;
    private String topIndustrialWasteTech;
    private String topMissileShieldTech;
    private String topBaseMissileTech;
    private String topBaseScatterPackTech;
    private String topPersonalShieldTech;
    private String topPlanetaryShieldTech;
    private String topReserveFuelRangeTech;
    private String topRoboticControlsTech;
    private String topShipInertialTech;
    private String topSoilEnrichmentTech;
    private String topSubspaceInterdictorTech;
    private String topShipWeaponTech;

    private List<String> tradedTechs;
    private List<String> newTechs;
    private List<TradeTechNotification> tradedTechNotifs;
    private boolean[] colonizableHostility = new boolean[13];
    public transient float totalResearchThisTurn = 0;

    public Empire empire()                                            { return empire; }
    public TechCategory category(int i)                               { return category[i]; }
    public MissileBase bestMissileBase()                              { return bestMissileBase; }
    public boolean spy()                                              { return spy; }
    public void spy(boolean b)                                        { spy = b; }
    public boolean canBuildStargate()                                 { return canBuildStargate; }
    public void canBuildStargate(boolean b)                           { canBuildStargate = b; }
    public boolean hyperspaceCommunications()                         { return hyperspaceCommunications; }
    public void hyperspaceCommunications(boolean b)                   { hyperspaceCommunications = b; }
	public boolean hasRepulsorTech()								  { return hasRepulsorTech; }
	public void hasRepulsorTech(boolean b)							  { hasRepulsorTech = b; }
	public boolean hasEnergyFocusTech()								  { return hasEnergyFocusTech; }
	public void hasEnergyFocusTech(boolean b)						  { hasEnergyFocusTech = b; }
	public boolean hasCloakingTech()								  { return hasCloakingTech; }
	public void hasCloakingTech(boolean b)							  { hasCloakingTech = b; }
	public boolean hasBlackHoleTech()								  { return hasBlackHoleTech; }
	public void hasBlackHoleTech(boolean b)							  { hasBlackHoleTech = b; }

    public boolean researchCompleted() {
        for (TechCategory cat: category) {
            if (!cat.researchCompleted())
                return false;
        }
        return true;
    }
    public List<String> tradedTechs() {
        if (tradedTechs == null)
            tradedTechs = new ArrayList<>();
        return tradedTechs;
    }
    public List<String> newTechs() {
        if (newTechs == null)
            newTechs = new ArrayList<>();
        return newTechs;
    }
    public List<TradeTechNotification> tradedTechNotifs() {
        if (tradedTechNotifs == null)
            tradedTechNotifs = new ArrayList<>();
        return tradedTechNotifs;
    }
    public TechArmor topArmorTech()                                   { return (TechArmor) tech(topArmorTech); }
    public void topArmorTech(TechArmor t)                             { topArmorTech = t.id();	}
    public TechAtmosphereEnrichment topAtmoEnrichmentTech()           { return (TechAtmosphereEnrichment) tech(topAtmoEnrichmentTech);	}
    public void topAtmoEnrichmentTech(TechAtmosphereEnrichment t)     { topAtmoEnrichmentTech = t.id(); }
    public TechAutomatedRepair topAutomatedRepairTech()               { return (TechAutomatedRepair) tech(topAutomatedRepairTech); }
    public void topAutomatedRepairTech(TechAutomatedRepair t)         { topAutomatedRepairTech = t.id(); }
    public TechBattleComputer topBattleComputerTech()                 { return (TechBattleComputer) tech(topBattleComputerTech); }
    public void topBattleComputerTech(TechBattleComputer t)           { topBattleComputerTech = t.id(); }
    public TechBattleSuit topBattleSuitTech()                         { return (TechBattleSuit) tech(topBattleSuitTech);	}
    public void topBattleSuitTech(TechBattleSuit t)                   { topBattleSuitTech = t.id();  }
    public TechBiologicalAntidote topBiologicalAntidoteTech()         { return (TechBiologicalAntidote) tech(topBiologicalAntidoteTech);	}
    public void topBiologicalAntidoteTech(TechBiologicalAntidote t)   { topBiologicalAntidoteTech = t.id(); }
    public TechBiologicalWeapon topBiologicalWeaponTech()             { return (TechBiologicalWeapon) tech(topBiologicalWeaponTech); }
    public void topBiologicalWeaponTech(TechBiologicalWeapon t)       { topBiologicalWeaponTech = t.id(); }
    public TechBombWeapon topBombWeaponTech()                         { return (TechBombWeapon) tech(topBombWeaponTech); }
    public void topBombWeaponTech(TechBombWeapon t)                   { topBombWeaponTech = t.id(); }
    public TechCloning topCloningTech()                               { return (TechCloning) tech(topCloningTech); }
    public void topCloningTech(TechCloning t)                         { topCloningTech = t.id(); }
    public TechControlEnvironment topControlEnvironmentTech()         { return (TechControlEnvironment) tech(topControlEnvironmentTech);	}
    public void topControlEnvironmentTech(TechControlEnvironment t)   { topControlEnvironmentTech = t.id(); }
    public TechDeflectorShield topDeflectorShieldTech()               { return (TechDeflectorShield) tech(topDeflectorShieldTech); }
    public void topDeflectorShieldTech(TechDeflectorShield t)         { topDeflectorShieldTech = t.id(); }
    public TechECMJammer topECMJammerTech()                           { return (TechECMJammer) tech(topECMJammerTech); }
    public void topECMJammerTech(TechECMJammer t)                     { topECMJammerTech = t.id(); }
    public TechEcoRestoration topEcoRestorationTech()                 { return (TechEcoRestoration) tech(topEcoRestorationTech); }
    public void topEcoRestorationTech(TechEcoRestoration t)           { topEcoRestorationTech = t.id(); }
    public TechEnergyPulsar topEnergyPulsarTech()                     { return (TechEnergyPulsar) tech(topEnergyPulsarTech); }
    public void topEnergyPulsarTech(TechEnergyPulsar t)               { topEnergyPulsarTech = t.id(); }
	public TechEngineWarp topEngineWarpTech()						{
		if (topEngineWarpTech == null)
			topEngineWarpTech = Tech.buildId(TechEngineWarp.KEY, 0);
		return (TechEngineWarp) tech(topEngineWarpTech);
	}
    public void topEngineWarpTech(TechEngineWarp t)                   { topEngineWarpTech = t.id(); }
    public TechFuelRange topFuelRangeTech()                           { return (TechFuelRange) tech(topFuelRangeTech); }
    public void topFuelRangeTech(TechFuelRange t)                     { topFuelRangeTech = t.id(); }
    public TechHandWeapon topHandWeaponTech()                         { return (TechHandWeapon) tech(topHandWeaponTech); }
    public void topHandWeaponTech(TechHandWeapon t)                   { topHandWeaponTech = t.id(); }
    public TechImprovedIndustrial topImprovedIndustrialTech()         { return (TechImprovedIndustrial) tech(topImprovedIndustrialTech); }
    public void topImprovedIndustrialTech(TechImprovedIndustrial t)   { topImprovedIndustrialTech = t.id(); }
    public TechImprovedTerraforming topTerraformingTech()             { return (TechImprovedTerraforming) tech(topTerraformingTech); }
    public void topTerraformingTech(TechImprovedTerraforming t)       { topTerraformingTech = t.id(); }
    public TechIndustrialWaste topIndustrialWasteTech()               { return (TechIndustrialWaste) tech(topIndustrialWasteTech); }
    public void topIndustrialWasteTech(TechIndustrialWaste t)         { topIndustrialWasteTech = t.id(); }
    public TechMissileShield topMissileShieldTech()                   { return (TechMissileShield) tech(topMissileShieldTech); }
    public void topMissileShieldTech(TechMissileShield t)             { topMissileShieldTech = t.id(); }
    public TechMissileWeapon topBaseMissileTech()                     { return (TechMissileWeapon) tech(topBaseMissileTech); }
    public void topBaseMissileTech(TechMissileWeapon t)               { topBaseMissileTech = t.id(); }
    public TechMissileWeapon topBaseScatterPackTech()                 { return (TechMissileWeapon) tech(topBaseScatterPackTech); }
    public void topBaseScatterPackTech(TechMissileWeapon t)           { topBaseScatterPackTech = t.id(); }
    
    public TechShipWeapon topShipWeaponTech()                         { return (TechShipWeapon) tech(topShipWeaponTech); }
    public void topShipWeaponTech(TechShipWeapon t)                   { topShipWeaponTech = t.id(); }
    public TechPersonalShield topPersonalShieldTech()                 { return (TechPersonalShield) tech(topPersonalShieldTech); }
    public void topPersonalShieldTech(TechPersonalShield t)           { topPersonalShieldTech = t.id(); }
    public TechPlanetaryShield topPlanetaryShieldTech()               { return (TechPlanetaryShield) tech(topPlanetaryShieldTech); }
    public void topPlanetaryShieldTech(TechPlanetaryShield t)         { topPlanetaryShieldTech = t.id(); }
    public TechReserveFuelRange topReserveFuelRangeTech()             { return (TechReserveFuelRange) tech(topReserveFuelRangeTech); }
    public void topReserveFuelRangeTech(TechReserveFuelRange t)       { topReserveFuelRangeTech = t.id(); }
    public TechRoboticControls topRoboticControlsTech()               { return (TechRoboticControls) tech(topRoboticControlsTech); }
    public void topRoboticControlsTech(TechRoboticControls t)         { topRoboticControlsTech = t.id(); }
    public TechShipInertial topShipInertialTech()                     { return (TechShipInertial) tech(topShipInertialTech); }
    public void topShipInertialTech(TechShipInertial t)               { topShipInertialTech = t.id(); }
    public TechSoilEnrichment topSoilEnrichmentTech()                 { return (TechSoilEnrichment) tech(topSoilEnrichmentTech); }
    public void topSoilEnrichmentTech(TechSoilEnrichment t)           { topSoilEnrichmentTech = t.id(); }
    public TechSubspaceInterdictor topSubspaceInterdictorTech()       { return (TechSubspaceInterdictor) tech(topSubspaceInterdictorTech); }
    public void topSubspaceInterdictorTech(TechSubspaceInterdictor t) { topSubspaceInterdictorTech = t.id();	}

	public void validateOnLoad()	{
		for (String id : allKnownTechs()) {
			Tech tech = tech(id);
			int techType = tech.techType;
			if (techType == Tech.REPULSOR)
				hasRepulsorTech = true;
			else if (techType == Tech.BEAM_FOCUS)
				hasEnergyFocusTech = true;
			else if (techType == Tech.CLOAKING)
				hasCloakingTech = true;
			else if (techType == Tech.BLACK_HOLE)
				hasBlackHoleTech = true;
			else if (techType == Tech.STARGATE && empire.isPlayer())
				// In case of Empire swapping, as some AI remove this info.
				canBuildStargate = true;
		}
	}
    public boolean[] colonizableHostility() {
        if (colonizableHostility == null) {
            colonizableHostility = new boolean[13];
            colonizableHostility[0] = true;
            colonizableHostility[1] = true;
            colonizableHostility[2] = true;
            colonizableHostility[3] = true;
            colonizableHostility[4] = true;
            colonizableHostility[5] = true;
            colonizableHostility[6] = true;
            for (String tId: planetology().knownTechs()) {
                Tech t = tech(tId);
                if (t.isControlEnvironmentTech()) {
                    TechControlEnvironment t0 = (TechControlEnvironment) t;
                    int h = t0.hostilityAllowed();
                    if (h < colonizableHostility.length)
                        colonizableHostility[h] = true;
                }
            }
        }
        return colonizableHostility;
    }
    public void init(Empire c, boolean spyFlag) {
        spy = spyFlag;
        colonizableHostility[0] = true;
        colonizableHostility[1] = true;
        colonizableHostility[2] = true;
        colonizableHostility[3] = true;
        colonizableHostility[4] = true;
        colonizableHostility[5] = true;
        colonizableHostility[6] = true;

        empire = c;
        category = new TechCategory[TechTree.NUM_CATEGORIES];
        for (int i=0; i<TechTree.NUM_CATEGORIES; i++ ) {
        	category[i] = new TechCategory(i, this, empire.techDiscoveryPct(i));
        }

        for (TechCategory cat: category)
            cat.learnFreeTechs();

        updateMissileBase();
    }
    public void recalc(Empire c) {
        init(c, false);
    }
    public void updateMissileBase()      { bestMissileBase = newMissileBase(); }
    public void spyOnTechs(TechTree tree) { spyOnTechs(tree, 99); }

    public void spyOnTechs(TechTree tree, int maxLevel) {
        topArmorTech = tree.topArmorTech;
        topAtmoEnrichmentTech = tree.topAtmoEnrichmentTech;
        topAutomatedRepairTech = tree.topAutomatedRepairTech;
        topBattleComputerTech = tree.topBattleComputerTech;
        topBattleSuitTech = tree.topBattleSuitTech;
        topBiologicalAntidoteTech = tree.topBiologicalAntidoteTech;
        topBiologicalWeaponTech = tree.topBiologicalWeaponTech;
        topBombWeaponTech = tree.topBombWeaponTech;
        topCloningTech = tree.topCloningTech;
        topControlEnvironmentTech = tree.topControlEnvironmentTech;
        topDeflectorShieldTech = tree.topDeflectorShieldTech;
        topECMJammerTech = tree.topECMJammerTech;
        topEcoRestorationTech = tree.topEcoRestorationTech;
        topEnergyPulsarTech = tree.topEnergyPulsarTech;
        topEngineWarpTech = tree.topEngineWarpTech;
        topFuelRangeTech = tree.topFuelRangeTech;
        topHandWeaponTech = tree.topHandWeaponTech;
        topImprovedIndustrialTech = tree.topImprovedIndustrialTech;
        topTerraformingTech = tree.topTerraformingTech;
        topIndustrialWasteTech = tree.topIndustrialWasteTech;
        topMissileShieldTech = tree.topMissileShieldTech;
        topPersonalShieldTech = tree.topPersonalShieldTech;
        topPlanetaryShieldTech = tree.topPlanetaryShieldTech;
        topRoboticControlsTech = tree.topRoboticControlsTech;
        topShipInertialTech = tree.topShipInertialTech;
        topSoilEnrichmentTech = tree.topSoilEnrichmentTech;
        topSubspaceInterdictorTech = tree.topSubspaceInterdictorTech;
        topBaseMissileTech = tree.topBaseMissileTech;
        topBaseScatterPackTech = tree.topBaseScatterPackTech;
        topReserveFuelRangeTech = tree.topReserveFuelRangeTech;
        topShipWeaponTech = tree.topShipWeaponTech;

		hasRepulsorTech	 = tree.hasRepulsorTech;
		hasEnergyFocusTech = tree.hasEnergyFocusTech;
		hasCloakingTech	 = tree.hasCloakingTech;
		hasBlackHoleTech = tree.hasBlackHoleTech;

        newTechs().clear();
        newTechs().addAll(tree.newTechs());

        // called when updating spy information
        for (int i=0;i<NUM_CATEGORIES;i++)
            category[i].spyKnownTechs(tree.category[i]);
    }
    public TechCategory computer()     { return category[0]; }
    public TechCategory construction() { return category[1]; }
    public TechCategory forceField()   { return category[2]; }
    public TechCategory planetology()  { return category[3]; }
    public TechCategory propulsion()   { return category[4]; }
    public TechCategory weapon()       { return category[5]; }
    public void acquireTradedTechs() {
        if (empire().isPlayerControlled()) {
            for (TradeTechNotification notif: tradedTechNotifs()) {
                boolean newTech = learnTech(notif.techId);
                if (newTech)
                    GameSession.instance().addTurnNotification(notif);
            }
        }
        else {
            for (String techId: tradedTechs()) 
                learnTech(techId);
        }

        tradedTechNotifs().clear();
        tradedTechs().clear();
        newTechs().clear();
    }
    public boolean adjustTechAllocation(int index, int adj) {
        return adjustTechAllocation(index,adj,false);
    }
    public boolean adjustTechAllocation(int index, int adj, boolean ignoreLock) {
        TechCategory changedCat = category[index];
        
        // this method can be called internally when a category completes research
        // and it needs to be reallocated to 0.
        // completed cats are considered "locked" so we want to ignore this locked
        // check when reallocating a completed category
        if (!ignoreLock && changedCat.locked())
            return false;

        int MAX = TechCategory.MAX_ALLOCATION_TICKS;

        int newValue = changedCat.allocation()+adj;
        if ((newValue <0)
        || (newValue > MAX))
            return false;

        for (int i=category.length-1;i>=0;i--) {
            if ((i != index) & (!category[i].locked()) & (adj != 0)) {
                int adj2;
                TechCategory affectedCat = category[i];
                if (adj > 0)
                    adj2 = min(adj,affectedCat.allocation());
                else
                    adj2 = 0 - min(0-adj,MAX-affectedCat.allocation());

                if (adj2 != 0) {
                    changedCat.adjustAllocation(adj2);
                    affectedCat.adjustAllocation(-adj2);
                    adj -= adj2;
                }
            }
        }
        return true;
    }
    public void equalizeAllocations() {
        int freeAlloc = TechCategory.MAX_ALLOCATION_TICKS;
        int numLocks = 0;

        for (TechCategory cat: category) {
            if (cat.locked()) {
                freeAlloc -= cat.allocation();
                numLocks++;
            } else {
                cat.allocation(0);
            }
        }
        // if every category is locked, don't try to equalize
        if (category.length == numLocks)
            return;
        redistributeRemainingAlloc(freeAlloc);
    }
    public void redistributeRemainingAlloc(int freeAlloc) {
        while(freeAlloc > 0)
        {
            int unlockedCategories = 0;
            for (TechCategory cat: category) {
                if (!cat.locked()) {
                    cat.adjustAllocation(1);
                    freeAlloc--;
                    unlockedCategories++;
                }
                if(freeAlloc <= 0)
                    break;
            }
            if(unlockedCategories == 0) {
                for (TechCategory cat: category) {
                    if (!cat.researchCompleted()) {
                        cat.adjustAllocation(1);
                        freeAlloc--;
                        unlockedCategories++;
                    }
                    if(freeAlloc <= 0)
                        break;
                }
            }
            if(unlockedCategories == 0) //Neither unlocked categories nor categories that still can research something were found
                return;
        }
    }
    public boolean canColonize(PlanetType pt) {
        if (options().restrictedColonization())
            return knowsTechForHostility(pt.hostility());
        else
            return pt.hostility() <= topHostilityAllowed();
    }
    public boolean canColonize(PlanetType pt, int newHostilityLevel) {
        int ptHostility = pt.hostility();
        if (options().restrictedColonization())
            return (ptHostility == newHostilityLevel) || knowsTechForHostility(ptHostility);
        else
            return (ptHostility <= newHostilityLevel) || (ptHostility <= topHostilityAllowed());
    }
    public boolean isLearningToColonize(PlanetType pt) {
        int hostility = pt.hostility();
        if (options().restrictedColonization()) 
            return knowsTechForHostility(hostility) || (hostility == researchingHostilityAllowed());
        
        return (hostility <= topHostilityAllowed()) || (hostility <= researchingHostilityAllowed());
    }
    public boolean canLearnToColonize(PlanetType pt) {
        return learnableHostilityAllowed(pt.hostility(), options().restrictedColonization());
    }
    public float minColonyLevel() {
        return topControlEnvironmentTech == null ? PlanetType.HOSTILITY_MINIMAL : topControlEnvironmentTech().environment();
    }
    public boolean knowsTechForHostility(int h) {
        boolean[] hostility = colonizableHostility();   
        return (h < hostility.length) && hostility[h];
    }
    public void learnToColonizeHostility(int h) {
        boolean[] hostility = colonizableHostility();     
        if (h < hostility.length)
            hostility[h] = true;
    }
    private int topHostilityAllowed() {
        return topControlEnvironmentTech != null ? topControlEnvironmentTech().hostilityAllowed() : 0;
    }
    private int researchingHostilityAllowed() {
        String id = planetology().currentTech();
        if (id == null)
            return 0;
        
        Tech t = tech(id);
        if (t.isControlEnvironmentTech()) {
            TechControlEnvironment t0 = (TechControlEnvironment) t;
            return t0.hostilityAllowed();
        }
        return 0;
    }
    private boolean learnableHostilityAllowed(int h, boolean restricted) {
        String currId = planetology().currentTech();
        if (currId != null) {
            Tech t = tech(currId);
            if (t.isControlEnvironmentTech()) {
                TechControlEnvironment t0 = (TechControlEnvironment) t;
                if ((t0.hostilityAllowed() == h) 
                || (!restricted && t0.hostilityAllowed() >= h))
                    return true;
            }
        }
        
        for (String id: planetology().techIdsAvailableForResearch(true)) {
            Tech t = tech(id);
            if (t.isControlEnvironmentTech()) {
                TechControlEnvironment t0 = (TechControlEnvironment) t;
                if ((t0.hostilityAllowed() == h) 
                || (!restricted && t0.hostilityAllowed() >= h))
                    return true;
            }
        }
        return false;
    }
    public boolean canTerraformHostile() {
        return topAtmoEnrichmentTech() != null;
    }
    public float researchingShipRange() {
        float range = ((TechFuelRange) tech(topFuelRangeTech)).range();

        String id = propulsion().currentTech();
        if (id == null)
            return range;
        
        Tech t = tech(id);
        if (t.isFuelRangeTech()) {
            TechFuelRange t0 = (TechFuelRange) t;
            range = max(range, t0.range());
        }
        return range;
    }
    public float learnableShipRange() {
        float range = ((TechFuelRange) tech(topFuelRangeTech)).range();

        for (String id: propulsion().techIdsAvailableForResearch(true)) {
            Tech t = tech(id);
            if (t.isFuelRangeTech()) {
                TechFuelRange t0 = (TechFuelRange) t;
                range = max(range, t0.range());
            }
        }
        return range;
    }
    public float researchingScoutRange() {
        float rsv = topReserveFuelRangeTech().range();
        float range = ((TechFuelRange) tech(topFuelRangeTech)).range()+rsv;

        String id = propulsion().currentTech();
        if (id == null)
            return range;
        
        Tech t = tech(id);
        if (t.isFuelRangeTech()) {
            TechFuelRange t0 = (TechFuelRange) t;
            range = max(range, t0.range()+rsv);
        }
        return range;
    }
    public float learnableScoutRange() {
        float rsv = topReserveFuelRangeTech().range();
        float range = ((TechFuelRange) tech(topFuelRangeTech)).range()+rsv;

        for (String id: propulsion().techIdsAvailableForResearch(true)) {
            Tech t = tech(id);
            if (t.isFuelRangeTech()) {
                TechFuelRange t0 = (TechFuelRange) t;
                range = max(range, t0.range()+rsv);
            }
        }
        return range;
    }
    public String environmentTechNeededToColonize(int hostility) {
        boolean restricted = options().restrictedColonization();
        // returns the id of the currently unknown ecology tech we need 
        // to research in order to colonize planets of a certain hostitily level
        // if no tech is needed or it is impossible, return null
        int hostilityAllowed = topHostilityAllowed();

        if (!restricted && (hostilityAllowed >= hostility))
            return null;
        
        // check current research tech first
        String currentId = planetology().currentTech();
        if (currentId == null)
            return null;
                    
        Tech t = tech(currentId);
        if (t.isControlEnvironmentTech()) {
            TechControlEnvironment t0 = (TechControlEnvironment) t;
                if ((t0.hostilityAllowed() == hostility) 
                || (!restricted && t0.hostilityAllowed() >= hostility))
                    return currentId;
        }
        
        for (String id: planetology().techIdsAvailableForResearch(true)) {
            t = tech(id);
            if (t.isControlEnvironmentTech()) {
                TechControlEnvironment t0 = (TechControlEnvironment) t;
                if ((t0.hostilityAllowed() == hostility) 
                || (!restricted && t0.hostilityAllowed() >= hostility))
                    return id;            
            }
        }
        return null;        
    }
    public String rangeTechNeededToScoutDistance(float dist) {
        // returns the id of the currently unknown propulsion tech we need 
        // to research in order for scout ships to reach range dist
        // if no tech is needed or it is impossible, return null
        float rsv = topReserveFuelRangeTech().range();
        float range = ((TechFuelRange) tech(topFuelRangeTech)).range()+rsv;
        if (range >= dist)
            return null;
        
        // check current research tech first
        String currentId = propulsion().currentTech();
        if (currentId == null)
            return null;
               
        Tech t = tech(currentId);
        if (t.isFuelRangeTech()) {
            TechFuelRange t0 = (TechFuelRange) t;
            if (t0.range()+rsv >= dist)
                return currentId;
        }
         
        for (String id: propulsion().techIdsAvailableForResearch(true)) {
            t = tech(id);
            if (t.isFuelRangeTech()) {
                TechFuelRange t0 = (TechFuelRange) t;
                if (t0.range()+rsv >= dist)
                    return id;
            }
        }      
        return null;      
    }
    public String rangeTechNeededToReachDistance(float dist) {
        // returns the id of the currently unknown propulsion tech we need 
        // to research in order for normal ships to reach range dist
        // if no tech is needed or it is impossible, return null
        float range = ((TechFuelRange) tech(topFuelRangeTech)).range();
        if (range >= dist)
            return null;
        
        // check current research tech first
        String currentId = propulsion().currentTech();
        if (currentId == null)
            return null;
        
        Tech t = tech(currentId);
        if (t.isFuelRangeTech()) {
            TechFuelRange t0 = (TechFuelRange) t;
            if (t0.range() >= dist)
                return currentId;
        }
        
        for (String id: propulsion().techIdsAvailableForResearch(true)) {
            t = tech(id);
            if (t.isFuelRangeTech()) {
                TechFuelRange t0 = (TechFuelRange) t;
                if (t0.range() >= dist)
                    return id;
            }
        }      
        return null;      
    }
    public float maxTechLevel() {
        float lvl = 0;
        for (TechCategory cat: category)
            lvl = Math.max(lvl,cat.techLevel());
        return lvl;
    }
    public Float avgTechLevel() {
        float sum = 0;
        for (TechCategory cat: category)
            sum += cat.techLevel();
        return sum/category.length;
    }
    public void preNextTurn() {
        totalResearchThisTurn = empire().totalPlanetaryResearch();
    }
    public void allocateResearch() {
        for (TechCategory cat : category) {
            cat.allocateResearchBC();
        }
        // if all categories are completed, no need to check
        // for reallocation of completed categories
        if (researchCompleted())
            return;
        
        // check to see if any categories are completed but
        // still have spending (not reallocated yet, so do it now)
        int freeAlloc = 60;
        for (TechCategory cat: category) {
            if (cat.researchCompleted() && (cat.allocation() > 0))
                adjustTechAllocation(cat.index(), 0-cat.allocation(), true);
            freeAlloc -= cat.allocation();
        }
        redistributeRemainingAlloc(freeAlloc);
    }
    public String randomKnownTech() {
        return random(category).randomKnownTech();
    }
    // BR: modified for the "Never" tech
//    public Tech randomUnknownTech(int minLevel, int levelDiff, boolean isPlayer) {
//        return random(category).randomUnknownTech(minLevel, levelDiff, isPlayer);
//    }
    // BR: modified for the "Never" tech and repeatable tech
    public Tech randomUnknownTech(int minLevel, int levelDiff, boolean isPlayer, Long seed1, Long seed2) {
    	return random(category, seed1).randomUnknownTech(minLevel, levelDiff, isPlayer, seed2);
    }
    public void knowAll() { knowAll(99, 1); }
    public void knowAll(int maxLevel, float pct) {
        for (TechCategory cat: category)
            cat.knowAll(maxLevel, pct);
    }
    public void learnAll() {
        for (TechCategory cat: category)
            cat.learnAll();
    }
    public void allowResearch(String id) {
        category[tech(id).cat.index()].allowResearch(id);
    }
    public boolean knows(Tech t) {
        return t == null ? true : category[t.cat.index()].knownTechs().contains(t.id());
    }
    public boolean knows(String id) {
        for (TechCategory cat: category)
        	if (cat.knownTechs().contains(id))
                    return true;
        return false;
    }
    public boolean knowsTechOfType(int type) {
        for (TechCategory cat: category) {
            for (String id: cat.knownTechs()) {
                if (tech(id).isType(type))
                    return true;
            }
        }
        return false;
    }
    public List<String> allKnownTechs() {
        List<String> techs = new ArrayList<>();
        TechCategory[] cats = category;
        for (TechCategory cat: cats) 
            techs.addAll(cat.knownTechs());
        return techs;        
    }
    public List<Tech> allTechsOfType(int type) {
        List<Tech> techs = new ArrayList<>();
        TechCategory[] cats = category;
        for (TechCategory cat: cats) {
            for (String id: cat.allTechs()) {
                Tech t = tech(id);
                if (t.isType(type))
                    techs.add(t);
            }
        }
        return techs;
    }
    public boolean studying(String id) {
        return id == null ? false : category[tech(id).cat.index()].currentTech().equals(id);
    }
    public List<Tech> techsUnknownTo(Empire empire, boolean spying) { // council final war use this too
        List<Tech> result = new ArrayList<>();
        IGameOptions opts = options();
    	if (spying && opts.forbidTechStealing()) // No tech stealing = no tech plundering
    		return result;

        List<String> forbiddenTech = opts.forbiddenTechList(empire.isPlayer());
        for (TechCategory cat: category) {
        	List<String> catTech = new ArrayList<>(cat.knownTechs());
        	catTech.removeAll(forbiddenTech);
            for (String id: catTech) {
                Tech t = tech(id);
                // if empire doesn't know tech and hasn't trade for it
                if (!empire.tech().knows(t)
                && (!empire.tech().tradedTechs().contains(id)))
                    result.add(t);
            }
        }
        return result;
    }
    public List<String> worseTechsUnknownToCiv(TechTree tree, float maxLevel) {
        List<String> r = new ArrayList<>();

        for (TechCategory cat : category) {
            for (String id: cat.knownTechs()) {
                Tech t = tech(id);
                if (!t.isFutureTech() && (t.level <= maxLevel) && !tree.knows(t))
                    r.add(id);
            }
        }
        return r;
    }
    public List<Tech> betterTechsUnknownToCiv(TechTree otherTech, float maxLevel, boolean excludeObs) {
        List<Tech> r = new ArrayList<>();

        for (TechCategory cat : category) {
            for (String id: cat.knownTechs()) {
                    Tech t = tech(id);
                if (!t.isFutureTech() && !otherTech.knows(t)) {
                    if (excludeObs && t.isObsolete(otherTech.empire))
                        ;
                    else  if (t.level >= maxLevel)
                        r.add(t);
                }
            }
        }
        return r;
    }
    public void acquireTechThroughTrade(String techId, int empId) {
        //Tech t = tech(techId);
        tradedTechs().add(techId);
        if (empire().isPlayerControlled())
            tradedTechNotifs().add(TradeTechNotification.create(techId, empId));
    }
    public boolean learnTech(String id) {
        newTechs().add(id);
        return category[tech(id).cat.index()].learnTech(id);
    }
    public int popIncreaseCost() {
        return topTerraformingTech == null ? TechImprovedTerraforming.BASE_COST : topTerraformingTech().costPerMillion;
    }
    public float armorGroundBonus() {
        return topArmorTech == null ? 0 : topArmorTech().groundAttackBonus;
    }
    public float battleSuitGroundBonus() {
        return topBattleSuitTech == null ? 0 : topBattleSuitTech().groundCombatBonus;
    }
    public float shieldGroundBonus() {
        return topPersonalShieldTech == null ? 0 : topPersonalShieldTech().groundAttackBonus;
    }
    public float weaponGroundBonus() {
        return topHandWeaponTech == null ? 0 : topHandWeaponTech().combatMod;
    }
    public float troopCombatAdj(boolean defendFlag) {
        float adj = empire.groundAttackBonus() + armorGroundBonus() + battleSuitGroundBonus() + shieldGroundBonus() + weaponGroundBonus();
        if (defendFlag)
            adj += 5;
        return adj;
    }
    public float maxPlanetaryShieldLevel() {
               return topPlanetaryShieldTech == null ? 0 : topPlanetaryShieldTech().damage;
    }
    public float maxDeflectorShieldLevel() {
        return topDeflectorShieldTech == null ? 0 : topDeflectorShieldTech().damage;
    }
    public float terraformAdj() {
        return topTerraformingTech == null ? 0 : topTerraformingTech().increase();
    }
	public int topBaseSpeed()				{ return topEngineWarpTech().baseWarp(); }
	public float topSpeed()					{ return topEngineWarpTech().warp(); }
	public float transportTravelSpeed()		{ return topEngineWarpTech().transportTravelSpeed(); }
	public int transportCombatSpeed()		{ return topEngineWarpTech().transportCombatSpeed(); }
    public float shipDamageRepairPct() {
        return topAutomatedRepairTech == null ? 0 : topAutomatedRepairTech().repairAdj;
    }
    public float antidoteLevel() {
        return topBiologicalAntidoteTech == null ? 0 : topBiologicalAntidoteTech().attackReduction;
    }
    public float biologicalAttackLevel() {
        return topBiologicalWeaponTech == null ? 0 : topBiologicalWeaponTech().maxDamage;
    }
    public float bombAttackLevel() {
        return topBombWeaponTech == null ? 0 : topBombWeaponTech().damageHigh();
    }
    public float populationCost() {
        float popCost = topCloningTech == null ? TechCloning.BASE_POPULATION_COST : topCloningTech().growthCost;
        switch (options().selectedPopGrowthFactor()) {
            case "Reduced":
                popCost = popCost * 2;
        }
        return popCost;
    }
    public boolean enrichSoil() {
        return topSoilEnrichmentTech != null;
    }
    public boolean subspaceInterdiction() {
        return topSubspaceInterdictorTech != null;
    }
    public float wasteElimination() {
        return topEcoRestorationTech == null ? TechEcoRestoration.BASE_WASTE_ELIMINATED : topEcoRestorationTech().wasteEliminated;
    }
    public float shipRange() {
        return topFuelRangeTech == null ? 1 : topFuelRangeTech().range();
    }
    public float scoutRange() {
       return shipRange() + topReserveFuelRangeTech().range();
    }
    public float bestFactoryCost() {
        return topImprovedIndustrialTech == null ? TechImprovedIndustrial.BASE_FACTORY_COST : topImprovedIndustrialTech().factoryCost;
    }
    public float factoryWasteMod() {
        return topIndustrialWasteTech == null ? TechIndustrialWaste.BASE_FACTORY_WASTE_MOD : topIndustrialWasteTech().wasteModifier;
    }
    public float wasteCleanupTechMod() {
        return 4 * factoryWasteMod() / wasteElimination();
    }
    public float maxFactoryCost() {
        // ignoreFactoryRefit = always use RC2 has basis for factory cost
        int controls = empire.ignoresFactoryRefit() ?  TechRoboticControls.BASE_ROBOT_CONTROLS : topRobotControls();
        return newFactoryCost(controls);
    }
    public float newFactoryCost(int controls) {
        return bestFactoryCost() * controls / 2;
    }
    public int topRobotControls() { // BR: renamed from baseRobotControls... was misleading
        return topRoboticControlsTech == null ? TechRoboticControls.BASE_ROBOT_CONTROLS : topRoboticControlsTech().mark;
    }
    public MissileBase newMissileBase() {
        MissileBase base = new MissileBase();
        if (topArmorTech != null)
            base.armor(topArmorTech().baseArmor);
        if (topBaseMissileTech != null)
            base.missile(topBaseMissileTech().baseMissile);
        if (topBaseScatterPackTech != null)
            base.scatterPack(topBaseScatterPackTech().baseMissile);
        if (topDeflectorShieldTech != null)
            base.shield(topDeflectorShieldTech().baseShield);
        if (topBattleComputerTech != null)
            base.computer(topBattleComputerTech().baseComputer);
        if (topECMJammerTech != null)
            base.ecm(topECMJammerTech().baseECM);
        if (topMissileShieldTech != null)
            base.missileShield(topMissileShieldTech().baseMissileShield);
        return base;
    }
    public float newMissileBaseCost() {
        return newMissileBase().cost(empire);
    }
}
