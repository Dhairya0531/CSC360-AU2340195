# Class Reflection — November 12, 2026

## Topics Covered

- Interactive computer vision interfaces: Designing responsive image inspection workstations in Java Swing (`DetectorWorkspace`, `ImagePanel`)
- Affine viewport transformations: Implementing smooth mouse-wheel zoom-to-cursor and drag-to-pan canvas navigation
- Synchronized visual-tabular data binding: Bidirectional navigation between `CrackRegion` particle metrics and canvas bounding-box overlays
- Dual-mode visualization: Seamless switching between high-visibility RGB overlay (`0xFF00AA` magenta candidates, red boundary tags) and 1-bit binary raster masks (`TYPE_BYTE_BINARY`)
- Quantitative morphometry and defect metrics: Deriving bounding-box span ($\sqrt{W^2 + H^2}$), estimated crack thickness ($A / L$), and directional orientation
- Transactional multi-format persistence: Building an atomic export engine (`ReportExporter`) generating `overlay.png`, `mask.png`, `regions.csv`, and schema-versioned `report.json` with rollback guarantees
- Headless CLI architecture: Designing robust automated batch screening (`CommandLine.java`) with explicit Unix exit codes (0, 1, 2)
- Quality assurance and test automation: Headless JUnit 5 regression test suites, synthetic ground-truth assets (`wall-clean.png`, `wall-cracked.png`), and JaCoCo coverage compliance

---

## Overview: Elevating the Wall Crack Detector into an Interactive Inspection Workstation

Today's class marked a critical milestone in Group 1's semester project: transitioning the **Wall Crack Detector** from a raw algorithmic prototype into a complete, production-grade visual inspection and reporting workstation. 

During our previous session on November 5, we established the core computer vision engine inside `CrackDetector.java`, integrating ImageJ 1.54p to handle grayscale luminance weighting (CCIR-601), rolling-ball illumination correction, Triangle/Otsu thresholding, and morphological gap healing. However, an algorithm is only as effective as the interface that exposes it. For structural engineers and building inspectors, raw pixel arrays and command-line logs are insufficient; they require intuitive visual tools to scrutinize defects at sub-pixel resolution, verify candidate validity, and generate auditable engineering reports.

```text
┌────────────────────────────────┐     ┌────────────────────────────────┐     ┌────────────────────────────────┐
│   Core Analysis Engine (Nov 5) │     │  Interactive Desktop Workspace │     │   Multi-Format Audit Engine    │
│  - Grayscale & Rolling-Ball    │ ──► │  - Affine Zoom/Pan Viewport    │ ──► │  - Transactional Exporter      │
│  - Triangle/Otsu Binarization  │     │  - Dual-Mode (Overlay vs Mask) │     │  - CSV Tabular Metrics         │
│  - Morphological Closing (D+E) │     │  - Synchronized Row-to-Region  │     │  - Schema v3 JSON Audit Trail  │
│  - Particle Shape Filtering    │     │  - Real-Time Parameter Tuning  │     │  - Headless CLI Batch Pipeline │
└────────────────────────────────┘     └────────────────────────────────┘     └────────────────────────────────┘
```

The canonical group repository is hosted at [Devang280904/CSC360-Group-01](https://github.com/Devang280904/CSC360-Group-01). Today, we focused on the architecture of our interactive desktop suite (`ui`), the mathematics of coordinate viewport mapping, bidirectional visual-tabular synchronization, and our transactional reporting pipeline.

---

## Group Project Progress: New Architectural Modules

Over the past week, our codebase expanded significantly, transitioning from backend algorithmic prototypes to an integrated desktop application. The repository structure now reflects a mature separation of concerns across presentation, I/O, domain models, and CLI automation:

```text
CSC360-Group-01/
├── pom.xml                                  # Java 17, ImageJ 1.54p, Shade fat-JAR, JaCoCo (>45% check)
├── README.md                                # Comprehensive user guide, screenshots, CLI examples
├── scripts/                                 # Automation tools (build.sh, test.sh, javadoc.sh)
└── src/
    ├── main/
    │   ├── java/org/wallcrack/
    │   │   ├── app/Main.java                # Universal entry router (Desktop GUI vs. Headless CLI)
    │   │   ├── cli/CommandLine.java         # Headless batch terminal runner with explicit exit codes
    │   │   ├── detection/CrackDetector.java # ImageJ computer vision pipeline (7-step detection engine)
    │   │   ├── io/                          # Defensive I/O, rendering, and export services
    │   │   │   ├── ImageFiles.java          # Format verification and safe image ingestion
    │   │   │   ├── ReportExporter.java      # Atomic multi-file export engine with rollback safety
    │   │   │   └── ResultImages.java        # High-visibility RGB overlays and 1-bit binary mask generation
    │   │   ├── model/                       # Immutable domain models and records
    │   │   │   ├── CrackRegion.java         # Geometric metrics (span, thickness, circularity, orientation)
    │   │   │   ├── DetectionConfig.java     # Strongly typed, validated analysis parameter record
    │   │   │   └── DetectionResult.java     # Immutable analysis snapshot with boolean candidate mask
    │   │   └── ui/                          # Custom Java Swing desktop interface
    │   │       ├── DesktopTheme.java        # 34 KB design system: dark palette, typography, borders
    │   │       ├── DetectorFrame.java       # Main application window, menu bar, drag-and-drop
    │   │       ├── DetectorWorkspace.java   # 59 KB split-pane layout: controls, table, view modes
    │   │       └── ImagePanel.java          # Affine transformation canvas: zoom-to-cursor & drag-pan
    │   └── resources/samples/               # Bundled sample images (wall-clean.png, wall-cracked.png)
    └── test/java/org/wallcrack/             # Comprehensive headless JUnit 5 regression test suite
```

### Summary of Newly Delivered Capabilities

| Component / File | Code Size | Architectural Purpose & Newly Added Capabilities |
|---|---|---|
| **`ui/DetectorWorkspace.java`** | 59 KB | The central nerve center of the UI. Integrates real-time parameter controls (sliders, combo boxes), synchronous detection workers, the candidate regions table, and export action triggers. |
| **`ui/ImagePanel.java`** | 19 KB | Custom high-performance canvas implementing affine transformations ($X' = sX + t_x$). Supports smooth mouse-wheel zooming centered on the mouse pointer, drag-panning, and sub-pixel candidate rendering. |
| **`ui/DesktopTheme.java`** | 34 KB | Unified design system providing a clean dark palette (`#1E1E24`), custom rounded buttons, flat scrollbars, accessible contrast ratios, and typography tailored for dense engineering data. |
| **`io/ReportExporter.java`** | 4.5 KB | Atomic export engine. Writes `overlay.png`, `mask.png`, `regions.csv`, and `report.json` simultaneously, with automatic rollback cleanup if any write fails. |
| **`io/ResultImages.java`** | 2.5 KB | Renders visual overlays using high-contrast magenta (`0xFF00AA`) for candidate pixels, red bounding boxes (`#DC1E2D`), and compact 1-bit binary rasters (`TYPE_BYTE_BINARY`). |
| **`cli/CommandLine.java`** | 6.5 KB | Full POSIX-style CLI engine. Parses command-line arguments (`--input`, `--output`, `--method`, `--sensitivity`), executes headless analyses, and returns explicit Unix exit codes (`0`, `1`, `2`). |

---

## Viewport Engineering: Affine Transformations and Zoom-to-Cursor

### The Coordinate Challenge

In structural crack inspection, users must inspect fine hairline fractures that span only 1 to 3 pixels in width within an image that may measure $1600 \times 1200$ pixels or higher. Standard Swing components like `JScrollPane` are ill-suited for this task: they introduce visual scrollbars, scroll clumsily, and fail to keep the cursor centered over the feature of interest during zooming.

To solve this, `ImagePanel.java` replaces standard scrollpanes with an **Affine Transformation Viewport** operating directly on Cartesian screen-to-image space:

```text
Screen Coordinate Space vs. Analysis Image Space

Screen Viewport (W_screen x H_screen)            Analysis Image Buffer (W_img x H_img)
┌───────────────────────────────────┐            ┌──────────────────────────────────┐
│                                   │            │ (0, 0)                           │
│           Mouse Cursor            │            │   ┌────────┐                     │
│                (Xm, Ym)           │  Mapping   │   │ Crack  │                     │
│                   •               │ ─────────► │   │ Region │                     │
│                                   │            │   └────────┘                     │
│                                   │            │                          (W, H)  │
└───────────────────────────────────┘            └──────────────────────────────────┘
```

---

### Mathematical Derivation of Zoom-to-Cursor

When a user rotates the mouse wheel over a specific structural flaw at screen coordinate $(X_m, Y_m)$, the world-space pixel coordinate $(X_w, Y_w)$ directly beneath the cursor must remain perfectly stationary on the screen after the zoom operation completes.

Let:
- $(X_{\text{offset}}, Y_{\text{offset}})$ be the current pan translation offsets.
- $s$ be the current scale factor.
- $s'$ be the new scale factor ($s' = s \cdot \text{zoomFactor}$).

The world coordinate $(X_w, Y_w)$ under the mouse cursor prior to zooming is:

$$X_w = \frac{X_m - X_{\text{offset}}}{s}, \quad Y_w = \frac{Y_m - Y_{\text{offset}}}{s}$$

After scaling to $s'$, we require that the same world coordinate $(X_w, Y_w)$ maps back to the exact same screen coordinate $(X_m, Y_m)$:

$$X_m = X_{\text{offset}}' + s' \cdot X_w$$

Substituting $X_w$ into this equation:

$$X_m = X_{\text{offset}}' + s' \cdot \left( \frac{X_m - X_{\text{offset}}}{s} \right)$$

Solving for the new translation offset $X_{\text{offset}}'$:

$$X_{\text{offset}}' = X_m - (X_m - X_{\text{offset}}) \cdot \frac{s'}{s}$$

$$Y_{\text{offset}}' = Y_m - (Y_m - Y_{\text{offset}}) \cdot \frac{s'}{s}$$

By updating the translation offsets using these exact formulas, `ImagePanel` ensures the image scales smoothly toward or away from the cursor position without any disorienting coordinate jumping.

---

### Drag-to-Pan Translation Mechanics

Panning is implemented by tracking relative mouse displacements between successive drag events:

$$\Delta X = X_{\text{current}} - X_{\text{previous}}, \quad \Delta Y = Y_{\text{current}} - Y_{\text{previous}}$$

$$X_{\text{offset}} \leftarrow X_{\text{offset}} + \Delta X, \quad Y_{\text{offset}} \leftarrow Y_{\text{offset}} + \Delta Y$$

During rendering, the entire transformation is encapsulated into an `AffineTransform` and executed via hardware-accelerated Java2D pipelines:

```java
Graphics2D g2d = (Graphics2D) g.create();
g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
g2d.translate(xOffset, yOffset);
g2d.scale(scale, scale);
g2d.drawImage(currentImage, 0, 0, null);
```

---

## Dual-Mode Visualization and Two-Way Synchronized Data Binding

### 1. Dual-Mode Visualization Engine (`ResultImages.java`)

To accommodate both contextual engineering review and automated binary mask verification, the application provides two visual display modes:

```text
1. Highlighted Overlay View (RGB Composite)     2. Binary Mask View (1-Bit TYPE_BYTE_BINARY)
┌─────────────────────────────────────────┐    ┌─────────────────────────────────────────┐
│ Original wall photo preserved in RGB    │    │ Background: Solid Black (0x000000)      │
│ Candidate pixels: Magenta (0xFF00AA)    │    │ Candidate pixels: Pure White (0xFFFFFF) │
│ Bounding box: Red border (#DC1E2D)      │    │ Clean, high-contrast silhouette         │
│ Identifier tag: "#1", "#2", ...         │    │ Ideal for IoU & segmentation metrics    │
└─────────────────────────────────────────┘    └─────────────────────────────────────────┘
```

- **Overlay View:** Retains the original photographic context while tinting accepted candidate pixels in bright magenta (`0xFF00AA`). A `BasicStroke(1.5f)` red rectangle outlines each region's bounding box, labeled with its unique region ID (`#1`, `#2`, etc.).
- **Binary Mask View:** Generates a compact `BufferedImage.TYPE_BYTE_BINARY` raster where foreground candidate pixels are pure white (`0xFFFFFF`) on a pure black (`0x000000`) background. This view serves as ground truth for computing Intersection-over-Union (IoU) and surface coverage percentages.

---

### 2. Bidirectional Visual-Tabular Synchronization

A standout UX feature implemented in `DetectorWorkspace.java` is **bidirectional synchronized data binding** between the candidate regions table and the image canvas:

```text
Table Row Selection ────────────────────────────────────────► Canvas Viewport Navigation
  User clicks row for Region #3                              1. Computes center: Cx = x + w/2, Cy = y + h/2
  (Area: 482 px, Thickness: 3.1 px)                          2. Calculates optimal zoom to frame bounding box
                                                             3. Smoothly centers viewport on Region #3
                                                             4. Highlights Region #3 border in gold

Canvas Click Detection ─────────────────────────────────────► Table Selection
  User clicks on a crack in the image                        1. Iterates candidate bounding boxes
  (Coordinate: x = 412, y = 188)                             2. Evaluates point-in-box: x ∈ [rx, rx+rw]
                                                             3. Finds matching region: Region #3
                                                             4. Automatically scrolls table and selects row!
```

#### Center-and-Zoom Formula
When a row is selected in the table, the viewport smoothly navigates to frame the target candidate region:

$$C_x = X_{\text{region}} + \frac{W_{\text{region}}}{2}, \quad C_y = Y_{\text{region}} + \frac{H_{\text{region}}}{2}$$

The target translation offset places this center directly at the middle of the screen viewport:

$$X_{\text{offset}} = \frac{W_{\text{screen}}}{2} - C_x \cdot s_{\text{target}}$$

$$Y_{\text{offset}} = \frac{H_{\text{screen}}}{2} - C_y \cdot s_{\text{target}}$$

This tight coordination eliminates manual hunting across large images, allowing inspectors to audit dozens of identified candidate regions within seconds.

---

## Quantitative Morphometry and Reporting Architecture

### Physical Crack Descriptors (`CrackRegion.java`)

Beyond binary segmentation, structural evaluation requires quantitative geometric data. In `CrackRegion.java`, our data model records and derives five key morphological metrics:

```text
CrackRegion Geometric Descriptors
┌───────────────────────────┬────────────────────────────────────────────────────────┐
│ Metric                    │ Mathematical Formulation / Definition                  │
├───────────────────────────┼────────────────────────────────────────────────────────┤
│ Bounding Box Dimensions   │ (x, y) origin, width (w), height (h) in pixels         │
│ Pixel Area (A)            │ Total count of connected candidate pixels              │
│ Bounding Box Span (L)     │ Diagonal length: L = √(w² + h²)                        │
│ Estimated Thickness (T)   │ Area divided by span: T_est = A / L                    │
│ Circularity Index (C)     │ Compactness: C = 4π · (Area / Perimeter²)              │
│ Orientation Category      │ Predominant axis: Horizontal, Vertical, or Diagonal    │
└───────────────────────────┴────────────────────────────────────────────────────────┘
```

- **Bounding Box Span ($L$):** Measures the maximum Euclidean diagonal span of the candidate's bounding box:
  $$L = \sqrt{W^2 + H^2}$$
- **Estimated Thickness ($T_{\text{est}}$):** Approximates the average physical width of the fissure in pixels by dividing the total pixel area by its diagonal span:
  $$T_{\text{est}} = \frac{\text{Area}}{L}$$
  For long, narrow cracks, this metric closely tracks true transverse width, distinguishing fine hairline cracks ($< 2\text{ px}$) from severe structural fractures ($> 10\text{ px}$).
- **Orientation Classification:** Categorizes the structural failure mode:
  $$\text{Orientation} = \begin{cases} \text{Horizontal}, & \text{if } W \ge 2H \\ \text{Vertical}, & \text{if } H \ge 2W \\ \text{Diagonal}, & \text{otherwise} \end{cases}$$

---

### Transactional Multi-Format Persistence (`ReportExporter.java`)

Engineering inspection data must be permanently archivable and auditable. `ReportExporter.java` writes four synchronized output artifacts to an export directory:

1. **`overlay.png`:** Full-resolution RGB image with highlighted candidate pixels and red bounding boxes.
2. **`mask.png`:** 1-bit high-contrast black-and-white binary mask.
3. **`regions.csv`:** Comma-separated spreadsheet listing all candidate regions with bounding boxes, area, span, circularity, thickness, and orientation.
4. **`report.json`:** Machine-readable JSON document conforming to Schema Version 3, capturing analysis metadata, original dimensions, effective threshold, and candidate statistics.

```text
Transactional Export Safety Pattern

               Trigger: Export Button Clicked / CLI Execution
                                     │
                                     ▼
        ┌──────────────────────────────────────────────────────────┐
        │ Pre-Flight Collision Check:                              │
        │ Verify none of the 4 target files exist in destination.  │
        └──────────────────────────────────────────────────────────┘
                         │                     │
                     [Exists]              [Clean]
                         │                     │
                         ▼                     ▼
               Throw IOException      Attempt Sequential Write:
             "Output already exists"  1. writePng(overlay.png)
                                      2. writePng(mask.png)
                                      3. writeString(regions.csv)
                                      4. writeString(report.json)
                                               │
                                      ┌────────┴────────┐
                                      │                 │
                                  [Success]         [Failure]
                                      │                 │
                                      ▼                 ▼
                              Export Completed   Atomic Rollback:
                                                 Delete all files created
                                                 during this invocation!
```

To guarantee that disk archives never contain partial or corrupted runs, `ReportExporter` enforces **transactional rollback semantics**. If an error occurs while writing the fourth file (e.g., out of disk space while writing `report.json`), an active `catch` block iterates over all files generated during that invocation and immediately deletes them via `Files.deleteIfExists(path)`.

---

## Headless CLI and Automated Testing Suite

### Headless Batch Execution (`CommandLine.java`)

To enable automated pipeline integration, bulk image processing, and server-side evaluation, the application provides a headless CLI entry point:

```bash
# Execute batch detection without spinning up Swing GUI
java -jar build/wall-crack-detector.jar \
  --input src/main/resources/samples/wall-cracked.png \
  --output output/run-01 \
  --method Triangle \
  --sensitivity 5 \
  --min-area 25 \
  --max-circularity 0.45 \
  --correct-lighting true \
  --closing 2
```

#### Explicit POSIX Exit Codes
The CLI adheres to strict Unix process standards:
- **`Exit Code 0`:** Analysis succeeded cleanly, or `--help` was requested.
- **`Exit Code 1`:** File I/O failure (input file not found, destination folder unwritable, or file collision).
- **`Exit Code 2`:** Invalid parameter input (unrecognized flag, number out of bounds, malformed option).

---

### Automated Regression Testing and JaCoCo Quality Gates

In our `pom.xml`, automated quality assurance is enforced through the **Maven Surefire Plugin** and the **JaCoCo Maven Plugin**:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.16</version>
    <executions>
        <execution>
            <id>coverage-report</id>
            <phase>verify</phase>
            <goals><goal>report</goal><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.45</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- **Headless Testing Mode:** Surefire executes unit tests with `-Djava.awt.headless=true` to prevent test failures on headless CI/CD Linux build servers lacking display managers.
- **Synthetic Test Imagery:** Bundled under `src/main/resources/samples/`:
  - `wall-clean.png`: Validates that pristine, uncracked wall surfaces produce exactly zero candidate regions (0 false positives).
  - `wall-cracked.png`: Verifies that true structural cracks are detected, bounded, and quantified within expected pixel ranges.
- **Coverage Gate:** Every `mvn verify` run enforces a minimum of **$45\%$ line coverage** across the codebase, failing the build if coverage slips.

---

## Technical Defense: Addressing Key Engineering Trade-offs

During our project architecture review, our group defended several deliberate design choices:

### 1. Why Build a Custom Viewport Canvas Over Standard `JScrollPane`?
- **The Issue:** `JScrollPane` handles panning by shifting viewport scrollbars, but it lacks built-in support for zoom-to-cursor mechanics and requires awkward coordinate conversions.
- **The Justification:** Authoring `ImagePanel` with custom `AffineTransform` logic allowed us to implement smooth, centered zoom-to-cursor scaling, middle-click and right-click panning, and hardware-accelerated bilinear interpolation, providing a professional CAD-like inspection experience.

### 2. Why Enforce Transactional Atomic Export with Rollbacks?
- **The Issue:** Writing four files sequentially risks leaving incomplete export folders if disk write errors or exceptions interrupt the process.
- **The Justification:** Structural inspection records are legal and engineering documents. An orphan `regions.csv` without its corresponding `overlay.png` or metadata `report.json` could lead to misinterpretation. Transactional rollbacks ensure that an export directory contains either all four verified artifacts or nothing at all.

### 3. Why Export Both an RGB Overlay and a 1-Bit Binary Mask?
- **The Distinction:** An RGB overlay is tailored for human inspection, allowing engineers to verify whether marked pixels correspond to physical fissures or harmless paint shadows.
- **The Utility of the Mask:** A 1-bit binary mask (`TYPE_BYTE_BINARY`) is essential for algorithmic validation. It consumes minimal disk space (1 bit per pixel) and allows automated benchmark scripts to compute exact Intersection-over-Union (IoU) scores against ground-truth segmentations.

### 4. Why Maintain Dual Desktop GUI and Headless CLI Modes?
- **The Trade-off:** Supporting both graphical and headless execution requires maintaining two separate entry paths.
- **The Justification:** Decoupling the UI from `CrackDetector` and `CommandLine` enables dual utility: civil engineers can interactively examine photos in the desktop application, while automated server scripts can run thousands of inspection photos overnight in headless batch mode.

---

## Retrospective: Synthesis of the CSC360 Engineering Lifecycle

Reflecting on the evolution of our work across the semester highlights how disparate computer graphics concepts weave into a cohesive engineering skillset:

```text
Semester Engineering Timeline
├── Phase 1: Analytical Vector Geometry (TriangleFX)
│   ├── September 15: Mathematical formulations (Ax = B, Cramer's Rule, Shoelace formula)
│   ├── September 17 - 29: Problem statement specs, UI refinement (PR #2), commit 779c8aa
│   └── October 01: Final presentation deck, live demo scripting, and mathematical defense
├── Phase 2: Theoretical Raster Foundations (Image Processing)
│   ├── October 29: Perceptual contrast, discrete intensity distributions, and convolution
│   └── November 03: Non-linear gamma curves, CLAHE, and separable Gaussian filter design
└── Phase 3: Applied Computer Vision System (Wall Crack Detector)
    ├── November 04 - 05: Core pipeline in CrackDetector.java (CCIR-601, rolling-ball, Triangle binarization)
    └── November 12: Desktop inspection workspace, affine zoom/pan, synchronized tables, and transactional reporting
```

By connecting pure linear algebra in Phase 1 with statistical signal processing in Phase 2 and production desktop engineering in Phase 3, we have developed a comprehensive foundation spanning both mathematical theory and production software architecture.

---

## Key Takeaways

1. **User Experience is an Algorithmic Enabler:** High-accuracy computer vision models are only as effective as the tools used to review them. Implementing an affine zoom-to-cursor viewport and synchronized visual-tabular binding enables inspectors to audit dozens of candidate regions effortlessly.
2. **Coordinate Preservation Requires Precise Mathematics:** Implementing smooth zoom-to-cursor requires recalculating translation offsets ($X' = X_m - (X_m - X) \cdot \frac{s'}{s}$) to keep the world-space pixel beneath the mouse stationary during scaling.
3. **Data Descriptors Transform Pixels into Engineering Insights:** Moving beyond raw binary masks to compute physical shape metrics—such as bounding box span ($L = \sqrt{W^2 + H^2}$), estimated thickness ($T = A/L$), and orientation—translates low-level pixels into actionable civil engineering data.
4. **Data Integrity Demands Transactional Export Pipelines:** Multi-file reporting bundles (`overlay.png`, `mask.png`, `regions.csv`, `report.json`) must be written atomically. Enforcing pre-flight collision checks and rollback cleanups prevents corrupted, partial archives.
5. **Decoupled Architecture Enables Headless Automation:** Separating core detection logic from Swing UI controls enables seamless dual-mode execution, allowing the same JAR to run interactively on desktop workstations or headlessly in CI/CD batch pipelines.
6. **Automated Quality Gates Ensure System Resiliency:** Combining headless JUnit 5 tests with synthetic ground-truth assets (`wall-clean.png` vs. `wall-cracked.png`) and JaCoCo coverage rules guarantees that pipeline enhancements do not introduce regressions.
