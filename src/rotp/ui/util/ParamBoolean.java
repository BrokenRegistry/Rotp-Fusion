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

import static rotp.ui.util.IParam.labelFormat;
import static rotp.ui.util.IParam.langHelp;
import static rotp.ui.util.IParam.realLangLabel;
import static rotp.ui.util.IParam.rowFormat;
import static rotp.ui.util.IParam.rowsSeparator;
import static rotp.ui.util.IParam.tableFormat;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import rotp.model.game.IGameOptions;
import rotp.ui.BasePanel;

public class ParamBoolean extends AbstractParam<Boolean> {
	
	// ===== Constructors =====
	//
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 */
	public ParamBoolean(String gui, String name, Boolean defaultValue) {
		super(gui, name, defaultValue);
	}
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 * @param isValueInit Always initialized by the last value
	 */
	public ParamBoolean(String gui, String name, Boolean defaultValue, boolean isValueInit) {
		super(gui, name, defaultValue);
		isValueInit(isValueInit);
	}
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 * @param isValueInit Always initialized by the last value
	 * @param isCfgFile Saved in Remnant.cfg
	 */
	public ParamBoolean(String gui, String name, Boolean defaultValue, boolean isValueInit, boolean isCfgFile) {
		super(gui, name, defaultValue);
		isValueInit(isValueInit);
		isCfgFile(isCfgFile);
	}

	// ===== Overriders =====
	//
	@Override public ParamBoolean isValueInit(boolean is) { super.isValueInit(is) ; return this; }
	@Override public ParamBoolean isDuplicate(boolean is) { super.isDuplicate(is) ; return this; }
	@Override public ParamBoolean isCfgFile(boolean is)	  { super.isCfgFile(is)   ; return this; }
	@Override public ParamBoolean formerName(String link) { super.formerName(link); return this; }
	@Override public ParamBoolean setDefaultValue(String key, Boolean value) {
		super.setDefaultValue(key, value);
		return this;
	}
	@Override public ParamBoolean setUpdateParameters(IUpdated method, String id)	{
		super.setUpdateParameters(method, id);
		return this;
	}
	
	@Override public String	getFullHelp()		{ return getHeadGuide() + getTableHelp(); }
	@Override public String	valueGuide(int id)	{ return getTableHelp(); }
	@Override public int	getIndex()			{ return get()? 1 : 0; }
	@Override public String	getCfgValue(Boolean value)		 { return yesOrNo(value); }
	@Override public void	setFromCfgValue(String newValue) { setFromCfg(yesOrNo(newValue)); }	
	@Override public String	guideValue()				{ return guiValue(get()); }
	@Override public String	guideDefaultValue()			{ return guiValue(defaultValue()); }
	@Override public boolean prev()						{ return next(); }
	@Override public boolean next()						{ set(!get()); return false; }
	@Override public boolean toggle(MouseWheelEvent e)	{ return next(); }
	@Override public boolean toggle(MouseEvent e, BasePanel frame) {
		if (getDir(e) == 0)
			setFromDefault(false, true);
		else
			return next();
		return false;
	}
	@Override protected Boolean getOptionValue(IGameOptions options) {
		Boolean value = options.dynOpts().getBoolean(getLangLabel());
		//System.out.println("getOptionValue " + getCfgLabel() + " " + value);
		if (value == null)
			if (formerName() == null)
				value = creationValue();
			else
				value = options.dynOpts().getBoolean(formerName(), creationValue());
		return value;
	}
	@Override protected void setOptionValue(IGameOptions options, Boolean value) {
		options.dynOpts().setBoolean(getLangLabel(), value);
		//System.out.println("setOptionValue " + getCfgLabel() + " " + value);
	}
	// ===== Other Methods =====
	//
	public	boolean	toggle()				{ return next(); }
	
	private String	getRowHelp(boolean b)	{
		String help  = valueHelp(b);
		String label = labelFormat(guiValue(b));
		return rowFormat(label, help);
	}
	private String	getTableHelp()			{
		String rows = getRowHelp(true) + rowsSeparator() + getRowHelp(false);
		return tableFormat(rows);
	}
	private	String	valueHelp(boolean b)	{
		String label = getValueLabel(b);
		String help = langHelp(label);
		if (help == null || help.isEmpty())
			help = defaultHelp(b);
		return help;
	}
	private	String	guiValue(boolean b)		{
		String label = getValueLabel(b);
		String value = realLangLabel(label);
		if (value == null)
			value = defaultGuiVal(b);
		return value;
	}
	private String defaultGuiVal(boolean b)	{ return realLangLabel(defaultLabel(b)); }
	private String defaultHelp(boolean b)	{ return langHelp(defaultLabel(b)); }
	private String getValueLabel(boolean b) { return getLangLabel() + valExt(b); }
	private String valExt(boolean b)		{ return b? "_YES" : "_NO"; }
	private String defaultLabel(boolean b)	{ return b? "BOOLEAN_YES" : "BOOLEAN_NO"; }
}
