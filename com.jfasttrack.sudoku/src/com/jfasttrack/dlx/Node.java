/*
  Node.java

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
 * A <code>Node</code> represents a non-zero entry in a sparse matrix. It is used by Algorithm X
 * (dancing links) to solve an exact cover problem. Each node is a member of 2 circular doubly
 * linked lists: one for its row and one for its column.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class Node {

    /*
     * The fields here and in the ColumnHeader subclass were originally private. The getters
     * and setters proved to be too slow once I started generating larger sudoku, so I chose to
     * break encapsulation and make all fields public. It made puzzle generation much faster.
     */

    /** The left neighbor of this <code>Node</code>. */
    public Node left = this;

    /** The right neighbor of this <code>Node</code>. */
    public Node right = this;

    /** The upper neighbor of this <code>Node</code>. */
    public Node up = this;

    /** The lower neighbor of this <code>Node</code>. */
    public Node down = this;

    /** The header of the column in which this node appears. */
    public ColumnHeader columnHeader;

    /**
     * This number describes the move made when this node is removed from the dancing links matrix
     * and put into a solution to an exact cover problem. Its usage and value depend on the type of
     * problem being solved.
     */
    public int applicationData;
}
