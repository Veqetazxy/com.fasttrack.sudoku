/*
  Cell.java

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import com.jfasttrack.sudoku.step.ValuePlacementStep;
import com.jfasttrack.sudoku.ui.Settings;


/**
 * A <code>Cell</code> is a small square in a sudoku grid. Each cell can hold one value.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class Cell {

    /** The number of pixels to be used as insets for cells' 3-D rectangles. */
    static final int INSET_SIZE = 3;

    /** The color used to fill cells that are givens. */
    private static final Color GIVEN_BACKGROUND_COLOR = new Color(192, 192, 255);

    /** The color used to fill cells that are not givens. */
    private static final Color CELL_BACKGROUND_COLOR = new Color(224, 224, 255);

    /** The color used to fill cells that are highlighted. */
    private static final Color HIGHLIGHTED_CELL_BACKGROUND_COLOR = new Color(160, 255, 160);

    /** The color used to fill supporting cells. */
    private static final Color SUPPORTING_CELL_BACKGROUND_COLOR = new Color(255, 255, 160);

    /** The color used to draw a 3-D rectangle in each cell. */
    private static final Color RECTANGLE_3D_COLOR = Color.LIGHT_GRAY;

    /** The color used to paint digits (values and candidates). */
    private static final Color DIGIT_COLOR = Color.BLACK;

    /** The font used to draw values in cells. */
    private static Font valueFont;

    /** The font used to draw candidates. */
    private static Font candidateFont;

    /** The current state of this <code>Cell</code>. */
    private CellState state = CellState.UNSOLVED;

    /** The value currently stored in this <code>Cell</code>. */
    private int value;

    /** The values that are candidates of this <code>Cell</code>. */
    private final BitSet candidates;

    /** The dimension of the puzzle grid. */
    private final int gridSize;

    /** The X coordinate of this <code>Cell</code> (zero-based). */
    private final int column;

    /** The Y coordinate of this <code>Cell</code> (zero-based). */
    private final int row;

    /** The index of the block to which this <code>Cell</code> belongs. */
    private int blockIndex;


    /** Interface for listeners to be informed of changes to this <code>Cell</code>'s value. */
    public interface ValueListener {

        /**
         * Called to report a change in this <code>Cell</code>'s <code>value</code>.
         *
         * @param cell  The <code>Cell</code> whose value has been changed.
         * @param step  The <code>Step</code> during which this <code>Cell</code>'s value is
         *              changed. <code>null</code> if there is no such <code>Step</code>.
         */
        void valueChanged(final Cell cell, final ValuePlacementStep step);
    }

    /** A collection of listeners to be informed when this <code>Cell</code>'s value is changed. */
    private final List listeners = new ArrayList();

    /**
     * Constructs an empty <code>Cell</code>, with no value and every possible candidate.
     *
     * @param puzzleSize  The size of the puzzle.
     * @param column      The X coordinate of this <code>Cell</code>.
     * @param row         The Y coordinate of this <code>Cell</code>.
     */
    public Cell(
            final int puzzleSize,
            final int column,
            final int row) {
        this.gridSize = puzzleSize;

        candidates = new BitSet(puzzleSize + 1);
        candidates.set(1, puzzleSize + 1, true);

        this.column = column;
        this.row    = row;
        blockIndex  = -1;
    }

    /**
     * Adds a listener that will be notified when this <code>Cell</code>'s value is changed.
     *
     * @param listener  A listener to be informed when this <code>Cell</code>'s value is changed.
     */
    public void addListener(final ValueListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener  A listener to be removed.
     */
    public void removeListener(final ValueListener listener) {
        listeners.remove(listener);
    }

    /**
     * Sets the <code>state</code> and <code>value</code> of this <code>Cell</code>.
     *
     * @param newState  The new state of this <code>Cell</code>.
     * @param newValue  The new value of this <code>Cell</code>.
     * @param step      The <code>Step</code> in which this state and value are being set.
     *                  <code>null</code> if there is no <code>Step</code>.
     */
    public void setStateAndValue(
            final CellState newState,
            final int newValue,
            final ValuePlacementStep step) {
        state = newState;
        value = newValue;
        if (containsValue()) {
            candidates.clear();
        }

        // Tell the listeners.
        Iterator iterator = listeners.iterator();
        while (iterator.hasNext()) {
            ValueListener listener = (ValueListener) iterator.next();
            listener.valueChanged(this, step);
        }
    }

    /**
     * Gets the current <code>state</code> of this <code>Cell</code>.
     *
     * @return  The current <code>state</code> of this <code>Cell</code>.
     */
    public CellState getState() {
        return state;
    }

    /**
     * Gets the current <code>value</code> of this <code>Cell</code>.
     *
     * @return  The current <code>value</code> of this <code>Cell</code>.
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets whether a specified value is a candidate for this <code>Cell</code>.
     *
     * @param candidateValue  A value.
     * @return                <code>true</code> if the value is a candidate for this
     *                        <code>Cell</code>. Otherwise, <code>false</code>.
     */
    public boolean hasCandidate(final int candidateValue) {
        return candidates.get(candidateValue);
    }

    /**
     * Adds a value as a candidate for this <code>Cell</code>.
     *
     * @param candidateValue  The candidate value to be added.
     */
    public void addCandidate(final int candidateValue) {
        candidates.set(candidateValue, true);
    }

    /**
     * Removes a value as a candidate for this <code>Cell</code>.
     *
     * @param candidateValue  The candidate value to be removed.
     */
    public void removeCandidate(final int candidateValue) {
        candidates.set(candidateValue, false);
    }

    /**
     * Gets a copy of this <code>Cell</code>'s candidates.
     *
     * @return  A copy of this <code>Cell</code>'s candidates.
     */
    public BitSet getCandidates() {
        return (BitSet) candidates.clone();
    }

    /**
     * Gets whether this <code>Cell</code> contains a value.
     *
     * @return  <code>true</code> if this <code>Cell</code>'s <code>state</code> is
     *          <code>GIVEN</code> or <code>SOLVED</code>. Otherwise, <code>false</code>.
     */
    public boolean containsValue() {
        return state == CellState.GIVEN || state == CellState.SOLVED;
    }

    /**
     * Gets the X coordinate of this <code>Cell</code>.
     *
     * @return  The X coordinate of this <code>Cell</code> (zero-based).
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the Y coordinate of this <code>Cell</code>.
     *
     * @return  The Y coordinate of this <code>Cell</code> (zero-based).
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets the index of the block to which this <code>Cell</code> belongs.
     *
     * @return  The index of the block to which this <code>Cell</code> belongs.
     */
    public int getBlockIndex() {
        return blockIndex;
    }

    /**
     * Sets the index of the block that holds this <code>Cell</code>.
     *
     * @param blockIndex  The index of the block that holds this <code>Cell</code>.
     */
    public void setBlockIndex(final int blockIndex) {
        this.blockIndex = blockIndex;
    }

    /**
     * Sets the font used to draw values.
     *
     * @param valueFont  The font used to draw values.
     */
    static void setValueFont(final Font valueFont) {
        Cell.valueFont = valueFont;
    }

    /**
     * Sets the font used to draw candidates.
     *
     * @param candidateFont  The font used to draw candidates.
     */
    static void setCandidateFont(final Font candidateFont) {
        Cell.candidateFont = candidateFont;
    }

    /**
     * Paints this <code>Cell</code>.
     *
     * @param g           The graphics context.
     * @param xOrigin     The X coordinate of the upper-left corner of this <code>Cell</code>.
     * @param yOrigin     The Y coordinate of the upper-left corner of this <code>Cell</code>.
     * @param cellWidth   The width of this <code>Cell</code>, in pixels.
     * @param cellHeight  The height of this <code>Cell</code>, in pixels.
     */
    public void paint(
            final Graphics g,
            final int      xOrigin,
            final int      yOrigin,
            final int      cellWidth,
            final int      cellHeight) {

        paintBackground(g, xOrigin, yOrigin, cellWidth, cellHeight);

        // Paint the diagonals, if needed.
        if (Options.getInstance().isUsingDiagonals()) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(3));
            g.setColor(new Color(160, 160, 160));
            if (column == row) {
                g2d.drawLine(
                        xOrigin + 1,
                        yOrigin + 1,
                        xOrigin + cellWidth - 2,
                        yOrigin + cellHeight - 2);
            }
            if (column + row + 1 == gridSize) {
                g2d.drawLine(
                        xOrigin + 1,
                        yOrigin + cellHeight - 2,
                        xOrigin + cellWidth - 2,
                        yOrigin + 1);
            }
        }

        // Draw the 3-D rectangle.
        g.setColor(RECTANGLE_3D_COLOR);
        g.draw3DRect(
                xOrigin + INSET_SIZE,
                yOrigin + INSET_SIZE,
                cellWidth  - INSET_SIZE * 2,
                cellHeight - INSET_SIZE * 2,
                true);
        g.draw3DRect(
                xOrigin + INSET_SIZE + 1,
                yOrigin + INSET_SIZE + 1,
                cellWidth  - INSET_SIZE * 2 - 2,
                cellHeight - INSET_SIZE * 2 - 2,
                true);

        // Paint the digits, either value or candidates.
        g.setColor(DIGIT_COLOR);
        if (containsValue()) {
            g.setFont(valueFont);
            paintCenteredString(
                    g,
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(getValue())),
                    xOrigin + cellWidth / 2,
                    yOrigin + cellHeight / 2);
        } else {
            if (Settings.getInstance().isShowingCandidates()) {
                g.setFont(candidateFont);
                paintCandidates(g, xOrigin, yOrigin, cellWidth, cellHeight);
            }
        }
    }

    /**
     * Paints this <code>Cell</code>'s background.
     *
     * @param g           The graphics context.
     * @param xOrigin     The X coordinate of the upper-left corner of this <code>Cell</code>.
     * @param yOrigin     The Y coordinate of the upper-left corner of this <code>Cell</code>.
     * @param cellWidth   The width of this <code>Cell</code>, in pixels.
     * @param cellHeight  The height of this <code>Cell</code>, in pixels.
     */
    private void paintBackground(
            final Graphics g,
            final int      xOrigin,
            final int      yOrigin,
            final int      cellWidth,
            final int      cellHeight) {

        Settings settings = Settings.getInstance();

        // Draw the (filled) background rectangle.
        Color backgroundColor = CELL_BACKGROUND_COLOR;
        g.setColor(backgroundColor);
        g.fillRect(xOrigin, yOrigin, cellWidth, cellHeight);

        // Draw the cell's highlight background, if any.
        Color highlightColor = state.getColor();
        if (state == CellState.GIVEN) {
            highlightColor = GIVEN_BACKGROUND_COLOR;
        }
        if (settings.shouldHighlight(this)) {
            highlightColor = HIGHLIGHTED_CELL_BACKGROUND_COLOR;
        } else if (settings.hasSupportingCell(this)) {
            highlightColor = SUPPORTING_CELL_BACKGROUND_COLOR;
        } else if (this.hasCandidate(settings.getHighlightedCandidateValue())) {
            highlightColor = HIGHLIGHTED_CELL_BACKGROUND_COLOR;
        }
        if (!highlightColor.equals(backgroundColor)) {
            g.setColor(highlightColor);
            g.fillRect(
                    xOrigin + INSET_SIZE + 1,
                    yOrigin + INSET_SIZE + 1,
                    cellWidth   - INSET_SIZE * 2 - 2,
                    cellHeight  - INSET_SIZE * 2 - 2);
        }
    }

    /**
     * Paints the candidates of this <code>Cell</code>.
     *
     * @param g           The graphics context.
     * @param xOrigin     The X coordinate of the upper-left corner of this <code>Cell</code>.
     * @param yOrigin     The Y coordinate of the upper-left corner of this <code>Cell</code>.
     * @param cellWidth   The width of this <code>Cell</code>, in pixels.
     * @param cellHeight  The height of this <code>Cell</code>, in pixels.
     */
    private void paintCandidates(
            final Graphics g,
            final int      xOrigin,
            final int      yOrigin,
            final int      cellWidth,
            final int      cellHeight) {

//TODO: Re-arrange the candidates for sizes like 10. Calculate those placements only once.
        int numberOfRows = (int) Math.sqrt(gridSize);
        int numberOfColumns = gridSize / numberOfRows;
        if (numberOfRows * numberOfColumns != gridSize) {
            numberOfColumns++;
        }
        int rowHeight = (cellHeight - INSET_SIZE * 2 - 2) / numberOfRows;
        int columnWidth = (cellWidth - INSET_SIZE * 2 - 2) / numberOfColumns;

        for (int candidateValue = 1;
                candidateValue <= gridSize;
                candidateValue++) {
            if (this.hasCandidate(candidateValue)) {
                int xCenter =
                        xOrigin + (candidateValue - 1) % numberOfColumns * columnWidth
                        + columnWidth / 2
                        + INSET_SIZE
                        + 2;
                int yCenter =
                        yOrigin + (candidateValue - 1) / numberOfColumns * rowHeight
                        + rowHeight / 2
                        + INSET_SIZE
                        + 2;
                paintCenteredString(
                    g,
                    String.valueOf(PuzzleDelegate.CHARACTERS.charAt(candidateValue)),
                    xCenter,
                    yCenter);
            }
        }
    }

    /**
     * Paints a centered <code>String</code>.
     *
     * @param g        The graphics context.
     * @param string   The <code>String</code> to be drawn.
     * @param xCenter  The X coordinate of the center of the <code>String</code>.
     * @param yCenter  The Y coordinate of the center of the <code>String</code>.
     */
    private static void paintCenteredString(
            final Graphics g,
            final String   string,
            final int      xCenter,
            final int      yCenter) {
        Rectangle2D bounds = g.getFontMetrics().getStringBounds(string, g);
        int stringX = (int) (xCenter - bounds.getCenterX());
        int stringY = (int) (yCenter - bounds.getCenterY());
        g.drawString(string, stringX, stringY);
    }
}
