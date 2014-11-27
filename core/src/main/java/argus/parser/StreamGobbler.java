package argus.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An input and output data exchanger between the application and an external
 * executable.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class StreamGobbler implements Runnable {

    private final InputStream is;
    private final OutputStream os;


    public StreamGobbler(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    /**
     * Maps the input and output data.
     */
    @Override
    public void run() {
        try {
            byte[] buffer = new byte[1 << 12];
            int c;
            while ((c = is.read(buffer)) != -1) {
                os.write(buffer, 0, c);
                os.flush();
            }
        } catch (IOException ex) {
            //logger.error("There was a problem writing the output.", ex);
            return;
        }
    }
}

