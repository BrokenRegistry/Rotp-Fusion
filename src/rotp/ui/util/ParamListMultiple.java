/*
 * Copyright 2015-2020 Ray Fowler
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rotp.ui.util;

import static rotp.ui.util.IParam.langLabel;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import rotp.model.game.IGameOptions;
import rotp.ui.RotPUI;
import rotp.ui.game.BaseModPanel;
import rotp.util.FontManager;

public class ParamListMultiple extends ParamList {
//	private int currentId		= -1; // For list with non unique values
	private final IntegerList listId	= new IntegerList(); // For list with non unique values
	private boolean identical, sequential, scrambled, simple;

	// ===== Constructors =====
	//
	/**
	 * Initializer for Duplicate
	 * @param gui  The label header
	 * @param name The name
	 * @param list keys for map table
	 * @param defaultIndex index to the default value
	 */
	public ParamListMultiple(String gui, String name, List<String> list, String defaultValue) {
		super(gui, name, defaultValue);
		isDuplicate(false);
		for (String element : list)
			put(element, element); // Temporary; needs to be further initialized
		updateIndexes(defaultValue);
	}
	// ===== Initializers =====
	//
	@Override public ParamListMultiple setDefaultValue(String key, String value) {
		super.setDefaultValue(key, value);
		return this;
	}
	@Override public ParamListMultiple showFullGuide(boolean show)	{ super.showFullGuide(show); return this; }
	@Override public ParamListMultiple refreshLevel(int level)	{ super.refreshLevel(level); return this; }
	@Override public ParamListMultiple forcedRefresh(boolean b)	{ super.forcedRefresh(b); return this; }
	@Override public ParamListMultiple isValueInit(boolean is)	{ super.isValueInit(is)	; return this; }
	@Override public ParamListMultiple isDuplicate(boolean is)	{ super.isDuplicate(is)	; return this; }
	@Override public ParamListMultiple isCfgFile(boolean is)	{ super.isCfgFile(is)	; return this; }
	@Override public void reInit(List<String> list)	{
		super.reInit(list);
		checkValidity(true);
	}
	// ===== Overriders =====
	//
	@Override public String	valueGuide(int id)		{ return values().get(id); }
	@Override public String guideValue()			{ return super.get(); }
	@Override public String getCfgValue(String str)	{ return str; }
	@Override public String	getGuiValue(int id)		{ return values().get(id); }
	@Override public String	getRowGuide(int id)		{ return values().get(id); }
	@Override public String set(String newValue)	{
		super.set(newValue);
		updateIndexes(newValue);
		String str = updateValue();
		return str;
	}
	@Override public String	setFromIndex(int id)	{
		listId.clear();
		listId.add(id);
		return updateValue();
	}
	@Override protected int getIndex(String value)	{
		if (value == null)
			return 0;
		IntegerList ids = values().getIndexes(value, false);
		if (ids.size() == 0)
			return 0;
		return ids.get(0);
	}

	@Override public String get()	{
		String str = "";
		String sep = "";
		for (int id: listId) {
			str += sep + getMapValue(id);
			sep = System.lineSeparator();
		}
//		System.out.println("ParamListMultiple.get() " + str);
		return str;
	}
	@Override public boolean next()	{
		IntegerList oldList = new IntegerList(listId);
		listId.clear();
		int maxSize = listSize();
		if (maxSize == 0) {
			set("");
			return false;
		}

		for (int id : oldList) {
			Integer newId = id+1 % maxSize;
			listId.add(newId);
		}
		String newValue = get();
		set(newValue);
		return false;
	}
	@Override public boolean prev()	{
		IntegerList oldList = new IntegerList(listId);
		listId.clear();
		int maxSize = listSize();
		if (maxSize == 0) {
			set("");
			return false;
		}

		for (int id : oldList) {
			Integer newId = id-1;
			if (newId<0)
				listId.add(maxSize-1);
			else
				listId.add(newId % maxSize);
		}
		String newValue = get();
		set(newValue);
		return false;
	}
	@Override public int getIndex()	{ return listId.isEmpty() ? 0 : listId.get(0); }
	@Override protected String getOptionValue(IGameOptions options) {
		IntegerList ids = (IntegerList) options.dynOpts().getObject(getLangLabel());
		if (ids == null)
			ids = new IntegerList(listId);
		listId.clear();
		listId.addAll(ids);

		String value = options.dynOpts().getString(getLangLabel());
		if (value == null)
			if (formerName() == null)
				value = creationValue();
			else
				value = options.dynOpts().getString(formerName(), creationValue());

		return value;
	}
	@Override protected void setOptionValue(IGameOptions options, String value) {
		options.dynOpts().setString(getLangLabel(), value);
		options.dynOpts().setObject(getLangLabel(), listId);
	}
	@Override protected String currentOption()		{ return get(); }
	@Override protected void setFromList(BaseModPanel frame)	{
		String message	= "<html>" + getGuiDescription() + "</html>";
		String title	= langLabel(getLangLabel(), "");
		initGuiTexts();
		String[] list= valueLabelMap.guiTextList.toArray(new String[listSize()]);
		int boxWidth  = RotPUI.scaledSize(430);
		int boxHeight = RotPUI.scaledSize(360);
		int boxX = boxPosX - RotPUI.scaledSize(80);

		ListDialogUI dialog = RotPUI.instance().listDialog();
		dialog.init(
				frame,	frame.getParent(),	// Frame & Location component
				message, title,				// Message & Title
				list, currentOption(),		// List & Initial choice
				null, false,				// long Dialogue & isVertical
				boxX, boxPosY,				// Position
				boxWidth, boxHeight,		// size
				dialogMonoFont(),			// Font
				frame,						// Preview
				values(),					// Alternate return
				this); 						// Parameter

		int input = dialog.showDialogGetId(refreshLevel, firstId());
		if (input >= 0)
			setFromIndex(input);
	}
	// ===== Public Methods =====
	//
	public String setFromIndex(List<Integer> ids)	{
		listId.clear();
		if (ids != null)
			listId.addAll(ids);
		return updateValue();
	}
	public boolean isIdentical()					{ return identical; }
	public boolean isSequential()					{ return sequential; }
	public boolean isScrambled()					{ return scrambled; }
	public boolean isSimple()						{ return simple; }

	// ===== Private Methods =====
	//
	private Font dialogMonoFont()	{ return FontManager.current().galaxyFont(RotPUI.scaledSize(20)); }
	private int firstId()			{ return listId.getFirst(0); }
	private StringList values()		{ return valueLabelMap.cfgValueList; }
	private String updateValue()	{ return super.set(get()); }
	private boolean checkValidity(boolean fixIndex)	{
		String oldValue = super.get();
		if (oldValue == null)
			oldValue = "";
		String newValue	= get();
		boolean valid	= oldValue.equals(newValue);
		if (valid || !fixIndex)
			return valid;

		// Fix
		updateIndexes(oldValue);
		newValue = get();
		valid	 = oldValue.equals(newValue);
		set(newValue);
		return valid;
	}
	private String makeValid(boolean fixIndex)		{
		String oldValue = super.get();
		if (oldValue == null)
			oldValue = "";
		String newValue	= get();
		if (oldValue.equals(newValue))
			return oldValue;

		// Fix
		if (fixIndex) {
			updateIndexes(oldValue);
			newValue = get();
		}
		return set(newValue);
	}
	private IntegerList getIndexes(String value)	{ return values().getIndexes(value, false); }
	private String[] splitValue(String value)		{
		if (value == null)
			value = "";
		return value.split(System.lineSeparator());
	}
	private void updateIndexes(String newValue)		{
		identical	= false;
		sequential	= false;
		scrambled	= false;
		simple		= true;
		listId.clear();

		String[] vals = splitValue(newValue);
		// single element
		if (vals.length <= 1) {
			IntegerList indices = getIndexes(newValue);
			if (indices.size() <= 1) {
				listId.addAll(indices);
				return;
			}
			listId.add(indices.get(0));
			return;
		}
		simple = false;

		// multiple elements
		// Test for identical
		identical	= true;
		String str0	= vals[0];
		for (int i=1; i<vals.length; i++)
			identical &= str0.equals(vals[i]);
		if (identical) {
			IntegerList idx = getIndexes(str0);
			if (idx.isEmpty()) {
				identical	= false;
				simple		= true;
				return;
			}
			Integer id = idx.get(0);
			for (int i=0; i<vals.length; i++)
				listId.add(id);
			return;
		}
		identical = false;

		// Get possible locations
		List<IntegerList> listList = new ArrayList<>();
		for (int i=0; i<vals.length; i++)
			listList.add(getIndexes(vals[i]));

		// Search for sequence
		boolean sequential = false;
		for (int id0 : listList.get(0)) {
			sequential = true;
			for (int i=1; i<vals.length; i++) {	// Through the strings
				if (!sequential)
					break;
				sequential = false;
				for (int idn : listList.get(i)) {	// through the indexes
					if (idn == id0+i) {
						sequential = true;
						break;
					}
				}
			}
			if (sequential) {
				listId.add(id0);
				for (int i=1; i<vals.length; i++)
					listId.add(id0+i);
				return;
			}
		}

		// scrambled
		scrambled = true;
		for (int i=0; i<vals.length; i++) {
			IntegerList list = listList.get(i);
			if (!list.isEmpty())
				listId.add(list.get(0));
		}
	}
}
