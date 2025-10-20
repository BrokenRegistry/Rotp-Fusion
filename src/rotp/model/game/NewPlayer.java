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

import rotp.model.empires.species.Species;

public class NewPlayer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String race;
    private String leaderName;
    private String homeWorldName;
    private int	   color = 0;

    // BR: No initialization needed 
    // Avoid too early access to options
//    public NewPlayer() {
//        Race def = Race.races().get(0);
//        race = def.id;
//        leaderName = def.randomLeaderName();
//        homeWorldName = def.defaultHomeworldName();
//        color = 0;	
//    }
    public void copy(NewPlayer p) {
        race = p.race;
        leaderName = p.leaderName;
        homeWorldName = p.homeWorldName;
        color = p.color;
    }
    public String leaderName()					{ return leaderName; }
    public void	  leaderName(String name)		{ leaderName = name; }
    public String homeWorldName()				{ return homeWorldName; }
    public void	  homeWorldName(String name)	{ homeWorldName = name; }
    public int	  color()						{ return color; }
    public void	  color(int r)					{ color = r; }
    public String race()						{ return race; }
    public void	  race(String name)				{ race = name; }
	public void	  update(IGameOptions opts)		{
		Species s = new Species(race());
		homeWorldName(s.defaultHomeworldName());
		leaderName(s.randomLeaderName());
	}
}
