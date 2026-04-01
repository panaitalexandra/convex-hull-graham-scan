# convex-hull-graham-scan
# Convex Hull Determination (Graham Scan with Circular Linked List)

## 1. Requirement

Given a set of **N** distinct points in a 2D plane, the objective is to find the **Convex Hull**—the smallest convex polygon that contains all the points within its interior or on its boundary.

The solution implements the **Graham Scan algorithm** using a **circular doubly linked list** to efficiently remove concavities and maintain the convex property.



---

## 2. Algorithm Description: Graham's Logic

The algorithm processes a sorted set of points and identifies the hull by eliminating "concave" turns (right turns or collinear points) directly from the linked list by re-linking the nodes.

### Phase I: Initialization
The algorithm is initialized with `GRAHAM(S, vm)`, where:
* **S**: The set of points, sorted by their polar angle relative to the starting point.
* **$v_m$**: The vertex with the **minimum Y-coordinate** (the lowest point), serving as the starting reference.

### Phase II: Hull Construction
Starting from $v = v_m$, the algorithm iterates through the circular list as long as the successor of the current vertex is not the starting point ($B(v) \neq v_m$):

1.  **Orientation Check:** We calculate the determinant for the triplet formed by the current vertex, its successor, and its successor's successor:
    $$\Delta(v, B(v), B(B(v)))$$

2.  **Convex Case (Left Turn):** If $\Delta > 0$, the points form a convex angle.
    * **Action:** Move forward to the next vertex: $v \leftarrow B(v)$.

3.  **Concave Case (Right Turn/Collinear):** If $\Delta \le 0$, the intermediate point $B(v)$ creates a concavity and must be removed.
    * **Deletion:** $B(v)$ is removed from the list. The links are updated so the successor of $v$ becomes the former $B(B(v))$.
    * **Backtracking:** To ensure the removal didn't create a new concavity with previous points, the algorithm steps back to the predecessor:
        * If $v \neq v_m$, then $v \leftarrow A(v)$.

### Final Result
Upon completion, the circular list **S** contains only the vertices that form the **Convex Hull** in counter-clockwise order.

---

## 3. Complexity
* **Sorting:** $O(n \log n)$ – required for the initial polar or coordinate sorting.
* **Scan Phase:** $O(n)$ – although the algorithm backtracks, each point is added to the list once and removed at most once.
* **Total Time Complexity:** **$O(n \log n)$**.
* **Space Complexity:** **$O(n)$** for the circular linked list structure.
