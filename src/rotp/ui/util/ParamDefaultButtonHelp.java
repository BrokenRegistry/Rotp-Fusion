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

import static rotp.ui.util.IParam.htmlTuneFont;
import static rotp.ui.util.IParam.langDesc;
import static rotp.ui.util.IParam.langHelp;
import static rotp.ui.util.IParam.langName;
import static rotp.ui.util.IParam.realLangLabel;
import static rotp.util.Base.NEWLINE;

import rotp.model.game.IMainOptions;
import rotp.util.ModifierKeysState;


public class ParamDefaultButtonHelp extends ParamButtonHelp {
	
	// ===== Constructors =====
	//
	public ParamDefaultButtonHelp(String label, String base, String shift, String ctrl, String ctrlShift) {
		this(label, base, true, shift, true, ctrl, true, ctrlShift, true);
	}
	public ParamDefaultButtonHelp(String label,
			String base,	  boolean showBase,
			String shift,	  boolean showShift,
			String ctrl,	  boolean showCtrl,
			String ctrlShift, boolean showCtrlShift) {
		super(label, base, showBase, shift, showShift, ctrl, showCtrl, ctrlShift, showCtrlShift);
	}

	private String selectedDefaultAddon() {
		String addonKey = IMainOptions.defaultSettings.get();
		String addon = realLangLabel(addonKey + "_NOW");
		return addon;
	}
	// ===== Overriders =====
	//
	@Override protected String helpLine(ModifierKeysState state, int html) {
		String help, line;
		String label = nameMap.get(state);
		if (label == null || label.isEmpty() || !showMap.get(state))
			return "";
		if (html == 1)
			help = langDesc(label);
		else
			help = langHelp(label);

		if (label.equals("SETTINGS_LOCAL_DEFAULT") || label.equals("SETTINGS_GLOBAL_DEFAULT"))
			help += selectedDefaultAddon();

		if (help.isEmpty())
			help = "---";
		if (html == 0) {
			line = state.helpLine;
			line += "[" + langName(label) + "]";
			line += NEWLINE + help;
			return line;
		}
		line = "<dl><dt>" + htmlTuneFont(-2, state.helpLine);
		line += "&nbsp <b>( "+ langName(label) + " )</b></dt><dd>";
		line += help + "</dd></dl>";
		return line;
	}
	@Override public String getToolTip()	{ return super.getToolTip() + selectedDefaultAddon(); }
}
