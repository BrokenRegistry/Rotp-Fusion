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
import java.util.List;
import java.util.Map;

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
	private static final int   MaxEmpireTentative = 100;
	protected static final String RANDOM_OPTION   = "SETUP_RANDOM_OPTION";
	protected static final String UI_KEY		  = IBaseOptsTools.BASE_UI;
	protected static final String ROOT_NAME		  = "GALAXY_SHAPE_";

	static final double twoPI = Math.PI * 2.0; // BR:
	private static float orionBuffer = 10;
	static float empireBuffer = 8;	
	private float[] x;
	private float[] y;
	private CompanionWorld[] companionWorlds; // BR:
	private int numCompanions;
	private ShapeRegion[][] regions;
	private int regionScale = 16;
	int fullWidth, fullHeight, width, height;
	int maxStars = 0;
	private int num = 0;
	private int homeStars = 0;
	@SuppressWarnings("unused") // kept for debug
	private int genAttempt = 0;
	private boolean usingRegions = false;
	private boolean fullyInit = false;
	List<EmpireSystem> empSystems = new ArrayList<>();
	private Point.Float orionXY;
	IGameOptions opts;
	
	// BR: added for symmetric galaxy
	private static float cx; // Width galaxy center
	private static float cy; // Height galaxy center
	private float sysBuffer = 1.9f;
	int numEmpires;
	private int numOpponents;
	protected Rand randRnd = new Rand(rng().nextLong()); // For random option selection purpose
	protected Rand rand	 = new Rand(randRnd.nextLong()); // For other than location purpose
	protected Rand randX = new Rand(randRnd.nextLong()); // For X and R
	protected Rand randY = new Rand(randRnd.nextLong()); // for Y and Angle
	private long tm0; // for timing computation
	// \BR
	
	private float dynamicGrowth = 1f;
	private int   currentEmpire = 0;
	private int   loopReserve   = 0;
	protected int homeStarNum	= 3;
	
	protected String finalOption1, finalOption2, finalOption3, finalOption4;
	protected int option1, option2, option3, option4;
	protected boolean[] randomizeShapeOptions;
	protected boolean isSymmetric;
	private boolean looseLimits;

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
		init0();
	}
	private void init0() {
		randRnd = new Rand(IGalaxyOptions.galaxyRandSource.get());
		rand	= randRnd;
		randX	= randRnd;
		randY	= randRnd;
		initFinalOption1();
		initFinalOption2();
		initFinalOption3();
		initFinalOption4();

        isSymmetric = (finalOption1 != null && finalOption1.contains("SYMMETRIC"))
        		|| (finalOption2 != null && finalOption2.contains("SYMMETRIC"));
        homeStarNum = opts.secondRingSystemNumber()+1;
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

	// ========== Methods to query shape info ==========
	void registerOptions(Map<String, ListShapeParam> shapesMap)	{
		shapesMap.put(name(), new ListShapeParam(name(), paramList()));
	}
	public ListShapeParam paramList()	{
		ListShapeParam list = new ListShapeParam(name());
		// ListShapeParam ignore null entries
		list.add(paramOption1());
		list.add(paramOption2());
		list.add(paramOption3());
		list.add(paramOption4());
		return list;
	}
	public IShapeOption paramOption1()	{ return null; }
	public IShapeOption paramOption2()	{ return null; }
	public IShapeOption paramOption3()	{ return null; }
	public IShapeOption paramOption4()	{ return null; }

	public void setOption1(String value)	{}
	public void setOption2(String value)	{}
//	public String defaultOption1()	{ return null; }
//	public String defaultOption2()	{ return null; }
	public List<String> options1()	{ return new ArrayList<>(); }
	public List<String> options2()	{ return new ArrayList<>(); }
	public int numOptions1()		{ return options1().size(); }
	public int numOptions2()		{ return options2().size(); }
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

	// ========== abstract and overridable methods ==========
	protected abstract int galaxyWidthLY();
	protected abstract int galaxyHeightLY();
	protected abstract float sizeFactor(String size);
	//	public boolean nebulaeHasStar(float x, float y, float buffer) {
	//		return isTooNearExistingSystem(x, y, buffer);
	//	}
	private void getPointFromRandomStarSystem(Point.Float pt) {
		int numSysPerEmpire	 = empSystems.get(0).numSystems();
		int numEmpireSystems = numEmpires * numSysPerEmpire;
		if (guiOptions().neverNebulaHomeworld()) {
			numEmpireSystems = 0; // Don't even think to start over one!
		}
		int numUnsettledSys	 = numberStarSystems();
		int totalSystems	 = numEmpireSystems + numUnsettledSys;
		int starId = rand.nextInt(totalSystems);
		if (starId < numEmpireSystems) {
			int empireId = starId/numSysPerEmpire;
			int systemId = starId - empireId*numSysPerEmpire;
			EmpireSystem empSys = empSystems.get(empireId);
			pt.x = empSys.x(systemId);
			pt.y = empSys.y(systemId);
		}
		else {
			int systemId = starId - numEmpireSystems;
			coords(systemId, pt);
		}
	}
	protected void setRandom(Point.Float pt) {
        pt.x = galaxyEdgeBuffer() + (fullWidth  - 2*galaxyEdgeBuffer()) * randX.nextFloat();
        pt.y = galaxyEdgeBuffer() + (fullHeight - 2*galaxyEdgeBuffer()) * randY.nextFloat();
	}
	public boolean valid(float x, float y) {
		if (x<0)          return false;
		if (y<0)          return false;
		if (x>fullWidth)  return false;
		if (y>fullHeight) return false;
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
    protected abstract void setSpecific(Point.Float p);
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
	// modnar: add option to start game with additional colonies
	// modnar: these colonies are in addition to number of stars chosen in galaxy
	int totalStarSystems()	{
		return num + homeStars
			+ opts.selectedCompanionWorlds()*(opts.selectedNumberOpponents()+1);
	}
	public List<EmpireSystem> empireSystems()	{ return empSystems; }
	protected float adjustedSizeFactor()	{ // BR: to converge more quickly
		float factor = sizeFactor(opts.selectedGalaxySize());
		float adjFactor = factor * dynamicGrowth;
		return adjFactor;
	}

	protected float systemBuffer() { return opts.systemBuffer(opts.selectedStarDensityOption()); }
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
	public Point.Float getCompanion(int empId, int compId) {
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
    protected double galaxyRay() {
    	return cx - galaxyEdgeBuffer();
    }
	private boolean generateSymmetric(boolean full) {
		int num1 = opts.firstRingSystemNumber();
		int num2 = opts.secondRingSystemNumber();
		genAttempt    = 0;
		dynamicGrowth = 1f;
		loopReserve   = 0;
		// add systems needed for empires
		while (empSystems.size() < numEmpires) { // Empires attempts loop
			if (full)
				fullInit();
			else
				quickInit();
			genAttempt++;
			if (genAttempt >= MaxEmpireTentative)
				return false; // Failed

			empSystems.clear();
			homeStars = 0;
			num = 0;
			// ===== First Orion
			// Center of the galaxy is the only reasonable symmetric option
			orionXY = new Point.Float(cx, cy);
			addSystem(orionXY);
			indexWorld = 1;
			// ===== Then the home world systems
			CtrPoint ptCtr = getPlayerSymmetricHomeWorld();
			Point.Float pt = ptCtr.get();
			if (valid(pt) && !isTooNearExistingSystem(pt.x, pt.y, true)) {
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
			float buffer = systemBuffer();

			float radius = opts.firstRingRadius();
			float minRel = buffer/radius;
			minRel *= minRel;
			for (int nbSys=1; nbSys<=num1; nbSys++) { // variable nearby systems
				// get player nearby system
				if (player.addNearbySystemsSym(this, null, radius, buffer, minRel))
					homeStars++;
				else {
					valid = false;
					break; // Fail... Retry
				}
				// ----- now the opponents
				// get the stars
				opp = getSymmetricSystems(new CtrPoint(player.x[nbSys], player.y[nbSys]));
				//  no test needed, they are valid by symmetry.
				for (int k=0; k<numOpponents; k++) {
			   		empSystems.get(k+1).addNearbySystemsSym(this, opp[k].get(), 0, 0, 0);
			   		homeStars++;
			   	}
			}
				
			radius = opts.secondRingRadius();
			minRel = buffer/radius;
			minRel *= minRel;
			for (int nbSys=num1+1; nbSys<=num2; nbSys++) { // variable nearby systems
				// get player nearby system
				if (player.addNearbySystemsSym(this, null, radius, buffer, minRel))
					homeStars++;
				else {
					valid = false;
					break; // Fail... Retry
				}
				// ----- now the opponents
				// get the stars
				opp = getSymmetricSystems(new CtrPoint(player.x[nbSys], player.y[nbSys]));
				//  no test needed, they are valid by symmetry.
				for (int k=0; k<numOpponents; k++) {
			   		empSystems.get(k+1).addNearbySystemsSym(this, opp[k].get(), radius, buffer, minRel);
			   		homeStars++;
			   	}
			}
			if (!valid)
				continue; // something was wrong!
		} // The empires are set

		// ===== add other systems to fill out galaxy
		int attempts = addUncolonizedSystemsSymmetric();
		companionWorlds = new CompanionWorld(empSystems.get(0), numCompanions).symmetric();
		long tm1 = System.currentTimeMillis();
		log("Galaxy generation: "+(tm1-tm0)+"ms  Regions: " + usingRegions+"  Attempts: ", str(attempts), "  stars:", str(num), "/", str(maxStars));
		return true;
	}
	private int addUncolonizedSystemsSymmetric() {
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
			if (!isTooNearExistingSystem(pt.getX(), pt.getY() ,false)) {
				addSystem(pt.get());
				// Then fill the symmetry
				// get the stars
				oppPt = getSymmetricSystems(pt);
				//  no test needed, they are valid by symmetry.
			   	for (CtrPoint opp : oppPt) {
			   		addSystem(opp.get());
			   	}
			}
		}
		return attempts;
	}
	// \BR
	private boolean valid(Point.Float p)	{ return valid(p.x, p.y); }
	public float maxScaleAdj()				{ return 1.0f; }
	public void coords(int n, Point.Float pt)	{
		if (n == 0) { // BR: Fixed Orion at the wrong place with regions
			if (orionXY == null) // BR: I don't know why this happen!!!
				return;
			pt.x = orionXY.x;
			pt.y = orionXY.y;
			return;
		}
		if (usingRegions) {
			int i = n-1; // BR: Fixed Orion at the wrong place with regions
			for (int a=0;a<regionScale;a++) {
				for (int b=0;b<regionScale;b++) {
					if (i >= regions[a][b].num)
						i -= regions[a][b].num;
					else {
						pt.x = regions[a][b].x[i];
						pt.y = regions[a][b].y[i];
						//log("system: "+n+"  is a:"+a+" b:"+b+"  i:"+i);
						return;
					}
				}
			}
			throw new RuntimeException("Invalid x index requested: "+i);
		}
		else {
			pt.x = x[n];
			pt.y = y[n];
		}
	}
	// BR: ========== Initialization Methods ==========
	//
	public float empireBuffer() { // BR: Made this parameter available for GUI
		float sysBuffer			 = systemBuffer();
		float minMaxEmpireBuffer = opts.numberStarSystems()/(numEmpires*2);
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
			maxStars = opts.numberStarSystems();
		else
			maxStars = min(MaxPreviewSystems, opts.numberStarSystems());
			
		// common symmetric and non symmetric initializer for generation
		numOpponents = max(0, opts.selectedNumberOpponents());
		numEmpires = numOpponents + 1;
		log("Galaxy shape: "+maxStars+ " stars"+ "  regionScale: "+regionScale+"   emps:"+numEmpires);
		tm0 = System.currentTimeMillis();
		empSystems.clear();

		// systemBuffer() is minimum distance between any 2 stars
		sysBuffer    = systemBuffer();
		empireBuffer = empireBuffer();
		// Orion buffer is 50% greater with minimum of 8 ly.
		orionBuffer  = max(4 * sysBuffer, empireBuffer*3/2); // BR: Restored Vanilla values.
		// BR: Player customization
		orionBuffer  = max(sysBuffer, orionBuffer * opts.orionToEmpireModifier());
		
		looseLimits = opts.looseNeighborhood();
	}
	private void fullInit() {
		fullyInit = true;
		init(opts.numberStarSystems());
	}
	private void quickInit() {
		fullyInit = false;
		init(min(MaxPreviewSystems, opts.numberStarSystems()));
	}
	protected void init(int numStars) {
		// System.out.println("========== GalaxyShape.init(): genAttempt = " + genAttempt);
		numOpponents = opts.selectedNumberOpponents();
		numEmpires = numOpponents + 1;
		numCompanions = opts.signedCompanionWorlds();
		num = 0;
		homeStars = 0;
		empSystems.clear();
		maxStars = numStars;

		initWidthHeight();
		float minSize = min(fullWidth, fullHeight);
		usingRegions = (minSize > 100) && !isSymmetric();
		if (usingRegions) {
			regionScale = min(64, (int) (minSize / 6.0));
			regions = new ShapeRegion[regionScale][regionScale];
			// int regionStars = (int) (2.5*maxStars/regionScale); // BR: reserve no more needed
			int regionStars = (int) (0.25*maxStars/regionScale); // BR: enough for dynamic arrays
			regionStars = max(2, regionStars); // BR: should never be a problem... But!
			for (int i=0;i<regionScale;i++) {
				for (int j=0;j<regionScale;j++)
					regions[i][j] = new ShapeRegion(regionStars);
			}
		}
		else {
			x = new float[maxStars];
			y = new float[maxStars];
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
		boolean valid = generateValid(true);
		clean();
		return valid;
	}
	public boolean quickGenerate() {
		boolean valid;
		if (IGalaxyOptions.galaxyRandSource.get() == 0 || !allowExtendedPreview())
			valid = generateValid(false);
		else
			valid = generateValid(true);
		clean();
		return valid;
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
		if (generate(full)) {
			//System.out.println("generateValid(" + full + ") " + genAttempt + " attempts");
			return true;
		}
		// Some issues... Switch to an easy shape
		System.err.println("Failed generateValid(" + full + ") " + genAttempt + " attempts");
		clean();
		IGalaxyOptions.shapeSelection.setFromDefault(false, false);
		if (generate(full)) {
			System.out.println("default generateValid(" + full + ") " + genAttempt + " attempts");
			return true;
		}
		// more issues...
		System.err.println("Failed default generateValid(" + full + ") " + genAttempt + " attempts");
		clean();
		return false;
	}
	private boolean generate(boolean full) {
		init0();
		singleInit(full);
		if (isSymmetric())
			return generateSymmetric(full);

		genAttempt = 0;
		dynamicGrowth = 1f;
		// add systems needed for empires
		while (empSystems.size() < numEmpires) {
			if (full)
				fullInit();
			else
				quickInit();
			genAttempt++;
			if (genAttempt >= MaxEmpireTentative)
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
		int attempts = addUncolonizedSystems();
		addCompanionsWorld();
		long tm1 = System.currentTimeMillis();
		log("Galaxy generation: "+(tm1-tm0)+"ms  Regions: " + usingRegions+"  Attempts: ", str(attempts), "  stars:", str(num), "/", str(maxStars));
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
	private Point.Float addOrion() {
		Point.Float pt = new Point.Float();
		indexWorld = 0; // modnar: explicitly set indexWorld=0 for orion
		findSpecificValidLocation(pt); // modnar: specific placement for orion location
		//findAnyValidLocation(pt);
		addSystem(pt);
		return pt;
	}
	private void addCompanionsWorld() {
		companionWorlds = new CompanionWorld[numEmpires];
		for (int i=0; i<numEmpires; i++) {
			companionWorlds[i] = new CompanionWorld(empSystems.get(i), numCompanions);
		}
	}
	private int addUncolonizedSystems() {
		int maxAttempts = maxStars * 10;

		// we've already generated 3(or more) stars for every empire so reduce their
		// total from the count of remaining stars to create ("too many stars" bug)
		int nonEmpireStars = maxStars - (empSystems.size() * homeStarNum);
		int attempts = 0;
		Point.Float pt = new Point.Float();
		while ((num < nonEmpireStars) && (attempts++ < maxAttempts)) {
			findAnyValidLocation(pt);
			if (!isTooNearExistingSystem(pt.x,pt.y,false))
				addSystem(pt);
		}
		// System.out.println("addUncolonizedSystems(): attempts/maxStars = "
		//                    + (float)attempts/maxStars);
		return attempts;
	}
	private Point.Float findAnyValidLocation(Point.Float p) {
		setRandom(p);
		while (!valid(p))
			setRandom(p);

		return p;
	}
	// modnar: add specific placement of orion/homeworld locations
	private Point.Float findSpecificValidLocation(Point.Float p) {
		setSpecific(p);
		//indexWorld++; // modnar: increment indexWorld for subsequent homeworld locations
		while (!valid(p) && indexWorld++<2000) {
			setSpecific(p);
			// modnar: incrementing indexWorld here to prevent accidental infinite loop of bad locations,
			// but need setSpecific to have some form of repeating modulo cut-off
			//indexWorld++;
		}
		if (!valid(p)) { // to avoid infinite loop
			p.x = cx;
			p.y = cy;
			//System.err.println("Center point generated");
			return null;
		}
		return p;
	}
	private void addSystem(Point.Float pt) { // BR: changed to protected
		addSystem(pt.x, pt.y);
	}
	private void addSystem(float x0, float y0) {
		if (num == 0) { // Orion: already stored in orionXY!
			if(!usingRegions) { // To be safe, but should not be needed!
				x[num] = x0;
				y[num] = y0;
			}
			num++;
			return;
		}
		if (usingRegions) {
			int xRgn = (int) (regionScale*x0/fullWidth);
			int yRgn = (int) (regionScale*y0/fullHeight);
			regions[xRgn][yRgn].addSystem(x0,y0);
			num++;
		}
		else {
			x[num] = x0;
			y[num] = y0;
			num++;
		}
	}
	private boolean isTooNearExistingSystem(float x0, float y0, boolean isHomeworld) {
		if (isHomeworld) {
			if (distance(x0,y0,orionXY.x,orionXY.y) <= orionBuffer)
				return true;
			for (EmpireSystem emp: empSystems) {
				if (distance(x0,y0,emp.colonyX(),emp.colonyY()) <= empireBuffer)
					return true;
			}
		}
		// float buffer = systemBuffer(); // BR: made global
		// not too close to other systems in galaxy
		return isTooNearExistingSystem(x0, y0, sysBuffer);
	}
	private boolean isTooNearExistingSystem(float x0, float y0, float buffer) {
		// float buffer = systemBuffer(); // BR: made global
		// not too close to other systems in galaxy
		if (usingRegions) {
			if (isTooNearSystemsInNeighboringRegions(x0, y0, buffer))
				return true;
		}
		else {
			if (isTooNearSystemsInEntireGalaxy(x0, y0, buffer)) // BR: global
				return true;
		}
		// not too close to other systems in any empire system
		for (EmpireSystem emp: empSystems) {
			for (int i=0;i<emp.num;i++) {
				if (distance(x0,y0,emp.x(i),emp.y(i)) <= buffer) // BR: global
					return true;
			}
		}
		return false;
	}
	private boolean isTooNearSystemsInNeighboringRegions(float x0, float y0, float buffer) {
		int xRgn = (int)(x0*regionScale/fullWidth);
		int yRgn = (int)(y0*regionScale/fullHeight);
		int yMin = max(0,yRgn-1);
		int yMax = min(regionScale-1,yRgn+1);
		int xMin = max(0,xRgn-1);
		int xMax = min(regionScale-1,xRgn+1);

		for (int x1=xMin;x1<=xMax;x1++) {
			for (int y1=yMin;y1<=yMax;y1++) {
				if (regions[x1][y1].isTooNearSystems(x0, y0, buffer))
					return true;
			}
		}
		return false;
	}
	private boolean isTooNearSystemsInEntireGalaxy(float x0, float y0, float buffer) {
		for (int i=0;i<num;i++) {
			if (distance(x0,y0,x[i],y[i]) <= buffer)
				return true;
		}
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
        Point.Float pt	 = new Point.Float();
        getPointFromRandomStarSystem(pt);
        
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
            float x = pt.x;
            float y = pt.y;
            if (!valid(x+w, y))
            	return neb.cancel();
            if (!valid(x+w, y+h))
            	return neb.cancel();
            if (!valid(x, y+h))
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
	@SuppressWarnings("serial")
	private class ShapeRegion implements Serializable {
		int num = 0;
		int size;
		float[] x;
		float[] y;
		public ShapeRegion(int maxStars) {
			size = maxStars;
			x = new float[size];
			y = new float[size];
		}
		public boolean isTooNearSystems(float x0, float y0, float buffer) {
			// float buffer = systemBuffer();
			for (int i=0;i<num;i++) {
				if (distance(x0,y0,x[i],y[i]) <= buffer)
					return true;
			}
			return false;
		}
		private void addSystem(float x0, float y0) {
			if (num == size)
				extendArray ();
			x[num] = x0;
			y[num] = y0;
			num++;
		}
		private void extendArray () { // BR: To resolve overflow!
			size *= 2;
			// System.out.println("extendArray () size change: " + num + " ==> " + size);
			x = Arrays.copyOf(x, size);
			y = Arrays.copyOf(y, size);
		}
	}
	public final class EmpireSystem implements Serializable {
		private static final long serialVersionUID = 1L;
		private final float[] x = new float[homeStarNum];
		private final float[] y = new float[homeStarNum];
		private int num = 0;
		private boolean valid = false;

		private EmpireSystem(GalaxyShape sp) {
			// empire is valid if it can create a valid home system
			// and variable valid nearby stars
			valid = addNewHomeSystem(sp);

			float buffer = systemBuffer();

			int num2 = opts.secondRingSystemNumber();
			int num1 = opts.firstRingSystemNumber();
			float radius = opts.firstRingRadius();
			float minRel = buffer/radius;
			minRel *= minRel;
			for (int nbSys=0; nbSys<num1; nbSys++)
				if (looseLimits)
					valid = valid && addNearbySystem(sp, colonyX(), colonyY(), radius, buffer, minRel);
				else
					valid = valid && addNearbySystem(sp, colonyX(), colonyY(), radius, buffer);

			radius = opts.secondRingRadius();
			minRel = buffer/radius;
			minRel *= minRel;
			for (int nbSys=num1; nbSys<num2; nbSys++)
				if (looseLimits)
					valid = valid && addNearbySystem(sp, colonyX(), colonyY(), radius, buffer, minRel);
				else
					valid = valid && addNearbySystem(sp, colonyX(), colonyY(), radius, buffer);
		}
		// BR: for symmetric galaxy
		private EmpireSystem(GalaxyShape sp, Point.Float pt) {
			// create first a valid home system (pt is already validated)
			// then from a second call two valid nearby stars
			addSystem(pt.x,pt.y);
		 	valid = true;
		}
		// BR: for symmetric galaxy
		private boolean addNearbySystemsSym(GalaxyShape sp, Point.Float pt
							, float maxDistance, float buffer, float minRel) {
			// if pt = null then search for a nearby system
			// else the system is already validated... add it
			boolean valid = false;
			if (pt == null) { // player world, search for one
				if (looseLimits)
					valid = addNearbySystem(sp, colonyX(), colonyY(), maxDistance, buffer, minRel);
				else
					valid = addNearbySystem(sp, colonyX(), colonyY(), maxDistance, buffer);
				return valid;
			}
			addSystem(pt.x,pt.y); // Other empires
			valid = true;
			return valid;
		}
		public int numSystems()	{ return num; }
		public float x(int i)	{ return x[i]; }
		public float y(int i)	{ return y[i]; }
		float colonyX()   { return x[0]; }
		float colonyY()   { return y[0]; }

		private boolean inNebula(Nebula neb)	{
			for (int i=0;i<num;i++) {
				if (neb.contains(x[i], y[i]))
					return true;
			}
			return false;
		}

		private boolean addNewHomeSystem(GalaxyShape sp) {
			int attempts = 0;
			Point.Float pt = new Point.Float();
			while (attempts++ < 100) {
				pt = findSpecificValidLocation(pt); // modnar: add specific placement of homeworld locations
				//findAnyValidLocation(pt);
				if (pt == null)
					pt = new Point.Float();
				else if (!sp.isTooNearExistingSystem(pt.x,pt.y,true)) {
					addSystem(pt.x,pt.y);
					return true;
				}
			}
			return false;
		}
		private boolean addNearbySystem(GalaxyShape sh, float x0, float y0,
										float maxDistance, float buffer) {
			float x1 = x0-maxDistance;
			float x2 = x0+maxDistance;
			float y1 = y0-maxDistance;
			float y2 = y0+maxDistance;
			int attempts = 0;
			Point.Float pt = new Point.Float();
			while (attempts < 100) {
				attempts++;
				pt.x = randX.nextFloat(x1, x2);
				pt.y = randY.nextFloat(y1, y2);
				if (sh.valid(pt)) {
					boolean tooCloseToAny = isTooNearExistingSystem(sh,pt.x,pt.y, buffer);
					boolean tooFarFromRef = distance(x0, y0, pt.x,pt.y) >= maxDistance;
					if (!tooCloseToAny && !tooFarFromRef) {
						addSystem(pt.x,pt.y);
						return true;
					}
				}
			}
			return false;
		}
		private boolean addNearbySystem(GalaxyShape sh, float x0, float y0,
										float maxDistance, float buffer, float minRel) {
			int attempts = 0;
			while (attempts < 100) {
				attempts++;
				double r = maxDistance * Math.sqrt(randX.nextDouble(minRel, 1));
				double a = randX.nextDouble() * twoPI;
				float x = x0 + (float) (r * Math.cos(a));
				float y = y0 + (float) (r * Math.sin(a));
				boolean tooCloseToAny = isTooNearExistingSystem(sh, x, y, buffer);
				if (!tooCloseToAny) {
					addSystem(x, y);
					return true;
				}
			}
			return false;
		}
		private boolean isTooNearExistingSystem(GalaxyShape sh, float x0, float y0, float buffer) {
			for (int i=0;i<num;i++) {
				if (distance(x0,y0,x[i],y[i]) <= buffer)
					return true;
			}
			return sh.isTooNearExistingSystem(x0,y0,false);
		}
		private void addSystem(float x0, float y0) {
			x[num] = x0;
			y[num] = y0;
			num++;
		}
	}

	/**
	 *  integrated the modnar companions world here
	 *  to avoid to spread symmetry management everywhere
	 *  Added some diversity in their angular placement
	 */
	private class CompanionWorld { // BR:

		final double minRandom = twoPI / 6.0;
		CtrPoint[] cW;
		
		// ========== constructors ==========
		//
		CompanionWorld(int numComp) {
			cW = new CtrPoint[abs(numComp)];			
		}
		CompanionWorld(EmpireSystem empire, int numComp) {
			this(numComp);
			CtrPoint home = new CtrPoint(empire.colonyX(), empire.colonyY());
			if (numComp == 0) { return; }
			if (numComp > 0) {
				double ctr   = twoPI / numComp;
				double width = twoPI / numComp - minRandom;
				double orientation = randY.nextDouble(twoPI); // Global orientation
				for (int i=0; i<numComp; i++) {
					cW[i] = home.shift(unit(orientation + randX.sym(i * ctr, width)));
				}
			} else { // old way
				numComp = -numComp;
				double ctr = twoPI / 4;
				double orientation = twoPI / 8 ; // Global orientation
				for (int i=0; i<numComp; i++) {
					cW[i] = home.shift(unit(orientation - i * ctr));
				}			
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
	class CtrPoint implements Cloneable { // BR:

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
	   	CtrPoint(double x) { this.x = x; }
	   	private /**
	   	 * @param xc referenced to center
	   	 * @param yc referenced to center
	   	 */
	   	CtrPoint(double xc, double yc) { x = xc; y = yc; }
	   	private /**
	   	 * @param xe referenced to the edge
	   	 * @param ye referenced to the edge
	   	 */
	   	CtrPoint(float xe, float ye) { x = xe - cx; y = ye - cy; }
		/**
		 * @param pt referenced to center
		 */
		private CtrPoint(CtrPoint pt) { x = pt.x; y = pt.y; }

	   	// ========== Getters ==========
	   	//
	   	private Point.Float get()   { return new Point.Float((float)x + cx, (float)y + cy); }   	
	   	float getX()        { return (float) x + cx; }   	
	   	float getY()        { return (float) y + cy; }   	
		CtrPoint rotate(double angle) {
			return new CtrPoint(Math.cos(angle) * x + Math.sin(angle) * y,
								Math.cos(angle) * y - Math.sin(angle) * x);
		}
		CtrPoint shift(CtrPoint shift) {
			return new CtrPoint(x + shift.x, y + shift.y);
		}
		@Override protected CtrPoint clone() { return new CtrPoint(this); }
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
}
