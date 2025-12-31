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

import static rotp.model.game.IDebugOptions.showVIPPanel;
import static rotp.ui.game.BaseModPanel.guideFontSize;
import static rotp.util.Base.NEWLINE;
import static rotp.util.Base.textSubs;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import rotp.Rotp;
import rotp.ui.RotPUI;
import rotp.ui.game.BaseModPanel;
import rotp.util.LabelManager;

public interface IParam extends InterfaceOptions{
	static final String LABEL_DESCRIPTION = "_DESC";
	static final String LABEL_HELP		 = "_HELP";
	static final String LABEL_GOV_LABEL	 = "_LABEL";
	static final String END				 = "   ";
	static final int INIT_DEPENDENCIES	 = 0;
	static final int VALID_DEPENDENCIES	 = 1;

	/**
	 * To be used after starting RotP or loading options
	 * @param level: "0" for list initialization, "1" for value validation
	 */
	default void initDependencies(int level)	{}
	// remote call
	interface IUpdated { public void valueUpdated(String id); }
	default void setUpdatedMethod(IUpdated method)	{}
	default IUpdated getUpdatedMethod()				{ return null; }
	default void setUpdatedId(String id)			{}
	default String getUpdatedId()					{ return null; }
	default void callUpdatedMethod()				{
		IUpdated method = getUpdatedMethod();
		if (method != null && Rotp.initialized())
			method.valueUpdated(getUpdatedId());
	}
	// user input
	default boolean next()						{ return false; } // Return forceUpdate
	default boolean prev()						{ return false; } // Return forceUpdate
	default boolean toggle(MouseWheelEvent e)	{ return false; } // Return forceUpdate
	default boolean toggle(MouseEvent e, BaseModPanel frame)					{ return false; } // Return forceUpdate
	default boolean toggle(MouseEvent e, MouseWheelEvent w, BaseModPanel frame)	{ return false; } // Return forceUpdate
	default boolean toggle(MouseEvent e, String p, BaseModPanel frame)			{ return false; } // Return forceUpdate
	default boolean toggle(MouseEvent e, String p, BaseModPanel pUI, BaseModPanel frame)	{ return false; } // Return forceUpdate
	default void	updated(boolean updated)	{}
	// State
	default boolean	isDuplicate()			{ return false; }
	default boolean	isCfgFile()				{ return false; }
	default boolean	isImage()				{ return false; }
	default boolean	isTitle()				{ return false; }
	default boolean	isSubMenu()				{ return false; }
	default boolean	isDefaultValue()		{ return false; }
	default int		getUnseen()				{ return 0; }
	default void	paint(Graphics2D g, int x, int y, int w, int h)	{ }
	default BufferedImage getImage(int width, int height)	{ return null; }
	/**
	 * To check if the currently set value is still locally valid
	 */
	default boolean	isValidValue()			{ return true; }
	default boolean	isActive()				{ return true; }
	default boolean	isGhost()				{ return false; }
	default boolean	updated()				{ return true; }
	default boolean	trueChange()			{ return true; }
	// Display
	default void setFromCfgValue(String val) {}
	default String	dynOptionIndex()		{ return getLangLabel(); }
	default String	getCfgValue()			{ return ""; }
	default String	getCfgLabel()			{ return ""; }
	default String	getGuiDisplay()			{ return ""; } // Name, value, ... and more
	default String	getGuiDisplay(int id)	{ return ""; }
	default String	getGuiDescription()		{ return ""; }
	default String	guideValue()			{ return ""; } // Only the value, (player view)
	default String	guideSelectedValue()	{ return guideValue(); }
	default String	guideDefaultValue()		{ return ""; }
	default String	guideMinimumValue()		{ return ""; }
	default String	guideMaximumValue()		{ return ""; }
	default String	guideMinMaxHelp()		{ return ""; }
	default int		getIndex()				{ return -1; }
	default String	getToolTip()			{ return getGuiDescription(); }
	default String	getToolTip(int id)		{ return ""; }
	default String	getGuiValue(int id)		{ return guideValue(); }
	default String	getLangLabel()			{ return ""; }
	default String	getLangLabel(int id)	{ return ""; }
	default String[] getModifiers()			{ return null; }
	default float	heightFactor()			{ return 1f; }

	default void drawBox(Graphics2D g, int x0, int y0, int w, int h, int indent, int blankW) {
		int x1 = x0+w;
		g.drawLine(x0, y0, x0+indent, y0);
		g.drawLine(x0+indent+blankW, y0, x1, y0);
		if (h>0) {
			int y1 = y0+h;
			g.drawLine(x0, y0, x0, y1);			
			g.drawLine(x0, y1, x1, y1);			
			g.drawLine(x1, y0, x1, y1);			
		}
	}

	// For Governor ToolTips & labels
	default String govTooltips()			{ return "<html>" + getDescription() + "</html>"; };
	default String govLabelTxt()			{ return langGovLabel(getLangLabel()); };

	// Limited size for toolTip boxes
	default String getDescription()			{
		if (getToolTip().isEmpty())
			return getGuiDescription();
		return getToolTip();
	};
	// Bigger Description for auto pop up help (guide)
	default String getHeadGuide()			{
		String help = headerHelp(true);
		help += defaultValueHelp();
		help += modifierHelp();
		return help;
	}
	default String getGuide(int id)			{ return getHeadGuide() + valueGuide(id); };
	default String getGuide()				{ return getHeadGuide() + selectionGuide(); }
	// Full help for "F1" requests
	default String getFullHelp()			{ return getGuide(); };
	default String getHelp()				{ return getDescription(); };

	// ===== Local Help and guide Tools =====
	default String headerHelp(String label, boolean sep)	{
		String name  = langLabel(label, "");
		String help  = langHelp(label);
		if (help.isEmpty())
			help = "<b><i>" + langLabel("GUIDE_NO_HELP_AVAILABLE") + "</i></b>";
		help = "<u><b>" + name + "</b></u>" + NEWLINE + help;
		if (sep)
			return help + baseSeparator();
		else
			return help;
	}
	default String headerHelp(boolean sep)	{ return headerHelp(getLangLabel(), sep); }
	default String defaultValueHelp()		{
		String help = labelFormat(langLabel("GUIDE_DEFAULT_VALUE_LABEL")) + guideDefaultValue();
		if (!showVIPPanel.get())
			help += htmlTuneFont(-2, "&ensp<i>" + langLabel("GUIDE_SET_WITH_MID_CLICK") + "</i>");
		help += "&emsp" + guideMinMaxHelp();
		help += baseSeparator();
		return help;
	}
	default String minMaxValuesHelp()		{
		String help = labelFormat(langLabel("GUIDE_MINIMUM_VALUE_LABEL")) + guideMinimumValue();
		help += "&emsp" + labelFormat(langLabel("GUIDE_MAXIMUM_VALUE_LABEL")) + guideMaximumValue();
//		help += baseSeparator();
		return help;
	}
	
	// The value in help format
	default String getSelectionStr()		{ return labelFormat(guideSelectedValue()); }
	default String getValueStr(int id)		{
		if (id<0)
			return "";
		 return labelFormat(getGuiValue(id));
	}
	default String getRowGuide(int id)		{
		if (id<0)
			return "";
		String help = realHelp(id);
		if (help == null)
			help = realDescription(id);
		if (help == null)
			help = "";
		return rowFormat(labelFormat(name(id)), help);
	}
	default String valueGuide(int id)		{ return "";}
	default String selectionGuide()			{
		String guideSelectedValue = guideSelectedValue();
		String val  = labelFormat(langLabel("GUIDE_SELECTED_VALUE_LABEL")) + guideSelectedValue;
		if (getIndex() < 0) // not a list
			return val;
		// this is a list
		String help = valueGuide(getIndex());
		return val + baseSeparator() + help;
	}
	default String modifierHelp()			{
		if (showVIPPanel.get())
			return "";
		String[] mod = getModifiers();
		if (mod == null)
			return "";
		String label = langLabel("GUIDE_KEY_MODIFIER_LABEL") + " ";
		String none  = langLabel("GUIDE_KEY_MODIFIER_NONE") + " ";
		String shift = " " + langLabel("GUIDE_KEY_MODIFIER_SHIFT") + " ";
		String ctrl  = " " + langLabel("GUIDE_KEY_MODIFIER_CTRL") + " ";
		String both  = " " + langLabel("GUIDE_KEY_MODIFIER_CTRL_SHIFT") + " ";
		String sep   = " " + langLabel("GUIDE_KEY_MODIFIER_SEPARATOR");
		String help = labelFormat(label) + NEWLINE
					+ none	+ mod[0] + sep
					+ shift	+ mod[1] + sep
					+ ctrl	+ mod[2] + sep
					+ both	+ mod[3]
					+ baseSeparator();
		return help;
	}
	// ===== Upper level language tools =====
	default String name(int id)				{
		if (id<0)
			return "";
		return langName(getLangLabel(id));
	}
	default String realDescription(int id)	{
		if (id<0)
			return "";
		return langDesc(getLangLabel(id));
	}
	default String realHelp(int id)			{
		if (id<0)
			return "";
		return langHelp(getLangLabel(id));
	}
	// ===== Search tools =====
	default IParam getSearchResult()		{ return this; }
	default String rawSearchLabel()			{
		String str = langLabel(getLangLabel(), "", "");
		str = str.replace(":", "").strip();
		return str;
	}
	default String getSearchLabel(boolean stripAccents)	{
		String str = rawSearchLabel();
		if (stripAccents)
			str = StringUtils.stripAccents(str);
		return str.toLowerCase();
	}
	default ParamSearchResult processSearch(ParamSearchList paramSet, IParam ui, String flt, int min, boolean stripAccents) {
		ParamSearchResult psr = new ParamSearchResult(this, ui, flt, min, stripAccents);
		if (psr.isGoodEnough())
			paramSet.add(psr);
		return psr;
	}
	// ===== Formatters =====
	static String tableFormat(String str)	{ return str; }
	static String rowFormat(String... strA)	{
		String row = "";
		for (String str : strA)
			row += str + "&emsp ";
		return row;
	}
	static String htmlTuneFont(int deltaSize, String str)	{
		int newSize = RotPUI.scaledSize(guideFontSize() + deltaSize);
		String head = "<span style=\"font-size:" + newSize + ".0pt\">";
		return head + str + "</span>";
	}
	static String getSeparator(int top, int thick, int down, String color)	{
		String sOpen	= "<div style=\" height: ";
		String sMid		= "px; font-size:0";
		String sClose	= "; \"></div>";
		String sColor	= "; background:#";
		return    sOpen + top	+ sMid + sClose
				+ sOpen + thick + sMid + sColor + color + sClose
				+ sOpen + down	+ sMid + sClose;
	}
	static String baseSeparator()			{ return getSeparator(5, 2, 3, "7f7f7f"); }
	static String rowsSeparator()			{ return getSeparator(4, 1, 2, "9F9F9F"); }
	static String labelFormat(String str)	{
		if (str.isEmpty())
			return str;
		return "<b>" + str + ":</b>&nbsp "; // Make it bold
	}
	// ===== Lower level language tools =====
	static String langName(String key)		{
		if (key == null)
			return "";
		String name = realLangLabel(key);
//		String name = langLabel(key); // TODO BR: For debug... comment! or not
		if (name == null)
			return "";
		return name.split("%1")[0];
	}
	static String langGovLabel(String key)	{
		if (key == null)
			return "";
//		System.out.println("langDesc: key+LABEL_GOV_LABEL = " + key+LABEL_GOV_LABEL);
		String label = realLangLabel(key+LABEL_GOV_LABEL);
//		label = langLabel(key+LABEL_GOV_LABEL); // TO DO BR: For debug... comment!
		if (label == null)
			return "";
//		System.out.println("label = " + label);
		return label;
	}
	static String langDesc(String key)		{
		if (key == null)
			return "";
//		System.out.println("langDesc: key+LABEL_DESCRIPTION = " + key+LABEL_DESCRIPTION);
		String desc = realLangLabel(key+LABEL_DESCRIPTION);
//		String desc = langLabel(key+LABEL_DESCRIPTION); // TODO BR: For debug... comment!
		if (desc == null)
			return "";
//		System.out.println("desc = " + desc);
		return desc;
	}
	static String langHelp(String key)		{
		if (key == null)
			return "";
//		System.out.println("langHelp: key+LABEL_HELP = " + key+LABEL_HELP);
		String help = realLangLabel(key+LABEL_HELP);
//		String help = langLabel(key+LABEL_HELP); // TO DO BR: For debug... comment!
		if (help == null)
			return langDesc(key);
//		System.out.println("help = " + help);
		return help;
	}
	static String langLabel(String key)		{
		if (key == null)
			return "";
		return LabelManager.current().label(key);
	}
	static String langLabel(String key, String... vals) {
		if (key == null)
			return "";
		String str = langLabel(key);
		for (int i=0;i<vals.length;i++)
			str = str.replace(textSubs[i], vals[i]);
		return str;
	}
	static String realLangLabel(String key) {
		if (key == null)
			return "";
		return LabelManager.current().realLabel(key);
	}

	class ParamSearchList extends ArrayList<ParamSearchResult>	{
		private static final long serialVersionUID = 1L;
		private static final ResultComparator resultComparator = new ResultComparator();
		private static final RatioComparator ratioComparator = new RatioComparator();
		private static final PartialRatioComparator partialRatioComparator = new PartialRatioComparator();
		public void sort()	{
			sort(resultComparator);
			sort(ratioComparator);
			sort(partialRatioComparator);
		}
		public void smartAdd(ParamSearchResult psr)	{
			ParamSearchResult containsSameParam = containsSameParam(psr.param);
			if (containsSameParam == null) {
				add(0, psr);
				return;
			}
			if (containsSameParam.ui == null) {
				remove(containsSameParam);
				add(0, psr);
			}
		}
		private ParamSearchResult containsSameParam(IParam p)	{
			for (ParamSearchResult psr : this)
				if (psr.param.equals(p))
					return psr;
			return null;
		}
		public String toString(String lineSep, boolean reverse)	{
			List<String> list = new ArrayList<>();
			if (reverse)
				for (ParamSearchResult p : this)
					list.add(0, p.toString());
			else
				for (ParamSearchResult p : this)
					list.add(p.toString());
			return String.join(lineSep, list);
		}
		public ParamSearchResult getFirst()	{ return isEmpty()? null : get(0); }
		public ParamSearchResult getLast()	{ return isEmpty()? null : get(size()-1); }
		@Override public String toString()	{ return toString(", ", false); }
	}
	class ResultComparator implements Comparator<ParamSearchResult>	{
		@Override public int compare(ParamSearchResult p1, ParamSearchResult p2)	{
			return Integer.compare(p2.result, p1.result);
		}
	}
	class RatioComparator implements Comparator<ParamSearchResult>	{
		@Override public int compare(ParamSearchResult p1, ParamSearchResult p2)	{
			return Integer.compare(p2.ratio, p1.ratio);
		}
	}
	class PartialRatioComparator implements Comparator<ParamSearchResult>	{
		@Override public int compare(ParamSearchResult p1, ParamSearchResult p2)	{
			return Integer.compare(p2.partialRatio, p1.partialRatio);
		}
	}
	class ParamSearchResult {
		public static final String COL_SEP = " |-> ";
		public final IParam param;
		public final IParam ui;
		private int ratio;
		private int partialRatio;
		private int min;
		int result;

		ParamSearchResult(IParam param, IParam ui, String flt, int min, boolean stripAccents)	{
			this.param	= param;
			this.ui		= ui;
			this.min	= min;
			String label = param.getSearchLabel(stripAccents);
			ratio = FuzzySearch.ratio(flt, label);
			if (ratio < 100)
				partialRatio = FuzzySearch.partialRatio(flt, label);
			else
				partialRatio = ratio;
			result = ratio + partialRatio;
		}
		public boolean isGoodEnough()			{ return result >= min; }
		
		@Override public String toString()		{
			return format(ui) + COL_SEP + format(param);
//			return format(ui) + COL_SEP + format(param) + " (" + result + "/" + partialRatio + "/" + ratio + ")";
		}
		private String subPanelFormat(String s)	{ return "[" + s.strip() + "]"; }
		private String format(IParam p)			{
			if (p == null)
				return subPanelFormat(langLabel("SETTINGS_MOD_SEARCH_RESULT_THIS_PANEL"));
			if (p.isSubMenu())
				return subPanelFormat(p.getGuiDisplay());
			return p.getGuiDisplay();
		}
	}
}
