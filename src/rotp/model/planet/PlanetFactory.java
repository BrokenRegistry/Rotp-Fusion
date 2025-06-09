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
package rotp.model.planet;

import static rotp.model.planet.Planet.creationSizeMax;
import static rotp.model.planet.Planet.creationSizeMin;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import rotp.model.empires.Race;
import rotp.model.empires.Species;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GameSession;
import rotp.model.game.IGameOptions;
import rotp.model.planet.Planet.PlanetBaseData;
import rotp.ui.util.ParamAAN2;
import rotp.util.Base;

public class PlanetFactory implements Base {
    private static final PlanetFactory instance = new PlanetFactory();
    public static PlanetFactory current() { return instance; }
    private static final String planetListFile = "data/planets/listing.txt";
    private static final String planetDataDir  = "data/planets/";

    static { instance.loadDataFiles(); }

    // BR: For Symmetric galaxies and Restart
    public static Planet copyPlanet(StarSystem sys, PlanetBaseData src) {
    	Planet p = new Planet(sys);
        p.initPlanetType(src.planetTypeKey);
        p.copy(src);
        return p;
    }
    // BR: For Symmetric galaxies
    public static Planet copyPlanet(StarSystem sys, Planet src) {
    	Planet p = new Planet(sys);
        p.initPlanetType(src.type().key());
        p.copy(src);
        return p;
    }
    public static Planet createPlanet(StarSystem sys, float bonus) {
        Planet p = instance.options().randomPlanet(sys);
        
        if (p.type().isAsteroids()) {
            p.baseSize(0);
            return p;
        }
        float size = p.type().randomSize();
        if (size == p.type().maxSize()) {
            while (p.random() < .2f)
                size += 5.0f;
        }
        else if (size == p.type().minSize()) {
            while (p.random() < .2f)
                size -= 5.0f;
        }
        
        // IMPORTANT: bound the size between 10 and 120
        // modnar: change planet size with selectedPlanetQualityOption, which changes the bonus value
        // also change bound range (size_min, size_max) with bonus value
        // give all sizes in multiples of 5
        float size_min = (float) (Math.round((creationSizeMin*bonus)/5) * 5);
        float size_max = (float) (Math.round((creationSizeMax*bonus)/5) * 5);
        size = (float) (Math.round((size*bonus)/5) * 5);
        size = Math.max(size_min, Math.min(size, size_max));
        
        p.baseSize(size);
        return p;
    }
    public static Planet createOrion(StarSystem sys, float bonus) {
        Planet p = instance.options().orionPlanet(sys);
        p.setOrionArtifact();
        p.makeEnvironmentFertile();
        p.baseSize(creationSizeMax*bonus);
        return p;
    }
    private static boolean combo(ParamAAN2 aan2, boolean b, boolean isPlayer) {
    	return aan2.isAlways(isPlayer) || (!aan2.isNever(isPlayer) && b);
    }
//    public static Planet createHomeworld(Race r, Race dr, StarSystem sys, // TODO BR: REMOVE
//    		float bonus, boolean isPlayer) { // BR: added player info and dataRace
	public static Planet createHomeworld(Species species, StarSystem sys,
			float bonus, boolean isPlayer) {
		IGameOptions opts = GameSession.instance().options();
		Race race	  = species.race();
		Race dataRace = species.dataRace();
		Planet planet = instance.options().randomPlayerPlanet(dataRace, sys);
		planet.baseSize(dataRace.homeworldSize() * bonus);
		if (race.homeworldKey() > 0)
			planet.terrainSeed(race.homeworldKey());
		// modnar: add option for changing Race homeworld type
		// BR: User control over these settings
		if (dataRace.raceWithUltraPoorHomeworld())
			planet.setResourceUltraPoor();
		if (dataRace.raceWithPoorHomeworld())
			planet.setResourcePoor();
		if (combo(opts.selectedRichHomeworld(),
				dataRace.raceWithRichHomeworld(), isPlayer))
			planet.setResourceRich();
		if (combo(opts.selectedUltraRichHomeworld(),
				dataRace.raceWithUltraRichHomeworld(), isPlayer))
			planet.setResourceUltraRich();
		if (combo(opts.selectedArtifactsHomeworld(),
				dataRace.raceWithArtifactsHomeworld(), isPlayer))
			planet.setArtifactRace();
		if (combo(opts.selectedOrionLikeHomeworld(),
				dataRace.raceWithOrionLikeHomeworld(), isPlayer))
			planet.setOrionRace();
		if (dataRace.raceWithHostileHomeworld())
			planet.makeEnvironmentHostile();
		if (combo(opts.selectedFertileHomeworld(),
				dataRace.raceWithFertileHomeworld(), isPlayer))
			planet.enrichSoil();
		if (combo(opts.selectedGaiaHomeworld(),
				dataRace.raceWithGaiaHomeworld(), isPlayer)) {
			planet.enrichSoil();
			planet.enrichSoil();
		}
		return planet;
	}
    // modnar: add option to start game with additional colonies
    // modnar: use orionPlanet (always Terran), always size-60
    public static Planet createCompanionWorld(StarSystem sys, float bonus) {
        Planet p = instance.options().orionPlanet(sys);
        p.baseSize(60*bonus);
        return p;
    }
    private void loadDataFiles() {
        log("Loading Planet Types...");
        BufferedReader in = reader(planetListFile);
        if (in == null)
            return;

        try {
            String input;
            while ((input = in.readLine()) != null)
                loadPlanetDataFile(input);
            in.close();
        }
        catch (IOException e) {
            err("PlanetFactory.loadDataFiles -- IOException: " + e);
        }
    }
    private void loadPlanetDataFile(String line) {
        if (isComment(line))
            return;

        BufferedReader in = reader(planetDataDir+line.trim());
        if (in == null)
            return;

        try {
            PlanetType newType = new PlanetType();
            String input;
            while ((input = in.readLine()) != null)
                loadPlanetDataLine(newType, input);
            in.close();
            PlanetType.addType(newType);
        }
        catch (IOException e) {
            err("PlanetFactory.loadPlanetDataFile(", line, ") -- IOException: ", e.toString());
        }
    }
    private void loadPlanetDataLine(PlanetType type, String input) {
        if (isComment(input))
            return;

        List<String> keyVal = substrings(input, ':');
        if (keyVal.size() < 2)
            return;

        String key = keyVal.get(0);
        String val = keyVal.get(1);

        switch(key) {
            case "key"            : type.key(val); return;
            case "descBiological" : type.descBiological(val); return;
            case "descSilicoid"   : type.descSilicoid(val); return;
            case "hostility"      : type.hostility(parseInt(val)); return;
            case "terrainImage"   : type.terrainKey(val); return;
            case "panoramaImage"  : type.panoramaKey(val); return;
            case "landscapeImage" : type.landscapeKeys(val); return;
            case "atmoImage"      : type.atmosphereKeys(val); return;
            case "cloudImage"     : type.cloudKeys(val); return;
            case "minSize"        : type.minSize(parseInt(val)); return;
            case "maxSize"        : type.maxSize(parseInt(val)); return;
            case "color"          : parseColorEntry(type, substrings(val, ',')); return;
            case "ship0"          : parseShipValues(type, 0, substrings(val, ',')); return;
            case "ship1"          : parseShipValues(type, 1, substrings(val, ',')); return;
            case "ship2"          : parseShipValues(type, 2, substrings(val, ',')); return;
            case "ship3"          : parseShipValues(type, 3, substrings(val, ',')); return;
        }
        err("unknown key->", input);
    }
    private void parseShipValues(PlanetType pt, int shipNum, List<String> vals) {
        pt.shipX(shipNum, parseInt(vals.get(0)));
        pt.shipY(shipNum, parseInt(vals.get(1)));
        pt.shipW(shipNum, parseInt(vals.get(2)));
    }
    private void parseColorEntry(PlanetType pt, List<String> vals) {
        if (vals.size() < 4)
            return;
        float pct = parseFloat(vals.get(0));
        int r = parseInt(vals.get(1));
        int g = parseInt(vals.get(2));
        int b = parseInt(vals.get(3));
        pt.colorMap().addColorMark(pct, r, g, b);
    }
}
