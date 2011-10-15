/*
  HiddenTripletSolver.java

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
 * This solver looks for hidden triplets. A hidden triplet is 3 <code>Cell</code>s in the same
 * <code>House</code> that have the same 3 candidates (or a subset of those 3 candidates), and no
 * other <code>Cell</code>s in that <code>House</code> have those candidates.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class HiddenTripletSolver implements ISolver {

    /**
     * Looks for hidden triplets in a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing a hidden triplet. <code>null</code> if the
     *                puzzle does not contain a hidden triplet.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        AbstractStep step = null;
        int size = puzzle.getGridSize();

        Iterator houses = puzzle.getAllHouses();
        while (houses.hasNext()) {
            House house = (House) houses.next();
            if (house.getNumberOfUnsolvedCells() <= 4) {
                continue;
            }

            step = lookForTripletInHouse(house, size);
            if (step != null) {
                break;
            }
        }

        return step;
    }

    /**
     * Checks a <code>House</code> to see whether it contains a hidden triplet.
     *
     * @param house  The <code>House</code> to be checked.
     * @param size   The size of the puzzle to be solved.
     * @return       A <code>Step</code> describing a hidden triplet. <code>null</code> if the
     *               <code>House</code> does not contain a hidden triplet.
     */
    private static AbstractStep lookForTripletInHouse(final House house, final int size) {
        AbstractStep step = null;

        // See how many times each number is a candidate in this house.
        int[] candidateCounts = new int[size + 1];
        Iterator cells = house.getUnsolvedCells();
        while (cells.hasNext()) {
            Cell cell = (Cell) cells.next();
            for (int value = 1; value <= size; value++) {
                if (cell.hasCandidate(value)) {
                    candidateCounts[value]++;
                }
            }
        }

        // For each possible trio of candidates that appears up to 3 times in this house.
    outerLoop:
        for (int candidate1 = 1; candidate1 <= size - 2; candidate1++) {
            if (candidateCounts[candidate1] < 1 || candidateCounts[candidate1] > 3) {
                continue;
            }
            for (int candidate2 = candidate1 + 1; candidate2 <= size - 1; candidate2++) {
                if (candidateCounts[candidate2] < 1 || candidateCounts[candidate2] > 3) {
                    continue;
                }
                for (int candidate3 = candidate2 + 1; candidate3 <= size; candidate3++) {
                    if (candidateCounts[candidate3] < 1 || candidateCounts[candidate3] > 3) {
                        continue;
                    }

                    // We have 3 cells with 3 candidates. Is it a triplet?
                    step = checkHouseForTriplet(house, candidate1, candidate2, candidate3);
                    if (step != null) {
                        break outerLoop;
                    }
                }
            }
        }

        return step;
    }

    /**
     * Given a <code>House</code> and a trio of candidates that appear up to 3 times in that
     * <code>House</code>, checks to see whether they occupy the same cells. If they do, marks all
     * other candidates in those cells for removal.
     *
     * @param house       The <code>House</code> to be checked.
     * @param candidate1  The value of the first candidate.
     * @param candidate2  The value of the second candidate.
     * @param candidate3  The value of the third candidate.
     * @return            A <code>Step</code> describing a hidden triplet. <code>null</code> if the
     *                    puzzle does not contain a hidden triplet.
     */
    private static AbstractStep checkHouseForTriplet(
            final House house,
            final int candidate1,
            final int candidate2,
            final int candidate3) {
        AbstractStep step;
        Set cellsToBeChanged = new HashSet();

        // We have 3 candidates. Each appears up to 3 times in this house. Do they appear in the
        // same cells? If so, make a note of which cells.
        Iterator cellsInHouse = house.getUnsolvedCells();
        while (cellsInHouse.hasNext()) {
            Cell cell = (Cell) cellsInHouse.next();
            if (cell.hasCandidate(candidate1)
                    || cell.hasCandidate(candidate2)
                    || cell.hasCandidate(candidate3)) {
                cellsToBeChanged.add(cell);
            }
        }

        if (cellsToBeChanged.size() != 3) {
            return null;    // This is not a hidden triplet.
        }

        // Is the triplet useful? Does it have candidates not in the triplet?
        Iterator cellsInTriplet = cellsToBeChanged.iterator();
        boolean useful = false;
        while (cellsInTriplet.hasNext()) {
            Cell cell = (Cell) cellsInTriplet.next();
            BitSet candidates = cell.getCandidates();
            candidates.set(candidate1, false);
            candidates.set(candidate2, false);
            candidates.set(candidate3, false);
            if (candidates.cardinality() > 0) {
                useful = true;
                break;
            }
        }
        if (!useful) {
            return null;
        }

        // We found a useful hidden triplet.
        MessageBundle messageBundle = MessageBundle.getInstance();
        String smallHint = messageBundle.getString("solver.hidden.triplet.small.hint");
        String bigHint = messageBundle.getString(
                "solver.hidden.triplet.big.hint",
                new String[] {
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate1)),
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate2)),
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate3)),
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
        return MessageBundle.getInstance().getString("solver.hidden.triplet.menu.item");
    }

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    public String getSolverNotApplicableMessage() {
        return MessageBundle.getInstance().getString("solver.hidden.triplet.not.applicable");
    }
}
