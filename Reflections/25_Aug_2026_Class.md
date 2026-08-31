# Class Reflection — August 25, 2026

## Topics Covered

- Git hygiene — syncing the repo before starting work
- The purpose and structure of a project `README.md`
- Maven support in IntelliJ IDEA
- The role and contents of `pom.xml`
- Accessibility considerations in Swing applications
- Why multithreading matters for responsive UIs
- Swing's Event Dispatch Thread (EDT) and thread safety

---

## Starting with Git: Pull First, Ask Questions Later

One habit that was emphasized today is to always run `git pull` before touching anything in the project. It sounds obvious, but skipping this step is how messy merge conflicts happen. If someone else has pushed changes and you start writing on top of an old version, you're setting yourself up for trouble.

Before even pulling, it's good practice to run:

```sh
git status
git branch --show-current
```

This tells you where you are and whether you have any pending changes. It's a simple routine, but it saves a lot of headaches.

---

## README: The First Thing a New Developer Reads

A `README.md` file is basically the front door of a project. When someone new joins or looks at your repo, the README is what orients them. Without it, they're left guessing how to set things up, what the project does, or how to run it.

A solid README typically covers:

- What the project is and what problem it solves
- What you need installed before you start
- Step-by-step setup and installation
- How to build and run the project
- How to run tests
- A brief look at the folder/file structure
- Examples of how to use the software
- Contribution guidelines and license info

The goal is to make it easy for anyone — including your future self — to pick up where things left off without needing to dig through the code first.

---

## Maven and IntelliJ IDEA

Maven is a build and dependency management tool that standardizes how Java projects are built. IntelliJ IDEA has built-in Maven support through its Maven tool window, which lets you:

- Sync or reload the Maven project after changes
- Download dependencies automatically
- Run lifecycle phases like `clean`, `compile`, `test`, and `package`
- Manage plugins and profiles
- See the full dependency tree

The nice thing is that what you can do in the IDE's Maven panel, you can also do from the terminal — so the behavior stays consistent regardless of environment.

## What `pom.xml` Actually Does

`pom.xml` stands for **Project Object Model**, and it's the configuration backbone of any Maven project. Everything Maven needs to know about the project lives here.

That includes:

- The project's `groupId`, `artifactId`, and `version` — essentially its identity
- The Java version to compile against
- All external library dependencies
- Build plugins and how they're configured
- The packaging format (like JAR or WAR)
- Test settings and resource handling
- Build profiles for different environments

Getting `pom.xml` right means anyone can clone the repo and build it the same way, every time.

---

## Accessibility in Swing: It Requires More Than JPanel

`JPanel` has built-in support for Java's accessibility API, which allows assistive technologies (like screen readers) to interact with the interface. But just using `JPanel` doesn't make an app accessible — it's a starting point, not a finish line.

To actually build an accessible Swing application, you need to:

- Give components meaningful accessible names and descriptions
- Pair labels with their corresponding input fields
- Make sure everything can be navigated with a keyboard
- Show clear focus indicators so users know where they are
- Use colors with enough contrast, and never rely on color alone to convey meaning
- Test with actual accessibility tools

Accessibility works best when it's part of the design from day one, not something patched in at the end.

---

## Multithreading: Why It Matters

Multithreading lets a program run more than one sequence of work at the same time. This is especially relevant in applications that:

- Need to stay responsive while doing something in the background
- Handle network or file I/O that could block
- Run tasks that are independent of each other
- Can benefit from multiple CPU cores

That said, multithreading isn't a magic fix. It adds complexity — things like race conditions, deadlocks, and harder-to-debug behavior become real concerns once multiple threads are involved.

## Why a Single-Threaded GUI Freezes

Swing processes all interface events — mouse clicks, keyboard input, repaints — on a single thread called the **Event Dispatch Thread (EDT)**. If you run something slow on the EDT, it can't process anything else until that work is done, which makes the entire UI appear frozen.

Common culprits include:

- Network calls or API requests
- File reading or writing
- Large database queries
- Heavy computations
- Any kind of sleep or blocking wait

The fix is to keep the EDT free — run long operations in a background thread and let the EDT do what it's meant to do.

## Thread Safety in Swing: `JFrame` Isn't Thread-Safe

Swing components, including `JFrame`, are not thread-safe. If multiple threads try to read from or write to a Swing component at the same time, the result can be unpredictable — visual glitches, inconsistent state, or outright crashes.

The rule is: create and update Swing components only on the EDT. `SwingUtilities.invokeLater` is the standard way to schedule that work:

```java
SwingUtilities.invokeLater(() -> {
    JFrame frame = new JFrame("My App");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(500, 400);
    frame.setVisible(true);
});
```

For background work that eventually needs to update the UI, `SwingWorker` is the right tool. It runs heavy work off the EDT and safely delivers results back:

```java
SwingWorker<String, Void> worker = new SwingWorker<>() {
    @Override
    protected String doInBackground() throws Exception {
        return performLongRunningTask();
    }

    @Override
    protected void done() {
        try {
            resultLabel.setText(get());
        } catch (Exception e) {
            resultLabel.setText("Something went wrong.");
        }
    }
};

worker.execute();
```

`doInBackground()` runs on a background thread; `done()` is called automatically on the EDT once the work finishes.

---

## Summary

A few things really stuck with me from today:

- Pull before you code — always.
- A README is how you make a project usable by others (and by yourself in six months).
- Maven and `pom.xml` are what make builds repeatable and consistent.
- Swing's accessibility support is a foundation, but real accessibility takes intentional effort.
- Heavy work on the EDT will freeze your app — keep it light.
- Use `SwingUtilities.invokeLater` for UI setup and `SwingWorker` for background tasks.
