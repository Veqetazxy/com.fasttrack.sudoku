/*
  TimerPanel.java

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;


/**
 * The <code>TimerPanel</code> displays the amount of time elapsed since the user started to
 * solve the currently displayed sudoku.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public final class TimerPanel extends JPanel implements ActionListener {

    /** The singleton instance of this class. */
    private static final TimerPanel INSTANCE = new TimerPanel();

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The timer used to count elapsed time. */
    private final Timer timer;

    /** The label that displays the formatted time. */
    private final JLabel statusLabel = new JLabel(" 0:00 ");

    /** The elapsed time (tenth of a second) since the user started solving the current sudoku. */
    private int timerTicks;

    /** Constructs a <code>TimerPanel</code>. */
    private TimerPanel() {
        super(new FlowLayout(FlowLayout.RIGHT, 0, 0));

        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        statusLabel.setForeground(Color.black);
        add(statusLabel);

        timer = new Timer(100, this);
        timer.start();
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @return  A reference to the singleton instance of this class.
     */
    public static TimerPanel getInstance() {
        return INSTANCE;
    }

    /**
     * Called whenever a timer tick occurs (every tenth of a second). Updates the internal tick
     * count, then reformats and displays the amount of elapsed time.
     *
     * @param event  Object describing the timer event that caused this call.
     */
    public void actionPerformed(final ActionEvent event) {
        timerTicks++;

        int seconds = timerTicks / 10;
        int minutes = (seconds % 3600) / 60;
        int hours = seconds / 3600;
        seconds %= 60;

        StringBuffer buffer = new StringBuffer();
        buffer.append(Integer.toString(hours));

        buffer.append(':');
        String minutesString = Integer.toString(minutes);
        if (minutesString.length() < 2) {
            buffer.append('0');
        }
        buffer.append(minutesString);

        buffer.append(':');
        String secondsString = Integer.toString(seconds);
        if (secondsString.length() < 2) {
            buffer.append('0');
        }
        buffer.append(secondsString);

        statusLabel.setText(buffer.toString());
    }

    /** Starts the timer. */
    public void start() {
        timer.start();
    }

    /** Stops the timer. */
    public void stop() {
        timer.stop();
    }

    /** Restarts the timer. */
    public void restart() {
        timer.restart();
        timerTicks = 0;
    }

    /**
     * Gets the preferred size of this component.
     *
     * @return  The preferred size of this component.
     */
    public Dimension getPreferredSize() {
        return new Dimension(54, super.getPreferredSize().height);
    }
}
