package argus.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Process of the external executable.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class ProcessConnector {

    private static Logger logger = LoggerFactory.getLogger(ProcessConnector.class);

    private InputStream is;
    private OutputStream os;
    private OutputStream es;
    private Process process;
    private Thread tis;
    private Thread tos;
    private Thread tes;

    /**
     * Constructor.
     *
     * @param is Input data.
     * @param os Output info data.
     * @param es Output error data.
     */
    public ProcessConnector(InputStream is, OutputStream os, OutputStream es) {
        this.is = is;
        this.os = os;
        this.es = es;
    }

    public void create(File dir, String... command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        pb.directory(dir);
        process = pb.start();

        tis = new Thread(new StreamGobbler(is, process.getOutputStream()));
        tis.start();
        tos = new Thread(new StreamGobbler(process.getInputStream(), os));
        tos.start();
        tes = new Thread(new StreamGobbler(process.getErrorStream(), es));
        tes.start();



//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread thread, Throwable thrwbl) {
//                tis.stop();
//                tos.stop();
//                tes.stop();
//
//                logger.error(thrwbl.getMessage(), thrwbl.getCause());
//            }
//        });
    }

    /**
     * Create the process to execute the external program.
     *
     * @param command The command line to be executed.
     * @throws IOException Problem executing the command line.
     */
    public void create(String... command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        process = pb.start();

        tis = new Thread(new StreamGobbler(is, process.getOutputStream()));
        tis.start();
        tos = new Thread(new StreamGobbler(process.getInputStream(), os));
        tos.start();
        tes = new Thread(new StreamGobbler(process.getErrorStream(), es));
        tes.start();



//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread thread, Throwable thrwbl) {
//                tis.stop();
//                tos.stop();
//                tes.stop();
//
//                logger.error(thrwbl.getMessage(), thrwbl.getCause());
//            }
//        });
    }

    public void destroy() {
        process.destroy();
        tis.interrupt();
        tos.interrupt();
        tes.interrupt();
    }
}

