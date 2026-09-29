summary: |
  This is the greenfield tech-stack bootstrap for the Snake project. The repository today only
  contains a README, so this work item establishes the Gradle build, Java 17 toolchain, and a
  minimal Swing/AWT application skeleton (a single JFrame launched from a main() entry point) so
  that every later gameplay story is built on a confirmed desktop-UI foundation rather than a web
  or console one. It also produces the standalone runnable jar distribution artifact and leaves
  behind a lightweight, test-first guardrail (both an automated JFrame-type assertion and a
  documented process checklist for future stories) so nobody accidentally reintroduces a web or
  console interface later.

scope:
  - description: |
      Add Gradle project scaffolding targeting Java 17: `settings.gradle`, `build.gradle`, and
      the Gradle wrapper. Configure the `java` toolchain to Java 17 and wire up JUnit 5 for the
      test source set.

      Key `build.gradle` fragments:
      ```groovy
      plugins {
          id 'java'
          id 'application'
      }

      group = 'com.bounteous.snake'
      version = '0.1.0'

      java {
          toolchain {
              languageVersion = JavaLanguageVersion.of(17)
          }
      }

      application {
          mainClass = 'com.bounteous.snake.SnakeApplication'
      }

      repositories { mavenCentral() }

      dependencies {
          testImplementation platform('org.junit:junit-bom:5.10.2')
          testImplementation 'org.junit.jupiter:junit-jupiter'
          testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
          testImplementation gradleTestKit()
      }

      test { useJUnitPlatform() }

      jar {
          archiveBaseName = 'snake'
          manifest {
              attributes 'Main-Class': 'com.bounteous.snake.SnakeApplication'
          }
      }
      ```
    files:
      - settings.gradle
      - build.gradle
      - gradle/wrapper/gradle-wrapper.properties
      - gradle/wrapper/gradle-wrapper.jar
      - gradlew
      - gradlew.bat
      - gradle.properties
    rationale: |
      AC2 requires the Gradle build to target Java 17 (LTS) and AC3 requires a standalone
      runnable jar. Using the `application` plugin's default `jar` task with an explicit
      `Main-Class` manifest attribute is sufficient for "standalone" here because the only
      runtime UI toolkit needed (Swing/AWT) ships inside the JDK itself — no shaded/fat jar or
      third-party runtime dependency is required for this skeleton.

  - description: |
      Add the minimal Swing/AWT application skeleton: a `SnakeGameFrame` that extends
      `javax.swing.JFrame`, and a `SnakeApplication` main class that launches it on the Swing
      event dispatch thread.

      ```java
      package com.bounteous.snake.ui;

      import javax.swing.JFrame;

      public class SnakeGameFrame extends JFrame {
          public SnakeGameFrame() {
              super("Snake");
              setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
              setSize(600, 600);
          }
      }
      ```

      ```java
      package com.bounteous.snake;

      import com.bounteous.snake.ui.SnakeGameFrame;
      import javax.swing.SwingUtilities;

      public final class SnakeApplication {
          public static void main(String[] args) {
              SwingUtilities.invokeLater(() -> new SnakeGameFrame().setVisible(true));
          }
      }
      ```
    files:
      - src/main/java/com/bounteous/snake/SnakeApplication.java
      - src/main/java/com/bounteous/snake/ui/SnakeGameFrame.java
    rationale: |
      AC1 requires the interface to be built with Swing/AWT and not a web or console interface.
      This is the smallest possible skeleton that satisfies that: one JFrame subclass and a
      main() entry point that shows it, with no game logic added beyond what this story asks for.

  - description: |
      Add the failing-first automated tests: a unit test asserting the UI is Swing-based, and
      two Gradle TestKit functional tests asserting the build targets Java 17 bytecode and
      produces a standalone runnable jar with the expected manifest.
    files:
      - src/test/java/com/bounteous/snake/ui/SnakeGameFrameTest.java
      - src/test/java/com/bounteous/snake/build/GradleBuildTargetsJava17Test.java
      - src/test/java/com/bounteous/snake/build/StandaloneJarTest.java
    rationale: |
      Test-first: each of AC1-AC3 gets one automated check that fails before the corresponding
      scaffolding/code exists and passes once it's added.

  - description: |
      Add a short story-authoring checklist that future implementation stories for this project
      must follow, capturing AC4's requirement in a documented, reviewable place rather than as
      an unenforceable automated test (a future story's acceptance criteria can't be
      unit-tested by this codebase).
    files:
      - docs/story-guidelines.md
    rationale: |
      AC4 is a process constraint on how *future* stories are written, not a runtime behavior of
      this application, so it is satisfied by a documented manual-check requirement rather than
      code. Placing it in `docs/story-guidelines.md` keeps it discoverable for whoever drafts the
      next story.

tests:
  - |
    AC1 - `src/test/java/com/bounteous/snake/ui/SnakeGameFrameTest.java` (fails until
    `SnakeGameFrame` exists and extends `JFrame`):
    ```java
    import static org.junit.jupiter.api.Assertions.assertTrue;
    import org.junit.jupiter.api.Test;
    import javax.swing.JFrame;
    import com.bounteous.snake.ui.SnakeGameFrame;

    class SnakeGameFrameTest {
        @Test
        void gameWindowIsASwingJFrameNotWebOrConsoleUi() {
            assertTrue(JFrame.class.isAssignableFrom(SnakeGameFrame.class));
        }
    }
    ```
  - |
    AC2 - `src/test/java/com/bounteous/snake/build/GradleBuildTargetsJava17Test.java` (fails
    until `build.gradle` sets the Java 17 toolchain), using Gradle TestKit to run a real build
    and inspecting the compiled class file's bytecode major version (61 = Java 17):
    ```java
    import org.gradle.testkit.runner.BuildResult;
    import org.gradle.testkit.runner.GradleRunner;
    import org.gradle.testkit.runner.TaskOutcome;
    import org.junit.jupiter.api.Test;
    import java.io.File;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import static org.junit.jupiter.api.Assertions.assertEquals;

    class GradleBuildTargetsJava17Test {
        @Test
        void compiledMainClassTargetsJava17Bytecode() throws Exception {
            BuildResult result = GradleRunner.create()
                .withProjectDir(new File("."))
                .withArguments("compileJava")
                .withPluginClasspath()
                .build();
            assertEquals(TaskOutcome.SUCCESS,
                result.task(":compileJava").getOutcome());

            byte[] classBytes = Files.readAllBytes(
                Path.of("build/classes/java/main/com/bounteous/snake/SnakeApplication.class"));
            int majorVersion = ((classBytes[6] & 0xFF) << 8) | (classBytes[7] & 0xFF);
            assertEquals(61, majorVersion);
        }
    }
    ```
  - |
    AC3 - `src/test/java/com/bounteous/snake/build/StandaloneJarTest.java` (fails until the
    `jar` task's manifest is configured), using Gradle TestKit to run `jar` and then reading the
    produced jar's manifest directly:
    ```java
    import org.gradle.testkit.runner.BuildResult;
    import org.gradle.testkit.runner.GradleRunner;
    import org.gradle.testkit.runner.TaskOutcome;
    import org.junit.jupiter.api.Test;
    import java.io.File;
    import java.util.jar.JarFile;
    import static org.junit.jupiter.api.Assertions.assertEquals;
    import static org.junit.jupiter.api.Assertions.assertTrue;

    class StandaloneJarTest {
        @Test
        void jarTaskProducesStandaloneRunnableJar() throws Exception {
            BuildResult result = GradleRunner.create()
                .withProjectDir(new File("."))
                .withArguments("jar")
                .withPluginClasspath()
                .build();
            assertEquals(TaskOutcome.SUCCESS, result.task(":jar").getOutcome());

            File jarFile = new File("build/libs/snake-0.1.0.jar");
            assertTrue(jarFile.exists());
            try (JarFile jar = new JarFile(jarFile)) {
                String mainClass = jar.getManifest().getMainAttributes()
                    .getValue("Main-Class");
                assertEquals("com.bounteous.snake.SnakeApplication", mainClass);
            }
        }
    }
    ```
  - |
    AC4 - manual check (no automated test is possible for content of a *future, not-yet-written*
    story): `docs/story-guidelines.md` is added containing a checklist line that must be present
    in every subsequent story's acceptance criteria, e.g. "Acceptance criteria include a
    Swing/AWT-based automated test or a documented manual check confirming no web or console
    interface is introduced." Verified by review of the doc's presence and wording, not by a
    test run.

assumptions_or_open_questions:
  - "Chosen Java package/group name is `com.bounteous.snake` and jar base name is `snake` (producing `snake-0.1.0.jar`) — no existing convention was found in the repo to confirm this, since it currently contains only a README."
  - "No third-party runtime dependency is needed for AC3's \"standalone\" jar because Swing/AWT ships inside the JDK; only JUnit 5 and Gradle TestKit (test-scope only) are added."
  - "Gradle wrapper is generated via `gradle wrapper --gradle-version <latest supporting Java 17 toolchains>` as part of implementation; exact wrapper version is left to the implementer rather than pinned here."
  - "AC4 is treated as a documentation/process deliverable (`docs/story-guidelines.md`) rather than an automated test, since it constrains the acceptance criteria of stories that don't exist yet and can't be checked by this codebase's test suite."

package_dependencies:
  - name: org.junit.jupiter:junit-jupiter
    version: "5.10.2"
    ecosystem: maven
    rationale: |
      Needed to write and run the JUnit 5 unit test for AC1 (`SnakeGameFrameTest`) as
      `testImplementation`, via Gradle/Maven Central.
  - name: org.junit.platform:junit-platform-launcher
    version: "1.10.2"
    ecosystem: maven
    rationale: |
      Required at test runtime by Gradle's `useJUnitPlatform()` test engine to execute the
      JUnit 5 tests.

notes: |
  This work item is pure tech-stack bootstrap: there is no existing Gradle build, Java source,
  or test tree in the repository yet (only a `README.md`). The plan therefore creates the full
  minimal skeleton rather than modifying existing code, and deliberately adds no gameplay logic
  beyond a single empty game window, since that is out of scope for SNAKE-STORY-005.

  A mermaid diagram is omitted: this is a small, purely-additive bootstrap (new files only, no
  existing modules being wired together), so a call-graph diagram would add nothing a reviewer
  can't see directly in the file list above.

review_focus: |
  Scope is strictly infrastructure: a Gradle build targeting Java 17, one empty Swing `JFrame`
  skeleton, a standalone runnable jar, and a documentation checklist for future stories — no
  gameplay/game-loop code is in scope and should not be expected here. The riskiest part is the
  two Gradle TestKit functional tests: bytecode major-version checks and hardcoded jar paths
  (`build/classes/java/main/...`, `build/libs/snake-0.1.0.jar`) are somewhat brittle to Gradle
  version/config changes, so if the implementer needs to adjust the toolchain or archive naming,
  the tests' literal paths/expected values must be updated in lockstep. Also note AC4 is
  deliberately satisfied by a documentation file, not a test, since it governs future stories'
  content rather than this codebase's runtime behavior.
