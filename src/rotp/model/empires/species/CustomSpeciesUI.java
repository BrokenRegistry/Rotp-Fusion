package rotp.model.empires.species;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.SOUTH;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import rotp.model.empires.species.SpeciesSettings.BoundAI;
import rotp.model.empires.species.SpeciesSettings.SettingMap;
import rotp.model.game.DynOptions;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.components.RDialog;
import rotp.ui.components.RLabel;
import rotp.ui.components.RSeparator;
import rotp.ui.components.RotPButtons.RButton;
import rotp.ui.components.RotPComponents;
import rotp.ui.components.RotPPanels.RContentPanel;
import rotp.ui.game.BaseModPanel;
import rotp.ui.game.GameUI;
import rotp.ui.game.HelpUI;
import rotp.ui.main.SystemPanel;
import rotp.ui.util.InterfaceHelp;
import rotp.util.FontManager;

public final class CustomSpeciesUI extends RDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final String ROOT		= SkillsFactory.ROOT;
	private static final int BUTTON_SEP_H	= s5;
	private static final int BUTTON_SEP_W	= s10;
	private static final int LEFT_MARGIN	= s20;
	private static final int RIGHT_MARGIN	= LEFT_MARGIN;
	private static final int VERTICAL_GAP	= s5;

	private static final String	TOTAL_COST_KEY	= ROOT + "GUI_COST";
	private static final String	MALUS_COST_KEY	= ROOT + "GUI_MALUS";
	private static final Color	COST_COLOR		= SystemPanel.blackText;
	private static final Color	MALUS_COLOR		= SystemPanel.redText;
	private static final Color	SEPARATOR_COLOR	= ICRSettings.optionC;
	private static final int	COST_FONT_SIZE	= 18;
	private	static final Font	DESC_FONT		= FontManager.current().plainFont(14);

	private final boolean allowEdit;
	private final JDialog dialog;
	private BasePanel lowerParent;

	private SkillsFactory sf;
	private SettingMap settingMap;
	// Panels
	private ContentPanel contentPane;
	private SettingsPane settingsPane;
	private CostPanel costPanel;
	private JTextPane descriptionBox;

	// private working
	private boolean canceled = false;
	private boolean updating = false;

	//
	private	List<Integer> spacerList;
	private List<Integer> columnList;
	List<ICRSettings> commonList;
	private List<ICRSettings> settingList;
//	private List<RSettingPanel> settingPanelList;
	private int settingSize;
	private String emptyDescription;

	private RSettingPanel hasPopUp;

	@Override public int baseInitialDelay()	{ return 100000; } // TODO BR: REMOVE

	JPanel testPopUp;
	void testPopUp(boolean show)	{
		Point p = getMousePosition();
		System.out.println("Position = " + p);
		BufferedImage eyeIcon = this.eyeIcon(scaled(300), scaled(300), Color.RED, true);
		testPopUp = new JPanel();
		testPopUp.add(new JLabel("Bla Bla Bla"));
		testPopUp.setOpaque(true);
		
	}

	// ========================================================================
	// #=== Initializers
	//
	public CustomSpeciesUI(BaseModPanel parent, boolean allowEdit) {
		super(parent);
		dialog = this;
		setName("CustomSpeciesUI");
		this.allowEdit= allowEdit;
	}
	public boolean showPanel()	{
		sf(SkillsFactory.getSkillsFactoryForEditor(parent));
		sf().setSettingTools((DynOptions) guiOptions().selectedPlayerCustomRace());
		settingMap	= sf().settingMap;
		commonList	= settingMap.getSettings();
//		settingPanelList = new ArrayList<>();
		emptyDescription = html("<b>Shift</b>&nbsp and <b>Ctrl</b>&nbsp can be used to change buttons, click and scroll functions");
		contentPane	= new ContentPanel();
		setContentPane(contentPane);

		initTooltips();
		setTitle(guiTitle());

		pack();
		setLocationRelativeTo(parent);
//		setModalityType(DEFAULT_MODALITY_TYPE);
		setModal(false);
		setVisible(true);
		return canceled;
	}
	private String totalCostStr()	{ return text(TOTAL_COST_KEY, Math.round(sf().getTotalCost())); }
	private String malusCostStr()	{ return text(MALUS_COST_KEY, Math.round(sf().getMalusCost())); }
	private void initLists() {
		commonList	= settingList;
		columnList	= sf().columnList();
		spacerList	= sf().spacerList();
		settingList	= sf().settingList();
		settingSize	= settingList.size();
	}
	// -#-
	// ========================================================================
	// #=== getters and setters
	//
	private SkillsFactory sf()			{ return sf; }
	private void sf(SkillsFactory sf)	{ this.sf = sf; }
	private void updating(boolean b)	{ updating = b; }
	private boolean isCustomSpecies()	{ return false; } // TODO BR: isCustomSpecies()
	private String guiTitleID()	{
		if (allowEdit)
			return ROOT + "GUI_TITLE";
		else
			if (isCustomSpecies())
				return ROOT + "SHOW_TITLE_CUSTOM";
			else
				return ROOT + "SHOW_TITLE";
	}
	private String guiTitle()	{ return text(guiTitleID()); }
	// -#-
	// ========================================================================
	// #=== Event calls
	//
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
//			System.out.println("toggle(MouseWheelEvent w)"); // TODO BR: REMOVE
			if (allowEdit) {
				RSettingPanel panel = panel(w);
				ICRSettings setting = panel.setting;
				if (setting instanceof SettingString) {
					// TODO BR: Call NamesUI
				}
				else
					setting.toggle(w);
				setting.updateGui(panel);
//				panel.updateDisplay();
				if (!setting.hasNoCost())
					costPanel.updateCost();
			}
		}
		private void toggle(MouseEvent e)	{
//			System.out.println("toggle(MouseEvent e)"); // TODO BR: REMOVE
			if (allowEdit) {
				RSettingPanel panel = panel(e);
				ICRSettings setting = panel.setting;
				if (setting instanceof SettingString && !(setting instanceof BoundAI)) {
					// TODO BR: Call NamesUI
				}
				else
					setting.toggle(e, contentPane);
				setting.updateGui(panel);
//				panel.updateDisplay();
				if (!setting.hasNoCost())
					costPanel.updateCost();
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
				descriptionBox.setText(description);
				HelpUI helpUI = RotPUI.helpUI();
				helpUI.clear();
				Rectangle onScreen = getLocationOnScreen(panel);
				helpUI.open(dialog, contentPane, panel, description, onScreen);
				System.out.println("helpUI.open(...);"); // TODO BR: REMOVE
			}
		}
		private void clearDescBox(MouseEvent e)	{
			RSettingPanel panel = panel(e);
			if (!isInside(e, panel)) {
				panel.highLighted(false);
				descriptionBox.setText(emptyDescription);
				HelpUI helpUI = RotPUI.helpUI();
				helpUI.close();
				hasPopUp = null;
			}
		}
	}
	private Rectangle getLocationOnScreen(JComponent c)	{
		return new Rectangle(c.getLocationOnScreen(), c.getSize());
	}
	private boolean isInside(MouseEvent e, JComponent c)	{
		return getLocationOnScreen(c).contains(e.getLocationOnScreen());
	}
//	// -#-
//	// ========================================================================
//	// #=== Level 1: BackGround Panel (set as content pane to be able to access paintComponent to gives a background
//	//
//	private class ContentPanel  extends JPanel implements RotPComponents {
//		@Override public void paintComponent(Graphics g) {
//			int w = getWidth();
//			int h = getHeight();
//			setHiRenderingHints(g);
//			g.drawImage(backImg(), 0, 0, w, h, this);
//		}
//	}
	// -#-
	// ========================================================================
	// #=== Level 1: Content Panel (set as content pane to be able to access paintComponent to gives a background
	//
	private class ContentPanel extends RContentPanel implements InterfaceHelp {
		private static final long serialVersionUID = 1L;
		private static final String NAME = "MainPanel";
		private ComponentPositioner positioner = new ComponentPositioner();
		private Dimension lastSize;

		ContentPanel()	{
			setName(NAME);
			title = title();
			initLists();

			setLayout(new GridBagLayout());
			GridBagConstraints gbc;
			int x = 0;
			int y = 0;
			// Top buttons
			gbc = newGbc(x,y, 1,1, 0,0, CENTER, HORIZONTAL, new Insets(topContentPosition()-s10, LEFT_MARGIN, VERTICAL_GAP, RIGHT_MARGIN), 0,0);
			add(new TopPane(), gbc);

			// Page selection pane (with contents)
			y++;
			settingsPane = new SettingsPane();
			settingsPane.buildPanel();
			gbc = newGbc(x,y, 1,1, 0,0, CENTER, NONE, new Insets(0, LEFT_MARGIN, 0, RIGHT_MARGIN), 0,0);
			add(settingsPane, gbc);

			// Description
			y++;
			gbc = newGbc(x,y, 1,1, 0,0, CENTER, HORIZONTAL, new Insets(0, LEFT_MARGIN, VERTICAL_GAP, RIGHT_MARGIN), 0,0);
			add(new DescriptionPane(), gbc);

			// Bottom buttons
			y++;
			gbc = newGbc(x,y, 1,1, 0,0, SOUTH, HORIZONTAL, new Insets(0, LEFT_MARGIN, VERTICAL_GAP, RIGHT_MARGIN), 0,0);
			add(new BottomPane(), gbc);
			lastSize = getSize();
			addComponentListener(positioner);
//			pack();
		}

		@Override protected String title()	{ return guiTitle(); }

		private class ComponentPositioner extends ComponentAdapter {
			@Override public void componentResized(ComponentEvent evt) {
				if (lastSize.equals(getSize()))
					return;
				lastSize = getSize();
				reCenter();
			}
		}

		@Override
		public void cancelHelp() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void advanceHelp() {
			// TODO Auto-generated method stub
			
		}
	}
	// -#-
	// ========================================================================
	// #=== Level 2: ==> TopPane
	//
	private class TopPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private TopPane() {
			setOpaque(false);
			setLayout(new GridBagLayout());
			costPanel = new CostPanel(scaled(200));

			int x = 0;
			int y = 0;
			add(costPanel, newGbc(x, y, 1,1, 0,0, WEST, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, BUTTON_SEP_W), 0,0));

			x++;
			addVariableSpace(this, x, y);

			x++;
			addVariableSpace(this, x, y);

			x++;
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
		SettingsPane() {
			setOpaque(false);
			setLayout(new GridBagLayout());
		}
		private void buildPanel() {
			if (updating)
				return;
			updating(true);

			int settingWidth = scaled(180);
			int columnWidth	 = scaled(200);
			int x = 0;
			int y = 0;
			SubPanel  subPanel = new SubPanel(columnWidth);
			add(subPanel, newGbc(x,0, 1,1, 1,0, NORTH, NONE, ZERO_INSETS, 1,0));
			Insets bulletInset = new Insets(0, 0, s5, 0);
			for (int i=0; i<settingSize; i++) {
				if (spacerList.contains(i)) {
					//new RSeparator(subPanel, true, null, 0, y ,s3);
					//new RSeparator(subPanel, true, s2, null, 0, y ,0, s4, null);
					//new RSeparator(subPanel, true, s2, null, 0, y ,0, s4, new Color(220, 170, 120));
					new RSeparator(subPanel, true, s2, null, 0, y ,0, s4, GameUI.loadHoverBackground());
					y++;
					//continue;
				}
				if (columnList.contains(i)) {
					x+=1;
					y = 0;
					subPanel = new SubPanel(columnWidth);
					add(subPanel, newGbc(x,0, 1,1, 1,0, NORTH, HORIZONTAL, new Insets(0, s10, 0, 0), 1,0));
					//continue;
				}
				ICRSettings setting = settingList.get(i);
				RSettingPanel settingPanel = new RSettingPanel(this, setting, settingWidth, new SettingListener());
//				settingPanelList.add(settingPanel);
				if (setting.isBullet())
					subPanel.add(settingPanel, newGbc(0,y, REMAINDER,1, 1,0, WEST, HORIZONTAL, bulletInset, 0,0));
				else
					subPanel.add(settingPanel, newGbc(0,y, REMAINDER,1, 1,0, WEST, HORIZONTAL, ZERO_INSETS, 0,0));
				y++;
			}
			updating(false);
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
	}
	// ========================================================================
	// === Level 2: ==> Description Pane
	//
	private class DescriptionPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private DescriptionPane()	{
			setOpaque(true);
			setBackground(GameUI.setupFrame());
			setLayout(new GridBagLayout());

			GridBagConstraints gbc = newGbc(0,0, 1,1, 0,0, CENTER, HORIZONTAL, ZERO_INSETS, 0,0);
			BufferedImage blankLine = new BufferedImage(scaled(800), s41, TYPE_INT_ARGB);
			JLabel picLabel = new JLabel(new ImageIcon(blankLine));
			add(picLabel, gbc);

			descriptionBox = new JTextPane();
			add(descriptionBox, gbc);

			descriptionBox.setOpaque(true);
			descriptionBox.setContentType("text/html");
			descriptionBox.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
			descriptionBox.setFont(DESC_FONT);
			descriptionBox.setBackground(GameUI.setupFrame());
		}
	}
	// ========================================================================
	// === Level 2: ==> BottomPane
	//
	private class BottomPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private BottomPane() {
			setOpaque(false);
			setLayout(new GridBagLayout());
			int x = 0;
			int y = 0;
			GridBagConstraints gbc = newGbc(x, y, 1,1, 0,0, EAST, NONE, new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, BUTTON_SEP_W), 0,0);
			add(new CancelButton(), gbc);

			x++;
			addVariableSpace(this, x, y);

			x++;
			addVariableSpace(this, x, y);

			gbc.insets = new Insets(0, BUTTON_SEP_W, BUTTON_SEP_H, BUTTON_SEP_W);
			x++;
			gbc.gridx = x;
			gbc.anchor = WEST;
			add(new ExitButton(), gbc);
		}
	}
	// -#-
	// ========================================================================
	// #=== Specific Buttons definition
	//
	private class ExitButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private ExitButton()	{
			super(ROOT + "BUTTON_EXIT");
			setLabelKey();
			addActionListener(e -> exitAction());
		}
		private void exitAction()	{ dispose(); }
	}
	private class CancelButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private CancelButton()	{
			super(ROOT + "BUTTON_CANCEL");
			setLabelKey();
			addActionListener(e -> cancelAction());
		}
		private void cancelAction()	{
			canceled = true;
			dispose();
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
	// ========================================================================
	// Other Specific Components definition
	//
	private class CostPanel extends JPanel implements RotPComponents	{
		private static final long serialVersionUID = 1L;
		RLabel totalCost, malusCost;
		private CostPanel(int width)	{
			setOpaque(false);
			setLayout(new GridBagLayout());
			setAlignmentY(0);
			setAlignmentX(0);
			BufferedImage blankLine = new BufferedImage(scaled(width), 1, TYPE_INT_ARGB);
			JLabel picLabel = new JLabel(new ImageIcon(blankLine));
			add(picLabel, newGbc(0,0, 2,0, 0,0, NORTH, NONE, new Insets(0, 0, 0, 0), 0,0));

			totalCost = new RLabel (totalCostStr(), COST_FONT_SIZE, COST_COLOR);
			add(totalCost, newGbc(0,0, 1,0, 0,0, EAST, NONE, new Insets(0, 0, 0, 0), 0,0));

			malusCost = new RLabel (malusCostStr(), COST_FONT_SIZE, MALUS_COLOR);
			add(malusCost, newGbc(1,0, 1,0, 0,0, WEST, NONE, new Insets(0, 0, 0, 0), 0,0));
		}
		private void updateCost()	{
			totalCost.setText(totalCostStr());
			malusCost.setText(malusCostStr());
		}
	}

	// ========================================================================
	// Other
	//
	@Override public void actionPerformed(ActionEvent e) { } // TODO BR:
	private void reCenter() {
		Point pLoc = parent.getLocationOnScreen();
		Dimension pSize = parent.getSize();
		Dimension cSize = getSize();
		int x = pLoc.x + (pSize.width - cSize.width)/2;
		int y = pLoc.y + (pSize.height - cSize.height)/2;
		setLocation(x, y);
	}
	// -#-
}
