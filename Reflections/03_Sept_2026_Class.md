# Class Reflection — September 3, 2026

## Topics Covered

- Text terminals vs graphical user interfaces
- Remote access with SSH
- Finding matching elements between two lists
- Visualizing matches with arrows in JavaFX
- Reproducing software failures
- Building JavaFX interfaces with FXML

---

## Terminals vs GUIs: It Depends on the Job

We opened today with a comparison that comes up a lot in development — when do you use a terminal and when do you use a GUI?

A terminal gives you a command-line interface where you type commands and get text back. A GUI gives you windows, buttons, menus, and visual components you can interact with directly. Neither one is universally better; the right choice depends on what you're trying to do.

**Where terminals shine:**

- They're lean — no extra memory or processing overhead for rendering graphics.
- Experienced users can work fast without hunting through menus.
- Commands can be scripted, which makes repetitive tasks consistent and automated.
- They work well over slow network connections since only text is transmitted.
- Every step is explicit and easy to document or share.

**Where GUIs have the edge:**

- Actions and options are visible, which helps new users figure out what's possible.
- Visual feedback matters when you're working with images, diagrams, or dashboards.
- Interactive workflows — dragging, resizing, previewing — are just more natural visually.

In practice, most development environments blend both. An IDE like IntelliJ has a GUI with an embedded terminal, which is a good example of not treating it as an either/or choice.

---

## Connecting to Remote Machines with SSH

SSH (Secure Shell) is how you get a terminal on another computer over a network. The basic command:

```sh
ssh username@example.com
```

All communication is encrypted, so it's safe to use even over public networks. Authentication works with a password or, more conveniently, a public key pair.

Once connected, you can do pretty much anything you could do locally — run commands, manage files, deploy code, edit configs, or transfer files using `scp` or `sftp`. Because only text is being sent back and forth, SSH is much lighter than streaming a full remote desktop. It's also easy to put common workflows into scripts, which is why server administration almost always happens through SSH.

---

## Matching Elements Across Two Lists

This part of class was more algorithmic. The scenario: you have two lists of values and want to find which items appear in both, then visualize those connections.

```java
List<String> leftItems  = List.of("A", "B", "C");
List<String> rightItems = List.of("C", "A", "D");
```

The straightforward approach is a nested loop — compare every item in the first list against every item in the second, and record any matches along with their indexes:

```java
for (int i = 0; i < leftItems.size(); i++) {
    for (int j = 0; j < rightItems.size(); j++) {
        if (leftItems.get(i).equals(rightItems.get(j))) {
            matches.add(new Match(i, j));
        }
    }
}
```

This runs in `O(n × m)` time. For small lists that's fine, but for larger ones a `HashMap` is faster — build a map of the right list's values to their indexes, then iterate through the left list with direct lookups:

```java
Map<String, List<Integer>> rightIndexes = new HashMap<>();

for (int j = 0; j < rightItems.size(); j++) {
    rightIndexes
        .computeIfAbsent(rightItems.get(j), k -> new ArrayList<>())
        .add(j);
}
```

Using a list of indexes per value handles duplicates correctly — if the same value appears multiple times, you record all of its positions.

Before writing any of this, there are a few things worth deciding upfront: Is matching case-sensitive? Should whitespace be ignored? Can one item match multiple items? How are `null` values treated? Those rules affect both the algorithm and what the visualization ends up looking like.

---

## Drawing Arrows Between Matched Items

Once you have a list of matching index pairs, the visual side is about converting those indexes into screen positions and drawing arrows between them.

The layout is straightforward — the left list sits on the left side of the pane, the right list on the right. For each match, you need the center-right point of the left label and the center-left point of the right label, then draw a line between them with an arrowhead at the destination.

A `Line` handles the shaft:

```java
Line shaft = new Line(startX, startY, endX, endY);
```

The arrowhead uses `Math.atan2` to get the angle of the line, then branches off at ±25 degrees:

```java
double angle = Math.atan2(endY - startY, endX - startX);
double arrowLength = 10.0;
double arrowAngle  = Math.toRadians(25.0);

double firstX  = endX - arrowLength * Math.cos(angle - arrowAngle);
double firstY  = endY - arrowLength * Math.sin(angle - arrowAngle);
double secondX = endX - arrowLength * Math.cos(angle + arrowAngle);
double secondY = endY - arrowLength * Math.sin(angle + arrowAngle);

Line firstSide  = new Line(endX, endY, firstX, firstY);
Line secondSide = new Line(endX, endY, secondX, secondY);
```

All three lines go into the pane, and you've got an arrow.

The design principle we discussed here was to keep three things separate: the data (the two lists), the matching logic (the algorithm), and the presentation (JavaFX nodes and positions). The matching logic in particular should be testable without needing to launch the GUI at all.

---

## Reproducing Software Failures

We spent some time on something that doesn't get enough attention — how to write a useful bug report.

A reproducible failure is one you can trigger reliably by following a known set of steps. That reproducibility is what makes debugging possible. If you can't consistently reproduce the problem, you can't confirm what's causing it or verify that a fix actually worked.

A good report includes:

- A clear one-line summary of the problem.
- Exact steps to reproduce it.
- What you expected to happen.
- What actually happened instead.
- The relevant input, error messages, and log output.
- Version information — Java, JavaFX, OS, library versions.
- Any environment or configuration details that might matter.

A minimal example looks something like:

```text
Summary:
The application crashes on right-click when the canvas is empty.

Steps to reproduce:
1. Start the application.
2. Open a new empty canvas.
3. Right-click near the center.

Expected: A circle appears at the clicked position.
Actual: The app closes with a NullPointerException.

Environment: Java 21, JavaFX 21, macOS
```

Once you can reproduce the failure, the workflow is: observe it, reduce it to the smallest failing case, inspect the program state, find the root cause, write a regression test, apply the fix, and run the test again to confirm it's resolved.

For intermittent failures — the harder ones — logs, timestamps, thread information, and random seeds can help narrow down when and why they occur.

---

## FXML: Separating Interface from Logic

The last topic was FXML, which is JavaFX's XML-based way of defining user interfaces. Instead of writing all your UI setup in Java code, you describe the layout in an FXML file and keep the behavior in a separate controller class.

A simple FXML file looks like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.Button?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.VBox?>

<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.example.MainController"
      alignment="CENTER"
      spacing="12">
    <Label fx:id="messageLabel" text="Ready" />
    <Button text="Continue" onAction="#handleContinue" />
</VBox>
```

The controller handles the logic. `@FXML` annotations connect the Java fields and methods to the elements defined in the file:

```java
public class MainController {

    @FXML
    private Label messageLabel;

    @FXML
    private void handleContinue() {
        messageLabel.setText("Button selected");
    }
}
```

Loading it is a single call:

```java
FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
Parent root = loader.load();
```

The main benefit is separation of concerns — the structure of the interface lives in the FXML file, and the behavior lives in the controller. That also makes it possible to use a visual design tool like Scene Builder to edit the layout without touching any Java code. For large interfaces with many components, this split keeps things much more manageable.

That said, FXML isn't mandatory. Small or highly dynamic interfaces can still be built entirely in Java code — it comes down to what's easier to maintain for the specific situation.

---

## Summary

A lot of ground covered today:

- Terminals and GUIs each have strengths — terminals for automation and remote work, GUIs for discoverability and visual interaction.
- SSH provides encrypted terminal access to remote machines and is the standard tool for server administration.
- Finding list matches requires clear rules around equality, case sensitivity, and duplicates before writing any code.
- A `HashMap` speeds up matching for large lists by turning inner-loop lookups into direct map queries.
- Arrow drawing between matched items uses `Math.atan2` to calculate the shaft angle and branches off for the arrowhead.
- Matching logic should be separated from the JavaFX presentation layer so it can be tested independently.
- A reproducible bug report with exact steps, expected vs actual behavior, and environment details is what makes debugging tractable.
- FXML separates interface structure from controller logic, which keeps large JavaFX applications organized and opens the door to visual design tools.
