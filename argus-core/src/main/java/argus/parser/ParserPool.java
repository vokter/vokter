package argus.parser;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * A parser-pool that contains a set number of parsers. When the last parser
 * from the pool is removed, future parsing workers will be locked until
 * a used parser is placed back in the pool.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class ParserPool {

    private final LinkedBlockingQueue<Parser> parsersQueue;

    public ParserPool() {
        this.parsersQueue = new LinkedBlockingQueue<>();
    }

    public Parser take() throws InterruptedException {
        return parsersQueue.take();
    }

    public void place(Parser parser) throws InterruptedException {
        this.parsersQueue.put(parser);
    }

    public void clear() {
        while (!parsersQueue.isEmpty()) {
            try {
                Parser parser = parsersQueue.take();
                parser.close();
                parser = null;
            } catch (InterruptedException ex) {
                throw new RuntimeException("There was a problem terminating the parsers.", ex);
            }
        }
        this.parsersQueue.clear();
    }
}
