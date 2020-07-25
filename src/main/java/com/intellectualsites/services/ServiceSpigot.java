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
package com.intellectualsites.services;

import com.google.common.reflect.TypeToken;
import com.intellectualsites.services.types.ConsumerService;
import com.intellectualsites.services.types.Service;
import com.intellectualsites.services.types.SideEffectService;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * Class that outputs results from the given context, using the specified service type
 *
 * @param <Context> Context type
 * @param <Result>  Result type
 */
public final class ServiceSpigot<Context, Result> {

    private final Context context;
    private final ServicePipeline pipeline;
    private final ServiceRepository<Context, Result> repository;

    ServiceSpigot(@Nonnull final ServicePipeline pipeline, @Nonnull final Context context,
        @Nonnull final TypeToken<? extends Service<Context, Result>> type) {
        this.context = context;
        this.pipeline = pipeline;
        this.repository = pipeline.getRepository(type);
    }

    /**
     * Get the first result that is generated for the given context. This cannot return null. If
     * nothing manages to produce a result, an exception will be thrown. If the pipeline has
     * been constructed properly, this will never happen.
     *
     * @return Generated result
     */
    @Nonnull public Result getResult() {
        final LinkedList<? extends ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>>>
            queue = this.repository.getQueue();
        queue.sort(null); // Sort using the built in comparator method
        ServiceRepository<Context, Result>.ServiceWrapper<? extends Service<Context, Result>>
            wrapper;
        boolean consumerService = false;
        while ((wrapper = queue.pollLast()) != null) {
            consumerService = wrapper.getImplementation() instanceof ConsumerService;
            if (!ServiceFilterHandler.INSTANCE.passes(wrapper, this.context)) {
                continue;
            }
            final Result result = wrapper.getImplementation().handle(this.context);
            if (wrapper.getImplementation() instanceof SideEffectService) {
                if (result == null) {
                    throw new IllegalStateException(
                        String.format("SideEffectService '%s' returned null", wrapper.toString()));
                } else if (result == State.ACCEPTED) {
                    return result;
                }
            } else if (result != null) {
                return result;
            }
        }
        // This is hack to make it so that the default
        // consumer implementation does not have to call #interrupt
        if (consumerService) {
            return (Result) State.ACCEPTED;
        }
        throw new IllegalStateException(
            "No service consumed the context. This means that the pipeline was not constructed properly.");
    }

    /**
     * Get the first result that is generated for the given context. This cannot return null. If
     * nothing manages to produce a result, an exception will be thrown. If the pipeline has
     * been constructed properly, this will never happen.
     *
     * @return Generated result
     */
    @Nonnull public CompletableFuture<Result> getResultAsynchronously() {
        return CompletableFuture.supplyAsync(this::getResult, this.pipeline.getExecutor());
    }

    /**
     * Forward the request through the original pipeline.
     *
     * @return New pump, for the result of this request
     */
    @Nonnull public ServicePump<Result> forward() {
        return this.pipeline.pump(this.getResult());
    }

    /**
     * Forward the request through the original pipeline.
     *
     * @return New pump, for the result of this request
     */
    @Nonnull public CompletableFuture<ServicePump<Result>> forwardAsynchronously() {
        return this.getResultAsynchronously().thenApply(pipeline::pump);
    }

}
