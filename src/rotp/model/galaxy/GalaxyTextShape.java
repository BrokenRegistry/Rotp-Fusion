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
package rotp.model.galaxy;

import static rotp.ui.UserPreferences.GALAXY_TEXT_FILE;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.Rotp;
import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionInteger;
import rotp.model.game.IGalaxyOptions.ShapeOptionList;
import rotp.model.game.IGalaxyOptions.ShapeOptionListMult;
import rotp.model.game.IGameOptions;

// modnar: custom map shape, Text
final class GalaxyTextShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "TEXT";
	private	static final String	BASE_NAME	= ROOT_NAME + SHORT_NAME;
			static final String	NAME		= UI_KEY + BASE_NAME;
	private	static final String	SETUP_MULTI_LINE_COPY	= "SETUP_MULTI_LINE_COPY";
	private	static final String	SETUP_MULTI_LINE_LEFT	= "SETUP_MULTI_LINE_LEFT";
	private	static final String	SETUP_MULTI_LINE_CENTER	= "SETUP_MULTI_LINE_CENTER";
	private	static final String	SETUP_MULTI_LINE_RIGHT	= "SETUP_MULTI_LINE_RIGHT";
	private	static final int	DEFAULT_OPT_1	= 0;
	private	static final String	DEFAULT_OPT_2	= SETUP_MULTI_LINE_COPY;
	private	static final int	DEFAULT_OPT_3	= 2;
	private	static final int	DEFAULT_OPT_4	= 5;
	private static ShapeOptionListMult	param1;
	private static ShapeOptionList		param2;
	private static ShapeOptionInteger	param3;
	private static ShapeOptionInteger	param4;

	private static ShapeOptionListMult param1()	{
		if (param1 == null) {
			param1 = new ShapeOptionListMult(
			BASE_NAME, 1,
			getShapeOption1List(),
			DEFAULT_OPT_1);
			param1.showFullGuide(false);
		}
		return param1;
	}
	private static ShapeOptionList	  param2()	{
		if (param2 == null) {
			param2 = new ShapeOptionList(
			BASE_NAME, 2,
			new ArrayList<String>(Arrays.asList(
				SETUP_MULTI_LINE_COPY,
				SETUP_MULTI_LINE_LEFT,
				SETUP_MULTI_LINE_CENTER,
				SETUP_MULTI_LINE_RIGHT,
				RANDOM_OPTION
				) ),
			DEFAULT_OPT_2);
		}
		return param2;
	}
	private static ShapeOptionInteger  param3()	{
		if (param3 == null) {
			param3 = new ShapeOptionInteger(BASE_NAME, 3, DEFAULT_OPT_3);
			param3.setLimits(1, 10);
			param3.setIncrements(1, 2, 5);
			param3.loop(true);
			param3.isDuplicate(false);
		}
		return param3;
	}
	private static ShapeOptionInteger  param4()	{
		if (param4 == null) {
			param4 = new ShapeOptionInteger(BASE_NAME, 4, DEFAULT_OPT_4);
			param4.setLimits(-100, 100);
			param4.setIncrements(1, 5, 20);
			param4.loop(true);
			param4.isDuplicate(false);
		}
		return param4;
	}

	private static List<String> reinitShapeOption1() {
		List<String> list = getShapeOption1List();
		param1().reInit(list);
		return list;
	}
	private static List<String> getShapeOption1List() {
		List<String> list = new ArrayList<>();
		// list.add(opts.selectedHomeWorldName());
		String path = Rotp.jarPath();
		String galaxyfile = GALAXY_TEXT_FILE;
		File file = new File(path, galaxyfile);
		if (!file.exists())
			initGalaxyTextFile(file);

		try ( BufferedReader in = new BufferedReader( new InputStreamReader( new FileInputStream(file), "UTF-8"));) {
			String input;
			while ((input = in.readLine()) != null) {
				String[] args = input.split("\t");
				String val = args[0].trim();
				if (!val.isEmpty())
					list.add(val);
			}				
		}
		catch (FileNotFoundException e) {
			System.err.println(path+galaxyfile+" not found.");
		}
		catch (IOException e) {
			System.err.println("GalaxyTextFile.load -- IOException: "+ e.toString());
		}
		return list;
	}

	private float aspectRatio, shapeFactor;
	private double textW, textH;
	private Area textShape;
	private Font font;
	private int  lineSpacing = 5;

	GalaxyTextShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

	@Override public void init(int n)		{
		super.init(n);
		lineSpacing = param4().getAsInt();

		BufferedImage img = new BufferedImage(16, 10, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = img.createGraphics();
		initTextArea (g2, finalStr(System.lineSeparator()));

		textW = textShape.getBounds().getWidth();
		textH = textShape.getBounds().getHeight();

		if (textW == 0 || textH == 0) {
			initTextArea (g2, "?");
			textW = textShape.getBounds().getWidth();
			textH = textShape.getBounds().getHeight();
		}

		// set galaxy aspect ratio to the textShape aspect ratio
		// this accommodates very long or short text strings
		// for multi-line texts, use adjust_line
		aspectRatio = (float) (textW / textH);
		shapeFactor = sqrt(max(aspectRatio, 1/aspectRatio));

		// reset w/h vars since aspect ratio may have changed
		initWidthHeight();

		// rescale textShape to fit galaxy map, then move into map center
		AffineTransform scaleText = new AffineTransform();
		AffineTransform moveText = new AffineTransform();

		// rescale
		double zoom;
		if (shapeFactor > 1.0) // Use Zoom X
			zoom = galaxyWidthLY() / textW;
		else // Use Zoom Y
			zoom = galaxyHeightLY() / textH;
		zoom = Math.max(0.1	, zoom); // min result in too munch attempt!
		scaleText.scale(zoom, zoom);
		textShape = new Area(scaleText.createTransformedShape(textShape));

		// recenter with multiple lines
		double oldX = textShape.getBounds().getX();
		double oldY = textShape.getBounds().getY();
		double moveX = (galaxyWidthLY()-textShape.getBounds().getWidth())/2 - oldX + galaxyEdgeBuffer();
		double moveY = (galaxyHeightLY()-textShape.getBounds().getHeight())/2 - oldY + galaxyEdgeBuffer();
		moveText.translate(moveX, moveY);
		textShape = new Area(moveText.createTransformedShape(textShape));
	}
	@Override public String getOption1()	{
		if (Rotp.noOptions())
			return "";
		return finalStr("⤾"); // ᓗ ↩ ᦱ ↵ ⤶ ⏎ ⤾  
	}
	@Override public String getOption3()	{
		if (Rotp.noOptions())
			return "";
		return text("SETUP_GALAXY_SHAPE_LINE_NUMBER", param3().get());
	}
	@Override public String getOption4()	{
		if (Rotp.noOptions())
			return "";
		return text("SETUP_GALAXY_SHAPE_LINE_SPACING", paramOption4().getAsInt());
	}
	@Override public String name()			{ return NAME; }
	@Override public GalaxyShape get()		{ return this; }
	@Override protected void initFinalOption1()		{
		finalOption1 = param1().get();
		if (RANDOM_OPTION.equals(finalOption1) || randomizeShapeOptions[0]) {
			List<String> optionList = new ArrayList<>(options1());
			optionList.remove(RANDOM_OPTION);
			finalOption1 = randRnd.random(optionList);
		}
		option1 = max(0, options1().indexOf(finalOption1));
	}
	@Override protected void initFinalOption2()		{
		finalOption2 = param2().get();
		if (RANDOM_OPTION.equals(finalOption2) || randomizeShapeOptions[1]) {
			List<String> optionList = new ArrayList<>(options2());
			optionList.remove(RANDOM_OPTION);
			finalOption2 = randRnd.random(optionList);
		}
		option2 = max(0, options2().indexOf(finalOption2));
	}
	@Override protected void initFinalOption3()		{ // not random yet
		finalOption3 = getOption3();
		option3 = param3().get();
	}
	@Override protected void initFinalOption4()		{ // not random yet
		finalOption4 = getOption4();
		option4 = param4().get();
	}
	@Override public float maxScaleAdj()			{ return 0.95f; }

	@Override public IShapeOption<?> paramOption1()	{ return param1(); }
	@Override public IShapeOption<?> paramOption2()	{ return param2(); }
	@Override public IShapeOption<?> paramOption3()	{ return param3(); }
	@Override public IShapeOption<?> paramOption4()	{ return param4(); }
	@Override public void setOption1(String value)	{ param1().set(value); }
	@Override public void setOption2(String value)	{ param2().set(value); }
	@Override public List<String> options1()		{ return param1().getOptions(); }
	@Override public List<String> options2()		{ return param2().getOptions(); }

	@Override protected float	minEmpireFactor()		{ return 4f; }
	@Override protected boolean	allowExtendedPreview()	{ return false; }
	@Override public void clean()						{ font = null; }
	@Override protected int galaxyWidthLY()				{ 
		return (int) sqrt(1.4f * aspectRatio * maxStars * adjustedSizeFactor());
	}
	@Override protected int galaxyHeightLY()			{
		return (int) sqrt(1.4f / aspectRatio * maxStars * adjustedSizeFactor());
	}
	@Override public void setSpecific(Point.Float pt)	{ setRandom(pt); }
	@Override public boolean valid(float x, float y)	{ return textShape.contains(x, y); }
	@Override protected float sizeFactor(String size)	{ return settingsFactor(1.0f); }

	private Font font()	{
		if (font == null) {
			font = galaxyFont(96);
		}
		return font;
	}
	private Area getArea(Graphics2D g2, String str)		{ return new Area(getShape(g2, str)); }
	private Shape getShape(Graphics2D g2, String str)	{
		Shape sh =  font().createGlyphVector(g2.getFontRenderContext(), str).getOutline();
		Rectangle bounds = sh.getBounds();
		int dx = -bounds.x;
		int dy = -bounds.y;
		if (finalOption2.equals(SETUP_MULTI_LINE_RIGHT))
			dx -= bounds.width;
		else if (finalOption2.equals(SETUP_MULTI_LINE_CENTER))
			dx -= bounds.width/2;
		AffineTransform moveText = new AffineTransform();
		moveText.translate(dx, dy);
		sh = moveText.createTransformedShape(sh);
		return sh;
	}
	private void initTextArea (Graphics2D g2, String s)	{
		if (!s.contains(System.lineSeparator())) {
			textShape = getArea(g2, s);
			return;
		}
		textShape = new Area();
		String[] txtArr	= s.split(System.lineSeparator());
		int moveY = 0;
		for (String txt : txtArr) {
			Shape sh = getShape(g2, txt);
			Rectangle bounds = sh.getBounds();
			int h = bounds.height;
			AffineTransform moveText = new AffineTransform();
			moveText.translate(0, moveY);
			sh = moveText.createTransformedShape(sh);
			textShape.add(new Area(sh));
			moveY += h + lineSpacing;
		}
	}
	private int currentGalaxyTextIndex(String s)		{
		String[] textList = getGalaxyTextList(false);
		for (int i=0; i<textList.length; i++) {
			if (s.equals((String) textList[i]))
				return i;
		}
		// Second chance by ignoring case
		for (int i=0; i<textList.length; i++) {
			if (s.equalsIgnoreCase((String) textList[i]))
				return i;
		}
		return -1;
	}
	private String[] getGalaxyTextList(boolean reload)	{
		List<String> list = GalaxyTextShape.reinitShapeOption1();
		return list.toArray(new String[list.size()]);
	}
	private String finalStr(String sep)					{
		String baseStr = finalOption1;
		String str	= baseStr;
		int num		= option3;

		switch (finalOption2) {
		case SETUP_MULTI_LINE_COPY:
			while (num > 1) {
				str += sep + baseStr;
				num--;
			}
			return str;

		default: // Multi lines
			String[] textList = getGalaxyTextList(false);
			int id = max(0, currentGalaxyTextIndex(baseStr));
			while (num > 1) {
				str += sep;
				id++;
				if (id >= textList.length)
					id = 0;
				str += textList[id];
				num--;
			}
			return str;
		}
	}

	private static void initGalaxyTextFile(File file)	{
		try (FileOutputStream fout = new FileOutputStream(file);
			// modnar: change to OutputStreamWriter, force UTF-8
			PrintWriter out = new PrintWriter(new OutputStreamWriter(fout, "UTF-8")); ) {
			out.println( "	List of customized Text Galaxies");
			out.println( "	Use a \"tab\" as separator to add comments");
			out.println();
			out.println( "ROTP	// Initial options");
			out.println( "ℳo○𝟏	// The precursor!");
			out.println();
			out.println("	A nice selection by U/dweller_below");
			out.println();
			out.println( "∞	Infinity feels good, but gameplay is the same as 8");
			out.println( "☸	The wheel of Dharma also feels appropriate");
			out.println( "༜	The Tibetan Sign Rdel Dkar Gsum gives 3 close rings. And it stacks well in multiple lines");
			out.println( "༶	The Tibetan Mark Caret gives 4 widely spaced star fields");
			out.println( "❖	This one gives 4 star fields with 8 to 13 light year spacing.");
			out.println( "ⵘ	Tifinagh Letter Ayer Yagh gives 5 star fields");
			out.println( "∴∵	You can stack or repeat these 2 characters for multiples of 3 or 6.");
			out.println( "፨	The Ethiopic Paragraph Separator is a nice 7 star fields.");
			out.println( "⁂");
			out.println( "🂓");
			out.println( "░");
			out.println( "▒");
			out.println( "⨌");
			out.println( "🦌");
			out.println( "⛄");
			out.println( "🎅");
			out.println( "🎄");
			out.println();
			out.println("	And more ...");
			out.println();
			out.println( "☃");
			out.println( "👽");
			out.println( "⌨");
			out.println( "⸎");
			out.println( "ꔘ");
			out.println( "꙰");
			out.println( "҈");
			out.println( "҉");
			out.println( "۞");
			out.println( "ꙮ");
			out.println( "𐩕");
			out.println( "֍");
			out.println( "֎");
			out.println( "☷");
			out.println( "❉");
			out.println( "⛆");
			out.println( "⣿");
			out.println( "𓃑");
			out.println( "𖡼");
			out.println( "𖥚");
			out.println( "፠");
			out.println( "⁂");
			out.println( "፤፤");
			out.println( "𐄳");
			out.println( "𐧾");
			out.println( "𐮜");
			out.println( "𑗗");
			out.println( "𝅂");
			out.println( "𞡜");
			out.println();
		}
		catch (IOException e) {
			System.err.println("GalaxyTextFile.save -- IOException: "+ e.toString());
		}
	}
}
