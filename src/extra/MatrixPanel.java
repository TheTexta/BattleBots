package extra;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MatrixPanel extends JPanel {
    private int[][] matrix;
    private JFrame frame;

    public MatrixPanel(int[][] matrix) {
        this.matrix = matrix;
        setPreferredSize(new Dimension(matrix.length, matrix[0].length));
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int matrixLength = matrix.length;
        int matrixRowLength = matrix[0].length;
        int componentWidth = getWidth();
        int componentHeight = getHeight();
        int cellWidth = componentWidth / matrixLength;
        int cellHeight = componentHeight / matrixRowLength;
    
        // Precompute color lookup table
        Color[][] colorTable = new Color[matrixLength][matrixRowLength];
        for (int row = 0; row < matrixLength; row++) {
            for (int col = 0; col < matrixRowLength; col++) {
                colorTable[row][col] = getColorForValue(matrix[row][col]);
            }
        }
    
        for (int row = 0; row < matrixLength; row++) {
            for (int col = 0; col < matrixRowLength; col++) {
                Color color = colorTable[row][col];
                g.setColor(color);
                g.fillRect(row * cellWidth, col * cellHeight, cellWidth, cellHeight);
            }
        }
    }
    
    
    private Color getColorForValue(int value) {
        // Define your own logic for mapping matrix values to colors
        // Here's a simple example that maps positive values to blue and negative values
        // to red
        if (value == 2) {
            return Color.BLUE;
        } else if (value == 1) {
            return Color.GRAY;
        } else if (value >= 3) {
            return Color.RED;
        } else {
            return Color.BLACK;
        }
    }

    public void createAndShowGUI() {
        frame = new JFrame("Matrix Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
    }

}
