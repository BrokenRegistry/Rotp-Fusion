package rotp.model.empires;

import static rotp.model.empires.CustomRaceDefinitions.fileToAlienRace;
import static rotp.model.empires.CustomRaceDefinitions.optionToAlienRace;

import java.util.List;

import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.util.Base;

public final class Species implements Base {

	private final String animationsKey, abilitiesKey; // Duplicate, for an easier Debug
	private final Race speciesAnimations, speciesAbilities;
	private final boolean isCustomSpecies, isRandomized;
	private final int nameIndex;
	private Empire empire;
	private String speciesName;

	// =========================================================
	// Constructor And Initializers
	//
	public Species(String animations, String abilities, String fileName)	{
		speciesAnimations	= SpeciesManager.instance().keyed(animations);
		speciesAbilities	= fileToAlienRace(fileName);
		animationsKey		= speciesAnimations.id();
		abilitiesKey		= speciesAbilities.id();
		isCustomSpecies		= speciesAbilities.isCustomRace();
		isRandomized		= speciesAbilities.isRandomized();
		speciesAbilities.isAbilities(true);
		nameIndex	= getSpeciesIndex();
		speciesName	= getSpeciesName(nameIndex);
	}
	public Species(String animations, String abilities)	{
		this(animations, abilities, (DynOptions)null);
	}
	public Species(String animations, String abilities, DynOptions options)	{
		speciesAnimations	= SpeciesManager.instance().keyed(animations);
		speciesAbilities	= SpeciesManager.instance().keyed(abilities, options);
		animationsKey		= speciesAnimations.id();
		abilitiesKey		= speciesAbilities.id();
		isCustomSpecies		= speciesAbilities.isCustomRace();
		isRandomized		= speciesAbilities.isRandomized();
		speciesAbilities.isAbilities(true);
		nameIndex	= getSpeciesIndex();
		speciesName	= getSpeciesName(nameIndex);
	}
	public Species(String animations, DynOptions options, boolean isPlayer)	{
		speciesAnimations	= SpeciesManager.instance().keyed(animations);
		IGameOptions opts	= guiOptions();
		if (isPlayer)
			if (opts.selectedPlayerIsCustom())
				speciesAbilities = SpeciesManager.instance().keyed(CustomRaceDefinitions.CUSTOM_RACE_KEY, options);
			else
				speciesAbilities = speciesAnimations;
		else
			speciesAbilities = optionToAlienRace(options);
		animationsKey	= speciesAnimations.id();
		abilitiesKey	= speciesAbilities.id();
		isCustomSpecies	= speciesAbilities.isCustomRace();
		isRandomized	= speciesAbilities.isRandomized();
		speciesAbilities.isAbilities(true);
		nameIndex	= getSpeciesIndex();
		speciesName	= getSpeciesName(nameIndex);
	}
	Species(Empire empire)	{ // reload transient
		speciesAnimations	= SpeciesManager.instance().keyed(empire.raceKey());
		speciesAbilities	= SpeciesManager.instance().keyed(empire.dataRaceKey(), empire.raceOptions());
		animationsKey		= speciesAnimations.id();
		abilitiesKey		= speciesAbilities.id();
		isCustomSpecies		= speciesAbilities.isCustomRace();
		isRandomized		= speciesAbilities.isRandomized();
		speciesAbilities.isAbilities(true);
		nameIndex	= empire.raceNameIndex();
		speciesName	= getSpeciesName(nameIndex);
		this.empire	= empire;
	}
	void empire(Empire empire)	{ this.empire = empire; }

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




}
