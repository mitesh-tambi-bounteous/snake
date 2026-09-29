package com.snake;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ContributingGuideTest {

    @Test
    void guideMandatesSwingCheckForFutureStories() throws IOException {
        String text = Files.readString(Path.of("CONTRIBUTING.md"));
        assertTrue(text.contains("Swing/AWT"));
        assertTrue(text.toLowerCase().contains("no web or console interface"));
    }
}
