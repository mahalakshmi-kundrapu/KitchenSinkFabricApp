package com.kony.adminconsole.service.campaignmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
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
 * Service to update campaign settings at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class CampaignSettingsUpdateService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(CampaignSettingsUpdateService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

        try {
            updateCampaignSettings(methodID, requestInstance, userId, result);
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(result);
            LOG.error("ApplicationException occured in CampaignSettingsUpdateService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(result);
            LOG.error("Exception occured in CampaignSettingsUpdateService JAVA service. Error: ", e);
        }

        return result;
    }

    Result updateCampaignSettings(String methodID, DataControllerRequest requestInstance, String userId, Result result)
            throws ApplicationException {

        // ** Campaign settings **
        String fullScreenAd = requestInstance.getParameter("showFullScreenAd");
        if (StringUtils.isBlank(fullScreenAd)) {
            throw new ApplicationException(ErrorCodeEnum.ERR_21756);
        }
        if (!(fullScreenAd.equals("true") || fullScreenAd.equals("false"))) {
            throw new ApplicationException(ErrorCodeEnum.ERR_21757);
        }

        Map<String, String> campaignSettingsMap = new HashMap<>();
        campaignSettingsMap.put("id", "2");
        campaignSettingsMap.put("showAdsPostLogin", fullScreenAd.equals("true") ? "1" : "0");

        String response = Executor.invokeService(ServiceURLEnum.APPLICATION_UPDATE, campaignSettingsMap, null,
                requestInstance);
        JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

        if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
                && responseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Campaign settings update successful");
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Campaign settings update failed");
            throw new ApplicationException(ErrorCodeEnum.ERR_21753);
        }

        return result;
    }
}
