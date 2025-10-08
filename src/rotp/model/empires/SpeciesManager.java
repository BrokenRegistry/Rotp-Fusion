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
package rotp.model.empires;

import static rotp.model.empires.CustomRaceDefinitions.getAlienRace;
import static rotp.model.empires.CustomRaceDefinitions.keyToRace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import rotp.model.game.DynOptions;
import rotp.util.Base;

public class SpeciesManager implements Base, Serializable {
	private static final long serialVersionUID = 1L;
	private static final String CUSTOM_RACE_DESCRIPTION	= "CUSTOM_RACE_DESCRIPTION";

	private Map<String, Race> raceMap = new HashMap<>();
	boolean isValidKey(String s) {
		return raceMap.get(s) != null;
	}
	public Race keyed(String s) {
		Race race = raceMap.get(s);
		if (race == null) { // BR: Add custom race if missing
			race = keyToRace(s);
			race.isCustomRace(true);
			race.setDescription4(race.text(CUSTOM_RACE_DESCRIPTION));
		}
		return race;
	}
	public Race keyed(String s, DynOptions options) {
		Race race = raceMap.get(s);
		if (race == null) { // BR: get the custom race
			race = getAlienRace(s, options);
			race.isCustomRace(true);
		}
		return race;
	}
	void addRace(Race r) { raceMap.put(r.id(), r);}
	public List<Race> races() {
		List<Race> races = new ArrayList<>();
		races.addAll(raceMap.values());
		return races;
	}
	public Map<String, String> baseRacesNamesMap()	{
		HashMap<String, String> map = new HashMap<>();
		for (Entry<String, Race> r : raceMap.entrySet()) {
			map.put(r.getKey(), r.getValue().setupName());
		}
		return map;
	}
}
