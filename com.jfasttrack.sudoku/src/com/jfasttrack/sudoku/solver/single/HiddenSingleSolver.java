/*
  HiddenSingleSolver.java

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
import com.jfasttrack.sudoku.puzzle.House;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.solver.ISolver;
import com.jfasttrack.sudoku.step.AbstractStep;
import com.jfasttrack.sudoku.step.ValuePlacementStep;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * This solver looks for a hidden single in a sudoku. A hidden single is a candidate value that
 * appears in only one unsolved <code>Cell</code> within a <code>House</code>.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class HiddenSingleSolver implements ISolver {

    /** The number of times each candidate appears in the current house. */
    private int[] candidateCount;

    /** The last <code>Cell</code> in which a candidate was seen. */
    private Cell[] lastCell;

    /**
     * Looks for a hidden single in a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing a hidden single. <code>null</code> if the
     *                puzzle does not contain a hidden single.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        AbstractStep step = null;

        Iterator iterator = puzzle.getAllHouses();
        while (iterator.hasNext()) {
            House house = (House) iterator.next();

            // Count the times each value is a candidate in this house.
            candidateCount = new int[puzzle.getGridSize() + 1];
            lastCell = new Cell[puzzle.getGridSize() + 1];

            countCandidates(puzzle, house);

            // If a value appears exactly once, it's a hidden single.
            step = getHiddenSingle(puzzle, house);
            if (step != null) {
                break;
            }
        }

        return step;
    }

    /**
     * Counts the number of times each candidate appears in a specified <code>House</code>.
     *
     * @param puzzle  The puzzle to be solved.
     * @param house   The <code>House</code> to be checked.
     */
    private void countCandidates(
            final AbstractPuzzleModel puzzle,
            final House house) {
        Iterator iterator = house.getUnsolvedCells();
        while (iterator.hasNext()) {
            Cell cell = (Cell) iterator.next();
            for (int value = 1; value <= puzzle.getGridSize(); value++) {
                if (cell.hasCandidate(value)) {
                    candidateCount[value]++;
                    lastCell[value] = cell;
                }
            }
        }
    }

    /**
     * Creates a <code>Step</code> describing a hidden single.
     *
     * @param puzzle  The puzzle to be solved.
     * @param house   The <code>House</code> to be checked.
     * @return        A <code>Step</code> describing a hidden single. <code>null</code> if no
     *                hidden single is found.
     */
    private AbstractStep getHiddenSingle(
            final AbstractPuzzleModel puzzle,
            final House house) {
        AbstractStep step = null;

        for (int value = 1; value <= puzzle.getGridSize(); value++) {
            if (candidateCount[value] != 1) {
                continue;
            }

            // We found a hidden single. Create a value placement step.
            MessageBundle messageBundle = MessageBundle.getInstance();
            String smallHint = messageBundle.getString("solver.single.hidden.small.hint");
            String bigHint = messageBundle.getString(
                    "solver.single.hidden.big.hint",
                    new String[] {
                        String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                        house.getName(),
                    }
            );
            Cell singleCell = lastCell[value];
            step = new ValuePlacementStep(smallHint, bigHint, singleCell, value);
            Iterator iterator = house.getUnsolvedCells();
            while (iterator.hasNext()) {
                Cell supportingCell = (Cell) iterator.next();
                if (supportingCell.containsValue()) {
                    continue;
                }
                if (supportingCell == singleCell) {
                    continue;
                }
                step.addExplainingCell(supportingCell);
            }

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
        return MessageBundle.getInstance().getString("solver.single.hidden.menu.item");
    }

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    public String getSolverNotApplicableMessage() {
        return MessageBundle.getInstance().getString("solver.single.hidden.not.applicable");
    }
}
