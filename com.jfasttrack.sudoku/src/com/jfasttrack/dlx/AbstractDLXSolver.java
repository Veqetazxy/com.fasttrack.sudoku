/*
  DancingLinksSolver.java

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

package com.jfasttrack.dlx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


/**
 * This class implements a solver for an exact cover problem using Donald Knuth's Algorithm X
 * (dancing links).
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public abstract class AbstractDLXSolver {

    /** The collection of <code>ColumnHeader</code>s. */
    private final List columnHeaders = new ArrayList();

    /** A collection of references to the first <code>Node</code> in each row. */
    private final List rowHeaders = new ArrayList();

    /** The root <code>Node</code> (master header) for the entire dancing links matrix. */
    private final ColumnHeader rootNode = new ColumnHeader();

    /** The steps that have been taken toward the solution of an exact cover problem. */
    private final Stack solutionNodes = new Stack();

    /** The listeners to be notified whenever a solution is found. */
    private final List solutionListeners = new ArrayList();

    /** Tells whether the solver is finished generating solutions. */
    private boolean done;

    /**
     * Creates the <code>ColumnHeader</code>s for an empty sparse matrix that will be used to solve
     * an exact cover problem.
     *
     * @param numberOfColumns  The number of columns in the matrix.
     */
    protected void createColumnHeaders(final int numberOfColumns) {
        for (int i = 0; i < numberOfColumns; i++) {
            ColumnHeader columnHeader = new ColumnHeader();
            columnHeader.left = rootNode.left;
            columnHeader.right = rootNode;
            rootNode.left.right = columnHeader;
            rootNode.left = columnHeader;
            columnHeaders.add(columnHeader);
        }
    }

    /**
     * Gets the root node of the dancing links matrix.
     *
     * @return  The root node of the dancing links matrix.
     */
    protected ColumnHeader getRoot() {
        return rootNode;
    }

    /**
     * Adds a row header to the dancing links matrix.
     *
     * @param rowHeader  The row header to be added.
     */
    protected void addRowHeader(final Node rowHeader) {
        rowHeaders.add(rowHeader);
    }

    /**
     * Gets the first <code>Node</code> in the specified row.
     *
     * @param rowIndex  The index of a row in the dancing links matrix.
     * @return          The first <code>Node</code> in the specified row.
     */
    public Node getRowHeader(final int rowIndex) {
        return (Node) rowHeaders.get(rowIndex);
    }

    /**
     * Adds a column header to the dancing links matrix.
     *
     * @param columnHeader  The column header to be added.
     */
    protected void addColumnHeader(final ColumnHeader columnHeader) {
        columnHeaders.add(columnHeader);
    }

    /**
     * Gets the <code>ColumnHeader</code> of the specified column.
     *
     * @param columnIndex  The index of a column in the dancing links matrix.
     * @return             The <code>ColumnHeader</code> of the specified column.
     */
    protected ColumnHeader getColumnHeader(final int columnIndex) {
        return (ColumnHeader) columnHeaders.get(columnIndex);
    }

    /**
     * Adds the specified row to the solution. Removes all affected columns
     * from further consideration.
     *
     * @param rowIndex  The index of the row to be added.
     */
    protected void addRowToSolution(final int rowIndex) {
        Node node = (Node) rowHeaders.get(rowIndex);
        do {
            coverColumn(node.columnHeader);
            node = node.right;
        } while (node != rowHeaders.get(rowIndex));
        solutionNodes.push(node);
    }

    /**
     * Removes every row from the solution of an exact cover problem. (This is usually done when
     * preparing the matrix to solve another problem.)
     */
    protected void removeAllRowsFromSolution() {
        while (!solutionNodes.isEmpty()) {
            int rowIndex = ((Node) solutionNodes.pop()).applicationData;
            Node node = ((Node) rowHeaders.get(rowIndex)).left;
            do {
                uncoverColumn(node.columnHeader);
                node = node.left;
            } while (node != rowHeaders.get(rowIndex));
            uncoverColumn(node.columnHeader);
        }
    }

    /**
     * Gets the header of the column that contains the fewest nodes. This is the column that will
     * be covered next.
     * <p>
     * Columns can be covered in any order without affecting the solution(s) to exact cover
     * problems. Selection of the shortest column minimizes the branching factor, making the
     * algorithm run much faster.
     *
     * @return  The header of the column that has the fewest nodes.
     */
    private ColumnHeader getHeaderOfShortestColumn() {

        /*
         * Pseudocode (copied from Knuth):
         *   set s <- infinity.
         *   for each j <- R[h], R[R[h]], ..., while j != h,
         *       if S[j] < s    set c <- j and s <- S[j].
         */

        int lengthOfShortest = Integer.MAX_VALUE;
        ColumnHeader headerOfShortest = null;

        for (ColumnHeader header = (ColumnHeader) rootNode.right;
                header != rootNode;
                header = (ColumnHeader) header.right) {
            if (header.columnLength < lengthOfShortest) {
                lengthOfShortest = header.columnLength;
                headerOfShortest = header;
            }
        }

        return headerOfShortest;
    }

    /**
     * Removes a column and one or more rows from the matrix.
     * <p>
     * Removes the specified column from the header list and removes all rows in that list from the
     * other column lists they are in.
     *
     * @param header  The header of the column to be removed.
     */
    private static void coverColumn(final ColumnHeader header) {

        /*
         * Pseudocode (copied from Knuth):
         *   Set L[R[c]] <- L[c] and R[L[c]] <- R[c].
         *   For each i <- D[c], D[D[c]], ..., while i != c,
         *       for each j <- R[i], R[R[i]], ..., while j != i,
         *           set U[D[j]] <- U[j], D[U[j]] <- D[j],
         *           and set S[C[j]] <- S[C[j]] - 1.
         */

        header.right.left = header.left;
        header.left.right = header.right;

        for (Node i = header.down; i != header; i = i.down) {
            for (Node j = i.right; j != i; j = j.right) {
                j.down.up = j.up;
                j.up.down = j.down;
                j.columnHeader.columnLength--;
            }
        }
    }

    /**
     * Undoes a <code>coverColumn</code> operation by restoring a column and its associated row(s).
     *
     * @param header  The header of the column to be restored.
     */
    private static void uncoverColumn(final ColumnHeader header) {

        /*
         * Uncovering is done in exactly the reverse order of covering.
         *
         * Pseudocode (copied from Knuth):
         *   For each i <- U[c], U[U[c]], ..., while i != c,
         *       for each j <- L[i], L[L[i]], ..., while j != i,
         *           set S[C[j]] <- S[C[j]] + 1.
         *           and set U[D[j]] <- j, D[U[j]] <- j.
         *   Set L[R[c]] <- c and R[L[c]] <- c.
         */

        for (Node i = header.up; i != header; i = i.up) {
            for (Node j = i.left; j != i; j = j.left) {
                j.columnHeader.columnLength++;
                j.down.up = j;
                j.up.down = j;
            }
        }

        header.right.left = header;
        header.left.right = header;
    }

    /** Solves an exact cover problem using Algorithm X (dancing links). */
    public void solve() {

        /*
         * Knuth includes the search depth as a parameter called k. That
         * parameter is omitted here because we have no use for it.
         *
         * Pseudocode (copied from Knuth):
         *   If R[h] = h, print the current solution and return.
         *   Otherwise choose a column object c.
         *   For each r <- D[c], D[D[c]], ..., while r != c,
         *       set O[k] -> r;
         *       for each j <- R[r], R[R[r], ..., while j != r,
         *           cover column j;
         *       search(k + 1);
         *       set r <- O[k] and c <- C[r];
         *       for each j <- L[r], L[L[r]], ..., while j != r,
         */

        if (rootNode.right == rootNode) {
            reportSolution();
            return;
        }

        ColumnHeader header = getHeaderOfShortestColumn();
        coverColumn(header);

        Node r = header.down;
        while (r != header) {
            solutionNodes.push(r);
            for (Node j = r.right; j != r; j = j.right) {
                coverColumn(j.columnHeader);
            }
            if (!done) {
                solve();
            }

            /*r = (Node)*/ solutionNodes.pop();
            header = r.columnHeader;
            for (Node j = r.left; j != r; j = j.left) {
                uncoverColumn(j.columnHeader);
            }
            r = r.down;
        }

        uncoverColumn(header);
    }

    /**
     * Adds a listener to the collection of registered listeners. Each listener will be notified
     * whenever a solution to an exact cover problem is found.
     *
     * @param listener  The listener to be added.
     */
    public void addSolutionListener(final SolutionListener listener) {
        solutionListeners.add(listener);
    }

    /**
     * Notifies each registered listener that a solution has been found. Based on the return value
     * from each notification, sets an instance variable that tells the solver whether to continue
     * generating solutions.
     */
    private void reportSolution() {
        Iterator iterator = solutionListeners.iterator();
        while (iterator.hasNext()) {
            SolutionListener listener = (SolutionListener) iterator.next();
            done |= listener.solutionFound(solutionNodes);
        }
    }
}
