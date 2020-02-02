package org.kryptonmlt.keybox.keyboxcli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class KeyboxCliApplication {

  public static void main(String[] args) {
    String[] disabledCommands = {"--spring.shell.command.quit.enabled=false"};
    String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);
    SpringApplication.run(KeyboxCliApplication.class, fullArgs);
  }
}
