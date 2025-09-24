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

import static rotp.model.game.IBaseOptsTools.MOD_UI;


public class ParamOptions extends ParamList {
	
	public static final String DEFAULT	= "Default";
	public static final String LAST		= "Last";
	public static final String USER		= "User";
	public static final String GAME		= "Game";
	
	private static final IndexableMap labelsOptions = new IndexableMap();
	static {
		labelsOptions.put(GAME,		MOD_UI + "OPTIONS_GAME");
		labelsOptions.put(LAST,		MOD_UI + "OPTIONS_LAST");
		labelsOptions.put(USER,		MOD_UI + "OPTIONS_USER");
		labelsOptions.put(DEFAULT,	MOD_UI + "OPTIONS_DEFAULT");
	}

	// ===== Constructors =====
	//
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 */
	public ParamOptions(String gui, String name, String defaultValue) {
		super(gui, name, defaultValue, labelsOptions);
		showFullGuide(true);
	}
	/**
	 * GUI = MOD_UI
	 * @param name The name
	 * @param defaultValue The default value
	 */
	public ParamOptions(String name, String defaultValue) {
		this(MOD_UI, name, defaultValue);
	}
	/**
	 * GUI = MOD_UI, defaultValue = Auto
	 * @param name The name
	 * @param TechCategory The category index from
	 */
	public ParamOptions(String name) {
		this(MOD_UI, name, LAST);
	}
	@Override public ParamOptions isValueInit(boolean is) { super.isValueInit(is) ; return this; }
	@Override public ParamOptions isDuplicate(boolean is) { super.isDuplicate(is) ; return this; }
	@Override public ParamOptions isCfgFile(boolean is)	  { super.isCfgFile(is)   ; return this; }
	@Override public ParamOptions setUpdateParameters(IUpdated method, String id)	{
		super.setUpdateParameters(method, id);
		return this;
	}

	// ===== Specific Public Methods =====
	//
	public boolean isDefault()	{ return get().equalsIgnoreCase(DEFAULT); }
	public boolean isLast()		{ return get().equalsIgnoreCase(LAST); }
	public boolean isUser()		{ return get().equalsIgnoreCase(USER); }
	public boolean isGame()		{ return get().equalsIgnoreCase(GAME); }
}
