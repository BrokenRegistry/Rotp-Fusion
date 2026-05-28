package rotp.model.combat;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

// Combat Shield as ellipsoid
// Z target must be lower than Z source
// Delta Z should be tune for the best effects...
public final class CombatShield {
	// BR: a lot of "final" thingh, as it may (or may not) improve the performances!?

	private static final float topShield		= 15;
	private static final float alphaImpactMin	= 0.5f;
	private static final float alphaImpactMax	= 0.9f;
	private static final float alphaSpreadMin	= 0.1f*3;
	private static final float alphaSpreadMax	= 0.3f*3;
	private static final float impactRbase		= 0.05f;
	private static final float impactRmin		= 0.05f;
	private static final float impactRmax		= 0.20f;
	private static final float spreadDrEnd		= 0.05f;
	private static final float spreadDrMin		= 0.2f;
	private static final float spreadingRmax	= 0.95f - spreadDrEnd;

	private static final double	PI		= Math.PI;
	private static final double	HALF_PI	= PI/2;
	private static final int FF		= 255;
	public	static final int ABOVE	= 0;
	public	static final int BELLOW	= 1;

	private static final int IMPACTING	= 0;
	private static final int SPREADING	= 1;
	private static final int FADING		= 2;
	private static final int RING_NUM	= 3;

	private final Tint noColor	= new Tint();
	private final int shieldLevel;
	private final float damage, beamForce;

	// Rendering
	private final GradientRing[] gradientRing = new GradientRing[RING_NUM];
	private final int shieldColumns, shieldRows, paintLength;
	private final int perimeter, maxDistance, fullDistance;
	private final Tint[][] paintArray;
	private final double noiseFactor, shieldTransparency;
	private final boolean enveloping;
	private final float flickeringFactor;

	// Locations, size, weapons
	private final Pos targetCenter	= new Pos();
	private final Pos shipSource	= new Pos();
	private final Pos shipImpact[];
	private final Pos impactAdj[];
	private final int spotDiameter;
	private final int holdFramesNum, landUpFramesNum, fadingFramesNum;
	private final int framesNum, beamFramesNum, attacksPerRound;
	private final Color shieldColor, beamColor;

	// Results
	private final Pos shieldImpact[];
	private final Geodesic geodesic;
	private final BufferedImage[][] shieldArray = new BufferedImage[3][];
	private final int shieldOffsetX, shieldOffsetY, centerOffsetX, centerOffsetY;
	private final double[] insideRatio;
	private double meanInsideRatio;
	private BufferedImage shieldBackGround;

	// Other variables
	private boolean neverInitialized = true;

	// Debug
	private boolean showTiming = false;
	
	// = = = = = Parameters management = = = = =
	//
	private BufferedImage[][] shieldArray() {
		if (neverInitialized)
			initShieldArray();
		return shieldArray;
	}
	public double[] insideRatio() {
		if (neverInitialized)
			initShieldArray();
		return insideRatio;
	}
	public double meanInsideRatio() {
		if (neverInitialized)
			initShieldArray();
		return meanInsideRatio;
	}
	public BufferedImage shieldBackGround() {
		if (shieldBackGround == null) {
			shieldBackGround = new BufferedImage(shieldColumns, shieldRows, TYPE_INT_ARGB);
			Graphics2D g = shieldBackGround.createGraphics();
			g.setColor(Color.black);
			g.fillOval(0, 0, shieldColumns, shieldRows);
			g.dispose();
		}
		return shieldBackGround;
	}

	// = = = = = Constructors, Setters and Initializers = = = = =
	//
	public CombatShield(int holdFrames, int landUpFrames, int fadingFrames,
			int boxWidth, int boxHeight, int targetCtrX, int targetCtrY,
			Color shieldColor, boolean enveloping, int border, int transparency, int flickering,
			int shieldNoisePct, int shieldLevel, BufferedImage targetImage, 
			int weaponX, int weaponY, int weaponZ, Color weaponColor, int spotSize,
			int attacksPerRound, float damage, float beamForce) {
		holdFramesNum   = holdFrames;
		landUpFramesNum = landUpFrames;
		fadingFramesNum = fadingFrames;
		beamFramesNum	= 1 + landUpFramesNum;
		framesNum		= beamFramesNum + holdFramesNum + fadingFramesNum;
		this.shieldColor= shieldColor;
		this.enveloping = enveloping;
		this.attacksPerRound = attacksPerRound;
		insideRatio		= new double[attacksPerRound];
		shieldImpact	= new Pos[attacksPerRound];
		shipImpact		= new Pos[attacksPerRound];
		impactAdj		= new Pos[attacksPerRound];
		for (int attack=0; attack < attacksPerRound; attack++) {
			shipImpact[attack]	 = new Pos();
			impactAdj[attack]	 = new Pos();
			shieldImpact[attack] = new Pos();
		}

		targetCenter.set(targetCtrX, targetCtrY, 0);
		this.shieldLevel = shieldLevel; // = design().maxShield
		long timeStart	 = System.currentTimeMillis();
		
		ShipAnalysis sA	 = new ShipAnalysis(targetImage, boxWidth, boxHeight, border);
		
		shieldColumns = (int) (2*sA.b+1);
		shieldRows	  = (int) (2*sA.a+1);
		perimeter	  = (int) sA.ellipsePerimeter();
		maxDistance	  = perimeter/2;
		fullDistance  = (int) (maxDistance*1.2);
		paintLength	  = (int) (perimeter * 1.1); // with a little reserve!

		paintArray = new Tint[framesNum][];
		shieldArray[ABOVE]	= new BufferedImage[framesNum];
		shieldArray[BELLOW]	= new BufferedImage[framesNum];
		for (int frame=0; frame< framesNum; frame++) {
			shieldArray[ABOVE][frame]	= new BufferedImage(shieldColumns, shieldRows, TYPE_INT_ARGB);
			shieldArray[BELLOW][frame]	= new BufferedImage(shieldColumns, shieldRows, TYPE_INT_ARGB);
			paintArray[frame]			= new Tint[paintLength];
		}
		shieldOffsetX = sA.shieldOffsetX();
		shieldOffsetY = sA.shieldOffsetY();
		centerOffsetX = sA.centerOffsetX();
		centerOffsetY = sA.centerOffsetY();
		
		geodesic = new Geodesic(sA.a, sA.b, attacksPerRound);

		noiseFactor = 0.01 * shieldNoisePct;
		shipSource.set(weaponX, weaponY, weaponZ);
		spotDiameter = spotSize;
		beamColor	 = weaponColor;
		this.beamForce = beamForce;
		this.damage = damage;
		flickeringFactor = flickering /100f;
		shieldTransparency	= transparency/100.0;
		if (showTiming)
			System.out.println("shipAnalyzis() Time = " + (System.currentTimeMillis()-timeStart));
	}
	public int[][] setImpact(int[] dx, int[] dy, int[] dz) { // dz is for the source
		int[][] trueImpacts = new int[attacksPerRound][];
		for (int attack=0; attack < attacksPerRound; attack++) {
			impactAdj[attack].set(dx[attack], dy[attack], dz[attack]);
			shipImpact[attack].set(targetCenter.x+dx[attack]+centerOffsetX, targetCenter.y+dy[attack]+centerOffsetY, dz[attack]);	
			trueImpacts[attack] = new int[] {(int)shipImpact[attack].x, (int)shipImpact[attack].y};
		}
		return trueImpacts;
	}
	private void initPaints() {
		float shieldForce = shieldLevel / topShield;
		float beamPowerFactor = 0;
		float absorptionRatio = 0;	   
		if (beamForce > 0) {
			beamPowerFactor = (float) Math.log10(beamForce);
			absorptionRatio = damage>0? 1 - (damage/beamForce):1;
		}
		// Transparencies: Transparent = 0; Opaque = 1
		float impactSpreadFactor = bounds(0, (float)Math.sqrt(beamPowerFactor * absorptionRatio), 1);
		float alphaImpact		 = alphaImpactMin + absorptionRatio * (alphaImpactMax-alphaImpactMin);
		float shieldAlphaSpread	 = alphaSpreadMin + impactSpreadFactor * (alphaSpreadMax-alphaSpreadMin);
		// Impact color based on BeamWeapon
		Tint impactColorStart	= new Tint(beamColor, alphaImpact);
		// Spread color based on Target color
		Tint shieldSpreadColor	= new Tint(shieldColor, shieldAlphaSpread);

		// Rays
		int impactR0Start	= 0;
		int impactR0End		= 0;
		float impactR1Start	= spotDiameter/2; // Plain color Impact
		float impactR1End	= fullDistance * bounds(impactRmin, impactRbase*beamPowerFactor, impactRmax);
		float impactR2Start	= impactR1Start * 1.5f;
		float shieldR0Start	= impactR1Start * 1.f;
		
		float cover = 0.5f;
		float shieldR1End	= impactR1End + fullDistance * spreadDrMin;
		float deltaR1		= shieldR1End - impactR1End;
		float impactR2End	= impactR1End + deltaR1*cover;
		float shieldR0End	= shieldR1End - deltaR1*cover;

		float shieldFactor	= bounds(0, (float)Math.sqrt(shieldForce * absorptionRatio), 1);
		shieldFactor = 0;
		float shieldR2End	= shieldR1End + shieldFactor * (fullDistance*spreadingRmax-shieldR1End);
		float shieldR1Start	= shieldR1End * impactR1Start/impactR1End;
		float shieldR2Start	= shieldR2End * impactR1Start/impactR1End;

		float shieldR3End	= shieldR2End + deltaR1*0.7f; // * cover;
		shieldR3End	= fullDistance/2;
		
		float shieldR3Start	= shieldR3End * impactR1Start/impactR1End;
		if (enveloping)
			shieldR3End = fullDistance;

		// On beam impact
		Tint[]	colors	  = new Tint[]	{impactColorStart, impactColorStart, noColor,      noColor};
		float[]	startRays = new float[]	{impactR0Start,    impactR1Start,   impactR2Start, fullDistance};
		float[]	stopRays  = new float[]	{impactR0End,      impactR1End,     impactR2End,   fullDistance};
		gradientRing[IMPACTING] = new GradientRing(colors, startRays, stopRays, beamFramesNum, flickeringFactor/2, 2);	
		
		// Spreading
		colors    = new Tint[] {noColor, noColor,       shieldSpreadColor, shieldSpreadColor, noColor,       noColor};
		startRays = new float[]{0,       shieldR0Start, shieldR1Start,      shieldR2Start,      shieldR3Start, fullDistance};
		stopRays  = new float[]{0,       shieldR0End/2, shieldR1End,        shieldR2End,        shieldR3End,   fullDistance};
		gradientRing[SPREADING] = new GradientRing(colors, startRays, stopRays, beamFramesNum*4/2, flickeringFactor, 1);
		
		// Fading
		if (fadingFramesNum>0) {
			Tint fullColor	= new Tint(FF,FF,FF,FF);
			float startFade = fullDistance / fadingFramesNum;
			colors    = new Tint[] {noColor, noColor,      fullColor,    fullColor};
			startRays = new float[]{0,       0,            startFade,    fullDistance};
			stopRays  = new float[]{0,       fullDistance, fullDistance, fullDistance};
			gradientRing[FADING] = new GradientRing(colors, startRays, stopRays, fadingFramesNum, 0, 0);
		}
	}
	private void initPaintArray() {
		for (int frame=0; frame<framesNum; frame++) {
			Tint[] paint = gradientRing[IMPACTING].getGradientPaint(paintLength);
			gradientRing[IMPACTING].evolve();
			for (int idx=1; idx<2; idx++) {
				Tint[] tintArr = gradientRing[idx].getGradientPaint(paintLength);
				gradientRing[idx].evolve();
				for (int dist=0; dist<paintLength; dist++)
					paint[dist] = paint[dist].addColor(tintArr[dist]);
			}
			paintArray[frame] = paint;
		}

		// Shield fading
		for (int frame=beamFramesNum+holdFramesNum; frame<framesNum; frame++) {
			Tint[] paint   = paintArray[frame];
			Tint[] tintArr = gradientRing[FADING].getGradientPaint(paintLength);
			gradientRing[FADING].evolve();
			for (int dist=0; dist<paintLength; dist++) {
				paint[dist] = paint[dist].setAlphaMultiplier(tintArr[dist]);
			}
		}
	}
	private void initShieldArray() {
		long timeStart = System.currentTimeMillis();
		long timeMid = timeStart;
		neverInitialized = false;
		
		initPaints();
		initPaintArray();
		if (showTiming)
			System.out.println("init Rings and PAints Array Time = " + (System.currentTimeMillis()-timeMid));
		timeMid = System.currentTimeMillis();

		findShieldImpact(shipImpact, impactAdj, shieldImpact);
		geodesic.setImpact(shieldImpact);
		geodesic.drawShieldArray(noiseFactor);
		
		if (showTiming)
			System.out.println("initShieldArray() Time = " + (System.currentTimeMillis()-timeStart));
	}

	// = = = = = getters = = = = =
	//
	public BufferedImage[][] getShieldArray()	{ return  shieldArray(); }
	public int shieldOffsetX()	{ return shieldOffsetX; }
	public int shieldOffsetY()	{ return shieldOffsetY; }
	public int centerOffsetX()	{ return centerOffsetX; }
	public int centerOffsetY()	{ return centerOffsetY; }
	public int getA()			{ return (int) geodesic.a; }
	public int getB()			{ return (int) geodesic.b; }
	public Rectangle shieldRec(){
		return new Rectangle(
				(int) (targetCenter.x+centerOffsetX-shieldColumns/2),
				(int) (targetCenter.y+centerOffsetY-shieldRows/2),
				shieldColumns, shieldRows);
	}

	// = = = = = Tools = = = = =
	//
	private void findShieldImpact(Pos[] shipImpact, Pos[] impactAdj, Pos[] unused) {
		double sum=0;
		for (int attack=0; attack < attacksPerRound; attack++) {
			insideRatio[attack] = findShieldImpact(shipImpact[attack], impactAdj[attack], shieldImpact[attack]);
			sum += insideRatio[attack];
		}
		meanInsideRatio = sum/attacksPerRound;
	}
	private double findShieldImpact(Pos shipImpact, Pos impactAdj, Pos shieldImpact) {
		// -> Center the ellipsoid on point = [0,0,0]
		// Surface equation: x*x/a/a + y*y/b/b + z*z/c/c = 1  
		// New target
		double xo = impactAdj.x;
		double yo = impactAdj.y;
		double zo = 0;
		// - beam path
		double dx = shipSource.x - shipImpact.x;
		double dy = shipSource.y - shipImpact.y;
		double dz = shipSource.z - shipImpact.z; // Should be positive
		// beam equation: [xo, yo, zo] + m * [dx, dy, dz] = [x, y, z] with m>0
		// Intersection point (z always positive)
		// (xo + m∙dx)^2 /a +  (yo + m∙dy)^2 /b +  (zo + m∙dz)^2 /c = 1
		//
		double dxN = dx/geodesic.bb;
		double dyN = dy/geodesic.aa;
		double dzN = dz/geodesic.aa;
		double k1 = 2*(xo*dxN + yo*dyN + zo*dzN);
		double k2 = 2*(dx*dxN + dy*dyN + dz*dzN);
		double k3 = xo*xo/geodesic.bb + (yo*yo+zo*zo)/geodesic.aa;
		double sr = Math.sqrt(k1*k1 - 2*(k3-1)*k2);
		double m1 = (-sr-k1)/k2;
		double m2 = (sr-k1)/k2;
		double insideRatio = m2>m1 ? m2 : m1;
		shieldImpact.x = impactAdj.x + insideRatio * dx;
		shieldImpact.y = impactAdj.y + insideRatio * dy;
		shieldImpact.z = impactAdj.z + insideRatio * dz;
		return insideRatio;
	}
	private float bounds(float low, float val, float hi) {
		return Math.min(Math.max(low, val), hi);
	}
	private int bounds(int low, int val, int hi) {
		return Math.min(Math.max(low, val), hi);
	}

	// = = = = = Nested Classes = = = = =
	//
	private final class ShipAnalysis {
		private final BufferedImage img;
		private final int imgW, imgH;
		private final double centerHeight;
		private final double[] dySqMax;

		private int ctrRow, ctrCol, colMin, colMax;
		private double a, b, aa, bb;
		private double shipRatio, boxRatio;
		private double leftSqMax, rightSqMax, distSqMax;
		
		private static final int alphaLim = 32;

		private void minimize(double margin, boolean show) {
			double maxAdjust	= Math.sqrt(distSqMax); // Min Product
			a = a * maxAdjust * margin;
			b = b * maxAdjust * margin;
			aa = a*a;
			bb = b*b;
			if (show)
				System.out.println("minimize");
			testSize(show);
		}
		private ShipAnalysis(BufferedImage shipImg, int boxWidth, int boxHeight, int border) {
			img		= shipImg;
			imgW	= img.getWidth();
			imgH	= img.getHeight();
			dySqMax	= new double[imgW];

			// Locate vertical position (relative to ship image)
			centerHeight = getVerticalCenter();
			// Find border square distances
			squareDistance(boxWidth, boxHeight);
			// Get first overview
			a(boxHeight/2.0);
			b(boxWidth/2.0);
			ctrCol = (colMax + colMin)/2;
			
			boolean show = false;
			
			// Set the ship ratio (increase size)
			double ratioAdjust = shipRatio/boxRatio;
			if (ratioAdjust>1) // extend b
				b (a * shipRatio);
			else
				a (b / shipRatio);
			testSize(show);

			// Centering and resize first pass
			center(show);
			minimize(1.02, show);
			// Centering and resize second pass
			center(show);
			minimize(1.0, show);
			
			// Add Borders
			a(a+2*border);
			b(b+2*border);
		}
		private void a(double val) {
			a  = val;
			aa = a*a;
		}
		private void b(double val) {
			b  = val;
			bb = b*b;
		}
		private void testSize(boolean show) {
			leftSqMax  = 0;
			rightSqMax = 0;
			for (int x=colMin; x<ctrCol; x++)
				leftSqMax = Math.max(leftSqMax, dySqMax[x]/aa + (x-ctrCol)*(x-ctrCol)/bb);
			for (int x=ctrCol; x<colMax; x++)
				rightSqMax = Math.max(rightSqMax, dySqMax[x]/aa + (x-ctrCol)*(x-ctrCol)/bb);
			distSqMax = Math.max(rightSqMax, leftSqMax);
			if (show) {
				System.out.println("leftSqMax = " + leftSqMax);
				System.out.println("rightSqMax = " + rightSqMax);
				System.out.println("distSqMax = " + distSqMax);			
			}
		}
		private void center(boolean show) {
			if (show)
				System.out.println("Center");
			int corr = (int) Math.abs(b * (leftSqMax-rightSqMax)/2);
			while (corr > 0) {
				if (rightSqMax > leftSqMax)
					ctrCol+=corr;
				else
					ctrCol-=corr;
				ctrCol = Math.max(0,Math.min(ctrCol, imgW));
				testSize(show);
				corr = (int) Math.abs(b * (leftSqMax-rightSqMax)/2);
			}
		}
		private void squareDistance(int boxWidth, int boxHeight) {
			colMin = -1;
			colMax = 0;
			double dySq = 0;
			for (int col=0; col<imgW; col++) {
				dySqMax[col] = 0;
				for(int row=0; row<=ctrRow; row++) {
					int color = img.getRGB(col, row);
					int alpha = color >> 24 & FF;
					if (alpha > alphaLim) {
						double sqMax = Math.pow(row-centerHeight, 2);
						if (sqMax > dySq)
							dySq = sqMax;
						dySqMax[col] = sqMax;
						if (colMin == -1)
							colMin = col;
						colMax = col;
						break;
					}
				}
			}
			int shipWidth	 = colMax-colMin+1;
			int shipHeight	 = (int) Math.round(2 * Math.sqrt(dySq));
			shipRatio = shipWidth / (double)shipHeight;
			boxRatio  = boxWidth / (double)boxHeight;			
		}
		private double getVerticalCenter() {
			int ctrCol = imgW/2;
			ctrRow = 0;
			int rowMin = -1;
			for(int row=0; row<imgH; row++) {
				int color = img.getRGB(ctrCol, row);
				int alpha = color >> 24 & FF;
				if (alpha > alphaLim) {
					ctrRow = row;
					if (rowMin == -1)
						rowMin = row;
				}
			}
			if (ctrRow==0) { // if the ship is too off-centered
				ctrRow = imgH/2;
				rowMin = ctrRow;
			}
			int vSize = ctrRow-rowMin+1;
			double centerHeight = ctrRow - (vSize-1)/2.0;
			ctrRow -= vSize/2;
			return centerHeight;
		}
		private double ellipsePerimeter() {
			// Approximative Ramanujan's formulas
			return Math.round(PI*(3*(a + b) - Math.sqrt((3*a+b)*(a+3*b))));
		}
		private int shieldOffsetX() { return (int)(ctrCol-b); }
		private int shieldOffsetY() { return (int)(ctrRow-a); }
		private int centerOffsetX() { return (int)(ctrCol-imgW/2); }
		private int centerOffsetY() { return (int)(ctrRow-imgH/2); }
	}
	private final class Tint {
		//private final int[] tint;
		private int trans = FF; // Transparency = 1 - alpha
		private int red   = 0;
		private int green = 0;
		private int blue  = 0;
		
		private Tint()				{ }
		private Tint(Tint source)	{ set(source); }
		private Tint(int... source)	{ set(source); }
		private Tint(Color source)	{ set(source); }
		private Tint(int source)	{ set(source); }
		private Tint(Color source, double alpha) {
			set(source);
			saturate((int)(alpha * FF));
		}

		@Override protected Tint clone() {return new Tint(this); }
		@Override public String toString() { 
			return "A=" + (FF-trans) +
					" R=" + red +
					" G=" + green +
					" B=" + blue +
					" T=" + trans;
		}
		// Getters
		private int argb(double transparencyFactor)	{ // most intensively used
			if (trans==FF)
				return 0;
			return ((FF-(int)(trans*transparencyFactor) << 24) 
					| (red << 16) | (green << 8) | blue);
		}
		// Setters
		private Tint set(Tint color) {
			trans = color.trans;
			red = color.red;
			green = color.green;
			blue = color.blue;
			return this;
		}
		private Tint set(int... color) {
			trans = FF-color[0];
			red	  = color[1];
			green = color[2];
			blue  = color[3];
			return this;
		}
		private Tint set(int color)	{
			trans = FF-((color >> 24) & FF);
			red   = (color >> 16) & FF; 
			green = (color >> 8) & FF;
			blue  = color & 0xff;
			return this;
		}		
		private Tint set(Color source) {
			set(source.getRGB());
			return this;
		}
		// Operations
		private Tint addColor(Tint color) {
			if (trans == FF)
				return set(color);
			if (color.trans == FF)
				return this;
			int ct = color.trans;
			trans = trans * ct / FF;
			int ca = FF-color.trans;
			red   = (red   * ct + color.red   * ca) / FF;
			green = (green * ct + color.green * ca) / FF;
			blue  = (blue  * ct + color.blue  * ca) / FF;
			return this;
		}
		private Tint saturate() { // new int[]
			int max = Math.max(red, Math.max(green, blue));
			if (max == 0) {
				red = FF;
				green = FF;
				blue = FF;
			}
			else {
				float factor = 255f/max;
				red *= factor;
				green *= factor;
				blue *= factor;
			}
			return this;
		}
		private Tint saturate(int alpha) {
			trans = FF-alpha;
			return saturate();
		}
		private Tint setAlphaMultiplier(double mult) {
			trans = (int) (FF-mult*(FF-trans));
			return this;
		}
		private Tint setAlphaMultiplier(Tint mult) {
			trans = FF-(FF-mult.trans)*(FF-trans)/FF;
			return this;
		}
		private Tint minus(Tint sub) {
			Tint diff = new Tint();
			diff.trans = trans - sub.trans;
			diff.red   = red   - sub.red;
			diff.green = green - sub.green;
			diff.blue  = blue  - sub.blue;
			return diff;
		}
		private Tint extend(Tint diff, int mult, float div) {
			Tint mix = new Tint();
			mix.trans = bounds(0, trans + (int) (diff.trans * mult / div), FF);
			mix.red   = bounds(0, red   + (int) (diff.red   * mult / div), FF);
			mix.green = bounds(0, green + (int) (diff.green * mult / div), FF);
			mix.blue  = bounds(0, blue  + (int) (diff.blue  * mult / div), FF);
			return mix.saturate();
		}
	}
	private final class Geodesic {
		// Lambert's formula for long lines
		// Ellipsoid parameters
		private final double[] beta;
		private final double[][] sqSinPcosQx2;
		private final double[][] sqCosPsinQx2;
		private final double a, b;		// a = The circle radius, b the third semi-axis
		private final double f;		// flattening
		private final double half_f;
		private final double aa, bb; 
		private final double aabb;
		private final double iaa, ibb;
		private final double b_a, aa_bb;	// b/a

		// Impact parameters
		private final double[] xI, yI, zI;	// the impact point
		private final double[] xI_bb, yI_aa, zI_aa;	// the impact point
		private final double[] betaI;	// reduced latitude
		
		private Geodesic(double a, double b, int attacksPerRound) {
			this.a	= a;
			this.b	= b;
			xI = new double[attacksPerRound];
			yI = new double[attacksPerRound];
			zI = new double[attacksPerRound];
			xI_bb = new double[attacksPerRound];
			yI_aa = new double[attacksPerRound];
			zI_aa = new double[attacksPerRound];
			betaI = new double[attacksPerRound];
			f		= (a-b)/(double)a;
			half_f	= f/2;
			b_a		= b/(double)a;
			aa_bb	= 1/(b_a*b_a);
			aa	= a*a;
			bb	= b*b;
			aabb = (long)aa*bb;
			iaa = 1/(double)aa;
			ibb = 1/(double)bb;
			sqSinPcosQx2 = new double[attacksPerRound][shieldColumns];
			sqCosPsinQx2 = new double[attacksPerRound][shieldColumns];
			// Fill the spheric correction
			beta = new double[shieldColumns];
			for (int i=0; i<shieldColumns; i++) {
				beta[i] = reducedLatitude(i-b);
			}
		}
		@Override public String toString() { 
			return "x=" + xI + " y=" + yI + " z=" + zI;
		}

		private void setImpact(Pos[] shieldImpact) {
			for (int attack=0; attack < attacksPerRound; attack++) {
				setImpact(shieldImpact[attack], attack);
			}
		}
		private void setImpact(Pos shieldImpact, int attack) {
			xI[attack] = shieldImpact.x;
			yI[attack] = shieldImpact.y;
			zI[attack] = shieldImpact.z;
			xI_bb[attack] = xI[attack]*ibb;
			yI_aa[attack] = yI[attack]*iaa;
			zI_aa[attack] = zI[attack]*iaa;
			betaI[attack] = beta[(int) Math.round(xI[attack]+b)];
			for (int i=0; i<shieldColumns; i++) {
				double betaP = reducedLatitude(i-b);
				double cosP = Math.cos((betaI[attack] + betaP)/2);
				double cos2P = cosP*cosP;
				double sin2P = 1-cos2P;
				double cosQ = Math.cos((betaI[attack] - betaP)/2);
				double cos2Q = cosQ*cosQ;
				double sin2Q = 1-cos2Q;
				sqSinPcosQx2[attack][i] = 2*sin2P*cos2Q;
				sqCosPsinQx2[attack][i] = 2*cos2P*sin2Q;
			}
		}
		private boolean insideEllipse(double x, double y) {
			return (x*x*aa + y*y*bb) <= aabb;
		}
		private double reducedLatitude(double x) { // z in earth geodesic, but x for us!
			// tan(β)=(1-f)tan(Φ) with f = (a-b)/a = 1-b/a
			// tan(β)= b/a * tan(Φ)
			// Known: x = b*sin(Φ) ==> sin(Φ) = x/b;
			// tan(β)= b/a * tan(asin(x/b))
			// Known: tan(asin(α)) = α/sqrt(1-a^2)
			// tan(β)=b/a * x/b / sqrt(1-x^2/b^2)
			// tan(β)=x/a / sqrt(1-x^2/b^2)
			// tan(β)=x*b/a / sqrt(b^2-x^2)
			
			double delta = bb - x*x;
			if (delta==0)
				if (x>0)
					return HALF_PI;
				else
					return -HALF_PI;
			else
				return Math.atan(b_a*x/Math.sqrt(delta));
		}
		private void drawShieldArray(double noiseFactor) {
			long timeStart = System.currentTimeMillis();
			double y = -a;
			for (int row=0; row<shieldRows; row++) {
				double x = -b;
				for (int column=0; column<shieldColumns; column++) {
					if (geodesic.insideEllipse(x, y)) {
						// Color on the shield
						// x = b*cos(θ) = b*sin(Φ)
						// y = a*cos(λ)*sin(θ) = a*cos(λ)*cos(Φ)
						// z = a*sin(λ)*sin(θ) = a*sin(λ)*cos(Φ)
						// set point
						double z = Math.sqrt(aa - y*y - x*x*aa_bb);
						// Transparency factor: Close to the border, transparency = 0
						// ==> z=0 ==> transparency = 0; alpha = 1
						double transparencyFactor =  Math.pow(z/a, 0.25);
						// Spherical central angle Δσ
						// Δσ = arcCos(sin(θ1)sin(θ2) + cos(θ1)cos(θ2)cos(λ1-λ2))
						// sin(θ1)sin(θ2) = (x1*x2)/b^2
						// cos(θ1)cos(θ2)cos(λ1-λ2) = cos(θ1)cos(θ2)cos(λ1)cos(λ2) + cos(θ1)cos(θ2)sin(λ1)sin(λ2)
						// cos(θ1)cos(θ2)cos(λ1-λ2) = (y1*y2+z1*z2)/a^2
						// Δσ = arcCos(x1*x2/b^2 + (y1*y2+z1*z2)/a^2)
						double distNFRatio	= 1+noiseFactor*(Math.random()-0.5);
						int aboveDist  = Integer.MAX_VALUE;
						int bellowDist = Integer.MAX_VALUE;

						for (int attack=0; attack < attacksPerRound; attack++) {
							double xPN = x*xI_bb[attack] + y*yI_aa[attack];
							double zzN = z*zI_aa[attack];
							// Above the ship
							double cosΔσ = xPN + zzN;
							if (cosΔσ>1)
								cosΔσ = 1;
							else if (cosΔσ<-1)
								cosΔσ = -1;
							double Δσ = Math.acos(cosΔσ);
							
							// Added 1.01 factor to avoid div per 0! (should be 0/0)
							int dist = (int) (distNFRatio*a*(Δσ-half_f*( 2*Δσ + Math.sin(Δσ) *
									(sqCosPsinQx2[attack][column]/(1.01-cosΔσ) - sqSinPcosQx2[attack][column]/(1.01+cosΔσ)) )));
							if (dist < aboveDist)
								aboveDist = dist;
							// Bellow the ship
							cosΔσ = xPN -zzN;
							if (cosΔσ>1)
								cosΔσ = 1;
							else if (cosΔσ<-1)
								cosΔσ = -1;
							Δσ = Math.acos(cosΔσ);
							dist = (int) (distNFRatio*a*(Δσ-half_f*( 2*Δσ + Math.sin(Δσ) *
									(sqCosPsinQx2[attack][column]/(1.01-cosΔσ) - sqSinPcosQx2[attack][column]/(1.01+cosΔσ)) )));
							if (dist < bellowDist)
								bellowDist = dist;
						}
						for (int frame=0; frame<framesNum; frame++) {
							int aboveColor  = paintArray[frame][aboveDist].argb(transparencyFactor);
							if (aboveColor!=0)
								shieldArray[ABOVE][frame].setRGB(column, row, aboveColor);
							int bellowColor = paintArray[frame][bellowDist].argb(transparencyFactor);
							if (bellowColor!=0)
								shieldArray[BELLOW][frame].setRGB(column, row, bellowColor);
						}
					}
					x++;
				}
				y++;
			}
			if (showTiming)
				System.out.println("- Full drawShieldArray() Time = " + (System.currentTimeMillis()-timeStart));		
		}
	}
	private final class Pos {
		private double x, y, z;
		
		private Pos() {}
		private Pos(int x, int y, int z) { set(x, y, z); }
		private Pos(int[] src) { set(src); }
		private Pos set(int[] src) {
			x = src[0];
			y = src[1];
			z = src[2];
			return this;
		}
		private Pos set(double X, double Y, double Z) {
			x = X;
			y = Y;
			z = Z;
			return this;
		}
		@Override public String toString() { return "x=" + x + " y=" + y + " z=" + z; }
	}
	private final class GradientRing {
		private final Tint[]  colors;
		private final Tint[]  deltaColor;	// Evolution along distances
		private final float[] rays;
		private final float[] raysEvol;		// Evolution from a frame to the next one
		private final float scintillation;	// Evolution from a frame to the next one
		private int count = 0;
		// private float[] factors = new float[]{1, 0.7f, 0f, 0.7f};
		
		private GradientRing(Tint[] colors, float[] startRays, float[] stopRays,
				int numEvol, float scintillationFactor, int countInit) {
			count = countInit;
			rays  = startRays.clone();
			for (int idx=0; idx<rays.length; idx++)
				if(rays[idx]<0)
					rays[idx]=0;
			scintillation = scintillationFactor;
			this.colors	  = new Tint[colors.length];
			for (int idx=0; idx<colors.length; idx++) 
				this.colors[idx] = new Tint(colors[idx]);

			deltaColor	= new Tint[colors.length-1];
			for (int idx=0; idx<deltaColor.length; idx++) 
				deltaColor[idx] = colors[idx+1].minus(colors[idx]);

			raysEvol	= new float[startRays.length];
			for (int idx=0; idx<raysEvol.length; idx++) {
				raysEvol[idx] = (stopRays[idx]-startRays[idx]) / numEvol;
				if(raysEvol[idx]<0)
					raysEvol[idx]=0;
			}
		}
		private void evolve() {
			count++;
			for (int idx=0; idx<rays.length; idx++)
				rays[idx] += raysEvol[idx];
		}
		private Tint[] getGradientPaint(int length) {
			Tint[] paint = new Tint[length];
			int rayStart=0, dist=0;
			for (int idx=0; idx<deltaColor.length; idx++) { // loop thru rings
				Tint colorStart	= new Tint(colors[idx]);
				Tint colorDelta	= deltaColor[idx];
				rayStart		= (int) (0.5 + rays[idx]);
				int	 rayStop	= Math.min(length, (int) (0.5 + rays[idx+1]));
				int	 rayDelta	= rayStop - rayStart;
				for (dist=0; dist<rayDelta; dist++) { // loop thru distances
					int distance = rayStart+dist;
					double opacityFactor = spreadingFactor(distance);
					float scintMult = 1-scintillation;
					if (count%2 == 0)
						opacityFactor*=scintMult;
					paint[distance] = colorStart.extend(colorDelta, dist+1, rayDelta+1)
												.setAlphaMultiplier(opacityFactor);
				}
			}
			for (int pos=rayStart+dist; pos<length; pos++) {
				if (paint[pos] == null)
					paint[pos] = new Tint();
			}
			return paint;
		}
		private double spreadingFactor(int distance) {
			int dist = distance;
			if (dist>maxDistance)
				dist = perimeter-dist;
			if (dist<spotDiameter)
				return 1;
			// transparency increase as the beam spread away
			// then decrease after 1/4 turn
			// Opacity factor
			// ~ beamRay/(distance)
			return Math.pow(spotDiameter/(double)dist, shieldTransparency); // 0.25
		}
	}
}
