/*
 * PlannerRuleCall.java
 *
 * This source file is part of the FoundationDB open source project
 *
 * Copyright 2015-2018 Apple Inc. and the FoundationDB project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apple.foundationdb.record.query.plan.cascades;

import com.apple.foundationdb.annotation.API;
import com.apple.foundationdb.record.EvaluationContext;
import com.apple.foundationdb.record.query.plan.cascades.matching.structure.BindingMatcher;
import com.apple.foundationdb.record.query.plan.cascades.matching.structure.PlannerBindings;

import javax.annotation.Nonnull;

/**
 * A <code>PlannerRuleCall</code> is a context object that supports a single application of a rule to a particular
 * expression. It stores and provides convenient access to various context related to the transformation, such as any
 * bindings, access to the {@link PlanContext}, etc. A <code>PlannerRuleCall</code> is passed to every rule's
 * {@link CascadesRule#onMatch(CascadesRuleCall)} method.
 */
@API(API.Status.EXPERIMENTAL)
public interface PlannerRuleCall {
    /**
     * Method that returns the current {@link EvaluationContext}.
     * @return the current {@link EvaluationContext}
     */
    @Nonnull
    EvaluationContext getEvaluationContext();

    /**
     * Return the map of bindings that this rule's matcher expression produced, which includes (by contract) all of the
     * bindings specified by the rule associated with this call.
     * This method should be implemented by rule call implementations, but users of the rule should usually access these
     * via {@link #get(BindingMatcher)}.
     *
     * @return the map of bindings that the rule's matcher expression produced
     */
    @Nonnull
    PlannerBindings getBindings();

    /**
     * Return the bindable that is bound to the given key.
     *
     * @param key the binding from the rule's matcher expression
     * @param <U> the requested return type
     *
     * @return the bindable bound to <code>name</code> in the rule's matcher expression
     *
     * @throws java.util.NoSuchElementException when <code>key</code> is not a valid binding, or is not bound to a
     * bindable
     */
    @Nonnull
    default <U> U get(@Nonnull BindingMatcher<U> key) {
        return getBindings().get(key);
    }
}
