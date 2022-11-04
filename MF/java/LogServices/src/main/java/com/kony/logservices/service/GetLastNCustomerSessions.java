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
import com.kony.logservices.dto.GetLastNCustomerSessionsDTO;
import com.kony.logservices.util.ErrorCodeEnum;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * GetLastNCustomerSessions service will fetch last 'N' customer sessions
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class GetLastNCustomerSessions extends AbstractLogJavaService {

    private static final Logger LOG = Logger.getLogger(GetLastNCustomerSessions.class);

    @Override
    public Object execute(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        try {

            String username = requestInstance.getParameter("username");
            if (StringUtils.isEmpty(username)) {
                return errorResponse(processedResult, ErrorCodeEnum.ERR_29005);
            }

            Integer sessionCount = 3;
            if (StringUtils.isNotEmpty(requestInstance.getParameter("sessionCount"))) {
                sessionCount = Integer.parseInt(requestInstance.getParameter("sessionCount"));
            }

            // get last 'N' customer sessions
            List<CustomerActivityDTO> sessions = LogDAO.getLastNCustomerSessions(username, sessionCount);
            GetLastNCustomerSessionsDTO getLastNCustomerSessionsDTO = new GetLastNCustomerSessionsDTO();
            getLastNCustomerSessionsDTO.setSessions(sessions);

            String resultJSON = JSONUtils.stringify(getLastNCustomerSessionsDTO);
            JSONObject resultJSONObject = new JSONObject(resultJSON);
            return CommonUtilities.constructResultFromJSONObject(resultJSONObject);

        } catch (Exception e) {
            LOG.error("Unexpected error occured while fetching customer activity", e);
            // Obtain the error message
            processedResult.addParam(new Param("ExceptionMessage", e.getMessage(), FabricConstants.STRING));
            return errorResponse(processedResult, ErrorCodeEnum.ERR_20001);
        }
    }

    /**
     * Constructs a failure response from the given ErrorCodeEnum object.
     * 
     * @param processedResult
     * @param codeEnum
     * @return Result object
     */
    public Result errorResponse(Result processedResult, ErrorCodeEnum codeEnum) {

        processedResult.addParam(new Param(FabricConstants.HTTP_STATUS_CODE, "200", FabricConstants.INT));
        codeEnum.setErrorCode(processedResult);
        return processedResult;
    }
}