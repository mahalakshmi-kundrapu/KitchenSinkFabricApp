package com.kony.adminconsole.service.poc;

import com.kony.adminconsole.exception.ApplicationException;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.controller.DataControllerResponse;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;

/**
 * Sample DBX Service
 * 
 * @author Aditya Mankal
 *
 */
public class Customer360Service extends DBXService {

    /**
     * Sample DBX Service Method
     */
    @Override
    protected Object invokeService(String methodID, Object[] inputArray, DataControllerRequest requestInstance,
            DataControllerResponse responseInstance) throws Exception {

        /*
         * DBX Business Code - Can intentionally throw Custom Exception based on scenario - ApplicationException - May
         * result in unexpected Exception
         */

        Result result = new Result();

        // Perform Task 1
        Record task1Record = doTask1(requestInstance);
        result.addRecord(task1Record);

        // Perform Task 2
        Record task2Record = doTask2(requestInstance);
        result.addRecord(task2Record);

        return result;
    }

    /**
     * Sample DBX Business Method
     * 
     * @param requestInstance
     * @return Record
     * @throws ApplicationException
     */
    private Record doTask1(DataControllerRequest requestInstance) throws ApplicationException {
        // Some business code
        Record record = new Record();
        record.setId("task1");
        return record;
    }

    /**
     * Sample DBX Business Method
     * 
     * @param requestInstance
     * @return Record
     * @throws ApplicationException
     */
    private Record doTask2(DataControllerRequest requestInstance) throws ApplicationException {
        // Some business code
        Record record = new Record();
        record.setId("task1");
        return record;
    }
}
