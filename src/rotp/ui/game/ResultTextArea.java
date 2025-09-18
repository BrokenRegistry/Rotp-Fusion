package rotp.ui.game;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import rotp.ui.util.IParam.ParamSearchList;
import rotp.ui.util.IParam.ParamSearchResult;

class ResultTextArea extends JTextArea implements MouseListener, MouseMotionListener, MouseWheelListener	{
	private static final long serialVersionUID = 1L;
	private static final String CRLF = System.lineSeparator();
	private final BaseCompactOptionsUI parent;
	ParamSearchList finalList;
	ParamSearchResult selectedResult;
	private boolean mouseHoverParam;
	//IParam selectedParam; 

	ResultTextArea(BaseCompactOptionsUI parent)	{
		super();
		this.parent = parent;
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	void updateText()	{ setText(finalList.toString(CRLF, false)); }
	boolean hasMouse()	{
		Point loc = MouseInfo.getPointerInfo().getLocation();
		if (loc == null)
			return false;
		SwingUtilities.convertPointFromScreen(loc, this);
		return contains(loc);
	}
	private ParamSearchResult selectParam(MouseEvent e)	{
		// Locate result
		setCaretPosition(viewToModel2D(e.getPoint()));
		String str = getText().substring(0, getCaretPosition());
		int id = StringUtils.countMatches(str, CRLF);
		if (id >= finalList.size() || id < 0) {
			System.err.println("Param null returned"); // TO DO BR: REMOVE
			return null;
		}
		selectedResult = finalList.get(id);
		// Panel or param?
		int sepCount = StringUtils.countMatches(str, ParamSearchResult.COL_SEP);
		mouseHoverParam = sepCount > id;
		return selectedResult;
	}

	@Override public void mouseMoved(MouseEvent e)		{ selectParam(e); }
	@Override public void mouseDragged(MouseEvent e)	{ }
	@Override public void mouseClicked(MouseEvent e)	{ }
	@Override public void mousePressed(MouseEvent e)	{ }
	@Override public void mouseReleased(MouseEvent e)	{
		if (selectParam(e) == null)
			return;
		if (mouseHoverParam || selectedResult.ui == null)
			parent.searchMouseAction(e, null, selectedResult.param);
		else
			parent.searchMouseAction(e, null, selectedResult.ui);
		updateText();
		repaint();
	}
	@Override public void mouseEntered(MouseEvent e)	{ }
	@Override public void mouseExited(MouseEvent e)		{ parent.mouseExited(e); }
	@Override public void mouseWheelMoved(MouseWheelEvent e)	{
		if (selectParam(e) == null)
			return;
			parent.searchMouseAction(null, e, selectedResult.param);
		updateText();
		repaint();
	}
}
