package com.kony.adminconsole.loans.postprocessor;
 
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.XML;
 
import com.konylabs.middleware.common.DataPostProcessor;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Result;
import com.kony.adminconsole.commons.utils.CommonUtilities;
 
public class XmlConversion implements DataPostProcessor {
 
    private static final Logger log = Logger.getLogger(XmlConversion.class);
 
    @Override
    public Object execute(Result results, DataControllerRequest dataControllerRequest) {
        log.error("Started XmlToJsonConversion");
        try {
            String XmlString = results.getParamByName("AddressValidateResponse").getValue();
            JSONObject xmlJSONObj = XML.toJSONObject(XmlString);
            String jsonPrettyPrintString = xmlJSONObj.toString();
            Result returnResult = new Result();
            returnResult = CommonUtilities.getResultObjectFromJSONObject(xmlJSONObj);
            return returnResult;
        } catch (Exception e) {
            return results;
        }
    }
}