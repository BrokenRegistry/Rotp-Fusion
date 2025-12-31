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

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import rotp.model.game.DynamicOptions;
import rotp.ui.game.ShowCustomRaceUI;
import rotp.util.Base;

public class SettingString extends SettingBase<String> implements Base{
	
	private String randomStr = "Randomized";
//	private String inputMessage = "Enter the value";
	
	// ========== constructors  and Initializers ==========
	//
	/**
	 * @param guiLangLabel  The label header
	 * @param nameLangLabel The nameLangLabel
	 * @param defaultvalue The default value
	 */
	public SettingString(String guiLangLabel, String nameLangLabel, String defaultValue, int lineNum) {
		super(guiLangLabel, nameLangLabel, defaultValue, false, true, true);
		hasNoCost(true);
		bulletHFactor(lineNum);
		// Fake list needed for bullet aspect
		for (int i=0; i<lineNum; i++)
			put("", "");
		defaultIndex(0);
		initOptionsText();
		getToolTip(); // to init the list
	}
	public SettingString(String guiLangLabel, String nameLangLabel, String defaultValue) {
		super(guiLangLabel, nameLangLabel, defaultValue, false, true, true);
		hasNoCost(true);
		bulletHFactor(1);
		// Fake list needed for bullet aspect
		put(defaultValue, defaultValue);
		defaultIndex(0);
		set(defaultValue);
		initOptionsText();
		getToolTip(); // to init the list
	}

//	protected void inputMessage(String inputMessage) {
//		this.inputMessage = inputMessage;
//	}
	protected void randomStr(String randomStr) {
		this.randomStr = randomStr;
	}
	// ===== Overriders =====
	//
	@Override public boolean isDefaultValue() {
		return defaultValue().equals(settingValue());
	}
	@Override public void setFromCfgValue(String cfgValue) {
		set(cfgValue);
	}
	@Override public void updateOption(DynamicOptions destOptions) {
		if (!isSpacer() && destOptions != null)
			destOptions.setString(dynOptionIndex(), settingValue());
	}
	@Override public void updateOptionTool(DynamicOptions srcOptions) {
		if (srcOptions != null && !isSpacer())
			set(srcOptions.getString(dynOptionIndex(), defaultValue()));
	}
	@Override public boolean next() {
		Object prev = UIManager.get("OptionPane.minimumSize");
		UIManager.put("OptionPane.minimumSize", new Dimension(scaled(800),scaled(90)));
		String msg = getInputMessage();
		String input;
		input = JOptionPane.showInputDialog(settingText().panel() ,msg, settingValue()); // TODO BR: Improve aspect
		UIManager.put("OptionPane.minimumSize", prev); 
		if (input == null)
			return false; // cancelled
		set(input);
		settingToSkill(ShowCustomRaceUI.displayedSpecies().getRawRace());
		updateGui();
		return false;
	}
	@Override public void setRandom(float min, float max, boolean gaussian) {
		set(randomStr);
	}
	@Override protected String randomize(float rand) { return randomStr; }
	@Override public int index()			{ return -1; } // to get default color
	@Override public void setFromDefault(boolean excludeCfg, boolean excludeSubMenu) {
		if (excludeCfg && isCfgFile())
			return;
		set(defaultValue());
	}
	@Override public boolean prev()			{ return next(); }
	@Override public void optionalInput()	{ next(); }
	@Override public String getGuiDisplay()	{ return getLabel() + END; }
	@Override public String guiCostOptionStr(int idx) {
		return optionValue(idx);
	}
	@Override void settingToolTip(String settingToolTip) {
		super.settingToolTip(settingToolTip);
		resetOptionsToolTip();
	}
	@Override public void formatData(Graphics g, int maxWidth) {
		List<String> lines = wrappedLines(g, settingValue(), maxWidth);
		clearLists();
		int lim = min(lines.size(), bulletHeightFactor());
		for (int i=0; i<lim; i++) {
			put(lines.get(i), getToolTip());
		}
		for (int i=lim; i<bulletHeightFactor(); i++) {
			put("", getToolTip());
		}		
	}
	@Override public SettingBase<?> index(int newIndex) { return this; }
	@Override void resetOptionsToolTip() {
		clearLists();
		for (int i=0; i<bulletHeightFactor(); i++) {
			put("", getToolTip());
		}		
	}
	// ===== Other Methods =====
	//
	public String settingValue(int item)			{ return settingValue(); }
	public void selectedValue(int item, String val)	{ selectedValue(val); }
}
