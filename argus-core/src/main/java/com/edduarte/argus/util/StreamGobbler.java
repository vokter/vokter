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

package com.edduarte.argus.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An input and output data exchanger between the application and an external
 * executable.
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class StreamGobbler implements Runnable {

    private final InputStream is;

    private final OutputStream os;


    public StreamGobbler(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }


    /**
     * Maps the input and output data.
     */
    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1 << 12];
            int c;
            while ((c = is.read(buffer)) != -1) {
                os.write(buffer, 0, c);
                os.flush();
            }
        } catch (IOException ex) {
            //logger.error("There was a problem writing the output.", ex);
            return;
        }
    }
}

