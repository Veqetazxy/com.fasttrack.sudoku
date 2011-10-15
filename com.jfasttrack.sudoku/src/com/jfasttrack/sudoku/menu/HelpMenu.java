/*
  HelpMenu.java

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
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.jfasttrack.sudoku.DancingLinksSudoku;
import com.jfasttrack.sudoku.ui.MessageBundle;


/**
 * The help menu provides online help for the user.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class HelpMenu extends JMenu {

    /** Message bundle that holds all messages for this program. */
    static final MessageBundle MESSAGE_BUNDLE = MessageBundle.getInstance();

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the help menu.
     *
     * @param owner  The program that owns this menu.
     */
    public HelpMenu(final DancingLinksSudoku owner) {
        super(MESSAGE_BUNDLE.getString("menu.help"));
        setMnemonic(MESSAGE_BUNDLE.getString("menu.help.accelerator").charAt(0));

        // Create the "help/about" menu item.
        JMenuItem aboutMenuItem = new JMenuItem(MESSAGE_BUNDLE.getString("menu.help.about"));
        aboutMenuItem.setMnemonic(
                MESSAGE_BUNDLE.getString("menu.help.about.accelerator").charAt(0));
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                Object[] message = new Object[] {
                    "              " + MESSAGE_BUNDLE.getString("program.name"),
                    MESSAGE_BUNDLE.getString("copyright"),
                    "             " + MESSAGE_BUNDLE.getString(
                            "version.date",
                            new String[] {
                                owner.getClass().getPackage().getImplementationVersion(),
                            }
                    ),
                };
                JOptionPane.showMessageDialog(
                        owner,
                        message,
                        MESSAGE_BUNDLE.getString("program.name"),
                        JOptionPane.INFORMATION_MESSAGE,
                        new ImageIcon(getClass().getResource("/data/Rocky.jpeg")));
            }
        });
        add(aboutMenuItem);
    }
}
