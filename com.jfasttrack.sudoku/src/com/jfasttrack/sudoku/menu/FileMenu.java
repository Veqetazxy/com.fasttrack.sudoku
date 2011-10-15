/*
  FileMenu.java

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.jfasttrack.sudoku.DancingLinksSudoku;
import com.jfasttrack.sudoku.puzzle.Options;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.puzzle.StandardSudoku;
import com.jfasttrack.sudoku.ui.CreationDialog;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * The file menu gives the user options to create, save, and load sudoku puzzles.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class FileMenu extends JMenu {

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** Message bundle that holds all messages for this program. */
    private static final MessageBundle MESSAGE_BUNDLE = MessageBundle.getInstance();

    /** The program that owns this menu. */
    private final DancingLinksSudoku owner;

    /** Menu item used to clear the sudoku grid. */
    private final JMenuItem startRestartMenuItem = new JMenuItem(
            MESSAGE_BUNDLE.getString("menu.file.restart"));

    /**
     * Constructs a <code>FileMenu</code>.
     *
     * @param owner  The application that owns this <code>FileMenu</code>.
     */
    public FileMenu(final DancingLinksSudoku owner) {
        super(MESSAGE_BUNDLE.getString("menu.file"));
        setMnemonic(MESSAGE_BUNDLE.getString("menu.file.accelerator").charAt(0));

        this.owner = owner;

        // Create and add the "New" menu item.
        JMenuItem newMenuItem = new JMenuItem(MESSAGE_BUNDLE.getString("menu.file.new"));
        newMenuItem.setMnemonic(KeyEvent.VK_N);
        newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        newMenuItem.addActionListener(new NewActionListener());
        add(newMenuItem);

        // Create and add the "New . . ." menu item.
        JMenuItem newWithOptionsMenuItem =
                new JMenuItem(MESSAGE_BUNDLE.getString("menu.file.new") + " . . .");
        newWithOptionsMenuItem.setMnemonic(KeyEvent.VK_W);
        newWithOptionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        newWithOptionsMenuItem.addActionListener(new NewWithOptionsActionListener());
        add(newWithOptionsMenuItem);

        // Create and add the "Start/Restart" menu item.
        startRestartMenuItem.setMnemonic(KeyEvent.VK_R);
        startRestartMenuItem.addActionListener(new StartActionListener());
        add(startRestartMenuItem);

        /*
         * The following items are available only when running as an application, due to security
         * issues when running as an applet.
         */
        if (owner.isRunningAsApplication()) {
            addSeparator();

            JMenuItem openMenuItem = new JMenuItem(
                    MESSAGE_BUNDLE.getString("menu.file.open") + " . . .");
            openMenuItem.setMnemonic(KeyEvent.VK_O);
            openMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_O,
                    InputEvent.CTRL_DOWN_MASK));
            openMenuItem.addActionListener(new OpenActionListener());
            add(openMenuItem);

            JMenuItem saveMenuItem = new JMenuItem(
                    MESSAGE_BUNDLE.getString("menu.file.save") + " . . .");
            saveMenuItem.setMnemonic(KeyEvent.VK_S);
            saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_S,
                    InputEvent.CTRL_DOWN_MASK));
            saveMenuItem.addActionListener(new SaveActionListener());
            add(saveMenuItem);

            // Create the quit menu item.
            addSeparator();
            JMenuItem quitMenuItem = new JMenuItem(MESSAGE_BUNDLE.getString("menu.file.quit"));
            quitMenuItem.setMnemonic(KeyEvent.VK_Q);
            quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_Q,
                    InputEvent.CTRL_DOWN_MASK));
            quitMenuItem.addActionListener(new QuitActionListener());
            add(quitMenuItem);
        }
    }

    /**
     * Gets the program that owns this menu.
     *
     * @return  A reference to  the program that owns this menu.
     */
    DancingLinksSudoku getOwner() {
        return owner;
    }

    /**
     * Gets the menu item used to start or restart solving.
     *
     * @return  A reference to the menu item used to start or restart solving.
     */
    public JMenuItem getStartRestartMenuItem() {
        return startRestartMenuItem;
    }

    /** Action listener for the <code>New</code> menu item. */
    class NewActionListener implements ActionListener {

        /**
         * Creates a new sudoku.
         *
         * @param event  Object describing the <code>ActionEvent</code>
         *               that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            Options.getInstance().setCreateAction(Options.CreateAction.GENERATE);
            DancingLinksSudoku owner = getOwner();
            owner.setPuzzleModel(new StandardSudoku());
            owner.getPuzzleDelegate().setOperatingMode(PuzzleDelegate.OperatingMode.SOLVING_MODE);
            startRestartMenuItem.setText(MESSAGE_BUNDLE.getString("menu.file.start"));
            owner.getHighlightPanel().setEnabled(true);
            owner.getHighlightPanel().clearSelection();
            owner.getMessagePanel().clear();
            owner.getTimerPanel().restart();
            owner.getTimerPanel().setVisible(true);
            owner.repaint();
        }
    }

    /** Action listener for the <code>New . . .</code> menu item. */
    class NewWithOptionsActionListener implements ActionListener {

        /**
         * Runs the creation dialog to get the user choices for creation of a new sudoku.
         *
         * @param event  Object describing the <code>ActionEvent</code> that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            CreationDialog createDialog =
                    new CreationDialog(JOptionPane.getFrameForComponent(owner), owner);
            createDialog.setVisible(true);
            createDialog.dispose();
        }
    }

    /** Action listener for the <code>Start</code> menu item. */
    class StartActionListener implements ActionListener {

        /**
         * Ends manual entry mode and puts the program into solving mode.
         *
         * @param event  Object describing the <code>ActionEvent</code>
         *               that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            DancingLinksSudoku owner = getOwner();
            if (startRestartMenuItem.getActionCommand().equals(
                    MESSAGE_BUNDLE.getString("menu.file.restart"))) {
                String puzzleString = ((StandardSudoku)
                        owner.getPuzzleDelegate().getPuzzleModel()).toOriginalString();
                owner.setPuzzleModel(new StandardSudoku(puzzleString));
            } else {
                owner.getPuzzleDelegate().setOperatingMode(
                        PuzzleDelegate.OperatingMode.SOLVING_MODE);
                owner.getHighlightPanel().setEnabled(true);
                startRestartMenuItem.setText(MESSAGE_BUNDLE.getString("menu.file.restart"));
            }
            owner.getMessagePanel().clear();
            owner.getTimerPanel().restart();
            owner.getTimerPanel().setVisible(true);
        }
    }

    /** Action listener for the <code>Open</code> menu item. */
    class OpenActionListener implements ActionListener {

        /**
         * Opens a disk file and loads a sudoku from it.
         *
         * @param event  Object describing the <code>ActionEvent</code>
         *               that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            BufferedReader br = null;
            JFileChooser fileChooser = new JFileChooser(".");

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(owner);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    FileInputStream fis = new FileInputStream(
                            fileChooser.getSelectedFile());
                    InputStreamReader isr = new InputStreamReader(fis);
                    br = new BufferedReader(isr);
                    StringBuffer puzzleBuffer = new StringBuffer();
                    String line = br.readLine();
                    do {
                        puzzleBuffer.append(line);
                        line = br.readLine();
                    } while (line != null);
                    StandardSudoku sudoku =
                            new StandardSudoku(puzzleBuffer.toString().replaceAll("\n", ""));
                    owner.setPuzzleModel(sudoku);
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(
                            owner,
                            "IOException reading sudoku:\n"
                                + ioe.getLocalizedMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    try {
                        if (br != null) {
                            br.close();
                        }
                    } catch (IOException ioe) {
                        // Not much we can do here.
                    }
                }
            }
        }
    }

    /** Action listener for the <code>Save</code> menu item. */
    class SaveActionListener implements ActionListener {

        /**
         * Saves a sudoku to a disk file.
         *
         * @param event  Object describing the <code>ActionEvent</code>
         *               that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            PrintStream ps = null;
            JFileChooser fileChooser = new JFileChooser(".");
            DancingLinksSudoku owner = getOwner();

            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showSaveDialog(owner);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file.exists()
                        && JOptionPane.showConfirmDialog(
                            owner,
                            "File \"" + file.getName()
                                    + "\" already exists.\n"
                                    + "Would you like to replace it?",
                            "Save",
                            JOptionPane.YES_NO_OPTION)
                          != JOptionPane.YES_OPTION) {
                    return;
                }
                try {
                    ps = new PrintStream(new FileOutputStream(file));
                    ps.print(owner.getPuzzleDelegate()
                            .getPuzzleModel().toString());
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(
                        owner,
                        "IOException saving sudoku:\n"
                            + ioe.getLocalizedMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
            }
        }
    }

    /** Action listener for the <code>Quit</code> menu item. */
    static class QuitActionListener implements ActionListener {

        /**
         * Exits this application.
         *
         * @param event  Object describing the <code>ActionEvent</code>
         *               that resulted in this call.
         */
        public void actionPerformed(final ActionEvent event) {
            System.exit(0);
        }
    }
}
