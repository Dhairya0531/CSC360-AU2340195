# Class Reflection — September 29, 2026

## Topics Covered

- Group 1 Project: Advancing TriangleFX from prototype to a polished, production-ready deliverable
- Repository contribution & review: Commit `779c8aa` and merging Pull Request #2 on [Devang280904/CSC360-Group1](https://github.com/Devang280904/CSC360-Group1)
- Comprehensive documentation architecture: Mathematical input contracts, execution pipelines, and reproducible Maven workflows
- User experience refinement: Custom JavaFX `ListCell` rendering in `ComboBox` presets and elimination of redundant UI clutter
- Architectural analysis: End-to-end data flow across model records, defensive parsers, geometry services, and coordinate-transformed canvas rendering
- Quality assurance & testing: JUnit verification of geometric edge cases and Javadoc standardization

---

## Overview: Transitioning TriangleFX from Prototype to Refined Deliverable

During today's session, our efforts on the **TriangleFX** group project centered on refinement, stabilization, and professional polish. Over the past two weeks, the project progressed from its initial conceptualization on September 15 and specification phase on September 17 into a fully functional JavaFX desktop application. 

With the core mathematical engine and basic UI working, our focus shifted toward making the application intuitive to use, transparent in its mathematical operations, and clean in its codebase. A significant portion of my effort involved expanding the technical documentation, refining the visual interface, reviewing teammate contributions via pull requests, and performing an architectural review of the entire execution flow.

```text
┌───────────────────────────┐     ┌───────────────────────────┐     ┌───────────────────────────┐
│   Phase 1: Foundations    │     │   Phase 2: Specification  │     │   Phase 3: Refinement     │
│       (Sept 15)           │ ──► │       (Sept 17)           │ ──► │       (Sept 29)           │
│  - Coordinate math        │     │  - Problem statement      │     │  - UI simplification      │
│  - Cramer's rule & area   │     │  - Natural text parsing   │     │  - Custom ComboBox cells  │
│  - Linear algebra systems │     │  - Viewport mapping       │     │  - Comprehensive README   │
└───────────────────────────┘     └───────────────────────────┘     └───────────────────────────┘
```

The canonical group repository is hosted at [Devang280904/CSC360-Group1](https://github.com/Devang280904/CSC360-Group1).

---

## Documented Contributions: Pull Request #2 and Documentation Overhaul

Collaborative software engineering requires structured integration rather than uncoordinated pushes directly to the `main` branch. During this development cycle, I was responsible for reviewing, refining, and integrating project documentation and visual assets.

### Contribution Details: Commit `779c8aa`

My primary recorded contribution is commit `779c8aa`, titled **“Expand README documentation and add screenshots.”**

The scope of this contribution included:
- **Files Touched:** 3 files modified across the repository.
- **Line Churn:** $+236$ additions and $-113$ deletions, significantly expanding clarity while eliminating obsolete notes.
- **Visual Assets:** Added two high-resolution screenshots capturing the running application in its dark-themed UI state, demonstrating both valid triangle rendering and error-handling dialogs.
- **Mathematical Specification:** Formally documented the matrix formulation, intersection criteria, and validation constraints.
- **Developer Onboarding:** Provided verified Maven build, test, execution, and Javadoc commands.
- **Roadmap & Scope:** Clearly separated completed features from architectural limitations and future work.

In addition to authoring commit `779c8aa`, I reviewed and merged [Pull Request #2](https://github.com/Devang280904/CSC360-Group1/pull/2) into `main`. This provided valuable hands-on experience in evaluating peer code, verifying merge compatibility, and maintaining branch hygiene in a team setting.

---

## README Overhaul: Engineering Documentation for Contributors

A core engineering principle reinforced today is that a codebase is only as usable as its documentation. Prior to this update, running or contributing to the project required reading through Java source files to infer the expected inputs and build requirements.

### Information Architecture of the Revised README

The reorganized `README.md` was structured around several key domains:

```text
README.md Architecture
├── 1. Project Overview & Capabilities
├── 2. Mathematical Model & Input Contracts (Ax = B)
├── 3. System Architecture & Layered Responsibilities
├── 4. Interactive UI Workflows & Preset Demonstrations
├── 5. Developer Guide: Build, Test, Run, and Package
└── 6. Current Limitations & Future Feature Roadmap
```

### The Mathematical Model in Matrix Form

The documentation explicitly details how TriangleFX maps user inputs to linear algebra:

$$A \cdot x = B$$

where:
- $A$ is a $3 \times 2$ coefficient matrix representing the three lines in standard form:
  $$A = \begin{bmatrix} a_{11} & a_{12} \\ a_{21} & a_{22} \\ a_{31} & a_{32} \end{bmatrix}$$
- $x = [x, y]^T$ is the $2 \times 1$ column vector of Cartesian unknowns.
- $B$ is the $3 \times 1$ constant vector:
  $$B = \begin{bmatrix} b_1 \\ b_2 \\ b_3 \end{bmatrix}$$

Each row in the matrix system defines one linear equation in standard form:

$$a_{i1} x + a_{i2} y = b_i$$

The application computes the three pairwise intersection points:

$$P_{12} = L_1 \cap L_2, \quad P_{23} = L_2 \cap L_3, \quad P_{31} = L_3 \cap L_1$$

These coordinates constitute the three vertices of the triangle if and only if each line pair yields a unique intersection ($D_{ij} \neq 0$) and the resulting vertices are distinct and non-collinear ($|D_{\text{area}}| > \epsilon$).

### Reproducible Developer Commands

To eliminate setup guesswork, the README specifies standardized Maven targets:

```sh
# Execute automated unit test suite (Geometry, Parser, and Models)
mvn test

# Launch the interactive JavaFX desktop application
mvn javafx:run

# Clean and compile an executable deployment artifact
mvn clean package

# Generate standardized HTML documentation
mvn javadoc:javadoc
```

Providing verified CLI commands ensures that any contributor can instantly clone the repository and verify system behavior across different machines.

---

## User Interface Simplification & Design System Refinements

A major focus of our work was cleaning up the JavaFX interface. In earlier iterations, the UI had accumulated multiple conflicting input mechanisms that complicated the user experience.

### 1. Preset Selector Styling via Custom `ListCell`

To streamline testing and demonstration, TriangleFX provides pre-configured geometric scenarios (e.g., *Right Triangle*, *Equilateral Triangle*, *Parallel Line Error*, *Collinear Points*). 

Selecting these scenarios is handled via a JavaFX `ComboBox`. However, default JavaFX `ComboBox` controls look mismatched against modern dark themes and frequently suffer from contrast and clipping issues. We customized the styling using a dedicated cell factory:

```text
┌────────────────────────────────────────────────────────┐
│ Preset: [ Select Geometric Scenario...            ▼ ] │
├────────────────────────────────────────────────────────┤
│   ★ Standard Right Triangle (3-4-5)                    │
│   ★ Equilateral Triangle                               │
│   ⚠ Parallel Lines Edge Case (No Intersection)         │
│   ⚠ Collinear Points (Zero Area)                       │
│   ⚠ Concurrent Lines (Single Intersection Point)       │
└────────────────────────────────────────────────────────┘
```

A key technical takeaway was discovering that styling a JavaFX `ComboBox` requires styling **both** the button cell (the control's collapsed face) and the popup list cells (the dropdown overlay). Setting background colors only on the outer control leaves the popup menu unstyled. By implementing a custom `ListCell`, we achieved:
- High-contrast text readability against dark slate backgrounds.
- Explicit visual feedback during mouse hover and item selection.
- Distinct status icons distinguishing valid configurations from edge-case demonstrations.
- Cursor transitions indicating interactive control affordances.

### 2. Elimination of the Raw Matrix Paste Panel

Earlier prototypes included an expandable text area allowing users to paste raw numeric matrices as whitespace-delimited text. During our UI review, we identified several problems with this feature:
- It duplicated the functionality of the structured $3 \times 3$ coefficient grid.
- It required dedicated toggle buttons, expanding UI panels, parsing code, and extra state variables in `TriangleApp`.
- It created ambiguous states (e.g., whether the structured grid or the pasted text took precedence).

We decided to **remove the raw matrix paste panel entirely**. This simplified the UI layout, cut dead code, and focused the user experience on two clear workflows: manually entering values in the structured grid or selecting a preset.

> **Design Principle:** Product refinement often means removing redundant features. A clean interface with one obvious way to perform a task is far more usable than an interface with three half-baked alternatives.

### 3. Refined 5-Step Interaction Workflow

The resulting interface guides the user through an intuitive, linear workflow:

```text
┌────────────────────────┐
│ 1. Enter Coefficients  │  User fills the 3x3 matrix grid or selects a preset
└───────────┬────────────┘
            │
            ▼
┌────────────────────────┐
│  2. Generate Diagram   │  Click "Generate Diagram" to trigger parsing & geometry
└───────────┬────────────┘
            │
            ▼
┌────────────────────────┐
│ 3. Review Equations    │  UI displays standardized equations: a·x + b·y = c
└───────────┬────────────┘
            │
            ▼
┌────────────────────────┐
│ 4. Inspect Triangle    │  Canvas renders auto-scaled shape; panel lists Area & Perimeter
└───────────┬────────────┘
            │
            ▼
┌────────────────────────┐
│ 5. Reset / Clear       │  Reset button flushes canvas and inputs for the next analysis
└────────────────────────┘
```

---

## Architectural Deep Dive: Execution Pipeline & Separation of Concerns

Studying the codebase end-to-end clarified how data flows through the application and how concerns are separated between presentation and computational layers.

### High-Level Execution Pipeline

```text
   [User Input / Preset Selection]
                  │
                  ▼
   [Input Parsing & Defensive Validation]
   - Check non-empty, parse doubles
   - Enforce a² + b² > ε for each line
                  │
                  ▼
   [Three Standard Line Records: Line(a, b, c)]
                  │
                  ▼
   [Pairwise Intersection Engine (GeometryService)]
   - Evaluate determinants: D = a1·b2 - a2·b1
   - Compute P12, P23, P31 via Cramer's Rule
                  │
                  ▼
   [Triangle Non-Degeneracy Verification]
   - Verify 3 distinct vertices (distance > ε)
   - Evaluate Shoelace determinant (|D_area| > ε)
                  │
                  ▼
   [Triangle Model Construction]
   - Calculates side lengths, perimeter, and area
                  │
                  ▼
   [JavaFX Canvas Rendering & Status Logging]
   - Coordinate transformation: Math Space ──► Canvas Pixels
   - Render grid, axes, polygon, vertices, and labels
```

### 1. The Domain Model Layer

Domain concepts are represented by immutable records:

```java
public record Point(double x, double y) {}

public record Line(double a, double b, double c) {
    public Line {
        if (Math.abs(a) < 1e-9 && Math.abs(b) < 1e-9) {
            throw new IllegalArgumentException("Line coefficients 'a' and 'b' cannot both be zero.");
        }
    }
}

public record Triangle(Point p1, Point p2, Point p3) {
    public double sideA() { return distance(p2, p3); }
    public double sideB() { return distance(p3, p1); }
    public double sideC() { return distance(p1, p2); }

    public double perimeter() {
        return sideA() + sideB() + sideC();
    }

    public double area() {
        double d = p1.x() * (p2.y() - p3.y())
                 + p2.x() * (p3.y() - p1.y())
                 + p3.x() * (p1.y() - p2.y());
        return Math.abs(d) / 2.0;
    }

    private static double distance(Point a, Point b) {
        return Math.hypot(b.x() - a.x(), b.y() - a.y());
    }
}
```

Encapsulating these values in dedicated domain models prevents raw arrays or floating-point primitives from leaking across UI controllers.

### 2. Geometry Service: Determinants and Tolerances

The geometry service isolates all computational logic. For any two lines $a_1 x + b_1 y = c_1$ and $a_2 x + b_2 y = c_2$, the determinant is:

$$D = a_1 b_2 - a_2 b_1$$

- **Non-Zero Determinant ($|D| > \epsilon$):** Yields a unique intersection via Cramer's rule:
  $$x = \frac{c_1 b_2 - c_2 b_1}{D}, \qquad y = \frac{a_1 c_2 - a_2 c_1}{D}$$
- **Zero Determinant ($|D| \leq \epsilon$):** Identifies parallel or coincident lines, immediately halting triangle construction with an informative error message.

### 3. Coordinate Transformation & Auto-Fit Canvas Rendering

Because mathematical Cartesian coordinates place $+Y$ pointing upward while JavaFX canvas coordinates place $+Y$ pointing downward, the rendering engine translates coordinate systems dynamically:

$$\text{Screen } X = \text{padding} + \text{offset}_x + (x_{\text{world}} - x_{\min}) \cdot \text{scale}$$

$$\text{Screen } Y = \text{height} - \text{padding} - \text{offset}_y - (y_{\text{world}} - y_{\min}) \cdot \text{scale}$$

```text
  Cartesian Coordinate Space                     JavaFX Screen Pixel Space
               +Y                                (0,0) ──────────────► +X
                │                                  │
    P1 (2, 6)   ●                                  │       P1 (ScreenX, ScreenY)
               / \                                 │        ●
              /   \                                │       / \
  ───────────●─────●────────► +X                   │      ●───●
         P2 (1,2) P3 (7,2)                         ▼
                                                  +Y
   Origin at center; +Y UP                        Origin at top-left; +Y DOWN
```

The rendering engine also auto-scales the viewport, fitting any triangle smoothly within the canvas bounds while drawing reference elements:
- Background subtle grid lines
- Cartesian $X$ and $Y$ reference axes with tick marks
- Origin marker $(0, 0)$
- Translucent filled triangle polygon with a solid accent border
- Circular vertex markers annotated with coordinate strings `(x, y)`

---

## Testing Strategy and Codebase Health

Keeping mathematical logic completely detached from the JavaFX UI layer allows automated unit testing to run via standard Maven cycles without needing a GUI display environment or running headless wrappers.

### Automated Unit Test Matrix

The test suite exercises both positive geometric scenarios and pathological edge cases:

| Test Class | Test Target | Verification Criteria |
|---|---|---|
| `GeometryServiceTest` | Unique Intersection | Accurately solves perpendicular and oblique lines |
| `GeometryServiceTest` | Parallel Lines | Verifies $|D| \leq 10^{-9}$ and throws non-intersecting exception |
| `GeometryServiceTest` | Coincident Lines | Flags infinite intersections for identical lines |
| `GeometryServiceTest` | Collinear Vertices | Confirms that points with area $< 10^{-9}$ fail triangle creation |
| `GeometryServiceTest` | Concurrency Check | Rejects systems where three lines intersect at a single point |
| `MatrixParserTest` | Valid Number Parsing | Converts valid numeric text inputs to standard coefficients |
| `MatrixParserTest` | Empty & Malformed Input | Throws descriptive `IllegalArgumentException` on invalid inputs |
| `MatrixParserTest` | Degenerate Lines | Rejects rows where both $a = 0$ and $b = 0$ |

### Javadoc Cleanliness

We also standardized Javadoc comments across the codebase. Earlier development passes left raw HTML styling (`<br>`, `<b>`) inside method docstrings. We cleaned these up into standard, readable Javadoc formatting, ensuring that `mvn javadoc:javadoc` generates clean, warning-free API documentation.

---

## Current Project State vs. Future Roadmap

By September 29, the repository had grown to **13 commits**, transitioning from initial documentation into a polished, working deliverable.

### Implemented Capabilities

- Maven-based compilation, test, and packaging workflow.
- Clean, dark-themed JavaFX desktop application.
- Structured $3 \times 3$ matrix coefficient entry grid.
- Custom-styled preset selector for rapid demonstrations.
- Determinant-based line intersection engine.
- Shoelace area, perimeter, and side length calculations.
- Auto-scaling, coordinate-inverted JavaFX canvas renderer.
- Comprehensive JUnit test suite for geometry and parsing.
- Refined documentation with visual application screenshots.

### Future Roadmap Items

While the application is complete for its primary demonstration goals, several enhancements remain on our roadmap:

1. **Symbolic Equation Parsing:** Incorporating the string parser developed during the September 17 design session to accept natural algebraic strings (e.g., `x + y = 8`, `y = 2x + 1`) alongside the matrix grid.
2. **Infinite Line Extensions:** Drawing the full source lines across the canvas viewport to visualize how the three lines extend past the triangle vertices.
3. **Automated GUI Testing:** Integrating **TestFX** for headless UI integration testing to assert button actions and canvas updates automatically in CI.
4. **Vector / Image Export:** Allowing users to export rendered triangles as high-resolution PNG or SVG files.
5. **External CSS Refactoring:** Moving remaining inline JavaFX styles into a separate, modular `.css` stylesheet.
6. **Keyboard Navigation & Accessibility:** Enhancing tab traversal, keyboard shortcuts, and screen-reader accessible descriptions.

---

## Key Takeaways

1. **Documentation is Core to Delivery:** A project is not finished when the code compiles; comprehensive README documentation, mathematical contracts, and visual screenshots make software understandable and maintainable.
2. **Feature Removal Improves Focus:** Eliminating the confusing raw matrix paste panel streamlined the interface and clarified user workflows without sacrificing capability.
3. **Styling JavaFX Controls Requires Thoroughness:** Complex controls like `ComboBox` cannot be styled through simple parent CSS properties alone; customizing both the button cell and the dropdown list cells is essential for a cohesive dark theme.
4. **Clean Layered Architecture Enables Fast Testing:** Decoupling immutable models and pure geometry calculations from the JavaFX UI layer allows the entire test suite to execute in milliseconds via `mvn test`.
5. **Coordinate Inversion is Essential for Canvas Math:** Converting Cartesian world space ($+Y$ upward) to screen pixel space ($+Y$ downward) with automatic viewport scaling ensures that geometry renders right-side up regardless of scale.
6. **Collaborative Git Hygiene Matters:** Using pull requests (like PR #2) and structured commits enables team review, preserves repository history, and keeps the `main` branch stable.
