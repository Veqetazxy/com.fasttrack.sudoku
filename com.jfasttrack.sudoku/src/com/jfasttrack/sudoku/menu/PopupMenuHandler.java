/*
  PopupMenuHandler.java

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.jfasttrack.sudoku.puzzle.Cell;
import com.jfasttrack.sudoku.puzzle.CellState;
import com.jfasttrack.sudoku.puzzle.History;
import com.jfasttrack.sudoku.puzzle.House;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.step.ValuePlacementStep;
import com.jfasttrack.sudoku.ui.MessageBundle;
import com.jfasttrack.sudoku.ui.Settings;


/**
 * This class listens for mouse events. It creates and runs the resulting popup menus as needed.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class PopupMenuHandler extends MouseAdapter {

    /** The delegate of the current puzzle. */
    private final PuzzleDelegate puzzleDelegate;

    /** The number of blocks that have been created by the user. */
    private int blockCount;

    /**
     * Constructs a <code>PopupMenuHandler</code>.
     *
     * @param puzzleDelegate  The delegate of the current puzzle.
     */
    public PopupMenuHandler(final PuzzleDelegate puzzleDelegate) {
        this.puzzleDelegate = puzzleDelegate;
    }

    /**
     * Responds to "mouse clicked" events when the user is entering the blocks of a new sudoku.
     * Toggles the selected state of the cell where the mouse was clicked.
     *
     * @param event  Object describing the event that caused this call.
     */
    public void mouseClicked(final MouseEvent event) {
        if (puzzleDelegate.getOperatingMode() == PuzzleDelegate.OperatingMode.ENTERING_BLOCKS
                && !event.isMetaDown()) {
            Cell cell = getCellAt(event.getX(), event.getY());
            if (cell.getState() == CellState.UNASSIGNED) {
                Settings settings = Settings.getInstance();
                if (settings.shouldHighlight(cell)) {
                    settings.removeHighlightedCell(cell);
                } else {
                    if (Settings.getInstance().getHighlightedCellCount()
                            < puzzleDelegate.getPuzzleModel().getGridSize()) {
                        settings.addHighlightedCell(cell);
                    }
                }
                puzzleDelegate.repaint();
            }
        }
    }

    /**
     * Responds to a mouse press. Invokes a popup menu if appropriate.
     *
     * @param event  Object describing the event that caused this call.
     */
    public void mousePressed(final MouseEvent event) {
        if (event.isPopupTrigger()) {
            Cell cell = getCellAt(event.getX(), event.getY());
            if (cell != null && (cell.getState() != CellState.GIVEN
                    || puzzleDelegate.getOperatingMode()
                            == PuzzleDelegate.OperatingMode.ENTERING_GIVENS)) {
                runPopupMenu(event, cell);
            }
        }
    }

    /**
     * Responds to a mouse release. Invokes a popup menu if appropriate.
     *
     * @param event  Object describing the event that caused this call.
     */
    public void mouseReleased(final MouseEvent event) {
        if (event.isPopupTrigger()) {
            Cell cell = getCellAt(event.getX(), event.getY());
            if (cell != null && (cell.getState() != CellState.GIVEN
                    || puzzleDelegate.getOperatingMode()
                            == PuzzleDelegate.OperatingMode.ENTERING_GIVENS)) {
                runPopupMenu(event, cell);
            }
        }
    }

    /**
     * Runs a popup menu that lets the user enter data or take a step toward the solution of a
     * sudoku.
     *
     * @param event  Object describing the event that caused this call.
     * @param cell   The <code>Cell</code> where the user clicked.
     */
    private void runPopupMenu(final MouseEvent event, final Cell cell) {
        JPopupMenu popupMenu = null;

        // Add all of the appropriate items to the popup menu.
        if (puzzleDelegate.getOperatingMode() == PuzzleDelegate.OperatingMode.ENTERING_BLOCKS) {
            popupMenu = createNewBlockPopupMenu();
        } else if (puzzleDelegate.getOperatingMode()
                == PuzzleDelegate.OperatingMode.ENTERING_GIVENS) {
            popupMenu = createManualEntryPopupMenu(cell);
        } else {
            popupMenu = createSolvingPopupMenu(cell);
        }

        if (popupMenu.getComponentCount() > 0) {

            // Make sure the menu will fit in the sudoku window.
            int menuX = event.getX();
            int menuWidth = popupMenu.getPreferredSize().width;
            if (menuX + menuWidth > puzzleDelegate.getWidth()) {
                menuX = menuX + 1 - menuWidth;
            }

            int menuY = event.getY();
            int menuHeight = popupMenu.getPreferredSize().height;
            if (menuY + menuHeight > puzzleDelegate.getHeight()) {
                menuY = menuY + 1 - menuHeight;
            }

            popupMenu.show(event.getComponent(), menuX, menuY);
        }
    }

    /**
     * If the user has selected the corrected number of <code>Cell</code>s, constructs a popup menu
     * that lets the user create a block from those <code>Cell</code>s.
     *
     * @return  A <code>JPopupMenu</code> that can be used to create a block from the currently
     *          selected cells.
     */
    private JPopupMenu createNewBlockPopupMenu() {

        JPopupMenu popupMenu = new JPopupMenu();

        if (Settings.getInstance().getHighlightedCellCount()
                == puzzleDelegate.getPuzzleModel().getGridSize()) {
            JMenuItem createBlockMenuItem = new JMenuItem(
                    MessageBundle.getInstance().getString("popup.create.block"));
            createBlockMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent event) {
                    String[] blockIndex = {
                        String.valueOf(blockCount + 1),
                    };
                    House block = new House(
                            MessageBundle.getInstance().getString("block.name", blockIndex));
                    Iterator i = Settings.getInstance().getHighlightedCells();
                    while (i.hasNext()) {
                        Cell cell = (Cell) i.next();
                        cell.setStateAndValue(CellState.UNSOLVED, 0, null);
                        cell.setBlockIndex(blockCount);
                        block.addCell(cell);
                    }
                    puzzleDelegate.getPuzzleModel().addBlock(block);

                    Settings.getInstance().clearHighlightedCells();
                    puzzleDelegate.repaint();
                    blockCount++;
                    if (blockCount == puzzleDelegate.getPuzzleModel().getGridSize()) {
//TODO: Display a message here.
                        puzzleDelegate.setOperatingMode(
                                PuzzleDelegate.OperatingMode.ENTERING_GIVENS);
                    }
                }
            });
            popupMenu.add(createBlockMenuItem);
        }

        return popupMenu;
    }

    /**
     * Creates a popup menu that lets the user enter or remove a value that is a given in the
     * puzzle.
     *
     * @param cell  The <code>Cell</code> where the user clicked.
     * @return      A <code>JPopupMenu</code> that can be used for manual puzzle entry.
     */
    private JPopupMenu createManualEntryPopupMenu(final Cell cell) {

        JPopupMenu popupMenu = new JPopupMenu();

        if (cell.containsValue()) {
            popupMenu.add(createClearMenuItem(cell));
        } else {
            for (int value = 1;
                    value <= puzzleDelegate.getPuzzleModel().getGridSize();
                    value++) {
                if (cell.hasCandidate(value)) {
                    popupMenu.add(createEnterValueMenuItem(cell, value));
                }
            }
        }

        return popupMenu;
    }

    /**
     * Creates a popup menu item used to clear the contents of a <code>Cell</code> during manual
     * entry of a sudoku.
     *
     * @param cell  The <code>Cell</code> to be cleared by this menu item.
     * @return      A <code>JMenuItem</code> that can be used to clear a <code>Cell</code>.
     */
    private JMenuItem createClearMenuItem(final Cell cell) {
        JMenuItem clearMenuItem = new JMenuItem(
                MessageBundle.getInstance().getString("popup.clear"));
        clearMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                puzzleDelegate.clearCell(cell);
                puzzleDelegate.repaint();
                blockCount = 0;
            }
        });

        return clearMenuItem;
    }

    /**
     * Runs a popup menu that lets the user take a step toward the solution
     * of a sudoku.
     *
     * @param cell  The <code>Cell</code> where the user clicked.
     * @return      A <code>JPopupMenu</code> that can be used to enter
     *              values or remove candidates.
     */
    private JPopupMenu createSolvingPopupMenu(final Cell cell) {
        JPopupMenu popupMenu = new JPopupMenu();

        /*
         * The user is solving a puzzle. Options are to store a
         * value into a cell or remove a candidate from a cell.
         */
        if (cell.getState() == CellState.UNSOLVED) {
            for (int value = 1;
                    value <= puzzleDelegate.getPuzzleModel().getGridSize();
                    value++) {
                if (cell.hasCandidate(value)) {
                    popupMenu.add(createSetValueMenuItem(cell, value));
                }
            }
            popupMenu.addSeparator();
            for (int value = 1;
                    value <= puzzleDelegate.getPuzzleModel().getGridSize();
                    value++) {
                if (cell.hasCandidate(value)) {
                    popupMenu.add(puzzleDelegate.createRemoveCandidateMenuItem(
                            cell, value));
                }
            }
        }

        return popupMenu;
    }

    /**
     * Gets the <code>Cell</code> at the specified X and Y location in the
     * GUI.
     *
     * @param mouseX  The X coordinate of a mouse click.
     * @param mouseY  The Y coordinate of a mouse click.
     * @return        The <code>Cell</code> at the specified X and Y location
     *                in the GUI. <code>null</code> if the position does not
     *                correspond to a <code>Cell</code>.
     */
    private Cell getCellAt(final int mouseX, final int mouseY) {
        int cellX = (mouseX - puzzleDelegate.getXOrigin() - 1)
                / puzzleDelegate.getCellSize();
        int cellY = (mouseY - puzzleDelegate.getYOrigin() - 1)
                / puzzleDelegate.getCellSize();

        Cell cell = null;

        if (mouseX >= puzzleDelegate.getXOrigin()
                && mouseY >= puzzleDelegate.getYOrigin()
                && cellX < puzzleDelegate.getPuzzleModel().getGridSize()
                && cellY < puzzleDelegate.getPuzzleModel().getGridSize()) {
            cell = puzzleDelegate.getPuzzleModel().getCellAt(cellY, cellX);
        }

        return cell;
    }

    /**
     * Creates a popup menu item used to set the contents of a
     * <code>Cell</code> during manual entry of a sudoku.
     *
     * @param cell   The <code>Cell</code> whose value is to be set by this
     *               menu item.
     * @param value  The value that can be entered into the <code>Cell</code>.
     * @return       A <code>JMenuItem</code> that can be used to set the
     *               value of a <code>Cell</code>.
     */
    private JMenuItem createEnterValueMenuItem(
            final Cell cell,
            final int value) {
        JMenuItem enterValueMenuItem = new JMenuItem(
            MessageBundle.getInstance().getString(
                "popup.place",
                new String[] {
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                }
            )
        );
        enterValueMenuItem.setActionCommand(
                String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)));
        enterValueMenuItem.addActionListener(new ActionListener() {

            /**
             * Responds to a the placement of a number in a sudoku grid.
             *
             * @param event  Object describing the event that caused this call.
             */
            public void actionPerformed(final ActionEvent event) {
                int value = PuzzleDelegate.CHARACTERS.indexOf(event.getActionCommand());
                cell.setStateAndValue(CellState.GIVEN, value, null);
                int index =
                        cell.getRow() * puzzleDelegate.getPuzzleModel().getGridSize()
                        + cell.getColumn();
                puzzleDelegate.getPuzzleModel().getOriginalPuzzle()[index] = value;
                puzzleDelegate.repaint();
            }
        });

        return enterValueMenuItem;
    }

    /**
     * Creates a popup menu item used to set the contents of a
     * <code>Cell</code> during solving.
     *
     * @param cell   The <code>Cell</code> whose value is to be set by this
     *               menu item.
     * @param value  The value that can be entered into the <code>Cell</code>.
     * @return       A <code>JMenuItem</code> that can be used to set the
     *               value of a <code>Cell</code>.
     */
    private JMenuItem createSetValueMenuItem(final Cell cell, final int value) {
        JMenuItem setValueMenuItem = new JMenuItem(
            MessageBundle.getInstance().getString(
                "popup.place",
                new String[] {
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)),
                }
            )
        );
        setValueMenuItem.setActionCommand(String.valueOf(PuzzleDelegate.CHARACTERS.charAt(value)));
        setValueMenuItem.addActionListener(new ActionListener() {

            /**
             * Responds to a the placement of a number in a sudoku grid.
             *
             * @param event  Object describing the event that caused this call.
             */
            public void actionPerformed(final ActionEvent event) {
                int value = PuzzleDelegate.CHARACTERS.indexOf(event.getActionCommand());
                ValuePlacementStep step = new ValuePlacementStep(cell, value);
                EditMenu editMenu = EditMenu.getInstance();
                History history = History.getInstance();

                cell.setStateAndValue(CellState.SOLVED, value, step);
                history.pushUndoStack(step);
                editMenu.setUndoEnabled(true);
                editMenu.setRedoEnabled(false);
                history.clearRedoStack();
                puzzleDelegate.getOwner().getMessagePanel().clear();
                Settings guiSettings = Settings.getInstance();
                guiSettings.clearHighlightedCells();
                guiSettings.clearSupportingCells();
                puzzleDelegate.repaint();
                if (puzzleDelegate.getPuzzleModel().isSolved()) {
                    puzzleDelegate.getOwner().getMessagePanel().setText(
                            MessageBundle.getInstance().getString("sudoku.solved"));
                    puzzleDelegate.getOwner().getHighlightPanel().clearSelection();
                    puzzleDelegate.getOwner().getTimerPanel().stop();
                }
            }
        });

        return setValueMenuItem;
    }
}
