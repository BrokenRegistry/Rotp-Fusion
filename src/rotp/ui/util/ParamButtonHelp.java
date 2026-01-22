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

import static rotp.ui.util.IParam.baseSeparator;
import static rotp.ui.util.IParam.htmlTuneFont;
import static rotp.ui.util.IParam.langDesc;
import static rotp.ui.util.IParam.langHelp;
import static rotp.ui.util.IParam.langName;
import static rotp.ui.util.IParam.realLangLabel;
import static rotp.util.Base.NEWLINE;

import java.util.EnumMap;

import rotp.util.ModifierKeysState;


public class ParamButtonHelp implements IParam<String> {
	
	protected EnumMap<ModifierKeysState, String>  nameMap = new EnumMap<>(ModifierKeysState.class);
	protected EnumMap<ModifierKeysState, Boolean> showMap = new EnumMap<>(ModifierKeysState.class);
	private final String	name;
	// ===== Constructors =====
	//
	public ParamButtonHelp(String label, String base, String shift, String ctrl, String ctrlShift) {
		this(label, base, true, shift, true, ctrl, true, ctrlShift, true);
	}
	public ParamButtonHelp(String label,
			String base,	  boolean showBase,
			String shift,	  boolean showShift,
			String ctrl,	  boolean showCtrl,
			String ctrlShift, boolean showCtrlShift) {
		name	= label;
		nameMap.put(ModifierKeysState.NONE, base);
		nameMap.put(ModifierKeysState.SHIFT, shift);
		nameMap.put(ModifierKeysState.CTRL, ctrl);
		nameMap.put(ModifierKeysState.CTRL_SHIFT, ctrlShift);
		showMap.put(ModifierKeysState.NONE, showBase);
		showMap.put(ModifierKeysState.SHIFT, showShift);
		showMap.put(ModifierKeysState.CTRL, showCtrl);
		showMap.put(ModifierKeysState.CTRL_SHIFT, showCtrlShift);
	}
	protected String helpLine(ModifierKeysState state, int html) {
		String help, line;
		String label = nameMap.get(state);
		if (label == null || label.isEmpty() || !showMap.get(state))
			return "";
		if (html == 1)
			help = langDesc(label);
		else
			help = langHelp(label);
		if (help.isEmpty())
			help = "---";
		if (html == 0) {
			line = state.helpLine;
			line += "[" + langName(label) + "]";
			line += NEWLINE + help;
			return line;
		}
		line = htmlTuneFont(-2, state.helpLine);
		line += "&nbsp <b>( "+ langName(label) + " )</b>";
		line += NEWLINE + help;
		return line;
	}
	private String buildHelp(int html) {
		String result = "";
		String help = "";
		for( ModifierKeysState state: nameMap.keySet()) {
			help = helpLine(state, html);
			if (!help.isEmpty())
				if (result.isEmpty())
					result = help;
				else if (html == 0)
					result += NEWLINE + NEWLINE + help;
				else
					result += baseSeparator() + help;
		}
		return result;
	}
	public	String getKey() { return nameMap.get(ModifierKeysState.get()); }
	public	String getKey(ModifierKeysState state) { return nameMap.get(state); }
	public	String[] getKeys() {
		return (String[]) nameMap.values().toArray(new String[nameMap.size()]);
	}

	// ===== Overriders =====
	//
	@Override public String getGuide()		{ return buildHelp(1); } // html
	@Override public String getFullHelp()	{ return buildHelp(2); } // html
	@Override public String getHelp()		{ return buildHelp(0); } // Full for old Help
	@Override public String getCfgLabel()	{ return name; }
	@Override public String getLangLabel()	{ return name; }
	@Override public String getToolTip()	{ return realLangLabel(getKey() + LABEL_DESCRIPTION); }

	@Override public void updateOptionTool()	{}
}
