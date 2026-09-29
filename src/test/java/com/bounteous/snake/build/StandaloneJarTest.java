package com.bounteous.snake.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.EnumSet;
import java.util.jar.JarFile;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

class StandaloneJarTest {

    @Test
    void jarTaskProducesStandaloneRunnableJar() throws Exception {
        BuildResult result = GradleRunner.create()
            .withProjectDir(new File("."))
            .withArguments("jar")
            .build();
        assertTrue(
            EnumSet.of(TaskOutcome.SUCCESS, TaskOutcome.UP_TO_DATE)
                .contains(result.task(":jar").getOutcome()));

        File jarFile = new File("build/libs/snake-0.1.0.jar");
        assertTrue(jarFile.exists());
        try (JarFile jar = new JarFile(jarFile)) {
            String mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
            assertEquals("com.bounteous.snake.SnakeApplication", mainClass);
        }
    }
}
