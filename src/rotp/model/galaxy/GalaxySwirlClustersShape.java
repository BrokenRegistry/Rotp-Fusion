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

// modnar: custom map shape, Swirl Clusters
final class GalaxySwirlClustersShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "SWIRLCLUSTERS";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
			static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 0;
	private static ShapeOptionList param1;

	private static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<String>(Arrays.asList(
				"SETUP_SWIRLCLUSTERS_0",
				"SETUP_SWIRLCLUSTERS_1",
				"SETUP_SWIRLCLUSTERS_2",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_1);
		}
		return param1;
	}

	GalaxySwirlClustersShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override public IShapeOption<?> paramOption1()	{ return param1(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public List<String> options1()		{ return param1().getOptions(); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 1.1f; }
	@Override protected float minEmpireFactor()		{ return 4f; }
    @Override
    public void init(int n) {
        super.init(n);
        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();
    }
    @Override
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(1.8*finalNumberStarSystems*adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(1.8*finalNumberStarSystems*adjustedSizeFactor()));
    }
    @Override
    public void setRandom(Point.Float pt) {
		float gW = (float) galaxyWidthLY();

		// modnar: choose swirl size, number of clusters, and cluster radii with options1
		// scale up the swirl size with size of map
		// scale up number of clusters with size of map
		// scale cluster radius with ~numSwirls
		float numSwirls = (float) Math.sqrt(Math.sqrt(finalNumberStarSystems)) - 1;
		int numClusters = (int) Math.floor(Math.sqrt(finalNumberStarSystems)*Math.log(finalNumberStarSystems)/10);
		float clusterR = (float) (numSwirls + 4.0f) / 1.5f;
		float swirlWidth = 0.05f;
		float clusterDelta = 0.0f; // distance to displace cluster

		switch(option1) {
            case 0: {
                // clusters distributed along spiral
                numSwirls = (float) Math.sqrt(Math.sqrt(finalNumberStarSystems)) - 1;
                numClusters = (int) Math.floor(Math.sqrt(finalNumberStarSystems)*Math.log(finalNumberStarSystems)/10);
                clusterR = (float) (numSwirls + 4.0f) / 1.5f;
                clusterDelta = 0.0f;
                break;
            }
            case 1: {
                // clusters spanning across tighter spirals
                numSwirls = (float) Math.sqrt(Math.sqrt(finalNumberStarSystems))*1.5f - 1;
                numClusters = (int) Math.floor(Math.sqrt(finalNumberStarSystems)*Math.log(finalNumberStarSystems)/10);
                clusterR = (float) (numSwirls + 4.0f) / 1.5f;
                clusterDelta = clusterR;
                break;
            }
            case 2: {
                // very tight spiral only, no visible clusters
                numSwirls = (float) (Math.sqrt(finalNumberStarSystems))/3.0f;
                numClusters = 5;
                clusterR = 0.1f;
                clusterDelta = 0.0f;
                break;
            }
        }
        numClusters = Math.max(1, numClusters); // BR: for very small galaxies

		int numSteps = (int) (200*numSwirls*numSwirls);
		// drop a cluster "every" clusterSteps
		// not quite since distance along swirl is not uniform with steps
		int clusterSteps = (int) Math.floor(2*numSteps / (max(1, numClusters-1)));
		int stepSelect = randX.nextInt(2*numSteps)+1;
		// select cluster position non-uniformally
		int clusterRandom = rand.nextInt(numClusters);
		int clusterSelect = (int) Math.floor(Math.sqrt(clusterRandom)*Math.sqrt(numClusters-1)*clusterSteps);

		switch (rand.nextInt(2)) {
            case 0:
                float xSwirl = (float) (0.5f*gW + galaxyEdgeBuffer() + 0.225f*gW*stepSelect*Math.cos(numSwirls*stepSelect*Math.PI/numSteps)/numSteps);
				float ySwirl = (float) (0.5f*gW + galaxyEdgeBuffer() + 0.225f*gW*stepSelect*Math.sin(numSwirls*stepSelect*Math.PI/numSteps)/numSteps);

				double phiSwirl = randY.nextDouble(2 * Math.PI);
				double radiusSwirl = Math.sqrt(randX.nextDouble()) * swirlWidth;

				pt.x = (float) (radiusSwirl * Math.cos(phiSwirl) + xSwirl);
				pt.y = (float) (radiusSwirl * Math.sin(phiSwirl) + ySwirl);

                break;
            case 1:
                float xDelta = (float) (0.225f*gW*clusterSelect*Math.cos(numSwirls*clusterSelect*Math.PI/numSteps)/numSteps);
				float yDelta = (float) (0.225f*gW*clusterSelect*Math.sin(numSwirls*clusterSelect*Math.PI/numSteps)/numSteps);

				float xCluster = (float) (0.5f*gW + galaxyEdgeBuffer() + xDelta);
				float yCluster = (float) (0.5f*gW + galaxyEdgeBuffer() + yDelta);

				float dCluster = (float) Math.sqrt(xDelta*xDelta + yDelta*yDelta);

				// move cluster a distance clusterDelta closer to center, if they are sufficently far away
				if ((dCluster > clusterR) && (clusterDelta > 0.1f)) {
					xCluster = (float) ((clusterDelta/dCluster)*(0.5f*gW + galaxyEdgeBuffer()) + (1.0f - (clusterDelta/dCluster))*(0.5f*gW + galaxyEdgeBuffer() + xDelta));
					yCluster = (float) ((clusterDelta/dCluster)*(0.5f*gW + galaxyEdgeBuffer()) + (1.0f - (clusterDelta/dCluster))*(0.5f*gW + galaxyEdgeBuffer() + yDelta));
				}

				double phiCluster = randY.nextDouble(2 * Math.PI);
				double radiusSelect = Math.sqrt(randX.nextDouble()) * clusterR;

				pt.x = (float) (radiusSelect * Math.cos(phiCluster) + xCluster);
				pt.y = (float) (radiusSelect * Math.sin(phiCluster) + yCluster);

                break;
        }

    }
    @Override
    public void setSpecific(Point.Float pt) { // modnar: add possibility for specific placement of homeworld/orion locations
        setRandom(pt);
    }
    @Override
    public boolean valid(float x, float y) {
        return true;
    }
    @Override
    protected float sizeFactor(String size) { return settingsFactor(1.0f); }
}
