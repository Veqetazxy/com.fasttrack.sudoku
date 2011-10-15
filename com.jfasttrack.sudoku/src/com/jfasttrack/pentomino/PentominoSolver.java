/*
  PentominoSolver.java

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

package com.jfasttrack.pentomino;

import com.jfasttrack.dlx.AbstractDLXSolver;
import com.jfasttrack.dlx.Node;


/**
 * This class uses Knuth's Algorithm X (dancing links) to solve pentomino puzzles.
 * <p>
 * Each row of the matrix represents a move (i. e., the placment of a pentomino into the grid.
 * The number of rows will vary depending on the width and height of the grid.
 * <p>
 * The dancing links matrix will have 72 columns, regardless of the dimensions of the puzzle. Each
 * of the first 12 columns represents a single pentomino shape; This ensures that each pentomino is
 * placed into the grid once. The remaining 60 columns each represent one square of the puzzle grid.
 * <p>
 * An "exact cover" will use each piece once and each square once.
 * <p>
 * Each time the matrix is completely "covered," a solution is constructed from the rows that make
 * up the cover.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
final class PentominoSolver extends AbstractDLXSolver {

    /** The number of squares in the grid to be filled with pentominoes (60). */
    private static final int NUMBER_OF_SQUARES =
            Pentomino.PENTOMINO_SIZE * Pentomino.ALL_PENTOMINOES.length;

    /** The width of the grid to be filled. */
    private final int gridWidth;

    /** The height of the grid to be filled. */
    private final int gridHeight;

    /**
     * Constructs a <code>PentominoSolver</code>.
     *
     * @param gridWidth   The width of the grid to be filled.
     * @param gridHeight  The height of the grid to be filled.
     */
    PentominoSolver(final int gridWidth, final int gridHeight) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;

        createNodes();
    }

    /** Creates the dancing links nodes that will be used to solve a pentomino puzzle. */
    void createNodes() {
        createColumnHeaders(Pentomino.ALL_PENTOMINOES.length + gridWidth * gridHeight);

        // Load every possible piece placement into the dancing links matrix.
        for (int pieceIndex = 0; pieceIndex < Pentomino.ALL_PENTOMINOES.length; pieceIndex++) {
            for (int orientation = 0;
                    orientation < Pentomino.ALL_PENTOMINOES[pieceIndex].length;
                    orientation++) {
                Pentomino piece = Pentomino.ALL_PENTOMINOES[pieceIndex][orientation];
            rowLoop:
                for (int row = 0; row < gridHeight; row++) {
                    for (int i = 0; i < Pentomino.PENTOMINO_SIZE; i++) {
                        if (row + piece.getYOffset(i) >= gridHeight) {
                            break rowLoop;
                        }
                    }
                columnLoop:
                    for (int column = 0; column < gridWidth; column++) {
                        for (int i = 0; i < Pentomino.PENTOMINO_SIZE; i++) {
                            if (column + piece.getXOffset(i) >= gridWidth) {
                                break columnLoop;
                            }
                        }

                        createRow(pieceIndex, orientation, row, column);
                    }
                }
            }
        }
    }

    /**
     * Creates one row of the dancing links matrix. Each row represents the placement of a pentomino
     * at a specific location in the puzzle grid.
     *
     * @param pieceIndex   The index of a piece.
     * @param orientation  The orientation of a piece.
     * @param row          The index of a row in the puzzle grid.
     * @param column       The index of a column in the puzzle grid.
     */
    void createRow(
            final int pieceIndex,
            final int orientation,
            final int row,
            final int column) {

        int pieceIdentifier = pieceIndex * Pentomino.ALL_PENTOMINOES.length + orientation;
        int location = row * gridWidth + column;

        /*
         * This is the value that will be stored with each node in the new row. Each time a solution
         * is reported, the node values will be used to tell which moves led to that solution.
         */
        int nodeValue = pieceIdentifier * NUMBER_OF_SQUARES + location;

        Pentomino piece = Pentomino.ALL_PENTOMINOES[pieceIndex][orientation];

        // Create the header for the new row.
        Node rowHeader = new Node();
        rowHeader.applicationData = nodeValue;
        addRowHeader(rowHeader);

        // Connect the row header to its column.
        rowHeader.columnHeader = getColumnHeader(pieceIndex);
        rowHeader.columnHeader.append(rowHeader);

        // Create and connect the rest of the nodes for this row.
        for (int i = 0; i < Pentomino.PENTOMINO_SIZE; i++) {
            Node node = new Node();
            node.applicationData = nodeValue;

            // Connect the node to its row.
            node.left = rowHeader.left;
            node.right = rowHeader;
            rowHeader.left.right = node;
            rowHeader.left = node;

            // Connect the node to its column header.
            int columnIndex =
                    Pentomino.ALL_PENTOMINOES.length
                    + (row + piece.getYOffset(i)) * gridWidth
                    + column + piece.getXOffset(i);
            getColumnHeader(columnIndex).append(node);
        }
    }
}
