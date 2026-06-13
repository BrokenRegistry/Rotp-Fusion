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
package rotp.model.combat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rotp.util.Base;

public final class FlightPath implements Base {
    public static final int mapW = 12;
    public static int[] basePathPriority = { -mapW, -mapW+1, 1, mapW+1, mapW, mapW-1, -1, -mapW-1 };
    public static int[] nwPathPriority =   { -mapW-1, -mapW, -1, -mapW+1, mapW-1, 1, mapW, mapW+1 };
    public static int[] nPathPriority =    { -mapW, -mapW-1, -mapW+1, -1, 1, mapW-1, mapW+1, mapW };
    public static int[] nePathPriority =   { -mapW+1, -mapW, 1, -mapW-1, mapW+1, -1, mapW, mapW-1 };
    public static int[] ePathPriority =    { 1, -mapW+1, mapW+1, -mapW, mapW, -mapW-1, mapW-1, -1 };
    public static int[] sePathPriority =   { mapW+1, 1, mapW, -mapW+1, mapW-1, -mapW, -1, -mapW-1 };
    public static int[] sPathPriority =    { mapW, mapW+1, mapW-1, 1, -1, -mapW+1, -mapW-1, -mapW };
    public static int[] swPathPriority =   { mapW-1, mapW, -1, mapW+1, -mapW-1, 1, -mapW, -mapW+1 };
    public static int[] wPathPriority =    { -1, -mapW-1, mapW-1, -mapW, mapW, -mapW+1, mapW+1, 1 };

    private List<Integer> points;
    private final int gridW;
    private int straightness = -1;
    private float sortValue = -1;
	private boolean repulsive = false;

    //public List<Integer> points() { return points; }
    //public void add(int pt)       { points.add(pt); }
	public boolean repulsive()	{ return repulsive; }
    public int size()             { return points.size(); }
    public int mapX(int i)        { return (points.get(i) % gridW) -1; 	}
    public int mapY(int i)        { return (points.get(i) / gridW) -1; 	}
    void limitMoves(int n) {
        if ((n >= 0) && points.size() > n) 
            points = points.subList(0,n);
    }
    private float sortValue()      {
        if (sortValue < 0)
            calculateSortValue();
        return sortValue;
    }
    private void calculateSortValue() {
        if (straightness < 0)
            calculateStraightness();
        sortValue = size()+(straightness/100.0f);
    }
    public FlightPath(List<Integer> pts, int w) {
        points = pts;
        gridW = w;
    }
	public FlightPath(List<Integer> pts, int w, boolean rep) {
		points = pts;
		gridW = w;
		repulsive = rep;
	}
    public FlightPath() {
        points = new ArrayList<>();
        gridW = mapW;
    }
    int destX() {
        return mapX(size()-1);
    }
    int destY() {
        return mapY(size()-1);
    }
    private void calculateStraightness() {
        int prevX = -1;
        int prevY = -1;
        for (int i=0;i<size();i++) {
            int x0 = mapX(i);
            int y0 = mapY(i);
            if (prevX >= 0) {
                straightness += abs(x0 - prevX);
                straightness += abs(y0 - prevY);
            }
            prevX = x0;
            prevY = y0;
        }
    }
    public static Comparator<FlightPath> SORT = new FlightPathSortComparator();
    private static class FlightPathSortComparator implements Comparator<FlightPath> {
    	@Override public int compare(FlightPath col1, FlightPath col2) {
            return Base.compare(col1.sortValue(),col2.sortValue());
        }
    }

	public static int encode(int x, int y)	{ return (y * mapW) + x; }
	public static int decodeX(int location)	{ return (location % mapW) -1; 	}
	public static int decodeY(int location)	{ return (location / mapW) -1; 	}
	private static int pathSize(FlightPath fp)	{  return fp == null ? 999 : fp.size(); }
	private static int moveDistance(int pt0, int pt1, int w)	{
		int x0 = pt0 % w;
		int y0 = pt0 / w;
		int x1 = pt1 % w;
		int y1 = pt1 / w;
		return Math.max(Math.abs(x0-x1), Math.abs(y0-y1));
	}
	private static int[] bestPathDeltas(int c0, int c1)	{ return bestPathDeltas(c0 % mapW, c0 / mapW, c1 % mapW, c1 / mapW); }
	private static int[] bestPathDeltas(int x0, int y0, int x1, int y1) {
		if (x1 < x0) {
			if (y1 < y0)
				return nwPathPriority;
			else if (y1 > y0)
				return swPathPriority;
			else
				return wPathPriority;
		}
		else if (x1 > x0) {
			if (y1 < y0)
				return nePathPriority;
			else if (y1 > y0)
				return sePathPriority;
			else
				return ePathPriority;
		}
		else {
			if (y1 < y0)
				return nPathPriority;
			else
				return sPathPriority;
		}
	}
	private static FlightPath loadValidPaths(
			int curr, int end,
			boolean[] valid, boolean[] repulsorMap,
			int moves, int maxMoves,
			List<FlightPath> paths, List<Integer> currPath,
			int[] deltas, int gridW, FlightPath bestPath) {

		boolean repulsiveEnd = repulsorMap[end];
		FlightPath updatedBestPath = bestPath;
		if (curr == end) {
			int size = currPath.size();
			if (size <= maxMoves && size <= pathSize(bestPath)) {
				FlightPath newPath = new FlightPath(currPath, gridW, repulsiveEnd);
				paths.add(newPath);
				updatedBestPath = newPath;
			}
			return updatedBestPath;
		}
		int[] basePaths = basePathPriority;

		int remainingMoves = moves - 1;
		for (int dir=0;dir<deltas.length;dir++) {
			int next = curr+deltas[dir];
			boolean repulsiveNext = repulsorMap[next];

			if (valid[next]) {
				// are we at the end? if so create FP and fall out
				if (next == end) {
					currPath.add(next);
					if (currPath.size() <= pathSize(bestPath)) {
						FlightPath newPath = new FlightPath(currPath, gridW, repulsiveEnd);
						paths.add(newPath);
						updatedBestPath = newPath;
					}
				}
				else if (!repulsiveNext && remainingMoves > 0) {
					int minMovesReq = moveDistance(next, end, gridW);
					int minPossibleMoves = minMovesReq + currPath.size() + 1;
					int bestPathSize = pathSize(updatedBestPath);
					if ((minPossibleMoves < bestPathSize) && (minMovesReq <= remainingMoves)) {
						int baseDir = 0;
						for (int i=0; i<basePaths.length;i++) {
							if (basePaths[i] == deltas[dir]) {
								baseDir = i; 
								break;
							}
						}
						List<Integer> nextPath = new ArrayList<>(currPath);
						nextPath.add(next);
						boolean[] nextValid = Arrays.copyOf(valid, valid.length);
						nextValid[curr] = false;
						nextValid[curr + basePaths[(baseDir+1)%8]] = false;
						nextValid[curr + basePaths[(baseDir+7)%8]] = false;
						if (baseDir %2 == 0) {
							nextValid[curr + basePaths[(baseDir+6)%8]] = false;
							nextValid[curr + basePaths[(baseDir+2)%8]] = false;
						}
						int [] pathDeltas = bestPathDeltas(next, end);
						updatedBestPath = loadValidPaths(next, end, nextValid, repulsorMap, remainingMoves, maxMoves, paths, nextPath, pathDeltas, gridW, updatedBestPath);
					}
				}
			}
		}
		return updatedBestPath;
	}
	private static FlightPath allValidPaths(int fromX, int fromY, int toX, int toY, int maxMoves, CombatStack stack, List<FlightPath> validPaths, FlightPath bestPath) {
		FlightPath updatedBestPath = bestPath;
		int gridW = ShipCombatManager.maxX+3;
		ShipCombatManager mgr = stack.mgr;

		// all squares containing ships, asteroids, etc or non-traversable
		// can also check for enemy repulsor beam effects
		boolean[] validTransit = mgr.validMoveMap(stack);
		boolean[] repulsorMap = mgr.repulsorMap(stack);

		int startX = fromX + 1;
		int startY = fromY + 1;
		int endX = toX + 1;
		int endY = toY + 1;

		// based on general direction to travel, find most straightforward path priority
		int[] pathDeltas = bestPathDeltas(startX, startY, endX, endY);

		int start = (startY*gridW)+startX;
		int end = (endY*gridW)+endX;

		List<Integer> path = new ArrayList<>();

		loadValidPaths(start, end, validTransit, repulsorMap, maxMoves, maxMoves, validPaths, path, pathDeltas, gridW, updatedBestPath);
		return updatedBestPath;
	}
	private static List<FlightPath> allValidPathsTo(CombatStack stack, int x1, int y1) {
		List<FlightPath> validPaths = new ArrayList<>();
		allValidPaths(stack.x, stack.y, x1, y1, (int)stack.move, stack, validPaths, null);
		return validPaths;
	}
	public static FlightPath pathTo(CombatStack stack, int x1, int y1) {
		List<FlightPath> validPaths = allValidPathsTo(stack, x1, y1);
		if (validPaths.isEmpty())
			return null;
		Collections.sort(validPaths,FlightPath.SORT);
		return validPaths.get(0);
	}
}
