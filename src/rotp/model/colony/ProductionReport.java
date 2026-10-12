package rotp.model.colony;

import rotp.model.game.GameSession;

public class ProductionReport	{ // Player only
	protected final Colony colony;
	protected final float currentSize, currentPop, inTransport;
	protected final float workerProductivity, reserveIncome;
	protected Integer nextTurnTransport, longTermTransport, incomingTransports;
	protected Float missingPop;
	protected float workingPopulation, workerProduction, production;
	protected int usedFactories;
	protected float totalProductionIncome, maxReserveIncome, totalIncome;

	ProductionReport(Colony col, float size, float pop, float inTr, float wProd, float rsvInc)	{
		colony = col;
		currentSize	= size;
		currentPop	= pop;
		inTransport	= inTr;
		reserveIncome	= rsvInc;
		workerProductivity	= wProd;
	}

	float missingPop()	{
		if (missingPop == null)
			missingPop = currentSize - currentPop + inTransport - incomingTransports();
		return missingPop;
	}
	int incomingTransports()	{
		if (incomingTransports == null)
			initTransports();
		return incomingTransports;
	}
	int longTermTransport()	{
		if (longTermTransport == null)
			initTransports();
		return longTermTransport;
	}
	int nextTurnTransport()	{
		if (nextTurnTransport == null)
			initTransports();
		return nextTurnTransport;
	}
	void initTransports()	{
		int[] newPopApproaching	= GameSession.instance().galaxy().newPopApproachingSystem(colony.starSystem());
		nextTurnTransport	= newPopApproaching[0];
		longTermTransport	= newPopApproaching[1];
		incomingTransports	= nextTurnTransport + longTermTransport;
		missingPop	= currentSize - currentPop + inTransport - incomingTransports;
	}
}

class OldProductionReport extends ProductionReport	{ // Player only

	OldProductionReport(Colony col, float size, float pop, float inTr, float wProd, float rsvInc)	{
		super(col, size, pop, inTr, wProd, rsvInc);
		workingPopulation	= currentPop - inTransport;	// also old way workingPop
		usedFactories		= colony.usedFactories(workingPopulation);
		workerProduction	= workingPopulation *  workerProductivity;
		production			= workerProduction + usedFactories;
		maxReserveIncome	= Math.min(production, reserveIncome);
		totalProductionIncome = colony.totalProductionIncome();
		totalIncome	= totalProductionIncome + maxReserveIncome;
	}
}

class NextProductionReport extends ProductionReport	{ // Player only
	private final OldProductionReport opr;

	NextProductionReport(Colony col, float size, float pop, float inTr, float wProd, float rsvInc)	{
		super(col, size, pop, inTr, wProd, rsvInc);
		opr = new OldProductionReport(col, size, pop, inTr, wProd, rsvInc);
		initTransports();

		workingPopulation	= nextTurnTransport + opr.workingPopulation;
		usedFactories		= colony.usedFactories(workingPopulation);
		workerProduction	= workingPopulation * workerProductivity;
		production			= workerProduction + usedFactories;
		maxReserveIncome	= Math.min(production, reserveIncome);
		float gross2Net		= colony.inRebellion() ? 1f : opr.totalProductionIncome / opr.production;
		totalProductionIncome = production * gross2Net;
		totalIncome	= totalProductionIncome + maxReserveIncome;
	}
}
