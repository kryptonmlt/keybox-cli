package org.kryptonmlt.keybox.keyboxcli.utils;

import java.io.File;
import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChromeHelper {

  private final static Logger LOGGER = LoggerFactory.getLogger(ChromeHelper.class);

  private WebDriver webDriver;

  @Value("${browser.headless}")
  private boolean headless;

  @Value("${browser.driver.folder}")
  private String driverFolder;

  public WebDriver init() {
    LOGGER.info("Initializing webdriver");
    DesiredCapabilities capabilities = DesiredCapabilities.chrome();
    ChromeDriverService service = new ChromeDriverService.Builder()
        .usingDriverExecutable(new File(getLocalDriver())).build();
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--no-sandbox"); // Bypass OS security model, MUST BE THE VERY FIRST OPTION
    if (headless) {
      options.addArguments("--headless");
    }
    options.setExperimentalOption("useAutomationExtension", false);
    options.addArguments("start-maximized"); // open Browser in maximized mode
    options.addArguments("disable-infobars"); // disabling infobars
    options.addArguments("--disable-extensions"); // disabling extensions
    options.addArguments("--disable-gpu"); // applicable to windows os only
    options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
    options.merge(capabilities);
    webDriver = new ChromeDriver(service, options);
    return webDriver;
  }

  private String getLocalDriver() {
    String path = driverFolder;
    if (SystemUtils.IS_OS_WINDOWS) {
      path = driverFolder + "\\chromedriver.exe";
    } else if (SystemUtils.IS_OS_MAC) {
      path = driverFolder + "/chromedriver-mac";
    } else {
      path = driverFolder + "/chromedriver-linux";
    }
    return path;
  }

  public void exit(WebDriver webDriver) {
    LOGGER.info("Closing webdriver");
    if (webDriver == null) {
      return;
    }
    try {
      webDriver.close();
      webDriver.quit();
      webDriver = null;
    } catch (Exception e) {
      LOGGER.error("Error closing webdriver", e);
      webDriver = null;
    }
  }

  public void exit() {
    this.exit(webDriver);
  }

  public WebDriver getWebDriver() {
    if (webDriver == null) {
      webDriver = this.init();
    }
    return webDriver;
  }
}
