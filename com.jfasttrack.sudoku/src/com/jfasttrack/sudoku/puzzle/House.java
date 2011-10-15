/*
  House.java

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.jfasttrack.dlx.ColumnHeader;
import com.jfasttrack.dlx.Node;
import com.jfasttrack.sudoku.dlx.SudokuSolver;
import com.jfasttrack.sudoku.step.ValuePlacementStep;


/**
 * A <code>House</code> is a type of constraint. A standard 9x9 sudoku has 27 houses: 9 rows, 9
 * columns, and 9 blocks. Each <code>Cell</code> in a <code>House</code> must have a different
 * value.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class House implements Cell.ValueListener {

    /** The collection of <code>Cell</code>s in this <code>House</code>. */
    private final Set allCells = new HashSet();

    /** The <code>Cell</code>s in this <code>House</code> that do not yet contain a value. */
    private final Set unsolvedCells = new HashSet();

    /** The name of this <code>House</code>. */
    private final String name;

    /**
     * Constructs a <code>House</code>.
     *
     * @param name  The name of this <code>House</code>.
     */
    public House(final String name) {
        this.name = name;
    }

    /**
     * Adds a <code>Cell</code> to this <code>House</code>.
     *
     * @param cell  The <code>Cell</code> to be added.
     */
    public void addCell(final Cell cell) {
        allCells.add(cell);
        unsolvedCells.add(cell);

        // This house will listen for changes to the cell's value.
        cell.addListener(this);
    }

    /**
     * Gets every <code>Cell</code> in this <code>House</code>.
     *
     * @return  An <code>Iterator</code> over all of the <code>Cell</code>s in this
     *          <code>House</code>.
     */
    public Iterator getAllCells() {
        return allCells.iterator();
    }

    /**
     * Gets the number of <code>Cell</code> that do not contain a value.
     *
     * @return  The number of <code>Cell</code> that do not contain a value.
     */
    public int getNumberOfUnsolvedCells() {
        return unsolvedCells.size();
    }

    /**
     * Gets each <code>Cell</code> that does not contain a value.
     *
     * @return  An <code>Iterator</code> over all of the <code>Cell</code>s that do not contain a
     *          value in this <code>House</code>.
     */
    public Iterator getUnsolvedCells() {
        return unsolvedCells.iterator();
    }

    /**
     * Gets a collection containing each unsolved <code>Cell</code> that has the specified value
     * as a candidate.
     *
     * @param candidateValue  A value that may be a candidate of some <code>Cell</code>s in this
     *                        <code>House</code>.
     * @return                A collection containing each unsolved <code>Cell</code> that has the
     *                        specified value as a candidate.
     */
    public Set getCellsWithCandidate(final int candidateValue) {
        Set cells = new HashSet();

        Iterator iterator = unsolvedCells.iterator();
        while (iterator.hasNext()) {
            Cell cell = (Cell) iterator.next();
            if (cell.hasCandidate(candidateValue)) {
                cells.add(cell);
            }
        }

        return cells;
    }

    /**
     * Gets whether a <code>Cell</code> is listed as an unsolved cell in this <code>House</code>.
     *
     * @param cell  A <code>Cell</code> to be tested.
     * @return      <code>true</code> if the <code>Cell</code> is listed as an unsolved cell in
     *              this <code>House</code>. Otherwise, <code>false</code>.
     */
    public boolean containsUnsolved(final Cell cell) {
        return unsolvedCells.contains(cell);
    }

    /**
     * Gets the name of this <code>House</code>.
     *
     * @return  The name of this <code>House</code>.
     */
    public String getName() {
        return name;
    }

    /**
     * Constructs the dancing links nodes that will be used to solve the sudoku that contains this
     * <code>House</code>.
     *
     * @param solver  The <code>SudokuSolver</code> that will solve the sudoku that contains this
     *                <code>House</code>.
     */
    public final void createDlxNodes(final SudokuSolver solver) {
        int gridSize = allCells.size();

        /*
         * The dancing links matrix will contain one column for each value in this house. Each
         * column represents the placement of a value in the house. A house is "covered" when it
         * contains each value exactly once.
         */

        // Construct the column headers.
        ColumnHeader[] columnHeader = new ColumnHeader[gridSize + 1];
        for (int value = 1; value <= gridSize; value++) {
            columnHeader[value] = solver.createColumnHeader();
        }

        // Construct the nodes.
        for (int value = 1; value <= gridSize; value++) {
            Iterator cells = allCells.iterator();

            while (cells.hasNext()) {
                Cell cell = (Cell) cells.next();

                int row = cell.getRow();
                int column = cell.getColumn();
                int rowIndex = (row * gridSize + column) * gridSize + value - 1;
                createAndLinkOneNode(solver, rowIndex, columnHeader[value]);
            }
        }
    }

    /**
     * Creates a <code>Node</code> and adds it to the dancing links matrix.
     *
     * @param solver        The <code>SudokuSolver</code> that will solve the sudoku that contains
     *                      this <code>House</code>.
     * @param rowIndex      The index of a row in the puzzle grid.
     * @param columnHeader  The header of the column that will contain the new <code>Node</code>.
     */
    private static void createAndLinkOneNode(
            final SudokuSolver solver,
            final int rowIndex,
            final ColumnHeader columnHeader) {

        // Create the new node.
        Node node = new Node();
        node.applicationData = rowIndex;

        // Connect it to its row.
        Node rowHeader = solver.getRowHeader(rowIndex);
        node.left = rowHeader.left;
        node.right = rowHeader;
        rowHeader.left.right = node;
        rowHeader.left = node;

        // Connect it to its column.
        columnHeader.append(node);
    }

    /**
     * Called whenever the value of a <code>Cell</code> in this <code>House</code> is placed or
     * removed. Updates cells as needed.
     *
     * @param cell  The <code>Cell</code> whose value has been changed.
     * @param step  The <code>Step</code> that resulted in this value being changed.
     *              <code>null</code> if there is no <code>Step</code>.
     */
    public void valueChanged(final Cell cell, final ValuePlacementStep step) {
        if (cell.getState() == CellState.UNSOLVED) {

            // A value has been removed from this cell.
            unsolvedCells.add(cell);
        } else {

            /*
             * The value of this cell has been set. Remove this value as a
             * candidate from every other cell in this house.
             */
            int value = cell.getValue();
            Iterator cells = allCells.iterator();
            while (cells.hasNext()) {
                Cell buddy  = (Cell) cells.next();
                if (buddy != cell && buddy.hasCandidate(cell.getValue())) {
                    buddy.removeCandidate(value);
                    if (step != null) {
                        step.addAffectedCell(buddy);
                    }
                }
            }

            // And remove this cell from the collection of unsolved cells.
            unsolvedCells.remove(cell);
        }
    }
}
