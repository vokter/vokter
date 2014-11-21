package argus.query;

import argus.index.Collection;
import argus.index.Document;
import argus.index.Occurrence;
import argus.index.Term;
import argus.query.vectormodel.DocumentVector;
import argus.query.vectormodel.MergedAxe;
import argus.query.vectormodel.MergedAxeGroup;
import argus.query.vectormodel.QueryVector;
import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedHashMultimap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static argus.util.Util.difference;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.toList;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class Search implements Runnable {

    private final Collection collection;
    private final Query query;
    private final Consumer<QueryResult> resultConsumer;


    public Search(final Collection collection,
                   final Query query,
                   Consumer<QueryResult> resultConsumer) {
        this.collection = collection;
        this.query = query;
        this.resultConsumer = resultConsumer;
    }


    @Override
    public void run() {

    }
}
