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
package org.openrewrite.java.migrate.util;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.*;

import static java.util.Objects.requireNonNull;

public class OptionalStreamRecipe extends Recipe {
    @Override
    public String getDisplayName() {
        return "`Stream<Optional>` idiom recipe";
    }

    @Override
    public String getDescription() {
        return "Migrate Java 8 `Optional<Stream>.filter(Optional::isPresent).map(Optional::get)` to Java 11 `.flatMap(Optional::stream)`.";
    }

    private static final MethodMatcher mapMatcher = new MethodMatcher("java.util.stream.Stream map(java.util.function.Function)");
    private static final MethodMatcher filterMatcher = new MethodMatcher("java.util.stream.Stream filter(java.util.function.Predicate)");
    private static final MethodMatcher optionalGetMatcher = new MethodMatcher("java.util.Optional get()");
    private static final MethodMatcher optionalIsPresentMatcher = new MethodMatcher("java.util.Optional isPresent()");

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new UsesMethod<>(optionalIsPresentMatcher), new OptionalStreamVisitor());
    }

    private static class OptionalStreamVisitor extends JavaIsoVisitor<ExecutionContext> {

        @Override
        public J.MethodInvocation visitMethodInvocation(J.MethodInvocation invocation, ExecutionContext ctx) {
            J.MethodInvocation mapInvocation = super.visitMethodInvocation(invocation, ctx);
            // .map(Optional::get)
            if (!mapMatcher.matches(mapInvocation) || !optionalGetMatcher.matches(mapInvocation.getArguments().get(0))) {
                return mapInvocation;
            }
            // .filter
            Expression mapSelectExpr = mapInvocation.getSelect();
            if (!filterMatcher.matches(mapSelectExpr)) {
                return mapInvocation;
            }
            // Optional::isPresent
            J.MethodInvocation filterInvocation = (J.MethodInvocation) mapSelectExpr;
            if (!optionalIsPresentMatcher.matches(filterInvocation.getArguments().get(0))) {
                return mapInvocation;
            }

            JRightPadded<Expression> filterSelect = requireNonNull(filterInvocation.getPadding().getSelect());
            JRightPadded<Expression> mapSelect = requireNonNull(mapInvocation.getPadding().getSelect());
            JavaType.Method mapInvocationType = requireNonNull(mapInvocation.getMethodType());
            J.MethodInvocation flatMapInvocation = JavaTemplate.builder("#{any(java.util.stream.Stream)}.flatMap(Optional::stream)")
                    .imports("java.util.Optional")
                    .build()
                    .apply(updateCursor(mapInvocation), mapInvocation.getCoordinates().replace(), filterSelect.getElement());
            Space flatMapComments = filterSelect.getAfter().withComments(ListUtils.concatAll(
                    filterSelect.getAfter().getComments(),
                    mapSelect.getAfter().getComments()
            ));
            return flatMapInvocation.getPadding()
                    .withSelect(filterSelect.withAfter(flatMapComments))
                    .withMethodType(mapInvocationType.withName("flatMap"))
                    .withPrefix(mapInvocation.getPrefix());
        }

    }
}
