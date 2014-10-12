package pt.ua.adis.project.web.server;

import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Server
        extends Tomcat
        implements LifecycleListener {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private static Server instance;

    private boolean initialized;
    private int numThreads;
    private ExecutorService executor;

    private Server() {
        super();
        initialized = false;
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public void initialize(int port, int numThreads) {
        if (initialized) {
            return;
        }

        this.numThreads = numThreads;
        this.executor = Executors.newFixedThreadPool(numThreads);

        setPort(port);

        try {
            String webappDirLocation = "src/main/resources/webapp/";
            org.apache.catalina.Context c = addWebapp("/", new File(webappDirLocation).getAbsolutePath());
            setBaseDir(".");


        } catch (ServletException e) {
            logger.error(e.getMessage(), e);
            return;
        }

//        addLifeCycleListener(this);
        this.initialized = true;
    }

    public ExecutorService getExecutor() {
        if (!initialized) {
            throw new RuntimeException("Server was not initialized.");
        }
        return executor;
    }

    @Override
    public void lifecycleEvent(LifecycleEvent lifecycleEvent) {
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            logger.error("There was a problem terminating the server executor.", ex);
        }
    }
}
