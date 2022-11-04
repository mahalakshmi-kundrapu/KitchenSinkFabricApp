package com.kony.adminconsole.service.limitsandfees;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.commons.utils.ODataQueryConstants;
import com.kony.adminconsole.exception.DBPAuthenticationException;
import com.kony.adminconsole.service.limitsandfees.overallpaymentlimits.OverallPaymentLimitsView;
import com.kony.adminconsole.utilities.DBPServices;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;
import com.konylabs.middleware.common.JavaService2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

public class VerifyEndUserTransactionWithPeriodicLimits implements JavaService2 {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final BigDecimal ZERO_CHECK = new BigDecimal("0");
    private static final String DAILY = "DAILY";
    private static final String WEEKLY = "WEEKLY";
    private static final String MONTHLY = "MONTHLY";
    private static final Logger LOG = Logger.getLogger(VerifyEndUserTransactionWithPeriodicLimits.class);

    @Override
    public Object invoke(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {
        try {
            HashMap<String, BigDecimal> limitsMap = new HashMap<String, BigDecimal>();
            Dataset dataset = new Dataset("violations");
            Result result = new Result();
            Record record = new Record();

            String authToken = requestInstance.getHeader(FabricConstants.X_KONY_AUTHORIZATION_HEADER);
            String customerId = requestInstance.getParameter("customerId");
            String serviceId = requestInstance.getParameter("serviceId");
            String transactionAmount = requestInstance.getParameter("transactionAmount");
            String accountId = requestInstance.getParameter("accountId");
            String systemUser = requestInstance.getParameter("systemUserId");

            if (StringUtils.isBlank(customerId)) {
                return ErrorCodeEnum.ERR_20565.setErrorCode(null);
            }

            if (StringUtils.isBlank(serviceId)) {
                return ErrorCodeEnum.ERR_20566.setErrorCode(null);
            }

            if (StringUtils.isBlank(transactionAmount)) {
                return ErrorCodeEnum.ERR_20576.setErrorCode(null);
            }

            if (StringUtils.isBlank(systemUser)) {
                return ErrorCodeEnum.ERR_20571.setErrorCode(null);
            }

            if (StringUtils.isBlank(accountId)) {
                return ErrorCodeEnum.ERR_20577.setErrorCode(null);
            }

            BigDecimal amount = new BigDecimal(transactionAmount);

            if (amount.compareTo(ZERO_CHECK) == 0) {
                return ErrorCodeEnum.ERR_20578.setErrorCode(null);
            }

            try {
                HashMap<String, BigDecimal> serviceLimits = getServiceDetailsForServiceId(serviceId, authToken,
                        requestInstance);

                BigDecimal minLimit = serviceLimits.get("minLimit");
                BigDecimal maxLimit = serviceLimits.get("maxLimit");

                if (minLimit != null && minLimit.compareTo(amount) > 0) {
                    result = ErrorCodeEnum.ERR_20579.setErrorCode(null);
                    record.addParam(new Param("minLimit", minLimit.toString(), FabricConstants.STRING));
                    dataset.addRecord(record);
                    result.addDataset(dataset);
                    return result;
                }

                if (maxLimit != null && maxLimit.compareTo(amount) < 0) {
                    result = ErrorCodeEnum.ERR_20580.setErrorCode(null);
                    record.addParam(new Param("maxLimit", maxLimit.toString(), FabricConstants.STRING));
                    dataset.addRecord(record);
                    result.addDataset(dataset);
                    return result;
                }

                // fetch user levels periodic limits
                fetchUserLevelPeriodicLimits(limitsMap, customerId, serviceId, authToken, requestInstance);

                // fetch user group levels periodic limits
                List<String> groupIds = fetchUserGroups(customerId, authToken, requestInstance);
                JSONArray userGroups = fetchUserGroupPeriodicLimits(groupIds, authToken, requestInstance);
                consolidateGroupPeriodWithMaxAmt(userGroups, limitsMap);

                // fetch service level periodic limits
                fetchServicePeriodicLimits(limitsMap, serviceId, authToken, requestInstance);

                // fetch start and end dates
                HashMap<String, String> dateMap = createEndAndStart(MONTHLY);
                String startDate = dateMap.get("startDate");
                String endDate = dateMap.get("endDate");

                // fetch transaction from retail banking exposed services of above start date
                // and end date
                JSONArray trasactions = getTransactions(authToken, accountId, startDate, endDate, requestInstance);

                // validate limits
                Dataset errors = validateTransactionLimits(trasactions, limitsMap, amount, null);
                if (!errors.getAllRecords().isEmpty()) {
                    Record rec = new Record();
                    rec.addDataset(errors);
                    dataset.addRecord(rec);
                }

                // validate with overall payment limits
                Dataset overallPaymentLimitsErrors = validateOverallPaymentLimits(trasactions, amount, serviceId,
                        authToken, requestInstance);
                if (!overallPaymentLimitsErrors.getAllRecords().isEmpty()) {
                    Record rec = new Record();
                    rec.addDataset(overallPaymentLimitsErrors);
                    dataset.addRecord(rec);
                }

                if (dataset.getAllRecords().isEmpty()) {
                    // fetch applicable fees input service id, transaction amount,customer id
                    int transactionFees = calculateTranscationApplicableFees(customerId, groupIds, serviceId,
                            transactionAmount, authToken, requestInstance);
                    result.addParam(new Param("fees", transactionFees + "", FabricConstants.STRING));
                }

            } catch (DBPAuthenticationException de) {
                de.getErrorCodeEnum().setErrorCode(result);
                return result;
            } catch (Exception e) {
                return ErrorCodeEnum.ERR_20001.setErrorCode(null);
            }

            result.addDataset(dataset);
            return result;
        } catch (Exception e) {
            Result errorResult = new Result();
            LOG.debug("Runtime Exception.Exception Trace:", e);
            ErrorCodeEnum.ERR_20001.setErrorCode(errorResult);
            return errorResult;
        }
    }

    private int calculateTranscationApplicableFees(String customerId, List<String> groupIds, String serviceId,
            String transactionAmount, String authToken, DataControllerRequest requestInstance) throws Exception {
        // fetch user level first
        int transactionFee = 0;
        JSONObject readResJSON = null;
        JSONArray transactionFeesArray = null;

        readResJSON = getTransactionFeesForUser(customerId, serviceId, ServiceURLEnum.TRANSACTIONFEESENDUSER_VIEW_READ,
                requestInstance);
        if (readResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception("Exception in Getting transactionFees");
        }
        transactionFeesArray = (JSONArray) readResJSON.get("transactionfeesenduser_view");
        for (int indexVar = 0; indexVar < transactionFeesArray.length(); indexVar++) {
            String maxAmt = ((JSONObject) transactionFeesArray.get(indexVar)).get("MaximumTransactionValue").toString()
                    .trim();
            String minAmt = ((JSONObject) transactionFeesArray.get(indexVar)).get("MinimumTransactionValue").toString()
                    .trim();
            int maxAmount = Integer.parseInt(maxAmt);
            int minAmount = Integer.parseInt(minAmt);
            int transAmount = Integer.parseInt(transactionAmount);
            String transFees = ((JSONObject) transactionFeesArray.get(indexVar)).get("Fees").toString().trim();
            if (transAmount >= minAmount && transAmount <= maxAmount) {
                transactionFee = Integer.parseInt(transFees);
                return transactionFee;
            }
        }
        // if not then group level

        int minGroupTransFees = Integer.MAX_VALUE;
        for (int i = 0; i < groupIds.size(); i++) {
            readResJSON = getTransactionFeesForGroup(customerId, groupIds.get(i),
                    ServiceURLEnum.TRANSACTIONFEEGROUP_VIEW_READ, requestInstance);
            if (readResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                throw new Exception("Exception in Getting transactionFees");
            }
            transactionFeesArray = (JSONArray) readResJSON.get("transactionfeegroup_view");
            for (int indexVar = 0; indexVar < transactionFeesArray.length(); indexVar++) {
                String maxAmt = ((JSONObject) transactionFeesArray.get(indexVar)).get("MaximumTransactionValue")
                        .toString().trim();
                String minAmt = ((JSONObject) transactionFeesArray.get(indexVar)).get("MinimumTransactionValue")
                        .toString().trim();
                int maxAmount = Integer.parseInt(maxAmt);
                int minAmount = Integer.parseInt(minAmt);
                int transAmount = Integer.parseInt(transactionAmount);
                String transFees = ((JSONObject) transactionFeesArray.get(indexVar)).get("Fees").toString().trim();
                int transactionFees = Integer.parseInt(transFees);
                if (transAmount >= minAmount && transAmount <= maxAmount) {
                    if (transactionFees < minGroupTransFees)
                        minGroupTransFees = transactionFees;
                }
            }
        }
        if (minGroupTransFees != Integer.MAX_VALUE) {
            transactionFee = minGroupTransFees;
            return transactionFee;
        }
        // if not then service level
        readResJSON = getTransactionFeesForService(customerId, serviceId,
                ServiceURLEnum.TRANSACTIONFEESERVICE_VIEW_READ, requestInstance);
        if (readResJSON.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception("Exception in Getting transactionFees");
        }
        transactionFeesArray = (JSONArray) readResJSON.get("transactionfeeservice_view");

        for (int indexVar = 0; indexVar < transactionFeesArray.length(); indexVar++) {
            String maxAmt = ((JSONObject) transactionFeesArray.get(indexVar)).get("MaximumTransactionValue").toString()
                    .trim();
            String minAmt = ((JSONObject) transactionFeesArray.get(indexVar)).get("MinimumTransactionValue").toString()
                    .trim();
            int maxAmount = Integer.parseInt(maxAmt);
            int minAmount = Integer.parseInt(minAmt);
            int transAmount = Integer.parseInt(transactionAmount);
            String transFees = ((JSONObject) transactionFeesArray.get(indexVar)).get("Fees").toString().trim();
            if (transAmount >= minAmount && transAmount <= maxAmount) {
                transactionFee = Integer.parseInt(transFees);
                return transactionFee;
            }
        }
        return transactionFee;
    }

    public JSONObject getTransactionFeesForService(String customerId, String serviceId, ServiceURLEnum url,
            DataControllerRequest requestInstance) {

        HashMap<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceId + "'");
        String readEndpointResponse = Executor.invokeService(url, postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject getTransactionFeesForGroup(String customerId, String groupId, ServiceURLEnum url,
            DataControllerRequest requestInstance) {

        HashMap<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER, " Group_id eq '" + groupId + "'");
        String readEndpointResponse = Executor.invokeService(url, postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    public JSONObject getTransactionFeesForUser(String customerId, String serviceId, ServiceURLEnum url,
            DataControllerRequest requestInstance) {

        HashMap<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "customer_id eq '" + customerId + "'" + "and service_id eq '" + serviceId + "'");
        String readEndpointResponse = Executor.invokeService(url, postParametersMap, null, requestInstance);
        return CommonUtilities.getStringAsJSONObject(readEndpointResponse);

    }

    private Dataset validateOverallPaymentLimits(JSONArray trasactions, BigDecimal amount, String serviceId,
            String authToken, DataControllerRequest requestInstance) throws Exception {
        try {
            Dataset dataset = new Dataset("overallPaymentLimitErrors");
            OverallPaymentLimitsView overallPaymentLimitsView = new OverallPaymentLimitsView();

            Dataset limits = overallPaymentLimitsView.fetchAllTransactionGroupsByIdServices(null,
                    ServiceURLEnum.OVERALLPAYMENTLIMITS_VIEW_READ, authToken, requestInstance);

            Dataset services = new Dataset();
            Set<String> servicesIdsInCombo = new HashSet<String>();
            Set<String> servicesNamesInCombo = new HashSet<String>();

            for (Record record : limits.getAllRecords()) {
                services = record.getDatasetById("services");
                servicesIdsInCombo = new HashSet<String>();
                servicesNamesInCombo = new HashSet<String>();
                for (Record servicesRec : services.getAllRecords()) {
                    String tempServiceId = servicesRec.getParam("serviceId").getValue().trim();
                    String tempServiceName = servicesRec.getParam("serviceName").getValue().trim();
                    servicesIdsInCombo.add(tempServiceId.toUpperCase().trim());
                    servicesNamesInCombo.add(tempServiceName.toUpperCase().trim());
                }
                if (servicesIdsInCombo.contains(serviceId.toUpperCase().trim())) {
                    Dataset periodicLimits = record.getDatasetById("periodicLimits");
                    HashMap<String, BigDecimal> oplimitsMap = new HashMap<String, BigDecimal>();
                    for (Record periodRec : periodicLimits.getAllRecords()) {
                        String periodName = periodRec.getParam("periodName").getValue();
                        BigDecimal limit = new BigDecimal(periodRec.getParam("maximumLimit").getValue());
                        oplimitsMap.put(periodName, limit);
                    }
                    // now validate this combo against limits
                    Dataset errors = validateTransactionLimits(trasactions, oplimitsMap, amount, servicesNamesInCombo);
                    if (!errors.getAllRecords().isEmpty()) {
                        Record addRecord = new Record();
                        addRecord.addParam(record.getParam("transactionGroupId"));
                        addRecord.addParam(record.getParam("transactionGroupName"));
                        addRecord.addDataset(errors);
                        dataset.addRecord(addRecord);
                    }
                }
            }

            return dataset;
        } catch (Exception e) {
            throw e;
        }
    }

    private HashMap<String, BigDecimal> getServiceDetailsForServiceId(String serviceId, String authToken,
            DataControllerRequest requestInstance) throws Exception {

        HashMap<String, BigDecimal> serviceLimitsMap = new HashMap<String, BigDecimal>();
        serviceLimitsMap.put("minLimit", null);
        serviceLimitsMap.put("maxLimit", null);

        HashMap<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put(ODataQueryConstants.FILTER, "id eq '" + serviceId + "'");
        String readEndpointResponse = Executor.invokeService(ServiceURLEnum.SERVICE_READ, postParametersMap, null,
                requestInstance);
        JSONObject serviceDetails = CommonUtilities.getStringAsJSONObject(readEndpointResponse);
        if (serviceDetails.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception("Error in validating limits");
        }
        JSONArray serviceLimits = serviceDetails.getJSONArray("service");
        if (serviceLimits.length() > 0) {
            if (serviceLimits.getJSONObject(0).has("MinTransferLimit")) {
                serviceLimitsMap.put("minLimit", serviceLimits.getJSONObject(0).getBigDecimal("MinTransferLimit"));
            }
            if (serviceLimits.getJSONObject(0).has("MaxTransferLimit")) {
                serviceLimitsMap.put("maxLimit", serviceLimits.getJSONObject(0).getBigDecimal("MaxTransferLimit"));
            }
        }
        return serviceLimitsMap;
    }

    // fetch user level periodic limits
    private void fetchUserLevelPeriodicLimits(HashMap<String, BigDecimal> limitsMap, String customerId,
            String serviceId, String authToken, DataControllerRequest requestInstance) throws Exception {
        HashMap<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put(ODataQueryConstants.FILTER,
                "Customer_id eq '" + customerId + "' and Service_id eq '" + serviceId + "'");
        String readResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMITENDUSER_VIEW_READ, postParametersMap,
                null, requestInstance);
        JSONObject jsonReponse = CommonUtilities.getStringAsJSONObject(readResponse);
        if (jsonReponse.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception("Unable to fetch user periodic limits");
        }
        JSONArray userPLs = jsonReponse.getJSONArray("periodiclimitenduser_view");
        for (int i = 0; i < userPLs.length(); i++) {
            JSONObject temp = userPLs.getJSONObject(i);
            String periodId = temp.getString("Period_Name");
            BigDecimal maxLimit = temp.getBigDecimal("MaximumLimit");
            limitsMap.put(periodId, maxLimit);
        }
    }

    // fetch groups of a customer
    private List<String> fetchUserGroups(String customerId, String authToken, DataControllerRequest requestInstance)
            throws Exception {
        HashMap<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put(ODataQueryConstants.FILTER, "Customer_id eq '" + customerId + "'");
        String readResponse = Executor.invokeService(ServiceURLEnum.CUSTOMERGROUP_READ, postParametersMap, null,
                requestInstance);
        JSONObject jsonReponse = CommonUtilities.getStringAsJSONObject(readResponse);
        if (jsonReponse.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception("Unable to fetch user groups periodic limits");
        }
        JSONArray userGroupsData = (JSONArray) jsonReponse.get("customergroup");

        List<String> userGroupIds = new ArrayList<String>();
        for (int i = 0; i < userGroupsData.length(); i++) {
            JSONObject temp = userGroupsData.getJSONObject(i);
            userGroupIds.add(temp.getString("Group_id"));
        }
        return userGroupIds;
    }

    // fetch periodic limits of groups
    private JSONArray fetchUserGroupPeriodicLimits(List<String> groupIds, String authToken,
            DataControllerRequest requestInstance) throws Exception {
        JSONArray userGroups = new JSONArray();
        HashMap<String, String> postParametersMap = new HashMap<String, String>();
        for (String groupId : groupIds) {
            postParametersMap.clear();
            postParametersMap.put(ODataQueryConstants.FILTER, "Group_id eq '" + groupId + "'");
            String readResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMITUSERGROUP_VIEW_READ,
                    postParametersMap, null, requestInstance);
            JSONObject jsonReponse = CommonUtilities.getStringAsJSONObject(readResponse);
            if (jsonReponse.getInt(FabricConstants.OPSTATUS) != 0) {
                throw new Exception("Unable to fetch user groups periodic limits");
            }
            JSONArray userGroupsData = (JSONArray) jsonReponse.get("periodiclimitusergroup_view");
            for (int i = 0; i < userGroupsData.length(); i++) {
                userGroups.put(userGroupsData.getJSONObject(i));
            }
        }
        return userGroups;
    }

    private void consolidateGroupPeriodWithMaxAmt(JSONArray userGroups, HashMap<String, BigDecimal> limitsMap) {
        HashMap<String, BigDecimal> groupLimits = new HashMap<String, BigDecimal>();
        for (int i = 0; i < userGroups.length(); i++) {
            JSONObject temp = userGroups.getJSONObject(i);
            String periodName = temp.getString("Period_Name");
            BigDecimal limit = new BigDecimal(temp.getString("MaximumLimit"));
            if (!limitsMap.containsKey(periodName)) {
                if (groupLimits.containsKey(periodName)) {
                    BigDecimal prevLimit = groupLimits.get(periodName);
                    limit = limit.compareTo(prevLimit) < 0 ? prevLimit : limit;
                }
                groupLimits.put(periodName, limit);
            }
        }
        limitsMap.putAll(groupLimits);
    }

    // fetch service level periodic limits
    private void fetchServicePeriodicLimits(HashMap<String, BigDecimal> limitsMap, String serviceId, String authToken,
            DataControllerRequest requestInstance) throws Exception {
        HashMap<String, String> postParametersMap = new HashMap<String, String>();
        postParametersMap.clear();
        postParametersMap.put(ODataQueryConstants.FILTER, "Service_id eq '" + serviceId + "'");
        String readResponse = Executor.invokeService(ServiceURLEnum.PERIODICLIMITSERVICE_VIEW_READ, postParametersMap,
                null, requestInstance);
        JSONObject jsonResponse = CommonUtilities.getStringAsJSONObject(readResponse);
        if (jsonResponse.getInt(FabricConstants.OPSTATUS) != 0) {
            throw new Exception("Error while fetching service level periodic limits");
        }
        JSONArray readServiceResponse = jsonResponse.getJSONArray("periodiclimitservice_view");
        for (int i = 0; i < readServiceResponse.length(); i++) {
            JSONObject temp = readServiceResponse.getJSONObject(i);
            String periodName = temp.getString("Period_Name");
            if (!limitsMap.containsKey(periodName)) {
                BigDecimal maxLimit = temp.getBigDecimal("MaximumLimit");
                limitsMap.put(periodName, maxLimit);
            }
        }
    }

    private JSONArray getTransactions(String authToken, String accountNumber, String StartDate, String EndDate,
            DataControllerRequest requestInstance) throws DBPAuthenticationException {

        JSONObject trasactionsDataset = DBPServices.getTransactions(accountNumber, StartDate, EndDate, requestInstance);
        return trasactionsDataset.getJSONArray("Transactions");
    }

    private Dataset validateTransactionLimits(JSONArray trasactions, HashMap<String, BigDecimal> limitsMap,
            BigDecimal amount, Set<String> servicesNamesInCombo) throws ParseException {
        Dataset dataset = new Dataset("periodicLimits");
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            BigDecimal prevTransactedAmt = null;
            BigDecimal transferrableAmt = null;
            Date startDate = null, endDate = null;
            for (String key : limitsMap.keySet()) {
                prevTransactedAmt = null;
                transferrableAmt = null;
                startDate = null;
                endDate = null;
                if (key.equalsIgnoreCase(DAILY)) {
                    HashMap<String, String> dateMap = createEndAndStart(DAILY);
                    startDate = dateFormat.parse(dateMap.get("startDate"));
                    endDate = dateFormat.parse(dateMap.get("endDate"));
                } else if (key.equalsIgnoreCase(WEEKLY)) {
                    HashMap<String, String> dateMap = createEndAndStart(WEEKLY);
                    startDate = dateFormat.parse(dateMap.get("startDate"));
                    endDate = dateFormat.parse(dateMap.get("endDate"));
                } else if (key.equalsIgnoreCase(MONTHLY)) {
                    HashMap<String, String> dateMap = createEndAndStart(MONTHLY);
                    startDate = dateFormat.parse(dateMap.get("startDate"));
                    endDate = dateFormat.parse(dateMap.get("endDate"));
                } else {
                    // USER TO GIVE START DATE AND END DATES
                    // FETCH TRANSACTIONS AND SUM THE TRANSACTIONS
                    // POPULATE PREVTRANSACTEDAMT AND TRANSFERRABLEAMT

                }
                prevTransactedAmt = fetchTotalTransactedAmountByDates(trasactions, startDate, endDate,
                        servicesNamesInCombo);
                transferrableAmt = limitsMap.get(key).subtract(prevTransactedAmt);
                if (amount.compareTo(transferrableAmt) > 0) {
                    Record record = new Record();
                    record.addParam(new Param("periodName", key, FabricConstants.STRING));
                    record.addParam(new Param("availableLimit", transferrableAmt.toString(), FabricConstants.STRING));
                    dataset.addRecord(record);
                }
            }
        } catch (ParseException e) {
            throw e;
        }
        return dataset;
    }

    private HashMap<String, String> createEndAndStart(String period) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        HashMap<String, String> hm = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        if (period.equalsIgnoreCase(DAILY)) {
            String formatted = dateFormat.format(cal.getTime());
            hm.put("startDate", formatted);
            hm.put("endDate", formatted);
        } else if (period.equalsIgnoreCase(WEEKLY)) {
            // "calculate" the start date of the week
            Calendar first = (Calendar) cal.clone();
            first.add(Calendar.DAY_OF_WEEK, first.getFirstDayOfWeek() - first.get(Calendar.DAY_OF_WEEK));

            // and add six days to the end date
            Calendar last = (Calendar) first.clone();
            last.add(Calendar.DAY_OF_YEAR, 6);

            String formatted = dateFormat.format(first.getTime());
            hm.put("startDate", formatted);
            formatted = dateFormat.format(last.getTime());
            hm.put("endDate", formatted);
        } else if (period.equalsIgnoreCase(MONTHLY)) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String formatted = dateFormat.format(cal.getTime());
            hm.put("startDate", formatted);
            cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE));
            formatted = dateFormat.format(cal.getTime());
            hm.put("endDate", formatted);
        }
        return hm;
    }

    private BigDecimal fetchTotalTransactedAmountByDates(JSONArray trasactions, Date startDate, Date endDate,
            Set<String> servicesNamesInCombo) throws ParseException {
        BigDecimal total = new BigDecimal(0);
        for (int i = 0; i < trasactions.length(); i++) {
            JSONObject temp = trasactions.getJSONObject(i);
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
                String transactionDate = temp.getString("transactionDate").substring(0, 10);
                String transactiontype = temp.getString("transactiontype");
                Date transDate = dateFormat.parse(transactionDate);
                // isSameService will be always true when validating w.r.t periodic Limits
                // isSameService will vary when validating w.r.t. overall periodic limits as
                // should consider only the
                // services in combo
                boolean isSameService = servicesNamesInCombo == null ? true
                        : servicesNamesInCombo.contains(transactiontype.toUpperCase().trim());
                if (isSameService && transDate.compareTo(startDate) >= 0 && transDate.compareTo(endDate) <= 0) {
                    total = total.add(temp.getBigDecimal("amount"));
                }
            } catch (ParseException e) {
                throw e;
            }
        }
        return total;
    }
}