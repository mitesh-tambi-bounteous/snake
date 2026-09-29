package com.bounteous.snake.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

class GradleBuildTargetsJava17Test {

    @Test
    void compiledMainClassTargetsJava17Bytecode() throws Exception {
        BuildResult result = GradleRunner.create()
            .withProjectDir(new File("."))
            .withArguments("compileJava")
            .build();
        assertTrue(
            EnumSet.of(TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE)
                .contains(result.task(":compileJava").getOutcome()));

        byte[] classBytes = Files.readAllBytes(
            Path.of("build/classes/java/main/com/bounteous/snake/SnakeApplication.class"));
        int majorVersion = ((classBytes[6] & 0xFF) << 8) | (classBytes[7] & 0xFF);
        assertEquals(61, majorVersion);
    }
}
