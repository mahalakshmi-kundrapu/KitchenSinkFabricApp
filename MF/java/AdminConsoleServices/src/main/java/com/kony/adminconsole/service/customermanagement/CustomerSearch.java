package com.kony.adminconsole.service.customermanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.dto.MemberSearchBean;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * CustomerSearch service will serach for a customer based on several search parameters
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class CustomerSearch implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(CustomerSearch.class);

    static final String APPLICANT_SEARCH = "APPLICANT_SEARCH";
    static final String APPLICANT_SEARCH_TOTAL_COUNT = "APPLICANT_SEARCH_TOTAL_COUNT";
    static final String GROUP_SEARCH = "GROUP_SEARCH";
    static final String GROUP_SEARCH_TOTAL_COUNT = "GROUP_SEARCH_TOTAL_COUNT";
    static final String CUSTOMER_SEARCH = "CUSTOMER_SEARCH";
    static final String CUSTOMER_SEARCH_TOTAL_COUNT = "CUSTOMER_SEARCH_TOTAL_COUNT";

    public static final String STATUS_SUCCESS = "Success";

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        try {

            // Sort validation
            if (StringUtils.isNotBlank(requestInstance.getParameter("_sortVariable"))
                    && requestInstance.getParameter("_sortVariable").contains(" ")) {
                ErrorCodeEnum.ERR_20541.setErrorCode(processedResult);
                return processedResult;
            }
            if (StringUtils.isNotBlank(requestInstance.getParameter("_sortDirection"))
                    && (requestInstance.getParameter("_sortDirection").contains(" ")
                            || (!requestInstance.getParameter("_sortDirection").equalsIgnoreCase("ASC")
                                    && !requestInstance.getParameter("_sortDirection").equalsIgnoreCase("DESC")))) {
                ErrorCodeEnum.ERR_20541.setErrorCode(processedResult);
                return processedResult;
            }
            MemberSearchBean memberSearchBean = new MemberSearchBean();
            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            memberSearchBean.setSearchType(requestInstance.getParameter("_searchType"));
            memberSearchBean.setMemberId(requestInstance.getParameter("_id"));
            memberSearchBean.setCustomerName(requestInstance.getParameter("_name"));
            memberSearchBean.setSsn(requestInstance.getParameter("_SSN"));
            memberSearchBean.setCustomerUsername(requestInstance.getParameter("_username"));
            memberSearchBean.setCustomerPhone(requestInstance.getParameter("_phone"));
            memberSearchBean.setCustomerEmail(requestInstance.getParameter("_email"));
            memberSearchBean.setIsStaffMember(requestInstance.getParameter("_IsStaffMember"));
            memberSearchBean.setCardorAccountnumber(requestInstance.getParameter("_cardorAccountnumber"));
            memberSearchBean.setTin(requestInstance.getParameter("_TIN"));
            memberSearchBean.setCustomerGroup(requestInstance.getParameter("_group"));
            memberSearchBean.setCustomerIDType(requestInstance.getParameter("_IDType"));
            memberSearchBean.setCustomerIDValue(requestInstance.getParameter("_IDValue"));
            memberSearchBean.setCustomerCompanyId(requestInstance.getParameter("_companyId"));
            memberSearchBean.setCustomerRequest(requestInstance.getParameter("_requestID"));
            memberSearchBean.setBranchIDS(requestInstance.getParameter("_branchIDS"));
            memberSearchBean.setProductIDS(requestInstance.getParameter("_productIDS"));
            memberSearchBean.setCityIDS(requestInstance.getParameter("_cityIDS"));
            memberSearchBean.setEntitlementIDS(requestInstance.getParameter("_entitlementIDS"));
            memberSearchBean.setGroupIDS(requestInstance.getParameter("_groupIDS"));
            memberSearchBean.setCustomerStatus(requestInstance.getParameter("_customerStatus"));
            memberSearchBean.setBeforeDate(requestInstance.getParameter("_before"));
            memberSearchBean.setAfterDate(requestInstance.getParameter("_after"));
            memberSearchBean.setSortVariable(requestInstance.getParameter("_sortVariable"));
            memberSearchBean.setSortDirection(requestInstance.getParameter("_sortDirection"));
            memberSearchBean.setPageOffset(requestInstance.getParameter("_pageOffset"));
            memberSearchBean.setPageSize(requestInstance.getParameter("_pageSize"));

            if (StringUtils.isBlank(memberSearchBean.getSearchType())) {
                ErrorCodeEnum.ERR_20690.setErrorCode(processedResult);
                return processedResult;

            }

            processedResult
                    .addParam(new Param("SortVariable", memberSearchBean.getSortVariable(), FabricConstants.STRING));
            processedResult
                    .addParam(new Param("SortDirection", memberSearchBean.getSortDirection(), FabricConstants.STRING));
            processedResult.addParam(
                    new Param("PageOffset", String.valueOf(memberSearchBean.getPageOffset()), FabricConstants.INT));
            processedResult.addParam(
                    new Param("PageSize", String.valueOf(memberSearchBean.getPageSize()), FabricConstants.INT));

            if (memberSearchBean.getSearchType().equalsIgnoreCase(APPLICANT_SEARCH)) {
                processedResult.addParam(new Param("TotalResultsFound", "0", FabricConstants.INT));
            }
            if (memberSearchBean.getSearchType().equalsIgnoreCase(GROUP_SEARCH)) {
                JSONObject searchResults = searchCustomers(authToken, GROUP_SEARCH_TOTAL_COUNT, memberSearchBean,
                        requestInstance);
                if (searchResults.has("records") && ((JSONArray) searchResults.get("records")).length() > 0) {
                    if (searchResults.getJSONArray("records").getJSONObject(0).has("SearchMatchs")) {
                        processedResult.addParam(new Param("TotalResultsFound",
                                searchResults.getJSONArray("records").getJSONObject(0).getString("SearchMatchs"),
                                FabricConstants.INT));
                    } else {
                        processedResult.addParam(new Param("TotalResultsFound", "0", FabricConstants.INT));
                    }
                }
            }
            if (memberSearchBean.getSearchType().equalsIgnoreCase(CUSTOMER_SEARCH)) {
                JSONObject searchResults = searchCustomers(authToken, CUSTOMER_SEARCH_TOTAL_COUNT, memberSearchBean,
                        requestInstance);
                if (searchResults.has("records") && ((JSONArray) searchResults.get("records")).length() > 0) {
                    if (searchResults.getJSONArray("records").getJSONObject(0).has("SearchMatchs")) {
                        processedResult.addParam(new Param("TotalResultsFound",
                                searchResults.getJSONArray("records").getJSONObject(0).getString("SearchMatchs"),
                                FabricConstants.INT));
                    } else {
                        processedResult.addParam(new Param("TotalResultsFound", "0", FabricConstants.INT));
                    }
                }
            }
            JSONObject customers = searchCustomers(authToken, memberSearchBean.getSearchType(), memberSearchBean,
                    requestInstance);

            if (customers.has("records")) {

                JSONArray recordsArray = customers.getJSONArray("records");
                Dataset recordsDataset = CommonUtilities.constructDatasetFromJSONArray(recordsArray);
                recordsDataset.setId("records");
                Param recordsStatus = new Param("Status", "Records returned: " + recordsArray.length(),
                        FabricConstants.STRING);
                processedResult.addDataset(recordsDataset);
                processedResult.addParam(recordsStatus);

                if (recordsArray.length() == 1 && memberSearchBean.getSearchType().equalsIgnoreCase(CUSTOMER_SEARCH)) {
                    CustomerHandler customerHandler = new CustomerHandler();
                    customerHandler.computeCustomerBasicInformation(requestInstance, processedResult,
                            recordsArray.getJSONObject(0).getString("id"),
                            recordsArray.getJSONObject(0).getString("Username"));
                }

            } else {
                ErrorCodeEnum.ERR_20716.setErrorCode(processedResult);
                return processedResult;

            }

            return processedResult;
        } catch (Exception e) {
            LOG.error("Unexpected error", e);
            processedResult.addParam(new Param("FailureReason", e.getMessage()));
            ErrorCodeEnum.ERR_20716.setErrorCode(processedResult);
            return processedResult;
        }
    }

    public static JSONObject searchCustomers(String authToken, String searchType, MemberSearchBean memberSearchBean,
            DataControllerRequest requestInstance) {

        Map<String, String> searchPostParameters = new HashMap<String, String>();
        searchPostParameters.put("_searchType", searchType);
        searchPostParameters.put("_id", memberSearchBean.getMemberId());
        searchPostParameters.put("_name", memberSearchBean.getCustomerName());
        searchPostParameters.put("_username", memberSearchBean.getCustomerUsername());
        searchPostParameters.put("_SSN", memberSearchBean.getSsn());
        searchPostParameters.put("_phone", memberSearchBean.getCustomerPhone());
        searchPostParameters.put("_email", memberSearchBean.getCustomerEmail());
        searchPostParameters.put("_IsStaffMember", memberSearchBean.getIsStaffMember());
        searchPostParameters.put("_cardorAccountnumber", memberSearchBean.getCardorAccountnumber());
        searchPostParameters.put("_TIN", memberSearchBean.getTin());
        searchPostParameters.put("_group", memberSearchBean.getCustomerGroup());
        searchPostParameters.put("_IDType", memberSearchBean.getCustomerIDType());
        searchPostParameters.put("_IDValue", memberSearchBean.getCustomerIDValue());
        searchPostParameters.put("_companyId", memberSearchBean.getCustomerCompanyId());
        searchPostParameters.put("_requestID", memberSearchBean.getCustomerRequest());
        searchPostParameters.put("_branchIDS", memberSearchBean.getBranchIDS());
        searchPostParameters.put("_productIDS", memberSearchBean.getProductIDS());
        searchPostParameters.put("_cityIDS", memberSearchBean.getCityIDS());
        searchPostParameters.put("_entitlementIDS", memberSearchBean.getEntitlementIDS());
        searchPostParameters.put("_groupIDS", memberSearchBean.getGroupIDS());
        searchPostParameters.put("_customerStatus", memberSearchBean.getCustomerStatus());
        searchPostParameters.put("_before", memberSearchBean.getBeforeDate());
        searchPostParameters.put("_after", memberSearchBean.getAfterDate());
        searchPostParameters.put("_sortVariable", memberSearchBean.getSortVariable());
        searchPostParameters.put("_sortDirection", memberSearchBean.getSortDirection());
        searchPostParameters.put("_pageOffset", String.valueOf(memberSearchBean.getPageOffset()));
        searchPostParameters.put("_pageSize", String.valueOf(memberSearchBean.getPageSize()));

        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_SEARCH_PROC_SERVICE,
                searchPostParameters, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

}
