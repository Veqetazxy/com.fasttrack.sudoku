/*
  Settings.java

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.jfasttrack.sudoku.puzzle.Cell;


/**
 * This class is a collection of GUI settings. It includes selections such as cells and candidate
 * values to be highlighted.
 * <br/>
 * This class is a singleton.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public final class Settings {

    /** The singleton instance of this class. */
    private static final Settings INSTANCE = new Settings();

    /**
     * The <code>Cell</code>s to be highlighted because a hint is showing that they can be changed.
     */
    private final Set highlightedCells = new HashSet();

    /**
     * The <code>Cell</code>s to be highlighted as supporting cells in a hint. These
     * <code>Cell</code>s are not changed by a step; They are highlighted in a different color
     * to help explain a hint.
     */
    private final Set supportingCells = new HashSet();

    /** Tells whether candidates of unsolved <code>Cell</code>s should be displayed. */
    private boolean showingCandidates = true;

    /** Tells whether the timer should be displayed. */
    private boolean showingTimer = true;

    /** The candidate value to be highlighted. */
    private int highlightedCandidateValue;

    /** Private constructor to keep anyone from instantiating this class. */
    private Settings() {
        // Nothing to do here.
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return  The singleton instance of this class.
     */
    public static Settings getInstance() {
        return INSTANCE;
    }

    /** Clear the collection of <code>Cell</code>s to be highlighted. */
    public void clearHighlightedCells() {
        highlightedCells.clear();
    }

    /**
     * Adds a <code>Cell</code> to the collection of highlighted <code>Cell</code>s.
     *
     * @param cell  The <code>Cell</code> to be added.
     */
    public void addHighlightedCell(final Cell cell) {
        highlightedCells.add(cell);
    }

    /**
     * Removes a <code>Cell</code> from the collection of highlighted <code>Cell</code>s.
     *
     * @param cell  The <code>Cell</code> to be removed.
     */
    public void removeHighlightedCell(final Cell cell) {
        highlightedCells.remove(cell);
    }

    /**
     * Gets the number of highlighted <code>Cell</code>s.
     *
     * @return  The number of highlighted <code>Cell</code>s.
     */
    public int getHighlightedCellCount() {
        return highlightedCells.size();
    }

    /**
     * Gets an <code>Iterator</code> over the highlighted <code>Cell</code>s.
     *
     * @return  An <code>Iterator</code> over the highlighted <code>Cell</code>s.
     */
    public Iterator getHighlightedCells() {
        return highlightedCells.iterator();
    }

    /**
     * Gets whether the specified <code>Cell</code> is to be highlighted.
     *
     * @param cell  A <code>Cell</code>.
     * @return      <code>true</code> if the <code>Cell</code> is to be highlighted. Otherwise,
     *              <code>false</code>.
     */
    public boolean shouldHighlight(final Cell cell) {
        return highlightedCells.contains(cell);
    }

    /**
     * Clears the collection of <code>Cell</code>s to be highlighted to provide extra information
     * to the user (as in a hint).
     */
    public void clearSupportingCells() {
        supportingCells.clear();
    }

    /**
     * Adds a <code>Cell</code> to the collection of supporting <code>Cell</code>s.
     *
     * @param cell  The <code>Cell</code> to be added.
     */
    public void addSupportingCell(final Cell cell) {
        supportingCells.add(cell);
    }

    /**
     * Gets whether the specified <code>Cell</code> is to be displayed as a supporting
     * <code>Cell</code>.
     *
     * @param cell  A <code>Cell</code>.
     * @return      <code>true</code> if the <code>Cell</code> is to be highlighted. Otherwise,
     *              <code>false</code>.
     */
    public boolean hasSupportingCell(final Cell cell) {
        return supportingCells.contains(cell);
    }

    /**
     * Sets whether candidates of unsolved <code>Cell</code>s are visible.
     *
     * @param showingCandidates  <code>true</code> if candidates of unsolved <code>Cell</code> are
     *                           to be displayed. Otherwise, <code>false</code>.
     */
    public void setShowingCandidates(final boolean showingCandidates) {
        this.showingCandidates = showingCandidates;
    }

    /**
     * Gets whether candidates of unsolved <code>Cell</code>s are visible.
     *
     * @return  <code>true</code> if candidates of unsolved <code>Cell</code>s are to be displayed.
     *          Otherwise, <code>false</code>.
     */
    public boolean isShowingCandidates() {
        return showingCandidates;
    }

    /**
     * Sets whether the timer is visible.
     *
     * @param showingTimer  <code>true</code> if the timer is to be displayed.
     *                      Otherwise, <code>false</code>.
     */
    public void setShowingTimer(final boolean showingTimer) {
        this.showingTimer = showingTimer;
    }

    /**
     * Gets whether the timer is visible.
     *
     * @return  <code>true</code> if the timer is to be displayed. Otherwise, <code>false</code>.
     */
    public boolean isShowingTimer() {
        return showingTimer;
    }

    /**
     * Sets the candidate value to be highlighted.
     *
     * @param highlightedCandidateValue  The candidate value to be highlighted.
     */
    public void setHighlightedCandidateValue(final int highlightedCandidateValue) {
        this.highlightedCandidateValue = highlightedCandidateValue;
    }

    /**
     * Gets the candidate value to be highlighted.
     *
     * @return  The candidate value to be highlighted.
     */
    public int getHighlightedCandidateValue() {
        return highlightedCandidateValue;
    }
}
