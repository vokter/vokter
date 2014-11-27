package argus.parser;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import it.unimi.dsi.lang.MutableString;

import java.util.Iterator;
import java.util.Set;

/**
 * High-performance implementation of a sentence splitter using Lingpipe toolkit.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class SentenceSplitter {

    private TokenizerFactory TOKENIZER_FACTORY;
    private SentenceModel SENTENCE_MODEL;
    private SentenceChunker SENTENCE_CHUNKER;


    public SentenceSplitter() {
        this.TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
        this.SENTENCE_MODEL = new MedlineSentenceModel();
        this.SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY, SENTENCE_MODEL);
    }


    public int[][] split(MutableString text) {

        Chunking chunking = SENTENCE_CHUNKER.chunk(text.toCharArray(), 0, text.length());
        Set<Chunk> sentences = chunking.chunkSet();

        int size = sentences.size();
        int[][] indices = new int[size][2];

        int i = 0;
        for (Iterator<Chunk> it = sentences.iterator(); it.hasNext(); ) {

            Chunk sentence = it.next();
            int start = sentence.start();
            int end = sentence.end();

            indices[i][0] = start;
            indices[i][1] = end;

            i++;
        }

        return indices;
    }
}
