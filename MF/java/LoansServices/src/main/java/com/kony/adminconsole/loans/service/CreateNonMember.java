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

public class CreateNonMember implements JavaService2 {
 private static final Logger LOGGER = Logger.getLogger(CreateNonMember.class);

 @SuppressWarnings("unchecked")
 @Override
 public Object invoke(String methodID, Object[] inputArray, DataControllerRequest dcRequest,
  DataControllerResponse dcResponse)
 throws Exception {
  try {
   Result result = new Result();
   Dataset customerDataset = new Dataset();
   customerDataset.setId("Customer");

   Map < String, String > inputmap = (Map < String, String > ) inputArray[1];
   String custID = CommonUtilities.getNewId().toString();
   inputmap.put("id", custID);
   inputmap.put("Status_id", "SID_CUS_ACTIVE");
   if (inputmap.containsKey("Is_MemberEligibile") && inputmap.get("Is_MemberEligibile") == null) {
    inputmap.remove("Is_MemberEligibile");
   }
   if (inputmap.containsKey("CustomerType_id") && inputmap.get("CustomerType_id") == null) {
    inputmap.remove("CustomerType_id");
   }
   if (inputmap.containsKey("RegLinkResendCount") && inputmap.get("RegLinkResendCount") == null) {
    inputmap.remove("RegLinkResendCount");
   }
   result = hitCustomer(inputmap, dcRequest);
   Record recordTemp = result.getAllDatasets().get(0).getRecord(0);
   customerDataset.addRecord(recordTemp);
   if (inputmap.containsKey("Phone") && inputmap.get("Phone") != null) {
    JSONObject phoneJSON = createJSONForCustComm((String) inputmap.get("Phone"), "COMM_TYPE_PHONE", custID);
    hitCustomerCommunication(LoansUtilities.convertJSONtoMap(phoneJSON), dcRequest);
    inputmap.remove("Phone");
   }
   if (inputmap.containsKey("Email") && inputmap.get("Email") != null) {
    JSONObject emailJSON = createJSONForCustComm((String) inputmap.get("Email"), "COMM_TYPE_EMAIL", custID);
    hitCustomerCommunication(LoansUtilities.convertJSONtoMap(emailJSON), dcRequest);
    inputmap.remove("Email");
   }
   result.addDataset(customerDataset);
   return result;
  } catch (Exception e) {
   LOGGER.error("Exception in CreateNonMember : " + e.getMessage());
   Param exceptionToBeShownToUser = new Param();
   exceptionToBeShownToUser.setValue(e.getMessage());
   Result result = new Result();
   result.addParam(exceptionToBeShownToUser);
   return result;
  }
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

 private Result hitCustomer(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
  Result result = null;
  String Value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.CUSTOMER_CREATE.getServiceURL(dcRequest), (HashMap < String, String > ) inputParams, null, null);
  JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
  result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);

  return result;
 }

 private Result hitCustomerCommunication(Map < String, String > inputParams, DataControllerRequest dcRequest) throws Exception {
  Result result = null;
  String Value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.CUSTOMERCOMMUNICATION_CREATE.getServiceURL(dcRequest), (HashMap < String, String > ) inputParams, null, null);
  JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
  result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);

  return result;
 }
}