package com.edduarte.vokter.persistence.mongodb;

import com.edduarte.vokter.diff.DiffDetector;
import com.edduarte.vokter.persistence.Diff;
import com.edduarte.vokter.persistence.DiffCollection;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class MongoDiffCollection implements DiffCollection {

    private final DB db;


    /**
     * Instantiate the Collection object, which represents the core access to
     * diffs using MongoDB persistence mechanisms.
     */
    public MongoDiffCollection(DB db) {
        this.db = db;
    }


    private static String getDiffCollectionName(String url, String contentType) {
        return
//                "diffs|" +
                url + "|" + contentType;
    }


    @Override
    public void addDifferences(String documentUrl, String documentContentType,
                               List<DiffDetector.Result> diffs) {
        DBCollection diffColl = db.getCollection(
                getDiffCollectionName(documentUrl, documentContentType));
        BulkWriteOperation bulkOp = diffColl.initializeUnorderedBulkOperation();
        diffs.parallelStream()
                .map(MongoDiff::new)
                .forEach(bulkOp::insert);
        bulkOp.execute();
        bulkOp = null;
        diffColl = null;
    }


    @Override
    public List<Diff> getDifferences(String documentUrl, String documentContentType) {
        // check diffs stored on the database
        DBCollection diffColl = db.getCollection(
                getDiffCollectionName(documentUrl, documentContentType));
        long count = diffColl.count();
        if (count <= 0) {
            return Collections.emptyList();
        }

        Iterable<DBObject> cursor = diffColl.find();
        return StreamSupport.stream(cursor.spliterator(), true)
                .map(MongoDiff::new)
                .collect(Collectors.toList());
    }


    @Override
    public void removeDifferences(String documentUrl, String documentContentType) {
        DBCollection diffColl = db.getCollection(
                getDiffCollectionName(documentUrl, documentContentType));
        diffColl.drop();
    }
}
