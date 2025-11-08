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

import static rotp.ui.util.IParam.langLabel;
import static rotp.ui.util.IParam.realLangLabel;
import static rotp.ui.util.IParam.rowsSeparator;
import static rotp.ui.util.IParam.tableFormat;

import rotp.model.game.DynamicOptions;

public class SettingBoolean extends SettingBase<Boolean> {
	
	private static final boolean defaultIsList			= true;
	private static final boolean defaultIsBullet		= false;
	private static final boolean defaultLabelsAreFinals	= true;
	private static final String  defaultBooleanYes		= "BOOLEAN_YES";
	private static final String  defaultBooleanNo		= "BOOLEAN_NO";

	// ========== constructors ==========
	//
	/**
	 * @param guiLangLabel  The label header
	 * @param nameLangLabel The nameLangLabel
	 * @param defaultvalue The default value
	 * @param costTrue 
	 * @param costFalse 
	 */
	public SettingBoolean(String guiLangLabel, String nameLangLabel, Boolean defaultValue, Boolean showFullGuide) {
		this(guiLangLabel, nameLangLabel, defaultValue, 0f, 0f, "YES", "NO", false, false);
		hasNoCost(true);
		showFullGuide(showFullGuide);
	}

	/**
	 * @param guiLangLabel  The label header
	 * @param nameLangLabel The nameLangLabel
	 * @param defaultvalue The default value
	 * @param costTrue 
	 * @param costFalse 
	 */
	public SettingBoolean(String guiLangLabel, String nameLangLabel, Boolean defaultValue) {
		this(guiLangLabel, nameLangLabel, defaultValue, 0f, 0f,
				defaultBooleanYes, defaultBooleanNo,
				defaultIsBullet, defaultLabelsAreFinals);
		hasNoCost(true);
	}
	/**
	 * @param guiLangLabel  The label header
	 * @param nameLangLabel The nameLangLabel
	 * @param defaultvalue The default value
	 * @param costTrue 
	 * @param costFalse 
	 */
	public SettingBoolean(String guiLangLabel, String nameLangLabel, Boolean defaultValue,
			float costTrue, float costFalse) {
		this(guiLangLabel, nameLangLabel, defaultValue, costTrue, costFalse,
				defaultBooleanYes, defaultBooleanNo,
				defaultIsBullet, defaultLabelsAreFinals);
	}
	/**
	 * @param guiLangLabel  The label header
	 * @param nameLangLabel The nameLangLabel
	 * @param defaultvalue The default value
	 * @param costTrue 
	 * @param costFalse 
	 * @param langLabelYes
	 * @param langLabelNo
	 * @param isBullet		To be displayed as bullet list
	 * @param labelsAreFinals when false: Labels are combined withName and Gui Label
	 */
	private SettingBoolean(String guiLangLabel, String nameLangLabel, Boolean defaultValue,
			float costTrue, float costFalse, String langLabelYes, String langLabelNo,
			boolean isBullet, boolean labelsAreFinals) {
		super(guiLangLabel, nameLangLabel, defaultValue, defaultIsList, isBullet, labelsAreFinals);
		put("No",  langLabelNo,  costFalse, false);
		put("Yes", langLabelYes, costTrue,  true);		
	}
	// ===== Overriders =====
	//
	@Override public String guideValue() 				{ return langLabel(guiOptionLabel()); }
	@Override public String	guideDefaultValue()			{ return guiValue(defaultValue()); }
	@Override public String guiOptionValue(int index)	{ return langLabel(guiOptionLabel(index)); }
	@Override public void updateOptionTool()			{
		if (!isSpacer() && dynOpts() != null)
			set(dynOpts().getBoolean(getLangLabel(), defaultValue()));
	}
	@Override public void updateOption(DynamicOptions options) {
		if (!isSpacer() && options != null)
			options.setBoolean(getLangLabel(), settingValue());
	}
	@Override public void updateOptionTool(DynamicOptions options) {
		if (!isSpacer() && options != null)
			set(options.getBoolean(getLangLabel(), defaultValue()));
	}
	@Override protected String getTableHelp()	{
		int size = listSize();
		String rows = "";
		if (size>0) {
			rows = getRowGuide(0);
			for (int i=1; i<size; i++)
				rows += rowsSeparator() + getRowGuide(i);
		}
		return tableFormat(rows);
	}
	private	String	guiValue(boolean b)		{
		String label = getValueLabel(b);
		String value = realLangLabel(label);
		if (value == null)
			value = defaultGuiVal(b);
		return value;
	}
	private String defaultGuiVal(boolean b)	{ return realLangLabel(defaultLabel(b)); }
	private String getValueLabel(boolean b) { return getLangLabel() + valExt(b); }
	private String valExt(boolean b)		{ return b? "_YES" : "_NO"; }
	private String defaultLabel(boolean b)	{ return b? "BOOLEAN_YES" : "BOOLEAN_NO"; }
}
