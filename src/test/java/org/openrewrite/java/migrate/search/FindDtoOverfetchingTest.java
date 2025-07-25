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
package org.openrewrite.java.migrate.search;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class FindDtoOverfetchingTest implements RewriteTest {

    @ParameterizedTest
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @ValueSource(strings = {
      "java.time.LocalDate", "java.time.*"
    })
    void findDtoOverfetching(String dtoType) {
        rewriteRun(
          spec -> spec.recipe(new FindDtoOverfetching(dtoType)),
          //language=java
          java(
            """
              import java.time.LocalDate;

              class Test {
                  void test(LocalDate date) {
                        date.getDayOfMonth();
                        date.getDayOfYear();
                  }

                  void test2(LocalDate date) {
                        date.getDayOfMonth();
                  }
              }
              """,
            """
              import java.time.LocalDate;

              class Test {
                  void test(LocalDate date) {
                        date.getDayOfMonth();
                        date.getDayOfYear();
                  }

                  /*~~(dayOfMonth)~~>*/void test2(LocalDate date) {
                        date.getDayOfMonth();
                  }
              }
              """
          )
        );
    }

    @Test
    void mustHaveSelect() {
        rewriteRun(
          spec -> spec.recipe(new FindDtoOverfetching("animals.Dog")),
          //language=java
          java(
            """
              package animals;

              public class Dog {
                  String name;
                  String breed;

                  public String getName() {
                      return getBreed();
                  }

                  public String getBreed() {
                      return breed;
                  }
              }
              """
          )
        );
    }

    @Test
    void mustBeParameter() {
        rewriteRun(
          spec -> spec.recipe(new FindDtoOverfetching("animals.Dog")),
          //language=java
          java(
            """
              package animals;

              public class Dog {
                  String name;
                  String breed;

                  public String getName() {
                      return new Dog().getBreed();
                  }

                  public String getBreed() {
                      return breed;
                  }
              }
              """
          )
        );
    }
}
