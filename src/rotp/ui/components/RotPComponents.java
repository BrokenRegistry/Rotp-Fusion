package rotp.ui.components;

import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.NORTHEAST;
import static java.awt.GridBagConstraints.NORTHWEST;
import static java.awt.GridBagConstraints.SOUTH;
import static java.awt.GridBagConstraints.SOUTHEAST;
import static java.awt.GridBagConstraints.SOUTHWEST;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import rotp.ui.ScaledInteger;
import rotp.ui.game.GameUI;
import rotp.util.Base;

public interface RotPComponents extends Base, ScaledInteger {
	Insets ZERO_INSETS	= new Insets(0, 0, 0, 0);

	default Color buttonBackgroundColor()	{ return GameUI.buttonBackgroundColor(); }
	default Color buttonTextColor()			{ return GameUI.borderBrightColor(); }
	default Color highlightColor()			{ return Color.YELLOW; }

	/**
	 * Creates a {@code GridBagConstraints} object with
	 * all of its fields set to the passed-in arguments.
	 *
	 * Note: Because the use of this constructor hinders readability
	 * of source code, this constructor should only be used by
	 * automatic source code generation tools.
	 *
	 * @param gridx     The initial gridx value (= Column).
	 * @param gridy     The initial gridy value (= Row).
	 * @param gridwidth The initial gridwidth value (= Number of Columns).
	 * @param gridheight The initial gridheight value (= Number of Rows).
	 * @param weightx   The initial weightx value (shifts rows to side of set anchor).
	 * @param weighty   The initial weighty value (shifts columns to side of set anchor).
	 * @param anchor    The initial anchor value (Can be based on NumPad key location).
	 * @param fill      The initial fill value (For resize purpose).
	 * @param insets    The initial insets value (= placement inside cell).
	 * @param ipadx     The initial ipadx value (= to be added to the width of object).
	 * @param ipady     The initial ipady value (= to be added to the height of object)..
	 *
	 * @see java.awt.GridBagConstraints#gridx
	 * @see java.awt.GridBagConstraints#gridy
	 * @see java.awt.GridBagConstraints#gridwidth
	 * @see java.awt.GridBagConstraints#gridheight
	 * @see java.awt.GridBagConstraints#weightx
	 * @see java.awt.GridBagConstraints#weighty
	 * @see java.awt.GridBagConstraints#anchor
	 * @see java.awt.GridBagConstraints#fill
	 * @see java.awt.GridBagConstraints#insets
	 * @see java.awt.GridBagConstraints#ipadx
	 * @see java.awt.GridBagConstraints#ipady
	 */
	default GridBagConstraints newGbc(int gridx, int gridy,
									int gridwidth, int gridheight,
									double weightx, double weighty,
									int anchor, int fill,
									Insets insets, int ipadx, int ipady) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx		= gridx;		// column
		gbc.gridy		= gridy;		// row
		gbc.gridwidth	= gridwidth;	// number of columns
		gbc.gridheight	= gridheight;	// number of rows
		gbcAnchor(gbc, anchor);			// Location inside the cell
		gbc.fill		= fill;			// For resize purpose
		gbc.ipadx		= ipadx;		// width of object
		gbc.ipady		= ipady;		// height of object
		gbc.insets		= insets;		// placement inside cell
		gbc.weightx		= weightx;		// shifts rows to side of set anchor
		gbc.weighty		= weighty;		// shifts columns to side of set anchor
		return gbc;
	}
	/**
	 * Change some elements of {@code GridBagConstraints} 
	 *
	 * @param gbc       The initial {@code GridBagConstraints} object, it will be modified.
	 * @param gridx     The initial gridx value (= Column).
	 * @param gridy     The initial gridy value (= Row).
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcMove(GridBagConstraints gbc, int dx, int dy) {
		gbc.gridx += dx;	// column
		gbc.gridy += dy;	// row
		return gbc;
	}
	/**
	 * Change some elements of {@code GridBagConstraints}
	 *
	 * @param gbc       The initial {@code GridBagConstraints} object, it will be modified.
	 * @param gridwidth The initial gridwidth value (= Number of Columns).
	 * @param gridheight The initial gridheight value (= Number of Rows).
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcSize (GridBagConstraints gbc, int gridwidth, int gridheight) {
		gbc.gridwidth	= gridwidth;	// number of columns
		gbc.gridheight	= gridheight;	// number of rows
		return gbc;
	}
	/**
	 * Change some elements of {@code GridBagConstraints}
	 *
	 * @param gbc       The initial {@code GridBagConstraints} object., it will be modified.
	 * @param weightx   The initial weightx value (shifts rows to side of set anchor).
	 * @param weighty   The initial weighty value (shifts columns to side of set anchor).
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcWeight (GridBagConstraints gbc, double weightx, double weighty) {
		gbc.weightx		= weightx;		// shifts rows to side of set anchor
		gbc.weighty		= weighty;		// shifts columns to side of set anchor
		return gbc;
	}
	/**
	 * Change some elements of {@code GridBagConstraints}
	 *
	 * @param gbc       The initial {@code GridBagConstraints} object., it will be modified.
	 * @param ipadx     The initial ipadx value (= to be added to the width of object).
	 * @param ipady     The initial ipady value (= to be added to the height of object).
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcPad (GridBagConstraints gbc, int ipadx, int ipady) {
		gbc.ipadx		= ipadx;		// width of object
		gbc.ipady		= ipady;		// height of object
		return gbc;
	}
	/**
	 * Change the anchor of {@code GridBagConstraints} 
	 *
	 * @param gbc    The initial {@code GridBagConstraints} object, it will be modified.
	 * @param anchor The initial anchor value (Can be based on NumPad key location).
	 * @return the interpreted anchor value.
	 * @return the modified {@code GridBagConstraints}
	 */
	default GridBagConstraints gbcAnchor(GridBagConstraints gbc, int anchor) {
		if (anchor == 0) {
			System.out.println("GridBagConstraints gbcAnchor(GridBagConstraints gbc, int anchor = 0)");
		}
		switch (anchor) {
			// Based on numPad
			case 1: gbc.anchor = SOUTHWEST;	break;
			case 2: gbc.anchor = SOUTH;		break;
			case 3: gbc.anchor = SOUTHEAST;	break;
			case 4: gbc.anchor = WEST;		break;
			case 5: gbc.anchor = CENTER;	break;
			case 6: gbc.anchor = EAST;		break;
			case 7: gbc.anchor = NORTHWEST;	break;
			case 8: gbc.anchor = NORTH;		break;
			case 9: gbc.anchor = NORTHEAST;	break;
			// Based on original constant
			default: gbc.anchor = anchor;
		}
		return gbc;
	}

}
