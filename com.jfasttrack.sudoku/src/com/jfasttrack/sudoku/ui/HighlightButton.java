/*
  HighlightButton.java

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

package com.jfasttrack.sudoku.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JToggleButton;


/**
 * A <code>HighlightButton</code> is a toggle button used to select a candidate value. When
 * selected, each unsolved <code>Cell</code> with the specified candidate value is highlighted
 * (displayed in a different color).
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class HighlightButton extends JToggleButton {

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>HighlightButton</code>.
     *
     * @param label  The text to be displayed inside this <code>HighlightButton</code>.
     */
    public HighlightButton(final String label) {
        super(" " + label + " ");

        setFont(new Font("Dialog", Font.BOLD, 16));
        setFocusable(false);
    }

    /**
     * Gets the <code>Insets</code> of this button.
     *
     * @return  The <code>Insets</code> of this button.
     */
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    /**
     * Gets the preferred size of this <code>HighlightButton</code>.
     *
     * @return  The preferred size of this <code>HighlightButton</code>.
     */
    public Dimension getPreferredSize() {

        // This is here because some buttons are not quite big enough under Ubuntu Linux.
        Dimension originalPreferredSize = super.getPreferredSize();
        return new Dimension(originalPreferredSize.width + 2, originalPreferredSize.height);
    }
}
