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

// modnar: custom map shape, Lorenz
public class GalaxyLorenzShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "LORENZ";
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
				"SETUP_LORENZ_0",
				"SETUP_LORENZ_1",
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
				"SETUP_VIEW_0",
				"SETUP_VIEW_1",
				"SETUP_VIEW_2",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_2);
		}
		return param2;
	}

	private double dt1=0.005; // integration time interval;
	private double sigma=10.0, rho=28.0, beta=8.0/3.0; // Lorenz-1 coefficients values
    private double dt2=0.02; // integration time interval;
	private double a=1.5, b=0.5, c=5.0; // Lorenz-2 coefficients values

	public GalaxyLorenzShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override public IShapeOption paramOption1()	{ return param1(); }
	@Override public IShapeOption paramOption2()	{ return param2(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public void setOption2(String value)	{ param2().set(value); }
	@Override public List<String> options1()		{ return param1().getOptions(); }
	@Override public List<String> options2()		{ return param2().getOptions(); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 0.95f; }
	@Override protected float   minEmpireFactor()	{ return 4f; }
    @Override protected boolean allowExtendedPreview()	{ return false; }
    @Override
    public void init(int n) {
        super.init(n);
        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();
    }
    @Override
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(2.0*4.0/3.0*opts.numberStarSystems()*adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(2.0*3.0/4.0*opts.numberStarSystems()*adjustedSizeFactor()));
    }
    @Override
    public void setRandom(Point.Float pt) {
        // choose lorenz function with option1
        switch(option1) {
            case 0: {
                // iterate over the Lorenz-1 attractor function a random
                // number of steps to get a random point on the function.
                double x = 10.0; double y = 10.0; double z = 10.0; //starting point for Lorenz
                int maxsteps = (int) Math.max(1000, Math.ceil(1.5 * maxStars)); // scale number of iterations with stars
                int n = (int) Math.ceil(rand.nextDouble() * maxsteps);
                for (int i = 0; i < n; i++) {
                    double x1 = x + dt1 * sigma*(y-x); 
                    double y1 = y + dt1 * (rho*x - y - x*z); 
                    double z1 = z + dt1 * (x*y - beta*z); 
                    x = x1;
                    y = y1;
                    z = z1;
                }

                float xf = (float) x;
                float yf = (float) y;
                float zf = (float) z;
                // choose lorenz view-point with option2
                switch(option2) {
                    case 0: {
                        pt.x = galaxyEdgeBuffer() + ((xf+22.0f)/44.0f)*galaxyWidthLY()  + randX.sym(-0.5f);
                        pt.y = galaxyEdgeBuffer() + ((yf+30.0f)/60.0f)*galaxyHeightLY() + randY.sym(-0.5f);
                        break;
                    }
                    case 1: {
                        pt.x = galaxyEdgeBuffer() + ((xf+22.0f)/44.0f)*galaxyWidthLY() + randX.sym(-0.5f);
                        pt.y = galaxyEdgeBuffer() + ((zf+0.0f)/55.0f)*galaxyHeightLY() + randY.sym(-0.5f);
                        break;
                    }
                    case 2: {
                        pt.x = galaxyEdgeBuffer() + ((yf+30.0f)/60.0f)*galaxyWidthLY() + randX.sym(-0.5f);
                        pt.y = galaxyEdgeBuffer() + ((zf+0.0f)/55.0f)*galaxyHeightLY() + randY.sym(-0.5f);
                        break;
                    }
                }
                break;
            }
            case 1: {
                // iterate over the Lorenz-2 attractor function a random
                // number of steps to get a random point on the function.
                double x = 0.5; double y = 1.0; double z = 0.5; //starting point for Lorenz-2
                int maxsteps = Math.max(2000, 1 * maxStars); // scale number of iterations with stars
                int n = (int) Math.ceil(rand.nextDouble() * maxsteps);
                for (int i = 0; i < n; i++) {
                    double x1 = x + dt2 * y; 
                    double y1 = y + dt2 * (-a*x - b*y + y*z); 
                    double z1 = z + dt2 * (-c*x*y - x*x + y*y); 
                    x = x1;
                    y = y1;
                    z = z1;
                }

                float xf = (float) x;
                float yf = (float) y;
                float zf = (float) z;

                // choose Lorenz-2 view-point with option2
                switch(option2) {
                    case 0: {
                        pt.x = galaxyEdgeBuffer() + ((xf+2.1f)/4.2f)*galaxyWidthLY()  + randX.symFloat();
                        pt.y = galaxyEdgeBuffer() + ((yf+2.9f)/5.8f)*galaxyHeightLY() + randY.symFloat();
                        break;
                    }
                    case 1: {
                        pt.x = galaxyEdgeBuffer() + ((xf-yf+2.5f)/5.0f)*galaxyWidthLY() + randX.symFloat();
                        pt.y = galaxyEdgeBuffer() + ((zf+5.1f)/7.5f)*galaxyHeightLY()   + randY.symFloat();
                        break;
                    }
                    case 2: {
                        pt.x = galaxyEdgeBuffer() + ((yf+xf+3.8f)/8.0f)*galaxyWidthLY() + randX.symFloat();
                        pt.y = galaxyEdgeBuffer() + ((zf+5.1f)/7.5f)*galaxyHeightLY()   + randY.symFloat();
                        break;
                    }
                }
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
