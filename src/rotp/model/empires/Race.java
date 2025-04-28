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
package rotp.model.empires;

import static rotp.model.empires.CustomRaceDefinitions.getAlienRace;
import static rotp.model.empires.CustomRaceDefinitions.keyToRace;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import rotp.model.empires.Leader.Objective;
import rotp.model.empires.Leader.Personality;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.DynOptions;
import rotp.model.planet.PlanetType;
import rotp.model.ships.ShipDesign;
import rotp.util.Base;
import rotp.util.ImageTransformer;
import rotp.util.LabelManager;
import rotp.util.LanguageManager;

public class Race implements Base, Serializable {
    private static final long serialVersionUID = 1L;
	private static final String CUSTOM_RACE_DESCRIPTION	= "CUSTOM_RACE_DESCRIPTION";
	private static final String CUSTOM_SPECIES_FOLDER	= "CustomSpecies/";
	private static final String INTRO_FILE_EXTENSION	= ".intro.txt";
	public  static final String crEmpireNameRandom		= "Randomized";

    private static Map<String, Race> raceMap = new HashMap<>();
    public static boolean isValidKey(String s) {
    	return raceMap.get(s) != null;
    }
    public static Race keyed(String s) {
        Race race = raceMap.get(s);
        if (race == null) { // BR: Add custom race if missing
        	race = keyToRace(s);
            race.isCustomRace(true);
            race.description4 = race.text(CUSTOM_RACE_DESCRIPTION);
        }
        return race;
    }
    public static Race keyed(String s, DynOptions options) {
        Race race = raceMap.get(s);
        if (race == null) { // BR: get the custom race
       		race = getAlienRace(s, options);
            race.isCustomRace(true);
        }
        return race;
    }
    public static void addRace(Race r) { raceMap.put(r.id, r);}
    public static List<Race> races() {
        List<Race> races = new ArrayList<>();
        races.addAll(raceMap.values());
        return races;
    }

    public static final List<String> notTalking, closed, open, notFiring;

    static {
        notTalking = new ArrayList<>();
        notTalking.add("Mouth");
        open = new ArrayList<>();
        open.add("Closed");
        closed = new ArrayList<>();
        closed.add("Open");
        notFiring = new ArrayList<>();
        notFiring.add("Firing");
    }

    public String id;
    public String setupName; // BR: was never used
    public String empireTitle; // BR: for custom Races
    public String racePrefix = ""; // BR: for custom Races
    public String raceSuffix = ""; // BR: for custom Races
    public String leaderPrefix = ""; // BR: for custom Races
    public String leaderSuffix = ""; // BR: for custom Races
    public String worldsPrefix = ""; // BR: for custom Races
    public String worldsSuffix = ""; // BR: for custom Races
    public String langKey;
    private String description1, description2, description3, description4; // modnar: add desc4
    public String directoryName;
    public String laboratoryKey, embassyKey, councilKey;
    public String holographKey;
    public String diplomatKey;
    public String scientistKey;
    public String soldierKey;
    public String spyFaceKey;
    public String leaderKey;
    public String soldierFaceKey;
    public String mugshotKey;
    public String wideMugshotKey;
    public String setupImageKey;
    public String advisorFaceKey;
    public String advisorScoutKey;
    public String advisorTransportKey;
    public String advisorDiplomacyKey;
    public String advisorShipKey;
    public String advisorRallyKey;
    public String advisorMissileKey;
    public String advisorWeaponKey;
    public String advisorCouncilKey;
    public String advisorRebellionKey;
    public String advisorResistCouncilKey;
    public String advisorCouncilResistedKey;
    public String diplomacyTheme;
    public String spyKey;
    public String gnnKey;
    public String gnnHostKey;
    public String gnnColor;
    public Color gnnTextColor;
    public String transportKey;
    public String transportDescKey;
    public String transportOpenKey;
    public int transportDescFrames, transportOpenFrames;
    public String shipAudioKey;
    public RaceCombatAnimation troopNormal = new RaceCombatAnimation();
    public RaceCombatAnimation troopHostile = new RaceCombatAnimation();
    public RaceCombatAnimation troopDeath1 = new RaceCombatAnimation();
    public RaceCombatAnimation troopDeath2 = new RaceCombatAnimation();
    public RaceCombatAnimation troopDeath3 = new RaceCombatAnimation();
    public RaceCombatAnimation troopDeath4 = new RaceCombatAnimation();
    public RaceCombatAnimation troopDeath1H = new RaceCombatAnimation();
    public RaceCombatAnimation troopDeath2H = new RaceCombatAnimation();
    public RaceCombatAnimation troopDeath3H = new RaceCombatAnimation();
    public RaceCombatAnimation troopDeath4H = new RaceCombatAnimation();
    public List<String> fortressKeys = new ArrayList<>();
    public String shieldKey;
    public String voiceKey;
    public String ambienceKey;
    public String flagWarKey, flagNormKey, flagPactKey;
    public String dlgWarKey, dlgNormKey,dlgPactKey;
    public String winSplashKey, lossSplashKey;
    public Color winTextC, lossTextC;
    public ImageTransformer diplomacyTransformer; // BR: Never Used!
    public List<String> raceNames = new ArrayList<>();
    public List<String> homeSystemNames = new ArrayList<>();
    public List<String> leaderNames = new ArrayList<>();
    private final List<String> soundKeys = new ArrayList<>();
    public List<String> systemNames = new ArrayList<>();

    public List<String> shipNamesSmall = new ArrayList<>();
    public List<String> shipNamesMedium = new ArrayList<>();
    public List<String> shipNamesLarge = new ArrayList<>();
    public List<String> shipNamesHuge = new ArrayList<>();

    private final List<String> remainingRaceNames = new ArrayList<>();
    private final List<String> remainingHomeworldNames = new ArrayList<>();
    private final List<String> remainingLeaderNames = new ArrayList<>();
    private float defaultRaceRelations = 0;
    private final HashMap<String, Integer> raceRelations = new HashMap<>();
    private LabelManager labels;

    // BR: Settings that where encoded in "HomeworldKey"
    private float bCBonus = 0f;
    private float hPFactor = 1f;
    private float maintenanceFactor = 1f;
    private float shipSpaceFactor = 1f;
    private String planetArtifacts = "None";
    private String planetRessource = "Normal";
    private String planetEnvironment = "Normal";
    // Custom Races:
    private boolean isCustomRace = false;
    private DynOptions raceOptions	= null;
    // \BR:
    public int startingYear;
    public int speciesType;
    public String homeworldStarType;
    public String homeworldPlanetType;
    public int homeworldSize;
    public String preferredShipSet;
    public int preferredShipSize = 2;
    private int shipAttackBonus = 0;
    private int shipDefenseBonus = 0;
    private int shipInitiativeBonus = 0;
    private int groundAttackBonus = 0;
    public boolean telepathic = false;
    public float spyCostMod = 1;
    public float internalSecurityAdj = 0;
    public float spyInfiltrationAdj = 0;
    public float workerProductivityMod = 1;
    public int robotControlsAdj = 0;
    public float techDiscoveryPct = 0.5f;
    public float researchBonusPct = 1.0f;
    public float growthRateMod = 1;
    public float tradePctBonus = 0;
    public float positiveDPMod = 1;
    public int diplomacyBonus = 0;
    public float councilBonus = 0;
    public float[] techMod = new float[] { 1, 1, 1, 1, 1, 1 };
    public float[] discoveryMod = new float[] { 0, 0, 0, 0, 0, 0 }; // BR:
    public boolean ignoresPlanetEnvironment = false;
    public String acceptedPlanetEnvironment = "No";
    public boolean ignoresFactoryRefit = false;
    public boolean availablePlayer = true;  // BR: never used!
    public boolean availableAI = true;		// BR: Never used!
    public boolean masksDiplomacy = false;
    private float labFlagX = 0;
    public int espionageX, espionageY;
    private int spyFactoryFrames = 0;
    private int spyMissileFrames = 0;
    private int spyRebellionFrames = 0;
    private String title;
    private String fullTitle;
    private int homeworldKey;
    public int transportW, transportYOffset, transportLandingFrames, colonistWalkingFrames;
    private int colonistDelay, colonistX1, colonistX2, colonistY1, colonistY2;
    public int dialogLeftMargin, dialogRightMargin,  dialogTopY;
    public float diploScale, diploOpacity;
    public int diploXOffset, diploYOffset;
    public int introTextX, flagW, flagH;

    public float[] personalityPct = new float[Personality.values().length];
    public float[] objectivePct = new float[Objective.values().length];
    public float[] shipDesignMods = new float[28];

    private transient BufferedImage transportClosedImg;
    private transient Image transportImg;
    private transient BufferedImage diploMug, wideDiploMug;

    public String defaultHomeworldName()   {
    	if (homeSystemNames.isEmpty())
    		return "Empty";
    	return homeSystemNames.get(0);
    }

    public int colonistDelay()             { return colonistDelay; }
    public int colonistStartX()            { return colonistX1; }
    public int colonistStartY()            { return colonistY1; }
    public int colonistStopX()             { return colonistX2; }
    public int colonistStopY()             { return colonistY2; }
    public int dialogLeftMargin()          { return dialogLeftMargin; }
    public int dialogRightMargin()         { return dialogRightMargin; }
    public int dialogTopY()                { return dialogTopY; }

    public Race () {
        leaderNames.add("Leader");
        for (int i=0;i<personalityPct.length;i++)
            personalityPct[i] = 1;
        for (int i=0;i<objectivePct.length;i++)
            objectivePct[i] = 1;
    }

    public Race(String dirPath) {
        directoryName = dirPath;
        labels = new LabelManager();
    }
    // TODO BR: For race customization to be completed
    String empireTitle() {
        String s = "[this_empire]";
        String key = "this";
        List<String> tokens = varTokens(s, key);
        String s1 = s;
        for (String token: tokens) {
            String replString = concat("[",key, token,"]");
            List<String> values = substrings(text(token), ',');
            s1 = s1.replace(replString, values.get(0));
        }
        if (s.equalsIgnoreCase(s1))
        	return this.empireTitle();
        return s1;
    }
    // BR: for race customization
    // Get a Copy the current race
    protected Race copy() {
    	Race copy = RaceFactory.current().reloadRaceDataFile(directoryName);
    	labels.copy(labels, copy.labels);
    	copy.setupName	  = setupName();
    	copy.empireTitle  = empireTitle();
    	copy.description1 = description1;
    	copy.description2 = description2;
    	copy.description3 = description3;
    	copy.description4 = description4;
    	copy.raceNames.addAll(raceNames);
    	copy.homeSystemNames.addAll(homeSystemNames);
    	copy.leaderNames.addAll(leaderNames);
    	copy.shipNamesSmall.addAll(shipNamesSmall);
    	copy.shipNamesMedium.addAll(shipNamesMedium);
     	copy.shipNamesLarge.addAll(shipNamesLarge);
    	copy.shipNamesHuge.addAll(shipNamesHuge);
   	
    	// useless for abilities
    	copy.troopNormal  = null;
    	copy.troopHostile = null;
    	copy.troopDeath1  = null;
    	copy.troopDeath2  = null;
    	copy.troopDeath3  = null;
    	copy.troopDeath4  = null;
    	copy.troopDeath1H = null;
    	copy.troopDeath2H = null;
    	copy.troopDeath3H = null;
    	copy.troopDeath4H = null;
    	return copy;
    }
    public void loadNameList()  {
        List<String> secondaryNames =  new ArrayList<>(raceNames);
        remainingRaceNames.clear();
        remainingRaceNames.add(secondaryNames.remove(0));
        remainingRaceNames.addAll(secondaryNames);
    }
    public void loadLeaderList()  {
        List<String> secondaryNames =  new ArrayList<>(leaderNames);
        remainingLeaderNames.clear();
        remainingLeaderNames.add(secondaryNames.remove(0));
        Collections.shuffle(secondaryNames);
        remainingLeaderNames.addAll(secondaryNames);
    }
    public void loadHomeworldList() {
        List<String> homeNames =  new ArrayList<>(homeSystemNames);
        remainingHomeworldNames.clear();
        remainingHomeworldNames.add(homeNames.remove(0));
        Collections.shuffle(homeNames);
        remainingHomeworldNames.addAll(homeNames);
    }
    public String nextAvailableName() {
        if (remainingRaceNames.isEmpty()) 
            loadNameList();
        String name = remainingRaceNames.remove(0);
        return name;
    }
    public int nameIndex(String n) {
        return raceNames.indexOf(n);
    }
    public String nameVariant(int i)  { return raceNames.get(i); }
    public String nextAvailableLeader() {
        if (remainingLeaderNames.isEmpty())
            loadLeaderList();
        return remainingLeaderNames.remove(0);
    }
    public String nextAvailableHomeworld() {
        if (remainingHomeworldNames.isEmpty())
            loadHomeworldList();
        return remainingHomeworldNames.remove(0);
    }
    public LabelManager raceLabels()              { return labels; }
    @Override
    public String toString()                      { return concat("Race:", id); }

    @Override
    public String text(String key) {
        if (raceLabels().hasLabel(key))
            return raceLabels().label(key);

        String altKey = LanguageManager.swapToken(key);
    	if (altKey != null && raceLabels().hasLabel(altKey))
    		return raceLabels().label(altKey);

    	return labels().label(key);
    }
    @Override public String text(String key, String... vals) {
        String str = text(key);
        for (int i=0;i<vals.length;i++)
            str = str.replace(textSubs[i], vals[i]);
        return str;
    }

	public List<String> customIntroduction() {
		List<String> introLines = new ArrayList<>();
		if (isCustomRace) {
			log("loading Custom Species Intro");
			String filename = CUSTOM_SPECIES_FOLDER +  id + INTRO_FILE_EXTENSION;
			BufferedReader in = reader(filename);
			if (in != null) {
				try {
					String input;
					while ((input = in.readLine()) != null) {
						if (!isComment(input)) {
							introLines.add(input);
						}
					}
				}
				catch (IOException e) {}
				finally {
					try {
						in.close();
					} catch (IOException e) {}
				}
			}
		}
		if (!introLines.isEmpty())
			return introLines;

		// return race-specific dialogue if present
		// else return default dialog
		if (raceLabels().hasIntroduction())
			return raceLabels().introduction();
		return null;
	}
    public List<String> introduction() {
        // return race-specific dialogue if present
        // else return default dialog
        if (raceLabels().hasIntroduction())
            return raceLabels().introduction();
        return labels().introduction();
    }

    private List<String> varTokens(String s) { // BR: for debug
        String startKey = "[";
        int keySize = startKey.length();
        List<String> tokens = new ArrayList<>();
        int prevIndex = -1;
        int nextIndex = s.indexOf(startKey, prevIndex);
        while (nextIndex >= 0) {
            int endIndex = s.indexOf(']', nextIndex);
            if (endIndex <= nextIndex)
                return tokens;
            String var = s.substring(nextIndex+keySize, endIndex);
            tokens.add(var);
            prevIndex = nextIndex;
            nextIndex = s.indexOf(startKey, endIndex);
        }
        return tokens;
    }
    public boolean validateDialogueTokens() { // BR: for debug
    	boolean valid = true;
    	for (Entry<String, List<String>> entry : raceLabels().dialogueMapEntrySet()) {
    		List<String> val = entry.getValue();
    		if (val == null || val.isEmpty()) {
    			valid = false;
    			String key = entry.getKey();
				System.err.println("Keyword with empty text: " + key + " / " + id);
    		}
    		else {
    			for (String txt : val) {
    				List<String> tokens = varTokens(txt);
    				if (!tokens.isEmpty()) {
    					for (String token : tokens) {
    						String src = token;
    						token = token.replace("your_", "_");
    						token = token.replace("my_", "_");
    						token = token.replace("other_", "_");
    						token = token.replace("alien_", "_");
    						token = token.replace("player_", "_");
    						token = token.replace("spy_", "_");
    						token = token.replace("leader_", "_");
    						token = token.replace("defender_", "_");
    						token = token.replace("attacker_", "_");
    						token = token.replace("voter_", "_");
       						token = token.replace("candidate_", "_");
       						token = token.replace("victim_", "_");
        					token = token.replace("rebel_", "_");
        					token = token.replace("rival_", "_");

        					switch (token) {
	       						case "_name":
	       						case "_home":
	       						case "system":
	       						case "amt":
	       						case "year":
	       						case "tech":
	       						case "techGiven":
	       						case "techReceived":
	       						case "framed":
	       						case "spiesCaught":
	       						case "forced":
	       						case "target":
	       							break;
	       						default:
       								if (!raceLabels().hasLabel(token)) {
       	    							if (!labels().hasLabel(token)) {
       		    							valid = false;
       		    							System.err.println("Missing token: " + token
       		    									+ " / " + id + " / " + src);
       	    							}
       	    						}
       						}
    					}
    				}
    			}
    		}
    	}
    	return valid;
    }
    public String dialogue(String key) {
        // return race-specific dialogue if present
        // else return default dialog
        if (raceLabels().hasDialogue(key))
            return raceLabels().dialogue(key);
        return labels().dialogue(key);
    }
    public String name()                        { return text(id); }
    public void setDescription1(String desc)    { description1 = desc; }
    public void setDescription2(String desc)    { description2 = desc; }
    public void setDescription3(String desc)    { description3 = desc; }
    public void setDescription4(String desc)    { description4 = desc; }
    public String getDescription1()             {
    	return description1;
    }
    public String getDescription2()             {
    	return description2;
    }
    public String getDescription3()             {
    	return getDescription3(setupName());
    }
     public String getDescription3(String name) {
    	 String desc = description3.replace("[empire]", empireTitle());
    	return desc.replace("[race]", name);
    }
   public String getDescription4()              {
    	return description4;
    }
    
    public String setupName() {
    	if (raceNames.isEmpty())
    		return "";
        return text(substrings(raceNames.get(0), '|').get(0));
    }
    public int shipAttackBonus()              { return shipAttackBonus; }
    public void shipAttackBonus(int i)        { shipAttackBonus = i; }
    public int shipDefenseBonus()             { return shipDefenseBonus; }
    public void shipDefenseBonus(int i)       { shipDefenseBonus = i; }
    public int shipInitiativeBonus()          { return shipInitiativeBonus; }
    public void shipInitiativeBonus(int i)    { shipInitiativeBonus = i; }
    public int groundAttackBonus()            { return groundAttackBonus; }
    public void groundAttackBonus(int i)      { groundAttackBonus = i; }
    public float spyCostMod()                 { return spyCostMod; }
    public float internalSecurityAdj()        { return internalSecurityAdj; }
    public float spyInfiltrationAdj()         { return spyInfiltrationAdj; }
    public float workerProductivityMod()      { return workerProductivityMod; }
    public int robotControlsAdj()             { return robotControlsAdj; }
    public float techDiscoveryPct()           { return techDiscoveryPct; }
    public float techDiscoveryPct(int i)      { 
    	return min(1, max(0,
    			techDiscoveryPct + discoveryMod[i]));
    }
    public float researchBonusPct()           { return researchBonusPct; }
    public float researchNoSpyBonusPct()      {
    	if (options().forbidTechStealing())
    		return 1f + max(0f, spyInfiltrationAdj/2);
    	return 1f;
    }
    public float growthRateMod()              { return growthRateMod; }
    public float tradePctBonus()              { return tradePctBonus; }
    public float positiveDPMod()              { return positiveDPMod; }
    public int diplomacyBonus()               { return diplomacyBonus; }
    public float councilBonus()               { return councilBonus; }
    public boolean ignoresPlanetEnvironment() { return ignoresPlanetEnvironment; }
    public String acceptedPlanetEnvironment() { return acceptedPlanetEnvironment; }
    public boolean ignoresFactoryRefit()      { return ignoresFactoryRefit; }
    public int homeworldKey()                 { return homeworldKey; }
    public void homeworldKey(int i)           { homeworldKey = i; }
    public String title()                     { return title; }
    public void title(String s)               { title = s; }
    public String fullTitle()                 { return fullTitle; }
    public void fullTitle(String s)           { fullTitle = s; }
    // BR: Custom Races
    boolean isCustomRace()                    { return isCustomRace; }
    Race   isCustomRace(boolean val)          { isCustomRace = val; return this;}
    public boolean isRandomized() { return crEmpireNameRandom.equalsIgnoreCase(empireTitle); }
    DynOptions raceOptions()           	      { return raceOptions; }
    void   raceOptions(DynOptions val)        { raceOptions = val; }
    // BR: Get the values encoded in HomeworldKey
    public float bCBonus()                    { return bCBonus; }
    public void  bCBonus(float val)           { bCBonus = val; }
    public float hPFactor()                   { return hPFactor;  }
    public void  hPFactor(float val)          { hPFactor = val; }
    public float maintenanceFactor()          { return maintenanceFactor; }
    public void  maintenanceFactor(float val) { maintenanceFactor = val; }
    public float shipSpaceFactor()            { return shipSpaceFactor; }
    public void  shipSpaceFactor(float val)   { shipSpaceFactor = val; }
    public String planetArtifacts()           {
    	if (planetRessource.equalsIgnoreCase("Artifact")) { // for backward compatibility
    		planetRessource = "Normal";
    		planetArtifacts = "Artifact";
    	}
    	return planetArtifacts;
    }
    public void   planetArtifacts(String s)   {
    	planetArtifacts = s;
    	if (planetRessource.equalsIgnoreCase("Artifact")) { // for backward compatibility
    		planetRessource = "Normal";
    		planetArtifacts = "Artifact";
    	}
    }
    public String planetRessource()           {
    	if (planetRessource.equalsIgnoreCase("Artifact")) { // for backward compatibility
    		planetRessource = "Normal";
    		planetArtifacts = "Artifact";
    	}
    	return planetRessource;
    }
    public void   planetRessource(String s)   {
   		planetRessource = s;
    	if (s.equalsIgnoreCase("Artifact")) { // for backward compatibility
    		planetArtifacts = s;
    		planetRessource = "Normal";
    	}
    }
    public String planetEnvironment()         { return planetEnvironment; }
    public void   planetEnvironment(String s) { planetEnvironment = s; }

    public boolean raceWithUltraPoorHomeworld() {
        return planetRessource.equalsIgnoreCase("UltraPoor");
    }
    public boolean raceWithPoorHomeworld() {
        return planetRessource.equalsIgnoreCase("Poor");
    }
    public boolean raceWithRichHomeworld() {
        return planetRessource.equalsIgnoreCase("Rich");
    }
    public boolean raceWithUltraRichHomeworld() {
        return planetRessource.equalsIgnoreCase("UltraRich");
    }
    public boolean raceWithOrionLikeHomeworld() {
        return planetArtifacts.equalsIgnoreCase("OrionLike");
    }
    public boolean raceWithArtifactsHomeworld() {
        return planetArtifacts.equalsIgnoreCase("Artifacts")
        		|| planetRessource.equalsIgnoreCase("Artifacts"); // for backward compatibility
    }
    public boolean raceWithHostileHomeworld() {
        return planetEnvironment.equalsIgnoreCase("Hostile");
    }
    public boolean raceWithFertileHomeworld() {
        return planetEnvironment.equalsIgnoreCase("Fertile");
    }
    public boolean raceWithGaiaHomeworld() {
        return planetEnvironment.equalsIgnoreCase("Gaia");
    }

    public float defaultRaceRelations()       { return defaultRaceRelations; }
    public void defaultRaceRelations(int d)   { defaultRaceRelations = d; }
    public float baseRelations(Race r) {
        float definedRelations = raceRelations.containsKey(r.id) ? raceRelations.get(r.id) : defaultRaceRelations();
        return definedRelations + options().baseAIRelationsAdj();
    }
    public void baseRelations(String key, int d) { raceRelations.put(key, d); }
    public float labFlagX()                   { return labFlagX; }
    public void labFlagX(float d)             { labFlagX = d; }
    public int spyFactoryFrames()             { return spyFactoryFrames; }
    public void spyFactoryFrames(int d)       { spyFactoryFrames = d; }
    public int spyMissileFrames()             { return spyMissileFrames; }
    public void spyMissileFrames(int d)       { spyMissileFrames = d; }
    public int spyRebellionFrames()           { return spyRebellionFrames; }
    public void spyRebellionFrames(int d)     { spyRebellionFrames = d; }
    public void espionageXY(List<String> vals) {
        espionageX = parseInt(vals.get(0));
        if (vals.size() > 1)
            espionageY = parseInt(vals.get(1));
    }
    public Image flagWar()                    { return image(flagWarKey); }
    public Image flagNorm()                   { return image(flagNormKey); }
    public Image flagPact()                   { return image(flagPactKey); }
    public Image dialogWar()                  { return image(dlgWarKey); }
    public Image dialogNorm()                 { return image(dlgNormKey); }
    public Image dialogPact()                 { return image(dlgPactKey); }
    public Image council()                    { return image(councilKey);  }
    public Image gnnEvent(String id)          { return image(gnnEventKey(id)); }
    private String gnnEventKey(String id)     { return concat(gnnColor,"_",id); }
    public BufferedImage gnn()                { return currentFrame(gnnKey); }
    public BufferedImage gnnHost()            { return currentFrame(gnnHostKey); }
    public BufferedImage laboratory()         { return currentFrame(laboratoryKey);  }
    public BufferedImage embassy()            { return currentFrame(embassyKey);  }
    public BufferedImage holograph()          { return currentFrame(holographKey);  }
    public BufferedImage mugshot()            { return currentFrame(mugshotKey);  }
    public BufferedImage setupImage()         { return currentFrame(setupImageKey);  }
    public BufferedImage spyMugshotQuiet()    { return currentFrame(spyFaceKey, notTalking);  }
    public BufferedImage soldierMugshot()     { return currentFrame(soldierFaceKey, notTalking);  }
    public BufferedImage advisorMugshot()     { return currentFrame(advisorFaceKey, notTalking); }
    public BufferedImage advisorScout()       { return currentFrame(advisorScoutKey, notTalking); }
    public BufferedImage advisorTransport()   { return currentFrame(advisorTransportKey, notTalking); }
    public BufferedImage advisorDiplomacy()   { return currentFrame(advisorDiplomacyKey, notTalking); }
    public BufferedImage advisorShip()        { return currentFrame(advisorShipKey, notTalking); }
    public BufferedImage advisorRally()       { return currentFrame(advisorRallyKey, notTalking); }
    public BufferedImage advisorMissile()     { return currentFrame(advisorMissileKey, notTalking); }
    public BufferedImage advisorWeapon()      { return currentFrame(advisorWeaponKey, notTalking); }
    public BufferedImage advisorCouncil()     { return currentFrame(advisorCouncilKey, notTalking); }
    public BufferedImage advisorRebellion()   { return currentFrame(advisorRebellionKey, notTalking); }
    public BufferedImage advisorResistCouncil()   { return currentFrame(advisorResistCouncilKey, notTalking); }
    public BufferedImage advisorCouncilResisted()  { return currentFrame(advisorCouncilResistedKey, notTalking); }
    public BufferedImage diplomatTalking()    { return currentFrame(diplomatKey);  }
    public BufferedImage scientistTalking()   { return currentFrame(scientistKey);  }
    public BufferedImage soldierTalking()     { return currentFrame(soldierKey);  }
    public BufferedImage spyTalking()         { return currentFrame(spyKey);  }
    public BufferedImage diploMugshotQuiet()  { return currentFrame(mugshotKey, notTalking);  }
    public BufferedImage diploWideMugshot()   { return currentFrame(wideMugshotKey, notTalking);  }
    public BufferedImage diplomatQuiet()      { return currentFrame(diplomatKey, notTalking);  }
    public BufferedImage scientistQuiet()     { return currentFrame(scientistKey, notTalking);  }
    public BufferedImage soldierQuiet()       { return currentFrame(soldierKey, notTalking);  }
    public BufferedImage spyQuiet()           { return currentFrame(spyKey, notTalking);  }
    public BufferedImage councilLeader()      { return asBufferedImage(image(leaderKey));  }
    public BufferedImage diploMug()    {
        if (diploMug == null)
            diploMug = newBufferedImage(diploMugshotQuiet());
        return diploMug;
    }
    public BufferedImage wideDiploMug()    {
        if (wideDiploMug == null)
            wideDiploMug = newBufferedImage(diploWideMugshot());
        return wideDiploMug;
    }
    public Image transport()          {
        if (transportImg == null)
            transportImg = image(transportKey);
        return transportImg;
    }
    public BufferedImage transportDescending()    {
        if (transportClosedImg == null)
            transportClosedImg = currentFrame(transportDescKey, closed);
        return transportClosedImg;
    }
    public BufferedImage transportOpening()   { return currentFrame(transportDescKey, open); }
    
    public List<Image> sabotageFactoryFrames() {
        List<Image> images = new ArrayList<>();
        for (int i=1;i<=spyFactoryFrames;i++) {
            String fileName = directoryName+"/SabotageFactories/Frame"+String.format("%03d.jpg", i);
            Image img = icon(fileName).getImage();
            images.add(img);
        }
        return images;
    }
    public List<Image> sabotageMissileFrames() {
        List<Image> images = new ArrayList<>();
        for (int i=1;i<=spyMissileFrames;i++) {
            String fileName = directoryName+"/SabotageMissiles/Frame"+String.format("%03d.jpg", i);
            Image img = icon(fileName).getImage();
            images.add(img);
        }
        return images;
    }
    public List<Image> sabotageRebellionFrames() {
        List<Image> images = new ArrayList<>();
        for (int i=1;i<=spyRebellionFrames;i++) {
            String fileName = directoryName+"/SabotageRebellion/Frame"+String.format("%03d.jpg", i);
            Image img = icon(fileName).getImage();
            images.add(img);
        }
        return images;
    }
    public BufferedImage fortress(int i)      { return currentFrame(fortressKeys.get(i)); }
    public int randomFortress()               { return roll(1,fortressKeys.size())-1; }
    public BufferedImage shield()             { return currentFrame(shieldKey); }
    public void resetMugshot()                { resetAnimation(mugshotKey); }
    public void resetSetupImage()             { resetAnimation(setupImageKey); }
    public void resetDiplomat()               { resetAnimation(diplomatKey); }
    public void resetScientist()              { resetAnimation(scientistKey); }
    public void resetSoldier()                { resetAnimation(soldierKey); }
    public void resetSpy()                    { resetAnimation(spyKey); }
    public void resetGNN(String id)           {
        resetAnimation(gnnKey);
        resetAnimation(gnnHostKey);
        resetAnimation(gnnEventKey(id));
    }
    public boolean isHostile(PlanetType pt) {
        return ignoresPlanetEnvironment() ? false : pt.hostileToTerrans();
    }
    public int preferredShipSize()    { return preferredShipSize; }
    public int randomShipSize() {
        float r = random();
        if (r <= .5)
            return preferredShipSize;

        if (r <= .75)
            return Math.min(preferredShipSize+1, ShipDesign.MAX_SIZE);

        return max(preferredShipSize-1, 0);
    }
    public int randomLeaderAttitude() {
        float r = random();
        float modAccum = 0;
        for (int i=0;i<personalityPct.length;i++) {
            modAccum += personalityPct[i];
            if (r < modAccum)
                return i;
        };
        return 0;
    }
    public int mostCommonLeaderAttitude() {
        float maxPct = 0;
        int maxAttitude = 0;
        for (int i=0;i<personalityPct.length;i++) {
            if (personalityPct[i] > maxPct) {
                maxPct = personalityPct[i];
                maxAttitude = i;
            }
        };
        return maxAttitude;
    }
    public int randomLeaderObjective() {
        float r = random();
        float modAccum = 0;
        for (int i=0;i<objectivePct.length;i++) {
            modAccum += objectivePct[i];
            if (r < modAccum)
                return i;
        };
        return 0;
    }
    public String randomSystemName(Empire emp) {
        // this is only called when a new system is scouted
        // the name is stored on the empire's system view for this system
        // and transferred to the system when it is colonized
        List<String> allPossibleNames = masterNameList(emp);
        for (StarSystem sys : galaxy().starSystems()) {
        	String name = sys.name().trim(); // custom species may add confusing spaces
        	allPossibleNames.remove(name);
        }
        // Multiple and Custom species may share the same list... We have to looks thru all systems view
        // looking at the galaxy().starSystems() is not good enough. Named only when colonized.
        int n = galaxy().numStarSystems();
        for (Empire e : galaxy().empires())
        	if (!e.extinct())
                for (int i=0;i<n;i++)
                    if (e.sv.isScouted(i))
                        allPossibleNames.remove(emp.sv.name(i));
        // Custom species may share the same list... We have to looks thru all systems
        // int n = galaxy().numStarSystems();
        // for (int i=0;i<n;i++) {
        //     if (emp.sv.isScouted(i))
        //         allPossibleNames.remove(emp.sv.name(i));
        // }
        String systemName = allPossibleNames.isEmpty() ? galaxy().nextSystemName(id) : allPossibleNames.get(0);
        log("Naming system:", systemName);
        return systemName;
    }
    private List<String> masterNameList(Empire emp) {
        Collections.shuffle(systemNames);
        // BR: add custom species prefix and suffix
        List<String> complexNames = new ArrayList<>();
    	String prefix = emp.dataRace().worldsPrefix;
    	String suffix = emp.dataRace().worldsSuffix;
        for (String s: systemNames)
        	complexNames.add(prefix + s + suffix);
        
        List<String> names = new ArrayList<>(complexNames);
        
        for (String s: complexNames)
            names.add(text("COLONY_NAME_2", s));
        for (String s: complexNames)
            names.add(text("COLONY_NAME_3", s));
        for (String s: complexNames)
            names.add(text("COLONY_NAME_4", s));
        for (String s: complexNames)
            names.add(text("COLONY_NAME_5", s));
        return names;
    }
    public String randomLeaderName() { return random(leaderNames); }
    public List<String> shipNames(int size) {
        switch(size) {
            case ShipDesign.SMALL:   return shipNamesSmall;
            case ShipDesign.MEDIUM:  return shipNamesMedium;
            case ShipDesign.LARGE:   return shipNamesLarge;
            case ShipDesign.HUGE:    return shipNamesHuge;
        }
        return null;
    }
    public void addSoundKey(String s)   { soundKeys.add(s); }
    public void colonistWalk(String s) {
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
        colonistWalkingFrames = parseInt(vals.get(1));
        colonistX1 = parseInt(fromXY.get(0));
        colonistY1 = parseInt(fromXY.get(1));
        colonistX2 = parseInt(toXY.get(0));
        colonistY2 = parseInt(toXY.get(1));
    }
    public void parseFortress(String s) {
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
    public void flagSize(String s) {
        List<String> vals = substrings(s, 'x');
        if (vals.size() != 2)
            err("Invalid FlagSize string: ", s);

        flagW  = parseInt(vals.get(0));
        flagH = parseInt(vals.get(1));
    }
    public void parseTransportDesc(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 3)
            err("Invalid TransportDesc string: ", s);

        transportDescKey  = concat(vals.get(0), ",", vals.get(2));
        transportDescFrames = parseInt(vals.get(1));
    }
    public void parseTransportOpen(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 3)
            err("Invalid Transport Open string: ", s);

        transportOpenKey  = concat(vals.get(0), ",", vals.get(2));
        transportOpenFrames = parseInt(vals.get(1));
    }
    public void parseWinSplash(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 4)
            err("Invalid Win Splash string: ", s);

        winSplashKey  = vals.get(0);
        int r = parseInt(vals.get(1));
        int g = parseInt(vals.get(2));
        int b = parseInt(vals.get(3));
        winTextC = new Color(r,g,b);
    }
    public void parseLossSplash(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 4)
            err("Invalid Loss Splash string: ", s);

        lossSplashKey  = vals.get(0).trim();
        int r = parseInt(vals.get(1).trim());
        int g = parseInt(vals.get(2).trim());
        int b = parseInt(vals.get(3).trim());
        lossTextC = new Color(r,g,b);
    }
    public void parseCouncilDiplomatLocation(String s) {
        List<String> vals = substrings(s, ',');
        if (vals.size() != 4)
            err("Invalid Council Diplomat location string: ", s);

        diploScale  = parseFloat(vals.get(0).trim());
        diploXOffset = parseInt(vals.get(1).trim());
        diploYOffset = parseInt(vals.get(2).trim());
        diploOpacity = parseFloat(vals.get(3).trim());
    }
    public void parseRaceNames(String names) { //, String langId) {
        raceNames.clear();
        raceNames.addAll(substrings(names, ','));
    }
}
