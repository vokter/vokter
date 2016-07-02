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

package com.edduarte.vokter.persistence.cassandra;

import com.edduarte.vokter.document.DocumentBuilder;
import com.edduarte.vokter.persistence.Document;
import com.edduarte.vokter.persistence.DocumentCollection;
import com.edduarte.vokter.util.Params;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.optimaize.langdetect.LanguageDetector;
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
public final class CassandraDocumentCollection implements DocumentCollection {

    private static final Logger logger = LoggerFactory.getLogger(CassandraDocumentCollection.class);

    private final DBCollection db;


    /**
     * Local and cached map of document IDs (integers) to document objects.
     */
    private final Cache<Params, Pair> documentsCache;


    /**
     * Instantiate the Collection object, which represents the core access to
     * documents using MongoDB persistence mechanisms.
     */
    public CassandraDocumentCollection(DB db) {
        this.db = db.getCollection("documents");
        this.documentsCache = CacheBuilder
                .newCache(Params.class, Pair.class)
                .name("documents")
                .expiryDuration(20, TimeUnit.SECONDS)
                .maxSize(100)
                .source(this::getInternal)
                .build();
    }


    @Override
    public Document addNewDocument(String documentUrl, String documentContentType,
                                   LanguageDetector langDetector,
                                   boolean filterStopwords, boolean ignoreCase) {
        DocumentBuilder builder = DocumentBuilder
                .fromUrl(documentUrl, documentContentType);
        if (filterStopwords) {
            builder.filterStopwords();
        }
        if (ignoreCase) {
            builder.ignoreCase();
        }

        Document d = builder.build(langDetector, CassandraDocument.class);

        // the specified combination of url and content type is new in the
        // collection, so add the same snapshot twice to avoid null pointer
        // exceptions when requesting a document pair later
        db.insert((CassandraDocument) d);
        db.insert((CassandraDocument) d.clone());

        return d;
    }


    @Override
    public Document addNewSnapshot(Document oldDocument,
                                   LanguageDetector langDetector,
                                   boolean filterStopwords, boolean ignoreCase) {
        DocumentBuilder builder = DocumentBuilder
                .fromUrl(oldDocument.getUrl(), oldDocument.getContentType())
                .withShingleLength(oldDocument.getShingleLength());
        if (filterStopwords) {
            builder.filterStopwords();
        }
        if (ignoreCase) {
            builder.ignoreCase();
        }

        Document d = builder.build(langDetector, CassandraDocument.class);
        if (d != null) {
            // there is already a document with the specified url and content
            // type, so remove the oldest one recorded and add this one
            DBCursor dbObjects = db.find(
                    new BasicDBObject(CassandraDocument.URL, d.getUrl())
                            .append(CassandraDocument.CONTENT_TYPE, d.getContentType())
            ).sort(new BasicDBObject(CassandraDocument.DATE, 1));

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
                CassandraDocument oldestDoc = new CassandraDocument((BasicDBObject) oldest);
                db.remove(oldestDoc);
                db.insert((CassandraDocument) d);
                return d;
            }
        }
        return null;
    }


    /**
     * Removes the specified document from the local database.
     */
    @Override
    public void remove(String url, String contentType) {
        Pair pair = get(url, contentType);
        if (pair != null) {
            CassandraDocument d1 = (CassandraDocument) pair.oldest();
            CassandraDocument d2 = (CassandraDocument) pair.latest();
            db.remove(d1);
            db.remove(d2);
        }
        documentsCache.remove(Params.of(url, contentType));
    }


    /**
     * Converts the specified document url and content type into a pair of
     * document object, the url and the contentType stored
     */
    @Override
    public Pair get(String url, String contentType) {
        try {
            Params query = Params.of(url, contentType);
            // the 'get' method will look for any documents in the local files or
            // temporary cache whose url is equal to the specified url and whose
            // content type is equal to the specified content type
            Pair pair = documentsCache.get(query);
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


    private Pair getInternal(Params query) {
        DBCursor dbObjects = db.find(
                new BasicDBObject(CassandraDocument.URL, query.getUrl())
                        .append(CassandraDocument.CONTENT_TYPE, query.getContentType())
        ).sort(new BasicDBObject(CassandraDocument.DATE, 1));

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
        CassandraDocument oldestDoc = oldest != null ?
                new CassandraDocument((BasicDBObject) oldest) : null;
        CassandraDocument latestDoc = latest != null ?
                new CassandraDocument((BasicDBObject) latest) : null;
        return Pair.of(oldestDoc, latestDoc);
    }


    @Override
    public void destroy() {
        documentsCache.destroy();
    }

}
