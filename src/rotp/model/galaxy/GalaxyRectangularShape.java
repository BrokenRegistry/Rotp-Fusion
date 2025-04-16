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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGameOptions;

public class GalaxyRectangularShape extends GalaxyShape {
	private	static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "RECTANGLE";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
	public	static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 0;
	private	static final int DEFAULT_OPT_2	= 0;
	private static ShapeOptionList param1;
	private static ShapeOptionList param2;

	public static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
					BASE_NAME, 1,
					new ArrayList<String>(Arrays.asList(
						"SETUP_RECTANGLE_0",
						"SETUP_RECTANGLE_1",
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
						"SETUP_VOID_0",
						"SETUP_VOID_1",
						"SETUP_VOID_2",
						"SETUP_VOID_5",
						RANDOM_OPTION
						) ),
					DEFAULT_OPT_2);
		}
		return param2;
	}

	Shape block, circle;
	Area totalArea, blockArea, circleArea;
	float adjust_density = 0.75f; // modnar: adjust stellar density
    float rectangleRatio = 4.0f/3.0f;
    int voids = 0;

	public GalaxyRectangularShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override public IShapeOption paramOption1()	{ return param1(); }
	@Override public IShapeOption paramOption2()	{ return param2(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public void setOption2(String value)	{ param2().set(value); }
	@Override public List<String> options1()		{ return param1().getOptions(); }
	@Override public List<String> options2()		{ return param2().getOptions(); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 0.95f; }
    @Override
    public void init(int n) {
        super.init(n);

        switch(option1) {
            case 0: {
                rectangleRatio = 4.0f/3.0f;
                break;
            }
            case 1: {
                rectangleRatio = 1.0f; // square
                break;
            }
            default: rectangleRatio = 4.0f/3.0f; break;
        }

        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();

		// modnar: choose void configurations with option2
        switch(option2) {
            case 0: {
                // no voids
                adjust_density = 0.75f;
                // reset w/h vars since aspect ratio may have changed
                initWidthHeight();

                float gE = (float) galaxyEdgeBuffer();
                float gW = (float) galaxyWidthLY();
                float gH = (float) galaxyHeightLY();
                
                block = new Rectangle2D.Float(gE, gE, gW, gH);
                blockArea = new Area(block);
                totalArea = blockArea;
                break;
            }
            case 1: {
                // single large central void
                adjust_density = 1.75f;
                // reset w/h vars since aspect ratio may have changed
                initWidthHeight();

                float gE = (float) galaxyEdgeBuffer();
                float gW = (float) galaxyWidthLY();
                float gH = (float) galaxyHeightLY();

                block = new Rectangle2D.Float(gE, gE, gW, gH);
                blockArea = new Area(block);
                totalArea = blockArea;

                circle = new Ellipse2D.Float(gE+0.5f*gW-0.44f*gH, gE+0.06f*gH, 0.88f*gH, 0.88f*gH);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);
                break;
            }
            case 2: {
                // two diagonal voids
                adjust_density = 1.2f;
                // reset w/h vars since aspect ratio may have changed
                initWidthHeight();

                float gE = (float) galaxyEdgeBuffer();
                float gW = (float) galaxyWidthLY();
                float gH = (float) galaxyHeightLY();

                block = new Rectangle2D.Float(gE, gE, gW, gH);
                blockArea = new Area(block);
                totalArea = blockArea;

                circle = new Ellipse2D.Float(gE+0.05f*gW, gE+0.05f*gH, 0.45f*gW, 0.45f*gW);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);

                circle = new Ellipse2D.Float(gE+0.5f*gW, gE+0.95f*gH-0.45f*gW, 0.45f*gW, 0.45f*gW);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);
                break;
            }
            case 3: {
                // five separated voids
                adjust_density = 1.5f;
                // reset w/h vars since aspect ratio may have changed
                initWidthHeight();

                float gE = (float) galaxyEdgeBuffer();
                float gW = (float) galaxyWidthLY();
                float gH = (float) galaxyHeightLY();

                block = new Rectangle2D.Float(gE, gE, gW, gH);
                blockArea = new Area(block);
                totalArea = blockArea;

                circle = new Ellipse2D.Float(gE+0.26f*gW, gE+0.5f*gH-0.24f*gW, 0.48f*gW, 0.48f*gW);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);

                circle = new Ellipse2D.Float(gE+0.05f*gW, gE+0.067f*gH, 0.3f*gH, 0.3f*gH);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);

                circle = new Ellipse2D.Float(gE+0.05f*gW, gE+0.633f*gH, 0.3f*gH, 0.3f*gH);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);

                circle = new Ellipse2D.Float(gE+0.95f*gW-0.3f*gH, gE+0.067f*gH, 0.3f*gH, 0.3f*gH);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);

                circle = new Ellipse2D.Float(gE+0.95f*gW-0.3f*gH, gE+0.633f*gH, 0.3f*gH, 0.3f*gH);
                circleArea = new Area(circle);
                totalArea.subtract(circleArea);
                break;
            }
            default: break;
        }
    }
    @Override
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(adjust_density*rectangleRatio*opts.numberStarSystems()*adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(adjust_density*(1/rectangleRatio)*opts.numberStarSystems()*adjustedSizeFactor()));
    }
    @Override
    public void setSpecific(Point.Float pt) { // modnar: add possibility for specific placement of homeworld/orion locations
        setRandom(pt);
    }
    @Override
    public boolean valid(float x, float y) {
        float buff = galaxyEdgeBuffer();
        if (x > (fullWidth-buff))
            return false;
        if (x < buff)
            return false;
        if (y > (fullHeight-buff))
            return false;
        if (y < buff)
            return false;
        return totalArea.contains(x, y);
    }
    @Override protected float sizeFactor(String size) {
        float adj = densitySizeFactor();
        switch (opts.selectedGalaxySize()) {
        	case IGameOptions.SIZE_MICRO:     return adj*8; 
        	case IGameOptions.SIZE_TINY:      return adj*10; 
            case IGameOptions.SIZE_SMALL:     return adj*15; 
            case IGameOptions.SIZE_SMALL2:    return adj*17;
            case IGameOptions.SIZE_MEDIUM:    return adj*19; 
            case IGameOptions.SIZE_MEDIUM2:   return adj*20; 
            case IGameOptions.SIZE_LARGE:     return adj*21; 
            case IGameOptions.SIZE_LARGE2:    return adj*22; 
            case IGameOptions.SIZE_HUGE:      return adj*23; 
            case IGameOptions.SIZE_HUGE2:     return adj*24; 
            case IGameOptions.SIZE_MASSIVE:   return adj*25; 
            case IGameOptions.SIZE_MASSIVE2:  return adj*26; 
            case IGameOptions.SIZE_MASSIVE3:  return adj*27; 
            case IGameOptions.SIZE_MASSIVE4:  return adj*28; 
            case IGameOptions.SIZE_MASSIVE5:  return adj*29; 
            case IGameOptions.SIZE_INSANE:    return adj*32; 
            case IGameOptions.SIZE_LUDICROUS: return adj*36; 
            default:
            	return settingsFactor(1.0f);
        }
    }
}
