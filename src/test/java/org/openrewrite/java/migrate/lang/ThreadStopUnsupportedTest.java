/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.migrate.lang;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.Issue;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.javaVersion;

class ThreadStopUnsupportedTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ThreadStopUnsupported());
    }

    @DocumentExample
    @Issue("https://github.com/openrewrite/rewrite-migrate-java/issues/194")
    @Test
    void addCommentPreJava21() {
        rewriteRun(
          //language=java
          java(
            """
              class Foo {
                  void bar() {
                      Thread.currentThread().stop();
                  }
              }
              """,
            """
              class Foo {
                  void bar() {
                      /*
                       * `Thread.stop()` always throws a `new UnsupportedOperationException()` in Java 21+.
                       * For detailed migration instructions see the migration guide available at
                       * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/doc-files/threadPrimitiveDeprecation.html
                       */
                      Thread.currentThread().stop();
                  }
              }
              """,
            src -> src.markers(javaVersion(17))
          )
        );
    }

    @Test
    void retainCommentIfPresent() {
        rewriteRun(
          //language=java
          java(
            """
              class Foo {
                  void bar() {
                      // I know, I know, but it's a legacy codebase and we're not ready to migrate yet
                      Thread.currentThread().stop();
                  }
              }
              """,
            src -> src.markers(javaVersion(8))
          )
        );
    }

    @Test
    void replaceStopWithThrowsOnJava21() {
        rewriteRun(
          //language=java
          java(
            """
              class Foo {
                  void bar() {
                      Thread.currentThread().stop();
                  }
              }
              """,
            """
              class Foo {
                  void bar() {
                      /*
                       * `Thread.stop()` always throws a `new UnsupportedOperationException()` in Java 21+.
                       * For detailed migration instructions see the migration guide available at
                       * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/doc-files/threadPrimitiveDeprecation.html
                       */
                      throw new UnsupportedOperationException();
                  }
              }
              """,
            src -> src.markers(javaVersion(21))
          )
        );
    }

    @Test
    void replaceResumeWithThrowsOnJava21() {
        rewriteRun(
          //language=java
          java(
            """
              class Foo {
                  void bar() {
                      Thread.currentThread().resume();
                  }
              }
              """,
            """
              class Foo {
                  void bar() {
                      /*
                       * `Thread.resume()` always throws a `new UnsupportedOperationException()` in Java 21+.
                       * For detailed migration instructions see the migration guide available at
                       * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/doc-files/threadPrimitiveDeprecation.html
                       */
                      throw new UnsupportedOperationException();
                  }
              }
              """,
            src -> src.markers(javaVersion(21))
          )
        );
    }

    @Test
    void replaceSuspendWithThrowsOnJava21() {
        rewriteRun(
          //language=java
          java(
            """
              class Foo {
                  void bar() {
                      Thread.currentThread().suspend();
                  }
              }
              """,
            """
              class Foo {
                  void bar() {
                      /*
                       * `Thread.suspend()` always throws a `new UnsupportedOperationException()` in Java 21+.
                       * For detailed migration instructions see the migration guide available at
                       * https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/doc-files/threadPrimitiveDeprecation.html
                       */
                      throw new UnsupportedOperationException();
                  }
              }
              """,
            src -> src.markers(javaVersion(21))
          )
        );
    }
}
