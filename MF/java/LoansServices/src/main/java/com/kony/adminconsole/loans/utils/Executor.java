package com.kony.adminconsole.loans.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.entity.BufferedHttpEntity;
import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.utils.ServiceExecutables;
import com.konylabs.middleware.controller.DataControllerRequest;

public class Executor {

	private static final Logger LOG = Logger.getLogger(Executor.class);
	
	public static String invokeService(LoansServiceURLEnum serviceURLEnum, Map<String, String> inputBodyMap, Map<String, String> requestHeaders,
			DataControllerRequest request) {
		try {
			return ServiceExecutables.INLINE.invokeService(serviceURLEnum.getServiceName(request), serviceURLEnum.getOperationName(request), convertMap(inputBodyMap),
					convertMap(requestHeaders), request);
		} catch (Exception e) {
			LOG.error(e);
			return null;
		}
	}

	public static BufferedHttpEntity invokePassthroughService(LoansServiceURLEnum serviceURLEnum, Map<String, String> inputBodyMap, Map<String, String> requestHeaders,
			DataControllerRequest request) {
		try {
		return ServiceExecutables.INLINE.invokePassthroughService(serviceURLEnum.getServiceName(request), serviceURLEnum.getOperationName(request), convertMap(inputBodyMap),
				convertMap(requestHeaders), request);
		} catch (Exception e) {
			LOG.error(e);
			return null;
		}
	}

	private static Map<String, Object> convertMap(Map<String, String> sourceMap) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (sourceMap != null && !sourceMap.isEmpty()) {
			for (Entry<String, String> currRecord : sourceMap.entrySet()) {
				resultMap.put(currRecord.getKey(), Object.class.cast(currRecord.getValue()));
			}
		}
		return resultMap;
	}
}
