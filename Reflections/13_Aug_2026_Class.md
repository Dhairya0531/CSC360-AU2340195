# Lab-1 Reflection: Maven-Based Swing Application

## Project Overview
Created a Maven-based Java Swing application that displays a window with a centered bordered square. This was my first hands-on experience setting up a complete Maven project structure with proper dependency management and package organization.

## Learning Outcomes

### 1. Maven Project Structure
- Successfully set up a standard Maven project with proper directory hierarchy:
  - `pom.xml` for project configuration and build management
  - `src/main/java/com/example/` for Java source files
  - `target/` directory for compiled output
- Learned the importance of following Maven conventions for seamless project management

### 2. Swing GUI Framework
- Used `JFrame` to create the main application window
- Implemented custom `JPanel` for drawing graphics
- Understood the paintComponent() method for custom rendering
- Used `Graphics` API to draw shapes (rectangle with black border)

### 3. Code Organization
- Refactored code into multiple classes for better organization:
  - `Main.java` - Entry point with main() method
  - `App.java` - Custom JFrame class handling window setup
  - `SquarePanel.java` - Custom JPanel class for drawing logic
- This separation improved code readability and maintainability

### 4. Build and Execution
- Compiled the project using `mvn clean compile`
- Executed the application using `mvn exec:java -Dexec.mainClass="com.example.Main"`
- Learned to troubleshoot build issues (incorrect main class reference)

## Technical Details

### Key Implementation Features:
- Window size: 400x400 pixels
- Square dimensions: 2/3 of the smaller window dimension for responsive scaling
- Centered positioning using calculated x and y offsets
- Black border with white background

### Maven Configuration:
- Java 11 as target/source version
- Proper manifest configuration for JAR execution
- Exec Maven Plugin for direct Java execution from command line

## Challenges Faced

1. **Incorrect Main Class Reference**: Initially tried to run `com.example.App` when the main method was actually in `com.example.Main`
   - Solution: Updated the pom.xml mainClass configuration

2. **Project Structure**: Understanding the Maven directory layout took initial setup but became clear once established

## Key Takeaways

1. **Maven Simplifies Project Management**: Instead of managing dependencies and classpaths manually, Maven handles everything through pom.xml
2. **Separation of Concerns**: Splitting code into multiple classes (Main, App, SquarePanel) made the project more maintainable
3. **Swing for GUI Development**: Swing provides a robust framework for building cross-platform desktop applications in Java
4. **Importance of Proper Configuration**: Correct pom.xml configuration and directory structure are crucial for smooth builds

## Future Improvements

- Add event listeners for mouse/keyboard interactions
- Implement dynamic resizing with proper square proportions
- Add configuration options for square color, border thickness, and background
- Create unit tests for the application
