package rotp.ui.util;

import java.util.ArrayList;
import java.util.stream.IntStream;

public class StringList extends ArrayList<String> {
	private static final long serialVersionUID = 1L;
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
}
