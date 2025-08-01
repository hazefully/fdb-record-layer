/*
 * IteratorResultSet.java
 *
 * This source file is part of the FoundationDB open source project
 *
 * Copyright 2015-2025 Apple Inc. and the FoundationDB project authors
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

package com.apple.foundationdb.relational.recordlayer;

import com.apple.foundationdb.annotation.API;
import com.apple.foundationdb.relational.api.Continuation;
import com.apple.foundationdb.relational.api.Row;
import com.apple.foundationdb.relational.api.StructMetaData;
import com.apple.foundationdb.relational.api.exceptions.RelationalException;
import com.apple.foundationdb.relational.util.SpotBugsSuppressWarnings;

import javax.annotation.Nonnull;
import java.sql.SQLException;
import java.util.Iterator;

import static com.apple.foundationdb.relational.api.exceptions.ErrorCode.UNSUPPORTED_OPERATION;


/**
 * Placeholder result set until better generic abstractions come along.
 */
@SpotBugsSuppressWarnings(value = "EI_EXPOSE_REP2", justification = "Intentionally exposed for performance reasons")
@API(API.Status.EXPERIMENTAL)
public class IteratorResultSet extends AbstractRecordLayerResultSet {

    private final Iterator<? extends Row> rowIter;

    private int currentRowPosition;

    public IteratorResultSet(StructMetaData metaData, Iterator<? extends Row> rowIter, int initialRowPosition) {
        super(metaData);
        this.rowIter = rowIter;
        this.currentRowPosition = initialRowPosition;
    }

    @Nonnull
    @Override
    public Continuation getContinuation() throws SQLException {
        boolean hasNext = rowIter.hasNext();
        boolean beginning = currentRowPosition == 0;

        if (hasNext) {
            throw new SQLException("Continuation can only be returned once the result set has been exhausted", UNSUPPORTED_OPERATION.getErrorCode());
        }

        if (beginning) {
            return ContinuationImpl.BEGIN;
        } else {
            return ContinuationImpl.END;
        }
    }

    @Override
    public void close() throws SQLException {
        //no-op at the moment
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    protected boolean hasNext() {
        return rowIter.hasNext();
    }

    @Override
    protected Row advanceRow() throws RelationalException {
        if (!rowIter.hasNext()) {
            return null;
        }
        currentRowPosition++;
        return rowIter.next();
    }
}
