package rotp.model.tech;

public class TechMoO1Cost extends Tech {
	float costs[] = {0, 0}; // {RotP, MoO1}
	void cost(float rotP, float moo1)		{ costs = new float[] {rotP, moo1}; }
	@Override void setCost(boolean moO1)	{ cost = moO1? costs[1] : costs[0]; }
}
