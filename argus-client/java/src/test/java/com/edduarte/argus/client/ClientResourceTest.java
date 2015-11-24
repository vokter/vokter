/*
 * Copyright 2015 Ed Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.argus.client;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.4.1
 * @since 1.0.0
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientResourceTest {

    private ClientResource resource;


    @Before
    public void setup() {
        resource = new ClientResource();
    }


    /**
     * Tests successful watch method.
     */
    @Test
    public void testA() {
        Response response = resource.watch("http://bbc.com", "a,the");

        // should be a successful response
        assertEquals(response.getStatus(), 200);
    }


    /**
     * Tests unsuccessful watch method.
     */
    @Test
    public void testB() {
        Response response = resource.watch("http://bbc.com", "a,the");

        // should be a 400 bad request (combination of document and client already exists)
        assertEquals(response.getStatus(), 400);
    }


    /**
     * Tests successful cancel method.
     */
    @Test
    public void testC() {
        Response response = resource.cancel("http://bbc.com");

        // should be a successful response
        assertEquals(response.getStatus(), 200);
    }


    /**
     * Tests unsuccessful cancel method.
     */
    @Test
    public void testD() {
        Response response = resource.cancel("http://bbc.com");

        // should be a 400 bad request (combination of document and client no longer exists)
        assertEquals(response.getStatus(), 400);
    }


    @After
    public void cleanup() {
        resource = null;
    }
}
