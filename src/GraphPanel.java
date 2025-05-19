import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GraphPanel extends JPanel {
    enum Mode { VERTEX, EDGE }
    private Mode currentMode = Mode.VERTEX;

    private final ArrayList<Point> vertices = new ArrayList<>();
    private final ArrayList<Color> colors = new ArrayList<>();
    private final ArrayList<Edge> edges = new ArrayList<>();

    private final int[] edgeInProgress = new int[2];
    private boolean selectingEdge = false;
    private final Random rand = new Random();

    private int draggedVertexIndex = -1;

    public GraphPanel() {
        setBackground(Color.WHITE);

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (currentMode == Mode.VERTEX) {
                    int index = getVertexAt(e.getPoint());
                    if (index != -1) {
                        draggedVertexIndex = index;
                    } else {
                        vertices.add(e.getPoint());
                        colors.add(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
                        repaint();
                    }
                } else if (currentMode == Mode.EDGE) {
                    // D'abord, vérifier si une arête est cliquée
                    int edgeIndex = getEdgeAt(e.getPoint());
                    if (edgeIndex != -1) {
                        edges.remove(edgeIndex);
                        repaint();
                        return;
                    }

                    // Sinon, gérer création d’arête
                    int clicked = getVertexAt(e.getPoint());
                    if (clicked != -1) {
                        if (!selectingEdge) {
                            edgeInProgress[0] = clicked;
                            selectingEdge = true;
                        } else {
                            edgeInProgress[1] = clicked;
                            edges.add(new Edge(edgeInProgress[0], edgeInProgress[1]));
                            selectingEdge = false;
                            repaint();
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggedVertexIndex = -1;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentMode == Mode.VERTEX && draggedVertexIndex != -1) {
                    vertices.set(draggedVertexIndex, e.getPoint());
                    repaint();
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    public void setMode(Mode mode) {
        currentMode = mode;
    }

    private int getVertexAt(Point p) {
        for (int i = 0; i < vertices.size(); i++) {
            if (p.distance(vertices.get(i)) < 15) return i;
        }
        return -1;
    }

    private int getEdgeAt(Point p) {
        for (int i = 0; i < edges.size(); i++) {
            Point p1 = vertices.get(edges.get(i).a);
            Point p2 = vertices.get(edges.get(i).b);
            Point2D.Double a = new Point2D.Double(p1.x, p1.y);
            Point2D.Double b = new Point2D.Double(p2.x, p2.y);
            double dist = a.distance(b) == 0 ? p.distance(a) :
                    Line2D.ptSegDist(a.x, a.y, b.x, b.y, p.x, p.y);
            if (dist < 5.0) return i;
        }
        return -1;
    }

    public void savePositions() {
        if (vertices.isEmpty()) return;
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                for (Point p : vertices) {
                    writer.write(p.x + " " + p.y + "\n");
                }
                JOptionPane.showMessageDialog(this, "Positions saved.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw edges
        g.setColor(Color.GRAY);
        for (Edge e : edges) {
            Point p1 = vertices.get(e.a);
            Point p2 = vertices.get(e.b);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }

        // Draw vertices
        for (int i = 0; i < vertices.size(); i++) {
            g.setColor(colors.get(i));
            Point p = vertices.get(i);
            g.fillOval(p.x - 10, p.y - 10, 20, 20);
        }
    }

    private static class Edge {
        int a, b;
        Edge(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }
}
