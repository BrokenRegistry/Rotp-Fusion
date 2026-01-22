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
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGameOptions;

// modnar: custom map shape, Maze
final class GalaxyMazeShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "MAZE";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
			static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 0;
	private static ShapeOptionList param1;

	private static ShapeOptionList param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionList(
			BASE_NAME, 1,
			new ArrayList<String>(Arrays.asList(
				"SETUP_MAZE_0",
				"SETUP_MAZE_1",
				"SETUP_MAZE_2",
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_1);
		}
		return param1;
	}

	private Shape block;
	private Area totalArea, blockArea;

	GalaxyMazeShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override public IShapeOption<?> paramOption1()	{ return param1(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public List<String> options1()		{ return param1().getOptions(); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 0.95f; }

	@Override protected float   minEmpireFactor()	{ return 4f; }
	@Override protected boolean allowExtendedPreview()	{ return false; }
	@Override public void init(int n)	{
		super.init(n);

		float gE = (float) galaxyEdgeBuffer();
		float gW = (float) galaxyWidthLY();
		float gH = (float) galaxyHeightLY();
		int adjust_seed = 1;

		// modnar: choose different mazes (different random initial conditions) with option1
		switch(option1) {
            case 0: {
                adjust_seed = 0;
                break;
            }
            case 1: {
                adjust_seed = 10;
                break;
            }
            case 2: {
                adjust_seed = 20;
                break;
            }
        }
    	for (int i=0; i<adjust_seed; i++) // new way to change the seed
    		rand.nextDouble();

		// determine maze size with numberStarSystems
		int width = (int) Math.max(1, 4*Math.ceil(1.5*Math.log(finalNumberStarSystems)-5));
		int height = (int) Math.max(1, 3*Math.ceil(1.5*Math.log(finalNumberStarSystems)-5));
		float deltaW = (float) gW/width;
		float deltaH = (float) gH/height;
		boolean WALL = false;
		boolean PASSAGE = !WALL;
		boolean[][] map = new boolean[width][height];

		block = new Rectangle2D.Float();
		blockArea = new Area(block);
		totalArea = blockArea;

		LinkedList<int[]> frontiers = new LinkedList<>();
        // Random randnum = new Random();
		// keep same random number seed
		// modified by numberStarSystems, UI_option, and selectedNumberOpponents
		// randnum.setSeed(finalNumberStarSystems*adjust_seed + opts.selectedNumberOpponents());
        int x = randX.nextInt(width);
        int y = randY.nextInt(height);
        frontiers.add(new int[]{x,y,x,y});

		// maze generation with Prim's algorithm
        while ( !frontiers.isEmpty() ){
            int[] f = frontiers.remove( rand.nextInt( frontiers.size() ) );
            x = f[2];
            y = f[3];
            if ( map[x][y] == WALL )
            {
                map[f[0]][f[1]] = map[x][y] = PASSAGE;

				// maze passage to be filled with stars
				block = new Rectangle2D.Float(gE+f[0]*deltaW,gE+f[1]*deltaH,deltaW,deltaH);
				blockArea = new Area(block);
				totalArea.add(blockArea);
				block = new Rectangle2D.Float(gE+x*deltaW,gE+y*deltaH,deltaW,deltaH);
				blockArea = new Area(block);
				totalArea.add(blockArea);

				// maze walls are meaningless for our purposes
                if ( x >= 2 && map[x-2][y] == WALL )
                    frontiers.add( new int[]{x-1,y,x-2,y} );
                if ( y >= 2 && map[x][y-2] == WALL )
                    frontiers.add( new int[]{x,y-1,x,y-2} );
                if ( x < width-2 && map[x+2][y] == WALL )
                    frontiers.add( new int[]{x+1,y,x+2,y} );
                if ( y < height-2 && map[x][y+2] == WALL )
                    frontiers.add( new int[]{x,y+1,x,y+2} );
            }
        }

        // reset w/h vars since aspect ratio may have changed
        initWidthHeight();
    }

    @Override
    protected int galaxyWidthLY() { 
        return (int) (Math.sqrt(0.75*4.0/3.0*finalNumberStarSystems*adjustedSizeFactor()));
    }
    @Override
    protected int galaxyHeightLY() { 
        return (int) (Math.sqrt(0.75*3.0/4.0*finalNumberStarSystems*adjustedSizeFactor()));
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
