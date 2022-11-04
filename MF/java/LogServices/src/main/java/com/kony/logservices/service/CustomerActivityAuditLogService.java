/**
 * 
 */
package com.kony.logservices.service;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.JSONUtils;
import com.kony.logservices.core.AbstractLogJavaService;
import com.kony.logservices.dao.LogDAO;
import com.kony.logservices.dto.AdminCustomerActivityDTO;
import com.kony.logservices.dto.CustomerActivityDTO;
import com.kony.logservices.dto.PaginationDTO;
import com.kony.logservices.util.ErrorCodeEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author Sowmya Mortha
 *
 */
public class CustomerActivityAuditLogService extends AbstractLogJavaService {

    private static final Logger logger = Logger.getLogger(CustomerActivityAuditLogService.class);

    @Override
    public Object execute(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            JSONObject filterDataJSON = CommonUtilities
                    .getStringAsJSONObject(requestInstance.getParameter("FilterData"));
            String userName = filterDataJSON.optString("userName");
            String userId = filterDataJSON.optString("id");
            String moduleName = filterDataJSON.optString("moduleName");
            String activityType = filterDataJSON.optString("activityType");
            String startDate = filterDataJSON.optString("startDate");
            String endDate = filterDataJSON.optString("endDate");
            String searchText = filterDataJSON.optString("searchText");
            ArrayList<String> statusTypeArray = null;
            ArrayList<String> channelTypeArray = null;
            ArrayList<String> osTypeArray = null;
            ArrayList<String> roleTypeArray = null;
            int pageNumber = 1;
            int noOfRecords = 10;
            String sortDirection = null;
            String resultJSON = null;

            Boolean isMemberActivity = true;
            if (StringUtils.isNotBlank(filterDataJSON.optString("isMemberActivity"))) {
                isMemberActivity = Boolean.parseBoolean(filterDataJSON.optString("isMemberActivity"));
            }
            if (StringUtils.isBlank(userName) && isMemberActivity) {
                ErrorCodeEnum.ERR_29005.setErrorCode(result);
                return result;
            } else if (StringUtils.isBlank(userId) && !isMemberActivity) {
                ErrorCodeEnum.ERR_29005.setErrorCode(result);
                return result;
            }

            if (StringUtils.isNotBlank(filterDataJSON.optString("pageNumber"))) {
                pageNumber = Integer.parseInt(filterDataJSON.optString("pageNumber"));
            }

            if (StringUtils.isNotBlank(filterDataJSON.optString("noOfRecords"))) {
                noOfRecords = Integer.parseInt(filterDataJSON.optString("noOfRecords"));
            }

            if (StringUtils.isNotBlank(filterDataJSON.optString("sortDirection"))) {
                sortDirection = filterDataJSON.optString("sortDirection");
            }

            if (StringUtils.isNotBlank(filterDataJSON.optString("status"))) {
                JSONArray statusJSONArray = CommonUtilities.getStringAsJSONArray(filterDataJSON.optString("status"));
                statusTypeArray = new ArrayList<String>(statusJSONArray.length());
                for (int i = 0; i < statusJSONArray.length(); i++) {
                    statusTypeArray.add(statusJSONArray.getString(i));
                }
            }

            if (StringUtils.isNotBlank(filterDataJSON.optString("chanel"))) {
                JSONArray chanelJSONArray = CommonUtilities.getStringAsJSONArray(filterDataJSON.optString("chanel"));
                channelTypeArray = new ArrayList<String>(chanelJSONArray.length());
                for (int i = 0; i < chanelJSONArray.length(); i++) {
                    channelTypeArray.add(chanelJSONArray.getString(i));
                }
            }

            if (StringUtils.isNotBlank(filterDataJSON.optString("os"))) {
                JSONArray osJSONArray = CommonUtilities.getStringAsJSONArray(filterDataJSON.optString("os"));
                osTypeArray = new ArrayList<String>(osJSONArray.length());
                for (int i = 0; i < osJSONArray.length(); i++) {
                    osTypeArray.add(osJSONArray.getString(i));
                }
            }

            if (StringUtils.isNotBlank(filterDataJSON.optString("role"))) {
                JSONArray roleJSONArray = CommonUtilities.getStringAsJSONArray(filterDataJSON.optString("role"));
                roleTypeArray = new ArrayList<String>(roleJSONArray.length());
                for (int i = 0; i < roleJSONArray.length(); i++) {
                    roleTypeArray.add(roleJSONArray.getString(i));
                }
            }

            if (isMemberActivity) {
                PaginationDTO<CustomerActivityDTO> paginatedTransactions = LogDAO.fetchCustomerActivityLogs(userName,
                        moduleName, activityType, startDate, endDate, searchText, statusTypeArray, channelTypeArray,
                        osTypeArray, null, pageNumber, noOfRecords, sortDirection);
                resultJSON = JSONUtils.stringify(paginatedTransactions);
            } else {
                PaginationDTO<AdminCustomerActivityDTO> paginatedTransactions = LogDAO.fetchAdminCustomerActivityLogs(
                        userId, moduleName, activityType, startDate, endDate, searchText, null, null, null,
                        roleTypeArray, pageNumber, noOfRecords, sortDirection);
                resultJSON = JSONUtils.stringify(paginatedTransactions);
            }

            JSONObject resultJSONObject = new JSONObject(resultJSON);
            return CommonUtilities.constructResultFromJSONObject(resultJSONObject);

        } catch (Exception e) {
            logger.error("Failed while executing get customer activity logs", e);
            ErrorCodeEnum.ERR_29004.setErrorCode(result);
        }
        return result;
    }
}
