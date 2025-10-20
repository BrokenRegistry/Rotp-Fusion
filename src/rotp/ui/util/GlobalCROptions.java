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

public class GlobalCROptions extends ParamString {

	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 */
	public GlobalCROptions(String gui, String name, String defaultValue)	{
		super(gui, name, defaultValue);
	}

	public StringList getBaseOptions()	{
		StringList list = new StringList();
		for (SpecificCROption opt : SpecificCROption.values()) {
			if(!opt.isSelection() && !opt.isUserChoice())
				list.add(opt.value);
		}
		return list;
	}
	public SpecificCROption getEnu()	{ return SpecificCROption.set(get()); }

	public boolean isBaseRace()			{ return SpecificCROption.isBaseRace(get()); }
	public boolean isSelection()		{ return SpecificCROption.isSelection(get()); }
	public boolean isReworked()			{ return SpecificCROption.isReworked(get()); }
	public boolean isPlayer()			{ return SpecificCROption.isPlayer(get()); }
	public boolean isRandom()			{ return SpecificCROption.isRandom(get()); }
	public boolean isFilteredFiles()	{ return SpecificCROption.isFilteredFiles(get()); }
	public boolean isAllFiles()			{ return SpecificCROption.isAllFiles(get()); }
	public boolean isFilesAndRaces()	{ return SpecificCROption.isFilesAndRaces(get()); }
	public boolean isAll()				{ return SpecificCROption.isAll(get()); }
}
