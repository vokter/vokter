package argus.document;

import argus.term.Term;
import argus.util.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Simple structure that holds a document current snapshot and associates
 * it with an url.
 * <p>
 * The id is obtained by using a synchronized counter, which in turn will ensure
 * that different Document objects being created in concurrency will always have
 * different IDs.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class Document extends BasicDBObject implements Serializable {
    public static final String ID = "id";
    public static final String URL = "url";
    public static final String ORIGINAL_CONTENT = "original_content";
    private static final long serialVersionUID = 1L;
    private static final int BOUND_INDEX = 4;

    private transient final DBCollection termCollection;

    Document(DB termDatabase, String url, String originalContent) {
        super(ID, Constants.bytesToHex(Constants.generateRandomBytes()));
        append(URL, url);
        append(ORIGINAL_CONTENT, originalContent);
        termCollection = termDatabase.getCollection(getTermCollectionName());
    }


    Document(DB termDatabase, BasicDBObject dbObject) {
        super(dbObject);
        termCollection = termDatabase.getCollection(getTermCollectionName());
    }

    public void addOccurrence(Term term) {
        termCollection.insert(term);
    }

    public void addOccurrences(Iterable<Term> terms) {
        addOccurrences(StreamSupport.stream(terms.spliterator(), false));
    }

    public void addOccurrences(Stream<Term> termStream) {
        BulkWriteOperation builder = termCollection.initializeUnorderedBulkOperation();
        termStream.forEach(builder::insert);
        builder.execute();
        builder = null;
    }

    public Term getOccurrence(String text, int wordCount) {
        if (text.isEmpty()) {
            return null;
        }
        int lowerBound = wordCount - BOUND_INDEX;
        int upperBound = wordCount + BOUND_INDEX;

        BasicDBObject boundQuery =
                new BasicDBObject("$gt", lowerBound).append("$lt", upperBound);
        BasicDBObject queriedObject = (BasicDBObject) termCollection.findOne(
                new BasicDBObject(Term.TEXT, text).append(Term.WORD_COUNT, boundQuery));
        return queriedObject != null ? new Term(queriedObject) : null;
    }

    public List<Term> getAllOccurrences(String termText) {
        if (termText.isEmpty()) {
            return null;
        }
        DBCursor cursor = termCollection.find(new BasicDBObject(Term.TEXT, termText));
        List<Term> list = new ArrayList<>();
        while (cursor.hasNext()) {
            BasicDBObject obj = (BasicDBObject) cursor.next();
            list.add(new Term(obj));
        }
        cursor.close();
        return list;
    }

//
//
//    @SuppressWarnings("unchecked")
//    public Stream<SnippetOccurrence> getTermOccurrences(final DB termDatabase,
//                                                        final String termText) {
//        if (!termText.isEmpty()) {
//            DBCollection termCollection = termDatabase.getCollection(getId());
//            BasicDBObject queriedObject = (BasicDBObject) termCollection.findOne(
//                    new BasicDBObject(Term.TEXT, termText));
//            if (queriedObject != null) {
//                // the term occurs in the document
//                List<BasicDBObject> occurrences =
//                        (ArrayList<BasicDBObject>) queriedObject.get(Term.OCCURRENCES);
//                Stream<SnippetOccurrence> stream = occurrences.parallelStream()
//                        .map(o -> new SnippetOccurrence(originalContent, new Occurrence(o)));
//                occurrences = null;
//
//                // returns an occurrence for this term for further comparison
//                return stream;
//            }
//        }
//        // the term does not occur in the document
//        return null;
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public Optional<SnippetOccurrence> getTermOccurrence(
//            final DB termDatabase, final String termText,
//            final int lowerBound, final int upperBound) {
//        if (!termText.isEmpty()) {
//            DBCollection termCollection = termDatabase.getCollection(getId());
//            BasicDBObject boundQuery =
//                    new BasicDBObject("$gt", lowerBound).append("$lt", upperBound);
//            BasicDBObject queriedObject = (BasicDBObject) termCollection.findOne(
//                    new BasicDBObject(Term.TEXT, termText).append(Term.OCCURRENCES,
//                            new BasicDBObject(Occurrence.WORD_COUNT, boundQuery)));
//            if (queriedObject != null) {
//                // the term occurs within the specified bounds
//                List occurrences = (ArrayList) queriedObject.get(Term.OCCURRENCES);
//                Stream<SnippetOccurrence> stream = occurrences.parallelStream()
//                        .map(o -> new SnippetOccurrence(originalContent, (Occurrence) o));
//                stream.filter(o ->
//                                o.getWordCount() > lowerBound && o.getWordCount() < upperBound
//                );
//                Optional<SnippetOccurrence> optional = stream.findFirst();
//                occurrences = null;
//
//                // returns an occurrence for this term for further comparison
//                return optional;
//            }
//        }
//        // the term does not occur within the specified bounds
//        return Optional.empty();
//    }
//
//
//    /**
//     * Uses the specified document contents to retrieve a string with every snippet
//     * of occurrence of this term in the specified document id. The number specified
//     * limit the number of snippets that are returned.
//     */
//    public TermSnippet getSnippetForTerm(String termText) {
//        Term term = getTerm(termText);
//        ArrayList occurrencesArray = (ArrayList) term.get(Term.OCCURRENCES);
//
//        List<TermSnippet> list = new ArrayList<>();
//        for (Object o : occurrencesArray) {
//            Occurrence occurrence = (Occurrence) o;
//            list.add(new TermSnippet(content, occurrence));
//            occurrence = null;
//        }
//        term = null;
//
//        return list;
//    }


    private String getTermCollectionName() {
        return getUrl().hashCode() + getString(ID);
    }


    public String getUrl() {
        return getString(URL);
    }


    public String getOriginalContent() {
        return getString(ORIGINAL_CONTENT);
    }

    /**
     * Converts a cluster of terms associated with a document into a String, where
     * each term is separated by a whitespace.
     */
    @SuppressWarnings("unchecked")
    public String getProcessedContent() {
        DBCursor cursor = termCollection.find();
        return StreamSupport.stream(cursor.spliterator(), false)
                .map(Term::new)
                .map(Term::toString)
                .collect(Collectors.joining(" "));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Document that = (Document) o;
        return this.getUrl().equalsIgnoreCase(that.getUrl());
    }


    @Override
    public int hashCode() {
        return getUrl().hashCode();
    }


    @Override
    public String toString() {
        return getUrl();
    }


    void destroy() {
        termCollection.drop();
    }
}

