/*
  XWingSolver.java

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

package com.jfasttrack.sudoku.solver.fish;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
 * This solver looks for X-wings in a sudoku.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class XWingSolver implements ISolver {

    /**
     * A collection of <code>Cell</code>s that have a candidate being considered. Each element
     * of this <code>List</code> contains the selected <code>Cell</code>s for a single
     * <code>House</code>.
     */
    private final List testCells = new ArrayList();

    /**
     * Looks for X-wings in a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing an X-wing. <code>null</code> if the puzzle
     *                does not contain an X-wing.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        AbstractStep step = findHorizontalXWing(puzzle);
        if (step == null) {
            step = findVerticalXWing(puzzle);
        }

        return step;
    }

    /**
     * Looks for an X-wing based on 2 rows or diagonals in which a value appears as a candidate
     * exactly twice.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing an X-wing. <code>null</code> if the puzzle
     *                does not contain a horizontal X-wing.
     */
    private AbstractStep findHorizontalXWing(final AbstractPuzzleModel puzzle) {
        AbstractStep step = null;

        for (int value = 1; value <= puzzle.getGridSize(); value++) {
            testCells.clear();

            // Build a list of rows and diagonals that have the candidate value exactly twice.
            Iterator rows = puzzle.getAllRows();
            while (rows.hasNext()) {
                House row = (House) rows.next();
                List cellsInRow = new ArrayList();
                Iterator cells = row.getAllCells();
                while (cells.hasNext()) {
                    Cell cell = (Cell) cells.next();
                    if (cell.hasCandidate(value)) {
                        cellsInRow.add(cell);
                    }
                }
                if (cellsInRow.size() == 2) {
                    testCells.add(cellsInRow);
                }
            }
            Iterator diagonals = puzzle.getBothDiagonals();
            while (diagonals.hasNext()) {
                House diagonal = (House) diagonals.next();
                List cellsInRow = new ArrayList();
                Iterator cells = diagonal.getAllCells();
                while (cells.hasNext()) {
                    Cell cell = (Cell) cells.next();
                    if (cell.hasCandidate(value)) {
                        cellsInRow.add(cell);
                    }
                }
                if (cellsInRow.size() == 2) {
                    testCells.add(cellsInRow);
                }
            }

            // Go through the selected rows, checking whether the values appear in the same columns.
            for (int rowIndex1 = 0; rowIndex1 < testCells.size() - 1; rowIndex1++) {
                List cellsInRow1 = (List) testCells.get(rowIndex1);
                Cell cell1 = (Cell) cellsInRow1.get(0);
                Cell cell2 = (Cell) cellsInRow1.get(1);
                for (int rowIndex2 = rowIndex1 + 1; rowIndex2 < testCells.size(); rowIndex2++) {
                    List cellsInRow2 = (List) testCells.get(rowIndex2);
                    Cell cell3 = (Cell) cellsInRow2.get(0);
                    Cell cell4 = (Cell) cellsInRow2.get(1);
                    if (cell1 == cell3 || cell2 == cell4) {
                        continue;
                    }
                    if (cell1.getColumn() != cell3.getColumn()) {
                        continue;
                    }
                    if (cell2.getColumn() != cell4.getColumn()) {
                        continue;
                    }
                    int column1 = cell1.getColumn();
                    int column2 = cell2.getColumn();

                    // This could be an X-wing. See if any other cells can be changed.
                    Set cellsToBeChanged = new HashSet();
                    for (int r = 0; r < puzzle.getGridSize(); r++) {
                        Cell cell = puzzle.getCellAt(r, column1);
                        if (cell != cell1 && cell != cell3 && cell.hasCandidate(value)) {
                            cellsToBeChanged.add(cell);
                        }
                        cell = puzzle.getCellAt(r, column2);
                        if (cell != cell2 && cell != cell4 && cell.hasCandidate(value)) {
                            cellsToBeChanged.add(cell);
                        }
                    }

                    if (cellsToBeChanged.isEmpty()) {
                        continue;
                    }

                    MessageBundle messageBundle = MessageBundle.getInstance();
                    String smallHint = messageBundle.getString(
                            "solver.fish.xwing.small.hint");
                    String bigHint = messageBundle.getString(
                            "solver.fish.xwing.big.hint",
                            new String[] {
                                String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                            }
                    );

                    step = new CandidateRemovalStep(smallHint, bigHint, cellsToBeChanged, value);
                    step.addExplainingCell(cell1);
                    step.addExplainingCell(cell2);
                    step.addExplainingCell(cell3);
                    step.addExplainingCell(cell4);
                    return step;
                }
            }
        }    // for value

        return step;
    }

    /**
     * Looks for an X-wing based on 2 columns or diagonals in which a value appears as a candidate
     * exactly twice.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing an X-wing. <code>null</code> if the puzzle
     *                does not contain a vertical X-wing.
     */
    private AbstractStep findVerticalXWing(final AbstractPuzzleModel puzzle) {
        AbstractStep step = null;

        for (int value = 1; value <= puzzle.getGridSize(); value++) {
            testCells.clear();

            // Build a list of columns and diagonals that have the candidate value exactly twice.
            Iterator columns = puzzle.getAllColumns();
            while (columns.hasNext()) {
                House column = (House) columns.next();
                List cellsInColumn = new ArrayList();
                Iterator cells = column.getAllCells();
                while (cells.hasNext()) {
                    Cell cell = (Cell) cells.next();
                    if (cell.hasCandidate(value)) {
                        cellsInColumn.add(cell);
                    }
                }
                if (cellsInColumn.size() == 2) {
                    testCells.add(cellsInColumn);
                }
            }
            Iterator diagonals = puzzle.getBothDiagonals();
            while (diagonals.hasNext()) {
                House diagonal = (House) diagonals.next();
                List cellsInColumn = new ArrayList();
                Iterator cells = diagonal.getAllCells();
                while (cells.hasNext()) {
                    Cell cell = (Cell) cells.next();
                    if (cell.hasCandidate(value)) {
                        cellsInColumn.add(cell);
                    }
                }
                if (cellsInColumn.size() == 2) {
                    testCells.add(cellsInColumn);
                }
            }

            // Go through the selected columns, checking whether the values appear in the same rows.
            for (int columnIndex1 = 0; columnIndex1 < testCells.size() - 1; columnIndex1++) {
                List cellsInColumn1 = (List) testCells.get(columnIndex1);
                Cell cell1 = (Cell) cellsInColumn1.get(0);
                Cell cell2 = (Cell) cellsInColumn1.get(1);
                for (int columnIndex2 = columnIndex1 + 1;
                        columnIndex2 < testCells.size();
                        columnIndex2++) {
                    List cellsInColumn2 = (List) testCells.get(columnIndex2);
                    Cell cell3 = (Cell) cellsInColumn2.get(0);
                    Cell cell4 = (Cell) cellsInColumn2.get(1);
                    if (cell1 == cell3 || cell2 == cell4) {
                        continue;
                    }
                    if (cell1.getRow() != cell3.getRow()) {
                        continue;
                    }
                    if (cell2.getRow() != cell4.getRow()) {
                        continue;
                    }
                    int row1 = cell1.getRow();
                    int row2 = cell2.getRow();

                    // This could be an X-wing. See if any other cells can be changed.
                    Set cellsToBeChanged = new HashSet();
                    for (int c = 0; c < puzzle.getGridSize(); c++) {
                        Cell cell = puzzle.getCellAt(row1, c);
                        if (cell != cell1 && cell != cell3 && cell.hasCandidate(value)) {
                            cellsToBeChanged.add(cell);
                        }
                        cell = puzzle.getCellAt(row2, c);
                        if (cell != cell2 && cell != cell4 && cell.hasCandidate(value)) {
                            cellsToBeChanged.add(cell);
                        }
                    }

                    if (cellsToBeChanged.isEmpty()) {
                        continue;
                    }

                    MessageBundle messageBundle = MessageBundle.getInstance();
                    String smallHint = messageBundle.getString(
                            "solver.fish.xwing.small.hint");
                    String bigHint = messageBundle.getString(
                            "solver.fish.xwing.big.hint",
                            new String[] {
                                String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                            }
                    );

                    step = new CandidateRemovalStep(smallHint, bigHint, cellsToBeChanged, value);
                    step.addExplainingCell(cell1);
                    step.addExplainingCell(cell2);
                    step.addExplainingCell(cell3);
                    step.addExplainingCell(cell4);
                    return step;
                }
            }
        }    // for value

        return step;
    }

    /**
     * Gets the text for the menu item used to invoke this solver.
     *
     * @return  The text for the menu item used to invoke this solver.
     */
    public String getNameOfMenuItem() {
        return MessageBundle.getInstance()
                .getString("solver.fish.xwing.menu.item");
    }

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    public String getSolverNotApplicableMessage() {
        return MessageBundle.getInstance()
                .getString("solver.fish.xwing.not.applicable");
    }
}
