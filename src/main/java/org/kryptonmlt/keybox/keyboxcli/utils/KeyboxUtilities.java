package org.kryptonmlt.keybox.keyboxcli.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyboxUtilities {

  private final static Logger LOGGER = LoggerFactory.getLogger(KeyboxUtilities.class);
  private final static OTPUtilities otpUtilities = new OTPUtilities();

  @Value(("${keybox.user}"))
  private String username;

  @Value(("${keybox.password}"))
  private String password;

  @Value(("${keybox.qrCodeText}"))
  private String qrCodeText;

  @Value(("${keybox.url}"))
  private String keyboxUrl;

  @Autowired
  private ChromeHelper chromeHelper;

  @Autowired
  private ServerCache serverCache;

  public void login() {
    LOGGER.info("Logging in to: " + username);
    chromeHelper.getWebDriver().get(keyboxUrl);
    WebDriverWait wait = new WebDriverWait(chromeHelper.getWebDriver(), 15);
    waitForLoginToLoad(wait);
    // setup qr code text if empty
    if (qrCodeText.isEmpty()) {
      LOGGER.info("Expecting to be for first time for: " + username);
      chromeHelper.getWebDriver().findElement(By.id("loginSubmit_auth_username"))
          .sendKeys(username);
      chromeHelper.getWebDriver().findElement(By.id("loginSubmit_auth_password"))
          .sendKeys(password);
      chromeHelper.getWebDriver().findElement(By.id("login_btn")).submit();
      wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".panel-footer a")));
      chromeHelper.getWebDriver().findElement(By.cssSelector(".panel-footer a")).click();
      qrCodeText = chromeHelper.getWebDriver().findElement(By.id("shared-secret")).getText();
      qrCodeText = qrCodeText.replace("- ", "");
      savePermanentlyQrText();
      chromeHelper.getWebDriver().findElement(By.id("otpSubmit_0")).click();
      waitForLoginToLoad(wait);
    }
    chromeHelper.getWebDriver().findElement(By.id("loginSubmit_auth_username")).sendKeys(username);
    chromeHelper.getWebDriver().findElement(By.id("loginSubmit_auth_password")).sendKeys(password);
    String accessCode = otpUtilities.generateOTPAccessCode(qrCodeText);
    chromeHelper.getWebDriver().findElement(By.id("loginSubmit_auth_otpToken"))
        .sendKeys(accessCode);
    LOGGER.info("Logging in to " + username + " using accesscode " + accessCode);
    chromeHelper.getWebDriver().findElement(By.id("login_btn")).submit();
    LOGGER.info("Logged in");
  }

  private void goToSystemsPage() {
    LOGGER.info("Going to Systems page");
    List<WebElement> links = chromeHelper.getWebDriver().findElements(By.cssSelector("ul li"));
    for (WebElement link : links) {
      if (link.getText().contains("Manage")) {
        link.click();
        break;
      }
    }
    // dropdown should now appear
    links = chromeHelper.getWebDriver().findElements(By.cssSelector(".dropdown-menu li a"));
    for (WebElement link : links) {
      if (link.getText().equalsIgnoreCase("Systems")) {
        link.click();
        break;
      }
    }
    LOGGER.info("Now in Systems page");
  }

  public void extractInfoFromSystemsPage() {
    LOGGER.info("Trying to extract Servers");
    loginIfNeeded();
    goToSystemsPage();
    List<WebElement> rows = chromeHelper.getWebDriver()
        .findElements(By.cssSelector(".container table tbody tr"));
    serverCache.clearServers();
    for (WebElement row : rows) {
      List<WebElement> columns = row.findElements(By.cssSelector("td"));
      serverCache
          .addServer(columns.get(0).getText(), columns.get(1).getText(), columns.get(2).getText(),
              columns.get(3).getText());
    }
    LOGGER.info("Finished extracting Servers");
  }

  public void addServer(String name, String username, String ip, String port) {
    LOGGER.info("Adding a server ..");
    loginIfNeeded();
    chromeHelper.getWebDriver()
        .findElement(By.cssSelector(".container .btn .btn-default .add_btn .spacer .spacer-bottom"))
        .click();
    WebDriverWait wait = new WebDriverWait(chromeHelper.getWebDriver(), 15);
    wait.until(
        ExpectedConditions.textToBePresentInElementValue(By.cssSelector("h4"), "Add System"));
    chromeHelper.getWebDriver().findElement(By.id("saveSystem_hostSystem_displayNm"))
        .sendKeys(name);
    chromeHelper.getWebDriver().findElement(By.id("saveSystem_hostSystem_user")).sendKeys(username);
    chromeHelper.getWebDriver().findElement(By.id("saveSystem_hostSystem_host")).sendKeys(ip);
    chromeHelper.getWebDriver().findElement(By.id("saveSystem_hostSystem_port")).sendKeys(port);

    chromeHelper.getWebDriver()
        .findElement(By.cssSelector(".modal-dialog .btn .btn-default .submit_btn"));
    LOGGER.info("Added server with name: " + name);
  }

  private void loginIfNeeded() {
    LOGGER.info("Checking if login is needed");
    chromeHelper.getWebDriver().get(chromeHelper.getWebDriver().getCurrentUrl());
    if (chromeHelper.getWebDriver().getTitle().contains("Login")) {
      this.login();
    }
  }

  private void waitForLoginToLoad(WebDriverWait wait) {
    wait.until(ExpectedConditions.elementToBeClickable(By.id("loginSubmit_auth_username")));
  }

  private void savePermanentlyQrText() {
    try {
      File file = new File("application.properties");
      LOGGER.info("Saving OTP secret in file: " + file.getAbsolutePath());
      if (!file.exists()) {
        file.createNewFile();
        FileWriter fw = new FileWriter(file);
        fw.write("qrCodeText=" + qrCodeText);
        fw.close();
      } else {
        BufferedReader bw = new BufferedReader(new FileReader(file));
        StringBuilder sB = new StringBuilder();
        String temp;
        while ((temp = bw.readLine()) != null) {
          sB.append(temp);
          sB.append("\n");
        }
        bw.close();
        FileWriter fw = new FileWriter(file);
        fw.write(sB.toString().replace("qrCodeText=", "qrCodeText=" + qrCodeText));
        fw.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}