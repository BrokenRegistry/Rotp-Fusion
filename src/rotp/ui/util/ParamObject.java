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
import java.io.Serializable;

import rotp.model.game.IGameOptions;
import rotp.ui.BasePanel;

class ParamObject extends AbstractParam<Serializable> {
	
	// ===== Constructors =====
	//
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultValue The default value
	 */
	ParamObject(String gui, String name, Serializable defaultValue) {
		super(gui, name, defaultValue);
	}
	// ===== Overriders =====
	//
	@Override public void setFromCfgValue(String newValue)	{ setFromCfg(newValue); }	
	@Override public boolean prev() { return false; }
	@Override public boolean next() { return false; }
	@Override public boolean toggle(MouseWheelEvent e)	{ return false; }
	@Override public boolean toggle(MouseEvent e, BasePanel frame)	{ return false; }
	@Override protected Serializable getOptionValue(IGameOptions options) {
		Serializable value = options.dynOpts().getObject(getLangLabel());
		if (value == null)
			if (formerName() == null)
				value = creationValue();
			else
				value = options.dynOpts().getObject(formerName(), creationValue());
		return value;
	}
	@Override protected void setOptionValue(IGameOptions options, Serializable value) {
		options.dynOpts().setObject(getLangLabel(), value);
	}
}
