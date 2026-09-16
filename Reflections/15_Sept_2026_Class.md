# Class Reflection — September 15, 2026

## Topics Covered

- Group 1 Project Implementation: Computational geometry, linear systems, and GUI design
- Verifying triangle formation and vertex orientation via signed 2D area
- Intersection of two lines in standard form: Determinants, Cramer's rule, and edge cases
- Analyzing overdetermined systems ($Ax = b$) of three lines in 2D space
- JavaFX interface architecture: `GridPane` layout, defensive input parsing, and state handling
- Decoupled software architecture: Separating UI controllers from pure mathematical domain services

---

## Overview: Group 1 Project Presentation

In today's class, Group 1 demonstrated their software project, which bridged analytical geometry, linear algebra, and graphical user interface engineering in Java. 

Their application tackled four cohesive milestones:
1. **Triangle Verification:** Evaluating whether three arbitrary coordinate points form a valid, non-degenerate triangle.
2. **Line Intersection:** Calculating the intersection point of two lines or classifying them as parallel or coincident.
3. **Overdetermined Linear Systems:** Formulating three line equations into matrix form ($Ax = b$) and determining whether they intersect concurrently or enclose a triangle.
4. **Interactive JavaFX Interface:** Building an intuitive, error-resilient GUI to collect coefficients, validate inputs, and report mathematical findings.

The presentation provided a strong practical demonstration of how fundamental mathematical concepts map directly into clean, testable object-oriented software.

---

## Milestone 1: Verifying Triangle Validity from Three Points

Given three arbitrary points in a 2D Cartesian plane:

$$P_1 = (x_1, y_1), \quad P_2 = (x_2, y_2), \quad P_3 = (x_3, y_3)$$

The goal is to determine if they form a valid triangle.

```text
       P2 (x2, y2)                                P2 (x2, y2)
         / \                                          /
        /   \      Valid Triangle                    /      Collinear (Degenerate)
       /     \     (Area > 0)                       /       (Area = 0)
      /       \                                    /
  P1 (x1, y1)───P3 (x3, y3)                  P1 (x1, y1)───P3 (x3, y3)
```

Three points form a valid triangle if and only if they are **non-collinear** and **distinct**. If the points lie along the same straight line, the enclosed geometric area collapses to zero.

### The Signed Area Formula

The area of a triangle formed by three points can be computed using the Shoelace formula (or cross product determinant):

$$\text{Area} = \frac{1}{2} \, |x_1(y_2 - y_3) + x_2(y_3 - y_1) + x_3(y_1 - y_2)|$$

To optimize computation, we compute twice the signed area ($D$) first:

$$D = x_1(y_2 - y_3) + x_2(y_3 - y_1) + x_3(y_1 - y_2)$$

The true geometric area is simply:

$$\text{Area} = \frac{|D|}{2}$$

### Orientation and Geometric Insights

Beyond determining whether an area exists, the sign of $D$ yields the **winding order** (orientation) of the vertices:
- **$D > 0$:** The vertices $P_1 \to P_2 \to P_3$ are arranged in a **counterclockwise (CCW)** sequence.
- **$D < 0$:** The vertices are arranged in a **clockwise (CW)** sequence.
- **$D = 0$:** The points are **collinear** (or duplicate points exist), meaning no valid triangle can be constructed.

### Handling Floating-Point Imprecision in Java

In digital computation, evaluating floating-point numbers (`double`) for exact equality (`== 0.0`) is unsafe due to IEEE 754 precision limitations and accumulated rounding errors. A robust implementation requires an epsilon threshold ($\epsilon$):

```java
public final class GeometryUtils {

    public static final double EPSILON = 1.0e-9;

    private GeometryUtils() {}

    /**
     * Determines whether three points form a valid, non-collinear triangle.
     */
    public static boolean formsTriangle(
            double x1, double y1,
            double x2, double y2,
            double x3, double y3) {

        double twiceSignedArea = 
                x1 * (y2 - y3) 
                + x2 * (y3 - y1) 
                + x3 * (y1 - y2);

        // Valid only if the absolute area exceeds our floating-point tolerance
        return Math.abs(twiceSignedArea) > EPSILON;
    }
}
```

> **Automatic Edge Case Handling:** If any two vertices are identical (e.g., $P_1 = P_2$), the formula inherently evaluates to zero, naturally rejecting repeated points without requiring separate pairwise distance checks.

---

## Milestone 2: Line Intersections and Standard Form Analysis

When modeling lines in software, choosing the appropriate mathematical representation is critical.

### Why Standard Form Beats Slope-Intercept

A common beginner mistake is using the slope-intercept equation:

$$y = mx + c$$

However, this form fails for vertical lines ($x = k$), where the slope $m$ approaches infinity, causing division-by-zero errors.

Instead, Group 1 represented lines in **standard general form**:

$$a x + b y = c$$

| Characteristic | Slope-Intercept Form ($y = mx + c$) | Standard Form ($ax + by = c$) |
|---|---|---|
| **Vertical Lines ($x = k$)** | Fails (requires infinite slope $m = \infty$) | Handled naturally ($a = 1, b = 0, c = k$) |
| **Horizontal Lines ($y = k$)** | Supported ($m = 0, c = k$) | Handled naturally ($a = 0, b = 1, c = k$) |
| **Matrix Compatibility** | Requires algebraic rearranging | Directly maps to linear systems $Ax = b$ |
| **Numerical Stability** | Prone to overflow near vertical slopes | Balanced coefficients avoid extreme values |

### Solving Two Lines via Cramer's Rule

Consider two lines in standard form:

$$a_1 x + b_1 y = c_1$$
$$a_2 x + b_2 y = c_2$$

Expressed as a system of linear equations:

$$\begin{bmatrix} a_1 & b_1 \\ a_2 & b_2 \end{bmatrix} \begin{bmatrix} x \\ y \end{bmatrix} = \begin{bmatrix} c_1 \\ c_2 \end{bmatrix}$$

The primary determinant of the coefficient matrix is:

$$D = a_1 b_2 - a_2 b_1$$

When $D \neq 0$, the lines are non-parallel and intersect at exactly one unique point $(x, y)$. Applying **Cramer's Rule**:

$$D_x = c_1 b_2 - c_2 b_1, \qquad D_y = a_1 c_2 - a_2 c_1$$

$$x = \frac{D_x}{D}, \qquad y = \frac{D_y}{D}$$

```text
    Case 1: Unique Point (D ≠ 0)         Case 2: Parallel (D = 0, Dx ≠ 0)       Case 3: Coincident (D = 0, Dx = Dy = 0)
             \     /                                 /         /                                / (L1)
              \   /                                 /         /                                // 
               \ / (Intersection)                  /         /                                // (L2 identical)
                X                                 / (L1)    / (L2)                           //
               / \                               /         /                                //
```

### Distinguishing Parallel vs. Coincident Lines

When $D \approx 0$, the normal vectors $[a_1, b_1]$ and $[a_2, b_2]$ are linearly dependent. We examine the augmented determinants ($D_x$ and $D_y$) to distinguish between parallel and identical lines:

1. **Coincident Lines:** If $|D| \leq \epsilon$, $|D_x| \leq \epsilon$, and $|D_y| \leq \epsilon$, both equations describe the exact same geometric line. There are **infinitely many intersection points**.
2. **Parallel Lines:** If $|D| \leq \epsilon$ but either $|D_x| > \epsilon$ or $|D_y| > \epsilon$, the lines have identical direction but distinct offsets. There is **no intersection point**.

### Immutable Domain Records

Modern Java records cleanly encapsulate geometric primitives:

```java
public record Point(double x, double y) {}

public record Line(double a, double b, double c) {
    public Line {
        // Enforce geometric validity: a and b cannot both be zero
        if (Math.abs(a) <= GeometryUtils.EPSILON && Math.abs(b) <= GeometryUtils.EPSILON) {
            throw new IllegalArgumentException("Invalid line: coefficients 'a' and 'b' cannot both be zero.");
        }
    }
}
```

---

## Milestone 3: Overdetermined Systems ($Ax = b$) with Three Lines

The third milestone expanded the system to three simultaneous lines in two dimensions:

$$a_1 x + b_1 y = c_1$$
$$a_2 x + b_2 y = c_2$$
$$a_3 x + b_3 y = c_3$$

In matrix form:

$$A = \begin{bmatrix} a_1 & b_1 \\ a_2 & b_2 \\ a_3 & b_3 \end{bmatrix}, \quad x = \begin{bmatrix} x \\ y \end{bmatrix}, \quad b = \begin{bmatrix} c_1 \\ c_2 \\ c_3 \end{bmatrix}$$

$$Ax = b$$

Here, $A$ is a $3 \times 2$ matrix, $x$ is a $2 \times 1$ coordinate vector, and $b$ is a $3 \times 1$ vector of constants.

### The Overdetermined Dilemma

Because there are **more equations (3) than unknowns (2)**, this system is overdetermined. In general 2D space, three arbitrary lines will rarely intersect at a single common point.

```text
       [Configuration A]                   [Configuration B]                   [Configuration C]
      Concurrent Point                      Enclosed Triangle                   Parallel Transversal
             \  |  /                               \     /                               /     /
              \ | /                                 \   /                               /     /   /
               \|/                                   \ /                               /     /   / (L3)
       ─────────X─────────                   ─────────X─────────                      / (L1)/ (L2)
               /|\                                   / \                             /     /
              / | \                                 /   \                           /     /
             /  |  \                               /     \                         /     /
```

### Algorithmic Evaluation Strategy

To programmatically classify the relationship between the three lines, Group 1 utilized the following systematic pipeline:

```text
┌─────────────────────────────────────────────────────────────┐
│ 1. Pairwise Determinants: Compute D(L1,L2), D(L2,L3), D(L3,L1)│
└──────────────────────────────┬──────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
 Are any lines parallel?              Are all pairs non-parallel?
   (D_ij ≈ 0)                                (All D_ij ≠ 0)
            │                                     │
            ▼                                     ▼
 Classify parallel / coincident       Compute 3 pairwise intersections:
 line combinations                    P1 = L1∩L2, P2 = L2∩L3, P3 = L3∩L1
                                                  │
                                                  ▼
                                      Are P1, P2, P3 identical?
                                      (Within tolerance ε)
                                       /              \
                                     YES              NO
                                     /                  \
                        Concurrent Intersection    Test formsTriangle(P1, P2, P3)
                        at point P*                - Returns TRUE: Valid Triangle
                                                   - Returns FALSE: Degenerate
```

1. **Step 1: Check for Concurrency (Single Common Intersection):**
   - Find a non-parallel pair (e.g., $L_1$ and $L_2$ where $|D_{12}| > \epsilon$) and compute their unique intersection point $P^* = (x^*, y^*)$.
   - Substitute $(x^*, y^*)$ into the third line equation and compute the residual:
     $$\text{Residual} = |a_3 x^* + b_3 y^* - c_3|$$
   - If $\text{Residual} \leq \epsilon$, all three lines pass through the exact same point $P^*$ (**concurrent lines**).

2. **Step 2: Check for Triangle Formation:**
   - If the lines are not concurrent, solve for all three pairwise intersections:
     $$P_{12} = L_1 \cap L_2, \quad P_{23} = L_2 \cap L_3, \quad P_{31} = L_3 \cap L_1$$
   - Feed these three intersection points into `formsTriangle(P12, P23, P31)`. If the signed area is non-zero, the three lines enclose a **valid geometric triangle**.

3. **Exact Consistency vs. Least-Squares Fitting:**
   - In numerical methods and machine learning, overdetermined systems are frequently approximated via the normal equations:
     $$A^T A x = A^T b \implies x = (A^T A)^{-1} A^T b$$
   - However, Group 1 highlighted that for analytical geometry verification, a least-squares approximation is inappropriate because the goal is detecting exact concurrency or discrete geometric shapes, rather than finding a "best-fit compromise" point.

---

## Milestone 4: JavaFX GUI Architecture and Input Engineering

A primary focus of Group 1's implementation was presenting an intuitive, crash-proof JavaFX interface for user interaction.

### Matrix Entry Layout via `GridPane`

The user interface arranged the nine required inputs into an aligned grid layout that visually mirrored the mathematical matrix:

```text
┌────────────────────────────────────────────────────────────────────────┐
│                      Linear Geometry Analyzer                          │
├────────────────────────────────────────────────────────────────────────┤
│             [ a · x ]   +   [ b · y ]   =   [  c  ]                    │
│  Line 1:   [ TextField ]   [ TextField ]   [ TextField ]               │
│  Line 2:   [ TextField ]   [ TextField ]   [ TextField ]               │
│  Line 3:   [ TextField ]   [ TextField ]   [ TextField ]               │
│                                                                        │
│               [ Calculate Relationship ]    [ Reset ]                  │
│                                                                        │
│  Status & Results:                                                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ » Lines intersect pairwise to form a triangle.                   │  │
│  │   Vertices: (0.0, 0.0), (4.0, 0.0), (0.0, 3.0)                   │  │
│  │   Enclosed Area: 6.000 sq units (Counterclockwise orientation)   │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

### Defensive Input Parsing and Validation

User-entered text must be thoroughly checked before performing calculations. Robust validation prevents crashes like `NumberFormatException`, division by zero, or `NaN` outputs.

```java
public class InputValidator {

    /**
     * Parses and validates a single double value from a TextField.
     */
    public static double parseField(TextField field, String fieldName) {
        String text = field.getText();
        
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Field '" + fieldName + "' cannot be empty.");
        }

        double value;
        try {
            value = Double.parseDouble(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Field '" + fieldName + "' must be a valid numerical value.");
        }

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Field '" + fieldName + "' cannot be NaN or Infinite.");
        }

        return value;
    }

    /**
     * Constructs and validates a Line instance from three text fields.
     */
    public static Line parseLine(TextField aField, TextField bField, TextField cField, int lineNumber) {
        double a = parseField(aField, "Line " + lineNumber + " (a)");
        double b = parseField(bField, "Line " + lineNumber + " (b)");
        double c = parseField(cField, "Line " + lineNumber + " (c)");

        return new Line(a, b, c);
    }
}
```

### Categorized Result Messaging

Instead of binary "Success/Failure" statuses, the interface provides descriptive geometric classifications:

- `Concurrent:` "All three lines intersect at common point (x, y)."
- `Triangle Formed:` "The three lines form a triangle with vertices P1, P2, and P3. Total Area = A."
- `Parallel Pairs:` "Lines 1 and 2 are parallel; no common intersection exists."
- `Coincident Lines:` "Lines 2 and 3 are coincident (represent the same line)."
- `Validation Error:` "Line 1 is degenerate: coefficients 'a' and 'b' cannot both be zero."

---

## Software Architecture & Testability: Layered Decoupling

A critical architectural topic discussed during the presentation was **avoiding UI-tight coupling**. Embedding mathematical algorithms directly inside JavaFX event handlers makes the business logic impossible to unit test without spinning up the JavaFX application thread.

Group 1 structured their application following clean separation of concerns:

```text
src/main/java/
├── application/
│   └── GeometryApp.java           # JavaFX launch lifecycle & Stage initialization
├── controller/
│   └── GeometryController.java    # Handles button clicks, UI field binding & display
├── model/
│   ├── Point.java                 # Immutable (x, y) record
│   ├── Line.java                  # Standard form line representation
│   └── AnalysisReport.java        # Structured analysis result object
└── service/
    └── GeometryService.java       # Pure mathematical engine (determinants, area, intersections)
```

```text
       ┌────────────────────────┐
       │   GeometryApp.java     │  (Boots JavaFX Application)
       └───────────┬────────────┘
                   │
                   ▼
       ┌────────────────────────┐
       │ GeometryController.java│  (Reads TextFields, handles actions, updates labels)
       └───────────┬────────────┘
                   │
         Delegates Computation
                   ▼
       ┌────────────────────────┐         ┌────────────────────────┐
       │ GeometryService.java   │ ◄───────┤   JUnit 5 Unit Tests   │  (No JavaFX runtime needed!)
       └───────────┬────────────┘         └────────────────────────┘
                   │ Uses
                   ▼
       ┌────────────────────────┐
       │  Point.java / Line.java│  (Domain Models)
       └────────────────────────┘
```

### Unit Testing Matrix

Because `GeometryService` is an isolated, pure-Java service, it can be thoroughly tested using JUnit without any UI dependencies:

| Test Case | Scenario / Input Configuration | Expected Assertion |
|---|---|---|
| **Distinct Triangle** | Points: $(0,0), (4,0), (0,3)$ | `formsTriangle(...) == true`, $\text{Area} = 6.0$ |
| **Collinear Points** | Points: $(1,1), (2,2), (3,3)$ | `formsTriangle(...) == false` |
| **Duplicate Vertices** | Points: $(2,5), (2,5), (7,9)$ | `formsTriangle(...) == false` |
| **Unique Intersection** | $x + y = 4$ and $x - y = 0$ | Point $(2.0, 2.0)$ returned |
| **Parallel Lines** | $2x + 4y = 8$ and $2x + 4y = 12$ | `isParallel(...) == true`, no intersection |
| **Coincident Lines** | $x + 2y = 3$ and $2x + 4y = 6$ | `isCoincident(...) == true` |
| **Three Concurrent Lines** | $x + y = 4$, $x - y = 0$, and $2x + y = 6$ | Concurrent at $(2.0, 2.0)$ |
| **Three Triangle Lines** | $y = 0$, $x = 0$, and $x + y = 2$ | Vertices $(0,0), (2,0), (0,2)$ form triangle |
| **Zero Coefficients** | $0x + 0y = 5$ | Throws `IllegalArgumentException` |

---

## Key Takeaways

1. **Non-Collinear Points Define Triangles:** Three points construct a valid triangle if and only if their signed area is non-zero ($|D| > \epsilon$). The signed value simultaneously identifies whether the vertex winding order is clockwise or counterclockwise.
2. **Standard Form Avoids Edge Cases:** Modeling lines as $ax + by = c$ rather than $y = mx + c$ eliminates division-by-zero vulnerabilities when working with vertical lines and maps directly to linear systems.
3. **Cramer's Rule and Determinants:** A non-zero 2D determinant ($D \neq 0$) guarantees a unique intersection. When $D \approx 0$, evaluating the augmented determinants ($D_x, D_y$) cleanly separates parallel lines from coincident lines.
4. **Overdetermined Systems ($Ax = b$):** Three lines in a 2D plane constitute a $3 \times 2$ overdetermined system. They meet concurrently only when all three equations share a consistent solution; otherwise, their pairwise intersections may form a triangle or parallel configurations.
5. **Floating-Point Tolerances:** Floating-point math must never rely on direct equality (`== 0.0`). Robust numerical code always applies an epsilon tolerance threshold ($\epsilon = 10^{-9}$).
6. **Defensive GUI Input Parsing:** JavaFX applications must defensively validate inputs across multiple tiers—checking for blank inputs, non-numeric formats, non-finite values (`NaN`, infinity), and degenerate equations ($a = b = 0$).
7. **Architectural Decoupling for Testability:** Isolating geometry calculations inside a pure service layer decoupled from JavaFX controllers enables unit testing without UI dependencies and keeps code maintainable.
