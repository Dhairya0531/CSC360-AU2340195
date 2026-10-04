# Class Reflection — October 1, 2026

## Topics Covered

- TriangleFX Finalization: Preparing the project demonstration and presentation slide deck (PPT)
- Structuring technical presentations: Translating complex mathematical codebases into clear visual slides
- Comprehensive demonstration roadmap: Showcasing positive geometries, edge-case handling, and error resiliency
- Visualizing core algorithms: Diagramming matrix systems ($Ax = B$), Cramer's rule, and Shoelace area verification
- Technical defense preparation: Addressing architectural trade-offs, coordinate inversions, and floating-point tolerances
- End-to-end project retrospective: Tracing the evolution of TriangleFX from inception to completion

---

## Overview: Preparing TriangleFX for Project Demonstration

In today's class, our work on the **TriangleFX** group project reached its culmination: synthesizing our codebase, architectural decisions, and documentation into a comprehensive **presentation slide deck (PPT)** and structuring our live project demonstration.

Over the preceding sessions, we navigated the entire software engineering lifecycle:
1. **September 15:** Analyzed the fundamental mathematics of 2D line intersections and triangle non-degeneracy.
2. **September 17:** Established formal project specifications in `docs/PROBLEM_STATEMENT.md` and designed natural string parsing and viewport transformation engines.
3. **September 29:** Refined the JavaFX UI, implemented custom `ComboBox` `ListCell` renderers, removed redundant UI panels, authored commit `779c8aa`, and merged Pull Request #2.
4. **October 1 (Today):** Built the presentation slide deck, structured our live demonstration walkthrough, and prepared our technical defense.

```text
┌───────────────────────────┐     ┌───────────────────────────┐     ┌───────────────────────────┐
│     Sept 15 - Sept 17     │     │          Sept 29          │     │          Oct 01           │
│  - Mathematical Theory    │ ──► │  - UI & Code Refinement   │ ──► │  - Slide Deck (PPT) Prep  │
│  - Problem Statement      │     │  - Documentation (PR #2)  │     │  - Live Demo Scripting    │
│  - Pipeline Design        │     │  - Custom ComboBox Cells  │     │  - Technical Defense      │
└───────────────────────────┘     └───────────────────────────┘     └───────────────────────────┘
```

The presentation challenged us to translate thousands of lines of Java code and rigorous linear algebra into a concise, visually compelling narrative for peers and faculty.

---

## Slide Deck (PPT) Architecture: Translating Code to Presentation

Designing an effective technical presentation requires striking a balance between high-level architectural motivation and low-level mathematical rigor. We structured our slide deck into eight cohesive sections:

```text
Slide Deck Architecture
├── Slide 1: Title & Team Overview
├── Slide 2: Motivation & Problem Statement
├── Slide 3: Mathematical Formulation (Ax = B & Cramer's Rule)
├── Slide 4: Triangle Verification (Shoelace Formula & Orientation)
├── Slide 5: System Architecture & Layered Decoupling
├── Slide 6: JavaFX UI Engineering & Custom Cell Factories
├── Slide 7: Viewport Mapping & Coordinate Transformation
├── Slide 8: Live Demonstration & Preset Walkthrough
├── Slide 9: Automated Testing & Edge-Case Resiliency
└── Slide 10: Retrospective & Future Roadmap
```

### Detailed Slide Breakdown

| Slide # | Slide Title | Core Message / Content | Key Visual / Artifact |
|---|---|---|---|
| **1** | **TriangleFX** | Project introduction, team member roles, and GitHub repository link. | Application hero screenshot showing dark-themed UI. |
| **2** | **Motivation & Scope** | Bridging analytical geometry with interactive desktop visualization; avoiding brittle slope-intercept assumptions. | Problem statement summary from `docs/PROBLEM_STATEMENT.md`. |
| **3** | **Linear Systems: $Ax = B$** | Formulating 3 lines in 2D space as an overdetermined system; evaluating pairwise determinants ($D = a_1 b_2 - a_2 b_1$). | Matrix equation diagram and Cramer's rule derivation. |
| **4** | **Triangle Verification** | Why 3 intersections are not enough; detecting concurrency and collinearity via the Shoelace formula ($|D_{\text{area}}| / 2 > \epsilon$). | Side-by-side diagrams of valid triangle vs. collinear collapse. |
| **5** | **Layered Architecture** | Clean decoupling of `model`, `parser`, `service`, and `view` packages to ensure testability without JavaFX runtime. | ASCII architectural pipeline diagram. |
| **6** | **UI Refinements & UX** | Streamlining user input; why removing the raw matrix paste panel improved clarity; styling custom `ListCell`s in `ComboBox`. | Before/after UI comparison screenshots. |
| **7** | **Coordinate Transformation** | Inverting the JavaFX $Y$-axis (top-left origin) to Cartesian math space; auto-fitting bounding boxes with uniform aspect ratios. | Math coordinate system vs. Screen pixel space diagram. |
| **8** | **Demonstration Suite** | Guided walkthrough showing pre-configured presets (Right, Equilateral, Parallel, Collinear). | Table of test scenarios with expected outcomes. |
| **9** | **Testing & Resiliency** | Unit testing matrix covering positive geometries and pathological edge cases using JUnit 5. | Test execution report table (`mvn test`). |
| **10** | **Conclusion & Roadmap** | Summary of deliverables, commit history (13 commits, PR #2), and future roadmap items. | Roadmap milestones (symbolic parsing, SVG export). |

---

## Visualizing the Mathematical Pipeline for an Audience

A primary focus during slide preparation was designing intuitive graphics to explain how abstract user inputs travel through mathematical transformations to emerge as rendered pixels.

```text
User Input / Preset
        │
        ▼
Matrix Representation: A · x = B
  [ a11  a12 ] [ x ]   [ b1 ]
  [ a21  a22 ] [ y ] = [ b2 ]
  [ a31  a32 ]         [ b3 ]
        │
        ▼
Pairwise Determinants: D_ij = a_i1·a_j2 - a_j1·a_i2
  - If |D_ij| ≤ ε  ──► Flag Parallel / Coincident Lines
  - If |D_ij| > ε  ──► Unique Intersection P_ij via Cramer's Rule
        │
        ▼
Solve Three Vertices: P12, P23, P31
        │
        ▼
Shoelace Signed Area: D_area = x1(y2 - y3) + x2(y3 - y1) + x3(y1 - y2)
  - If |D_area| ≤ ε ──► Flag Collinear / Concurrent (Zero Area)
  - If |D_area| > ε ──► Valid Non-Degenerate Triangle
        │
        ▼
Coordinate Transformation:
  Screen X = pad + (x_world - x_min) · scale
  Screen Y = Height - pad - (y_world - y_min) · scale
        │
        ▼
JavaFX Canvas Rendering: Axes, Grid, Polygon, Vertices, and Labels
```

This visual flow became the central diagram of our slide deck, allowing any audience member—regardless of their background in Java or linear algebra—to instantly grasp the computational pipeline.

---

## Live Demonstration Script & Showcase Scenarios

In addition to preparing slides, we scripted our live software demonstration. A common pitfall in project presentations is entering random numbers on the fly, which risks unexpected inputs or awkward pauses.

We organized our demonstration around the pre-configured **Preset Scenarios** we built into the `ComboBox`, showcasing both expected functionality and robust error recovery:

```text
┌────────────────────────────────────────────────────────────────────────┐
│                      Live Demonstration Workflow                       │
├────────────────────────────────────────────────────────────────────────┤
│  Scenario 1: Classic Right Triangle (3-4-5)                            │
│  - Select Preset ──► Click "Generate Diagram"                          │
│  - Highlight: Clean 90° angle, integer coordinates, area = 6.00 sq u   │
│                                                                        │
│  Scenario 2: Equilateral Triangle                                      │
│  - Select Preset ──► Click "Generate Diagram"                          │
│  - Highlight: Precision with irrational coefficients (√3 approximations)│
│                                                                        │
│  Scenario 3: Parallel Lines Transversal (Pathological Case)            │
│  - Select Preset ──► Highlight: Determinant D12 = 0                    │
│  - Highlight: Descriptive alert banner: "Lines 1 and 2 are parallel"   │
│                                                                        │
│  Scenario 4: Concurrent Lines (Single Intersection Point)              │
│  - Select Preset ──► All three lines meet at (2.0, 2.0)                │
│  - Highlight: Area = 0.0, Shoelace rejects degenerate triangle         │
│                                                                        │
│  Scenario 5: Reset & Freeform Entry                                    │
│  - Click "Reset" ──► Verify complete canvas and field clearing         │
│  - Manually enter custom line equations and observe real-time scaling  │
└────────────────────────────────────────────────────────────────────────┘
```

### Demonstration Test Scenarios

| Scenario Name | Mathematical Configuration | Visual / Expected Result | Purpose in Demonstration |
|---|---|---|---|
| **Right Triangle** | $x = 0$, $y = 0$, $3x + 4y = 12$ | Triangle with vertices $(0,0), (4,0), (0,3)$; $\text{Area} = 6.0$ | Validates standard orthogonal geometry. |
| **Equilateral** | $y = 0$, $y = \sqrt{3}x$, $y = -\sqrt{3}x + 2\sqrt{3}$ | Equilateral triangle centered on canvas; equal sides | Proves floating-point precision stability. |
| **Parallel Lines** | $x + y = 2$, $x + y = 6$, $2x - y = 1$ | Error banner: *Lines 1 & 2 are parallel* | Demonstrates graceful rejection without UI crash. |
| **Concurrent Lines** | $x + y = 4$, $x - y = 0$, $2x + y = 6$ | Error banner: *Lines meet at single point (2, 2)* | Highlights two-tier validation beyond pairwise solving. |
| **Collinear Points** | $x - y = 0$, $2x - 2y = 0$, $3x - 3y = 0$ | Error banner: *Coincident lines detected* | Confirms rank deficiency detection. |

---

## Technical Defense: Addressing Architectural Trade-offs

During slide deck preparation, our group anticipated technical questions likely to be asked by instructors and peers. We documented clear engineering justifications for our design choices:

### 1. Why Standard Form ($ax + by = c$) Over Slope-Intercept ($y = mx + c$)?
- **Slope-Intercept Limitation:** Cannot represent vertical lines ($x = k$) because slope $m = \frac{\Delta y}{\Delta x} \to \infty$, causing division by zero.
- **Standard Form Advantage:** Seamlessly represents vertical lines ($1x + 0y = k$) and horizontal lines ($0x + 1y = k$) without special branching or infinite values. Directly maps to linear systems $Ax = B$.

### 2. Why Floating-Point Epsilon Tolerance ($\epsilon = 10^{-9}$)?
- **The Problem:** In IEEE 754 arithmetic, operations like `0.1 + 0.2 == 0.3` evaluate to `false` due to binary precision limits.
- **Our Solution:** Every equality comparison in TriangleFX evaluates $|a - b| \leq \epsilon$. This prevents false negatives where collinear points or parallel lines fail detection due to minute floating-point drift.

### 3. Why Remove the Raw Matrix Paste Panel?
- **The Trade-off:** While pasting raw numbers sounds convenient, in practice it created redundant input paths, ambiguous precedence, and confusing state transitions.
- **The Justification:** Removing it reduced cognitive overhead, cut dead code, and focused the user on two clean, unambiguous options: the structured grid or pre-configured presets.

### 4. How Does Coordinate Inversion Work?
- **The Conflict:** Cartesian coordinates place $+Y$ upward with the origin $(0, 0)$ at the center, whereas JavaFX `Canvas` places $(0, 0)$ at the top-left with $+Y$ pointing downward.
- **The Solution:** The `CoordinateTransform` service inverts the $Y$-axis:
  $$Y_{\text{screen}} = \text{Height} - \text{padding} - (Y_{\text{world}} - Y_{\min}) \cdot \text{scale}$$
  This ensures mathematical shapes render right-side up with automated scaling to fit any viewport.

---

## Retrospective on the Development Lifecycle

Reflecting on the trajectory of TriangleFX from our initial discussions on September 15 through today's slide presentation highlighted the value of disciplined software engineering:

```text
Timeline of Engineering Milestones
├── Sept 15: Mathematical Foundations (Group 1 Project Presentation)
│   └── Established Cramer's rule, Shoelace formula, and 3x2 matrix systems
├── Sept 17: Architectural Specification (TriangleFX Inception)
│   └── Authored docs/PROBLEM_STATEMENT.md; designed string parsers & viewport math
├── Sept 29: Implementation Refinement & Collaboration
│   └── Styled custom ListCell ComboBox; removed UI clutter; authored commit 779c8aa; merged PR #2
└── Oct 01: Final Presentation & Demonstration
    └── Assembled 10-slide technical PPT; scripted live demo; prepared architectural defense
```

By maintaining modular separation between the mathematical domain (`GeometryService`, `Line`, `Point`, `Triangle`) and the presentation layer (`TriangleApp`, `TriangleCanvas`), we were able to:
- Test all geometric algorithms in isolation via JUnit without spinning up JavaFX.
- Iterate on the UI styling and dark theme without breaking mathematical invariants.
- Present a cohesive story linking mathematical theory, clean code architecture, and practical usability.

---

## Key Takeaways

1. **Presentation is an Engineering Deliverable:** Writing functional code is only half the battle; communicating architecture, design decisions, and mathematical principles through structured presentation slides is essential for professional engineering.
2. **Scripting Demonstrations Prevents Surprises:** Designing a clear, preset-driven demonstration script ensures all positive capabilities and edge cases (parallel lines, concurrency, collinearity) are showcased reliably.
3. **Anticipating Technical Questions Strengthens Design:** Actively documenting justifications for key trade-offs (standard form vs. slope-intercept, floating-point $\epsilon$, UI panel removal) reinforces confidence during project defense.
4. **Visual Pipelines Clarify Complexity:** Abstract mathematical transformations (matrix multiplication, determinants, coordinate transformations) become instantly understandable when visualized as an end-to-end data flow diagram.
5. **Decoupled Architecture Pays Off:** Separating business logic from UI controls not only enables headless unit testing but also makes describing the system architecture during a presentation straightforward and clean.
6. **Iterative Refinement Yields Quality:** Tracing TriangleFX from initial mathematical prototypes on September 15 to a polished deliverable with full documentation and presentation slides demonstrates the power of steady, iterative software development.
