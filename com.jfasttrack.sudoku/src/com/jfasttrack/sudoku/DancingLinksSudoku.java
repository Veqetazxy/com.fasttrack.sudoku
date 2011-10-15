/*
  DancingLinksSudoku.java

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

package com.jfasttrack.sudoku;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import com.jfasttrack.sudoku.menu.EditMenu;
import com.jfasttrack.sudoku.menu.FileMenu;
import com.jfasttrack.sudoku.menu.HelpMenu;
import com.jfasttrack.sudoku.menu.OptionsMenu;
import com.jfasttrack.sudoku.menu.SolveMenu;
import com.jfasttrack.sudoku.puzzle.AbstractPuzzleModel;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.ui.HighlightPanel;
import com.jfasttrack.sudoku.ui.MessageBundle;
import com.jfasttrack.sudoku.ui.MessagePanel;
import com.jfasttrack.sudoku.ui.TimerPanel;


/**
 * This is the main program to run Dancing Links Sudoku as either an applet or an application.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class DancingLinksSudoku extends JApplet {

//TODO: i18n for the menu mnemonics and accelerators.

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Tells whether this program is running as an application. (<code>false</code> means it's
     * running as an applet.)
     */
    private boolean runningAsApplication;

    /** The puzzle currently being displayed and solved. */
    private PuzzleDelegate puzzleDelegate;

    /**
     * Panel containing buttons that let the user select cells to be displayed in a different color.
     */
    private HighlightPanel highlightPanel;

    /** A place to display hints and status messages. */
    private final MessagePanel messagePanel = new MessagePanel();

    /** Label that displays the elapsed time since solving was started. */
    private final TimerPanel timerPanel = TimerPanel.getInstance();

    /** The File menu. */
    private FileMenu fileMenu;

    /** Initializes <code>DancingLinksSudoku</code>. */
    public void init() {
        setJMenuBar(createMenuBar());
        setContentPane(createContentPane());
        setFocusable(true);
    }

    /**
     * Creates the menu bar and all of its menus.
     *
     * @return  A reference to the menu bar.
     */
    private JMenuBar createMenuBar() {
        JMenuBar mainMenu = new JMenuBar();

        fileMenu = new FileMenu(this);
        mainMenu.add(fileMenu);
        EditMenu.setOwner(this);
        EditMenu editMenu = EditMenu.getInstance();
        mainMenu.add(editMenu);
        mainMenu.add(new SolveMenu(this));
        mainMenu.add(new OptionsMenu(this));
        mainMenu.add(new HelpMenu(this));

        return mainMenu;
    }

    /**
     * Gets a reference to the File menu.
     *
     * @return  A reference to the File menu.
     */
    public FileMenu getFileMenu() {
        return fileMenu;
    }

    /**
     * Creates the content pane and all of its components.
     *
     * @return  The new content pane.
     */
    private Container createContentPane() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        puzzleDelegate = new PuzzleDelegate(this);
        highlightPanel = new HighlightPanel(puzzleDelegate);
        contentPane.add(highlightPanel, BorderLayout.NORTH);
        contentPane.add(puzzleDelegate, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(timerPanel, BorderLayout.EAST);
        bottomPanel.add(messagePanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        return contentPane;
    }

    /**
     * Sets the <code>PuzzleModel</code>.
     *
     * @param puzzleModel  The new <code>PuzzleModel</code>.
     */
    public void setPuzzleModel(final AbstractPuzzleModel puzzleModel) {
        puzzleDelegate.setPuzzleModel(puzzleModel);

        getContentPane().remove(highlightPanel);
        highlightPanel = new HighlightPanel(puzzleDelegate);
        getContentPane().add(highlightPanel, BorderLayout.NORTH);

        // A new puzzle has no moves to undo or redo.
        EditMenu editMenu = EditMenu.getInstance();
        editMenu.setUndoEnabled(false);
        editMenu.setRedoEnabled(false);

        highlightPanel.repaint();
    }

    /**
     * Gets whether this program is running as an application.
     *
     * @return  <code>true</code> if this program is running as an application. <code>false</code>
     *          if this program is running as an applet.
     */
    public boolean isRunningAsApplication() {
        return runningAsApplication;
    }

    /**
     * Gets the <code>PuzzleDelegate</code>.
     *
     * @return  The <code>PuzzleDelegate</code>.
     */
    public PuzzleDelegate getPuzzleDelegate() {
        return puzzleDelegate;
    }

    /**
     * Gets the <code>HighlightPanel</code>.
     *
     * @return  The <code>HighlightPanel</code>.
     */
    public HighlightPanel getHighlightPanel() {
        return highlightPanel;
    }

    /**
     * Gets the <code>MessagePanel</code>.
     *
     * @return  A reference to the <code>MessagePanel</code>.
     */
    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    /**
     * Gets the <code>TimerPanel</code>.
     *
     * @return  A reference to the <code>TimerPanel</code>.
     */
    public TimerPanel getTimerPanel() {
        return timerPanel;
    }

    /** Starts the timer. */
    public void start() {
        timerPanel.start();
    }

    /** Stops the timer. */
    public void stop() {
        timerPanel.stop();
    }

    /**
     * Sets the visibility of the timer.
     *
     * @param visible  <code>true</code> if the timer is to become visible. Otherwise,
     *                 <code>false</code>.
     */
    public void setTimerVisible(final boolean visible) {
        timerPanel.setVisible(visible);
    }

    /**
     * The main program to run <code>DancingLinksSudoku</code> as an application.
     *
     * @param args  Command-line parameters (unused).
     */
    public static void main(final String[] args) {
        MessageBundle messageBundle = MessageBundle.getInstance();
        JFrame frame = new JFrame(messageBundle.getString("program.name"));

        final DancingLinksSudoku dancingLinksSudoku = new DancingLinksSudoku();
        dancingLinksSudoku.runningAsApplication = true;

        frame.setJMenuBar(dancingLinksSudoku.createMenuBar());

        frame.setContentPane(dancingLinksSudoku.createContentPane());

        frame.setSize(620, 724);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowDeiconified(final WindowEvent event) {
                dancingLinksSudoku.start();
            }
            public void windowIconified(final WindowEvent event) {
                dancingLinksSudoku.stop();
            }
        });

        frame.setVisible(true);
    }
}
