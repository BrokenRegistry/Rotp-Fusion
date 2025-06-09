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
package rotp.model.game;

import java.io.Serializable;

import rotp.model.empires.CustomRaceDefinitions;
import rotp.model.empires.Empire.EmpireBaseData;
import rotp.model.empires.Species;

public class NewPlayer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String race;
    private String leaderName;
    private String homeWorldName;
    private int	   color = 0;
	private transient Species species;
	private void initSpecies(IGameOptions options, EmpireBaseData empSrc)	{
		// Get abilities parameters
		String abilitiesKey = race();
		DynOptions crOptions = null;
		String restartChangesPlayerRace = options.selectedRestartChangesPlayerRace();
		if (options.selectedPlayerIsCustom()) {
			abilitiesKey = CustomRaceDefinitions.CUSTOM_RACE_KEY;
			crOptions = options.playerCustomRaceOption();
		}
		if (empSrc != null && !options.selectedRestartAppliesSettings()
				&& !restartChangesPlayerRace.equals("GuiLast")
				&& !restartChangesPlayerRace.equals("GuiSwap")) { // Use Restart info
			abilitiesKey = empSrc.dataRaceKey;
			crOptions = empSrc.raceOptions;
		}
		// Get Species
		species = new Species(race(), abilitiesKey, crOptions, null);
	}
	public Species getPlayer(IGameOptions options, EmpireBaseData empSrc)	{
		initSpecies(options, empSrc);
		return species;
	}
	public Species getPlayer(IGameOptions options)	{
		if (species == null)
			initSpecies(options, null);
		return species;
	}
	public void update(IGameOptions options)	{
		initSpecies(options, null);
		homeWorldName(species.defaultHomeworldName());
		leaderName(species.randomLeaderName());
	}
	public void setRandom(IGameOptions options)	{
		species = null;
	}
	public void race(String name)		{ race = name; }
	String leaderName()					{ return leaderName; }
	void leaderName(String name)		{ leaderName = name; }
	String homeWorldName()				{ return homeWorldName; }
	void homeWorldName(String name)		{ homeWorldName = name; }
	int color()							{ return color; }
	void color(int r)					{ color = r; }
	String race()						{ return race; }
}
