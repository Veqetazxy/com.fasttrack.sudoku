/*
  History.java

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

import java.util.Stack;

import com.jfasttrack.sudoku.step.AbstractStep;


/**
 * The <code>History</code> keeps a record of steps that have been taken toward the solution of a
 * sudoku. It maintains stacks of moves done and moves undone.
 * <br/>
 * This class is a singleton.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public final class History {

    /** The singleton instance of this class. */
    private static final History INSTANCE = new History();

    /** A history of the <code>Step</code>s taken toward the solution of this puzzle. */
    private final Stack undoStack;

    /**
     * A history of the <code>Step</code>s that have been undone, saved in case the user wants to
     * redo them.
     */
    private final Stack redoStack;

    /** Private constructor, to keep anyone from instantiating this class. */
    private History() {
        undoStack = new Stack();
        redoStack = new Stack();
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return  The singleton instance of this class.
     */
    public static History getInstance() {
        return INSTANCE;
    }

    /**
     * Gets whether the <code>undo</code> should be available to the user.
     *
     * @return  <code>true</code> if there is at least one <code>Step</code> available in the
     *          collection of steps taken to solve this puzzle. Otherwise, <code>false</code>.
     */
    public boolean undoIsAvailable() {
        return undoStack.size() > 0;
    }

    /**
     * Gets whether the <code>redo</code> should be available to the user.
     *
     * @return  <code>true</code> if there is at least one <code>Step</code> available in the
     *          collection of steps that have been undone. Otherwise, <code>false</code>.
     */
    public boolean redoIsAvailable() {
        return redoStack.size() > 0;
    }

    /** Clears the undo stack. */
    public void clearUndoStack() {
        undoStack.clear();
    }

    /**
     * Pushes a step onto the undo stack.
     *
     * @param step  The step to be pushed.
     */
    public void pushUndoStack(final AbstractStep step) {
        undoStack.push(step);
    }

    /**
     * Pops a step off the undo stack.
     *
     * @return  The popped step.
     */
    public AbstractStep popUndoStack() {
        return (AbstractStep) undoStack.pop();
    }

    /** Clears the redo stack. */
    public void clearRedoStack() {
        redoStack.clear();
    }

    /**
     * Pushes a step onto the redo stack.
     *
     * @param step  The step to be pushed.
     */
    public void pushRedoStack(final AbstractStep step) {
        redoStack.push(step);
    }

    /**
     * Pops a step off the redo stack.
     *
     * @return  The popped step.
     */
    public AbstractStep popRedoStack() {
        return (AbstractStep) redoStack.pop();
    }
}
