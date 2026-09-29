package com.snake;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import org.junit.jupiter.api.Test;

class SwingUiTest {

    @Test
    void gameWindowIsASwingJFrame() {
        assertTrue(JFrame.class.isAssignableFrom(SnakeGameWindow.class));
    }

    @Test
    void mainHasNoArgsEntryPointAndDoesNotReadFromStdin() throws Exception {
        Method main = Main.class.getMethod("main", String[].class);
        assertTrue(Modifier.isStatic(main.getModifiers()));
    }
}
