/*
 * Copyright 2015 Eduardo Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.vokter.document;

import com.edduarte.vokter.model.mongodb.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.cache2k.Cache;
import org.cache2k.CacheBuilder;
import org.cache2k.PropagatedCacheException;
import org.cache2k.impl.CacheLockSpinsExceededError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * A DocumentCollection represents the widest information unit, and has direct
 * access to every collected document and term.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public final class DocumentCollection {

    private static final Logger logger = LoggerFactory.getLogger(DocumentCollection.class);

    private final String collectionName;

    private final DB documentsDB;


    /**
     * Local and cached map of document IDs (integers) to document objects.
     */
    private final Cache<Params, DocumentPair> documentsCache;


    /**
     * Instantiate the Collection object, which represents the core access to the
     * above mentioned persistence and cache mechanisms.
     */
    public DocumentCollection(String collectionName, DB documentsDB) {
        this.collectionName = collectionName;
        this.documentsDB = documentsDB;
        this.documentsCache = CacheBuilder
                .newCache(Params.class, DocumentPair.class)
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
        DBCollection collection = documentsDB.getCollection(collectionName);

        DBCursor dbObjects = collection.find(
                new BasicDBObject(Document.URL, d.getUrl())
                        .append(Document.CONTENT_TYPE, d.getContentType())
        ).sort(new BasicDBObject(Document.DATE, 1));

        if (dbObjects.hasNext()) {
            // there is already a document with the specified url and content
            // type, so remove the oldest one recorded and add this one
            DBObject oldest = null;
            try {
                oldest = dbObjects.next();
            } catch (NoSuchElementException ignored) {
            }
            DBObject latest = null;
            try {
                latest = dbObjects.next();
            } catch (NoSuchElementException ignored) {
            }
            if (oldest != null && latest != null) {
                Document oldestDoc = new Document((BasicDBObject) oldest);
                collection.remove(oldestDoc);
                collection.insert(d);
            }
        } else {
            // the specified combination of url and content type is new in the
            // collection, so add the same snapshot twice to avoid null pointer
            // exceptions when requesting a document pair later
            collection.insert(d);
            collection.insert(d.clone());
        }
    }


    /**
     * Removes the specified document from the local database.
     */
    public void remove(String url, String contentUrl) {
        DocumentPair pair = get(url, contentUrl);
        if (pair != null) {
            Document d1 = pair.oldest();
            Document d2 = pair.latest();
            DBCollection collection = documentsDB.getCollection(collectionName);
            collection.remove(d1);
            collection.remove(d2);
        }
        documentsCache.remove(new Params(url, contentUrl));
    }


    /**
     * Converts the specified document url and content type into a pair of
     * document object, the url and the contentType stored
     */
    public DocumentPair get(String url, String contentUrl) {
        try {
            Params query = new Params(url, contentUrl);
            // the 'get' method will look for any documents in the local files or
            // temporary cache whose url is equal to the specified url and whose
            // content type is equal to the specified content type
            DocumentPair pair = documentsCache.get(query);
            if (pair != null) {
                return pair;
            } else {
                // the 'null' value was unexpectedly added to the cache, so
                // remove it to avoid further problems and return null
                documentsCache.remove(query);
                return null;
            }
        } catch (PropagatedCacheException | CacheLockSpinsExceededError ex) {
            // if this exception occurs, then no occurrences of the specified
            // document were found in this collection, so just return null
            return null;
        }
    }


    private DocumentPair getInternal(Params query) {
        DBCollection collection = documentsDB.getCollection(collectionName);

        DBCursor dbObjects = collection.find(
                new BasicDBObject(Document.URL, query.url)
                        .append(Document.CONTENT_TYPE, query.contentType)
        ).sort(new BasicDBObject(Document.DATE, 1));

        DBObject oldest = null;
        try {
            oldest = dbObjects.next();
        } catch (NoSuchElementException ignored) {
        }
        DBObject latest = null;
        try {
            latest = dbObjects.next();
        } catch (NoSuchElementException ignored) {
        }

        if (oldest == null && latest == null) {
            return null;
        }
        Document oldestDoc = oldest != null ?
                new Document((BasicDBObject) oldest) : null;
        Document latestDoc = latest != null ?
                new Document((BasicDBObject) latest) : null;
        return DocumentPair.of(oldestDoc, latestDoc);
    }


    /**
     * Immediately commands this index to clear the documents stored in memory
     * cache. Every retrieval of documents performed after this will require
     * reading the database again.
     */
    public void clearCache() {
        documentsCache.clear();
    }


    public void destroy() {
        DBCollection collection = documentsDB.getCollection(collectionName);
        collection.drop();
        documentsCache.destroy();
        documentsDB.dropDatabase();
    }


    private static class Params {

        private final String url;

        private final String contentType;


        private Params(String url, String contentType) {
            this.url = url;
            if (contentType == null) {
                this.contentType = "";
            } else {
                this.contentType = contentType;
            }
        }


        public static Params of(String url, String contentType) {
            return new Params(url, contentType);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Params that = (Params) o;
            return this.url.equals(that.url) &&
                    this.contentType.equals(that.contentType);

        }


        @Override
        public int hashCode() {
            int result = url.hashCode();
            result = 31 * result + contentType.hashCode();
            return result;
        }
    }

}
