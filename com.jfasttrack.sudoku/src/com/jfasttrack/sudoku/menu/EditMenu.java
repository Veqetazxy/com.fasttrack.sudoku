/*
  EditMenu.java

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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.jfasttrack.sudoku.DancingLinksSudoku;
import com.jfasttrack.sudoku.puzzle.History;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.puzzle.StandardSudoku;
import com.jfasttrack.sudoku.ui.MessageBundle;
import com.jfasttrack.sudoku.ui.Settings;


/**
 * The edit menu gives the user options to undo and redo steps. For an application, it also
 * provides copy and paste options.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public final class EditMenu extends JMenu {

    /** Message bundle that holds all messages for this program. */
    private static final MessageBundle MESSAGE_BUNDLE = MessageBundle.getInstance();

    /** The singleton instance of this class. */
    private static EditMenu instance;

    /** The program that owns this menu. */
    private static DancingLinksSudoku owner;

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Menu item used to undo a step in a solution. */
    private final JMenuItem undoMenuItem;

    /** Menu item used to redo a step that was undone. */
    private final JMenuItem redoMenuItem;

    /** Creates the edit menu. */
    private EditMenu() {
        super(MESSAGE_BUNDLE.getString("menu.edit"));
        setMnemonic(MESSAGE_BUNDLE.getString("menu.edit.accelerator").charAt(0));

        undoMenuItem = new JMenuItem(MESSAGE_BUNDLE.getString("menu.edit.undo"));
        undoMenuItem.setMnemonic(KeyEvent.VK_U);
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Z,
                InputEvent.CTRL_DOWN_MASK));
        undoMenuItem.addActionListener(new UndoActionListener());
        undoMenuItem.setEnabled(false);
        add(undoMenuItem);

        redoMenuItem = new JMenuItem(
                MESSAGE_BUNDLE.getString("menu.edit.redo"));
        redoMenuItem.setMnemonic(KeyEvent.VK_R);
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Z,
                InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
        redoMenuItem.addActionListener(new RedoActionListener());
        redoMenuItem.setEnabled(false);
        add(redoMenuItem);

        if (owner.isRunningAsApplication()) {
            addSeparator();

            JMenuItem copyMenuItem = new JMenuItem(
                    MESSAGE_BUNDLE.getString("menu.edit.copy"));
            copyMenuItem.setMnemonic(KeyEvent.VK_C);
            copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_C,
                    InputEvent.CTRL_DOWN_MASK));
            copyMenuItem.addActionListener(new CopyActionListener());
            add(copyMenuItem);

            JMenuItem pasteMenuItem = new JMenuItem(
                    MESSAGE_BUNDLE.getString("menu.edit.paste"));
            pasteMenuItem.setMnemonic(KeyEvent.VK_P);
            pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_V,
                    InputEvent.CTRL_DOWN_MASK));
            pasteMenuItem.addActionListener(new PasteActionListener());
            add(pasteMenuItem);
        }
    }

    /**
     * Gets the singleton instance of this class. Creates the instance if it does not already exist.
     *
     * @return  The singleton instance of this class.
     */
    public static EditMenu getInstance() {
        if (instance == null) {
            instance = new EditMenu();
        }
        return instance;
    }

    /**
     * Sets the owner of an <code>EditMenu</code>.
     *
     * @param owner  The owner of an <code>EditMenu</code>.
     */
    public static void setOwner(final DancingLinksSudoku owner) {
        EditMenu.owner = owner;
    }

    /**
     * Gets the program that owns this menu.
     *
     * @return  A reference to  the program that owns this menu.
     */
    static DancingLinksSudoku getOwner() {
        return owner;
    }

    /**
     * Sets whether the user is allowed to undo a step.
     *
     * @param enabled  <code>true</code> if the undo stack contains any steps.
     *                 Otherwise, <code>false</code>.
     */
    public void setUndoEnabled(final boolean enabled) {
        undoMenuItem.setEnabled(enabled);
    }

    /**
     * Sets whether the user is allowed to redo a step.
     *
     * @param enabled  <code>true</code> if the redo stacl contains any steps.
     *                 Otherwise, <code>false</code>.
     */
    public void setRedoEnabled(final boolean enabled) {
        redoMenuItem.setEnabled(enabled);
    }

    /** Action listener for the <code>Undo</code> menu item. */
    class UndoActionListener implements ActionListener {

        /**
         * Undoes the last step taken.
         *
         * @param event  Object describing the <code>ActionEvent</code>
         *               that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            owner.getPuzzleDelegate().undo();
            undoMenuItem.setEnabled(History.getInstance().undoIsAvailable());
            redoMenuItem.setEnabled(true);
            owner.getPuzzleDelegate().repaint();
        }
    }

    /** Action listener for the <code>Redo</code> menu item. */
    class RedoActionListener implements ActionListener {

        /**
         * Undoes the last step that was undone.
         *
         * @param event  Object describing the <code>ActionEvent</code>
         *               that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            owner.getPuzzleDelegate().redo();
            redoMenuItem.setEnabled(History.getInstance().redoIsAvailable());
            undoMenuItem.setEnabled(true);
            owner.getPuzzleDelegate().repaint();
        }
    }

    /** Action listener for the <code>Copy</code> menu item. */
    static class CopyActionListener implements ActionListener {

        /**
         * Copies the sudoku to the system clipboard.
         *
         * @param event  Object describing the <code>ActionEvent</code>
         *               that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            StringSelection string = new StringSelection(
                    owner.getPuzzleDelegate().getPuzzleModel().toString());
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(string, null);
        }
    }

    /** Action listener for the <code>Paste</code> menu item. */
    static class PasteActionListener implements ActionListener {

        /**
         * Creates a new sudoku from the contents of the system clipboard.
         *
         * @param event  Object describing the <code>ActionEvent</code> that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            Transferable transferable =
                    Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            DancingLinksSudoku owner = getOwner();
            try {
                if (transferable != null
                        && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    String text = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    StandardSudoku sudoku = new StandardSudoku(text);
                    owner.setPuzzleModel(sudoku);
                    owner.getPuzzleDelegate().setOperatingMode(
                            PuzzleDelegate.OperatingMode.SOLVING_MODE);
                    owner.getHighlightPanel().setEnabled(true);
                    owner.getHighlightPanel().clearSelection();
                    Settings guiSettings = Settings.getInstance();
                    guiSettings.setHighlightedCandidateValue(0);
                    guiSettings.clearHighlightedCells();
                    guiSettings.clearSupportingCells();
                    owner.getTimerPanel().restart();
                    owner.getTimerPanel().setVisible(true);
                }
            } catch (UnsupportedFlavorException ufe) {
                JOptionPane.showMessageDialog(
                        owner,
                        "UnsupportedFlavorException reading sudoku:\n"
                            + ufe.getLocalizedMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(
                        owner,
                        "IOException reading sudoku:\n"
                            + ioe.getLocalizedMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException iae) {
                JOptionPane.showMessageDialog(
                        owner,
                        "Clipboard does not contain a valid sudoku:\n"
                            + iae.getLocalizedMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
