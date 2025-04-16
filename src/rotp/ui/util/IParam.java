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
	default boolean	isTitle()				{ return false; }
	default boolean	isSubMenu()				{ return false; }
	default boolean	isDefaultValue()		{ return false; }
	default int		getUnseen()				{ return 0; }
	/**
	 * To check if the currently set value is still locally valid
	 */
	default boolean	isValidValue()			{ return true; }
	default boolean	isActive()				{ return true; }
	default boolean	updated()				{ return true; }
	default boolean	trueChange()			{ return true;}
	// Display
	default void setFromCfgValue(String val) {}
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
}
