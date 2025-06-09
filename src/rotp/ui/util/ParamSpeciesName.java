/*
 * Copyright 2015-2020 Ray Fowler
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rotp.ui.util;

import rotp.model.empires.Race;
import rotp.model.empires.SpeciesManager;
import rotp.util.LanguageManager;
import rotp.util.sound.SoundManager;

public class ParamSpeciesName extends ParamString {
	
	private String id = "en"; // = language directory; Default = English
	
	// ===== Constructors =====
	//
	/**
	 * @param gui    The label header
	 * @param langID The language id
	 * @param name   The name
	 * @param defaultValue The default value
	 */
	public ParamSpeciesName(String gui, String langId, String name, String defaultValue) {
		super(gui, name, defaultValue);
		id = langId;
		isCfgFile(true);
	}
	/**
	 * default constructor for English language
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 */
	public ParamSpeciesName(String gui, String name, String defaultValue) {
		this(gui, "en", name, defaultValue);
	}
	// ===== Public Methods =====
	//
	public String[] getValid()		{
		int rightArraySize = rightArraySize();
		String[] strArray = getArray();
		if (strArray.length != rightArraySize)
			return null;
		for (int i=0; i<rightArraySize; i++) {
			String s = strArray[i].trim();
			if (s.isEmpty())
				return null;
			strArray[i] = s;
		}
		return strArray;
	}
	public boolean newName()		{ return getValid() != null; }
	public String getEmpire()		{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "en":
				return strArray[0];
			case "fr":
				return strArray[0];
			default:
				return "";
		}
	}
	public String getEmpireOf()		{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "fr":
				return strArray[1];
			default:
				return "";
		}
	}
	public String getRace()			{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "en":
				return strArray[1];
			case "fr":
				return strArray[2];
			default:
				return "";
		}
	}
	public String getRaceAdjec()	{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "fr":
				return strArray[2];
			default:
				return "";
		}
	}
	public String getRaceAdjecF()	{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "fr":
				return strArray[3];
			default:
				return "";
		}
	}
	public String getRacePlural()	{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "en":
				return strArray[2];
			case "fr":
				return strArray[4];
			default:
				return "";
		}
	}
	public String getRacePluralNoun()	{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "fr":
				return strArray[4];
			default:
				return "";
		}
	}
	public String getRacePluralNounOf()	{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "fr":
				return strArray[5];
			default:
				return "";
		}
	}
	public String getRacePluralNounTo()	{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "fr":
				return strArray[6];
			default:
				return "";
		}
	}
	public String getRacePluralNounAdjec()	{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "fr":
				return strArray[7];
			default:
				return "";
		}
	}
	public String getRacePluralNounAdjecF()	{
		String[] strArray = getValid();
		if (strArray == null)
			return null;
		switch (id) {
			case "fr":
				return strArray[8];
			default:
				return "";
		}
	}

	// ===== Overriders =====
	//
	@Override public boolean isValidValue()	{
		String[] strArray = getArray();
		return strArray.length == 0 || newName(strArray);
	}

	@Override public String	set(String val)	{
		super.set(val);
		String[] strArray = getArray();
		if (strArray.length == 0 || newName(strArray)) {
			String key = getCfgLabel();
			String suffix = "_" + id.toUpperCase();
			if (key.endsWith(suffix))
				key = key.replace(suffix, "");
			Race race = SpeciesManager.current().keyed(key);
			LanguageManager.current().reloadRace(race);
		}
		else {
			misClick();
		}
		return val;
	}
	// ===== Private Methods =====
	//
	private void misClick()		{ SoundManager.current().playAudioClip("MisClick"); }
	private String[] getArray()	{ return get().split(","); }
	private boolean newName(String[] strArray)	{
		int rightArraySize = rightArraySize();
		if (strArray.length != rightArraySize)
			return false;
		for (int i=0; i<rightArraySize; i++)
			if (strArray[i].trim().isEmpty())
				return false;
		return true;
	}
	private int rightArraySize() {
		switch (id) {
			case "en":
				return 3;
			case "fr":
				return 9;
			default:
				return -1;
		}
	}
}
