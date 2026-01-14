package rotp.model.empires.species;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import rotp.ui.game.BaseModPanel.ModText;
import rotp.ui.main.SystemPanel;
import rotp.ui.util.IParam;

/**
 * Interface Custom Species Setting
 */
public interface ICRSettings extends IParam {
	Color optionC		= SystemPanel.blackText;	// Unselected option Color
	Color selectC		= SystemPanel.whiteText;	// Selected option color
	Color frameC		= SystemPanel.blackText;	// Setting frame color
	Color settingPosC	= SystemPanel.limeText;		// Setting name color
	Color settingNegC	= SystemPanel.redText;		// Setting name color
	Color settingC		= SystemPanel.whiteText;		// Setting name color
	Color textC			= SystemPanel.whiteText;
	Color labelC		= SystemPanel.orangeText;
	Color costC			= SystemPanel.blackText;
	Color malusC		= SystemPanel.redText;

	String  getLabel();
	default void guiSelect()	{}
	default void updateGui()	{}
	default void updateGui(RSettingPanel panel)	{}
	/**
	 *  Push current settings to SpeciesSkill
	 */
	default void settingToSkill(SpeciesSkills skills)	{ updateOption(skills.speciesOptions()); }
	/**
	 *  Pull settings from SpeciesSkill
	 */
	default void skillToSetting(SpeciesSkills skills)	{ updateOptionTool(skills.speciesOptions()); }
	default void clearImage()	{}

	default void settingText(ModText txt)		{}
	default void hasNoCost(boolean hasNoCost)	{}
	default void setValueFromCost(float cost)	{}
	default void setRandom(float cost)			{}
	default void optionText(ModText optionText, int i)	{}
	default void setRandom(float min, float max, boolean gaussian)	{}
	default void drawSetting(int sizePad, int endPad, int optionH, int currentdWith,
			Color frameC, int frameShift, int xLine, int yLine, int settingIndent,
			int shift, int settingH, int frameTopPad, int wSetting, int optionIndent,
			boolean retina, float retinaFactor)	{}

	default boolean toggle(MouseEvent e, MouseWheelEvent w, int idx)	{ return false; }
	default boolean isSpacer()			{ return false; }
	default boolean hasNoCost()			{ return true; }
	default boolean isBullet()			{ return false; }

	default int index()					{ return 0; }
	default int bulletStart()			{ return 0; }
	default int bulletBoxSize()			{ return 0; }
	default int deltaYLines()			{ return 0; }

	default ModText settingText()		{ return null; }
	default ModText optionText(int i)	{ return null; }
	default ModText[] optionsText()		{ return null; }
	default BufferedImage getImage()	{ return null; }
	default Color getCostColor()		{ return null;}

	default float costFactor()			{ return 0f; }
	default float settingCost()			{ return 0f; }
	default float lastRandomSource()	{ return 0f; }

	default String guiSettingDisplayStr()		{ return ""; }
	default String guiCostOptionStr(int idx)	{ return ""; }
}
