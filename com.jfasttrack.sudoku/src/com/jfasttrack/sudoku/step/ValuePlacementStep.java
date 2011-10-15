/*
  ValuePlacementStep.java

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

package com.jfasttrack.sudoku.step;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.jfasttrack.sudoku.puzzle.Cell;
import com.jfasttrack.sudoku.puzzle.CellState;


/**
 * A <code>ValuePlacementStep</code> is a step that sets the <code>value</code> of a
 * <code>Cell</code>.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class ValuePlacementStep extends AbstractStep {

    /**
     * The <code>Cell</code>'s candidates before the <code>value</code> is placed, saved here in
     * case this step is undone.
     */
    private final BitSet originalCandidates;

    /** The value to be placed into the <code>Cell</code>. */
    private final int value;

    /**
     * The other <code>Cell</code>s affected by this <code>Step</code>. These are the
     * <code>Cell</code>'s buddies whose candidates include the value being placed.
     */
    private final Set affectedCells = new HashSet();

    /**
     * Constructs a <code>ValuePlacementStep</code>.
     *
     * @param smallHint  A general description of this <code>Step</code>.
     * @param bigHint    A detailed description, telling where a value can be placed, and which
     *                   solving technique is used.
     * @param cell       The <code>Cell</code> whose value is set.
     * @param value      The value placed into the <code>Cell</code>.
     */
    public ValuePlacementStep(
            final String smallHint,
            final String bigHint,
            final Cell cell,
            final int value) {
        super(smallHint, bigHint);

        addChangedCell(cell);
        this.value = value;

        originalCandidates = cell.getCandidates();
    }

    /**
     * Constructs a <code>ValuePlacementStep</code>.
     *
     * @param cell       The <code>Cell</code> whose value is set.
     * @param value      The value placed into the <code>Cell</code>.
     */
    public ValuePlacementStep(final Cell cell, final int value) {
        this("", "", cell, value);
    }

    /**
     * Adds a <code>Cell</code> to the collection of <code>Cell</code>s affected by this
     * <code>Step</code>.
     *
     * @param cell  The <code>Cell</code> to be added.
     */
    public void addAffectedCell(final Cell cell) {
        affectedCells.add(cell);
    }

    /**
     * Gets an <code>Iterator</code> over the collection of <code>Cell</code>s affected by this
     * <code>Step</code>.
     *
     * @return  An <code>Iterator</code> over the collection of <code>Cell</code>s affected by this
     *          <code>Step</code>.
     */
    public Iterator getAffectedCells() {
        return affectedCells.iterator();
    }

    /**
     * Undoes this <code>ValuePlacementStep</code>. Restores the original <code>Cell</code>'s state
     * and candidates. Restores the step's value as a candidate in the other <code>Cell</code>s
     * where it was removed.
     */
    public void undo() {

        Cell cell = (Cell) getChangedCells().next();

        // Restore the original cell's state and value.
        cell.setStateAndValue(CellState.UNSOLVED, 0, null);

        // Restore the original cell's candidates.
        int candidateValue = originalCandidates.nextSetBit(0);
        while (candidateValue > 0) {
            cell.addCandidate(candidateValue);
            candidateValue = originalCandidates.nextSetBit(candidateValue + 1);
        }

        // Restore candidates that this step removed in other cells.
        Iterator iterator = affectedCells.iterator();
        while (iterator.hasNext()) {
            Cell otherCell = (Cell) iterator.next();
            otherCell.addCandidate(value);
        }
    }

    /** Redoes this <code>ValuePlacementStep</code>. */
    public void redo() {
        Cell cell = (Cell) getChangedCells().next();
        cell.setStateAndValue(CellState.SOLVED, value, this);
    }
}
