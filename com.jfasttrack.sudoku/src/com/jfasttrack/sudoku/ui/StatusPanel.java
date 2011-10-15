/*
  StatusPanel.java

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

import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;


/**
 * The <code>StatusPanel</code> is a panel containing a text field. It displays various messages
 * for the user.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class StatusPanel extends JPanel {

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Component used to display messages. */
    private final JLabel statusLabel = new JLabel("");

    /** Constructs a <code>StatusPanel</code>. */
    public StatusPanel() {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        add(statusLabel);
        setText("Welcome to Dancing Links Sudoku!");
    }

    /**
     * Sets the text in the status label.
     *
     * @param text  The text to be displayed.
     */
    final void setText(final String text) {
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setText(" " + text);
    }

    /** Clears the text in the status label. */
    public void clear() {
        statusLabel.setText(" ");
    }
}
