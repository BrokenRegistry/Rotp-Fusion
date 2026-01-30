package rotp.model.empires.species;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.SOUTH;
import static java.awt.GridBagConstraints.SOUTHEAST;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static rotp.model.game.IBaseOptsTools.LIVE_OPTIONS_FILE;
import static rotp.model.game.IMainOptions.speciesDirectory;
import static rotp.ui.util.IParam.baseSeparator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import rotp.Rotp;
import rotp.model.empires.species.DNAFactory.RaceList;
import rotp.model.empires.species.SpeciesSettings.AllSpeciesAttributes;
import rotp.model.empires.species.SpeciesSettings.AvatarKey;
import rotp.model.empires.species.SpeciesSettings.PreferredShipSet;
import rotp.model.empires.species.SpeciesSettings.PrefixSufix;
import rotp.model.empires.species.SpeciesSettings.RaceKey;
import rotp.model.game.DynOptions;
import rotp.model.game.IGameOptions;
import rotp.model.ships.ShipImage;
import rotp.model.ships.ShipLibrary;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.components.RComboBox;
import rotp.ui.components.RLabel;
import rotp.ui.components.RSeparator;
import rotp.ui.components.RotPButtons;
import rotp.ui.components.RotPButtons.RButton;
import rotp.ui.components.RotPComponents;
import rotp.ui.game.GameUI;
import rotp.ui.game.GuideUI;
import rotp.ui.game.GuideUI.IGuide;
import rotp.ui.main.SystemPanel;
import rotp.ui.races.RacesUI;
import rotp.ui.util.IParam;
import rotp.util.AnimationManager;
import rotp.util.Base;
import rotp.util.FontManager;
import rotp.util.ImageManager;

public final class DNAWorkshop extends BasePanel implements RotPComponents {//, KeyListener {
	private static final long serialVersionUID = 1L;
	private static final String ROOT		= DNAFactory.ROOT;
	private static final int BUTTON_SEP_H	= s5;
	private static final int BUTTON_SEP_W	= s10;
	private static final int LEFT_MARGIN	= s20;
	private static final int RIGHT_MARGIN	= LEFT_MARGIN;
	private static final int VERTICAL_GAP	= s5;
	private static final int BORDER_WIDTH	= s15;

	private static final String	TOTAL_COST_KEY	= ROOT + "GUI_COST";
	private static final String	MALUS_COST_KEY	= ROOT + "GUI_MALUS";
	private static final Color	COST_COLOR		= SystemPanel.blackText;
	private static final Color	MALUS_COLOR		= SystemPanel.redText;
	private static final int	COST_FONT_SIZE	= 18;
	private	static final Font	DESC_FONT		= FontManager.current().narrowFont(14);
	private static final int	ANIM_DIVISOR	= 1;
	private static final int	BG_ALPHA		= 200;

	private boolean allowEdit;
	private DNAFactory dnaFactory;
	private Image backImage;

	// Panels
	private final DNAWorkshop workshop;
	private ContentPanel contentPane;
	private CostPanel	 costPanel;
	private FleetPanel	 fleetPanel;

	// private working
	private boolean oldTooltipState;
	private boolean animateFleet = true;
	private	List<Integer> spacerList;
	private List<Integer> columnList;
	private List<ICRSettings<?>> settingList, randomGeneratorList;
	private AvatarKey avatarKey;
	private PreferredShipSet prefFleet;
	private RaceKey raceKey;
	private int settingSize;
	private String emptyDescription;

	private RSettingPanel hasPopUp;
	private RaceList raceList;

	public DNAWorkshop()	{
		workshop = this;
		setName("CustomSpeciesUI");
		setOpaque(true);
	}
	public void init(boolean allowEdit) { //, DynOptions speciesOptions)	{
		this.allowEdit	= allowEdit;
		oldTooltipState	= isTooltipEnabled();
		setTooltipEnabled(false);
		setEnabled(true);
		setVisible(true);
		enterGuide(this);
		this.removeAll();
		setLayout(new GridBagLayout());
		setGuideColors(GameUI.loadHoverBackground(), Color.BLACK);

		addVariableSpace(this, 0, 0);
		contentPane	= new ContentPanel(allowEdit);
		add(contentPane, newGbc(1,1, 1,1, 0,0, SOUTHEAST, NONE, new Insets(0, 0, 0, 0), 0,0));
		fleetPanel = new FleetPanel(new Dimension(scaled(Rotp.IMG_W)*3/10, scaled(Rotp.IMG_H)*3/10));
		add(fleetPanel, newGbc(0,0, 0,2, 0,0, NORTHWEST, NONE, new Insets(s15, s25, 0, 0), 0,0));

		if (allowEdit)
			dnaFactory().setSettingTools((DynOptions) guiOptions().selectedPlayerCustomRace());
		else
			species().initCRToShow(dnaFactory()); 
		emptyDescription = htmlText("CUSTOM_RACE_EMPTY_DESCRIPTION");

		backImage = null;
		refreshPanel(true);
	}
	public void returnFromNameEditor(boolean cancelled)	{
		dnaFactory().postNameEditor(cancelled);
		setGuideColors(GameUI.loadHoverBackground(), Color.BLACK);
		refreshAll();
	}
	private RacesUI racesUI()	{ return RotPUI.instance().racesUI(); }
	private Species species()	{ return racesUI().selectedEmpire(); }
	// ========================================================================
	// #=== Main Methods
	//
	private String totalCostStr()	{ return text(TOTAL_COST_KEY, Math.round(dnaFactory().getTotalCost())); }
	private String malusCostStr()	{ return text(MALUS_COST_KEY, Math.round(dnaFactory().getMalusCost())); }
	private void initLists() {
		columnList	= dnaFactory().columnList();
		spacerList	= dnaFactory().spacerList();
		settingList	= dnaFactory().settingList();
		settingSize	= settingList.size();
		raceList	= dnaFactory().initRaceList();
		randomGeneratorList = dnaFactory().guiList();

		for (ICRSettings<?> setting : settingList) {
			if (setting instanceof AvatarKey)
				avatarKey = (AvatarKey) setting;
			if (setting instanceof PreferredShipSet)
				prefFleet = (PreferredShipSet) setting;
			if (setting instanceof RaceKey)
				raceKey = (RaceKey) setting;
		}
	}
	private void close()	{
		buttonClick();
		setTooltipEnabled(oldTooltipState);
		leaveGuide();
		descriptionPane.setActive(false);

		if (allowEdit)
			RotPUI.instance().selectSetupRacePanel();
		else
			RotPUI.instance().returnToDiplomacyPanel();

		setVisible(false);
		setEnabled(false);
		removeAll();
		randomGeneratorList	= null;
		dnaFactory	= null;
		contentPane	= null;
		costPanel	= null;
		fleetPanel	= null;
		backImage	= null;
		columnList	= null;
		spacerList	= null;
		settingList	= null;
		avatarKey	= null;
		prefFleet	= null;
		raceKey		= null;
		raceList	= null;
	}
	private void refreshAll()	{
		backImage = null;
		refreshPanel(true);
		workshop.repaint();
	}
	private void refreshPanel(boolean forced)	{ contentPane.settingsPane.updatePanel(forced); }
	private void reloadRaceList(boolean foldersRework) {
		raceList.reload(foldersRework);
		contentPane.settingsPane.gmoSelection.updateList(raceList.getListForUI());
		refreshAll();
	}
	private Rectangle getLocationOnScreen(JComponent c)	{
		return new Rectangle(c.getLocationOnScreen(), c.getSize());
	}
	private boolean isInside(MouseEvent e, JComponent c)	{
		return getLocationOnScreen(c).contains(e.getLocationOnScreen());
	}
	private Image backImage()	{
		if (backImage == null) {

			int w = getWidth();
			int h = getHeight();
			backImage = createImage(w, h);
			Graphics2D g = (Graphics2D) backImage.getGraphics();
			setRenderingHints(g);

			// draw avatar
			if (!allowEdit) {
				// draw background image
				BufferedImage labImg = species().laboratory();
				g.drawImage(labImg, 0,0, w, h, 0, 0, labImg.getWidth(), labImg.getHeight(), null);

				// draw avatar
				species().resetScientist();
				Image avatarImg = species().scientistQuiet();
				g.drawImage(avatarImg, 0,2*h/6, 4*w/6,h, 0,0, avatarImg.getWidth(this), avatarImg.getHeight(this), null);
			}
			else if (avatarKey.isDefaultValue()) {
				// draw background image
				String key = random(new String[] {"LANDSCAPE_RUINS_ANTARAN", "LANDSCAPE_RUINS_ORION", "DERELICT_SHIP"});
				Image ruinImg = ImageManager.current().image(key);
				g.drawImage(ruinImg, 0,0, w, h, 0, 0, ruinImg.getWidth(this), ruinImg.getHeight(this), null);
			}
			else {
				Race avatar = Species.getAnim(avatarKey.settingValue());
				// draw background image
				BufferedImage labImg = avatar.laboratory();
				g.drawImage(labImg, 0,0, w, h, 0, 0, labImg.getWidth(), labImg.getHeight(), null);

				// draw avatar
				avatar.resetScientist();
				Image avatarImg = avatar.scientistQuiet();
				g.drawImage(avatarImg, 0,2*h/6, 4*w/6,h, 0,0, avatarImg.getWidth(this), avatarImg.getHeight(this), null);
			}

			// draw Title
			String title = guiTitle();
			g.setFont(narrowFont(50));
			int sw = g.getFontMetrics().stringWidth(title);
			int x = w*2/3 - sw/2;
			drawBorderedString(g, title, 2, x, s60, Color.darkGray, Color.white);

			g.dispose();
		}
		return backImage;
	}

	@Override public void paintComponent(Graphics g)	{
		super.paintComponent(g);
		setHiRenderingHints(g);
		int w = getWidth();
		int h = getHeight();
		g.drawImage(backImage(), 0, 0, w, h, this);
	}
	@Override public void keyReleased(KeyEvent e)	{
		switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				setModifierKeysState(e);
				close();
				return;
			case KeyEvent.VK_G:
				IGuide.buttonGuideAction(null);
				return;
			default:
				super.keyReleased(e);
		}
	}
	@Override public String ambienceSoundKey()	{ return allowEdit? "UnspecifiedAction" : "ResearchAmbience"; }
	@Override public JComponent getComponent()	{ return this; }
	@Override public void animate()				{
		if (AnimationManager.current().playAnimations())
			if (animateFleet && animationCount() % ANIM_DIVISOR == 0)
				fleetPanel.repaint();
	}
	// -#-
	// ========================================================================
	// #=== getters and setters
	//
	private void dnaFactory(DNAFactory sf)	{ this.dnaFactory = sf; }
	private DNAFactory dnaFactory()	{
		if (dnaFactory == null)
			dnaFactory(DNAFactory.getSkillsFactoryForEditor(this));
		return dnaFactory;
	}
	private boolean isGMO()		{ return racesUI().selectedEmpire().isCustomSpecies(); }
	private boolean isRandom()	{ return racesUI().selectedEmpire().isRandomized(); }
	private String guiTitleID()	{
		if (allowEdit)
			return ROOT + "GUI_TITLE";
		else
			if (isRandom())
				return ROOT + "SHOW_TITLE_RANDOM";
			else if (isGMO())
				return ROOT + "SHOW_TITLE_CUSTOM";
			else
				return ROOT + "SHOW_TITLE";
	}
	private String guiTitle()	{ return text(guiTitleID()); }
	// -#-
	// ========================================================================
	// #=== Event calls
	//
	@Override public void cancelHelp()	{
		if (hasPopUp != null)
			hasPopUp.highLighted(false);
		hasPopUp = null;
	}
	@Override public void advanceHelp()	{ 
		if (hasPopUp != null)
			hasPopUp.highLighted(false);
		hasPopUp = null;
	}
	private final class SettingListener extends MouseAdapter	{
		@Override public void mouseClicked(MouseEvent e)	{ }
		@Override public void mousePressed(MouseEvent e)	{ }
		@Override public void mouseReleased(MouseEvent e)	{ toggle(e); }
		@Override public void mouseEntered(MouseEvent e)	{ setDescBox(e); }
		@Override public void mouseExited(MouseEvent e)		{ clearDescBox(e); }
		@Override public void mouseDragged(MouseEvent e)	{ }
		@Override public void mouseMoved(MouseEvent e)		{ }
		@Override public void mouseWheelMoved(MouseWheelEvent w)	{ toggle(w); }

		private RSettingPanel panel(MouseEvent e)		{ return (RSettingPanel) e.getComponent(); }
		private void toggle(MouseWheelEvent w)	{
			if (allowEdit) {
				RSettingPanel panel = panel(w);
				ICRSettings<?> setting = panel.setting;
				if (setting instanceof AvatarKey || setting instanceof PreferredShipSet) {
					setting.toggle(w);
					backImage = null;
					panel.updateGui(true);
					workshop.repaint();
					setDescBox(w);
					return;
				}
				if (setting instanceof SettingString)
					return;
				else if (setting.toggle(w)) {
					refreshPanel(false);
					setDescBox(w);
					return;
				}
				panel.updateGui(true);
				setDescBox(w);
				if (!setting.hasNoCost()) {
					costPanel.updateCost();
					costPanel.paintImmediately();
					refreshPanel(false);
				}
			}
		}
		private void toggle(MouseEvent e)	{
			if (allowEdit) {
				RSettingPanel panel = panel(e);
				ICRSettings<?> setting = panel.setting;
				if (setting instanceof AvatarKey || setting instanceof PreferredShipSet) {
					setting.toggle(e, workshop);
					backImage = null;
					panel.updateGui(true);
					workshop.repaint();
					setDescBox(e);
					return;
				}
				if (setting instanceof PrefixSufix) {
					setting.toggle(e, workshop);
					return;
				}
				if (setting instanceof SettingString) {
					selectNamesAction(null);
					return;
				}
				if (setting.isBullet())
					panel.setBullet(e);
				else
					setting.toggle(e, workshop);
				panel.updateGui(true);
				refreshPanel(false);
				setDescBox(e);

				if (!setting.hasNoCost()) {
					costPanel.updateCost();
					costPanel.paintImmediately();
					refreshPanel(false);
				}
			}
		}
		private void setDescBox(MouseEvent e)	{
			RSettingPanel panel = panel(e);
			ICRSettings<?> setting = panel.setting;
			if (hasPopUp != null && panel != hasPopUp)
				hasPopUp.highLighted(false);
			hasPopUp = panel;
			panel.highLighted(true);
			String description = setting.getGuiDescription();
			descriptionPane.setText(description);
			GuideUI.open(panel, setting.getGuide());
		}
		private void clearDescBox(MouseEvent e)	{
			RSettingPanel panel = panel(e);
			if (!isVisible() || !panel.isVisible()) {
				panel.highLighted(false);
				GuideUI.close();
				descriptionPane.setText(emptyDescription);
				hasPopUp = null;
			}
			else if (!isInside(e, panel)) {
				panel.highLighted(false);
				GuideUI.close();
				descriptionPane.setText(emptyDescription);
				hasPopUp = null;
			}
			panel.setting.updateGui();
		}
		private void selectNamesAction(ActionEvent e)	{
			AllSpeciesAttributes settings = dnaFactory().preNameEditor();
			RotPUI.instance().selectNameEditorPanel(workshop, settings);
			workshop.setVisible(false);
			workshop.setEnabled(false);
		}
	}
	// -#-
	// ========================================================================
	// #=== Level 1: Content Panel (set as content pane to be able to access paintComponent to gives a background
	//
	private class ContentPanel extends BasePanel {
		private static final long serialVersionUID = 1L;
		private static final String NAME = "MainPanel";
		private SettingsPane settingsPane;

		ContentPanel(boolean allowEdit)	{
			setOpaque(false);
			setName(NAME);
			initLists();

			setLayout(new GridBagLayout());
			GridBagConstraints gbc;
			int x = 0;
			int y = 0;

			settingsPane = new SettingsPane(allowEdit);
			gbc = newGbc(x,y, 1,1, 0,0, EAST, NONE, new Insets(0, LEFT_MARGIN, 0, RIGHT_MARGIN), 0,0);
			add(settingsPane, gbc);

			// Description
			y++;
			gbc = newGbc(x,y, 1,1, 0,0, EAST, HORIZONTAL, new Insets(VERTICAL_GAP, LEFT_MARGIN, 0, RIGHT_MARGIN), 0,0);
			Color bgC = Base.setAlpha(GameUI.raceCenterColor(), BG_ALPHA);
			descriptionPane.init(bgC, SystemPanel.blackText, DESC_FONT, emptyDescription, 1, s41);
			descriptionPane.setActive(true);
			add(descriptionPane, gbc);

			// Bottom buttons
			y++;
			gbc = newGbc(x,y, 1,1, 0,0, EAST, HORIZONTAL, new Insets(VERTICAL_GAP, LEFT_MARGIN, VERTICAL_GAP, RIGHT_MARGIN), 0,0);
			add(new BottomPane(allowEdit), gbc);

			y++;
			BufferedImage blankLine = new BufferedImage(s20, s10, TYPE_INT_ARGB);
			JLabel picLabel = new JLabel(new ImageIcon(blankLine));
			add(picLabel, newGbc(x,y, 1,1, 0,0, EAST, HORIZONTAL, ZERO_INSETS, 0,0));
		}
	}
	// -#-
	// ========================================================================
	// === Level 2: ==> Settings Panel
	//
	private class SettingsPane extends JPanel	{
		private static final long serialVersionUID = 1L;

		private BufferedImage backImg;
		private int width, height;
		private List<RSettingPanel> settingPanelList = new ArrayList<>();
		private RComboBox<String> gmoSelection;

		SettingsPane(boolean allowEdit)	{ buildPanel(allowEdit); }
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int w = getWidth();
			int h = getHeight();
			setHiRenderingHints(g);
			g.drawImage(backImg(), 0, 0, w, h, this);
		}
		private void buildPanel(boolean allowEdit) {
			setOpaque(false);
			setLayout(new GridBagLayout());
			Color borderColor = Base.setAlpha(GameUI.setupShade(), BG_ALPHA/2);
			Border border = BorderFactory.createLineBorder(borderColor, BORDER_WIDTH);
			setBorder(border);

			// Min Size
			add(new JLabel(), newGbc(0, 0, 4,2, 0,0, CENTER, NONE, new Insets(scaled(540), scaled(680), 0, 0), 0,0));

			int side = s10;
			int[] settingWidth	= {scaled(160), scaled(160), scaled(160), scaled(160)};
			int[] subPanelWidth	= {scaled(180), scaled(180), scaled(180), scaled(180)};
			int x = 0;
			int y = 0;
			JPanel  subPanel = newSubPanel(subPanelWidth[x]);
			subPanel.setBorder(new EmptyBorder(s5, side, s5, 0));
			add(subPanel, newGbc(0,1, 1,1, 1,0, NORTH, NONE, ZERO_INSETS, 0,0));

			if (allowEdit) {
				RandomizePane rg = new RandomizePane();
				add(rg, newGbc(0,1, 1,1, 1,0, SOUTH, NONE, new Insets(0, side, side, 0), 0,0));

				add(newFolderButton(), newGbc(x, y, 1,1, 0,0, CENTER, NONE, new Insets(side, 0, 0, 0), 0,0));
				y++;
				gmoSelection = newGMOSelection();
				subPanel.add(gmoSelection, newGbc(x, y, 1,1, 0,0, CENTER, HORIZONTAL, new Insets(s5, 0, side, 0), 0,0));
			}

			Insets bulletInset = new Insets(0, 0, s5, 0);
			costPanel = new CostPanel(settingWidth[x]);
			add(costPanel, newGbc(1,1, 2,1, 1,0, SOUTH, HORIZONTAL, new Insets(0, side, side, 0), 0,0));
			y++;

			for (int i=0; i<settingSize; i++) {
				if (spacerList.contains(i)) {
					new RSeparator(subPanel, true, s2, null, 0, y ,0, s4, GameUI.loadHoverBackground());
					y++;
					//continue;
				}
				if (columnList.contains(i)) {
					x+=1;
					y = 0;
					subPanel = newSubPanel(subPanelWidth[x]);
					subPanel.setBorder(new EmptyBorder(side, 0, s5, x==3? side:0));
					add(subPanel, newGbc(x,0, 1,2, 1,0, NORTH, HORIZONTAL, new Insets(0, side, 0, 0), 1,0));
					//continue;
				}
				ICRSettings<?> setting = settingList.get(i);
				RSettingPanel settingPanel = new RSettingPanel(this, setting, settingWidth[x], new SettingListener());
				settingPanelList.add(settingPanel);
				if (setting.isBullet())
					subPanel.add(settingPanel, newGbc(0,y, REMAINDER,1, 1,0, WEST, HORIZONTAL, bulletInset, 0,0));
				else
					subPanel.add(settingPanel, newGbc(0,y, REMAINDER,1, 1,0, WEST, HORIZONTAL, ZERO_INSETS, 0,0));
				y++;
			}
		}
		private RComboBox<String> newGMOSelection()	{
			RComboBox<String> box = new RComboBox<>(raceList);
			box.setPopupLocation(SwingConstants.SOUTH);
			box.enableArrow(true);
			box.setMaximumRowCount(30);
			box.addActionListener(e -> gmoSelectionAction(e));
			return box;
		}
		private void gmoSelectionAction(ActionEvent evt)	{
			String prevValue = raceList.settingValue();
			String selection = (String) gmoSelection.getSelectedItem();
			raceList.selectedValue(selection);
			// Test validity
			if (!raceList.settingValue().equals(selection)) {
				Toolkit.getDefaultToolkit().beep();
				reloadRaceList(false);
				raceList.selectedValue(prevValue);
			}
			refreshAll();
		}
		private void updatePanel(boolean forced)	{
			costPanel.updateCost();
			for(RSettingPanel pane : settingPanelList)
					pane.updateGui(forced);
		}
		private JPanel newSubPanel(int minWidth)	{
			JPanel panel = new JPanel();
			JLabel minWidthLabel = new JLabel();
			minWidthLabel.setPreferredSize(new Dimension(minWidth, 1));
			panel.add(minWidthLabel);
			panel.setOpaque(false);
			panel.setLayout(new GridBagLayout());
			panel.setAlignmentY(0);
			panel.setAlignmentX(0);
			return panel;
		}
		private BufferedImage backImg()	{
			int w = getWidth();
			int h = getHeight();
			if (backImg == null || w != width || h != height)
				initBackImg(w, h, BG_ALPHA);
			return backImg;
		}
		private void initBackImg(int w, int h, int alpha)	{
			width = w;
			height = h;
			backImg = new BufferedImage(w, h, TYPE_INT_ARGB);;
			Graphics2D g = (Graphics2D) backImg.getGraphics();
			setHiRenderingHints(g);
			g.setPaint(GameUI.modBackground(0, w, alpha));
			g.fillRect(0, 0, w, h);

			g.dispose();
		}
		private RButton newFolderButton()	{
			RButton button = new RButton(ROOT + "BUTTON_FOLDER", RotPButtons.DEFAULT_FONT_SIZE);
			button.setMinimumSize(new Dimension(scaled(125), s10));
			button.setParam(speciesDirectory);
			button.setParent(workshop);
			button.setLabelKey();
			button.setListener(e -> selectFolderAction(e));
			return button;
		}
		private String selectFolderAction(MouseEvent e)	{
			if (e.getID() == MouseEvent.MOUSE_RELEASED) {
				if (e.isControlDown())
					reloadRaceList(true);
				else
					reloadRaceList(false);
				dnaFactory().loadRace();
				repaint();
			}
			return null;
		}
	}
	// ========================================================================
	// === Level 2: ==> BottomPane
	//
	private class BottomPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private BottomPane(boolean allowEdit) {
			setOpaque(false);
			setLayout(new GridBagLayout());
			int x = 0;
			int y = 0;
			add(newGuideButton(true), newGbc(x, y, 1,1, 0,0, EAST, NONE, new Insets(0, 0, BUTTON_SEP_H, BUTTON_SEP_W), 0,0));

			x++;
			addVariableSpace(this, x, y);

			if (allowEdit) {
				x++;
				add(newLoadPlayerButton(), newGbc(x, y, 1,1, 0,0, CENTER, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, 0), 0,0));
				x++;
				add(newSetPlayerButton(), newGbc(x, y, 1,1, 0,0, CENTER, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, 0), 0,0));
				x++;
				add(newSaveButton(), newGbc(x, y, 1,1, 0,0, CENTER, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, BUTTON_SEP_W), 0,0));
			}

			x++;
			addVariableSpace(this, x, y);

			x++;
			add(newExitButton(), newGbc(x, y, 1,1, 0,0, WEST, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, 0), 0,0));
		}
		private RButton newExitButton()		{
			RButton button = RotPButtons.newBigButton(ROOT + "BUTTON_EXIT", true);
			button.setLabelKey();
			button.setMinimumSize(new Dimension(s150, s30));
			button.addActionListener(evt -> exitAction(evt));
			return button;
		}
		private void exitAction(ActionEvent e)	{ close(); }

		private RButton newLoadPlayerButton()	{
			RButton button = RotPButtons.newBigButton(ROOT + "LOAD_PLAYER", true);
			button.setMinimumSize(new Dimension(s150, s30));
			button.setLabelKey();
			button.addActionListener(evt -> loadPlayerAction(evt));
			return button;
		}
		private void loadPlayerAction(ActionEvent e)	{
			buttonClick();
			IGameOptions opts = guiOptions();
			DynOptions player = opts.selectedPlayerCustomRace();
			dnaFactory().initSkillsForEditor(player);
			refreshAll();
		}

		private RButton newSetPlayerButton()	{
			RButton button = RotPButtons.newBigButton(ROOT + "GUI_SELECT", true);
			button.setMinimumSize(new Dimension(s150, s30));
			button.setLabelKey();
			button.addActionListener(evt -> setPlayerAction(evt));
			return button;
		}
		private void setPlayerAction(ActionEvent e)	{
			buttonClick();
			IGameOptions opts = guiOptions();
			DynOptions player = dnaFactory().getAsOptions();
			opts.selectedPlayerCustomRace(player);
			opts.selectedPlayerIsCustom(true);
			String anim = dnaFactory().getPlayerAnim();
			if (anim != null)
				opts.selectedPlayerRace(anim);
			opts.saveOptionsToFile(LIVE_OPTIONS_FILE);
			dnaFactory().saveRace();
			reloadRaceList(false);
			dnaFactory().initSkillsForEditor(player);
			refreshAll();
		}

		private RButton newSaveButton()	{
			RButton button = RotPButtons.newBigButton(ROOT + "GUI_SAVE", true);
			button.setMinimumSize(new Dimension(s150, s30));
			button.setLabelKey();
			button.addActionListener(evt -> saveAction(evt));
			return button;
		}
		private void saveAction(ActionEvent e)	{
			String currentSpecies = raceKey.settingValue();
			dnaFactory().saveRace();
			reloadRaceList(false);
			raceList.selectedValue(currentSpecies);
			refreshAll();
		}

	}
	// ========================================================================
	// === Level 3: ==> Cost Panel
	//
	private class CostPanel extends JPanel implements RotPComponents	{
		private static final long serialVersionUID = 1L;
		RLabel totalCost, malusCost;
		private CostPanel(int width)	{
			setOpaque(false);
			Border border = BorderFactory.createLineBorder(SystemPanel.blackText,1);
			setBorder(border);
			setLayout(new GridBagLayout());
			setAlignmentY(0);
			setAlignmentX(0);
			BufferedImage blankLine = new BufferedImage(width, 1, TYPE_INT_ARGB);
			JLabel picLabel = new JLabel(new ImageIcon(blankLine));
			add(picLabel, newGbc(0,0, 2,0, 0,0, NORTH, NONE, new Insets(0, 0, 0, 0), 0,0));

			totalCost = new RLabel (totalCostStr(), COST_FONT_SIZE, COST_COLOR);
			totalCost.setOpaque(false);
			add(totalCost, newGbc(0,0, 1,0, 0,0, EAST, NONE, new Insets(0, 0, 0, 0), 0,0));

			malusCost = new RLabel (malusCostStr(), COST_FONT_SIZE, MALUS_COLOR);
			malusCost.setOpaque(false);
			add(malusCost, newGbc(1,0, 1,0, 0,0, WEST, NONE, new Insets(0, 0, 0, 0), 0,0));
		}
		private void updateCost()	{
			totalCost.setText(totalCostStr());
			malusCost.setText(malusCostStr());
		}
		@Override public JComponent getComponent()	{ return this; }
	}
	// ========================================================================
	// === Level 3: ==> Random Generator Panel
	//
	private class RandomizePane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private List<RSettingPanel> settingPanelList = new ArrayList<>();
		private RandomizePane() {
			setOpaque(false);
			Border border = new LineBorder(Color.BLACK, 1);
			Border margin = new EmptyBorder(s10, s10, s10, s10);
			setBorder(new CompoundBorder(border, margin));
			setLayout(new GridBagLayout());
			int y = 0;
			add(newRandomGetButton(), newGbc(0, y, 1,1, 0,0, WEST, NONE, new Insets(0, 0, s10, 0), 0,0));
			add(newRandomPushButton(), newGbc(1, y, 1,1, 0,0, EAST, NONE, new Insets(0, 0, s10, 0), 0,0));

			int settingWidth = scaled(180);
			for (ICRSettings<?> setting : randomGeneratorList) {
				RSettingPanel settingPanel = new RSettingPanel(this, setting, settingWidth, new SettingListener());
				y++;
				add(settingPanel, newGbc(0,y, REMAINDER,1, 1,0, WEST, HORIZONTAL, ZERO_INSETS, 0,0));
				settingPanelList.add(settingPanel);
			}

			y++;
			add(newRandomizeButton(), newGbc(0, y, 2,1, 0,0, EAST, NONE, new Insets(s5, 0, 0, 0), 0,0));
		}
		private RButton newRandomGetButton()	{
			RButton button = RotPButtons.newButton(ROOT + "GUI_RANDOM_GET");
			button.setLabelKey();
			button.setMinimumSize(new Dimension(s65, s10));
			button.addActionListener(e -> randomGetAction());
			return button;
		}
		private RButton newRandomPushButton()	{
			RButton button = RotPButtons.newButton(ROOT + "GUI_RANDOM_PUSH");
			button.setLabelKey();
			button.setMinimumSize(new Dimension(s65, s10));
			button.addActionListener(e -> randomPushAction());
			return button;
		}
		private RButton newRandomizeButton()	{
			RButton button = RotPButtons.newBigButton(ROOT + "GUI_RANDOM", true);
			button.setMinimumSize(new Dimension(scaled(158), s10));
			button.setLabelKey();
			button.addActionListener(e -> randomizeAction());
			return button;
		}
		private void randomGetAction()	{
			IGameOptions opts = guiOptions();
			dnaFactory().randomTargetMax.set(opts.randomAlienRacesTargetMax());
			dnaFactory().randomTargetMin.set(opts.randomAlienRacesTargetMin());
			dnaFactory().randomMax.set(opts.randomAlienRacesMax());
			dnaFactory().randomMin.set(opts.randomAlienRacesMin());
			dnaFactory().randomUseTarget.set(opts.randomAlienRacesUseTarget());
			dnaFactory().randomSmoothEdges.set(opts.randomAlienRacesSmoothEdges());
			refreshPanel(true);
		}
		private void randomPushAction()	{
			IGameOptions opts = guiOptions();
			opts.randomAlienRacesTargetMax(dnaFactory().randomTargetMax.settingValue());
			opts.randomAlienRacesTargetMin(dnaFactory().randomTargetMin.settingValue());
			opts.randomAlienRacesMax(dnaFactory().randomMax.settingValue());
			opts.randomAlienRacesMin(dnaFactory().randomMin.settingValue());
			opts.randomAlienRacesUseTarget(dnaFactory().randomUseTarget.settingValue());
			opts.randomAlienRacesSmoothEdges(dnaFactory().randomSmoothEdges.settingValue());
		}
		private void randomizeAction()	{
			dnaFactory().randomizeRace(false);
			refreshPanel(true);
		}
	}
	// -#-
	// ========================================================================
	// #=== Other Specific Components definition : GMO Selection
	//
	// -#-
	// ========================================================================
	// #=== Other Classes: FleetPanel
	//
	private final class FleetPanel extends BasePanel implements RotPComponents, MouseListener, MouseWheelListener	{
		private static final long serialVersionUID = 1L;
		private static final int DRAG = 10;
		private Dimension size;
		private ShipLibrary sl = ShipLibrary.current();

		private FleetPanel(Dimension size)	{
			this.size = size;
			setOpaque(false);
			setAlignmentY(0);
			setAlignmentX(0);

			setVisible(true);
			setEnabled(true);
			addMouseListener(this);
			addMouseWheelListener(this);
		}
		@Override public Dimension getPreferredSize()		{ return size; }
		@Override public void paintComponent(Graphics g)	{
			if (prefFleet.isDefaultValue())
				return;
			setRenderingHints(g);
			int w = getWidth();
			int h = getHeight();

			BufferedImage fleetImage = fleetImage(prefFleet.index(), w, h);
			g.drawImage(fleetImage, 0,0, this);
		}
		@Override public IParam<?> getParam()				{ return prefFleet; }
		@Override public JComponent getComponent()			{ return this; }
		@Override public void mouseClicked(MouseEvent evt)	{ }
		@Override public void mousePressed(MouseEvent evt)	{ }
		@Override public void mouseReleased(MouseEvent evt)	{
			if (SwingUtilities.isRightMouseButton(evt))
				prefFleet.toggle(evt, workshop);
			else if (SwingUtilities.isMiddleMouseButton(evt))
				prefFleet.toggle(evt, workshop);
			else if (evt.isControlDown())
				prefFleet.toggle(evt, workshop);
			else
				animateFleet = !animateFleet;
			mouseEntered(evt);
		}
		@Override public void mouseEntered(MouseEvent evt)	{
			String desc = text(ROOT + "FLEET_HELP");
			popGuide(prefFleet.getGuide() + baseSeparator() + desc, -s36, -s36);
			setDescription(desc);
		}
		@Override public void mouseExited(MouseEvent evt)	{
			hideGuide();
			clearDescription();
		}
		@Override public void mouseWheelMoved(MouseWheelEvent evt)	{
			prefFleet.toggle(evt);
			mouseEntered(evt);
		}

		private BufferedImage fleetImage(int shipStyle, int w0, int h0)	{
			BufferedImage fleetImg = new BufferedImage(w0, h0, TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) fleetImg.getGraphics();
			setRenderingHints(g);

			// Star background
			g.setColor(starBackgroundC());
			g.fillOval(0, 0, w0, h0);
			g.setClip(new Ellipse2D.Float(0, 0, w0, h0));	
			if(animationCount() % DRAG == 0)
				starScrollX++;
			drawStars(g, w0, h0);

			// Add cloud as nebulae
			int rule = AlphaComposite.SRC_OVER;
			float alpha = 0.25f;
			AlphaComposite comp = AlphaComposite.getInstance(rule , alpha);
			Composite oldComp = g.getComposite();
			g.setComposite(comp);
			Image cloud = icon("images/planets/Clouds_Inferno_02.png").getImage();
			g.drawImage(cloud, 0,0, w0,h0, 0,0, cloud.getWidth(this), cloud.getHeight(this), this);
			g.setComposite(oldComp);

			int cx = w0/2;
			int dx = w0/4;
			//int bx = w0/3;
			int ex = 2*w0/5;
			int sx = w0/8;
			int cy = h0/2;
			int by = h0/3;
			int dy = h0/4;
			int sy = h0/10;
			int ty = h0/12;
			// Central Ship
			int shipH[]	= new int[] {h0/8,	h0/8,	h0/8,	h0/8,	h0/8,	h0/8,	h0/8,	h0/8,	h0/10,	h0/10,	h0/10,	h0/10};
			int shipY[]	= new int[] {cy,	cy,		cy-by,	cy+by,	cy-dy,	cy+dy,	cy-dy,	cy+dy,	cy-sy,	cy+sy,	cy-ty,	cy+ty};
			int shipX[]	= new int[] {cx+sx,	cx-sx,	cx,		cx,		cx+dx,	cx+dx,	cx-dx,	cx-dx,	cx-ex,	cx-ex,	cx+ex,	cx+ex};
			int size[]	= new int[] {3,		3,		3,		3,		2,		2,		1,		1,		2,		2,		0,		0};
			int shape[]	= new int[] {4,		3,		5,		5,		2,		2,		1,		1,		3,		3,		0,		0};

			for (int i=0; i<shipH.length; i++)
				drawImage(g, shipStyle, shape[i], size[i], shipH[i], shipX[i], shipY[i], i);

			g.setClip(null);
			g.setStroke(stroke2);
			g.setColor(new Color(100,161,231));
			g.drawOval(1, 1, w0-2, h0-2);

			g.dispose();
			return fleetImg;
		}
		private void drawImage(Graphics2D g0, int shipStyle, int shapeId, int shipSize, int shipHeight, int x0, int y0, int i)	{
			int shipWidth = (int) (shipHeight * 1.3314f);
			BufferedImage shipImg = getShipImage(shipStyle, shapeId, shipSize, shipWidth, shipHeight, i);
			int x = x0 - shipImg.getWidth(this)/2 + s5;
			int y = y0 - shipImg.getHeight(this)/2;
			g0.drawImage(shipImg, x,y, this);
		}
		private BufferedImage getShipImage(int shipStyle, int shapeId, int shipSize, int shipWidth, int shipHeight, int i)	{
			ShipImage images = sl.shipImage(shipStyle, shipSize, shapeId);
			Image img = icon(images.animIcon(i)).getImage();
			int w0 = img.getWidth(null);
			int h0 = img.getHeight(null);
			float scale = min(shipWidth*2f/w0, shipHeight*2f/h0);
			int w1 = (int)(scale*w0);
			int h1 = (int)(scale*h0);
			BufferedImage resizedImg = new BufferedImage(w1,h1, TYPE_INT_ARGB);
			Graphics2D g = resizedImg.createGraphics();
			// modnar: one-step progressive image downscaling, mostly for Sakkra ships (higher-res image files)
			// there should be better methods
			if (scale < 0.5) {
				BufferedImage tmp = new BufferedImage(w0/2, h0/2, TYPE_INT_ARGB);
				Graphics2D g2D = tmp.createGraphics();
				setHiRenderingHints(g2D);
				g2D.drawImage(img, 0, 0, w0/2, h0/2, 0, 0, w0, h0, this);
				g2D.dispose();
				img = tmp;
				w0 = img.getWidth(null);
				h0 = img.getHeight(null);
				scale = scale*2;
			}
			setHiRenderingHints(g);
			g.drawImage(img, 0, 0, w1, h1, 0, 0, w0, h0, null);
			g.dispose();
			return resizedImg;
		}
	}
	// -#-
}
