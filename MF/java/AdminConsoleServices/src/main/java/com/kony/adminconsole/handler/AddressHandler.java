package com.kony.adminconsole.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.controller.DataControllerRequest;

/**
 * 
 * Handler to Resolve Address Attributes
 *
 * @author Aditya Mankal
 *
 */
public class AddressHandler {

    private static final Logger LOG = Logger.getLogger(AddressHandler.class);

    public String getRegionID(String countryId, String regionName, DataControllerRequest requestInstance) {

        try {
            String regionID = null;

            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER,
                    "Country_id eq '" + countryId + "' and Name eq '" + regionName + "'");
            inputMap.put(ODataQueryConstants.SELECT, "id");

            String readRegionResponse = Executor.invokeService(ServiceURLEnum.REGION_READ, inputMap, null,
                    requestInstance);
            JSONObject readRegionResponseJSON = CommonUtilities.getStringAsJSONObject(readRegionResponse);

            if (readRegionResponseJSON != null && readRegionResponseJSON.has(FabricConstants.OPSTATUS)
                    && readRegionResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readRegionResponseJSON.has("region")) {
                JSONArray regionJSONArray = readRegionResponseJSON.optJSONArray("region");
                if (regionJSONArray != null && regionJSONArray.length() == 1) {// Valid Query
                    regionID = regionJSONArray.getJSONObject(0).optString("id");
                    return regionID;
                }
            }

            LOG.error("Failed CRUD Operation OR Invalid Query. Response:" + readRegionResponse);
            return null;
        } catch (Exception e) {
            LOG.error("Exception in Fetching Region ID. Recieved Input: CountryID:" + countryId + " RegionName:"
                    + regionName + " DataControllerRequest:" + requestInstance);
            throw e;
        }

    }

    public String getCountryID(String countryName, DataControllerRequest requestInstance) {
        try {
            String countryID = null;

            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "Name eq '" + countryName + "'");
            inputMap.put(ODataQueryConstants.SELECT, "id");

            String readCountryResponse = Executor.invokeService(ServiceURLEnum.COUNTRY_READ, inputMap, null,
                    requestInstance);
            JSONObject readCountryResponseJSON = CommonUtilities.getStringAsJSONObject(readCountryResponse);

            if (readCountryResponseJSON != null && readCountryResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCountryResponseJSON.getInt(FabricConstants.OPSTATUS) == 0
                    && readCountryResponseJSON.has("country")) {
                JSONArray countryJSONArray = readCountryResponseJSON.optJSONArray("country");
                if (countryJSONArray != null && countryJSONArray.length() == 1) {// Valid Query
                    countryID = countryJSONArray.getJSONObject(0).optString("id");
                    return countryID;
                }
            }

            LOG.error("Failed CRUD Operation OR Invalid Query. Response:" + readCountryResponse);
            return null;
        } catch (Exception e) {
            LOG.error("Exception in Fetching Region ID. Recieved Input: CountryName:" + countryName
                    + " DataControllerRequest:" + requestInstance);
            throw e;
        }

    }

    public String getCityID(String regionID, String countryID, String cityName, DataControllerRequest requestInstance) {
        try {
            String cityID = null;

            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put(ODataQueryConstants.FILTER, "Region_id eq '" + regionID + "' and Country_id eq '" + countryID
                    + "' and Name eq '" + cityName + "'");
            inputMap.put(ODataQueryConstants.SELECT, "id");

            String readCityResponse = Executor.invokeService(ServiceURLEnum.CITY_READ, inputMap, null, requestInstance);
            JSONObject readCityResponseJSON = CommonUtilities.getStringAsJSONObject(readCityResponse);

            if (readCityResponseJSON != null && readCityResponseJSON.has(FabricConstants.OPSTATUS)
                    && readCityResponseJSON.getInt(FabricConstants.OPSTATUS) == 0 && readCityResponseJSON.has("city")) {
                JSONArray cityJSONArray = readCityResponseJSON.optJSONArray("city");
                if (cityJSONArray != null && cityJSONArray.length() == 1) {// Valid Query
                    cityID = cityJSONArray.optJSONObject(0).optString("id");
                    return cityID;
                }
            }

            LOG.error("Failed CRUD Operation OR Invalid Query. Response:" + readCityResponse);
            return null;
        } catch (Exception e) {
            LOG.error("Exception in Fetching Region ID. Recieved Input: RegionID:" + regionID + " CountryID:"
                    + countryID + " DataControllerRequest:" + requestInstance);
            throw e;
        }

    }

    public String deleteAddress(String addressID, DataControllerRequest requestInstance) {
        try {
            String deleteAddressResponse = null;
            Map<String, String> inputMap = new HashMap<String, String>();
            inputMap.put("id", addressID);
            deleteAddressResponse = Executor.invokeService(ServiceURLEnum.ADDRESS_DELETE, inputMap, null,
                    requestInstance);
            return deleteAddressResponse;
        } catch (Exception e) {
            LOG.error("Exception in deleting Address. Recieved Input: AddressID:" + addressID
                    + " DataControllerRequest:" + requestInstance);
            throw e;
        }
    }
}
