package com.pluribus.vcf.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MonitorMetrics extends PageInfra implements Runnable {

	private java.util.Properties metricsProperties;
	File localMetricsFile;
	PrintWriter printMetricsInFile;

	public MonitorMetrics(WebDriver driver, File metricsFile) throws IOException {
		super(driver);
		this.localMetricsFile = metricsFile;
		metricsProperties = new Properties();
		metricsProperties.loadFromXML(new FileInputStream("Metrics.xml"));
	}

	public void runMetrics() throws InterruptedException, IOException {

		int memoryThresholdViolationCount = 0;
		int cpuThresholdViolationCount = 0;

		try {

			if (localMetricsFile.exists()) {
				ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
				
				driver.switchTo().window(tabs.get(1));
				driver.get("https://10.110.3.4/vcf-mgr/metrics");
				String json = driver.findElement(By.tagName("body")).getText();
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> obj = mapper.readValue(json, Map.class);
				printMetricsInFile.println("=================================================");
				printMetricsInFile.println("Time when below Sample was taken : " + Calendar.getInstance().getTime());
				printMetricsInFile.println("=================================================");
				int mem = (Integer) obj.get("mem");
				int free_mem = (Integer) obj.get("mem.free");
				printMetricsInFile.println("Total Memory : " + mem);
				printMetricsInFile.println("Free Memory : " + free_mem);
				float mem_performance;
				mem_performance = (float) ((free_mem * 100) / mem);
				printMetricsInFile.println("Memory Performance in Percentage : " + mem_performance);

				int totalMemorySamples = (Integer.parseInt(metricsProperties.getProperty("monitior_cycle"))
						* Integer.parseInt(metricsProperties.getProperty("monitor_period")));
				if (mem_performance > Integer.parseInt(metricsProperties.getProperty("memory_threshold"))) {
					printMetricsInFile.println("System memory performance is fine");
				} else {
					memoryThresholdViolationCount++;
					printMetricsInFile.println(memoryThresholdViolationCount + " Memory Sample resulted in Violation");
				}

				float memoryViolationPercentage = ((float) (memoryThresholdViolationCount) * 100) / totalMemorySamples;
				if (memoryViolationPercentage >= Float.valueOf(metricsProperties.getProperty("memory_violation_threshold"))) {
					printMetricsInFile.println(memoryThresholdViolationCount + " out of " + totalMemorySamples
							+ " Memory Monitoring samples resulted in violations from the permitted "
							+ metricsProperties.getProperty("memory_violation_threshold") + " violation so result is FAILURE");
				}

				printMetricsInFile.println("=================================================");
				int processors = (Integer) obj.get("processors");
				double systemload_average = (Double) obj.get("systemload.average");
				printMetricsInFile.println("Total processors : " + processors);
				printMetricsInFile.println("Average Systemload Load : " + systemload_average);
				float cpu_performance;
				cpu_performance = (float) ((systemload_average * 100) / processors);
				printMetricsInFile.println("CPU Performance in Percentage : " + cpu_performance);

				int totalCpuSamples = (Integer.parseInt(metricsProperties.getProperty("monitior_cycle"))
						* Integer.parseInt(metricsProperties.getProperty("monitor_period")));
				if (cpu_performance < Integer.parseInt(metricsProperties.getProperty("cpu_threshold"))) {
					printMetricsInFile.println("System Average Systemload Load is fine");
				} else {
					cpuThresholdViolationCount++;
					printMetricsInFile.println(cpuThresholdViolationCount + " System Average Load Sample resulted in Violation");
				}

				float cpuViolationPercentage = (((float) cpuThresholdViolationCount) / totalCpuSamples) * 100;
				if (cpuViolationPercentage > Float.valueOf(metricsProperties.getProperty("cpu_violation_threshold"))) {
					printMetricsInFile.println(cpuThresholdViolationCount + " out of " + totalCpuSamples
							+ " CPU Monitoring samples resulted in violations from the permitted "
							+ metricsProperties.getProperty("cpu_violation_threshold") + " violation so result is FAILURE");
				}

				driver.switchTo().window(tabs.get(0));
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
					while (count < (Integer.parseInt(metricsProperties.getProperty("monitior_cycle"))
							* Integer.parseInt(metricsProperties.getProperty("monitor_period")))) {
						runMetrics();
						TimeUnit.MINUTES.sleep(Integer.parseInt(metricsProperties.getProperty("monitior_cycle")));
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
