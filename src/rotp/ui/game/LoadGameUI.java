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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.SwingUtilities;

import rotp.Rotp;
import rotp.model.galaxy.GalaxyFactory.GalaxyCopy;
import rotp.model.game.GameSession;
import rotp.model.game.IGameOptions;
import rotp.model.game.IMainOptions;
import rotp.ui.BasePanel;
import rotp.ui.NoticeMessage;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.main.SystemPanel;

public final class LoadGameUI  extends BasePanel implements MouseListener, MouseWheelListener {
    private static final long serialVersionUID = 1L;
    private static final int  MAX_FILES = 20;
    private static final int SORT_FN_UP = 1;
    private static final int SORT_FN_DN = 2;
    private static final int SORT_DT_UP = 3;
    private static final int SORT_DT_DN = 4;
    private static final int SORT_SZ_UP = 5;
    private static final int SORT_SZ_DN = 6;
    
    private static final SimpleDateFormat fileDateFmtN = new SimpleDateFormat("MMM dd, HH:mm");
    private static final SimpleDateFormat fileDateFmtY = new SimpleDateFormat("yyyy MMM dd, HH:mm");
    static LoadGameUI current;

    LoadListingPanel listingPanel;
    List<String> saveFiles = new ArrayList<>();
    List<Long> saveSizes = new ArrayList<>();
    List<String> saveDates = new ArrayList<>();
    String selectedFile = "";
    Shape hoverBox;
    Shape selectBox;
    int selectIndex;
    int start = 0;
    int end = 0;
    private GalaxyCopy oldGalaxy;
    private boolean restart;

    int sortOrder = SORT_DT_UP;
    int buttonW, button1X, button2X;

    boolean hasAutosave = false;
    boolean loading = false;
    boolean hasBackupDir = false;
    boolean showingBackups = false;
    String backupDirInfo = "";
    String saveDirInfo = "";
    private final Rectangle cancelBox = new Rectangle();
    private final Rectangle loadBox = new Rectangle();
    private final Rectangle fileNameBox = new Rectangle();
    private final Rectangle fileSizeBox = new Rectangle();
    private final Rectangle fileDateBox = new Rectangle();
    private final RoundRectangle2D saveDirBox = new RoundRectangle2D.Float();
    private final RoundRectangle2D backupDirBox = new RoundRectangle2D.Float();
    private LinearGradientPaint[] loadBackC;
    private LinearGradientPaint[] cancelBackC;

    public LoadGameUI() {
        current = this;
        initModel();
    }
    // BR: for restarting with new options
    public void init(GalaxyCopy oldGalaxy) {
     	this.oldGalaxy	= oldGalaxy;
    	restart = true;
    	init();
    }
    public void init() {
        saveFiles.clear();
        saveDates.clear();
        hoverBox = null;
        selectBox = null;
        selectIndex = -1;
        start = 0;
        end = 0;
        selectedFile = "";
        hasAutosave = false;
        loading = false;
        hasBackupDir = false;
        showingBackups = false;

        sortListing();
    }
    private void sortListing() {
        hasAutosave = false;
        saveFiles.clear();
        saveSizes.clear();
        saveDates.clear();
        String ext = GameSession.SAVEFILE_EXTENSION;
		SimpleDateFormat fileDateFmt;
		if (IMainOptions.isLoadSaveWidthNormal())
			fileDateFmt = fileDateFmtN;
		else
			fileDateFmt = fileDateFmtY;

        // check for autosave
        String saveDirPath = UserPreferences.saveDirectoryPath();
        String backupDirPath = UserPreferences.backupDirectoryPath();
        File saveDir = new File(saveDirPath);
        File backupDir = new File(backupDirPath);
        hasBackupDir = backupDir.exists() && backupDir.isDirectory();

        FilenameFilter filter = (File dir, String name1) -> name1.toLowerCase().endsWith(ext);
        File[] fileList = saveDir.listFiles(filter);

        // fileList = null if prefs pointing to an invalid folder...default to jarPath 
        if (fileList == null) {
            saveDirPath = Rotp.jarPath();
            saveDir = new File(saveDirPath);
            backupDirPath = saveDirPath+"/"+GameSession.BACKUP_DIRECTORY;
            backupDir = new File(backupDirPath);
            hasBackupDir = backupDir.exists() && backupDir.isDirectory();;
            fileList = saveDir.listFiles(filter);
        }
        long sSize = fileList.length;
        if (hasBackupDir)
            sSize--;
        saveDirInfo = text("LOAD_GAME_SAVE_DIR", (int)sSize);
        
        if (hasBackupDir) {
            long bSize = backupDir.listFiles(filter).length;
            backupDirInfo = text("LOAD_GAME_BACKUP_DIR", (int)bSize);
        }

        File[] filesList;
        if (showingBackups) 
            filesList = backupDir.listFiles(filter);
        else 
            filesList = saveDir.listFiles(filter);

        File autoSave = new File(saveDir, GameSession.RECENT_SAVEFILE);
        if (!showingBackups && autoSave.isFile()) {
            hasAutosave = true;
            saveFiles.add(text("LOAD_GAME_AUTOSAVE"));
            saveDates.add(fileDateFmt.format(autoSave.lastModified()));
            saveSizes.add(autoSave.length());
        }

        switch(sortOrder) {
            case SORT_FN_UP : Arrays.sort(filesList, FILE_NAME); break;
            case SORT_FN_DN : Arrays.sort(filesList, Collections.reverseOrder(FILE_NAME)); break;
            case SORT_DT_UP : Arrays.sort(filesList, FILE_DATE); break;
            case SORT_DT_DN : Arrays.sort(filesList, Collections.reverseOrder(FILE_DATE)); break;
            case SORT_SZ_UP : Arrays.sort(filesList, FILE_SIZE); break;
            case SORT_SZ_DN : Arrays.sort(filesList, Collections.reverseOrder(FILE_SIZE)); break;
        }
        for (File f : filesList){
            if (f.isFile()) {
                String name = f.getName();
                if (name.endsWith(ext)
                && !name.equalsIgnoreCase(GameSession.RECENT_SAVEFILE)) {
                    List<String> parts = substrings(name, '.');
                    if (!parts.get(0).trim().isEmpty()) {
                        saveFiles.add(name.substring(0, name.length()-ext.length()));
                        saveDates.add(fileDateFmt.format(f.lastModified()));
                        saveSizes.add(f.length());
                    }
                }
            }
        }
        if (!saveDates.isEmpty()) {
            selectIndex = 0;
            if (hasAutosave)
                selectedFile = GameSession.RECENT_SAVEFILE;
            else
                selectedFile = saveFiles.get(start+selectIndex)+GameSession.SAVEFILE_EXTENSION;
        }
    }
    private String selectedFileName(int index) {
        if ((start+index == 0) && hasAutosave)
            return GameSession.RECENT_SAVEFILE;
        else
            return saveFiles.get(start+index)+GameSession.SAVEFILE_EXTENSION;
    }
    private String fileBaseName(String fn) {
        String ext = GameSession.SAVEFILE_EXTENSION;
        if (fn.endsWith(ext)
        && !fn.equalsIgnoreCase(GameSession.RECENT_SAVEFILE)) {
            List<String> parts = substrings(fn, '.');
            if (!parts.get(0).trim().isEmpty())
                return fn.substring(0, fn.length()-ext.length());
        }
        return "";
    }
    private void initGradients() {
        int w = getWidth();
        buttonW = s100+s100;
        button1X = (w/2)-s10-buttonW;
        button2X = (w/2)+s10;
        Point2D start1 = new Point2D.Float(button1X, 0);
        Point2D end1 = new Point2D.Float(button1X+buttonW, 0);
        Point2D start2 = new Point2D.Float(button2X, 0);
        Point2D end2 = new Point2D.Float(button2X+buttonW, 0);
        float[] dist = {0.0f, 0.5f, 1.0f};

        Color brownEdgeC = new Color(100,70,50);
        Color brownMidC = new Color(161,110,76);
        Color[] brownColors = {brownEdgeC, brownMidC, brownEdgeC };

        Color grayEdgeC = new Color(59,66,65);
        Color grayMidC = new Color(107,118,117);
        Color[] grayColors = {grayEdgeC, grayMidC, grayEdgeC };

        loadBackC = new LinearGradientPaint[2];
        cancelBackC = new LinearGradientPaint[2];

        loadBackC[0] = new LinearGradientPaint(start1, end1, dist, brownColors);
        cancelBackC[0] = new LinearGradientPaint(start2, end2, dist, brownColors);
        loadBackC[1] = new LinearGradientPaint(start1, end1, dist, grayColors);
        cancelBackC[1] = new LinearGradientPaint(start2, end2, dist, grayColors);
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (loadBackC == null)
            initGradients();
        Image back = GameUI.defaultBackground;
        int imgW = back.getWidth(null);
        int imgH = back.getHeight(null);
        g.drawImage(back, 0, 0, getWidth(), getHeight(), 0, 0, imgW, imgH, this);
    }
    private void initModel() {
        addMouseWheelListener(this);
        listingPanel = new LoadListingPanel();
        setLayout(new BorderLayout());
        add(listingPanel, BorderLayout.CENTER);
    }
    private void scrollDown() {
        int prevStart = start;
        int prevSelect = selectIndex;
        start = max(0, min(start+1, saveFiles.size()-MAX_FILES));
        if ((start == prevStart) && (selectIndex >= 0))
            selectIndex = min(selectIndex+1, saveFiles.size()-1, MAX_FILES-1);
        selectedFile = selectedFileName(selectIndex);
        if ((prevStart != start) || (prevSelect != selectIndex))
            repaint();
    }
    private void scrollUp() {
        int prevStart = start;
        int prevSelect = selectIndex;
        start = max(start-1, 0);
        if ((start == prevStart) && (selectIndex >= 0))
            selectIndex = max(selectIndex-1, 0);
        selectedFile = selectedFileName(selectIndex);
        if ((prevStart != start) || (prevSelect != selectIndex))
            repaint();
    }
    private void sortByFileName() {
        switch (sortOrder) {
            case SORT_FN_UP : sortOrder = SORT_FN_DN; break;
            default         : sortOrder = SORT_FN_UP; break;
        }
        sortListing();
        repaint();
    }
    private void sortByDate() {
        switch (sortOrder) {
            case SORT_DT_UP : sortOrder = SORT_DT_DN; break;
            default         : sortOrder = SORT_DT_UP; break;
        }
        sortListing();
        repaint();
    }
    private void sortBySize() {
        switch (sortOrder) {
            case SORT_SZ_UP : sortOrder = SORT_SZ_DN; break;
            default         : sortOrder = SORT_SZ_UP; break;
        }
        sortListing();
        repaint();
    }
    private void toggleSaveBackupListing() {
        showingBackups = !showingBackups;
        selectBox = null;
        start = 0;
        selectIndex = 0;
        sortListing();
        current.repaint();
    }
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int count = e.getUnitsToScroll();
        int absCount = Math.abs(count);
        if (count < 0) {
            while (absCount > 0) {
                scrollUp();
                absCount--;
            }
        }
        else  {
            while (absCount > 0) {
                scrollDown();
                absCount--;
            }
        }
    }
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() > 3)
            return;
        RotPUI.instance().selectMainPanel(false);
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        switch(k) {
            case KeyEvent.VK_DOWN:  scrollDown();	return;
            case KeyEvent.VK_UP:    scrollUp();		return;
            case KeyEvent.VK_L:
            	if (e.isAltDown()) {
            		debugReloadLabels(this);
            		break;
            	}
            	if (canSelect())
                    loadGame(selectedFile);
                return;
            case KeyEvent.VK_ENTER:
                if (canSelect())
                    loadGame(selectedFile);
                return;
            case KeyEvent.VK_TAB:
                if (hasBackupDir)
                    toggleSaveBackupListing();
                break;
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_C:    cancelLoad();	return;
            case KeyEvent.VK_RIGHT:
            	if (e.isControlDown()) {
            		IGameOptions.loadSaveWidth.next();
            		sortListing();
            		repaint();
            	}
            	return;
            case KeyEvent.VK_LEFT:
            	if (e.isControlDown()) {
            		IGameOptions.loadSaveWidth.prev();
            		sortListing();
            		repaint();
            	}
            	return;
        }
    }
    private boolean canSelect()    { return selectIndex >= 0; }
    private boolean canLoad()      { return !selectedFile.isEmpty(); }
    public void loadRecentGame() {
        loading = true;
        repaint();
        buttonClick();
        final Runnable load = () -> {
            GameSession.instance().loadRecentSession(false);
        };
        SwingUtilities.invokeLater(load);
    }

    private GameSession loadObjectData(InputStream is) { // BR: copy from GameSession
        try {
            GameSession newSession;
            try (InputStream buffer = new BufferedInputStream(is)) {
                ObjectInput input = new ObjectInputStream(buffer);
                newSession = (GameSession) input.readObject();
            }
            return newSession;
        }
        catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    private GameSession preloadGame(String dir, String filename) { // BR: partial copy from GameSession
        GameSession newSession;    	
        try {
            log("preLoading game from file: ", filename);
            File saveFile = dir.isEmpty() ? new File(filename) : new File(dir, filename);
            // assume the file is not zipped, load it directly
            try (InputStream file = new FileInputStream(saveFile)) {
                newSession = loadObjectData(file);
            }

            // if newSession is null, see if it is zipped
            if (newSession == null) {
                try (ZipFile zipFile = new ZipFile(saveFile)) {
                    ZipEntry ze = zipFile.entries().nextElement();
                    InputStream zis = zipFile.getInputStream(ze);
                    newSession = loadObjectData(zis);
                    if (newSession == null) 
                        throw new RuntimeException(text("LOAD_GAME_BAD_VERSION", filename));
                }
            }
            session().options(newSession.options()); // Will be updated later
            oldGalaxy.copy(newSession);
         }
        catch(IOException e) {
            throw new RuntimeException(text("LOAD_GAME_BAD_VERSION", filename));
        }
        return newSession;
    }
    private void restartGame(String dirName, String s) {
        loading = true;
        repaint();

        final Runnable load = () -> {
        	GameSession newSession; 
            newSession = preloadGame(dirName, s);
            SelectRestartEmpireUI selectRestartEmpireUI = SelectRestartEmpireUI.instance();
            selectRestartEmpireUI.init(oldGalaxy, newSession);
    		disableGlassPane();
    		selectRestartEmpireUI.open();
            restart	  = false;
            oldGalaxy = null;
        };
        SwingUtilities.invokeLater(load);	
    }
    public void loadGame(String s) {
        if (!canLoad())
            return;
        loading = true;
        repaint();
        buttonClick();
        GameUI.gameName = fileBaseName(s);
        String dirName = showingBackups ? session().backupDir() : session().saveDir();

        if (restart) {
        	restartGame(dirName, s);
        	return;
        }

        final Runnable load = () -> {
            GameSession.instance().loadSession(dirName, s, false);
        };
        SwingUtilities.invokeLater(load);
    }
    public void cancelLoad() {
        buttonClick();
        if (restart) {
            restart	  = false;
            oldGalaxy = null;
        	RotPUI.instance().selectSetupGalaxyPanel();
        }
        else
        	RotPUI.instance().selectGamePanel();
    }
    class LoadListingPanel extends BasePanel implements MouseListener, MouseMotionListener {
        private static final long serialVersionUID = 1L;
        private final Rectangle[] gameBox = new Rectangle[MAX_FILES];
        private final Rectangle listBox = new Rectangle();
        private boolean dragging = false;
        private int lastMouseY;
        private int yOffset = 0;
        private int lineH = s50;
        public LoadListingPanel() {
            init();
        }
        private void init() {
            setOpaque(false);
            for (int i=0;i<gameBox.length;i++)
                gameBox[i] = new Rectangle();
            addMouseListener(this);
            addMouseMotionListener(this);
        }
        @Override
        public void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;

            for (int i=0;i<gameBox.length;i++)
                gameBox[i].setBounds(0,0,0,0);

            int w = getWidth();
            lineH = s22;

            String title = text("LOAD_GAME_TITLE");
            g.setFont(font(60));
            int sw = g.getFontMetrics().stringWidth(title);
            drawShadowedString(g, title, 1, 3, (w-sw)/2, scaled(120), GameUI.titleShade(), GameUI.titleColor());

            end = min(saveFiles.size(), start+MAX_FILES);

            //int w0 = scaled(650);
            boolean showYear = !IMainOptions.isLoadSaveWidthNormal();
            int w0 = scaled(IMainOptions.loadSaveWidth());
            int x0 = (w-w0)/2;
            int h0 = s5+(MAX_FILES*lineH);
            int y0 = scaled(180);

            // draw back mask
            int wTop = hasBackupDir ? s45 : s10;
            int wSide = s40;
            int wBottom = s80;
            g.setColor(GameUI.loadListMask());
            g.fillRect(x0-wSide, y0-wTop, w0+wSide+wSide, h0+lineH+wTop+wBottom);

            // draw dir info
            if (hasBackupDir) {
                g.setFont(narrowFont(20));
                int sw0 = g.getFontMetrics().stringWidth(saveDirInfo);
                saveDirBox.setRoundRect(x0, y0-s30, sw0+s20, s30, s8, s8);
                if (showingBackups)
                    g.setColor(GameUI.loadHiBackground());
                else
                    g.setColor(GameUI.loadHoverBackground());
                g.fill(saveDirBox);
                if (hoverBox == saveDirBox)
                    g.setColor(Color.yellow);
                else
                    g.setColor(SystemPanel.blackText);
                drawString(g,saveDirInfo, x0+s10, y0-s10);

                int sw1 = g.getFontMetrics().stringWidth(backupDirInfo);
                int x1 = x0+sw0+s30;
                backupDirBox.setRoundRect(x1, y0-s30, sw1+s20, s30, s8, s8);
                if (!showingBackups)
                    g.setColor(GameUI.loadHiBackground());
                else
                    g.setColor(GameUI.loadHoverBackground());
                g.fill(backupDirBox);
                if (hoverBox == backupDirBox)
                    g.setColor(Color.yellow);
                else
                    g.setColor(SystemPanel.blackText);
                drawString(g,backupDirInfo, x1+s10, y0-s10);

                g.setColor(GameUI.loadHoverBackground());
                g.fillRect(x0,y0-s3,w0,s3);
            }

            g.setColor(GameUI.raceCenterColor());
            g.fillRect(x0, y0, w0, h0+lineH);

            g.setColor(GameUI.sortLabelBackColor());
            g.fillRect(x0, y0, w0, lineH);
            drawFilenameButton(g, x0+s30, y0+lineH);
            if (showYear)
                drawSizeButton(g, x0+w0-scaled(185), y0+lineH);
            else
                drawSizeButton(g, x0+w0-scaled(150), y0+lineH);
            drawDateButton(g, x0+w0-s30, y0+lineH);
            // draw list of games to load
            int lineY = y0+s5+lineH;
            listBox.setBounds(x0, y0, w0, h0);
            for (int i=start;i<start+MAX_FILES;i++) {
                int boxIndex = i-start;
                if (boxIndex == selectIndex) {
                    g.setPaint(GameUI.loadHoverBackground());
                    g.fillRect(x0+s20, lineY-s4, w0-s40, lineH);
                }
                else if (i % 2 == 1) {
                    g.setPaint(GameUI.loadHiBackground());
                    g.fillRect(x0+s20, lineY-s4, w0-s40, lineH);
                }
                if (i<end) {
                    drawSaveGame(g, boxIndex, saveFiles.get(i), saveSizes.get(i), saveDates.get(i), x0, lineY, w0, lineH, showYear);
                    gameBox[boxIndex].setBounds(x0,lineY,w0,lineH);
                }
                lineY += lineH;
            }
            // draw load button
            int buttonY = lineY+s20;
            int buttonH = s40;
            loadBox.setBounds(button1X,buttonY,buttonW,buttonH);
            g.setColor(SystemPanel.buttonShadowC);
            g.fillRoundRect(button1X+s1,buttonY+s3,buttonW,buttonH,s8,s8);
            g.fillRoundRect(button1X+s2,buttonY+s4,buttonW,buttonH,s8,s8);
            g.setPaint(loadBackC[GameUI.opt()]);
            g.fillRoundRect(button1X,buttonY,buttonW,buttonH,s5,s5);

            String text1 = text("LOAD_GAME_OK");
            g.setFont(narrowFont(30));
            int sw1 = g.getFontMetrics().stringWidth(text1);
            int x1 = button1X + ((buttonW-sw1)/2);

            boolean hoveringLoad = (loadBox == hoverBox) && canLoad();
            Color textC = hoveringLoad ? GameUI.textHoverColor() : GameUI.textColor();
            drawShadowedString(g, text1, 0, 2, x1, buttonY+buttonH-s10, GameUI.textShade(), textC);

            if (hoveringLoad) {
                Stroke prev2 = g.getStroke();
                g.setStroke(stroke1);
                g.drawRoundRect(button1X,buttonY,buttonW,buttonH,s5,s5);
                g.setStroke(prev2);
            }

            // draw cancel button
            cancelBox.setBounds(button2X,buttonY,buttonW,buttonH);
            g.setColor(SystemPanel.buttonShadowC);
            g.fillRoundRect(button2X+s1,buttonY+s3,buttonW,buttonH,s8,s8);
            g.fillRoundRect(button2X+s2,buttonY+s4,buttonW,buttonH,s8,s8);
            g.setPaint(cancelBackC[GameUI.opt()]);
            g.fillRoundRect(button2X,buttonY,buttonW,buttonH,s5,s5);

            String text2 = text("LOAD_GAME_CANCEL");
            g.setFont(narrowFont(30));
            int sw2 = g.getFontMetrics().stringWidth(text2);
            int x2 = button2X + ((buttonW-sw2)/2);

            textC = (cancelBox == hoverBox) ? GameUI.textHoverColor() : GameUI.textColor();
            drawShadowedString(g, text2, 0, 2, x2, buttonY+buttonH-s10, GameUI.textShade(), textC);

            if (cancelBox == hoverBox) {
                Stroke prev2 = g.getStroke();
                g.setStroke(stroke1);
                g.drawRoundRect(button2X,buttonY,buttonW,buttonH,s5,s5);
                g.setStroke(prev2);
            }

            // if loading, draw notice
            if (loading) {
                NoticeMessage.setStatus(text("LOAD_GAME_LOADING"));
                drawNotice(g, 30);
            }
        }
        private void drawFilenameButton(Graphics g, int x, int y) {
            Color textC = fileNameBox == hoverBox ? GameUI.textHoverColor() : GameUI.textColor();
            g.setFont(narrowFont(20));
            String title = text("LOAD_GAME_FILENAME");
            int sw = g.getFontMetrics().stringWidth(title);
            fileNameBox.setBounds(x, y-lineH,sw,lineH);
            g.setColor(textC);
            drawString(g,title, x, y-(lineH/5));
        }
        private void drawSizeButton(Graphics g, int x, int y) {
            Color textC = fileSizeBox == hoverBox ? GameUI.textHoverColor() : GameUI.textColor();
            g.setFont(narrowFont(20));
            String title = text("LOAD_GAME_SIZE");
            int sw = g.getFontMetrics().stringWidth(title);
            fileSizeBox.setBounds(x-sw, y-lineH, sw, lineH);
            g.setColor(textC);
            drawString(g,title, x-sw, y-(lineH/5));
        }
        private void drawDateButton(Graphics g, int x, int y) {
            Color textC = fileDateBox == hoverBox ? GameUI.textHoverColor() : GameUI.textColor();
            g.setFont(narrowFont(20));
            String title = text("LOAD_GAME_DATE");
            int sw = g.getFontMetrics().stringWidth(title);
            fileDateBox.setBounds(x-sw, y-lineH, sw, lineH);
            g.setColor(textC);
            drawString(g,title, x-sw, y-(lineH/5));
        }
        private void scrollY(int deltaY) {
            yOffset += deltaY;
            if (yOffset > lineH) {
                scrollUp();
                yOffset -= lineH;
            }
            else if (yOffset < -lineH) {
                scrollDown();
                yOffset += lineH;
            }
        }
        private void drawSaveGame(Graphics2D g, int index, String filename, long sz, String dt, int x, int y, int w, int h, boolean showYear) {
            Color c0 = (index != selectIndex) && (hoverBox == gameBox[index]) ? GameUI.loadHoverBackground() : Color.black;
            g.setColor(c0);
            g.setFont(narrowFont(20));
            int yearW = showYear? s35 : 0;
            int sw0 = g.getFontMetrics().stringWidth(filename);
            int maxW = w-scaled(250)-yearW;
            int sizeRight = x+w-scaled(150)-yearW;
            int dateRight = x+w-s30;
            g.setClip(x+s25, y+h-s30, maxW, s30);
            drawString(g,filename, x+s30, y+h-s8);
            g.setClip(null);
            if (sw0 > maxW)
                drawString(g,text("LOAD_GAME_TOO_LONG"), x+s25+maxW, y+h-s8);

            String szStr = shortFmt(sz);
            int sw1 = g.getFontMetrics().stringWidth(szStr);
//            drawString(g,szStr, x+w-scaled(150)-sw1, y+h-s8);
            drawString(g,szStr, sizeRight-sw1, y+h-s8);

            int sw2 = g.getFontMetrics().stringWidth(dt);
//            drawString(g,dt, x+w-s30-sw2, y+h-s8);
            drawString(g,dt, dateRight-sw2, y+h-s8);
        }
        @Override
        public void mouseDragged(MouseEvent e) {
            mouseMoved(e);
        }
        @Override
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int deltaY = y - lastMouseY;
            lastMouseY = y;

            if (dragging && listBox.contains(x,y))
                scrollY(deltaY);

            Shape oldHover = hoverBox;
            hoverBox = null;

            if (loadBox.contains(x,y))
                hoverBox = loadBox;
            else if (cancelBox.contains(x,y))
                hoverBox = cancelBox;
            else if (fileNameBox.contains(x,y))
                hoverBox = fileNameBox;
            else if (fileDateBox.contains(x,y))
                hoverBox = fileDateBox;
            else if (fileSizeBox.contains(x,y))
                hoverBox = fileSizeBox;
            else if (saveDirBox.contains(x,y))
                hoverBox = saveDirBox;
            else if (backupDirBox.contains(x,y))
                hoverBox = backupDirBox;
            else {
                for (int i=0;i<gameBox.length;i++) {
                    if (gameBox[i].contains(x,y))
                        hoverBox = gameBox[i];
                }
            }

            if (hoverBox != oldHover)
                repaint();
        }
        @Override
        public void mouseClicked(MouseEvent arg0) {  }
        @Override
        public void mouseEntered(MouseEvent arg0) { }
        @Override
        public void mouseExited(MouseEvent arg0) {
            if (hoverBox != null) {
                hoverBox = null;
                repaint();
            }
        }
        @Override
        public void mousePressed(MouseEvent arg0) {
            dragging = true;
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            dragging = false;
            if (e.getButton() > 3)
                return;
            int count = e.getClickCount();
            if (hoverBox == null)
                return;

            if (hoverBox == loadBox) {
                loadGame(selectedFile);
                return;
            }
            if (hoverBox == fileNameBox) {
                sortByFileName();
                return;
            }
            if (hoverBox == fileSizeBox) {
                sortBySize();
                return;
            }
            if (hoverBox == fileDateBox) {
                sortByDate();
                return;
            }
            if (hoverBox == cancelBox) {
                cancelLoad();
                return;
            }
            if (hoverBox == backupDirBox) {
                if (!showingBackups) 
                    toggleSaveBackupListing();
            }
            if (hoverBox == saveDirBox) {
                if (showingBackups) 
                    toggleSaveBackupListing();
            }
            if (count == 2)
                loadGame(selectedFile);
            if (hoverBox != selectBox) {
                softClick();
                selectBox = hoverBox;
                for (int i=0;i<gameBox.length;i++) {
                    if (gameBox[i] == hoverBox)
                        selectIndex = i;
                }
                if (!saveFiles.isEmpty()) {
                    selectedFile = selectedFileName(selectIndex);
                }
                current.repaint();
            }
        }
    }
    public static Comparator<File> FILE_NAME = (File f1, File f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
    public static Comparator<File> FILE_DATE = (File f1, File f2) -> Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
    public static Comparator<File> FILE_SIZE = (File f1, File f2) -> Long.valueOf(f2.length()).compareTo(f1.length());
}
