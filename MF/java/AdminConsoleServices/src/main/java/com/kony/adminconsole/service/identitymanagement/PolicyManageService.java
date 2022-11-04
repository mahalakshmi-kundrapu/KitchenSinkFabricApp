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
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.dto.PolicyBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to create or update username/password policy at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class PolicyManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(PolicyFetchService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

        try {

            PolicyBean policyBean = new PolicyBean();

            if (methodID.contains("create")) {
                policyBean.setCreatePolicy(true);
            } else if (methodID.contains("update")) {
                policyBean.setUpdatePolicy(true);
            } else if (methodID.contains("delete")) {
                policyBean.setDeletePolicy(true);
            }

            String usernamePolicyForCustomer = requestInstance.getParameter("usernamePolicyForCustomer");
            String passwordPolicyForCustomer = requestInstance.getParameter("passwordPolicyForCustomer");
            if ((usernamePolicyForCustomer != null && usernamePolicyForCustomer.equals("false"))
                    || (passwordPolicyForCustomer != null && passwordPolicyForCustomer.equals("false"))) {
                policyBean.setPolicyForCustomer(false);
            }

            if (policyBean.isPolicyForCustomer()) {
                policyBean.setTypeId(methodID.contains("Username") ? "PT_CUST_USERNAME" : "PT_CUST_PASSWORD");
            } else {
                policyBean.setTypeId(methodID.contains("Username") ? "PT_USER_USERNAME" : "PT_USER_PASSWORD");
            }

            String localeCode = requestInstance.getParameter("localeCode");
            if (localeCode == null || localeCode.equals("")) {
                ErrorCodeEnum.ERR_21109.setErrorCode(result);
                return result;
            } else {
                policyBean.setLocaleCode(localeCode);
            }

            policyBean = fetchPolicy(policyBean, methodID, requestInstance, userId);
            if (policyBean.getPolicyId() == null) {
                if (policyBean.isCreatePolicy() || policyBean.isUpdatePolicy()) {
                    policyBean.setCreatePolicy(true);
                    policyBean.setUpdatePolicy(false);
                    policyBean.getCreateOrUpdatePolicyMap().put("id", CommonUtilities.getNewId().toString());
                    policyBean.getCreateOrUpdatePolicyMap().put("createdby", userId);

                    result = createOrUpdatePolicy(result, policyBean, methodID, requestInstance, userId);
                } else {
                    throw new ApplicationException(getErrorCode(methodID));
                }
            } else {
                if (policyBean.isCreatePolicy() || policyBean.isUpdatePolicy()) {
                    policyBean.setCreatePolicy(false);
                    policyBean.setUpdatePolicy(true);
                    policyBean.getCreateOrUpdatePolicyMap().put("id", policyBean.getPolicyId());
                    policyBean.getCreateOrUpdatePolicyMap().put("modifiedby", userId);

                    result = createOrUpdatePolicy(result, policyBean, methodID, requestInstance, userId);
                } else if (policyBean.isDeletePolicy()) {
                    policyBean.getCreateOrUpdatePolicyMap().put("id", policyBean.getPolicyId());

                    result = deletePolicy(result, policyBean, methodID, requestInstance, userId);
                } else {
                    throw new ApplicationException(getErrorCode(methodID));
                }
            }

        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in PolicyManageService. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in PolicyManageService. Error: ", e);
        }

        return result;
    }

    public PolicyBean fetchPolicy(PolicyBean policyBean, String methodID, DataControllerRequest requestInstance,
            String userId) throws ApplicationException {

        // ** Reading from 'policy_view' view **
        Map<String, String> readPolicyViewMap = new HashMap<>();
        readPolicyViewMap.put(ODataQueryConstants.FILTER,
                "Type_id eq '" + policyBean.getTypeId() + "' and Locale eq '" + policyBean.getLocaleCode() + "'");

        String readPolicyResponse = Executor.invokeService(ServiceURLEnum.POLICY_VIEW_READ, readPolicyViewMap, null,
                requestInstance);
        JSONObject readPolicyResponseJSON = CommonUtilities.getStringAsJSONObject(readPolicyResponse);

        if (readPolicyResponseJSON != null && readPolicyResponseJSON.has(FabricConstants.OPSTATUS)
                && readPolicyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readPolicyResponseJSON.has("policy_view")) {

            JSONArray readPolicyResponseJSONArray = readPolicyResponseJSON.getJSONArray("policy_view");
            if (readPolicyResponseJSONArray.length() == 1) {

                JSONObject policyResponse = readPolicyResponseJSONArray.getJSONObject(0);
                if (StringUtils.isNotBlank(policyResponse.optString("id"))) {
                    policyBean.setPolicyId(policyResponse.getString("id"));
                } else {
                    throw new ApplicationException(getErrorCode(methodID));
                }
            }
        }

        return policyBean;
    }

    public Result createOrUpdatePolicy(Result result, PolicyBean policyBean, String methodID,
            DataControllerRequest requestInstance, String userId) throws ApplicationException {

        String policyDescription = requestInstance.getParameter("policyDescription");
        if (policyDescription == null || policyDescription.equals("")) {
            ErrorCodeEnum.ERR_21110.setErrorCode(result);
            return result;
        } else {
            policyBean.setPolicyDescription(policyDescription);
        }

        policyBean.getCreateOrUpdatePolicyMap().put("Type_id", policyBean.getTypeId());
        policyBean.getCreateOrUpdatePolicyMap().put("Locale_Code", policyBean.getLocaleCode());
        policyBean.getCreateOrUpdatePolicyMap().put("Content", policyBean.getPolicyDescription());
        // Policy description will be received in base64-encoded form from client.
        policyBean.getCreateOrUpdatePolicyMap().put("rtx", "Content");
        // This parameter is used in 'CrudLayerPreProcessor' to base64-decode the
        // encoded policy description.
        // Value of the 'rtx' parameter is base64-decoded to obtain the description.

        // ** Creating new policy entry / updating existing policy entry in
        // 'policycontent' table **
        String createOrUpdatePolicyResponse = null;
        if (policyBean.isCreatePolicy()) {
            createOrUpdatePolicyResponse = Executor.invokeService(ServiceURLEnum.POLICYCONTENT_CREATE,
                    policyBean.getCreateOrUpdatePolicyMap(), null, requestInstance);
        } else {
            createOrUpdatePolicyResponse = Executor.invokeService(ServiceURLEnum.POLICYCONTENT_UPDATE,
                    policyBean.getCreateOrUpdatePolicyMap(), null, requestInstance);
        }

        JSONObject createOrUpdatePolicyResponseJSON = CommonUtilities
                .getStringAsJSONObject(createOrUpdatePolicyResponse);
        if (createOrUpdatePolicyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            if (policyBean.isCreatePolicy()) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.CREATE,
                        ActivityStatusEnum.SUCCESSFUL, "Policy create successful");
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.UPDATE,
                        ActivityStatusEnum.SUCCESSFUL, "Policy update successful");
            }
        } else {
            getErrorCode(methodID).setErrorCode(result);
            if (policyBean.isCreatePolicy()) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "Policy create failed");
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.UPDATE,
                        ActivityStatusEnum.FAILED, "Policy update failed");
            }
        }

        return result;
    }

    public Result deletePolicy(Result result, PolicyBean policyBean, String methodID,
            DataControllerRequest requestInstance, String userId) throws ApplicationException {

        // ** Deleting policy entry in 'policycontent' table **
        String deletePolicyResponse = null;
        deletePolicyResponse = Executor.invokeService(ServiceURLEnum.POLICYCONTENT_DELETE,
                policyBean.getCreateOrUpdatePolicyMap(), null, requestInstance);

        JSONObject deletePolicyResponseJSON = CommonUtilities.getStringAsJSONObject(deletePolicyResponse);
        if (deletePolicyResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.DELETE,
                    ActivityStatusEnum.SUCCESSFUL, "Policy delete successful");
        } else {
            getErrorCode(methodID).setErrorCode(result);
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CREDENTIALPOLICIES, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "Policy delete failed");
        }

        return result;
    }

    public ErrorCodeEnum getErrorCode(String methodID) {
        if (methodID.contains("create")) {
            return methodID.contains("Username") ? ErrorCodeEnum.ERR_21102 : ErrorCodeEnum.ERR_21122;
        } else if (methodID.contains("update")) {
            return methodID.contains("Username") ? ErrorCodeEnum.ERR_21103 : ErrorCodeEnum.ERR_21123;
        } else {
            return methodID.contains("Username") ? ErrorCodeEnum.ERR_21104 : ErrorCodeEnum.ERR_21124;
        }
    }
}
