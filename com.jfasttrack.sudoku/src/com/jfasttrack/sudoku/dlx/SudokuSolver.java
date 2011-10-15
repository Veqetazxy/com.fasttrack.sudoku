/*
  SudokuSolver.java

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

package com.jfasttrack.sudoku.dlx;

import java.util.Iterator;

import com.jfasttrack.dlx.AbstractDLXSolver;
import com.jfasttrack.dlx.ColumnHeader;
import com.jfasttrack.dlx.Node;
import com.jfasttrack.sudoku.puzzle.AbstractPuzzleModel;
import com.jfasttrack.sudoku.puzzle.House;


/**
 * This class uses Knuth's dancing links algorithm to solve a sudoku. It can solve a sudoku grid of
 * any size. The comments that follow assume a standard 9x9 sudoku grid.
 * <p>
 * Each row of the matrix represents a move (i. e., the placement of a digit digit into a cell.
 * Each of the 9 digits can be placed into any of the 81 cells. This means that the starting matrix
 * for a standard sudoku contains 729 (9*81) rows.
 * <p>
 * The matrix columns enforce the various constraints on the moves. Each move must satisfy 4
 * constraints:
 * <ul>
 * <li>
 * Each cell can hold only one digit. This is enforced by the first 81 columns, one for each cell.
 * </li>
 * <li>
 * Each value can appear only once in each row. The next 81 columns are one for each value in each
 * row.
 * </li>
 * <li>
 * Each value can appear only once in each column. The next 81 columns are one for each value in
 * each column.
 * </li>
 * <li>
 * Each value can appear only once in each block. The next 81 columns are one for each value in each
 * block.
 * </li>
 * </ul>
 * For a standard sudoku, the matrix will have a total of 324 columns.
 * <p>
 * An "exact cover" will have (a) 1 value in each cell, (b) each value appearing exactly once in
 * each row, (c) each value appearing exactly once in each column, and (d) each value appearing
 * exactly once in each block.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class SudokuSolver extends AbstractDLXSolver {

    /** The sudoku puzzle to be solved. */
    private final AbstractPuzzleModel puzzle;

    /** The number of <code>Cell</code>s in each row and each column of the sudoku to be solved. */
    private final int gridSize;

    /**
     * Constructs a <code>SudokuSolver</code>.
     *
     * @param puzzle  The sudoku to be solved.
     */
    public SudokuSolver(final AbstractPuzzleModel puzzle) {
        this.puzzle = puzzle;
        gridSize = puzzle.getGridSize();
        createNodes();
    }

    /** Creates the dancing links nodes that will be used to solve a sudoku. */
    protected final void createNodes() {
        createHeaders(gridSize);

        // Each house will construct its own nodes and add them to the dancing links matrix.
        Iterator houses = puzzle.getAllHouses();
        int matrixColumnIndex = gridSize * gridSize;
        while (houses.hasNext()){
            House house = (House) houses.next();
            house.createDlxNodes(this);
            matrixColumnIndex += gridSize;
        }
    }

    /**
     * Creates the headers of the dancing links matrix.
     *
     * @param size  The size of the puzzle grid.
     */
    private void createHeaders(final int size) {

        // Create the column headers.
        for (int cellIndex = 0; cellIndex < size * size; cellIndex++) {
            createColumnHeader();
        }

        // Create the row headers.
        int matrixRowIndex = 0;
        for (int cellIndex = 0; cellIndex < size * size; cellIndex++) {
            for (int value = 0; value < size; value++) {
                Node rowHeader = new Node();
                rowHeader.applicationData = matrixRowIndex;
                addRowHeader(rowHeader);
                rowHeader.columnHeader = getColumnHeader(cellIndex);
                rowHeader.columnHeader.append(rowHeader);
                matrixRowIndex++;
            }
        }
    }

    /**
     * Creates a new <code>ColumnHeader</code> and adds it to the dancing links matrix.
     *
     * @return  The new <code>ColumnHeader</code>.
     */
    public final ColumnHeader createColumnHeader() {
        ColumnHeader root = getRoot();
        ColumnHeader columnHeader = new ColumnHeader();
        columnHeader.left = root.left;
        columnHeader.right = root;
        root.left.right = columnHeader;
        root.left = columnHeader;
        addColumnHeader(columnHeader);
        return columnHeader;
    }

    /**
     * Scans the puzzle, looking for givens (i. e., numbers filled in before solving. Adds these
     * numbers to the solution and removes their corresponding entries from the dancing links
     * matrix.
     *
     * @param puzzleGrid  An array of integers containing an unsolved sudoku.
     */
    public final void placeGivens(final int[] puzzleGrid) {
        for (int row = 0; row < gridSize; row++) {
            for (int column = 0; column < gridSize; column++) {
                int cellIndex = row * gridSize + column;
                if (puzzleGrid[cellIndex] > 0) {
                    addRowToSolution(cellIndex * gridSize + (puzzleGrid[cellIndex] - 1));
                }
            }
        }
    }

    /** Resets the dancing links matrix by removing all of the givens. */
    public void removeAllGivens() {
        removeAllRowsFromSolution();
    }
}
