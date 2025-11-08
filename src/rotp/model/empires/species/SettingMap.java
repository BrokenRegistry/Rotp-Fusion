package rotp.model.empires.species;

import java.util.HashMap;

import rotp.ui.util.StringList;

public class SettingMap extends SettingString {

	private StringList keys;
	private HashMap<String, SettingStringList> map = new HashMap<>();

	public SettingMap(String guiLangLabel, String nameLangLabel, String defaultValue, int lineNum) {

		super(guiLangLabel, nameLangLabel, defaultValue, lineNum);
		// TODO Auto-generated constructor stub
	}
	
}
