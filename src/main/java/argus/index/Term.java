package argus.index;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import gnu.trove.TCollections;
import gnu.trove.TDecorators;
import gnu.trove.map.TLongDoubleMap;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import it.unimi.dsi.lang.MutableString;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.split;

/**
 * A Term represents the most basic information unit, corresponding to a
 * indexable and searchable field of text.
 * <p/>
 * This class was named Token in the previous assignment.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public final class Term {

    private static final int SNIPPET_OFFSET = 42;


    /**
     * The text that represents this term.
     * If the tokenizer that collected this term had stemming enabled, then
     * the text stored here is stemmed. To retrieve the original state of the
     * text represented by this term within a specific document, the 'snippet'
     * for that document must be looks instead, using the occurrences multimap
     * below.
     */
    private MutableString text;


    /**
     * Map that stores the indexes of their occurrences in each corresponding
     * document id. A multimap structure was preferred to a simple map structure
     * since it allows multiple occurrences to be stored per document id.
     * Another reason was, with such a structure, it is easy to find
     * the total number of occurrences (including multiple occurrences per
     * document) by calling {@link Multimap#size()}, which is the size of total
     * key-value entries, and the number of unique occurrences (excluding multiple
     * occurrences per document) by calling {@link Multimap#keySet().size()}.
     */
    private Multimap<Long, Occurrence> occurrences;


    /**
     * Map that stores the normalized weights of this term for each document id.
     */
    private TLongDoubleMap normalizedWeights;


    /**
     * Constructor used by the Tokenizer, when the specified text corresponds to a
     * new, unique term.
     */
    static Term of(final MutableString text) {
        return new Term(text);
    }


    /**
     * Constructor used by the Index term-cache, when loading a term from a local
     * index file. This is the inverse process of the 'writeLine(Writer writer)'
     * method below.
     */
    static Term parseLine(final String line) throws IOException {
        int tokenTextEndIndex = line.indexOf(":");
        if (tokenTextEndIndex <= 0) {
            throw new IOException("Invalid line '" + line + "'!");
        }
        String tokenText = line.substring(0, tokenTextEndIndex);
        Term term = new Term(new MutableString(tokenText));

        String[] s = split(line.substring(tokenTextEndIndex + 1), ' ');
        for (String documentString : s) {
            int docIdEndIndex = documentString.indexOf('=');
            if (docIdEndIndex < 0) {
                throw new IOException("Invalid line for term '" + tokenText + "'!");
            }
            String docIdString = documentString.substring(0, docIdEndIndex);
            long docId = Long.parseLong(docIdString);

            int nlizeEndIndex = documentString.indexOf('=', docIdEndIndex + 1);
            if (nlizeEndIndex < 0) {
                throw new IOException("Invalid line for term '" + tokenText + "'!");
            }
            String nlizeString = documentString.substring(docIdEndIndex + 1, nlizeEndIndex);
            double nlize = Double.parseDouble(nlizeString);
            term.normalizedWeights.put(docId, nlize);

            String[] occurrences = split(documentString.substring(nlizeEndIndex + 1), '|');
            for (String o : occurrences) {
                String[] oArgs = split(o, '-');

                String posString = oArgs[0];
                int pos = Integer.parseInt(posString);

                String startString = oArgs[1];
                int start = Integer.parseInt(startString);

                String endString = oArgs[2];
                int end = Integer.parseInt(endString);

                term.occurrences.put(docId, new Occurrence(term, pos, start, end));
            }
        }
        return term;
    }


    private Term(final MutableString text) {
        this.text = text;

        // instantiates a map holding primitive integers (instead of Integer
        // objects) as keys and ArrayList<Occurrence> as values
        this.occurrences = Multimaps.synchronizedListMultimap(
                Multimaps.newListMultimap(
                        TDecorators.wrap(new TLongObjectHashMap<>()),
                        ArrayList::new
                ));

        // instantiates a map holding primitive integers (instead of Integer
        // objects) as keys and primitive doubles (instead of Double objects) as
        // values
        this.normalizedWeights = TCollections.synchronizedMap(new TLongDoubleHashMap());
    }


    /**
     * Sets this term as occurring in the specified document at the specified
     * phrase pointer and at the specified starting and ending positions.
     */
    public void addOccurrence(long documentId, int position, int start, int end) {
        occurrences.put(documentId, new Occurrence(this, position, start, end));
    }


    /**
     * Sets this term weight factor for the specified document.
     */
    public void addNormalizedWeight(long documentId, double nlize) {
        normalizedWeights.put(documentId, nlize);
    }


    /**
     * Returns this term weight factor for the specified document.
     */
    public double getWeightOfDocument(long documentId) {
        return normalizedWeights.get(documentId);
    }


    /**
     * Checks if this term occurs in the specified document.
     *
     * @param documentId the id of the document to test if this term occurs
     * @return <tt>true</tt> if this term occurs in the specified document,
     * <tt>false</tt> in case otherwise
     */
    public boolean occursInDocument(long documentId) {
        return occurrences.containsKey(documentId);
    }


    /**
     * Gets this terms occurrences in the specified document.
     *
     * @param documentId the id of the document to obtain occurrences of this term
     * @return a set of occurrences of this term in the specified document
     */
    public Set<Occurrence> getOccurencesInDocument(long documentId) {
        return new HashSet<>(occurrences.get(documentId));
    }


    /**
     * Returns the term frequency (tf) of this term t in the document d, which is
     * the number of occurrences of t in d.
     */
    public int getTermFrequency(long documentId) {
        java.util.Collection<Occurrence> occurrencesInDocument = occurrences.get(documentId);
        if (occurrencesInDocument == null) {
            return 0;

        } else {
            return occurrencesInDocument.size();
        }
    }


    /**
     * Returns the document frequency (df) of this term t, which is the number
     * of unique documents that contain t (excluding multiple occurrences in each
     * document).
     */
    public int getDocumentFrequency() {
        return getOccurringDocuments().size();
    }


    /**
     * Returns the inverse document frequency (idf) of this term t.
     * Since the term object does not store a parent Collection, the value
     * of N (total number of documents) must be specified.
     */
    public double getInverseDocumentFrequency(double N) {
        double division = N / (double) getDocumentFrequency();
        return Math.log10(division);
    }


    /**
     * Returns this term weight for the specified document, based on the
     * log tf weighting scheme.
     */
    public double getLogFrequencyWeight(long documentId) {
        double tf = getTermFrequency(documentId);

        if (tf > 0) {
            return 1.0 + Math.log10(tf);

        } else {
            return 0;
        }
    }


    /**
     * Returns this term weight for the specified document, based on the
     * tf-idf weighting scheme.
     * <p/>
     * The tf-idf weight of a term is the product of its tf weight and its
     * idf weight.
     */
    public double getTfIdfWeight(long documentId, int N) {
        return getLogFrequencyWeight(documentId) * getInverseDocumentFrequency(N);
    }


    /**
     * Returns the collection frequency of this term t, which is the number
     * of occurrences of t in the collection (including multiple occurrences).
     */
    public int getCollectionFrequency() {
        return occurrences.size();
    }


    /**
     * Returns a list of document IDs where this term occurs.
     */
    public Set<Long> getOccurringDocuments() {
        return occurrences.keySet();
    }


    /**
     * Writes this term index / occurrence list into a string line on the
     * specified writer. This is the inverse process of the 'parseLine(String line)'
     * method above.
     */
    public void writeLine(Writer writer) throws IOException {
        writer.append(text);
        writer.append(':');
        for (Iterator<Long> it = occurrences.keySet().iterator(); it.hasNext(); ) {
            Long docId = it.next();
            writer.append(docId.toString());
            writer.append('=');
            writer.append(Double.toString(normalizedWeights.get(docId)));
            writer.append('=');

            for (Iterator<Occurrence> it2 = occurrences.get(docId).iterator(); it2.hasNext(); ) {
                Occurrence occ = it2.next();
                writer.append(Integer.toString(occ.getPhrasePosition()));
                writer.append('-');
                writer.append(Integer.toString(occ.getStartPosition()));
                writer.append('-');
                writer.append(Integer.toString(occ.getEndPosition()));

                if (it2.hasNext()) {
                    writer.append('|');
                }
            }
            if (it.hasNext()) {
                writer.append(' ');
            }
        }
        writer.append("\n");
    }


    /**
     * Uses the specified document contents to retrieve a string with every snippet
     * of occurrence of this term in the specified document id. The number specified
     * limit the number of snippets that are returned.
     */
    public String getSummaryForDocument(Document d, int numSnippets) {
        long documentId = d.getId();
        MutableString contents = d.getContentSnapshot();

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Iterator<Occurrence> it = occurrences.get(documentId).iterator(); it.hasNext(); ) {
            Occurrence o = it.next();
            int snippetStart = o.getStartPosition() - SNIPPET_OFFSET;
            if (snippetStart < 0) {
                snippetStart = 0;
            }
            int snippetEnd = o.getEndPosition() + 1 + SNIPPET_OFFSET;
            if (snippetEnd > contents.length()) {
                snippetEnd = contents.length();
            }

            if (snippetStart != 0 && count == 0) {
                sb.append("... ");
            }
            sb.append(contents.substring(snippetStart, o.getStartPosition()));
            sb.append("<b>");
            sb.append(contents.substring(o.getStartPosition(), o.getEndPosition() + 1));
            sb.append("</b>");
            sb.append(contents.substring(o.getEndPosition() + 1, snippetEnd));

            count++;
            if (count == numSnippets) {
                if (snippetEnd != contents.length()) {
                    sb.append(" ...");
                }
                break;
            }

            if (it.hasNext()) {
                sb.append(" ... ");
            }
        }

        return sb.toString();
    }


    /**
     * Tests the equality of this term with the specified object.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term term = (Term) o;
        return text.equals(term.text);
    }


    /**
     * Returns the hashcode of this term.
     */
    @Override
    public int hashCode() {
        return text.hashCode();
    }


    /**
     * Returns the text that represents this term.
     */
    @Override
    public String toString() {
        return text.toString();
    }

}

