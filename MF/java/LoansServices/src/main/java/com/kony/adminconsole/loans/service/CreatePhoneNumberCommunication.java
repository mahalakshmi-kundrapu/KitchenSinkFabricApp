package com.kony.adminconsole.loans.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.kony.adminconsole.loans.utils.LoansUtilities;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

@SuppressWarnings({
 "rawtypes",
 "unchecked"
})
public class CreatePhoneNumberCommunication implements JavaService2 {

 private static final Logger LOGGER = Logger.getLogger(CreatePhoneNumberCommunication.class);

 @Override
 public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
  DataControllerResponse dcResponse)
 throws Exception {
  try {
   Result result = new Result();
   Dataset customerDataset = new Dataset();
   customerDataset.setId("CustomerCommunication");
   Map inputmap = (Map) inputArray[1];
   String custID = (String) inputmap.get("Customer_id");
   if (inputmap.containsKey("Phone") && inputmap.get("Phone") != null) {
    JSONObject phoneJSON = createJSONForCustComm((String) inputmap.get("Phone"), "COMM_TYPE_PHONE", custID);
    result = hitCustomerCommunication(LoansUtilities.convertJSONtoMap(phoneJSON), dcRequest);
    Record recordTemp = result.getAllDatasets().get(0).getRecord(0);
    customerDataset.addRecord(recordTemp);
    inputmap.remove("Phone");
   }
   result.addDataset(customerDataset);
   return result;
  } catch (Exception e) {
   LOGGER.error("Exception in CreatePhoneNumberCommunication : " + e.getMessage());
   Param exceptionToBeShownToUser = new Param();
   exceptionToBeShownToUser.setValue(e.getMessage());
   Result result = new Result();
   result.addParam(exceptionToBeShownToUser);
   return result;
  }
 }

 private JSONObject createJSONForCustComm(String value, String type, String customerID) {
  JSONObject json = new JSONObject();

  json.put("id", "CID" + CommonUtilities.getNewId().toString());
  json.put("isPrimary", "1");
  json.put("Value", value);
  json.put("Customer_id", customerID);
  json.put("Extension", "Personal");
  json.put("Type_id", type);

  return json;
 }

 private Result hitCustomerCommunication(Map inputParams, DataControllerRequest dcRequest) throws Exception {
  Result result = null;
  String Value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.CUSTOMERCOMMUNICATION_CREATE.getServiceURL(dcRequest), (HashMap < String, String > ) inputParams, null, null);
  JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
  result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);

  return result;
 }
}