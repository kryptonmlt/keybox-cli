package org.kryptonmlt.keybox.keyboxcli.shell;

import org.kryptonmlt.keybox.keyboxcli.dao.Server;
import org.kryptonmlt.keybox.keyboxcli.utils.ChromeHelper;
import org.kryptonmlt.keybox.keyboxcli.utils.KeyboxUtilities;
import org.kryptonmlt.keybox.keyboxcli.utils.ServerCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class KeyboxCommands {

  @Autowired
  private ServerCache serverCache;
  @Autowired
  private KeyboxUtilities keyboxUtilities;
  @Autowired
  private ChromeHelper chromeHelper;

  @ShellMethod("Lists all servers from local cache")
  public String list(@ShellOption({"-v", "--verbose"}) boolean verbose,
      @ShellOption(defaultValue = "") String grep) {
    if (serverCache.getServers().isEmpty()) {
      keyboxUtilities.extractInfoFromSystemsPage();
    }
    StringBuilder sB = new StringBuilder();
    for (Server server : serverCache.getServers()) {

      if ((grep == null || grep.isEmpty()) || server.getName().contains(grep)) {
        sB.append(server.getName());
        if (verbose) {
          sB.append(" - ");
          sB.append(server.getUsername());
          sB.append("@");
          sB.append(server.getIp());
        }
        sB.append("\n");
      }
    }
    return sB.toString();
  }

  @ShellMethod("Reloads the local cache with the latest data in keybox")
  public String reload() {
    keyboxUtilities.extractInfoFromSystemsPage();
    return "Reloading done";
  }

  @ShellMethod("Add server to keybox")
  public String add(@ShellOption({"-N", "--name"}) String name,
      @ShellOption({"-U", "--username"}) String username,
      @ShellOption({"-I", "--ip"}) String ip,
      @ShellOption({"-P", "--port"}) String port) {
    keyboxUtilities.addServer(name, username, ip, port);
    this.reload();
    return "Server added";
  }

  @ShellMethod(value = "Exit the shell.", key = {"quit", "exit"})
  public void quit() {
    chromeHelper.exit();
    System.exit(0);
  }
}
