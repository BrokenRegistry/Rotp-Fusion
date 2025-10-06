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

import static rotp.model.game.IGameOptions.useFusionFont;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.font.TextAttribute;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum FontManager implements Base {
    INSTANCE;
    public static FontManager current()  { return INSTANCE; }
    private static final String FUSION_FONT = "NotoFusionSimplified-Regular.otf";

    private static final int MAX_FONT_SIZE = 200;
    private String logoFont;
    private String introFont;
    private String dlgFont;
    private String narrowFont;
    private String plainFont;
    private Font   galaxyFont;
    private final Map<String,String[]> languageFontNames = new HashMap<>(); // maps lang code to filenames of font
    private final Map<String,Font> languageFonts = new HashMap<>();       // maps filename of font to a font[]
    private final Map<String,Font[]> allFonts = new HashMap<>();
    private int dlgSize, narrowSize, plainSize, introSize,  logoSize; //, languageSize;

    public static Font getNarrowFont(float size) {
    	Font[] fonts = INSTANCE.allFonts.get(INSTANCE.narrowFont);
    	return fonts[0].deriveFont(size*INSTANCE.narrowSize/100);
    }
    public void resetGalaxyFont() { galaxyFont = null; }
    @Override public Font galaxyFont(int size) { // BR: MonoSpaced font for Galaxy
    	if (galaxyFont == null) {
    		Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
    		attributes.put(TextAttribute.TRACKING, -0.15);
    		if (useFusionFont.get())
    			galaxyFont = loadFusionFont(15).deriveFont(attributes);
    		else
    			galaxyFont = loadMonoFont(15).deriveFont(attributes);
    	}
    	Font font = galaxyFont.deriveFont((float) size);
    	return font;
    }
    @Override
    public Font dlgFont(int n) { return getFont(dlgFont, n, dlgSize); }
    @Override
    public Font narrowFont(int n) { return getFont(narrowFont, n, narrowSize); }
    @Override
    public Font plainFont(int n)  { return getFont(plainFont, n, plainSize);  }
    public Font languageFont(String code) {
        String filenames[] = languageFontNames.get(code);
        return languageFonts.get(filenames[5]);
    }
    @Override
    public Font font(int n)        { return getFont(introFont, n, introSize);  }
    @Override
    public Font logoFont(int n)    { return getFont(logoFont, n, logoSize); }

    private Font getFont(String filename, int size, int scale) {
        if (!allFonts.containsKey(filename)) 
            createFontTable(filename, null);
        
        int n = min(MAX_FONT_SIZE, size);
        Font[] fonts = allFonts.get(filename);
        
        int index = n*scale/100;
        return fonts[index];
    }
    private void createFontTable(String filename, Font f) {
        if (allFonts.containsKey(filename))
            return;
        
        Font[] fonts = new Font[MAX_FONT_SIZE+1];
        allFonts.put(filename, fonts);
        
        // index 0 is the base font that we use to re-derive
        // the others when the window size changes 
        fonts[0] = f;
        
        for (int size=1;size<=MAX_FONT_SIZE;size++) {
            if (fonts[size] == null)
                fonts[size] = f.deriveFont((float) scaled(size));
        }
    }
    public void resetFonts() {
        // window scaling has scaled. Rederive all fonts according to new scale
        for (String key: allFonts.keySet()) {
            Font[] fonts = allFonts.get(key);
            Font baseFont = fonts[0];
            for (int size=1;size<=MAX_FONT_SIZE;size++) 
                fonts[size] = baseFont.deriveFont((float) scaled(size));
        }
    }
    public String[] fontNames(String langCode) {
        if (!languageFontNames.containsKey(langCode))
            languageFontNames.put(langCode, new String[6]);
        
        return languageFontNames.get(langCode);
    }
    public void loadFonts(String baseDir, String langDir) {
        String fontDir = baseDir+"fonts/";
        String dir = baseDir+langDir+"/";
        log("Loading fonts - baseDir: ", baseDir, "  fontDir: ", fontDir, "  langDir: ", dir);
        String dataFile = "fonts.txt";
        BufferedReader in;
       	in = reader(dir+dataFile);
        if (in == null) {
            err("can't find fonts file! ", dir, dataFile);
            return;
        }
        try {
            String input;
            while ((input = in.readLine()) != null)
                loadFontLine(input, fontDir, langDir);
            in.close();
        }
        catch (IOException e) {
            err("FontManager.loadFonts -- IOException: ", e.toString());
        }
    }
    private void loadFontLine(String input, String fontDir, String langCode) {
        if (isComment(input))
            return;

        List<String> fields = substrings(input, ',');
        String fontName = fields.get(0);
        if (fontName.equalsIgnoreCase("name")) // these lines should have been commented
        	return;
        String filename = fields.size() > 1 ? fields.get(1) : fields.get(0);
        int fontSizing = fields.size() > 2 ? parseInt(fields.get(2)): 100;

        InputStream is = fileInputStream(fontDir+filename);
        if (is == null) {
            err("FontManager.loadFont: could not get inputStream for:"+fontDir+filename);
            return;
        }
        
        try {
            Font newFont = Font.createFont(Font.TRUETYPE_FONT, is);
            String[] fontNames = fontNames(langCode);
            if (fontName.equalsIgnoreCase("dialogue")) {
                fontNames[0] = filename;
                dlgFont = filename;
                dlgSize = fontSizing;
                createFontTable(filename, newFont);
            }
            else if (fontName.equalsIgnoreCase("narrow")) {
                fontNames[1] = filename;
                narrowFont = filename;
                narrowSize = fontSizing;
                createFontTable(filename, newFont);
            }
            else if (fontName.equalsIgnoreCase("plain")) {
                fontNames[2] = filename;
                plainFont = filename;
                plainSize = fontSizing;
                createFontTable(filename, newFont);
            }
            else if (fontName.equalsIgnoreCase("normal")) {
                fontNames[3] = filename;
                introFont = filename;
                introSize = fontSizing;
                createFontTable(filename, newFont);
            }
            else if (fontName.equalsIgnoreCase("logo")) {
                fontNames[4] = filename;
                logoFont = filename;
                logoSize = fontSizing;
                createFontTable(filename, newFont);
            }
        }
        catch (FontFormatException | IOException e) {
            err("FontManager.loadFont -- Exception: " + e.getMessage());
        }
    }
    public void loadLanguageFonts(String baseDir, String langDir) {
        String fontDir = baseDir+"fonts/";
        String dir = baseDir+langDir+"/";
        log("Loading fonts - baseDir: ", baseDir, "  fontDir: ", fontDir, "  langDir: ", dir);
        String dataFile = "fonts.txt";

        BufferedReader in = reader(dir+dataFile);
        if (in == null) {
            err("can't find fonts file! ", dir, dataFile);
            return;
        }
        try {
            String input;
            while ((input = in.readLine()) != null)
                loadLanguageFontLine(input, fontDir, langDir);
            in.close();
        }
        catch (IOException e) {
            err("FontManager.loadFonts -- IOException: ", e.toString());
        }
    }
    private void loadLanguageFontLine(String input, String fontDir, String langCode) {
        if (isComment(input))
            return;

        List<String> fields = substrings(input, ',');
        String fontName = fields.get(0);
        
        if (!fontName.equalsIgnoreCase("language"))
            return;
        
        String filename = fields.size() > 1 ? fields.get(1) : fields.get(0);
        
        String[] fontNames = fontNames(langCode);
        fontNames[5] = filename;
        
        if (languageFonts.containsKey(filename))
            return;
        
        int fontScaling = fields.size() > 2 ? parseInt(fields.get(2)): 100;

        InputStream is = fileInputStream(fontDir+filename);
        if (is == null) {
            err("FontManager.loadFont: could not get inputStream for:"+fontDir+filename);
            return;
        }

        try {
            Font newFont = Font.createFont(Font.TRUETYPE_FONT, is);
            createFontTable(filename, newFont);
            Font font2 = getFont(filename, 15, fontScaling);
            languageFonts.put(filename, font2);
        }
        catch (FontFormatException | IOException e) {
            err("FontManager.loadFont -- Exception: " + e.getMessage());
        }
    }
    public Font loadFusionFont(int size) {
		String filename = FUSION_FONT;
		String fontDir = "lang/fonts/";
        InputStream is = fileInputStream(fontDir+filename);
        if (is == null)
            return loadMonoFont(size);

        try {
        	return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont((float) size);
        }
        catch (FontFormatException | IOException e) {}
        return loadMonoFont(size);
    }
    private Font loadMonoFont(int size) {
        return new Font(Font.MONOSPACED, Font.PLAIN, size);
    }
}
