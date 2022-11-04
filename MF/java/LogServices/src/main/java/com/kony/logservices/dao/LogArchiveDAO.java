package com.kony.logservices.dao;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2oException;

import com.kony.logservices.core.BaseActivity;
import com.kony.logservices.dto.MoneyMovementLogDTO;
import com.kony.logservices.dto.TransactionActivityDTO;
import com.kony.logservices.handler.LogDataSourceHandler;
import com.kony.logservices.util.SQLQueriesEnum;

/**
 * DAO handles execution of sql queries to log archive database
 * 
 * @author Venkateswara Rao Alla
 *
 */
public class LogArchiveDAO {

    private static final Logger LOG = Logger.getLogger(LogArchiveDAO.class);

    private static final int BATCH_SIZE = 50;

    /**
     * Archives all those logs where the event date is less than the provided date argument
     * 
     * @param logClazz
     * @param archiveBefore
     * @return
     */
    public static <T extends BaseActivity> boolean archiveLogs(Class<T> logClazz, Date archiveBefore) {

        try {
            int endOffset = BATCH_SIZE;
            int totalNumberOfRecords = 0;
            boolean isArchived = false;
            List<T> logs = fetchLogData(logClazz, archiveBefore, 0, endOffset);

            // continue archiving logs in batches
            while (logs != null && !logs.isEmpty()) {
                totalNumberOfRecords += logs.size();
                try {
                    // save logs in a batch. On failure, save logs one by one.
                    // One possible reason of failure situation is, logs got saved in archive table
                    // and failed in
                    // deleting from main log table. Next run will pick the same archived logs from
                    // main log table and
                    // tries to save it in archive table and fails due to primary key constraint
                    // violations.
                    saveLogsInBatch(logs);
                    isArchived = true;
                } catch (Sql2oException sql2oe) {
                    if (sql2oe.getCause() != null
                            && sql2oe.getCause() instanceof SQLIntegrityConstraintViolationException) {
                        isArchived = saveLogs(logs) == logs.size();
                    }
                }

                // delete archived logs
                if (isArchived) {
                    deleteLogs(logs);
                }

                // fetch next batch of logs
                logs = fetchLogData(logClazz, archiveBefore, endOffset, endOffset += BATCH_SIZE);
            }

            LOG.info("Total number of logs archived: " + totalNumberOfRecords);

        } catch (Exception e) {
            LOG.error("Failed to archive logs", e);
        }
        return true;
    }

    /**
     * Returns list of logs for the provided class. The maximum size of logs returned would be based on the provided
     * offsets.
     * 
     * @param logClazz
     * @param archiveBefore
     * @param startOffset
     * @param endOffset
     * @return
     */
    public static <T extends BaseActivity> List<T> fetchLogData(Class<T> logClazz, Date archiveBefore, int startOffset,
            int endOffset) {
        List<T> logs = new ArrayList<>();

        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().open()) {
            String fetchQuery = new StringBuilder(SQLQueriesEnum.getActivityReadQuery(logClazz)).append(" WHERE")
                    .append((logClazz.isAssignableFrom(TransactionActivityDTO.class)
                            || logClazz.isAssignableFrom(MoneyMovementLogDTO.class)) ? " transactionDate" : " eventts")
                    .append(" < :archiveBefore LIMIT :startOffset, :endOffset").toString();

            logs = con.createQuery(fetchQuery).addParameter("archiveBefore", archiveBefore)
                    .addParameter("startOffset", startOffset).addParameter("endOffset", endOffset)
                    .executeAndFetch(logClazz);
        } catch (Exception e) {
            LOG.error("Exception occured while fetching log data", e);
        }
        return logs;
    }

    /**
     * Saves all the provided list of logs in a single transaction
     * 
     * @param logs
     */
    public static <T extends BaseActivity> void saveLogsInBatch(List<T> logs) {
        if (logs != null && !logs.isEmpty()) {
            Class<? extends BaseActivity> logClazz = logs.get(0).getClass();
            try (Connection con = LogDataSourceHandler.getLogArchiveSql2oInstance().beginTransaction()) {
                for (T log : logs) {
                    con.createQuery(SQLQueriesEnum.getActivityInsertQuery(logClazz)).bind(log).executeUpdate();
                }
                con.commit();
            }
        }
    }

    /**
     * Saves each log from the provided list in a separate transaction. Returns the count of logs being saved
     * successfully, -1 incase if the argument is null or an empty list
     * 
     * @param logs
     * @return
     */
    public static <T extends BaseActivity> long saveLogs(List<T> logs) {
        long savedCount = -1;
        if (logs != null && !logs.isEmpty()) {
            savedCount = 0;
            Class<? extends BaseActivity> logClazz = logs.get(0).getClass();
            for (T log : logs) {
                try (Connection con = LogDataSourceHandler.getLogArchiveSql2oInstance().beginTransaction()) {
                    con.createQuery(SQLQueriesEnum.getActivityInsertQuery(logClazz)).bind(log).executeUpdate();
                    con.commit();
                    savedCount++;
                } catch (Sql2oException sql2oe) {
                    if (sql2oe.getCause() != null
                            && sql2oe.getCause() instanceof SQLIntegrityConstraintViolationException) {
                        // incrementing the saved count assuming the log already being saved previously
                        savedCount++;
                    } else {
                        LOG.error(
                                "Failure in saving archived log, continuing by saving another log from the provided list of logs",
                                sql2oe);
                    }

                }
            }
        }
        return savedCount;
    }

    /**
     * Deletes the list of provided logs from the main log table
     * 
     * @param logs
     */
    public static <T extends BaseActivity> void deleteLogs(List<T> logs) {
        if (logs != null && !logs.isEmpty()) {
            Class<? extends BaseActivity> logClazz = logs.get(0).getClass();
            Map<String, Object> parameters = new HashMap<>();

            ArrayList<String> idsList = new ArrayList<String>(logs.size());
            for (T log : logs) {
                idsList.add(log.getId());
            }

            StringBuilder deleteQuery = new StringBuilder(SQLQueriesEnum.getActivityDeleteQuery(logClazz));

            deleteQuery.append(" WHERE id IN (").append(LogDAO.generateInClauseParameters("id", idsList, parameters))
                    .append(") ");
            try (Connection con = LogDataSourceHandler.getLogSql2oInstance().beginTransaction()) {
                Query query = con.createQuery(deleteQuery.toString());
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    query.addParameter(entry.getKey(), entry.getValue());
                }
                query.executeUpdate();
                con.commit();
            }
        }
    }

}
