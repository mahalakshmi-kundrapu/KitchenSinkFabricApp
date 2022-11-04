package com.kony.adminconsole.service.campaignmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.dto.DefaultCampaignSpecificationBean;
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
 * Service to update default campaign details (description & specifications) at the back end
 * 
 * @author Mohit Khosla (KH2356)
 */

public class DefaultCampaignUpdateService implements JavaService2 {

	private static final Logger LOG = Logger.getLogger(DefaultCampaignUpdateService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result result = new Result();
		String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

		try {
			updateDefaultCampaign(methodID, requestInstance, userId, result);
		}
		catch (ApplicationException ae) {
			ae.getErrorCodeEnum().setErrorCode(result);
			LOG.error("ApplicationException occured in DefaultCampaignUpdateService JAVA service. Error: ", ae);
		}
		catch (Exception e) {
			ErrorCodeEnum.ERR_20001.setErrorCode(result);
			LOG.error("Exception occured in DefaultCampaignUpdateService JAVA service. Error: ", e);
		}

		return result;
	}

	Result updateDefaultCampaign(String methodID, DataControllerRequest requestInstance, String userId, Result result)
			throws ApplicationException {

		// -> -> DEFAULTCAMPAIGN table fields <- <-

		// ** Campaign description **
		String description = requestInstance.getParameter("description");
		if (!StringUtils.isBlank(description) && description.length() <= 150) {

			Map<String, String> campaignMap = new HashMap<>();
			campaignMap.put("name", "Default Ad Campaign");
			campaignMap.put("description", description);
			campaignMap.put("modifiedby", userId);

			String updateDefaultCampaignResponse = Executor.invokeService(ServiceURLEnum.DEFAULTCAMPAIGN_UPDATE,
					campaignMap, null, requestInstance);
			JSONObject createCampaignResponseJSON = CommonUtilities
					.getStringAsJSONObject(updateDefaultCampaignResponse);

			if (createCampaignResponseJSON != null && createCampaignResponseJSON.has(FabricConstants.OPSTATUS)
					&& createCampaignResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
						ActivityStatusEnum.SUCCESSFUL, "Default campaign update successful");
			}
			else {
				AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
						ActivityStatusEnum.FAILED, "Default campaign update failed");
				throw new ApplicationException(ErrorCodeEnum.ERR_21747);
			}
		}
		else if (!StringUtils.isBlank(description) && description.length() > 150) {
			throw new ApplicationException(ErrorCodeEnum.ERR_21732);
		}

		// -> -> DEFAULTCAMPAIGNSPECIFICATION table records <- <-
		if (requestInstance.getParameter("specifications") != null) {

			Map<String, DefaultCampaignSpecificationBean> defaultCampaignSpecifications = getDefaultCampaignSpecifications(requestInstance);
			JSONArray specifications = new JSONArray(requestInstance.getParameter("specifications"));

			for (int i = 0; i < specifications.length(); ++i) {

				JSONObject specification = specifications.getJSONObject(i);

				// ** Channel ID **
				String channelId = specification.optString("channelId");
				if (StringUtils.isBlank(channelId)) {
					throw new ApplicationException(ErrorCodeEnum.ERR_21725);
				}

				// ** Screen ID **
				String screenId = specification.optString("screenId");
				if (StringUtils.isBlank(screenId)) {
					throw new ApplicationException(ErrorCodeEnum.ERR_21726);
				}

				// ** Image index **
				String imageIndex = specification.optString("imageIndex");
				if (StringUtils.isBlank(imageIndex)) {
					throw new ApplicationException(ErrorCodeEnum.ERR_21735);
				}

				// ** Image resolution **
				String imageResolution = specification.optString("imageResolution");
				if (StringUtils.isBlank(imageResolution)) {
					throw new ApplicationException(ErrorCodeEnum.ERR_21728);
				}

				// ** Image URL **
				String imageURL = specification.optString("imageURL");
				if (StringUtils.isBlank(imageURL)) {
					throw new ApplicationException(ErrorCodeEnum.ERR_21727);
				}

				// ** Destination URL **
				String destinationURL = specification.optString("destinationURL");

				String defaultCampaignSpecificationsKey = channelId + "_" + screenId + "_" + imageResolution + "_"
						+ imageIndex;
				if (defaultCampaignSpecifications.containsKey(defaultCampaignSpecificationsKey)) {
					defaultCampaignSpecifications.remove(defaultCampaignSpecificationsKey);
				}

				Map<String, String> defaultCampaignSpecificationProcMap = new HashMap<>();
				defaultCampaignSpecificationProcMap.put("_channel", channelId);
				defaultCampaignSpecificationProcMap.put("_screen", screenId);
				defaultCampaignSpecificationProcMap.put("_imageIndex", imageIndex);
				defaultCampaignSpecificationProcMap.put("_imageResolution", imageResolution);
				defaultCampaignSpecificationProcMap.put("_imageURL", imageURL);
				defaultCampaignSpecificationProcMap.put("_destinationURL",
						!StringUtils.isBlank(destinationURL) ? destinationURL : "");
				defaultCampaignSpecificationProcMap.put("_createdOrModifiedBy", userId);

				String updateResponse = Executor.invokeService(
						ServiceURLEnum.CAMPAIGN_C360_DEFAULT_SPECIFICATION_MANAGE_PROC,
						defaultCampaignSpecificationProcMap, null, requestInstance);
				JSONObject updateDefaultCampaignSpecProcResponseJSON = CommonUtilities
						.getStringAsJSONObject(updateResponse);

				if (updateDefaultCampaignSpecProcResponseJSON != null
						&& updateDefaultCampaignSpecProcResponseJSON.has(FabricConstants.OPSTATUS)
						&& updateDefaultCampaignSpecProcResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
							ActivityStatusEnum.SUCCESSFUL, "Default campaign specification update successful");
				}
				else {
					AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.UPDATE,
							ActivityStatusEnum.FAILED, "Default campaign specification update failed");
					throw new ApplicationException(ErrorCodeEnum.ERR_21707);
				}
			}

			if (!defaultCampaignSpecifications.isEmpty()) {

				for (Map.Entry<String, DefaultCampaignSpecificationBean> entry : defaultCampaignSpecifications
						.entrySet()) {

					DefaultCampaignSpecificationBean defaultCampaignSpecification = entry.getValue();

					Map<String, String> postParametersMap = new HashMap<>();
					postParametersMap.put("campaignplaceholder_id",
							defaultCampaignSpecification.getCampaignPlaceholderId());
					postParametersMap.put("image_index", defaultCampaignSpecification.getImageIndex());

					String response = Executor.invokeService(ServiceURLEnum.DEFAULTCAMPAIGNSPECIFICATION_DELETE,
							postParametersMap, null, requestInstance);

					JSONObject responseJSONObject = CommonUtilities.getStringAsJSONObject(response);
					if (responseJSONObject != null && responseJSONObject.has(FabricConstants.OPSTATUS)
							&& responseJSONObject.getInt(FabricConstants.OPSTATUS) == 0) {
						AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
								ActivityStatusEnum.SUCCESSFUL, "Default campaign specification delete successful");
					}
					else {
						AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CAMPAIGN, EventEnum.DELETE,
								ActivityStatusEnum.FAILED, "Default campaign specification delete failed");
						throw new ApplicationException(ErrorCodeEnum.ERR_21750);
					}
				}
			}
		}

		return result;
	}

	Map<String, DefaultCampaignSpecificationBean> getDefaultCampaignSpecifications(
			DataControllerRequest requestInstance) throws ApplicationException {

		Map<String, DefaultCampaignSpecificationBean> defaultCampaignSpecifications = new HashMap<String, DefaultCampaignSpecificationBean>();

		// ** Reading from 'defaultcampaignspecification_get_c360_proc' stored procedure
		Map<String, String> postParametersMap = new HashMap<String, String>();

		String response = Executor.invokeService(
				ServiceURLEnum.CAMPAIGN_C360_DEFAULT_SPECIFICATION_GET_PROC, postParametersMap, null, requestInstance);
		JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

		if (responseJSON != null && responseJSON.has(FabricConstants.OPSTATUS)
				&& responseJSON.getInt(FabricConstants.OPSTATUS) == 0 && responseJSON.has("records")) {

			JSONArray responseJSONArray = responseJSON.getJSONArray("records");

			for (int i = 0; i < responseJSONArray.length(); ++i) {

				JSONObject responseJSONObject = responseJSONArray.getJSONObject(i);

				DefaultCampaignSpecificationBean defaultCampaignSpecification = new DefaultCampaignSpecificationBean();

				defaultCampaignSpecification.setCampaignPlaceholderId(responseJSONObject.optString("campaignplaceholder_id"));
				defaultCampaignSpecification.setChannel(responseJSONObject.optString("channel"));
				defaultCampaignSpecification.setScreen(responseJSONObject.optString("screen"));
				defaultCampaignSpecification.setImageResolution(responseJSONObject.optString("image_resolution"));
				defaultCampaignSpecification.setImageIndex(responseJSONObject.optString("image_index"));

				String defaultCampaignSpecificationsKey = defaultCampaignSpecification.getChannel() + "_"
						+ defaultCampaignSpecification.getScreen() + "_"
						+ defaultCampaignSpecification.getImageResolution() + "_"
						+ defaultCampaignSpecification.getImageIndex();

				defaultCampaignSpecifications.put(defaultCampaignSpecificationsKey, defaultCampaignSpecification);
			}
		}
		else {
			throw new ApplicationException(ErrorCodeEnum.ERR_21748);
		}

		return defaultCampaignSpecifications;
	}
}
