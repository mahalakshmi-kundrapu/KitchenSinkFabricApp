package com.kony.logservices.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import com.kony.adminconsole.commons.handler.JSONPayloadHandler;
import com.kony.adminconsole.commons.utils.JSONUtils;
import com.kony.logservices.core.AbstractLogJavaService;
import com.kony.logservices.core.BaseActivity;
import com.kony.logservices.dao.LogDAO;
import com.kony.logservices.dto.AdminActivityDTO;
import com.kony.logservices.dto.AdminCustomerActivityDTO;
import com.kony.logservices.dto.CustomerActivityDTO;
import com.kony.logservices.dto.TransactionActivityDTO;
import com.kony.logservices.util.ErrorCodeEnum;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * AuditUserAndCustomerLogs service is used to audit activities done by customer and CSR.
 * 
 * @author Alahari Prudhvi Akhil (KH2346)
 * 
 */
public class AuditUserAndCustomerLogs extends AbstractLogJavaService {

    private static final Logger LOG = Logger.getLogger(AuditUserAndCustomerLogs.class);

    @Override
    public Object execute(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();
        try {

            // Construct model based on the request payload
            BaseActivity activityModel = JSONPayloadHandler.parseLog(BaseActivity.class, requestInstance);
            if (activityModel == null) {
                ErrorCodeEnum.ERR_29001.setErrorCode(processedResult);
                return processedResult;
            }

            processedResult
                    .addParam(new Param("ModelReceived", JSONUtils.stringify(activityModel), FabricConstants.STRING));

            // Audit log
            LogDAO.saveLog(activityModel);
            processedResult.addParam(new Param("OperationStatus", "Success", FabricConstants.STRING));
        } catch (Exception e) {
            LOG.error("Unexpected error occured while saving the log", e);

            // Construct Sample payload
            processedResult.addParam(new Param("SamplePayload", constructSamplePayload(), FabricConstants.STRING));
            // Obtain the error message
            processedResult.addParam(new Param("ExceptionMessage", e.getMessage(), FabricConstants.STRING));
            processedResult.addParam(new Param("OperationStatus", "Failed", FabricConstants.STRING));
            ErrorCodeEnum.ERR_29002.setErrorCode(processedResult);
        }

        return processedResult;
    }

    /**
     * Constructs a sample payload
     * 
     * 
     * @return String
     */
    public String constructSamplePayload() {
        AdminActivityDTO adminActivityDTO = new AdminActivityDTO();
        adminActivityDTO.setUsername("admin2");
        adminActivityDTO.setUserRole("Super Admin");
        adminActivityDTO.setEvent("Update");
        adminActivityDTO.setModuleName("FAQ");
        adminActivityDTO.setDescription("Updating FAQ");
        adminActivityDTO.setEventts(new Date());

        AdminCustomerActivityDTO adminCustomerActivityDTO = new AdminCustomerActivityDTO();
        adminCustomerActivityDTO.setAdminName("John");
        adminCustomerActivityDTO.setAdminRole("Super Admin");
        adminCustomerActivityDTO.setCustomerId("olbuser");
        adminCustomerActivityDTO.setDescription("Updated customer email address");
        adminCustomerActivityDTO.setActivityType("Update");
        adminCustomerActivityDTO.setEventts(new Date());

        CustomerActivityDTO customerActivityDTO = new CustomerActivityDTO();
        customerActivityDTO.setActivityType("Update address");
        customerActivityDTO.setChannel("Mobile");
        customerActivityDTO.setDescription("Updating address");
        customerActivityDTO.setDevice("Moto g5");
        customerActivityDTO.setErrorCode("0");
        customerActivityDTO.setEventts(new Date());
        customerActivityDTO.setOperatingSystem("Android");
        customerActivityDTO.setIpAddress("127.0.0.1");
        customerActivityDTO.setModuleName("Customer");
        customerActivityDTO.setReferenceId("111");
        customerActivityDTO.setStatus("Done");
        customerActivityDTO.setUsername("olbuser");

        TransactionActivityDTO transactionActivityDTO = new TransactionActivityDTO();
        transactionActivityDTO.setAmount(new BigDecimal("72.23"));
        transactionActivityDTO.setBatchId("AFI8765H909M89");
        transactionActivityDTO.setCurrencyCode("USD");
        transactionActivityDTO.setDescription("Domestic Wire Transfer");
        transactionActivityDTO.setFromAccount("0532013000213542");
        transactionActivityDTO.setFromAccountType("Savings");
        transactionActivityDTO.setFromMobileOrEmail("malachi_mitchel@hotmail.com");
        transactionActivityDTO.setInternationalRoutingCode("DE89 3704 0044 0532 0130 05 ");
        transactionActivityDTO.setPayeeName("Kony Dev");
        transactionActivityDTO.setRoutingNumber("AFI8765H909F4");
        transactionActivityDTO.setStatus("Posted");
        transactionActivityDTO.setSwiftCode("CHASUS33");
        transactionActivityDTO.setToAccount("0532013000213556");
        transactionActivityDTO.setToMobileOrEmail("malachi_mitchel@hotmail.com");
        transactionActivityDTO.setToAccountType("Current");
        transactionActivityDTO.setTransactionDate(new Date());
        transactionActivityDTO.setTransactionId("VADE0B2489321");
        transactionActivityDTO.setType("Domestic");
        transactionActivityDTO.setUsername("john.bailey");

        try {
            return "AdminActivityDTO => " + JSONUtils.stringify(adminActivityDTO) + " AdminCustomerActivityDTO => "
                    + JSONUtils.stringify(adminCustomerActivityDTO) + " CustomerActivityDTO => "
                    + JSONUtils.stringify(customerActivityDTO) + " TransactionActivityDTO => "
                    + JSONUtils.stringify(transactionActivityDTO);
        } catch (IOException e) {
            LOG.error("Exception", e);
            return null;
        }
    }

}