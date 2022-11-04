package com.kony.adminconsole.loans.postprocessor;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.konylabs.middleware.common.DataPostProcessor;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class GetCustomerIdPostProcessor implements DataPostProcessor {
	/*
	 * To log debug, info and error statements  
	 */
	private static final Logger log = Logger.getLogger(GetCustomerIdPostProcessor.class);
	
	/**
	 * Constants to hold the field names as Strings 
	 */
	public static final String CUSTOMER_ID = "id";
	public static final String CUSTOMER_USERNAME= "Username";
	public static final String CUSTOMER_FIRSTNAME= "FirstName";
	public static final String CUSTOMER_LASTNAME= "LastName";

	@Override
	public Object execute(Result results, DataControllerRequest dataControllerRequest) throws Exception {
		Result returnResult = new Result();
		Session sessionObject = dataControllerRequest.getSession();
		try{
			List<Param> result = results.getAllParams();
			if (result.isEmpty()) {
				if (log.isDebugEnabled()) {
					log.debug("No Customer Records Found!");
				}
				returnResult.addParam(getParamObject("errmsg", "No Customer Records Found!"));
				returnResult.addParam(getParamObject("errcode", "9001"));
				returnResult.addParam(getParamObject("Opstatus","9000"));
			}else{
				Param param = results.getParamByName("customer");
				String value = param.getValue();
				if(value.isEmpty()){
					if (log.isDebugEnabled()) {
						log.debug("No Customer Records Found!");
					}
					returnResult.addParam(getParamObject("errmsg", "No Customer Records Found!"));
					returnResult.addParam(getParamObject("errcode", "9001"));
					returnResult.addParam(getParamObject("Opstatus","9000"));
				}else{
					JSONObject json = new JSONObject(value);
					String db_customer_idValue=json.getString(CUSTOMER_ID);
					String db_customer_usernameValue=json.getString(CUSTOMER_USERNAME);
//						Session sessionObj = dataControllerRequest.getSession(true);
//						sessionObj.setAttribute("Customer.id", db_customer_idValue);
//						sessionObj.setAttribute("Customer.username", db_customer_usernameValue);
			
					returnResult.addParam(getParamObject("errmsg", "Customer Record Found!"));
					returnResult.addParam(getParamObject("Opstatus","0"));
					returnResult.addParam(getParamObject("errcode", "0"));
					sessionObject.setAttribute("IsGetCustomerObjectCalled","true");
					sessionObject.setAttribute("isValidateOTPCalled","false");
					returnResult.addParam(getParamObject("FirstName",json.getString(CUSTOMER_FIRSTNAME)));
					returnResult.addParam(getParamObject("LastName",json.getString(CUSTOMER_LASTNAME)));
					returnResult.addParam(getParamObject("Username",db_customer_usernameValue));
				}
			}
		} catch (Exception e) {
			log.error(" Error Occured in GetCustomerIdPostProcessor - execute Method: ", e);
			returnResult.addParam(getParamObject("errmsg", "Unknown error occured."+e.getMessage()));
			returnResult.addParam(getParamObject("Opstatus","9001"));
		}
		return returnResult;
	}
	private Param getParamObject(String Key, String Value) {
		Param param= new Param();
		param.setName(Key);
		param.setValue(Value);
		return param;
	}
}