package com.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SnakeGameWindowTest {

    @Test
    void createInitialGameStartsRunningWithZeroScore() {
        Game game = SnakeGameWindow.createInitialGame();
        assertEquals(GameStatus.RUNNING, game.status());
        assertEquals(0, game.score());
        assertEquals(3, game.snakeLength());
    }
}
