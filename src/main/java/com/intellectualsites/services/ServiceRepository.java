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
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Repository that contains implementations for a given service type
 *
 * @param <Context>  Service context
 * @param <Response> Service response type
 * @param <S>        Service type
 */
public class ServiceRepository<Context, Response> {

    private final Object lock = new Object();
    private final TypeToken<? extends Service<Context, Response>> serviceType;
    private final List<ServiceWrapper<? extends Service<Context, Response>>> implementations;

    /**
     * Create a new service repository for a given service type
     *
     * @param serviceType Service type
     */
    public ServiceRepository(@Nonnull final TypeToken<? extends Service<Context, Response>> serviceType) {
        this.serviceType = serviceType;
        this.implementations = new LinkedList<>();
    }

    /**
     * Register a new implementation for the service
     *
     * @param service Implementation
     * @param <T>     Type of the implementation
     */
    public <T extends Service<Context, Response>> void registerImplementation(@Nonnull final T service) {
        synchronized (this.lock) {
            this.implementations.add(new ServiceWrapper<>(service));
        }
    }

    /**
     * Get a queue containing all implementations
     *
     * @return Queue containing all implementations
     */
    @Nonnull public Deque<ServiceWrapper<? extends Service<Context, Response>>> getQueue() {
        synchronized (this.lock) {
            return new LinkedList<>(this.implementations);
        }
    }


    /**
     * Used to store {@link Service} implementations together
     * with their state
     */
    final class ServiceWrapper<T extends Service<Context, Response>> {

        private final boolean defaultImplementation;
        private final T implementation;

        private ServiceWrapper(@Nonnull final T implementation) {
            this.defaultImplementation = implementations.isEmpty();
            this.implementation = implementation;
        }

        @Nonnull T getImplementation() {
            return this.implementation;
        }

        boolean isDefaultImplementation() {
            return this.defaultImplementation;
        }

        @Override public String toString() {
            return String
                .format("ServiceWrapper{type=%s,implementation=%s}", serviceType.toString(),
                    TypeToken.of(implementation.getClass()).toString());
        }

    }

}
