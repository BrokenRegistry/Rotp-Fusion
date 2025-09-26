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
package rotp.model.planet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import rotp.model.colony.Colony;
import rotp.model.colony.GovWorksheet;
import rotp.model.empires.Empire;
import rotp.model.events.SystemTerraformingEvent;
import rotp.model.galaxy.IMappedObject;
import rotp.model.galaxy.NamedObject;
import rotp.model.galaxy.ShipFleet;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GameSession;
import rotp.model.tech.TechAtmosphereEnrichment;
import rotp.model.tech.TechSoilEnrichment;
import rotp.model.tech.TechTree;
import rotp.ui.util.planets.Sphere2D;
import rotp.ui.util.planets.SphereShadowPaint;
import rotp.util.Base;
import rotp.util.ColorRange;
import rotp.util.FastImage;

public class Planet implements Base, IMappedObject, Serializable {
    private static final long serialVersionUID = 1L;
    public static final int creationSizeMax	= 120;
    public static final int creationSizeMin	= 10;
    public static final int baseSizeMax		= 180;
    public static final int baseSizeMin		= creationSizeMin;
    public static final int finalSizeMax	= baseSizeMax + 120;
    public static int COUNT = 0;

    private static final SphereShadowPaint ssp = new SphereShadowPaint();
    private static BufferedImage buffer1, buffer2;
    private static BufferedImage lastBuffer;
    private static final HashMap<Integer, BufferedImage> sphereShadows = new HashMap<>();
    private static final ColorRange OCEAN_WATER_C = new ColorRange(new Color(154,140,155), new Color(0,20,100));
    private static final Color OCEAN_TOXIC_C = new Color(103,29,16);
    private static final ColorRange TERRAINLO_TERRAN_C = new ColorRange(new Color(141,131,95), new Color(92,114,75));
    private static final ColorRange TERRAINHI_TERRAN_C = new ColorRange(new Color(155,121,94), new Color(155,155,94));
    private static final Color TERRAIN_TOXIC_HI = new Color(237,136,44);
    private static final Color TERRAIN_TOXIC_LO = new Color(136,80,33);
    private static final Color NONE_C = new Color(0,0,0,0);
    private static final ColorRange CLOUD_TOXIC_C = new ColorRange(new Color(246,182,95,25), new Color(233,171,88,25)); // titan atmo
    private static final Color CLOUD_INFERNO_C = new Color(228,227,225); // venus atmo
    private static final Color CLOUD_TERRAN_C = Color.white;
    private static final Color TERRAIN_ICE_C = new Color(186,194,201);
    private static final Color TERRAIN_RUST_C1 = new Color(88,58,50);  // mars dark surface
    private static final Color TERRAIN_RUST_C2 = new Color(187,111,85); // mars bright surface
    private static final Color TERRAIN_RADIATED_C1 = new Color(137,103,75); // mars bright surface
    private static final Color TERRAIN_RADIATED_C2 = new Color(132,116,93); // mars bright surface
    private static final int ENVIRONMENT_NONE = -1;
    private static final int ENVIRONMENT_HOSTILE = 0;
    private static final int ENVIRONMENT_NORMAL = 1;
    private static final int ENVIRONMENT_FERTILE = 2;
    private static final int ENVIRONMENT_GAIA = 3;
    private static final int ULTRA_POOR = 1;
    private static final int POOR = 2;
    private static final int NORMAL = 3;
    private static final int RICH = 4;
    private static final int ULTRA_RICH = 5;
    private static final int NO_ARTIFACTS = 0;
    private static final int RUINS_ANTARAN = 6;
    private static final int RUINS_ORION = 7;
    private static final int ASTEROID_DENSITY_NONE = 0;
    private static final int ASTEROID_DENSITY_LOW  = 1;
    private static final int ASTEROID_DENSITY_HIGH = 2;

    private String planetTypeKey;
    private final StarSystem system;

    private float baseSize = 0;
    private int terraformLevel = 0;
    private int environment = ENVIRONMENT_NORMAL;
    private int resources = NORMAL;
    private int artifacts = NO_ARTIFACTS;
    private int bonusTechs = 0;
    private float waste = 0;

    private int founderId = Empire.NULL_ID;
    private Colony colony;

    // vars for visual display
    private int rotationDirection = 1;
    private String landscapeKey;
    private int iceLevel = 0;
    private int terrainSeed = 0;
    private float oceanPct = 0;
    private int cloudThickness = 0;  //200 nothing, 550 all white, 400-450 terran
    private final int[] alienFactories;
    // private Integer asteroidsMin, asteroidsMax;
    private Integer asteroidsDensity;

    // vars used for sprite drawing
    public Color terrainColor1 = Color.green;
    public Color terrainColor2 = Color.green.darker();
    public Color oceanColor = Color.blue;
    public Color cloudColor = Color.white;
    public Color iceColor = Color.white;
    public int oceanLevel;

    public transient float viewPct;
    private transient PlanetType type;

    public PlanetType type()               {
        if (type == null)
            type = PlanetType.keyed(planetTypeKey);
        return type;
    }
    private void initAsteroids()           {
    	int[] probabilities = type().asteroidProbability(planetTypeKey);
    	switch (resources) {
	    	case RICH:
	    		probabilities[0] += options().richNoAsteroidsModPct();
	    		break;
	    	case ULTRA_RICH:
	    		probabilities[0] += options().ultraRichNoAsteroidsModPct();
	    		break;
    	}
    	float rand = 100 * random() - probabilities[0];
    	if (rand < 0) { // No asteroids
    		asteroidsDensity = ASTEROID_DENSITY_NONE;
    		return;
    	}
    	rand -= probabilities[1];
    	if(rand < 0) { // Low density
    		asteroidsDensity = ASTEROID_DENSITY_LOW;
    		return;
    	}
    	// High density
    	asteroidsDensity = ASTEROID_DENSITY_HIGH;
    }
    public int asteroidsCount()            {
    	if (asteroidsDensity == null)
    		initAsteroids();
    	switch (asteroidsDensity) {
	    	case ASTEROID_DENSITY_HIGH:
	        	int highMin = options().minHighAsteroids();
	        	int highMax = options().maxHighAsteroids();
	        	return roll(min(highMin, highMax), max(highMin, highMax));
	    	case ASTEROID_DENSITY_LOW:
	        	int lowMin = options().minLowAsteroids();
	        	int lowMax = options().maxLowAsteroids();
	        	return roll(min(lowMin, lowMax), max(lowMin, lowMax));
	    	case ASTEROID_DENSITY_NONE:
	    	default:
	    		return 0;
    	}
    }
    
    public int iceLevel()                  { return iceLevel; }
    public int cloudThickness()            { return cloudThickness; }
    public int terrainSeed()               { return terrainSeed; }
    public void terrainSeed(int i)         { terrainSeed = i; }
    public float terrainVal()              { return (float) terrainSeed / PlanetType.TERRAIN_MAX; }
    public int environment()               { return environment; }
    public float oceanPct()                { return oceanPct; }
    public void degradeEnvironment()       { environment = min(environment(), ENVIRONMENT_NORMAL); }
    public void makeEnvironmentNone()      { environment = ENVIRONMENT_NONE; }
    public void makeEnvironmentHostile()   { environment = ENVIRONMENT_HOSTILE; }
    public void makeEnvironmentNormal()    { environment = ENVIRONMENT_NORMAL; }
    public void makeEnvironmentFertile()   { environment = ENVIRONMENT_FERTILE; }
    public void makeEnvironmentGaia()      { environment = ENVIRONMENT_GAIA; }

    public boolean isResourceUltraPoor()   { return resources == ULTRA_POOR; }
    public boolean isResourcePoor()        { return resources == POOR; }
    public boolean isResourceNormal()      { return resources == NORMAL; }
    public boolean isResourceRich()        { return resources == RICH; }
    public boolean isResourceUltraRich()   { return resources == ULTRA_RICH; }

    private int resources()                { return resources; }
    public void depleteResources()         { resources = max(ULTRA_POOR, resources-1); }
    public void enrichResources()          { resources = min(ULTRA_RICH, resources+1); }
    public void setResourceUltraPoor()     { resources = ULTRA_POOR; }
    public void setResourcePoor()          { resources = POOR; }
    public void setResourceNormal()        { resources = NORMAL; }
    public void setResourceRich()          { resources = RICH; }
    public void setResourceUltraRich()     { resources = ULTRA_RICH; }

    public int resourcesSort() {
        return artifacts > 0 ? artifacts() : resources();
    }
    public int artifacts()                 { return artifacts; }
    public void removeArtifact()           { // BR: easier to remove than to test if allowed!
        artifacts = NO_ARTIFACTS;
        bonusTechs = 0;
    }
    public void setArtifact()              {
        artifacts = RUINS_ANTARAN;
        bonusTechs = 1;
    }
	// modnar: setArtifactRace for setting Race Homeworld to be Artifact
	public void setArtifactRace()          { artifacts = RUINS_ANTARAN; }
	// BR: even more Scientific race
	public void setOrionRace()             { artifacts = RUINS_ORION; }
    public void setOrionArtifact()         { 
        artifacts = RUINS_ORION; 
        bonusTechs = 3;
    }
    public boolean noArtifacts()           { return artifacts == NO_ARTIFACTS; }
    public boolean isAntaran()             { return artifacts == RUINS_ANTARAN; } // BR:
    public boolean isArtifact()            { return (artifacts == RUINS_ORION) || (artifacts == RUINS_ANTARAN); }
    public boolean isOrionArtifact()       { return artifacts == RUINS_ORION; }
    public boolean hasBonusTechs()         { return bonusTechs > 0; }
    public int bonusTechs()                { return bonusTechs; }
    public void plunderBonusTech()         { bonusTechs = 0; }

    public String ruinsKey()               {
        switch(artifacts) {
            case RUINS_ORION   : return "LANDSCAPE_RUINS_ORION";
            case RUINS_ANTARAN : return "LANDSCAPE_RUINS_ANTARAN";
        }
        return "";
    }

    public boolean isEnvironmentFriendly() { return isEnvironmentNormal() || isEnvironmentFertile() || isEnvironmentGaia(); }
    public boolean isEnvironmentNone()     { return environment == ENVIRONMENT_NONE; }
    public boolean isEnvironmentHostile()  { return environment == ENVIRONMENT_HOSTILE; }
    public boolean isEnvironmentNormal()   { return environment == ENVIRONMENT_NORMAL; }
    public boolean isEnvironmentFertile()  { return environment == ENVIRONMENT_FERTILE; }
    public boolean isEnvironmentGaia()     { return environment == ENVIRONMENT_GAIA; }

    public void irradiateEnvironment(int newSize) {
        Empire systemEmp = null;
        initPlanetType(PlanetType.RADIATED);
        if (newSize<=0)
        baseSize(type().randomSize());
        else
        	baseSize(newSize);
        environment = ENVIRONMENT_HOSTILE;
        terraformLevel = 0;

        if (colony != null) {
            systemEmp = colony.empire();
            colony.setPopulation(min(baseSize, colony.population()));
            colony.empire().sv.refreshFullScan(starSystem().id);
        }

        // update system views
        List<ShipFleet> fleets = starSystem().orbitingFleets();
        for (ShipFleet fl: fleets)
            fl.empire().sv.refreshFullScan(starSystem().id);
        if (systemEmp != null)
            systemEmp.sv.refreshFullScan(starSystem().id);
    }
    public void degradeToType(String pType) {      
        PlanetType deadType = PlanetType.keyed(pType);
        if (type().hostility() < deadType.hostility()) {
            initPlanetType(pType);
            baseSize(type().randomSize());
        }
        environment = ENVIRONMENT_HOSTILE;
        terraformLevel = 0;
        waste = min(waste, maxWaste());
    	initTerrain(type()); // BR: Make the environment follow!
    }
    public void sufferImpactEvent(NamedObject impactor) {
        Empire systemEmp = null;
        if (colony != null) {
            systemEmp = colony.empire();
            systemEmp.lastAttacker(impactor);
            colony.destroy();
        }

        // if this was a terran-type planet, change it to Barren
        if (!type().hostileToTerrans()) {
            initPlanetType(PlanetType.BARREN);
            baseSize(type().randomSize());
        }

        starSystem().abandoned(false);
        environment = ENVIRONMENT_HOSTILE;
        terraformLevel = 0;

        // update system views
        List<ShipFleet> fleets = starSystem().orbitingFleetsNoMonster();
        for (ShipFleet fl: fleets)
            fl.empire().sv.refreshFullScan(starSystem().id);
        if (systemEmp != null)
            systemEmp.sv.refreshFullScan(starSystem().id);
    }
    public int rotationDirection()         { return rotationDirection; }

    public Planet(StarSystem s) {
        COUNT++;
        system = s;
        rotationDirection = random() < 0.5 ? -1 : 1;
        alienFactories = new int[galaxy().numEmpires()];
    }
    public void initPlanetType(String ptype) {
        type = null;
        planetTypeKey = ptype;
        PlanetType pt = type();
        initTerrain(pt);
//        terrainSeed = pt.randomTerrainSeed();
//        landscapeKey = pt.randomLandscapeKey();
//        oceanPct = pt.randomOceanPct();
//        iceLevel = pt.randomIceLevel();
//        cloudThickness = pt.randomCloudThickness();
//        initColors();
    }
    public int alienFactories(int empId)  { return alienFactories[empId]; }
    public void addAlienFactories(int empId, int factories) {
        alienFactories[empId] += factories;
    }
    public void removeAlienFactories(int empId) { alienFactories[empId] = 0; }
    public int numAlienFactories() {
        int numFactories = 0;
        for (int i:alienFactories)
            numFactories += i;
        return numFactories;
    }
    public int randomAlienFactoryEmpire() {
        int num = 0;
        int[] empIds = new int[alienFactories.length];
        for (int i=0;i<alienFactories.length;i++) {
            if (alienFactories[i] >0)
                empIds[num++] = i;
        }
        if (num == 0)
            return Empire.NULL_ID;
        int r = roll(1,num) - 1;
        return empIds[r];
    }
    public Image landscapeImage()         { return image(landscapeKey); }
    // MappedObject overrides
    @Override
    public float x()               { return system.x();  }
    @Override
    public float y()               { return system.y();  }

    public float baseSize()        { return baseSize; }
    public void baseSize(float d)  { baseSize = d; }
    // max base size for planet (before general terraforming) is 180
    public void increaseBaseSize(float amt) {
        float maxSize = baseSizeMax*session().populationBonus();
        float newSize = Math.min(maxSize, baseSize()+amt);
        baseSize(newSize);
    }
    public float currentSize()      { return baseSize() + terraformLevel; }
    public StarSystem starSystem()  { return system; }
    public Colony colony()          { return colony; }
    public void setColony(Colony c) { colony = c; }

    public float waste()           { return waste; }
    public void resetWaste()       { waste = 0; }
    public void addWaste(float w)  { waste = waste+w; }
    public float maxWaste()        { return currentSize() * 0.9f; }
    public float sizeAfterWaste()  { return currentSize() - min(maxWaste(), waste()); }
    public void removeExcessWaste() { waste = min(maxWaste(), waste); }

    public void resetBiosphere()    {
        terraformLevel= 0;
        if (isColonized())
            colony().ecology().resetBiosphere();
    }
    public float potentialSize(TechTree tech)	{
    	TechSoilEnrichment soil = tech.topSoilEnrichmentTech();
    	TechAtmosphereEnrichment atmo = tech.topAtmoEnrichmentTech();
    	float newSize = baseSize();
    	int level = -1;
    	if (soil != null)
    		level = soil.typeSeq;
    	if (isEnvironmentHostile() && atmo != null) {
			newSize += atmosphereIncrease();
    		if (level >= 0)
    			newSize += fertileIncrease(newSize);
    		if (level >= 1)
    			newSize += gaiaIncrease(newSize);
    		return newSize;
		}
    	if (isEnvironmentNormal()) {
    		if (level >= 0)
    			newSize += fertileIncrease(newSize);
    		if (level >= 1)
    			newSize += gaiaIncrease(newSize);
    		return newSize;
    	}
    	if (isEnvironmentFertile()) {
    		if (level >= 1)
    			newSize += gaiaIncrease(newSize);
    		return newSize;
    	}
    	return newSize;
    }
    public void potentialImprovement(TechTree tech, GovWorksheet gws)	{
    	TechSoilEnrichment soil = tech.topSoilEnrichmentTech();
    	TechAtmosphereEnrichment atmo = tech.topAtmoEnrichmentTech();
    	float terraformAdj = tech.terraformAdj();
    	float newSize = baseSize();
    	int level = -1;
    	if (soil != null)
    		level = soil.typeSeq;
    	if (isEnvironmentHostile()) {
    		if (atmo != null) {
        		gws.canTerraformAtmosphere = true;
        		gws.anyTerraform = true;
        		gws.atmosphereIncrease = atmosphereIncrease(); 
    			newSize += gws.atmosphereIncrease;
        		if (level >= 0) {
        			gws.canEnrichSoil = true;
        			gws.enrichIncrease = fertileIncrease(newSize);
        			newSize += gws.enrichIncrease;
        		}
        		if (level >= 1)
        			gws.enrichIncrease += gaiaIncrease(newSize);
        		if (terraformAdj > terraformLevel) {
        			gws.canTerraform = true;
        			gws.anyTerraform = true;
        			gws.terraformIncrease = terraformAdj - terraformLevel;
        		}
        		return;
    		}
    		else {
    			terraformAdj *= options().hostileTerraformingPct();
    	    	if (terraformAdj > terraformLevel) {
    				gws.canTerraform = true;
    				gws.anyTerraform = true;
    				gws.terraformIncrease = terraformAdj - terraformLevel;
    			}
    		}
    	}
    	else if (isEnvironmentNormal()) {
    		if (level >= 0) {
    			gws.canEnrichSoil = true;
    			gws.anyTerraform = true;
    			gws.enrichIncrease = fertileIncrease(newSize);
    			newSize += gws.enrichIncrease;
    		}
    		if (level >= 1)
    			gws.enrichIncrease += gaiaIncrease(newSize);
    		if (terraformAdj > terraformLevel) {
    			gws.canTerraform = true;
    			gws.anyTerraform = true;
    			gws.terraformIncrease = terraformAdj - terraformLevel;
    		}
    		return;
    	}
    	else if (isEnvironmentFertile()) {
    		if (level >= 1) {
    			gws.canEnrichSoil = true;
    			gws.anyTerraform = true;
    			gws.enrichIncrease = gaiaIncrease(newSize);
    		}
    		if (terraformAdj > terraformLevel) {
    			gws.canTerraform = true;
    			gws.anyTerraform = true;
    			gws.terraformIncrease = terraformAdj - terraformLevel;
    		}
    		return;
    	}
    }
    private float fertileIncrease(float size) { return (float)Math.ceil(size/20.0f) * 5; }
    private float gaiaIncrease(float size)    { return (float)Math.ceil((size-10)/20.0f) * 5; }
    private int   atmosphereIncrease()        { return (int) (20*session().populationBonus()); }
    public void enrichSoil() {
        if (isEnvironmentNormal()) {
            starSystem().addEvent(new SystemTerraformingEvent("SYSEVENT_SOIL_ENRICHED"));
            makeEnvironmentFertile();
            float incr = fertileIncrease(baseSize());
            increaseBaseSize(incr);
        }
        else if (isEnvironmentFertile()) {
            starSystem().addEvent(new SystemTerraformingEvent("SYSEVENT_GAIA_TERRAFORMING"));
            makeEnvironmentGaia();
            float incr = gaiaIncrease(baseSize());
            increaseBaseSize(incr);
        }
        if (colony != null)
        	colony.empire().sv.refreshFullScan(starSystem().id);
        	// BR: To reset the view
    }
    public void terraformAtmosphere() {
        // change env to NORMAL
        // upgrade type to MINIMAL
        // add 20 to planet size
        if (isEnvironmentHostile()) {
            environment = Math.max(environment, ENVIRONMENT_NORMAL);
            planetTypeKey = PlanetType.MINIMAL;
            int incr = atmosphereIncrease();
            increaseBaseSize(incr);
            type = null;
        	// type();
        	initTerrain(type()); // BR: Make the environment follow!
            starSystem().addEvent(new SystemTerraformingEvent("SYSEVENT_ATMOSPHERE_TERRAFORMED"));
            if (colony != null)
            	colony.empire().sv.refreshFullScan(starSystem().id);
            	// BR: To reset the view
        }
    }
    public void terraformBiosphere(float amt) {
        terraformLevel += amt;
    }
    public Empire empire() { return colony == null ? null : colony.empire(); }
    public boolean canTerraformAtmosphere(Empire civ) {
        return (isEnvironmentHostile() && civ.tech().canTerraformHostile());
    }
    public Colony becomeColonized(Empire c) {
        if (founderId == Empire.NULL_ID)
            founderId = c.id;
        colony = new Colony(c, this);
        colony.industry().factories(alienFactories(c.id));
        removeAlienFactories(c.id);
        return colony;
    }
    public boolean isColonized() { return (colony != null); }
    public int founderId()       { return founderId; }

    public float researchAdj() {
        switch (artifacts) {
            case RUINS_ORION:    return 4; // Orion ruins  // modnar: change Orion to MoO1 4X Tech
            case RUINS_ANTARAN:  return 2; // Antaran ruins
            default: return 1;
        }
    }
    public float productionAdj() {
        switch (resources) {
            case ULTRA_POOR: return .33f;
            case POOR:       return .5f;
            case NORMAL:     return 1;
            case RICH:       return 2;
            case ULTRA_RICH: return 3;
            default:         return 1;
        }
    }
    public float growthAdj() {
        switch (environment) {
            case ENVIRONMENT_HOSTILE: return .5f;
            case ENVIRONMENT_FERTILE: return 1.5f;
            case ENVIRONMENT_GAIA: return 2;
            case ENVIRONMENT_NORMAL:
            default: return 1;
        }
    }
    public float ultimateMaxSize() { return isColonized() ? colony.ultimateMaxSize() : baseSize(); }
    public float maxSize()         { return isColonized() ? colony.maxSize() : baseSize(); }
    public float maxSizeAfterSoilAtmoTform() {
        float size = maxSize();
        int tempEnv = environment();
        Empire emp = empire();
        
        // if hostile, can we t-form atmosphere?
        if (canTerraformAtmosphere(emp)) {
            size += emp.tech().topAtmoEnrichmentTech().sizeIncrease(baseSize());
            tempEnv = ENVIRONMENT_NORMAL;
        }
        
        // still hostile, fall out
        if (tempEnv == ENVIRONMENT_HOSTILE)
            return size;
        
        // check if known soil enrich tech is better than this planet's 
        // current environment. If not, fall out
        TechSoilEnrichment soilTech = emp.tech().topSoilEnrichmentTech();
        if (soilTech == null)
            return size;
   
        // if we are not fertile and can be made fertile, count that
        if ((tempEnv == ENVIRONMENT_NORMAL) && (soilTech.environment > ENVIRONMENT_NORMAL)) {
            tempEnv = ENVIRONMENT_FERTILE;
            size += fertileIncrease(baseSize());
        }
        
        // if we are not gaia and can be made gaia, count that
        if ((tempEnv == ENVIRONMENT_FERTILE) && (soilTech.environment > ENVIRONMENT_FERTILE)) {
            size += gaiaIncrease(baseSize());
        }

        return size;
    }
    public float normalPopGrowth(float currentPopulation) { return normalPopGrowth(currentPopulation, empire()); }
    public float normalPopGrowth(float currentPopulation, Empire civ) {
        float maxNewPopulation = currentSize() - currentPopulation;
        float baseGrowthRate = max(0, (1 - (currentPopulation / currentSize())) / 10);
        switch (options().selectedPopGrowthFactor()) {
            case "Reduced":
                baseGrowthRate = baseGrowthRate / 2;
        }
        if (civ != null) {
            baseGrowthRate *= civ.growthRateMod();
            if (!civ.ignoresPlanetEnvironment())
                baseGrowthRate *= growthAdj();
        }

        // always at least .1 base growth in pop
        float newGrownPopulation = max(.1f, currentPopulation * baseGrowthRate);
        newGrownPopulation = min(newGrownPopulation, maxNewPopulation);

        return newGrownPopulation;
    }

    private void initTerrain(PlanetType pt) { // BR:
        terrainSeed = pt.randomTerrainSeed();
        landscapeKey = pt.randomLandscapeKey();
        oceanPct = pt.randomOceanPct();
        iceLevel = pt.randomIceLevel();
        cloudThickness = pt.randomCloudThickness();
        initColors();
    }
    private void initColors() {
        switch(type().key()) {
            case PlanetType.OCEAN:
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor1 = TERRAINHI_TERRAN_C.color(pow(oceanPct(), 3));
                terrainColor2 = TERRAINLO_TERRAN_C.color(pow(oceanPct(), 3));
                break;
            case PlanetType.JUNGLE:
                // use Lefebre2 color map
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor1 = TERRAINHI_TERRAN_C.color(pow(oceanPct(), 3));
                terrainColor2 = TERRAINLO_TERRAN_C.color(pow(oceanPct(), 3));
                break;
            case PlanetType.TERRAN:
                // use Lefebre
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor1 = TERRAINHI_TERRAN_C.color((float)Math.pow(oceanPct(),0.25));
                terrainColor2 = TERRAINLO_TERRAN_C.color((float)Math.pow(oceanPct(), 0.25));
                break;
            case PlanetType.STEPPE:
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor1 = TERRAINHI_TERRAN_C.color(sqrt(oceanPct()));
                terrainColor2 = TERRAINLO_TERRAN_C.color(sqrt(oceanPct()));
                break;
            case PlanetType.ARID:
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor1 = TERRAINHI_TERRAN_C.color(oceanPct());
                terrainColor2 = TERRAINLO_TERRAN_C.color(oceanPct());
                break;
            case PlanetType.DESERT:
                // use Bathymetric color map
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor1 = TERRAINHI_TERRAN_C.color(oceanPct());
                terrainColor2 = TERRAINLO_TERRAN_C.color(oceanPct());
                break;
            case PlanetType.MINIMAL:
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor1 = TERRAINHI_TERRAN_C.color(sqrt(oceanPct()));
                terrainColor2 = TERRAINLO_TERRAN_C.color(sqrt(oceanPct()));
                break;
            case PlanetType.BARREN:
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor1 = TERRAINHI_TERRAN_C.color(sqrt(oceanPct()));
                terrainColor2 = TERRAINLO_TERRAN_C.color(sqrt(oceanPct()));
                break;
            case PlanetType.TUNDRA:
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor1 = TERRAIN_ICE_C;
                terrainColor2 = TERRAIN_ICE_C;
                break;
            case PlanetType.DEAD:
                // use Mars
                oceanColor = OCEAN_WATER_C.color(sqrt(oceanPct()));
                cloudColor = CLOUD_TERRAN_C;
                terrainColor2 = TERRAIN_RUST_C1;
                terrainColor1 = TERRAIN_RUST_C2;
                break;
            case PlanetType.INFERNO:
                oceanColor = NONE_C;
                cloudColor = CLOUD_INFERNO_C;
                terrainColor2 = TERRAIN_RUST_C1;
                terrainColor1 = TERRAIN_RUST_C2;
                break;
            case PlanetType.TOXIC:
                oceanColor = OCEAN_TOXIC_C;
                cloudColor = CLOUD_TOXIC_C.randomColor();
                terrainColor1 = TERRAIN_TOXIC_HI;
                terrainColor2 = TERRAIN_TOXIC_LO;
                break;
            case PlanetType.RADIATED:
                oceanColor = NONE_C;
                cloudColor = NONE_C;
                terrainColor2 = TERRAIN_RADIATED_C1;
                terrainColor1 = TERRAIN_RADIATED_C2;
                break;
        }
    }
    public void rotate(float n)    {
        // delta should be between -1 and 1
        float delta = .001f*n*rotationDirection();
        delta = delta - (int) delta;

        viewPct += delta;

        // view pct should be  between 0 and 1
        if (viewPct < 0)
            viewPct++;
        if (viewPct > 1)
            viewPct--;
    }
    public BufferedImage draw(Graphics g, int width, int height, int x0, int y0, int size, int lightDirection) {
        lastBuffer = image(size,lightDirection);
        if (lastBuffer == null)
            return null;

        int r = min(200,size/2);
        int newWidth = r+r;
        int newHeight = r+r;
        // startX & startY, if positive, represent the left & top margin for the image
        // if negative, they represent the right & bottom margins
        if (x0 < 0)
            x0 = (width - newWidth + x0);
        if (y0 < 0)
            y0 = (height - newHeight + y0);
        g.drawImage(lastBuffer, x0, y0, x0+size, y0+size, 0, 0, lastBuffer.getWidth(), lastBuffer.getHeight(), null);

        return lastBuffer;
    }
    public BufferedImage image(int desiredW, int lightDirection) {
        if (type().isAsteroids())
            return null;

        int w = Math.min(2*Sphere2D.SMALL_PLANET_R,desiredW);
        lastBuffer = nextBuffer(w, w);

        Graphics2D g = lastBuffer.createGraphics();

        // DRAW TERRAIN
        BufferedImage planetImg = terrainSphere(desiredW).image();
        g.drawImage(planetImg, 0, 0, w, w, 0, 0, planetImg.getWidth(), planetImg.getHeight(), null);

        // DRAW SHADOW
        if (lightDirection >= 0) {
            BufferedImage shadowImg = shadowImage(lightDirection);
            g.drawImage(shadowImg, 0, 0, w, w, 0, 0, shadowImg.getWidth(), shadowImg.getHeight(), null);
        }
        g.dispose();
        return lastBuffer;
    }
    public void prepareImage() {
        if (type().isAsteroids())
            return;
        if (type().smallSphere(this) == null) {
            log("Generating sphere for: ", type().toString());
            GameSession.instance().smallSphereService().submit(sphereGenerator(Sphere2D.SMALL_PLANET_R));
            generate2DSphere(Sphere2D.FAST_PLANET_R);
        }
    }
    public Runnable sphereGenerator(final int radius) {
        return () -> { generate2DSphere(radius); };
    }
    public FastImage terrainSphere(int desiredW) {
        Sphere2D sphere = sphere2d(desiredW);

        if (sphere == null)
            err("Sphere2D was NULL!");
        return sphere2d(desiredW).image(viewPct);
    }
    public Sphere2D sphere2d(int desiredW) {
        if  (type().isAsteroids())
            return null;

        prepareImage();
        if (type().smallSphere(this) == null)
            err("Sphere is null for: ", type().toString());
        return type().smallSphere(this);
    }
    public void generate2DSphere(int radius) {
        if (type().sphereResolution(this) >= radius)
            return;

        log("Generate2DSphere()  sphereResolution:" + type().sphereResolution(this)+", radius:" + radius);
        // generate height map and resultant terrain grayscale image
        //long start = System.currentTimeMillis();
        //log("Generating height map, raidus: ", str(radius), " for planet ", planet.starSystem().name());
        PlanetHeightMap heightMap =  new PlanetHeightMap(terrainVal(), radius, oceanPct());
        //log("Time to generate: ", str(System.currentTimeMillis()-start), "ms");
        oceanLevel  = heightMap.seaLevel() - Byte.MIN_VALUE;
        FastImage terrainImg = FastImage.fromHeightMap(heightMap);

        // get a random cloud img and overlay onto terrain img
        //FastImage cloudImg = PlanetImager.current().randomCloudImage(this, terrainImg.getWidth(), terrainImg.getHeight());
        FastImage cloudImg = null;
        Sphere2D newSphere = new Sphere2D(terrainImg, cloudImg, this);

        if (radius <= Sphere2D.SMALL_PLANET_R)
            type().smallSphere(newSphere, this);
    }
    private BufferedImage shadowImage(int direction) {
        if (!sphereShadows.containsKey(direction))
            createShadowImage(direction);
        return sphereShadows.get(direction);
    }
    private void createShadowImage(int direction) {
        int r = 100;
        RoundRectangle2D rect = new RoundRectangle2D.Float(0,0, r+r, r+r, 0, 0);
        BufferedImage img = new BufferedImage(r+r, r+r, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        ssp.set(r, r, new Point2D.Float(0, r), Color.black, direction, 0.1f);
        g.setPaint(ssp);
        g.fill(rect);
        g.dispose();
        sphereShadows.put(direction, img);
    }
    private BufferedImage nextBuffer(int w, int h) {
        if ((buffer1 == null)
        || (buffer1.getWidth() != w) || (buffer1.getHeight() != h))  {
            buffer1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            return buffer1;
        }
        if ((buffer2 == null)
        || (buffer2.getWidth() != w) || (buffer2.getHeight() != h))  {
            buffer2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            return buffer2;
        }

        // both buffer1 & 2 exist and are the right size
        // return the one that's not the active buffer
        if (buffer1 == lastBuffer)
            return buffer2;
        else
            return buffer1;
    }

    public float monsterPlanetValue() {
        float size = currentSize(); // planet size
        
        // increase planet value depending on size (value is normalized below)
        // (4*pow(SIZE, 0.7)
        float val = (float) (4.0 * Math.pow(size, 0.7f));

        // Higher desire value for Rich, Ultra-Rich, Artifacts
        // Lower desire value for Poor, Ultra-Poor
        // modnar: increase values for poor/ultra-poor
        if (isResourceUltraPoor())
            val *= 0.6;
        else if (isResourcePoor())
            val *= 0.75;
        else if (isResourceNormal())
            val *= 1;
        else if (isResourceRich())
            val *= 2;
        else if (isResourceUltraRich())
            val *= 3;

        //float for artifacts, triple for super-artifacts
        if (isAntaran())
            val *= 2;
        else if (isOrionArtifact())
            val *= 3;

        // normalized to size 100
        return val/100f;
    }
    // BR: For Symmetric galaxies
    void copy(Planet src) {
    	this.environment = src.environment();
    	this.resources   = src.resources();
    	this.artifacts   = src.artifacts();
    	this.bonusTechs  = src.bonusTechs();
    	this.baseSize    = src.baseSize();
    }
    // BR: For Symmetric galaxies and Restart
    void copy(PlanetBaseData src) {
    	baseSize		= src.baseSize;
    	terraformLevel	= src.terraformLevel;
    	environment		= src.environment;
    	resources		= src.resources;
    	artifacts		= src.artifacts;
    	bonusTechs		= src.bonusTechs;
    	// waste		= src.waste; // for future option: starting with random waste!
    }
	// ==================== PlanetBaseData ====================
	//
	public static class PlanetBaseData {
		String planetTypeKey;
		private float baseSize;
		private int terraformLevel;
		private int environment;
		private int resources;
		private int artifacts;
		private int bonusTechs;
		// public float waste; // for future option: starting with random waste!
		public PlanetBaseData(Planet src) {
			planetTypeKey	= src.planetTypeKey;
			baseSize		= src.baseSize;
			terraformLevel	= src.terraformLevel;
			environment		= src.environment;
			resources		= src.resources;
			artifacts		= src.artifacts;
			bonusTechs		= src.bonusTechs;
			// waste		= src.waste;
		}
	}
}
