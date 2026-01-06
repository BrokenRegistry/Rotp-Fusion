package rotp.model.game;

import static rotp.model.game.IGalaxyOptions.sizeSelection;
import static rotp.model.game.IPreGameOptions.dynStarsPerEmpire;

import rotp.ui.util.LinkData;
import rotp.ui.util.LinkValue;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamFloat;
import rotp.ui.util.ParamInteger;

public interface ISystemsOptions extends IBaseOptsTools {

	ParamInteger artifactPlanetMult		= new ParamInteger( MOD_UI, "ARTIFACT_MULT", 100)
			.setLimits(0, 10000)
			.setIncrements(5, 50, 200)
			.pctValue(true);
	ParamInteger artifactPlanetOffset	= new ParamInteger( MOD_UI, "ARTIFACT_OFFSET", 0)
			.setLimits(-100, 100)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default float	artifactPlanetProb(float base)	{
		float r = (float) (0.01 * (base * artifactPlanetMult.get() + artifactPlanetOffset.get()));
		// System.out.println("artifactPlanetProb from " + base + " to " + r);
		return r;
	}

	ParamInteger orionPlanetProb		= new ParamInteger( MOD_UI, "ARTIFACT_ORION", 0)
			.setLimits(0, 100)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default float	orionPlanetProb()	{ return 0.01f * orionPlanetProb.get(); }

	ParamInteger ultraPoorPlanetMult	= new ParamInteger( MOD_UI, "ULTRA_POOR_MULT", 100)
			.setLimits(0, 10000)
			.setIncrements(5, 50, 200)
			.pctValue(true);
	ParamInteger ultraPoorPlanetOffset	= new ParamInteger( MOD_UI, "ULTRA_POOR_OFFSET", 0)
			.setLimits(-100, 100)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default float	ultraPoorPlanetProb(float base)	{
		float r = (float) (0.01 * (base * ultraPoorPlanetMult.get() + ultraPoorPlanetOffset.get()));
		// System.out.println("ultraPoorPlanetProb from " + base + " to " + r);
		return r;
	}

	ParamInteger poorPlanetMult			= new ParamInteger( MOD_UI, "POOR_MULT", 100)
			.setLimits(0, 10000)
			.setIncrements(5, 50, 200)
			.pctValue(true);
	ParamInteger poorPlanetOffset		= new ParamInteger( MOD_UI, "POOR_OFFSET", 0)
			.setLimits(-100, 100)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default float	poorPlanetProb(float base)	{
		float r = (float) (0.01 * (base * poorPlanetMult.get() + poorPlanetOffset.get()));
		// System.out.println("poorPlanetProb from " + base + " to " + r);
		return r;
	}

	ParamInteger richPlanetMult			= new ParamInteger( MOD_UI, "RICH_MULT", 100)
			.setLimits(0, 10000)
			.setIncrements(5, 50, 200)
			.pctValue(true);
	ParamInteger richPlanetOffset		= new ParamInteger( MOD_UI, "RICH_OFFSET", 0)
			.setLimits(-100, 100)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default float	richPlanetProb(float base)	{
		float r = (float) (0.01 * (base * richPlanetMult.get() + richPlanetOffset.get()));
		// System.out.println("richPlanetProb from " + base + " to " + r);
		return r;
	}

	ParamInteger ultraRichPlanetMult	= new ParamInteger( MOD_UI, "ULTRA_RICH_MULT", 100)
			.setLimits(0, 10000)
			.setIncrements(5, 50, 200)
			.pctValue(true);
	ParamInteger ultraRichPlanetOffset	= new ParamInteger( MOD_UI, "ULTRA_RICH_OFFSET", 0)
			.setLimits(-100, 100)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default float	ultraRichPlanetProb(float base)	{
		float r = (float) (0.01 * (base * ultraRichPlanetMult.get() + ultraRichPlanetOffset.get()));
		// System.out.println("ultraRichPlanetProb from " + base + " to " + r);
		return r;
	}

	ParamInteger PlanetSizeMultiplier	= new ParamInteger( MOD_UI, "PLANET_SIZE_MULT", 100)
			.setLimits(80, 150)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default float planetSizeMultiplier()	{ return 0.01f * PlanetSizeMultiplier.get(); }

	ParamInteger PlanetCreationSizeMax	= new ParamInteger( MOD_UI, "PLANET_CREATION_MAX", 120)
			.setLimits(100, 150)
			.setIncrements(1, 5, 20);
	default int	planetCreationSizeMax()		{ return PlanetCreationSizeMax.get(); }
	ParamInteger PlanetCreationSizeMin	= new ParamInteger( MOD_UI, "PLANET_CREATION_MIN", 10)
			.setLimits(5, 25)
			.setIncrements(1, 5, 20);
	default int	planetCreationSizeMin()		{ return PlanetCreationSizeMin.get(); }
	ParamInteger PlanetCreationRound	= new ParamInteger( MOD_UI, "PLANET_CREATION_ROUND", 5)
			.setLimits(1, 10)
			.setIncrements(1, 2, 5);
	default int	planetCreationRound()		{ return PlanetCreationSizeMin.get(); }

	ParamInteger PlanetBaseSizeMax	= new ParamInteger( MOD_UI, "PLANET_BASE_SIZE_MAX", 180)
			.setLimits(150, 300)
			.setIncrements(1, 5, 20);
	default int	planetBaseSizeMax()			{ return PlanetBaseSizeMax.get(); }

	ParamBoolean allowRichPoorArtifact	= new ParamBoolean( MOD_UI, "RICH_POOR_ARTIFACT", false);
	default boolean allowRichPoorArtifact()		{ return allowRichPoorArtifact.get(); }

	ParamInteger orionToEmpireModifier	= new ParamInteger( MOD_UI, "ORION_TO_EMPIRE_MODIFIER", 100)
			.setDefaultValue(MOO1_DEFAULT, 0)
			.setLimits(0, 1000)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default float	orionToEmpireModifier()	{ return (float) (0.01 * orionToEmpireModifier.get()); }

	ParamFloat firstRingRadius = new FirstRingRadius();
	final class FirstRingRadius extends ParamFloat {
		FirstRingRadius() {
			super(MOD_UI, "FIRST_RING_RADIUS", 3.0f);
			setDefaultValue(MOO1_DEFAULT, 4.0f);
			setLimits(2f, 10f);
			setIncrements(0.1f, 0.5f, 1f);
			isValueInit(false);
			guiFormat("0.0");
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				resetLinks();
				addLink(secondRingRadius,	   DO_FOLLOW, GO_UP,   GO_UP,   "Radius");
				//addLink(firstRingSystemNumber, DO_FOLLOW, GO_DOWN, GO_DOWN, "Number");
				//addLink(opts().starDensity(),	DO_REFRESH);
			}
			else
				super.initDependencies(level);
		}
		@Override public boolean isValidValue()	{ return isValidDoubleCheck(); }
		@Override protected void convertValueToLink(LinkData rec)	{
			switch (rec.key) {
//				case "Number":
//					rec.aimValue = new LinkValue(radiusToNumStars(rec.srcValue.floatValue()));
//					return;
				case "Radius":
					rec.aimValue = rec.srcValue;
					return;
				default:
					super.convertValueToLink(rec);
			}
		}
	}
	default float firstRingRadius() { return firstRingRadius.getValidValue(); }

	ParamFloat secondRingRadius = new SecondRingRadius();
	final class SecondRingRadius extends ParamFloat {
		SecondRingRadius() {
			super(MOD_UI, "SECOND_RING_RADIUS", 6.0f);
			setLimits(2f, 50f);
			setIncrements(0.1f, 0.5f, 2f);
			isValueInit(false);
			guiFormat("0.0");
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				resetLinks();
				addLink(firstRingRadius,		DO_FOLLOW, GO_DOWN, GO_DOWN, "Radius");
				//addLink(secondRingSystemNumber,	DO_FOLLOW, GO_DOWN, GO_DOWN, "Number");
				//addLink(opts().starDensity(),	DO_REFRESH);
			}
			else
				super.initDependencies(level);
		}
		@Override public boolean isValidValue()	{ return isValidDoubleCheck(); }
		@Override protected void convertValueToLink(LinkData rec)	{
			switch (rec.key) {
//				case "Number":
//					rec.aimValue = new LinkValue(radiusToNumStars(rec.srcValue.floatValue()));
//					return;
				case "Radius":
					rec.aimValue = rec.srcValue;
					return;
				default:
					super.convertValueToLink(rec);
			}
		}
	}
	default float secondRingRadius() { return secondRingRadius.getValidValue(); }

	float surfaceSecurityFactor = 0.855f;

//	static Integer radiusToNumStars(float radius) {
//		float systemBuffer = 1.9f;
//		if (!Rotp.noOptions()) {
//			IGameOptions opts = RulesetManager.current().currentOptions();
//			systemBuffer = opts.systemBuffer(opts.selectedStarDensityOption());
//		}
//		float root = radius / (systemBuffer * surfaceSecurityFactor);
//		int numStars = (int) (root*root);
//		return numStars;
//	}
//	static Float numStarsToRadius(int num) {
//		float systemBuffer = 1.9f;
//		if (!Rotp.noOptions()) {
//			IGameOptions opts = RulesetManager.current().currentOptions();
//			systemBuffer = opts.systemBuffer(opts.selectedStarDensityOption());
//		}
//		float radius = (float) (systemBuffer * Math.sqrt(num) * surfaceSecurityFactor);
//		return radius;
//	}

	ParamInteger firstRingSystemNumber = new FirstRingSystemNumber();
	final class FirstRingSystemNumber extends ParamInteger {
		FirstRingSystemNumber() {
			super(MOD_UI, "FIRST_RING_SYS_NUM",2);
			setDefaultValue(MOO1_DEFAULT, 1);
			setLimits(0, 50);
			setIncrements(1, 5, 10);
			isValueInit(false);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				resetLinks();
				addLink(secondRingSystemNumber,	DO_FOLLOW, GO_UP,   GO_UP,   "Number");
				//addLink(firstRingRadius,		DO_FOLLOW, GO_UP,   GO_UP,   "Radius");
				addLink(firstRingHabitable,		DO_FOLLOW, GO_DOWN, GO_DOWN, "Habitable");			
				//addLink(opts().starDensity(),	DO_REFRESH);
			}
			else
				super.initDependencies(level);
		}
		@Override public boolean isValidValue()	{ return isValidDoubleCheck(); }
		@Override protected void convertValueToLink(LinkData rec)	{
			switch (rec.key) {
				case "Number":
				case "Habitable":
					rec.aimValue = rec.srcValue;
					return;
//				case "Radius":
//					rec.aimValue = new LinkValue(numStarsToRadius(rec.srcValue.intValue()));
//					return;
				default:
					super.convertValueToLink(rec);
			}
		}
	}
	default int firstRingSystemNumber()	{ return firstRingSystemNumber.getValidValue(); }
	default void setFirstRingSystemNumber(int num)	{ firstRingSystemNumber.set(num); }
	default String getFirstRingSystemNumberLabel()	{ return firstRingSystemNumber.getLangLabel(); }

	ParamInteger secondRingSystemNumber = new SecondRingSystemNumber();
	final class SecondRingSystemNumber extends ParamInteger {
		SecondRingSystemNumber() {
			super(MOD_UI, "SECOND_RING_SYS_NUM", 2);
			setLimits(0, 200);
			setIncrements(1, 5, 20);
			isValueInit(false);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				resetLinks();
				//addLink(secondRingRadius,		DO_FOLLOW, GO_UP,   GO_UP,   "Radius");
				addLink(sizeSelection,			DO_FOLLOW, GO_UP,   GO_UP,   "Size");
				addLink(dynStarsPerEmpire,		DO_FOLLOW, GO_UP,   GO_UP,   "Dyn");
				addLink(secondRingHabitable,	DO_FOLLOW, GO_DOWN, GO_DOWN, "Habitable");
				addLink(firstRingSystemNumber,	DO_FOLLOW, GO_DOWN, GO_DOWN, "Number");
				addLink(sizeSelection,			DO_FOLLOW, GO_DOWN, GO_DOWN, "Size");
				//addLink(opts().starDensity(),	DO_REFRESH);
			}
			else
				super.initDependencies(level);
		}
		@Override public boolean isValidValue()	{ return isValidDoubleCheck(); }
		@Override protected void convertValueToLink(LinkData rec)	{
			switch (rec.key) {
				case "Number":
				case "Habitable":
					rec.aimValue = rec.srcValue;
					return;
				case "Size":
					int size = rec.srcValue.intValue()+2;
					rec.aimValue = new LinkValue(opts().getGalaxyKey(size));
					//System.out.println("Convert Size = "+(size-2)+"->"+size+"->"+rec.aimValue.stringValue());
					return;
				case "Dyn":
					rec.aimValue = new LinkValue((rec.srcValue.intValue()+1));
					return;
//				case "Radius":
//					rec.aimValue = new LinkValue(numStarsToRadius(rec.srcValue.intValue()));
//					return;
				default:
					super.convertValueToLink(rec);
			}
		}
	}
	default int secondRingSystemNumber() { return secondRingSystemNumber.getValidValue(); }
	default void setSecondRingSystemNumber(int num)		{ secondRingSystemNumber.set(num); }
	default String getSecondRingSystemNumberLabel()		{ return secondRingSystemNumber.getLangLabel(); }

	ParamInteger firstRingHabitable = new FirstRingHabitable();
	final class FirstRingHabitable extends ParamInteger {
		FirstRingHabitable() {
			super(MOD_UI, "FIRST_RING_HABITABLE", 1);
			setDefaultValue(MOO1_DEFAULT, 0);
			setLimits(0, 50);
			setIncrements(1, 5, 10);
			isValueInit(false);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				resetLinks();
				addLink(secondRingHabitable,   DO_FOLLOW, GO_UP, GO_UP, "Habitable");
				addLink(firstRingSystemNumber, DO_FOLLOW, GO_UP, GO_UP, "Number");
			}
			else
				super.initDependencies(level);
		}
		@Override public boolean isValidValue()	{ return isValidDoubleCheck(); }
		@Override protected void convertValueToLink(LinkData rec)	{
			switch (rec.key) {
				case "Number":
				case "Habitable":
					rec.aimValue = rec.srcValue;
					return;
				default:
					super.convertValueToLink(rec);
			}
		}
	}
	default int firstRingHabitable() { return firstRingHabitable.getValidValue(); }

	ParamInteger secondRingHabitable = new SecondRingHabitable();
	final class SecondRingHabitable extends ParamInteger {
		SecondRingHabitable() {
			super(MOD_UI, "SECOND_RING_HABITABLE", 1);
			setDefaultValue(MOO1_DEFAULT, 0);
			setLimits(0, 200);
			setIncrements(1, 5, 20);
			isValueInit(false);
		}
		@Override public void initDependencies(int level)	{
			if (level == 0) {
				resetLinks();
				addLink(secondRingSystemNumber,	DO_FOLLOW, GO_UP,   GO_UP,   "Number");
				addLink(firstRingHabitable,		DO_FOLLOW, GO_DOWN, GO_DOWN, "Habitable");
			}
			else
				super.initDependencies(level);
		}
		@Override public boolean isValidValue()	{ return isValidDoubleCheck(); }
		@Override protected void convertValueToLink(LinkData rec)	{
			switch (rec.key) {
				case "Number":
				case "Habitable":
					rec.aimValue = rec.srcValue;
					return;
				default:
					super.convertValueToLink(rec);
			}
		}
	}
	default int secondRingHabitable() { return secondRingHabitable.getValidValue(); }
}
