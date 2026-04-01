public class Point implements Comparable {
    private int x, y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public void translate(int xm, int ym) {
        x = x + xm;
        y = y + ym;
    }

    // Logic updated to handle quadrants relative to origin (after translation)
    // or relative to visual center (250,250) if not translated.
    // Assuming standard Cartesian quadrants.
    public int getQuadrant() {
        if ((x >= 0) && (y >= 0)) return 1;
        else if ((x < 0) && (y >= 0)) return 2;
        else if ((x < 0) && (y < 0)) return 3;
        else return 4;
    }

    public int compareTo(Object o) {
        Point other = (Point) o;

        // Compare by quadrant first
        if (this.getQuadrant() != other.getQuadrant()) {
            return this.getQuadrant() - other.getQuadrant();
        }

        // If in same quadrant, check determinant (cross product)
        // This determines which point has a smaller angle
        int det = getDet(this.x, this.y, other.getX(), other.getY());

        // If det < 0, this point is "to the right" (clockwise) of other
        return -det;
    }

    // Standard cross product relative to origin (0,0)
    public int getDet(int x1, int y1, int x2, int y2) {
        return x1 * y2 - x2 * y1;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}