package rotp.ui.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import org.knowm.xchart.style.PieStyler.LabelType;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.colors.ChartColor;
import org.knowm.xchart.style.colors.MatlabSeriesColors;
import org.knowm.xchart.style.lines.MatlabSeriesLines;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.MatlabSeriesMarkers;
import org.knowm.xchart.style.theme.Theme;

import rotp.util.Base;

public class XChartRotpTheme implements Theme, Base	{
	private static final Color TRANSPARENT_BG = new Color(0, 0, 0, 0);
	private final BasicStroke baseStroke = new BasicStroke(scaled(1));
	
	// Chart Style ///////////////////////////////
	@Override public Font getBaseFont()						{ return plainFont(10); }
	@Override public Color getChartBackgroundColor()		{ return TRANSPARENT_BG; }
	@Override public Color getChartFontColor()				{ return ChartColor.BLACK.getColor(); }
	@Override public int getChartPadding()					{ return scaled(10); }

	// Chart Title ///////////////////////////////
	@Override public Font getChartTitleFont()				{ return narrowFont(13); }
	@Override public boolean isChartTitleVisible()			{ return true; }
	@Override public boolean isChartTitleBoxVisible()		{ return false; }
	@Override public Color getChartTitleBoxBackgroundColor(){ return ChartColor.WHITE.getColor(); }
	@Override public Color getChartTitleBoxBorderColor()	{ return ChartColor.WHITE.getColor(); }
	@Override public int getChartTitlePadding()				{ return scaled(4); }

	// Chart Legend ///////////////////////////////
	@Override public Font getLegendFont()					{ return plainFont(10); }
	@Override public boolean isLegendVisible()				{ return true; }
	@Override public Color getLegendBackgroundColor()		{ return TRANSPARENT_BG; }
	@Override public Color getLegendBorderColor()			{ return ChartColor.BLACK.getColor(); }
	@Override public int getLegendPadding()					{ return scaled(4); }
	@Override public int getLegendSeriesLineLength()		{ return scaled(12); }
	@Override public LegendPosition getLegendPosition()		{ return LegendPosition.OutsideE; }

	// Chart Plot Area ///////////////////////////////
	@Override public boolean isPlotGridLinesVisible()		{ return true; }
	@Override public boolean isPlotGridVerticalLinesVisible()	{ return true; }
	@Override public boolean isPlotGridHorizontalLinesVisible()	{ return true; }
	@Override public Color getPlotBackgroundColor()			{ return TRANSPARENT_BG; }
	@Override public Color getPlotBorderColor()				{ return ChartColor.BLACK.getColor(); }
	@Override public boolean isPlotBorderVisible()			{ return true; }
	@Override public Color getPlotGridLinesColor()			{ return ChartColor.BLACK.getColor(); }
	@Override public BasicStroke getPlotGridLinesStroke()	{
		return new BasicStroke(
				.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {1f, 3.0f}, 0.0f);
	}
	@Override public boolean isPlotTicksMarksVisible()		{ return true; }
	@Override public double getPlotContentSize()			{ return .96; }
	@Override public int getPlotMargin()					{ return scaled(3); }

	// Chart Annotations ///////////////////////////////
	@Override public Font getAnnotationTextPanelFont()		{ return narrowFont(10); }
	@Override public Color getAnnotationTextPanelFontColor()		{ return ChartColor.BLACK.getColor(); }
	@Override public Color getAnnotationTextPanelBackgroundColor()	{ return ChartColor.WHITE.getColor(); }
	@Override public Color getAnnotationTextPanelBorderColor()		{ return ChartColor.DARK_GREY.getColor(); }
	@Override public int getAnnotationTextPanelPadding()	{ return scaled(10); }
	@Override public Font getAnnotationTextFont()			{ return narrowFont(10); }
	@Override public Color getAnnotationTextFontColor()		{ return ChartColor.BLACK.getColor(); }
	@Override public BasicStroke getAnnotationLineStroke()	{ return baseStroke; }
	@Override public Color getAnnotationLineColor()			{ return ChartColor.DARK_GREY.getColor(); }

	// Chart Button ///////////////////////////////
	@Override public Color getChartButtonBackgroundColor()	{ return ChartColor.BLUE.getColor().brighter(); }
	@Override public Color getChartButtonBorderColor()		{ return ChartColor.BLUE.getColor().darker(); }
	@Override public Color getChartButtonHoverColor()		{ return ChartColor.BLUE.getColor(); }
	@Override public Color getChartButtonFontColor()		{ return getChartFontColor(); }
	@Override public Font getChartButtonFont()				{ return getLegendFont(); }
	@Override public int getChartButtonMargin()				{ return scaled(6); }

	// Tool Tips ///////////////////////////////
	@Override public boolean isToolTipsEnabled()			{ return false; }
	@Override public Styler.ToolTipType getToolTipType()	{ return Styler.ToolTipType.xAndYLabels; }
	@Override public Font getToolTipFont()					{ return getBaseFont(); }
	@Override public Color getToolTipBackgroundColor()		{ return ChartColor.WHITE.getColor(); }
	@Override public Color getToolTipBorderColor()			{ return ChartColor.DARK_GREY.getColor(); }
	@Override public Color getToolTipHighlightColor()		{ return ChartColor.LIGHT_GREY.getColor(); }

	// Chart Axes ///////////////////////////////
	@Override public boolean isXAxisTitleVisible()			{ return true; }
	@Override public boolean isYAxisTitleVisible()			{ return true; }
	@Override public Font getAxisTitleFont()				{ return narrowFont(11); }
	@Override public boolean isXAxisTicksVisible()			{ return true; }
	@Override public boolean isYAxisTicksVisible()			{ return true; }
	@Override public Font getAxisTickLabelsFont()			{ return narrowFont(10); }
	@Override public int getAxisTickMarkLength()			{ return scaled(4); }
	@Override public int getAxisTickPadding()				{ return scaled(2); }
	@Override public Color getAxisTickMarksColor()			{ return ChartColor.BLACK.getColor(); }
	@Override public BasicStroke getAxisTickMarksStroke()	{ return baseStroke; }
	@Override public Color getAxisTickLabelsColor()			{ return ChartColor.BLACK.getColor(); }
	@Override public boolean isAxisTicksLineVisible()		{ return false; }
	@Override public boolean isAxisTicksMarksVisible()		{ return false; }
	@Override public int getAxisTitlePadding()				{ return scaled(5); }
	@Override public int getXAxisTickMarkSpacingHint()		{ return scaled(35); }
	@Override public int getYAxisTickMarkSpacingHint()		{ return scaled(20); }

	// Cursor ///////////////////////////////
	@Override public boolean isCursorEnabled()				{ return false; }
	@Override public Color getCursorColor()					{ return Color.BLACK; }
	@Override public float getCursorSize()					{ return 1; }
	@Override public Font getCursorFont()					{ return plainFont(16); }
	@Override public Color getCursorFontColor()				{ return Color.WHITE; }
	@Override public Color getCursorBackgroundColor()		{ return Color.GRAY; }

	// Zoom /////////////////////////////////////
	@Override public boolean isZoomEnabled()				{ return false; }

	// Bar Charts ///////////////////////////////
	@Override public double getAvailableSpaceFill()			{ return .9; }
	@Override public boolean isOverlapped()					{ return false; }

	// Pie Charts ///////////////////////////////
	@Override public boolean isCircular()					{ return true; }
	@Override public double getStartAngleInDegrees()		{ return 0; }
	@Override public Font getPieFont()						{ return plainFont(15); }
	@Override public double getLabelsDistance()				{ return .67; }
	@Override public LabelType getLabelType()				{ return LabelType.Name; }
	@Override public boolean setForceAllLabelsVisible()		{ return false; }
	@Override public double getDonutThickness()				{ return .33; }
	@Override public boolean isSumVisible()					{ return false; }
	@Override public Font getSumFont()						{ return plainFont(15); }

	// Line, Scatter, Area Charts ///////////////////////////////
	@Override public int getMarkerSize()					{ return scaled(8); }

	// Error Bars ///////////////////////////////
	@Override public Color getErrorBarsColor()				{ return ChartColor.BLACK.getColor(); }
	@Override public boolean isErrorBarsColorSeriesColor()	{ return false; }

	// Labels (pie charts, bar charts)
	@Override public Color getLabelsFontColorAutomaticDark()	{ return Color.BLACK; }
	@Override public Color getLabelsFontColorAutomaticLight()	{ return Color.WHITE; }
	@Override public boolean isLabelsFontColorAutomaticEnabled()	{ return true; }

	// SeriesMarkers, SeriesLines, SeriesColors ///////////////////////////////
	@Override public Color[] getSeriesColors()				{ return new MatlabSeriesColors().getSeriesColors(); }
	@Override public Marker[] getSeriesMarkers()			{ return new MatlabSeriesMarkers().getSeriesMarkers(); }
	@Override public BasicStroke[] getSeriesLines()			{ return new MatlabSeriesLines().getSeriesLines(); }
}