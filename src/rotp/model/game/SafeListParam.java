package rotp.model.game;

import java.util.ArrayList;
import java.util.Collection;

import rotp.ui.util.IParam;
import rotp.ui.util.ParamSpacer;

public class SafeListParam extends ArrayList<IParam> {
	private static final long serialVersionUID = 1L;
	public final String name;
    public SafeListParam(String name) { this.name = name; }
    public SafeListParam(String name, Collection<IParam> c) {
    	this(name);
    	addAll(c);
    }
    public SafeListParam(Collection<IParam> c)	{
    	this("");
    	addAll(c);
    }
    public boolean sameAs(SafeListParam list)	{
    	if (name.isEmpty())
    		return false;
    	if (name.equals(list.name))
    		return size() == list.size();
    	return false;
    }
	@Override public IParam get(int id)			{
		if (id<0 || size() == 0)
			return null;
		if (id>=size())
			return super.get(0);
		return super.get(id);
	}
	@Override public boolean addAll(Collection<? extends IParam> c)	{
		if (c == null)
			return false;
		return super.addAll(c);
	}
	public SafeListParam getNoNull() {
		SafeListParam list = new SafeListParam(name);
		for (IParam param : this)
			if (param != null)
				list.add(param);
		return list;
	}
	SafeListParam getNoTitle() {
		SafeListParam list = new SafeListParam(name);
		for (IParam param : this)
			if (param != null && !param.isTitle())
				list.add(param);
		return list;
	}
	SafeListParam getNoSpacer() {
		SafeListParam list = new SafeListParam(name);
		for (IParam param : this)
			if (param != null && !param.isTitle() && !(param instanceof ParamSpacer))
				list.add(param);
		return list;
	}
	public int sizeNoNull() {
		int size = 0;
		for (IParam param : this)
			if (param != null)
				size++;
		return size;
	}
	public int sizeNoTitle() {
		int size = 0;
		for (IParam param : this)
			if (param != null && !param.isTitle())
				size++;
		return size;
	}
	public int sizeNoSpacer() {
		int size = 0;
		for (IParam param : this)
			if (param != null && !param.isTitle() && !(param instanceof ParamSpacer))
				size++;
		return size;
	}
}
