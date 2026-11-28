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

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import rotp.Rotp;
import rotp.model.game.IBaseOptsTools;
import rotp.model.game.IGalaxyOptions;
import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ListShapeParam;
import rotp.model.game.IGameOptions;
import rotp.util.Base;
import rotp.util.Rand;

// BR: Added symmetric galaxies functionalities
// moved modnar companion worlds here with some more random positioning
// added some comments to help my understanding of this class
public abstract class GalaxyShape implements Base, Serializable {
	private static final long  serialVersionUID   = 1L;
	private static final int   GALAXY_EDGE_BUFFER = 12;
	private static final float maxMinEmpireFactor = 15f;
	private static final float absMinEmpireBuffer = 3.8f;
	private static final int   MaxPreviewSystems  = 5000;
	protected static final String RANDOM_OPTION   = "SETUP_RANDOM_OPTION";
	protected static final String UI_KEY		  = IBaseOptsTools.BASE_UI;
	protected static final String ROOT_NAME		  = "GALAXY_SHAPE_";
	protected static final double twoPI = Math.PI * 2.0; // BR:
	private static float orionBuffer = 10;
	protected static float empireBuffer = 8;

	public static boolean generating = false;

	private int maxEmpireTentative = 500;
	private List<StarPt> systems = new ArrayList<>();
	private List<StarPt> linkingSystems = new ArrayList<>();
	private List<StarPt> routeSystems = new ArrayList<>();
	private CompanionWorld[] companionWorlds; // BR:
	private int numCompanions;
	private ShapeRegion[][] regions;
	private int regionScale = 16;
	protected int fullWidth, fullHeight, width, height;
	int maxStars = 0;
	private int fillerStars = 0;
	private int totalStars = 0;
	private int num = 0;
	private int numFiller = 0;
	private int homeStars = 0;
	private int genAttempt = 0;
	private boolean usingRegions = false;
	private boolean fullyInit = false;
	List<EmpireSystem> empSystems = new ArrayList<>();
	private StarPt orionXY;
	IGameOptions opts;

	// BR: added for symmetric galaxy
	private static float cx; // Width galaxy center
	private static float cy; // Height galaxy center
	protected float sysBuffer = 1.9f;
	private float fillerBuffer = sysBuffer;
	private float fillerBufferFactor = 1f;
	private float maxPreviewFactor = 1f;
	private int numEmpires;
	private int numOpponents;
	protected Rand randRnd = new Rand(rng().nextLong()); // For random option selection purpose
	protected Rand rand	 = new Rand(randRnd.nextLong()); // For other than location purpose
	protected Rand randX = new Rand(randRnd.nextLong()); // For X and R
	protected Rand randY = new Rand(randRnd.nextLong()); // for Y and Angle
	private long tm0; // for timing computation
	// \BR

	private float dynamicGrowth	= 1f;
	private int currentEmpire	= 0;
	private int loopReserve		= 0;
	private int homeStarNum		= 3;
	private int loopValid		= 0;
	private boolean isValid		= false;

	protected int nominalSystemNumber; // BR: Original value, Without the fillers
	protected String finalOption1, finalOption2, finalOption3, finalOption4;
	protected int option1, option2, option3, option4;
	protected boolean[] randomizeShapeOptions;
	private boolean isSymmetric;
	private boolean looseLimits;
	private float maxDistanceToConnectEmpires;
	private boolean connectAllEmpires;
	private boolean connectedOrionToEmpires;
	private boolean connectAllStars;
	private boolean removeExcessLinks;

	protected abstract String name();
	protected abstract GalaxyShape get();
	GalaxyShape (IGameOptions options, boolean[] rndOpt) {
		if (options == null)
			return; // BR: used to create empty shape to query shape info
		opts = options;
		if (rndOpt == null)
			randomizeShapeOptions = new boolean[4];
		else
			randomizeShapeOptions = rndOpt;
	}
	protected void init0() {
		randRnd = new Rand(IGalaxyOptions.galaxyRandSource.get());
		rand	= randRnd;
		randX	= randRnd;
		randY	= randRnd;
		// in case of random, will generate a new value
		// takes into account the number of opponents, and may adjust it
		numOpponents = max(0, opts.selectedNumberOpponents(true));

		float fillerFactor	= opts.fillerSystemsFactor();
		maxPreviewFactor	= 1f + fillerFactor;
		fillerBufferFactor	= (float) (1.0/Math.sqrt(maxPreviewFactor));
		nominalSystemNumber	= opts.numberStarSystems(true);
		// Get the final value
		numOpponents = max(0, opts.selectedNumberOpponents());
		numEmpires	 = numOpponents + 1;

		initFinalOption1();
		initFinalOption2();
		initFinalOption3();
		initFinalOption4();

		isSymmetric = (finalOption1 != null && finalOption1.contains("SYMMETRIC"))
				|| (finalOption2 != null && finalOption2.contains("SYMMETRIC"));
		homeStarNum = opts.secondRingSystemNumber()+1;

		boolean allowConnection		= 50000 >= nominalSystemNumber * opts.selectedEmpireSpreadingFactor();
		maxDistanceToConnectEmpires	= opts.maxEmpiresConnextionGap() -0.1f;
		connectAllEmpires			= allowConnection && opts.areEmpiresConnectedToEmpires();
		connectedOrionToEmpires		= allowConnection && opts.areEmpiresConnectedToOrion();
		connectAllStars				= opts.areEmpiresConnectedToAllSystems() && nominalSystemNumber <= 5000;
		removeExcessLinks			= opts.removeNonRoutedConnections();

	}
	protected void initFinalOption1()	{
		finalOption1 = getOption1();
		if (RANDOM_OPTION.equals(finalOption1) || randomizeShapeOptions[0]) {
			List<String> optionList = new ArrayList<>(options1());
			optionList.remove(RANDOM_OPTION);
			finalOption1 = randRnd.random(optionList);
		}
		option1 = max(0, options1().indexOf(finalOption1));
	}
	protected void initFinalOption2()	{
		finalOption2 = getOption2();
		if (RANDOM_OPTION.equals(finalOption2) || randomizeShapeOptions[1]) {
			List<String> optionList = new ArrayList<>(options2());
			optionList.remove(RANDOM_OPTION);
			finalOption2 = randRnd.random(optionList);
		}
		option2 = max(0, options2().indexOf(finalOption2));
	}
	protected void initFinalOption3()	{ finalOption3 = getOption3(); }
	protected void initFinalOption4()	{ finalOption4 = getOption4(); }

	public int width()					{ return fullWidth; }
	public int height()					{ return fullHeight; }
	protected boolean fullyInit()		{ return fullyInit; }
	protected double starDist(StarPt p0, StarPt p1)	{
		final float dx = p1.x - p0.x;
		final float dy = p1.y - p0.y;
		return Math.sqrt(dx*dx + dy*dy);
	}

	// ========== Methods to query shape info ==========
	void registerOptions(Map<String, ListShapeParam> shapesMap)	{
		shapesMap.put(name(), new ListShapeParam(paramList()));
	}
	public int nominalSystemNumber()	{ return nominalSystemNumber; }
	public int neighborhoodsSize()		{ return empSystems.size() * homeStarNum; }
	public int finalSystemNumber()		{ return neighborhoodsSize() + systems.size(); }
	public String numberOfStarSystem()	{
		if (finalSystemNumber() == nominalSystemNumber())
			return str(nominalSystemNumber());
		else
			return nominalSystemNumber() + "(" + finalSystemNumber() + ")";
	}
	public ListShapeParam paramList()	{
		ListShapeParam list = new ListShapeParam();
		// ListShapeParam ignore null entries
		list.add(paramOption1());
		list.add(paramOption2());
		list.add(paramOption3());
		list.add(paramOption4());
		return list;
	}
	public IShapeOption<?> paramOption1()	{ return null; }
	public IShapeOption<?> paramOption2()	{ return null; }
	public IShapeOption<?> paramOption3()	{ return null; }
	public IShapeOption<?> paramOption4()	{ return null; }

	public List<String> options1()	{ return new ArrayList<>(); }
	public List<String> options2()	{ return new ArrayList<>(); }
	public String getOption1()		{
		if (paramOption1() == null || Rotp.noOptions())
			return "";
		return paramOption1().getCfgValue();
	}
	public String getOption2()		{
		if (paramOption2() == null || Rotp.noOptions())
			return "";
		return paramOption2().getCfgValue();
	}
	public String getOption3()		{
		if (paramOption3() == null || Rotp.noOptions())
			return "";
		return paramOption3().getCfgValue();
	}
	public String getOption4()		{
		if (paramOption4() == null || Rotp.noOptions())
			return "";
		return paramOption4().getCfgValue();
	}
	private float maxDistanceToConnectEmpires()	{ return maxDistanceToConnectEmpires; }
	private boolean connectAllEmpires()			{ return connectAllEmpires; }
	private boolean connectedOrionToEmpires()	{ return connectedOrionToEmpires; }
	private boolean isConnectAllStars()			{ return connectAllStars; }
	private boolean removeExcessLinks()			{ return removeExcessLinks; }

	// ========== abstract and overridable methods ==========
	protected abstract int galaxyWidthLY();
	protected abstract int galaxyHeightLY();
	protected abstract float sizeFactor(String size);
	private void getPointFromRandomStarSystem(StarPt pt) {
		int numSysPerEmpire	 = empSystems.get(0).numSystems();
		int numEmpireSystems = numEmpires * numSysPerEmpire;
		if (guiOptions().neverNebulaHomeworld())
			numEmpireSystems = 0; // Don't even think to start over one!

		int numUnsettledSys	= numberStarSystems();
		int totalSystems	= numEmpireSystems + numUnsettledSys;
		int starId = rand.nextInt(totalSystems);
		if (starId < numEmpireSystems) {
			int empireId = starId/numSysPerEmpire;
			int systemId = starId - empireId*numSysPerEmpire;
			EmpireSystem empSys = empSystems.get(empireId);
			pt.x = empSys.starPts[systemId].x;
			pt.y = empSys.starPts[systemId].y;
		}
		else {
			int systemId = starId - numEmpireSystems;
			StarPt p = coords(systemId);
			pt.x = p.x;
			pt.y = p.y;
		}
	}
	protected int numEmpires()	{return numEmpires; }
	protected void setRandom(StarPt pt)	{
		int edgeBuffer = galaxyEdgeBuffer();
		pt.x = edgeBuffer + (fullWidth  - 2*edgeBuffer) * randX.nextFloat();
		pt.y = edgeBuffer + (fullHeight - 2*edgeBuffer) * randY.nextFloat();
	}
	protected boolean valid(StarPt p)		{
		if (p == null)
			return false;
		if (p.x < 0 || p.y < 0)	return false;
		if (p.x > fullWidth)	return false;
		if (p.y > fullHeight)	return false;
		return true;
	}
	protected void clean() {} // To remove big temporary data;
	protected float settingsFactor(float shapeFactor) {
		// shapeFactor is not used yet
		float adjDensity = densitySizeFactor();
		float largeGal = 7f + 6f * (float) Math.log10(maxStars);
		float smallGal = 1.8f * sqrt(maxStars);
		float selected = max(4f, min(largeGal, smallGal));
		return adjDensity * selected * shapeFactor;
	}
    protected float densitySizeFactor() { return opts.densitySizeFactor(); }
/*    protected float densitySizeFactor() {
        float adj = 1.0f;
        switch (opts.selectedStarDensityOption()) {
            case IGameOptions.STAR_DENSITY_LOWEST:  adj = 1.3f; break;
            case IGameOptions.STAR_DENSITY_LOWER:   adj = 1.2f; break;
            case IGameOptions.STAR_DENSITY_LOW:     adj = 1.1f; break;
            case IGameOptions.STAR_DENSITY_HIGH:    adj = 0.9f; break;
            case IGameOptions.STAR_DENSITY_HIGHER:  adj = 0.8f; break;
            case IGameOptions.STAR_DENSITY_HIGHEST: adj = 0.7f; break;
        }
        return adj;
    } */
	// modnar: add possibility for specific placement of homeworld/orion locations
	// indexWorld variable will be used by setSpecific in each Map Shape for locations
	protected void setSpecific(StarPt pt)	{ setRandom(pt); }
	int indexWorld;

	protected float   minEmpireFactor()        { return 3f; }
	protected boolean allowExtendedPreview()   { return true; }
	protected boolean isSymmetric()            { return isSymmetric; }
	protected boolean isCircularSymmetric()    { return isSymmetric; }
	protected boolean isRectangulatSymmetric() { return false; }
	public CtrPoint getPlayerSymmetricHomeWorld() {
		// This may have been be abstract... but symmetric isn't mandatory...
		// So either an empty method here or one in every non symmetric child class
		return null;
	}
	public CtrPoint getValidRandomSymmetric() {
		// This may have been be abstract... but symmetric isn't mandatory...
		// So either an empty method here or one in every non symmetric child class
		return null;
	}
	private CtrPoint[] getOtherSymmetricSystems(CtrPoint src) {
		return null; // if relevant, this method should be overridden
	}
	// BR: ========== Getter Methods ==========
	//
	public int numCompanionWorld()	{ return numCompanions; }
	public int numberStarSystems()	{ return num; }
//	public int number
	int numberFillerSystems(){ return numFiller; }
	// modnar: add option to start game with additional colonies
	// modnar: these colonies are in addition to number of stars chosen in galaxy
	int totalStarSystems()	{
		return num + homeStars + opts.selectedCompanionWorlds()*numEmpires;
	}
	public List<EmpireSystem> empireSystems()	{ return empSystems; }
	protected float adjustedSizeFactor()	{ // BR: to converge more quickly
		float factor = sizeFactor(opts.selectedGalaxySize());
		float adjFactor = factor * dynamicGrowth;
		// System.out.println("adjustedSizeFactor: " + adjFactor);
		return adjFactor;
	}

	private float systemBuffer() { return opts.systemBuffer(opts.selectedStarDensityOption()); }
/*	public static float systemBuffer(String StarDensityOption) {
		switch (StarDensityOption) {
			case IGameOptions.STAR_DENSITY_LOWEST:  return 2.5f;
			case IGameOptions.STAR_DENSITY_LOWER:   return 2.3f;
			case IGameOptions.STAR_DENSITY_LOW:		return 2.1f;
			case IGameOptions.STAR_DENSITY_HIGH:	return 1.7f;
			case IGameOptions.STAR_DENSITY_HIGHER:  return 1.5f;
			case IGameOptions.STAR_DENSITY_HIGHEST: return 1.3f;
		}
		return 1.9f;
	} */
	public StarPt getCompanion(int empId, int compId) {
		return this.companionWorlds[empId].cW[compId].get();
	}
	// BR: ========== Very symmetry specific Methods ==========
	//
	private CtrPoint[] getCircularSymmetricSystems(CtrPoint src) {
		return src.rotate(twoPI/numEmpires, numOpponents);
	}
	private CtrPoint[] getRectangularSymmetricSystems(CtrPoint src) {
		return null; // To be implemented later
	}
	private CtrPoint[] getSymmetricSystems(CtrPoint src) {
		if (isCircularSymmetric()) {
			return getCircularSymmetricSystems(src);
		}
		else if (isRectangulatSymmetric()) {
			return getRectangularSymmetricSystems(src);
		}
		else {
			return getOtherSymmetricSystems(src);
		}
	}
	protected double galaxyRay()	{ return cx - galaxyEdgeBuffer(); }
	private boolean generateSymmetric(boolean full) {
		int num1 = opts.firstRingSystemNumber();
		int num2 = opts.secondRingSystemNumber();
		genAttempt    = 0;
		dynamicGrowth = densitySizeFactor();
		loopReserve   = 0;
		// add systems needed for empires
		while (empSystems.size() < numEmpires) { // Empires attempts loop
			if (full)
				fullInit();
			else
				quickInit();
			genAttempt++;
			if (genAttempt >= maxEmpireTentative)
				return false; // Failed

			empSystems.clear();
			homeStars = 0;
			num = 0;
			// ===== First Orion
			// Center of the galaxy is the only reasonable symmetric option
			orionXY = new StarPt(cx, cy);
			addSystem(orionXY);
			indexWorld = 1;
			// ===== Then the home world systems
			CtrPoint ptCtr = getPlayerSymmetricHomeWorld();
			StarPt pt = ptCtr.get();
			if (valid(pt) && !isTooNearExistingSystem(pt, true)) {
				EmpireSystem sys = new EmpireSystem(this, pt);
				empSystems.add(sys);
				homeStars++; // the nearby system will be set later
			} else {
				dynamicGrowth += 0.01f;
				continue; // Fail... Retry					
			}
			// ----- now the opponents Homes
			// get the stars
			CtrPoint[] opp = getSymmetricSystems(ptCtr);
			//  no test needed, they are valid by symmetry.
			for ( CtrPoint p : opp) {
				EmpireSystem sys = new EmpireSystem(this, p.get());
				empSystems.add(sys);
				homeStars++; // the nearby system will be set later
			}

			// ===== Then the nearby systems
			boolean valid = true;
			EmpireSystem player = empSystems.get(0);
			float buffer0 = sysBuffer;
			float radius1 = opts.firstRingRadius();
			float buffer1 = min(buffer0, radius1/sqrt(max(1.4f, num1)));
			float minRel1 = buffer1/radius1;
			minRel1 *= minRel1;
			for (int sysId=1; sysId<=num1; sysId++) { // variable nearby systems
				// get player nearby system
				if (player.addNearbySystemsSym(this, null, radius1, buffer1, minRel1))
					homeStars++;
				else {
					valid = false;
					break; // Fail... Retry
				}
				// ----- now the opponents
				// get the stars
				opp = getSymmetricSystems(new CtrPoint(player.starPts[sysId]));
				//  no test needed, they are valid by symmetry.
				for (int k=0; k<numOpponents; k++) {
					empSystems.get(k+1).addNearbySystemsSym(this, opp[k].get(), 0, 0, 0);
					homeStars++;
				}
			}

			float radius2 = opts.secondRingRadius();
			float buffer2 = min(buffer1, radius2/sqrt(max(1.4f, num1)));
			float minRel2 = buffer2/radius2;
			minRel2 *= minRel2;
			for (int nbSys=num1+1; nbSys<=num2; nbSys++) { // variable nearby systems
				// get player nearby system
				if (player.addNearbySystemsSym(this, null, radius2, buffer2, minRel2))
					homeStars++;
				else {
					valid = false;
					break; // Fail... Retry
				}
				// ----- now the opponents
				// get the stars
				opp = getSymmetricSystems(new CtrPoint(player.starPts[nbSys]));
				//  no test needed, they are valid by symmetry.
				for (int k=0; k<numOpponents; k++) {
					empSystems.get(k+1).addNearbySystemsSym(this, opp[k].get(), 0, 0, 0);
					homeStars++;
				}
			}
			if (!valid)
				continue; // something was wrong!
		} // The empires are set

		// ===== add other systems to fill out galaxy
		int attempts[] = addUncolonizedSystemsSymmetric();
		companionWorlds = new CompanionWorld(empSystems.get(0), numCompanions).symmetric();
		long tm1 = System.currentTimeMillis();
		log("Galaxy generation: "+(tm1-tm0)+"ms  Regions: " + usingRegions+"  Attempts: ", str(attempts[0]), "/", str(attempts[1]), "  stars:", str(num), "/", str(maxStars));
		return true;
	}
	private int[] addUncolonizedSystemsSymmetric() {
		int maxAttempts = maxStars * 10;
		// we've already generated 3(or more) stars for every empire so reduce their
		// total from the count of remaining stars to create ("too many stars" bug)
		int nonEmpireStars = maxStars - (empSystems.size() * homeStarNum);
		// Adjust for compatibility with symmetric galaxy 
		// Remove Orion, modulo number of Empires, then add Orion
		nonEmpireStars = 1 + Math.floorDiv(nonEmpireStars-1, numEmpires) * numEmpires;
		int attempts = 0;
		CtrPoint[] oppPt;
		while ((num < nonEmpireStars) && (attempts++ < maxAttempts)) {
			// Find a location
			CtrPoint pt = getValidRandomSymmetric();
			if (!isTooNearExistingSystem(pt.get(), false)) {
				addSystem(pt.get());
				// Then fill the symmetry
				// get the stars
				oppPt = getSymmetricSystems(pt);
				//  no test needed, they are valid by symmetry.
				for (CtrPoint opp : oppPt)
					addSystem(opp.get());
			}
		}

		// add filler systems
		int fillerAttempts = 0;
		if (fillerStars > 0) {
			numFiller = 0;
			int limitStars = totalStars - (empSystems.size() * homeStarNum);
			limitStars = 1 + Math.floorDiv(limitStars-1, numEmpires) * numEmpires;
			while ((num < limitStars) && (fillerAttempts++ < maxAttempts)) {
				// Find a location
				CtrPoint pt = getValidRandomSymmetric();
				if (!isTooNearExistingSystem(pt.get(), fillerBuffer)) {
					addSystem(pt.get());
					// Then fill the symmetry
					// get the stars
					oppPt = getSymmetricSystems(pt);
					//  no test needed, they are valid by symmetry.
					for (CtrPoint opp : oppPt) {
						addSystem(opp.get());
						numFiller++;
					}
				}
			}
			numFiller *= empSystems.size();
		}
		return new int[] {attempts, fillerAttempts};
	}
	// \BR
	public float maxScaleAdj()				{ return 1.0f; }
	public StarPt coords(int n)				{ return systems.get(n); }
	public List<StarPt> getSystems()		{ return systems; }
	public List<StarPt> linkingSystems()	{ return linkingSystems; }
	public List<StarPt> routeSystems()		{ return routeSystems; }
	public boolean isValid()	{ return isValid; }
	// BR: ========== Initialization Methods ==========
	//
	public float empireBuffer() { // BR: Made this parameter available for GUI
		float sysBuffer			 = systemBuffer();
		float minMaxEmpireBuffer = nominalSystemNumber/(numEmpires*2);
		float minEmpireBuffer    = sysBuffer * minEmpireFactor();
		float maxMinEmpireBuffer = sysBuffer * maxMinEmpireFactor;
		// the stars/empires ratio for the most "densely" populated galaxy is about 8:1
		// we want to set the minimum distance between empires to half that in ly, with a minimum
		// of 6 ly... this means that it will not increase until there is at least a 12:1
		// ratio. However, the minimum buffer will never exceed the "MAX_MIN", to ensure that
		// massive maps don't always GUARANTEE hundreds of light-years of space to expand uncontested
		float vanillaBuffer = min(maxMinEmpireBuffer, max(minEmpireBuffer, minMaxEmpireBuffer));
		float spreadBuffer  = vanillaBuffer * opts.selectedEmpireSpreadingFactor();
		return max(spreadBuffer, absMinEmpireBuffer);
	}
	protected void singleInit(boolean full) {
		if (full)
			maxStars = nominalSystemNumber();
		else
			maxStars = min((int)(MaxPreviewSystems/maxPreviewFactor), nominalSystemNumber);

		// common symmetric and non symmetric initializer for generation
		log("Galaxy shape: "+maxStars+ " stars"+ "  regionScale: "+regionScale+"   emps:"+numEmpires);
		tm0 = System.currentTimeMillis();
		empSystems.clear();

		// systemBuffer() is minimum distance between any 2 stars
		sysBuffer    = systemBuffer();
		fillerBuffer = sysBuffer * fillerBufferFactor;
		empireBuffer = empireBuffer();
		// Orion buffer is 50% greater with minimum of 8 ly.
		orionBuffer  = max(4 * sysBuffer, empireBuffer*3/2); // BR: Restored Vanilla values.
		// BR: Player customization
		orionBuffer  = max(sysBuffer, orionBuffer * opts.orionToEmpireModifier());

		looseLimits = opts.looseNeighborhood();
	}
	private void fullInit() {
		fullyInit = true;
		init(nominalSystemNumber);
	}
	private void quickInit() {
		fullyInit = false;
		init(min((int)(MaxPreviewSystems/maxPreviewFactor), nominalSystemNumber));
	}
	protected void init(int numStars) {
		// System.out.println("========== GalaxyShape.init(): genAttempt = " + genAttempt);
		numCompanions = opts.signedCompanionWorlds();
		num = 0;
		numFiller = 0;
		homeStars = 0;
		empSystems.clear();
		maxStars	= numStars;
		totalStars	= (int)(maxStars * maxPreviewFactor);
		fillerStars	= totalStars - maxStars;

		initWidthHeight();
		float minSize = min(fullWidth, fullHeight);
		usingRegions = (minSize > 100) && !isSymmetric();

		// BR: The "No region" is now always built to be able to retrieve the placement sequence
		systems.clear();
		linkingSystems.clear();
		routeSystems.clear();

		// BR: The region are still used to speed up the distance validation
		if (usingRegions) {
			regionScale = min(64, (int) (minSize / 6.0));
			regions = new ShapeRegion[regionScale][regionScale];
			// int regionStars = (int) (2.5*maxStars/regionScale); // BR: reserve no more needed
			int regionStars = (int) (0.25*totalStars/regionScale); // BR: enough for dynamic arrays
			regionStars = max(2, regionStars); // BR: should never be a problem... But!
			for (int i=0;i<regionScale;i++) {
				for (int j=0;j<regionScale;j++)
					regions[i][j] = new ShapeRegion(regionStars);
			}
		}
	}
	protected void initWidthHeight() {
		width  = galaxyWidthLY();
		height = galaxyHeightLY();
		fullWidth  = width  + (2 * galaxyEdgeBuffer());
		fullHeight = height + (2 * galaxyEdgeBuffer());
		// BR: for symmetric galaxy
		cx = fullWidth  / 2.0f;
		cy = fullHeight / 2.0f;
	}
	boolean fullGenerate() {
		if (generating) {
			System.err.println("setGalaxyShape() while generating");
		}
		maxEmpireTentative = 500;
		generating = true;
		loopValid = 0;
		isValid = generateValid(true);
		clean();
		generating = false;
		return isValid;
	}
	public boolean quickGenerate() {
		if (generating) {
			System.err.println("setGalaxyShape() while generating");
		}
		maxEmpireTentative = 500;
		generating = true;
		loopValid = 0;
		if (IGalaxyOptions.galaxyRandSource.get() == 0 || !allowExtendedPreview())
			isValid = generateValid(false);
		else
			isValid = generateValid(true);
		clean();
		generating = false;
		return isValid;
	}
/*	private void displayDebug() {
		int nbSys = opts.numberStarSystems();
		String size = opts.selectedGalaxySize();
		float dynFactor  = settingsFactor(1);
		float baseFactor = sizeFactor(size);
		System.out.format("Nb Stars = %6d; Spreading = %3d; factorRatio = %4.2f; genAttempt = %5d; corrFactor = %4.2f",
				nbSys,
				opts.selectedEmpireSpreadingPct(),
				dynFactor/baseFactor,
				genAttempt,
				dynamicGrowth
				);
		System.out.println("  Shape = " + opts.selectedGalaxyShape());
	} */
	private float growthFactor() {
		if (currentEmpire == 0)
			return 2f;
		float missingEmpire  = max(1f, numEmpires-empSystems.size()+loopReserve);
		float targetExponent = missingEmpire/5 	* numEmpires/currentEmpire;
		float targetFactor   = (float) Math.pow(1.05, targetExponent);
		float growthFactor   = max(min(targetFactor, 2f), 1.05f);
//		System.out.println("Break at:" + empSystems.size() +
//				"  growthFactor = " + growthFactor +
//				"  dynamicGrowth = " + dynamicGrowth
//				);
		return growthFactor;
	}
	private boolean generateValid(boolean full) {
		init0();
		loopValid +=1;
		if (generate(full)) {
			//System.out.println("generateValid(" + full + ") " + genAttempt + " attempts");
			return true;
		}
		// Some issues... Switch to an easy shape
		if (loopValid == 0) {
			System.err.println("Failed generateValid(" + full + ") " + genAttempt + " attempts");
			clean();
			IGalaxyOptions.shapeSelection.setFromDefault(false, false);
			if (generate(full)) {
				System.out.println("default generateValid(" + full + ") " + genAttempt + " attempts");
				return true;
			}
		}
		// more issues...
		System.err.println("Failed default generateValid(" + full + ") " + genAttempt + " attempts");
		clean();
		return false;
	}
	private boolean generate(boolean full) {
		singleInit(full);
		if (isSymmetric())
			return generateSymmetric(full);

		genAttempt = 0;
		dynamicGrowth = densitySizeFactor();
		// add systems needed for empires
		while (empSystems.size() < numEmpires) {
			if (full)
				fullInit();
			else
				quickInit();
			genAttempt++;
			if (genAttempt >= maxEmpireTentative)
				return false; // Failed
			empSystems.clear();
			homeStars = 0;
			num = 0;
			orionXY = addOrion();
			indexWorld = 1; // modnar: after specific orion placement, set indexWorld=1 for homeworlds
			loopReserve = 1 + numEmpires/10;
			int fail = 0;
			for (currentEmpire=0;
					currentEmpire<numEmpires+loopReserve && numEmpires != empSystems.size();
					currentEmpire++) {
				EmpireSystem sys = new EmpireSystem(this);
				if (sys.valid) {
					empSystems.add(sys);
					homeStars += sys.numSystems();
				}
				else {
					fail++;
					if (fail >= loopReserve) {
						dynamicGrowth *= growthFactor();
						break;
					}
				}
			}
		}

		// add other systems to fill out galaxy
		int attempts[] = addUncolonizedSystems();
		// add other systems to fill out galaxy
		addCompanionsWorld();
		long tm1 = System.currentTimeMillis();
		log("Galaxy generation: "+(tm1-tm0)+"ms  Regions: " + usingRegions+"  Attempts: ", str(attempts[0]), "/", str(attempts[1]), "  stars:", str(num), "/", str(maxStars));
		return true;
	}
	protected int galaxyEdgeBuffer() {
		int minEdge =  opts.looseNeighborhood()? (int) opts.secondRingRadius() : 1;
		switch(opts.selectedGalaxySize()) {
			case IGameOptions.SIZE_MICRO:	  return max(minEdge, 1);
			case IGameOptions.SIZE_TINY:	  return max(minEdge, 1);
			case IGameOptions.SIZE_SMALL:	  return max(minEdge, 1);
			case IGameOptions.SIZE_SMALL2:	  return max(minEdge, 1);
			case IGameOptions.SIZE_MEDIUM:	  return max(minEdge, 2);
			case IGameOptions.SIZE_MEDIUM2:   return max(minEdge, 2);
			case IGameOptions.SIZE_LARGE:	  return max(minEdge, 2);
			case IGameOptions.SIZE_LARGE2:	  return max(minEdge, 2);
			case IGameOptions.SIZE_HUGE:	  return max(minEdge, 3);
			case IGameOptions.SIZE_HUGE2:	  return max(minEdge, 3);
			case IGameOptions.SIZE_MASSIVE:   return max(minEdge, 3);
			case IGameOptions.SIZE_MASSIVE2:  return max(minEdge, 3);
			case IGameOptions.SIZE_MASSIVE3:  return max(minEdge, 3);
			case IGameOptions.SIZE_MASSIVE4:  return max(minEdge, 4);
			case IGameOptions.SIZE_MASSIVE5:  return max(minEdge, 4);
			case IGameOptions.SIZE_INSANE:    return max(minEdge, 5);
			case IGameOptions.SIZE_LUDICROUS: return max(minEdge, 8);
			case IGameOptions.SIZE_DYNAMIC:   return max(minEdge, (int) (Math.log10(maxStars)));
		}
		return GALAXY_EDGE_BUFFER;
	}
	private StarPt addOrion() {
		indexWorld = 0; // modnar: explicitly set indexWorld=0 for orion
		StarPt pt = findSpecificValidLocation(); // modnar: specific placement for orion location
		if (pt == null)
			pt = new StarPt(cx, cy);
		pt.setAsOrion();
		addSystem(pt);
		return pt;
	}
	private void addCompanionsWorld() {
		companionWorlds = new CompanionWorld[numEmpires];
		for (int i=0; i<numEmpires; i++) {
			companionWorlds[i] = new CompanionWorld(empSystems.get(i), numCompanions);
		}
	}
	private int[] addUncolonizedSystems() {
		int maxAttempts = maxStars * 10;

		// we've already generated 3(or more) stars for every empire so reduce their
		// total from the count of remaining stars to create ("too many stars" bug)
		int nonEmpireStars = maxStars - (empSystems.size() * homeStarNum);
		int attempts = 0;
		while ((num < nonEmpireStars) && (attempts++ < maxAttempts)) {
			StarPt pt = findAnyValidLocation();
			if (!isTooNearExistingSystem(pt, false))
				addSystem(pt);
		}

		if (connectAllEmpires())
			new ConnectEmpire(maxDistanceToConnectEmpires()).connectEmpire();

		// add filler systems
		int fillerAttempts = 0;
		if (fillerStars > 0) {
			numFiller = 0;
			int limitStars = totalStars - (empSystems.size() * homeStarNum);
			while ((num < limitStars) && (fillerAttempts++ < maxAttempts)) {
				StarPt pt = findAnyValidLocation();
				if (!isTooNearExistingSystem(pt, fillerBuffer)) {
					pt.setAsAsteroid();
					addSystem(pt);
					numFiller++;
				}
			}
		}
		// System.out.println("addUncolonizedSystems(): attempts/maxStars = "
		//                    + (float)attempts/maxStars);
		return new int[] {attempts, fillerAttempts};
	}
	private StarPt findAnyValidLocation() {
		StarPt p = new StarPt();
		setRandom(p);
		while (!valid(p))
			setRandom(p);
		return p;
	}
	// modnar: add specific placement of orion/homeworld locations
	private StarPt findSpecificValidLocation() {
		StarPt p = new StarPt();
		boolean valid = false;
		//indexWorld++; // modnar: increment indexWorld for subsequent homeworld locations
		while (!valid && indexWorld<2000) {
			setSpecific(p);
			valid = valid(p);
			// modnar: incrementing indexWorld here to prevent accidental infinite loop of bad locations,
			// but need setSpecific to have some form of repeating modulo cut-off
			indexWorld++;
		}
		if (!valid) { // to avoid infinite loop
			p.x = cx;
			p.y = cy;
			//System.err.println("Center point generated: num = " + num);
			return null;
		}
		return p;
	}
	private void addSystem(StarPt pt) { // BR: changed to protected
		if (num == 0) { // Orion: already stored in orionXY!
			systems.add(pt);
			num++;
			return;
		}
		// BR: The region are still used to speed up the distance validation
		if (usingRegions) {
			int xRgn = bounds(0, (int)(regionScale * pt.x / fullWidth), regionScale-1);
			int yRgn = bounds(0, (int) (regionScale * pt.y / fullHeight), regionScale-1);
			regions[xRgn][yRgn].addSystem(pt);
		}
		// BR: The "No region" is now always built to be able to retrieve the placement sequence
		systems.add(pt);
		num++;
		if (num != systems.size())
			System.out.println("num != systems.size() " + num + " vs " + systems.size());
	}
	private boolean isTooNearExistingSystem(StarPt pt, boolean isHomeworld)	{
		if (isHomeworld) {
			if (starDist(pt, orionXY) <= orionBuffer)
				return true;
			for (EmpireSystem emp: empSystems) {
				if (starDist(pt, emp.colony()) <= empireBuffer)
					return true;
			}
		}
		// float buffer = systemBuffer(); // BR: made global
		// not too close to other systems in galaxy
		return isTooNearExistingSystem(pt, sysBuffer);
	}
	private boolean isTooNearExistingSystem(StarPt pt, float buffer)	{
		if (usingRegions) {
			if (isTooNearSystemsInNeighboringRegions(pt, buffer))
				return true;
		}
		else {
			if (isTooNearSystemsInEntireGalaxy(pt, buffer)) // BR: global
				return true;
		}
		// not too close to other systems in any empire system
		for (EmpireSystem emp: empSystems)
			for (int i=0; i<emp.num; i++)
				if (starDist(pt, emp.nearby(i)) <= buffer) // BR: global
					return true;
		return false;
	}
	private boolean isTooNearSystemsInNeighboringRegions(StarPt pt, float buffer)	{
		int xRgn = (int)(pt.x * regionScale / fullWidth);
		int yRgn = (int)(pt.y * regionScale / fullHeight);
		int yMin = max(0, yRgn-1);
		int yMax = min(regionScale-1, yRgn+1);
		int xMin = max(0, xRgn-1);
		int xMax = min(regionScale-1, xRgn+1);

		for (int x1=xMin; x1<=xMax; x1++)
			for (int y1=yMin; y1<=yMax; y1++)
				if (regions[x1][y1].isTooNearSystems(pt, buffer))
					return true;

		return starDist(pt, orionXY) <= buffer;
	}

	private boolean isTooNearSystemsInEntireGalaxy(StarPt pt, float buffer)	{
		for (StarPt sys : systems)
			if (starDist(pt, sys) <= buffer)
				return true;
		return false;
	}
	// ##### Nebulae Management
	// BR: Moved here from galaxy, for preview purpose
	public void createNebulas(List<Nebula> nebulas) {
		int numNebula = opts.numberNebula();
		float nebSize = opts.nebulaSizeMult();
		Nebula.reinit(rand.nextLong());
		// add the nebulae
		// for each nebula, try to create it at the options size
		// in unsuccessful, decrease option size until it is
		// less than 1 or less than half of the option size
		for (int i=0; i<numNebula; i++) {
			float size = nebSize;
			boolean added = false;
			while(!added) {
				added = addNebula(size, nebulas);
				if (!added) {
					size--;
					added = size < 1;
				}
			}
		}
	}
	// BR: may be used later for a preview
	private boolean addNebula(float nebSize, List<Nebula> nebulas) {
		int numTentatives = opts.nebulaCallsBeforeShrink();
		for (int i=0; i<numTentatives; i++) {
			Nebula neb = tryAddNebula(nebSize, nebulas);
			if ( neb != null) {
				nebulas.add(neb);
				return true;
			}
		}
		return false;
	}
	private Nebula tryAddNebula(float nebSize, List<Nebula> nebulas) {
        // each nebula creates a buffered image for display
        // after we have created 5 nebulae, start cloning
        // existing nebulae (add their images) when making
        // new nebulae
        int MAX_UNIQUE_NEBULAS = 16;
        boolean looseNebula = guiOptions().looseNebula();
        StarPt pt = new StarPt();
        getPointFromRandomStarSystem(pt); // Safer, the point will be modified

        Nebula neb;
        if (nebulas.size() < MAX_UNIQUE_NEBULAS)
            neb = new Nebula(nebSize, true);
        else
            neb = random(nebulas).copy();

        float w = neb.adjWidth();
        float h = neb.adjHeight();
        // BR: Needed by Bitmap Galaxies
        // Center the nebula on the star
		pt.x -= w/2;
		pt.y -= h/2;
		if (!looseNebula && !valid(pt))
			return neb.cancel();

		neb.setXY(pt.x, pt.y);
		if (!looseNebula) {
			if (!valid(new StarPt(pt.x+w, pt.y)))
				return neb.cancel();
			if (!valid(new StarPt(pt.x+w, pt.y+h)))
				return neb.cancel();
			if (!valid(new StarPt(pt.x, pt.y+h)))
				return neb.cancel();
		}
        if (guiOptions().neverNebulaHomeworld())
	        for (EmpireSystem sys : empSystems)
	            if (sys.inNebula(neb))
	            	return neb.cancel();

        if (guiOptions().selectedRealNebula()) {
            // don't add nebulae to close to an existing nebula
            for (Nebula existingNeb: nebulas)
                if (existingNeb.isToClose(neb))
                	return neb.cancel();
        }
        else {
            // don't add classic nebulae whose center point is in an existing nebula
            for (Nebula existingNeb: nebulas)
                if (existingNeb.contains(neb.centerX(), neb.centerY()))
                	return neb.cancel();
        }
        return neb;
    }

	// ========================================================================
	// Nested Classes
	//
	private final class ConnectEmpire	{
		private final List<Star> stars;
		private final List<CentralStar> centralStars;
		private final List<CentralStar> routeCentralStars; // for empire and orion only
		private final StarSet linkStars;
		private final float maxDist, maxSquareDist;
		private int id, firstLinkId;
		private int minMissingEmpire;
		private RouteFinder<Star> routeFinder;

		ConnectEmpire(final float maxDistance)	{
			stars = new ArrayList<>();
			linkStars = new StarSet();
			centralStars = new ArrayList<>();
			routeCentralStars = new ArrayList<>();
			maxDist = maxDistance;
			maxSquareDist = maxDist * maxDist;
			init();
		}
		private void init()	{ // Build Star List
			id = 0;
			minMissingEmpire = numEmpires;
			// Empire Systems
			for (EmpireSystem empSys: empireSystems()) {
				CentralStar star = new CentralStar(empSys.colony(), id++);
				centralStars.add(star);
				routeCentralStars.add(star);
				stars.add(star);
				for (int i=1; i<empSys.num; i++)
					stars.add(new Star(empSys.nearby(i), id++));
			}

			// Companions' worlds
			if (companionWorlds != null)
				for (int i=0; i<numEmpires; i++)
					for (int j=0; j<numCompanions; j++)
						stars.add(new Star(getCompanion(i, j), id++));

			// Other systems
			if (connectedOrionToEmpires()) {
				CentralStar star = new CentralStar(systems.get(0), id++);
				centralStars.add(star);
				routeCentralStars.add(star);
				stars.add(star);
				for (int i=1; i<systems.size(); i++)
					stars.add(new Star(systems.get(i), id++));
			}
			else
				for (StarPt sys : systems)
					stars.add(new Star(sys, id++));

			firstLinkId = id;
		}
		private void connectEmpire() {
			final double maxStep = maxDist;
			final double minStep = sysBuffer;
			linkStars.clear();
			linkingSystems().clear();
			routeSystems.clear();
			computeAllDistances(stars);
			if (buildAllGroups(centralStars, minStep, maxStep)) {
				if (isConnectAllStars())
					connectAllStars(centralStars.get(0).cluster, minStep, maxStep);
				computeAllDistances(stars);
				buildAllGroups(centralStars, minStep, maxStep);
				findRoutes(routeCentralStars);
			}
			else {
				log("Failed to connect Empires: linking Systems size = " + linkingSystems().size());
				System.out.println("Failed to connect Empires: linking Systems size = " + linkingSystems().size());
			}
			log("Connect Empires: linking Systems size = " + linkingSystems().size());
			log("Connect Empires: route Systems size = " + routeSystems().size());
			// System.out.println("linking Systems size = " + linkingSystems().size());
			// System.out.println("route Systems size = " + routeSystems().size());

			if (removeExcessLinks() && !isConnectAllStars()) {
				for (Iterator<StarPt> iPt = systems.iterator(); iPt.hasNext();) {
					StarPt pt = iPt.next();
					if (pt.isConnection() && !pt.isRoute())
						iPt.remove();
				}
				num = systems.size();
				for (Iterator<StarPt> iPt = linkingSystems.iterator(); iPt.hasNext();) {
					StarPt pt = iPt.next();
					if (pt.isConnection() && !pt.isRoute())
						iPt.remove();
				}
				log("Remove Links: linking Systems size = " + linkingSystems().size());
				log("Remove Link: route Systems size = " + routeSystems().size());
				// System.out.println("linking Systems size = " + linkingSystems().size());
				// System.out.println("route Systems size = " + routeSystems().size());
			}
		}
		private void findRoutes(final List<CentralStar> centralStarList)	{
			StarSet nodes = centralStarList.get(0).cluster;
			Map<String, Set<String>> connections = new HashMap<>();
			for (Star node : nodes)
				connections.put(node.getId(), node.getIdSet());

			Graph<Star> graph = new Graph<>(nodes, connections);
			Scorer<Star> nextNodeScorer = new NextNodeScorer();
			Scorer<Star> targetScorer = new TargetScorer();
			routeFinder = new RouteFinder<>(graph, nextNodeScorer, targetScorer);

			StarSet allPath = new StarSet();
			for (int i=1; i<centralStarList.size(); i++) {
				List<Star> path = routeFinder.findRoute(centralStarList.get(i-1), centralStarList.get(i));
				float maxD = 0;
				for (int s=1; s<path.size(); s++) {
					Star s1 = path.get(s-1);
					Star s2 = path.get(s);
					float dx = s2.pt.x - s1.pt.x;
					float dy = s2.pt.y - s1.pt.y;
					float dist = sqrt(dx*dx+dy*dy);
					if (dist > maxD)
						maxD = dist;
				}
				// System.out.println("Path " + i + " Size = " + path.size() + " Max Distance = " + fmt(maxD, 1) + " (" + fmt(maxDist, 1) + ")");
				allPath.addAll(path);
			}
			// System.out.println("AllPath Size = " + allPath.size());
			routeSystems.clear();
			for (Star star : allPath) {
				star.pt.setAsRoute();
				routeSystems.add(star.pt);
			}
		}
		private void addConnectingStars(List<CentralStar> centralStars, double minStep, double maxStep)	{
			List<NearestStarPair> nearestPairs = new ArrayList<>();
			StarSet listedStars = new StarSet();
			for (CentralStar centralStar : centralStars) {
				NearestStarPair pair = new NearestStarPair(centralStar.cluster);
				if (!listedStars.contains(pair.star1) &&  !listedStars.contains(pair.star2)) {
					nearestPairs.add(pair);
					linkStars.addAll(pair.getLinkingStarPts(minStep, maxStep));
				}
			}
		}
		private void connectAllStars(StarSet cluster, double minStep, double maxStep)	{
			int lastClusterSize = 0;
			while (stars.size() > cluster.size() && lastClusterSize != cluster.size()) {
				lastClusterSize = cluster.size();
				NearestStarPair pair = new NearestStarPair(cluster);
				linkStars.addAll(pair.getLinkingStarPts(minStep, maxStep));
			}
		}
		private List<CentralStar> getClusters(final List<CentralStar> centralStarList)	{
			StarSet missing = new StarSet(centralStarList);
			List<CentralStar> clusterHomes = new ArrayList<>();
			for (CentralStar centralStar : centralStarList)
				if (missing.contains(centralStar)) {
					clusterHomes.add(centralStar);
					for (Iterator<Star> iStar = missing.iterator(); iStar.hasNext();) {
						Star star = iStar.next();
						if (centralStar.cluster.contains(star))
							iStar.remove();
					}
				}
			// System.out.println("number of isolated clusters: " + clusterHomes.size());
			// log("number of isolated clusters: " + clusterHomes.size());
			return clusterHomes;
		}
		private boolean buildAllGroups(final List<CentralStar> centralStarList, double minStep, double maxStep)	{
			// Build and check that all central systems are connected
			// System.out.println();
			// System.out.println("validateDistances()");

			// Initialize and Check if they are already connected
			for (CentralStar homeStar : centralStarList)
				if (homeStar.buildAndValidateCentrals(centralStarList))
					return true; // Connected to all required stars

			int maxLoop = max(2000, num);
			int count = 0;
			while (count < maxLoop) {
			// System.out.println("validateDistances() " + count + "/" + maxLoop);
				// Some systems are isolated
				// System.out.println("Isolated Clusters detected");
				//log("Isolated Clusters detected");
				List<CentralStar> clusterHomes = getClusters(centralStars);
	
				// Get fixing stars
				addConnectingStars(clusterHomes, minStep, maxStep);

				for (CentralStar centralStar : clusterHomes)
					if (centralStar.buildAndValidateCentrals(centralStarList))
						return true; // Connected to all required stars

				count++;
			}
			return count < maxLoop;
		}
		private void computeAllDistances(final List<Star> starList)	{
			final int size = starList.size();
			for (int i=0; i<size; i++) {
				final Star star = starList.get(i);
				for (int j=i+1; j<size; j++)
					star.validateDistance(starList.get(j), maxSquareDist);
			}
		}
		// A* Path finding based on Baeldung course
		// https://www.baeldung.com/java-a-star-pathfinding
		public interface GraphNode	{ String getId(); }
		public interface Scorer<T extends GraphNode>	{ double computeCost(T from, T to); }
		private class TargetScorer implements Scorer<Star>	{
			@Override public double computeCost(Star from, Star to)	{
				double dx = to.pt.x - from.pt.x;
				double dy = to.pt.y - from.pt.y;
				return Math.sqrt(dx*dx + dy*dy);
			}
		}
		private class NextNodeScorer implements Scorer<Star>	{
			@Override public double computeCost(Star from, Star to)	{
				if (to instanceof CentralStar)
					return 0;
				if (to.isLink())
					return 2;
				return 4;
			}
		}
		public class Graph<T extends GraphNode>	{
			private final Set<T> nodes;
			private final Map<String, Set<String>> connections;

			Graph(Set<T> nodes, Map<String, Set<String>> connections)	{
				this.nodes = nodes;
				this.connections = connections;
			}

			public T getNode(String id) {
				return nodes.stream()
						.filter(node -> node.getId().equals(id))
						.findFirst()
						.orElseThrow(() -> new IllegalArgumentException("No node found with ID"));
			}

			public Set<T> getConnections(T node)	{
				return connections.get(node.getId()).stream()
						.map(this::getNode)
						.collect(Collectors.toSet());
			}
		}
		class RouteNode<T extends GraphNode> implements Comparable<RouteNode<T>> {
			private final T current;
			private T previous;
			private double routeScore;
			private double estimatedScore;

			RouteNode(T current)	{
				this(current, null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			}
			RouteNode(T current, T previous, double routeScore, double estimatedScore)	{
				this.current = current;
				this.previous = previous;
				this.routeScore = routeScore;
				this.estimatedScore = estimatedScore;
			}
			@Override public int compareTo(RouteNode<T> other)	{
				if (this.estimatedScore > other.estimatedScore)
					return 1;
				else if (this.estimatedScore < other.estimatedScore)
					return -1;
				else
					return 0;
			}
			private T getCurrent()						{ return current; }
			private T getPrevious()						{ return previous; }
			private void setPrevious(T node)			{ previous = node; }
			private double getRouteScore()				{ return routeScore; }
			private void setRouteScore(double d)		{ routeScore = d; }
			private void setEstimatedScore(double d)	{ estimatedScore = d; }
		}
		public class RouteFinder<T extends GraphNode> {
			private final Graph<T> graph;
			private final Scorer<T> nextNodeScorer;
			private final Scorer<T> targetScorer;

			RouteFinder(Graph<T> graph, Scorer<T> nextNodeScorer, Scorer<T> targetScorer)	{
				this.graph = graph;
				this.nextNodeScorer = nextNodeScorer;
				this.targetScorer = targetScorer;
			}
			public List<T> findRoute(T from, T to) {
				Queue<RouteNode<T>> openSet = new PriorityQueue<>();
				Map<T, RouteNode<T>> allNodes = new HashMap<>();

				RouteNode<T> start = new RouteNode<>(from, null, 0d, targetScorer.computeCost(from, to));
				openSet.add(start);
				allNodes.put(from, start);

				while (!openSet.isEmpty()) {
					RouteNode<T> next = openSet.poll();
					if (next.getCurrent().equals(to)) {
						List<T> route = new ArrayList<>();
						RouteNode<T> current = next;
						do {
							route.add(0, current.getCurrent());
							current = allNodes.get(current.getPrevious());
						}
						while (current != null);
						return route;
					}
					graph.getConnections(next.getCurrent()).forEach(connection -> { 
						RouteNode<T> nextNode = allNodes.getOrDefault(connection, new RouteNode<>(connection));
						allNodes.put(connection, nextNode);

						double newScore = next.getRouteScore() + nextNodeScorer.computeCost(next.getCurrent(), connection);
						if (newScore < nextNode.getRouteScore()) {
							nextNode.setPrevious(next.getCurrent());
							nextNode.setRouteScore(newScore);
							nextNode.setEstimatedScore(newScore + targetScorer.computeCost(connection, to));
							openSet.add(nextNode);
						}
					});
				}
				throw new IllegalStateException("No route found");
			}
		}

		private final class NearestStarPair {
			StarSet cluster;
			Star star1, star2;
			float minSquareDist;

			NearestStarPair(StarSet cluster)	{
				this.cluster = cluster;
				minSquareDist = Float.MAX_VALUE;
				List<Star> otherStars = new ArrayList<>(stars);
				otherStars.removeAll(cluster);
				StarSet clusterCopy = new StarSet(cluster);
				for (Star s1 : clusterCopy)
					for (Star s2 : otherStars) {
						final float dx = s2.pt.x - s1.pt.x;
						final float dy = s2.pt.y - s1.pt.y;
						final float squareDist = dx*dx + dy*dy;
						if (squareDist <= maxSquareDist) {
							s1.inRangeStars.add(s2);
							s2.inRangeStars.add(s1);
							cluster.add(s2);
						}
						else if (squareDist < minSquareDist) {
							minSquareDist = squareDist;
							star1 = s1;
							star2 = s2;
						}
				}
			}

			private void tryNextStep(StarPt ps, StarPt pe, StarPt pt, double minStep, double maxStep)	{
				final double maxDev = 2;
				final double phi = Math.atan2(pe.y - ps.y, pe.x - ps.x) + maxDev * (randY.nextDouble()-0.5);
				final double step = randY.nextDouble(minStep, maxStep);
				pt.x = (float) (ps.x + step * Math.cos(phi));
				pt.y = (float) (ps.y + step * Math.sin(phi));
			}
			private double getValidNextStep(StarPt ps, StarPt pe, StarPt pt, double minStep, double maxStep)	{
				tryNextStep(ps, pe, pt, minStep, maxStep);
				double dist = pt.distanceTo(pe);
				int maxTry = 100;
				while (dist < minStep || isTooNearExistingSystem(pt, false)) {
					tryNextStep(ps, pe, pt, minStep, maxStep);
					dist = pt.distanceTo(pe);
					if (--maxTry < 0)
						return -1;
				}
				return dist;
			}
			StarSet getLinkingStarPts(double minStep, double maxStep)	{
				StarSet newStars = new StarSet();
				Star star = star1;
				double dist = Double.MAX_VALUE;
				while (dist > maxStep) {
					StarPt pt = new StarPt();
					dist = getValidNextStep(star.pt, star2.pt, pt, minStep, maxStep);
					if (dist < 0)
						return newStars;
					pt.setAsConnect();
					addSystem(pt);
					linkingSystems.add(pt);
					Star link = new Star(pt, id++);
					newStars.add(link);
					link.inRangeStars.add(star);
					star.inRangeStars.add(link);
					stars.add(star);
					cluster.add(link);
					star = link;
				}
				star2.inRangeStars.add(star);
				star.inRangeStars.add(star2);
				return newStars;
			}
		}
		private final class CentralStar extends Star {
			final StarSet cluster = new StarSet();
			final Set<CentralStar> missingCentral = new HashSet<>();

			CentralStar(StarPt pt, int id)	{ super(pt, id); }

			private StarSet buildGroup()	{
				cluster.clear();;
				cluster.add(this);
				StarSet prevCluster = new StarSet();
				StarSet addedStars = new StarSet(cluster);

				while (!addedStars.isEmpty()) {
					prevCluster.addAll(addedStars);
					for (Star star : addedStars)
						cluster.addAll(star.inRangeStars);
					addedStars.addAll(cluster);
					addedStars.removeAll(prevCluster);
				}
				return inRangeStars;
			}
			private boolean buildAndValidateCentrals(List<CentralStar> centralStars)	{
				buildGroup();
				missingCentral.clear();
				missingCentral.addAll(centralStars);
				missingCentral.removeAll(cluster);
				if (minMissingEmpire > missingCentral.size())
					minMissingEmpire = missingCentral.size();
				return missingCentral.isEmpty();
			}
		}
		private final class StarSet extends HashSet<Star> {
			// to simplify the use of list of set
			private static final long serialVersionUID = 1L;
			private StarSet()	{}
			private StarSet(StarSet src)	{ addAll(src); }
			private StarSet(Set<CentralStar> src)	{ addAll(src); }
			private StarSet(List<CentralStar> src)	{ addAll(src); }
		}
		private class Star implements GraphNode {
			final int id;
			final String strId;
			final StarPt pt;
			final StarSet inRangeStars = new StarSet();

			Star(StarPt pt, int id)	{
				this.pt = pt;
				this.id = id;
				strId = Integer.toString(id);
			}
			private Set<String> getIdSet()	{
				Set<String> idSet = new HashSet<>();
				for (Star star: inRangeStars)
					idSet.add(star.getId());
				return idSet;
			}
			boolean isLink()	{return id >= firstLinkId; }
			final void validateDistance(Star star, final float maxSquareDist)	{
				final float dx = pt.x - star.pt.x;
				final float dy = pt.y - star.pt.y;
				if (maxSquareDist > (dx*dx + dy*dy)) {
					inRangeStars.add(star);
					star.inRangeStars.add(this);
				}
			}
			@Override public boolean equals(Object o)	{ return o instanceof Star &&  id == ((Star)o).id; }
			@Override public int hashCode()				{ return id; }
			@Override public String getId()				{ return strId; }
		}
	}
	private final class ShapeRegion {
		int num = 0;
		int size;
		StarPt[] pts;
		public ShapeRegion(final int maxStars) {
			size = maxStars;
			pts = new StarPt[maxStars];
		}
		public boolean isTooNearSystems(StarPt pt, float buffer) {
			for (int i=0; i<num; i++)
				if (starDist(pt, pts[i]) <= buffer)
					return true;
			return false;
		}
		private void addSystem(StarPt pt) {
			if (num == size)
				extendArray ();
			pts[num] = pt;
			num++;
		}
		private void extendArray () { // BR: To resolve overflow!
			size *= 2;
			// System.out.println("extendArray () size change: " + num + " ==> " + size);
			pts = Arrays.copyOf(pts, size);
		}
	}
	public final class EmpireSystem {
		private final StarPt[] starPts = new StarPt[homeStarNum];
		private int num = 0;
		private boolean valid = false;

		private EmpireSystem(GalaxyShape sp) {
			// empire is valid if it can create a valid home system
			// and variable valid nearby stars
			valid = addNewHomeSystem(sp);

			float buffer = sysBuffer;

			int num2 = opts.secondRingSystemNumber();
			int num1 = opts.firstRingSystemNumber();
			float radius1  = opts.firstRingRadius();
			float buffer1 = min(buffer, radius1/sqrt(max(1.4f, num1)));
			float minRel1 = buffer1/radius1;
			minRel1 *= minRel1;
			for (int nbSys=0; nbSys<num1; nbSys++)
				if (looseLimits)
					valid = valid && addNearbySystem(sp, colony(), radius1, buffer1, minRel1);
				else
					valid = valid && addNearbySystem(sp, colony(), radius1, buffer1);

			float radius2 = opts.secondRingRadius();
			float buffer2 = min(buffer, radius2/sqrt(max(1.4f, num2)));
			float minRel2 = buffer2/radius2;
			minRel2 *= minRel2;
			for (int nbSys=num1; nbSys<num2; nbSys++)
				if (looseLimits)
					valid = valid && addNearbySystem(sp, colony(), radius2, buffer2, minRel2);
				else
					valid = valid && addNearbySystem(sp, colony(), radius2, buffer2);
		}
		// BR: for symmetric galaxy
		private EmpireSystem(GalaxyShape sp, StarPt homePt) {
			// create first a valid home system (pt is already validated)
			// then from a second call two valid nearby stars
			addSystem(homePt);
		 	valid = true;
		}
		// BR: for symmetric galaxy
		private boolean addNearbySystemsSym(GalaxyShape sp, StarPt pt, float maxDistance, float buffer, float minRel) {
			// if pt = null then search for a nearby system
			// else the system is already validated... add it
			boolean valid = false;
			if (pt == null) { // player world, search for one
				if (looseLimits)
					valid = addNearbySystem(sp, colony(), maxDistance, buffer, minRel);
				else
					valid = addNearbySystem(sp, colony(), maxDistance, buffer);
				return valid;
			}
			addSystem(pt); // Other empires
			valid = true;
			return valid;
		}
		public int numSystems()	{ return num; }
		public StarPt nearby(int i)	{ return starPts[i]; }
		public StarPt colony()		{ return starPts[0]; }
		private boolean inNebula(Nebula neb)	{
			for (int i=0; i<num; i++)
				if (neb.contains(starPts[i]))
					return true;
			return false;
		}
		private boolean addNewHomeSystem(GalaxyShape sh) {
			int attempts = 0;
			while (attempts++ < 100) {
				StarPt homePt = findSpecificValidLocation(); // modnar: add specific placement of homeworld locations
				if (homePt == null)
					return false;
				if (!sh.isTooNearExistingSystem(homePt, true)) {
					homePt.setAsHomeworld();
					addSystem(homePt);
					return true;
				}
			}
			return false;
		}
		private boolean addNearbySystem(GalaxyShape sh, StarPt homePt, float maxDistance, float buffer) {
			float x1 = homePt.x - maxDistance;
			float x2 = homePt.x + maxDistance;
			float y1 = homePt.y - maxDistance;
			float y2 = homePt.y + maxDistance;
			int attempts = 0;
			StarPt ptNearby = new StarPt();
			while (attempts < 100) {
				attempts++;
				ptNearby.x = randX.nextFloat(x1, x2);
				ptNearby.y = randY.nextFloat(y1, y2);
				if (sh.valid(ptNearby)) {
					boolean tooCloseToAny = isTooNearExistingSystem(sh, ptNearby, buffer);
					boolean tooFarFromRef = starDist(homePt, ptNearby) >= maxDistance;
					if (!tooCloseToAny && !tooFarFromRef) {
						addSystem(ptNearby);
						return true;
					}
				}
			}
			return false;
		}
		private boolean addNearbySystem(GalaxyShape sh, StarPt homePt, float maxDistance, float buffer, float minRel) {
			int attempts = 0;
			while (attempts < 100) {
				attempts++;
				double r = maxDistance * Math.sqrt(randX.nextDouble(minRel, 1));
				double a = randX.nextDouble() * twoPI;
				StarPt ptNearby = new StarPt(homePt.x + r * Math.cos(a), homePt.y + r * Math.sin(a));
				boolean tooCloseToAny = isTooNearExistingSystem(sh, ptNearby, buffer);
				if (!tooCloseToAny) {
					addSystem(ptNearby);
					return true;
				}
			}
			return false;
		}
		private boolean isTooNearExistingSystem(GalaxyShape sh, StarPt pt, float buffer) {
			for (int i=0;i<num;i++)
				if (starDist(pt, starPts[i]) <= buffer)
					return true;
			return sh.isTooNearExistingSystem(pt, buffer);
		}
		private void addSystem(StarPt pt)	{
			starPts[num] = pt;
			num++;
		}
	}

	/**
	 *  integrated the modnar companions world here
	 *  to avoid to spread symmetry management everywhere
	 *  Added some diversity in their angular placement
	 */
	private final class CompanionWorld { // BR:

		final double minRandom = twoPI / 6.0;
		CtrPoint[] cW;

		// ========== constructors ==========
		//
		CompanionWorld(int numComp) {
			cW = new CtrPoint[abs(numComp)];			
		}
		CompanionWorld(EmpireSystem empire, int numComp) {
			this(numComp);
			CtrPoint home = new CtrPoint(empire.colony());
			if (numComp == 0) { return; }
			if (numComp > 0) {
				double ctr   = twoPI / numComp;
				double width = twoPI / numComp - minRandom;
				double orientation = randY.nextDouble(twoPI); // Global orientation
				for (int i=0; i<numComp; i++) {
					cW[i] = home.shift(unit(orientation + randX.sym(i * ctr, width)));
				}
			}
			else { // old way
				numComp = -numComp;
				double ctr = twoPI / 4;
				double orientation = twoPI / 8 ; // Global orientation
				for (int i=0; i<numComp; i++)
					cW[i] = home.shift(unit(orientation - i * ctr));
			}
		}
	   	// ========== Getters ==========
	   	//
		private CompanionWorld[] symmetric() {
			CompanionWorld[] cw = new CompanionWorld[numEmpires];
			double angle = twoPI / numEmpires;
			cw[0] = this;
			for (int i=1; i<numEmpires; i++) {
				cw[i] = rotate(i * angle);
			}
			return cw;
		}
		private CompanionWorld rotate(double angle) {
			CompanionWorld cw = new CompanionWorld(cW.length);
			for (int i=0; i<cW.length; i++) {
				cw.cW[i] = cW[i].rotate(angle);
			}
			return cw;
		}
		private CtrPoint unit(double angle) {
			return new CtrPoint(1.0, 0.0).rotate(angle);
		}
	}
	/**
	 * Quite similar to Point.Float for center referenced point
	 * Not an extended Point class to prevent confusion
	 */
	final class CtrPoint implements Cloneable { // BR:

		// double as all Math operation are performed in double!
		// And this allow to differentiate centered points (double)
		// vs non centered points (float)
		private double x = 0f;
		private double y = 0f;

		// ========== constructors ==========
		//
		CtrPoint() {}
		/**
		 * @param x referenced to center; y=0
		 */
		CtrPoint(double x)	{ this.x = x; }
		/**
		 * @param xc referenced to center
		 * @param yc referenced to center
		 */
		private CtrPoint(double xc, double yc)	{ x = xc; y = yc; }
		/**
		 * @param xe referenced to the edge
		 * @param ye referenced to the edge
		 */
		private CtrPoint(float xe, float ye)	{ x = xe - cx; y = ye - cy; }
		/**
		 * @param ptE referenced to the edge
		 */
		private CtrPoint(StarPt ptE)	{ x = ptE.x - cx; y = ptE.y - cy; }
		/**
		 * @param pt referenced to center
		 */
		private CtrPoint(CtrPoint pt)	{ x = pt.x; y = pt.y; }

		// ========== Getters ==========
		//
		StarPt get()	{ return new StarPt((float)x + cx, (float)y + cy); }
		float getX()	{ return (float) x + cx; }
		float getY()	{ return (float) y + cy; }
		CtrPoint rotate(double angle) {
			return new CtrPoint(Math.cos(angle) * x + Math.sin(angle) * y,
								Math.cos(angle) * y - Math.sin(angle) * x);
		}
		CtrPoint shift(CtrPoint shift) {
			return new CtrPoint(x + shift.x, y + shift.y);
		}
		@Override protected CtrPoint clone()	{ return new CtrPoint(this); }
		// ========== Other Methods ==========
		//
		private CtrPoint[] rotate(double angle, int n) {
			CtrPoint[] result = new CtrPoint[n];
			CtrPoint pt = this;
			for (int i=0; i<n; i++) {
				pt = pt.rotate(angle);
				result[i] = pt;
			}
			return result;
		}
	}
	public class StarPt extends Point.Float	{
		private static final long serialVersionUID = 1L;
		private static final int ORION		= 1;
		private static final int HOMEWORLD	= ORION << 1;
		private static final int CONNECTION	= HOMEWORLD << 1;
		private static final int ROUTE		= CONNECTION << 1;
		private static final int ASTEROID	= ROUTE << 1;

		private int category;

		private StarPt ()	{}
		StarPt (float x, float y)	{ super(x, y); }
		private StarPt (double x, double y)	{ super((float)x, (float)y); }

		private StarPt setAsConnect()	{ category |= CONNECTION;	return this;}
		private StarPt setAsRoute()		{ category |= ROUTE;		return this;}
		private StarPt setAsAsteroid()	{ category |= ASTEROID;		return this;}
		private StarPt setAsHomeworld()	{ category |= HOMEWORLD;	return this;}
		private StarPt setAsOrion()		{ category |= ORION;		return this;}

		public boolean isConnection()	{ return CONNECTION == (CONNECTION & category); }
		public boolean isRoute()		{ return ROUTE == (ROUTE & category); }
		public boolean isAsteroid()		{ return ASTEROID == (ASTEROID & category); }
		public boolean isHomeworld()	{ return HOMEWORLD == (HOMEWORLD & category); }
		public boolean isOrion()		{ return ORION == (ORION & category); }

		protected double distanceTo(Point.Float pt)	{
			final float dx = pt.x - x;
			final float dy = pt.y - y;
			return Math.sqrt(dx*dx + dy*dy);
		}
	}
}
