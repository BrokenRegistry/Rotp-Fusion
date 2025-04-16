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

import rotp.model.game.DynamicOptions;
import rotp.model.game.IGameOptions;

public interface InterfaceOptions {
	/**
	 * Set the option tools value from specific options. 
	 * Used when switching between Game and settings
	 */
	default void prepareToSave(IGameOptions o)	{}
	void updateOptionTool();
	// For settings only
	default void updateOptionTool(DynamicOptions options)	{}
	// For settings only
	default void updateOption(DynamicOptions options)	{}
	default void setFromDefault(boolean excludeCfg, boolean excludeSubMenu)	{ }
	// cascadeSubMenu as integer to avoid infinite loop!
	default void copyOption(IGameOptions src, IGameOptions dest,
							boolean updateTool, int cascadeSubMenu)	{}
}
