# Class Reflection — November 5, 2026

## Topics Covered

- Bridging image processing theory with group project execution: Inception and architecture of the **Wall Crack Detector**
- Repository initialization and collaborative workflows: Setting up [Devang280904/CSC360-Group-01](https://github.com/Devang280904/CSC360-Group-01)
- Practical application of November 3 concepts: Implementing perceptual grayscale conversion, histogram thresholding, and morphological filtering
- Illumination gradient mitigation: Rolling-ball background subtraction (`BackgroundSubtracter`) to neutralize non-uniform wall lighting
- Histogram-driven automated binarization: Comparative evaluation of **Triangle** vs. **Otsu** algorithms for sparse linear defect segmentation
- Mathematical morphology in defect detection: Morphological closing ($A \bullet B = (A \oplus B) \ominus B$) via successive dilation and erosion passes to bridge hairline fractures
- Connected component analysis & geometric feature extraction: ImageJ `ParticleAnalyzer` filtering via Area ($> 30\text{ px}$) and Circularity ($4\pi \frac{\text{Area}}{\text{Perimeter}^2} < 0.6$)
- Software architecture & decoupled design: Pure Java 17 core, ImageJ 1.54p integration, dual execution modes (interactive Swing UI vs. headless CLI), and automated report exports (`overlay.png`, `mask.png`, `regions.csv`, `report.json`)

---

## Overview: Translating Digital Image Processing Theory into Project Execution

In today's class, our theoretical exploration of image processing from October 29 and November 3—specifically contrast dynamics, intensity distributions, and spatial neighborhood filtering—directly transitioned into active project development. Following the completion and defense of our first semester project (**TriangleFX**), Group 1 initiated our second major engineering endeavor: the **Wall Crack Detector**.

While **TriangleFX** focused on analytical 2D vector geometry, line intersection matrices ($Ax = B$), and coordinate space transformations, the **Wall Crack Detector** challenges us to solve an applied raster computer vision problem. Real-world civil infrastructure assessment requires identifying thin, low-contrast structural cracks on unevenly lit, textured concrete and plaster surfaces. 

```text
┌────────────────────────────────┐     ┌────────────────────────────────┐     ┌────────────────────────────────┐
│      Theory (Oct 29 - Nov 03)  │     │   Core Engine Implementation   │     │  Multi-Modal Delivery System   │
│  - Luminance & Weber Contrast  │ ──► │  - ImageJ 1.54p Integration    │ ──► │  - Desktop Swing GUI           │
│  - Histogram & CDF Analysis    │     │  - Rolling-Ball Subtraction    │     │  - Headless CLI Batch Mode     │
│  - Morphological Operations    │     │  - Triangle/Otsu Binarization  │     │  - Multi-Format Report Exporter│
│  - Spatial Convolution Kernels │     │  - Morphological Closing (D+E) │     │  - Zoom/Pan Inspection Canvas  │
└────────────────────────────────┘     └────────────────────────────────┘     └────────────────────────────────┘
```

The canonical group repository is established at [Devang280904/CSC360-Group-01](https://github.com/Devang280904/CSC360-Group-01). Today's discussion centered on dissecting the end-to-end detection pipeline within `CrackDetector.java`, analyzing how mathematical morphology and statistical thresholding solve defect isolation, and reviewing the architectural modularity of our Java 17 codebase.

---

## Group Project Evolution: Introducing the Wall Crack Detector

### Problem Motivation and Scope

Detecting structural flaws on building walls is critical for preventative maintenance and seismic safety screening. Manual visual inspection by civil engineers is labor-intensive, subjective, and prone to oversight when dealing with extensive surface areas. 

However, automating crack detection presents major image processing hurdles:
1. **Severe Illumination Gradients:** Indoor lighting and angled sunlight produce heavy vignetting and soft wall shadows that easily fool global thresholding.
2. **Surface Texture Noise:** Rough concrete, stipple plaster, and paint blemishes generate high-frequency textural noise that mimics crack pixels.
3. **Hairline Discontinuities:** Micro-cracks often register as broken, fragmented segments separated by narrow single-pixel gaps rather than continuous lines.
4. **Low Local Contrast:** Hairline cracks often differ from their surrounding plaster by only a few grayscale intensity levels.

Our engineering objective with the **Wall Crack Detector** is to build a reliable, high-performance Java desktop and CLI tool that performs preliminary screening on standard photographs (PNG/JPEG), isolates crack candidates, suppresses non-crack surface artifacts, and outputs quantitative geometric measurements.

---

### Codebase Organization and Modular Design

Following professional software engineering principles, the application decouples image analysis, data representation, input/output operations, and user presentation across clear package boundaries under `src/main/java/org/wallcrack/`:

```text
CSC360-Group-01/
├── pom.xml                                  # Maven build, Java 17 target, Shade plugin, JaCoCo
├── README.md                                # Full documentation, visual previews, CLI workflows
├── scripts/                                 # Automation scripts (build.sh, test.sh, javadoc.sh)
└── src/
    ├── main/
    │   ├── java/org/wallcrack/
    │   │   ├── app/                         # Application lifecycle and entry (Main.java)
    │   │   ├── cli/                         # Headless batch terminal engine (CommandLine.java)
    │   │   ├── detection/                   # Core computer vision pipeline (CrackDetector.java)
    │   │   ├── io/                          # Image loading, report exporting, and overlay rendering
    │   │   │   ├── ImageFiles.java
    │   │   │   ├── ReportExporter.java
    │   │   │   └── ResultImages.java
    │   │   ├── model/                       # Immutable domain models and configurations
    │   │   │   ├── CrackRegion.java
    │   │   │   ├── DetectionConfig.java
    │   │   │   └── DetectionResult.java
    │   │   └── ui/                          # Swing desktop GUI, canvas viewport, and design system
    │   │       ├── DesktopTheme.java
    │   │       ├── MainFrame.java
    │   │       ├── MainWorkspace.java
    │   │       └── ResultsPanel.java
    │   └── resources/samples/               # Bundled sample assets (wall-clean.png, wall-cracked.png)
    └── test/java/org/wallcrack/             # Comprehensive JUnit 5 test suite
```

### Architectural Responsibilities

| Package | Key Classes | Core Architectural Responsibility |
|---|---|---|
| **`app`** | `Main.java` | Universal bootstrap entry point; inspects CLI flags (`--input`, `--help`) to route execution to either the desktop Swing GUI or the headless CLI engine. |
| **`cli`** | `CommandLine.java` | Headless execution runner; parses batch parameters, executes detection without spinning up an AWT/Swing display, and writes artifacts to disk. |
| **`detection`** | `CrackDetector.java` | Pure computer vision processing engine; wraps ImageJ primitives (`ColorProcessor`, `BackgroundSubtracter`, `AutoThresholder`, `ParticleAnalyzer`) into an isolated, thread-safe analysis routine. |
| **`io`** | `ImageFiles.java`, `ReportExporter.java`, `ResultImages.java` | Defensive file I/O, format verification, composite overlay rendering (magenta markings, red bounding boxes), and multi-format reporting (`.csv`, `.json`, `.png`). |
| **`model`** | `CrackRegion.java`, `DetectionConfig.java`, `DetectionResult.java` | Immutable record-style data models holding bounding box dimensions, pixel area, span, circularity metrics, and analysis configuration snapshots. |
| **`ui`** | `MainFrame.java`, `MainWorkspace.java`, `ResultsPanel.java`, `DesktopTheme.java` | Modern desktop interface; features drag-and-drop file ingestion, real-time pan/zoom viewport canvas, interactive table-to-image synchronized inspection, and dark theme palette. |

---

## Inside CrackDetector.java: The 7-Step Image Processing Pipeline

The core technical milestone discussed today was the algorithmic implementation in `CrackDetector.java`. The class takes a raw `BufferedImage` along with a `DetectionConfig` and applies a deterministic multi-stage computer vision pipeline:

```text
Raw Wall Photograph (RGB / PNG / JPEG)
                 │
                 ▼
 ┌──────────────────────────────────────────────────┐
 │ Step 1: Input Normalization & Bilinear Scaling   │ ──► Enforces 40 MP boundary, scales down proportionally,
 └──────────────────────────────────────────────────┘     composites alpha transparency onto solid white.
                 │
                 ▼
 ┌──────────────────────────────────────────────────┐
 │ Step 2: Perceptual Grayscale Conversion          │ ──► CCIR-601 Luminance Weights:
 └──────────────────────────────────────────────────┘     Y = 0.299·R + 0.587·G + 0.114·B (Yields ByteProcessor)
                 │
                 ▼
 ┌──────────────────────────────────────────────────┐
 │ Step 3: Rolling-Ball Illumination Normalization  │ ──► BackgroundSubtracter (Radius = 50 px):
 └──────────────────────────────────────────────────┘     Removes low-frequency shadows, isolating dark features.
                 │
                 ▼
 ┌──────────────────────────────────────────────────┐
 │ Step 4: Histogram-Driven Auto-Thresholding       │ ──► Evaluates intensity histogram via Triangle / Otsu;
 └──────────────────────────────────────────────────┘     applies sensitivity offset; produces binary mask.
                 │
                 ▼
 ┌──────────────────────────────────────────────────┐
 │ Step 5: Morphological Closing (Gap Bridging)     │ ──► Successive Dilate passes bridge hairline fissures;
 └──────────────────────────────────────────────────┘     successive Erode passes restore true contour boundaries.
                 │
                 ▼
 ┌──────────────────────────────────────────────────┐
 │ Step 6: Particle Analysis & Geometric Filtering  │ ──► Connected-component labeling; filters out small noise
 └──────────────────────────────────────────────────┘     (Area < 30) and compact spots (Circularity > 0.6).
                 │
                 ▼
 ┌──────────────────────────────────────────────────┐
 │ Step 7: Packaging Immutable DetectionResult      │ ──► Extracts bounding boxes, span, circularity; packages
 └──────────────────────────────────────────────────┘     retained boolean mask and CrackRegion records.
```

---

## Detailed Mathematical and Algorithmic Analysis

### 1. Perceptual Grayscale Conversion (CCIR-601 Standard)

Color cameras capture light in three separate spectral bands: Red ($R$), Green ($G$), and Blue ($B$). However, crack detection relies on structural luminance rather than chromatic tint. Simply averaging the RGB channels ($\frac{R + G + B}{3}$) produces distorted results because the Human Visual System possesses vastly different spectral sensitivities across cone photoreceptors.

To mirror true perceptual luminance, `CrackDetector` configures ImageJ's `ColorProcessor` with standard **CCIR-601 luminance coefficients**:

$$Y = 0.299 \cdot R + 0.587 \cdot G + 0.114 \cdot B$$

- **Green ($58.7\%$):** Receives the highest weight because human photopic vision is centered around $555\text{ nm}$, making the eye most sensitive to green tonal shifts.
- **Red ($29.9\%$):** Provides intermediate structural weight.
- **Blue ($11.4\%$):** Receives minimal weight due to low human blue-cone density and high susceptibility to optical sensor noise.

This conversion outputs an 8-bit `ByteProcessor` where pixel values $r \in [0, 255]$ faithfully encode luminance.

---

### 2. Rolling-Ball Background Subtraction for Illumination Correction

Uneven illumination is the single greatest cause of false positives in automated inspection. A dark shadow in a corner can easily register a lower grayscale intensity than a brightly illuminated crack on the center of the wall.

To eliminate these macroscopic lighting gradients without destroying fine, sharp crack lines, `CrackDetector` integrates ImageJ's **Rolling-Ball Background Subtraction** (`BackgroundSubtracter` with radius $r = 50\text{ px}$):

```text
Rolling-Ball Algorithmic Mechanics (1D Cross-Section View)

Intensity
  ^
  |        .---.  <-- Gradual Lighting Gradient (Macro Shadow)
  |       /     \           |
  |      /   V   \          |  V = Crack Dip (Narrow, high-frequency)
  |     /         \         |
  |    /           \        v
  |   (   Ball      )  <-- Sphere of radius R rolls under the surface
  +---'---._____.---'---------------------------------------------------> x
```

- **Mechanism:** Imagine a 3D parabolic or spherical ball of radius $R$ rolling beneath the 3D intensity surface of the image.
- Because cracks are spatially narrow (typically 1 to 5 pixels wide), the large $50$-pixel ball cannot roll inside the narrow intensity depressions; it only touches the broader, gradual hills and valleys representing uneven lighting.
- The path traced by the ball defines the estimated background illumination $I_{\text{bg}}(x, y)$.
- Subtracting this background from the original image ($I_{\text{corrected}} = I_{\text{orig}} - I_{\text{bg}}$) flattens macroscopic gradients while keeping high-frequency dark crack fissures fully preserved.

---

### 3. Automated Thresholding: Triangle vs. Otsu Algorithm

Once illumination is normalized, the image histogram must be partitioned to separate dark crack candidates ($[0, T]$) from the brighter wall background ($[T + 1, 255]$).

Our implementation supports both **Triangle** and **Otsu** algorithms via `AutoThresholder`:

```text
Histogram Profiling: Bimodal (Otsu) vs. Long-Tailed Skewed (Triangle)

Otsu Scenario (Equal Bimodal Classes):       Triangle Scenario (Sparse Defects on Dominant Wall):
  Frequency                                   Frequency
    ^                                           ^
    |      Peak 1            Peak 2             |  Peak (Wall Background)
    |       .-.               .-.               |   |\
    |      /   \             /   \              |   | \
    |     /     \   Valley  /     \             |   |  \          Distance d (Maximized)
    |    /       \____.____/       \            |   |   \           . | .
    +---+-------------+-------------+->         |   |    \_________/  |  \_______ (Tail: Cracks)
        0             T            255          +---+-----------------+--------------+->
                                                    0                 T             255
```

#### Otsu's Method (Class Variance Maximization)
Otsu's algorithm exhaustively searches for an intensity threshold $T$ that minimizes intra-class variance, or equivalently, maximizes **inter-class variance** $\sigma_B^2(T)$:

$$\sigma_B^2(T) = \omega_0(T) \cdot \omega_1(T) \cdot \left[ \mu_0(T) - \mu_1(T) \right]^2$$

where $\omega_0, \omega_1$ are the probabilities of the two classes and $\mu_0, \mu_1$ are their respective means. While Otsu works exceptionally well on images with roughly equal foreground and background pixel populations, it can falter on wall photographs where crack pixels occupy less than $0.5\%$ of total image area, causing the threshold to drift into the background peak.

#### The Triangle Algorithm (Geometric Extremum)
To handle highly skewed, unimodal distributions where dark crack defects form a faint tail against a massive wall peak:
1. Construct a straight line on the histogram between the maximum histogram peak $(r_{\max}, h_{\max})$ and the lowest intensity extreme $(r_{\min}, h_{\min})$.
2. For every intensity bin $r$ between the peak and the minimum, calculate the orthogonal geometric distance $d(r)$ from the line to the histogram curve.
3. The threshold $T$ is selected at the point where distance $d(r)$ is maximized.

In practice, the **Triangle algorithm** proves vastly superior for isolating sparse, hairline dark fissures without picking up background concrete grain.

---

### 4. Morphological Closing ($A \bullet B$): Bridging Discontinuous Micro-Cracks

Real-world cracks rarely form unbroken continuous lines. Due to paint bridging, plaster textures, or optical sensor noise, hairline cracks frequently break into fragmented strings of disconnected pixels.

To bridge these micro-gaps without artificially inflating crack thickness, `CrackDetector` applies **morphological closing**:

$$A \bullet B = (A \oplus B) \ominus B$$

where $A$ represents the binary candidate mask and $B$ denotes a symmetric structuring element (kernel):

```text
Morphological Closing Pipeline (Dilation followed by Erosion)

1. Fragmented Crack (Input A):      [████]   [ ]   [██████]   [ ]   [██]
                                              ▲                ▲
                                              Hairline Discontinuities

2. Dilation (A ⊕ B):                [████████████████████████████████]
   (Expands foreground, merges gaps, but increases thickness)

3. Erosion ((A ⊕ B) ⊖ B):           [██████████████████████████████]
   (Shrinks boundaries back, restores original width, gap remains bridged!)
```

1. **Dilation ($A \oplus B$):** The structuring element slides across the binary mask. If any overlapping pixel touches a white candidate pixel, the target pixel is set to white. This grows candidate boundaries outwards, causing adjacent crack fragments to merge across small gaps.
2. **Erosion ($A \ominus B$):** The dilated image is then eroded by the same structuring element. A pixel remains white only if all pixels covered by the structuring element are white. This shrinks the boundaries back to their true physical dimensions while leaving the fused bridges permanently intact.

In `CrackDetector.java`, the closing radius is configurable (`closingRadius > 0`). Running $n$ passes of `binaryMask.dilate(1, 0)` followed by $n$ passes of `binaryMask.erode(1, 0)` ensures flexible gap healing tailored to the photographic resolution.

---

### 5. Connected Component Analysis and Circularity Filtering

After binarization and morphological closing, the binary mask contains both true cracks and non-crack artifacts (e.g., nail holes, paint bubbles, fly specks, stipple shadows).

To isolate authentic cracks, `CrackDetector` executes ImageJ's `ParticleAnalyzer` with two rigorous geometric constraints:

#### Minimum Area Threshold ($\text{Area} \ge 30\text{ px}$)
Filters out isolated high-frequency noise spikes and salt-and-pepper artifacts. Any contiguous region containing fewer than 30 pixels is automatically dropped.

#### Shape Descriptor: Circularity Metric
Cracks are physically characterized by high length-to-width aspect ratios and jagged, elongated perimeters. Conversely, surface blemishes, screw holes, and moisture spots tend to be compact or circular.

The dimensionless **Circularity ($C$)** metric is defined as:

$$C = 4\pi \cdot \frac{\text{Area}}{\text{Perimeter}^2}$$

- **Perfect Circle:** When an object is a perfect circle, $\text{Area} = \pi r^2$ and $\text{Perimeter} = 2\pi r$. Substituting yields:
  $$C = 4\pi \cdot \frac{\pi r^2}{(2\pi r)^2} = \frac{4\pi^2 r^2}{4\pi^2 r^2} = 1.0$$
- **Elongated Crack:** As an object becomes thinner, more jagged, and more elongated, its perimeter $P$ increases dramatically relative to its area $A$. Consequently, $P^2$ grows rapidly, driving circularity toward zero ($C \to 0.0$).

```text
Circularity Metric Comparison

Shape Profile:             Circle (Paint Bubble)      Ellipse (Blemish)       Linear Crack
Visual:                           ●                         ⬭                  \
Area (px):                       314                       314                  314
Perimeter (px):                   63                        80                  280
Circularity (C):                 1.00                      0.62                 0.05
ParticleAnalyzer Action:      REJECTED                  REJECTED              ACCEPTED
```

By enforcing `config.maxCircularity()` (default $0.6$), `ParticleAnalyzer` reliably rejects compact non-crack marks while accepting genuine linear fracture topologies.

---

## Technical Defense: Addressing Architectural Trade-offs

During our project design review, we identified and defended several key engineering trade-offs:

### 1. Why Integrate ImageJ Core (`ij:1.54p`) Over Raw Hand-Crafted Arrays?
- **The Alternative:** We could have implemented custom 2D integer loops for convolution, flood-fill labeling, and thresholding.
- **The Justification:** While educational, writing custom low-level image loops in Java is error-prone and often unoptimized. ImageJ 1.54p is a mature, battle-tested scientific library with decades of optimization across biomedical and material science domains. Leveraging ImageJ allowed our team to focus on domain-specific pipeline composition, shape filtering, and user experience rather than reinventing low-level raster buffers.

### 2. Why Triangle Thresholding Over Otsu as the Default?
- **The Conflict:** Otsu is widely recognized as the standard algorithm for automated thresholding in textbook image processing.
- **The Reality:** Otsu's mathematical foundation assumes a bimodal distribution with two distinct, roughly equal populations. In wall inspection, background plaster accounts for $99.5\%$ of all pixels, while cracks make up only $0.5\%$. Under these conditions, Otsu often sets the cutoff too high, misclassifying thousands of plaster texture pixels as cracks. The **Triangle algorithm** specifically identifies the transition from a dominant peak to an elongated tail, making it ideal for sparse defect detection.

### 3. Why Morphological Closing Instead of Gaussian Smoothing?
- **The Difference:** Gaussian smoothing applies a weighted linear moving average that blurs both noise and high-frequency boundaries, washing out faint hairline cracks.
- **The Advantage:** Morphological closing is a non-linear set-theoretic operation. It bridges spatial discontinuities between nearby crack segments without attenuating the sharp intensity gradient that defines the crack boundary.

### 4. Why Enforce a 40-Megapixel Upper Bound in `prepareImage`?
- **Memory Considerations:** High-resolution mobile phone photos often exceed 48 to 108 megapixels. Uncompressed in memory, a single 40 MP 32-bit ARGB image consumes $\approx 160\text{ MB}$. Creating multiple processing buffers (`ColorProcessor`, `ByteProcessor`, mask buffers, overlay layers) quickly pushes heap consumption beyond $1\text{ GB}$, risking `OutOfMemoryError` on consumer machines.
- **The Solution:** The detector calculates a proportional downscaling factor to cap maximum dimensions while applying high-quality bilinear interpolation (`RenderingHints.VALUE_INTERPOLATION_BILINEAR`), preserving crack topology while ensuring sub-second execution speeds.

---

## Retrospective: Tracing the Evolution Across CSC360 Projects

Comparing our work across the semester demonstrates a natural progression in software engineering maturity:

```text
Timeline of Engineering Milestones
├── Sept 15 - Oct 01: TriangleFX (Analytical Geometry & Linear Systems)
│   ├── Formulated Ax = B, Cramer's rule determinants, and Shoelace area verification
│   ├── Implemented custom JavaFX ListCell ComboBox presets and inverted Cartesian viewports
│   └── Defended mathematical edge cases (collinearity, concurrency, floating-point ε tolerances)
├── Oct 29 - Nov 03: Theoretical Image Processing Foundations
│   ├── Formalized Weber/Michelson contrast, discrete histogram PMF/CDF, and CLAHE
│   └── Analyzed 2D spatial convolution, separable Gaussian kernels, and rank-order median filters
└── Nov 04 - Nov 05: Wall Crack Detector (Applied Computer Vision & Raster Engineering)
    ├── Initialized Devang280904/CSC360-Group-01 with Maven, ImageJ 1.54p, and JaCoCo coverage
    ├── Implemented CrackDetector.java: CCIR-601 grayscale, rolling-ball subtraction, Triangle binarization
    └── Built morphological closing gap-bridging and circularity-based particle filtering
```

While **TriangleFX** taught us the discipline of rigorous mathematical proofs, headless unit testing, and UI decoupling in vector space, the **Wall Crack Detector** challenges us to manage the statistical messiness of physical real-world data—handling lighting shadows, sensor grain, and morphology in raster space.

---

## Key Takeaways

1. **Theory Directly Drives Implementation:** The mathematical principles analyzed on November 3 (intensity distributions, histogram thresholding, and morphological filtering) are not abstract concepts; they serve as the foundational building blocks of our `CrackDetector` engine.
2. **Perceptual Weighting Matters in Grayscale Conversion:** Simply averaging RGB channels loses critical luminance fidelity. Employing CCIR-601 luminance weights ($0.299R + 0.587G + 0.114B$) ensures that grayscale intensity accurately reflects human visual perception and surface contrast.
3. **Lighting Normalization is Crucial for Real-World Computer Vision:** Global thresholding fails when scenes contain macro shadows. Applying a 50-pixel rolling-ball background subtraction removes gradual illumination gradients while preserving high-frequency crack dips.
4. **Choose Thresholding Algorithms Based on Data Distribution:** Otsu's method excels on bimodal histograms with balanced classes, but the **Triangle algorithm** is vastly superior for sparse defect detection where hairline cracks form a subtle tail on a dominant background peak.
5. **Morphological Closing Heals Discontinuities Non-Destructively:** Unlike linear Gaussian blurring which smears sharp edges, morphological closing (dilation followed by erosion) bridges narrow hairline gaps without sacrificing true boundary width or edge sharpness.
6. **Circularity Decouples Genuine Cracks from Noise:** By pairing minimum area thresholds with a dimensionless circularity ceiling ($4\pi \frac{\text{Area}}{\text{Perimeter}^2} < 0.6$), the system effectively rejects circular blemishes, screw holes, and plaster grain while retaining elongated, irregular crack structures.
