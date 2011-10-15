/*
  CandidateRemovalStep.java

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

import java.util.Iterator;
import java.util.Set;

import com.jfasttrack.sudoku.puzzle.Cell;


/**
 * A <code>CandidateRemovalStep</code> is a <code>Step</code> that removes
 * a candidate value from one or more <code>Cell</code>s.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class CandidateRemovalStep extends AbstractStep {

    /** The candidate value removed. */
    private final int value;

    /**
     * Constructs a <code>CandidateRemovalStep</code>.
     *
     * @param smallHint  A general description of this <code>Step</code>.
     * @param bigHint    A detailed description, telling where candidate(s) can be removed and
     *                   which solving technique is used.
     * @param cells      The <code>Cell</code>s from which the candidate value is removed.
     * @param value      The candidate value removed from the <code>Cell</code>s.
     */
    public CandidateRemovalStep(
            final String smallHint,
            final String bigHint,
            final Set cells,
            final int value) {
        super(smallHint, bigHint);

        Iterator iterator = cells.iterator();
        while (iterator.hasNext()) {
            addChangedCell((Cell) iterator.next());
        }

        this.value = value;
    }

    /**
     * Constructs a <code>CandidateRemovalStep</code>.
     *
     * @param cells  The <code>Cell</code>s from which the candidate value is removed.
     * @param value  The candidate value removed from the <code>Cell</code>s.
     */
    public CandidateRemovalStep(final Set cells, final int value) {
        this("", "", cells, value);
    }

    /**
     * Undoes this <code>CandidateRemovalStep</code>. Restores the value as a candidate to the
     * <code>Cell</code> from which it was removed.
     */
    public void undo() {
        if (getNumberOfChangedCells() == 1) {
            Cell changedCell = (Cell) getChangedCells().next();
            changedCell.addCandidate(value);
        }
    }

    /** Redoes this <code>CandidateRemovalStep</code> by removing the candidate value. */
    public void redo() {
        Cell cell = (Cell) getChangedCells().next();
        cell.removeCandidate(value);
    }
}
