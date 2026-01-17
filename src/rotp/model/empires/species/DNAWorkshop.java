package rotp.model.empires.species;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.SOUTHEAST;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static rotp.model.game.IMainOptions.showGuide;
import static rotp.model.game.IRaceOptions.defaultRaceKey;

import java.awt.Color;
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
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import rotp.model.empires.species.SpeciesSettings.AvatarKey;
import rotp.model.empires.species.SpeciesSettings.SettingMap;
import rotp.model.game.DynOptions;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.components.RLabel;
import rotp.ui.components.RSeparator;
import rotp.ui.components.RotPButtons.RButton;
import rotp.ui.components.RotPButtons.RHugeButton;
import rotp.ui.components.RotPComponents;
import rotp.ui.components.RotPPanels.RPanel;
import rotp.ui.game.GameUI;
import rotp.ui.game.GuideUI;
import rotp.ui.main.SystemPanel;
import rotp.util.Base;
import rotp.util.FontManager;

public final class DNAWorkshop extends BasePanel implements RotPComponents {//, KeyListener {
	private static final long serialVersionUID = 1L;
	private static final String ROOT		= SkillsFactory.ROOT;
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
	private static final Color	SEPARATOR_COLOR	= ICRSettings.optionC;
	private static final int	COST_FONT_SIZE	= 18;
	private	static final Font	DESC_FONT		= FontManager.current().narrowFont(14);

	private boolean allowEdit;
	private BasePanel parent;

	private SkillsFactory dnaFactory;
	private SettingMap settingMap;
	private Image backImage;
	private int backGroundAlpha = 200;

	// Panels
	private final DNAWorkshop workshop;
	private ContentPanel contentPane;	// TODO BR: Check if removable
	private SettingsPane settingsPane;	// TODO BR: Check if removable
	private CostPanel costPanel;
	private DescriptionPane descriptionPane;

	// private working
	private boolean canceled = false;
	private boolean oldTooltipState;

	//
	private	List<Integer> spacerList;
	private List<Integer> columnList;
	List<ICRSettings> commonList;
	private List<ICRSettings> settingList;
	private int settingSize;
	private String emptyDescription;

	private RSettingPanel hasPopUp;

	public DNAWorkshop()	{
		workshop = this;
		setName("CustomSpeciesUI");
		setOpaque(true);
		//sf(SkillsFactory.getSkillsFactoryForEditor(this));
		setLayout(new GridBagLayout());
	}
	public void init(BasePanel parent, boolean allowEdit)	{
		this.parent		= parent;
		this.allowEdit	= allowEdit;
		oldTooltipState	= isTooltipEnabled();
		setTooltipEnabled(false);
		
		setEnabled(true);
		setVisible(true);
		this.removeAll();

		addVariableSpace(this, 0, 0);
		contentPane	= new ContentPanel(allowEdit);
		add(contentPane, newGbc(1,1, 1,1, 0,0, EAST, NONE, new Insets(0, 0, 0, 0), 0,0));

		dnaFactory().setSettingTools((DynOptions) guiOptions().selectedPlayerCustomRace());
		settingMap	= dnaFactory().settingMap;
		commonList	= settingMap.getSettings();
		emptyDescription = htmlText("CUSTOM_RACE_EMPTY_DESCRIPTION");

		backImage = null;
		repaint();
	}
	// ========================================================================
	// #=== Main Methods
	//
	private String totalCostStr()	{ return text(TOTAL_COST_KEY, Math.round(dnaFactory().getTotalCost())); }
	private String malusCostStr()	{ return text(MALUS_COST_KEY, Math.round(dnaFactory().getMalusCost())); }
	private void initLists() {
		commonList	= settingList;
		columnList	= dnaFactory().columnList();
		spacerList	= dnaFactory().spacerList();
		settingList	= dnaFactory().settingList();
		settingSize	= settingList.size();
	}
	private Rectangle getLocationOnScreen(JComponent c)	{
		return new Rectangle(c.getLocationOnScreen(), c.getSize());
	}
	private boolean isInside(MouseEvent e, JComponent c)	{
		return getLocationOnScreen(c).contains(e.getLocationOnScreen());
	}
	private Image backImage()	{
		if (backImage == null) {
			AvatarKey avatarKey = (AvatarKey) settingList.get(0);
			Race avatar;
			if (avatarKey.isDefaultValue())
				avatar = Species.getAnim(defaultRaceKey);
			else
				avatar = Species.getAnim(avatarKey.settingValue());

			BufferedImage labImg = avatar.laboratory();
			Image avatarImg = avatar.scientistQuiet();
			int w = getWidth();
			int h = getHeight();
			backImage = createImage(w, h);

			Graphics2D g = (Graphics2D) backImage.getGraphics();
			setRenderingHints(g);

			// draw background image
			g.drawImage(labImg, 0,0, w, h, 0, 0, labImg.getWidth(), labImg.getHeight(), null);

			// draw avatar
			g.drawImage(avatarImg, 0,2*h/5, 3*w/5,h, 0,0, avatarImg.getWidth(this), avatarImg.getHeight(this), null);

			// draw Title
			String title = guiTitle();
			g.setFont(narrowFont(50));
			int sw = g.getFontMetrics().stringWidth(title);
			int x = (w - sw)/2;
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
			default:
				super.keyReleased(e);
		}
	}
	@Override public String ambienceSoundKey()	{ return "ResearchAmbience"; }
	@Override public JComponent getComponent()	{ return this; }
	@Override public void animate()		{ } // TODO BR:
	// -#-
	// ========================================================================
	// #=== getters and setters
	//
	private void dnaFactory(SkillsFactory sf)	{ this.dnaFactory = sf; }
	private SkillsFactory dnaFactory()	{
		if (dnaFactory == null)
			dnaFactory(SkillsFactory.getSkillsFactoryForEditor(this));
		return dnaFactory;
	}
	private boolean isGMO()		{ return false; } // TODO BR: isCustomSpecies()
	private String guiTitleID()	{
		if (allowEdit)
			return ROOT + "GUI_TITLE";
		else
			if (isGMO())
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
	final class SettingListener extends MouseAdapter	{
		@Override public void mouseClicked(MouseEvent e)	{ }
		@Override public void mousePressed(MouseEvent e)	{ }
		@Override public void mouseReleased(MouseEvent e)	{ toggle(e); }
		@Override public void mouseEntered(MouseEvent e)	{ setDescBox(e); }
		@Override public void mouseExited(MouseEvent e)		{ clearDescBox(e); }
		@Override public void mouseDragged(MouseEvent e)	{ }
		@Override public void mouseMoved(MouseEvent e)		{ }
		@Override public void mouseWheelMoved(MouseWheelEvent w)	{ toggle(w); }

		private RSettingPanel panel(MouseEvent e)		{ return (RSettingPanel) e.getComponent(); }
//		private ICRSettings getSetting(MouseEvent e)	{ return panel(e).setting; }
		private void toggle(MouseWheelEvent w)	{
			if (allowEdit) {
				RSettingPanel panel = panel(w);
				ICRSettings setting = panel.setting;
				if (setting instanceof AvatarKey) {
					setting.toggle(w);
					backImage = null;
					setting.updateGui(panel);
					workshop.repaint();
					return;
				}
				if (setting instanceof SettingString) {
					// TODO BR: Call NamesUI
				}
				else
					setting.toggle(w);
				setting.updateGui(panel);

				if (!setting.hasNoCost()) {
					costPanel.updateCost();
					costPanel.paintImmediately();
				}
			}
		}
		private void toggle(MouseEvent e)	{
			if (allowEdit) {
				RSettingPanel panel = panel(e);
				ICRSettings setting = panel.setting;
				if (setting instanceof AvatarKey) {
					setting.toggle(e, workshop);
					backImage = null;
					setting.updateGui(panel);
					workshop.repaint();
					return;
				}
				if (setting instanceof SettingString) {
					// TODO BR: Call NamesUI
				}
				else
					setting.toggle(e, workshop);
				setting.updateGui(panel);

				if (!setting.hasNoCost()) {
					costPanel.updateCost();
					costPanel.paintImmediately();
				}
			}
		}
		private void setDescBox(MouseEvent e)	{
			RSettingPanel panel = panel(e);
			ICRSettings setting = panel.setting;
			if (panel != hasPopUp) {
				if (hasPopUp != null)
					hasPopUp.highLighted(false);
				hasPopUp = panel;
				panel.highLighted(true);
				String description = setting.getGuiDescription();
				descriptionPane.setText(description);
//				GuideUI.clear();
				GuideUI.open(panel, setting.getGuide());
//				repaint();
			}
		}
		private void clearDescBox(MouseEvent e)	{
			RSettingPanel panel = panel(e);
			if (!isInside(e, panel)) {
				panel.highLighted(false);
				GuideUI.close();
				descriptionPane.setText(emptyDescription);
				hasPopUp = null;
			}
		}
	}
	// -#-
	// ========================================================================
	// #=== Level 1: Content Panel (set as content pane to be able to access paintComponent to gives a background
	//
	private class ContentPanel extends BasePanel {
		private static final long serialVersionUID = 1L;
		private static final String NAME = "MainPanel";

		ContentPanel(boolean allowEdit)	{
			setOpaque(false);
			setName(NAME);
			initLists();

			setLayout(new GridBagLayout());
			GridBagConstraints gbc;
			int x = 0;
			int y = 0;

			if (allowEdit) {
				Insets inset = new Insets(0, BUTTON_SEP_W, 0, BUTTON_SEP_W);
				add(new TopPane(allowEdit), newGbc(x,y, 1,1, 0,0, SOUTHEAST, NONE, inset, 0,0));
			}

			// Page selection pane (with contents)
			y++;
			settingsPane = new SettingsPane();
			settingsPane.buildPanel(allowEdit);
			gbc = newGbc(x,y, 1,1, 0,0, EAST, NONE, new Insets(0, LEFT_MARGIN, 0, RIGHT_MARGIN), 0,0);
			add(settingsPane, gbc);

			// Description
			y++;
			gbc = newGbc(x,y, 1,1, 0,0, EAST, HORIZONTAL, new Insets(VERTICAL_GAP, LEFT_MARGIN, 0, RIGHT_MARGIN), 0,0);
			descriptionPane = new DescriptionPane(allowEdit);
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
	// #=== Level 2: ==> TopPane
	//
	private class TopPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private TopPane(boolean allowEdit) {
			setOpaque(false);
			setLayout(new GridBagLayout());
			int x = 0;
			int y = 0;
			add(new NamesButton(), newGbc(x, y, 1,1, 0,0, EAST, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, BUTTON_SEP_W), 0,0));
			x++;
			add(new FolderButton(), newGbc(x, y, 1,1, 0,0, WEST, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, BUTTON_SEP_W), 0,0));
		}
	}
	// ========================================================================
	// === Level 2: ==> Settings Panel
	//
	private class SettingsPane extends JPanel	{
		private static final long serialVersionUID = 1L;

		private BufferedImage backImg;
		private int width, height;

		SettingsPane() {
			setOpaque(false);
			setLayout(new GridBagLayout());
		}
		private void buildPanel(boolean allowEdit) {
			Color borderColor = Base.setAlpha(GameUI.setupShade(), backGroundAlpha/2);
			Border border = BorderFactory.createLineBorder(borderColor, BORDER_WIDTH);
			setBorder(border);

			int side = s10;
			int settingWidth = scaled(180);
			int columnWidth	 = scaled(200);
			int x = 0;
			int y = 0;
			SubPanel  subPanel = new SubPanel(columnWidth);
			subPanel.setBorder(new EmptyBorder(s5, side, s5, 0));
			add(subPanel, newGbc(x,y, 1,1, 1,0, NORTH, NONE, ZERO_INSETS, 0,0));
			Insets bulletInset = new Insets(0, 0, s5, 0);

			costPanel = new CostPanel(settingWidth);
			subPanel.add(costPanel, newGbc(x, y, 1,1, 0,0, WEST, HORIZONTAL, bulletInset, 0,0));
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
					subPanel = new SubPanel(columnWidth);
					subPanel.setBorder(new EmptyBorder(s10, 0, s5, x==3? side:0));
					add(subPanel, newGbc(x,0, 1,1, 1,0, NORTH, HORIZONTAL, new Insets(0, s10, 0, 0), 1,0));
					//continue;
				}
				ICRSettings setting = settingList.get(i);
				RSettingPanel settingPanel = new RSettingPanel(this, setting, settingWidth, new SettingListener());
				if (setting.isBullet())
					subPanel.add(settingPanel, newGbc(0,y, REMAINDER,1, 1,0, WEST, HORIZONTAL, bulletInset, 0,0));
				else
					subPanel.add(settingPanel, newGbc(0,y, REMAINDER,1, 1,0, WEST, HORIZONTAL, ZERO_INSETS, 0,0));
				y++;
			}
		}

		private BufferedImage backImg()	{
			int w = getWidth();
			int h = getHeight();
			if (backImg == null || w != width || h != height)
				initBackImg(w, h, backGroundAlpha);
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
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int w = getWidth();
			int h = getHeight();
			setHiRenderingHints(g);
			g.drawImage(backImg(), 0, 0, w, h, this);
		}
	}
	private class SubPanel extends JPanel implements RotPComponents	{
		private static final long serialVersionUID = 1L;
		private SubPanel(int width)	{
			BufferedImage blankLine = new BufferedImage(width, 1, TYPE_INT_ARGB);
			JLabel picLabel = new JLabel(new ImageIcon(blankLine));
			add(picLabel);
			setOpaque(false);
			setLayout(new GridBagLayout());
			setAlignmentY(0);
			setAlignmentX(0);
		}
		@Override public JComponent getComponent()	{ return this; }
	}
	// ========================================================================
	// === Level 2: ==> Description Pane
	//
	private class DescriptionPane extends RPanel	{
		private static final long serialVersionUID = 1L;
		private JTextPane descriptionBox;
		private DescriptionPane(boolean allowEdit)	{
			Color color = Base.setAlpha(GameUI.raceCenterColor(), backGroundAlpha);
			setOpaque(false);
			setBackground(color);
			setLayout(new GridBagLayout());

			GridBagConstraints gbc = newGbc(0,0, 1,1, 0,0, CENTER, HORIZONTAL, ZERO_INSETS, 0,0);
			BufferedImage blankLine = new BufferedImage(scaled(840), s41, TYPE_INT_ARGB);
			JLabel picLabel = new JLabel(new ImageIcon(blankLine));
			picLabel.setOpaque(false);
			add(picLabel, gbc);

			descriptionBox = new JTextPane();
			descriptionBox.setForeground(Color.BLACK);
			descriptionBox.setOpaque(false);
			descriptionBox.setContentType("text/html");
			descriptionBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			descriptionBox.setFont(DESC_FONT);
			descriptionBox.setText(emptyDescription);
			descriptionBox.setBackground(color);
		}
		private void setText(String text)	{
			descriptionBox.setText(text);
			repaint();
		}
		@Override protected void paintComponent(Graphics g)	{
			super.paintComponent(g);
			int w = getWidth();
			int h = getHeight();
			g.setColor(getBackground());
			g.fillRect(0, 0, w-1, h-1);

			Dimension dim =  descriptionBox.getPreferredSize();
			descriptionBox.setSize(dim);
			g.translate(s10, 0);
			descriptionBox.paint(g);
			g.translate(-s10, 0);
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
			add(new GuideButton(), newGbc(x, y, 1,1, 0,0, EAST, NONE, new Insets(0, 0, BUTTON_SEP_H, BUTTON_SEP_W), 0,0));

			x++;
			addVariableSpace(this, x, y);

			x++;
			addVariableSpace(this, x, y);

			x++;
			add(new CancelButton(), newGbc(x, y, 1,1, 0,0, EAST, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, BUTTON_SEP_W), 0,0));

			x++;
			add(new ExitButton(), newGbc(x, y, 1,1, 0,0, WEST, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, 0), 0,0));
		}
	}
	// -#-
	// ========================================================================
	// #=== Specific Buttons definition
	//
	private void close()	{
		buttonClick();
		setTooltipEnabled(oldTooltipState);
		RotPUI.instance().selectSetupRacePanel();
		disableGlassPane();
		setVisible(false);
		setEnabled(false);
		remove(contentPane);
		dnaFactory				= null;
		contentPane		= null;
		settingsPane	= null;
		costPanel		= null;
		descriptionPane	= null;
		backImage		= null;
		parent			= null;
	}
	private class ExitButton extends RHugeButton	{
		private static final long serialVersionUID = 1L;
		private ExitButton()	{
			super(ROOT + "BUTTON_EXIT");
			setLabelKey();
			addActionListener(e -> exitAction());
		}
		private void exitAction()	{ close(); }
	}
	private class CancelButton extends RHugeButton	{
		private static final long serialVersionUID = 1L;
		private CancelButton()	{
			super(ROOT + "BUTTON_CANCEL");
			setLabelKey();
			addActionListener(e -> cancelAction());
		}
		private void cancelAction()	{
			canceled = true;
			close();
		}
	}
	private class FolderButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private FolderButton()	{
			super(ROOT + "BUTTON_FOLDER");
			setLabelKey();
			addActionListener(e -> selectFolderAction());
		}
		private void selectFolderAction()	{ // TODO BR: selectFolderAction()
			Toolkit.getDefaultToolkit().beep();
		}
	}
	private class NamesButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private NamesButton()	{
			super(ROOT + "BUTTON_NAMES");
			setLabelKey();
			addActionListener(e -> selectNamesAction());
		}
		private void selectNamesAction()	{ // TODO BR: selectNamesAction()
			Toolkit.getDefaultToolkit().beep();
		}
	}
	private class GuideButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private GuideButton()	{
			super("SETTINGS_GUIDE");
			setLabelKey();
			addActionListener(e -> buttonGuideAction(e));
		}
		private void buttonGuideAction(ActionEvent e)	{
			buttonClick();
			showGuide.toggle();
			if (showGuide()) {
				RButton button = (RButton) e.getSource();
				button.popGuide(button.getToolTipText());
			}
		}
	}

	// ========================================================================
	// Other Specific Components definition
	//
	private class CostPanel extends JPanel implements RotPComponents	{
		private static final long serialVersionUID = 1L;
		RLabel totalCost, malusCost;
		private CostPanel(int width)	{
			setOpaque(false);
			Border border = BorderFactory.createLineBorder(Color.BLACK,1);
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
//		private void paintImmediately()	{ paintImmediately(0, 0, getWidth(), getHeight()); }
		@Override public JComponent getComponent()	{ return this; }
	}

	// ========================================================================
	// Other
	//
	// -#-
	
}
