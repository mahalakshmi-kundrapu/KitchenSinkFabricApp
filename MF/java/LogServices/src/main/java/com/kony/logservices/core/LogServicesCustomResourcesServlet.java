package com.kony.logservices.core;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.kony.logservices.handler.LogDataSourceHandler;
import com.konylabs.middleware.servlet.IntegrationCustomServlet;

/**
 * Custom servlet used to intialize/destroy resources required by Log Services
 * 
 * @author Venkateswara Rao Alla
 *
 */

@IntegrationCustomServlet(servletName = "LogServicesCustomResourcesServlet", urlPatterns = {
        "LogServicesCustomResourcesServlet" })
public class LogServicesCustomResourcesServlet extends HttpServlet {

    private static final long serialVersionUID = -3727063610799396927L;
    private static final Logger LOG = Logger.getLogger(LogServicesCustomResourcesServlet.class);

    @Override
    public void init() throws ServletException {
        /*
         * Workaround. Loading the following referenced custom classes in this method to avoid
         * java.lang.NoClassDefFoundError on custom classes when destroy() is called on app un-publish.
         */
        loadCustomClasses();

    }

    @Override
    public void destroy() {
        // closing logs datasources
        LogDataSourceHandler.closeLogDataSource();
        LogDataSourceHandler.closeLogArchiveDataSource();
    }

    public void loadCustomClasses() {
        try {
            @SuppressWarnings("unused")
            Class<LogDataSourceHandler> logDataSourceHandlerClazz = LogDataSourceHandler.class;
        } catch (Exception e) {
            LOG.error("Error while loading custom classes", e);
        }
    }

}
