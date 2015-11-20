/*
 * Copyright 2014 Ed Duarte
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

package com.edduarte.argus.stopper;

import com.edduarte.argus.parser.Parser;
import com.edduarte.argus.parser.SimpleParser;
import com.edduarte.argus.util.Constants;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Module that checks if the received textual state of a occurrence corresponds
 * to a stopword loaded from a local file.
 * <p>
 * The file's contents are read by line, including every word in each line as a
 * stopword. When the pipe character '|' is detected, the remaining text from
 * the line is ignored.
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.0
 * @since 1.0.0
 */
public class FileStopper implements Stopper {

    private static final Logger logger = LoggerFactory.getLogger(FileStopper.class);

    private Set<MutableString> stopwords;


    public FileStopper(String language) {
        boolean loadSuccessful = load(language);
        if (!loadSuccessful) {
            this.stopwords = ImmutableSet.of();
        }
    }


    private boolean load(String language) {
        File stopwordsFile = new File(Constants.STOPWORDS_DIR, language + ".txt");
        if (stopwordsFile.exists()) {
            try (InputStream is = new FileInputStream(stopwordsFile);
                 Parser parser = new SimpleParser()) {

                Set<MutableString> stopwordsAux = new HashSet<>();
                List<String> lines = IOUtils.readLines(is);
                lines.forEach(line -> {
                    line = line.trim().toLowerCase();
                    if (!line.isEmpty()) {
                        int indexOfPipe = line.indexOf('|');

                        MutableString stopwordLine;
                        if (indexOfPipe == -1) {
                            // there are no pipes in this line
                            // -> add the whole line as a stopword
                            stopwordLine = new MutableString(line);

                        } else if (indexOfPipe > 0) {
                            // there is a pipe in this line and it's not the first char
                            // -> add everything from index 0 to the pipe's index
                            String word = line.substring(0, indexOfPipe).trim();
                            stopwordLine = new MutableString(word);
                        } else {
                            return;
                        }

                        Set<MutableString> stopwordsAtLine = parser
                                .parse(stopwordLine)
                                .parallelStream()
                                .map(sw -> sw.text)
                                .collect(Collectors.toSet());
                        stopwordsAux.addAll(stopwordsAtLine);
                    }
                });

                stopwords = ImmutableSet.copyOf(stopwordsAux);
                return true;

            } catch (IOException e) {
                logger.error("There was a problem loading the stopword file.", e);
                stopwords = Collections.emptySet();
            }
        }

        return false;
    }


    @Override
    public boolean isStopword(MutableString occurrenceText) {
        return stopwords.contains(occurrenceText);
    }


    @Override
    public boolean isEmpty() {
        return stopwords.isEmpty();
    }


    @Override
    public void destroy() {
        stopwords = null;
    }
}
