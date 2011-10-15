/*
  Options.java

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

import java.util.StringTokenizer;


/**
 * This class contains the options in effect when creating a new sudoku.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public final class Options {

    /** The action taken when a sudoku is created. */
    public static class CreateAction {

        /** Create an empty puzzle grid. */
        public static final CreateAction CREATE_EMPTY = new CreateAction();

        /** Load a sudoku from a <code>String</code>. */
        public static final CreateAction LOAD = new CreateAction();

        /** Generate a random sudoku. */
        public static final CreateAction GENERATE = new CreateAction();
    }

    /** The type of blocks in the new sudoku. */
    public static class BlockType {

        /** The sudoku has rectangular blocks. */
        public static final BlockType RECTANGULAR = new BlockType();

        /** The sudoku has jigsaw blocks. */
        public static final BlockType JIGSAW = new BlockType();
    }

    /** The singleton instance of this class. */
    private static final Options INSTANCE = new Options();

    /** The action taken when a sudoku is created. */
    private CreateAction createAction;

    /** The size of the sudoku grid. */
    private int gridSize;

    /** The type of blocks in the new sudoku. */
    private BlockType blockType;

    /** The width of each rectangular sudoku block. */
    private int blockWidth;

    /** The height of each rectangular sudoku block. */
    private int blockHeight;

    /** Tells whether the sudoku has diagonal houses. */
    private boolean usingDiagonals;

    /** Constructs an <code>Options</code>. */
    private Options() {
        setDefaults();
    }

    /** Sets the default value for each option. */
    public void setDefaults() {
        gridSize       = 9;
        blockType      = BlockType.RECTANGULAR;
        blockWidth     = 3;
        blockHeight    = 3;
        usingDiagonals = false;
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return  The singleton instance of this class.
     */
    public static Options getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the action taken when a sudoku is created.
     *
     * @param createAction  The action taken when a sudoku is created.
     */
    public void setCreateAction(final CreateAction createAction) {
        this.createAction = createAction;
    }

    /**
     * Gets the action taken when a sudoku is created.
     *
     * @return  The action taken when a sudoku is created.
     */
    public CreateAction getCreateAction() {
        return createAction;
    }

    /**
     * Sets the size of the sudoku grid.
     *
     * @param gridSize  The size of the sudoku grid.
     */
    public void setGridSize(final int gridSize) {
        this.gridSize = gridSize;
    }

    /**
     * Gets the size of the sudoku grid.
     *
     * @return  The size of the sudoku grid.
     */
    public int getGridSize() {
        return gridSize;
    }

    /**
     * Sets the type of blocks in the new sudoku.
     *
     * @param blockType  The type of blocks in the new sudoku.
     */
    public void setBlockType(final BlockType blockType) {
        this.blockType = blockType;
    }

    /**
     * Gets the type of blocks in the new sudoku.
     *
     * @return  The type of blocks in the new sudoku.
     */
    public BlockType getBlockType() {
        return blockType;
    }

    /**
     * Sets the width of each rectangular sudoku block.
     *
     * @param blockWidth  The width of each rectangular sudoku block.
     */
    public void setBlockWidth(final int blockWidth) {
        this.blockWidth = blockWidth;
    }

    /**
     * Gets the width of each rectangular sudoku block.
     *
     * @return  The width of each rectangular sudoku block.
     */
    public int getBlockWidth() {
        return blockWidth;
    }

    /**
     * Sets the height of each rectangular sudoku block.
     *
     * @param blockHeight  The height of each rectangular sudoku block.
     */
    public void setBlockHeight(final int blockHeight) {
        this.blockHeight = blockHeight;
    }

    /**
     * Gets the height of each rectangular sudoku block.
     *
     * @return  The height of each rectangular sudoku block.
     */
    public int getBlockHeight() {
        return blockHeight;
    }

    /**
     * Sets whether the sudoku has diagonal houses.
     *
     * @param usingDiagonals  Whether the sudoku has diagonal houses.
     */
    public void setUsingDiagonals(final boolean usingDiagonals) {
        this.usingDiagonals = usingDiagonals;
    }

    /**
     * Gets whether the sudoku has diagonal houses.
     *
     * @return  Whether the sudoku has diagonal houses.
     */
    public boolean isUsingDiagonals() {
        return usingDiagonals;
    }

    /**
     * Loads the <code>Options</code> from a <code>String</code>.
     *
     * @param line  A <code>String</code> describing a sudoku.
     */
    public void load(final String line) {
        setDefaults();

        StringTokenizer st = new StringTokenizer(line);
        while (st.hasMoreElements()) {
            String token = st.nextToken();
            if (token.startsWith("size=")) {
                gridSize = Integer.parseInt(token.substring("size=".length()));
            } else if (token.startsWith("jigsaw")) {
                blockType = BlockType.JIGSAW;
            } else if (token.startsWith("rectangular:")) {
                StringTokenizer st2 = new StringTokenizer(token, ":");
                st2.nextToken();
                blockHeight = Integer.parseInt(st2.nextToken());
                blockWidth  = Integer.parseInt(st2.nextToken());
            } else if (token.startsWith("diagonals")) {
                usingDiagonals = true;
            }
        }
    }

    /**
     * Creates a <code>String</code> representation of the <code>Options</code>.
     *
     * @return  A <code>String</code> representation of the <code>Options</code>.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(64);

        if (gridSize != 9) {
            buffer.append("size=" + gridSize + " ");
        }
        if (blockType == BlockType.JIGSAW) {
            buffer.append("jigsaw ");
        } else if (blockWidth != 3 || blockHeight != 3) {
            buffer.append("rectangular:" + blockHeight + ":" + blockWidth + " ");
        }
        if (usingDiagonals) {
            buffer.append("diagonals ");
        }

        if (buffer.length() > 0) {
            buffer.insert(0, ": ");
        }

        return buffer.toString();
    }
}
