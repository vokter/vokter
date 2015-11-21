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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Process connector of an external executable.
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.0
 * @since 1.0.0
 */
public class ProcessWrapper {

    private InputStream is;

    private OutputStream os;

    private OutputStream es;

    private Process process;

    private Thread tis;

    private Thread tos;

    private Thread tes;


    public ProcessWrapper(InputStream is, OutputStream os, OutputStream es) {
        this.is = is;
        this.os = os;
        this.es = es;
    }


    public void create(File dir, String... command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        pb.directory(dir);
        process = pb.start();

        tis = new Thread(new StreamGobbler(is, process.getOutputStream()));
        tis.start();
        tos = new Thread(new StreamGobbler(process.getInputStream(), os));
        tos.start();
        tes = new Thread(new StreamGobbler(process.getErrorStream(), es));
        tes.start();


//        Thread.setDefaultUncaughtExceptionHandler((thread, thrwbl) -> {
//            tis.stop();
//            tos.stop();
//            tes.stop();
//        });
    }


    /**
     * Creates a process connector by holding the process launched by the specified
     * command line as a wrapped object.
     *
     * @param command the command line to be executed.
     * @throws IOException there was problem executing the command line.
     */
    public void create(String... command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        process = pb.start();

        tis = new Thread(new StreamGobbler(is, process.getOutputStream()));
        tis.start();
        tos = new Thread(new StreamGobbler(process.getInputStream(), os));
        tos.start();
        tes = new Thread(new StreamGobbler(process.getErrorStream(), es));
        tes.start();


//        Thread.setDefaultUncaughtExceptionHandler((thread, thrwbl) -> {
//            tis.stop();
//            tos.stop();
//            tes.stop();
//        });
    }


    public void destroy() {
        process.destroy();
        tis.interrupt();
        tos.interrupt();
        tes.interrupt();
    }
}

