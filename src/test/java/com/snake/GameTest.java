package com.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GameTest {

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

    @Test
    void tickIntoWallLeavesSnakeLengthAndScoreUnchanged() {
        Snake snake = new Snake(List.of(new Position(9, 5), new Position(8, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(0, 0)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertEquals(2, game.snakeLength());
        assertEquals(0, game.score());
        assertEquals(GameStatus.GAME_OVER, game.status());
    }

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

    @Test
    void eatingFoodReplacesItWithASpawnedCellNotOnTheSnake() {
        Snake snake = new Snake(List.of(new Position(5, 5), new Position(4, 5), new Position(3, 5)));
        Game game = new Game(10, 10, snake, new Food(new Position(6, 5)), new FoodSpawner(new Random(42)));
        game.tick(Direction.RIGHT);
        assertTrue(game.snakeSegments().stream().noneMatch(p -> p.equals(game.foodPosition())));
        assertNotEquals(new Position(6, 5), game.foodPosition());
    }
}
