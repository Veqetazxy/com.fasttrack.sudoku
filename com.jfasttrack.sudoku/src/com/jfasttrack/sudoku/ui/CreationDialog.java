/*
  CreationDialog.java

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

package com.jfasttrack.sudoku.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jfasttrack.sudoku.DancingLinksSudoku;
import com.jfasttrack.sudoku.puzzle.Options;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.puzzle.StandardSudoku;
import com.jfasttrack.sudoku.puzzle.Options.BlockType;


/**
 * This is the dialog used to select the options used when creating a new sudoku.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class CreationDialog extends JDialog implements ChangeListener {

    /** Message bundle that holds all messages for this program. */
    private static final MessageBundle MESSAGE_BUNDLE = MessageBundle.getInstance();

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The owner of this dialog. */
    private final DancingLinksSudoku program;

    /** The spinner used to select the size of the sudoku to be generated. */
    private final JSpinner gridSizeSpinner =
            new JSpinner(new SpinnerNumberModel(Options.getInstance().getGridSize(), 4, 16, 1));

    /**
     * The combo box used to select the type and dimensions of blocks in the sudoku to be generated.
     */
    private final JComboBox blocksComboBox = new JComboBox();

    /** The check box that tells whether the sudoku is to have diagonal houses. */
    private final JCheckBox diagonalsCheckBox =
            new JCheckBox(MESSAGE_BUNDLE.getString("create.diagonals"));

    /**
     * Constructs a <code>CreationDialog</code>.
     *
     * @param parentFrame  The frame that is the parent of this modal dialog.
     * @param program      The owner of this dialog.
     */
    public CreationDialog(final Frame parentFrame, final DancingLinksSudoku program) {
        super(parentFrame, MESSAGE_BUNDLE.getString("create.title"), true);

        this.program = program;

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBackground(Color.LIGHT_GRAY);

        contentPane.add(createChoicesPanel(), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout());
        southPanel.add(createButtonPanel());
        contentPane.add(southPanel, BorderLayout.SOUTH);

        // Reduce the size of this dialog and center it on the screen.
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (screenSize.width  - getPreferredSize().width) / 2,
                (screenSize.height - getPreferredSize().height) / 2);
    }

    /**
     * Constructs a panel containing the buttons used when setting the parameters for a new sudoku.
     *
     * @return  A panel containing the buttons used when setting the parameters for a new sudoku.
     */
    private JPanel createChoicesPanel() {
        JPanel choicesPanel = new JPanel(new GridLayout(0, 2, 6, 4));
        choicesPanel.add(new JLabel(MESSAGE_BUNDLE.getString("create.size"), JLabel.RIGHT));
        JFormattedTextField textField =
                ((JSpinner.DefaultEditor) gridSizeSpinner.getEditor()).getTextField();
        textField.setEditable(false);
        gridSizeSpinner.getEditor().setEnabled(false);
        gridSizeSpinner.addChangeListener(this);
        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sizePanel.add(gridSizeSpinner);
        choicesPanel.add(sizePanel);

        choicesPanel.add(new JLabel(MESSAGE_BUNDLE.getString("create.blocks"), JLabel.RIGHT));
        loadBlocksComboBox();
        JPanel blocksPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        blocksPanel.add(blocksComboBox);
        choicesPanel.add(blocksPanel);

        choicesPanel.add(new JLabel("              "
                + MESSAGE_BUNDLE.getString("create.extra.houses"), JLabel.RIGHT));
        diagonalsCheckBox.setSelected(Options.getInstance().isUsingDiagonals());
        choicesPanel.add(diagonalsCheckBox);

        return choicesPanel;
    }

    /**
     * Constructs a panel containing the buttons used when setting the parameters for a new sudoku.
     *
     * @return  A panel containing the buttons used when setting the parameters for a new sudoku.
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 4, 4));

        JButton okButton = new JButton(MESSAGE_BUNDLE.getString("create.generate"));
        okButton.addActionListener(new OKActionListener());
        buttonPanel.add(okButton);

        JButton clearButton = new JButton(MESSAGE_BUNDLE.getString("create.empty.grid"));
        clearButton.addActionListener(new ClearActionListener());
        buttonPanel.add(clearButton);

        JButton cancelButton = new JButton(MESSAGE_BUNDLE.getString("cancel"));
        cancelButton.addActionListener(new CancelActionListener());
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    /** Copies the user-selected parameters into the <code>Options</code>. */
    void setOptions() {
        final Options options = Options.getInstance();
        options.setGridSize(((Integer) gridSizeSpinner.getValue()).intValue());
        if (blocksComboBox.getSelectedIndex() == 0) {
            options.setBlockType(BlockType.JIGSAW);
        } else {
            options.setBlockType(BlockType.RECTANGULAR);
            StringTokenizer st =
                    new StringTokenizer((String) blocksComboBox.getSelectedItem(), "x");
            options.setBlockHeight(Integer.parseInt(st.nextToken()));
            options.setBlockWidth(Integer.parseInt(st.nextToken()));
        }
        options.setUsingDiagonals(diagonalsCheckBox.isSelected());
    }

    /**
     * Gets the insets of this dialog.
     *
     * @return  The insets of this dialog.
     */
    public Insets getInsets() {
        return new Insets(44, 12, 12, 12);
    }

    /**
     * Paints this dialog.
     *
     * @param g  The graphics context.
     */
    public void paint(final Graphics g) {
        g.setColor(super.getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paint(g);
    }

    /**
     * Clears the blocks combo box and then reloads it with block sizes appropriate for the
     * selected grid size.
     *
     * @param event  Object describing the event that caused this call.
     */
    public void stateChanged(final ChangeEvent event) {
        loadBlocksComboBox();
    }

    /**
     * Called during initialization and each time the user selects a different grid size for a
     * sudoku. Clears the blocks combo box and then reloads it with block sizes appropriate for
     * the selected grid size.
     */
    private void loadBlocksComboBox() {
        blocksComboBox.removeAllItems();

        blocksComboBox.addItem(MESSAGE_BUNDLE.getString("create.jigsaw"));
        int gridSize = ((Integer) gridSizeSpinner.getValue()).intValue();
        for (int i = 2; i < gridSize - 1; i++) {
            if (gridSize % i == 0) {
                blocksComboBox.addItem(i + "x" + gridSize / i);
            }
        }
    }

    /**
     * Gets the program that owns this dialog.
     *
     * @return  A reference to the program that owns this dialog.
     */
    DancingLinksSudoku getProgram() {
        return program;
    }

    /** This class responds to the OK button. */
    class OKActionListener implements ActionListener {

        /**
         * Responds to the OK button by generating a new sudoku.
         *
         * @param event  Object describing the event that caused this call.
         */
        public void actionPerformed(final ActionEvent event) {
            final DancingLinksSudoku mainProgram = getProgram();

            setOptions();

            setVisible(false);
            dispose();

//TODO: Move these into class FileMenu.
            Options.getInstance().setCreateAction(Options.CreateAction.GENERATE);
            mainProgram.setPuzzleModel(new StandardSudoku());
            mainProgram.getPuzzleDelegate().setOperatingMode(
                    PuzzleDelegate.OperatingMode.SOLVING_MODE);
//            startRestartMenuItem.setText(messageBundle.getString("menu.file.start"));
            mainProgram.getHighlightPanel().setEnabled(true);
            mainProgram.getHighlightPanel().clearSelection();
            mainProgram.getMessagePanel().clear();
            mainProgram.getTimerPanel().restart();
            mainProgram.getTimerPanel().setVisible(true);
            mainProgram.repaint();
        }

    }

    /** This class responds to the clear button. */
    class ClearActionListener implements ActionListener {

        /**
         * Responds to the clear button by creating an empty sudoku grid.
         *
         * @param event  Object describing the event that caused this call.
         */
        public void actionPerformed(final ActionEvent event) {
            final DancingLinksSudoku mainProgram = getProgram();

            setOptions();

            setVisible(false);
            dispose();

            Options.getInstance().setCreateAction(Options.CreateAction.CREATE_EMPTY);
            mainProgram.setPuzzleModel(new StandardSudoku());

            if (Options.getInstance().getBlockType() == BlockType.RECTANGULAR) {
                mainProgram.getPuzzleDelegate().setOperatingMode(
                        PuzzleDelegate.OperatingMode.ENTERING_GIVENS);
            } else {
                mainProgram.getPuzzleDelegate().setOperatingMode(
                        PuzzleDelegate.OperatingMode.ENTERING_BLOCKS);
            }
            mainProgram.getHighlightPanel().clearSelection();
            mainProgram.getHighlightPanel().setEnabled(false);
            mainProgram.getFileMenu().getStartRestartMenuItem().setText(
                    MESSAGE_BUNDLE.getString("menu.file.start"));
//TODO: Disable the solvers here.
            mainProgram.getMessagePanel().clear();
//TODO: Handle the timer here.
            mainProgram.getTimerPanel().setVisible(false);
        }
    }

    /** This class responds to the cancel button. */
    class CancelActionListener implements ActionListener {

        /**
         * Responds to the cancel button by removing this dialog from the screen.
         *
         * @param event  Object describing the event that caused this call.
         */
        public void actionPerformed(final ActionEvent event) {
            setVisible(false);
            dispose();
        }
    }
}
