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

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGameOptions;

// modnar: custom map shape, Grid
public class GalaxyGridShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "GRID";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
	public	static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 0;
	private static ShapeOptionList param1;
	
	public static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<String>(Arrays.asList(
				"SETUP_GRID_0",
				"SETUP_GRID_1",
				"SETUP_GRID_2",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_1);
		}
		return param1;
	}

	private float gW, gH, nGrid, clusterR;
	private int nClusters, numSteps, horizontalSteps;
	private ArrayList<Integer> clusterList;

	public GalaxyGridShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override public IShapeOption paramOption1()	{ return param1(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 1.1f; }
	@Override protected float minEmpireFactor()		{ return 4f; }
    @Override
    public void init(int n) {
		int maxClusters;
        super.init(n);
        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();
 
		// choose number of grids, clusters, and cluster radii with option1
		// scale up the number of grid lines with size of map
		// scale up number of clusters with size of map
		// scale cluster radius with ~nGrid		
		switch(option1) {
	        case 2:
	            // fine grid, no clusters
	        	nGrid = (float) Math.max(1,(int)(1.8f*Math.sqrt(Math.sqrt(opts.numberStarSystems()))-1));
	        	maxClusters = (int) ((nGrid+1)*(nGrid+1));
	            nClusters = 0;
	            clusterR  = 0.1f;
	            break;
	        case 1:
	            // rough grid, clusters at all intersections
	        	nGrid = (float) Math.max(1,(int)(Math.sqrt(Math.sqrt(opts.numberStarSystems()))-1));
	        	maxClusters = (int) ((nGrid+1)*(nGrid+1));
	            nClusters = maxClusters;
	            clusterR  = (nGrid + 5.0f) / 2.0f;
	            break;
	        case 0:
	        default:
	            // rough grid, some clusters at intersections
	        	nGrid = (float) Math.max(1,(int)(Math.sqrt(Math.sqrt(opts.numberStarSystems()))-1));
	        	maxClusters = (int) ((nGrid+1)*(nGrid+1));
	            nClusters = (int) min(maxClusters-1, (int)(Math.sqrt(opts.numberStarSystems())/1.7));
	            clusterR = (float) (nGrid + 5.0f) / 2.0f;
	            break;
		}

		gW = (float) galaxyWidthLY() - 2.0f*clusterR;
		gH = (float) galaxyHeightLY() - 2.0f*clusterR;
		gW = Math.max(5, gW); // BR: for very small galaxies
		gH = Math.max(5, gH); // BR: for very small galaxies

		// scale the resolution of the grid with map dimensions and number of grids
		numSteps = (int) (10*(gW+gH)*(nGrid+1));
		horizontalSteps = (int) (10*gW*(nGrid+1));

		// randomly assign clusters at intersections
		// but use map size and number of opponents as seed to ensure same sequence
		// use shuffle list to ensure unique draws
		ArrayList<Integer> tempList = new ArrayList<Integer>();
		for(int i = 0; i < maxClusters; i++){
			tempList.add(i);
		}
		shuffle(tempList, rand);
		clusterList = new ArrayList<Integer>(tempList.subList(0, (int)nClusters));
		// System.out.println("maxClusters = " + maxClusters + "  nClusters = " + nClusters);
    }
    @Override
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(1.5*opts.numberStarSystems()*adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(1.5*opts.numberStarSystems()*adjustedSizeFactor()));
    }
    @Override
    public void setRandom(Point.Float pt) {		
		// switch between populating the grid vs cluster
    	if (option1==2 || rand.nextBoolean()) {
			int stepSelect = rand.nextInt(numSteps);
				// horizontal grids
				if (stepSelect < horizontalSteps) { 
					int gridRow = (int) Math.floor(stepSelect/(10*gW));
					pt.x = (float) (clusterR + galaxyEdgeBuffer() + gW*(stepSelect-gridRow*(10*gW))/(10*gW));
					pt.y = (float) (clusterR + galaxyEdgeBuffer() + gH*(gridRow/nGrid));
				}
				// vertical grids
				else {
					int gridColumn = (int) Math.floor((stepSelect - horizontalSteps)/(10*gH));
					pt.x = (float) (clusterR + galaxyEdgeBuffer() + gW*(gridColumn/nGrid));
					pt.y = (float) (clusterR + galaxyEdgeBuffer() + gH*(stepSelect-horizontalSteps-gridColumn*(10*gH))/(10*gH));
				}
    	} else {
				int clusterSelect = rand.nextInt(nClusters);

				int clusterPos = clusterList.get(clusterSelect);
                int clusterX = (int) (clusterPos % (nGrid+1));
				int clusterY = (int) Math.floor(clusterPos / (nGrid+1));

				float xCluster = (float) ((clusterX/nGrid)*gW + clusterR + galaxyEdgeBuffer());
				float yCluster = (float) ((clusterY/nGrid)*gH + clusterR + galaxyEdgeBuffer());

				double phiCluster = randY.nextDouble(2 * Math.PI);
				double radiusSelect = Math.sqrt(randX.nextDouble()) * clusterR;

				pt.x = (float) (radiusSelect * Math.cos(phiCluster) + xCluster);
				pt.y = (float) (radiusSelect * Math.sin(phiCluster) + yCluster);
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
