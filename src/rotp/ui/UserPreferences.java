/*
 * Copyright 2015-2020 Ray Fowler
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.ui;

import static rotp.model.game.DefaultValues.DEF_VAL;
import static rotp.model.game.IConvenienceOptions.AUTOBOMBARD_INVADE;
import static rotp.model.game.IConvenienceOptions.AUTOBOMBARD_NEVER;
import static rotp.model.game.IConvenienceOptions.AUTOBOMBARD_NO;
import static rotp.model.game.IConvenienceOptions.AUTOBOMBARD_WAR;
import static rotp.model.game.IConvenienceOptions.AUTOBOMBARD_YES;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import rotp.Rotp;
import rotp.model.game.GameSession;
import rotp.model.game.IMainOptions;
import rotp.ui.options.AllSubUI;
import rotp.ui.util.IParam;
import rotp.ui.util.ParamString;
import rotp.util.LanguageManager;

public class UserPreferences implements IMainOptions {

	private static final String PREFERENCES_FILE  = "Remnants.cfg";
	public  static final String GALAXY_TEXT_FILE  = "Galaxy.txt";
	private static final String keyFormat = "%-25s: "; // BR: from 20 to 25 for a better alignment

	private static boolean showMemory  = false;
	private static boolean playMusic   = true;
	private static boolean playSounds  = true;
	private static int musicVolume     = 10;
	private static int soundVolume     = 10;
	private static boolean displayYear = false;
	private static boolean disableAdvisor = true;
	private static String displayMode     = WINDOW_MODE;
	private static String graphicsMode    = GRAPHICS_HIGH;
	private static String texturesMode    = TEXTURES_BOTH;
	private static String sensitivityMode = SENSITIVITY_HIGH;
	private static String saveDir = "";
	private static float uiTexturePct = 0.20f;
	private static int screenSizePct = 93;
	private static int selectedScreen = -1; // BR: to specify the destination display
	private static int backupTurns = 5; // modnar: change default turns between backups to 5
	private static List<IParam<?>> optionList;
	private static boolean readyToSave = false;
	private static List<IParam<?>> optionList() {
		if (optionList == null) {
			optionList = AllSubUI.allCfgOptions(false);
		}
		
		return optionList;
	}
	public static void initialList(boolean initial) {
		optionList = AllSubUI.allCfgOptions(true);
		save();
	}

	/**
	 * Advanced setting GUI Default Button Action
	 */
	public static void setForNewGame()		 { }
	public static void musicVolume(int i)    { musicVolume = i; }
	public static int musicVolume()		     { return musicVolume; }
	public static void soundVolume(int i)    {
		soundVolume = i;
	}
	public static int soundVolume()		     { return soundVolume; }
	public static void showMemory(boolean b) { showMemory = b; }
	public static boolean showMemory()	     { return showMemory; }
	public static boolean fullScreen()	     { return displayMode.equals(FULLSCREEN_MODE); }
	public static boolean windowed()	     { return displayMode.equals(WINDOW_MODE); }
	public static boolean borderless()	     { return displayMode.equals(BORDERLESS_MODE); }
	public static String displayMode()	     { return displayMode; }
	public static void displayMode(String s) { displayMode = s; }
	public static void graphicsMode(String s) { graphicsMode = s; save();}
	public static String graphicsMode()	      {
		if(showVIPPanel.get())
			return GRAPHICS_LOW;
		return graphicsMode;
	}
	public static void texturesMode(String s) { texturesMode = s; save();}
	public static String texturesMode()	      { return texturesMode; }
	public static boolean texturesInterface() { return texturesMode.equals(TEXTURES_INTERFACE) || texturesMode.equals(TEXTURES_BOTH); }
	public static boolean texturesMap()	      { return texturesMode.equals(TEXTURES_MAP) || texturesMode.equals(TEXTURES_BOTH); }

	public static void sensitivityMode(String s) { sensitivityMode = s; save();}
	public static String sensitivityMode()	     { return sensitivityMode; }
	// public static boolean sensitivityHigh()	 { return sensitivityMode.equals(SENSITIVITY_HIGH); }
	public static boolean sensitivityMedium()    { return sensitivityMode.equals(SENSITIVITY_MEDIUM); }
	public static boolean sensitivityLow()	     { return sensitivityMode.equals(SENSITIVITY_LOW); }

	private static boolean graphicLow()		{ return graphicsMode().equals(GRAPHICS_LOW); }
	private static boolean graphicHigh()		{ return graphicsMode().equals(GRAPHICS_HIGH); }
	public static boolean graphicRetina()	{ return graphicsMode().equals(GRAPHICS_RETINA); }
	public static boolean playAnimations()  { return !graphicLow(); }
	public static boolean antialiasing()	{ return graphicHigh() || graphicRetina(); }
	public static boolean playSounds()      { return playSounds; }
	public static void toggleSounds()       { playSounds = !playSounds; save(); }
	public static boolean playMusic()       { return playMusic; }
	public static void toggleMusic()        { playMusic = !playMusic; save();  } // called from sound manager
	
	public	static void		selectedScreen(int i)			{ selectedScreen = i; }
	public	static int		selectedScreen()				{ return selectedScreen; }
	public	static int		screenSizePct()					{ return screenSizePct; }
	public	static String	saveDirectoryPath()				{
		if (saveDir.isEmpty())
			return Rotp.jarPath();
		else
			return saveDir;
	}
	public	static String	backupDirectoryPath()			{
		return saveDirectoryPath()+"/"+GameSession.BACKUP_DIRECTORY;
	}
	public	static String	saveDir()						{ return saveDir; }
	public	static void		saveDir(String s)				{ saveDir = s; save(); }
	public	static int		backupTurns()					{ return backupTurns; }
	public	static boolean	backupTurns(int i)				{
		int prev = backupTurns;
		backupTurns = Math.min(Math.max(0,i), MAX_BACKUP_TURNS);
		save();
		return prev != backupTurns;
	}
	public	static boolean	disableAdvisor()				{ return disableAdvisor; }
	public	static void		disableAdvisor(boolean b)		{ disableAdvisor = b; }
	private	static void		uiTexturePct(int i)				{ uiTexturePct = i / 100.0f; }
	static	float			uiTexturePct()					{ return uiTexturePct; }
	public static void loadAndSave() {
		// System.out.println("UserPreferences: loadAndSave()");
		reload();
		readyToSave = true;
		save();
	}
	public static void reload()					{ load (false); }
	public static void load(boolean coreOnly)	{
		// System.out.println("UserPreferences: load() " + coreOnly);
		String path = Rotp.jarPath();
		File configFile = new File(path, PREFERENCES_FILE);
		// modnar: change to InputStreamReader, force UTF-8
		try ( BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream(configFile), "UTF-8"));) {
			String input;
			while ((input = in.readLine()) != null)
				loadPreferenceLine(input.trim(), coreOnly);
		}
		catch (FileNotFoundException e) {
			System.err.println(path+PREFERENCES_FILE+" not found.");
		}
		catch (IOException e) {
			System.err.println("UserPreferences.load -- IOException: "+ e.toString());
		}
	}
	public static int save() {
		if (!readyToSave) {
			// System.out.println("UserPreferences: save() when not ready to save... Save Aborted");
			return 0;
		}
		// System.out.println("UserPreferences: save()");
		String path = Rotp.jarPath();
		try (FileOutputStream fout = new FileOutputStream(new File(path, PREFERENCES_FILE));
			// modnar: change to OutputStreamWriter, force UTF-8
			PrintWriter out = new PrintWriter(new OutputStreamWriter(fout, "UTF-8")); ) {
			out.println("===== Base ROTP Settings =====");
			out.println();
			out.println(keyFormat("DEFAULT_OPTIONS") + DEF_VAL.getSettingName());
			out.println(keyFormat("DISPLAY_MODE")+displayModeToSettingName(displayMode));
			out.println(keyFormat("GRAPHICS")+graphicsModeToSettingName(graphicsMode));
			out.println(keyFormat("MUSIC")+ yesOrNo(playMusic));
			out.println(keyFormat("SOUNDS")+ yesOrNo(playSounds));
			out.println(keyFormat("MUSIC_VOLUME")+ musicVolume);
			out.println(keyFormat("SOUND_VOLUME")+ soundVolume);
			out.println(keyFormat("SAVE_DIR")+ saveDir);
			out.println(keyFormat("BACKUP_TURNS")+ backupTurns);
			out.println(keyFormat("TEXTURES")+texturesToSettingName(texturesMode));
			out.println(keyFormat("SENSITIVITY")+sensitivityToSettingName(sensitivityMode));
			out.println(keyFormat("SHOW_MEMORY")+ yesOrNo(showMemory));
			out.println(keyFormat("DISPLAY_YEAR")+ yesOrNo(displayYear));
			out.println(keyFormat("SCREEN_SIZE_PCT")+ screenSizePct());
			out.println(keyFormat("SELECTED_SCREEN")+ selectedScreen());
			out.println(keyFormat("UI_TEXTURE_LEVEL")+(int) (uiTexturePct()*100));
			out.println(keyFormat("DISABLE_ADVISOR") + yesOrNo(disableAdvisor));
			out.println(keyFormat("LANGUAGE")+ languageDir());

			out.println();
			out.println("===== Extended Settings =====");
			out.println();
			for (IParam<?> param : optionList()) {
				if (param != null
						&& !param.isDuplicate()
						&& param.isCfgFile()
						&& !param.isSubMenu()) {
//					if (param instanceof ParamSpeciesName) {
//						System.out.print(param.getCfgLabel());
//						System.out.println(param.getCfgValue());
//					}
					out.println(keyFormat(param.getCfgLabel()) + param.getCfgValue());
				}
			}
			return 0;
		}
		catch (IOException e) {
			System.err.println("UserPreferences.save -- IOException: "+ e.toString());
			return -1;
		}
	}

	private static String keyFormat(String s)  { return String.format(keyFormat, s); }

	private static void loadPreferenceLine(String line, boolean coreOnly) {
		if (line.isEmpty())
			return;
		if (!line.contains(":"))
			return;

		String[] args = line.split(":");
		String key = args[0].toUpperCase().trim();
		if (key.isEmpty())
			return;
		
		// BR: this to allows empty strings
		String val = "";
		if (args.length >1)
			val = args[1].trim();

		// for values that may have embedded :, like the save dir path
		String fullVal = val;
		for (int i=2;i<args.length;i++)
			fullVal = fullVal+":"+args[i];

		if (Rotp.logging)
			System.out.println("Key:"+key+"  value:"+val);
		switch(key) {
			case "DEFAULT_OPTIONS": DEF_VAL.setFromSettingName(val); return;
			case "DISPLAY_MODE": displayMode = displayModeFromSettingName(val); return;
			case "GRAPHICS":     graphicsMode = graphicsModeFromSettingName(val); return;
			case "MUSIC":        playMusic = yesOrNo(val); return;
			case "SOUNDS":       playSounds = yesOrNo(val); return;
			case "MUSIC_VOLUME": setMusicVolume(val); return;
			case "SOUND_VOLUME": setSoundVolume(val); return;
			case "SAVE_DIR":     saveDir  = fullVal.trim(); return;
			case "BACKUP_TURNS": backupTurns  = Integer.valueOf(val); return;
			case "TEXTURES":     texturesMode = texturesFromSettingName(val); return;
			case "SENSITIVITY":  sensitivityMode = sensitivityFromSettingName(val); return;
			case "SHOW_MEMORY":  showMemory = yesOrNo(val); return;
			case "SCREEN_SIZE_PCT": setScreenSizePct(Integer.valueOf(val)); return;
			case "SELECTED_SCREEN": setSelectedScreen(Integer.valueOf(val)); return;
			case "UI_TEXTURE_LEVEL": uiTexturePct(Integer.valueOf(val)); return;
			case "LANGUAGE": selectLanguage(val); return;
		}
		if (coreOnly)
			return;

		switch(key) {
			case "DISPLAY_YEAR": displayYear = yesOrNo(val); return;
			case "DISABLE_ADVISOR": disableAdvisor = yesOrNo(val); return;
			default:
				// BR: Global Mod GUI
				for (IParam<?> param : optionList()) {
					if (param != null && key.equalsIgnoreCase(param.getCfgLabel())) {
						if (param instanceof ParamString)
							param.setFromCfgValue(fullVal.trim());
						else
							param.setFromCfgValue(val);
						break;
					}
				}
				return;
		}
	}
	private static String yesOrNo(boolean b) {
		return b ? "YES" : "NO";
	}
	private static boolean yesOrNo(String s) {
		return s.equalsIgnoreCase("YES");
	}
	private static void selectLanguage(String s) {
		LanguageManager.selectLanguage(s);
	}
	private static void setMusicVolume(String s) {
		int val = Integer.valueOf(s);
		musicVolume = Math.max(0, Math.min(10,val));
	}
	private static void setSoundVolume(String s) {
		int val = Integer.valueOf(s);
		soundVolume = Math.max(0, Math.min(10,val));
	}
	private static String languageDir() {
		return LanguageManager.selectedLanguageDir();
	}
	private	static void setSelectedScreen(int i) {
		selectedScreen = Math.max(-1, Math.min(i, Rotp.maxScreenIndex()));
	}
	private static void setScreenSizePct(int i) {
		if (i==-1) {
			screenSizePct = i;
			return;
		}
		screenSizePct = Math.max(25,Math.min(i,200));
	}
	public static boolean shrinkFrame() {
		int oldSize = screenSizePct;
		setScreenSizePct(screenSizePct-5);
		return oldSize != screenSizePct;
	}
	public static boolean expandFrame() {
		int oldSize = screenSizePct;
		setScreenSizePct(screenSizePct+5);
		return oldSize != screenSizePct;
	}
	public static String displayModeToSettingName(String s) {
		switch(s) {
			case WINDOW_MODE:	  return "Windowed";
			case BORDERLESS_MODE: return "Borderless";
			case FULLSCREEN_MODE: return "Fullscreen";
		}
		return "Windowed";
	}
	private static String displayModeFromSettingName(String s) {
		switch(s) {
			case "Windowed":   return WINDOW_MODE;
			case "Borderless": return BORDERLESS_MODE;
			case "Fullscreen": return FULLSCREEN_MODE;
		}
		return WINDOW_MODE;
	}
	public static String graphicsModeToSettingName(String s) {
		switch(s) {
			case GRAPHICS_LOW:	  return "Low";
			case GRAPHICS_MEDIUM: return "Medium";
			case GRAPHICS_HIGH:   return "High";
			case GRAPHICS_RETINA: return "Retina";
		}
		return "High";
	}
	private static String graphicsModeFromSettingName(String s) {
		switch(s) {
			case "Low":	   return GRAPHICS_LOW;
			case "Medium": return GRAPHICS_MEDIUM;
			case "High":   return GRAPHICS_HIGH;
			case "Retina": return GRAPHICS_RETINA;
		}
		return GRAPHICS_HIGH;
	}
	public static String autoBombardToSettingName(String s) {
		switch(s) {
			case AUTOBOMBARD_NO:	 return "No";
			case AUTOBOMBARD_NEVER:  return "Never";
			case AUTOBOMBARD_YES:	 return "Yes";
			case AUTOBOMBARD_WAR:	 return "War";
			case AUTOBOMBARD_INVADE: return "Invade";
		}
		return "No";
	}
	public static String texturesToSettingName(String s) {
		switch(s) {
			case TEXTURES_NO:		 return "No";
			case TEXTURES_INTERFACE: return "Interface";
			case TEXTURES_MAP:	     return "Map";
			case TEXTURES_BOTH:	     return "Both";
		}
		return "Both";
	}
	private static String texturesFromSettingName(String s) {
		switch(s) {
			case "No":        return TEXTURES_NO;
			case "Interface": return TEXTURES_INTERFACE;
			case "Map":       return TEXTURES_MAP;
			case "Both":      return TEXTURES_BOTH;
		}
		return TEXTURES_BOTH;
	}
	public static String sensitivityToSettingName(String s) {
		switch(s) {
			case SENSITIVITY_HIGH:   return "High";
			case SENSITIVITY_MEDIUM: return "Medium";
			case SENSITIVITY_LOW:    return "Low";
		}
		return "High";
	}
	private static String sensitivityFromSettingName(String s) {
		switch(s) {
			case "High":   return SENSITIVITY_HIGH;
			case "Medium": return SENSITIVITY_MEDIUM;
			case "Low":    return SENSITIVITY_LOW;
		}
		return SENSITIVITY_HIGH;
	}
}
