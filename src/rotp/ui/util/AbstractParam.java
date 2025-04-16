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

import static rotp.model.game.DefaultValues.DEF_VAL;
import static rotp.model.game.DefaultValues.FUSION_DEFAULT;
import static rotp.model.game.DefaultValues.MOO1_DEFAULT;
import static rotp.model.game.DefaultValues.ROTP_DEFAULT;
import static rotp.model.game.IGovOptions.NOT_GOVERNOR;
import static rotp.ui.util.IParam.langLabel;
import static rotp.util.Base.textSubs;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import rotp.model.game.DynamicOptions;
import rotp.model.game.GovernorOptions;
import rotp.model.game.IGameOptions;
import rotp.model.game.RulesetManager;
import rotp.ui.UserPreferences;
import rotp.ui.game.BaseModPanel;
import rotp.util.sound.SoundManager;

public abstract class AbstractParam <T> implements IParam {
	// Ignore UCDetector public warning!
	protected static final boolean GO_UP	= true;
	protected static final boolean GO_DOWN	= false;
	protected static final int DO_FOLLOW	= 0;
	protected static final int DO_LOCK		= 1;
	protected static final int DO_REFRESH	= 2;
	private final String name; // 
	private final String gui;  // The label header
	private T value;
	private Map<String, T> defaultValue = new HashMap<>();
	private T minValue		= null;
	private T maxValue		= null;
	private T baseInc		= null;
	private T shiftInc		= null;
	private T ctrlInc		= null;
	private T shiftCtrlInc	= null;
	private int	isGovernor	= NOT_GOVERNOR;
	private boolean isDuplicate	= false;
	private boolean isCfgFile	= false;
	private boolean isValueInit	= true; // default values are initialized with current value.	
	private boolean	updated		= true;
	private boolean	trueChange	= true;
	private String	formerName;	// Link to another option for initialization (when upgrading)
	private List<LinkData> linkList;
	private boolean processingToggle = false;
	private boolean	forcedRefresh	 = false;

	@Override public void updated(boolean val)	{ updated = val; }
	@Override public boolean updated()			{ return updated; }
	@Override public boolean trueChange()		{
		boolean mem = trueChange;
		trueChange = false;
		return mem;
	}
	public void trueChange(boolean val)			{ trueChange = val; }

	// ========== constructors ==========
	//
	/**
	 * @param gui  The label headerTechLibrary
	 * @param name The name
	 * @param commonDefault The common default value
	 */
	protected AbstractParam(String gui, String name, T commonDefault) {
		this.gui = gui;
		this.name = name;
		defaultValue(commonDefault);
		value = defaultValue.get(DEF_VAL.defVal());
	}

	/**
	 * To set the increment based on key modifiers
	 * 
	 * @param baseInc  The base increment
	 * @param shiftInc The increment when Shift is hold
	 * @param ctrlInc  The increment when Ctrl is hold
	 * @param shiftCtrlInc  The increment when Ctrl and Shift are hold
	 * @return this for chaining purpose
	 */
	protected AbstractParam<T> setIncrements(T baseInc, T shiftInc, T ctrlInc, T shiftCtrlInc) {
		this.baseInc	  = baseInc;
		this.shiftInc	  = shiftInc;
		this.ctrlInc	  = ctrlInc;
		this.shiftCtrlInc = shiftCtrlInc;	
		return this;
	}
	/**
	 * To set the parameter limits
	 * 
	 * @param min The lower limit, no limit if the value is null
	 * @param max The upper limit, no limit if the value is null
	 * @return this for chaining purpose
	 */
	protected AbstractParam<T> setLimits(T min, T max) {
		minValue = min;
		maxValue = max;
		return this;
	}
	// ===== For duplicates to be overridden =====
	// For internal use only! Do not call from outside AbstracParam
	protected T getOption() {
		if (isDuplicate()) { // Just in case!
			System.err.println("getOption() Not set " + getCfgLabel());
		}		
		return value; // for cfg non duplicate
	}
	protected void setOption(T value) { setOptionValue(opts(), value); }
	protected abstract T getOptionValue(IGameOptions options);
	protected void setOptionValue(IGameOptions options, T value) {
		if (isDuplicate()) { // Just in case!
			System.err.println("getFromOption() not updated to getOptionValue: " + name);
		}		
	}
	// public void transfer (IGameOptions opts, boolean set) {}
	// ========== Public Interfaces ==========
	//
	//	public abstract void setFromCfgValue(String val);
	//	public abstract void next();
	//	public abstract void prev();
	//	public abstract void toggle(MouseWheelEvent e);
	//	public abstract void toggle(MouseEvent e);
	@Override public void prepareToSave(IGameOptions o)	{ setOptionValue(o, get()); }
	@Override public String toString() {
		return getCfgLabel() + " = " + getCfgValue();
	}
	@Override public void updateOptionTool()	{
		if (isCfgFile)
			setOption(get());
		else
			set(getOptionValue(opts()));
	}
	@Override public void copyOption(IGameOptions src, IGameOptions dest,
									boolean updateTool, int cascadeSubMenu) { // TODO BR: Double check this
		if (src == null || dest == null)
			return;
		T val;
		if (isCfgFile && src == opts()) // current option get current value
			val = getOption();
		else
			val = getOptionValue(src); // get from the source
		if (updateTool)
			set(val);
		if (isCfgFile && dest == opts()) // dest = current: update cfg file
			setOption(val);
		else
			setOptionValue(dest, val);
	}
	@Override public String getCfgValue()		{ return getCfgValue(value); }
	@Override public String getCfgLabel()		{ return name; }
	@Override public String getLangLabel()		{ return gui + name; }
	@Override public String getGuiDescription() { return langLabel(descriptionId()); }
	@Override public String guideValue()		{ return String.valueOf(value); }
	@Override public String getGuiDisplay()		{ return langLabel(getLangLabel(), guideValue()) + END; }
	@Override public String getGuiDisplay(int idx)	{
		String str = langLabel(getLangLabel()); // Get from label.txt
//		String[] strArr = str.split("%%");
//		if (strArr.length == 1)
//			strArr = str.split(textSubs[0]);
		String[] strArr = str.split(textSubs[0]);

		switch(idx) {
		case 0:
			if (strArr.length > 0)
				return strArr[0];
			else
				return "";
		case 1:
			if (strArr.length == 2)
				return guideValue() + strArr[1];
			else if (strArr.length == 3)
				return strArr[1] + guideValue() + strArr[2];
			else
				return guideValue();
		default:
			return "";
		}
	}
	@Override public String guideDefaultValue()	{ return defaultValue().toString(); }
	@Override public boolean isDefaultValue()	{ return defaultValue().equals(get()); }
	@Override public boolean isDuplicate()		{ return isDuplicate; }
	@Override public boolean isCfgFile()		{ return isCfgFile; }
	@Override public String getGuiValue(int idx){ return guideValue(); } // For List
	@Override public void setFromDefault(boolean excludeCfg, boolean excludeSubMenu)	{
		if (excludeCfg && isCfgFile())
			return;
		if (excludeSubMenu && isSubMenu())
			return;
		set(defaultValue());
	}
	@Override public boolean toggle(MouseEvent e, MouseWheelEvent w, BaseModPanel frame) {
		if (processingToggle)
			return false;
		processingToggle = true;

		LinkValue lastValue = linkValue(get());
		boolean forceUpdate = forcedRefresh || !isValidValue(); // To refresh display if become valid.

		// Set the new value for testing purpose
		if (e == null)
			forceUpdate |= toggle(w);
		else
			forceUpdate |= toggle(e, frame);
		forceUpdate |= !isValidValue();

		// Should be locally valid
		// Stop here if no dependencies
		if (linkList == null) {
			processingToggle = false;
			return forceUpdate;
		}

		// Stop Here if the value has not been changed
		LinkValue newValue = linkValue(get());
		if (newValue.equals(lastValue)) {
			processingToggle = false;
			return forceUpdate;
		}

		// The value has changed, test for dependencies
		boolean up = getDirectionOfChange(lastValue, newValue);

		// Test if Locked by dependencies
		// They can not be changed, so local test is enough
		for (LinkData entry : linkList) {
			if (entry.locked(up))
				if (entry.copy().isUpdateNeeded(newValue)) {
					set(lastValue);
					badClick();
					processingToggle = false;
					return true;
				}
		}

		// Test if dependencies and their dependencies can follow 
		for (LinkData entry : linkList) {
			if (entry.follow(up))
				if (entry.isInvalidLinkedValue(newValue)) {
					set(lastValue);
					badClick();
					processingToggle = false;
					return true;
				}
		}

		// All dependencies and their dependencies are valid.
		// Apply changes now
		for (LinkData entry : linkList) {
			if (entry.follow(up))
				entry.followValue(newValue);
		}
		
		// update the dependencies list
		for (LinkData entry : linkList) {
			if (entry.action == DO_REFRESH)
				entry.aim.initDependencies(VALID_DEPENDENCIES);
		}
		
		processingToggle = false;
		return true;
	}
	// ========== Tools for overriders ==========
	//
	protected boolean updateNeeded(LinkValue value, boolean up) {
		if (up)
			return value.isPositiveDiff(linkValue(get()));
		else
			return linkValue(get()).isPositiveDiff(value);
	}
	protected IGameOptions   opts()		{ return RulesetManager.current().currentOptions(); }
	private	  DynamicOptions dynOpts()	{ return opts().dynOpts(); }
	protected T last()					{ return value; }
	protected AbstractParam<T> formerName(String link)	{
		formerName = link;
		return this;
	}
	public void resetToDefaultValue()	{ set(defaultValue()); }
	public T defaultValue()				{ return defaultValue.get(DEF_VAL.defVal()); }
	public T get()						{
		if (isCfgFile) {
			value = getOption();
		}
		else if (isDuplicate) {
			value = getOptionValue(opts());
		}
		return value;
	}	
	public T setFromIndex(int i)		{ return null; }
	public String getCfgValue(T value)	{ return String.valueOf(value); }
	// ========== Public Getters ==========
	//
	protected boolean processingToggle()	{ return processingToggle; }
	protected boolean forcedRefresh()		{ return forcedRefresh; }
	public String getLabel()		{ return langLabel(getLangLabel()); }
	protected String formerName()	{ return formerName; }
	protected T creationValue()		{ return isValueInit? value : defaultValue(); }
	protected T minValue()			{ return minValue; }
	protected T maxValue()			{ return maxValue; }
	T baseInc()		{ return baseInc; }
	T shiftInc()	{ return shiftInc; }
	T ctrlInc()		{ return ctrlInc; }
	T shiftCtrlInc(){ return shiftCtrlInc; }
	// ========== Public Setters ==========
	//
	public void toggle(boolean reverse)		{ if (reverse) prev(); else next(); }
	protected void setFromCfg(T newValue)	{
		updated(true);
		value = newValue;
		updateOption(dynOpts());
	}
	public T silentSet(T newValue) 			{ // Reserved call from governor class
		updated(true);;
		value = newValue;
		setOption(newValue); // For overrider
		return value;
	}
	public T set(T newValue)				{
		updated(true);;
		trueChange(false);
		if(updated() && value != null && newValue != null)
			trueChange(!value.equals(newValue));
		value = newValue;
		setOption(newValue); // For overrider
		if (trueChange && (isGovernor != NOT_GOVERNOR))
			GovernorOptions.callForRefresh(isGovernor);
		if (isCfgFile)
			UserPreferences.save();
		return value;
	}
	public void maxValue (T newValue)		{ maxValue = newValue;}
	public void minValue (T newValue)		{ minValue = newValue;}
	public void isGovernor(int val)			{ isGovernor  = val ; }
	public void defaultValue(T commonValue)	{
		defaultValue.put(FUSION_DEFAULT, commonValue);
		defaultValue.put(MOO1_DEFAULT, commonValue);
		defaultValue.put(ROTP_DEFAULT, commonValue);
	}
	public AbstractParam<T> setDefaultValue(String key, T value)	{
		defaultValue.put(key, value);
		value = defaultValue();
		return this;
	}
	public AbstractParam<T> forcedRefresh(boolean forced) {
		forcedRefresh = forced;
		return this;
	}
	/**
	 * For backward compatibility
	 * <br>Old save game are initialized with:
	 * <br>true: The current value.
	 * <br>false: The default value.
	 * <br>true by default.
	 * <br>Should be set to false if this parameter change a previously constant value.
	 */
	public AbstractParam<T> isValueInit(boolean is)	{ isValueInit = is ; return this; }
	public AbstractParam<T> isDuplicate(boolean is)	{ isDuplicate = is ; return this; }
	public AbstractParam<T> isCfgFile(boolean is)	{ isCfgFile   = is ; return this; }
	
	// ========== Private Methods ==========
	//
	private void badClick() { SoundManager.current().playAudioClip("MisClick"); }
	// ========== Protected Methods ==========
	//
	protected String descriptionId()		{ return getLangLabel() + LABEL_DESCRIPTION; }
	protected T getInc(InputEvent e)		{
		if (e.isShiftDown())
			if (e.isControlDown())
				return shiftCtrlInc;
			else
				return shiftInc;
		else if (e.isControlDown())
			return ctrlInc;
		else
			return baseInc;
	}
	protected int getDir(MouseEvent e)		{
		if (SwingUtilities.isRightMouseButton(e)) return -1;
		if (SwingUtilities.isLeftMouseButton(e)) return 1;
		return 0;
	}
	protected int getDir(MouseWheelEvent e)	{
		if (e.getWheelRotation() < 0) return 1;
		return -1;
	}
	//========== Static Methods ==========
	//
	static String yesOrNo(boolean b) { return b ? "Yes" : "No"; }
	static boolean yesOrNo(String s) { return s.equalsIgnoreCase("YES") || s.equalsIgnoreCase("TRUE"); }
	/**
	 * Convert String to Float and manage errors
	 * @param string Source of Float
	 * @return Float value, or <b>null</b> on error
	 */
	static Float stringToFloat(String string) {
		try {
			return Float.valueOf(string.trim());
		}
		catch (NumberFormatException nfe) {
			return null; // silent error!
		}
	}
	/**
	 * Convert String to Integer and manage errors
	 * @param string Source of Integer
	 * @return Integer value, or <b>null</b> on error
	 */
	protected static Integer stringToInteger(String string) {
		try {
			return Integer.valueOf(string.trim());
		}
		catch (NumberFormatException nfe) {
			return null; // silent error!
		}
	}
	// ========== Parameters dependencies methods ==========
	protected boolean isInvalidLocalMax(LinkData rec)	{
		return isInvalidLocalMax(linkValue(rec.aimValue));
	}
	protected boolean isInvalidLocalMin(LinkData rec)	{
		return isInvalidLocalMin(linkValue(rec.aimValue));
	}
	protected boolean isInvalidLocalValue(LinkData rec)	{
		return isInvalidLocalValue(linkValue(rec.aimValue));
	}
	protected boolean isInvalidLocalMax(T value)	{ return true; }
	protected boolean isInvalidLocalMin(T value)	{ return true; }
	protected boolean isInvalidLocalValue(T value)	{
		return isInvalidLocalMin(value) && isInvalidLocalMax(value);
	}
	LinkValue linkValue(T val)	{ return new LinkValue(); } 
	T linkValue(LinkValue val)	{ return null; }
	private void set(LinkValue val)	{ set(linkValue(val)); }
	protected void set(LinkData rec)	{ set(rec.aimValue); }
	public boolean followValue(LinkData rec)	{ return false; }
	/**
	 * Used as standard Override by options with dependencies
	 */
	protected boolean isValidDoubleCheck()	{ return !isInvalidLocalValue(get()); }
	
	protected List<LinkData> linkList()	{ return linkList; }
	protected void resetLinks()	{ linkList = null; }
	private void addLink(LinkData data)	{
		if (linkList == null)
			linkList = new ArrayList<>();
		linkList.add(data);
	}
	protected void addLink(AbstractParam<?> aim, int action, boolean srcUp, boolean aimUp, String key) {
		addLink(new LinkData(aim, action, srcUp, aimUp, key, this));
	}
	protected void addLink(AbstractParam<?> aim, int action) { // Only use for refresh
		addLink(new LinkData(aim, action, false, false, "", this));
	}
	protected void convertValueToLink(LinkData rec) 		  {
		System.err.println("convertValueToLink request is not implemented:");
		System.err.println("Called by: " + name);
	}
	protected Boolean getDirectionOfChange(T before, T after) { return null; }
	private Boolean getDirectionOfChange(LinkValue before, LinkValue after) {
		return getDirectionOfChange(linkValue(before), linkValue(after));
	}
}
