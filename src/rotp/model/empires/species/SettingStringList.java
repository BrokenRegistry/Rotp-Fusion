package rotp.model.empires.species;

import rotp.ui.util.StringList;

public class SettingStringList extends SettingString {

	public SettingStringList(String guiLangLabel, String nameLangLabel, String defaultValue, int lineNum) {
		super(guiLangLabel, nameLangLabel, defaultValue, lineNum);
		// TODO Auto-generated constructor stub
	}
	public void set(StringList list)	{ super.set(list.asString()); }
	public StringList get()				{ return new StringList(settingValue()); }
}
