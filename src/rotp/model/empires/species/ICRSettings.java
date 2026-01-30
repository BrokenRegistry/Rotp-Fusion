package rotp.model.empires.species;

import rotp.ui.util.IParam;

/**
 * Interface Custom Species Setting
 */
public interface ICRSettings<T> extends IParam<T> {
	default void guiSelect()	{}
	default void updateGui()	{}
	/**
	 *  Push current settings to SpeciesSkill
	 */
	default void settingToSkill(SpeciesSkills skills)	{ updateOption(skills.speciesOptions()); }
	/**
	 *  Pull settings from SpeciesSkill
	 */
	default void skillToSetting(SpeciesSkills skills)	{ updateOptionTool(skills.speciesOptions()); }

	default void hasNoCost(boolean hasNoCost)	{}
	default void setValueFromCost(float cost)	{}
	default void setRandom(float cost)			{}
	default void setRandom(float min, float max, boolean gaussian)	{}

	default boolean isSpacer()			{ return false; }
	default boolean hasNoCost()			{ return true; }
	default boolean isBullet()			{ return false; }
	default boolean isSettingString()	{ return false; }

	default int index()					{ return 0; }
	default int bulletStart()			{ return 0; }
	default int bulletBoxSize()			{ return 0; }
	default int deltaYLines()			{ return 0; }

	default float costFactor()			{ return 0f; }
	default float settingCost()			{ return 0f; }
	default float lastRandomSource()	{ return 0f; }

	default String guiSettingDisplayStr()		{ return ""; }
	default String guiCostOptionStr(int idx)	{ return ""; }
}
