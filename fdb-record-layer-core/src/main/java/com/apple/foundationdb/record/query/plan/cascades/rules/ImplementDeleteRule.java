/*
 * ImplementDeleteRule.java
 *
 * This source file is part of the FoundationDB open source project
 *
 * Copyright 2015-2019 Apple Inc. and the FoundationDB project authors
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

package com.apple.foundationdb.record.query.plan.cascades.rules;

import com.apple.foundationdb.annotation.API;
import com.apple.foundationdb.record.query.plan.cascades.ImplementationCascadesRule;
import com.apple.foundationdb.record.query.plan.cascades.ImplementationCascadesRuleCall;
import com.apple.foundationdb.record.query.plan.cascades.PlanPartition;
import com.apple.foundationdb.record.query.plan.cascades.Quantifier;
import com.apple.foundationdb.record.query.plan.cascades.Reference;
import com.apple.foundationdb.record.query.plan.cascades.expressions.DeleteExpression;
import com.apple.foundationdb.record.query.plan.cascades.matching.structure.BindingMatcher;
import com.apple.foundationdb.record.query.plan.cascades.properties.DistinctRecordsProperty;
import com.apple.foundationdb.record.query.plan.cascades.properties.StoredRecordProperty;
import com.apple.foundationdb.record.query.plan.plans.RecordQueryUnorderedPrimaryKeyDistinctPlan;

import javax.annotation.Nonnull;

import static com.apple.foundationdb.record.query.plan.cascades.matching.structure.AnyMatcher.any;
import static com.apple.foundationdb.record.query.plan.cascades.matching.structure.PlanPartitionMatchers.anyPlanPartition;
import static com.apple.foundationdb.record.query.plan.cascades.matching.structure.PlanPartitionMatchers.filterPlanPartitions;
import static com.apple.foundationdb.record.query.plan.cascades.matching.structure.PlanPartitionMatchers.planPartitions;
import static com.apple.foundationdb.record.query.plan.cascades.matching.structure.QuantifierMatchers.forEachQuantifierOverRef;
import static com.apple.foundationdb.record.query.plan.cascades.matching.structure.RelationalExpressionMatchers.deleteExpression;

/**
 * A rule that implements a {@link DeleteExpression} by creating
 * a {@link com.apple.foundationdb.record.query.plan.plans.RecordQueryDeletePlan}.
 */
@API(API.Status.EXPERIMENTAL)
@SuppressWarnings("PMD.TooManyStaticImports")
public class ImplementDeleteRule extends ImplementationCascadesRule<DeleteExpression> {
    @Nonnull
    private static final BindingMatcher<PlanPartition> innerPlanPartitionMatcher = anyPlanPartition();

    @Nonnull
    private static final BindingMatcher<Reference> innerReferenceMatcher =
            planPartitions(filterPlanPartitions(planPartition -> planPartition.getPartitionPropertyValue(StoredRecordProperty.storedRecord()),
                    any(innerPlanPartitionMatcher)));

    private static final BindingMatcher<Quantifier.ForEach> innerQuantifierMatcher =
            forEachQuantifierOverRef(innerReferenceMatcher);

    @Nonnull
    private static final BindingMatcher<DeleteExpression> root =
            deleteExpression(innerQuantifierMatcher);

    public ImplementDeleteRule() {
        super(root);
    }

    @Override
    public void onMatch(@Nonnull final ImplementationCascadesRuleCall call) {
        final var innerPlanPartition = call.get(innerPlanPartitionMatcher);
        final var innerReference = call.get(innerReferenceMatcher);
        final var innerQuantifier = call.get(innerQuantifierMatcher);
        final var deleteExpression = call.get(root);

        final var planPartitionReference =
                call.memoizeMemberPlansFromOther(innerReference, innerPlanPartition.getPlans());
        final var distinctPlansReference =
                innerPlanPartition.getPartitionPropertyValue(DistinctRecordsProperty.distinctRecords())
                ? planPartitionReference
                : call.memoizePlan(new RecordQueryUnorderedPrimaryKeyDistinctPlan(Quantifier.physical(planPartitionReference)));

        final var physicalQuantifier =
                Quantifier.physicalBuilder()
                        .morphFrom(innerQuantifier)
                        .build(distinctPlansReference);
        call.yieldPlan(deleteExpression.toPlan(physicalQuantifier));
    }
}
