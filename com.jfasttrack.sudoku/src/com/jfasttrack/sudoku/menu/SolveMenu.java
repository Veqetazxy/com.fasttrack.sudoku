/*
  SolveMenu.java

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

package com.jfasttrack.sudoku.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.jfasttrack.dlx.Node;
import com.jfasttrack.dlx.SolutionListener;
import com.jfasttrack.sudoku.DancingLinksSudoku;
import com.jfasttrack.sudoku.dlx.SudokuSolver;
import com.jfasttrack.sudoku.puzzle.Cell;
import com.jfasttrack.sudoku.puzzle.CellState;
import com.jfasttrack.sudoku.solver.ISolver;
import com.jfasttrack.sudoku.solver.Solver;
import com.jfasttrack.sudoku.step.AbstractStep;
import com.jfasttrack.sudoku.ui.MessageBundle;
import com.jfasttrack.sudoku.ui.Settings;


/**
 * The solve menu provides a way to check the validity of a puzzle. It also
 * provides all hints.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class SolveMenu extends JMenu {

    /** Message bundle that holds all messages for this program. */
    private static final MessageBundle MESSAGE_BUNDLE = MessageBundle.getInstance();

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The program that owns this menu. */
    private final DancingLinksSudoku owner;

    /** The number of solutions found while checking a sudoku. */
    private int numberOfSolutions;

    /** A solution found while checking a sudoku. */
    private int[] solution;

    /**
     * Creates the solve menu.
     *
     * @param owner  The program that owns this menu.
     */
    public SolveMenu(final DancingLinksSudoku owner) {
        super(MESSAGE_BUNDLE.getString("menu.solve"));
        setMnemonic(MESSAGE_BUNDLE.getString("menu.solve.accelerator").charAt(0));

        this.owner = owner;

        JMenuItem checkMenuItem = new JMenuItem(MESSAGE_BUNDLE.getString("menu.solve.check"));
        checkMenuItem.setMnemonic(
                MESSAGE_BUNDLE.getString("menu.solve.check.accelerator").charAt(0));
        checkMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_K,
                InputEvent.CTRL_DOWN_MASK));
        checkMenuItem.addActionListener(new CheckerActionListener());
        add(checkMenuItem);

        addSeparator();

        JMenuItem getASmallHintMenuItem =
                new JMenuItem(MESSAGE_BUNDLE.getString("menu.solve.get.small.hint"));
        getASmallHintMenuItem.setMnemonic(MESSAGE_BUNDLE.getString(
                "menu.solve.get.small.hint.accelerator").charAt(0));
        getASmallHintMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H,
                InputEvent.CTRL_DOWN_MASK));
        getASmallHintMenuItem.addActionListener(new SmallHintActionListener());
        add(getASmallHintMenuItem);

        JMenuItem getABigHintMenuItem =
                new JMenuItem(MESSAGE_BUNDLE.getString("menu.solve.get.big.hint"));
        getABigHintMenuItem.setMnemonic(MESSAGE_BUNDLE.getString(
                "menu.solve.get.big.hint.accelerator").charAt(0));
        getABigHintMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H,
                InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
        getABigHintMenuItem.addActionListener(new BigHintActionListener());
        add(getABigHintMenuItem);

        addSeparator();

        JMenu[] solverGroupMenu = new JMenu[Solver.GROUP_NAME.length];
        for (int i = 0; i < Solver.GROUP_NAME.length; i++) {
            solverGroupMenu[i] = new JMenu(MESSAGE_BUNDLE.getString(Solver.GROUP_NAME[i]));
            add(solverGroupMenu[i]);
        }

        for (int i = 0; i < Solver.SOLVERS.length; i++) {
            ISolver solver = Solver.SOLVERS[i];
            String menuItemName = solver.getNameOfMenuItem();
            JMenuItem solverMenuItem = new JMenuItem(menuItemName);
            solverMenuItem.addActionListener(new SpecificSolverActionListener(solver));
            solverGroupMenu[Solver.GROUP_INDEX[i]].add(solverMenuItem);
        }
    }

    /**
     * Checks the puzzle model to make sure it contains a value sudoku.
     * Sets the text in the status panel depending on the result of the
     * check.
     */
    void checkSudoku() {
        final int size = owner.getPuzzleDelegate().getPuzzleModel().getGridSize();
        solution = new int[size * size];
        numberOfSolutions = 0;
        SudokuSolver solver1 = new SudokuSolver(owner.getPuzzleDelegate().getPuzzleModel());
        solver1.placeGivens(owner.getPuzzleDelegate().getPuzzleModel().getOriginalPuzzle());

        solver1.addSolutionListener(new CheckerSolutionListener());
        solver1.solve();
        switch (numberOfSolutions) {
        case 0 :
            owner.getMessagePanel().setText(MESSAGE_BUNDLE.getString("checker.0.solutions"));
            break;
        case 1 :
            boolean valid = true;
            for (int cellIndex = 0; cellIndex < size * size; cellIndex++) {
                Cell cell = owner.getPuzzleDelegate().getPuzzleModel()
                        .getCellAt(cellIndex / size, cellIndex % size);
                if (cell.getState() == CellState.UNSOLVED) {
                    if (!cell.hasCandidate(solution[cellIndex])) {
                        valid = false;
                        break;
                    }
                } else if (cell.getState() == CellState.SOLVED
                        && cell.getValue() != solution[cellIndex]) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                owner.getMessagePanel().setText(MESSAGE_BUNDLE.getString("checker.1.solution"));
            } else {
                owner.getMessagePanel().setText(MESSAGE_BUNDLE.getString("checker.invalid"));
            }
            break;
        default :
            owner.getMessagePanel().setText(MESSAGE_BUNDLE.getString("checker.2.solutions"));
            break;
        }
    }

//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////

    /**
     * This class responds to user requests to check the validity of a sudoku.
     */
    class CheckerActionListener implements ActionListener {

        /**
         * Responds to action events by checking the validity of a sudoku.
         *
         * @param event  Object describing the event that caused this call.
         */
        public void actionPerformed(final ActionEvent event) {
            checkSudoku();
        }
    }

    /** This class responds to user requests for a small hint. */
    class SmallHintActionListener implements ActionListener {

        /**
         * Responds to action events by providing a small hint.
         *
         * @param event  Object describing the event that caused this call.
         */
        public void actionPerformed(final ActionEvent event) {
            AbstractStep solutionStep = new Solver().getNextStep(
                    owner.getPuzzleDelegate().getPuzzleModel());
            Settings guiSettings = Settings.getInstance();
            if (solutionStep == null) {
                owner.getMessagePanel().setText(
                        MESSAGE_BUNDLE.getString(
                                "solver.hint.none.available"));
            } else {
                owner.getMessagePanel().setText(solutionStep.getSmallHint());
            }
            guiSettings.clearHighlightedCells();
            guiSettings.clearSupportingCells();
            owner.getHighlightPanel().clearSelection();
            guiSettings.setHighlightedCandidateValue(0);
            owner.getPuzzleDelegate().repaint();
        }
    }

    /** This class responds to user requests for a big hint. */
    class BigHintActionListener implements ActionListener {

        /**
         * Responds to action events by providing a big hint.
         *
         * @param event  Object describing the event that caused this call.
         */
        public void actionPerformed(final ActionEvent event) {
            AbstractStep solutionStep = new Solver().getNextStep(
                    owner.getPuzzleDelegate().getPuzzleModel());
            Settings guiSettings = Settings.getInstance();
            owner.getHighlightPanel().clearSelection();
            guiSettings.setHighlightedCandidateValue(0);
            if (solutionStep == null) {
                owner.getMessagePanel().setText(
                        MESSAGE_BUNDLE.getString("solver.hint.none.available"));
                guiSettings.clearHighlightedCells();
                guiSettings.clearSupportingCells();
            } else {
                owner.getHighlightPanel().clearSelection();
                owner.getMessagePanel().setText(solutionStep.getBigHint());
                Iterator iterator = solutionStep.getChangedCells();
                while (iterator.hasNext()) {
                    guiSettings.addHighlightedCell((Cell) iterator.next());
                }
                iterator = solutionStep.getExplainingCells();
                while (iterator.hasNext()) {
                    guiSettings.addSupportingCell((Cell) iterator.next());
                }
            }
            owner.getPuzzleDelegate().repaint();
        }
    }

    /** This class responds to user requests to invoke a specific solver. */
    class SpecificSolverActionListener implements ActionListener {

        /** The solver to be invoked. */
        private final ISolver solver;

        /**
         * Constructs a <code>SpecificSolverActionListener</code>.
         *
         * @param solver  The solver to be invoked.
         */
        SpecificSolverActionListener(final ISolver solver) {
            this.solver = solver;
        }

        /**
         * Responds to action events by providing a big hint from the
         * specified solver.
         *
         * @param event  Object describing the event that caused this call.
         */
        public void actionPerformed(final ActionEvent event) {
            AbstractStep solutionStep = solver.getNextStep(
                    owner.getPuzzleDelegate().getPuzzleModel());
            Settings settings = Settings.getInstance();
            owner.getHighlightPanel().clearSelection();
            settings.setHighlightedCandidateValue(0);
            if (solutionStep == null) {
                owner.getMessagePanel().setText(solver.getSolverNotApplicableMessage());
                settings.clearHighlightedCells();
                settings.clearSupportingCells();
            } else {
                owner.getMessagePanel().setText(solutionStep.getBigHint());
                settings.clearHighlightedCells();
                Iterator iterator = solutionStep.getChangedCells();
                while (iterator.hasNext()) {
                    settings.addHighlightedCell((Cell) iterator.next());
                }
                settings.clearSupportingCells();
                iterator = solutionStep.getExplainingCells();
                while (iterator.hasNext()) {
                    settings.addSupportingCell((Cell) iterator.next());
                }
            }
            owner.getPuzzleDelegate().repaint();
        }
    }

    /**
     * This is the listener that will be notified of a solution while
     * checking the validity of a sudoku.
     */
    class CheckerSolutionListener implements SolutionListener {

        /**
         * Records the solution found by the solver.
         *
         * @param solutionNodes  The <code>Node</code>s that make up
         *                       the generated solution.
         * @return               <code>true</code> if more than one
         *                       solution has been found.
         */
        public boolean solutionFound(final List solutionNodes) {
            int size = owner.getPuzzleDelegate().getPuzzleModel().getGridSize();
            numberOfSolutions++;

            Iterator iterator = solutionNodes.iterator();
            while (iterator.hasNext()) {
                Node node = (Node) iterator.next();
                int index = node.applicationData / size;
                int value = node.applicationData % size + 1;
                solution[index] = value;
            }

            return numberOfSolutions > 1;
        }
    }
}
