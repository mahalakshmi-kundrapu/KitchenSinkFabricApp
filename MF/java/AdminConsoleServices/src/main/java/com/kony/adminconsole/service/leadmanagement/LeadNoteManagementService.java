package com.kony.adminconsole.service.leadmanagement;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

/**
 * Service to manage Lead Notes
 *
 * @author Aditya Mankal
 */
public class LeadNoteManagementService implements JavaService2 {

    private static final String CREATE_LEAD_NOTE_METHOD_ID = "createLeadNote";
    private static final String FETCH_LEAD_NOTES_METHOD_ID = "fetchLeadNotes";

    private static final String LEAD_NOTES_SORT_CRITERIA = "lastmodifiedts";
    private static final String DEFAULT_SORT_ORDER = "desc";
    private static final int DEFAULT_RECORDS_PER_PAGE = 30;

    private static final Logger LOG = Logger.getLogger(LeadNoteManagementService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) {

        try {

            LOG.debug("Method Id: " + methodID);

            // Fetch Logged In User Info
            String loggedInUser = StringUtils.EMPTY;
            UserDetailsBean userDetailsBeanInstance = LoggedInUserHandler.getUserDetails(requestInstance);
            if (userDetailsBeanInstance != null) {
                loggedInUser = userDetailsBeanInstance.getUserId();
            }

            if (StringUtils.equals(methodID, CREATE_LEAD_NOTE_METHOD_ID)) {
                // Create Lead Note
                String note = StringUtils.trim(requestInstance.getParameter("note"));
                String leadId = StringUtils.trim(requestInstance.getParameter("leadId"));
                String leadStatusId = StringUtils.trim(requestInstance.getParameter("leadStatusId"));
                return createLeadNote(leadId, note, leadStatusId, loggedInUser, requestInstance);
            }

            else if (StringUtils.equals(methodID, FETCH_LEAD_NOTES_METHOD_ID)) {
                // Fetch Lead Notes
                String leadId = StringUtils.trim(requestInstance.getParameter("leadId"));
                String leadStatusId = StringUtils.trim(requestInstance.getParameter("leadStatusId"));

                String recordsPerPage = StringUtils.trim(requestInstance.getParameter("recordsPerPage"));
                String sortOrder = StringUtils.trim(requestInstance.getParameter("sortOrder"));
                String pageNumber = StringUtils.trim(requestInstance.getParameter("pageNumber"));
                return getLeadNotes(leadId, leadStatusId, recordsPerPage, pageNumber, sortOrder, requestInstance);
            }

            return new Result();
        } catch (ApplicationException e) {
            Result errorResult = new Result();
            LOG.error("Application Exception. Checked Involved Operations. Exception Trace:", e);
            e.getErrorCodeEnum().setErrorCode(errorResult);
            return errorResult;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.error("Exception in Fetching Leads Master Data. Exception:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }

    }

    /**
     * Method to fetch the notes of a Lead
     * 
     * @param leadId
     * @param isArchivedLead
     * @param requestInstance
     * @return Result object containing Lead Notes
     * @throws ApplicationException
     */
    private Result getLeadNotes(String leadId, String leadStatusId, String recordsPerPage, String pageNumber,
            String sortOrder, DataControllerRequest requestInstance) throws ApplicationException {

        Result result = new Result();

        // Validate Input
        if (StringUtils.isBlank(leadId)) {
            LOG.error("lead Id is a mandatory input to fetch lead notes");
            ErrorCodeEnum.ERR_21515.setErrorCode(result);
            return result;
        }

        // Prepare Query Map
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("_leadId", StringUtils.isBlank(leadId) ? StringUtils.EMPTY : leadId);
        queryMap.put("_leadStatusId", StringUtils.isBlank(leadStatusId) ? StringUtils.EMPTY : leadStatusId);

        // Set sort-order and sort-criteria
        sortOrder = StringUtils.equalsAnyIgnoreCase(sortOrder, "asc", "desc") ? sortOrder : DEFAULT_SORT_ORDER;
        queryMap.put("_sortOrder", sortOrder);
        queryMap.put("_sortCriteria", LEAD_NOTES_SORT_CRITERIA);

        // Validate Records per page
        int recordsPerPageValue = DEFAULT_RECORDS_PER_PAGE;
        if (NumberUtils.isParsable(recordsPerPage) && Integer.parseInt(recordsPerPage) <= DEFAULT_RECORDS_PER_PAGE) {
            recordsPerPageValue = Integer.parseInt(recordsPerPage);
        }
        queryMap.put("_recordsPerPage", String.valueOf(recordsPerPageValue));
        // Calculate offset
        int offset = 0;
        if (NumberUtils.isParsable(pageNumber)) {
            offset = CommonUtilities.calculateOffset(Integer.parseInt(recordsPerPage), Integer.parseInt(pageNumber));
        }
        queryMap.put("_offset", String.valueOf(offset));

        // Execute Service
        String serviceResponse = Executor.invokeService(ServiceURLEnum.LEAD_NOTES_SEARCH_PROC_SERVICE, queryMap, null,
                requestInstance);
        JSONObject readLeadResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);

        // Check operation status
        if (readLeadResponseJSON == null || !readLeadResponseJSON.has(FabricConstants.OPSTATUS)
                || readLeadResponseJSON.getInt(FabricConstants.OPSTATUS) != 0 || !readLeadResponseJSON.has("records")) {
            LOG.error("Failed CRUD Operation");
            throw new ApplicationException(ErrorCodeEnum.ERR_21516);
        }
        LOG.debug("Successful CRUD Operation");

        // Prepare result
        JSONArray leadNotes = readLeadResponseJSON.optJSONArray("records");
        Dataset leadNotesDataset = CommonUtilities.constructDatasetFromJSONArray(leadNotes);
        leadNotesDataset.setId("notes");
        result.addDataset(leadNotesDataset);

        // Return success result
        return result;
    }

    /**
     * Method to create a note of a lead
     * 
     * @param leadId
     * @param note
     * @param loggedInUser
     * @param requestInstance
     * @return operation result
     * @throws ApplicationException
     */
    private Result createLeadNote(String leadId, String note, String leadStatusId, String loggedInUser,
            DataControllerRequest requestInstance) throws ApplicationException {

        // Prepare Input Map
        Map<String, String> inputMap = new HashMap<>();
        String noteId = CommonUtilities.getNewId().toString();
        inputMap.put("id", noteId);
        inputMap.put("lead_Id", leadId);
        inputMap.put("note", note);
        inputMap.put("createdby", loggedInUser);
        inputMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());

        ServiceURLEnum serviceURL = StringUtils.equals(leadStatusId, StatusEnum.SID_ARCHIVED.name())
                ? ServiceURLEnum.ARCHIVEDLEADNOTE_CREATE
                : ServiceURLEnum.LEADNOTE_CREATE;

        // Execute Service
        String serviceResponse = Executor.invokeService(serviceURL, inputMap, null, requestInstance);

        // Check operation status
        JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
        if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            // Failed operation
            LOG.error("Failed CRUD Operation. Response:" + serviceResponse);
            throw new ApplicationException(ErrorCodeEnum.ERR_21514);
        }

        // Return success result
        Result result = new Result();
        result.addParam(new Param("noteId", noteId, FabricConstants.STRING));
        result.addParam(new Param("status", "success", FabricConstants.STRING));
        return result;
    }
}
