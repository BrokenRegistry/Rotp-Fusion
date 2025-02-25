package rotp.ui.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.metal.MetalButtonUI;

import rotp.Rotp;
import rotp.model.empires.Empire;
import rotp.model.galaxy.StarSystem;
import rotp.model.game.GameSession;
import rotp.model.game.GovernorOptions;
import rotp.ui.BasePanel;
import rotp.ui.game.GameUI;
import rotp.ui.options.AllSubUI;
import rotp.ui.options.ISubUiKeys;
import rotp.ui.util.IParam;
import rotp.ui.util.ParamSubUI;
import rotp.ui.util.swing.RotpJSpinner;
import rotp.ui.util.swing.RotpJSpinnerButton;
import rotp.util.FontManager;
/**
 * Produced using Netbeans Swing GUI builder.
 */
public class GovernorOptionsPanel extends BasePanel{
	private static final long serialVersionUID = 1L;
	private static final String HEADER  = "GOVERNOR_";
	private static final String DESC    = IParam.LABEL_DESCRIPTION;
	private static final String HELP    = IParam.LABEL_HELP;
	private static final String LABEL   = IParam.LABEL_GOV_LABEL;
	private static final String TOOLTIP = "_TT";
	
	private static final float	valueFontSize		= 14f;
	private static final float	baseFontSize		= 14f;
	private static final float	labelFontSize		= 14f;
	private static final float	buttonFontSize		= 18f;
	private static final float	panelTitleFontSize	= 20f;
	private static final float	baseIconSize		= 16f;
	private static final float	arrowWidthFactor	= 0.8f;
	private static final float	buttonCornerFactor	= 5f/18f;
	private static final int	buttonTopInset		= 6;
	private static final int	buttonSideInset		= 10;
	private static final int	animationStep		= 100; // ms
	private static final int	ANIMATION_STOPPED	= 0;
	private static final int	ANIMATION_ONGOING	= 1;
	private static final int	ANIMATION_CANCELED	= 2;
	private static final int	ANIMATION_RESET		= 3;

	private  Font	valueFont, baseFont, labelFont, buttonFont, panelTitleFont, tooltipFont;
	private  Color	frameBgColor, panelBgColor, textBgColor, valueBgColor, tooltipBgColor;
	private  Color	textColor, valueTextColor, panelTitleColor, tooltipTxtColor;
	private  Color	buttonColor, buttonTextColor, iconBgColor;
	private  Color	hiddenColor, disabledColor, hoverColor, borderColor;
	private  int	iconSize, arrowHeight, buttonCorner;
	private  Icon	iconCheckRadio		= new ScalableCheckBoxAndRadioButtonIcon();
	private  GovButtonUI rotpButtonUI	= new GovButtonUI();
//	private	Inset	iconInset			= new Insets(topInset, 2, 0, 2);
	
	// Display format variable, needed for reset purpose
	//
	private  Boolean	isNewFormat, isCustomSize;
	private  Integer	sizeFactorPct,	brightnessFactorPct;
	private  int		animationLive	= 0;
	private  boolean	updateOngoing	= false;
	private  boolean	horlogeOngoing	= false; // Another crash prevention

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(15); // no
    private ScheduledFuture<?> anim;
	private Runnable timedRefresh = new TimeRefreshRunable();
	private static GovernorFrame frame;
	// ========== Public Method and Overrider ==========
	//
	private void optionUpdate() {
		if (!updateOngoing) {
			loadDisplayValues();
			loadValues();
			protectedReset();
		}
	} 
	void applyStyle() { protectedUpdatePanel(); }
	void reOpen() {
		if (executor.getQueue().size() == 0) {
			System.out.println("executor Timer Restarted ");
			horlogeOngoing = false;
			anim = executor.scheduleAtFixedRate(timedRefresh, animationStep, animationStep, TimeUnit.MILLISECONDS);
		}
		protectedUpdateColor(); // In the case the color set has been changed.
	}
	public static void close() {
		if (frame == null)
			return;
		frame.setVisible(false); 
	}
	
	// ========== Protected initializers ==========
	// Loading and saving values occurring during these call
	// may trigger new-initializations 
	// The first call win!
	//
	private void protectedReset()		{
		if (!updateOngoing) {
			updateOngoing = true;
			if (animationLive != ANIMATION_STOPPED) {
				animationLive = ANIMATION_RESET;
			} 
			else {
				resetPanel();
			}
		}
	}
	private void protectedInitPanel()	{
		if (!updateOngoing) {
			updateOngoing = true;
			initPanel();
			updateOngoing = false;
		}
	}
	private void protectedUpdateColor() {
		if (!updateOngoing) {
			updateOngoing = true;
			initNewColors(true);
			updatePanel(frame, isNewFormat(), false, 0);
			updateOngoing = false;
			setRaceImg(); 	// Pack and set icon
		}
	}
	private void protectedUpdateSize()	{ protectedReset(); }
	private void protectedUpdatePanel() {
		if (!updateOngoing) {
			updateOngoing = true;
			initNewColors(true);
			initNewFonts();
			initTooltips();
			updatePanel(frame, isNewFormat(), false, 0);
			updateOngoing = false;
			setRaceImg(); 	// Pack and set icon
		}
	}

	// ========== Constructor and initializers ==========
	//
	GovernorOptionsPanel(GovernorFrame f) {
		frame = f;
		govOptions().clearReset();
		frame.setVisible(true);
		protectedInitPanel();
	}
	private void initNewColors(boolean local) {
		if (isNewFormat()) {
			float brightness;
			if (local)
				brightness = (Integer)brightnessPct.getValue() /100f;
			else
				brightness = govOptions().getBrightnessPct()/100f;

			frameBgColor	= multColor(GameUI.setupFrame(),			0.40f * brightness);
			panelBgColor	= multColor(GameUI.paneBackgroundColor(),	0.60f * brightness);
			valueBgColor	= multColor(GameUI.paneBackgroundColor(),	0.85f * brightness);
			textBgColor		= panelBgColor;
			tooltipBgColor	= multColor(GameUI.paneBackgroundColor(),	0.95f * brightness);
			
			buttonColor		= panelBgColor;
			borderColor		= multColor(valueBgColor, 1.2f);
			hiddenColor		= multColor(frameBgColor, 0.8f);
			disabledColor	= multColor(frameBgColor, 1.2f);
			hoverColor		= Color.yellow;
			
			buttonTextColor	= multColor(valueBgColor, 1.2f);
	
			textColor		= SystemPanel.blackText;
			valueTextColor	= SystemPanel.blackText;
			panelTitleColor	= SystemPanel.whiteText;
			iconBgColor		= valueBgColor;
			tooltipTxtColor	= SystemPanel.blackText;
		}
		else {
			tooltipBgColor	= new Color(214,217,223);
			tooltipTxtColor	= SystemPanel.blackText;
		}
	}
	private void initNewFonts() {
		if (isNewFormat()) {
			iconSize	 	= (int) scaledSize(baseIconSize);
			buttonCorner 	= (int) scaledSize(buttonFontSize * buttonCornerFactor);
			arrowHeight 	= (int) scaledSize((valueFontSize+buttonTopInset) * arrowWidthFactor);
			valueFont		= FontManager.getNarrowFont(scaledSize(valueFontSize));
			baseFont		= FontManager.getNarrowFont(scaledSize(baseFontSize));
			labelFont		= FontManager.getNarrowFont(scaledSize(labelFontSize));
			buttonFont		= FontManager.getNarrowFont(scaledSize(buttonFontSize));
			panelTitleFont	= FontManager.getNarrowFont(scaledSize(panelTitleFontSize));
			tooltipFont		= FontManager.getNarrowFont(scaledSize(baseFontSize));
		}
		else {
			tooltipFont		= new Font("SansSerif", Font.PLAIN, 12);
		}
	}
	private void initPanel() {
		initNewFonts();
		initNewColors(false);
		initComponents();	// Load the form
		loadValues();		// Load User's values
		initTooltips();
		updatePanel(frame, isNewFormat(), false, 0); // Apply the new formating
		setRaceImg();		// Pack and set icon
	}
	private void resetPanel() {
		updateOngoing = true;
		govOptions().clearReset();

		boolean visible = frame.isVisible();
		frame.setVisible(false);
		//Remove the components before reloading
		Component[] componentList = getComponents();
		for(Component c : componentList){
			remove(c);
		}
		frame.revalidate();
		initPanel();
		frame.revalidate();
		updateOngoing = false;
		frame.setVisible(true);
		frame.setLocation(govOptions().getPosition());
		frame.setVisible(visible);
		startAnimation();
	}
	private void loadDisplayValues() {
		GovernorOptions opt = govOptions();
		isNewFormat			= !opt.isOriginalPanel();
		isCustomSize		= opt.isCustomSize();
		sizeFactorPct		= opt.getSizeFactorPct();
		brightnessFactorPct	= opt.getBrightnessPct();
	}
	private void initTooltips()	{
		UIManager.put("ToolTip.font", tooltipFont);
		UIManager.put("ToolTip.background", tooltipBgColor);
		UIManager.put("ToolTip.foreground", tooltipTxtColor);
		ToolTipManager.sharedInstance().setDismissDelay(10000);
	}
	// ========== Local tools ==========
	//
	private String getLabel(String name)		{ return text(HEADER + name + LABEL); }
	private String getHelp(String name)			{ return text(HEADER + name + HELP); }
	private String getDescription(String name)	{ return text(HEADER + name + DESC); }
	private String getBaseToolTip(String name)	{ return text(HEADER + name + TOOLTIP); }
	private String getToolTip(String name)		{
		String toolTip = getBaseToolTip(name);
		if (toolTip.startsWith(HEADER)) {
			toolTip = getHelp(name);
			if (toolTip.startsWith(HEADER)) {
				toolTip = getDescription(name);
				if (toolTip.startsWith(HEADER))
					return name;
			}
		}
		return "<html>" + toolTip + "</html>";
	}
	private boolean	isAutoApply()				{ return (govOptions().isAutoApply()); }
	private boolean	isAnimatedImage()			{ return (govOptions().isAnimatedImage()); }
	private boolean	isCustomSize()				{
		if (isCustomSize == null)
			isCustomSize = govOptions().isCustomSize();
		return isCustomSize;
	}
	private boolean	isNewFormat()				{
		if (isNewFormat == null)
			isNewFormat = !govOptions().isOriginalPanel();
		return isNewFormat;
	}
	private int		getSizeFactor()				{
		if (sizeFactorPct == null)
			sizeFactorPct = govOptions().getSizeFactorPct();
		return sizeFactorPct;
	}
	private int		getBrightnessPct()			{
		if (brightnessFactorPct == null)
			brightnessFactorPct = govOptions().getBrightnessPct();
		return brightnessFactorPct;
	}
	private int		scaledSize(int size)		{ return (int) (size * getFinalSizefactor()); }
	private float 	scaledSize(float size)		{ return size * getFinalSizefactor(); }
	private float 	getFinalSizefactor()		{
		if (isCustomSize())
			return Rotp.resizeAmt() * getSizeFactor()/100f;
		else
			return Rotp.resizeAmt();
	}
	private void	setCustomSize(boolean val)	{
		isCustomSize = val;
		if (isAutoApply())
			govOptions().setIsCustomSize(isCustomSize);
	}
	private void	setBrightnessPct(int val)	{
		brightnessFactorPct = val;
		if (isAutoApply())
			govOptions().setBrightnessPct(brightnessFactorPct);
	}
	private void	setSizeFactorPct(int pct)	{
		sizeFactorPct = pct;
		if (isAutoApply())
			govOptions().setSizeFactorPct(pct);
	}
	private void	setNewFormat(boolean val)	{
		isNewFormat = val;
		if (isAutoApply())
			govOptions().setIsOriginalPanel(!val);
	}

	// ========== Image display and animation ==========
	//
	private void stopAnimation() {
		if (animationLive == ANIMATION_ONGOING)
			animationLive = ANIMATION_CANCELED;
	}
	private void startAnimation() {
		if (anim == null)
			anim = executor.scheduleAtFixedRate(timedRefresh, animationStep, animationStep, TimeUnit.MILLISECONDS);
		if (updateOngoing)
			return;
		if (isAnimatedImage() && animationLive == ANIMATION_STOPPED) {
			animationLive = ANIMATION_ONGOING;
		}
	}
	private void govAnimate() {
		if (animationLive == ANIMATION_RESET) {
			animationLive = ANIMATION_STOPPED;
			resetPanel(); // called by protected
			return;
		}
		if (isAnimatedImage() && animationLive == ANIMATION_ONGOING) {
			if (frame.isVisible()) {
				updateRaceImage();
				return;
			}
		}
		else {
			animationLive = ANIMATION_STOPPED;
		}
	}
	private void updateRaceImage() {
		BufferedImage raceImg = GameSession.instance().galaxy().player().race().setupImage();
		int srcWidth	= raceImg.getWidth();
		int srcHeight	= raceImg.getHeight();
		int margin		= scaledSize(0);
		int destWidth	= raceImage.getWidth()  - margin;
		int destHeight	= raceImage.getHeight() - margin;
		// Get New Size
		float fW = (float)srcWidth/destWidth;
		float fH = (float)srcHeight/destHeight;
		destWidth *= fW/fH;
		if (destWidth*destHeight == 0)
			return;
		raceImage.setSize(destWidth, destHeight);
		// Flip the Image
		BufferedImage flipped = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics g = flipped.getGraphics();
		g.drawImage(raceImg, 0, 0, destWidth, destHeight, srcWidth, 0, 0, srcHeight, null);
		raceImage.setIcon(new ImageIcon(flipped));
		repaint(raceImage.getBounds());
	}
	private void setRaceImg() {
		frame.pack(); // Should set a width!
		if (raceImage.getWidth() == 0)
			return;
		raceImage.setOpaque(false);
		updateRaceImage();
		frame.pack();
		startAnimation();
	}
	
	// ========== Update Panel Tools ==========
	//
	private void setBasicArrowButton	(Component c, boolean newFormat, boolean debug) {
		BasicArrowButton button = (BasicArrowButton) c;
		if (newFormat) {
			button.setBackground(frameBgColor);
		}
	}
	private void setRotpSpinnerButton	(Component c, boolean newFormat, boolean debug) {
		RotpJSpinnerButton button = (RotpJSpinnerButton) c;
		button.setFocusPainted(false);
		String name = button.getName();
		//System.out.println("RotpJSpinnerButton name: " + name );
		if (name != null) {
			//button.setText(getLabel(name));
			button.setToolTipText(getToolTip(name));
		}
		if (newFormat) {
			button.setBackground(null);
			button.setForeground(buttonTextColor);
			int topInset  = scaledSize(buttonTopInset);
			int sideInset = scaledSize(buttonSideInset);
			button.setMargin(new Insets(topInset, sideInset, -5, sideInset));
			button.setBorderPainted(false);
			button.setFocusPainted(false);
		}
	}
	private void setJButton				(Component c, boolean newFormat, boolean debug) {
		JButton button = (JButton) c;
		button.setFocusPainted(false);
		String name = button.getName();
		//System.out.println("button name: " + name );
		if (name != null) {
			button.setText(getLabel(name));
			button.setToolTipText(getToolTip(name));
		}
		if (newFormat) {
			button.setUI(rotpButtonUI);
			button.setBackground(null);
			button.setForeground(buttonTextColor);
			int topInset  = scaledSize(buttonTopInset);
			int sideInset = scaledSize(buttonSideInset);
			button.setFont(buttonFont);
			button.setMargin(new Insets(topInset, sideInset, 0, sideInset));
			button.setIcon(new GovButtonIcon());
			button.setOpaque(true);
			button.setContentAreaFilled(false);
			button.setBorderPainted(false);
			button.setFocusPainted(false);
		}
	}
	private void setJCheckBox			(Component c, boolean newFormat, boolean debug) {
		JCheckBox box = (JCheckBox) c;
		box.setFocusPainted(false);
		String name = box.getName();
		//System.out.println("box name: " + name );
		if (name != null) {
			box.setText(getLabel(name));
			box.setToolTipText(getToolTip(name));
		}
		if (newFormat) {
			box.setBackground(textBgColor);
			box.setForeground(textColor);
			box.setFont(baseFont);
			int topInset  = scaledSize(buttonTopInset);
			box.setMargin(new Insets(topInset, 2, 0, 2));
			box.setIcon(iconCheckRadio);
		}
	}
	private void setJRadioButton		(Component c, boolean newFormat, boolean debug) {
		JRadioButton button = (JRadioButton) c;
		button.setFocusPainted(false);
		String name = button.getName();
		//System.out.println("button name: " + name );
		if (name != null) {
			button.setText(getLabel(name));
			button.setToolTipText(getToolTip(name));
		}
		if (newFormat) {
			button.setBackground(textBgColor);
			button.setForeground(textColor);
			button.setFont(baseFont);
			int topInset  = scaledSize(buttonTopInset);
			button.setMargin(new Insets(topInset, 2, 0, 2));
			button.setIcon(iconCheckRadio);
		}
	}
	private void setJToggleButton		(Component c, boolean newFormat, boolean debug) {
		JToggleButton button = (JToggleButton) c;
		String name = button.getName();
		//System.out.println("button name: " + name );
		if (name != null) {
			button.setToolTipText(getToolTip(name));
		}
		
	}
	private void setJSpinner			(Component c, boolean newFormat, boolean debug) {
		GovernorJSpinner spinner = (GovernorJSpinner) c;
		String name = spinner.getName();
		//System.out.println("GovernorJSpinner name: " + name );
		if (name != null) {
			//spinner.setToolTipText(getToolTip(name));
		}
		if (newFormat) {
			spinner.getLayout();
			spinner.setBackground(valueBgColor);
			spinner.setForeground(textColor);
			spinner.setFont(valueFont);
			spinner.setBorder(null);
			spinner.centerText();
		}	   	
	}
	private void setJLabel				(Component c, boolean newFormat, boolean debug) {
		JLabel label = (JLabel) c;
		String name = label.getName();
		//System.out.println("Label name: " + name );
		if (name != null) {
			label.setText(getLabel(name));
			label.setToolTipText(getToolTip(name));
		}
		if (newFormat) {
			label.setBackground(textBgColor);
			label.setForeground(textColor);
			label.setFont(labelFont);
		}
	}
	private void setJFormattedTextField	(Component c, boolean newFormat, boolean debug) {
		JFormattedTextField txt = (JFormattedTextField) c;
		String name = txt.getName();
		//System.out.println("Label name: " + name );
		if (name != null) {
			//txt.setText(getLabel(name));
			//txt.setToolTipText(getToolTip(name));
		}
		if (newFormat) {
			txt.setBackground(valueBgColor);
			txt.setForeground(valueTextColor);
			txt.setFont(valueFont);
			
		}
	}
	private void setNumberEditor		(Component c, boolean newFormat, boolean debug) {
		NumberEditor num = (NumberEditor) c;
		String name = num.getName();
		// System.out.println("NumberEditor name: " + name );
		if (name != null) {
			num.setToolTipText(getToolTip(name));
		}
		if (newFormat) {
			num.setBackground(valueBgColor);
			num.setForeground(valueTextColor);
			int topInset  = scaledSize(6);
			int sideInset = scaledSize(2);
			Border border = BorderFactory.createEmptyBorder(topInset, sideInset, 0, sideInset);
			num.setBorder(border);
		}
	}
	private void setJPanel				(Component c, boolean newFormat, boolean debug) {
		JPanel pane = (JPanel) c;
		Border brdr = pane.getBorder();
		String name = pane.getName();
		//System.out.println("Label name: " + name );
		if (name != null && brdr != null && brdr instanceof TitledBorder) {
			pane.setToolTipText(getToolTip(name));
			TitledBorder border = (TitledBorder) brdr;
			border.setTitle(getLabel(name));
		}
		if (newFormat) {
			pane.setBackground(panelBgColor);
			pane.setFont(baseFont);
			if (brdr != null && brdr instanceof TitledBorder) {
				TitledBorder border = (TitledBorder) brdr;
				border.setTitleColor(panelTitleColor);
				border.setTitleFont(panelTitleFont);
			}
		}
	}
	private void setJLayeredPane		(Component c, boolean newFormat, boolean debug) { }
	private void setJRootPane			(Component c, boolean newFormat, boolean debug) { }

	private	void updatePanel(Container parent, boolean newFormat, boolean debug, int k) {
		autoUpdatePanel(parent, newFormat, debug, k);
		specialUpdatePanel(parent, newFormat, debug, k);
	}
	private	void specialUpdatePanel(Container parent, boolean newFormat, boolean debug, int k) {
		if (newFormat) {
			setBackground(frameBgColor);
		}
		else {
			Component[] componentList = getComponents();
			for(Component c : componentList){
				if (c instanceof JPanel) {
					setBackground(c.getBackground());
					break;
				}
			}
		}
		setRaceImg();
	}
	private	void autoUpdatePanel(Container parent, boolean newFormat, boolean debug, int k) {
		for (Component c : parent.getComponents()) {
			if (c instanceof BasicArrowButton) {
				if (debug) System.out.println("BasicArrowButton : " + k + " -- " + c.toString());
				setBasicArrowButton(c, newFormat, debug);
			} 
			else if (c instanceof RotpJSpinnerButton) {
				if (debug) System.out.println("RotpSpinnerButton : " + k + " -- " + c.toString());
				setRotpSpinnerButton(c, newFormat, debug);
			}
			else if (c instanceof JButton) {
				if (debug) System.out.println("JButton : " + k + " -- " + c.toString());
				setJButton(c, newFormat, debug);
			}
			else if (c instanceof JCheckBox) {
				if (debug) System.out.println("JCheckBox : " + k + " -- " + c.toString());
				setJCheckBox(c, newFormat, debug);
			}
			else if (c instanceof JRadioButton) {
				if (debug) System.out.println("JRadioButton : " + k + " -- " + c.toString());
				setJRadioButton(c, newFormat, debug);
			}
			else if (c instanceof JToggleButton) {
				if (debug) System.out.println("JToggleButton : " + k + " -- " + c.toString());
				setJToggleButton(c, newFormat, debug);
			}
			else if (c instanceof JSpinner) {
				if (debug) System.out.println("JSpinner : " + k + " -- " + c.toString());
				setJSpinner(c, newFormat, debug);
			}
			else if (c instanceof JLabel) {
				if (debug) System.out.println("JLabel : " + k + " -- " + c.toString());
				setJLabel(c, newFormat, debug);
			}
			else if (c instanceof JFormattedTextField) {
				if (debug) System.out.println("JFormattedTextField : " + k + " -- " + c.toString());
				setJFormattedTextField(c, newFormat, debug);
			}
			else if (c instanceof NumberEditor) {
				if (debug) System.out.println("NumberEditor : " + k + " -- " + c.toString());
				setNumberEditor(c, newFormat, debug);
			}
			else if (c instanceof JPanel) {
				if (debug) System.out.println("JPanel : " + k + " -- " + c.toString());
				setJPanel(c, newFormat, debug);
			}
			else if (c instanceof JLayeredPane) {
				if (debug) System.out.println("JLayeredPane : " + k + " -- " + c.toString());
				setJLayeredPane(c, newFormat, debug);
			}
			else if (c instanceof JRootPane) {
				if (debug) System.out.println("JRootPane : " + k + " -- " + c.toString());
				setJRootPane(c, newFormat, debug);
			}
			else {
				if (debug) System.out.println("-- " + k + " -- " + c.toString());
			}
			if (c instanceof Container) {
				autoUpdatePanel((Container)c, newFormat, debug, k+1);
			}
		}
	}

	// ========== Load and save Values ==========
	//
	private void loadValues() {
		GovernorOptions options = govOptions();
		// Other Options and duplicate
		this.governorDefault.setSelected(options.isGovernorOnByDefault());
		this.completionist.setEnabled(isCompletionistEnabled());
		this.autoApplyToggleButton.setSelected(isAutoApply());
		
		// AutoTransport Options
		this.autotransportAI.setSelected(options.isAutotransportAI());
		this.autotransportFull.setSelected(options.isAutotransportFull());
		this.allowUngoverned.setSelected(options.isAutotransportUngoverned());
		this.transportMaxTurns.setValue(options.getTransportMaxTurns());
		this.transportRichDisabled.setSelected(options.isTransportRichDisabled());
		this.transportPoorDouble.setSelected(options.isTransportPoorDouble());

		// StarGates Options
		switch (govOptions().getGates()) {
			case None:
				this.stargateOff.setSelected(true);
				break;
			case Rich:
				this.stargateRich.setSelected(true);
				break;
			case All:
				this.stargateOn.setSelected(true);
				break;
		}

		// Colony Options
		this.missileBases.setValue(options.getMinimumMissileBases());
		this.shieldWithoutBases.setSelected(options.getShieldWithoutBases());
		this.autospend.setSelected(options.isAutospend());
		this.reserveFromRich.setSelected(options.isReserveFromRich());
		this.excessToResearch.setSelected(options.isExcessToResearch());
		this.reserve.setValue(options.getReserve());
		this.shipbuilding.setSelected(options.isShipbuilding());
		this.followColonyRequests.setSelected(options.isFollowingColonyRequests());
		this.legacyGrowthMode.setSelected(options.legacyGrowthMode());
		this.terraformEarly.setValue(options.terraformEarly());

		// Intelligence Options
		this.autoInfiltrate.setSelected(options.isAutoInfiltrate());
		this.autoSpy.setSelected(options.isAutoSpy());
		this.spareXenophobes.setSelected(options.respectPromises());

		// Fleet Options
		this.autoScout.setSelected(options.isAutoScout());
		this.autoColonize.setSelected(options.isAutoColonize());
		this.autoAttack.setSelected(options.isAutoAttack());
		this.autoScoutShipCount.setValue(options.getAutoScoutShipCount());
		this.autoColonyShipCount.setValue(options.getAutoColonyShipCount());
		this.autoAttackShipCount.setValue(options.getAutoAttackShipCount());

		// Aspect Options
		this.customSize.setSelected(isCustomSize());
		this.sizePct.setValue(getSizeFactor());
		this.brightnessPct.setValue(getBrightnessPct());
		this.isOriginal.setSelected(!isNewFormat());
	}
	private void applyAction() {// BR: Save Values
		if (!isAutoApply())
			return;
		GovernorOptions options = govOptions();
		
		// AutoTransport Options
		options.setAutotransportAI(autotransportAI.isSelected());
		options.setAutotransportFull(autotransportFull.isSelected());
		options.setAutotransportUngoverned(allowUngoverned.isSelected());
		options.setTransportMaxTurns((Integer)transportMaxTurns.getValue());
		options.setTransportRichDisabled(transportRichDisabled.isSelected());
		options.setTransportPoorDouble(transportPoorDouble.isSelected());

		// StarGates Options
		applyStargates();

		// Colony Options
		options.setMinimumMissileBases((Integer)missileBases.getValue());
		options.setShieldWithoutBases(shieldWithoutBases.isSelected());
		options.setAutospend(autospend.isSelected());
		options.setReserveFromRich(reserveFromRich.isSelected());
		options.setExcessToResearch(excessToResearch.isSelected());
		options.setReserve((Integer)reserve.getValue());
		options.setShipbuilding(shipbuilding.isSelected());
		options.setfollowColonyRequests(followColonyRequests.isSelected());
		options.setLegacyGrowthMode(legacyGrowthMode.isSelected());
		options.setTerraformEarly((Integer)terraformEarly.getValue());

		// Intelligence Options
		options.setAutoInfiltrate(autoInfiltrate.isSelected());
		options.setAutoSpy(autoSpy.isSelected());
		options.setRespectPromises(spareXenophobes.isSelected());

		// Fleet Options
		options.setAutoScout(autoScout.isSelected());
		options.setAutoColonize(autoColonize.isSelected());
		options.setAutoAttack(autoAttack.isSelected());
		options.setAutoScoutShipCount((Integer)autoScoutShipCount.getValue());
		options.setAutoColonyShipCount((Integer)autoColonyShipCount.getValue());
		options.setAutoAttackShipCount((Integer)autoAttackShipCount.getValue());

		// Aspect Options
		options.setIsOriginalPanel(isOriginal.isSelected());
		options.setIsCustomSize(customSize.isSelected());
		options.setSizeFactorPct((Integer)sizePct.getValue());
		options.setBrightnessPct((Integer)brightnessPct.getValue());
		// Other Options
		options.setGovernorOnByDefault(governorDefault.isSelected());
		options.setIsAnimatedImage(isAnimatedImage());
	}								   
	private void applyStargates() {// BR: 
		if (stargateOff.isSelected())
			govOptions().setGates(GovernorOptions.GatesGovernor.None);
		else if (stargateRich.isSelected())
			govOptions().setGates(GovernorOptions.GatesGovernor.Rich);
		else if (stargateOn.isSelected())
			govOptions().setGates(GovernorOptions.GatesGovernor.All);
	}

	// ========== Completionist tools ==========
	//
	private boolean isCompletionistEnabled() {
		if (GameSession.instance().galaxy() == null) {
			return false;
		}
		float colonized = GameSession.instance().galaxy().numColonizedSystems() / (float)GameSession.instance().galaxy().numStarSystems();
		float controlled = GameSession.instance().galaxy().player().numColonies() / GameSession.instance().galaxy().numColonizedSystems();
		boolean completed = GameSession.instance().galaxy().player().tech().researchCompleted();
		// System.out.format("Colonized %.2f galaxy, controlled %.2f galaxy, completed research %s%n", colonized, controlled, completed);
		if (colonized >= 0.3 && controlled >= 0.5 && completed) {
			return true;
		} else {
			return false;
		}
	}
	private void performCompletionist() {
		// game not in session
		if (GameSession.instance().galaxy() == null) {
			return;
		}
		// Techs to give
		String techs[] = {
				"ImprovedTerraforming:8",
				"SoilEnrichment:1",
				"AtmosphereEnrichment:0",
				"ControlEnvironment:6",
				"Stargate:0"
		};
		for (Empire e: GameSession.instance().galaxy().empires()) {
			if (e.extinct()) {
				continue;
			}
			for (String t: techs) {
				e.tech().allowResearch(t);
			}
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        stargateOptions = new javax.swing.ButtonGroup();
        governorDefault = new javax.swing.JCheckBox();
        javax.swing.JPanel autotransportPanel = new javax.swing.JPanel();
        autotransportFull = new javax.swing.JCheckBox();
        transportMaxTurns = new GovernorJSpinner();
        javax.swing.JLabel transportMaxTurnsLabel = new javax.swing.JLabel();
        javax.swing.JLabel transportMaxTurnsNebula = new javax.swing.JLabel();
        transportRichDisabled = new javax.swing.JCheckBox();
        transportPoorDouble = new javax.swing.JCheckBox();
        autotransportAI = new javax.swing.JCheckBox();
        allowUngoverned = new javax.swing.JCheckBox();
        allGovernorsOn = new javax.swing.JButton();
        allGovernorsOff = new javax.swing.JButton();
        javax.swing.JPanel stargatePanel = new javax.swing.JPanel();
        stargateOff = new javax.swing.JRadioButton();
        stargateRich = new javax.swing.JRadioButton();
        stargateOn = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        completionist = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        autoApplyToggleButton = new javax.swing.JCheckBox();
        javax.swing.JPanel fleetPanel = new javax.swing.JPanel();
        autoScout = new javax.swing.JCheckBox();
        autoColonize = new javax.swing.JCheckBox();
        autoAttack = new javax.swing.JCheckBox();
        autoColonyShipCount = new GovernorJSpinner();
        autoColonyShipCountLabel = new javax.swing.JLabel();
        autoScoutShipCount = new GovernorJSpinner();
        autoAttackShipCount = new GovernorJSpinner();
        autoScoutShipCountLabel = new javax.swing.JLabel();
        autoAttackShipCountLabel = new javax.swing.JLabel();
        javax.swing.JPanel colonyPanel = new javax.swing.JPanel();
        autospend = new javax.swing.JCheckBox();
        reserve = new GovernorJSpinner();
        reserveLabel = new javax.swing.JLabel();
        shipbuilding = new javax.swing.JCheckBox();
        shieldWithoutBases = new javax.swing.JCheckBox();
        legacyGrowthMode = new javax.swing.JCheckBox();
        missileBases = new GovernorJSpinner();
        missileBasesLabel = new javax.swing.JLabel();
        terraformEarly = new GovernorJSpinner();
        terraformEarlyLabel = new javax.swing.JLabel();
        followColonyRequests = new javax.swing.JCheckBox();
        reserveFromRich = new javax.swing.JCheckBox();
        excessToResearch = new javax.swing.JCheckBox();
        javax.swing.JPanel spyPanel = new javax.swing.JPanel();
        spareXenophobes = new javax.swing.JCheckBox();
        autoSpy = new javax.swing.JCheckBox();
        autoInfiltrate = new javax.swing.JCheckBox();
        jPanelAspect = new javax.swing.JPanel();
        isOriginal = new javax.swing.JCheckBox();
        customSize = new javax.swing.JCheckBox();
        sizePct = new GovernorJSpinner();
        sizeFactorLabel = new javax.swing.JLabel();
        brightnessPct = new GovernorJSpinner();
        brightnessLabel = new javax.swing.JLabel();
        raceImage = new javax.swing.JLabel();
        fineTuningButton = new javax.swing.JButton();

        governorDefault.setSelected(true);
        governorDefault.setText("Governor is on by default");
        governorDefault.setName("ON_BY_DEFAULT"); // NOI18N
        governorDefault.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                governorDefaultActionPerformed(evt);
            }
        });

        autotransportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Autotransport Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13))); // NOI18N
        autotransportPanel.setName("TRANSPORT_OPTIONS"); // NOI18N

        autotransportFull.setText("Population automatically transported from colonies close to max population capacity");
        autotransportFull.setMinimumSize(new java.awt.Dimension(0, 0));
        autotransportFull.setName("AUTOTRANSPORT_GOV"); // NOI18N
        autotransportFull.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                autotransportFullActionPerformed(evt);
            }
        });

        transportMaxTurns.setModel(new javax.swing.SpinnerNumberModel(15, 1, 15, 1));
        transportMaxTurns.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
                transportMaxTurnsStateChanged(evt);
            }
        });
        transportMaxTurns.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                transportMaxTurnsMouseWheelMoved(evt);
            }
        });

        transportMaxTurnsLabel.setText("Maximum transport distance in turns");
        transportMaxTurnsLabel.setName("TRANSPORT_MAX_TURNS"); // NOI18N
        transportMaxTurnsLabel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                transportMaxTurnsLabelMouseWheelMoved(evt);
            }
        });

        transportMaxTurnsNebula.setText("(1.5x higher distance when transporting to nebulae)");
        transportMaxTurnsNebula.setName("TRANSPORT_MAX_TURNS_NEBULA"); // NOI18N

        transportRichDisabled.setText("Don't send from Rich/Artifacts planets");
        transportRichDisabled.setName("TRANSPORT_RICH_OFF"); // NOI18N
        transportRichDisabled.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                transportRichDisabledActionPerformed(evt);
            }
        });

        transportPoorDouble.setText("Send double from Poor planets");
        transportPoorDouble.setName("TRANSPORT_POOR_DBL"); // NOI18N
        transportPoorDouble.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                transportPoorDoubleActionPerformed(evt);
            }
        });

        autotransportAI.setText("Let AI handle population transportation (Xilmi AI)");
        autotransportAI.setName("AUTO_TRANSPORT"); // NOI18N
        autotransportAI.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                autotransportAIActionPerformed(evt);
            }
        });

        allowUngoverned.setText("Allow sending population from ungoverned colonies");
        allowUngoverned.setName("TRANSPORT_UNGOVERNED"); // NOI18N
        allowUngoverned.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                allowUngovernedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout autotransportPanelLayout = new javax.swing.GroupLayout(autotransportPanel);
        autotransportPanel.setLayout(autotransportPanelLayout);
        autotransportPanelLayout.setHorizontalGroup(
            autotransportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autotransportPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autotransportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(transportPoorDouble)
                    .addComponent(transportMaxTurnsNebula)
                    .addGroup(autotransportPanelLayout.createSequentialGroup()
                        .addComponent(transportMaxTurns, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(transportMaxTurnsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(autotransportAI)
                    .addComponent(allowUngoverned)
                    .addComponent(autotransportFull, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transportRichDisabled))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        autotransportPanelLayout.setVerticalGroup(
            autotransportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autotransportPanelLayout.createSequentialGroup()
                .addComponent(autotransportAI)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(allowUngoverned)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autotransportFull, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(autotransportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(transportMaxTurns, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(transportMaxTurnsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transportMaxTurnsNebula)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transportRichDisabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transportPoorDouble))
        );

        allGovernorsOn.setText("All Governors ON");
        allGovernorsOn.setName("ALL_GOVERNORS_ON"); // NOI18N
        allGovernorsOn.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                allGovernorsOnActionPerformed(evt);
            }
        });

        allGovernorsOff.setText("All Governors OFF");
        allGovernorsOff.setName("ALL_GOVERNORS_OFF"); // NOI18N
        allGovernorsOff.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                allGovernorsOffActionPerformed(evt);
            }
        });

        stargatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Stargate Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13))); // NOI18N
        stargatePanel.setName("STARGATES_OPTIONS"); // NOI18N

        stargateOptions.add(stargateOff);
        stargateOff.setText("Never build stargates");
        stargateOff.setName("STARGATES_NONE"); // NOI18N
        stargateOff.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                stargateOffActionPerformed(evt);
            }
        });

        stargateOptions.add(stargateRich);
        stargateRich.setText("Build stargates on Rich");
        stargateRich.setName("STARGATES_RICH_1"); // NOI18N
        stargateRich.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                stargateRichActionPerformed(evt);
            }
        });

        stargateOptions.add(stargateOn);
        stargateOn.setText("Always build stargates");
        stargateOn.setName("STARGATES_ALL"); // NOI18N
        stargateOn.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                stargateOnActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("and Ultra Rich planets");
        jLabel1.setName("STARGATES_RICH_2"); // NOI18N

        javax.swing.GroupLayout stargatePanelLayout = new javax.swing.GroupLayout(stargatePanel);
        stargatePanel.setLayout(stargatePanelLayout);
        stargatePanelLayout.setHorizontalGroup(
            stargatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stargatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(stargatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stargateOff)
                    .addComponent(stargateRich)
                    .addComponent(stargateOn)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        stargatePanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, stargateRich});

        stargatePanelLayout.setVerticalGroup(
            stargatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stargatePanelLayout.createSequentialGroup()
                .addComponent(stargateOff)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stargateRich)
                .addGap(0, 0, 0)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stargateOn)
                .addGap(0, 0, 0))
        );

        okButton.setText("OK");
        okButton.setToolTipText("Apply settings and close the GUI");
        okButton.setName("OK_BUTTON"); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.setName("CANCEL_BUTTON"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        completionist.setText("Completionist Technologies");
        completionist.setToolTipText("<html>\nI like completing games fully. <br/>\nAllow all Empires to Research the following Technologies:<br/>\n<br/>\nControlled Irradiated Environment<br/>\nAtmospheric Terraforming<br/>\nComplete Terraforming<br/>\nAdvanced Soil Enrichment<br/>\nIntergalactic Star Gates<br/>\n<br/>\nMore than 30% of the Galaxy needs to be colonized.<br/>\nPlayer must control more than 50% of colonized systems.<br/>\nPlayer must have completed all Research in their Tech Tree (Future Techs too).<br/>\n</html>");
        completionist.setName("COMPLETIONIST_TECHNOLOGIES"); // NOI18N
        completionist.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                completionistActionPerformed(evt);
            }
        });

        applyButton.setText("Apply");
        applyButton.setToolTipText("Apply settings and keep GUI open");
        applyButton.setName("APPLY_BUTTON"); // NOI18N
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        autoApplyToggleButton.setSelected(true);
        autoApplyToggleButton.setText("Auto Apply");
        autoApplyToggleButton.setToolTipText("For the settings to be applied live.");
        autoApplyToggleButton.setName("AUTO_APPLY"); // NOI18N
        autoApplyToggleButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoApplyToggleButtonActionPerformed(evt);
            }
        });

        fleetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Fleet Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13))); // NOI18N
        fleetPanel.setName("FLEET_OPTIONS"); // NOI18N

        autoScout.setText("Auto Scout");
        autoScout.setName("AUTO_SCOUT"); // NOI18N
        autoScout.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoScoutActionPerformed(evt);
            }
        });

        autoColonize.setText("Auto Colonize");
        autoColonize.setName("AUTO_COLONIZE"); // NOI18N
        autoColonize.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoColonizeActionPerformed(evt);
            }
        });

        autoAttack.setText("Auto Attack");
        autoAttack.setName("AUTO_ATTACK"); // NOI18N
        autoAttack.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoAttackActionPerformed(evt);
            }
        });

        autoColonyShipCount.setModel(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
        autoColonyShipCount.setName("AUTO_COLONY_COUNT"); // NOI18N
        autoColonyShipCount.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
                autoColonyShipCountStateChanged(evt);
            }
        });
        autoColonyShipCount.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                autoColonyShipCountMouseWheelMoved(evt);
            }
        });

        autoColonyShipCountLabel.setText("Number of colony ships to send");
        autoColonyShipCountLabel.setName("AUTO_COLONY_COUNT"); // NOI18N

        autoScoutShipCount.setModel(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
        autoScoutShipCount.setName("AUTO_SCOUT_COUNT"); // NOI18N
        autoScoutShipCount.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
                autoScoutShipCountStateChanged(evt);
            }
        });
        autoScoutShipCount.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                autoScoutShipCountMouseWheelMoved(evt);
            }
        });

        autoAttackShipCount.setModel(new javax.swing.SpinnerNumberModel(1, 1, 9999, 1));
        autoAttackShipCount.setName("AUTO_ATTACK_COUNT"); // NOI18N
        autoAttackShipCount.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
                autoAttackShipCountStateChanged(evt);
            }
        });
        autoAttackShipCount.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                autoAttackShipCountMouseWheelMoved(evt);
            }
        });

        autoScoutShipCountLabel.setText("Number of scout ships to send");
        autoScoutShipCountLabel.setName("AUTO_SCOUT_COUNT"); // NOI18N

        autoAttackShipCountLabel.setText("Number of attack ships to send");
        autoAttackShipCountLabel.setName("AUTO_ATTACK_COUNT"); // NOI18N

        javax.swing.GroupLayout fleetPanelLayout = new javax.swing.GroupLayout(fleetPanel);
        fleetPanel.setLayout(fleetPanelLayout);
        fleetPanelLayout.setHorizontalGroup(
            fleetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fleetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(fleetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(autoColonize)
                    .addComponent(autoScout)
                    .addComponent(autoAttack))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(fleetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(autoAttackShipCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoColonyShipCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoScoutShipCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(fleetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fleetPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(fleetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(autoColonyShipCountLabel)
                            .addComponent(autoScoutShipCountLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(fleetPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(autoAttackShipCountLabel)))
                .addContainerGap())
        );

        fleetPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {autoAttackShipCount, autoColonyShipCount, autoScoutShipCount});

        fleetPanelLayout.setVerticalGroup(
            fleetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fleetPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(fleetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoScoutShipCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoScoutShipCountLabel)
                    .addComponent(autoScout))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                .addGroup(fleetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoColonize)
                    .addComponent(autoColonyShipCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoColonyShipCountLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE)
                .addGroup(fleetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoAttackShipCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoAttackShipCountLabel)
                    .addComponent(autoAttack))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        colonyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Colony Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13))); // NOI18N
        colonyPanel.setName("COLONY_OPTIONS"); // NOI18N

        autospend.setText("Autospend");
        autospend.setToolTipText("Automatically spend reserve on planets with lowest production");
        autospend.setName("AUTOSPEND"); // NOI18N
        autospend.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                autospendActionPerformed(evt);
            }
        });

        reserve.setModel(new javax.swing.SpinnerNumberModel(1000, 0, 100000, 10));
        reserve.setName("RESERVE"); // NOI18N
        reserve.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
                reserveStateChanged(evt);
            }
        });
        reserve.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                reserveMouseWheelMoved(evt);
            }
        });

        reserveLabel.setText("Keep in reserve");
        reserveLabel.setName("RESERVE"); // NOI18N

        shipbuilding.setText("Shipbuilding with Governor enabled");
        shipbuilding.setToolTipText("Divert resources into shipbuilding and not research if planet is already building ships");
        shipbuilding.setName("SHIP_BUILDING"); // NOI18N
        shipbuilding.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                shipbuildingActionPerformed(evt);
            }
        });

        shieldWithoutBases.setText("Allow shields without bases");
        shieldWithoutBases.setName("SHIELD_WITHOUT_BASES"); // NOI18N
        shieldWithoutBases.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                shieldWithoutBasesActionPerformed(evt);
            }
        });

        legacyGrowthMode.setText("Develop colonies as quickly as possible");
        legacyGrowthMode.setName("LEGACY_GROWTH_MODE"); // NOI18N
        legacyGrowthMode.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                legacyGrowthModeActionPerformed(evt);
            }
        });

        missileBases.setModel(new javax.swing.SpinnerNumberModel(0, 0, 1000, 1));
        missileBases.setName("MIN_MISSILE_BASES"); // NOI18N
        missileBases.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
                missileBasesStateChanged(evt);
            }
        });
        missileBases.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                missileBasesMouseWheelMoved(evt);
            }
        });

        missileBasesLabel.setText("Minimum missile bases");
        missileBasesLabel.setName("MIN_MISSILE_BASES"); // NOI18N

        terraformEarly.setModel(new javax.swing.SpinnerNumberModel(0, 0, 400, 1));
        terraformEarly.setName("TERRAFORM_EARLY"); // NOI18N
        terraformEarly.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
                terraformEarlyStateChanged(evt);
            }
        });
        terraformEarly.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                terraformEarlyMouseWheelMoved(evt);
            }
        });

        terraformEarlyLabel.setText("Boost Planet Early");
        terraformEarlyLabel.setName("TERRAFORM_EARLY"); // NOI18N

        followColonyRequests.setText("Follow Colony Requests");
        followColonyRequests.setToolTipText("Follow Colony Requests");
        followColonyRequests.setName("FOLLOW_COLONY_REQUESTS"); // NOI18N
        followColonyRequests.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                followColonyRequestsActionPerformed(evt);
            }
        });

        reserveFromRich.setText("Reserve from Rich and Ultra Rich colonies");
        reserveFromRich.setToolTipText("Automatically spend reserve on planets with lowest production");
        reserveFromRich.setName("RESERVE_FROM_RICH"); // NOI18N
        reserveFromRich.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                reserveFromRichActionPerformed(evt);
            }
        });

        excessToResearch.setText("Excess to Research");
        excessToResearch.setToolTipText("Automatically spend reserve on planets with lowest production");
        excessToResearch.setName("DIVERT_EXCESS_TO_RESEARCH"); // NOI18N
        excessToResearch.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                excessToResearchActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout colonyPanelLayout = new javax.swing.GroupLayout(colonyPanel);
        colonyPanel.setLayout(colonyPanelLayout);
        colonyPanelLayout.setHorizontalGroup(
            colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colonyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colonyPanelLayout.createSequentialGroup()
                        .addComponent(missileBases)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(missileBasesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colonyPanelLayout.createSequentialGroup()
                        .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(colonyPanelLayout.createSequentialGroup()
                                .addComponent(terraformEarly, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(terraformEarlyLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(shipbuilding)
                            .addComponent(shieldWithoutBases)
                            .addComponent(legacyGrowthMode))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colonyPanelLayout.createSequentialGroup()
                        .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(colonyPanelLayout.createSequentialGroup()
                                .addComponent(reserve, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(reserveLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(autospend, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(reserveFromRich, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(excessToResearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colonyPanelLayout.createSequentialGroup()
                        .addComponent(followColonyRequests)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        colonyPanelLayout.setVerticalGroup(
            colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colonyPanelLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colonyPanelLayout.createSequentialGroup()
                        .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(missileBases, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(missileBasesLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(autospend)
                            .addComponent(shieldWithoutBases))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(reserveFromRich)
                            .addComponent(shipbuilding))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(excessToResearch)
                            .addComponent(legacyGrowthMode)))
                    .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(reserve, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(reserveLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colonyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(terraformEarlyLabel)
                        .addComponent(terraformEarly, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(followColonyRequests))
                .addGap(0, 0, 0))
        );

        colonyPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {missileBases, missileBasesLabel});

        colonyPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {reserve, reserveLabel});

        reserveFromRich.getAccessibleContext().setAccessibleDescription("Allocate spending from fully built rich and ultra-rich colonies to “industry” in order to redirect credit production to the empire reserve.");
        excessToResearch.getAccessibleContext().setAccessibleDescription("Divert excess colony spending from treasury reserve to research.");

        spyPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Intelligence Options", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13))); // NOI18N
        spyPanel.setName("INTELLIGENCE_OPTIONS"); // NOI18N

        spareXenophobes.setText("Respect promises");
        spareXenophobes.setToolTipText("Once enjoined to stop espionage by an alien empire, the Governor will follow the player's choice for the time necessary for the empire to calm down.");
        spareXenophobes.setName("SPARE_XENOPHOBES"); // NOI18N
        spareXenophobes.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                spareXenophobesActionPerformed(evt);
            }
        });

        autoSpy.setText("Let AI handle spies");
        autoSpy.setToolTipText("Hand control over spies to AI");
        autoSpy.setName("AUTO_SPY"); // NOI18N
        autoSpy.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoSpyActionPerformed(evt);
            }
        });

        autoInfiltrate.setText("Auto Infiltrate");
        autoInfiltrate.setToolTipText("Automatically sends spies to infiltrate other empires");
        autoInfiltrate.setName("AUTO_INFILTRATE"); // NOI18N
        autoInfiltrate.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoInfiltrateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout spyPanelLayout = new javax.swing.GroupLayout(spyPanel);
        spyPanel.setLayout(spyPanelLayout);
        spyPanelLayout.setHorizontalGroup(
            spyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spyPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(spyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(autoInfiltrate)
                    .addComponent(autoSpy)
                    .addComponent(spareXenophobes))
                .addContainerGap())
        );
        spyPanelLayout.setVerticalGroup(
            spyPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(spyPanelLayout.createSequentialGroup()
                .addComponent(autoInfiltrate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(autoSpy)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(spareXenophobes)
                .addContainerGap())
        );

        jPanelAspect.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Aspect", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 13))); // NOI18N
        jPanelAspect.setName("ASPECT_OPTIONS"); // NOI18N

        isOriginal.setText("Original View");
        isOriginal.setName("ORIGINAL_PANEL"); // NOI18N
        isOriginal.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                isOriginalActionPerformed(evt);
            }
        });

        customSize.setText("CustomSize");
        customSize.setName("CUSTOM_SIZE"); // NOI18N
        customSize.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                customSizeActionPerformed(evt);
            }
        });

        sizePct.setModel(new javax.swing.SpinnerNumberModel(100, 20, 200, 1));
        sizePct.setToolTipText("Size Factor");
        sizePct.setName("SIZE_FACTOR"); // NOI18N
        sizePct.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sizePctStateChanged(evt);
            }
        });
        sizePct.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                sizePctMouseWheelMoved(evt);
            }
        });

        sizeFactorLabel.setText("% Size");
        sizeFactorLabel.setName("SIZE_FACTOR"); // NOI18N

        brightnessPct.setModel(new javax.swing.SpinnerNumberModel(100, 20, 300, 1));
        brightnessPct.setToolTipText("Color Brightness");
        brightnessPct.setName("BRIGHTNESS"); // NOI18N
        brightnessPct.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
			public void stateChanged(javax.swing.event.ChangeEvent evt) {
                brightnessPctStateChanged(evt);
            }
        });
        brightnessPct.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
			public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                brightnessPctMouseWheelMoved(evt);
            }
        });

        brightnessLabel.setText("% Bright");
        brightnessLabel.setName("BRIGHTNESS"); // NOI18N

        javax.swing.GroupLayout jPanelAspectLayout = new javax.swing.GroupLayout(jPanelAspect);
        jPanelAspect.setLayout(jPanelAspectLayout);
        jPanelAspectLayout.setHorizontalGroup(
            jPanelAspectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAspectLayout.createSequentialGroup()
                .addGroup(jPanelAspectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelAspectLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(sizePct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sizeFactorLabel))
                    .addGroup(jPanelAspectLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanelAspectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(isOriginal)
                            .addComponent(customSize)))
                    .addGroup(jPanelAspectLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(brightnessPct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(brightnessLabel)))
                .addGap(0, 0, 0))
        );

        jPanelAspectLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {customSize, isOriginal});

        jPanelAspectLayout.setVerticalGroup(
            jPanelAspectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelAspectLayout.createSequentialGroup()
                .addComponent(isOriginal)
                .addGap(0, 0, 0)
                .addComponent(customSize)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelAspectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sizePct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sizeFactorLabel))
                .addGap(0, 0, 0)
                .addGroup(jPanelAspectLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(brightnessPct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(brightnessLabel))
                .addGap(0, 0, 0))
        );

        jPanelAspectLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {brightnessLabel, brightnessPct});

        jPanelAspectLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {sizeFactorLabel, sizePct});

        raceImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        raceImage.setFocusable(false);
        raceImage.setMinimumSize(new java.awt.Dimension(50, 50));
        raceImage.setName("RACE_IMAGE"); // NOI18N
        raceImage.setRequestFocusEnabled(false);
        raceImage.setVerifyInputWhenFocusTarget(false);
        raceImage.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
                raceImageMouseClicked(evt);
            }
        });

        fineTuningButton.setText("Fine Tuning");
        fineTuningButton.setName("FINE_TUNING_BUTTON"); // NOI18N
        fineTuningButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
                fineTuningButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(applyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(autoApplyToggleButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(completionist)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cancelButton))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(fleetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jPanelAspect, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(2, 2, 2))
                                    .addComponent(autotransportPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(colonyPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(allGovernorsOn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(governorDefault)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(allGovernorsOff)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(stargatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(raceImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(fineTuningButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {raceImage, spyPanel, stargatePanel});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(governorDefault)
                            .addComponent(allGovernorsOff)
                            .addComponent(allGovernorsOn))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(autotransportPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(raceImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(colonyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(spyPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(fineTuningButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fleetPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanelAspect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stargatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cancelButton)
                    .addComponent(okButton)
                    .addComponent(applyButton)
                    .addComponent(completionist)
                    .addComponent(autoApplyToggleButton))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {allGovernorsOff, allGovernorsOn, governorDefault});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {applyButton, autoApplyToggleButton, cancelButton, completionist, okButton});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {fleetPanel, jPanelAspect, stargatePanel});

    }// </editor-fold>//GEN-END:initComponents

	private void allGovernorsOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allGovernorsOnActionPerformed
		for (StarSystem sys : GameSession.instance().galaxy().player().orderedColonies()) {
			if (!sys.isColonized()) {
				// shouldn't happen
				continue;
			}
			sys.colony().setGovernor(true);
			sys.colony().governIfNeeded();
			if (isAutoApply())
				govOptions().setGovernorOnByDefault(governorDefault.isSelected());
		}
	}//GEN-LAST:event_allGovernorsOnActionPerformed

	private void allGovernorsOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allGovernorsOffActionPerformed
		for (StarSystem ss : GameSession.instance().galaxy().player().orderedColonies()) {
			if (!ss.isColonized()) {
				// shouldn't happen
				continue;
			}
			ss.colony().setGovernor(false);
			if (isAutoApply())
				govOptions().setGovernorOnByDefault(governorDefault.isSelected());
		}
		this.allowUngoverned.setSelected(false);
		govOptions().setAutotransportUngoverned(allowUngoverned.isSelected());
		this.autoSpy.setSelected(false);
		govOptions().setAutoSpy(autoSpy.isSelected());
		this.autoInfiltrate.setSelected(false);
		govOptions().setAutoInfiltrate(autoInfiltrate.isSelected());
	}//GEN-LAST:event_allGovernorsOffActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
		applyAction();
		frame.setVisible(false);
	}//GEN-LAST:event_okButtonActionPerformed

	private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
		applyAction();
	}//GEN-LAST:event_applyButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
		frame.setVisible(false);
	}//GEN-LAST:event_cancelButtonActionPerformed

	private void completionistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_completionistActionPerformed
		performCompletionist();
	}//GEN-LAST:event_completionistActionPerformed

	private void autoColonyShipCountMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_autoColonyShipCountMouseWheelMoved
		mouseWheel(autoColonyShipCount, evt);
	}//GEN-LAST:event_autoColonyShipCountMouseWheelMoved

	private void autoScoutShipCountMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_autoScoutShipCountMouseWheelMoved
		mouseWheel(autoScoutShipCount, evt);
	}//GEN-LAST:event_autoScoutShipCountMouseWheelMoved

	private void autoAttackShipCountMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_autoAttackShipCountMouseWheelMoved
		mouseWheel(autoAttackShipCount, evt);
	}//GEN-LAST:event_autoAttackShipCountMouseWheelMoved

	private void autotransportAIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autotransportAIActionPerformed
		if (isAutoApply())
			govOptions().setAutotransportAI(autotransportAI.isSelected());
	}//GEN-LAST:event_autotransportAIActionPerformed

	private void transportMaxTurnsLabelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_transportMaxTurnsLabelMouseWheelMoved
		mouseWheel(transportMaxTurns, evt);
	}//GEN-LAST:event_transportMaxTurnsLabelMouseWheelMoved

	private void transportMaxTurnsMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_transportMaxTurnsMouseWheelMoved
		mouseWheel(transportMaxTurns, evt);
	}//GEN-LAST:event_transportMaxTurnsMouseWheelMoved

	private void autoApplyToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoApplyToggleButtonActionPerformed
		govOptions().setAutoApply(autoApplyToggleButton.isSelected());
		applyAction();
	}//GEN-LAST:event_autoApplyToggleButtonActionPerformed

	private void allowUngovernedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allowUngovernedActionPerformed
		if (isAutoApply())
			govOptions().setAutotransportUngoverned(allowUngoverned.isSelected());
	}//GEN-LAST:event_allowUngovernedActionPerformed

	private void autotransportFullActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autotransportFullActionPerformed
		if (isAutoApply())
			govOptions().setAutotransportFull(autotransportAI.isSelected());
	}//GEN-LAST:event_autotransportFullActionPerformed

	private void transportRichDisabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transportRichDisabledActionPerformed
		if (isAutoApply())
			govOptions().setTransportRichDisabled(transportRichDisabled.isSelected());
	}//GEN-LAST:event_transportRichDisabledActionPerformed

	private void transportPoorDoubleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transportPoorDoubleActionPerformed
		if (isAutoApply())
			govOptions().setTransportPoorDouble(transportPoorDouble.isSelected());
	}//GEN-LAST:event_transportPoorDoubleActionPerformed

	private void autoScoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoScoutActionPerformed
		if (isAutoApply())
			govOptions().setAutoScout(autoScout.isSelected());
	}//GEN-LAST:event_autoScoutActionPerformed

	private void autoColonizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoColonizeActionPerformed
		if (isAutoApply())
			govOptions().setAutoColonize(autoColonize.isSelected());
	}//GEN-LAST:event_autoColonizeActionPerformed

	private void autoAttackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoAttackActionPerformed
		if (isAutoApply())
			govOptions().setAutoAttack(autoAttack.isSelected());
	}//GEN-LAST:event_autoAttackActionPerformed

	private void autoInfiltrateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoInfiltrateActionPerformed
		if (isAutoApply())
			govOptions().setAutoInfiltrate(autoInfiltrate.isSelected());
	}//GEN-LAST:event_autoInfiltrateActionPerformed

	private void autoSpyActionPerformed(java.awt.event.ActionEvent evt) {                                               
		if (isAutoApply())
			govOptions().setAutoSpy(autoSpy.isSelected());
	}                                                

	private void spareXenophobesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoSpyActionPerformed
		if (isAutoApply())
			govOptions().setRespectPromises(spareXenophobes.isSelected());
	}//GEN-LAST:event_autoSpyActionPerformed

	private void stargateOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stargateOffActionPerformed
		if (isAutoApply()) applyStargates();
	}//GEN-LAST:event_stargateOffActionPerformed

	private void stargateRichActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stargateRichActionPerformed
		if (isAutoApply()) applyStargates();
	}//GEN-LAST:event_stargateRichActionPerformed

	private void stargateOnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stargateOnActionPerformed
		if (isAutoApply()) applyStargates();
	}//GEN-LAST:event_stargateOnActionPerformed

	private void autoAttackShipCountStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_autoAttackShipCountStateChanged
		if (isAutoApply())
			govOptions().setAutoAttackShipCount((Integer)autoAttackShipCount.getValue());
	}//GEN-LAST:event_autoAttackShipCountStateChanged

	private void autoColonyShipCountStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_autoColonyShipCountStateChanged
		if (isAutoApply())
			govOptions().setAutoColonyShipCount((Integer)autoColonyShipCount.getValue());
	}//GEN-LAST:event_autoColonyShipCountStateChanged

	private void autoScoutShipCountStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_autoScoutShipCountStateChanged
		if (isAutoApply())
			govOptions().setAutoScoutShipCount((Integer)autoScoutShipCount.getValue());
   }//GEN-LAST:event_autoScoutShipCountStateChanged

	private void transportMaxTurnsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_transportMaxTurnsStateChanged
		if (isAutoApply())
			govOptions().setTransportMaxTurns((Integer)transportMaxTurns.getValue());
	}//GEN-LAST:event_transportMaxTurnsStateChanged

	private void governorDefaultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_governorDefaultActionPerformed
		if (isAutoApply())
			govOptions().setGovernorOnByDefault(governorDefault.isSelected());
	}//GEN-LAST:event_governorDefaultActionPerformed

	private void brightnessPctMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_brightnessPctMouseWheelMoved
		mouseWheel(brightnessPct, evt);
	}//GEN-LAST:event_brightnessPctMouseWheelMoved

	private void sizePctMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_sizePctMouseWheelMoved
		mouseWheel(sizePct, evt);
	}//GEN-LAST:event_sizePctMouseWheelMoved

	private void isOriginalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isOriginalActionPerformed
		setNewFormat (!isOriginal.isSelected());
		protectedReset();
	}//GEN-LAST:event_isOriginalActionPerformed

	private void customSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customSizeActionPerformed
		setCustomSize(customSize.isSelected());
		protectedUpdateSize();
	}//GEN-LAST:event_customSizeActionPerformed

	private void raceImageMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_raceImageMouseClicked
		if (govOptions().toggleAnimatedImage())
			startAnimation();
		else
			stopAnimation();
	}//GEN-LAST:event_raceImageMouseClicked

	private void sizePctStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sizePctStateChanged
		setSizeFactorPct((Integer)sizePct.getValue());
		if (isCustomSize())
			protectedUpdateSize();
	}//GEN-LAST:event_sizePctStateChanged

	private void brightnessPctStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_brightnessPctStateChanged
		setBrightnessPct((Integer)brightnessPct.getValue());
		protectedUpdateColor();
	}//GEN-LAST:event_brightnessPctStateChanged

    private void excessToResearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excessToResearchActionPerformed
        if (isAutoApply())
        govOptions().setExcessToResearch(excessToResearch.isSelected());;
    }//GEN-LAST:event_excessToResearchActionPerformed

    private void reserveFromRichActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reserveFromRichActionPerformed
        if (isAutoApply())
        govOptions().setReserveFromRich(reserveFromRich.isSelected());;
    }//GEN-LAST:event_reserveFromRichActionPerformed

    private void followColonyRequestsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_followColonyRequestsActionPerformed
        if (isAutoApply())
        govOptions().setfollowColonyRequests(followColonyRequests.isSelected());
    }//GEN-LAST:event_followColonyRequestsActionPerformed

    private void terraformEarlyMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_terraformEarlyMouseWheelMoved
        mouseWheel(terraformEarly, evt);
    }//GEN-LAST:event_terraformEarlyMouseWheelMoved

    private void terraformEarlyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_terraformEarlyStateChanged
        if (isAutoApply())
        govOptions().setTerraformEarly((Integer)terraformEarly.getValue());
    }//GEN-LAST:event_terraformEarlyStateChanged

    private void missileBasesMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_missileBasesMouseWheelMoved
        mouseWheel(missileBases, evt);
    }//GEN-LAST:event_missileBasesMouseWheelMoved

    private void missileBasesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_missileBasesStateChanged
        if (isAutoApply())
        govOptions().setMinimumMissileBases((Integer)missileBases.getValue());
    }//GEN-LAST:event_missileBasesStateChanged

    private void legacyGrowthModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_legacyGrowthModeActionPerformed
        if (isAutoApply())
        govOptions().setLegacyGrowthMode(legacyGrowthMode.isSelected());
    }//GEN-LAST:event_legacyGrowthModeActionPerformed

    private void shieldWithoutBasesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shieldWithoutBasesActionPerformed
        if (isAutoApply())
        govOptions().setShieldWithoutBases(shieldWithoutBases.isSelected());
    }//GEN-LAST:event_shieldWithoutBasesActionPerformed

    private void shipbuildingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shipbuildingActionPerformed
        if (isAutoApply())
        govOptions().setShipbuilding(shipbuilding.isSelected());
    }//GEN-LAST:event_shipbuildingActionPerformed

    private void reserveMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_reserveMouseWheelMoved
        mouseWheel(reserve, evt);
    }//GEN-LAST:event_reserveMouseWheelMoved

    private void reserveStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_reserveStateChanged
        if (isAutoApply())
        govOptions().setReserve((Integer)reserve.getValue());
    }//GEN-LAST:event_reserveStateChanged

    private void autospendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autospendActionPerformed
        if (isAutoApply())
        govOptions().setAutospend(autospend.isSelected());
    }//GEN-LAST:event_autospendActionPerformed

    private void fineTuningButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fineTuningButtonActionPerformed
		ParamSubUI subUI = AllSubUI.getHandle(ISubUiKeys.GOVERNOR_SPECIAL_KEY).getUI();
		subUI.start(this);
		frame.setVisible(false);
    }//GEN-LAST:event_fineTuningButtonActionPerformed

	private  void mouseWheel(JSpinner spinner, java.awt.event.MouseWheelEvent evt) {
		if (evt.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) {
			return;
		}
		SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
		// BR: added Shift and Ctrl accelerator
		int inc = (int) Math.signum(evt.getUnitsToScroll()) * model.getStepSize().intValue();
		if (evt.isShiftDown())
			inc *= 5;
		if (evt.isControlDown())
			inc *= 20;
		int value = inc + (int) model.getValue();	   
		int minimum = ((Number)model.getMinimum()).intValue();
		int maximum = ((Number)model.getMaximum()).intValue();
		if (value < minimum) {
			value = minimum;
		}
		if (value > maximum) {
			value = maximum;
		}
		spinner.setValue(value);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton allGovernorsOff;
    javax.swing.JButton allGovernorsOn;
    javax.swing.JCheckBox allowUngoverned;
    javax.swing.JButton applyButton;
    javax.swing.JCheckBox autoApplyToggleButton;
    javax.swing.JCheckBox autoAttack;
    javax.swing.JSpinner autoAttackShipCount;
    javax.swing.JLabel autoAttackShipCountLabel;
    javax.swing.JCheckBox autoColonize;
    javax.swing.JSpinner autoColonyShipCount;
    javax.swing.JLabel autoColonyShipCountLabel;
    javax.swing.JCheckBox autoInfiltrate;
    javax.swing.JCheckBox autoScout;
    javax.swing.JSpinner autoScoutShipCount;
    javax.swing.JLabel autoScoutShipCountLabel;
    javax.swing.JCheckBox autoSpy;
    javax.swing.JCheckBox autospend;
    javax.swing.JCheckBox autotransportAI;
    javax.swing.JCheckBox autotransportFull;
    javax.swing.JLabel brightnessLabel;
    javax.swing.JSpinner brightnessPct;
    javax.swing.JButton cancelButton;
    javax.swing.JButton completionist;
    javax.swing.JCheckBox customSize;
    javax.swing.JCheckBox excessToResearch;
    javax.swing.JButton fineTuningButton;
    javax.swing.JCheckBox followColonyRequests;
    javax.swing.JCheckBox governorDefault;
    javax.swing.JCheckBox isOriginal;
    javax.swing.JLabel jLabel1;
    javax.swing.JPanel jPanelAspect;
    javax.swing.JCheckBox legacyGrowthMode;
    javax.swing.JSpinner missileBases;
    javax.swing.JLabel missileBasesLabel;
    javax.swing.JButton okButton;
    javax.swing.JLabel raceImage;
    javax.swing.JSpinner reserve;
    javax.swing.JCheckBox reserveFromRich;
    javax.swing.JLabel reserveLabel;
    javax.swing.JCheckBox shieldWithoutBases;
    javax.swing.JCheckBox shipbuilding;
    javax.swing.JLabel sizeFactorLabel;
    javax.swing.JSpinner sizePct;
    javax.swing.JCheckBox spareXenophobes;
    javax.swing.JRadioButton stargateOff;
    javax.swing.JRadioButton stargateOn;
    javax.swing.ButtonGroup stargateOptions;
    javax.swing.JRadioButton stargateRich;
    javax.swing.JSpinner terraformEarly;
    javax.swing.JLabel terraformEarlyLabel;
    javax.swing.JSpinner transportMaxTurns;
    javax.swing.JCheckBox transportPoorDouble;
    javax.swing.JCheckBox transportRichDisabled;
    // End of variables declaration//GEN-END:variables

	// Just test the layout
	/* public static void main(String arg[]) {
		// initialize everything
		RotPUI.instance();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("GovernorOptions");
				frame.setDefaultCloseOperation(GovernorFrame.EXIT_ON_CLOSE);

				//Create and set up the content pane.
				GovernorOptionsPanel newContentPane = new GovernorOptionsPanel(frame);
				newContentPane.setOpaque(true); //content panes must be opaque
				frame.setContentPane(newContentPane);

				//Display the window.
				frame.pack();
				frame.setVisible(true);
			}
		});

	} */
	
	// ========== Nested Class ==========
	//
	private class TimeRefreshRunable implements Runnable {
		@Override public void run() {
	    	if (horlogeOngoing)
	    		return;
	    	horlogeOngoing	= true;
	    	if (govOptions().resetRequested() && frame.isFocused()) {
	    		if (!updateOngoing) {
		    		optionUpdate();
		    		govAnimate();
		    		// System.out.println("resetRequested() and executed (isFocused)");
	    		} else
	    			govAnimate();
	    		horlogeOngoing	= false;
	    		return;
	    	}
	    	if (govOptions().refreshRequested()) {
	    		if (!updateOngoing) {
		    		loadDisplayValues();
		    		loadValues();
					govOptions().clearRefresh();
		    		//System.out.println("refreshRequested() and executed");
	    		}
	    	} else if (frame.isFocused()) {
	    		govAnimate();
	    	}
	    	horlogeOngoing	= false;
	    }
	}

	// =============== Check Box and Radio Button ===============
	//
	private class ScalableCheckBoxAndRadioButtonIcon implements Icon {

		private ScalableCheckBoxAndRadioButtonIcon () {  }
		
		protected int dim() { return Math.round(iconSize); }
		@Override public void paintIcon(Component component, Graphics g0, int xi, int yi) {
			ButtonModel buttonModel = ((AbstractButton) component).getModel();
			Graphics2D g = (Graphics2D) g0;
			float y	= (float) (0.5 * (component.getSize().getHeight() - dim()));
			float x	= 2f;
			int corner = 0;
			int border = 1;
			int d2 = (int)(iconSize*0.8f);
			if (component instanceof JRadioButton) {
				corner = dim();
				d2 = (int)(iconSize*0.7f);
			}
			
			if (buttonModel.isRollover()) {
				g.setColor(hoverColor);
				border = 2;
			} else {
				g.setColor(borderColor);
			}
			g.fillRoundRect((int)x, (int)y, dim(), dim(), corner, corner);
			if (buttonModel.isPressed()) {
				g.setColor(Color.GRAY);
			} else {
				g.setColor(iconBgColor);
			}
			g.fillRoundRect(border + (int)x, (int)y + border, dim() - 2*border, dim() - 2*border, corner, corner);
			
			if (buttonModel.isSelected()) {
				Stroke prev = g.getStroke();
				g.setStroke(new BasicStroke(iconSize/5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.setColor(SystemPanel.whiteText);
				int x0 = (int)(x+iconSize/4);
				int y0 = (int)(3*iconSize/4+y);
				int d1 = (int)(iconSize*0.3f);
				g.drawLine(x0-d1, y0-d1, x0, y0);
				g.drawLine(x0, y0, x0+d2, y0-d2);
				g.setStroke(prev);
			}
		}
		@Override public int getIconWidth()  { return dim(); }
		@Override public int getIconHeight() { return dim(); }
	}
	// ==================== Buttons ====================
	//
	private class GovButtonUI extends MetalButtonUI {
		@Override  protected Color getDisabledTextColor() { return disabledColor; }
	    @Override  protected Color getSelectColor()		  { return hoverColor; }
	}
	// =================================================
	//
	private class GovButtonIcon implements Icon {

		private GovButtonIcon () {  }
		
		@Override public int getIconHeight() { return 2; }
		@Override public int getIconWidth()	 { return 2; }
		@Override public void paintIcon(Component component, Graphics g0, int xi, int yi) {
			Graphics2D g	= (Graphics2D) g0;
			JButton button	= (JButton) component;			
			ButtonModel buttonModel = button.getModel();
			Color borderC = borderColor;
			Color centerC = buttonColor;
			int corner = Math.round(buttonCorner);
			int border = 1;
			int x = 0;
			int y = 0;
			int w = button.getWidth();
			int h = button.getHeight();
			
			if (!buttonModel.isEnabled()) {
				borderC = disabledColor;
				centerC = hiddenColor;
			}
			else if (buttonModel.isRollover()) {
				borderC = hoverColor;
				border = 2;
			}
			// Fill background to go over OS choices... 
			g.setColor(frameBgColor);
			g.fillRect(x, y, w, h);
			
			// Fill the buttons
			g.setColor(centerC);
			g.fillRoundRect(x, y, w, h, corner, corner);
			
			// Draw borders
			Stroke prev = g.getStroke();
			g.setStroke(new BasicStroke(border, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.setColor(borderC);
			g.drawRoundRect(x + border, y + border, w - 2*border, h - 2*border, corner, corner);
			g.setStroke(prev);
		}
	}
	// ==================== Spinners ====================
	//
	private class GovernorJSpinner extends RotpJSpinner {
		private static final long serialVersionUID = 1L;
		@Override public	boolean isNewFormat()	{ return isNewFormat; }
		@Override public	int		arrowSize()		{ return arrowHeight; }
		@Override protected Color	borderColor()	{ return borderColor; }
		@Override protected Color	valueBgColor()	{ return valueBgColor; }
		@Override protected Color	disabledColor()	{ return disabledColor; }
		@Override protected Color	hiddenColor()	{ return hiddenColor; }
		@Override protected Color	hoverColor()	{ return hoverColor; }
		@Override protected Color	panelBgColor()	{ return panelBgColor; }
	}
}
