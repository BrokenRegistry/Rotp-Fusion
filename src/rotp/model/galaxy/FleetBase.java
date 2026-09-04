package rotp.model.galaxy;

import java.awt.Rectangle;
import java.io.Serializable;

import rotp.ui.main.GalaxyMapPanel;

abstract class FleetBase implements Ship, Serializable {
	private static final long serialVersionUID = 1L;
	private float arrivalTime = Float.MAX_VALUE;
	private final Integer hashCode;
	
	private transient boolean displayed;
	private transient Rectangle selectBox;
	private transient boolean hovering;

	@Override public int hashCode() {
    	if (hashCode == null)
    		return super.hashCode(); // for backward compatibility
    	return hashCode;
    }
    @Override public boolean equals(Object ship) {
    	if (hashCode == null)
    		return this==ship; // for backward compatibility
    	if (ship == null)
    		return false;
    	if (this==ship)
    		return true;
    	if (ship instanceof Ship)
    		return ship.hashCode() == this.hashCode();
    	return false;
    }
    FleetBase () {
    	hashCode = galaxy().nextHashCodeShip();
    }
	@Override public float arrivalTimeAdjusted() {
		if (arrivalTime == Float.MAX_VALUE) {
	    	throw new RuntimeException("Something has gone terribly wrong: it appears setArrivalTime() was never called.");
	    }
	    return arrivalTime;
	}
	protected abstract float calculateAdjustedArrivalTime();
	void setArrivalTimeAdjusted() {
		arrivalTime = calculateAdjustedArrivalTime();
		if (arrivalTime == Float.MAX_VALUE) {
			throw new RuntimeException("Something has gone terribly wrong: calculateArrivalTime() returned Float.MAX_VALUE.");
		}
	}

	@Override
	public boolean displayed() { return displayed; }
	protected abstract boolean decideWhetherDisplayed(GalaxyMapPanel map);
	@Override
	public void setDisplayed(GalaxyMapPanel map) {
		displayed = decideWhetherDisplayed(map);
	}

	Rectangle selectBox() {
		if (selectBox == null)
			selectBox = new Rectangle();
		return selectBox;
	}

	@Override
	public boolean hovering()                   { return hovering; }
	@Override
	public void hovering(boolean b)             { hovering = b; }
}
