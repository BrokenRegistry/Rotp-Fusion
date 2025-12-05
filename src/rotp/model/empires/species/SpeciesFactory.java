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
import rotp.model.empires.species.SkillsFactory.RaceList;
import rotp.model.galaxy.Galaxy.GalaxyBaseData;
import rotp.model.galaxy.GalaxyFactory.GalaxyCopy;
import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.util.StringList;
import rotp.util.Base;

public final class SpeciesFactory implements ISpecies, Base {
	private final IGameOptions opts;
//	private final SkillsFactory sf = new SkillsFactory();
//	private final List<Species> alienSpecies		= new ArrayList<>();
	private final List<Integer> civilizationColors	= new ArrayList<>();
	private final RaceList customSpeciesList		= SkillsFactory.newRaceList();
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
	private final String[] selectedOpponents;
	private final boolean randomInternalAbility, randomInternalAbility16;
	private final boolean useLocalSkills;
	private final String selectedGlobalSkillsKey;
	private final SpecificCROption selectedGlobalSkills;

	// Restart var
	private final GalaxyCopy galaxyCopy;
	private final GalaxyBaseData galSrc;
	private final EmpireBaseData empSrc[];
	private final boolean isRestart;
	StringList AllInternalKeys;

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
		isRestart = galaxyCopy != null;
		
		galSrc = isRestart? galaxyCopy.galSrc : null;
		empSrc = isRestart? galSrc.empires : null;
		randomInternalAbility	= opts.randomizeAIAbility();
		randomInternalAbility16	= opts.randomizeAIAbility16();
		selectedGlobalSkillsKey	= opts.selectedGlobalAbility();
		selectedGlobalSkills	= SpecificCROption.set(selectedGlobalSkillsKey);
		useLocalSkills = opts.useSelectableAbilities();

		Species.cleanUsedNames();
		civilizationColors.addAll(opts.possibleColors());
		playerSpecies = createPlayerSpecies();
		playerOptions = playerSpecies.speciesOptions();
		if (debug())
			System.out.println("Player: " + playerSpecies.toString());

		if (!isRestart || opts.selectedRestartAppliesSettings()) {
			allowedRaceList	= getAllowedAlienSkills();
			alienRaceList	= getAllAlienSkills();
		}
		else {
			allowedRaceList	= null;
			alienRaceList	= null;
		}

		selectedOpponents	= opts.selectedOpponentRaces();
		allInternalKeys		= opts.allRaceKeyList();
		baseInternalKeys	= opts.baseRaceKeyList();
		internalSpeciesKeys	= opts.getInternalSpeciesList();
		createAlienSpecies();

		Species.cleanUsedNames();	
	}
	private boolean debug()					{ return true; }

	private boolean createAlienSpecies()	{ // TODO
		initLists();
		createPureSelectedAnim();
		

		return false;
	}
	private void createPureSelectedAnim()	{
		for (AlienInfo alien : alienSpecies) {
			if (alien.iconSelectedAnim && !alien.created) {
				alien.createIconSpecies();
			}
		}
	}
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
			animKey	= selectedOpponents[alienId];
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
			SpecificCROption skills = SpecificCROption.set(skillsKey);
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
				case USER_CHOICE:
					species.setSpeciesSkills(fileToSkills(skillsKey));
					break;
				case REWORKED:
					String skillKey = reworkedKey(skillsKey);
					if (!skillKey.isEmpty())
						species.setSpeciesSkills(fileToSkills(skillKey));
					break;
				case PLAYER:
					species.setSpeciesSkills(optionToSkills(playerOptions));
					break;
				case RANDOM: // Create a random race
					species.setSpeciesSkills(RANDOM_RACE_KEY);
					break;
				case RANDOM_BASE: // Choose randomly in the base list
					species.setSpeciesSkills(random(baseInternalKeys));
					break;
				case RANDOM_MOD: // Choose randomly including the Modnar Races
					species.setSpeciesSkills(random(allInternalKeys));
					break;
				case FILES_FLT:
					if (allowedRaceList.isEmpty())
						species.setSpeciesSkills(animKey);
					else
						species.setSpeciesSkills(fileToSkills(random(allowedRaceList)));
					break;
				case FILES_NO_FLT:
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
				case BASE_RACE: // default as vanilla
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
