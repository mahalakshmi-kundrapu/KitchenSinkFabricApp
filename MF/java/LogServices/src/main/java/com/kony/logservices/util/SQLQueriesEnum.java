package com.kony.logservices.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.kony.logservices.core.BaseActivity;
import com.kony.logservices.dto.AdminActivityDTO;
import com.kony.logservices.dto.AdminCustomerActivityDTO;
import com.kony.logservices.dto.AuditActivityDTO;
import com.kony.logservices.dto.CustomerActivityDTO;
import com.kony.logservices.dto.MoneyMovementLogDTO;
import com.kony.logservices.dto.TransactionActivityDTO;

/**
 * Enum used to maintain sql queries with associated constants
 * 
 * @author Venkateswara Rao Alla
 * 
 */

public enum SQLQueriesEnum {

    // admin activity
    ADMINACTIVITY_READ, ADMINACTIVITY_INSERT, ADMINACTIVITY_DELETE, ADMINACTIVITY_COUNT_READ,

    // customer activity
    CUSTOMERACTIVITY_READ, CUSTOMERACTIVITY_INSERT, CUSTOMERACTIVITY_DELETE, CUSTOMERACTIVITY_READ_COUNT,
    CUSTOMERACTIVITY_LAST_N_SESSIONS,

    // customer activity by admin
    ADMINCUSTOMERACTIVITY_READ, ADMINCUSTOMERACTIVITY_INSERT, ADMINCUSTOMERACTIVITY_DELETE,
    ADMINCUSTOMERACTIVITY_READ_COUNT,

    // transaction log
    TRANSACTIONLOG_READ, TRANSACTIONLOG_INSERT, TRANSACTIONLOG_DELETE, TRANSACTIONLOG_COUNT_READ,
    TRANSACTIONVALUEVOLUMETYPE_READ,

    // Audit logs
    AUDITLOG_READ, AUDITLOG_ARCH_READ, AUDITLOG_INSERT, AUDITLOG_DELETE, AUDITLOG_COUNT_READ,
    AUDITLOG_SEARCH_SELECT_QUERY, AUDITLOG_LAST_N_SESSIONS,

    // MoneyMovement logs
    MONEYMOVEMENT_READ, MONEYMOVEMENT_DELETE, MONEYMOVEMENT_INSERT;

    private static final Logger LOG = Logger.getLogger(SQLQueriesEnum.class);
    private static final Properties PROPS = loadProps();

    private SQLQueriesEnum() {
    }

    private static Properties loadProps() {
        Properties properties = new Properties();
        try (InputStream inputStream = SQLQueriesEnum.class.getClassLoader()
                .getResourceAsStream("sql-queries.properties")) {
            properties.load(inputStream);
            return properties;
        } catch (Exception e) {
            LOG.error("Error while loading sql-queries.properties", e);
        }
        return properties;
    }

    /**
     * Returns query associated with this enum constant
     * 
     * @param key
     * @return
     */
    public String getQuery() {
        return PROPS.getProperty(this.name());
    }

    static final Map<Class<? extends BaseActivity>, SQLQueriesEnum> LOG_INSERT_QUERIES_MAPPER =
            mapLogTypeAndInsertQueries();

    static final Map<Class<? extends BaseActivity>, SQLQueriesEnum> LOG_DELETE_QUERIES_MAPPER =
            mapLogTypeAndDeleteQueries();

    static final Map<Class<? extends BaseActivity>, SQLQueriesEnum> LOG_READ_QUERIES_MAPPER =
            mapLogTypeAndReadQueries();

    /**
     * Mapper maps sub class of type {@link BaseActivity} to corresponding insert query constant from
     * {@link SQLQueriesEnum}
     * 
     * @return
     */
    public static Map<Class<? extends BaseActivity>, SQLQueriesEnum> mapLogTypeAndInsertQueries() {
        Map<Class<? extends BaseActivity>, SQLQueriesEnum> map = new HashMap<>();
        map.put(TransactionActivityDTO.class, SQLQueriesEnum.TRANSACTIONLOG_INSERT);
        map.put(AdminActivityDTO.class, SQLQueriesEnum.ADMINACTIVITY_INSERT);
        map.put(CustomerActivityDTO.class, SQLQueriesEnum.CUSTOMERACTIVITY_INSERT);
        map.put(AdminCustomerActivityDTO.class, SQLQueriesEnum.ADMINCUSTOMERACTIVITY_INSERT);
        map.put(AuditActivityDTO.class, SQLQueriesEnum.AUDITLOG_INSERT);
        map.put(MoneyMovementLogDTO.class, SQLQueriesEnum.MONEYMOVEMENT_INSERT);
        return map;
    }

    /**
     * Mapper maps sub class of type {@link BaseActivity} to corresponding delete query constant from
     * {@link SQLQueriesEnum}
     * 
     * @return
     */
    public static Map<Class<? extends BaseActivity>, SQLQueriesEnum> mapLogTypeAndDeleteQueries() {
        Map<Class<? extends BaseActivity>, SQLQueriesEnum> map = new HashMap<>();
        map.put(TransactionActivityDTO.class, SQLQueriesEnum.TRANSACTIONLOG_DELETE);
        map.put(AdminActivityDTO.class, SQLQueriesEnum.ADMINACTIVITY_DELETE);
        map.put(CustomerActivityDTO.class, SQLQueriesEnum.CUSTOMERACTIVITY_DELETE);
        map.put(AdminCustomerActivityDTO.class, SQLQueriesEnum.ADMINCUSTOMERACTIVITY_DELETE);
        map.put(AuditActivityDTO.class, SQLQueriesEnum.AUDITLOG_DELETE);
        map.put(MoneyMovementLogDTO.class, SQLQueriesEnum.MONEYMOVEMENT_DELETE);
        return map;
    }

    /**
     * Mapper maps sub class of type {@link BaseActivity} to corresponding read query constant from
     * {@link SQLQueriesEnum}
     * 
     * @return
     */
    public static Map<Class<? extends BaseActivity>, SQLQueriesEnum> mapLogTypeAndReadQueries() {
        Map<Class<? extends BaseActivity>, SQLQueriesEnum> map = new HashMap<>();
        map.put(TransactionActivityDTO.class, SQLQueriesEnum.TRANSACTIONLOG_READ);
        map.put(AdminActivityDTO.class, SQLQueriesEnum.ADMINACTIVITY_READ);
        map.put(CustomerActivityDTO.class, SQLQueriesEnum.CUSTOMERACTIVITY_READ);
        map.put(AdminCustomerActivityDTO.class, SQLQueriesEnum.ADMINCUSTOMERACTIVITY_READ);
        map.put(AuditActivityDTO.class, SQLQueriesEnum.AUDITLOG_ARCH_READ);
        map.put(MoneyMovementLogDTO.class, SQLQueriesEnum.MONEYMOVEMENT_READ);
        return map;
    }

    /**
     * Returns insert sql string for the provided concrete class of type {@link BaseActivity}
     * 
     * @param clazz
     * @return
     */
    public static <T extends BaseActivity> String getActivityInsertQuery(Class<T> clazz) {
        return LOG_INSERT_QUERIES_MAPPER.get(clazz).getQuery();
    }

    /**
     * Returns delete sql string for the provided concrete class of type {@link BaseActivity}
     * 
     * @param clazz
     * @return
     */
    public static <T extends BaseActivity> String getActivityDeleteQuery(Class<T> clazz) {
        return LOG_DELETE_QUERIES_MAPPER.get(clazz).getQuery();
    }

    /**
     * Returns select sql string for the provided concrete class of type {@link BaseActivity}
     * 
     * @param clazz
     * @return
     */
    public static <T extends BaseActivity> String getActivityReadQuery(Class<T> clazz) {
        return LOG_READ_QUERIES_MAPPER.get(clazz).getQuery();
    }

}
