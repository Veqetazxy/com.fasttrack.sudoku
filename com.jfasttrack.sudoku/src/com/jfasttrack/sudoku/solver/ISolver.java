/*
  ISolver.java

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

package com.jfasttrack.sudoku.solver;

import com.jfasttrack.sudoku.puzzle.AbstractPuzzleModel;
import com.jfasttrack.sudoku.step.AbstractStep;


/**
 * The <code>ISolver</code> interface is implemented by classes that solve sudoku.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public interface ISolver {

    /**
     * Gets a <code>Step</code> toward the solution of a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> toward the solution of a sudoku. <code>null</code> if
     *                this solver cannot be applied to this puzzle.
     */
    AbstractStep getNextStep(AbstractPuzzleModel puzzle);

    /**
     * Gets the string used to name this solver in the menu.
     *
     * @return  The string used to name this solver in the menu.
     */
    String getNameOfMenuItem();

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    String getSolverNotApplicableMessage();
}
