import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GraphicInterface extends JFrame {
    private Drawing canvas;
    private JButton wrapBtn, playBtn, resetBtn;
    private JTextArea logArea;
    private ButtonListener bl;

    // Modern Color Palette
    private final Color COL_BLUE = new Color(50, 151, 147);
    private final Color COL_GREEN = new Color(46, 204, 113);
    private final Color COL_RED = new Color(31, 104, 31);
    private final Color COL_BG = new Color(236, 240, 241);
    private final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 13);

    public GraphicInterface() {
        super("Graham Scan Visualization");
        this.setLayout(new BorderLayout());

        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(COL_BG);
        this.setContentPane(mainContainer);
        bl = new ButtonListener();

        // --- Side Panel ---
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBackground(Color.WHITE);
        sidePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        sidePanel.setPreferredSize(new Dimension(240, 600));

        JLabel title = new JLabel("Controls");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(title);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 20)));

        wrapBtn = createButton("Instant Hull", COL_BLUE);
        sidePanel.add(wrapBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        playBtn = createButton("Animate", COL_GREEN);
        sidePanel.add(playBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));

        resetBtn = createButton("Reset", COL_RED);
        sidePanel.add(resetBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- Logger ---
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(200, 300));
        sidePanel.add(new JLabel("Event Log:"));
        sidePanel.add(scroll);

        // --- Canvas ---
        canvas = new Drawing(logArea);
        JPanel canvasWrap = new JPanel(new GridBagLayout()); // Centers the canvas
        canvasWrap.setBackground(COL_BG);
        canvasWrap.add(canvas);

        this.add(sidePanel, BorderLayout.WEST);
        this.add(canvasWrap, BorderLayout.CENTER);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 700);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        log("Left click to add points.");
    }

    private JButton createButton(String txt, Color bg) {
        JButton b = new JButton(txt);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(200, 35));
        b.setFont(FONT_BTN);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(bl);
        return b;
    }

    private void log(String s) {
        logArea.append("> " + s + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void setButtonsEnabled(boolean val) {
        wrapBtn.setEnabled(val);
        playBtn.setEnabled(val);
        // Reset is handled separately usually
        wrapBtn.setBackground(val ? COL_BLUE : Color.GRAY);
        playBtn.setBackground(val ? COL_GREEN : Color.GRAY);
    }

    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == resetBtn) {
                canvas.reset();
                setButtonsEnabled(true);
                resetBtn.setBackground(COL_RED);
                return;
            }

            if (canvas.getPointsCount() < 3) {
                log("Add at least 3 points!");
                return;
            }

            if (e.getSource() == wrapBtn) {
                canvas.setState(1); // Instant Mode
                setButtonsEnabled(false);
                log("Convex Hull calculated.");
            } else
            if (e.getSource() == playBtn) {
                setButtonsEnabled(false); // Lock buttons
                resetBtn.setEnabled(false);
                resetBtn.setBackground(Color.GRAY);
                log("Running animation...");

                canvas.startAnimation(() -> {
                    // When animation finishes:
                    resetBtn.setEnabled(true);
                    resetBtn.setBackground(COL_RED);
                    log("Done! Press Reset to restart");
                });
            }
        }
    }

    public static void main(String[] args) {
        // Use standard Swing thread safety
        SwingUtilities.invokeLater(GraphicInterface::new);
    }
}