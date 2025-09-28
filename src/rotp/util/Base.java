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

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static rotp.model.game.IGameOptions.DIFFICULTY_CUSTOM;
import static rotp.model.game.IRaceOptions.playerCustomRace;
import static rotp.model.game.IRaceOptions.playerIsCustom;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
//import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.FastMath;

import rotp.Rotp;
import rotp.model.empires.Empire;
import rotp.model.empires.ISpecies;
import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.DynOptions;
import rotp.model.game.GameSession;
import rotp.model.game.GovernorOptions;
import rotp.model.game.IGameOptions;
import rotp.model.game.RulesetManager;
import rotp.model.tech.Tech;
import rotp.model.tech.TechLibrary;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.util.planets.PlanetImager;
import rotp.util.sound.SoundClip;
import rotp.util.sound.SoundManager;

public interface Base extends InputEventUtil {
    public static String[] monthName = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
    public static String[] letter = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N" };
    //public static Random random = new Random(); // Moved to Rotp for reset purpose
    public default Rand rng() { return Rotp.rand(); }
    public static DecimalFormat df1 = new DecimalFormat("0.0");
    public static DecimalFormat df2 = new DecimalFormat("0.00");
    public static DecimalFormat df3 = new DecimalFormat("0.000");
    public static DecimalFormat df4 = new DecimalFormat("0.0000");
    public static DecimalFormat df5 = new DecimalFormat("0.00000");
    public static DecimalFormat df6 = new DecimalFormat("0.000000");
    public static DecimalFormat sf1 = new DecimalFormat("0.0E00");
    public static DecimalFormat sf2 = new DecimalFormat("0.00E00");
    public static DecimalFormat sf3 = new DecimalFormat("0.000E00");
    public static DecimalFormat sf4 = new DecimalFormat("0.0000E00");
    public static DecimalFormat sf5 = new DecimalFormat("0.00000E00");
    public static DecimalFormat sf6 = new DecimalFormat("0.000000E00");
    public static DecimalFormat sf7 = new DecimalFormat("0.0000000E00");
    public static DecimalFormat sf8 = new DecimalFormat("0.00000000E00");
    public static DecimalFormat pad4 = new DecimalFormat("0000");
    public static String NEWLINE	 = "<br>"; 	// BR: for descriptions and Help. System independent .
    public static int MB = 1048576;
    public static ImageColorizer colorizer = new ImageColorizer();
    public static String[] textSubs = { "%1", "%2", "%3", "%4", "%5", "%6", "%7", "%8", "%9", "%0" };

	default RulesetManager rulesetManager()	{ return RulesetManager.current(); }
	default GameSession session()			{ return GameSession.instance(); }
	default Galaxy galaxy()					{ return session().galaxy(); }
	default Empire player()					{ return galaxy().player(); }
	default boolean isPlayer(Empire e)		{ return galaxy().isPlayer(e); }
	default LabelManager labels()			{ return LabelManager.current(); }
	default IGameOptions options()			{ return session().options(); }
	default IGameOptions newGameOptions()	{ return rulesetManager().newOptions(); }
	default IGameOptions guiOptions()		{ return rulesetManager().currentOptions(); }
	default DynOptions   dynOptions()		{ return guiOptions().dynOpts(); }
	default GovernorOptions govOptions()	{ return GameSession.instance().getGovernorOptions(); }

    public default Object sessionVar(String key) {
        return session().var(key);
    }
    public default void removeSessionVar(String key) {
        session().removeVar(key);
    }
    public default void sessionVar(String key, Object value) {
        session().var(key,  value);
    }
    public default int scaled(int i) {
        return RotPUI.scaledSize(i);
    }
    public default int unscaled(int i) {
        return RotPUI.unscaledSize(i);
    }
    public default void mapClick()    { playAudioClip("MapClick"); }
    public default void buttonClick() { playAudioClip("ButtonClick"); }
    public default void menuClick()   { playAudioClip("MenuClick"); }
    public default void softClick()   { playAudioClip("SoftClick"); }
    public default void misClick()    { playAudioClip("MisClick"); }

    public default long timeMs()  { return System.currentTimeMillis() - Rotp.startMs; }
    public default boolean playAnimations()   { return AnimationManager.current().playAnimations(); }
    public default void stopAmbience() {
        SoundManager.current().stopAmbience();
    }
    public default void playAmbience(String key) {
        SoundManager.current().playAmbience(key);
    }
    public default SoundClip playAudioClip(String key) {
        return SoundManager.current().playAudioClip(key);
    }
    public default SoundClip playAudioClip(String key, int hullSize) {
        return SoundManager.current().playAudioClip(key, hullSize);
    }
    public default SoundClip alwaysPlayAudioClip(String key) {
        return SoundManager.current().alwaysPlay(key);
    }
    public default int id(Empire e)       { return e == null ? Empire.NULL_ID : e.id; }
    public default int id(StarSystem s)   { return s == null ? StarSystem.NULL_ID : s.id; }
    public default String text(String key) {
        if (!Rotp.initialized() || (galaxy() == null) || (player() == null))
            return labels().label(key);
        else
            return player().raceText(key);
    }
    public default String text(String key, String... vals) {
        String str = text(key);
        for (int i=0;i<vals.length;i++)
            str = str.replace(textSubs[i], vals[i]);
        return str;
    }
    public default String text(String key, long... vals) {
        String str = text(key);
        for (int i=0;i<vals.length;i++)
            str = str.replace(textSubs[i],String.valueOf(vals[i]));
        return str;
    }
    public default String text(String key, int... vals) {
        String str = text(key);
        for (int i=0;i<vals.length;i++)
            str = str.replace(textSubs[i],String.valueOf(vals[i]));
        return str;
    }
    public default String text(String key, String val1, int val2) {
        String str = text(key);
        str = str.replace("%1", val1);
        return str.replace("%2", String.valueOf(val2));
    }
    public default String text(String key, String val1, String val2, int val3) {
        String str = text(key);
        str = str.replace("%1", val1);
        str = str.replace("%2", val2);
        return str.replace("%3", String.valueOf(val3));
    }
	public default Font galaxyFont(int size) { return FontManager.current().galaxyFont(size); }
	public default int scaledGalaxyFont(Graphics2D g2, String str, int maxWidth, int maxHeight, int desiredFont, int minFont) {
		int size = desiredFont;
		g2.setFont(galaxyFont(size));
		Rectangle bounds = getStringBounds(g2, str, 0, 0);
		while ((bounds.height>maxHeight || bounds.width>maxWidth) && size>minFont) {
			size--;
			g2.setFont(galaxyFont(size));
			bounds = getStringBounds(g2, str, 0, 0);
		}
	 	 return size;
	}
    /**
     * First choice is dialogue font... But if the dialogue font
     * can not display the String str, return the narrow font
     * (dialogue font do not include Greek Character Set)
     */
    public default Font dlgOrPlainFont(int size, String str) {
    	Font dlg = FontManager.current().dlgFont(size);
    	if (dlg.canDisplayUpTo(str) == -1)
    		return dlg;
    	else
    		return FontManager.current().plainFont(size);
    }
    public default Font dlgFont(int size) {
        return FontManager.current().dlgFont(size);
    }
    public default Font narrowFont(int size) {
        return FontManager.current().narrowFont(size);
    }
    public default Font plainFont(int size) {
        return FontManager.current().plainFont(size);
    }
    public default Font font(int size) {
        return FontManager.current().font(size);
    }
    public default Font logoFont(int size) {
        return FontManager.current().logoFont(size);
    }
    public default void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public default void exception(Exception e) {
        e.printStackTrace();
        if (RotPUI.useDebugFile) {
            PrintWriter debugFile = RotPUI.debugFile();
            if (debugFile != null) {
                e.printStackTrace(debugFile);
                debugFile.flush();
            }
        }
        RotPUI.instance().selectErrorPanel(e);
    }
    public default void err(String... text) {
        String output = concat(text);
        try {
            System.err.println(output);
            if (RotPUI.useDebugFile) {
                PrintWriter debugFile = RotPUI.debugFile();
                if (debugFile != null) {
                    debugFile.println(output);
                    debugFile.flush();
                }
            }
        }
        catch(Exception e) { }
    }
    public default void log(String... text) {
        if (!Rotp.logging)
            return;
        String output = concat(text);
        try {
            System.out.println(output);
            if (Logger.logListener != null) {
                Logger.logListener.accept(output);
            }
        }
        catch(Exception e) { e.printStackTrace(); }
    }
    public default int maximumSystems()    { return Rotp.maximumSystems; }
    public default boolean veryLowMemory() {
        return (Rotp.maxHeapMemory < 500)
            || (galaxy() != null) && (galaxy().numStarSystems() > (maximumSystems()*3/4));
    }
    public default boolean lowMemory() {
        return (Rotp.maxHeapMemory < 800)
            || ((galaxy() != null) && (galaxy().numStarSystems() > (maximumSystems()/2)));
    }
    public default boolean midMemory()                 { return (galaxy() != null) && (galaxy().numStarSystems() > (maximumSystems()/3)); }
    public default Image image(String s)               { return ImageManager.current().image(s); }
    public default Image scaledImageW(String s, int w) { return ImageManager.current().scaledImageW(s, w); }
    public default int animationCount()                { return RotPUI.instance().animationCount(); }
    public default long animationMs()                  { return RotPUI.instance().animationMs(); }
    public default void allFrames(String key, int cnt, int imgIndex, List<Image> frames, List<Integer> refs)  {
        AnimationManager.current().allFrames(key, cnt, imgIndex, frames, refs);
    }
    public default BufferedImage currentFrame(String key)  {
        return AnimationManager.current().currentFrame(key);
    }
    public default BufferedImage currentFrame(String key, List<String> exclusions)  {
        return AnimationManager.current().currentFrame(key, exclusions);
    }
    public default void setFontHints(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        if(UserPreferences.antialiasing()) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
    }
    public default void resetAnimation(String key) {
        AnimationManager.current().reset(key);
    }
    public default List<BufferedImage> allExplosionFrames(String key) {
        return AnimationManager.current().allExplosionFrames(key);
    }
    public default float distance(float x0, float y0, float x1, float y1) {
        return (float) Math.sqrt( ((x1-x0)*(x1-x0)) + ((y1-y0)*(y1-y0)) );
    }
    public default float random()              { return rng().nextFloat(); }
    public default float random(float d)       { return d * random(); }
    public default float random(float low, float hi) { return low + ((hi-low)*random()); }
    public default <T> T random(T[] array) {
        return array == null ? null : array[(rng().nextInt(array.length))];
    }
    public default <T> T random(T[] array, Long seed) {
    	if (seed == null)
    		return random(array);
    	if (array == null)
    		return null;
        return array[(new Random(seed).nextInt(array.length))];
    }
    public default <T> T random(List<T> list) {
        return (list == null || list.isEmpty()) ? null : list.get(rng().nextInt(list.size()));
    }
    public default <T> T random(List<T> list, Long seed) {
    	if (seed == null)
    		return random(list);
    	if (list == null || list.isEmpty())
    		return null;
        return list.get(new Random(seed).nextInt(list.size()));
    }
    public default <T> T random(Set<T> list) {
        return random(new ArrayList<>(list));
    }
    public default void shuffle(List<?> list, Rand rand)	{
    	Collections.shuffle(list, new Random(rand.nextLong()));
    }
    public default void shuffle(List<?> list)	{
    	Collections.shuffle(list, new Random(rng().nextLong()));
    }    public default float asin(float d)  { return (float) FastMath.asin(d); }
    public default int bounds(int low, int val, int hi) {
        return Math.min(Math.max(low, val), hi);
    }
    public default float bounds(float low, float val, float hi) {
        return Math.min(Math.max(low, val), hi);
    }
    public default int roll(int low, int hi) {
        if (low == hi)
            return low;
        return low+(int)((hi-low+1)*random());
    }
    public default int summate(float n) {
        int n1 = (int) n;
        if (n1 < 1)
            return 0;
        else
            return  n1*(n1+1)/2;
    }
    public default int fibonacci(int n) {
        int a = 0;
        int b = 1;
        for (int i=0;i<n;i++) {
            int sum = a+b;
            a=b;
            b=sum;
        }
        return b;
    }
    // modnar: add "Quarter-squares plus 1" function [A033638] for warp speed
    public default int quarterSquaresPlusOne(int n) {
        int a = (int)(Math.floor(n * n / 4) + 1);
        return a;
    }
    public default List<String> varTokens(String s, String key) {
        String startKey = concat("[",key,"_");
        int keySize = startKey.length();
        List<String> tokens = new ArrayList<>();
        int prevIndex = -1;
        int nextIndex = s.indexOf(startKey, prevIndex);
        while (nextIndex >= 0) {
            int endIndex = s.indexOf(']', nextIndex);
            if (endIndex <= nextIndex)
                return tokens;
            String var = s.substring(nextIndex+keySize-1, endIndex);
            tokens.add(var);
            prevIndex = nextIndex;
            nextIndex = s.indexOf(startKey, endIndex);
        }
        return tokens;
    }
    public default String concat(String s1)   { return s1; }
    public default String concat(String s1, String s2) { return str(s1).concat(str(s2)); }
    public default String concat(String... s) {
        if (s.length == 1)
            return s[0];
        if (s.length == 2)
            return s[0].concat(s[1]);

        StringBuilder sb = new StringBuilder(s.length*16);
        for (int i=0;i<s.length;i++)
            sb.append(s[i]);
        return sb.toString();
    }
    public default float sqrt(int i)                 { return (float) Math.sqrt(i); }
    public default float sqrt(float f)               { return (float) Math.sqrt(f); }
    public default float abs(float v1)           	 { return Math.abs(v1); }
    public default int abs(int v1)              	 { return Math.abs(v1); }
    public default int ceil(float f)              	 { return (int) Math.ceil(f); }
    public default int round(float f)              	 { return Math.round(f); }
    public default int round(double f)             	 { return (int) Math.round(f); }
    public default int max(int v1, int v2)           { return Math.max(v1,v2); }
    public default int max(int v1, int v2, int v3)   { return max(v1,max(v2,v3)); }
    public default int max(int... n) {
        int max = n[0];
        for (int i=1;i<n.length;i++) {
            if (n[i] > max)
                max = n[i];
        }
        return max;
    }
    public default float max(float v1, float v2)            { return Math.max(v1,v2); }
    public default float max(float v1, float v2, float v3) { return max(v1,max(v2,v3)); }
    public default float max(float... n) {
        float max = n[0];
        for (int i=1;i<n.length;i++) {
            if (n[i] > max)
                max = n[i];
        }
        return max;
    }
    public default int min(int v1, int v2)           { return Math.min(v1,v2); }
    public default int min(int v1, int v2, int v3)   { return min(v1,min(v2,v3)); }
    public default int min(int... n) {
        int min = n[0];
        for (int i=1;i<n.length;i++) {
            if (n[i] < min)
                min = n[i];
        }
        return min;
    }
    public default float min(float v1, float v2)            { return Math.min(v1,v2); }
    public default float min(float v1, float v2, float v3) { return min(v1,min(v2,v3)); }
    public default float min(float... n) {
        float min = n[0];
        for (int i=1;i<n.length;i++) {
            if (n[i] < min)
                min = n[i];
        }
        return min;
    }
    public default float avg(float v1, float v2)            { return (v1+v2)/2; }
    public default float avg(float v1, float v2, float v3) { return (v1+v2+v3)/3; }
    public default float avg(float... n) {
        float sum = n[0];
        for (int i=1;i<n.length;i++)
            sum = n[i];
        return sum/n.length;
    }
    public default String scifmt(float d, int n) {
        String res = null;
        switch(n) {
            case 1:  res = sf1.format(d); break;
            case 2:  res = sf2.format(d); break;
            case 3:  res = sf3.format(d); break;
            case 4:  res = sf4.format(d); break;
            case 5:  res = sf5.format(d); break;
            case 6:  res = sf6.format(d); break;
            case 7:  res = sf7.format(d); break;
            case 8:  res = sf8.format(d); break;
            default: res = sf3.format(d); break;
        }
        return res.replace('E','e');
    }
    public default String fmt(float d, int n) {
        if (n == 0)
            return str((int)d);
        switch(n) {
            case 1:  return df1.format(d);
            case 2:  return df2.format(d);
            case 3:  return df3.format(d);
            case 4:  return df4.format(d);
            case 5:  return df5.format(d);
            case 6:  return df6.format(d);
            default: return df3.format(d);
        }
    }
    public default String fmt(float d) {
        if (Math.abs(d) < .0005)
            return "0";
        else if (d < .1)
            return df3.format(d);
        else if (d < 1)
            return df2.format(d);
        else if (d < 10)
            return df1.format(d);
        else
            return str((int)d);
    }
    public default String shortFmt(float d) {
        return shortFmt((int) d);
    }
    public default String shortFmt(int amt) {
        // shows integer until 9999, then shows 3 digits of precision for higher numbers
        // using K/M/B for thousands, millions and billions
        if (amt < 1e4)
            return str(amt);
        else if (amt < 1e5) 
            return text("NUM_FORMAT_THOUSANDS", df1.format(amt/1000f));       
        else if (amt < 1e6) {
            amt = amt/1000;
            return text("NUM_FORMAT_THOUSANDS", amt);            
        }
        else if (amt < 1e7) {
            String amtStr =df2.format(amt/1000000f);
            return text("NUM_FORMAT_MILLIONS", amtStr);
        }   
        else if (amt < 1e8) {
            String amtStr =df1.format(amt/1000000f);
            return text("NUM_FORMAT_MILLIONS", amtStr);
        }   
        else if (amt < 1e9) {
            amt = amt / 1000000;
            return text("NUM_FORMAT_MILLIONS", amt);
        }
        else if (amt < 1e10) {
            String amtStr =df2.format(amt/1000000000f);
            return text("NUM_FORMAT_BILLIONS", amtStr);
        }
        else if (amt < 1e11) {
            String amtStr =df1.format(amt/1000000000f);
            return text("NUM_FORMAT_BILLIONS", amtStr);
        }
        else  {
            amt = amt/1000000000;
            return text("NUM_FORMAT_BILLIONS", amt);
        }
    } 
    public default float round(float val, float precision) {
        return ((int)((val+(precision/2.0))/precision)) * precision;
    }
    public default int round(float val, int precision) {
        return ((int)((val+(precision/2))/ precision)) * precision;
    }
    public default int getAlpha(int pixel) { return (pixel >> 24) & 0xFF; }
    public default int getRed(int pixel)   { return (pixel >> 16) & 0xFF; }
    public default int getGreen(int pixel) { return (pixel >> 8) & 0xFF; }
    public default int getBlue(int pixel)  { return (pixel >> 0) & 0xFF; }
    public default Tech tech(String id)    { 
    	return TechLibrary.current().tech(id);
    }

    public default String date(float n) {
        int year = (int) n;
        float frac = n - (int) n;
        int month = (int) (frac * 12);
        int day =  (int)  ((frac - (month / 12)) * 30);

        return concat(str(year), ".", monthName[month], ".", str(++day));
    }
    public default String displayYearOrTurn() {
        if (options().displayYear())
            return text("MAIN_YEAR_DISPLAY", galaxy().currentYear());
        else
            return text("MAIN_TURN_DISPLAY", galaxy().currentTurn());
    }
    public default boolean equal(float d1, float d2, float precision) {
        return Math.abs(d1-d2) < precision;
    }
    public default List<String> substrings(String input, char delim) {
        return substrings(input,delim,0);
    }
    public default List<String> substrings(String input, char delim, int min) {
        // min - minimum number of fields to be returned... missing fields returned as empty
        List<String> res = new ArrayList<>();
        int from = 0;
        int mark = 0;
        String subString;

        while ((mark >= 0) || (res.size()<min)) {
            mark = input.indexOf(delim, from);
            if ((mark < 0) || (res.size() == (min-1)))
                subString = input.substring(from).trim();
            else
                subString = input.substring(from, mark).trim();
            res.add(subString);
            from = mark + 1;
        }
        return res;
    }
    public default float pow(float d, int e) {
        // Math.pow(d1,d2) is slow for integer exponents
        // there are faster algorithms for large values of e,
        // but this game predominantly uses e <10, so this is fine
        float res = 1;
        if (e > 0) {
            for (int i=1;i<=e;i++)
                res *= d;
        }
        else if (e < 0) {
            for (int i=-1;i>=e;i--) 
                res /= d;
                }
        return res;
    }
    public default float parseFloat(String s0) throws NumberFormatException {
        String s = s0.trim();
        if (s.isEmpty())
            return 0.0f;
        // checks for scientific notation
        if (s.contains("e")) {
            List<String> strings = substrings(s, 'e', 2);
            try { float num = Float.valueOf(strings.get(0));
                int exp = Integer.valueOf(strings.get(1));
                return num*pow(10,exp);
            }
            catch (NumberFormatException e) {
                err("Base.parseDouble (1) -- error parsing: " + s);
                throw e;
            }
        }

        try { return Float.valueOf(s); }
        catch (NumberFormatException e) {
            err("Base.parseDouble (2) -- error parsing: " + s);
            throw e;
        }
    }
    public default List<String> parsedValues(String s, char delim) {
        // used for parsing delimited text file lines that may be commented
        // null means EOF... preserve that
        if (s == null)
            return null;
        // trim spaces
        String s1 = s.trim();

        if (s1.isEmpty())
            return new ArrayList<>();
        else {
            char char0 = s1.charAt(0);
            if ((char0 == '/') || (char0 == '\\') || (char0 == '#') || (char0 == '*'))
                // if a comment, then return empty string
                return new ArrayList<>();
            else
                // else return the trimmed string
                return substrings(s, delim);
        }
    }

    public default int parseInt(String s0) throws NumberFormatException {
        String s = s0.trim();
        if (s.isEmpty())
            return 0;
        try { return Integer.valueOf(s.trim()); }
        catch (NumberFormatException e) {
            err("Base.parseInteger -- error parsing: " + s);
            throw e;
        }
    }
    public default Color parseColor(String s) throws NumberFormatException {
        if (s.trim().isEmpty())
            return new Color(0,0,0);
        int red = 0;
        int green = 0;
        int blue = 0;
        List<String> rgbs = substrings(s,',');
        try {
            if (rgbs.size() > 0)
                red = parseInt(rgbs.get(0));
            if (rgbs.size() > 1)
                green = parseInt(rgbs.get(1));
            if (rgbs.size() > 2)
                blue = parseInt(rgbs.get(2));
        }
        catch (NumberFormatException e) {
            err("Base.parseColor -- error parsing: " + s);
            throw e;
        }
        return new Color(red,green,blue);
    }
    public default boolean isComment(String line) {
        String s = line.trim();
        if (s.isEmpty())
            return true;

        char char0 = s.charAt(0);
        if ((char0 == '/') || (char0 == '\\') || (char0 == '#'))
            return true;

        // special char in UTF-8
        if (char0 == 65279)
            return true;
        return false;
    }
    public default URL url(String n) {
        return Rotp.class.getResource(n);
    }
    public default ImageIcon icon(String n)  {
        return icon(n, true);
    }
    public default String webpImageName(String wavFile) {
        int idx = wavFile.lastIndexOf('.');
        String filenameWithoutExtension;
        if (idx > 0) {
            filenameWithoutExtension = wavFile.substring(0, idx);
        } else {
            filenameWithoutExtension = wavFile;
        }
        String filenameWithExtension = filenameWithoutExtension + ".webp";
        return filenameWithExtension;
    }
    public default ImageIcon icon(String n, boolean logError)  {
        if ((n == null) || n.isEmpty()) {
           //("Base.icon() -- resource is empty or null");
            return null;
        }

        String webp = webpImageName(n);
        {
            URL resource = null;
            try {
                resource = url(webp);
            }
            catch(Exception e) {
//                err("Base.icon() -- error retrieving webp resource: ", webp+" : ", e.getMessage());
            }
            if (resource == null) {
                if (logError) {
//                    err("Base.icon() -- webp Resource not found:", webp);
                }
            }
            else {
                try {
                    BufferedImage img = ImageIO.read(resource);
                    if (img != null) {
                        return new ImageIcon(img);
                    } else {
                        // fail silently, fall back to jpg/png
                    }
                } catch (IOException e) {
                    err("Base.icon() -- error retrieving webp resource: ", webp+" : ", e.getMessage());
                }
            }
        }

        URL resource = null;
        try {
            resource = url(n);
        }
        catch(Exception e) {
            err("Base.icon() -- error retrieving resource: ", n+" : ", e.getMessage());
            return null;
        }
        if (resource == null) {
            if (logError) 
                err("Base.icon() -- Resource not found:", n);
            return null;
        }
        else 
            return new ImageIcon(resource);
    }
    public default File file(String n) {
        return new File(Rotp.jarPath(), n);
    }
    public default InputStream fileInputStream(String n) {
        String fullString = "../rotp/" +n;

        try { return new FileInputStream(new File(Rotp.jarPath(), n)); } 
        catch (FileNotFoundException e) {
            try { return new FileInputStream(fullString); } 
            catch (FileNotFoundException ex) {
                return Rotp.class.getResourceAsStream(n);
            }
        }
    }
    public default boolean readerExists(String n) {
        String fullString = "../rotp/" +n;
        FileInputStream fis = null;
        // InputStreamReader in = null;
        InputStream zipStream = null;

        try {
            fis = new FileInputStream(new File(Rotp.jarPath(), n));
        } catch (FileNotFoundException e) {
            try {
                fis = new FileInputStream(fullString);
            } catch (FileNotFoundException ex) {
                zipStream = Rotp.class.getResourceAsStream(n);
            }
        }

        boolean exists = (fis != null) || (zipStream != null);

        try {
            if (fis != null) 
                fis.close();
            else if (zipStream != null)
                zipStream.close();
        }
        catch(IOException e) {};

        return exists;
    }
    public default BufferedReader reader(String n) {
        String fullString = "../rotp/" +n;
        FileInputStream fis = null;
        InputStreamReader in = null;
        InputStream zipStream = null;

        try {
            fis = new FileInputStream(new File(Rotp.jarPath(), n));
        } catch (FileNotFoundException e) {
            try {
                fis = new FileInputStream(fullString);
            } catch (FileNotFoundException ex) {
                zipStream = Rotp.class.getResourceAsStream(n);
            }
        }

        try {
            if (fis != null)
                in = new InputStreamReader(fis, "UTF-8");
            else if (zipStream != null)
                in = new InputStreamReader(zipStream, "UTF-8");
            else
                err("Base.reader() -- FileNotFoundException:", n);
        } catch (IOException ex) {
            err("Base.reader() -- UnsupportedEncodingException: ", n);
        }

        if (in == null)
            return null;

        return new BufferedReader(in);
    }
    public default PrintWriter writer(String n) {
        String fullString = "src/rotp/" +n;
        try {
            FileOutputStream fout = new FileOutputStream(new File(fullString));
            return new PrintWriter(fout, true);
        }
        catch (FileNotFoundException e) {
            err("Base.writer -- " + e);
            e.printStackTrace();
            return null;
        }
    }
    public default InputStream inputStream(String n) {
        InputStream stream = null;
        File fontFile = new File(n);
        if (fontFile.exists())
            try {
                stream = new FileInputStream(fontFile);
            }
            catch (FileNotFoundException e) {
                err("Base.fileStream -- FileNotFoundException: " + n);
            }
        else {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(Rotp.jarFileName);
                ZipEntry ze = jarFile.getEntry(n);
                if (ze != null)
                    stream = jarFile.getInputStream(ze);
            }
            catch (IOException e) {
                err("Base.fileStream -- IOException: " + n);
            }
            finally {
                try {
                    if (jarFile != null)
                        jarFile.close();
                } catch (IOException e) {}
            }
        }
        return stream;
    }
    public default OutputStream outputStream(String s) throws IOException {
        try {
            OutputStream file = new FileOutputStream(s);
            OutputStream buffer = new BufferedOutputStream(file);
            return buffer;
        }
        catch(IOException e){
            log("Cannot create output file: ", s);
            log(e.getMessage());
            throw(e);
        }
    }
    public static int compare(int a, int b)        { return Integer.compare(a,b); }
    public static int compare(float a, float b)  { return Float.compare(a, b); }
    public default Color newColor(int r, int g, int b) {
        return newColor(r,g,b,255);
    }
    public default Color newColor(int r, int g, int b, int a) {
        //log("Creating color r:"+r+" g:"+g+" b:"+b+" a:"+a);
        return new Color(r,g,b,a);
    }
    public default String str(String s) { return s == null ? "null" : s; }
    public default String str(long i)   { return Long.toString(i); }
    public default String str(int i)    { return Integer.toString(i); }
    public default String str(float i)  { return Float.toString(i); }
    public default BufferedImage flip(BufferedImage img) {
        if (img == null)
            return null;
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage flippedImg = newBufferedImage(w, h);
        Graphics g = flippedImg.getGraphics();
        g.drawImage(img, w, 0, 0, h, 0, 0, w, h, null);
        g.dispose();
        return flippedImg;
    }
    public default BufferedImage newOpaqueImage(int w, int h) {
        //log("Creating image w:"+w+"  h:"+h);
        return BasePanel.gc().createCompatibleImage(w, h);
    }
    public default BufferedImage newBufferedImage(int w, int h) {
        return BasePanel.gc().createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    }
    public default BufferedImage asBufferedImage(Image image) {
        if (image == null)
            return null;
        if (image instanceof BufferedImage)
            return (BufferedImage) image;
        return newBufferedImage(image);
    }
    public default BufferedImage newOpaqueImage(Image image) {
        BufferedImage bufferedImage = newOpaqueImage(image.getWidth(null), image.getHeight(null));
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(image, null, null);
        g.dispose();
        return bufferedImage;
    }
    public default BufferedImage newBufferedImage(Image image) {
        BufferedImage bufferedImage = newBufferedImage(image.getWidth(null), image.getHeight(null));
        Graphics2D g = bufferedImage.createGraphics();
        g.drawImage(image, null, null);
        g.dispose();
        return bufferedImage;
    }
    public default Border newLineBorder(Color c, int th) {
        return BorderFactory.createLineBorder(c,scaled(th));
    }
    public default Border newEmptyBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(scaled(top), scaled(left), scaled(bottom), scaled(right));
    }
    public default String replaceDigits(String s0) {
        String s = s0;
        if (LanguageManager.customDigits != null) {
            char[] oldDigits = LanguageManager.latinDigits;
            char[] newDigits = LanguageManager.customDigits;
            int n = min(oldDigits.length, newDigits.length);
            for (int j=0;j<n;j++)
                s = s.replace(oldDigits[j], newDigits[j]);
        }
        return s;
    }
    public default String strFormat(String fmt, int n) { return String.format(fmt, n);  }
    public default void drawString(Graphics g, String str0, int x, int y) {
        String str = replaceDigits(str0);
        g.drawString(str, x, y);
    }
    public default void drawBorderedString(Graphics g, String str, int x, int y, Color back, Color fore) {
        drawBorderedString(g, str, 1, x, y, back, fore);
    }
    public default void drawBorderedString(Graphics g, String str, int th, int x, int y, Color back, Color fore) {
        if (str == null)
            return;
        g.setColor(back);
        int thick = th;
        int start = 0-thick;
        for (int x0=start;x0<=thick;x0++) {
            int x0s = scaled(x0);
            for (int y0=start;y0<=thick;y0++) {
                int y0s = scaled(y0);
                drawString(g,str, x+x0s, y+y0s);
            }
        }
        g.setColor(fore);
        drawString(g,str,  x, y);
    }
    public default void drawShadowedString(Graphics g, String str, int x, int y, Color back, Color fore) {
        drawShadowedString(g, str, 1, x, y, back, fore);
    }
    public default void drawShadowedString(Graphics g, String str, int thick, int x, int y, Color back, Color fore) {
        drawShadowedString(g, str, 0, thick, x, y, back, fore);
    }
    public default void drawAlphaShadowedString(Graphics2D g, float alpha, String str, int thick, int x, int y, Color back, Color fore) {
        drawAlphaShadowedString(g, alpha, str, 0, thick, x, y, back, fore);
    }
    public default void drawShadowedString(Graphics g, String str, int th0, int th1, int x, int y, Color back, Color fore) {
        g.setColor(back);

        int topThick = scaled(th0);
        int thick = th1;
        for (int x0=(0-topThick);x0<=thick;x0++) {
            int x0s = scaled(x0);
            for (int y0=(0-topThick);y0<=thick;y0++) {
                int y0s = scaled(y0);
                drawString(g,str, x+x0s, y+y0s);
            }
        }
        g.setColor(fore);
        drawString(g,str,  x, y);
    }
    public default void drawShadowedString(Graphics g, String str, int scale,  int th0, int th1, int x, int y, Color back, Color fore) {
        g.setColor(back);

        int topThick = scaled(th0);
        int thick = scaled(th1);
        int incr = scaled(scale);
        for (int x0=(0-topThick);x0<=thick;x0+=incr) {
            for (int y0=(0-topThick);y0<=thick;y0+=incr)
                drawString(g,str, x+x0, y+y0);
        }
        g.setColor(fore);
        drawString(g,str,  x, y);
    }
    public default void drawAlphaShadowedString(Graphics2D g, float alpha, String str, int th0, int th1, int x, int y, Color back, Color fore) {
        if (alpha <= 0)
            return;

        int mult = 2*(th0+th1+1)*(th0+th1+1);

        Composite c = g.getComposite();

        if (alpha < 1)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha/mult));

        g.setColor(back);

        int topThick = scaled(th0);
        int thick = scaled(th1);
        for (int x0=(0-topThick);x0<=thick;x0++) {
            for (int y0=(0-topThick);y0<=thick;y0++)
                drawString(g,str, x+x0, y+y0);
        }
        if (alpha < 1)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha/2));

        g.setColor(fore);
        drawString(g,str,  x, y);
        g.setComposite(c);
    }
    public default void drawBoldString(Graphics g,  String str, int x, int y) {
        for (int x0=0;x0<=1;x0++) {
            for (int y0=0;y0<=1;y0++)
                drawString(g,str, x+x0, y+y0);
        }
    }
    public default void drawBoldString(Graphics g,  String str, int x, int y,Color fore) {
        g.setColor(fore);

        for (int x0=0;x0<=1;x0++) {
            for (int y0=0;y0<=1;y0++)
                drawString(g,str, x+x0, y+y0);
        }
    }
    public default boolean isLightColor(Color c) {
        return (c.getRed()+c.getGreen()+c.getBlue()) > 320;
    }
    public default void drawShadedPolygon(Graphics g, int[] x, int[] y, Color c0, int offsetX, int offsetY) {
        int x0[] = new int[x.length];
        int y0[] = new int[y.length];

        Color cLight = c0.brighter();
        Color cShade = c0.darker();

        // draw Shade
        for (int i=0;i<x.length;i++) {
            x0[i]=x[i]-offsetX; y0[i]=y[i]-offsetY;
        }
        g.setColor(cShade);
        g.fillPolygon(x0,y0,x0.length);

        // draw Light
        for (int i=0;i<x.length;i++) {
            x0[i]=x[i]+offsetX; y0[i]=y[i]+offsetY;
        }
        g.setColor(cLight);
        g.fillPolygon(x0,y0,x0.length);

        // draw normal
        g.setColor(c0);
        g.fillPolygon(x,y,x.length);
    }
    public default List<String> wrappedLines(Graphics g, String text, int maxWidth) {
        return wrappedLines(g, text, maxWidth, 0);
    }
    public default List<String> wrappedLines(Graphics g, String text, int maxWidth, int line1Indent) {
    	// BR: Zero would lead to infinite loop
        maxWidth = max(scaled(5), maxWidth);
        List<String> lines = new ArrayList<>();

        FontMetrics fm = g.getFontMetrics();
        int indent = line1Indent;
        String currentLine = "";

        if (LanguageManager.current().currentLogographic()) {
            for (int i=0;i<text.length();i++) {
                String newLine = currentLine;
                String word = text.substring(i, i+1);
                newLine = newLine + word;
                int newWidth = fm.stringWidth(newLine);
                if (newWidth > (maxWidth-indent)) {
                    lines.add(currentLine);
                    indent = 0;
                    currentLine = word;
                }
                else
                    currentLine = newLine;
            }
        }
        else { // BR: added forced lines '\n' management 
            String[] forcedLines = text.split(NEWLINE);
        	for (String forcedLine : forcedLines) {
                List<String> words = substrings(forcedLine, ' ');
                if (!currentLine.isEmpty())
                	lines.add(currentLine);
                currentLine = "";
        		if (forcedLine.trim().isEmpty()) { // requested blank line
        			lines.add("");
        			continue;
        		}
                for (String word: words) {
                    String newLine = currentLine;
                    if (newLine.isEmpty())
                        newLine = newLine + word;
                    else
                        newLine = newLine + " " + word;
                    int newWidth = fm.stringWidth(newLine);
                    if (newWidth > (maxWidth-indent)) {
                    	if (!currentLine.isEmpty())
                    		lines.add(currentLine);
                        indent = 0;
                        currentLine = word;
                    }
                    else
                        currentLine = newLine;
                }
        	}
        }
//        else { // Character based
//            List<String> words = substrings(text, ' ');
//            for (String word: words) {
//                String newLine = currentLine;
//                if (newLine.isEmpty())
//                    newLine = newLine + word;
//                else
//                    newLine = newLine + " " + word;
//                int newWidth = fm.stringWidth(newLine);
//                if (newWidth > (maxWidth-indent)) {
//                    lines.add(currentLine);
//                    indent = 0;
//                    currentLine = word;
//                }
//                else
//                    currentLine = newLine;
//            }
//        }

        if (!currentLine.isEmpty())
            lines.add(currentLine);

        return lines;
    }
    public default List<String> scaledPlainWrappedLines(Graphics g, String text, int maxWidth, int maxLines, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        g.setFont(plainFont(fontSize));
        List<String> wrappedLines = wrappedLines(g, text, maxWidth);
        while ((wrappedLines.size() > maxLines) && (fontSize > minFont)) {
            fontSize--;
            g.setFont(plainFont(fontSize));
            wrappedLines = wrappedLines(g, text, maxWidth);
        }
        return wrappedLines;
    }
    public default List<String> scaledNarrowWrappedLines(Graphics g, String text, int maxWidth, int maxLines, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        g.setFont(narrowFont(fontSize));
        List<String> wrappedLines = wrappedLines(g, text, maxWidth);
        while ((wrappedLines.size() > maxLines) && (fontSize > minFont)) {
            fontSize--;
            g.setFont(narrowFont(fontSize));
            wrappedLines = wrappedLines(g, text, maxWidth);
        }
        return wrappedLines;
    }
    public default List<String> scaledDialogueWrappedLines(Graphics g, String text, int maxWidth, int maxLines, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        g.setFont(dlgFont(fontSize));
        List<String> wrappedLines = wrappedLines(g, text, maxWidth);
        while ((wrappedLines.size() > maxLines) && (fontSize > minFont)) {
            fontSize--;
            g.setFont(dlgFont(fontSize));
            wrappedLines = wrappedLines(g, text, maxWidth);
        }
        return wrappedLines;
    }
    /**
    * First choice is dialogue font... But if the dialogue font
    * can not display the String str, return the narrow font
    * (dialogue font do not include Greek Character Set)
    * Negative value is returned for narrow fonts
    */
    default int scaledDialogueOrPlainFontSize(Graphics g, String text, int maxWidth, int maxLines, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        Font font = dlgFont(fontSize);
        if (font.canDisplayUpTo(text) == -1)
        	return scaledDialogueFontSize(g, text, maxWidth, maxLines, desiredFont, minFont);
        else
        	return -scaledPlainFontSize(g, text, maxWidth, maxLines, desiredFont, minFont);
    }
    default int scaledPlainFontSize(Graphics g, String text, int maxWidth, int maxLines, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        g.setFont(plainFont(fontSize));
        List<String> wrappedLines = wrappedLines(g, text, maxWidth);
        while ((wrappedLines.size() > maxLines) && (fontSize > minFont)) {
            fontSize--;
            g.setFont(plainFont(fontSize));
            wrappedLines = wrappedLines(g, text, maxWidth);
        }
        return fontSize;
    }
    default int scaledDialogueFontSize(Graphics g, String text, int maxWidth, int maxLines, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        g.setFont(dlgFont(fontSize));
        List<String> wrappedLines = wrappedLines(g, text, maxWidth);
        while ((wrappedLines.size() > maxLines) && (fontSize > minFont)) {
            fontSize--;
            g.setFont(dlgFont(fontSize));
            wrappedLines = wrappedLines(g, text, maxWidth);
        }
        return fontSize;
    }
    public default List<String> scaledWrappedLines(Graphics g, String text, int maxWidth, int maxLines, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        g.setFont(font(fontSize));
        List<String> wrappedLines = wrappedLines(g, text, maxWidth);
        while ((wrappedLines.size() > maxLines) && (fontSize > minFont)) {
            fontSize--;
            g.setFont(font(fontSize));
            wrappedLines = wrappedLines(g, text, maxWidth);
        }
        return wrappedLines;
    }
    /**
     * scaled narrow Font
     */
    public default int scaledFont(Graphics g, String text, int maxWidth, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        g.setFont(narrowFont(fontSize));
        while ((g.getFontMetrics().stringWidth(text) > maxWidth) && (fontSize > minFont)) {
            fontSize--;
            g.setFont(narrowFont(fontSize));
        }
        return fontSize;
    }
    /**
     * scaled normal Font
     */
    public default int scaledFontSize(Graphics g, String text, int maxWidth, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        g.setFont(font(fontSize));
        while ((g.getFontMetrics().stringWidth(text) > maxWidth) && (fontSize > minFont)) {
            fontSize--;
            g.setFont(font(fontSize));
        }
        return fontSize;
    }
    public default int scaledLogoFont(Graphics g, String text, int maxWidth, int desiredFont, int minFont) {
        int fontSize = desiredFont;
        g.setFont(logoFont(fontSize));
        while ((g.getFontMetrics().stringWidth(text) > maxWidth) && (fontSize > minFont)) {
            fontSize--;
            g.setFont(logoFont(fontSize));
        }
        return fontSize;
    }
    public default void invokeAndWait(Runnable runnable) {
        if (EventQueue.isDispatchThread())
            runnable.run();
        else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    public default void invokeLater(Runnable runnable) {
        try {
            SwingUtilities.invokeLater(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public default void drawBackgroundStars(BufferedImage img, ImageObserver obs) {
        drawBackgroundStars(img, img.getGraphics(), img.getWidth(obs), img.getHeight(obs), scaled(50), scaled(100));
    }
    public default void drawBackgroundStars(BufferedImage img, ImageObserver obs, int minDist, int varDist) {
        drawBackgroundStars(img, img.getGraphics(), img.getWidth(obs), img.getHeight(obs), minDist, varDist);
    }
    public default void drawBackgroundStars(Graphics g, int w, int h) {
        drawBackgroundStars(g, w, h, scaled(50), scaled(100));
    }
    public default void drawBackgroundStars(Graphics g, int w, int h, int minDist, int varDist) {
        // draw background stars
        g.setColor(newColor(0,0,0,0));
        g.fillRect(0,0,w,h);
        int count = w*h;
        int p = 0;
        // BR: added security Crashed once, probably because random was too close to 1!?
        int w_ = max(0, w-1);
        int h_ = max(0, h-1);

        // Color dimmest = newColor(32,32,32);
        // Color dimmer = newColor(48,48,48);
        // Color dim = newColor(64,64,64);
        Color avg = newColor(96,96,96);
        Color bright = newColor(144,144,144);
        Color brighter = newColor(196,196,196);
        Color brightest = newColor(255,255,255);

        int s1 = BasePanel.s1;
        int s2 = BasePanel.s2;

        while (p < count) {
            p += (minDist + (int) Math.ceil(random()*varDist));
            // int x1 = p % w; // BR: was useless
            // int y1 = p / h;
            int x1 = (int) Math.ceil(random()*w_);
            int y1 = (int) Math.ceil(random()*h_);
            int roll = (int) Math.ceil(random()*100);
			// modnar: test reduce background star speckle
			/*
            if (roll <= 50)
                g.setColor(dimmest);
            else if (roll <= 75)
                g.setColor(dimmer);
            else if (roll <= 87)
                g.setColor(dim);
            else if (roll <= 93)
                g.setColor(avg);
            else if (roll <= 97)
                g.setColor(bright);
            else if (roll <= 99)
                g.setColor(brighter);
            else
                g.setColor(brightest);
			*/
			// modnar: test reduce background star speckle
			if (roll <= 50)
                g.setColor(avg);
            else if (roll <= 75)
                g.setColor(bright);
            else if (roll <= 90)
                g.setColor(brighter);
            else
                g.setColor(brightest);

            if ((roll > 95) && (random() < .2))
                g.fillRoundRect(x1, y1, s2, s2, s2, s2);
            else if (random() < .1)
                g.fillRect(x1,y1,s1,s1);
        }
    }
    public default void drawBackgroundStars(BufferedImage image, Graphics g, int w, int h, int minDist, int varDist) {
        // draw background stars
        g.setColor(newColor(0,0,0,0));
        g.fillRect(0,0,w,h);
        int count = w*h;
        int p = 0;
        // BR: added security Crashed once, probably because random was too close to 1!?
        int w_ = max(0, w-1);
        int h_ = max(0, h-1);

        int s1 = BasePanel.s1;
        int s2 = BasePanel.s2;
		// int s4 = BasePanel.s4;

        while (p < count) {
            p += (minDist + (int) Math.ceil(random()*varDist));
            // int x1 = p % w; // BR: was useless
            // int y1 = p / h;
            int x1 = (int) Math.floor(random()*w_);
            int y1 = (int) Math.floor(random()*h_);
            int clr=  image.getRGB(x1,y1);
            int  red   = (clr & 0x00ff0000) >> 16;
            int  green = (clr & 0x0000ff00) >> 8;
            int  blue  =  clr & 0x000000ff;
            int minPixelValue = 0;
            int roll = (int) Math.ceil(random()*100);
            if (roll <= 50)
                minPixelValue = 96;
            else if (roll <= 75)
                minPixelValue = 128;
            else if (roll <= 90)
                minPixelValue = 144;
            else
                minPixelValue = 196;
            g.setColor(new Color(max(red,minPixelValue), max(green, minPixelValue), max(blue, minPixelValue)));
            if ((roll > 95) && (random() < .2))
                g.fillRoundRect(x1, y1, s2, s2, s2, s2);
            else if (random() < .1)
                g.fillRect(x1,y1,s1,s1);
        }
    }
    public default BufferedImage makeTransparent(Image img, Color c) {
        colorizer.image(img);
        colorizer.onlySpecificColor(c);
        return colorizer.makeTransparent();
    }
    public default String stringAt(List<String> names, int i) {
        if ((i >= names.size()) || names.get(i).isEmpty())
            return names.get(0);
        else
            return names.get(i);
    }
    public default List<String> readSystemNames(String filePath) {
        BufferedReader reader = reader(filePath);
        if (reader == null)
            return null;

        List<String> names = new ArrayList<>();
        try {
            List<String> lineValues;
            while ((lineValues = (parsedValues(reader.readLine(), ','))) != null) {
                if (!lineValues.isEmpty())
                    names.add(lineValues.get(0));
            }
        }
        catch (IOException e) {
            err("Base.readFileLines: ", filePath, " -- IOException: ", e.toString());
        }
        finally {
            try {
                reader.close();
            } catch (IOException e) {}
        }
        return names;
    }
    public default void drawBackgroundNebula(BufferedImage img) {
        int imgW = img.getWidth();
        int imgH = img.getHeight();

        int nebR = 0;
        int nebG = 0;
        int nebB = roll(160,255);

        //int centerX = w/2;
        //int centerY = h/2;

        FastImage fImg = PlanetImager.current().terrainBase().copy();
        int w = fImg.getWidth();
        int h = fImg.getHeight();

        int floor = 255;
        int ceiling = 0;
        for (int y=0;y<h;y++)    for (int x=0;x<w;x++) {
            int pixel = fImg.getRGB(x, y);
            floor = min(floor, pixel & 0xff);
            ceiling = max(ceiling, pixel & 0xff);
        }
        for (int x=0;x<w;x++)   for (int y=0;y<h;y++) {
            int pixel = fImg.getRGB(x, y);
            int landLevel = pixel & 0xff;
            landLevel = (int) (256*((float)(landLevel-floor)/(ceiling-floor)));
            // int distFromEdgeX = min(x, w-x);
            // int distFromEdgeY = min(y, h-y);
            //int distFromEdge = min(distFromEdgeX, distFromEdgeY);
            // float pctFromEdge = min((float)distFromEdgeX/w, (float)distFromEdgeY/h);
            //int distFromCenter = (int) Math.min(128,Math.sqrt(((x-centerX)*(x-centerX))+((y-centerY)*(y-centerY))));
            //int alpha = min(distFromEdge/2, landLevel*3/5);
            int alpha = landLevel/4;
            //alpha = (int) (pctFromEdge * landLevel);
            //alpha = min(alpha*3/2, (alpha+255)/2);
            //alpha = Math.min(145-distFromCenter, landLevel/2);
            int newPixel = (alpha << 24) | (nebR << 16) | (nebG << 8) | nebB;
            fImg.setRGB(x, y, newPixel);
        }

        BufferedImage back = fImg.image();

        Graphics g = img.getGraphics();
        g.drawImage(back, 0, 0, imgW, imgH, null);
        g.dispose();
    }
    public default String generateGameName() {
		String name;
		if (playerIsCustom.get())
			name = playerCustomRace.getRace().setupName;
		else
			name = ISpecies.R_M.keyed(newGameOptions().selectedPlayerRace()).setupName();
		name +=   " - " + text(newGameOptions().selectedGalaxySize())
				+ " - " + text(newGameOptions().selectedGameDifficulty());
		// modnar: add custom difficulty level option, set in Remnants.cfg
		// append this custom difficulty percentage to gameName if selected
		if (newGameOptions().selectedGameDifficulty().equals(DIFFICULTY_CUSTOM)) {
			name += " (" + Integer.toString(newGameOptions().selectedCustomDifficulty()) + "%)";
		}
		return name;
    }
    public default String generateGameName(IGameOptions options) {
		String name;
		if (options.selectedPlayerIsCustom())
			name = options.playerCustomRace().getRace().setupName;
		else
			name = ISpecies.R_M.keyed(options.selectedPlayerRace()).setupName();
		name +=   " - " + text(options.selectedGalaxySize())
				+ " - " + text(options.selectedGameDifficulty());
		// modnar: add custom difficulty level option, set in Remnants.cfg
		// append this custom difficulty percentage to gameName if selected
		if (options.selectedGameDifficulty().equals(DIFFICULTY_CUSTOM)) {
			name += " (" + Integer.toString(options.selectedCustomDifficulty()) + "%)";
		}
		return name;
    }
    public default boolean has3Buttons() {
    	return MouseInfo.getNumberOfButtons() >= 3;
    	//return false;
    }
    // ----- Debug tools -----
    public default void writeToFile(String fileName, String s, boolean newLine, boolean append) {
        File saveFile = file(fileName);
        FileWriter fw = null;

		try {
			fw = new FileWriter(saveFile, append);
			fw.write(s);
			if (newLine)
				fw.write(System.getProperty( "line.separator" ));
		}
		catch(IOException ex) {}
        finally {
            try {
            	fw.close();
            }
            catch(IOException ex) {}
        }
    }
    public default String getTurn() {
    	return concat("Turn:", String.format("% 4d", galaxy().currentTurn()));
    }
    public default String getTurn(String subTurn) {
    	return concat(getTurn(), subTurn);
    }
    public default String msToHMS(long ms) {
    	long s = ms/1000;
    	long m = s/60;
    	long h = m/60;
    	ms -= s*1000;
    	s -= m*60;
    	m -= h*60;
    	return String.format("%02d:%02d:%02d.%03d", h, m, s, ms);
    }
    public default void turnLog(String fileName, String s) {
    	writeToFile(fileName, getTurn(" | ")+s, true, true);
    }
    public default Color setAlpha(Color color, float alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(),
				bounds(0, (int)(alpha*255+0.5), 255));
	}
    public default Color saturateColor(Color color, float alpha) {
    	int max = max(color.getRed(), max(color.getGreen(), color.getBlue()));
    	if (max == 0)
    		return setAlpha(Color.white, alpha);
    	else
    		return multColor(color, 255f/max, alpha);
	}
    public default Color multColor(Color color, float factor, float alpha) {
		factor = Math.max(0, factor/255);
		return new Color(Math.min(1f, color.getRed()   * factor),
						 Math.min(1f, color.getGreen() * factor),
						 Math.min(1f, color.getBlue()  * factor),
						 bounds(0f, alpha, 1f));
	}
    public default Color multColor(Color color, float factor) {
		factor = Math.max(0, factor/255);
		return new Color(Math.min(1f, color.getRed()   * factor),
						 Math.min(1f, color.getGreen() * factor),
						 Math.min(1f, color.getBlue()  * factor),
						 color.getAlpha()/255f);
	}
    public default  Point2D center(Rect rect) {
    	return new Point2D.Float((float) rect.getCenterX(), (float) rect.getCenterY());
    }
    default void memLog()	{ memLog(""); }
    default void memLog(String subTurn)	{
        System.out.println(getTurn(subTurn));
        System.out.println(memHeap());
        System.out.println(memNonHeap());
        System.out.println("Memory Pool: ");
        List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean : beans)
            System.out.println(memoryPoolToString(bean));
        System.out.println("Garbage Collector: ");
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans())
        	System.out.println(memoryGCToString(bean));
    }
    default String memNonHeap()	{
    	MemoryUsage mem = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
    	return StringUtils.rightPad("Non Heap: ", 32) + memoryToString(mem);
    }
    default String memHeap()	{
    	MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    	return StringUtils.rightPad("Heap: ", 32) + memoryToString(mem);
    }
    default String memoryToString(MemoryUsage mem)	{
    	String s = "";
    	s += " Init: "		+ StringUtils.leftPad((mem.getInit()     /MB) + " MB", 7);
    	s += " Used: "		+ StringUtils.leftPad((mem.getUsed()     /MB) + " MB", 7);
    	s += " Committed: " + StringUtils.leftPad((mem.getCommitted()/MB) + " MB", 7);
    	s += " Max: "		+ StringUtils.leftPad((mem.getMax()      /MB) + " MB", 7);
    	return s;
    }
    default String memoryPoolToString(MemoryPoolMXBean bean)	{
    	return StringUtils.rightPad(bean.getName(), 32)
    			+ memoryToString(bean.getUsage());
    }
    default String memoryGCToString(GarbageCollectorMXBean bean)	{
    	return StringUtils.rightPad(bean.getName(), 24)
       			+ " Count = " + StringUtils.leftPad(bean.getCollectionCount() + "",    3)
    			+ " Time = "  + StringUtils.leftPad(bean.getCollectionTime()  + " ms", 6);
    }
    default Integer getInteger(String text)	{
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}
    default Long getLong(String text)	{
		try {
			return Long.parseLong(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}
    default Float getFloat(String text)		{
		try {
			return Float.parseFloat(text);
		} catch (NumberFormatException e) {
			return null;
		}
	}
    default Graphics2D getGraphicsRH(Image image) {
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		return g;
    }
    default BufferedImage halvedImage(Image img) {
    	int w0 = img.getWidth(null);
        int h0 = img.getHeight(null);
    	int w1 = w0/2;
        int h1 = h0/2;
        BufferedImage tmp = new BufferedImage(w1, h1, TYPE_INT_ARGB);
        Graphics2D g2D = getGraphicsRH(tmp);
		g2D.drawImage(img, 0, 0, w1, h1, 0, 0, w0, h0, null);
		g2D.dispose();
		return tmp;
    }
    default BufferedImage resizeImage(Image img, int w, int h) {
    	int w0 = img.getWidth(null);
        int h0 = img.getHeight(null);
        float scale = min((float)w/w0, (float)h/h0);
        while (scale < 0.5) {
        	img = halvedImage(img);
        	w0 = img.getWidth(null);
        	h0 = img.getHeight(null);
        	scale = min((float)w/w0, (float)h/h0);
        }
        BufferedImage tmp = new BufferedImage(w, h, TYPE_INT_ARGB);
        Graphics2D g2D = getGraphicsRH(tmp);
		g2D.drawImage(img, 0, 0, w, h, 0, 0, w0, h0, null);
		g2D.dispose();
		return tmp;
    }
    default float pctToLive(int pctToDie)	{
    	// pct(distance) = 100 * exp(-decay*distance)
    	// -decay = ln(pct(1)/100)
    	int pctToLive = 100-pctToDie;
    	return (float) -Math.log(pctToLive/100.0);
    }
    default float liveToRatio(float survive, float distance)	{
    	return (float) (Math.exp(-survive * distance));
    }
    default int afterDecay(int num, float survive, float distance) {
    	float ratio = liveToRatio(survive, distance);
    	if (num > 100)
    		return (int) Math.round(num * ratio);
    	int remain = num;
    	for (int i=0; i<num; i++)
    		if (random()>ratio)
    			remain--;
    	return remain;
    }
    default BufferedImage rulesIcon(int width, int height, int cnr) {
    	return CustomIcons.instance.rulesIcon(width, height, cnr);
    }
    default BufferedImage settingsIcon(int width, int height, int cnr) {
    	return CustomIcons.instance.settingsIcon(width, height, cnr);
    }
    default BufferedImage subMenuIcon(int wi, int hi, Color rectCol, Color barCol, Color lineCol) {
    	return CustomIcons.instance.subMenuIcon(wi, hi, rectCol, barCol, lineCol);
    }
    default BufferedImage subMenuMoreIcon(int wi, int hi, Color rectCol, Color barCol, Color lineCol) {
    	return CustomIcons.instance.subMenuMoreIcon(wi, hi, rectCol, barCol, lineCol);
    }
    default BufferedImage eyeIcon(int wi, int hi, Color lineCol, boolean fillIris) {
    	return CustomIcons.instance.eyeIcon(wi, hi, lineCol, fillIris);
    }
	default BufferedImage targetIcon(int wi, int hi, Color lineCol, int CircleNum) {
		return CustomIcons.instance.targetIcon(wi, hi, lineCol, CircleNum);
	}
    default BufferedImage globalDefaultDesignIcon(int side, Color lineCol) {
    	return CustomIcons.instance.globalDefaultDesignIcon(side, lineCol);
    }
    default BufferedImage localDefaultDesignIcon(int side, Color lineCol) {
    	return CustomIcons.instance.localDefaultDesignIcon(side, lineCol);
    }
	default BufferedImage magnifierIcon(int side, Color lineCol) {
		return CustomIcons.instance.magnifierIcon(side, lineCol);
	}
    default void copyLabels(String baseName) {
		// First copy file from the modified source to final destination
		// Validated for eclipse IDE
		String jarPath  = Rotp.jarPath();
		String destName = "classes/" + baseName;	// the file that will be loaded
		String srcName  = "../src/" + baseName;		// up from target/class
		File f1 = new File(jarPath, destName);
		File f2 = new File(jarPath, srcName);
		if(!f1.exists())
			return; // Should not happen!

		if (f2.exists()) { // Validated for eclipse IDE -> update the file
			Path destpath   = f1.toPath(); 
			Path sourcepath = f2.toPath();
			try {
				Files.copy(sourcepath, destpath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				System.err.println("reloadLabels() IOException");
				System.err.println(e.toString());
			}
		}
	}
    default void debugReloadLabels(String langDir) {
		if (!Rotp.isIDE()) {
			// BR: Intended to only To be used from IDE
			Toolkit.getDefaultToolkit().beep();
			if (langDir == null || langDir.isEmpty())
				LanguageManager.current().reloadLanguage();
			else
				LanguageManager.selectLanguage(langDir);
			return;
		}
		Toolkit.getDefaultToolkit().beep();
		copyLabels("rotp/lang/en/labels.txt");
		copyLabels("rotp/lang/en/techs.txt");
		copyLabels("rotp/lang/fr/labels.txt");
		copyLabels("rotp/lang/fr/techs.txt");
		if (langDir == null || langDir.isEmpty())
			LanguageManager.current().reloadLanguage();
		else
			LanguageManager.selectLanguage(langDir);
		return;
    }
    default void debugReloadLabels(Component component) {
    	debugReloadLabels("");
    	component.repaint();
    }
	default Rectangle getStringBounds(Graphics2D g2, String str, float x, float y) {
		FontRenderContext frc = g2.getFontRenderContext();
		GlyphVector gv = g2.getFont().createGlyphVector(frc, str);
		return gv.getPixelBounds(null, x, y);
	}
    class Rect extends Rectangle {
    	private static final long serialVersionUID = 1L;
    	public Rect()				{ super(); }
    	public Rect(Point p)		{ super(p); }
    	public Rect(Dimension d)	{ super(d); }
    	public Rect(Rectangle r)	{ super(r); }
       	public Rect(Point p, Dimension d)	{ super(p, d); }
      	public Rect(int width, int height)	{ super(width, height); }
       	public Rect(int x, int y, int width, int height)	{ super(x, y, width, height); }

       	public Rect intersection(Rect r)	{ return new Rect(super.intersection(r)); }
       	public Rect union(Rect r)			{ return new Rect(super.union(r)); }
	
       	public int xc() 	{ return (int) getCenterX(); }
       	public int yc() 	{ return (int) getCenterY(); }
       	public int xe() 	{ return x + width; }
       	public int ye() 	{ return y + height; }

       	public Rectangle rectangle()	{ return new Rectangle(x, y, width, height); }
    }
}
