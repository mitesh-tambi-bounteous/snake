package com.bounteous.snake;

import com.bounteous.snake.ui.SnakeGameFrame;
import javax.swing.SwingUtilities;

public final class SnakeApplication {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SnakeGameFrame().setVisible(true));
    }
}
