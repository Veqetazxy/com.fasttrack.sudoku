/*
  HiddenPairSolver.java

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

package com.jfasttrack.sudoku.solver.subset;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.jfasttrack.sudoku.puzzle.AbstractPuzzleModel;
import com.jfasttrack.sudoku.puzzle.Cell;
import com.jfasttrack.sudoku.puzzle.House;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.solver.ISolver;
import com.jfasttrack.sudoku.step.AbstractStep;
import com.jfasttrack.sudoku.step.CandidateRemovalStep;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * This solver looks for hidden pairs. A hidden pair is 2 <code>Cell</code>s in the same
 * <code>House</code> where both <code>Cell</code>s have the same 2 candidates, and no other
 * <code>Cell</code>s in that <code>House</code> have those candidates.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class HiddenPairSolver implements ISolver {

    /**
     * Looks for hidden pairs in a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing a hidden pair. <code>null</code> if the puzzle
     *                does not contain a hidden pair.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        AbstractStep step = null;
        int gridSize = puzzle.getGridSize();

        Iterator houses = puzzle.getAllHouses();
        while (houses.hasNext()) {
            House house = (House) houses.next();
            if (house.getNumberOfUnsolvedCells() <= 3) {
                continue;
            }

            step = lookForPairInHouse(house, gridSize);
            if (step != null) {
                break;
            }
        }

        return step;
    }

    /**
     * Checks a <code>House</code> to see whether it contains a hidden pair.
     *
     * @param house      The <code>House</code> to be checked.
     * @param gridSize   The size of the puzzle to be solved.
     * @return           A <code>Step</code> describing a hidden pair. <code>null</code> if the
     *                   <code>House</code> does not contain a hidden pair.
     */
    private static AbstractStep lookForPairInHouse(final House house, final int gridSize) {
        AbstractStep step = null;

        // See how many times each number is a candidate in this house.
        int[] candidateCounts = new int[gridSize + 1];
        Iterator cells = house.getUnsolvedCells();
        while (cells.hasNext()) {
            Cell cell = (Cell) cells.next();
            for (int value = 1; value <= gridSize; value++) {
                if (cell.hasCandidate(value)) {
                    candidateCounts[value]++;
                }
            }
        }

        // For each possible pair of candidates that appears twice in this house
    outerLoop:
        for (int candidate1 = 1; candidate1 < gridSize; candidate1++) {
            if (candidateCounts[candidate1] < 1 || candidateCounts[candidate1] > 2) {
                continue;
            }
            for (int candidate2 = candidate1 + 1; candidate2 <= gridSize; candidate2++) {
                if (candidateCounts[candidate2] < 1 || candidateCounts[candidate2] > 2) {
                    continue;
                }

                // We have 2 cells with 2 candidates. Is it a pair?
                step = checkHouseForPair(house, candidate1, candidate2);
                if (step != null) {
                    break outerLoop;
                }
            }
        }

        return step;
    }

    /**
     * Given a <code>House</code> and two candidates that appear twice in that <code>House</code>,
     * checks to see whether they occupy the same cells. If they do, marks all other candidates in
     * those cells for removal.
     *
     * @param house       The <code>House</code> to be checked.
     * @param candidate1  The value of the first candidate.
     * @param candidate2  The value of the second candidate.
     * @return            A <code>Step</code> describing a hidden pair. <code>null</code> if the
     *                    puzzle does not contain a hidden pair.
     */
    private static AbstractStep checkHouseForPair(
            final House house,
            final int candidate1,
            final int candidate2) {
        AbstractStep step = null;
        Set cellsToBeChanged = new HashSet();

        // We have 2 candidates. Each appears twice in this house. Do they appear in the same cells?
        // If so, make a note of which cells.
        Iterator cellsInHouse = house.getUnsolvedCells();
        while (cellsInHouse.hasNext()) {
            Cell cell = (Cell) cellsInHouse.next();
            if (cell.hasCandidate(candidate1) && cell.hasCandidate(candidate2)) {
                cellsToBeChanged.add(cell);
            }
        }

        if (cellsToBeChanged.size() != 2) {
            return step;    // This is not a hidden pair.
        }

        // Is the pair useful? Does it have candidates not in the pair?
        Iterator cellsInPair = cellsToBeChanged.iterator();
        boolean useful = false;
        while (cellsInPair.hasNext()) {
            Cell cell = (Cell) cellsInPair.next();
            BitSet candidates = cell.getCandidates();
            candidates.set(candidate1, false);
            candidates.set(candidate2, false);
            if (candidates.cardinality() > 0) {
                useful = true;
                break;
            }
        }
        if (!useful) {
            return step;
        }

        // We found a useful hidden pair.
        MessageBundle messageBundle = MessageBundle.getInstance();
        String smallHint = messageBundle.getString("solver.hidden.pair.small.hint");
        String bigHint = messageBundle.getString(
                "solver.hidden.pair.big.hint",
                new String[] {
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate1)),
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate2)),
                }
        );

        step = new CandidateRemovalStep(smallHint, bigHint, cellsToBeChanged, 0);

        cellsInHouse = house.getUnsolvedCells();
        while (cellsInHouse.hasNext()) {
            Cell cell = (Cell) cellsInHouse.next();
            if (cellsToBeChanged.contains(cell)) {
                continue;
            }
            step.addExplainingCell(cell);
        }

        return step;
    }

    /**
     * Gets the text for the menu item used to invoke this solver.
     *
     * @return  The text for the menu item used to invoke this solver.
     */
    public String getNameOfMenuItem() {
        return MessageBundle.getInstance().getString("solver.hidden.pair.menu.item");
    }

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    public String getSolverNotApplicableMessage() {
        return MessageBundle.getInstance().getString("solver.hidden.pair.not.applicable");
    }
}
