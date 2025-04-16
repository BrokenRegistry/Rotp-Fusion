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
package rotp.model.game;

import rotp.Rotp;
import rotp.ui.UserPreferences;
import rotp.ui.options.AllSubUI;

// BR: converted enum !? to class
// Extended its purpose to manage all IGameOptions instances
// Was only used once to create first Game session option

public final class RulesetManager {
	private static RulesetManager instance;
	private IGameOptions newGameOptions;
	private int currentOptions;

	private RulesetManager() {}

	public static RulesetManager current()	{
		if (instance == null) {
			instance = new RulesetManager();
			AllSubUI.allModOptions(true);
			instance.createNewOptions();
			try {
				UserPreferences.loadAndSave();
			}
			catch (Throwable t) {
				Rotp.startupException = t;
				System.out.println("Err: UserPreferences init: " + t.getMessage());
			}
		}
		return instance;
	}

	public IGameOptions currentOptions()	{
		if (isGameMode())
			return GameSession.instance().options();
		else
			return newOptions();
	}
	public IGameOptions newOptions()		{
		if (newGameOptions == null)
			createNewOptions();
		return newGameOptions;
	}
	public void updateOptionsFromGame()		{
		if (isSetupMode())
			return;
		newGameOptions = GameSession.instance().options().copyAllOptions();
		newGameOptions.setAsSetup();
		setAsSetupMode();
	}
	public boolean isSetupMode()			{ return currentOptions == IGameOptions.SETUP_ID; }
	public boolean isGameMode()				{ return currentOptions == IGameOptions.GAME_ID; }
	public void setAsGameMode()				{ currentOptions(IGameOptions.GAME_ID); }

	private void setAsSetupMode()			{ currentOptions(IGameOptions.SETUP_ID); }
	private void currentOptions(int id)		{
		currentOptions = id;
		currentOptions().UpdateOptionsTools();
	}
	private void createNewOptions() 		{
		Rotp.ifIDE("==================== Create newGameOptions (Setup) ====================");
		newGameOptions = new MOO1GameOptions(false);
		((MOO1GameOptions) newGameOptions).init();
		newGameOptions.setAsSetup();
		Rotp.noOptions(false);
	}
}
