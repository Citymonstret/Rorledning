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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pipeline
 */
public final class ServicePipeline {

    private final Map<TypeToken<? extends Service<?, ?>>, ServiceRepository<?, ?>> repositories;

    ServicePipeline() {
        this.repositories = new HashMap<>();
    }

    /**
     * Create a new {@link ServicePipelineBuilder}
     *
     * @return Builder instance
     */
    @Nonnull public static ServicePipelineBuilder builder() {
        return new ServicePipelineBuilder();
    }

    /**
     * Register a service type so that it is recognized by the pipeline
     *
     * @param type                  Service type
     * @param defaultImplementation Default implementation of the service. This must *always* be supplied
     *                              and will be the last service implementation that the pipeline attempts to
     *                              access. This will never be filtered out.
     * @param <Context>             Service context type
     * @param <Result>              Service result type
     * @return ServicePipeline The service pipeline instance
     */
    public <Context, Result> ServicePipeline registerServiceType(
        @Nonnull final TypeToken<? extends Service<Context, Result>> type,
        @Nonnull final Service<Context, Result> defaultImplementation) {
        if (repositories.containsKey(type)) {
            throw new IllegalArgumentException(
                String.format("Service of type '%s' has already been registered", type.toString()));
        }
        final ServiceRepository<Context, Result> repository = new ServiceRepository<>(type);
        repository.registerImplementation(defaultImplementation);
        this.repositories.put(type, repository);
        return this;
    }

    /**
     * Start traversing the pipeline by providing the context that will be used
     * to generate the results
     *
     * @param context Context
     * @param <Context> Context type
     * @return Service pumper instance
     */
    @Nonnull public <Context> ServicePumper<Context> pump(@Nonnull final Context context) {
        return new ServicePumper<>(this, context);
    }

    @Nonnull <Context, Result> ServiceRepository<Context, Result> getRepository(@Nonnull final TypeToken<? extends Service<Context, Result>> type) {
        final ServiceRepository<Context, Result> repository =
            (ServiceRepository<Context, Result>) this.repositories.get(type);
        if (repository == null) {
            throw new IllegalArgumentException(String.format("No service registered for type '%s'", type.toString()));
        }
        return repository;
    }

}
