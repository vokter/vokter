package argus.parser;

import argus.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GDep Parser wrapper implementation.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class GDepParser implements AutoCloseable {

    private static Logger logger = LoggerFactory.getLogger(GDepParser.class);

    private String[] parserCommand;
    private File dir;
    private PipedInputStream pis;
    private PipedOutputStream sink;
    private PipedOutputStream pos;
    private PipedInputStream source;
    private BufferedReader br;
    private BufferedWriter bw;
    private ProcessConnector pc;
    private SentenceSplitter splitter;


    public GDepParser() {
        this.splitter = new SentenceSplitter();

        // Set new parser path
        String toolPath = Constants.GDEP_DIR.getAbsolutePath();

        List<String> command = new ArrayList<>();
        command.add(toolPath);
        command.add("-tok");

        dir = Constants.GDEP_DIR;
        parserCommand = command.toArray(new String[command.size()]);
    }


//    private void addArguments(final ParserLevel parserLevel,
//                              final boolean doWhiteSpaceTokenization, List<String> command) {
//        if (doWhiteSpaceTokenization) {
//            command.add("-wst");
//        }
//
//        if (parserLevel.equals(ParserLevel.TOKENIZATION)) {
//            command.add("-tok");
//        } else if (parserLevel.equals(ParserLevel.POS)) {
//            command.add("-pos");
//        } else if (parserLevel.equals(ParserLevel.LEMMATIZATION)) {
//            command.add("-lemma");
//        } else if (parserLevel.equals(ParserLevel.CHUNKING)) {
//            command.add("-chunk");
//        } else if (parserLevel.equals(ParserLevel.DEPENDENCY)) {
//            command.add("-dep");
//        }
//    }


    /**
     * Launch the parser.
     *
     * @throws IOException Problem launching the parser.
     */
    public GDepParser launch() throws IOException {
        pis = new PipedInputStream();
        sink = new PipedOutputStream(pis);
        pos = new PipedOutputStream();
        source = new PipedInputStream(pos);
        br = new BufferedReader(new InputStreamReader(source));
        bw = new BufferedWriter(new OutputStreamWriter(sink));

        pc = new ProcessConnector(pis, pos, new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
            }
        }));


        if (dir == null) {
            pc.create(parserCommand);
        } else {
            pc.create(dir, parserCommand);
        }
        return this;
    }

    /**
     * Terminates the execution of the parser.
     */
    @Override
    public void close() {
        try {
            pis.close();
            pis = null;
            sink.close();
            sink = null;
            pos.close();
            pos = null;
            source.close();
            source = null;
            br.close();
            br = null;
            bw.close();
            bw = null;
            pc.destroy();
            System.gc();
        }catch (IOException ex){
            logger.error("Error terminating GDep parser: "+ex.toString());
        }
    }

//    /**
//     * Parse the specified text using GDepTranslator, creating and structuring Sentences and
//     * adding them to the specified Corpus
//     *
//     * @param text The text to be parsed.
//     * @throws NejiException Problem parsing the sentence.
//     */
//    @Override
//    public List<Sentence> parse(Corpus corpus, String text) throws NejiException {
//        return parseWithLevel_(level, corpus, text);
//    }
//
//
//    @Override
//    public List<Sentence> parseWithLevel_(ParserLevel parserLevel, Corpus corpus, String text) throws NejiException {
//        List<Sentence> _LIST = new ArrayList<>();
//        if (!isLaunched()) {
//            return null;
//        }
//        int[][] splitIdx = splitter.split(text);
//        List<Pair<Integer, Integer>> splitPairList = new ArrayList<>();
//
//        for(int[] newSplittedSentence : splitIdx) {
//            int start = newSplittedSentence[0];
//            int end = newSplittedSentence[1];
//            splitPairList.add(new Pair<>(start, end));
//        }
//
//        List<List<Object>> parserOutputList = new ArrayList<>();
//        try {
//            for(Pair<Integer, Integer> pair : splitPairList) {
//                String s = text.substring(pair.a(), pair.b());
//
//                List<Object> results = new ArrayList<Object>();
//                parserOutputList.add(results);
//
//                String parserLevelArg = levelToArg(parserLevel);
//                bw.write(parserLevelArg + "|" + s.trim() + "\n");
////                bw.write(s.trim() + "\n");
//                bw.flush();
//
//                while (!br.ready()) {
//                    // wait for results
//                    Thread.yield();
//                }
//                String line;
//
//                while (!(line = br.readLine()).equalsIgnoreCase("")) {
//                    results.add(line);
//                }
//            }
//        } catch (IOException ex) {
//            throw new NejiException("An error occurred while parsing the sentence.", ex);
//        }
//
//        int k = 0;
//        for(Pair<Integer, Integer> pair : splitPairList) {
//            String sentenceText = text.substring(pair.a(), pair.b());
//            Sentence sentence = new Sentence(corpus, sentenceText);
//            sentence.setStart(pair.a());
//            sentence.setEnd(pair.b());
//            translate(sentence, parserOutputList.get(k++));
//            corpus.addSentence(sentence);
//            _LIST.add(sentence);
//        }
//
//        return _LIST;
//    }
//
//    private String levelToArg(final ParserLevel parserLevel){
//        switch (parserLevel){
//            case TOKENIZATION: return "tok";
//            case POS: return "pos";
//            case LEMMATIZATION: return "lem";
//            case CHUNKING: return "chu";
//            case DEPENDENCY: return "dep";
//            default: return null;
//        }
//    }
//
//    private void translate(Sentence sentence, final List<Object> parserOutput) {
//        int start = 0, end = 0, tokenCounter = 0, offset = 0;
//
//        UndirectedGraph<Token, LabeledEdge> dependencyGraph = new SimpleGraph<>(LabeledEdge.class);
//
//        DependencyList dependencyList = new DependencyList();
//        List<String> chunkTags = new ArrayList<String>();
//        for (Object result : parserOutput) {
//            String[] parts = result.toString().split("\t");
//
//            // Get parsing results
//            String tokenText = null, lemma = null, pos = null, chunk = null;
//            DependencyTag depTag = null;
//            Integer depToken = null;
//            if (parts.length >= 3) { // Tokenization parsing
//                tokenText = parts[1];
//            }
//            if (parts.length >= 4) { // Lemmatization parsing
//                lemma = parts[2];
//            }
//            if (parts.length >= 5) { // POS parsing
//                pos = parts[3];
//            }
//            if (parts.length >= 6) { // Chunking parsing
//                chunk = parts[3];
//                pos = parts[4];
//            }
//            if (parts.length >= 8) { // Dependency Parsing
//                depToken = Integer.valueOf(parts[6]) - 1;
//                depTag = DependencyTag.valueOf(parts[7]);
//                pos = parts[4];
//                chunk = parts[3];
//            }
//
//            // Get end without white spaces
//            end = start + tokenText.length() - 1;
//
//            // Create token
//            Token token = new Token(sentence, start, end, tokenCounter++);
//
//            String sentenceText = sentence.getText();
//            if (sentenceText != null) {
//                // Get start and end source with white spaces
//                start = sentenceText.indexOf(tokenText, offset);
//                end = start + tokenText.length() - 1;
//
//                // Set start and end source
//                token.setStart(start);
//                token.setEnd(end);
//            }
//
//            // Add features
//            if (lemma != null) {
//                token.putFeature("LEMMA", lemma);
////                token.addFeature("LEMMA=" + lemma);
//            }
//            if (pos != null) {
//                token.putFeature("POS", pos);
////                token.addFeature("POS=" + pos);
//            }
//            if (chunk != null) {
//                token.putFeature("CHUNK", chunk);
//                chunkTags.add(chunk);
////                token.addFeature("CHUNK=" + chunk);
//            }
//
//
//            // Add dependencies to build graph.
//            if (depToken != null && depTag != null) {
//                dependencyList.add(new Dependency(depTag, depToken));
//                dependencyGraph.addVertex(token);
//            }
//
//            // Add token to sentence
//            sentence.addToken(token);
//
//            // Offsets
//            start += tokenText.length();
//            offset = end + 1;
//        }
//
//        // Add dependency edges
//        if (!dependencyList.isEmpty()) {
//            // Add edges
//            for (int i = 0; i < dependencyList.size(); i++) {
//                Dependency dependency = dependencyList.get(i);
//                if (dependency.tag.equals(DependencyTag.ROOT)) {
//                    continue;
//                }
//
//                Token from = sentence.getToken(i);
//                Token to = sentence.getToken(dependency.index);
//
//                LabeledEdge<Token, DependencyTag> edge = new LabeledEdge<>(from, to, dependency.tag);
//                dependencyGraph.addEdge(from, to, edge);
//            }
//        }
//        // Set dependency graph
//        sentence.setDependencyGraph(dependencyGraph);
//
//        // Add chunks
//        ChunkList chunkList = new ChunkList(sentence);
//        if (!chunkTags.isEmpty()) {
//            chunkList = getChunkList(sentence, chunkTags);
//        }
//        sentence.setChunks(chunkList);
//
//    }
//
//    private class Dependency {
//        DependencyTag tag;
//        int index;
//
//        private Dependency(DependencyTag tag, int index) {
//            this.tag = tag;
//            this.index = index;
//        }
//    }

//    private class DependencyList extends ArrayList<Dependency> {
//    }
}

