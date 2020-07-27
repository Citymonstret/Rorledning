//
// MIT License
//
// Copyright (c) 2020 IntellectualSites
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.
//
package com.intellectualsites.services.types;

import com.intellectualsites.services.ChunkedRequestContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Service type that allows service to generate partial results for bigger requests
 *
 * @param <Context> Context type
 * @param <Result>  Result type
 */
public interface PartialResultService<Context, Result, Chunked extends ChunkedRequestContext<Context, Result>>
    extends Service<Chunked, Map<Context, Result>> {

    @Override @Nullable default Map<Context, Result> handle(@Nonnull final Chunked context) {
        if (!context.isCompleted()) {
            this.handleRequests(context.getRemaining()).forEach(context::storeResult);
        }
        if (context.isCompleted()) {
            return context.getAvailableResults();
        }
        return null;
    }

    /**
     * Attempt to generate results for a list of requests, and return
     * a map of all successful requests
     *
     * @param requests Requests
     * @return Map of request-result pairs
     */
    @Nonnull Map<Context, Result> handleRequests(@Nonnull List<Context> requests);

}
