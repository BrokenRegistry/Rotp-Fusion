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

import static rotp.ui.util.IParam.langLabel;
import static rotp.ui.util.IParam.rowsSeparator;
import static rotp.ui.util.IParam.tableFormat;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import rotp.model.game.IDebugOptions;
import rotp.model.game.IGameOptions;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.game.BaseCompactOptionsUI;
import rotp.ui.game.BaseModPanel;
import rotp.ui.options.AllSubUI;


public class ParamSubUI extends AbstractParam<SafeListPanel> {
	
	private final String GUI_TITLE_ID;
	private final String GUI_ID;
	private SafeListPanel optionsMap;
	private final SafeListParam optionsList;
	private int unSeen = 0;
	
	// ===== Constructors =====
	//
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param guiTitleID Label for the GUI Title
	 * @param guiId Unique GUI ID for load and save
	 */
	public ParamSubUI(String gui, String name,
			String guiTitleID, String guiId)
	{
		super(gui, name, null);
		GUI_TITLE_ID	= gui + guiTitleID;
		GUI_ID			= guiId;
		optionsMap = AllSubUI.getHandle(guiId).optionsMap();
		optionsList		= new SafeListParam(GUI_ID);
		for (SafeListParam list : optionsMap) {
			for (IParam param : list) {
				if (param != null && !param.isTitle())
					optionsList.add(param);
			}
		}
	}
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param guiTitleID Label for the GUI Title
	 * @param guiID Unique GUI ID for load and save
	 */
	public ParamSubUI(String gui, String guiId) {
		this(gui, guiId+"_UI", guiId+"_TITLE", guiId);
	}
	// ===== Overriders =====
	//
	@Override public ParamSubUI isValueInit(boolean is) { super.isValueInit(is) ; return this; }
	@Override public ParamSubUI isDuplicate(boolean is) { super.isDuplicate(is) ; return this; }
	@Override public ParamSubUI isCfgFile(boolean is)	{ super.isCfgFile(is)   ; return this; }
	@Override public boolean isDefaultValue()	{ 
		boolean is = true;
		for (IParam param : optionsList)
			if (param != null)
				is &= param.isDefaultValue();
		return is;
	}
	@Override public void copyOption(IGameOptions src, IGameOptions dest,
									boolean updateTool, int cascadeSubMenu) {
		super.copyOption(src, dest, updateTool, cascadeSubMenu);
		for (IParam param : optionsList) {
			if (param == null)
				continue;
			if (cascadeSubMenu<=0 && param instanceof ParamSubUI)
				continue;
			if (!param.isCfgFile())
				param.copyOption(src, dest, updateTool, cascadeSubMenu-1);
		}
	}
	@Override public void updateOptionTool() {
		super.updateOptionTool();
		for (IParam param : optionsList)
			if (param != null)
				param.updateOptionTool();
	}
	@Override public void setFromDefault(boolean excludeCfg, boolean excludeSubMenu) {
		super.setFromDefault(excludeCfg, excludeSubMenu);
		for (IParam param : optionsList)
			if (param != null)
				if (!(excludeCfg && param.isCfgFile())
						&& !(excludeSubMenu && param.isSubMenu()))
					param.setFromDefault(excludeCfg, excludeSubMenu);
	}
	@Override protected SafeListPanel getOptionValue(IGameOptions options) { return last(); }
	@Override protected void setOptionValue(IGameOptions options, SafeListPanel value) {}
	@Override public void setFromCfgValue(String val) {
		for (IParam param : optionsList)
			if (param != null && !param.isCfgFile())
				param.setFromCfgValue(val);
	}
	@Override public int getUnseen()	{ return unSeen; }
	@Override public boolean next()		{ return false; }
	@Override public boolean prev()		{ return false; }
	@Override public boolean toggle(MouseWheelEvent e) { return false; }
	@Override public boolean toggle(MouseEvent e, BaseModPanel frame) { return false; }
	@Override public boolean toggle(MouseEvent e, String p, BaseModPanel pUI) {
		updated(true);
		BaseCompactOptionsUI ui = RotPUI.getOptionPanel();
		ui.initUI(GUI_TITLE_ID, GUI_ID);//, optionsMap);
		ui.start(p, pUI);
		return false;
	};
	@Override public String guideValue()	{
		String label = isDefaultValue()? "SUB_UI_DEFAULT_YES" : "SUB_UI_DEFAULT_NO";
		return langLabel(label);
	}
	@Override public String getCfgValue()	{ return super.getCfgValue(); }
	@Override public String getCfgLabel()	{ return super.getCfgLabel(); }
	@Override public boolean isSubMenu()	{ return true; }
	@Override public String getHeadGuide()	{ return headerHelp(true); }
	@Override public String	getGuide()	{
		String help = getHeadGuide();
		help += getTableHelp();
		return help;
	}

	// ===== Other Methods =====
	//
	public void hovering(BaseModPanel pUI, Rectangle location)	{
		updated(true);
		BaseCompactOptionsUI ui = RotPUI.getOptionPanel();
		ui.initUI(GUI_TITLE_ID, GUI_ID, true, location);
		ui.start("", pUI);
	}
	public void start(BasePanel pUI)	{
		updated(true);
		BaseCompactOptionsUI ui = RotPUI.getOptionPanel();
		ui.initUI(GUI_TITLE_ID, GUI_ID);//, optionsMap);
		ui.start("", pUI);
	}
	public String titleId() { return GUI_TITLE_ID; }
	public SafeListParam optionsList() { return optionsList; }
	public void updateList() {
		optionsList.clear();
		for (SafeListParam list : optionsMap) {
			for (IParam param : list) {
				if (param != null && !param.isTitle())
					optionsList.add(param);
			}
		}
	}
	public void newMap(SafeListPanel newMap) {
		optionsMap.clear();
		optionsMap.addAll(newMap);
		updateList();
	}
	public void unseen(int count)	{ unSeen = count; }

	// ===== Private Methods =====
	//
	private String getTableHelp()		{
		List<String> rowList = getRowList();
		int size = rowList.size();
		int maxSize = 30;
		int lim = Math.min(maxSize, size);
		String rows = "";
		if (size>0) {
			if (IDebugOptions.showVIPPanel.get()) {
				rows = "(0) " + rowList.get(0);
				for (int i=1; i<size; i++)
					rows += rowsSeparator() + "(" + i + ") " + rowList.get(i);
			}
			else {
				rows = rowList.get(0);
				for (int i=1; i<lim; i++)
					rows += rowsSeparator() + rowList.get(i);
				if (maxSize==lim)
					rows += rowsSeparator() + "<b>...</b>";
			}
		}
		return tableFormat(rows);
	}
	private List<String> getRowList() {
		List<String> strList = new ArrayList<>();
		for ( IParam param : optionsMap.getListNoTitle()) {
			if (param.isSubMenu()) {
				strList.add("<b>" + param.getGuiDisplay() + "</b>");
			}
			else
				strList.add(param.getGuiDisplay());
		}
		return strList;
	}
}
