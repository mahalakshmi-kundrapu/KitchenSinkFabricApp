package com.kony.adminconsole.service.identitymanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to fetch policies (username/password) from the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class PolicyFetchService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(PolicyFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();

        try {
            String acceptHeaderValue = requestInstance.getHeader("Accept-Language");
            if (acceptHeaderValue != null && acceptHeaderValue.contains("_")) {
                acceptHeaderValue = acceptHeaderValue.replace('_', '-');
            }

            boolean isCustomer = true;

            String usernamePolicyForCustomer = requestInstance.getParameter("usernamePolicyForCustomer");
            String passwordPolicyForCustomer = requestInstance.getParameter("passwordPolicyForCustomer");
            if ((usernamePolicyForCustomer != null && usernamePolicyForCustomer.equals("false"))
                    || (passwordPolicyForCustomer != null && passwordPolicyForCustomer.equals("false"))) {
                isCustomer = false;
            }

            String usernamePolicyForAllLocales = requestInstance.getParameter("usernamePolicyForAllLocales");
            String passwordPolicyForAllLocales = requestInstance.getParameter("passwordPolicyForAllLocales");
            if (usernamePolicyForAllLocales != null
                    && !(usernamePolicyForAllLocales.equals("true") || usernamePolicyForAllLocales.equals("false"))) {
                throw new ApplicationException(ErrorCodeEnum.ERR_21105);
            }
            if (passwordPolicyForAllLocales != null
                    && !(passwordPolicyForAllLocales.equals("true") || passwordPolicyForAllLocales.equals("false"))) {
                throw new ApplicationException(ErrorCodeEnum.ERR_21125);
            }

            String typeId = StringUtils.EMPTY;
            if (methodID.contains("Username")) {
                typeId = isCustomer == true ? "PT_CUST_USERNAME" : "PT_USER_USERNAME";
            } else {
                typeId = isCustomer == true ? "PT_CUST_PASSWORD" : "PT_USER_PASSWORD";
            }

            // ** Reading from 'policy_view' view **
            Map<String, String> policyViewMap = new HashMap<>();

            if ((usernamePolicyForAllLocales != null && usernamePolicyForAllLocales.equals("true"))
                    || (passwordPolicyForAllLocales != null && passwordPolicyForAllLocales.equals("true"))) {
                // -> All policies <-
                policyViewMap.put(ODataQueryConstants.FILTER, "Type_id eq '" + typeId + "'");
            } else if (acceptHeaderValue != null) { // -> Policy in user locale & 'en-US' <-
                policyViewMap.put(ODataQueryConstants.FILTER,
                        "Type_id eq '" + typeId + "' and (Locale eq '" + acceptHeaderValue + "' or Locale eq 'en-US')");
            } else { // -> Policy in 'en-US' <-
                policyViewMap.put(ODataQueryConstants.FILTER, "Type_id eq '" + typeId + "' and Locale eq 'en-US'");
            }

            String readPolicyResponse = Executor.invokeService(ServiceURLEnum.POLICY_VIEW_READ, policyViewMap, null,
                    requestInstance);
            JSONObject readPolicyResponseJSON = CommonUtilities.getStringAsJSONObject(readPolicyResponse);

            if (readPolicyResponseJSON != null && readPolicyResponseJSON.has(FabricConstants.OPSTATUS)
                    && readPolicyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readPolicyResponseJSON.has("policy_view")) {

                JSONArray readPolicyResponseJSONArray = readPolicyResponseJSON.getJSONArray("policy_view");

                if ((usernamePolicyForAllLocales != null && usernamePolicyForAllLocales.equals("true"))
                        || (passwordPolicyForAllLocales != null && passwordPolicyForAllLocales.equals("true"))) {

                    // -> Returning policies in all locales <-
                    Dataset policyDataset = new Dataset();
                    if (methodID.contains("Username")) {
                        policyDataset.setId("usernamepolicy");
                    } else {
                        policyDataset.setId("passwordpolicy");
                    }

                    for (int i = 0; i < readPolicyResponseJSONArray.length(); ++i) {

                        JSONObject policyResponse = readPolicyResponseJSONArray.getJSONObject(i);
                        Record policyRecord = new Record();

                        if (StringUtils.isNotBlank(policyResponse.optString("Locale"))) {
                            policyRecord.addParam(
                                    new Param("locale", policyResponse.getString("Locale"), FabricConstants.STRING));
                        } else {
                            throw new ApplicationException(
                                    methodID.contains("Username") ? ErrorCodeEnum.ERR_21101 : ErrorCodeEnum.ERR_21121);
                        }

                        if (StringUtils.isNotBlank(policyResponse.optString("PolicyContent"))) {
                            policyRecord.addParam(new Param("content", policyResponse.getString("PolicyContent"),
                                    FabricConstants.STRING));
                        } else {
                            throw new ApplicationException(
                                    methodID.contains("Username") ? ErrorCodeEnum.ERR_21101 : ErrorCodeEnum.ERR_21121);
                        }

                        policyDataset.addRecord(policyRecord);
                    }

                    result.addDataset(policyDataset);
                } else {

                    // -> Returning policies in only 1 locale <-
                    Record policyRecord = new Record();
                    if (methodID.contains("Username")) {
                        policyRecord.setId("usernamepolicy");
                    } else {
                        policyRecord.setId("passwordpolicy");
                    }

                    if (readPolicyResponseJSONArray.length() > 0) {

                        JSONObject policyResponse = null;
                        if (readPolicyResponseJSONArray.length() == 1) {
                            // -> Policy response contains entries in only 1 locale (either user locale or
                            // 'en-US') <-
                            policyResponse = readPolicyResponseJSONArray.getJSONObject(0);
                        } else if (readPolicyResponseJSONArray.length() == 2) {
                            // -> Policy response contains entries in 2 locales (user locale and 'en-US') <-
                            // -> We need to return policy to client in only 1 locale, with preference to
                            // user locale

                            if (StringUtils.isNotBlank(readPolicyResponseJSONArray.getJSONObject(1).optString("Locale"))
                                    && readPolicyResponseJSONArray.getJSONObject(1).getString("Locale")
                                            .equals("en-US")) {
                                policyResponse = readPolicyResponseJSONArray.getJSONObject(0);
                            } else if (StringUtils
                                    .isNotBlank(readPolicyResponseJSONArray.getJSONObject(0).optString("Locale"))
                                    && readPolicyResponseJSONArray.getJSONObject(0).getString("Locale")
                                            .equals("en-US")) {
                                policyResponse = readPolicyResponseJSONArray.getJSONObject(1);
                            } else {
                                throw new ApplicationException(methodID.contains("Username") ? ErrorCodeEnum.ERR_21101
                                        : ErrorCodeEnum.ERR_21121);
                            }
                        } else {
                            throw new ApplicationException(
                                    methodID.contains("Username") ? ErrorCodeEnum.ERR_21101 : ErrorCodeEnum.ERR_21121);
                        }

                        if (StringUtils.isNotBlank(policyResponse.optString("Locale"))) {
                            policyRecord.addParam(
                                    new Param("locale", policyResponse.getString("Locale"), FabricConstants.STRING));
                        } else {
                            throw new ApplicationException(
                                    methodID.contains("Username") ? ErrorCodeEnum.ERR_21101 : ErrorCodeEnum.ERR_21121);
                        }

                        if (StringUtils.isNotBlank(policyResponse.optString("PolicyContent"))) {
                            policyRecord.addParam(new Param("content", policyResponse.getString("PolicyContent"),
                                    FabricConstants.STRING));
                        } else {
                            throw new ApplicationException(
                                    methodID.contains("Username") ? ErrorCodeEnum.ERR_21101 : ErrorCodeEnum.ERR_21121);
                        }
                    }

                    result.addRecord(policyRecord);
                }

            } else {
                throw new ApplicationException(
                        methodID.contains("Username") ? ErrorCodeEnum.ERR_21101 : ErrorCodeEnum.ERR_21121);
            }
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in PolicyFetchService. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in PolicyFetchService. Error: ", e);
        }

        return result;
    }
}
