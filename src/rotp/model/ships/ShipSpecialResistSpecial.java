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
package rotp.model.ships;

import rotp.model.tech.TechResistSpecial;

public final class ShipSpecialResistSpecial extends ShipSpecial {
	private static final long serialVersionUID = 1L;
	public ShipSpecialResistSpecial(TechResistSpecial t) {
		tech(t);
		sequence(t.level + .05f);
	}
	@Override public TechResistSpecial tech()	{ return (TechResistSpecial) super.tech(); }
	@Override public boolean isWeapon()			{ return false; }
	@Override public int	 range()			{ return tech().range; }
	@Override public boolean isImmuneToStasis()	{ return tech().immuneToStasis; }
	@Override public boolean resistRepulsors()	{ return tech().resistRepulsors; }
 }
