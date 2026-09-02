package rotp.model.empires;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import rotp.model.colony.Colony;
import rotp.model.colony.Colony.ColonyBudget;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GovernorOptions;
import rotp.util.Base;

public final class EmpireBudget extends ReinitBudget implements Base, Serializable {
	private static final long serialVersionUID = 1L;

	private static final int UPDATE	= 0; // From options
	public static final int CLEAR	= 1; // direct request
	public static final int LIST	= 2; // direct request
	public static final int REDO	= 3; // direct request
	public static final int COIN	= 4; // direct request
	// fund raising
	private float requestedReservesBC;
	private float expectedRevenueBC;
	private float totalReservesBC;
	private int minReserve;
	// subsidies
	private float unusedPlayerBC;
	private float unusedReserves;
	private float unusedGovernor;
	// common
	private BudgetStatus status = new BudgetStatus();
	private boolean checkClean = false;

	private final Empire empire;
	EmpireBudget(Empire emp)		{ empire = emp; }

	public void setChanges(int i)	{ status.set(i); }
	public int getChanges()			{ return status().get(); }
	private BudgetStatus status()	{
		if (status == null)
			status = new BudgetStatus();
		return status;
	}

	// Main
	public boolean budgetIfNeeded()	{
		if (updateInProgress)
			return false;
		if (status.isNewBudgetRequired()) {
			GovernorOptions govOptions = session().getGovernorOptions();
			boolean grant = govOptions.redoBudgetGrantAllowed();
			boolean raise = govOptions.redoBudgetRaiseAllowed();
			redoBudget(empire.allColonizedSystems(), grant, raise, REDO, true);
			return true;
		}
		else
			return false;
	}
	public void refreshBudget()		{ redoBudget(empire.allColonizedSystems(), false, false, UPDATE, false); }
	public void redoBudget(List<StarSystem> systems, boolean grant, boolean raise, int type, boolean forceGovern)	{
		syncBudget(systems, grant, raise, type, forceGovern);
		status.clear();
	}
	public synchronized void syncBudget(List<StarSystem> systems, boolean grant, boolean raise, int type, boolean forceGovern)	{
		updateInProgress = true;
		planTheBudget(systems, grant, raise, type, forceGovern);
		updateInProgress = false;
	}
	public void colonyBudget(StarSystem system)	{
			GovernorOptions govOptions = session().getGovernorOptions();
		boolean grant = govOptions.redoBudgetGrantAllowed();
		boolean raise = govOptions.redoBudgetRaiseAllowed();
		boolean fullRedo = grant && raise;
		// the colony will be fully updated, but the other will follow the rules
		if (status.isNewBudgetRequired()) {
			if (!fullRedo)
				// first fully update the colony
				empire.budget().syncBudget(Arrays.asList(system), true, true, COIN, true);
			// then update all the other
			empire.budget().syncBudget(empire.allColonizedSystems(), grant, raise, UPDATE, true);
			status.clear();
		}
		else if (govOptions().redoBudgetOnColony()) {
			makeBudgetColonyObsolete();
			if (!fullRedo)
				// first fully update the colony
				empire.budget().syncBudget(Arrays.asList(system), true, true, COIN, true);
			// then update all the other
			empire.budget().syncBudget(empire.allColonizedSystems(), grant, raise, UPDATE, true);
			status.clear();
		}
		else { // only update the colony
			empire.budget().syncBudget(Arrays.asList(system), true, true, EmpireBudget.COIN, true);
			// Do not change the status
		}
	}

	// common
	private void resetEmpireBudget(List<StarSystem> systems, boolean raise, boolean grant, boolean govern)	{
		minReserve		= session().getGovernorOptions().autospendMinReserve(empire.totalPlanetaryProduction());
		totalReservesBC	= empire.totalReserve();
		unusedReserves	= totalReservesBC;
		unusedPlayerBC	= totalReservesBC;
		unusedGovernor	= totalReservesBC;
	}
	private void budgetizeReserve(ColonyBudget budget, boolean fromPlayer, boolean grant)	{
		if (!grant) {
			if (fromPlayer)
				unusedReserves -= budget.budgetSubsidiesBC();
			else {
				if (budget.governorBudgetBC() != null)
					unusedGovernor -= budget.governorBudgetBC();
				if (budget.isPlayerBudget())
					return;
				unusedReserves -= budget.budgetSubsidiesBC();
			}
			return;
		}

		if (fromPlayer) {
			int request = budget.playerBudgetBC();
			float subsidies = unusedReserves >= request ? request : unusedReserves;
			subsidies = max(0, subsidies);
			unusedReserves -= subsidies;
			budget.budgetSubsidiesAndGovern(subsidies);
			//System.out.println("budgetizeReserve fromPlayer" + budget.toString());
			return;
		}

		// Governor alone
		int request = budget.reserveNeededBC();
		boolean noLimit = budget.noMinumumReserve();
		float available = noLimit? unusedGovernor : unusedGovernor - minReserve;
		float subsidies = available >= request ? request : available;
		subsidies = max(0, subsidies);
		unusedGovernor -= subsidies;
		budget.governorBudgetBC(ceil(subsidies));
		//System.out.println("budgetizeReserve Governor Alone " + budget.toString());

		// in the case the player set a value: no changes
		if (budget.isPlayerBudget())
			return;

		// final subsidies
		available = noLimit? unusedReserves : unusedReserves - minReserve;
		subsidies = available >= request ? request : available;
		subsidies = max(0, subsidies);
		unusedReserves -= subsidies;
		budget.budgetSubsidiesAndGovern(subsidies);
		//System.out.println("budgetizeReserve Governor Player " + budget.toString());
	}
	private void setUnusedPlayerReserves()	{ unusedPlayerBC = unusedReserves; }
	public float subsidized()				{ return empire.totalReserve() - unusedReserves; }
	public float unusedReserves()			{ return unusedReserves; }
	public float unusedPlayerReserves()		{ return unusedPlayerBC; }
	public void setRequestedReserves(float bc)	{ requestedReservesBC = bc; }
	public float requestedReserves()		{ return requestedReservesBC; }
	public void setExpectedRevenueBC(float bc)	{ expectedRevenueBC = bc; }
	private float excessRevenueBC()			{ return expectedRevenueBC - requestedReservesBC; }
	public boolean updateInProgress()		{ return updateInProgress; }
	public boolean isTaxLevelChanged()		{ return status.newTaxLevel(); }

	void makeTaxLevelObsolete()				{ status.taxLevelChanged();}	// Only direct call from mouse action
	public void makeTransortObsolete()		{ status.transportChanged();}
	public void makeBudgetOptionsObsolete()	{ status.optionsChanged();}		// Only direct call from UI
	public void makeBudgetColonyObsolete()	{ status.colonyChanged();}		// Only direct call from mouse action
	void makeEmpireChargesObsolete()		{ status.chargesChanged();}		// Dangerous! originate from everywhere

	public void transferBudget()	{
		boolean carryUnfunded = govOptions().autospendCarryUnfunded();
		for (StarSystem sys: empire.allColonizedSystems()) {
			if (sys == null)
				continue;
			Colony col = sys.colony();
			if (col == null || col.inRebellion())
				continue;
			col.budget().transfertBudget(carryUnfunded);
		}
	}
	private boolean checkForClean(String str)	{
		if (!checkClean)
			return true;
		int[] needCleaning	= player().needCleaning();
		int govLockCount	= needCleaning[Colony.GOV_LOCKED_DIRTY];
		int govUnlockCount	= needCleaning[Colony.GOV_UNLOCKED_DIRTY];
		int lockedCount		= needCleaning[Colony.LOCKED_DIRTY];
		int unlockedCount	= needCleaning[Colony.UNLOCKED_DIRTY];
		int dirtyCount		= govLockCount + govUnlockCount + lockedCount + unlockedCount;
		boolean clean = dirtyCount == 0;
		if (!clean)
			System.out.println(str + dirtyCount);
		return clean;
	}
	public synchronized void planTheBudget(List<StarSystem> systems, boolean grant, boolean raise, int type, boolean forceGovern)	{
		checkClean = true; // TODO BR: set checkClean = false;
		GovernorOptions govOptions = session().getGovernorOptions();
		boolean shieldWithoutBases = govOptions.getShieldWithoutBases();
		float maxIndustryRatio = govOptions.autospendMaxIndustryRatio();

		checkForClean("planTheBudget start unclean: ");

		resetEmpireBudget(systems, grant, raise, forceGovern);
		checkForClean("planTheBudget resetEmpireBudget unclean: ");

		boolean clearGrant = grant;
		boolean clearRaise = raise;
		boolean doGrant = grant;
		boolean doRaise = raise;
		switch (type) {
			case CLEAR:
				clearGrant = grant;
				clearRaise = raise;
				doGrant = false;
				doRaise = false;
				break;
			case UPDATE:
			case LIST:
			case REDO:
			case COIN:
			default:
		}
		for (StarSystem sys: systems) {
			if (sys == null)
				continue;
			Colony col = sys.colony();
			if (col != null)
				col.budget().budgetReset(shieldWithoutBases, maxIndustryRatio, clearGrant, clearRaise, forceGovern);
		}
		checkForClean("planTheBudget col.budget().budgetReset loop unclean: ");

		// go with the list
		splitList(systems);
		requestSubsidies(doGrant); // only filter the list if not grant.
		checkForClean("planTheBudget requestSubsidies(doGrant) unclean: ");

		// Update Empire reserve status
		mergeLists();
		refreshEmpireReserveStatus();

		// Plan Reserves
		if (doRaise)
			raiseFunds();
		checkForClean("planTheBudget requestSubsidies(raiseFunds) unclean: ");

		clearLists();
	}
	private void clearLists()	{
		playerBudgets.clear();
		governorBudgets.clear();
		otherBudgets.clear();
		remainingBudgets.clear();
	}
	private void mergeLists()	{
		remainingBudgets.clear();
		remainingBudgets.addAll(playerBudgets);
		remainingBudgets.addAll(governorBudgets);
		remainingBudgets.addAll(otherBudgets);
	}
	private void splitList(List<StarSystem> systems)	{
		clearLists();
		for (StarSystem sys: systems) {
			if (sys == null)
				continue;
			Colony col = sys.colony();
			if (col == null || col.inRebellion())
				continue;
			ColonyBudget budget = col.budget();
			if (budget.isPlayerBudget()) {
				playerBudgets.add(budget);
				if (col.isGovernor()) // could be both
					governorBudgets.add(budget);
			}
			else if (col.isGovernor())
				governorBudgets.add(budget);
			else
				otherBudgets.add(budget);
		}
	}
	private void requestSubsidies(boolean grant)	{
		requestSubsidies(playerBudgets, true, grant);
		setUnusedPlayerReserves();
		requestSubsidies(governorBudgets, false, grant);
	}
	private void requestSubsidies(List<ColonyBudget> budgets, boolean fromPlayer, boolean grant)	{
		GovernorOptions govOptions = session().getGovernorOptions();
		// first, help systems that are fighting plague or supernova research events
		boolean helpRandomEvent = govOptions.autospendFundHelpRandomEvent();
		boolean newColoniesFirst = govOptions.isAutospendOnNewColoniesFirst();
		boolean spendOnNewColonies = govOptions.isAutospendOnNewColonies();
		boolean spendToBoostArtefact = govOptions.isAutospendOnArtefacts();
		float autospendMaxIndustryRatio = govOptions.autospendMaxIndustryRatio();

		if (helpRandomEvent || fromPlayer)
			requestSubsidiesForRandomEvents(budgets, fromPlayer, grant);

		if (!fromPlayer)
			requestSubsidiesForTaggedColonies(budgets, grant);

		if (newColoniesFirst) {
			if (spendOnNewColonies)
				requestSubsidiesForNewColony(budgets, autospendMaxIndustryRatio, fromPlayer, grant);
			if (spendToBoostArtefact)
				requestSubsidiesForArtefactColony (budgets, fromPlayer, grant);
		}
		else {
			if (spendToBoostArtefact)
				requestSubsidiesForArtefactColony (budgets, fromPlayer, grant);
			if (spendOnNewColonies)
				requestSubsidiesForNewColony(budgets, autospendMaxIndustryRatio, fromPlayer, grant);
		}
		if (!fromPlayer)
			return;

		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			budgetizeReserve(budget, fromPlayer, grant);
		budgets.clear();
	}
	private void requestSubsidiesForRandomEvents(List<ColonyBudget> allBudgets, boolean fromPlayer, boolean grant)	{
		List<ColonyBudget> budgets = new ArrayList<>();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.hasProject()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			budgetizeReserve(budget, fromPlayer, grant);
	}
	private void requestSubsidiesForTaggedColonies(List<ColonyBudget> allBudgets, boolean grant)	{
		// Start with those allowed to pick in the reserve
		List<ColonyBudget> budgets = new ArrayList<>();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.noMinumumReserve()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			budgetizeReserve(budget, false, grant);
		// Then the others the reserve
		budgets.clear();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isGovernorBudget()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			budgetizeReserve(budget, false, grant);
	}
	private void requestSubsidiesForNewColony (List<ColonyBudget> allBudgets, float maxIndustryRatio, boolean fromPlayer, boolean grant)	{
		List<ColonyBudget> budgets = new ArrayList<>();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isNewColony(maxIndustryRatio)) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.INCREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			budgetizeReserve(budget, fromPlayer, grant);
	}
	private void requestSubsidiesForArtefactColony (List<ColonyBudget> allBudgets, boolean fromPlayer, boolean grant)	{
		List<ColonyBudget> budgets = new ArrayList<>();

		// Boost fully developed Orion Artefact colonies
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isOrionArtifact() && budget.colonyIsDeveloped()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.DECREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			budgetizeReserve(budget, fromPlayer, grant);

		// Boost fully developed Antaran Artefact colonies
		budgets.clear();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isAntaran() && budget.colonyIsDeveloped()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.DECREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			budgetizeReserve(budget, fromPlayer, grant);

		// Boost undeveloped Orion Artefact colonies
		budgets.clear();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isOrionArtifact()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.DECREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			budgetizeReserve(budget, fromPlayer, grant);

		// Boost undeveloped Antaran Artefact colonies
		budgets.clear();
		for (Iterator<ColonyBudget> iter = allBudgets.iterator(); iter.hasNext(); ) {
			ColonyBudget budget = iter.next();
			if (budget.isAntaran()) {
				budgets.add(budget);
				iter.remove();
			}
		}
		Collections.sort(budgets, Colony.DECREASING_BUDGET);
		for (ColonyBudget budget : budgets)
			budgetizeReserve(budget, fromPlayer, grant);
	}
	private void raiseFunds()	{
		if (empire.divertColonyExcessToResearch())
			return;
		float excessRevenueBC = excessRevenueBC();
		float allowedExcessRevenue = 10;
		boolean isReserveFromRich = govOptions().isReserveFromRich();

		if (excessRevenueBC > allowedExcessRevenue) {
			Collections.sort(remainingBudgets, INC_RESOURCES);
			// reset excess spending
			for (ColonyBudget cBudget : remainingBudgets)
				if (cBudget.colonyIsDeveloped() && !(isReserveFromRich && cBudget.isHighResource())) {
					excessRevenueBC -= cBudget.resetExess(2*excessRevenueBC, true, true);
					if (excessRevenueBC == 0)
						break;
					if (excessRevenueBC < 0) {
						// add one tick back
						excessRevenueBC += cBudget.tryToContribute(-2*excessRevenueBC, true);
						break;
					}
				}
		}
		checkForClean("planTheBudget requestSubsidies(raiseFunds) excessRevenueBC > allowedExcessRevenue unclean: ");

		if (excessRevenueBC < 0) {
			Collections.sort(remainingBudgets, DEC_RESOURCES);
			for (ColonyBudget cBudget : remainingBudgets)
				if (cBudget.isSubjectToTaxes()) {
					excessRevenueBC += cBudget.tryToContribute(-2*excessRevenueBC, true);
					if (excessRevenueBC >= 0)
						break;
				}
		}
		checkForClean("planTheBudget requestSubsidies(raiseFunds) excessRevenueBC < 0 unclean: ");
		float expectedRevenueBC = excessRevenueBC + requestedReserves();
		setExpectedRevenueBC(expectedRevenueBC);
	}
	private void refreshEmpireReserveStatus() {
		GovernorOptions govOptions = session().getGovernorOptions();
		float totalProd = empire.totalPlanetaryProduction();

		// already in reserves
		float unusedReserves	= unusedReserves();
		float reserveMax		= govOptions.autospendReserveMax();
		float reserveMaxRatio	= govOptions.autospendReserveMaxRatio();
		float reserveMaxBC		= max(reserveMax, totalProd * reserveMaxRatio);
		float maxToCollect		= reserveMaxBC - unusedReserves;

		// turn by turn
		float reserveNextTurn	= govOptions.autospendReserveNextTurn();
		float reserveNextTurnR	= govOptions.autospendReserveNextTurnRatio();
		float reserveNextTurnBC	= max(reserveNextTurn, totalProd * reserveNextTurnR);

		// then
		float requestedReserves	= min(maxToCollect, reserveNextTurnBC);
		setRequestedReserves(requestedReserves);

		// Check if budget update is needed
		float allocatedBC	= empire.empireExcessSpendingIncome();

		float taxedBC	= 0;
		for (ColonyBudget colBudget : remainingBudgets)
			taxedBC += colBudget.budgetTaxedBC();
		setExpectedRevenueBC(taxedBC + allocatedBC);
	}
	private static final Comparator<ColonyBudget> INC_RESOURCES = (ColonyBudget b1, ColonyBudget b2) -> Integer.compare(b1.resourcesSort(), b2.resourcesSort());
	private static final Comparator<ColonyBudget> DEC_RESOURCES = (ColonyBudget b1, ColonyBudget b2) -> Integer.compare(b2.resourcesSort(), b1.resourcesSort());

	private final class BudgetStatus implements Serializable {
		private static final long serialVersionUID = 1L;

		private static final int TAX_LEVEL	= 1;	// Tax level changed
		private static final int TRANSPORT	= 2;	// Transport sent or cleared
		private static final int OPTIONS	= 4;	// Budget related option changed
		private static final int COLONIES	= 8;	// Some colony consign changed
		private static final int CHARGES	= 16;	// General Empire spending changed.

		private int changes = 0;

		private void set(int i)	{ changes = i; }
		private int get()		{ return changes; }

//		private boolean anyChange()		{ return changes == 0; }
		private boolean isNew(int id)	{ return (changes & id) != 0; }
		private boolean newTaxLevel()	{ return isNew(TAX_LEVEL); }
		private boolean newTransport()	{ return isNew(TRANSPORT); }
		private boolean newOptions()	{ return isNew(OPTIONS); }	
		private boolean newColony()		{ return isNew(COLONIES); }
		private boolean newCharges()	{ return isNew(CHARGES); }

		private void clear()			{ changes = 0; }
		private void taxLevelChanged()	{ changes |= TAX_LEVEL; }	// Only direct call from mouse action
		private void transportChanged()	{ changes |= TRANSPORT; }
		private void optionsChanged()	{ changes |= OPTIONS; }		// Only direct call from UI
		private void colonyChanged()	{ changes |= COLONIES; }	// Only direct call from mouse action
		private void chargesChanged()	{ changes |= CHARGES; } 	// Dangerous! originate from everywhere

		private boolean isNewBudgetRequired()	{
			GovernorOptions govOptions = session().getGovernorOptions();
			if (govOptions.redoBudgetAllowed())
				return (newOptions()		&& govOptions.redoBudgetOnOptionChange())
						|| (newTaxLevel()	&& govOptions.redoBudgetOnTaxChange())
						|| (newTransport()	&& govOptions.redoBudgetOnTransport())
						|| (newColony()		&& govOptions.redoBudgetOnColony())
						|| (newCharges()	&& govOptions.redoBudgetOnSpendings());
			return false;
		}
	}
}
class ReinitBudget {
	// So they will be initialized on reload.
	transient List<ColonyBudget> playerBudgets		= new ArrayList<>();
	transient List<ColonyBudget> governorBudgets	= new ArrayList<>();
	transient List<ColonyBudget> otherBudgets		= new ArrayList<>();
	transient List<ColonyBudget> remainingBudgets	= new ArrayList<>();
	transient boolean updateInProgress = false;
	ReinitBudget()	{}	// Required for superclass de-serialization
}

