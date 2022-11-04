package com.kony.adminconsole.loans.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.kony.adminconsole.commons.utils.FabricConstants;
import com.konylabs.middleware.controller.DataControllerRequest;
import com.konylabs.middleware.exceptions.MetricsException;
import com.konylabs.middleware.metrics.KonyCustomMetrics;
import com.konylabs.middleware.metrics.KonyCustomMetricsDataSet;

public class LoansMetricsProcessorHelper {

	public boolean addCustomMetrics(DataControllerRequest request,
			List<LoansMetricsData> custMetrics, Boolean isOld)
			throws MetricsException, ParseException {
		if (null != request.getAttribute(FabricConstants.CUSTOM_LIB)) {
			KonyCustomMetrics objKonyCustomMetrics = (KonyCustomMetrics) request
					.getAttribute(FabricConstants.CUSTOM_LIB);
			if (isOld) {
				List<KonyCustomMetricsDataSet> cusMetDataSetList = objKonyCustomMetrics
						.getCustomMetrics();
				if (null != cusMetDataSetList && !cusMetDataSetList.isEmpty()) {
					KonyCustomMetricsDataSet konyCustomMetricsDataSet = (KonyCustomMetricsDataSet) cusMetDataSetList
							.get(0);
					addCustomMetricToMetricDataset(custMetrics,
							konyCustomMetricsDataSet);
				}
			} else {
				KonyCustomMetricsDataSet metricsDataset = addCustomMetricToMetricDataset(
						custMetrics, null);
				objKonyCustomMetrics.addCustomMetrics(metricsDataset);
			}
		} else {
			addCustomMetrics(request, custMetrics);
		}
		return true;
	}

	private boolean addCustomMetrics(DataControllerRequest request,
			List<LoansMetricsData> custMetrics) throws MetricsException,
			ParseException {
		KonyCustomMetrics kmetrics = new KonyCustomMetrics();
		KonyCustomMetricsDataSet metricsDataset = addCustomMetricToMetricDataset(
				custMetrics, null);
		kmetrics.addCustomMetrics(metricsDataset);
		request.setAttribute(FabricConstants.CUSTOM_LIB, kmetrics);
		return true;
	}

	private KonyCustomMetricsDataSet addCustomMetricToMetricDataset(
			List<LoansMetricsData> custMetrics,
			KonyCustomMetricsDataSet metricsDataset) throws MetricsException,
			ParseException {
		metricsDataset = (null == metricsDataset) ? new KonyCustomMetricsDataSet()
				: metricsDataset;
		for (int i = 0; i < custMetrics.size(); i++) {
			LoansMetricsData metricData = custMetrics.get(i);
			switch (metricData.getMetricType()) {
			case LoansMetricsData.STRING:
				metricsDataset.setMetricsString(metricData.getMetricName(),
						metricData.getMetricValue());
				break;
			case LoansMetricsData.BOOLEAN:
				metricsDataset.setMetricsBoolean(metricData.getMetricName(),
						Boolean.parseBoolean(metricData.getMetricValue()));
				break;
			case LoansMetricsData.LONG:
				metricsDataset.setMetricsLong(metricData.getMetricName(),
						Long.parseLong(metricData.getMetricValue()));
				break;
			case LoansMetricsData.DOUBLE:
				metricsDataset.setMetricsDouble(metricData.getMetricName(),
						Double.parseDouble(metricData.getMetricValue()));
				break;
			case LoansMetricsData.DATE:
				metricsDataset.setMetricsDate(metricData.getMetricName(),
						getCurrentDate(metricData.getMetricValue()));
				break;
			case LoansMetricsData.TIMESTAMP:
				metricsDataset.setMetricsTimestamp(metricData.getMetricName(),
						metricData.getMetricValue());
				break;
			default :
				metricsDataset.setMetricsString(metricData.getMetricName(),
						metricData.getMetricValue());
			}
		}
		return metricsDataset;
	}

	private Date getCurrentDate(String metricDate) throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.parse(metricDate);
	}
}
