/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General License, Version 3 (the "License");
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
package rotp.model.empires.species;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rotp.model.empires.RaceCombatAnimation;
import rotp.util.ImageTransformer;

class Race extends SpeciesSkills {
	private static final long serialVersionUID = 1L;
	private static final List<String> notTalking = new ArrayList<>(Arrays.asList("Mouth"));
	private static final List<String> closed	 = new ArrayList<>(Arrays.asList("Open"));
	private static final List<String> open		 = new ArrayList<>(Arrays.asList("Closed"));
	static final List<String> notFiring	 = new ArrayList<>(Arrays.asList("Firing"));

	String id;
	private String laboratoryKey, embassyKey, councilKey;
	private String holographKey;
	private String diplomatKey;
	private String scientistKey;
	private String soldierKey;
	private String spyFaceKey;
	private String leaderKey;
	private String soldierFaceKey;
	private String mugshotKey;
	private String wideMugshotKey;
	private String setupImageKey;
	private String advisorFaceKey;
	private String advisorScoutKey;
	private String advisorTransportKey;
	private String advisorDiplomacyKey;
	private String advisorShipKey;
	private String advisorRallyKey;
	private String advisorMissileKey;
	private String advisorWeaponKey;
	private String advisorCouncilKey;
	private String advisorRebellionKey;
	private String advisorResistCouncilKey;
	private String advisorCouncilResistedKey;
	private String diplomacyTheme;
	private String spyKey;
	private String gnnKey;
	private String gnnHostKey;
	private String gnnColor;
	private Color gnnTextColor;
	private String transportKey;
	private String transportDescKey;
	private String transportOpenKey;
	private int transportDescFrames, transportOpenFrames;
	private String shipAudioKey;
	private RaceCombatAnimation troopNormal = new RaceCombatAnimation();
	private RaceCombatAnimation troopHostile = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath1 = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath2 = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath3 = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath4 = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath1H = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath2H = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath3H = new RaceCombatAnimation();
	private RaceCombatAnimation troopDeath4H = new RaceCombatAnimation();
	private List<String> fortressKeys = new ArrayList<>();
	private String shieldKey;
	private String voiceKey;
	private String ambienceKey;
	private String flagWarKey, flagNormKey, flagPactKey;
	private String dlgWarKey, dlgNormKey,dlgPactKey;
	private String winSplashKey, lossSplashKey;
	private Color winTextC, lossTextC;
	private ImageTransformer diplomacyTransformer; // BR: Never Used!
	private final List<String> soundKeys = new ArrayList<>();
	private int espionageX, espionageY;
	private int transportW, transportYOffset, transportLandingFrames, colonistWalkingFrames;
	private int colonistDelay, colonistX1, colonistX2, colonistY1, colonistY2;
	private int dialogLeftMargin, dialogRightMargin,  dialogTopY;
	private float diploScale, diploOpacity;
	private int diploXOffset, diploYOffset;
	private int flagW, flagH;

	private transient BufferedImage transportClosedImg;
	private transient Image transportImg;
	private transient BufferedImage diploMug, wideDiploMug;

	@Override String id()				{ return id; }
	@Override void id(String s)			{ super.id(s); id = s; }
	int colonistDelay()					{ return colonistDelay; }
	int colonistStartX()				{ return colonistX1; }
	int colonistStartY()				{ return colonistY1; }
	int colonistStopX()					{ return colonistX2; }
	int colonistStopY()					{ return colonistY2; }
	int dialogLeftMargin()				{ return dialogLeftMargin; }
	void dialogLeftMargin(int i)		{ dialogLeftMargin = i; }
	int dialogRightMargin()				{ return dialogRightMargin; }
	void dialogRightMargin(int i)		{ dialogRightMargin = i; }
	int dialogTopY()					{ return dialogTopY; }
	void dialogTopY(int i)				{ dialogTopY = i; }
	int colonistWalkingFrames()			{ return colonistWalkingFrames; }
	void colonistWalkingFrames(int i)	{ colonistWalkingFrames = i; }
	int transportLandingFrames()		{ return transportLandingFrames; }
	void transportLandingFrames(int i)	{ transportLandingFrames = i; }
	int transportDescFrames()			{ return transportDescFrames; }
	void transportDescFrames(int i)		{ transportDescFrames = i; }
	int transportOpenFrames()			{ return transportOpenFrames; }
	void transportOpenFrames(int i)		{ transportOpenFrames = i; }
	int transportYOffset()				{ return transportYOffset; }
	void transportYOffset(int i)		{ transportYOffset = i; }
	int transportW()					{ return transportW; }
	void transportW(int i)				{ transportW = i; }
	int flagW()							{ return flagW; }
	void flagW(int i)					{ flagW = i; }
	int flagH()							{ return flagH; }
	void flagH(int i)					{ flagH = i; }
	int diploXOffset()					{ return diploXOffset; }
	void diploXOffset(int i)			{ diploXOffset = i; }
	int diploYOffset()					{ return diploYOffset; }
	void diploYOffset(int i)			{ diploYOffset = i; }
	float diploScale()					{ return diploScale; }
	void diploScale(float f)			{ diploScale = f; }
	float diploOpacity()				{ return diploOpacity; }
	void diploOpacity(float f)			{ diploOpacity = f; }

	Color gnnTextColor()				{ return gnnTextColor; }
	void gnnTextColor(Color c)			{ gnnTextColor = c; }

	String lossSplashKey()				{ return lossSplashKey; }
	void lossSplashKey(String s)		{ lossSplashKey = s; }
	String winSplashKey()				{ return winSplashKey; }
	void winSplashKey(String s)			{ winSplashKey = s; }
	String shipAudioKey()				{ return shipAudioKey; }
	void shipAudioKey(String s)			{ shipAudioKey = s; }
	String ambienceKey()				{ return ambienceKey; }
	void ambienceKey(String s)			{ ambienceKey = s; }
	String transportDescKey()			{ return transportDescKey; }
	void transportDescKey(String s)		{ transportDescKey = s; }
	String transportOpenKey()			{ return transportOpenKey; }
	void transportOpenKey(String s)		{ transportOpenKey = s; }
	void mugshotKey(String s)			{ mugshotKey = s; }
	void wideMugshotKey(String s)		{ wideMugshotKey = s; }
	void setupImageKey(String s)		{ setupImageKey = s; }
	void spyFaceKey(String s)			{ spyFaceKey = s; }
	void soldierFaceKey(String s)		{ soldierFaceKey = s; }
	void advisorFaceKey(String s)		{ advisorFaceKey = s; }
	void advisorScoutKey(String s)		{ advisorScoutKey = s; }
	void advisorTransportKey(String s)	{ advisorTransportKey = s; }
	void advisorDiplomacyKey(String s)	{ advisorDiplomacyKey = s; }
	void advisorShipKey(String s)		{ advisorShipKey = s; }
	void advisorRallyKey(String s)		{ advisorRallyKey = s; }
	void advisorMissileKey(String s)	{ advisorMissileKey = s; }
	void advisorWeaponKey(String s)		{ advisorWeaponKey = s; }
	void advisorCouncilKey(String s)	{ advisorCouncilKey = s; }
	void advisorRebellionKey(String s)	{ advisorRebellionKey = s; }
	void advisorResistCouncilKey(String s)	{ advisorResistCouncilKey = s; }
	void advisorCouncilResistedKey(String s)	{ advisorCouncilResistedKey = s; }
	void councilKey(String s)			{ councilKey = s; }
	void laboratoryKey(String s)		{ laboratoryKey = s; }
	void embassyKey(String s)			{ embassyKey = s; }
	void holographKey(String s)			{ holographKey = s; }
	void diplomatKey(String s)			{ diplomatKey = s; }
	void scientistKey(String s)			{ scientistKey = s; }
	void soldierKey(String s)			{ soldierKey = s; }
	void spyKey(String s)				{ spyKey = s; }
	void leaderKey(String s)			{ leaderKey = s; }
	void gnnKey(String s)				{ gnnKey = s; }
	void gnnHostKey(String s)			{ gnnHostKey = s; }
	void gnnColor(String s)				{ gnnColor = s; }
	void flagWarKey(String s)			{ flagWarKey = s; }
	void flagNormKey(String s)			{ flagNormKey = s; }
	void flagPactKey(String s)			{ flagPactKey = s; }
	void dlgWarKey(String s)			{ dlgWarKey = s; }
	void dlgNormKey(String s)			{ dlgNormKey = s; }
	void dlgPactKey(String s)			{ dlgPactKey = s; }
	void transportKey(String s)			{ transportKey = s; }
	void shieldKey(String s)			{ shieldKey = s; }
	void voiceKey(String s)				{ voiceKey = s; }

	RaceCombatAnimation troopNormal()	{ return troopNormal; }
	RaceCombatAnimation troopHostile()	{ return troopHostile; }
	RaceCombatAnimation troopDeath1()	{ return troopDeath1; }
	RaceCombatAnimation troopDeath2()	{ return troopDeath2; }
	RaceCombatAnimation troopDeath3()	{ return troopDeath3; }
	RaceCombatAnimation troopDeath4()	{ return troopDeath4; }
	RaceCombatAnimation troopDeath1H()	{ return troopDeath1H; }
	RaceCombatAnimation troopDeath2H()	{ return troopDeath2H; }
	RaceCombatAnimation troopDeath3H()	{ return troopDeath3H; }
	RaceCombatAnimation troopDeath4H()	{ return troopDeath4H; }

	void troopNormal(RaceCombatAnimation a)		{ troopNormal = a; }
	void troopHostile(RaceCombatAnimation a)	{ troopHostile = a; }
	void troopDeath1(RaceCombatAnimation a)		{ troopDeath1 = a; }
	void troopDeath2(RaceCombatAnimation a)		{ troopDeath2 = a; }
	void troopDeath3(RaceCombatAnimation a)		{ troopDeath3 = a; }
	void troopDeath4(RaceCombatAnimation a)		{ troopDeath4 = a; }
	void troopDeath1H(RaceCombatAnimation a)	{ troopDeath1H = a; }
	void troopDeath2H(RaceCombatAnimation a)	{ troopDeath2H = a; }
	void troopDeath3H(RaceCombatAnimation a)	{ troopDeath3H = a; }
	void troopDeath4H(RaceCombatAnimation a)	{ troopDeath4H = a; }
	ImageTransformer diplomacyTransformer()			{ return diplomacyTransformer; }
	void diplomacyTransformer(ImageTransformer s)	{ diplomacyTransformer = s; }

	Race()	{ super(); }
	Race(String dirPath)	{ super(dirPath); }
	@Override public String toString()		{ return concat("Anim:", id); }
	String diplomacyTheme()					{ return diplomacyTheme; }
	void diplomacyTheme(String str)			{ diplomacyTheme = str; }

	void espionageXY(List<String> vals)	{
		espionageX = parseInt(vals.get(0));
		if (vals.size() > 1)
			espionageY = parseInt(vals.get(1));
	}
	Image flagWar()				{ return image(flagWarKey); }
	Image flagNorm()			{ return image(flagNormKey); }
	Image flagPact()			{ return image(flagPactKey); }
	Image dialogWar()			{ return image(dlgWarKey); }
	Image dialogNorm()			{ return image(dlgNormKey); }
	Image dialogPact()			{ return image(dlgPactKey); }
	Image council()				{ return image(councilKey);  }
	Image gnnEvent(String id)	{ return image(gnnEventKey(id)); }
	private String gnnEventKey(String id)	{ return concat(gnnColor,"_",id); }
	BufferedImage gnn()					{ return currentFrame(gnnKey); }
	BufferedImage gnnHost()				{ return currentFrame(gnnHostKey); }
	BufferedImage laboratory()			{ return currentFrame(laboratoryKey);  }
	BufferedImage embassy()				{ return currentFrame(embassyKey);  }
	BufferedImage holograph()			{ return currentFrame(holographKey);  }
	BufferedImage mugshot()				{ return currentFrame(mugshotKey);  }
	BufferedImage setupImage()			{ return currentFrame(setupImageKey);  }
	BufferedImage spyMugshotQuiet()		{ return currentFrame(spyFaceKey, notTalking);  }
	BufferedImage soldierMugshot()		{ return currentFrame(soldierFaceKey, notTalking);  }
	BufferedImage advisorMugshot()		{ return currentFrame(advisorFaceKey, notTalking); }
	BufferedImage advisorScout()		{ return currentFrame(advisorScoutKey, notTalking); }
	BufferedImage advisorTransport()	{ return currentFrame(advisorTransportKey, notTalking); }
	BufferedImage advisorDiplomacy()	{ return currentFrame(advisorDiplomacyKey, notTalking); }
	BufferedImage advisorShip() 		{ return currentFrame(advisorShipKey, notTalking); }
	BufferedImage advisorRally()		{ return currentFrame(advisorRallyKey, notTalking); }
	BufferedImage advisorMissile()		{ return currentFrame(advisorMissileKey, notTalking); }
	BufferedImage advisorWeapon()		{ return currentFrame(advisorWeaponKey, notTalking); }
	BufferedImage advisorCouncil()		{ return currentFrame(advisorCouncilKey, notTalking); }
	BufferedImage advisorRebellion()	{ return currentFrame(advisorRebellionKey, notTalking); }
	BufferedImage advisorResistCouncil()	{ return currentFrame(advisorResistCouncilKey, notTalking); }
	BufferedImage advisorCouncilResisted()	{ return currentFrame(advisorCouncilResistedKey, notTalking); }
	BufferedImage diplomatTalking()		{ return currentFrame(diplomatKey);  }
	BufferedImage scientistTalking()	{ return currentFrame(scientistKey);  }
	BufferedImage soldierTalking()		{ return currentFrame(soldierKey);  }
	BufferedImage spyTalking()			{ return currentFrame(spyKey);  }
	BufferedImage diploMugshotQuiet()	{ return currentFrame(mugshotKey, notTalking);  }
	BufferedImage diploWideMugshot()	{ return currentFrame(wideMugshotKey, notTalking);  }
	BufferedImage diplomatQuiet()		{ return currentFrame(diplomatKey, notTalking);  }
	BufferedImage scientistQuiet()		{ return currentFrame(scientistKey, notTalking);  }
	BufferedImage soldierQuiet()		{ return currentFrame(soldierKey, notTalking);  }
	BufferedImage spyQuiet()			{ return currentFrame(spyKey, notTalking);  }
	BufferedImage councilLeader()		{ return asBufferedImage(image(leaderKey));  }
	BufferedImage diploMug()	{
		if (diploMug == null)
			diploMug = newBufferedImage(diploMugshotQuiet());
		return diploMug;
	}
	BufferedImage wideDiploMug()	{
		if (wideDiploMug == null)
			wideDiploMug = newBufferedImage(diploWideMugshot());
		return wideDiploMug;
	}
	Image transport()	{
		if (transportImg == null)
			transportImg = image(transportKey);
		return transportImg;
	}
	BufferedImage transportDescending()	{
		if (transportClosedImg == null)
			transportClosedImg = currentFrame(transportDescKey, closed);
		return transportClosedImg;
	}
	BufferedImage transportOpening()	{ return currentFrame(transportDescKey, open); }
	BufferedImage fortress(int i)		{ return currentFrame(fortressKeys.get(i)); }
	int randomFortress()			{ return roll(1,fortressKeys.size())-1; }
	BufferedImage shield()			{ return currentFrame(shieldKey); }
	void resetMugshot()		{ resetAnimation(mugshotKey); }
	void resetSetupImage()	{ resetAnimation(setupImageKey); }
	void resetDiplomat()			{ resetAnimation(diplomatKey); }
	void resetScientist()			{ resetAnimation(scientistKey); }
	void resetSoldier()				{ resetAnimation(soldierKey); }
	void resetSpy()					{ resetAnimation(spyKey); }
	void resetGNN(String id)		{
		resetAnimation(gnnKey);
		resetAnimation(gnnHostKey);
		resetAnimation(gnnEventKey(id));
	}
	void addSoundKey(String s)	{ soundKeys.add(s); }
	void colonistWalk(String s)	{
		List<String> vals = substrings(s, ',');
		if (vals.size() != 3)
			err("Invalid colonistWalk string: ", s);

		// The string argument represents the pixel offset from the
		// top-left of the transport ship for the colonist to walk
		// from and then to before planting his flag
		List<String> points = substrings(vals.get(2), '>');
		if (points.size() != 2)
			err("Invalid colonistWalk string: ", s);

		List<String> fromXY = substrings(points.get(0),'@');
		if (fromXY.size() != 2)
			err("Invalid from point in colonistWalk string:", s);

		List<String> toXY = substrings(points.get(1),'@');
		if (toXY.size() != 2)
			err("Invalid to point in colonistWalk string:", s);

		colonistDelay = parseInt(vals.get(0));
		colonistWalkingFrames(parseInt(vals.get(1)));
		colonistX1 = parseInt(fromXY.get(0));
		colonistY1 = parseInt(fromXY.get(1));
		colonistX2 = parseInt(toXY.get(0));
		colonistY2 = parseInt(toXY.get(1));
	}
	void parseFortress(String s)	{
		//  f1|f2|f3|f4, spec
		//   reconstructs as this list:
		//  f1,spec
		//  f2,spec
		//  f3,spec
		//  f4,spec
		List<String> vals = substrings(s, ',');
		if (vals.size() != 2)
			err("Invalid fortress string: ", s);
		List<String> forts = substrings(vals.get(0), '|');

		for (String fort: forts) {
			String fortKey = concat(fort, ",", vals.get(1));
			fortressKeys.add(fortKey);
		}
	}
	void flagSize(String s)	{
		List<String> vals = substrings(s, 'x');
		if (vals.size() != 2)
		    err("Invalid FlagSize string: ", s);

		flagW(parseInt(vals.get(0)));
		flagH(parseInt(vals.get(1)));
	}
	void parseTransportDesc(String s)	{
		List<String> vals = substrings(s, ',');
		if (vals.size() != 3)
			err("Invalid TransportDesc string: ", s);

		transportDescKey(concat(vals.get(0), ",", vals.get(2)));
		transportDescFrames(parseInt(vals.get(1)));
	}
	void parseTransportOpen(String s)	{
		List<String> vals = substrings(s, ',');
		if (vals.size() != 3)
			err("Invalid Transport Open string: ", s);

		transportOpenKey(concat(vals.get(0), ",", vals.get(2)));
		transportOpenFrames (parseInt(vals.get(1)));
	}
	void parseWinSplash(String s)	{
		List<String> vals = substrings(s, ',');
		if (vals.size() != 4)
			err("Invalid Win Splash string: ", s);

		winSplashKey(vals.get(0));
		int r = parseInt(vals.get(1));
		int g = parseInt(vals.get(2));
		int b = parseInt(vals.get(3));
		winTextC = new Color(r,g,b);
	}
	void parseLossSplash(String s)	{
		List<String> vals = substrings(s, ',');
		if (vals.size() != 4)
			err("Invalid Loss Splash string: ", s);

		lossSplashKey(vals.get(0).trim());
		int r = parseInt(vals.get(1).trim());
		int g = parseInt(vals.get(2).trim());
		int b = parseInt(vals.get(3).trim());
		lossTextC = new Color(r,g,b);
	}
	void parseCouncilDiplomatLocation(String s)	{
		List<String> vals = substrings(s, ',');
		if (vals.size() != 4)
			err("Invalid Council Diplomat location string: ", s);

		diploScale(parseFloat(vals.get(0).trim()));
		diploXOffset(parseInt(vals.get(1).trim()));
		diploYOffset(parseInt(vals.get(2).trim()));
		diploOpacity(parseFloat(vals.get(3).trim()));
	}
}
