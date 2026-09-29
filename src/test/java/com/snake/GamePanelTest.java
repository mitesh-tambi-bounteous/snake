package com.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GamePanelTest {

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
}
