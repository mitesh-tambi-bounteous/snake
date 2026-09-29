package com.snake;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class BuildConfigJava17Test {

    @Test
    void buildTargetsJava17() throws IOException {
        String buildScript = Files.readString(Path.of("build.gradle"));
        assertTrue(buildScript.contains("JavaLanguageVersion.of(17)"));
    }
}
