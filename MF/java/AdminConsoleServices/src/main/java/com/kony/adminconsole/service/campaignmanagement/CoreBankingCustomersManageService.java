package com.kony.adminconsole.service.campaignmanagement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.dto.ModelBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to add Core Banking customers into 'backendidentifier' table in the back end
 * ---- >>>> FOR TEMPORARY USE ONLY! ---- ---- To be invoked until DBP readies their customer sync service <<<< ----
 * 
 * @author Mohit Khosla (KH2356)
 */

public class CoreBankingCustomersManageService implements JavaService2 {

	private static final Logger LOG = Logger.getLogger(CoreBankingCustomersManageService.class);

	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) throws Exception {

		Result result = new Result();
		String userId = LoggedInUserHandler.getUserDetails(requestInstance).getUserId();

		try {
			List<ModelBean> modelList = CampaignHandler.getAllModels(requestInstance);
			Set<String> coreBankingCustomers = new HashSet<String>();

			for (int i = 0; i < modelList.size(); ++i) {
				String endpointURL = modelList.get(i).getEndpointURL();

				List<String> customerIdsFromOneURL = CampaignHandler.getCustomerIdsFromCoreBanking(requestInstance, endpointURL);

				for (String customerId : customerIdsFromOneURL) {
					coreBankingCustomers.add(customerId);
				}
			}

			addEntriesToBackendIdentifier(requestInstance, userId, coreBankingCustomers);
		}
		catch (ApplicationException ae) {
			ae.getErrorCodeEnum().setErrorCode(result);
			LOG.error("ApplicationException occured in CoreBankingCustomersManageService JAVA service. Error: ", ae);
		}
		catch (Exception e) {
			ErrorCodeEnum.ERR_20001.setErrorCode(result);
			LOG.error("Exception occured in CoreBankingCustomersManageService JAVA service. Error: ", e);
		}

		return result;
	}

	public void addEntriesToBackendIdentifier(DataControllerRequest requestInstance, String userId, Set<String> coreBankingCustomers) throws ApplicationException {

		for (String coreBankingCustomer : coreBankingCustomers) {

			Map<String, String> backendIdentifierMap = new HashMap<>();

			backendIdentifierMap.put("id", CommonUtilities.getNewId().toString());
			backendIdentifierMap.put("Customer_id", "1002496540"); // 'dbpolbuser'
			backendIdentifierMap.put("BackendId", coreBankingCustomer);
			backendIdentifierMap.put("createdby", userId);

			String response = Executor.invokeService(ServiceURLEnum.BACKENDIDENTIFIER_CREATE, backendIdentifierMap, null, requestInstance);
			JSONObject responseJSON = CommonUtilities.getStringAsJSONObject(response);

			if (responseJSON == null || !responseJSON.has(FabricConstants.OPSTATUS) || responseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
				throw new ApplicationException(ErrorCodeEnum.ERR_21792);
			}
		}
	}
}