/*
 * Copyright 2015-2020 Ray Fowler
 * 
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     https://www.gnu.org/licenses/gpl-3.0.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rotp.ui.game;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

import rotp.Rotp;
import rotp.model.game.GameSession;
import rotp.model.game.IDebugOptions;
import rotp.model.game.IGameOptions;
import rotp.ui.BasePanel;
import rotp.ui.BaseText;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.sprites.RoundGradientPaint;
import rotp.util.FontManager;
import rotp.util.ImageManager;
import rotp.util.LanguageManager;
import rotp.util.ModifierKeysState;
import rotp.util.ThickBevelBorder;

public class GameUI  extends BasePanel implements MouseListener, MouseMotionListener, ActionListener {
    private static final long serialVersionUID = 1L;
	public static String AMBIENCE_KEY = "IntroAmbience";
    protected static RoundGradientPaint rgp;

    public static final int BG_DURATION = 80;
    public static final float SLIDESHOW_MAX = 15f;
    
    public  static String gameName = "";
    
    private static final Color langShade[] = { new Color(0,0,0,128), new Color(128,0,0,96) };
    // private static final Color menuHover[] = {  new Color(255,220,181), new Color(255,255,210) };
    private static final Color menuDepressed[] = { new Color(156,96,77), new Color(110,110,110) };
    private static final Color menuEnabled[] = { new Color(255,203,133), new Color(197,197,197) };
    private static final Color menuDisabled[] = { new Color(156,96,77), new Color(110,110,110) };
    private static final Color menuShade[] = { new Color(16,10,8,3), new Color(0,0,0,6) };

    private static final Color logoFore[] = { new Color(255,220,181), new Color(240,240,240) };
    private static final Color logoShade[] = { new Color(65,30,24,2), new Color(0,0,0,3) };
    private static final Color setupShade[] = { new Color(65,30,24,128), new Color(40,40,40,128) };
    private static final Color setupFrame[] = { new Color(254,204,153), new Color(195,205,205) };

    private static final Color[] titleColor = { new Color(255,220,181), new Color(240,240,240)  };
    private static final Color[] titleShade = { new Color(25,25,25), new Color(25,25,25) };
    private static final Color[] labelColor = { new Color(79,52,33), new Color(39,44,44) };
    private static final Color[] raceEdgeColor = { new Color(114,75,49), new Color(44,48,47) };
    private static final Color[] raceCenterColor = { new Color(179,117,77), new Color(86,96,95) };
    private static final Color[] borderBrightColor = { new Color(254,204,153), new Color(172,181,181) };
    private static final Color[] borderMidColor = { new Color(179,116,73), new Color(106,121,121) };
    private static final Color[] borderDarkColor = { new Color(79,52,33),  new Color(39,44,44) };
    private static final Color[] paneBackgroundColor = { new Color(240,182,132), new Color(172,181,181) };
    private static final Color[] saveGameBackgroundColor = { new Color(26,17,17), new Color(26,17,17) };
    private static final Color[] buttonBackgroundColor = { new Color(93,61,40), new Color(53,60,60) };
    private static final Color[] buttonTextColor = { new Color(240,240,240), new Color(196,196,196) };
    private static final Color[] disabledTextColor = { new Color(160,160,150), new Color(128,128,128) };
    private static final Color[] textColor = { new Color(246,197,130), new Color(196,196,196) };
    private static final Color[] textHoverColor = { new Color(250,247,140), new Color(253,219,180) };
    private static final Color[] textSelectedColor = { new Color(153,196,153), new Color(151,136,205) };
    private static final Color[] textShade = { new Color(25,25,25), new Color(25,25,25) };
    private static final Color[] loadHiBackgroundColor = { new Color(188,123,81), new Color(91,101,100) };
    private static final Color[] loadHoverBackgroundColor = { new Color(219,167,122), new Color(160,172,170) };
    private static final Color[] loadListMask = { new Color(0,0,0,120), new Color(0,0,0,120) };
    private static final Color[] sortLabelBackColor = { new Color(100,70,50), new Color(59,66,65) };
    private static LinearGradientPaint[] loadBackground;
    private static LinearGradientPaint[] raceLeftBackground;
    private static LinearGradientPaint[] raceRightBackground;
    private static LinearGradientPaint[] buttonLeftBackground;
    private static LinearGradientPaint[] buttonRightBackground;
    private static LinearGradientPaint[] opponentsSetupBackground;
    private static LinearGradientPaint[] galaxySetupBackground;
    private static LinearGradientPaint[] settingsSetupBackground;

    private static Border setupBorder;
    private static Border buttonBorder;
    private static Border saveGameBorder;
    private static Border saveListBorder;
    private static int colorSet = 0;
    
    int fuzz = 8;
    int fuzzSc = 2;
    int diff = s60;
    int languageX;
    BaseText discussText, continueText, newGameText, loadGameText, saveGameText, settingsText, exitText, restartText;
    BaseText versionText, manualText;
    BaseText developerText, artistText, graphicDsnrText, writerText, soundText, translatorText, slideshowText;
    BaseText shrinkText, enlargeText, winSizeText;
	BaseText lastVersionText, newVersionText, lastVerJarText, lastVerMiniText, lastVerExeText;
    BaseText hoverBox;
    Rectangle languageBox = new Rectangle();
    boolean mouseDepressed = false;
    boolean hideText = false;
    int startingScale = 100;
    int startingScreen = -1;
    String startingDisplayMode;
    public static Image defaultBackground;
    Image backImg1, backImg2;
    BufferedImage titleImg;
    BufferedImage backImg;
    String imageKey1, imageKey2;
    int animationTimer = BG_DURATION;
    private final GameLanguagePane languagePanel;
    float slideshowFade = SLIDESHOW_MAX;
    private boolean showLastUpdatedLink = false;
    private boolean showNewUpdatedLink  = false;
    private boolean checkedForUpdate    = false;
    private long currentVersion;
    private GitInfo	gitInfoBR, gitInfoXilmi, gitInfoLast, gitInfoNew;

    public static void  colorSet(int set)		  { colorSet = set; }
    public static Color langShade()               { return langShade[opt()]; }
    public static Color titleColor()              { return titleColor[opt()]; }
    public static Color titleShade()              { return titleShade[opt()]; }
    public static Color setupShade()              { return setupShade[opt()]; }
    public static Color setupFrame()              { return setupFrame[opt()]; }
    public static Color labelColor()              { return labelColor[opt()]; }
    public static Color paneBackgroundColor()     { return paneBackgroundColor[opt()]; }
    public static Color buttonTextColor()         { return buttonTextColor[opt()]; }
    public static Color disabledTextColor()       { return disabledTextColor[opt()]; }
    public static Color textColor()               { return textColor[opt()]; }
    public static Color textHoverColor()          { return textHoverColor[opt()]; }
    public static Color textSelectedColor()       { return textSelectedColor[opt()]; }
    public static Color textShade()               { return textShade[opt()]; }
    public static Color saveGameBackgroundColor() { return saveGameBackgroundColor[opt()]; }
    public static Color raceEdgeColor()           { return raceEdgeColor[opt()]; }
    public static Color raceCenterColor()         { return raceCenterColor[opt()]; }
    public static Color buttonBackgroundColor()   { return buttonBackgroundColor[opt()]; }
    public static Color borderBrightColor()       { return borderBrightColor[opt()]; }
    public static Color borderMidColor()          { return borderMidColor[opt()]; }
    public static Color borderDarkColor()         { return borderDarkColor[opt()]; }
    public static Color loadHiBackground()        { return loadHiBackgroundColor[opt()]; }
    public static Color loadHoverBackground()     { return loadHoverBackgroundColor[opt()]; }
    public static Color loadListMask()            { return loadListMask[opt()]; }
    public static Color sortLabelBackColor()      { return sortLabelBackColor[opt()]; }

    public static LinearGradientPaint modBackground(float x0, float x1) {
          Point2D start = new Point2D.Float(x0, 0); // BR adjustable for 10 or 16 species
          Point2D end = new Point2D.Float(x1, 0);
          float[] dist = {0.0f, 0.1f, 0.9f, 1.0f};
          Color mid0 = raceCenterColor();
//          Color edge0 = opt()==0? new Color(147,96, 61) : new Color(65,72,71);
          Color edge0 = raceEdgeColor();
//          Color mid0 = raceCenterColor();
          Color[] colors0 = {edge0, mid0,  mid0, edge0 };
          return new LinearGradientPaint(start, end, dist, colors0, CycleMethod.REPEAT);
    }
    public static LinearGradientPaint buttonBackground(int x0, int x1) {
        Point2D start = new Point2D.Float(x0, 0); // BR adjustable for 10 or 16 species
        Point2D end = new Point2D.Float(x1, 0);
        float[] dist = {0.0f, 0.5f, 0.51f, 1.0f};
        Color edge0 = raceEdgeColor();
        Color mid0 = loadHiBackground();
        Color[] colors0 = {edge0, mid0,  mid0, edge0 };
        return new LinearGradientPaint(start, end, dist, colors0);
  }
    public static LinearGradientPaint loadBackground() {
        if (loadBackground == null) {
            loadBackground = new LinearGradientPaint[2];
            Point2D start = new Point2D.Float(RotPUI.scaledSize(350), 0);
            Point2D end = new Point2D.Float(RotPUI.scaledSize(879), 0);
            float[] dist = {0.0f, 0.1f, 0.9f, 1.0f};
            Color edge0 = new Color(113,74,49);
            Color mid0 = new Color(188,123,81);
            Color[] colors0 = {edge0, mid0,  mid0, edge0 };
            loadBackground[0] = new LinearGradientPaint(start, end, dist, colors0);
            Color edge1 = new Color(41,44,43);
            Color mid1 = new Color(88,97,96);
            Color[] colors1 = {edge1, mid1, mid1, edge1 };
            loadBackground[1] = new LinearGradientPaint(start, end, dist, colors1);
        }
        return loadBackground[opt()];
    }
    public static LinearGradientPaint buttonLeftBackground() {
        if (buttonLeftBackground == null) {
            buttonLeftBackground = new LinearGradientPaint[2];
            Point2D start = new Point2D.Float(RotPUI.scaledSize(710), 0);
            Point2D end = new Point2D.Float(RotPUI.scaledSize(930), 0);
            float[] dist = {0.0f, 0.5f, 0.51f, 1.0f};
            Color edge0 = new Color(113,74,49);
            Color mid0 = new Color(188,123,81);
            Color[] colors0 = {edge0, mid0,  mid0, edge0 };
            buttonLeftBackground[0] = new LinearGradientPaint(start, end, dist, colors0);
            Color edge1 = new Color(41,44,43);
            Color mid1 = new Color(88,97,96);
            Color[] colors1 = {edge1, mid1, mid1, edge1 };
            buttonLeftBackground[1] = new LinearGradientPaint(start, end, dist, colors1);
        }
        return buttonLeftBackground[opt()];
    }
    public static LinearGradientPaint buttonRightBackground() {
        if (buttonRightBackground == null) {
            buttonRightBackground = new LinearGradientPaint[2];
            Point2D start = new Point2D.Float(RotPUI.scaledSize(950), 0);
            Point2D end = new Point2D.Float(RotPUI.scaledSize(1170), 0);
            float[] dist = {0.0f, 0.3f, 0.65f, 1.0f};
//            float[] dist = {0.0f, 0.3f, 0.7f, 1.0f};
            Color edge0 = new Color(113,74,49);
            Color mid0 = new Color(188,123,81);
            Color[] colors0 = {edge0, mid0,  mid0, edge0 };
            buttonRightBackground[0] = new LinearGradientPaint(start, end, dist, colors0);
            Color edge1 = new Color(41,44,43);
            Color mid1 = new Color(88,97,96);
            Color[] colors1 = {edge1, mid1, mid1, edge1 };
            buttonRightBackground[1] = new LinearGradientPaint(start, end, dist, colors1);
        }
        return buttonRightBackground[opt()];
    }
    public static LinearGradientPaint raceLeftBackground() {
        if (raceLeftBackground == null) {
            raceLeftBackground = new LinearGradientPaint[2];
            Point2D start = new Point2D.Float(RotPUI.scaledSize(150), 0); // modnar: extend left side gradient
            Point2D end = new Point2D.Float(RotPUI.scaledSize(420), 0);
            float[] dist = {0.0f, 0.2f, 0.8f, 1.0f}; // modnar: adjust left side gradient
            Color edge0 = new Color(113,74,49);
            Color mid0 = new Color(188,123,81);
            Color[] colors0 = {edge0, mid0,  mid0, edge0 };
            raceLeftBackground[0] = new LinearGradientPaint(start, end, dist, colors0);
            Color edge1 = new Color(51,56,55);
            Color mid1 = new Color(100,111,110);
            Color[] colors1 = {edge1, mid1, mid1, edge1 };
            raceLeftBackground[1] = new LinearGradientPaint(start, end, dist, colors1);
        }
        return raceLeftBackground[opt()];
    }
    public static LinearGradientPaint raceRightBackground(int x) {
//        if (raceRightBackground == null) {
            raceRightBackground = new LinearGradientPaint[2];
//            Point2D start = new Point2D.Float(RotPUI.scaledSize(815), 0);
//            Point2D end = new Point2D.Float(RotPUI.scaledSize(1040), 0); // modnar: extend right side gradient
            Point2D start = new Point2D.Float(x, 0); // BR adjustable for 10 or 16 species
            Point2D end = new Point2D.Float(x + RotPUI.scaledSize(225), 0);
            float[] dist = {0.0f, 0.2f, 0.8f, 1.0f}; // modnar: adjust right side gradient
            Color edge0 = new Color(113,74,49);
            Color mid0 = new Color(188,123,81);
            Color[] colors0 = {edge0, mid0,  mid0, edge0 };
            raceRightBackground[0] = new LinearGradientPaint(start, end, dist, colors0);
            Color edge1 = new Color(51,56,55);
            Color mid1 = new Color(100,111,110);
            Color[] colors1 = {edge1, mid1, mid1, edge1 };
            raceRightBackground[1] = new LinearGradientPaint(start, end, dist, colors1);
//        }
        return raceRightBackground[opt()];
    }
    public static LinearGradientPaint galaxySetupBackground() {
        if (galaxySetupBackground == null) {
            galaxySetupBackground = new LinearGradientPaint[2];
            Point2D start = new Point2D.Float(RotPUI.scaledSize(685), 0);
            Point2D end = new Point2D.Float(RotPUI.scaledSize(1150), 0);
            float[] dist = {0.0f, 0.3f, 0.7f, 1.0f};
            Color edge0 = new Color(113,74,49);
            Color mid0 = new Color(188,123,81);
            Color[] colors0 = {edge0, mid0,  mid0, edge0 };
            galaxySetupBackground[0] = new LinearGradientPaint(start, end, dist, colors0);
            Color edge1 = new Color(51,56,55);
            Color mid1 = new Color(100,111,110);
            Color[] colors1 = {edge1, mid1, mid1, edge1 };
            galaxySetupBackground[1] = new LinearGradientPaint(start, end, dist, colors1);
        }
        return galaxySetupBackground[opt()];
    }
    public static LinearGradientPaint settingsSetupBackground(int w) {
        if (settingsSetupBackground == null) {
            settingsSetupBackground = new LinearGradientPaint[2];
            Point2D start = new Point2D.Float(BasePanel.s100, 0);
            Point2D end = new Point2D.Float(w-BasePanel.s100, 0);
            float[] dist = {0.0f, 0.05f, 0.95f, 1.0f};
            Color edge0 = new Color(113,74,49);
            Color mid0 = new Color(188,123,81);
            Color[] colors0 = {edge0, mid0,  mid0, edge0 };
            settingsSetupBackground[0] = new LinearGradientPaint(start, end, dist, colors0);
            Color edge1 = new Color(51,56,55);
            Color mid1 = new Color(100,111,110);
            Color[] colors1 = {edge1, mid1, mid1, edge1 };
            settingsSetupBackground[1] = new LinearGradientPaint(start, end, dist, colors1);
        }
        return settingsSetupBackground[opt()];
    }
    public static LinearGradientPaint settingsSetupBackgroundW(int w, int wBg) {
    	float edge = (w-wBg)/2.0f + BasePanel.s50;
    	float eR = (float)BasePanel.s100/wBg;
        Point2D start = new Point2D.Float(edge, 0);
        Point2D end = new Point2D.Float(w-edge, 0);
        float[] dist = {0.0f, eR, 1f-eR, 1.0f};
        if (opt() == 0) {
        	Color edge0 = new Color(113,74,49);
            Color mid0 = new Color(188,123,81);
        	Color[] colors0 = {edge0, mid0,  mid0, edge0 };
        	return new LinearGradientPaint(start, end, dist, colors0);
        } else {
        	Color edge1 = new Color(51,56,55);
            Color mid1 = new Color(100,111,110);
        	Color[] colors1 = {edge1, mid1, mid1, edge1 };
        	return new LinearGradientPaint(start, end, dist, colors1);
        }
    }
    public static LinearGradientPaint opponentsSetupBackground() {
        if (opponentsSetupBackground == null) {
            opponentsSetupBackground = new LinearGradientPaint[2];
            Point2D start = new Point2D.Float(RotPUI.scaledSize(80), 0);
            Point2D end = new Point2D.Float(RotPUI.scaledSize(585), 0);
            float[] dist = {0.0f, 0.3f, 0.7f, 1.0f};
            Color edge0 = new Color(113,74,49);
            Color mid0 = new Color(188,123,81);
            Color[] colors0 = {edge0, mid0,  mid0, edge0 };
            opponentsSetupBackground[0] = new LinearGradientPaint(start, end, dist, colors0);
            Color edge1 = new Color(51,56,55);
            Color mid1 = new Color(100,111,110);
            Color[] colors1 = {edge1, mid1, mid1, edge1 };
            opponentsSetupBackground[1] = new LinearGradientPaint(start, end, dist, colors1);
        }
        return opponentsSetupBackground[opt()];
    }
    public static Border setupBorder() {
        if (setupBorder == null)
            setupBorder = new ThickBevelBorder(12, 1, borderBrightColor(), borderMidColor(), borderDarkColor());
        return setupBorder;
    }
    public static Border saveGameBorder() {
        if (saveGameBorder == null)
            saveGameBorder = new ThickBevelBorder(6, 1, borderBrightColor(), borderMidColor(), borderDarkColor());
        return saveGameBorder;
    }
    public static Border saveListBorder() {
        if (saveListBorder == null)
            saveListBorder = new ThickBevelBorder(12, 1, borderBrightColor(), borderMidColor(), borderDarkColor());
        return saveListBorder;
    }
    public static Border buttonBorder() {
        if (buttonBorder == null)
            buttonBorder = new ThickBevelBorder(6, 1, borderBrightColor(), borderMidColor(), borderDarkColor());
        return buttonBorder;
    }

    // private static int opt = -1;
    private static final String[] backImgKeys = { 
        "LANDSCAPE_RUINS_ORION", "LANDSCAPE_RUINS_ANTARAN", 
        "AlkCouncil", "AlkWin", "AlkLoss", "AlkSab01", "AlkSab02",
        "BulCouncil", "BulWin", "BulLoss", "BulSab01", "BulSab02", 
        "DarCouncil01", "DarWin", "DarLoss", "DarSab01", "DarSab02",
        "HumCouncil", "HumWin", "HumLoss",  "HumSab01", "HumSab02", 
        "KlaCouncil", "KlaWin", "KlaLoss", "KlaSab01", "KlaSab02",
        "MekCouncil", "MekWin", "MekLoss", "MekSab01", "MekSab02", 
        "MrrCouncil", "MrrWin", "MrrLoss", "MrrSab01", "MrrSab02",
        "PsiCouncil", "PsiWin", "PsiLoss", "PsiSab01", "PsiSab02",
        "SakCouncil", "SakWin", "SakLoss", "SakSab01", "SakSab02",
        "SilCouncil", "SilWin", "SilLoss", "SilSab01", "SilSab02",
         };
    private Image background() { return backImg; }
    public static int opt()   { return colorSet; }

    public GameUI() {
        startingScale = UserPreferences.screenSizePct();
        startingScreen = UserPreferences.selectedScreen();
        startingDisplayMode = UserPreferences.displayMode();
        languagePanel = new GameLanguagePane(this);
        imageKey1 = backImgKeys[0];
        imageKey2 = random(backImgKeys);
        while (imageKey1.equals(imageKey2))
            imageKey2 = random(backImgKeys);
        Color enabledC   = menuEnabled[0];
        Color disabledC  = menuDisabled[0];
        Color hoverC     = logoFore[1];
        Color depressedC = menuDepressed[1];
        Color shadedC    = menuShade[1];
        // int w = getWidth();
        shrinkText      = new BaseText(this, false,20,   10,24,  enabledC, disabledC, hoverC, depressedC, shadedC, 0, 0, 0);
        enlargeText     = new BaseText(this, false,20,    0,24,  enabledC, disabledC, hoverC, depressedC, shadedC, 0, 0, 0);
        enlargeText.preceder(shrinkText);
        winSizeText        = new BaseText(this, false,20,    0,24,  enabledC, disabledC, hoverC, depressedC, shadedC, 0, 0, 0);
        winSizeText.preceder(enlargeText);
        continueText    = new BaseText(this, true, 45,   0, 340,  enabledC, disabledC, hoverC, depressedC, shadedC, 1, 1, 8);
        newGameText     = new BaseText(this, true, 45,   0, 385,  enabledC, disabledC, hoverC, depressedC, shadedC, 1, 1, 8);
        loadGameText    = new BaseText(this, true, 45,   0, 430,  enabledC, disabledC, hoverC, depressedC, shadedC, 1, 1, 8);
        saveGameText    = new BaseText(this, true, 45,   0, 475,  enabledC, disabledC, hoverC, depressedC, shadedC, 1, 1, 8);
        settingsText    = new BaseText(this, true, 45,   0, 520,  enabledC, disabledC, hoverC, depressedC, shadedC, 1, 1, 8);
        manualText      = new BaseText(this, true, 45,   0, 565,  enabledC, disabledC, hoverC, depressedC, shadedC, 1, 1, 8);
        exitText        = new BaseText(this, true, 45,   0, 610,  enabledC, disabledC, hoverC, depressedC, shadedC, 1, 1, 8);
        restartText     = new BaseText(this, true, 45,   0, 430,  enabledC, disabledC, hoverC, depressedC, shadedC, 1, 1, 8);
        versionText     = new BaseText(this, false,16,   5, -35,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 0, 1);
        discussText     = new BaseText(this, false,22,   5, -10,  enabledC, disabledC, hoverC, depressedC, Color.black, 1, 1, 1);
        developerText   = new BaseText(this, false,16, -210,-95,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 1, 1);
        artistText      = new BaseText(this, false,16, -210,-78,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 1, 1);
        graphicDsnrText = new BaseText(this, false,16, -210,-61,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 1, 1);
        writerText      = new BaseText(this, false,16, -210,-44,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 1, 1);
        soundText       = new BaseText(this, false,16, -210,-27,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 1, 1);
        translatorText  = new BaseText(this, false,16, -210,-10,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 1, 1);
        slideshowText   = new BaseText(this, false,16, -210,-10,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 1, 1);
        lastVersionText = new BaseText(this, false,15,   5, -75,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 0, 1);
        newVersionText  = new BaseText(this, false,15,   5, -55,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 0, 1);
		lastVerJarText	= new BaseText(this, false,15,   5, -95,  enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 0, 1);
		lastVerMiniText	= new BaseText(this, false,15,   5, -115, enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 0, 1);
		lastVerExeText	= new BaseText(this, false,15,   5, -135, enabledC,  enabledC, hoverC, depressedC, Color.black, 1, 0, 1);

        developerText.disabled(true);
        artistText.disabled(true);
        graphicDsnrText.disabled(true);
        writerText.disabled(true);
        soundText.disabled(true);
        translatorText.disabled(true);
        slideshowText.disabled(true);
        versionText.disabled(false);
        newVersionText.disabled(false);
		lastVersionText.disabled(false);
		lastVerJarText.disabled(false);
		lastVerMiniText.disabled(false);
        lastVerExeText.disabled(false);
        developerText.bordered(true);
        artistText.bordered(true);
        graphicDsnrText.bordered(true);
        writerText.bordered(true);
        soundText.bordered(true);
        translatorText.bordered(true);
        slideshowText.bordered(true);
        versionText.bordered(true);
        lastVersionText.bordered(true);
		lastVerJarText.bordered(true);
		lastVerMiniText.bordered(true);
		lastVerExeText.bordered(true);
        newVersionText.bordered(true);
        discussText.bordered(true);
        shrinkText.bordered(true);
        enlargeText.bordered(true);
        winSizeText.bordered(true);
        setTextValues();
        initModel();
    }
    private void initModel() {
        setOpaque(false);
        addMouseListener(this);
        addMouseMotionListener(this);
        languagePanel.setVisible(false);
        add(languagePanel);
    }
    private void setTextValues() {
        discussText.displayText(text("GAME_DISCUSS_ONLINE"));
        // continueText.displayText(text("GAME_MENU_CONTINUE"));
        newGameText.displayText(text("GAME_MENU_NEW_GAME"));
        // loadGameText.displayText(text("GAME_MENU_LOAD_GAME"));
        // saveGameText.displayText(text("GAME_MENU_SAVE_GAME"));
        settingsText.displayText(text("GAME_MENU_SETTINGS"));
        manualText.displayText(text("GAME_MENU_OPEN_MANUAL"));
        exitText.displayText(text("GAME_MENU_EXIT"));
        restartText.displayText(text("GAME_MENU_RESTART"));

        shrinkText.displayText(text("GAME_SHRINK"));
        enlargeText.displayText(text("GAME_ENLARGE"));
        developerText.displayText(text("CREDITS_DEVELOPER"));
        artistText.displayText(text("CREDITS_ILLUSTRATOR"));
        graphicDsnrText.displayText(text("CREDITS_GRAPHIC_DESIGN"));
        writerText.displayText(text("CREDITS_WRITER"));
        soundText.displayText(text("CREDITS_SOUND"));
        translatorText.displayText(text("CREDITS_TRANSLATOR"));
        slideshowText.displayText(text("CREDITS_ILLUSTRATOR"));
        versionText.displayText(text("GAME_VERSION", str(Rotp.releaseId)));

        updateTextValues();
    }
	private void updateTextValues() {
		switch (ModifierKeysState.get()) {
		case CTRL:
		case CTRL_SHIFT:
	        continueText.displayText(text("GAME_MENU_REPLAY_LAST_TURN"));
	        loadGameText.displayText(text("GAME_MENU_LOAD_OPTIONS"));
	        saveGameText.displayText(text("GAME_MENU_SAVE_OPTIONS"));
	        break;
		default:
	        continueText.displayText(text("GAME_MENU_CONTINUE"));
	        loadGameText.displayText(text("GAME_MENU_LOAD_GAME"));
	        saveGameText.displayText(text("GAME_MENU_SAVE_GAME"));
		}
	}

    public void init() {
        slideshowFade = SLIDESHOW_MAX;
        rotp.ui.main.GovernorOptionsPanel.close();
		if (session().options() != null)
			rulesetManager().updateOptionsFromGame();      
        resetSlideshowTimer();
		ModifierKeysState.reset();
    }
    @Override
    public void animate() {
        if (glassPane() != null)
            return;
        
        animationTimer--;
        slideshowFade -=.1f;
        if (animationTimer <= 0) {
            imageKey1 = imageKey2;
            imageKey2 = random(backImgKeys);
            backImg1 = backImg2;
            log("getting image: "+imageKey2);
            backImg2 = ImageManager.current().image(imageKey2);
            backImg = newOpaqueImage(backImg1);
            animationTimer = BG_DURATION;
            repaint();
        }
        else if (animationTimer < 20) {
            float pct = bounds(0.0f, (20-animationTimer)/20f, 1.0f);
            BufferedImage img = newOpaqueImage(backImg1);
            Graphics2D imgG = (Graphics2D) img.getGraphics();
            AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pct);
            imgG.setComposite(composite);
            imgG.drawImage(backImg2, 0,0,null);
            imgG.dispose();
            backImg = img;
            repaint();
        }
        else if ((slideshowFade <= 1) && (slideshowFade >= -0.3f))
            repaint();
    }
    @Override
    public boolean drawMemory()      { return true; }
    @Override
    public String ambienceSoundKey() { return canContinue() ? super.ambienceSoundKey() : AMBIENCE_KEY; }
    @Override public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        int w = getWidth();
		updateTextValues();
        
        languagePanel.initFonts();

        if (backImg == null) {
            backImg1 =  ImageManager.current().image(imageKey1);
            backImg2 =  ImageManager.current().image(imageKey2);
            backImg = newOpaqueImage(backImg1);
            defaultBackground = backImg;
        }
        Image back = background();
        int imgW = back.getWidth(null);
        int imgH = back.getHeight(null);
        g.drawImage(back, 0, 0, getWidth(), getHeight(), 0, 0, imgW, imgH, this);
        
        Composite prevComp = g.getComposite();
        float textAlpha = min(1,max(0,slideshowFade));
        if ((textAlpha < 1) || hideText) {
            if (!hideText) {
                AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1-textAlpha);
                g.setComposite(ac);
            }
            slideshowText.draw(g);
        }
 
        if (textAlpha == 0) {
            languagePanel.setVisible(false);
            g.setComposite(prevComp);
            return;
        }
        
        if (textAlpha < 1) {
            AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER,textAlpha);
            g.setComposite(ac);
        }
        
        String titleStr1 = text("GAME_TITLE_LINE_1");
        String titleStr2 = text("GAME_TITLE_LINE_2");
        String titleStr3 = text("GAME_TITLE_LINE_3");

        if (titleImg == null) {
            titleImg = newBufferedImage(getWidth(), scaled(200));
            Graphics2D imgG = (Graphics2D) titleImg.getGraphics();
            setFontHints(imgG);
            int bigFont = scaledLogoFont(imgG, titleStr1+titleStr3, w*3/4, 80, 65);
            int smallFont = scaledLogoFont(imgG, titleStr2, w*3/20, 60, 40);
            imgG.setFont(logoFont(bigFont));
            int sw1a = imgG.getFontMetrics().stringWidth(titleStr1);
            imgG.setFont(logoFont(smallFont));
            int sw1b = imgG.getFontMetrics().stringWidth(titleStr2);
            imgG.setFont(logoFont(bigFont));
            int sw1c = imgG.getFontMetrics().stringWidth(titleStr3);
            int sw1Title = sw1a+sw1b+sw1c+s40;
            int x1Left = (w-sw1Title)/2;
            imgG.setFont(logoFont(bigFont));
            int baseY = scaled(150);
            drawShadowedString(imgG, titleStr1, 1, 0, 10, x1Left, baseY, logoShade[1], logoFore[0]);
            imgG.setFont(logoFont(smallFont));
            drawShadowedString(imgG, titleStr2, 1, 0, 10, x1Left+sw1a+s20, baseY, logoShade[1], logoFore[0]);
            imgG.setFont(logoFont(bigFont));
            drawShadowedString(imgG, titleStr3, 1, 0, 10, x1Left+sw1a+sw1b+s40, baseY, logoShade[1], logoFore[0]);
            imgG.setFont(logoFont(bigFont));
            drawShadowedString(imgG, titleStr1, 1, 0, 10, x1Left, baseY, logoShade[1], logoFore[0]);
            imgG.setFont(logoFont(smallFont));
            drawShadowedString(imgG, titleStr2, 1, 0, 10, x1Left+sw1a+s20, baseY, logoShade[1], logoFore[0]);
            imgG.setFont(logoFont(bigFont));
            imgG.dispose();
        }
        
        if (!hideText)
           g.drawImage(titleImg, 0, s100, null);
        if (hideText) {
            g.setComposite(prevComp);
            return;
        }

        if (languagePanel.fontsReady) {
            int lw = languagePanel.w;
            int lh = languagePanel.h;
            languagePanel.setBounds(w-lw-s15,s5,lw,lh);

            if (languagePanel.isVisible()) {
                g.setColor(langShade());
                g.fillRoundRect(w-s55, s5, s40, s40,s10,s10);
            }
            Image img = image("LANGUAGE_ICON");
            g.drawImage(img, w-s55, s5, s40, s40, this);
            languageBox.setBounds(w-s55, s5, s40, s40);

            String langText = LanguageManager.current().selectedLanguageName();
            g.setFont(narrowFont(24));
            int langSW = g.getFontMetrics().stringWidth(langText);
            int langX = w-s55-langSW-s10;
            g.setColor(logoFore[0]);
            drawShadowedString(g, langText, 2, langX, s30, Color.black, logoFore[0]);
        }

        discussText.disabled(false);
        if (!discussText.isEmpty())
            discussText.draw(g);
        
        if (canOpenManual()) {
            exitText.setY(610);
        }
        else
            exitText.setY(565);

        if (canRestart()) {
            continueText.reset();
            newGameText.reset();
            loadGameText.reset();
            saveGameText.reset();
            settingsText.disabled(false);
            settingsText.drawCentered(g);
            manualText.reset();
            exitText.reset();
            restartText.disabled(false);
            restartText.drawCentered(g);
        }
        else {
            restartText.reset();
            continueText.disabled(!canContinue());
            continueText.drawCentered(g);
            newGameText.disabled(!canNewGame());
            newGameText.drawCentered(g);
            loadGameText.disabled(!canLoadGame());
            loadGameText.drawCentered(g);
            saveGameText.disabled(!canSaveGame());
            saveGameText.drawCentered(g);
            settingsText.disabled(false);
            settingsText.drawCentered(g);
            manualText.visible(canOpenManual());
            manualText.drawCentered(g);
            exitText.disabled(!canExit());
            exitText.drawCentered(g);
        }
        // draw Shrink/Enlarge at top Right
        winSizeText.displayText(text("GAME_WINDOW_SIZE", UserPreferences.screenSizePct()));
        winSizeText.visible(shrinkText.isHovered() || enlargeText.isHovered());
        shrinkText.visible(UserPreferences.windowed());
        enlargeText.visible(UserPreferences.windowed());
        shrinkText.draw(g);
        enlargeText.draw(g);
        winSizeText.draw(g);

        // draw version at bottom right, then go up for other values
        developerText.draw(g);
        artistText.draw(g);
        graphicDsnrText.draw(g);
        writerText.draw(g);
        soundText.draw(g);
        translatorText.draw(g);
        versionText.draw(g);

		if (showLastUpdatedLink) {
			lastVersionText.displayText(text("GAME_MENU_LAST_RELEASE"));
			lastVerJarText.displayText(text("GAME_MENU_LAST_JAR"));
			lastVerMiniText.displayText(text("GAME_MENU_LAST_MINI_JAR"));
			lastVerExeText.displayText(text("GAME_MENU_LAST_ZIP"));
			lastVersionText.draw(g);
			lastVerJarText.draw(g);
			lastVerMiniText.draw(g);
			lastVerExeText.draw(g);
		}
		if (showNewUpdatedLink) {
			newVersionText.displayText(text("GAME_MENU_NEW_RELEASE"));
			newVersionText.draw(g);
		}

    	int boxW  = scaled(450);
        if (versionText == hoverBox) {
        	String info = text("GAME_MENU_GITHUB_REPOSITORY", Rotp.repName)
        			+ NEWLINE + NEWLINE + text("GAME_MENU_CHECK_FOR_UPDATE");
        	if (checkedForUpdate)
        		if (showLastUpdatedLink)
        			info += NEWLINE + NEWLINE + text("GAME_MENU_GOT_NEW_VERSION");
        		else
        			info += NEWLINE + NEWLINE + text("GAME_MENU_NO_NEW_VERSION");
        	drawInfo(g, info, versionText.x(), versionText.y(), boxW);
        }
		int offsetX = scaled(180);
		int offsetY = s25;
		if (showLastUpdatedLink && lastVerExeText == hoverBox) {
			String info = text("GAME_MENU_ZIP_DESC", gitInfoLast.githubRepo());
			drawInfo(g, info, lastVerExeText.x()+offsetX, lastVerExeText.y()+offsetY, boxW);
		}
		if (showLastUpdatedLink && lastVerMiniText == hoverBox) {
			String info = text("GAME_MENU_MINI_JAR_DESC", gitInfoLast.githubRepo());
			drawInfo(g, info, lastVerMiniText.x()+offsetX, lastVerMiniText.y()+offsetY, boxW);
		}
		if (showLastUpdatedLink && lastVerJarText == hoverBox) {
			String info = text("GAME_MENU_JAR_DESC", gitInfoLast.githubRepo());
			drawInfo(g, info, lastVerJarText.x()+offsetX, lastVerJarText.y()+offsetY, boxW);
		}
		if (showLastUpdatedLink && lastVersionText == hoverBox) {
			String info = text("GAME_MENU_LAST_VERSION", gitInfoLast.githubRepo());
			drawInfo(g, info, lastVersionText.x()+offsetX, lastVersionText.y()+offsetY, boxW);
		}
		if (showNewUpdatedLink && newVersionText == hoverBox) {
			String info =  text("GAME_MENU_NEWER_VERSION", gitInfoNew.githubRepo());
			drawInfo(g, info, newVersionText.x()+offsetX, newVersionText.y()+offsetY, boxW);
		}

        g.setComposite(prevComp);
    }
    private void drawInfo(Graphics2D g, String info, int xLeft, int yBottom, int w) {
    	int fontSize = 15;
    	g.setFont(plainFont(fontSize));
    	int lineH = scaled(fontSize);
    	int marginY = s5;
    	int marginX = s10;
    	int sw = 0;
    	List<String> lines = wrappedLines(g, info, w - 2*marginX);
    	for (String line : lines)
    		sw = max(sw, g.getFontMetrics().stringWidth(line));
    	w = sw + 2*marginX;
    	int boxH = lines.size()*lineH + 2*marginY;
    	int yTop = yBottom - boxH;
    	g.setColor(textShade());
    	g.fillRect(xLeft, yTop, w, boxH);
    	g.setColor(textColor());
    	int x0 = xLeft + marginX;
    	int y0 = yTop + marginY - lineH/4;
    	for (String line : lines) {
    		y0 += lineH;
    		g.drawString(line, x0, y0);
    	}
    }
    private String manualFilePath()	 {
        return LanguageManager.current().selectedLanguageFullPath()+"/manual.pdf";
    }
    private boolean manualExists()	 { 
        String filename = manualFilePath();
        return readerExists(filename);
    }
    public	boolean canContinue()    { return session().status().inProgress() || session().hasRecentSession(); }
    private	boolean canRecenStart()  { return session().hasRecentStartSession(); }
    private boolean canNewGame()     { return true; }
    private boolean canLoadGame()    { return true; }
    private boolean canSaveGame()    { return session().status().inProgress() || isCtrlDown(); }
    private boolean canOpenManual()  { return manualExists(); }
    private boolean canExit()        { return true; }
    private boolean canRestart()     { 
    	return !UserPreferences.displayMode().equals(startingDisplayMode) 
            || (UserPreferences.screenSizePct() != startingScale)
            || (UserPreferences.selectedScreen() != startingScreen);
    }

    private String getPage(String adress) {
    	try {
			URL url = new URL(adress);
			Scanner sc = new Scanner(url.openStream());
			StringBuffer sb = new StringBuffer();
			while(sc.hasNext()) {
				sb.append(sc.next());
				//System.out.println(sc.next());
			}
			sc.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
    }
	private GitInfo getGitInfo (String repo) {
		String baseLink = "https://github.com/" + repo + "/Rotp-Fusion/releases/";
		String githubLink	= baseLink + "latest/";
		String rawResult	= getPage(githubLink);
		String[] resArr		= rawResult.split(",|\\<|\\>");
		List <String> version = new ArrayList<>();
	
		for (String line : resArr) {
			if (line.contains("app-argument=")) {
				boolean ok = false;
				String[] keyArr = line.split("/");
				for (String key : keyArr) {
					if (ok)
						version.add(key.replace("\"", ""));
					else
						ok = key.equals("tag");
				}
			}
		}
		String linkRoot	= "";
		String shortVersion	= "";
		String longVersion	= "";
		if (version.size() >3) {
			longVersion = version.get(0) + "/"
					+ version.get(1) + "/" + version.get(2)
					+ "/" + version.get(3);
			shortVersion = version.get(0) + "."
					+ version.get(1) + "." + version.get(2)
					+ "." + version.get(3).substring(0, 2);
			linkRoot = baseLink + "download/" + longVersion + "/" + "rotp-Fusion-"
					+ version.get(0) + "-" + version.get(1) + "-" + version.get(2);
		}
		String jarFile		= linkRoot + ".jar";
		String jarMiniFile	= linkRoot + "-mini.jar";
		String zipFile		= linkRoot + "-windows.zip";
		Long value = getValue(shortVersion);
		//String folder = 
		return new GitInfo (value, repo, githubLink, jarFile, jarMiniFile, zipFile) ;
	}
	private record GitInfo (
			Long version,
			String githubRepo,
			String githubLink,
			String jarFile,
			String jarMiniFile,
			String zipFile) {}

    private Long getValue(String version) {
    	Long val = 0L;
    	if (version.isEmpty())
    		return val;
    	String[] strings = version.split("-|\\.");
    	if (strings.length < 4)
    		return val;
    	
    	Long year  = getLong(strings[0]);
    	if (year == null)
    		return val;
    	Long month = getLong(strings[1]);
    	if (month == null)
    		return val;
    	Long day   = getLong(strings[2]);
    	if (day == null)
    		return val;
    	Long hour  = getLong(strings[3]);
    	if (hour == null)
    		return val;
    	val = ((year*100L + month)*100L + day)*100L + hour;
    	return val;
    }
	private void checkForUpdate() {
		lastVersionText.displayText("");
		lastVerJarText.displayText("");
		lastVerMiniText.displayText("");
		lastVerExeText.displayText("");
		newVersionText.displayText("");
		gitInfoLast		= null;
		gitInfoNew		= null;
		currentVersion	= getValue(Rotp.buildTime);
		gitInfoXilmi	= getGitInfo("Xilmi");
		gitInfoBR		= getGitInfo("BrokenRegistry");

		showNewUpdatedLink  = false;
		showLastUpdatedLink = gitInfoBR.version() > currentVersion || gitInfoXilmi.version() > currentVersion;
		if (showLastUpdatedLink) {
			if (gitInfoBR.version() > gitInfoXilmi.version()) {
				gitInfoLast = gitInfoBR;
				if (gitInfoXilmi.version() > currentVersion) {
					gitInfoNew = gitInfoXilmi;
					showNewUpdatedLink = true;
				}
			}
			else {
				gitInfoLast = gitInfoXilmi;
				if (gitInfoBR.version() > currentVersion) {
					gitInfoNew = gitInfoBR;
					showNewUpdatedLink = true;
				}
			}
		}
	}
    private void versionAction(MouseEvent e) {
    		buttonClick();
    		checkForUpdate();
    		checkedForUpdate = true;
    		repaint();
    }

    private void rescaleMenuOptions() {
        restartText.rescale();
        continueText.rescale();
        newGameText.rescale();
        loadGameText.rescale();
        saveGameText.rescale();
        settingsText.rescale();
        manualText.rescale();
        exitText.rescale();
    }
    private void resetSlideshowTimer() {
        if (slideshowFade < 1) {
            slideshowFade = SLIDESHOW_MAX;
            repaint();
        }
    }
    private void loadHotKeysUI()		{
    	HelpUI helpUI = RotPUI.helpUI();
        helpUI.clear();
        int xHK = scaled(80);
        int yHK = scaled(280);
        int wHK = scaled(360);
        helpUI.addBrownHelpText(xHK, yHK, wHK, 18, text("SETUP_GAME_HELP_HK"));
        helpUI.open(this);
	}
	@Override public void showHotKeys()	{
		loadHotKeysUI();
		repaint();   
	}
    @Override public void advanceHelp()	{ cancelHelp(); }
    @Override public void cancelHelp()	{ RotPUI.helpUI().close(); }
    @Override public void keyReleased(KeyEvent e) {
    	checkModifierKey(e);
        int k = e.getKeyCode();
        switch (k) {
        case KeyEvent.VK_Z:
        	hideText = false;
        	repaint();
        	return;
        case KeyEvent.VK_V:
        	if (e.isAltDown()) {
        		checkForUpdate();
            	repaint();
            	return;
        	}
        }
    }
    @Override public void keyPressed(KeyEvent e) {
        resetSlideshowTimer();
    	checkModifierKey(e);
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.VK_MINUS:
                if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) // BR: updated deprecated
                    shrinkFrame(); 
                return;
            case KeyEvent.VK_EQUALS: 
                if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) // BR: updated deprecated
                    expandFrame(); 
                return;
            case KeyEvent.VK_H:
		    case KeyEvent.VK_F1: showHotKeys();		return;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_C:  continueGame();	return;
            case KeyEvent.VK_N:  newGame();			return;
            case KeyEvent.VK_L:
            	if (e.isControlDown())
            		loadOptions();
            	else
            		loadGame();
            	return;
            case KeyEvent.VK_M:
            case KeyEvent.VK_O:  openManual();		return;
            case KeyEvent.VK_R:  replayLastTurn();	return;
            case KeyEvent.VK_S:
            	if (e.isControlDown())
            		saveOptions();
            	else
            		saveGame();
            	return;
            case KeyEvent.VK_T:  goToSettings();	return;
            case KeyEvent.VK_E:
            case KeyEvent.VK_X:  exitGame();		return;
            case KeyEvent.VK_Z:  hideText = true;	repaint();	return;
            case KeyEvent.VK_PAGE_UP:	IDebugOptions.showVIPPanel(true);  return;
            case KeyEvent.VK_PAGE_DOWN:	IDebugOptions.showVIPPanel(false); return;
            case KeyEvent.VK_HOME:
            	File file = new File (Rotp.jarPath());
            	Desktop desktop = Desktop.getDesktop();
				try {
					desktop.open(file);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
        }
    }
    private void shrinkFrame() {
        if (!UserPreferences.windowed())
            return;
        if (UserPreferences.shrinkFrame()) {
            Rotp.setFrameSize();
            rescaleMenuOptions();
            titleImg = null;
            languagePanel.initBounds();
            UserPreferences.save();
            repaint();
       }
    }
    private void expandFrame() {
        if (!UserPreferences.windowed())
            return;
       if (UserPreferences.expandFrame()) {
            Rotp.setFrameSize();
            rescaleMenuOptions();
            titleImg = null;
            languagePanel.initBounds();
            UserPreferences.save();
            repaint();
       }    
    }
    private void openRedditPage() {
        try {
            buttonClick();
            Desktop.getDesktop().browse(new URL("http://www.reddit.com/r/rotp").toURI());
        } catch (IOException | URISyntaxException e) {}
    }
    private void openUrlPage(String url) {
        try {
            buttonClick();
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (IOException | URISyntaxException e) {}
    }
    private void openManual() {
        try {
            buttonClick();
            String filename = manualFilePath();
            InputStream manualAsStream = fileInputStream(filename);
            Path tempOutput = Files.createTempFile("ROTP_Manual", ".pdf");
            tempOutput.toFile().deleteOnExit();
            Files.copy(manualAsStream, tempOutput, StandardCopyOption.REPLACE_EXISTING);
            File userManual = new File (tempOutput.toFile().getPath());
            if (userManual.exists()) 
                Desktop.getDesktop().open(userManual);
        } catch (IOException e) {}
    }
    private void loadOptions()	{
    	String dirPath = Rotp.jarPath();
    	String ext = IGameOptions.OPTIONFILE_EXTENSION;
    	ext = ext.replace(".", "");
    	JFileChooser chooser = new JFileChooser();
    	chooser.setCurrentDirectory(new File(dirPath));
    	chooser.setAcceptAllFileFilterUsed(false);
    	chooser.addChoosableFileFilter(new FileNameExtensionFilter("Options", ext));
    	int status = chooser.showOpenDialog(this);
    	if (status == JFileChooser.APPROVE_OPTION) {
    		File file = chooser.getSelectedFile();
    		if (file != null) {
    			buttonClick();
    			newGameOptions().updateAllNonCfgFromFile(file.getName());
    			return;
    		}
    	}
		misClick();
    }
    private void saveOptions()	{
    	String dirPath = Rotp.jarPath();
    	String ext = IGameOptions.OPTIONFILE_EXTENSION;
    	//ext = ext.replace(".", "");
    	JFileChooser chooser = new JFileChooser();
    	chooser.setCurrentDirectory(new File(dirPath));
    	chooser.setAcceptAllFileFilterUsed(false);
    	chooser.addChoosableFileFilter(new FileNameExtensionFilter("Options", ext.replace(".", "")));
    	int status = chooser.showSaveDialog(this);
    	if (status == JFileChooser.APPROVE_OPTION) {
    		File file = chooser.getSelectedFile();
	  		if (file != null) {
				buttonClick();
				String name = file.getName();
				if (!name.toUpperCase().endsWith(ext.toUpperCase()))
					name += ext;
				newGameOptions().saveOptionsToFile(name);
				return;
			}
    	}
		misClick();
    }
    private void replayLastTurn() { // BR:
        if (canRecenStart()) {
            buttonClick();
           	session().loadRecentStartGame(true);
			rulesetManager().setAsGameMode();
			UserPreferences.reload();
            RotPUI.instance().selectMainPanel();
            RotPUI.instance().mainUI().showDisplayPanel();
            if (gameName == "")
        		gameName = generateGameName(options());
        }
    }
    public void continueGame() { // BR:
        if (canContinue()) {
            buttonClick();
            if (!session().status().inProgress()) {
            	session().loadLastSavedGame(true);
            	// session().loadRecentSession(true);
            }
			rulesetManager().setAsGameMode();
			UserPreferences.reload();

			if (session().galaxy().playerSwapRequest())
				session().galaxy().swapPlayerEmpire();

    		RotPUI.instance().selectMainPanel();
            RotPUI.instance().mainUI().showDisplayPanel();
            if (gameName == "")
        		gameName = generateGameName(options());
        }
    }
    private void newGame() { // BR:
        if (canNewGame()) {
            buttonClick();
            RotPUI.instance().selectSetupRacePanel();
        }
    }
    private void loadGame() { // BR:
        if (canLoadGame()) {
            buttonClick();
//            loadRequest(false); // The call was not for SetupRaceUI
//            newGameOptions(); // To create one if none
            RotPUI.instance().selectLoadGamePanel();
        }
    }
    private void saveGame() { // BR:
        if (canSaveGame()) {
            buttonClick();
            RotPUI.instance().selectSaveGamePanel();
        }
    }
    private void exitGame() {
        if (canExit()) {
            buttonClick();
            GameSession.instance().exit();
        }
    }
    private void restartGame() {
        Rotp.restart();
    }
    private void selectLanguage(int i) {
        softClick();
        LanguageManager.current().selectLanguage(i);
        UserPreferences.save();
        setTextValues();
        titleImg = null;
        repaint();
    }
    private void goToSettings() {
		buttonClick();
		MainOptionsUI mainOptionsUI = RotPUI.mainOptionsUI();
		mainOptionsUI.init();
		return;
    }
    @Override
    public void playAmbience() {
        // in case playing ambiance causes a sound error
        super.playAmbience();
        setTextValues();
    }
    @Override public void mouseClicked(MouseEvent e)  { }
    @Override public void mouseEntered(MouseEvent e)  { }
    @Override public void mouseExited(MouseEvent e)   { }
    @Override public void mousePressed(MouseEvent e)  {
        resetSlideshowTimer();
        mouseDepressed = true;
        if (hoverBox != null)
            hoverBox.mousePressed();
    }
    @Override public void mouseReleased(MouseEvent e) {
    	checkModifierKey(e);
        if (e.getButton() > 3)
            return;
        int x = e.getX();
        int y = e.getY();
        if (hoverBox != null)
            hoverBox.mouseReleased();
        mouseDepressed = false;

        if (manualText.contains(x,y))
            openManual();
        else if (discussText.contains(x,y))
            openRedditPage();
        else if (continueText.contains(x,y))
        	if (e.isControlDown())
        		replayLastTurn();
        	else
        		continueGame();
        else if (newGameText.contains(x,y))
            newGame();
        else if (loadGameText.contains(x,y))
        	if (e.isControlDown())
        		loadOptions();
        	else
        		loadGame();
        else if (saveGameText.contains(x,y))
        	if (e.isControlDown())
        		saveOptions();
        	else
        		saveGame();
        else if (settingsText.contains(x,y))
            goToSettings();
        else if (exitText.contains(x,y))
            exitGame();
        else if (restartText.contains(x,y))
            restartGame();
        else if (shrinkText.contains(x,y))
            shrinkFrame();
        else if (enlargeText.contains(x,y))
            expandFrame();
        else if (versionText.contains(x,y))
        	versionAction(e);
		else if (lastVersionText.contains(x,y))
			openUrlPage(gitInfoLast.githubLink());
		else if (lastVerJarText.contains(x,y))
			openUrlPage(gitInfoLast.jarFile());
		else if (lastVerMiniText.contains(x,y))
			openUrlPage(gitInfoLast.jarMiniFile());
		else if (lastVerExeText.contains(x,y))
			openUrlPage(gitInfoLast.zipFile());
		else if (newVersionText.contains(x,y))
			openUrlPage(gitInfoNew.githubLink());;
    }
    @Override public void mouseDragged(MouseEvent e)  { mouseMoved(e); }
    @Override public void mouseMoved(MouseEvent e)    {
        int x = e.getX();
        int y = e.getY();

        resetSlideshowTimer();
        boolean repaint = false;

        if (hideText)
            return;

        BaseText newHover = null;
        if (languageBox.contains(x,y)) 
            languagePanel.setVisible(true);
        else if (discussText.contains(x,y))
            newHover = discussText;
        else if (canContinue() && continueText.contains(x,y))
            newHover = continueText;
        else if (canNewGame() && newGameText.contains(x,y))
            newHover = newGameText;
        else if (canLoadGame() && loadGameText.contains(x,y))
            newHover = loadGameText;
        else if (canSaveGame() && saveGameText.contains(x,y))
            newHover = saveGameText;
        else if (manualText.contains(x,y))
            newHover = manualText;
        else if (settingsText.contains(x,y))
            newHover = settingsText;
        else if (canExit() && exitText.contains(x,y))
            newHover = exitText;
        else if (canRestart() && restartText.contains(x,y))
            newHover = restartText;
        else if (shrinkText.contains(x,y))
            newHover = shrinkText;
        else if (enlargeText.contains(x,y))
            newHover = enlargeText;
		else if (versionText.contains(x,y)) {
			newHover = versionText;
			repaint = true;
		}
		else if (lastVersionText.contains(x,y)) {
			newHover = lastVersionText;
			repaint = true;
		}
		else if (lastVerJarText.contains(x,y)) {
			newHover = lastVerJarText;
			repaint = true;
		}
		else if (lastVerMiniText.contains(x,y)) {
			newHover = lastVerMiniText;
			repaint = true;
		}
		else if (lastVerExeText.contains(x,y)) {
			newHover = lastVerExeText;
			repaint = true;
		}
		else if (newVersionText.contains(x,y)) {
			newHover = newVersionText;
			repaint = true;
		}

		if (hoverBox != newHover) {
			if (hoverBox != null) {
				if (hoverBox == versionText
						|| hoverBox == lastVersionText
						|| hoverBox == lastVerJarText
						|| hoverBox == lastVerMiniText
						|| hoverBox == lastVerExeText
						|| hoverBox == newVersionText)
					repaint = true;
                hoverBox.mouseExit();
                repaint(hoverBox.bounds());
            }
            hoverBox = newHover;
            if (hoverBox != null) {
                if (mouseDepressed)
                    hoverBox.mousePressed();
                else
                    hoverBox.mouseEnter();
                repaint(hoverBox.bounds());
            }
        }
        if (repaint)
        	repaint();
    }
    private class GameLanguagePane extends BasePanel implements MouseListener, MouseMotionListener {
        private static final long serialVersionUID = 1L;
        private List<String> names;
        private List<String> codes;
        private int w;
        private int h;
        private boolean fontsInitialized = false;
        private boolean fontsReady = false;
        private Rectangle[] lang;
        private Rectangle hoverBox;
        private GameUI parent;
        private GameLanguagePane(GameUI ui) {
            parent = ui;
            init();
        }
        private void init() {
            codes = LanguageManager.current().languageCodes();
            names = LanguageManager.current().languageNames();
            initBounds();
            lang = new Rectangle[names.size()];
            for (int i=0;i<lang.length;i++)
                lang[i] = new Rectangle();
            addMouseListener(this);
            addMouseMotionListener(this);
            setOpaque(false);
        }
        private void initBounds() {
            w = scaled(100);
            h = s45+(s17*names.size());
        }
        private void initFonts() {
            if (fontsInitialized)
                return;
            
            fontsInitialized = true;
            Thread r1 = new RenderFontsThread();
            r1.start();
        }
        private void renderFonts() {
            Graphics g = getGraphics();
            int y0 = 0;
            for (int i=0; i<names.size(); i++) {
                String code = codes.get(i);
                String name = names.get(i);
                Font f = FontManager.current().languageFont(code);
                g.setFont(f);
                g.setColor(Color.white);
                int sw = g.getFontMetrics().stringWidth(name);
                drawString(g,name, w-sw-s5, y0);
            }    
            fontsReady = true;
            g.dispose();
            parent.repaint();
        }
        @Override
        public void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();
            g.setColor(langShade());
            int topM = s35;
            int lineH = s17;
            g.fillRoundRect(0,topM,w,h-topM,s10,s10);
            int y0 = topM;
            for (int i=0; i<names.size(); i++) {
                String code = codes.get(i);
                String name = names.get(i);
                Font f = FontManager.current().languageFont(code);
                g.setFont(f);
                Color c0 = hoverBox == lang[i] ? Color.yellow : Color.white;
                g.setColor(c0);
                y0 += lineH;
                int sw = g.getFontMetrics().stringWidth(name);
                drawString(g,name, w-sw-s5, y0);
                lang[i].setBounds(w-sw-s5, y0-lineH, sw+s5, lineH);
            }
        }
        @Override
        public void mouseClicked(MouseEvent e) { }
        @Override
        public void mousePressed(MouseEvent e) { 
            resetSlideshowTimer();
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            for (int i=0;i<lang.length;i++) {
                if (hoverBox == lang[i]) {
                    selectLanguage(i);
                    break;
                }
            }
        }
        @Override
        public void mouseEntered(MouseEvent e) { }
        @Override
        public void mouseExited(MouseEvent e) {
            hoverBox = null;
            setVisible(false);
        }
        @Override
        public void mouseDragged(MouseEvent e) { }
        @Override
        public void mouseMoved(MouseEvent e) {
        	checkModifierKey(e);
            int x = e.getX();
            int y = e.getY();
            resetSlideshowTimer();
            Rectangle prevHover = hoverBox;
            hoverBox = null;
            for (Rectangle box: lang) {
                if (box.contains(x,y)) {
                    hoverBox = box;
                    break;
                }
            }

            if (hoverBox != prevHover)
                repaint();
        }

        private class RenderFontsThread extends Thread {
        	@Override public void run() { renderFonts(); }
        }
    }
}

