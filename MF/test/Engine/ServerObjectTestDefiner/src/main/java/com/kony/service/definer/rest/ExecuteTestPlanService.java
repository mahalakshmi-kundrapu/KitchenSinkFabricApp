package com.kony.service.definer.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;

import com.kony.service.dto.Service;
import com.kony.service.dto.TestArticle;
import com.kony.service.dto.TestCase;
import com.kony.service.dto.TestReport;
import com.kony.service.dto.TestSummary;
import com.kony.service.test.manager.TestManager;
import com.kony.service.test.report.HTMLReportGenerator;

/**
 * Class to handle Requests on resource {@link TestArticle}
 *
 * @author Aditya Mankal
 */
@Path("/TestPlan")
public class ExecuteTestPlanService {

	@POST
	@Path("/executeTestPlan")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response executeRequest(TestArticle testArticle) throws IOException {

		Map<Service, List<TestReport>> servicesTestsReportMap = new LinkedHashMap<>();
		TestSummary testSummary = new TestSummary();

		List<Service> services = testArticle.getServices();

		// Traverse services
		if (services != null && !services.isEmpty()) {

			for (Service service : services) {
				// Execute Service Tests
				List<TestCase> testCases = service.getTests();
				testSummary.setTestStartDateTime(new Date());
				List<TestReport> testReports = new ArrayList<>();
				for (TestCase testCase : testCases) {
					testReports.add(TestManager.executeTest(testArticle, service, testCase));
				}

				// Store Test-Reports of current Service
				testSummary.setTestCompletionDateTime(new Date());
				servicesTestsReportMap.put(service, testReports);
			}

		}

		// Sort Test Plan Reports
		servicesTestsReportMap = TestManager.sortTestPlanReports(servicesTestsReportMap, testArticle.getTestPlansExecutionOrder());

		// Initialise Test Summary
		testSummary.setAppName(testArticle.getAppName());
		testSummary.setAppVersion(testArticle.getAppVersion());
		testSummary.setTestEnvironmentHostURL(testArticle.getHostURL());
		testSummary.setTestEnvironmentAuthServiceURL(testArticle.getAuthServiceURL());
		testSummary.setTestInititedBy("Kony Dev");

		// Prepare Summary Report
		testSummary.setTestResults(servicesTestsReportMap);
		String htmlReport = HTMLReportGenerator.getTestSummaryAsHTMLMessage(testSummary);

		// Return Response
		return Response.status(HttpStatus.SC_OK).entity(htmlReport).header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML).build();
	}
}
