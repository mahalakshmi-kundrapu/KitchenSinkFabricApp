package com.kony.logservices.service;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.JSONUtils;
import com.kony.logservices.core.AbstractLogJavaService;
import com.kony.logservices.dao.LogDAO;
import com.kony.logservices.dto.AuditLogsAndMoneyMovementLogsDTO;
import com.kony.logservices.dto.PaginationDTO;
import com.kony.logservices.dto.SearchCustomerAuditLogsDTO;
import com.kony.logservices.util.ErrorCodeEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

public class SearchCustomerAuditLogs extends AbstractLogJavaService {

    private static final Logger LOG = Logger.getLogger(SearchCustomerAuditLogs.class);
    private static final String INPUT_USERNAME = "username";
    private static final String INPUT_CUSTOMERID = "customerid";
    private static final String INPUT_IS_CSR_ASSIST = "isCSRAssist";
    private static final String INPUT_SEARCHTEXT = "searchText";
    private static final String INPUT_MODULE = "module";
    private static final String INPUT_ACTIVITY_TYPE = "activityType";
    private static final String INPUT_START_DATE = "startDate";
    private static final String INPUT_END_DATE = "endDate";
    private static final String INPUT_START_AMOUNT = "startAmount";
    private static final String INPUT_END_AMOUNT = "endAmount";
    private static final String INPUT_SORT_VARIABLE = "sortVariable";
    private static final String INPUT_SORT_DIRECTION = "sortDirection";
    private static final String INPUT_PAGESIZE = "pageSize";
    private static final String INPUT_PAGEOFFSET = "pageOffset";

    @Override
    public Object execute(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        try {
            String username = requestInstance.getParameter(INPUT_USERNAME);
            String customerid = requestInstance.getParameter(INPUT_CUSTOMERID);
            String searchText = requestInstance.getParameter(INPUT_SEARCHTEXT);
            String isCSRAssist = requestInstance.getParameter(INPUT_IS_CSR_ASSIST);
            String module = requestInstance.getParameter(INPUT_MODULE);
            String activityType = requestInstance.getParameter(INPUT_ACTIVITY_TYPE);
            String startDate = requestInstance.getParameter(INPUT_START_DATE);
            String endDate = requestInstance.getParameter(INPUT_END_DATE);
            String startAmount = requestInstance.getParameter(INPUT_START_AMOUNT);
            String endAmount = requestInstance.getParameter(INPUT_END_AMOUNT);
            String sortVariable = requestInstance.getParameter(INPUT_SORT_VARIABLE);
            String sortDirection = requestInstance.getParameter(INPUT_SORT_DIRECTION);
            String pageSize = requestInstance.getParameter(INPUT_PAGESIZE);
            String pageOffset = requestInstance.getParameter(INPUT_PAGEOFFSET);

            SearchCustomerAuditLogsDTO customerAuditLogsDTO = new SearchCustomerAuditLogsDTO();
            customerAuditLogsDTO.setCustomerid(customerid);
            customerAuditLogsDTO.setUsername(username);
            customerAuditLogsDTO.setIsCSRAssist(isCSRAssist);
            customerAuditLogsDTO.setSearchText(searchText);
            customerAuditLogsDTO.setModule(module);
            customerAuditLogsDTO.setActivityType(activityType);
            customerAuditLogsDTO.setStartDate(startDate);
            customerAuditLogsDTO.setEndDate(endDate);
            customerAuditLogsDTO.setStartAmount(startAmount);
            customerAuditLogsDTO.setEndAmount(endAmount);
            customerAuditLogsDTO.setPageSize(pageSize);
            customerAuditLogsDTO.setSortVariable(sortVariable);
            customerAuditLogsDTO.setSortDirection(sortDirection);
            customerAuditLogsDTO.setPageOffset(pageOffset);
            customerAuditLogsDTO.setSearchText(searchText);

            PaginationDTO<AuditLogsAndMoneyMovementLogsDTO> auditActivityLogs = LogDAO
                    .searchCustomerAuditLogs(customerAuditLogsDTO);

            String resultJSONString = JSONUtils.stringify(auditActivityLogs);
            JSONObject resultJSON = new JSONObject(resultJSONString);
            return CommonUtilities.constructResultFromJSONObject(resultJSON);
        } catch (Exception e) {
            LOG.error("Failed while executing search customer audit logs", e);
            ErrorCodeEnum.ERR_29010.setErrorCode(processedResult);
            return processedResult;
        }
    }
}
