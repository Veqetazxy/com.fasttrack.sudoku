/*
  PentominoApplet.java

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

package com.jfasttrack.pentomino;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import com.jfasttrack.dlx.Node;
import com.jfasttrack.dlx.SolutionListener;


/**
 * This is the main program to run Dancing Links Pentominoes.
 *
 * @author   Pete Boton
 * @version  2009/05
 */
public class PentominoApplet extends JApplet implements ActionListener, SolutionListener {

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The total number of squares in a pentomino grid. */
    private static final int NUMBER_OF_SQUARES = 60;

    /** The available widths for pentomino grids. */
    private static final int[] GRID_WIDTHS = {
        20, 15, 12, 10,
    };

    /** The available sizes for pentomino grids. */
    private static final String[] GRID_SIZES = {
        "3x20",
        "4x15",
        "5x12",
        "6x10",
    };

    /** Model that will hold solutions to be displayed. */
    private final DefaultListModel listModel = new DefaultListModel();

    /** List that displays the solutions to pentomino puzzles. */
    private final JList solutionList = new JList(listModel);

    /** The height (number of squares) of the grid to be filled. */
    private int gridHeight = 3;

    /** The width (number of squares) of the grid to be filled. */
    private int gridWidth = NUMBER_OF_SQUARES / gridHeight;

    /** Label that displays the number of solutions found. */
    private final JLabel solutionLabel = new JLabel("");

    /** Initializes this applet. Creates the GUI, then runs a solver with default values. */
    public void init() {
        UIManager.put("Panel.background", new Color(212, 232, 255));

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new FlowLayout());
        northPanel.add(new JLabel("Select grid size: "));
        JComboBox sizeComboBox = new JComboBox(GRID_SIZES);
        sizeComboBox.addActionListener(this);
        northPanel.add(sizeComboBox);

        contentPane.add(northPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new FlowLayout());
        solutionList.setCellRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(
                    final JList list,
                    final Object value,
                    final int index,
                    final boolean isSelected,
                    final boolean cellHasFocus) {
                Color backgroundColor = list.getBackground();
                if (isSelected) {
                    backgroundColor = list.getSelectionBackground();
                }
                return new RendererPanel((int[][]) value, backgroundColor);
            }
        });
        solutionList.setVisibleRowCount(5);
        JScrollPane scrollPane = new JScrollPane(solutionList);
        scrollPane.setPreferredSize(new Dimension(312, 400));
        centerPanel.add(scrollPane);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        PentominoSolver solver = new PentominoSolver(gridWidth, gridHeight);
        solver.addSolutionListener(this);
        solver.solve();

        JPanel southPanel = new JPanel(new FlowLayout());
        solutionLabel.setText(listModel.getSize()  + " solutions found");
        southPanel.add(solutionLabel);
        contentPane.add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * Responds to events generated by the combo box used to select puzzle
     * size. Creates and runs a new pentomino solver.
     *
     * @param event  Object describing the event that caused this call.
     */
    public void actionPerformed(final ActionEvent event) {
        JComboBox comboBox = (JComboBox) event.getSource();
        int selectedIndex = comboBox.getSelectedIndex();

        listModel.removeAllElements();

        gridWidth = GRID_WIDTHS[selectedIndex];
        gridHeight = NUMBER_OF_SQUARES / gridWidth;
        PentominoSolver solver =
                new PentominoSolver(gridWidth, gridHeight);
        solver.addSolutionListener(this);
        solver.solve();
        solutionLabel.setText(listModel.getSize()  + " solutions found");
    }

    /**
     * Converts a solution from dancing links nodes into a completed pentominoes grid.
     *
     * @param  solutionNodes  The DLX nodes that make up the solution.
     * @return                <code>false</code> to tell the solver to continue generating
     *                        solutions.
     */
    public boolean solutionFound(final List solutionNodes) {
        int[][] solutionGrid = new int[gridHeight][gridWidth];

        Iterator iterator = solutionNodes.iterator();
        while (iterator.hasNext()) {
            Node node = (Node) iterator.next();

            // Split each move into its row, column, and piece.
            int move = node.applicationData;

            int pieceIdentifier = move / NUMBER_OF_SQUARES;
            int pieceIndex =
                    pieceIdentifier / Pentomino.ALL_PENTOMINOES.length;
            int orientation =
                    pieceIdentifier % Pentomino.ALL_PENTOMINOES.length;

            int location = move % NUMBER_OF_SQUARES;
            int row = location / gridWidth;
            int column = location % gridWidth;

            // Place the piece into its position in the pentominoes grid.
            Pentomino piece =
                    Pentomino.ALL_PENTOMINOES[pieceIndex][orientation];
            for (int j = 0; j < Pentomino.PENTOMINO_SIZE; j++) {
                solutionGrid[row    + piece.getYOffset(j)]
                            [column + piece.getXOffset(j)]
                    = pieceIndex;
            }
        }
        listModel.addElement(solutionGrid);

        return false;
    }

    /**
     * This is the main program to run the pentomino solver as an application.
     *
     * @param args  Command-line arguments (unused).
     */
    public static void main(final String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        PentominoApplet solver = new PentominoApplet();
        solver.init();
        contentPane.add(solver, BorderLayout.CENTER);

        frame.setSize(new Dimension(380, 512));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
