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
	private SearchLoop lastSearch = new SearchLoop();

	SearchTextField(BaseCompactOptionsUI parent)	{
		super(parent);
		this.parent = parent;
		resultField = new ResultTextArea(parent);
		getDocument().addDocumentListener(new SearchFieldListener());
	}

	void setOptions(SafeListPanel optionsPanel)	{ this.optionsPanel = optionsPanel; }
	private boolean newSearchResults()			{ return lastSearch.newSearchResults; }
	private void newSearchResults(boolean b)	{ lastSearch.newSearchResults = false; }
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
	public IParam<?> getHoveredParam()	{ return resultField.selectedResult.param; }
	public IParam<?> getHoveredUI()		{ return resultField.selectedResult.ui; }
	@Override public void paintComponent(Graphics g0)	{
		if (parent.guidePopUp.isVisible())
			return;
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		paintSearchResults(g);
	}
	private void paintSearchResults(Graphics2D g)	{
		validFocus();
		if (resultField.finalList == null)
			return;
		int resSize = resultField.finalList.size();
		if (resSize == 0)
			return;
		if (newSearchResults()) {
			resultField.updateText();
			resultField.setVisible(true);
			newSearchResults(false);
			requestFocus();
		}
		validFocus();
	}
	void newSearch()	{
		lastSearch = new SearchLoop();
		lastSearch.start();
	}
	private class SearchFieldListener implements DocumentListener	{
		@Override public void changedUpdate(DocumentEvent evt)	{ newSearch(); }
		@Override public void removeUpdate(DocumentEvent evt)	{ newSearch(); }
		@Override public void insertUpdate(DocumentEvent evt)	{ newSearch(); }
	}

	private class SearchLoop {
		private boolean newSearchResults = false;
		ParamSearchList finalList;
		Search search;
		Thread searchThread;
		private void start()	{
			resultField.finalList = new ParamSearchList();
			finalList = resultField.finalList;
			search = new Search();
			searchThread = new Thread(search, "SearchLoop");
			searchThread.start();
		}
		private class Search implements Runnable {
			@Override public void run() {
				clearResult();
				resultField.finalList = new ParamSearchList();
				finalList = resultField.finalList;
				newSearchResults = false;
				String toSearch = getText();
				if (toSearch.length() < 1)
					return;

				boolean stripAccent = !LanguageManager.isDefaultLanguage();
				ParamSearchList paramList = optionsPanel.getSearchList(null, toSearch.toLowerCase(), MIN_PCT, stripAccent);
				if (paramList.isEmpty())
					return;

				paramList.sort();
				for (ParamSearchResult p : paramList) {
					if (finalList.size() >= MAX_PARAM)
						break;
					finalList.smartAdd(p);
				}
				newSearchResults = true;
			}
		}
	}
}
