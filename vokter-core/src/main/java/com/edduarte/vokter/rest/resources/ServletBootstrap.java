package com.edduarte.vokter.rest.resources;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Info;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class ServletBootstrap extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(ServletBootstrap.class);

    private static final long serialVersionUID = -6039834823506457822L;

//    public static final String SCHEDULER_ATTRIBUTE_KEY = "UCTIME_SCHEDULER";
//
//    private Scheduler scheduler;


    @Override
    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();

//        try {
//            StdSchedulerFactory factory = new StdSchedulerFactory();
//            factory.initialize(servletContext.getResourceAsStream("/WEB-INF/quartz.properties"));
//            scheduler = factory.getScheduler();
//            factory = null;
//            scheduler.start();
//
//            servletContext.setAttribute(SCHEDULER_ATTRIBUTE_KEY, scheduler);
//
//        } catch (SchedulerException e) {
//            throw new ServletException(e);
//        }

        Info info = new Info()
                .title("Vokter REST API")
                .description("Vokter is a high-performance, scalable web " +
                        "service that provides web-page monitoring, " +
                        "triggering notifications when specified keywords " +
                        "were either added or removed from a web document. " +
                        "This service implements a information retrieval " +
                        "system that fetches, indexes and performs queries " +
                        "over web documents on a periodic basis. Difference " +
                        "detection is implemented by comparing occurrences " +
                        "between two snapshots of the same document. " +
                        "Additionally, it supports multi-language stop-word " +
                        "filtering to ignore changes in common grammatical " +
                        "conjunctions or articles, and stemming to detect " +
                        "changes in lexically derived words.");

        final String contextPath = servletContext.getContextPath();
        final StringBuilder sbBasePath = new StringBuilder();
        logger.info("{}", contextPath);
        sbBasePath.append(contextPath);
        sbBasePath.append("/api");

        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setBasePath(sbBasePath.toString());
        beanConfig.setResourcePackage("com.edduarte.rest.resources");
        beanConfig.setScan(true);
        beanConfig.setInfo(info);

        super.init(config);
    }


    @Override
    public void destroy() {
//        try {
//            scheduler.shutdown();
//        } catch (SchedulerException ex) {
//            logger.error(ex.getMessage(), ex);
//        }

        super.destroy();
    }
}
