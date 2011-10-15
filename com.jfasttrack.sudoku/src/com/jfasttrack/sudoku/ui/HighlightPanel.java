/*
  HighlightPanel.java

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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.jfasttrack.sudoku.puzzle.AbstractPuzzleModel;
import com.jfasttrack.sudoku.puzzle.Cell;
import com.jfasttrack.sudoku.puzzle.PuzzleDelegate;
import com.jfasttrack.sudoku.step.ValuePlacementStep;


/**
 * The <code>HighlightPanel</code> is a panel containing a group of <code>HighlightButton</code>s.
 * These buttons tell which candidate values the user wants highlighted (displayed in a different
 * color).
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class HighlightPanel extends JPanel implements ActionListener, Cell.ValueListener {

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The puzzle delegate containing <code>Cell</code>s to be highlighted. */
    private final PuzzleDelegate puzzleDelegate;

    /** The group containing all <code>HighlightButton</code>s. */
    private final ButtonGroup group = new ButtonGroup();

    /** The buttons used to highlight <code>Cell</code>s with specific candidates. */
    private final List highlightButtons = new ArrayList();

    /**
     * Constructs a <code>HighlightPanel</code>.
     *
     * @param puzzleDelegate  The puzzle delegate containing <code>Cell</code>s to be highlighted.
     */
    public HighlightPanel(final PuzzleDelegate puzzleDelegate) {
        this.puzzleDelegate = puzzleDelegate;

        setLayout(new FlowLayout());

        add(new JLabel(MessageBundle.getInstance().getString("option.highlight") + ":"));

        // Selection of this button removes all highlighting.
        HighlightButton button = new HighlightButton("  ");
        group.add(button);
        button.addActionListener(this);
        add(button);
        highlightButtons.add(button);

        // These buttons are used to select values to highlight.
        for (int i = 1; i < puzzleDelegate.getPuzzleModel().getGridSize() + 1; i++) {
            button = new HighlightButton(String.valueOf(PuzzleDelegate.CHARACTERS.charAt(i)));
            group.add(button);
            button.addActionListener(this);
            add(button);
            highlightButtons.add(button);
        }

        AbstractPuzzleModel puzzle = puzzleDelegate.getPuzzleModel();
        Iterator i = puzzle.getAllCells();
        while (i.hasNext()) {
            Cell cell = (Cell) i.next();
            cell.addListener(this);
        }
    }

    /** Clears the selection, so no <code>Cell</code>s are highlighted. */
    public void clearSelection() {
        group.setSelected(((HighlightButton) highlightButtons.get(0)).getModel(), true);

        Settings guiSettings = Settings.getInstance();
        guiSettings.setHighlightedCandidateValue(0);
        guiSettings.clearHighlightedCells();
        guiSettings.clearSupportingCells();
    }

    /**
     * Enables or disables every button in this panel. (Buttons are enabled while solving and
     * disabled when manually entering a puzzle.)
     *
     * @param enabled  <code>true</code> to enable each button. <code>false</code> to disable each
     *                 button.
     */
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        Enumeration buttons = group.getElements();
        while (buttons.hasMoreElements()) {
            ((JToggleButton) buttons.nextElement()).setEnabled(enabled);
        }
    }

    /**
     * Called when a <code>HighlightButton</code> is selected. Stores the button's value into the
     * GUI settings, then repaints the puzzle.
     *
     * @param event  Object describing that event that caused this method to be called.
     */
    public void actionPerformed(final ActionEvent event) {
        int buttonValue = highlightButtons.indexOf(event.getSource());
        if (buttonValue > 0) {
            Settings guiSettings = Settings.getInstance();
            guiSettings.setHighlightedCandidateValue(buttonValue);
            guiSettings.clearHighlightedCells();
            guiSettings.clearSupportingCells();
        } else {
            clearSelection();
        }

        puzzleDelegate.repaint();
    }

    /**
     * Called whenever a value is placed into a <code>Cell</code>. Checks the puzzle to see
     * whether there are still any unsolved <code>Cell</code>s that have the same value. If not,
     * then disables the highlight button with that value.
     *
     * @param cell  The <code>Cell</code> that has been solved.
     * @param step  The step that caused the value to be placed into the <code>Cell</code>
     *              (unused).
     */
    public void valueChanged(final Cell cell, final ValuePlacementStep step) {
        if (this.isEnabled()) {
            AbstractPuzzleModel puzzle = puzzleDelegate.getPuzzleModel();
            int size = puzzle.getGridSize();
            int[] valueCounts = new int[size + 1];

            Iterator i = puzzle.getAllCells();
            while (i.hasNext()) {
                Cell cell1 = (Cell) i.next();
                if (cell1.containsValue()) {
                    valueCounts[cell1.getValue()]++;
                }
            }

            for (int value = 1; value <= size; value++) {
                HighlightButton button = (HighlightButton) highlightButtons.get(value);
                button.setSelected(false);
                button.setEnabled(valueCounts[value] < size);
            }
        }
    }

    /**
     * Gets the preferred size of this <code>HighlightPanel</code>.
     *
     * @return  The preferred size of this <code>HighlightPanel</code>.
     */
    public Dimension getPreferredSize() {

        /*
         * This is here in case there are more HighlightButtons than will fit across one line of
         * the panel. When using FlowLayout, the preferred size always assumes a single line of
         * components. If the buttons will not fit across a single line, then recalculate the
         * height to accommodate a second line.
         */

        Dimension preferredSize = super.getPreferredSize();
        int width = getParent().getSize().width;
        int height = preferredSize.height;
        if (preferredSize.width > width) {
            FlowLayout lm = (FlowLayout) getLayout();
            height += lm.getVgap()
                    + ((HighlightButton) highlightButtons.get(0)).getPreferredSize().height;
        }
        return new Dimension(width, height);
    }
}
