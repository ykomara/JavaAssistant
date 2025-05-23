import javax.swing.*;

public class GraphMenuBar extends JMenuBar {
    public GraphMenuBar(GraphPanel panel) {
        JMenu fileMenu = new JMenu("File");
        JMenuItem savePositions = new JMenuItem("Save Positions");
        JMenuItem saveGraph = new JMenuItem("Save Graph");
        JMenuItem newGraph = new JMenuItem("New");
        newGraph.addActionListener(e -> panel.promptBeforeReset());
        fileMenu.add(newGraph);
        saveGraph.addActionListener(e -> panel.saveGraphAsDot());
        fileMenu.add(saveGraph);
        savePositions.addActionListener(e -> panel.savePositions());
        fileMenu.add(savePositions);

        JMenu insertMenu = new JMenu("Insert");
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem vertexMode = new JRadioButtonMenuItem("Vertex", true);
        JRadioButtonMenuItem edgeMode = new JRadioButtonMenuItem("Edge");

        vertexMode.addActionListener(e -> panel.setMode(GraphPanel.Mode.VERTEX));
        edgeMode.addActionListener(e -> panel.setMode(GraphPanel.Mode.EDGE));

        group.add(vertexMode);
        group.add(edgeMode);

        insertMenu.add(vertexMode);
        insertMenu.add(edgeMode);

        add(fileMenu);
        add(insertMenu);
    }
}
