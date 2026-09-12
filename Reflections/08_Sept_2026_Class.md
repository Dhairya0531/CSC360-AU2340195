# Class Reflection — September 8, 2026

## Topics Covered

- The Java Collections Framework (`List`, `Set`, `Queue`, `Deque`, `Map`)
- Collection views vs. independent copies
- Managing errors and diagnostics: Exceptions, Logging, and Debugging
- Event handling mechanisms in AWT and Swing (The Delegation Event Model)
- The Java GUI event class hierarchy
- Building master-detail graphical layouts

---

## Exploring the Java Collections Framework

We spent a solid part of class walking through the Java Collections Framework (JCF). Rather than rolling our own data structures from scratch, Java provides a unified hierarchy of interfaces and battle-tested implementations designed for grouping and manipulating objects.

The framework is cleanly divided based on how data is organized and accessed:

### 1. `List`: Ordered Sequences with Duplicates

A `List` preserves insertion order and allows elements to be accessed or modified by an integer index. Duplicates are permitted.

- **`ArrayList`:** Backed by a dynamic resizable array. Retrieving an item via `get(index)` is $O(1)$, making it the standard workhorse for general-purpose lists. However, inserting or deleting elements in the middle requires shifting downstream elements ($O(n)$).
- **`LinkedList`:** Implemented as a doubly-linked list. It provides quick insertion and deletion at the ends or when holding an iterator, but arbitrary index lookups require traversing nodes sequentially ($O(n)$). It also implements `Deque`.

```java
List<String> studentRoster = new ArrayList<>();
studentRoster.add("Rohan");
studentRoster.add("Priya");
studentRoster.add("Rohan"); // Allowed: lists accept duplicates

String firstStudent = studentRoster.get(0); // O(1) indexed lookup
```

### 2. `Set`: Collections of Distinct Elements

A `Set` enforces uniqueness—it rejects any element already present according to `equals()`.

- **`HashSet`:** Leverages a hash table (`HashMap` behind the scenes). Offers near-constant time $O(1)$ performance for lookups, insertions, and deletions, but does not guarantee any specific iteration order.
- **`LinkedHashSet`:** Combines a hash table with a linked list to retain elements in their insertion order.
- **`TreeSet`:** Backed by a Red-Black tree. Elements are continuously kept sorted in ascending natural order (or via a custom `Comparator`). Basic operations take $O(\log n)$ time.

```java
Set<String> rollNumbers = new HashSet<>();
rollNumbers.add("AU234001");
rollNumbers.add("AU234002");
boolean addedAgain = rollNumbers.add("AU234001"); // Returns false; set size remains 2
```

### 3. `Queue` and `Deque`: Controlled Order Processing

Queues hold elements intended for processing in a specific sequence—most frequently First-In, First-Out (FIFO).

- **`ArrayDeque`:** An efficient circular resizable array implementation without capacity restrictions. It excels as both a FIFO queue and a LIFO stack, consistently outperforming `Stack` and `LinkedList`.
- **`PriorityQueue`:** Does not follow insertion order; instead, items are ordered by priority (natural sorting or a `Comparator`). Calling `poll()` always pulls the minimum (or maximum) priority element ($O(\log n)$ extraction).

```java
Queue<String> printJobs = new ArrayDeque<>();
printJobs.offer("LabReport.pdf");
printJobs.offer("LectureSlides.pdf");

String currentJob = printJobs.poll(); // Returns "LabReport.pdf"
```

### 4. `Map`: Key-Value Mappings

A `Map` models associations between unique keys and corresponding values. Each key maps to at most one value.

- **`HashMap`:** Provides $O(1)$ average-time key lookups without maintaining iteration order.
- **`LinkedHashMap`:** Preserves insertion order (or access order when configured as an LRU cache).
- **`TreeMap`:** Retains keys in sorted order using a balanced binary search tree ($O(\log n)$ lookups).

```java
Map<String, Double> gradeAverages = new HashMap<>();
gradeAverages.put("AU234001", 88.5);
gradeAverages.put("AU234002", 94.0);

// Updating an existing key overrides the value
gradeAverages.put("AU234001", 91.0);
```

> **Why doesn't `Map` extend `Collection`?**
> A frequent conceptual question: A `Collection` is a group of individual items (`E`), while a `Map` works on pairs of elements (a key `K` and a value `V`). Because key-value pairs require distinct contracts (e.g., uniqueness of keys, querying keys vs values), forcing `Map` into `Collection` would break interface consistency.

### Framework Overview

| Interface | Ordering Guarantee | Allows Duplicates? | Common Implementations | Typical Use Case |
|---|---|---|---|---|
| `List` | Positional / Insertion order | Yes | `ArrayList`, `LinkedList` | Maintaining ordered items with index access |
| `Set` | Implementation-dependent | No | `HashSet`, `LinkedHashSet`, `TreeSet` | Tracking unique IDs, tags, or visited states |
| `Queue` / `Deque` | FIFO or priority order | Yes | `ArrayDeque`, `PriorityQueue` | Task pipelines, scheduling, breadcrumbs |
| `Map` | Key-based ordering | Unique keys, duplicate values | `HashMap`, `LinkedHashMap`, `TreeMap` | Lookups, dictionaries, caching metadata |

---

## Collection Views: References vs. Copies

One of the most important takeaways from this discussion was understanding **collection views**. A view gives you an alternate window into an existing collection without creating an independent duplicate of the underlying data.

Because a view is backed by the original data structure, mutations can ripple across both sides.

### 1. Sublist Views (`subList`)

Calling `subList(fromIndex, toIndex)` returns a live slice backed by the parent list:

```java
List<String> fruits = new ArrayList<>(List.of("Apple", "Banana", "Cherry", "Date"));
List<String> middleSlice = fruits.subList(1, 3); // ["Banana", "Cherry"]

// Modifying through the view modifies the original!
middleSlice.set(0, "Blueberry");

System.out.println(fruits); // Output: [Apple, Blueberry, Cherry, Date]
```

**The Gotcha:** If the underlying list is structurally modified (elements added or removed) directly instead of through the `subList` view, the view becomes invalid. Subsequent access on the view typically throws a `ConcurrentModificationException`.

### 2. Map Views

A `Map` exposes three views of its internal data:

```java
Map<String, Integer> inventory = new HashMap<>();
inventory.put("Pencils", 50);
inventory.put("Notebooks", 20);

Set<String> productNames = inventory.keySet();            // View of all keys
Collection<Integer> stockCounts = inventory.values();      // View of all values
Set<Map.Entry<String, Integer>> pairs = inventory.entrySet(); // View of key-value tuples
```

If you delete an element from `productNames`:
```java
productNames.remove("Pencils"); // Removes both the key AND its value from `inventory`
```

### 3. Read-Only Views vs. Defensive Snapshots

It is critical not to confuse an unmodifiable view with an independent snapshot:

```java
List<String> original = new ArrayList<>(List.of("Alpha", "Beta"));

// 1. Unmodifiable View:
List<String> readOnlyView = Collections.unmodifiableList(original);

// 2. True Immutable Snapshot (Java 10+):
List<String> independentCopy = List.copyOf(original);

// Modifying the underlying source:
original.add("Gamma");

System.out.println(readOnlyView);     // [Alpha, Beta, Gamma] -> Reflects changes!
System.out.println(independentCopy);  // [Alpha, Beta]         -> Completely isolated
```

- `Collections.unmodifiableList()` prevents direct edits through the wrapper, but changes to `original` still leak through.
- `List.copyOf()` or creating a new collection (`new ArrayList<>(original)`) allocates new memory and protects against downstream mutations.

---

## Managing Failures: Exceptions, Logging, and Debugging

Writing reliable software requires a clear separation between handling runtime errors, keeping an audit trail, and diagnosing broken logic.

### 1. Exceptions: Handling the Unexpected

Exceptions interrupt normal execution when something goes wrong. Java categorizes them into two primary branches:

- **Checked Exceptions (extend `Exception` directly):** The compiler mandates handling these using `try-catch` blocks or declaring them via `throws`. They represent recoverable failure conditions external to your code (e.g., missing files, network interruptions).
  - Examples: `IOException`, `SQLException`.
- **Unchecked Exceptions (extend `RuntimeException`):** The compiler does not force you to handle them. They generally indicate programming errors, invalid arguments, or violated invariants that should have been avoided.
  - Examples: `NullPointerException`, `IndexOutOfBoundsException`, `IllegalArgumentException`.
- **Errors (extend `Error`):** Catastrophic system failures outside ordinary application recovery (e.g., `OutOfMemoryError`, `StackOverflowError`). Applications should rarely attempt to catch them.

```java
public void loadConfiguration(Path configPath) {
    try {
        String data = Files.readString(configPath);
        parseSettings(data);
    } catch (NoSuchFileException e) {
        // Recover or report missing configuration
        logger.warn("Config file missing at {}. Applying defaults.", configPath);
        applyDefaults();
    } catch (IOException e) {
        logger.error("Failed to read config file due to I/O error", e);
        throw new IllegalStateException("Startup aborted: unreadable configuration", e);
    }
}
```

### 2. Logging: Recording Application Behavior

While `System.out.println()` works for quick experiments, production software relies on structured logging frameworks (such as SLF4J backed by Logback or Log4j2).

**Why logging frameworks matter:**
- **Configurable Levels:** Messages are categorized by severity (`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`). In production, noisy debug logs can be silenced without touching code.
- **Routing & Format:** Logs can output to files, cloud aggregators, or standard out with timestamps, thread IDs, and class names automatically attached.
- **Parameterized Logging:** Avoids costly string concatenation when a log level is disabled:
  ```java
  // Efficient: evaluation is deferred if DEBUG is turned off
  logger.debug("Processed transaction {} for account {}", txnId, accountId);
  ```

### 3. Systematic Debugging

Debugging is a methodical process rather than random trial-and-error:
1. **Reproduce:** Find the minimal repeatable steps or unit test that triggers the fault.
2. **Isolate:** Set breakpoints in your IDE near the point of departure.
3. **Inspect:** Examine variable states, object references, and the call stack.
4. **Step Through:** Use *Step Over* (F8) to progress line by line, *Step Into* (F7) to enter method internals, and *Step Out* (Shift+F8) to return to the caller.
5. **Advanced Breakpoints:** Conditional breakpoints (pausing only when `i == 999`) and exception breakpoints (pausing the exact moment an NPE is thrown) save huge amounts of time in large loops.

---

## Event-Driven Programming in AWT and Swing

Unlike command-line programs that run sequentially from `main()` to termination, GUI applications are **event-driven**. They sit in an idle event loop waiting for external inputs—clicks, keypresses, mouse movements, or window resizing.

### The Delegation Event Model

Java GUI frameworks (both AWT and Swing) rely on the **delegation event model**, which separates event generation from event handling:

1. **Event Source:** The GUI component where the interaction originates (e.g., `JButton`, `JTextField`).
2. **Event Object:** An object encapsulating details about what occurred (e.g., timestamp, coordinates, click count).
3. **Event Listener:** An interface containing callback methods. A listener registers with the source. When the event occurs, the source invokes the appropriate callback on all registered listeners.

```java
JButton submitButton = new JButton("Submit Query");

// Registering an action listener using modern lambda syntax:
submitButton.addActionListener(event -> {
    String command = event.getActionCommand();
    runQuery(command);
});
```

### Common Events and Their Listeners

| Event Class | Listener Interface | Common Triggers | Key Methods |
|---|---|---|---|
| `ActionEvent` | `ActionListener` | Clicking a button, choosing a menu item, hitting Enter in a text field | `actionPerformed(ActionEvent e)` |
| `MouseEvent` | `MouseListener` | Pressing, releasing, clicking, entering, or exiting a component | `mousePressed`, `mouseReleased`, `mouseClicked` |
| `MouseEvent` | `MouseMotionListener` | Moving or dragging the cursor across a component | `mouseMoved`, `mouseDragged` |
| `KeyEvent` | `KeyListener` | Keyboard button pressed, released, or typed | `keyPressed`, `keyReleased`, `keyTyped` |
| `WindowEvent` | `WindowListener` | Opening, closing, iconifying, or activating a window | `windowClosing`, `windowOpened`, etc. |
| `ListSelectionEvent` | `ListSelectionListener` | Changing selection in a `JList` or `JTable` | `valueChanged(ListSelectionEvent e)` |
| `DocumentEvent` | `DocumentListener` | Modifying text inside a Swing text component | `insertUpdate`, `removeUpdate`, `changedUpdate` |

### Avoiding Boilerplate with Adapter Classes

Many listener interfaces contain multiple methods (for instance, `WindowListener` has 7 methods). If you only care about closing the window, implementing the full interface forces you to write empty bodies for all other methods.

AWT provides **Adapter classes** (like `WindowAdapter`, `MouseAdapter`) that provide empty default implementations so you can override only what you need:

```java
// Using an adapter instead of implementing all 7 WindowListener methods
frame.addWindowListener(new WindowAdapter() {
    @Override
    public void windowClosing(WindowEvent e) {
        promptSaveAndClose();
    }
});
```

### The Event Dispatch Thread (EDT) and `SwingWorker`

Swing is single-threaded. All component rendering and event listener callbacks execute on a dedicated thread called the **Event Dispatch Thread (EDT)**.

- **Golden Rule:** Never perform blocking I/O, heavy file operations, or complex calculations on the EDT. If the EDT is blocked, the interface freezes, buttons stop responding, and the window will not repaint.
- **Offloading Work:** Use `SwingWorker<T, V>` or background threads to execute heavy operations asynchronously, updating the Swing components only when results are ready via `done()` or `process()`.

---

## The Java Event Class Hierarchy

All event objects in Java derive from `java.util.EventObject`. The GUI events branch under `java.awt.AWTEvent`. Understanding this hierarchy makes it easier to know what information is accessible inside an event handler.

```text
java.util.EventObject
└── java.awt.AWTEvent
    ├── ActionEvent          (Buttons, menus, action commands)
    ├── AdjustmentEvent      (Scrollbars and range adjustments)
    ├── ItemEvent            (Checkboxes, radio buttons, combo boxes)
    ├── TextEvent            (AWT text changes)
    └── ComponentEvent       (Geometry/visibility changes)
        ├── FocusEvent       (Keyboard focus gained / lost)
        ├── WindowEvent      (Window lifecycle events)
        ├── ContainerEvent   (Child components added / removed)
        └── InputEvent       (Raw user hardware input)
            ├── KeyEvent     (Key codes, unicode characters, modifiers)
            └── MouseEvent   (Coordinates, button index, clicks)
                └── MouseWheelEvent (Wheel rotation metrics)
```

Because `MouseEvent` and `KeyEvent` inherit from `InputEvent`, they provide access to modifier keys (like `event.isShiftDown()` or `event.isControlDown()`). Furthermore, because they inherit from `AWTEvent` and `EventObject`, any handler can call `getSource()` to identify the originating component.

---

## Implementing Master-Detail User Interfaces

A classic desktop and enterprise user interface design pattern is the **Master-Detail** layout.

### Concept

The layout partitions the workspace into two linked panels:
- **Master Section:** Displays a high-level list or table of items (e.g., inbox emails, registered students, product catalogs).
- **Detail Section:** Shows comprehensive attributes or an editable form for whichever item is currently selected in the master section.

```text
+----------------------+------------------------------------------+
|      MASTER VIEW     |               DETAIL VIEW                |
|  (JList / JTable)    |                 (JPanel)                 |
|                      |                                          |
| > John Doe           | Name: John Doe                           |
|   Jane Smith         | Email: jdoe@example.com                  |
|   Robert Taylor      | Status: Active                           |
|                      | [Edit] [Delete]                          |
+----------------------+------------------------------------------+
```

### Swing Implementation Details

In Swing, this is commonly constructed using:
1. A `JList` or `JTable` wrapped in a `JScrollPane` as the master component.
2. A customized `JPanel` with labels and text fields as the detail component.
3. A `JSplitPane` holding both sides with a movable divider.
4. A `ListSelectionListener` to synchronize selections with the detail view.

```java
// Master list of records
DefaultListModel<Student> studentListModel = new DefaultListModel<>();
JList<Student> masterStudentList = new JList<>(studentListModel);

// Detail form
StudentDetailPanel detailPanel = new StudentDetailPanel();

// Split layout
JSplitPane splitPane = new JSplitPane(
    JSplitPane.HORIZONTAL_SPLIT,
    new JScrollPane(masterStudentList),
    detailPanel
);
splitPane.setDividerLocation(250);

// Synchronizing selection:
masterStudentList.addListSelectionListener(e -> {
    // Crucial check: ignore intermediate selection states while dragging
    if (!e.getValueIsAdjusting()) {
        Student selected = masterStudentList.getSelectedValue();
        if (selected != null) {
            detailPanel.populate(selected);
        }
    }
});
```

> **Important Detail:** `event.getValueIsAdjusting()` returns `true` while the user is actively dragging through list items with the mouse or arrow keys. Checking `!e.getValueIsAdjusting()` ensures the detail panel updates only when the selection settles, preventing unnecessary re-renders.

---

## Key Takeaways

1. **Pick the Right Collection:** Use `ArrayList` for fast random access, `LinkedList`/`ArrayDeque` for end-insertion/queueing, `HashSet` for distinct items, and `HashMap` for key lookups.
2. **Map vs. Collection:** `Map` is an integral part of the Collections Framework, but it models pairings ($K \rightarrow V$) rather than individual elements, so it intentionally does not inherit from `Collection`.
3. **Views vs. Copies:** Views (`subList`, `keySet()`) share their backing data with the parent collection. Direct structural modifications to the source can break active views. If isolation is needed, create defensive copies using `List.copyOf()` or explicit constructors.
4. **Checked vs. Unchecked Exceptions:** Reserve checked exceptions for recoverable external conditions (like I/O failures). Use unchecked runtime exceptions for programmatic logic errors.
5. **Logs Over Prints:** Structured logging frameworks provide level-based filtering, contextual metadata, and zero-overhead parameterized string substitution.
6. **Delegation Event Architecture:** Event sources broadcast typed event objects to registered listeners. Adapter classes eliminate empty boilerplate methods.
7. **Protect the EDT:** Heavy computations and network calls must never run on the Event Dispatch Thread; keep UI interactions snappy by moving background tasks to worker threads.
8. **Master-Detail Flow:** Keep data models independent of UI presentation, and always verify `!e.getValueIsAdjusting()` when observing selection changes in Swing lists.
