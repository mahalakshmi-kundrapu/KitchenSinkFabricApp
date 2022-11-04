package com.kony.adminconsole.loans.postprocessor;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.loans.errorhandling.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPostProcessor;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class HierarchyReadPostProcessor implements DataPostProcessor {
	/** To log debug, info and error statements */
	private static final Logger log = Logger.getLogger(HierarchyReadPostProcessor.class);

	@Override
	public Object execute(Result results, DataControllerRequest dataControllerRequest) throws Exception {
		
		Result errorResult = new Result();
		List<Dataset> result = results.getDataSets();
		
		if (result.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug(" Empty recordss.. ");
			}
			Param storedprocparam = new Param();
			storedprocparam  = results.findParam("records");
			if (storedprocparam!= null) {
				// Stored procedure empty records
				errorResult = ErrorCodeEnum.ERR_33402.constructResultObject();
				return errorResult;
			}
			return results;
		}
		try{
			Dataset dataset = (Dataset) result.get(0);
			if (dataset.getRecords().isEmpty()) {
				if (log.isDebugEnabled()) {
					log.debug(" Empty recordss.. ");
				}
				errorResult = ErrorCodeEnum.ERR_33402.constructResultObject();
				return errorResult;
			}
			for (Record record : dataset.getRecords()) {
				if (record != null) {
					List<Param> params = record.getParams();
					for (Param param : params) {
						if (param != null && param.getObjectValue() instanceof String && isValidJSONArray(param.getObjectValue())) {
							JSONArray array = new JSONArray(param.getValue());
							Dataset datasetChild = new Dataset(param.getName() + "1");
							for (int i = 0; i < array.length(); i++) {
								JSONObject obj = (JSONObject) array.get(i);
								Record recordTemp = new Record();
								Set<String> set = obj.keySet();
								for (String key : set) {
									Param param1 = new Param();
									if (obj.get(key) instanceof JSONArray) {
										Dataset datasetTemp = new Dataset(key);
										datasetTemp = convertToDataset(datasetTemp, (JSONArray) obj.get(key));
										recordTemp.setDataset(datasetTemp);
									} else if (obj.get(key) instanceof String) {
										param1.setName(key);
										param1.setValue((String) obj.get(key));
										recordTemp.setParam(param1);
									} else if (obj.get(key) instanceof Boolean) {
										param1.setName(key);
										param1.setObjectValue((Boolean) obj.get(key));
										recordTemp.setParam(param1);
									}
								}
								datasetChild.setRecord(recordTemp);
							}
							record.setDataset(datasetChild);
							param.setValue("");
						}
					}
				}
			}
			
		} catch (Exception e) {
			log.error(" Error Occured in HierarchyRead - execute Method: ", e);
			errorResult=ErrorCodeEnum.ERR_31000.constructResultObject();
			return errorResult;
		}
		return results;
	}
	
	/**
	 *
	 * Method to check whether object is a valid JSONArray or not
	 * 
	 * @param str
	 * @return boolean
	 */
	public static boolean isValidJSONArray(Object str) {
		try {
			JSONArray jsonarr = new JSONArray(str.toString());
			if (log.isDebugEnabled()) {
				log.debug("isValidJSONArray is true for Object str: \n" + jsonarr.toString());
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	
	/**
	 *
	 * Method to convert JSONArray to Dataset
	 * 
	 * @param dataset
	 *            - An empty dataset
	 * @param array
	 * @return dataset - JSONArray converted to Dataset
	 */

	public static Dataset convertToDataset(Dataset dataset, JSONArray array) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("&&&&&& In method convertToDataset!!! &&&&&");
			}
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = (JSONObject) array.get(i);
				if (obj.length() == 0) {
					return dataset;
				}
				Record recordTemp = new Record();
				Set<String> set = obj.keySet();
				for (String key : set) {
					Param param1 = new Param();
					if (obj.get(key) instanceof JSONArray) {
						Dataset datasetTemp = new Dataset(key);
						datasetTemp = convertToDataset(datasetTemp, (JSONArray) obj.get(key));
						recordTemp.setDataset(datasetTemp);
					} else if (obj.get(key) instanceof String) {
						param1.setName(key);
						param1.setValue((String) obj.get(key));
						recordTemp.setParam(param1);
					} else if (obj.get(key) instanceof Boolean) {
						param1.setName(key);
						param1.setObjectValue((Boolean) obj.get(key));
						recordTemp.setParam(param1);
					}
				}
				dataset.setRecord(recordTemp);
			}
		} catch (Exception e) {
			log.error(" Error Occured in HierarchyRead - convertToDataset Method: ", e);
		}
		return dataset;
	}

}
