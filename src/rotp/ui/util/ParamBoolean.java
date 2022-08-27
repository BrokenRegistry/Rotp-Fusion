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

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class ParamBoolean extends AbstractParam<Boolean> {
	
	private float costTrue;
	private float costFalse;
	
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
	 * @param allowSave To allow the parameter to be saved in Remnants.cfg
	 */
	public ParamBoolean(String gui, String name, Boolean defaultValue, boolean allowSave) {
		super(gui, name, defaultValue);
		allowSave(allowSave);
	}
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 * @param costTrue  Cost if the setting is true
	 * @param costFalse Cost if the setting is false
	 * @param allowSave To allow the parameter to be saved in Remnants.cfg
	 */
	public ParamBoolean(String gui, String name, Boolean defaultValue
			,float costTrue, float costFalse, boolean allowSave) {
		super(gui, name, defaultValue);
		allowSave(allowSave);
		this.costTrue	= costTrue;
		this.costFalse	= costFalse;
	}
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 * @param costTrue  Cost if the setting is true
	 * @param costFalse Cost if the setting is false
	 * @param allowSave To allow the parameter to be saved in Remnants.cfg
	 */
	public ParamBoolean(String gui, String name, Boolean defaultValue
			,int costTrue, int costFalse, boolean allowSave) {
		super(gui, name, defaultValue);
		allowSave(allowSave);
		this.costTrue	= costTrue;
		this.costFalse	= costFalse;
	}
	// ===== Overriders =====
	//
	@Override public float getCost() {
		if (get())
			return costTrue;
		return costFalse;
	}
	@Override public String getCfgValue() {
		return yesOrNo(get());
	}
	@Override public String getGuiValue() {
		return yesOrNo(get());
	}
	@Override public Boolean next() {
		return setAndSave(!get()); 
	}
	@Override public Boolean toggle(MouseWheelEvent e) {
		return next(); 
	}
	@Override public Boolean toggle(MouseEvent e) {
		if (getDir(e) == 0)
			return setToDefault(true);
		else
			return next();
	}
	@Override public Boolean setFromCfgValue(String newValue) {
		return value(yesOrNo(newValue));
	}	
	@Override public Boolean prev() { return next(); }
}