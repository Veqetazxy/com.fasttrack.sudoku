/*
  NakedPairSolver.java

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
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.solver.ISolver;
import com.jfasttrack.sudoku.step.AbstractStep;
import com.jfasttrack.sudoku.step.CandidateRemovalStep;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * This solver looks for naked pairs. A naked pair is a pair of <code>Cell</code>s in the same
 * <code>House</code> where both <code>Cell</code>s have the same two candidates (and only those
 * two candidates).
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class NakedPairSolver implements ISolver {

    /**
     * Looks for naked pairs in a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing a naked pair. <code>null</code> if the puzzle
     *                does not contain a naked pair.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        Iterator iterator1 = puzzle.getAllCells();
        while (iterator1.hasNext()) {
            Cell cell1 = (Cell) iterator1.next();
            if (cell1.getCandidates().cardinality() != 2) {
                continue;
            }

            Iterator iterator2 = puzzle.getBuddies(cell1).iterator();
            while (iterator2.hasNext()) {
                Cell cell2 = (Cell) iterator2.next();
                if (!cell1.getCandidates().equals(cell2.getCandidates())) {
                    continue;
                }

                AbstractStep step = createStep(puzzle, cell1, cell2);
                if (step != null) {
                    return step;
                }
            }
        }

        return null;
    }

    /**
     * Creates a <code>Step</code> describing a naked pair.
     *
     * @param puzzle  The puzzle being solved.
     * @param cell1   One <code>Cell</code> containing the naked pair.
     * @param cell2   The other <code>Cell</code> containing the naked pair.
     * @return        A <code>Step</code> describing a naked pair.
     */
    private static AbstractStep createStep(
            final AbstractPuzzleModel puzzle,
            final Cell cell1,
            final Cell cell2) {
        Set cellsToBeChanged = new HashSet(puzzle.getUnsolvedBuddies(cell1));
        cellsToBeChanged.retainAll(puzzle.getUnsolvedBuddies(cell2));
        if (cellsToBeChanged.isEmpty()) {
            return null;
        }

        BitSet candidates = cell1.getCandidates();
        int candidate1 = candidates.nextSetBit(0);
        int candidate2 = candidates.nextSetBit(candidate1 + 1);
        Iterator iterator = cellsToBeChanged.iterator();
        while (iterator.hasNext()) {
            Cell cell = (Cell) iterator.next();
            if (!cell.hasCandidate(candidate1)
                    && !cell.hasCandidate(candidate2)) {
                iterator.remove();
            }
        }
        if (cellsToBeChanged.isEmpty()) {
            return null;
        }

        MessageBundle messageBundle = MessageBundle.getInstance();
        String smallHint = messageBundle.getString("solver.naked.pair.small.hint");
        String bigHint = messageBundle.getString(
                "solver.naked.pair.big.hint",
                new String[] {
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate1)),
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate2)),
                }
        );

        CandidateRemovalStep step = new CandidateRemovalStep(
                smallHint, bigHint, cellsToBeChanged, 0);
        step.addExplainingCell(cell1);
        step.addExplainingCell(cell2);
        iterator = cellsToBeChanged.iterator();
        while (iterator.hasNext()) {
            step.addChangedCell((Cell) iterator.next());
        }

        return step;
    }

    /**
     * Gets the text for the menu item used to invoke this solver.
     *
     * @return  The text for the menu item used to invoke this solver.
     */
    public String getNameOfMenuItem() {
        return MessageBundle.getInstance().getString("solver.naked.pair.menu.item");
    }

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    public String getSolverNotApplicableMessage() {
        return MessageBundle.getInstance().getString("solver.naked.pair.not.applicable");
    }
}
