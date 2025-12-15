package rotp.model.empires.species;

import static rotp.model.empires.species.SpeciesSettings.RANDOM_RACE_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import rotp.model.empires.Empire.EmpireBaseData;
import rotp.model.empires.species.SkillsFactory.AnimationListMap;
import rotp.model.empires.species.SkillsFactory.CivRecordList;
import rotp.model.empires.species.SkillsFactory.CivilizationRecord;
import rotp.model.empires.species.SkillsFactory.RaceList;
import rotp.model.galaxy.Galaxy.GalaxyBaseData;
import rotp.model.galaxy.GalaxyFactory.GalaxyCopy;
import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.util.StringList;
import rotp.util.Base;
import rotp.util.LanguageManager;

public final class SpeciesFactory implements ISpecies, Base {
	private final IGameOptions opts;
	private final SkillsFactory sf =  SkillsFactory.getSkillsFactoryForGalaxy();
//	private final List<Species> alienSpecies		= new ArrayList<>();
	private final List<Integer> civilizationColors	= new ArrayList<>();
	private final RaceList customSpeciesList		= sf.new RaceList();
	private final HashMap<String, StringList> animMapOrigin	= customSpeciesList.animationMap();
	private final HashMap<String, StringList> animationMap	= initAnimationMap();
//	private final StringList allowedRaceList;
//	private final StringList alienRaceList;
	private final StringList internalSpeciesKeys;
	private final StringList baseInternalKeys;
	private final StringList allInternalKeys;
//	private final List<AlienInfo> alienSpecies	= new ArrayList<>();
	private final int playerId;
	private final Species playerSpecies;
//	private final DynOptions playerOptions;
	private final String[] selectedAliens; // = SelectedOpponents
	private final boolean randomInternalAbility, randomInternalAbility16;
	private final boolean useLocalSkills;
	private final String langDir;
	private final String selectedGlobalSkillsKey;
	private final SpecificCROption selectedGlobalSkills;
	private final AnimationListMap animationListMap, fullAnimationMap, internalListMap;
	private final CivRecordList allCustomCiv, allFullAnimationCiv, allWithPreferedAnim;
	private final CivRecordList selectedInternalCiv, allInternalCiv;
	
	// Restart var
	private final GalaxyCopy galaxyCopy;
	private final GalaxyBaseData galSrc;
	private final EmpireBaseData empSrc[]; // TODO BR: Restart
	private final boolean isRestart;
	private final int numAliens;
	StringList AllInternalKeys;
	AlienData[] aliensData;

//	private boolean[] isRandomOpponent;	// BR: only Random Races will be customized
	private String playerSkillKey;		// BR: in case Alien races are a copy of player race

	public Species getPlayerSpecies()		{ return playerSpecies; }
	public List<Species> getAlienSpecies()	{
		List<Species> list	= new ArrayList<>();
		for (AlienData alien : aliensData)
			list.add(alien.species);
		return list;
	}
	public SpeciesFactory(IGameOptions options, GalaxyCopy galaxyCopy, int playerId)	{
		this.galaxyCopy = galaxyCopy;
		this.playerId = playerId;
		opts = options;
		langDir = LanguageManager.selectedLanguageDir();
		numAliens	= opts.selectedNumberOpponents();
		aliensData	= new AlienData[numAliens];

		isRestart	= galaxyCopy != null;
		galSrc = isRestart? galaxyCopy.galSrc : null;
		empSrc = isRestart? galSrc.empires : null;
		randomInternalAbility	= opts.randomizeAIAbility();
		randomInternalAbility16	= opts.randomizeAIAbility16();
		selectedGlobalSkillsKey	= opts.selectedGlobalAbility();
		selectedGlobalSkills	= SpecificCROption.set(selectedGlobalSkillsKey);
		useLocalSkills = opts.useSelectableAbilities();

		selectedAliens		= opts.selectedOpponentRaces();
		allInternalKeys		= opts.allRaceKeyList();
		baseInternalKeys	= opts.baseRaceKeyList();
		internalSpeciesKeys	= opts.getInternalSpeciesList();
		animationListMap	= sf.new AnimationListMap().loadCustomSpecies();
		fullAnimationMap	= animationListMap.getFullAnimMap();
		allCustomCiv		= animationListMap.getAllCustomCiv();
		allFullAnimationCiv	= animationListMap.getFullAnim(allCustomCiv);
		allWithPreferedAnim	= animationListMap.getWithPrefAnim(allCustomCiv);
		internalListMap		= sf.new AnimationListMap().loadInternalSpecies();
		selectedInternalCiv	= internalListMap.getSelectedCiv(internalSpeciesKeys);
		allInternalCiv		= internalListMap.getAllCiv();

		civilizationColors.addAll(opts.possibleColors());

//		if (!isRestart || opts.selectedRestartAppliesSettings()) {
//			allowedRaceList	= getAllowedAlienSkills();
//			alienRaceList	= getAllAlienSkills();
//		}
//		else {
//			allowedRaceList	= null;
//			alienRaceList	= null;
//		}
		Species.cleanUsedNames();

		// Player
		playerSpecies = createPlayerSpecies();
//		playerOptions = playerSpecies.speciesOptions();
		if (debug())
			System.out.println("Player: " + playerSpecies.toString());

		// Aliens
		createAlienSpecies();

		// Terminate
		Species.cleanUsedNames();
	}
	private boolean debug()					{ return true; }

	private boolean createAlienSpecies()	{ // TODO BR:
		List<AlienData> postponedAliens = new ArrayList<>();
		// Create species recipients
		for (int alienId=0; alienId<numAliens; alienId++)
			aliensData[alienId] = new AlienData(alienId);

		postponedAliens.addAll(createIconSelectedAnim());
		postponedAliens.addAll(createSkillsSelectedAnim());
		postponedAliens.addAll(createRandomAnimSelectedSkills());

		postponedAliens = createNoSelectionSpecies(selectedInternalCiv);
		if (!postponedAliens.isEmpty())
			postponedAliens = createNoSelectionSpecies(allInternalCiv);
		return postponedAliens.isEmpty();
	}

	private List<AlienData> createNoSelectionSpecies(CivRecordList animSource )	{ // TODO BR:
		List<AlienData> aliens = new ArrayList<>();

		// First: create animations (can be changed later on)
		// Get all remaining data
		for (AlienData data : aliensData) {
			if (data.isValid)
				continue;
			// select an anim
			CivilizationRecord civ = animSource.nextRandom(false);
			// species, allowCustomAnim = true, allowCustomNames = true
			data.setSpecies(new Species(civ.fileKey), true, true);
			aliens.add(data);
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
			CivilizationRecord civ = selectedInternalCiv.nextRandom(false);
			// species, allowCustomAnim = true, allowCustomNames = true
			data.setSpecies(new Species(civ.fileKey), true, true);
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
		for (Iterator<AlienData> iter = aliens.iterator(); iter.hasNext(); ) { // TODO BR:
			AlienData data = iter.next();
			if (!data.hasLocalSkills || data.isValid)
				continue;
			if (assignSkills(data))
				iter.remove();
		}
		if (aliens.isEmpty())
			return;

		// Then: set globally selected Skills
		for (Iterator<AlienData> iter = aliens.iterator(); iter.hasNext(); ) { // TODO BR:
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
	private boolean assignSkills(AlienData alienData)	{ // TODO BR:
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

	private class AlienData {
		final int alienId;
		Species species;
		String animationSelectedKey;
		String skillsSelectedKey;
		SpecificCROption skills;
		final boolean hasLocalSkills;
		final boolean canHaveCustomAnim;
//		final boolean canHaveCustomNames;
		final boolean askForCustomAnim;
		boolean allowCustomAnim;
//		boolean allowCustomNames;
//		boolean isAnimFromSkills;
//		boolean isNamesFromSkills;
		boolean isValid;

		AlienData(int alienId) {
			this.alienId = alienId;
			animationSelectedKey = selectedAliens[alienId];
			skillsSelectedKey = opts.specificOpponentCROption(alienId+1);
			skills = SpecificCROption.set(skillsSelectedKey);
			hasLocalSkills = useLocalSkills && !skills.isSelection();
			if (!hasLocalSkills) {
				skillsSelectedKey = selectedGlobalSkillsKey;
				skills = selectedGlobalSkills;
			}
			canHaveCustomAnim	= skills.canHaveCustomAnim();
//			canHaveCustomNames	= skills.canHaveCustomNames();
			askForCustomAnim	= skills.askForCustomAnim();
		}

		void setSpecies(Species species, boolean allowCustomAnim, boolean allowCustomNames)	{
			this.species = species;
			this.allowCustomAnim = allowCustomAnim;
//			this.allowCustomNames = allowCustomNames;
		}
		private boolean checkAnimUpdate(CivilizationRecord civ)	{
			if (!allowCustomAnim || !civ.hasPreferedAnim())
				return false;
			if (species.animKey().equals(civ.prefAnimKey))
				return false; // No change needed
			return true;
		}
//		private boolean checkNamesUpdate(CivilizationRecord civ)	{ return allowCustomNames && civ.isFullAnim(); }
		private boolean processUserSelection()	{
			// = from selected custom file
			// Get all available skills and chose  a random one
			CivRecordList customCivs = allCustomCiv.getFileKey(skillsSelectedKey);
			CivilizationRecord civ = customCivs.nextRandom(true);
			if (civ == null) {
				System.err.println("Error: Custom Species user selection pointed to empty civilization: " + skillsSelectedKey);
				isValid = false;
				return isValid;
			}
			// Assign the skills to the species
			species.setSpeciesSkills(civ.skillsKey, civ.speciesOptions);
			// check for animation from skills
			return tryNewAnimfromSkills(civ);
		}
		private boolean processReworked()		{
			// = Find alternative skills and try to update Anim
			CivRecordList customCivs =  animationListMap.get(species.animKey());
			CivilizationRecord civ = customCivs.nextRandom(true);
			if (civ == null)
				// None... just continue with original skills
				return setNominalAnimNames();

			species.setSpeciesSkills(civ.speciesOptions);
			return tryNewAnimfromSkills(civ);
		}
		private boolean processReworkedFull()	{
			if (species == null) {
				// Create Species from custom skills, including animation selection and names
				CivilizationRecord civ = allFullAnimationCiv.nextRandom(true);
				if (civ == null) {
					civ = allWithPreferedAnim.nextRandom(true);
					if (civ == null)
						return false; // We tried everything !!!
					setSpecies(new Species(civ.prefAnimKey), true, true);
					species.setSpeciesSkills(civ.speciesOptions);
					return tryNewAnimfromSkills(civ);
				}
				setSpecies(new Species(civ.prefAnimKey), true, true);
				species.setSpeciesSkills(civ.speciesOptions);
				return tryNewAnimfromSkills(civ);
			}
			else { // Species has already been created... Strange choice!
				CivRecordList customCivs = fullAnimationMap.get(species.animKey());
				CivilizationRecord civ = customCivs.nextRandom(true);
				if (civ == null)
					// None... try a simple reworked
					return processReworked();
				species.setSpeciesSkills(civ.speciesOptions);
				return tryNewAnimfromSkills(civ);
			}
		}
		private boolean processSameAsPlayer()	{
			if (species == null) {
				// Create Species same anim as player
				setSpecies(new Species(playerSpecies.animKey()), true, true);
			}
			CivRecordList customCivs =  allCustomCiv.getKey(playerSkillKey);
			CivilizationRecord civ = customCivs.nextRandom(false);
			if (civ == null) {
				customCivs =  selectedInternalCiv.getKey(playerSkillKey);
				civ = customCivs.nextRandom(false);
				if (civ == null)
					return setNominalAnimNames();
			}
			species.setSpeciesSkills(civ.speciesOptions);
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
			CivilizationRecord civ = allCustomCiv.nextRandom(true);
			if (civ == null)
				return setNominalAnimNames();
			species.setSpeciesSkills(civ.speciesOptions);
			return tryNewAnimfromSkills(civ);
		}
		private boolean processAllFiles()		{ // from custom files, no filter
			CivilizationRecord civ = allCustomCiv.nextRandom(false);
			if (civ == null)
				return setNominalAnimNames();
			species.setSpeciesSkills(civ.speciesOptions);
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
		private boolean tryNewAnimfromSkills (CivilizationRecord skillCiv)	{
			if (checkAnimUpdate(skillCiv))
				if (skillCiv.isFullAnim()) { // Full custom and not already used
					species.setNewSpeciesAnim(skillCiv.prefAnimKey);
					species.setAllCustomNames(skillCiv, langDir);
					species.lockUsedNames();
					skillCiv.markAsUsed();
//					isAnimFromSkills = true;
//					isNamesFromSkills = true;
					isValid = true;
					if (debug())
						System.out.println("Alien: " + species.toString());
					return isValid;
				}
				else { // Try internal anim if remaining one
					CivRecordList internalCivs = internalListMap.getFullAnimCiv(skillCiv.prefAnimKey);
					CivilizationRecord intCiv = internalCivs.nextRandom(false);
					if (intCiv != null) { // Use internal anim
						species.setNewSpeciesAnim(skillCiv.prefAnimKey);
						species.setAllAnimNames(intCiv, langDir);
						species.lockUsedNames();
						intCiv.markAsUsed();
//						isAnimFromSkills = true;
//						isNamesFromSkills = false;
						isValid = true;
						if (debug())
							System.out.println("Alien: " + species.toString());
						return isValid;
					}
				}
			// Can not change anim -> set current one
			return setNominalAnimNames();
		}
		private boolean setNominalAnimNames()	{
			CivRecordList internalCivs = internalListMap.getFullAnimCiv(species.animKey());
			CivilizationRecord animCiv = internalCivs.nextRandom(false);
			if (animCiv != null) { // Use internal anim
				species.setAllAnimNames(animCiv, langDir);
				species.lockUsedNames();
				animCiv.markAsUsed();
//				isNamesFromSkills = false;
				isValid = true;
				if (debug())
					System.out.println("Alien: " + species.toString());
			}
			else
				isValid = false;
			return isValid;
		}
	}

//	private void initLists()	{
//		int numAliens = opts.selectedNumberOpponents();
//		for (int alienId=0; alienId<numAliens; alienId++) {
//			AlienInfo alien	= new AlienInfo(alienId);
//			alienSpecies.add(alien);
//		}
//	}
//	private final class AlienInfo {
//		private final int alienId;
//		private final int civId;
//		private final String animKey;
//		private final SpecificCROption skills;
//		private final boolean iconSelectedAnim;
//		private final boolean hasSkills;
//		private final boolean internalSkills;
//		private final boolean tryChangeAnim;
//
//		private boolean randomAnim;
//		private boolean tryChangeName = true;
//		private boolean created = false;
//		private String skillsKey;
//		private Species species;
//		private DynOptions options;
//
//		private AlienInfo(int id)	{
//			alienId	= id;
//			civId	= alienId + 1;
//			animKey	= selectedAliens[alienId];
//			iconSelectedAnim = animKey != null;
//			randomAnim = !iconSelectedAnim;
//
//			skills = getSkills();
//			hasSkills = !skills.isBaseRace();
//			if (hasSkills)
//				internalSkills = AllInternalKeys.contains(animKey);
//			else
//				internalSkills = false;
//
//			tryChangeAnim = hasSkills && !internalSkills && animKey == null;
//		}
//		private SpecificCROption getSkills()	{
//			String localSkillsKey	= opts.specificOpponentCROption(civId);
//			SpecificCROption skills = SpecificCROption.set(localSkillsKey);
//			if (useLocalSkills && !skills.isSelection()) {
//				skillsKey = localSkillsKey;
//				return skills;
//			}
//			else {
//				skillsKey = selectedGlobalSkillsKey;
//				return selectedGlobalSkills;
//			}
//		}
//		private void createIconSpecies()	{ // TODO
//			species = new Species(animKey);
////			species = new Species(animKey, skillsKey, options);
//			
//
//			// Civilization Name
//			String CivilzationName = playerSpecies.civilizationName();
//			if (CivilzationName.isEmpty())
//				System.err.println("Error: No available name for Player Civilization");
//
//			// Home World Name
//			String systemName = opts.selectedHomeWorldName();
//			if (systemName.isEmpty())
//				systemName = playerSpecies.nextAvailableHomeworldExt();
//			else
//				playerSpecies.usedHomeNames().add(systemName);
//			playerSpecies.initialHomeWorld = systemName;
//
//			// Leader Name
//			String leaderName = opts.selectedLeaderName();
//			if (leaderName.isEmpty())
//				leaderName = playerSpecies.nextAvailableLeaderExt();
//			else
//				playerSpecies.usedLeaderNames().add(leaderName);
//			playerSpecies.initialLeaderName = leaderName;
//
//			// Player Color
//			int color = options().selectedPlayerColor();
//			playerSpecies.colorId = color;
//			civilizationColors.remove(color);			created = true;
//		}
//		private void processSkills()	{
//			switch (skills) {
//				case USER_SELECTION:
//					species.setSpeciesSkills(fileToSkills(skillsKey));
//					break;
//				case REWORKED:
//				case REWORKED_FULL:
//					String skillKey = animationKey(skillsKey);
//					if (!skillKey.isEmpty())
//						species.setSpeciesSkills(fileToSkills(skillKey));
//					break;
//				case PLAYER:
//					species.setSpeciesSkills(optionToSkills(playerOptions));
//					break;
//				case RANDOM: // Create a random race
//					species.setSpeciesSkills(RANDOM_RACE_KEY);
//					break;
//				case RANDOM_10: // Choose randomly in the base list
//					species.setSpeciesSkills(random(baseInternalKeys));
//					break;
//				case RANDOM_16: // Choose randomly including the Modnar Races
//					species.setSpeciesSkills(random(allInternalKeys));
//					break;
//				case FILES_FLT:
//					if (allowedRaceList.isEmpty())
//						species.setSpeciesSkills(animKey);
//					else
//						species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
//					break;
//				case ALL_FILES:
//					if (alienRaceList.isEmpty())
//						species.setSpeciesSkills(animKey);
//					else
//						species.setSpeciesSkills(fileToSkills(random(alienRaceList)));
//					break;
//				case FILES_RACES:
//					if (rng().nextBoolean())
//						if (allowedRaceList.isEmpty())
//							species.setSpeciesSkills(random(allInternalKeys));
//						else
//							species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
//					else
//						species.setSpeciesSkills(random(allInternalKeys));
//					break;
//				case ALL:
//					if (rng().nextBoolean())
//						if (rng().nextBoolean())
//							if (allowedRaceList.isEmpty())
//								species.setSpeciesSkills(optionToSkills(playerOptions));
//							else
//								species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
//						else
//							species.setSpeciesSkills(optionToSkills(playerOptions));
//					else if (rng().nextBoolean())
//						species.setSpeciesSkills(random(allInternalKeys));
//					else
//						species.setSpeciesSkills(RANDOM_RACE_KEY);
//					break;
//				case ORIGINAL_SPECIES: // default as vanilla
//				default:
//					if (randomInternalAbility) // original Advanced Option random abilities
//						if (randomInternalAbility16)
//							species.setSpeciesSkills(random(allInternalKeys));
//						else
//							species.setSpeciesSkills(random(baseInternalKeys));
//					else
//						species.setSpeciesSkills(animKey);
//					break;
//			}
//		}
//	}
//
//	private StringList buildAlienRaces() {
//		StringList alienList = new StringList();
//		StringList allspeciesOptions = new StringList();
//		StringList internalSpecies = options().getInternalSpeciesList();
//		int maxRaces = options().selectedNumberOpponents();
//		int mult = IGameOptions.MAX_OPPONENT_TYPE;
//
//		// first, build randomized list of opponent races
//		for (int i=0; i<mult; i++) {
//			shuffle(internalSpecies);
//			allspeciesOptions.addAll(internalSpecies);
//		}
//		if(opts.fullyScrambledSpecies())
//			shuffle(allspeciesOptions);
//
//		
//		// next, remove from that list the player and any selected opponents
//		String[] selectedOpponents = options().selectedOpponentRaces();
//		isRandomOpponent = new boolean[selectedOpponents.length]; // BR: only Random Races will be customized
//		allspeciesOptions.remove(options().selectedPlayerRace());
//
//		for (int i=0;i<maxRaces;i++) {
//			if (selectedOpponents[i] != null) {
//				allspeciesOptions.remove(selectedOpponents[i]);
//				isRandomOpponent[i] = false;
//			} else {
//				isRandomOpponent[i] = true;
//			}
//		}
//		// build alien race list, replacing unselected opponents (null)
//		// with remaining options
//		for (int i=0;i<maxRaces;i++) {
//			if (selectedOpponents[i] == null)
//				alienList.add(allspeciesOptions.remove(0));
//			else
//				alienList.add(selectedOpponents[i]);
//		}
//		return alienList;
//	}

	private Species createPlayerSpecies()	{ // TODO Lock names
		GalaxyBaseData galSrc = null; // Used for Restart
		EmpireBaseData empSrc = null; // Used for Restart
		if (galaxyCopy != null) {
			galSrc = galaxyCopy.galSrc;
			empSrc = galSrc.empires[playerId];
		}

		// Player Animation
		String playerAnimKey = opts.selectedPlayerRace();
		Species playerSpecies = new Species(playerAnimKey);
		// Player Skills
		playerSkillKey = playerAnimKey;
		DynOptions skillOptions = null;
		String restartChangesPlayerRace = opts.selectedRestartChangesPlayerRace();
		if (opts.selectedPlayerIsCustom())
			playerSkillKey = SkillsFactory.CUSTOM_RACE_KEY;
		if (galaxyCopy != null && !opts.selectedRestartAppliesSettings()
				&& !restartChangesPlayerRace.equals("GuiLast")
				&& !restartChangesPlayerRace.equals("GuiSwap")) { // Use Restart info
			playerSkillKey = empSrc.dataRaceKey;
			skillOptions = empSrc.raceOptions;
		}
		playerSpecies.setSpeciesSkills(playerSkillKey, skillOptions);

		// Civilization Name
		String CivilzationName = playerSpecies.civilizationName();
		if (CivilzationName.isEmpty())
			System.err.println("Error: No available name for Player Civilization");

		// Home World Name
		String systemName = opts.selectedHomeWorldName();
		if (systemName.isEmpty())
			systemName = playerSpecies.nextAvailableHomeworldExt();
		else
			playerSpecies.usedHomeNames().add(systemName);
		playerSpecies.initialHomeWorld = systemName;

		// Leader Name
		String leaderName = opts.selectedLeaderName();
		if (leaderName.isEmpty())
			leaderName = playerSpecies.nextAvailableLeaderExt();
		else
			playerSpecies.usedLeaderNames().add(leaderName);
		playerSpecies.initialLeaderName = leaderName;

		// Player Color
		int color = options().selectedPlayerColor();
		playerSpecies.colorId = color;
		civilizationColors.remove(color);
		return playerSpecies;
	}
	private HashMap<String, StringList> initAnimationMap()	{
		HashMap<String, StringList> animationMap  = new HashMap<>();
		for (Entry<String, StringList> entry : animMapOrigin.entrySet())
			animationMap.put(entry.getKey(), new StringList(entry.getValue()));
		return animationMap;
	}
//	private String animationKey(String animKey)	{
//		// Search for an unused rework
//		StringList list = animationMap.get(animKey);
//		if (list.isEmpty())
//			// No more: then refill
//			list.addAll(animMapOrigin.get(animKey));
//		shuffle(list);
//		shuffle(list);
//		// Get one (could be empty!)
//		String skillKey = list.removeFirst();
//		//System.out.println("Rework Anim key = " + animKey + " -> Skill key = " + skillKey);
//		return skillKey;
//	}
//	private LinkedList<String> buildAlienRaces() {
//		LinkedList<String> alienKeyList = new LinkedList<>();
//		List<String> allRaceKeyList = new ArrayList<>();
//		List<String> internalKeyList = opts.getInternalSpeciesList(); // BR:
//		int maxSpecies = opts.selectedNumberOpponents();
//		int mult = IGameOptions.MAX_OPPONENT_TYPE;
//
//		// first, build randomized list of opponent species
//		for (int i=0;i<mult;i++) {
//			shuffle(internalKeyList);
//			allRaceKeyList.addAll(internalKeyList);
//		}
//
//		// next, remove from that list the player and any selected opponents
//		String[] selectedOpponents = opts.selectedOpponentRaces();
//		isRandomOpponent = new boolean[selectedOpponents.length]; // BR: only Random Races will be customized
//		allRaceKeyList.remove(opts.selectedPlayerRace());
//
//		for (int i=0;i<maxSpecies;i++) {
//			if (selectedOpponents[i] != null) {
//				allRaceKeyList.remove(selectedOpponents[i]);
//				isRandomOpponent[i] = false;
//			} else {
//				isRandomOpponent[i] = true;
//			}
//		}
//		// build alien race list, replacing unselected opponents (null)
//		// with remaining options
//		for (int i=0;i<maxSpecies;i++) {
//			if (selectedOpponents[i] == null)
//				alienKeyList.add(allRaceKeyList.remove(0));
//			else
//				alienKeyList.add(selectedOpponents[i]);
//		}
//		return alienKeyList;
//	}
}
