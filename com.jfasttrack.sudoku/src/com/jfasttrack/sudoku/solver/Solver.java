/*
  Solver.java

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

package com.jfasttrack.sudoku.solver;

import com.jfasttrack.sudoku.puzzle.AbstractPuzzleModel;
import com.jfasttrack.sudoku.solver.fish.FishFinder;
import com.jfasttrack.sudoku.solver.fish.XWingSolver;
import com.jfasttrack.sudoku.solver.intersection.IntersectionSolver;
import com.jfasttrack.sudoku.solver.leftovers.LeftoversSolver;
import com.jfasttrack.sudoku.solver.single.HiddenSingleSolver;
import com.jfasttrack.sudoku.solver.single.NakedSingleSolver;
import com.jfasttrack.sudoku.solver.subset.HiddenPairSolver;
import com.jfasttrack.sudoku.solver.subset.HiddenQuadSolver;
import com.jfasttrack.sudoku.solver.subset.HiddenTripletSolver;
import com.jfasttrack.sudoku.solver.subset.NakedPairSolver;
import com.jfasttrack.sudoku.solver.subset.NakedQuadSolver;
import com.jfasttrack.sudoku.solver.subset.NakedTripletSolver;
import com.jfasttrack.sudoku.step.AbstractStep;


/**
 * A <code>Solver</code> is an object that solves a sudoku. It invokes various specialized solvers
 * that use different methods to try to solve puzzles.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class Solver {

    /*
     * The order of solvers here is not important. The intent was to list solvers in order from the
     * simplest to the most complex. When the user asks for a hint, the solvers are run in the
     * order listed here until a hint is found.
     *
     * If the order of solvers is changed, the group indexes should be changed to match.
     */

    /** An instance of each available solver. */
    public static final ISolver[] SOLVERS = {
        new NakedSingleSolver(),
        new HiddenSingleSolver(),
        new IntersectionSolver(),
        new NakedPairSolver(),
        new NakedTripletSolver(),
        new NakedQuadSolver(),
        new HiddenPairSolver(),
        new HiddenTripletSolver(),
        new HiddenQuadSolver(),
        new LeftoversSolver(),
        new XWingSolver(),                      // X-wing
        new FishFinder(3),                      // Swordfish
        new FishFinder(4),                      // Jellyfish
        new FishFinder(5),                      // Squirmbag
    };

    /**
     * The index of the group to which each solver belongs. These groups are used in the solve menu.
     */
    public static final int[] GROUP_INDEX = {
        0, 0,
        1,
        2, 2, 2, 2, 2, 2,
        3,
        4, 4, 4, 4,
    };

    /** The name of each group of solvers. */
    public static final String[] GROUP_NAME = {
        "solver.single",
        "solver.intersection",
        "solver.subset",
        "solver.leftovers",
        "solver.fish",
    };

    /**
     * Gets the next <code>Step</code> in the solution of a sudoku.
     *
     * @param puzzle  The puzzle to be solved.
     * @return        The next <code>Step</code> in the solution of a sudoku.
     *                <code>null</code> is no solver can be applied.
     */
    public AbstractStep getNextStep(final AbstractPuzzleModel puzzle) {
        AbstractStep solutionStep = null;

        for (int i = 0; i < SOLVERS.length; i++) {
            solutionStep = SOLVERS[i].getNextStep(puzzle);
            if (solutionStep != null) {
                break;
            }
        }

        return solutionStep;
    }
}
