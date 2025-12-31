package rotp.model.empires.species;

import static rotp.model.empires.species.SpeciesSettings.RANDOM_RACE_KEY;
import static rotp.model.game.IRaceOptions.defaultRaceKey;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rotp.model.empires.Empire.EmpireBaseData;
import rotp.model.empires.species.SkillsFactory.AnimationListMap;
import rotp.model.empires.species.SkillsFactory.CivRecordList;
import rotp.model.empires.species.SkillsFactory.CivilizationRecord;
import rotp.model.galaxy.Galaxy.GalaxyBaseData;
import rotp.model.galaxy.GalaxyFactory.GalaxyCopy;
import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.util.StringList;
import rotp.util.Base;
import rotp.util.LanguageManager;

public final class SpeciesFactory implements ISpecies, Base {
	private final IGameOptions options;
	private SkillsFactory sf =  SkillsFactory.getSkillsFactoryForGalaxy();
	private final StringList internalSpeciesKeys;
	private final StringList baseInternalKeys;
	private final StringList allInternalKeys;
	private final int playerId;
	private Species playerSpecies;
	private final String[] selectedAliens; // = SelectedOpponents
	private final boolean randomInternalAbility, randomInternalAbility16;
	private final boolean useLocalSkills;
	private final String langDir;
	private final String selectedGlobalSkillsKey;
	private final SpecificCROption selectedGlobalSkills;
	private AnimationListMap animationListMap, fullAnimationMap, allInternalListMap, selectedInternalMap;
	private CivRecordList allCustomCiv, allFullAnimationCiv, allWithPreferedAnim;
	private CivRecordList selectedInternalCiv, allInternalCiv;
	private AlienData[] aliensData;

	// Restart var
	private final GalaxyBaseData galaxySrc;
	private final EmpireBaseData empiresSrc[];
	private final boolean isRestart, fullRestart;
	private final int numAliens;

	private List<Integer> civilizationColors	= new ArrayList<>();
	private String playerSkillKey;	// BR: in case Alien races are a copy of player race
	private void clearAll()	{
		animationListMap	= null;
		fullAnimationMap	= null;
		allInternalListMap	= null;
		selectedInternalMap	= null;
		allCustomCiv		= null;
		allFullAnimationCiv	= null;
		allWithPreferedAnim	= null;
		selectedInternalCiv	= null;
		allInternalCiv	= null;
		aliensData		= null;
		playerSpecies	= null;
		sf = null;
	}
	public Species getPlayerSpecies()		{ return playerSpecies; }
	public List<Species> getAlienSpecies()	{
		List<Species> list	= new ArrayList<>();
		for (AlienData alien : aliensData)
			list.add(alien.species);
		clearAll();
		return list;
	}
	public SpeciesFactory(IGameOptions options, GalaxyCopy galaxyCopy, int playerId)	{
		Species.cleanUsedNames();
		this.playerId	= playerId;
		this.options	= options;

		isRestart	= galaxyCopy != null;
		fullRestart	= isRestart && !options.selectedRestartAppliesSettings();
		galaxySrc	= isRestart? galaxyCopy.galSrc : null;
		empiresSrc	= isRestart? galaxySrc.empires : null;
		numAliens	= isRestart? empiresSrc.length-1 : options.selectedNumberOpponents();
		langDir		= LanguageManager.selectedLanguageDir();
		aliensData	= new AlienData[numAliens];

		randomInternalAbility	= options.randomizeAIAbility();
		randomInternalAbility16	= options.randomizeAIAbility16();
		selectedGlobalSkillsKey	= options.selectedGlobalAbility();
		selectedGlobalSkills	= SpecificCROption.set(selectedGlobalSkillsKey);
		useLocalSkills		= options.useSelectableAbilities();
		selectedAliens		= options.selectedOpponentRaces();
		allInternalKeys		= options.allRaceKeyList();
		baseInternalKeys	= options.baseRaceKeyList();
		internalSpeciesKeys	= options.getInternalSpeciesList();

		animationListMap	= sf.new AnimationListMap().loadCustomSpecies();
		allCustomCiv		= animationListMap.getAllCiv();
		if (allCustomCiv.size()<50) {
			allCustomCiv	= animationListMap.getAllCiv();
		}

		fullAnimationMap	= animationListMap.getFullAnimMap();
		allFullAnimationCiv	= fullAnimationMap.getAllCiv();
		if (allFullAnimationCiv.size()<50) {
			fullAnimationMap	= animationListMap.getFullAnimMap();
			allFullAnimationCiv	= fullAnimationMap.getAllCiv();
		}

		allWithPreferedAnim	= animationListMap.getWithPrefAnim(allCustomCiv);
		if (allWithPreferedAnim.size()<50) {
			allWithPreferedAnim	= animationListMap.getWithPrefAnim(allCustomCiv);;
		}

		allInternalListMap	= sf.new AnimationListMap().loadInternalSpecies();
		allInternalCiv		= allInternalListMap.getAllCiv();
		selectedInternalMap	= allInternalListMap.getSelectedMap(internalSpeciesKeys);
		selectedInternalCiv	= selectedInternalMap.getAllCiv();

		options.randomizeColors();
		civilizationColors	= options.possibleColors();

		if (debug())
			System.out.println("==========");
		// Player
		playerSpecies = createPlayerSpecies();
		// Aliens
		createAlienSpecies();
		// Terminate
		Species.cleanUsedNames();
	}
	private boolean debug()	{ return false; }

	private List<AlienData> createNoSelectionSpecies(AnimationListMap animSource )	{
		List<AlienData> aliens = new ArrayList<>();

		// First: create animations (can be changed later on)
		// Get all remaining data
		for (AlienData data : aliensData) {
			if (data.isValid)
				continue;
			// select an anim
			CivRecordList civList = animSource.nextRandom(false, true);
			CivilizationRecord civ = civList.nextRandom(false, true);
			// species, allowCustomAnim = true, allowCustomNames = true
			data.setSpecies(new Species(civ.fileKey), true, true);
			aliens.add(data);
			assignSkillsSequentially(aliens);
		}
		if (aliens.isEmpty())
			return aliens;

		// Then: There is a problem!
		// Return the remaining list... Or maybe try to fixes
		System.err.println("not all No selection species were created; Missing: " + aliens.size());
		return aliens;
	}
	private List<AlienData> createRandomAnimSelectedSkills()	{
		List<AlienData> aliens = new ArrayList<>();

		// First: create animations (can be changed later on)
		for (AlienData data : aliensData) {
			if (!data.hasLocalSkills || data.isValid)
				continue;
			// select an anim
			CivRecordList civList = selectedInternalMap.nextRandom(false, true);
			CivilizationRecord civ = civList.nextRandom(false, true);
			// species, allowCustomAnim = true, allowCustomNames = true
			data.setSpecies(new Species(civ.fileKey), true, true);
			aliens.add(data);
		}
		if (aliens.isEmpty())
			return aliens;

		// Then finalize species by assigning their skills
		assignSkillsSequentially(aliens);
		if (aliens.isEmpty())
			return aliens;

		// Then: There is a problem!
		// Return the remaining list... Or maybe try to fixes
		System.err.println("not all Random selected anim were created; Missing: " + aliens.size());
		return aliens;
	}
	private List<AlienData> createSkillsSelectedAnim()	{
		List<AlienData> aliens = new ArrayList<>();

		for (AlienData data : aliensData)
			if (!data.askForCustomAnim || data.isValid)
				continue;
			else if (!data.processReworkedFull())
				aliens.add(data);

		if (aliens.isEmpty())
			return aliens;
		// Then: There is a problem!
		// Return the remaining list... Or maybe try to fixes
		System.err.println("not all Skills selected animations were created; Missing: " + aliens.size());
		return aliens;
	}
	private List<AlienData> createIconSelectedAnim()	{
		List<AlienData> aliens = new ArrayList<>();

		// First: create animations (not to be changed later on)
		for (AlienData data : aliensData) {
			if (data.animationSelectedKey == null || data.isValid)
				continue;
			// species, allowCustomAnim = false, allowCustomNames = true
			data.setSpecies(new Species(data.animationSelectedKey), false, true);
//			data.isAnimFromSkills = false;
			aliens.add(data);
		}
		if (aliens.isEmpty())
			return aliens;

		// The finalize species by assigning their skills
		assignSkillsSequentially(aliens);
		if (aliens.isEmpty())
			return aliens;

		// Then: There is a problem!
		// Return the remaining list... Or maybe try to fixes
		System.err.println("not all Icon selected animations were created; Missing: " + aliens.size());
		return aliens;
	}
	private void assignSkillsSequentially(List<AlienData> aliens)	{
		// First create species that can't change their Animations
		for (Iterator<AlienData> iter = aliens.iterator(); iter.hasNext(); ) {
			AlienData data = iter.next();
			if (data.canHaveCustomAnim)
				continue;
			if (assignFixedAnimSkills(data))
				iter.remove();
		}
		if (aliens.isEmpty())
			return;

		// Then: set individually selected Skills
		for (Iterator<AlienData> iter = aliens.iterator(); iter.hasNext(); ) {
			AlienData data = iter.next();
			if (!data.hasLocalSkills || data.isValid)
				continue;
			if (assignSkills(data))
				iter.remove();
		}
		if (aliens.isEmpty())
			return;

		// Then: set globally selected Skills
		for (Iterator<AlienData> iter = aliens.iterator(); iter.hasNext(); ) {
			AlienData data = iter.next();
			if (data.hasLocalSkills || data.isValid)
				continue;
			if (assignSkills(data))
				iter.remove();
		}
	}
	private boolean assignFixedAnimSkills(AlienData alienData)	{
		switch (alienData.skills) {
			case RANDOM:			return alienData.processRandom();
			case RANDOM_10:			return alienData.processRandom10();
			case RANDOM_16:			return alienData.processRandom16();
			case ORIGINAL_SPECIES:	return alienData.processOriginalSpecies();
			default:				return false;
		}
	}
	private boolean assignSkills(AlienData alienData)	{
		switch (alienData.skills) {
			case USER_SELECTION:	return alienData.processUserSelection();
			case REWORKED:			return alienData.processReworked();
			case REWORKED_FULL:		return alienData.processReworkedFull();
			case PLAYER:			return alienData.processSameAsPlayer();
			case RANDOM:			return alienData.processRandom();
			case RANDOM_10:			return alienData.processRandom10();
			case RANDOM_16:			return alienData.processRandom16();
			case FILES_FLT:			return alienData.processFileFlt();
			case ALL_FILES:			return alienData.processAllFiles();
			case FILES_RACES:		return alienData.processFileRaces();
			case ALL:				return alienData.processAll();
			case ORIGINAL_SPECIES:	return alienData.processOriginalSpecies();
			default:				return alienData.processOriginalSpecies();
		}
	}
	private void finalizeSpecies(Species species, CivilizationRecord civ) {
		if (species.lockUsedNames())
			System.out.println("Lock Used Names failed");
		if (civ != null)
			civ.markAsUsed();
		if (civilizationColors.isEmpty())
			civilizationColors = options.possibleColors();
		species.colorId = civilizationColors.remove(0);
		if (debug())
			System.out.println(species.toString());
	}
	private boolean createAlienSpecies()	{
		List<AlienData> postponedAliens = new ArrayList<>();
		if (fullRestart) {
			for (int alienId=0; alienId<numAliens; alienId++) {
				aliensData[alienId] = new AlienData(alienId, empiresSrc[alienId+1]);
				finalizeSpecies(aliensData[alienId].species, null);
			}
			return true;
		}

		// Create species recipients
		for (int alienId=0; alienId<numAliens; alienId++)
			aliensData[alienId] = new AlienData(alienId);

		postponedAliens.addAll(createIconSelectedAnim());
		postponedAliens.addAll(createSkillsSelectedAnim());
		postponedAliens.addAll(createRandomAnimSelectedSkills());

		postponedAliens = createNoSelectionSpecies(selectedInternalMap);
		if (!postponedAliens.isEmpty())
			postponedAliens = createNoSelectionSpecies(allInternalListMap);
		return postponedAliens.isEmpty();
	}
	private Species createPlayerSpecies()	{
		Species playerSpecies;
		// Player Animation
		String playerAnimKey = options.selectedPlayerRace();
		// Player Skills
		playerSkillKey = playerAnimKey;
		DynOptions skillOptions = null;
		String restartChangesPlayerRace = options.selectedRestartChangesPlayerRace();
		if (options.selectedPlayerIsCustom())
			playerSkillKey = SkillsFactory.CUSTOM_RACE_KEY;
		if (fullRestart
				&& !restartChangesPlayerRace.equals("GuiLast")
				&& !restartChangesPlayerRace.equals("GuiSwap")) { // Use Restart info
			EmpireBaseData empSrc = empiresSrc[playerId];
			playerSkillKey = empSrc.dataRaceKey;
			skillOptions = empSrc.raceOptions;
			playerSpecies = new Species(empSrc);
			finalizeSpecies(playerSpecies, null);
			return playerSpecies;
		}
		else {
			playerSpecies = new Species(playerAnimKey);
			playerSpecies.setSpeciesSkills(playerSkillKey, skillOptions);
		}
		CivRecordList internalCivs = allInternalCiv.getKey(playerAnimKey);
		CivilizationRecord animCiv = internalCivs.get(0);
		if (animCiv != null) { // Use internal anim
			playerSpecies.setAllAnimNames(animCiv, langDir);
			finalizeSpecies(playerSpecies, animCiv);
		}
		else
			System.err.println("Error: No available civilization for Player Civilization");

		// Player Color
		int playerColor = options().selectedPlayerColor();
		playerSpecies.colorId = playerColor;
		civilizationColors.clear();
		boolean playerCExcluded = false;
		for (int i : options.possibleColors())
			if ((i == playerColor) && !playerCExcluded)
				playerCExcluded = true;
			else
				civilizationColors.add(i);
		return playerSpecies;
	}
	private final class AlienData	{
		Species species;
		String animationSelectedKey;
		String skillsSelectedKey;
		SpecificCROption skills;
		boolean hasLocalSkills;
		boolean canHaveCustomAnim;
		boolean askForCustomAnim;
		boolean allowCustomAnim;
		boolean isValid;

		AlienData(int alienId, EmpireBaseData eSrc) {
			animationSelectedKey = eSrc.raceKey;
			skillsSelectedKey = eSrc.dataRaceKey;
			species = new Species(eSrc);
		}

		AlienData(int alienId) {
			animationSelectedKey = selectedAliens[alienId];
			skillsSelectedKey = options.specificOpponentCROption(alienId+1);
			skills = SpecificCROption.set(skillsSelectedKey);
			hasLocalSkills = useLocalSkills && !skills.isSelection();
			if (!hasLocalSkills) {
				skillsSelectedKey = selectedGlobalSkillsKey;
				skills = selectedGlobalSkills;
			}
			canHaveCustomAnim	= skills.canHaveCustomAnim();
			askForCustomAnim	= skills.askForCustomAnim();
		}

		void setSpecies(Species species, boolean allowCustomAnim, boolean allowCustomNames)	{
			this.species = species;
			this.allowCustomAnim = allowCustomAnim;
		}
		private boolean processUserSelection()	{
			// = from selected custom file
			// Get all available skills and chose  a random one
			CivRecordList customCivs = allCustomCiv.getFileKey(skillsSelectedKey);
			CivilizationRecord civ = customCivs.nextRandom(true, false);
			if (civ == null) {
				if (skillsSelectedKey.startsWith(SpeciesSettings.BASE_RACE_MARKER)) {
					String key = Species.languageToKey(skillsSelectedKey.substring(1));
					if (key == null)
						key = defaultRaceKey;
					customCivs = allInternalCiv.getFileKey(key);
					civ = customCivs.nextRandom(true, false);
					if (civ == null) {
						System.err.println("Error: Custom Species user selection pointed to empty civilization: " + skillsSelectedKey.substring(1) + " -> " + key);
						isValid = false;
						return isValid;
					}
				}
				else {
					System.err.println("Error: Custom Species user selection pointed to empty civilization: " + skillsSelectedKey);
					isValid = false;
					return isValid;
				}
			}
			// Assign the skills to the species
			species.setSpeciesSkills(civ.skillsKey, civ.speciesOptions);
			species.fileKey = civ.fileKey;
			// check for animation from skills
			return tryNewAnimfromSkills(civ);
		}
		private boolean processReworked()		{
			// = Find alternative skills and try to update Anim
			CivRecordList customCivs =  animationListMap.get(species.animKey());
			CivilizationRecord civ = customCivs.nextRandom(true, false);
			if (civ == null)
				// None... just continue with original skills
				return setNominalAnimNames();

			species.setSpeciesSkills(civ.speciesOptions);
			species.fileKey = civ.fileKey;
			return tryNewAnimfromSkills(civ);
		}
		private boolean processReworkedFull()	{
			if (species == null) {
				// Create Species from custom skills, including animation selection and names
				CivilizationRecord civ = allFullAnimationCiv.nextRandom(true, true);
				if (civ == null || civ.useCount>0 || !civ.isFullAnim()) {
					civ = allFullAnimationCiv.nextRandom(true, true);
					civ = allWithPreferedAnim.nextRandom(true, false);
					if (civ == null || civ.useCount>0)
						return false; // We tried everything !!!
					setSpecies(new Species(civ.prefAnimKey), true, true);
					species.setSpeciesSkills(civ.speciesOptions);
					species.fileKey = civ.fileKey;
					return tryNewAnimfromSkills(civ);
				}
				setSpecies(new Species(civ.prefAnimKey), true, true);
				species.setSpeciesSkills(civ.speciesOptions);
				species.fileKey = civ.fileKey;
				return tryNewAnimfromSkills(civ);
			}
			else { // Species has already been created... Strange choice!
				CivRecordList customCivs = fullAnimationMap.get(species.animKey());
				CivilizationRecord civ = customCivs.nextRandom(true, false);
				if (civ == null) {
					// None... So, takes any from full animations
					civ = allFullAnimationCiv.nextRandom(true, false);
					if (civ == null) {
						// None... try a simple reworked
						return processReworked();
					}
				}
				species.setSpeciesSkills(civ.speciesOptions);
				species.fileKey = civ.fileKey;
				return tryNewAnimfromSkills(civ);
			}
		}
		private boolean processSameAsPlayer()	{
			if (species == null) {
				// Create Species same anim as player
				setSpecies(new Species(playerSpecies.animKey()), true, true);
			}
			CivRecordList customCivs =  allCustomCiv.getKey(playerSkillKey);
			CivilizationRecord civ = customCivs.nextRandom(false, false);
			if (civ == null) {
				customCivs =  selectedInternalCiv.getKey(playerSkillKey);
				civ = customCivs.nextRandom(false, false);
				if (civ == null)
					return setNominalAnimNames();
			}
			species.setSpeciesSkills(civ.skillsKey, civ.speciesOptions);
			species.fileKey = civ.fileKey;
			return tryNewAnimfromSkills(civ);
		}
		private boolean processRandom()			{ // Create a random race
			species.setSpeciesSkills(RANDOM_RACE_KEY);
			return setNominalAnimNames();
		}
		private boolean processRandom10()		{ // Choose randomly in the base list
			species.setSpeciesSkills(random(baseInternalKeys));
			return setNominalAnimNames();
		}
		private boolean processRandom16()		{ // Choose randomly including the Modnar Races
			species.setSpeciesSkills(random(allInternalKeys));
			return setNominalAnimNames();
		}
		private boolean processFileFlt()		{ // from custom files, available to AI
			CivilizationRecord civ = allCustomCiv.nextRandom(true, false);
			if (civ == null)
				return setNominalAnimNames();
			species.setSpeciesSkills(civ.speciesOptions);
			species.fileKey = civ.fileKey;
			return tryNewAnimfromSkills(civ);
		}
		private boolean processAllFiles()		{ // from custom files, no filter
			CivilizationRecord civ = allCustomCiv.nextRandom(false, false);
			if (civ == null)
				return setNominalAnimNames();
			species.setSpeciesSkills(civ.speciesOptions);
			species.fileKey = civ.fileKey;
			return tryNewAnimfromSkills(civ);
		}
		private boolean processFileRaces()		{ // from custom files, available to AI or any internal species
			if (rng().nextBoolean() && processFileFlt())
				return true;
			return processRandom16();
		}
		private boolean processAll()			{
			if (rng().nextBoolean())
				if (rng().nextBoolean())
					if (processFileFlt())
						return true;
					else
						return processRandom16();
				else
					return processSameAsPlayer();
			else if (rng().nextBoolean())
				return processRandom16();
			else
				return processRandom();
		}
		private boolean processOriginalSpecies(){ // default as vanilla
			if (randomInternalAbility) // original Advanced Option random abilities
				if (randomInternalAbility16)
					species.setSpeciesSkills(random(allInternalKeys));
				else
					species.setSpeciesSkills(random(baseInternalKeys));
			// else keep nominal skills
			return setNominalAnimNames();
		}
		private boolean implementsNewAnimfromSkills(CivilizationRecord skillCiv)	{
			// First test custom skills names
			if (skillCiv.isCustom && skillCiv.isFullAnim()) { // Full custom and not already used
				// Test is already the right anim
				if (!species.animKey().equals(skillCiv.prefAnimKey))
					species.setNewSpeciesAnim(skillCiv.prefAnimKey);
				species.setAllCustomNames(skillCiv, langDir);
				finalizeSpecies(species, skillCiv);
				isValid = true;
				return isValid;
			}
			// Then try with internal Anim
			CivRecordList internalCivs = allInternalCiv.getKey(skillCiv.prefAnimKey);
			CivilizationRecord intCiv = internalCivs.nextRandom(false, true);
			if (intCiv != null) { // Use internal anim
				// Test is already the right anim
				if (!species.animKey().equals(skillCiv.prefAnimKey))
					species.setNewSpeciesAnim(skillCiv.prefAnimKey);
				species.setAllAnimNames(intCiv, langDir);
				finalizeSpecies(species, intCiv);
				isValid = true;
				return isValid;
			}
			return false;
		}
		private boolean tryNewAnimfromSkills (CivilizationRecord skillCiv)	{
			// Check if skills request specific animation
			if (allowCustomAnim && skillCiv.hasPreferedAnim()) {
				// Try change it
				if(implementsNewAnimfromSkills(skillCiv))
					return true;
			}

			// Failed or not requested: Try with current Anim
			return setCurrentsAnim(skillCiv);
		}
		private boolean setCurrentsAnim(CivilizationRecord skillCiv)	{
			// First Try from custom skills names
			if (skillCiv.isCustom && skillCiv.isFullAnim()) { // Full custom and not already used
				species.setAllCustomNames(skillCiv, langDir);
				finalizeSpecies(species, skillCiv);
				isValid = true;
				return isValid;
			}
			// Then try with internal Anim
			return setNominalAnimNames();
		}
		private boolean setNominalAnimNames()	{
			CivRecordList internalCivs = allInternalCiv.getKey(species.animKey());
			CivilizationRecord animCiv = internalCivs.nextRandom(false, true);
			if (animCiv != null) { // Use internal anim
				species.setAllAnimNames(animCiv, langDir);
				finalizeSpecies(species, animCiv);
				isValid = true;
			}
			else
				isValid = false;
			return isValid;
		}
	}
}
