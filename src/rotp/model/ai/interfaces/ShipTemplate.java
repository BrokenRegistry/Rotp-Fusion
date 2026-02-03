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
package rotp.model.ai.interfaces;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import rotp.model.ai.EnemyColonyTarget;
import rotp.model.ai.EnemyShipTarget;
import rotp.model.empires.Empire;
import rotp.model.empires.EmpireView;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipSpecial;
import rotp.model.ships.ShipWeapon;
import rotp.model.tech.TechTree;
import rotp.util.Base;

public abstract class ShipTemplate implements Base {
	
	public enum DesignType {
		FIGHTER, BOMBER, DESTROYER, MISSILE, BEAM, HYBRID, INTERCEPTOR;
		public boolean isFighter()		{ return this == FIGHTER;}
		public boolean isBomber()		{ return this == BOMBER;}
		public boolean isDestroyer()	{ return this == DESTROYER;}
		public boolean isMissile()		{ return this == MISSILE;}
		public boolean isBeam()			{ return this == BEAM;}
		public boolean isHybrid()		{ return this == HYBRID;}
		public boolean isInterceptor()	{ return this == INTERCEPTOR;}
		public boolean mustHaveBeam()	{ return isBeam() || isHybrid() || isInterceptor();}
		public boolean isStandard()		{ return isFighter() || isBomber() || isDestroyer(); }
	};
	
	protected static final List<DesignDamageSpec> dmgSpecs = new ArrayList<>();
	protected static final ShipDesign mockDesign = new ShipDesign();
	
	abstract protected ShipDesign bestDesign(ShipDesigner ai, DesignType role);
	protected ShipDesign newDesign(ShipDesigner ai, DesignType role, int size) {
		return bestDesign(ai, role);
	};

	public ShipDesign autoDestroyerDesign(ShipDesigner ai, int size)	{
		return newDesign(ai, DesignType.DESTROYER, size);
	}
	public ShipDesign autoFighterDesign(ShipDesigner ai, int size)		{
		return newDesign(ai, DesignType.FIGHTER, size);
	}
	public ShipDesign autoBomberDesign(ShipDesigner ai, int size)		{
		return newDesign(ai, DesignType.BOMBER, size);
	}
	public ShipDesign autoMissileDesign(ShipDesigner ai, int size)		{
		return newDesign(ai, DesignType.MISSILE, size);
	}
	public ShipDesign autoBeamDesign(ShipDesigner ai, int size)			{
		return newDesign(ai, DesignType.BEAM, size);
	}
	public ShipDesign autoHybridDesign(ShipDesigner ai, int size)		{
		return newDesign(ai, DesignType.HYBRID, size);
	}
	public ShipDesign autoInterceptorDesign(ShipDesigner ai, int size)	{
		return newDesign(ai, DesignType.INTERCEPTOR, size);
	}

	public ShipDesign newFighterDesign(ShipDesigner ai)		{
		return bestDesign(ai, DesignType.FIGHTER);
	}
	public ShipDesign newBomberDesign(ShipDesigner ai)		{
		return bestDesign(ai, DesignType.BOMBER);
	}
	public ShipDesign newDestroyerDesign(ShipDesigner ai)	{
		return bestDesign(ai, DesignType.DESTROYER);
	}
	public ShipDesign newMissileDesign(ShipDesigner ai)		{
		return bestDesign(ai, DesignType.MISSILE);
	}
	public ShipDesign newBeamDesign(ShipDesigner ai)		{
		return bestDesign(ai, DesignType.BEAM);
	}
	public ShipDesign newHybridDesign(ShipDesigner ai)		{
		return bestDesign(ai, DesignType.HYBRID);
	}
	public ShipDesign newInterceptorDesign(ShipDesigner ai)	{
		return bestDesign(ai, DesignType.INTERCEPTOR);
	}

	// ********** HELPER FUNCTIONS ASSESSING ENEMIES AND OWN PRODUCTION ********** //

	public List<EnemyShipTarget> buildShipTargetList(List<TechTree> rivals)	{
		List<EnemyShipTarget> shipTargets = new ArrayList<>();
		for (TechTree tt : rivals)
			if (tt != null)
				shipTargets.add(new EnemyShipTarget(tt));
		return shipTargets;
	}
	public List<EnemyColonyTarget> buildColonyTargetList(List<TechTree> rivals)	{
		List<EnemyColonyTarget> colonyTargets = new ArrayList<>();
		for (TechTree tt : rivals)
			if (tt != null)
				colonyTargets.add(new EnemyColonyTarget(tt));
		return colonyTargets;
	}
	public List<TechTree> assessRivalsTech(Empire emp, int rivalsNum)	{
		List<TechTree> rivalTech = new ArrayList<>();
		SortedMap<Float, EmpireView> relationsMap = new TreeMap<>();

		// sorting all known empires by the relations with them, ascending
		for (EmpireView ev : emp.empireViews()) {
			if (ev != null)
				relationsMap.put(ev.embassy().relations(), ev);
		}

		// yeah, sorry, that was the most straightforward Java-ish method I found to get top three
		if (!relationsMap.isEmpty()) { 
			Iterator<EmpireView> worstNeighbors = relationsMap.values().iterator();
			for (int i = 0; (i < rivalsNum) && (worstNeighbors.hasNext()); i++) {
				rivalTech.add(worstNeighbors.next().spies().tech());
			}
		}

		// if we have less known empires than rivalsNum, add ourselves into the list
		if (rivalTech.size() < rivalsNum) {
			rivalTech.add(emp.tech());
		}
		return rivalTech;
	}

	// ********** DAMAGE SIMULATION FUNCTIONS ********** //
	// some are called from outside

	public float estimatedBombDamage(ShipDesign d, EnemyColonyTarget target)	{
		float totalDamage = 0;
		for (int i=0;i<ShipDesign.maxWeapons();i++) {
			float wpnDamage;
			ShipWeapon wpn = d.weapon(i);
			if (!wpn.groundAttacksOnly())
				wpnDamage = 0;
			else {
				wpnDamage = d.wpnCount(i) * wpn.firepower(target.shieldLevel);
				// +15% damage for each weapon computer level
				// this estimates increased dmg from +hit
				wpnDamage *= (1+ (.15*wpn.computerLevel()));
			}
			totalDamage += wpnDamage;
		}
		return totalDamage;
	}

////////////////////////////////////////////////////

//********** DESIGNDAMAGESPEC-RELATED ********** //

	protected DesignDamageSpec newDamageSpec()	{
		if (dmgSpecs.isEmpty())
			return new DesignDamageSpec();
		else
			return dmgSpecs.remove(0);
	}
	protected class DesignDamageSpec	{
		public int numWeapons = 0;
		public ShipWeapon weapon;
		public ShipSpecial special;
		public float damage;
		public void set(DesignDamageSpec spec)	{
			numWeapons = spec.numWeapons;
			weapon = spec.weapon;
			special = spec.special;
			damage = spec.damage;
			spec.reclaim();
		}
		public void reclaim()	{
			numWeapons = 0;
			weapon = null;
			special = null;
			damage = 0;
			dmgSpecs.add(this);
		}
	}
}
