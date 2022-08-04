import dsa.LinkedQueue;
import dsa.MaxPQ;
import dsa.Point2D;
import dsa.RectHV;
import stdlib.StdIn;
import stdlib.StdOut;

public class KdTreePointST<Value> implements PointST<Value> {
    private Node root; // Reference to the root of a 2dTree
    private int n; // Number of nodes in the tree

    // Constructs an empty symbol table.
    public KdTreePointST() {
        root = null;
        n = 0;
    }

    // Returns true if this symbol table is empty, and false otherwise.
    public boolean isEmpty() {
        return n == 0;
    }

    // Returns the number of key-value pairs in this symbol table.
    public int size() {
        return n;
    }

    // Inserts the given point and value into this symbol table.
    public void put(Point2D p, Value value) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        if (value == null) {
            throw new NullPointerException("value is null");
        }
        // Generate a new Rectangle
        RectHV a = new RectHV(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        root = put(root, p, value, a, true);

    }

    // Returns the value associated with the given point in this symbol table, or null.
    public Value get(Point2D p) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        return get(root, p, true);
    }

    // Returns true if this symbol table contains the given point, and false otherwise.
    public boolean contains(Point2D p) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        if (get(p) != null) {
            return true;
        }
        return false;
    }

    // Returns all the points in this symbol table.
    public Iterable<Point2D> points() {
        LinkedQueue<Point2D> pqueue = new LinkedQueue<>();
        if (isEmpty()) {
            return pqueue;
        }

        LinkedQueue<Node> queue = new LinkedQueue<>();

        queue.enqueue(root);
        while (!queue.isEmpty()) {
            Node temp = queue.dequeue();
            pqueue.enqueue(temp.p);
            if (temp.lb != null) {
                queue.enqueue(temp.lb);
            }
            if (temp.rt != null) {
                queue.enqueue(temp.rt);
            }
        }
        return pqueue;
    }

    // Returns all the points in this symbol table that are inside the given rectangle.
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) {
            throw new NullPointerException("rect is null");
        }
        LinkedQueue<Point2D> arect = new LinkedQueue<>();
        range(root, rect, arect);
        return arect;
    }

    // Returns the point in this symbol table that is different from and closest to the given point,
    // or null.
    public Point2D nearest(Point2D p) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        Point2D temp = new Point2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        return nearest(root, p, temp, true);
    }

    // Returns up to k points from this symbol table that are different from and closest to the
    // given point.
    public Iterable<Point2D> nearest(Point2D p, int k) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        MaxPQ<Point2D> nearestpoints = new MaxPQ<>(p.distanceToOrder());
        nearest(root, p, k, nearestpoints, true);
        return nearestpoints;
    }

    // Note: In the helper methods that have lr as a parameter, its value specifies how to
    // compare the point p with the point x.p. If true, the points are compared by their
    // x-coordinates; otherwise, the points are compared by their y-coordinates. If the
    // comparison of the coordinates (x or y) is true, the recursive call is made on x.lb;
    // otherwise, the call is made on x.rt.

    // Inserts the given point and value into the KdTree x having rect as its axis-aligned
    // rectangle, and returns a reference to the modified tree.
    private Node put(Node x, Point2D p, Value value, RectHV rect, boolean lr) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        if (x == null) {
            n++;
            return new Node(p, value, rect);
        }
        if (x.p == p) {
            x.value = value;
        } else if (lr && p.x() < x.p.x() || !lr && p.y() < x.p.y()) {
            if (lr) {
                RectHV nrect = new RectHV(rect.xMin(), rect.yMin(), x.p.x(), rect.yMax());
                x.lb = put(x.lb, p, value, nrect, !lr);
            } else {
                RectHV nnrect = new RectHV(rect.xMin(), rect.yMin(), rect.xMax(), x.p.y());
                x.lb = put(x.lb, p, value, nnrect, !lr);
            }
        } else {
            if (lr) {
                RectHV newRect = new RectHV(x.p.x(), rect.yMin(), rect.xMax(), rect.yMax());
                x.rt = put(x.rt, p, value, newRect, !lr);
            } else {
                RectHV newRect2 = new RectHV(rect.xMin(), x.p.y(), rect.xMax(), rect.yMax());
                x.rt = put(x.rt, p, value, newRect2, !lr);
            }
        }
        return x;
    }


    // Returns the value associated with the given point in the KdTree x, or null.
    private Value get(Node x, Point2D p, boolean lr) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        if (x == null) {
            return null;
        }
        if (x.p.equals(p)) {
            return x.value;
        }
        if (lr) {
            if (p.x() < x.p.x()) {
                get(x.lb, p, !lr);
            } else {
                get(x.rt, p, !lr);
            }
        } else {
            if (p.y() < x.p.y()) {
                get(x.lb, p, !lr);
            } else {
                get(x.rt, p, !lr);
            }
        }
        return null;
    }

    // Collects in the given queue all the points in the KdTree x that are inside rect.
    private void range(Node x, RectHV rect, LinkedQueue<Point2D> q) {
        if (x == null) {
            return;
        }
        if (!x.rect.intersects(rect)) {
            return;
        }
        if (rect.contains(x.p)) {
            q.enqueue(x.p);
        }
        range(x.lb, rect, q);
        range(x.rt, rect, q);
    }


    // Returns the point in the KdTree x that is closest to p, or null; nearest is the closest
    // point discovered so far.
    private Point2D nearest(Node x, Point2D p, Point2D nearest, boolean lr) {
        if (p == null) {
            throw new NullPointerException("p is null");
        }
        Point2D temp = nearest; // representation of nearest to avoid changing parameter values
        // if tree does not exist, return known nearest pt
        if (x == null) {
            return temp;
        }
        // if a closer pt is found, replace nearest with that pt
        if (p.distanceSquaredTo(x.p) < p.distanceSquaredTo(nearest) && !x.p.equals(p)) {
            temp = x.p;
        }

        boolean sent; // sentinel value to determine whether to go right/left
        // compare x or y coord based on level given by lr
        if (lr) {
            if (p.x() > x.p.x()) {
                temp = nearest(x.rt, p, temp, !lr);
                sent = false;
            } else { // otherwise go left
                temp = nearest(x.lb, p, temp, !lr);
                sent = true;
            }
        } else {
            if (p.y() > x.p.y()) {
                temp = nearest(x.rt, p, temp, !lr);
                sent = false;
            } else { // otherwise go down
                temp = nearest(x.lb, p, temp, !lr);
                sent = true;
            }
        }
        if (sent) {
            return nearest(x.rt, p, temp, lr);
        }
        return nearest(x.lb, p, temp, lr);
    }

    // Collects in the given max-PQ up to k points from the KdTree x that are different from and
    // closest to p.
    private void nearest(Node x, Point2D p, int k, MaxPQ<Point2D> pq, boolean lr) {
        if (x == null || pq.size() >= k && pq.max().distanceSquaredTo(p)
                < x.rect.distanceSquaredTo(p)) {
            return;
        }
        if (!x.p.equals(p)) {
            pq.insert(x.p);
        }
        if (pq.size() > k) {
            pq.delMax();
        }
        boolean leftBottom;
        if (lr && p.x() < x.p.x() || !lr && p.y() < x.p.y()) {
            nearest(x.lb, p, k, pq, !lr);
            leftBottom = true;
        } else {
            nearest(x.rt, p, k, pq, !lr);
            leftBottom = false;
        }
        nearest(leftBottom ? x.rt : x.lb, p, k, pq, !lr);

    }

    // A representation of node in a KdTree in two dimensions (ie, a 2dTree). Each node stores a
    // 2d point (the key), a value, an axis-aligned rectangle, and references to the left/bottom
    // and right/top subtrees.
    private class Node {
        private final Point2D p;   // the point (key)
        private Value value; // the value
        private final RectHV rect; // the axis-aligned rectangle
        private Node lb;     // the left/bottom subtree
        private Node rt;     // the right/top subtree

        // Constructs a node given the point (key), the associated value, and the
        // corresponding axis-aligned rectangle.
        Node(Point2D p, Value value, RectHV rect) {
            this.p = p;
            this.value = value;
            this.rect = rect;
        }
    }

    // Unit tests the data type. [DO NOT EDIT]
    public static void main(String[] args) {
        KdTreePointST<Integer> st = new KdTreePointST<>();
        double qx = Double.parseDouble(args[0]);
        double qy = Double.parseDouble(args[1]);
        int k = Integer.parseInt(args[2]);
        Point2D query = new Point2D(qx, qy);
        RectHV rect = new RectHV(-1, -1, 1, 1);
        int i = 0;
        while (!StdIn.isEmpty()) {
            double x = StdIn.readDouble();
            double y = StdIn.readDouble();
            Point2D p = new Point2D(x, y);
            st.put(p, i++);
        }
        StdOut.println("st.empty()? " + st.isEmpty());
        StdOut.println("st.size() = " + st.size());
        StdOut.printf("st.contains(%s)? %s\n", query, st.contains(query));
        StdOut.printf("st.range(%s):\n", rect);
        for (Point2D p : st.range(rect)) {
            StdOut.println("  " + p);
        }
        StdOut.printf("st.nearest(%s) = %s\n", query, st.nearest(query));
        StdOut.printf("st.nearest(%s, %d):\n", query, k);
        for (Point2D p : st.nearest(query, k)) {
            StdOut.println("  " + p);
        }
    }
}
