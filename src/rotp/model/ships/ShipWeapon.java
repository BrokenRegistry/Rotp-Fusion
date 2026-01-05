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
package rotp.model.ships;

import rotp.model.combat.CombatStack;
import rotp.model.combat.CombatStackColony;
import rotp.model.game.ICombatOptions;
import rotp.model.tech.TechBiologicalWeapon;

public class ShipWeapon extends ShipComponent {
    private static final long serialVersionUID = 1L;
    private static final boolean showErrorMessages = false; // BR: Set to False
    private static final boolean showErrors = false; // BR: Set to False
    @Override
    public boolean isBeamWeapon()     { return false; }
    @Override
    public boolean isWeapon()         { return true; }
    public boolean noWeapon()         { return range() == 0; }
    public int minDamage()            { return 0; }
    public int maxDamage()            { return 0; }
    public int shots()                { return 1; }
    public int turnsToFire()          { return 1; }
    public float computerLevel()      { return 0; }
    public float decay()              { return 0; }
    public boolean canAttackPlanets() { return (!noWeapon() && (maxDamage() > 0)); }
	public float firepower()			{ return firepower(0, 0); }
	public float hitPct(float dLevel)	{ return bounds(.05f, (5 + dLevel)/10, 1); } //cannot hit for more than 100%
	public int minDamage(float dLevel)	{ // For MoO1 combat resolution
		if (dLevel<=5 || !options().moo1CombatResolution())
			return minDamage();
		float hitPct = (5 + dLevel) / 10;
		int minDamage = minDamage();
		return (int) (minDamage + (maxDamage()-minDamage) * (1 - 1/hitPct));
	}
//    public float firepower(float shield) {
//		int maxAttackPower = maxDamage();
//		float ensuingShield = (shield * shieldMod());
//		if (ICombatOptions.moo1ShieldRules.get())	// To follow MoO1 rules
//			ensuingShield = (int) ensuingShield;
//		if (ensuingShield >= maxAttackPower)
//			return 0.0f; // The weapon is too weak
//		int minAttackPower = minDamage();
//
//		// The first attack power that can penetrate the shield
//		int minPowerToHit = Math.max(minAttackPower, (int) ensuingShield + 1);
//		// Average damage when the attack hit the target
//		float averageHitDamage = (minPowerToHit + maxAttackPower)/2f - ensuingShield;
//		// Average Damage including the non hit
//		float hitProbability = (float) (maxAttackPower - minPowerToHit + 1) / (maxAttackPower - minAttackPower + 1);
//		float averageDamage = averageHitDamage * hitProbability;
//
//		return attacksPerRound() * averageDamage * scatterAttacks();
//	}
	public float firepower(float shield) { return firepower(shield, 0); } // TODO BR: try to update callers to give the right level difference
	public float firepower(float shield, float diffLevel)	{
		int maxAttackPower = maxDamage();
		float ensuingShield = (shield * shieldMod());
		if (ICombatOptions.moo1ShieldRules.get())	// To follow MoO1 rules
			ensuingShield = (int) ensuingShield;
		if (ensuingShield >= maxAttackPower)
			return 0.0f; // The weapon is too weak
		int minAttackPower = minDamage(diffLevel);

		// The first attack power that can penetrate the shield
		int minPowerToHit = Math.max(minAttackPower, (int) ensuingShield + 1);
		// Average damage when the attack hit the target
		float averageHitDamage = (minPowerToHit + maxAttackPower)/2f - ensuingShield;
		// Average Damage including the non hit
		float hitProbability = (float) (maxAttackPower - minPowerToHit + 1) / (maxAttackPower - minAttackPower + 1);
		float averageDamage = averageHitDamage * hitProbability;

		return attacksPerRound() * averageDamage * scatterAttacks();
	}
    public float max(ShipDesign d, int i) {
        return noWeapon() ? 0 : max(0, (float)Math.floor(d.availableSpaceForWeaponSlot(i) / space(d))) ;
    }
    @Override
    public float estimatedKills(CombatStack source, CombatStack target, int num) {
        if (groundAttacksOnly() && !target.isColony())
            return 0;
		float diffLevel = source.attackLevel() - target.bombDefense();
        float shieldLevel = target.shieldLevel();
        //ail: we multiply our damage by the amount of weapons shooting
        // modnar: account for planetDamageMod()
        // correctly calculate damage estimate for attacking colony (in round-about way)
        // beams and torpedoes do half damage against colonies, planetDamageMod() = 0.5f
        // other weapons have planetDamageMod() = 1.0f, so this correction would have no effect for them
        // average(beamMax/2-shield, beamMin/2-shield)  // correct formula
        // = average(beamMax-2*shield, beamMin-2*shield)/2  // equivalent formula used here
        float dmg;
        if (target.isColony()) {
            shieldLevel = target.shieldLevel() / planetDamageMod();
            dmg = firepower(shieldLevel, diffLevel) * num * planetDamageMod();
        }
		else
			dmg = firepower(shieldLevel, diffLevel) * num;

        if (dmg == 0)
            return 0;
        //ail: The only thing that makes sense to return here is our total damage divided by the target's hitpoints
        return dmg/target.maxStackHits();
    }
    @Override
    public float estimatedBombardDamage(CombatStack source, CombatStackColony target) {
        int num = bombardAttacks();
        float shieldMod = source.targetShieldMod(this);
        float shieldLevel = shieldMod * target.shieldLevel();
        float beamMod = 1;
		float diffLevel = source.attackLevel() - target.bombDefense();
		float hitPct = hitPct(diffLevel); //cannot hit for more than 100%
        if(isBeamWeapon())
        {
            shieldLevel /= planetDamageMod(); 
            beamMod = planetDamageMod();
        }
        if(isBioWeapon() && target.num == 0)
        {
            float targetAntiDote = target.empire().tech().antidoteLevel();
            float damage = TechBiologicalWeapon.avgDamage(maxDamage(), (int)targetAntiDote) * 200;
            return damage * num;
        }
		return firepower(shieldLevel, diffLevel)* num * beamMod * hitPct;
    }
    @Override
    public float estimatedBombardDamage(ShipDesign des, CombatStackColony target) {
        int num = bombardAttacks();
        float shieldMod = des.targetShieldMod(this);
        float shieldLevel = shieldMod * target.shieldLevel();
        float beamMod = 1;
		float diffLevel = des.attackLevel() + des.empire().shipAttackBonus() - target.bombDefense();
		float hitPct = hitPct(diffLevel); //cannot hit for more than 100%
        if(isBeamWeapon())
        {
            shieldLevel /= planetDamageMod(); 
            beamMod = planetDamageMod();
        }
        if(isBioWeapon() && target.num == 0)
        {
            float targetAntiDote = target.empire().tech().antidoteLevel();
            float damage = TechBiologicalWeapon.avgDamage(maxDamage(), (int)targetAntiDote) * 200;
            return damage * num;
        }
		return firepower(shieldLevel, diffLevel)* num * beamMod * hitPct;
    }
    @Override
    public String fieldValue(int n, ShipDesign d, int bank) {
        switch(n) {
            case 0: return name().isEmpty() ? text("SHIP_DESIGN_COMPONENT_NONE") : name();
            case 1: return str((int)max(d, bank));
            case 2: int min = minDamage();
                int max = maxDamage();
                return (min == max) ? ""+min : text("SHIP_DESIGN_WEAPON_DMG_RANGE", min, max);
            case 3: return str((int)(cost(d)+(enginesRequired(d)*d.engine().cost(d))));
            case 4: return str((int)size(d));
            case 5: return str((int)power(d));
            case 6: return str((int)space(d));
            case 7: return desc();
        }
        return "";
    }
    public void drawIneffectiveAttack(CombatStack source, CombatStack target, int count) {
    	if (showErrors)
    		tech().drawIneffectiveAttack(source, target, source.weaponNum(this), count);
    	else {
	        try {
	            tech().drawIneffectiveAttack(source, target, source.weaponNum(this), count);
	        }
	        catch(Exception e) {
	        	if (showErrorMessages) {
	        		System.err.println("drawIneffectiveAttack Exception");
	        		System.err.println(e.getMessage());
	        	}
	        }
    	}
    }
    public void drawUnsuccessfulAttack(CombatStack source, CombatStack target, int count) {
    	if (showErrors)
    		tech().drawUnsuccessfulAttack(source, target, source.weaponNum(this), count);
    	else {
	        try {
	            tech().drawUnsuccessfulAttack(source, target, source.weaponNum(this), count);
	        }
	        catch(Exception e) {
	        	if (showErrorMessages) {
	        		System.err.println("drawUnsuccessfulAttack Exception");
	        		System.err.println(e.getMessage());
	        	}
	        }
    	}
    }
    public void drawSuccessfulAttack(CombatStack source, CombatStack target, float dmg, int count) {
    	if (showErrors)
    		tech().drawSuccessfulAttack(source, target, source.weaponNum(this), dmg, count);
    	else {
	        try {
	            tech().drawSuccessfulAttack(source, target, source.weaponNum(this), dmg, count);
	        }
	        catch(Exception e) {
	        	if (showErrorMessages) {
	        		System.err.println("drawSuccessfulAttack Exception");
	        		System.err.println(e.getMessage());
	        	}
	        }
    	}
    }
    public void drawSuccessfulAttack(CombatStack source, CombatStack target, float dmg, int count, float force) {
    	if (showErrors)
    		 tech().drawSuccessfulAttack(source, target, source.weaponNum(this), dmg, count, force);
    	else {
            try {
                tech().drawSuccessfulAttack(source, target, source.weaponNum(this), dmg, count, force);
            }
            catch(Exception e) {
            	if (showErrorMessages) {
    	        	System.err.println("drawSuccessfulAttack Exception");
    	        	System.err.println(e.getMessage());
            	}
            }
    	}
    }
}
