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

import static rotp.Rotp.rand;
import static rotp.model.game.IMainOptions.minListSizePopUp;
import static rotp.ui.util.IParam.langHelp;
import static rotp.ui.util.IParam.langLabel;
import static rotp.ui.util.IParam.realLangLabel;
import static rotp.ui.util.IParam.rowsSeparator;
import static rotp.ui.util.IParam.tableFormat;
import static rotp.util.Base.textSubs;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import rotp.model.game.DynamicOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.util.ListDialogUI;
import rotp.ui.util.StringList;

public class SettingBase<T> implements ICRSettings<T> {
	
	public enum CostFormula {DIFFERENCE, RELATIVE, NORMALIZED}
	private static final boolean defaultIsList			= true;
	private static final boolean defaultIsBullet		= false;
	private static final boolean defaultLabelsAreFinals	= false;
	private static final String  costFormat				= "%6s ";
	private static final String  INPUT					= "_INPUT";

	private final StringList		cfgValueList = new StringList();
	private final StringList		labelList	 = new StringList();
	private final LinkedList<Float>	costList	 = new LinkedList<>();
	private final LinkedList<T> 	valueList	 = new LinkedList<>();
	private final StringList 		tooltipList  = new StringList();
	private final String nameLabel;
	private final String guiLabel;

	private boolean labelsAreFinals = defaultLabelsAreFinals;
	private boolean isBullet		= defaultIsBullet;
	private boolean allowListSelect	= false;
	private boolean showFullGuide	= false;
	private boolean isSpacer 		= false;
	private boolean hasNoCost		= false;
	private boolean isList			= defaultIsList;
	private int		refreshLevel	= 0;
	private int		bulletHFactor	= 1;
	private int		bulletMax   	= 25;
	private int		bulletStart 	= 0;
	private T		selectedValue	= null;
	private T		defaultValue	= null;
	private String	settingToolTip;
	private float	lastRandomSource;

	private boolean updated = true;
	private int deltaYLines;
	
	// ========== Constructors and initializers ==========
	//
	/**
	 * @param guiLabel		The label header
	 * @param nameLabel		The nameLabel
	 * @param defaultIndex	The default list index
	 * @param isList		Either a list or simple value
	 * @param isBullet		To be displayed as bullet list
	 * @param labelsAreFinals when false: Labels are combined withName and Gui Label
	 */
	SettingBase(String guiLabel, String nameLabel, T defaultValue,
			boolean isList, boolean isBullet, boolean labelsAreFinals) {
		this(guiLabel, nameLabel);
		this.defaultValue	= defaultValue;
		this.isList			= isList;
		this.isBullet		= isBullet;
		this.labelsAreFinals= labelsAreFinals;
	}
	/**
	 * @param guiLabel  The label header
	 * @param nameLabel The nameLabel
	 */
	SettingBase(String guiLabel, String nameLabel) {
		this.guiLabel	= guiLabel;
		this.nameLabel	= nameLabel;
	}
	protected void maxBullet(int maxBullet)	{ bulletMax = maxBullet; }
	void settingToolTip(String tt)			{ settingToolTip = tt; }
	private	void loadSettingToolTip()		{
		settingToolTip = langHelp(getLangLabel());
		if (settingToolTip == null)
			settingToolTip = "";
	}
	void isBullet(boolean isBullet)				{ this.isBullet = isBullet; }
	void allowListSelect(boolean allow)			{ allowListSelect = allow; }
	private	void isList(boolean isList)			{ this.isList = isList; }
	void labelsAreFinals(boolean finals)		{ labelsAreFinals = finals; }
	void showFullGuide(boolean show)			{ showFullGuide = show; }
	protected void bulletHFactor(int factor)	{ bulletHFactor = factor; }
	protected void refreshLevel(int level)		{ refreshLevel  = level; }
	protected String getInputMessage()			{ return text(getLangLabel() + INPUT); }
	@Override public String toString()	{
		String s = getLangLabel() + ": " + selectedValue + " (" + defaultValue + ")";
		return s;
	}
	// ========== Public IParam Interfaces ==========
	//
	@Override public void setFromCfgValue(String cfgValue) {
		int index = cfgValidIndex(cfgValueList.indexOfIgnoreCase(cfgValue));
		selectedValue(valueList.get(index));
	}
	@Override public boolean next() {
		int selectedIndex = cfgValidIndex()+1;
		if (selectedIndex >= cfgValueList.size())
			selectedIndex = 0;
		selectedValue(valueList.get(selectedIndex));
		return false;
	}
	@Override public boolean prev() {
		int selectedIndex = cfgValidIndex()-1;
		if (selectedIndex < 0)
			selectedIndex = cfgValueList.size()-1;
		selectedValue(valueList.get(selectedIndex));	
		return false;
	}
	@Override public boolean toggle(MouseEvent e, MouseWheelEvent w, BasePanel frame) {
		if (e == null)
			return toggle(w);
		else
			return toggle(e, frame);
	}
	@Override public boolean toggle(MouseWheelEvent w) {
		if (getDir(w) > 0)
			return next();
		else 
			return prev();
	}
	@Override public boolean toggle(MouseEvent e, BasePanel frame) {
		if (getDir(e) == 0) 
			setFromDefault(true, true);
		else if (allowListSelect && frame != null && 
				(e.isControlDown() || listSize() >= minListSizePopUp.get()))
			setFromList(frame);
		else if (getDir(e) > 0)
			return next();
		else 
			return prev();
		return false;
	}
	@Override public void setFromDefault(boolean excludeCfg, boolean excludeSubMenu) {
		selectedValue(defaultValue);
	}
	@Override public void updateOption(DynamicOptions destOptions) {
		if (!isSpacer && destOptions != null)
			destOptions.setString(dynOptionIndex(), getCfgValue());
	}
	@Override public void updateOptionTool(DynamicOptions srcOptions) {
		if (!isSpacer && srcOptions != null)
			setFromCfgValue(srcOptions.getString(dynOptionIndex(), getDefaultCfgValue()));
	}
	@Override public void copyOption(IGameOptions src, IGameOptions dest,
									boolean updateTool, int cascadeSubMenu) {
		if (!isSpacer && src != null && dest != null)
			dest.dynOpts().setString(dynOptionIndex(), getCfgValue());
		dest.dynOpts().setString(dynOptionIndex(), src.dynOpts().getString(dynOptionIndex(), getDefaultCfgValue()));
	}
	@Override public String getGuiDisplay(int idx)	{
		String str = text(getLangLabel()); // Get from label.txt
		String[] strArr = str.split(textSubs[0]);

		switch(idx) {
		case 0:
			if (strArr.length > 0)
				return strArr[0];
			else
				return "";
		case 1:
			if (strArr.length > 1)
				return guideValue() + strArr[1];
			else
				return guideValue();
		default:
			return "";
		}
	}
	@Override public String getCfgValue() 			{ return getCfgValue(settingValue()); }
	@Override public String getCfgLabel()			{ return nameLabel; }
	@Override public String getGuiDescription() 	{ return langLabel(descriptionId()); }
	@Override public String guideValue()			{ return String.valueOf(settingValue()); }
	@Override public String getGuiDisplay()			{ return text(getLangLabel(), guideSelectedValue()) + END; }
	@Override public String getToolTip()			{
		if (settingToolTip == null) {
			loadSettingToolTip();
			resetOptionsToolTip();
		}
		return settingToolTip;
	}
	@Override public String getToolTip(int idx) 	{
		if (idx >= tooltipList.size())
			return "";
		String tt = tooltipList.get(idx);
		if (tt == null)
			return "";
		return tt;
	}
	@Override public String guideDefaultValue()		{ return defaultValue.toString(); }
	@Override public boolean isDefaultValue()		{ return defaultValue() == settingValue(); }
	@Override public String getLangLabel()			{ return guiLabel + nameLabel; }
	@Override public String getLangLabel(int id)	{ return labelList.get(valueValidIndex(id)); }
	@Override public int	getIndex()				{ return valueValidIndex(); }
	@Override public String	getGuide()				{
		if(showFullGuide())
			return getFullHelp();
		return ICRSettings.super.getGuide();
	}
	@Override public String	getFullHelp()			{
		String help = getHeadGuide();
		help += getTableHelp();
		return help;
	}
	@Override public String getValueStr(int id)		{ return valueGuide(valueValidIndex(id)); }
	@Override public String valueGuide(int id) 		{ return tableFormat(getRowGuide(id)); }
	@Override public boolean updated()				{ return updated; }
	@Override public void updated(boolean val)		{ updated = val; }

	// ========== Public ICRSettings Interfaces ==========
	//
	@Override public void hasNoCost(boolean hasNoCost)	{ this.hasNoCost = hasNoCost; }
	// return true if needs to repaint
	@Override public float settingCost()	{
		if (isSpacer() || hasNoCost)
			return 0f;;
		return costList.get(costValidIndex());
	}
	@Override public String guiCostOptionStr(int idx)		{ return guiCostOptionStr(idx, 0); }
	@Override public int index()							{ return cfgValidIndex(); }
	@Override public void guiSelect()	{
		if (isSpacer())
			return;
		//settingToSkill(ShowCustomRaceUI.displayedSpecies().getRawRace());
		updateGui();
	}
	@Override public void setRandom(float min, float max, boolean gaussian)	{ set(randomize(min, max, gaussian)); }
	@Override public void setRandom(float rand)		{
		lastRandomSource = rand;
		set(randomize(rand));
	}
	@Override public void setValueFromCost(float cost)	{ set(getValueFromCost(cost)); }
	@Override public int deltaYLines()				{ return deltaYLines; }
	@Override public String guiSettingDisplayStr()	{ return isBullet ? guiSettingLabelCostStr() : guiSettingLabelValueCostStr(); }
	@Override public boolean isSpacer()				{ return isSpacer; }
	@Override public boolean hasNoCost()			{ return hasNoCost; }
	@Override public boolean isBullet()				{ return isBullet; }
	@Override public int bulletStart()				{ return bulletStart; }
	@Override public float lastRandomSource()		{ return lastRandomSource; }
	@Override public float	costFactor()			{
		if (isList) {
			if (lastRandomSource<0)
				return -Collections.min(costList);
			else
				return Collections.max(costList);
		}
		if (settingCost()<0)
			return -Math.min(maxValueCostFactor(), minValueCostFactor());
		else
			return Math.max(maxValueCostFactor(), minValueCostFactor());
	}
	@Override public int bulletBoxSize()			{ return isBullet()? Math.min(listSize(), bulletMax) : 0; }
	// ========== Overridable Methods ==========
	//
	private boolean showFullGuide()			{ return showFullGuide; }
	void resetOptionsToolTip()				{}
	protected String getCfgValue(T value)	{
		if (isList) {
			int index = valueValidIndex(valueList.indexOf(value));
			return cfgValueList.get(index);
		}
		return String.valueOf(value);
	}
	public void optionalInput()	{}
	public void formatData(Graphics g, int maxWidth) {}
	public float maxValueCostFactor() {
		if (isList) {
			return Collections.max(costList);
		}
		return 0f;
	}
	public float minValueCostFactor() {
		if (isList) {
			return Collections.min(costList);
		}
		return 0f;
	}
	public T settingValue() {
		if (selectedValue == null)
			return defaultValue;
		else
			return selectedValue;
	}
	SettingBase<?> set(T newValue) {
		if (isList) {
			selectedValue = newValue;
			selectedValue(valueList.get(valueValidIndex()));
		} else
			selectedValue(newValue);
		return this;
	}
	public SettingBase<?> index(int newIndex) {
		selectedValue(valueList.get(cfgValidIndex(newIndex)));
		return this;
	}
	protected T randomize(float rand) {
		if (isList) {
			if (rand > 0)
				rand *= Collections.max(costList);
			else
				rand *= -Collections.min(costList);				
			return getValueFromCost(rand);
		}
		return null; // Should be overridden
	}
	protected T getValueFromCost(float cost) {
		if (isList) {
			int bestIdx = 0;
			float bestDev =  Math.abs(cost - costList.getFirst());
			for (int i=1; i<costList.size(); i++) {
				float dev = Math.abs(cost - costList.get(i));
				if (dev < bestDev) {
					bestIdx = i;
					bestDev = dev;
				}
			}
			return valueList.get(bestIdx);
		}
		return null; // Should be overridden
	}
	@Override public void selectedValue(T newValue) {
		selectedValue = newValue;
		updated = true;
	}
	protected StringList guiTextsList()		{
		StringList guiTextList = new StringList();
		for (String label : labelList)
			guiTextList.add(langLabel(label));
		return guiTextList;
	}
	protected StringList altReturnList()	{ return cfgValueList; }
	// ========== Setter ==========
	//
	SettingBase<?> defaultIndex(int index)	{
		setDefaultIndex(bounds(0, index, cfgValueList.size()-1));
		return this;
	}
	SettingBase<?> defaultCfgValue(String defaultCfgValue) {
		setDefaultIndex(cfgValidIndex(cfgValueList.indexOfIgnoreCase(defaultCfgValue)));
		return this;
	}
	public String guiOptionValue(int index) { // For List
		return String.valueOf(optionValue(index));
	}
	// ===== Getters =====
	//
	protected String defaultLangLabel()	{ return langLabel(labelList.get(valueValidDefaultIndex())); }
	protected String getSelLangLabel()	{ return langLabel(labelList.get(getIndex())); }
	protected T	defaultValue()			{ return defaultValue; }
	protected String guiOptionLabel()	{ return guiOptionLabel(index()); }
	protected String guiOptionLabel(int index)	{ return langLabel(labelList.get(cfgValidIndex(index))); }
	@Override public String getLabel()	{ return langLabel(getLangLabel()); }
	int	bulletHeightFactor()			{ return bulletHFactor; }
	public	boolean	isDefaultIndex()	{ return cfgValidIndex() == rawDefaultIndex(); }
	int listSize()						{ return valueList.size(); }
	public	StringList getOptions()		{ return new StringList(cfgValueList); }
	public	StringList getLabels()		{ return new StringList(labelList); }
	public	LinkedList<T> getValues()	{
		LinkedList<T> list = new LinkedList<T>();
		list.addAll(valueList);
		return list;
	}
	private	String getFinalLabel(String rawLabel)	{
		if (rawLabel == null)
			return "";
		if (labelsAreFinals)
			return rawLabel;
		return getLangLabel() +"_"+ rawLabel;
	}
	private	void addLabel(String rawLabel)	{ labelList.add(getFinalLabel(rawLabel)); }
	private	String getToolTip(String label, boolean finalKey)	{
		if (label == null || label.isEmpty())
			return "";
		if (finalKey)
			return langLabel(label);
		String tt = langHelp(getFinalLabel(label));
		if (tt == null || tt.isEmpty()) {
			// System.out.println("Missed TT: " + label);
			return "";
		}
		return tt;
	}
	private	void addToolTip(String label, boolean finalKey)		{
		tooltipList.add(getToolTip(label, finalKey));
	}
	public String getLabel(String langDir)	{
		String langLabel = getLangLabel();
		String realLabel = realLangLabel(langLabel);
		if (realLabel != null) {
			if (realLabel.isEmpty())
				realLabel = realLangLabel(langLabel);
			return realLabel;
		}
		String labelEnd = langDir;
		langLabel = StringUtils.removeEnd(langLabel, labelEnd);
		realLabel = realLangLabel(langLabel);
		if (realLabel != null)
			return realLabel;
		return StringUtils.removeEnd(getCfgLabel(), labelEnd);
	}
	// ===== Other Public Methods =====
	//
	/**
	 * Add a new Option with its Label
	 * @param cfgValue
	 * @param langLabel
	 * @param cost
	 * @param value
	 * @return this for chaining purpose
	 */
	void put(String cfgValue, String langLabel, float cost, T value) {
		isList(true);
		cfgValueList.add(cfgValue);
		costList.add(cost);
		valueList.add(value);
		addLabel(langLabel);
		addToolTip(langLabel, false);
	}
	void put(String cfgValue, String langLabel, float cost, T value, String tooltipKey) {
		isList(true);
		cfgValueList.add(cfgValue);
		costList.add(cost);
		valueList.add(value);
		addLabel(langLabel);
		addToolTip(tooltipKey, true);
	}
	void put(T value, String tooltipKey) {
		cfgValueList.add("");
		costList.add(0f);
		valueList.add(value);
		labelList.add("");
		addToolTip(tooltipKey, true);
	}
	protected int getDir(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) return -1;
		if (SwingUtilities.isLeftMouseButton(e)) return 1;
		return 0;
	}
	protected int getDir(MouseWheelEvent e) {
		if (e.getWheelRotation() < 0) return 1;
		return -1;
	}
	protected void clearLists() {
		cfgValueList.clear();
		labelList.clear();
		costList.clear();
		valueList.clear();
		tooltipList.clear();
	}
	protected T optionValue(int index)	{ return valueList.get(valueValidIndex(index)); }
	protected String getTableHelp()		{
		int size = listSize();
		String rows = "";
		if (size>0) {
			rows = getRowGuide(0);
			for (int i=1; i<size; i++)
				rows += rowsSeparator() + getRowGuide(i);
		}
		return tableFormat(rows);
	}
	protected String getDefaultCfgValue()	{ return getCfgValue(defaultValue); }
	// ========== Private Methods ==========
	//
	/**
	 * @param min Limit Value in %
	 * @param max Limit Value in %
	 * @param gaussian yes = smooth edges
	 * @return a randomized value
	 */
	private T randomize(float min, float max, boolean gaussian) {
		if (this.isSpacer)
			return null;
		if (hasNoCost && isList && !valueList.isEmpty()) {
			int rand = rand().nextInt(valueList.size());
			return valueList.get(rand);
		}
		float rand;
		float mini = Math.min(min, max)/100;
		float maxi = Math.max(min, max)/100;
		if (gaussian)
			rand = (maxi + mini + (maxi-mini) * (float) rand().nextGaussian())/2;
		else
			rand = mini + (maxi-mini) * (float) rand().nextFloat();
		lastRandomSource = rand;
		return randomize(rand);
	}
	private float optionCost(int index)	{ return costList.get(index); }
	private String descriptionId()		{ return getLangLabel() + LABEL_DESCRIPTION; }
	private String settingCostString()	{ return settingCostString(1); } // default decimal number
	private String settingCostString(int dec)				{ return costString(settingCost(), dec); }
	private String optionCostStringIdx(int idx, int dec)	{ return costString(optionCost(idx), dec); }
	private String guiSettingLabelCostStr() {
		if (hasNoCost)
			return getLabel();
		return getLabel() + ": " + settingCostString();
	}
	private String guiSettingLabelValueCostStr() {
		if (hasNoCost)
			return getLabel() + ": " + guideSelectedValue();
		return getLabel() + ": " + guideSelectedValue() + " " + settingCostString();
	}
	private String guiCostOptionStr(int idx, int dec) {
		if (hasNoCost)
			return guiOptionLabel(idx);
		String cost = String.format(costFormat,  optionCostStringIdx(idx, dec));
		return cost + guiOptionLabel(idx);
	}
	private void setDefaultIndex(int index) {
		defaultValue = valueList.get(cfgValidIndex(index));
	}
	private String costString(float cost, int dec) {
		String str = "(";
		switch (dec) {
		case 0:
			str += "" + Math.round(cost);
			break;
		case 2:
			str +=  new DecimalFormat("0.00").format(cost);
			break;
		case 3:
			str +=  new DecimalFormat("0.000").format(cost);
			break;
		default:
			str +=  new DecimalFormat("0.0").format(cost);
			break;
		}
		return str + ")";
	}
	private int bounds(int low, int val, int hi)	{ return Math.min(Math.max(low, val), hi); }
	private int cfgValidIndex()				{ return cfgValidIndex(rawSelectedIndex()); }
	private int cfgValidIndex(int index)	{ return cfgValueList.isValidIndex(index)? index : valueValidDefaultIndex(); }
	//private int labelValidIndex(int index)	{ return labelList.isValidIndex(index)? index : valueValidDefaultIndex(); }
	private int valueValidDefaultIndex()	{ return bounds(0, rawDefaultIndex(), valueList.size()-1); }
	private int valueValidIndex()			{ return valueValidIndex(rawSelectedIndex()); }	
	private int valueValidIndex(int index)	{ return index<0 || index>valueList.size()? valueValidDefaultIndex() : index; }
	private int costValidDefaultIndex()		{ return bounds(0, rawDefaultIndex(), costList.size()-1); }
	private int rawSelectedIndex()			{ return valueList.indexOf(selectedValue); }
	private int rawDefaultIndex()			{ return valueList.indexOf(defaultValue); }
	private int costValidIndex()			{ return costValidIndex(rawSelectedIndex()); }
	private int costValidIndex(int index)	{ return index<0 || index>costList.size()? costValidDefaultIndex() : index; }
	private static String text(String key, String... vals) {
		String str = langLabel(key);
		for (int i=0;i<vals.length;i++)
			str = str.replace(textSubs[i], vals[i]);
		return str;
	}
	private int getValueIndexIgnoreCase(String value) {
		int index = 0;
		for (String entry : altReturnList()) {
			if (entry.equalsIgnoreCase(value))
				return index;
			index++;
		}
		return -1;
	}
	@SuppressWarnings("unchecked")
	private void setFromList(Component frame) {
		String message	= "<html>" + getGuiDescription() + "</html>";
		String title	= text(getLangLabel(), "");
		// System.out.println("getIndex() = " + getIndex());
		// System.out.println("currentOption() = " + currentOption());
		StringList guiTextList = guiTextsList();
		//initMapGuiTexts();
		String[] list = guiTextList.toArray(new String[listSize()]);
		int height = 128 + (int)Math.ceil(18.5 * list.length);
		height = Math.max(300, height);
		height = Math.min(350, height);

		ListDialogUI dialog = RotPUI.instance().listDialog();
		dialog.init(
				frame,	frame,					// Frame & Location component
				message, title,					// Message & Title
				list, selectedValue.toString(),	// List & Initial choice
				null, true,						// long Dialogue & isVertical
				-1, -1,						// Position
				RotPUI.scaledSize(360), RotPUI.scaledSize(height),	// size
				null, null,				// Font, Preview
				altReturnList(),		// Alternate return
				this);					// help parameter

		String input = (String) dialog.showDialog(refreshLevel);
		if (input != null && getValueIndexIgnoreCase(input) >= 0)
			set((T) input);
	}
}
