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

package com.edduarte.vokter.parser;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * A parser-pool that contains a set number of parsers. When the last parser
 * from the pool is removed, future parsing workers will be locked until
 * a used parser is placed back in the pool.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class ParserPool {

    private final LinkedBlockingQueue<Parser> parsersQueue;


    public ParserPool() {
        this.parsersQueue = new LinkedBlockingQueue<>();
    }


    public Parser take() throws InterruptedException {
        return parsersQueue.take();
    }


    public void place(Parser parser) throws InterruptedException {
        this.parsersQueue.put(parser);
    }


    public void clear() {
        while (!parsersQueue.isEmpty()) {
            try {
                Parser parser = parsersQueue.take();
                parser.close();
                parser = null;
            } catch (InterruptedException ex) {
                throw new RuntimeException("There was a problem terminating the parsers.", ex);
            }
        }
        this.parsersQueue.clear();
    }
}
