package argus.document;

import argus.term.Occurrence;
import argus.term.SnippetOccurrence;
import argus.term.Term;
import argus.util.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import it.unimi.dsi.lang.MutableString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


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
public final class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;

    private final String url;

    private final MutableString content;


    Document(String url, MutableString content) {
        this.id = Constants.bytesToHex(Constants.generateRandomBytes());
        this.url = url;
        this.content = content;
    }


    public void addTerm(DB termDatabase, Term term) {
        DBCollection termCollection = termDatabase.getCollection(getTermCollectionName());
        termCollection.insert(term);
    }


    public void addMultipleTerms(DB termDatabase, Iterable<Term> termToSave) {
        DBCollection termCollection = termDatabase.getCollection(getTermCollectionName());
        BulkWriteOperation builder = termCollection.initializeUnorderedBulkOperation();
        termToSave.forEach(builder::insert);
        builder.execute();
    }


//    public Term getTerm(DB termDatabase, String termText) {
//        if (termText.isEmpty()) {
//            return null;
//        }
//        DBCollection termCollection = termDatabase.getCollection(getTermCollectionName());
//        BasicDBObject queriedObject = (BasicDBObject) termCollection
//                .findOne(new BasicDBObject(Term.TEXT, termText));
//        return queriedObject != null ? new Term(queriedObject) : null;
//    }


    @SuppressWarnings("unchecked")
    public List<SnippetOccurrence> getTermOccurences(final DB termDatabase,
                                                     final String termText,
                                                     final int lowerBound,
                                                     final int upperBound) {
        if (!termText.isEmpty()) {
            DBCollection termCollection = termDatabase.getCollection(getTermCollectionName());
            BasicDBObject boundQuery =
                    new BasicDBObject("$gt", lowerBound).append("$lt", upperBound);
            BasicDBObject queriedObject = (BasicDBObject) termCollection.findOne(
                    new BasicDBObject(Term.TEXT, termText).append(Term.OCCURRENCES,
                            new BasicDBObject(Occurrence.WORD_COUNT, boundQuery)));
            if (queriedObject != null) {
                // the term occurs within the specified bounds
                List occurrences = (ArrayList) queriedObject.get(Term.OCCURRENCES);
                Stream<SnippetOccurrence> stream = occurrences
                        .parallelStream()
                        .map(o -> new SnippetOccurrence(content, (Occurrence) o));
                // returns the list of occurrences for this term for further comparison
                return stream.collect(toList());
            }
        }
        // the term does not occur within the specified bounds
        return new ArrayList<>();
    }


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


    public String getUrl() {
        return url;
    }


    public String getTermCollectionName() {
        return id;
    }


    public MutableString getContent() {
        return content;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Document document = (Document) o;
        return url.equalsIgnoreCase(document.url);
    }


    @Override
    public int hashCode() {
        return url.hashCode();
    }


    @Override
    public String toString() {
        return url;
    }


    void destroy(DB termDatabase) {
        content.delete(0, content.length());
        DBCollection termCollection = termDatabase.getCollection(getTermCollectionName());
        termCollection.drop();
    }
}

