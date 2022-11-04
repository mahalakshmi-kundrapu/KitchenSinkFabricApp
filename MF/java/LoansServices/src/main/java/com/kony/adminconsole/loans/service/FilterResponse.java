package com.kony.adminconsole.loans.service;

import com.konylabs.middleware.common.DataPostProcessor;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.dataobject.Param;
import com.konylabs.middleware.dataobject.Record;
import com.konylabs.middleware.dataobject.Result;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class FilterResponse
  implements DataPostProcessor
{
  private static final Logger LOG = Logger.getLogger(FilterResponse.class);
  
  public FilterResponse() {}
  
  public Object execute(Result arg0, DataControllerRequest arg1) throws Exception {
    System.out.println("Starting post processor excesution...");
    
    Record output = arg0.getRecordById("output");
    Record updatedConfigBundles = output.getRecordById("updatedConfigurations");
    Record finalBundles = output.getRecordById("finalBundles");
    ArrayList<Param> paramsConfigurations = updatedConfigBundles.getParams();
    List<String> toBeRemovedId = new ArrayList();
    LinkedHashSet<String> bundles = new LinkedHashSet();
    
    Param downloadKeys = finalBundles.getParam("downloadServerKeys");
    String downloadServerKeys = downloadKeys.getValue();
    
    if (downloadServerKeys.isEmpty())
    {
      return arg0;
    }
    
    try
    {
      if (downloadServerKeys.toLowerCase().contains("false"))
      {
        for (Param param : updatedConfigBundles.getParams())
        {
          String value = param.getValue();
          String bundle_id = param.getName();
          if (value.toLowerCase().contains("server_key")) {
            toBeRemovedId.add(bundle_id);
          } else {
            bundles.add(bundle_id);
          }
        }
      } else if (downloadServerKeys.toLowerCase().contains("true"))
      {
        for (Param param : updatedConfigBundles.getParams())
        {
          String value = param.getValue();
          String bundle_id = param.getName();
          if (value.toLowerCase().contains("server_key")) {
            bundles.add(bundle_id);
          } else {
            toBeRemovedId.add(bundle_id);
          }
          
        }
      }
    }
    catch (Exception e)
    {
      System.out.println("error = " + e.toString());
    }
    
    for (String bundleid : toBeRemovedId) {
      updatedConfigBundles.removeParamsByName(bundleid);
    }
    
    finalBundles.removeParamsByName("bundles");
    finalBundles.setParam(new Param("bundles", StringUtils.join(bundles, ","), "JSON"));
    return arg0;
  }
}
