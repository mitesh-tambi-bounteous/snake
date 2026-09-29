summary: |
  This story bootstraps the Snake project as a Java 17 Swing/AWT desktop application built
  with Gradle, replacing the currently-empty repository (only a README and `.arc` tooling
  config exist today). It adds a Gradle wrapper and `build.gradle` targeting Java 17, a
  minimal `com.snake.Main` entry point that opens a `javax.swing.JFrame`-based window (no
  game logic yet — that is future stories' scope), and configures the `jar` task so
  `./gradlew build` produces a standalone runnable jar (no third-party runtime
  dependencies means no shading/fat-jar plugin is needed). It also records, in
  `CONTRIBUTING.md`, the AC4 guideline that every future implementation story's acceptance
  criteria must include a Swing/AWT-based test or manual check proving no web/console
  interface was introduced, and backs that guideline with an automated check on the
  document itself so the rule can't silently drift.

scope:
  - description: |
      Bootstrap the Gradle project: `settings.gradle` (rootProject.name = 'snake') and a
      generated Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`,
      `gradle/wrapper/gradle-wrapper.properties`) pinned to Gradle 8.10, produced by running
      `gradle wrapper --gradle-version 8.10` once a local Gradle install is available (the
      wrapper jar is a generated binary, not hand-authored). Add a `.gitignore` covering
      `build/` and `.gradle/`.
    files:
      - settings.gradle
      - gradlew
      - gradlew.bat
      - gradle/wrapper/gradle-wrapper.properties
      - gradle/wrapper/gradle-wrapper.jar
      - .gitignore
    rationale: |
      AC2 and AC3 require the project to be "built via Gradle"; there is currently no Gradle
      setup at all (`.arc/config/setup.yaml` explicitly notes `./gradlew does not exist
      until SNAKE-STORY-005 creates it`), so this is the prerequisite for every other item.

  - description: |
      Write `build.gradle` (Groovy DSL) applying the `java` and `application` plugins,
      pinning the toolchain to Java 17, wiring JUnit 5 for `test`, and configuring the `jar`
      task's manifest so the assembled jar is directly runnable with `java -jar`:
      ```groovy
      plugins {
          id 'java'
          id 'application'
      }

      java {
          toolchain {
              languageVersion = JavaLanguageVersion.of(17)
          }
      }

      application {
          mainClass = 'com.snake.Main'
      }

      jar {
          manifest {
              attributes 'Main-Class': 'com.snake.Main'
          }
      }

      repositories { mavenCentral() }

      dependencies {
          testImplementation 'org.junit.jupiter:junit-jupiter:5.10.3'
          testImplementation gradleTestKit()
      }

      test {
          useJUnitPlatform()
      }
      ```
      Because the runtime has zero third-party dependencies (Swing/AWT ships in the JDK),
      the plain `jar` task output is already standalone — no shadow/fat-jar plugin is
      required.
    files:
      - build.gradle
    rationale: |
      Directly satisfies AC2 (Java 17 toolchain) and AC3 (standalone runnable jar via
      `./gradlew build`). `gradleTestKit()` is bundled with Gradle itself (not a Maven
      Central artifact), so it does not need a `package_dependencies` entry.

  - description: |
      Add the minimal application source: `com.snake.Main` (entry point) and
      `com.snake.SnakeGameWindow` (a `JFrame` subclass that sets a title and closes on exit).
      No game logic is added — that is out of scope for this tech-stack story.
      ```java
      package com.snake;

      import javax.swing.JFrame;

      public class SnakeGameWindow extends JFrame {
          public SnakeGameWindow() {
              super("Snake");
              setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
              setSize(600, 600);
          }
      }
      ```
      ```java
      package com.snake;

      public class Main {
          public static void main(String[] args) {
              SnakeGameWindow window = new SnakeGameWindow();
              window.setVisible(true);
          }
      }
      ```
    files:
      - src/main/java/com/snake/Main.java
      - src/main/java/com/snake/SnakeGameWindow.java
    rationale: |
      Satisfies AC1: the launched application is a Swing/AWT desktop window, not a web
      server or console loop. `Main.main` takes no `Scanner`/stdin input and starts no
      HTTP listener.

  - description: |
      Add the failing-first automated tests backing AC1–AC3 (see `tests` below for the
      exact assertions): a structural Swing test that avoids requiring a real display (so
      it runs in headless CI), a build-script content test for the Java 17 toolchain, and a
      Gradle TestKit functional test that actually runs the `jar` task and inspects the
      resulting artifact's manifest.
    files:
      - src/test/java/com/snake/SwingUiTest.java
      - src/test/java/com/snake/BuildConfigJava17Test.java
      - src/test/java/com/snake/StandaloneJarBuildTest.java
    rationale: |
      Test-first coverage for AC1, AC2, and AC3 respectively.

  - description: |
      Add `CONTRIBUTING.md` with a "Writing Acceptance Criteria for New Stories" section
      stating that every new implementation story's acceptance criteria must include a
      Swing/AWT-based automated test or a documented manual check confirming that no web
      or console interface was introduced. Back this with an automated test that asserts
      the guideline text is actually present in the document, so the rule can't be quietly
      deleted or drift out of sync.
    files:
      - CONTRIBUTING.md
      - src/test/java/com/snake/ContributingGuideTest.java
    rationale: |
      Directly satisfies AC4, which is a documentation/process constraint on how future
      stories are drafted rather than a runtime behavior — there is no application code to
      exercise, so the "test" is a content check on the guideline document itself.

tests:
  - |
    AC1 (Swing/AWT desktop interface, not web/console) — `src/test/java/com/snake/SwingUiTest.java`,
    written first and red before `SnakeGameWindow`/`Main` exist:
    ```java
    @Test
    void gameWindowIsASwingJFrame() {
        assertTrue(JFrame.class.isAssignableFrom(SnakeGameWindow.class));
    }

    @Test
    void mainHasNoArgsEntryPointAndDoesNotReadFromStdin() throws Exception {
        Method main = Main.class.getMethod("main", String[].class);
        assertTrue(Modifier.isStatic(main.getModifiers()));
    }
    ```
    This is deliberately reflection-based (no `JFrame` instantiation) so it runs in a
    headless CI JVM, where constructing an AWT `Window` throws `HeadlessException`. The
    full visual confirmation ("it runs as a desktop application... not a web or console
    interface") is additionally a documented manual check: run `./gradlew run`, confirm a
    Swing window titled "Snake" appears and no text prompt/console input is requested.
  - |
    AC2 (Gradle build targets Java 17) — `src/test/java/com/snake/BuildConfigJava17Test.java`,
    written first and red before `build.gradle` is authored:
    ```java
    @Test
    void buildTargetsJava17() throws IOException {
        String buildScript = Files.readString(Path.of("build.gradle"));
        assertTrue(buildScript.contains("JavaLanguageVersion.of(17)"));
    }
    ```
  - |
    AC3 (standalone runnable jar from `./gradlew build`) —
    `src/test/java/com/snake/StandaloneJarBuildTest.java`, written first and red before the
    `jar` manifest config exists:
    ```java
    @Test
    void jarTaskProducesStandaloneRunnableJar() throws IOException {
        BuildResult result = GradleRunner.create()
            .withProjectDir(new File("."))
            .withArguments("clean", "jar")
            .build();
        assertEquals(TaskOutcome.SUCCESS, result.task(":jar").getOutcome());

        File[] jars = new File("build/libs").listFiles((d, n) -> n.endsWith(".jar"));
        assertEquals(1, jars.length);
        try (JarFile jar = new JarFile(jars[0])) {
            assertEquals("com.snake.Main", jar.getManifest().getMainAttributes().getValue("Main-Class"));
        }
    }
    ```
  - |
    AC4 (future stories must include a Swing/AWT test or manual check) —
    `src/test/java/com/snake/ContributingGuideTest.java`, written first and red before
    `CONTRIBUTING.md` is authored:
    ```java
    @Test
    void guideMandatesSwingCheckForFutureStories() throws IOException {
        String text = Files.readString(Path.of("CONTRIBUTING.md"));
        assertTrue(text.contains("Swing/AWT"));
        assertTrue(text.toLowerCase().contains("no web or console interface"));
    }
    ```

assumptions_or_open_questions:
  - |
    Assumed Gradle wrapper version 8.10 (current stable at plan time, supports Java 17
    toolchains). Not specified in the story; flag if a different pinned version is
    preferred.
  - |
    Assumed base package `com.snake` and rootProject name `snake`, inferred from the
    repository name and README title ("# snake"); no package/group name was specified in
    the story.
  - |
    Assumed the CI/build environment used for `./gradlew test` is headless (no X11/Wayland
    display), so AC1's automated test is written to be reflection-only rather than
    instantiating a real `JFrame`. If the environment does provide a virtual display
    (e.g. Xvfb), the automated coverage could later be strengthened to actually construct
    and `pack()` the window — flagging this as an option rather than doing it speculatively
    now.
  - |
    Assumed AC4 is satisfied by documenting and enforcing the guideline in
    `CONTRIBUTING.md` (checked by an automated content test) rather than by any change to
    story-authoring tooling outside this repository, since no such tooling exists in this
    codebase.
  - |
    No game logic (snake movement, rendering loop, input handling) is in scope here — this
    story is explicitly the tech-stack bootstrap only, per its description ("no separate
    architecture chore needed").

package_dependencies:
  - name: org.junit.jupiter:junit-jupiter
    version: "5.10.3"
    ecosystem: maven
    rationale: |
      JUnit 5 is needed to write the test-first coverage for AC1–AC4 (`test { useJUnitPlatform() }`
      in build.gradle); the project currently has no test dependencies declared at all.

notes: |
  No third-party runtime dependency is added (Swing/AWT and the `jar`/`application`
  plugins are part of the JDK and Gradle respectively), so AC3's "standalone runnable jar"
  is satisfied by the plain `jar` task's manifest `Main-Class` attribute — no shadow/fat-jar
  plugin is needed and none is added, avoiding a guessed plugin coordinate for a problem
  this project doesn't actually have.

  This is a greenfield bootstrap (only `README.md`, `.env`, and `.arc/` tooling files exist
  today; there is no prior `build.gradle` or `src/` to reconcile with), so no diagram is
  included — every file listed under `scope` is newly created, there is no existing module
  graph to show call direction against.

review_focus: |
  In scope: Gradle project bootstrap (wrapper, `build.gradle`, Java 17 toolchain), a
  minimal Swing `Main`/`SnakeGameWindow` pair with no game logic, jar-manifest
  configuration for a standalone runnable jar, and a `CONTRIBUTING.md` guideline (AC4)
  backed by a content-assertion test. Out of scope: any Snake gameplay, rendering, or input
  handling — those belong to later stories. The riskiest area is AC1's test design: it
  deliberately avoids instantiating a real `JFrame` (reflection-only assertions) to stay
  headless-CI-safe, so it does not prove the window actually renders — that gap is closed
  only by the documented manual `./gradlew run` check, not by automated CI. Also
  deliberate: no shadow/fat-jar Gradle plugin is used for AC3, since the project has zero
  runtime dependencies to bundle, so the plain `jar` task is already a valid standalone
  artifact.
