package argus.parser;

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
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
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
