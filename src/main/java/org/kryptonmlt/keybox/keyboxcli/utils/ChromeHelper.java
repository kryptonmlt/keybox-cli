package org.kryptonmlt.keybox.keyboxcli.utils;

import java.io.File;
import java.net.URL;
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
    ClassLoader classLoader = getClass().getClassLoader();
    URL url = classLoader.getResource("chromedriver.exe");
    return url.getFile();
  }

  public void exit(WebDriver webDriver) {
    LOGGER.info("Closing webdriver");
    try {
      webDriver.close();
      webDriver.quit();
    } catch (Exception e) {
      LOGGER.error("Error closing webdriver", e);
    }
  }

  public void exit() {
    this.exit(webDriver);
  }

  public WebDriver getWebDriver() {
    return webDriver;
  }
}
