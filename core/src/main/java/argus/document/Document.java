package argus.document;

import argus.term.Term;
import argus.util.Constants;
import com.google.common.collect.Iterables;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private static final long serialVersionUID = 1L;

    public static final String ID = "id";
    public static final String URL = "url";
    public static final String ORIGINAL_CONTENT = "original_content";

    Document(String url, String originalContent) {
        super(ID, Constants.bytesToHex(Constants.generateRandomBytes()));
        append(URL, url);
        append(ORIGINAL_CONTENT, originalContent);
    }


    Document(BasicDBObject dbObject) {
        super(dbObject);
    }


    public Term getTerm(DB termDatabase, String termText) {
        if (termText.isEmpty()) {
            return null;
        }
        DBCollection termCollection = termDatabase.getCollection(getTermCollectionName());
        BasicDBObject queriedObject = (BasicDBObject) termCollection
                .findOne(new BasicDBObject(Term.TEXT, termText));
        return queriedObject != null ? new Term(queriedObject) : null;
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


    public String getTermCollectionName() {
        return getUrl().hashCode() + getString(ID);
    }


    public String getUrl() {
        return getString(URL);
    }


    public String getOriginalContent() {
        return getString(ORIGINAL_CONTENT);
    }


    @SuppressWarnings("unchecked")
    public String getProcessedContent(DB termDatabase) {
        DBCollection termCollection = termDatabase.getCollection(getTermCollectionName());

        DBCursor cursor = termCollection.find();
        return StreamSupport.stream(cursor.spliterator(), true)
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


    void destroy(DB termDatabase) {
        DBCollection termCollection = termDatabase.getCollection(getTermCollectionName());
        termCollection.drop();
    }
}

