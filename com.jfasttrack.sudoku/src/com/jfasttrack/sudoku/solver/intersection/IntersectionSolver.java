/*
  IntersectionSolver.java

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

package com.jfasttrack.sudoku.solver.intersection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.jfasttrack.sudoku.puzzle.AbstractPuzzleModel;
import com.jfasttrack.sudoku.puzzle.Cell;
import com.jfasttrack.sudoku.puzzle.House;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.solver.ISolver;
import com.jfasttrack.sudoku.step.AbstractStep;
import com.jfasttrack.sudoku.step.CandidateRemovalStep;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * This solver looks for intersections in a sudoku.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class IntersectionSolver implements ISolver {

//TODO: Do I want to split this into 2 solvers?

    /**
     * Looks for intersections in a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        A <code>Step</code> describing an intersection. <code>null</code> if the
     *                sudoku does not contain an intersection.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        AbstractStep step = null;

        // Copy the collection of houses into a local list.
        List houses = new ArrayList();
        Iterator allHouses = puzzle.getAllHouses();
        while (allHouses.hasNext()) {
            houses.add(allHouses.next());
        }

    outerLoop:
        for (int value = 1; value < puzzle.getGridSize(); value++) {

            // Consider each pair of houses that have cells in common.
            for (int i1 = 0; i1 < houses.size() - 1; i1++) {
                House house1 = (House) houses.get(i1);
                Set house1Cells = house1.getCellsWithCandidate(value);

                for (int i2 = i1 + 1; i2 < houses.size(); i2++) {
                    House house2 = (House) houses.get(i2);
                    Set house2Cells = house2.getCellsWithCandidate(value);

                    // See which cells the houses have in common.
                    Set intersection = new HashSet(house1Cells);
                    intersection.retainAll(house2Cells);

                    /*
                     * If size is 1, this is technically an intersection. However, it is also a
                     * single, so I prefer not to identify it as an intersection here.
                     */
                    if (intersection.size() <= 1) {
                        continue;
                    }

                    step = compareHouses(house1Cells, house2Cells, intersection, value);
                    if (step != null) {
                        break outerLoop;
                    }
                    step = compareHouses(house2Cells, house1Cells, intersection, value);
                    if (step != null) {
                        break outerLoop;
                    }
                }    // for i2
            }    // for i1
        }    // for value

        return step;
    }

    /**
     * Compares the cells of two houses to see if there is an intersection.
     *
     * @param house1Cells   The <code>Cell</code>s of a <code>House</code>.
     * @param house2Cells   The <code>Cell</code>s of another <code>House</code>.
     * @param intersection  The <code>Cell</code>s that are included in both <code>House</code>s.
     * @param value         The value being checked.
     * @return              A step toward the solution of a sudoku. <code>null</code> if this
     *                      solver does not apply to these parameters.
     */
    private static AbstractStep compareHouses(
            final Set house1Cells,
            final Set house2Cells,
            final Set intersection,
            final int value) {
        AbstractStep step = null;

        if (house1Cells.equals(intersection) && !house2Cells.equals(intersection)) {
            step = createStep(house2Cells, intersection, value);
        } else if (house2Cells.equals(intersection) && !house1Cells.equals(intersection)) {
            step = createStep(house1Cells, intersection, value);
        }

        return step;
    }

    /**
     * Creates a <code>Step</code> describing an intersection.
     *
     * @param cellsToChange  The <code>Cell</code>s to be changed.
     * @param intersection   The <code>Cell</code>s common to both <code>House</code>s in the
     *                       intersection.
     * @param value          The value for which an intersection was found.
     * @return               A <code>Step</code> describing an intersection.
     */
    private static AbstractStep createStep(
            final Set cellsToChange,
            final Set intersection,
            final int value) {

        MessageBundle messageBundle = MessageBundle.getInstance();
        String smallHint = messageBundle.getString("solver.intersection.small.hint");
        String bigHint = messageBundle.getString(
                "solver.intersection.big.hint",
                new String[] {
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                }
        );

        Set cellsToBeChanged = new HashSet();
        cellsToBeChanged.addAll(cellsToChange);
        cellsToBeChanged.removeAll(intersection);
        CandidateRemovalStep step =
                new CandidateRemovalStep(smallHint, bigHint, cellsToBeChanged, value);
        Iterator iterator = intersection.iterator();
        while (iterator.hasNext()) {
            step.addExplainingCell((Cell) iterator.next());
        }

        return step;
    }

    /**
     * Gets the text for the menu item used to invoke this solver.
     *
     * @return  The text for the menu item used to invoke this solver.
     */
    public String getNameOfMenuItem() {
        return MessageBundle.getInstance().getString("solver.intersection.menu.item");
    }

    /**
     * Gets the message to be displayed when this solver cannot be applied.
     *
     * @return  The message to be displayed when this solver cannot be applied.
     */
    public String getSolverNotApplicableMessage() {
        return MessageBundle.getInstance().getString("solver.intersection.not.applicable");
    }
}
