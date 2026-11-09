# Class Reflection — November 3, 2026

## Topics Covered

- Non-linear contrast adjustments and tonal transfer functions: Gamma scaling ($s = c \cdot r^\gamma$) and logarithmic dynamic range compression
- Intensity distribution analysis: Discrete probability density functions (PDF) and cumulative distribution functions (CDF)
- Global vs. Local contrast enhancement: Contrasting standard Histogram Equalization (GHE) with Contrast Limited Adaptive Histogram Equalization (CLAHE)
- Spatial domain convolution mechanics: Discrete 2D convolution, correlation, sliding window indexing, and perimeter boundary handling
- Filter architectures and frequency response: Low-pass smoothing (Box, Gaussian), non-linear rank filtering (Median), and derivative edge detection (Sobel, Laplacian)
- Algorithmic optimization and precision: Separable kernel factorizations ($O(K^2) \to O(2K)$) and floating-point accumulation buffers

---

## Overview: Advanced Tonal Manipulation and Spatial Context

Today's session expanded our study of digital image processing by investigating how statistical distributions and spatial neighborhood relationships can be manipulated to enhance visual fidelity and extract structural features. 

While previous discussions introduced the high-level concepts of contrast, image histograms, and filtering kernels, today we examined their formal mathematical mechanics and practical engineering trade-offs. Raster processing requires an understanding of both **photometric transformations** (modifying pixel intensities based on global or local distributions) and **spatial convolutions** (transforming pixel values based on localized spatial neighborhoods).

```text
┌───────────────────────────┐     ┌───────────────────────────┐     ┌───────────────────────────┐
│     Tonal Remapping       │     │    Histogram Profiling    │     │   Spatial Neighborhood    │
│  - Linear Min-Max Stretch │ ──► │  - Empirical PDF & CDF    │ ──► │  - Sliding Window Kernel  │
│  - Power-Law (Gamma) Curve│     │  - Global Equalization    │     │  - Low-Pass & High-Pass   │
│  - Dynamic Range Expansion│     │  - Tile-Based CLAHE       │     │  - Boundary Padding       │
└───────────────────────────┘     └───────────────────────────┘     └───────────────────────────┘
```

By connecting tonal transformations with discrete spatial convolution, we establish the foundation for advanced computer vision operations, including feature tracking, boundary segmentation, and adaptive image restoration.

---

## 1. Contrast Dynamics: Perceptual Salience and Non-Linear Tonal Transformations

### The Nature of Contrast in Digital Images

In digital imagery, **contrast** represents the degree of luminance and chromatic differentiation that allows distinct visual entities, textures, and object boundaries to be distinguished from one another. Rather than measuring absolute brightness, contrast quantifies the **rate of intensity change** across spatial coordinates or across the scene's global dynamic range.

The Human Visual System (HVS) does not perceive light linearly; rather, it responds logarithmically to intensity variations according to the Weber-Fechner law. Consequently, contrast is what enables our visual cortex to delineate structural geometry, depth planes, and surface reflectance under varying illumination conditions.

```text
Linear vs. Non-Linear Intensity Distribution

Linear Scale (Equal numerical steps, unequal perceived steps):
[ 0 ]-------[ 32 ]-------[ 64 ]-------[ 128 ]-------[ 192 ]-------[ 255 ]
Deep Black                                                     Peak White

Perceptual Scaling (Logarithmic response):
Small luminance shifts in shadows (10 -> 20) are perceived as dramatic changes;
identical shifts in highlights (235 -> 245) are barely detectable.
```

---

### Non-Linear Tonal Transfer Functions

When linear min-max contrast stretching fails to distribute details across shadowed or blown-out regions, non-linear transfer functions $s = T(r)$ are employed:

#### 1. Power-Law (Gamma) Transformations
The power-law transformation is formulated as:

$$s = c \cdot r^\gamma$$

where $r \in [0, 1]$ represents normalized input intensity, $s \in [0, 1]$ is the output intensity, and $c$ is a scaling constant (typically $c = 1$).
- **When $\gamma < 1$ (Shadow Expansion):** The curve bows upward, mapping a narrow band of dark input values into a wider band of output values, brightening shadows without washing out highlight anchors.
- **When $\gamma > 1$ (Highlight Expansion):** The curve dips downward, compressing shadow values and stretching highlights, ideal for toning down overexposed scenes.
- **When $\gamma = 1$:** The mapping represents the standard linear identity function.

```text
Gamma Transfer Curves: s = r^γ
Output s
  1.0 ┌───────────────────────────────/
      │                         . ' /  (γ = 0.4: Shadow expansion)
      │                   . '      /
      │             . '           /
      │       . '                /
      │  . '                    /      (γ = 1.0: Linear identity)
  0.5 │                    . ' /
      │                . '    /
      │            . '       /   . '   (γ = 2.5: Contrast compression / highlight focus)
      │        . '       . ' . '
  0.0 └───────────────────────────────
      0.0              0.5           1.0  Input r
```

#### 2. Logarithmic Dynamic Range Compression
For imagery with extreme dynamic range (such as Fourier spectrums or HDR sensor readouts), the logarithmic transformation compresses high-intensity peaks while expanding lower values:

$$s = c \cdot \log(1 + r)$$

This function prevents dominant high-intensity pixels from masking subtle low-intensity structural details.

---

### Summary of Tonal Mapping Approaches

| Transformation Type | Mathematical Formula | Primary Mechanism | Visual Effect & Application |
|---|---|---|---|
| **Linear Min-Max Stretch** | $s = \frac{r - r_{\min}}{r_{\max} - r_{\min}} \cdot (L - 1)$ | Affine rescaling of intensity range | Restores contrast across underutilized dynamic ranges without altering modal relationships. |
| **Gamma Correction ($\gamma < 1$)** | $s = r^\gamma$ ($0 < \gamma < 1$) | Expands dark tones non-linearly | Illuminates underexposed shadows while preserving highlight details. |
| **Gamma Correction ($\gamma > 1$)** | $s = r^\gamma$ ($\gamma > 1$) | Compresses shadows, expands highlights | Enhances bright regions and increases overall visual drama/punch. |
| **Logarithmic Transform** | $s = c \log(1 + r)$ | Dynamic range compression | Visualizes frequency spectra and brings out detail in high dynamic range data. |

---

## 2. Histogram Profiling, Equalization, and Adaptive Contrast Management

### Statistical Anatomy of an Image Histogram

An image histogram provides a global statistical summary of pixel distributions. Given an $M \times N$ 8-bit image with $L = 256$ discrete intensity levels $r_k \in [0, L-1]$:
- The frequency count of pixels having intensity $r_k$ is denoted $n_k$.
- The **normalized probability mass function (PMF)** is:

$$p(r_k) = \frac{n_k}{M \cdot N}, \quad \text{where } \sum_{k=0}^{L-1} p(r_k) = 1$$

- The **Cumulative Distribution Function (CDF)** measures the accumulated probability of all intensities up to $r_k$:

$$P(r_k) = \sum_{j=0}^{k} p(r_j)$$

The shape of this distribution provides a direct diagnostic read on the photometric health of an image:

```text
Exposure Profiles and Corresponding CDF Curves

1. Underexposed / Left-Skewed            2. Balanced / Uniform CDF
   PMF                   CDF                PMF                   CDF
   ^                     ^                  ^                     ^
   |█                    |    .---          |    .----.           |     /
   |██                   |   /              |   /      \          |    /
   |███____              | _/               | _/        \_        |  /
   +-------> r           +-------> r        +-------> r           +-------> r
```

---

### Global Histogram Equalization (GHE)

Global Histogram Equalization seeks to linearize the cumulative distribution function so that every intensity level in the dynamic range has equal representation, maximizing the image's information entropy.

The transformation function maps each input level $r_k$ to an output level $s_k$:

$$s_k = T(r_k) = \text{round}\left( (L - 1) \cdot \sum_{j=0}^{k} p(r_j) \right) = \text{round}\left( \frac{L - 1}{M \cdot N} \sum_{j=0}^{k} n_j \right)$$

While GHE effectively expands compressed contrast ranges, it has a significant real-world drawback: **it operates globally**. In scenes with large, relatively uniform regions (such as an open sky or dark backdrop), GHE over-amplifies the contrast of subtle sensor noise, introducing harsh banding, grain, and unnatural artifacts into homogeneous areas.

---

### Contrast Limited Adaptive Histogram Equalization (CLAHE)

To overcome the artifacts of global equalization, **Contrast Limited Adaptive Histogram Equalization (CLAHE)** localizes the transformation process:

```text
CLAHE Tile Division and Interpolation Pipeline

┌───────────┬───────────┬───────────┐
│  Tile 11  │  Tile 12  │  Tile 13  │
│  (8x8 px) │  (8x8 px) │  (8x8 px) │
├───────────┼───────────┼───────────┤
│  Tile 21  │  Tile 22  │  Tile 23  │   1. Divide image into contextual grid tiles (e.g. 8x8)
│     • P(x,y)          │           │   2. Calculate local histogram & clip at predefined limit
├───────────┼───────────┼───────────┤   3. Redistribute clipped pixels uniformly across all bins
│  Tile 31  │  Tile 32  │  Tile 33  │   4. Compute local CDF mapping per tile
└───────────┴───────────┴───────────┘   5. Map pixel P(x,y) via Bilinear Interpolation across 4 tiles
```

1. **Grid Partitioning:** The image is partitioned into non-overlapping contextual tiles (typically $8 \times 8$ or $16 \times 16$ pixels).
2. **Contrast Limiting (Clipping):** A local histogram is constructed for each tile. A clip limit threshold is applied; any pixel counts exceeding this threshold are clipped and redistributed evenly across all histogram bins, preventing excessive noise amplification.
3. **Local CDF Computation:** An equalization mapping is derived for each individual tile based on its clipped distribution.
4. **Bilinear Interpolation:** To eliminate visible boundary seams between adjacent tiles, each pixel's final value is computed by bilinearly interpolating the transformation values of the four surrounding tile centers.

CLAHE is the industry-standard contrast enhancement method across medical radiography, satellite imagery, and autonomous driving vision systems.

---

## 3. Spatial Domain Filtering: Discrete Convolution and Neighborhood Operations

### Point Processing vs. Neighborhood Processing

While tonal mappings (contrast adjustment, histogram remapping) are **point operations** where $g(x, y) = T[f(x, y)]$, spatial filtering belongs to **neighborhood processing**:

$$g(x, y) = \mathcal{F}\left[ \{ f(x + i, y + j) \mid (i, j) \in \mathcal{N}(x, y) \} \right]$$

Here, the output pixel value is calculated by taking into account both the central pixel and the spatial configuration of its immediate neighbors.

---

### Mechanics of Discrete 2D Spatial Convolution

Spatial filtering is implemented by convolving an image $f(x, y)$ with a spatial kernel (or mask) $w(s, t)$ of dimensions $m \times n$ (with odd dimensions $m = 2a + 1, n = 2b + 1$):

$$g(x, y) = f(x, y) * w(x, y) = \sum_{s=-a}^{a} \sum_{t=-b}^{b} w(s, t) \cdot f(x - s, y - t)$$

In true convolution, the kernel is rotated $180^\circ$ prior to multiplying. If the kernel is applied without flipping, the operation is **spatial cross-correlation**:

$$g_{\text{corr}}(x, y) = \sum_{s=-a}^{a} \sum_{t=-b}^{b} w(s, t) \cdot f(x + s, y + t)$$

For symmetric masks (such as Gaussian or Laplacian filters), spatial convolution and cross-correlation yield identical results.

```text
2D Discrete Convolution Mechanism (3x3 Kernel Example)

Image Neighborhood Array                   Convolution Kernel Mask
┌───────────┬───────────┬───────────┐    ┌───────────┬───────────┬───────────┐
│ f(x-1,y-1)│ f(x, y-1) │ f(x+1,y-1)│    │  w(-1,-1) │  w(0,-1)  │  w(1,-1)  │
├───────────┼───────────┼───────────┤    ├───────────┼───────────┼───────────┤
│ f(x-1, y) │  f(x, y)  │ f(x+1, y) │  * │  w(-1, 0) │  w(0, 0)  │  w(1, 0)  │
├───────────┼───────────┼───────────┤    ├───────────┼───────────┼───────────┤
│ f(x-1,y+1)│ f(x, y+1) │ f(x+1,y+1)│    │  w(-1, 1) │  w(0, 1)  │  w(1, 1)  │
└───────────┴───────────┴───────────┘    └───────────┴───────────┴───────────┘
                       │
                       ▼
    g(x, y) = Σ [ w(s, t) · f(x + s, y + t) ]   for s, t ∈ {-1, 0, 1}
                       │
                       ▼
       New Pixel Written to Destination Image Buffer g
```

---

### Handling Boundary Discontinuities

When evaluating pixels along the image perimeter, portions of the kernel extend beyond the valid pixel grid. Handling these boundary edges properly prevents memory faults and border distortion:

```text
Boundary Treatment Options for a Kernel Overhanging the Border

1. Zero-Padding (Border = 0)            2. Replicate / Clamping (Border = Nearest)
   ┌───┬───┬───┬───┐                       ┌───┬───┬───┬───┐
   │ 0 │ 0 │ 0 │ 0 │                       │ 45│ 45│ 50│ 55│
   ├───┼───┼───┼───┤                       ├───┼───┼───┼───┤
   │ 0 │ 45│ 50│ 55│                       │ 45│ 45│ 50│ 55│
   ├───┼───┼───┼───┤                       ├───┼───┼───┼───┤
   │ 0 │ 60│ 65│ 70│                       │ 60│ 60│ 65│ 70│
   └───┴───┴───┴───┘                       └───┴───┴───┴───┘
   (Creates artificial dark border frames) (Preserves natural boundary average)
```

- **Zero-Padding:** Unmapped pixels are assigned value $0$. Simple to implement, but creates artificial dark borders when applying low-pass smoothing filters.
- **Clamp / Replicate:** Extends the outermost perimeter pixels outward. Avoids dark framing artifacts and preserves mean local intensity.
- **Reflection / Mirror:** Reflects boundary pixels symmetrically ($f(-1) = f(1)$). This maintains first-order spatial derivatives across the border and is common in high-quality image processing.
- **Normalized Cropping:** Only computes convolution values where the kernel is fully supported, resulting in an output image shrunk by $2a$ pixels horizontally and $2b$ pixels vertically.

---

## 4. Kernel Architectures: Linear Smoothing, Rank Filters, and Edge Gradients

### Taxonomy of Spatial Filters

```text
Spatial Domain Filtering Operations
├── Low-Pass Smoothing (Noise reduction & blurring)
│   ├── Box / Mean Filter (Equal weight moving average)
│   └── Gaussian Filter (Distance-weighted isotropic bell curve)
├── Non-Linear Rank Statistics (Edge-preserving noise suppression)
│   ├── Median Filter (Removes salt-and-pepper / impulse noise)
│   ├── Min Filter (Morphological erosion / dark feature expansion)
│   └── Max Filter (Morphological dilation / bright feature expansion)
└── High-Pass & Derivative (Edge detection & sharpening)
    ├── 1st Order Gradients: Sobel, Prewitt, Scharr (Directional vectors)
    ├── 2nd Order Laplacians: Discrete Laplacian, LoG (Zero-crossing localization)
    └── High-Boost / Unsharp Masking: Detail & texture amplification
```

---

### Detailed Comparison of Standard Filter Kernels

| Filter Name | Mathematical Kernel Formulation | Core Function | Strengths & Trade-offs |
|---|---|---|---|
| **Box (Mean) Filter** | $w = \frac{1}{9} \begin{bmatrix} 1 & 1 & 1 \\ 1 & 1 & 1 \\ 1 & 1 & 1 \end{bmatrix}$ | Replaces target pixel with local arithmetic mean | Extremely fast; however, it blurs sharp object boundaries and produces visible square bokeh artifacts. |
| **Gaussian Filter** | $G(x, y) = \frac{1}{2\pi\sigma^2} e^{-\frac{x^2+y^2}{2\sigma^2}}$ | Isotropic low-pass smoothing weighted by spatial distance | Natural, rotationally symmetric blur; preserves frequency details better than a simple box filter. |
| **Median Filter** | $g(x, y) = \text{median}\{ f(x+i, y+j) \}$ | Non-linear rank-order filter; replaces center with median neighbor | **Eradicates impulse (salt-and-pepper) noise** while leaving sharp structural edges completely intact. |
| **Sobel Gradient** | $K_x = \begin{bmatrix}-1&0&1\\-2&0&2\\-1&0&1\end{bmatrix}, K_y = \begin{bmatrix}-1&-2&-1\\0&0&0\\1&2&1\end{bmatrix}$ | First-order spatial derivatives; computes gradient vector $\nabla f$ | Highlights boundaries; calculates both gradient magnitude $\|\nabla f\|$ and edge direction $\theta = \arctan(K_y/K_x)$. |
| **Laplacian Filter** | $\nabla^2 f \approx \begin{bmatrix} 0 & 1 & 0 \\ 1 & -4 & 1 \\ 0 & 1 & 0 \end{bmatrix}$ or $\begin{bmatrix} 1 & 1 & 1 \\ 1 & -8 & 1 \\ 1 & 1 & 1 \end{bmatrix}$ | Second-order isotropic derivative; finds inflection points | Identifies edge centerlines via zero-crossings; highly vulnerable to noise unless preceded by Gaussian smoothing. |
| **Unsharp Masking** | $f_{\text{sharp}} = f + k \cdot (f - f_{\text{smooth}})$, with $k \ge 1$ | High-boost filtering; amplifies high spatial frequencies | Significantly improves perceived clarity and edge contrast; can amplify sensor noise if $k$ is set too high. |

---

### Separable Kernels: Algorithmic Efficiency

An essential computational optimization in spatial filtering is identifying whether a 2D kernel is **separable**. A 2D matrix $W$ of size $K \times K$ is separable if it can be factored into the outer product of two 1D vectors:

$$W = u \cdot v^T$$

The 2D Gaussian kernel is naturally separable:

$$G(x, y) = \left( \frac{1}{\sqrt{2\pi}\sigma} e^{-\frac{x^2}{2\sigma^2}} \right) \cdot \left( \frac{1}{\sqrt{2\pi}\sigma} e^{-\frac{y^2}{2\sigma^2}} \right) = G(x) \cdot G(y)$$

```text
2D Direct Convolution vs. Separable Decomposition

Direct 2D Convolution (K x K):
  For each pixel: K² multiplications + (K² - 1) additions
  Complexity per pixel: O(K²)

Separable 1D Two-Pass Convolution:
  Pass 1 (Horizontal 1D filtering with u): K operations/pixel
  Pass 2 (Vertical 1D filtering with v):   K operations/pixel
  Complexity per pixel: O(2K)

Example (7x7 Kernel on 4K Image):
  - Direct 2D:   49 multiply-accumulate operations per pixel (~406 million ops total)
  - Separable:   14 multiply-accumulate operations per pixel (~116 million ops total)
  => 3.5x computational performance speedup with identical numerical output!
```

---

## 5. Architectural Trade-offs and Practical Engineering Decisions

### 1. Linear Blurring vs. Non-Linear Median Filtering
- **The Challenge:** Real-world sensors suffer from both high-frequency Gaussian thermal noise and bit-drop impulse noise (dead or saturated pixels).
- **The Trade-off:** While a Gaussian filter handles distributed noise well, applying it to impulse noise spreads bad pixel values to neighboring pixels, creating dark or bright splotches. A **Median filter** completely discards impulse outliers because extreme values naturally fall at the ends of the sorted rank list and are never chosen as the median.
- **Architectural Choice:** Run median filtering first to strip impulse anomalies, then follow with a light Gaussian pass for smooth gradient transitions.

### 2. Gradient Edge Detectors: First vs. Second Derivative
- **Sobel (First Derivative):** Produces wide edge ridges that require non-maximum suppression (such as in the Canny pipeline) to thin down to 1-pixel contours.
- **Laplacian (Second Derivative):** Generates clean, zero-crossing edge transitions, making boundary localization straightforward. However, because differentiation amplifies high-frequency components ($\mathcal{F}[\frac{d}{dx} f] \propto \omega \cdot F(\omega)$), second derivatives amplify noise quadratically ($\omega^2$).
- **Engineering Principle:** Always combine second-derivative filters with low-pass Gaussian pre-filtering, forming the **Laplacian of Gaussian (LoG)** operator or its Difference of Gaussians (DoG) approximation.

### 3. Buffer Formats and Arithmetic Overflow Handling
- Spatial filters frequently yield negative results (e.g., edge detectors with $[-1, 0, 1]$ coefficients) or values exceeding $255$.
- Storing intermediate values in unsigned 8-bit integer buffers (`uint8`) causes severe underflow (negative numbers wrapping around to $\approx 255$) and overflow.
- **Engineering Best Practice:** Maintain image processing pipelines using 32-bit floating-point buffers (`float32`). Perform all convolutions, dynamic range scalings, and intermediate math in float space, clamping back to $[0, 255]$ only during final display rasterization.

---

## Key Takeaways

1. **Contrast Defines Boundary Information:** Contrast measures the rate of intensity variation across space and dynamic range. While linear stretching scales values evenly, non-linear mappings like Gamma curves ($\gamma < 1$) expand dark shadow details to match the logarithmic response of human vision.
2. **Histograms Provide Structural Diagnostics:** An intensity histogram functions as an exposure fingerprint. Skewed distributions highlight underexposure or blown-out highlights, while narrow modal spikes indicate poor dynamic range utilization.
3. **Adaptive Equalization Prevents Over-Amplification:** Standard Global Histogram Equalization (GHE) often creates harsh noise artifacts in uniform areas. Contrast Limited Adaptive Histogram Equalization (CLAHE) solves this by operating on localized tiles, clipping peaks, and smoothly interpolating between boundaries.
4. **Spatial Filtering Depends on Local Context:** Unlike point-wise tone mapping, spatial filters transform target pixels based on their surrounding neighborhood using sliding 2D convolution masks.
5. **Non-Linear Rank Filters Preserve Sharp Boundaries:** Linear smoothers (Box, Gaussian) suppress noise at the cost of blurring object edges. The non-linear Median filter eliminates salt-and-pepper noise while leaving edge transitions crisp.
6. **Separable Kernels Optimize Real-Time Pipelines:** Decomposing symmetric 2D kernels into orthogonal 1D passes reduces algorithmic complexity from $O(K^2)$ to $O(2K)$, enabling real-time performance on high-resolution image data.

