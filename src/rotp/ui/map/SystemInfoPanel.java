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
package rotp.ui.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import rotp.model.empires.Empire;
import rotp.model.empires.SystemView;
import rotp.model.events.StarSystemEvent;
import rotp.model.galaxy.StarSystem;
import rotp.ui.BasePanel;
import rotp.ui.main.MainUI;
import rotp.ui.main.SystemPanel;
import rotp.util.Palette;

public class SystemInfoPanel extends SystemPanel implements MouseMotionListener {
    private static final long serialVersionUID = 1L;
    private static final Color selectedC = new Color(178,124,87);
    private static final Color unselectedC = new Color(112,85,68);
    private static Palette palette;
    EmpireInfoGraphicPane graphicPane;
    SystemsUI parent;
    SystemSummaryPane summaryPane;
    SystemFlagsPane systemFlagsPane;
    public SystemInfoPanel(SystemsUI p) {
        parent = p;
        init0();
    }
    public void init() {
        systemFlagsPane.init();
    }
    public void exit() {
        systemFlagsPane.exit();
    }
    private void init0() {
        palette = Palette.named("Brown");
        setOpaque(true);
        setBackground(selectedC);
        setBorder(newEmptyBorder(6,6,6,6));
        setPreferredSize(new Dimension(scaled(250), getHeight()));

        graphicPane = new EmpireInfoGraphicPane(this);
        graphicPane.setPreferredSize(new Dimension(getWidth(),scaled(140)));

        detailPane = detailPane();
        BorderLayout layout = new BorderLayout();
        layout.setVgap(s6);
        setLayout(layout);
        add(topPane(), BorderLayout.NORTH);
        add(detailPane, BorderLayout.CENTER);

        addMouseMotionListener(this);
    }
    public void forceAutoFlagColor(boolean all) {
        summaryPane.forceAutoFlagColor(all);
    }
    public void toggleFlagColor(boolean rightClick) {
        summaryPane.toggleFlagColor(rightClick);
    }
	@Override public IMapHandler mapHandler()	{ return parent; }
    @Override
    public String subPanelTextureName()    { return TEXTURE_BROWN; }
    @Override
    public StarSystem systemViewToDisplay() { return selectedSystem(); }
    @Override
    public void animate() { 
        graphicPane.animate(); 
        detailPane.animate();
    }
    @Override
    protected BasePanel topPane() {
        return graphicPane;
    }
    @Override
    protected BasePanel detailPane() {
        summaryPane = new SystemSummaryPane();

        systemFlagsPane = new SystemFlagsPane();

        BasePanel systemEventsPanel = new SystemHistoryPane();
        systemEventsPanel.setPreferredSize(new Dimension(getWidth(), scaled(250)));

        BasePanel empireDetailPane = new BasePanel();
        empireDetailPane.setOpaque(false);
        empireDetailPane.setLayout(new BorderLayout(0,s5));
        empireDetailPane.add(summaryPane, BorderLayout.NORTH);
        empireDetailPane.add(systemFlagsPane, BorderLayout.CENTER);
        empireDetailPane.add(systemEventsPanel, BorderLayout.SOUTH);
        return empireDetailPane;
    }
    StarSystem selectedSystem() {
        return parent.systemToDisplay();
    }
	@Override public void mouseDragged(MouseEvent e){}
	@Override public void mouseMoved(MouseEvent e)	{}
    class EmpireInfoGraphicPane extends BasePanel implements ActionListener {
        private static final long serialVersionUID = 1L;
        SystemPanel parent;
        Ellipse2D starCircle = new Ellipse2D.Float();
        Ellipse2D planetCircle = new Ellipse2D.Float();
        int currentHover = 0;
        EmpireInfoGraphicPane(SystemPanel p) {
            parent = p;
            init();
        }
        private void init() {
            setBackground(palette.black);
        }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            StarSystem sys = parent.systemViewToDisplay();
            if (sys == null)
                return;

            Empire pl = player();
            int w = getWidth();
            int h = getHeight();

            Graphics2D g2 = (Graphics2D) g;
            g2.drawImage(pl.sv.starBackground(this), 0, 0, null);
            drawStar(g2, selectedSystem().starType(), s80, getWidth()-s20, s20); //modnar: move star due to increased planet size
            starCircle.setFrame((getWidth()/3)-s20, s10, s40, s40);

            g.setFont(narrowFont(36));
            String str = player().sv.knownName(sys.id);
            int y0 = s32; //modnar: move colony name up due to increase planet size
            int x0 = s25;
            drawBorderedString(g, str, 2, x0, y0, Color.black, SystemPanel.orangeText);

            if (!player().sv.isScouted(sys.id))
                return;
            boolean showTransports = sys.canShowIncomingTransports();
            int x1 = showTransports? s10 : s5;
            int y1 = showTransports? s55 : s70/2;
            int r = showTransports? s70 : s80; //modnar: increase planet size
//            int x1 = s5;
//            int y1 = s70/2;
//            int r = s80; //modnar: increase planet size
            selectedSystem().planet().draw(g, w, h, x1, y1, r+r, 45);
            planetCircle.setFrame(x1, y1, r+r, r+r);
            parent.drawPlanetInfo(g2, selectedSystem(), false, false, showTransports, s40, getWidth()+s5, getHeight()-s5);
        }
        @Override
        public void animate() {
            if (animationCount() % 3 == 0) {
                try {
                    selectedSystem().planet().rotate(1);
                    repaint();
                }
                catch (Exception e) { }
            }
        }
    }
    final class SystemSummaryPane extends BasePanel implements MouseMotionListener, MouseListener, MouseWheelListener {
        private static final long serialVersionUID = 1L;
        Shape hoverBox;
        Rectangle flagBox = new Rectangle();
        SystemSummaryPane() {
            setBackground(unselectedC);
            setPreferredSize(new Dimension(getWidth(), s40));
            addMouseListener(this);
            addMouseMotionListener(this);
            addMouseWheelListener(this);
        }
        @Override
        public void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g);
            int w = getWidth();
            StarSystem sys = systemViewToDisplay();
            if (sys == null)
                return;
            int id = sys.id;
            String name = player().sv.descriptiveName(id);
            scaledFont(g, name, w-s50, 24, 14);
            drawShadowedString(g, name, 2, s10, s30, MainUI.shadeBorderC(), SystemPanel.whiteLabelText);
            
            int sz = s60;
            int shX = (options().selectedFlagColorCount() == 1)? 0 : s8; // BR: flagColorCount
            if (hoverBox == flagBox) {
                Image hoverImage = parent.flagHover(sys);
                g.drawImage(hoverImage, w-sz+s15-shX, -s15, sz, sz, null);
            }
            Image flagImage = parent.flagImage(sys);
            g.drawImage(flagImage, w-sz+s15-shX, -s15, sz, sz, null);
            flagBox.setBounds(w-sz+s25,-s15-shX,sz-s20,sz-s10);
        }
        public void forceAutoFlagColor (boolean all) {
            StarSystem sys = systemViewToDisplay();
            player().sv.forceAutoFlagColor(sys.id, all);
        }
        public void toggleFlagColor(boolean rightClick) { // BR: used for "F" vs "Shift-F"
            StarSystem sys = systemViewToDisplay();
            if (rightClick)
                player().sv.resetFlagColor(sys.id);
            else
                player().sv.toggleFlagColor(sys.id, false);
            parent.repaint();
        }
		@Override public void mouseDragged(MouseEvent e)	{}
		@Override public void mouseMoved(MouseEvent e)		{
			if (flagBox.contains(e.getX(), e.getY()))
				hoverBox = hoverBox(flagBox, hoverBox);
			else
				hoverBox = hoverBox(null, hoverBox);
		}
		@Override public void mouseClicked(MouseEvent e)	{}
		@Override public void mousePressed(MouseEvent e)	{}
		@Override public void mouseReleased(MouseEvent e)	{
            boolean rightClick = SwingUtilities.isRightMouseButton(e);
            boolean middleClick = SwingUtilities.isMiddleMouseButton(e);
            if (hoverBox == flagBox) {
            	StarSystem sys = systemViewToDisplay();
            	// BR: if 3 buttons:
            	//   - Middle click = Reset
            	//   - Right click = Reverse
                if (middleClick)
                    player().sv.resetFlagColor(sys.id);
                else if (rightClick)
                	if (has3Buttons())
                        player().sv.toggleFlagColor(sys.id, true);
                	else
                		player().sv.resetFlagColor(sys.id);
                else
                    player().sv.toggleFlagColor(sys.id, false);
           }
        }
		@Override public void mouseEntered(MouseEvent e)	{}
		@Override public void mouseExited(MouseEvent e)		{ hoverBox = hoverBox(null, hoverBox); }
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
        	setModifierKeysState(e); // BR: For the Flag color selection
            if (hoverBox == flagBox) {
                StarSystem sys = systemViewToDisplay();
                if (e.getWheelRotation() < 0)
                    player().sv.toggleFlagColor(sys.id, true);
                else
                    player().sv.toggleFlagColor(sys.id, false);
                parent.repaint();
            }
        }
    }
    final class SystemFlagsPane extends BasePanel implements MouseListener {
        private static final long serialVersionUID = 1L;
        JTextArea comments = new JTextArea();
        StarSystem sys;
        String emptyNotes;
        boolean commentLocSet = false;
        SystemFlagsPane() {
            setBackground(unselectedC);
            add(comments);
            comments.setPreferredSize(new Dimension(getWidth()-s20,s100));
            comments.setBackground(selectedC);
            comments.setForeground(SystemPanel.blackText);
            comments.setFont(narrowFont(17));
            comments.setCaretColor(SystemPanel.blackText);
            comments.putClientProperty("caretWidth", s3);
            comments.setFocusTraversalKeysEnabled(false);
            comments.addMouseListener(this);
        }
        public void init() {
            emptyNotes = text("SYSTEMS_ENTER_NOTES");
        }
        public void exit() {
            sys = null;
        }
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            StarSystem prevSys = sys;
            sys = systemViewToDisplay();
            
            // if we are displaying a different system, updating 
            // the notes in the comments field
            if (sys != prevSys) {
                if (sys == null)
                    comments.setText("");
                else if (sys.notes().isEmpty())
                    comments.setText(emptyNotes);
                else
                    comments.setText(sys.notes());
            }
            if (sys == null)
                return;
            
            int id = sys.id;
            SystemView sv = player().sv.view(id);
            String title = text("SYSTEMS_ALERT");
            g.setFont(narrowFont(22));
            drawShadowedString(g, title, 2, s10, s25, MainUI.shadeBorderC(), SystemPanel.whiteLabelText);
            int sw = g.getFontMetrics().stringWidth(title);
            Color c0 = parent.alertColor(sv);
            if (c0 != null) {
                g.setColor(c0);
                int x0 = s10+sw+s20;
                int y0 = s15;
                g.fillRect(x0-s11, y0-s2, s7, s4);
                g.fillRect(x0+s5, y0-s2, s7, s4);
                g.fillRect(x0-s2, y0-s10, s4, s7);
                g.fillRect(x0-s2, y0+s5, s4, s7);
            }
            
            String desc = parent.alertDescription(sv);
            if (desc == null)
                desc = text("SYSTEMS_NO_ALERTS");
            
            g.setFont(narrowFont(17));
            g.setColor(SystemPanel.blackText);
            List<String> lines = wrappedLines(g, desc, getWidth()-s20);
            int y0 = s47;
            for (String line: lines) {
                drawString(g,line, s10, y0);
                y0 += s18;
            }
            
            comments.setBounds(s5, getHeight()-s100, getWidth()-s10, s95);
        }
		@Override public void mouseClicked(MouseEvent e)	{}
		@Override public void mousePressed(MouseEvent e)	{}
		@Override public void mouseReleased(MouseEvent e)	{}
		@Override public void mouseEntered(MouseEvent e)	{
            parent.animate = false;
            comments.setCaretPosition(comments.getText().length());
            comments.setBounds(s5, getHeight()-s100, getWidth()-s10, s95);
            comments.requestFocus();
            comments.repaint();
        }
        @Override public void mouseExited(MouseEvent e)		{
            parent.animate = true;
            // ensure we update the system's notes when we leave the field
            String notes = comments.getText().trim();
            if (notes.equals(emptyNotes))
                notes = "";
            sys.notes(notes);
            if (notes.isEmpty())
                comments.setText(emptyNotes);
        }
    }
    final class SystemHistoryPane extends BasePanel implements MouseListener, MouseMotionListener, MouseWheelListener {
        private static final long serialVersionUID = 1L;
        private final int lineH = s16;
        private final int scrollH = 3 * lineH;
		private final Color scrollBarC = new Color(211,166,125);
		private final Color scrollTrackC = new Color(90,68,54);
        StarSystem sys, lastSys;
        int offsetY = 0;
		int maxOffsetY = 0;
		int dragStartY = 0;
		int dragStartOffsetY = 0;
		boolean draggingScrollbar = false;
		boolean hoveringScrollbar = false;

        Rectangle eventsBox = new Rectangle();
		Rectangle scrollTrack = new Rectangle();
		Rectangle scrollThumb = new Rectangle();

        SystemHistoryPane() {
            setBackground(unselectedC);
            addMouseListener(this);
			addMouseMotionListener(this);
            addMouseWheelListener(this);
       }
        @Override
        public void paintComponent(Graphics g0) {
            int w = getWidth();
            int h = getHeight();
            Graphics2D g = (Graphics2D) g0;
            super.paintComponent(g);
			scrollTrack.setBounds(0,0,0,0);
			scrollThumb.setBounds(0,0,0,0);
            sys = systemViewToDisplay();
            if (sys != lastSys) {
				lastSys = sys;
				offsetY = 0;
				maxOffsetY = 0;
            }
            if (sys == null)
                return;

            int id = sys.id;
            SystemView sv = player().sv.view(id);
            String title = text("SYSTEMS_HISTORY");
            g.setFont(narrowFont(22));
            drawShadowedString(g, title, 2, s10, s25, MainUI.shadeBorderC(), SystemPanel.whiteLabelText);

            eventsBox.setBounds(s5, s35, w-s10, h-s40);
            //g.setColor(selectedC);
            //g.fill(eventsBox);

            if (!sv.scouted())
                return;

            g.setClip(eventsBox);

            g.setFont(narrowFont(16));
            g.setColor(SystemPanel.blackText);
            int x0 = eventsBox.x+s10;
            int w0 = eventsBox.width-s20;
            // int lineH = s16;
            int lastTurn = sv.lastReportTurn();
            List<StarSystemEvent> reverseEvents = new ArrayList<>(sv.system().events());
            Collections.reverse(reverseEvents);

			int scrollW = s10;
			int textW = w0-s40;
			int contentH = historyHeight(g, reverseEvents, lastTurn, textW);
			boolean needsScroll = (contentH-lineH+s20-s3 > eventsBox.height);
			if (needsScroll) {
				// leave room for the scrollbar without overlapping the text
				textW = textW-scrollW-s5;
				contentH = historyHeight(g, reverseEvents, lastTurn, textW);
			}
			// contentH counts the trailing gap after the last event, so subtract
			// one line height to stop scrolling once the last line is visible
			maxOffsetY = max(0, contentH-lineH+s20-s3-eventsBox.height+s5);
			offsetY = min(maxOffsetY, max(0, offsetY));
			int y0 = eventsBox.y+s20-offsetY;

            for (StarSystemEvent ev: reverseEvents) {
                if (lastTurn >= ev.turn()) {
                    String yr= ev.year();
                    String desc = ev.description();
					List<String> descLines = this.wrappedLines(g, desc, textW);
                    int yrOffset = (descLines.size()-1)*(lineH/2);
                    drawString(g,yr, x0, y0+yrOffset);
                    for (String line: descLines) {
                        drawString(g,line, x0+s40, y0);
                        y0 += lineH;
                    }
                    y0 += s3;
                }
            }
            g.setClip(null);

			if (needsScroll) {
				int trackX = eventsBox.x+eventsBox.width-scrollW-s3;
				int trackY = eventsBox.y+s20;
				int trackH = eventsBox.height-s20-s5;
				int thumbH = max(s12, (int) ((float)trackH*eventsBox.height/(eventsBox.height+maxOffsetY)));
				int thumbY = trackY+(int) ((float)(trackH-thumbH)*offsetY/maxOffsetY);
				scrollTrack.setBounds(trackX, trackY, scrollW, trackH);
				scrollThumb.setBounds(trackX, thumbY, scrollW, thumbH);
				g.setColor(scrollTrackC);
				g.fillRoundRect(trackX, trackY, scrollW, trackH, scrollW, scrollW);
				g.setColor(scrollBarC);
				g.fillRoundRect(trackX, thumbY, scrollW, thumbH, scrollW, scrollW);
				if (hoveringScrollbar) {
					Stroke prev = g.getStroke();
					g.setColor(Color.yellow);
					g.setStroke(stroke2);
					g.drawRoundRect(trackX, thumbY, scrollW, thumbH, scrollW, scrollW);
					g.setStroke(prev);
				}
			}
		}
		private int historyHeight(Graphics g, List<StarSystemEvent> events, int lastTurn, int textW) {
			int height = 0;
			for (StarSystemEvent ev: events)
				if (lastTurn >= ev.turn())
					height += wrappedLines(g, ev.description(), textW).size()*lineH + s3;
			return height;
		}
		private void scroll(int delta) {
			offsetY = max(0, min(maxOffsetY, offsetY+delta));
			repaint();
		}
		@Override public void mouseClicked(MouseEvent e) { 
			if (SwingUtilities.isMiddleMouseButton(e)) {
				offsetY = 0;
				repaint();
			}
		}
		@Override public void mousePressed(MouseEvent e) {
			if (!SwingUtilities.isLeftMouseButton(e))
				return;

			if (scrollThumb.contains(e.getPoint())) {
				draggingScrollbar = true;
				dragStartY = e.getY();
				dragStartOffsetY = offsetY;
			}
			else if (scrollTrack.contains(e.getPoint()) && maxOffsetY > 0) {
				int thumbRange = scrollTrack.height-scrollThumb.height;
				if (thumbRange > 0) {
					int thumbY = e.getY()-scrollTrack.y-scrollThumb.height/2;
					thumbY = max(0, min(thumbRange, thumbY));
					offsetY = (int) ((float)maxOffsetY*thumbY/thumbRange);
					repaint();
				}
			}
		}
		@Override public void mouseReleased(MouseEvent e) {
			draggingScrollbar = false;
		}
		@Override public void mouseDragged(MouseEvent e) {
			if (!draggingScrollbar)
				return;

			int thumbRange = scrollTrack.height-scrollThumb.height;
			if (thumbRange <= 0 || maxOffsetY <= 0)
				return;

			int dy = e.getY()-dragStartY;
			offsetY = max(0, min(maxOffsetY, dragStartOffsetY+(int) ((float)dy*maxOffsetY/thumbRange)));
			repaint();
		}
		@Override public void mouseMoved(MouseEvent e) {
			boolean hovering = scrollThumb.contains(e.getPoint());
			if (hovering != hoveringScrollbar) {
				hoveringScrollbar = hovering;
				repaint();
			}
		}
		@Override public void mouseEntered(MouseEvent e) { }
		@Override public void mouseExited(MouseEvent e)  {
			if (hoveringScrollbar) {
				hoveringScrollbar = false;
				repaint();
			}
		}
		@Override public void mouseWheelMoved(MouseWheelEvent e) { scroll(e.getWheelRotation() > 0 ? scrollH : -scrollH); }
	}
}
