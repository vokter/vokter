package argus.parser;

import argus.stemmer.Stemmer;
import argus.stopper.StopwordsLoader;
import argus.util.Constants;
import com.aliasi.util.Pair;
import it.unimi.dsi.lang.MutableString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * High performance dependency parser wrapper implementation, currently being used
 * only for tokenization purposes.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class GeniaParser implements AutoCloseable {

    private static final String BINARY_WIN = "win32.exe";
    private static final String BINARY_LIN64 = "./linux64";
    private static final String BINARY_LIN32 = "./linux32";
    private static final String BINARY_MAC = "./mac64";

    private static Logger logger = LoggerFactory.getLogger(GeniaParser.class);

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
    private boolean isLaunched;
    private StopwordsLoader stopwordsLoader;
    private Stemmer stemmer;
    private boolean ignoreCase = false;

    public GeniaParser() {
        this.splitter = new SentenceSplitter();

        this.dir = Constants.PARSER_DIR;

        List<String> command = new ArrayList<>();
        command.add(new File(dir, getCompatibleBinary()).getAbsolutePath());
        command.add("-tok");
        this.parserCommand = command.toArray(new String[command.size()]);
    }

    public static String getCompatibleBinary() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("windows")) {
            return BINARY_WIN;
        } else if (os.contains("mac")) {
            return BINARY_MAC;
        } else {
            if (System.getProperty("os.arch").contains("64"))
                return BINARY_LIN64;
            else
                return BINARY_LIN32;
        }
    }

    /**
     * Launches the parser.
     */
    public GeniaParser launch() throws IOException {
        if (isLaunched) {
            return this;
        }
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
        isLaunched = true;
        return this;
    }

    /**
     * Terminates this parser.
     */
    @Override
    public void close() {
        if (!isLaunched) {
            return;
        }

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
        } catch (IOException ex) {
            logger.error("Error terminating GDep parser: " + ex.toString());
        }
    }

    public void setupStopwords(final StopwordsLoader stopwordsLoader) {
        this.stopwordsLoader = stopwordsLoader;
    }


    public void setupStemming(final Stemmer stemmer) {
        this.stemmer = stemmer;
    }


    public void setupIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }


    /**
     * Parses the specified text and obtains tokenized results.
     */
    public List<ParserResult> parse(MutableString text) {
        List<ParserResult> _results = new ArrayList<>();
        if (!isLaunched) {
            return _results;
        }

        int[][] idx = splitter.split(text);
        List<Pair<Integer, Integer>> splitPairList = new ArrayList<>();

        for (int[] split : idx) {
            int start = split[0];
            int end = split[1];
            splitPairList.add(new Pair<>(start, end));
        }

        try {
            for (Pair<Integer, Integer> pair : splitPairList) {
                MutableString sentenceText = text.substring(pair.a(), pair.b());

                List<Object> parserOutput = new ArrayList<>();

                bw.write("tok|" + sentenceText.trim().toString() + "\n");
                bw.flush();

                while (!br.ready()) {
                    // wait for results
                    Thread.yield();
                }
                String line;

                while (!(line = br.readLine()).equals("")) {
                    parserOutput.add(line);
                }

                int start = 0, end = 0, tokenCounter = 0, offset = 0;

                for (Object result : parserOutput) {
                    String[] parts = result.toString().split("\t");

                    // Get parsing results
                    MutableString tokenText = new MutableString(parts[1]);

                    // Get start and end source with white spaces
                    start = sentenceText.indexOf(tokenText, offset);
                    end = start + tokenText.length();

                    if (ignoreCase) {
                        tokenText.toLowerCase();
                    }
                    // checks if the text is a stopword
                    // if true, do not stem it nor add it to the ParserResult list
                    boolean isStopword = false;
                    if (stopwordsLoader != null) {
                        MutableString textToTest = tokenText;
                        if (!ignoreCase) {
                            textToTest = textToTest.copy().toLowerCase();
                        }
                        isStopword = stopwordsLoader.isStopword(textToTest);
                    }
                    if (!isStopword) {

                        // stems the term text
                        if (stemmer != null) {
                            stemmer.stem(tokenText);
                        }

                        _results.add(new ParserResult(tokenCounter, start, end, tokenText));
                    }

                    tokenCounter++;

                    // Offsets
                    start += tokenText.length();
                    offset = end + 1;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("An error occurred while parsing the sentence.", ex);
        }

        return _results;
    }
}

