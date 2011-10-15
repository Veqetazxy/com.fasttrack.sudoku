/*
  StandardSudoku.java

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

package com.jfasttrack.sudoku.puzzle;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import com.jfasttrack.dlx.Node;
import com.jfasttrack.dlx.SolutionListener;
import com.jfasttrack.sudoku.dlx.SudokuSolver;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * A <code>StandardSudoku</code> is a regular sudoku: a single square grid (of any size) with
 * rectangular or jigsaw blocks.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class StandardSudoku extends AbstractPuzzleModel {

    /** Message bundle that holds all messages for this program. */
    private static final MessageBundle MESSAGE_BUNDLE = MessageBundle.getInstance();

    /** The number of solutions generated during puzzle creation. */
    private int numberOfSolutions;

    /** Random number generator. */
    private final Random random = new Random();

    /** A line of text containing part of a sudoku. */
    private String line;

    /** Constructs a <code>StandardSudoku</code>. */
    public StandardSudoku() {
        if (Options.getInstance().getCreateAction() == Options.CreateAction.GENERATE) {
            generateRandomSudoku();
        }
    }

    /**
     * Constructs a <code>StandardSudoku</code> from a <code>String</code>.
     *
     * @param puzzleString  A <code>String</code> containing a sudoku.
     */
    public StandardSudoku(final String puzzleString) {
        super(puzzleString);

        Options options = Options.getInstance();

        StringTokenizer st = new StringTokenizer(puzzleString.replaceAll("0", "."), "\n");

        line = st.nextToken();
        if (line.charAt(0) == ':') {
            line = st.nextToken();
        }

        // If this is a jigsaw sudoku, create the houses.
        if (options.getBlockType() == Options.BlockType.JIGSAW) {
            createJigsawBlocks(st);
            line = st.nextToken();
        }

        setCells(st);
    }

    /**
     * Create the jigsaw blocks of a new sudoku.
     *
     * @param st  <code>StringTokenizer</code> whose next tokens describe the blocks of a sudoku.
     */
    private void createJigsawBlocks(final StringTokenizer st) {
        for (int b = 0; b < getGridSize(); b++) {
            String[] houseIndex = {
                String.valueOf(b + 1),
            };
            addBlock(new House(MESSAGE_BUNDLE.getString("block.name", houseIndex)));
        }

        int i = 0;
        for (int row = 0; row < getGridSize(); row++) {
            for (int column = 0; column < getGridSize(); column++) {
                while (Character.isWhitespace(line.charAt(i))) {
                    i++;
                }
                int blockIndex = PuzzleDelegate.CHARACTERS.indexOf(line.charAt(i));
                House block = getBlock(blockIndex);
                Cell cell = getCellAt(row, column);
                cell.setBlockIndex(blockIndex);
                block.addCell(cell);
                i++;
            }
            if (row < getGridSize() - 1 && i >= line.length()) {
                line = st.nextToken();
                i = 0;
            }
        }
    }

    /**
     * Create the cells of a new sudoku.
     *
     * @param st  <code>StringTokenizer</code> whose next tokens describe a sudoku.
     */
    private void setCells(final StringTokenizer st) {
        int stringIndex = 0;
        for (int row = 0; row < getGridSize(); row++) {
            for (int column = 0; column < getGridSize(); column++) {
                char c = line.charAt(stringIndex);
                while (c == ' ' || c == '\n') {
                    stringIndex++;
                    c = line.charAt(stringIndex);
                }
                int value = PuzzleDelegate.CHARACTERS.indexOf(c);
                originalPuzzle[row * getGridSize() + column] = value;
                Cell cell = getCellAt(row, column);
                if (value > 0) {
                    cell.setStateAndValue(CellState.GIVEN, value, null);
                } else {
                    cell.setStateAndValue(CellState.UNSOLVED, 0, null);
                }
                stringIndex++;
            }
            if (row < getGridSize() - 1 && stringIndex >= line.length()) {
                line = st.nextToken();
                stringIndex = 0;
            }
        }
    }

    /** Generates a random sudoku. */
    private void generateRandomSudoku() {

        // Puzzle creation is a 3-step process.

        // Step 1: Generate a solution grid.
        generateSolutionGrid();

        // Step 2: Randomly remove pairs of values as long as the puzzle has a unique solution.
        randomlyRemoveValues();

        // Step 3: Minimize the puzzle. Go through the grid, trying to remove values 2 at a time.
        minimize();

        // originalPuzzle contains a valid, minimized sudoku. Copy the values into the workGrid.
        for (int row = 0; row < getGridSize(); row++) {
            for (int column = 0; column < getGridSize(); column++) {
                int cellIndex = row * getGridSize() + column;
                int value = originalPuzzle[cellIndex];
                if (value > 0) {
                    getCellAt(row, column).setStateAndValue(CellState.GIVEN, value, null);
                }
            }
        }
    }

    /** Generates a random solution grid. */
    private void generateSolutionGrid() {
        int size = getGridSize();
        SudokuSolver solver1 = new SudokuSolver(this);
        solver1.addSolutionListener(new SolutionListener() {
            public boolean solutionFound(final List solutionNodes) {
                numberOfSolutions++;

                Iterator iterator = solutionNodes.iterator();
                while (iterator.hasNext()) {
                    Node node = (Node) iterator.next();
                    int index = node.applicationData / getGridSize();
                    int value = node.applicationData % getGridSize() + 1;
                    originalPuzzle[index] = value;
                }

                return true;
            }
        });

        int limit = size;
        do {
            // Clear the grid.
            for (int cellIndex = 0; cellIndex < size * size; cellIndex++) {
                originalPuzzle[cellIndex] = 0;
            }

            // Seed the grid with a few random numbers.
            for (int value = 1; value <= limit; value++) {

                // Generate the index of an empty cell.
                int cellIndex = 0;
                do {
                    cellIndex = random.nextInt(size * size);
                } while (originalPuzzle[cellIndex] != 0);

                // And put the value into the grid.
                originalPuzzle[cellIndex] = value;
            }

            // Solve the grid.
            solver1.placeGivens(getOriginalPuzzle());
            numberOfSolutions = 0;
            solver1.solve();
            solver1.removeAllGivens();
            limit--;
        } while (numberOfSolutions == 0);
    }

    /** Randomly removes values from a sudoku grid. */
    private void randomlyRemoveValues() {

        /*
         * Start with a completed (solved) sudoku grid.
         *
         * Each pass through the loop:
         *  1) Select the indexes of 4 non-empty cells. Make the selections to preserve symmetry.
         *  2) Remove the values from those cells.
         *  3) Try to solve the resulting puzzle.
         *  4) If the result puzzle has more than 1 solution, put those values back into the grid.
         * Keep looping until the test in step 4 fails 3 times.
         */

        int[] cellIndex = new int[4];
        int[] value = new int[4];
        int size = getGridSize();
        int numberOfCells = size * size;

        SudokuSolver solver2 = new SudokuSolver(this);
        solver2.addSolutionListener(new SolutionListener() {
            public boolean solutionFound(final List solutionNodes) {
                numberOfSolutions++;
                return false;
            }
        });

        int failureCount = 0;
        do {

            // Generate the indexes of 4 cells that are not empty.
            do {
                cellIndex[0] = random.nextInt(numberOfCells);
            } while (originalPuzzle[cellIndex[0]] == 0);
            cellIndex[1] = numberOfCells - cellIndex[0] - 1;
            do {
                cellIndex[2] = random.nextInt(numberOfCells);
            } while (originalPuzzle[cellIndex[2]] == 0
                    && cellIndex[2] != cellIndex[0]
                    && cellIndex[2] != cellIndex[1]);
            cellIndex[3] = numberOfCells - cellIndex[2] - 1;

            // Save the values in those cells, then clear those cells.
            for (int i = 0; i < value.length; i++) {
                value[i] = originalPuzzle[cellIndex[i]];
                originalPuzzle[cellIndex[i]] = 0;
            }

            // Solve the resulting sudoku.
            numberOfSolutions = 0;
            solver2.placeGivens(getOriginalPuzzle());
            solver2.solve();
            solver2.removeAllGivens();

            // If the solution is no longer unique, put the values back and record a failure.
            if (numberOfSolutions > 1) {
                for (int i = value.length - 1; i >= 0; i--) {
                    originalPuzzle[cellIndex[i]] = value[i];
                }
                failureCount++;
            }

        // Loop until this fails 3 times.
        } while (failureCount < 3);
    }

    /** Minimizes a sudoku grid. */
    private void minimize() {
        int size = getGridSize();
        int numberOfCells = size * size;
        SudokuSolver solver = new SudokuSolver(this);
        solver.addSolutionListener(new SolutionListener() {

            /**
             * This is called when a solution is found. It merely
             * increments the number of solutions.
             *
             * @param solutionNodes  The <code>Node</code>s that make up
             *                       the generated solution.
             * @return               <code>false</code> to tell the solver
             *                       to continue generating solutions.
             */
            public boolean solutionFound(final List solutionNodes) {
                numberOfSolutions++;
                return false;
            }
        });

        for (int cellIndex1 = 0; cellIndex1 < (numberOfCells + 1) / 2; cellIndex1++) {
            if (originalPuzzle[cellIndex1] != 0) {
                int cellIndex2 = numberOfCells - cellIndex1 - 1;

                // Save the values, then clear those cells.
                int value1 = originalPuzzle[cellIndex1];
                int value2 = originalPuzzle[cellIndex2];
                originalPuzzle[cellIndex1] = 0;
                originalPuzzle[cellIndex2] = 0;

                // Solve the resulting sudoku.
                numberOfSolutions = 0;
                solver.placeGivens(getOriginalPuzzle());
                solver.solve();
                solver.removeAllGivens();

                // If the solution is no longer unique, put the values back.
                if (numberOfSolutions > 1) {
                    originalPuzzle[cellIndex1] = value1;
                    originalPuzzle[cellIndex2] = value2;
                }
            }
        }
    }

    /**
     * Gets a string representation of the original puzzle.
     *
     * @return  A <code>String</code> representation of the original puzzle.
     */
    public String toOriginalString() {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < originalPuzzle.length; i++) {
            result.append(PuzzleDelegate.CHARACTERS.charAt(originalPuzzle[i]));
        }

        return result.toString();
    }

    /**
     * Gets a string representation of this <code>StandardSudoku</code>.
     *
     * @return  A string representation of this <code>StandardSudoku</code>.
     */
    public String toString() {
        Options options = Options.getInstance();
        StringBuffer result = new StringBuffer();

        String optionsString = options.toString();
        if (optionsString.length() > 0) {
            result.append(optionsString);
            result.append('\n');
        }

        if (options.getBlockType() == Options.BlockType.JIGSAW) {
            for (int row = 0; row < getGridSize(); row++) {
                for (int column = 0; column < getGridSize(); column++) {
                    result.append(
                            PuzzleDelegate.CHARACTERS.charAt(
                                    getCellAt(row, column).getBlockIndex()));
                }
                result.append('\n');
            }
        }

        for (int row = 0; row < getGridSize(); row++) {
            for (int column = 0; column < getGridSize(); column++) {
                Cell cell = getCellAt(row, column);
                if (cell.containsValue()) {
                    result.append(PuzzleDelegate.CHARACTERS.charAt(cell.getValue()));
                } else {
                    result.append('.');
                }
            }
            result.append('\n');
        }

        return result.toString();
    }
}
