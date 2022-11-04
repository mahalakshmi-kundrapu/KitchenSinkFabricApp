/**
 * 
 */
package com.kony.adminconsole.customerdata.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import com.kony.adminconsole.customerdata.constants.AddressType;
import com.kony.adminconsole.customerdata.constants.CommunicationExtension;
import com.kony.adminconsole.customerdata.constants.CommunicationType;
import com.kony.adminconsole.customerdata.constants.CustomerDataHeaders;
import com.kony.adminconsole.customerdata.constants.CustomerFlagStatus;
import com.kony.adminconsole.customerdata.constants.CustomerStatus;
import com.kony.adminconsole.customerdata.constants.DatabaseType;
import com.kony.adminconsole.customerdata.constants.EmployementStatus;
import com.kony.adminconsole.customerdata.constants.MaritalStatus;
import com.kony.adminconsole.customerdata.dto.Customer;
import com.kony.adminconsole.customerdata.util.Utilities;

/**
 * Utility to generate SQL scripts of Customer Data
 * 
 * @author Aditya Mankal
 * 
 */
@SuppressWarnings("deprecation")
public class CustomerSQLScriptGenerator {

    private static final String VALID_INPUT_FILE_EXTENSION = "CSV"; // Provide without '.' (Case Insensitive)
    private static final String VALID_ERROR_DESC_FILE_EXTENSION = "CSV"; // Provide without '.' (Case Insensitive)
    private static final String VALID_OUTPUT_FILE_EXTENSION = "SQL"; // Provide without '.' (Case Insensitive)

    private static final String INPUT_CSV_FILE_PATH_PROPERTY_KEY = "inputCSVFilePath";
    private static final String OUTPUT_SQL_FILE_PATH_PROPERTY_KEY = "outputSQLFilePath";
    private static final String LOG_FILE_PATH_PROPERTY_KEY = "outputLogFilePath";
    private static final String ERROR_DESCRIPTION_CSV_FILE_PATH_PROPERTY_KEY = "outputCSVFilePath";
    private static final String DATABASE_TYPE_PROPERTY_KEY = "databaseType";

    private static final int FIRST_NAME_MIN_CHARS = 1;
    private static final int FIRST_NAME_MAX_CHARS = 50;

    private static final int LAST_NAME_MIN_CHARS = 1;
    private static final int LAST_NAME_MAX_CHARS = 50;

    private static final String IS_OLB_ALLOWED_DEFAULT_VALUE = "0";
    private static final String IS_CSRASSIST_ALLOWED_DEFAULT_VALUE = "0";
    private static final String IS_STAFF_MEMBER_DEFAULT_VALUE = "0";

    private static final String CUSTOMER_STATUS_DEFAULT_VALUE = "Customer Active";

    private static final String PRIMARY_EMAIL_DEFAULT_LOC_TYPE = "Other";
    private static final String SECONDARY_EMAIL_DEFAULT_LOC_TYPE = "Other";
    private static final String TERITARY_EMAIL_DEFAULT_LOC_TYPE = "Other";

    private static final String PRIMARY_PHONE_DEFAULT_LOC_TYPE = "Other";
    private static final String SECONDARY_PHONE_DEFAULT_LOC_TYPE = "Other";
    private static final String TERITARY_PHONE_DEFAULT_LOC_TYPE = "Other";

    private static final String IS_PRIMARY_TRUE_VALUE = "1";
    private static final String IS_PRIMARY_FALSE_VALUE = "0";

    private static final DatabaseType DEFAULT_DATABASE_TYPE = DatabaseType.MYSQL;

    private static String CUSTOMER_TEMPLATE;
    private static String ADDRESS_TEMPLATE;
    private static String PASSWORD_HISTORY_TEMPLATE;
    private static String CUSTOMER_ADDRESS_TEMPLATE;
    private static String CUSTOMER_COMMUNICATIION_TEMPLATE;
    private static String CUSTOMER_GROUP_TEMPLATE;
    private static String CUSTOMER_FLAG_STATUS;

    private static final String SOFT_DELETE_FLAG = "0";
    private static String DEFAULT_TIME_STAMP = StringUtils.EMPTY;
    private static final String CREATED_BY = "Customer Import Tool";
    private static final String MODIFIED_BY = "Customer Import Tool";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static StringBuilder sqlBuffer = null;

    static {
        try (InputStream inputStream = CustomerSQLScriptGenerator.class.getClassLoader()
                .getResourceAsStream("SQLTemplates.properties");) {
            Properties properties = new Properties();
            properties.load(inputStream);
            CUSTOMER_TEMPLATE = properties.getProperty("customerTemplate");
            ADDRESS_TEMPLATE = properties.getProperty("addressTemplate");
            PASSWORD_HISTORY_TEMPLATE = properties.getProperty("passwordHistoryTemplate");
            CUSTOMER_ADDRESS_TEMPLATE = properties.getProperty("customerAddressTemplate");
            CUSTOMER_COMMUNICATIION_TEMPLATE = properties.getProperty("customerCommunicationTemplate");
            CUSTOMER_GROUP_TEMPLATE = properties.getProperty("customerGroupTemplate");
            CUSTOMER_FLAG_STATUS = properties.getProperty("customerFlagStatusTemplate");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inputs to the tool are passed as System Property values
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {
        String inputCSVFilePath = System.getProperty(INPUT_CSV_FILE_PATH_PROPERTY_KEY);
        String outputSQLFilePath = System.getProperty(OUTPUT_SQL_FILE_PATH_PROPERTY_KEY);
        String outputLogFilePath = System.getProperty(LOG_FILE_PATH_PROPERTY_KEY);
        String errorDescriptionCSVFilePath = System.getProperty(ERROR_DESCRIPTION_CSV_FILE_PATH_PROPERTY_KEY);
        String databaseType = System.getProperty(DATABASE_TYPE_PROPERTY_KEY);
        DatabaseType databaseTypeConstant = null;
        PrintStream standardOutStream = null;
        File inputCSVFilePointer = null, outputSQLFilePointer = null, errorDescriptionCSVFilePointer = null;

        try {

            if (StringUtils.isNotBlank(outputLogFilePath)) {
                try {
                    standardOutStream = new PrintStream(outputLogFilePath.trim());
                    System.setOut(standardOutStream);
                    System.setErr(standardOutStream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (StringUtils.isBlank(inputCSVFilePath)) {
                System.err.println("System Property -D" + INPUT_CSV_FILE_PATH_PROPERTY_KEY + " is required.");
                System.exit(1);
            } else {
                try {
                    inputCSVFilePointer = new File(inputCSVFilePath.trim());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                } finally {
                    String fileExtension = Utilities.getFileExtension(inputCSVFilePointer);
                    if (inputCSVFilePointer == null || !inputCSVFilePointer.exists()) {
                        System.err.println("System Property -D" + INPUT_CSV_FILE_PATH_PROPERTY_KEY
                                + " Error:Invalid Input file path. Please provide the path of an accessible '"
                                + VALID_INPUT_FILE_EXTENSION + "'");
                        System.err.println("Process Terminated");
                        System.exit(1);
                    }
                    if (!StringUtils.equalsIgnoreCase(fileExtension, VALID_INPUT_FILE_EXTENSION)) {
                        System.err.println("Parameter: -D" + INPUT_CSV_FILE_PATH_PROPERTY_KEY
                                + " Error:Invalid Input file. Please provide a file with extension "
                                + VALID_INPUT_FILE_EXTENSION + " as the input.");
                        System.err.println("Process Terminated");
                        System.exit(1);
                    }
                }
            }

            if (StringUtils.isBlank(outputSQLFilePath)) {
                System.err.println("System Property -D" + OUTPUT_SQL_FILE_PATH_PROPERTY_KEY + " is required.");
                System.err.println("Process Terminated");
                System.exit(1);
            } else {
                outputSQLFilePointer = new File(outputSQLFilePath.trim());
                boolean createFileStatus = true;
                if (!outputSQLFilePointer.exists()) {
                    createFileStatus = outputSQLFilePointer.createNewFile();
                    if (createFileStatus == false) {
                        throw new Exception("Failed to create output SQL File");
                    }
                }

                String fileExtension = Utilities.getFileExtension(outputSQLFilePointer);
                if (outputSQLFilePointer == null || !outputSQLFilePointer.exists()) {
                    System.err.println("System Property -D" + OUTPUT_SQL_FILE_PATH_PROPERTY_KEY
                            + " Error: Could not create the output SQL File. Please verify the access privilages.");
                    System.err.println("Process Terminated");
                    System.exit(1);
                }
                if (!StringUtils.equalsIgnoreCase(fileExtension, VALID_OUTPUT_FILE_EXTENSION)) {
                    System.err.println("System Property -D" + OUTPUT_SQL_FILE_PATH_PROPERTY_KEY
                            + " Error: Invalid output file extension. The extension of the output file must be '"
                            + VALID_OUTPUT_FILE_EXTENSION + "'");
                    System.err.println("Process Terminated");
                    System.exit(1);
                }

            }

            if (StringUtils.isBlank(errorDescriptionCSVFilePath)) {
                System.err
                        .println("System Property -D" + ERROR_DESCRIPTION_CSV_FILE_PATH_PROPERTY_KEY + " is required.");
                System.err.println("Process Terminated");
                System.exit(1);
            }

            else {
                boolean createFileStatus = true;
                errorDescriptionCSVFilePointer = new File(errorDescriptionCSVFilePath.trim());
                if (!errorDescriptionCSVFilePointer.exists()) {
                    createFileStatus = errorDescriptionCSVFilePointer.createNewFile();
                }

                if (createFileStatus == false) {
                    throw new Exception("Failed to create Error Description CSV File");
                }

                String fileExtension = Utilities.getFileExtension(errorDescriptionCSVFilePointer);

                if (errorDescriptionCSVFilePointer == null || !errorDescriptionCSVFilePointer.exists()) {
                    System.err.println("System Property -D" + ERROR_DESCRIPTION_CSV_FILE_PATH_PROPERTY_KEY
                            + "Error: Could not create the output 'Error CSV File' File. Please verify the access privilages.");
                    System.err.println("Process Terminated");
                    System.exit(1);
                }

                if (!StringUtils.equalsIgnoreCase(fileExtension, VALID_ERROR_DESC_FILE_EXTENSION)) {
                    System.err.println("System Property -D" + ERROR_DESCRIPTION_CSV_FILE_PATH_PROPERTY_KEY
                            + "Error: Invalid 'Error CSV File' extension. The extension of the 'Error CSV File' must be '"
                            + VALID_ERROR_DESC_FILE_EXTENSION + "'");
                    System.err.println("Process Terminated");
                    System.exit(1);
                }

                if (StringUtils.isBlank(databaseType)) {
                    System.err.println("System Property -D" + DATABASE_TYPE_PROPERTY_KEY
                            + " has not been provided. Proceeding further with the default Database Type: "
                            + DEFAULT_DATABASE_TYPE.toString());
                    databaseType = DEFAULT_DATABASE_TYPE.toString();
                }

                try {
                    databaseTypeConstant = DatabaseType.valueOf(databaseType.toUpperCase());
                    DEFAULT_TIME_STAMP = databaseTypeConstant.getCurrentTimestamp();
                } catch (IllegalArgumentException iae) {
                    System.err.println("System property -D" + DATABASE_TYPE_PROPERTY_KEY
                            + " must be one of following values: " + Arrays.asList(DatabaseType.values()));
                    System.err.println("Process Terminated");
                    System.exit(1);
                }
            }

            execute(inputCSVFilePath, errorDescriptionCSVFilePath, outputSQLFilePath, databaseTypeConstant);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (standardOutStream != null) {
                standardOutStream.close();
            }
        }
    }

    /**
     * Method to process the data record by record and collate the SQL statements
     * 
     * @param inputCSVFilePath
     * @param errorDescriptionCSVFilePath
     * @param outputSQLFilePath
     */
    private static void execute(String inputCSVFilePath, String errorDescriptionCSVFilePath, String outputSQLFilePath,
            DatabaseType databaseType) {

        Map<String, String> currRecordMap = null;
        StringBuilder errorMessageBuffer = null;

        try (FileWriter resultFileWriter = new FileWriter(outputSQLFilePath);

                CSVParser csvParser = CSVFormat.RFC4180.withIgnoreEmptyLines(true).withDelimiter('|')
                        .withFirstRecordAsHeader().parse(new BufferedReader(new FileReader(inputCSVFilePath)));

                CSVPrinter errorCSVFilePrinter = CSVFormat.RFC4180.withRecordSeparator(LINE_SEPARATOR)
                        .withHeader(CustomerDataHeaders.class)
                        .print(new File(errorDescriptionCSVFilePath), StandardCharsets.UTF_8);) {

            Customer customer = new Customer();

            for (CSVRecord currRecord : csvParser) {

                customer = new Customer();
                currRecordMap = currRecord.toMap();

                if (currRecordMap != null && !currRecordMap.isEmpty()) {

                    for (Map.Entry<String, String> entry : currRecordMap.entrySet()) {
                        Method setter = customer.getClass().getMethod("set" + entry.getKey().trim(), String.class);
                        setter.invoke(customer,
                                StringUtils.isBlank(entry.getValue()) ? StringUtils.EMPTY : entry.getValue().trim());
                    }
                    customer.setHashedPassword(Utilities.hashPassword(customer.getPassword()));

                    errorMessageBuffer = validateCustomerData(customer);
                    if (errorMessageBuffer != null && errorMessageBuffer.length() > 0) {
                        // Pass this record to error file with error description
                        System.err.println("CustomerId:" + customer.getId() + " Error Description:"
                                + errorMessageBuffer.toString() + " Status:Validation Error");

                        List<String> recordList = Utilities.getRecordAsList(currRecord);
                        recordList.add(errorMessageBuffer.toString());
                        errorCSVFilePrinter.printRecord(recordList);
                        errorCSVFilePrinter.flush();
                        continue;
                    } else {
                        System.out.println("Generating SQL statements for customer. Customer Id:" + customer.getId());
                    }

                    initSQLBuffer();
                    addMetadata(customer);
                    if (StringUtils.isNotBlank(databaseType.getStartTransactionStatement())) {
                        sqlBuffer.append(databaseType.getStartTransactionStatement()).append(LINE_SEPARATOR);
                    }

                    generateCustomerTableInsertScript(customer, databaseType);
                    generatePasswordHistoryTableInsertScript(customer, databaseType);
                    generateAddressTableInsertStatement(customer);
                    generateCustomerCommunicationTableInsertScript(customer);
                    generateCustomerGroupTableInsertStatements(customer);
                    generateCustomerFlagStatusTableInsertStatements(customer);
                    if (StringUtils.isNotBlank(databaseType.getCloseTransactionStatement())) {
                        sqlBuffer.append(databaseType.getCloseTransactionStatement()).append(LINE_SEPARATOR);
                    }
                    sqlBuffer.trimToSize();
                    String fileContent = sqlBuffer.toString();
                    fileContent = fileContent.replace("''", "null");
                    resultFileWriter.write(fileContent);
                    resultFileWriter.flush();
                }
            }

        } catch (IOException | NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to initialize the SQL Buffer
     */
    private static void initSQLBuffer() {
        sqlBuffer = new StringBuilder();
    }

    /**
     * Method to append meta data of the current customer record to the SQL Buffer
     * 
     * @param customer
     */
    private static void addMetadata(Customer customer) {
        sqlBuffer.append(LINE_SEPARATOR + "-- -----------------------");
        sqlBuffer.append(LINE_SEPARATOR + "-- Customer Id:" + customer.getId());
        sqlBuffer.append(LINE_SEPARATOR + "-- Customer First Name:" + customer.getFirstName());
        sqlBuffer.append(LINE_SEPARATOR + "-- Customer First Last:" + customer.getLastName());
        sqlBuffer.append(LINE_SEPARATOR + "-- -----------------------" + LINE_SEPARATOR);
    }

    /**
     * Method to generate the insert statements of 'customer' table
     * 
     * @param customer
     */
    private static void generateCustomerTableInsertScript(Customer customer, DatabaseType databaseType) {

        HashMap<String, String> customerDataMap = new HashMap<>();
        customerDataMap.put("Id", customer.getId());
        customerDataMap.put("FirstName", customer.getFirstName());
        customerDataMap.put("MiddleName", customer.getMiddleName());
        customerDataMap.put("LastName", customer.getLastName());
        customerDataMap.put("Username", customer.getUsername());
        customerDataMap.put("Password", customer.getHashedPassword());
        customerDataMap.put("Salutation", customer.getSalutation());
        customerDataMap.put("Gender", customer.getGender());
        customerDataMap.put("DateOfBirth", databaseType.parseDate(Long.valueOf(customer.getDateOfBirth())));
        customerDataMap.put("Status", customer.getStatus());
        customerDataMap.put("Ssn", customer.getSsn());
        customerDataMap.put("MaritalStatus", customer.getMaritalStatus());
        customerDataMap.put("SpouseName", customer.getSpouseName());
        customerDataMap.put("EmployementStatus", customer.getEmployementStatus());
        customerDataMap.put("IsOlbAllowed", customer.getIsOlbAllowed());
        customerDataMap.put("IsEnrolledForOlb", customer.getIsEnrolledForOlb());
        customerDataMap.put("IsStaffMember", customer.getIsStaffMember());
        customerDataMap.put("LocationCode", customer.getLocationCode());
        customerDataMap.put("PreferredContactMethod", customer.getPreferredContactMethod());
        customerDataMap.put("PreferredContactTime", customer.getPreferredContactTime());
        customerDataMap.put("IsAssistConsented", customer.getIsAssistConsented());
        customerDataMap.put("Createdby", CREATED_BY);
        customerDataMap.put("Modifiedby", MODIFIED_BY);
        customerDataMap.put("Createdtimestamp",
                databaseType.parseTimestamp(Long.valueOf(customer.getCreatedtimestamp())));
        customerDataMap.put("lastmodifiedts", DEFAULT_TIME_STAMP);
        customerDataMap.put("Synctimestamp", DEFAULT_TIME_STAMP);
        customerDataMap.put("Softdeleteflag", SOFT_DELETE_FLAG);

        appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_TEMPLATE, customerDataMap);

    }

    /**
     * Method to generate the insert statements of the 'passwordhistory' table
     * 
     * @param customer
     * @param databaseType
     */
    private static void generatePasswordHistoryTableInsertScript(Customer customer, DatabaseType databaseType) {
        HashMap<String, String> customerDataMap = new HashMap<>();
        customerDataMap.put("id", Utilities.generateId());
        customerDataMap.put("Customer_id", customer.getId());
        customerDataMap.put("PreviousPassword", customer.getHashedPassword());
        customerDataMap.put("createdby", CREATED_BY);
        customerDataMap.put("modifiedby", MODIFIED_BY);
        customerDataMap.put("createdts", DEFAULT_TIME_STAMP);
        customerDataMap.put("lastmodifiedts", DEFAULT_TIME_STAMP);
        customerDataMap.put("lastsynctimestamp", DEFAULT_TIME_STAMP);
        customerDataMap.put("softdeleteflag", SOFT_DELETE_FLAG);

        appendToSQLBuffer(CustomerSQLScriptGenerator.PASSWORD_HISTORY_TEMPLATE, customerDataMap);
    }

    /**
     * Method to generate the insert statements of 'address' and 'customeraddress' tables
     * 
     * @param customer
     */
    private static void generateAddressTableInsertStatement(Customer customer) {

        String primaryAddressId = Utilities.generateId();
        String secondaryAddressId = null;
        String tertiaryAddressId = null;

        HashMap<String, String> addressDataMap = new HashMap<>();
        addressDataMap.put("id", primaryAddressId);
        addressDataMap.put("Region", customer.getPrimaryRegion());
        addressDataMap.put("City", customer.getPrimaryCity());
        addressDataMap.put("Addressline1", customer.getPrimaryAddressline1());
        addressDataMap.put("Addressline2", customer.getPrimaryAddressline2());
        addressDataMap.put("Addressline3", customer.getPrimaryAddressline3());
        addressDataMap.put("ZipCode", customer.getPrimaryZipCode());
        addressDataMap.put("createdby", CREATED_BY);
        addressDataMap.put("modifiedby", MODIFIED_BY);
        addressDataMap.put("createdts", DEFAULT_TIME_STAMP);
        addressDataMap.put("lastmodifiedts", DEFAULT_TIME_STAMP);
        addressDataMap.put("synctimestamp", DEFAULT_TIME_STAMP);
        addressDataMap.put("softdeleteflag", SOFT_DELETE_FLAG);

        appendToSQLBuffer(CustomerSQLScriptGenerator.ADDRESS_TEMPLATE, addressDataMap);

        if (StringUtils.isNotBlank(customer.getSecondaryAddressline1())) {
            addressDataMap.clear();
            secondaryAddressId = Utilities.generateId();
            addressDataMap.put("id", secondaryAddressId);
            addressDataMap.put("Region", customer.getSecondaryRegion());
            addressDataMap.put("City", customer.getSecondaryCity());
            addressDataMap.put("Addressline1", customer.getSecondaryAddressline1());
            addressDataMap.put("Addressline2", customer.getSecondaryAddressline2());
            addressDataMap.put("Addressline3", customer.getSecondaryAddressline3());
            addressDataMap.put("ZipCode", customer.getSecondaryZipCode());
            addressDataMap.put("createdby", CREATED_BY);
            addressDataMap.put("modifiedby", MODIFIED_BY);
            addressDataMap.put("createdts", DEFAULT_TIME_STAMP);
            addressDataMap.put("lastmodifiedts", DEFAULT_TIME_STAMP);
            addressDataMap.put("synctimestamp", DEFAULT_TIME_STAMP);
            addressDataMap.put("softdeleteflag", SOFT_DELETE_FLAG);

            appendToSQLBuffer(CustomerSQLScriptGenerator.ADDRESS_TEMPLATE, addressDataMap);
        }

        if (StringUtils.isNotBlank(customer.getTertiaryAddressline1())) {
            addressDataMap.clear();
            tertiaryAddressId = Utilities.generateId();
            addressDataMap.put("id", tertiaryAddressId);
            addressDataMap.put("Region", customer.getTertiaryRegion());
            addressDataMap.put("City", customer.getTertiaryCity());
            addressDataMap.put("Addressline1", customer.getTertiaryAddressline1());
            addressDataMap.put("Addressline2", customer.getTertiaryAddressline2());
            addressDataMap.put("Addressline3", customer.getTertiaryAddressline3());
            addressDataMap.put("ZipCode", customer.getTertiaryZipCode());
            addressDataMap.put("createdby", CREATED_BY);
            addressDataMap.put("modifiedby", MODIFIED_BY);
            addressDataMap.put("createdts", DEFAULT_TIME_STAMP);
            addressDataMap.put("lastmodifiedts", DEFAULT_TIME_STAMP);
            addressDataMap.put("synctimestamp", DEFAULT_TIME_STAMP);
            addressDataMap.put("softdeleteflag", SOFT_DELETE_FLAG);

            appendToSQLBuffer(CustomerSQLScriptGenerator.ADDRESS_TEMPLATE, addressDataMap);
        }

        HashMap<String, String> customerAddressDataMap = new HashMap<>();
        customerAddressDataMap.put("customerId", customer.getId());
        customerAddressDataMap.put("addressId", primaryAddressId);
        customerAddressDataMap.put("addressType", customer.getPrimaryAddressType());
        customerAddressDataMap.put("isPrimary", IS_PRIMARY_TRUE_VALUE);
        customerAddressDataMap.put("createdby", CREATED_BY);
        customerAddressDataMap.put("modifiedby", MODIFIED_BY);
        customerAddressDataMap.put("createdts", DEFAULT_TIME_STAMP);
        customerAddressDataMap.put("lastmodifiedts", DEFAULT_TIME_STAMP);
        customerAddressDataMap.put("synctimestamp", DEFAULT_TIME_STAMP);
        customerAddressDataMap.put("softdeleteflag", SOFT_DELETE_FLAG);

        appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_ADDRESS_TEMPLATE, customerAddressDataMap);

        if (StringUtils.isNotBlank(secondaryAddressId)) {
            customerAddressDataMap.clear();
            customerAddressDataMap.put("customerId", customer.getId());
            customerAddressDataMap.put("addressId", secondaryAddressId);
            customerAddressDataMap.put("addressType", customer.getSecondaryAddressType());
            customerAddressDataMap.put("isPrimary", IS_PRIMARY_FALSE_VALUE);
            customerAddressDataMap.put("createdby", CREATED_BY);
            customerAddressDataMap.put("modifiedby", MODIFIED_BY);
            customerAddressDataMap.put("createdts", DEFAULT_TIME_STAMP);
            customerAddressDataMap.put("lastmodifiedts", DEFAULT_TIME_STAMP);
            customerAddressDataMap.put("synctimestamp", DEFAULT_TIME_STAMP);
            customerAddressDataMap.put("softdeleteflag", SOFT_DELETE_FLAG);

            appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_ADDRESS_TEMPLATE, customerAddressDataMap);
        }

        if (StringUtils.isNotBlank(tertiaryAddressId)) {
            customerAddressDataMap.clear();
            customerAddressDataMap.put("customerId", customer.getId());
            customerAddressDataMap.put("addressId", tertiaryAddressId);
            customerAddressDataMap.put("addressType", customer.getTertiaryAddressType());
            customerAddressDataMap.put("isPrimary", IS_PRIMARY_FALSE_VALUE);
            customerAddressDataMap.put("createdby", CREATED_BY);
            customerAddressDataMap.put("modifiedby", MODIFIED_BY);
            customerAddressDataMap.put("createdts", DEFAULT_TIME_STAMP);
            customerAddressDataMap.put("lastmodifiedts", DEFAULT_TIME_STAMP);
            customerAddressDataMap.put("synctimestamp", DEFAULT_TIME_STAMP);
            customerAddressDataMap.put("softdeleteflag", SOFT_DELETE_FLAG);

            appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_ADDRESS_TEMPLATE, customerAddressDataMap);
        }
    }

    /**
     * Method to generate the insert statements of 'customercommunication' table
     * 
     * @param customer
     */
    private static void generateCustomerCommunicationTableInsertScript(Customer customer) {
        String communicationId = Utilities.generateId();
        HashMap<String, String> customerCommunicationData = new HashMap<>();

        if (StringUtils.isNotBlank(customer.getPrimaryEmail())) {
            communicationId = Utilities.generateId();
            customerCommunicationData.put("Id", communicationId);
            customerCommunicationData.put("Type_id", CommunicationType.COMM_TYPE_EMAIL.toString());
            customerCommunicationData.put("Customer_id", customer.getId());
            customerCommunicationData.put("isPrimary", IS_PRIMARY_TRUE_VALUE);
            customerCommunicationData.put("Value", customer.getPrimaryEmail());
            customerCommunicationData.put("Extension", customer.getPrimaryEmailLocType());
            customerCommunicationData.put("createdby", CREATED_BY);
            customerCommunicationData.put("modifiedby", MODIFIED_BY);
            customerCommunicationData.put("createdts", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("lastmodifiedts", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("synctimestamp", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("softdeleteflag", SOFT_DELETE_FLAG);

            appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_COMMUNICATIION_TEMPLATE, customerCommunicationData);
        }

        if (StringUtils.isNotBlank(customer.getPrimaryPhone())) {
            customerCommunicationData.clear();
            communicationId = Utilities.generateId();
            customerCommunicationData.put("Id", communicationId);
            customerCommunicationData.put("Type_id", CommunicationType.COMM_TYPE_PHONE.toString());
            customerCommunicationData.put("Customer_id", customer.getId());
            customerCommunicationData.put("isPrimary", IS_PRIMARY_TRUE_VALUE);
            customerCommunicationData.put("Value", customer.getPrimaryPhone());
            customerCommunicationData.put("Extension", customer.getPrimaryPhoneLocType());
            customerCommunicationData.put("createdby", CREATED_BY);
            customerCommunicationData.put("modifiedby", MODIFIED_BY);
            customerCommunicationData.put("createdts", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("lastmodifiedts", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("synctimestamp", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("softdeleteflag", SOFT_DELETE_FLAG);

            appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_COMMUNICATIION_TEMPLATE, customerCommunicationData);
        }

        if (StringUtils.isNotBlank(customer.getSecondaryEmail())) {
            customerCommunicationData.clear();
            communicationId = Utilities.generateId();
            customerCommunicationData.put("Id", communicationId);
            customerCommunicationData.put("Type_id", CommunicationType.COMM_TYPE_EMAIL.toString());
            customerCommunicationData.put("Customer_id", customer.getId());
            customerCommunicationData.put("isPrimary", IS_PRIMARY_FALSE_VALUE);
            customerCommunicationData.put("Value", customer.getSecondaryEmail());
            customerCommunicationData.put("Extension", customer.getSecondaryEmailLocType());
            customerCommunicationData.put("createdby", CREATED_BY);
            customerCommunicationData.put("modifiedby", MODIFIED_BY);
            customerCommunicationData.put("createdts", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("lastmodifiedts", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("synctimestamp", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("softdeleteflag", SOFT_DELETE_FLAG);

            appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_COMMUNICATIION_TEMPLATE, customerCommunicationData);
        }

        if (StringUtils.isNotBlank(customer.getSecondaryPhone())) {
            customerCommunicationData.clear();
            communicationId = Utilities.generateId();
            customerCommunicationData.put("Id", communicationId);
            customerCommunicationData.put("Type_id", CommunicationType.COMM_TYPE_PHONE.toString());
            customerCommunicationData.put("Customer_id", customer.getId());
            customerCommunicationData.put("isPrimary", IS_PRIMARY_FALSE_VALUE);
            customerCommunicationData.put("Value", customer.getSecondaryPhone());
            customerCommunicationData.put("Extension", customer.getSecondaryPhoneLocType());
            customerCommunicationData.put("createdby", CREATED_BY);
            customerCommunicationData.put("modifiedby", MODIFIED_BY);
            customerCommunicationData.put("createdts", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("lastmodifiedts", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("synctimestamp", DEFAULT_TIME_STAMP);
            customerCommunicationData.put("softdeleteflag", SOFT_DELETE_FLAG);

            appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_COMMUNICATIION_TEMPLATE, customerCommunicationData);
        }

    }

    /**
     * Method to generate the insert statements of 'membergroup' table
     * 
     * @param customer
     */
    private static void generateCustomerGroupTableInsertStatements(Customer customer) {
        HashMap<String, String> customerGroupData = new HashMap<>();
        if (StringUtils.isNotBlank(customer.getGroup())) {
            String customerGroups[];
            if (customer.getGroup().contains(",")) {
                customerGroups = customer.getGroup().split(",");
            } else {
                customerGroups = new String[1];
                customerGroups[0] = customer.getGroup();
            }
            for (int indexVar = 0; indexVar < customerGroups.length; indexVar++) {
                customerGroupData.clear();
                customerGroupData.put("Customer_id", customer.getId());
                customerGroupData.put("CustomerGroup", customerGroups[indexVar]);
                customerGroupData.put("createdby", CREATED_BY);
                customerGroupData.put("modifiedby", MODIFIED_BY);
                customerGroupData.put("createdts", DEFAULT_TIME_STAMP);
                customerGroupData.put("lastmodifiedts", DEFAULT_TIME_STAMP);
                customerGroupData.put("synctimestamp", DEFAULT_TIME_STAMP);
                customerGroupData.put("softdeleteflag", SOFT_DELETE_FLAG);

                appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_GROUP_TEMPLATE, customerGroupData);
            }
        }
    }

    /**
     * Method to generate the insert statements of 'customerflagstatus' table
     * 
     * @param customer
     */
    private static void generateCustomerFlagStatusTableInsertStatements(Customer customer) {
        HashMap<String, String> customerFlagData = new HashMap<>();
        if (StringUtils.isNotBlank(customer.getRisks())) {
            String customerRisks[];
            if (customer.getRisks().contains(",")) {
                customerRisks = customer.getRisks().split(",");
            } else {
                customerRisks = new String[1];
                customerRisks[0] = customer.getRisks();
            }
            for (int indexVar = 0; indexVar < customerRisks.length; indexVar++) {
                customerFlagData.clear();
                customerFlagData.put("Customer_id", customer.getId());
                customerFlagData.put("CustomerRisk", customerRisks[indexVar]);
                customerFlagData.put("createdby", CREATED_BY);
                customerFlagData.put("modifiedby", MODIFIED_BY);
                customerFlagData.put("createdts", DEFAULT_TIME_STAMP);
                customerFlagData.put("lastmodifiedts", DEFAULT_TIME_STAMP);
                customerFlagData.put("synctimestamp", DEFAULT_TIME_STAMP);
                customerFlagData.put("softdeleteflag", SOFT_DELETE_FLAG);

                appendToSQLBuffer(CustomerSQLScriptGenerator.CUSTOMER_FLAG_STATUS, customerFlagData);
            }
        }
    }

    /**
     * Method to validate the customer data
     * 
     * @param customer
     * @return StringBuilder
     */
    private static StringBuilder validateCustomerData(Customer customer) {
        StringBuilder errorBuffer = new StringBuilder();

        if (StringUtils.isBlank(customer.getId())) {
            errorBuffer.append("->Customer Id is a mandatory input.");
        }
        if (customer.getFirstName() != null && (customer.getFirstName().length() < FIRST_NAME_MIN_CHARS
                || customer.getFirstName().length() > FIRST_NAME_MAX_CHARS)) {
            errorBuffer.append("->Customer First Name should have a minimum of " + FIRST_NAME_MIN_CHARS
                    + " characters and a maximum of " + FIRST_NAME_MAX_CHARS + " characters.");
        }
        if (customer.getLastName() != null && (customer.getLastName().length() < LAST_NAME_MIN_CHARS
                || customer.getLastName().length() > LAST_NAME_MAX_CHARS)) {
            errorBuffer.append("->Customer Last Name should have a minimum of " + LAST_NAME_MIN_CHARS
                    + " characters and a maximum of " + LAST_NAME_MAX_CHARS + " characters.");
        }
        if (StringUtils.isBlank(customer.getUsername())) {
            errorBuffer.append("->Customer Username is a mandatory input.");
        }

        if (StringUtils.isBlank(customer.getDateOfBirth())) {
            errorBuffer.append("->Customer Date of Birth is a mandatory input.");
        } else {
            try {
                Long.parseLong(customer.getDateOfBirth());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                errorBuffer
                        .append("->Invalid Date of Birth. The value must be formatted as Epoch Time in Milliseconds.");
            }
        }

        if (StringUtils.isNotBlank(customer.getCreatedtimestamp())) {
            try {
                Long.parseLong(customer.getCreatedtimestamp());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                errorBuffer.append(
                        "->Invalid Created timestamp. The value must be formatted as Epoch Time in Milliseconds.");
            }
        } else {
            Date date = new Date();
            customer.setCreatedtimestamp(Long.toString(date.getTime()));
        }

        if (StringUtils.isBlank(customer.getStatus())) {
            customer.setStatus(CUSTOMER_STATUS_DEFAULT_VALUE);
        } else {
            if (!CustomerStatus.validateAlias(customer.getStatus())) {
                errorBuffer.append("->Invalid Customer Status. Allowed Values:"
                        + ArrayUtils.toString(CustomerStatus.getAllValues()));
            }
        }
        if (!MaritalStatus.validateAlias(customer.getMaritalStatus())) {
            errorBuffer.append("->Invalid Marital Status. Allowed Values:"
                    + ArrayUtils.toString(MaritalStatus.getAllAliasValues()));
        }
        if (!EmployementStatus.validateAlias(customer.getEmployementStatus())) {
            errorBuffer.append("->Invalid Employement Status Allowed Values:"
                    + ArrayUtils.toString(EmployementStatus.getAllValues()));
        }
        if (StringUtils.isBlank(customer.getIsOlbAllowed())) {
            customer.setIsOlbAllowed(IS_OLB_ALLOWED_DEFAULT_VALUE);
        }
        if (StringUtils.isBlank(customer.getIsAssistConsented())) {
            customer.setIsAssistConsented(IS_CSRASSIST_ALLOWED_DEFAULT_VALUE);
        }
        if (StringUtils.isBlank(customer.getIsStaffMember())) {
            customer.setIsStaffMember(IS_STAFF_MEMBER_DEFAULT_VALUE);
        }
        if (StringUtils.isNotBlank(customer.getRisks())) {
            if (!CustomerFlagStatus.validateAlias(customer.getRisks())) {
                errorBuffer.append("->Invalid Risk Status. Allowed Values:"
                        + ArrayUtils.toString(CustomerFlagStatus.getAllValues()));
            }
        }

        if (StringUtils.isBlank(customer.getPrimaryRegion())) {
            errorBuffer.append("->Primary Region is a mandatory input.");
        }
        if (StringUtils.isBlank(customer.getPrimaryCity())) {
            errorBuffer.append("->Primary City is a mandatory input.");
        }
        if (!AddressType.validateAlias(customer.getPrimaryAddressType())) {
            errorBuffer.append("->Invalid Primary Address Type. Allowed Values:"
                    + ArrayUtils.toString(AddressType.getAllValues()));
        }
        if (StringUtils.isBlank(customer.getPrimaryAddressline1())) {
            errorBuffer.append("->Primary Address Line 1 is a mandatory input.");
        }
        if (StringUtils.isBlank(customer.getPrimaryZipCode())) {
            errorBuffer.append("->Primary ZipCode is a mandatory input.");
        }

        if (StringUtils.isNotBlank(customer.getSecondaryAddressline1())
                || StringUtils.isNotBlank(customer.getSecondaryAddressline2())
                || StringUtils.isNotBlank(customer.getSecondaryAddressline3())
                || StringUtils.isNotBlank(customer.getSecondaryCity())
                || StringUtils.isNotBlank(customer.getSecondaryRegion())
                || StringUtils.isNotBlank(customer.getSecondaryZipCode())
                || StringUtils.isNotBlank(customer.getSecondaryAddressType())) {

            if (StringUtils.isBlank(customer.getSecondaryRegion())) {
                errorBuffer.append("->Secondary Region is a mandatory input.");
            }
            if (StringUtils.isBlank(customer.getSecondaryCity())) {
                errorBuffer.append("->Secondary City is a mandatory input.");
            }
            if (!AddressType.validateAlias(customer.getSecondaryAddressType())) {
                errorBuffer.append("->Invalid Secondary Address Type. Allowed Values:"
                        + ArrayUtils.toString(AddressType.getAllValues()));
            }
            if (StringUtils.isBlank(customer.getSecondaryAddressline1())) {
                errorBuffer.append("->Secondary Address Line 1 is a mandatory input.");
            }
            if (StringUtils.isBlank(customer.getSecondaryZipCode())) {
                errorBuffer.append("->Secondary ZipCode is a mandatory input.");
            }
        }

        if (StringUtils.isNotBlank(customer.getTertiaryAddressline1())
                || StringUtils.isNotBlank(customer.getTertiaryAddressline2())
                || StringUtils.isNotBlank(customer.getTertiaryAddressline3())
                || StringUtils.isNotBlank(customer.getTertiaryCity())
                || StringUtils.isNotBlank(customer.getTertiaryRegion())
                || StringUtils.isNotBlank(customer.getTertiaryZipCode())
                || StringUtils.isNotBlank(customer.getTertiaryAddressType())) {

            if (StringUtils.isBlank(customer.getTertiaryRegion())) {
                errorBuffer.append("->tertiary Region is a mandatory input.");
            }
            if (StringUtils.isBlank(customer.getTertiaryCity())) {
                errorBuffer.append("->tertiary City is a mandatory input.");
            }
            if (!AddressType.validateAlias(customer.getTertiaryAddressType())) {
                errorBuffer.append("->Invalid tertiary Address Type. Allowed Values:"
                        + ArrayUtils.toString(AddressType.getAllValues()));
            }
            if (StringUtils.isBlank(customer.getTertiaryAddressline1())) {
                errorBuffer.append("->tertiary Address Line 1 is a mandatory input.");
            }
            if (StringUtils.isBlank(customer.getTertiaryZipCode())) {
                errorBuffer.append("->tertiary ZipCode is a mandatory input.");
            }
        }

        if (StringUtils.isBlank(customer.getPrimaryEmail())) {
            errorBuffer.append("->Primary Email is mandatory.");
        } else {
            if (StringUtils.isBlank(customer.getPrimaryEmailLocType())) {
                customer.setPrimaryEmailLocType(PRIMARY_EMAIL_DEFAULT_LOC_TYPE);
            } else if (ArrayUtils.contains(CommunicationExtension.values(), customer.getPrimaryEmailLocType())) {
                errorBuffer.append("->Invalid Primary Email Location Type. Allowed Values:"
                        + ArrayUtils.toString(CommunicationExtension.values()));
            }
        }

        if (StringUtils.isNotBlank(customer.getSecondaryEmail())) {
            if (StringUtils.isBlank(customer.getPrimaryEmailLocType())) {
                customer.setSecondaryEmailLocType(SECONDARY_EMAIL_DEFAULT_LOC_TYPE);
            } else if (ArrayUtils.contains(CommunicationExtension.values(), customer.getSecondaryEmailLocType())) {
                errorBuffer.append("->Invalid Secondary Email Location Type. Allowed Values:"
                        + ArrayUtils.toString(CommunicationExtension.values()));
            }
        }

        if (StringUtils.isNotBlank(customer.getTeritaryEmail())) {
            if (StringUtils.isBlank(customer.getTeritaryEmailLocType())) {
                customer.setTeritaryEmailLocType(TERITARY_EMAIL_DEFAULT_LOC_TYPE);
            } else if (ArrayUtils.contains(CommunicationExtension.values(), customer.getTeritaryEmailLocType())) {
                errorBuffer.append("->Invalid Teritary Email Location Type. Allowed Values:"
                        + ArrayUtils.toString(CommunicationExtension.values()));
            }
        }

        if (StringUtils.isBlank(customer.getPrimaryPhone())) {
            errorBuffer.append("->Primary Phone is mandatory.");
        } else {
            if (StringUtils.isBlank(customer.getPrimaryPhoneLocType())) {
                customer.setPrimaryPhoneLocType(PRIMARY_PHONE_DEFAULT_LOC_TYPE);
            } else if (ArrayUtils.contains(CommunicationExtension.values(), customer.getPrimaryPhoneLocType())) {
                errorBuffer.append("->Invalid Primary Phone Location Type. Allowed Values:"
                        + ArrayUtils.toString(CommunicationExtension.values()));
            }
        }

        if (StringUtils.isNotBlank(customer.getSecondaryPhone())) {
            if (StringUtils.isBlank(customer.getSecondaryPhoneLocType())) {
                customer.setPrimaryPhoneLocType(SECONDARY_PHONE_DEFAULT_LOC_TYPE);
            } else if (ArrayUtils.contains(CommunicationExtension.values(), customer.getSecondaryPhoneLocType())) {
                errorBuffer.append("->Invalid Secondary Phone Location Type. Allowed Values:"
                        + ArrayUtils.toString(CommunicationExtension.values()));
            }
        }

        if (StringUtils.isNotBlank(customer.getTeritaryPhone())) {
            if (StringUtils.isBlank(customer.getTeritaryPhoneLocType())) {
                customer.setPrimaryPhoneLocType(TERITARY_PHONE_DEFAULT_LOC_TYPE);
            }
            if (ArrayUtils.contains(CommunicationExtension.values(), customer.getTeritaryPhoneLocType())) {
                errorBuffer.append("->Invalid tertiary Phone Location Type. Allowed Values:"
                        + ArrayUtils.toString(CommunicationExtension.values()));
            }
        }
        errorBuffer.trimToSize();
        return errorBuffer;
    }

    /**
     * Method to map the current data to the SQL template, and to append the generated SQL statement to the SQL Buffer
     * 
     * @param template
     * @param dataMap
     */
    private static void appendToSQLBuffer(String template, Map<String, String> dataMap) {
        sqlBuffer.append(StrSubstitutor.replace(template, dataMap)).append(LINE_SEPARATOR);
    }

}