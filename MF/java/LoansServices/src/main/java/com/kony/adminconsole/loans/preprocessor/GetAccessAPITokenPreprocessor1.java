package com.kony.adminconsole.loans.preprocessor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.dto.JSONResponseHandlerBean;
import com.kony.adminconsole.commons.utils.http.HTTPOperations;
import com.kony.adminconsole.loans.utils.LoansServiceURLEnum;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

@SuppressWarnings({
	 "unchecked",
	 "rawtypes"
	})
public class GetAccessAPITokenPreprocessor1 implements DataPreProcessor2 {

	private static final Logger log = Logger
			.getLogger(GetAccessAPITokenPreprocessor1.class);
	private static final String SERVICE_NAME = "CustomerGet";
	static Properties LoansConfigProperties= new Properties();
	@Override
	public boolean execute(HashMap hashMap, DataControllerRequest request,
			DataControllerResponse response, Result result) throws Exception {
		try {
			String ServiceID = request.getParameter("current_serviceID");
			String deviceId = request.getHeader("X-Kony-DeviceId");
			Session sessionObject = request.getSession();
			if(ServiceID.equals(SERVICE_NAME)){
				InputStream LoansConfigPropertiesStream = GetAccessAPITokenPreprocessor1.class.getClassLoader().getResourceAsStream("LoansServiceURLCollection.properties");
				LoansConfigProperties.load(LoansConfigPropertiesStream);
				String XKonyToken = request.getHeader("X-Kony-Authorization");
				sessionObject.setAttribute("KonyDeviceId",request.getHeader("X-Kony-DeviceId"));
				if(callAccessAPIIntegrationServiceForAuthToken(request,XKonyToken)){
					String AccessToken = LoansConfigProperties.getProperty("AdminAccessAPI_AuthToken");
					Map<String,Object> headerMap = request.getHeaderMap();
					sessionObject.setAttribute("IsGenerateOTPCalled","false");
					sessionObject.setAttribute("IsGetCustomerObjectCalled","false");
					sessionObject.setAttribute("isValidateOTPCalled","false");
					hashMap.put("X-Kony-Authorization",AccessToken);
					hashMap.put("X-Kony-AC-API-Access-By","OLB");
					hashMap.put("DeviceID",deviceId);
					headerMap.put("X-Kony-Authorization",AccessToken);
				}
				else{
					log.debug("Error while calling the login Service For Admin Console");
					throw new Exception("Not Auhtorized");
				}
			}
			else{
				if(deviceId.equals(sessionObject.getAttribute("KonyDeviceId"))){
					if(ServiceID.equals("GenerateOTP") && sessionObject.getAttribute("IsGetCustomerObjectCalled").equals("false")){
						log.debug("Not authorized for this request");
						throw new Exception("Not Authorized!");
					}
					if(ServiceID.equals("ValidateOTP") && sessionObject.getAttribute("IsGenerateOTPCalled").equals("false")){
						log.debug("Not authorized for this request");
						throw new Exception("Not Authorized!");
					}
					if(ServiceID.equals("SetPassword") && sessionObject.getAttribute("isValidateOTPCalled").equals("false")){
						log.debug("Not authorized for this request");
						throw new Exception("Not Authorized!");
					}
					String AccessToken = LoansConfigProperties.getProperty("AdminAccessAPI_AuthToken");
					if(AccessToken==null){
						log.debug("AccessToken is null while getting from JAR");
						throw new Exception("Not Authorized!");
					}
					Map<String,Object> headerMap = request.getHeaderMap();
					hashMap.put("X-Kony-Authorization",AccessToken);
					hashMap.put("X-Kony-AC-API-Access-By","OLB");
					hashMap.put("DeviceID",deviceId);
					headerMap.put("X-Kony-Authorization",AccessToken);
				}
				else{
					log.debug("Device is not registered for this Service");
					throw new Exception("Device is not registered for this Service");
				}
			}
		} catch (Exception e) {
			System.out.println("Exception : " + e.getMessage());
			Param exceptionToBeShownToUser = new Param();
			exceptionToBeShownToUser.setName("errmsg");
			exceptionToBeShownToUser.setValue(e.getMessage());
			result.addParam(exceptionToBeShownToUser);
			return false;
		}
		return true;
	}
	public static boolean callAccessAPIIntegrationServiceForAuthToken (DataControllerRequest request,String XKony) throws Exception{
		try{
			JSONResponseHandlerBean Response= HTTPOperations.hitPOSTServiceAndGetJSONResponse(LoansServiceURLEnum.GET_TOKEN_CUSTOMAPI_GET.getServiceURL(request), null, XKony, null);
			JSONObject ResponseJSON = Response.getResponseAsJSONObject();
			if(ResponseJSON != null){
				if(ResponseJSON instanceof JSONObject){
					String ClaimsToken = ResponseJSON.getString("Claims_Token");
					if(ClaimsToken != null){
						LoansConfigProperties.setProperty("AdminAccessAPI_AuthToken",ClaimsToken);
						return true;
					}
					log.debug("Claims_Token is empty or null in Response "+Response.toString());
					throw new Exception("Claims_Token is empty or null in Response");
				}
				else{
					log.debug("Wrong Response received from AccessAPI"+Response.toString());
					throw new Exception("Wrong Response received from AccessAPI");
				}
			}
		}
		catch(Exception e){
			log.debug("Unknown Error Occured "+e.getStackTrace());
			log.debug("Error from Previous Service"+e.getMessage());
			throw new Exception("Error in AccessAPI "+e.getMessage());
		}
		return false;
	}
}
