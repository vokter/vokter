package argus.document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.cache2k.Cache;
import org.cache2k.CacheBuilder;
import org.cache2k.PropagatedCacheException;
import org.cache2k.impl.CacheLockSpinsExceededError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * A DocumentCollection represents the widest information unit, and has direct
 * access to every collected document and term.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class DocumentCollection {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCollection.class);

    private final String collectionName;
    private final DB documentsDB;
    private final DB occurrencesDB;


    /**
     * Local and cached map of document IDs (integers) to document objects.
     */
    private final Cache<String, Document> documentsCache;


    /**
     * Instantiate the Collection object, which represents the core access to the
     * above mentioned persistence and cache mechanisms.
     */
    public DocumentCollection(String collectionName, DB documentsDB, DB occurrencesDB) {
        this.collectionName = collectionName;
        this.documentsDB = documentsDB;
        this.occurrencesDB = occurrencesDB;
        this.documentsCache = CacheBuilder
                .newCache(String.class, Document.class)
                .name(collectionName)
                .expiryDuration(20, TimeUnit.SECONDS)
                .maxSize(100)
                .source(this::getInternal)
                .build();
    }


    /**
     * Adds the specified document to the local database.
     */
    public void add(Document d) {
        if (d == null) {
            return;
        }
        System.out.println("adding " + d.getString(Document.ID));
        DBCollection collection = documentsDB.getCollection(collectionName);
        collection.insert(d);
    }


    /**
     * Removes the specified document from the local database.
     */
    public void remove(String url) {
        Document d = get(url);
        if (d != null) {
            System.out.println("removing " + d.getString(Document.ID));
            d.destroy();
            DBCollection collection = documentsDB.getCollection(collectionName);
            collection.remove(d);
        }
        documentsCache.remove(url);
    }


    /**
     * Converts the specified document url into a document object, by reading it
     * from its local file / cache.
     */
    public Document get(String documentUrl) {
        try {
            // the 'get' method will look for any document in the local files or
            // temporary cache whose url is equal to the specified url
            Document document = documentsCache.get(documentUrl);
            if (document != null) {
                return document;
            } else {
                // the 'null' value was added to the cache, so remove it
                documentsCache.remove(documentUrl);
                return null;
            }
        } catch (PropagatedCacheException | CacheLockSpinsExceededError ex) {
//            // if this exception occurs, then no occurrences of the specified
//            // document were found in this collection
            return null;
        }
    }


    private Document getInternal(String documentUrl) {
        DBCollection collection = documentsDB.getCollection(collectionName);
        BasicDBObject mongoDocument = (BasicDBObject) collection
                .findOne(new BasicDBObject(Document.URL, documentUrl));
        return mongoDocument != null ? new Document(occurrencesDB, mongoDocument) : null;
    }


    /**
     * Immediately commands this index to clear the documents stored in memory cache.
     * Every retrieval of documents performed after this will require reading the
     * local document file again.
     */
    public void clearCache() {
        documentsCache.clear();
    }


    public void destroy() {
        DBCollection collection = documentsDB.getCollection(collectionName);
        collection.drop();
        documentsCache.destroy();
        documentsDB.dropDatabase();
        occurrencesDB.dropDatabase();
    }
}
