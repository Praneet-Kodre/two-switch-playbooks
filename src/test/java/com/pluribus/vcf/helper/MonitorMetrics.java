package com.pluribus.vcf.helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.DatatypeConverter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.openqa.selenium.WebDriver;

import com.pluribus.vcf.helper.CustomHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MonitorMetrics extends PageInfra implements Runnable {

	private java.util.Properties metricsProperties;
	private String vcfIpForMetrics;
	private static StringBuffer result;
	private String metricsUserName;
	private String metricsPassword;
	int memoryThresholdViolationCount = 0;
	int cpuThresholdViolationCount = 0;

	public MonitorMetrics(WebDriver driver, String vcfIp, String vcfUserName, String password) throws IOException {
		super(driver);
		metricsProperties = new Properties();
		metricsProperties.loadFromXML(new FileInputStream("metrics.xml"));
		this.vcfIpForMetrics = vcfIp;
		this.metricsUserName = vcfUserName;
		this.metricsPassword = password;
	}

	public void runMetrics() throws InterruptedException, IOException {



		try {
				HttpClient httpclient = CustomHttpClient.getInstance().getNewHttpClient(null);
				HttpGet httpget = new HttpGet("https://" + vcfIpForMetrics + "/vcf-mgr/metrics");
				String userNamePassword = metricsUserName+":"+metricsPassword;
				String encoding = DatatypeConverter.printBase64Binary(userNamePassword.getBytes("UTF-8"));
				httpget.setHeader("Authorization", "Basic " + encoding);
				HttpResponse response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
						result = new StringBuffer();
						
						String line = null;
						while ((line = reader.readLine()) != null) {
							result.append(line);
						}
					} catch (IOException ex) {
						throw ex;

					} catch (RuntimeException ex) {
						httpget.abort();
						throw ex;

					} finally {
						instream.close();
					}
				}
			String json = result.toString();
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> obj = mapper.readValue(json, Map.class);
				printLogs("info", "metricsFile", "=================================================");
				printLogs("info", "metricsFile", "Time when below Sample was taken : " + Calendar.getInstance().getTime());
				printLogs("info", "metricsFile", "=================================================");
				int mem = (Integer) obj.get("mem");
				int freeMem = (Integer) obj.get("mem.free");
				printLogs("info", "metricsFile", "Total Memory : " + mem);
				printLogs("info", "metricsFile", "Free Memory : " + freeMem);
				float memPerformance;
				memPerformance = (float) ((freeMem * 100) / mem);
				printLogs("info", "metricsFile", "Memory Performance in Percentage : " + memPerformance);
				
				int totalMemorySamples = (Integer.parseInt(metricsProperties.getProperty("monitiorCycle"))
						* Integer.parseInt(metricsProperties.getProperty("monitorPeriod")));
				if (memPerformance >= Integer.parseInt(metricsProperties.getProperty("memoryThreshold"))) {
					printLogs("info", "metricsFile", "System memory performance is fine");
				} else {
					memoryThresholdViolationCount++;
					printLogs("info", "metricsFile", memoryThresholdViolationCount + " Memory Sample resulted in Violation");
					float memoryViolationPercentage = ((float) (memoryThresholdViolationCount) * 100) / totalMemorySamples;
					if (memoryViolationPercentage >= Float.valueOf(metricsProperties.getProperty("memoryViolationThreshold"))) {
						printLogs("info", "metricsFile", memoryThresholdViolationCount + " out of " + totalMemorySamples
								+ " Memory Monitoring samples resulted in violations from the permitted "
								+ metricsProperties.getProperty("memoryViolationThreshold") + "% violation so result is FAILURE");
					}
				}

				printLogs("info", "metricsFile", "=================================================");
				int processors = (Integer) obj.get("processors");
				double systemloadAverage = (Double) obj.get("systemload.average");
				printLogs("info", "metricsFile", "Total processors : " + processors);
				printLogs("info", "metricsFile", "Average Systemload Load : " + systemloadAverage);
				float cpuPerformance;
				cpuPerformance = (float) ((systemloadAverage * 100) / processors);
				printLogs("info", "metricsFile", "CPU Performance in Percentage : " + cpuPerformance);
				
				int totalCpuSamples = (Integer.parseInt(metricsProperties.getProperty("monitiorCycle"))
						* Integer.parseInt(metricsProperties.getProperty("monitorPeriod")));
				if (cpuPerformance <= Integer.parseInt(metricsProperties.getProperty("cpuThreshold"))) {
					printLogs("info", "metricsFile", "System Average Systemload Load is fine");
				} else {
					cpuThresholdViolationCount++;
					printLogs("info", "metricsFile", cpuThresholdViolationCount + " System Average Load Sample resulted in Violation");
					float cpuViolationPercentage = (((float) cpuThresholdViolationCount) / totalCpuSamples) * 100;
					if (cpuViolationPercentage >= Float.valueOf(metricsProperties.getProperty("cpuViolationThreshold"))) {
						printLogs("info", "metricsFile", cpuThresholdViolationCount + " out of " + totalCpuSamples
								+ " CPU Monitoring samples resulted in violations from the permitted "
								+ metricsProperties.getProperty("cpuViolationThreshold") + "% violation so result is FAILURE");
					}
				}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		int count = 0;
		try {
			while (true) {
				try {
					while (count < (Integer.parseInt(metricsProperties.getProperty("monitiorCycle"))
							* Integer.parseInt(metricsProperties.getProperty("monitorPeriod")))) {
						runMetrics();
						TimeUnit.MINUTES.sleep(Integer.parseInt(metricsProperties.getProperty("monitiorCycle")));
						count++;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public static void printLogs(String level, String fileName, String msg) {
		if (level.equalsIgnoreCase("error")) {
			com.jcabi.log.Logger.error(fileName, msg);
		}
		if (level.equalsIgnoreCase("info")) {
			com.jcabi.log.Logger.info(fileName, msg);
		}
	}
	
}
