package rotp.model.game;

import static rotp.model.galaxy.StarSystem.getMinFont;
import static rotp.model.galaxy.StarSystem.setFontPct;
import static rotp.model.galaxy.StarSystem.setMinFont;
import static rotp.model.galaxy.StarSystem.setMinFont2;
import static rotp.model.game.DefaultValues.DEF_VAL;
import static rotp.ui.UserPreferences.backupTurns;
import static rotp.ui.UserPreferences.disableAdvisor;
import static rotp.ui.UserPreferences.displayMode;
import static rotp.ui.UserPreferences.displayModeToSettingName;
import static rotp.ui.UserPreferences.graphicsMode;
import static rotp.ui.UserPreferences.graphicsModeToSettingName;
import static rotp.ui.UserPreferences.musicVolume;
import static rotp.ui.UserPreferences.playMusic;
import static rotp.ui.UserPreferences.playSounds;
import static rotp.ui.UserPreferences.saveDir;
import static rotp.ui.UserPreferences.saveDirectoryPath;
import static rotp.ui.UserPreferences.selectedScreen;
import static rotp.ui.UserPreferences.sensitivityMode;
import static rotp.ui.UserPreferences.sensitivityToSettingName;
import static rotp.ui.UserPreferences.soundVolume;
import static rotp.ui.UserPreferences.texturesMode;
import static rotp.ui.UserPreferences.texturesToSettingName;
import static rotp.ui.main.GalaxyMapPanel.maxFlagScale;
import static rotp.ui.main.GalaxyMapPanel.maxFleetHugeScale;
import static rotp.ui.main.GalaxyMapPanel.maxFleetLargeScale;
import static rotp.ui.main.GalaxyMapPanel.maxFleetSmallScale;
import static rotp.ui.main.GalaxyMapPanel.maxFleetTransportScale;
import static rotp.ui.main.GalaxyMapPanel.maxFleetUnarmedScale;
import static rotp.ui.main.GalaxyMapPanel.maxRallyScale;
import static rotp.ui.main.GalaxyMapPanel.maxStargateScale;
import static rotp.ui.util.IParam.langLabel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JFileChooser;

import rotp.Rotp;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.game.BaseModPanel;
import rotp.ui.game.GameUI;
import rotp.ui.game.MainOptionsUI;
import rotp.ui.util.ParamBoolInt;
import rotp.ui.util.ParamBoolean;
import rotp.ui.util.ParamFloat;
import rotp.ui.util.ParamInteger;
import rotp.ui.util.ParamList;
import rotp.ui.util.ParamOptions;
import rotp.ui.util.ParamSpeciesName;
import rotp.ui.util.ParamString;
import rotp.ui.util.RotpFileChooser;
import rotp.util.FontManager;
import rotp.util.LanguageManager;
import rotp.util.sound.SoundManager;

public interface IMainOptions extends IDebugOptions, ICombatOptions {
	String WINDOW_MODE			= "GAME_SETTINGS_WINDOWED";
	String BORDERLESS_MODE		= "GAME_SETTINGS_BORDERLESS";
	String FULLSCREEN_MODE		= "GAME_SETTINGS_FULLSCREEN";
	String GRAPHICS_LOW			= "GAME_SETTINGS_GRAPHICS_LOW";
	String GRAPHICS_MEDIUM		= "GAME_SETTINGS_GRAPHICS_MED";
	String GRAPHICS_HIGH		= "GAME_SETTINGS_GRAPHICS_HIGH";
	String GRAPHICS_RETINA		= "GAME_SETTINGS_GRAPHICS_RETINA";
	String TEXTURES_NO			= "GAME_SETTINGS_TEXTURES_NO";
	String TEXTURES_INTERFACE	= "GAME_SETTINGS_TEXTURES_INTERFACE";
	String TEXTURES_MAP			= "GAME_SETTINGS_TEXTURES_MAP";
	String TEXTURES_BOTH		= "GAME_SETTINGS_TEXTURES_BOTH";
	String SENSITIVITY_HIGH		= "GAME_SETTINGS_SENSITIVITY_HIGH";
	String SENSITIVITY_MEDIUM	= "GAME_SETTINGS_SENSITIVITY_MEDIUM";
	String SENSITIVITY_LOW		= "GAME_SETTINGS_SENSITIVITY_LOW";
	int	   MAX_BACKUP_TURNS		= 20; // modnar: change max turns between backups to 20
	
	// ==================== Duplicates for Base Main Settings Options ====================
	//
	ParamList displayMode = new DisplayMode();
	final class DisplayMode extends ParamList {
		DisplayMode() {
			super( GAME_UI, "DISPLAY_MODE",
					Arrays.asList(
							WINDOW_MODE,
							BORDERLESS_MODE,
							FULLSCREEN_MODE
							),
					WINDOW_MODE);
			isDuplicate(true);
			isCfgFile(true);
			showFullGuide(true);
		}
		@Override public String getCfgValue()		{ return displayModeToSettingName(get()); }
		@Override public String getOption()			{ return displayMode(); }
		@Override public void setOption(String s)	{ displayMode(s); }
	}
//	default boolean fullScreen()	{ return displayMode().equals(FULLSCREEN_MODE); }
//	default boolean windowed()		{ return displayMode().equals(WINDOW_MODE); }
//	default boolean borderless()	{ return displayMode().equals(BORDERLESS_MODE); }
//	default String displayMode()	{ return displayMode.get(); }
	
	ParamList graphicsMode = new GraphicsMode();
	final class GraphicsMode extends ParamList {
		GraphicsMode() {
			super(GAME_UI, "GRAPHICS",
					Arrays.asList(
							GRAPHICS_HIGH,
							GRAPHICS_MEDIUM,
							GRAPHICS_LOW,
							GRAPHICS_RETINA
							),
					GRAPHICS_HIGH);
			isDuplicate(true);
			isCfgFile(true);
			showFullGuide(true);
		}
		@Override public String getCfgValue()		{ return graphicsModeToSettingName(get()); }
		@Override public String getOption()			{ return graphicsMode(); }
		@Override public void setOption(String s)	{ graphicsMode(s); }
	}
//	default boolean playAnimations()	{ return !graphicsMode.get().equals(GRAPHICS_LOW); }
//	default boolean antiAliasing()		{ return graphicsMode.get().equals(GRAPHICS_HIGH); }

	ParamList texturesMode = new TexturesMode();
	final class TexturesMode extends ParamList {
		TexturesMode() {
			super(GAME_UI, "TEXTURES",
					Arrays.asList(
							TEXTURES_NO,
							TEXTURES_INTERFACE,
							TEXTURES_MAP,
							TEXTURES_BOTH
							),
					TEXTURES_BOTH);
			isDuplicate(true);
			isCfgFile(true);
			showFullGuide(true);
		}
		@Override public String getCfgValue()		{ return texturesToSettingName(get()); }
		@Override public String getOption()			{ return texturesMode(); }
		@Override public void setOption(String s)	{ texturesMode(s); }
	}
//	default boolean texturesInterface()		{ return texturesInterface(); }
//	default boolean texturesMap()			{ return texturesMap(); }

	ParamList sensitivityMode = new SensitivityMode();
	final class SensitivityMode extends ParamList {
		SensitivityMode() {
			super(GAME_UI, "SENSITIVITY",
					Arrays.asList(
							SENSITIVITY_LOW,
							SENSITIVITY_MEDIUM,
							SENSITIVITY_HIGH
							),
					SENSITIVITY_MEDIUM);
			isDuplicate(true);
			isCfgFile(true);
			showFullGuide(true);
		}
		@Override public String getCfgValue()		{ return sensitivityToSettingName(get()); }
		@Override public String getOption()			{ return sensitivityMode(); }
		@Override public void setOption(String s)	{ sensitivityMode(s); }
	}
//	default boolean sensitivityMedium()		{ return sensitivityMedium(); }
//	default boolean sensitivityLow()		{ return sensitivityLow(); }

	// Do no use alone, Go through soundVolume
	ParamBoolean playSounds = new PlaySounds();
	final class PlaySounds extends ParamBoolean {
		PlaySounds() {
			super(GAME_UI, "SOUNDS", true);
			isDuplicate(true);
			isCfgFile(true);
		}
		@Override public Boolean getOption()		{ return playSounds(); }
		@Override public void setOption(Boolean b)	{
			if(playSounds() != b)
				//playSounds(!s); // ! because following line will toggle!
				SoundManager.current().toggleSounds();
		}
	}

	ParamBoolInt soundVolume = new SoundVolume();
	final class SoundVolume extends ParamBoolInt {
		SoundVolume() {
			super(GAME_UI, playSounds,
					"SOUNDS_ON", "SOUNDS_OFF", 10, 0, 10);
			isDuplicate(true);
			isCfgFile(true);
			loop(true);
		}
		@Override public String getCfgLabel()		{ return "SOUND_VOLUME"; }
		@Override public Integer getOption()		{ return soundVolume(); }
		@Override public void setOption(Integer i)	{
			soundVolume(Math.abs(i));
			SoundManager.current().resetSoundVolumes();
		}
	}
//	default int soundVolume()		{ return soundVolume.get(); }
//	default boolean playSounds()	{ return playSounds.get(); }
	
	// Do no use alone, Go through musicVolume
	ParamBoolean playMusic = new PlayMusic();
	final class PlayMusic extends ParamBoolean {
		PlayMusic() {
			super(GAME_UI, "MUSIC", true);
			isDuplicate(true);
			isCfgFile(true);
		}
		@Override public Boolean getOption()		{ return playMusic(); }
		@Override public void setOption(Boolean b)	{
			if(playMusic() != b)
				//playMusic(!s); // ! because following line will toggle!
				SoundManager.current().toggleMusic();
		}
	}

	ParamBoolInt musicVolume = new MusicVolume();
	final class MusicVolume extends ParamBoolInt {
		MusicVolume() {
			super(GAME_UI, playMusic, "MUSIC_ON", "MUSIC_OFF", 10, 0, 10);
			isDuplicate(true);
			isCfgFile(true);
			loop(true);
		}
		@Override public String getCfgLabel()		{ return "MUSIC_VOLUME"; }
		@Override public Integer getOption()		{ return musicVolume(); }
		@Override public void setOption(Integer i)	{
			musicVolume(Math.abs(i));
			SoundManager.current().resetMusicVolumes();
		}
	}
//	default int musicVolume()		{ return musicVolume.get(); }
//	default boolean playMusic()		{ return playMusic.get(); }

	ParamInteger selectedScreen = new SelectedScreen();
	final class SelectedScreen extends ParamInteger {
		SelectedScreen() {
			super(GAME_UI, "SELECTED_SCREEN", -1);
			setLimits(-1, Rotp.maxScreenIndex());
			loop(true);
			isDuplicate(true);
			isCfgFile(true);
		}
		@Override public Integer getOption()		{ return selectedScreen(); }
		@Override public void setOption(Integer i)	{ selectedScreen(i); }
	}
//	default int selectedScreen()		{ return selectedScreen.get(); }

	ParamInteger backupTurns = new BackupTurns();
	final class BackupTurns extends ParamInteger {
		BackupTurns() {
			super(GAME_UI, "BACKUP", 5);
			setLimits(0, MAX_BACKUP_TURNS);
			setIncrements(1, 5, 10);
			isDuplicate(true);
			isCfgFile(true);
			specialZero(GAME_UI + "BACKUP_OFF");
		}
		@Override public String getCfgLabel()		{ return "BACKUP_TURNS"; }
		@Override public Integer getOption()		{ return backupTurns(); }
		@Override public void setOption(Integer i)	{ backupTurns(i); }
	}

	//	default int backupTurns()		{ return backupTurns.get(); }
	ParamInteger backupKeep	= new ParamInteger(GAME_UI, "BACKUP_KEEP", -1)
			.setLimits(-1, 1000)
			.setIncrements(1, 5, 20)
			.isDuplicate(false)
			.isCfgFile(true)
			.specialNegative(GAME_UI + "BACKUP_KEEP_ALL")
			.loop(true);
	default int backupKeep()		{ return backupKeep.get(); }
	default boolean deleteBackup()	{ return !backupKeep.isSpecialNegative(); }

	ParamString saveDirectory = new SaveDirectory();
	final class SaveDirectory extends ParamString {
		SaveDirectory() {
			super(GAME_UI, "SAVEDIR", "");
			isDuplicate(true);
			isCfgFile(true);
		}
		@Override protected String descriptionId()	{
			String es = get().isEmpty()? "1" : "2";
			String label = super.descriptionId() + es;
			return label;			
		}
		@Override public String getGuiDescription()	{ return langLabel(descriptionId(), get()); }
		@Override public String guideValue()		{
			String es = get().isEmpty()? "_DEFAULT" : "_CUSTOM";
			String label = getLangLabel() + es;
			return langLabel(label);
		}
		@Override public boolean toggle(MouseEvent e, BaseModPanel frame)	{
			if (getDir(e) == 0) {
				set("");
				return false;
			}
	        final JFileChooser fc = new RotpFileChooser();
	        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        File saveDir = new File(saveDirectoryPath());
	        fc.setCurrentDirectory(saveDir);
	        int returnVal = fc.showOpenDialog(frame);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            String path = fc.getSelectedFile().getAbsolutePath();
	            set(path);
	        }
			return false;
	    }
		@Override public String	getCfgLabel()		{ return "SAVE_DIR"; }
		@Override public String	getOption()			{ return saveDir(); }
		@Override public void	setOption(String s)	{ saveDir(s); }
	}
	//	default int saveDir()		{ return saveDirectory.get(); }

	ParamBoolean disableAdvisor = new DisableAdvisor();
	final class DisableAdvisor extends ParamBoolean {
		DisableAdvisor() {
			super(MOD_UI, "DISABLE_ADVISOR", false);
			isDuplicate(true);
			isCfgFile(true);
		}
		@Override public Boolean getOption()		{ return disableAdvisor(); }
		@Override public void setOption(Boolean b)	{ disableAdvisor(b); }
	}

	ParamList disableAutoHelp = new ParamList( MOD_UI, "DISABLE_AUTO_HELP", "Yes")
			.isCfgFile(true)
			.showFullGuide(true)
			.put("Yes",		MOD_UI + "DISABLE_AUTO_HELP_YES")
			.put("No",		MOD_UI + "DISABLE_AUTO_HELP_NO")
			.put("Never",	MOD_UI + "DISABLE_AUTO_HELP_NEVER");
	default boolean isAutoHelpDisabled()	{ return disableAutoHelp.get().equalsIgnoreCase("Yes"); }
	default void autoHelpHasBeenShown()		{
		if (disableAutoHelp.get().equalsIgnoreCase("No"))
			disableAutoHelp.set("Yes");
	}

	ParamBoolean showGuide = new ParamBoolean(MOD_UI, "SHOW_GUIDE", true).isCfgFile(true);
	default boolean showGuide()			{ return showGuide.get(); }
	
	// ==================== Mod Options ====================
	//
	ParamOptions menuStartup	= new ParamOptions(MOD_UI, "MENU_STARTUP", ParamOptions.LAST).isCfgFile(true);
	ParamBoolean useFusionFont	= new UseFusionFont();
	final class UseFusionFont extends ParamBoolean {
		UseFusionFont() {
			super(MOD_UI, "USE_FUSION_FONT", false);
			isCfgFile(true);
		}
		@Override public void setOption(Boolean val) { FontManager.INSTANCE.resetGalaxyFont(); }
	}

	String LOAD_SAVE_NORMAL	= "NORMAL";
	String LOAD_SAVE_WIDE	= "WIDE";
	String LOAD_SAVE_FULL	= "FULL";
	ParamList loadSaveWidth = new ParamList( MOD_UI, "LOAD_SAVE_WIDTH", LOAD_SAVE_NORMAL)
			.isCfgFile(true)
			.showFullGuide(true)
			.put(LOAD_SAVE_NORMAL,	MOD_UI + "LOAD_SAVE_NORMAL")
			.put(LOAD_SAVE_WIDE,	MOD_UI + "LOAD_SAVE_WIDE")
			.put(LOAD_SAVE_FULL,	MOD_UI + "LOAD_SAVE_FULL");
	static int loadSaveWidth()	{
		String val = (loadSaveWidth.get().toUpperCase());
		if (val.equals(LOAD_SAVE_NORMAL))
			return 650;
		else if (val.equals(LOAD_SAVE_WIDE))
			return 850;
		else if (val.equals(LOAD_SAVE_FULL))
			return 1200;
		else
			return 650;
	}
	static boolean isLoadSaveWidthNormal()	{ return loadSaveWidth.get().equals(LOAD_SAVE_NORMAL); }

	ParamList compactOptionOnly = new ParamList( MOD_UI, "COMPACT_OPTION_ONLY", "Yes")
			.isCfgFile(true)
			.showFullGuide(true)
			.put("Yes",	MOD_UI + "COMPACT_OPTION_ONLY_YES")
			.put("No",	MOD_UI + "COMPACT_OPTION_ONLY_NO");
	default ParamList compactOptionOnly()	{ return compactOptionOnly; }
	
	ParamList gameOverTitles	= new ParamList( MOD_UI, "GAME_OVER_TITLE", "Original")
			.isCfgFile(true)
			.isDuplicate(false)
			.showFullGuide(true)
			.put("Original",	 MOD_UI + "GAME_OVER_TITLE_ORIGINAL")
			.put("Extended",	 MOD_UI + "GAME_OVER_TITLE_EXTENDED")
			.put("RealmsBeyond", MOD_UI + "GAME_OVER_TITLE_RB");
	static String gameOverTitlesKeyExt() {
		switch (gameOverTitles.get().toUpperCase()) {
			case "EXTENDED" :		return "_E";
			case "REALMSBEYOND" :	return "_RB";
			default:				return "";
		}
	}
	default boolean originalGOTitles()	{ return gameOverTitles.get().equalsIgnoreCase("Original"); }
	default boolean extendedGOTitles()	{ return gameOverTitles.get().equalsIgnoreCase("Extended"); }
	default boolean rbGOTitles()		{ return gameOverTitles.get().equalsIgnoreCase("RealmsBeyond"); }

	ParamBoolean showAlternateAnimation	= new ParamBoolean(MOD_UI, "SHOW_ALT_ANIMATION", true).isCfgFile(true);
	ParamBoolean originalSpeciesOnly	= new ParamBoolean(MOD_UI, "ORIGINAL_SPECIES_ONLY", true).isCfgFile(true);
	default boolean originalSpeciesOnly()	{ return originalSpeciesOnly.get(); }

	ParamBoolean displayFreeTech	= new ParamBoolean(MOD_UI, "DISPLAY_FREE_TECH", true)
			.isCfgFile(true)
			.setDefaultValue(ROTP_DEFAULT, false)
			.setDefaultValue(MOO1_DEFAULT, false);
	default boolean displayFreeTech()		{ return displayFreeTech.get(); }
	default boolean toggleDisplayFreeTech()	{ return displayFreeTech.toggle(); }

	ParamBoolean  noFogOnIcons	= new ParamBoolean( BASE_UI, "NO_FOG_ON_ICONS", false).isCfgFile(true);
	default boolean noFogOnIcons()	{ return noFogOnIcons.get(); }

	ParamFloat   showFlagFactor	= new ShowFlagFactor();
	final class ShowFlagFactor extends ParamFloat {
		ShowFlagFactor() {
			super(MOD_UI, "SHOW_FLAG_FACTOR", 1.0f);
			setLimits(0.3f, 3f);
			setIncrements(0.01f, 0.05f, 0.2f);
			cfgFormat("%");
			guiFormat("%");
			isCfgFile(true);
		}
		@Override public void setOption(Float val) { maxFlagScale((int) (80 * val)); }
	}

	ParamFloat   showPathFactor	= new ShowPathFactor();
	final class ShowPathFactor extends ParamFloat {
		ShowPathFactor() {
			super(MOD_UI, "SHOW_PATH_FACTOR", 1.0f);
			setLimits(0.3f, 3f);
			setIncrements(0.01f, 0.05f, 0.2f);
			cfgFormat("%");
			guiFormat("%");
			isCfgFile(true);
		}
		@Override public void setOption(Float val) { maxRallyScale((int) (100 * val)); }
	}

	ParamInteger showNameMinFont	= new ShowNameMinFont();
	final class ShowNameMinFont extends ParamInteger {
		ShowNameMinFont() {
			super(MOD_UI, "SHOW_NAME_MIN_FONT", 8);
			setLimits(2, 24);
			setIncrements(1, 2, 5);
			isCfgFile(true);
		}
		@Override public void setOption(Integer val) {
			setMinFont(val);
			setMinFont2(Math.round(val/showInfoFontRatio.get()));
		}
	}

	ParamFloat showInfoFontRatio	= new ShowInfoFontRatio();
	final class ShowInfoFontRatio extends ParamFloat {
		ShowInfoFontRatio() {
			super(MOD_UI, "SHOW_INFO_FONT_RATIO", 0.7f);
			setLimits(0.2f, 3f);
			setIncrements(0.01f, 0.05f, 0.2f);
			cfgFormat("%");
			guiFormat("%");
			isCfgFile(true);
		}
		@Override public void setOption(Float val) { setMinFont2(Math.round(getMinFont()/val)); }
	}

	ParamFloat mapFontFactor	= new MapFontFactor();
	final class MapFontFactor extends ParamFloat {
		MapFontFactor() {
			super(MOD_UI, "MAP_FONT_FACTOR", 1.0f);
			setLimits(0.3f, 3f);
			setIncrements(0.01f, 0.05f, 0.2f);
			cfgFormat("%");
			guiFormat("%");
			isCfgFile(true);
		}
		@Override public void setOption(Float val) { setFontPct(Math.round(val * 100)); }
	}

	ParamFloat showFleetFactor	= new ShowFleetFactor();
	final class ShowFleetFactor extends ParamFloat {
		ShowFleetFactor() {
			super(MOD_UI, "SHOW_FLEET_FACTOR", 1.0f);
			setLimits(0.3f, 3f);
			setIncrements(0.01f, 0.05f, 0.2f);
			cfgFormat("%");
			guiFormat("%");
			isCfgFile(true);
		}
		@Override public void setOption(Float val) {
			maxStargateScale		((int) (40 * val));
			maxFleetUnarmedScale	((int) (40 * val));
			maxFleetTransportScale	((int) (60 * val));
			maxFleetSmallScale		((int) (60 * val));
			maxFleetLargeScale		((int) (80 * val));
			maxFleetHugeScale		((int) (100 * val));	
		}
	}

	ParamBoolean showPendingOrders	= new ParamBoolean(MOD_UI, "SHOW_PENDING_ORDERS", true)
			.isCfgFile(true);
	default boolean showPendingOrders()		{ return showPendingOrders.get(); }

	ParamBoolean raceStatusLog		= new ParamBoolean(MOD_UI, "RACE_STATUS_LOG", false).isCfgFile(true);
	default boolean selectedRaceStatusLog()	{ return raceStatusLog.get(); }
	default void	toggleRaceStatusLog()	{ raceStatusLog.toggle(); }

	ParamInteger galaxyPreviewColorStarsSize = new ParamInteger(MOD_UI, "GALAXY_PREVIEW_COLOR_SIZE", 5)
			.setLimits(0, 20)
			.setIncrements(1, 2, 5)
			.isCfgFile(true);
	default ParamInteger galaxyPreviewColorStarsSize()	{ return galaxyPreviewColorStarsSize; }

	ParamBoolean galaxyPreviewAI		= new ParamBoolean(MOD_UI, "GALAXY_PREVIEW_AI", true)
			.isCfgFile(true);
	default boolean galaxyPreviewAI()		{ return galaxyPreviewAI.get(); }

	ParamBoolean galaxyPreviewPlayer	= new ParamBoolean(MOD_UI, "GALAXY_PREVIEW_PLAYER", true)
			.isCfgFile(true);
	default boolean galaxyPreviewPlayer()	{ return galaxyPreviewPlayer.get(); }

	ParamBoolean galaxyPreviewOrion		= new ParamBoolean(MOD_UI, "GALAXY_PREVIEW_ORION", true)
			.isCfgFile(true);
	default boolean galaxyPreviewOrion()	{ return galaxyPreviewOrion.get(); }

	ParamInteger minListSizePopUp	= new ParamInteger(MOD_UI, "MIN_LIST_SIZE_POP_UP" , 8)
			.setLimits(0, 10)
			.loop(true)
			.isCfgFile(true)
			.specialZero(MOD_UI + "MIN_LIST_SIZE_POP_UP_NEVER");
	default ParamInteger minListSizePopUp()	{ return minListSizePopUp; }

	ParamList colorSet = new ColorSet();
	final class ColorSet extends ParamList {
		ColorSet() {
			super(MOD_UI, "COLOR_SET", "Brown");
			isCfgFile(true);
			showFullGuide(true);
			put("Brown",	MOD_UI + "COLOR_SET_BROWN");
			put("Grey",		MOD_UI + "COLOR_SET_GREY");
		}
		@Override public void setFromCfgValue(String val) {
			super.setFromCfgValue(val);
			GameUI.colorSet(selectedColorSet());
		}
		@Override public void setOption(String val) {
			super.setOption(val);
			GameUI.colorSet(selectedColorSet());
			if (RotPUI.instance() == null)
				return;
			MainOptionsUI mo = RotPUI.mainOptionsUI();
			if (mo.activeList() == null)
				return;
			mo.clearImages();
			mo.refreshGui(0);
		}
	}
	static int selectedColorSet()	{
		switch (colorSet.get().toUpperCase()) {
			case "BROWN":	return 0;
			case "GREY":	return 1;
			default:		return 0;
		}
	}

	ParamList raceStatusView		= new ParamList( MOD_UI, "RACE_STATUS_VIEW", "PctTotal")
			.isCfgFile(true)
			.showFullGuide(true)
			.put("PctTotal",	MOD_UI + "RACE_STATUS_VIEW_PCT_TOTAL")
			.put("PctPlayer",	MOD_UI + "RACE_STATUS_VIEW_PCT_PLAYER")
			.put("Value",		MOD_UI + "RACE_STATUS_VIEW_VALUE");
	default void	toggleRaceStatusView()	{ raceStatusView.next(); }
	default String	raceStatusViewText()	{ return raceStatusView.guideValue(); }
	default boolean raceStatusViewTotal()	{ return raceStatusView.get().equals("PctTotal"); }
	default boolean raceStatusViewPlayer()	{ return raceStatusView.get().equals("PctPlayer"); }
	default boolean raceStatusViewValue()	{ return raceStatusView.get().equals("Value"); }

	ParamInteger realNebulaSize	= new RealNebulaeSize();
	final class RealNebulaeSize extends ParamInteger {
		RealNebulaeSize() {
			super(MOD_UI, "REAL_NEBULAE_SIZE", 0);
			setLimits(0, 5);
			setIncrements(1, 1, 1);
			isCfgFile(false);
			specialZero(MOD_UI + "REAL_NEBULAE_NO");
			loop(true);
		}
		@Override public Integer set(Integer i)	{
			rotp.model.galaxy.Nebula.requestedQuality(i);
			super.set(i);
			UserPreferences.save();
			return get();
		}
	}
	default int selectedRealNebulaSize()	{ return realNebulaSize.get(); }
	default boolean selectedRealNebula()	{ return realNebulaSize.get() != 0; }
	default ParamInteger getRealNebulaSize()	{ return realNebulaSize; }

	ParamBoolean realNebulaShape	= new ParamBoolean(MOD_UI, "REAL_NEBULAE_SHAPE", true).isCfgFile(false);
	default boolean realNebulaShape()	{ return realNebulaShape.get(); }
	default ParamBoolean getRealNebulaShape()	{ return realNebulaShape; }

	ParamInteger realNebulaeOpacity	= new ParamInteger(MOD_UI, "REAL_NEBULAE_OPACITY", 60)
			.setLimits(10, 100)
			.setIncrements(1, 5, 20)
			.isCfgFile(true)
			.pctValue(true);
	default float realNebulaeOpacity()	{ return realNebulaeOpacity.get()/100f; }

	ParamBoolean showAllAI	= new ShowAllAI();
	final class ShowAllAI extends ParamBoolean {
		ShowAllAI() {
			super(MOD_UI, "SHOW_ALL_AI", true);
			isCfgFile(true);
			isDuplicate(false);
		}
		@Override public Boolean set(Boolean newValue) {
			super.set(newValue);
			rotp.ui.game.SetupGalaxyUI.specificAI().reInit(null);
			rotp.ui.game.SetupGalaxyUI.opponentAI().reInit(null);
			rotp.model.game.IAdvOptions.autoplay.reInit(null);
			return get();
		}
	}

	default boolean selectedShowAllAI()			{ return showAllAI.get(); }
	String NAME_UI		= "NAME_";
	// Customized nominal names
	// Former English Values
	String altairiMOOen		= "Alkari Sovereignty, Alkari, Alkaris";
	String ursinathiMOOen	= "Bulrathi Empire, Bulrathi, Bulrathis";
	String nazlokMOOen		= "Darloks, Darlok, Darloks";
	String humanMOOen		= "Human Triumvirate, Human, Humans";
	String kholdanMOOen		= "Klackon Hive, Klackon, Klackons";
	String meklonarMOOen	= "Meklar Dominion, Meklar, Meklars";
	String fiershanMOOen	= "Mrrshan Clan, Mrrshan, Mrrshan";
	String mentaranMOOen	= "Psilon Republic, Psilon, Psilons";
	String ssslauraMOOen	= "Sakkra Conclave, Sakkra, Sakkras";
	String cryslonoidMOOen	= "Silicoid Imperium, Silicoid, Silicoids";
	// Former French Values
	String altairiMOOfr		= "La Souveraineté Alkarienne, de la Souveraineté Alkarienne, Alkarien, Alkarienne, "
							+ "les Alkaris, des Alkaris, aux Alkaris, Alkariens, Alkariennes";
	String ursinathiMOOfr	= "L'Empire Bulrathien, de l'Empire Bulrathien, Bulrathien, Bulrathienne, "
							+ "les Bulrathis, des Bulrathis, aux Bulrathis, Bulrathiens, Bulrathiennes";
	String nazlokMOOfr		= "Darlok, des Darloks, Darlokien, Darlokienne, "
							+ "les Darloks, des Darloks, aux Darloks, Darlokiens, Darlokiennes";
	String humanMOOfr		= "Le Triumvirat Humain, du Triumvirat Humain, Humain, Humaine, "
							+ "les Humains, des Humains, aux Humains, Humains, Humaines";
	String kholdanMOOfr		= "La Ruche Klackonienne, de la Ruche Klackonienne, Klackonien, Klackonienne, "
							+ "les Klackons, des Klackons, aux Klackons, Klackoniens, Klackoniennes";
	String meklonarMOOfr	= "Le Dominion Meklarien, du Dominion Meklarien, Meklarien, Meklarienne, "
							+ "les Meklars, des Meklars, aux Meklars, Meklariens,  Meklariennes";
	String fiershanMOOfr	= "Le Clan Mrrshan, du Clan Mrrshan, Mrrshan, Mrrshan, "
							+ "les Mrrshan, des Mrrshan, aux Mrrshan, Mrrshan, Mrrshan";
	String mentaranMOOfr	= "La République Psilonne, de la République Psilonne, Psilon, Psilonne, "
							+ "les Psilons, des Psilons, aux Psilons, Psilons, Psilonnes";
	String ssslauraMOOfr	= "Le Conclave Sakkra, du Conclave Sakkra, Sakkra, Sakkra, "
							+ "les Sakkras, des Sakkras, aux Sakkras, Sakkras, Sakkras";
	String cryslonoidMOOfr	= "L'Imperium Silicoïdien, de L'Imperium Silicoïdien, Silicoïdien, Silicoïdienne, "
							+ "les Silicoïds, des Silicoïds, aux Silicoïds, Silicoïdiens, Silicoïdiennes";

	// Customized nominal names
	// for English language
	ParamSpeciesName altairi	= new ParamSpeciesName(NAME_UI, "RACE_ALKARI",	 altairiMOOen);
	ParamSpeciesName ursinathi	= new ParamSpeciesName(NAME_UI, "RACE_BULRATHI", ursinathiMOOen);
	ParamSpeciesName nazlok		= new ParamSpeciesName(NAME_UI, "RACE_DARLOK",	 nazlokMOOen);
	ParamSpeciesName human		= new ParamSpeciesName(NAME_UI, "RACE_HUMAN",	 humanMOOen);
	ParamSpeciesName kholdan	= new ParamSpeciesName(NAME_UI, "RACE_KLACKON",	 kholdanMOOen);
	ParamSpeciesName meklonar	= new ParamSpeciesName(NAME_UI, "RACE_MEKLAR",	 meklonarMOOen);
	ParamSpeciesName fiershan	= new ParamSpeciesName(NAME_UI, "RACE_MRRSHAN",	 fiershanMOOen);
	ParamSpeciesName mentaran	= new ParamSpeciesName(NAME_UI, "RACE_PSILON",	 mentaranMOOen);
	ParamSpeciesName ssslaura	= new ParamSpeciesName(NAME_UI, "RACE_SAKKRA",	 ssslauraMOOen);
	ParamSpeciesName cryslonoid	= new ParamSpeciesName(NAME_UI, "RACE_SILICOID", cryslonoidMOOen);
	// for French language
	ParamSpeciesName altairiFr		= new ParamSpeciesName(NAME_UI, "fr", "RACE_ALKARI_FR",		altairiMOOfr);
	ParamSpeciesName ursinathiFr	= new ParamSpeciesName(NAME_UI, "fr", "RACE_BULRATHI_FR",	ursinathiMOOfr);
	ParamSpeciesName nazlokFr		= new ParamSpeciesName(NAME_UI, "fr", "RACE_DARLOK_FR",		nazlokMOOfr);
	ParamSpeciesName humanFr		= new ParamSpeciesName(NAME_UI, "fr", "RACE_HUMAN_FR",		humanMOOfr);
	ParamSpeciesName kholdanFr		= new ParamSpeciesName(NAME_UI, "fr", "RACE_KLACKON_FR",	kholdanMOOfr);
	ParamSpeciesName meklonarFr		= new ParamSpeciesName(NAME_UI, "fr", "RACE_MEKLAR_FR",		meklonarMOOfr);
	ParamSpeciesName fiershanFr		= new ParamSpeciesName(NAME_UI, "fr", "RACE_MRRSHAN_FR",	fiershanMOOfr);
	ParamSpeciesName mentaranFr		= new ParamSpeciesName(NAME_UI, "fr", "RACE_PSILON_FR",		mentaranMOOfr);
	ParamSpeciesName ssslauraFr		= new ParamSpeciesName(NAME_UI, "fr", "RACE_SAKKRA_FR",		ssslauraMOOfr);
	ParamSpeciesName cryslonoidFr	= new ParamSpeciesName(NAME_UI, "fr", "RACE_SILICOID_FR",	cryslonoidMOOfr);

	// Mapped customized nominal names
	// for English language
	HashMap<String, ParamSpeciesName> speciesNameMap = speciesNameMap();
	static HashMap<String, ParamSpeciesName> speciesNameMap() {
		HashMap<String, ParamSpeciesName> map= new HashMap<>();
		map.put(human.getCfgLabel(),		human);
		map.put(altairi.getCfgLabel(),		altairi);
		map.put(cryslonoid.getCfgLabel(),	cryslonoid);
		map.put(fiershan.getCfgLabel(),		fiershan);
		map.put(kholdan.getCfgLabel(),		kholdan);
		map.put(meklonar.getCfgLabel(),		meklonar);
		map.put(mentaran.getCfgLabel(),		mentaran);
		map.put(nazlok.getCfgLabel(),		nazlok);
		map.put(ssslaura.getCfgLabel(),		ssslaura);
		map.put(ursinathi.getCfgLabel(),	ursinathi);
		return map;
	}
	// for French language
	HashMap<String, ParamSpeciesName> speciesNameMapFr = speciesNameMapFr();
	static HashMap<String, ParamSpeciesName> speciesNameMapFr() {
		HashMap<String, ParamSpeciesName> map= new HashMap<>();
		map.put(humanFr.getCfgLabel(),		humanFr);
		map.put(altairiFr.getCfgLabel(),	altairiFr);
		map.put(cryslonoidFr.getCfgLabel(),	cryslonoidFr);
		map.put(fiershanFr.getCfgLabel(),	fiershanFr);
		map.put(kholdanFr.getCfgLabel(),	kholdanFr);
		map.put(meklonarFr.getCfgLabel(),	meklonarFr);
		map.put(mentaranFr.getCfgLabel(),	mentaranFr);
		map.put(nazlokFr.getCfgLabel(),		nazlokFr);
		map.put(ssslauraFr.getCfgLabel(),	ssslauraFr);
		map.put(ursinathiFr.getCfgLabel(),	ursinathiFr);
		return map;
	}

	ParamBoolean moo1SpeciesName = new Moo1SpeciesName();
	final class Moo1SpeciesName extends ParamBoolean {
		Moo1SpeciesName() {
			super(MOD_UI, "MOO1_SPECIES_NAME", false);
			isCfgFile(false);
			isDuplicate(false);
		}
		@Override public boolean toggle(MouseEvent e, MouseWheelEvent w, BaseModPanel frame) {
			String langId = LanguageManager.selectedLanguageDir();
			switch (langId) {
				case "en":
					human.set(humanMOOen);
					altairi.set(altairiMOOen);
					cryslonoid.set(cryslonoidMOOen);
					fiershan.set(fiershanMOOen);
					kholdan.set(kholdanMOOen);
					meklonar.set(meklonarMOOen);
					mentaran.set(mentaranMOOen);
					nazlok.set(nazlokMOOen);
					ssslaura.set(ssslauraMOOen);
					ursinathi.set(ursinathiMOOen);
					next();
					LanguageManager.current().reloadLanguage();
					return true;
				case "fr":
					humanFr.set(humanMOOfr);
					altairiFr.set(altairiMOOfr);
					cryslonoidFr.set(cryslonoidMOOfr);
					fiershanFr.set(fiershanMOOfr);
					kholdanFr.set(kholdanMOOfr);
					meklonarFr.set(meklonarMOOfr);
					mentaranFr.set(mentaranMOOfr);
					nazlokFr.set(nazlokMOOfr);
					ssslauraFr.set(ssslauraMOOfr);
					ursinathiFr.set(ursinathiMOOfr);
					next();
					LanguageManager.current().reloadLanguage();
					return true;
				default:
					return false;
			}
		}
		@Override public String	guideValue() { return ""; }
	}

	ParamBoolean clearSpeciesName	= new ClearSpeciesName();
	final class ClearSpeciesName extends ParamBoolean {
		ClearSpeciesName() {
			super(MOD_UI, "CLEAR_SPECIES_NAME", false);
			isCfgFile(false);
			isDuplicate(false);
		}
		@Override public boolean toggle(MouseEvent e, MouseWheelEvent w, BaseModPanel frame) {
			String langId = LanguageManager.selectedLanguageDir();
			switch (langId) {
				case "en":
					for (ParamSpeciesName param : speciesNameMap.values()) {
						param.set("");
						param.updated(true);
					}
					next();
					LanguageManager.current().reloadLanguage();
					return true;
				case "fr":
					for (ParamSpeciesName param : speciesNameMapFr.values()) {
						param.set("");
						param.updated(true);
					}
					next();
					LanguageManager.current().reloadLanguage();
					return true;
				default:
					return false;
			}
		}
		@Override public String	guideValue() { return ""; }
	}

	ParamBoolean activateSpeciesName	= new ActivateSpeciesName();
	final class ActivateSpeciesName extends ParamBoolean {
		ActivateSpeciesName() {
			super(MOD_UI, "ACTIVATE_SPECIES_NAME", false);
			isCfgFile(false);
			isDuplicate(false);
		}
		@Override public boolean toggle(MouseEvent e, MouseWheelEvent w, BaseModPanel frame) {
			next();
			LanguageManager.current().reloadLanguage();
			return false;
		}
		@Override public String	guideValue() { return ""; }
	}

	ParamList defaultSettings	= new DefaultSettings();
	final class DefaultSettings extends ParamList {
		DefaultSettings() {
			super(MOD_UI, "DEFAULT_SETTINGS",
					Arrays.asList(
							FUSION_DEFAULT,
							MOO1_DEFAULT,
							ROTP_DEFAULT
							),
					FUSION_DEFAULT);
			setDefaultValue(MOO1_DEFAULT, MOO1_DEFAULT);
			setDefaultValue(ROTP_DEFAULT, ROTP_DEFAULT);
			isDuplicate(true);
			isCfgFile(true);
			showFullGuide(true);
		}
		@Override public String getCfgValue()		{ return DEF_VAL.getSettingName(); }
		@Override public String getOption()			{ return DEF_VAL.defVal(); }
		@Override public void setOption(String s)	{ DEF_VAL.selectedDefault(s); }
	}
	ParamBoolean shipBasedMissiles	= new ParamBoolean(MOD_UI, "SHIP_BASED_MISSILES", true)
			.isCfgFile(true);
	default boolean shipBasedMissiles()	{ return shipBasedMissiles.get(); }

	ParamBoolean scoutAndColonyOnly	= new ParamBoolean(MOD_UI, "SCOUT_AND_COLONY_ONLY", false)
			.isCfgFile(true);
	default boolean scoutAndColonyOnly()		{ return scoutAndColonyOnly.get(); }

	// TODO BR: Add this option to a panel to be saved
	int MAX_CASCADE_PANEL_SAVE_LOAD = 10;
	ParamBoolean cascadeSubPanelSaveLoad	= new ParamBoolean(MOD_UI, "CASCADE_SUBPANEL_SAVE_LOAD", false)
			.isCfgFile(true);
	default int cascadeSubPanelSaveLoad() { return cascadeSubPanelSaveLoad.get()? MAX_CASCADE_PANEL_SAVE_LOAD : 0; }

	String PANEL_ALIGNMENT	= "PANEL_ALIGN";
	String LEFT_ALIGN		= "LEFT";
	String RIGHT_ALIGN		= "RIGHT";
	String CENTERED			= "CENTER";
	String JUSTIFIED		= "JUSTIFY";
	ParamList optionPanelAlignment	= new ParamList( MOD_UI, PANEL_ALIGNMENT, CENTERED)
			.isCfgFile(true)
			.showFullGuide(true)
			.forcedRefresh(true)
			.put(CENTERED,		MOD_UI + PANEL_ALIGNMENT + "_" + CENTERED)
			.put(LEFT_ALIGN,	MOD_UI + PANEL_ALIGNMENT + "_" + LEFT_ALIGN)
			.put(RIGHT_ALIGN,	MOD_UI + PANEL_ALIGNMENT + "_" + RIGHT_ALIGN)
			.put(JUSTIFIED,		MOD_UI + PANEL_ALIGNMENT + "_" + JUSTIFIED);
	default boolean optionPanelIsCentered()		{ return optionPanelAlignment.get().equals(CENTERED); }
	default boolean optionPanelIsLeftAlign()	{ return optionPanelAlignment.get().equals(LEFT_ALIGN); }
	default boolean optionPanelIsRightAlign()	{ return optionPanelAlignment.get().equals(RIGHT_ALIGN); }
	default boolean optionPanelIsJustified()	{ return optionPanelAlignment.get().equals(JUSTIFIED); }

	ParamBoolean keepShipDesignName	= new ParamBoolean(MOD_UI, "KEEP_SHIP_NAME", false).isCfgFile(true);
	default boolean keepShipDesignName()	{ return keepShipDesignName.get(); }

	ParamBoolean showTechProgress	= new ParamBoolean(MOD_UI, "SHOW_TECH_PROGRESS", true).isCfgFile(true);
	default boolean showTechProgress()		{ return showTechProgress.get(); }
}
