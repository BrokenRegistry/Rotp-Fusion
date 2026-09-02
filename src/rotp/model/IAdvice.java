package rotp.model;

import rotp.ui.ScaledInteger;
import rotp.util.AdviceBox;

public interface IAdvice extends ScaledInteger {
	default AdviceBox getBox()	{ return null; }
	boolean hovering();
}
