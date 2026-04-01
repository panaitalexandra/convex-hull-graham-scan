import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;

public class Drawing extends JPanel {
    private ArrayList<Point> points;
    private JTextArea logger;
    private Object[] hullObjects; // The result from your algorithm

    // 0 = Input, 1 = Finished (Instant), 2 = Animation
    private int state;

    // Animation variables
    private Timer animTimer;
    private int currentSegmentIndex;
    private float drawProgress;
    private Runnable onAnimationFinish;

    // Colors and Styles
    private final Color GRID_COLOR = new Color(224, 224, 224);
    private final Color AXIS_COLOR = new Color(100, 100, 100);
    private final Color POINT_COLOR = new Color(31, 104, 31);
    private final Color HULL_COLOR = new Color(50, 151, 147); // Purple/Magenta
    private final Stroke HULL_STROKE = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    public Drawing(JTextArea logger) {
        this.logger = logger;
        points = new ArrayList<>();
        state = 0;

        this.setPreferredSize(new Dimension(501, 501));
        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        // Use MouseAdapter for cleaner code
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                processMouseClick(e);
            }
        });
    }

    // --- Animation Logic ---
    public void startAnimation(Runnable onFinish) {
        if (points.size() < 3) return;
        this.onAnimationFinish = onFinish;

        // Run your algorithm
        Algorithm alg = new Algorithm(points);
        alg.wrap();
        hullObjects = alg.getBoundary();

        if(hullObjects.length < 3) {
            JOptionPane.showMessageDialog(this, "Not enough points for a hull!");
            return;
        }

        // Setup Animation
        currentSegmentIndex = 0;
        drawProgress = 0.0f;
        state = 2; // Animation state

        if (animTimer != null && animTimer.isRunning()) animTimer.stop();

        // Timer updates every 16ms (~60fps)
        animTimer = new Timer(16, e -> updateAnimationFrame());
        animTimer.start();
    }

    private void updateAnimationFrame() {
        drawProgress += 0.05f; // Speed of drawing line

        if (drawProgress >= 1.0f) {
            drawProgress = 0.0f;
            currentSegmentIndex++;

            // If we have drawn all segments
            if (currentSegmentIndex >= hullObjects.length) {
                animTimer.stop();
                state = 1; // Finished state
                if (onAnimationFinish != null) onAnimationFinish.run();
            }
        }
        repaint();
    }

    // --- Paint Logic ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Make lines smooth (Antialiasing)
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawAxes(g2);
        drawPoints(g2);

        // Draw Convex Hull based on state
        if (points.size() >= 3) {
            if (state == 1) {
                // Instant / Finished result
                if (hullObjects == null) calculateHullInstant();
                drawFullHull(g2);
            } else if (state == 2) {
                // Animating
                drawAnimatedHull(g2);
            }
        }
    }

    private void calculateHullInstant() {
        Algorithm alg = new Algorithm(points);
        alg.wrap();
        hullObjects = alg.getBoundary();
    }

    private void drawFullHull(Graphics2D g2) {
        if (hullObjects == null || hullObjects.length < 2) return;
        g2.setColor(HULL_COLOR);
        g2.setStroke(HULL_STROKE);

        for (int i = 0; i < hullObjects.length; i++) {
            Point p1 = (Point) hullObjects[i];
            Point p2 = (Point) hullObjects[(i + 1) % hullObjects.length];
            drawLineRescaled(g2, p1, p2);
            drawPointLabel(g2, p1, i);
        }
    }

    private void drawAnimatedHull(Graphics2D g2) {
        if (hullObjects == null) return;
        g2.setColor(HULL_COLOR);
        g2.setStroke(HULL_STROKE);

        // Draw fully completed lines
        for (int i = 0; i < currentSegmentIndex; i++) {
            Point p1 = (Point) hullObjects[i];
            Point p2 = (Point) hullObjects[(i + 1) % hullObjects.length];
            drawLineRescaled(g2, p1, p2);
            drawPointLabel(g2, p1, i);
        }

        // Draw the currently moving line
        if (currentSegmentIndex < hullObjects.length) {
            Point pStart = (Point) hullObjects[currentSegmentIndex];
            Point pEnd = (Point) hullObjects[(currentSegmentIndex + 1) % hullObjects.length];

            drawPointLabel(g2, pStart, currentSegmentIndex);

            // Calculate intermediate pixel position
            double x1 = calcX(pStart.getX());
            double y1 = calcY(pStart.getY());
            double x2 = calcX(pEnd.getX());
            double y2 = calcY(pEnd.getY());

            double curX = x1 + (x2 - x1) * drawProgress;
            double curY = y1 + (y2 - y1) * drawProgress;

            g2.drawLine((int)x1, (int)y1, (int)curX, (int)curY);

            // Draw a small "pen" head
            g2.fillOval((int)curX-3, (int)curY-3, 6, 6);
        }
    }

    // --- Helpers ---
    private void drawLineRescaled(Graphics2D g2, Point p1, Point p2) {
        g2.drawLine(calcX(p1.getX()), calcY(p1.getY()), calcX(p2.getX()), calcY(p2.getY()));
    }

    private void drawPointLabel(Graphics2D g2, Point p, int index) {
        Font originalFont = g2.getFont();
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        g2.drawString(String.valueOf(index), calcX(p.getX()) + 5, calcY(p.getY()) - 5);
        g2.setFont(originalFont);
        g2.setColor(HULL_COLOR);
    }

    private void drawPoints(Graphics2D g2) {
        for (Point p : points) {
            int px = calcX(p.getX());
            int py = calcY(p.getY());
            g2.setColor(new Color(0,0,0, 50)); // Shadow
            g2.fillOval(px - 1, py - 1, 10, 10);
            g2.setColor(POINT_COLOR);
            g2.fillOval(px - 4, py - 4, 8, 8);
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(1));
        for(int i = -25; i <= 25; i++) {
            g2.drawLine(calcX(i * 10), 0, calcX(i * 10), 500); // Scaled for visual grid
            g2.drawLine(0, calcY(i * 10), 500, calcY(i * 10));
        }
    }

    private void drawAxes(Graphics2D g2) {
        g2.setColor(AXIS_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(0, 250, 500, 250); // X Axis
        g2.drawString("X", 480, 240);
        g2.drawLine(250, 0, 250, 500); // Y Axis
        g2.drawString("Y", 260, 15);
    }

    // Coordinate converters
    public int calcX(double x) { return (int) (250 + x); } // Simplified scaling for raw pixels
    public int calcY(double y) { return (int) (250 - y); }

    public int getPointsCount() { return points.size(); }

    public void setState(int type) {
        this.state = type;
        if(type == 0) {
            hullObjects = null;
            if (animTimer != null) animTimer.stop();
        }
        repaint();
    }

    public void processMouseClick(MouseEvent e) {
        if (state == 0) {
            // Convert screen to logical coordinates (relative to center 250,250)
            int mx = e.getX() - 250;
            int my = 250 - e.getY();

            points.add(new Point(mx, my));

            if(logger != null) logger.append("Point added: [" + mx + ", " + my + "]\n");
            repaint();
        } else {
            if(logger != null) logger.append("Reset to add more points.\n");
        }
    }

    public void reset() {
        points.clear();
        state = 0;
        hullObjects = null;
        if(animTimer != null) animTimer.stop();
        repaint();
        if(logger != null) logger.append("\n---- Canvas Reset ----\n");
    }
}