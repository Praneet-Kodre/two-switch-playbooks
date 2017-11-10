package com.pluribus.vcf.test;

import com.pluribus.vcf.helper.TestSetup;
import com.pluribus.vcf.helper.MonitorMetrics;
import com.pluribus.vcf.pagefactory.VCFLoginPage;
import com.pluribus.vcf.pagefactory.VCFManagerPage;
import com.pluribus.vcf.pagefactory.VCFHomePage;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;
import org.testng.annotations.Parameters;

public class VcfMgr extends TestSetup{
	private VCFHomePage home1;
	private VCFManagerPage vcfMgr1;
	private VCFLoginPage login;
	private String vcfUserName = "admin";
	private MonitorMetrics monitorMetrics;
	private Thread t;
	
	@BeforeClass(alwaysRun = true)
	public void init() throws Exception{
		login = new VCFLoginPage(getDriver());
		home1 = new VCFHomePage(getDriver());
		vcfMgr1 = new VCFManagerPage(getDriver());
	}
	
	@Parameters({"password"})
    @Test(groups = {"smoke","regression"}, description = "Login to VCF as admin  and Change Password")
    public void loginAdmin(@Optional("test123")String password) throws Exception{
		
		 login.firstlogin(vcfUserName,password); login.waitForLogoutButton();
		 login.logout(); Thread.sleep(60000);
		 
    }
	
	@Parameters({ "password", "vcfIp", "enableMetrics"})
	@Test(groups = { "smoke", "regression" }, description = "Login to VCF as test123 After Password Change")
	public void loginTest123(@Optional("test123") String password, String vcfIp,@Optional("0")  String enableMetrics) throws Exception {
		login.login(vcfUserName, password);
		Thread.sleep(1000);
		login.waitForLogoutButton();

		if(Integer.parseInt(enableMetrics)==1) {
		monitorMetrics = new MonitorMetrics(getDriver(), vcfIp, vcfUserName, password);
		t = new Thread(monitorMetrics);
		t.start();
		}
		Thread.sleep(6000);
		home1.gotoVCFMgr();
	}
	 
/*	@Parameters({"Metrics"})
	@Test(groups={"smoke","regression"},description="Check Metrics")
	public void RunMetrics(String Metrics) throws Exception {		
		Robot r = null;
		try {
			r = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		r.keyPress(KeyEvent.VK_CONTROL);
		r.keyPress(KeyEvent.VK_T);
		r.keyRelease(KeyEvent.VK_CONTROL);
		r.keyRelease(KeyEvent.VK_T);

		File metricsFile = new File(Metrics);
		monitorMetrics = new MonitorMetrics(getDriver(), metricsFile);
		Thread t = new Thread(monitorMetrics);
		t.start();
		Thread.sleep(6000);
		home1.gotoVCFMgr();
	}*/
	
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
		if(t != null) {
		t.stop();
		}
		Thread.sleep(120000); 
	}
	
	   @Parameters({"switchIp"})
	   @Test(groups={"smoke","regression"},dependsOnMethods = {"runvrrpBgpPlaybook"},description="Check L3 BGP - VRRP playbook Configuration")
	   public void upgradeVCFC( @Optional("10.110.0.85")String switchIp) throws IOException,InterruptedException {
		    Shell sh1 = new Shell.Verbose(
		            new SSHByPassword(
		            		switchIp,
		                22,
		                "network-admin",
		                "test123"
		            )
		        );
			//String out1;
			System.out.println(new Shell.Plain(sh1).exec("vrouter-show count-output"));
			System.out.println(new Shell.Plain(sh1).exec("vrouter-interface-show count-output"));
			System.out.println(new Shell.Plain(sh1).exec("vrouter-loopback-interface-show count-output"));
			System.out.println(new Shell.Plain(sh1).exec("vrouter-show format bgp-as count-output"));
			System.out.println(new Shell.Plain(sh1).exec("vrouter-show format bgp-redistribute count-output"));
			System.out.println(new Shell.Plain(sh1).exec("vrouter-show format bgp-max-paths count-output"));
			System.out.println(new Shell.Plain(sh1).exec("vrouter-bgp-neighbor-show count-output"));
			}
	
/*	@Parameters({"hostFile","vlanCsvFile", "vrrpCsvFile","ospfCsvFile","password","gatewayIp"})
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
		Thread.sleep(30000);
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
		Thread.sleep(30000);
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
	}*/
	
}
