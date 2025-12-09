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

import rotp.ui.util.StringList;

public enum SpecificCROption {

	SELECTION		("'Selection'"),
	ORIGINAL_SPECIES("'Original Species'"),
	REWORKED		("'Reworked'"),
	REWORKED_FULL	("'Reworked Full'"),
	PLAYER			("'Player'"),
	RANDOM			("'Random'"),
	RANDOM_10		("'Random 10'"),
	RANDOM_16		("'Random 16'"),
	FILES_FLT		("'Files'"),
	ALL_FILES		("'All Files'"),
	FILES_RACES		("'Files Races'"),
	ALL				("'All'"),
	USER_SELECTION	("''");

	public final String value;
	private SpecificCROption(String opt) { value = opt;}
	private	static final String LABEL_CONVERTER_KEY	= "ABILITIES_";

	public static String getLabel(String value)	{
		for (SpecificCROption opt: values())
			if (opt.value.equals(value))
				return(LABEL_CONVERTER_KEY + opt.name());
		System.err.println("ERROR no such value in SpecificCROption.getLabel(string value): " + value);
		return value;
	}
	public static SpecificCROption set(String opt) {
		for (SpecificCROption crO: values())
			if (opt.equals(crO.value))
				return crO;
		return USER_SELECTION;
	}
	public static StringList getSpecificOptions() {
		StringList list = new StringList();
		for (SpecificCROption opt: values())
			if(!opt.isUserChoice())
				list.add(opt.value);
		return list;
	}
	public static StringList getGlobalOptions() {
		StringList list = new StringList();
		for (SpecificCROption opt: values())
			if(!opt.isSelection() && !opt.isUserChoice())
				list.add(opt.value);
		return list;
	}
	public static SpecificCROption defaultSpecificValue() { return SELECTION; }

	public boolean isBaseRace()		 { return this == ORIGINAL_SPECIES;  }
	public boolean isSelection()	 { return this == SELECTION; }
	public boolean isReworked()		 { return this == REWORKED; }
	public boolean isPlayer()		 { return this == PLAYER; }
	public boolean isRandom()		 { return this == RANDOM; }
	public boolean isFilteredFiles() { return this == FILES_FLT; }
	public boolean isAllFiles()		 { return this == ALL_FILES; }
	public boolean isFilesAndRaces() { return this == FILES_RACES; }
	public boolean isAll()			 { return this == ALL; }
	public boolean isUserChoice()	 { return this == USER_SELECTION; }

	static boolean isBaseRace(String opt)		{ // with backward compatibility
		return (opt.equals(ORIGINAL_SPECIES.value) || opt.equalsIgnoreCase("'Base Race'"));
	}
	static boolean isSelection(String opt)		{ return opt.equals(SELECTION.value); }
	static boolean isReworked(String opt)		{ return opt.equals(REWORKED.value); }
	static boolean isPlayer(String opt)			{ return opt.equals(PLAYER.value); }
	static boolean isRandom(String opt)			{ return opt.equals(RANDOM.value); }
	static boolean isFilteredFiles(String opt)	{ return opt.equals(FILES_FLT.value); }
	static boolean isAllFiles(String opt)		{ return opt.equals(ALL_FILES.value); }
	static boolean isFilesAndRaces(String opt)	{ return opt.equals(FILES_RACES.value); }
	static boolean isAll(String opt)			{ return opt.equals(ALL.value); }
	static boolean isUserChoice(String opt) {
		for (SpecificCROption crO: values())
			if (opt.equals(crO.value))
				return false;
		return true;
	}
}
