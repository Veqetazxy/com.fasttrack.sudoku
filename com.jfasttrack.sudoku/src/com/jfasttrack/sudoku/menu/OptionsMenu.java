/*
  OptionsMenu.java

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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import com.jfasttrack.sudoku.DancingLinksSudoku;
import com.jfasttrack.sudoku.ui.MessageBundle;
import com.jfasttrack.sudoku.ui.Settings;


/**
 * The options menu gives the user a way to set program options.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class OptionsMenu extends JMenu {

    /** Message bundle that holds all messages for this program. */
    private static final MessageBundle MESSAGE_BUNDLE = MessageBundle.getInstance();

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the options menu.
     *
     * @param owner  The program that owns this menu.
     */
    public OptionsMenu(final DancingLinksSudoku owner) {
        super(MESSAGE_BUNDLE.getString("menu.options"));
        setMnemonic(MESSAGE_BUNDLE.getString("menu.options.accelerator").charAt(0));

        // Create the "show candidates" menu item.
        final JCheckBoxMenuItem showCandidatesMenuItem = new JCheckBoxMenuItem(
                MESSAGE_BUNDLE.getString("menu.options.show.candidates"));
        showCandidatesMenuItem.setSelected(Settings.getInstance().isShowingCandidates());
        showCandidatesMenuItem.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent event) {
                Settings.getInstance().setShowingCandidates(
                        showCandidatesMenuItem.isSelected());
                owner.getPuzzleDelegate().repaint();
            }
        });
        add(showCandidatesMenuItem);

        // Create the "show timer" menu item.
        final JCheckBoxMenuItem showTimerMenuItem = new JCheckBoxMenuItem(
                MESSAGE_BUNDLE.getString("menu.options.show.timer"));
        showTimerMenuItem.setSelected(
                Settings.getInstance().isShowingCandidates());
        showTimerMenuItem.addItemListener(new ItemListener() {
            public void itemStateChanged(final ItemEvent event) {
                boolean visible = showTimerMenuItem.isSelected();
                Settings.getInstance().setShowingTimer(visible);
                owner.setTimerVisible(visible);
            }
        });
        add(showTimerMenuItem);
    }
}
