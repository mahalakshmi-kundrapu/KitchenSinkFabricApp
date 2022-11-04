package com.kony.adminconsole.service.campaignmanagement;

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
 * Service to delete campaign role
 * 
 * @author Sowmya Mortha (KH2256)
 * 
 */
public class CampaignGroupDeleteService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CampaignGroupDeleteService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        Result result = new Result();
        try {
            String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();
            isAssocaitedToOtherCampaigns(requestInstance);
            deleteCustomers(requestInstance);
            deleteRole(requestInstance, userId, result);

        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in CampaignGroupDeleteService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in CampaignGroupDeleteService JAVA service. Error: ", e);
        }

        return result;
    }

    /**
     * This method is to check if the role has been associated to other campaigns
     * 
     * @throws ApplicationException
     */
    private void isAssocaitedToOtherCampaigns(DataControllerRequest requestInstance)
            throws ApplicationException {
        // check for valid role id
        String groupId = requestInstance.getParameter("id");
        if (StringUtils.isBlank(groupId)) {
            throw new ApplicationException(ErrorCodeEnum.ERR_21793);
        }
        // check if role has been assigned to any other campaign
        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "group_id eq '" + groupId + "'");
        postParametersMap.put(ODataQueryConstants.SELECT, "campaign_id");

        String readCampaignGroupResponse = Executor.invokeService(ServiceURLEnum.CAMPAIGNGROUP_READ,
                postParametersMap, null, requestInstance);
        JSONObject readCampaignGroupResponseJSON = CommonUtilities.getStringAsJSONObject(readCampaignGroupResponse);

        if (readCampaignGroupResponseJSON != null && readCampaignGroupResponseJSON.has(FabricConstants.OPSTATUS)
                && readCampaignGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readCampaignGroupResponseJSON.has("campaigngroup")) {
            JSONArray readCampaignGroupJSONArray = readCampaignGroupResponseJSON.optJSONArray("campaigngroup");
            if (readCampaignGroupJSONArray != null && readCampaignGroupJSONArray.length() != 0) {
                throw new ApplicationException(ErrorCodeEnum.ERR_21794);
            }
        }
    }

    /**
     * This method is to delete all the customer associated with the group
     * 
     */
    private void deleteCustomers(DataControllerRequest requestInstance) throws ApplicationException {
        String groupId = requestInstance.getParameter("id");
        Map<String, String> postParametersMap = new HashMap<>();
        postParametersMap.put("_groupId", groupId);

        String response = Executor.invokeService(ServiceURLEnum.CAMPAIGN_C360_CUSTOMERGROUP_DELETE_PROC,
                postParametersMap, null, requestInstance);
        JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

        if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
                && responseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            // need to add scheduler here as module name
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "customers for the group are delete successfully by scheduler" + groupId);
        } else {
            // need to add scheduler here as module name
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
                    ActivityStatusEnum.FAILED, "customers for the group are delete failed by scheduler" + groupId);
            throw new ApplicationException(ErrorCodeEnum.ERR_21795);
        }
    }

    /**
     * This method is to delete role
     */
    private Result deleteRole(DataControllerRequest requestInstance, String userId, Result result) {

        String groupId = requestInstance.getParameter("id");

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("id", groupId);

        String deleteMemberGroupResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_DELETE,
                postParametersMap, null, requestInstance);
        JSONObject deleteMemberGroupResponseJSON = CommonUtilities.getStringAsJSONObject(deleteMemberGroupResponse);

        if (deleteMemberGroupResponseJSON.has(FabricConstants.OPSTATUS)
                && deleteMemberGroupResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            if (deleteMemberGroupResponseJSON.has("deletedRecords")
                    && deleteMemberGroupResponseJSON.getInt("deletedRecords") >= 1) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
                        ActivityStatusEnum.SUCCESSFUL,
                        "Campaign Member Group delete successfull. groupId: " + groupId);
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
                        ActivityStatusEnum.FAILED,
                        "Campaign Member Group delete failed. groupId: " + groupId);
                ErrorCodeEnum.ERR_21784.setErrorCode(result);
            }
            return result;
        }
        // need to confirm if this should be replaced to campaign group
        AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
                ActivityStatusEnum.FAILED, "Campaign Member Group delete failed. groupId: " + groupId);
        ErrorCodeEnum.ERR_21784.setErrorCode(result);

        return result;
    }
}
