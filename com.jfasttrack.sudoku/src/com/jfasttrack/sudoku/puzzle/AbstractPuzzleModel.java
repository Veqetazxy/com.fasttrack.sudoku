/*
  AbstractPuzzleModel.java

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.jfasttrack.sudoku.puzzle.Options.CreateAction;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * The puzzle model holds the data for a sudoku grid.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public abstract class AbstractPuzzleModel {

    /** The values that make up the original (unsolved) puzzle. */
    protected final int[] originalPuzzle;

    /**
     * The grid used to solve the sudoku. It starts out with the same contents as
     * <code>originalPuzzle</code>, then gets filled in as the puzzle is solved.
     */
    private final List workGrid;

    /**
     * The size of the grid. Grids must be square (unlike blocks), so this number is used as both
     * the height and width of the grid. It is also the maximum value of any value or candidate.
     */
    private final int gridSize;

    /** The rows of this <code>PuzzleModel</code>. */
    private final List rows = new ArrayList();

    /** The columns of this <code>PuzzleModel</code>. */
    private final List columns = new ArrayList();

    /** The blocks of this <code>PuzzleModel</code>. */
    private final List blocks = new ArrayList();

    /** The diagonals of this <code>PuzzleModel</code>. */
    private final List diagonals = new ArrayList();

    /** A collection containing every <code>House</code> (row, column, block, etc.). */
    private final List houses = new ArrayList();

    /** Constructs an <code>AbstractPuzzleModel</code>. */
    AbstractPuzzleModel() {
        Options options = Options.getInstance();
        gridSize = options.getGridSize();
        workGrid = new ArrayList();
        originalPuzzle = new int[gridSize * gridSize];

        int[][] blockIndexes = createBlockIndexes();
        createHouses();
        createCells(blockIndexes);
    }

    /**
     * Constructs an <code>AbstractPuzzleModel</code> from a <code>String</code>.
     *
     * @param puzzleString  A <code>String</code> containing a sudoku.
     */
    AbstractPuzzleModel(final String puzzleString) {
        Options options = Options.getInstance();
        options.setDefaults();
        options.setCreateAction(Options.CreateAction.LOAD);

        StringTokenizer st = new StringTokenizer(puzzleString, "\n");

        String line = st.nextToken();
        if (line.charAt(0) == ':') {
            options.load(line);
        }

        gridSize = options.getGridSize();
        workGrid = new ArrayList();

        originalPuzzle = new int[gridSize * gridSize];
        int[][] blockIndexes = createBlockIndexes();
        createHouses();
        createCells(blockIndexes);
    }

    /**
     * Creates an array containing the block index for each cell.
     *
     * @return  An array containing the block index for each cell.
     */
    private int[][] createBlockIndexes() {
        Options options = Options.getInstance();
        Options.CreateAction createAction = options.getCreateAction();

        int[][] blockIndexes = null;
        if (options.getBlockType() == Options.BlockType.RECTANGULAR) {
            blockIndexes = new int[gridSize][gridSize];
            for (int row = 0; row < gridSize; row++) {
                for (int column = 0; column < gridSize; column++) {
                    blockIndexes[row][column] =
                            row / options.getBlockHeight() * options.getBlockHeight()
                            + column / options.getBlockWidth();
                }
            }
        } else {

            // JIGSAW
            if (createAction == Options.CreateAction.GENERATE) {
                blockIndexes = JigsawGenerator.getInstance().run(gridSize);
            } else {

                // (CREATE_EMPTY or LOAD) + JIGSAW. Set all of the indexes to -1.
                blockIndexes = new int[gridSize][gridSize];
                for (int row = 0; row < gridSize; row++) {
                    for (int column = 0; column < gridSize; column++) {
                        blockIndexes[row][column] = -1;
                    }
                }
            }
        }

        return blockIndexes;
    }

    /** Creates this sudoku's row, column, and -- if appropriate -- diagonal houses. */
    private void createHouses() {
        MessageBundle messageBundle = MessageBundle.getInstance();
        Options options = Options.getInstance();
        Options.CreateAction createAction = options.getCreateAction();

        // Create the houses.
        for (int i = 0; i < gridSize; i++) {
            String[] houseIndex = {
                String.valueOf(i + 1),
            };
            House row = new House(messageBundle.getString("row.name", houseIndex));
            rows.add(row);
            houses.add(row);
            House column = new House(messageBundle.getString("column.name", houseIndex));
            columns.add(column);
            houses.add(column);
            if (createAction == CreateAction.GENERATE
                    || options.getBlockType() == Options.BlockType.RECTANGULAR) {
                House block = new House(messageBundle.getString("block.name", houseIndex));
                blocks.add(block);
                houses.add(block);
            }
        }

        // If using diagonals, create those houses.
        if (options.isUsingDiagonals()) {
            House diagonal1 = new House(messageBundle.getString("diagonal.\\"));
            diagonals.add(diagonal1);
            houses.add(diagonal1);
            House diagonal2 = new House(messageBundle.getString("diagonal./"));
            diagonals.add(diagonal2);
            houses.add(diagonal2);
        }
    }

    /**
     * Creates the cells and add them to their respective houses.
     *
     * @param blockIndexes  Array containing the block index for each <code>Cell</code>.
     */
    private void createCells(final int[][] blockIndexes) {
        Options options = Options.getInstance();
        Options.CreateAction createAction = options.getCreateAction();

        for (int row = 0; row < gridSize; row++) {
            for (int column = 0; column < gridSize; column++) {
                Cell cell = new Cell(gridSize, column, row);
                workGrid.add(cell);
                ((House) rows.get(row)).addCell(cell);
                ((House) columns.get(column)).addCell(cell);
                int blockIndex = blockIndexes[row][column];
                if (createAction == CreateAction.GENERATE
                        || options.getBlockType() == Options.BlockType.RECTANGULAR) {
                    ((House) blocks.get(blockIndex)).addCell(cell);
                    cell.setBlockIndex(blockIndex);
                    cell.setStateAndValue(CellState.UNSOLVED, 0, null);
                } else {
                    cell.setStateAndValue(CellState.UNASSIGNED, 0, null);
                }
                if (options.isUsingDiagonals()) {
                    if (row == column) {
                        House diagonal1 = (House) diagonals.get(0);
                        diagonal1.addCell(cell);
                    }
                    if (row + column + 1 == gridSize) {
                        House diagonal2 = (House) diagonals.get(1);
                        diagonal2.addCell(cell);
                    }
                }
            }
        }
    }

    /**
     * Gets the size of the puzzle grid.
     *
     * @return  The size of the puzzle grid (i. e., the number of cells horizontally and
     *          vertically).
     */
    public int getGridSize() {
        return gridSize;
    }

    /**
     * Gets a reference to the original puzzle grid.
     *
     * @return  A reference to the original puzzle grid.
     */
    public int[] getOriginalPuzzle() {
        return originalPuzzle;
    }

    /**
     * Gets the <code>Cell</code> at the specified location.
     *
     * @param row     The Y coordinate of a <code>Cell</code>.
     * @param column  The X coordinate of a <code>Cell</code>.
     * @return        The <code>Cell</code> at the specified location.
     */
    public final Cell getCellAt(final int row, final int column) {
        return (Cell) workGrid.get(row * gridSize + column);
    }

    /**
     * Gets an <code>Iterator</code> over all <code>Cell</code>s.
     *
     * @return  An <code>Iterator</code> over all <code>Cell</code>s.
     */
    public Iterator getAllCells() {
        return workGrid.iterator();
    }

    /**
     * Gets an <code>Iterator</code> over all <code>House</code>s.
     *
     * @return  An <code>Iterator</code> over all <code>House</code>s.
     */
    public Iterator getAllHouses() {
        return houses.iterator();
    }

    /**
     * Gets an <code>Iterator</code> over all rows.
     *
     * @return  An <code>Iterator</code> over all rows.
     */
    public Iterator getAllRows() {
        return rows.iterator();
    }

    /**
     * Gets an <code>Iterator</code> over all columns.
     *
     * @return  An <code>Iterator</code> over all columns.
     */
    public Iterator getAllColumns() {
        return columns.iterator();
    }

    /**
     * Adds a block to this sudoku.
     *
     * @param block  The block to be added.
     */
    public void addBlock(final House block) {
        blocks.add(block);
        houses.add(block);
    }

    /**
     * Gets an <code>Iterator</code> over all blocks.
     *
     * @return  An <code>Iterator</code> over all blocks.
     */
    public Iterator getAllBlocks() {
        return blocks.iterator();
    }

    /**
     * Gets the block with the specified index.
     *
     * @param blockIndex  The index of a block.
     * @return            The block with the specified index.
     */
    public House getBlock(final int blockIndex) {
        return (House) blocks.get(blockIndex);
    }

    /**
     * Gets an <code>Iterator</code> both diagonals.
     *
     * @return  An <code>Iterator</code> over both diagonals.
     */
    public Iterator getBothDiagonals() {
        return diagonals.iterator();
    }

    /**
     * Gets whether this puzzle has been solved.
     *
     * @return  <code>true</code> if every <code>Cell</code> contains a value. Otherwise,
     *          <code>false</code>.
     */
    public boolean isSolved() {
        boolean solved = true;

        Iterator iterator = workGrid.iterator();
        while (iterator.hasNext()) {
            Cell cell = (Cell) iterator.next();
            if (cell.getState() == CellState.UNSOLVED) {
                solved = false;
                break;
            }
        }

        return solved;
    }

    /**
     * Gets the index of the block that contains a specified <code>Cell</code>.
     *
     * @param column  The X coordinate of a <code>Cell</code>.
     * @param row     The Y coordinate of a <code>Cell</code>.
     * @return        The number of the block that contains the specified <code>Cell</code>.
     */
    public int getBlockIndex(final int column, final int row) {
        return getCellAt(row, column).getBlockIndex();
    }

    /**
     * Gets the buddies of a specified <code>Cell</code>.
     *
     * @param cell  A <code>Cell</code>.
     * @return      A collection containing all of the <code>Cell</code>'s buddies (i.e., each
     *              <code>Cell</code> that shares a <code>House</code> with the specified
     *              <code>Cell</code>).
     */
    public Set getBuddies(final Cell cell) {
        Set buddies = new HashSet();

        Iterator iterator = houses.iterator();
        while (iterator.hasNext()) {
            House house = (House) iterator.next();
            if (!house.containsUnsolved(cell)) {
                continue;
            }

            Iterator potentialBuddies = house.getAllCells();
            while (potentialBuddies.hasNext()) {
                buddies.add(potentialBuddies.next());
            }
        }

        buddies.remove(cell);
        return buddies;
    }

    /**
     * Gets the unsolved buddies of a specified <code>Cell</code>.
     *
     * @param cell  A <code>Cell</code>.
     * @return      A collection containing all of the cell's unsolved buddies (i.e., each unsolved
     *              <code>Cell</code> that shares a house with the specified <code>Cell</code>).
     */
    public Set getUnsolvedBuddies(final Cell cell) {
        Set buddies = new HashSet();

        Iterator iterator = houses.iterator();
        while (iterator.hasNext()) {
            House house = (House) iterator.next();
            if (!house.containsUnsolved(cell)) {
                continue;
            }

            Iterator unsolvedBuddies = house.getUnsolvedCells();
            while (unsolvedBuddies.hasNext()) {
                buddies.add(unsolvedBuddies.next());
            }
        }

        buddies.remove(cell);
        return buddies;
    }
}
