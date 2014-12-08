package argus.diff;

import argus.document.Document;
import argus.query.Query;
import argus.query.QueryBuilder;
import argus.term.Term;
import com.mongodb.DB;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DifferenceDetector implements Callable<List<Discrepancy>> {
    private static final int BOUND_OFFSET = 5;

    private final DB termDatabase;
    private final Document oldSnapshot;
    private final Document newSnapshot;
    private final Query query;

    public DifferenceDetector(final DB termDatabase,
                              final Document oldSnapshot,
                              final Document newSnapshot,
                              final Query query) {
        this.termDatabase = termDatabase;
        this.oldSnapshot = oldSnapshot;
        this.newSnapshot = newSnapshot;
        this.query = query;
    }

    @Override
    public List<Discrepancy> call() {
        DiffMatchPatch dmp = new DiffMatchPatch();

        String original = oldSnapshot.getProcessedContent(termDatabase);
        String revision = newSnapshot.getProcessedContent(termDatabase);

        LinkedList<DiffMatchPatch.Diff> diffs = dmp.diff_main(original, revision);
        dmp.diff_cleanupEfficiency(diffs);

        System.out.println(diffs.toString());
//        diffs.forEach(diff -> {
//            switch (diff.operation) {
//                case INSERT:
//                    Term newTerm = newSnapshot.getTerm(termDatabase, diff.text);
//
//                    break;
//            }
//        });

        return null;
    }
}
