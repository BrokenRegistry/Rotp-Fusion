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
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGameOptions;

final class GalaxyEllipticalShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private static final String SYMMETRIC	= "SETUP_ELLIPSE_SYMMETRIC";
	private	static final String SHORT_NAME	= "ELLIPSE";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
			static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 2;
	private	static final int DEFAULT_OPT_2	= 0;
	private static ShapeOptionList param1;
	private static ShapeOptionList param2;

	private static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<String>(Arrays.asList(
				"SETUP_ELLIPSE_0",
				"SETUP_ELLIPSE_1",
				"SETUP_ELLIPSE_2",
				"SETUP_ELLIPSE_3",
				"SETUP_ELLIPSE_4",
				SYMMETRIC,
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_1);
		}
		return param1;
	}
	private static ShapeOptionList param2()	{
		if (param2 == null) {
			param2 = new ShapeOptionList(
			BASE_NAME, 2,
			new ArrayList<String>(Arrays.asList(
				"SETUP_VOID_0",
				"SETUP_VOID_1",
				"SETUP_VOID_2",
				"SETUP_VOID_3",
				"SETUP_VOID_4",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_2);
		}
		return param2;
	}

	private Shape ellipse, hole, orionSpot;
	private Area totalArea, circleArea, holeArea, orionArea;
	private float adjust_density = 1.0f; // modnar: adjust stellar density
	private float ellipseRatio = 2.0f;
	private float voidSize = 0.0f;
	// BR: for symmetric galaxy
	private double minRandRay = 0.0; // relative limit Stars ray
	private double randomOrientation;

	GalaxyEllipticalShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }
    // BR: for symmetric galaxy
    private CtrPoint getRandomSymmetric(double minRay) {
    	double ray   = galaxyRay() * Math.sqrt(randX.nextDouble(minRay, 1));
    	double angle = randY.sym(randomOrientation, twoPI / numEmpires());
    	return new CtrPoint(ray).rotate(angle);
    }
    @Override public CtrPoint getValidRandomSymmetric() {
    	CtrPoint pt = getRandomSymmetric(minRandRay);
		while (!valid(pt.getX(), pt.getY()))
			pt = getRandomSymmetric(minRandRay);
    	return pt;
    }
	@Override public CtrPoint getPlayerSymmetricHomeWorld()	{
		double minHomeRay = Math.pow(empireBuffer * numEmpires() / twoPI / galaxyRay(), 2);
		return getRandomSymmetric(minHomeRay);
	}

	@Override public IShapeOption<?> paramOption1()	{ return param1(); }
	@Override public IShapeOption<?> paramOption2()	{ return param2(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public void setOption2(String value)	{ param2().set(value); }
	@Override public List<String> options1()		{ return param1().getOptions(); }
	@Override public List<String> options2()		{ return param2().getOptions(); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 0.8f; }
    @Override
    public void init(int n) {
        super.init(n);

        switch(option2) {
            case 0: voidSize = 0.0f; break;
            case 1: voidSize = 0.2f; break;
            case 2: voidSize = 0.4f; break;
            case 3: voidSize = 0.6f; break;
            case 4: voidSize = 0.8f; break;
            default: voidSize = 0.0f; break;
        }

        // modnar: account for void size
        adjust_density = 1.0f / (1.0f - voidSize*voidSize);

        switch(option1) { // BR: moved after option2
            case 0: ellipseRatio = 1.0f; break;
            case 1: ellipseRatio = 1.5f; break;
            case 2: ellipseRatio = 2.0f; break;
            case 3: ellipseRatio = 3.0f; break;
            case 4: ellipseRatio = 5.0f; break;
            case 5:
            	ellipseRatio = 1.0f;  // BR: Symmetric
	        	randomOrientation = rand.nextDouble(twoPI);
	        	// a void coming from symmetry depends on number of opponents
	         	double minHomeRay = empireBuffer * numEmpires() / twoPI;
	        	double minRay = systemBuffer() * numEmpires() / twoPI;
	        	double maxRay = (float) Math.sqrt(maxStars * adjustedSizeFactor())
	        							/ 2 - galaxyEdgeBuffer();
	        	float adjTmp = (float) (1.0 / (1.0 - minRay*minRay/maxRay/maxRay));
	            adjust_density = max(adjust_density, adjTmp);
	            double securityFactor = 0.95 * galaxyRay() / minHomeRay;
	            if (securityFactor < 1.0f) {
	            	adjust_density /= securityFactor * securityFactor;
	            }
	            adjust_density = max(1f, adjust_density);
	            minRandRay = Math.pow(Math.max(voidSize, minRay / galaxyRay()), 2); 
	        	break; // \BR
            default: ellipseRatio = 2.0f; break;
        }

        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();

        float gE = (float) galaxyEdgeBuffer();
        float gW = (float) galaxyWidthLY();
        float gH = (float) galaxyHeightLY();

        ellipse = new Ellipse2D.Float(gE,gE,gW,gH);
        circleArea = new Area(ellipse);
        totalArea = circleArea; // modnar: use totalArea for valid(x,y)

        hole = null;
        if (voidSize > 0) {
            float vW = voidSize*gW;
            float vH = voidSize*gH;
            float vX = gE+((gW-vW)/2);
            float vY = gE+((gH-vH)/2);
            hole = new Ellipse2D.Float(vX, vY,vW,vH);
            holeArea = new Area(hole);
            totalArea.subtract(holeArea);
        }

        // modnar: add central orion location for circular, void-4, non-small maps
        float rOrion = 1.0f;
        orionSpot = new Ellipse2D.Float(gE+0.5f*gW-rOrion,gE+0.5f*gH-rOrion,2.0f*rOrion,2.0f*rOrion);
        orionArea = new Area(orionSpot);
        if ((option1 == 0)&&(finalNumberStarSystems>90)&&(voidSize > 0.7f)) {
            totalArea.add(orionArea);
        }

    }
    @Override
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(adjust_density*ellipseRatio*maxStars*adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(adjust_density*(1/ellipseRatio)*maxStars*adjustedSizeFactor()));
    }
    @Override
    public void setSpecific(Point.Float pt) { // modnar: add possibility for specific placement of homeworld/orion locations
        // modnar: setSpecific only for circular, void-4, non-small maps
        if ((option1 == 0)&&(finalNumberStarSystems>90)&&(voidSize > 0.7f)) {
            if (indexWorld == 0) { // orion
                pt.x = galaxyEdgeBuffer()+0.5f*galaxyWidthLY();
                pt.y = galaxyEdgeBuffer()+0.5f*galaxyHeightLY();
            }
            else { // empire homeworlds
                // int numStarts = opts.selectedNumberOpponents()+1;
                // float rStart = 0.45f*galaxyHeightLY();
                // float xStart = rStart * (float)Math.cos(indexWorld*2*Math.PI/numStarts);
                // float yStart = rStart * (float)Math.sin(indexWorld*2*Math.PI/numStarts);
                // pt.x = galaxyEdgeBuffer()+0.5f*galaxyWidthLY()+xStart;
                // pt.y = galaxyEdgeBuffer()+0.5f*galaxyHeightLY()+yStart;
                setRandom(pt); // BR: the other solution was to slow
            }
        }
        else {
            setRandom(pt);
        }
    }
    @Override
    public boolean valid(float x, float y) {
        return totalArea.contains(x, y); // modnar: use totalArea for valid(x,y)
    }
    @Override protected float sizeFactor(String size) {
        float adj = densitySizeFactor();
        switch (opts.selectedGalaxySize()) {
            case IGameOptions.SIZE_TINY:      return adj*8; 
            case IGameOptions.SIZE_SMALL:     return adj*10; 
            case IGameOptions.SIZE_SMALL2:    return adj*12;
            case IGameOptions.SIZE_MEDIUM:    return adj*13; 
            case IGameOptions.SIZE_MEDIUM2:   return adj*14; 
            case IGameOptions.SIZE_LARGE:     return adj*16; 
            case IGameOptions.SIZE_LARGE2:    return adj*18; 
            case IGameOptions.SIZE_HUGE:      return adj*20; 
            case IGameOptions.SIZE_HUGE2:     return adj*22; 
            case IGameOptions.SIZE_MASSIVE:   return adj*24; 
            case IGameOptions.SIZE_MASSIVE2:  return adj*26; 
            case IGameOptions.SIZE_MASSIVE3:  return adj*28; 
            case IGameOptions.SIZE_MASSIVE4:  return adj*30; 
            case IGameOptions.SIZE_MASSIVE5:  return adj*32; 
            case IGameOptions.SIZE_INSANE:    return adj*36; 
            case IGameOptions.SIZE_LUDICROUS: return adj*49; 
            default:
            	return settingsFactor(1.0f);
        }
   }
}
