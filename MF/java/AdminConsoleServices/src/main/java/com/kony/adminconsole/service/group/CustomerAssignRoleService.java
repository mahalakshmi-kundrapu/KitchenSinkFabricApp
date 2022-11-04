package com.kony.adminconsole.service.group;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.handler.MultipartPayloadHandler;
import com.kony.adminconsole.commons.handler.MultipartPayloadHandler.FormItem;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.config.EnvironmentConfiguration;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.dto.EmailHandlerBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.EmailHandler;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to assign Customers to a Role
 * 
 * @author Sai Krishna Aitha
 *
 */
public class CustomerAssignRoleService implements JavaService2 {

    private static final int CUSTOMERS_BATCH_SIZE = 10;
    private static final String CSV_FILE_EXTENSION = "csv";
    private static final String EMAIL_TEMPLATE_PATH = "emailTemplates/UploadCustomerForaRole.html";
    private static final Logger LOG = Logger.getLogger(CustomerAssignRoleService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        BufferedReader br = null;

        try {

            Result processedResult = new Result();

            // Validate Request Content Type
            if (MultipartPayloadHandler.isMultipartRequest(requestInstance) == false) {
                throw new ApplicationException(ErrorCodeEnum.ERR_20001);
            }

            // Read Multi-part Request
            List<FormItem> formItems = null;
            formItems = MultipartPayloadHandler.handleMultipart(requestInstance);
            ImportCustomersRequest importCustomersRequest = getRequest(formItems);
            File file = importCustomersRequest.getImportedFile();
            String roleId = importCustomersRequest.getRoleId();
            String userId = importCustomersRequest.getUserId();
            // Validate File
            if (file == null) {
                LOG.error("No File received");
                throw new ApplicationException(ErrorCodeEnum.ERR_20627);
            }
            String fileExtension = CommonUtilities.getFileExtension(file);
            if (!StringUtils.equals(fileExtension, CSV_FILE_EXTENSION)) {
                LOG.error("Uploaded File is not a CSV");
                throw new ApplicationException(ErrorCodeEnum.ERR_20628);
            }

            // update customer assign role status
            setEntityStatus(roleId, "0", requestInstance);

            // Fetch logged-in user details
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);

            // Validate Role Id
            Map<String, String> queryMap = new HashMap<String, String>();
            queryMap.put(ODataQueryConstants.FILTER, "id eq '" + roleId + "'");
            String serviceResponse = Executor.invokeService(ServiceURLEnum.MEMBERGROUP_READ, queryMap, null,
                    requestInstance);
            JSONObject roleReadResponse = CommonUtilities.getStringAsJSONObject(serviceResponse);
            if (roleReadResponse == null || !roleReadResponse.has(FabricConstants.OPSTATUS)
                    || roleReadResponse.getInt(FabricConstants.OPSTATUS) != 0
                    || roleReadResponse.optJSONArray("membergroup") == null) {
                LOG.error("Failed to read membergroup");
                throw new ApplicationException(ErrorCodeEnum.ERR_20404);
            }
            if (roleReadResponse.optJSONArray("membergroup").length() == 0) {
                LOG.error("Invalid Role Id");
                throw new ApplicationException(ErrorCodeEnum.ERR_20510);
            }
            JSONArray roleArray = roleReadResponse.optJSONArray("membergroup");
            JSONObject currJson = roleArray.optJSONObject(0);
            String roleName = currJson.optString("Name");
            // Load CSV File into a BufferedReader
            LOG.debug("Loading csv file into buffer reader");
            String line = StringUtils.EMPTY;
            br = new BufferedReader(new FileReader(file));
            @SuppressWarnings("unused") // Skip Header Line
            String headerLine = br.readLine();

            // Traverse CSV File
            int index = 0, countOfCustomers = 0;
            String currentCustomerId = StringUtils.EMPTY;
            // Set<String> processedCustomerRecords = new HashSet<>();
            List<String> currentCustomerIds = new ArrayList<>();
            List<String> failedCustomerIds = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {

                    if (line.contains(",")) {
                        // Extract first element from the row
                        currentCustomerId = StringUtils.trim(line.substring(0, line.indexOf(",")));
                    } else {
                        // Only one element present in the row
                        currentCustomerId = StringUtils.trim(line);
                    }

                    // Check if the customer record has already been processed
                    if (StringUtils.isBlank(currentCustomerId)) {
                        continue;
                    }
                    index++;

                    // Add Customer Id to the current list
                    currentCustomerIds.add(currentCustomerId);
                    countOfCustomers++;
                    if (index == CUSTOMERS_BATCH_SIZE) {
                        // Assign customers by processing current list
                        failedCustomerIds
                                .addAll(assignCustomersToRole(currentCustomerIds, roleId, userId, requestInstance));
                        currentCustomerIds.clear();
                        index = 0;
                    }
                }
            }

            // Assign remaining customers
            failedCustomerIds.addAll(assignCustomersToRole(currentCustomerIds, roleId, userId, requestInstance));

            // Compute Failed Count of Customers
            int failedCountOfCustomers = failedCustomerIds.size();
            int successCount = countOfCustomers - failedCountOfCustomers;
            LOG.debug("Failed Count of Customers:" + failedCountOfCustomers);

            // Send Email notification

            sendEmailNotificationWithUploadStatus(loggedInUserDetails, file, roleName, countOfCustomers, successCount,
                    failedCountOfCustomers, failedCustomerIds, requestInstance, processedResult);
            // Return service result
            processedResult.addParam(
                    new Param("TotalCountofCustomers", Integer.toString(countOfCustomers), FabricConstants.STRING));
            processedResult.addParam(
                    new Param("SuccessCountofCustomers", Integer.toString(successCount), FabricConstants.STRING));
            processedResult.addParam(new Param("Failed Ids", failedCustomerIds.toString(), FabricConstants.STRING));
            setEntityStatus(roleId, "1", requestInstance);
            return processedResult;
        } catch (FileSizeLimitExceededException fslee) {
            Result errorResult = new Result();
            LOG.error("FileSizeLimitExceededException found:" + fslee);
            ErrorCodeEnum.ERR_20563.setErrorCode(errorResult);
            return errorResult;
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("ApplicationException found:" + e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.error("Exception:" + e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        } finally {
            br.close();
        }
    }

    private void setEntityStatus(String roleId, String status, DataControllerRequest requestInstance)
            throws ApplicationException {

        // Read Customer Assign Role Status
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put(ODataQueryConstants.FILTER, "Entity_id eq '" + roleId + "'");
        requestMap.put(ODataQueryConstants.SELECT, "Entity_id");
        String serviceResponse = Executor.invokeService(ServiceURLEnum.ENTITYSTATUS_READ, requestMap, null,
                requestInstance);
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || serviceResponseJSON.optJSONArray("entitystatus") == null) {
            LOG.error("Failed to read customerassignrolestatus");
            throw new ApplicationException(ErrorCodeEnum.ERR_20431);
        }

        requestMap.clear();
        requestMap.put("Entity_id", roleId);
        requestMap.put("status", status);
        ServiceURLEnum serviceURL;
        ErrorCodeEnum errorCode;
        if (serviceResponseJSON.optJSONArray("entitystatus").length() == 0) {
            // Status info of current role does not exist. create a new record in table
            errorCode = ErrorCodeEnum.ERR_20432;
            serviceURL = ServiceURLEnum.ENTITYSTATUS_CREATE;
        } else {
            // Status info of current role does exist. update existing record in table
            errorCode = ErrorCodeEnum.ERR_20433;
            serviceURL = ServiceURLEnum.ENTITYSTATUS_UPDATE;
        }

        // Check service status
        serviceResponse = Executor.invokeService(serviceURL, requestMap, null, requestInstance);
        serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        LOG.debug(serviceResponseJSON);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Failed CRUD Operation:" + serviceURL.name());
            throw new ApplicationException(errorCode);
        }
    }

    @SuppressWarnings("unused")
    private void sendEmailNotificationWithUploadStatus(UserDetailsBean loggedInUserDetails, File file, String rolename,
            int countOfCustomers, int successCountOfCustomers, int failedCountOfCustomers,
            List<String> failedCustomerIds, DataControllerRequest requestInstance, Result processedResult) {

        String loggedInUserId = loggedInUserDetails.getUserId();
        String firstName = loggedInUserDetails.getFirstName();
        String lastName = loggedInUserDetails.getLastName();
        String emailId = loggedInUserDetails.getEmailId();
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("_"));

        String emailContent = StringUtils.EMPTY;
        String emailSubject = "File import Complete:" + fileName + ".csv";
        try {
            InputStream templateStream = this.getClass().getClassLoader().getResourceAsStream(EMAIL_TEMPLATE_PATH);
            emailContent = IOUtils.toString(templateStream, StandardCharsets.UTF_8);

        } catch (IOException e) {
            LOG.error("Exception while reading UploadCustomerForaRole.html file", e);
            ErrorCodeEnum.ERR_20434.setErrorCode(processedResult);
            return;
        }

        emailContent = emailContent.replaceAll("%firstname%", firstName);
        emailContent = emailContent.replaceAll("%filename%", fileName);
        emailContent = emailContent.replaceAll("%Rolename%", rolename);
        emailContent = emailContent.replaceAll("%TotalCustomers%", String.valueOf(countOfCustomers));
        emailContent = emailContent.replaceAll("%SuccessCount%", String.valueOf(successCountOfCustomers));
        emailContent = emailContent.replaceAll("%FailedCount%", String.valueOf(failedCountOfCustomers));
        emailContent = emailContent.replaceAll("%FailedCustomerIDs%", failedCustomerIds.toString());

        String emailTemplateLogo = EnvironmentConfiguration.AC_EMAIL_TEMPLATE_LOGO_URL.getValue(requestInstance);
        emailContent = emailContent.replaceAll("emailTemplateLogo", emailTemplateLogo);

        LOG.debug(emailContent);
        EmailHandlerBean emailHandlerBean = new EmailHandlerBean();
        emailHandlerBean.setSubject(emailSubject);
        emailHandlerBean.setBody(emailContent);
        emailHandlerBean.setRecipientEmailId(emailId);
        emailHandlerBean.setFirstName(firstName);
        emailHandlerBean.setLastName(lastName);

        JSONObject enrollKMSResponse = EmailHandler.enrolKMSUser(emailHandlerBean, requestInstance);
        LOG.debug(enrollKMSResponse);
        JSONObject emailResponse = EmailHandler.sendEmailToSingleRecipient(emailHandlerBean, requestInstance);
        LOG.debug(emailResponse);
    }

    /**
     * Method to assign the role to the list of Customers
     * 
     * @param customerIds
     * @param roleId
     * @param requestInstance
     * @return Ids of Customers for which the role assignment could not be done
     * @throws ApplicationException
     */
    private List<String> assignCustomersToRole(List<String> customerIds, String roleId, String userId,
            DataControllerRequest requestInstance) throws ApplicationException {

        List<String> failedRecords = new ArrayList<>();

        if (customerIds != null && !customerIds.isEmpty()) {
            StringBuilder customerIdsBuffer = new StringBuilder();

            // Append CustomerIds to Buffer
            for (String customerId : customerIds) {
                customerIdsBuffer.append(customerId).append(",");
            }

            // Prepare Input Map
            Map<String, String> inputMap = new HashMap<>();
            String customerIdsStr = customerIdsBuffer.toString();
            customerIdsStr = CommonUtilities.replaceLastOccuranceOfString(customerIdsStr, ",", StringUtils.EMPTY);
            inputMap.put("_customerIds", customerIdsStr);
            inputMap.put("_groupId", roleId);
            inputMap.put("_userId", userId);
            inputMap.put("_numberOfRows", Integer.toString(customerIds.size()));

            // Execute Service
            String operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMER_GROUP_PROC_SERVICE, inputMap,
                    null, requestInstance);

            // Check Service Success state
            JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
            if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                    || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                    || !operationResponseJSON.has("records")) {
                LOG.error("Failed CRUD Operation");
                throw new ApplicationException(ErrorCodeEnum.ERR_20509);
            }
            LOG.debug("Succesful CRUD Operation");

            // Traverse failed records
            JSONArray records = operationResponseJSON.optJSONArray("records");
            if (records != null && records.length() > 0) {

                JSONObject record = records.optJSONObject(0);
                if (record != null && record.has("FAILED_RECORDS")) {

                    // Comma separated list of Customer Ids for which the insert/update failed
                    String failedRecordsVal = record.optString("FAILED_RECORDS");

                    if (StringUtils.isNotBlank(failedRecordsVal)) {
                        if (StringUtils.contains(failedRecordsVal, ",")) {
                            // Multiple Failed Records
                            failedRecords = Arrays.asList(failedRecordsVal.split(","));
                        } else {
                            // Single Failed Record
                            failedRecords.add(failedRecordsVal);
                        }
                    }
                }
            }
        }
        return failedRecords;
    }

    private ImportCustomersRequest getRequest(List<FormItem> formItems) {
        ImportCustomersRequest importCustomersRequest = new ImportCustomersRequest();
        if (formItems != null && !formItems.isEmpty()) {
            for (FormItem formItem : formItems) {
                if (formItem.isFile()) {
                    importCustomersRequest.setImportedFile(formItem.getFile());
                }
                if (StringUtils.equals(formItem.getParamName(), "roleId")) {
                    importCustomersRequest.setRoleId(formItem.getParamValue());
                }
                if (StringUtils.equals(formItem.getParamName(), "User_id")) {
                    importCustomersRequest.setUserId(formItem.getParamValue());
                }
            }
        }
        return importCustomersRequest;
    }

    class ImportCustomersRequest {
        private String roleId;
        private File importedFile;
        private String userId;

        public String getRoleId() {
            return roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }

        public File getImportedFile() {
            return importedFile;
        }

        public void setImportedFile(File importedFile) {
            this.importedFile = importedFile;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

    }

}
