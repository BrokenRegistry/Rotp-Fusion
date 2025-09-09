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

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import rotp.Rotp;
import rotp.model.game.DynOptions;
import rotp.util.Base;
import rotp.util.LabelManager;
import rotp.util.LanguageManager;

public final class SpeciesManager implements Base, Serializable {
	private	static final long serialVersionUID	= 1L;
	private	static final String RACE_LIST_FILE	= "races/listing.txt";
	private	static final String CUSTOM_FOLDER	= "CustomSpecies";
	public	static final String SPECIES_EXT		= ".race";
	private	static final String CUSTOM_RACE_DESCRIPTION	= "CUSTOM_RACE_DESCRIPTION";
	private	static SpeciesManager instance;
	final RaceFactory factory	= new RaceFactory();
	private	final SpeciesMap baseSpeciesMap		= new SpeciesMap();
	private	final SpeciesMap customSpeciesMap	= new SpeciesMap();

	// =========================================================
	// Constructors and initializers
	//
	private SpeciesManager() {}

	public static SpeciesManager current()	{
		if (instance == null)
			instance = new SpeciesManager();
		return instance;
	}
	public void init()	{
		loadBaseDataFiles();
		loadCustomSpeciesDataFiles();
	}
	public void resetRaceLangFiles()	{
		for (Race r : baseSpecies())
			r.raceLabels().resetDialogue();
	}
	public void loadRaceLangFiles(String langDir)	{
		for (Race r : baseSpecies()) {
			r.raceLabels().resetDialogue();
			loadRaceLangFiles(r, langDir);
		}
		//loadCustomSpeciesLangFiles(langDir);
	}
	File customLangFolder(Race cs)	{
		File langFolder = new File(Rotp.jarPath() + "/" + getLangFolder(cs));
		if (!langFolder.exists() || !langFolder.isDirectory())
			return null;
		return langFolder;
	}
	File customNamesFile(Race cs)	{
		File langFolder = customLangFolder(cs);
		if (langFolder == null)
			return null;
		String filename = langFolder+cs.lalelsFileName(LabelManager.NAMES_FILE);
		File namesFile = new File(filename);
		if (namesFile.exists())
			return namesFile;
		return null;
	}
	public void loadCustomSpeciesLangFiles()	{
		String langDir = LanguageManager.selectedLanguageDir();
		for (Race cs : customSpecies()) {
			String folderName = getLangFolder(cs);
			File langFolder = new File(Rotp.jarPath() + "/" + folderName);
			if (!langFolder.exists() || !langFolder.isDirectory())
				continue;
			System.out.println("Custom species target language folder: " + langFolder);

			// TODO BR: Continue
			cs.raceLabels().resetDialogue();
			factory.loadRaceLangFiles(cs, langDir, folderName);
			String root = CustomRaceDefinitions.ROOT;
			DynOptions opts = cs.raceOptions();
			System.out.println("Desc1: " + opts.getString(root + "RACE_DESC_1")); // TODO BR: REMOVE
			System.out.println("Desc2: " + opts.getString(root + "RACE_DESC_2"));
			System.out.println("Desc3: " + opts.getString(root + "RACE_DESC_3"));
			System.out.println("Desc4: " + opts.getString(root + "RACE_DESC_4"));
			opts.setString(root + "RACE_DESC_1", cs.getDescription1());
			opts.setString(root + "RACE_DESC_2", cs.getDescription2());
			opts.setString(root + "RACE_DESC_3", cs.getDescription3());
			opts.setString(root + "RACE_DESC_4", cs.getDescription4());
			System.out.println("Desc1: " + opts.getString(root + "RACE_DESC_1"));
			System.out.println("Desc2: " + opts.getString(root + "RACE_DESC_2"));
			System.out.println("Desc3: " + opts.getString(root + "RACE_DESC_3"));
			System.out.println("Desc4: " + opts.getString(root + "RACE_DESC_4"));
		}
	}
	public void loadBaseDataFiles()	{
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
	public void loadCustomSpeciesDataFiles()	{
		File[] fileList = loadCustomSpeciesList();
		if (fileList == null || fileList.length == 0)
			return;

		for (File file : fileList) {
			DynOptions opt = CustomRaceDefinitions.loadOptions(file);
			if (opt.size() == 0)
				System.err.println("Empty race file: " + file.getName());
			else {
				Race cs = CustomRaceDefinitions.optionToAlienRace(opt);
				System.out.println("add custom specie to list: " + cs.setupName + " " + cs.id());
				addCustomSpecies(cs);
			}
		}
		loadCustomSpeciesLangFiles();
	}
	public void loadRaceLangFiles(Race r, String langDir)	{
		String dir = "lang/" + langDir + "/races/";
		factory.loadRaceLangFiles(r, langDir, dir);
	}
//	public Race reloadRaceDataFile(String raceDirPath)		{ return factory.reloadRaceDataFile(raceDirPath); }

	// =========================================================
	// Main Getters
	//
	public Race keyed(String s)		{
		Race race = baseSpeciesMap.get(s);
		if (race == null)
			race = customSpeciesMap.get(s);
		if (race == null) { // BR: Add custom race if missing
			System.err.println("SpeciesManager.keyed(" + s + ") is not available");
//			race = keyToRace(s);
//			race.isCustomRace(true);
//			race.setDescription4(race.text(CUSTOM_RACE_DESCRIPTION));
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

	// =========================================================
	// Public Methods
	//
	boolean isBaseSpecies(String key)	{ return baseSpeciesMap.contains(key); }
	boolean isCustomSpecies(String key)	{ return customSpeciesMap.contains(key); }
	public String customSpeciesFolder()	{ return Rotp.jarPath() + "/" + CUSTOM_FOLDER + "/"; }
	public List<Race> baseSpecies()		{ return baseSpeciesMap.getList(); }
	public List<Race> customSpecies()	{ return customSpeciesMap.getList(); }
	public List<String> getAllCustomSpecies()	{
		List<String> list = new ArrayList<>();
		for (Race abilities : customSpecies())
			list.add(abilities.id());
		return list;
	}
	public List<String> getAllowedAlienCustomSpecies()	{
		List<String> list = new ArrayList<>();
		for (Race abilities : customSpecies())
			if (abilities.availableAI())
				list.add(abilities.id());
		return list;
	}

	// =========================================================
	// Local Methods, do not make public
	//
	Race addBaseSpecies(Race r)			{ return baseSpeciesMap.put(r); }
	Race addCustomSpecies(Race r)		{ return customSpeciesMap.put(r); }

	// =========================================================
	// private Methods
	//
	private void loadCustomSpeciesFiles()	{
		log("Loading Custom Species: ", CUSTOM_FOLDER);
		for (Path dir : getCustomSpeciesSubfolders()) {
			System.out.println("CustomSpecies subfolder: " + dir.getFileName());
		}
	}
	private List<Path> getCustomSpeciesSubfolders()	{
		List<Path> folders = listFolderIn(Rotp.jarPath() + "/" + CUSTOM_FOLDER);
		if (!folders.isEmpty())
			folders.remove(0); // the container
		return folders;
	}
	private List<Path> listFolderIn(String folder)	{
		final List<Path> folders = new ArrayList<>();
		try {
			Path dir = Paths.get(folder);
			folders.addAll(Files.find(dir, 1, (path, attrs) 
					-> attrs.isDirectory())
					.collect(Collectors.toList()));
			return folders;
		}
		catch (IOException e) {}
		return folders;
	}
	private File[] loadCustomSpeciesList() {
		File speciesDir = new File(customSpeciesFolder());
		boolean exist = speciesDir.exists();
		boolean isDir = speciesDir.isDirectory();
		System.out.println("customSpeciesFolder(): " + customSpeciesFolder()); // TODO BR: REMOVE
		System.out.println("exist: " + exist + " isDir: " + isDir);
		FilenameFilter filter = (File dir, String name1) -> name1.toLowerCase().endsWith(SPECIES_EXT);
		File[] fileList = speciesDir.listFiles(filter);
		return fileList;
	}
	private String getLangFolder(Race r)	{ return CUSTOM_FOLDER + "/" + r.id() + "/"; }
}

final class SpeciesMap extends HashMap<String, Race> {
	private static final long serialVersionUID = 1L;
	Race put(Race race)			{ return put(race.id(), race); }
	List<Race> getList()		{ return new ArrayList<>(values()); }
	boolean contains(String s)	{ return containsKey(s); }
}
