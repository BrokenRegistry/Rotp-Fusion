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
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGameOptions;

// modnar: custom map shape, Shuriken
final class GalaxyShurikenShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "SHURIKEN";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
			static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 0;
	private	static final int DEFAULT_OPT_2	= 0;
	private static ShapeOptionList param1;
	private static ShapeOptionList param2;

	private static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<String>(Arrays.asList(
				"SETUP_SHURIKEN_A",
				"SETUP_SHURIKEN_0",
				"SETUP_SHURIKEN_1",
				"SETUP_SHURIKEN_2",
				"SETUP_SHURIKEN_3",
				"SETUP_SHURIKEN_4",
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
				"SETUP_SHURIKEN_ORIGINAL",
				"SETUP_SHURIKEN_ALTERNATIVE",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_2);
		}
		return param2;
	}

	private Path2D flake;
	private Shape flakeROT;
	private Area totalArea, flakeArea;
	private int numPoints = 16;

	GalaxyShurikenShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override protected float minEmpireFactor()		{ return 4f; }
	@Override public IShapeOption<?> paramOption1()	{ return param1(); }
	@Override public IShapeOption<?> paramOption2()	{ return param2(); }
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

		float gE = (float) galaxyEdgeBuffer();
		float gW = (float) galaxyWidthLY();
		float gH = (float) galaxyHeightLY();

		// modnar: choose different number of points for the flake polygon with option1
		switch(option1) {
            case 0:
                numPoints = 7;
                break;
            case 1:
                numPoints = 9;
                break;
            case 2:
                numPoints = 11;
                break;
            case 3:
                numPoints = 13;
                break;
            case 4:
                numPoints = 15;
                break;
            case 5:
                numPoints = 17;
                break;
        }
		
		// Random randnum = new Random();
		flake = new Path2D.Float();
		switch(option2) {
			case 0: // BR: original Modnar solution
				// keep same random number seed
				// modified by numberStarSystems, UI_option, and selectedNumberOpponents
				// rand.setSeed(finalNumberStarSystems*numPoints + opts.selectedNumberOpponents());
				// initial flake polygon start, ensure polygon extent
				flake.moveTo(gE + 0.5f*gW, gE + 0.65f*gH);
				flake.lineTo(gE + 0.4f*gW, gE + 0.02f*gH);
		
				// points to define triangular slice within which to draw flake polygon
				Point.Float p1 = new Point.Float(gE + 0.5f*gW, gE + 0.65f*gH);
				Point.Float p2 = new Point.Float(gE + 0.35f*gW, gE + 0.02f*gH);
				Point.Float p3 = new Point.Float(gE + 0.65f*gW, gE + 0.02f*gH);

				// create flake polygon shape, with random points/path
				for (int i = 0; i < numPoints; i++) {
					// uniform random point within triangle slice
					float rand1 = randX.nextFloat();
					float rand2 = randY.nextFloat();
					float px = p1.x*(1-rand1) + p2.x*((float) Math.sqrt(rand1)*(1-rand2)) + p3.x*((float) Math.sqrt(rand1)*rand2);
					float py = p1.y*(1-rand1) + p2.y*((float) Math.sqrt(rand1)*(1-rand2)) + p3.y*((float) Math.sqrt(rand1)*rand2);
					flake.lineTo(px, py);
		        }
				break;

			case 1:
				// keep same random number seed
				// modified by numberStarSystems, UI_option, and selectedNumberOpponents
				// rand.setSeed(finalNumberStarSystems*numPoints + opts.selectedNumberOpponents());
				// Then same as Case 2
			case 2:
				// initial flake polygon start, ensure polygon extent
				flake.moveTo(gE + 0.5f*gW, gE + 0.02f*gH);
				
				float step	= (float) 1.8f / numPoints;
				int size	= numPoints/2;
				float[] yA	= new float[size];
				float[] xA	= new float[size];
				float ray	= 0.48f;
				float maxX	= 0.15f;
				float minX	= 0.02f;
				float maxR	= 0.1f;
				float adj	= 1f;
				
				for (int i = 0; i < size; i++) {
					// uniform random point within triangle slice
					float rand1 = randX.nextFloat();
					float rand2 = randY.nextFloat();
					ray	  = (float) (ray - rand1*step);
					yA[i] = ray;
					xA[i] = (maxX-minX)*rand2 + minX;
		        }
				if (ray > maxR)
					adj = (0.48f-maxR) / (0.48f-ray);
				for (int i = 0; i < size; i++) {
					yA[i] = (0.5f-yA[i]) * adj * gH;
					flake.lineTo((0.5f + xA[i])*gW, yA[i]);					
				}
				for (int i = size-1; i >= 0; i--) {
					flake.lineTo((0.5f - xA[i])*gW, yA[i]);					
				}
				
				break;
	 	}		
		flake.lineTo(gE + 0.5f*gW, gE + 0.02f*gH);
        flake.closePath();
		
		flakeArea = new Area(flake);
		totalArea = flakeArea;
		
		// rotate flakes and combine together
		for (int i = 1; i < 6; i++)
        {
		AffineTransform rotate = AffineTransform.getRotateInstance(i*Math.PI/3, gE + 0.5f*gW, gE + 0.5f*gH);
		flakeROT = rotate.createTransformedShape(flake);
		
		flakeArea = new Area(flakeROT);
		totalArea.add(flakeArea);
		}
        
        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();
    }
    @Override
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(1.2*finalNumberStarSystems*adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(1.2*finalNumberStarSystems*adjustedSizeFactor()));
    }
    @Override
    public void setSpecific(Point.Float pt) { // modnar: add possibility for specific placement of homeworld/orion locations
        setRandom(pt);
    }
    @Override
    public boolean valid(float x, float y) {
        return totalArea.contains(x, y);
    }
    @Override
    protected float sizeFactor(String size) { return settingsFactor(1.0f); }
}
