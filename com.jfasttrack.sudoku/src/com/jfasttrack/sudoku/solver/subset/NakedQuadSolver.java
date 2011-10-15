/*
  NakedQuadSolver.java

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
 * This solver looks for naked quads. A naked quad is a collection of 4 <code>Cell</code>s in the
 * same <code>House</code> that collectively contain exactly 4 candidate values.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class NakedQuadSolver implements ISolver {

    /**
     * Looks for naked quads in a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing a naked quad. <code>null</code> if the puzzle
     *                does not contain a naked quad.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        AbstractStep step = null;

        Iterator allHouses = puzzle.getAllHouses();
        while (allHouses.hasNext()) {
            House house = (House) allHouses.next();
            int numberOfUnsolvedCells = house.getNumberOfUnsolvedCells();
            if (numberOfUnsolvedCells <= 4) {
                continue;
            }

            // Create an array containing the unsolved cells.
            Cell[] unsolvedCells = new Cell[numberOfUnsolvedCells];
            Iterator iterator2 = house.getUnsolvedCells();
            for (int i = 0; i < numberOfUnsolvedCells; i++) {
                Cell nextCell = (Cell) iterator2.next();
                unsolvedCells[i] = nextCell;
            }

            step = findQuadInHouse(puzzle, unsolvedCells, numberOfUnsolvedCells);
            if (step != null) {
                break;
            }
        }

        return step;
    }

    /**
     * Checks the unsolved <code>Cell</code>s of a <code>House</code> to see whether it contains a
     * naked quad.
     *
     * @param puzzle                 The puzzle being solved.
     * @param unsolvedCells          A collection of unsolved <code>Cell</code>s that belong to a
     *                               <code>House</code>.
     * @param numberOfUnsolvedCells  The number of unsolved <code>Cell</code>s in the
     *                               <code>House</code> being considered.
     * @return                       A <code>Step</code> describing a naked quad. <code>null</code>
     *                               if this collection of unsolved <code>Cell</code>s does not
     *                               contain a naked quad.
     */
    private static AbstractStep findQuadInHouse(
            final AbstractPuzzleModel puzzle,
            final Cell[] unsolvedCells,
            final int numberOfUnsolvedCells) {
        AbstractStep step = null;

    outerLoop:
        for (int i1 = 0; i1 < numberOfUnsolvedCells - 3; i1++) {
            Cell cell1 = unsolvedCells[i1];
            if (cell1.getCandidates().cardinality() > 4) {
                continue;
            }
            BitSet candidates1 = cell1.getCandidates();

            for (int i2 = i1 + 1; i2 < numberOfUnsolvedCells - 2; i2++) {
                Cell cell2 = unsolvedCells[i2];
                if (cell2.getCandidates().cardinality() > 4) {
                    continue;
                }
                BitSet candidates2 = cell2.getCandidates();
                BitSet union12 = (BitSet) candidates1.clone();
                union12.or(candidates2);
                if (union12.cardinality() > 4) {
                    continue;
                }

                for (int i3 = i2 + 1; i3 < numberOfUnsolvedCells - 1; i3++) {
                    Cell cell3 = unsolvedCells[i3];
                    if (cell3.getCandidates().cardinality() > 4) {
                        continue;
                    }
                    BitSet candidates3 = cell3.getCandidates();
                    BitSet union123 = (BitSet) union12.clone();
                    union123.or(candidates3);
                    if (union123.cardinality() > 4) {
                        continue;
                    }

                    for (int i4 = i3 + 1; i4 < numberOfUnsolvedCells; i4++) {
                        Cell cell4 = unsolvedCells[i4];
                        if (cell4.getCandidates().cardinality() > 4) {
                            continue;
                        }
                        BitSet candidates4 = cell4.getCandidates();
                        BitSet union1234 = (BitSet) union123.clone();
                        union1234.or(candidates4);
                        if (union1234.cardinality() > 4) {
                            continue;
                        }

                        step = createStep(puzzle, cell1, cell2, cell3, cell4, union1234);
                        if (step != null) {
                            break outerLoop;
                        }
                    }
                }
            }
        }

        return step;
    }

    /**
     * Creates a <code>Step</code> describing a naked quad.
     *
     * @param puzzle      The puzzle being solved.
     * @param cell1       The first <code>Cell</code> of the naked quad.
     * @param cell2       The second <code>Cell</code> of the naked quad.
     * @param cell3       The third <code>Cell</code> of the naked quad.
     * @param cell4       The fourth <code>Cell</code> of the naked quad.
     * @param candidates  The collection of candidates that makeup this naked quad.
     * @return            A <code>Step</code> describing a naked quad.
     */
    private static AbstractStep createStep(
            final AbstractPuzzleModel puzzle,
            final Cell cell1,
            final Cell cell2,
            final Cell cell3,
            final Cell cell4,
            final BitSet candidates) {
        Set cellsToBeChanged = new HashSet(puzzle.getUnsolvedBuddies(cell1));
        cellsToBeChanged.retainAll(puzzle.getUnsolvedBuddies(cell2));
        cellsToBeChanged.retainAll(puzzle.getUnsolvedBuddies(cell3));
        cellsToBeChanged.retainAll(puzzle.getUnsolvedBuddies(cell4));
        if (cellsToBeChanged.isEmpty()) {
            return null;
        }

        int candidate1 = candidates.nextSetBit(0);
        int candidate2 = candidates.nextSetBit(candidate1 + 1);
        int candidate3 = candidates.nextSetBit(candidate2 + 1);
        int candidate4 = candidates.nextSetBit(candidate3 + 1);
        Iterator iterator = cellsToBeChanged.iterator();
        while (iterator.hasNext()) {
            Cell cell = (Cell) iterator.next();
            if (!cell.hasCandidate(candidate1)
                    && !cell.hasCandidate(candidate2)
                    && !cell.hasCandidate(candidate3)
                    && !cell.hasCandidate(candidate4)) {
                iterator.remove();
            }
        }
        if (cellsToBeChanged.isEmpty()) {
            return null;
        }

        MessageBundle messageBundle = MessageBundle.getInstance();
        String smallHint = messageBundle.getString("solver.naked.quad.small.hint");
        String bigHint = messageBundle.getString(
                "solver.naked.quad.big.hint",
                new String[] {
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate1)),
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate2)),
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate3)),
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidate4)),
                }
        );

        CandidateRemovalStep step =
                new CandidateRemovalStep(smallHint, bigHint, cellsToBeChanged, 0);
        step.addExplainingCell(cell1);
        step.addExplainingCell(cell2);
        step.addExplainingCell(cell3);
        step.addExplainingCell(cell4);
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
        return MessageBundle.getInstance().getString("solver.naked.quad.menu.item");
    }

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    public String getSolverNotApplicableMessage() {
        return MessageBundle.getInstance().getString("solver.naked.quad.not.applicable");
    }
}
