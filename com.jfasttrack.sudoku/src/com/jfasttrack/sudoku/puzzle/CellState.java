/*
  CellState.java

  Copyright (C) 2008-2009 by Pete Boton, www.jfasttrack.com

  This file is part of Dancing Links Sudoku.

  Dancing Links Sudoku is free for non-commercial use. Contact the author for commercial use.

  You can redistribute and/or modify this software only under the terms of the GNU General Public
  License as published by the Free Software Foundation. Version 2 of the License or (at your option)
  any later version may be used.

  This program is distributed in the hope that it will be useful and enjoyable, but WITH NO
  WARRANTY; not even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this program; if not,
  write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307.
*/

package com.jfasttrack.sudoku.puzzle;

import java.awt.Color;


/**
 * <code>CellState</code> tells whether a <code>Cell</code> is a given, is unsolved, or solved.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public final class CellState {

    /** Indicates that a <code>Cell</code> is not yet part of a block. */
    public static final CellState UNASSIGNED = new CellState(new Color(255, 200, 200));

    /** Indicates that this <code>Cell</code> contains a value in the original puzzle. */
    public static final CellState GIVEN = new CellState(new Color(192, 192, 255));

    /** Indicates that this <code>Cell</code> does not yet contain a value. */
    public static final CellState UNSOLVED = new CellState(new Color(224, 224, 255));

    /*** Indicates that a value for this <code>Cell</code> was provided during solving. */
    public static final CellState SOLVED = new CellState(new Color(224, 224, 255));

    /** The color used to paint the backgrounds of <code>Cell</code>s in this state. */
    private final Color color;

    /**
     * Constructs a <code>CellState</code>.
     *
     * @param color  The color used to paint the backgrounds of <code>Cell</code>s in this state.
     */
    private CellState(final Color color) {
        this.color = color;
    }

    /**
     * Gets the color used to paint the backgrounds of <code>Cell</code>s in this state.
     *
     * @return  The color used to paint the backgrounds of <code>Cell</code>s in this state.
     */
    public Color getColor() {
        return color;
    }
}
