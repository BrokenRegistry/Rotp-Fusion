package rotp.model.empires.species;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;

import rotp.model.empires.species.SpeciesSettings.AllSpeciesAttributes;
import rotp.model.empires.species.SpeciesSettings.CivilizationNameItems;
import rotp.model.empires.species.SpeciesSettings.SpeciesAttributes;
import rotp.model.empires.species.SpeciesSettings.SpeciesDescriptionItems;
import rotp.model.empires.species.SpeciesSettings.SpeciesLabelItems;
import rotp.model.empires.species.SpeciesSettings.SpeciesNameItems;
import rotp.ui.components.RButtonBar;
import rotp.ui.components.RButtonBar.BarEvent;
import rotp.ui.components.RButtonBar.ButtonBarListener;
import rotp.ui.components.RDialog;
import rotp.ui.components.RLabel;
import rotp.ui.components.RSeparator;
import rotp.ui.components.RotPButtons.RButton;
import rotp.ui.components.RotPPanels.RContentPanel;
import rotp.ui.components.RotPTextFields.SettingField;
import rotp.ui.game.BaseModPanel;
import rotp.ui.game.GameUI;
import rotp.ui.util.StringList;
import rotp.util.FontManager;
import rotp.util.LanguageManager;

class CustomNameUI extends RDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static final String ROOT = SkillsFactory.ROOT + "UI_";

	private static final Insets BUTTON_INSETS	= new Insets(0, s10, 0, s10);
	private static final String  LANGUAGE_ID	= "LANGUAGE";
	private static final String  ITEM_ID		= "ITEM";
	private static final int NORM_FIELDS_COL	= 20;
	private static final int WIDE_FIELDS_COL	= 40;
	private static final int buttonSepH			= s5;
	private static final int buttonSepW			= s10;
	private static final int UNKNOWN_COLOR_ID	= -1;
	private static final int DEFAULT_COLOR_ID	= 0;
	private static final int HIGHLIGHT_COLOR_ID	= 1;
	private static final int VALID_COLOR_ID		= 2;
	private static final Color VALID_COLOR		= new Color(190, 217, 115) ; // new Color(254,204,153)
	private int LEFT_MARGIN		= s20;
	private int RIGHT_MARGIN	= LEFT_MARGIN;
	private int VERTICAL_GAP	= s10;

	private final StringList languageNames = new StringList(LanguageManager.current().languageNames());
	private final StringList languageCodes = new StringList(LanguageManager.current().languageCodes());
	private final String defaultLanguage = languageNames.getFirst();
	private int selectedLanguageIdx = LanguageManager.selectedLanguage();
	private String globalLanguage = languageNames.get(selectedLanguageIdx);
	private int leftLanguageButtonId, rightLanguageButtonId;
	private String leftLanguageDir, rightLanguageDir;

	private boolean canceled = false;
	private boolean updating;
	private AllSpeciesAttributes settings;
	private ArrayList<SettingField> fields;

	private StringList languageButtonList; // Dynamic
	private StringList civilizationButtonList;	// Dynamic
	private Map<String, Integer> languageTextColor = new HashMap<>();
	private Map<String, Integer> civilizationTextColor = new HashMap<>();
	private SettingField keyField;

	// Panels
	private ContentPanel contentPane;
	private PageSelectionPane pageSelectionPane;
	private BookPane bookPane;
	private LangageSelection langageSelection; // ComboBox
	private SelectionBars bars;

	// ========================================================================
	// #=== Initializers
	//
	private void updating(boolean b)	{ updating = b; }
	private boolean preTwoLanguages()	{
		leftLanguageButtonId = 0;
		//rightLanguageButtonId = 0;
		if (languageButtonList.size() == 1)
			return false;

		// Check if global language is selected
		int selectedIndex = languageButtonList.getSelectedIndex();
		leftLanguageButtonId = selectedIndex;
		String localLanguage = languageButtonList.getFromSelectedIndex();
		boolean isUiLanguage = localLanguage.equals(globalLanguage);
		if (isUiLanguage) {
//			if (leftLanguageButtonId == rightLanguageButtonId) {
//				rightLanguageButtonId = 1;
//			}
//			return true;
			return false;
		}

		// Check if global language is in the list
		int globalId = languageButtonList.indexOf(globalLanguage);
		if (globalId >= 0) {
			rightLanguageButtonId = selectedIndex;
			leftLanguageButtonId = globalId;
			return true;
		}

		// Check if default language is in the list
		int defaultId = languageButtonList.indexOf(defaultLanguage);
		if (selectedIndex == defaultId)
			return false;
		if (defaultId > 0) {
			rightLanguageButtonId = selectedIndex;
			leftLanguageButtonId = defaultId;
			return true;
		}

		// Check if first of the list is selected
		if (selectedIndex == 0)
			return false;
		rightLanguageButtonId = selectedIndex;
		leftLanguageButtonId = 0;
		return true;
	}
	private boolean twoLanguages()	{
		boolean twoLanguages = preTwoLanguages();
		leftLanguageDir = langDirFromButtonId(leftLanguageButtonId);
		rightLanguageDir = langDirFromButtonId(rightLanguageButtonId);
		return twoLanguages;
	}
	private String langDirFromButtonId(int idx)	{
		String langName = languageButtonList.get(idx);
		int langManagerIdx = languageNames.indexOf(langName);
		return languageCodes.get(langManagerIdx);
	}
	private String langDirFromlangName(String langName)	{
		int langManagerIdx = languageNames.indexOf(langName);
		return languageCodes.get(langManagerIdx);
	}
	private String langDirFromButtonList()	{
		String langName = languageButtonList.getFromSelectedIndex();
		int langManagerIdx = languageNames.indexOf(langName);
		return languageCodes.get(langManagerIdx);
	}
	private StringList remainingLang()	{
		StringList list = new StringList(languageNames);
		list.removeAll(languageButtonList);
		return list;
	}
	private String nextLanguage()	{
		String nextLanguage = remainingLang().getFirst();
		languageNames.setSelectedIndex(nextLanguage);
		return nextLanguage;
	}
	CustomNameUI(BaseModPanel parent, AllSpeciesAttributes settings) {
		super(parent);
		setName("CustomNameUI");
		this.settings = settings;
	}
	boolean showPanel()	{
		contentPane = new ContentPanel();
		setContentPane(contentPane);
		initTooltips();

		setTitle(text(ROOT + "NAMES_TITLE"));

		pack();
		setLocationRelativeTo(parent);
		setModal(true);
		setVisible(true);

		return canceled;
	}
	private void initLists() {
		int currentId = LanguageManager.selectedLanguage();
		languageNames.setSelectedIndex(currentId);
		languageCodes.setSelectedIndex(currentId);
		languageButtonList = settings.getLanguageNames();
		civilizationButtonList = settings.getCivilizationsNames();
	}
	// -#-
	// ========================================================================
	// #=== Button Text Color
	//
	private Color getTextColor(Integer colorId)	{
		if (colorId == null)
			return buttonTextColor();
		switch (colorId) {
		case VALID_COLOR_ID:
			return VALID_COLOR;
		default:
			return buttonTextColor();
		}
	}
	private boolean languageColorHasChanged(String langDir, String langName)	{
		if (languageTextColor.containsKey(langName)) {
			int colorId = languageTextColor.get(langName);
			boolean isAnimAutonomous = settings.isAnimAutonomous(langDir);
			int newColor = isAnimAutonomous? VALID_COLOR_ID : DEFAULT_COLOR_ID;
			if (newColor == colorId)
				return false;
			languageTextColor.put(langName, newColor);
			return true;
		}
		else {
			boolean isAnimAutonomous = settings.isAnimAutonomous(langDir);
			int newColor = isAnimAutonomous? VALID_COLOR_ID : DEFAULT_COLOR_ID;
			languageTextColor.put(langName, newColor);
			return true;
		}
	}
	private boolean civilizationColorHasChanged(String langDir, String civName, int idx)	{
		if (civilizationTextColor.containsKey(civName)) {
			int colorId = civilizationTextColor.get(civName);
			boolean isAnimAutonomous = settings.isAnimAutonomous(langDir, idx);
			int newColor = isAnimAutonomous? VALID_COLOR_ID : DEFAULT_COLOR_ID;
			if (newColor == colorId)
				return false;
			civilizationTextColor.put(civName, newColor);
			return true;
		}
		else {
			boolean isAnimAutonomous = settings.isAnimAutonomous(langDir, idx);
			isAnimAutonomous = settings.isAnimAutonomous(langDir, idx);
			int newColor = isAnimAutonomous? VALID_COLOR_ID : DEFAULT_COLOR_ID;
			civilizationTextColor.put(civName, newColor);
			return true;
		}
	}
	private void validateButtonTextColor()	{
		String currentLangName = languageButtonList.getFromSelectedIndex();
		String currentLangDir = langDirFromlangName(currentLangName);
		// loop through civilizations for current language
		boolean langAutonomous = true;
		int idx = 0;
		// System.out.println("civilizationButtonList: " + civilizationButtonList.toString()); // TO DO BR: comment
		for (String civName : civilizationButtonList) {
			if (civilizationColorHasChanged(currentLangDir, civName, idx))
				bars.selectCivilization.repaintButton(idx);
			langAutonomous &= (VALID_COLOR_ID == civilizationTextColor.get(civName));
			idx++;
		}
		// update current language
		if (languageTextColor.containsKey(currentLangName)) {
			int colorId = languageTextColor.get(currentLangName);
			int newColor = langAutonomous? VALID_COLOR_ID : DEFAULT_COLOR_ID;
			if (newColor != colorId) {
				languageTextColor.put(currentLangName, newColor);
				bars.selectLanguage.repaintButton(languageButtonList.getSelectedIndex());
			}
		}
		else {
			int newColor = langAutonomous? VALID_COLOR_ID : DEFAULT_COLOR_ID;
			languageTextColor.put(currentLangName, newColor);
			bars.selectLanguage.repaintButton(languageButtonList.getSelectedIndex());
		}

		// loop through other languages
		idx = 0;
		for (String langName : languageButtonList) {
			if (!langName.equals(currentLangName)) {
				String langDir = langDirFromlangName(langName);
				if (languageColorHasChanged(langDir, langName))
					bars.selectLanguage.repaintButton(idx);
			}
			idx++;
		}
	}
	@Override public void actionPerformed(ActionEvent e) { validateButtonTextColor(); }
	private void reCenter() {
		Point pLoc = parent.getLocationOnScreen();
		Dimension pSize = parent.getSize();
		Dimension cSize = getSize();
		int x = pLoc.x + (pSize.width - cSize.width)/2;
		int y = pLoc.y + (pSize.height - cSize.height)/2;
		setLocation(x, y);
	}
	private class LanguageBarListener implements ButtonBarListener	{
		@Override public void actionPerformed(BarEvent e) {
			if (updating)
				return;

			switch (e.event) {
				case BUTTON_ADDED:
					languageTextColor.put(e.newLabel, UNKNOWN_COLOR_ID);
					civilizationTextColor.clear();
					break;
				case BUTTON_REMOVED:
					languageTextColor.remove(e.prevLabel);
					civilizationTextColor.clear();
					break;
				case BUTTON_SELECTED:
					civilizationTextColor.clear();
					break;
				case BUTTON_RENAMED:
					languageTextColor.remove(e.prevLabel);
					languageTextColor.put(e.newLabel, DEFAULT_COLOR_ID);
					civilizationTextColor.clear();
					// System.out.println("LanguageBarListener: Button renamed " + e.toString()); // TO DO BR: comment
					break;
			}
			languageNames.setSelectedIndex(e.newLabel);
			pageSelectionPane.buildPanel();
		}
	}
	private class CivilizationBarListener implements ButtonBarListener	{
		@Override public void actionPerformed(BarEvent e) {
			if (updating)
				return;

			switch (e.event) {
				case BUTTON_ADDED:
					civilizationTextColor.put(e.newLabel, UNKNOWN_COLOR_ID);
					for (String langName : languageButtonList)
						languageTextColor.put(langName, UNKNOWN_COLOR_ID);
					settings.insert(e.index, e.newLabel);
					break;
				case BUTTON_REMOVED:
					civilizationTextColor.remove(e.prevLabel);
					for (String langName : languageButtonList) {
						int langColor = languageTextColor.get(langName);
						if (langColor != VALID_COLOR_ID)
							languageTextColor.put(langName, UNKNOWN_COLOR_ID);
					}
					settings.delete(e.index);
					break;
				case BUTTON_SELECTED:
					// System.out.println("EmpireBarListener: Button selected " + e.toString()); // TO DO BR: comment
					break;
				case BUTTON_RENAMED:
					int prev = civilizationTextColor.remove(e.prevLabel);
					civilizationTextColor.put(e.newLabel, prev);
					// System.out.println("EmpireBarListener: Button renamed " + e.toString()); // TO DO BR: comment
					pageSelectionPane.refreshBar();
					return;
			}
			pageSelectionPane.buildPanel();
		}
	}
	// -#-
	// ========================================================================
	// #=== Level 1: Content Panel (set as content pane to be able to access paintComponent to gives a background
	//
	private class ContentPanel extends RContentPanel {
		private static final long serialVersionUID = 1L;
		private static final String NAME = "MainPanel";
		private ComponentPositioner positioner = new ComponentPositioner();
		private Dimension lastSize;

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

		private class ComponentPositioner extends ComponentAdapter {
				@Override public void componentResized(ComponentEvent evt) {
					if (lastSize.equals(getSize()))
						return;
					lastSize = getSize();
					reCenter();
				}
		}
	}
	// -#-
	// ========================================================================
	// #=== Level 2: ==> Selection Panel
	//
	private class PageSelectionPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		PageSelectionPane() {
			setOpaque(false);
			setLayout(new GridBagLayout());
		}
		private void buildPanel() {
			if (updating)
				return;
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
		}
		private void refreshBar()	{
			updating(true);
			if (bars != null)
				remove(bars);
			bars = new SelectionBars();
			bars.buildPanel();
			GridBagConstraints gbc = newGbc(0,0, REMAINDER,1, 0,0, CENTER, HORIZONTAL, new Insets(0, LEFT_MARGIN, 0, RIGHT_MARGIN), 0,0);
			add(bars, gbc);
			pack();
			updating(false);
		}
		private void updateLanguage(String language)	{
			// System.out.println("update Language to " + language + " updating = " + updating); // TO DO BR: comment
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
	// -#-
	// ========================================================================
	// #=== Level 3: Book Pane
	//
	private class BookPane extends JPanel	{
		private static final long serialVersionUID = 1L;
		private GridBagConstraints c;
		private PagePane page0, page1;

		private BookPane(String name)	{
			setName(name);
			setOpaque(false);
			setForeground(Color.black);
			Border border = new LineBorder(GameUI.raceEdgeColor(), s2);
			setBorder(border);
			setLayout(new GridBagLayout());
			buildPanel();
		}
		private BookPane() {
			setOpaque(false);
			setForeground(Color.black);
			Border border = new LineBorder(GameUI.raceEdgeColor(), s2);
			setBorder(border);
			setLayout(new GridBagLayout());
		}
		private PagePane page0(boolean single) {
			if (page0 == null)
				page0 = new PagePane(true, single);
			return page0;
		}
		private PagePane page1(boolean single) {
			if (page1 == null)
				page1 = new PagePane(false, false);
			return page1;
		}
		private void buildPanel() {
			updating(true);
			c = new GridBagConstraints();
			if (twoLanguages())
				sidedLanguage();
			else
				singleLanguage();
			updating(false);
		}
		private void singleLanguage() {
			c.insets = new Insets(s5, s5, s5, s5);
			c.gridx = 0;
			c.gridy = 0;
			c.weightx = 1;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.CENTER;
			add(page0(true), c);
		}
		private void sidedLanguage() {
			GridBagConstraints gbc = newGbc(0, 0, 1, 1, 0, 0, 7, NONE, new Insets(s5, s5, s5, s5), 0, 0);
			add(page0(false), gbc);
			gbc.gridx++;
			new RSeparator(this, false, s2, null, gbc.gridx, gbc.gridy);
			gbc.gridy = 0;
			gbc.gridx++;
			c.anchor = GridBagConstraints.EAST;
			add(page1(false), gbc);
		}
	}
	// -#-
	// ========================================================================
	// #=== Level 4: Page Pane
	//
	private class PagePane extends JPanel	{
		private static final long serialVersionUID = 1L;
		String language, languageDir;
		int languageId, civilizationId;
		private PagePane(boolean left, boolean single)	{
			if (left) {
				languageId	= leftLanguageButtonId;
				languageDir	= leftLanguageDir;
			}
			else {
				languageId	= rightLanguageButtonId;
				languageDir	= rightLanguageDir;
			}
			language = languageButtonList.get(languageId);
			int lgManagerId = languageNames.indexOf(language);

			civilizationId = civilizationButtonList.getSelectedIndex();
			// System.out.println("Page left = " + left + " Language = " + language); // TO DO BR: comment
			setOpaque(false);
			setForeground(Color.black);
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			// Mandatory fields for Animation selection
			Color headersColor = Color.BLACK;
			c.insets = ZERO_INSETS;
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 1;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.WEST;
			String str = text("CUSTOM_RACE_MANDATORY_FIELDS");
			RLabel mandatory = new RLabel(str);
			mandatory.setForeground(headersColor);
			add(mandatory, c);

			// language
			c.insets = ZERO_INSETS;
			c.gridx = 1;
			c.gridy = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
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

			// From Name text file
			// Empire dependent fields
			// ==> EmpireNameItems
			int x = 0;
			int y = 1;
			SpeciesAttributes identifications = settings.getAttributes(languageDir);
			CivilizationNameItems civilizationNameItems = identifications.civilizationNameItems;
			boolean first = true;
			for (SettingString setting : civilizationNameItems) {
				SettingField sf = new SettingField(this, setting, NORM_FIELDS_COL, x, y, languageDir, civilizationId);
				if (first && left) {
					addChangeListener(sf, e -> bars.empireNameChangedAction(e));
					first = false;
				}
				y++;
			}

			// Copy Language Button
			c.gridx = 0;
			c.gridy = y;
			if (!single) {
				c.insets = BUTTON_INSETS;
				c.gridx = 1;
				c.gridy = y+1;
				c.gridheight = 2;
				c.anchor = GridBagConstraints.EAST;
				add(new CopyLanguageButton(left), c);
			}

			// Common to the species (UI)
			c.insets = new Insets(s5, 0, 0, 0);
			c.gridx = 0;
			c.gridy = y;
			y++;
			c.gridheight = 1;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.WEST;
			str = text("CUSTOM_RACE_SPECIES_FIELDS");
			RLabel common = new RLabel(str);
			common.setForeground(headersColor);
			add(common, c);

			// ==> SpeciesNameItems
			SpeciesNameItems speciesNameItems = identifications.speciesNameItems;
			for (SettingString setting : speciesNameItems) {
				new SettingField(this, setting, NORM_FIELDS_COL, x, y, languageDir, civilizationId);
				y++;
			}

			// ==> SpeciesDescriptionItems
			SpeciesDescriptionItems speciesDescriptionItems = identifications.speciesDescriptionItems;
			for (SettingString setting : speciesDescriptionItems) {
				new SettingField(this, setting, WIDE_FIELDS_COL, x, y, languageDir, civilizationId);
				y++;
			}

			// Fill From Animations Button
			c.gridx = 0;
			c.gridy = y;
			if (left && languageId < 2 && settings.hasAnim()) {
				c.gridy +=1;
				c.gridx = 1;
				c.insets = BUTTON_INSETS;
				c.gridheight = 2;
				c.anchor = GridBagConstraints.EAST;
				add(new FillFromAnimButton(), c);
			}

			// Common dialogs to the species
			c.insets = new Insets(s5, 0, 0, 0);
			c.gridx = 0;
			c.gridy = y;
			y++;
			c.gridheight = 1;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.WEST;
			str = text("CUSTOM_RACE_SPECIES_DIALOG_FIELDS");
			RLabel speciesDialog = new RLabel(str);
			speciesDialog.setForeground(headersColor);
			add(speciesDialog, c);

			// ==> SpeciesLabelItems
			SpeciesLabelItems speciesLabelItems = identifications.speciesLabelItems;
			for (SettingString setting : speciesLabelItems) {
				new SettingField(this, setting, NORM_FIELDS_COL, x, y, languageDir, civilizationId);
				y++;
			}

			// Multiple Fields
			// dialog to the civilization
			c.insets = new Insets(s5, 0, 0, 0);
			c.gridx = 0;
			c.gridy = y;
			y++;
			c.gridheight = 1;
			c.gridwidth = 2;
			c.anchor = GridBagConstraints.WEST;
			str = text("CUSTOM_RACE_ETHNIC_DIALOG_FIELDS");
			RLabel civDialog = new RLabel(str);
			civDialog.setForeground(headersColor);
			add(civDialog, c);

			// Fill From Names Button
			c.gridx = 0;
			c.gridy = y;
			if (lgManagerId < 2) {
				c.insets = BUTTON_INSETS;
				c.gridx = 1;
				c.gridy = y+1;
				c.gridheight = 3;
				c.anchor = GridBagConstraints.EAST;
				add(new FillFromNamesButton(languageDir), c);
			}
			y++;
			// ==> EmpireLabelItems
			for (SettingString setting : identifications.civilizationLabelItems) {
				new SettingField(this, setting, NORM_FIELDS_COL, x, y, languageDir, civilizationId);
				y++;
			}
		}
	}
	// ========================================================================
	// Level 4: Selection Bar
	//
	private class SelectionBars extends JPanel	{
		private static final long serialVersionUID = 1L;
		private RButtonBar selectCivilization, selectLanguage;
		private SelectionBars() {
			setName("SelectionPane");
			setOpaque(false);
			setLayout(new GridBagLayout());
			int x = 0;
			int y = 0;
			selectCivilization = new RButtonBar(civilizationButtonList, ITEM_ID, true, true, false);
			selectCivilization.setButtonBarListener(new CivilizationBarListener());
			selectCivilization.setTextColorGetter((idx, name) -> getCivilizationTextColor(idx, name));
			GridBagConstraints gbc = newGbc(x,y, 1,1, 0,0, WEST, NONE, ZERO_INSETS, 0,0);
			add(selectCivilization, gbc);

			x++;
			addVariableSpace(this, x, y);

			selectLanguage = new RButtonBar(languageButtonList, LANGUAGE_ID, false, true, true);
			selectLanguage.setButtonBarListener(new LanguageBarListener());
			selectLanguage.setNewTextRequest(new NextName());
			selectLanguage.setTextColorGetter((idx, name) -> getLanguageTextColor(idx, name));
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
			selectCivilization = new RButtonBar(civilizationButtonList, ITEM_ID, true, true, false);
			selectCivilization.setButtonBarListener(new CivilizationBarListener());
			GridBagConstraints gbc = newGbc(x,y, 1,1, 0,0, WEST, NONE, ZERO_INSETS, 0,0);
			add(selectCivilization, gbc);

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
		void empireNameChangedAction(ChangeEvent e)	{
			SettingField src = (SettingField) e.getSource();
			String text = src.getText();
			if (text.isBlank())
				return;
			selectCivilization.renameSelected(text);
		}
		Color getCivilizationTextColor(int idx, String name)	{
			Integer colorId = civilizationTextColor.get(name);
			Color color = getTextColor(colorId);
			return color;
		}
		Color getLanguageTextColor(int idx, String name)	{
			Integer colorId = languageTextColor.get(name);
			Color color = getTextColor(colorId);
			return color;
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
	private class FillFromAnimButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private FillFromAnimButton()	{
			super("CUSTOM_RACE_FILL_FROM_ANIM_BUTTON");
			setLabelKey();
			addActionListener(e -> fillFromAnim(e));
		}
		private void fillFromAnim(ActionEvent evt)	{
			boolean forced = isForced(evt);
			if (isForAllCiv(evt)) {
				int size = civilizationButtonList.size();
				for (int civIdx=0; civIdx<size; civIdx++) {
					String civName = civilizationButtonList.getFromSelectedIndex();
					settings.fillFromAnim(forced, civIdx, civName);
				}
			}
			else {
				int civIdx  = civilizationButtonList.getSelectedIndex();
				String civName = civilizationButtonList.getFromSelectedIndex();
				settings.fillFromAnim(forced, civIdx, civName);
			}
			pageSelectionPane.buildPanel();
		}
	}
	private class FillFromNamesButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private FillFromNamesButton(String langDir)	{
			super("CUSTOM_RACE_FILL_FROM_NAME_BUTTON");
			setLabelKey();
			addActionListener(e -> fillFromFromNames(e, langDir));
		}
		private void fillFromFromNames(ActionEvent evt, String langDir)	{
			boolean forced = isForced(evt);
			if (isForAllCiv(evt)) {
				int size = civilizationButtonList.size();
				for (int civIdx=0; civIdx<size; civIdx++)
					settings.fillLabelsFromNames(langDir, civIdx, forced);
			}
			else {
				int civIdx  = civilizationButtonList.getSelectedIndex();
				settings.fillLabelsFromNames(langDir, civIdx, forced);
			}
			pageSelectionPane.buildPanel();
		}
	}
	private class CopyLanguageButton extends RButton	{
		private static final long serialVersionUID = 1L;
		private CopyLanguageButton(boolean toLeft)	{
			super(toLeft? "CUSTOM_RACE_COPY_FROM_RIGHT_BUTTON" : "CUSTOM_RACE_COPY_FROM_LEFT_BUTTON");
			setLabelKey();
			addActionListener(e -> copyLanguage(e, toLeft));
		}
		private void copyLanguage(ActionEvent evt, boolean toLeft)	{
			int destId = toLeft? leftLanguageButtonId : rightLanguageButtonId;
			int srcId  = toLeft? rightLanguageButtonId : leftLanguageButtonId;
			String destLangDir = langDirFromButtonId(destId);
			String srcLangDir  = langDirFromButtonId(srcId);
			boolean forced = isForced(evt);
			if (isForAllCiv(evt)) {
				int size = civilizationButtonList.size();
				for (int civIdx=0; civIdx<size; civIdx++)
					settings.copyFromLanguage(srcLangDir, destLangDir, civIdx, forced);
			}
			else {
				int civIdx  = civilizationButtonList.getSelectedIndex();
				settings.copyFromLanguage(srcLangDir, destLangDir, civIdx, forced);
			}
			pageSelectionPane.buildPanel();
		}
	}
	private boolean isForced(ActionEvent evt) { return (evt.getModifiers() & ActionEvent.SHIFT_MASK) > 0; }
	private boolean isForAllCiv(ActionEvent evt) { return (evt.getModifiers() & ActionEvent.CTRL_MASK) > 0; }
	// ========================================================================
	// Other Specific Components definition
	//
	
	private class LangageSelection extends JComboBox<String>	{
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
				// System.out.println("LanguageAction " + evt.getActionCommand()); // TO DO BR: comment
				String language = (String) getSelectedItem();
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
	// -#-
}
