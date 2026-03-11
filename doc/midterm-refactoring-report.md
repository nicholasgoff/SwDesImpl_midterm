# Midterm Refactoring Report (Track B)

## Scope and Tooling

- Repository: `SwDesImpl_midterm`
- Branch: `refactoring`
- PMD command: `./gradlew.bat pmdMain`
- PMD ruleset file: `pmd-rules.xml`
- Focused smell rules:
  - `ExcessiveClassLength` with `minimum=150`
  - `ExcessiveParameterList` with `minimum=4`
  - `GodClass` (included for analysis; no violations detected)

Note: `pmdMain` is configured with `ignoreFailures=false`, so Gradle fails when violations exist, but the XML report is still generated at `build/reports/pmd/main.xml`.

## Part A - Code Smell Analysis

Baseline PMD run produced 23 violations in main code.

### A1. Large Class / God Class Instance 1

- PMD Rule Name: `ExcessiveClassLength`
- Location: `src/main/java/nl/tudelft/jpacman/Launcher.java` (class `Launcher`, baseline violation lines 30-213)
- Metric Value Reported: class spans 184 lines (line range 30-213)
- Threshold: 150 lines
- Smell Category: Large Class / God Class
- Design Problem Evaluation:
  - The class performed game launching, level loading, dependency/factory wiring, keyboard mapping, and player selection.
  - This is a Single Responsibility Principle issue: startup orchestration and object graph construction were tightly coupled.

### A2. Large Class / God Class Instance 2

- PMD Rule Name: `ExcessiveClassLength`
- Location: `src/main/java/nl/tudelft/jpacman/level/MapParser.java` (class `MapParser`, baseline violation lines 22-261)
- Metric Value Reported: class spans 240 lines (line range 22-261)
- Threshold: 150 lines
- Smell Category: Large Class / God Class
- Design Problem Evaluation:
  - The class combined map text validation, stream I/O, text-to-grid conversion, and level entity placement.
  - This reduces cohesion and increases coupling between parsing policy and game object creation.

### A3. Long Parameter List Instance 1

- PMD Rule Name: `ExcessiveParameterList`
- Location: `src/main/java/nl/tudelft/jpacman/level/MapParser.java` method `makeGrid` (baseline violation lines 79-80)
- Metric Value Reported: 6 parameters (`char[][] map, int width, int height, Square[][] grid, List<Ghost> ghosts, List<Square> startPositions`)
- Threshold: 4 parameters
- Smell Category: Long Parameter List
- Design Problem Evaluation:
  - The parameters represent a data clump (grid and placement state) that had to be passed together.
  - This increased call-site complexity and made the method error-prone.

### A4. Long Parameter List Instance 2

- PMD Rule Name: `ExcessiveParameterList`
- Location: `src/main/java/nl/tudelft/jpacman/ui/BoardPanel.java` method `render` (baseline violation line 114)
- Metric Value Reported: 6 parameters (`Square square, Graphics graphics, int x, int y, int width, int height`)
- Threshold: 4 parameters
- Smell Category: Long Parameter List
- Design Problem Evaluation:
  - Four primitive coordinate values are a classic primitive-obsession pattern.
  - The signature obscured intent and made rendering calls harder to read and maintain.

## Part B - Refactoring

A minimum of 4 refactorings (2 per smell category) were completed.

### B1. Long Parameter Refactoring #1

- Target: `MapParser.makeGrid`
- Technique: Introduce Parameter Object (`MapBuildState`)
- Commit: `8ccf56b`
- Why Appropriate:
  - Encapsulates related mutable build state into one domain object.
  - Improves readability and reduces argument ordering mistakes.
- Metrics:
  - Before: 6 parameters
  - After: 2 parameters (`char[][] map, MapBuildState buildState`)
  - PMD Result: warning removed for `makeGrid` in post-refactor PMD report

### B2. Long Parameter Refactoring #2

- Target: `BoardPanel.render(Square, Graphics, int, int, int, int)`
- Technique: Introduce Parameter Object (use `java.awt.Rectangle`)
- Commit: `25bce25`
- Why Appropriate:
  - Bundles geometric bounds into a single object with explicit semantics.
  - Simplifies call sites and clarifies rendering intent.
- Metrics:
  - Before: 6 parameters
  - After: 3 parameters (`Square, Graphics, Rectangle`)
  - PMD Result: warning removed for `BoardPanel.render` in post-refactor PMD report

### B3. Large Class Refactoring #1

- Target: `MapParser`
- Technique: Extract Class (`MapTextParser` and `MapBuildState`)
- Commit: `71033fc`
- Why Appropriate:
  - Separates text validation/conversion and build state from level assembly logic.
  - Improves cohesion and information hiding around parsing concerns.
- Metrics:
  - Before: PMD class span lines 22-261
  - After: PMD class span lines 18-200
  - Delta: 61 fewer lines in class span
  - PMD Status: still above threshold 150, but significantly improved and responsibilities are cleaner

### B4. Large Class Refactoring #2

- Target: `Launcher`
- Technique: Extract Class (`LauncherFactoryProvider`)
- Commit: `28f97b1`
- Why Appropriate:
  - Moves dependency/factory wiring out of startup orchestration.
  - Reduces coupling between UI control flow and object graph construction.
- Metrics:
  - Before: PMD class span lines 30-213
  - After: file length 150 lines; `ExcessiveClassLength` warning no longer present
  - PMD Status: large-class warning resolved for `Launcher`

### Build and Behavior Verification

- `./gradlew.bat test` executed successfully after refactoring steps.
- Observable behavior preserved at regression-test level (existing suite passes).

## Part C - Reflection

### C1. Which PMD warnings were not addressed, and why?

- Remaining warnings include:
  - `ExcessiveClassLength` in `Level`, `CollisionInteractionMap`, `Navigation`, `AnimatedSprite`, and still `MapParser`.
  - Multiple `ExcessiveParameterList` warnings in the sprite API (`Sprite`, `ImageSprite`, `EmptySprite`, `AnimatedSprite`) and others.
- Reasons:
  - Some signatures are part of stable rendering abstractions; changing them broadly would require cross-cutting API redesign and larger regression risk.
  - `Level` and `CollisionInteractionMap` are core gameplay classes; deep decomposition would exceed this iteration's safe scope.

### C2. Did you identify smells not detected by PMD?

- Yes.
- Potential smells not directly flagged here:
  - Feature envy risk in control-flow heavy classes where behavior depends heavily on collaborator internals.
  - Temporal coupling in startup/wiring code paths (ordering dependencies matters).
  - API rigidity in rendering interfaces caused by primitive-heavy methods.

### C3. Which refactoring had the most significant architectural impact?

- Extracting `LauncherFactoryProvider` from `Launcher`.
- It created a clearer architectural boundary between orchestration (launch flow) and dependency construction, reducing coupling and improving testability/extensibility.

### C4. What trade-offs did the refactoring introduce?

- Added classes and indirection increase navigation overhead for new contributors.
- `MapParser` now coordinates with helper classes, which improves cohesion but distributes logic across files.
- These trade-offs are acceptable because they improve SRP adherence, reduce parameter/data clumping, and make responsibilities clearer.

## Incremental Commit Trail

1. `c6ed409` - Configure PMD smell ruleset for midterm analysis
2. `8ccf56b` - Refactor `MapParser.makeGrid` with parameter object
3. `25bce25` - Introduce `Rectangle` parameter in `BoardPanel` rendering
4. `71033fc` - Extract map text parsing from `MapParser`
5. `28f97b1` - Extract `Launcher` dependency wiring into helper class
