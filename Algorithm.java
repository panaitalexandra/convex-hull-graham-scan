import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Algorithm {
    private ArrayList<Point> points;
    private Object[] objects;
    private Point m; // Centroid
    private int mTx, mTy; // Translation offsets

    public Algorithm(ArrayList<Point> p) {
        points = new ArrayList<>(p);
    }

    private void fixM() {
        if (points.isEmpty()) return;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Object obj : points) {
            Point p = (Point) obj;
            if (p.getX() < minX) minX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() > maxY) maxY = p.getY();
        }

        // Calculate centroid
        m = new Point((minX + maxX) / 2, (minY + maxY) / 2);
    }

    public void translate() {
        this.fixM();
        System.out.println("Centroid M: " + m.getX() + " " + m.getY());

        ArrayList<Point> translatedPoints = new ArrayList<>();
        Iterator<Point> it = points.iterator();

        // The visual center is 250, 250. 
        // We shift points so M becomes (0,0) logically for sorting.
        mTx = 250 - m.getX();
        mTy = 250 - m.getY();

        while (it.hasNext()) {
            Point pp = it.next();
            Point p = new Point(pp.getX(), pp.getY());
            // Translate relative to visual center to make math easier
            // Note: In strict math, we usually just substract M. 
            // Here we respect the original code's offset logic.
            p.translate(mTx - 250, mTy - 250);
            translatedPoints.add(p);
        }
        points = translatedPoints;
        m.setX(0);
        m.setY(0);
    }

    public void sort() {
        this.translate();
        objects = points.toArray();
        Arrays.sort(objects);
        System.out.println("After sorting:");
        for (int i = 0; i < objects.length; i++) {
            System.out.println(((Point) objects[i]).toString());
        }
    }

    public void wrap() {
        sort();

        // Convert array back to ArrayList for easy removal
        ArrayList<Point> hull = new ArrayList<>();
        for(Object o : objects) hull.add((Point)o);

        // Graham Scan Logic
        if (hull.size() < 3) return;

        int i = 0;
        while (i < hull.size()) {
            Point p1 = hull.get(i);
            Point p2 = hull.get((i + 1) % hull.size());
            Point p3 = hull.get((i + 2) % hull.size());

            // Check for right turn or collinear (non-convex)
            double det = calcDet(p1.getX(), p1.getY(),
                    p2.getX(), p2.getY(),
                    p3.getX(), p3.getY());

            if (det <= 0) {
                // Not a left turn -> remove the middle point (p2)
                hull.remove((i + 1) % hull.size());

                // Step back to re-verify previous point
                if (i > 0) i--;
            } else
                // Valid left turn, move forward
                i++;

            // Safety break for loop wrapping logic (simplified)
            if (i > hull.size()) break;
        }

        objects = hull.toArray();
    }

    public Object[] getBoundary() {
        // Translate back to original coordinates for drawing
        for (int i = 0; i < objects.length; i++) {
            ((Point) objects[i]).translate(-(mTx - 250), -(mTy - 250));
        }
        return objects;
    }

    // Cross product of 3 points (vectors p1->p2 and p1->p3)
    public double calcDet(double x1, double y1, double x2, double y2, double x3, double y3) {
        return (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);
    }
}