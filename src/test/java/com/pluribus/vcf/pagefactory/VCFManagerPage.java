package com.pluribus.vcf.pagefactory;

import com.jcabi.log.Logger;
import com.pluribus.vcf.helper.PageInfra;
import java.nio.file.FileSystems;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.pluribus.vcf.helper.PageInfra;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class VCFManagerPage extends PageInfra {
	
	@FindBy(how = How.CSS, using = "button.btn.btn-info")
	WebElement addFabric;
	
	String nextButtonId = "button[type=submit]";
	String fileUploadId = "hostFile";
	String vlanUpload = "input#csvFileForPlaybook[file-type='vlan_csv_file']";
	String vrrpUpload = "input#csvFileForPlaybook[file-type='vrrp_csv_file']";
	String ospfUpload = "input#csvFileForPlaybook[file-type='ospf_csv_file']";
	String bgpUpload = "input#csvFileForPlaybook[file-type='bgp_csv_file']";
	String ztpMenuWindow = "div.ngdialog-content";
	String ztpIconId = "button.icon-ztp";
	String addSeedId = "button.icon-add";
	String backButton = "button.btn.btn-info";
	String seedWindowId = "div.form-horizontal.inner-dialog-container";
	String switchSelectId = "div.col-sm-8 select";
	String netvisorEnterpriseLic = "button.btn.btn-info[type='button']";
	String userIdField = "input[placeholder='User ID']";
	String pwdField = "input[type=Password]";
	String uNameField = "userName";
	String passField = "password";	
	String devDiscoveryFields = "div.ui-grid-cell-contents.ng-binding.ng-scope";
	String nextId = "button[class='btn btn-success'][type=submit]";
	String verifyNextId = "button[class='btn btn-success'][type=button]";
	String playbookLabel = "div.col-sm-4.ng-scope span";
	String playbookImg = "img[alt='";  
    String playbookLink = "div[ref='";
    String deleteButton = "button.button";
	String closeButton = "button.button[ng-click='closeThisDialog()']";
	String playbookCloseButton = "button.button[ng-click='closeAllDialog()']";
	String fabricNodeImage = "image.fabricNode";
	String progressBar = "div.progress-bar.ng-isolate-scope.progress-bar-info span";
	String deleteIcon = "button.icon-delete.ng-scope";
	String confirmDelPopup = "div.inner-dialog-container";
	String vrrpNextButton = "button.btn.btn-success[type=submit][ng-show]";
	String fabricNameFieldSel = "input.form-control.ng-pristine.ng-untouched.ng-valid.ng-not-empty.ng-valid-required.ng-valid-pattern[value=gui-fabric]";
	String advancedButtonSel = "button[type='button'][uib-tooltip='Maximize Advanced Settings']";
	String gatewayIpSel = "input.form-control.ng-pristine.ng-untouched.ng-valid.ng-not-empty.ng-valid-required.ng-valid-pattern[value='10.9.9.1']";
	String setupTabSel = "uib-tab-heading.ng-scope";
	String toggle40gSel = "select.form-control.ng-pristine.ng-untouched.ng-valid.ng-not-empty[value='True']";
	String playbookCompletionMessageId = "success-message-dialog";
	String resetFabricCheckBox = "input[type='checkbox']";
	String errorMsg = "div.inner-dialog-container.panel.panel-danger";
	String successMsg = "div#success-message-dialog div.panel-body"; 
	String toastMsg = "div.d3-tip"; 
	
	public VCFManagerPage(WebDriver driver) {
		super(driver);
	}
	
	public boolean terminateAndCleanZtp (String vcfIp) {
		boolean status = false;
		String cleanZtpUrl = "https://"+vcfIp+"/vcf-mgr/api/ansible/terminateAndCleanZtp";
		driver.navigate().to(cleanZtpUrl);
		try {
			Thread.sleep(60000);
		} catch (Exception e) {
			
		}
		WebElement message = driver.findElement(By.tagName("body"));
		if(message.getText().contains("Process Terminated Successfully"))  status = true;
		//driver.navigate().to("https://"+vcfIp+"/vcf-mgr/index.html#/fabrics/map");
		driver.navigate().back();
		try {
			Thread.sleep(10000);
		} catch (Exception e) {
			
		}
		return status;
	}
	
	//Verify seed switch successful add
	public boolean getSeedSwitchDiscoveryStatus (String switchName, int iter) throws Exception{
		boolean status = false;
		List<WebElement> rows = new ArrayList();
		int i = 0;
		boolean found = false;
		while(i < iter) {
			rows = driver.findElements(By.cssSelector(devDiscoveryFields));
			int idx = 0;
			for (WebElement row : rows) {
				if(row.getText().equals(switchName)) {
					if(rows.get(idx+6).getText().contains("completed")) {
						status = true;
						found = true;
						break;
					} else if (rows.get(idx+6).getText().contains("failed")) {
						status = false;
						found = true;
						break;
					}
				}
				idx += 1;
				Thread.sleep(3000);
			 }
			if(found == true) break;
			i++;
		}
		return status;
	}
	
	public void delAllSeedsVcfMgr() throws Exception{
/*		waitForElementVisibility(addFabric, 100);
		addFabric.click();
		Thread.sleep(10000);
		boolean status = false;
		if(isElementActive(By.cssSelector(deleteIcon))) {
			List <WebElement> delIconList = driver.findElements(By.cssSelector(deleteIcon)); 
			for (WebElement row : delIconList) {
				row.click();
				waitForElementToClick(By.cssSelector(confirmDelPopup),100);
				List <WebElement> buttons = driver.findElements(By.cssSelector(deleteButton));
				for (WebElement button: buttons){
					if(button.getText().contains("Yes")) {
						button.click();
						break;
					}
				}
			Thread.sleep(10000); //waiting for success message to go away
			}
		}
		waitForElementToClick(By.cssSelector(backButton),100);
		addFabric.click();
		Thread.sleep(5000);
	}
	
	public void clickOnBack() throws Exception {
		boolean status = false;
		waitForElementVisibility(addFabric,100);
		waitForElementToClick(By.cssSelector(backButton),100);
		addFabric.click();
		Thread.sleep(10000);
	}
	
	public boolean launchZTP(String hostFile, String vlanCsvFile, String vrrpCsvFile, String bgpCsvFile, String ospfCsvFile, String password, String playbookName, String gatewayIp) throws Exception {
		boolean status = false;
		waitForElementVisibility(addFabric,100);
		waitForElementToClick(By.cssSelector(backButton),100);
		addFabric.click();
		//Thread.sleep(5000);	//waiting for click to go through. 
		while(!isElementActive(By.cssSelector(ztpIconId))) {
			Thread.sleep(5000);
		}
		waitForElementToClick(By.cssSelector(ztpIconId),100);
		retryingFindClick(driver.findElement(By.cssSelector(ztpIconId)));
		waitForElementToClick(By.cssSelector(netvisorEnterpriseLic),100);
		driver.findElement(By.cssSelector(netvisorEnterpriseLic)).click();
		//Thread.sleep(5000); //waiting for click to go through
		while(!isElementActive(By.cssSelector(ztpMenuWindow))) {
			Thread.sleep(5000);
		}
		waitForElementToClick(By.cssSelector(ztpMenuWindow),100);
		textToBePresentInElementValue(By.cssSelector(fabricNameFieldSel),"gui-fabric");
		WebElement fabricName = driver.findElement(By.cssSelector(fabricNameFieldSel));
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddhh");
	    String dateAsString = simpleDateFormat.format(new Date());
	    setValue(fabricName, "gui-fabric-"+dateAsString);
		WebElement element = driver.findElement(By.id(fileUploadId));
		((RemoteWebElement) element).setFileDetector(new LocalFileDetector());
		element.sendKeys(hostFile);
		setValue(driver.findElement(By.name(uNameField)),"network-admin");
		setValue(driver.findElement(By.name(passField)),password);

		List <WebElement> elements = driver.findElements(By.cssSelector(advancedButtonSel));
		elements.get(0).click();
		
		Thread.sleep(2000);
		List <WebElement> dropdowns =driver.findElements(By.cssSelector(toggle40gSel));
		for (WebElement row: dropdowns) {
			if(row.getText().contains("True")) {
				Select dropdown = new Select(row);
				dropdown.selectByVisibleText("False");
				break;
			}
		}
		
		Thread.sleep(2000);
		//setValue(driver.findElement(By.cssSelector(gatewayIpSel)),gatewayIp);
		List <WebElement> rstFabric = driver.findElements(By.cssSelector(resetFabricCheckBox));
		for (WebElement row: rstFabric) {
			if (!row.isSelected()){
					row.click();
			}
		}
		
		WebElement nextButton = driver.findElement(By.cssSelector(nextId));
		nextButton.click();
		Thread.sleep(2000); //Sleeping for the click to go through 
		
		if(isElementActive(By.cssSelector(errorMsg))) {
			String message = driver.findElement(By.cssSelector(errorMsg)).getText();
			com.jcabi.log.Logger.error("playbookConfig failed with error: ",message);
			return false;
		}
		
		boolean successMessageFound = false;
		int i = 0;
		while((i<100)&&((!isElementActive(By.cssSelector(successMsg)))||(!isElementActive(By.cssSelector(errorMsg))))) {
			Thread.sleep(100000);
			com.jcabi.log.Logger.info("playbookConfig","Waiting for fabric reset playbook execution to be completed");
			if(isElementActive(By.cssSelector(successMsg))) {
				String message = driver.findElement(By.cssSelector(successMsg)).getText();
				com.jcabi.log.Logger.info("playbookConfig",message);
				successMessageFound = true;
				driver.findElement(By.cssSelector(closeButton)).click();
				break;
			}
			if(isElementActive(By.cssSelector(errorMsg))) {
				String message = driver.findElement(By.cssSelector(errorMsg)).getText();
				com.jcabi.log.Logger.error("playbookConfigError",message);
				successMessageFound = false;
				break;
			}
			i++;
		}
		
		if(successMessageFound == false) {
			com.jcabi.log.Logger.error("playbookConfig","Fabric configured message not found. Playbook configuration timed out");
			return false;
		}
		
		List <WebElement> statusTabs = driver.findElements(By.cssSelector(progressBar));
		if((statusTabs.get(0).getText().contains("Fabric configured"))&&(statusTabs.get(1).getText().contains("Verify Topology"))) {
			com.jcabi.log.Logger.info("playbookConfig","Status bars: "+statusTabs.get(0).getText()+","+statusTabs.get(1).getText());
			com.jcabi.log.Logger.info("playbookConfig","Verified status bar message after fabric reset playbook was successfully run");
		} else {
			com.jcabi.log.Logger.error("playbookConfig","Status bar messages are incorrect"+statusTabs.get(0).getText()+" "+statusTabs.get(1).getText());
		}
		
		 //Click on Next
        i = 0;
        while ((i<5) && (!isElementActive(By.cssSelector(verifyNextId)))) {
                Thread.sleep(100000);
                if(isElementActive(By.cssSelector(verifyNextId))) break;
                i++;
        }
        waitForElementToClick(By.cssSelector(verifyNextId),100);
        nextButton = driver.findElement(By.cssSelector(verifyNextId));
        nextButton.click();
        
        //Thread.sleep(2000);	
        while(!isElementActive(By.cssSelector(playbookLabel))) {
        	Thread.sleep(2000);
        }
        
      //Select appropriate playbook
        Actions action = new Actions(driver);
        action.moveToElement(driver.findElement(By.cssSelector(playbookImg+playbookName+"']"))).perform();
        WebElement subElement = driver.findElement(By.cssSelector(playbookLink+playbookName+"']"));
        action.moveToElement(subElement);
        action.click();
        action.perform();
        
        Thread.sleep(30000);
        element = driver.findElement(By.cssSelector(vlanUpload));
    	((RemoteWebElement) element).setFileDetector(new LocalFileDetector());
    	element.sendKeys(vlanCsvFile);
    	
        if(playbookName.equals("L2 Cluster with VRRP")) {
        	element = driver.findElement(By.cssSelector(vrrpUpload));
        	((RemoteWebElement) element).setFileDetector(new LocalFileDetector());
        	element.sendKeys(vrrpCsvFile);
        }
        
        else if (playbookName.equals("L3 BGP with VRRP")) {
        	element = driver.findElement(By.cssSelector(vrrpUpload));
        	((RemoteWebElement) element).setFileDetector(new LocalFileDetector());
        	element.sendKeys(vrrpCsvFile);
        	
        	element = driver.findElement(By.cssSelector(bgpUpload));
        	((RemoteWebElement) element).setFileDetector(new LocalFileDetector());
        	element.sendKeys(bgpCsvFile);
        } 
        
        else if (playbookName.equals("L3 OSPF with VRRP")) {
        	element = driver.findElement(By.cssSelector(vrrpUpload));
        	((RemoteWebElement) element).setFileDetector(new LocalFileDetector());
        	element.sendKeys(vrrpCsvFile);
        	
        	element = driver.findElement(By.cssSelector(ospfUpload));
        	((RemoteWebElement) element).setFileDetector(new LocalFileDetector());
        	element.sendKeys(ospfCsvFile);
        }
        
        
        waitForElementToClick(By.cssSelector(vrrpNextButton),100);
        nextButton = driver.findElement(By.cssSelector(vrrpNextButton));
        retryingFindClick(nextButton);
        Thread.sleep(2000);
		
		successMessageFound = false;
		i = 0;
		while((i<100)&&((!isElementActive(By.cssSelector(successMsg)))||(!isElementActive(By.cssSelector(errorMsg))))) {
			Thread.sleep(100000);
			com.jcabi.log.Logger.info("playbookConfig","Waiting for playbook "+playbookName+" execution to be completed");
			if(isElementActive(By.cssSelector(successMsg))) {
				String message = driver.findElement(By.cssSelector(successMsg)).getText();
				com.jcabi.log.Logger.info("playbookConfig",message);
				driver.findElement(By.cssSelector(playbookCloseButton)).click();
				successMessageFound = true;
				break;
			}
			if(isElementActive(By.cssSelector(errorMsg))) {
				String message = driver.findElement(By.cssSelector(errorMsg)).getText();
				com.jcabi.log.Logger.error("playbookConfigError",message);
				successMessageFound = false;
				break;
			}
			i++;
		}
		
		if(successMessageFound == false) {
			com.jcabi.log.Logger.error("playbookConfig","Playbook "+playbookName+" did not complete. Success message was not found");
			return successMessageFound;
		}
		Thread.sleep(30000); //Sleeping to allow the fabric map to be displayed longer
		return true;
	}
	
	public boolean isElementActive(By by) {
		boolean status = false;
		
		driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
		boolean exists = (driver.findElements(by).size() != 0);
		
		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		return exists;
	}
	
	public boolean checkConfigStatus() {
		boolean status = true;
		try {
			Thread.sleep(100000);
		} catch (Exception e) {
			com.jcabi.log.Logger.error("configurePlaybook",e.toString());
		}
		driver.getCurrentUrl();
		return status;
	}
}
