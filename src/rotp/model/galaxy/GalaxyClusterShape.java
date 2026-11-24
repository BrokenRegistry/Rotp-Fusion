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

// modnar: custom map shape, Cluster
final class GalaxyClusterShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "CLUSTER";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
			static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 0;
	private static ShapeOptionList param1;

	private static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<>(Arrays.asList(
				"SETUP_CLUSTER_0",
				"SETUP_CLUSTER_1",
				"SETUP_CLUSTER_2",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_1);
		}
		return param1;
	}

	private Point.Float cc1, cc2, cc3, cc4, cc5, cc6, cc7, cc8;
	
	GalaxyClusterShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override public IShapeOption<?> paramOption1()	{ return param1(); }
	@Override public List<String> options1()		{ return param1().getOptions(); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 0.95f; }
	@Override protected float minEmpireFactor()		{ return 4f; }
    @Override
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(1.7*4.0/3.0*nominalSystemNumber*adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(1.7*3.0/4.0*nominalSystemNumber*adjustedSizeFactor()));
    }
    @Override
    public void init(int n) {
        super.init(n);

		cc1 = new Point.Float();
		cc2 = new Point.Float();
		cc3 = new Point.Float();
		cc4 = new Point.Float();
		cc5 = new Point.Float();
		cc6 = new Point.Float();
		cc7 = new Point.Float();
		cc8 = new Point.Float();

		// choose cluster locations with options1
		switch(option1) {
            case 0: {
                // 4 distinct clusters
                cc1.x = 0.25f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc1.y = 0.75f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc2.x = 0.38f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc2.y = 0.44f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc3.x = 0.75f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc3.y = 0.25f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc4.x = 0.62f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc4.y = 0.56f*galaxyHeightLY() + galaxyEdgeBuffer();
                
                cc5.x = 0.25f*galaxyWidthLY() + galaxyEdgeBuffer(); // same as cc1
                cc5.y = 0.75f*galaxyHeightLY() + galaxyEdgeBuffer(); // same as cc1
                cc6.x = 0.25f*galaxyWidthLY() + galaxyEdgeBuffer(); // same as cc1
                cc6.y = 0.75f*galaxyHeightLY() + galaxyEdgeBuffer(); // same as cc1
                cc7.x = 0.25f*galaxyWidthLY() + galaxyEdgeBuffer(); // same as cc1
                cc7.y = 0.75f*galaxyHeightLY() + galaxyEdgeBuffer(); // same as cc1
                cc8.x = 0.25f*galaxyWidthLY() + galaxyEdgeBuffer(); // same as cc1
                cc8.y = 0.75f*galaxyHeightLY() + galaxyEdgeBuffer(); // same as cc1
                break;
            }
            case 1: {
                // 6 distinct clusters
                cc1.x = 0.16f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc1.y = 0.52f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc2.x = 0.5f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc2.y = 0.67f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc3.x = 0.5f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc3.y = 0.33f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc4.x = 0.84f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc4.y = 0.48f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc5.x = 0.3f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc5.y = 0.78f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc6.x = 0.7f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc6.y = 0.22f*galaxyHeightLY() + galaxyEdgeBuffer();
                
                cc7.x = 0.18f*galaxyWidthLY() + galaxyEdgeBuffer(); // same as cc1
                cc7.y = 0.5f*galaxyHeightLY() + galaxyEdgeBuffer(); // same as cc1
                cc8.x = 0.18f*galaxyWidthLY() + galaxyEdgeBuffer(); // same as cc1
                cc8.y = 0.5f*galaxyHeightLY() + galaxyEdgeBuffer(); // same as cc1
                break;
            }
            case 2: {
                // 8 distinct clusters
                cc1.x = 0.15f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc1.y = 0.5f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc2.x = 0.3f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc2.y = 0.22f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc3.x = 0.85f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc3.y = 0.5f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc4.x = 0.7f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc4.y = 0.78f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc5.x = 0.45f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc5.y = 0.42f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc6.x = 0.42f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc6.y = 0.74f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc7.x = 0.55f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc7.y = 0.58f*galaxyHeightLY() + galaxyEdgeBuffer();
                cc8.x = 0.58f*galaxyWidthLY() + galaxyEdgeBuffer();
                cc8.y = 0.26f*galaxyHeightLY() + galaxyEdgeBuffer();
                break;
            }
        }

        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();
    }
	@Override protected boolean valid(StarPt p)	{
		double lim = rand.nextDouble();
		// cRadius defines approx. cluster radius
		// using two for variety
		int galaxyHeightLY = galaxyHeightLY();

		// calculate the distances from the random point to ClusterCenters
		double cRadius1 = 0.22 * galaxyHeightLY;
		double lim1 = lim * cRadius1;
		if (lim1 >= p.distanceTo(cc1)) return true;
		if (lim1 >= p.distanceTo(cc2)) return true;
		if (lim1 >= p.distanceTo(cc3)) return true;
		if (lim1 >= p.distanceTo(cc4)) return true;

		double cRadius2 = 0.15 * galaxyHeightLY;
		double lim2 = lim * cRadius2;
		if (lim2 >= p.distanceTo(cc5)) return true;
		if (lim2 >= p.distanceTo(cc6)) return true;
		if (lim2 >= p.distanceTo(cc7)) return true;
		if (lim2 >= p.distanceTo(cc8)) return true;

		return false;
	}
    @Override
    protected float sizeFactor(String size) { return settingsFactor(1.0f); }
}
