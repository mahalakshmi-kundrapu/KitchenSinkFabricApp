package com.kony.adminconsole.service.locationsandlocationservices;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to manage the Location, their schedules and their corresponding Location Records(Create,Update,Delete)
 *
 * @author Aditya Mankal
 * 
 */
public class LocationsAndServicesManageService implements JavaService2 {

    private static final Logger LOG = Logger.getLogger(LocationsAndServicesManageService.class);

    private static final String DELETE_LOCATION_METHOD_NAME = "DeleteLocation";
    private static final String UPDATE_LOCATION_AND_LOCATION_SERVICES_METHOD_NAME = "UpdateLocationAndLocationServices";

    private static final int PHONE_NUMBER_MAX_DIGITS = 21;
    private static final int LOCATION_CODE_MAX_CHARS = 10;
    private static final int LOCATION_NAME_MAX_CHARS = 100;
    private static final int LOCATION_DISPLAY_NAME_MAX_CHARS = 100;
    private static final int LOCATION_DESCRIPTION_MAX_CHARS = 200;
    private static final int ADDRESS_MAX_CHARS = 80;

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        try {

            LOG.debug("Method Id:" + methodID);

            // Fetch Logged-in User Details
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            String loggedInUserId = userDetailsBeanInstance.getUserId();

            // Read Input Parameters
            String locationId = requestInstance.getParameter("Location_id");
            String locationStatusId = requestInstance.getParameter("Status_id");
            String locationOfflineOnlineStatus = requestInstance.getParameter("Location_OfflineOnlineStatus");
            String locationDetailsString = requestInstance.getParameter("Location_Details");
            String serviceDetailsString = requestInstance.getParameter("Service_Details");
            String addressDetailsString = requestInstance.getParameter("Address_Details");
            String scheduleDetailsString = requestInstance.getParameter("Schedule_Details");
            String customerSegmentString = requestInstance.getParameter("CustomerSegment_Details");
            String supportedCurrencyString = requestInstance.getParameter("SupportedCurrency_Details");
            String workScheduleId = null, addressId = null;

            JSONObject addressDetailsJSONObject = CommonUtilities.getStringAsJSONObject(addressDetailsString);
            JSONObject scheduleDetailsJSONObject = CommonUtilities.getStringAsJSONObject(scheduleDetailsString);
            JSONObject locationDetailsJSONObject = CommonUtilities.getStringAsJSONObject(locationDetailsString);
            JSONObject serviceDetailsJSONObject = CommonUtilities.getStringAsJSONObject(serviceDetailsString);
            JSONArray customerSegmentJSONArray = CommonUtilities.getStringAsJSONArray(customerSegmentString);
            JSONArray supportedCurrencyJSONArray = CommonUtilities.getStringAsJSONArray(supportedCurrencyString);

            Result processedResult = new Result();
            boolean isCreateRequest = false;

            if (StringUtils.equalsIgnoreCase(methodID, UPDATE_LOCATION_AND_LOCATION_SERVICES_METHOD_NAME)) {
                // Update Request

                // Validate Location Id
                if (StringUtils.isBlank(locationId)) {
                    ErrorCodeEnum.ERR_20363.setErrorCode(processedResult);
                    Param errorParam = new Param("validationError", "Location ID cannot be null.",
                            FabricConstants.STRING);
                    processedResult.addParam(errorParam);
                    return processedResult;
                }

                if (StringUtils.isNotBlank(locationOfflineOnlineStatus)) {
                    // Request is to Toggle Location Status.
                    LOG.debug("Request is to Toggle Location Status. Proceeding Further..");
                    processedResult.addRecord(toggleLocationStatus(locationId, locationOfflineOnlineStatus,
                            loggedInUserId, requestInstance));
                    return processedResult;
                }

                // Validate Work Schedule Id
                if (scheduleDetailsJSONObject != null) {
                    workScheduleId = scheduleDetailsJSONObject.optString("WorkScheduleID");
                    if (StringUtils.isBlank(workScheduleId)) {
                        ErrorCodeEnum.ERR_20363.setErrorCode(processedResult);
                        Param errorParam = new Param("validationError", " Work Schedule ID cannot be null.",
                                FabricConstants.STRING);
                        processedResult.addParam(errorParam);
                        return processedResult;
                    }
                }

                // Validate Address Id
                if (addressDetailsJSONObject != null) {
                    addressId = addressDetailsJSONObject.optString("AddressID");
                    if (StringUtils.isBlank(addressId)) {
                        ErrorCodeEnum.ERR_20363.setErrorCode(processedResult);
                        Param errorParam = new Param("validationError", " Address ID cannot be null.",
                                FabricConstants.STRING);
                        processedResult.addParam(errorParam);
                        return processedResult;
                    }

                }

            } else if (StringUtils.equalsIgnoreCase(methodID, DELETE_LOCATION_METHOD_NAME)) {
                // Deactivate Location
                if (StringUtils.isBlank(locationId)) {
                    ErrorCodeEnum.ERR_20363.setErrorCode(processedResult);
                    Param errorParam = new Param("Error: Invalid Input. Location ID cannot be null.", "ErrorMessage",
                            FabricConstants.STRING);
                    processedResult.addParam(errorParam);
                    return processedResult;
                }
                processedResult
                        .addRecord(toggleLocationStatus(locationId, "Deactivate", loggedInUserId, requestInstance));
                // Deactivating location when delete location is called.
                return processedResult;

            } else {
                // Create Request. Generate Random ID values
                isCreateRequest = true;
                locationId = String.valueOf(CommonUtilities.getNumericId());
                workScheduleId = CommonUtilities.getNewId().toString();
                addressId = CommonUtilities.getNewId().toString();
            }

            // Set Location Address
            if (addressDetailsJSONObject != null) {
                Record manageLocationAddressResponseRecord = manageLocationAddress(addressDetailsJSONObject, addressId,
                        loggedInUserId, isCreateRequest, requestInstance);
                processedResult.addRecord(manageLocationAddressResponseRecord);
            }

            // Set Location Schedule
            if (scheduleDetailsJSONObject != null) {
                // Set Location Work Schedule
                Record locationWorkScheduleParam = setLocationWorkSchedule(workScheduleId, isCreateRequest,
                        scheduleDetailsJSONObject, loggedInUserId, requestInstance);
                processedResult.addRecord(locationWorkScheduleParam);

                // Set Location Day Schedule
                Record locationDayScheduleParam = setLocationDaySchedule(workScheduleId, scheduleDetailsJSONObject,
                        isCreateRequest, loggedInUserId, requestInstance);
                processedResult.addRecord(locationDayScheduleParam);
            }

            // Set Location Definition
            Record locationDefinitionRecord = setLocationDetails(locationDetailsJSONObject, locationId, addressId,
                    workScheduleId, locationStatusId, loggedInUserId, isCreateRequest, requestInstance);
            processedResult.addRecord(locationDefinitionRecord);

            // Set Location Services
            if (serviceDetailsJSONObject != null) {
                Record locationServicesRecord = setLocationServices(serviceDetailsJSONObject, locationId,
                        loggedInUserId, requestInstance);
                processedResult.addRecord(locationServicesRecord);
            }

            // Set Location Customer Segments
            if (customerSegmentString != null && locationDetailsJSONObject != null
                    && locationDetailsJSONObject.optString("Type_id").equals("Branch")) {
                Record customerSegmentsRecord = setLocationCustomerSegments(customerSegmentJSONArray, locationId,
                        loggedInUserId, requestInstance);
                processedResult.addRecord(customerSegmentsRecord);
            }

            // Set Location Currencies
            if (supportedCurrencyString != null && locationDetailsJSONObject != null
                    && locationDetailsJSONObject.optString("Type_id").equals("ATM")) {
                Record locationCurrenciesRecord = setLocationCurrencies(supportedCurrencyJSONArray, locationId,
                        loggedInUserId, requestInstance);
                processedResult.addRecord(locationCurrenciesRecord);
            }

            // Return Result
            return processedResult;
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            ErrorCodeEnum.ERR_20355.setErrorCode(errorResult);
            return errorResult;
        }
    }

    /**
     * Method to create Location Schedule
     * 
     * @param workScheduleId
     * @param loggedInUserId
     * @param weekDayStartTime
     * @param weekDayEndTime
     * @param weekEndStartTime
     * @param weekEndEndTime
     * @param listOfAddedWeekendDays
     * @param requestInstance
     * @return Status Param
     * @throws ApplicationException
     */
    private Param createLocationSchedule(String workScheduleId, String loggedInUserId, String weekDayStartTime,
            String weekDayEndTime, String weekEndStartTime, String weekEndEndTime, List<String> listOfAddedWeekendDays,
            DataControllerRequest requestInstance) throws ApplicationException {

        Param statusParam = new Param("status", "Success", FabricConstants.STRING);

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("WorkSchedule_id", workScheduleId);
        inputMap.put("createdby", loggedInUserId);
        inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

        String currDayName = StringUtils.EMPTY;
        String currDayScheduleId = StringUtils.EMPTY;
        String operationResponse = StringUtils.EMPTY;
        JSONObject operationResponseJSON = null;

        // Create Day-Schedule for 7 days of the Week
        for (DayOfWeek dayObject : DayOfWeek.values()) {
            currDayName = dayObject.name();
            currDayScheduleId = CommonUtilities.getNewId().toString();
            inputMap.put("id", currDayScheduleId);
            inputMap.put("WeekDayName", currDayName);

            if (isWeekDay(currDayName)) {
                // Weekday
                if (StringUtils.isNoneBlank(weekDayStartTime))
                    inputMap.put("StartTime", weekDayStartTime);
                if (StringUtils.isNoneBlank(weekDayEndTime))
                    inputMap.put("EndTime", weekDayEndTime);
            } else {
                // Weekend
                if (!listOfAddedWeekendDays.contains(currDayName))
                    continue;
                if (StringUtils.isNoneBlank(weekEndStartTime))
                    inputMap.put("StartTime", weekEndStartTime);
                if (StringUtils.isNoneBlank(weekEndEndTime))
                    inputMap.put("EndTime", weekEndEndTime);
            }
            operationResponse = Executor.invokeService(ServiceURLEnum.DAYSCHEDULE_CREATE, inputMap, null,
                    requestInstance);
            operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

            if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                    || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                    || !operationResponseJSON.has("dayschedule")) {
                LOG.error("Failed CRUD Operation");
                throw new ApplicationException(ErrorCodeEnum.ERR_20362);

            }
            LOG.debug("Successful CRUD Operation");
        }

        return statusParam;

    }

    /**
     * Method to set the Location Day Schedule
     * 
     * @param workScheduleId
     * @param scheduleDetailsJSONObject
     * @param isCreateRequest
     * @param loggedInUserId
     * @param requestInstance
     * @throws ApplicationException
     */
    private Record setLocationDaySchedule(String workScheduleId, JSONObject scheduleDetailsJSONObject,
            boolean isCreateRequest, String loggedInUserId, DataControllerRequest requestInstance)
            throws ApplicationException {

        Record operationRecord = new Record();
        operationRecord.setId("daySchedule");

        Param statusParam = new Param("status", "Success", FabricConstants.STRING);
        operationRecord.addParam(statusParam);

        String weekDayStartTime = scheduleDetailsJSONObject.optString("WeekDayStartTime");
        String weekDayEndTime = scheduleDetailsJSONObject.optString("WeekDayEndTime");
        String weekEndStartTime = scheduleDetailsJSONObject.optString("WeekEndStartTime");
        String weekEndEndTime = scheduleDetailsJSONObject.optString("WeekEndEndTime");

        ArrayList<String> listOfAddedWeekendDays = new ArrayList<String>();
        ArrayList<String> listOfRemovedWeekendDays = new ArrayList<String>();

        if (scheduleDetailsJSONObject.has("WeekendWorkingDays")) {
            JSONObject weekendWorkingDaysJSON = scheduleDetailsJSONObject.getJSONObject("WeekendWorkingDays");
            listOfAddedWeekendDays = CommonUtilities
                    .getStringElementsOfJSONArrayToArrayList(weekendWorkingDaysJSON.getJSONArray("AddedDays"));
            listOfRemovedWeekendDays = CommonUtilities
                    .getStringElementsOfJSONArrayToArrayList(weekendWorkingDaysJSON.getJSONArray("RemovedDays"));
        }

        Map<String, String> inputMap = new HashMap<>();
        String operationResponse = StringUtils.EMPTY;
        JSONObject operationResponseJSON = null;

        if (isCreateRequest == true) {
            // Create Location Schedule
            statusParam = createLocationSchedule(workScheduleId, loggedInUserId, weekDayStartTime, weekDayEndTime,
                    weekEndStartTime, weekEndEndTime, listOfAddedWeekendDays, requestInstance);
        } else {
            // Update Location Schedule

            inputMap.clear();
            inputMap.put(ODataQueryConstants.FILTER, "WorkSchedule_id eq '" + workScheduleId + "'");

            // Fetch Existing Day-Schedule Association
            operationResponse = Executor.invokeService(ServiceURLEnum.DAYSCHEDULE_READ, inputMap, null,
                    requestInstance);
            operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
            if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                    || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                    || !operationResponseJSON.has("dayschedule")) {
                LOG.error("Failed CRUD Operation");
                throw new ApplicationException(ErrorCodeEnum.ERR_20361);
            }

            LOG.debug("Successful CRUD Operation");

            JSONObject currDayScheduleObject = null;
            String currDayName = StringUtils.EMPTY;
            String currDayScheduleId = StringUtils.EMPTY;
            JSONArray dayscheduleArray = operationResponseJSON.getJSONArray("dayschedule");

            if (dayscheduleArray == null || dayscheduleArray.length() == 0) {
                // Indicates that no schedule has been associated. Associate Day Schedule(s)
                statusParam = createLocationSchedule(workScheduleId, loggedInUserId, weekDayStartTime, weekDayEndTime,
                        weekEndStartTime, weekEndEndTime, listOfAddedWeekendDays, requestInstance);
            } else {

                List<String> weekDays = getWeekDays();// List contains the 5 weekdays

                // Traverse Day-Schedule Array - Update Existing Day Schedule
                for (Object object : dayscheduleArray) {

                    if (object instanceof JSONObject) {

                        currDayScheduleObject = (JSONObject) object;
                        currDayScheduleId = currDayScheduleObject.optString("id");
                        currDayName = currDayScheduleObject.getString("WeekDayName");

                        inputMap.clear();
                        inputMap.put("id", currDayScheduleId);
                        inputMap.put("WorkSchedule_id", workScheduleId);

                        if (!isWeekDay(currDayName)) {

                            // Current Day is a Weekend
                            if (listOfAddedWeekendDays.contains(currDayName)) {
                                // Existing weekend. Indicates only a schedule update
                                listOfAddedWeekendDays.remove(currDayName);
                            }

                            if (listOfRemovedWeekendDays.contains(currDayName)) {
                                // Existing weekend. Indicates that it was removed
                                listOfRemovedWeekendDays.remove(currDayName);
                                Executor.invokeService(ServiceURLEnum.DAYSCHEDULE_DELETE, inputMap, null,
                                        requestInstance);
                                continue;
                            }

                            if (StringUtils.isNoneBlank(weekEndStartTime))
                                inputMap.put("StartTime", weekEndStartTime);
                            if (StringUtils.isNoneBlank(weekEndEndTime))
                                inputMap.put("EndTime", weekEndEndTime);
                        } else {
                            // Current Day is a Weekday
                            if (StringUtils.isNoneBlank(weekDayStartTime))
                                inputMap.put("StartTime", weekDayStartTime);
                            if (StringUtils.isNoneBlank(weekDayEndTime))
                                inputMap.put("EndTime", weekDayEndTime);

                        }
                        inputMap.put("WeekDayName", currDayName);
                        inputMap.put("modifiedby", loggedInUserId);
                        inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

                        operationResponse = Executor.invokeService(ServiceURLEnum.DAYSCHEDULE_UPDATE, inputMap, null,
                                requestInstance);
                        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

                        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                                || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                                || !operationResponseJSON.has("dayschedule")) {
                            LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DAYSCHEDULE_UPDATE.getServiceURL());
                            throw new ApplicationException(ErrorCodeEnum.ERR_20360);
                        }
                        LOG.error("Successful CRUD Operation:" + ServiceURLEnum.DAYSCHEDULE_UPDATE.getServiceURL());

                        weekDays.remove(currDayName);// Remove the current Day name from the list
                    }
                }

                // Handling the newly added weekend days
                if (StringUtils.isNotBlank(weekEndStartTime) && StringUtils.isNotBlank(weekEndEndTime)) {

                    for (String currWeekend : listOfAddedWeekendDays) {
                        currDayScheduleId = CommonUtilities.getNewId().toString();
                        inputMap.clear();
                        inputMap.put("id", currDayScheduleId);
                        inputMap.put("StartTime", weekEndStartTime);
                        inputMap.put("EndTime", weekEndEndTime);
                        inputMap.put("WeekDayName", currWeekend);
                        inputMap.put("WorkSchedule_id", workScheduleId);
                        inputMap.put("createdby", loggedInUserId);
                        inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                        operationResponse = Executor.invokeService(ServiceURLEnum.DAYSCHEDULE_CREATE, inputMap, null,
                                requestInstance);
                        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                                || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                                || !operationResponseJSON.has("dayschedule")) {
                            LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DAYSCHEDULE_CREATE.getServiceURL());
                            throw new ApplicationException(ErrorCodeEnum.ERR_20362);
                        }
                        LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.DAYSCHEDULE_CREATE.getServiceURL());
                    }
                }

                LOG.debug("Pending WeekDays:" + weekDays);
                // Handle the remaining Week Days - CORNER Case
                if (StringUtils.isNotBlank(weekDayStartTime) && StringUtils.isNotBlank(weekDayEndTime)) {

                    for (String currWeekDay : weekDays) {
                        currDayScheduleId = CommonUtilities.getNewId().toString();
                        inputMap.clear();
                        inputMap.put("id", currDayScheduleId);
                        inputMap.put("StartTime", weekDayStartTime);
                        inputMap.put("EndTime", weekDayEndTime);
                        inputMap.put("WeekDayName", currWeekDay);
                        inputMap.put("WorkSchedule_id", workScheduleId);
                        inputMap.put("createdby", loggedInUserId);
                        inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                        operationResponse = Executor.invokeService(ServiceURLEnum.DAYSCHEDULE_CREATE, inputMap, null,
                                requestInstance);
                        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
                        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                                || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                                || !operationResponseJSON.has("dayschedule")) {
                            LOG.error("Failed CRUD Operation:" + ServiceURLEnum.DAYSCHEDULE_CREATE.getServiceURL());
                            throw new ApplicationException(ErrorCodeEnum.ERR_20362);
                        }
                        LOG.debug("Successful CRUD Operation:" + ServiceURLEnum.DAYSCHEDULE_CREATE.getServiceURL());
                    }
                }

            }
        }
        return operationRecord;

    }

    /**
     * Method to get a List containing names of WeekDays
     * 
     * @return List containing names of WeekDays
     */
    private List<String> getWeekDays() {
        List<String> weekdays = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            if (isWeekDay(day.name())) {
                weekdays.add(day.name());
            }
        }
        return weekdays;
    }

    /**
     * Method to set the location Work Schedule
     * 
     * @param workScheduleId
     * @param isCreateRequest
     * @param scheduleDetailsJSONObject
     * @param userId
     * @param requestInstance
     * @throws ApplicationException
     */
    private Record setLocationWorkSchedule(String workScheduleId, boolean isCreateRequest,
            JSONObject scheduleDetailsJSONObject, String userId, DataControllerRequest requestInstance)
            throws ApplicationException {

        Record operationRecord = new Record();
        operationRecord.setId("workSchedule");

        Param statusParam = new Param("status", "Success", FabricConstants.STRING);
        operationRecord.addParam(statusParam);

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();

        if (scheduleDetailsJSONObject.has("WorkScheduleDescription")) {

            EventEnum operation = null;
            ErrorCodeEnum errorCode = null;
            String manageWorkScheduleResponse = StringUtils.EMPTY;

            inputMap.put("id", workScheduleId);
            inputMap.put("Description", scheduleDetailsJSONObject.optString("WorkScheduleDescription"));

            if (isCreateRequest) {
                operation = EventEnum.CREATE;
                errorCode = ErrorCodeEnum.ERR_20359;
                inputMap.put("createdby", userId);
                inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                manageWorkScheduleResponse = Executor.invokeService(ServiceURLEnum.WORKSCHEDULE_CREATE, inputMap, null,
                        requestInstance);
            } else {
                operation = EventEnum.UPDATE;
                errorCode = ErrorCodeEnum.ERR_20358;
                inputMap.put("modifiedby", userId);
                inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
                manageWorkScheduleResponse = Executor.invokeService(ServiceURLEnum.WORKSCHEDULE_UPDATE, inputMap, null,
                        requestInstance);
            }

            JSONObject manageWorkScheduleResponseJSON = CommonUtilities
                    .getStringAsJSONObject(manageWorkScheduleResponse);
            if (manageWorkScheduleResponseJSON != null && manageWorkScheduleResponseJSON.has(FabricConstants.OPSTATUS)
                    && manageWorkScheduleResponseJSON.getInt(FabricConstants.OPSTATUS) == 0) {
                statusParam.setValue("Success");
                LOG.debug("Successful CRUD Operation");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, operation,
                        ActivityStatusEnum.SUCCESSFUL, "WorkSchedule ID:" + workScheduleId);
            } else {
                LOG.error("Failed CRUD Operation");
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, operation,
                        ActivityStatusEnum.FAILED, "WorkSchedule ID:" + workScheduleId);
                throw new ApplicationException(errorCode);
            }
        }

        return operationRecord;
    }

    /**
     * Method to set the Location Details
     * 
     * @param locationDetailsJSONObject
     * @param locationId
     * @param locationAddressId
     * @param workScheduleId
     * @param locationStatusId
     * @param loggedInUserId
     * @param isCreateRequest
     * @param requestInstance
     * @return operation Record
     * @throws ApplicationException
     */
    private Record setLocationDetails(JSONObject locationDetailsJSONObject, String locationId, String locationAddressId,
            String workScheduleId, String locationStatusId, String loggedInUserId, boolean isCreateRequest,
            DataControllerRequest requestInstance) throws ApplicationException {

        Record operationRecord = new Record();
        operationRecord.setId("locationDetails");

        Param statusParam = new Param("status", "Success", FabricConstants.STRING);
        operationRecord.addParam(statusParam);

        boolean isValidLocationData = true;
        StringBuffer errorMessageBuffer = new StringBuffer();
        errorMessageBuffer.append("ERROR:");

        // Validate Inputs and Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", locationId);

        // Read Inputs
        if (locationDetailsJSONObject != null) {

            String locationName = locationDetailsJSONObject.optString("Name");
            String locationDisplayName = locationDetailsJSONObject.optString("DisplayName");
            String locationDescription = locationDetailsJSONObject.optString("Description");
            String locationTypeId = locationDetailsJSONObject.optString("Type_id");
            String locationCode = locationDetailsJSONObject.optString("Code");
            String locationEmail = locationDetailsJSONObject.optString("Email");
            String locationIsMainBranch = locationDetailsJSONObject.optString("IsMainBranch");
            String locationPhoneNumber = locationDetailsJSONObject.optString("PhoneNumber");
            String locationURL = locationDetailsJSONObject.optString("WebsiteURL");
            String locationBankType = locationDetailsJSONObject.optString("BankType");

            if (locationDescription.length() > LOCATION_DESCRIPTION_MAX_CHARS) {
                isValidLocationData = false;
                errorMessageBuffer
                        .append("Description can have a maximum of " + LOCATION_DESCRIPTION_MAX_CHARS + " characters.");
            } else {
                inputMap.put("Description", locationDescription);
            }

            if (locationPhoneNumber.length() >= PHONE_NUMBER_MAX_DIGITS) {
                isValidLocationData = false;
                errorMessageBuffer.append("Phone Number can have a maximum of " + PHONE_NUMBER_MAX_DIGITS + " digits.");
            } else {
                inputMap.put("PhoneNumber", locationPhoneNumber);
            }

            if (locationCode.length() > LOCATION_CODE_MAX_CHARS) {
                isValidLocationData = false;
                errorMessageBuffer
                        .append("Location code can have a maximum of " + LOCATION_CODE_MAX_CHARS + " characters.");
            } else {
                inputMap.put("Code", locationCode);
            }

            if (locationName.length() > LOCATION_NAME_MAX_CHARS) {
                isValidLocationData = false;
                errorMessageBuffer
                        .append("Location name can have a maximum of of " + LOCATION_NAME_MAX_CHARS + " characters.");
            } else {
                inputMap.put("Name", locationName);
            }

            if (locationDisplayName.length() > LOCATION_DISPLAY_NAME_MAX_CHARS) {
                isValidLocationData = false;
                errorMessageBuffer.append("Location Display name can have a maximum of of "
                        + LOCATION_DISPLAY_NAME_MAX_CHARS + " characters.");
            } else {
                inputMap.put("DisplayName", locationDisplayName);
            }

            if (!CommonUtilities.isValidEmailID(locationEmail)) {
                isValidLocationData = false;
                errorMessageBuffer.append("Invalid Email address format.");
            } else {
                inputMap.put("EmailId", locationEmail);
            }

            if (StringUtils.isNoneBlank(locationURL)) {
                inputMap.put("WebsiteUrl", locationURL);
            }

            if (StringUtils.isNotBlank(locationBankType)) {
                String isMobile = locationBankType.equals("Mobile") ? "1" : "0";
                inputMap.put("isMobile", isMobile);
            }

            if (!isValidLocationData) {
                LOG.debug("Invalid Location Details: Info:" + errorMessageBuffer.toString());
                throw new ApplicationException(ErrorCodeEnum.ERR_20357);

            }
            if (StringUtils.isNotBlank(locationTypeId))
                inputMap.put("Type_id", locationTypeId);
            if (StringUtils.isNotBlank(locationIsMainBranch))
                inputMap.put("IsMainBranch", locationIsMainBranch);
        }

        if (StringUtils.isNotBlank(workScheduleId))
            inputMap.put("WorkSchedule_id", workScheduleId);
        if (StringUtils.isNotBlank(locationAddressId))
            inputMap.put("Address_id", locationAddressId);
        if (StringUtils.isNotBlank(locationStatusId))
            inputMap.put("Status_id", locationStatusId);

        if (isCreateRequest) {
            inputMap.put("createdby", loggedInUserId);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
        } else {
            inputMap.put("modifiedby", loggedInUserId);
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
        }

        // Create/Update Location
        EventEnum eventEnum = null;
        String manageLocationResponse = StringUtils.EMPTY;
        if (isCreateRequest) {
            manageLocationResponse = Executor.invokeService(ServiceURLEnum.LOCATION_CREATE, inputMap, null,
                    requestInstance);
            eventEnum = EventEnum.CREATE;
        } else {
            manageLocationResponse = Executor.invokeService(ServiceURLEnum.LOCATION_UPDATE, inputMap, null,
                    requestInstance);
            eventEnum = EventEnum.UPDATE;
        }

        // Verify Operation Status
        JSONObject manageLocationResponseJSON = CommonUtilities.getStringAsJSONObject(manageLocationResponse);
        if (manageLocationResponseJSON == null || !manageLocationResponseJSON.has(FabricConstants.OPSTATUS)
                || manageLocationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            statusParam.setValue("Failure");
            LOG.error("Failed CRUD Operation");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, eventEnum,
                    ActivityStatusEnum.FAILED, "Location Id" + locationId);
        } else {
            LOG.debug("Successful CRUD Operation");
            statusParam.setValue("Success");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, eventEnum,
                    ActivityStatusEnum.SUCCESSFUL, "Location Id" + locationId);
        }
        return operationRecord;
    }

    /**
     * Method to set Location Address
     * 
     * @param addressDetailsJSONObject
     * @param addressId
     * @param loggedInUserId
     * @param isCreateRequest
     * @param requestInstance
     * @return
     * @throws ApplicationException
     */
    private Record manageLocationAddress(JSONObject addressDetailsJSONObject, String addressId, String loggedInUserId,
            boolean isCreateRequest, DataControllerRequest requestInstance) throws ApplicationException {

        Record operationRecord = new Record();
        operationRecord.setId("locationAddress");

        Param statusParam = new Param("status", "Success", FabricConstants.STRING);
        operationRecord.addParam(statusParam);

        Param addressIdParam = new Param("AddressIDParam", addressId, FabricConstants.STRING);
        operationRecord.addParam(addressIdParam);

        String locationCityId = addressDetailsJSONObject.optString("City_id");
        String locationAddressLine1 = addressDetailsJSONObject.optString("addressLine1");
        String locationAddressLine2 = addressDetailsJSONObject.optString("addressLine2");
        String locationAddressLine3 = addressDetailsJSONObject.optString("addressLine3");
        String locationZipCode = addressDetailsJSONObject.optString("zipCode");
        String locationRegionId = addressDetailsJSONObject.optString("Region_id");
        String locationLongitude = addressDetailsJSONObject.optString("longitude");
        String locationLattitude = addressDetailsJSONObject.optString("latitude");

        Map<String, String> inputMap = new HashMap<String, String>();
        inputMap.put("id", addressId);

        if (locationAddressLine1.length() > ADDRESS_MAX_CHARS) {
            LOG.error("Address can have a maximum of " + ADDRESS_MAX_CHARS + " digits.");
            throw new ApplicationException(ErrorCodeEnum.ERR_20356);
        }

        if (StringUtils.isNotBlank(locationAddressLine1)) {
            inputMap.put("addressLine1", locationAddressLine1);
        }
        if (StringUtils.isNotBlank(locationAddressLine2)) {
            inputMap.put("addressLine2", locationAddressLine2);
        }
        if (StringUtils.isNotBlank(locationAddressLine3)) {
            inputMap.put("addressLine3", locationAddressLine3);
        }
        if (StringUtils.isNotBlank(locationLattitude)) {
            inputMap.put("latitude", locationLattitude);
        }
        if (StringUtils.isNotBlank(locationLongitude)) {
            inputMap.put("logitude", locationLongitude);
        }
        if (StringUtils.isNotBlank(locationZipCode)) {
            inputMap.put("zipCode", locationZipCode);
        }
        if (StringUtils.isNotBlank(locationRegionId)) {
            inputMap.put("Region_id", locationRegionId);
        }
        if (StringUtils.isNotBlank(locationCityId)) {
            inputMap.put("City_id", locationCityId);
        }

        EventEnum eventEnum = null;
        ErrorCodeEnum errorCode;
        String manageAddressReponse = StringUtils.EMPTY;
        if (isCreateRequest) {
            eventEnum = EventEnum.CREATE;
            errorCode = ErrorCodeEnum.ERR_20353;
            inputMap.put("createdby", loggedInUserId);
            inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
            manageAddressReponse = Executor.invokeService(ServiceURLEnum.ADDRESS_CREATE, inputMap, null,
                    requestInstance);
        } else {
            eventEnum = EventEnum.UPDATE;
            errorCode = ErrorCodeEnum.ERR_20354;
            inputMap.put("modifiedby", loggedInUserId);
            inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());
            manageAddressReponse = Executor.invokeService(ServiceURLEnum.ADDRESS_UPDATE, inputMap, null,
                    requestInstance);
        }
        JSONObject manageAddressReponseJSON = CommonUtilities.getStringAsJSONObject(manageAddressReponse);
        if (manageAddressReponseJSON == null || !manageAddressReponseJSON.has(FabricConstants.OPSTATUS)
                || manageAddressReponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Failed CRUD Operation");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, eventEnum,
                    ActivityStatusEnum.FAILED, "Address Id:" + addressId);
            throw new ApplicationException(errorCode);
        } else {
            LOG.debug("Successful CRUD Operation");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, eventEnum,
                    ActivityStatusEnum.SUCCESSFUL, "Address Id:" + addressId);
        }

        return operationRecord;
    }

    /**
     * Method to set the Location Services
     * 
     * @param serviceDetailsJSONObject
     * @param locationId
     * @param userId
     * @param requestInstance
     * @return
     * @throws ApplicationException
     */
    private Record setLocationServices(JSONObject serviceDetailsJSONObject, String locationId, String userId,
            DataControllerRequest requestInstance) throws ApplicationException {

        Record operationRecord = new Record();
        operationRecord.setId("locationServices");

        String currServiceId = StringUtils.EMPTY;
        String operationResponse = StringUtils.EMPTY;
        JSONObject operationResponseJSON = null;

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("Location_id", locationId);

        // Traverse List of Added Services
        JSONArray listOfAddedServices = serviceDetailsJSONObject.optJSONArray("AddedServices");
        if (listOfAddedServices != null && listOfAddedServices.length() > 0) {
            inputMap.put("createdby", userId);

            for (int indexVar = 0; indexVar < listOfAddedServices.length(); indexVar++) {
                currServiceId = listOfAddedServices.optString(indexVar);
                inputMap.put("facility_id", currServiceId);
                inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
                operationResponse = Executor.invokeService(ServiceURLEnum.LOCATIONFACILITY_CREATE, inputMap, null,
                        requestInstance);
                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    LOG.error("Failed CRUD Operation");
                    throw new ApplicationException(ErrorCodeEnum.ERR_20351);
                } else {
                    LOG.error("Successful CRUD Operation");
                }

            }

        }

        // Traverse List of Removed Services
        inputMap.clear();
        inputMap.put("Location_id", locationId);
        JSONArray listOfRemovedServices = serviceDetailsJSONObject.optJSONArray("RemovedServices");
        if (listOfRemovedServices != null & listOfRemovedServices.length() > 0) {

            for (int indexVar = 0; indexVar < listOfRemovedServices.length(); indexVar++) {
                currServiceId = listOfRemovedServices.optString(indexVar);
                inputMap.put("facility_id", currServiceId);
                operationResponse = Executor.invokeService(ServiceURLEnum.LOCATIONFACILITY_DELETE, inputMap, null,
                        requestInstance);
                operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

                if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                        || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                    LOG.error("Failed CRUD Operation");
                    throw new ApplicationException(ErrorCodeEnum.ERR_20352);
                } else {
                    LOG.error("Successful CRUD Operation");
                }

            }
        }

        operationRecord.addParam(new Param("status", "Success", FabricConstants.STRING));
        return operationRecord;
    }

    /**
     * Method to set the Customer Location Segment
     * 
     * @param customersegmentJSONArray
     * @param locationId
     * @param userId
     * @param requestInstance
     * @return operation Record
     * @throws ApplicationException
     */
    private Record setLocationCustomerSegments(JSONArray customersegmentJSONArray, String locationId, String userId,
            DataControllerRequest requestInstance) throws ApplicationException {

        Record operationRecord = new Record();
        operationRecord.setId("setLocationCustomerSegment");
        operationRecord.addParam(new Param("Status", "Success", FabricConstants.STRING));

        String customersegmentName = StringUtils.EMPTY;
        String operationResponse = StringUtils.EMPTY;
        JSONObject operationResponseJSON = null;
        Map<String, String> inputMap = new HashMap<>();

        operationResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERSEGMENT_READ, new HashMap<String, String>(),
                null, requestInstance);
        operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        Map<String, String> customersegmentMap = new HashMap<>();
        if (operationResponseJSON != null && operationResponseJSON.has(FabricConstants.OPSTATUS)
                && operationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && operationResponseJSON.has("customersegment")) {
            LOG.debug("Successful CRUD Operation");
            JSONArray readResponseJSONArray = operationResponseJSON.getJSONArray("customersegment");
            for (int i = 0; i < readResponseJSONArray.length(); ++i) {
                customersegmentMap.put(readResponseJSONArray.getJSONObject(i).optString("type"),
                        readResponseJSONArray.getJSONObject(i).optString("id"));
            }
        } else {
            LOG.debug("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_20349);
        }

        for (int indexVar = 0; indexVar < customersegmentJSONArray.length(); indexVar++) {
            inputMap.clear();
            customersegmentName = customersegmentJSONArray.get(indexVar).toString();
            inputMap.put("Location_id", locationId);
            inputMap.put("segment_id", customersegmentMap.get(customersegmentName));

            operationResponse = Executor.invokeService(ServiceURLEnum.LOCATIONCUSTOMERSEGMENT_CREATE, inputMap, null,
                    requestInstance);
            operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

            if (operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "Failed to map Customersegment and Location. LocationID: "
                                + locationId + " segment_id: " + customersegmentMap.get(customersegmentName));
                throw new ApplicationException(ErrorCodeEnum.ERR_20347);
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.CREATE,
                        ActivityStatusEnum.SUCCESSFUL, "Successfully mapped Customersegment and Location. LocationID: "
                                + locationId + " segment_id: " + customersegmentMap.get(customersegmentName));
            }
        }

        return operationRecord;
    }

    /**
     * Method to set the Location Currencies
     * 
     * @param currencyJSONArray
     * @param locationID
     * @param userID
     * @param requestInstance
     * @return
     * @throws ApplicationException
     */
    private Record setLocationCurrencies(JSONArray currencyJSONArray, String locationID, String userID,
            DataControllerRequest requestInstance) throws ApplicationException {

        Record operationRecord = new Record();
        operationRecord.setId("setLocationCurrency");
        operationRecord.addParam(new Param("Status", "Success", FabricConstants.STRING));

        String operationResponse = Executor.invokeService(ServiceURLEnum.CURRENCY_READ, new HashMap<String, String>(),
                null, requestInstance);
        JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

        Map<String, String> inputMap = new HashMap<>();
        if (operationResponseJSON != null && operationResponseJSON.has(FabricConstants.OPSTATUS)
                && operationResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && operationResponseJSON.has("currency")) {
            LOG.debug("Successful CRUD Operation");
            JSONArray readResponseJSONArray = operationResponseJSON.getJSONArray("currency");
            for (int i = 0; i < readResponseJSONArray.length(); ++i) {
                inputMap.put(readResponseJSONArray.getJSONObject(i).optString("code"),
                        readResponseJSONArray.getJSONObject(i).optString("code"));
            }
        } else {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_20348);
        }

        Map<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put("Location_id", locationID);

        String currencyCode = StringUtils.EMPTY;
        for (int indexVar = 0; indexVar < currencyJSONArray.length(); indexVar++) {
            currencyCode = currencyJSONArray.get(indexVar).toString();

            postParametersMap.put("currency_code", inputMap.get(currencyCode));

            operationResponse = Executor.invokeService(ServiceURLEnum.LOCATIONCURRENCY_CREATE, postParametersMap, null,
                    requestInstance);
            operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);

            if (operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, "Failed to map Currency and Location. LocationID: " + locationID
                                + " currency_code: " + inputMap.get(currencyCode));
                throw new ApplicationException(ErrorCodeEnum.ERR_20346);
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.CREATE,
                        ActivityStatusEnum.SUCCESSFUL, "Successfully mapped Currency and Location. LocationID: "
                                + locationID + " currency_code: " + inputMap.get(currencyCode));
            }

        }

        return operationRecord;
    }

    private boolean isWeekDay(String day) {
        if (StringUtils.isBlank(day))
            return false;
        if (day.equalsIgnoreCase("SATURDAY") || day.equalsIgnoreCase("SUNDAY"))
            return false;
        return true;
    }

    /**
     * Method to toggle the Location status
     * 
     * @param locationId
     * @param locationStatus
     * @param loggedInUserId
     * @param requestInstance
     * @return operation Record
     * @throws ApplicationException
     */
    private Record toggleLocationStatus(String locationId, String locationStatus, String loggedInUserId,
            DataControllerRequest requestInstance) throws ApplicationException {

        if (StringUtils.isBlank(locationId) || StringUtils.isBlank(locationStatus)) {
            LOG.error("Location Id and Location Status are mandatory inputs to toggle Location Status");
            throw new ApplicationException(ErrorCodeEnum.ERR_20350);
        }

        Record operationRecord = new Record();
        operationRecord.setId("setLocationStatus");

        Param statusParam = new Param("status", "Success", FabricConstants.STRING);
        operationRecord.addParam(statusParam);

        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("id", locationId);
        if (StringUtils.equalsIgnoreCase(locationStatus, "Deactivate")) {
            inputMap.put("softdeleteflag", "1");
        } else {
            inputMap.put("softdeleteflag", "0");
        }
        inputMap.put("modifiedby", loggedInUserId);
        inputMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        String operationResponse = Executor.invokeService(ServiceURLEnum.LOCATION_UPDATE, inputMap, null,
                requestInstance);

        JSONObject operationResponseJSON = CommonUtilities.getStringAsJSONObject(operationResponse);
        if (operationResponseJSON == null || !operationResponseJSON.has(FabricConstants.OPSTATUS)
                || operationResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            LOG.error("Failed CRUD Operation");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.UPDATE,
                    ActivityStatusEnum.FAILED, "Location update failed. LocationID: " + locationId);
            throw new ApplicationException(ErrorCodeEnum.ERR_20350);
        } else {
            LOG.debug("Successful CRUD Operation");
            AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.UPDATE,
                    ActivityStatusEnum.SUCCESSFUL, "Location updated successful. LocationID:" + locationId);
        }

        return operationRecord;
    }

}