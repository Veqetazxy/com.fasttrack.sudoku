/*
  PuzzleDelegate.java

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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.jfasttrack.sudoku.DancingLinksSudoku;
import com.jfasttrack.sudoku.menu.EditMenu;
import com.jfasttrack.sudoku.menu.PopupMenuHandler;
import com.jfasttrack.sudoku.step.AbstractStep;
import com.jfasttrack.sudoku.step.CandidateRemovalStep;
import com.jfasttrack.sudoku.ui.MessageBundle;
import com.jfasttrack.sudoku.ui.Settings;


/**
 * This is the GUI side (view and controller) of a sudoku.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class PuzzleDelegate extends JPanel {

    /** The state of the program, describing what actions the user is taking. */
    public static class OperatingMode {

        /** This mode is set when the user is manually entering jigsaw blocks. */
        public static final OperatingMode ENTERING_BLOCKS = new OperatingMode();

        /** This mode is set when the user is manually entering a puzzle. */
        public static final OperatingMode ENTERING_GIVENS = new OperatingMode();

        /** This mode is set when the user is solving a puzzle. */
        public static final OperatingMode SOLVING_MODE = new OperatingMode();
    }

    /** The characters used to represent numbers in a grid. */
    public static final String CHARACTERS = ".123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** The color used as fill around the edges of the sudoku grid. */
    private static final Color FILL_COLOR = Color.LIGHT_GRAY;

    /** The color used for the grid background. This includes the lines between the cells. */
    private static final Color GRID_BACKGROUND_COLOR = Color.BLACK;

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The program's current operating mode. */
    private OperatingMode operatingMode = OperatingMode.SOLVING_MODE;

    /** The GUI component that owns this <code>PuzzleDelegate</code>. */
    private final DancingLinksSudoku owner;

    /** The model of the sudoku to be painted in this panel. */
    private AbstractPuzzleModel puzzleModel;

    /** The size (in pixels) of the grid. The same value is used for the height and width. */
    private int gridSize;

    /** The height and width (in pixels) of each cell. */
    private int cellSize;

    /** The X coordinate of the upper left corner of this sudoku. */
    private int xOrigin;

    /** The Y coordinate of the upper left corner of this sudoku. */
    private int yOrigin;

    /**
     * Constructs a <code>PuzzleDelegate</code>.
     *
     * @param owner  The GUI component that owns this <code>PuzzleDelegate</code>.
     */
    public PuzzleDelegate(final DancingLinksSudoku owner) {
        this.owner = owner;

        // Randomly generate a new puzzle.
        Options.getInstance().setCreateAction(Options.CreateAction.GENERATE);
        puzzleModel = new StandardSudoku();

        addMouseListener(new PopupMenuHandler(this));

        super.setOpaque(false);
    }

    /**
     * Removes the value of a <code>Cell</code> restoring its candidates and the candidates of its
     * buddies as needed.
     *
     * @param cell  The <code>Cell</code> to be cleared.
     */
    public void clearCell(final Cell cell) {
        cell.setStateAndValue(CellState.UNSOLVED, 0, null);

        // Recalculate the candidates of this cell.
        BitSet candidates = calculateCandidates(cell);
        for (int value = 1; value <= puzzleModel.getGridSize(); value++) {
            if (candidates.get(value)) {
                cell.addCandidate(value);
            }
        }

        // Recalculate the candidates of this cell's buddies.
        Iterator iterator = getPuzzleModel().getBuddies(cell).iterator();
        while (iterator.hasNext()) {
            Cell buddy = (Cell) iterator.next();
            if (buddy.getState() == CellState.UNSOLVED) {
                candidates = calculateCandidates(buddy);
                for (int value = 1; value <= puzzleModel.getGridSize(); value++) {
                    if (candidates.get(value)) {
                        buddy.addCandidate(value);
                    }
                }
            }
        }
    }

    /**
     * Recalculates the candidates of a <code>Cell</code>.
     *
     * @param cell  The <code>Cell</code> whose candidates are to be calculated.
     * @return      A <code>BitSet</code> identifying the <code>Cell</code>'s candidates.
     */
    private BitSet calculateCandidates(final Cell cell) {

        // Start by assuming that every available value is a candidate.
        BitSet candidates = new BitSet(gridSize + 1);
        candidates.set(1, gridSize + 1, true);

        // If any of the cell's buddies have the value, remove that value from the candidates.
        Iterator iterator = puzzleModel.getBuddies(cell).iterator();
        while (iterator.hasNext()) {
            Cell buddy = (Cell) iterator.next();
            if (buddy.getState() == CellState.GIVEN) {
                candidates.clear(buddy.getValue());
            }
        }

        return candidates;
    }

    /**
     * Creates a popup menu item used to remove a candidate from a <code>Cell</code> during solving.
     *
     * @param cell   The <code>Cell</code> whose candidate is to be removed by this menu item.
     * @param value  The value that can be entered into the <code>Cell</code>.
     * @return       A <code>JMenuItem</code> that can be used to remove a candidate from a
     *               <code>Cell</code>.
     */
    public JMenuItem createRemoveCandidateMenuItem(final Cell cell, final int value) {
        JMenuItem removeCandidateMenuItem = new JMenuItem(
            MessageBundle.getInstance().getString(
                "popup.remove",
                new String[] {
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                }
            )
        );
        removeCandidateMenuItem.setActionCommand(String.valueOf(value));
        removeCandidateMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                int valueToBeRemoved = Integer.parseInt(event.getActionCommand());
                Set cells = new HashSet();
                EditMenu editMenu = EditMenu.getInstance();
                History history = History.getInstance();
                cells.add(cell);
                CandidateRemovalStep step = new CandidateRemovalStep(cells, valueToBeRemoved);
                cell.removeCandidate(valueToBeRemoved);
                editMenu.setUndoEnabled(true);
                editMenu.setRedoEnabled(false);
                history.pushUndoStack(step);
                history.clearRedoStack();
                owner.getMessagePanel().clear();
                Settings guiSettings = Settings.getInstance();
                guiSettings.clearHighlightedCells();
                guiSettings.clearSupportingCells();
                repaint();
            }
        });

        return removeCandidateMenuItem;
    }

    /**
     * Sets the operating mode of this <code>PuzzleDelegate</code>.
     *
     * @param operatingMode  The new operating mode.
     */
    public void setOperatingMode(final OperatingMode operatingMode) {
        History history = History.getInstance();
        this.operatingMode = operatingMode;
        history.clearUndoStack();
        history.clearRedoStack();
    }

    /**
     * Gets the operating mode of this <code>PuzzleDelegate</code>.
     *
     * @return  The operating mode of this <code>PuzzleDelegate</code>.
     */
    public OperatingMode getOperatingMode() {
        return operatingMode;
    }

    /**
     * Undoes the last <code>Step</code> that was taken. Removes that <code>Step</code> from the
     * collection of steps taken.
     */
    public void undo() {
        History history = History.getInstance();
        AbstractStep step = history.popUndoStack();
        step.undo();
        history.pushRedoStack(step);
    }

    /**
     * Redoes the last <code>Step</code> that was undone. Removes that <code>Step</code> from the
     * collection of steps taken and adds it to the <code>redoStack</code>.
     */
    public void redo() {
        History history = History.getInstance();
        AbstractStep step = history.popRedoStack();
        step.redo();
        history.pushUndoStack(step);
    }

    /**
     * Sets the <code>puzzleModel</code>.
     *
     * @param puzzleModel  The <code>puzzleModel</code>.
     */
    public void setPuzzleModel(final AbstractPuzzleModel puzzleModel) {
        this.puzzleModel = puzzleModel;
        History history = History.getInstance();
        history.clearUndoStack();
        history.clearRedoStack();
        repaint();
    }

    /**
     * Gets the <code>puzzleModel</code>.
     *
     * @return  The <code>puzzleModel</code>.
     */
    public AbstractPuzzleModel getPuzzleModel() {
        return puzzleModel;
    }

    /**
     * Gets the X coordinate of the upper left corner of this sudoku.
     *
     * @return  The X coordinate of the upper left corner of this sudoku.
     */
    public int getXOrigin() {
        return xOrigin;
    }

    /**
     * Gets the Y coordinate of the upper left corner of this sudoku.
     *
     * @return  The Y coordinate of the upper left corner of this sudoku.
     */
    public int getYOrigin() {
        return yOrigin;
    }

    /**
     * Gets the owner of this <code>PuzzleDelegate</code>.
     *
     * @return  The owner of this <code>PuzzleDelegate</code>.
     */
    public DancingLinksSudoku getOwner() {
        return owner;
    }

    /**
     * Gets the height and width (in pixels) of each cell.
     *
     * @return  The height and width (in pixels) of each cell.
     */
    public int getCellSize() {
        return cellSize;
    }

    /**
     * Paints this <code>PuzzleDelegate</code>.
     *
     * @param g  The graphics context.
     */
    protected void paintComponent(final Graphics g) {

        // Paint the background.
        g.setColor(FILL_COLOR);
        g.fillRect(0, 0, getSize().width, getSize().height);

        // Calculate the screen coordinates where objects are to be painted.
        cellSize = Math.min(getSize().height, getSize().width) / puzzleModel.getGridSize();
        gridSize = cellSize * puzzleModel.getGridSize() + 1;
        xOrigin = (getSize().width - gridSize) / 2;
        yOrigin = (getSize().height - gridSize) / 2;

        // Draw the grid background. This includes the lines between the cells.
        g.setColor(GRID_BACKGROUND_COLOR);
        g.fillRect(xOrigin, yOrigin, gridSize, gridSize);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int fontSize = (int) Math.round(cellSize * 0.75);
        Cell.setValueFont(new Font("Arial", Font.PLAIN, fontSize));

        final float scaleFactor = 0.82f;
        int numberOfRows = (int) Math.sqrt(puzzleModel.getGridSize());
        int numberOfColumns = puzzleModel.getGridSize() / numberOfRows;
        if (numberOfRows * numberOfColumns != puzzleModel.getGridSize()) {
            numberOfColumns++;
        }
        int columnWidth = (cellSize - Cell.INSET_SIZE * 2 - 2) / numberOfColumns;
        fontSize = Math.round(scaleFactor * cellSize / numberOfRows) + 1;
        do {
            fontSize--;
            Font candidateFont = new Font("Arial", Font.PLAIN, fontSize);
            g.setFont(candidateFont);
            Cell.setCandidateFont(candidateFont);
        } while (g.getFontMetrics().getStringBounds("D", g).getWidth() > columnWidth);

        // Draw each cell.
        for (int row = 0; row < puzzleModel.getGridSize(); row++) {
            for (int column = 0; column < puzzleModel.getGridSize(); column++) {
                Cell cell = puzzleModel.getCellAt(row, column);
                paintCell(g, cell);
            }
        }
    }

    /**
     * Paints a <code>Cell</code>.
     *
     * @param g     The graphics context.
     * @param cell  The <code>Cell</code> to be painted.
     */
    private void paintCell(final Graphics g, final Cell cell) {
        int column = cell.getColumn();
        int row = cell.getRow();

        // Figure out the cell's location and size.
        int cellX = column * cellSize + xOrigin + 1;
        int cellY = row * cellSize + yOrigin + 1;
        int cellWidth = cellSize - 1;
        int cellHeight = cellSize - 1;

        // Adjust the insets when adjacent cells are in different blocks
        if (cell.getState() != CellState.UNASSIGNED) {
            if (column > 0
                    && puzzleModel.getBlockIndex(column, row)
                            != puzzleModel.getBlockIndex(column - 1, row)) {
                cellX += 2;
                cellWidth -= 2;
            }
            if (column < puzzleModel.getGridSize() - 1
                    && puzzleModel.getBlockIndex(column, row)
                            != puzzleModel.getBlockIndex(column + 1, row)) {
                cellWidth -= 2;
            }
            if (row > 0
                    && puzzleModel.getBlockIndex(column, row)
                            != puzzleModel.getBlockIndex(column, row - 1)) {
                cellY += 2;
                cellHeight -= 2;
            }
            if (row < puzzleModel.getGridSize() - 1
                    && puzzleModel.getBlockIndex(column, row)
                            != puzzleModel.getBlockIndex(column, row + 1)) {
                cellHeight -= 2;
            }
        }

        cell.paint(g, cellX, cellY, cellWidth, cellHeight);
    }
}
