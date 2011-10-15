/*
  NakedSingleSolver.java

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

package com.jfasttrack.sudoku.solver.single;

import java.util.Iterator;

import com.jfasttrack.sudoku.puzzle.AbstractPuzzleModel;
import com.jfasttrack.sudoku.puzzle.Cell;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.solver.ISolver;
import com.jfasttrack.sudoku.step.AbstractStep;
import com.jfasttrack.sudoku.step.ValuePlacementStep;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * This solver looks for a naked single in a sudoku. A naked single is an unsolved <code>Cell</code>
 * that has exactly one candidate.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class NakedSingleSolver implements ISolver {

    /**
     * Looks for a naked single in a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing a naked single. <code>null</code> if the
     *                puzzle does not contain a naked single.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        ValuePlacementStep step = null;

        Iterator iterator = puzzle.getAllCells();
        while (iterator.hasNext()) {
            Cell cell = (Cell) iterator.next();
            if (cell.containsValue()) {
                continue;
            }
            if (cell.getCandidates().cardinality() != 1) {
                continue;
            }

            // We found a naked single. Create a value placement step.
            MessageBundle messageBundle = MessageBundle.getInstance();
            int value = cell.getCandidates().nextSetBit(0);
            String smallHint = messageBundle.getString("solver.single.naked.small.hint");
            String bigHint = messageBundle.getString(
                    "solver.single.naked.big.hint",
                    new String[] {
                        String.valueOf(cell.getRow() + 1),
                        String.valueOf(cell.getColumn() + 1),
                        String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                    }
            );
            step = new ValuePlacementStep(smallHint, bigHint, cell, value);

            break;
        }

        return step;
    }

    /**
     * Gets the text for the menu item used to invoke this solver.
     *
     * @return  The text for the menu item used to invoke this solver.
     */
    public String getNameOfMenuItem() {
        return MessageBundle.getInstance().getString("solver.single.naked.menu.item");
    }

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    public String getSolverNotApplicableMessage() {
        return MessageBundle.getInstance().getString("solver.single.naked.not.applicable");
    }
}
