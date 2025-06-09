package rotp.model.game;

import static rotp.model.game.IMapOptions.divertExcessToResearch;

import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.io.Serializable;

import rotp.model.colony.Colony;
import rotp.model.planet.Planet;
import rotp.ui.options.AllSubUI;
import rotp.ui.util.AbstractParam;
import rotp.ui.util.IParam;

/**
 * Governor options.
 */
public class GovernorOptions implements Serializable, IGovOptions {
	private static final long serialVersionUID = 1l;
	private static boolean callForRefresh	= false;
	private static boolean callForReset		= false;

	public enum GatesGovernor {
		None,
		Rich,
		All
	}
	// The old options are kept Active for compatibility
	// The new dynamic options are needed for multiple access
	// Remnant.cfg options will be read once, the ignored.
	// keep backwards compatibility with system properties
//	private boolean governorOnByDefault = UserPreferences.governorOnByDefault();
//	private boolean legacyGrowthMode = UserPreferences.legacyGrowth(); // BR: moved to remnant.cfg
//	private boolean autotransport = "true".equalsIgnoreCase(System.getProperty("autotransport", "false"));
//	private boolean autotransportXilmi = "true".equalsIgnoreCase(System.getProperty("autotransportXilmi", "false"));
//	private boolean autotransportUngoverned = "true".equalsIgnoreCase(System.getProperty("autotransportUngoverned", "false"));
//	private GatesGovernor gates = "false".equalsIgnoreCase(System.getProperty("autogate", "true")) ? GatesGovernor.None : GatesGovernor.Rich;
//
//	// 1.5x for destinations inside nebulae
//	private int transportMaxTurns = 5;
//	private boolean transportRichDisabled = true;
//	private boolean transportPoorDouble = true;
//
//	private int minimumMissileBases = 0;
//	private boolean shieldWithoutBases = false;
//	private boolean autospend = UserPreferences.governorAutoSpendByDefault();
//	private boolean autoApply = UserPreferences.S();
//	private boolean autoInfiltrate = "true".equalsIgnoreCase(System.getProperty("autoInfiltrate", "true"));
//	private boolean autoSpy = "true".equalsIgnoreCase(System.getProperty("autoSpy", "false"));
//	private int reserve = 1000;
// 
//	private boolean shipbuilding = true;
//
//	// if true, automatically scout new planets
//	private boolean autoScout = true;
//	// if true, automatically colonize new planets
//	private boolean autoColonize = true;
//	// if true, send ships to enemy colonies
//	private boolean autoAttack = false;
//	// How many ships should Auto* missions send?
//	private int autoScoutShipCount  = 1;
//	private int autoColonyShipCount = 1;
//	private int autoAttackShipCount = 1;
	private boolean governorOnByDefault		= isGovernorOnByDefault();
	private boolean legacyGrowthMode		= legacyGrowthMode();
	private boolean autotransport			= isAutotransportFull();
	private boolean autotransportXilmi		= isAutotransportAI();
	private boolean autotransportUngoverned	= isAutotransportUngoverned();
	private GatesGovernor gates				= getGates();

	// 1.5x for destinations inside nebulae
	private int 	transportMaxTurns		= getTransportMaxTurns();
	private boolean transportRichDisabled	= isTransportRichDisabled();
	private boolean transportPoorDouble		= isTransportPoorDouble();

	private int		minimumMissileBases		= getMinimumMissileBases();
	private boolean shieldWithoutBases		= getShieldWithoutBases();
	private boolean autospend				= isAutospend();
	private boolean autoApply				= isAutoApply();
	private boolean autoInfiltrate			= isAutoInfiltrate();
	private boolean autoSpy					= isAutoSpy();
	private int		reserve					= getReserve();
 
	private boolean shipbuilding			= isShipbuilding();
	private boolean colonyRequests			= isFollowingColonyRequests();

	// if true, automatically scout new planets
	private boolean autoScout				= isAutoScout();
	// if true, automatically colonize new planets
	private boolean autoColonize			= isAutoColonize();
	// if true, send ships to enemy colonies
	private boolean autoAttack				= isAutoAttack();
	// How many ships should Auto* missions send?
	private int		autoScoutShipCount		= getAutoScoutShipCount();
	private int		autoColonyShipCount		= getAutoColonyShipCount();
	private int		autoAttackShipCount		= getAutoAttackShipCount();
	// if true, new colonies will have auto ship building set to "on"
	// Converted use: true = not yet transfered.
	// The autoShipsByDefault original function will be implemented
	// Using the news parameters
	private boolean autoShipsByDefault = true;
	
	// ========== Constructor And Initializers ==========
	public GovernorOptions() {
		//System.out.println("GovernorOptions() " + autoShipsByDefault);
		for (IParam param : AllSubUI.governorSubUI().optionsList()) {
//			System.out.println("is duplicate? = " + param.isDuplicate() + " - " + param.isCfgFile()
//			+ " - " + param.getCfgLabel());
			((AbstractParam <?>) param).isGovernor(GOV_REFRESH);
		}

		auto_Apply.isGovernor(GOV_RESET);
		customSize.isGovernor(GOV_RESET);
		animatedImage.isGovernor(GOV_RESET);
		brightnessPct.isGovernor(GOV_RESET);
		originalPanel.isGovernor(GOV_RESET);
		sizeFactorPct.isGovernor(GOV_RESET);
		verticalPosition.isGovernor(GOV_RESET);
		horizontalPosition.isGovernor(GOV_RESET);
		
	}
	public void gameStarted() { autoShipsByDefault = false; }
	public void gameLoaded()  {
		// System.out.println("autoShipsByDefault = " + autoShipsByDefault);
		if (autoShipsByDefault) {
			autoTransportAI.silentSet(autotransportXilmi);
			autotransportFull.silentSet(autotransport);
			autotransportAll.silentSet(autotransportUngoverned);
			transportNoRich.silentSet(transportRichDisabled);
			transportPoorX2.silentSet(transportPoorDouble);
			transportMaxDist.silentSet(transportMaxTurns);
			starGateOption.silentSet(gates.name());
			missileBasesMin.silentSet(minimumMissileBases);
			shieldAlones.silentSet(shieldWithoutBases);
			autoSpend.silentSet(autospend);
			reserveForSlow.silentSet(reserve);
			shipBuilding.silentSet(shipbuilding);
			followColonyRequests.silentSet(colonyRequests);
			maxGrowthMode.silentSet(legacyGrowthMode);
			auto_Infiltrate.silentSet(autoInfiltrate);
			auto_Spy.silentSet(autoSpy);
			auto_Scout.silentSet(autoScout);
			autoScoutCount.silentSet(autoScoutShipCount);
			govAutoColonize.silentSet(autoColonize);
			autoColonyCount.silentSet(autoColonyShipCount);
			auto_Attack.silentSet(autoAttack);
			autoAttackCount.silentSet(autoAttackShipCount);
			auto_Apply.silentSet(autoApply);
			governorByDefault.silentSet(governorOnByDefault);
		}
		autoShipsByDefault = false;
		for (IParam param: AllSubUI.governorSubUI().optionsList()) {
			param.updateOptionTool();
		}
		// Converted use of autoShipsByDefault: true = not yet transfered.
		// The autoShipsByDefault original function will be implemented using the new parameters
		// if true, new colonies will have auto ship building set to "on"
	}
	public static void callForReset()	{ callForReset	= true; }
	public static void callForRefresh(int call)	{
		//System.out.println("callForRefresh(int call): " + call);
		callForRefresh	= callForRefresh || (call == GOV_REFRESH);
		callForReset	= callForReset   || (call == GOV_RESET);
	}
	public void		clearRefresh()		{ callForRefresh = false; }
	public void		clearReset()		{ clearRefresh(); callForReset = false; }
	public boolean	refreshRequested()	{ return callForRefresh; }
	public boolean	resetRequested()	{ return callForReset; }
	
	public boolean	isOriginalPanel()				{ return originalPanel.get(); }
	public void		setIsOriginalPanel(boolean b)	{ originalPanel.set(b); }

	public boolean	isCustomSize()					{ return customSize.get(); }
	public void		setIsCustomSize(boolean b)		{ customSize.set(b); }

	public boolean	isAnimatedImage()				{ return animatedImage.get(); }
	public void		setIsAnimatedImage(boolean b)	{ animatedImage.silentSet(b); }
	public boolean	toggleAnimatedImage()	 		{
		animatedImage.toggle();
		return animatedImage.get();
	}

	public int		getBrightnessPct()				{ return brightnessPct.get(); }
	public void		setBrightnessPct(int i)			{ brightnessPct.set(i); }

	public int		getSizeFactorPct()				{ return sizeFactorPct.get(); }
	public void		setSizeFactorPct(int i)			{ sizeFactorPct.set(i); }

//	public int  getPositionX()						{ return horizontalPosition.get(); }
//	public void setPositionX(int i)					{ horizontalPosition.silentSet(i); }
//
//	public int  getPositionY()						{ return verticalPosition.get(); }
//	public void setPositionY(int i)					{ verticalPosition.silentSet(i); }

	public Point getPosition()						{
		Point pt = new Point();
			pt.x = horizontalPosition.get();
			pt.y = verticalPosition.get();
		return pt;
	}
	public void setPosition(Point pt)				{
		horizontalPosition.silentSet(pt.x);
		verticalPosition.silentSet(pt.y);
	}

	public boolean	isAutoApply()					{ return auto_Apply.get(); }
	public void		setAutoApply(boolean b)			{ auto_Apply.silentSet(b); }

	public boolean isGovernorOnByDefault()			{ return governorByDefault.get(); }
	public void setGovernorOnByDefault(boolean b)	{ governorByDefault.silentSet(b); }

	public boolean	isAutotransportAI()				{ return autoTransportAI.get(); }
	public void		setAutotransportAI(boolean b)	{ autoTransportAI.silentSet(b); }

	public boolean	isAutotransportFull()			{ return autotransportFull.get(); }
	public void		setAutotransportFull(boolean b)	{ autotransportFull.silentSet(b); }

	public boolean	isAutotransportUngoverned()		{ return autotransportAll.get(); }
	public void	setAutotransportUngoverned(boolean b)	{ autotransportAll.silentSet(b); }

	public boolean	isTransportRichDisabled()		{ return transportNoRich.get(); }
	public void	setTransportRichDisabled(boolean b) { transportNoRich.silentSet(b); }

	public boolean	isTransportPoorDouble()			{ return transportPoorX2.get(); }
	public void	setTransportPoorDouble(boolean b)	{ transportPoorX2.silentSet(b); }

	public int		getTransportMaxTurns()			{ return transportMaxDist.get(); }
	public void		setTransportMaxTurns(int i)		{ transportMaxDist.silentSet(i); }

	public GatesGovernor getGates()					{
		String gate = starGateOption.get();
		for (GatesGovernor value: GatesGovernor.values())
			if (gate.equalsIgnoreCase(value.name()))
				return value;
		return GatesGovernor.Rich; // Default Value
	}
	public void		setGates(GatesGovernor gates)	{ starGateOption.silentSet(gates.name()); }
	public boolean	shouldBuildGate(Colony col)		{
		if (!col.shipyard().canBuildStargate())
			return false;
		switch (getGates()) {
			case All:	return true;
			case None:	return false;
			case Rich:
				Planet p = col.planet();
				return p.isResourceRich() || p.isResourceUltraRich();
		}
		return false;
	}

	/** Develop colonies as quickly as possible */
	public boolean	legacyGrowthMode()				{ return maxGrowthMode.get(); }
	public void		setLegacyGrowthMode(boolean b)	{ maxGrowthMode.silentSet(b); }

	public int		terraformEarly()				{ return terraformEarly.get(); }
	public void		setTerraformEarly(int pct)		{ terraformEarly.silentSet(pct); }

	public int		getMinimumMissileBases()		{ return missileBasesMin.get(); }
	public void		setMinimumMissileBases(int i)	{ missileBasesMin.silentSet(i); }

	public boolean	getShieldWithoutBases()			{ return shieldAlones.get(); }
	public void		setShieldWithoutBases(boolean b){ shieldAlones.silentSet(b); }

	public boolean	isAutospend()					{ return autoSpend.get(); }
	public void		setAutospend(boolean b)			{ autoSpend.silentSet(b); }

	public int		getReserve()					{ return reserveForSlow.get(); }
	public void		setReserve(int i)				{ reserveForSlow.silentSet(i); }

	public boolean	isReserveFromRich()				{ return reserveFromRich.get(); }
	public void		setReserveFromRich(boolean b)	{ reserveFromRich.silentSet(b); }

	public boolean	isExcessToResearch()			{ return divertExcessToResearch.get(); }
	public void		setExcessToResearch(boolean b)	{ divertExcessToResearch.silentSet(b); }

	/** Shipbuilding with Governor enabled */
	public boolean	isShipbuilding()				{ return shipBuilding.get(); }
	public void		setShipbuilding(boolean b)		{ shipBuilding.silentSet(b); }

	public boolean	isFollowingColonyRequests()		{ return followColonyRequests.get(); }
	public void	setfollowColonyRequests(boolean b)	{ followColonyRequests.silentSet(b); }

	public boolean	isAutoInfiltrate()				{ return auto_Infiltrate.get(); }
	public void		setAutoInfiltrate(boolean b)	{ auto_Infiltrate.silentSet(b); }

	public boolean	isAutoSpy()						{ return auto_Spy.get(); }
	public void		setAutoSpy(boolean b)			{ auto_Spy.silentSet(b); }

	public boolean	respectPromises()				{ return respectPromises.get(); }
	public void		setRespectPromises(boolean b)	{ respectPromises.silentSet(b); }

	public boolean	isAutoScout()					{ return auto_Scout.get(); }
	public void		setAutoScout(boolean b)			{ auto_Scout.silentSet(b); }
	public void		toggleAutoScout()				{ auto_Scout.toggle(); }

	public boolean	isAutoColonize()				{ return govAutoColonize.get(); }
	public void		setAutoColonize(boolean b)		{ govAutoColonize.silentSet(b); }
	public void		toggleAutoColonize()			{ govAutoColonize.toggle(); }
	public String	autoColonizeTT()				{ return govAutoColonize.govTooltips(); }
	public String	autoColonizeText()				{ return govAutoColonize.govLabelTxt(); }

	public boolean	isAutoAttack()					{ return auto_Attack.get(); }
	public void		setAutoAttack(boolean b)		{ auto_Attack.silentSet(b); }
	public void		toggleAutoAttack()				{ auto_Attack.toggle(); }

	public int		getAutoScoutShipCount()			{ return autoScoutCount.get(); }
	public void		setAutoScoutShipCount(int i)	{ autoScoutCount.silentSet(i); }
	public void		autoScout(MouseWheelEvent e)	{ autoScoutCount.toggle(e); }

	public int		getAutoColonyShipCount()		{ return autoColonyCount.get(); }
	public void		setAutoColonyShipCount(int i)	{ autoColonyCount.silentSet(i); }
	public void		autoColony(MouseWheelEvent e)	{ autoColonyCount.toggle(e); }

	public int		getAutoAttackShipCount()		{ return autoAttackCount.get(); }
	public void		setAutoAttackShipCount(int i)	{ autoAttackCount.silentSet(i); }
	public void		autoAttack(MouseWheelEvent e)	{ autoAttackCount.toggle(e); }

	public boolean	isAutoShipsByDefault()			{ return autoShipsDefault.get(); }
	public void		setAutoShipsByDefault(boolean b){ autoShipsDefault.silentSet(b); }

	// Fine Tuning options: Not in the floating windows
	public String	subsidyNormalUse()				{ return subsidyNormalUse.get(); }
	public String	subsidyTerraformUse()			{ return subsidyTerraformUse.get(); }
	public float	workerToFactoryROILimit()		{ return workerToFactoryROI.get()/100f; }
	public int		maxColoniesForROI()				{ return maxColoniesForROI.get(); }
	public boolean	showTriggeredROI()				{ return showTriggeredROI.get(); }
	public float	terraformFactoryPct()			{ return terraformFactoryPct.get()/100f; }
	public float	terraformPopulationPct()		{ return terraformPopulationPct.get()/100f; }
	public float	terraformMissingPopulation()	{ return terraformPopulation.get(); }
	public float	terraformCost2Income()			{ return terraformCost2Income.get()/100f; }
	public int		defaultShipTakePct()			{ return defaultShipTakePct.get(); }
	public float	colonyDistanceWeight()			{ return colonyDistanceWeight.get()/100f; }
	public boolean	compensateGrowth()				{ return compensateGrowth.get(); }
	public float	minColonyGrowth()				{ return minColonyGrowth.get(); }
	public float	colonyEarlyBoostPct()			{ return colonyEarlyBoostPct.get()/100f; }
	public boolean	earlyBaseBuilding()				{ return earlyBaseBuilding.get(); }
	public float	earlyBaseBoostPct()				{ return earlyBaseBoostPct.get()/100f; }
	public boolean	armedScoutGuard()				{ return armedScoutGuard.get(); }
	public boolean	autoScoutSmart()				{ return autoScoutSmart.get(); }
	public boolean	autoScoutNearFirst()			{ return autoScoutNearFirst.get(); }
	public int		autoScoutMaxTime()				{ return autoScoutMaxTime.get(); }
	public boolean	armedColonizerGuard()			{ return armedColonizerGuard.get(); }
	public boolean	trainSpiesASAP()				{ return trainSpiesASAP.get(); }
	public boolean	contactUpdateSpending()			{ return contactUpdateSpending.get(); }
}
