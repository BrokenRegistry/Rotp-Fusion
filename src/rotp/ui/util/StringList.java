package rotp.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class StringList extends ArrayList<String> {
	private static final long serialVersionUID = 1L;

	public StringList()	{}
	public StringList(List<String> src)	{ super(src); }

	@Override public String get(int id)	{
		if (id<0 || size() == 0)
			return "";
		if (id>=size())
			return super.get(0);
		return super.get(id % size());
	}
	public String getFirst()	{ return get(0); }
	public String getLast()		{ return get(size()-1); }

	public IntegerList getIndexes(String search, boolean ignoreCase)	{
		if (ignoreCase)
			return new IntegerList(IntStream.range(0, size())
					.filter(i -> get(i).equalsIgnoreCase(search))
					.boxed().toList());
		else
			return new IntegerList(IntStream.range(0, size())
					.filter(i -> get(i).equals(search))
					.boxed().toList());
	}
	public int[] getIndexArray(String search, boolean ignoreCase)	{
		if (ignoreCase)
			return IntStream.range(0, size())
					.filter(i -> get(i).equalsIgnoreCase(search))
					.toArray();
			else
				return IntStream.range(0, size())
						.filter(i -> get(i).equals(search))
						.toArray();
	}
	/**
	 * Get first index matching string, ignoring the case
	 * @param s VString to search for
	 * @return Index, -1 if none
	 */
	int indexOfIgnoreCase(String s)	{
		int index = 0;
		for (String entry : this) {
			if (entry.equalsIgnoreCase(s))
				return index;
			index++;
		}
		return -1;
	}
	boolean isValidIndex(int index)	{ return index >= 0 && index < size(); }
}
