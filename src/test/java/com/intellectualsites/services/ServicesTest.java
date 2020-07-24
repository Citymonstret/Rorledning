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
import com.intellectualsites.services.mock.MockService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServicesTest {

    @Test public void testPipeline() throws Exception {
        final ServicePipeline servicePipeline = ServicePipeline.builder().build();
        Assertions.assertNotNull(servicePipeline);
        servicePipeline.registerServiceType(TypeToken.of(MockService.class), new DefaultMockService());
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            servicePipeline.registerServiceType(TypeToken.of(MockService.class), new DefaultMockService());
        });
        Assertions.assertEquals(32, servicePipeline.pump(new MockService.MockContext("Hello")).through(MockService.class)
            .getResult().getInteger());
        Assertions.assertNotNull(servicePipeline.pump(new MockService.MockContext("oi")).through(MockService.class).getResultAsynchronously().get());
    }

}
