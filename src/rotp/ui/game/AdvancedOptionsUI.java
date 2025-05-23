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
package rotp.ui.game;

// modnar: add UI panel for modnar MOD game options, based on StartOptionsUI.java
public class AdvancedOptionsUI extends AbstractOptionsUI {
	private static final long serialVersionUID = 1L;
	private static final String guiTitleID = "SETTINGS_TITLE";
	public static final String GUI_ID     = "ADVANCED_OPTIONS";
	
	// Just call the "super" with GUI Title Label ID
	public AdvancedOptionsUI() {
		super(guiTitleID, GUI_ID);
	}
	@Override protected void init0() {
		duplicateList = rotp.model.game.IAdvOptions.advancedOptions();
	}
}
