/**
 * 
 */
package com.kony.adminconsole.service.customermanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.dto.ExportColumnDetailsBean;
import com.kony.adminconsole.dto.MemberSearchBean;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.ExcelGenerator;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * @author Sowmya Mortha
 *
 */
public class ExportCustomersService extends ExcelGenerator implements JavaService2 {

    private static final Logger logger = Logger.getLogger(ExportCustomersService.class);
    public static final String REPORT_TITLE = "Customer Search Results";

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {

            StringBuilder generatedBy = new StringBuilder();
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                generatedBy.append(StringUtils.defaultString(loggedInUserDetails.getFirstName())).append(" ");
                generatedBy.append(StringUtils.defaultString(loggedInUserDetails.getMiddleName())).append(" ");
                generatedBy.append(StringUtils.defaultString(loggedInUserDetails.getLastName()));
            }

            Map<String, String> queryParamsMap = (Map<String, String>) requestInstance.getAttribute("queryparams");
            String authToken = queryParamsMap.get("authToken");

            JSONArray custJSONArray = null;
            StringBuilder filters = new StringBuilder();

            String customerID = "";
            if (queryParamsMap.containsKey("_id")) {
                customerID = queryParamsMap.get("_id");
                filters.append("User Id contains \"").append(customerID).append("\"").append(" AND ");
            }
            String customerName = "";
            if (queryParamsMap.containsKey("_name")) {
                customerName = queryParamsMap.get("_name");
                filters.append("Name contains \"").append(customerName).append("\"").append(" AND ");

            }
            String customerUsername = "";
            if (queryParamsMap.containsKey("_username")) {
                customerUsername = queryParamsMap.get("_username");
                filters.append("Username contains \"").append(customerUsername).append("\"").append(" AND ");

            }
            String ssn = "";
            if (queryParamsMap.containsKey("_SSN")) {
                ssn = queryParamsMap.get("_SSN");
                filters.append("SSN contains \"").append(ssn).append("\"").append(" AND ");

            }
            String customerPhone = "";
            if (queryParamsMap.containsKey("_phone")) {
                customerPhone = queryParamsMap.get("_phone");
                filters.append("Contact Number contains \"").append(customerPhone).append("\"").append(" AND ");
            }
            String customerEmail = "";
            if (queryParamsMap.containsKey("_email")) {
                customerEmail = queryParamsMap.get("_email");
                filters.append("Email Id contains \"").append(customerEmail).append("\"").append(" AND ");
            }
            String searchType = "";
            if (queryParamsMap.containsKey("_searchType")) {
                searchType = queryParamsMap.get("_searchType");
                filters.append("Customer Search Type equals \"").append(searchType).append("\"").append(" AND ");
            }
            String customerGroup = "";
            if (queryParamsMap.containsKey("_group")) {
                customerGroup = queryParamsMap.get("_group");
                filters.append("Customer Classification equals \"").append(customerGroup).append("\"").append(" AND ");
            }
            String customerRequest = "";
            if (queryParamsMap.containsKey("_requestID")) {
                customerRequest = queryParamsMap.get("_requestID");
                filters.append("Request IDs contains \"").append(customerRequest).append("\"").append(" AND ");
            }
            String sortVariable = "DEFAULT";
            if (queryParamsMap.containsKey("_sortVariable")) {
                sortVariable = queryParamsMap.get("_sortVariable");
                filters.append("Sort variable equals \"").append(sortVariable).append("\"").append(" AND ");
            }
            String sortDirection = "ASC";
            if (queryParamsMap.containsKey("_sortDirection")) {
                sortDirection = queryParamsMap.get("_sortDirection");
                filters.append("Sort direction equals \"").append(sortDirection).append("\"").append(" AND ");
            }
            String pageOffset = "";
            if (queryParamsMap.containsKey("_pageOffset")) {
                pageOffset = queryParamsMap.get("_pageOffset");
                filters.append("Page offset contains \"").append(pageOffset).append("\"").append(" AND ");
            }
            String pageSize = "";
            if (queryParamsMap.containsKey("_pageSize")) {
                pageSize = queryParamsMap.get("_pageSize");
                filters.append("Page size contains \"").append(pageSize).append("\"").append(" AND ");
            }
            if (filters.length() > 0) {
                filters.delete(filters.lastIndexOf("AND"), filters.length());
            }
            Integer offset = queryParamsMap.containsKey("offset") ? Integer.parseInt(queryParamsMap.get("offset")) : 0;

            MemberSearchBean memberSearchBean = new MemberSearchBean();
            memberSearchBean.setSearchType(searchType);
            memberSearchBean.setMemberId(customerID);
            memberSearchBean.setCustomerName(customerName);
            memberSearchBean.setSsn(ssn);
            memberSearchBean.setCustomerUsername(customerUsername);
            memberSearchBean.setCustomerPhone(customerPhone);
            memberSearchBean.setCustomerEmail(customerEmail);
            memberSearchBean.setCustomerGroup(customerGroup);
            memberSearchBean.setCustomerRequest(customerRequest);
            memberSearchBean.setSortVariable(sortVariable);
            memberSearchBean.setSortDirection(sortDirection);
            memberSearchBean.setPageOffset(pageOffset);
            memberSearchBean.setPageSize(pageSize);
            JSONObject customers = CustomerSearch.searchCustomers(authToken, memberSearchBean.getSearchType(),
                    memberSearchBean, requestInstance);
            if (customers.has("records") && ((JSONArray) customers.get("records")).length() > 0) {
                custJSONArray = customers.getJSONArray("records");
            }
            // for header name
            boolean isApplicant = memberSearchBean.getSearchType().equalsIgnoreCase(CustomerSearch.APPLICANT_SEARCH);
            List<ExportColumnDetailsBean> fieldList = new ArrayList<ExportColumnDetailsBean>();
            if (isApplicant) {
                fieldList.add(new ExportColumnDetailsBean(0, "", "FirstName"));
                fieldList.add(new ExportColumnDetailsBean(0, "NAME", "LastName"));
                fieldList.add(new ExportColumnDetailsBean(1, "APPLICATION ID", "id"));
                fieldList.add(new ExportColumnDetailsBean(2, "CONTACT NUMBER", "ContactNumber"));
                fieldList.add(new ExportColumnDetailsBean(3, "EMAIL ID", "EmailID"));
            } else {
                fieldList.add(new ExportColumnDetailsBean(1, "", "Salutation"));
                fieldList.add(new ExportColumnDetailsBean(1, "NAME", "name"));
                fieldList.add(new ExportColumnDetailsBean(2, "USER ID", "id"));
                fieldList.add(new ExportColumnDetailsBean(3, "USER NAME", "Username"));
                fieldList.add(new ExportColumnDetailsBean(4, "CONTACT NUMBER", "PrimaryPhoneNumber"));
                fieldList.add(new ExportColumnDetailsBean(5, "EMAIL ID", "PrimaryEmailAddress"));
                fieldList.add(new ExportColumnDetailsBean(6, "SSN", "Ssn"));
            }
            fieldList.sort(null);
            // write excel
            byte[] bytes = generateExcel(custJSONArray, REPORT_TITLE, generatedBy.toString(), offset, fieldList,
                    filters.toString());
            Map<String, String> customHeaders = new HashMap<String, String>();
            customHeaders.put("Content-Type", "application/vnd.ms-excel");
            customHeaders.put("Content-Disposition", "attachment; filename=\"customer.xlsx\"");
            responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON,
                    new BufferedHttpEntity(new ByteArrayEntity(bytes)));
            responseInstance.setStatusCode(HttpStatus.SC_OK);
            responseInstance.getHeaders().putAll(customHeaders);
        } catch (Exception e) {
            logger.error("Failed while exporting customer records", e);
            ErrorCodeEnum.ERR_20687.setErrorCode(result);

            String errorMessage = "Failed to export customer records. Please contact administrator.";
            CommonUtilities.fileDownloadFailure(responseInstance, errorMessage);
        }
        return result;
    }
}