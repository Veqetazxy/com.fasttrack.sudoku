/*
  JigsawGenerator.java

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

import java.util.Stack;


/**
 * This class generates jigsaw blocks for a sudoku.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public final class JigsawGenerator {

    /** Horizontal increments. */
    private static final int[] X_INC = {
        0, -1, 1, 0,
    };

    /** Vertical increments. */
    private static final int[] Y_INC = {
        -1, 0, 0, 1,
    };

    /** The singleton instance of this class. */
    private static final JigsawGenerator INSTANCE = new JigsawGenerator();

    /** The size of the sudoku grid. */
    private int gridSize;

    /** Array containing the generated jigsaw blocks. */
    private int[][] grid;

    /** Temporary array used to make sure generated jigsaw blocks are valid. */
    private int[][] grid2;

    /** A history of placements, used in case anything needs to be undone. */
    private Stack[] assignments;

    /** The index of the jigsaw block under construction. */
    private int index;

    /** The number of cells in the region currently under construction. */
    private int regionSize;

    /** Private constructor to keep anyone from instantiating this class. */
    private JigsawGenerator() {
        // Nothing to do here.
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return  The singleton instance of this class.
     */
    public static JigsawGenerator getInstance() {
        return INSTANCE;
    }

    /**
     * Generates a jigsaw grid of the specified size.
     *
     * @return  <code>true</code> if the generation was successful. Otherwise, <code>false</code>.
     */
    private boolean generate() {
        int r = 0;
        int c = 0;

        grid      = new int[gridSize][gridSize];
        grid2     = new int[gridSize][gridSize];
        assignments = new Stack[gridSize];

        // If the grid size is odd, don't let the center square be filled in.
        if ((gridSize & 1) > 0) {
            grid[gridSize / 2][gridSize / 2] = -1;
        }

        // Each time through this loop places one piece.
        int failureCount = 0;
        for (index = 1; index <= gridSize / 2; index++) {
            assignments[index] = new Stack();

            // Find the first available (empty) space.
        searchLoop:
            for (r = 0; r < gridSize; r++) {
                for (c = 0; c < gridSize; c++) {
                    if (grid[r][c] == 0) {
                        break searchLoop;
                    }
                }
            }

            boolean valid = false;
            do {
                createOnePiece(r, c);
                valid = isValid();
                if (!valid) {
                    while (!assignments[index].isEmpty()) {
                        Assignment p = (Assignment) assignments[index].pop();
                        grid[p.row][p.column] = 0;
                        grid[gridSize - p.row - 1][gridSize - p.column - 1] = 0;
                    }
                    failureCount++;
                    if (failureCount >= 3) {
                        return false;
                    }
                }
            } while (!valid);
        }

        return true;
    }

    /**
     * Creates one jigsaw piece with its first square at the specified position in the grid.
     *
     * @param r  The index of a row.
     * @param c  The index of a column.
     */
    private void createOnePiece(final int r, final int c) {

        // Place the first cell and record it.
        assignments[index] = new Stack();
        assignments[index].push(new Assignment(r, c));
        grid[r][c] = index;
        grid[gridSize - r - 1][gridSize - c - 1] = gridSize - index + 1;

        // Select a cell in the current piece and try to place a new cell next to it.
        int direction = 0;
        int newR = 0;
        int newC = 0;
        do {
            int cellIndex = (int) (Math.random() * assignments[index].size());
            Assignment p = (Assignment) assignments[index].get(cellIndex);
            direction = (int) (Math.random() * X_INC.length);
            newR = p.row + Y_INC[direction];
            newC = p.column + X_INC[direction];
        } while (newR < 0 || newC < 0 || newR >= gridSize || newC >= gridSize
                || grid[newR][newC] != 0);

        placeOneCell(newR, newC);
    }

    // Place the currently selected cell, then select another cell.
    /**
     * Adds one cell to the jigsaw block under construction.
     *
     * @param r  The index of a row containing one cell of the jigsaw block.
     * @param c  The index of a column containing one cell of the jigsaw block.
     */
    private void placeOneCell(final int r, final int c) {

        // Place the currently selected cell.
        assignments[index].push(new Assignment(r, c));
        grid[r][c] = index;
        grid[gridSize - r - 1][gridSize - c - 1] = gridSize - index + 1;

        // If we have finished this piece, display the board.
        if (assignments[index].size() % gridSize == 0) {
            return;
        }

        // Generate the location of the next cell of this jigsaw piece.
        int direction = 0;
        int cellIndex = 0;
        int newR = 0;
        int newC = 0;
        do {
            cellIndex = (int) (Math.random() * assignments[index].size());
            Assignment p = (Assignment) assignments[index].get(cellIndex);
            direction = (int) (Math.random() * X_INC.length);
            newR = p.row + Y_INC[direction];
            newC = p.column + X_INC[direction];
        } while (newR < 0 || newC < 0 || newR >= gridSize || newC >= gridSize
                || grid[newR][newC] != 0);

        placeOneCell(newR, newC);
    }

    /**
     * Determines whether a grid containing some jigsaw blocks is valid.
     *
     * @return  <code>true</code> if the size of each empty region is a multiple of the grid size.
     *          Otherwise, <code>false</code>.
     */
    private boolean isValid() {

        // Copy the main grid to our work grid.
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                grid2[r][c] = grid[r][c];
            }
        }

        while (true) {
            // Find the first available (empty) space.
            int r = 0;
            int c = 0;
        searchLoop:
            for (r = 0; r < gridSize; r++) {
                for (c = 0; c < gridSize; c++) {
                    if (grid2[r][c] == 0) {
                        break searchLoop;
                    }
                }
            }
            if (r == gridSize) {
                return true;
            }

            regionSize = 0;
            fillRegion(r, c);
            if (regionSize % gridSize != 0) {
                return false;
            }
        }
    }

    /**
     * Fills a region of the work grid. This is used to determine whether a newly generated
     * jigsaw block is valid.
     *
     * @param r  The index of the starting row.
     * @param c  The index of the starting column.
     */
    private void fillRegion(final int r, final int c) {
        grid2[r][c] = 99;
        regionSize++;
        if (r > 0 && grid2[r - 1][c] <= 0) {
            fillRegion(r - 1, c);
        }
        if (r < gridSize - 1 && grid2[r + 1][c] <= 0) {
            fillRegion(r + 1, c);
        }
        if (c > 0 && grid2[r][c - 1] <= 0) {
            fillRegion(r, c - 1);
        }
        if (c < gridSize - 1 && grid2[r][c + 1] <= 0) {
            fillRegion(r, c + 1);
        }
    }

    /**
     * Runs the generator to generate an array containing jigsaw blocks.
     *
     * @param size  The size of the grid.
     * @return      An array containing jigsaw blocks.
     */
    public int[][] run(final int size) {
        this.gridSize = size;
        boolean done = false;
        do {
            done = generate();
        } while (!done);

        if ((gridSize & 1) > 0) {
            for (int r = 0; r < gridSize; r++) {
                for (int c = 0; c < gridSize; c++) {
                    if (grid[r][c] <= 0){
                        grid[r][c] = (gridSize + 1) / 2;
                    }
                }
            }
        }

        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                grid[r][c]--;
            }
        }

        return grid;
    }

    /** An <code>Assignment</code> associates a <code>Cell</code> with a block. */
    private static class Assignment {

        /** The row of the <code>Cell</code>. */
        private final int row;

        /** The column of the <code>Cell</code>. */
        private final int column;

        /**
         * Constructs an <code>Assignment</code>.
         *
         * @param row     The row of the <code>Cell</code>.
         * @param column  The column of the <code>Cell</code>.
         */
        Assignment(final int row, final int column) {
            this.row = row;
            this.column = column;
        }
    }
}
