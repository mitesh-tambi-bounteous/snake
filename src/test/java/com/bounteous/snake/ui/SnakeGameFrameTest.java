package com.bounteous.snake.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JFrame;
import org.junit.jupiter.api.Test;

class SnakeGameFrameTest {

    @Test
    void gameWindowIsASwingJFrameNotWebOrConsoleUi() {
        assertTrue(JFrame.class.isAssignableFrom(SnakeGameFrame.class));
    }
}
