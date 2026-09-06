# Class Reflection — September 1, 2026

## Topics Covered

- Drawing a triangle from three coordinates in JavaFX
- Drawing circles on a canvas using mouse input
- Connecting circles with arrows
- Checking whether a point is inside a circle
- Trees and tree terminology
- Binary trees and binary search trees

---

## Drawing a Triangle from Three Points

Today started with something that seemed simple but had a few interesting details underneath — drawing a triangle given three coordinates.

Since we already know all three vertices, there's no need to calculate line intersections or anything like that. You just connect the dots:

1. A to B
2. B to C
3. C back to A

In JavaFX, `strokePolygon` handles this in one call:

```java
double[] xCoordinates = {x1, x2, x3};
double[] yCoordinates = {y1, y2, y3};

graphicsContext.strokePolygon(xCoordinates, yCoordinates, 3);
```

Use `fillPolygon` instead if you want a solid triangle.

One thing worth checking before drawing: are the three points actually forming a triangle? If they're collinear (all sitting on the same line), there's no area and you'd just be drawing a line. The signed double area formula tells you:

```text
D = x₁(y₂ - y₃) + x₂(y₃ - y₁) + x₃(y₁ - y₂)
```

If `D` is zero, the points are collinear. Otherwise, the area of the triangle is `|D| / 2`. With floating-point coordinates, compare against a small tolerance instead of checking for exact zero.

---

## Drawing Circles on Right-Click

Next we looked at how to place circles on a canvas based on mouse input — specifically, drawing a circle wherever the user right-clicks.

The handler checks for `MouseButton.SECONDARY` (right-click) and uses the event's coordinates as the center. `fillOval` and `strokeOval` take the top-left corner of a bounding box rather than the center, so subtracting the radius from both coordinates gets the placement right:

```java
canvas.setOnMouseClicked(event -> {
    if (event.getButton() == MouseButton.SECONDARY) {
        double centerX = event.getX();
        double centerY = event.getY();

        graphicsContext.setFill(Color.LIGHTBLUE);
        graphicsContext.setStroke(Color.DARKBLUE);
        graphicsContext.fillOval(
            centerX - radius, centerY - radius,
            radius * 2, radius * 2
        );
        graphicsContext.strokeOval(
            centerX - radius, centerY - radius,
            radius * 2, radius * 2
        );
    }
});
```

Equal width and height give a circle; different values would give an ellipse.

### Storing the circles

Just drawing pixels isn't enough if the circles need to be selected or connected later. Storing each circle as a data object keeps the model and the rendering separate:

```java
record CircleNode(double centerX, double centerY, double radius) {}
List<CircleNode> circles = new ArrayList<>();
```

Every right-click adds a new `CircleNode` to the list. This makes it easy to redraw everything, run hit tests, and build connections on top.

---

## Connecting Circles with Arrows

Once you have circles stored as data, drawing arrows between them is the next step. An arrow has a shaft (a line) and an arrowhead (two short angled lines at the tip).

The tricky part is making the arrow start and end at the circle's edge rather than the center. This requires a unit direction vector between the two centers:

```text
dx = x₂ - x₁,  dy = y₂ - y₁
length = √(dx² + dy²)
ux = dx / length,  uy = dy / length
```

The actual endpoints shift outward from each center by its radius:

```text
startX = x₁ + ux × r₁,  startY = y₁ + uy × r₁
endX   = x₂ - ux × r₂,  endY   = y₂ - uy × r₂
```

The arrowhead is drawn using `Math.atan2` to get the shaft angle, then offsetting by a fixed angle on each side:

```java
private void drawArrow(GraphicsContext gc,
        double startX, double startY,
        double endX, double endY) {

    double arrowLength = 12.0;
    double arrowAngle = Math.toRadians(25.0);
    double angle = Math.atan2(endY - startY, endX - startX);

    gc.strokeLine(startX, startY, endX, endY);

    double firstX  = endX - arrowLength * Math.cos(angle - arrowAngle);
    double firstY  = endY - arrowLength * Math.sin(angle - arrowAngle);
    double secondX = endX - arrowLength * Math.cos(angle + arrowAngle);
    double secondY = endY - arrowLength * Math.sin(angle + arrowAngle);

    gc.strokeLine(endX, endY, firstX, firstY);
    gc.strokeLine(endX, endY, secondX, secondY);
}
```

### Redrawing order

JavaFX's `Canvas` is immediate-mode — shapes are stored as pixels, not as objects the scene graph can manage. So whenever the model changes, the right approach is:

1. Clear the canvas.
2. Draw all arrows first.
3. Draw all circles on top.

Drawing circles last hides any slight overlap at the boundaries.

---

## Checking Whether a Point Is Inside a Circle

To detect which circle the user clicked, we need a hit test. The math is straightforward: a point `(px, py)` is inside a circle with center `(cx, cy)` and radius `r` when:

```text
(px - cx)² + (py - cy)² ≤ r²
```

No square root needed — comparing squared distances gives the same result and is faster:

```java
private boolean containsPoint(double centerX, double centerY,
        double radius, double pointX, double pointY) {

    double dx = pointX - centerX;
    double dy = pointY - centerY;
    return dx * dx + dy * dy <= radius * radius;
}
```

If circles overlap, iterating through the list in reverse drawing order ensures the topmost circle gets selected first.

---

## Trees: Hierarchical Data Structures

The second half of class shifted to data structures — specifically trees. A tree is a hierarchical structure of nodes connected by edges, with one root at the top.

Key properties:

- The root has no parent; every other node has exactly one parent.
- Nodes can have zero or more children.
- There are no cycles.
- A tree with `n` nodes has exactly `n - 1` edges.

Some terminology that came up:

| Term | Meaning |
|---|---|
| Root | The top-level node with no parent |
| Parent / Child | Direct ancestor / descendant relationship |
| Sibling | Nodes that share the same parent |
| Leaf | A node with no children |
| Internal node | A node with at least one child |
| Depth | Edges from the root to a node |
| Height | Longest downward path from a node to a leaf |
| Subtree | A node and all of its descendants |

Trees come up everywhere — file systems, UI component hierarchies, expression parsing, search indexes.

---

## Binary Trees and Binary Search Trees

A binary tree is a tree where each node has **at most two children** — a left child and a right child. On its own, that constraint doesn't imply any particular ordering.

A **binary search tree (BST)** adds a rule: for any node, everything in its left subtree is smaller, and everything in its right subtree is larger. That ordering is what makes searching efficient.

Search, insertion, and deletion all run in `O(h)` where `h` is the height:

- In a balanced BST, height is around `log n`, giving `O(log n)` operations.
- In a worst-case unbalanced BST (essentially a linked list), height reaches `n`, giving `O(n)`.

Inorder traversal of a BST (left → root → right) visits every value in sorted order, which is a nice property.

Binary trees also show up in other forms that follow different rules — heaps, expression trees, Huffman trees, decision trees — none of which are BSTs but all use the same two-child structure.

---

## Summary

Quite a lot covered today between graphics and data structures:

- Three known vertices connect directly into a triangle — check collinearity first with the signed area formula.
- `strokePolygon` and `fillPolygon` handle triangles cleanly in JavaFX.
- Right-click events are detected via `MouseButton.SECONDARY`; `fillOval` takes the bounding box top-left, not the center.
- Store circle data in a model — don't rely on pixels alone if you need to interact with the shapes later.
- Arrows between circles use a unit direction vector to land at the circle edges, not the centers.
- Hit testing with squared distance avoids `Math.sqrt` and works just as well.
- Trees are acyclic, hierarchical structures; every node except the root has exactly one parent.
- Binary search trees add ordering to the binary tree structure, giving `O(log n)` operations when balanced.
