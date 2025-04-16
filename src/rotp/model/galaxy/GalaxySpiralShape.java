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
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGameOptions;

public class GalaxySpiralShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private static final String SYMMETRIC	= "SETUP_SPIRAL_SYMMETRIC";
	private	static final String SHORT_NAME	= "SPIRAL";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
	public	static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 2;
	private	static final int DEFAULT_OPT_2	= 3;
	private static ShapeOptionList param1;
	private static ShapeOptionList param2;

	public static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<String>(Arrays.asList(
					"SETUP_SPIRAL_2_ARMS",
					"SETUP_SPIRAL_3_ARMS",
					"SETUP_SPIRAL_4_ARMS",
					"SETUP_SPIRAL_5_ARMS",
					"SETUP_SPIRAL_6_ARMS",
					"SETUP_SPIRAL_7_ARMS",
					"SETUP_SPIRAL_8_ARMS",
				SYMMETRIC,
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_1);
		}
		return param1;
	}
	public static ShapeOptionList param2()	{
		if (param2 == null) {
			param2 = new ShapeOptionList(
			BASE_NAME, 2,
			new ArrayList<String>(Arrays.asList(
				"SETUP_SPIRAL_ROTATION_0",
				"SETUP_SPIRAL_ROTATION_1",
				"SETUP_SPIRAL_ROTATION_2",
				"SETUP_SPIRAL_ROTATION_3",
				"SETUP_SPIRAL_ROTATION_4",
				"SETUP_SPIRAL_ROTATION_5",
				"SETUP_SPIRAL_ROTATION_6",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_2);
		}
		return param2;
	}

    public static int numOptions1 = 8;
    public static int numOptions2 = 7;
    float numArms = 8;
    float armOffsetMax = 0.7f;
    float rotationFactor = 0;
    float armSeparationDistance = 2 * (float)Math.PI / numArms;
    Shape circle;
    float adjust_density = 1.0f; //unused // BR: used for symmetric
    // BR: for symmetric galaxy
    private double minRandRay = 0.0; // relative limit Stars ray
    private double randomOrientation;

    float randomX = 0;
    float randomY = 0;

	public GalaxySpiralShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	// BR: for symmetric galaxy
    private CtrPoint getRandomSymmetric(double minRay) {
        double ray = randX.nextDouble(minRay, 1); 
        ray *= ray; // to favor short ray
        double armOffset = randY.sym(armOffsetMax);
        armOffset = (armOffset - armOffsetMax/2) / ray;
        armOffset *= armOffset * Math.signum(armOffset); // to Squeeze the arm
        double angle = armOffset + ray * rotationFactor;
		return new CtrPoint(ray * galaxyRay()).rotate(angle + randomOrientation);
    }
    @Override public CtrPoint getValidRandomSymmetric() {
    	CtrPoint pt = getRandomSymmetric(minRandRay);
		while (!valid(pt.getX(), pt.getY()))
			pt = getRandomSymmetric(minRandRay);
    	return pt;
    }
	@Override public CtrPoint getPlayerSymmetricHomeWorld()	{
		double minHomeRay = Math.sqrt(empireBuffer * numEmpires / twoPI / galaxyRay());
		return getRandomSymmetric(minHomeRay);
	}
	@Override public IShapeOption paramOption1()	{ return param1(); }
	@Override public IShapeOption paramOption2()	{ return param2(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public void setOption2(String value)	{ param2().set(value); }
	@Override public List<String> options1()		{ return param1().getOptions(); }
	@Override public List<String> options2()		{ return param2().getOptions(); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 1.1f; }
    @Override
    public void init(int n) {
        super.init(n);

        numArms = option1+2;
    	// BR: For symmetric galaxy
        if (isSymmetric()) {
        	randomOrientation = rand.nextDouble(twoPI);
        	// a void coming from symmetry depends on number of opponents
         	double minHomeRay = empireBuffer * numEmpires / twoPI;
        	double minRay = systemBuffer() * numEmpires / twoPI;
        	double maxRay = (float) Math.sqrt(maxStars * adjustedSizeFactor())
        							/ 2 - galaxyEdgeBuffer();
        	float adjTmp = (float) (1.0 / (1.0 - minRay*minRay/maxRay/maxRay));
            adjust_density = max(adjust_density, adjTmp);
            double securityFactor = 0.95 * galaxyRay() / minHomeRay;
            if (securityFactor < 1.0f) {
            	adjust_density /= securityFactor * securityFactor;
            }
            adjust_density = max(1f, adjust_density);
            minRandRay = Math.sqrt(minRay / galaxyRay()); 
            numArms = numEmpires;
        } // \BR:

        rotationFactor = option2;
        armSeparationDistance = 2 * (float)Math.PI / numArms;

        circle = new Ellipse2D.Float(0,0,galaxyWidthLY(), galaxyHeightLY());
        initWidthHeight();
    }
    @Override // BR: added adjust_density for the void in symmetric galaxies
    protected int galaxyWidthLY() {
        return (int) (Math.sqrt(adjust_density*maxStars*adjustedSizeFactor()));
    }
    @Override // BR: added adjust_density for the void in symmetric galaxies
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(adjust_density*maxStars*adjustedSizeFactor()));
    }
    @Override
    public void setRandom(Point.Float pt) {
        float buff = galaxyEdgeBuffer();
        float adjW = fullWidth-buff-buff;
        float adjH = fullHeight-buff-buff;

        float dist = randX.nextFloat();
        dist = dist * dist;

        float angle = (float) randY.nextDouble(2*Math.PI);
        float armOffset = rand.nextFloat(armOffsetMax);
        armOffset = (armOffset - armOffsetMax/2)/dist;
        armOffset = armOffset > 0 ? armOffset*armOffset : -1*armOffset*armOffset;

        float rotation = dist * rotationFactor;
        angle = (int)(angle/armSeparationDistance)*armSeparationDistance+armOffset+rotation;

        float rX = (float)(Math.cos(angle)*dist);
        float rY = (float)(Math.sin(angle)*dist);
        pt.x = buff+(adjW*(1+rX)/2);
        pt.y = buff+(adjH*(1+rY)/2);
    }
    @Override
    public void setSpecific(Point.Float pt) { // modnar: add possibility for specific placement of homeworld/orion locations
        setRandom(pt);
    }
    private float dynSizeFactor(String size) {
	    float adjDensity = densitySizeFactor();
		float largeGal = 12f + 12f * (float) Math.log10(maxStars);
		float smallGal = 1.8f * sqrt(maxStars);
		float selected = min(58f, max(4f, min(largeGal, smallGal)));
		return adjDensity * selected;
    }
    @Override protected float sizeFactor(String size) {
        float adj = densitySizeFactor();
        switch (opts.selectedGalaxySize()) {
            case IGameOptions.SIZE_TINY:      return adj*24; 
            case IGameOptions.SIZE_SMALL:     return adj*24; 
            case IGameOptions.SIZE_SMALL2:    return adj*25;
            case IGameOptions.SIZE_MEDIUM:    return adj*26; 
            case IGameOptions.SIZE_MEDIUM2:   return adj*29; 
            case IGameOptions.SIZE_LARGE:     return adj*32; 
            case IGameOptions.SIZE_LARGE2:    return adj*36; 
            case IGameOptions.SIZE_HUGE:      return adj*40; 
            case IGameOptions.SIZE_HUGE2:     return adj*44; 
            case IGameOptions.SIZE_MASSIVE:   return adj*48; 
            case IGameOptions.SIZE_MASSIVE2:  return adj*50; 
            case IGameOptions.SIZE_MASSIVE3:  return adj*52; 
            case IGameOptions.SIZE_MASSIVE4:  return adj*54; 
            case IGameOptions.SIZE_MASSIVE5:  return adj*56; 
            case IGameOptions.SIZE_INSANE:    return adj*58; 
            case IGameOptions.SIZE_LUDICROUS: return adj*58; 
            default:
            	return dynSizeFactor(size); 
        }
    }
}
