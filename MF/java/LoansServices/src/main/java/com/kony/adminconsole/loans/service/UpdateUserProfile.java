package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilities;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class UpdateUserProfile implements JavaService2 {
	private static final Logger LOGGER = Logger.getLogger(UpdateUserProfile.class);

	@SuppressWarnings({ "unchecked" })
	@Override
	public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
			DataControllerResponse dcResponse) throws Exception {
		try {
			Result result = new Result();
			Dataset customerDataset = new Dataset();
			Map<String, String> inputmap = (Map<String, String>) inputArray[1];
			String custID = inputmap.get("id");
			customerDataset.setId("Customer");

			if (inputmap.containsKey("CustomerAddress") && inputmap.get("CustomerAddress") != null) {
				Map<String, String> inputParams = new HashMap<String, String>();
				inputParams.put("$filter", "Customer_id eq " + custID);
				JSONObject res = getCustomerAddress(inputParams, dcRequest);
				JSONArray customerAddressArr = new JSONArray(inputmap.get("CustomerAddress"));
				JSONObject customerAddress = customerAddressArr.getJSONObject(0);
				customerAddress.put("Customer_id", custID);
				String addressId = "ADD" + CommonUtilities.getNewId().toString();
				JSONArray arr = (JSONArray) res.get("customeraddress");
				for (int i = 0; i < arr.length(); i++) {
					JSONObject j = (JSONObject) arr.get(i);
					if (j.has("Type_id") && j.get("Type_id").equals("ADR_TYPE_HOME")) {
						addressId = j.getString("Address_id");
					}
				}
				customerAddress.put("Address_id", addressId);
				if (inputmap.containsKey("Address") && inputmap.get("Address") != null) {
					JSONArray addressArr = new JSONArray(inputmap.get("Address"));
					JSONObject address = addressArr.getJSONObject(0);
					address.put("id", addressId);
					if (arr.length() > 0) {
						updateAddress(LoansUtilities.convertJSONtoMap(address), dcRequest);
						updateCustomerAddress(LoansUtilities.convertJSONtoMap(customerAddress), dcRequest);
					} else {
						createAddress(LoansUtilities.convertJSONtoMap(address), dcRequest);
						customerAddress.put("Type_id", "ADR_TYPE_HOME");
						customerAddress.put("isPrimary", "1");
						customerAddress.put("Address_id", addressId);
						createCustomerAddress(LoansUtilities.convertJSONtoMap(customerAddress), dcRequest);
					}
					inputmap.remove("Address");
					inputmap.remove("CustomerAddress");
				}
			}

			if (inputmap.containsKey("Phone") && inputmap.get("Phone") != null) {
				Map<String, String> inputParams = new HashMap<String, String>();
				inputParams.put("$filter", "Customer_id eq " + custID);
				JSONObject phoneJSON = createJSONForCustComm((String) inputmap.get("Phone"), "COMM_TYPE_PHONE", custID);
				JSONObject res = getCustomerCommunication((Map<String, String>) inputParams, dcRequest);
				JSONArray arr = (JSONArray) res.get("customercommunication");
				if (arr.length() > 0) {
					for (int i = 0; i < arr.length(); i++) {
						JSONObject j = (JSONObject) arr.get(i);
						if (j.has("Type_id") && j.get("Type_id").equals("COMM_TYPE_PHONE")) {
							phoneJSON.put("isPrimary", "0");
						}
					}
					createCustomerCommunication(LoansUtilities.convertJSONtoMap(phoneJSON), dcRequest);
				} else {
					createCustomerCommunication(LoansUtilities.convertJSONtoMap(phoneJSON), dcRequest);
				}
				inputmap.remove("Phone");
			}

			if (inputmap.containsKey("Email") && inputmap.get("Email") != null) {
				Map<String, String> inputParams = new HashMap<String, String>();
				inputParams.put("$filter", "Customer_id eq " + custID);
				JSONObject emailJSON = createJSONForCustComm((String) inputmap.get("Email"), "COMM_TYPE_EMAIL", custID);
				JSONObject res = getCustomerCommunication((Map<String, String>) inputParams, dcRequest);

				JSONArray arr = (JSONArray) res.get("customercommunication");
				if (arr.length() > 0) {
					for (int i = 0; i < arr.length(); i++) {
						JSONObject j = (JSONObject) arr.get(i);
						if (j.has("Type_id") && j.get("Type_id").equals("COMM_TYPE_EMAIL")) {
							emailJSON.put("isPrimary", "0");
						}
					}
					createCustomerCommunication(LoansUtilities.convertJSONtoMap(emailJSON), dcRequest);
				} else {
					createCustomerCommunication(LoansUtilities.convertJSONtoMap(emailJSON), dcRequest);
				}
				inputmap.remove("Email");
			}
			if (inputmap.containsKey("DateOfBirth") && inputmap.get("DateOfBirth") == null) {
				inputmap.remove("DateOfBirth");
			}

			result = updateCustomer(inputmap, dcRequest);
			Record recordTemp = result.getAllDatasets().get(0).getRecord(0);
			customerDataset.addRecord(recordTemp);
			return result;
		} catch (Exception e) {
			LOGGER.error("Exception in UpdateUserProfile : " + e.getMessage());
			Result result = new Result();
			result = ErrorCodeEnum.ERR_31000.constructResultObject();
			return result;
		}

	}

	private Result updateCustomer(Map<String, String> inputParams, DataControllerRequest dcRequest) throws Exception {
		Result result = null;
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.CUSTOMER_UPDATE.getServiceURL(dcRequest), (HashMap<String, String>) inputParams,
				null, null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

	private Result createAddress(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		Result result = null;
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.ADDRESS_CREATE.getServiceURL(dcRequest), (HashMap<String, String>) inputParams,
				null, null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

	private Result updateAddress(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		Result result = null;
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.ADDRESS_UPDATE.getServiceURL(dcRequest), (HashMap<String, String>) inputParams,
				null, null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

	private Result updateCustomerAddress(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		Result result = null;
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.CUSTOMERADDRESS_UPDATE.getServiceURL(dcRequest),
				(HashMap<String, String>) inputParams, null, null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

	private Result createCustomerAddress(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		Result result = null;
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.CUSTOMERADDRESS_CREATE.getServiceURL(dcRequest),
				(HashMap<String, String>) inputParams, null, null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

	private Result createCustomerCommunication(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		Result result = new Result();
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.CUSTOMERCOMMUNICATION_CREATE.getServiceURL(dcRequest),
				(HashMap<String, String>) inputParams, null, null);
		JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
		result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
		return result;
	}

	private JSONObject getCustomerCommunication(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.CUSTOMERCOMMUNICATION_GET.getServiceURL(dcRequest),
				(HashMap<String, String>) inputParams, null, null);
		return CommonUtilities.getStringAsJSONObject(Value);
	}

	private JSONObject getCustomerAddress(Map<String, String> inputParams, DataControllerRequest dcRequest) {
		String Value = HTTPOperations.hitPOSTServiceAndGetResponse(
				LoansServiceURLEnum.CUSTOMERADDRESS_GET.getServiceURL(dcRequest), (HashMap<String, String>) inputParams,
				null, null);
		return CommonUtilities.getStringAsJSONObject(Value);
	}

	private JSONObject createJSONForCustComm(String value, String type, String customerID) {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("id", "CID" + CommonUtilities.getNewId().toString());
		jsonObject.put("isPrimary", "1");
		jsonObject.put("Value", value);
		jsonObject.put("Customer_id", customerID);
		jsonObject.put("Extension", "Personal");
		jsonObject.put("Type_id", type);

		return jsonObject;
	}
}