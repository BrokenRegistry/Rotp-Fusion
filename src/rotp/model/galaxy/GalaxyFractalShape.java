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

// modnar: custom map shape, Fractal
final class GalaxyFractalShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "FRACTAL";
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
				"SETUP_FRACTAL_0",
				"SETUP_FRACTAL_1",
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
				"SETUP_GALAXY_MAP_OPTION_A",
				"SETUP_GALAXY_MAP_OPTION_B",
				"SETUP_GALAXY_MAP_OPTION_C",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_2);
		}
		return param2;
	}

	private float adjust_density = 2.0f; // modnar: adjust stellar density

	GalaxyFractalShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override public IShapeOption<?> paramOption1()	{ return param1(); }
	@Override public IShapeOption<?> paramOption2()	{ return param2(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public void setOption2(String value)	{ param2().set(value); }
	@Override public List<String> options1()		{ return param1().getOptions(); }
	@Override public List<String> options2()		{ return param2().getOptions(); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 0.95f; }
	@Override protected float	minEmpireFactor()	{ return 4f; }
	@Override protected boolean	allowExtendedPreview()	{ return false; }
    @Override
    public void init(int n) {
        super.init(n);
        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();
    }

    @Override
    protected int galaxyWidthLY() { 
		return (int) (Math.sqrt(adjust_density
				* Math.max(33, finalNumberStarSystems) 
				* adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
		return (int) (Math.sqrt(adjust_density
				* Math.max(33, finalNumberStarSystems)
				* adjustedSizeFactor()));
    }

	// returns the midpoint of point1 and point2
	private static Point.Float midPoint(Point.Float point1, Point.Float point2) {
        return new Point.Float((point1.x + point2.x) / 2.0f, (point1.y + point2.y) / 2.0f);
    }

	// returns the point between point1 and point2, two-third of the way to point2
	private static Point.Float twothirdPoint(Point.Float point1, Point.Float point2) {
        return new Point.Float(point1.x/3.0f + 2.0f*point2.x/3.0f, point1.y/3.0f + 2.0f*point2.y/3.0f);
    }
	
    @Override
    public void setRandom(Point.Float pt) {
        // choose fractal type with options1
		switch(option1) {
            case 0: {
                adjust_density = 3.0f;
                // reset w/h vars since aspect ratio may have changed
                initWidthHeight();
                // choose fractal generation with option2
                switch(option2) {
                    case 0: {
                        // Sierpinski Triangle
                        // set Sierpinski dimensions
                        float triangleWidth = (float) galaxyWidthLY();
                        float triangleHeight = (float) Math.ceil(triangleWidth * Math.sqrt(3.0f/4.0f));

                        // outer Sierpinski triangle vertex points
                        Point.Float p1 = new Point.Float(0.0f, triangleHeight+0.05f*galaxyHeightLY());
                        Point.Float p2 = new Point.Float(triangleWidth/2.0f, 0.0f+0.05f*galaxyHeightLY());
                        Point.Float p3 = new Point.Float(triangleWidth, triangleHeight+0.05f*galaxyHeightLY());

                        // initial start point for chaos game, take middle point with some variation
                        Point.Float pnew = new Point.Float(triangleWidth/2.0f+randX.symFloat(), triangleHeight/2.0f+randY.symFloat());

                        // scale number of iterations with stars
                        int n = (int) Math.ceil(rand.nextDouble() * 1.5 * maxStars);
                        int i = 0;

                        // iterate through chaos game for Sierpinski randomly
                        while (i < n)
                        {
                            switch (rand.nextInt(3)) {
                                case 0:
                                    pnew = midPoint(pnew, p1);
                                    break;
                                case 1:
                                    pnew = midPoint(pnew, p2);
                                    break;
                                case 2:
                                    pnew = midPoint(pnew, p3);
                                    break;
                            }
                            i++;
                        }

                        pt.x = (float) pnew.x + galaxyEdgeBuffer() + randX.sym(0.5f);
                        pt.y = (float) pnew.y + galaxyEdgeBuffer() + randY.sym(0.5f);
                        break;
                    }

                    case 1: {
                        // Sierpinski Carpet
                        // (?) perhaps too "full"? maybe use Vicsek fractal (?)
                        // set Chaos game boundary dimensions
                        float boxWidth = (float) galaxyWidthLY();
                        float boxHeight = (float) galaxyHeightLY();

                        // box vertex points
                        Point.Float p1 = new Point.Float(0.0f, 0.0f);
                        Point.Float p2 = new Point.Float(boxWidth, 0.0f);
                        Point.Float p3 = new Point.Float(boxWidth, boxHeight);
                        Point.Float p4 = new Point.Float(0.0f, boxHeight);
                        Point.Float p5 = new Point.Float(0.5f*boxWidth, 0.0f);
                        Point.Float p6 = new Point.Float(0.0f, 0.5f*boxHeight);
                        Point.Float p7 = new Point.Float(boxWidth, 0.5f*boxHeight);
                        Point.Float p8 = new Point.Float(0.5f*boxWidth, boxHeight);

                        // initial start point for chaos game, take near middle point with some variation
                        Point.Float pnew = new Point.Float(boxWidth/3.0f+randX.sym(0.5f), boxHeight/3.0f+randY.sym(0.5f));

                        // scale number of iterations with stars
                        int n = (int) Math.ceil(rand.nextDouble() * 1.5 * maxStars);
                        int i = 0;
                        // selection vertices
                        int newVertex = 0;

                        // sierpinski carpet chaos game
                        while (i < n)
                        {
                            newVertex = rand.nextInt(8);
                            if (newVertex == 0)
                            {
                            pnew = twothirdPoint(pnew, p1);
                            }
                            else if (newVertex == 1)
                            {
                            pnew = twothirdPoint(pnew, p2);
                            }
                            else if (newVertex == 2)
                            {
                            pnew = twothirdPoint(pnew, p3);
                            }
                            else if (newVertex == 3)
                            {
                            pnew = twothirdPoint(pnew, p4);
                            }
                            else if (newVertex == 4)
                            {
                            pnew = twothirdPoint(pnew, p5);
                            }
                            else if (newVertex == 5)
                            {
                            pnew = twothirdPoint(pnew, p6);
                            }
                            else if (newVertex == 6)
                            {
                            pnew = twothirdPoint(pnew, p7);
                            }
                            else if (newVertex == 7)
                            {
                            pnew = twothirdPoint(pnew, p8);
                            }
                            i++;
                        }

                        pt.x = (float) pnew.x + galaxyEdgeBuffer() + randX.sym(0.1f);
                        pt.y = (float) pnew.y + galaxyEdgeBuffer() + randY.sym(0.1f);
                        break;
                    }

                    case 2: {
                        // Barnsley Fern
                        // scale number of iterations with stars
                        int n = (int) Math.ceil(maxStars + rand.nextDouble() * 1.5 * maxStars);
                        int i = 0;
                        Point.Float pnew = new Point.Float(0.5f, 0.0f);

                        // Barnsley fern, repeated choose one of four update rules at random
                        while (i < n)
                        {
                            Point.Float ptemp = new Point.Float();
                            float r = rand.nextFloat();

                            // stem
                            if (r <= 0.10f)  { // original probability = 0.01f, increase to get more stem connectivity
                                ptemp.x = 0.50f;
                                ptemp.y = 0.16f * pnew.y;
                            }

                            // largest left-hand leaflet
                            else if (r <= 0.15f) { // original probability = 0.08f
                                ptemp.x =  0.20f * pnew.x - 0.26f * pnew.y + 0.400f;
                                ptemp.y =  0.23f * pnew.x + 0.22f * pnew.y - 0.045f;
                            }

                            // largest right-hand leaflet
                            else if (r <= 0.20f) { // original probability = 0.15f
                                ptemp.x = -0.15f * pnew.x + 0.28f * pnew.y + 0.575f;
                                ptemp.y =  0.26f * pnew.x + 0.24f * pnew.y - 0.086f;
                            }

                            // successively smaller leaflets
                            else {
                                ptemp.x =  0.85f * pnew.x + 0.04f * pnew.y + 0.075f;
                                ptemp.y = -0.04f * pnew.x + 0.85f * pnew.y + 0.180f;
                            }
                            pnew = ptemp;
                            i++;
                        }
                        
                        pt.x = (float) ((pnew.x-0.55f)*1.9f+0.55f)*(galaxyWidthLY()) + randX.sym(0.25f);
                        pt.y = (float) (pnew.y+0.02f)*0.95f*(galaxyHeightLY()) + randY.sym(0.25f);
                        break;
                    }
                }
                break;
            }
            case 1: {
                adjust_density = 2.0f;
                // reset w/h vars since aspect ratio may have changed
                initWidthHeight();
                // set Chaos game boundary dimensions
                float boxWidth = (float) galaxyWidthLY();
                float boxHeight = (float) galaxyHeightLY();

                // box vertex points
                Point.Float p1 = new Point.Float(0.0f, 0.0f);
                Point.Float p2 = new Point.Float(boxWidth, 0.0f);
                Point.Float p3 = new Point.Float(boxWidth, boxHeight);
                Point.Float p4 = new Point.Float(0.0f, boxHeight);

                // initial start point for chaos game, take near middle point with some variation
                Point.Float pnew = new Point.Float(boxWidth/2.0f+randX.sym(0.5f), boxHeight/2.0f+randY.sym(0.5f));

                // scale number of iterations with stars
                int n = (int) Math.ceil(rand.nextDouble() * 1.5 * maxStars);
                int i = 0;
                // selection vertices
                int newVertex = 0;
                int oldVertex = 0; int oldVertex2 = 1;

                // iterate through chaos game, with different rules
                // choose with option2
                switch(option2) {
                    case 0: {
                        // currently chosen vertex cannot neighbor the previously chosen vertex if the two previously chosen vertices are the same
                        while (i < n)
                        {
                            newVertex = rand.nextInt(4);
                            if (newVertex == 0 && !(oldVertex==3 && oldVertex2==3) && !(oldVertex==1 && oldVertex2==1))
                            {
                            pnew = midPoint(pnew, p1);
                            oldVertex2 = oldVertex;
                            oldVertex = 0;
                            }
                            else if (newVertex == 1 && !(oldVertex==0 && oldVertex2==0) && !(oldVertex==2 && oldVertex2==2))
                            {
                            pnew = midPoint(pnew, p2);
                            oldVertex2 = oldVertex;
                            oldVertex = 1;
                            }
                            else if (newVertex == 2 && !(oldVertex==1 && oldVertex2==1) && !(oldVertex==3 && oldVertex2==3))
                            {
                            pnew = midPoint(pnew, p3);
                            oldVertex2 = oldVertex;
                            oldVertex = 2;
                            }
                            else if (newVertex == 3 && !(oldVertex==2 && oldVertex2==2) && !(oldVertex==0 && oldVertex2==0))
                            {
                            pnew = midPoint(pnew, p4);
                            oldVertex2 = oldVertex;
                            oldVertex = 3;
                            }
                            i++;
                        }
                        break;
                    }
                    case 1: {
                        // current vertex cannot be the same as the previously chosen vertex
                        while (i < n)
                        {
                            newVertex = rand.nextInt(4);
                            if (newVertex == 0 && oldVertex != 0)
                            {
                            pnew = midPoint(pnew, p1);
                            oldVertex = 0;
                            }
                            else if (newVertex == 1 && oldVertex != 1)
                            {
                            pnew = midPoint(pnew, p2);
                            oldVertex = 1;
                            }
                            else if (newVertex == 2 && oldVertex != 2)
                            {
                            pnew = midPoint(pnew, p3);
                            oldVertex = 2;
                            }
                            else if (newVertex == 3 && oldVertex != 3)
                            {
                            pnew = midPoint(pnew, p4);
                            oldVertex = 3;
                            }
                            i++;
                        }
                        break;
                    }
                    case 2: {
                        // current vertex cannot be one place away (anti-clockwise) from the previously chosen vertex
                        while (i < n)
                        {
                            newVertex = rand.nextInt(4);
                            if (newVertex == 0 && oldVertex != 1)
                            {
                            pnew = midPoint(pnew, p1);
                            oldVertex = 0;
                            }
                            else if (newVertex == 1 && oldVertex != 2)
                            {
                            pnew = midPoint(pnew, p2);
                            oldVertex = 1;
                            }
                            else if (newVertex == 2 && oldVertex != 3)
                            {
                            pnew = midPoint(pnew, p3);
                            oldVertex = 2;
                            }
                            else if (newVertex == 3 && oldVertex != 0)
                            {
                            pnew = midPoint(pnew, p4);
                            oldVertex = 3;
                            }
                            i++;
                        }
                        break;
                    }
                }

                pt.x = (float) pnew.x + galaxyEdgeBuffer() + randX.sym(0.5f);
                pt.y = (float) pnew.y + galaxyEdgeBuffer() + randY.sym(0.5f);
                break;
            }
		}
        initWidthHeight();
    }
    @Override
    public void setSpecific(Point.Float pt) { // modnar: add possibility for specific placement of homeworld/orion locations
        setRandom(pt);
    }
    @Override
    protected float sizeFactor(String size) { return settingsFactor(1.0f); }
}
