package rotp.model.combat;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_BEVEL;
import static java.awt.BasicStroke.JOIN_ROUND;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static rotp.model.combat.CombatShield.ABOVE;
import static rotp.model.combat.CombatShield.BELLOW;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import rotp.Rotp;
import rotp.model.game.IGameOptions;
import rotp.model.ships.ShipImage;
import rotp.model.ships.ShipLibrary;
import rotp.util.Base;

public class DemoShields extends JPanel implements Base, ActionListener {
	private static final long serialVersionUID = 1L;
	private int screenWidth	= 3840, screenHeight;
	private static final int COLUMNS_NUM = 10; 
	private static final int ROWS_NUM	 = 8;
	private static final int IMAGE_SEP	 = 10;
	private static final int MAX_ZOOM	 = 3;
	private static final int MAX_MODELS	 = 6-1;
	private static final int MAX_STYLES	 = 17-1;
	private static final int MAX_HULLS	 = 4-1;
	private static JFrame app;

	private final Color spaceBlue = new Color(32,32,64);

	private final String[] monsters	= new String[] {"ORION_GUARDIAN", "SPACE_CRYSTAL", "SPACE_PIRATES", "SPACE_AMOEBA"};
	private int folderId, hullId, modelId, monsterId, colorId;

	private BufferedImage shipImg;
	private int animationDelay;
	private Timer animationTimer;
	private int[] targetCtr = new int[] {0, 0, 0};
	private int[] sourceCtr = new int[] {1000, 500, 100};
	private int[] srcLocX = new int[] {-1,  0,  1, 1, 1, 0, -1, -1};
	private int[] srcLocY = new int[] {-1, -1, -1, 0, 1, 1,  1,  0};
	private int srcLocation = 0;
	private Color shieldColor = Color.green;
	private int heavyHoldFramesNum   = 0;
	private int shieldLevel = 1;
	private float beamForce = 10f;
	private float damage	= 5f;
	private int imageWidth, imageHeight;
	private int shipWidth, shipHeight;
	private int boxWidth, boxHeight;
	private int windowWidth,windowHeight;
	private Rectangle winRec; 
	private float shipScale;
	private int winTarX, winTarY;
	private int		wpnCount		= 50;
	private boolean isHeavy			= false;
	private boolean holdTimer		= false;
	private int		zoomFactor		= 1;
	private boolean resize			= false;
	private boolean srcRotate		= false;
	private boolean weaponRotate	= false;
	private boolean colorRotate		= false;
	private boolean fullRandom		= true;
	private boolean muted			= false;
	private boolean playerIsTarget	= false;
	
	private List<Color> listColors = new ArrayList<>();

	// Weapon parameters
	int weaponMaxId = 23;
	float cost, size, power;
	boolean restricted = false;
	int weaponId = 1;
	int damageLow = 0;
	int damageHigh = 0;
	int range = 1;
	boolean heavyAllowed = false;
	int heavyDamageLow = 0;
	int heavyDamageHigh = 0;
	int heavyRange = 2;
	int attacksPerRound = 1;
	int computer = 0;
	float enemyShieldMod = 1;
	boolean streaming = false;
	String soundEffect = "ShipLaser";
	int weaponSpread = 1; // 1, 2, 4, 7
	int windUpFrames = 3;
	int holdFrames = 0;
	int beamStroke, dashStroke;
	Color beamColor = Color.red;
	Color beamColor2;
	Color cycleColor;
	Color cycleColor2;
	Stroke weaponStroke;
	// \Weapon parameters
	private void initSizes() {
		screenWidth	 = scaled(Rotp.IMG_W);
		screenHeight = scaled(Rotp.IMG_H);
		// Box size
		boxWidth  = (screenWidth-scaled(20))/COLUMNS_NUM;
		boxHeight = (screenHeight-scaled(65))/ROWS_NUM;
		// ship size
		Image baseShipImg = loadImage(folderId, hullId, modelId);
		int baseShipWidth  = baseShipImg.getWidth(null);
		int baseShipHeight = baseShipImg.getHeight(null);
		shipScale  = Math.min((float)boxWidth/baseShipWidth, (float)boxHeight/baseShipHeight)*9/10;
		shipWidth  = (int) (baseShipWidth  * shipScale);
		shipHeight = (int) (baseShipHeight * shipScale);

		shipImg = new BufferedImage(shipWidth, shipHeight, TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) shipImg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY); 
		g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(baseShipImg, 0, 0, shipWidth, shipHeight, 0, 0, baseShipWidth, baseShipHeight, null);
		g.dispose();
		imageWidth  = boxWidth + scaled(IMAGE_SEP);
		imageHeight = boxHeight + scaled(IMAGE_SEP);

		windowWidth  = imageWidth * 23/10 + 10; // + IMAGE_SEP;
		windowHeight = imageHeight * 3  + 30; // + IMAGE_SEP;
		winRec	= new Rectangle(windowWidth, windowHeight);
		if (resize) {
			app.setSize(windowWidth, windowHeight);
			resize = false;
		}
		winTarX = IMAGE_SEP*0 + imageWidth;//  + boxWidth/2 ;
		winTarY = scaled(IMAGE_SEP)/2 + imageHeight + boxHeight/2;
	}
	private void init() {
		folderId  = 0;
		hullId	  = 0;
		modelId	  = 0;
		monsterId = 0;
		colorId	  = 1;
		if ((new File(Rotp.jarPath() + "/../src/rotp")).exists())
			zoomFactor	= 2;
		listColors.clear();
		listColors.add(new Color(237,28,36));   // red
		listColors.add(new Color(0,166,81));	// green
		listColors.add(new Color(247,229,60));  // yellow
		listColors.add(new Color(9,131,214));   // blue
		listColors.add(new Color(255,127,0));   // orange
		listColors.add(new Color(145,51,188));  // purple
		listColors.add(new Color(0,255,255));   // modnar: aqua
		listColors.add(new Color(255,0,255));   // modnar: fuchsia
		listColors.add(new Color(132,57,20));   // brown
		listColors.add(new Color(255,255,255)); // white
		listColors.add(new Color(0,255,0));		// modnar: lime
		listColors.add(new Color(220,160,220)); // modnar: plum*
		listColors.add(new Color(160,220,250)); // modnar: light blue*
		listColors.add(new Color(170,255,195)); // modnar: mint*
		listColors.add(new Color(128,128,0));   // modnar: olive**
		loadShields();
	}
	private boolean isMonster() { return folderId == MAX_STYLES; }
	private int[] newSourcePos(int dx, int dy, int dz) {
		return new int[] {
				targetCtr[0] + dx * boxWidth - boxWidth/6,
				targetCtr[1] - dy * boxHeight,
				targetCtr[2] + dz
			};
	}
	private void loadShields() {
		// long timeStart = System.currentTimeMillis();
		// long timeMid = timeStart;
		initWeapon();
		initSizes();
		targetCtr = new int[] {winTarX, winTarY, 0};
		sourceCtr = newSourcePos(1, 1, scaled(200));
//		System.out.println("loadShields() Time = " + (System.currentTimeMillis()-timeMid));
	}
	private void paintLines(SortedMap<Integer, ArrayList<Line2D.Double>> lines, Graphics2D g) {
		for(ArrayList<Line2D.Double> lineList : lines.values()) {
			for(Line2D.Double line : lineList) {
				g.draw(line);
			}
		}
	}
	private void paintLines(ArrayList<Line2D.Double> lines, Graphics2D g) {
		for(Line2D.Double line : lines) {
			g.draw(line);
		}
	}
	private ArrayList<Line2D.Double> addMultiLines(int size, int weapons, int sx, int sy, int tx, int ty) {
		// Direct copy; avoid changes
		int offSet = scaled((size + 1) * 5);
		ArrayList<Line2D.Double> lines = new ArrayList<>();
		if(weapons < 3)
			lines.add(new Line2D.Double(sx, sy, tx, ty));
		else if(weapons < 6) {
			lines.add(new Line2D.Double(sx, sy + offSet, tx, ty));
			lines.add(new Line2D.Double(sx, sy - offSet, tx, ty));
		} else {
			lines.add(new Line2D.Double(sx, sy, tx, ty));
			lines.add(new Line2D.Double(sx, sy + offSet, tx, ty));
			lines.add(new Line2D.Double(sx, sy - offSet, tx, ty));
		}
		return lines;
	}
	private void initWeapon() {
		dashStroke = 0;
		restricted = false;
		damageLow = 0;
		damageHigh = 0;
		range = 1;
		heavyAllowed = false;
		heavyDamageLow = 0;
		heavyDamageHigh = 0;
		heavyRange = 2;
		attacksPerRound = 1;
		computer = 0;
		enemyShieldMod = 1;
		streaming = false;
		soundEffect = "ShipLaser";
		weaponSpread = 1; // 1, 2, 4, 7
		windUpFrames = 3;
		holdFrames = 0;
		beamStroke = 0;
		beamColor = Color.red;
		beamColor2 = null;
		cycleColor = null;
		cycleColor2 = null;
		weaponStroke = null;
		switch(weaponId) {
			case 0: // LASER
				damageLow = 1;
				damageHigh = 4;
				heavyAllowed = true;
				heavyDamageLow = 1;
				heavyDamageHigh = 7;
				cost = 8;
				size = 10;
				power = 25;
				beamColor = new Color(0x9f,0x33,0x35);
				beamStroke = 1;
				soundEffect = "ShipLaser";
				break;
			case 1: // GATLING LASER
				damageLow = 1;
				damageHigh = 4;
				attacksPerRound = 4;
				cost = 20;
				size = 20;
				power = 70;
				beamColor = new Color(0xbd,0x23,0x3a);
				beamColor2 = new Color(0x72,0x10,0x10);
				beamStroke = 1;
				soundEffect = "ShipMultiLaser";
				break;
			case 2: // NEUTRON PELLET GUN
				damageLow = 2;
				damageHigh = 5;
				enemyShieldMod = .5f;
				cost = 7.5f;
				size = 15;
				power = 25;
				beamColor = new Color(0xa8,0xb4,0x85);
				beamStroke = 1;
				dashStroke = 3;
				soundEffect = "ShipNeutronPelletGun";
				break;
			case 3: // ION CANNON
				damageLow = 3;
				damageHigh = 8;
				heavyAllowed = true;
				heavyDamageLow = 3;
				heavyDamageHigh = 15;
				cost = 10;
				size = 15;
				power = 35;
				beamColor = new Color(0xa4,0x7b,0x56);
				beamStroke = 1;
				soundEffect = "ShipIonCannon";
				break;
			case 4: // MASS DRIVER
				weaponSpread = 1;
				damageLow = 5;
				damageHigh = 8;
				enemyShieldMod = .5f;
				cost = 18;
				size = 55;
				power = 50;
				beamColor = new Color(0xac,0xac,0xac);
				beamStroke = 1;
				dashStroke = 3;
				soundEffect = "ShipMassDriver";
				break;
			case 5: // NEUTRON BLASTER
				damageLow = 3;
				damageHigh = 12;
				heavyAllowed = true;
				heavyDamageLow = 3;
				heavyDamageHigh = 24;
				cost = 15;
				size = 20;
				power = 60;
				beamColor = new Color(0x3c,0x03,0x78);
				beamColor2 = new Color(0xcd,0xb1,0xe8);
				beamStroke = 1;
				soundEffect = "ShipNeutronBlaster";
				break;
			case 6: // GRAVITON BEAM
				damageLow = 1;
				damageHigh = 15;
				streaming = true;
				weaponSpread = 4;
				holdFrames = 6;
				cost = 12;
				size = 30;
				power = 60;
				beamColor = new Color(0x28,0x00,0x7e);
				beamColor2 = new Color(0xf5,0xb7,0xf3);
				beamStroke = 1;
				soundEffect = "ShipGravitonBeam";
				break;
			case 7: // HARD BEAM
				weaponSpread = 1;
				damageLow = 8;
				damageHigh = 12;
				enemyShieldMod = .5f;
				cost = 25;
				size = 50;
				power = 100;
				beamColor = new Color(0xf0,0xb5,0x6e);
				beamColor2 = new Color(0xcb,0x81,0x29);
				beamStroke = 1;
				soundEffect = "ShipHardBeam";
				break;
			case 8: // FUSION BEAM
				damageLow = 4;
				damageHigh = 16;
				heavyAllowed = true;
				heavyDamageLow = 4;
				heavyDamageHigh = 30;
				cost = 13;
				size = 20;
				power = 75;
				beamColor = new Color(0x0c,0x56,0x0c);
				beamColor2 = new Color(0x82,0xc8,0x82);
				beamStroke = 1;
				soundEffect = "ShipFusionBeam";
				break;
			case 9: // MEGABOLT CANNON
				damageLow = 2;
				damageHigh = 20;
				weaponSpread = 4;
				holdFrames = 6;
				computer = 3;
				cost = 16;
				size = 30;
				power = 65;
				beamColor = new Color(0xe5,0xee,0xbe);
				cycleColor = new Color(0xce,0xe2,0x89);
				cycleColor2 = new Color(0xea,0xbb,0xea);
				beamStroke = 1;
				soundEffect = "ShipMegaBoltCannon";
				break;
			case 10: // PHASOR
				damageLow = 5;
				damageHigh = 20;
				heavyAllowed = true;
				heavyDamageLow = 5;
				heavyDamageHigh = 40;
				cost = 18;
				size = 20;
				power = 90;
				beamColor = new Color(0xb6,0x07,0x5a);
				beamColor2 = new Color(0xde,0x8d,0xb3);
				beamStroke = 1;
				soundEffect = "ShipPhasor";
				break;
			case 11: // AUTO-BLASTER
				damageLow = 4;
				damageHigh = 16;
				attacksPerRound = 3;
				cost = 24;
				size = 30;
				power = 90;
				beamColor = new Color(0x24,0xbe,0x93);
				beamColor2 = new Color(0x03,0x25,0x1d);
				beamStroke = 1;
				soundEffect = "ShipAutoBlaster";
				break;
			case 12: // TACHYON BEAM
				damageLow = 1;
				damageHigh = 25;
				weaponSpread = 4;
				holdFrames = 6;
				streaming = true;
				cost = 18;
				size = 30;
				power = 80;
				beamColor = new Color(0x36,0x06,0x00);
				beamColor2 = new Color(0xe1,0xa3,0x8d);
				beamStroke = 1;
				soundEffect = "ShipTachyonBeam";
				break;
			case 13: // GAUSS AUTO-CANNON
				damageLow = 7;
				damageHigh = 10;
				enemyShieldMod = .5f;
				attacksPerRound = 4;
				cost = 40;
				size = 105;
				power = 105;
				beamColor = new Color(0xac,0xac,0xac);
				beamStroke = 3;
				dashStroke = 1;
				soundEffect = "ShipGaussAutoCannon";
				break;
			case 14: // PARTICLE BEAM
				damageLow = 10;
				damageHigh = 20;
				enemyShieldMod = .5f;
				cost = 26;
				size = 90;
				power = 75;
				beamColor = new Color(0x3b,0x39,0x48);
				beamColor2 = new Color(0x95,0x94,0x9d);
				dashStroke = 1;
				beamStroke = 3;
				soundEffect = "ShipParticleBeam";
				break;
			case 15: // PLASMA CANNON
				damageLow = 6;
				damageHigh = 30;
				weaponSpread = 4;
				cost = 24;
				size = 30;
				power = 110;
				beamColor = new Color(0xfe,0x29,0x28);
				beamColor2 = new Color(0xce,0x1f,0x1e);
				beamStroke = 1;
				soundEffect = "ShipPlasmaCannon";
				break;
			case 16: // DEATH RAY
				range = 1;
				damageLow = 200;
				damageHigh = 1000;
				weaponSpread = 7;
				holdFrames = 6;
				restricted = true;
				cost = 120;
				size = 2000;
				power = 2000;
				beamColor = new Color(0x56,0x02,0xc2);
				beamColor2 = new Color(0xcb,0x33,0x5e);
				beamStroke = 1;
				soundEffect = "ShipDeathRay";
				break;
			case 17: // DISRUPTOR
				damageLow = 10;
				damageHigh = 40;
				range = 2;
				cost = 100;
				size = 70;
				power = 160;
				beamColor = new Color(0xa4,0x7b,0x56);
				beamColor2 = new Color(0x82,0xc8,0x82);
				beamStroke = 1;
				soundEffect = "ShipDisruptor";
				break;
			case 18: // PULSE PHASOR
				damageLow = 5;
				damageHigh = 20;
				attacksPerRound = 3;
				holdFrames = 4;
				cost = 42;
				size = 40;
				power = 120;
				beamColor = new Color(0xb6,0x07,0x5a);
				cycleColor = new Color(0xde,0x8d,0xb3);
				beamStroke = 1;
				soundEffect = "ShipPulsePhasor";
				break;
			case 19: // TRI-FOCUS PLASMA CANNON
				damageLow = 20;
				damageHigh = 50;
				weaponSpread = 2;
				cost = 55;
				size = 65;
				power = 180;
				beamColor = new Color(0xfe,0x29,0x28);
				beamColor2 = new Color(0xce,0x1f,0x1e);
				beamStroke = 1;
				soundEffect = "ShipTriFocusPlasmaCannon";
				break;
			case 20: // STELLAR CONVERTOR
				damageLow = 10;
				damageHigh = 35;
				attacksPerRound = 4;
				weaponSpread = 4;
				holdFrames = 4;
				range = 3;
				cost = 105;
				size = 200;
				power = 300;
				beamColor = new Color(0xff,0xff,0xb0);
				cycleColor = new Color(0xff,0xff,0xff);
				beamStroke = 1;
				soundEffect = "ShipStellarConvertor";
				break;
			case 21: // MAULER DEVICE
				damageLow = 20;
				damageHigh = 100;
				weaponSpread = 7;
				holdFrames = 4;
				cost = 120;
				size = 150;
				power = 300;
				beamColor = new Color(0x00,0xaf,0x7d);
				beamColor2 = new Color(0x4c,0xd0,0xab);
				beamStroke = 1;
				soundEffect = "ShipMauler";
				break;
			case 22: // AMOEBA STREAM
				damageLow = 250;
				damageHigh = 1000;
				range = 3;
				streaming = true;
				restricted = true;
				beamColor = Color.green;
				beamStroke = 5;
				soundEffect = "ShipAmoebaStream";
				break;
			case 23: // CRYSTAL RAY
				damageLow = 100;
				damageHigh = 300;
				range = 3;
				attacksPerRound = 4;
				restricted = true;
				beamColor = Color.white;
				beamStroke = 4;
				soundEffect = "ShipMultiLaser";
				break;
		}
//		System.out.println( "##### New Weapon : " + soundEffect +
//							"  Attacks Per Round = " + attacksPerRound);
	}
	private void setPaint(Graphics2D g, int i, int weaponX, int weaponY, int weaponDx, int weaponDy) {
		int s10 = scaled(10);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		if(beamColor2 != null) {
			GradientPaint gp = new GradientPaint(weaponX+i*s10, weaponY+i*s10, beamColor,
					weaponX+i*s10+weaponDx, weaponY+i*s10+weaponDy, beamColor2, true);
			g.setPaint(gp);
		} else
			g.setColor(beamColor);
			
		if(cycleColor != null) {
			if(i%2 == 0) {
				g.setColor(cycleColor);
			} else if (cycleColor2 != null && i%3 == 0) {
				g.setColor(cycleColor2);
			} else {
				g.setColor(beamColor);
			}
		}
	}
	private void drawAttack(Graphics2D g, int weaponX, int weaponY, int impactX, int impactY,
			int wpnNum, float dmg, int count, int boxW, int boxH, float force) {
		IGameOptions opt = options();
		count = 50;
		int targetTLx	= targetCtr[0]-boxW/2; // Top Left points
		int targetTLy	= targetCtr[1]-boxH/2;
		int targetCtrX	= targetCtr[0];
		int targetCtrY	= targetCtr[1];
		int sourceTLx	= sourceCtr[0]-boxW/2;
		int sourceTLy	= sourceCtr[1]-boxH/2;
		int shipTLx		= targetCtr[0]-shipImg.getWidth()/2;
		int shipTLy		= targetCtr[1]-shipImg.getHeight()/2;
		int distance	= (int) Math.round(Math.sqrt (
				Math.pow(weaponX-impactX, 2) +
				Math.pow(weaponY-impactY, 2)));
		int distFactor	= 8*distance;
		int weaponDx	= (impactX-weaponX)/distFactor;
		int weaponDy	= (impactY-weaponY)/distFactor;
		int weaponDz	= opt.weaponZRandom();
		int weaponZ		= scaled(opt.weaponZposition()+roll(-weaponDz,weaponDz));
		boolean isAbove = weaponZ>=0;
		long sleepTime	= opt.beamAnimationDelay(); // Original = 50;
		int targetSize	= hullId;
		if (isMonster())
			targetSize	= 3;
		int sourceSize	= 3;
		int shieldBorders = opt.shieldBorder(sourceSize);
		int wpnCount = count / attacksPerRound;

		int beamWidth = scaled(3);
		int spotWidth = scaled(attacksPerRound *(1 + 2*weaponSpread));
		if ((dashStroke > 0) && (weaponStroke == null)) {
			float dash[] = new float[]{0, beamWidth*dashStroke};
			weaponStroke = new BasicStroke(beamWidth, CAP_ROUND, JOIN_BEVEL, 0, dash, 0);
		}
		if (weaponStroke != null)
			g.setStroke(weaponStroke);
		else {
			int strokeSize = beamStroke*2;
			if (isHeavy)
				strokeSize += 1;
			g.setStroke(new BasicStroke(scaled(strokeSize), CAP_ROUND, JOIN_ROUND));
		}
		int windUpFramesNum = opt.beamWindupFrames();
		int holdFramesNum = max(0, holdFrames + opt.beamHoldFrames());
		if (isHeavy) {
			spotWidth = scaled((1 + (beamStroke+2*weaponSpread-1)));
			holdFrames = heavyHoldFramesNum;
		} else {
			spotWidth = scaled((beamStroke+2*weaponSpread-1));
			holdFramesNum += opt.heavyBeamHoldFrames();
		}
		int fadingFramesNum = opt.shieldFadingFrames()? windUpFramesNum+2 : 0;
		int landUpFramesNum = windUpFramesNum;

		boolean playerEcho = opt.playerSoundEcho();
		CombatShield cs = new CombatShield(holdFramesNum, landUpFramesNum, fadingFramesNum,
				boxW, boxH, targetCtrX, targetCtrY, shieldColor, opt.shieldEnveloping(), shieldBorders,
				opt.shieldTransparency(), opt.shieldFlickering(), opt.shieldNoisePct(), shieldLevel, shipImg,
				weaponX, weaponY, weaponZ, beamColor, spotWidth, attacksPerRound, damage, force);

		Rectangle toRefreshRec = cs.shieldRec();
		int shieldTLx = toRefreshRec.x;
		int shieldTLy = toRefreshRec.y;

		// Full Beam trajectory generation
		ArrayList<Line2D.Double> lines = new ArrayList<>();
		double insideRatio = 0;
		int[] xAdj = new int[attacksPerRound];
		int[] yAdj = new int[attacksPerRound];
		int[] zAdj = new int[attacksPerRound]; // init to 0 by default
		int roll = 3*(targetSize+1);
		for(int i = 0; i < attacksPerRound; ++i) {
			xAdj[i] = scaled(roll(-roll,roll));
			yAdj[i] = scaled(roll(-roll,roll));
		}
		int[][] shipImpact = cs.setImpact(xAdj, yAdj, zAdj);
		BufferedImage[][] shieldArray = cs.getShieldArray();
		insideRatio	= cs.meanInsideRatio();
		for(int i = 0; i < attacksPerRound; ++i) {
			if (weaponSpread > 1) {
				int xMod = (sourceTLy == targetTLy) ? 0 : 1;
				int yMod = (sourceTLx == targetTLx) ? 0 : 1;
				if ((sourceTLx < targetTLx) && (sourceTLy < targetTLy))
					xMod = -1;
				else if ((sourceTLx > targetTLx) && (sourceTLy > targetTLy))
					xMod = -1;
				for (int n = -1 * weaponSpread; n <= weaponSpread; n++) {
					int adj = scaled(n);
					lines.addAll(addMultiLines(sourceSize, wpnCount, weaponX, weaponY,
							shipImpact[i][0]+(xMod*adj), shipImpact[i][1]+(yMod*adj)));
				}
			} else {
				lines.addAll(addMultiLines(sourceSize, wpnCount, weaponX, weaponY, shipImpact[i][0], shipImpact[i][1]));
			}
		}
		double fraction =  (1-insideRatio) / (windUpFramesNum);
		//
		// Animations start Here
		//
		float hiddenBeamAlpha = 0.3f;
		if(beamColor2 == null && cycleColor == null)
			beamColor2 = multColor(beamColor, 0.75f);
		setPaint(g, 0, weaponX, weaponY, weaponDx, weaponDy);
		// Start playing sound
		if (!muted && !playerIsTarget)
			playAudioClip(soundEffect);
		// Beams Progression toward target
		SortedMap<Integer, ArrayList<Line2D.Double>> partLines = new TreeMap<>();
		for(int i = 0; i < windUpFramesNum; ++i) {
			ArrayList<Line2D.Double> pl = new ArrayList<>();
			for(Line2D.Double line : lines) {
				double newX1 = line.getX1() + (line.getX2() - line.getX1()) * i * fraction;
				double newY1 = line.getY1() + (line.getY2() - line.getY1()) * i * fraction;
				double newX2 = line.getX1() + (line.getX2() - line.getX1()) * (i + 1) * fraction;
				double newY2 = line.getY1() + (line.getY2() - line.getY1()) * (i + 1) * fraction;
				pl.add(new Line2D.Double(newX1, newY1, newX2, newY2));
			}
			partLines.put(i, pl);
			setPaint(g, 0, weaponX, weaponY, weaponDx, weaponDy);
			paintLines(pl, g);
			if (!isAbove)
				g.drawImage(shipImg, shipTLx, shipTLy, null);
			paintImmediately(winRec);
			sleep(sleepTime);
		}

		// Beams reach the shield
		ArrayList<Line2D.Double> hitLines = new ArrayList<>(); // The ones that go thru the shield
		clearWindow(g);
		setPaint(g, windUpFramesNum, weaponX, weaponY, weaponDx, weaponDy);
		for(Line2D.Double line : lines) {
			double newX1 = line.getX1() + (line.getX2() - line.getX1()) * windUpFramesNum * fraction;
			double newY1 = line.getY1() + (line.getY2() - line.getY1()) * windUpFramesNum * fraction;
			double newX2 = line.getX2();
			double newY2 = line.getY2();
			hitLines.add(new Line2D.Double(newX1, newY1, newX2, newY2));
		}
		if (!muted && playerIsTarget) // player hear sound when hit
			if(playerEcho)
				playAudioClip(soundEffect, targetSize);
			else
				playAudioClip(soundEffect);

		// Draw first shield part
		if (isAbove) {
			g.drawImage(shieldArray[BELLOW][0], shieldTLx, shieldTLy, null);
			g.drawImage(shipImg, shipTLx, shipTLy, null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hiddenBeamAlpha));
			paintLines(hitLines, g); // hitting lines are in-between
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			g.drawImage(shieldArray[ABOVE][0], shieldTLx, shieldTLy, null);
			paintLines(partLines, g); // visible line are above
		} else {
			paintLines(partLines, g); // visible line are bellow
			g.drawImage(shieldArray[BELLOW][0], shieldTLx, shieldTLy, null);
			paintLines(hitLines, g); // hitting lines are in-between
			g.drawImage(shipImg, shipTLx, shipTLy, null);
			g.drawImage(shieldArray[ABOVE][0], shieldTLx, shieldTLy, null);
		}
		paintImmediately(winRec);
		sleep(sleepTime);

		// Show Continuous
		if (isAbove) {
			for(int i = 0; i < holdFramesNum; i++) {
				clearWindow(g);
				setPaint(g, i+1+windUpFramesNum, weaponX, weaponY, weaponDx, weaponDy);
				int shieldIdx = i+1;
				g.drawImage(shieldArray[BELLOW][shieldIdx], shieldTLx, shieldTLy, null);
				g.drawImage(shipImg, shipTLx, shipTLy, null);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hiddenBeamAlpha));
				paintLines(hitLines, g); // hitting lines are in-between
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				g.drawImage(shieldArray[ABOVE][shieldIdx], shieldTLx, shieldTLy, null);

				paintLines(partLines, g);
				paintImmediately(winRec);
				sleep(sleepTime);
			}
		} else {
			for(int i = 0; i < holdFramesNum; i++) {
				clearWindow(g);
				setPaint(g, i+1+windUpFramesNum, weaponX, weaponY, weaponDx, weaponDy);
				paintLines(partLines, g);
				int shieldIdx = i+1;
				paintLines(hitLines, g); // hitting lines are in-between
				g.drawImage(shieldArray[BELLOW][shieldIdx], shieldTLx, shieldTLy, null);
				g.drawImage(shipImg, shipTLx, shipTLy, null);
				g.drawImage(shieldArray[ABOVE][shieldIdx], shieldTLx, shieldTLy, null);
				paintImmediately(winRec);
				sleep(sleepTime);
			}			
		}

		// Show end of beam
		if (isAbove) {
			for(int i = 0; i < windUpFramesNum; ++i) {
				clearWindow(g);
				setPaint(g, i+1+windUpFramesNum+holdFramesNum, weaponX, weaponY, weaponDx, weaponDy);
				int shieldIdx = i+1+holdFramesNum;

				g.drawImage(shieldArray[BELLOW][shieldIdx], shieldTLx, shieldTLy, null);
				g.drawImage(shipImg, shipTLx, shipTLy, null);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hiddenBeamAlpha));
				paintLines(hitLines, g); // hitting lines are in-between
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				g.drawImage(shieldArray[ABOVE][shieldIdx], shieldTLx, shieldTLy, null);

				partLines.get(i).clear();
				paintLines(partLines, g);
				paintImmediately(winRec);
				sleep(sleepTime);
			}			
		} else {
			for(int i = 0; i < windUpFramesNum; ++i) {
				clearWindow(g);
				setPaint(g, i+1+windUpFramesNum+holdFramesNum, weaponX, weaponY, weaponDx, weaponDy);
				partLines.get(i).clear();
				paintLines(partLines, g);
				int shieldIdx = i+1+holdFramesNum;
				g.drawImage(shieldArray[BELLOW][shieldIdx], shieldTLx, shieldTLy, null);
				paintLines(hitLines, g); // hitting lines are in-between
				g.drawImage(shipImg, shipTLx, shipTLy, null);
				g.drawImage(shieldArray[ABOVE][shieldIdx], shieldTLx, shieldTLy, null);
				paintImmediately(winRec);
				sleep(sleepTime);
			}			
		}
		// Show shield fading
		for(int i = 0; i < fadingFramesNum; ++i) {
			clearWindow(g);
			int shieldIdx = i+1+holdFramesNum+windUpFramesNum;
			g.drawImage(shieldArray[BELLOW][shieldIdx], shieldTLx, shieldTLy, null);
			g.drawImage(shipImg, shipTLx, shipTLy, null);
			g.drawImage(shieldArray[ABOVE][shieldIdx], shieldTLx, shieldTLy, null);
			paintImmediately(winRec);
			sleep(sleepTime);
		}
	}
	public DemoShields() {
		init();
		animationDelay = 50;
		startAnimation();
	}
	@Override public int scaled(int i) {
		float resizeAmt = Rotp.resizeAmt()*zoomFactor;
		if (i < 1)
			return (int) Math.ceil(resizeAmt*i);
		else if (i > 1)
			return (int) Math.floor(resizeAmt*i);
		else
			return i;
	}
	private void clearWindow(Graphics2D g) {
		g.setPaint(spaceBlue);
		g.fillRect(0, 0, windowWidth, windowHeight);
	}
	private void paintBeamView(Graphics2D g) {
		holdTimer = true;
		clearWindow(g);
		int shipTLx	= targetCtr[0]-shipImg.getWidth()/2;
		int shipTLy	= targetCtr[1]-shipImg.getHeight()/2;

		if (this.fullRandom) {
			srcLocation	= roll(0, srcLocX.length-1);
			weaponId	= roll(0, weaponMaxId);
			colorId		= roll(0, listColors.size()-1);
			folderId	= roll(0, MAX_STYLES);
			monsterId	= roll(0, monsters.length-1);
			hullId		= roll(0, MAX_HULLS);
			modelId		= roll(0, MAX_MODELS);
			playerIsTarget = rng().nextBoolean();
			loadShields();
		} else {
			if (srcRotate)
				srcLocation++;
			if (srcLocation >= srcLocX.length)
				srcLocation = 0;
			if (weaponRotate) {
				nextWeapon();
				this.initWeapon();
			}
			if (colorRotate)
				nextColor();
		}
		shieldColor = listColors.get(colorId);
		sourceCtr = newSourcePos(srcLocX[srcLocation], srcLocY[srcLocation], scaled(200));
		g.drawImage(shipImg, shipTLx, shipTLy, this);
		paintImmediately(winRec);		
		sleep(500);

		int weaponX	= sourceCtr[0] + boxWidth/3;
		int weaponY	= sourceCtr[1];
		int impactX	= targetCtr[0];
		int impactY	= targetCtr[1];
		int wpnNum	= weaponId;
		float dmg	= damage;
		int count	= wpnCount;
		float force	= beamForce;
		
		drawAttack(g, weaponX, weaponY, impactX, impactY, wpnNum, dmg, count, boxWidth, boxHeight, force);
		holdTimer = false;
	}
	@Override public void paintComponent(Graphics g0) {
		if (holdTimer)
			return;
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		paintBeamView(g);
	}
	@Override public void actionPerformed(ActionEvent e) {
		if (holdTimer)
			return;		
		repaint();
	}
	private Image loadImage(int shipStyle, int shipSize, int shapeId) {
		if (isMonster())
			return image(monsters[monsterId]);
		ShipImage images = ShipLibrary.current().shipImage(shipStyle, shipSize, shapeId);
		return icon(images.baseIcon()).getImage();
	}
	private void startAnimation() {
		if (animationTimer == null) {
			animationTimer = new Timer(animationDelay, this);
			animationTimer.start();
		}
		else if (!animationTimer.isRunning())
			animationTimer.restart();
	}
	private boolean prevFolder() {
		monsterId=monsters.length-1;
		folderId--;
		if (folderId < 0) {
			folderId = MAX_STYLES;
			return true;
		}
		return false;
	}
	private boolean nextFolder() {
		monsterId=0;
		folderId++;
		if (folderId > MAX_STYLES) {
			folderId = 0;
			return true;
		}
		return false;
	}
	private boolean prevHull() {
		if (isMonster())
			return false;
		hullId--;
		if (hullId < 0) {
			hullId = MAX_HULLS;
			return true;
		}
		return false;
	}
	private boolean nextHull() {
		if (isMonster())
			return false;
		hullId++;
		if (hullId > MAX_HULLS) {
			hullId = 0;
			return true;
		}
		return false;
	}
	private boolean prevModel() {
		if (isMonster())
			return false;
		modelId--;
		if (modelId < 0) {
			modelId = MAX_MODELS;
			return true;
		}
		return false;
	}
	private boolean nextModel() {
		if (isMonster())
			return false;
		modelId++;
		if (modelId > MAX_MODELS) {
			modelId = 0;
			return true;
		}
		return false;
	}
	private boolean prevMonster() {
		monsterId--;
		System.out.println("prevMonster(): " + monsterId);
		if (monsterId < 0) {
			monsterId = monsters.length-1;
			return true;
		}
		return false;
	}
	private boolean nextMonster() {
		monsterId++;
		System.out.println("prevMonster(): " + monsterId);
		if (monsterId == monsters.length) {
			monsterId = 0;
			return true;
		}
		return false;
	}
	private void prevShip() {
		if (isMonster()) {
			if (prevMonster())
				prevFolder();
			return;
		}
		if (prevModel())
			if (prevHull())
				prevFolder();
	}
	private void nextShip() {
		if (isMonster()) {
			if (nextMonster())
				nextFolder();
			return;
		}
		if (nextModel())
			if (nextHull())
				nextFolder();
	}
	private void prevWeapon() {
		weaponId--;
		if (weaponId < 0)
			weaponId = weaponMaxId;
	}
	private void nextWeapon() {
		weaponId++;
		if (weaponId > weaponMaxId)
			weaponId = 0;
	}
	private void prevZoom() {
		zoomFactor--;
		if (zoomFactor < 1)
			zoomFactor = MAX_ZOOM;
		resize = true;
	}
	private void nextZoom() {
		zoomFactor++;
		if (zoomFactor > MAX_ZOOM)
			zoomFactor = 1;
		resize = true;
	}
	private void prevColor() {
		colorId--;
		if (colorId < 0)
			colorId = listColors.size()-1;
	}
	private void nextColor() {
		colorId++;
		if (colorId >= listColors.size())
			colorId = 0;
	}
	public static void main(String args[]) {
		DemoShields anim = new DemoShields();
		app = new JFrame("3D Shields and Echo Sound Test");
		app.add(anim, BorderLayout.CENTER);
		app.setSize(anim.windowWidth, anim.windowHeight);
		app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		app.setLocation(0, 0);
		app.addKeyListener(new KeyListener(){
			@Override public void keyPressed(KeyEvent e) {}
			@Override  public void keyTyped(KeyEvent e) {}
			@Override public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()) {
//					case KeyEvent.VK_B:
//						anim.beamView = !anim.beamView;
//						anim.holdTimer = false;
//						return;
					case KeyEvent.VK_C:
						if (e.isControlDown())
							anim.colorRotate = !anim.colorRotate;
						else if (e.isShiftDown())
							anim.prevColor();
						else
							anim.nextColor();
						anim.loadShields();
						return;
					case KeyEvent.VK_M:
						if (e.isShiftDown())
							anim.prevModel();
						else
							anim.nextModel();
						anim.loadShields();
						return;
					case KeyEvent.VK_N:
					case KeyEvent.VK_RIGHT:
						anim.nextShip();
						anim.loadShields();
						return;
					case KeyEvent.VK_LEFT:
						anim.prevShip();
						anim.loadShields();
						return;
					case KeyEvent.VK_DOWN:
						anim.loadShields();
						return;
					case KeyEvent.VK_P: // Is Player
						anim.playerIsTarget = !anim.playerIsTarget;
						return;
					case KeyEvent.VK_Q: // Quiet
						anim.muted = !anim.muted;
						return;
					case KeyEvent.VK_R: // Randomize
						anim.fullRandom = !anim.fullRandom;
						return;
					case KeyEvent.VK_T:
					case KeyEvent.VK_S:
						if (e.isShiftDown())
							anim.prevFolder();
						else
							anim.nextFolder();
						anim.loadShields();
						return;
					case KeyEvent.VK_H:
						if (e.isShiftDown())
							anim.prevHull();
						else
							anim.nextHull();
						anim.loadShields();
						return;
					case KeyEvent.VK_W:
						if (e.isControlDown())
							anim.weaponRotate = !anim.weaponRotate;
						else if (e.isShiftDown())
							anim.prevWeapon();
						else
							anim.nextWeapon();
						anim.loadShields();
						return;
					case KeyEvent.VK_Z:
						if (e.isShiftDown())
							anim.prevZoom();
						else
							anim.nextZoom();
						anim.loadShields();
						return;
					case KeyEvent.VK_NUMPAD1:
						anim.srcRotate	 = false;
						anim.srcLocation = 0;
						return;
					case KeyEvent.VK_NUMPAD2:
						anim.srcRotate	 = false;
						anim.srcLocation = 1;
						return;
					case KeyEvent.VK_NUMPAD3:
						anim.srcRotate	 = false;
						anim.srcLocation = 2;
						return;
					case KeyEvent.VK_NUMPAD4:
						anim.srcRotate	 = false;
						anim.srcLocation = 7;
						return;
					case KeyEvent.VK_NUMPAD5:
						anim.srcRotate	 = true;
						return;
					case KeyEvent.VK_NUMPAD6:
						anim.srcRotate	 = false;
						anim.srcLocation = 3;
						return;
					case KeyEvent.VK_NUMPAD7:
						anim.srcRotate	 = false;
						anim.srcLocation = 6;
						return;
					case KeyEvent.VK_NUMPAD8:
						anim.srcRotate	 = false;
						anim.srcLocation = 5;
						return;
					case KeyEvent.VK_NUMPAD9:
						anim.srcRotate	 = false;
						anim.srcLocation = 4;
						return;
				}
			}
		});
		app.setVisible(true);
	}
}
