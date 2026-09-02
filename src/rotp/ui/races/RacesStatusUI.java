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
package rotp.ui.races;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rotp.model.empires.Empire;
import rotp.model.empires.EmpireStatus;
import rotp.model.game.IGameOptions;
import rotp.ui.BasePanel;
import rotp.ui.RotPUI;
import rotp.ui.UserPreferences;
import rotp.ui.main.SystemPanel;

public final class RacesStatusUI extends BasePanel implements MouseListener, MouseMotionListener, MouseWheelListener {
    private static final long serialVersionUID = 1L;
	private static Stroke dashedLineStroke;
	private static Stroke dotedLineStroke;

	private final RacesUI parent;
	private Shape hoverShape;
	private int dragY;
	private List<RaceValue> vals = new ArrayList<>();

	private int[] dataY = new int[6];
	private int[] dataYMax = new int[6];
	private Rect[] fullBoxes = new Rect[6];
	private Rect[] dataBoxes = new Rect[6];
	private Rect[] dataScrollers = new Rect[6];
    Rect playerHistoryButton = new Rect();
    Rect aiHistoryButton = new Rect();;
    Rect scaleButton = new Rect();;
    Rect valueButton = new Rect();;

    private LinearGradientPaint backGradient;
	RacesStatusUI(RacesUI p) {
        parent = p;
        dashedLineStroke = new BasicStroke(s2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
        dotedLineStroke = new BasicStroke(s1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{s2, s4}, 0);
        initModel();
    }
	void init()	{
		galaxy().refreshEmpireStatus(); // BR: always up to date
        for (int i=0; i<dataY.length; i++)
            dataY[i] = 0;
    }
    @Override
    public void drawTexture(Graphics g)      { }
    @Override
    public String textureName()     { return TEXTURE_BROWN; }
	void changedEmpire()			{ init(); }
    @Override
    public void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        int w = getWidth();
        int h = getHeight();

        if (backGradient == null) {
            Point2D start = new Point2D.Float(0, getHeight() / 2);
            Point2D end = new Point2D.Float(0, getHeight());
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {RacesUI.darkerBrown, RacesUI.gradientBottom};
            backGradient = new LinearGradientPaint(start, end, dist, colors);
        }
        g.setPaint(backGradient);
        g.fillRect(0,h/2,w, h/2);
        if (parent.selectedEmpire().isPlayer()) 
            paintPlayerData(g);
        else
            paintAIData(g);
    }
    private void initModel() {
        for (int i=0;i<fullBoxes.length;i++) 
            fullBoxes[i] = new Rect();
        for (int i=0;i<dataBoxes.length;i++) 
            dataBoxes[i] = new Rect();
        for (int i=0;i<dataScrollers.length;i++) 
            dataScrollers[i] = new Rect();

        setBackground(RacesUI.darkerBrown);
        setBorder(newEmptyBorder(5,5,5,5));
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
    }
    private void paintPlayerData(Graphics2D g) {
        Empire emp = parent.selectedEmpire();
        int w = getWidth();
        int h = getHeight();

        // modnar: reduce RaceIcon size, increase RankingList size
        int s100 = scaled(100);
        int s110 = scaled(110);
        int s150 = scaled(150);
        int s160 = scaled(160);
        int s210 = scaled(210);
        int buttonTopY = scaled(140);
        int rankingTopY = scaled(170);
        int titleLeftX = scaled(160);
        int s370 = scaled(370);
        drawRaceIconBase(g, emp, s55, s25, s110, s110);
        drawRaceIconBase(g, emp, w-s65-s100, s25, s110, s110);
        drawHistoryButton(g, player(), playerHistoryButton, s55, buttonTopY, s210, s25);
        drawScaleButton(g, buttonTopY);
        aiHistoryButton.setBounds(0,0,0,0);
        drawValueButton(g, w-s65-s150, buttonTopY, s160, s25);
        drawAllRankingsLists(g, emp, s55, rankingTopY, w-s55-s55, h-rankingTopY-s10);
        if (UserPreferences.texturesInterface()) 
            drawTexture(g,0,0,w,h);
        drawRaceIcon(g, emp, s60, s30, s100, s100);
        drawOpponentIcon(g, null, w-s60-s100, s30, s100, s100);
        drawPlayerTitle(g, emp, titleLeftX, s30, s370, s50);
        drawVS(g, emp, titleLeftX, s40, w-scaled(380), s50); 
        drawKnownEmpiresTitle(g, emp, w-s60-s100, s30+s100, 0, s50);
    }
    private void paintAIData(Graphics2D g) {
        Empire emp = parent.selectedEmpire();
        int w = getWidth();
        int h = getHeight();

        // modnar: reduce RaceIcon size, increase RankingList size
        int s100 = scaled(100);
        int s110 = scaled(110);
        //int s160 = scaled(160);
        int s200 = scaled(200);
        int s210 = scaled(210);
        int buttonTopY = scaled(140);
        int rankingTopY = scaled(170);
        int titleLeftX = scaled(160);
        int s370 = scaled(370);
        drawRaceIconBase(g, emp, s55, s25, s110, s110);
        drawRaceIconBase(g, emp, w-s65-s100, s25, s110, s110);
        drawHistoryButton(g, player(), playerHistoryButton, s55, buttonTopY, s210, s25);
        drawScaleButton(g, buttonTopY);
        valueButton.setBounds(0,0,0,0);
        drawHistoryButton(g, emp, aiHistoryButton, w-s65-s200, buttonTopY, s210, s25);
        drawVsRankingsLists(g, emp, s55, rankingTopY, w-s55-s55, h-rankingTopY-s10);
        if (UserPreferences.texturesInterface()) 
            drawTexture(g,0,0,w,h);
        drawRaceIcon(g, emp, s60, s30, s100, s100);
        drawOpponentIcon(g, emp, w-s60-s100, s30, s100, s100);
        drawPlayerTitle(g, emp, titleLeftX, s30, s370, s50);
        drawVS(g, emp, titleLeftX, s40, w-scaled(380), s50);
        drawAITitle(g, emp, w-s60-s100, s30+s100, 0, s50);
    }
    private void drawScaleButton(Graphics2D g, int y) {
    	int w = scaled(160);
    	int x = (getWidth()-w)/2;
    	int h = s25;
        g.setColor(RacesUI.darkBrown);
        int cnr = min(w/8,h/8);
        Shape rect = new RoundRectangle2D.Float(x,y,w,h,cnr, cnr);
        g.fill(rect);

        scaleButton.setBounds(x,y,w,h);
        if (scaleButton == hoverShape) {
            Stroke prev = g.getStroke();
            g.setStroke(stroke1);
            g.setColor(Color.yellow);
            g.draw(rect);
            g.setStroke(prev);
        }

        g.setFont(narrowFont(20));
        if (scaleButton == hoverShape) 
            g.setColor(Color.yellow);
        else
            g.setColor(SystemPanel.whiteText);

        String text;
        if (options().selectedRaceStatusLog())
        	text = text("SETTINGS_MOD_RACE_STATUS_LOG_YES_UI");
        else
        	text = text("SETTINGS_MOD_RACE_STATUS_LOG_NO_UI");
        int sw = g.getFontMetrics().stringWidth(text);
        int x0 = x+(w-sw)/2;
        drawString(g,text, x0, y+h-s6);
    }
    private void drawValueButton(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(RacesUI.darkBrown);
        int cnr = min(w/8,h/8);
        Shape rect = new RoundRectangle2D.Float(x,y,w,h,cnr, cnr);
        g.fill(rect);

        valueButton.setBounds(x,y,w,h);
        if (valueButton == hoverShape) {
            Stroke prev = g.getStroke();
            g.setStroke(stroke1);
            g.setColor(Color.yellow);
            g.draw(rect);
            g.setStroke(prev);
        }

        g.setFont(narrowFont(20));
        if (valueButton == hoverShape) 
            g.setColor(Color.yellow);
        else
            g.setColor(SystemPanel.whiteText);

        String text = options().raceStatusViewText();
        int sw = g.getFontMetrics().stringWidth(text);
        int x0 = x+(w-sw)/2;
        drawString(g,text, x0, y+h-s6);
    }
    private void drawHistoryButton(Graphics2D g, Empire emp, Rect button, int x, int y, int w, int h) {
        if (galaxy().numberTurns() == 0)
            return;
        g.setColor(RacesUI.darkBrown);
        int cnr = min(w/8,h/8);
        Shape rect = new RoundRectangle2D.Float(x,y,w,h,cnr, cnr);
        g.fill(rect);

        button.setBounds(x,y,w,h);
        if (button == hoverShape) {
            Stroke prev = g.getStroke();
            g.setStroke(stroke1);
            g.setColor(Color.yellow);
            g.draw(rect);
            g.setStroke(prev);
        }

        //g.setFont(narrowFont(20));
        if (button == hoverShape) 
            g.setColor(Color.yellow);
        else
            g.setColor(SystemPanel.whiteText);

        String text = text("RACES_STATUS_HISTORY");
        text = emp.replaceTokens(text, "alien");
        scaledFont(g, text, w-s10, 20, 10);
        int sw = g.getFontMetrics().stringWidth(text);
        int x0 = x+(w-sw)/2;
        drawString(g,text, x0, y+h-s6);
    }
    private void drawRaceIconBase(Graphics2D g, Empire emp, int x, int y, int w, int h) {
        g.setColor(RacesUI.darkBrown);
        Shape rect = new RoundRectangle2D.Float(x,y,w,h,w/8, h/8);
        g.fill(rect);  
    }
    private void drawRaceIcon(Graphics2D g, Empire emp, int x, int y, int w, int h) {
        g.setColor(Color.black);
        Shape rect = new RoundRectangle2D.Float(x,y,w,h,w/8, h/8);
        g.fill(rect);

        BufferedImage backImg = parent.raceIconBackImg();
        g.drawImage(backImg, x,y, s100,s100, null); // modnar: reduce RaceIcon background size

        int x1 = x + w/10;
        int w1 = w * 8/10;
        int y1 = y + h/10;
        int h1 = h * 8/10;

        Image img = player().flagPact();
        int imgH = img.getHeight(null);
        int imgW = img.getWidth(null);
        g.drawImage(img, x1, y1, x1+w1, y1+h1, 0, 0, imgW, imgH, null);
    }
    private void drawOpponentIcon(Graphics2D g, Empire emp, int x, int y, int w, int h) {
        g.setColor(Color.black);
        Shape rect = new RoundRectangle2D.Float(x,y,w,h,w/8, h/8);
        g.fill(rect);
        
        BufferedImage backImg = parent.raceIconBackImg();
        g.drawImage(backImg, x,y, s100,s100, null); // modnar: reduce RaceIcon background size
        
        if (emp == null)
            return;

        int x1 = x + w/10;
        int w1 = w * 8/10;
        int y1 = y + h/10;
        int h1 = h * 8/10;

        Image img = player().viewForEmpire(emp).flag();
        int imgH = img.getHeight(null);
        int imgW = img.getWidth(null);
        g.drawImage(img, x1, y1, x1+w1, y1+h1, 0, 0, imgW, imgH, null);
    }
    private void drawPlayerTitle(Graphics2D g, Empire emp, int x, int y, int w, int h) {
        g.setColor(SystemPanel.orangeText);
        g.setFont(narrowFont(32));
        String title = text("RACES_STATUS_THE_EMPIRE");
        title = player().replaceTokens(title, "alien");
        drawString(g,title, x+s10, y+h-s15);
    }
    private void drawVS(Graphics2D g, Empire emp, int x, int y, int w, int h) {
        g.setColor(SystemPanel.orangeText);
        g.setFont(narrowFont(32));
        String vs = text("RACES_STATUS_VS");
        int sw = g.getFontMetrics().stringWidth(vs);
        int x0 = x+(w-sw)/2;
        drawString(g,vs, x0, y+h-s2);
    }
    private void drawKnownEmpiresTitle(Graphics2D g, Empire emp, int x, int y, int w, int h) {
        g.setColor(SystemPanel.orangeText);
        g.setFont(narrowFont(32));
        String title = text("RACES_STATUS_KNOWN_EMPIRES", player().contacts().size());
        int sw = g.getFontMetrics().stringWidth(title);
        drawString(g,title, x-sw-s10, y-s15);
    }
    private void drawAITitle(Graphics2D g, Empire emp, int x, int y, int w, int h) {
        g.setColor(SystemPanel.orangeText);
        g.setFont(narrowFont(32));
        String title = text("RACES_STATUS_THE_EMPIRE");
        title = emp.replaceTokens(title, "alien");
        int sw = g.getFontMetrics().stringWidth(title);
        drawString(g,title, x-sw-s10, y-s15);
    }
    private void drawAllRankingsLists(Graphics2D g, Empire emp, int x, int y, int w, int h) {
        g.setColor(RacesUI.darkBrown);
        g.fillRect(x, y, w, h);
        int w1 = (w-s20)/3;
        int h1 = h/2;

        int x1 = x+s10;
        int x2 = x1+w1;
        int x3 = x2+w1;
        int y1 = y;
        int y2 = y+h1;

        drawRankingPane(g, x1, y1, w1, h1, EmpireStatus.FLEET);
        drawRankingPane(g, x2, y1, w1, h1, EmpireStatus.POPULATION);
        drawRankingPane(g, x3, y1, w1, h1, EmpireStatus.TECHNOLOGY);
        drawRankingPane(g, x1, y2, w1, h1, EmpireStatus.PLANETS);
        drawRankingPane(g, x2, y2, w1, h1, EmpireStatus.PRODUCTION);
        drawRankingPane(g, x3, y2, w1, h1, EmpireStatus.POWER);
    }
    private void drawRankingPane(Graphics2D g, int x, int y, int w, int h, int num) {
        int numDisplayEmps = bounds(4, galaxy().numActiveEmpires(), 8);
        IGameOptions opts = options();
        g.setFont(font(16));
        int maxSW =  g.getFontMetrics().stringWidth(player().raceName());
        for (Empire emp: player().contactedEmpires()) {
            maxSW = max(maxSW, g.getFontMetrics().stringWidth(emp.raceName()));
        }
		int marginLeft = maxSW+s5;
		int marginRight = s5;
		int marginTop = s35;
		int marginBottom = s10;

        boolean absolute = options().raceStatusViewValue();
        g.setFont(font(20));
        String title = player().status().title(num);
        if (num == EmpireStatus.TECHNOLOGY)
        	if (absolute)
        		title = ("Max " + title);
        	else
        		title = ("Avg " + title);
        int sw = g.getFontMetrics().stringWidth(title);
        int x0 = x+marginLeft+(w-sw-marginLeft)/2;
        int y0 = y+s30;
        drawShadowedString(g, title, 1, x0, y0, SystemPanel.blackText, SystemPanel.whiteText);

		int x1 = x + marginLeft;
		int w1 = w - marginLeft - marginRight;
		int y1 = y + marginTop;
		int listH = h-marginTop - marginBottom;

		// recalculate h1 so bars fit in evenly
		int verticalSpacing = s5;
		int topSpacing = s5;
		int bottomSpacing = s5;
		int leftSpacing = s5;	// left margin
		int rightSpacing = s40;	// right margin, (include scroll bar)
        int barH = (listH-((numDisplayEmps-1)*verticalSpacing)-topSpacing-bottomSpacing)/numDisplayEmps;
        listH = (barH*numDisplayEmps)+((numDisplayEmps-1)*verticalSpacing)+topSpacing+bottomSpacing;
        int yAdj = (barH-s10)/2;

        g.setColor(RacesUI.darkerBrown);
        g.fillRect(x1, y1, w1, listH);
        fullBoxes[num].setBounds(x1-s5-maxSW, y1, w1+s5+maxSW, listH);
        dataBoxes[num].setBounds(x1, y1, w1, listH);
		int maxNameY = y1 + listH + barH;
        if (dataBoxes[num] == hoverShape) {
            Stroke prev = g.getStroke();
            g.setStroke(stroke2);
            g.setColor(Color.yellow);
            g.draw(dataBoxes[num]);
            g.setStroke(prev);
        }
        boolean maxTech = absolute && (num == EmpireStatus.TECHNOLOGY);
        if (maxTech)
            getEmpireListing(EmpireStatus.TECHNOLOGY_MAX);
        else
        	getEmpireListing(num);

        float maxValue	= vals.get(0).value;
        float minValue	= maxValue;
        float sumValues	= 0;
        for (RaceValue rv: vals) {
            if (rv.value > 0) {
            	sumValues += rv.value;
            	minValue = Math.min(minValue, rv.value);
            }
        }
        minValue /= 2;
        float logScale = logScale(maxValue/minValue);

        // BR: the with will depends on the scroll bar activation 
        int scrollW   = s12; // scroll bar width
        int rows	  = vals.size();
        dataYMax[num] = max(0, s21+(barH*rows) - listH);
        boolean noScrollBar = (dataYMax[num] == 0);

		int x2 = x1 + leftSpacing;
		int y2 = y1 + topSpacing - dataY[num];
		int w2 = w1 - leftSpacing - rightSpacing; // 100% bar width
		int wbt = (w2 + rightSpacing - leftSpacing); // with available for display bar and text
		wbt = noScrollBar? wbt : wbt-scrollW;

        g.setFont(font(16));
        g.setClip(fullBoxes[num]);

        float playerVal	= player().status().lastViewValue(player(), num);
        float norm = 1;
        if (opts.raceStatusViewPlayer())
        	norm = 100/playerVal;
        else if (opts.raceStatusViewTotal())
        	norm = 100/sumValues;

        for (RaceValue rv: vals) {
            if (rv.value < 0) {
                g.setColor(RacesUI.darkBrown);
                String s = text("RACES_STATUS_NO_DATA");
                drawString(g,s, x2, y2+barH-yAdj);
            }
            else if (maxValue > 0) {
                g.setColor(rv.emp.color());
                int barW;
                if (opts.selectedRaceStatusLog())
                	barW = (int) (w2 * logScale(rv.value/minValue) / logScale);
                else
                	barW = (int) (w2 * (rv.value / maxValue));
                g.fillRect(x2, y2, barW, barH);
                g.setColor(RacesUI.brown);
                String age = "";
                if (rv.age > 0)
                	age = "("+str(rv.age)+")";

                float pct = rv.value * norm;
                String val = "";
                if (absolute)
                	if (maxTech)
                		val= df1.format(pct);
                	else
                		val = str(Math.round(pct));
                else if (pct >= 10)
                	val = str(Math.round(pct));
                else
                	val= df1.format(pct);

                int swa = g.getFontMetrics().stringWidth(age);
                int swv = g.getFontMetrics().stringWidth(val);
                if (swv+barW+s5 > wbt) { // Value to the left
                	if(s5+swv+s5 > barW) { // Value full left
                		drawBorderedString(g, val, x2+s5, y2+barH-yAdj, Color.black, RacesUI.brown);
                		drawString(g, age, x2+barW+s5+swv+s5, y2+barH-yAdj);
                	}
                	else {
                		drawBorderedString(g, val, x2+barW-s5-swv, y2+barH-yAdj, Color.black, RacesUI.brown);
                		drawString(g, age, x2+barW+s5, y2+barH-yAdj);
                	}
                }
                else { // Value to the right
                	if (s5+swa+s5 > barW) { // age to the right
                		drawString(g, val, x2+barW+s5, y2+barH-yAdj);
                		drawString(g, age, x2+barW+s5+swv+s5, y2+barH-yAdj);
                	}
                	else { // age to the left
                		drawBorderedString(g, age, x2+barW-s5-swa, y2+barH-yAdj, Color.black, RacesUI.brown);
                		drawString(g, val, x2+barW+s5, y2+barH-yAdj);
                	}
                }
            }
            String name = rv.emp.raceName();
            int sw2 = g.getFontMetrics().stringWidth(name);
            g.setColor(SystemPanel.blackText);
			int yName = y2 + barH - yAdj;
			// BR: I don't know why it's not clipped as it should be!!!
			if (yName < maxNameY)
				drawString(g, name, x1-s5-sw2, yName);
            y2 += (barH+verticalSpacing);
        }

		dataYMax[num] = max(0, s21+((barH+verticalSpacing)*rows) - listH);
        if (dataYMax[num] == 0) 
            dataScrollers[num].setBounds(0,0,0,0);
        else {
            g.setColor(RacesUI.scrollBarC);
            // int scrollW = s12;
            int scrollH = (int) ((float)listH*listH/(listH+dataYMax[num]));
            int scrollX = x1+w1-scrollW-s2;
            int scrollY =(int) (y1+ (float)listH*dataY[num]/(dataYMax[num]+listH));
            g.fillRoundRect(scrollX, scrollY, scrollW, scrollH, s4, s4);
            dataScrollers[num].setBounds(scrollX, scrollY, scrollW, scrollH);
            if (hoverShape == dataScrollers[num]) {
                Stroke prev = g.getStroke();
                g.setColor(Color.yellow);
                g.setStroke(stroke2);
                g.drawRoundRect(scrollX, scrollY, scrollW, scrollH, s4, s4);
                g.setStroke(prev);
            }
        }
        g.setClip(null);
    }
    private void getEmpireListing(int cat) {
        vals.clear();
        for (Empire emp: player().contactedEmpires()) {
            float val = emp.status().lastViewValue(player(), cat);
            int age=  emp.status().age(player());
            vals.add(new RaceValue(emp, val, age));
        }
        float val = player().status().lastViewValue(player(), cat);
        vals.add(new RaceValue(player(), val, 0));
        Collections.sort(vals);
    }
    private void drawVsRankingsLists(Graphics2D g, Empire emp, int x, int y, int w, int h) {
        g.setColor(RacesUI.darkBrown);
        g.fillRect(x, y, w, h);
        int w1 = (w-s20)/3;
        int h1 = h/2;

        int x1 = x+s10;
        int x2 = x1+w1;
        int x3 = x2+w1;
        int y1 = y;
        int y2 = y+h1;

        drawHistoryPane(g, x1, y1, w1, h1, EmpireStatus.FLEET);
        drawHistoryPane(g, x2, y1, w1, h1, EmpireStatus.POPULATION);
        drawHistoryPane(g, x3, y1, w1, h1, EmpireStatus.TECHNOLOGY);
        drawHistoryPane(g, x1, y2, w1, h1, EmpireStatus.PLANETS);
        drawHistoryPane(g, x2, y2, w1, h1, EmpireStatus.PRODUCTION);
        drawHistoryPane(g, x3, y2, w1, h1, EmpireStatus.POWER);
    }
    private float logScale(float src) {
		if (src<0)
			return src;
		else if (src==0)
			return 0f;
		else
			return (float) Math.log10(src);
    }
    private float[] scaleVal(float[] src) {
        if (options().selectedRaceStatusLog()) {
        	float[] lg = new float[src.length];
        	for (int i=0; i<src.length; i++)
       			lg[i] = logScale(src[i]);
        	return lg;
        }
        return src;
    }
    private void drawHistoryPane(Graphics2D g, int x, int y, int w, int h, int cat) {
        int mgnL = s20;
        int mgnR = s20;
        int mgnT = s50;
        int mgnB = s10;

        g.setFont(font(20));
        String title = player().status().title(cat);
        int sw = g.getFontMetrics().stringWidth(title);
        int x0 = x+mgnL+(w-sw-mgnL)/2;
        int y0 = y+s35;
        drawShadowedString(g, title, 1, x0, y0, SystemPanel.blackText, SystemPanel.whiteText);

        int x1 = x+mgnL;
        int w1 = w-mgnL-mgnR;
        int y1 = y+mgnT;
        int h1 = h-mgnT-mgnB;

        // recalculate h1 so bars fit in evenly
        int tSpacing = s20;
        int bSpacing = s10;
        int lSpacing = s5;
        int rSpacing = s25;

        g.setColor(RacesUI.darkerBrown);
        g.fillRect(x1, y1, w1, h1);

        boolean dualView = cat == EmpireStatus.TECHNOLOGY;
        float[] playerVals, empireVals, playerHigh, empireHigh;
        playerVals = scaleVal(player().status().values(cat));
        empireVals = scaleVal(parent.selectedEmpire().status().values(cat));
        if (dualView) {
            playerHigh = scaleVal(player().status().values(EmpireStatus.TECHNOLOGY_MAX));
            empireHigh = scaleVal(parent.selectedEmpire().status().values(EmpireStatus.TECHNOLOGY_MAX));        	
        }
        else {
            playerHigh = playerVals;
            empireHigh = empireVals;
        }

        int totalTurns = galaxy().numberTurns();
        int empireTurns = parent.selectedEmpire().status().lastViewTurn(player());
        int displayW = w1-rSpacing-lSpacing;
        int displayH = h1-tSpacing-bSpacing;

        // find maximum Y value to display
        float maxYValue = 0;
        float minYValue = Float.MAX_VALUE;
        for (int i=1;i<=totalTurns;i++) {
        	maxYValue = Math.max(playerHigh[i], maxYValue);
        	minYValue = Math.min(playerVals[i], minYValue);
        }
        for (int i=1;i<=empireTurns;i++) {
        	maxYValue = Math.max(empireHigh[i], maxYValue);
        	if (empireVals[i] > 0)
        		minYValue = Math.min(empireVals[i], minYValue);
        }
        if (options().selectedRaceStatusLog()) {
        	if (dualView) {
                for (int i=1;i<=totalTurns;i++) {
                	playerVals[i] -= minYValue;
                	playerHigh[i] -= minYValue;
                }
                for (int i=1;i<=empireTurns;i++) {
                	empireVals[i] -= minYValue;
                	empireHigh[i] -= minYValue;
                }
        	}
        	else {
                for (int i=1;i<=totalTurns;i++)
                	playerVals[i] -= minYValue;
                for (int i=1;i<=empireTurns;i++)
                	empireVals[i] -= minYValue;
        	}
        	maxYValue -= minYValue;
        }

        int startX = x1+lSpacing;    // x-location for pt values in turn 0
        int startY = y1+h1-bSpacing; // y-location for pt values = 0
        int maxPtSpacing = s20;
        Stroke prevStroke = g.getStroke();
        g.setFont(font(18));

        if (dualView) {
        	g.setStroke(dotedLineStroke);
            // DRAW SELECTED EMPIRE
            g.setColor(parent.selectedEmpire().color());
            int prevX = startX;
            int prevY = -1;
            for (int i=1;i<=empireTurns;i++) {
                int ptX = startX+(displayW*i/totalTurns);
                if ((ptX - prevX) > maxPtSpacing)
                    ptX = prevX + maxPtSpacing;
                int ptY = maxYValue == 0 ? 0 : startY-(int)(displayH*empireHigh[i]/maxYValue);
                if (prevY >= 0)
                    g.drawLine(prevX, prevY, ptX, ptY);
                prevX = ptX;
                prevY = ptY;
            }

            // DRAW PLAYER
            g.setColor(player().color());
            prevX = startX;
            prevY = -1;
            for (int i=1;i<=totalTurns;i++) {
                int ptX = startX+(displayW*i/totalTurns);
                if ((ptX - prevX) > maxPtSpacing)
                    ptX = prevX + maxPtSpacing;
                int ptY = maxYValue == 0 ? 0 : startY-(int)(displayH*playerHigh[i]/maxYValue);
                if (prevY >= 0)
                    g.drawLine(prevX, prevY, ptX, ptY);
                prevX = ptX;
                prevY = ptY;
            }
        }

        g.setStroke(stroke2);
        // DRAW SELECTED EMPIRE
        g.setColor(parent.selectedEmpire().color());
        String name = parent.selectedEmpire().raceName();
        int nameW = g.getFontMetrics().stringWidth(name);
        drawString(g,name, startX+displayW+rSpacing-s5-nameW, startY+s5);
        int prevX = startX;
        int prevY = -1;
        for (int i=1;i<=empireTurns;i++) {
            int ptX = startX+(displayW*i/totalTurns);
            if ((ptX - prevX) > maxPtSpacing)
                ptX = prevX + maxPtSpacing;
            int ptY = maxYValue == 0 ? 0 : startY-(int)(displayH*empireVals[i]/maxYValue);
            if (prevY >= 0)
                g.drawLine(prevX, prevY, ptX, ptY);
            prevX = ptX;
            prevY = ptY;
        }

        // DRAW PLAYER
        g.setColor(player().color());
        drawString(g,player().raceName(), startX, startY-displayH);
        prevX = startX;
        prevY = -1;
        for (int i=1;i<=totalTurns;i++) {
            int ptX = startX+(displayW*i/totalTurns);
            if ((ptX - prevX) > maxPtSpacing)
                ptX = prevX + maxPtSpacing;
            int ptY = maxYValue == 0 ? 0 : startY-(int)(displayH*playerVals[i]/maxYValue);
            if (prevY >= 0)
                g.drawLine(prevX, prevY, ptX, ptY);
            prevX = ptX;
            prevY = ptY;
        }

       if (empireTurns < 0) {
            g.setFont(narrowFont(24));
            g.setColor(RacesUI.darkBrown);
            String s = text("RACES_STATUS_NO_DATA");
            int sw1 = g.getFontMetrics().stringWidth(s);
            drawString(g,s, x1+(w1-sw1)/2, y1+(h1+s24)/2);            
        }
        // DRAW SELECTED EMPIRE AGAIN, with dashed lines
        g.setStroke(dashedLineStroke);
        g.setColor(parent.selectedEmpire().color());
        prevX = startX;
        prevY = -1;
        for (int i=0;i<=empireTurns;i++) {
            int ptX = startX+(displayW*i/totalTurns);
            if ((ptX - prevX) > maxPtSpacing)
                ptX = prevX + maxPtSpacing;
            int ptY = maxYValue == 0 ? 0 : startY-(int)(displayH*empireVals[i]/maxYValue);
            if (prevY >= 0)
                g.drawLine(prevX, prevY, ptX, ptY);
            prevX = ptX;
            prevY = ptY;
        }
        g.setStroke(prevStroke);
    }
    private void showHistory(Empire e) {
        RotPUI.instance().selectHistoryPanel(e.id, false);
    }
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int count = e.getUnitsToScroll();
        for (int i=0;i<dataBoxes.length;i++) {
            if ((hoverShape == dataBoxes[i])
            || (hoverShape == dataScrollers[i])) {
                int prevY = dataY[i];
                if (count < 0)
                    dataY[i] = max(0,dataY[i]-s10);
                else 
                    dataY[i] = min(dataYMax[i],dataY[i]+s10);
                if (dataY[i] != prevY)
                    repaint(fullBoxes[i]);
                return;
            }
        }
    }
    @Override
    public void mouseDragged(MouseEvent e) { 
        int x = e.getX();
        int y = e.getY();
        int dY = y-dragY;
        dragY = y;
        for (int i=0;i<dataBoxes.length;i++) {
            if (dataScrollers[i] == hoverShape) {
                if ((y >= dataBoxes[i].y) || (y <= (dataBoxes[i].y+dataBoxes[i].height))) { 
                    int h = (int) dataBoxes[i].getHeight();
                    int dListY = (int)((float)dY*(h+dataYMax[i])/h);
                    if (dY < 0)
                        dataY[i] = max(0,dataY[i]+dListY);
                    else 
                        dataY[i] = min(dataYMax[i],dataY[i]+dListY);
                }
                repaint(fullBoxes[i]);
                return;
            }
            else if (dataBoxes[i] == hoverShape) {
                if (dataBoxes[i].contains(x,y)) { 
                    int h = (int) dataBoxes[i].getHeight();
                    int dListY = (int)(-(float)dY*(h+dataYMax[i])/h);
                    if (dListY < 0)
                        dataY[i] = max(0,dataY[i]+dListY);
                    else 
                        dataY[i] = min(dataYMax[i],dataY[i]+dListY);
                }
                repaint(fullBoxes[i]);
                return;
            }
        }
    }
    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        Shape prevHover = hoverShape;
        hoverShape = null;
        if (playerHistoryButton.contains(x,y))
            hoverShape = playerHistoryButton;
        else if (aiHistoryButton.contains(x,y))
            hoverShape = aiHistoryButton;
        else if (scaleButton.contains(x,y))
            hoverShape = scaleButton;
        else if (valueButton.contains(x,y))
            hoverShape = valueButton;
        else {
            for (int i=0;i<dataBoxes.length;i++) {
                if (dataScrollers[i].contains(x,y)) 
                    hoverShape = dataScrollers[i];
                else if (dataBoxes[i].contains(x,y)) 
                    hoverShape = dataBoxes[i];
            }
        }
        if (hoverShape != prevHover) 
            repaint();     
    }
    @Override
    public void mouseClicked(MouseEvent mouseEvent) { }
    @Override
    public void mousePressed(MouseEvent e) { 
        dragY = e.getY();
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() > 3)
            return;
        dragY = 0;
        if (hoverShape == aiHistoryButton)
            showHistory(parent.selectedEmpire());
        else if (hoverShape == playerHistoryButton)
            showHistory(player());
        else if (hoverShape == scaleButton) {
            options().toggleRaceStatusLog();
            repaint();
        }
        else if (hoverShape == valueButton) {
            options().toggleRaceStatusView();
            repaint();
        }
    }
    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}
    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        if (hoverShape != null) {
            hoverShape = null;
            repaint();
        }
    }
    public static class RaceValue implements Comparable<RaceValue> {
        public float value = 0;
        public int age = 0;
        public Empire emp;
        public RaceValue(Empire e, float v, int a) {
        	value = v;
            age  = a;
            emp = e;
        }
        @Override
        public int compareTo(RaceValue rv) {
            return value > rv.value ? -1 : value < rv.value ? +1 : 0;
        }
    }
}