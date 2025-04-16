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
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGameOptions;

// modnar: custom map shape, Bulls eye
public class GalaxyBullseyeShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "BULLSEYE";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
	public	static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 0;
	private static ShapeOptionList param1;

	public static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<String>(Arrays.asList(
				"SETUP_BULLSEYE_0",
				"SETUP_BULLSEYE_1",
				"SETUP_BULLSEYE_2",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_1);
		}
		return param1;
	}

    Shape circle, square, arc;
	Area totalArea, circleArea, squareArea, arcArea;
    float adjust_density = 2.0f;

	public GalaxyBullseyeShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override public IShapeOption paramOption1()	{ return param1(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 1.0f; }
	@Override protected float	minEmpireFactor()	{ return 4f; }
	@Override protected boolean	allowExtendedPreview()	{ return false; }
    @Override
    public void init(int n) {
        super.init(n);

		// modnar: different bullseye/target configurations with options1
        switch(option1) {
            case 0: { // standard dart board, exclusiveOr
                adjust_density = 2.0f;
                // reset w/h vars since aspect ratio may have changed
                initWidthHeight();

                float gE = (float) galaxyEdgeBuffer();
                //float gW = (float) galaxyWidthLY();
                float gH = (float) galaxyHeightLY();

                // number of arc sections in the dart board
                int nArcs = (int) Math.min(20, Math.ceil(Math.sqrt(opts.numberStarSystems())/4.0f));

                // double ring
                circle = new Ellipse2D.Float(gE,gE,gH,gH);
                circleArea = new Area(circle);
                totalArea = circleArea;
                circle = new Ellipse2D.Float(gE+0.0235f*gH, gE+0.0235f*gH, 0.953f*gH, 0.953f*gH);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);

                // treble ring
                circle = new Ellipse2D.Float(gE+0.1625f*gH, gE+0.1625f*gH, 0.675f*gH, 0.675f*gH);
                circleArea = new Area(circle);
                totalArea.add(circleArea);
                circle = new Ellipse2D.Float(gE+0.1855f*gH, gE+0.1855f*gH, 0.629f*gH, 0.629f*gH);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);

                // arc segments/sections
                for ( int i = 0; i < nArcs; i++ ){
                    float arcStart = (float) (i*360.0f/nArcs + 90.0f/nArcs);
                    float arcExtent = (float) (180.0f/nArcs);
                    arc = new Arc2D.Float(gE, gE, gH, gH, arcStart, arcExtent, Arc2D.PIE);
                    arcArea = new Area(arc);
                    totalArea.exclusiveOr(arcArea);
                }

                // central bullseye
                circle = new Ellipse2D.Float(gE+0.45325f*gH, gE+0.45325f*gH, 0.0935f*gH, 0.0935f*gH);
                circleArea = new Area(circle);
                totalArea.add(circleArea);
                circle = new Ellipse2D.Float(gE+0.4813f*gH, gE+0.4813f*gH, 0.0374f*gH, 0.0374f*gH);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);
                break;
            }
            case 1: { // concentric ring target
                adjust_density = 1.4f;
                // reset w/h vars since aspect ratio may have changed
                initWidthHeight();

                float gE = (float) galaxyEdgeBuffer();
                //float gW = (float) galaxyWidthLY();
                float gH = (float) galaxyHeightLY();

                // number of rings/halos
                int nRings = (int) Math.min(200, Math.floor(Math.sqrt(opts.numberStarSystems())/2.5f));

                // width of each ring/halo, in ly
                float rWidth = 4.0f;

                // create each circular ring/halo
                circle = new Ellipse2D.Float(gE,gE,gH,gH);
                circleArea = new Area(circle);
                totalArea = circleArea;
                circle = new Ellipse2D.Float(gE+rWidth/2, gE+rWidth/2, gH-rWidth, gH-rWidth);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);

                for ( int i = 1; i < nRings; i++ ){
                    float ringD = (float) (1.0f-1.0f*i/nRings);
                    float ringPos = (float) 0.5f*(1.0f-ringD);

                    circle = new Ellipse2D.Float(gE+ringPos*gH, gE+ringPos*gH, ringD*gH, ringD*gH);
                    circleArea = new Area(circle);
                    totalArea.add(circleArea);
                    circle = new Ellipse2D.Float(gE+ringPos*gH+rWidth/2, gE+ringPos*gH+rWidth/2, ringD*gH-rWidth, ringD*gH-rWidth);
                    circleArea = new Area(circle);
                    totalArea.subtract(circleArea);
                }

                // central bulls-eye
                circle = new Ellipse2D.Float(gE+0.5f*gH*(1.0f-0.5f/nRings), gE+0.5f*gH*(1.0f-0.5f/nRings), 0.5f*gH/nRings, 0.5f*gH/nRings);
                circleArea = new Area(circle);
                totalArea.add(circleArea);
                break;
            }
            case 2: { // concentric square target
                adjust_density = 1.2f;
                // reset w/h vars since aspect ratio may have changed
                initWidthHeight();

                float gE = (float) galaxyEdgeBuffer();
                //float gW = (float) galaxyWidthLY();
                float gH = (float) galaxyHeightLY();

                // number of rings/halos
                int nRings = (int) Math.min(200, Math.floor(Math.sqrt(opts.numberStarSystems())/3));

                // width of each ring/halo, in ly
                float rWidth = 3.0f;

                // create each square ring/halo
                square = new Rectangle2D.Float(gE,gE,gH,gH);
                squareArea = new Area(square);
                totalArea = squareArea;
                square = new Rectangle2D.Float(gE+rWidth/2, gE+rWidth/2, gH-rWidth, gH-rWidth);
                squareArea = new Area(square);
                totalArea.subtract(squareArea);

                for ( int i = 1; i < nRings; i++ ){
                    float ringD = (float) (1.0f-1.0f*i/nRings);
                    float ringPos = (float) 0.5f*(1.0f-ringD);

                    square = new Rectangle2D.Float(gE+ringPos*gH, gE+ringPos*gH, ringD*gH, ringD*gH);
                    squareArea = new Area(square);
                    totalArea.add(squareArea);
                    square = new Rectangle2D.Float(gE+ringPos*gH+rWidth/2, gE+ringPos*gH+rWidth/2, ringD*gH-rWidth, ringD*gH-rWidth);
                    squareArea = new Area(square);
                    totalArea.subtract(squareArea);
                }

                // central bulls-eye
                square = new Rectangle2D.Float(gE+0.5f*gH*(1.0f-0.5f/nRings), gE+0.5f*gH*(1.0f-0.5f/nRings), 0.5f*gH/nRings, 0.5f*gH/nRings);
                squareArea = new Area(square);
                totalArea.add(squareArea);
                break;
            }
        }
    }
    @Override
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(adjust_density*opts.numberStarSystems()*adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(adjust_density*opts.numberStarSystems()*adjustedSizeFactor()));
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
