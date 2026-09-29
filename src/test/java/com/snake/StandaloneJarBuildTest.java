package com.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;

class StandaloneJarBuildTest {

    @Test
    void jarTaskProducesStandaloneRunnableJar() throws IOException {
        BuildResult result = GradleRunner.create()
            .withProjectDir(new File("."))
            .withArguments("jar", "--rerun-tasks")
            .build();
        assertEquals(TaskOutcome.SUCCESS, result.task(":jar").getOutcome());

        File[] jars = new File("build/libs").listFiles((d, n) -> n.endsWith(".jar"));
        assertEquals(1, jars.length);
        try (JarFile jar = new JarFile(jars[0])) {
            assertEquals(
                "com.snake.Main", jar.getManifest().getMainAttributes().getValue("Main-Class"));
        }
    }
}
