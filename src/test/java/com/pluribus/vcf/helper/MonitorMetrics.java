package com.pluribus.vcf.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.pluribus.vcf.helper.CustomHttpClient;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MonitorMetrics extends PageInfra implements Runnable {

	private java.util.Properties metricsProperties;
	private File localMetricsFile;
	private PrintWriter printMetricsInFile;
	private String vcfIpForMetrics;
	private static StringBuffer result;
	private String metricsUserName;
	private String metricsPassword;
	int memoryThresholdViolationCount = 0;
	int cpuThresholdViolationCount = 0;

	public MonitorMetrics(WebDriver driver, File metricsFile, String vcfIp, String vcfUserName, String password) throws IOException {
		super(driver);
		this.localMetricsFile = metricsFile;
		metricsProperties = new Properties();
		metricsProperties.loadFromXML(new FileInputStream("metrics.xml"));
		this.vcfIpForMetrics = vcfIp;
		this.metricsUserName = vcfUserName;
		this.metricsPassword = password;
	}

	public void runMetrics() throws InterruptedException, IOException {



		try {

			if (localMetricsFile.exists()) {
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
				printMetricsInFile.println("=================================================");
				printMetricsInFile.println("Time when below Sample was taken : " + Calendar.getInstance().getTime());
				printMetricsInFile.println("=================================================");
				int mem = (Integer) obj.get("mem");
				int freeMem = (Integer) obj.get("mem.free");
				printMetricsInFile.println("Total Memory : " + mem);
				printMetricsInFile.println("Free Memory : " + freeMem);
				float memPerformance;
				memPerformance = (float) ((freeMem * 100) / mem);
				printMetricsInFile.println("Memory Performance in Percentage : " + memPerformance);

				int totalMemorySamples = (Integer.parseInt(metricsProperties.getProperty("monitiorCycle"))
						* Integer.parseInt(metricsProperties.getProperty("monitorPeriod")));
				if (memPerformance >= Integer.parseInt(metricsProperties.getProperty("memoryThreshold"))) {
					printMetricsInFile.println("System memory performance is fine");
				} else {
					memoryThresholdViolationCount++;
					printMetricsInFile.println(memoryThresholdViolationCount + " Memory Sample resulted in Violation");
					float memoryViolationPercentage = ((float) (memoryThresholdViolationCount) * 100) / totalMemorySamples;
					if (memoryViolationPercentage >= Float.valueOf(metricsProperties.getProperty("memoryViolationThreshold"))) {
						printMetricsInFile.println(memoryThresholdViolationCount + " out of " + totalMemorySamples
								+ " Memory Monitoring samples resulted in violations from the permitted "
								+ metricsProperties.getProperty("memoryViolationThreshold") + " violation so result is FAILURE");
					}
				}

				printMetricsInFile.println("=================================================");
				int processors = (Integer) obj.get("processors");
				double systemloadAverage = (Double) obj.get("systemload.average");
				printMetricsInFile.println("Total processors : " + processors);
				printMetricsInFile.println("Average Systemload Load : " + systemloadAverage);
				float cpuPerformance;
				cpuPerformance = (float) ((systemloadAverage * 100) / processors);
				printMetricsInFile.println("CPU Performance in Percentage : " + cpuPerformance);

				int totalCpuSamples = (Integer.parseInt(metricsProperties.getProperty("monitiorCycle"))
						* Integer.parseInt(metricsProperties.getProperty("monitorPeriod")));
				if (cpuPerformance <= Integer.parseInt(metricsProperties.getProperty("cpuThreshold"))) {
					printMetricsInFile.println("System Average Systemload Load is fine");
				} else {
					cpuThresholdViolationCount++;
					printMetricsInFile.println(cpuThresholdViolationCount + " System Average Load Sample resulted in Violation");
					float cpuViolationPercentage = (((float) cpuThresholdViolationCount) / totalCpuSamples) * 100;
					if (cpuViolationPercentage >= Float.valueOf(metricsProperties.getProperty("cpuViolationThreshold"))) {
						printMetricsInFile.println(cpuThresholdViolationCount + " out of " + totalCpuSamples
								+ " CPU Monitoring samples resulted in violations from the permitted "
								+ metricsProperties.getProperty("cpuViolationThreshold") + " violation so result is FAILURE");
					}
				}	
			} else {
				System.out.println("File doesnot exist");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		int count = 0;
		// TODO Auto-generated method stub
		try {
			printMetricsInFile = new PrintWriter(new FileOutputStream(localMetricsFile), true);
			while (true) {
				try {
					while (count < (Integer.parseInt(metricsProperties.getProperty("monitiorCycle"))
							* Integer.parseInt(metricsProperties.getProperty("monitorPeriod")))) {
						runMetrics();
						TimeUnit.MINUTES.sleep(Integer.parseInt(metricsProperties.getProperty("monitiorCycle")));
						count++;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			printMetricsInFile.close();
		}
	}

}
