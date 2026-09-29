package com.snake;

import java.awt.Dimension;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.Timer;

public class SnakeGameWindow extends JFrame {

    static final int CELL_SIZE = 20;
    static final int GRID_SIZE = 20;
    static final int TICK_INTERVAL_MS = 150;

    public SnakeGameWindow() {
        super("Snake");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Game game = createInitialGame();
        GamePanel panel = new GamePanel(game, CELL_SIZE);
        panel.setPreferredSize(new Dimension(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE));
        add(panel);
        pack();
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
