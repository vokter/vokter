package argus.query;

import argus.index.Collection;

import java.util.function.Consumer;

/**
 * TODO
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
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
