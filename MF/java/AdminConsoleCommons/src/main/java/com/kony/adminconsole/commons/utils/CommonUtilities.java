package com.kony.adminconsole.commons.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/*
 * Author: Aditya Mankal
 * For: Kony Inc. 
 * For Internal Use Only
 * 
 */
public class CommonUtilities {

    private static final Logger LOG = Logger.getLogger(CommonUtilities.class);

    public static void main(String[] args) {
        System.out.println(getNewId().toString());
    }

    private static Random random = new Random();

    public static String getFileExtension(File file) {
        if (file == null) {
            return StringUtils.EMPTY;
        }
        String fileName = file.getName();
        if (StringUtils.isBlank(fileName) || !fileName.contains(".") || fileName.endsWith(".")) {
            return StringUtils.EMPTY;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

    public static boolean isValidEmailID(String emailAddress) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailAddress);
        return matcher.find();
    }

    /**
     * Fetch the auth token either from request headers or from query parameters. Return null if not found in both.
     * 
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getAuthToken(DataControllerRequest request) {
        Object authToken = null;

        // locate Auth token from headers. If not found, locate it from query parameters
        // otherwise return null
        authToken = request.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);

        if (authToken == null) {
            Map<String, Object> queryParams = (Map<String, Object>) request.getAttribute("queryparams");
            if (queryParams != null) {
                authToken = queryParams.get("authToken");
            }
        }
        return String.class.cast(authToken);

    }

    public static String convertTimetoISO8601Format(Date dateInstance) {
        if (dateInstance == null) {
            return StringUtils.EMPTY;
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        return dateFormat.format(dateInstance);
    }

    public static Date parseTimestampStringtoDateObject(String timestamp, String dateformat) {
        if (StringUtils.isBlank(timestamp) || StringUtils.isBlank(dateformat))
            return null;
        SimpleDateFormat dateFormatInstance = new SimpleDateFormat(dateformat);
        Date dateObject = null;
        try {
            dateObject = dateFormatInstance.parse(timestamp);
        } catch (ParseException e) {
            LOG.error("Unexpected error has occurred", e);

        }
        return dateObject;
    }

    public static String encodeFile(File fileObject) {
        try (FileInputStream fileInputStream = new FileInputStream(fileObject)) {
            String encode = "";
            byte bt[] = new byte[(int) fileObject.length()];
            int count = fileInputStream.read(bt);
            LOG.debug("Read " + count + " bytes.");
            encode = new String(Base64.encodeBase64(bt));
            return encode;
        } catch (Exception e) {
            LOG.error("Unexpected error has occurred", e);
        }
        return null;
    }

    public static String replaceLastOccuranceOfString(String sourceString, String targetSubString,
            String replacementString) {
        if (targetSubString == null)
            return sourceString;
        if (replacementString == null)
            replacementString = "";
        int index = sourceString.lastIndexOf(targetSubString);
        if (index == -1)
            return sourceString;
        return sourceString.substring(0, index) + replacementString
                + sourceString.substring(index + targetSubString.length());
    }

    public static ArrayList<String> getStringElementsOfJSONArrayToArrayList(JSONArray targetJSONArray) {
        ArrayList<String> resultArrayList = new ArrayList<String>();
        if (targetJSONArray == null)
            return resultArrayList;
        for (int indexVar = 0; indexVar < targetJSONArray.length(); indexVar++) {
            try {
                resultArrayList.add(targetJSONArray.getString(indexVar));
            } catch (Exception e) {
                continue;
            }
        }
        return resultArrayList;
    }

    public static UUID getNewId() {
        UUID uuidValue = UUID.randomUUID();
        return uuidValue;
    }

    public static JSONObject getStringAsJSONObject(String jsonString) {
        JSONObject generatedJSONObject = new JSONObject();
        if (StringUtils.isBlank(jsonString))
            return null;
        try {
            generatedJSONObject = new JSONObject(jsonString);
            return generatedJSONObject;
        } catch (JSONException e) {
            LOG.error("Unexpected error has occurred", e);
            return null;
        }
    }

    public static JSONArray getStringAsJSONArray(String jsonString) {
        JSONArray generatedJSONArray = new JSONArray();
        if (StringUtils.isBlank(jsonString))
            return null;
        try {
            generatedJSONArray = new JSONArray(jsonString);
            return generatedJSONArray;
        } catch (JSONException e) {
            return null;
        }
    }

    public static long getTimeElapsedFromTimestampToNowInMinutes(String timeStamp, String timeStampFormat) {

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(timeStampFormat);
        Date userCreationDateTimeInstance;
        try {
            userCreationDateTimeInstance = dateTimeFormat.parse(timeStamp);
            Date currentDateTimeInstance = new Date();
            long diffInMilliSeconds = currentDateTimeInstance.getTime() - userCreationDateTimeInstance.getTime();
            return TimeUnit.MILLISECONDS.toMinutes(diffInMilliSeconds);
        } catch (ParseException e) {
            LOG.error("Unexpected error has occurred", e);
        }
        return 0l;
    }

    public static HashMap<String, String> getObjectArrayAsHashMap(Object sourceObjectArray) {
        String objectArrayAsString = sourceObjectArray.toString();
        objectArrayAsString = objectArrayAsString.substring(1, objectArrayAsString.length() - 1);
        String[] keyValuePairArray = objectArrayAsString.split(",");
        HashMap<String, String> keyValuePairMap = new HashMap<String, String>();
        for (String currKeyValuePair : keyValuePairArray) {
            String[] currKeyValuePairArray = currKeyValuePair.split("=");
            if (currKeyValuePairArray.length > 1)
                keyValuePairMap.put(currKeyValuePairArray[0].trim(), currKeyValuePairArray[1].trim());
            else
                keyValuePairMap.put(currKeyValuePairArray[0].trim(), "");
        }
        return keyValuePairMap;
    }

    public static String[] prependStringToStringArray(String newValue, String[] existingStringArray) {
        String[] newStringArray = new String[existingStringArray.length + 1];
        newStringArray[0] = newValue;
        System.arraycopy(existingStringArray, 0, newStringArray, 1, existingStringArray.length);
        return newStringArray;
    }

    public static String genPhoneNumber() {

        return (random.nextInt(9999) + 10000) + "" + (random.nextInt(9999) + 10000);
    }

    public static Dataset constructDatasetFromJSONArray(JSONArray JSONArray) {
        Dataset dataset = new Dataset();
        for (int count = 0; count < JSONArray.length(); count++) {
            Record record = constructRecordFromJSONObject((JSONObject) JSONArray.get(count));
            dataset.addRecord(record);
        }
        return dataset;
    }

    public static Record constructRecordFromJSONObject(JSONObject JSONObject) {
        Record response = new Record();
        if (JSONObject == null || JSONObject.length() == 0) {
            return response;
        }
        Iterator<String> keys = JSONObject.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (JSONObject.get(key) instanceof Integer) {
                Param param = new Param(key, JSONObject.get(key).toString(), FabricConstants.INT);
                response.addParam(param);

            } else if (JSONObject.get(key) instanceof Boolean) {
                Param param = new Param(key, JSONObject.get(key).toString(), FabricConstants.BOOLEAN);
                response.addParam(param);

            } else if (JSONObject.get(key) instanceof JSONArray) {
                Dataset dataset = constructDatasetFromJSONArray(JSONObject.getJSONArray(key));
                dataset.setId(key);
                response.addDataset(dataset);
            } else if (JSONObject.get(key) instanceof JSONObject) {
                Record record = constructRecordFromJSONObject(JSONObject.getJSONObject(key));
                record.setId(key);
                response.addRecord(record);
            } else {
                Param param = new Param(key, JSONObject.optString(key), FabricConstants.STRING);
                response.addParam(param);
            }
        }

        return response;
    }

    public static Result constructResultFromJSONObject(JSONObject JSONObject) {
        Result response = new Result();
        if (JSONObject == null || JSONObject.length() == 0) {
            return response;
        }
        Iterator<String> keys = JSONObject.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (JSONObject.get(key) instanceof Integer) {
                Param param = new Param(key, JSONObject.get(key).toString(), FabricConstants.INT);
                response.addParam(param);

            } else if (JSONObject.get(key) instanceof Boolean) {
                Param param = new Param(key, JSONObject.get(key).toString(), FabricConstants.BOOLEAN);
                response.addParam(param);

            } else if (JSONObject.get(key) instanceof JSONArray) {
                Dataset dataset = constructDatasetFromJSONArray(JSONObject.getJSONArray(key));
                dataset.setId(key);
                response.addDataset(dataset);
            } else if (JSONObject.get(key) instanceof JSONObject) {
                Record record = constructRecordFromJSONObject(JSONObject.getJSONObject(key));
                record.setId(key);
                response.addRecord(record);
            } else {
                Param param = new Param(key, JSONObject.optString(key), FabricConstants.STRING);
                response.addParam(param);
            }
        }

        return response;
    }

    public static String getISOFormattedLocalTimestamp() {
        String localDateTime;
        if (LocalDateTime.now().getSecond() == 0) {
            localDateTime = LocalDateTime.now().plusSeconds(1).withNano(0).toString();
        } else {
            localDateTime = LocalDateTime.now().withNano(0).toString();
        }
        return localDateTime;
    }

    public static String getCustomerIdForGivenUserName() {
        String localDateTime;
        if (LocalDateTime.now().getSecond() == 0) {
            localDateTime = LocalDateTime.now().plusSeconds(1).withNano(0).toString();
        } else {
            localDateTime = LocalDateTime.now().withNano(0).toString();
        }
        return localDateTime;
    }

    public static int generateRandomWithRange(int min, int max) {
        int range = (max - min) + 1;
        return (int) (Math.random() * range) + min;

    }

    public static String encodeToBase64(String sourceString) {
        if (sourceString == null)
            return null;
        return new String(java.util.Base64.getEncoder().encode(sourceString.getBytes()));
    }

    public static String decodeFromBase64(String sourceString) {
        if (sourceString == null)
            return null;
        return new String(Base64.decodeBase64(sourceString));
    }

    public static String encodeURI(String sourceString) throws UnsupportedEncodingException {
        if (sourceString == null)
            return null;
        return new String(java.net.URLEncoder.encode(sourceString, "UTF-8"));
    }

    public static String decodeURI(String sourceString) throws UnsupportedEncodingException {
        if (sourceString == null)
            return null;
        return new String(java.net.URLDecoder.decode(sourceString, "UTF-8"));
    }

    public static Result getResultObjectFromJSONObject(JSONObject inputJSON) {
        Result response = new Result();
        if (inputJSON == null || inputJSON.length() == 0) {
            return response;
        }
        Iterator<String> keys = inputJSON.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (inputJSON.get(key) instanceof String) {
                Param param = new Param(key, inputJSON.getString(key), FabricConstants.STRING);
                response.addParam(param);

            } else if (inputJSON.get(key) instanceof Integer) {
                Param param = new Param(key, inputJSON.optString(key), FabricConstants.INT);
                response.addParam(param);

            } else if (inputJSON.get(key) instanceof Boolean) {
                Param param = new Param(key, inputJSON.optString(key), "boolean");
                response.addParam(param);

            } else if (inputJSON.get(key) instanceof JSONArray) {
                Dataset dataset = CommonUtilities.constructDatasetFromJSONArray(inputJSON.getJSONArray(key));
                dataset.setId(key);
                response.addDataset(dataset);
            } else if (inputJSON.get(key) instanceof JSONObject) {
                Record record = CommonUtilities.constructRecordFromJSONObject(inputJSON.getJSONObject(key));
                record.setId(key);
                response.addRecord(record);
            }
        }

        return response;
    }

    public static long getNumericId() {
        long generatedValue;
        generatedValue = (long) 1000000000 + random.nextInt(900000000);
        return generatedValue;
    }

    public static List<String> getJSONArrayAsList(JSONArray sourceJSONArray) {
        List<String> resultList = new ArrayList<String>();
        if (sourceJSONArray == null || sourceJSONArray.length() == 0) {
            return resultList;
        }
        for (int indexVar = 0; indexVar < sourceJSONArray.length(); indexVar++) {
            resultList.add(sourceJSONArray.optString(indexVar));
        }
        return resultList;
    }

    public static List<String> getStringifiedArrayAsList(String stringifiedList) {
        List<String> processedList = new ArrayList<>();
        if (StringUtils.isNotBlank(stringifiedList)) {
            JSONArray jsonArray = getStringAsJSONArray(stringifiedList);
            if (jsonArray != null && jsonArray.length() > 0) {
                processedList = getJSONArrayAsList(jsonArray);
            }
        }
        return processedList;
    }

    public static List<String> removeDuplicatesInList(List<String> sourceList) {
        if (sourceList != null && !sourceList.isEmpty()) {
            Set<String> set = new HashSet<String>();
            set.addAll(sourceList);
            sourceList.clear();
            sourceList.addAll(set);
        }
        return sourceList;
    }

    public static void fileDownloadFailure(DataControllerResponse responseInstance, String errorMessage)
            throws Exception {
        Map<String, String> customHeaders = new HashMap<String, String>();
        customHeaders.put("Content-Type", "text/plain; charset=utf-8");

        responseInstance.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON,
                new BufferedHttpEntity(new StringEntity(errorMessage, StandardCharsets.UTF_8)));
        responseInstance.getHeaders().putAll(customHeaders);
        responseInstance.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    public static Date parseDateStringToDate(String dateString, String dateFormat) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            sdf.setLenient(false);
            return sdf.parse(dateString, new ParsePosition(0));
        } catch (Exception exe) {
            return null;
        }
    }

    public static int calculateAge(Date inputDate) {
        try {
            Calendar calender = Calendar.getInstance();
            calender.setTime(inputDate);
            int dayOfMonth = calender.get(Calendar.DAY_OF_MONTH);
            int month = calender.get(Calendar.MONTH);
            int year = calender.get(Calendar.YEAR);
            LocalDate today = LocalDate.now(); // Today's date
            LocalDate birthday = LocalDate.of(year, month, dayOfMonth);
            Period period = Period.between(birthday, today);
            return period.getYears();
        } catch (Exception e) {
            LOG.error("Exception in calculating Age.", e);
            throw e;
        }
    }

    public static String formatLanguageIdentifier(String languageIdentifier) {
        LOG.debug("Formatting Language Identifier. Recieved Value:" + languageIdentifier);
        if (StringUtils.isBlank(languageIdentifier)) {
            return StringUtils.EMPTY;
        }
        languageIdentifier = languageIdentifier.replace("_", "-");
        List<LanguageRange> languageRange = Locale.LanguageRange.parse(languageIdentifier);
        if (languageRange != null && !languageRange.isEmpty()) {
            String formattedIdentifier = languageRange.get(0).getRange();
            formattedIdentifier = formattedIdentifier.replace("_", "-");
            LOG.debug("Formatted Language Identifier. Result Value:" + formattedIdentifier);
            return formattedIdentifier;
        }
        return StringUtils.EMPTY;
    }

    /**
     * Method to remove filter records from CRUD response based on the Locale
     * 
     * @param records
     * @param localeIdentifierKey
     * @param uniqueIdentifierKey
     * @param defaultLocale
     * @return filtered records based on the Locale
     */
    public static JSONArray filterRecordsByLocale(JSONArray records, String localeIdentifierKey,
            String uniqueIdentifierKey, String defaultLocale) {

        try {
            LOG.debug("Recieved Default Locale:" + defaultLocale);
            LOG.debug("Recieved Locale Identifier Key:" + localeIdentifierKey);
            LOG.debug("Recieved Unique Identifier Key:" + uniqueIdentifierKey);

            if (records != null && records.length() > 0 && StringUtils.isNotBlank(uniqueIdentifierKey)
                    && StringUtils.isNotBlank(localeIdentifierKey)) {
                LOG.debug("Count of Records" + records.length());

                // Filtered Records
                JSONArray filteredRecords = new JSONArray();

                // Method local variables
                JSONObject currJSON;
                String currLocale;
                String currUniqueIdentifer;

                // Maps used for filtering of Records
                Map<String, JSONObject> defaultLocaleRecords = new HashMap<>();
                Map<String, JSONObject> userPreferredLocaleRecords = new HashMap<>();

                LOG.debug("Traversing Input Records");

                for (Object currObject : records) {

                    if (currObject instanceof JSONObject) {
                        currJSON = (JSONObject) currObject;

                        if (currJSON.has(uniqueIdentifierKey)) {

                            // Unique Identifier of current record
                            currUniqueIdentifer = currJSON.optString(uniqueIdentifierKey);

                            // Locale of current record
                            currLocale = currJSON.optString(localeIdentifierKey);

                            if (StringUtils.equals(currLocale, defaultLocale)) {
                                // Locale of current record and Default Locale are same. Add to Default Locale
                                // Map
                                defaultLocaleRecords.put(currUniqueIdentifer, currJSON);
                            } else {
                                // Locale of current record and Default Locale are different. Add to User
                                // Preferred Locale Map
                                userPreferredLocaleRecords.put(currUniqueIdentifer, currJSON);
                            }
                        }

                    }
                }

                LOG.debug("Count of Default Locale Records:" + defaultLocaleRecords.size());
                LOG.debug("Count of User Preferred Locale Records:" + userPreferredLocaleRecords.size());

                LOG.debug("Consolidating segregated records");
                // User Preferred Locale Records take precedence over Default Locale Records
                for (Entry<String, JSONObject> currEntry : userPreferredLocaleRecords.entrySet()) {
                    filteredRecords.put(currEntry.getValue());
                    if (defaultLocaleRecords.containsKey(currEntry.getKey())) {
                        // Remove Records of Default Locale for which the records of user preferred
                        // Locale are present
                        defaultLocaleRecords.remove(currEntry.getKey());
                    }
                }
                LOG.debug("Count of Default Locale Records after traversing User Preferrred Locale Records:"
                        + defaultLocaleRecords.size());

                // Add Default Locale Records for which records of User Preferred Locale are not
                // available
                for (Entry<String, JSONObject> currEntry : defaultLocaleRecords.entrySet()) {
                    filteredRecords.put(currEntry.getValue());
                }

                LOG.debug("Returning consolidated Records");
                // Return filtered set of Records
                return filteredRecords;
            }

            // Default return value
            return records;
        } catch (Exception e) {
            LOG.error("Exception in Filtering Records by Locale. Exception:", e);
            throw e;
        }
    }

    /**
     * Method to sort JSON Array of JSON objects on a particular attribute
     * 
     * @param jsonArray
     * @param key
     * @param isAscending
     * @param TRUE
     *            if 'key' is of numeric type
     * @return sorted JSON Array
     */
    public static JSONArray sortJSONArrayOfJSONObjects(JSONArray jsonArray, String key, boolean isAscending,
            boolean isNumericValue) {

        try {

            if (jsonArray == null || jsonArray.length() == 0 || StringUtils.isBlank(key)) {
                return jsonArray;
            }

            // Add JSON Objects into List
            List<JSONObject> jsonValuesList = new ArrayList<>();
            for (int index = 0; index < jsonArray.length(); index++) {
                jsonValuesList.add(jsonArray.getJSONObject(index));
            }

            // Sort JSON Array
            Collections.sort(jsonValuesList, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject object1, JSONObject object2) {

                    try {
                        if (isNumericValue == true) {

                            Integer val1 = Integer.valueOf(object1.optInt(key));
                            Integer val2 = Integer.valueOf(object2.optInt(key));

                            if (isAscending == true) {
                                return val1.compareTo(val2);
                            } else {
                                return val2.compareTo(val1);
                            }
                        } else {
                            String val1 = object1.optString(key);
                            String val2 = object2.optString(key);

                            if (isAscending == true) {
                                return val1.compareTo(val2);
                            } else {
                                return val2.compareTo(val1);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Exception in sorting JSON Array. Exception:", e);
                        throw e;
                    }
                }
            });

            // Construct result JSON Array
            JSONArray sortedJSONArray = new JSONArray();
            for (int i = 0; i < jsonValuesList.size(); i++) {
                sortedJSONArray.put(jsonValuesList.get(i));
            }

            return sortedJSONArray;
        } catch (Exception e) {
            LOG.error("Exception in sorting JSON Array. Exception:", e);
            throw e;
        }
    }

    /**
     * Method to sort Dataset of Records on a particular attribute
     * 
     * @param dataset
     * @param key
     * @param isAscending
     * @param TRUE
     *            if 'key' is of numeric type
     * @return sorted Dataset
     */
    public static Dataset sortDatasetofRecords(Dataset dataset, String key, boolean isAscending,
            boolean isNumericValue) {

        try {

            if (dataset == null || dataset.getAllRecords().size() == 0 || StringUtils.isBlank(key)) {
                return dataset;
            }

            // Add Records into List
            List<Record> recordsList = new ArrayList<>(dataset.getAllRecords());

            // Sort JSON Array
            Collections.sort(recordsList, new Comparator<Record>() {

                @Override
                public int compare(Record record1, Record record2) {

                    try {
                        if (isNumericValue == true) {

                            Integer val1 = Integer.valueOf(record1.getParamByName(key).getValue());
                            Integer val2 = Integer.valueOf(record2.getParamByName(key).getValue());

                            if (isAscending == true) {
                                return val1.compareTo(val2);
                            } else {
                                return val2.compareTo(val1);
                            }
                        } else {
                            String val1 = record1.getParamByName(key).getValue();
                            String val2 = record2.getParamByName(key).getValue();

                            if (isAscending == true) {
                                return val1.compareTo(val2);
                            } else {
                                return val2.compareTo(val1);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Exception in sorting Dataset. Exception:", e);
                        throw e;
                    }
                }
            });

            // Construct result Dataset
            Dataset sortedDataset = new Dataset();
            sortedDataset.addAllRecords(recordsList);

            return sortedDataset;
        } catch (Exception e) {
            LOG.error("Exception in sorting Dataset. Exception:", e);
            throw e;
        }
    }

    /**
     * Method to sort List of Records on a particular attribute
     * 
     * @param records
     * @param key
     * @param isAscending
     * @param TRUE
     *            if 'key' is of numeric type
     * @return sorted Records
     */
    public static List<Record> sortRecordsByParamAttribute(List<Record> records, String key, boolean isAscending,
            boolean isNumericValue) {

        try {

            if (records == null || records.size() == 0 || StringUtils.isBlank(key)) {
                return records;
            }

            // Clone Records List
            List<Record> recordsList = new ArrayList<>(records);

            Collections.sort(recordsList, new Comparator<Record>() {

                @Override
                public int compare(Record record1, Record record2) {

                    try {
                        if (isNumericValue == true) {

                            Integer val1 = Integer.valueOf(record1.getParamByName(key).getValue());
                            Integer val2 = Integer.valueOf(record2.getParamByName(key).getValue());

                            if (isAscending == true) {
                                return val1.compareTo(val2);
                            } else {
                                return val2.compareTo(val1);
                            }
                        } else {
                            String val1 = record1.getParamByName(key).getValue();
                            String val2 = record2.getParamByName(key).getValue();

                            if (isAscending == true) {
                                return val1.compareTo(val2);
                            } else {
                                return val2.compareTo(val1);
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Exception in sorting Dataset. Exception:", e);
                        throw e;
                    }
                }
            });

            return recordsList;

        } catch (Exception e) {
            LOG.error("Exception in sorting Records. Exception:", e);
            throw e;
        }
    }

    /**
     * Method to sort Map of JSON Objects based on an attribute
     * 
     * @param objectMap
     * @param key
     * @param isAscending
     * @param isNumeric
     * @return Sorted Map
     */
    public static Map<String, JSONObject> sortJSONObjectMapByValue(Map<String, JSONObject> objectMap, String key,
            boolean isAscending, boolean isNumeric) {

        try {
            // Create a list from elements of HashMap
            List<Map.Entry<String, JSONObject>> objectList = new LinkedList<Map.Entry<String, JSONObject>>(
                    objectMap.entrySet());

            // Sort the List
            Collections.sort(objectList, new Comparator<Map.Entry<String, JSONObject>>() {
                public int compare(Map.Entry<String, JSONObject> object1, Map.Entry<String, JSONObject> object2) {
                    int sortValue = 0;
                    if (isNumeric) {
                        Integer value1 = Integer.valueOf(object1.getValue().optInt(key));
                        Integer value2 = Integer.valueOf(object2.getValue().optInt(key));
                        sortValue = value1.compareTo(value2);
                    } else {
                        String value1 = object1.getValue().optString(key);
                        String value2 = object2.getValue().optString(key);
                        sortValue = value1.compareTo(value2);
                    }
                    if (isAscending) {
                        return sortValue;
                    } else {
                        return -sortValue;
                    }
                }
            });

            // Prepare Result Map
            Map<String, JSONObject> sortedMap = new LinkedHashMap<String, JSONObject>();
            for (Map.Entry<String, JSONObject> entry : objectList) {
                sortedMap.put(entry.getKey(), entry.getValue());
            }
            return sortedMap;
        } catch (Exception e) {
            LOG.error("Exception in sorting Map. Exception:", e);
            throw e;
        }
    }

    public static int calculateOffset(int recordsPerPage, int pageNumber) {
        int offset = recordsPerPage * (pageNumber - 1);
        if (offset < 0) {
            return 0;
        }
        return offset;
    }

    public static File getInputStreamAsFile(InputStream sourceInputStream) throws IOException {
        File file = null;
        file = File.createTempFile("prefix", "suffix");
        FileOutputStream result = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = sourceInputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        result.close();
        return file;
    }
}