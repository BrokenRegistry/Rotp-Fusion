package rotp.model.game;

import rotp.ui.options.ISubUiKeys;
import rotp.ui.util.ParamSpacer;
import rotp.ui.util.ParamTitle;

public interface IBaseOptsTools extends ISubUiKeys{
	String BASE_UI				= "SETUP_";
	String GAME_UI				= "GAME_SETTINGS_";
	String ADV_UI				= "SETTINGS_";
	String MOD_UI				= "SETTINGS_MOD_";
	String AUTO_SHIP_DESIGN		= "AUTO_SD_";
	String HEADERS				= "HEADERS_";	
	String OPTIONFILE_EXTENSION	= ".options";
	String GAME_OPTIONS_FILE	= "Game" + OPTIONFILE_EXTENSION;
	String LAST_OPTIONS_FILE	= "Last" + OPTIONFILE_EXTENSION;
	String LIVE_OPTIONS_FILE	= "Live" + OPTIONFILE_EXTENSION;
	String USER_OPTIONS_FILE	= "User" + OPTIONFILE_EXTENSION;
	String NEGATIVE_DISABLED	= "SETTINGS_MOD_NEGATIVE_DISABLED";
	String FUSION_DEFAULT		= DefaultValues.FUSION_DEFAULT;
	String MOO1_DEFAULT			= DefaultValues.MOO1_DEFAULT;
	String ROTP_DEFAULT			= DefaultValues.ROTP_DEFAULT;
	// To be able to identify the current options
	int UNKNOWN_ID = 0;
	int GAME_ID  = 1;
	int SETUP_ID = 2;

	ParamTitle RELEVANT_TITLE					= new ParamTitle("RELEVANT_PARAMETERS");
	ParamTitle GOVERNOR_BASE_OPTIONS_TITLE		= new ParamTitle("GOVERNOR_BASE_OPTIONS");
	ParamTitle GOVERNOR_ADVANCED_OPTIONS_TITLE	= new ParamTitle("GOVERNOR_ADVANCED_OPTIONS");

	ParamSpacer HEADER_SPACER_100	= new ParamSpacer(1f);
	ParamSpacer HEADER_SPACER_50	= new ParamSpacer(0.5f);
	ParamSpacer LINE_SPACER_25		= new ParamSpacer(0.25f);
	default ParamSpacer headerSpacer()	{ return HEADER_SPACER_50; }
	default SafeListParam spacerList()	{
		SafeListParam list = new SafeListParam("SPACERS");
		list.add(HEADER_SPACER_100);
		list.add(HEADER_SPACER_50);
		list.add(LINE_SPACER_25);
		return list;
	}
}
