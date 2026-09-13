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
package rotp.model.tech;

import rotp.model.empires.Empire;
import rotp.model.game.DefaultValues;
import rotp.model.game.IBaseOptsTools;
import rotp.model.ships.ShipDesign;
import rotp.model.ships.ShipEngine;
import rotp.model.ships.ShipManeuver;
import rotp.ui.main.overlay.MapOverlayAdvice;
import rotp.ui.util.ParamBoolean;

public final class TechEngineWarp extends Tech {
	public static final String KEY = "EngineWarp";
	private static final int[] baseManeuverSize = {2, 15, 100, 700};
    private int baseWarp;
    //public String shName;

    TechEngineWarp(String typeId, int lv, int seq, boolean b, TechCategory c) {
        id(typeId, seq);
        typeSeq = seq;
        level = lv;
        cat = c;
        free = b;
        init();
    }
	@Override public String detail()		{
		float warp = warp();
		if (warp == (int) warp) 
			return detail((int) warp);
		else
			return detail(df1.format(warp)); 
	}
    @Override
    public boolean canBeMiniaturized()      { return true; }
    @Override
    public void init() {
        super.init();
        techType = Tech.ENGINE_WARP;

        switch(typeSeq) {
            case 0: baseWarp = 1; break;
            case 1: baseWarp = 2; break;
            case 2: baseWarp = 3; break;
            case 3: baseWarp = 4; break;
            case 4: baseWarp = 5; break;
            case 5: baseWarp = 6; break;
            case 6: baseWarp = 7; break;
            case 7: baseWarp = 8; break;
            case 8: baseWarp = 9; break;
        }
    }
	public float transportTravelSpeed()	{ return max(1, (extendedWarp() - 1)) * options().selectedWarpSpeedFactor(); }
	public int transportCombatSpeed()	{ return max(1, baseWarp - 1); }
	public int baseWarp()				{ return baseWarp; }
	public int extendedWarp()			{ return options().extendedWarp(this); }
	public float warp()					{ return options().warpSpeed(this); }
    @Override public float warModeFactor()			{ return 1.5f; }
    @Override public float expansionModeFactor()	{ return 2; }
    @Override public boolean providesShipComponent(){ return true; }
    @Override public boolean isObsolete(Empire c)	{ return baseWarp() < c.tech().topBaseSpeed(); }
    @Override public float baseValue(Empire c)		{ return c.ai().scientist().baseValue(this); }
    @Override
    public void provideBenefits(Empire c) {
        super.provideBenefits(c);
        if (!isObsolete(c))
            c.tech().topEngineWarpTech(this);

        ShipEngine sh = new ShipEngine(this);
        c.shipLab().addEngine(sh);

        // when engine tech is learned, ship maneuvers for all
        // lower engine techs become available. Add them if the
        // design lab does not have them yet.
        for (Tech t: c.tech().allTechsOfType(techType)) {
            TechEngineWarp tech = (TechEngineWarp) t;
            if (tech.level <= level) {
                if (!c.shipLab().hasManeuverForTech(tech)) {
                    ShipManeuver sh2 = new ShipManeuver(tech);
                    c.shipLab().addManeuver(sh2);
                }
            }
        }

        if (c.isPlayerControlled() && (baseWarp > 1))
            galaxy().giveAdvice(MapOverlayAdvice.MAIN_ADVISOR_SHIP_ENGINE);
    }
    @Override
    public float baseCost() {
        return baseWarp * 2;
    }
    public float powerOutput() { return baseWarp * 10; }
    @Override
    public float baseSize(ShipDesign d) {
        switch(baseWarp) {
            case 1: return 10;
            case 2: return 18;
            case 3: return 26;
            case 4: return 33;
            case 5: return 36;
            case 6: return 40;
            case 7: return 44;
            case 8: return 47;
            case 9: return 50;
        }
        return (23 + (baseWarp * 3));
    }
	private float baseManeuverSize(int size)	{ return (size>=0 && size<=4) ? baseManeuverSize[size] * baseWarp : 0; }
	public float baseManeuverPower(int size, int engineWarp) {
		return moo1ManeuverPower.get() ? baseManeuverSize(size) : baseManeuverSize(size) / engineWarp;
	}
	@Override public boolean isEngineWarpTech()	{ return true; }

	public static final ParamBoolean moo1ManeuverPower = new ParamBoolean(IBaseOptsTools.MOD_UI, "MOO1_MANEUVER_PWR", false)
			.setDefaultValue(DefaultValues.MOO1_DEFAULT, true);
}
