package com.kony.adminconsole.service.alertmanagement.staging;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.kony.adminconsole.handler.AlertManagementHandler;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.DBPServices;
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

public class CustomerAccountAlertsGet implements JavaService2 {

	private static final String INPUT_USERNAME = "username";
	private static final String INPUT_ACCOUNTS = "accounts";
	private static final String APPLICATION_ATTRIBUTE = "isAlertAccountIDLevel";
	
	private static final Logger LOG = Logger.getLogger(CustomerAccountAlertsGet.class);
	
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
			DataControllerResponse responseInstance) {
		Result processedResult = new Result();
		
		try {
			String username = requestInstance.getParameter(INPUT_USERNAME);
			
			if(StringUtils.isBlank(username)) {
				ErrorCodeEnum.ERR_20612.setErrorCode(processedResult);
				return processedResult;
			}
			
			Dataset customerAccountsDataset = new Dataset("accountInfo");
			CustomerHandler customerHandler = new CustomerHandler();
			
			JSONObject customerDetails = customerHandler.getCustomerDetails(username, null, requestInstance);
			String customerId = customerDetails.optString("id");
			if(StringUtils.isBlank(customerId)) {
				ErrorCodeEnum.ERR_21028.setErrorCode(processedResult);
				return processedResult;
			}
			JSONArray accountsArray = null;
			//Check if input has accounts
			String accountsFromInput = requestInstance.getParameter(INPUT_ACCOUNTS);
			if(StringUtils.isBlank(accountsFromInput)) {
				JSONObject accounts = DBPServices.getAccounts(username, requestInstance);
				// Validate response
				if (accounts == null || !accounts.has(FabricConstants.OPSTATUS)
						|| (accounts.getInt(FabricConstants.OPSTATUS) != 0)) {
					ErrorCodeEnum.ERR_20656.setErrorCode(processedResult);
					return processedResult;
				}
				if(accounts.has("Accounts")) {
					accountsArray = accounts.getJSONArray("Accounts");	
				}else {
					accountsArray = new JSONArray();
				}
				
			}else {
				accountsArray = new JSONArray(accountsFromInput);
			}
			
			Map<String,String> accountTypes = AlertManagementHandler.getAccountTypesMap(requestInstance);
			Map<String, String> accountTypesMasterdata = AlertManagementHandler.getAccountTypesMap(requestInstance);
			
			boolean isAlertsAccountNumberLevel = Boolean.parseBoolean(customerHandler.getAttributeFromApplicationTable(APPLICATION_ATTRIBUTE, requestInstance));
			
			JSONArray customerAlerts = getCustomerAccountSubscribtionStatus(customerId, requestInstance, processedResult);
			if(customerAlerts == null) {
				return processedResult;
			}
			
			Set<String> accountStatusArray = AlertManagementHandler.getAccountStatusArray(isAlertsAccountNumberLevel, customerAlerts);
			
			if(isAlertsAccountNumberLevel) {
				accountsArray.forEach((account)->{
					JSONObject accountObject = (JSONObject)account;
					Record accountsRecord = new Record();
					accountsRecord.addParam(new Param("accountID", accountObject.getString("accountID"),FabricConstants.STRING));
					accountsRecord.addParam(new Param("nickName", accountObject.optString("nickName"),FabricConstants.STRING));
					accountsRecord.addParam(new Param("accountType", accountObject.optString("accountType"),FabricConstants.STRING));
					accountsRecord.addParam(new Param("bankName", accountObject.optString("bankName"),FabricConstants.STRING));
					if(accountObject.has("currencyCode") && !accountObject.getString("currencyCode").equalsIgnoreCase("$currencyCode")) {
						accountsRecord.addParam(new Param("transactionCurrency", accountObject.optString("currencyCode"),FabricConstants.STRING));
					}
					
					if(accountStatusArray.contains(accountObject.getString("accountID"))){
						accountsRecord.addParam(new Param("isEnabled", "true",FabricConstants.STRING));
					}else {
						accountsRecord.addParam(new Param("isEnabled", "false",FabricConstants.STRING));
					}
					customerAccountsDataset.addRecord(accountsRecord);
				});
			}else {
				Map<String, String> accountTypesMap = new HashMap<>();
				accountsArray.forEach((account)->{
					JSONObject accountObject = (JSONObject)account;
					String customerAccountType = accountObject.optString("accountType");
					
					if(accountStatusArray.contains(accountTypes.get(customerAccountType))) {
						accountTypesMap.put(customerAccountType, "true");
					}else {
						accountTypesMap.put(customerAccountType, "false");
					}
				});
				
				accountTypesMap.forEach((accountType, isEnabled) -> {
					Record accountsRecord = new Record();
					accountsRecord.addParam(new Param("accountType", accountType,FabricConstants.STRING));
					accountsRecord.addParam(new Param("accountTypeId", accountTypesMasterdata.get(accountType),FabricConstants.STRING));
					accountsRecord.addParam(new Param("isEnabled", isEnabled,FabricConstants.STRING));
					customerAccountsDataset.addRecord(accountsRecord);
				});
			}
			
			processedResult.addParam(new Param("isAlertsAccountNumberLevel",String.valueOf(isAlertsAccountNumberLevel),FabricConstants.STRING));
			processedResult.addDataset(customerAccountsDataset);
			
		}catch(DBPAuthenticationException dae) {
			LOG.error("DBP authentication Exception. Failed to login:", dae);
			dae.getErrorCodeEnum().setErrorCode(processedResult);
			return processedResult;
		}catch(ApplicationException ae) {
			LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", ae);
			ae.getErrorCodeEnum().setErrorCode(processedResult);
			return processedResult;
		}catch(Exception e) {
			LOG.error("Unexpected Exception.Exception Trace:", e);
			ErrorCodeEnum.ERR_20926.setErrorCode(processedResult);
			return processedResult;
		}
		return processedResult;
	}
	
	private static JSONArray getCustomerAccountSubscribtionStatus(String customerId, DataControllerRequest requestInstance, Result processedResult) {
		Map<String, String> inputMap = new HashMap<>();
		inputMap.put(ODataQueryConstants.FILTER,"Customer_id eq '"+customerId+"'");
		String readSwitchResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERALERTSWITCH_READ, inputMap, null, requestInstance);
		JSONObject readSwitchResponseJSON = CommonUtilities.getStringAsJSONObject(readSwitchResponse);
		if (readSwitchResponseJSON != null && readSwitchResponseJSON.has(FabricConstants.OPSTATUS)
				&& readSwitchResponseJSON.getInt(FabricConstants.OPSTATUS) == 0 && readSwitchResponseJSON.has("customeralertswitch")) {
			return readSwitchResponseJSON.getJSONArray("customeralertswitch");
		}
		ErrorCodeEnum.ERR_20926.setErrorCode(processedResult);
		processedResult.addParam(new Param("FailureReason", readSwitchResponse, FabricConstants.STRING));
		return null;
	}

}
