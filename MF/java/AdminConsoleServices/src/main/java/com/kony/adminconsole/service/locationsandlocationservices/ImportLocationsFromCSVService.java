package com.kony.adminconsole.service.locationsandlocationservices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kony.adminconsole.commons.exception.InvalidFileNameException;
import com.kony.adminconsole.commons.handler.MultipartPayloadHandler;
import com.kony.adminconsole.commons.handler.MultipartPayloadHandler.FormItem;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.core.security.LoggedInUserHandler;
import com.kony.adminconsole.core.security.UserDetailsBean;
import com.kony.adminconsole.dto.CreateInBackendBean;
import com.kony.adminconsole.dto.DeleteFromBackendBean;
import com.kony.adminconsole.dto.EmailHandlerBean;
import com.kony.adminconsole.dto.ImportLocationsBean;
import com.kony.adminconsole.dto.LocationFileBean;
import com.kony.adminconsole.dto.ReadFromBackendBean;
import com.kony.adminconsole.exception.ApplicationException;
import com.kony.adminconsole.handler.AuditHandler;
import com.kony.adminconsole.handler.EmailHandler;
import com.kony.adminconsole.utilities.ActivityStatusEnum;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.EventEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ModuleNameEnum;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.kony.adminconsole.utilities.StatusEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Result;

@SuppressWarnings("all")
public class ImportLocationsFromCSVService implements JavaService2 {

    public static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s]*$");
    public static final Pattern EMAIL_ID_PATTERN = Pattern
            .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    public static final Pattern MOBILE_NO_PATTERN = Pattern.compile("^((\\+)(\\d{1,3})[\\s-]?)?(\\d{10})$");

    private static final Logger LOG = Logger.getLogger(ImportLocationsFromCSVService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result processedResult = new Result();

        UserDetailsBean userDetailsObject = LoggedInUserHandler.getUserDetails(requestInstance);
        String userId = userDetailsObject.getUserId();

        ImportLocationsBean importLocationsBean = new ImportLocationsBean();
        LocationFileBean locationFileBean = new LocationFileBean();

        File csvFile = null;
        List<FormItem> formItems = null;
        try {
            formItems = MultipartPayloadHandler.handleMultipart(requestInstance);
        } catch (FileSizeLimitExceededException fslee) {
            ErrorCodeEnum.ERR_20373.setErrorCode(processedResult);
            return processedResult;
        } catch (InvalidFileNameException ifne) {
            ErrorCodeEnum.ERR_20378.setErrorCode(processedResult);
            return processedResult;
        }

        BufferedReader br = null;

        try {

            for (FormItem formItem : formItems) {
                if (formItem.isFile()) {
                    csvFile = formItem.getFile();
                } else if (formItem.getParamName().equals("user_ID")) {
                    importLocationsBean.setUser_ID(formItem.getParamValue());
                }
            }

            importLocationsBean.setAuthToken(CommonUtilities.getAuthToken(requestInstance));

            String csvFileName = csvFile.getName();
            if (!csvFileName.substring(csvFileName.lastIndexOf(".") + 1).equals("csv")) {
                ErrorCodeEnum.ERR_20374.setErrorCode(processedResult);
                return processedResult;
            }

            br = new BufferedReader(new FileReader(csvFile));
            String titleLine = br.readLine();

            String[] actualTitleLineValues = titleLine.split(",");
            String[] formalTitleLineValues = { "Code", "Name", "Display Name", "Email ID", "Type (Branch/ATM)",
                    "Status (Active/Inactive)", "Main Branch (Yes/No)", "[If Bank] Bank Type (Physical/Mobile)",
                    "[If Bank] Customer Segment", "[If ATM] Supported Currency", "Description", "Street Address",
                    "City", "Region", "Zipcode", "Country", "Phone Number", "Latitude", "Longitude",
                    "Weekday timings from", "Weekday timings to", "Weekend (Yes/No)",
                    "[If yes] Weekend days (Saturday/Sunday)", "[If yes] Weekend timings from",
                    "[If yes] Weekend timings to", "List of facilities" };

            boolean correctTemplate = true;

            if (formalTitleLineValues.length != 26) {
                correctTemplate = false;
            }

            for (int i = 0; i < 26; ++i) {
                if (!formalTitleLineValues[i].equals(actualTitleLineValues[i])) {
                    correctTemplate = false;
                    break;
                }
            }

            StringBuilder responseCsvBuilder = new StringBuilder(); // Contains the text for response CSV file
            importLocationsBean.setResponseCsvPrinter(CSVFormat.DEFAULT.withHeader("Code", "Name", "Display Name",
                    "Email ID", "Type (Branch/ATM)", "Status (Active/Inactive)", "Main Branch (Yes/No)",
                    "[If Bank] Bank Type (Physical/Mobile)", "[If Bank] Customer Segment",
                    "[If ATM] Supported Currency", "Description", "Street Address", "City", "Region", "Zipcode",
                    "Country", "Phone Number", "Latitude", "Longitude", "Weekday timings from", "Weekday timings to",
                    "Weekend (Yes/No)", "[If yes] Weekend days (Saturday/Sunday)", "[If yes] Weekend timings from",
                    "[If yes] Weekend timings to", "List of facilities", "Status").print(responseCsvBuilder));

            if (correctTemplate == false) {
                processedResult.addParam(new Param("correctTemplate", "false", "boolean"));
            } else {
                processedResult.addParam(new Param("correctTemplate", "true", "boolean"));

                // ** IMPORT LOCATION processing begins.. **

                // -> Inserting 1 record into 'locationfile' table with status 0 <-
                locationFileBean.setId("file_" + (new Date()).getTime());
                locationFileBean.setStatus(0);

                manageLocationFile(requestInstance, userId, "create", importLocationsBean, locationFileBean);
                // -> - <-

                String line = "";

                while ((line = br.readLine()) != null) {

                    importLocationsBean.setCreateList(new ArrayList<CreateInBackendBean>());
                    importLocationsBean.setCorrectData(true);

                    String[] lineValues = line.split(",");

                    // ----- ----- ADDRESS TABLE ----- -----
                    importLocationsBean = addressTable(requestInstance, importLocationsBean, line, lineValues);
                    if (!importLocationsBean.isCorrectData())
                        continue;

                    // ----- ----- SCHEDULE TABLES ----- -----
                    importLocationsBean = scheduleTables(requestInstance, importLocationsBean, line, lineValues);
                    if (!importLocationsBean.isCorrectData())
                        continue;

                    // ----- ----- LOCATION TABLE ----- -----
                    importLocationsBean = locationTable(requestInstance, importLocationsBean, line, lineValues);
                    if (!importLocationsBean.isCorrectData())
                        continue;

                    // ----- ----- LOCATIONFACILITY TABLE ----- -----
                    importLocationsBean = locationfacilityTable(requestInstance, importLocationsBean, line, lineValues);
                    if (!importLocationsBean.isCorrectData())
                        continue;

                    // ----- ----- LOCATIONCUSTOMERSEGMENT TABLE ----- -----
                    importLocationsBean = locationcustomersegmentTable(requestInstance, importLocationsBean, line,
                            lineValues);
                    if (!importLocationsBean.isCorrectData())
                        continue;

                    // ----- ----- LOCATIONCURRENCY TABLE ----- -----
                    importLocationsBean = locationcurrencyTable(requestInstance, importLocationsBean, line, lineValues);
                    if (!importLocationsBean.isCorrectData())
                        continue;

                    boolean createSuccess = createInBackend(requestInstance, importLocationsBean, line);

                    if (createSuccess == true) {
                        importLocationsBean.setSuccessCount(importLocationsBean.getSuccessCount() + 1);
                    } else {
                        importLocationsBean.setCorrectData(false);
                        importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
                    }

                    importLocationsBean.getResponseCsvPrinter().close();
                }

                processedResult.addParam(new Param("correctTemplate", "true", "boolean"));

                if (importLocationsBean.getFailureCount() == 0) {
                    locationFileBean.setCsvContent("");
                } else {
                    locationFileBean.setCsvContent(responseCsvBuilder.toString());
                    processedResult
                            .addParam(new Param("locationFileName", locationFileBean.getId(), FabricConstants.STRING));
                }
            }
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(processedResult);
            LOG.error("ApplicationException occured in ImportLocationsFromCSVService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
            LOG.error("Exception occured in ImportLocationsFromCSVService JAVA service. Error: ", e);
        }

        try {
            manageLocationFile(requestInstance, userId, "update", importLocationsBean, locationFileBean);
            sendStatusEmail(requestInstance, userDetailsObject, importLocationsBean.getSuccessCount(),
                    importLocationsBean.getFailureCount());

            br.close();
            if (csvFile != null) {
                csvFile.delete();
            }
        } catch (ApplicationException ae) {
            ae.getErrorCodeEnum().setErrorCode(processedResult);
            LOG.error("ApplicationException occured in ImportLocationsFromCSVService JAVA service. Error: ", ae);
        } catch (Exception e) {
            ErrorCodeEnum.ERR_20001.setErrorCode(processedResult);
            LOG.error("Exception occured in ImportLocationsFromCSVService JAVA service. Error: ", e);
        }

        processedResult.addParam(new Param("successCount", Integer.toString(importLocationsBean.getSuccessCount()),
                FabricConstants.INT));
        processedResult.addParam(new Param("failureCount", Integer.toString(importLocationsBean.getFailureCount()),
                FabricConstants.INT));

        return processedResult;
    }

    public ImportLocationsBean addressTable(DataControllerRequest requestInstance,
            ImportLocationsBean importLocationsBean, String line, String[] lineValues) throws Exception {

        // ** Reading entry from 'country' table **
        Map<String, String> countryTableMap = new HashMap<String, String>();
        countryTableMap.put(ODataQueryConstants.SELECT, "id");
        countryTableMap.put(ODataQueryConstants.FILTER, "Name eq '" + lineValues[15] + "'");

        ReadFromBackendBean readCountryBean = new ReadFromBackendBean(countryTableMap, ServiceURLEnum.COUNTRY_READ,
                "country", "Country read from CSV successful", "Country read from CSV failed");
        String countryCode = readFromBackend(requestInstance, importLocationsBean, readCountryBean, line);

        if (!importLocationsBean.isCorrectData())
            return importLocationsBean;

        // ** Reading entry from 'region' table **
        Map<String, String> regionTableMap = new HashMap<String, String>();
        regionTableMap.put(ODataQueryConstants.SELECT, "id");
        regionTableMap.put(ODataQueryConstants.FILTER,
                "Name eq '" + lineValues[13] + "' and Country_id eq '" + countryCode + "'");

        ReadFromBackendBean readRegionBean = new ReadFromBackendBean(regionTableMap, ServiceURLEnum.REGION_READ,
                "region", "Region read from CSV successful", "Region read from CSV failed");
        String regionCode = readFromBackend(requestInstance, importLocationsBean, readRegionBean, line);

        if (!importLocationsBean.isCorrectData())
            return importLocationsBean;

        // ** Reading entry from 'city' table **
        Map<String, String> cityTableMap = new HashMap<String, String>();
        cityTableMap.put(ODataQueryConstants.SELECT, "id");
        cityTableMap.put(ODataQueryConstants.FILTER, "Name eq '" + lineValues[12] + "' and Region_id eq '" + regionCode
                + "' and Country_id eq '" + countryCode + "'");

        ReadFromBackendBean readCityBean = new ReadFromBackendBean(cityTableMap, ServiceURLEnum.CITY_READ, "city",
                "City read from CSV successful", "City read from CSV failed");
        String cityCode = readFromBackend(requestInstance, importLocationsBean, readCityBean, line);

        if (!importLocationsBean.isCorrectData())
            return importLocationsBean;

        // ** Creating entry in 'address' table **
        Map<String, String> addressTableMap = new HashMap<String, String>();

        importLocationsBean.setAddressTableId(CommonUtilities.getNewId().toString());
        addressTableMap.put("id", importLocationsBean.getAddressTableId()); // Address ID

        addressTableMap.put("City_id", cityCode);
        addressTableMap.put("Region_id", regionCode);

        if (lineValues[11].length() != 0 && lineValues[11].length() <= 80) {
            addressTableMap.put("addressLine1", lineValues[11]); // Address
        } else if (lineValues[11].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Address cannot be empty").split(","));
            return importLocationsBean;
        } else if (lineValues[11].length() > 80) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Address cannot exceed 80 characters").split(","));
            return importLocationsBean;
        }

        if (lineValues[14].length() != 0 && isNumber(lineValues[14])) {
            addressTableMap.put("zipCode", lineValues[14]); // Zipcode
        } else if (lineValues[14].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Zipcode cannot be empty").split(","));
            return importLocationsBean;
        } else if (!isNumber(lineValues[14])) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Zipcode must be a number").split(","));
            return importLocationsBean;
        }

        if (lineValues[17].length() != 0 && isDecimalNumber(lineValues[17]) && Double.parseDouble(lineValues[17]) <= 90
                && Double.parseDouble(lineValues[17]) >= -90) {
            addressTableMap.put("latitude",
                    "" + Math.round(Double.parseDouble(lineValues[17]) * 1000000.0) / 1000000.0); // Latitude
        } else if (lineValues[17].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Latitude cannot be empty").split(","));
            return importLocationsBean;
        } else if (!isDecimalNumber(lineValues[17])) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Latitude must be a decimal number").split(","));
            return importLocationsBean;
        } else if (Double.parseDouble(lineValues[17]) > 90) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Latitude value cannot exceed 90 degrees").split(","));
            return importLocationsBean;
        } else if (Double.parseDouble(lineValues[17]) < -90) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Latitude value cannot be less than -90 degrees").split(","));
            return importLocationsBean;
        }

        if (lineValues[18].length() != 0 && isDecimalNumber(lineValues[18]) && Double.parseDouble(lineValues[18]) <= 180
                && Double.parseDouble(lineValues[18]) >= -180) {
            addressTableMap.put("logitude",
                    "" + Math.round(Double.parseDouble(lineValues[18]) * 1000000.0) / 1000000.0); // Longitude
        } else if (lineValues[18].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Longitude cannot be empty").split(","));
            return importLocationsBean;
        } else if (!isDecimalNumber(lineValues[18])) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Longitude must be a decimal number").split(","));
            return importLocationsBean;
        } else if (Double.parseDouble(lineValues[18]) > 180) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Longitude cannot exceed 180 degrees").split(","));
            return importLocationsBean;
        } else if (Double.parseDouble(lineValues[18]) < -180) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Longitude value cannot be less than -180 degrees").split(","));
            return importLocationsBean;
        }

        importLocationsBean.getCreateList()
                .add(new CreateInBackendBean(addressTableMap, ServiceURLEnum.ADDRESS_CREATE,
                        ServiceURLEnum.ADDRESS_DELETE, "address", "Create Address from locations CSV successful",
                        "Create Address from locations CSV failed"));

        return importLocationsBean;
    }

    public ImportLocationsBean scheduleTables(DataControllerRequest requestInstance,
            ImportLocationsBean importLocationsBean, String line, String[] lineValues) throws Exception {

        // ** Creating entry in 'workschedule' table **
        Map<String, String> workscheduleTableMap = new HashMap<String, String>();

        importLocationsBean.setWorkscheduleTableId(CommonUtilities.getNewId().toString());
        workscheduleTableMap.put("id", importLocationsBean.getWorkscheduleTableId()); // Work Schedule ID

        workscheduleTableMap.put("Description", "Week_" + lineValues[0]);
        workscheduleTableMap.put("createdby", importLocationsBean.getUser_ID());
        workscheduleTableMap.put("modifiedby", importLocationsBean.getUser_ID());

        importLocationsBean.getCreateList().add(new CreateInBackendBean(workscheduleTableMap,
                ServiceURLEnum.WORKSCHEDULE_CREATE, ServiceURLEnum.WORKSCHEDULE_DELETE, "workschedule",
                "Create WorkSchedule from locations CSV successful", "Create WorkSchedule from locations CSV failed"));

        // ** Creating entries in 'dayschedule table' **
        List<String> days = new ArrayList<String>();
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");

        if (lineValues[21].contains("Yes")) {
            if (lineValues[22].contains("Saturday")) {
                days.add("Saturday");
            }
            if (lineValues[22].contains("Sunday")) {
                days.add("Sunday");
            }
        }

        for (int i = 0; i < days.size(); ++i) {

            String dayStartTime = (i >= 0 && i <= 4) ? getTimeForSchedule(lineValues[19])
                    : getTimeForSchedule(lineValues[23]);
            String dayEndTime = (i >= 0 && i <= 4) ? getTimeForSchedule(lineValues[20])
                    : getTimeForSchedule(lineValues[24]);

            if (dayStartTime.equals("ERROR") || dayEndTime.equals("ERROR")) {
                importLocationsBean.setCorrectData(false);
                importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
                importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Incorrect day timings").split(","));
                continue;
            }

            Map<String, String> dayscheduleTableMap = new HashMap<String, String>();

            importLocationsBean.setDayscheduleTableId(CommonUtilities.getNewId().toString());
            dayscheduleTableMap.put("id", importLocationsBean.getDayscheduleTableId()); // Day Schedule ID

            dayscheduleTableMap.put("WorkSchedule_id", importLocationsBean.getWorkscheduleTableId());
            dayscheduleTableMap.put("WeekDayName", days.get(i));
            dayscheduleTableMap.put("StartTime", dayStartTime);
            dayscheduleTableMap.put("EndTime", dayEndTime);
            dayscheduleTableMap.put("createdby", importLocationsBean.getUser_ID());
            dayscheduleTableMap.put("modifiedby", importLocationsBean.getUser_ID());

            importLocationsBean.getCreateList()
                    .add(new CreateInBackendBean(dayscheduleTableMap, ServiceURLEnum.DAYSCHEDULE_CREATE,
                            ServiceURLEnum.DAYSCHEDULE_DELETE, "dayschedule",
                            "Create DaySchedule from locations CSV successful",
                            "Create DaySchedule from locations CSV failed"));
        }

        return importLocationsBean;
    }

    public ImportLocationsBean locationTable(DataControllerRequest requestInstance,
            ImportLocationsBean importLocationsBean, String line, String[] lineValues) throws Exception {

        // ** Creating entry in 'location' table **
        Map<String, String> locationTableMap = new HashMap<String, String>();

        importLocationsBean.setLocationTableId(CommonUtilities.getNewId().toString());
        locationTableMap.put("id", importLocationsBean.getLocationTableId()); // Location ID

        if (lineValues[0].length() != 0 && lineValues[0].length() <= 10) {
            locationTableMap.put("Code", lineValues[0]); // Code
        } else if (lineValues[0].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Code cannot be empty").split(","));
            return importLocationsBean;
        } else if (lineValues[0].length() > 10) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Code cannot exceed 10 digits").split(","));
            return importLocationsBean;
        }

        if (lineValues[1].length() != 0 && lineValues[1].length() <= 100
                && NAME_PATTERN.matcher(lineValues[1]).matches()) {
            locationTableMap.put("Name", lineValues[1]); // Name
        } else if (lineValues[1].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Name cannot be empty").split(","));
            return importLocationsBean;
        } else if (lineValues[1].length() > 100) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Name cannot exceed 100 characters").split(","));
            return importLocationsBean;
        } else if (!NAME_PATTERN.matcher(lineValues[1]).matches()) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Incorrect Name").split(","));
            return importLocationsBean;
        }

        if (lineValues[2].length() != 0 && lineValues[2].length() <= 100
                && NAME_PATTERN.matcher(lineValues[2]).matches()) {
            locationTableMap.put("DisplayName", lineValues[2]); // Display Name
        } else if (lineValues[2].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Display Name cannot be empty").split(","));
            return importLocationsBean;
        } else if (lineValues[2].length() > 100) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Display Name cannot exceed 100 characters").split(","));
            return importLocationsBean;
        } else if (!NAME_PATTERN.matcher(lineValues[2]).matches()) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Incorrect Display Name").split(","));
            return importLocationsBean;
        }

        if (lineValues[3].length() != 0 && EMAIL_ID_PATTERN.matcher(lineValues[3]).matches()) {
            locationTableMap.put("EmailId", lineValues[3]); // Email ID
        } else if (lineValues[3].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Email ID cannot be empty").split(","));
            return importLocationsBean;
        } else if (!EMAIL_ID_PATTERN.matcher(lineValues[3]).matches()) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Incorrect Email ID").split(","));
            return importLocationsBean;
        }

        if (lineValues[4].contains("Branch") ^ lineValues[4].contains("ATM")) {
            if (lineValues[4].contains("Branch")) {
                locationTableMap.put("Type_id", "Branch"); // Type -> Branch
            } else {
                locationTableMap.put("Type_id", "ATM"); // Type -> ATM
            }
        } else {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Incorrect Type").split(","));
            return importLocationsBean;
        }

        if (lineValues[5].contains("Active") ^ lineValues[5].contains("Inactive")) {
            if (lineValues[5].contains("Active")) {
                locationTableMap.put("Status_id", StatusEnum.SID_ACTIVE.name()); // Status -> Active
            } else {
                locationTableMap.put("Status_id", StatusEnum.SID_INACTIVE.name()); // Status -> Inactive
            }
        } else {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Incorrect Status").split(","));
            return importLocationsBean;
        }

        if (lineValues[6].contains("Yes") ^ lineValues[6].contains("No")) {
            if (lineValues[6].contains("Yes")) {
                locationTableMap.put("IsMainBranch", "1"); // Main Branch -> Yes
            } else {
                locationTableMap.put("IsMainBranch", "0"); // Main Branch -> No
            }
        } else {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Incorrect Main Branch").split(","));
            return importLocationsBean;
        }

        if (lineValues[4].contains("Branch")) {
            if (lineValues[7].contains("Physical") ^ lineValues[7].contains("Mobile")) {
                if (lineValues[7].contains("Mobile")) {
                    locationTableMap.put("isMobile", "1"); // Bank Type -> Mobile
                } else {
                    locationTableMap.put("isMobile", "0"); // Bank Type -> Physical
                }
            } else {
                importLocationsBean.setCorrectData(false);
                importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
                importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Incorrect Bank Type").split(","));
                return importLocationsBean;
            }
        }

        if (lineValues[10].length() != 0 && lineValues[10].length() <= 200) {
            locationTableMap.put("Description", lineValues[10]); // Description
        } else if (lineValues[10].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Description cannot be empty").split(","));
            return importLocationsBean;
        } else if (lineValues[10].length() > 200) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Description cannot exceed 200 characters").split(","));
            return importLocationsBean;
        }

        if (lineValues[16].length() != 0 && lineValues[16].length() <= 21
                && MOBILE_NO_PATTERN.matcher(lineValues[16]).matches()) {
            locationTableMap.put("PhoneNumber", lineValues[16]); // Phone Number
        } else if (lineValues[16].length() == 0) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Phone Number cannot be empty").split(","));
            return importLocationsBean;
        } else if (lineValues[16].length() > 21) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter()
                    .printRecord((line + ",Phone Number cannot exceed 21 characters").split(","));
            return importLocationsBean;
        } else if (!MOBILE_NO_PATTERN.matcher(lineValues[16]).matches()) {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            importLocationsBean.getResponseCsvPrinter().printRecord((line + ",Incorrect Phone Number").split(","));
            return importLocationsBean;
        }

        locationTableMap.put("Address_id", importLocationsBean.getAddressTableId());
        locationTableMap.put("WorkSchedule_id", importLocationsBean.getWorkscheduleTableId());

        importLocationsBean.getCreateList()
                .add(new CreateInBackendBean(locationTableMap, ServiceURLEnum.LOCATION_CREATE,
                        ServiceURLEnum.LOCATION_DELETE, "location", "Create location from locations CSV successful",
                        "Create location from locations CSV failed"));

        return importLocationsBean;
    }

    public ImportLocationsBean locationfacilityTable(DataControllerRequest requestInstance,
            ImportLocationsBean importLocationsBean, String line, String[] lineValues) throws Exception {

        String[] facilityList = lineValues[25].split(";");

        for (String facility : facilityList) {

            facility = facility.trim();

            // ** Reading entry from 'facility' table **
            Map<String, String> facilityTableMap = new HashMap<String, String>();
            facilityTableMap.put(ODataQueryConstants.SELECT, "id");
            facilityTableMap.put(ODataQueryConstants.FILTER, "name eq '" + facility + "'");

            ReadFromBackendBean readFacilityBean = new ReadFromBackendBean(facilityTableMap,
                    ServiceURLEnum.FACILITY_READ, "facility", "Facility read from CSV successful",
                    "Facility read from CSV failed");
            String facilityTableId = readFromBackend(requestInstance, importLocationsBean, readFacilityBean, line);

            if (!importLocationsBean.isCorrectData())
                return importLocationsBean;

            // ** Creating entry in 'locationfacility' table **
            Map<String, String> locationfacilityTableMap = new HashMap<String, String>();

            locationfacilityTableMap.put("Location_id", importLocationsBean.getLocationTableId());
            locationfacilityTableMap.put("facility_id", facilityTableId);
            locationfacilityTableMap.put("createdby", importLocationsBean.getUser_ID());
            locationfacilityTableMap.put("modifiedby", importLocationsBean.getUser_ID());

            importLocationsBean.getCreateList()
                    .add(new CreateInBackendBean(locationfacilityTableMap, ServiceURLEnum.LOCATIONFACILITY_CREATE,
                            ServiceURLEnum.LOCATIONFACILITY_DELETE, "locationfacility",
                            "Create locationfacility from locations CSV successful",
                            "Create locationfacility from locations CSV failed"));
        }

        return importLocationsBean;
    }

    public ImportLocationsBean locationcustomersegmentTable(DataControllerRequest requestInstance,
            ImportLocationsBean importLocationsBean, String line, String[] lineValues) throws Exception {

        if (lineValues[4].contains("Branch")) {

            String[] customersegmentList = lineValues[8].split(";");

            for (String customersegment : customersegmentList) {

                customersegment = customersegment.trim();

                // ** Reading entry from 'customersegment' table **
                Map<String, String> customersegmentTableMap = new HashMap<String, String>();
                customersegmentTableMap.put(ODataQueryConstants.SELECT, "id");
                customersegmentTableMap.put(ODataQueryConstants.FILTER, "type eq '" + customersegment + "'");

                ReadFromBackendBean readCustomersegmentBean = new ReadFromBackendBean(customersegmentTableMap,
                        ServiceURLEnum.CUSTOMERSEGMENT_READ, "customersegment",
                        "Customersegment read from CSV successful", "Customersegment read from CSV failed");
                String customersegmentTableId = readFromBackend(requestInstance, importLocationsBean,
                        readCustomersegmentBean, line);

                if (!importLocationsBean.isCorrectData())
                    return importLocationsBean;

                // ** Creating entry in 'locationcustomersegment' table **
                Map<String, String> locationcustomersegmentTableMap = new HashMap<String, String>();

                locationcustomersegmentTableMap.put("Location_id", importLocationsBean.getLocationTableId());
                locationcustomersegmentTableMap.put("segment_id", customersegmentTableId);
                locationcustomersegmentTableMap.put("createdby", importLocationsBean.getUser_ID());
                locationcustomersegmentTableMap.put("modifiedby", importLocationsBean.getUser_ID());

                importLocationsBean.getCreateList().add(new CreateInBackendBean(locationcustomersegmentTableMap,
                        ServiceURLEnum.LOCATIONCUSTOMERSEGMENT_CREATE, ServiceURLEnum.LOCATIONCUSTOMERSEGMENT_DELETE,
                        "locationcustomersegment", "Create locationcustomersegment from locations CSV successful",
                        "Create locationcustomersegment from locations CSV failed"));
            }
        }

        return importLocationsBean;
    }

    public ImportLocationsBean locationcurrencyTable(DataControllerRequest requestInstance,
            ImportLocationsBean importLocationsBean, String line, String[] lineValues) throws Exception {

        if (lineValues[4].contains("ATM")) {

            String[] currencyList = lineValues[9].split(";");

            for (String currency : currencyList) {

                currency = currency.trim();

                // ** Reading entry from 'currency' table **
                Map<String, String> currencyTableMap = new HashMap<String, String>();
                currencyTableMap.put(ODataQueryConstants.SELECT, "code");
                currencyTableMap.put(ODataQueryConstants.FILTER, "code eq '" + currency + "'");

                ReadFromBackendBean readCurrencyBean = new ReadFromBackendBean(currencyTableMap,
                        ServiceURLEnum.CURRENCY_READ, "currency", "Currency read from CSV successful",
                        "Currency read from CSV failed");
                String currencyTableCode = readFromBackend(requestInstance, importLocationsBean, readCurrencyBean,
                        line);

                if (!importLocationsBean.isCorrectData())
                    return importLocationsBean;

                // ** Creating entry in 'locationcurrency' table **
                Map<String, String> locationcurrencyTableMap = new HashMap<String, String>();

                locationcurrencyTableMap.put("Location_id", importLocationsBean.getLocationTableId());
                locationcurrencyTableMap.put("currency_code", currencyTableCode);
                locationcurrencyTableMap.put("createdby", importLocationsBean.getUser_ID());
                locationcurrencyTableMap.put("modifiedby", importLocationsBean.getUser_ID());

                importLocationsBean.getCreateList()
                        .add(new CreateInBackendBean(locationcurrencyTableMap, ServiceURLEnum.LOCATIONCURRENCY_CREATE,
                                ServiceURLEnum.LOCATIONCURRENCY_DELETE, "locationcurrency",
                                "Create locationcurrency from locations CSV successful",
                                "Create locationcurrency from locations CSV failed"));
            }
        }

        return importLocationsBean;
    }

    public String readFromBackend(DataControllerRequest requestInstance, ImportLocationsBean importLocationsBean,
            ReadFromBackendBean readBean, String line) throws Exception {

        String code = null;

        String readResponse = Executor.invokeService(readBean.getURL(), readBean.getParameterMap(), null,
                requestInstance);
        JSONObject readResponseJSON = CommonUtilities.getStringAsJSONObject(readResponse);

        if (readResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                && readResponseJSON.getJSONArray(readBean.getResponseKey()) != null
                && readResponseJSON.getJSONArray(readBean.getResponseKey()).length() != 0) {
            if (readBean.getResponseKey().equals("currency")) {
                code = readResponseJSON.getJSONArray(readBean.getResponseKey()).getJSONObject(0).getString("code");
            } else {
                code = readResponseJSON.getJSONArray(readBean.getResponseKey()).getJSONObject(0).getString("id");
            }
        } else {
            importLocationsBean.setCorrectData(false);
            importLocationsBean.setFailureCount(importLocationsBean.getFailureCount() + 1);
            String errmsg = (readResponseJSON.getInt(FabricConstants.OPSTATUS) != 0)
                    ? (readResponseJSON.getString("errmsg"))
                    : readBean.getResponseKey().toUpperCase() + " does not exist in database";
            importLocationsBean.getResponseCsvPrinter().printRecord((line + "," + errmsg).split(","));
        }

        return code;
    }

    public boolean createInBackend(DataControllerRequest requestInstance, ImportLocationsBean importLocationsBean,
            String line) throws Exception {

        boolean createSuccess = false;
        Stack<DeleteFromBackendBean> deleteList = new Stack<DeleteFromBackendBean>();

        for (int i = 0; i < importLocationsBean.getCreateList().size(); ++i) {

            CreateInBackendBean createBean = importLocationsBean.getCreateList().get(i);

            String createResponse = Executor.invokeService(createBean.getCreateURL(), createBean.getCreateMap(), null,
                    requestInstance);
            JSONObject createResponseJSON = CommonUtilities.getStringAsJSONObject(createResponse);

            if (createResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && createResponseJSON.getJSONArray(createBean.getResponseKey()) != null) {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.CREATE,
                        ActivityStatusEnum.SUCCESSFUL, createBean.getSuccessDescription());
                createSuccess = true;
                deleteList.push(
                        new DeleteFromBackendBean(createBean.getCreateMap().get("id"), createBean.getDeleteURL()));
            } else {
                AuditHandler.auditAdminActivity(requestInstance, ModuleNameEnum.LOCATIONS, EventEnum.CREATE,
                        ActivityStatusEnum.FAILED, createBean.getFailureDescription());
                createSuccess = false;
                importLocationsBean.getResponseCsvPrinter()
                        .printRecord((line + "," + createResponseJSON.getString("errmsg")).split(","));
                deleteFromBackend(importLocationsBean, deleteList, requestInstance);
                break;
            }
        }

        return createSuccess;
    }

    public void deleteFromBackend(ImportLocationsBean importLocationsBean, Stack<DeleteFromBackendBean> deleteList,
            DataControllerRequest requestInstance) throws Exception {

        Iterator<DeleteFromBackendBean> it = deleteList.iterator();
        while (it.hasNext()) {
            DeleteFromBackendBean deleteBean = deleteList.pop();
            Map<String, String> deleteMap = new HashMap<String, String>();
            deleteMap.put("id", deleteBean.getDeleteId());

            Executor.invokeService(deleteBean.getDeleteURL(), deleteMap, null, requestInstance);
        }
    }

    public void manageLocationFile(DataControllerRequest requestInstance, String userId, String createOrUpdate,
            ImportLocationsBean importLocationsBean, LocationFileBean locationFileBean) throws ApplicationException {

        // ** Creating or updating entry in 'locationfile' table **
        Map<String, String> locationfileTableMap = new HashMap<String, String>();
        locationfileTableMap.put("id", locationFileBean.getId());
        locationfileTableMap.put("lastmodifiedts", CommonUtilities.getISOFormattedLocalTimestamp());

        if (createOrUpdate.equals("create")) {
            locationfileTableMap.put("locationfilestatus", "0");
            locationfileTableMap.put("createdby", userId);
            locationfileTableMap.put("createdts", CommonUtilities.getISOFormattedLocalTimestamp());
        } else {
            locationfileTableMap.put("locationfilestatus", "1");
            locationfileTableMap.put("locationfileclob", locationFileBean.getCsvContent());
            locationfileTableMap.put("successcount", "" + importLocationsBean.getSuccessCount());
            locationfileTableMap.put("failurecount", "" + importLocationsBean.getFailureCount());
            locationfileTableMap.put("modifiedby", userId);
        }

        String manageLocationfileResponse = Executor
                .invokeService(createOrUpdate.equals("create") ? ServiceURLEnum.LOCATIONFILE_CREATE
                        : ServiceURLEnum.LOCATIONFILE_UPDATE, locationfileTableMap, null, requestInstance);
        JSONObject manageLocationfileResponseJSON = CommonUtilities.getStringAsJSONObject(manageLocationfileResponse);

        if (manageLocationfileResponseJSON.getInt(FabricConstants.OPSTATUS) != 0
                || manageLocationfileResponseJSON.getJSONArray("locationfile") == null) {
            throw new ApplicationException(ErrorCodeEnum.ERR_20375);
        }
    }

    private void sendStatusEmail(DataControllerRequest requestInstance, UserDetailsBean userDetailsObject,
            int successCount, int failureCount) throws ApplicationException {

        String emailContent = StringUtils.EMPTY;
        try {
            InputStream templateStream = this.getClass().getClassLoader()
                    .getResourceAsStream("emailTemplates/importLocationsStatus.html");
            emailContent = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Exception occured while fetching importLocationsStatus.html", e);
            throw new ApplicationException(ErrorCodeEnum.ERR_20377);
        }

        emailContent = emailContent.replace("%firstName%", userDetailsObject.getFirstName());
        emailContent = emailContent.replace("%totalCount%", "" + (successCount + failureCount));
        emailContent = emailContent.replace("%successCount%", "" + successCount);
        emailContent = emailContent.replace("%failureCount%", "" + failureCount);

        EmailHandlerBean emailHandlerBean = new EmailHandlerBean();
        emailHandlerBean.setSubject("Import Locations Status");
        emailHandlerBean.setBody(emailContent);
        emailHandlerBean.setRecipientEmailId(userDetailsObject.getEmailId());
        emailHandlerBean.setFirstName(userDetailsObject.getFirstName());
        emailHandlerBean.setLastName(userDetailsObject.getLastName());

        JSONObject enrollKMSResponse = EmailHandler.enrolKMSUser(emailHandlerBean, requestInstance);
        LOG.debug("Import locations ::: Enroll KMS user response: " + enrollKMSResponse);
        JSONObject emailResponse = EmailHandler.sendEmailToSingleRecipient(emailHandlerBean, requestInstance);
        LOG.debug("Import locations ::: Send email response: " + emailResponse);
    }

    public String getTimeForSchedule(String scheduleTime) {

        String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String hour = "", minute = "";

        if (scheduleTime.contains(":") && (scheduleTime.contains("AM") ^ scheduleTime.contains("PM"))) {
            int colonIndex = scheduleTime.indexOf(":");
            hour = scheduleTime.substring(0, colonIndex);
            minute = scheduleTime.substring(colonIndex + 1, colonIndex + 3);

            if (isNumber(hour) && isNumber(minute)) {
                if (hour.length() == 1) {
                    hour = "0" + hour;
                }
                if (scheduleTime.contains("AM") && hour.equals("12")) {
                    hour = "00";
                }
                if (scheduleTime.contains("PM") && !hour.equals("12")) {
                    hour = Integer.toString(Integer.parseInt(hour) + 12);
                }
            } else {
                return "ERROR";
            }
        } else {
            return "ERROR";
        }

        String dateTime = dateString + "T" + hour + ":" + minute + ":00";
        return dateTime;
    }

    public static boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean isDecimalNumber(String s) {
        try {
            Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}