package rotp.model.empires.species;

import static rotp.model.empires.species.DNAFactory.fileToAlienRaceInfo;
import static rotp.model.empires.species.SpeciesSettings.BASE_RACE_MARKER;
import static rotp.model.game.IGalaxyOptions.globalCROptions;
import static rotp.ui.util.IParam.labelFormat;
import static rotp.ui.util.IParam.realLangLabel;
import static rotp.ui.util.IParam.rowFormat;

import java.util.List;

import rotp.model.game.IGameOptions;
import rotp.model.game.RulesetManager;
import rotp.ui.RotPUI;
import rotp.ui.game.SetupGalaxyUI;
import rotp.ui.util.ParamList;
import rotp.util.LabelManager;

public interface ISpecies {
	String ORION_KEY = "ORION";

	class Utils {
		private static int currentIndex()			{ return SetupGalaxyUI.mouseBoxIndex()+1; }
		private static String convert(String key)	{
			String labelKey	= SpecificCROption.getLabel(key);
			String labelTxt	= LabelManager.current().label(labelKey);
			if (labelTxt.equals(labelKey))
				return key;
			return labelTxt;
		}
		private static IGameOptions options()	{ return RulesetManager.current().currentOptions(); }
		private static void postSelect(boolean click)	{ RotPUI.setupGalaxyUI().postSelectionLight(click); }
	}
	// For Guide, do not add to panels
	class ParamListGlobalAbilities extends ParamList { // For Guide
		public ParamListGlobalAbilities(String gui, String name, String defaultValue) {
			super(gui, name, null, defaultValue);
			isDuplicate(true); // To activate get and set optionValue
		}
		@Override public String getOptionValue(IGameOptions options)	{ return globalCROptions.get(); }
		@Override public void setOptionValue(IGameOptions options, String str) {
			globalCROptions.set(str);
			Utils.postSelect(false);
		}
		@Override public String	guideValue()	{ return Utils.convert(get()); }
		@Override public String getRowGuide(int id)	{
			String key		= getLangLabel(id);
			String labelKey	= SpecificCROption.getLabel(key);
			String help		= realLangLabel(labelKey + LABEL_DESCRIPTION);
			if (help != null)
				return rowFormat(labelFormat(realLangLabel(labelKey)), help);

			key  = getGuiValue(id);
			help = realLangLabel(key + LABEL_DESCRIPTION);
			SpeciesSkills speciesSkill = fileToAlienRaceInfo(null, key);
			String raceName = speciesSkill.setupName;
			if (key.startsWith(BASE_RACE_MARKER))
				help = labelFormat(name(id)) + "<i>(Original species)</i>&nbsp " + speciesSkill.getDescription1();
			else
				help = labelFormat(raceName) + speciesSkill.getDescription1();
			help += "<br>" + speciesSkill.getDescription2()
					+ "&ensp /&ensp " + speciesSkill.getDescription3()
					+ "&ensp /&ensp " + speciesSkill.getDescription4();
			return help;
		}
		@Override protected int dialogWidth()	{ return RotPUI.scaledSize(500); }
		@Override protected int dialogHeight()	{ return RotPUI.scaledSize(450);}
		@Override public void reInit(List<String> list)	 {
			valueLabelMap.clear();
			for (String element : list)
				put(element, Utils.convert(element));
			if (!list.contains(get()))
				set(list.get(0));
		}
	};
	// For Guide, do not add to panels
	class ParamListSpecificAbilities extends ParamList {
		public ParamListSpecificAbilities(String gui, String name, String defaultValue)	{
			super(gui, name, null, defaultValue);
			isDuplicate(true); // To activate get and set optionValue
		}
		@Override public String getOptionValue(IGameOptions opts)	{
			return opts.specificOpponentCROption(Utils.currentIndex());
		}
		@Override public void setOptionValue(IGameOptions opts, String str)	{
			opts.specificOpponentCROption(str, Utils.currentIndex());
			Utils.postSelect(false);
		}
		@Override public String	guideValue()		{ return Utils.convert(get()); }
		@Override public String getRowGuide(int id)	{
			String key		= getLangLabel(id);
			String labelKey	= SpecificCROption.getLabel(key);
			String help		= realLangLabel(labelKey + LABEL_DESCRIPTION);
			if (help != null)
				return rowFormat(labelFormat(realLangLabel(labelKey)), help);

			key  = getGuiValue(id);
			help = realLangLabel(key + LABEL_DESCRIPTION);
			SpeciesSkills speciesSkill = fileToAlienRaceInfo(null, key);
			String raceName	= speciesSkill.setupName;
			if (key.startsWith(BASE_RACE_MARKER))
				help = labelFormat(name(id)) + "<i>(Original species)</i>&nbsp " + speciesSkill.getDescription1();
			else
				help = labelFormat(raceName) + speciesSkill.getDescription1();
			help += "<br>" + speciesSkill.getDescription2()
					+ "&ensp /&ensp " + speciesSkill.getDescription3()
					+ "&ensp /&ensp " + speciesSkill.getDescription4();
			return help;
		}
		@Override protected int dialogWidth()	{ return RotPUI.scaledSize(500); }
		@Override protected int dialogHeight()	{ return RotPUI.scaledSize(450);}
		@Override public void reInit(List<String> list)	{
			valueLabelMap.clear();
			for (String element : list)
				put(element, Utils.convert(element));
			if (!list.contains(get()))
				set(list.get(0));
		}
		public  String	guideValue(int id)	{ return Utils.convert(Utils.options().specificOpponentCROption(id)); }
	};
}
