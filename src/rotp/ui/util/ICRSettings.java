package rotp.ui.util;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import rotp.ui.game.BaseModPanel.ModText;

public interface ICRSettings extends IParam {
//	void guiSelect();
//	void updateGui();
//	void pushSetting();
//	void pullSetting();
//	void clearImage();
//
//	void settingText(ModText txt);
//	void hasNoCost(boolean hasNoCost);
//	void setValueFromCost(float cost);
//	void setRandom(float cost);
//	void optionText(ModText optionText, int i);
//	void setRandom(float min, float max, boolean gaussian);
//	void drawSetting(int sizePad, int endPad, int optionH, int currentdWith,
//			Color frameC, int frameShift, int xLine, int yLine, int settingIndent,
//			int shift, int settingH, int frameTopPad, int wSetting, int optionIndent,
//			boolean retina, float retinaFactor);
//
//	boolean toggle(MouseEvent e, MouseWheelEvent w, int idx);
//	boolean isSpacer();
//	boolean hasNoCost();
//	boolean isBullet();
//
//	int index()	;
//	int bulletStart();
//	int bulletBoxSize();
//	int deltaYLines();
//
//	ModText settingText();
//	ModText optionText(int i);
//	ModText[] optionsText();
//	BufferedImage getImage();
//
//	float costFactor();
//	float settingCost();
//	float lastRandomSource();
//
//	String guiSettingDisplayStr();
//	String guiCostOptionStr(int idx);


	default void guiSelect()	{}
	default void updateGui()	{}
	default void pushSetting()	{}
	default void pullSetting()	{}
	default void clearImage()	{}

	default void settingText(ModText txt)		{}
	default void hasNoCost(boolean hasNoCost)	{}
	default void setValueFromCost(float cost)	{}
	default void setRandom(float cost)			{}
	default void optionText(ModText optionText, int i)	{}
	default void setRandom(float min, float max, boolean gaussian)	{}
	default void drawSetting(int sizePad, int endPad, int optionH, int currentdWith,
			Color frameC, int frameShift, int xLine, int yLine, int settingIndent,
			int shift, int settingH, int frameTopPad, int wSetting, int optionIndent,
			boolean retina, float retinaFactor)	{}

	default boolean toggle(MouseEvent e, MouseWheelEvent w, int idx)	{ return false; }
	default boolean isSpacer()			{ return false; }
	default boolean hasNoCost()			{ return true; }
	default boolean isBullet()			{ return false; }

	default int index()					{ return 0; }
	default int bulletStart()			{ return 0; }
	default int bulletBoxSize()			{ return 0; }
	default int deltaYLines()			{ return 0; }

	default ModText settingText()		{ return null; }
	default ModText optionText(int i)	{ return null; }
	default ModText[] optionsText()		{ return null; }
	default BufferedImage getImage()	{ return null; }

	default float costFactor()			{ return 0f; }
	default float settingCost()			{ return 0f; }
	default float lastRandomSource()	{ return 0f; }

	default String guiSettingDisplayStr()		{ return ""; }
	default String guiCostOptionStr(int idx)	{ return ""; }
}
