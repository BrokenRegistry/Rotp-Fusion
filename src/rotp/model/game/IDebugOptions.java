package rotp.model.game;

import static rotp.ui.UserPreferences.showMemory;

import rotp.Rotp;
import rotp.model.empires.Empire;
import rotp.ui.RotPUI;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;
import rotp.ui.vipconsole.VIPConsole;

public interface IDebugOptions extends IBaseOptsTools {
    String MEMORY_LOGFILE	 = "AutoRunMemory.txt";
    String AUTORUN_LOGFILE	 = "AutoRunPlayer.txt";
    String NOTIF_LOGFILE	 = "AutoRunNotifications.txt";
    String AUTORUN_OTHERFILE = "AutoRunOther.txt";
    String AUTORUN_EVENTS	 = "AutoRunEvents.txt";
    String AUTORUN_BENCHMARK = "AutoRunBenchmark.txt";

	ParamBoolean debugShowMemory	= new DebugShowMemory();
	class DebugShowMemory extends ParamBoolean {
		DebugShowMemory() {
			super(GAME_UI, "MEMORY", false);
			isDuplicate(true);
			isCfgFile(true);
		}
		@Override public String getCfgLabel()		{ return "SHOW_MEMORY"; }
		@Override public Boolean getOption()		{ return showMemory(); }
		@Override public void setOption(Boolean b)	{ showMemory(b); }
	}
	static boolean debugShowMemory()		{ return debugShowMemory.get(); }

    ParamBoolean debugShowMoreMemory	= new ParamBoolean(GAME_UI, "MORE_MEMORY", false, true, true);
	static boolean debugShowMoreMemory()	{ return debugShowMoreMemory.get(); }

	ParamBoolean debugConsoleMemory	= new ParamBoolean(GAME_UI, "MEMORY_CONSOLE", false, true, true);
	static boolean debugConsoleMemory()	{ return debugConsoleMemory.get(); }

	ParamBoolean debugFileMemory	= new ParamBoolean(GAME_UI, "MEMORY_FILE", false, true, true);
	static boolean debugFileMemory()		{ return debugFileMemory.get(); }

	ParamList debugAutoRun			= new ParamList( MOD_UI, "DEBUG_AUTO_RUN", "Off")
		.isDuplicate(false)
		.isCfgFile(true)
		.showFullGuide(true)
		.put("Off",		MOD_UI + "DEBUG_AUTO_RUN_OFF")
		.put("On",		MOD_UI + "DEBUG_AUTO_RUN_ON")
		.put("Bench",	MOD_UI + "DEBUG_AUTO_RUN_BENCH")
		.put("End",		MOD_UI + "DEBUG_AUTO_RUN_END");
	static boolean debugAutoRun()	{ return !debugAutoRun.get().equalsIgnoreCase("Off"); }
	static boolean debugBenchmark()	{ return debugAutoRun.get().equalsIgnoreCase("Bench"); }
	static void	debugBMContinue()	{ debugAutoRun.set("Bench"); }
	static boolean debugBMBreak()	{ return debugAutoRun.get().equalsIgnoreCase("End"); }

	ParamList debugARContinueOnLoss	= new ParamList( MOD_UI, "DEBUG_AR_CONTINUE_ON_LOSS", "BMOnly")
		.isDuplicate(false)
		.isCfgFile(true)
		.showFullGuide(true)
		.put("Always",	MOD_UI + "DEBUG_AR_CONTINUE_ON_LOSS_ALWAYS")
		.put("Never",	MOD_UI + "DEBUG_AR_CONTINUE_ON_LOSS_NEVER")
		.put("BMOnly",	MOD_UI + "DEBUG_AR_CONTINUE_ON_LOSS_BM_ONLY");

	static boolean debugARStopOnLoss()	{
		switch (debugARContinueOnLoss.get()) {
		case "Never":
			return true;
		case "Always":
			return false;
		case "BMOnly":
			return !debugBenchmark();
		}
		return true;
	}
	ParamList debugBMShowAll		= new ParamList( MOD_UI, "DEBUG_BM_SHOW_ALL", "Off")
		.isDuplicate(false)
		.isCfgFile(true)
		.showFullGuide(true)
		.put("Off",		MOD_UI + "DEBUG_BM_SHOW_ALL_OFF")
		.put("Alway",	MOD_UI + "DEBUG_BM_SHOW_ALL_ALWAY")
		.put("Lost",		MOD_UI + "DEBUG_BM_SHOW_ALL_LOSS");
	static boolean debugBMShowAll()	{
		switch (debugBMShowAll.get()) {
		case "Off":
			return false;
		case "Alway":
			return true;
		case "Lost":
			return GameSession.instance().status().lost();
		}
		return false;
	}
	ParamInteger debugBMMaxTurns	= new ParamInteger(MOD_UI, "DEBUG_BM_MAX_TURNS", -1)
			.setLimits(-1, null)
			.setIncrements(1, 10, 100)
			.isCfgFile(true)
			.specialNegative(NEGATIVE_DISABLED);
	static int debugBMMaxTurns()			{ return debugBMMaxTurns.get(); }
	
	ParamInteger debugBMLostTurns	= new ParamInteger(MOD_UI, "DEBUG_BM_LOST_TURNS", -1)
			.setLimits(-1, null)
			.setIncrements(1, 10, 100)
			.isCfgFile(true)
			.specialNegative(NEGATIVE_DISABLED);
	static int debugBMLostTurns()			{ return debugBMLostTurns.get(); }

	ParamBoolean debugBMZoomOut		= new ParamBoolean(MOD_UI, "DEBUG_BM_ZOOM_OUT", false, true, true);
	static boolean debugBMZoomOut()		{ return debugBMZoomOut.get(); }

	ParamBoolean debugNoAutoSave	= new ParamBoolean(MOD_UI, "DEBUG_NO_AUTOSAVE", false, true, true);
	static boolean debugNoAutoSave()		{ return debugNoAutoSave.get(); }

	ParamBoolean consoleAutoRun		= new ParamBoolean(GAME_UI, "CONSOLE_AUTO_PLAY", false, true, true);
	static boolean consoleAutoRun()		{ return consoleAutoRun.get(); }

	ParamBoolean debugLogNotif		= new ParamBoolean(GAME_UI, "DEBUG_LOG_NOTIF", true, true, true);
	static boolean debugLogNotif()			{ return debugLogNotif.get(); }

	ParamBoolean debugLogEvents		= new ParamBoolean(GAME_UI, "DEBUG_LOG_EVENTS", true, true, true);
	static boolean debugLogEvents()		{ return debugLogEvents.get(); }

	ParamBoolean showVIPPanel		= new ShowVIPPanel();
	class ShowVIPPanel extends ParamBoolean {
		ShowVIPPanel() {
			super(GAME_UI, "SHOW_CONSOLE_PANEL", false);
			isDuplicate(false);
			isCfgFile(true);
		}
		@Override public Boolean set(Boolean newValue) {
			super.set(newValue);
			RotPUI.useDebugFile = newValue;
			RotPUI.isVIPConsole = newValue;
			VIPConsole.showConsole(newValue);
			return newValue;
		}
	}
	static boolean selectedShowVIPPanel()	{ return showVIPPanel.get(); }
	static void showVIPPanel()				{ VIPConsole.showConsole(selectedShowVIPPanel()); }
	static void showVIPPanel(boolean b)	{
		showVIPPanel.set(b);
		showVIPPanel();
	}

	ParamBoolean continueAnyway		= new ParamBoolean(GAME_UI, "CONTINUE_ANYWAY", false, false);
	default void continueAnyway(boolean b)	{ continueAnyway.set(b); }
	default boolean continueAnyway()		{ return continueAnyway.get(); }

	ParamInteger debugPlayerEmpire	= new DebugPlayerEmpire();
	default void debugPlayerEmpire(int id)	{ debugPlayerEmpire.set(id); }
	default int debugPlayerEmpire()			{ return debugPlayerEmpire.get(); }
	class DebugPlayerEmpire extends ParamInteger {
		DebugPlayerEmpire() {
			super(MOD_UI, "DEBUG_PLAYER_EMPIRE", Empire.DEFAULT_PLAYER_ID);
			isValueInit(false);
			isCfgFile(false);
			setLimits(0, 49);
			setIncrements(1, 5, 20);
		}
		@Override public Integer set(Integer newVal)	{
			int val = super.set(newVal);
			if (Rotp.noOptions())
				return val;
			else {
				GameSession session = GameSession.instance();
				if (session.status().inProgress())
					session.galaxy().playerSwapRequest(val);;
				}
			return val;
		}
		@Override public String guideSelectedValue()		{
			String str = super.guideSelectedValue();
			if (Rotp.noOptions())
				return str;
			else {
				GameSession session = GameSession.instance();
				if (session.status().inProgress())
					str += session.galaxy().getEmpireList();
				}
			return str;
		}
	}
}
