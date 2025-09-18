package rotp.model.game;

import java.util.ArrayList;

import rotp.ui.util.IParam;
import rotp.ui.util.IParam.ParamSearchList;

public class SafeListPanel extends ArrayList<SafeListParam>{
	private static final long serialVersionUID = 1L;
	public final String name;
	public SafeListPanel(String name) {
		this.name = name;
	}
	@Override public SafeListParam get(int id) {
		if (id<0 || size() == 0)
			return new SafeListParam(name + id);
		if (id>=size())
			return super.get(0);
		return super.get(id);
	}
	public SafeListParam getListNoNull() {
		SafeListParam paramList = new SafeListParam(name);
		for ( SafeListParam list : this )
			paramList.addAll(list.getNoNull());
		return paramList;
	}
	public SafeListParam getListNoTitle() {
		SafeListParam paramList = new SafeListParam(name);
		for ( SafeListParam list : this )
			paramList.addAll(list.getNoTitle());
		return paramList;
	}
	public SafeListParam getListNoSpacer() {
		SafeListParam paramList = new SafeListParam(name);
		for ( SafeListParam list : this )
			paramList.addAll(list.getNoSpacer());
		return paramList;
	}
	public int listSize() {
		int size = 0;
		for ( SafeListParam list : this )
			size += list.sizeNoNull();
		return size;
	}
	public int listSizeNoTitle() {
		int size = 0;
		for ( SafeListParam list : this )
			size += list.sizeNoTitle();
		return size;
	}
	public int listSizeNoSpacer() {
		int size = 0;
		for ( SafeListParam list : this )
			size += list.sizeNoSpacer();
		return size;
	}
	public ParamSearchList getSearchList(IParam ui, String toSearch, int min, boolean stripAccents)	{
		ParamSearchList paramSet = new ParamSearchList();
		for (SafeListParam list : this)
			paramSet.addAll(list.getSearchList(ui, toSearch, min, stripAccents));
		return paramSet;
	}
}
