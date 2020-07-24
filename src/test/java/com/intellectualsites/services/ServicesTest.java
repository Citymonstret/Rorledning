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
import com.intellectualsites.services.mock.DefaultMockService;
import com.intellectualsites.services.mock.DefaultSideEffectService;
import com.intellectualsites.services.mock.MockService;
import com.intellectualsites.services.mock.MockSideEffectService;
import com.intellectualsites.services.mock.SecondaryMockService;
import com.intellectualsites.services.mock.SecondaryMockSideEffectService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class ServicesTest {

    @Test public void testPipeline() throws Exception {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build();
        Assertions.assertNotNull(servicePipeline);
        servicePipeline.registerServiceType(TypeToken.of(MockService.class), new DefaultMockService());
        Assertions.assertThrows(IllegalArgumentException.class, () -> servicePipeline.registerServiceType(TypeToken.of(MockService.class), new DefaultMockService()));
        final SecondaryMockService secondaryMockService = new SecondaryMockService();
        servicePipeline.registerServiceImplementation(TypeToken.of(MockService.class), secondaryMockService,
            Collections.singleton(secondaryMockService));
        servicePipeline.registerServiceImplementation(MockService.class, mockContext -> new MockService.MockResult(-91),
            Collections.singleton(mockContext -> mockContext.getString().startsWith("-91")));
        Assertions.assertEquals(32, servicePipeline.pump(new MockService.MockContext("Hello")).through(MockService.class)
            .getResult().getInteger());
        Assertions.assertEquals(999, servicePipeline.pump(new MockService.MockContext("potato")).through(MockService.class)
            .getResult().getInteger());
        Assertions.assertEquals(-91, servicePipeline.pump(new MockService.MockContext("-91")).through(MockService.class)
            .getResult().getInteger());
        Assertions.assertNotNull(servicePipeline.pump(new MockService.MockContext("oi")).through(MockService.class).getResultAsynchronously().get());
    }

    @Test public void testSideEffectServices() throws Exception {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build();
        servicePipeline.registerServiceType(TypeToken.of(MockSideEffectService.class), new DefaultSideEffectService());
        final MockSideEffectService.MockPlayer mockPlayer = new MockSideEffectService.MockPlayer(20);
        Assertions.assertEquals(20, mockPlayer.getHealth());
        Assertions.assertEquals(State.ACCEPTED, servicePipeline.pump(mockPlayer).through(MockSideEffectService.class).getResult());
        Assertions.assertEquals(0, mockPlayer.getHealth());
        mockPlayer.setHealth(20);
        servicePipeline.registerServiceImplementation(MockSideEffectService.class, new SecondaryMockSideEffectService(), Collections.emptyList());
        Assertions.assertThrows(IllegalStateException.class, () -> servicePipeline.pump(mockPlayer).through(MockSideEffectService.class).getResult());
    }

}
