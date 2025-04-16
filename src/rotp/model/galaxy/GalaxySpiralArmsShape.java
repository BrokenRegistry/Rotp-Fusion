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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGameOptions;

// modnar: custom map shape, Spiral Arms
public class GalaxySpiralArmsShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private static final String SYMMETRIC	= "SETUP_SPIRALARMS_SYMMETRIC"; 
	private	static final String SHORT_NAME	= "SPIRALARMS";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
	public	static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 3;
	private	static final int DEFAULT_OPT_2	= 0;
	private static ShapeOptionList param1;
	private static ShapeOptionList param2;

	public static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<String>(Arrays.asList(
				"SETUP_SPIRALARMS_0",	// Straight
				"SETUP_SPIRALARMS_1",	// Very Loose
				"SETUP_SPIRALARMS_2",	// Loose
				"SETUP_SPIRALARMS_3",	// Normal
				"SETUP_SPIRALARMS_4",	// Tight
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
				"SETUP_SPIRALARMS_NORMAL",
				SYMMETRIC,
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_2);
		}
		return param2;
	}

    // BR: for symmetric galaxy
    private float numSwirls = 2.0f;
    private float armRadius = 1.5f;
    private double minRandRay = 0.0; // relative limit Stars ray
    private double randomOrientation;
    private float adjust_density = 1.0f;
	// \BR:
	public GalaxySpiralArmsShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }
	// BR: for symmetric galaxy
    private CtrPoint getRandomSymmetric(double minRay) {
		double armRadius   = Math.min(this.armRadius, twoPI * galaxyRay() / numEmpires);
		double swirlRadius = randX.nextDouble();
		double swirlAngle  = numSwirls * swirlRadius * Math.PI;
		CtrPoint arm = new CtrPoint(swirlRadius * galaxyRay()).rotate(swirlAngle);
		double phi = randY.nextDouble(twoPI);
		double radiusSelect = Math.sqrt(randX.nextDouble()) * armRadius * (1 - swirlRadius);
    	return new CtrPoint(radiusSelect).rotate(phi + randomOrientation).shift(arm);
    }
    @Override protected float minEmpireFactor() { return 4f; }
    @Override public CtrPoint getValidRandomSymmetric() {
    	CtrPoint pt = getRandomSymmetric(minRandRay);
		while (!valid(pt.getX(), pt.getY()))
			pt = getRandomSymmetric(minRandRay);
    	return pt;
    }
	@Override public CtrPoint getPlayerSymmetricHomeWorld() {
		double minHomeRay = Math.sqrt(empireBuffer * numEmpires / twoPI / galaxyRay());
		return getRandomSymmetric(minHomeRay);
	}
	@Override public boolean isSymmetric() {
		return opts.selectedGalaxyShapeOption2().equals(SYMMETRIC);
	}
	@Override public boolean isCircularSymmetric() { return isSymmetric(); }

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
        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();

        float gW = (float) galaxyWidthLY();
		numSwirls = 2.0f;
		armRadius = max(1.5f, 0.03f*gW);
		// BR: Moved here: common for Normal and Symmetric
		// Added Straight and very loose
		switch(option1) {
	        case 0: { // straight, no swirls, very large swirl arm width
	            numSwirls = (float) 0.0f;
	            armRadius = (float) max(1.5f, 0.09f*gW);
	            break;
	        }
	        case 1: { // very loose swirls, larger swirl arm width
	            numSwirls = (float) 0.5f;
	            armRadius = (float) max(1.5f, 0.07f*gW);
	            break;
	        }
	        case 2: { // loose swirls, large swirl arm width
	            numSwirls = (float) 1.0f;
	            armRadius = (float) max(1.5f, 0.05f*gW);
	            break;
	        }
	        case 3: { // normal swirls, medium swirl arm width
	            numSwirls = (float) 2.0f;
	            armRadius = (float) max(1.5f, 0.03f*gW);
	            break;
	        }
	        case 4: { // tight swirls, small swirl arm width
	            numSwirls = (float) 3.0f;
	            armRadius = (float) max(1.5f, 0.02f*gW);
	            break;
	        }
	    }

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
        } // \BR:
        initWidthHeight();
    }
    @Override // BR: added adjust_density for the void in symmetric galaxies
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(2.0*opts.numberStarSystems()*adjust_density*adjustedSizeFactor()));
    }
    @Override // BR: added adjust_density for the void in symmetric galaxies
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(2.0*opts.numberStarSystems()*adjust_density*adjustedSizeFactor()));
    }
    @Override
    public void setRandom(Point.Float pt) {
		float gW = (float) galaxyWidthLY();

		// scale up the number of spirals with size of map
		int numSpirals = (int) Math.floor(Math.sqrt(Math.sqrt(opts.numberStarSystems())));
		int numSteps = (int) 50*numSpirals;

		int armSelect  = randY.nextInt(numSpirals);
		int stepSelect = randX.nextInt(numSteps);

		float xArm = (float) (0.5f*gW + galaxyEdgeBuffer() + 0.45f*gW*stepSelect*Math.cos(numSwirls*stepSelect*Math.PI/numSteps + armSelect*2*Math.PI/numSpirals)/numSteps);
		float yArm = (float) (0.5f*gW + galaxyEdgeBuffer() + 0.45f*gW*stepSelect*Math.sin(numSwirls*stepSelect*Math.PI/numSteps + armSelect*2*Math.PI/numSpirals)/numSteps);

		double phi = randY.nextDouble(2 * Math.PI);
		double radiusSelect = Math.sqrt(randX.nextDouble()) * armRadius * (numSteps - stepSelect)/numSteps;

		pt.x = (float) (radiusSelect * Math.cos(phi) + xArm);
        pt.y = (float) (radiusSelect * Math.sin(phi) + yArm);
    }
    @Override
    public void setSpecific(Point.Float pt) { // modnar: add possibility for specific placement of homeworld/orion locations
        setRandom(pt);
    }
    @Override
    protected float sizeFactor(String size) { return settingsFactor(1.0f); }
}
