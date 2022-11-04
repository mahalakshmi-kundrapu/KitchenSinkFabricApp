package com.kony.logservices.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.JSONUtils;
import com.kony.logservices.core.AbstractLogJavaService;
import com.kony.logservices.dao.LogDAO;
import com.kony.logservices.dto.CustomerActivityDTO;
import com.kony.logservices.dto.GetAllActivitiesInACustomerSessionDTO;
import com.kony.logservices.util.ErrorCodeEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * GetAllActivitiesInACustomerSession service will fetch all the activities in a customer session
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class GetAllActivitiesInACustomerSession extends AbstractLogJavaService {

    private static final Logger LOG = Logger.getLogger(GetAllActivitiesInACustomerSession.class);

    @Override
    public Object execute(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        try {

            String sessionId = requestInstance.getParameter("sessionId");
            if (StringUtils.isEmpty(sessionId)) {
                ErrorCodeEnum.ERR_29008.setErrorCode(processedResult);
                return processedResult;

            }

            // get all customer activities in a session
            List<CustomerActivityDTO> activities = LogDAO.getAllActivitiesForASession(sessionId);

            GetAllActivitiesInACustomerSessionDTO getAllActivitiesInACustomerSessionDTO =
                    new GetAllActivitiesInACustomerSessionDTO();
            getAllActivitiesInACustomerSessionDTO.setActivities(activities);

            String resultJSON = JSONUtils.stringify(getAllActivitiesInACustomerSessionDTO);
            JSONObject resultJSONObject = new JSONObject(resultJSON);
            return CommonUtilities.constructResultFromJSONObject(resultJSONObject);

        } catch (Exception e) {
            LOG.error("Unexpected error occured while fetching customer activity", e);
            // Obtain the error message
            processedResult.addParam(new Param("ExceptionMessage", e.getMessage(), FabricConstants.STRING));
            ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
        }
        return processedResult;
    }

}