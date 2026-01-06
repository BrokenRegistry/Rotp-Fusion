package rotp.model.game;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.style.XYStyler;

import rotp.model.colony.Colony;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.ships.ShipDesign;
import rotp.ui.util.IParam;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamFloat;
import rotp.ui.util.ParamImage;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;
import rotp.ui.util.XChartRotpTheme;

public interface IInGameOptions extends IRandomEvents, IConvenienceOptions, ICombatOptions, IShipDesignOption {

	// ========================================================================
	// GamePlay options
	ParamList popGrowthFactor				= new ParamList(MOD_UI, "POP_GROWTH", "Normal")
			.showFullGuide(true)
			.put("Normal",		MOD_UI + "POP_GROWTH_NORMAL")
			.put("Reduced",		MOD_UI + "POP_GROWTH_REDUCED");
	default String selectedPopGrowthFactor()	{ return popGrowthFactor.get(); }

	ParamList targetBombard					= new ParamList(MOD_UI, "TARGET_BOMBARD", "None")
			.showFullGuide(true)
			.put("None",	MOD_UI + "TARGET_BOMBARD_NONE")
			.put("AI",		MOD_UI + "TARGET_BOMBARD_AI")
			.put("Player",	MOD_UI + "TARGET_BOMBARD_PLAYER")
			.put("Both",	MOD_UI + "TARGET_BOMBARD_BOTH");
	default boolean targetBombardAllowedForAI() {
		switch (targetBombard.get().toUpperCase()) {
			case  "BOTH":
			case  "AI":
				return true;
			default:
				return false;
		}
	}
	default boolean targetBombardAllowedForPlayer() {
		switch (targetBombard.get().toUpperCase()) {
			case  "BOTH":
			case  "PLAYER":
				return true;
			default:
				return false;
		}
	}

	ParamInteger customDifficulty	= new ParamInteger(MOD_UI, "CUSTOM_DIFFICULTY", 100)
			.setLimits(20, 500)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default int selectedCustomDifficulty()		{ return customDifficulty.get(); }

	String DYNAMIC_DIFFICULTY_UPDATE_ID	= "turnMod";
	ParamBoolean dynamicDifficulty	= new ParamBoolean(MOD_UI, "DYNAMIC_DIFFICULTY", false)
			.setUpdateParameters(IInGameOptions::tagDynamicDifficulty, "");
	default boolean selectedDynamicDifficulty()	{ return dynamicDifficulty.get(); }

	final class ParamDynDiffInt extends ParamInteger	{
		public ParamDynDiffInt(String gui, String name, Integer defaultValue) {
			super(gui, name, defaultValue);
		}
		@Override public boolean isGhost()	{ return !dynamicDifficulty.get(); }
	}
	ParamInteger dynamicDifficultyDelay	= new ParamDynDiffInt(MOD_UI, "DYNAMIC_DIFFICULTY_DELAY", 150)
			.setLimits(20, 500)
			.setIncrements(1, 5, 20)
			.setUpdateParameters(Empire::valueUpdated, DYNAMIC_DIFFICULTY_UPDATE_ID);
	default int dynamicDifficultyDelay()		{ return dynamicDifficultyDelay.get(); }

	ParamInteger dynamicDifficultyRange	= new ParamDynDiffInt(MOD_UI, "DYNAMIC_DIFFICULTY_RANGE", 10)
			.setLimits(0, 500)
			.setIncrements(1, 5, 20)
			.setUpdateParameters(Empire::valueUpdated, DYNAMIC_DIFFICULTY_UPDATE_ID);
	default int dynamicDifficultyRange()		{ return dynamicDifficultyRange.get(); }

	ParamInteger dynamicDifficultySpan	= new ParamDynDiffInt(MOD_UI, "DYNAMIC_DIFFICULTY_SPAN", 100)
			.setLimits(10, 500)
			.setIncrements(1, 5, 20)
			.setUpdateParameters(Empire::valueUpdated, DYNAMIC_DIFFICULTY_UPDATE_ID);
	default float dynamicDifficultySpan()		{ return dynamicDifficultySpan.get()/100f; }

	static double dynamicDifficultyBaseTurnMod(int turn, int delay, int range)	{
		if (range == 0) { 
			if (turn == delay)
				return 0.5;
			if (turn > delay)
				return 1;
			return 0;
		}
		return 0.5 + Math.atan((double)(turn-delay)/range)/Math.PI;
	}
	static double dynamicDifficultyTurnMod(int turn, int delay, int range)	{
		double mod = dynamicDifficultyBaseTurnMod(turn, delay, range);
		if (turn >= delay)
			return mod;
		double shift = dynamicDifficultyBaseTurnMod(5, delay, range);// -.0219177f;
		return (mod - shift) / (0.5f - shift) / 2;
	}
	default double dynamicDifficultyTurnMod(int turn)	{
		int delay = dynamicDifficultyDelay();
		int range = dynamicDifficultyRange();
		double turnMod = dynamicDifficultyTurnMod(turn, delay, range);
		return turnMod;
	}
	
	final class ParamDynDiffTurnImage extends ParamImage {
		public ParamDynDiffTurnImage()	{
			super(MOD_UI, "DYNAMIC_DIFFICULTY_TURN_IMAGE");
		}
		@Override public boolean updated()	{
			return true;
			//return dynamicDifficultyMode.updated() || dynamicDifficultySpan.updated();
		}
		@Override public float heightFactor()	{ return 8f; }
		@Override public void paint(Graphics2D g, int x, int y, int w, int h)	{
			BufferedImage img = getImage(w, h);
			g.drawImage(img, x, y, null);
		}
		@Override public BufferedImage getImage(int width, int height)	{
			BufferedImage img = new BufferedImage(width, height, TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) img.getGraphics();
			XYChart chart = getChart();
			chart.paint(g, width, height);
			g.dispose();
			return img;
		}
		@Override public boolean isGhost()	{ return !dynamicDifficulty.get(); }
		public XYChart getChart()	{
			int delay = dynamicDifficultyDelay.get();
			int range = dynamicDifficultyRange.get();
			return getChart(delay, range);
		}
		public XYChart getChart(int delay, int range)	{
			String xTitle = IParam.langLabel("GRAPH_DYN_DIFF_DELAY_X_AXIS_LABEL");
			String yTitle = IParam.langLabel("GRAPH_DYN_DIFF_DELAY_Y_AXIS_LABEL");
			String TitleHead;
			if (dynamicDifficulty.get())
				TitleHead = IParam.langLabel("GRAPH_DYN_DIFF_DELAY_HEAD_TITLE");
			else
				TitleHead = IParam.langLabel("GRAPH_DYN_DIFF_DELAY_HEAD_TITLE_OFF");
			int xMax = (Math.max(2*delay, delay+3*range))/100;
			xMax = Math.max(3, xMax)*100;
			double yMax = 1;
			int step = 5;
			double xStart = 5;
			int iSh = (int) (xStart/step);
			int n = (int) (xMax/step - iSh);
			double[] xP = new double[n];
			double[] yP = new double[n];
			for (int i=0; i<n; i++) {
				int x = step * (i+iSh);
				xP[i] = x;
				yP[i] = dynamicDifficultyTurnMod(x, delay, range);
			}
			XYChart chart = QuickChart.getChart(TitleHead, xTitle, yTitle, null, xP, yP);
			XYStyler styler = chart.getStyler();
			styler.setTheme(new XChartRotpTheme());
			styler.setLegendVisible(false);
			styler.setYAxisMin(0.0);
			styler.setYAxisMax(yMax);
			styler.setXAxisMin(0.0);
			styler.setXAxisMax((double) xMax);
			return chart;
		}
	}
	ParamDynDiffTurnImage dynamicDifficultyTurnImage	= new ParamDynDiffTurnImage();

	final class ParamDynamicDifficulty extends ParamList {
		private static final String UNFAIR_TO_PLAYER	= "DYN_DIFF_UNFAIR_TO_PLAYER";
		private static final String UNFAIR_TO_AI		= "DYN_DIFF_UNFAIR_TO_AI";
		private static final String FAIR_BOUND			= "DYN_DIFF_FAIR_BOUND";
		private static final String FAIR_UNBOUND		= "DYN_DIFF_FAIR_UNBOUND";
		private static final String QUICK_ENDING		= "DYN_DIFF_QUICK_ENDING";
		public ParamDynamicDifficulty()	{
			super(MOD_UI, "DYNAMIC_DIFFICULTY_MODE", UNFAIR_TO_PLAYER);
			showFullGuide(true);
			put(UNFAIR_TO_PLAYER,	MOD_UI + UNFAIR_TO_PLAYER);
			put(UNFAIR_TO_AI,		MOD_UI + UNFAIR_TO_AI);
			put(FAIR_BOUND,			MOD_UI + FAIR_BOUND);
			put(FAIR_UNBOUND,		MOD_UI + FAIR_UNBOUND);
			put(QUICK_ENDING,		MOD_UI + QUICK_ENDING);
		}
		@Override public boolean isGhost()	{ return !dynamicDifficulty.get(); }
		public double getScaleMod(double r)	{ return getScaleMod(r, dynamicDifficultySpan.get() / 100.0, get());
		}
		public double getScaleMod(double r, double span, String mode)	{
			double ratioExp = 1/span;
			double ratio = Math.pow(r, ratioExp);
			double scale;

			switch (mode) {
				case UNFAIR_TO_PLAYER:	scale = unfairToPlayer(ratio);	break;
				case UNFAIR_TO_AI:		scale = unfairToAI(ratio);		break;
				case FAIR_BOUND:		scale = fairBound(ratio);		break;
				case FAIR_UNBOUND:		scale = fairUnbound(ratio);		break;
				case QUICK_ENDING:		scale = quickEnd(ratio);		break;
				default:				scale = unfairToPlayer(ratio);	break;
			}
			return Math.pow(scale, span);
		}
		public XYChart getChart()	{
			double span = dynamicDifficultySpan.get() / 100.0;
			String mode = get();
			String subTitle = guideValue();
			return getChart(span, mode, subTitle);
		}
		public XYChart getChart(double span, String mode, String subTitle)	{
			String xTitle = IParam.langLabel("GRAPH_DYN_DIFF_MODE_X_AXIS_LABEL");
			String yTitle = "";
			String TitleHead;
			if (dynamicDifficulty.get())
				TitleHead = IParam.langLabel("GRAPH_DYN_DIFF_MODE_HEAD_TITLE", subTitle);
			else
				TitleHead = IParam.langLabel("GRAPH_DYN_DIFF_MODE_HEAD_TITLE_OFF");
			String[] names = new String[] {
					IParam.langLabel("GRAPH_DYN_DIFF_MODE_LEGEND_1"),
					IParam.langLabel("GRAPH_DYN_DIFF_MODE_LEGEND_2"),
					IParam.langLabel("GRAPH_DYN_DIFF_MODE_LEGEND_3"),
					IParam.langLabel("GRAPH_DYN_DIFF_MODE_LEGEND_4")};
			double xMax = 2.5;
			double yMax = 2.5;
			double step = .05;
			double xStart = .1;
			int iSh = (int) (xStart/step);
			int n = (int) (xMax/step - iSh);
			double[] xP = new double[n];
			double[][] yP = new double[4][n];
			for (int i=0; i<n; i++) {
				xP[i] = step * (i+iSh);
				yP[0][i] = 1;
				yP[1][i] = getScaleMod(xP[i], span, mode);
				yP[2][i] = xP[i];
				yP[3][i] = xP[i]*yP[1][i];
			}
			XYChart chart = QuickChart.getChart(TitleHead, xTitle, yTitle, names, xP, yP);
			XYStyler styler = chart.getStyler();
			styler.setTheme(new XChartRotpTheme());
			styler.setYAxisMin(0.0);
			styler.setYAxisMax(yMax);
			styler.setXAxisMin(0.0);
			styler.setXAxisMax(xMax);
			return chart;
		}
		private double bound(double r)			{ return (1 + Math.pow(r-1, 3) / (Math.pow(r-1, 2) + 0.25)) / r; }
		private double unbound(double r)		{ return (1.01 - 1.01/(100*r+1)) * bound(r); }
		private double fairUnbound(double r)	{ return r > 1.0? 1/unbound(1/r) : unbound(r); }
		private double fairBound(double r)		{ return r > 1.0? bound(r) : 1/bound(1/r); }
		private double unfairToAI(double r)		{ return r > 1.0? 1/unbound(1/r) : 1/bound(1/r); }
		private double unfairToPlayer(double r)	{ return r > 1.0? bound(r) : unbound(r); }
		private double quickEnd(double r)		{ return 1/fairUnbound(r); }
	}
	ParamDynamicDifficulty dynamicDifficultyMode	= new ParamDynamicDifficulty();
	default double getDynamicDifficultyScale(double r_empInd)	{ return dynamicDifficultyMode.getScaleMod(r_empInd); }

	final class ParamDynDiffModeImage extends ParamImage {
		public ParamDynDiffModeImage() {
			super(MOD_UI, "DYNAMIC_DIFFICULTY_MODE_IMAGE");
		}
		@Override public boolean updated()		{ return true; }
		@Override public boolean isGhost()		{ return !dynamicDifficulty.get(); }
		@Override public float heightFactor()	{ return 8f; }
		@Override public void paint(Graphics2D g, int x, int y, int w, int h)	{
			BufferedImage img = getImage(w, h);
			g.drawImage(img, x, y, null);
		}
		@Override public BufferedImage getImage(int width, int height)	{
			BufferedImage img = new BufferedImage(width, height, TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) img.getGraphics();
			XYChart chart = dynamicDifficultyMode.getChart();
			chart.paint(g, width, height);
			g.dispose();
			return img;
		}
	}
	ParamDynDiffModeImage dynamicDifficultyModeImage	= new ParamDynDiffModeImage();
	static void tagDynamicDifficulty(String id)	{
		dynamicDifficultyMode.updated(true);
		dynamicDifficultySpan.updated(true);
		dynamicDifficultyRange.updated(true);
		dynamicDifficultyDelay.updated(true);
	}

	ParamList scrapRefundOption		= new ParamList(MOD_UI, "SCRAP_REFUND", "All")
			.showFullGuide(true)
			.put("All",		MOD_UI + "SCRAP_REFUND_ALL")
			.put("Empire",	MOD_UI + "SCRAP_REFUND_EMPIRE")
			.put("Ally",	MOD_UI + "SCRAP_REFUND_ALLY")
			.put("Never",	MOD_UI + "SCRAP_REFUND_NEVER");
	default String selectedScrapRefundOption()	{ return scrapRefundOption.get(); }

	ParamFloat scrapRefundFactor	= new ParamFloat(MOD_UI, "SCRAP_REFUND_FACTOR", 0.25f)
			.setLimits(0f, 1f)
			.setIncrements(0.01f, 0.05f, 0.2f)
			.cfgFormat( "0.##")
			.guiFormat("%");
	default float selectedScrapRefundFactor()	{ return scrapRefundFactor.get(); }

	ParamInteger shipSpaceFactor	= new ParamInteger(MOD_UI, "SHIP_SPACE_FACTOR", 100)
			.setLimits(10, 1000)
			.setIncrements(1, 10, 50)
			.pctValue(true);
	default float selectedShipSpaceFactor()	{ return shipSpaceFactor.get()/100f; }

	ParamFloat missileBaseModifier	= new ParamFloat(MOD_UI, "MISSILE_BASE_MODIFIER", 2f/3f)
			.setDefaultValue(MOO1_DEFAULT, 1f)
			.setDefaultValue(ROTP_DEFAULT, 1f)
			.setLimits(0.1f, 2f)
			.setIncrements(0.01f, 0.05f, 0.2f)
			.cfgFormat("0.##")
			.guiFormat("%")
			.formerName(MOD_UI+"MISSILE_SIZE_MODIFIER");
	default float selectedMissileBaseModifier()	{ return missileBaseModifier.get(); }

	ParamFloat missileShipModifier	= new ParamFloat(MOD_UI, "MISSILE_SHIP_MODIFIER", 2f/3f)
			.setDefaultValue(MOO1_DEFAULT, 1f)
			.setDefaultValue(ROTP_DEFAULT, 1f)
			.setLimits(0.1f, 2f)
			.setIncrements(0.01f, 0.05f, 0.2f)
			.cfgFormat("0.##")
			.guiFormat("%")
			.formerName(MOD_UI+"MISSILE_SIZE_MODIFIER");
	default float selectedMissileShipModifier()	{ return missileShipModifier.get(); }

	ParamBoolean challengeMode		= new ParamBoolean(MOD_UI, "CHALLENGE_MODE", false);
	default boolean selectedChallengeMode()		{ return challengeMode.get(); }
	
	ParamFloat counciRequiredPct	= new ParamFloat(MOD_UI, "COUNCIL_REQUIRED_PCT", 2f/3f)
			.setLimits(0f, 0.99f)
			.setIncrements(0.01f/3f, 0.02f, 0.1f)
			.cfgFormat("0.##")
			.guiFormat("‰");

	ParamInteger bombingTarget		= new ParamInteger(MOD_UI, "BOMBING_TARGET", 2)
			.setLimits(0, 300)
			.setIncrements(1, 5, 20);
	default int selectedBombingTarget()			{ return bombingTarget.get(); }

	ParamList autoTerraformEnding	= new ParamList( MOD_UI, "AUTO_TERRAFORM_ENDING", "Populated")
			.showFullGuide(true)
			.put("Populated",	MOD_UI + "TERRAFORM_POPULATED")
			.put("Terraformed",	MOD_UI + "TERRAFORM_TERRAFORMED")
			.put("Cleaned",		MOD_UI + "TERRAFORM_CLEANED");
	default String selectedAutoTerraformEnding()	{ return autoTerraformEnding.get(); }

	ParamBoolean trackUFOsAcrossTurns = new ParamBoolean(MOD_UI, "TRACK_UFOS_ACROSS_TURNS", false);
	default boolean selectedTrackUFOsAcrossTurns()	{ return trackUFOsAcrossTurns.get(); }

	ParamBoolean allowTechStealing	= new ParamBoolean(MOD_UI, "ALLOW_TECH_STEALING", true);
	default boolean forbidTechStealing()	 	{ return !allowTechStealing.get(); }

	ParamInteger maxTechsCaptured	= new MaxTechsCaptured()
			.setLimits(0, 200)
			.setIncrements(1, 5, 20);
	default int maxTechsCaptured()				{ return maxTechsCaptured.get(); }
	final class MaxTechsCaptured extends ParamInteger {
		MaxTechsCaptured() {
			super(MOD_UI, "MAX_TECH_CAPTURED", 6);
			setLimits(0, 10);
			setIncrements(1, 2, 5);
			loop(true);
		}
		// For backward compatibility
		@Override protected Integer getOptionValue(IGameOptions options) {
			Integer value = options.dynOpts().getInteger(getLangLabel());
			if (value == null) {
				Boolean allowTechStealing = options.dynOpts().getBoolean(MOD_UI + "ALLOW_TECH_STEALING");
				if (allowTechStealing == null)
					allowTechStealing = true;
				if (allowTechStealing)
					value = defaultValue();
				else
					value = 0;
			}
			return value;
		}
	}

	ParamInteger maxSecurityPct		= new ParamInteger(MOD_UI, "MAX_SECURITY_PCT", 10)
			.setLimits(10, 90)
			.setIncrements(1, 5, 20)
			.pctValue(true);
	default int selectedMaxSecurityPct()		{ return maxSecurityPct.get(); }

	ParamList darkGalaxy			= new ParamList( MOD_UI, "DARK_GALAXY", "No")
			.showFullGuide(true)
			.isValueInit(false)
			.put("No",		MOD_UI + "DARK_GALAXY_NO")
			.put("Shrink",	MOD_UI + "DARK_GALAXY_SHRINK")
			.put("NoSpy",	MOD_UI + "DARK_GALAXY_NO_SPY")
			.put("Spy",		MOD_UI + "DARK_GALAXY_SPY");
	default boolean selectedDarkGalaxy()	{
		return !darkGalaxy.get().equalsIgnoreCase("No") 
				&& GameSession.instance().inProgress(); // for the final replay
	}
	default boolean darkGalaxySpy()			{ return darkGalaxy.get().equalsIgnoreCase("Spy"); }
	default boolean darkGalaxyNoSpy()		{ return darkGalaxy.get().equalsIgnoreCase("NoSpy"); }
	default boolean darkGalaxyDark()		{ return darkGalaxy.get().equalsIgnoreCase("Shrink"); }
	
	ParamList transportAutoEco			= new ParamList( MOD_UI, "TRANSPORT_AUTO_ECO", "No")
			.showFullGuide(true)
			.isValueInit(false)
			.put("No",	 MOD_UI + "TRANSPORT_AUTO_ECO_NO")
			.put("Yes",	 MOD_UI + "TRANSPORT_AUTO_ECO_YES")
			.put("Last", MOD_UI + "TRANSPORT_AUTO_ECO_LAST");
	default boolean transportAutoEcoDefaultNo()	{ return transportAutoEco.get().equals("No"); }
	default boolean transportAutoEcoDefaultYes(){ return transportAutoEco.get().equals("Yes"); }
	default boolean transportAutoEcoLast()		{ return transportAutoEco.get().equals("Last"); }

	ParamBoolean spyOverSpend			= new ParamBoolean(MOD_UI, "SPY_OVERSPEND", true);
	default boolean spyOverSpend()				{ return spyOverSpend.get(); }

	ParamList councilPlayerVote			= new ParamList( MOD_UI, "COUNCIL_PLAYER_VOTE", "By Size")
			.showFullGuide(true)
			.put("First",	MOD_UI + "COUNCIL_PLAYER_VOTE_FIRST")
			.put("By Size",	MOD_UI + "COUNCIL_PLAYER_VOTE_SIZE")
			.put("Last",	MOD_UI + "COUNCIL_PLAYER_VOTE_LAST");
	default boolean playerVotesFirst()	{ return councilPlayerVote.get().equalsIgnoreCase("First"); }
	default boolean playerVotesLast()	{ return councilPlayerVote.get().equalsIgnoreCase("Last"); }

	String END_OF_GAME				= "END_OF_GAME";
	String END_OF_GAME_NORMAL		= END_OF_GAME + "_NORMAL";
	String END_OF_GAME_TURN_COUNCIL	= END_OF_GAME + "_TURN_COUNCIL";
	String END_OF_GAME_TURN_POP		= END_OF_GAME + "_TURN_POP";
	String END_OF_GAME_TURN_TEC_IND	= END_OF_GAME + "_TURN_TEC_IND";
	String END_OF_GAME_TURN_POWER	= END_OF_GAME + "_TURN_POWER";
	ParamList endOfGameCondition	= new ParamList( MOD_UI, END_OF_GAME, END_OF_GAME_NORMAL)
			.showFullGuide(true)
			.setUpdateParameters(IInGameOptions::tagEndOfGameTurn, "")
			.put(END_OF_GAME_NORMAL,		MOD_UI + END_OF_GAME_NORMAL)
			.put(END_OF_GAME_TURN_COUNCIL,	MOD_UI + END_OF_GAME_TURN_COUNCIL)
			.put(END_OF_GAME_TURN_POP,		MOD_UI + END_OF_GAME_TURN_POP)
			.put(END_OF_GAME_TURN_TEC_IND,	MOD_UI + END_OF_GAME_TURN_TEC_IND)
			.put(END_OF_GAME_TURN_POWER,	MOD_UI + END_OF_GAME_TURN_POWER);
	default boolean turnLimitedEndOfGame()	{ return !endOfGameCondition.get().equals(END_OF_GAME_NORMAL); }
	default boolean turnLimitedCouncil()	{ return endOfGameCondition.get().equals(END_OF_GAME_TURN_COUNCIL); }
	default String endOfGameCondition()		{ return endOfGameCondition.get(); }

	final class ParamEndOfGameTurn extends ParamInteger	{
		public ParamEndOfGameTurn()	{
			super(MOD_UI, "END_OF_GAME_TURN", 200);
			setLimits(10, 10000);
			setIncrements(10, 50, 200);
			pctValue(false);
		}
		@Override public boolean isGhost()	{ return endOfGameCondition.get().equals(END_OF_GAME_NORMAL); }
	}
	ParamEndOfGameTurn endOfGameTurn	= new ParamEndOfGameTurn();
	default int selectedEndOfGameTurn()		{ return endOfGameTurn.get(); }
	static void tagEndOfGameTurn(String id)	{ endOfGameTurn.updated(true); }

	ParamBoolean defaultForwardRally	= new ParamBoolean(MOD_UI, "DEFAULT_FORWARD_RALLY", true);
	default boolean defaultForwardRally()	{ return defaultForwardRally.get(); }

	ParamBoolean defaultChainRally		= new ParamBoolean(MOD_UI, "DEFAULT_CHAIN_RALLY", true);
	default boolean defaultChainRally()		{ return defaultChainRally.get(); }
	
	String CHAIN_RALLY_SPEED_FLEET	= "FLEET";
	String CHAIN_RALLY_SPEED_MIN	= "MIN";
	String CHAIN_RALLY_SPEED_TOP	= "TOP";
	ParamList chainRallySpeed		= new ParamList( MOD_UI, "CHAIN_RALLY_SPEED", CHAIN_RALLY_SPEED_FLEET)
			.showFullGuide(true)
			.put(CHAIN_RALLY_SPEED_FLEET,	MOD_UI + "CHAIN_RALLY_SPEED_FLEET")
			.put(CHAIN_RALLY_SPEED_MIN,		MOD_UI + "CHAIN_RALLY_SPEED_MIN")
			.put(CHAIN_RALLY_SPEED_TOP,		MOD_UI + "CHAIN_RALLY_SPEED_TOP");
	default String chainRallySpeed()		{ return chainRallySpeed.get(); }
	default Float chainRallySpeed(Empire player, ShipFleet fleet)	{
		if (fleet == null)
			return chainRallySpeed(player);
		switch (chainRallySpeed.get().toUpperCase()) {
			case CHAIN_RALLY_SPEED_FLEET :
				return fleet.slowestStackSpeed();
			case CHAIN_RALLY_SPEED_MIN :
				return fleet.empire().minActiveDesignSpeed();
			case CHAIN_RALLY_SPEED_TOP :
				return fleet.empire().tech().topSpeed();
		}
		return null;
	}
	default Float chainRallySpeed(Empire player, ShipDesign design)	{
		if (design == null)
			return chainRallySpeed(player);
		switch (chainRallySpeed.get().toUpperCase()) {
			case CHAIN_RALLY_SPEED_FLEET :
				return (float) design.warpSpeed();
			case CHAIN_RALLY_SPEED_MIN :
				return player.minActiveDesignSpeed();
			case CHAIN_RALLY_SPEED_TOP :
				return player.tech().topSpeed();
		}
		return null;
	}
	default Float chainRallySpeed(Empire player)	{
		switch (chainRallySpeed.get().toUpperCase()) {
			case CHAIN_RALLY_SPEED_FLEET :
			case CHAIN_RALLY_SPEED_MIN :
				return player.minActiveDesignSpeed();
			case CHAIN_RALLY_SPEED_TOP :
				return player.tech().topSpeed();
		}
		return null;
	}

	String PEACE_TREATY_NORMAL		= "NORMAL";
	String PEACE_TREATY_ARMISTICE	= "NOWAR";
	String PEACE_TREATY_COLD_WAR	= "TRUCE";
	ParamList specialPeaceTreaty	= new ParamList( MOD_UI, "SPECIAL_PEACE_TREATY", PEACE_TREATY_NORMAL)
			.showFullGuide(true)
			.put(PEACE_TREATY_NORMAL,	 MOD_UI + "SPECIAL_PEACE_TREATY_NORMAL")
			.put(PEACE_TREATY_ARMISTICE, MOD_UI + "SPECIAL_PEACE_TREATY_NOWAR")
			.put(PEACE_TREATY_COLD_WAR,	 MOD_UI + "SPECIAL_PEACE_TREATY_TRUCE");
	default boolean allowPeaceTreaty()	{ return !specialPeaceTreaty.get().equalsIgnoreCase(PEACE_TREATY_ARMISTICE) ;}
	default boolean isColdWarMode()		{ return specialPeaceTreaty.get().equalsIgnoreCase(PEACE_TREATY_COLD_WAR) ;}

	String DEVELOPED_ALL		= "ALL";
	String DEVELOPED_NO_BASE	= "NO_BASES";
	String DEVELOPED_INDUSTRY	= "INDUSTRY";
	ParamList developedDefinition	= new ParamList(MOD_UI, "DEVELOPED_DEFINITION", DEVELOPED_ALL)
			.showFullGuide(true)
			.put(DEVELOPED_ALL, 	 MOD_UI + "DEVELOPED_ALL")
			.put(DEVELOPED_NO_BASE,  MOD_UI + "DEVELOPED_NO_BASE")
			.put(DEVELOPED_INDUSTRY, MOD_UI + "DEVELOPED_INDUSTRY");
	default boolean isDeveloped(Colony col)	{
		switch (developedDefinition.get()) {
		case DEVELOPED_NO_BASE:
			return col.industry().isCompleted(maxMissingFactories())
					&& col.ecology().isCompleted(maxMissingPopulation());
		case DEVELOPED_INDUSTRY:
			return col.industry().isCompleted(maxMissingFactories());
		case DEVELOPED_ALL:
		default:
			return col.defense().isCompleted(0)
					&& col.industry().isCompleted(maxMissingFactories())
					&& col.ecology().isCompleted(maxMissingPopulation());
		}
	}
	ParamInteger maxMissingPopulation	= new ParamInteger(MOD_UI, "DEV_MAX_MISSING_POP", 3)
			.setDefaultValue(MOO1_DEFAULT, 0)
			.setDefaultValue(ROTP_DEFAULT, 0)
			.setLimits(0, 50)
			.setIncrements(1, 5, 20);
	default int maxMissingPopulation()	{ return maxMissingPopulation.get(); }	

	ParamInteger maxMissingFactories	= new ParamInteger(MOD_UI, "DEV_MAX_MISSING_FACT", 3)
			.setDefaultValue(MOO1_DEFAULT, 0)
			.setDefaultValue(ROTP_DEFAULT, 0)
			.setLimits(0, 50)
			.setIncrements(1, 5, 20);
	default int maxMissingFactories()	{ return maxMissingFactories.get(); }	

	String RALLY_COMBAT_NEVER	= "RALLY_COMBAT_NEVER";
	String RALLY_COMBAT_BUILT	= "RALLY_COMBAT_BUILT";
	String RALLY_COMBAT_ALL		= "RALLY_COMBAT_ALL";
	String RALLY_COMBAT_PASS_BY	= "RALLY_COMBAT_PASS_BY";
	ParamList rallyCombat			= new ParamList( MOD_UI, "RALLY_COMBAT", RALLY_COMBAT_ALL)
			.isCfgFile(true)
			.showFullGuide(true)
			.put(RALLY_COMBAT_NEVER,	MOD_UI + RALLY_COMBAT_NEVER)
			.put(RALLY_COMBAT_BUILT,	MOD_UI + RALLY_COMBAT_BUILT)
			.put(RALLY_COMBAT_PASS_BY,	MOD_UI + RALLY_COMBAT_PASS_BY)
			.put(RALLY_COMBAT_ALL,		MOD_UI + RALLY_COMBAT_ALL)
			.setDefaultValue(FUSION_DEFAULT, RALLY_COMBAT_ALL)
			.setDefaultValue(MOO1_DEFAULT,   RALLY_COMBAT_BUILT)
			.setDefaultValue(ROTP_DEFAULT,   RALLY_COMBAT_BUILT);
	default boolean rallyJoinCombat()		{ return !rallyCombat.get().equals(RALLY_COMBAT_NEVER); }
	default boolean rallyBuiltJoinCombat()	{ return rallyCombat.get().equals(RALLY_COMBAT_BUILT) || rallyAllJoinCombat(); }
	default boolean rallyTransitJoinCombat(){ return rallyCombat.get().equals(RALLY_COMBAT_PASS_BY) || rallyAllJoinCombat(); }
	default boolean rallyAllJoinCombat()	{ return rallyCombat.get().equals(RALLY_COMBAT_ALL); }

	String COMBAT_LOSS_DEFENSES	= "COMBAT_LOSS_DEFENSE";
	String COMBAT_LOSS_RALLY	= "COMBAT_LOSS_RALLY";
	String COMBAT_LOSS_SHARED	= "COMBAT_LOSS_SHARED";
	ParamList rallyCombatLoss	= new ParamList( MOD_UI, "RALLY_COMBAT_LOSS", COMBAT_LOSS_DEFENSES)
			.isCfgFile(true)
			.showFullGuide(true)
			.put(COMBAT_LOSS_DEFENSES,	MOD_UI + COMBAT_LOSS_DEFENSES)
			.put(COMBAT_LOSS_RALLY,		MOD_UI + COMBAT_LOSS_RALLY)
			.put(COMBAT_LOSS_SHARED,	MOD_UI + COMBAT_LOSS_SHARED);
	default String rallyLosses()		{ return rallyCombatLoss.get(); }
	default boolean rallyLossDefense()	{ return rallyCombatLoss.get().equals(COMBAT_LOSS_DEFENSES); }
	default boolean rallyLossRally()	{ return rallyCombatLoss.get().equals(COMBAT_LOSS_RALLY); }
	default boolean rallyLossShared()	{ return rallyCombatLoss.get().equals(COMBAT_LOSS_SHARED); }

	ParamBoolean useSmartRefit		= new ParamBoolean(MOD_UI, "USE_SMART_REFIT", true);
	default boolean useSmartRefit()		{ return useSmartRefit.get(); }

	String MAX_LANDING_UNLIMITED	= "MAX_LANDING_UNLIMITED";
	String MAX_LANDING_MULTIPLER	= "MAX_LANDING_MULTIPLER";
	String MAX_LANDING_FIXED		= "MAX_LANDING_FIXED";
	ParamList maxLandingTroops	= new ParamList( MOD_UI, "MAX_LANDING_TROOPS", MAX_LANDING_UNLIMITED)
			.showFullGuide(true)
			.setDefaultValue(MOO1_DEFAULT, MAX_LANDING_FIXED)
			.isValueInit(false)
			.put(MAX_LANDING_UNLIMITED,	MOD_UI + MAX_LANDING_UNLIMITED)
			.put(MAX_LANDING_FIXED,		MOD_UI + MAX_LANDING_FIXED)
			.put(MAX_LANDING_MULTIPLER,	MOD_UI + MAX_LANDING_MULTIPLER);
	ParamInteger maxLandingTroopsAmount	= new ParamInteger(MOD_UI, "MAX_LANDING_AMOUNT", 300)
			.setDefaultValue(MOO1_DEFAULT, 300)
			.setLimits(0, 10000)
			.setIncrements(10, 50, 200);
	ParamInteger maxLandingTroopsFactor	= new ParamInteger(MOD_UI, "MAX_LANDING_FACTOR", 200)
			.setLimits(0, 10000)
			.setIncrements(10, 50, 200);
	ParamInteger maxLandingTroopsIAFactor	= new ParamInteger(MOD_UI, "MAX_LANDING_IA_FACTOR", 100)
			.setLimits(0, 1000)
			.setIncrements(5, 20, 100);
	default float maxLandingTroops(StarSystem sys, boolean isPlayer)	{
		float playerLimit = 0;
		switch (maxLandingTroops.get()) {
			case MAX_LANDING_FIXED:
				playerLimit = maxLandingTroopsAmount.get();
			case MAX_LANDING_MULTIPLER:
				playerLimit = maxLandingTroopsFactor.get() * sys.planet().currentSize() / 100;
			case MAX_LANDING_UNLIMITED:
			default:
				playerLimit = Integer.MAX_VALUE;
		}
		if (isPlayer)
			return playerLimit;
		else
			return playerLimit * maxLandingTroopsIAFactor.get() / 100;
	}
	String AGGRESSIV_NORMAL		= "AGGRESSIV_NORMAL";
	String AGGRESSIV_AI_WAR_OK	= "AGGRESSIV_AI_WAR_OK";	// AI can declare war to AI but not to player
	String AGGRESSIV_AI_NO_WAR	= "AGGRESSIV_AI_NO_WAR";	// No war between AI
	String AGGRESSIV_NEVER_WAR	= "AGGRESSIV_NEVER_WAR";	// Player can't declare war neither
	String AGGRESSIV_ALWAYS_WAR	= "AGGRESSIV_ALWAYS_WAR";	// All empire are permanently at war
	String AGGRESSIV_ALLIANCE	= "AGGRESSIV_ALLIANCE"; 	// All empire are permanently Allied
	ParamList gameAgressiveness	= new ParamList( MOD_UI, "GAME_AGGRESSIVENESS", AGGRESSIV_NORMAL)
			.isCfgFile(true)
			.showFullGuide(true)
			.put(AGGRESSIV_NORMAL,		MOD_UI + AGGRESSIV_NORMAL)
			.put(AGGRESSIV_AI_WAR_OK,	MOD_UI + AGGRESSIV_AI_WAR_OK)
			.put(AGGRESSIV_AI_NO_WAR,	MOD_UI + AGGRESSIV_AI_NO_WAR)
			.put(AGGRESSIV_NEVER_WAR,	MOD_UI + AGGRESSIV_NEVER_WAR)
			.put(AGGRESSIV_ALLIANCE,	MOD_UI + AGGRESSIV_ALLIANCE)
			.put(AGGRESSIV_ALWAYS_WAR,	MOD_UI + AGGRESSIV_ALWAYS_WAR);
	
	default boolean alwaysAlly()	{ return gameAgressiveness.get().equals(AGGRESSIV_ALLIANCE); }
	default boolean alwaysAtWar()	{ return gameAgressiveness.get().equals(AGGRESSIV_ALWAYS_WAR); }
	default boolean canStopWar()	{ return !alwaysAtWar(); }
	default boolean canStartWar(Empire ask, Empire target)	{ return canStartWar(ask.isPlayer(), target.isPlayer()); }
	default boolean canStartWar(boolean askIsPlayer, boolean targetIsPlayer)	{
		// Player vs AI
		if (askIsPlayer)
			switch (gameAgressiveness.get()) {
				case AGGRESSIV_NEVER_WAR:
					return false;
				default:
					return true;
			}
		// AI vs Player
		if (targetIsPlayer)
			switch (gameAgressiveness.get()) {
				case AGGRESSIV_NEVER_WAR:
				case AGGRESSIV_AI_WAR_OK:
				case AGGRESSIV_AI_NO_WAR:
					return false;
				default:
					return true;
			}
		// AI vs AI
		switch (gameAgressiveness.get()) {
			case AGGRESSIV_NEVER_WAR:
				return false;
			default:
				return true;
		}
	}
	ParamBoolean skirmishesAllowed		= new ParamBoolean(MOD_UI, "SKIRMISHES_ALLOWED", true);
	default boolean skirmishesAllowed()	{ return skirmishesAllowed.get(); }
	default boolean skirmishesAllowed(Empire ask, Empire target)	{
		if (skirmishesAllowed.get())
			return true;
		EmpireView view = ask.viewForEmpire(target);
		if (view != null && view.embassy().war())
			return true;

		boolean askIsPlayer		= ask.isPlayer();
		boolean targetIsPlayer	= target.isPlayer();
		if (askIsPlayer)
			switch (gameAgressiveness.get()) {
				case AGGRESSIV_NEVER_WAR:
					return false;
				case AGGRESSIV_AI_WAR_OK:
				case AGGRESSIV_AI_NO_WAR:
					// if AI can't attack player, and skirmish are not allowed:
					// Player can't start skirmish either. Player should declare war first!
					return skirmishesAllowed.get();
				default:
					return true;
			}
		// AI vs Player
		if (targetIsPlayer)
			switch (gameAgressiveness.get()) {
				case AGGRESSIV_NEVER_WAR:
				case AGGRESSIV_AI_WAR_OK:
				case AGGRESSIV_AI_NO_WAR:
					return skirmishesAllowed.get();
				default:
					return true;
			}
		// AI vs AI
		switch (gameAgressiveness.get()) {
			case AGGRESSIV_NEVER_WAR:
				return skirmishesAllowed.get();
			default:
				return true;
		}
	}

	String CLOSEST_COLONY	= "CLOSEST_COLONY";
	String CLOSEST_ALLY		= "CLOSEST_ALLY";
	String ANY_ALLY			= "ANY_ALLY";
	String ANY_STAR_SYSTEM	= "ANY_STAR_SYSTEM";
	ParamList retreatDestination	= new ParamList( MOD_UI, "RETREAT_DESTINATION", ANY_ALLY)
			.showFullGuide(true)
			.put(CLOSEST_COLONY,	MOD_UI + CLOSEST_COLONY)
			.put(CLOSEST_ALLY,		MOD_UI + CLOSEST_ALLY)
			.put(ANY_ALLY,			MOD_UI + ANY_ALLY)
			.put(ANY_STAR_SYSTEM,	MOD_UI + ANY_STAR_SYSTEM)
			.setDefaultValue(FUSION_DEFAULT, ANY_ALLY)
			.setDefaultValue(MOO1_DEFAULT, CLOSEST_COLONY)
			.setDefaultValue(ROTP_DEFAULT, ANY_ALLY);
	default boolean retreatToAnyPlanet()	{ return retreatDestination.get().equals(ANY_STAR_SYSTEM); }
	default boolean retreatOnlyToAlly()		{ 
		switch (retreatDestination.get()) {
			case CLOSEST_ALLY:
			case ANY_ALLY:
				return true;
			default:
				return false;
		}
	}
	default boolean retreatClosestOnly()	{ 
		switch (retreatDestination.get()) {
			case CLOSEST_COLONY:
			case CLOSEST_ALLY:
				return true;
			default:
				return false;
		}
	}

	ParamBoolean hyperComRetreatExtended	= new ParamBoolean(MOD_UI, "HYPER_COM_RETREAT_EXT", true)
			.setDefaultValue(FUSION_DEFAULT, false)
			.setDefaultValue(MOO1_DEFAULT, false)
			.setDefaultValue(ROTP_DEFAULT, false);
	default boolean hyperComRetreatExtended()			{ return hyperComRetreatExtended.get(); }
	ParamBoolean noEnemyOnRetreatDestination	= new ParamBoolean(MOD_UI, "NO_RETREAT_ENEMY_DESTINATION", false);
	default boolean noEnemyOnRetreatDestination()		{ return noEnemyOnRetreatDestination.get(); }

//	ParamBoolean markRetreatOnArrivalAsRetreating	= new ParamBoolean(MOD_UI, "RETREAT_ON_ARRIVAL_RETREATING", true);
//	default boolean markRetreatOnArrivalAsRetreating()	{ return markRetreatOnArrivalAsRetreating.get(); }
//	ParamBoolean markDiplomaticRetreatAsRetreating	= new ParamBoolean(MOD_UI, "DIPLOMATIC_RETREAT_RETREATING", true);
//	default boolean markDiplomaticRetreatAsRetreating()	{ return markDiplomaticRetreatAsRetreating.get(); }
}
