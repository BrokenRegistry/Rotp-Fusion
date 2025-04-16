package rotp.ui.util;

import java.util.ArrayList;
import java.util.Collection;

public class IntegerList extends ArrayList<Integer> {
	private static final long serialVersionUID = 1L;

	public IntegerList(IntegerList src)			{ addAll(src); }
	public IntegerList(Collection<Integer> c)	{ addAll(c); }
	public IntegerList()	{}
	
	@Override public Integer get(int id)	{
		if (id<0 || isEmpty())
			return null;
		if (id>=size())
			return super.get(id % size());
		return super.get(id);
	}
	public Integer getFirst()			{ return get(0); }
	public Integer getLast()			{ return get(size()-1); }
	public Integer getFirst(int ifNone)	{
		Integer val = get(0);
		return val == null ? ifNone : val;
	}
	public Integer getLast(int ifNone)	{
		Integer val = getLast();
		return val == null ? ifNone : val;
	}
}
