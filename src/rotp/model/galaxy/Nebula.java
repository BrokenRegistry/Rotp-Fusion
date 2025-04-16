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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

import rotp.model.game.IGameOptions;
import rotp.model.planet.Planet;
import rotp.ui.main.GalaxyMapPanel;
import rotp.ui.sprites.MapSprite;
import rotp.ui.util.planets.PlanetImager;
import rotp.util.FastImage;
import rotp.util.Rand;

public class Nebula extends MapSprite implements IMappedObject, Serializable {
    private static final long serialVersionUID = 1L;
    private static final Color labelColor = new Color(255,255,255,64);
    private static final int shapeQuality = 10;
    private static final List<String> nebulaeFiles = new ArrayList<>();
    private static List<String> randomFiles	 = new ArrayList<>();
    static {
    	nebulaeFiles.add("images/nebulae/the_lagoon_nebula.png");
    	nebulaeFiles.add("images/nebulae/crab-nebula-mosaic.png");
    	nebulaeFiles.add("images/nebulae/ring-nebula.png");
    	nebulaeFiles.add("images/nebulae/cats-paw-b-16.png");
    	nebulaeFiles.add("images/nebulae/potw1444a-alt.png");
    	nebulaeFiles.add("images/nebulae/california_nebula_ngc_1499.png");
    	nebulaeFiles.add("images/nebulae/antenna galaxy.png");
    	nebulaeFiles.add("images/nebulae/cosmic_hourglass_l1527.png");
    	nebulaeFiles.add("images/nebulae/cartwheel.png");
    	nebulaeFiles.add("images/nebulae/wr124_nebula.png");
    }
    private	static int requestedQuality = 0;
    private static Rand randNeb;
    
    private Rectangle.Float shape;
    private Rectangle.Float innerShape;
    private int sysId = -1;
    private int numStars = 0;
    private float size, width, height;
    private float x, y;
    private String nebulaFile;
    // private float  rotation; // Later
    private transient Shape	realNebulaArea;
	private transient Shape baseRNShape;
    private transient BufferedImage image;
    private transient int currentQuality;

    static void reinit(long source)		{
    	randomFiles.clear();
    	randNeb = new Rand(source);
    }
    public static void requestedQuality(int val)	{ requestedQuality = val; }

    @Override public Rand rng()		{ return randNeb==null? super.rng(): randNeb;}
    private boolean	isRealNebula()	{ return currentQuality > 0;}
    private float width()			{ return width; }
    private float height()			{ return height; }
    //public Rectangle.Float shape()	{ return shape; }
    private String name() {
        if (sysId < 1)
            return "";
        
        String sysName = player().sv.name(sysId);
        if (sysName.isEmpty())
            return text("NEBULA_ID", sysId);
        else
            return text("NEBULA_NAME", sysName);
    }

    Nebula copy() {
        Nebula neb = new Nebula();
        neb.size   = size;
        neb.width  = width;
        neb.height = height;
        neb.image  = image;
        return neb;
    }
    //public boolean intersects(Nebula n)	{ return shape.intersects(n.shape); }
    @Override
    public float x()               { return x; }
    @Override
    public float y()               { return y; }
    
    float adjWidth()               { return size == 0 ? width : size*width; }
    float adjHeight()              { return size == 0 ? height : size*height; }
    
    float centerX()                { return x+(adjWidth()/2); }
    float centerY()                { return y+(adjHeight()/2); }
    
    boolean noStars()              { return numStars == 0; }
    
    void setXY(float x1, float y1) {
        x = x1;
        y = y1;
        shape = new Rectangle.Float(x, y, adjWidth(), adjHeight());
        innerShape = new Rectangle.Float(x+1, y+1, adjWidth()-2, adjHeight()-2);
        if (baseRNShape != null) {
        	AffineTransform at = new AffineTransform();
	        at.translate(x, y);
	        realNebulaArea = at.createTransformedShape(baseRNShape);
        }
    }
    public Nebula() { }
    Nebula(float sizeMult, boolean buildImage) {
    	requestedQuality = guiOptions().selectedRealNebulaSize();
    	currentQuality	 = requestedQuality;
        size = max(1, sizeMult);
    	if (isRealNebula()) {
    		newRealNebula(buildImage);
    		return;
    	}
        width = random(8f,14f);
        height = random(8f,14f);
        if (buildImage) // BR: to allow a preview later...
        	image  = buildImage();
    }
    Nebula cancel()	{
    	if (isRealNebula())
    		randomFiles.add(nebulaFile);
    	return null;
    }
    private void newRealNebula(boolean buildImage) {
    	int wF = 12; // 19
    	int hF = 19; // 12
    	float area	= random(8f,14f) * wF * random(8f,14f) * hF;
    	image		= nextNebula();
        int imgW	= image.getWidth();
        int imgH	= image.getHeight();
        float scale	= sqrt(area / (imgW * imgH));
        width		= (int) Math.round(wF);
        height		= (int) Math.round(scale * imgH / hF);
        if (buildImage) // BR: to allow a preview later...
        	buildNebulaImage(image);
    }
    private void buildNebulaImage(BufferedImage img) {
        int w = (int) width()  * 19 * currentQuality;
        int h = (int) height() * 12 * currentQuality;
		image = newBufferedImage(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
		baseRNShape = FastImage.from((Image)image).getImageOutline(shapeQuality, adjWidth(), adjHeight(), currentQuality);
    	AffineTransform at = new AffineTransform();
        at.translate(x, y);
        realNebulaArea = at.createTransformedShape(baseRNShape);
    }
    void noteStarSystem(StarSystem sys) {
        numStars++;
        if (sysId < 0) {
            sysId = sys.id;
            return;
        }
        float centerX = centerX();
        float centerY = centerY();
        StarSystem currSys = galaxy().system(sysId);
        float currDist = distance(currSys.x(), currSys.y(), centerX, centerY);
        float newDist = distance(sys.x(), sys.y(), centerX, centerY);
        
        if (newDist < currDist)
            sysId = sys.id;
    }
    void enrichCentralSystem() {
    	IGameOptions opts = guiOptions();
        if (numStars < opts.nebulaEnrichmentInsideStar())
            return;
        if (galaxy().numStarSystems() <= opts.nebulaEnrichmentGalaxySize())
            return;
        
        Planet planet = galaxy().system(sysId).planet();
        if (planet.isEnvironmentNone())
            return;
        if (planet.isArtifact()) // BR: Not both!
        	return;
        float ultraPct = numStars * .07f;
        if (random() < ultraPct)
        	planet.setResourceUltraRich();
        else if (!planet.isResourceUltraRich())
        	planet.setResourceRich();
    }
    boolean isToClose(Nebula neb) {
    	Rectangle2D intersect;
    	if (isRealNebula()) {
    		Area copy = new Area(realNebulaArea);
    		copy.intersect(new Area(neb.realNebulaArea));
    		intersect = copy.getBounds2D();
    	}
    	else
     		intersect = shape.createIntersection(innerShape); 

    	float limit = 0 * size;
 		double area = intersect.getWidth() * intersect.getHeight();
 		return area > limit;
    }
    boolean intersects(Nebula neb) {
    	if (isRealNebula()) {
    		Area copy = new Area(realNebulaArea);
    		copy.intersect(new Area(neb.realNebulaArea));
    		return !copy.isEmpty();
    	}
    	else
    		return shape.intersects(neb.shape); 
    }
    boolean intersects(Line2D path) {
    	if (isRealNebula()) {
    		if (realNebulaArea.contains(path.getP1()) || realNebulaArea.contains(path.getP2()))
    			return true;
    		PathIterator polyIt = realNebulaArea.getPathIterator(null);
    		double[] coords = new double[6];
    		double[] firstCoords = new double[2];
    		double[] lastCoords = new double[2];
    		polyIt.currentSegment(firstCoords);
    		lastCoords[0] = firstCoords[0];
    		lastCoords[1] = firstCoords[1];
    		polyIt.next();
    		while(!polyIt.isDone()) {
    			int type = polyIt.currentSegment(coords);
    			Line2D.Double currentLine;
    			switch(type) {
    			case PathIterator.SEG_LINETO :
    				currentLine = new Line2D.Double(lastCoords[0], lastCoords[1], coords[0], coords[1]);
    				if(currentLine.intersectsLine(path))
    					return true;
    				lastCoords[0] = coords[0];
    				lastCoords[1] = coords[1];
    				break;
    			case PathIterator.SEG_CLOSE : // Close the current polygon
    				currentLine = new Line2D.Double(coords[0], coords[1], firstCoords[0], firstCoords[1]);
    				if(currentLine.intersectsLine(path))
    					return true;
    				break;
    			case PathIterator.SEG_MOVETO : // Go to the next polygon
    				firstCoords[0] = coords[0];
    				lastCoords[0]  = coords[0];
    				firstCoords[1] = coords[1];
    				lastCoords[1]  = coords[1];
    			}
    			polyIt.next();
    		}
    		return false;
    	}
    	else
    		return (innerShape != null) && path.intersects(innerShape);
    }
    boolean contains(float x, float y) {
    	if (isRealNebula())
    		return realNebulaArea.contains(x,y);
    	else
    		return (innerShape != null) && innerShape.contains(x, y);
    }
    private BufferedImage image() {
    	//shapeQuality = 9;
        if (image == null || requestedQuality != currentQuality)
        	image = buildImage();
        return image;
    }
    private BufferedImage buildImage() {
    	requestedQuality = guiOptions().selectedRealNebulaSize();
    	currentQuality	 = requestedQuality;
    	if (requestedQuality > 0)
    		return buildNebulaImage();
    	
        int w = (int) width()*19;
        int h = (int) height()*12;

        int nebR = roll(160,225);
        int nebG = 0;
        int nebB = roll(160,255);

        //int centerX = w/2;
        //int centerY = h/2;
        FastImage fImg = PlanetImager.current().getTerrainSubImage(w,h);

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
            int distFromEdgeX = min(x, w-x);
            int distFromEdgeY = min(y, h-y);
            int distFromEdge = min(distFromEdgeX, distFromEdgeY);
            float pctFromEdge = min((float)distFromEdgeX/w, (float)distFromEdgeY/h);
            //int distFromCenter = (int) Math.min(128,Math.sqrt(((x-centerX)*(x-centerX))+((y-centerY)*(y-centerY))));
            int alpha = min(distFromEdge/2, landLevel*3/5);
            alpha = (int) (pctFromEdge * landLevel);
            alpha = min(alpha*3/2, (alpha+255)/2);
            //alpha = Math.min(145-distFromCenter, landLevel/2);
            int newPixel = (alpha << 24) | (nebR << 16) | (nebG << 8) | nebB;
            fImg.setRGB(x, y, newPixel);
        }
        return fImg.image();
    }
    @Override
    public boolean isSelectableAt(GalaxyMapPanel map, int mapX, int mapY) { return false; }

    @Override
    public void draw(GalaxyMapPanel map, Graphics2D g2) {
        Rectangle mShape = mapShape(map);
        
        if (isRealNebula()) {
        	float opacity = guiOptions().realNebulaeOpacity();
            Composite prevComp = g2.getComposite();
            Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER , opacity);
            g2.setComposite(comp );
            RenderingHints prevRender = g2.getRenderingHints();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(image(), mShape.x, mShape.y, mShape.x+mShape.width, mShape.y+mShape.height,
            						0, 0, image().getWidth(), image().getHeight(), map);
            g2.setRenderingHints(prevRender);
            g2.setComposite(prevComp);
            //image();
            boolean showLimits = false; // TO DO BR: set to false
            if (showLimits && baseRNShape != null) {
    			double scaleX = (double) mShape.width / baseRNShape.getBounds2D().getWidth();
    			double scaleY = (double) mShape.height / baseRNShape.getBounds2D().getHeight();
    			AffineTransform at = new AffineTransform();
    			at.scale(scaleX, scaleY);
    			Shape scaled = at.createTransformedShape(baseRNShape);
    			at = new AffineTransform();
    		    at.translate(mShape.x, mShape.y);
    		    g2.setColor(Color.red);
    		    Shape toDraw = at.createTransformedShape(scaled);
    		    g2.draw(toDraw);
    		}
        }
        else {
            g2.drawImage(image(), mShape.x, mShape.y, mShape.x+mShape.width, mShape.y+mShape.height,
					0, 0, image().getWidth(), image().getHeight(), map);
        }
        if (map.hideSystemNames())
            return;
        
        // use smaller font when we have the full name
        float scale = map.scaleX();
        int fontSize = sysId <= 0 ? (int) (size*1800/scale) : (int) (size*1200/scale);
        if (fontSize >= 14) {
            String name = name();
            if (!name.isEmpty()) {
                g2.setFont(narrowFont(fontSize));
                g2.setColor(labelColor);
                int sw = g2.getFontMetrics().stringWidth(name);
                int x0 = mShape.x+((mShape.width-sw)/2);
                int y0 = mShape.y+((mShape.height-fontSize)/2);
                drawString(g2, name, x0, y0);
            }
        }
    }
    private Rectangle mapShape(GalaxyMapPanel map) {
        int x0 = map.mapX(x);
        int y0 = map.mapY(y);
        int x1 = map.mapX(x+(int)adjWidth());
        int y1 = map.mapY(y+(int)adjHeight());
        return new Rectangle(x0,y0, x1-x0, y1-y0);
    }
    private BufferedImage buildNebulaImage() {
    	if (nebulaFile == null)
    		image = nextNebula();
    	else
    		image = newBufferedImage(icon(nebulaFile).getImage());
    	
        int w = (int) width()  *19 *currentQuality;
        int h = (int) height() *12 *currentQuality;
        
		image = newBufferedImage(image.getScaledInstance(w, h, Image.SCALE_SMOOTH));
		baseRNShape = FastImage.from((Image)image).getImageOutline(shapeQuality, adjWidth(), adjHeight(), currentQuality);
    	AffineTransform at=new AffineTransform();
        at.translate(x, y);
        realNebulaArea = at.createTransformedShape(baseRNShape);
        return image;
    }
	private String nextNebulaFile() {
		if (randomFiles.isEmpty()) {
			randomFiles.addAll(nebulaeFiles);
	        shuffle(randomFiles);
		}
		return randomFiles.remove(0);
	}
	private BufferedImage nextNebula() {	
		nebulaFile	  = nextNebulaFile();
		ImageIcon img = icon(nebulaFile);
		return newBufferedImage(img.getImage());
	}

	public void drawNebulaPreview(Graphics2D g2, int xP, int yP, float factor)	{
		int x0 = (int) (xP + x * factor);
		int y0 = (int) (yP + y * factor);
        int x1 = (int) (x0 + width * factor);
        int y1 = (int) (y0 + height * factor);
        //System.out.println("Nebula x=" + fmt(x, 1) + " y=" + fmt(y, 1));
        if (isRealNebula()) {
        	float opacity = guiOptions().realNebulaeOpacity();
            Composite prevComp = g2.getComposite();
            Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER , opacity);
            g2.setComposite(comp );
            RenderingHints prevRender = g2.getRenderingHints();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
    		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        	g2.drawImage(image(), x0, y0, x1, y1, 0, 0, image().getWidth(), image().getHeight(), null);
            g2.setRenderingHints(prevRender);
            g2.setComposite(prevComp);
        }
        else
        	g2.drawImage(image(), x0, y0, x1, y1, 0, 0, image().getWidth(), image().getHeight(), null);
	}
}
