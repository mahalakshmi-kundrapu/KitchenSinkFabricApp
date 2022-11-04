package com.kony.adminconsole.commons.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.handler.EnvironmentConfigurationsHandler;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * Thread executor is used to maintain a common thread pool across Customer 360 app and carry out multi thread
 * operations
 * 
 * @author Alahari Prudhvi Akhil
 *
 */

public final class ThreadExecutor {

    private static final ExecutorService THREAD_POOL = createExecutor();
    private static final int DEFAULT_POOL_SIZE = 25;
    private static final String AC_THREAD_POOL_SIZE = "AC_THREAD_POOL_SIZE";

    private static final Logger LOG = Logger.getLogger(ThreadExecutor.class);

    private ThreadExecutor() {

    }

    private static ExecutorService createExecutor() {
        try {
            String environmentPoolSize = EnvironmentConfigurationsHandler.getValue(AC_THREAD_POOL_SIZE,
                    (DataControllerRequest) null);
            if (StringUtils.isNotBlank(environmentPoolSize)) {
                return Executors.newFixedThreadPool(Integer.parseInt(environmentPoolSize));
            }
        } catch (Exception e) {
            LOG.error("Exception in creating Thread Pool.", e);
        }
        try {
            return Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
        } catch (Exception e) {
            LOG.error("Exception in creating Thread Pool of Fixed Size.", e);
            return null;
        }
    }

    public static void shutdownExecutor() {
        // Shutdown the thread pool when the app gets unpublished from the environment
        if (THREAD_POOL != null) {
            THREAD_POOL.shutdown();
        }
    }

    public static <T> Future<T> execute(Callable<T> callable) throws InterruptedException {
        if (THREAD_POOL != null) {
            return THREAD_POOL.submit(callable);
        }
        return null;
    }

    public static <T> List<Future<T>> execute(List<Callable<T>> callables) throws InterruptedException {
        if (THREAD_POOL != null) {
            return THREAD_POOL.invokeAll(callables);
        }
        return null;
    }

    public static <T> List<T> executeAndWaitforCompletion(List<Callable<T>> callables)
            throws InterruptedException, ExecutionException {
        if (THREAD_POOL != null) {
            List<T> t = new ArrayList<T>();
            List<Future<T>> futures = THREAD_POOL.invokeAll(callables);
            // Wait for all the futures to complete
            for (Future<T> future : futures) {
                t.add(future.get());
            }
            return t;
        }
        return null;
    }
}
