package rotp.model.events;

import java.awt.Point;
import java.util.HashMap;

import rotp.model.game.DynOptions;

public interface IMonsterPos {
	
	boolean DRAW_WANDERING = false; // TO DO BR: !!! set to false (For debug purpose!)

	default HashMap<Integer, Point.Float> wanderPath()	{ return null; }
	default Point.Float	pos()				{ return null; }
	default int			targetTurnCount()	{ return 0; }
	default boolean		notified()			{ return false; }
	default DynOptions	dynamicOpts()		{ return null; }
}
