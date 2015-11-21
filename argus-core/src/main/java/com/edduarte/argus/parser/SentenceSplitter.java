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

package com.edduarte.argus.parser;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import it.unimi.dsi.lang.MutableString;

import java.util.Set;

/**
 * High-performance implementation of a sentence splitter using Lingpipe toolkit.
 *
 * @author Ed Duarte (<a href="mailto:ed@edduarte.com">ed@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class SentenceSplitter {

    private SentenceChunker chunker;


    public SentenceSplitter() {
        TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
        SentenceModel sentenceModel = new MedlineSentenceModel();
        this.chunker = new SentenceChunker(tokenizerFactory, sentenceModel);
    }


    public int[][] split(MutableString text) {

        Chunking chunking = chunker.chunk(text.toCharArray(), 0, text.length());
        Set<Chunk> sentences = chunking.chunkSet();

        int size = sentences.size();
        int[][] indices = new int[size][2];

        int i = 0;
        for (Chunk sentence : sentences) {
            int start = sentence.start();
            int end = sentence.end();

            indices[i][0] = start;
            indices[i][1] = end;

            i++;
        }

        return indices;
    }
}
