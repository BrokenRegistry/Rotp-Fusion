package rotp.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

// No out of range error Return "" instead
// Easier to tut and retrieve these list in Maps
public class StringList extends ArrayList<String> {
	private static final long serialVersionUID = 1L;

	public StringList()	{}
	public StringList(int initialCapacity)	{ super(initialCapacity); }
	public StringList(List<String> src)		{ super(src); }

	@Override public String get(int id)	{
		if (id<0 || size() == 0)
			return "";
		if (id>=size())
			return super.get(0);
		return super.get(id % size());
	}
	@Override public String remove(int id)	{ return id<0 || id>=size()? "" : super.remove(id); }

	public String getFirst()	{ return size()==0? "" : super.get(0); }
	public String getLast()		{ return size()==0? "" : super.get(size()-1); }
	public String removeFirst()	{ return size()==0? "" : super.remove(0); }
	public String removeLast()	{ return size()==0? "" : super.remove(size()-1); }

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
	public int indexOfIgnoreCase(String s)	{
		int index = 0;
		for (String entry : this) {
			if (entry.equalsIgnoreCase(s))
				return index;
			index++;
		}
		return -1;
	}
	public boolean isValidIndex(int index)	{ return index >= 0 && index < size(); }
	public void removeNullAndEmpty()		{ removeAll(Arrays.asList("", null)); }
}
