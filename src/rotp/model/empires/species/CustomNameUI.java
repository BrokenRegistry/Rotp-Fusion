package rotp.model.empires.species;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import rotp.model.empires.species.CustomRaceDefinitions.AllSpeciesAttributes;
import rotp.model.empires.species.SpeciesSettings.SpeciesAttributes;
import rotp.ui.components.RButtonBar;
import rotp.ui.components.RButtonBar.BarEvent;
import rotp.ui.components.RButtonBar.ButtonBarListener;
import rotp.ui.components.RDialog;
import rotp.ui.components.RLabel;
import rotp.ui.components.RSeparator;
import rotp.ui.components.RotPButtons.RButton;
import rotp.ui.components.RotPButtons.RToggleButton;
import rotp.ui.components.RotPPanels.RContentPanel;
import rotp.ui.components.RotPTextFields.SettingField;
import rotp.ui.game.BaseModPanel;
import rotp.ui.game.GameUI;
import rotp.ui.util.StringList;
import rotp.util.FontManager;
import rotp.util.LanguageManager;

public class CustomNameUI extends RDialog {
	private static final long serialVersionUID = 1L;
	private static final String ROOT = CustomRaceDefinitions.ROOT + "UI_";

	private static final Insets BUTTON_INSETS = new Insets(0, s10, 0, s10);
	private static final String  LANGUAGE_ID = "LANGUAGE";
	private static final String  ITEM_ID	 = "ITEM";
	private static final int NORM_FIELDS_COL = 20;
	private static final int WIDE_FIELDS_COL = 40;
	private static final int buttonSepH		= s5;
	private static final int buttonSepW		= s10;
	private int LEFT_MARGIN		= s20;
	private int RIGHT_MARGIN	= LEFT_MARGIN;
	private int HORIZONTAL_GAP	= s10;
	private int VERTICAL_GAP	= s10;

//	private final HashMap<String, StringList> onePerEmpireMap	= SpeciesSettings.getOnePerEmpireMap();
//	private final HashMap<String, StringList> onePerSpeciesMap	= SpeciesSettings.getOnePerSpeciesMap();
	private final StringList languageNames = new StringList(LanguageManager.current().languageNames());
	private final StringList languageCodes = new StringList(LanguageManager.current().languageCodes());

	private boolean updating;
	private AllSpeciesAttributes settings;
	private ArrayList<SettingField> fields;

	private StringList languageButtonList; // Dynamic
	private StringList ethnicButtonList;	// Dynamic
	private SettingField keyField;

	// Panels
	private ContentPanel contentPane;
	private PageSelectionPane pageSelectionPane;
	private BookPane bookPane;
	private LangageSelection langageSelection; // ComboBox

	private void updating(boolean b)	{ updating = b; }
	private boolean twoLanguages()		{
		int selectedIndex = languageButtonList.getSelectedIndex();
		int size = languageButtonList.size();
		return size > 1 && selectedIndex > 0;
	}
	private int languageIndex()			{ return languageNames.getSelectedIndex(); }
	private StringList remainingLang()	{
		StringList list = new StringList(languageNames);
		list.removeAll(languageButtonList);
		return list;
	}
	private String nextLanguage()		{
		String nextLanguage = remainingLang().getFirst();
		languageNames.setSelectedIndex(nextLanguage);
		return nextLanguage;
	}

	CustomNameUI(BaseModPanel parent, AllSpeciesAttributes settings) {
		super(parent);
		setName("CustomNameUI");
		this.settings = settings;

		contentPane = new ContentPanel();
		setContentPane(contentPane);

		setTitle(text(ROOT + "NAMES_TITLE"));

		pack();
		setLocationRelativeTo(parent);
		setModal(true);
		setVisible(true);
	}
	private void initLists() {
		int currentId = LanguageManager.selectedLanguage();
		languageNames.setSelectedIndex(currentId);
		languageCodes.setSelectedIndex(currentId);

		languageButtonList = new StringList();
		languageButtonList.add(nextLanguage());
		if (currentId > 0) {
			languageButtonList.add(languageNames.get(currentId));
			languageButtonList.setSelectedIndex(1);
		}
		else
			languageButtonList.setSelectedIndex(0);

		ethnicButtonList = settings.getEthnicNames(); // TODO BR: complete
		ethnicButtonList.setSelectedIndex(0);
	}
	private void reCenter() {
		Point pLoc = parent.getLocationOnScreen();
		Dimension pSize = parent.getSize();
		Dimension cSize = getSize();
		int x = pLoc.x + (pSize.width - cSize.width)/2;
		int y = pLoc.y + (pSize.height - cSize.height)/2;
		setLocation(x, y);
	}
	class LanguageBarListener implements ButtonBarListener	{ // TODO BR:
		@Override public void actionPerformed(BarEvent e) {
			if (updating)
				return;

			// System.out.println("LanguageBarListener:" + e.toString());
			switch (e.event) {
				case BUTTON_ADDED:
					break;
				case BUTTON_REMOVED:
					break;
				case BUTTON_SELECTED:
					break;
			}
			languageNames.setSelectedIndex(e.newLabel);
			pageSelectionPane.buildPanel();
		}
	}
	class EthnicBarListener implements ButtonBarListener	{ // TODO BR:
		@Override public void actionPerformed(BarEvent e) {
			if (updating)
				return;

			// System.out.println("EmpireBarListener:" + e.toString());
			switch (e.event) {
				case BUTTON_ADDED:
					break;
				case BUTTON_REMOVED:
					break;
				case BUTTON_SELECTED:
					System.out.println("EmpireBarListener: Button selected" + e.toString());
					break;
			}
			pageSelectionPane.buildPanel();
		}
	}
	// ========================================================================
	// Level 1: Content Panel (set as content pane to be able to access paintComponent to gives a background
	//
	class ContentPanel extends RContentPanel { // TODO
		private static final long serialVersionUID = 1L;
		private static final String NAME = "MainPanel";
		ComponentPositioner positioner = new ComponentPositioner();
		Dimension lastSize;

		ContentPanel()	{
			setName(NAME);
			title = title();
			initLists();

			fields		= new ArrayList<>();
			keyField	= new SettingField(settings.raceKey, NORM_FIELDS_COL);
			fields.add(keyField);
			
			// Page selection pane (with contents)
			pageSelectionPane = new PageSelectionPane();
			pageSelectionPane.buildPanel();

			setLayout(new GridBagLayout());
			int x = 0;
			int y = 0;
			GridBagConstraints gbc = newGbc(0,0, REMAINDER,1, 0,0, CENTER, HORIZONTAL, new Insets(topContentPosition(), LEFT_MARGIN, 0, RIGHT_MARGIN), 0,0);
			add(pageSelectionPane, gbc);

			// Bottom buttons
			y++;
			gbc = newGbc(x,y, 1,1, 0,0, CENTER, HORIZONTAL, new Insets(VERTICAL_GAP, LEFT_MARGIN, VERTICAL_GAP, RIGHT_MARGIN), 0,0);
			add(new BottomPane(), gbc);
			lastSize = getSize();
			addComponentListener(positioner);

		}
		@Override protected String title()	{ return text(ROOT + "NAMES_TITLE"); }

		class ComponentPositioner extends ComponentAdapter {
				@Override public void componentResized(ComponentEvent evt) {
					if (lastSize.equals(getSize()))
						return;
					lastSize = getSize();
					reCenter();
//					Component c = (Component)evt.getSource();
//					System.out.println(evt.toString());
//					System.out.println(c.toString());
				}
		}
	}
	// ========================================================================
	// Level 2: ==> Selection Panel
	//
	private class PageSelectionPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private SelectionBars bars;
		// TODO
		PageSelectionPane() {
			setOpaque(false);
			setLayout(new GridBagLayout());
		}
		private void buildPanel() {
			if (updating)
				return;
			// System.out.println();
			// System.out.println("====> UPDATING = TRUE");
			updating(true);
			if (bars != null)
				removeAll();

			bars = new SelectionBars();
			bars.buildPanel();
			GridBagConstraints gbc = newGbc(0,0, REMAINDER,1, 0,0, CENTER, HORIZONTAL, new Insets(0, LEFT_MARGIN, 0, RIGHT_MARGIN), 0,0);
			add(bars, gbc);

			bookPane = new BookPane();
			bookPane.buildPanel();
			gbc = newGbc(0,1, 1,1, 0,0, CENTER, HORIZONTAL, new Insets(0, LEFT_MARGIN, 0, RIGHT_MARGIN), 0,0);
			add(bookPane, gbc);
			pack();
			updating(false);
			// System.out.println("====> UPDATING = FALSE");
		}
		private void updateLanguage(String language)	{
			// System.out.println("update Language to " + language + " updating = " + updating);
			if (updating)
				return;
			languageNames.setSelectedIndex(language);
			int langIndex = languageButtonList.getSelectedIndex();
			languageButtonList.set(langIndex, language);
			buildPanel();
		}
	}
	// ========================================================================
	// Level 2: ==> BottomPane
	//
	private class BottomPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private BottomPane() {
			setOpaque(false);
			setLayout(new GridBagLayout());
			int x = 0;
			int y = 0;
			GridBagConstraints gbc = newGbc(x, y, 1,1, 0,0, EAST, NONE, new Insets(buttonSepH, buttonSepW, buttonSepH, buttonSepW), 0,0);
			add(new CancelButton(), gbc);

			x++;
			addVariableSpace(this, x, y);

			x++;
			gbc.gridx = x;
			keyField = new SettingField(this, settings.raceKey, NORM_FIELDS_COL, 1, gbc.gridy);

			x++;
			addVariableSpace(this, x, y);

			gbc.insets = new Insets(buttonSepH, buttonSepW, buttonSepH, buttonSepW);
			x++;
			gbc.gridx = x;
			gbc.anchor = WEST;
			add(new ExitButton(), gbc);
			pack();
		}
	}

	// ========================================================================
	// Level 3: Book Pane
	//
	private class BookPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private GridBagConstraints c;
		private PagePane page0, page1;

		private BookPane(String name)	{
			setName(name);
			setOpaque(false);
			setForeground(Color.black);
			Border border = new LineBorder(GameUI.raceEdgeColor(), s2); // TODO
			setBorder(border);
			setLayout(new GridBagLayout());
			buildPanel();
		}
		private BookPane() {
			setOpaque(false);
			setForeground(Color.black);
			Border border = new LineBorder(GameUI.raceEdgeColor(), s2); // TODO
			setBorder(border);
			setLayout(new GridBagLayout());
		}

		private PagePane page0() {
			if (page0 == null)
				page0 = new PagePane(true);
			return page0;
		}
		private PagePane page1() {
			if (page1 == null)
				page1 = new PagePane(false);
			return page1;
		}
		private void buildPanel() {
			updating(true);
			c = new GridBagConstraints();
			if (twoLanguages())
				sidedLanguage();
			else
				singleLanguage();
			// pack();
			updating(false);
		}
		private void singleLanguage() {
			c.insets = ZERO_INSETS;
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.CENTER;
			add(page0(), c);
		}
		private void sidedLanguage() {
			GridBagConstraints gbc = newGbc(0, 0, 1, 1, 0, 0, 7, NONE, ZERO_INSETS, 0, 0);
			add(page0(), gbc);
			gbc.gridx++;
			new RSeparator(this, false, gbc.gridx, gbc.gridy);
			//new RotPSeparatorV(this, c.gridx, c.gridy);
			gbc.gridx++;
			new RSeparator(this, false, gbc.gridx, gbc.gridy);
			gbc.gridy = 0;
			gbc.gridx++;
			c.anchor = GridBagConstraints.EAST;
			add(page1(), gbc);
		}
	}
	// ========================================================================
	// Level 4: Page Pane
	//
	private class PagePane extends JPanel	{
		private static final long serialVersionUID = 1L;
		String language, languageDir;
		int languageId, ethnicId;
		private PagePane(boolean left)	{
			if (left)
				languageId = 0;
			else
				languageId = languageIndex();

			language	= languageNames.get(languageId);
			languageDir	= languageCodes.get(languageId);
			ethnicId	= ethnicButtonList.getSelectedIndex();
			// System.out.println("Page left = " + left + " Language = " + language);
			setOpaque(false);
			setForeground(Color.black);
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			// language
			c.insets = ZERO_INSETS;
			c.gridx = 1;
			c.gridy = 0;
			c.gridheight = 1;
			c.anchor = GridBagConstraints.EAST;
			add(new RLabel("Language:"), c);
			c.gridy = 1;
			if (left)
				add(new RLabel(language), c);
			else {
				StringList list = remainingLang();
				list.add(0, language);
				list.setSelectedIndex(0);
				langageSelection = new LangageSelection(list);
				langageSelection.setSelectedItem(language);
				add(langageSelection, c);
			}
// TODO
			// From Name text file
			// Empire dependent fields

			// ==> EmpireNameItems
			int x = 0;
			int y = 0;
			SpeciesAttributes identifications = settings.getAttributes(languageDir);
			for (SettingString setting : identifications.empireNameItems) {
				new SettingField(this, setting, NORM_FIELDS_COL, x, y, languageDir, ethnicId);
				y++;
			}

			new RSeparator(this, true, s2, null, x, y);
			y++;

			// Common to the species
			// ==> SpeciesNameItems
			for (SettingString setting : identifications.speciesNameItems) {
				new SettingField(this, setting, NORM_FIELDS_COL, x, y, languageDir, ethnicId);
				y++;
			}

			// ==> SpeciesDescriptionItems
			for (SettingString setting : identifications.speciesDescriptionItems) {
				new SettingField(this, setting, WIDE_FIELDS_COL, x, y, languageDir, ethnicId);
				y++;
			}

			c.gridx = 0;
			c.gridy = y;
			if (languageId < 2) {
				c.gridy +=1;
				c.gridx = 1;
				addSuggestButton(this, c);
			}

			new RSeparator(this, true, s4, null, x, y);
			y++;

			// For Dialogues
			// Common Fields

			// ==> SpeciesLabelItems
			for (SettingString setting : identifications.speciesLabelItems) {
				new SettingField(this, setting, NORM_FIELDS_COL, x, y, languageDir, ethnicId);
				y++;
			}

			new RSeparator(this, true, s2, null, x, y);

			// Multiple Fields

			c.gridx = 0;
			c.gridy = y;
			if (languageId < 2) {
				c.insets = BUTTON_INSETS;
				c.gridx = 1;
				c.gridy = y+1;
				c.gridheight = 3;
				c.anchor = GridBagConstraints.EAST;
				add(new SuggestButton(), c);
				//addSuggestButton(this, c);
			}
			y++;

			// ==> EmpireLabelItems
			for (SettingString setting : identifications.empireLabelItems) {
				new SettingField(this, setting, NORM_FIELDS_COL, x, y, languageDir, ethnicId);
				y++;
			}
		}
	}
	// ========================================================================
	// Level 4: Selection Bar
	//

	private class SelectionBars extends JPanel	{
		private static final long serialVersionUID = 1L;
		private RButtonBar selectEmpire, selectLanguage;
		private SelectionBars() {
			setName("SelectionPane");
			setOpaque(false);
			setLayout(new GridBagLayout());
			int x = 0;
			int y = 0;
			selectEmpire = new RButtonBar(ethnicButtonList, ITEM_ID, true, true, false);
			selectEmpire.setButtonBarListener(new EthnicBarListener());
			GridBagConstraints gbc = newGbc(x,y, 1,1, 0,0, WEST, NONE, ZERO_INSETS, 0,0);
			add(selectEmpire, gbc);

			x++;
			addVariableSpace(this, x, y);

			selectLanguage = new RButtonBar(languageButtonList, LANGUAGE_ID, false, true, true);
			selectLanguage.setButtonBarListener(new LanguageBarListener());
			selectLanguage.setNewTextRequest(new NextName());
			gbc.gridx = x+1;
			gbc.weightx = 0;
			gbc.anchor = EAST;
			add(selectLanguage, gbc);
			pack();
		}
		private void buildPanel()	{
			setLayout(new GridBagLayout());
			int x = 0;
			int y = 0;
			selectEmpire = new RButtonBar(ethnicButtonList, ITEM_ID, true, true, false);
			selectEmpire.setButtonBarListener(new EthnicBarListener());
			GridBagConstraints gbc = newGbc(x,y, 1,1, 0,0, WEST, NONE, ZERO_INSETS, 0,0);
			add(selectEmpire, gbc);

			x++;
			addVariableSpace(this, x, y);

			selectLanguage = new RButtonBar(languageButtonList, LANGUAGE_ID, false, true, true);
			selectLanguage.setButtonBarListener(new LanguageBarListener());
			selectLanguage.setNewTextRequest(new NextName());
			gbc.gridx = x+1;
			gbc.weightx = 0;
			gbc.anchor = EAST;
			add(selectLanguage, gbc);
			pack();			
		}
		private class NextName implements Function<String, String>	{
			@Override public String apply(String name) {
				if (name.equals(LANGUAGE_ID))
					return nextLanguage();
				if (name.equals(ITEM_ID))
					return null;
				return null;
			}
			
		}
	}
	// ========================================================================
	// Level 5: 
	//

	private void addSuggestButton(Container pane, GridBagConstraints c)	{
		c.insets = BUTTON_INSETS;
		//c.gridx = 1;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.EAST;
		pane.add(new SuggestButton(), c);
	}

	// ========================================================================
	// Specific Buttons definition
	//
	private class ExitButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private ExitButton()	{
			super("Exit");
			setToolTipText("TODO");
			addActionListener(new ExitAction());
		}
		private class ExitAction implements ActionListener {
			@Override public void actionPerformed(ActionEvent evt)	{
				dispose();
				// TODO BR:  Exit
			}
		}
	}
	private class CancelButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private CancelButton()	{
			super("Cancel");
			setToolTipText("TODO");
			addActionListener(new CancelAction());
		}
		private class CancelAction implements ActionListener {
			@Override public void actionPerformed(ActionEvent evt)	{
				dispose();
				// TODO BR:  Cancel
			}
		}
	}
	private class SuggestButton extends RToggleButton	{
		private static final long serialVersionUID = 1L;
		private SuggestButton()	{
			super("Suggest");
			setToolTipText("TODO");
			addActionListener(new SuggestAction());
		}
		private class SuggestAction implements ActionListener	{
			@Override public void actionPerformed(ActionEvent evt)	{
				// TODO BR:  Cancel
			}
		}
	}
	// ========================================================================
	// Other Specific Components definition
	//
	
	private class LangageSelection extends JComboBox<String>	{ // TODO
		private static final long serialVersionUID = 1L;
		protected int comboFontSize	= 12;
		protected Font comboFont	= FontManager.current().narrowFont(comboFontSize);
		private LangageSelection(StringList list)	{
			super(list.getArray());
			setBackground(GameUI.setupFrame());
			setForeground(Color.BLACK);
			getEditor().getEditorComponent().setBackground(GameUI.setupFrame());
			getEditor().getEditorComponent().setForeground(Color.BLACK);
			setRenderer(new listRenderer());
			setFont(comboFont);
			setSelectedIndex(0);
			addActionListener(new LanguageAction());
			//setFocusable(false);
		}
		private class LanguageAction implements ActionListener	{
			@Override public void actionPerformed(ActionEvent evt)	{
				// System.out.println("LanguageAction " + evt.getActionCommand());
				String language = (String) getSelectedItem();
//				int idx = languageNames.indexOf(language);
//				languageIndex(idx);
				pageSelectionPane.updateLanguage(language);
			}
		}
		private class listRenderer extends DefaultListCellRenderer	{
			private static final long serialVersionUID = 1L;
			@Override public void paint(Graphics g) {
				setBackground(GameUI.setupFrame());
				setForeground(Color.BLACK);
				super.paint(g);
			}
		}
	}
}
