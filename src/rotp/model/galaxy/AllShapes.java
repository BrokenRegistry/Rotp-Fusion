package rotp.model.galaxy;

import static rotp.model.galaxy.GalaxyRandomShape.ALL_SHAPES;
import static rotp.model.galaxy.GalaxyRandomShape.COMPLEX_SHAPES;
import static rotp.model.galaxy.GalaxyRandomShape.EXTRA_SHAPES;
import static rotp.model.galaxy.GalaxyRandomShape.IMAGINARY_SHAPES;
import static rotp.model.galaxy.GalaxyRandomShape.REAL_SHAPES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import rotp.model.game.IBaseOptsTools;
import rotp.model.game.IGalaxyOptions;
import rotp.model.game.IGalaxyOptions.ListShapeParam;
import rotp.model.game.IGameOptions;
import rotp.model.game.SafeListParam;

public class AllShapes {
	private	static final Map<String, ListShapeParam> SHAPES_MAP = new LinkedHashMap<>();
	
	public static Map<String, ListShapeParam> shapesMap()	{
		if (SHAPES_MAP.isEmpty()) {
			new GalaxyRectangularShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyEllipticalShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxySpiralShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyTextShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyClusterShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxySwirlClustersShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyGridShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxySpiralArmsShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyMazeShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyShurikenShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyBullseyeShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyLorenzShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyFractalShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyBitmapShape(null, null).registerOptions(SHAPES_MAP);
			new GalaxyRandomShape(null, null).registerOptions(SHAPES_MAP);
		}
		return SHAPES_MAP;
	}
	public static GalaxyShape getShape()	{
		String name = IGalaxyOptions.shapeSelection.get();
		return getShape(name, null, null);
	}
	public static GalaxyShape getShape(String name, IGameOptions options, boolean[] rndOpt)	{
		switch(name) {
			case GalaxyRectangularShape.NAME:
				return new GalaxyRectangularShape(options, rndOpt).get();
			case GalaxyEllipticalShape.NAME:
				return new GalaxyEllipticalShape(options, rndOpt).get();
			case GalaxySpiralShape.NAME:
				return new GalaxySpiralShape(options, rndOpt).get();
			case GalaxyTextShape.NAME:
				return new GalaxyTextShape(options, rndOpt).get();
			case GalaxyClusterShape.NAME:
				return new GalaxyClusterShape(options, rndOpt).get();
			case GalaxySwirlClustersShape.NAME:
				return new GalaxySwirlClustersShape(options, rndOpt).get();
			case GalaxyGridShape.NAME:
				return new GalaxyGridShape(options, rndOpt).get();
			case GalaxySpiralArmsShape.NAME:
				return new GalaxySpiralArmsShape(options, rndOpt).get();
			case GalaxyMazeShape.NAME:
				return new GalaxyMazeShape(options, rndOpt).get();
			case GalaxyShurikenShape.NAME:
				return new GalaxyShurikenShape(options, rndOpt).get();
			case GalaxyBullseyeShape.NAME:
				return new GalaxyBullseyeShape(options, rndOpt).get();
			case GalaxyLorenzShape.NAME:
				return new GalaxyLorenzShape(options, rndOpt).get();
			case GalaxyFractalShape.NAME:
				return new GalaxyFractalShape(options, rndOpt).get();
			case GalaxyBitmapShape.NAME:
				return new GalaxyBitmapShape(options, rndOpt).get();
			case GalaxyRandomShape.NAME:
				return new GalaxyRandomShape(options, rndOpt).get();
			default:
				System.err.println("Error: Unknown Galaxy name... " + name + " !?");
				System.out.println("Rectangular Shape returned instead");
				return new GalaxyRectangularShape(options, rndOpt).get();
		}
	}
	public static String getDefault()		{ return GalaxyRectangularShape.NAME; }
	public static List<String> getNames()	{ return new ArrayList<>(shapesMap().keySet()); }
	public static List<String> getNamesForRandom( boolean bigList)	{
		if (bigList)
			return getNamesForRandom(new ArrayList<String>(Arrays.asList(REAL_SHAPES, IMAGINARY_SHAPES)));
		else
			return getNamesForRandom(new ArrayList<String>(Arrays.asList(REAL_SHAPES)));
	}
	public static Map<String, String> getMapForRandom()	{
		Map<String, String> map = new HashMap<>();
		map.put(GalaxyRectangularShape.NAME, REAL_SHAPES);
		map.put(GalaxyEllipticalShape.NAME, REAL_SHAPES);
		map.put(GalaxySpiralShape.NAME, REAL_SHAPES);
		map.put(GalaxySpiralArmsShape.NAME, REAL_SHAPES);
		map.put(GalaxyClusterShape.NAME, REAL_SHAPES);
		map.put(GalaxyShurikenShape.NAME, REAL_SHAPES);
		map.put(GalaxySwirlClustersShape.NAME, IMAGINARY_SHAPES);
		map.put(GalaxyGridShape.NAME, IMAGINARY_SHAPES);
		map.put(GalaxyMazeShape.NAME, IMAGINARY_SHAPES);
		map.put(GalaxyBullseyeShape.NAME, IMAGINARY_SHAPES);
		map.put(GalaxyLorenzShape.NAME, IMAGINARY_SHAPES);
		map.put(GalaxyFractalShape.NAME, IMAGINARY_SHAPES);
		map.put(GalaxyTextShape.NAME, EXTRA_SHAPES);
		return map;
	}
	public static List<String> getNamesForRandom(List<String> keys)	{
		Map<String, String> map = getMapForRandom();
		List<String> names = new ArrayList<>();
		for (Entry<String, String> e : map.entrySet())
			if (keys.contains(e.getValue()))
				names.add(e.getKey());
		return names;
	}
	public static List<String> getNamesForRandom(String key)	{
		switch (key) {
			case REAL_SHAPES:
				return getNamesForRandom(new ArrayList<String>(
						Arrays.asList(REAL_SHAPES)));
			case IMAGINARY_SHAPES:
				return getNamesForRandom(new ArrayList<String>(
						Arrays.asList(IMAGINARY_SHAPES)));
			case EXTRA_SHAPES:
				return getNamesForRandom(new ArrayList<String>(
						Arrays.asList(EXTRA_SHAPES)));
			case ALL_SHAPES:
				return getNamesForRandom(new ArrayList<String>(
						Arrays.asList(REAL_SHAPES, IMAGINARY_SHAPES, EXTRA_SHAPES)));
			case COMPLEX_SHAPES:
			default:
				return getNamesForRandom(new ArrayList<String>(
						Arrays.asList(REAL_SHAPES, IMAGINARY_SHAPES)));
		}
	}
	public static SafeListParam optionList()	{
		SafeListParam list = new SafeListParam("");
		for (Entry<String, ListShapeParam> entry : shapesMap().entrySet()) {
			list.addAll(entry.getValue());
			list.add(IBaseOptsTools.LINE_SPACER_25);
		}
		if (!list.isEmpty())
			list.remove(list.size()-1);
		return list;
	}
}
