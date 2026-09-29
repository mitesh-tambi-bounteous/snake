summary: |
  The codebase currently has no game engine at all — only `SnakeGameWindow` (an empty JFrame)
  and `Main`. This story introduces the domain model needed to prove the one rule the acceptance
  criteria care about — the snake's length and the score change ONLY when the snake's head moves
  onto a food item, and change for no other reason (elapsed time/ticks, self-collision,
  wall-collision, or a game-status transition such as GAME_OVER) — AND, per reviewer direction,
  folds in the two follow-on decisions previously flagged as open questions so the feature is
  actually playable end-to-end within this story: (1) a food-respawn strategy so a new food cell
  is placed after each eat, and (2) wiring the model into the existing Swing window with a game
  loop timer and arrow-key input. The model itself (`Position`, `Direction`, `GameStatus`, `Food`,
  `Snake`, `FoodSpawner`, `Game`) stays UI-independent with a single `Game.tick(Direction)` step
  function; `GamePanel` and a modified `SnakeGameWindow` are the thin Swing layer on top. No
  time-based growth, bonus-item mechanics, or level system are introduced — those are explicitly
  out of scope per the story description.
scope:
  - description: |
      Add `Position`, a simple immutable grid coordinate.
      ```java
      public record Position(int x, int y) {}
      ```
    files:
      - src/main/java/com/snake/Position.java
    rationale: |
      Coordinate type shared by Snake, Food, FoodSpawner, and Game's bounds/collision checks.
  - description: |
      Add `Direction`, the four cardinal movement deltas.
      ```java
      public enum Direction {
          UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0);
          public final int dx;
          public final int dy;
          Direction(int dx, int dy) { this.dx = dx; this.dy = dy; }
      }
      ```
    files:
      - src/main/java/com/snake/Direction.java
    rationale: |
      Game.tick needs a direction to compute the candidate next head position; GamePanel maps
      arrow keys onto this same enum.
  - description: |
      Add `GameStatus`, the two states relevant to this story (running vs. ended by collision).
      ```java
      public enum GameStatus { RUNNING, GAME_OVER }
      ```
    files:
      - src/main/java/com/snake/GameStatus.java
    rationale: |
      AC5/AC6 require proving that a game-status change (collision ending the game) does not
      itself alter length/score, and that further ticks after that change are no-ops.
  - description: |
      Add `Food`, an immutable holder for the current food position.
      ```java
      public record Food(Position position) {}
      ```
    files:
      - src/main/java/com/snake/Food.java
    rationale: |
      Represents the single food item the snake's head can move onto.
  - description: |
      Add `Snake`, owning the body segments (head first), movement/growth, and both collision
      predicates needed by movement (excluding tail) and by food spawning (including tail).
      ```java
      public final class Snake {
          public Snake(List<Position> initialBody) { ... }
          public Position head() { ... }
          public int length() { ... }
          public List<Position> segments() { ... }
          public boolean collidesExcludingTail(Position candidate) { ... }
          public boolean occupies(Position candidate) { ... }
          public void advance(Position newHead, boolean grow) {
              body.addFirst(newHead);
              if (!grow) body.removeLast();
          }
      }
      ```
    files:
      - src/main/java/com/snake/Snake.java
    rationale: |
      `advance` is the single place length changes: growth happens only when the caller (Game)
      passes grow=true, which only happens on eating food. `collidesExcludingTail` checks the
      body excluding the current tail cell, since the tail vacates that cell on a non-eating move
      — the standard Nokia Snake self-collision rule. `occupies` (whole body, tail included) is
      used by `FoodSpawner` so a respawned food item is never placed on a cell the snake still
      occupies. `segments()` is read by `GamePanel` for rendering.
  - description: |
      Add `FoodSpawner`, picking the next food cell after one is eaten.
      ```java
      public final class FoodSpawner {
          private final Random random;
          public FoodSpawner(Random random) { this.random = random; }
          public Position spawn(int width, int height, Snake snake) {
              Position candidate;
              do {
                  candidate = new Position(random.nextInt(width), random.nextInt(height));
              } while (snake.occupies(candidate));
              return candidate;
          }
      }
      ```
    files:
      - src/main/java/com/snake/FoodSpawner.java
    rationale: |
      Resolves the previously-open "food respawn strategy" question: after eating, `Game` needs
      a next food cell that is never on top of the snake. `Random` is injected so tests can
      script a deterministic sequence of candidates (including one that must be rejected because
      it lands on the snake) rather than relying on a live PRNG.
  - description: |
      Add `Game`, the orchestrator: bounds/wall check, self-collision check, eat-food detection,
      score/status, and food respawn on eating.
      ```java
      public final class Game {
          public Game(int width, int height, Snake snake, Food food, FoodSpawner foodSpawner) { ... }
          public void tick(Direction direction) {
              if (status != GameStatus.RUNNING) return;
              Position head = snake.head();
              Position next = new Position(head.x() + direction.dx, head.y() + direction.dy);
              boolean hitsWall = next.x() < 0 || next.y() < 0 || next.x() >= width || next.y() >= height;
              if (hitsWall || snake.collidesExcludingTail(next)) {
                  status = GameStatus.GAME_OVER;
                  return;
              }
              boolean ateFood = next.equals(food.position());
              snake.advance(next, ateFood);
              if (ateFood) {
                  score++;
                  food = new Food(foodSpawner.spawn(width, height, snake));
              }
          }
          public List<Position> snakeSegments() { return snake.segments(); }
          public Position foodPosition() { return food.position(); }
          public int score() { return score; }
          public int snakeLength() { return snake.length(); }
          public GameStatus status() { return status; }
      }
      ```
    files:
      - src/main/java/com/snake/Game.java
    rationale: |
      Single method (`tick`) is the one place per-step behavior is decided, which the test-first
      suite exercises directly to prove each AC; `snakeSegments()`/`foodPosition()` are the read
      model `GamePanel` renders from, keeping Swing out of the game-state class entirely.
  - description: |
      Add `GamePanel`, the Swing rendering/input/loop-tick surface on top of `Game`.
      ```java
      public final class GamePanel extends JPanel implements ActionListener, KeyListener {
          private final Game game;
          private final int cellSize;
          private Direction direction = Direction.RIGHT;

          public GamePanel(Game game, int cellSize) {
              this.game = game;
              this.cellSize = cellSize;
              setFocusable(true);
              addKeyListener(this);
          }

          @Override public void actionPerformed(ActionEvent e) {
              game.tick(direction);
              repaint();
          }

          @Override public void keyPressed(KeyEvent e) {
              Direction mapped = mapKey(e.getKeyCode());
              if (mapped != null) direction = mapped;
          }

          static Direction mapKey(int keyCode) {
              switch (keyCode) {
                  case KeyEvent.VK_UP: return Direction.UP;
                  case KeyEvent.VK_DOWN: return Direction.DOWN;
                  case KeyEvent.VK_LEFT: return Direction.LEFT;
                  case KeyEvent.VK_RIGHT: return Direction.RIGHT;
                  default: return null;
              }
          }

          @Override protected void paintComponent(Graphics g) {
              super.paintComponent(g);
              g.setColor(Color.GREEN);
              for (Position segment : game.snakeSegments()) {
                  g.fillRect(segment.x() * cellSize, segment.y() * cellSize, cellSize, cellSize);
              }
              g.setColor(Color.RED);
              Position food = game.foodPosition();
              g.fillRect(food.x() * cellSize, food.y() * cellSize, cellSize, cellSize);
          }

          @Override public void keyTyped(KeyEvent e) {}
          @Override public void keyReleased(KeyEvent e) {}
      }
      ```
    files:
      - src/main/java/com/snake/GamePanel.java
    rationale: |
      `JPanel` (unlike `JFrame`/`Window`) does not throw `HeadlessException` on construction, so
      `mapKey` and `actionPerformed` (the one-tick-per-timer-fire logic) are directly unit
      testable in a headless CI environment; only rendering pixels is untestable and is kept to a
      trivial `fillRect` loop with no branching logic of its own.
  - description: |
      Modify `SnakeGameWindow` to build a `Game`, wrap it in a `GamePanel`, and drive it with a
      `javax.swing.Timer`; extract initial-game construction into a package-private static method
      so it is testable without instantiating the `JFrame` itself.
      ```java
      public class SnakeGameWindow extends JFrame {
          static final int CELL_SIZE = 20;
          static final int GRID_SIZE = 20;
          static final int TICK_INTERVAL_MS = 150;

          public SnakeGameWindow() {
              super("Snake");
              setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
              setSize(600, 600);

              Game game = createInitialGame();
              GamePanel panel = new GamePanel(game, CELL_SIZE);
              add(panel);
              new Timer(TICK_INTERVAL_MS, panel).start();
              panel.requestFocusInWindow();
          }

          static Game createInitialGame() {
              Snake snake = new Snake(List.of(
                  new Position(5, 5), new Position(4, 5), new Position(3, 5)));
              Food food = new Food(new Position(10, 10));
              return new Game(GRID_SIZE, GRID_SIZE, snake, food, new FoodSpawner(new Random()));
          }
      }
      ```
    files:
      - src/main/java/com/snake/SnakeGameWindow.java
    rationale: |
      `createInitialGame()` is package-private and static so `SnakeGameWindowTest` can call it
      directly and assert on the resulting `Game` without constructing the `JFrame` (which throws
      `HeadlessException` on a headless CI runner) — matching the existing repo pattern in
      `SwingUiTest`, which already avoids instantiating `SnakeGameWindow`/`Main` for the same
      reason.
  - description: |
      Test-first suite covering all six ACs against `Game`/`Snake`, plus the two folded-in
      concerns: `FoodSpawner` respawn behavior and the `GamePanel`/`SnakeGameWindow` wiring.
    files:
      - src/test/java/com/snake/GameTest.java
      - src/test/java/com/snake/FoodSpawnerTest.java
      - src/test/java/com/snake/GamePanelTest.java
      - src/test/java/com/snake/SnakeGameWindowTest.java
    rationale: |
      Written before the production classes above; each fails to compile/run until its
      corresponding class exists, then passes once the minimal logic lands.
tests:
  - |
    AC1 (length +1 on eating) and AC2 (score +1 on eating), in `GameTest.java`:
    ```java
    @Test
    void tickOntoFoodGrowsSnakeByOneSegment() {
        Snake snake = new Snake(List.of(new Position(5, 5), new Position(4, 5), new Position(3, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(6, 5)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertEquals(4, game.snakeLength());
    }

    @Test
    void tickOntoFoodIncreasesScoreByOne() {
        Snake snake = new Snake(List.of(new Position(5, 5), new Position(4, 5), new Position(3, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(6, 5)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertEquals(1, game.score());
    }
    ```
  - |
    AC3 (length unchanged when time elapses without eating) and AC4 (score unchanged), in
    `GameTest.java`:
    ```java
    @Test
    void tickWithoutReachingFoodLeavesSnakeLengthUnchanged() {
        Snake snake = new Snake(List.of(new Position(5, 5), new Position(4, 5), new Position(3, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(9, 9)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertEquals(3, game.snakeLength());
    }

    @Test
    void tickWithoutReachingFoodLeavesScoreUnchanged() {
        Snake snake = new Snake(List.of(new Position(5, 5), new Position(4, 5), new Position(3, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(9, 9)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertEquals(0, game.score());
    }
    ```
  - |
    AC5/AC6 wall-collision case: length and score unchanged when the head moves into a wall.
    ```java
    @Test
    void tickIntoWallLeavesSnakeLengthAndScoreUnchanged() {
        Snake snake = new Snake(List.of(new Position(9, 5), new Position(8, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(0, 0)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertEquals(2, game.snakeLength());
        assertEquals(0, game.score());
        assertEquals(GameStatus.GAME_OVER, game.status());
    }
    ```
  - |
    AC5/AC6 self-collision case: length and score unchanged when the head moves onto the snake's
    own body.
    ```java
    @Test
    void tickIntoSelfLeavesSnakeLengthAndScoreUnchanged() {
        Snake snake = new Snake(List.of(
            new Position(5, 5), new Position(6, 5), new Position(6, 6),
            new Position(5, 6), new Position(4, 6)));
        Game game = new Game(10, 10, snake, new Food(new Position(0, 0)), new FoodSpawner(new Random(42)));
        game.tick(Direction.DOWN);
        assertEquals(5, game.snakeLength());
        assertEquals(0, game.score());
        assertEquals(GameStatus.GAME_OVER, game.status());
    }
    ```
  - |
    AC5/AC6 game-state-change case: once status has already changed to GAME_OVER, a further tick
    is a no-op and does not change length or score.
    ```java
    @Test
    void tickAfterGameOverLeavesSnakeLengthAndScoreUnchanged() {
        Snake snake = new Snake(List.of(new Position(9, 5), new Position(8, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(0, 0)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertEquals(GameStatus.GAME_OVER, game.status());
        game.tick(Direction.LEFT);
        assertEquals(2, game.snakeLength());
        assertEquals(0, game.score());
    }
    ```
  - |
    Folded-in concern: food respawn never lands on the snake's body, in `FoodSpawnerTest.java`,
    using a scripted `Random` that first offers an occupied cell then a free one:
    ```java
    @Test
    void spawnRetriesUntilCandidateIsUnoccupied() {
        Snake snake = new Snake(List.of(new Position(0, 0), new Position(1, 0)));
        Random scripted = new Random() {
            private final int[] xs = {0, 2};
            private final int[] ys = {0, 2};
            private int call = 0;
            @Override public int nextInt(int bound) {
                int value = (call % 2 == 0) ? xs[call / 2] : ys[call / 2];
                call++;
                return value;
            }
        };
        Position spawned = new FoodSpawner(scripted).spawn(5, 5, snake);
        assertEquals(new Position(2, 2), spawned);
    }
    ```
  - |
    Folded-in concern: `Game` actually calls the spawner on eating, so the new food cell differs
    from the eaten one and is never on the snake, in `GameTest.java`:
    ```java
    @Test
    void eatingFoodReplacesItWithASpawnedCellNotOnTheSnake() {
        Snake snake = new Snake(List.of(new Position(5, 5), new Position(4, 5), new Position(3, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(6, 5)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertTrue(game.snakeSegments().stream().noneMatch(p -> p.equals(game.foodPosition())));
        assertNotEquals(new Position(6, 5), game.foodPosition());
    }
    ```
  - |
    Folded-in concern: arrow keys map to `Direction` and a timer fire advances the game by one
    tick, in `GamePanelTest.java`:
    ```java
    @Test
    void mapKeyTranslatesArrowKeysToDirectionsAndIgnoresOthers() {
        assertEquals(Direction.UP, GamePanel.mapKey(KeyEvent.VK_UP));
        assertEquals(Direction.DOWN, GamePanel.mapKey(KeyEvent.VK_DOWN));
        assertEquals(Direction.LEFT, GamePanel.mapKey(KeyEvent.VK_LEFT));
        assertEquals(Direction.RIGHT, GamePanel.mapKey(KeyEvent.VK_RIGHT));
        assertNull(GamePanel.mapKey(KeyEvent.VK_SPACE));
    }

    @Test
    void actionPerformedAdvancesGameByOneTick() {
        Snake snake = new Snake(List.of(new Position(5, 5), new Position(4, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(9, 9)), new FoodSpawner(new Random(42)));
        GamePanel panel = new GamePanel(game, 20);
        panel.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_FIRST, "tick"));
        assertEquals(new Position(6, 5), game.snakeSegments().get(0));
    }
    ```
  - |
    Folded-in concern: the window wires up a valid, running initial game without requiring a
    display, in `SnakeGameWindowTest.java`:
    ```java
    @Test
    void createInitialGameStartsRunningWithZeroScore() {
        Game game = SnakeGameWindow.createInitialGame();
        assertEquals(GameStatus.RUNNING, game.status());
        assertEquals(0, game.score());
        assertEquals(3, game.snakeLength());
    }
    ```
assumptions_or_open_questions:
  - |
    "the level/game state changes" in AC5/AC6 is treated as: a game-status transition (RUNNING ->
    GAME_OVER caused by a collision) must not itself change length/score, and once GAME_OVER,
    further ticks are no-ops. No level system exists or is introduced, since the story description
    says no other growth/score triggers are in scope.
  - |
    Self-collision uses the standard "excluding current tail" rule (the tail cell vacates on a
    non-growing move), matching classic Nokia Snake behavior referenced in the story description.
  - |
    Per reviewer direction, food-respawn strategy and Swing/keyboard/timer wiring — originally
    flagged as open questions/out-of-scope — are now implemented as part of this story so the
    feature is playable end-to-end, not left as a bare model.
  - |
    Grid size (20x20), cell size (20px), and tick interval (150ms) are arbitrary reasonable
    defaults for `SnakeGameWindow.createInitialGame()`/the `Timer`; no AC or existing file
    specifies them, so these constants are a plan-time choice a reviewer may want to adjust.
  - |
    Direction reversal into the snake's own neck (e.g. pressing DOWN while moving UP) is not
    specifically guarded against in `GamePanel.keyPressed` — it is left to fall through to the
    existing self-collision check in `Game.tick` on the following move, since no AC calls out
    key-input validation and this still preserves "no length/score change on self-collision."
package_dependencies: []
notes: |
  `build.gradle` already declares `junit-jupiter:5.10.3` with `useJUnitPlatform()`, so no new test
  infra or dependency is needed; everything used (`java.util.Random`, `javax.swing.*`) is already
  in the JDK. `Main.java` is untouched — it still just constructs `SnakeGameWindow` and calls
  `setVisible(true)`. `SnakeGameWindowTest`/`GamePanelTest` deliberately avoid instantiating
  `SnakeGameWindow` (a `JFrame`) directly, mirroring the existing `SwingUiTest`, since `Window`
  subclasses throw `HeadlessException` on construction on a headless CI runner; `GamePanel`
  (a `JPanel`) has no such restriction, so its tick/key-mapping logic is exercised directly.

  ```mermaid
  flowchart TD
      Main[Main.java untouched] --> Window[SnakeGameWindow.java modified: builds Game + GamePanel + Timer]
      Window --> Panel[GamePanel.java new: renders, maps keys, ticks Game]
      Panel -->|tick per Timer fire| Game[Game.java modified: tick, snakeSegments, foodPosition]
      Game --> Snake[Snake.java modified: occupies, collidesExcludingTail, segments]
      Game --> Spawner[FoodSpawner.java new: next food cell on eat]
      Spawner --> Snake

      classDef touched fill:#f96,color:#000
      class Window,Panel,Game,Snake,Spawner touched
  ```
review_focus: |
  In scope: the UI-independent `Game`/`Snake`/`Food`/`FoodSpawner` model proving growth/score
  change only on eating food (never on elapsed ticks, self-collision, wall-collision, or a
  post-collision GAME_OVER state), PLUS — per explicit reviewer request — the food-respawn
  strategy and the Swing wiring (`GamePanel`, modified `SnakeGameWindow`) that make the game
  actually playable. Out of scope: time-based growth, bonus items, and any level system (per the
  story description). The riskiest areas are (1) `Snake.collidesExcludingTail`'s off-by-one
  potential (covered by the wall/self-collision tests), and (2) the headless-safe test strategy
  for the Swing layer — `SnakeGameWindowTest`/`GamePanelTest` intentionally never instantiate the
  `JFrame` itself and instead exercise extracted package-private/static logic, so don't flag the
  absence of a "does the window actually show" test as a gap; it's a deliberate, CI-safe choice
  consistent with the existing `SwingUiTest` pattern.
