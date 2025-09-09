package rotp.model.empires;

import static rotp.model.empires.CustomRaceDefinitions.fileToAlienRace;
import static rotp.model.empires.CustomRaceDefinitions.optionToAlienRace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rotp.model.galaxy.Galaxy;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.util.Base;

public final class Species implements Base {

	private final String animationsKey, abilitiesKey; // Duplicate, for an easier Debug
	private final Race speciesAnimations, speciesAbilities;
	private final boolean isCustomSpecies, isRandomized, isRandomSelection;
	private final int nameIndex;
	private Empire empire;
	private String speciesName, systemsKey;
	private List<String> systemNames;	// link to race systemNames
	private List<SpeciesLabels> labelList;
	// =========================================================
	// Constructor And Initializers
	//
	public Species(String animations, String abilities, String fileName, Galaxy gal)	{
		speciesAnimations	= SpeciesManager.current().keyed(animations);
		speciesAbilities	= fileToAlienRace(fileName);
		animationsKey		= speciesAnimations.id();
		abilitiesKey		= speciesAbilities.id();
		isCustomSpecies		= speciesAbilities.isCustomRace();
		isRandomized		= speciesAbilities.isRandomized();
		isRandomSelection	= isCustomSpecies && !isRandomized;
		nameIndex	= getSpeciesIndex();
		init(gal);
	}
	public Species(String animations, String abilities, Galaxy gal)	{
		this(animations, abilities, (DynOptions)null, gal);
	}
	public Species(String animations, String abilities, DynOptions options, Galaxy gal)	{
		speciesAnimations	= SpeciesManager.current().keyed(animations);
		speciesAbilities	= SpeciesManager.current().keyed(abilities, options);
		animationsKey		= speciesAnimations.id();
		abilitiesKey		= speciesAbilities.id();
		isCustomSpecies		= speciesAbilities.isCustomRace();
		isRandomized		= speciesAbilities.isRandomized();
		isRandomSelection	= isCustomSpecies && !isRandomized;
		nameIndex	= getSpeciesIndex();
		init(gal);
	}
	public Species(String animations, DynOptions options, boolean isPlayer, Galaxy gal)	{
		speciesAnimations	= SpeciesManager.current().keyed(animations);
		IGameOptions opts	= guiOptions();
		if (isPlayer)
			if (opts.selectedPlayerIsCustom())
				speciesAbilities = SpeciesManager.current().keyed(CustomRaceDefinitions.CUSTOM_RACE_KEY, options);
			else
				speciesAbilities = speciesAnimations;
		else
			speciesAbilities = optionToAlienRace(options);
		animationsKey	= speciesAnimations.id();
		abilitiesKey	= speciesAbilities.id();
		isCustomSpecies	= speciesAbilities.isCustomRace();
		isRandomized	= speciesAbilities.isRandomized();
		isRandomSelection	= isCustomSpecies && !isRandomized;
		nameIndex	= getSpeciesIndex();
		init(gal);
	}
	Species(Empire empire, Galaxy gal)	{ // reload transient
		speciesAnimations	= SpeciesManager.current().keyed(empire.raceKey());
		speciesAbilities	= SpeciesManager.current().keyed(empire.dataRaceKey(), empire.raceOptions());
		animationsKey		= speciesAnimations.id();
		abilitiesKey		= speciesAbilities.id();
		isCustomSpecies		= speciesAbilities.isCustomRace();
		isRandomized		= speciesAbilities.isRandomized();
		isRandomSelection	= isCustomSpecies && !isRandomized;
		nameIndex	= empire.raceNameIndex();
		this.empire	= empire;
		init(gal);
	}
	void empire(Empire empire)	{ this.empire = empire; }
	void init(Galaxy gal)		{
		speciesName(getSpeciesName(nameIndex));

		String prefix = speciesAbilities.worldsPrefix();
		String suffix = speciesAbilities.worldsSuffix();
		if (isCustomSpecies && !speciesAbilities.speciesLabels().systemNames.isEmpty())
			systemsKey = (prefix + abilitiesKey() + suffix).trim();
		else
			systemsKey = (prefix + animationsKey() + suffix).trim();
		labelList = new ArrayList<>();
		if (gal != null && isCustomSpecies) {
			boolean reloadCS = gal.customSpeciesNames().containsKey(systemsKey);
			if (reloadCS)
				labelList.add(gal.customSpeciesNames().get(systemsKey));
			if (!speciesAbilities.speciesLabels().isEmpty()) {
				if (!reloadCS)
					gal.customSpeciesNames().put(systemsKey, speciesAbilities.speciesLabels());
				labelList.add(speciesAbilities.speciesLabels());
			}
		}
		labelList.add(speciesAnimations.speciesLabels());
		systemNames = rawSystemNames();
	}

	// =========================================================
	// Temporary public Methods && for Empire only methods
	//
	public Race race()				{ return speciesAnimations; }
	public Race dataRace()			{ return speciesAbilities; }

	String id()						{ return animationsKey + "_" + abilitiesKey; }
	String animationsKey()			{ return animationsKey; }
	String abilitiesKey()			{ return abilitiesKey; }
	DynOptions speciesOptions()		{ return speciesAbilities.raceOptions(); }
	boolean isCustomRace()			{ return isCustomSpecies; }
	int speciesNameIndex()			{ return nameIndex; }
	void speciesName(String name)	{ speciesName = name; }

	// =========================================================
	// Custom Species Edition
	//
	void setDescription(String desc, int id)	{
		if (isCustomSpecies && !isRandomized) { // For security! Should always be the case.
			speciesAbilities.setDescription(desc, id);
			File folder = SpeciesManager.current().customLangFolder(speciesAbilities);
		}
	}


	// =========================================================
	// Species Conditional
	//
	public String speciesName()					{ return speciesName; }
	public String familyName()					{ return nameVariant(0); }
	public String speciesName(int index)		{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.nameVariant(index);
			if (name != null)
				return name;
		}
		return speciesAnimations.nameVariant(index);
	}
	public String nameVariant(int index)		{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.nameVariant(index);
			if (name != null)
				return name;
		}
		return speciesAnimations.nameVariant(index);
	}
	public String randomLeaderName()			{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.randomLeaderName();
			if (name != null)
				return name;
		}
		return speciesAnimations.randomLeaderName();
	}
	public void resetSetupImage()				{
		if (isCustomSpecies && !isRandomized) {
			if (speciesAbilities.setupImageKey() != null) {
				speciesAbilities.resetSetupImage();
				return;
			}
		}
		speciesAnimations.resetSetupImage();
	}
	public void resetMugshot()					{
		if (isCustomSpecies && !isRandomized) {
			if (speciesAbilities.mugshotKey() != null) {
				speciesAbilities.resetMugshot();
				return;
			}
		}
		speciesAnimations.resetMugshot();
	}
	public String setupName()					{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.setupName();
			if (name != null)
				return name;
		}
		return speciesAnimations.setupName();
	}
	public String defaultHomeworldName()		{
		if (isCustomSpecies && !isRandomized) {
			if (speciesAbilities.hasHomeworldNames())
				return speciesAbilities.defaultHomeworldName();
		}
		return speciesAnimations.defaultHomeworldName();
	}
	public String getDescription1()				{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.getDescription1();
			if (name != null)
				return name;
		}
		return speciesAnimations.getDescription1();
	}
	public String getDescription2()				{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.getDescription2();
			if (name != null)
				return name;
		}
		return speciesAnimations.getDescription2();
	}
	public String getDescription3()				{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.getDescription3();
			if (name != null)
				return name;
		}
		return speciesAnimations.getDescription3();
	}
	public String getDescription3(String s)		{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.getDescription3(s);
			if (name != null)
				return name;
		}
		return speciesAnimations.getDescription3(s);
	}
	public String getDescription4()				{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.getDescription4();
			if (name != null)
				return name;
		}
		return speciesAnimations.getDescription4();
	}

	String nextAvailableLeader()				{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.nextAvailableLeader();
			if (name != null)
				return name;
		}
		return speciesAnimations.nextAvailableLeader();
	}
	String nextAvailableHomeworld()				{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.nextAvailableHomeworld();
			if (name != null)
				return name;
		}
		return speciesAnimations.nextAvailableHomeworld();
	}
	String replaceTokens(String s, String key)	{
		if (key.equals("player")) // BR: many confusion in translations
			s = replaceTokens(s, "my");
		List<String> tokens = varTokens(s, key);
		String s1 = s;
		for (String token: tokens) {
			String replString = concat("[",key, token,"]");
			// leader name is special case, not in dictionary
			if (token.equals("_name")) {
				s1 = s1.replace(replString, leader().name());
				continue;
			}
			if (token.equals("_home")) {
				s1 = s1.replace(replString, empire.sv.name(empire.capitalSysId()));   
				continue;
			}
			if (isCustomPlayer() && !isRandomized && token.equals("_race")) {
				s1 = s1.replace(replString, dataRace().setupName);			  
				continue;
			}
			if (isCustomPlayer() && !isRandomized && token.equals("_empire")) {
				s1 = s1.replace(replString, empire.empireTitle());
				continue;
			}
			int idx = nameIndex;
			if (nameIndex < 0) {
				idx = -nameIndex - 1;
				List<String> values = substrings(speciesAbilities.text(token), ',');
				if (!values.isEmpty()) {
					String value = idx < values.size() ? values.get(idx) : values.get(0);
					s1 = s1.replace(replString, value);
					continue;
				}
			}
			List<String> values = substrings(speciesAnimations.text(token), ',');
			if (!values.isEmpty()) {
				String value = idx < values.size() ? values.get(idx) : values.get(0);
				s1 = s1.replace(replString, value);
				continue;
			}
		}
		return s1;
	}
	String label(String token)					{
		int idx = nameIndex;
		if (nameIndex < 0) {
			idx = -nameIndex - 1;
			List<String> values = substrings(speciesAbilities.text(token), ',');
			if (!values.isEmpty())
				return idx < values.size() ? values.get(idx) : values.get(0);
		}
		List<String> values = substrings(speciesAnimations.text(token), ',');
		return idx < values.size() ? values.get(idx) : values.get(0);	  
	}
	String randomSystemName()	{
		// this is only called when a new system is scouted
		// the name is stored on the empire's system view for this system
		// and transferred to the system when it is colonized
		Galaxy gal = galaxy();
		if (!gal.raceSystemNames().containsKey(systemsKey))
			loadSystemsNames(systemsKey, -5);

		List<String> remainingNames = gal.raceSystemNames().get(systemsKey);
		while (remainingNames.isEmpty()) {
			int nextSeq = gal.raceSystemCtr().get(systemsKey) + 1;
			loadSystemsNames(systemsKey, nextSeq);
		}

		String nextName = remainingNames.remove(0);
		log("Naming system:", nextName);
		return nextName;
	}
	public List<String> rawSystemNames()	{
		for (SpeciesLabels labels : labelList)
			if (!labels.systemNames.isEmpty())
				return labels.systemNames;
		return new ArrayList<>(); // Should never happen
	}
	public List<String> finalSystemNames()	{
		// BR: add custom species prefix and suffix
		String prefix = speciesAbilities.worldsPrefix();
		String suffix = speciesAbilities.worldsSuffix();
		Collections.shuffle(systemNames);
		if (prefix.isEmpty() && suffix.isEmpty())
			return systemNames;
		List<String> names = new ArrayList<>();
		for (String s: systemNames)
			names.add((prefix + s + suffix).trim());
		return names;
	}
//	public List<String> systemNames(String prefix, String suffix)	{
//		// BR: add custom species prefix and suffix
//		Collections.shuffle(systemNames);
//		if (prefix.isEmpty() && suffix.isEmpty())
//			return systemNames;
//		List<String> names = new ArrayList<>();
//		for (String s: systemNames)
//			names.add((prefix + s + suffix).trim());
//		return names;
//	}
	// =========================================================
	// Species Animations
	//

	// =========================================================
	// Species Abilities
	//
	public String abilitiesName()	{ return speciesAbilities.name(); } // For debug only

	// ===== Species Conditional Abilities =====

	// =========================================================
	// Other Methods should stay private
	//
	private boolean isPlayer()			{ return empire.isPlayer(); }
	private boolean isCustomPlayer()	{ return isCustomSpecies && isPlayer(); }
	private Leader leader()				{ return empire.leader(); }
	private int getSpeciesIndex()		{
		if (isCustomSpecies && !isRandomized) {
			String name = speciesAbilities.nextAvailableName();
			if (name != null)
				return -speciesAbilities.nameIndex(name)-1;
		}
		String name = speciesAnimations.nextAvailableName();
		return speciesAnimations.nameIndex(name);
	}
	private String getSpeciesName(int index)	{
		if (index<0) {
			String name = speciesAbilities.nameVariant(-index-1);
			if (name != null)
				return name;
		}
		return speciesAnimations.nameVariant(index);
	}
	private List<String> removeUsedStarNames(List<String> allNames)	{
		for (StarSystem sys : galaxy().starSystems())
			allNames.remove(sys.name().trim()); // custom species may add confusing spaces

		// Multiple and Custom species may share the same list... We have to looks thru all systems view
		// looking at the galaxy().starSystems() is not good enough. Named only when colonized.
		int n = galaxy().numStarSystems();
		for (Empire emp : galaxy().empires())
			if (!emp.extinct())
				for (int i=0; i<n; i++)
					if (emp.sv.isScouted(i))
						allNames.remove(empire.sv.name(i).trim());
		return allNames;
	}
//	private List<String> masterNameList()	{
//		List<String> baseNames = systemNames(speciesAbilities.worldsPrefix(), speciesAbilities.worldsSuffix());
//		List<String> names = new ArrayList<>(baseNames);
//
//		for (String s: baseNames)
//			names.add(text("COLONY_NAME_2", s));
//		for (String s: baseNames)
//			names.add(text("COLONY_NAME_3", s));
//		for (String s: baseNames)
//			names.add(text("COLONY_NAME_4", s));
//		for (String s: baseNames)
//			names.add(text("COLONY_NAME_5", s));
//		return names;
//	}
	private void loadSystemsNames(String rId, int i)	{
		Galaxy gal = galaxy();
		List<String> baseNames = rawSystemNames();
		List<String> names = new ArrayList<>();
		switch (i) {
		case -5:
			names = new ArrayList<>(baseNames);
			break;
		case -4:
			for (String s : baseNames)
				names.add(text("COLONY_NAME_2", s));
			break;
		case -3:
			for (String s : baseNames)
				names.add(text("COLONY_NAME_3", s));
			break;
		case -2:
			for (String s : baseNames)
				names.add(text("COLONY_NAME_4", s));
			break;
		case -1:
			for (String s : baseNames)
				names.add(text("COLONY_NAME_5", s));
			break;
		default:
			String tail = " " + Base.letter[i];
			for (String s : baseNames)
				names.add(s + tail);
			break;
		}
		gal.raceSystemNames().put(rId, removeUsedStarNames(names));
		gal.raceSystemCtr().put(rId, i);
	}
}
