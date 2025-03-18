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

import static rotp.model.game.IMainOptions.minListSizePopUp;
import static rotp.ui.util.IParam.langLabel;
import static rotp.ui.util.IParam.rowsSeparator;
import static rotp.ui.util.IParam.tableFormat;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import rotp.model.game.IDebugOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.RotPUI;
import rotp.ui.game.BaseModPanel;

public class ParamList extends AbstractParam<String> {

	private final IndexableMap valueLabelMap;
	private boolean showFullGuide = false;
	private int	refreshLevel = 0;
	private int	boxPosX	= -1;
	private int	boxPosY	= -1;
	
	// ===== Constructors =====
	//
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultCfgLabel The default CfgLabel
	 */
	public ParamList(String gui, String name, String defaultCfgLabel) {
		super(gui, name, defaultCfgLabel);
		valueLabelMap = new IndexableMap();
	}
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultCfgLabel The default CfgLabel
	 * @param list keys for map table
	 * @param header The label Header
	 */
/*	public ParamList(String gui, String name, String defaultCfgLabel, ArrayList<String> list, String mid) {
		super(gui, name, defaultCfgLabel);
		valueLabelMap = new IndexableMap();
		for (String element : list)
			put(element, mid + element.toUpperCase());
	} */
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultCfgLabel The default CfgLabel
	 * @param optionLabelMap  existing IndexableMap
	 */
	public ParamList(String gui, String name, String defaultCfgLabel, IndexableMap optionLabelMap) {
		super(gui, name, defaultCfgLabel);
		this.valueLabelMap = optionLabelMap;
	}
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param list keys for map table
	 * @param defaultIndex index to the default value
	 * @param isDuplicate if true the option already exist 
	 */
/*	public ParamList(String gui, String name, List<String> list, int defaultIndex, boolean isDuplicate) {
		super(gui, name, list.get(defaultIndex));
		isDuplicate(isDuplicate);
		valueLabelMap = new IndexableMap();
		for (String element : list)
			put(element, element); // Temporary; needs to be further initialized
	} */
	/**
	 * Initializer for Duplicate
	 * @param gui  The label header
	 * @param name The name
	 * @param list keys for map table
	 * @param defaultIndex index to the default value
	 */
/*	public ParamList(String gui, String name, List<String> list, int defaultIndex) {
		super(gui, name, list.get(defaultIndex));
		isDuplicate(true);
		valueLabelMap = new IndexableMap();
		for (String element : list)
			put(element, element); // Temporary; needs to be further initialized
	} */
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param list keys for map table
	 * @param defaultIndex index to the default value
	 * @param isDuplicate if true the option already exist 
	 */
/*	public ParamList(String gui, String name, List<String> list, String defaultValue, boolean isDuplicate) {
		super(gui, name, defaultValue);
		isDuplicate(isDuplicate);
		valueLabelMap = new IndexableMap();
		for (String element : list)
			put(element, element); // Temporary; needs to be further initialized
	} */
	/**
	 * Initializer for Duplicate
	 * @param gui  The label header
	 * @param name The name
	 * @param list keys for map table
	 * @param defaultIndex index to the default value
	 */
	public ParamList(String gui, String name, List<String> list, String defaultValue) {
		super(gui, name, defaultValue);
		isDuplicate(true);
		valueLabelMap = new IndexableMap();
		for (String element : list)
			put(element, element); // Temporary; needs to be further initialized
	}
	/**
	 * Initializer for Duplicate Dynamic (shape options)
	 * @param gui  The label header
	 * @param name The name
	 */
	public ParamList(String gui, String name) {
		super(gui, name, "");
		isDuplicate(true);
		valueLabelMap = new IndexableMap();
	}
	
	// ===== Initializers =====
	//
	public ParamList showFullGuide(boolean show) { showFullGuide = show; return this; }
	public ParamList refreshLevel(int level)	 { refreshLevel = level; return this; }
 	public void setPosition (int x, int y)		 { boxPosX = x; boxPosY = y; }
	public void reInit(List<String> list)		 {
		valueLabelMap.clear();
		for (String element : list)
			put(element, langLabel(element)); // "text" should now be available
	}
	// ===== Overriders =====
	//
	@Override public ParamList forcedRefresh(boolean b)	{ super.forcedRefresh(b); return this; }
	@Override public ParamList isValueInit(boolean is)	{ super.isValueInit(is) ; return this; }
	@Override public ParamList isDuplicate(boolean is)	{ super.isDuplicate(is) ; return this; }
	@Override public ParamList isCfgFile(boolean is)	{ super.isCfgFile(is)   ; return this; }
	@Override public ParamList setDefaultValue(String key, String value) {
		super.setDefaultValue(key, value);
		return this;
	}

	@Override public boolean isActive()					{ return listSize()>0; }
	@Override public String guideDefaultValue()			{ return name(defaultValueIndex()); }
	@Override public String getCfgValue(String value)	{ return validateValue(value); }
	@Override public String	guideValue()				{
		int index = getIndex();
		if (index >= listSize())
			return "";
		return name(index);
	}
	@Override public LinkValue linkValue(String val) 	{ return new LinkValue(val); } 
	@Override protected String linkValue(LinkValue val) { return val.stringValue(); }
	@Override public boolean isInvalidLocalValue(String value)	{ return !valueLabelMap.isValid(value); }
	@Override protected boolean isInvalidLocalMax(String value)	{ return isInvalidLocalValue(value); }
	@Override protected boolean isInvalidLocalMin(String value)	{ return isInvalidLocalValue(value); }
	@Override public boolean next()						{
		set(valueLabelMap.getNextLangLabelIgnoreCase(get()));
		return false;
	}
	@Override public boolean prev()						{
		set(valueLabelMap.getPrevValueIgnoreCase(get())); 
		return false;
	}
	@Override public boolean toggle(MouseWheelEvent e)	{
		if (getDir(e) > 0)
			return next();
		else 
			return prev();
	}
	@Override public boolean toggle(MouseEvent e, BaseModPanel frame){
		if (getDir(e) == 0)
			setFromDefault(false, true);
		else if (frame != null && 
				(e.isControlDown() || listSize() >= minListSizePopUp.get()))
			setFromList(frame);
		else if (getDir(e) > 0)
			return next();
		else 
			return prev();
		return false;
	}
	@Override public void	setFromCfgValue(String newCfgValue)	{
		setFromCfg(validateValue(newCfgValue));
	}
	@Override public int	getIndex()				{ return getValidIndex(); }
	@Override public String	setFromIndex(int id)	{
		return super.set(valueLabelMap.getCfgValue(getValidIndex(id)));
	}
	@Override public String	getGuiValue(int id)		{
		return langLabel(valueLabelMap.getLangLabel(getValidIndex(id)));
	}
	@Override public String	getGuide()				{
		if(showFullGuide)
			return getFullHelp();
		return super.getGuide();
	}
	@Override public String	getFullHelp()			{
		String help = getHeadGuide();
		help += getTableHelp();
		return help;
	}
	@Override public String getSelectionStr()		{ return getValueStr(getIndex(get())); }
	@Override public String getValueStr(int id)		{ return valueGuide(getValidIndex(id)); }
	@Override public String valueGuide(int id) 		{ return tableFormat(getRowGuide(id)); }
	@Override public String getLangLabel(int id)	{
		if (id<0 || id >= listSize())
			return "";
		if (isDuplicate())
			return valueLabelMap.getCfgValue(id);
		else
			return valueLabelMap.getLangLabel(getValidIndex(id));
	}
	@Override protected String getOptionValue(IGameOptions options) {
		String value = options.dynOpts().getString(getLangLabel());
		if (value == null)
			if (formerName() == null)
				value = creationValue();
			else
				value = options.dynOpts().getString(formerName(), creationValue());
		return value;
	}
	@Override protected void setOptionValue(IGameOptions options, String value) {
		options.dynOpts().setString(getLangLabel(), value);
	}
	@Override protected Boolean getDirectionOfChange(String before, String after) {
		float valBefore	= getIndex(before);
		float valAfter	= getIndex(after);
		if (valAfter > valBefore)
			return GO_UP;
		if (valAfter < valBefore)
			return GO_DOWN;
		return null;
	}
	// ===== Other Protected Methods =====
	//
	protected int refreshLevel()		 { return refreshLevel; }
	protected int getIndex(String value) {
		if (isDuplicate()) {
			int idx = valueLabelMap.getLangLabelIndexIgnoreCase(value);
			if (idx == -1)
				return valueLabelMap.getValueIndexIgnoreCase(value);
			return idx;
		}
		else
			return valueLabelMap.getValueIndexIgnoreCase(value);
	}
	protected String getLangLabelFromValue(String newValue) {
		String newLangLabel = valueLabelMap.getLangLabelFromValue(newValue);
		return newLangLabel;
	}
	protected String getMapValue(int id) { return valueLabelMap.cfgValueList.get(id); }
	// ===== Other Public Methods =====
	//
	public String getLangLabelKey() {
		int index = getIndex();
		if (index >= listSize())
			return "";
		return valueLabelMap.getLangLabel(getValidIndex(index));
	}
	public String getLangLabel(String keyExt) { return langLabel(getLangLabelKey() + keyExt); }
	public List<String> getOptions()	{
		List<String> list = new ArrayList<String>();
		if (isDuplicate()) // Values and labels are swap because values may be redundant
			list.addAll(valueLabelMap.langLabelList);
		else
			list.addAll(valueLabelMap.cfgValueList);
		return list;
	}
	public IndexableMap getOptionLabelMap()	{ return valueLabelMap; }
	/**
	 * Add a new Option with its Label
	 * @param option
	 * @param label
	 * @return this for chaining purpose
	 */
	public ParamList put(String option, String label) {
		valueLabelMap.put(option, label);
		return this;
	}
	// ===== Private Methods =====
	//
	private void initGuiTexts() {
		int id = defaultValueIndex();
		initMapGuiTexts();
		if (id >= 0)
			defaultValue(valueLabelMap.cfgValueList.get(id));
	}
	/**
	 * Check if the entry is valid and return a valid value
	 * @param key the entry to check
	 * @return a valid value, preferably the value to test
	 */
	private String validateValue(String key)	{
		if (valueLabelMap.valuesContainsIgnoreCase(key))
			return key;
		if (valueLabelMap.valuesContainsIgnoreCase(defaultValue()))
			return defaultValue();
		return valueLabelMap.getCfgValue(0);
	}
	protected int listSize()					{ return valueLabelMap.listSize(); }
	private String currentOption()				{
		int index = Math.max(0, getIndex());
		return valueLabelMap.guiTextList.get(index);
	}
	private void setFromList(BaseModPanel frame){
		String message	= "<html>" + getGuiDescription() + "</html>";
		String title	= langLabel(getLangLabel(), "");
		initGuiTexts();
		String[] list= valueLabelMap.guiTextList.toArray(new String[listSize()]);
		int height = 128 + (int)Math.ceil(18.5 * list.length);
		height = Math.max(300, height);
		height = Math.min(350, height);
		ListDialog dialog = RotPUI.listDialog();
		dialog.init(
				frame,	frame.getParent(),	// Frame & Location component
				message, title,				// Message & Title
				list, currentOption(),		// List & Initial choice
				null, true,					// long Dialogue & isVertical
				boxPosX, boxPosY,			// Position
				RotPUI.scaledSize(350), RotPUI.scaledSize(height),	// size
				null,						// Font
				frame,						// Preview
				valueLabelMap.cfgValueList,	// Alternate return
				this); 						// Parameter

		String input = (String) dialog.showDialog(refreshLevel);
		if (input != null && valueLabelMap.getValueIndexIgnoreCase(input) >= 0)
			set(input);
	}
	private String getTableHelp()				{
		int size = listSize();
		String rows = "";
		if (size>0) {
			if (IDebugOptions.showVIPPanel.get()) {
				rows = "(0) " + getRowGuide(0);
				for (int i=1; i<size; i++)
					rows += rowsSeparator() + "(" + i + ") " + getRowGuide(i);
			}
			else {
				rows = getRowGuide(0);
				for (int i=1; i<size; i++)
					rows += rowsSeparator() + getRowGuide(i);
			}
		}
		return tableFormat(rows);
	}
	private void initMapGuiTexts()				{
		valueLabelMap.guiTextList.clear();
		for (String label : valueLabelMap.langLabelList)
			valueLabelMap.guiTextList.add(langLabel(label));
	}
	
	protected int defaultValueIndex()			{
		int id = getIndex(defaultValue());
		if (id < 0)
			if (listSize()==0)
				return -1;
			else
				return 0;
		return id;
	}
	public int	getRawIndex()					{
		String value = get();
		if (isDuplicate()) {
			int idx = valueLabelMap.getLangLabelIndexIgnoreCase(value);
			if (idx == -1)
				return valueLabelMap.getValueIndexIgnoreCase(value);
			else
				return idx;
		}
		else
			return valueLabelMap.getValueIndexIgnoreCase(value);
	}
	private int getValidIndex()					{
		int id = getRawIndex();
		if (id < 0)
			id = defaultValueIndex();
		return id;		
	}
	private int getValidIndex(int id)			{
		if (id < 0)
			return getValidIndex();
		return id;
	}
	//========== Nested class ==========
	//
	public static class IndexableMap{
		
		private final SafeList cfgValueList	 = new SafeList(); // also key list
		private final SafeList langLabelList = new SafeList();
		private final SafeList guiTextList	 = new SafeList();

		
		// ========== Constructors and Initializers ==========
		//
		public IndexableMap() {}
		private void clear () {
			cfgValueList.clear();
			langLabelList.clear();
		}
		// ========== Setters ==========
		//
		public void put(String option, String label) {
			cfgValueList.add(option);
			langLabelList.add(label);
		}
		// ========== Getters ==========
		//
		private boolean isValid(String option)	{ return cfgValueList.contains(option); }
		private int	   validId(int id)	{
			if (id < 0)
				return 0;
			if (id >= listSize())
				return 0;
			return id;
		}
		private int    listSize()			{ return cfgValueList.size(); }
		private String getCfgValue(int id)	{ return cfgValueList.get(validId(id)); }
		private String getLangLabel(int id)	{ return langLabelList.get(validId(id)); }
		private String getLangLabelFromValue(String value) {
			int index = getValueIndexIgnoreCase(value);
			return langLabelList.get(index);
		}
//		/**
//		 * get the value from the langLabel
//		 * @param langLabel The langLabel to search
//		 * @return the corresponding value
//		 */
//		private String getValueFromLangLabel(String langLabel) {
//			int index = getLangLabelIndexIgnoreCase(langLabel);
//			return cfgValueList.get(index);
//		}
		/**
		 * search for value regardless of the case and return the previous key
		 * @param value The value to search
		 * @return the previous value, looping at the beginning, the last if string is not found
		 */
		private String getPrevValueIgnoreCase(String value) {
			int index = getValueIndexIgnoreCase(value)-1;
			if (index < 0)
				return cfgValueList.get(cfgValueList.size()-1);
			return cfgValueList.get(index);
		}
		/**
		 * search for value regardless of the case and return the next key
		 * @param value The value to search
		 * @return the next value, looping at the end, the first if string is not found
		 */
		private String getNextLangLabelIgnoreCase(String value) {
			int index = getValueIndexIgnoreCase(value) + 1;
			if (index >= cfgValueList.size())
				return cfgValueList.get(0);
			return cfgValueList.get(index);
		}
		/**
		 * Test if value is part of cfgValue list regardless of the case
		 * @param value The key to search for
		 * @return true if value is found
		 */
		private boolean valuesContainsIgnoreCase(String value) {
			return getValueIndexIgnoreCase(value) != -1;
		}
//		/**
//		 * search for the value position in the guiText list
//		 * @param value The value to search regardless of the case
//		 * @return the value index, -1 if none
//		 */
//		private int getGuiTextIndexIgnoreCase(String value) {
//			int index = 0;
//			for (String entry : guiTextList) {
//				if (entry.equalsIgnoreCase(value))
//					return index;
//				index++;
//			}
//			return -1;
//		}
		/**
		 * search for the value position in the cfgValue list
		 * @param value The value to search regardless of the case
		 * @return the value index, -1 if none
		 */
		private int getValueIndexIgnoreCase(String value) {
			int index = 0;
			for (String entry : cfgValueList) {
				if (entry.equalsIgnoreCase(value))
					return index;
				index++;
			}
			return -1;
		}
		/**
		 * search for the langLabel position in the langLabel list
		 * @param langLabel The label to search regardless of the case
		 * @return the key index, -1 if none
		 */
		private int getLangLabelIndexIgnoreCase(String langLabel) {
			int index = 0;
			for (String entry : langLabelList) {
				if (entry.equalsIgnoreCase(langLabel))
					return index;
				index++;
			}
			return -1;
		}
		class SafeList extends ArrayList<String> {
			private static final long serialVersionUID = 1L;
			@Override public String get(int id) {
				if (id<0 || size() == 0)
					return "";
				if (id>=size())
					return super.get(0);
				return super.get(id);
			}
		}
	}
}
