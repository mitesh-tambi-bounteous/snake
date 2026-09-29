summary: |
  Wall-collision hard-stop, self-collision hard-stop, and "no further state changes after
  game over" (ACs 1-5) are already implemented and covered by tests in `Game.tick`
  (`src/main/java/com/snake/Game.java`) and `Snake.collidesExcludingTail`
  (`src/main/java/com/snake/Snake.java`), verified by `GameTest.tickIntoWallLeavesSnakeLengthAndScoreUnchanged`,
  `GameTest.tickIntoSelfLeavesSnakeLengthAndScoreUnchanged`, and
  `GameTest.tickAfterGameOverLeavesSnakeLengthAndScoreUnchanged`. This plan closes the
  remaining gap: the game-over screen must actually display "Game Over", the current score,
  and a restart option (ACs 6-8), and activating that restart option must start a new game
  with the snake back at its starting position and score reset to zero (ACs 9-10). It adds a
  `Game.restart()` / `Snake.reset(...)` capability, wires a restart key binding into
  `GamePanel`, and renders the game-over overlay text when `GameStatus.GAME_OVER`.

scope:
  - description: |
      Give `Snake` a way to reset its body back to a supplied starting layout in place
      (the panel holds a `final Game` which holds a `final Snake`, so restart must mutate
      existing objects rather than construct new ones):
      ```java
      public void reset(List<Position> body) {
          this.body.clear();
          this.body.addAll(body);
      }
      ```
    files:
      - src/main/java/com/snake/Snake.java
    rationale: |
      AC9 requires the snake to return to its starting position on restart. `Snake`'s body
      is a private `LinkedList`, so resetting it requires a method on `Snake` itself.

  - description: |
      Add restart capability to `Game`: capture the initial snake layout in the constructor
      and expose a `restart()` method that puts the snake back at that layout, resets score
      to zero, respawns food, and sets status back to `RUNNING`.
      ```java
      private final List<Position> initialSnakeBody;
      ...
      public Game(int width, int height, Snake snake, Food food, FoodSpawner foodSpawner) {
          ...
          this.initialSnakeBody = List.copyOf(snake.segments());
      }

      public void restart() {
          snake.reset(initialSnakeBody);
          score = 0;
          status = GameStatus.RUNNING;
          food = new Food(foodSpawner.spawn(width, height, snake));
      }
      ```
    files:
      - src/main/java/com/snake/Game.java
    rationale: |
      AC9 and AC10 require a "new game" (snake at start position, score zero) to begin when
      the player activates restart. `Game` already owns `score`, `status`, and `food`
      mutation in `tick`, so `restart` follows the same ownership pattern instead of
      GamePanel reaching into Game's internals.

  - description: |
      In `GamePanel`: (1) bind a restart key (`R`) to an action that calls `game.restart()`
      then `repaint()`, mirroring the existing `bindArrowKey` pattern; (2) in
      `paintComponent`, when `game.status() == GameStatus.GAME_OVER`, draw "Game Over", the
      current score, and a restart prompt ("Press R to restart") centered on the panel,
      instead of / in addition to the normal snake+food rendering.
      ```java
      private void bindRestartKey() {
          getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restart");
          getActionMap().put("restart", new AbstractAction() {
              @Override
              public void actionPerformed(ActionEvent e) {
                  game.restart();
                  repaint();
              }
          });
      }
      ```
    files:
      - src/main/java/com/snake/GamePanel.java
    rationale: |
      AC6-AC8 require "Game Over", the score, and a restart option to be visibly presented
      once the game ends; AC9-AC10 require that activating that option actually restarts.
      Binding through the existing Swing `InputMap`/`ActionMap` mechanism keeps the restart
      trigger consistent with how arrow-key input is already wired.

  - description: |
      Add the failing-first tests (see `tests` below for exact assertions) for the new
      `Snake.reset`, `Game.restart`, and `GamePanel` game-over-overlay/restart-key behavior.
    files:
      - src/test/java/com/snake/GameTest.java
      - src/test/java/com/snake/GamePanelTest.java
    rationale: |
      Test-first coverage for AC6-AC10; ACs 1-5 already have passing coverage in
      `GameTest` and require no new tests.

tests:
  - |
    AC1/AC2 (wall hard-stop, no wrap-around) — already GREEN, no new test needed:
    `GameTest.tickIntoWallLeavesSnakeLengthAndScoreUnchanged` asserts
    `assertEquals(GameStatus.GAME_OVER, game.status())` after ticking the head past the
    right edge, and the snake's segments list never gains a position on the opposite edge.
  - |
    AC3/AC4 (no movement/state change on input after game over) — already GREEN, no new
    test needed: `GameTest.tickAfterGameOverLeavesSnakeLengthAndScoreUnchanged` ticks again
    after game over and asserts `assertEquals(2, game.snakeLength())` and
    `assertEquals(0, game.score())` are unchanged from before the second tick.
  - |
    AC5 (self-collision hard-stop) — already GREEN, no new test needed:
    `GameTest.tickIntoSelfLeavesSnakeLengthAndScoreUnchanged` asserts
    `assertEquals(GameStatus.GAME_OVER, game.status())` after the head moves onto an
    existing body segment.
  - |
    AC9/AC10 (restart resets snake position and score) — new failing test first, in
    `GameTest.java`, red before `Game.restart()`/`Snake.reset()` exist:
    ```java
    @Test
    void restartResetsSnakeToStartingPositionAndScoreToZero() {
        List<Position> startingBody = List.of(new Position(9, 5), new Position(8, 5));
        Snake snake = new Snake(startingBody);
        Game game = new Game(10, 10, snake, new Food(new Position(0, 0)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        game.tick(Direction.RIGHT);
        assertEquals(GameStatus.GAME_OVER, game.status());

        game.restart();

        assertEquals(startingBody, game.snakeSegments());
        assertEquals(0, game.score());
        assertEquals(GameStatus.RUNNING, game.status());
    }
    ```
  - |
    AC6/AC7/AC8 (Game Over text, score, restart option are displayed) — new failing test
    first, in `GamePanelTest.java`, red before `GamePanel.paintComponent` is changed. Since
    `paintComponent` draws directly with `Graphics` (no accessible string model), assert via
    an offscreen render capture:
    ```java
    @Test
    void paintingAfterGameOverShowsGameOverScoreAndRestartOption() {
        Snake snake = new Snake(List.of(new Position(9, 5), new Position(8, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(0, 0)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertEquals(GameStatus.GAME_OVER, game.status());

        GamePanel panel = new GamePanel(game, 20);
        panel.setSize(200, 200);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        panel.paint(image.getGraphics());

        assertTrue(panel.gameOverOverlayText().contains("Game Over"));
        assertTrue(panel.gameOverOverlayText().contains(String.valueOf(game.score())));
        assertTrue(panel.gameOverOverlayText().toLowerCase().contains("restart"));
    }
    ```
    This adds a small package-private `GamePanel.gameOverOverlayText()` helper (built from
    the same strings `paintComponent` draws) purely so the test can assert on exact text
    content without parsing pixels, matching how `GamePanel.mapKey` is already exposed
    package-private for testability.
  - |
    AC9/AC10 via the UI trigger — new failing test first, in `GamePanelTest.java`, red
    before the `R` key binding exists:
    ```java
    @Test
    void restartActionResetsGameAfterGameOver() {
        Snake snake = new Snake(List.of(new Position(9, 5), new Position(8, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(0, 0)), new FoodSpawner(new Random(42)));
        GamePanel panel = new GamePanel(game, 20);
        game.tick(Direction.RIGHT);
        assertEquals(GameStatus.GAME_OVER, game.status());

        panel.getActionMap().get("restart").actionPerformed(
            new ActionEvent(panel, ActionEvent.ACTION_FIRST, "restart"));

        assertEquals(GameStatus.RUNNING, game.status());
        assertEquals(0, game.score());
    }
    ```

assumptions_or_open_questions:
  - |
    Assumed the restart key is `R` (unbound today, doesn't collide with the arrow-key
    bindings). The story doesn't specify a key; flag if a different key/mouse click/button
    is preferred.
  - |
    Assumed "a new game begins" (AC9) also means the food is respawned via the existing
    `FoodSpawner`, since there's no other food-reset behavior specified and leaving stale
    food in place would be inconsistent with a fresh game.
  - |
    Assumed the game-over overlay replaces/overlays the normal board rendering in the same
    `GamePanel.paintComponent`, rather than a separate Swing component/dialog, since the
    codebase has no existing screen/state-routing mechanism to introduce one.
  - |
    Assumed AC6-AC8's "displayed on screen" is satisfied by drawing text via `Graphics`
    (consistent with how score is already drawn today) rather than adding Swing UI
    components (e.g. `JLabel`/`JButton`), to stay consistent with the existing rendering
    approach and avoid introducing a new UI pattern for one screen.

package_dependencies: []

notes: |
  ACs 1-5 require no code changes — `Game.tick` (src/main/java/com/snake/Game.java:23-40)
  already computes `hitsWall` from the four boundary comparisons and calls
  `snake.collidesExcludingTail(next)` before any mutation, and short-circuits on
  `status != GameStatus.RUNNING` at the top of `tick`, which is exactly AC3/AC4's "no further
  state changes" requirement. This plan only extends `Snake`, `Game`, and `GamePanel` for the
  display/restart behavior (ACs 6-10).

  ```mermaid
  flowchart TD
      classDef touched fill:#f96,color:#000
      Window[SnakeGameWindow] -->|constructs| Panel[GamePanel]
      Timer[javax.swing.Timer] -->|actionPerformed each tick| Panel
      Panel -->|game.tick / game.restart| Game
      Game -->|snake.reset on restart| Snake
      Game -->|foodSpawner.spawn on restart| FoodSpawner

      class Panel touched
      class Game touched
      class Snake touched
  ```

review_focus: |
  In scope: making the already-passing wall/self-collision hard-stop logic visible on
  screen (Game Over text + score) and adding a working restart path that resets snake
  position and score (ACs 6-10). Out of scope: any change to the collision-detection logic
  itself in `Game.tick`/`Snake.collidesExcludingTail` — that's already correct and tested
  (ACs 1-5), so a reviewer should not expect diffs there beyond the new `restart()` method.
  Riskiest area: `Game.restart()` must fully reset all mutable state (`score`, `status`,
  `food`, and the snake's body) — a partial reset (e.g. forgetting to respawn food or reset
  status) would leave the game in an inconsistent state that only shows up after the second
  play-through, not the first. Deliberate choice: the game-over text is drawn via `Graphics`
  in `paintComponent` rather than a separate Swing dialog/component, to match the existing
  score-rendering approach; a `gameOverOverlayText()` package-private helper is added purely
  to make that text assertable in tests without pixel-parsing, mirroring the existing
  `GamePanel.mapKey` testability pattern.
