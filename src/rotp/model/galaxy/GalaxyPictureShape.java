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
package rotp.model.galaxy;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import rotp.model.game.IGalaxyOptions.IShapeOption;
import rotp.model.game.IGalaxyOptions.ShapeOptionInteger;
import rotp.model.game.IGameOptions;

final class GalaxyPictureShape extends GalaxyShape {
	private static final long serialVersionUID = 1L;
	private	static final String SHORT_NAME	= "PICTURES";
	private	static final String BASE_NAME	= ROOT_NAME + SHORT_NAME;
			static final String NAME		= UI_KEY + BASE_NAME;
	private	static final int DEFAULT_OPT_1	= 50;
	private	static final int DEFAULT_OPT_2	= 100;
	private	static final String DEFAULT_OPT_3 = "";

	private	static final String SETUP_PICTURE_GREY_SUM		= "SETUP__PICTURE_GREY_SUM";
	private	static final String SETUP__PICTURE_GREY_MAX		= "SETUP__PICTURE_GREY_MAX";
	private	static final String SETUP__PICTURE_GREY_INVERSE	= "SETUP__PICTURE_GREY_INVERSE";
	private	static final String SETUP__PICTURE_NORMAL		= "SETUP__PICTURE_NORMAL";
	private	static final String SETUP__PICTURE_SHARP1		= "SETUP__PICTURE_SHARP1";
	private	static final String SETUP__PICTURE_SHARP2		= "SETUP__PICTURE_SHARP2";
	private	static final String SETUP__PICTURE_SHARP3		= "SETUP__PICTURE_SHARP3";
	private	static final String SETUP__PICTURE_SHARP4		= "SETUP__PICTURE_SHARP4";
	private static ShapeOptionInteger param1;
	private static ShapeOptionInteger param2;
	private static ShapeOptionFile param3;

	private static ShapeOptionInteger param1()	{
		if (param1 == null)
			param1 = new ShapeOptionInteger(BASE_NAME, 1, DEFAULT_OPT_1);
		return param1;
	}
	private static ShapeOptionInteger param2()	{
		if (param2 == null)
			param2 = new ShapeOptionInteger(BASE_NAME, 2, DEFAULT_OPT_2);
		return param2;
	}
	private static ShapeOptionFile param3()	{
		if (param3 == null)
			param3 = new ShapeOptionFile( BASE_NAME, 3, DEFAULT_OPT_3);
		return param3;
	}

	// Gaussian alternative
	private static final int GX = 127;
	private static final int GY = 127;
	private static final float GS = 127f/6f;

	private static final float STAR_CLAMPING = 0.1f;
	private static final float HOME_CLAMPING = 0.3f;

	private float adjustDensity;
	private float aspectRatio;
	private float densityFactor;
	private float[][] starsPD; // Star Map
	private float[][] sharpPD; // for grey maps
	private float[][] starsCD;
	private float[][] userCD;
	private float[][] orionCD;
	private float[][] nebulaeCD;
	private float[][][] alienCD;

	private int xBM, yBM;
	private int alienSize;
	private int sharpNb;
	private float offset, xMult, yMult, volume;
	private boolean isSum, isInverted;
	private boolean isSharp;




//	private	final float[] clips	= new float[] {0.3f, 0.1f, 0f} ;

	private PixelsArray pixelsArray;

	private static BufferedImage loadImage(String path)	{
		try { return ImageIO.read(new File(path)); }
		catch (IOException e) { return null; }
	}

	GalaxyPictureShape(IGameOptions options, boolean[] rndOpt)	{ super(options, rndOpt); }

//	private int numCol()	{ return pixelsArray.numCols; }
//	private int numLine()	{ return pixelsArray.numRows; }
	private float sqr(float x) { return x*x;}

	@Override protected void initFinalOption1()		{
		finalOption1 = param1().getToString();
		option1 = param1().get();
	}
	@Override protected void initFinalOption2()		{
		finalOption1 = param2().getToString();
		option1 = param2().get();
	}
	@Override public IShapeOption<?> paramOption1()	{ return param1(); }
	@Override public IShapeOption<?> paramOption2()	{ return param2(); }
	@Override public IShapeOption<?> paramOption3()	{ return param3(); }
	@Override public String getOption3()			{ return param3().get(); }
	@Override public String name()					{ return NAME; }
	@Override public GalaxyShape get()				{ return this; }

	@Override public float maxScaleAdj()			{ return 1.1f; }

	@Override public double starDist(float x0, float y0, float x1, float y1)	{	// TODO BR: starDist
		return super.starDist(x0, y0, x1, y1);
	}

	@Override protected void singleInit(boolean full)	{	// TODO BR: VALIDATE
		super.singleInit(full);
		// System.out.println("========== GalaxyPicturesShape.singleInit()");
		alienSize = 1;

		// Get bitmap (Normalized to One)
		String option3 = param3.get();
		pixelsArray = new PixelsArray(option3);
		if (!pixelsArray.singleInit()) { // Empty ==> default shape
			pixelsArray = new PixelsArray((BufferedImage) null);
			pixelsArray.singleInit();
		}
	}
	private class PixelStat {
		protected static final int HOME_ID	= 0;
		protected static final int STAR_ID	= 1;
		protected static final int MAP_ID	= 2;
		protected static final int[] ZONE_IDS	= new int[] {HOME_ID, STAR_ID, MAP_ID} ;
		protected static final float[] BASE_LEVELS	= new float[] {0.3f, 0.1f, 0f} ;

		protected float[] cumulativeSums = new float[ZONE_IDS.length];
		protected float[] levels = new float[ZONE_IDS.length];
		protected float levelMax, grayMax;

		protected void setStat(PixelStat src)	{
			for (int i : ZONE_IDS)
				cumulativeSums[i] = src.cumulativeSums[i];
		}
		protected void sumStat(PixelStat prevStat, float gray)	{
			for (int i : ZONE_IDS)
				if (gray > levels[i])
					cumulativeSums[i] = prevStat.cumulativeSums[i] + gray;
		}
	}
	private class Pixel	extends PixelStat {
		protected float red, green, blue, alpha, gray;

		protected Pixel(float gris)	{
			red = green = blue = gray = levelMax = grayMax = gris;
			alpha = 1;
		}
		protected Pixel(int color)	{
			alpha	= ((color >> 24) & 0xff) / 255f;
			red		= ((color >> 16) & 0xff) * alpha / 255f;
			green	= ((color >> 8)  & 0xff) * alpha / 255f;
			blue	= (color & 0xff) * alpha / 255f;
			grayMax	= gray = 0.299f * red + 0.587f * green + 0.114f * blue;
			levelMax= green > red ? green : red;
			if (blue > levelMax)
				levelMax = blue;
		}
//		protected Pixel(Pixel pix)	{
//			alpha	= pix.alpha;
//			red		= pix.red;
//			green	= pix.green;
//			blue	= pix.blue;
//			gray	= pix.gray;
//			grayMax	= pix.grayMax;
//			levelMax	= pix.levelMax;
//			starProbSum	= pix.starProbSum;
//			homeProbSum	= pix.homeProbSum;
//		}

		protected PixelStat setProbability(PixelStat prevStat)	{
			sumStat(prevStat, gray);
			return this;
		}
	}
	private final class PixelsRow extends PixelStat	{
		private final Pixel[] pixelsRow;
		PixelsRow(int size)	{ pixelsRow = new Pixel[size]; }

		private void setPixel(int col, float grey)	{ pixelsRow[col] = new Pixel(grey); }
		private void setPixel(int col, int rgb)		{ pixelsRow[col] = new Pixel(rgb); }

		protected PixelStat setProbability(PixelStat prevStat)	{
			for (Pixel p : pixelsRow)
				prevStat = p.setProbability(prevStat);
			return this;
		}
	}
	private final class PixelsArray extends PixelStat	{
		private int numCols, numRows, numPix, lastCol, lastRow;
		private PixelsRow[] pixelsArray;
		private float pixelsPerStar, volumeFactor;
		private float x2c, y2l;
		private float arrayMax;
		private QuadPixels quad = new QuadPixels();

		private PixelsArray(String path)		{ this(loadImage(path)); }
		private PixelsArray(BufferedImage img)	{
			if (img != null && setNumPixels(img.getWidth(), img.getHeight()))
				extract(img);
			else
				genGaussian();
		}

		private boolean singleInit()	{
			setProbability();
			volume = cumulativeSums[MAP_ID];
			if (volume == 0)
				return false;
			
			aspectRatio = (float) numCols / numRows;
			volumeFactor = numPix * grayMax / volume;
			densityFactor = (float) (volumeFactor/3.0); // TODO BR: Fine tune
			adjustDensity = sqrt(densityFactor);
			return true;
		}
		private void init()	{
			x2c = (lastCol) / fullWidth;
			y2l = (lastRow) / fullHeight;
		}
		//private QuadPixels getQuad(float x, float y)	{ return new QuadPixels(loc(x, y)); }
		private Point.Float loc(float x, float y)	{ return new Point.Float(x * x2c, y * y2l); }
		private boolean setNumPixels(int c, int r)	{
			numCols	= c;
			numRows	= r;
			numPix	= numCols * numPix;
			lastCol = numCols-1;
			lastRow = numRows-1;
			pixelsPerStar = numPix / maxStars;
			if (numPix > 0) {
				pixelsArray = new PixelsRow[numRows];
				for (int i=0; i<numRows; i++)
					pixelsArray[i] = new PixelsRow(numCols);
				return true;
			}
			return false;
		}
		private float getGray(QuadPixels quad)	{ return quad.getGray(); }
		private void extract(BufferedImage img)	{
			arrayMax = 0;
			for (int y=0; y<numRows; y++) {
				PixelsRow row = pixelsArray[y];
				for (int x=0; x<numCols; x++)
					row.setPixel(x, img.getRGB(x, y));
			}
		}
		private void genGaussian()	{
			arrayMax = 0;
			setNumPixels(GX, GY);

			float cx = lastCol / 2f;
			float cy = lastRow / 2f;
			float s2 = -2 * GS * GS;
			for (int y=0; y<numRows; y++) {
				PixelsRow row = pixelsArray[y];
				float dy2 = sqr(y-cy);
				for (int x=0; x<numCols; x++)
					row.setPixel(x, (float) Math.exp((dy2 + sqr(x-cx)) / s2));
			}
		}
		private void setProbability()	{
			PixelStat stat = new PixelStat();
			for (int i : ZONE_IDS)
				levels[i] = BASE_LEVELS[i] * arrayMax;
			for (PixelsRow row : pixelsArray)
				stat = row.setProbability(stat);
			setStat(stat);
		}
		private boolean validStar(float x, float y)	{ return valid(x, y, STAR_CLAMPING); }
		private boolean validHome(float x, float y)	{ return valid(x, y, HOME_CLAMPING); }
		private boolean valid(float x, float y, float clamp) {
			if (x<0 || x>fullWidth || y<0 || y>fullHeight)
				return false;
			return quad.setLoc(x, y).getGray() >= clamp;
		}

		private void setRandom(Point.Float pt, int zoneId)	{	// TODO BR: VALIDATE
			float source = rand.nextFloat() * cumulativeSums[zoneId];
			int colId, rowId;
			for (rowId=0; rowId<numRows; rowId++)
				if(source <= pixelsArray[rowId].cumulativeSums[zoneId])
					break;
			if (rowId == numRows)
				rowId--;
			Pixel[] row = pixelsArray[rowId].pixelsRow;

			for (colId=0; colId<numCols; colId++)
				if(source <= row[colId].cumulativeSums[zoneId])
					break;

			if (colId == numCols)
				colId--;
			float x = colId + randX.nextFloat();
			float y = rowId + randX.nextFloat();
			pt.x = offset + x * xMult;
			pt.y = offset + y * yMult;
		}
		private float distance(Point.Float p0, Point.Float p1)	{ // TODO BR:
			float dx = p1.x - p0.x;
			float dy = p1.y - p0.y;
			double dp = Math.sqrt(dx*dx + dy*dy);
			int numStep = 1 + (int) dp;
			float sx = dx / numStep;
			float sy = dy / numStep;

			float x = p0.x;
			float y = p0.y;
			float dist = 0;
			for (int i=0; i<numStep; i++) {
				dist += quad.setLoc(x, y).radius();
				x += sx;
				y += sy;
			}

			return dist;
		}

		private final class QuadPixels {
			Pixel pUL, pUR;
			Pixel pDL, pDR;
			float wL, wR, wU, wD;

			private QuadPixels()	{}
			//private QuadPixels(Point.Float pt)	{ setLoc(pt); }

			private QuadPixels setLoc(float x, float y)	{
				Point.Float pt = loc(x, y);
				int c = (int) pt.x;
				int r = (int) pt.y;

				pUL	= pixelsArray[r].pixelsRow[c];
				pUR	= pixelsArray[r].pixelsRow[c+1];
				pDL	= pixelsArray[r+1].pixelsRow[c];
				pDR	= pixelsArray[r+1].pixelsRow[c+1];

				wR	= pt.x - c;
				wL	= 1 - wR;
				wD	= pt.y - r;
				wU	= 1 - wD;
				return this;
			}
			private Color getColor()	{
				float r = interp(pUL.red, pUR.red, pDL.red, pDR.red);
				float g = interp(pUL.green, pUR.green, pDL.green, pDR.green);
				float b = interp(pUL.blue, pUR.blue, pDL.blue, pDR.blue);
				float a = interp(pUL.alpha, pUR.alpha, pDL.alpha, pDR.alpha);
				return new Color(r, g, b, a);
			}

			private float getGray()	{ return interp(pUL.gray, pUR.gray, pDL.gray, pDR.gray); }
			private float interp(float uL, float uR, float dL, float dR)	{ return wU * (wL*uL + wR*uR) + wD * (wL*dL + wR*dR); }
			
			private float radius()	{ // TODO BR:
				float gray = getGray();
				if (gray < 0.1f)
					return 10f;
				return 1/gray;
			}
		}
	}






	private int getGrey(int red, int green, int blue)	{ return round(0.299 * red + 0.587 * green + 0.114 * blue); }	// TODO BR: VALIDATE
	private void setRandom(float[][] cD, Point.Float pt)	{	// TODO BR: VALIDATE
		float source = rand.nextFloat();
		int col = xBM-1;
		int row;
		for (row=0; row<yBM; row++) {
			if(source < cD[row][col])
				break;
		}
		if (row == yBM) // source = 1.0
			row--;
		for (col=0; col<xBM; col++) {
			if(source < cD[row][col])
				break;
		} 
		if (col == xBM) // source = 1.0
			col--;
		float x = col + randX.nextFloat();
		float y = row + randY.nextFloat();
		
		pt.x = offset + x * xMult;
		pt.y = offset + y * yMult;
	}

	private float[][] alienCD(int id)	{	// TODO BR: VALIDATE
		int idx = Math.floorMod(id, alienSize);
		// System.out.println("alienCD(int id): alienSize= " + alienSize + "  id= " + id + " idx= " + idx);
		return alienCD[idx];
	}
    @Override protected float   minEmpireFactor()		{ return 4f; }	// TODO BR: VALIDATE
    @Override protected boolean allowExtendedPreview()	{ return true; }	// TODO BR: VALIDATE
	@Override public void clean()	{	// TODO BR: VALIDATE
		starsPD  = null;
		starsCD  = nebulaeCD;
	}
	@Override public void init(int n)	{	// TODO BR: VALIDATE
		super.init(n);
		// reset w/h vars since aspect ratio may have changed
		initWidthHeight();
		offset = galaxyEdgeBuffer();
		xMult  = (float) width/xBM;
		yMult  = (float) height/yBM;
	}
	@Override protected int galaxyWidthLY()	{	// TODO BR: VALIDATE
		return (int) (sqrt(finalNumberStarSystems*adjustDensity*adjustedSizeFactor()*aspectRatio));
	}
	@Override protected int galaxyHeightLY()	{	// TODO BR: VALIDATE
		return (int) (sqrt(finalNumberStarSystems*adjustDensity*adjustedSizeFactor()/aspectRatio));
	}
	@Override public void setRandom(Point.Float pt)		{ setRandom(starsCD, pt); }	// TODO BR: VALIDATE
	// modnar: add possibility for specific placement of homeworld/orion locations
	@Override public void setSpecific(Point.Float pt)	{	// TODO BR: VALIDATE
		if (indexWorld == 0) { // orion
			setRandom(orionCD, pt);
			return;
		}
		if (empSystems.size() == 0) { // Player homeworld
			setRandom(userCD, pt);
			return;
		}
		// Aliens homeworlds
		setRandom(alienCD(empSystems.size()-1), pt);
	}
	@Override public boolean valid(float x, float y)	{ return pixelsArray.validStar(x, y); }
	@Override protected float sizeFactor(String size)	{ return settingsFactor(0.8f); }	// TODO BR: VALIDATE
}
