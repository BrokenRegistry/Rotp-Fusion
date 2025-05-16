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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rotp.model.game.DynOptions;
import rotp.util.Base;

public final class SpeciesManager implements Base, Serializable {
	private static final long serialVersionUID = 1L;
	private static final String CUSTOM_RACE_DESCRIPTION	= "CUSTOM_RACE_DESCRIPTION";
	private static final String RACE_LIST_FILE	= "races/listing.txt";
	private static SpeciesManager instance;
	private static boolean initialized = false;
	final RaceFactory factory	= new RaceFactory();
	private final SpeciesMap baseSpeciesMap		= new SpeciesMap();
	private final SpeciesMap customSpeciesMap	= new SpeciesMap();

	public static SpeciesManager instance()	{
		if (instance == null)
			instance = new SpeciesManager();
		return instance;
	}
	private SpeciesManager() {}
	
	public void resetRaceLangFiles()	{
		if (!initialized)
			loadBaseDataFiles();
		for (Race r : baseSpecies())
			r.raceLabels().resetDialogue();
	}
	public void loadBaseDataFiles()		{
		log("Loading Races: ", RACE_LIST_FILE);
		BufferedReader in = reader(RACE_LIST_FILE);
		if (in == null)
			return;
		try {
			String input;
			while ((input = in.readLine()) != null)
				factory.loadRaceDataFile(input.trim());
			in.close();
		}
		catch (IOException e) {
			err("RaceFactory.loadRaces -- IOException: ", e.toString());
		}
	}
	public void loadRaceLangFiles(String langDir)			{
		for (Race r : baseSpecies()) {
			r.raceLabels().resetDialogue();
			loadRaceLangFiles(r, langDir);
		}
	}
	public void loadRaceLangFiles(Race r, String langDir)	{ factory.loadRaceLangFiles(r, langDir); }
	public Race reloadRaceDataFile(String raceDirPath)		{ return factory.reloadRaceDataFile(raceDirPath); }

	public Race keyed(String s)		{
		Race race = baseSpeciesMap.get(s);
		if (race == null) { // BR: Add custom race if missing
			race = keyToRace(s);
			race.isCustomRace(true);
			race.setDescription4(race.text(CUSTOM_RACE_DESCRIPTION));
		}
		return race;
	}
	public Race keyed(String s, DynOptions options)	{
		Race race = baseSpeciesMap.get(s);
		if (race == null) { // BR: get the custom race
			race = getAlienRace(s, options);
			race.isCustomRace(true);
		}
		return race;
	}
	boolean isBaseSpecies(String key)	{ return baseSpeciesMap.contains(key); }
	boolean isCustomSpecies(String key)	{ return customSpeciesMap.contains(key); }
	Race addBaseSpecies(Race r)			{ return baseSpeciesMap.put(r); }
	Race addCustomSpecies(Race r)		{ return customSpeciesMap.put(r); }
	public List<Race> baseSpecies()		{ return baseSpeciesMap.getList(); }
	public List<Race> customSpecies()	{ return customSpeciesMap.getList(); }
}

final class SpeciesMap extends HashMap<String, Race> {
	private static final long serialVersionUID = 1L;
	Race put(Race race)			{ return put(race.id(), race); }
	List<Race> getList()		{ return new ArrayList<>(values()); }
	boolean contains(String s)	{ return containsKey(s); }
}
