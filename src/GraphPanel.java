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

    public void saveGraphAsDot() {
        if (vertices.isEmpty()) return;

        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                writer.write("graph G {\n");

                // écrire tous les sommets (numérotés)
                for (int i = 0; i < vertices.size(); i++) {
                    writer.write("  " + i + ";\n");
                }

                // écrire toutes les arêtes
                for (Edge e : edges) {
                    writer.write("  " + e.a + " -- " + e.b + ";\n");
                }

                writer.write("}\n");
                JOptionPane.showMessageDialog(this, "Graphe sauvegardé au format DOT.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    public void clearGraph() {
        vertices.clear();
        colors.clear();
        edges.clear();
        selectingEdge = false;
        repaint();
    }
    public void promptBeforeReset() {
        int response = JOptionPane.showConfirmDialog(this,
                "Souhaitez-vous sauvegarder le graphe actuel avant de créer un nouveau ?",
                "Nouveau graphe",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (response == JOptionPane.CANCEL_OPTION) {
            return;
        }
        if (response == JOptionPane.YES_OPTION) {
            saveGraphAsDot(); // méthode déjà créée
        }
        clearGraph();
    }
    public void openGraphFromDot() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            java.util.List<String> lines = java.nio.file.Files.readAllLines(chooser.getSelectedFile().toPath());
            ArrayList<Point> newVertices = new ArrayList<>();
            ArrayList<Color> newColors = new ArrayList<>();
            ArrayList<Edge> newEdges = new ArrayList<>();

            int maxId = -1;
            for (String line : lines) {
                line = line.trim();
                if (line.matches("\\d+;")) {
                    int id = Integer.parseInt(line.replace(";", ""));
                    maxId = Math.max(maxId, id);
                }
            }

            // Générer les positions aléatoires des sommets
            for (int i = 0; i <= maxId; i++) {
                int x = rand.nextInt(getWidth() - 40) + 20;
                int y = rand.nextInt(getHeight() - 40) + 20;
                newVertices.add(new Point(x, y));
                newColors.add(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
            }

            // Lire les arêtes
            for (String line : lines) {
                line = line.trim();
                if (line.matches("\\d+ -- \\d+;")) {
                    String[] parts = line.replace(";", "").split(" -- ");
                    int a = Integer.parseInt(parts[0]);
                    int b = Integer.parseInt(parts[1]);
                    newEdges.add(new Edge(a, b));
                }
            }

            // Mise à jour du graphe
            this.vertices.clear();
            this.vertices.addAll(newVertices);
            this.colors.clear();
            this.colors.addAll(newColors);
            this.edges.clear();
            this.edges.addAll(newEdges);
            repaint();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ouverture du fichier : " + e.getMessage());
        }
    }


}
