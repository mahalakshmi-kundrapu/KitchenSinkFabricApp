package com.kony.logservices.dao;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.sql2o.Connection;
import org.sql2o.Query;

import com.kony.adminconsole.commons.utils.DateUtils;
import com.kony.logservices.core.BaseActivity;
import com.kony.logservices.dto.AdminActivityDTO;
import com.kony.logservices.dto.AdminCustomerActivityDTO;
import com.kony.logservices.dto.AuditLogsAndMoneyMovementLogsDTO;
import com.kony.logservices.dto.CustomerActivityDTO;
import com.kony.logservices.dto.PaginationDTO;
import com.kony.logservices.dto.SearchCustomerAuditLogsDTO;
import com.kony.logservices.dto.TransactionActivityDTO;
import com.kony.logservices.dto.TransactionValueVolumeTypeDTO;
import com.kony.logservices.handler.LogDataSourceHandler;
import com.kony.logservices.util.SQLQueriesEnum;

/**
 * DAO handles execution of sql queries to log database
 * 
 * @author Venkateswara Rao Alla
 *
 */
public class LogDAO {

    private static final Logger LOG = Logger.getLogger(LogDAO.class);

    public static <T extends BaseActivity> void saveLog(T log) {

        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().beginTransaction()) {
            con.createQuery(SQLQueriesEnum.getActivityInsertQuery(log.getClass())).bind(log).executeUpdate();
            con.commit();
        }
    }

    public static List<CustomerActivityDTO> getLastNCustomerSessions(String username, Integer sessionCount) {

        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().open()) {
            return con.createQuery(SQLQueriesEnum.AUDITLOG_LAST_N_SESSIONS.getQuery())
                    .addParameter("username", username).addParameter("limit", sessionCount)
                    .executeAndFetch(CustomerActivityDTO.class);
        }
    }

    public static List<CustomerActivityDTO> getAllActivitiesForASession(String sessionId) {

        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().open()) {
            return con
                    .createQuery(
                            SQLQueriesEnum.AUDITLOG_READ.getQuery() + " WHERE sessionId=:sessionId order by eventts")
                    .addParameter("sessionId", sessionId).executeAndFetch(CustomerActivityDTO.class);
        }
    }

    public static PaginationDTO<TransactionActivityDTO> getPaginatedTransactions(String searchText, String searchName,
            String startDate, String endDate, Integer startAmount, Integer endAmount, int pageNumber, int noOfRecords,
            String sortBy, String sortDirection, ArrayList<String> fromAccountTypeArray,
            ArrayList<String> toAccountTypeArray, ArrayList<String> statusTypeArray, ArrayList<String> typeArray,
            ArrayList<String> currencyTypeArray, String fromMobileEmail, String toMobileEmail) {
        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().open()) {
            StringBuilder paginationQuery = new StringBuilder();
            Map<String, Object> parameters = new HashMap<>();
            String whereClauses = getTransactionLogsQuery(searchText, searchName, startDate, endDate, startAmount,
                    endAmount, sortBy, sortDirection, fromAccountTypeArray, toAccountTypeArray, statusTypeArray,
                    typeArray, fromMobileEmail, toMobileEmail, currencyTypeArray, parameters);

            // Query to get Count based on filters
            Query txCountQuery = con.createQuery(SQLQueriesEnum.TRANSACTIONLOG_COUNT_READ.getQuery() + whereClauses);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                txCountQuery.addParameter(entry.getKey(), entry.getValue());
            }
            Integer count = txCountQuery.executeScalar(Integer.class);

            // Query to fetch List of Logs
            if (pageNumber > 0 && noOfRecords > 0) {
                int offsetValue = getOffsetValue(pageNumber, noOfRecords);
                paginationQuery.append(" LIMIT :noOfRecords OFFSET :offsetValue");
                parameters.put("noOfRecords", noOfRecords);
                parameters.put("offsetValue", offsetValue);
            }
            Query txQuery = con
                    .createQuery(SQLQueriesEnum.TRANSACTIONLOG_READ.getQuery() + whereClauses + paginationQuery);

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                txQuery.addParameter(entry.getKey(), entry.getValue());
            }
            List<TransactionActivityDTO> transactionLogs = txQuery.executeAndFetch(TransactionActivityDTO.class);
            PaginationDTO<TransactionActivityDTO> result = new PaginationDTO<>();
            result.setPage(pageNumber);
            result.setPageSize(noOfRecords);
            result.setCount(count);
            result.setLogs(transactionLogs);
            return result;
        }
    }

    public static String getTransactionLogsQuery(String searchText, String serviceName, String startDate,
            String endDate, Integer startAmount, Integer endAmount, String sortBy, String sortDirection,
            ArrayList<String> fromAccountTypeArray, ArrayList<String> toAccountTypeArray,
            ArrayList<String> statusTypeArray, ArrayList<String> typeArray, String fromMobileEmail,
            String toMobileEmail, ArrayList<String> currencyTypeArray, Map<String, Object> parameters) {
        StringBuilder query = new StringBuilder();

        if (StringUtils.isNotBlank(serviceName)) {
            query.append(" serviceName = :serviceName AND");
            parameters.put("serviceName", serviceName);

        }

        if (StringUtils.isNotBlank(searchText)) {
            query.append(" (transactionId LIKE :searchText OR username LIKE :searchText) AND");
            parameters.put("searchText", searchText + "%");
        }

        if (startDate != null && endDate != null) {
            Date startOfDate = null;
            Date endOfDate = null;
            try {
                startOfDate = DateUtils.parseToStartOfDay(startDate, DateUtils.PATTERN_MM_DD_YYYY);
                endOfDate = DateUtils.parseToEndOfDay(endDate, DateUtils.PATTERN_MM_DD_YYYY);
            } catch (ParseException e) {
                LOG.error("Failure in parsing date strings", e);

            }
            if (startOfDate != null && endOfDate != null) {
                query.append(" transactionDate BETWEEN :startDate AND :endDate AND");
                parameters.put("startDate", startOfDate);
                parameters.put("endDate", endOfDate);
            }
        }

        if (startAmount != null && endAmount != null) {
            query.append(" amount BETWEEN :startAmount AND :endAmount AND");
            parameters.put("startAmount", startAmount);
            parameters.put("endAmount", endAmount);
        }

        if (fromAccountTypeArray != null && fromAccountTypeArray.size() > 0) {
            query.append(" fromAccountType IN (")
                    .append(generateInClauseParameters("fromAccountType", fromAccountTypeArray, parameters))
                    .append(") AND");
        }

        if (toAccountTypeArray != null && toAccountTypeArray.size() > 0) {
            query.append(" toAccountType IN (")
                    .append(generateInClauseParameters("toAccountType", toAccountTypeArray, parameters))
                    .append(") AND");
        }

        if (statusTypeArray != null && statusTypeArray.size() > 0) {
            query.append(" status IN (").append(generateInClauseParameters("status", statusTypeArray, parameters))
                    .append(") AND");
        }

        if (typeArray != null && typeArray.size() > 0) {
            query.append(" type IN (").append(generateInClauseParameters("type", typeArray, parameters))
                    .append(") AND");
        }

        if (currencyTypeArray != null && currencyTypeArray.size() > 0) {
            query.append(" currencyCode IN (")
                    .append(generateInClauseParameters("currencyCode", currencyTypeArray, parameters)).append(") AND");
        }

        if (fromMobileEmail != null) {
            if (fromMobileEmail.equalsIgnoreCase("mobile")) {
                query.append(" fromMobileOrEmail LIKE '%@%' AND");
            } else {
                query.append(" fromMobileOrEmail NOT LIKE '%@%' AND");
            }
        }

        if (toMobileEmail != null) {
            if (toMobileEmail.equalsIgnoreCase("mobile")) {
                query.append(" toMobileOrEmail LIKE '%@%' AND");
            } else {
                query.append(" toMobileOrEmail NOT LIKE '%@%' AND");
            }
        }

        if (query.length() > 0) {
            query.insert(0, " WHERE");
            query.delete(query.lastIndexOf("AND"), query.length());
        }
        if (sortBy != null && sortDirection != null) {
            query.append(" ORDER BY " + sortBy + " " + sortDirection);
        } else {
            query.append(" ORDER BY transactionDate DESC");
        }

        return query.toString();
    }

    public static PaginationDTO<AdminCustomerActivityDTO> fetchAdminCustomerActivityLogs(String userName,
            String moduleName, String activityType, String startDate, String endDate, String searchText,
            ArrayList<String> statusTypeArray, ArrayList<String> channelTypeArray, ArrayList<String> osTypeArray,
            ArrayList<String> roleTypeArray, int pageNumber, int noOfRecords, String sortDirection) {
        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().open()) {
            StringBuilder paginationQuery = new StringBuilder();

            Map<String, Object> parameters = new HashMap<>();
            String whereClauses = buildCustomerActivityLogQuery(false, userName, moduleName, activityType, startDate,
                    endDate, searchText, statusTypeArray, channelTypeArray, osTypeArray, roleTypeArray, pageNumber,
                    noOfRecords, sortDirection, parameters);
            // Query to get Count based on filters
            Query txCountQuery = con
                    .createQuery(SQLQueriesEnum.ADMINCUSTOMERACTIVITY_READ_COUNT.getQuery() + whereClauses);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                txCountQuery.addParameter(entry.getKey(), entry.getValue());
            }
            Integer count = txCountQuery.executeScalar(Integer.class);

            // Query to fetch List of Logs
            if (pageNumber > 0 && noOfRecords > 0) {
                int offsetValue = getOffsetValue(pageNumber, noOfRecords);
                paginationQuery.append(" LIMIT :noOfRecords OFFSET :offsetValue");
                parameters.put("noOfRecords", noOfRecords);
                parameters.put("offsetValue", offsetValue);
            }
            Query txQuery = con
                    .createQuery(SQLQueriesEnum.ADMINCUSTOMERACTIVITY_READ.getQuery() + whereClauses + paginationQuery);

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                txQuery.addParameter(entry.getKey(), entry.getValue());
            }
            List<AdminCustomerActivityDTO> transactionLogs = txQuery.executeAndFetch(AdminCustomerActivityDTO.class);
            PaginationDTO<AdminCustomerActivityDTO> result = new PaginationDTO<>();
            result.setPage(pageNumber);
            result.setPageSize(noOfRecords);
            result.setCount(count);
            result.setLogs(transactionLogs);
            return result;
        }
    }

    public static PaginationDTO<AdminActivityDTO> getPaginatedAdminConsoleLogs(String searchText, String moduleName,
            String startDate, String endDate, String sortBy, String sortDirection, int pageNumber, int noOfRecords,
            ArrayList<String> eventTypeArray, ArrayList<String> userRoleTypeArray, ArrayList<String> statusTypeArray) {
        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().open()) {
            Map<String, Object> parameters = new HashMap<>();
            StringBuilder whereClause = new StringBuilder();
            if (StringUtils.isNotBlank(searchText)) {
                whereClause.append(" username LIKE :searchText AND");
                parameters.put("searchText", searchText + "%");
            }
            if (StringUtils.isNotBlank(moduleName)) {
                whereClause.append(" moduleName LIKE :moduleName AND");
                parameters.put("moduleName", moduleName + "%");
            }
            if (startDate != null && endDate != null) {
                Date startOfDate = null;
                Date endOfDate = null;
                try {
                    startOfDate = DateUtils.parseToStartOfDay(startDate, DateUtils.PATTERN_MM_DD_YYYY);
                    endOfDate = DateUtils.parseToEndOfDay(endDate, DateUtils.PATTERN_MM_DD_YYYY);
                } catch (ParseException e) {
                    LOG.error("Failure in parsing date strings", e);

                }
                if (startOfDate != null && endOfDate != null) {
                    whereClause.append(" eventts BETWEEN :startDate AND :endDate AND");
                    parameters.put("startDate", startOfDate);
                    parameters.put("endDate", endOfDate);
                }
            }
            if (eventTypeArray != null && eventTypeArray.size() > 0) {
                whereClause.append(" event IN (")
                        .append(generateInClauseParameters("event", eventTypeArray, parameters)).append(") AND");
            }
            if (userRoleTypeArray != null && userRoleTypeArray.size() > 0) {
                whereClause.append(" userRole IN (")
                        .append(generateInClauseParameters("userRole", userRoleTypeArray, parameters)).append(") AND");
            }
            if (statusTypeArray != null && statusTypeArray.size() > 0) {
                whereClause.append(" status IN (")
                        .append(generateInClauseParameters("status", statusTypeArray, parameters)).append(") AND");
            }
            if (whereClause.length() > 0) {
                whereClause.insert(0, " WHERE");
                whereClause.delete(whereClause.lastIndexOf("AND"), whereClause.length());
            }
            if (sortBy != null && sortDirection != null) {
                whereClause.append(" ORDER BY " + sortBy + " " + sortDirection);
            } else {
                whereClause.append(" ORDER BY eventts DESC");
            }
            // Query to get Count based on filters
            Query acCountQuery = con.createQuery(SQLQueriesEnum.ADMINACTIVITY_COUNT_READ.getQuery() + whereClause);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                acCountQuery.addParameter(entry.getKey(), entry.getValue());
            }
            Integer count = acCountQuery.executeScalar(Integer.class);

            // Query to fetch List of Logs
            if (pageNumber > 0 && noOfRecords > 0) {
                int offsetValue = getOffsetValue(pageNumber, noOfRecords);
                whereClause.append(" LIMIT :noOfRecords OFFSET :offsetValue");
                parameters.put("noOfRecords", noOfRecords);
                parameters.put("offsetValue", offsetValue);
            }
            Query acQuery = con.createQuery(SQLQueriesEnum.ADMINACTIVITY_READ.getQuery() + whereClause);

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                acQuery.addParameter(entry.getKey(), entry.getValue());
            }
            List<AdminActivityDTO> adminConsoleLogs = acQuery.executeAndFetch(AdminActivityDTO.class);
            PaginationDTO<AdminActivityDTO> result = new PaginationDTO<>();
            result.setPage(pageNumber);
            result.setPageSize(noOfRecords);
            result.setCount(count);
            result.setLogs(adminConsoleLogs);
            return result;
        }

    }

    public static PaginationDTO<CustomerActivityDTO> fetchCustomerActivityLogs(String userName, String moduleName,
            String activityType, String startDate, String endDate, String searchText, ArrayList<String> statusTypeArray,
            ArrayList<String> channelTypeArray, ArrayList<String> osTypeArray, ArrayList<String> roleTypeArray,
            int pageNumber, int noOfRecords, String sortDirection) {
        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().open()) {
            StringBuilder paginationQuery = new StringBuilder();

            Map<String, Object> parameters = new HashMap<>();
            String whereClauses = buildCustomerActivityLogQuery(true, userName, moduleName, activityType, startDate,
                    endDate, searchText, statusTypeArray, channelTypeArray, osTypeArray, roleTypeArray, pageNumber,
                    noOfRecords, sortDirection, parameters);
            // Query to get Count based on filters
            Query txCountQuery = con.createQuery(SQLQueriesEnum.CUSTOMERACTIVITY_READ_COUNT.getQuery() + whereClauses);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                txCountQuery.addParameter(entry.getKey(), entry.getValue());
            }
            Integer count = txCountQuery.executeScalar(Integer.class);

            // Query to fetch List of Logs
            if (pageNumber > 0 && noOfRecords > 0) {
                int offsetValue = getOffsetValue(pageNumber, noOfRecords);
                paginationQuery.append(" LIMIT :noOfRecords OFFSET :offsetValue");
                parameters.put("noOfRecords", noOfRecords);
                parameters.put("offsetValue", offsetValue);
            }
            Query txQuery = con
                    .createQuery(SQLQueriesEnum.CUSTOMERACTIVITY_READ.getQuery() + whereClauses + paginationQuery);

            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                txQuery.addParameter(entry.getKey(), entry.getValue());
            }
            List<CustomerActivityDTO> transactionLogs = txQuery.executeAndFetch(CustomerActivityDTO.class);
            PaginationDTO<CustomerActivityDTO> result = new PaginationDTO<>();
            result.setPage(pageNumber);
            result.setPageSize(noOfRecords);
            result.setCount(count);
            result.setLogs(transactionLogs);
            return result;
        }
    }

    public static String buildCustomerActivityLogQuery(boolean isMemberActivity, String userName, String moduleName,
            String activityType, String startDate, String endDate, String searchText, ArrayList<String> statusTypeArray,
            ArrayList<String> channelTypeArray, ArrayList<String> osTypeArray, ArrayList<String> roleTypeArray,
            int pageNumber, int noOfRecords, String sortDirection, Map<String, Object> parameters) {

        StringBuilder query = new StringBuilder();
        if (isMemberActivity) {
            query.append("username = :username AND ");
            parameters.put("username", userName);
            if (StringUtils.isNotBlank(moduleName)) {
                query.append("moduleName = :moduleName AND ");
                parameters.put("moduleName", moduleName);
            }
            if (StringUtils.isNotBlank(searchText)) {
                query.append("moduleName Like :searchText AND ");
                parameters.put("searchText", searchText + "%");
            }
            if (statusTypeArray != null && statusTypeArray.size() > 0) {
                query.append(" status IN (").append(generateInClauseParameters("status", statusTypeArray, parameters))
                        .append(") AND");
            }
            if (channelTypeArray != null && channelTypeArray.size() > 0) {
                query.append(" channel IN (")
                        .append(generateInClauseParameters("channel", channelTypeArray, parameters)).append(") AND");
            }
            if (osTypeArray != null && osTypeArray.size() > 0) {
                query.append(" operatingSystem IN (")
                        .append(generateInClauseParameters("operatingSystem", osTypeArray, parameters)).append(") AND");
            }
        } else {
            query.append("customerId = :userid AND ");
            parameters.put("userid", userName);
            if (StringUtils.isNotBlank(searchText)) {
                query.append("adminName like :searchText AND ");
                parameters.put("searchText", searchText + "%");
            }
            if (roleTypeArray != null && roleTypeArray.size() > 0) {
                query.append(" adminRole IN (")
                        .append(generateInClauseParameters("adminRole", roleTypeArray, parameters)).append(") AND");
            }
        }

        if (StringUtils.isNotBlank(activityType)) {
            query.append("activityType = :activityType AND ");
            parameters.put("activityType", activityType);
        }
        if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
            Date startOfDate = null;
            Date endOfDate = null;
            try {
                startOfDate = DateUtils.parseToStartOfDay(startDate, DateUtils.PATTERN_MM_DD_YYYY);
                endOfDate = DateUtils.parseToEndOfDay(endDate, DateUtils.PATTERN_MM_DD_YYYY);
            } catch (ParseException e) {
                LOG.error("Failure in parsing date strings", e);
            }

            if (startOfDate != null && endOfDate != null) {
                query.append(" eventts BETWEEN :startDate AND :endDate AND");
                parameters.put("startDate", startOfDate);
                parameters.put("endDate", endOfDate);
            }
        }
        if (query.length() > 0) {
            query.insert(0, " WHERE ");
            query.delete(query.lastIndexOf("AND"), query.length());
            query.append(" ORDER BY eventts ");
            if (sortDirection != null) {
                query.append(sortDirection);
            }
        }
        return query.toString();
    }

    public static List<TransactionValueVolumeTypeDTO> getTransactionValueLogsQuery(Date startDate, Date endDate) {
        List<TransactionValueVolumeTypeDTO> transactionValueVolumeTypes = new ArrayList<>();

        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().open()) {

            StringBuilder queryStr = new StringBuilder(SQLQueriesEnum.TRANSACTIONVALUEVOLUMETYPE_READ.getQuery());
            if (startDate != null && endDate != null) {
                queryStr.append(" where transactionDate BETWEEN :startDate AND :endDate");
            }
            queryStr.append(" group by serviceName, channel");

            Query query = con.createQuery(queryStr.toString());

            if (startDate != null && endDate != null) {
                query.addParameter("startDate", startDate).addParameter("endDate", endDate);
            }

            transactionValueVolumeTypes = query.executeAndFetch(TransactionValueVolumeTypeDTO.class);
        }
        return transactionValueVolumeTypes;
    }

    private static int getOffsetValue(int pageNumber, int noOfRecords) {
        return ((pageNumber - 1) * noOfRecords);
    }

    public static String generateInClauseParameters(String basename, ArrayList<String> array,
            Map<String, Object> parameters) {
        StringBuilder pattern = new StringBuilder();
        String placeHolder = null;
        for (int index = 0; index < array.size(); index++) {
            placeHolder = basename + index;
            pattern.append(":" + placeHolder + ",");
            parameters.put(placeHolder, array.get(index));
        }
        pattern.deleteCharAt((pattern.length() - 1));
        return pattern.toString();
    }

    public static PaginationDTO<AuditLogsAndMoneyMovementLogsDTO> searchCustomerAuditLogs(
            SearchCustomerAuditLogsDTO customerAuditLogsDTO) {

        try (Connection con = LogDataSourceHandler.getLogSql2oInstance().open()) {
            Query txQuery = con.createQuery(constructSearchQuery(customerAuditLogsDTO));

            List<AuditLogsAndMoneyMovementLogsDTO> auditLogs = txQuery
                    .executeAndFetch(AuditLogsAndMoneyMovementLogsDTO.class);

            PaginationDTO<AuditLogsAndMoneyMovementLogsDTO> result = new PaginationDTO<>();
            result.setPageSize(customerAuditLogsDTO.getPageSize());
            result.setPageOffset(customerAuditLogsDTO.getPageOffset());
            result.setPage(customerAuditLogsDTO.getPageOffset() / customerAuditLogsDTO.getPageSize());
            result.setSortVariable(customerAuditLogsDTO.getSortVariable());
            result.setSortDirection(customerAuditLogsDTO.getSortDirection());

            Boolean hasNextPage = (auditLogs.size() == customerAuditLogsDTO.getPageSize() + 1);
            result.setHasNextPage(hasNextPage);
            if (hasNextPage) {
                auditLogs.remove(auditLogs.size() - 1);
            }

            result.setLogs(auditLogs);
            return result;

        }
    }

    private static String constructSearchQuery(SearchCustomerAuditLogsDTO customerAuditLogsDTO) {

        StringBuilder searchQuery = new StringBuilder(SQLQueriesEnum.AUDITLOG_SEARCH_SELECT_QUERY.getQuery());
        StringBuilder whereClause = new StringBuilder();

        if (StringUtils.isNotBlank(customerAuditLogsDTO.getUsername())) {
            whereClause.append(" al.UserName = '" + customerAuditLogsDTO.getUsername() + "'");
        }

        if (StringUtils.isNotBlank(customerAuditLogsDTO.getCustomerid())) {
            if (whereClause.length() > 0)
                whereClause.append(" and ");
            whereClause.append(" al.Customer_Id = '" + customerAuditLogsDTO.getCustomerid() + "'");
        }

        if (StringUtils.isNotBlank(customerAuditLogsDTO.getSearchText())) {
            if (whereClause.length() > 0)
                whereClause.append(" and ");
            whereClause.append(" (al.UserName like '" + customerAuditLogsDTO.getSearchText()
                    + "%' or ml.fromAccountNumber like '" + customerAuditLogsDTO.getSearchText()
                    + "%' or ml.toAccountNumber like '" + customerAuditLogsDTO.getSearchText() + "%')");
        }

        if (customerAuditLogsDTO.isCSRAssistFlagSet()) {
            if (customerAuditLogsDTO.getIsCSRAssist()) {
                if (whereClause.length() > 0)
                    whereClause.append(" and ");
                whereClause.append(" al.isCSRAssist = '1' ");
            } else {
                if (whereClause.length() > 0)
                    whereClause.append(" and ");
                whereClause.append(" al.isCSRAssist = '0' ");
            }
        }

        if (StringUtils.isNotBlank(customerAuditLogsDTO.getModule())) {
            if (whereClause.length() > 0)
                whereClause.append(" and ");
            whereClause.append(" al.EventType = '" + customerAuditLogsDTO.getModule() + "'");
        }

        if (StringUtils.isNotBlank(customerAuditLogsDTO.getActivityType())) {
            if (whereClause.length() > 0)
                whereClause.append(" and ");
            whereClause.append(" al.EventSubType = '" + customerAuditLogsDTO.getActivityType() + "'");
        }

        if (StringUtils.isNotBlank(customerAuditLogsDTO.getStartAmount())) {
            if (whereClause.length() > 0)
                whereClause.append(" and ");
            whereClause.append(" ml.amount >= '" + customerAuditLogsDTO.getStartAmount() + "'");
        }

        if (StringUtils.isNotBlank(customerAuditLogsDTO.getEndAmount())) {
            if (whereClause.length() > 0)
                whereClause.append(" and ");
            whereClause.append(" ml.amount <= '" + customerAuditLogsDTO.getEndAmount() + "'");
        }

        if (StringUtils.isNotBlank(customerAuditLogsDTO.getStartDate())) {
            if (whereClause.length() > 0)
                whereClause.append(" and ");
            whereClause.append(" Date(al.createdts) >= '" + customerAuditLogsDTO.getStartDate() + "'");
        }

        if (StringUtils.isNotBlank(customerAuditLogsDTO.getEndDate())) {
            if (whereClause.length() > 0)
                whereClause.append(" and ");
            whereClause.append(" Date(al.createdts) <= '" + customerAuditLogsDTO.getEndDate() + "'");
        }

        searchQuery.append(
                " al left join moneymovementlog ml ON (!isnull(al.MoneyMovementRefId) and ml.Id = al.MoneyMovementRefId)");

        if (whereClause.length() > 0) {
            searchQuery.append(" WHERE ").append(whereClause);
        }

        searchQuery.append(" ORDER BY ").append(customerAuditLogsDTO.getSortVariable()).append(" ")
                .append(customerAuditLogsDTO.getSortDirection());
        searchQuery.append(" LIMIT ").append(customerAuditLogsDTO.getPageOffset()).append(",")
                .append(customerAuditLogsDTO.getPageSize() + 1);

        return searchQuery.toString();
    }
}