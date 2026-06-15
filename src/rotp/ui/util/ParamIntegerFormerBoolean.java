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

import rotp.model.game.IGameOptions;

public class ParamIntegerFormerBoolean extends ParamInteger {

	// ========== constructors ==========
	//
	/**
	 * @param gui  The label header
	 * @param name The name
	 * @param defaultvalue The default value
	 */
	public ParamIntegerFormerBoolean(String gui, String name, Integer defaultValue) {
		super(gui, name, defaultValue);
		setLimits(0, 100);
		setIncrements(1, 5, 20);
		super.pctValue(true);
//		super.specialZero("BOOLEAN_NO");
//		super.specialValue(100, "BOOLEAN_YES");
	}
	// ===== Overriders =====
	//
	@Override protected Integer getOptionValue(IGameOptions options) {
		Integer value = options.dynOpts().getInteger(getLangLabel());
		if (value == null)
			if (formerName() == null)
				value = creationValue();
			else {
				Boolean boolVal = options.dynOpts().getBoolean(formerName());
				if (boolVal == null)
					value = creationValue();
				else if (boolVal)
					value = maxValue();
				else
					value = minValue();
			}
		if (minValue() != null && value < minValue())
			value = minValue();
		else if (maxValue() != null && value > maxValue())
			value = maxValue();
		return value;
	}
}
