package com.kony.adminconsole.service.locationsandlocationservices;

import java.util.HashMap;
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
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class BranchDetailsService implements JavaService2 {
    private static final Logger LOG = Logger.getLogger(BranchDetailsService.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        Result result = new Result();
        JSONObject resultJSON = new JSONObject();
        String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
        String systemUser = "";
        JSONArray readBranchDetailsJSONArray;
        LOG.info("inside BranchDetails Service:");
        try {
            UserDetailsBean loggedInUserDetails = LoggedInUserHandler.getUserDetails(requestInstance);
            if (loggedInUserDetails != null) {
                systemUser = loggedInUserDetails.getUserId();
            }
            JSONObject readBranchDetailsResponseJSON = getLocationDetails(systemUser, "Branch", authToken,
                    requestInstance);
            if (readBranchDetailsResponseJSON != null && readBranchDetailsResponseJSON.has(FabricConstants.OPSTATUS)
                    && readBranchDetailsResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readBranchDetailsResponseJSON.has("locationdetails_view")) {
                LOG.debug("Fetch AT Services Status:Sucessful");
                readBranchDetailsJSONArray = readBranchDetailsResponseJSON.optJSONArray("locationdetails_view");
                if (!(readBranchDetailsJSONArray == null || readBranchDetailsJSONArray.length() < 1)) {
                    resultJSON = gen_BranchDetails_Json(readBranchDetailsJSONArray, requestInstance);
                }
                String readEndpointResponse = Executor.invokeService(ServiceURLEnum.BANK_READ, new HashMap<>(),
                        new HashMap<>(), requestInstance);
                JSONObject readBankResponseJSON = CommonUtilities.getStringAsJSONObject(readEndpointResponse);
                if (readBankResponseJSON != null && readBankResponseJSON.has(FabricConstants.OPSTATUS)
                        && readBankResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                        && readBankResponseJSON.has("bank")) {
                    JSONArray readBankJSONArray = readBankResponseJSON.optJSONArray("bank");
                    JSONObject readBankJSON = (JSONObject) readBankJSONArray.get(0);
                    if (StringUtils.isNotBlank(readBankJSON.optString("Description"))) {
                        resultJSON.put("BrandName", readBankJSON.optString("Description"));
                    }
                }
                Record branchinfo_view = CommonUtilities.constructRecordFromJSONObject(resultJSON);
                branchinfo_view.setId("branchinfo_view");
                Result processedResult = new Result();
                processedResult.addRecord(branchinfo_view);
                return processedResult;
            } else {
                ErrorCodeEnum.ERR_20369.setErrorCode(result);
                result.addParam(new Param("BranchDetailsReadError", "Branch details could not be read successfully",
                        FabricConstants.STRING));
                return result;
            }
        } catch (Exception e) {
            LOG.error("BranchDetailsService Exception: ", e);
            result.addParam(new Param("status", "Failure", FabricConstants.STRING));
            ErrorCodeEnum.ERR_20369.setErrorCode(result);
        }

        return result;
    }

    public JSONObject getLocationDetails(String systemUser, String locationType, String authToken,
            DataControllerRequest requestInstance) {
        Map<String, String> postParametersMap = new HashMap<String, String>();
        String preDefinedFilter = null;
        preDefinedFilter = "type eq " + locationType;
        postParametersMap.put(ODataQueryConstants.FILTER, preDefinedFilter);

        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.LOCATIONDETAILS_VIEW_READ,
                postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);
    }

    public static JSONObject getFacilityDetails(DataControllerRequest requestInstance) {
        String readFacilityDetailsResponse = Executor.invokeService(ServiceURLEnum.LOCATIONFACILITY_VIEW_READ,
                new HashMap<>(), new HashMap<>(), requestInstance);
        return CommonUtilities.getStringAsJSONObject(readFacilityDetailsResponse);
    }

    public static JSONObject gen_BranchDetails_Json(JSONArray readLocationDetailsJSONArray,
            DataControllerRequest requestInstance) {
        JSONArray resultJSONArray = new JSONArray();
        JSONObject facilityJSON = new JSONObject();
        facilityJSON = getFacilityDetails(requestInstance);
        for (Object requestBranchObject : readLocationDetailsJSONArray) {
            JSONObject requestBranchJSONObject = (JSONObject) requestBranchObject;
            JSONObject jsonObject = new JSONObject();
            JSONObject LocationCoordinatesObj = new JSONObject();
            JSONObject GeographicCoordinates = new JSONObject();
            JSONObject PostalAddressObj = new JSONObject();
            JSONArray contactInfoArr = new JSONArray();
            String locationID = requestBranchJSONObject.optString("locationId");
            if (facilityJSON != null && facilityJSON.has(FabricConstants.OPSTATUS)
                    && facilityJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && facilityJSON.has("locationfacility_view")) {
                JSONArray readFacilityJSONArray = facilityJSON.optJSONArray("locationfacility_view");
                JSONArray arr = new JSONArray();
                for (Object requestFacilityObject : readFacilityJSONArray) {
                    JSONObject readFacilityJSON = (JSONObject) requestFacilityObject;
                    String facilityLocID = readFacilityJSON.optString("Location_id");
                    if (StringUtils.isNotBlank(facilityLocID) && facilityLocID.equals(locationID)
                            && StringUtils.isNotBlank(readFacilityJSON.optString("Facility_Name"))
                            && StringUtils.isNotBlank(readFacilityJSON.optString("Facility_code"))) {
                        String code = readFacilityJSON.optString("Facility_code");
                        String name = readFacilityJSON.optString("Facility_Name");
                        JSONObject obj = new JSONObject();
                        obj.put("Code", code);
                        obj.put("Name", name);
                        arr.put(obj);
                    }
                    jsonObject.put("Facilities", (Object) arr);
                }
            }

            String identification = requestBranchJSONObject.optString("Code");
            jsonObject.put("Identification", identification);

            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("services"))) {
                String serviceStr = requestBranchJSONObject.optString("services");
                String[] services = serviceStr.split("\\|\\|\\ ");
                JSONArray arr = new JSONArray();
                for (int i = 0; i < services.length; i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("type", services[i]);
                    arr.put(obj);
                }
                jsonObject.put("BranchServices", arr);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("segments"))) {
                String segmentStr = requestBranchJSONObject.optString("segments");
                String[] segments = segmentStr.split(",");
                JSONArray arr = new JSONArray();
                for (int i = 0; i < segments.length; i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("type", segments[i]);
                    arr.put(obj);
                }
                jsonObject.put("CustomerSegment", arr);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("workingHours"))) {

                String workingHoursStr = requestBranchJSONObject.optString("workingHours");
                String[] workingHours = workingHoursStr.split("\\|\\|\\ ");
                JSONObject workingHoursObj = new JSONObject();
                JSONObject dayObj = new JSONObject();
                JSONObject AvailabilityObj = new JSONObject();
                JSONArray arr = new JSONArray();
                JSONArray arr2 = new JSONArray();
                for (int i = 0; i < workingHours.length; i++) {
                    String temp = workingHours[i];
                    JSONObject obj = new JSONObject();
                    String name = temp.substring(0, temp.indexOf(':'));
                    String time = temp.substring(temp.indexOf(':') + 1);
                    String startTime = time.substring(0, time.indexOf('-'));
                    String endTime = time.substring(time.indexOf('-') + 1);
                    obj.put("OpeningTime", startTime);
                    obj.put("ClosingTime", endTime);
                    arr.put(obj);
                    workingHoursObj.put("OpeningHours", arr);
                    workingHoursObj.put("Name", name);
                    arr2.put(workingHoursObj);
                }
                dayObj.put("Day", arr2);
                AvailabilityObj.put("StandardAvailability", dayObj);
                jsonObject.put("Availability", AvailabilityObj);
            }

            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("phone"))) {
                String phone = requestBranchJSONObject.optString("phone");
                JSONObject obj = new JSONObject();
                obj.put("ContactType", "Phone");
                obj.put("ContactContent", phone);
                contactInfoArr.put(obj);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("email"))) {
                String email = requestBranchJSONObject.optString("email");
                JSONObject obj = new JSONObject();
                obj.put("ContactType", "Email");
                obj.put("ContactContent", email);
                contactInfoArr.put(obj);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("IsMainBranch"))) {
                Boolean isMainBranch = Boolean.parseBoolean(requestBranchJSONObject.optString("IsMainBranch"));
                jsonObject.put("isMainBranch", (isMainBranch == true) ? "Yes" : "No");
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("isMobile"))) {
                Boolean isMobile = Boolean.parseBoolean(requestBranchJSONObject.optString("isMobile"));
                jsonObject.put("Type", (isMobile == false) ? "Physical" : "Mobile");
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("addressLine1"))) {
                String addressLine1 = requestBranchJSONObject.optString("addressLine1");
                PostalAddressObj.put("addressLine1", addressLine1);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("addressLine2"))) {
                String addressLine2 = requestBranchJSONObject.optString("addressLine2");
                PostalAddressObj.put("addressLine2", addressLine2);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("addressLine3"))) {
                String addressLine3 = requestBranchJSONObject.optString("addressLine3");
                PostalAddressObj.put("addressLine3", addressLine3);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("city"))) {
                String city = requestBranchJSONObject.optString("city");
                PostalAddressObj.put("city", city);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("streetname"))) {
                String streetname = requestBranchJSONObject.optString("streetname");
                PostalAddressObj.put("StreetName", streetname);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("region"))) {
                String region = requestBranchJSONObject.optString("region");
                PostalAddressObj.put("TownName", region);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("zipcode"))) {
                String zipcode = requestBranchJSONObject.optString("zipcode");
                PostalAddressObj.put("PostCode", zipcode);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("country"))) {
                String country = requestBranchJSONObject.optString("country");
                PostalAddressObj.put("Country", country);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("latitude"))) {
                String latitude = requestBranchJSONObject.optString("latitude");
                LocationCoordinatesObj.put("Latitude", latitude);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("longitude"))) {
                String longitude = requestBranchJSONObject.optString("longitude");
                LocationCoordinatesObj.put("longitude", longitude);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("informationTitle"))) {
                String informationTitle = requestBranchJSONObject.optString("informationTitle");
                jsonObject.put("Name", informationTitle);
            }
            if (StringUtils.isNotBlank(requestBranchJSONObject.optString("Description"))) {
                String description = requestBranchJSONObject.optString("Description");
                jsonObject.put("Description", description);
            }
            GeographicCoordinates.put("GeographicCoordinates", LocationCoordinatesObj);
            PostalAddressObj.put("GeoLocation", GeographicCoordinates);
            jsonObject.put("PostalAddress", PostalAddressObj);
            jsonObject.put("ContactInfo", contactInfoArr);
            resultJSONArray.put(jsonObject);
        }
        JSONObject resultJSON = new JSONObject();
        resultJSON.put("Branch", resultJSONArray);
        return resultJSON;
    }

}