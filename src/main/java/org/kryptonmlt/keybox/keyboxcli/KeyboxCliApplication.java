package org.kryptonmlt.keybox.keyboxcli;

import org.kryptonmlt.keybox.keyboxcli.utils.KeyboxUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KeyboxCliApplication {

  public static void main(String[] args) {
    SpringApplication.run(KeyboxCliApplication.class, args);
  }
}
