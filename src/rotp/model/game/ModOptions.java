package rotp.model.game;

public class ModOptions implements IModOptions {

	@Override public int id()							{ return 0; }
	@Override public void id(int id)					{ }
	@Override public DynOptions dynOpts()				{ return null; }
	@Override public IGameOptions opts()				{ return null; }
	@Override public IGameOptions copyAllOptions()		{ return null; }
	@Override public void prepareToSave(boolean secure)	{ }
	@Override public void UpdateOptionsTools()			{ }
	@Override public void loadStartupOptions()			{ }
	@Override public void resetAllNonCfgSettingsToDefault()	{ }
	@Override public void saveOptionsToFile(String s)	{ }
	@Override public void saveOptionsToFile(String s, SafeListParam p)	{ }
	@Override public void updateFromFile(String s, SafeListParam p)	{ }
	@Override public void resetPanelSettingsToDefault(SafeListParam p,
								boolean excludeCfg, boolean excludeSubMenu)	{ }
	@Override public void copyAliensAISettings(IGameOptions dest)			{ }
	@Override public void updateAllNonCfgFromFile(String fileName)			{ }
	@Override public float densitySizeFactor()			{ return 0; }
}
