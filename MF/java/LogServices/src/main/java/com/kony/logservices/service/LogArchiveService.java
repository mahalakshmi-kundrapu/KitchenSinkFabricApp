package com.kony.logservices.service;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import com.kony.logservices.core.AbstractLogJavaService;
import com.kony.logservices.core.BaseActivity;
import com.kony.logservices.dao.LogArchiveDAO;
import com.kony.logservices.dto.AdminActivityDTO;
import com.kony.logservices.dto.AdminCustomerActivityDTO;
import com.kony.logservices.dto.AuditActivityDTO;
import com.kony.logservices.dto.CustomerActivityDTO;
import com.kony.logservices.dto.MoneyMovementLogDTO;
import com.kony.logservices.dto.TransactionActivityDTO;
import com.kony.logservices.util.EnvironmentConfiguration;
import com.kony.logservices.util.ErrorCodeEnum;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

public class LogArchiveService extends AbstractLogJavaService {

    private static final Logger LOG = Logger.getLogger(LogArchiveService.class);

    private static final Map<String, Class<? extends BaseActivity>> METHODID_TO_LOGCLASS_MAPPER =
            mapMethodIdToLogClass();

    @Override
    public Object execute(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String logRetentionPeriodStr = EnvironmentConfiguration.LOG_RETENTION_PERIOD_IN_MONTHS
                .getValue(requestInstance);
        if (StringUtils.isBlank(logRetentionPeriodStr)) {
            LOG.error("Retention Period of Logs has not been configured. Cannot Archive logs");
            ErrorCodeEnum.ERR_29009.setErrorCode(result);
            return result;
        }

        try {
            Integer logRetentionPeriod = Integer.parseInt(logRetentionPeriodStr);
            Date archiveBefore = calculateArchivingDate(logRetentionPeriod);
            LogArchiveDAO.archiveLogs(METHODID_TO_LOGCLASS_MAPPER.get(methodID), archiveBefore);
            result.addParam(
                    new Param(FabricConstants.HTTP_STATUS_CODE, String.valueOf(HttpStatus.SC_OK), FabricConstants.INT));
        } catch (Exception e) {
            LOG.error("Exception in LogArchiveService", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
        }
        return result;
    }

    private static Date calculateArchivingDate(Integer logRetentionPeriod) {

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - logRetentionPeriod);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        return calendar.getTime();
    }

    private static Map<String, Class<? extends BaseActivity>> mapMethodIdToLogClass() {
        Map<String, Class<? extends BaseActivity>> map = new HashMap<>();
        map.put("archiveAdminActivityData", AdminActivityDTO.class);
        map.put("archiveAdminCustomerActivityData", AdminCustomerActivityDTO.class);
        map.put("archiveCustomerActivityData", CustomerActivityDTO.class);
        map.put("archiveTransactionLogData", TransactionActivityDTO.class);
        map.put("archiveAuditActivityData", AuditActivityDTO.class);
        map.put("archiveMoneyMovementLogData", MoneyMovementLogDTO.class);
        return map;
    }

}
