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

import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import rotp.ui.game.BaseModPanel;

public class ParamBoolInt extends ParamInteger {
	private ParamBoolean boolParam;

	// ========== constructors ==========
	//

	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultvalue The default value
	 * @param minValue The minimum value (null = none)
	 * @param maxValue The maximum value (null = none)
	 */
	public ParamBoolInt(String gui, ParamBoolean bool, String enabled, String disabled,
			Integer defaultValue, Integer minValue, Integer maxValue) {
		super(gui, enabled, defaultValue);
		setLimits(minValue, maxValue);
		boolParam = bool;
		specialNegative(gui+disabled);
	}

	// ===== Overriders =====
	//
	@Override public ParamBoolInt isValueInit(boolean is) { super.isValueInit(is) ; return this; }
	@Override public ParamBoolInt isDuplicate(boolean is) { super.isDuplicate(is) ; return this; }
	@Override public ParamBoolInt isCfgFile(boolean is)	  { super.isCfgFile(is)   ; return this; }
	@Override public ParamBoolInt setUpdateParameters(IUpdated method, String id)	{
		super.setUpdateParameters(method, id);
		return this;
	}

	@Override public String getGuiDisplay()		{
	    String val;
	    if (isEnabled())
	    	val = langLabel(super.getLangLabel(), guideValue());
	    else
	    	val = langLabel(negativeLabel());
	    return langLabel(boolParam.getLangLabel(), val +END);
	}
	@Override public String getLangLabel()		{ return boolParam.getLangLabel(); }
	@Override public Integer set(Integer val)	{
		super.set(val);
		boolParam.set(isEnabled());
		return val;
	}
	@Override public boolean prev() {
		if (isEnabled())
			return super.prev();
		return false;
	}
	@Override public boolean next() {
		if (isEnabled())
			return super.next();
		return false;
	}
	@Override public boolean toggle(MouseEvent e, BaseModPanel frame)	{
		if (SwingUtilities.isRightMouseButton(e)) {
			if (e.isShiftDown())
				prev();
			else
				next();
			return false;
		}
			
		int lastInt = Math.abs(last());
		boolean lastBool = boolParam.get();
		boolParam.set(!lastBool);
		if (lastBool)
			set(-lastInt);
		else
			set(lastInt);
		return false;
	}
	// ===== Other Public Methods =====
	//
	public boolean isDisabled() { return last() < 0;}
	public boolean isEnabled() { 
		return last() >= 0;
	}
	// ===== Other Private Methods =====
	//
}
