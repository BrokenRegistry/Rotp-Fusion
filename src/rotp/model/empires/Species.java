package rotp.model.empires;

import java.io.Serializable;

public class Species implements ISpecies, Serializable {
	private static final long serialVersionUID = 1L;

	String	speciesAnimKey;		// Internal Species (jar description)
	String	speciesSkillKey;
	boolean	isOriginalSkill;	// Same Animation and Skill
	boolean	isInternalSkill;	// (jar description) but can be different than Animations
	boolean	isScrambled;		// Internal Skill but scrambled
	boolean	isRandomized;
	
	String SpeciesName;	// Must be unique
	String LeaderName;	// Must be unique
	String Homeworld;	// Must be unique

	String	speciesAnimKey()			{ return speciesAnimKey; };		// Internal Species (jar description)
	void	speciesAnimKey(String s)	{ speciesAnimKey = s; };		// Internal Species (jar description)
	String	speciesSkillKey()			{ return speciesSkillKey; };
	void	speciesSkillKey(String s)	{ speciesSkillKey = s; };

	boolean	isOriginalSkill()			{ return isOriginalSkill; };	// Same Animation and Skill
	void	isOriginalSkill(boolean b)	{ isOriginalSkill = b; };		// Same Animation and Skill
	boolean	isInternalSkill()			{ return isInternalSkill; };	// (jar description) but can be different than Animations
	void	isInternalSkill(boolean b)	{ isInternalSkill = b; };		// (jar description) but can be different than Animations
	boolean	isScrambled()				{ return isScrambled; };		// Internal Skill but scrambled
	void	isScrambled(boolean b)		{ isScrambled = b; };			// Internal Skill but scrambled
	boolean	isRandomized()				{ return isRandomized; };
	void	isRandomized(boolean b)		{ isRandomized = b; };
	
	String	SpeciesName()			{ return SpeciesName; };	// Must be unique
	void	SpeciesName(String s)	{ SpeciesName = s; };		// Must be unique
	String	LeaderName()			{ return LeaderName; };		// Must be unique
	void	LeaderName(String s)	{ LeaderName = s; };		// Must be unique
	String	Homeworld()				{ return Homeworld; };		// Must be unique
	void	Homeworld(String s)		{ Homeworld = s; };			// Must be unique
	
}
