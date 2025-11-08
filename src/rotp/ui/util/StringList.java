package rotp.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

// No out of range error Return "" instead
// Easier to put and retrieve these list in Maps
public class StringList extends ArrayList<String> {
	private static final long serialVersionUID = 1L;
	public static final String REGEX = "\\s*,\\s*";
	public static final String SEP	 = ",";

	private int selectedIndex = -1;

	public StringList()	{}
	public StringList(int initialCapacity)		{ super(initialCapacity); }
	public StringList(List<String> src)			{ super(src); }
	public StringList(StringList src)			{ super(src); selectedIndex = src.selectedIndex; }
	public StringList(String src)				{ this(src, REGEX); }
	public StringList(String src, String regex)	{
		super(Arrays.asList(src.split(regex, -1)));
		if (src.isEmpty())
			clear();
	}
	/**
	 * Replaces the String at the specified position in this list with the specified String.
	 * If the index is greater than the last index, the list will be filled with the specified String.
	 * If the index is negative, nothing will be changed
	 * @param index index of the String to replace
	 * @param str   String to be stored at the specified position
	 * @return the String previously at the specified position (null if none)
	 */
	@Override public String set(int index, String str)	{
		if (index < 0)
			return null;
		if (index < size())
			return super.set(index, str);
		while (size() <= index)
				add (str);
		return null;
	}
	/**
	 * Inserts the specified String at the specified position in this
	 * list. Shifts the String currently at that position (if any) and
	 * any subsequent String to the right (adds one to their indices).
	 * If the index is greater than the last index, the list will be filled with the specified String.
	 * If the index is negative, nothing will be changed
	 *
	 * @param index index at which the specified String is to be inserted
	 * @param str   String to be inserted
	 */
	@Override public void add(int index, String str)	{
		if (index < 0)
			return;
		if (index <= size())
			super.add(index, str);
		else
			while (size() <= index)
				add (str);
	}
	@Override public String get(int index)	{
		if (index<0)
			return "";
		if (size() == 0)
			add("-");
		if (index >= size()) { // extend the list for higher indexes
			while (size() <= index)
				add(super.get(size()-1));
		}
		return super.get(index);
	}
	public String get(int index, String fill)	{
		if (index<0)
			return "";
		if (size() == 0)
			add(fill);
		if (index >= size()) { // extend the list for higher indexes
			while (size() <= index)
				add(super.get(size()-1));
		}
		return super.get(index);
	}
	@Override public String remove(int id)	{ return id<0 || id>=size()? "" : super.remove(id); }
	@Override public String toString()	{ return super.toString() + System.lineSeparator() + selectedIndex + " -> " + getFromSelectedIndex(); }

	public String asString(String sep)	{ return String.join(sep, (List<String>)this); }
	public String asString()			{ return asString(SEP); }

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
	public void resetFrom(Collection<String> s)	{ clear(); addAll(s); }
	public void resetFrom(StringList src)	{ clear(); addAll(src); selectedIndex = src.selectedIndex; }
	public void rotate(int i)				{ Collections.rotate(this, i); }
	public String join(String sep)			{ return String.join(sep, this); }
	public String[] getArray()				{
		String[] array = new String[size()];
		toArray(array);
		return array;
	}
	public int getBestValidIndex(int idx)	{ 
		if (idx < 0)
			idx = 0;
		if (idx >= size())
			idx = size()-1;
		return idx;
	}
	
	// Methods related to Selected Index 
	public int setSelectedIndex(String s)	{ selectedIndex = indexOf(s); return selectedIndex; }
	public int getSelectedIndex()			{ return selectedIndex; }
	public String setSelectedIndex(int idx)	{ selectedIndex = idx; return get(idx); }
	public String getFromSelectedIndex()	{ return get(selectedIndex); }
	public boolean isValidSelectedIndex()	{ return isValidIndex(selectedIndex); }
	public int ValidSelectedIndex()			{ selectedIndex = getBestValidIndex(selectedIndex); return selectedIndex; }
}
