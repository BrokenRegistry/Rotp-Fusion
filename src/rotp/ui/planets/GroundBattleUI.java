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
package rotp.ui.planets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints; // modnar: needed for adding RenderingHints
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import rotp.model.colony.Colony;
import rotp.model.colony.ColonyDefense;
import rotp.model.empires.Empire;
import rotp.model.empires.RaceCombatAnimation;
import rotp.model.galaxy.Transport;
import rotp.model.tech.TechHandWeapon;
import rotp.ui.BasePanel;
import rotp.util.Base;
import rotp.util.sound.SoundClip;

public class GroundBattleUI extends BasePanel implements MouseListener {
    private static final long serialVersionUID = 1L;
	// modnar: MAX_SOLDIERS = 500 causes bugs when troops > 500
	// other parts of the code has been changed to work around having any such max value
    private static final int MAX_SOLDIERS = 500;
    // private static final int MAX_COUNTDOWN = 50;
    private static final int MAX_SHIPS = 3;
    // private static final int MIN_COUNTDOWN = -10;
    private final static int[] attackerState = new int[MAX_SOLDIERS];
    private final static int[] defenderState = new int[MAX_SOLDIERS];
    private final static int[] attackerX = new int[MAX_SOLDIERS];
    private final static int[] defenderX = new int[MAX_SOLDIERS];
    private final static int[] attackerY = new int[MAX_SOLDIERS];
    private final static int[] defenderY = new int[MAX_SOLDIERS];

    private static int ATTACKER_END_FIRING, DEFENDER_END_FIRING, ATTACKER_END_DYING, DEFENDER_END_DYING;
    private final static int BEGIN_FIRING = 1;
    private final static int NOT_FIRING = 0;
    private final static int BEGIN_DYING = -1;

    private boolean attackerDead;  // true if attacker died this turn, false for defender
    private int deathThisTurn = 0; // index of attacker/defender that died this turn

    private Colony colony;
    Empire defenderEmp, attackerEmp;
    private Transport transport;
    private TechHandWeapon attackerWeapon;
    private TechHandWeapon defenderWeapon;
    private int totalAttackers  = 0;
    private int totalDefenders = 0;
    private int landingCount;

    private Image landscapeImg;
    private LinearGradientPaint soldierBackG;
    String subtitle;

    private final List<Image> descendingFrames = new ArrayList<>();
    private final List<Integer> descendingFrameRefs = new ArrayList<>();
    private final List<Image> openingFrames = new ArrayList<>();
    private final List<Integer> openingFrameRefs = new ArrayList<>();

    List<BufferedImage> attackerFrames = new ArrayList<>();
    List<BufferedImage> defenderFrames = new ArrayList<>();
    List<BufferedImage> attackerDeathFrames = new ArrayList<>();
    List<BufferedImage> defenderDeathFrames = new ArrayList<>();
    List<Integer> remainingAttackers = new ArrayList<>();
    List<Integer> remainingDefenders = new ArrayList<>();
    // which animated frame the soldiers will fire
    int attackerFiringFrame, defenderFiringFrame;
    int attackerFinalFrame, defenderFinalFrame;
    int attackerIconW, attackerIconH, defenderIconW, defenderIconH;
    int attackerImgW, attackerImgH, defenderImgW, defenderImgH;
    float attackerScale, defenderScale;
    int attackerYSpacing, defenderYSpacing, attackerXSpacing, defenderXSpacing;
    boolean attackerFired, defenderFired;
    boolean exited = false;
    int baseIconH;
    // x,y coords (within icon img) of gun when firing
    int attackerGunX = 0;
    int attackerGunY = 0;
    int defenderGunX = 0;
    int defenderGunY = 0;
    LandingShip[] ships = new LandingShip[MAX_SHIPS];
    private SoundClip shipLanding;
    String sysName;
    String title;

    public GroundBattleUI() {
        init();
    }
    private void init() {
        setBackground(Color.black);
        addMouseListener(this);
    }
    public void init(Colony c, Transport tr) {
        colony = c;
        transport = tr;
        baseIconH = s70;  // standard width of troopers
        landingCount = 0;
        exited = false;
        attackerEmp = transport.empire();
        defenderEmp = colony.empire();
        sysName = player().sv.name(colony.starSystem().id);
        if (defenderEmp == attackerEmp) {
            title = text("INVASION_BATTLE_REBELS", str(galaxy().currentYear()), sysName);
            title = attackerEmp.replaceTokens(title, "attacker");
        }
        else {
            title = text("INVASION_BATTLE", str(galaxy().currentYear()), sysName);
            title = attackerEmp.replaceTokens(title, "attacker");
            title = defenderEmp.replaceTokens(title, "defender");
        }

        initLandscapeImage(colony);

        if (tr.size() < tr.launchSize()) 
            subtitle = text("INVASION_SOME_TROOPS_LANDED", str(tr.size()), str(tr.launchSize()));
        else
            subtitle = text("INVASION_TROOPS_LANDED", str(tr.size()));

		subtitle = attackerEmp.replaceTokens(subtitle, "attacker");

        descendingFrames.clear();
        descendingFrameRefs.clear();

		allFrames(attackerEmp.transportDescKey(), attackerEmp.transportDescFrames(), 0, descendingFrames, descendingFrameRefs);
        openingFrames.clear();
        openingFrameRefs.clear();
		allFrames(attackerEmp.transportOpenKey(), attackerEmp.transportOpenFrames(), 0, openingFrames, openingFrameRefs);

        for (int i=0;i<ships.length;i++)
            ships[i] = new LandingShip(i);

        // determine which attack and death animations to use
        attackerWeapon = attackerEmp.tech().topHandWeaponTech();
        defenderWeapon = defenderEmp.tech().topHandWeaponTech();
        RaceCombatAnimation attacker;
        RaceCombatAnimation defender;
        RaceCombatAnimation attackerDeath  = null;
        RaceCombatAnimation defenderDeath  = null;
		if (c.planet().isEnvironmentHostile()) {
			attacker  = attackerEmp.troopHostile();
			defender  = defenderEmp.troopHostile();
			switch(attackerWeapon.deathType) {
				case TechHandWeapon.COLLAPSE:	defenderDeath = defenderEmp.troopDeath1H(); break;
				case TechHandWeapon.DISRUPT:	defenderDeath = defenderEmp.troopDeath2H(); break;
				case TechHandWeapon.IMMOLATE:	defenderDeath = defenderEmp.troopDeath3H(); break;
				case TechHandWeapon.VAPORIZE:	defenderDeath = defenderEmp.troopDeath4H(); break;
			}
			switch(defenderWeapon.deathType) {
				case TechHandWeapon.COLLAPSE:	attackerDeath = attackerEmp.troopDeath1H(); break;
				case TechHandWeapon.DISRUPT:	attackerDeath = attackerEmp.troopDeath2H(); break;
				case TechHandWeapon.IMMOLATE:	attackerDeath = attackerEmp.troopDeath3H(); break;
				case TechHandWeapon.VAPORIZE:	attackerDeath = attackerEmp.troopDeath4H(); break;
			}
		}
		else {
			attacker  = attackerEmp.troopNormal();
			defender  = defenderEmp.troopNormal();
			switch(attackerWeapon.deathType) {
				case TechHandWeapon.COLLAPSE:	defenderDeath = defenderEmp.troopDeath1(); break;
				case TechHandWeapon.DISRUPT:	defenderDeath = defenderEmp.troopDeath2(); break;
				case TechHandWeapon.IMMOLATE:	defenderDeath = defenderEmp.troopDeath3(); break;
				case TechHandWeapon.VAPORIZE:	defenderDeath = defenderEmp.troopDeath4(); break;
			}
			switch(defenderWeapon.deathType) {
				case TechHandWeapon.COLLAPSE:	attackerDeath = attackerEmp.troopDeath1(); break;
				case TechHandWeapon.DISRUPT:	attackerDeath = attackerEmp.troopDeath2(); break;
				case TechHandWeapon.IMMOLATE:	attackerDeath = attackerEmp.troopDeath3(); break;
				case TechHandWeapon.VAPORIZE:	attackerDeath = attackerEmp.troopDeath4(); break;
			}
		}

        // init attacker vars
        totalAttackers = tr.size();
        attackerFinalFrame = 0;
        attackerFrames.clear();
        for (Image img: attacker.firingFrames())
            attackerFrames.add(asBufferedImage(img));
        attackerDeathFrames.clear();
        for (Image img: attackerDeath.firingFrames())
            attackerDeathFrames.add(asBufferedImage(img));
        ATTACKER_END_DYING = BEGIN_DYING - attackerDeathFrames.size() + 1;
        ATTACKER_END_FIRING = BEGIN_FIRING + attackerFrames.size() - 1;
        attackerFiringFrame = attacker.firingFrame;
        attackerIconW = attackerFrames.get(0).getWidth();
        attackerIconH = attackerFrames.get(0).getHeight();
        attackerScale = attacker.scale;
        attackerYSpacing = scaled(attacker.ySpacing);
        attackerXSpacing = scaled(attacker.xSpacing);
        attackerImgW = (int) (attackerScale*attackerIconW);
        attackerImgH = (int) (attackerScale*attackerIconH);
        attackerGunX = (int) (attackerScale*attacker.gunX);
        attackerGunY = (int) (attackerScale*attacker.gunY);
        remainingAttackers.clear();
        for (int i=0;i<totalAttackers;i++)
            remainingAttackers.add(i);
        for (int i=0;i<attackerState.length;i++)
            attackerState[i] = NOT_FIRING;  // -1 is "not firing"  0-N is the firing frame #

        // init defender vars
        totalDefenders = c.defense().troops();
        defenderFinalFrame = 0;
        defenderFrames.clear();
        for (Image img: defender.firingFrames())
            defenderFrames.add(flip(asBufferedImage(img)));
        defenderDeathFrames.clear();
        for (Image img: defenderDeath.firingFrames())
            defenderDeathFrames.add(flip(asBufferedImage(img)));
        DEFENDER_END_DYING = BEGIN_DYING - defenderDeathFrames.size() + 1;
        DEFENDER_END_FIRING = BEGIN_FIRING + defenderFrames.size() - 1;
        defenderFiringFrame = defender.firingFrame;
        defenderIconW = defenderFrames.get(0).getWidth();
        defenderIconH = defenderFrames.get(0).getHeight();
        defenderScale = defender.scale;
        defenderYSpacing = scaled(defender.ySpacing);
        defenderXSpacing = scaled(defender.xSpacing);
        defenderImgW = (int) (defenderScale*defenderIconW);
        defenderImgH = (int) (defenderScale*defenderIconH);
        defenderGunX = (int) (defenderScale*defender.gunX);
        defenderGunY = (int) (defenderScale*defender.gunY);
        remainingDefenders.clear();
        for (int i=0;i<totalDefenders;i++)
                remainingDefenders.add(i);
        for (int i=0;i<defenderState.length;i++)
                defenderState[i] = NOT_FIRING;  // -1 is "not firing"  0-N is the firing frame #
		shipLanding = playAudioClip(attackerEmp.shipAudioKey());

        //log("Starting Ground Battle. ", totalAttackers+" attackers vs. ", str(totalDefenders), " defenders");
    }
    private void initLandscapeImage(Colony c) {
        int w = getWidth();
        int h = getHeight();
        landscapeImg = newBufferedImage(w,h);
        Graphics2D g = (Graphics2D) landscapeImg.getGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());
		// modnar: use (slightly) better upsampling
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(c.planet().type().atmosphereImage(), 0, 0, w, h, null);
        g.drawImage(c.planet().type().randomCloudImage(), 0, 0, w, h, null);
        g.drawImage(c.planet().landscapeImage(), 0, 0, w, h, null);

        // draw fortress
        BufferedImage fortImg = colony.empire().fortress(colony.fortressNum());
        //BufferedImage fortImg = defenderEmp.race().fortress(0);
        int fortW = scaled(fortImg.getWidth());
        int fortH = scaled(fortImg.getHeight());
        int fortX = w-fortW;
        int fortY = h-scaled(180)-fortH;
        g.drawImage(fortImg, fortX, fortY, fortX+fortW, fortY+fortH, 0, 0, fortImg.getWidth(), fortImg.getHeight(), null);

        // for hostile planets, draw shield
        if (defenderEmp.isHostile(colony.planet().type())) {
            BufferedImage shieldImg = defenderEmp.shield();
            g.drawImage(shieldImg, fortX, fortY, fortX+fortW, fortY+fortH, 0, 0, shieldImg.getWidth(), shieldImg.getHeight(), null);
        }

        int barY = getHeight()-scaled(140);
        if (soldierBackG == null) {
            Color c0 = new Color(255,255,255,128);
            Color c1 = new Color(255,255,255,0);
            Point2D start = new Point2D.Float(0, barY);
            Point2D end = new Point2D.Float(getWidth(), barY);
            float[] dist = {0.0f, 0.4f, 0.6f, 1.0f};
            Color[] colors = {c0, c1, c1, c0 };
            soldierBackG = new LinearGradientPaint(start, end, dist, colors);
        }
        g.setPaint(soldierBackG);
        g.fillRect(0, barY, getWidth(), s80);

        g.dispose();
    }
    private void allStopFiring() {
        for (int i=0;i<attackerState.length;i++) {
            if (attackerState[i] < NOT_FIRING)
                attackerState[i] = ATTACKER_END_DYING;
            else
                attackerState[i] = NOT_FIRING;
        }
        for (int i=0;i<defenderState.length;i++) {
            if (defenderState[i] < NOT_FIRING)
                defenderState[i] = DEFENDER_END_DYING;
            else
                defenderState[i] = NOT_FIRING;
        }
    }
    private float startPct(int count) {
        if (count < 5)
            return 1.0f;
        else if (count < 20)
            return 0.4f;
        else
            return 0.1f;
    }
    private void allStartFiring(int attackerCount, int defenderCount) {
        float attackerStartPct = startPct(attackerCount);
        float defenderStartPct = startPct(defenderCount);
        // int attackFrames = attackerFrames.size();

        for (int i=0;i<attackerCount;i++) {
            // int currState= attackerState[i];
            if (attackerState[i] == NOT_FIRING) {
                if (random() < attackerStartPct)
                    attackerState[i] = BEGIN_FIRING;
            }
            else if (attackerState[i] <= BEGIN_DYING) {
                if (attackerState[i] > ATTACKER_END_DYING)
                    attackerState[i]--;
            }
            else if (attackerState[i] >= BEGIN_FIRING) {
                attackerState[i]++;
                if (attackerState[i] >= ATTACKER_END_FIRING)
                    attackerState[i] = NOT_FIRING;
            }
            //System.out.println("Attacker "+i+": from "+currState+" to "+attackerState[i]);
        }
        for (int i=0;i<defenderCount;i++) {
            // int currState= defenderState[i];
            if (defenderState[i] == NOT_FIRING) {
                if (random() < defenderStartPct)
                    defenderState[i] = BEGIN_FIRING;
            }
            else if (defenderState[i] <= BEGIN_DYING) {
                if (defenderState[i] > DEFENDER_END_DYING)
                    defenderState[i]--;
            }
            else if (defenderState[i] >= BEGIN_FIRING) {
                defenderState[i]++;
                if (defenderState[i] >= DEFENDER_END_FIRING)
                    defenderState[i] = NOT_FIRING;
            }
            //System.out.println("Defender "+i+": from "+currState+" to "+defenderState[i]);
        }
    }
    @Override
    public String ambienceSoundKey() { return "GroundCombatAmbience"; }
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        switch(k) {
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_SPACE:
                advanceScreen();
                return;
            case KeyEvent.VK_L:
            	if (e.isAltDown()) {
            		debugReloadLabels(this);
            		return;
            	}
        }
        repaint();
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintCombatScene(screenBuffer());
        g.drawImage(screenBuffer(),0,0,null);
    }
    private void paintCombatScene(Image img) {
        Graphics2D g = (Graphics2D) img.getGraphics();
        setFontHints(g);
        int w = getWidth();
        int h = getHeight();
        Color detailLineC = Color.white;

        attackerFired =false;
        defenderFired = false;

        // modnar: use (slightly) better upsampling
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(landscapeImg, 0, 0, w, h, null);

        int textY = h-s100;

        // draw ships (one ship per 20 transports)
        int numShips = bounds(1,(totalAttackers/20)+1, ships.length);
        for (int i=numShips-1;i>=0;i--)
            ships[i].draw(g);

        // draw defenders
        ColonyDefense defense = colony.defense();
        int x0 = (w/2)+s40;
        int y0 = textY;
		// modnar: use Math.min(MAX_SOLDIERS,totalDefenders) to draw defenders
		// avoid bug with more than 500 troops
        drawTroops(g, Math.min(MAX_SOLDIERS,totalDefenders), false, x0, y0-s40);

        if (!landing()) {
            String attArmorDesc = text("INVASION_TROOP_ARMOR_DESC", transport.armorDesc(), transport.battleSuitDesc());
            String attShieldDesc = transport.shieldDesc();
            String attWpnDesc = transport.weaponDesc();
            String attLine = concat(attArmorDesc, attShieldDesc, attWpnDesc);
            g.setFont(narrowFont(28));
            // draw attacker15s
            x0 = s40;
            y0 = textY;
			// modnar: use Math.min(MAX_SOLDIERS,totalAttackers) to draw defenders
			// avoid bug with more than 500 attackers
            drawTroops(g, Math.min(MAX_SOLDIERS,totalAttackers), true, x0, y0-s40);
            // draw attacker info
            g.setFont(narrowFont(40));
            String str1 = text("INVASION_ATTACKERS_TITLE", str(transport.size()));
            str1 = transport.empire().replaceTokens(str1, "attacker");
            drawBorderedString(g, str1, 2, x0, y0, Color.black, Color.white);
            scaledFont(g, attLine, w/2-s60, 22, 18);
            //g.setFont(narrowFont(22));
            y0 += s30;
            if (!attArmorDesc.isEmpty()) {
                int sw = g.getFontMetrics().stringWidth(attArmorDesc)+s10;
                drawBorderedString(g,attArmorDesc,2,x0,y0,Color.black, detailLineC);
                x0 += sw;
            }
            if (!attShieldDesc.isEmpty()) {
                int sw = g.getFontMetrics().stringWidth(attShieldDesc)+s10;
                drawBorderedString(g,attShieldDesc,2,x0,y0,Color.black, detailLineC);
                x0 += sw;
            }
            if (!attWpnDesc.isEmpty()) {
                drawBorderedString(g,attWpnDesc,2,x0,y0,Color.black, detailLineC);
            }
        }

        // draw defender info
        g.setFont(narrowFont(40));
        x0 = w-s40;
        y0 = textY;
        String str2;
        if (defenderEmp == attackerEmp)
            str2 = text("INVASION_REBELS_TITLE", str(defense.troops()));
        else
            str2 = text("INVASION_DEFENDERS_TITLE", str(defense.troops()));
        str2 = defenderEmp.replaceTokens(str2, "defender");
        int sw2 = g.getFontMetrics().stringWidth(str2);
        drawBorderedString(g, str2, 2, x0-sw2, textY, Color.black, Color.white);
        String defArmorDesc = text("INVASION_TROOP_ARMOR_DESC", defense.armorDesc(), defense.battleSuitDesc());
        String defShieldDesc = defense.personalShieldDesc();
        String defWpnDesc = defense.weaponDesc();
        String defLine = concat(defArmorDesc, defShieldDesc, defWpnDesc);
        scaledFont(g, defLine, w/2-s60, 22, 18);
        //g.setFont(narrowFont(22));
        y0 += s30;
        x0 = w-s30;
        if (!defWpnDesc.isEmpty()) {
            int sw = g.getFontMetrics().stringWidth(defWpnDesc)+s10;
            x0 -= sw;
            drawBorderedString(g,defWpnDesc,2,x0,y0,Color.black, detailLineC);
        }
        if (!defShieldDesc.isEmpty()) {
            int sw = g.getFontMetrics().stringWidth(defShieldDesc)+s10;
            x0 -= sw;
            drawBorderedString(g,defShieldDesc,2,x0,y0,Color.black, detailLineC);
        }
        if (!defArmorDesc.isEmpty()) {
            int sw = g.getFontMetrics().stringWidth(defArmorDesc)+s10;
            x0 -= sw;
            drawBorderedString(g,defArmorDesc,2,x0,y0,Color.black, detailLineC);
        }

        // BR: Update the title
        if (!battleInProgress())
        	updateEndOfCombatTitle();
        
        // draw title last (so it overlays any ship)
        int titleLineH = s40;
        g.setFont(narrowFont(40));
        List<String> lines = wrappedLines(g, title, w*4/5);
        y0 = s10;
        for (String line: lines) {
            y0 += titleLineH;
            int sw = g.getFontMetrics().stringWidth(line);
            x0 = (w-sw)/2;
            drawBorderedString(g, line, 2, x0, y0, Color.black, Color.yellow);
        }
        // draw subtitle
        g.setFont(narrowFont(24));
        int sw = g.getFontMetrics().stringWidth(subtitle);
        x0 = (w-sw)/2;
        y0 += s30;
        drawBorderedString(g, subtitle, 1, x0, y0, Color.black, Color.yellow);
        
        drawSkipText(g, !battleInProgress());

        g.dispose();
    }
    @Override
    public void animate() {
        if (exited)
            return;
        landingCount++;
        if (!landing()) {
            if (battleInProgress()) {
                allStartFiring(Math.min(MAX_SOLDIERS,totalAttackers), Math.min(MAX_SOLDIERS,totalDefenders));
                if (animationCount() % 3 == 0) {
                    attackerDead = colony.singleCombatAgainstTransports(transport);
                    deathThisTurn = assignRandomVictim(attackerDead);
                    if (!battleInProgress())
                        allStopFiring();
                }
            }
            else
            	return;
        }
        repaint();
    }
    private int assignRandomVictim(boolean attacker) {
        Integer deadIndex;
        if (attacker) {
            // kill off attackers with index higher than 500 first
            if (remainingAttackers.size() >= MAX_SOLDIERS) 
                deadIndex = remainingAttackers.size() - 1;
            else 
                deadIndex = random(remainingAttackers);
            if (deadIndex == null)
            	return -1;
            if (deadIndex < attackerState.length)
                attackerState[deadIndex] = BEGIN_DYING;
            remainingAttackers.remove(deadIndex);
        }
        else {
            // kill off attackers with index higher than 500 first
            if (remainingDefenders.size() >= MAX_SOLDIERS) 
                deadIndex = remainingDefenders.size() - 1;
            else 
                deadIndex = random(remainingDefenders);
            if (deadIndex == null)
            	return -1;
            if (deadIndex < defenderState.length)
                defenderState[deadIndex] = BEGIN_DYING;
            remainingDefenders.remove(deadIndex);
        }
        return deadIndex;
    }
    private boolean landing() {
        for (LandingShip ship: ships) {
            if (ship.landing())
                return true;
        }
        return false;
    }
    private boolean battleInProgress() {
        return colony.defense().troops() > 0 && transport.size() > 0;
    }
	private void updateEndOfCombatTitle()	{
		if (transport.size() == 0) {
			if (defenderEmp == attackerEmp)	// Rebels
				title = text("INVASION_LOSS_REBEL", defenderEmp.raceName(), sysName);
			else 
				title = text("INVASION_LOSS", defenderEmp.raceName(), sysName);
			title = defenderEmp.replaceTokens(title, "defender");
		}
		else {
			if (defenderEmp == attackerEmp)	// Rebels
				title = text("INVASION_WIN_REBEL", attackerEmp.raceName(), sysName);
			else 
				title = text("INVASION_WIN", attackerEmp.raceName(), sysName);
			title = attackerEmp.replaceTokens(title, "attacker");
		}
	}
    private void advanceScreen() {
        if (landing()) {
            if (shipLanding != null)
                shipLanding.endPlaying();
            for (LandingShip ship: ships)
                ship.forceLand();
        }
        else if (battleInProgress()) {
            colony.completeDefenseAgainstTransports(transport);
            int defendersKilled = remainingDefenders.size() - colony.defense().troops();
            int attackersKilled = remainingAttackers.size() - transport.size();
            for (int i=0;i<attackersKilled;i++)
                assignRandomVictim(true);
            for (int i=0;i<defendersKilled;i++)
                assignRandomVictim(false);
            allStopFiring();
            log("ending with defenders:"+totalDefenders);
            repaint();
        }
        else {
        	updateEndOfCombatTitle();
            exited = true;
            repaint();
            session().resumeNextTurnProcessing();
        }
    }
    private void drawTroops(Graphics2D g, int troopCount, boolean attack, int x, int y) {
//        log("DrawTroops. count:"+troopCount+" attacker?"+attack+"  x:"+x+"  y:"+y);
    	if (troopCount<=0)
    		return;
        int MAX_PER_ROW = 25;
        int  NUM_ROWS = 6;
        int MAX_ROWS = 20;
        // try to do 6 rows evenly, only go to more when more than 6*MAX_PER_ROW
        int PER_ROW = (int) Math.ceil(1.0* troopCount / NUM_ROWS);
        while ((PER_ROW > MAX_PER_ROW) && (NUM_ROWS < MAX_ROWS)) {
            NUM_ROWS++;
            PER_ROW = (int) Math.ceil(1.0*troopCount / NUM_ROWS);
        }
        BufferedImage iconImg;
        int imgW = attack ? attackerImgW : defenderImgW;
        int imgH = attack ? attackerImgH : defenderImgH;
        int rowH = attack ? attackerYSpacing : defenderYSpacing;
        int iconWSpacing = attack ? attackerXSpacing : defenderXSpacing;

        int iconWIncr = attack ? iconWSpacing : -iconWSpacing;
        int rowStartX = attack ? x : x+(PER_ROW*iconWSpacing)-iconWSpacing;
        int numRows = (troopCount + PER_ROW - 1) / PER_ROW;
        int y0 = y - (4 * rowH);

        //log("troopSize:"+troopSize+"  w: "+imgW+" h:"+imgH+"   nuwRows:"+numRows);
        int count = 0;
        for (int row=0; row<numRows; row++) {
            int rowCount = Math.min(PER_ROW, troopCount);
            int rowAdj = row % 2 == 0 ? s8 : 0;
            int x0 = rowStartX+rowAdj;
            for (int troop=0; troop<rowCount; troop++) {
                int yAdj = troop % 2 == 0 ? s10 : 0;
                if (attack) {
                    int frame = attackerState[count];   // ATTACKER_END_DYING... 0... ATTACKER_END_FIRING
                    if (frame > BEGIN_DYING) 
                        iconImg = frame < attackerFrames.size() ? attackerFrames.get(frame) : null;
                    else {
                        int deathFrame = 0-frame-1;
                        iconImg = deathFrame < attackerDeathFrames.size() ? attackerDeathFrames.get(0-frame-1) : null;
                    }

                    attackerX[count] = x0;
                    attackerY[count] = y0+yAdj;
                    //log("attacker #"+count+" frame:"+frame+"  x:"+x0+"  y:"+(y0+yAdj)+"  w:"+imgW+"  h:"+imgH);
					// modnar: use (slightly) better downsampling
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.drawImage(iconImg, x0, y0+yAdj, imgW, imgH, null);
                    if (frame == attackerFiringFrame)
                        fireAtDefender(g, count);
                }
                else {
                    int frame = defenderState[count];  // DEFENDER_END_DYING... 0... DEFENDER_END_FIRING
                    if (frame > BEGIN_DYING)
                        iconImg = frame < defenderFrames.size() ? defenderFrames.get(frame) : null;
                    else {
                        int deathFrame = 0-frame-1;
                        iconImg = deathFrame < defenderDeathFrames.size() ? defenderDeathFrames.get(0-frame-1) : null;
                    } 

                    defenderX[count] = x0;
                    defenderY[count] = y0+yAdj;
                    //log("defender #"+count+" frame:"+frame+"  x:"+x0+"  y:"+(y0+yAdj)+"  w:"+imgW+"  h:"+imgH);
					// modnar: use (slightly) better downsampling
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g.drawImage(iconImg, x0, y0+yAdj, imgW, imgH, null);
                    if (frame == defenderFiringFrame)
                        fireAtAttacker(g, count);
                }
                x0 += iconWIncr;
                count++;
            }
            troopCount -= rowCount;
            y0 += rowH;
        }
    }
    private int fireAtDefender(Graphics2D g, int n) {
        attackerFired = true;
        int gunX = attackerX[n]+attackerGunX;
        int gunY = attackerY[n]+attackerGunY;

        // if defender dies this turn, ensure that
        // predecided victim is fired on at least once
        Integer victim;
        if (!attackerDead && deathThisTurn > 0) {
            victim = deathThisTurn;
            deathThisTurn = 0;
        }
        else {
            victim = random(remainingDefenders);
	        if (victim == null) {
	            victim = deathThisTurn;
	            deathThisTurn = 0;
	        }
        }

        int victimX = defenderX[0]-(defenderImgW/2);
        int victimY = defenderY[0]+(defenderImgH/2);

        // modnar: adjust firing to prevent >500 troop bug
        if (victim >= MAX_SOLDIERS) {
            victimX = defenderX[MAX_SOLDIERS-1]-(defenderImgW/2);
            victimY = defenderY[MAX_SOLDIERS-1]+(defenderImgH/2);
        }
        else {
            victimX = defenderX[victim]-(defenderImgW/2);
            victimY = defenderY[victim]+(defenderImgH/2);
        }

        //log("firing at defender#"+victim+"  from:"+gunX+"@"+gunY+"  to:"+victimX+"@"+victimY+"  gun:"+attackerGunX+"@"+attackerGunY);
        attackerWeapon.drawEffect(g, gunX, gunY, victimX, victimY);
        return victim;
    }
    private int fireAtAttacker(Graphics2D g, int n) {
        defenderFired = true;
        // defender images flipped horizontally, calc GunX from right side
        int gunX = defenderX[n]-defenderGunX;
        int gunY = defenderY[n]+defenderGunY;

        // if attacker dies this turn, ensure that
        // predetermined victim is fired on at least once
        Integer victim;
        if (attackerDead && deathThisTurn > 0) {
            victim = deathThisTurn;
            deathThisTurn = 0;
        }
        else {
        	victim = random(remainingAttackers);
        	if (victim == null) {
	            victim = deathThisTurn;
	            deathThisTurn = 0;
        	}
        }

        int victimX = attackerX[0]+(attackerImgW/2);
        int victimY = attackerY[0]+(attackerImgH/2);
		
		// modnar: adjust firing to prevent >500 troop bug
		if (victim >= MAX_SOLDIERS) {
			victimX = attackerX[MAX_SOLDIERS-1]+(attackerImgW/2);
			victimY = attackerY[MAX_SOLDIERS-1]+(attackerImgH/2);
		}
		else {
			victimX = attackerX[victim]+(attackerImgW/2);
			victimY = attackerY[victim]+(attackerImgH/2);
		}

        //log("firing at attacker#"+victim+"  from:"+gunX+"@"+gunY+"  to:"+victimX+"@"+victimY+"  gun:"+defenderGunX+"@"+defenderGunY);
        defenderWeapon.drawEffect(g, gunX, gunY, victimX, victimY);
        return victim;
    }
    @Override
    public void mouseClicked(MouseEvent e) { }
    @Override
    public void mouseEntered(MouseEvent e) { }
    @Override
    public void mouseExited(MouseEvent e) { }
    @Override
    public void mousePressed(MouseEvent e) { }
    @Override
    public void mouseReleased(MouseEvent e) {
        if ((e.getButton() > 3) || e.getClickCount() > 1)
            return;
        advanceScreen();
    }
    private class LandingShip implements Base {
        int countDelay = 0;
        BufferedImage shipClosed;
        int shipX, shipEndY, dispW, dispH;
        int descIndex = 0;
        int openIndex = 0;
        int numLandingFrames = 10;
        int dropPerFrame = 1;
        int endTopY = 0;
        int startY = 0;

        public LandingShip(int n) {
			Empire emp = transport.empire();
			numLandingFrames = emp.transportLandingFrames() / 5;
			shipClosed = newBufferedImage(emp.transportDescending());

            shipX = scaled(colony.planet().type().shipX(n+1));
            shipEndY = scaled(colony.planet().type().shipY(n+1));
            dispW = scaled(colony.planet().type().shipW(n+1));
            dispH = shipClosed.getHeight()*dispW/shipClosed.getWidth();

            dropPerFrame = (int) Math.ceil(1.0*shipEndY/numLandingFrames);
            endTopY = shipEndY-dispH;
            startY = endTopY-(dropPerFrame*(numLandingFrames+countDelay));
            //log("startY:"+startY+"  endTopY:"+endTopY+"  numFrames:"+numLandingFrames+"  dropPerFrame:"+dropPerFrame);
            countDelay = roll(0,20);
        }
        public void forceLand() {
            landingCount = max(landingCount, (numLandingFrames+countDelay));
        }
        public boolean landing() {
            return landingCount < (numLandingFrames+countDelay);
        }
        public void draw(Graphics g) {
            int thisY = min(endTopY, startY+(landingCount*dropPerFrame));
            // draw landing ship
            Image shipImg = thisY == endTopY ? nextOpeningShipImage() : nextDescendingShipImage();
            if (shipImg == null)
            	return;
            int imgW = shipImg.getWidth(null);
            int imgH = shipImg.getHeight(null);
            g.drawImage(shipImg, shipX, thisY, shipX+dispW, thisY+dispH, 0, 0, imgW, imgH, null);
        }
        private Image nextDescendingShipImage() {
            int frame = descIndex++;
            for (int i=0;i<descendingFrameRefs.size();i++) {
                if (frame < descendingFrameRefs.get(i))
                    return descendingFrames.get(i);
                frame -= descendingFrameRefs.get(i);
            }      
            if(descendingFrames.size() > 0)
                return descendingFrames.get(descendingFrames.size()-1);
            return null;
        }
        private Image nextOpeningShipImage() {
            int frame = openIndex++;
            for (int i=0;i<openingFrameRefs.size();i++) {
                if (frame < openingFrameRefs.get(i))
                    return openingFrames.get(i);
                frame -= openingFrameRefs.get(i);
            }      
            return openingFrames.get(openingFrames.size()-1);
        }
    }
}
