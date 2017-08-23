package com.pluribus.vcf.test;

import com.pluribus.vcf.helper.TestSetup;
import com.jcabi.ssh.SSHByPassword;
import com.jcabi.ssh.Shell;
import com.pluribus.vcf.helper.PageInfra;
import com.pluribus.vcf.helper.SwitchMethods;
import com.pluribus.vcf.pagefactory.VCFLoginPage;
import com.pluribus.vcf.pagefactory.VCFManagerPage;
import com.pluribus.vcf.pagefactory.VCFHomePage;

import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;

public class VcfMgr extends TestSetup{
	private VCFHomePage home1;
	private VCFManagerPage vcfMgr1;
	private VCFLoginPage login;
	private String vcfUserName = "admin";
	
	@BeforeClass(alwaysRun = true)
	public void init() throws Exception{
		login = new VCFLoginPage(getDriver());
		home1 = new VCFHomePage(getDriver());
		vcfMgr1 = new VCFManagerPage(getDriver());
	}
	
	@Parameters({"password"})
    @Test(groups = {"smoke","regression"}, description = "Login to VCF as admin  and Change Password")
    public void loginAdmin(@Optional("test123")String password) throws Exception{
        login.firstlogin(vcfUserName,password);
        login.waitForLogoutButton();
        login.logout();
        Thread.sleep(60000);
    }
	
	 @Parameters({"password"})  
	    @Test(groups = {"smoke","regression"},dependsOnMethods={"loginAdmin"},description = "Login to VCF as test123 After Password Change")
	    public void loginTest123(@Optional("test123")String password) throws Exception{
	        login.login(vcfUserName, password);
	        Thread.sleep(10000);
	        login.waitForLogoutButton();
	        Thread.sleep(60000);
	        home1.gotoVCFMgr();
	 	}
	 
	@Parameters({"hostFile","vlanCsvFile", "vrrpCsvFile","bgpCsvFile","password","gatewayIp"})
	@Test(groups={"smoke","regression"},dependsOnMethods = {"loginTest123"},description="Configure L3 BGP - VRRP playbook")
	public void runvrrpBgpPlaybook(String hostFile, String vlanCsvFile, String vrrpCsvFile, String bgpCsvFile, @Optional("test123") String password,String gatewayIp) throws Exception {
		vcfMgr1.delAllSeedsVcfMgr();
		File file1 = new File(hostFile);
		if(file1.exists()) {
			if(vcfMgr1.launchZTP(hostFile,vlanCsvFile,vrrpCsvFile,bgpCsvFile,"",password,"L3 BGP with VRRP",gatewayIp)) {
				com.jcabi.log.Logger.info("vcfMgrconfig","L3 BGP playbook were configured successfully");
			} else {
				com.jcabi.log.Logger.error("vcfMgrconfig","L3 BGP Playbook were not configured successfully");
			}
		} else {
			com.jcabi.log.Logger.error("vcfMgrconfig", "File doesn't exist");
		}
		Thread.sleep(120000); 
	}
	
	@Parameters({"hostFile","vlanCsvFile", "vrrpCsvFile","ospfCsvFile","password","gatewayIp"})
	@Test(groups={"smoke","regression"},dependsOnMethods = {"runvrrpBgpPlaybook"},description="Configure L3 OSPF - VRRP playbook")
	public void runvrrpOspfPlaybook(String hostFile, String vlanCsvFile, String vrrpCsvFile, String ospfCsvFile, @Optional("test123") String password,String gatewayIp) throws Exception {
		vcfMgr1.clickOnBack();
		vcfMgr1.delAllSeedsVcfMgr();
		File file1 = new File(hostFile);
		if(file1.exists()) {
			if(vcfMgr1.launchZTP(hostFile,vlanCsvFile,vrrpCsvFile,"",ospfCsvFile,password,"L3 OSPF with VRRP",gatewayIp)) {
				com.jcabi.log.Logger.info("vcfMgrconfig","L3 OSPF playbook were configured successfully");
			} else {
				com.jcabi.log.Logger.error("vcfMgrconfig","L3 OSPF playbook were not configured successfully");
			}
		} else {
			com.jcabi.log.Logger.error("vcfMgrconfig", "File doesn't exist");
		}
	}
	
	@Parameters({"hostFile", "vlanCsvFile", "vrrpCsvFile","password","gatewayIp"})
	@Test(groups={"smoke","regression"},dependsOnMethods = {"runvrrpOspfPlaybook"},description="Configure L2 Cluster with VRRP playbook")
	public void runvrrpPlaybook(String hostFile, String vlanCsvFile, String vrrpCsvFile, @Optional("test123") String password, String gatewayIp) throws Exception {
		vcfMgr1.clickOnBack();
    	vcfMgr1.delAllSeedsVcfMgr();
		File file1 = new File(hostFile);
		if(file1.exists()) {
			if(vcfMgr1.launchZTP(hostFile,vlanCsvFile,vrrpCsvFile,"","",password,"L2 Cluster with VRRP",gatewayIp)) {
				com.jcabi.log.Logger.info("vcfMgrconfig","VRRP Playbook were configured successfully");
			} else {
				com.jcabi.log.Logger.error("vcfMgrconfig","VRRP Playbook were not configured successfully");
			}
		} else {
			com.jcabi.log.Logger.error("vcfMgrconfig", "File doesn't exist");
		}
	}
	
	@Parameters({"hostFile", "vlanCsvFile", "password","gatewayIp"})
	@Test(groups={"smoke","regression"},dependsOnMethods = {"runvrrpPlaybook"},description="Configure L2 Cluster playbook")
	public void runL2ClusterPlaybook(String hostFile, String vlanCsvFile, @Optional("test123") String password, String gatewayIp) throws Exception {
		vcfMgr1.clickOnBack();
    	vcfMgr1.delAllSeedsVcfMgr();
		File file1 = new File(hostFile);
		if(file1.exists()) {
			if(vcfMgr1.launchZTP(hostFile,vlanCsvFile,"","","",password,"L2 Two Switch Cluster",gatewayIp)) {
				com.jcabi.log.Logger.info("vcfMgrconfig","L2 Two Switch Cluster Playbook was configured successfully");
			} else {
				com.jcabi.log.Logger.error("vcfMgrconfig","L2 Two Switch Cluster Playbook were not configured successfully");
			}
		} else {
			com.jcabi.log.Logger.error("vcfMgrconfig", "File doesn't exist");
		}
	}
	
}
