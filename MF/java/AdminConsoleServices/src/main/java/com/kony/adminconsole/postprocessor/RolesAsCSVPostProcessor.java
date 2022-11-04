package com.kony.adminconsole.postprocessor;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.StringEntity;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.ErrorCodeEnum;
import com.konylabs.middleware.common.DataPostProcessor2;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Dataset;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Dataprocessor used to convert roles data to CSV and pushes the stream in actual response
 * 
 * @author Venkateswara Rao Alla
 *
 */

public class RolesAsCSVPostProcessor implements DataPostProcessor2 {

    @Override
    public Object execute(Result result, DataControllerRequest request, DataControllerResponse response)
            throws Exception {

        Result thisResult = new Result(); // current process result
        StringBuilder builder = new StringBuilder(); // holds the CSV content
        boolean onError = false;

        try {
            CSVPrinter csvPrinter = CSVFormat.DEFAULT.withHeader("Role", "Description", "Status").print(builder);

            // getting all roles from result object being sent by previous service
            Dataset rolesDataset = result.getDatasetById("roles_view");
            if (rolesDataset != null) {
                for (Record record : rolesDataset.getAllRecords()) {
                    csvPrinter.printRecord(record.getParam("role_Name").getValue(),
                            record.getParam("role_Desc").getValue(), record.getParam("Status_id").getValue());
                }
            }

            csvPrinter.close();

        } catch (Exception e) {
            onError = true;
            setErrorParams(thisResult);
        }

        if (!onError) {
            try {
                // below is the main logic used to send the binary data to client in response.
                // We need to use
                // orchestration service and has to enable pass through output
                Map<String, String> customHeaders = new HashMap<String, String>();
                customHeaders.put("Content-Type", "text/plain; charset=utf-8");
                customHeaders.put("Content-Disposition",
                        "attachment; filename=\"roles_" + (new Date()).getTime() + ".csv\"");

                response.setAttribute(FabricConstants.CHUNKED_RESULTS_IN_JSON,
                        new BufferedHttpEntity(new StringEntity(builder.toString(), StandardCharsets.UTF_8)));
                response.getHeaders().putAll(customHeaders);
            } catch (Exception e) {
                setErrorParams(thisResult);
            }
        }

        return thisResult;
    }

    private void setErrorParams(Result result) {
        ErrorCodeEnum.ERR_20564.setErrorCode(result);
        result.addParam(new Param(FabricConstants.HTTP_STATUS_CODE, String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR),
                FabricConstants.INT));
    }

}