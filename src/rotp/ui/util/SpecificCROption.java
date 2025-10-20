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

package rotp.ui.util;

public enum SpecificCROption {
	
	SELECTION	("'Selection'"),
	BASE_RACE	("'Original Species'"),
	REWORKED	("'Reworked'"),
	PLAYER		("'Player'"),
	RANDOM		("'Random'"),
	RANDOM_BASE	("'Random 10'"),
	RANDOM_MOD	("'Random 16'"),
	FILES_FLT	("'Files'"),
	FILES_NO_FLT("'All Files'"),
	FILES_RACES	("'Files Races'"),
	ALL			("'All'"),
	USER_CHOICE	("''");

	public final String value;

	private SpecificCROption(String opt) { value = opt; }

	public static SpecificCROption set(String opt) {
		for (SpecificCROption crO: values())
			if (opt.equals(crO.value))
				return crO;
		return USER_CHOICE;
	}
	public static StringList options() {
		StringList list = new StringList();
		for (SpecificCROption opt: values())
			list.add(opt.value);
		list.removeLast();
		return list;
	}
	public static SpecificCROption defaultSpecificValue() { return SELECTION; }

	public boolean isBaseRace()		 { return this == BASE_RACE;  }
	public boolean isSelection()	 { return this == SELECTION; }
	public boolean isReworked()		 { return this == REWORKED; }
	public boolean isPlayer()		 { return this == PLAYER; }
	public boolean isRandom()		 { return this == RANDOM; }
	public boolean isFilteredFiles() { return this == FILES_FLT; }
	public boolean isAllFiles()		 { return this == FILES_NO_FLT; }
	public boolean isFilesAndRaces() { return this == FILES_RACES; }
	public boolean isAll()			 { return this == ALL; }
	public boolean isUserChoice()	 { return this == USER_CHOICE; }

	static boolean isBaseRace(String opt)		{
		return (opt.equals(BASE_RACE.value) || opt.equalsIgnoreCase("'Base Race'"));
	}
	static boolean isSelection(String opt)		{ return opt.equals(SELECTION.value); }
	static boolean isReworked(String opt)		{ return opt.equals(REWORKED.value); }
	static boolean isPlayer(String opt)			{ return opt.equals(PLAYER.value); }
	static boolean isRandom(String opt)			{ return opt.equals(RANDOM.value); }
	static boolean isFilteredFiles(String opt)	{ return opt.equals(FILES_FLT.value); }
	static boolean isAllFiles(String opt)		{ return opt.equals(FILES_NO_FLT.value); }
	static boolean isFilesAndRaces(String opt)	{ return opt.equals(FILES_RACES.value); }
	static boolean isAll(String opt)			{ return opt.equals(ALL.value); }
	static boolean isUserChoice(String opt) {
		for (SpecificCROption crO: values())
			if (opt.equals(crO.value))
				return false;
		return true;
	}
}
