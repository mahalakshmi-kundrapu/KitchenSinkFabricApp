package com.kony.adminconsole.core.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.dbp.core.auth.FabricServicePermissionsDTO;
import com.dbp.core.service.FabricServicePermissionsService;
import com.kony.adminconsole.commons.utils.CommonUtilities;
import com.kony.adminconsole.commons.utils.FabricConstants;
import com.kony.adminconsole.utilities.Executor;
import com.kony.adminconsole.utilities.ServiceURLEnum;

/**
 * Class to register the Customer360 Service - Permission Mapping
 * 
 * @author Aditya Mankal
 *
 */
public class ServicePermissionMapRegister implements FabricServicePermissionsService {

    private static final Logger LOG = Logger.getLogger(ServicePermissionMapRegister.class);

    @Override
    public List<FabricServicePermissionsDTO> getFabricServicePermissionsMappings() {

        try {

            List<FabricServicePermissionsDTO> fabricServicePermissions = new ArrayList<>();

            // Fetch Fabric Services
            String serviceResponse = Executor.invokeService(ServiceURLEnum.SERVICE_PERMISSION_MAPPER_READ,
                    new HashMap<String, String>(), new HashMap<String, String>(), StringUtils.EMPTY);

            // Parse Service Response
            JSONObject serviceResponseJSON = CommonUtilities.getStringAsJSONObject(serviceResponse);
            if (serviceResponseJSON == null || !serviceResponseJSON.has(FabricConstants.OPSTATUS)
                    || serviceResponseJSON.getInt(FabricConstants.OPSTATUS) != 0) {
                LOG.error("Failed to fetch Fabric Service Permission mappings. Service Response:" + serviceResponse);
                return null;
            }
            LOG.debug("Fetched Fabric Service Permission mappings");

            JSONArray servicePermissionsArray = serviceResponseJSON.optJSONArray("service_permission_mapper");
            if (servicePermissionsArray != null) {

                LOG.debug("Count of Records:" + servicePermissionsArray.length());

                JSONObject currJSON = null;
                List<String> permissionsList;
                FabricServicePermissionsDTO fabricServicePermission = null;
                String id, serviceName, objectName, operationName, userType, permissions;

                // Traverse servicePermissionsArray and construct FabricServicePermissionsDTO
                // instances
                for (Object currObject : servicePermissionsArray) {
                    if (currObject instanceof JSONObject) {
                        currJSON = (JSONObject) currObject;

                        id = currJSON.optString("id");
                        serviceName = currJSON.optString("service_name");
                        objectName = currJSON.optString("object_name");
                        operationName = currJSON.optString("operation");
                        userType = currJSON.optString("user_type");
                        permissions = currJSON.optString("permissions");
                        permissionsList = CommonUtilities.getStringifiedArrayAsList(permissions);
                        fabricServicePermission = new FabricServicePermissionsDTO(id, serviceName, objectName,
                                operationName, userType, permissionsList);

                        // Add current mapping to list
                        fabricServicePermissions.add(fabricServicePermission);
                    }
                }
            }

            // Return Service Permission Mapping
            LOG.debug("Returning Service Permission map");
            return fabricServicePermissions;

        } catch (Exception e) {
            LOG.error("Exception in registering service-permission mapping. Exception:", e);
            return null;
        }
    }

}
