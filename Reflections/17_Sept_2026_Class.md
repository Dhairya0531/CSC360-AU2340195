# Class Reflection — September 17, 2026

## Topics Covered

- Advancing the TriangleFX Project: Transitioning from theoretical geometry to project specification
- Requirements engineering & formal scoping in `docs/PROBLEM_STATEMENT.md`
- Flexible equation parsing: Converting raw string equations (`x + y = 8`, `y = 2x + 1`, `x = 4`) to standard form ($ax + by = c$)
- Pairwise intersection solving via Cramer's rule determinants and augmented rank checks
- Verifying non-degenerate triangles using the signed-area Shoelace formula and orientation tests
- JavaFX graphical rendering: World-to-screen coordinate transformations, canvas scaling, and vertex labeling
- Software architecture: Modular separation across parser, geometry domain, rendering, and UI layers

---

## Project Genesis: Advancing from Group 1 to TriangleFX

Following Group 1's project presentation on September 15—which established the core mathematical foundations of coordinate geometry and linear systems—our focus shifted toward designing and architecting **TriangleFX**.

While Group 1's initial prototype relied on a rigid $3 \times 3$ grid of raw numeric text fields (forcing users to manually supply $a$, $b$, and $c$ coefficients), **TriangleFX** elevates the user experience. The application allows users to enter full algebraic equations in natural text form (such as `x + y = 8`, `2x - 3y = 10`, or `y = 2x + 1`). The system parses these equations, computes pairwise intersections, rigorously verifies whether a valid non-degenerate triangle is formed, and graphically renders the resulting triangle on an interactive JavaFX canvas with labeled vertices.

```text
┌───────────────────────────┐     ┌───────────────────────────┐     ┌───────────────────────────┐
│     Sept 15 Foundation    │     │       Sept 17 Progress    │     │     TriangleFX Vision     │
│  - Raw coefficient grid   │ ──► │  - docs/PROBLEM_STATEMENT │ ──► │  - Full text equation UI  │
│  - Mathematical formulas  │     │  - Parsing design specs   │     │  - Interactive Canvas     │
│  - Manual matrix inputs   │     │  - Transformation pipeline│     │  - Real-time rendering    │
└───────────────────────────┘     └───────────────────────────┘     └───────────────────────────┘
```

The primary milestone achieved on September 17 was completing the **documentation and specification phase**, formalizing the project scope in `docs/PROBLEM_STATEMENT.md`, establishing mathematical validation invariants, and designing the string parsing and rendering engines.

---

## Milestone 1: Requirements Engineering and Problem Statement Scoping

A critical software engineering practice emphasized in class is that robust implementation begins with explicit requirements specification. Diving straight into JavaFX code without defining mathematical boundaries leads to brittle software.

In `docs/PROBLEM_STATEMENT.md`, we established the core functional and non-functional requirements for TriangleFX:

### 1. Functional Scope

1. **Textual Input:** Accept three user-provided linear equations as free-form strings.
2. **Equation Normalization:** Parse heterogeneous linear formats into canonical standard form:
   $$a x + b y = c$$
3. **Intersection Calculation:** Compute the three pairwise intersection points:
   $$P_1 = L_1 \cap L_2, \quad P_2 = L_2 \cap L_3, \quad P_3 = L_3 \cap L_1$$
4. **Geometric Validation:** Determine if the intersections form a valid, non-collinear, non-degenerate triangle.
5. **Visual Rendering:** Dynamically scale, center, and draw the triangle and coordinate axes on a JavaFX `Canvas`, annotating each vertex with its computed Cartesian coordinates.
6. **Graceful Error Reporting:** Provide clear feedback for syntax errors, parallel lines, coincident lines, or collinear intersection points.

### 2. Supported Equation Formats

To maximize usability, TriangleFX does not restrict the user to a single syntax. The parser is designed to recognize:

| Syntax Type | User Input Example | Canonical Normalization ($ax + by = c$) | Notes |
|---|---|---|---|
| **Standard Form** | `x + y = 8` | $1x + 1y = 8$ | Direct $a, b, c$ extraction |
| **Coefficients & Signs** | `2x - 3y = 10` | $2x - 3y = 10$ | Handles explicit negative signs |
| **Slope-Intercept Form** | `y = 2x + 1` | $-2x + 1y = 1$ | Transposes $x$-term to LHS |
| **Vertical Line** | `x = 4` | $1x + 0y = 4$ | Missing $y$-term ($b = 0$) |
| **Horizontal Line** | `y = -3` | $0x + 1y = -3$ | Missing $x$-term ($a = 0$) |
| **Reversed Variable Order**| `4y + 3x = 12` | $3x + 4y = 12$ | Commutative term reordering |

---

## Milestone 2: Equation Parsing Engine — From String to Standard Form

The most significant architectural upgrade from Group 1's prototype is the **Equation Parsing Pipeline**. Instead of requiring users to decompose lines into numeric matrices themselves, the software parses natural linear syntax.

```text
  Raw String: "y = 2x + 1"
          │
          ▼  [Step 1: Sanitize]
  Remove whitespace, validate single '=' sign
          │
          ▼  [Step 2: Split Sides]
  LHS: "y",  RHS: "2x+1"
          │
          ▼  [Step 3: Transpose & Group]
  Move variables to LHS, constants to RHS: "-2x + y = 1"
          │
          ▼  [Step 4: Regex Token Extraction]
  Match terms: a = -2.0, b = 1.0, c = 1.0
          │
          ▼  [Step 5: Validity Check]
  Verify a² + b² ≠ 0  ──►  Yields Line(-2.0, 1.0, 1.0)
```

### Parsing Algorithm & Regex Modeling

A robust parser splits the equation by `=`, moves all variable terms to the Left-Hand Side (LHS) and numeric constants to the Right-Hand Side (RHS), flipping their signs upon crossing the equality boundary.

A regular expression matches individual linear terms:

```java
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EquationParser {

    // Regex pattern matching terms like: 2x, -3.5y, +x, -y, 7, -12.4
    private static final Pattern TERM_PATTERN = 
            Pattern.compile("([+-]?[0-9]*\\.?[0-9]+)?([xy])|([+-]?[0-9]*\\.?[0-9]+)");

    private EquationParser() {}

    /**
     * Parses an arbitrary linear equation string into standard form: ax + by = c
     */
    public static Line parse(String equationStr) {
        if (equationStr == null || !equationStr.contains("=")) {
            throw new IllegalArgumentException("Invalid equation: Missing '=' sign.");
        }

        String[] parts = equationStr.replaceAll("\\s+", "").split("=");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid equation: Must contain exactly one '=' sign.");
        }

        double a = 0.0, b = 0.0, c = 0.0;

        // Process Left-Hand Side (terms keep their signs)
        double[] lhs = extractCoefficients(parts[0], 1.0);
        // Process Right-Hand Side (terms flip signs when moved to LHS)
        double[] rhs = extractCoefficients(parts[1], -1.0);

        a = lhs[0] + rhs[0];
        b = lhs[1] + rhs[1];
        // Constants on LHS move to RHS (flip sign), constants on RHS stay positive
        c = -(lhs[2] + rhs[2]);

        // Enforce geometric validity
        if (Math.abs(a) < 1e-9 && Math.abs(b) < 1e-9) {
            throw new IllegalArgumentException("Degenerate line: 'x' and 'y' coefficients cannot both be zero.");
        }

        return new Line(a, b, c);
    }

    private static double[] extractCoefficients(String expression, double signMultiplier) {
        double a = 0.0, b = 0.0, constant = 0.0;
        
        Matcher matcher = TERM_PATTERN.matcher(expression);
        while (matcher.find()) {
            String fullMatch = matcher.group();
            if (fullMatch.isEmpty()) continue;

            String coeffStr = matcher.group(1);
            String varStr = matcher.group(2);
            String pureConst = matcher.group(3);

            if (varStr != null) {
                double coeff = parseCoefficient(coeffStr);
                if (varStr.equalsIgnoreCase("x")) {
                    a += coeff * signMultiplier;
                } else if (varStr.equalsIgnoreCase("y")) {
                    b += coeff * signMultiplier;
                }
            } else if (pureConst != null) {
                constant += Double.parseDouble(pureConst) * signMultiplier;
            }
        }
        return new double[]{a, b, constant};
    }

    private static double parseCoefficient(String coeffStr) {
        if (coeffStr == null || coeffStr.isEmpty() || coeffStr.equals("+")) return 1.0;
        if (coeffStr.equals("-")) return -1.0;
        return Double.parseDouble(coeffStr);
    }
}
```

This design handles single variables (`x = 4`), slope-intercept forms (`y = 2x + 1`), and standard formats with arbitrary spacing without crashing.

---

## Milestone 3: Computational Geometry Engine — Pairwise Intersections & Non-Degeneracy

Once the three equations are normalized into standard lines $L_1, L_2, L_3$, the geometry engine calculates their pairwise intersections and validates the triangle.

```text
                     Line 1: a1·x + b1·y = c1
                            /        \
                           /          \
                          /   P1       \
                         /     /\       \
                        /     /  \       \
                       /     /    \       \
       Line 2:        /     /      \       \       Line 3:
   a2·x + b2·y = c2  /    P2────────P3      \  a3·x + b3·y = c3
```

### 1. Pairwise Intersections via Cramer's Rule

For any two lines $L_i$ and $L_j$:

$$\begin{bmatrix} a_i & b_i \\ a_j & b_j \end{bmatrix} \begin{bmatrix} x \\ y \end{bmatrix} = \begin{bmatrix} c_i \\ c_j \end{bmatrix}$$

We evaluate the determinant:

$$D_{ij} = a_i b_j - a_j b_i$$

- **If $|D_{ij}| \leq \epsilon$:** The lines are either **strictly parallel** or **coincident**. In either case, they do not form a unique intersection point, and triangle construction terminates immediately.
- **If $|D_{ij}| > \epsilon$:** The lines intersect at a unique point:
  $$x = \frac{c_i b_j - c_j b_i}{D_{ij}}, \qquad y = \frac{a_i c_j - a_j c_i}{D_{ij}}$$

Computing all three combinations yields candidates:
$$P_1 = L_1 \cap L_2, \quad P_2 = L_2 \cap L_3, \quad P_3 = L_3 \cap L_1$$

### 2. Validating the Non-Degenerate Triangle

Finding three intersection points is **necessary but not sufficient**. Three lines can produce intersection points that fail to form a real triangle:

1. **Concurrent Lines:** If all three lines intersect at the exact same point ($P_1 = P_2 = P_3$), the triangle collapses into a single coordinate.
2. **Collinear Intersection Points:** If the three intersection points fall onto a single line, the enclosed area is zero.

To verify non-degeneracy, we apply the signed-area Shoelace determinant:

$$D_{\text{area}} = x_1(y_2 - y_3) + x_2(y_3 - y_1) + x_3(y_1 - y_2)$$

$$\text{Area} = \frac{1}{2} \, |D_{\text{area}}|$$

```java
public final class GeometryEngine {

    public static final double EPSILON = 1e-9;

    public record Point(double x, double y) {}
    public record Line(double a, double b, double c) {}

    public static Point intersect(Line l1, Line l2) {
        double d = l1.a() * l2.b() - l2.a() * l1.b();
        if (Math.abs(d) <= EPSILON) {
            throw new ArithmeticException("Lines are parallel or coincident; no unique intersection.");
        }
        double dx = l1.c() * l2.b() - l2.c() * l1.b();
        double dy = l1.a() * l2.c() - l2.a() * l1.c();
        return new Point(dx / d, dy / d);
    }

    public static double computeArea(Point p1, Point p2, Point p3) {
        double twiceSignedArea = 
                p1.x() * (p2.y() - p3.y()) 
                + p2.x() * (p3.y() - p1.y()) 
                + p3.x() * (p1.y() - p2.y());
        return Math.abs(twiceSignedArea) / 2.0;
    }

    public static boolean isValidTriangle(Point p1, Point p2, Point p3) {
        return computeArea(p1, p2, p3) > EPSILON;
    }
}
```

---

## Milestone 4: Graphical Visualization Pipeline in JavaFX

A major topic of discussion on September 17 was bridging the gap between mathematical world coordinates and JavaFX pixel rendering.

### The Coordinate System Discrepancy

Mathematical Cartesian coordinates and computer screen coordinates differ fundamentally in their vertical orientation:

```text
  Standard Cartesian System (Math)              JavaFX Screen System (Canvas)
              +Y                                (0,0) ──────────────► +X
               │                                  │
               │                                  │
  ─────────────┼─────────────► +X                 │
               │                                  ▼
               │                                 +Y
              -Y
   Origin at center; +Y points UP               Origin at top-left; +Y points DOWN
```

If mathematical coordinates are drawn directly to a JavaFX `Canvas`, the triangle will render upside down.

### World-to-Screen Coordinate Transformation

To render the triangle dynamically regardless of whether coordinates are small (e.g., $(0, 1)$) or large (e.g., $(100, 250)$), TriangleFX applies an automated **bounding-box normalization and viewport mapping**:

1. **Determine World Bounds:**
   $$x_{\min} = \min(x_1, x_2, x_3), \quad x_{\max} = \max(x_1, x_2, x_3)$$
   $$y_{\min} = \min(y_1, y_2, y_3), \quad y_{\max} = \max(y_1, y_2, y_3)$$

2. **Compute Dimensions & Scale Factor:**
   To prevent distortion, we use uniform scaling with padding:
   $$\Delta x = \max(x_{\max} - x_{\min}, \, \epsilon), \quad \Delta y = \max(y_{\max} - y_{\min}, \, \epsilon)$$
   $$s_x = \frac{W - 2 \cdot \text{pad}}{\Delta x}, \quad s_y = \frac{H - 2 \cdot \text{pad}}{\Delta y}$$
   $$\text{Scale } s = \min(s_x, s_y)$$

3. **Invert the Y-Axis:**
   Mathematical coordinates $(x_w, y_w)$ map to screen pixels $(x_s, y_s)$ via:
   $$x_s = \text{pad} + (x_w - x_{\min}) \cdot s + x_{\text{offset}}$$
   $$y_s = H - \text{pad} - (y_w - y_{\min}) \cdot s - y_{\text{offset}}$$

```java
public class CoordinateTransform {
    private final double scale;
    private final double xMin, yMin;
    private final double xOffset, yOffset;
    private final double canvasHeight;
    private final double padding;

    public CoordinateTransform(Point p1, Point p2, Point p3, double width, double height, double padding) {
        this.canvasHeight = height;
        this.padding = padding;

        this.xMin = Math.min(p1.x(), Math.min(p2.x(), p3.x()));
        double xMax = Math.max(p1.x(), Math.max(p2.x(), p3.x()));
        this.yMin = Math.min(p1.y(), Math.min(p2.y(), p3.y()));
        double yMax = Math.max(p1.y(), Math.max(p2.y(), p3.y()));

        double deltaX = Math.max(xMax - xMin, 1.0);
        double deltaY = Math.max(yMax - yMin, 1.0);

        double availableWidth = width - 2 * padding;
        double availableHeight = height - 2 * padding;

        this.scale = Math.min(availableWidth / deltaX, availableHeight / deltaY);
        this.xOffset = (availableWidth - deltaX * scale) / 2.0;
        this.yOffset = (availableHeight - deltaY * scale) / 2.0;
    }

    public double toScreenX(double worldX) {
        return padding + xOffset + (worldX - xMin) * scale;
    }

    public double toScreenY(double worldY) {
        return canvasHeight - (padding + yOffset + (worldY - yMin) * scale);
    }
}
```

### Canvas Drawing Pipeline

With the transformation matrix established, the JavaFX renderer performs sequential drawing operations:
1. **Background & Grid Lines:** Clear canvas, draw subtle grid lines and axes.
2. **Filled Triangle Polygon:** Call `gc.fillPolygon(xPoints, yPoints, 3)` using a semi-transparent accent fill.
3. **Triangle Perimeter:** Outline edges with `gc.strokePolygon(xPoints, yPoints, 3)`.
4. **Vertex Handles & Labels:** Draw circular anchors at $(x_s, y_s)$ and render readable labels (e.g., `P1 (2.0, 6.0)`).

```text
┌──────────────────────────────────────────────────────────────┐
│  TriangleFX Visualizer                                       │
│                                                              │
│              P1 (2.0, 6.0)                                   │
│                  ●                                           │
│                 / \                                          │
│                /   \         Area: 12.00 sq units            │
│               /     \                                        │
│              /       \                                       │
│             /         \                                      │
│  P2 (1.0, 2.0) ●───────● P3 (7.0, 2.0)                       │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## Architectural Blueprint and Implementation Roadmap

To maintain clean separation of concerns, the TriangleFX codebase is structured into four distinct modules:

```text
src/main/java/com/trianglefx/
├── parser/
│   └── EquationParser.java         # Tokenizes & converts text equations to Line records
├── model/
│   ├── Point.java                  # Immutable (x, y) coordinate
│   ├── Line.java                   # Standard form representation: ax + by = c
│   └── Triangle.java               # Three vertices, computed area, and perimeter
├── service/
│   ├── GeometryEngine.java         # Solves intersections, determinants & area checks
│   └── CoordinateTransform.java    # Maps world Cartesian space to screen pixel bounds
├── view/
│   ├── TriangleCanvas.java         # Custom JavaFX Canvas rendering shapes & labels
│   └── ControlPanel.java           # Text input fields, action buttons, & status logs
└── TriangleFXApp.java              # JavaFX Application entry point
```

### Comprehensive Validation Flow

```text
User Submits 3 Strings
         │
         ▼
[Syntax Validation] ──────────► Syntax Error (Missing '=', invalid tokens)
         │
         ▼
[Geometric Line Validation] ──► Degenerate Line (a = b = 0)
         │
         ▼
[Pairwise Intersections] ─────► Parallel / Coincident Lines (No intersection)
         │
         ▼
[Area & Collinearity Check] ──► Collinear Points / Concurrency (Zero area)
         │
         ▼
[Coordinate Transformation]
         │
         ▼
[Render Triangle & Labels]
```

---

## Key Takeaways

1. **Specification Precedes Implementation:** Establishing a formal problem statement (`docs/PROBLEM_STATEMENT.md`) ensures that edge cases—such as vertical lines, syntax variations, and degenerate geometries—are addressed before writing UI code.
2. **Text Parsing Enhances UX:** Moving from raw coefficient matrix entry to regex-based natural equation parsing (`x + y = 8`, `y = 2x + 1`, `x = 4`) makes the software practical and user-friendly.
3. **Standard Form Simplifies Systems:** Normalizing every equation into $ax + by = c$ establishes a consistent mathematical baseline, eliminating edge cases with infinite slopes.
4. **Two-Stage Geometric Validation:** Finding three pairwise intersections is not enough; the system must also verify that the intersection points do not collapse into a single point (concurrency) or lie on a straight line (collinearity) by testing that the signed area strictly exceeds zero.
5. **Handling Inverted Screen Coordinates:** JavaFX uses a top-left origin where $+Y$ points downward. Rendering Cartesian geometry requires inverting the $Y$-axis and applying automated bounding-box scaling to keep shapes centered and properly proportioned.
6. **Separation of Concerns:** Dividing the project into `parser`, `model`, `service`, and `view` packages ensures that the core mathematical engine can be unit tested without requiring a JavaFX display environment.
