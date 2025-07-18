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
package org.openrewrite.java.migrate.javax;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class AddTransientAnnotationToEntityTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion().classpathFromResources(new InMemoryExecutionContext(), "javax.persistence-api-2.2"))
          .recipe(new AddTransientAnnotationToEntity());
    }

    @DocumentExample
    @Test
    void addTransient() {
        //language=java
        rewriteRun(
          java(
            """
              import javax.persistence.Entity;
              import javax.persistence.Id;

              @Entity
              public class EntityA {
                  @Id
                  private int id;

                  private EntityB entityReference;
              }
              """,
            """
              import javax.persistence.Entity;
              import javax.persistence.Id;
              import javax.persistence.Transient;

              @Entity
              public class EntityA {
                  @Id
                  private int id;

                  @Transient
                  private EntityB entityReference;
              }
              """
          ),
          java(
            """
              import javax.persistence.Entity;
              import javax.persistence.Id;

              @Entity
              public class EntityB {
                  @Id
                  private int id;
              }
              """
          )
        );
    }

    @Test
    void ignoreJpaAnnotatedEntity() {
        //language=java
        rewriteRun(
          java(
            """
              import javax.persistence.Entity;
              import javax.persistence.Id;

              @Entity
              public class EntityA {
                  @Id
                  private int id;

                  @Id
                  private EntityB entityReference;
              }
              """
          ),
          java(
            """
              import javax.persistence.Entity;
              import javax.persistence.Id;

              @Entity
              public class EntityB {
                  @Id
                  private int id;
              }
              """
          )
        );
    }

    @Test
    void addTransientOnNonJpaAnnotatedEntity() {
        //language=java
        rewriteRun(
          java(
            """
              import javax.persistence.Entity;
              import javax.persistence.Id;

              import java.lang.annotation.Documented;

              @Entity
              public class EntityA {
                  @Id
                  private int id;

                  @Documented
                  private EntityB entityReference;
              }
              """,
            """
              import javax.persistence.Entity;
              import javax.persistence.Id;
              import javax.persistence.Transient;

              import java.lang.annotation.Documented;

              @Entity
              public class EntityA {
                  @Id
                  private int id;

                  @Documented
                  @Transient
                  private EntityB entityReference;
              }
              """
          ),
          java(
            """
              import javax.persistence.Entity;
              import javax.persistence.Id;

              @Entity
              public class EntityB {
                  @Id
                  private int id;
              }
              """
          )
        );
    }

    @Test
    void ignoreNonEntity() {
        //language=java
        rewriteRun(
          java(
            """
              import javax.persistence.Entity;
              import javax.persistence.Id;

              @Entity
              public class EntityA {
                  @Id
                  private int id;

                  private NotEntityB entityReference;
              }
              """
          ),
          java(
            """
              import javax.persistence.Id;

              public class NotEntityB {
                  @Id
                  private int id;
              }
              """
          )
        );
    }
}
