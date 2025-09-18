/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.util;

import java.awt.ComponentOrientation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import rotp.Rotp;
import rotp.model.empires.ISpecies;
import rotp.model.empires.Race;
import rotp.model.empires.RaceFactory;
import rotp.model.game.GovernorOptions;
import rotp.model.game.IGovOptions;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.main.EmpireColonySpendingPane;
import rotp.ui.planets.MultiColonySpendingPane;

public class LanguageManager implements Base {
    private static LanguageManager instance = new LanguageManager();
    public static LanguageManager current() { return instance; }

    public static int DEFAULT_LANGUAGE = 0;
    private static final String baseDir = "lang/";
    private static final String languageFile = "languages.txt";
    private static final List<Language> languages = new ArrayList<>();
    private static int selectedLanguage = LanguageManager.DEFAULT_LANGUAGE;
    public static final char[] latinDigits = { '0','1','2','3','4','5','6','7','8','9' };
    public static char[] customDigits = null;
    public static Language currentLanguage;

    public static void selectDefaultLanguage() { instance.selectLanguage(LanguageManager.DEFAULT_LANGUAGE); }
    public static int selectedLanguage()        { return selectedLanguage; }
    public static void selectedLanguage(int i)  {
    	selectedLanguage = i;
    	if (i >= 0)
    		currentLanguage  = languages.get(i);
    }
	public static boolean isDefaultLanguage()	{ return selectedLanguage == DEFAULT_LANGUAGE; }
    private List<Language> languages()  {
        if (languages.isEmpty()) {
            loadLanguages();
            selectedLanguage(-1);
            selectLanguage(DEFAULT_LANGUAGE);
        }
        return languages;
    }
    public List<String> languageCodes() {
        List<String> names = new ArrayList<>();
        for (Language lang: languages)
            names.add(lang.directory);
        return names;
    }
    public List<String> languageNames() {
        List<String> names = new ArrayList<>();
        for (Language lang: languages)
            names.add(lang.name);
        return names;
    }
    private Language languageForCode(String code) {
        for (Language lang: languages) {
            if (lang.directory.equalsIgnoreCase(code))
                return lang;
        }
        return null;
    }
    public static int languageNumber(String dir) {
        for (int i=0;i<languages.size();i++) {
            Language lang = languages.get(i);
            if (lang.directory.equalsIgnoreCase(dir))
                return i;
        }
        return DEFAULT_LANGUAGE;
    }
    public static void selectLanguage(String dir) {
        for (int i=0;i<languages.size();i++) {
            Language lang = languages.get(i);
            if (lang.directory.equalsIgnoreCase(dir)) {
                current().selectLanguage(i);
                return;
            }
        }
    }
    public static String selectedLanguageDir() { return languages.get(selectedLanguage).directory; }
    public static String languageDir(int i)    { return languages.get(i).directory; }
    public String selectedLanguageName()       { return language(selectedLanguage()); }
    public String defaultLanguageFullPath()    { return baseDir+languages().get(DEFAULT_LANGUAGE).directory; }
    public String selectedLanguageFullPath()   { return baseDir+languages().get(selectedLanguage()).directory; }

    public String language(int i)   { return languages().get(i).name; }
    public String langDir(int i)    { return languages().get(i).directory; }
    public String langSubdir(int i) { return languages().get(i).subdirectory;    }
    public String fontName(int i)   { return languages().get(i).font; }
    public Locale locale(int i)     { return languages().get(i).locale; }
    public boolean logographic(int i)   { return languages().get(i).logographic; }
    public ComponentOrientation orientation(int i) { return languages().get(i).orientation; }
    public void cycleLanguage(boolean up) {
        int i = selectedLanguage();
        i += (up?1:-1);
        if (i < 0)
            i = languages().size() - 1;
        else if (i >= languages().size())
            i = 0;

        selectLanguage(i);
        UserPreferences.save();
    }
    public void selectLanguage(int i) {
        if (selectedLanguage() == i)
            return;
        loadLanguage(i);
		String newFrameTitle = text("GAME_TITLE_FRAME");
		Rotp.getFrame().setTitle(newFrameTitle);
		if (RotPUI.instance() != null)
			RotPUI.instance().resetListDialog();
		validateDialogueTokens(false); // TO DO BR: change to false (true for debug)
		if (!Rotp.noOptions()) {
			EmpireColonySpendingPane.resetPanel();
			MultiColonySpendingPane.resetPanel();
		}
        GovernorOptions.callForRefresh(IGovOptions.GOV_REFRESH);
    }
    private void validateDialogueTokens(boolean doIt) {
    	LabelManager.validate = doIt;
    	if (!doIt)
    		return;
    	boolean valid = true;
    	System.out.println("Start Dialogue Tokens Validation");    	
    	for (Race r : ISpecies.R_M.races()) {
    		valid &= r.validateDialogueTokens();
    	}
    	if (valid)
    		System.out.println("Validation Successful");
    	else
    		System.out.println("Validation Failed");
    }
    public void reloadLanguage()      { loadLanguage(selectedLanguage()); } // BR: to reload labels without having to restart
    public void reloadRace(Race race) { // BR: to reload Selected race labels
    	Language newLang = languages().get(selectedLanguage);
    	RaceFactory.current().loadRaceLangFiles(race, newLang.directory);
    }
    public void loadLanguage(int i)   {
        Language defLang = languages().get(DEFAULT_LANGUAGE); // BR: Uncommented
        Language newLang = languages().get(i);

        // load fonts for selected language
        FontManager.current().loadFonts(baseDir, newLang.directory);

        // reset dialogue maps in label managers
        labels().resetDialogue();
        RaceFactory.current().resetRaceLangFiles();

        // reload default labels, since that is assured of completeness
        String currDir;
        selectedLanguage(0);
        currDir = baseDir+defLang.directory+"/"; // BR: Uncommented
        labels().loadLabelFile(currDir); // BR: Uncommented
        labels().loadDialogueFile(currDir); // BR: Uncommented
        labels().loadTechsFile(currDir); // BR: Uncommented
        RaceFactory.current().loadRaceLangFiles(defLang.directory); // BR: Uncommented

        // now overwrite those with labels for the selected language
        selectedLanguage(i);

        customDigits = newLang.digits;

        if (i != DEFAULT_LANGUAGE) {  // BR: Uncommented
            currDir = baseDir+newLang.directory+"/";
            labels().resetDialogue(); // To avoid mixing languages
            labels().load(currDir);
            RaceFactory.current().loadRaceLangFiles(newLang.directory);
        }
    }
    public String defaultLangDir()    { return langDir(DEFAULT_LANGUAGE); }
    public String currentLanguage()   { return language(selectedLanguage()); }
    public String currentLangDir()    { return langDir(selectedLanguage()); }
    public String currentLangSubdir() { return langSubdir(selectedLanguage()); }
    public String currentFont()       { return fontName(selectedLanguage()); }
    public Locale currentLocale()     { return locale(selectedLanguage()); }
    public boolean currentLogographic() { return logographic(selectedLanguage()); }
    public ComponentOrientation currentOrientation()  { return orientation(selectedLanguage()); }

    public static String swapToken(String token) {
    	if (currentLanguage.hasTokenMap) {
    		String alt = currentLanguage.tokenMap.get(token);
    		// System.out.println("getValidToken: " + token + " => " + alt);
    		return alt;
    	}
    	return null;
    }

    protected void loadLanguages() {
        loadInstalledLanguages();
        File langDir = new File(Rotp.jarPath()+"/lang");
        File[] langFolders = langDir.listFiles();
        if (langFolders != null) {
            for (File f : langFolders){
                if (f.isDirectory()) {
                    String langCode = f.getName();
                    String langName = languageDisplayName(langCode);
                    Language language = languageForCode(langCode);
                    if (langName != null) {
                        FontManager.current().loadLanguageFonts(baseDir, langCode);
                        if (language == null) 
                            languages.add(new Language(langCode, "", langName, "", "", false, null, null));
                        else 
                            language.name = langName;
                    }
                }
            }
        }
    }

    protected String languageDisplayName(String fn) {
        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(Rotp.jarPath()+"/lang/"+fn, "fonts.txt"));
        } catch (FileNotFoundException e) {
            return null;
        }
        InputStreamReader isr;
        try {
            isr = new InputStreamReader(fis, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }

		try (BufferedReader in = new BufferedReader(isr)) {
			try {
				String input;
				while ((input = in.readLine()) != null) {
					String[] vars = input.split(",");
					if ((vars.length > 1) && vars[0].equalsIgnoreCase("name"))
						return vars[1].trim();
				}
				in.close();
				isr.close();
				fis.close();
			}
			catch (IOException e) {
				err("LanguageManager.languageDisplayName()2 -- IOException: ", e.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
    }
    protected void loadInstalledLanguages() {
        BufferedReader in = reader(baseDir+languageFile);
        if (in == null) {
            err("LanguageManager.loadInstalledLanguages() - can't find language file! ", baseDir, languageFile);
            return;
        }
        try {
            String input;
            while ((input = in.readLine()) != null)
                loadInstalledLanguageLine(input);
            in.close();
        }
        catch (IOException e) {
            err("LanguageManager.loadInstalledLanguages() -- IOException: ", e.toString());
        }
    }
    protected void loadInstalledLanguageLine(String input) {
        if (isComment(input))
            return;

        // BR: Added option for token replacement
        List<String> entry = substrings(input, ';', 0);
        input = entry.remove(0);
        HashMap<String, String> tokenMap = getTokenMap(entry);

        List<String> strings = substrings(input, ',');
        String dirString = strings.get(0);
        String subdirString = "";
        String nameString = strings.get(1);
        String orientString = strings.get(2);
        String fontString = strings.get(3);
        String logoString = strings.get(4);
        char[] digitsString = strings.size() > 5 ? strings.get(5).toCharArray() : null;

        boolean logo = logoString.equalsIgnoreCase("Y");

        languages.add(new Language(dirString, subdirString, nameString, orientString, fontString, logo, digitsString, tokenMap));
        // load fonts for selected language
        FontManager.current().loadLanguageFonts(baseDir, dirString);
    }
    private HashMap<String, String> getTokenMap(List<String> input) {
    	HashMap<String, String> tokenMap = new HashMap<>();
        while (!input.isEmpty()) {
        	String pair = input.remove(0);
        	List<String> strings = substrings(pair, ',');
        	if (strings.size() == 2 && !strings.get(0).isEmpty() && !strings.get(1).isEmpty()) {
        		tokenMap.put(strings.get(0), strings.get(1));
        	}
        	else {
        		System.err.println("Error: wrong number of token in " + languageFile + " pair = " + pair);
        	}
        }
    	return tokenMap;
    }

    class Language {
        final String directory;
        final String subdirectory;
        final Locale locale;
        final ComponentOrientation orientation;
        final boolean logographic;
        String name;
        final String font;
        final char[] digits;
        final HashMap<String, String> tokenMap;
        final boolean hasTokenMap;

        public Language(String dir, String sub, String n, String o, String f,
        		boolean logo, char[] d, HashMap<String, String> tMap) {
            directory = dir;
            subdirectory = sub;
            name = n;
            font = f;
            logographic = logo;
            locale = new Locale(dir);
            digits = d;
            if (o.trim().equalsIgnoreCase("RT"))
                orientation = ComponentOrientation.RIGHT_TO_LEFT;
            else
                orientation = ComponentOrientation.LEFT_TO_RIGHT;
            hasTokenMap = tMap != null && !tMap.isEmpty();
            tokenMap = hasTokenMap? tMap : null;
        }
    }
}
