/*
  RendererPanel.java

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

package com.jfasttrack.pentomino;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;


/**
 * This is the renderer that paints pentomino solutions.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
class RendererPanel extends JPanel {

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The amount of space to leave at the top and bottom of the drawing. */
    private static final int VERTICAL_INSET = 6;

    /** The size of each (small) square in a pentomino. */
    private static final int SQUARE_SIZE = 14;

    /** The colors used to paint pentominoes. */
    private static final Color[] COLORS = {
        Color.blue,
        Color.cyan,
        Color.darkGray,
        Color.gray,
        Color.green,
        Color.lightGray,
        Color.magenta,
        Color.orange,
        Color.pink,
        Color.red,
        Color.white,
        Color.yellow,
    };

    /** The pentomino grid to be painted. */
    private final int[][] pentominoGrid;

    /** The background color of this panel. */
    private final Color backgroundColor;

    /** The preferred size of this panel. */
    private final Dimension preferredSize;

    /** The preferred height of this panel. */
    private final int preferredHeight;

    /** The preferred width of this panel. */
    private final int preferredWidth;

    /**
     * Constructs a <code>RendererPanel</code>.
     *
     * @param pentominoGrid    The pentomino grid to be painted.
     * @param backgroundColor  The background color of this panel.
     */
    RendererPanel(final int[][] pentominoGrid, final Color backgroundColor) {
        this.pentominoGrid = pentominoGrid;
        this.backgroundColor = backgroundColor;

        preferredHeight = pentominoGrid.length * SQUARE_SIZE + VERTICAL_INSET * 2;
        preferredWidth = pentominoGrid[0].length * SQUARE_SIZE;

        preferredSize = new Dimension(preferredWidth, preferredHeight);
    }

    /**
     * Paints this <code>RendererPanel</code>.
     *
     * @param g  A graphics context.
     */
    public void paintComponent(final Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the horizontal lines.
        int x = (getWidth() - preferredWidth) / 2;
        int y = VERTICAL_INSET;
        g.setColor(Color.black);
        for (int r = 0; r <= pentominoGrid.length; r++) {
            g.drawLine(x, y, x + preferredWidth, y);
            y += SQUARE_SIZE;
        }

        // Draw the vertical lines.
        x = (getWidth() - preferredWidth) / 2;
        y = VERTICAL_INSET;
        for (int c = 0; c <= pentominoGrid[0].length; c++) {
            g.drawLine(x, y, x, y + preferredHeight - VERTICAL_INSET * 2);
            x += SQUARE_SIZE;
        }

        // Draw the pentominoes.
        y = VERTICAL_INSET + 1;
        for (int r = 0; r < pentominoGrid.length; r++) {
            x = (getWidth() - preferredWidth) / 2 + 1;
            for (int c = 0; c < pentominoGrid[0].length; c++) {
                int piece = pentominoGrid[r][c];
                g.setColor(COLORS[piece]);
                g.fillRect(x, y, SQUARE_SIZE - 1, SQUARE_SIZE - 1);
                x += SQUARE_SIZE;
            }
            y += SQUARE_SIZE;
        }
    }

    /**
     * Gets the preferred size of this <code>RendererPanel</code>.
     *
     * @return  The preferred size of this <code>RendererPanel</code>.
     */
    public Dimension getPreferredSize() {
        return preferredSize;
    }
}
