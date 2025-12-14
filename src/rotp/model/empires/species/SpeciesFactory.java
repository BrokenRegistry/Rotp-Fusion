package rotp.model.empires.species;

import static rotp.model.empires.species.SkillsFactory.fileToSkills;
import static rotp.model.empires.species.SkillsFactory.getAllAlienSkills;
import static rotp.model.empires.species.SkillsFactory.getAllowedAlienSkills;
import static rotp.model.empires.species.SkillsFactory.optionToSkills;
import static rotp.model.empires.species.SpeciesSettings.RANDOM_RACE_KEY;

import java.util.ArrayList;
import java.util.HashMap;
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
	private final StringList allowedRaceList;
	private final StringList alienRaceList;
	private final StringList internalSpeciesKeys;
	private final StringList baseInternalKeys;
	private final StringList allInternalKeys;
	private final List<AlienInfo> alienSpecies	= new ArrayList<>();
	private final int playerId;
	private final Species playerSpecies;
	private final DynOptions playerOptions;
	private final String[] selectedAliens; // = SelectedOpponents
	private final boolean randomInternalAbility, randomInternalAbility16;
	private final boolean useLocalSkills;
	private final String langDir;
	private final String selectedGlobalSkillsKey;
	private final SpecificCROption selectedGlobalSkills;
	private final AnimationListMap animationListMap, fullAnimationMap, internalListMap;
	private final CivRecordList allCustomCiv, allFullAnimationCiv, internalCiv;

	// Restart var
	private final GalaxyCopy galaxyCopy;
	private final GalaxyBaseData galSrc;
	private final EmpireBaseData empSrc[];
	private final boolean isRestart;
	private final int numAliens;
	StringList AllInternalKeys;
//	Species[] aliens;
	AlienData[] aliensData;

	private boolean[] isRandomOpponent;	// BR: only Random Races will be customized
	private String playerSkillKey;		// BR: in case Alien races are a copy of player race

	public Species getPlayerSpecies()		{ return playerSpecies; }
	public List<Species> getAlienSpecies()	{
		List<Species> list	= new ArrayList<>();
		for (AlienInfo alien : alienSpecies)
			list.add(alien.species);
		return list;
	}
	public SpeciesFactory(IGameOptions options, GalaxyCopy galaxyCopy, int playerId)	{
		this.galaxyCopy = galaxyCopy;
		this.playerId = playerId;
		opts = options;
		langDir = LanguageManager.selectedLanguageDir();
		numAliens	= opts.selectedNumberOpponents();
//		aliens		= new Species[numAliens];
		aliensData	= new AlienData[numAliens];

		isRestart	= galaxyCopy != null;
		galSrc = isRestart? galaxyCopy.galSrc : null;
		empSrc = isRestart? galSrc.empires : null;
		randomInternalAbility	= opts.randomizeAIAbility();
		randomInternalAbility16	= opts.randomizeAIAbility16();
		selectedGlobalSkillsKey	= opts.selectedGlobalAbility();
		selectedGlobalSkills	= SpecificCROption.set(selectedGlobalSkillsKey);
		useLocalSkills = opts.useSelectableAbilities();

		animationListMap	= sf.new AnimationListMap().loadCustomSpecies();
		fullAnimationMap	= animationListMap.getFullAnimMap();
		allCustomCiv		= animationListMap.getAllCustomCiv();
		allFullAnimationCiv	= animationListMap.getFullAnim(allCustomCiv);
		internalListMap		= sf.new AnimationListMap().loadInternalSpecies();
		internalCiv			= internalListMap.getAllCiv();
		selectedAliens		= opts.selectedOpponentRaces();
		allInternalKeys		= opts.allRaceKeyList();
		baseInternalKeys	= opts.baseRaceKeyList();
		internalSpeciesKeys	= opts.getInternalSpeciesList();

		civilizationColors.addAll(opts.possibleColors());

		if (!isRestart || opts.selectedRestartAppliesSettings()) {
			allowedRaceList	= getAllowedAlienSkills();
			alienRaceList	= getAllAlienSkills();
		}
		else {
			allowedRaceList	= null;
			alienRaceList	= null;
		}
		Species.cleanUsedNames();

		// Player
		playerSpecies = createPlayerSpecies();
		playerOptions = playerSpecies.speciesOptions();
		if (debug())
			System.out.println("Player: " + playerSpecies.toString());

		// Aliens
		createAlienSpecies();

		// Terminate
		Species.cleanUsedNames();
	}
	private boolean debug()					{ return true; }

	private boolean createAlienSpecies()	{ // TODO
		// Create species recipients
		for (int alienId=0; alienId<numAliens; alienId++)
			aliensData[alienId] = new AlienData(alienId);

		createIconSelectedAnim();

		// createSkillsSelectedAnim();

		// createRandomAnimSelectedSkills()

		// createNoSelectionSpecies()


		return false;
	}
	private void createIconSelectedAnim()	{
		boolean allValid = true;

		// First create species that can't change their Animations
		for (int alienId=0; alienId<numAliens; alienId++) {
			String animKey = selectedAliens[alienId];
			if (animKey == null)
				continue;

			AlienData data = aliensData[alienId];
			if (data.canHaveCustomAnim)
				continue;

			// species, allowCustomAnim = false, allowCustomNames = false
			data.setSpecies(new Species(animKey), false, false);
			allValid &= processFixedAnimSkills(data);
		}

		// First: create animations (not to be changed later on)
		for (int alienId=0; alienId<numAliens; alienId++) {
			String animKey = selectedAliens[alienId];
			if (animKey == null)
				continue;
			// species, allowCustomAnim = false, allowCustomNames = true
			aliensData[alienId].setSpecies(new Species(animKey), false, true);
		}

		// Then: Locally set Skills
		for (int alienId=0; alienId<numAliens; alienId++) { // TODO
			AlienData data = aliensData[alienId];
			if (data.hasLocalSkills && !data.isValid) {
				allValid &= processLocalSkills(aliensData[alienId]);
			}
		}

		// Then: Globally set Skills
		for (int alienId=0; alienId<numAliens; alienId++) {
			AlienData data = aliensData[alienId];
			if (!data.hasLocalSkills && !data.isValid) {
				allValid &= processGlobalSkills(aliensData[alienId]); // TODO
			}

		// Then: Check invalid

		}

	}
	private boolean processFixedAnimSkills(AlienData alienData)	{ // TODO
		switch (alienData.skills) {
			case RANDOM:			return alienData.processRandom();
			case RANDOM_10:			return alienData.processRandom10();
			case RANDOM_16:			return alienData.processRandom16();
			case ORIGINAL_SPECIES:	return alienData.processOriginalSpecies();
			default:				return false;
		}
	}
	private boolean processLocalSkills(AlienData alienData)	{ // TODO
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
	private boolean processGlobalSkills(AlienData alienData)	{ // TODO
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
	private boolean processSkills(AlienData alienData)	{
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
		String skillsKey;
		SpecificCROption skills;
		final boolean hasLocalSkills;
		final boolean canHaveCustomAnim;
		final boolean canHaveCustomNames;
		boolean allowCustomAnim;
		boolean allowCustomNames;
		boolean isAnimFromSkills;
		boolean isNamesFromSkills;
		boolean isValid;

		AlienData(int alienId) {
			this.alienId = alienId;
//			species = aliens[alienId];
			skillsKey = opts.specificOpponentCROption(alienId+1);
			skills = SpecificCROption.set(skillsKey);
			hasLocalSkills = useLocalSkills && !skills.isSelection();
			if (!hasLocalSkills) {
				skillsKey = selectedGlobalSkillsKey;
				skills = selectedGlobalSkills;
			}
			canHaveCustomAnim = skills.canHaveCustomAnim();
			canHaveCustomNames = skills.canHaveCustomNames();
		}

		void setSpecies(Species species, boolean allowCustomAnim, boolean allowCustomNames)	{
			this.species = species;
			this.allowCustomAnim = allowCustomAnim;
			this.allowCustomNames = allowCustomNames;
		}
		void setSpecies(Species species)	{ this.species = species; }
		private boolean checkAnimUpdate(CivilizationRecord civ)	{
			if (!allowCustomAnim || !civ.hasPreferedAnim())
				return false;
			if (species.animKey().equals(civ.prefAnimKey))
				return false; // No change needed
			return true;
		}
		private boolean processUserSelection()	{ // TODO BR:
			CivRecordList customCivs = allCustomCiv.getFileKey(skillsKey);
			CivilizationRecord civ = customCivs.nextRandom();
			if (civ == null) {
				System.err.println("Error: Custom Species user selection pointed to empty civilization: " + skillsKey);
				isValid = false;
				return isValid;
			}
			species.setSpeciesSkills(civ.skillsKey, civ.speciesOptions);
			boolean tryNewAnim = checkAnimUpdate(civ);
			boolean customNames = civ.fullAnim;
			if (tryNewAnim)
				if (civ.isFullAnim()) { // Full custom
					species.setNewSpeciesAnim(civ.prefAnimKey);
					species.setAllCustomNames(civ, langDir);
					isAnimFromSkills = true;
					isNamesFromSkills = true;
					isValid = true;
					return isValid;
				}
				else { // Try internal anim if remaining
					CivRecordList internalCivs = internalListMap.getFullAnimCiv(civ.prefAnimKey);
					CivilizationRecord intCiv = internalCivs.nextRandom();
					if (intCiv != null) { // Use internal anim
						species.setNewSpeciesAnim(civ.prefAnimKey);
						species.setAllAnimNames(intCiv, langDir);
						isValid = true;
						return isValid;
					}
				}
			// Can not change anim -> set current one
			return setNominalAnimNames();
		}
		private boolean processReworked()		{ // TODO
			String newSkillsKey = animationKey(skillsKey);
			if (!newSkillsKey.isEmpty())
				species.setSpeciesSkills(fileToSkills(newSkillsKey));
			return isValid;
		}
		private boolean processReworkedFull()	{ // TODO
			return isValid;
		}
		private boolean processSameAsPlayer()	{ // TODO
			species.setSpeciesSkills(optionToSkills(playerOptions));
			return isValid;
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
		private boolean processFileFlt()		{ // TODO
			if (!allowedRaceList.isEmpty())
				species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
			return isValid;
		}
		private boolean processAllFiles()		{ // TODO
			if (!alienRaceList.isEmpty())
				species.setSpeciesSkills(fileToSkills(random(alienRaceList)));
			return isValid;
		}
		private boolean processFileRaces()		{ // TODO
			if (rng().nextBoolean())
				if (allowedRaceList.isEmpty())
					species.setSpeciesSkills(random(allInternalKeys));
				else
					species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
			else
				species.setSpeciesSkills(random(allInternalKeys));
			return isValid;
		}
		private boolean processAll()			{ // TODO
			if (rng().nextBoolean())
				if (rng().nextBoolean())
					if (allowedRaceList.isEmpty())
						species.setSpeciesSkills(optionToSkills(playerOptions));
					else
						species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
				else
					species.setSpeciesSkills(optionToSkills(playerOptions));
			else if (rng().nextBoolean())
				species.setSpeciesSkills(random(allInternalKeys));
			else
				species.setSpeciesSkills(RANDOM_RACE_KEY);
			return isValid;
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

		private boolean setNominalAnimNames()	{
			CivRecordList internalCivs = internalListMap.getFullAnimCiv(species.animKey());
			CivilizationRecord animCiv = internalCivs.nextRandom();
			if (animCiv != null) { // Use internal anim
				species.setAllAnimNames(animCiv, langDir);
				species.lockUsedNames();
				animCiv.markAsUsed();
				isValid = true;
			}
			else
				isValid = false;
			return isValid;
		}
	}

//	private Species createIconSpecies(String animKey)	{ // TODO
//		Species species = new Species(animKey);
//
//		// Civilization Name
//		String CivilzationName = playerSpecies.civilizationName();
//		if (CivilzationName.isEmpty())
//			System.err.println("Error: No available name for Player Civilization");
//
//		// Home World Name
//		String systemName = opts.selectedHomeWorldName();
//		if (systemName.isEmpty())
//			systemName = playerSpecies.nextAvailableHomeworldExt();
//		else
//			playerSpecies.usedHomeNames().add(systemName);
//		playerSpecies.initialHomeWorld = systemName;
//
//		// Leader Name
//		String leaderName = opts.selectedLeaderName();
//		if (leaderName.isEmpty())
//			leaderName = playerSpecies.nextAvailableLeaderExt();
//		else
//			playerSpecies.usedLeaderNames().add(leaderName);
//		playerSpecies.initialLeaderName = leaderName;
//
//		// Player Color
//		int color = options().selectedPlayerColor();
//		playerSpecies.colorId = color;
//		civilizationColors.remove(color);
//		//created = true;
//	}
	private void initLists()	{
		int numAliens = opts.selectedNumberOpponents();
		for (int alienId=0; alienId<numAliens; alienId++) {
			AlienInfo alien	= new AlienInfo(alienId);
			alienSpecies.add(alien);
		}
	}
	private final class AlienInfo {
		private final int alienId;
		private final int civId;
		private final String animKey;
		private final SpecificCROption skills;
		private final boolean iconSelectedAnim;
		private final boolean hasSkills;
		private final boolean internalSkills;
		private final boolean tryChangeAnim;

		private boolean randomAnim;
		private boolean tryChangeName = true;
		private boolean created = false;
		private String skillsKey;
		private Species species;
		private DynOptions options;

		private AlienInfo(int id)	{
			alienId	= id;
			civId	= alienId + 1;
			animKey	= selectedAliens[alienId];
			iconSelectedAnim = animKey != null;
			randomAnim = !iconSelectedAnim;

			skills = getSkills();
			hasSkills = !skills.isBaseRace();
			if (hasSkills)
				internalSkills = AllInternalKeys.contains(animKey);
			else
				internalSkills = false;

			tryChangeAnim = hasSkills && !internalSkills && animKey == null;
		}
		private SpecificCROption getSkills()	{
			String localSkillsKey	= opts.specificOpponentCROption(civId);
			SpecificCROption skills = SpecificCROption.set(localSkillsKey);
			if (useLocalSkills && !skills.isSelection()) {
				skillsKey = localSkillsKey;
				return skills;
			}
			else {
				skillsKey = selectedGlobalSkillsKey;
				return selectedGlobalSkills;
			}
		}
		private void createIconSpecies()	{ // TODO
			species = new Species(animKey);
//			species = new Species(animKey, skillsKey, options);
			

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
			civilizationColors.remove(color);			created = true;
		}
		private void processSkills()	{
			switch (skills) {
				case USER_SELECTION:
					species.setSpeciesSkills(fileToSkills(skillsKey));
					break;
				case REWORKED:
				case REWORKED_FULL:
					String skillKey = animationKey(skillsKey);
					if (!skillKey.isEmpty())
						species.setSpeciesSkills(fileToSkills(skillKey));
					break;
				case PLAYER:
					species.setSpeciesSkills(optionToSkills(playerOptions));
					break;
				case RANDOM: // Create a random race
					species.setSpeciesSkills(RANDOM_RACE_KEY);
					break;
				case RANDOM_10: // Choose randomly in the base list
					species.setSpeciesSkills(random(baseInternalKeys));
					break;
				case RANDOM_16: // Choose randomly including the Modnar Races
					species.setSpeciesSkills(random(allInternalKeys));
					break;
				case FILES_FLT:
					if (allowedRaceList.isEmpty())
						species.setSpeciesSkills(animKey);
					else
						species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
					break;
				case ALL_FILES:
					if (alienRaceList.isEmpty())
						species.setSpeciesSkills(animKey);
					else
						species.setSpeciesSkills(fileToSkills(random(alienRaceList)));
					break;
				case FILES_RACES:
					if (rng().nextBoolean())
						if (allowedRaceList.isEmpty())
							species.setSpeciesSkills(random(allInternalKeys));
						else
							species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
					else
						species.setSpeciesSkills(random(allInternalKeys));
					break;
				case ALL:
					if (rng().nextBoolean())
						if (rng().nextBoolean())
							if (allowedRaceList.isEmpty())
								species.setSpeciesSkills(optionToSkills(playerOptions));
							else
								species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
						else
							species.setSpeciesSkills(optionToSkills(playerOptions));
					else if (rng().nextBoolean())
						species.setSpeciesSkills(random(allInternalKeys));
					else
						species.setSpeciesSkills(RANDOM_RACE_KEY);
					break;
				case ORIGINAL_SPECIES: // default as vanilla
				default:
					if (randomInternalAbility) // original Advanced Option random abilities
						if (randomInternalAbility16)
							species.setSpeciesSkills(random(allInternalKeys));
						else
							species.setSpeciesSkills(random(baseInternalKeys));
					else
						species.setSpeciesSkills(animKey);
					break;
			}
		}
	}

	private StringList buildAlienRaces() {
		StringList alienList = new StringList();
		StringList allspeciesOptions = new StringList();
		StringList internalSpecies = options().getInternalSpeciesList();
		int maxRaces = options().selectedNumberOpponents();
		int mult = IGameOptions.MAX_OPPONENT_TYPE;

		// first, build randomized list of opponent races
		for (int i=0; i<mult; i++) {
			shuffle(internalSpecies);
			allspeciesOptions.addAll(internalSpecies);
		}
		if(opts.fullyScrambledSpecies())
			shuffle(allspeciesOptions);

		
		// next, remove from that list the player and any selected opponents
		String[] selectedOpponents = options().selectedOpponentRaces();
		isRandomOpponent = new boolean[selectedOpponents.length]; // BR: only Random Races will be customized
		allspeciesOptions.remove(options().selectedPlayerRace());

		for (int i=0;i<maxRaces;i++) {
			if (selectedOpponents[i] != null) {
				allspeciesOptions.remove(selectedOpponents[i]);
				isRandomOpponent[i] = false;
			} else {
				isRandomOpponent[i] = true;
			}
		}
		// build alien race list, replacing unselected opponents (null)
		// with remaining options
		for (int i=0;i<maxRaces;i++) {
			if (selectedOpponents[i] == null)
				alienList.add(allspeciesOptions.remove(0));
			else
				alienList.add(selectedOpponents[i]);
		}
		return alienList;
	}

	private Species createPlayerSpecies()	{
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
	private String animationKey(String animKey)	{
		// Search for an unused rework
		StringList list = animationMap.get(animKey);
		if (list.isEmpty())
			// No more: then refill
			list.addAll(animMapOrigin.get(animKey));
		shuffle(list);
		shuffle(list);
		// Get one (could be empty!)
		String skillKey = list.removeFirst();
		//System.out.println("Rework Anim key = " + animKey + " -> Skill key = " + skillKey);
		return skillKey;
	}
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
