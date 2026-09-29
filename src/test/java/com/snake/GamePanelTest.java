package com.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
}
