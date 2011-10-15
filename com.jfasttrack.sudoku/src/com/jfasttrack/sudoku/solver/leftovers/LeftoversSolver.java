/*
  LeftoversSolver.java

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

package com.jfasttrack.sudoku.solver.leftovers;

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
import com.jfasttrack.sudoku.step.ValuePlacementStep;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * This solver applies the law of leftovers to sudoku puzzles.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class LeftoversSolver implements ISolver {

    /*
     * This solver designates groups of cells as "innies" and "outies." Innies and outies may
     * be interchanged, so one solver's innies may well be another solver's outies and vice versa.
     * It just doesn't matter.
     */

    /** The innies of the currently considered line(s). */
    private static final Set INNIES = new HashSet();

    /** The outies of the currently considered line(s). */
    private static final Set OUTIES = new HashSet();

    /**
     * Looks for a place in a sudoku where the law of leftovers can be applied.
     *
     * @param puzzle  The sudoku to be solved.
     * @return        A <code>Step</code> toward the solution of a sudoku. <code>null</code> if
     *                this solver cannot be applied to this sudoku.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        AbstractStep step = null;

        for (int leftoverSize = 1; leftoverSize <= puzzle.getGridSize() / 2; leftoverSize++) {
            step = findLeftoversByRow(puzzle, leftoverSize);
            if (step == null) {
                step = findLeftoversByColumn(puzzle, leftoverSize);
            }
            if (step != null) {
                break;
            }
        }

        return step;
    }

    /**
     * Divides the sudoku horizontally, looking for leftovers.
     *
     * @param puzzle        The sudoku to be solved.
     * @param leftoverSize  The size (number of cells) of the leftover to be found.
     * @return              A <code>Step</code> toward the solution of a sudoku. <code>null</code>
     *                      if this solver cannot be applied to this sudoku.
     */
    private static AbstractStep findLeftoversByRow(
            final AbstractPuzzleModel puzzle,
            final int                 leftoverSize) {
        AbstractStep step = null;
        int size = puzzle.getGridSize();

        int[] blockCounts = new int[size];

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Cell cell = puzzle.getCellAt(r, c);
                blockCounts[cell.getBlockIndex()]++;
            }

            collectRowInniesAndOuties(puzzle, r, blockCounts);
            if (INNIES.size() == leftoverSize && OUTIES.size() == leftoverSize) {
                step = createStep(puzzle, INNIES, OUTIES);
                if (step == null) {
                    step = createStep(puzzle, OUTIES, INNIES);
                }
                if (step != null) {
                    break;
                }
            }
        }

        return step;
    }

    /**
     * Scans a sudoku grid looking for innies and outies when dividing the grid on a horizontal
     * line.
     *
     * @param puzzle       The sudoku to be solved.
     * @param row          The index of the row where the grid is divided.
     * @param blockCounts  The number of <code>Cell</code>s in each block, considering only
     *                     <code>Cell</code>s in or above the specified row.
     */
    private static void collectRowInniesAndOuties(
            final AbstractPuzzleModel puzzle,
            final int                 row,
            final int[]               blockCounts) {
        int size = puzzle.getGridSize();

        INNIES.clear();
        OUTIES.clear();
        for (int b = 0; b < size; b++) {
            if (blockCounts[b] == size) {
                continue;
            }
            House block = puzzle.getBlock(b);
            Iterator i = block.getAllCells();
            if (blockCounts[b] <= size / 2) {
                while (i.hasNext()) {
                    Cell cell = (Cell) i.next();
                    if (cell.getRow() <= row) {
                        INNIES.add(cell);
                    }
                }
            } else {
                while (i.hasNext()) {
                    Cell cell = (Cell) i.next();
                    if (cell.getRow() > row) {
                        OUTIES.add(cell);
                    }
                }
            }
        }
    }

    /**
     * Divides the sudoku vertically, looking for leftovers.
     *
     * @param puzzle        The sudoku to be solved.
     * @param leftoverSize  The size (number of cells) of the leftover to be found.
     * @return              A <code>Step</code> toward the solution of a sudoku. <code>null</code>
     *                      if this solver cannot be applied to this sudoku.
     */
    private static AbstractStep findLeftoversByColumn(
            final AbstractPuzzleModel puzzle,
            final int                 leftoverSize) {
        AbstractStep step = null;
        int size = puzzle.getGridSize();

        int[] blockCounts = new int[size];
        for (int c = 0; c < size; c++) {
            for (int r = 0; r < size; r++) {
                Cell cell = puzzle.getCellAt(r, c);
                blockCounts[cell.getBlockIndex()]++;
            }

            collectColumnInniesAndOuties(puzzle, c, blockCounts);
            if (INNIES.size() == leftoverSize && OUTIES.size() == leftoverSize) {
                step = createStep(puzzle, INNIES, OUTIES);
                if (step == null) {
                    step = createStep(puzzle, OUTIES, INNIES);
                }
                if (step != null) {
                    break;
                }
            }
        }

        return step;
    }

    /**
     * Scans a sudoku grid looking for innies and outies when dividing the grid on a vertical line.
     *
     * @param puzzle       The sudoku to be solved.
     * @param column       The index of the column where the grid is divided.
     * @param blockCounts  The number of <code>Cell</code>s in each block, considering only
     *                     <code>Cell</code>s in or to the left of the specified column.
     */
    private static void collectColumnInniesAndOuties(
            final AbstractPuzzleModel puzzle,
            final int                 column,
            final int[]               blockCounts) {
        int size = puzzle.getGridSize();

        INNIES.clear();
        OUTIES.clear();
        for (int b = 0; b < size; b++) {
            if (blockCounts[b] == size) {
                continue;
            }
            House block = puzzle.getBlock(b);
            Iterator i = block.getAllCells();
            if (blockCounts[b] <= size / 2) {
                while (i.hasNext()) {
                    Cell cell = (Cell) i.next();
                    if (cell.getColumn() <= column) {
                        INNIES.add(cell);
                    }
                }
            } else {
                while (i.hasNext()) {
                    Cell cell = (Cell) i.next();
                    if (cell.getColumn() > column) {
                        OUTIES.add(cell);
                    }
                }
            }
        }
    }

    /**
     * Creates a <code>Step</code> describing an application of the law of leftovers.
     *
     * @param puzzle    The sudoku to be solved.
     * @param cellSet1  A collection of <code>Cell</code>s, either innies or outies.
     * @param cellSet2  Another collection of <code>Cell</code>s, either innies or outies.
     * @return          A <code>Step</code> toward the solution of a sudoku. <code>null</code> if
     *                  this solver cannot be applied to this sudoku.
     */
    private static AbstractStep createStep(
            final AbstractPuzzleModel puzzle,
            final Set                 cellSet1,
            final Set                 cellSet2) {
        AbstractStep step = null;

        // A value in a solved cell in one set must also appear in a solved cell of the other set.
        step = generateRequiredValueStep(cellSet1, cellSet2);
        if (step == null) {
            step = generateRequiredValueStep(cellSet2, cellSet1);
        }

        // A value that is not a candidate in one set cannot be a candidate in the other.
        if (step == null) {
            step = createCommonCandidateStep(puzzle, cellSet2, cellSet1);
        }
        if (step == null) {
            step = createCommonCandidateStep(puzzle, cellSet1, cellSet2);
        }

//TODO:
// If all of the innies are in the same line, any digit that must appear in one of the innies
// can be removed from the other cells in that line.
// If all of the outies are in the same line, any digit that must appear in one of the outies
// can be removed from the other cells in that line.

        return step;
    }

    /**
     * For each given or solved <code>Cell</code> in the first set, tests to see where its value
     * might appear in the second set. If it can appear in only one unsolved <code>Cell</code> in
     * the second set, then that <code>Cell</code> must contain that value.
     *
     * @param cellSet1  A collection of <code>Cell</code>s, either innies or outies.
     * @param cellSet2  Another collection of <code>Cell</code>s, either innies or outies.
     * @return          A <code>Step</code> toward the solution of a sudoku. <code>null</code> if
     *                  this solver cannot be applied to this sudoku.
     */
    private static AbstractStep generateRequiredValueStep(final Set cellSet1, final Set cellSet2) {
        AbstractStep step = null;

        // Loop through each cell in the first set that contains a value.
        Iterator i1 = cellSet1.iterator();
    outerLoop:
        while (i1.hasNext()) {
            Cell cell1 = (Cell) i1.next();
            if (!cell1.containsValue()) {
                continue;
            }
            int value = cell1.getValue();

            // Check the cells in the second set to see which have that value as a candidate.
            Set requiredOuties = new HashSet();
            Iterator i2 = cellSet2.iterator();
            while (i2.hasNext()) {
                Cell cell2 = (Cell) i2.next();
                if (cell2.containsValue()) {
                    if (cell2.getValue() == cell1.getValue()) {
                        continue outerLoop;
                    }
                    continue;
                }
                if (cell2.hasCandidate(value)) {
                    requiredOuties.add(cell2);
                }
            }    // while i2

            // If that value can appear in only one cell of the second set, it must appear there.
            if (requiredOuties.size() == 1) {
                step = createValuePlacementStep(cellSet1, cellSet2, requiredOuties, value);
                break;
            }
        }    // while i1

        return step;
    }

    /**
     * Compares the collective candidates in 2 sets of <code>Cell</code>s. If any value appears as
     * a candidate in the first set but not the second, it can be removed as a candidate from each
     * <code>Cell</code> in the first set.
     *
     * @param cellSet1        A collection of <code>Cell</code>s, either innies or outies.
     * @param cellSet2        Another collection of <code>Cell</code>s, either innies or outies.
     * @param requiredOuties  The collection of outies.
     * @param value           The value that can be placed into a <code>Cell</code>.
     * @return                A <code>Step</code> toward the solution of a sudoku.
     */
    private static AbstractStep createValuePlacementStep(
            final Set cellSet1,
            final Set cellSet2,
            final Set requiredOuties,
            final int value) {

        Cell requiredOutie = (Cell) requiredOuties.iterator().next();

        MessageBundle messageBundle = MessageBundle.getInstance();
        String smallHint = messageBundle.getString("solver.leftovers.small.hint");
        String bigHint = messageBundle.getString(
                "solver.leftovers.big.hint.1",
                new String[] {
                    String.valueOf(requiredOutie.getRow() + 1),
                    String.valueOf(requiredOutie.getColumn() + 1),
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                }
        );
        AbstractStep step = new ValuePlacementStep(smallHint, bigHint, requiredOutie, value);
        Iterator i3 = cellSet1.iterator();
        while (i3.hasNext()) {
            step.addExplainingCell((Cell) i3.next());
        }
        Iterator i4 = cellSet2.iterator();
        while (i4.hasNext()) {
            Cell cell = (Cell) i4.next();
            if (cell != requiredOutie) {
                step.addExplainingCell(cell);
            }
        }

        return step;
    }

    /**
     * Compares the collective candidates in 2 sets of <code>Cell</code>s. If any value appears as
     * a candidate in the first set but not the second, it can be removed as a candidate from each
     * <code>Cell</code> in the first set.
     *
     * @param puzzle    The sudoku to be solved.
     * @param cellSet1  A collection of <code>Cell</code>s, either innies or outies.
     * @param cellSet2  Another collection of <code>Cell</code>s, either innies or outies.
     * @return          A <code>Step</code> toward the solution of a sudoku. <code>null</code> if
     *                  this solver cannot be applied to this sudoku.
     */
    private static AbstractStep createCommonCandidateStep(
            final AbstractPuzzleModel puzzle,
            final Set                 cellSet1,
            final Set                 cellSet2) {
        AbstractStep step = null;

        // Collect all of the candidate values in the first set of cells.
        BitSet candidates1 = collectCandidateValues(cellSet1);
        BitSet candidates2 = collectCandidateValues(cellSet2);

        // If any value appears in the first set but not the second, generate a step of a solution.
        for (int value = 1; value <= puzzle.getGridSize(); value++) {
            if (candidates1.get(value) && !candidates2.get(value)) {
                MessageBundle messageBundle = MessageBundle.getInstance();
                String smallHint = messageBundle.getString("solver.leftovers.small.hint");
                String bigHint = messageBundle.getString(
                        "solver.leftovers.big.hint.2",
                        new String[] {
                            String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                        }
                );
                Set affectedCells = new HashSet();
                Iterator i = cellSet1.iterator();
                while (i.hasNext()) {
                    Cell cell = (Cell) i.next();
                    if (cell.hasCandidate(value)) {
                        affectedCells.add(cell);
                    }
                }
                step = new CandidateRemovalStep(smallHint, bigHint, affectedCells, value);
                i = cellSet2.iterator();
                while (i.hasNext()) {
                    step.addExplainingCell((Cell) i.next());
                }
                break;
            }
        }

        return step;
    }

    /**
     * Builds a bitset containing each candidate that is a candidate in one or more of the
     * <code>Cell</code>s provided.
     *
     * @param cells  The <code>Cell</code>s to be checked.
     * @return       A bitset containing each candidate that is a candidate in one or more of the
     *               <code>Cell</code>s provided.
     */
    private static BitSet collectCandidateValues(final Set cells) {
        BitSet candidates = new BitSet();
        Iterator i = cells.iterator();
        while (i.hasNext()) {
            Cell cell = (Cell) i.next();
            if (cell.containsValue()) {
                candidates.set(cell.getValue());
            } else {
                candidates.or(cell.getCandidates());
            }
        }

        return candidates;
    }

    /**
     * Gets the text for the menu item used to invoke this solver.
     *
     * @return  The text for the menu item used to invoke this solver.
     */
    public String getNameOfMenuItem() {
        return MessageBundle.getInstance().getString("solver.leftovers.menu.item");
    }

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    public String getSolverNotApplicableMessage() {
        return MessageBundle.getInstance().getString("solver.leftovers.not.applicable");
    }
}
