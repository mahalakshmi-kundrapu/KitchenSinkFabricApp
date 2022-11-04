package com.kony.adminconsole.service.card;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.CustomerHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to perform CSR actions on the Customer Cards
 * 
 * @author Aditya Mankal
 * 
 */
public class CardManageService implements JavaService2 {

    private static final String COREBANKING_REQUEST_TYPE_LOCK_CARD = "Lock";
    private static final String COREBANKING_REQUEST_TYPE_LOST_STOLEN_CARD = "Report Lost";
    private static final String COREBANKING_REQUEST_TYPE_ACTIVATE_CARD = "Activate";
    private static final String COREBANKING_REQUEST_TYPE_DEACTIVATE_CARD = "Deactivate";
    private static final Logger LOG = Logger.getLogger(CardManageService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {

        try {
            String cardNumber = requestInstance.getParameter("cardNumber");
            String cardAction = requestInstance.getParameter("cardAction");
            String actionReason = requestInstance.getParameter("actionReason");
            String customerUsername = requestInstance.getParameter("customerUsername");

            Result processedResult = new Result();

            ErrorCodeEnum errorInformation = null;

            if (StringUtils.isBlank(cardNumber)) {
                errorInformation = ErrorCodeEnum.ERR_20534;
            } else if (StringUtils.isBlank(customerUsername)) {
                errorInformation = ErrorCodeEnum.ERR_20533;
            }

            if (errorInformation != null) {
                errorInformation.setErrorCode(processedResult);
                return processedResult;
            }
            return manageCustomerCard(requestInstance, cardNumber, customerUsername, cardAction, actionReason);
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

    /**
     * Method to perform the CSR actions on the Customer's card
     * 
     * @param requestInstance
     * @param cardID
     * @param cardAction
     * @param actionReason
     * @param authToken
     * @return
     */
    private Result manageCustomerCard(DataControllerRequest requestInstance, String cardNumber, String customerUsername,
            String cardAction, String actionReason) {

        Result cardOperationResult = new Result();

        cardAction = getCardOperationString(cardAction);

        CustomerHandler customerHandler = new CustomerHandler();
        String customerId = customerHandler.getCustomerId(customerUsername, requestInstance);

        JSONObject updateCardStatusResponseJSON;
        try {
            updateCardStatusResponseJSON = DBPServices.updateCustomerCardStatus(requestInstance, cardNumber,
                    customerUsername, cardAction, actionReason);
        } catch (DBPAuthenticationException e) {
            e.getErrorCodeEnum().setErrorCode(cardOperationResult);
            return cardOperationResult;
        }

        if (updateCardStatusResponseJSON != null && updateCardStatusResponseJSON.has(ErrorCodeEnum.ERROR_CODE_KEY)) {
            ErrorCodeEnum.ERR_20698.setErrorCode(cardOperationResult);
            return cardOperationResult;
        }

        if (updateCardStatusResponseJSON != null && updateCardStatusResponseJSON.has(FabricConstants.OPSTATUS)
                && updateCardStatusResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
            cardOperationResult.addParam(new Param("status", "Status updated succesfully", FabricConstants.STRING));
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL,
                    "CARD:" + cardNumber + " ACTION:" + cardAction + "Customer id: " + customerId);
        } else {
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.CUSTOMERS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED,
                    "CARD:" + cardNumber + " ACTION:" + cardAction + "Customer id: " + customerId);
            ErrorCodeEnum.ERR_20698.setErrorCode(cardOperationResult);
            return cardOperationResult;
        }
        return cardOperationResult;
    }

    /**
     * Method to map the Card Action to the appropriate value as honored by Core Banking
     * 
     * @param cardAction
     * @return
     */
    private String getCardOperationString(String cardAction) {
        String backendCardAction;
        switch (cardAction) {
            case "LOCK_CARD":
                backendCardAction = COREBANKING_REQUEST_TYPE_LOCK_CARD;
                break;
            case "LOST_STOLEN_CARD":
                backendCardAction = COREBANKING_REQUEST_TYPE_LOST_STOLEN_CARD;
                break;
            case "ACTIVATE_CARD":
                backendCardAction = COREBANKING_REQUEST_TYPE_ACTIVATE_CARD;
                break;
            case "DEACTIVATE_CARD":
                backendCardAction = COREBANKING_REQUEST_TYPE_DEACTIVATE_CARD;
                break;
            default:
                backendCardAction = "INVALID";
                break;
        }
        return backendCardAction;
    }

}