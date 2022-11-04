package com.kony.adminconsole.loans.preprocessor;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.kony.adminconsole.loans.postprocessor.GetCustomerIdPostProcessor;
import com.konylabs.middleware.common.DataPreProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;
import com.konylabs.middleware.session.Session;

public class GetSessionToken implements DataPreProcessor2{

	private static final Logger log = Logger.getLogger(GetCustomerIdPostProcessor.class);
	
	@Override
	public boolean execute(HashMap hashMap, DataControllerRequest request,
			DataControllerResponse response, Result result) throws Exception {
		try{
		Session session = request.getSession();
		String deviceId = request.getHeader("X-Kony-DeviceId");
		String CustomerId =null;
		String username = null;
		if(session.getAttribute("KonyDeviceId")!=null && session.getAttribute("KonyDeviceId").equals(deviceId)){			
			if(session.getAttribute("Customer.id") != null){
				CustomerId = session.getAttribute("Customer.id").toString();
			}
			else{
				log.debug("CustomerId not found");
				throw(new Exception("CustomerId not found"));
			}
			if(session.getAttribute("Customer.username") != null){
				username = session.getAttribute("Customer.username").toString();
			}
			else{
				log.debug("Customer username not found");
				throw(new Exception("Customer Username not found"));
			}
			hashMap.put("id",CustomerId);
			hashMap.put("username",username);
		}
		else{
			log.debug("DeviceId is not same as what is registered for this call");
			throw new Exception("DeviceId is not same as what is registered for this call");
		}
		}
		catch(Exception e){
			System.out.println("Exception : "+e.getMessage());
			Param exceptionToBeShownToUser = new Param();
			exceptionToBeShownToUser.setValue(e.getMessage());
			result.addParam(exceptionToBeShownToUser);
			return false;
		}
		return true;
	}
}
