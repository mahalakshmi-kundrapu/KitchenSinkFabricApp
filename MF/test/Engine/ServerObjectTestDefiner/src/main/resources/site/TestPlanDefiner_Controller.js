var typeOperators = {
    "string": {
        "equals": {
            "operandValueType": "string"
        },
        "equalsIgnoreCase": {
            "operandValueType": "string"
        },
        "equalsAny": {
            "operandValueType": "string"
        },
        "equalsAnyIgnoreCase": {
            "operandValueType": "string"
        },
        "contains": {
            "operandValueType": "string"
        },
        "containsIgnoreCase": {
            "operandValueType": "string"
        },
        "notEquals": {
            "operandValueType": "string"
        },
        "notEqualsIgnoreCase": {
            "operandValueType": "string"
        },
        "notContains": {
            "operandValueType": "string"
        },
        "notContainsIgnoreCase": {
            "operandValueType": "string"
        },
        "hasLength": {
            "operandValueType": "number"
        }
    },
    "number": {
        "equals": {
            "operandValueType": "number"
        },
        "equalsAny": {
            "operandValueType": "string"
        },
        "notEquals": {
            "operandValueType": "number"
        },
        "greaterThan": {
            "operandValueType": "number"
        },
        "lesserThan": {
            "operandValueType": "number"
        },
        "greaterThanOrEqualsTo": {
            "operandValueType": "number"
        },
        "lesserThanOrEqualsTo": {
            "operandValueType": "number"
        }
    },
    "array": {
        "hasNElements": {
            "operandValueType": "number"
        },
        "hasAtleastNElements": {
            "operandValueType": "number"
        },
        "hasAtmostNElements": {
            "operandValueType": "number"
        },
        "hasElement": {
            "operandValueType": "string"
        },
        "notHasElement": {
            "operandValueType": "string"
        }
    },
    "boolean": {
        "equals": {
            "operandValueType": "boolean"
        },
        "notEquals": {
            "operandValueType": "boolean"
        }
    },
    "object": {},
    "null": {}
}

/*Service URLs*/
var fetchRuntimeDataURL = '/ServerObjectTestDefiner/rest/Runtimes';
var fetchEnvironmentDataURL = '/ServerObjectTestDefiner/rest/Environment';
var executeRequestURL = '/ServerObjectTestDefiner/rest/Request/executeRequest';
var executeTestPlanURL = '/ServerObjectTestDefiner/rest/TestPlan/executeTestPlan';

/*Service Article Variables*/
var serviceJSON = {};
var name; //Service Name - SCOPE - Service Definition
var url; //Service URL - SCOPE - Service Definition
var moduleName //Service Module Name - SCOPE - Service Definition
var httpMethod; //Request Method - SCOPE - Service Definition
var tests = []; //Test Cases - SCOPE - Service Definition

var responseHeaderAssertions = []; //Response Header Assertions - SCOPE - Test Definition
var responseBodyAssertions = []; //Response Body Assertions - SCOPE - Test Definition

var currTestID; //ID of the Test case being edited
var editTestMode = false; //True when imported test case is being edited

var variableCollection = {}; // Variable collection object. Holds placeholders and corresponding values

var runtimesList;
var selectedRuntimeData;

//Function that gets executed after completion of Form-load event
window.onload = function () {
    addDefaultValues();
};

//Function to add Default values to Form
function addDefaultValues() {
    //Add Default ContentType Header
    addFieldsToRequestHeaderTable('Content-Type', 'application/json');

    //Default to Response Body for Assertions
    document.getElementById('responseBody_radio_btn').checked = true;

    //Set Runtime Data
    fetchRuntimeListData();

    //Set Input-Output Fields Table
    addRowToInputParamaterTable();
    addRowToOutputParamaterTable();

    document.getElementById('selectAssertions_chk').style.visibility = 'hidden';
    document.getElementById('authPayloadBody_txtArea').disabled = true;
    document.getElementById('authPayloadHeaders_txtArea').disabled = true;
    setAssertionOperator('string');
}

//Function to handle click event on 'Export Service' button
function exportService_btnClick() {
    try {
        clearErrorNotations();

        //Check Service Module Name
        moduleName = document.getElementById('serviceModuleName_input').value;
        if (isEmptyVar(moduleName)) {
            errorMessage = "Module name cannot be empty."
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Invalid Service Definition. Info:<br>" + errorMessage + "</p>"
            });
            $('html,body').animate({
                scrollTop: $('#serviceOverviewDiv').offset().top
            }, 'slow');
            document.getElementById('serviceModuleName_input').style.border = '1px solid red';
            return;
        }

        //Check Service Name
        name = document.getElementById('serviceName_input').value;
        if (isEmptyVar(name)) {
            errorMessage = "Service name cannot be empty."
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Invalid Service Definition. Info:<br>" + errorMessage + "</p>"
            });
            $('html,body').animate({
                scrollTop: $('#serviceOverviewDiv').offset().top
            }, 'slow');
            document.getElementById('serviceName_input').style.border = '1px solid red';
            return;
        }

        if (tests == undefined || tests == null || tests.length == 0) {
            errorMessage = "<p>No Test Cases defined</p>"
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: errorMessage
            });
            return;
        }
        exportServiceToFile();
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to get the service(aka TestPlan) as JSON
function getServiceAsJSON() {
    name = document.getElementById('serviceName_input').value;
    moduleName = document.getElementById('serviceModuleName_input').value;
    url = document.getElementById('serviceURL_input').value;
    httpMethod = document.getElementById('requestMethod_select').value;
    description = document.getElementById('serviceDescription_textArea').value;

    serviceJSON.description = trimString(description);
    serviceJSON.inputFieldsDocument = getInputFieldsDocument();
    serviceJSON.outputFieldsDocument = getOutputFieldsDocument();

    serviceJSON.name = trimString(name);
    serviceJSON.url = processServiceURL(url);
    serviceJSON.moduleName = trimString(moduleName);

    serviceJSON.httpMethod = httpMethod;
    serviceJSON.tests = tests;
    serviceJSON = sanitisePayload(serviceJSON);
    return serviceJSON;
}

//Function to export Service to File
function exportServiceToFile() {
    try {
        var serviceJSON = getServiceAsJSON();
        let dataStr = JSON.stringify(serviceJSON, null, 4);
        let dataUri = 'data:application/json;charset=utf-8,' + encodeURIComponent(dataStr);

        let exportFileDefaultName = 'Service_' + trimString(moduleName) + "_" + trimString(name) + '.json';

        let linkElement = document.createElement('a');
        linkElement.setAttribute('href', dataUri);
        linkElement.setAttribute('download', exportFileDefaultName);
        linkElement.click();
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Funtion to process the Service URL
function processServiceURL(serviceURL) {
    if (typeof serviceURL == 'string') {
        serviceURL = trimString(serviceURL);

        //Remove the leading '/' from a Service URL
        if (serviceURL.startsWith("/")) {
            serviceURL = serviceURL.substr(1);
        }
        return serviceURL;
    }
}

//Function to handle click event of 'Save Test Case' Button
function saveTestCase_btnClick() {
    try {
        clearErrorNotations();

        //Reset the select-all test cases check box
        document.getElementById('selectTests_chk').checked = false;

        if (editTestMode == true) {
            var currTestName;
            for (var currRef in tests) {
                if (tests[currRef].id == currTestID) {
                    currTestName = tests[currRef].name;
                    break;
                }
            }
            //  Confirmation to Save changes to existing Test Case 
            $.confirm({
                title: "Confirmation",
                content: "<p>Save changes made to Test '" + currTestName + "'? This action cannot be undone.</p>",
                boxWidth: "40%",
                useBootstrap: false,
                buttons: {
                    "Cancel": function () {
                        return; //Do nothing
                    },
                    "Discard Changes": function () {
                        //Refresh Test cases List Table
                        refreshTestCasesListTable();

                        //Reset Test case DOM elements
                        clearTestCaseElements();
                        addFieldsToRequestHeaderTable('Content-Type', 'application/json');
                        editTestMode = false;
                    },
                    "Save Changes": {
                        text: 'Save Changes',
                        btnClass: 'btn-blue',
                        keys: ['enter', 'shift'],
                        action: function () {
                            //Validate Test Case Definition and save Test Case
                            saveTestCase();
                        }
                    }
                }
            });
        } else {
            //Validate Test Case Definition and save Test Case
            saveTestCase();
        }

    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to handle validate Test Case Definition and save Test Case
function saveTestCase() {

    var testName = document.getElementById('testName_input').value;
    var testPriority = document.getElementById('testPriority_select').value;
    var securityLevel = document.getElementById('securityLevel_select').value;
    var identityService = document.getElementById('identityService_select').value;
    var testSequence = document.getElementById('testSequence_txt').value;
    var requestBody = document.getElementById('requestBody_txtArea').value;
    var isCustomAuthTest = document.getElementById('customAuthPayload_chk').checked;
    var customAuthBody = document.getElementById('authPayloadBody_txtArea').value;
    var customAuthHeaders = document.getElementById('authPayloadHeaders_txtArea').value;
    var requestHeaders = getRequestHeaders();

    //Sanitise Request Body
    requestBody = sanitisePayload(requestBody);

    if (isCustomAuthTest === false) {
        //To Ignore values fetched from DOM elements
        customAuthBody = "";
        customAuthHeaders = "{}";
    }

    //Validate Inputs
    var isValidContent = true;
    var errorMessage = '';
    if (isEmptyVar(testName)) {
        isValidContent = false;
        errorMessage = "&nbsp&nbsp'Test Name' cannot be empty.";
        document.getElementById('testName_input').style.border = '1px solid red';
    }
    if (isEmptyVar(testSequence) || isNaN(testSequence) || testSequence < 1) {
        isValidContent = false;
        errorMessage = errorMessage + "&nbsp&nbsp'Test Sequence' must be a positive number.";
        document.getElementById('testSequence_txt').style.border = '1px solid red';
    }

    try {
        if (!isEmptyVar(customAuthHeaders)) {
            customAuthHeaders = JSON.parse(customAuthHeaders);
        } else {
            customAuthHeaders = {};
        }

    } catch (err) {
        isValidContent = false;
        errorMessage = errorMessage + "&nbsp&nbsp'Custom Auth Headers' must be a valid JSON.";
        document.getElementById('testSequence_txt').style.border = '1px solid red';
    }
    errorMessage = errorMessage.split('.').join("<br>")
    if (isValidContent == false) {
        $.alert({
            title: 'Error',
            type: 'red',
            boxWidth: '35%',
            useBootstrap: false,
            content: "<p>Invalid Test Definition. Info:<br>" + errorMessage + "</p>"
        });
        $('html,body').animate({
            scrollTop: $('#testDefinitionDiv').offset().top
        }, 'slow');
        console.log('Invalid Test Definition:' + errorMessage);
        return;
    }

    //Verify if the Test Name is unique
    for (var index = 0; index < tests.length; index++) {
        if (tests[index].name == testName && tests[index].id != currTestID) {
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Test with the name '" + testName + "' already exists</p>",
            });
            $('html,body').animate({
                scrollTop: $('#testDefinitionDiv').offset().top
            }, 'slow');
            document.getElementById('testName_input').style.border = '1px solid red';
            return;
        }
    }

    //Verify if the Test Sequence is valid
    for (var index = 0; index < tests.length; index++) {
        if (tests[index].testSequence == testSequence && tests[index].id != currTestID) {
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Test with the sequence '" + testSequence + "' already exists</p>",
            });
            $('html,body').animate({
                scrollTop: $('#testDefinitionDiv').offset().top
            }, 'slow');
            document.getElementById('testSequence_txt').style.border = '1px solid red';
            return;
        }
    }

    //Verify if atleast one assertion has been added
    if (responseBodyAssertions.length + responseHeaderAssertions.length == 0) {
        $.alert({
            title: 'Error',
            type: 'red',
            boxWidth: '35%',
            useBootstrap: false,
            content: "<p>Please add atleast one Assertion to proceed</p>",
        });
        return;
    }

    var currTestObject = {};
    if (editTestMode == true) {
        //Implies an update-scenario of an existing test case
        for (var currRef in tests) {
            if (tests[currRef].id == currTestID) {
                currTestObject.id = currTestID;
                currTestObject.name = trimString(testName);
                currTestObject.identityService = identityService;
                currTestObject.securityLevel = securityLevel;
                currTestObject.testPriority = testPriority;
                currTestObject.testSequence = parseInt(testSequence, 10);
                currTestObject.requestBody = trimString(requestBody);
                currTestObject.requestHeaders = requestHeaders;
                currTestObject.responseBodyAssertions = responseBodyAssertions;
                currTestObject.responseHeaderAssertions = responseHeaderAssertions;
                currTestObject.isCustomAuthTest = isCustomAuthTest;
                currTestObject.customAuthBody = customAuthBody;
                currTestObject.customAuthHeaders = customAuthHeaders;
                updateTestObjectAtStore(currTestID, currTestObject);
            }
        }

        //Reset the Security Level
        setSecurityLevelToAuthenticatedAppUser();

        //Refresh Test cases List Table
        refreshTestCasesListTable();

        //Reset the Edit Test Case Mode
        editTestMode = false;

        //Reset Test Case DOM elements
        clearTestCaseElements();
        addFieldsToRequestHeaderTable('Content-Type', 'application/json');

    } else {
        $.confirm({
            title: 'Confirmation',
            boxWidth: '40%',
            useBootstrap: false,
            content: '<p>Click <i>\'OK\'</i> to save the test-case.<br>Click <i>\'Cancel\'</i> to add more assertions to the test-case.</p>',
            buttons: {
                OK: function () {
                    addTestCaseToStore(testName, securityLevel, identityService, testPriority, testSequence, requestBody, requestHeaders, customAuthBody, customAuthHeaders, isCustomAuthTest, responseBodyAssertions, responseHeaderAssertions);

                    //Reset the Security Level
                    setSecurityLevelToAuthenticatedAppUser();

                    //Refresh Test cases List Table
                    refreshTestCasesListTable();

                    //Reset Test Case DOM elements
                    clearTestCaseElements();
                    addFieldsToRequestHeaderTable('Content-Type', 'application/json');
                },
                Cancel: function () {
                    //Do nothing
                }
            }
        });
    }
}

function trimString(input) {
    if (typeof input == 'string') {
        return input.trim();
    }
    return input;
}

//Function to reset Test Case DOM elements
function clearTestCaseElements() {

    clearErrorNotations();

    document.getElementById('testName_input').style.border = '1px solid #ccc';
    document.getElementById('testSequence_txt').style.border = '1px solid #ccc';
    document.getElementById('requestBody_txtArea').style.border = '1px solid #ccc';

    document.getElementById('testName_input').value = '';
    document.getElementById('testSequence_txt').value = '';
    document.getElementById('requestBody_txtArea').value = '';

    document.getElementById('testPriority_select').selectedIndex = 0;
    document.getElementById('identityService_select').selectedIndex = 0;

    //Clear Request Headers Table
    clearRequestHeadersTable();

    //Clear Global Variables
    responseHeaderAssertions = [];
    responseBodyAssertions = [];

    //Clear Assertions Table
    clearResponseAssertionsFields();
    clearAssertionsListTable();

    //Clear Service Response
    clearServiceResponse();

    //Set Default Auth Payload
    setDefaultAuthPayload();
}

//Function to update the Test object at the store
function updateTestObjectAtStore(testID, testObject) {
    try {
        var tempArray = [];
        tempArray.push(testID);
        //Remove existing instance from the store
        removeTestCasesFromStore(tempArray);

        //Push the test object with updated values
        tests.push(testObject);
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to add current instance of Test Case to Store
function addTestCaseToStore(testName, securityLevel, identityService, testPriority, testSequence, requestBody, requestHeaders, customAuthBody, customAuthHeaders, isCustomAuthTest, responseBodyAssertions, responseHeaderAssertions) {
    //Prepare Aggregated Object for current instance of 'Test'
    try {
        var currTestObject = {};
        currTestObject.id = getRandomNumber() + '';
        currTestObject.name = testName;
        currTestObject.securityLevel = securityLevel;
        currTestObject.identityService = identityService;
        currTestObject.testPriority = testPriority;
        currTestObject.testSequence = parseInt(testSequence, 10);
        currTestObject.requestBody = requestBody;
        currTestObject.requestHeaders = requestHeaders;
        currTestObject.responseBodyAssertions = responseBodyAssertions;
        currTestObject.responseHeaderAssertions = responseHeaderAssertions;
        currTestObject.customAuthBody = customAuthBody;
        currTestObject.customAuthHeaders = customAuthHeaders;
        currTestObject.isCustomAuthTest = isCustomAuthTest;
        //Insert/Update current instance of test into global array
        tests.push(currTestObject);
        return currTestObject;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to add current Instance of Test to TestCasesList Table
function addTestObjectToTestCasesListTable(currTestObject) {
    try {
        var testCasesListTable = document.getElementById('testCasesList_table');
        var currRow, currCell, currCountOfRows, chkbox, currTestObject, countOfAssertions, currTestSequence;
        currCountOfRows = testCasesListTable.rows.length;

        //Add row at last but one postion
        currRow = testCasesListTable.insertRow(currCountOfRows - 1);

        //Add checkbox to cell[0]
        currCell = currRow.insertCell(-1);
        chkbox = document.createElement('input');
        chkbox.type = 'checkbox';
        chkbox.value = currTestObject.id;
        currCell.appendChild(chkbox);
        currCell.style.textAlign = 'center';

        //Test Name cell
        currCell = currRow.insertCell(-1);
        currCell.innerText = currTestObject.name;

        //Assertions Count cell
        currCell = currRow.insertCell(-1);
        countOfAssertions = currTestObject.responseBodyAssertions.length + currTestObject.responseHeaderAssertions.length;
        currCell.innerText = countOfAssertions;

        // Test Sequence cell
        currCell = currRow.insertCell(-1);
        currTestSequence = currTestObject.testSequence;
        currCell.innerText = currTestSequence;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to remove current instance of Test from 'Test Cases' Table
function removeTestObjectFromTestCasesTable(testID) {
    try {
        var testCasesListTable = document.getElementById('testCasesList_table');
        var testCasesListTableRows = testCasesListTable.rows;
        var countOfRows = testCasesListTableRows.length;
        var currRow, currCell, chkbox;
        for (var index = 0; index < currCountOfRows; index++) {
            currRow = testCasesListTableRows[index];
            currCell = currRow.cells[0];
            chkbox = currCell.firstChild;
            if (chkbox.value == testID) {
                testCasesListTable.deleteRow(index);
            }
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to handle click event of 'Add Assertion' Button
function addAssertion_btnClick() {

    try {

        //Reset border colors
        clearErrorNotations();

        //Fetch values from DOM elements
        var assertionPath = document.getElementById('responsePathExpression_txt').value;
        var dataType = document.getElementById('responseExpectedDataType_select').value;
        var value = document.getElementById('responseExpectedValue_input').value;
        var isValueAgnostic = document.getElementById('isValueAgnostic_chk').checked;
        var isNullable = document.getElementById('isNullable_chk').checked;
        var isSaveValueSelected = document.getElementById('saveValue_chk').checked;
        var variableName = document.getElementById('variableName_txt').value;

        //Validate values
        var isValidContent = true;
        var errorMessage = '';
        var operator;

        if (isEmptyVar(assertionPath)) {
            isValidContent = false;
            errorMessage = "&nbsp;&nbsp;&nbsp;'Assertion Path' cannot be empty.";
            document.getElementById('responsePathExpression_txt').style.border = '1px solid red';
        }

        if (isEmptyVar(value) && !isValueAgnostic) {
            isValidContent = false;
            errorMessage = errorMessage + "&nbsp;&nbsp;&nbsp;'Assertion Value' cannot be empty.";
            document.getElementById('responseExpectedValue_input').style.border = '1px solid red';
        }

        if (isEmptyVar(variableName) && isSaveValueSelected) {
            isValidContent = false;
            errorMessage = errorMessage + "&nbsp;&nbsp;&nbsp;'Variable Name' cannot be empty when 'Save Value' is selected";
            document.getElementById('variableName_txt').style.border = '1px solid red';
        }

        if (!isValueAgnostic) {
            operator = document.getElementById('assertionOperator_select').value;
            if (typeOperators[dataType][operator] === undefined || typeOperators[dataType][operator].operandValueType === undefined) {
                isValidContent = false;
                errorMessage = errorMessage + "&nbsp;&nbsp;&nbsp;'Invalid Value Type Configuration.";
                document.getElementById('responseExpectedValue_input').style.border = '1px solid red';
            } else {
                if (typeOperators[dataType][operator].operandValueType == "number" && isNaN(value)) {
                    isValidContent = false;
                    errorMessage = errorMessage + "&nbsp;&nbsp;&nbsp;'Assertion Value' should be of type 'number' in this context.";
                    document.getElementById('responseExpectedValue_input').style.border = '1px solid red';
                }

                if (typeOperators[dataType][operator].operandValueType == "boolean" && (value.toUpperCase() != "TRUE" && value.toUpperCase() != "FALSE")) {
                    isValidContent = false;
                    errorMessage = errorMessage + "&nbsp;&nbsp;&nbsp;'Assertion Value' should be of type 'boolean' in this context.";
                    document.getElementById('responseExpectedValue_input').style.border = '1px solid red';
                }
            }
        } else {
            operator = "";
        }

        if (isVariableDefined(variableName) === true) {
            isValidContent = false;
            errorMessage = errorMessage + "&nbsp;&nbsp;&nbsp;Variable with the name '" + variableName + "' has already been defined."
            document.getElementById('variableName_txt').style.border = '1px solid red';
        }

        errorMessage = errorMessage.split('.').join("<br>")
        //Invalid payload
        if (isValidContent == false) {
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Invalid Assertion Definition. Info:<br>" + errorMessage
            });
            console.log("Invalid Assertion Definition:" + errorMessage);
            return;
        }

        //Add value to Variable collection Map
        if (!isEmptyVar(variableName)) {
            var attributeValue;
            if (document.getElementById('responseBody_radio_btn').checked == true) {
                var responseBodyStr = document.getElementById('responseBody_txtArea').value;
                if (!isEmptyVar(responseBodyStr)) {
                    var responseBodyJSON = JSON.parse(responseBodyStr);
                    attributeValue = jsonPath(responseBodyJSON, assertionPath);
                }

            } else {
                var responseHeaderStr = document.getElementById('responseHeaders_txtArea').value;
                if (!isEmptyVar(responseHeaderStr)) {
                    var responseHeadersJSON = JSON.parse(responseHeaderStr);
                    attributeValue = jsonPath(responseHeadersJSON, assertionPath);
                }
            }
            if (attributeValue == false || isEmptyVar(attributeValue)) {
                $.alert({
                    title: 'Information',
                    type: 'dark',
                    boxWidth: '35%',
                    useBootstrap: false,
                    content: "<p>No value found at the given path. Cannot add value with the name '" + variableName + "' to the collection map.<br>Proceeding with adding the Assertion.</p>"
                });
            } else {
                variableCollection[variableName] = attributeValue;
            }
        }

        var assertionSource;
        if (document.getElementById('responseBody_radio_btn').checked == true) {
            assertionSource = 'Response Body';
        } else {
            assertionSource = 'Response Header';
        }

        var assertionObject = addAssertionToStore(assertionPath, dataType, value, operator, assertionSource, isValueAgnostic, isNullable, variableName);
        addAssertionToAssertionTable(assertionObject, assertionSource);
        clearResponseAssertionsFields();
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function tpo replace place-holders with stored value. Place holder Syntax: {{VARIABLE_NAME}}
function substitutePlaceHoldersInRequestBody(requestBody) {
    for (var variableName in variableCollection) {
        requestBody = requestBody.replace("{{" + variableName + "}}", variableCollection[variableName]);
    }
    return requestBody;
}

//Function to clear the entries of Response Assertions Fields
function clearResponseAssertionsFields() {

    document.getElementById('saveValue_chk').checked = false;

    document.getElementById('variableName_txt').value = '';
    document.getElementById('variableName_txt').disabled = true;
    document.getElementById('variableName_txt').placeholder = 'Not Applicable';
    document.getElementById('variableName_txt').style.border = '1px solid #ccc';

    document.getElementById('assertionOperator_select').disabled = false;
    document.getElementById('responsePathExpression_txt').style.border = '1px solid #ccc';
    document.getElementById('responseExpectedValue_input').style.border = '1px solid #ccc';

    document.getElementById('isNullable_chk').checked = false;
    document.getElementById('isValueAgnostic_chk').checked = false;
    document.getElementById('responseExpectedValue_input').disabled = false;
    document.getElementById('responseExpectedValue_input').placeholder = "Assertion Value(s)";

    document.getElementById('responsePathExpression_txt').value = '';
    document.getElementById('responseExpectedValue_input').value = '';
    document.getElementById('responseExpectedDataType_select').selectedIndex = 0;
    setAssertionOperator(document.getElementById('responseExpectedDataType_select').value);
}

//Function to add Assertion to the Store Object
function addAssertionToStore(assertionPath, dataType, value, operator, assertionSource, isValueAgnostic, isNullable, variableName) {
    try {
        var currAssertionObject = {};

        assertionId = getRandomNumber() + '';
        currAssertionObject.id = assertionId;
        currAssertionObject.path = assertionPath;
        currAssertionObject.dataType = dataType;
        currAssertionObject.value = value;
        currAssertionObject.isNullable = isNullable;
        currAssertionObject.isValueAgnostic = isValueAgnostic;
        currAssertionObject.operator = operator;
        if (assertionSource == 'Response Body') {
            responseBodyAssertions.push(currAssertionObject);
        } else if (assertionSource == 'Response Header') {
            responseHeaderAssertions.push(currAssertionObject);
        }
        if (!isEmptyVar(variableName)) {
            currAssertionObject.variableName = variableName;
        }
        return currAssertionObject; //returning added assertion object for optional verification/consumption
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Funtion to add an Assertion Object to Assertion Table - Internal Method
function addAssertionToAssertionTable(currObject, source) {
    try {
        var assertionsTable = document.getElementById('testAssertionsList_table');

        var currRow, currCell, chkbox;
        currCountOfRows = assertionsTable.rows.length;

        //Add row last but one position from the bottom
        currRow = assertionsTable.insertRow(currCountOfRows - 1);

        //Add checkbox to cell[0]
        currCell = currRow.insertCell(-1);
        chkbox = document.createElement('input');
        chkbox.type = 'checkbox';
        chkbox.value = currObject.id;
        currCell.appendChild(chkbox);
        currCell.style.textAlign = 'center';

        //Add assertionPath to cell[1]
        currCell = currRow.insertCell(-1);
        currCell.innerText = currObject.path;

        //Add dataType to cell[2]
        currCell = currRow.insertCell(-1);
        if (currObject.isValueAgnostic == false) {
            currCell.innerText = currObject.dataType + " - " + currObject.operator;
        } else {
            currCell.innerText = currObject.dataType;
        }

        //Add expectedValue to cell[3]
        currCell = currRow.insertCell(-1);
        currCell.innerText = currObject.value;

        //Add source to cell[4]
        currCell = currRow.insertCell(-1);
        currCell.innerText = source;
        currCell.style.textAlign = 'center';

        //Add source to cell[5]
        currCell = currRow.insertCell(-1);
        currCell.innerText = capitalizeFirstLetter(currObject.isValueAgnostic.toString());
        currCell.style.textAlign = 'center';

        //Add source to cell[6]
        currCell = currRow.insertCell(-1);
        currCell.innerText = capitalizeFirstLetter(currObject.isNullable.toString());
        currCell.style.textAlign = 'center';
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to delete the entries from the Assertion Table
function clearAssertionsListTable() {
    try {
        var assertionsTable = document.getElementById('testAssertionsList_table');
        var currCountOfRows = assertionsTable.rows.length;
        for (var index = 1; index < currCountOfRows - 1; index++) {
            assertionsTable.deleteRow(1);
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to delete the entries from the Request Headers Table
function clearRequestHeadersTable() {
    try {
        var requestHeadersTable = document.getElementById('requestHeaders_table');
        var currCountOfRows = requestHeadersTable.rows.length;
        for (var index = 0; index < currCountOfRows; index++) {
            requestHeadersTable.deleteRow(0);
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to delete entries from the Test Cases Table
function clearTestCasesListTable() {
    var testCasesTable = document.getElementById('testCasesList_table');
    var currCountOfRows = testCasesTable.rows.length;
    for (var index = 2; index < currCountOfRows - 1; index++) {
        testCasesTable.deleteRow(2);
    }
}

//Function to refresh the Assertion List Table
function refreshAssertionsListTable() {
    try {

        clearAssertionsListTable();
        var currObject;

        for (var currRef in responseBodyAssertions) {
            currObject = responseBodyAssertions[currRef];
            addAssertionToAssertionTable(currObject, 'Response Body');
            // index++;
        }

        for (var currRef in responseHeaderAssertions) {
            currObject = responseHeaderAssertions[currRef];
            addAssertionToAssertionTable(currObject, 'Response Headers');
            // index++;
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }

}

//Function to handle click event of 'Remove Assertion' button
function deleteResponseAssertion_btnClick() {
    try {
        //Reset border colors
        clearErrorNotations();

        var currRow, currCell, currCheckBox;
        var assertionsTable = document.getElementById('testAssertionsList_table');
        var assertionTableRows = assertionsTable.rows;
        var assertionTableRowsCount = assertionTableRows.length;
        if (assertionTableRowsCount <= 2) {
            //Row 0 -> Header Elements
            //Row 1 -> Remove Assertion Button
            return;
        }
        //Remove selected assertions from Store
        var targetIds = [];
        for (var index = 1; index < assertionTableRowsCount - 1; index++) {
            currRow = assertionTableRows[index];
            currCell = currRow.cells[0];
            currCheckBox = currCell.firstChild;
            if (currCheckBox.checked == true) {
                targetIds.push(currCheckBox.value); //Assertion ID
            }
        }
        removeAssertionsFromStore(targetIds);
        //Refresh Assertions List Table
        refreshAssertionsListTable();

        if (responseBodyAssertions.length + responseHeaderAssertions.length == 0) {
            //No Assertions
            document.getElementById('selectAssertions_chk').style.visibility = 'hidden';
        }
        document.getElementById('selectAssertions_chk').checked = false;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Funtion to remove Assertions from the Store Object
function removeAssertionsFromStore(targetIds) {
    try {
        responseHeaderAssertions = responseHeaderAssertions.filter((e) => {
            return !targetIds.includes(e.id);
        });
        responseBodyAssertions = responseBodyAssertions.filter((e) => {
            return !targetIds.includes(e.id);
        });
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Returns the IDs of the selected Test cases
function getSelectedTestCases() {
    try {
        var currRow, currCell, chkBox;
        var testCasesTable = document.getElementById('testCasesList_table');
        var testCasesTableRows = testCasesTable.rows;
        var testCasesTableRowsCount = testCasesTableRows.length;

        var selectedTestCases = [];
        for (var index = 2; index < testCasesTableRowsCount - 1; index++) {
            currRow = testCasesTableRows[index];
            currCell = currRow.cells[0];
            chkBox = currCell.firstChild;
            if (chkBox.checked == true) {
                selectedTestCases.push(chkBox.value); //Test Case ID
            }
        }
        return selectedTestCases;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to uncheck selections of Test Cases
function uncheckSelectedTestCases() {
    try {
        var currRow, currCell, chkBox;
        var testCasesTable = document.getElementById('testCasesList_table');
        var testCasesTableRows = testCasesTable.rows;
        var testCasesTableRowsCount = testCasesTableRows.length;

        var selectedTestCases = [];
        for (var index = 2; index < testCasesTableRowsCount - 1; index++) {
            currRow = testCasesTableRows[index];
            currCell = currRow.cells[0];
            chkBox = currCell.firstChild;
            chkBox.checked = false;
        }
        return selectedTestCases;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to handle click event of 'Remove Test Case'
function removeTestCase_btnClick() {
    try {
        clearErrorNotations();

        var targetIds = getSelectedTestCases();

        if (!isEmptyVar(targetIds) || targetIds.length > 0) {
            $.confirm({
                type: 'dark',
                boxWidth: "40%",
                title: 'Confirmation',
                useBootstrap: false,
                content: '<p>Do you want to remove the selected Test Case(s)?<br>This action cannot be undone.</p>',
                buttons: {
                    Remove: function () {
                        removeTestCasesFromStore(targetIds);
                        refreshTestCasesListTable();

                        if (editTestMode == true) {
                            //Clear Test Fields
                            document.getElementById('testName_input').value = '';
                            document.getElementById('testPriority_select').selectedIndex = 0;
                            document.getElementById('identityService_select').selectedIndex = 0;
                            document.getElementById('testSequence_txt').value = '';
                            document.getElementById('requestBody_txtArea').value = '';

                            //Clear Request Headers Table and add Default ContentType Header
                            clearRequestHeadersTable();
                            addFieldsToRequestHeaderTable('Content-Type', 'application/json');

                            //Clear Global Variables
                            responseHeaderAssertions = [];
                            responseBodyAssertions = [];

                            //Clear Assertions Table
                            clearAssertionsListTable();

                            //Clear Service Response
                            clearServiceResponse();

                            //Reset the Edit Mode Value
                            editTestMode = false;
                        }
                    },
                    Cancel: function () {
                        //Do Nothing
                    }
                }
            });
        }

    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to refresh the Assertion List Table
function refreshTestCasesListTable() {
    try {
        clearTestCasesListTable();
        sortTestsArray();
        for (var currRef in tests) {
            addTestObjectToTestCasesListTable(tests[currRef]);
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to sort Test cases Array
function sortTestsArray() {
    try {
        tests = tests.sort(dynamicSort('testSequence'));
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

/*Function so sort objects on a specfic property.
Function invoked on an Array object*/
function dynamicSort(property) {
    try {
        var sortOrder = 1;
        if (property[0] === '-') {
            sortOrder = -1;
            property = property.substr(1);
        }
        return function (a, b) {
            var result = (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
            return result * sortOrder;
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to remove Test cases from the Store Object
function removeTestCasesFromStore(targetIds) {
    try {
        tests = tests.filter((e) => {
            return !targetIds.includes(e.id);
        });
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to handle click event of 'Add Header' button
function addHeader_btnClick() {
    try {
        addFieldsToRequestHeaderTable(null, null);
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to handle key press event of output-parameter field
function outputParameterField_onKeyPress() {
    var tableReferece = document.getElementById('serviceDescription_OutputFieldsTable');
    var lastRow = tableReferece.rows[tableReferece.rows.length - 1];
    var currDescriptionCell = lastRow.cells[1];
    var currDescriptionField = currDescriptionCell.firstChild;
    if (!isEmptyVar(currDescriptionField.value)) {
        addRowToOutputParamaterTable();
    }
}

//Function to handle key press event of output-parameter field
function outputParameterField_onPaste(event) {
    var content;
    event.preventDefault();
    if (event.clipboardData) {
        content = event.clipboardData.getData('text/plain');
        document.execCommand('insertText', false, content);
    } else if (window.clipboardData) {
        content = window.clipboardData.getData('Text');
        if (window.getSelection) {
            window.getSelection().getRangeAt(0).insertNode(document.createTextNode(content));
        }
    }
    if (!isEmptyVar(content)) {
        var tableReferece = document.getElementById('serviceDescription_OutputFieldsTable');
        var lastRow = tableReferece.rows[tableReferece.rows.length - 1];
        var currDescriptionCell = lastRow.cells[1];
        var currDescriptionField = currDescriptionCell.firstChild;
        if (!isEmptyVar(currDescriptionField.value)) {
            addRowToOutputParamaterTable();
        }
    }
}

//Function to handle key press event of input-parameter field
function inputParameterField_onPaste(event) {
    var content;
    event.preventDefault();
    if (event.clipboardData) {
        content = event.clipboardData.getData('text/plain');
        document.execCommand('insertText', false, content);
    } else if (window.clipboardData) {
        content = window.clipboardData.getData('Text');
        if (window.getSelection) {
            window.getSelection().getRangeAt(0).insertNode(document.createTextNode(content));
        }
    }
    if (!isEmptyVar(content)) {
        var tableReferece = document.getElementById('serviceDescription_InputFieldsTable');
        var lastRow = tableReferece.rows[tableReferece.rows.length - 1];
        var currDescriptionCell = lastRow.cells[1];
        var currDescriptionField = currDescriptionCell.firstChild;
        if (!isEmptyVar(currDescriptionField.value)) {
            addRowToInputParamaterTable();
        }
    }
}

//Function to add Row to Input Parameter Table
function addRowToOutputParamaterTable() {
    try {
        var tableReferece = document.getElementById('serviceDescription_OutputFieldsTable');
        var currRow, currCell, currCountOfRows, countOfAddedParameters, currTextField, currCheckBox;

        currCountOfRows = tableReferece.rows.length;
        countOfAddedParameters = currCountOfRows - 1; //Removing the Action Buttons Row from consideration

        currRow = tableReferece.insertRow(-1);
        currRow.id = 'outputParamRow_' + countOfAddedParameters;

        //Add Field Name Cell
        currCell = currRow.insertCell(-1);
        currTextField = document.createElement('input');;
        currTextField.type = 'text';
        currTextField.placeholder = 'Field Name';
        currCell.appendChild(currTextField);
        currCell.style.textAlign = 'center';
        currCell.style.width = '25%';

        //Add Field Description Cell
        currCell = currRow.insertCell(-1);
        currTextField = document.createElement('input');
        currTextField.type = 'text';
        currTextField.placeholder = 'Field Description';
        currTextField.onkeypress = outputParameterField_onKeyPress;
        currTextField.onpaste = outputParameterField_onPaste;
        currCell.appendChild(currTextField);
        currCell.style.textAlign = 'center';
        currCell.style.width = '45%';

        //Add is mandatory cell
        currCell = currRow.insertCell(-1);
        currCheckBox = document.createElement('input');
        currCheckBox.type = 'checkbox';
        currCheckBox.name = 'isMandatory_chk';
        currCell.appendChild(currCheckBox);
        currCell.style.textAlign = 'center';
        currCell.style.width = '5%';

        //Add Range Cell
        currCell = currRow.insertCell(-1);
        currTextField = document.createElement('input');
        currTextField.type = 'text';
        currTextField.placeholder = 'Range';
        currCell.appendChild(currTextField);
        currCell.style.textAlign = 'center';
        currCell.style.width = '20%';

        //Add Delete Row Cell
        currCell = currRow.insertCell(-1);
        currCell.style.width = '5%';
        if (countOfAddedParameters != 0) {
            var image = document.createElement('img');
            image.src = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADsQAAA7EAZUrDhsAAAAHdElNRQfjBBYVNRPglI/8AAAAzklEQVQoz3WRyw3CMBBE37qFcIAaAoQDHYRPDSBBCVQYoAGQothuAQ7EogAkzCHBJAH2ZOvNaGd3sWvjbGESOmUSW5jSrMSURICTWZx/sJ34jAjkpuQCQOQzO+li8FflN7i25INxbAVMQkYPgLtfyKP5G53ky8P7VaWSd+bgq+r+nI/PEAQdScCg+FkSjH9b+MXoFAQmYf8vpKrdUd17SUpZ7+WopyB6LIe2pzV0KlrLsInbexGtpN/FEOcyqw7gB4odjoK0eUuIc1IKHLsXSHprzV+w+rYAAAAldEVYdGRhdGU6Y3JlYXRlADIwMTktMDQtMjJUMTk6NTM6MTkrMDI6MDAdJ9RQAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE5LTA0LTIyVDE5OjUzOjE5KzAyOjAwbHps7AAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAAASUVORK5CYII=";
            image.className = 'deleteIcon';
            currCell.appendChild(image);
            currCell.style.textAlign = 'center';
            $(image).click(function () {
                $(this).parents('tr').first().remove();
            });
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to set the passed Field and Description values to the last row of the Input Parameter Table
function setEntryToInputParameterTable(field, description, isMandatory, range) {
    try {
        var tableReference = document.getElementById('serviceDescription_InputFieldsTable');
        var lastRow = tableReference.rows[tableReference.rows.length - 1];
        currFieldNameInput = lastRow.cells[0].firstChild; //Field name is at index 0
        currDescriptionInput = lastRow.cells[1].firstChild; //Field description is at index 1
        currIsMandatoryCheckbox = lastRow.cells[2].firstChild; //isMandatory Checkbox is at index 2
        currRangeInput = lastRow.cells[3].firstChild; //Range field is at index 3

        if (!isEmptyVar(field)) {
            currFieldNameInput.value = field;
        }
        if (!isEmptyVar(description)) {
            currDescriptionInput.value = description;
        }
        if (isMandatory) {
            currIsMandatoryCheckbox.checked = true;
        }
        if (!isEmptyVar(range)) {
            currRangeInput.value = description;
        }

    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to set the passed Field and Description values to the last row of the Output Parameter Table
function setEntryToOutputParameterTable(field, description, isMandatory, range) {
    try {
        var tableReference = document.getElementById('serviceDescription_OutputFieldsTable');
        var lastRow = tableReference.rows[tableReference.rows.length - 1];

        currFieldNameInput = lastRow.cells[0].firstChild; //Field name is at index 0
        currDescriptionInput = lastRow.cells[1].firstChild; //Field description is at index 1
        currIsMandatoryCheckbox = lastRow.cells[2].firstChild; //isMandatory Checkbox is at index 2
        currRangeInput = lastRow.cells[3].firstChild; //Range field is at index 3

        if (!isEmptyVar(field)) {
            currFieldNameInput.value = field;
        }
        if (!isEmptyVar(description)) {
            currDescriptionInput.value = description;
        }
        if (isMandatory) {
            currIsMandatoryCheckbox.checked = true;
        }
        if (!isEmptyVar(range)) {
            currRangeInput.value = description;
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to handle on-click event of Input Parameter Field
function inputParameterField_onClick() {
    var tableReferece = document.getElementById('serviceDescription_InputFieldsTable');
    var lastRow = tableReferece.rows[tableReferece.rows.length - 1];
    var currDescriptionCell = lastRow.cells[1];
    var currDescriptionField = currDescriptionCell.firstChild;
    if (!isEmptyVar(currDescriptionField.value)) {
        addRowToInputParamaterTable();
    }
}

//Function to add Row to Input Parameter Table
function addRowToInputParamaterTable() {
    try {
        var tableReferece = document.getElementById('serviceDescription_InputFieldsTable');
        var currRow, currCell, currCountOfRows, countOfAddedParameters, currTextField, currCheckBox;

        currCountOfRows = tableReferece.rows.length;
        countOfAddedParameters = currCountOfRows - 1; //Removing the Action Buttons Row from consideration

        currRow = tableReferece.insertRow(-1);
        currRow.id = 'inputParamRow_' + countOfAddedParameters;

        //Add Field Name Cell
        currCell = currRow.insertCell(-1);
        currTextField = document.createElement('input');;
        currTextField.type = 'text';
        currTextField.placeholder = 'Field Name';
        currCell.appendChild(currTextField);
        currCell.style.textAlign = 'center';
        currCell.style.width = '25%';

        //Add Field Description Cell
        currCell = currRow.insertCell(-1);
        currTextField = document.createElement('input');
        currTextField.type = 'text';
        currTextField.placeholder = 'Field Description';
        currTextField.onkeypress = inputParameterField_onClick;
        currTextField.onpaste = inputParameterField_onPaste;
        currCell.appendChild(currTextField);
        currCell.style.textAlign = 'center';
        currCell.style.width = '45%';

        //Add is mandatory cell
        currCell = currRow.insertCell(-1);
        currCheckBox = document.createElement('input');
        currCheckBox.type = 'checkbox';
        currCheckBox.name = 'isMandatory_chk';
        currCell.appendChild(currCheckBox);
        currCell.style.textAlign = 'center';
        currCell.style.width = '5%';

        //Add Range Cell
        currCell = currRow.insertCell(-1);
        currTextField = document.createElement('input');
        currTextField.type = 'text';
        currTextField.placeholder = 'Range';
        currCell.appendChild(currTextField);
        currCell.style.textAlign = 'center';
        currCell.style.width = '20%';

        //Add Delete Row Cell
        currCell = currRow.insertCell(-1);
        currCell.style.width = '5%';
        if (countOfAddedParameters != 0) {
            var image = document.createElement('img');
            image.src = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAQAAAC1+jfqAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAAAAmJLR0QAAKqNIzIAAAAJcEhZcwAADsQAAA7EAZUrDhsAAAAHdElNRQfjBBYVNRPglI/8AAAAzklEQVQoz3WRyw3CMBBE37qFcIAaAoQDHYRPDSBBCVQYoAGQothuAQ7EogAkzCHBJAH2ZOvNaGd3sWvjbGESOmUSW5jSrMSURICTWZx/sJ34jAjkpuQCQOQzO+li8FflN7i25INxbAVMQkYPgLtfyKP5G53ky8P7VaWSd+bgq+r+nI/PEAQdScCg+FkSjH9b+MXoFAQmYf8vpKrdUd17SUpZ7+WopyB6LIe2pzV0KlrLsInbexGtpN/FEOcyqw7gB4odjoK0eUuIc1IKHLsXSHprzV+w+rYAAAAldEVYdGRhdGU6Y3JlYXRlADIwMTktMDQtMjJUMTk6NTM6MTkrMDI6MDAdJ9RQAAAAJXRFWHRkYXRlOm1vZGlmeQAyMDE5LTA0LTIyVDE5OjUzOjE5KzAyOjAwbHps7AAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAAAASUVORK5CYII=";
            image.className = 'deleteIcon';
            currCell.appendChild(image);
            currCell.style.textAlign = 'center';
            $(image).click(function () {
                $(this).parents('tr').first().remove();
            });
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to get the get the Input Fields as JSON Object
function getInputFieldsDocument() {
    try {
        var tableReferece = document.getElementById('serviceDescription_InputFieldsTable');
        var tableRows = tableReferece.rows;
        var tableRowsCount = tableRows.length;
        var inputFieldsDocument = [];
        var currRow, currFieldName, currFieldDescription, currIsMandatory, currRange;

        for (var index = 0; index < tableRowsCount; index++) {
            currRow = tableRows[index];

            //Field Name
            currFieldName = currRow.cells[0].firstChild.value;

            //Field Description
            currFieldDescription = currRow.cells[1].firstChild.value;

            //Is Mandatory
            currIsMandatory = currRow.cells[2].firstChild.checked;

            //Range
            currRange = currRow.cells[3].firstChild.value;

            // Save Field Data
            if (!isEmptyVar(currFieldName) && !isEmptyVar(currFieldDescription)) {
                var currFieldDoc = {};
                currFieldDoc.name = trimString(currFieldName);
                currFieldDoc.description = trimString(currFieldDescription);
                currFieldDoc.isMandatory = currIsMandatory;
                currFieldDoc.range = currRange;
                inputFieldsDocument.push(currFieldDoc);
            }
        }
        return inputFieldsDocument;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to get the get the Input Fields as JSON Object
function getOutputFieldsDocument() {
    try {
        var tableReferece = document.getElementById('serviceDescription_OutputFieldsTable');
        var tableRows = tableReferece.rows;
        var tableRowsCount = tableRows.length;
        var inputFieldsDocument = [];
        var currRow, currFieldName, currFieldDescription, currIsMandatory, currRange;
        for (var index = 0; index < tableRowsCount; index++) {
            currRow = tableRows[index];

            //Field Name
            currFieldName = currRow.cells[0].firstChild.value;

            //Field Description
            currFieldDescription = currRow.cells[1].firstChild.value;

            //Is Mandatory
            currIsMandatory = currRow.cells[2].firstChild.checked;

            //Range
            currRange = currRow.cells[3].firstChild.value;

            // Save Field Data
            if (!isEmptyVar(currFieldName) && !isEmptyVar(currFieldDescription)) {
                var currFieldDoc = {};
                currFieldDoc.name = trimString(currFieldName);
                currFieldDoc.description = trimString(currFieldDescription);
                currFieldDoc.isMandatory = currIsMandatory;
                currFieldDoc.range = currRange;
                inputFieldsDocument.push(currFieldDoc);
            }
        }
        return inputFieldsDocument;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Funtion to add new fields to Request Header fields with the specified values
function addFieldsToRequestHeaderTable(headerKeyName, headerKeyValue) {
    try {
        var requestHeadersTable = document.getElementById('requestHeaders_table');
        var currRow, currCell, currCountOfRows, countOfAddedHeaders, currTextField;

        currCountOfRows = requestHeadersTable.rows.length;
        //Removing the Action Buttons Row from consideration
        countOfAddedHeaders = currCountOfRows - 1;

        currRow = requestHeadersTable.insertRow(-1);
        currRow.id = 'headerRow_' + countOfAddedHeaders;

        currCell = currRow.insertCell(-1);
        var chkbox = document.createElement('input');
        chkbox.type = 'checkbox';
        chkbox.id = 'header_chk_' + countOfAddedHeaders;
        chkbox.name = 'header_chk_' + countOfAddedHeaders;
        currCell.appendChild(chkbox);
        currCell.style.textAlign = 'center';
        currCell.style.width = '5%';

        currCell = currRow.insertCell(-1);
        currTextField = document.createElement('input');;
        currTextField.type = 'text';
        currTextField.id = 'header_key_' + countOfAddedHeaders;
        currTextField.placeholder = 'Header Key';
        currCell.appendChild(currTextField);
        currCell.style.textAlign = 'center';
        currCell.style.width = '48.5%';
        if (headerKeyName) {
            currTextField.value = headerKeyName;
        }

        currCell = currRow.insertCell(-1);
        currTextField = document.createElement('input');;
        currTextField.type = 'text';
        currTextField.id = 'header_value_' + countOfAddedHeaders;
        currTextField.placeholder = 'Header Value';
        currCell.appendChild(currTextField);
        currCell.style.textAlign = 'center';
        currCell.style.width = '48.5%';
        if (headerKeyValue) {
            currTextField.value = headerKeyValue;
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to handle click event of the 'Edit Test Case' button
function editTestCase_btnClick() {
    try {
        //Clear Error Notations
        clearErrorNotations();

        //Reset the Security Level
        setSecurityLevelToAuthenticatedAppUser();

        //Fetch Test-Object of selected Test
        var selectedTestCases = getSelectedTestCases();

        if (selectedTestCases == null || selectedTestCases == undefined || isNaN(selectedTestCases[0]) || selectedTestCases.length > 1) {
            var alertContent;
            if (selectedTestCases.length > 1) {
                alertContent = "<p>Select only one Test-case<br>";
            } else {
                alertContent = "<p>Select the Test-case to be modified<br>";
            }
            $.alert({
                title: 'Information',
                type: 'dark',
                boxWidth: '35%',
                useBootstrap: false,
                content: alertContent
            });
            return;
        }

        //Fetch reference of selected Test (currTestID - GLOBAL Variable)
        var previousTestID = currTestID; //Backup of previous value
        currTestID = selectedTestCases[0];
        for (var currRef in tests) {
            if (tests[currRef].id == currTestID) {
                currTest = tests[currRef];
                break;
            }
        }

        //Check if the Test-Case's identity service is supported
        var currentIdentityService = currTest.identityService;
        var isSupported = true;
        if (currTest.securityLevel == "AuthenticatedAppUser") {
            var isSupported = isSupportedIdentityService(currentIdentityService);
        }

        if (isSupported === true) {
            //Supported Identity Service
            setTestCaseTobeEditedToView(currTest);
        } else {
            //Unsupported Identity Service
            var selectedApplication = document.getElementById('application_select').value;
            var selectedIdentityService = document.getElementById('identityService_select').value;
            $.confirm({
                title: 'Confirmation',
                boxWidth: '90%',
                useBootstrap: false,
                content: '<p>The Identity Service of this Test-Case is not supported in the selected application: <b>\'' + selectedApplication + '\'</b><br><br>' +
                    'Click <i>\'Proceed\'</i> to change the Identity Service of the Test-Case to the currently selected Identity Service: <b>\'' + selectedIdentityService + '\'</b><br>' +
                    'Click <i>\'Cancel\'</i> to choose an application which supports the Identity Service: <b>\'' + currentIdentityService + '\'</b><br>',
                buttons: {
                    Proceed: function () {
                        //Set the currently selected Identity Service to the Test case
                        currTest.identityService = selectedIdentityService;
                        setTestCaseTobeEditedToView(currTest);
                    },
                    Cancel: function () {
                        //Cancel Test Edit
                        currTestID = previousTestID;
                        return;
                    }
                }
            });
        }

    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to determine if the Test-Case is supported based on current Identity Service drop-down values
function isSupportedIdentityService(identityServiceName) {
    var selectEle = document.getElementById('identityService_select');
    var optionsCount = selectEle.options.length;
    for (var index = 0; index < optionsCount; index++) {
        if (selectEle[index].text == identityServiceName) {
            return true;
        }
    }
    return false;
}

//Function to set the test-case to be edited to view
function setTestCaseTobeEditedToView(currTest) {
    try {
        //Set Edit-Test Mode to True
        editTestMode = true;

        //Scroll Page to testDefinitionDiv
        $('html,body').animate({
            scrollTop: $('#testDefinitionDiv').offset().top
        }, 'slow');

        //Clear View Elements 
        clearRequestHeadersTable();
        clearServiceResponse();
        clearResponseAssertionsFields();
        clearAssertionsListTable();
        clearTestCaseElements();
        clearAuthPayloadAndSetEmptyView();

        var selectedAppName = document.getElementById('application_select').value;

        //Set Security Level & Identity Service
        var currSecurityLevel = currTest.securityLevel;
        setSecurityLevelToView(currSecurityLevel);
        if (currSecurityLevel == "AuthenticatedAppUser") {
            var currentIdentityService = currTest.identityService;

            //Clear Identity Services from dropdown
            $('#identityService_select').empty();

            //Populate the Identity Services
            populateIdentityServicesList(selectedRuntimeData);

            //Enable customAuthPayload_chk checkbox 
            document.getElementById('customAuthPayload_chk').disabled = false;

            //Set the Identity Service Data
            var identityServiceSelectElement = document.getElementById('identityService_select');
            var optionsCount = identityServiceSelectElement.options.length;
            for (var index = 0; index < optionsCount; index++) {
                if (identityServiceSelectElement[index].text == currentIdentityService) {
                    identityServiceSelectElement.selectedIndex = index;
                    setIdentityServicePayload(selectedAppName, currentIdentityService);
                    break;
                }
            }

            //Set Custom Auth Payload if configured
            var isCustomAuthTest = currTest.isCustomAuthTest;
            document.getElementById('customAuthPayload_chk').checked = isCustomAuthTest;
            if (isCustomAuthTest == true) {
                var customAuthRequestBody = currTest.customAuthBody;
                var customAuthRequestHeaders = currTest.customAuthHeaders;

                document.getElementById('authPayloadBody_txtArea').disabled = false;
                document.getElementById('authPayloadHeaders_txtArea').disabled = false;
                document.getElementById('AuthRequestHeaders_a').style.color = '#000000';
                document.getElementById('AuthRequestBody_a').style.color = '#000000';

                document.getElementById('AuthRequestHeaders_a').innerHTML = 'Custom Auth Request Headers';
                document.getElementById('AuthRequestBody_a').innerHTML = 'Custom Auth Request Body';

                try {
                    if (!isEmptyObject(customAuthRequestBody)) {
                        document.getElementById('authPayloadBody_txtArea').value = JSON.stringify(JSON.parse(customAuthRequestBody), null, 4);
                    } else {
                        document.getElementById('authPayloadBody_txtArea').value = "";
                    }
                    if (!isEmptyObject(customAuthRequestHeaders)) {
                        document.getElementById('authPayloadHeaders_txtArea').value = JSON.stringify(customAuthRequestHeaders, null, 4);
                    } else {
                        document.getElementById('authPayloadHeaders_txtArea').value = "";
                    }

                } catch (err) {
                    document.getElementById('authPayloadBody_txtArea').value = customAuthRequestBody;
                    document.getElementById('authPayloadHeaders_txtArea').value = customAuthRequestHeaders;
                }
            }

        } else {
            /* Anonymous App User OR Public */

            //Clear Auth Payload
            clearAuthPayloadAndSetEmptyView();

            //Disable customAuthPayload_chk checkbox 
            document.getElementById('customAuthPayload_chk').disabled = true;

            //Clear Identity Services from dropdown
            $('#identityService_select').empty();
        }

        //Set Test Name          
        document.getElementById('testName_input').value = currTest.name;

        //Set Test Priority
        var testPrioritySelectEle = document.getElementById('testPriority_select');
        optionsCount = testPrioritySelectEle.options.length;
        for (var index = 0; index < optionsCount; index++) {
            if (testPrioritySelectEle[index].text == currTest.testPriority) {
                testPrioritySelectEle.selected = true;
                break;
            }
        }

        //Set Test Sequence
        document.getElementById('testSequence_txt').value = currTest.testSequence;

        //Set Request Body
        var requestBody = sanitisePayload(currTest.requestBody);
        try {
            requestBody = JSON.stringify(JSON.parse(requestBody), null, 4);
        } catch (err) {
            //Handled Exception. Request Body is not a JSON
        }
        document.getElementById('requestBody_txtArea').value = requestBody;

        //Set Request Headers
        for (var currRef in currTest.requestHeaders) {
            addFieldsToRequestHeaderTable(currRef, currTest.requestHeaders[currRef]);
        }

        // Add Assertions
        responseBodyAssertions = currTest.responseBodyAssertions;
        responseHeaderAssertions = currTest.responseHeaderAssertions;
        refreshAssertionsListTable();
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to set the passed security level to the securityLevel_select dropdown
function setSecurityLevelToView(securityLevel) {
    if (isEmptyVar(securityLevel)) {
        return;
    }
    var securityLevelSelect = document.getElementById("securityLevel_select");
    var optionsCount = securityLevelSelect.options.length;
    for (var index = 0; index < optionsCount; index++) {
        if (securityLevelSelect[index].value == securityLevel) {
            securityLevelSelect.selectedIndex = index;
            break;
        }
    }
}

//Function to handle click event of the 'Remove Header' button
function removeHeader_btnClick() {
    try {
        var targetRows = [];
        var currRow, currCell, currCheckBox;
        var requestHeadersTable = document.getElementById('requestHeaders_table');
        var requestHeadersTableRows = requestHeadersTable.rows;
        var requestHeadersTableRowsCount = requestHeadersTableRows.length;

        for (var index = 0; index < requestHeadersTableRowsCount; index++) {
            currRow = requestHeadersTableRows[index];
            currCell = currRow.cells[0];
            currCheckBox = currCell.firstChild;
            if (currCheckBox.checked == true) {
                targetRows.push(currRow.id);
            }
        }

        for (var index = 0; index < targetRows.length; index++) {
            deleteRow(targetRows[index]);
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}


//Function to store the request headers to the 'requestHeaders' object
function getRequestHeaders() {
    try {
        var currRow;
        var currKeyCell;
        var currValueCell;
        var currKey;
        var currValue;
        var requestHeadersTable = document.getElementById('requestHeaders_table');
        var requestHeadersTableRows = requestHeadersTable.rows;
        var requestHeadersTableRowsCount = requestHeadersTableRows.length;
        var requestHeaders = {};
        for (var index = 0; index < requestHeadersTableRowsCount; index++) {
            currRow = requestHeadersTableRows[index];
            currKeyCell = currRow.cells[1]; //Header Key is at index 1
            currValueCell = currRow.cells[2]; //Header Value is at index 2
            currKey = currKeyCell.firstChild.value;
            currValue = currValueCell.firstChild.value;
            requestHeaders[currKey] = currValue;
        }
        return requestHeaders;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Utility Function to delete a row based on Row ID
function deleteRow(rowid) {
    try {
        var row = document.getElementById(rowid);
        var table = row.parentNode;
        while (table && table.tagName != 'TABLE')
            table = table.parentNode;
        if (!table)
            return;
        table.deleteRow(row.rowIndex);
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Funtion to handle click event of common select check box for Assertions Table
function assertionsList_chk_OnClick(checkbox) {
    try {
        var currRow, currCell, currCheckBox;
        var assertionsTable = document.getElementById('testAssertionsList_table');
        var assertionTableRows = assertionsTable.rows;
        var assertionTableRowsCount = assertionTableRows.length;

        if (checkbox.checked == true) {
            for (var index = 1; index < assertionTableRowsCount - 1; index++) {
                currRow = assertionTableRows[index];
                currCell = currRow.cells[0];
                currCheckBox = currCell.firstChild;
                currCheckBox.checked = true;
            }
        } else {
            for (var index = 1; index < assertionTableRowsCount - 1; index++) {
                currRow = assertionTableRows[index];
                currCell = currRow.cells[0];
                currCheckBox = currCell.firstChild;
                currCheckBox.checked = false;
            }
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Funtion to handle click event of common select check box for TestCases Table
function testsList_chk_OnClick(checkbox) {
    try {
        var currRow, currCell, currCheckBox;
        var testCasesTable = document.getElementById('testCasesList_table');
        var testCasesTableRows = testCasesTable.rows;
        var testCasesTableRowsCount = testCasesTableRows.length;

        if (checkbox.checked == true) {
            for (var index = 1; index < testCasesTableRowsCount - 1; index++) {
                currRow = testCasesTableRows[index];
                currCell = currRow.cells[0];
                currCheckBox = currCell.firstChild;
                currCheckBox.checked = true;
            }
        } else {
            for (var index = 1; index < testCasesTableRowsCount - 1; index++) {
                currRow = testCasesTableRows[index];
                currCell = currRow.cells[0];
                currCheckBox = currCell.firstChild;
                currCheckBox.checked = false;
            }
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to fetch the Runtimes List
function fetchRuntimeListData() {
    try {
        $.ajax({
            type: 'GET',
            url: fetchRuntimeDataURL,
            success: function (response) {

                //Set to global varibale
                runtimesList = response;

                //Populate the runtimes list dropdown
                populateRuntimeSelect(response);

                //Populate the selected runtime data
                var selectedRuntime = document.getElementById('runtime_select').value;
                fetchRuntimeData(selectedRuntime);

            },
            error: function (response) {},
            complete: function (response) {}
        });
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to set the run-time list to form
function populateRuntimeSelect(runtimeList) {
    try {
        if (runtimeList == null || runtimeList == undefined) {
            return;
        }
        var currRuntime;
        for (var currRef in runtimeList) {
            currRuntime = runtimeList[currRef];
            $('#runtime_select').append('<option>' + currRuntime + '</option>');
        }

    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}


//Function to fetch the runtime data of the selected runtime
function fetchRuntimeData(runtimeName) {
    try {
        clearRuntimeData();
        var serviceURL = fetchRuntimeDataURL + "?instanceName=" + runtimeName;
        $.ajax({
            type: 'GET',
            url: serviceURL,
            success: function (response) {
                selectedRuntimeData = response;
                populateSelectedRuntimeData(selectedRuntimeData);
            },
            error: function (response) {},
            complete: function (response) {}
        });
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to handle on-change event of run-time select
function runtime_select_onchange(runtimeSelect) {
    var selectedRuntime = runtimeSelect.value;
    console.log("Selected Runtime:" + selectedRuntime);
    fetchRuntimeData(selectedRuntime);
}

function clearApplicationData() {
    //Clear Identity Services Data
    $('#application_select').empty();
}

function clearRuntimeData() {

    //Reset global variable
    selectedRuntimeData = "";

    //Clear Identity Services Data
    $('#identityService_select').empty();
    setDefaultAuthPayload();

    //Clear Application Data
    clearApplicationData();

}

//Function to handle on-change event of application name
function application_select_onchange(applicationSelect) {

    //Populate the Identity Services
    populateIdentityServicesList(selectedRuntimeData);

    //Set the Selected Identity Service Data
    var selectedAppName = applicationSelect.value;
    var selectedIdentityService = document.getElementById('identityService_select').value;
    setIdentityServicePayload(selectedAppName, selectedIdentityService);
}

//Function to populate the selected run-time data
function populateSelectedRuntimeData(runtimeData) {

    //Populate Runtime Apps List
    populateApplicationListData(runtimeData);

    //populate Runtime URL
    populateRuntimeURL(runtimeData);

    //Populate Identity Service Data
    populateIdentityServicesList(runtimeData);

}

//Function to populate the list of hosted apps
function populateApplicationListData(runtimeData) {
    try {
        if (runtimeData == null || runtimeData == undefined) {
            return;
        }
        var currApp;
        var applications = runtimeData.applications;
        for (var currRef in applications) {
            currApp = applications[currRef].name;
            $('#application_select').append('<option>' + currApp + '</option>');
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to set the environment data to view
function populateRuntimeURL(runtimeData) {
    try {
        if (runtimeData == null || runtimeData == undefined) {
            return;
        }
        var inputField = document.getElementById('serviceURL_prefix_input');
        var content = runtimeData.hostURL + '/';
        inputField.value = content;
        inputField.style.width = (inputField.value.length - 5) + 'ch';
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to fetch selected App Data
function fetchSelectedAppData() {
    var selectedAppData;
    var selectedAppName = document.getElementById('application_select').value;
    var applications = selectedRuntimeData.applications;
    for (var currRef in applications) {
        if (applications[currRef].name == selectedAppName) {
            selectedAppData = applications[currRef];
        }
    }
    return selectedAppData;
}
//Function to set Identity Service Data to View
function populateIdentityServicesList(runtimeData) {
    try {

        //Clear Existing Identity Services list
        $('#identityService_select').empty();

        //Fetch Selected App Data
        var selectedAppData = fetchSelectedAppData();

        //Set Identity Services of selected app
        var currIdentityService;
        var identityServicesData = selectedAppData.identityServices;
        for (var currRef in identityServicesData) {
            currIdentityService = identityServicesData[currRef];
            $('#identityService_select').append('<option>' + currIdentityService.providerName + '</option>');
        }
        setDefaultAuthPayload();
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to handle click event of 'Execute Request' button
function executeRequest_btnClick(executeRequestbutton) {
    try {
        clearErrorNotations();
        var errorMessage = "";
        var isValidContent = true;

        //Validate URL
        var baseURL = document.getElementById('serviceURL_prefix_input').value;
        var serviceURL = document.getElementById('serviceURL_input').value;
        serviceURL = processServiceURL(serviceURL);
        var requestURL = baseURL + "/" + serviceURL;
        if (isEmptyVar(serviceURL)) {
            isValidContent = false;
            errorMessage = errorMessage + "Service URL cannot be empty";
        } else if (!isValidURL(requestURL)) {
            isValidContent = false;
            errorMessage = errorMessage + "Invalid Service URL";
        }

        //Validate Custom Auth Headers
        var isCustomAuthRequest = document.getElementById('customAuthPayload_chk').checked;
        if (isCustomAuthRequest == true) {
            var customAuthHeaders = document.getElementById('authPayloadHeaders_txtArea').value;
            if (isEmptyVar(customAuthHeaders)) {
                customAuthHeaders = "{}";
            }
            try {
                if (!isEmptyVar(customAuthHeaders)) {
                    customAuthHeaders = JSON.parse(customAuthHeaders);
                } else {
                    customAuthHeaders = {};
                }

            } catch (err) {
                isValidContent = false;
                errorMessage = errorMessage + "&nbsp&nbsp'Custom Auth Headers' must be a valid JSON.";
                document.getElementById('authPayloadHeaders_txtArea').style.border = '1px solid red';
            }
        }

        if (isValidContent == false) {
            document.getElementById('serviceURL_input').style.border = '1px solid red';
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>" + errorMessage + "</p>"
            });
            $('html,body').animate({
                scrollTop: $('#serviceOverviewDiv').offset().top
            }, 'slow');
            console.log('Invalid Service URL');
            return;
        }

        clearServiceResponse();
        executeRequestAndGetResponse();
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to execute the configured HTTP request
function executeRequestAndGetResponse() {
    try {
        var baseURL = document.getElementById('serviceURL_prefix_input').value;
        var serviceURL = document.getElementById('serviceURL_input').value;
        var requestURL = baseURL + serviceURL;
        var requestHeaders = getRequestHeaders();
        var requestBody = document.getElementById('requestBody_txtArea').value;
        var requestMethod = document.getElementById('requestMethod_select').value;
        var securityLevel = document.getElementById('securityLevel_select').value;
        var isCustomAuthRequest = document.getElementById('customAuthPayload_chk').checked;
        var customAuthBody = document.getElementById('authPayloadBody_txtArea').value;
        var customAuthHeaders = document.getElementById('authPayloadHeaders_txtArea').value;
        var identityServiceName = document.getElementById('identityService_select').value;

        if (isEmptyVar(customAuthHeaders)) {
            customAuthHeaders = {};
        } else {
            customAuthHeaders = JSON.parse(customAuthHeaders);
        }
        requestBody = substitutePlaceHoldersInRequestBody(requestBody);
        console.log("RequestBody:" + requestBody);
        var serviceRequest = {};

        //Set Request Payload
        serviceRequest.url = requestURL;
        serviceRequest.requestBody = requestBody;
        serviceRequest.requestMethod = requestMethod;
        serviceRequest.requestHeaders = requestHeaders;

        //Set Identity Info
        var selectedAppData = fetchSelectedAppData();
        serviceRequest.appKey = selectedAppData.appKey;
        serviceRequest.appSecret = selectedAppData.appSecret;
        serviceRequest.customAuthBody = customAuthBody;
        serviceRequest.customAuthHeaders = customAuthHeaders;
        serviceRequest.isCustomAuthRequest = isCustomAuthRequest;
        serviceRequest.authServiceURL = selectedRuntimeData.authServiceURL;
        serviceRequest.securityLevel = securityLevel;

        var identityService = {};
        var selectedAppIdentityServices = selectedAppData.identityServices;
        if (selectedAppIdentityServices) {
            for (var index = 0; index < selectedAppIdentityServices.length; index++) {
                if (selectedAppIdentityServices[index].providerName == identityServiceName) {
                    identityService = selectedAppIdentityServices[index];
                    break;
                }
            }
        }
        serviceRequest.identityService = identityService;

        //Set DOM Elements
        var executeRequestbutton = document.getElementById('executeRequest_btn');
        executeRequestbutton.disabled = true;
        executeRequestbutton.innerHTML = 'Executing Request...';

        //Execute Request
        $.ajax({
            type: 'POST',
            url: executeRequestURL,
            headers: {
                'Content-Type': 'application/json'
            },
            data: JSON.stringify(serviceRequest),
            success: function (response) {
                setResponseToView(response);
            },
            error: function (response) {
                alert("Execute Request - Error Call back. Check console log for details.");
                console.error(response);
            },
            complete: function (response) {
                executeRequestbutton.disabled = false;
                executeRequestbutton.innerHTML = "Execute Request&nbsp; <img style='vertical-align: middle;' src='data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMS4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iQ2FwYV8xIiB4PSIwcHgiIHk9IjBweCIgdmlld0JveD0iMCAwIDQ3OC43MDMgNDc4LjcwMyIgc3R5bGU9ImVuYWJsZS1iYWNrZ3JvdW5kOm5ldyAwIDAgNDc4LjcwMyA0NzguNzAzOyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSIgd2lkdGg9IjE2cHgiIGhlaWdodD0iMTZweCI+CjxnPgoJPGc+CgkJPHBhdGggZD0iTTQ1NC4yLDE4OS4xMDFsLTMzLjYtNS43Yy0zLjUtMTEuMy04LTIyLjItMTMuNS0zMi42bDE5LjgtMjcuN2M4LjQtMTEuOCw3LjEtMjcuOS0zLjItMzguMWwtMjkuOC0yOS44ICAgIGMtNS42LTUuNi0xMy04LjctMjAuOS04LjdjLTYuMiwwLTEyLjEsMS45LTE3LjEsNS41bC0yNy44LDE5LjhjLTEwLjgtNS43LTIyLjEtMTAuNC0zMy44LTEzLjlsLTUuNi0zMy4yICAgIGMtMi40LTE0LjMtMTQuNy0yNC43LTI5LjItMjQuN2gtNDIuMWMtMTQuNSwwLTI2LjgsMTAuNC0yOS4yLDI0LjdsLTUuOCwzNGMtMTEuMiwzLjUtMjIuMSw4LjEtMzIuNSwxMy43bC0yNy41LTE5LjggICAgYy01LTMuNi0xMS01LjUtMTcuMi01LjVjLTcuOSwwLTE1LjQsMy4xLTIwLjksOC43bC0yOS45LDI5LjhjLTEwLjIsMTAuMi0xMS42LDI2LjMtMy4yLDM4LjFsMjAsMjguMSAgICBjLTUuNSwxMC41LTkuOSwyMS40LTEzLjMsMzIuN2wtMzMuMiw1LjZjLTE0LjMsMi40LTI0LjcsMTQuNy0yNC43LDI5LjJ2NDIuMWMwLDE0LjUsMTAuNCwyNi44LDI0LjcsMjkuMmwzNCw1LjggICAgYzMuNSwxMS4yLDguMSwyMi4xLDEzLjcsMzIuNWwtMTkuNywyNy40Yy04LjQsMTEuOC03LjEsMjcuOSwzLjIsMzguMWwyOS44LDI5LjhjNS42LDUuNiwxMyw4LjcsMjAuOSw4LjdjNi4yLDAsMTIuMS0xLjksMTcuMS01LjUgICAgbDI4LjEtMjBjMTAuMSw1LjMsMjAuNyw5LjYsMzEuNiwxM2w1LjYsMzMuNmMyLjQsMTQuMywxNC43LDI0LjcsMjkuMiwyNC43aDQyLjJjMTQuNSwwLDI2LjgtMTAuNCwyOS4yLTI0LjdsNS43LTMzLjYgICAgYzExLjMtMy41LDIyLjItOCwzMi42LTEzLjVsMjcuNywxOS44YzUsMy42LDExLDUuNSwxNy4yLDUuNWwwLDBjNy45LDAsMTUuMy0zLjEsMjAuOS04LjdsMjkuOC0yOS44YzEwLjItMTAuMiwxMS42LTI2LjMsMy4yLTM4LjEgICAgbC0xOS44LTI3LjhjNS41LTEwLjUsMTAuMS0yMS40LDEzLjUtMzIuNmwzMy42LTUuNmMxNC4zLTIuNCwyNC43LTE0LjcsMjQuNy0yOS4ydi00Mi4xICAgIEM0NzguOSwyMDMuODAxLDQ2OC41LDE5MS41MDEsNDU0LjIsMTg5LjEwMXogTTQ1MS45LDI2MC40MDFjMCwxLjMtMC45LDIuNC0yLjIsMi42bC00Miw3Yy01LjMsMC45LTkuNSw0LjgtMTAuOCw5LjkgICAgYy0zLjgsMTQuNy05LjYsMjguOC0xNy40LDQxLjljLTIuNyw0LjYtMi41LDEwLjMsMC42LDE0LjdsMjQuNywzNC44YzAuNywxLDAuNiwyLjUtMC4zLDMuNGwtMjkuOCwyOS44Yy0wLjcsMC43LTEuNCwwLjgtMS45LDAuOCAgICBjLTAuNiwwLTEuMS0wLjItMS41LTAuNWwtMzQuNy0yNC43Yy00LjMtMy4xLTEwLjEtMy4zLTE0LjctMC42Yy0xMy4xLDcuOC0yNy4yLDEzLjYtNDEuOSwxNy40Yy01LjIsMS4zLTkuMSw1LjYtOS45LDEwLjhsLTcuMSw0MiAgICBjLTAuMiwxLjMtMS4zLDIuMi0yLjYsMi4yaC00Mi4xYy0xLjMsMC0yLjQtMC45LTIuNi0yLjJsLTctNDJjLTAuOS01LjMtNC44LTkuNS05LjktMTAuOGMtMTQuMy0zLjctMjguMS05LjQtNDEtMTYuOCAgICBjLTIuMS0xLjItNC41LTEuOC02LjgtMS44Yy0yLjcsMC01LjUsMC44LTcuOCwyLjVsLTM1LDI0LjljLTAuNSwwLjMtMSwwLjUtMS41LDAuNWMtMC40LDAtMS4yLTAuMS0xLjktMC44bC0yOS44LTI5LjggICAgYy0wLjktMC45LTEtMi4zLTAuMy0zLjRsMjQuNi0zNC41YzMuMS00LjQsMy4zLTEwLjIsMC42LTE0LjhjLTcuOC0xMy0xMy44LTI3LjEtMTcuNi00MS44Yy0xLjQtNS4xLTUuNi05LTEwLjgtOS45bC00Mi4zLTcuMiAgICBjLTEuMy0wLjItMi4yLTEuMy0yLjItMi42di00Mi4xYzAtMS4zLDAuOS0yLjQsMi4yLTIuNmw0MS43LTdjNS4zLTAuOSw5LjYtNC44LDEwLjktMTBjMy43LTE0LjcsOS40LTI4LjksMTcuMS00MiAgICBjMi43LTQuNiwyLjQtMTAuMy0wLjctMTQuNmwtMjQuOS0zNWMtMC43LTEtMC42LTIuNSwwLjMtMy40bDI5LjgtMjkuOGMwLjctMC43LDEuNC0wLjgsMS45LTAuOGMwLjYsMCwxLjEsMC4yLDEuNSwwLjVsMzQuNSwyNC42ICAgIGM0LjQsMy4xLDEwLjIsMy4zLDE0LjgsMC42YzEzLTcuOCwyNy4xLTEzLjgsNDEuOC0xNy42YzUuMS0xLjQsOS01LjYsOS45LTEwLjhsNy4yLTQyLjNjMC4yLTEuMywxLjMtMi4yLDIuNi0yLjJoNDIuMSAgICBjMS4zLDAsMi40LDAuOSwyLjYsMi4ybDcsNDEuN2MwLjksNS4zLDQuOCw5LjYsMTAsMTAuOWMxNS4xLDMuOCwyOS41LDkuNyw0Mi45LDE3LjZjNC42LDIuNywxMC4zLDIuNSwxNC43LTAuNmwzNC41LTI0LjggICAgYzAuNS0wLjMsMS0wLjUsMS41LTAuNWMwLjQsMCwxLjIsMC4xLDEuOSwwLjhsMjkuOCwyOS44YzAuOSwwLjksMSwyLjMsMC4zLDMuNGwtMjQuNywzNC43Yy0zLjEsNC4zLTMuMywxMC4xLTAuNiwxNC43ICAgIGM3LjgsMTMuMSwxMy42LDI3LjIsMTcuNCw0MS45YzEuMyw1LjIsNS42LDkuMSwxMC44LDkuOWw0Miw3LjFjMS4zLDAuMiwyLjIsMS4zLDIuMiwyLjZ2NDIuMUg0NTEuOXoiIGZpbGw9IiNGRkZGRkYiLz4KCQk8cGF0aCBkPSJNMjM5LjQsMTM2LjAwMWMtNTcsMC0xMDMuMyw0Ni4zLTEwMy4zLDEwMy4zczQ2LjMsMTAzLjMsMTAzLjMsMTAzLjNzMTAzLjMtNDYuMywxMDMuMy0xMDMuM1MyOTYuNCwxMzYuMDAxLDIzOS40LDEzNi4wMDEgICAgeiBNMjM5LjQsMzE1LjYwMWMtNDIuMSwwLTc2LjMtMzQuMi03Ni4zLTc2LjNzMzQuMi03Ni4zLDc2LjMtNzYuM3M3Ni4zLDM0LjIsNzYuMyw3Ni4zUzI4MS41LDMxNS42MDEsMjM5LjQsMzE1LjYwMXoiIGZpbGw9IiNGRkZGRkYiLz4KCTwvZz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8Zz4KPC9nPgo8L3N2Zz4K' />"
            }
        });
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to construct the Test Article 
function getTestArticle() {

    var appData = fetchSelectedAppData();
    var identityServices = appData.identityServices;

    var appKey = appData.appKey;
    var appSecret = appData.appSecret;
    var hostURL = selectedRuntimeData.hostURL;
    var authServiceURL = selectedRuntimeData.authServiceURL;

    var appName = appData.name;
    var appVersion = "0.1-Dev";

    var services = [];
    var serviceJSON = getServiceAsJSON();
    services.push(serviceJSON);

    var testArticle = {};
    testArticle.hostURL = hostURL;
    testArticle.authServiceURL = authServiceURL;

    testArticle.appKey = appKey;
    testArticle.appSecret = appSecret;

    testArticle.appName = appName;
    testArticle.appVersion = appVersion;

    testArticle.services = services;
    testArticle.identityServices = identityServices;

    return testArticle;
}

//Function to handle on-click event of Run Test Plan button
function executeTestPlan_btnClick() {
    try {
        if (tests && tests.length > 0) {
            //Execute Test Article
            var testArticle = getTestArticle();

            //Set DOM Elements
            var executeTestPlanButton = document.getElementById('executeTestPlan_btn');
            executeTestPlanButton.disabled = true;
            var buttonContentBackup = executeTestPlanButton.innerHTML;
            executeTestPlanButton.innerHTML = 'Executing Test Plan...';

            //Execute Request
            $.ajax({
                type: 'POST',
                url: executeTestPlanURL,
                headers: {
                    'Content-Type': 'application/json'
                },
                data: JSON.stringify(testArticle),
                success: function (response) {
                    var messageWindow = window.open("", "_blank");
                    if (!messageWindow || messageWindow.closed || typeof messageWindow.closed == 'undefined') {
                        //Popup blocked
                        $.alert({
                            title: 'Error',
                            type: 'red',
                            boxWidth: '35%',
                            useBootstrap: false,
                            content: "<p>Pop-up Blocker is enabled! Please add this site to your exception list.</p>"
                        });
                    } else {
                        messageWindow.document.write(response);
                    }
                },
                error: function (response) {
                    alert("Execute Request - Error Call back. Check console log for details.");
                    console.error(response);
                },
                complete: function (response) {
                    executeTestPlanButton.disabled = false;
                    executeTestPlanButton.innerHTML = buttonContentBackup;
                }
            });
        } else {
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>No Test Cases defined</p>"
            });
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to set response of the tested request to view
function setResponseToView(requestResponse) {
    try {
        var httpResponse = requestResponse.httpResponse;
        if (requestResponse.authRequestSuccessful == true) {
            var responseHeaders = httpResponse.responseHeaders;
            var responseBody = httpResponse.responseBody;
            var responseCode = httpResponse.responseCode;
            var responseTime = httpResponse.responseTime;

            try {
                //Indent response body as JSON. If error occurs, display without any further parsing
                document.getElementById('responseBody_txtArea').value = JSON.stringify(JSON.parse(responseBody), null, 4);
            } catch (err) {
                document.getElementById('responseBody_txtArea').value = responseBody;
            }
            document.getElementById('responseHeaders_txtArea').value = JSON.stringify(responseHeaders, null, 4);

            if (responseCode == 200) {
                var index = 0,
                    size = 0;
                var bodyAssertionPathsTextAreaContents = '';
                var bodyAssertionPaths = requestResponse.bodyAssertionPaths;
                size = bodyAssertionPaths.length;
                for (var currRef in bodyAssertionPaths) {
                    index++;
                    bodyAssertionPathsTextAreaContents = bodyAssertionPathsTextAreaContents + bodyAssertionPaths[currRef];
                    if (index !== size) {
                        bodyAssertionPathsTextAreaContents = bodyAssertionPathsTextAreaContents + '\n';
                    }
                }
                document.getElementById('responseBodyPaths_txtArea').value = bodyAssertionPathsTextAreaContents;

                index = 0;
                var headerAssertionPathsTextAreaContents = '';
                var headerAssertionPaths = requestResponse.headerAssertionPaths;
                size = headerAssertionPaths.length;
                for (var currRef in headerAssertionPaths) {
                    index++;
                    headerAssertionPathsTextAreaContents = headerAssertionPathsTextAreaContents + headerAssertionPaths[currRef];
                    if (index !== size) {
                        headerAssertionPathsTextAreaContents = headerAssertionPathsTextAreaContents + '\n';
                    }
                }
                document.getElementById('responseHeadersPaths_txtArea').value = headerAssertionPathsTextAreaContents;
            }
            var contentLength = (JSON.stringify(responseBody).length / 1000);
            document.getElementById('status_code_p').innerHTML = 'Status: ' + responseCode;
            document.getElementById('responseTime_p').innerHTML = 'Response Time (Excluding Auth): ' + responseTime + ' ms';
            if (responseCode == 200) {
                document.getElementById('status_code_p').style.color = 'green';
            } else {
                document.getElementById('status_code_p').style.color = 'red';
            }
            document.getElementById('size_p').innerHTML = 'Size: ' + contentLength + ' KB';
        } else {
            if (document.getElementById('customAuthPayload_chk').checked == true) {
                content = "<p>Failed to fetch Authentication Token. Verify Custom Auth Payload.</p>";
            } else {
                content = "<p>Failed to fetch Authentication Token. Verify Identity Service Configuration at Server.</p>";
            }

            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: content
            });
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to capitalise first letter of a String
function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

//Function to generate a random number
function getRandomNumber() {
    try {
        return Math.floor((Math.random() * 100000000) + 1);
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

// Open a dialog box
function openFileDialog(accept, multy = false, callback) {
    try {
        var inputElement = document.createElement('input');
        inputElement.type = 'file';
        inputElement.accept = accept; // Note Edge does not support this attribute
        if (multy) {
            inputElement.multiple = multy;
        }
        if (typeof callback === 'function') {
            inputElement.addEventListener('change', callback);
        }
        inputElement.dispatchEvent(new MouseEvent('click'));
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

// Function to handle click event of 'Import Service' Button
function import_service_btnClick() {
    try {
        clearErrorNotations();
        // Open File dialog for JSON Files
        openFileDialog('.json,application/json', false, handleFileDialogChange);
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

// Handler for on-change event for File Dialog
function handleFileDialogChange(event) {
    try {
        [...this.files].forEach(file => {

            var reader = new FileReader();
            reader.readAsText(file, 'UTF-8');
            reader.onload = function (evt) {
                var fileContents = evt.target.result;
                console.log('Successful in reading file contents.');
                setServiceArticleToView(fileContents);
            }
            reader.onerror = function (evt) {
                div.textContent = 'error reading file';
                console.log('Error in reading file contents.');
            }

        });
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to set the imported Service Article to View
function setServiceArticleToView(serviceArticleContents) {
    try {
        var serviceArticle = {};
        try {
            //Parse Service Article contents
            serviceArticle = JSON.parse(serviceArticleContents);
        } catch (err) {
            console.log('Malformed service Article');
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Invalid Test Plan</p>"
            });
            return;
        }

        if (validateImportedServiceContents(serviceArticle) == true) {
            console.log('Valid service Article');

            //Reset Form
            resetForm();

            //Initialise global variables
            serviceJSON = serviceArticle;
            tests = serviceArticle.tests;

            //Set Module Name
            if (serviceArticle.moduleName) { //Remove Condition post editing test plans
                document.getElementById('serviceModuleName_input').value = serviceArticle.moduleName;
            } else {
                document.getElementById('serviceModuleName_input').value = "";
            }

            //Set Service Name
            document.getElementById('serviceName_input').value = serviceArticle.name;

            //Set Service URL
            document.getElementById('serviceURL_input').value = serviceArticle.url;

            //Set Request Method
            var selectEle = document.getElementById('requestMethod_select');
            var optionsCount = selectEle.options.length;
            for (var index = 0; index < optionsCount; index++) {
                if (selectEle[index].text == serviceArticle.httpMethod) {
                    selectEle.selected = true;
                    break;
                }
            }

            //Set Service Description
            var description = serviceArticle.description;
            if (description) {
                document.getElementById('serviceDescription_textArea').value = description;
            }

            //Set Input Fields Description
            var inputFieldsDocument = serviceArticle.inputFieldsDocument;
            if (!isEmptyObject(inputFieldsDocument)) {
                for (var index = 0; index < inputFieldsDocument.length; index++) {
                    setEntryToInputParameterTable(inputFieldsDocument[index].name,
                        inputFieldsDocument[index].description,
                        inputFieldsDocument[index].isMandatory,
                        inputFieldsDocument[index].range);
                    addRowToInputParamaterTable();
                }
            }

            //Set Output Fields Description
            var outputFieldsDocument = serviceArticle.outputFieldsDocument;
            if (!isEmptyObject(outputFieldsDocument)) {
                for (var index = 0; index < outputFieldsDocument.length; index++) {
                    setEntryToOutputParameterTable(outputFieldsDocument[index].name,
                        outputFieldsDocument[index].description,
                        outputFieldsDocument[index].isMandatory,
                        outputFieldsDocument[index].range);
                    addRowToOutputParamaterTable();
                }
            }

            //Render Test Cases List Table
            refreshTestCasesListTable();

            //Acknowledge with an alert
            $.alert({
                title: 'Information',
                type: 'dark',
                boxWidth: '30%',
                useBootstrap: false,
                content: "<p>Test Plan Imported</p>",
            });

        } else {
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Invalid Test Plan</p>"
            });
            console.log('Invalid Test Plan');
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}


//Function to reset Form elements to default values
function resetForm() {
    try {
        editTestMode = false;

        document.getElementById('serviceName_input').value = '';
        document.getElementById('serviceURL_input').value = '';
        document.getElementById('testName_input').value = '';
        document.getElementById('testPriority_select').selectedIndex = 0;
        document.getElementById('identityService_select').selectedIndex = 0;
        document.getElementById('serviceDescription_textArea').value = '';
        document.getElementById('testSequence_txt').value = '';
        document.getElementById('requestBody_txtArea').value = '';

        clearServiceDocument();
        clearAssertionsListTable();
        clearErrorNotations();
        clearRequestHeadersTable();
        clearResponseAssertionsFields();
        clearServiceResponse();
        clearTestCaseElements();
        clearTestCasesListTable();

        addFieldsToRequestHeaderTable('Content-Type', 'application/json');
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to clear the Service Document
function clearServiceDocument() {
    try {
        document.getElementById('serviceDescription_textArea').value = '';

        var inputTableReference = document.getElementById('serviceDescription_InputFieldsTable');
        var ouputTableReference = document.getElementById('serviceDescription_OutputFieldsTable');

        //Clear Values at Row Index 1
        inputTableReference.rows[1].cells[0].firstChild.value = '';
        inputTableReference.rows[1].cells[1].firstChild.value = '';
        inputTableReference.rows[1].cells[2].firstChild.checked = false;
        inputTableReference.rows[1].cells[3].firstChild.value = '';

        ouputTableReference.rows[1].cells[0].firstChild.value = '';
        ouputTableReference.rows[1].cells[1].firstChild.value = '';
        ouputTableReference.rows[1].cells[2].firstChild.checked = false;
        ouputTableReference.rows[1].cells[3].firstChild.value = '';

        //Delete Rows from Index 2
        var currCountOfRows = inputTableReference.rows.length;
        for (var index = 2; index < currCountOfRows; index++) {
            inputTableReference.deleteRow(-1);
        }
        currCountOfRows = ouputTableReference.rows.length;
        for (var index = 2; index < currCountOfRows; index++) {
            ouputTableReference.deleteRow(-1);
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to clear error notations
function clearErrorNotations() {
    document.getElementById('testName_input').style.border = '1px solid #ccc';
    document.getElementById('testSequence_txt').style.border = '1px solid #ccc';
    document.getElementById('requestBody_txtArea').style.border = '1px solid #ccc';
    document.getElementById('serviceModuleName_input').style.border = '1px solid #ccc';
    document.getElementById('serviceName_input').style.border = '1px solid #ccc';
    document.getElementById('serviceURL_input').style.border = '1px solid #ccc';
    document.getElementById('responsePathExpression_txt').style.border = '1px solid #ccc';
    document.getElementById('responseExpectedValue_input').style.border = '1px solid #ccc';
    document.getElementById('variableName_txt').style.border = '1px solid #ccc';
}

//Function to clear remove service response from the view
function clearServiceResponse() {
    try {
        document.getElementById('responseBody_txtArea').value = '';
        document.getElementById('responseBodyPaths_txtArea').value = '';
        document.getElementById('responseHeaders_txtArea').value = '';
        document.getElementById('responseHeadersPaths_txtArea').value = '';
        document.getElementById('responseTime_p').innerHTML = '';
        document.getElementById('status_code_p').innerHTML = '';
        document.getElementById('status_code_p').style.color = 'black';
        document.getElementById('size_p').innerHTML = '';
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to validate the Imported Service JSON
function validateImportedServiceContents(serviceArticle) {
    try {
        if (serviceArticle == null || serviceArticle == undefined || typeof serviceArticle !== 'object') {
            console.error("Invalid Service Article");
            return false;
        }
        if (isEmptyVar(serviceArticle.moduleName)) {
            console.error("Invalid Module Name");
            return false;
        }
        if (isEmptyVar(serviceArticle.name)) {
            console.error("Invalid Service Name");
            return false;
        }
        if (isEmptyVar(serviceArticle.url)) {
            console.error("Invalid Service URL");
            return false;
        }
        if (isEmptyVar(serviceArticle.httpMethod) || !isValidHTTPMethod(serviceArticle.httpMethod)) {
            console.error("Invalid HTTP Method");
            return false;
        }
        if (isEmptyVar(serviceArticle.url)) {
            console.error("Invalid Service URL");
            return false;
        }
        if (serviceArticle.tests == null || serviceArticle.tests == undefined) {
            console.error("Invalid Service tests");
            return false;
        }

        var currTest;
        for (var currRef in serviceArticle.tests) {
            currTest = serviceArticle.tests[currRef];
            if (isEmptyVar(currTest.id)) {
                console.error("Invalid Test ID");
                return false;
            }
            if (isEmptyVar(currTest.name)) {
                console.error("Invalid Test Name");
                return false;
            }
            if (isEmptyVar(currTest.securityLevel) ||
                (currTest.securityLevel != "AuthenticatedAppUser" &&
                    currTest.securityLevel != "AnonymousAppUser" &&
                    currTest.securityLevel != "Public")) {
                console.error("Invalid Security Level");
                return false;
            }
            if (isEmptyVar(currTest.identityService) && currTest.securityLevel == "AuthenticatedAppUser") {
                console.error("Invalid Identity Service");
                return false;
            }
            if (isEmptyVar(currTest.testPriority)) {
                console.error("Invalid Test priority");
                return false;
            }
            if (isEmptyVar(currTest.testSequence)) {
                console.error("Invalid Test Sequence");
                return false;
            }
            if (currTest.requestHeaders == null || currTest.requestHeaders == undefined) {
                console.error("Invalid Request Headers");
                return false;
            }
            if (currTest.responseBodyAssertions == null || currTest.responseBodyAssertions == undefined) {
                console.error("Invalid Response Body assertions");
                return false;
            }
            var currAssertion;
            for (var currRef in currTest.responseBodyAssertions) {
                currAssertion = currTest.responseBodyAssertions[currRef];
                if (isEmptyVar(currAssertion.id)) {
                    console.error("Invalid Assertion");
                    return false;
                }
                if (isEmptyVar(currAssertion.path)) {
                    console.error("Invalid Assertion Path");
                    return false;
                }
                if (isEmptyVar(currAssertion.dataType)) {
                    console.error("Invalid Assertion data type");
                    return false;
                }
                if (isEmptyVar(currAssertion.value) && !currAssertion.isValueAgnostic) {
                    console.error("Invalid Assertion Value");
                    return false;
                }
                if (typeof currAssertion.isNullable != 'boolean') {
                    console.error("Invalid isNullable Value");
                    return false;
                }
            }

            for (var currRef in currTest.responseHeaderAssertions) {
                currAssertion = currTest.responseHeaderAssertions[currRef];
                if (isEmptyVar(currAssertion.id)) {
                    console.error("Invalid Assertion ID");
                    return false;
                }
                if (isEmptyVar(currAssertion.path)) {
                    console.error("Invalid Assertion Path");
                    return false;
                }
                if (isEmptyVar(currAssertion.dataType)) {
                    console.error("Invalid Data Type");
                    return false;
                }
                if (isEmptyVar(currAssertion.value)) {
                    console.error("Invalid Value");
                    return false;
                }
                if (typeof currAssertion.isNullable != 'boolean') {
                    console.error("Invalid isNullable Value");
                    return false;
                }
            }
        }
        return true;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to check if the variable does not have any content
function isEmptyVar(testVar) {
    try {
        if (testVar == null || testVar == undefined) {
            return true;
        }
        if (typeof testVar == 'string') {
            if (testVar.trim() == '') {
                return true;
            } else {
                return false;
            }
        }
    } catch (err) {
        console.error(err);
        console.log("Recieved Value:" + testVar);
        alert("Unexpected Error. Check console log for details.");
        return false;
    }
}

//Function to handle on-click event of Show Variable Anchor
function showVariableAnchor_click() {
    var content = "",
        currVariableName = "",
        currPath;
    try {
        var isNotHavingVariables = true;
        for (var currRef in tests) {

            isNotHavingVariables = true;
            content = content +
                "<b>Test:&nbsp;" + tests[currRef].name + "</b><br><br>" +
                "&nbsp;&nbsp;Response Body Variables List:" + "<br><br>";
            if (!isEmptyObject(tests[currRef].responseBodyAssertions)) {
                for (var currAssertion in tests[currRef].responseBodyAssertions) {
                    currVariableName = tests[currRef].responseBodyAssertions[currAssertion].variableName;
                    if (!isEmptyVar(currVariableName)) {
                        isNotHavingVariables = false;
                        currPath = tests[currRef].responseBodyAssertions[currAssertion].path;
                        content = content +
                            "&nbsp;&nbsp;&nbsp;&nbsp;Variable Name:&nbsp;" + currVariableName + "<br>" +
                            "&nbsp;&nbsp;&nbsp;&nbsp;Variable Value:&nbsp;" + variableCollection[currVariableName] + "<br>" +
                            "&nbsp;&nbsp;&nbsp;&nbsp;Variable Path:&nbsp;" + currPath + "<br>" + "<br>";
                    }
                }
            }
            if (isNotHavingVariables === true) {
                content = content + "&nbsp;&nbsp;&nbsp;&nbsp;None" + "<br>" + "<br>";
            }

            isNotHavingVariables = true;
            content = content +
                "&nbsp;&nbsp;Response Header Variables List:" + "<br><br>";
            if (!isEmptyObject(tests[currRef].responseHeaderAssertions)) {
                for (var currAssertion in tests[currRef].responseHeaderAssertions) {
                    currVariableName = tests[currRef].responseHeaderAssertions[currAssertion].variableName;
                    if (!isEmptyVar(currVariableName)) {
                        isNotHavingVariables = false;
                        currPath = tests[currRef].responseHeaderAssertions[currAssertion].path;
                        content = content +
                            "&nbsp;&nbsp;&nbsp;&nbsp;Variable Name:&nbsp;" + currVariableName + "<br>" +
                            "&nbsp;&nbsp;&nbsp;&nbsp;Variable Value:&nbsp;" + variableCollection[currVariableName] + "<br>" +
                            "&nbsp;&nbsp;&nbsp;&nbsp;Variable Path:&nbsp;" + currPath + "<br>" + "<br>";
                    }
                }
            }
            if (isNotHavingVariables === true) {
                content = content + "&nbsp;&nbsp;&nbsp;&nbsp;None" + "<br>" + "<br>";
            }
        }

        if (!isEmptyObject(responseBodyAssertions) || !isEmptyObject(responseHeaderAssertions)) {
            var currTestName = '<< Current Test >>';
            isNotHavingVariables = true;
            content = content +
                "<b>Test:&nbsp;" + currTestName + "</b><br><br>" +
                "&nbsp;&nbsp;Response Body Variables List:" + "<br><br>";
            for (var currAssertion in responseBodyAssertions) {
                currVariableName = responseBodyAssertions[currAssertion].variableName;
                if (!isEmptyVar(currVariableName)) {
                    isNotHavingVariables = false;
                    currPath = responseBodyAssertions[currAssertion].path;
                    content = content +
                        "&nbsp;&nbsp;&nbsp;&nbsp;Variable Name:&nbsp;" + currVariableName + "<br>" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;Variable Value:&nbsp;" + variableCollection[currVariableName] + "<br>" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;Variable Path:&nbsp;" + currPath + "<br>" + "<br>";
                }
            }
            if (isNotHavingVariables === true) {
                content = content + "&nbsp;&nbsp;&nbsp;&nbsp;None" + "<br>" + "<br>";
            }

            isNotHavingVariables = true;
            content = content +
                "&nbsp;&nbsp;Response Header Variables List:" + "<br><br>";
            for (var currAssertion in responseHeaderAssertions) {
                currVariableName = responseHeaderAssertions[currAssertion].variableName;
                if (!isEmptyVar(currVariableName)) {
                    isNotHavingVariables = false;
                    currPath = responseHeaderAssertions[currAssertion].path;
                    content = content +
                        "&nbsp;&nbsp;&nbsp;&nbsp;Variable Name:&nbsp;" + currVariableName + "<br>" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;Variable Value:&nbsp;" + variableCollection[currVariableName] + "<br>" +
                        "&nbsp;&nbsp;&nbsp;&nbsp;Variable Path:&nbsp;" + currPath + "<br>" + "<br>";
                }
                if (isNotHavingVariables === true) {
                    content = content + "&nbsp;&nbsp;&nbsp;&nbsp;None" + "<br>" + "<br>";
                }
            }
            if (isEmptyVar(content)) {
                content = "No Variables Defined";
            }
        }
        //Avoiding JQuery Alert for better performance 
        if (!isEmptyVar(content)) {
            content = content.replace(/&nbsp;/g, " ");
            content = content.replace(/<br>/g, "\n");
            content = content.replace(/<b>/g, "-- ");
            content = content.replace(/<\/b>/g, " --");
            alert(content);
        }
        // $.alert({
        //     title: 'Information',
        //     typeAnimated: true,
        //     type: 'dark',
        //     boxWidth: '50%',
        //     useBootstrap: false,
        //     content: "<p>" + content + "</p>",
        // });

    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to check if the variable with the given name has already been defined
function isVariableDefined(variableName) {
    return false;
    //Implementation commented
    // try {
    //     var currVariableName;
    //     for (var currRef in tests) {
    //         if (!isEmptyObject(tests[currRef].responseBodyAssertions)) {
    //             for (var currAssertion in tests[currRef].responseBodyAssertions) {
    //                 currVariableName = tests[currRef].responseBodyAssertions[currAssertion].variableName;
    //                 if (currVariableName !== undefined) {
    //                     if (currVariableName.toUpperCase() == variableName.toUpperCase()) {
    //                         return true;
    //                     }
    //                 }
    //             }
    //         }
    //         if (!isEmptyObject(tests[currRef].responseHeaderAssertions)) {
    //             for (var currAssertion in tests[currRef].responseHeaderAssertions) {
    //                 currVariableName = tests[currRef].responseHeaderAssertions[currAssertion].variableName;
    //                 if (currVariableName !== undefined) {
    //                     if (currVariableName.toUpperCase() == variableName.toUpperCase()) {
    //                         return true;
    //                     }
    //                 }
    //             }
    //         }
    //     }

    //     if (!isEmptyObject(responseBodyAssertions)) {
    //         for (var currAssertion in responseBodyAssertions) {
    //             currVariableName = responseBodyAssertions[currAssertion].variableName;
    //             if (currVariableName !== undefined) {
    //                 if (currVariableName.toUpperCase() == variableName.toUpperCase()) {
    //                     return true;
    //                 }
    //             }
    //         }
    //     }
    //     if (!isEmptyObject(responseHeaderAssertions)) {
    //         for (var currAssertion in responseHeaderAssertions) {
    //             currVariableName = responseHeaderAssertions[currAssertion].variableName;
    //             if (currVariableName !== undefined) {
    //                 if (currVariableName.toUpperCase() == variableName.toUpperCase()) {
    //                     return true;
    //                 }
    //             }
    //         }
    //     }
    //     return false;
    // } catch (err) {
    //     console.error(err);
    //     alert("Unexpected Error. Check console log for details.");
    // }
}

//Function to validate if the specified value is a valid HTTP Method
function isValidHTTPMethod(httpMethod) {
    try {
        if (httpMethod == 'POST' || httpMethod == 'PUT' || httpMethod == 'GET' || httpMethod == 'DELETE' || httpMethod == 'PATCH' || httpMethod == 'OPTIONS') {
            return true;
        }
        return false;
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to remove characters such as '\n' etc
function sanitisePayload(payload) {
    try {
        //Attempt to parse as JSON
        return JSON.stringify(JSON.parse(payload));
    } catch (err) {
        //TODO: Add generic sanity code
        return payload;
    }
}

//Function to pause thread for given time in milliseconds
function sleep(millis) {
    var date = new Date();
    var curDate = null;
    do {
        curDate = new Date();
    }
    while (curDate - date < millis);
}

//Function to handle click event of 'Test Path' Button
function testPath_btnClick() {
    try {
        //Reset border colors
        document.getElementById('responsePathExpression_txt').style.border = '1px solid #ccc';
        document.getElementById('responseExpectedValue_input').style.border = '1px solid #ccc';

        var assertionPath = document.getElementById('responsePathExpression_txt').value;
        if (isEmptyVar(assertionPath)) {
            document.getElementById('responsePathExpression_txt').style.border = '1px solid red';
            $.alert({
                title: 'Error',
                type: 'red',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Path Expression cannot be empty</p>"
            });
            return;
        }

        var value;
        if (document.getElementById('responseBody_radio_btn').checked == true) {
            var responseBodyStr = document.getElementById('responseBody_txtArea').value;
            if (isEmptyVar(responseBodyStr)) {
                $.alert({
                    title: 'Error',
                    type: 'red',
                    boxWidth: '35%',
                    useBootstrap: false,
                    content: "<p>Response Body is Empty</p>"
                });
                return;
            }
            var responseBodyJSON = JSON.parse(responseBodyStr);
            value = jsonPath(responseBodyJSON, assertionPath);
        } else {
            var responseHeaderStr = document.getElementById('responseHeaders_txtArea').value;
            if (isEmptyVar(responseHeaderStr)) {
                $.alert({
                    title: 'Error',
                    type: 'red',
                    boxWidth: '35%',
                    useBootstrap: false,
                    content: "<p>Response Header map is Empty</p>"
                });
                return;
            }
            var responseHeadersJSON = JSON.parse(responseHeaderStr);
            value = jsonPath(responseHeadersJSON, assertionPath);
        }

        if (value === false) {
            $.alert({
                title: 'Information',
                type: 'dark',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Value not Found</p>"
            });

        } else {
            $.alert({
                title: 'Information',
                type: 'dark',
                boxWidth: '35%',
                useBootstrap: false,
                content: "<p>Value:" + value + "</p>"
            });
        }

    } catch (err) {
        document.getElementById('responsePathExpression_txt').style.border = '1px solid red';
        $.alert({
            title: 'Error',
            type: 'red',
            boxWidth: '35%',
            useBootstrap: false,
            content: "<p>Invalid Path Expression</p>"
        });
    }
}

//Function to validate the Test Sequence - Restrict input to digits[0-9] only
function validateTestSequence(evt) {
    var theEvent = evt || window.event;

    // Handle paste
    if (theEvent.type === 'paste') {
        key = event.clipboardData.getData('text/plain');
    } else {
        // Handle key press
        var key = theEvent.keyCode || theEvent.which;
        key = String.fromCharCode(key);
    }
    //Validate Input
    var regex = /[0-9]/;
    if (!regex.test(key)) {
        theEvent.returnValue = false;
        if (theEvent.preventDefault) {
            theEvent.preventDefault();
        }
    }
}

//Function to toggle state of 'Exepcted Value Field' on click of 'is Value Agnostic' checkbox
function toggleExpectedValueFieldState(checkbox) {
    var expectedValueField = document.getElementById('responseExpectedValue_input');
    var assertionOpertors_select = document.getElementById('assertionOperator_select');
    expectedValueField.style.border = '1px solid #ccc';
    if (checkbox.checked == true) {
        assertionOpertors_select.disabled = true;
        expectedValueField.value = "";
        expectedValueField.disabled = true;
        expectedValueField.placeholder = "Not Applicable";
    } else {
        var selectedDataType = document.getElementById('responseExpectedDataType_select').value;
        setAssertionOperator(selectedDataType);
        assertionOpertors_select.disabled = false;
        expectedValueField.placeholder = "Assertion Value(s)";
        expectedValueField.disabled = false;
    }
}
//Function to validate if the passed string is a valid URL
function isValidURL(str) {
    if (isEmptyVar(str)) {
        return false;
    }
    var pattern = new RegExp('^(https?:\\/\\/)?' + // protocol
        '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.?)+[a-z]{2,}|' + // domain name
        '((\\d{1,3}\\.){3}\\d{1,3}))' + // OR ip (v4) address
        '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' + // port and path
        '(\\?[;&a-z\\d%_.~+=-]*)?' + // query string
        '(\\#[-a-z\\d_]*)?$', 'i'); // fragment locator
    return pattern.test(str);
}

//Function to handle on-change event for 'responseExpectedDataType_select'
function responseExpectedDataType_select_onChange(selectedDataType_select) {
    var selectedDataType = selectedDataType_select.value;
    setAssertionOperator(selectedDataType);
}

//Function to set Assertion Operators based on selected data-type
function setAssertionOperator(selectedDataType) {

    var isValueAgnosticChecked = document.getElementById('isValueAgnostic_chk').checked;
    if (isValueAgnosticChecked === false) {
        var assertionOpertors_select = document.getElementById('assertionOperator_select');
        assertionOpertors_select.options.length = 0;
        var operators = [];
        operators = Object.keys(typeOperators[selectedDataType]);
        operators.forEach(function (operator) {
            var option = document.createElement("option");
            option.text = operator;
            assertionOpertors_select.add(option);
        });
    }
}

//Function to clear the Auth Payload and set the Empty View
function clearAuthPayloadAndSetEmptyView() {
    document.getElementById('authPayloadBody_txtArea').value = "";
    document.getElementById('authPayloadHeaders_txtArea').value = "";
    document.getElementById('customAuthPayload_chk').checked = false;
    document.getElementById('authPayloadBody_txtArea').disabled = true;
    document.getElementById('authPayloadHeaders_txtArea').disabled = true;
    document.getElementById('AuthRequestHeaders_a').style.color = '#a4a4a8';
    document.getElementById('AuthRequestBody_a').style.color = '#a4a4a8';
    document.getElementById('AuthRequestHeaders_a').innerHTML = 'Default Auth Request Headers';
    document.getElementById('AuthRequestBody_a').innerHTML = 'Default Auth Request Body';
}

//Function to set Identity Service Payload based on selected Identity Service
function setIdentityServicePayload(appName, identityServiceName) {

    if (isEmptyVar(appName) || isEmptyVar(identityServiceName)) {
        //Clear Auth Payloads
        clearAuthPayloadAndSetEmptyView();
        return;
    }

    var applications = selectedRuntimeData.applications;
    for (var currRef in applications) {
        if (applications[currRef].name == appName) {
            selectedAppData = applications[currRef];
        }
    }
    var identityServices = selectedAppData.identityServices;
    for (var index in identityServices) {
        if (identityServices[index].providerName == identityServiceName) {
            var requestBody = identityServices[index].requestBody;
            var requestHeaders = identityServices[index].requestHeaders;
            try {
                if (!isEmptyObject(requestBody)) {
                    document.getElementById('authPayloadBody_txtArea').value = JSON.stringify(JSON.parse(requestBody), null, 4);
                } else {
                    document.getElementById('authPayloadBody_txtArea').value = "";
                }
                if (!isEmptyObject(requestHeaders)) {
                    document.getElementById('authPayloadHeaders_txtArea').value = JSON.stringify(requestHeaders, null, 4);
                } else {
                    document.getElementById('authPayloadHeaders_txtArea').value = "";
                }
                break;
            } catch (err) {
                document.getElementById('authPayloadBody_txtArea').value = requestBody;
                document.getElementById('authPayloadHeaders_txtArea').value = requestHeaders;
                break;
            }
        }
    }
}

//Function to handle on-change event of security_level_select element
function securityLevel_select_onchage(selectEle) {
    try {
        var securityLevel = selectEle.value;
        if (securityLevel == "AnonymousAppUser" || securityLevel == "Public") {
            /* Anonymous App User OR Public */

            //Clear Auth Payload
            clearAuthPayloadAndSetEmptyView();

            //Disable customAuthPayload_chk checkbox 
            document.getElementById('customAuthPayload_chk').disabled = true;

            //Clear Identity Services from dropdown
            $('#identityService_select').empty();
        } else {
            /* Authenticated App User */
            setSecurityLevelToAuthenticatedAppUser();
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to set the security level to authenticated app user
function setSecurityLevelToAuthenticatedAppUser() {

    //Set Security Level to Authenticated App User
    setSecurityLevelToView("AuthenticatedAppUser");

    //Populate the Identity Services
    populateIdentityServicesList(selectedRuntimeData);

    //Set the Identity Service Data
    var selectedAppName = document.getElementById('application_select').value;
    var selectedIdentityService = document.getElementById('identityService_select').value;
    setIdentityServicePayload(selectedAppName, selectedIdentityService);

    //Enable customAuthPayload_chk checkbox 
    document.getElementById('customAuthPayload_chk').disabled = false;
}

//Function to handle on-change event of 'customAuthPayload_chk' check box
function customAuthPayload_chk_onchange(checkBox) {
    try {
        if (checkBox.checked == true) {
            //Custom Auth Payload - Enable Text Areas
            document.getElementById('authPayloadBody_txtArea').disabled = false;
            document.getElementById('authPayloadHeaders_txtArea').disabled = false;
            document.getElementById('AuthRequestHeaders_a').style.color = '#000000';
            document.getElementById('AuthRequestBody_a').style.color = '#000000';
            document.getElementById('AuthRequestHeaders_a').innerHTML = 'Custom Auth Request Headers';
            document.getElementById('AuthRequestBody_a').innerHTML = 'Custom Auth Request Body';
        } else {
            //Custom Auth Payload - Disable Text Areas
            setDefaultAuthPayload();
        }
    } catch (err) {
        console.error(err);
        alert("Unexpected Error. Check console log for details.");
    }
}

//Function to set the the default Auth Payload
function setDefaultAuthPayload() {
    document.getElementById('customAuthPayload_chk').checked = false;
    document.getElementById('authPayloadBody_txtArea').disabled = true;
    document.getElementById('authPayloadHeaders_txtArea').disabled = true;
    document.getElementById('AuthRequestHeaders_a').style.color = '#a4a4a8';
    document.getElementById('AuthRequestBody_a').style.color = '#a4a4a8';

    document.getElementById('AuthRequestHeaders_a').innerHTML = 'Default Auth Request Headers';
    document.getElementById('AuthRequestBody_a').innerHTML = 'Default Auth Request Body';

    var selectedIdentityService = document.getElementById('identityService_select').value;
    var selectedAppName = document.getElementById('application_select').value;
    setIdentityServicePayload(selectedAppName, selectedIdentityService);
}

//Function to hanlde 'on-change' event of 'Identity Service Select'
function identityService_select_onchage(identityServiceSelect) {
    var selectedIdentityService = identityServiceSelect.value;
    var selectedAppName = document.getElementById('application_select').value;
    setIdentityServicePayload(selectedAppName, selectedIdentityService);
}

//Function to hanlde 'on-click' event of 'Save Value Checkbox'
function saveValue_chk_onclick(saveValue_chk) {
    try {
        var textBox = document.getElementById('variableName_txt');
        if (saveValue_chk.checked == true) {
            //Save Attribute Value - Enable Text Box
            textBox.disabled = false;
            textBox.placeholder = "Variable Name";
        } else {
            textBox.value = "";
            textBox.disabled = true;
            textBox.placeholder = "Not Applicable";
        }
    } catch (err) {
        alert(err);
    }
}

//Function to handle on-click event of the Preview Anrch
function previewAnchor_onClick() {
    var windowContent = getDocumentPreviewPageContent();
    var messageWindow = window.open("", "_blank");
    if (!messageWindow || messageWindow.closed || typeof messageWindow.closed == 'undefined') {
        //Popup blocked
        $.alert({
            title: 'Error',
            type: 'red',
            boxWidth: '35%',
            useBootstrap: false,
            content: "<p>Pop-up Blocker is enabled! Please add this site to your exception list.</p>"
        });
    } else {
        messageWindow.document.write(windowContent);
    }
}

//Function to get the Preview Page HTML Document
function getDocumentPreviewPageContent() {
    // Prepare Service Doc JSON
    var documentJSON = {};
    documentJSON.serviceName = document.getElementById('serviceName_input').value;
    documentJSON.serviceURL = document.getElementById('serviceURL_input').value;
    documentJSON.serviceHTTPMethod = document.getElementById('requestMethod_select').value;
    documentJSON.serviceDescription = document.getElementById('serviceDescription_textArea').value;

    //Replace new line character with <br>
    if (documentJSON.serviceDescription) {
        documentJSON.serviceDescription = documentJSON.serviceDescription.replace("\n", "<br>");
    }
    documentJSON.inputFieldsDocument = getInputFieldsDocument();
    documentJSON.outputFieldsDocument = getOutputFieldsDocument();

    var documentJSONString = JSON.stringify(documentJSON);
    var htmlContent = "<html><head> <title>Service Document</title> <style>h4, p, a{font-family: Helvetica, Arial, sans-serif;}table{border-collapse: collapse; font-family: Helvetica, Arial, sans-serif; width: 100%;}th{padding: 8px; border: 1px solid black;}.noWrapCell{padding: 8px; border: 1px solid black; white-space: nowrap;}.wrapCell{padding: 8px; border: 1px solid black; word-wrap: break-word;}td, tr{padding: 8px; border: 1px solid black;}tr:nth-child(even){background-color: #f2f2f2;}</style></head><body> <img src='data:image/jpg;base64,/9j/4AAQSkZJRgABAQEASABIAAD/4QAYRXhpZgAASUkqAAgAAAAAAAAAAAAAAP/hAzNodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NjcyOSwgMjAxMi8wNS8wMy0xMzo0MDowMyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIEVsZW1lbnRzIDEyLjAgV2luZG93cyIgeG1wTU06SW5zdGFuY2VJRD0ieG1wLmlpZDo0QkM2QkYzNkI2NDcxMUU1OEY1NkE0Rjk0N0EzQ0E5NCIgeG1wTU06RG9jdW1lbnRJRD0ieG1wLmRpZDo0QkM2QkYzN0I2NDcxMUU1OEY1NkE0Rjk0N0EzQ0E5NCI+IDx4bXBNTTpEZXJpdmVkRnJvbSBzdFJlZjppbnN0YW5jZUlEPSJ4bXAuaWlkOjRCQzZCRjM0QjY0NzExRTU4RjU2QTRGOTQ3QTNDQTk0IiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjRCQzZCRjM1QjY0NzExRTU4RjU2QTRGOTQ3QTNDQTk0Ii8+IDwvcmRmOkRlc2NyaXB0aW9uPiA8L3JkZjpSREY+IDwveDp4bXBtZXRhPiA8P3hwYWNrZXQgZW5kPSJyIj8+/9sAQwAEAwMEAwMEBAQEBQUEBQcLBwcGBgcOCgoICxAO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' style='position: absolute;display: block; left: 15px; top: 18px;' height='62' width='72'/> <br><h2 align='center' style='font-family: Helvetica;'>Fabric Service Document</h2> <hr> <br><table> <tr bgcolor='#98B4D4'> <td width='20%'></td><td></td></tr><tr> <td width='20%'><b>Name</b></td><td id='service_Name_td'></td></tr><tr> <td width='20%'><b>URL</b></td><td id='service_URL_td'></td></tr><tr> <td width='20%'><b>HTTP Method</b></td><td id='service_Method_td'></td></tr><tr> <td width='20%'><b>Description</b></td><td id='service_Desc_td'></td></tr></table> <br><h3 align='center' style='font-family: Helvetica;'>Input/Output Fields</h3> <div> <table id='serviceDescription_InputFieldsTable'> <tr bgcolor='#98B4D4'> <th style='text-align: left; color: black' colspan='4'>Input Fields</th> </tr><tr> <th width='30%'>Field</th> <th width='45%'>Description</th> <th width='5%'>isMandatory</th> <th width='20%'>Range</th> </tr></table> <br><br><table id='serviceDescription_OutputFieldsTable'> <tr bgcolor='#98B4D4'> <th style='text-align: left; color: black' colspan='4'>Output Fields</th> </tr><tr> <th width='30%'>Field</th> <th width='45%'>Description</th> <th width='5%'>isMandatory</th> <th width='20%'>Range</th> </tr></table> <br></div></body><script>var documentJSON=" + documentJSONString + "; setServiceDocument(); function setServiceDocument(){if (documentJSON){/*Set Service Name*/ if (documentJSON.serviceName){document.getElementById('service_Name_td').innerHTML=documentJSON.serviceName;}/*Set Service URL*/ if (documentJSON.serviceURL){document.getElementById('service_URL_td').innerHTML=documentJSON.serviceURL;}/*Set Service Method*/ if (documentJSON.serviceHTTPMethod){document.getElementById('service_Method_td').innerHTML=documentJSON.serviceHTTPMethod;}/*Set Service Description*/ if (documentJSON.serviceDescription){document.getElementById('service_Desc_td').innerHTML=documentJSON.serviceDescription;}var currRow, currCell, currCheckBox, tableReferece; /*Set Input Parameters Description*/ var inputFieldsDocument=documentJSON.inputFieldsDocument; if (inputFieldsDocument){tableReferece=document.getElementById('serviceDescription_InputFieldsTable'); for (var index=0; index < inputFieldsDocument.length; index++){/*Add New Row*/ currRow=tableReferece.insertRow(-1); /*Field Cell*/ currCell=currRow.insertCell(-1); currCell.innerHTML=inputFieldsDocument[index].name; currCell.className='noWrapCell'; /*Description Cell*/ currCell=currRow.insertCell(-1); currCell.innerHTML=inputFieldsDocument[index].description; /*Is Mandatory Cell*/ /*Is Mandatory Cell*/ currCell=currRow.insertCell(-1); if (inputFieldsDocument[index].isMandatory){currCell.innerHTML='<img src=\"data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iTGF5ZXJfMSIgeD0iMHB4IiB5PSIwcHgiIHZpZXdCb3g9IjAgMCA1MTIgNTEyIiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCA1MTIgNTEyOyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSIgd2lkdGg9IjE2cHgiIGhlaWdodD0iMTZweCI+CjxnPgoJPGc+CgkJPHBhdGggZD0iTTUwNC41MDIsNzUuNDk2Yy05Ljk5Ny05Ljk5OC0yNi4yMDUtOS45OTgtMzYuMjA0LDBMMTYxLjU5NCwzODIuMjAzTDQzLjcwMiwyNjQuMzExYy05Ljk5Ny05Ljk5OC0yNi4yMDUtOS45OTctMzYuMjA0LDAgICAgYy05Ljk5OCw5Ljk5Ny05Ljk5OCwyNi4yMDUsMCwzNi4yMDNsMTM1Ljk5NCwxMzUuOTkyYzkuOTk0LDkuOTk3LDI2LjIxNCw5Ljk5LDM2LjIwNCwwTDUwNC41MDIsMTExLjcgICAgQzUxNC41LDEwMS43MDMsNTE0LjQ5OSw4NS40OTQsNTA0LjUwMiw3NS40OTZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8L2c+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg==\"/>'}currCell.style.textAlign='center'; /*Range Cell*/ currCell=currRow.insertCell(-1); currCell.innerHTML=inputFieldsDocument[index].range;}}/*Set Output Parameters Description */ var outputFieldsDocument=documentJSON.outputFieldsDocument; if (outputFieldsDocument){tableReferece=document.getElementById('serviceDescription_OutputFieldsTable'); for (var index=0; index < outputFieldsDocument.length; index++){/*Add New Row*/ /*Field Cell*/ currRow=tableReferece.insertRow(-1); currCell=currRow.insertCell(-1); currCell.innerHTML=outputFieldsDocument[index].name; /*Description Cell*/ currCell.className='noWrapCell'; currCell=currRow.insertCell(-1); currCell.innerHTML=outputFieldsDocument[index].description; /*Is Mandatory Cell*/ currCell=currRow.insertCell(-1); if (outputFieldsDocument[index].isMandatory){currCell.innerHTML='<img src=\"data:image/svg+xml;utf8;base64,PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iaXNvLTg4NTktMSI/Pgo8IS0tIEdlbmVyYXRvcjogQWRvYmUgSWxsdXN0cmF0b3IgMTkuMC4wLCBTVkcgRXhwb3J0IFBsdWctSW4gLiBTVkcgVmVyc2lvbjogNi4wMCBCdWlsZCAwKSAgLS0+CjxzdmcgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIiB4bWxuczp4bGluaz0iaHR0cDovL3d3dy53My5vcmcvMTk5OS94bGluayIgdmVyc2lvbj0iMS4xIiBpZD0iTGF5ZXJfMSIgeD0iMHB4IiB5PSIwcHgiIHZpZXdCb3g9IjAgMCA1MTIgNTEyIiBzdHlsZT0iZW5hYmxlLWJhY2tncm91bmQ6bmV3IDAgMCA1MTIgNTEyOyIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSIgd2lkdGg9IjE2cHgiIGhlaWdodD0iMTZweCI+CjxnPgoJPGc+CgkJPHBhdGggZD0iTTUwNC41MDIsNzUuNDk2Yy05Ljk5Ny05Ljk5OC0yNi4yMDUtOS45OTgtMzYuMjA0LDBMMTYxLjU5NCwzODIuMjAzTDQzLjcwMiwyNjQuMzExYy05Ljk5Ny05Ljk5OC0yNi4yMDUtOS45OTctMzYuMjA0LDAgICAgYy05Ljk5OCw5Ljk5Ny05Ljk5OCwyNi4yMDUsMCwzNi4yMDNsMTM1Ljk5NCwxMzUuOTkyYzkuOTk0LDkuOTk3LDI2LjIxNCw5Ljk5LDM2LjIwNCwwTDUwNC41MDIsMTExLjcgICAgQzUxNC41LDEwMS43MDMsNTE0LjQ5OSw4NS40OTQsNTA0LjUwMiw3NS40OTZ6IiBmaWxsPSIjMDAwMDAwIi8+Cgk8L2c+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPGc+CjwvZz4KPC9zdmc+Cg==\"/>'}currCell.style.textAlign='center'; currCell.style.width='5%'; /*Range Cell*/ currCell=currRow.insertCell(-1); currCell.innerHTML=outputFieldsDocument[index].range;}}}}</script></html>";
    return htmlContent;
}


//Function to check if the passed object is empty
function isEmptyObject(obj) {
    return jQuery.isEmptyObject(obj);
}

/* JSONPath 0.8.0 - XPath for JSON
 *
 * Copyright (c) 2007 Stefan Goessner (goessner.net)
 * Licensed under the MIT (MIT-LICENSE.txt) licence.
 */
function jsonPath(obj, expr, arg) {
    var P = {
        resultType: arg && arg.resultType || "VALUE",
        result: [],
        normalize: function (expr) {
            var subx = [];
            return expr.replace(/[\['](\??\(.*?\))[\]']/g, function ($0, $1) {
                    return "[#" + (subx.push($1) - 1) + "]";
                })
                .replace(/'?\.'?|\['?/g, ";")
                .replace(/;;;|;;/g, ";..;")
                .replace(/;$|'?\]|'$/g, "")
                .replace(/#([0-9]+)/g, function ($0, $1) {
                    return subx[$1];
                });
        },
        asPath: function (path) {
            var x = path.split(";"),
                p = "$";
            for (var i = 1, n = x.length; i < n; i++)
                p += /^[0-9*]+$/.test(x[i]) ? ("[" + x[i] + "]") : ("['" + x[i] + "']");
            return p;
        },
        store: function (p, v) {
            if (p) P.result[P.result.length] = P.resultType == "PATH" ? P.asPath(p) : v;
            return !!p;
        },
        trace: function (expr, val, path) {
            if (expr) {
                var x = expr.split(";"),
                    loc = x.shift();
                x = x.join(";");
                if (val && val.hasOwnProperty(loc))
                    P.trace(x, val[loc], path + ";" + loc);
                else if (loc === "*")
                    P.walk(loc, x, val, path, function (m, l, x, v, p) {
                        P.trace(m + ";" + x, v, p);
                    });
                else if (loc === "..") {
                    P.trace(x, val, path);
                    P.walk(loc, x, val, path, function (m, l, x, v, p) {
                        typeof v[m] === "object" && P.trace("..;" + x, v[m], p + ";" + m);
                    });
                } else if (/,/.test(loc)) { // [name1,name2,...]
                    for (var s = loc.split(/'?,'?/), i = 0, n = s.length; i < n; i++)
                        P.trace(s[i] + ";" + x, val, path);
                } else if (/^\(.*?\)$/.test(loc)) // [(expr)]
                    P.trace(P.eval(loc, val, path.substr(path.lastIndexOf(";") + 1)) + ";" + x, val, path);
                else if (/^\?\(.*?\)$/.test(loc)) // [?(expr)]
                    P.walk(loc, x, val, path, function (m, l, x, v, p) {
                        if (P.eval(l.replace(/^\?\((.*?)\)$/, "$1"), v[m], m)) P.trace(m + ";" + x, v,
                            p);
                    });
                else if (/^(-?[0-9]*):(-?[0-9]*):?([0-9]*)$/.test(loc)) // [start:end:step]  phyton slice syntax
                    P.slice(loc, x, val, path);
            } else
                P.store(path, val);
        },
        walk: function (loc, expr, val, path, f) {
            if (val instanceof Array) {
                for (var i = 0, n = val.length; i < n; i++)
                    if (i in val)
                        f(i, loc, expr, val, path);
            } else if (typeof val === "object") {
                for (var m in val)
                    if (val.hasOwnProperty(m))
                        f(m, loc, expr, val, path);
            }
        },
        slice: function (loc, expr, val, path) {
            if (val instanceof Array) {
                var len = val.length,
                    start = 0,
                    end = len,
                    step = 1;
                loc.replace(/^(-?[0-9]*):(-?[0-9]*):?(-?[0-9]*)$/g, function ($0, $1, $2, $3) {
                    start = parseInt($1 || start);
                    end = parseInt($2 || end);
                    step = parseInt($3 || step);
                });
                start = (start < 0) ? Math.max(0, start + len) : Math.min(len, start);
                end = (end < 0) ? Math.max(0, end + len) : Math.min(len, end);
                for (var i = start; i < end; i += step)
                    P.trace(i + ";" + expr, val, path);
            }
        },
        eval: function (x, _v, _vname) {
            try {
                return $ && _v && eval(x.replace(/@/g, "_v"));
            } catch (e) {
                throw new SyntaxError("jsonPath: " + e.message + ": " + x.replace(/@/g, "_v").replace(/\^/g,
                    "_a"));
            }
        }
    };

    var $ = obj;
    if (expr && obj && (P.resultType == "VALUE" || P.resultType == "PATH")) {
        P.trace(P.normalize(expr).replace(/^\$;/, ""), obj, "$");
        return P.result.length ? P.result : false;
    }
}