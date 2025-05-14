import javax.swing.*;

public class GraphDrawingApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graph Drawing");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            GraphPanel panel = new GraphPanel();
            frame.setJMenuBar(new GraphMenuBar(panel));
            frame.add(panel);

            frame.setVisible(true);
        });
    }
}
