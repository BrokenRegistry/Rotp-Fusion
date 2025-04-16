package rotp.ui.options;

import java.awt.image.BufferedImage;

import rotp.model.game.IModOptions;
import rotp.model.game.SafeListPanel;
import rotp.model.game.SafeListParam;
import rotp.ui.util.ParamSubUI;

public abstract class AbstractOptionsSubUI implements IModOptions {
	private boolean hasExtraParam = false;
	
	public abstract String optionId();
	public abstract SafeListPanel optionsMap();

	// Generally overridden
	SafeListParam	minorList()	{ return new SafeListParam(uiMinorKey()); }
	SafeListParam	majorList()	{ return new SafeListParam(uiMajorKey()); }
	SafeListParam	fullList()	{ return optionsMap().getListNoTitle(); } // Override to add spacers

	// Some time Overridden
	boolean noPanel()			{ return false; }
	boolean isCfgFile()			{ return false; }
	BufferedImage getIcon()		{ return null; }

	// Constructor
	public ParamSubUI getUI()	{
		return new ParamSubUI(
				headId(),
				uiNameKey(),
//				optionsMap(),
				uiTitleKey(),
				optionId()
				).isCfgFile(isCfgFile());
	}

	final boolean hasExtraParam()	{ return hasExtraParam; }

	String headId()					{ return MOD_UI; }
	String uiNameKey()				{ return optionId() + "_UI"; }
	String uiTitleKey()				{ return optionId() + "_TITLE"; }
	final String uiFullKey()		{ return optionId() + "_FULL"; }
	final String uiMajorKey()		{ return optionId() + "_MAJOR"; }
	final String uiMinorKey()		{ return optionId() + "_MINOR"; }
	final String uiCallKey()		{ return optionId() + "_MAJOR2"; }

	final SafeListParam getListNoSpacer()					{ return optionsMap().getListNoSpacer(); }
	public final SafeListParam getUiAll(boolean relevant)	{ return buildList(fullList(), uiFullKey(), relevant); }
	final SafeListParam getUiMajor(boolean relevant)		{ return buildList(majorList(), uiMajorKey(), relevant); }
	final SafeListParam getUiMinor(boolean relevant)		{ return buildList(minorList(), uiMinorKey(), relevant); }
	private SafeListParam buildList (SafeListParam list, String key, boolean relevant) {
		int fullSize = optionsMap().listSizeNoSpacer();
		int listSize = list.sizeNoSpacer();
		hasExtraParam = fullSize >= listSize;
		SafeListParam buildList = new SafeListParam(key);
		if (relevant)
			buildList.add(RELEVANT_TITLE);
		ParamSubUI ui = getUI();
		ui.unseen(fullSize-listSize);
		buildList.add(ui);
		buildList.addAll(list);
		return buildList;
	}
}
