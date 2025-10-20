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
package rotp.model.empires.species;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rotp.Rotp;
import rotp.model.game.IGameOptions;
import rotp.ui.util.ParamSpeciesName;
import rotp.ui.util.PlayerShipSet;
import rotp.ui.util.StringList;
import rotp.util.AnimationManager;
import rotp.util.Base;
import rotp.util.ImageManager;
import rotp.util.LabelManager;
import rotp.util.LanguageManager;
import rotp.util.PixelShifter;

public enum RaceFactory implements Base {
    INSTANCE;
    public static RaceFactory current()   { return INSTANCE; }

    private static final String raceListFile = "races/listing.txt";
    private  RaceFactory() {
        loadDataFiles();
    }
    public void loadDataFiles() {
        log("Loading Races: ", raceListFile);
        BufferedReader in = reader(raceListFile);
        if (in == null)
            return;
        try {
            String input;
            while ((input = in.readLine()) != null)
                loadRaceDataFile(input.trim());
            in.close();
        }
        catch (IOException e) {
            err("RaceFactory.loadRaces -- IOException: ", e.toString());
        }
    }
    // BR: The safest way to copy a race!
    private static List<String> reloadedSettings() {
    	List<String> list = new ArrayList<>();
    	list.add("key");
    	list.add("langKey");
    	list.add("year");
    	list.add("homestarType");
    	list.add("homeworldType");
    	list.add("homeworldSize");
    	list.add("homeworldKey");
    	list.add("mugshot");
		// list.add("setupImage");
		// list.add("spyMug");
		// list.add("soldierMug");
		// list.add("diploProfile");
		// list.add("lab");
		// list.add("embassy");
		// list.add("holograph");
		// list.add("diplomat");
		// list.add("scientist");
		// list.add("trooper");
		// list.add("spy");
		// list.add("leader");
		// list.add("diploTheme");
		// list.add("gnn");
		// list.add("gnnHost");
		// list.add("gnnColor");
		// list.add("gnnTextColor");
		// list.add("winSplash");
		// list.add("lossSplash");
		// list.add("advisorMug");
		// list.add("advisorScout");
		// list.add("advisorTransport");
		// list.add("advisorDiplomacy");
		// list.add("advisorShip");
		// list.add("advisorRally");
		// list.add("advisorMissile");
		// list.add("advisorWeapon");
		// list.add("advisorCouncil");
		// list.add("advisorRebellion");
		// list.add("advisorResistCouncil");
		// list.add("advisorCouncilResisted");
		// list.add("transport");
		// list.add("transportDesc");
		// list.add("transportOpen");
		// list.add("transportW");
		// list.add("transportYOff");
		// list.add("transportLandingFrames");
		// list.add("colonistWalk");
		// list.add("landingAudio");
		// list.add("troopIcon");
		// list.add("troopHIcon");
		// list.add("troopDeath1");
		// list.add("troopDeath2");
		// list.add("troopDeath3");
		// list.add("troopDeath4");
		// list.add("troopDeath1H");
		// list.add("troopDeath2H");
		// list.add("troopDeath3H");
		// list.add("troopDeath4H");
		// list.add("troopScale");
		// list.add("troopFireXY");
		// list.add("troopHFireXY");
		// list.add("fortress");
		// list.add("council");
		// list.add("shield");
		// list.add("introTextX");
		// list.add("espionageXY");
		// list.add("spyFactories");
		// list.add("spyMissiles");
		// list.add("spyRebellion");
		// list.add("dialogTextX");
		// list.add("dialogTextY");
		// list.add("councilDiplo");
		// list.add("labFlagX");
		list.add("flagSize");
    	list.add("flagWar");
    	list.add("flagNormal");
    	list.add("flagPact");
		// list.add("dialogWar");
		// list.add("dialogNormal");
		// list.add("dialogPact");
		// list.add("voice");
		// list.add("ambience");
    	list.add("species");
    	list.add("personality");
    	list.add("objective");
    	list.add("preferredship");
    	list.add("shipmod");
    	list.add("groundmod");
    	list.add("spymod");
    	list.add("prodmod");
    	list.add("techmod");
    	list.add("popmod");
    	list.add("diplomod");
    	list.add("research");
    	list.add("relations");
    	list.add("shipdesign");
    	// -- None in current races definitions
    	// list.add("diplomatXform"); // never used
    	// list.add("homeworld"); // = "homeworldKey"
    	// list.add("available"); // Available player | Available AI // never used
    	return list;
    }
    // BR: The safest way to copy a race!
    private boolean isInList(String val, List<String> list) {
    	for (String s : list)
    		if (s.equalsIgnoreCase(val))
    			return true;
    	return false;
    }
    // BR: The safest way to copy a race!
    Race reloadRaceDataFile(String raceDirPath) {
        String filename = raceDirPath+"/definition.txt";
        List<String> loadList = reloadedSettings();
        BufferedReader in = reader(filename);
        if (in == null) {
            err("Could not open file: ", filename);
            return null;
        }
        Race newRace = new Race(raceDirPath);
        try {
            String input;
            while ((input = in.readLine()) != null) {
            	// load only what is mandatory 
            	if (isComment(input))
            		continue;
                List<String> vals = substrings(input, ':');
                if (vals.size() < 2)
                	continue;
                if (isInList(vals.get(0), loadList))
                	loadRaceDataLine(newRace, input);
            }
            in.close();
            //ImageManager.current().loadImageList(raceDirPath+"/images.txt");
            //AnimationManager.current().loadAnimationList(raceDirPath+"/animations.txt");
        }
        catch (IOException e) {
            err("RaceFactory.loadRaceDataFile(", filename, ") -- IOException: ", e.toString());
        }
		// useless for abilities
		newRace.troopNormal(null);
		newRace.troopHostile(null);
		newRace.troopDeath1(null);
		newRace.troopDeath2(null);
		newRace.troopDeath3(null);
		newRace.troopDeath4(null);
		newRace.troopDeath1H(null);
		newRace.troopDeath2H(null);
		newRace.troopDeath3H(null);
		newRace.troopDeath4H(null);

	return newRace;
	}
    private void loadRaceDataFile(String raceDirName) {
        if (isComment(raceDirName))
            return;

        // try to open the race file
        String raceDirPath = concat("races/", raceDirName);
        String filename = raceDirPath+"/definition.txt";
        BufferedReader in = reader(filename);
        if (in == null) {
            err("Could not open file: ", filename);
            return;
        }

        Race newRace = new Race(raceDirPath);
        try {
            String input;
            while ((input = in.readLine()) != null)
                loadRaceDataLine(newRace, input);
            in.close();
            ImageManager.current().loadImageList(raceDirPath+"/images.txt");
            AnimationManager.current().loadAnimationList(raceDirPath+"/animations.txt");
        }
        catch (IOException e) {
            err("RaceFactory.loadRaceDataFile(", filename, ") -- IOException: ", e.toString());
        }
        Species.addRace(newRace);
    }
    public void resetRaceLangFiles() {
        for (Race r : Species.races())
            r.raceLabels().resetDialogue();
    }
    public void loadRaceLangFiles(String langDir) {
        for (Race r : Species.races()) {
        	r.raceLabels().resetDialogue();
        	loadRaceLangFiles(r, langDir);
			Species.addName(r);
        }
    }
	void loadRaceLangFiles(Race r, String langDir) {
		String dir = concat("lang/", langDir, "/races/");
		List<String> sNames = readSystemNames(dir+r.langKey()+".systems.txt");
		if (sNames != null)
			r.systemNames(sNames);
		LabelManager labels = r.raceLabels();
		//
		// Load Species Labels
		//
		labels.labelFile(r.langKey()+".labels.txt");
		labels.dialogueFile(r.langKey()+".dialogue.txt");
		labels.introFile(r.langKey()+".intro.txt");
        labels.loadIntroFile(dir);
        labels.loadDialogueFile(dir);
        labels.loadLabelFile(dir);

        // Update Species names if required.
        // Only for English and French language
        String[] speciesNames = null;
        int selectedLanguage = LanguageManager.selectedLanguage();
        String langId = LanguageManager.selectedLanguageDir();
        String speciesName = "";
		if (!r.id.equals("RACE_HUMAN")) { // Human names do not change
			if (selectedLanguage <= 0) {
				HashMap<String, ParamSpeciesName> map = IGameOptions.speciesNameMap;
				ParamSpeciesName param = map.get(r.id);
				speciesNames = param==null? null : param.getValid();
				// Will be added instead of replaced
				if (speciesNames != null) {
					labels.replaceFirstVal("_empire",		speciesNames[0]);
					labels.replaceFirstVal("_race",			speciesNames[1]);
					labels.replaceFirstVal("_race_plural",	speciesNames[2]);
					speciesName = speciesNames[1];
				}
			}
			else if (langId.equals("fr")) {
				HashMap<String, ParamSpeciesName> map = IGameOptions.speciesNameMapFr;
				ParamSpeciesName param = map.get(r.id + "_FR");
				speciesNames = param==null? null : param.getValid();
				// Will be added instead of replaced
				if (speciesNames != null) {
					labels.replaceFirstVal("_empire",			 speciesNames[0]);
					labels.replaceFirstVal("_empireof",			 speciesNames[1]);
					labels.replaceFirstVal("_raceadjec",		 speciesNames[2]);
					labels.replaceFirstVal("_raceadjecF",		 speciesNames[3]);
					labels.replaceFirstVal("_race_pluralnoun",	 speciesNames[4]);
					labels.replaceFirstVal("_race_pluralnounof", speciesNames[5]);
					labels.replaceFirstVal("_race_pluralnounto", speciesNames[6]);
					labels.replaceFirstVal("_race_pluraladjec",	 speciesNames[7]);
					labels.replaceFirstVal("_race_pluraladjecF", speciesNames[8]);
					speciesName = speciesNames[2];
				}
		}
		}
        //
        // Load Species Names
        //
		String filename = dir+r.langKey()+".names.txt";
        BufferedReader in = reader(filename);
        if (in == null)
            return;

        int wc = 0;
        try {
            String input;
            while ((input = in.readLine()) != null)
                wc += loadRaceLangLine(r, input, langDir);
            in.close();
        }
		catch (IOException e) {
			err("RaceFactory.loadRaceLangFile(", r.directoryName()+") -- IOException: ", e.toString());
		}
		if (speciesNames != null) { // Update Species names if required.
			StringList nameList = r.speciesNames();
			// Move the first element to the end
			nameList.rotate(-1);
			// Add the new element at the first position
			nameList.add(0, speciesName);
		}
        if (Rotp.countWords)
            log("WORDS - "+filename+": "+wc);

        //
        // Update Ship set labels
        //
        String shipset;
        String labelKey = PlayerShipSet.tokenLabelKey();
        String tag = LabelManager.current().label(labelKey);
        List<String> tokens = varTokens(tag, "base"); // Should be one token
        if (tokens.size() >= 1) {
        	String token = tokens.get(0);
        	List<String> values = substrings(r.text(token), ',');
        	if (values.size() >= 1)
        		shipset = values.get(0);
        	else
        		shipset = r.name();
        }
        else 
			shipset = r.name();
        String key = PlayerShipSet.rootLabelKey() + r.id.replace("RACE_", "");
        LabelManager.current().addLabel(key, shipset);
    }
    private void loadRaceDataLine(Race r, String input) {
        if (isComment(input))
            return;

        List<String> vals = substrings(input, ':');
        if (vals.size() < 2)
            return;

        String key = vals.get(0);
        String value = vals.get(1);

		if (key.equalsIgnoreCase("key"))				{ r.id(value); return; }
		if (key.equalsIgnoreCase("langKey"))			{ r.langKey(value); return; }
		if (key.equalsIgnoreCase("year"))				{ r.startingYear(parseInt(value)); return; }
		if (key.equalsIgnoreCase("homestarType"))		{ r.homeworldStarType(value); return; }
		if (key.equalsIgnoreCase("homeworldType"))		{ r.homeworldPlanetType(value); return; }
		// if (key.equalsIgnoreCase("homeworldKey"))	{ r.homeworldKey(parseInt(value)); return; }
		if (key.equalsIgnoreCase("homeworldKey"))		{ parseHomeWorlKey(r, value); return; }
		if (key.equalsIgnoreCase("homeworldSize"))		{ r.homeworldSize(parseInt(value)); return; }
		if (key.equalsIgnoreCase("mugshot"))			{ r.mugshotKey(value); return; }
		if (key.equalsIgnoreCase("diploProfile"))		{ r.wideMugshotKey(value); return; }
		if (key.equalsIgnoreCase("setupImage"))			{ r.setupImageKey(value); return; }
		if (key.equalsIgnoreCase("spyMug"))				{ r.spyFaceKey(value); return; }
		if (key.equalsIgnoreCase("soldierMug"))			{ r.soldierFaceKey(value); return; }
		if (key.equalsIgnoreCase("advisorMug"))			{ r.advisorFaceKey(value); return; }
		if (key.equalsIgnoreCase("advisorScout"))		{ r.advisorScoutKey(value); return; }
		if (key.equalsIgnoreCase("advisorTransport"))	{ r.advisorTransportKey(value); return; }
		if (key.equalsIgnoreCase("advisorDiplomacy"))	{ r.advisorDiplomacyKey(value); return; }
		if (key.equalsIgnoreCase("advisorShip"))		{ r.advisorShipKey(value); return; }
		if (key.equalsIgnoreCase("advisorRally"))		{ r.advisorRallyKey(value); return; }
		if (key.equalsIgnoreCase("advisorMissile"))		{ r.advisorMissileKey(value); return; }
		if (key.equalsIgnoreCase("advisorWeapon"))		{ r.advisorWeaponKey(value); return; }
		if (key.equalsIgnoreCase("advisorCouncil"))		{ r.advisorCouncilKey(value); return; }
		if (key.equalsIgnoreCase("advisorRebellion"))	{ r.advisorRebellionKey(value); return; }
		if (key.equalsIgnoreCase("advisorResistCouncil"))	{ r.advisorResistCouncilKey(value); return; }
		if (key.equalsIgnoreCase("advisorCouncilResisted"))	{ r.advisorCouncilResistedKey(value); return; }
		if (key.equalsIgnoreCase("council")) 		{ r.councilKey(value); return; }
		if (key.equalsIgnoreCase("lab"))			{ r.laboratoryKey(value); return; }
		if (key.equalsIgnoreCase("embassy"))		{ r.embassyKey(value); return; }
		if (key.equalsIgnoreCase("holograph"))		{ r.holographKey(value); return; }
		if (key.equalsIgnoreCase("diplomat"))		{ r.diplomatKey(value); return; }
		if (key.equalsIgnoreCase("scientist"))		{ r.scientistKey(value); return; }
		if (key.equalsIgnoreCase("trooper"))		{ r.soldierKey(value); return; }
		if (key.equalsIgnoreCase("spy"))			{ r.spyKey(value); return; }
		if (key.equalsIgnoreCase("leader"))			{ r.leaderKey(value); return; }
		if (key.equalsIgnoreCase("diploTheme"))		{ r.diplomacyTheme(value); return; }
		if (key.equalsIgnoreCase("gnn"))			{ r.gnnKey(value); return; }
		if (key.equalsIgnoreCase("gnnHost"))		{ r.gnnHostKey(value); return; }
		if (key.equalsIgnoreCase("gnnColor"))		{ r.gnnColor(value); return; }
		if (key.equalsIgnoreCase("gnnTextColor"))	{ r.gnnTextColor(parseColor(value)); return; }
		if (key.equalsIgnoreCase("diplomatXform"))	{ r.diplomacyTransformer(PixelShifter.createFrom(value)); }
		if (key.equalsIgnoreCase("winSplash"))		{ r.parseWinSplash(value); return; }
		if (key.equalsIgnoreCase("lossSplash"))		{ r.parseLossSplash(value); return; }
		if (key.equalsIgnoreCase("flagSize"))		{ r.flagSize(value); return; }
		if (key.equalsIgnoreCase("flagWar"))		{ r.flagWarKey(value); return; }
		if (key.equalsIgnoreCase("flagNormal"))		{ r.flagNormKey(value); return; }
		if (key.equalsIgnoreCase("flagPact"))		{ r.flagPactKey(value); return; }
		if (key.equalsIgnoreCase("dialogWar"))		{ r.dlgWarKey(value); return; }
		if (key.equalsIgnoreCase("dialogNormal"))	{ r.dlgNormKey(value); return; }
		if (key.equalsIgnoreCase("dialogPact"))		{ r.dlgPactKey(value); return; }
		if (key.equalsIgnoreCase("troopIcon"))		{ r.troopNormal().iconSpec(value); return; }
		if (key.equalsIgnoreCase("troopFireXY"))	{ r.troopNormal().fireXY(value); return; }
		if (key.equalsIgnoreCase("troopScale"))		{
			r.troopNormal().scaling(value);
			r.troopDeath1().scaling(value);
			r.troopDeath2().scaling(value);
			r.troopDeath3().scaling(value);
			r.troopDeath4().scaling(value);
			r.troopHostile().scaling(value);
			r.troopDeath1H().scaling(value);
			r.troopDeath2H().scaling(value);
			r.troopDeath3H().scaling(value);
			r.troopDeath4H().scaling(value);
			return;
		}
		if (key.equalsIgnoreCase("troopHIcon"))		{ r.troopHostile().iconSpec(value); return; }
		if (key.equalsIgnoreCase("troopHFireXY"))	{ r.troopHostile().fireXY(value); return; }
		if (key.equalsIgnoreCase("troopDeath1"))	{ r.troopDeath1().iconSpec(value); return; }
		if (key.equalsIgnoreCase("troopDeath2"))	{ r.troopDeath2().iconSpec(value); return; }
		if (key.equalsIgnoreCase("troopDeath3"))	{ r.troopDeath3().iconSpec(value); return; }
		if (key.equalsIgnoreCase("troopDeath4"))	{ r.troopDeath4().iconSpec(value); return; }
		if (key.equalsIgnoreCase("troopDeath1H"))	{ r.troopDeath1H().iconSpec(value); return; }
		if (key.equalsIgnoreCase("troopDeath2H"))	{ r.troopDeath2H().iconSpec(value); return; }
		if (key.equalsIgnoreCase("troopDeath3H"))	{ r.troopDeath3H().iconSpec(value); return; }
		if (key.equalsIgnoreCase("troopDeath4H"))	{ r.troopDeath4H().iconSpec(value); return; }
		if (key.equalsIgnoreCase("landingAudio"))	{ r.shipAudioKey(value); return; }
		if (key.equalsIgnoreCase("transport"))		{ r.transportKey(value); return; }
		if (key.equalsIgnoreCase("transportDesc"))	{ r.parseTransportDesc(value); return; }
		if (key.equalsIgnoreCase("transportOpen"))	{ r.parseTransportOpen(value); return; }
		if (key.equalsIgnoreCase("transportW"))		{ r.transportW(parseInt(value)); return; }
		if (key.equalsIgnoreCase("transportYOff"))	{ r.transportYOffset(parseInt(value)); return; }
		if (key.equalsIgnoreCase("transportLandingFrames"))	{ r.transportLandingFrames(parseInt(value)); return; }
		if (key.equalsIgnoreCase("colonistWalk"))	{ r.colonistWalk(value); return; }
		if (key.equalsIgnoreCase("labFlagX"))		{ r.labFlagX(parseFloat(value)); return; }
		if (key.equalsIgnoreCase("spyFactories"))	{ r.spyFactoryFrames(parseInt(value)); return; }
		if (key.equalsIgnoreCase("spyMissiles"))	{ r.spyMissileFrames(parseInt(value)); return; }
		if (key.equalsIgnoreCase("spyRebellion"))	{ r.spyRebellionFrames(parseInt(value)); return; }
		if (key.equalsIgnoreCase("espionageXY"))	{ r.espionageXY(substrings(value, '@')); return; }
		if (key.equalsIgnoreCase("dialogTextX"))	{ parseDialogTextMargins(r, substrings(value, ',')); return; }
		if (key.equalsIgnoreCase("dialogTextY"))	{ r.dialogTopY(parseInt(value)); return; }
		if (key.equalsIgnoreCase("fortress"))		{ r.parseFortress(value); return; }
		if (key.equalsIgnoreCase("shield"))			{ r.shieldKey(value); return; }
		if (key.equalsIgnoreCase("introTextX"))		{ r.introTextX(parseInt(value)); return; }
		if (key.equalsIgnoreCase("councilDiplo"))	{ r.parseCouncilDiplomatLocation(value); return; }
		if (key.equalsIgnoreCase("homeworld"))		{ r.homeworldKey(parseInt(value)); return; }
		if (key.equalsIgnoreCase("voice"))			{ r.voiceKey(value); return; }
		if (key.equalsIgnoreCase("ambience"))		{ r.ambienceKey(value); return; }
		if (key.equalsIgnoreCase("species"))		{ parseRaceSpeciesMods(r, substrings(value, ',')); return; }
		if (key.equalsIgnoreCase("ignoreEco"))		{
			r.acceptedPlanetEnvironment(value);
			r.ignoresPlanetEnvironment(!value.equalsIgnoreCase("No"));
			return;
		}
		if (key.equalsIgnoreCase("preferredship"))	{ parseRaceShipSize(r, substrings(value,',')); return; }
		if (key.equalsIgnoreCase("shipmod"))		{ parseRaceShipMods(r, substrings(value,',')); return; }
		if (key.equalsIgnoreCase("groundmod"))		{ r.groundAttackBonus(parseInt(value)); return; }
		if (key.equalsIgnoreCase("spymod"))			{ parseRaceSpyMods(r, substrings(value,',')); return; }
		if (key.equalsIgnoreCase("prodmod"))		{ parseRaceProdMods(r, substrings(value,',')); return; }
		if (key.equalsIgnoreCase("techmod"))		{ parseRaceTechMods(r, substrings(value,',')); return; }
		if (key.equalsIgnoreCase("popmod"))			{ parseRacePopMods(r, value); return; }
		if (key.equalsIgnoreCase("diplomod"))		{ parseRaceDiploMods(r, substrings(value,',')); return; }
		if (key.equalsIgnoreCase("research"))		{ parseRaceResearchMods(r, substrings(value,',')); return; }
		if (key.equalsIgnoreCase("personality"))	{ parseRacePersonalityMods(r, value); return; }
		if (key.equalsIgnoreCase("objective"))		{ parseRaceObjectiveMods(r, value); return; }
		if (key.equalsIgnoreCase("relations"))		{ parseRaceRelationsMods(r, substrings(value,',')); return; }
		if (key.equalsIgnoreCase("shipdesign"))		{ parseShipDesignMods(r, substrings(value,',')); return; }
		if (key.equalsIgnoreCase("available"))		{ parseRaceAvailableFlags(r, value); return; }

	err("unknown key->", input);
	}

    private int loadRaceLangLine(Race r, String input, String langDir) {
        if (isComment(input))
            return 0;

        List<String> vals = substrings(input, ':');
        if (vals.size() < 2)
            return 0;

        String key = vals.get(0);
        String value = vals.get(1);

        int wc = 0;
        if (Rotp.countWords)
            wc = substrings(value,',').size();  // uncomment 

		if (key.equalsIgnoreCase("name"))	{ r.parseSpeciesNames(value); return wc; }
		if (key.equalsIgnoreCase("desc1"))	{ r.setDescription1(value); return wc; }
		if (key.equalsIgnoreCase("desc2"))	{ r.setDescription2(value); return wc; }
		if (key.equalsIgnoreCase("desc3"))	{ r.setDescription3(value); return wc; }
		if (key.equalsIgnoreCase("desc4"))	{ r.setDescription4(value); return wc; } // modnar: add desc4
		if (key.equalsIgnoreCase("home"))	{ r.homeSystemNames().clear(); r.homeSystemNames().addAll(substrings(value, ',')); return wc; }
		if (key.equalsIgnoreCase("title"))	{ r.title(value.trim()); return wc; }
		if (key.equalsIgnoreCase("fulltitle"))	{ r.fullTitle(value.trim()); return wc; }
		if (key.equalsIgnoreCase("leader"))	{ r.leaderNames().clear(); r.leaderNames().addAll(substrings(value, ',')); return wc; }
		if (key.equalsIgnoreCase("ship1"))	{ r.shipNamesSmall().clear(); r.shipNamesSmall().addAll(substrings(value, ',')); return wc; }
		if (key.equalsIgnoreCase("ship2"))	{ r.shipNamesMedium().clear(); r.shipNamesMedium().addAll(substrings(value, ',')); return wc; }
		if (key.equalsIgnoreCase("ship3"))	{ r.shipNamesLarge().clear(); r.shipNamesLarge().addAll(substrings(value, ',')); return wc; }
		if (key.equalsIgnoreCase("ship4"))	{ r.shipNamesHuge().clear(); r.shipNamesHuge().addAll(substrings(value, ',')); return wc; }
	err("unknown key->", input);
	return 0;
	}
	private void parseHomeWorlKey(Race r, String value) {
		int key = parseInt(value);
		r.homeworldKey(key);
		switch (key) {
		case 888:
			r.hPFactor(0.666f);
			r.shipSpaceFactor(1.4f);
			r.planetRessource("Rich");
			return;
		case 1337:
			r.planetArtifacts("Artifacts");
			r.planetEnvironment("Fertile");
			return;
		case 8888:
			r.planetRessource("UltraRich");
			return;
		case 10101:
			r.bCBonus(0.25f);
			r.maintenanceFactor(0.5f);
			return;
		}
	}
	private void parseDialogTextMargins(Race r, List<String> vals) {
		if (vals.size() < 2)
			err("Race ", r.name(), " is missing some dialog text margins");
		r.dialogLeftMargin(parseInt(vals.get(0)));
		r.dialogRightMargin(parseInt(vals.get(1)));
	}
	private void parseRaceSpeciesMods(Race r, List<String> vals)	{
		if (vals.size() < 2)
			err("Race ", r.name(), " is missing some species vars");
		
		// field #1 is species type (carbon,silicate,robotic) -
		r.speciesType(parseInt(vals.get(0)));
		// deprecated - will be overwritten by ignoreEco param
		r.ignoresPlanetEnvironment(parseInt(vals.get(1)) == 1);
		if (parseInt(vals.get(1)) == 1)
			r.acceptedPlanetEnvironment("All");
		else
			r.acceptedPlanetEnvironment("No");
	}
	private void parseRaceShipSize(Race r, List<String> vals) {
		r.preferredShipSet(vals.get(0));
		r.preferredShipSize(parseInt(vals.get(1)));
	}
	private void parseRaceShipMods(Race r, List<String> vals)	{
		r.shipAttackBonus(parseInt(vals.get(0)));
		r.shipDefenseBonus(parseInt(vals.get(1)));
		r.shipInitiativeBonus(parseInt(vals.get(2)));
	}
	private void parseRaceSpyMods(Race r, List<String> vals)	{
		r.spyCostMod((float) parseInt(vals.get(0)) / 100);
		r.internalSecurityAdj((float) parseInt(vals.get(1)) / 100);
		r.spyInfiltrationAdj((float) parseInt(vals.get(2)) / 100);
		r.telepathic(parseInt(vals.get(3)) == 1);
		r.masksDiplomacy(parseInt(vals.get(4)) == 1);
	}
	private void parseRaceProdMods(Race r, List<String> vals)	{
		if (vals.size() < 3)
			err("Race ", r.name(), " is missing some prod vars. ");
		r.workerProductivityMod((float) parseInt(vals.get(0)) / 100);
		r.robotControlsAdj(parseInt(vals.get(1)));
		r.ignoresFactoryRefit((parseInt(vals.get(2)) == 1));
	}
	private void parseRaceTechMods(Race r, List<String> vals)	{
		if (vals.size() < 2)
			err("Race ", r.name(), " is missing some research vars. ");
		// field #1 is tech discovery pct (only field for now)
		r.techDiscoveryPct((float) parseInt(vals.get(0)) / 100);
		r.researchBonusPct((float) parseInt(vals.get(1)) / 100);
	}
	private void parseRacePopMods(Race r, String input) {
		// field #1 is the growth rate mod (only field for now)
		String growthMod = input.trim();
		r.growthRateMod((float) parseInt(growthMod) / 100);
	}
	private void parseRaceDiploMods(Race r, List<String> vals)	{
		// field #1 is trade bonus modifier (as %)
		r.tradePctBonus((float) parseInt(vals.get(0)) / 100);
		// field #2 is positive DP modifier (as %)
		r.positiveDPMod((float) parseInt(vals.get(1)) / 100);
		// field #3 is diplomatic relations modifier (as %)
		r.diplomacyBonus(parseInt(vals.get(2)));
		// field #4 is council vote modifier (as %)
		r.councilBonus((float) parseInt(vals.get(3)) / 100);
	}
	private void parseRaceResearchMods(Race r, List<String> vals)	{
		// field #1 is computer %
		r.techMod(0, (float) parseInt(vals.get(0)) / 100);
		// field #2 is construction %
		r.techMod(1, (float) parseInt(vals.get(1)) / 100);
		// field #3 is force field %
		r.techMod(2, (float) parseInt(vals.get(2)) / 100);
		// field #4 is planetary %
		r.techMod(3, (float) parseInt(vals.get(3)) / 100);
		// field #5 is propulsion %
		r.techMod(4, (float) parseInt(vals.get(4)) / 100);
		// field #6 is weapon %
		r.techMod(5, (float) parseInt(vals.get(5)) / 100);
	}
	private void parseRacePersonalityMods(Race r, String input) {
		List<String> vals = substrings(input, ',');
		for (int i=0; i<vals.size(); i++)
			r.personalityPct(i, parseInt(vals.get(i))/100.0f);
	}
	private void parseRaceObjectiveMods(Race r, String input) {
		List<String> vals = substrings(input, ',');
		for (int i=0; i<vals.size(); i++)
			r.objectivePct(i, parseFloat(vals.get(i))/100.0f);
	}
	private void parseRaceRelationsMods(Race r, List<String> relations) {
		for (String s : relations) {
			List<String> vals = substrings(s, '=');
			if (vals.get(0).equalsIgnoreCase("DEFAULT"))
				r.defaultRaceRelations(parseInt(vals.get(1)));
			else
				r.baseRelations(vals.get(0), parseInt(vals.get(1)));
		}
	}
	private void parseShipDesignMods(Race r, List<String> mods) {
		int maxMods = r.shipDesignModsSize();

		if (mods.size() > maxMods)
			err("Too many ship design mods specified for "+r.name());
		for (int i=0; i<mods.size(); i++) {
			float val = parseFloat(mods.get(i));
			if (i < maxMods)
				r.shipDesignMods(i, val);
		}
	}
	private void parseRaceAvailableFlags(Race r, String input) {
		List<String> vals = this.substrings(input, ',');
		if (vals.size() < 2)
			return;
		// field #1 is available to player
		r.availablePlayer(!vals.get(0).equalsIgnoreCase("no"));

		// field #2 is available to AI
		r.availableAI(!vals.get(1).equalsIgnoreCase("no"));
	}
}
