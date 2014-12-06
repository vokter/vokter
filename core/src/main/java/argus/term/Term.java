package argus.term;

import com.mongodb.BasicDBObject;
import it.unimi.dsi.lang.MutableString;

import java.util.ArrayList;


/**
 * A Term represents the most basic information unit, corresponding to a
 * indexable and searchable field of text.
 * <p/>
 * This class was named Token in the previous assignment.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public final class Term extends BasicDBObject {

    public static final String TEXT = "text";
    public static final String WEIGHT = "weight";
    public static final String OCCURRENCES = "occurrences";
    private static final int SNIPPET_OFFSET = 100;

//    /**
//     * The text that represents this term.
//     * If the tokenizer that collected this term had stemming enabled, then
//     * the text stored here is stemmed. To retrieve the original state of the
//     * text represented by this term within a specific document, the 'snippet'
//     * for that document must be obtained instead, using the occurrences set
//     * below.
//     */
//    private MutableString text;
//
//
//    /**
//     * List that stores the indexes of their occurrences in the parent
//     * document.
//     */
//    private Set<Occurrence> occurrences;
//
//
//    /**
//     * Map that stores the normalized weights of this term for each document id.
//     */
//    private double normalizedWeight;


//    /**
//     * Constructor used by the Index term-cache, when loading a term from a local
//     * index file. This is the inverse process of the 'writeLine(Writer writer)'
//     * method below.
//     */
//    static Term parseLine(final String line) throws IOException {
//        int tokenTextEndIndex = line.indexOf(":");
//        if (tokenTextEndIndex <= 0) {
//            throw new IOException("Invalid line '" + line + "'!");
//        }
//        String tokenText = line.substring(0, tokenTextEndIndex);
//        Term term = new Term(new MutableString(tokenText));
//
//        int nlizeEndIndex = line.indexOf('=', tokenTextEndIndex + 1);
//        if (nlizeEndIndex < 0) {
//            throw new IOException("Invalid line for term '" + tokenText + "'!");
//        }
//        String nlizeString = line.substring(tokenTextEndIndex + 1, nlizeEndIndex);
//        term.normalizedWeight = Double.parseDouble(nlizeString);
//
//        String[] occurrences = split(line.substring(nlizeEndIndex + 1), '|');
//        for (String o : occurrences) {
//            String[] oArgs = split(o, '-');
//
//            String posString = oArgs[0];
//            int pos = Integer.parseInt(posString);
//
//            String startString = oArgs[1];
//            int initialize = Integer.parseInt(startString);
//
//            String endString = oArgs[2];
//            int end = Integer.parseInt(endString);
//
//            term.occurrences.add(new Occurrence(term, pos, initialize, end));
//        }
//        return term;
//    }


    /**
     * Constructor used by the DocumentPipeline, when the specified text
     * corresponds to a new, unique term in the parent document.
     */
    public Term(final MutableString text) {
        super(TEXT, text);
        append(WEIGHT, 1);
        append(OCCURRENCES, new ArrayList<>());
    }


    /**
     * Constructor used by the TermLoader, when obtaining a term from a query to
     * the MongoDB term collection.
     */
    public Term(final BasicDBObject mongoObject) {
        super(mongoObject);
    }


    /**
     * Sets this term as occurring in the specified document at the specified
     * phrase pointer and at the specified starting and ending positions.
     */
    public void addOccurrence(int position, int start, int end) {
        ArrayList occurrencesArray = (ArrayList) get(OCCURRENCES);
        occurrencesArray.add(new Occurrence(position, start, end));
    }


    /**
     * Sets this term normalized weight factor.
     */
    public void addNormalizedWeight(double nlize) {
        put(WEIGHT, nlize);
    }


    /**
     * Returns this term normalized weight factor.
     */
    public double getNormalizedWeight() {
        return (double) get(WEIGHT);
    }


    /**
     * Returns the term frequency (tf) of this term t in the document d, which is
     * the number of occurrences of t in d.
     */
    public int getTermFrequency() {
        ArrayList occurrencesArray = (ArrayList) get(OCCURRENCES);
        return occurrencesArray.size();
    }


    /**
     * Returns this term weight based on the log tf weighting scheme.
     */
    public double getLogFrequencyWeight() {
        double tf = getTermFrequency();

        if (tf > 0) {
            return 1.0 + Math.log10(tf);

        } else {
            return 0;
        }
    }


//    /**
//     * Writes this term index / occurrence list into a string line on the
//     * specified writer. This is the inverse process of the 'parseLine(String line)'
//     * method above.
//     */
//    public String toIndexLine() throws IOException {
//        StringBuilder sb = new StringBuilder();
//        sb.append(text);
//        sb.append(':');
//        sb.append(Double.toString(normalizedWeight));
//        sb.append('=');
//
//        for (Iterator<Occurrence> it2 = occurrences.iterator(); it2.hasNext(); ) {
//            Occurrence occ = it2.next();
//            sb.append(Integer.toString(occ.getWordCount()));
//            sb.append('-');
//            sb.append(Integer.toString(occ.getStartPosition()));
//            sb.append('-');
//            sb.append(Integer.toString(occ.getEndPosition()));
//
//            if (it2.hasNext()) {
//                sb.append('|');
//            }
//        }
//        sb.append("\n");
//        return sb.toString();
//    }


//    /**
//     * Uses the specified document contents to retrieve a string with every snippet
//     * of occurrence of this term in the specified document id. The number specified
//     * limit the number of snippets that are returned.
//     */
//    public String getSummaryForDocument(Document d, int numSnippets) {
//        MutableString contents = d.getContentSnapshot();
//
//        StringBuilder sb = new StringBuilder();
//        int count = 0;
//        for (Iterator<Occurrence> it = occurrences.get(documentId).iterator(); it.hasNext(); ) {
//            Occurrence o = it.next();
//            int snippetStart = o.getStartPosition() - SNIPPET_OFFSET;
//            if (snippetStart < 0) {
//                snippetStart = 0;
//            }
//            int snippetEnd = o.getEndPosition() + 1 + SNIPPET_OFFSET;
//            if (snippetEnd > contents.length()) {
//                snippetEnd = contents.length();
//            }
//
//            if (snippetStart != 0 && count == 0) {
//                sb.append("... ");
//            }
//            sb.append(contents.substring(snippetStart, o.getStartPosition()));
//            sb.append("<b>");
//            sb.append(contents.substring(o.getStartPosition(), o.getEndPosition() + 1));
//            sb.append("</b>");
//            sb.append(contents.substring(o.getEndPosition() + 1, snippetEnd));
//
//            count++;
//            if (count == numSnippets) {
//                if (snippetEnd != contents.length()) {
//                    sb.append(" ...");
//                }
//                break;
//            }
//
//            if (it.hasNext()) {
//                sb.append(" ... ");
//            }
//        }
//
//        return sb.toString();
//    }


//    /**
//     * Tests the equality of this term with the specified object.
//     */
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Term term = (Term) o;
//        return text.equals(term.text);
//    }
//
//
//    /**
//     * Returns the hashcode of this term.
//     */
//    @Override
//    public int hashCode() {
//        return text.hashCode();
//    }


    /**
     * Returns the text that represents this term.
     */
    @Override
    public String toString() {
        MutableString text = (MutableString) get(TEXT);
        return text.toString();
    }

}

