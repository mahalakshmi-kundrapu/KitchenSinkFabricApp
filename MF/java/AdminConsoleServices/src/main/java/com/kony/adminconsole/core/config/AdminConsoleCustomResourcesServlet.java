package com.kony.adminconsole.core.config;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import com.dbp.core.service.manager.DBPServicesManager;
import com.kony.adminconsole.commons.utils.ThreadExecutor;
import com.kony.adminconsole.core.security.ServicePermissionMapRegister;
import com.konylabs.middleware.servlet.IntegrationCustomServlet;

/**
 * Custom servlet used to register & release resources required by Customer 360
 * 
 * @author Alahari Prudhvi Akhil, Aditya Mankal
 *
 */
@IntegrationCustomServlet(servletName = "AdminConsoleCustomResourcesServlet", urlPatterns = {
        "AdminConsoleCustomResourcesServlet" })
public class AdminConsoleCustomResourcesServlet extends HttpServlet {

    private static final long serialVersionUID = -3727063610799396467L;

    private static final Logger LOG = Logger.getLogger(AdminConsoleCustomResourcesServlet.class);

    @Override
    public void init() throws ServletException {
        try {
            // Registering the class which populates the Service Permission Mapping
            DBPServicesManager.registerDBPServiceImpl(ServicePermissionMapRegister.class);
        } catch (Exception e) {
            LOG.error("Exception in initializing resources", e);
        }
    }

    @Override
    public void destroy() {
        try {
            // Shutting down the customer 360 thread pool
            ThreadExecutor.shutdownExecutor();
        } catch (Exception e) {
            LOG.error("Exception in destroying resources", e);
        }
    }

}
