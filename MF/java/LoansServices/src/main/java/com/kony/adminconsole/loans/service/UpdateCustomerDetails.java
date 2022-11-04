package com.kony.adminconsole.loans.service;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class UpdateCustomerDetails implements JavaService2 {
 @Override

 public Object invoke(String methodID, Object[] inputArray,
  DataControllerRequest dcRequest, DataControllerResponse dcResponse)
 throws Exception {
  try {
   Result result = new Result();
   Dataset customerDataset = new Dataset();
   customerDataset.setId("Customer");
   Map inputmap = (Map) inputArray[1];
   if (inputmap.containsKey("CustomerType_id") && inputmap.get("CustomerType_id") == null) {
    inputmap.remove("CustomerType_id");
   }
   if (inputmap.containsKey("FirstName") && inputmap.get("FirstName") == null) {
    inputmap.remove("FirstName");
   }
   if (inputmap.containsKey("MiddleName") && inputmap.get("MiddleName") == null) {
    inputmap.remove("MiddleName");
   }
   if (inputmap.containsKey("LastName") && inputmap.get("LastName") == null) {
    inputmap.remove("LastName");
   }
   if (inputmap.containsKey("Is_MemberEligibile") && inputmap.get("Is_MemberEligibile") == null) {
    inputmap.remove("Is_MemberEligibile");
   }
   if(inputmap.containsKey("RegLinkResendCount")&&inputmap.get("RegLinkResendCount")==null) {
		inputmap.remove("RegLinkResendCount");
	}
   if ((inputmap.containsKey("CustomerType_id") && inputmap.get("CustomerType_id") != null) || (inputmap.containsKey("FirstName") && inputmap.get("FirstName") != null) ||
    (inputmap.containsKey("MiddleName") && inputmap.get("MiddleName") != null) || (inputmap.containsKey("LastName") && inputmap.get("LastName") != null) || (inputmap.containsKey("Is_MemberEligibile") && inputmap.get("Is_MemberEligibile") != null)|| (inputmap.containsKey("RegLinkResendCount") && inputmap.get("RegLinkResendCount") != null)) {
    Result updateQueryResponse = hitCustomerUpdateService(inputmap, dcRequest);
    Record recordTemp = updateQueryResponse.getAllDatasets().get(0).getRecord(0);
    customerDataset.addRecord(recordTemp);
   }
   if (inputmap.containsKey("Phone") && inputmap.get("Phone") != null) {
    inputmap.put("Value", inputmap.get("Phone"));
    inputmap.put("id", inputmap.get("PhoneId"));
    Result PhoneUpdateResponse = hitCustomerCommunicationUpdateService(inputmap, dcRequest);
    Record recordTemp1 = PhoneUpdateResponse.getAllDatasets().get(0).getRecord(0);
    customerDataset.addRecord(recordTemp1);
   }
   if (inputmap.containsKey("Email") && inputmap.get("Email") != null) {
    inputmap.put("Value", inputmap.get("Email"));
    inputmap.put("id", inputmap.get("EmailId"));
    Result EmailUpdateResponse = hitCustomerCommunicationUpdateService(inputmap, dcRequest);
    Record recordTemp2 = EmailUpdateResponse.getAllDatasets().get(0).getRecord(0);
    customerDataset.addRecord(recordTemp2);
   }
   result.addDataset(customerDataset);
   return result;
  } catch (Exception e) {
   Result result = new Result();
   String stringliteral = "String";
   result.addParam(new Param("stacktrace", createStackTraceString(e), stringliteral));
   return result;
  }
 }

 private Result hitCustomerUpdateService(Map inputParams, DataControllerRequest dcRequest) {
  Result result = null;
  try {
   String Value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.CUSTOMER_UPDATE.getServiceURL(dcRequest), (HashMap < String, String > ) inputParams, null, null);
   JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
   result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
  } catch (Exception e) {
   e.printStackTrace();
  }
  return result;
 }

 private Result hitCustomerCommunicationUpdateService(Map inputParams, DataControllerRequest dcRequest) {
  Result result = null;
  try {
   String Value = HTTPOperations.hitPOSTServiceAndGetResponse(LoansServiceURLEnum.CUSTOMERCOMMUNICATION_UPDATE.getServiceURL(dcRequest), (HashMap < String, String > ) inputParams, null, null);
   JSONObject ValueResponseJSON = CommonUtilities.getStringAsJSONObject(Value);
   result = CommonUtilities.getResultObjectFromJSONObject(ValueResponseJSON);
  } catch (Exception e) {
   e.printStackTrace();
  }
  return result;
 }
 public String createStackTraceString(Exception e) {
  StringWriter sw = new StringWriter();
  PrintWriter pw = new PrintWriter(sw);
  e.printStackTrace(pw);
  String sStackTrace = sw.toString(); // stack trace as a string
  return sStackTrace;
 }
}