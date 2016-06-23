/*
 * Copyright 2015 Eduardo Duarte
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

package com.edduarte.vokter.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A class that generates and returns a unique id from an incrementer, meaning that
 * every newly generated id will be the previously generated id + 1.
 * Note: This method differs from a simple static integer counter by being
 * thread-safe.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public final class SynchronizedCounter {
    private final AtomicLong sNextGeneratedId;


    public SynchronizedCounter() {
        sNextGeneratedId = new AtomicLong(1);
    }


    public SynchronizedCounter(int startAt) {
        sNextGeneratedId = new AtomicLong(startAt);
    }


    public long getAndIncrement() {
        for (; ; ) {
            final long result = sNextGeneratedId.get();
            long newValue = result + 1;

            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }


    public void reset() {
        sNextGeneratedId.set(1);
    }
}
