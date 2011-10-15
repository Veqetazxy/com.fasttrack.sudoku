/*
  SolutionListener.java

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

import java.util.List;


/**
 * This interface is implemented by any class that is to be notified when a solution to an exact
 * cover problem is found.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public interface SolutionListener {

    /**
     * Reports the that a solution to an exact cover problem was found.
     *
     * @param solutionNodes  The dancing links nodes that make up this solution.
     * @return               <code>true</code> if solving has been completed. <code>false</code>
     *                       if the solver should continue generating solutions.
     */
    boolean solutionFound(List solutionNodes);
}
