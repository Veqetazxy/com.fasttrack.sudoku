/*
  ColumnHeader.java

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

package com.jfasttrack.dlx;



/**
 * A <code>ColumnHeader</code> is a <code>Node</code> that appears at the top of each column in a
 * dancing links matrix.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class ColumnHeader extends Node {

    /** The number of nodes in this column. */
    public int columnLength;

    /*
     * Knuth's column header also has a field called "name" that he uses when showing the
     * solution(s). Name has been omitted here because this implementation has no use for it.
     */

    /** Constructs a <code>ColumnHeader</code>. */
    public ColumnHeader() {
        this.applicationData = -1;
        columnHeader = this;
    }

    /**
     * Adds a <code>Node</code> to the end of this column.
     *
     * @param node  The <code>Node</code> to be added.
     */
    public void append(final Node node) {
        node.columnHeader = this;
        node.down = this;
        node.up = this.up;
        up.down = node;
        up = node;

        columnLength++;
    }
}
