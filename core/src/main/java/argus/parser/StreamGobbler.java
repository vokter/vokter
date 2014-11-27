package argus.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Map the input and output data to run external executables.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class StreamGobbler implements Runnable {

    /**
     * {@link Logger} to be used in the class.
     */
    private static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);
    /**
     * Input data.
     */
    private InputStream is;
    /**
     * Output data.
     */
    private OutputStream os;

    /**
     * Constructor.
     *
     * @param is Input data
     * @param os Output data
     */
    public StreamGobbler(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    /**
     * Map the input and output data.
     */
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

