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
package org.openrewrite.java.migrate;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import static org.openrewrite.java.Assertions.java;

class DontOverfetchDtoTest implements RewriteTest {

    @SuppressWarnings("LombokGetterMayBeUsed")
    @Test
    void findDtoOverfetching() {
        rewriteRun(
          spec -> spec.recipe(new DontOverfetchDto("animals.Dog", "name"))
            .typeValidationOptions(TypeValidation.none()),
          //language=java
          java(
            """
              package animals;
              public class Dog {
                  String name;
                  String breed;
                  public String getName() {
                      return name;
                  }
                  public String getBreed() {
                      return breed;
                  }
              }
              """
          ),
          //language=java
          java(
            """
              import animals.Dog;

              class Test {
                  boolean test(Dog dog, int age) {
                      if(dog.getName() != null) {
                          return true;
                      }
                  }
              }
              """,
            """
              class Test {
                  boolean test(java.lang.String name, int age) {
                      if(name != null) {
                          return true;
                      }
                  }
              }
              """
          )
        );
    }
}
