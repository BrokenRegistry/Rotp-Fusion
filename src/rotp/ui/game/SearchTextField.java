package rotp.ui.game;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import rotp.model.game.SafeListPanel;
import rotp.ui.BaseTextField;
import rotp.ui.util.IParam;
import rotp.ui.util.IParam.ParamSearchList;
import rotp.ui.util.IParam.ParamSearchResult;
import rotp.util.LanguageManager;

class SearchTextField extends BaseTextField	{
	private static final long serialVersionUID = 1L;
	private static final int MIN_PCT = 100;
	private static final int MAX_PARAM = 10;
	
	private final BaseCompactOptionsUI parent;
	private SafeListPanel optionsPanel;
	ResultTextArea resultField;
	//ParamSearchList finalList;
	private boolean newSearchResults;

	SearchTextField(BaseCompactOptionsUI parent)	{
		super(parent);
		this.parent = parent;
		resultField = new ResultTextArea(parent);
		getDocument().addDocumentListener(new SearchFieldListener());
	}

	void setOptions(SafeListPanel optionsPanel)	{ this.optionsPanel = optionsPanel; }
	private boolean hasMouse()	{
		Point loc = MouseInfo.getPointerInfo().getLocation();
		if (loc == null)
			return false;
		SwingUtilities.convertPointFromScreen(loc, this);
		return contains(loc);
	}

	private void validFocus()	{
		if (hasFocus())
			return;
		if (hasMouse() || resultField.hasMouse())
			requestFocus();
	}
	void clearResult()	{
		resultField.finalList = null;
		resultField.setText("");
		resultField.setVisible(false);
		parent.refreshGui(0);
		validFocus();
	}
	public IParam getHoveredParam()	{ return resultField.selectedResult.param; }
	public IParam getHoveredUI()	{ return resultField.selectedResult.ui; }
	void search()	{
		String toSearch = getText();
		if (toSearch.length() < 1) {
			clearResult();
			return;
		}
		boolean stripAccent = !LanguageManager.isDefaultLanguage();
		ParamSearchList paramList = optionsPanel.getSearchList(null, toSearch, MIN_PCT, stripAccent);
		newSearchResults = false;
		if (paramList.isEmpty()) {
			clearResult();
			return;
		}

		paramList.sort();
		resultField.finalList = new ParamSearchList();
		for (ParamSearchResult p : paramList) {
			if (resultField.finalList.size() >= MAX_PARAM)
				break;
			resultField.finalList.smartAdd(p);
		}
		newSearchResults = true;
	}
	@Override public void paintComponent(Graphics g0)	{
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		paintSearchResults(g);
	}
	private void paintSearchResults(Graphics2D g)	{
		validFocus();
		if (resultField.finalList == null) {
			return;
		}
		int resSize = resultField.finalList.size();
		if (resSize == 0)
			return;
		if (newSearchResults) {
			resultField.updateText();
			resultField.setVisible(true);
			newSearchResults = false;
			requestFocus();
		}
		validFocus();
	}
	private class SearchFieldListener implements DocumentListener	{
		@Override public void changedUpdate(DocumentEvent evt)	{ search(); }
		@Override public void removeUpdate(DocumentEvent evt)	{ search(); }
		@Override public void insertUpdate(DocumentEvent evt)	{ search(); }
	}
}
