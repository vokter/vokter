package argus.document;

import it.unimi.dsi.lang.MutableString;

import java.io.Serializable;


/**
 * Simple structure that holds a document current snapshot and associates
 * it with an url.
 * <p/>
 * The id is obtained by using a synchronized counter, which in turn will ensure
 * that different Document objects being created in concurrency will always have
 * different IDs.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class Document implements Serializable {
    private static final long serialVersionUID = 1L;

//    private static final transient DB termDatabase;
//    static {
//        MongoClient mongoClient = Context.getInstance().getMongoClient();
//        termDatabase = mongoClient.getDB("index_db");
//    }


    private final String url;

    private final MutableString originalContent;

//    private final transient DBCollection termCollection;


    public Document(String url, MutableString originalContent) {
        this.url = url;
        this.originalContent = originalContent;
//        this.termCollection = termDatabase.getCollection(url);
    }


//    public Term getTerm(String termText) throws Throwable {
//        if (termText.isEmpty()) {
//            return null;
//        }
//
//        BasicDBObject queriedObject = (BasicDBObject) termCollection
//                .findOne(new BasicDBObject(Term.TEXT, termText));
//        return queriedObject != null ? new Term(queriedObject) : null;
//    }
//
//
//    public boolean termOccursWithin(String termText, int lowerSlopBound, int upperSlopBound) throws Throwable {
//        if (termText.isEmpty()) {
//            return false;
//        }
//
//        BasicDBObject queriedObject = (BasicDBObject) termCollection
//                .findOne(new BasicDBObject(Term.TEXT, termText)
//                        .append(Term.OCCURRENCES, new BasicDBObject(
//                                Occurrence.WORD_COUNT,
//                                new BasicDBObject("$gt", lowerSlopBound)
//                                        .append("$lt", upperSlopBound))));
//        return queriedObject != null;
//    }
//
//
//    public void addTerm(Term termToSave) throws Throwable {
//        termCollection.insert(termToSave);
//    }
//
//
//    public void addTermBulk(Set<Term> termToSave) throws Throwable {
//        BulkWriteOperation builder = termCollection.initializeUnorderedBulkOperation();
//        termToSave.forEach(builder::insert);
//        builder.execute();
//    }


    public String getUrl() {
        return url;
    }


    public MutableString getContent() {
        return originalContent;
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


    public void destroy() {
        originalContent.delete(0, originalContent.length());
//        termCollection.drop();
    }
}

