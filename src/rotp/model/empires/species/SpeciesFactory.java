package rotp.model.empires.species;

import static rotp.model.empires.species.SkillsFactory.getAllAlienSkills;
import static rotp.model.empires.species.SkillsFactory.getAllowedAlienSkills;

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

public class SpeciesFactory implements ISpecies, Base {
	private final IGameOptions opts;
//	private final SkillsFactory sf = new SkillsFactory();
	private final List<Species> alienSpecies		= new ArrayList<>();
	private final List<Integer> civilizationColors	= new ArrayList<>();
	private final RaceList customSpeciesList		= SkillsFactory.newRaceList();
	private final HashMap<String, StringList> animMapOrigin	= customSpeciesList.animationMap();
	private final HashMap<String, StringList> animationMap	= initAnimationMap();
	private final StringList allowedRaceList;
	private final StringList alienRaceList;
	private List<Integer> pureSelectedAnim;
	private List<Integer> skillSelectedAnim;
	private List<Integer> randomSelectedAnim;
	private final int playerId;
	private final Species playerSpecies;
	private final String[] selectedOpponents;

	// Restart var
	private final GalaxyCopy galaxyCopy;
	private final GalaxyBaseData galSrc;
	private final EmpireBaseData empSrc[];
	private final boolean isRestart;
	StringList AllInternalKeys;

	private boolean[] isRandomOpponent;	// BR: only Random Races will be customized
	private String playerSkillKey;		// BR: in case Alien races are a copy of player race

	public SpeciesFactory(IGameOptions options, GalaxyCopy galaxyCopy, int playerId)	{
		this.galaxyCopy = galaxyCopy;
		this.playerId = playerId;
		opts = options;
		isRestart = galaxyCopy != null;
		
		galSrc = isRestart? galaxyCopy.galSrc : null;
		empSrc = isRestart? galSrc.empires : null;

		Species.cleanUsedNames();
		civilizationColors.addAll(opts.possibleColors());
		playerSpecies = createPlayerSpecies();
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
		AllInternalKeys		= options().allRaceKeyList();

		createAlienSpecies();

		Species.cleanUsedNames();	
	}
	public List<Species> getAlienSpecies()	{ return alienSpecies; }
	public Species getPlayerSpecies()		{ return playerSpecies; }

	private boolean debug()			{ return true; }
	private boolean createAlienSpecies()	{ // TODO
		initTables();

		return false;
	}
	private final class AlienInfo {
		final int alienId;
		final int civId;
		final String animKey;
		final String skillKey;
		final boolean internalSkill;

		AlienInfo(int id)	{
			alienId	= id;
			civId	= alienId + 1;
			animKey	= selectedOpponents[alienId];
			skillKey	= opts.specificOpponentCROption(civId);
			if (hasSkills())
				internalSkill = AllInternalKeys.contains(animKey);
			else
				internalSkill = false;
		}

		boolean isSelectedAnim()	{ return animKey != null; }
		boolean hasSkills()			{ return skillKey != null; }
	}

	private void initTables()	{
		int numAliens = opts.selectedNumberOpponents();
		StringList AllInternalKeys = options().allRaceKeyList();
		for (int alienId=0; alienId<numAliens; alienId++) {
			int civId = alienId + 1;
			AlienInfo alien	= new AlienInfo(alienId);
			
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
