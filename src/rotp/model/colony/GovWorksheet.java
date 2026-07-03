package rotp.model.colony;

import static rotp.model.colony.ColonySpendingCategory.MAX_TICKS;

import rotp.model.empires.Empire;
import rotp.model.game.GovernorOptions;
import rotp.model.game.IGovOptions;
import rotp.model.planet.Planet;
import rotp.model.tech.TechTree;

public final class GovWorksheet {
	private final GovernorOptions gov;
	private final Colony c;
	private final TechTree tech;
	private final Planet p;
	private final ColonyIndustry industry;
	private final ColonyEcology ecology;
	private final float planetProdAdj, factoryNetYield, workerBaseROI;
	private final float maxReserveIncome;
	private final float workerToFactoryROILimit;
	private final boolean wasShipRequest, hasSubsidies, useROILimit;
	final boolean promoteShips, keepDirectShipAlloc, shouldBuildGate, promoteBases;
	final float totalIncome, cleanupCost;
	final float targetPopPercent;

	//
	float maxSize;
	float targetPopMin, initialPop, targetPopPctToBuy;
	float minGrowth = 2.0f;

	float atmosphereCost, nextEnrichSoilCost, terraformCost;
	boolean promoteWorkers, promoteTerraform;

	public boolean canTerraformAtmosphere, canEnrichSoil, canTerraform, anyTerraform;
	public float atmosphereIncrease, enrichIncrease, terraformIncrease;

	public float minGrowth()	{
		return minGrowth;
	}
	public float targetPopMin()	{
		return initialPop + minGrowth;
	}
	public float targetPopPctToBuy()	{
		return targetPopPctToBuy;
	}

	GovWorksheet (Colony colony, boolean loweredShipPriority)	{
		ColonyShipyard shipyard	= colony.shipyard();
		Empire e	= colony.empire();
		c	= colony;
		p	= c.planet();
		gov	= c.govOptions();
		industry = c.industry();
		ecology	 = c.ecology();
		tech	 = e.tech();

		maxSize		= c.ultimateMaxSize();
		initialPop	= c.population();
		targetPopPercent	= 1.0f;
		float neutralGrowth	= p.normalPopGrowth(initialPop, null);
		boolean hasRequest	= c.prioritizeShips() || c.prioritizeResearch();
		if (gov.legacyGrowthMode() || !hasRequest) { // Force boost
			minGrowth		= maxSize - initialPop;
			targetPopMin	= maxSize;
		}
		else {
			float workable	= Math.min(maxSize, industry.factories() / industry.robotControls());
			float boostLim	= Math.max(workable, maxSize * gov.colonyEarlyBoostPct());
			if (gov.compensateGrowth())
				minGrowth	= Math.max(neutralGrowth, gov.minColonyGrowth());
			else
				minGrowth	= gov.minColonyGrowth();
			targetPopMin	= Math.min(maxSize, initialPop + minGrowth);
			targetPopMin	= Math.max(targetPopMin, boostLim);
		}
		targetPopPctToBuy	= targetPopMin/maxSize;

		cleanupCost		= c.minimumCleanupCost();
		planetProdAdj	= p.productionAdj();
		factoryNetYield	= c.factoryNetProductivity();
		workerBaseROI	= e.workerProductivity() / tech.populationCost();
		boolean buildingStargate	= c.allocation(Colony.SHIP) > 0 &&
				shipyard.design().equals(e.shipLab().stargateDesign()) &&
				!shipyard.stargateCompleted();
		boolean wasBuildingShips	= c.allocation(Colony.SHIP) > 0 && !buildingStargate;
		promoteShips	 = shipyard.buildLimit() > 0;
		shouldBuildGate	 = !promoteShips && gov.shouldBuildGate(c);		
		wasShipRequest	 = promoteShips || c.prioritizeShips();
		maxReserveIncome = c.maxReserveIncome();
		totalIncome		 = c.totalIncome();
		hasSubsidies	 = c.maxReserveIncome() > 0;
		promoteTerraform = c.ultimateMaxSize() > p.currentSize();
		workerToFactoryROILimit = gov.workerToFactoryROILimit();
		useROILimit	= e.numColonies() < gov.maxColoniesForROI();
		// Defense management
		// Set max missile bases if minimum is set
		ColonyDefense defense = c.defense();
		int minBase = gov.getMinimumMissileBases();
		int maxBase = defense.maxBases();
		if (minBase > 0 && maxBase < minBase)
			defense.maxBases(minBase);
		promoteBases = !defense.isCompleted() && gov.earlyBaseBuilding() && c.industry().completedPct() >= gov.earlyBaseBoostPct();

		// Ships management
		boolean isDirectShipAlloc	= !loweredShipPriority && wasBuildingShips && !wasShipRequest;
		keepDirectShipAlloc	= isDirectShipAlloc && gov.isShipbuilding();

		c.ecology().checkPlanetImprovement(this);
		promoteTerraform = promoteTerraform();
	}
	int[] govBuildSeq()	{
		if (promoteWorkers())
			return Colony.govBuildSeqW;
		else
			return Colony.govBuildSeq;
	}
	private float workerToFactoryROI()	{
		float industryBC = totalIncome * c.allocation(Colony.INDUSTRY)/MAX_TICKS;
		float factoryNetCost = industry.bestFactoryCost(industryBC) / planetProdAdj;
		float factoryNetROI	 = factoryNetYield / factoryNetCost;
		float workerToFactoryROI = workerBaseROI / factoryNetROI;
		//System.out.println("workerToFactoryROI: " + workerToFactoryROI);
		return workerToFactoryROI;
	}
	private boolean promoteWorkers()	{
		if (hasSubsidies) {
			switch (gov.subsidyNormalUse()) {
				case IGovOptions.INDUSTRY:
					promoteWorkers = false;
					return promoteWorkers;
				case IGovOptions.ECOLOGY:
					promoteWorkers = true;
					return promoteWorkers;
				case IGovOptions.PLANET_BASED:
					if (p.isResourceRich() || p.isResourceUltraRich()) {
						promoteWorkers = true;
						return promoteWorkers;
					}
					else if (p.isResourcePoor() || p.isResourceUltraPoor()) {
						promoteWorkers = false;
						return promoteWorkers;
					}
					break;
				case IGovOptions.GOV_CHOICE:
				default:
			}
		}
		promoteWorkers = workerToFactoryROI() > workerToFactoryROILimit;
		return promoteWorkers && useROILimit;
	}
	int updateLimitedAllocation(int alloc)	{
		int spare = (alloc * c.govShipBuildSparePct) / 100;
		int limitedAllocation = alloc - spare;
		return limitedAllocation;
	}
	int getRemainingAllocation() {
		int maxAllocation = MAX_TICKS;
		// determine how much categories are over/under spent
		int spendingTotal = 0;
		for (int i = 0; i < Colony.NUM_CATS; i++)
			spendingTotal += c.spending[i].allocation();
		int alloc = maxAllocation - spendingTotal;
		return alloc;
	}
	private boolean promoteTerraform()	{
		if (hasSubsidies) {
			switch (gov.subsidyTerraformUse()) {
				case IGovOptions.INDUSTRY:
					return false;
				case IGovOptions.ECOLOGY:
					return true;
				case IGovOptions.PLANET_BASED:
					if (p.isResourceRich() || p.isResourceUltraRich()) {
						return true;
					}
					else if (p.isResourcePoor() || p.isResourceUltraPoor()) {
						return false;
					}
					break;
				case IGovOptions.GOV_CHOICE:
				default:
			}
		}
		ecology.checkPlanetImprovement(this);
		if (!anyTerraform)
			return false;
		// True if minimum factories are already built
		float numFactories = industry.factories();
		int currentBuildableFactories = industry.currentBuildableFactories();
		float factoryCompletion = numFactories/currentBuildableFactories;
		if (factoryCompletion >= gov.terraformFactoryPct())
			return true;

		// True if minimum population is already there
		float nextTurnPop	= c.population() + ecology.upcomingPopGrowthFloat();
		float currentsize	= p.currentSize();;
		float PopulationPct	= nextTurnPop/currentsize;
		if (PopulationPct >= gov.terraformPopulationPct())
			return true;
		float missingPop = currentsize - nextTurnPop;
		if (missingPop <= gov.terraformMissingPopulation())
			return true;

		// True if one improvement is done in one turn.
		float costLimit = totalIncome - cleanupCost;
		costLimit *= gov.terraformCost2Income();
		if (p.isResourceUltraPoor()) // 3 turns for ultra poor
			costLimit *= 3;
		else if (p.isResourcePoor()) // 2 turns for poor
			costLimit *= 2;
		else if (maxReserveIncome > 0) // 2 turns if allocated funds
			costLimit *= 2;
		if (canTerraformAtmosphere && atmosphereCost <= costLimit)
			return true;
		if (canEnrichSoil && nextEnrichSoilCost <= costLimit)
			return true;
		if (canTerraform && terraformCost < costLimit)
			return true;
		if (canTerraform && tech.popIncreaseCost()*10 < costLimit)
			return true;
		return false;
	}
	boolean shouldBuildGate()			{ return shouldBuildGate; }
}
